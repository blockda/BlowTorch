/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import com.offsetnull.bt.service.plugin.Plugin;
import com.offsetnull.bt.speedwalk.DirectionData;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.service.plugin.settings.EncodingOption;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.settings.ConfigurationLoader;
import com.offsetnull.bt.alias.AliasData;

import dalvik.system.PathClassLoader;

/** The implementation of the background Service handler.
 * 
 *	This class mostly just holds Connection objects and proxys commands made from the foreground window to the appropriate connection.
 */
public class StellarService extends Service {

	/** Message constant indicating system startup. */
	protected static final int MESSAGE_STARTUP = 0;
	/** Message constant indicating a new conenction launch. */
	protected static final int MESSAGE_NEWCONENCTION = 1;
	/** Message constant indicating that the active connection should switch to a different connection. */
	protected static final int MESSAGE_SWITCH = 2;
	/** Message constant indicating that the active connection should reload its settings. */
	protected static final int MESSAGE_RELOADSETTINGS = 3;
	/** Not really sure what this is for but I think it had something to do with debugging an ANR in the service. */
	protected static final int MESSAGE_STOPANR = 4;
	/** Duration of a short interval of time. */
	private static final int SHORT_DURATION = 300;
	/** Tracker for if the foreground window is showing or hidden. */
	private boolean mWindowShowing = true;
	/** The handler object used to coordinate multi-threaded efforts from the aidl callback onto the main thread. */
	private Handler mHandler = null;
	/** The WifiLock object. */
	private WifiManager.WifiLock mWifiLock = null;
	/** The WifiManager object. */
	private WifiManager mWifiManager = null;

	
	static {
		System.loadLibrary("sqlite3");
		System.loadLibrary("lua");
	}
	
	@Override
	public void onLowMemory() {
		//Log.e("SERVICE","The service has been requested to shore up memory usage, potentially going to be killed.");
	}
	
