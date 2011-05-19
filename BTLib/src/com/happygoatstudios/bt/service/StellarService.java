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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.service.IStellarServiceCallback;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.ConfigurationLoader;
import com.happygoatstudios.bt.settings.HyperSAXParser;
import com.happygoatstudios.bt.settings.HyperSettings;
import com.happygoatstudios.bt.speedwalk.DirectionData;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerExtraTask;
import com.happygoatstudios.bt.timer.TimerProgress;
import com.happygoatstudios.bt.trigger.TriggerData;

import dalvik.system.PathClassLoader;


public class StellarService extends Service {

	public static final String ALIAS_PREFS = "ALIAS_SETTINGS";
	TreeMap<String, String> aliases = new TreeMap<String, String>();
	RemoteCallbackList<IStellarServiceCallback> callbacks = new RemoteCallbackList<IStellarServiceCallback>();
	HyperSettings the_settings = new HyperSettings();
	NotificationManager mNM;
	OutputStream output_writer = null;
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
	protected static final int MESSAGE_DISPLAYPARAMS = 504;
	public static final int MESSAGE_DISCONNECTED = 505;
	protected static final int MESSAGE_RECONNECT = 506;
	public static final int MESSAGE_DODISCONNECT = 507;
	public static final int MESSAGE_PROCESSORWARNING = 508;
	public static final int MESSAGE_DEBUGTELNET = 509;
	protected static final int MESSAGE_DOBUTTONRELOAD = 510;
	public static final int MESSAGE_MCCPFATALERROR = 511;
	public boolean sending = false;
	String settingslocation = "test_settings2.xml";
	com.happygoatstudios.bt.window.TextTree buffer_tree = new com.happygoatstudios.bt.window.TextTree();

	Timer the_timer = new Timer("BLOWTORCH_TIMER",true);
	HashMap<String,TimerExtraTask> timerTasks = new HashMap<String,TimerExtraTask>();
	
	public void onLowMemory() {
		//Log.e("SERVICE","The service has been requested to shore up memory usage, potentially going to be killed.");
	}
	
