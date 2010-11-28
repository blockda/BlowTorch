package com.happygoatstudios.bt.service;




import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
//import android.util.Log;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.IStellarServiceCallback;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.HyperSAXParser;
import com.happygoatstudios.bt.settings.HyperSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerExtraTask;
import com.happygoatstudios.bt.timer.TimerProgress;
import com.happygoatstudios.bt.trigger.TriggerData;


public class StellarService extends Service {

	public static final String ALIAS_PREFS = "ALIAS_SETTINGS";
	TreeMap<String, String> aliases = new TreeMap<String, String>();
	RemoteCallbackList<IStellarServiceCallback> callbacks = new RemoteCallbackList<IStellarServiceCallback>();
	
	HyperSettings the_settings = new HyperSettings();
	
	NotificationManager mNM;
	
	OutputStream output_writer = null;
	
	OutputWriter outputter = null;
	
	Processor the_processor = null;
	
	Object sendlock = new Object();
	
	protected int bindCount = 0;
	
	InetAddress the_addr = null;
	String host;
	int port;
	String display;
	final int BAD_PORT = 999999;
	final String BAD_HOST = "NOTSETYET";
	Socket the_socket = null;
	
	DataPumper pump = null;
	
	Handler myhandler = null;
	
	public int trigger_count = 5555;
	
	final static public int MESSAGE_PROCESS = 102;
	final static public int MESSAGE_INIT = 100;
	final static public int MESSAGE_END = 101;
	final static public int MESSAGE_SETDATA = 103;
	final static public int MESSAGE_STARTCOMPRESS = 104;
	final static public int MESSAGE_ENDCOMPRESS = 105;
	final static public int MESSAGE_SENDDATA = 106;
	final static public int MESSAGE_REQUESTBUFFER = 107;
	final static public int MESSAGE_CHECKIFALIVE = 109;

	protected static final int MESSAGE_SAVEBUFFER = 108;
	protected static final int MESSAGE_SENDOPTIONDATA = 110;
	private static final int MESSAGE_DOFINALDISPATCH = 121;
	public static final int MESSAGE_COMPRESSIONREQUESTED = 131;
	private static final int MESSAGE_THROTTLEEVENT = 197;
	protected static final int MESSAGE_HANDLEWIFI = 496;
	protected static final int MESSAGE_SAVEXML = 497;
	public static final int MESSAGE_TIMERFIRED = 498;
	protected static final int MESSAGE_TIMERSTART = 499;
	protected static final int MESSAGE_TIMERPAUSE = 500;
	protected static final int MESSAGE_TIMERRESET = 501;
	public static final int MESSAGE_TIMERINFO = 502;
	public static final int MESSAGE_BELLINC = 503;
	
	public boolean sending = false;
	
	StringBuffer the_buffer = new StringBuffer();
	String settingslocation = "test_settings2.xml";
	
	private boolean compressionStarting = false;
	
	//need some goodies to track running timers.
	Timer the_timer = new Timer("BLOWTORCH_TIMER",true);
	HashMap<String,TimerExtraTask> timerTasks = new HashMap<String,TimerExtraTask>();
	
	public void onLowMemory() {
		//Log.e("SERVICE","The service has been requested to shore up memory usage, potentially going to be killed.");
	}
	