	/** Implementation of onStartCommand(...) Service function.
	 * 
	 *  @param intent see docs
	 *  @param flags see docs
	 *  @param startId see docs
	 *  @return see docs
	 *  @see Android Documentation for Service.onStartCommand()
	 */
	public final int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent == null) {
			return Service.START_STICKY_COMPATIBILITY;
		}
		if (ConfigurationLoader.isTestMode(this.getApplicationContext())) {
			//Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this.getApplicationContext()));
			Log.e("BLOWTORCH", "SHOULD SET THE UNCAUGHT EXCEPTION HANDLER HERE.");
		}
		return Service.START_STICKY_COMPATIBILITY;
	}

	/** The implementation of the onCreate() Service method. */
	public final void onCreate() {
		
		connections = new HashMap<String, Connection>();
		
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
		
		SharedPreferences prefs = this.getSharedPreferences("SERVICE_INFO", 0);
		
		int libsver = prefs.getInt("CURRENT_LUA_LIBS_VERSION", 0);
		Bundle meta = null;
		try {
			meta = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int packagever = meta.getInt("BLOWTORCH_LUA_LIBS_VERSION");
		if (packagever != libsver) {
			//copy new libs.
			try {
				updateLibs();
			} catch (NameNotFoundException e) {
				e.printStackTrace(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mHandler = new Handler(new ServiceHandler());

	}
	
	/** There are a few things that are needed to be handled on the main thread and the aidl bridge makes
	 * that difficult, so this is used to aggregate code onto the main thread rather than the dispatch threads.
	 */
	private class ServiceHandler implements Handler.Callback {
		@Override
		public boolean handleMessage(final Message msg) {
			switch(msg.what) {
			case MESSAGE_RELOADSETTINGS:
				connections.get(connectionClutch).reloadSettings();
				reloadWindows();
				break;
			case MESSAGE_STARTUP:
				if (connections.get(connectionClutch).getPump() == null) {
					connections.get(connectionClutch).getHandler().sendEmptyMessage(Connection.MESSAGE_STARTUP);
				}
				break;
			case MESSAGE_NEWCONENCTION:
				Bundle b = msg.getData();
				String display = b.getString("DISPLAY");
				String host = b.getString("HOST");
				int port = b.getInt("PORT");
				
				Connection c = connections.get(display);
				if (c == null) {
					//make new conneciton.
					connectionClutch = display;
					c = new Connection(display, host, port, StellarService.this);
					connections.put(connectionClutch, c);
					c.initWindows();
				}
				break;
			case MESSAGE_SWITCH:
				switchTo((String) msg.obj);
				break;
			default:
				break;
			}
			return true;
		}
	}
	
	/** Implementation of the Service.onDestroy() method. */
	public final void onDestroy() {
		doShutdown();
		super.onDestroy();
	}
	
	/** The top level disconnect function.
	 * 
	 * This method is initated by a connection when it has disconnected. This method
	 * interfaces with the foreground window if appropriate to show the disconnection message.
	 *
	 * @param c The connection that disconnected.
	 */
	public final void doDisconnect(final Connection c) {
		//attempt to display the disconnection dialog.
		if (c.getDisplay().equals(connectionClutch)) {
		
			final int n = callbacks.beginBroadcast();
			for (int i = 0; i < n; i++) {
				try {
					callbacks.getBroadcastItem(i).doDisconnectNotice(c.getDisplay());
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
			
			if (n < 1) {
				ShowDisconnectedNotification(c, c.getDisplay(), c.getHost(), c.getPort());
			}
		} else {
			ShowDisconnectedNotification(c, c.getDisplay(), c.getHost(), c.getPort());
		}
		
	}
	
	/** Dispatches an error dialog in the foreground window.
	 * 
	 * @param error The error message to show.
	 * @throws RemoteException Thrown when something has gone wrong with the aidl bridge.
	 */
	public final void dispatchXMLError(final String error) throws RemoteException {
		final int n = callbacks.beginBroadcast();
		for (int i = 0; i < n; i++) {
			callbacks.getBroadcastItem(i).displayXMLError(error);
		}
		callbacks.finishBroadcast();
	}

	/** Enables Wifi KeepAlive. */
	public final void enableWifiKeepAlive() {
		//get the wifi manager
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		}
			
		//check if we are connected to a wifi network
		WifiInfo info = mWifiManager.getConnectionInfo();
		if (info.getNetworkId() != -1) {
			//if so, grab the lock
			//Log.e("SERVICE","ATTEMPTING TO GRAB WIFI LOCK");
			mWifiLock = mWifiManager.createWifiLock("BLOWTORCH_WIFI_LOCK");
			boolean held = false;
			while (!held) {
				mWifiLock.acquire();
				held = mWifiLock.isHeld();
			}
		}
	}
	
	/** The implementation of the bell vibrator. Connections will call this. */
	public final void doVibrateBell() {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(SHORT_DURATION);
	}
	

	/** The implementation ofthe bell notifier. Connections will call this.
	 * 
	 * @param display The display name of the calling connection.
	 * @param host The host name of the calling connection.
	 * @param port The port number for the calling connection.
	 */
	@SuppressWarnings("deprecation")
	public final void doNotifyBell(final String display, final String host, final int port) { 
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		Notification note = new Notification(resId, "Alert!", System.currentTimeMillis());
		
		Context context = getApplicationContext();
		CharSequence contentTitle = display + " - Alert!";
		CharSequence contentText = "The server is notifying you with the bell character, 0x07.";
		Intent notificationIntent = null;
		String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this.getApplicationContext());
		notificationIntent = new Intent(windowAction);
		notificationIntent.putExtra("DISPLAY", display);
		notificationIntent.putExtra("HOST", host);
		notificationIntent.putExtra("PORT", Integer.toString(port));
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
		note.flags = Notification.DEFAULT_ALL;
		
		mNotificationManager.notify(StellarService.getNotificationId(), note);
	}
	
	/** Implementation of the visual bell callback. Called from a Connection. */
	public final void doDisplayBell() {
		final int n = callbacks.beginBroadcast();
		for (int i = 0; i < n; i++) {
			try {
				callbacks.getBroadcastItem(i).doVisualBell();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
		callbacks.finishBroadcast();
	}
	
	/** Disables the wifi keep alive. */
	public final void disableWifiKeepAlive() {
		//if we have a wifi lock, release it
		if (mWifiLock != null) {
			mWifiLock.release();
			mWifiLock = null;
		}
	}
	
	/** Utility method for dispatching a toast message to be displayed by the foreground window. 
	 * 
	 * @param message The message to show.
	 * @param longtime true for Toast.LONG, false for Toast.SHORT
	 */
	public final void dispatchToast(final String message, final boolean longtime) {
		final int n = callbacks.beginBroadcast();
		for (int i = 0; i < n; i++) {
			try {
				callbacks.getBroadcastItem(i).showMessage(message, longtime);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	
	/** Utility method for dispatching a generic error looking dialog on the foreground window.
	 * 
	 * @param message The message to display.
	 */
	public final void dispatchDialog(final String message) {
		final int n = callbacks.beginBroadcast();
		for (int i = 0; i < n; i++) {
			try {
				callbacks.getBroadcastItem(i).showDialog(message);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	
	ArrayList<String> timer_actions;

	
	boolean firstDispatch = true;

	
	private static int notificationCount = 100;
	public static int getNotificationId() {
		notificationCount += 1;
		return new Integer(notificationCount);
	}
	
	boolean debug = false;
	
	private void ShowDisconnectedNotification(Connection c,String display,String host,int port) {
		//if we are here it means that the server has explicitly closed the connection, and nobody was around to see it.
		c.shutdown(); //call this to make sure all net threads are really dead, and to remove the ongoing notification and re-set the foreground notification if need be.
		connections.remove(display);
		
		//mNM.cancel(5545);
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		CharSequence brandName = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
		Notification note = new Notification(resId,brandName + " Disconnected",System.currentTimeMillis());
		//String defaultmsg = "Click to reconnect: "+ host +":"+ port;
		Context context = getApplicationContext();
		CharSequence contentTitle = brandName + " Disconnected";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = null;
		String message = "Click to reconnect: " + host + ":" + port;;
		if(message != null && !message.equals("")) {
			contentText = message;
		} else {
			//contentText = defaultmsg;
		}
		Intent notificationIntent = null;
		String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this.getApplicationContext());
		notificationIntent = new Intent(windowAction);
		
		String apkName = null;
		try {
			apkName = this.getPackageManager().getApplicationInfo(this.getPackageName(), 0).sourceDir;
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		Class<?> w = null;
    	PathClassLoader cl = new dalvik.system.PathClassLoader(apkName,ClassLoader.getSystemClassLoader());
    	try {
			w = Class.forName("com.offsetnull.bt.window.MainWindow",false,cl);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	
		
		try {
			notificationIntent.setClass(this.createPackageContext(this.getPackageName(), Context.CONTEXT_INCLUDE_CODE), w);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		notificationIntent.putExtra("DISPLAY",display);
		notificationIntent.putExtra("HOST", host);
		notificationIntent.putExtra("PORT", Integer.toString(port));
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		int id = getNotificationId();
		PendingIntent contentIntent = PendingIntent.getActivity(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
		note.flags = note.flags | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
		Pattern invalidchars = Pattern.compile("\\W"); 
		//Matcher replacebadchars = invalidchars.matcher(display);
		//String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		//SharedPreferences sprefs = this.getSharedPreferences(prefsname,0);
		//SharedPreferences.Editor editor = sprefs.edit();
		//editor.putBoolean("CONNECTED", false);
		//editor.putBoolean("FINISHSTART", true);
		//editor.commit();
		//editor.commit();
		//this.stopForeground(true);
		mNotificationManager.notify(id,note);
		showdcmessage = true;
		
		//now, if the launcher connection list has a listener, we should notify it that a connection has gone
		int N = launcherCallbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				launcherCallbacks.getBroadcastItem(i).connectionDisconnected();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		launcherCallbacks.finishBroadcast();
		//this.stopSelf();
	}
	
//	public void showNotification() {
//		
//		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
//		
//		//Debug.waitForDebugger();
//		Notification note = new Notification(resId,"BlowTorch Connected",System.currentTimeMillis());
//		Context context = getApplicationContext();
//		
//		CharSequence contentTitle = null;
//		CharSequence contentText = null;
//		if(connections.size() > 1) {
//			contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
//			contentText = connections.size() + " connections";
//		} else if(connections.size() == 1){
//			Connection c = connections.get(connectionClutch);
//			contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
//			contentText = "Connected: ("+ c.host +":"+ c.port + ")";
//		} else {
//			contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
//			contentText = "Not connected.";
//		}
//		
//		Intent notificationIntent = null;
//		String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this.getApplicationContext());
//		notificationIntent = new Intent(windowAction);
//		
//		String apkName = null;
//		try {
//			apkName = this.getPackageManager().getApplicationInfo(this.getPackageName(), 0).sourceDir;
//		} catch (NameNotFoundException e1) {
//			e1.printStackTrace();
//		}
//		Class<?> w = null;
//    	PathClassLoader cl = new dalvik.system.PathClassLoader(apkName,ClassLoader.getSystemClassLoader());
//    	try {
//			w = Class.forName("com.offsetnull.bt.window.MainWindow",false,cl);
//		} catch (ClassNotFoundException e1) {
//			e1.printStackTrace();
//		}
//	
//		
//		try {
//			notificationIntent.setClass(this.createPackageContext(this.getPackageName(), Context.CONTEXT_INCLUDE_CODE), w);
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		if(connections.size() > 0) {
//		Connection c = connections.get(connectionClutch);
//		notificationIntent.putExtra("DISPLAY", c.display);
//		notificationIntent.putExtra("HOST", c.host);
//		notificationIntent.putExtra("PORT", Integer.toString(c.port));
//		}
//		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//	
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//		note.icon = resId;
//		note.flags = Notification.FLAG_ONGOING_EVENT;
//		this.startForeground(5545, note);
//		
//		
//	}
	
boolean hasForegroundNotification = false;
int foregroundNotificationId = -1;
HashMap<String,Integer> mConnectionNotificationIdMap = new HashMap<String,Integer>();
HashMap<String,Notification> mConnectionNotificationMap = new HashMap<String,Notification>();
NotificationManager mNotificationManager = null;

public void showConnectionNotification(String display,String host,int port) {
	if(mConnectionNotificationMap.containsKey(display)) { return; }
	int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
	
	//Debug.waitForDebugger();
	Notification note = new Notification(resId,"BlowTorch Connected",System.currentTimeMillis());
	Context context = getApplicationContext();
	
	CharSequence contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
	CharSequence contentText = "Connected: ("+ host +":"+ port + ")";
	
//	if(connections.size() > 1) {
//		contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
//		contentText = connections.size() + " connections";
//	} else if(connections.size() == 1){
//		Connection c = connections.get(connectionClutch);
//		contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
//		contentText = "Connected: ("+ c.host +":"+ c.port + ")";
//	} else {
//		contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
//		contentText = "Not connected.";
//	}
	
	Intent notificationIntent = null;
	String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this.getApplicationContext());
	notificationIntent = new Intent(windowAction);
	
	String apkName = null;
	try {
		apkName = this.getPackageManager().getApplicationInfo(this.getPackageName(), 0).sourceDir;
	} catch (NameNotFoundException e1) {
		e1.printStackTrace();
	}
	Class<?> w = null;
	PathClassLoader cl = new dalvik.system.PathClassLoader(apkName,ClassLoader.getSystemClassLoader());
	try {
		w = Class.forName("com.offsetnull.bt.window.MainWindow",false,cl);
	} catch (ClassNotFoundException e1) {
		e1.printStackTrace();
	}

	
	try {
		notificationIntent.setClass(this.createPackageContext(this.getPackageName(), Context.CONTEXT_INCLUDE_CODE), w);
	} catch (NameNotFoundException e) {
		e.printStackTrace();
	}
	
	//if(connections.size() > 0) {
	//Connection c = connections.get(connectionClutch);
	notificationIntent.putExtra("DISPLAY", display);
	notificationIntent.putExtra("HOST", host);
	notificationIntent.putExtra("PORT", Integer.toString(port));
	//}
	int notificationID = -1;
	if(mConnectionNotificationIdMap.containsKey(display)) {
		notificationID = mConnectionNotificationIdMap.get(display);
	} else {
		notificationID = getNotificationId();
	}
	
	
	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);

	PendingIntent contentIntent = PendingIntent.getActivity(this, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	note.icon = resId;
	note.flags = Notification.FLAG_ONGOING_EVENT;
	
	
	
	if(!hasForegroundNotification) {
		foregroundNotificationId = notificationID;
		this.startForeground(foregroundNotificationId, note);
		hasForegroundNotification = true;
	} else {
		//int notificationId = notificationID;
		mNotificationManager.notify(notificationID, note);
	}
	
	mConnectionNotificationIdMap.put(display, notificationID);
	mConnectionNotificationMap.put(display, note);
}

public void removeConnectionNotification(String display) {
	if(!mConnectionNotificationIdMap.containsKey(display)) {return;}
	int id = mConnectionNotificationIdMap.get(display);
	mConnectionNotificationIdMap.remove(display);
	mConnectionNotificationMap.remove(display);
	if(id != foregroundNotificationId) {
		//just kill off the notification
		mNotificationManager.cancel(id);
	} else {
		this.stopForeground(true);
		
		//get the first connection and make it the new foreground notification
		if(mConnectionNotificationMap.size() == 0) { connectionClutch = ""; return; }
		String[] tmp = new String[mConnectionNotificationMap.size()];
		tmp = mConnectionNotificationMap.values().toArray(tmp);
		int tmp_id = mConnectionNotificationIdMap.get(tmp[0]);
		connectionClutch = tmp[0];
		Notification tmp_note = mConnectionNotificationMap.get(tmp[0]);
		mNotificationManager.cancel(tmp_id);
		this.startForeground(tmp_id, tmp_note);
	}
	
}
	
	public void killNetThreads() {
		//if(pump != null) {
		//	pump.handler.sendEmptyMessage(DataPumper.MESSAGE_END);
		//	pump = null;
		//}
		
		//if(pump != null) {
			/*try {
				output_writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}	
			
			output_writer = null;*/
		//}
		
		/*if(the_socket != null) {
			try {
				the_socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			the_socket = null;
		}*/
	}
	boolean showdcmessage = false;
	public void doShutdown() {
		//pump.stop();
//		the_timer.cancel();
//		
//		killNetThreads();
//		//kill the notification.
//		mNM.cancel(5545);
//		if(!showdcmessage) {
//			mNM.cancelAll();
//		}
		
		for(Connection c : connections.values()) {
			//c.killNetThreads();
			c.shutdown();
			
			c = null;
		}
		
		this.stopForeground(true);
		
		Log.e("service","service termination complete");
		this.stopSelf();
		
		
	}
	
	//public void doProcess(byte[] data) {
		//broadcast this data.
	//}
	
	private HashMap<String,Connection> connections = null;
	String connectionClutch = "";
	
	public void onRebind(Intent i) {
		Log.e("LOG","REBIND CALLED");
		
	}
	
	public boolean onUnbind(Intent i) {
		Log.e("SERVICE","IN ONBIND");
		super.onUnbind(i);
		return true;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		//Log.e("SERVICE","onBind Called");
		return mBinder;
//		SharedPreferences p = this.getSharedPreferences("CONNECT_TO", Context.MODE_PRIVATE);
//		String display = p.getString("CONNECT_TO", "DEFAUT");
//		//edit.commit();
//		Log.e("LOG","ATTEMPTING CONNECTION TO:" + display);
//		Connection currentConnection = connections.get(display);
//		if(currentConnection != null) {
//			connectionClutch = display;
//			return currentConnection.mBinder;
//			
//		} else {
//			connectionClutch = display;
//			
//			Connection c = new Connection(connectionClutch,"not",8784,this);
//			
//			connections.put(connectionClutch, c);
//			return c.mBinder;
//		}
	}
	
	
	/*IBinder startConnection(String display, String host, int port) {
		Connection c = new Connection(display,host,port,this);
		connectionClutch = display;
		connections.put(connectionClutch, c);
		return c.mBinder;
	}*/

	private int calculate80CharFontSize() {
		int windowWidth = this.getResources().getDisplayMetrics().widthPixels;
		if(this.getResources().getDisplayMetrics().heightPixels > windowWidth) {
			windowWidth = this.getResources().getDisplayMetrics().heightPixels;
		}
		float fontSize = 8.0f;
		float delta = 1.0f;
		Paint p = new Paint();
		p.setTextSize(8.0f);
		//p.setTypeface(Typeface.createFromFile(service.getFontName()));
		p.setTypeface(Typeface.MONOSPACE);
		boolean done = false;
		
		float charWidth = p.measureText("A");
		float charsPerLine = windowWidth / charWidth;
		
		if(charsPerLine < 80.0f) {
			//for QVGA screens, this test will always fail on the first step.
			done = true;
		} else {
			fontSize += delta;
			p.setTextSize(fontSize);
		}
		
		while(!done) {
			charWidth = p.measureText("A");
			charsPerLine = windowWidth / charWidth;
			if(charsPerLine < 80.0f) {
				done = true;
				fontSize -= delta; //return to the previous font size that produced > 80 characters.
			} else {
				fontSize += delta;
				p.setTextSize(fontSize);
			}
		}
		return (int)fontSize;
	}

	public void setClutch(String connection) {
		connectionClutch = connection;
	}

	//ConnectionSettingsPlugin the_settings = null;
	public void switchTo(String display) {
		setClutch(display);
		int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).markWindowsDirty();
				callbacks.getBroadcastItem(i).loadWindowSettings();
				callbacks.getBroadcastItem(i).loadSettings();
				callbacks.getBroadcastItem(i).reloadBuffer();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
		
	}
	
	/*public void startNewConnection(String host, int port, String display) {
		Connection c = connections.get(display);
		if(c == null) { //should be.
			Connection tmp = new Connection(display,host,port,this);
			//tmp.
			//tmp.
			connections.put(display, tmp);
			
		}
		
	}*/
	
	public void reloadWindows() {
		//Log.e("SERVER","INITIALIZE WINDOWS INITIATED: reloadWindows");
		int N = callbacks.beginBroadcast();
		
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(0).loadWindowSettings();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		callbacks.finishBroadcast();
		
	}
	
	public RemoteCallbackList<IConnectionBinderCallback> callbacks = new RemoteCallbackList<IConnectionBinderCallback>();
	public RemoteCallbackList<ILauncherCallback> launcherCallbacks = new RemoteCallbackList<ILauncherCallback>();
	IConnectionBinder.Stub mBinder = new IConnectionBinder.Stub() {

		public void registerCallback(IConnectionBinderCallback c,String host,int port,String display)
				throws RemoteException {
			// TODO Auto-generated method stub
			if(c != null) {
				callbacks.register(c);
				//if(pump == null) {
					//Log.e("SERVICE","STARTING UP CONNECTION");
					//doStartup();
					
				//}
				//String host = c.getHost();
				//String display = c.getDisplay();
				//int port = c.getPort();
				if(!connections.containsKey(display)) {
					this.setConnectionData(host, port, display);
					//this.initXfer();
				} else {
					connectionClutch = display;
					c.loadWindowSettings();
				}
				
				
				
				//do the work to start up a connection.	
			}
		}

		public void unregisterCallback(IConnectionBinderCallback c)
				throws RemoteException {
			if(c !=  null) {
				callbacks.unregister(c);
			}
		}
		
		public void registerLauncherCallback(ILauncherCallback c) {
			if(c!=null) {
				launcherCallbacks.register(c);
			}
		}
		
		public void unregisterLauncherCallback(ILauncherCallback c) {
			if(c!=null) {
				launcherCallbacks.unregister(c);
			}
		}

		public int getPid() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void initXfer() throws RemoteException {
			//handler.sendEmptyMessage(MESSAGE_STARTUP);
			
			mHandler.sendEmptyMessage(MESSAGE_STARTUP);
		}

		public void endXfer() throws RemoteException {
			//doStartup();
			Connection c = connections.get(connectionClutch);
			c.sendDataToWindow("\n"+Colorizer.colorRed + "Connection terminated by user."+Colorizer.colorWhite+"\n\n");
			c.killNetThreads(true);
			connections.get(connectionClutch).doDisconnect(true);
		}

		public boolean hasBuffer() throws RemoteException {
			/*Connection c = connections.get(connectionClutch);
			if(c == null) {
				//dispatch error
			} else {
				if(c.buffer.getBrokenLineCount() > 0) {
					return true;
				}
			}*/
			return false;
		}

		public boolean isConnected() throws RemoteException {
			if(connections.size() < 1) {
				return false;
			}
			return connections.get(connectionClutch).isConnected();
		}

		public void sendData(byte[] seq) throws RemoteException {
			Handler handler = connections.get(connectionClutch).getHandler();
			handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_SENDDATA_BYTES, seq));
		}

		public void saveSettings() throws RemoteException {
			Connection c = connections.get(connectionClutch);
			if(c == null) return;
			c.saveMainSettings();
		}

		public void setNotificationText(CharSequence seq)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setConnectionData(String host, int port, String display)
				throws RemoteException {
			Message msg = mHandler.obtainMessage(MESSAGE_NEWCONENCTION);
			Bundle b = msg.getData();
			b.putString("DISPLAY",display);//, value)
			b.putString("HOST",host);
			b.putInt("PORT",port);
			msg.setData(b);
			mHandler.sendMessage(msg);
			
		}

		public void beginCompression() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void stopCompression() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void requestBuffer() throws RemoteException {
			/*Connection c = connections.get(connectionClutch);
			if(c == null) {
				//dispatch error.
				return;
			} 
			int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				synchronized(c.buffer) {
					callbacks.getBroadcastItem(i).rawDataIncoming(c.buffer.dumpToBytes(true));
				}
			}
			callbacks.finishBroadcast();*/
		}

		public void saveBuffer(byte[] buffer) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void addAlias(AliasData a) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public List getSystemCommands() throws RemoteException {
			// TODO Auto-generated method stub
			return connections.get(connectionClutch).getSystemCommands();
		}

		public Map getAliases() throws RemoteException {
			// TODO Auto-generated method stub
			return connections.get(connectionClutch).getAliases();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void setAliases(Map map) throws RemoteException {
			connections.get(connectionClutch).setAliases((HashMap<String, AliasData>) map);
		}

		public void LoadSettingsFromPath(String path) throws RemoteException {
			connections.get(connectionClutch).startLoadSettingsSequence(path);
		}

		public void ExportSettingsToPath(String path) throws RemoteException {
			connections.get(connectionClutch).exportSettings(path);
		}

		public void resetSettings() throws RemoteException {
			connections.get(connectionClutch).resetSettings();
		}

		public Map getTriggerData() throws RemoteException {
			// TODO Auto-generated method stub
			HashMap<String,TriggerData> triggers = connections.get(connectionClutch).getTriggers();
			
			return triggers;
		}
		
		public Map getPluginTriggerData(String id) throws RemoteException {
			
			return connections.get(connectionClutch).getPluginTriggers(id);
		}

		public Map getDirectionData() throws RemoteException {
			
			return connections.get(connectionClutch).getDirectionData();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void setDirectionData(Map data) throws RemoteException {
			connections.get(connectionClutch).setDirectionData((HashMap<String, DirectionData>) data);
		}

		public void newTrigger(TriggerData data) throws RemoteException {
			connections.get(connectionClutch).addTrigger(data);
		}

		public void updateTrigger(TriggerData from, TriggerData to)
				throws RemoteException {
			connections.get(connectionClutch).updateTrigger(from,to);
			
		}

		public void deleteTrigger(String which) throws RemoteException {
			connections.get(connectionClutch).deleteTrigger(which);
		}

		public TriggerData getTrigger(String pattern) throws RemoteException {
			// TODO Auto-generated method stub
			return connections.get(connectionClutch).getTrigger(pattern);
		}

		public boolean isKeepLast() throws RemoteException {
			//return the_settings.isKeepLast();
			return connections.get(connectionClutch).isKeepLast();
		}

		

		public void setDisplayDimensions(int rows, int cols)
				throws RemoteException {
			Connection c = connections.get(connectionClutch);
			if(c == null) {
				//dispatch error.
			}
			
			c.getProcessor().setDisplayDimensions(rows, cols);
		}

		public void reconnect(String str) throws RemoteException {
			if(str == null || str.equals("")) str = connectionClutch;
			connections.get(str).doReconnect();
		}

		public Map getTimers() throws RemoteException {
			// TODO Auto-generated method stub
			return connections.get(connectionClutch).getTimers();
		}
		
		public Map getPluginTimers(String plugin) throws RemoteException {
			return connections.get(connectionClutch).getPluginTimers(plugin);
		}

		public TimerData getTimer(String ordinal) throws RemoteException {
			// TODO Auto-generated method stub
			return connections.get(connectionClutch).getTimer(ordinal);
		}

		public void startTimer(String ordinal) throws RemoteException {
			connections.get(connectionClutch).playTimer(ordinal);
		}

		public void pauseTimer(String ordinal) throws RemoteException {
			connections.get(connectionClutch).pauseTimer(ordinal);
		}

		public void stopTimer(String ordinal) throws RemoteException {
			connections.get(connectionClutch).stopTimer(ordinal);
		}
		
		public void startPluginTimer(String plugin,String ordinal) throws RemoteException {
			connections.get(connectionClutch).playPluginTimer(plugin,ordinal);
		}

		public void pausePluginTimer(String plugin,String ordinal) throws RemoteException {
			connections.get(connectionClutch).pausePluginTimer(plugin,ordinal);
		}

		public void stopPluginTimer(String plugin,String ordinal) throws RemoteException {
			connections.get(connectionClutch).stopPluginTimer(plugin,ordinal);
		}

		public void updateTimer(TimerData old, TimerData newtimer)
				throws RemoteException {
			connections.get(connectionClutch).updateTimer(old,newtimer);
		}

		public void addTimer(TimerData newtimer) throws RemoteException {
			connections.get(connectionClutch).addTimer(newtimer);
		}

		public void removeTimer(TimerData deltimer) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getNextTimerOrdinal() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public Map getTimerProgressWad() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getEncoding() throws RemoteException {
			// TODO Auto-generated method stub
			return (String)((EncodingOption)connections.get(connectionClutch).getSettings().findOptionByKey("encoding")).getValue();
		}

		public String getConnectedTo() throws RemoteException {
			return connectionClutch;
		}
		
		

		public boolean isFullScreen() throws RemoteException {
			return connections.get(connectionClutch).isFullScren();
		}

		

		public void setTriggerEnabled(boolean enabled, String key)
				throws RemoteException {
			connections.get(connectionClutch).setTriggerEnabled(enabled, key);
			
		}

		public void setButtonSetLocked(boolean locked, String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isButtonSetLocked(String key) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isButtonSetLockedMoveButtons(String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isButtonSetLockedNewButtons(String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isButtonSetLockedEditButtons(String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void startNewConnection(String host, int port, String display)
				throws RemoteException {
			//StellarService.this.startConnection(display,host,port);
		}

		public void switchTo(String display) throws RemoteException {
			mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SWITCH,display));
		}

		public boolean isConnectedTo(String display) throws RemoteException {
			return connections.keySet().contains(display);
				//return true;
			//}
		}

		public List getConnections() throws RemoteException {
			List<String> tmp = new ArrayList<String>();
			for(String key : connections.keySet()) {
				tmp.add(key);
			}
			return tmp;
		}

		public WindowToken[] getWindowTokens() throws RemoteException {
			if(connections == null || connections.size() == 0) return null;
			return connections.get(connectionClutch).getWindows();
		}

		public void registerWindowCallback(String displayName,String name,IWindowCallback callback)
				throws RemoteException {
			Log.e("SERVICE","ATTEMPTING TO SET WINDOW CALLBACK FOR:" + displayName + " window:"+name);
			Connection c = connections.get(displayName);
			if(c != null) {
				c.registerWindowCallback(name, callback);
			} else {
				Log.e("SERVICE","CONNECTION WAS NOT FOUND FOR: "+displayName);
			}
		}

		public void unregisterWindowCallback(String name,
				IWindowCallback callback) throws RemoteException {
			Connection c = connections.get(name);
			if(c != null) {
				c.unregisterWindowCallback(callback);
			}
		}

		public String getScript(String plugin, String name)
				throws RemoteException {
			return connections.get(connectionClutch).getScript(plugin,name);
			//return null;
		}

		public void reloadSettings() throws RemoteException {
			mHandler.sendEmptyMessage(MESSAGE_RELOADSETTINGS);
			
		}

		public void pluginXcallS(String plugin, String function, String str)
				throws RemoteException {
			connections.get(connectionClutch).pluginXcallS(plugin,function,str);
		}

		public Map getPluginList() throws RemoteException {
			
			Connection c = connections.get(connectionClutch);
			HashMap<String,String> list = new HashMap<String,String>();
			
			for(Plugin p : c.getPlugins()) {
				String info = "";
				info += p.getTriggerCount() + " T, ";
				info += p.getAliasCount() + " A, ";
				info += p.getTimerCount() + " C, ";
				info += p.getScriptCount() + " S, ";
				info += p.getStorageType();
				list.put(p.getName(), info);
			}
			
			return list;
		}
		
		public List getPluginsWithTriggers() {
			ArrayList<String> list = new ArrayList<String>();
			Connection c = connections.get(connectionClutch);
			for(Plugin p : c.getPlugins()) {
				if(p.getSettings().getTriggers().size() > 0) {
					list.add(p.getName());
				}
			}
			return list;
		}

		public void newPluginTrigger(String selectedPlugin, TriggerData data)
				throws RemoteException {
			connections.get(connectionClutch).newPluginTrigger(selectedPlugin,data);
		}

		public void updatePluginTrigger(String selectedPlugin,
				TriggerData from, TriggerData to) throws RemoteException {
			connections.get(connectionClutch).updatePluginTrigger(selectedPlugin,from,to);
		}

		public TriggerData getPluginTrigger(String selectedPlugin,String pattern)
				throws RemoteException {
			return connections.get(connectionClutch).getPluginTrigger(selectedPlugin,pattern);
		}

		public void setPluginTriggerEnabled(String selectedPlugin,
				boolean enabled, String key) throws RemoteException {
			connections.get(connectionClutch).setPluginTriggerEnabled(selectedPlugin,enabled,key);
		}

		public void deletePluginTrigger(String selectedPlugin, String which)
				throws RemoteException {
			connections.get(connectionClutch).deletePluginTrigger(selectedPlugin,which);
		}

		public AliasData getAlias(String key) throws RemoteException {
			
			return connections.get(connectionClutch).getAlias(key);
		}

		public AliasData getPluginAlias(String plugin, String key)
				throws RemoteException {
			
			return connections.get(connectionClutch).getPluginAlias(plugin,key);
		}

		public Map getAliases(String currentPlugin)
				throws RemoteException {
			
			return connections.get(connectionClutch).getAliases();
		}
		
		public Map getPluginAliases(String currentPlugin) {
			return connections.get(connectionClutch).getPluginAliases(currentPlugin);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void setPluginAliases(String plugin, Map map)
				throws RemoteException {
			connections.get(connectionClutch).setPluginAliases(plugin,(HashMap<String, AliasData>) map);
		}

		public void deleteAlias(String key) throws RemoteException {
			connections.get(connectionClutch).deleteAlias(key);
		}

		
		public void deletePluginAlias(String plugin, String key)
				throws RemoteException {
			connections.get(connectionClutch).deletePluginAlias(plugin,key);
		}

		@Override
		public void setAliasEnabled(boolean enabled, String key)
				throws RemoteException {
			connections.get(connectionClutch).setAliasEnabled(enabled,key);
			
		}

		@Override
		public void setPluginAliasEnabled(String plugin, boolean enabled,
				String key) throws RemoteException {
			connections.get(connectionClutch).setPluginAliasEnabled(plugin,enabled,key);
		}

		@Override
		public TimerData getPluginTimer(String plugin,String name) throws RemoteException {
			return connections.get(connectionClutch).getPluginTimer(plugin,name);
		}

		@Override
		public void deleteTimer(String name) throws RemoteException {
			connections.get(connectionClutch).deleteTimer(name);
		}

		@Override
		public void deletePluginTimer(String plugin, String name)
				throws RemoteException {
			connections.get(connectionClutch).deletePluginTimer(plugin,name);
		}

		@Override
		public void updatePluginTimer(String plugin, TimerData old,
				TimerData newtimer) throws RemoteException {
			connections.get(connectionClutch).updatePluginTimer(plugin,old,newtimer);
		}

		@Override
		public void addPluginTimer(String plugin, TimerData newtimer)
				throws RemoteException {
			connections.get(connectionClutch).addPluginTimer(plugin,newtimer);
		}

		@Override
		public SettingsGroup getSettings() throws RemoteException {
			if(connections.size() == 0) { return null; }
			//Log.e("sf","getting settings" + connections.size() + " clutch:" + connectionClutch);
			SettingsGroup sg = connections.get(connectionClutch).getSettings();
			//if(sg == null) { Log.e("fsds","settings are null."); };
			return connections.get(connectionClutch).getSettings();
		}

		@Override
		public SettingsGroup getPluginSettings(String plugin)
				throws RemoteException {
			return connections.get(connectionClutch).getPluginSettings(plugin);
		}

		@Override
		public void updateBooleanSetting(String key, boolean value)
				throws RemoteException {
			connections.get(connectionClutch).updateBooleanSetting(key,value);
		}

		@Override
		public void updatePluginBooleanSetting(String plugin, String key,
				boolean value) throws RemoteException {
			connections.get(connectionClutch).updatePluginBooleanSetting(plugin,key,value);
		}

		@Override
		public void updateIntegerSetting(String key, int value)
				throws RemoteException {
			connections.get(connectionClutch).updateIntegerSetting(key, value);
		}

		@Override
		public void updatePluginIntegerSetting(String plugin, String key,
				int value) throws RemoteException {
			connections.get(connectionClutch).updatePluginIntegerSetting(plugin, key, value);
		}

		@Override
		public void updateFloatSetting(String key, float value)
				throws RemoteException {
			connections.get(connectionClutch).updateFloatSetting(key, value);
		}

		@Override
		public void updatePluginFloatSetting(String plugin, String key,
				float value) throws RemoteException {
			connections.get(connectionClutch).updatePluginFloatSetting(plugin, key, value);
		}

		@Override
		public void updateStringSetting(String key, String value)
				throws RemoteException {
			connections.get(connectionClutch).updateStringSetting(key, value);
		}

		@Override
		public void updatePluginStringSetting(String plugin, String key,
				String value) throws RemoteException {
			connections.get(connectionClutch).updatePluginStringSetting(plugin, key, value);
		}

		@Override
		public void updateWindowBufferMaxValue(String plugin, String window,
				int amount) throws RemoteException {
			connections.get(connectionClutch).updateWindowBufferMaxValue(plugin,window,amount);
		}
		
		@Override
		public void closeConnection(String display) {
			Connection c = connections.get(display);
			if(c != null) {
				c.shutdown();
			
				connections.remove(display);
			}
			//switch to the next active connection.
			//connectionClutch = connections.
			//showNotification();
		}
		
		@Override
		public void windowShowing(boolean show) {
			//Log.e("Log","window showing: " + show);
			mWindowShowing = show;
		}

		@Override
		public void dispatchLuaError(String message) throws RemoteException {
			connections.get(connectionClutch).dispatchLuaError(message);
		}
		
		@Override
		public void addLink(String path) {
			connections.get(connectionClutch).addLink(path);
		}

		@Override
		public void deletePlugin(String plugin) throws RemoteException {
			connections.get(connectionClutch).deletePlugin(plugin);
		}

		@Override
		public void setPluginEnabled(String plugin, boolean enabled)
				throws RemoteException {
			connections.get(connectionClutch).setPluginEnabled(plugin,enabled);
		}

		@Override
		public List getPluginsWithAliases() {
			ArrayList<String> list = new ArrayList<String>();
			Connection c = connections.get(connectionClutch);
			for(Plugin p : c.getPlugins()) {
				if(p.getSettings().getAliases().size() > 0) {
					list.add(p.getName());
				}
			}
			return list;
		}

		@Override
		public List getPluginsWithTimers() throws RemoteException {
			ArrayList<String> list = new ArrayList<String>();
			Connection c = connections.get(connectionClutch);
			for(Plugin p : c.getPlugins()) {
				if(p.getSettings().getTimers().size() > 0) {
					list.add(p.getName());
				}
			}
			return list;
		}

		@Override
		public boolean isLinkLoaded(String link) throws RemoteException {
			// TODO Auto-generated method stub
			boolean retval = connections.get(connectionClutch).isLinkLoaded(link);
			return retval;
		}

		@Override
		public String getPluginPath(String plugin) throws RemoteException {
			String path = connections.get(connectionClutch).getPluginPath(plugin);
			if(path == null) path = "";
			return path;
		}

		@Override
		public void dispatchLuaText(String str) throws RemoteException {
			connections.get(connectionClutch).dispatchLuaText(str);
		}

		@Override
		public void callPluginFunction(String plugin, String function)
				throws RemoteException {
			connections.get(connectionClutch).callPluginFunction(plugin,function);
		}

		@Override
		public boolean isPluginInstalled(String desired) throws RemoteException {
			return connections.get(connectionClutch).isPluginInstalled(desired);
			//return false;
		}

	};

	public void sendRawDataToWindow(byte[] data) {
		//service.sendRawDataToWindow(data);
		int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).rawDataIncoming(data);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		callbacks.finishBroadcast();
		
	}

		public boolean isWindowConnected() {
//			boolean showing = false;
//			int N = callbacks.beginBroadcast();
//			for(int i =0;i<N;i++) {
//				try {
//					showing = callbacks.getBroadcastItem(i).isWindowShowing();
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				if(showing) {
//					break;
//				}
//			}
//			callbacks.finishBroadcast();
			return mWindowShowing;
		}

		public void doClearAllButtons() {
			
			int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).clearAllButtons();
				} catch (RemoteException e) {
				}
			}
			callbacks.finishBroadcast();
			//return null;
		}

		public void doExecuteColorDebug(Integer iarg) {
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).executeColorDebug(iarg);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
			
		}

		public void doDirtyExit() {
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).invokeDirtyExit();
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
		}

		public void doExecuteFullscreen(boolean set) {
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setScreenMode(set);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
		}

		public void doShowKeyboard(String text,boolean dopopup,boolean doadd,boolean doflush,boolean doclear,boolean doclose) {
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).showKeyBoard(text,dopopup,doadd,doflush,doclear,doclose);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
		}

		public void markWindowsDirty() {
			final int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).markWindowsDirty();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			callbacks.finishBroadcast();
		
		}

		public void markSettingsDirty() {
			final int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).markSettingsDirty();
				} catch (RemoteException e) {
					
				}
			}
		}

		public void dispatchKeepLast(Boolean value) {
			final int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try{
					callbacks.getBroadcastItem(i).setKeepLast((boolean)value);
				} catch(RemoteException e) {
					
				}
			}
			
			callbacks.finishBroadcast();
			
		}
		
		private void updateLibs() throws NameNotFoundException, IOException {
			ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			//ai.dataDir;
			String dataDir = ai.dataDir;
			File libs = new File(dataDir + "/lua/lib");
			deleteRecursive(libs);
			File share = new File(dataDir + "/lua/share");
			deleteRecursive(share);
			
			File lualib = new File(dataDir + "/lua/lib/5.1/");
			if(!lualib.exists()) lualib.mkdirs();
			
			File luashare = new File(dataDir + "/lua/share/5.1/");
			if(!luashare.exists()) luashare.mkdirs();
			
			File luares = new File(dataDir + "/lua/share/5.1/res");
			if(!luares.exists()) luares.mkdirs();
			
			File luareshdpi = new File(dataDir + "/lua/share/5.1/res/hdpi");
			if(!luareshdpi.exists()) luareshdpi.mkdirs();
			File luaresmdpi = new File(dataDir + "/lua/share/5.1/res/mdpi");
			if(!luaresmdpi.exists()) luaresmdpi.mkdirs();
			File luaresldpi = new File(dataDir + "/lua/share/5.1/res/ldpi");
			if(!luaresldpi.exists()) luaresldpi.mkdirs();
			
			
			//copy new file.
			AssetManager assetManager = this.getAssets();
			String[] files = null;
			try {
				files = assetManager.list("lib/lua/5.1");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(String filename : files) {
				//Log.e("asset name:","name:"+filename);
				InputStream in = assetManager.open("lib/lua/5.1/"+filename);
				File tmp = new File(lualib,filename);
				if(!tmp.exists()) { tmp.createNewFile(); }
				OutputStream out = new FileOutputStream(tmp);
				copyfile(in,out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
			
			files = assetManager.list("share/lua/5.1");
			for(String filename : files) {
				if(!filename.equals("res")) {
					InputStream in = assetManager.open("share/lua/5.1/" + filename);
					File tmp = new File(luashare,filename);
					if(!tmp.exists()) { tmp.createNewFile(); }
					OutputStream out = new FileOutputStream(tmp);
					copyfile(in,out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				}
			}
			
			
			files = assetManager.list("share/lua/5.1/res/hdpi");
			for(String filename : files) {
				InputStream in = assetManager.open("share/lua/5.1/res/hdpi/" + filename);
				File tmp = new File(luareshdpi,filename);
				if(!tmp.exists()) { tmp.createNewFile(); }
				OutputStream out = new FileOutputStream(tmp);
				copyfile(in,out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
			
			files = assetManager.list("share/lua/5.1/res/mdpi");
			for(String filename : files) {
				InputStream in = assetManager.open("share/lua/5.1/res/mdpi/" + filename);
				File tmp = new File(luaresmdpi,filename);
				if(!tmp.exists()) { tmp.createNewFile(); }
				OutputStream out = new FileOutputStream(tmp);
				copyfile(in,out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
			
			files = assetManager.list("share/lua/5.1/res/ldpi");
			for(String filename : files) {
				InputStream in = assetManager.open("share/lua/5.1/res/ldpi/" + filename);
				File tmp = new File(luaresldpi,filename);
				if(!tmp.exists()) { tmp.createNewFile(); }
				OutputStream out = new FileOutputStream(tmp);
				copyfile(in,out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		}
		
		private void deleteRecursive(File file) {
			if(file.isDirectory()) {
				for(File child : file.listFiles()) {
					deleteRecursive(child);
				}
			} else {
				file.delete();
			}
		}
		
		private void copyfile(InputStream in,OutputStream out) throws IOException {
			byte[] buffer = new byte[1024];
			int read;
			while((read = in.read(buffer)) != -1) {
				out.write(buffer,0,read);
			}
		}

		public void doExecuteSetOrientation(Integer value) {
			int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setOrientation(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
		}

		public void doExecuteKeepScreenOn(Boolean value) {
			int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setKeepScreenOn(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
		}

		public void doExecuteFullscreenEditor(Boolean value) {
			int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setUseFullscreenEditor(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
		}

		public void doExecuteUseSuggestions(Boolean value) {
			int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setUseSuggestions(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
		}

		public void doExecuteCompatibilityMode(Boolean value) {
			int N = callbacks.beginBroadcast();
			for(int i=0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setCompatibilityMode(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
		}


}