	public int onStartCommand(Intent intent,int flags,int startId) {
		if(intent == null) {
			//Log.e("SERVICE","onStartCommand passed null intent");
			return Service.START_STICKY;
		}
		
		if(ConfigurationLoader.isTestMode(this.getApplicationContext())) {
			Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this.getApplicationContext()));
		}
		
		return Service.START_STICKY;
	}
	
	public void onCreate() {
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mNM.cancel(5546);
		host = BAD_HOST;
		port = BAD_PORT;
		
		
		ColorDebugCommand colordebug = new ColorDebugCommand();
		DirtyExitCommand dirtyexit = new DirtyExitCommand();
		TimerCommand timercmd = new TimerCommand();
		BellCommand bellcmd = new BellCommand();
		FullScreenCommand fscmd = new FullScreenCommand();
		KeyBoardCommand kbcmd = new KeyBoardCommand();
		DisconnectCommand dccmd = new DisconnectCommand();
		ReconnectCommand rccmd = new ReconnectCommand();
		SpeedwalkCommand swcmd = new SpeedwalkCommand();
		LoadButtonsCommand lbcmd = new LoadButtonsCommand();
		ClearButtonsCommand cbcmd = new ClearButtonsCommand();
		specialcommands.put(colordebug.commandName, colordebug);
		specialcommands.put(dirtyexit.commandName, dirtyexit);
		specialcommands.put(timercmd.commandName, timercmd);
		specialcommands.put(bellcmd.commandName, bellcmd);
		specialcommands.put(fscmd.commandName, fscmd);
		specialcommands.put(kbcmd.commandName, kbcmd);
		specialcommands.put("kb", kbcmd);
		specialcommands.put(dccmd.commandName, dccmd);
		specialcommands.put(rccmd.commandName, rccmd);
		specialcommands.put(swcmd.commandName, swcmd);
		specialcommands.put(lbcmd.commandName, lbcmd);
		specialcommands.put(cbcmd.commandName, cbcmd);
		
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
		settingslocation = prefsname + ".xml";
		loadXmlSettings(prefsname +".xml");
		
		buffer_tree.setLineBreakAt(80); //this doesn't really matter
		buffer_tree.setEncoding(the_settings.getEncoding());
		buffer_tree.setMaxLines(the_settings.getMaxLines());
		
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_DOBUTTONRELOAD:
					DispatchButtonLoad((String)msg.obj);
					break;
				case MESSAGE_DEBUGTELNET:
					if(the_processor != null) {
						the_processor.setDebugTelnet((Boolean)msg.obj);
					}
					break;
				case MESSAGE_PROCESSORWARNING:
					try {
						doDispatchNoProcess(((String)msg.obj).getBytes(the_settings.getEncoding()));
					} catch (RemoteException e4) {
						throw new RuntimeException(e4);
					} catch (UnsupportedEncodingException e4) {
						throw new RuntimeException(e4);
					}
					break;
				case MESSAGE_MCCPFATALERROR:
					Message endCompress = this.obtainMessage(MESSAGE_SENDOPTIONDATA);
					Bundle eb = endCompress.getData();
					byte[] ec_neg = new byte[] { TC.IAC , TC.DONT , TC.COMPRESS2 };
					eb.putByteArray("THE_DATA", ec_neg);
					endCompress.setData(eb);
					this.sendMessage(endCompress);
					try {
						StellarService.this.doDispatchNoProcess(new String("\n\n" + Colorizer.colorRed + "MCCP Data Format Error - Attempting to restart MCCP\nSome data may be lost. Reconnect if compression is not restarted automatically." + Colorizer.colorWhite + "\n\n").getBytes(the_settings.getEncoding()));
					} catch (RemoteException e4) {
						e4.printStackTrace();
					} catch (UnsupportedEncodingException e4) {
						e4.printStackTrace();
					}
					//Message startCompress = this.obtainMessage(MESSAGE_SENDOPTIONDATA);
					//Bundle sb = endCompress.getData();
					byte[] sc_neg = new byte[] { TC.IAC , TC.WILL , TC.COMPRESS2 };
					//sb.putByteArray("THE_DATA", sc_neg);
					//sb.putString("DEBUG_MESSAGE", "\nIAC DO COMPRESS2 - Sent\n");
					//startCompress.setData(sb);
					//this.sendMessageDelayed(startCompress,20);
					the_processor.RawProcess(sc_neg);
					break;
				case MESSAGE_DODISCONNECT:
					killNetThreads();
					DoDisconnect(null);
					isConnected = false;
					break;
				case MESSAGE_RECONNECT:
					killNetThreads();
					for(TriggerData t : the_settings.getTriggers().values()) {
						if(t.isFireOnce()) {
							t.setFired(false);
						}
					}
					buildTriggerData();
					if(the_processor != null) { the_processor.reset();  }// corner case. Not sure how to make this null.//2/16/2008 -- Fix for NullPointerException found in StellarService$handler
					
					try {
						doStartup();
					} catch (UnknownHostException e3) {
						throw new RuntimeException(e3);
					} catch (RemoteException e3) {
						throw new RuntimeException(e3);
					} catch (IOException e3) {
						throw new RuntimeException(e3);
					}
					
					
					break;
				case MESSAGE_DISCONNECTED:
					//the pump has reported the connection closed.
					//Log.e("SERV","ACTIVATING DISCONNECT");
					killNetThreads();
					DoDisconnect(null);
					isConnected = false;
					break;
				case MESSAGE_DISPLAYPARAMS:
					if(the_processor != null) {
						the_processor.setDisplayDimensions(msg.arg1, msg.arg2);
						the_processor.disaptchNawsString();
					}
					break;
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
					String ordinal = Integer.toString(msg.arg1);
					DoTimerResponders(ordinal);
					TimerData td = the_settings.getTimers().get(ordinal);
					if(td != null) {
						if(!td.isRepeat()) {
							TimerExtraTask tt = timerTasks.remove(ordinal);
							tt.cancel();
							the_timer.purge();
							td.reset();
							td.setPlaying(false);
						}
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
					break;
				case MESSAGE_INIT:
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
					pump.stop();
					doShutdown();
					break;
				case MESSAGE_PROCESS:
					try {
						dispatch((byte[])msg.obj);
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
					break;
				case MESSAGE_DOFINALDISPATCH:
					try {
						dispatchFinish((byte[])msg.obj);
					} catch (UnsupportedEncodingException e3) {
						e3.printStackTrace();
					}
					break;
				case MESSAGE_SETDATA:
					host = msg.getData().getString("HOST");
					port = msg.getData().getInt("PORT");
					display = msg.getData().getString("DISPLAY");					
					showNotification();
					break;
				case MESSAGE_STARTCOMPRESS:
					pump.getHandler().sendMessage(pump.getHandler().obtainMessage(DataPumper.MESSAGE_COMPRESS,msg.obj));
					break;
				case MESSAGE_ENDCOMPRESS:
					break;
				case MESSAGE_SENDOPTIONDATA:
					Bundle b = msg.getData();
					byte[] obytes = b.getByteArray("THE_DATA");
					String message = b.getString("DEBUG_MESSAGE");
					if(message != null) {
						try {
							doDispatchNoProcess(message.getBytes(the_settings.getEncoding()));
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					
					try {
						if(output_writer != null) {
							output_writer.write(obytes);
							output_writer.flush();
						}
					} catch (IOException e2) {
						throw new RuntimeException(e2);
					}
					break;
				case MESSAGE_SENDDATA:
					
					byte[] bytes = (byte[]) msg.obj;
					
					Data d = null;
					try {
						d = ProcessOutputData(new String(bytes,the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e2) {
						e2.printStackTrace();
					}
					
					if(d == null) {
						return;
					}
					
					String nosemidata = null;
					try {
						
						if(d.cmdString != null && !d.cmdString.equals("")) {
							nosemidata = d.cmdString;
							byte[] sendtest = nosemidata.getBytes(the_settings.getEncoding());
							ByteBuffer buf = ByteBuffer.allocate(sendtest.length*2); //just in case EVERY byte is the IAC
							//int found = 0;
							int count = 0;
							for(int i=0;i<sendtest.length;i++) {
								if(sendtest[i] == (byte)0xFF) {
									//buf = ByteBuffer.wrap(buf.array());
									buf.put((byte)0xFF);
									buf.put((byte)0xFF);
									count += 2;
								} else {
									buf.put(sendtest[i]);
									count++;
								}
							}
							
							byte[] tosend = new byte[count];
							buf.rewind();
							buf.get(tosend,0,count);
							
							if(output_writer != null) {
								output_writer.write(tosend);
								output_writer.flush();
							} else {
								doDispatchNoProcess(new String(Colorizer.colorRed + "\nDisconnected.\n" + Colorizer.colorWhite).getBytes("UTF-8"));
							}
						} else {
							if(d.cmdString.equals("") && d.visString == null) {
								output_writer.write(crlf.getBytes(the_settings.getEncoding()));
								output_writer.flush();
								d.visString = "\n";
							}
						}
						//send the transformed data back to the window
						if(d.visString != null && !d.visString.equals("")) {
							try {
								if(the_settings.isLocalEcho()) {
									//preserve.
									//buffer_tree.addBytesImplSimple(data)
									doDispatchNoProcess(d.visString.getBytes(the_settings.getEncoding()));
								}
							} catch (RemoteException e) {
								throw new RuntimeException(e);
							}
						}
					} catch (IOException e) {
						//throw new RuntimeException(e);
						myhandler.sendEmptyMessage(MESSAGE_DISCONNECTED);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					break;
				case MESSAGE_REQUESTBUFFER:
					try {
						sendBuffer();
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
					break;
				case MESSAGE_SAVEBUFFER:
					try {
						buffer_tree.addBytesImpl((byte[])msg.obj);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
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
	
	private void DoDisconnect(String message) {
		//attempt to display the disconnection dialog.
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).doDisconnectNotice();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
		
		if(N < 1) {
			//no listeneres, just shutdown and put up a new notification.
			ShowDisconnectedNotification(message);
			//doShutdown();
		}
		
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
		
		HyperSAXParser parser = new HyperSAXParser(filename,this);
		
		try {
			the_settings = parser.load();
		} catch (FileNotFoundException e) {
			the_settings = loadDefaultSettings();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			//settings wad corrupted.
			String message = e.getMessage();
			try {
				dispatchXMLError(message);
			} catch (RemoteException e1) {
			}
			//throw new RuntimeException(e);
		}
		
		if(the_settings.getDirections().size() == 0) {
			loadDefaultDirections();
		}
		buildAliases();
		buildTriggerData();
		
	}
	
	private HyperSettings loadDefaultSettings() {
		HyperSettings tmp = null;
		HyperSAXParser parser = null;
		parser = new HyperSAXParser(null,this); //load default settings.
		try {
			tmp = parser.load();
		} catch (FileNotFoundException e1) {
			// SHOULD NEVER GET HERE, "null" load implementation uses a resource handle to a file that will guaranteed be there.
		} catch (IOException e1) {
			// SHOULD NEVER GET HERE, well, this one maybe, if the internal memory got severed by a micrometeorite or cosmic ray.
			throw new RuntimeException(e1);
		} catch (SAXException e1) {
			//SHOULD NEVER GET HERE, defaults files should always be "parseable" before getting wadd-ed.
		}
		tmp.setLineSize(calculate80CharFontSize());
		
		//TODO: adjust default buttons.
		//this is tricky, got to go through and adjust the dip to real pixel values, and compute the max/min bounding box.
		float margin = 7.0f; //10 dip margin
		float right = 0.0f;
		float bottom = 0.0f;
		float left = 100000.0f;
		float top = 100000.0f;
		float density = this.getResources().getDisplayMetrics().density;
		for(Vector<SlickButtonData> set : tmp.getButtonSets().values()) {
			for(SlickButtonData data : set) {
				data.setX((int)(data.getX() * density));
				data.setY((int)(data.getY() * density));
				if((data.getX() + ((data.getWidth() * density)/2)) > right) right = data.getX() + ((data.getWidth() * density)/2);
				if((data.getY() + ((data.getHeight() * density)/2)) > bottom) bottom = data.getY() + ((data.getHeight() * density)/2);
				if((data.getX() - ((data.getWidth() * density)/2)) < left) left = data.getX() - ((data.getWidth() * density)/2);
				if((data.getY() - ((data.getHeight() * density)/2)) < top) top = data.getY() - ((data.getHeight() * density)/2);
			}
		}
		
		//so now we have the "bounding box" so we need to compute the shift to the right corner.
		int width = this.getResources().getDisplayMetrics().widthPixels;
		if(width < this.getResources().getDisplayMetrics().heightPixels) width = this.getResources().getDisplayMetrics().heightPixels;
		
		int xOffset = width - (int)right - (int)(margin*density);
		for(Vector<SlickButtonData> set : tmp.getButtonSets().values()) {
			for(SlickButtonData data : set) {
				data.setX(data.getX() + xOffset);
				//data.setY(data.getY() + offset);
			}
		}
		return tmp;
	}
	
	private void loadDefaultDirections() {
		HashMap<String,DirectionData> tmp = new HashMap<String,DirectionData>();
		tmp.put("n", new DirectionData("n","n"));
		tmp.put("e", new DirectionData("e","e"));
		tmp.put("s", new DirectionData("s","s"));
		tmp.put("w", new DirectionData("w","w"));
		tmp.put("h", new DirectionData("h","nw"));
		tmp.put("j", new DirectionData("j","ne"));
		tmp.put("k", new DirectionData("k","sw"));
		tmp.put("l", new DirectionData("l","se"));
		the_settings.setDirections(tmp);
	}
	
	private void doVibrateBell() {
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
	}
	
	private int bellcount = 3344;
	private void doNotifyBell() { 
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		Notification note = new Notification(resId,"Alert!",System.currentTimeMillis());
		//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "BlowTorch - Alert!";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = "The server is notifying you with the bell character, 0x07.";
		Intent notificationIntent = null;
		String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this.getApplicationContext());
		notificationIntent = new Intent(windowAction);
		notificationIntent.putExtra("DISPLAY", display);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
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
	protected boolean isConnected = false;
	private final IStellarService.Stub mBinder = new IStellarService.Stub() {
		public void registerCallback(IStellarServiceCallback m) throws RemoteException {
			if(m != null && !hasListener) {
				if(callbacks.register(m,binderCookie)) {
					bindCount++;
					//Log.e("SERV","Registering callback, " + bindCount + " now.");
					//hasListener = true;
					hasListener = isWindowShowing();
				} else {
					//Log.e("SERV","Callback not registerd because it is already in the list, " + bindCount + " now.");
				}
			} else {
				callbacks.kill();
				callbacks = new RemoteCallbackList<IStellarServiceCallback>();
				if(m!= null) {
					callbacks.register(m);
					//hasListener = true;
					hasListener = isWindowShowing();
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
					//hasListener = false;
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

		public void saveBuffer(byte[] buffer) throws RemoteException {
			//Message msg = myhandler.obtainMessage(StellarService.MESSAGE_SAVEBUFFER);
			//Bundle b = msg.getData();
			//b.putString("BUFFER",buffer);
			//msg.setData(b);
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_SAVEBUFFER, buffer));
			
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
		public void setAliases(@SuppressWarnings("rawtypes") Map map) throws RemoteException {
			
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


		@SuppressWarnings({ "rawtypes" })
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
				buffer_tree.setMaxLines(keepcount);
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
				} catch (FileNotFoundException e) {
					//shouldn't get here. file path has already been queried and checked.
				} catch (IOException e) {
					throw new RuntimeException(e); //die before corrupting something.
				} catch (SAXException e) {
					String message = e.getMessage();
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
				the_settings = loadDefaultSettings();
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


		@SuppressWarnings({ "rawtypes" })
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

		@SuppressWarnings({ "rawtypes" })
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

		@SuppressWarnings({ "rawtypes" })
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
					TimerProgress p = new TimerProgress();
					p.setTimeleft(timer.getTTF());
					p.setState(TimerProgress.STATE.PLAYING);
					p.setPercentage(((float)timer.getTTF()/1000)/((float)timer.getSeconds()));
					tmp.put(timer.getOrdinal().toString(), p);
					
				} else {
					
				}
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
				buffer_tree.setEncoding(input);
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

		@SuppressWarnings({ "rawtypes" })
		public List getSystemCommands() throws RemoteException {
			ArrayList<String> names = new ArrayList<String>();
			for(String name : specialcommands.keySet()) {
				names.add(name);
			}
			return names;
		}

		public void saveSettings() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
		}

		public void setDisplayDimensions(int rows, int cols)
				throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_DISPLAYPARAMS,rows,cols));
		}

		public void reconnect() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_RECONNECT);
		}

		public boolean hasBuffer() throws RemoteException {
			if(buffer_tree.getBrokenLineCount() > 0) {
				return true;
			} else {
				return false;
			}
		}

		public boolean isConnected() throws RemoteException {
			return isConnected ;
		}

		public boolean isRoundButtons() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isRoundButtons();
			}
		}

		public void setRoundButtons(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setRoundButtons(use);
			}
		}

		public int getBreakAmount() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getBreakAmount();
			}
		}

		public int getOrientation() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getOrientation();
			}
		}

		public boolean isWordWrap() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isWordWrap();
			}
		}

		public void setBreakAmount(int pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setBreakAmount(pIn);
			}
		}

		public void setOrientation(int pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setOrientation(pIn);
			}
		}

		public void setWordWrap(boolean pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setWordWrap(pIn);
			}
		}

		public boolean isDebugTelnet() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isDebugTelnet();
			}
		}

		public boolean isRemoveExtraColor() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isRemoveExtraColor();
			}
		}

		public void setDebugTelnet(boolean pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setDebugTelnet(pIn);
				myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_DEBUGTELNET,new Boolean(pIn)));
			}
		}

		public void setRemoveExtraColor(boolean pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setRemoveExtraColor(pIn);
			}
		}

		public void updateAndRenameSet(String oldSet, String newSet, ColorSetSettings settings) throws RemoteException {
			String currentlyUsed = null;
			synchronized(the_settings) {
				currentlyUsed = the_settings.getLastSelected();
				//update references
				for(String name : the_settings.getButtonSets().keySet()) {
					Vector<SlickButtonData> buttons = the_settings.getButtonSets().get(name);
					for(SlickButtonData button : buttons) {
						if(button.getTargetSet().equals(oldSet)) {
							button.setTargetSet(newSet);
						}
					}
				}
				//remove old set
				Vector<SlickButtonData> newset_buttons = the_settings.getButtonSets().remove(oldSet);
				the_settings.getSetSettings().remove(oldSet);
				//make new set
				the_settings.getButtonSets().put(newSet, newset_buttons);
				the_settings.getSetSettings().put(newSet, settings);
				//send update notification
				if(oldSet.equals(currentlyUsed)) {
					the_settings.setLastSelected(newSet);
				}
			}
			myhandler.sendEmptyMessage(MESSAGE_SAVEXML);
			if(oldSet.equals(currentlyUsed)) {
				myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_DOBUTTONRELOAD,newSet));
			} else {
				//we have to load it anyway in case buttons in that set have been updated to go to the new page.
				myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_DOBUTTONRELOAD,currentlyUsed));
			}
		}

		
		public boolean isEchoAliasUpdate() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isEchoAliasUpdates();
			}
		}

		
		public void setEchoAliasUpdate(boolean use) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setEchoAliasUpdates(use);
			}
		}

		@SuppressWarnings("rawtypes")
		public Map getDirectionData() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getDirections();
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void setDirectionData(Map data) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setDirections((HashMap<String,DirectionData>)data);
			}
		}

		
		public void setHyperLinkMode(String pIn) throws RemoteException {
			synchronized(the_settings) {
				for(HyperSettings.LINK_MODE mode : HyperSettings.LINK_MODE.values()) {
					if(mode.getValue().equals(pIn)) {
						the_settings.setHyperLinkMode(mode);
					}
				}
			}
		}

		
		public String getHyperLinkMode() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getHyperLinkMode().getValue();
			}
		}

		
		public void setHyperLinkColor(int pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setHyperLinkColor(pIn);
			}
		}

		
		public int getHyperLinkColor() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.getHyperLinkColor();
			}
		}

		//@Override
		public void setHyperLinkEnabled(boolean pIn) throws RemoteException {
			synchronized(the_settings) {
				the_settings.setHyperLinkEnabled(pIn);
			}
		}

		//@Override
		public boolean isHyperLinkEnabled() throws RemoteException {
			synchronized(the_settings) {
				return the_settings.isHyperLinkEnabled();
			}
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
	
	public Data ProcessCommand(String cmd) {
		//Log.e("SERVICE","IN CMD: "+cmd);
		Data data = new Data();
		if(cmd.equals(".." + "\n") || cmd.equals("..")) {
			//Log.e("SERVICE","CMD==\"..\"");
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
			
			return null;
		}
		
		
		if(cmd.startsWith(".") && the_settings.isProcessPeriod()) {
			
			if(cmd.startsWith("..")) {
				data.cmdString = cmd.replace("..", ".");
				data.visString = cmd.replace("..", ".");
				return data;
			}
			
			
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
								data.cmdString = "";
								if(the_settings.isEchoAliasUpdates()) {
									data.visString = "["+alias+"=>"+argument+"]";
								} else {
									data.visString = "";
								}
								return data;
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
								return null;
							}
						} else if(specialcommands.containsKey(alias)){
							//Log.e("SERVICE","SERVICE FOUND SPECIAL COMMAND: " + alias);
							SpecialCommand command = specialcommands.get(alias);
							data = (Data) command.execute(argument);
							return data;
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
							return null;
						}
					}
			} else {
				//Log.e("SERVICE",cmd + " not valid.");
			}
			
			return data;
		} else {
			data.cmdString = cmd;
			data.visString = cmd;
			return data;
		}
		
		
		//return null;
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
	private void buildAliases() {
		joined_alias.setLength(0);
		
		//Object[] a = the_settings.getAliases().keySet().toArray();
		Object[] a = the_settings.getAliases().values().toArray();
		
		
		String prefix = "\\b";
		String suffix = "\\b";
		//StringBuffer joined_alias = new StringBuffer();
		if(a.length > 0) {
			if(((AliasData)a[0]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
			if(((AliasData)a[0]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
			joined_alias.append("("+prefix+((AliasData)a[0]).getPre()+suffix+")");
			for(int i=1;i<a.length;i++) {
				if(((AliasData)a[i]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
				if(((AliasData)a[i]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
				joined_alias.append("|");
				joined_alias.append("("+prefix+((AliasData)a[i]).getPre()+suffix+")");
			}
			
		}
		
		alias_replace = Pattern.compile(joined_alias.toString());
		alias_replacer = alias_replace.matcher("");
		alias_recursive = alias_replace.matcher("");
		//Log.e("SERVICE","BUILDING ALIAS PATTERN: " + joined_alias.toString());
	}
	
	
	
	public void sendInitOk() throws RemoteException {
		
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).loadSettings();
		}
		callbacks.finishBroadcast();
	}
	
	public void dispatchXMLError(String error) throws RemoteException {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).displayXMLError(error);
		}
		callbacks.finishBroadcast();
	}
	
	public void sendBuffer() throws RemoteException {
		
		byte[] buf = buffer_tree.dumpToBytes(true);
		
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).rawBufferIncoming(buf);
		}
		
		callbacks.finishBroadcast();
		
		if( N < 1) {
			myhandler.sendEmptyMessageDelayed(MESSAGE_REQUESTBUFFER,100);
			try {
				buffer_tree.addBytesImpl(buf);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
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
	private WifiManager the_wifi_manager = null;
	
	private void EnableWifiKeepAlive() {
		//get the wifi manager
		if(the_wifi_manager == null) {
			the_wifi_manager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		}
			
		//check if we are connected to a wifi network
		WifiInfo info = the_wifi_manager.getConnectionInfo();
		if(info.getNetworkId() != -1) {
			//if so, grab the lock
			//Log.e("SERVICE","ATTEMPTING TO GRAB WIFI LOCK");
			the_wifi_lock = the_wifi_manager.createWifiLock("BLOWTORCH_WIFI_LOCK");
			boolean held = false;
			while(!held) {
				the_wifi_lock.acquire();
				held = the_wifi_lock.isHeld();
			}
		}
	}
	
	private void DisableWifiKeepAlive() {
		//if we have a wifi lock, release it
		if(the_wifi_lock != null) {
			the_wifi_lock.release();
			the_wifi_lock = null;
		}
	}
	
	private void DispatchButtonLoad(String setName) {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).reloadButtons(setName);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
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
	
	StringBuffer dataToServer = new StringBuffer();
	StringBuffer dataToWindow = new StringBuffer();
	private Data ProcessOutputData(String out) throws UnsupportedEncodingException {
		dataToServer.setLength(0);
		dataToWindow.setLength(0);
		//Log.e("BT","PROCESSING: " + out);
		//steps that need to happen.
		if(out.endsWith("\n")) {
			out = out.substring(0, out.length()-2);
		}
		
		if(out.equals("")) {
			Data enter = new Data();
			enter.cmdString = "";
			enter.visString = null;
			return enter;
		}
		//1 - chop up input up on semi's
		String[] commands = null;
		if(the_settings.isSemiIsNewLine()) {
			commands = semicolon.split(out);  
		} else {
			commands = new String[] { out };
		}
		StringBuffer holdover = new StringBuffer();
		//2 - for each unit		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(commands));
		
		
		ListIterator<String> iterator = list.listIterator();
		while(iterator.hasNext()) {
			String cmd = iterator.next();
			
			if(cmd.endsWith("~")) {
				holdover.append(cmd.substring(0,cmd.length()-1) + ";");
			} else {
				if(holdover.length() > 0) {
					cmd = holdover.toString() + cmd;
					holdover.setLength(0);
				}
				//3 - do special command processing.
				Data d = ProcessCommand(cmd);
				//4 - handle command processing output
				
				if(d != null) {
					boolean m = false;
					if(d.cmdString != null && d.visString != null) {
						if(d.cmdString.equals(d.visString)) {
							m = true; //aliases & regular commands will always have the same cmdString and visString
						}
					}
					
					//5 - alias replacement				
					if(d.cmdString != null && !d.cmdString.equals("")) {
						byte[] tmp = DoAliasReplacement(d.cmdString.getBytes(the_settings.getEncoding()));
						String tmpstr = new String(tmp,the_settings.getEncoding());
						//if(tmpstr)
						if(!d.cmdString.equals(tmpstr)) {
							//alias replaced, needs to be processed
							
							String[] alias_cmds = null;
							if(the_settings.isSemiIsNewLine()) {
								alias_cmds = semicolon.split(tmpstr);
							} else {
								alias_cmds = new String[] { tmpstr };
							}
							for(String alias_cmd : alias_cmds) {
								iterator.add(alias_cmd);
							}
							for(int ax=0;ax<alias_cmds.length;ax++) {
								iterator.previous();
							}
						
						} else {
						
							if(m) {
								String srv = new String(tmp,the_settings.getEncoding()) + crlf;
								//srv = srv.replace(";", crlf);
								dataToServer.append(new String(srv));
								
								dataToWindow.append(new String(tmp,the_settings.getEncoding()) + ";");
							} else {
								String srv = new String(tmp,the_settings.getEncoding()) + crlf;
								//srv = srv.replace(";", crlf);
								dataToServer.append(new String(srv));
							}
						}
					}
						//dataToServer.append(d.cmdString + crlf);
					if(d.visString != null && !d.visString.equals("")) {
						if(!m) {
							dataToWindow.append(d.visString + ";");
						}
					}
				}
			

			}
		}
		//7 - return Data packet with commands to send to server, and data to send to window.
		Data d = new Data();
		d.cmdString = dataToServer.toString();
		d.visString = dataToWindow.toString();
		//if(the_settings.isSemiIsNewLine()) {
			if(d.visString.endsWith(";")) {
				d.visString = d.visString.substring(0,d.visString.length()-1) + "\n";
			}
		//}
		//Log.e("BT","TO SERVER:" + d.cmdString);
		//Log.e("BT","TO WINDOW:" + d.visString);
		
		return d;
	}
	
	public class SpecialCommand {
		public String commandName;
		public SpecialCommand() {
			//nothing really to do here
		}
		public Object execute(Object o) {
			//this is to be overridden.
			return null;
		}
		
		
	}
	public class Data {
		public String cmdString;
		public String visString;
		public Data() {
			cmdString = "";
			visString = "";
		}
	}
	
	private class ColorDebugCommand extends SpecialCommand{
		public ColorDebugCommand() {
			commandName = "colordebug";
		}
		public Object execute(Object o) {
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
				
				return null;
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
			
			return null;
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
		public Object execute(Object o) {
			
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
			return null;
		}
	}
	
	ArrayList<String> timer_actions;
	
	private class TimerCommand extends SpecialCommand {
		
		public TimerCommand() {
			this.commandName = "timer";
		}
		public Object execute(Object o)  {
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
					return null;
				}
				
				try {
					pOrdinal = Integer.parseInt(ordinal);
					pOrdinal = pOrdinal + 1;
				} catch (NumberFormatException e) {
					try {
						doDispatchNoProcess(getErrorMessage("Timer index argument " + ordinal + " is not a number.","Acceptable argument is an integer.").getBytes());
						return null;
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
						return null;
					}
					if(action.equals("reset")) {
						myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERRESET, 0, domsg, ordinal));
						return null;
					}
					if(action.equals("play")) {
						//play
						myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERSTART,0,domsg,ordinal));
						return null;
					}
					if(action.equals("pause")) {
						myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TIMERPAUSE, 0, domsg, ordinal));
						return null;
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
			
			return null;
			
		}
	}
	
	private class BellCommand extends SpecialCommand {
		public BellCommand() {
			this.commandName = "dobell";
		}
		public Object execute(Object o) {
			
			myhandler.sendEmptyMessage(MESSAGE_BELLINC);
			
			return null;
			
		}
	}
	
	private class FullScreenCommand extends SpecialCommand {
		public FullScreenCommand() {
			this.commandName = "togglefullscreen";
		}
		public Object execute(Object o) {
			
			
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
			return null;
		}
	}
	
	private class KeyBoardCommand extends SpecialCommand {
		public KeyBoardCommand() {
			this.commandName = "keyboard";
			//alternate short form, kb.
		}
		public Object execute(Object o) {
			
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
			//.kb close - closes the keyboard
			//.kb clear - clears any text in the keyboard
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
					doDispatchNoProcess(getErrorMessage("Keyboard (kb) special command usage:",".kb options message\n" +
							"Options are as follows:\n" +
							"add,popup,flush,close,clear\n"+
							"add and popup are optional flags that will append text or popup the window when supplied.\n" +
							"flush sends the current text in the input window to the server.\n" +
							"close will close the keyboard if it is open.\n"+
							"clear will erase any text that is currently in the input window.\n" +
							"Example:\n" +
							"\".kb popup reply \" will put \"reply \" into the input bar and pop up the keyboard.\n" +
							"\".kb add foo\" will append foo to the current text in the input box and not pop up the keyboard.\n" +
							"\".kb flush\" will transmit the text currently in the box.\n" +
							"The cursor is always moved to the end of the new text.").getBytes(the_settings.getEncoding()));
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
			
			Pattern p = Pattern.compile("^\\s*(add|popup|flush|close|clear){0,1}\\s*(add\\s+|popup\\s+|flush\\s+){0,1}(.*)$");
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
			boolean doclear = false;
			boolean doclose = false;
			
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
				if(operation1.equalsIgnoreCase("clear")) {
					doclear = true;
				}
				if(operation1.equalsIgnoreCase("close")) {
					doclose = true;
				}
			}
			
			try {
				text = new String(DoAliasReplacement(text.getBytes(the_settings.getEncoding())),the_settings.getEncoding());
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
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
			return null;
		}
	}
	
	private class DisconnectCommand extends SpecialCommand {
		
		public DisconnectCommand() {
			this.commandName = "disconnect";
		}
		public Object execute(Object o) {
			
			
			myhandler.sendEmptyMessage(MESSAGE_DODISCONNECT);
			String msg = "\n" + Colorizer.colorRed + "Disconnected." + Colorizer.colorWhite + "\n";
			try {
				doDispatchNoProcess(msg.getBytes(the_settings.getEncoding()));
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
	}
	
	private class ReconnectCommand extends SpecialCommand {
		public ReconnectCommand() {
			this.commandName = "reconnect";
		}
		public Object execute(Object o) {
			
			
			myhandler.sendEmptyMessage(MESSAGE_RECONNECT);
			String msg = "\n" + Colorizer.colorRed + "Reconnecting . . ." + Colorizer.colorWhite + "\n";
			try {
				doDispatchNoProcess(msg.getBytes(the_settings.getEncoding()));
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
	}
	
	private class ClearButtonsCommand extends SpecialCommand {
		public ClearButtonsCommand() {
			this.commandName = "clearbuttons";
		}
		
		public Object execute(Object o) {
			int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).clearAllButtons();
				} catch (RemoteException e) {
				}
			}
			callbacks.finishBroadcast();
			return null;
		}
		
	}
	
	private class LoadButtonsCommand extends SpecialCommand {
		public LoadButtonsCommand() {
			this.commandName = "loadset";
		}
		
		public Object execute(Object o) {
			String str = (String)o;
			if(the_settings.getButtonSets().containsKey(str)) {
				//load that set.
				int N = callbacks.beginBroadcast();
				for(int i=0;i<N;i++) {
					try {
						callbacks.getBroadcastItem(i).reloadButtons(str);
					} catch (RemoteException e) {
					}
				}
				callbacks.finishBroadcast();
			} else {
				//invalid key
				DispatchToast("Button Set: \"" + str + "\" does not exist.",false);
			}
			return null;
		}
	}
	
	private class SpeedwalkCommand extends SpecialCommand {
		
		
		
		public SpeedwalkCommand() {
			this.commandName = "run";
		}
		public Object execute(Object o) {
			String str = (String)o;
			
			Character cr = new Character((char)13);
			Character lf = new Character((char)10);
			String crlf = cr.toString() + lf.toString();
			//str will be of the form, 3d2enewsnu3d32wijkl
			//direction ordinals are now configurable.
			
			if(str.equals("") || str.equals(" ")) {
				try {
					doDispatchNoProcess(getErrorMessage("Speedwalk (run) special command usage:",".run directions\n" +
							"Direction ordinal to command mappings are editable, press MENU->More->Speedwalk Configuration for more info. The default mapping is as follows:\n" +
							" n: north\n e: east\n s: south\n w: west\n u: up\n d: down\n h: northwest\n j: northeast\n k: southwest\n l: southeast\n"+
							"directions may be prefeced with an integer value to run that many times.\n" +
							"Commands may be inserted into the direction stream with commas,\n" +
							"directions may be resumed by entering another comma followed by directions.\n" +
							"Example:\n" +
							"\".run 3desw2n\", will send d;d;d;e;s;w;n;n to the server.\n" +
							"\".run jlk3n3j\", will send se;nw;sw;n;n;n;se;se;se to the server.\n"+
							"\".run 3ds,open door,3w\" will send d;d;d;s;open door;w;w;w to the server.\n").getBytes(the_settings.getEncoding()));
				} catch (RemoteException ef) {
					ef.printStackTrace();
				} catch (UnsupportedEncodingException ea) {
					throw new RuntimeException(ea);
				}
				return null;
				
			}

			StringBuffer buf = new StringBuffer();
			boolean commanding = false;
			LinkedList<Integer> runtable = new LinkedList<Integer>();
			for(int i=0;i<str.length();i++) {
				char theChar = str.charAt(i);
				String bit = String.valueOf(theChar);
				if(commanding) {
					if(bit.equals(",")) {
						commanding = false;
						buf.append(crlf);
					} else {
						buf.append(bit);
					}
				} else {
					
				
					try {
						int num = Integer.parseInt(bit);
						runtable.add(num);
						//place += 1;
						//runlength = (runlength *10) + runlength * num;
					} catch (NumberFormatException e) {
						//got exception, this is a direction or an invalid character.
						boolean valid = false;
						String respString = "";
						
						//make "theChar" a string
						String testVal = Character.toString(theChar);
						if(testVal.equals(",")) {
							commanding = true;
							buf.append(crlf);
						} else {
							//check if the testVal has a mapping in the table
							if(the_settings.getDirections().containsKey(testVal)) {
								valid = true;
								respString = the_settings.getDirections().get(testVal).getCommand();
							}
						}
						
						
						/*switch(theChar) {
						case 'n':
							respString = "n";
							valid = true;
							break;
						case 'e':
							respString = "e";
							valid = true;
							break;
						case 's':
							respString = "s";
							valid = true;
							break;
						case 'w':
							respString = "w";
							valid = true;
							break;
						case 'u':
							respString = "u";
							valid = true;
							break;
						case 'd':
							respString = "d";
							valid = true;
							break;
						case 'h':
							respString = "ne";
							valid = true;
							break;
						case 'j':
							respString = "se";
							valid = true;
							break;
						case 'k':
							respString = "sw";
							valid = true;
							break;
						case 'l':
							respString = "nw";
							valid = true;
							break;
						case ',':
							commanding = true;
							buf.append(crlf);
							break;
						default:
							
						
						}*/
						
						if(valid) {
							//compute the run length.
							int run = 1;
							int tmpPlace = runtable.size()-1;
							if(runtable.size() > 0) {
								run = 0;
								for(Integer tmp : runtable) {
									run += Math.pow(10,tmpPlace) * tmp;
									tmpPlace--;
								}
							}
							
							for(int j=0;j<run;j++) {
								//if(j == run-1) {
								//	buf.append(respString);
								//} else {
									buf.append(respString+crlf);
								//}
							}
							
							runtable.clear();
							
						} else if(!valid && !commanding) {
							//bail with error,
							int errlength = i + 5;
							StringBuffer tmpb = new StringBuffer();
							for(int a=0;a<errlength;a++) {
								tmpb.append("-");
							}
							tmpb.append("^");
							try {
								doDispatchNoProcess(getErrorMessage("Invalid direction in command:","."+commandName + " " +str+"\n" +
										tmpb.toString() + "\n" + 
										"At location " + errlength + ", " + bit).getBytes(the_settings.getEncoding()));
							} catch (RemoteException ef) {
								ef.printStackTrace();
							} catch (UnsupportedEncodingException ea) {
								throw new RuntimeException(ea);
							}
							return null;
						}
					}
				}
			}
			
			Data d = new Data();
			d.cmdString = buf.toString();
			d.cmdString = d.cmdString.substring(0, d.cmdString.length()-2); //strip trailing crlf
			d.visString = ".run " + str;
			
			return d;
		}
	}
	
	private boolean isWindowShowing() {
		boolean result = false;
		final int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				result = callbacks.getBroadcastItem(i).isWindowShowing();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
		
		return result;
	}
	
	/*private void DoBreakAt(int pLines) {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).doLineBreak(pLines);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
	}*/
	
	private String getErrorMessage(String arg1,String arg2) {
		
		String errormessage = "\n" + Colorizer.colorRed + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
		errormessage += arg1 + "\n";
		errormessage += arg2 + "\n";
		//errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
		errormessage += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.colorWhite+"\n";
		return errormessage;
	}
	
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	
	
	
	//Colorizer colorer = new Colorizer();
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	StringBuffer regexp_test = new StringBuffer();
	Vector<String> test_set = new Vector<String>();
	
	boolean firstDispatch = true;
	
	public void dispatch(byte[] data) throws RemoteException, UnsupportedEncodingException {
		
		byte[] rawData = the_processor.RawProcess(data);
		//changing this to send data to the window, then process the triggers.
		//if(firstDispatch)
		if(rawData == null) {
			return;
		}
		//Spannable processed = the_processor.DoProcess(data);
		Message dofinal = myhandler.obtainMessage(MESSAGE_DOFINALDISPATCH,rawData);
		myhandler.sendMessage(dofinal);
	}
	

	HashMap<String,String> captureMap = new HashMap<String,String>();
	//private int bufferLineCount = 0;
	private Pattern bufferLine = Pattern.compile(".*\n");
	Matcher bufferLineMatch = bufferLine.matcher("");
	StringBuffer tempBuffer = new StringBuffer();
	Matcher colorStripper = colordata.matcher("");
	public void dispatchFinish(byte[] rawData) throws UnsupportedEncodingException {
		
		//String htmlText = colorer.htmlColorize(data);
		//Log.e("SERV","MADE SOME HTML:"+htmlText);
		//if(firstDispatch)
		if(rawData == null || rawData.length == 0) { return;}
		//callbacks.
		final int N = callbacks.beginBroadcast();
		int final_count = N;
	
		for(int i = 0;i<N;i++) {
			try {
				if(callbacks.getBroadcastItem(i).isWindowShowing()) {
					callbacks.getBroadcastItem(i).rawDataIncoming(rawData);
				}
			} catch (RemoteException e) {
				//just need to catch it, don't need to care, the list maintains itself apparently.
				final_count = final_count - 1;
			}
		}
		callbacks.finishBroadcast();
		buffer_tree.addBytesImplSimple(rawData);
		buffer_tree.prune();
		
		if(trigger_string.length() < 1) {
			return; //return without processing, if there are no triggers.
		}
		
		colorStripper = colorStripper.reset(new String(rawData,the_settings.getEncoding()));
		regexp_test.append(colorStripper.replaceAll(""));
		
		boolean rebuildTriggers = false;
		
		if(has_triggers) {
			
			trigger_matcher.reset(regexp_test);
			hasListener = isWindowShowing();
			while(trigger_matcher.find()) {
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
		
		buffer_tree.addBytesImplSimple(data);
		ByteBuffer buf = ByteBuffer.allocate(data.length);
		for(int i = 0;i<data.length;i++)  {
			if(data[i] != (byte)0x0d) { //strip carriage
				buf.put(data[i]);
			}
		}
		int size = buf.position();
		byte[] stripped = new byte[size];
		buf.rewind();
		buf.get(stripped,0,size);
		
		
		final int N = callbacks.beginBroadcast();
		int final_count = N;
		

		//Log.e("SERVICE","SENDING TO WINDOW: " + rawData);
		for(int i = 0;i<N;i++) {
			try {
			callbacks.getBroadcastItem(i).rawDataIncoming(stripped);
			} catch (RemoteException e) {
				//just need to catch it, don't need to care, the list maintains itself apparently.
				final_count = final_count - 1;
			}
		}
		callbacks.finishBroadcast();
	}
	
	Pattern alias_replace = Pattern.compile(joined_alias.toString());
	Matcher alias_replacer = alias_replace.matcher("");
	Matcher alias_recursive = alias_replace.matcher("");
	
	Pattern whiteSpace = Pattern.compile("\\s");
	
	private byte[] DoAliasReplacement(byte[] input) {
		if(joined_alias.length() > 0) {

			//Pattern to_replace = Pattern.compile(joined_alias.toString());
			byte[] retval = null;
			//Matcher replacer = null;
			try {
				alias_replacer.reset(new String(input,the_settings.getEncoding()));//replacer = to_replace.matcher(new String(bytes,the_settings.getEncoding()));
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
			StringBuffer replaced = new StringBuffer();
			
			boolean found = false;
			boolean doTail = true;
			while(alias_replacer.find()) {
				found = true;
				
				AliasData replace_with = the_settings.getAliases().get(alias_replacer.group(0));
				//do special replace if only ^ is matched.
				if(replace_with.getPre().startsWith("^") && !replace_with.getPre().endsWith("$")) {
					doTail = false;
					//do special replace.
					String[] tParts = null;
					try {
						tParts = whiteSpace.split(new String(input,the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					HashMap<String,String> map = new HashMap<String,String>();
					for(int i=0;i<tParts.length;i++) {
						map.put(Integer.toString(i), tParts[i]);
					}
					ToastResponder r = new ToastResponder();
					String finalString = r.translate(replace_with.getPost(), map);
					
					replaced.append(finalString);
					
				} else {
					alias_replacer.appendReplacement(replaced, replace_with.getPost());
				}
			}
			if(doTail) {
				alias_replacer.appendTail(replaced);
			}
			StringBuffer buffertemp = new StringBuffer();
			if(found) { //if we replaced a match, we need to continue the find/match process until none are found.
				boolean recursivefound = false;
				do {
					recursivefound = false;
					//Matcher recursivematch = to_replace.matcher(replaced.toString());
					alias_recursive.reset(replaced.toString());
					buffertemp.setLength(0);
					while(alias_recursive.find()) {
						recursivefound = true;
						AliasData replace_with = the_settings.getAliases().get(alias_recursive.group(0));
						alias_recursive.appendReplacement(buffertemp, replace_with.getPost());
					}
					if(recursivefound) {
						alias_recursive.appendTail(buffertemp);
						replaced.setLength(0);
						replaced.append(buffertemp);
					}
				} while(recursivefound == true);
			}
			//so replacer should contain the transformed string now.
			//pull the bytes back out.
			try {
				retval = replaced.toString().getBytes(the_settings.getEncoding());
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
			replaced.setLength(0);
			
			return retval;
		} else {
			return input;
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
			
			hasListener = isWindowShowing();
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
			
			the_processor = new Processor(myhandler,mBinder,the_settings.getEncoding(),this.getApplicationContext());
			synchronized(the_settings) {
				if(the_settings.isKeepWifiActive()) {
					EnableWifiKeepAlive();
				}
				
				the_processor.setDebugTelnet(the_settings.isDebugTelnet());
			}
			
			isConnected = true;
			
		} catch (SocketException e) {
			DispatchDialog("Socket Exception: " + e.getMessage());
			//Log.e("SERVICE","NET FAILURE:" + e.getMessage());
		} catch (SocketTimeoutException e) {
			DispatchDialog("Operation timed out.");
		} catch (ProtocolException e) {
			DispatchDialog("Protocol Exception: " + e.getMessage());
		}

		

	}
	
	private void ShowDisconnectedNotification(String message) {
		
		//mNM.cancel(5545);
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		Notification note = new Notification(resId,"BlowTorch Disconnected",System.currentTimeMillis());
		String defaultmsg = "Click to reconnect: "+ host +":"+ port;
		Context context = getApplicationContext();
		CharSequence contentTitle = "BlowTorch Disconnected";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = null;
		if(message != null && !message.equals("")) {
			contentText = message;
		} else {
			contentText = defaultmsg;
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
			w = Class.forName("com.happygoatstudios.bt.window.MainWindow",false,cl);
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
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
		Pattern invalidchars = Pattern.compile("\\W"); 
		Matcher replacebadchars = invalidchars.matcher(display);
		String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		SharedPreferences sprefs = this.getSharedPreferences(prefsname,0);
		SharedPreferences.Editor editor = sprefs.edit();
		editor.putBoolean("CONNECTED", false);
		editor.putBoolean("FINISHSTART", true);
		editor.commit();
		editor.commit();
		this.stopForeground(true);
		mNM.notify(5546,note);
		showdcmessage = true;
		this.stopSelf();
	}
	
	private void showNotification() {
		
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		
		Notification note = new Notification(resId,"BlowTorch Initialized",System.currentTimeMillis());
		Context context = getApplicationContext();
		
		CharSequence contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
		CharSequence contentText = "Connected: ("+ host +":"+ port + ")";
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
			w = Class.forName("com.happygoatstudios.bt.window.MainWindow",false,cl);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	
		
		try {
			notificationIntent.setClass(this.createPackageContext(this.getPackageName(), Context.CONTEXT_INCLUDE_CODE), w);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		notificationIntent.putExtra("DISPLAY", display);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
		note.flags = Notification.FLAG_ONGOING_EVENT;
		this.startForeground(5545, note);
		
		
	}
	
	public void killNetThreads() {
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
			output_writer = null;
		}
		
		if(the_socket != null) {
			try {
				the_socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			the_socket = null;
		}
	}
	boolean showdcmessage = false;
	public void doShutdown() {
		//pump.stop();
		the_timer.cancel();
		
		killNetThreads();
		//kill the notification.
		mNM.cancel(5545);
		if(!showdcmessage) {
			mNM.cancelAll();
		}
		
		
	}
	
	public void doProcess(byte[] data) {
		//broadcast this data.
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
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

}