	public void onCreate() {
		//called when we are created from a startService or bindService call with the IBaardTERMService interface intent.
		//Log.e("SERV","BAARDTERMSERVICE STARTING!");
		
		//set up the crash reporter
		//Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this.getApplicationContext()));
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		host = BAD_HOST;
		port = BAD_PORT;
		
		//load special commands.
		ColorDebugCommand colordebug = new ColorDebugCommand();
		//BrokenColor brokencolor = new BrokenColor();
		DirtyExitCommand dirtyexit = new DirtyExitCommand();
		TimerCommand timercmd = new TimerCommand();
		//EncCommand enccmd = new EncCommand();
		BellCommand bellcmd = new BellCommand();
		FullScreenCommand fscmd = new FullScreenCommand();
		KeyBoardCommand kbcmd = new KeyBoardCommand();
		specialcommands.put(colordebug.commandName, colordebug);
		//specialcommands.put(brokencolor.commandName,brokencolor);
		specialcommands.put(dirtyexit.commandName, dirtyexit);
		specialcommands.put(timercmd.commandName, timercmd);
		specialcommands.put(bellcmd.commandName, bellcmd);
		specialcommands.put(fscmd.commandName, fscmd);
		specialcommands.put(kbcmd.commandName, kbcmd);
		specialcommands.put("kb", kbcmd);
		//specialcommands.put(enccmd.commandName, enccmd);
		
		
		//Looper.prepare();
		SharedPreferences prefs = this.getSharedPreferences("SERVICE_INFO", 0);
		settingslocation = prefs.getString("SETTINGS_PATH", "");
		if(settingslocation.equals("")) {
			//Log.e("SERVICE","LAUNCHER FAILED TO PROVIDE SETTINGS PATH");
			return;
		}
		
		Pattern invalidchars = Pattern.compile("\\W"); 
		Matcher replacebadchars = invalidchars.matcher(settingslocation);
		String prefsname = replacebadchars.replaceAll("");
		prefsname = prefsname.replaceAll("/", "");
		//Log.e("SERVICE","Attempting to load "+ prefsname);
		settingslocation = prefsname + ".xml";
		loadXmlSettings(prefsname +".xml");
		
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_BELLINC:
					//bell recieved.
					if(the_settings.isVibrateOnBell()) {
						doVibrateBell();
					}
					if(the_settings.isNotifyOnBell()) {
						doNotifyBell();
					}
					if(the_settings.isDisplayOnBell()) {
						doDisplayBell();
					}
					break;
				case MESSAGE_TIMERINFO:
					if(timerTasks.containsKey((String)msg.obj)) {
						TimerExtraTask t = timerTasks.get((String)msg.obj);
						//calculate time, ugh, can't we do this somewhere else.
						long then = t.getStarttime();
						long now = System.currentTimeMillis();
						long secleft = ((the_settings.getTimers().get((String)msg.obj).getSeconds()*1000)-(now - then))/1000;
						DispatchToast("Timer " + (String)msg.obj + ": " + Long.toString(secleft) + " seconds left.",false);
					} else {
						DispatchToast("Timer " + (String)msg.obj + " not running.",false);
					}
					break;
				case MESSAGE_TIMERRESET:
					if(timerTasks.containsKey((String)msg.obj)) {
						TimerExtraTask t = timerTasks.get((String)msg.obj);
						t.cancel();
						the_timer.purge();
						//reset the timer
						
						timerTasks.remove((String)msg.obj);
						TimerData timer = the_settings.getTimers().get((String)msg.obj);
						timer.reset();
						//schedule for playing.
						
						TimerExtraTask newt = new TimerExtraTask(Integer.parseInt((String)msg.obj),System.currentTimeMillis(),myhandler);
						
						timer.setTTF(timer.getSeconds()*1000);
						newt.setStarttime(System.currentTimeMillis());
						timer.setPauseLocation(0l);
						if(timer.isRepeat()) {
							the_timer.scheduleAtFixedRate(newt, timer.getTTF(), timer.getSeconds()*1000);
						} else {
							the_timer.schedule(newt, timer.getTTF());
						}
						timer.setPlaying(true);
						timerTasks.put(timer.getOrdinal().toString(), newt);
						
						
						
					} else {
						TimerData timer = the_settings.getTimers().get((String)msg.obj);
						timer.reset();
						timer.setPlaying(false);
					}
					
					if(msg.arg2 == 50) {
						//send message.
						DispatchToast("Timer " + (String)msg.obj + " reset.",false);
					}
					break;
				case MESSAGE_TIMERPAUSE:
					
					//check to see if the timer exists in the timertasks.
					if(timerTasks.containsKey((String)msg.obj)) {
						TimerExtraTask t = timerTasks.get((String)msg.obj);
						long current = System.currentTimeMillis();
						long start = t.getStarttime();
						timerTasks.remove((String)msg.obj);
						t.cancel();
						the_timer.purge();
						//update the timer data to reflect that it is not running
						//however does need to have the ttl set, so if resumed,
						//will pick up where it left off.
						TimerData timer = the_settings.getTimers().get((String)msg.obj);
						timer.setTTF((timer.getSeconds()*1000)-(current-start));
						timer.setPauseLocation(current-start);
						//Log.e("SERVICE","PAUSING TIMER " + (String)msg.obj + " WITH " + timer.getTTF()/1000);
						timer.setPlaying(false);
					}
					
					if(msg.arg2 == 50) {
						//send message.
						DispatchToast("Timer " + (String)msg.obj + " paused.",false);
					}
					break;
				case MESSAGE_TIMERSTART:
					
					DoTimerStart((String)msg.obj,msg.arg2);
					
					
					break;
				case MESSAGE_TIMERFIRED:
					//Log.e("SERVICE","TIMER " + msg.arg1 + " FIRED!");
					String ordinal = Integer.toString(msg.arg1);
					DoTimerResponders(ordinal);
					TimerData td = the_settings.getTimers().get(ordinal);
					if(!td.isRepeat()) {
						//need to make sure the timerTask is cancelled
						TimerExtraTask tt = timerTasks.remove(ordinal);
						tt.cancel();
						the_timer.purge();
						td.reset();
						td.setPlaying(false);
					}
					
					break;
				case MESSAGE_SAVEXML:
					saveXmlSettings(settingslocation);
					break;
				case MESSAGE_HANDLEWIFI:
					Boolean state = (Boolean) msg.obj;
					if(state) {
						EnableWifiKeepAlive();
					} else {
						DisableWifiKeepAlive();
					}
					break;
				case MESSAGE_THROTTLEEVENT:
					doThrottleBackgroundImpl();
					break;
				case MESSAGE_COMPRESSIONREQUESTED:
					compressionStarting = true;
					break;
				case MESSAGE_INIT:
					//Log.e("BTSERVICE","INTIIALIZING");
					
					try {
						doStartup();
					} catch (UnknownHostException e) {
						throw new RuntimeException(e);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} catch (RemoteException e) {
						
						e.printStackTrace();
					}
					
					break;
				case MESSAGE_END:
					//Log.e("BTSERVICE","ENDING");
					pump.stop();
					doShutdown();
					break;
				case MESSAGE_PROCESS:
					//Log.e("BTSERVICE","PROCESSING");
					//byte[] data = msg.getData().getByteArray("THEBYTES");
					
					//need to throttle this somehow to avoid sending messages too fast.
					try {
						dispatch((byte[])msg.obj);
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
					break;
				case MESSAGE_DOFINALDISPATCH:
					//Log.e("BTSERVICE","FINAL DISPATCH");
					if(compressionStarting) {
						this.sendMessageDelayed(Message.obtain(msg), 10); //re-send this message for processing until compress is turned on.
					} else {
						dispatchFinish((String)msg.obj);
					}
					break;
				case MESSAGE_SETDATA:
					//Log.e("BTSERVICE","SETTING DISPLAY DATA!");
					host = msg.getData().getString("HOST");
					port = msg.getData().getInt("PORT");
					display = msg.getData().getString("DISPLAY");					
					showNotification();
					break;
				case MESSAGE_STARTCOMPRESS:
					//Log.e("BTSERVICE","STARTING COMPRESSION!");
					compressionStarting = false;
					pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_COMPRESS);
					break;
				case MESSAGE_ENDCOMPRESS:
					break;
				case MESSAGE_SENDOPTIONDATA:
					//Log.e("BTSERVICE","SENDING OPTION DATA");
					byte[] obytes = (byte[])(msg.obj);
					
					try {
						output_writer.write(obytes);
						output_writer.flush();
					} catch (IOException e2) {
						throw new RuntimeException(e2);
					}
					
					break;
				case MESSAGE_SENDDATA:
					//Log.e("BTSERVICE","SENDING NORMAL DATA");
					//byte[] bytes = msg.getData().getByteArray("THEDATA");
					byte[] bytes = (byte[]) msg.obj;
					
					//dispatch this for command processing
					String retval = null;
					try {
						retval = ProcessCommands(new String(bytes,the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					if(retval == null || retval.equals("")) {
						//command was intercepted. do nothing for now and return
						//Log.e("SERVICE","CONSUMED ALL COMMANDS");
						return;
					} else {
						//not a command data.
						try {
							//Log.e("SERVICE","PROCESSED COMMANDS AND WAS LEFT WITH:" + retval);
							if(retval.equals("")) { return; }
							bytes = retval.getBytes(the_settings.getEncoding());
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
					}
					//do search and replace with aliases.
					
					if(joined_alias.length() > 0) {

						Pattern to_replace = Pattern.compile(joined_alias.toString());
						
						Matcher replacer = null;
						try {
							replacer = to_replace.matcher(new String(bytes,the_settings.getEncoding()));
						} catch (UnsupportedEncodingException e1) {
							throw new RuntimeException(e1);
						}
						
						StringBuffer replaced = new StringBuffer();
						
						boolean found = false;
						while(replacer.find()) {
							//String matched = replacer.group(0);
							found = true;
							AliasData replace_with = the_settings.getAliases().get(replacer.group(0));
							replacer.appendReplacement(replaced, replace_with.getPost());
						}
						
						replacer.appendTail(replaced);
						
						StringBuffer buffertemp = new StringBuffer();
						if(found) { //if we replaced a match, we need to continue the find/match process until none are found.
							boolean recursivefound = false;
							do {
								recursivefound = false;
								Matcher recursivematch = to_replace.matcher(replaced.toString());
								while(recursivematch.find()) {
									recursivefound = true;
									AliasData replace_with = the_settings.getAliases().get(recursivematch.group(0));
									recursivematch.appendReplacement(buffertemp, replace_with.getPost());
								}
								if(recursivefound) {
									recursivematch.appendTail(buffertemp);
									replaced.setLength(0);
									replaced.append(buffertemp);
								}
							} while(recursivefound == true);
						}
						//so replacer should contain the transformed string now.
						//pull the bytes back out.
						try {
							bytes = replaced.toString().getBytes(the_settings.getEncoding());
							//Log.e("SERVICE","UNTRNFORMED:" + new String(bytes));
							//Log.e("SERVICE","TRANSFORMED: " + replaced.toString());
						} catch (UnsupportedEncodingException e1) {
							throw new RuntimeException(e1);
						}
						
						replaced.setLength(0);
					}
					
					//strip semi
					Character cr = new Character((char)13);
					Character lf = new Character((char)10);
					String crlf = cr.toString() + lf.toString();
					byte[] preserve = bytes;
					String tostripsemi = null;
					try {
						tostripsemi = new String(bytes,the_settings.getEncoding());
					} catch (UnsupportedEncodingException e1) {
						throw new RuntimeException(e1);
					}
					
					String nosemidata = null;
					synchronized(the_settings) {
						if(the_settings.isSemiIsNewLine()) {
							nosemidata = tostripsemi.replace(";", crlf);
						} else {
							nosemidata = tostripsemi;
						}
					}
					//nosemidata = nosemidata.concat(crlf);
					
					try {
						output_writer.write(nosemidata.getBytes(the_settings.getEncoding()));

						output_writer.flush();
						
						//send the transformed data back to the window
						try {
							if(the_settings.isLocalEcho()) {
								doDispatchNoProcess(preserve);
							}
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					break;
				case MESSAGE_REQUESTBUFFER:
					//Log.e("BTSERVICE","SENDING REQUESTED BUFFER");
					try {
						sendBuffer();
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
					break;
				case MESSAGE_SAVEBUFFER:
					//Log.e("BTSERVICE","SAVING TARGET BUFFER");
					the_buffer = new StringBuffer(msg.getData().getString("BUFFER") + the_buffer);
					break;
				default:
					break;	
				}
				
			}
		};
		
		//populate the timer_actions hash so we can parse arguments.
		timer_actions = new ArrayList<String>();
		timer_actions.add("play");
		timer_actions.add("pause");
		timer_actions.add("reset");
		timer_actions.add("info");
		
		//start any timers that might be "playing"
		for(TimerData t : the_settings.getTimers().values()) {
			if(t.isPlaying()) {
				DoTimerStart(t.getOrdinal().toString(),0);
			}
		}
	}
	
	public void onDestroy() {
		//Log.e("SERV","ON DESTROY CALLED!");
		saveXmlSettings(settingslocation);
		//saveAliases();
		doShutdown();
	}
	
	public void saveXmlSettings(String filename) {
		try {
			FileOutputStream fos = this.openFileOutput(filename,Context.MODE_PRIVATE);
			fos.write(HyperSettings.writeXml2(the_settings).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void loadXmlSettings(String filename) {
		
		try {
			FileInputStream fos = this.openFileInput(filename);
			fos.close();
			
			//if the file exists, we will get here, if not, it will go to file not found.
			HyperSAXParser parser = new HyperSAXParser(filename,this);
			the_settings = parser.load();
			buildAliases();
			buildTriggerData();
			
			//temporarily output the timers.
			for(TimerData timer : the_settings.getTimers().values()) {
				timer.reset();
				//timer.setPlaying(false);
				if(timer.isPlaying()) {
					//
					//myhandler.sendMessageDelayed(myhandler.obtainMessage(MESSAGE_TIMERSTART,timer.getOrdinal().toString()),1000); //start this timer in 1 second.
				}
				//Log.e("SERVICE","LOADED SETTINGS, TIMER" + timer.getOrdinal() + ", DURATION " + timer.getSeconds());
			}
			
		} catch (FileNotFoundException e) {
			//if the file does not exist, then we need to load the default settings
			the_settings.getButtonSets().put("default", new Vector<SlickButtonData>());
			ColorSetSettings def_colorset = new ColorSetSettings();
			def_colorset.toDefautls();
			the_settings.getSetSettings().put("default", def_colorset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void loadAliases() {
		SharedPreferences pref = this.getSharedPreferences(ALIAS_PREFS, 0);
		if(display == null || display.equals("")) {
			return;
			//no display set, do not try and load aliases.
		}
		
		Pattern whitespacestrip = Pattern.compile("\\s");
		Matcher stripper = whitespacestrip.matcher(display);
		
		String usethis = stripper.replaceAll("");
		int count = pref.getInt("ALIASCOUNT" + usethis, 0);
		//Log.e("SERVICE","Loading: " + count + " aliases.");
		if(count > 0) {
			for(int i=0;i<count;i++) {
				String alias = pref.getString(usethis + "ALIAS" + i, "");
				if(!alias.equals("")) {
					//Log.e("SERVICE","Attempting to load: " + alias);
					String[] parts = alias.split("\\Q[||]\\E");
					if(parts.length == 2) {
						//only do well formatted alias pairs.
						aliases.put(parts[0], parts[1]);
					}
				}
			}
		}
	}
	
	public void saveAliases() {
		SharedPreferences pref = this.getSharedPreferences(ALIAS_PREFS, 0);
		
		if(display == null || display.equals("")) {
			return;
			//no display set, do not try and load aliases.
		}
		
		Pattern whitespacestrip = Pattern.compile("\\s");
		Matcher stripper = whitespacestrip.matcher(display);
		
		String usethis = stripper.replaceAll("");
		
		Editor ed = pref.edit();
		
		Object[] a = aliases.keySet().toArray();
		
		if(a.length > 0) {
			for(int i=0;i<a.length;i++) {
				ed.putString(usethis + "ALIAS" + i, (String)a[i] + "[||]" + aliases.get(a[i]));
			}
			
			ed.putInt("ALIASCOUNT" + usethis, a.length);
		}
		
		ed.commit();
		
	}
	
	private void doVibrateBell() {
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
	}
	
	private int bellcount = 3344;
	private void doNotifyBell() { 
		Notification note = new Notification(com.happygoatstudios.bt.R.drawable.blowtorch_notification2,"Alert!",System.currentTimeMillis());
		//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "BlowTorch - Alert!";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = "The server is notifying you with the bell character, 0x07.";
		Intent notificationIntent = new Intent(this, com.happygoatstudios.bt.window.MainWindow.class);
		notificationIntent.putExtra("DISPLAY", display);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = com.happygoatstudios.bt.R.drawable.blowtorch_notification2;
		note.flags = Notification.DEFAULT_ALL;
		
		//startForeground to avoid being killed off.
		//this.startForeground(5545, note);
		
		mNM.notify(bellcount,note);
	}
	
	private void doDisplayBell() {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).doVisualBell();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	

	
	private void doThrottleBackgroundImpl() {
		if(pump == null) return;
		if(pump.getHandler() == null) return;
		if(the_settings == null) return;
		
		synchronized(the_settings) {
			if(the_settings.isThrottleBackground()) {
				if(!hasListener && pump != null) {
					if(pump.getHandler() != null) 
						pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_THROTTLE);
				} else {
					if(pump.getHandler() != null)
						pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_NOTHROTTLE);
				}
			}
		}
		
	}
	
	Object binderCookie = new Object();
	Boolean hasListener = false;
	private final IStellarService.Stub mBinder = new IStellarService.Stub() {
		public void registerCallback(IStellarServiceCallback m) throws RemoteException {
			if(m != null && !hasListener) {
				if(callbacks.register(m,binderCookie)) {
					bindCount++;
					//Log.e("SERV","Registering callback, " + bindCount + " now.");
					hasListener = true;
				} else {
					//Log.e("SERV","Callback not registerd because it is already in the list, " + bindCount + " now.");
				}
			} else {
				callbacks.kill();
				callbacks = new RemoteCallbackList<IStellarServiceCallback>();
				if(m!= null) {
					callbacks.register(m);
					hasListener = true;
				}
			}
			sendInitOk();
			doThrottleBackground();
		}
		
		private void doThrottleBackground() {
			myhandler.sendEmptyMessage(MESSAGE_THROTTLEEVENT);
		}
		
		public void unregisterCallback(IStellarServiceCallback m)
		throws RemoteException {
			if(m != null) {
				if(callbacks.unregister(m)) {
					bindCount--;
					//Log.e("SERV","Unregistering callback, " + bindCount + " left.");
					hasListener = false;
				}
			}
			doThrottleBackground();
		}
		
		




		public int getPid() throws RemoteException {
			return Process.myPid();
		}
		
		
		public void endXfer() {
			
		}
		
		public void setNotificationText(CharSequence seq) throws RemoteException {
			//do some stuff
			
		}

		public void initXfer() throws RemoteException {
			//
			myhandler.sendEmptyMessage(StellarService.MESSAGE_INIT);
		}

		public void sendData(byte[] seq) throws RemoteException {
			
			//ENTER GIANT SYNCHRONIZATION STEP
			if(myhandler.hasMessages(StellarService.MESSAGE_SENDDATA)) {
				synchronized(sendlock) {
					while(myhandler.hasMessages(StellarService.MESSAGE_SENDDATA)) {
						try {
							sendlock.wait();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			Message msg = myhandler.obtainMessage(StellarService.MESSAGE_SENDDATA,seq);
			myhandler.sendMessage(msg);
		}
		
		public void setConnectionData(String ihost,int iport,String display) {
			//host = ihost;
			//port = iport;
			Message msg = myhandler.obtainMessage(StellarService.MESSAGE_SETDATA);
			Bundle b = new Bundle();
			b.putString("HOST",ihost);
			b.putInt("PORT",iport);
			b.putString("DISPLAY", display);
			
			msg.setData(b);
			
			myhandler.sendMessage(msg);
			
		}
		
		public void beginCompression() {
			myhandler.sendEmptyMessage(StellarService.MESSAGE_STARTCOMPRESS);
			
		}
		
		public void stopCompression() {
			
		}

		public void requestBuffer() throws RemoteException {
			myhandler.sendEmptyMessage(StellarService.MESSAGE_REQUESTBUFFER);
			
		}

		public void saveBuffer(String buffer) throws RemoteException {
			Message msg = myhandler.obtainMessage(StellarService.MESSAGE_SAVEBUFFER);
			Bundle b = msg.getData();
			b.putString("BUFFER",buffer);
			msg.setData(b);
			myhandler.sendMessage(msg);
			
		}


		public void addAlias(AliasData d) throws RemoteException {
			
			the_settings.getAliases().put(d.getPre(), d);
			buildAliases();
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
		}


		public Map<String, AliasData> getAliases() throws RemoteException {
			
			return the_settings.getAliases();
		}


		@SuppressWarnings("unchecked")
		public void setAliases(Map map) throws RemoteException {
			
			the_settings.getAliases().clear();
			the_settings.setAliases(new HashMap<String,AliasData>(map));
			buildAliases();
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
		}


		public void addButton(String targetset, SlickButtonData newButton)
				throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.getButtonSets().get(targetset).add(newButton);
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			}
			
	
		}



		public void removeButton(String targetset, SlickButtonData buttonToNuke)
				throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.getButtonSets().get(targetset).remove(buttonToNuke);
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			}	
		}


		public void setFontName(String name) throws RemoteException {
		
			synchronized(the_settings) {
				the_settings.setFontName(name);
			}
		}


		public void setFontPath(String path) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.setFontPath(path);
			}
		}


		public void setFontSize(int size) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.setLineSize(size);
			}
		}


		public void setFontSpaceExtra(int size) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.setLineSpaceExtra(size);
			}
		}


		/*public void setSelectedButtonSet(String setname) throws RemoteException {
			
			
		}*/


		public void setSemiOption(boolean boolsAreNewline)
				throws RemoteException {
			synchronized(the_settings) {
				the_settings.setSemiIsNewLine(boolsAreNewline);
			}
		}


		public List<SlickButtonData> getButtonSet(String setname)
				throws RemoteException {
			synchronized(the_settings) {
				String orig_set = the_settings.getLastSelected();
				the_settings.setLastSelected(setname);
				Vector<SlickButtonData> tmp = the_settings.getButtonSets().get(setname);
				if(tmp == null) {
					//Log.e("SERVICE","WINDOW REQUESTED BUTTONSET: " + setname + " but got null");
					the_settings.setLastSelected(orig_set);
				} else {
					//Log.e("SERVICE","WINDOW REQUESTED BUTTONSET: " + setname + " and am returning real data");
				}
				return tmp;
			}
		}


		public List<String> getButtonSetNames() throws RemoteException {
			
			
			synchronized(the_settings) {
				ArrayList<String> keys = new ArrayList<String>();
			
				for(String key : the_settings.getButtonSets().keySet()) {
					keys.add(key);
				}
			
			return keys;
			}
		}


		public void modifyButton(String targetset, SlickButtonData orig,
				SlickButtonData mod) throws RemoteException {
			
			synchronized(the_settings) {
				
				int loc = the_settings.getButtonSets().get(targetset).indexOf(orig);
				
				//Log.e("SERVICE","ATTEMPTING TO MODIFY BUTTON: " + orig.toString() + " at location " + loc);
				the_settings.getButtonSets().get(targetset).remove(loc); //remove original
				the_settings.getButtonSets().get(targetset).add(loc,mod); //insert mod in its place
				//the_settings.getButtonSets().get(targetset).
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			}
			
			//Log.e("SERVICE","MODIFYING BUTTON " + orig.toString() + " FROM BUTTONSET: " + targetset + " with " + mod.toString() + ", now contains " + the_settings.getButtonSets().get(targetset).size() + " buttons.");
			//Vector<SlickButtonData> buttons = the_settings.getButtonSets().get(targetset);
			//for(SlickButtonData data : buttons) {
			//	Log.e("SERVICE",data.toString());
			//}
		}


		public void addNewButtonSet(String name) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.setLastSelected(name);
				the_settings.getButtonSets().put(name, new Vector<SlickButtonData>());
				ColorSetSettings def_colorset = new ColorSetSettings();
				def_colorset.toDefautls();
				the_settings.getSetSettings().put(name, def_colorset);
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			}
			
		}


		public List<String> getButtonSets() throws RemoteException {
			
			synchronized(the_settings) {
				Set<String> keys = the_settings.getAliases().keySet();
				return new ArrayList<String>(keys);
			}
		}


		public int clearButtonSet(String name) throws RemoteException {
			
			synchronized(the_settings) {
				int count = the_settings.getButtonSets().get(name).size();
				the_settings.getButtonSets().get(name).removeAllElements();
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
				return count;
			}
			
		}


		public int deleteButtonSet(String name) throws RemoteException {
			
			synchronized(the_settings) {
				int count = the_settings.getButtonSets().get(name).size();
				if(name.equals("default")) {
					//cannot delete default button set, only clear it
					the_settings.getButtonSets().get(name).removeAllElements();
					myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
					return count;
				} else {
					
					the_settings.getButtonSets().remove(name);
					myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
					return count;
				}
				
			}
		}


		@SuppressWarnings("unchecked")
		public Map getButtonSetListInfo() throws RemoteException {
			
			HashMap<String,Integer> tmp = new HashMap<String,Integer>();
			
			synchronized(the_settings) {
				for(String key : the_settings.getButtonSets().keySet()) {
					tmp.put(key, the_settings.getButtonSets().get(key).size());
				}
			}
			
			return tmp;
		}
		
		public String getLastSelectedSet() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getLastSelected();
			}
		}


		public void setMaxLines(int keepcount) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.setMaxLines(keepcount);
			}
			
		}
		
		public int getMaxLines() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getMaxLines();
			}
		}


		public int getFontSize() throws RemoteException {
			
			synchronized(the_settings) {
				
				return the_settings.getLineSize();
			}
		}


		public int getFontSpaceExtra() throws RemoteException {
			
			synchronized(the_settings) {
				return the_settings.getLineSpaceExtra();
			}
		}


		public String getFontName() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getFontName();
			}
		}


		public void ExportSettingsToPath(String path) throws RemoteException {
			synchronized(the_settings) {
				//FileOutputStream tmp = null;
				
				try {
				//tmp = BaardTERMService.this.openFileOutput(path, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
				//BaardTERMService.this.openF
				File root = Environment.getExternalStorageDirectory();
				String state = Environment.getExternalStorageState();
				if(Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					boolean added = false;
					String updated = path;
					Pattern xmlend = Pattern.compile("^.+\\.[Xx][Mm][Ll]$");
					Matcher xmlmatch = xmlend.matcher(updated);
					if(!xmlmatch.matches()) {
						added = true;
						updated = path + ".xml";
					}
					File file = new File(root,updated);
					file.createNewFile();
					//Log.e("SERVICE","ATTEMPTING TO WRITE TO FILE: " + file.getPath());
					FileWriter writer = new FileWriter(file);
					BufferedWriter tmp = new BufferedWriter(writer);
					tmp.write(HyperSettings.writeXml2(the_settings));
					tmp.close();
					
					String message = "Saved: " + updated;
					if(added) {
						message += "\nAppended .xml extension.";
					}
					
					DispatchToast(message,true);
					//Toast msg = Toast.makeText(StellarService.this.getApplicationContext(), message, Toast.LENGTH_SHORT);
					//msg.show();
				} else {
					//Log.e("SERVICE","COULD NOT WRITE SETTINGS FILE!");
					//Toast msg = Toast.makeText(StellarService.this.getApplicationContext(), "SD Card not available. File not written.", Toast.LENGTH_SHORT);
					//msg.show();
					DispatchToast("SD Card not available. File not written.",true);
				}
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		}
		
		public void LoadSettingsFromPath(String path) throws RemoteException {
			synchronized(the_settings) {
				HyperSAXParser loader = new HyperSAXParser(path,StellarService.this);
				try {
					the_settings = loader.load();
				} catch(RuntimeException e) {
					//extract the message.
					String message = e.getMessage();
					//send the message.
					dispatchXMLError(message);
				} finally {
					buildAliases();
					buildTriggerData();
				}
			}

			sendInitOk();
		}


		public void resetSettings() throws RemoteException {
			synchronized(the_settings) {
				the_settings = new HyperSettings();
				the_settings.getButtonSets().put("default", new Vector<SlickButtonData>());
			}
			sendInitOk();
		}


		public ColorSetSettings getCurrentColorSetDefaults()
				throws RemoteException {
			
			synchronized(the_settings) {
				return the_settings.getSetSettings().get(the_settings.getLastSelected());
			}
		}


		public ColorSetSettings getColorSetDefaultsForSet(String theSet)
				throws RemoteException {
			
			synchronized(the_settings) {
				return the_settings.getSetSettings().get(theSet);
			}
		}


		public void setColorSetDefaultsForSet(String theSet,ColorSetSettings input)
				throws RemoteException {
			
			synchronized(the_settings) {
				//back up old set to use for the button update.
				ColorSetSettings oldsettings = the_settings.getSetSettings().get(theSet);
				
				//set the new settings
				the_settings.getSetSettings().remove(theSet);
				the_settings.getSetSettings().put(theSet, input);
				
				//update all the button in the new set with the new data, if they have the same data as the old settings.
				Vector<SlickButtonData> edited_set = the_settings.getButtonSets().get(theSet);
				for(SlickButtonData button : edited_set) {
					button.setFromSetSettings(input, oldsettings);
				}
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
				//need to go through all the button in the set and update the values.
			}
			
		}


		public void setProcessPeriod(boolean value) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.setProcessPeriod(value);
			}
			
		}


		@SuppressWarnings("unchecked")
		public Map getTriggerData() throws RemoteException {
			
			synchronized(the_settings) {
				return the_settings.getTriggers();
			}
		}


		public void deleteTrigger(String which) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.getTriggers().remove(which);
				
			}
			buildTriggerData();
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			
		}


		public void newTrigger(TriggerData data) throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.getTriggers().put(data.getPattern(), data);
				
			}
			buildTriggerData();
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
		}


		public void updateTrigger(TriggerData from, TriggerData to)
				throws RemoteException {
			
			synchronized(the_settings) {
				the_settings.getTriggers().remove(from.getPattern());
				the_settings.getTriggers().put(to.getPattern(), to);
				//for(TriggerResponder responder : to.getResponders()) {
				//	Log.e("SERVICE","MODIFIED TRIGGER, RESPONDER NOW: "+ responder.getFireType().getString());
				//}
			}
			buildTriggerData();
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
		}


		public TriggerData getTrigger(String pattern) throws RemoteException {
			
			synchronized(the_settings) {
				
				return the_settings.getTriggers().get(pattern);
			}
		}


		public boolean getUseExtractUI() throws RemoteException {
			
			synchronized(the_settings) {
				return the_settings.isUseExtractUI();
				
			}
		}


		public void setThrottleBackground(boolean use) throws RemoteException {
			synchronized(the_settings) {
				
				the_settings.setThrottleBackground(use);
			}
		}


		public void setUseExtractUI(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setUseExtractUI(use);
				
			}
		}

		public boolean isProcessPeriod() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isProcessPeriod();
				
			}
		}

		public boolean isSemiNewline() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isSemiIsNewLine();
			}
		}

		public boolean isThrottleBackground() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isThrottleBackground();
			}
		}

		public boolean isKeepWifiActive() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isKeepWifiActive();
			}
		}

		public void setKeepWifiActive(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setKeepWifiActive(use);
				Message keepalive = myhandler.obtainMessage(MESSAGE_HANDLEWIFI);
				keepalive.obj = new Boolean(use);
				myhandler.sendMessage(keepalive);
			}
		}

		public boolean isAttemptSuggestions() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isAttemptSuggestions();
			}
		}

		public void setAttemptSuggestions(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setAttemptSuggestions(use);
			}
		}

		public boolean isKeepLast() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isKeepLast();
			}
		}

		public void setKeepLast(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setKeepLast(use);
			}
		}

		public boolean isBackSpaceBugFix() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isBackspaceBugFix();
			}
		}

		public void setBackSpaceBugFix(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setBackspaceBugFix(use);
			}
		}

		public boolean isAutoLaunchEditor() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isAutoLaunchButtonEdtior();
			}
		}

		public boolean isDisableColor() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isDisableColor();
			}
		}

		public void setAutoLaunchEditor(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setAutoLaunchButtonEdtior(use);
			}
		}

		public void setDisableColor(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setDisableColor(use);
			}
		}

		public String HapticFeedbackMode() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getHapticFeedbackMode();
			}
		}

		public void setHapticFeedbackMode(String use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setHapticFeedbackMode(use);
			}
		}

		public String getAvailableSet() throws RemoteException {
			synchronized(the_settings) {
				//String result = "";
				Set<String> keyset = the_settings.getButtonSets().keySet();
				if(keyset.contains("default")) {
					//this should always be the case.
					return "default";
				} else {
					return "";
				}
			}
		}

		public String getHFOnFlip() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getHapticFeedbackOnFlip();
			}
		}

		public String getHFOnPress() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getHapticFeedbackOnPress();
			}
		}

		public void setHFOnFlip(String use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setHapticFeedbackOnFlip(use);
			}
		}

		public void setHFOnPress(String use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setHapticFeedbackOnPress(use);
			}
		}

		public TimerData getTimer(String ordinal) throws RemoteException {
			synchronized(the_settings) {
				//set up the timer before actually passing it, as it might have a playing item in the queue.
				TimerData the_timer = the_settings.getTimers().get(ordinal);
				TimerExtraTask t = null;
				if(timerTasks.containsKey(ordinal)) {
					t = timerTasks.get(ordinal);
					//harvest info.
					long started_at = t.getStarttime();
					long current = System.currentTimeMillis();
					
					the_timer.setTTF(the_timer.getSeconds()*1000 - (current-started_at));
					the_timer.setPlaying(true);
				} else {
					the_timer.setPlaying(false);
				}
				return the_timer;
			}
		}

		@SuppressWarnings("unchecked")
		public Map getTimers() throws RemoteException {
			synchronized(the_settings) {
				//updat the timer table
				for(TimerData timer : the_settings.getTimers().values()) {
					if(timerTasks.containsKey(timer.getOrdinal().toString())) {
						//get the timer data and update ttf values
						TimerExtraTask t = timerTasks.get(timer.getOrdinal().toString());
						long started = t.getStarttime();
						long now = System.currentTimeMillis();
						
						timer.setPlaying(true);
					
						timer.setTTF(timer.getSeconds()*1000 - (now - started));
					}
					//Log.e("SERVICE","SERVICE SENDING TIMER WITH " + timer.getSeconds().toString() + " SECONDS.");
				}
				
				return the_settings.getTimers();
			}
		}

		public void pauseTimer(String ordinal) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERPAUSE,ordinal));
		}

		public void resetTimer(String ordinal) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERRESET,ordinal));
		}

		public void startTimer(String ordinal) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERSTART, ordinal));
		}

		public void stopTimer(String ordinal) throws RemoteException {
			//not actually needed.
		}

		public void addTimer(TimerData newtimer) throws RemoteException {
			synchronized(the_settings) {
				//Log.e("SERVICE","SERVICE GOT NEW TIMER");
				newtimer.reset();
				the_settings.getTimers().put(newtimer.getOrdinal().toString(), newtimer);
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			}
		}

		public void removeTimer(TimerData deltimer) throws RemoteException {
			synchronized(the_settings) {
				if(timerTasks.containsKey(deltimer.getOrdinal().toString())) {
					TimerExtraTask t = timerTasks.get(deltimer.getOrdinal().toString());
					t.cancel();
					timerTasks.remove(deltimer.getOrdinal().toString());
				}
				//delete timer.
				the_settings.getTimers().remove(deltimer.getOrdinal().toString());
				
				//re-ordinal.
				int ordinal = deltimer.getOrdinal() + 1;
				boolean ended = false;
				while(!ended) {
					if(the_settings.getTimers().containsKey(Integer.toString(ordinal))) {
						TimerData tmp = the_settings.getTimers().remove(Integer.toString(ordinal));
						tmp.setOrdinal(ordinal -1);
						the_settings.getTimers().put(Integer.toString(ordinal-1),tmp);
						
					} else {
						ended = true;
					}
					ordinal = ordinal+1;
				}
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
				//remove this from the clock manager / stop it before removing it.
				//the_settings.getTimers().remove(deltimer.getOrdinal().toString());
			}
		}

		public void updateTimer(TimerData old, TimerData newtimer)
				throws RemoteException {
			synchronized(the_settings) {
				//make sure that any running timers are stopped.
				if(timerTasks.containsKey(old.getOrdinal().toString())) {
					TimerExtraTask t = timerTasks.get(old.getOrdinal().toString());
					t.cancel();
					the_timer.purge();
					timerTasks.remove(old.getOrdinal().toString());
				}
				
				newtimer.reset();
				the_settings.getTimers().put(newtimer.getOrdinal().toString(), newtimer);
				myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			}
		}

		public int getNextTimerOrdinal() throws RemoteException {
			synchronized(the_settings) {
				//list should always be sorted.
				return the_settings.getTimers().size();
			}
		}

		@SuppressWarnings("unchecked")
		public Map getTimerProgressWad() throws RemoteException {
			HashMap<String,TimerProgress> tmp = new HashMap<String,TimerProgress>();
			
			for(TimerData timer : the_settings.getTimers().values()) {
				if(timerTasks.containsKey(timer.getOrdinal().toString())) {
					//get the timer data and update ttf values
					TimerExtraTask t = timerTasks.get(timer.getOrdinal().toString());
					long started = t.getStarttime();
					long now = System.currentTimeMillis();
					
					timer.setPlaying(true);
				
					timer.setTTF(timer.getSeconds()*1000 - (now - started));
					
					//what we are lookin for is the progress and time left.
					TimerProgress p = new TimerProgress();
					p.setTimeleft(timer.getTTF());
					//p.set
					p.setState(TimerProgress.STATE.PLAYING);
					p.setPercentage(((float)timer.getTTF()/1000)/((float)timer.getSeconds()));
					tmp.put(timer.getOrdinal().toString(), p);
					
				} else {
					/*if(timer.getTTF() != timer.getSeconds()*1000) {
						TimerProgress paused = new TimerProgress();
						paused.setState(TimerProgress.STATE.PAUSED);
						paused.setPercentage(((float)timer.getTTF()/1000)/((float)timer.getSeconds()));
						paused.setTimeleft(timer.getTTF());
						tmp.put(timer.getOrdinal().toString(), paused);
						
					} else {
						TimerProgress stopped = new TimerProgress();
						stopped.setState(TimerProgress.STATE.STOPPED);
						stopped.setTimeleft(timer.getSeconds()*1000);
						stopped.setPercentage(100);
						tmp.put(timer.getOrdinal().toString(), stopped);
					}*/
					
				}
				//Log.e("SERVICE","SERVICE SENDING TIMER WITH " + timer.getSeconds().toString() + " SECONDS.");
			}
			
			return tmp;
		}

		public String getEncoding() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getEncoding();
			}
		}

		public void setEncoding(String input) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setEncoding(input);
				the_processor.setEncoding(input);
			}
		}
		
		public String getConnectedTo() throws RemoteException {
			return host + ":" + Integer.toString(port);

		}

		public boolean isDisplayOnBell() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isDisplayOnBell();
			}
		}

		public boolean isKeepScreenOn() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isKeepScreenOn();
			}
		}

		public boolean isLocalEcho() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isLocalEcho();
			}
		}

		public boolean isNotifyOnBell() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isNotifyOnBell();
			}
		}

		public boolean isVibrateOnBell() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isVibrateOnBell();
			}
		}

		public void setDisplayOnBell(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setDisplayOnBell(use);
			}
		}

		public void setKeepScreenOn(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setKeepScreenOn(use);
			}
		}

		public void setLocalEcho(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setLocalEcho(use);
			}
		}

		public void setNotifyOnBell(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setNotifyOnBell(use);
			}
		}

		public void setVibrateOnBell(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setVibrateOnBell(use);
			}
		}

		public boolean isFullScreen() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isFullScreen();
			}
		}

		public void setFullScreen(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setFullScreen(use);
			}
		}

		@SuppressWarnings("unchecked")
		public List getSystemCommands() throws RemoteException {
			ArrayList<String> names = new ArrayList<String>();
			for(String name : specialcommands.keySet()) {
				names.add(name);
			}
			return names;
		}
		
	};
	
	Pattern newline = Pattern.compile("\n");
	Pattern semicolon = Pattern.compile(";");

	//Pattern commandPattern = Pattern.compile("^.(\\w+)\\s+(.+)$");
	Pattern commandPattern = Pattern.compile("^.(\\w+)\\s*(.*)$");
	Matcher commandMatcher = commandPattern.matcher("");
	
	Character cr = new Character((char)13);
	Character lf = new Character((char)10);
	String crlf = cr.toString() + lf.toString();
	
	public String ProcessCommands(String input) {
		
		
		
		//split input into groups.
		String[] commands = semicolon.split(input);
		String output = "";
		int currentstr = 1;
		for(String cmd : commands) {
		
			//cmd = cmd.concat("\n");
			if(cmd.startsWith(".") && the_settings.isProcessPeriod()) {
				//we have a player
				if(cmd.startsWith("..")) {
					if(cmd.equals(".." + crlf) || cmd.equals("..") ) {
						//special case, toggle processing.
						synchronized(the_settings) {
							String outputmsg = "\n" + Colorizer.colorRed + "Dot command processing ";
							if(the_settings.isProcessPeriod()) {
								//the_settings.setProcessPeriod(false);
								overrideProcessPeriods(false);
								outputmsg = outputmsg.concat("disabled.");
							} else {
								//the_settings.setProcessPeriod(true);
								overrideProcessPeriods(true);
								outputmsg = outputmsg.concat("enabled.");
							}
							outputmsg = outputmsg.concat(Colorizer.colorWhite + "\n");
							try {
								doDispatchNoProcess(outputmsg.getBytes(the_settings.getEncoding()));
							} catch (RemoteException e) {
								throw new RuntimeException(e);
							} catch (UnsupportedEncodingException e) {
								throw new RuntimeException(e);
							}
						}
					} else {
						output = output.concat(cmd.replace("..", ".") + ";");
					}
					
					//allow it to go through
				} else {
					//attempt to look up alias name
					commandMatcher.reset(cmd);
					if(commandMatcher.find()) {
						synchronized(the_settings) {
							
							//string should be of the form .aliasname |settarget can have whitespace|

								String alias = commandMatcher.group(1);
								String argument = commandMatcher.group(2);
								
								
								if(the_settings.getAliases().containsKey(alias)) {
									//real argument
									if(!argument.equals("")) {
										AliasData mod = the_settings.getAliases().remove(alias);
										mod.setPost(argument);
										the_settings.getAliases().put(alias, mod);
									} else {
										//display error message
										String noarg_message = "\n" + Colorizer.colorRed + " Alias \"" + alias + "\" can not be set to nothing. Acceptable format is \"." + alias + " replacetext\"" + Colorizer.colorWhite +"\n";
										try {
											doDispatchNoProcess(noarg_message.getBytes(the_settings.getEncoding()));
										} catch (RemoteException e) {
											throw new RuntimeException(e);
										} catch (UnsupportedEncodingException e) {
											throw new RuntimeException(e);
										}
									}
								} else if(specialcommands.containsKey(alias)){
									//Log.e("SERVICE","SERVICE FOUND SPECIAL COMMAND: " + alias);
									SpecialCommand command = specialcommands.get(alias);
									command.execute(argument);
								} else {
									//format error message.
									
									String error = Colorizer.colorRed + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
									error += "  \""+alias+"\" is not a recognized alias or command.\n";
									error += "   No data has been sent to the server. If you intended\n";
									error += "   this to be done, please type \".."+alias+"\"\n";
									error += "   To toggle command processing, input \"..\" with no arguments\n";
									error += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.colorWhite+"\n";  
									
									try {
										doDispatchNoProcess(error.getBytes(the_settings.getEncoding()));
									} catch (RemoteException e) {
										throw new RuntimeException(e);
									} catch (UnsupportedEncodingException e) {
										throw new RuntimeException(e);
									}
								}
							}
					} else {
						//Log.e("SERVICE",cmd + " not valid.");
					}
				}
				
				
			} else {
				//normal command
				//Log.e("SERVICE", cmd+ "| UP FOR NORMAL PROCESSING" );
				if(cmd.equals(".." + crlf) || cmd.equals("..")) {
					//Log.e("SERVICE",cmd + " == ..");
					synchronized(the_settings) {
						String outputmsg = "\n" + Colorizer.colorRed + "Dot command processing ";
						if(the_settings.isProcessPeriod()) {
							//the_settings.setProcessPeriod(false);
							overrideProcessPeriods(false);
							outputmsg = outputmsg.concat("disabled.");
						} else {
							//the_settings.setProcessPeriod(true);
							overrideProcessPeriods(true);
							outputmsg = outputmsg.concat("enabled.");
						}
						outputmsg = outputmsg.concat(Colorizer.colorWhite + "\n");
						try {
							doDispatchNoProcess(outputmsg.getBytes(the_settings.getEncoding()));
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
					}
				} else {
					output = output.concat(cmd + ((currentstr == commands.length) ? "" : ";"));
				}
			}
			
			currentstr++;
		
		}
		
		return output;
	}
	
	private void overrideProcessPeriods(boolean value) {
		synchronized(the_settings) {
			the_settings.setProcessPeriod(value);
			//this can be called from somewhere esle, so to make sure
			//the settings activity doesn't reset a wrong value, we will write
			//the shared preferences key here
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(StellarService.this);
			//boolean setvalue = prefs.getBoolean("PROCESS_PERIOD", true);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("PROCESS_PERIOD", the_settings.isProcessPeriod());
			editor.commit();
			
			//Log.e("SERVICE","SET PROCESS PERIOD FROM:" + setvalue + " to " + value);
			
		}
	}
	
	public Bundle CountNewLine(String ISOLATINstring,int maxlines) {
		
		Matcher match = newline.matcher(ISOLATINstring);
		
		int prunelocation = 0;
		int numberfound = 0;
		while(match.find()) {
			numberfound++;
		}
		
		if(numberfound > maxlines) {
			int numtoprune = numberfound - maxlines;
			match.reset();
			for(int i = 0;i < numtoprune;i++) {
				if(match.find()) { //shouldalways be true
					prunelocation = match.start();
				}
			}
			//by the time we are here, the prunelocation is known
		}
		
		Bundle dat = new Bundle();
		dat.putInt("TOTAL", numberfound);
		dat.putInt("PRUNELOC", prunelocation);
		
		return dat;
	}
	
	StringBuffer joined_alias = new StringBuffer();
	public void buildAliases() {
		joined_alias.setLength(0);
		
		Object[] a = the_settings.getAliases().keySet().toArray();
		
		//StringBuffer joined_alias = new StringBuffer();
		if(a.length > 0) {
			joined_alias.append("(\\b"+(String)a[0]+"\\b)");
			for(int i=1;i<a.length;i++) {
				joined_alias.append("|");
				joined_alias.append("(\\b"+(String)a[i]+"\\b)");
			}
			
		}
		//Log.e("SERVICE","BUILDING ALIAS PATTERN: " + joined_alias.toString());
	}
	
	public void sendInitOk() throws RemoteException {
		
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).loadSettings();
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	
	public void dispatchXMLError(String error) throws RemoteException {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).displayXMLError(error);
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	
	public void sendBuffer() throws RemoteException {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			//callbacks.getBroadcastItem(i).dataIncoming(data);
			//callbacks.getBroadcastItem(i).processedDataIncoming(the_buffer);
			callbacks.getBroadcastItem(i).rawBufferIncoming(the_buffer.toString());
		}
		
		callbacks.finishBroadcast();
		
		//Log.e("SERV","BUFFERED DATA REQUESTED. Delivered and cleared.");
		
		if(the_buffer != null) {
			the_buffer.setLength(0);
			//the_buffer.clearSpans();
		}
	}
	
	Pattern trigger_regex = Pattern.compile("");
	Matcher trigger_matcher = trigger_regex.matcher("");
	private StringBuffer trigger_string = new StringBuffer();
	private Boolean has_triggers = false;
	private void buildTriggerData() {
	
		synchronized(the_settings) {
			
			has_triggers = false; // let us determine if there are still triggers till after the processing is done.
			
			if(the_settings.getTriggers().keySet().size() < 1) {
				has_triggers = false;
				return;
			}
			
			trigger_string.setLength(0);
			for(TriggerData trigger: the_settings.getTriggers().values()) {
				if((trigger.isFireOnce() && !trigger.isFired()) || !trigger.isFireOnce()) {
					//Log.e("SERVICE","WORKING ON TRIGGER:" + trigger.getName());
					has_triggers = true;
					
					if(trigger.isInterpretAsRegex()) {
						trigger_string.append("(" + trigger.getPattern() + ")|");
					} else {
						trigger_string.append("(\\Q" + trigger.getPattern() + "\\E)|");
					}
				}
			}
			
		}
		if(has_triggers) {
			trigger_string.replace(trigger_string.length()-1, trigger_string.length(), ""); //kill the last |
			trigger_regex = Pattern.compile(trigger_string.toString(), Pattern.MULTILINE);
			trigger_matcher = trigger_regex.matcher("");
		} 
		//Log.e("SERVICE","TRIGGER STRING NOW:" + trigger_string.toString());
	}
	
	//private boolean isWifiLocked = false;
	private WifiManager.WifiLock the_wifi_lock = null;
	
	private void EnableWifiKeepAlive() {
		//get the wifi manager
		WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		
		//check if we are connected to a wifi network
		WifiInfo info = wifi.getConnectionInfo();
		if(info.getNetworkId() != -1) {
			//if so, grab the lock
			//Log.e("SERVICE","ATTEMPTING TO GRAB WIFI LOCK");
			the_wifi_lock = wifi.createWifiLock("BLOWTORCH_WIFI_LOCK");
			the_wifi_lock.acquire();
		}
	}
	
	private void DisableWifiKeepAlive() {
		//if we have a wifi lock, release it
		if(the_wifi_lock != null) {
			the_wifi_lock.release();
			the_wifi_lock = null;
		}
	}
	
	private void DispatchToast(String message,boolean longtime) {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).showMessage(message,longtime);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	
	
	private void DispatchDialog(String message) {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).showDialog(message);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}
	private class SpecialCommand {
		public String commandName;
		public SpecialCommand() {
			//nothing really to do here
		}
		public void execute(Object o) {
			//this is to be overridden.
		}
	}
	
	private class ColorDebugCommand extends SpecialCommand{
		public ColorDebugCommand() {
			commandName = "colordebug";
		}
		public void execute(Object o) {
			//Log.e("WINDOW","EXECUTING COLOR DEBUG COMMAND WITH STRING ARGUMENT: " + (String)o);
			String arg = (String)o;
			Integer iarg = 0;
			boolean failed = false;
			
			try {
				iarg = Integer.parseInt(arg);
			} catch (NumberFormatException e) {
				//invalid number
				failed = true;
				//errormessage += "\"colordebug\" special command is unable to use the argument: " + arg + "\n";
				//errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
			}
			if(iarg < 0 || iarg > 3) {
				//invalid number
				failed = true;
			}
			
			if(failed) {
				String errormessage = "\n" + Colorizer.colorRed + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
				if(arg.equals("")) {
					errormessage += "\"colordebug\" special command requires an argument.\n";
				} else {
					errormessage += "\"colordebug\" special command is unable to use the argument: " + arg + "\n";
				}
				errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
				errormessage += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.colorWhite+"\n";
				
				try {
					doDispatchNoProcess(errormessage.getBytes(the_settings.getEncoding()));
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				
				return;
			}
			//if we are here we are good to go.
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
			
			//so with the color debug mode set, we should probably dispatch a message to them.
			String success = "\n" + Colorizer.colorRed + "Color Debug Mode " + iarg + " activated. ";
			if(iarg == 0) {
				success = "\n" + Colorizer.colorRed + "Normal color processing resumed." ;
			} else if(iarg == 1) {
				success += "(color enabled, color codes shown)";
			} else if(iarg == 2) {
				success += "(color disabled, color codes shown)";
			} else if(iarg == 3) {
				success += "(color disabled, color codes not shown)";
			} else {
				success += "(this argument shouldn't happen, contact developer)";
			}
			
			success += Colorizer.colorWhite +"\n";
			
			try {
				doDispatchNoProcess(success.getBytes(the_settings.getEncoding()));
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			
		}
		
	}
	
	/*private class BrokenColor extends SpecialCommand {
		public BrokenColor() {
			this.commandName = "brokencolor";
		}
		
		public void execute(Object o) {
			String testmessage = Colorizer.debugString;
			try {
				doDispatchNoProcess(testmessage.getBytes());
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	}*/
	
	private class DirtyExitCommand extends SpecialCommand {
		public DirtyExitCommand() {
			this.commandName = "closewindow";
		}
		public void execute(Object o) {
			
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
	}
	
	ArrayList<String> timer_actions;
	
	private class TimerCommand extends SpecialCommand {
		
		public TimerCommand() {
			this.commandName = "timer";
		}
		public void execute(Object o)  {
			//example argument " info 0"
			//regex = "^\s+(\S+)\s+(\d+)";
			Pattern p = Pattern.compile("^\\s*(\\S+)\\s+(\\d+)\\s*(\\S*)");
			
			Matcher m = p.matcher((String)o);
			
			int pOrdinal = -1;
			
			if(m.matches()) {
				//extract arguments
				String action = m.group(1).toLowerCase();
				String ordinal = m.group(2);
				String silent = "";
				if(m.groupCount() > 2) {
					silent = m.group(3);
				} else {
					//do nothing
				}
				if(timer_actions.contains(action)) {
					//we have a valid action.
				} else {
					//error with bad action.
					try {
						doDispatchNoProcess(getErrorMessage("Timer action arguemnt " + action + " is invalid.","Acceptable arguments are \"play\",\"pause\",\"reset\" and \"info\".").getBytes());
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
					return;
				}
				
				try {
					pOrdinal = Integer.parseInt(ordinal);
					pOrdinal = pOrdinal + 1;
				} catch (NumberFormatException e) {
					try {
						doDispatchNoProcess(getErrorMessage("Timer index argument " + ordinal + " is not a number.","Acceptable argument is an integer.").getBytes());
						return;
					} catch (RemoteException e1) {
						throw new RuntimeException(e);
					}
				}
				
				if(the_settings.getTimers().containsKey(ordinal)) {
					//valid timer.
					int domsg = 50;
					//Log.e("SERVICE","SILENT IS " + silent);
					if(!silent.equals("")) {
						domsg = 0;
					}
					
					if(action.equals("info")) {
						myhandler.sendMessage(myhandler.obtainMessage(StellarService.MESSAGE_TIMERINFO, ordinal));
						return;
					}
					if(action.equals("reset")) {
						myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERRESET, 0, domsg, ordinal));
						return;
					}
					if(action.equals("play")) {
						//play
						myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERSTART,0,domsg,ordinal));
						return;
					}
					if(action.equals("pause")) {
						myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERPAUSE, 0, domsg, ordinal));
						return;
					}
					
					
				} else {
					//invalid timer
					try {
						doDispatchNoProcess(getErrorMessage("Timer at index " + ordinal + " does not exist.","The timer index is the number displayed next to the timer the timer selection screen.").getBytes());
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
				}
				
				
				
			} else {
				try {
					doDispatchNoProcess(getErrorMessage("Timer command: \".timer" + (String)o + "\" is invalid.","Timer function format \".timer action index [silent]\"\nWhere action is \"play\",\"pause\",\"reset\" or \"info\".\nIndex is the timer index displayed in the timer selection list.").getBytes());
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}
			
		}
	}
	
	private class BellCommand extends SpecialCommand {
		public BellCommand() {
			this.commandName = "bell";
		}
		public void execute(Object o) {
			
			myhandler.sendEmptyMessage(MESSAGE_BELLINC);
			
		}
	}
	
	private class FullScreenCommand extends SpecialCommand {
		public FullScreenCommand() {
			this.commandName = "togglefullscreen";
		}
		public void execute(Object o) {
			
			
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).setScreenMode(!the_settings.isFullScreen());
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
			
		}
	}
	
	private class KeyBoardCommand extends SpecialCommand {
		public KeyBoardCommand() {
			this.commandName = ".keyboard";
			//alternate short form, kb.
		}
		public void execute(Object o) {
			
			//DO ALIAS/VARIABLE TRANSFORMATIONS!!!
			//ACTUALLY, I THINK THE TRANSFORM STEP
			//IS DONE BEFORE SPECIAL COMMAND PARSING.
			
			//command format.
			//.kb message - set keyboard text.
			//.kb add message - append message to current keyboard.
			//.kb popup message - set keyboard text and popup.
			//.kb add popop message - append message and popup.
			//.kb popup add message - same as prev, but with syntax swapped.
			//.kb flush message - send the keyboard.
			//.kb - print the kb help message.
			boolean failed = false;
			if(o==null) {
				//fail, print kb
				failed = true;
			} else if(((String)o).equals("")) {
				//fail, print kb.
				failed = true;
			}
			
			if(failed) {
				try {
					doDispatchNoProcess(getErrorMessage("keyboard (kb) special command usage:",".kb [add]|[popup] message\nadd and popup are optional flags that will append text or popup the window when supplied.\nExample:\n\".kb popup reply \" will put \"reply \" into the input bar and pop up the keyboard.\n\".kb add foo\" will append foo to the current text in the input box and not pop up the keyboard.\nThe cursor is always moved to the end of the new text.").getBytes(the_settings.getEncoding()));
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				return;
			}
			
			Pattern p = Pattern.compile("^\\s*(add|popup|flush){0,1}\\s*(add\\s+|popup\\s+|flush\\s+){0,1}(.*)$");
			Matcher m = p.matcher((String)o);
			String operation1 = "";
			String operation2 = "";
			String text = "";
			if(m.matches()) {
				//match
				operation1 = m.group(1);
				operation2 = m.group(2);
				text = m.group(3);
			} else {
				//shouldn't ever not match.
			}
			boolean doadd = false;
			boolean dopopup = false;
			boolean doflush = false;
			
			if(operation1 != null && !operation1.equals("")) {
				operation1 = operation1.replaceAll("\\s", "");
				if(operation1.equalsIgnoreCase("add")) {
					doadd = true;
				}
				if(operation1.equalsIgnoreCase("popup")) {
					dopopup = true;
				}
			}
			if(operation2 != null && !operation2.equals("")) {
				operation2 = operation2.replaceAll("\\s", "");
				if(operation2.equalsIgnoreCase("add")) {
					doadd = true;
				}
				if(operation2.equalsIgnoreCase("popup")) {
					dopopup = true;
				}
			}
			
			if(operation1 != null && !operation1.equals("")) {
				
				if(operation1.equalsIgnoreCase("flush")) {
					doflush = true;
				}
			}
			
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).showKeyBoard(text,dopopup,doadd,doflush);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
			
		}
	}
	
	private String getErrorMessage(String arg1,String arg2) {
		
		String errormessage = "\n" + Colorizer.colorRed + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
		errormessage += arg1 + "\n";
		errormessage += arg2 + "\n";
		//errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
		errormessage += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.colorWhite+"\n";
		return errormessage;
	}
	
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	
	
	
	Colorizer colorer = new Colorizer();
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	StringBuffer regexp_test = new StringBuffer();
	Vector<String> test_set = new Vector<String>();
	
	boolean firstDispatch = true;
	
	public void dispatch(byte[] data) throws RemoteException, UnsupportedEncodingException {
		
		String rawData = the_processor.RawProcess(data);
		//changing this to send data to the window, then process the triggers.
		//if(firstDispatch)
		//Spannable processed = the_processor.DoProcess(data);
		Message dofinal = myhandler.obtainMessage(MESSAGE_DOFINALDISPATCH,rawData);
		myhandler.sendMessage(dofinal);
	}
	

	HashMap<String,String> captureMap = new HashMap<String,String>();
	public void dispatchFinish(String rawData) {
		
		//String htmlText = colorer.htmlColorize(data);
		//Log.e("SERV","MADE SOME HTML:"+htmlText);
		//if(firstDispatch)
		
		
		final int N = callbacks.beginBroadcast();
		int final_count = N;
	
		for(int i = 0;i<N;i++) {
			try {
			callbacks.getBroadcastItem(i).rawDataIncoming(rawData);
			} catch (RemoteException e) {
				//just need to catch it, don't need to care, the list maintains itself apparently.
				final_count = final_count - 1;
			}
		}
		callbacks.finishBroadcast();
		
		//if(callbacks.)
		if(final_count == 0) {
			//someone isnt listening so save the buffer
			the_buffer.append(rawData);
			//Log.e("SERV","No listeners, buffering data.");
		} else {
			//someone is listening so save the buffer.
			the_buffer.setLength(0);
			//the_buffer.clearSpans();
			//Log.e("SERV","Clearing the buffer because I have " + bindCount + " listeners.");
		}
		
		//IDLE:  "Your eyes glaze over."
		//REQU:  "QUEST: You may now quest again."
		if(trigger_string.length() < 1) {
			return; //return without processing, if there are no triggers.
		}
		
		Matcher stripcolor = colordata.matcher(rawData);
		regexp_test.append(stripcolor.replaceAll(""));
		
		boolean rebuildTriggers = false;
		//test the de-colorized data against registered patterns.
		
		
		if(has_triggers) {
			
			trigger_matcher.reset(regexp_test);
		
			while(trigger_matcher.find()) {
				//so if we found something here, we triggered.
				//Log.e("SERVICE","TRIGGERPARSE FOUND" + trigger_matcher.group(0));
				TriggerData triggered = the_settings.getTriggers().get(trigger_matcher.group(0));
				if(triggered != null) {
					//build hash map, if we are here we have a literal match.
					captureMap.clear();
					captureMap.put("0", trigger_matcher.group(0)); //it is only ever going to have 1 group.
					
					//iterate through the responders.
					for(TriggerResponder responder : triggered.getResponders()) {
						responder.doResponse(this, display, trigger_count++,hasListener,myhandler,captureMap);
					}
					
					if(triggered.isFireOnce()) {
						triggered.setFired(true);
						rebuildTriggers = true;
					}
				} else {
					//the hash map lookup failed this could mean that we are looking at a regexp trigger
					//pull out each trigger, if it is a regexp trigger, check if it matches the group, if it does,
					//then we have our winnar and we should process that trigger.
					for(TriggerData data : the_settings.getTriggers().values()) {
						if(data.isInterpretAsRegex()) {
							Pattern pattern = Pattern.compile(data.getPattern());
							Matcher testmatch = pattern.matcher(trigger_matcher.group(0));
							if(testmatch.matches()) {
								//we have a wienner.
								//build the capture map so the responders can respond.
								captureMap.clear();
								for(int i=0;i<=testmatch.groupCount();i++) {
									captureMap.put(Integer.toString(i), testmatch.group(i));
								}
								for(TriggerResponder responder : data.getResponders()) {
									responder.doResponse(this, display, trigger_count++, hasListener, myhandler,captureMap);
								}
								
								if(data.isFireOnce()) {
									data.setFired(true);
									rebuildTriggers = true;
								}
							}
						}
					}
				}
			}
			
			if(rebuildTriggers) {
				rebuildTriggers = false;
				buildTriggerData();
			}
		}
		
		regexp_test.setLength(0);
		
	}
	
	public void doDispatchNoProcess(byte[] data) throws RemoteException{
		
		String rawData = null;
		try {
			rawData = new String(data,the_settings.getEncoding());
		} catch (UnsupportedEncodingException e1) {
			
			e1.printStackTrace();
		}
		
		final int N = callbacks.beginBroadcast();
		int final_count = N;
		

		//Log.e("SERVICE","SENDING TO WINDOW: " + rawData);
		for(int i = 0;i<N;i++) {
			try {
			callbacks.getBroadcastItem(i).rawDataIncoming(rawData);
			} catch (RemoteException e) {
				//just need to catch it, don't need to care, the list maintains itself apparently.
				final_count = final_count - 1;
			}
		}
		callbacks.finishBroadcast();
		if(final_count == 0) {
			//someone is listening so don't save the buffer
			//Log.e("SERV","No listeners, buffering data.");
		} else {
			
			the_buffer.setLength(0);
			//the_buffer.clearSpans();
			//Log.e("SERV","Clearing the buffer because I have " + bindCount + " listeners.");
		}
	}
	
	private void DoTimerResponders(String ordinal) {
		synchronized(the_settings) {
			
			//just a precaution, 
			if(myhandler == null) {
				return; //responders need the handler, and will choke on null.
			}
			
			if(!the_settings.getTimers().containsKey(ordinal)) {
				return; // no ordinal
			}
			
			TimerData data = the_settings.getTimers().get(ordinal);
			if(data == null) {
				return; //this shoudn't happen. means there is a null entry in the map.
			}
			
			for(TriggerResponder responder : data.getResponders()) {
				responder.doResponse(StellarService.this.getApplicationContext(), display, trigger_count++, hasListener, myhandler, null);
			}
		}
	}
	
	private void DoTimerStart(String timer,Integer loud) {
		TimerData data = the_settings.getTimers().get(timer);
		if(data == null || timerTasks.containsKey(timer)) {
			//no timer with that ordinal,or it is already started
		} else {
			TimerExtraTask t = new TimerExtraTask(Integer.parseInt(timer),System.currentTimeMillis(),myhandler);
			if(data.getPauseLocation() == 0) {
				t.setStarttime(System.currentTimeMillis());
			} else {
				t.setStarttime(System.currentTimeMillis() - data.getPauseLocation());
			}
			timerTasks.put(timer, t);
			if(data.isRepeat()) {
				the_timer.scheduleAtFixedRate(t, data.getTTF() , data.getSeconds()*1000);
			} else {
				//do once
				//the_timer.scheduleAtFixedRate(t, data.getTTF() , data.getSeconds()*1000);
				the_timer.schedule(t, data.getTTF());
			}
			if(loud == 50) {
				//send message.
				DispatchToast("Timer " + timer + " started.",false);
			}
		}
	}
	
	boolean debug = false;
	
	public void doStartup() throws UnknownHostException, IOException, RemoteException {
		if(host == BAD_HOST || port == BAD_PORT) {
			return; //dont' start 
		}
		
		if(debug) {
			return;
		}
		
		

		doDispatchNoProcess(new String(Colorizer.colorCyanBright+"Attempting connection to: "+ Colorizer.colorYeollowBright + host + ":"+port+"\n"+Colorizer.colorCyanBright+"Timeout set to 14 seconds."+Colorizer.colorWhite+"\n").getBytes());
	
		
		InetAddress addr = null;
		
			//InetAddress[] x = InetAddress.getAllByName(host);
		try {
			addr = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			DispatchDialog("Unknown Host: " + e.getMessage());
			return;
		}
			//addr = x[0];
			
		String ip = addr.getHostAddress();
		if(ip.equals(host)) {
			//it was an ip address, so don't display it.
		} else {
			doDispatchNoProcess(new String(Colorizer.colorCyanBright+"Looked up: "+Colorizer.colorYeollowBright + ip +Colorizer.colorCyanBright+ " for "+Colorizer.colorYeollowBright+host+Colorizer.colorWhite+"\n").getBytes());
		}
		
		the_addr = addr;
		if(the_addr == null) {
			//Log.e("SERV","NULL ADDRESS LOOKED UP");
		}
		

		
		try {
			
			//the
			//the_socket = new Socket(addr.getHostAddress(),port);
			
			the_socket = new Socket();
			SocketAddress adr = new InetSocketAddress(addr,port);
			

			the_socket.connect(adr, 14000);
			doDispatchNoProcess(new String(Colorizer.colorCyanBright+"Connected to: "+Colorizer.colorYeollowBright+host+Colorizer.colorCyanBright+"!"+Colorizer.colorWhite+"\n").getBytes());
			
			the_socket.setSendBufferSize(1024);
			
			try {
				output_writer = new BufferedOutputStream(the_socket.getOutputStream());
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			
			try {
				pump = new DataPumper(the_socket.getInputStream(),null,myhandler);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			pump.start();
			
			synchronized(this) {
				try {
					this.wait(500);
					//give the pump some time sto start up
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} 
			}
			
			pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_INITXFER);
			
			//show notification
			showNotification();
			
			the_processor = new Processor(myhandler,mBinder,the_settings.getEncoding());
			the_buffer = new StringBuffer();
			
			
			synchronized(the_settings) {
				if(the_settings.isKeepWifiActive()) {
					EnableWifiKeepAlive();
				}
			}
			
			
			//BEGIN OPERATIONS!
			
		} catch (SocketException e) {
			DispatchDialog("Socket Exception: " + e.getMessage());
			//Log.e("SERVICE","NET FAILURE:" + e.getMessage());
		} catch (SocketTimeoutException e) {
			DispatchDialog("Operation timed out.");
		} catch (ProtocolException e) {
			DispatchDialog("Protocol Exception: " + e.getMessage());
		}

		

	}
	
	private void showNotification() {
		
		
		
		Notification note = new Notification(com.happygoatstudios.bt.R.drawable.blowtorch_notification2,"BlowTorch Initialized",System.currentTimeMillis());
		//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "BlowTorch";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = "Connected: ("+ host +":"+ port + ")";
		Intent notificationIntent = new Intent(this, com.happygoatstudios.bt.window.MainWindow.class);
		notificationIntent.putExtra("DISPLAY", display);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = com.happygoatstudios.bt.R.drawable.blowtorch_notification2;
		note.flags = Notification.FLAG_ONGOING_EVENT;
		
		//startForeground to avoid being killed off.
		this.startForeground(5545, note);
		
		//mNM.notify(5545,note);
		
	}
	
	public void doShutdown() {
		//pump.stop();
		the_timer.cancel();
		
		if(pump != null) {
			pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_END);
			pump = null;
		}
		
		if(output_writer != null) {
			try {
				output_writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}	
		}
		
		if(the_socket != null) {
			try {
				the_socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			the_socket = null;
		}
		//kill the notification.
		mNM.cancel(5545);
		mNM.cancelAll();
		
	}
	
	public void doProcess(byte[] data) {
		//broadcast this data.
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

}
