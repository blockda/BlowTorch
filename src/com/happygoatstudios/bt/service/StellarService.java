package com.happygoatstudios.bt.service;




import java.io.BufferedOutputStream;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R;
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
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
//import android.util.Log;
import android.widget.Toast;

import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.service.IStellarServiceCallback;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.HyperSAXParser;
import com.happygoatstudios.bt.settings.HyperSettings;
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

	public boolean sending = false;
	
	StringBuffer the_buffer = new StringBuffer();
	String settingslocation = "test_settings2.xml";
	
	private boolean compressionStarting = false;
	
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
		specialcommands.put(colordebug.commandName, colordebug);
		//specialcommands.put(brokencolor.commandName,brokencolor);
		specialcommands.put(dirtyexit.commandName, dirtyexit);
		
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
					//
					//try {
					//	sendInitOk();
					//} catch (RemoteException e3) {
					//	// TODO Auto-generated catch block
					//	e3.printStackTrace();
					//}
					//loadAliases();
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
					//byte[] obytes = msg.getData().getByteArray("THEDATA");
					byte[] obytes = (byte[])(msg.obj);
					String odbgmsg = null;
					try {
						odbgmsg = new String(obytes,"ISO-8859-1");
					} catch (UnsupportedEncodingException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					//Log.e("SERV","SENDING STRING " + odbgmsg + "|size: " + obytes.length);
					try {
						output_writer.write(obytes);
						output_writer.flush();
						
						//end a "enter" to make sure that the options are recieved.
						//output_writer.write(crlf.getBytes());
						//output_writer.flush();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					
					break;
				case MESSAGE_SENDDATA:
					//Log.e("BTSERVICE","SENDING NORMAL DATA");
					//byte[] bytes = msg.getData().getByteArray("THEDATA");
					byte[] bytes = (byte[]) msg.obj;
					
					//dispatch this for command processing
					String retval = ProcessCommands(new String(bytes));
					if(retval == null || retval.equals("")) {
						//command was intercepted. do nothing for now and return
						//Log.e("SERVICE","CONSUMED ALL COMMANDS");
						return;
					} else {
						//not a command data.
						try {
							//Log.e("SERVICE","PROCESSED COMMANDS AND WAS LEFT WITH:" + retval);
							if(retval.equals("")) { return; }
							bytes = retval.getBytes("ISO-8859-1");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//do search and replace with aliases.
					
					
					//convert all alias keys to the big match string.
					//Object[] a = aliases.keySet().toArray();
					
					//StringBuffer joined_alias = new StringBuffer();
					if(joined_alias.length() > 0) {
						/*joined_alias.append("("+(String)a[0]+")");
						for(int i=1;i<a.length;i++) {
							joined_alias.append("|");
							joined_alias.append("("+(String)a[i]+")");
						}
						*/
						
					
						
						
						Pattern to_replace = Pattern.compile(joined_alias.toString());
						
						Matcher replacer = null;
						try {
							replacer = to_replace.matcher(new String(bytes,"ISO-8859-1"));
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						StringBuffer replaced = new StringBuffer();
						
						boolean found = false;
						while(replacer.find()) {
							//String matched = replacer.group(0);
							found = true;
							String replace_with = the_settings.getAliases().get(replacer.group(0));
							replacer.appendReplacement(replaced, replace_with);
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
									String replace_with = the_settings.getAliases().get(recursivematch.group(0));
									recursivematch.appendReplacement(buffertemp, replace_with);
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
							bytes = replaced.toString().getBytes("ISO-8859-1");
							//Log.e("SERVICE","UNTRNFORMED:" + new String(bytes));
							//Log.e("SERVICE","TRANSFORMED: " + replaced.toString());
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
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
						tostripsemi = new String(bytes,"ISO-8859-1");
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
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
						String dbgmsg = new String(bytes,"ISO-8859-1");
						
						
						//Log.e("SERV","SENDING STRING " + dbgmsg + "|size: " + bytes.length);
						
						output_writer.write(nosemidata.getBytes("ISO-8859-1"));

						output_writer.flush();
						
						//send the transformed data back to the window
						try {
							doDispatchNoProcess(preserve);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//Log.e("SERV","calling notify.");

						//output_writer.
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					//}
					
					}
					break;
				case MESSAGE_REQUESTBUFFER:
					//Log.e("BTSERVICE","SENDING REQUESTED BUFFER");
					//dispatch();
					try {
						sendBuffer();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_SAVEBUFFER:
					//Log.e("BTSERVICE","SAVING TARGET BUFFER");
					the_buffer = new StringBuffer(msg.getData().getString("BUFFER") + the_buffer);
					break;
				//case MESSAGE_CHECKIFALIVE:
				//	if(the_socket != null) {
						//the_socket.
						/*if(!the_socket.isConnected() || the_socket.isClosed()) {
						//do shutdown, except now, attempt to launch a dialog.
							Toast.makeText(BaardTERMService.this, "CONNECTION DEAD", 3000);
						}*/
				//	}
				//	this.sendEmptyMessageDelayed(StellarService.MESSAGE_CHECKIFALIVE, 3000);
				//	break;
				default:
					break;	
				}
				
			}
		};
		
		//Log.e("SERV","REACHED THE END OF THE STARTUP METHOD");
		//Looper.loop();
		//myhandler.sendEmptyMessageDelayed(StellarService.MESSAGE_CHECKIFALIVE, 3000);
		//REGISTER TRIGGER PATTERS.
		//test_set.add("Your eyes glaze over.");
		//test_set.add("QUEST: You may now quest again.");
		//test_set.add("i love kurt");
		//aliases.put("REPLACE", "gulp");
		//aliases.put("TESTREP", "enter");
		
		//read in aliases from disk.
		
		//test_set.add("TICK");
		//test_set.add("--> TICK <--");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (FileNotFoundException e) {
			//if the file does not exist, then we need to load the default settings
			the_settings.getButtonSets().put("default", new Vector<SlickButtonData>());
			ColorSetSettings def_colorset = new ColorSetSettings();
			def_colorset.toDefautls();
			//def_colorset.setPrimaryColor(0xFFFFFFFF);
			the_settings.getSetSettings().put("default", def_colorset);
			
			//TriggerData new_trigger = new TriggerData();
			//new_trigger.setName("testtrigger");
			//new_trigger.setPattern("FOOOCHANGROO!");
			//new_trigger.setInterpretAsRegex(true);
			//NotificationResponder responder = new NotificationResponder();
			//responder.setTitle("You have triggered the beast");
			//responder.setMessage("Mud output for you");
			//new_trigger.getResponders().add(responder);
			//the_settings.getTriggers().put(new_trigger.getName(), new_trigger);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		//Log.e("SERVICE","SETTINGS LOADED");
		//Log.e("SEERVICE","Contains " + the_settings.getButtonSets().size() + " button sets");
		//Set<String> keys = the_settings.getButtonSets().keySet();
		//for(String key : keys) {
			//Log.e("SERVICE","Found ButtonSet:" + key + " , it contains " + the_settings.getButtonSets().get(key).size() + " buttons.");
		//}
		//omg. look at how clean that is.
		
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
			
			//if(hasListener) {
				doThrottleBackground();
			//}
	
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
			//String str = (String)seq;
			
			//byte[] bytes = str.getBytes();
			
			//TODO: instead of sending the message from the binder object, send myself a message, then dispatch to pump
			/*Message msg = pump.getHandler().obtainMessage(DataPumper.MESSAGE_SEND);
			
			Bundle b = new Bundle();
			
			b.putByteArray("THEDATA", seq);
			
			msg.setData(b);
			
			pump.getHandler().sendMessage(msg);*/
			
			//ENTER GIANT SYNCHRONIZATION STEP
			if(myhandler.hasMessages(StellarService.MESSAGE_SENDDATA)) {
			
				//Log.e("SERV","GOING TO SLEEP");
			
				synchronized(sendlock) {
				
					while(myhandler.hasMessages(StellarService.MESSAGE_SENDDATA)) {
						
						try {
							sendlock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				//Log.e("SERV","WAKING BACK UP AFTER NOTIFY CALLED");
			
			}
			
			Message msg = myhandler.obtainMessage(StellarService.MESSAGE_SENDDATA,seq);
			
			//Bundle b = new Bundle();
			
			//b.putByteArray("THEDATA",seq);
			
			//msg.setData(b);
			
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


		public void addAlias(String what, String to) throws RemoteException {
			//aliases.put(what, to);
			the_settings.getAliases().put(what, to);
			buildAliases();
		}


		public Map<String, String> getAliases() throws RemoteException {
			//return aliases;
			return the_settings.getAliases();
		}


		public void setAliases(Map map) throws RemoteException {
			//aliases.clear();
			//aliases = new TreeMap<String, String>(map);
			the_settings.getAliases().clear();
			the_settings.setAliases(new HashMap<String,String>(map));
			buildAliases();
		}


		public void addButton(String targetset, SlickButtonData newButton)
				throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.getButtonSets().get(targetset).add(newButton);
			}
			
			//Log.e("SERVICE","ADDING BUTTON " + newButton.toString() + " FROM BUTTONSET: " + targetset + ", now contains " + the_settings.getButtonSets().get(targetset).size() + " buttons.");
			//Vector<SlickButtonData> buttons = the_settings.getButtonSets().get(targetset);
			//for(SlickButtonData data : buttons) {
			//	Log.e("SERVICE",data.toString());
			//}
		}


		/*public List<SlickButtonData> getSelectedButtonSet()
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}*/


		public void removeButton(String targetset, SlickButtonData buttonToNuke)
				throws RemoteException {
			// TODO Auto-generated method stub
			
			synchronized(the_settings) {
				//Vector<SlickButtonData> testset = the_settings.getButtonSets().get(targetset);
				//for(SlickButtonData tmp : testset) {
					//if(tmp.equals(buttonToNuke)) {
						the_settings.getButtonSets().get(targetset).remove(buttonToNuke);
					//}
				//}
				
				
			}
			
			//Log.e("SERVICE","REMOVING BUTTON " + buttonToNuke.toString() + " FROM BUTTONSET: " + targetset + ", now contains " + the_settings.getButtonSets().get(targetset).size() + " buttons.");
			//Vector<SlickButtonData> buttons = the_settings.getButtonSets().get(targetset);
			//for(SlickButtonData data : buttons) {
			//	Log.e("SERVICE",data.toString());
			//}
			
		}


		public void setFontName(String name) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.setFontName(name);
			}
		}


		public void setFontPath(String path) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.setFontPath(path);
			}
		}


		public void setFontSize(int size) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.setLineSize(size);
			}
		}


		public void setFontSpaceExtra(int size) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.setLineSpaceExtra(size);
			}
		}


		/*public void setSelectedButtonSet(String setname) throws RemoteException {
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				
				int loc = the_settings.getButtonSets().get(targetset).indexOf(orig);
				
				//Log.e("SERVICE","ATTEMPTING TO MODIFY BUTTON: " + orig.toString() + " at location " + loc);
				the_settings.getButtonSets().get(targetset).remove(loc); //remove original
				the_settings.getButtonSets().get(targetset).add(loc,mod); //insert mod in its place
				//the_settings.getButtonSets().get(targetset).
				
			}
			
			//Log.e("SERVICE","MODIFYING BUTTON " + orig.toString() + " FROM BUTTONSET: " + targetset + " with " + mod.toString() + ", now contains " + the_settings.getButtonSets().get(targetset).size() + " buttons.");
			//Vector<SlickButtonData> buttons = the_settings.getButtonSets().get(targetset);
			//for(SlickButtonData data : buttons) {
			//	Log.e("SERVICE",data.toString());
			//}
		}


		public void addNewButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.setLastSelected(name);
				the_settings.getButtonSets().put(name, new Vector<SlickButtonData>());
				ColorSetSettings def_colorset = new ColorSetSettings();
				def_colorset.toDefautls();
				the_settings.getSetSettings().put(name, def_colorset);
			}
			
		}


		public List<String> getButtonSets() throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				Set<String> keys = the_settings.getAliases().keySet();
				return new ArrayList<String>(keys);
			}
		}


		public int clearButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				int count = the_settings.getButtonSets().get(name).size();
				the_settings.getButtonSets().get(name).removeAllElements();
				return count;
			}
			
		}


		public int deleteButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				int count = the_settings.getButtonSets().get(name).size();
				if(name.equals("default")) {
					//cannot delete default button set, only clear it
					the_settings.getButtonSets().get(name).removeAllElements();
					return count;
				} else {
					
					the_settings.getButtonSets().remove(name);
					return count;
				}
			}
		}


		public Map getButtonSetListInfo() throws RemoteException {
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				return the_settings.getLineSize();
			}
		}


		public int getFontSpaceExtra() throws RemoteException {
			// TODO Auto-generated method stub
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
					if(!path.endsWith(".xml")) {
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
					
					DispatchToast(message);
					//Toast msg = Toast.makeText(StellarService.this.getApplicationContext(), message, Toast.LENGTH_SHORT);
					//msg.show();
				} else {
					//Log.e("SERVICE","COULD NOT WRITE SETTINGS FILE!");
					//Toast msg = Toast.makeText(StellarService.this.getApplicationContext(), "SD Card not available. File not written.", Toast.LENGTH_SHORT);
					//msg.show();
					DispatchToast("SD Card not available. File not written.");
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
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				return the_settings.getSetSettings().get(the_settings.getLastSelected());
			}
		}


		public ColorSetSettings getColorSetDefaultsForSet(String theSet)
				throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				return the_settings.getSetSettings().get(theSet);
			}
		}


		public void setColorSetDefaultsForSet(String theSet,ColorSetSettings input)
				throws RemoteException {
			// TODO Auto-generated method stub
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
				
				//need to go through all the button in the set and update the values.
			}
			
		}


		public void setProcessPeriod(boolean value) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.setProcessPeriod(value);
			}
			
		}


		public Map getTriggerData() throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				return the_settings.getTriggers();
			}
		}


		public void deleteTrigger(String which) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.getTriggers().remove(which);
				
			}
			buildTriggerData();
			
		}


		public void newTrigger(TriggerData data) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.getTriggers().put(data.getPattern(), data);
				
			}
			buildTriggerData();
		}


		public void updateTrigger(TriggerData from, TriggerData to)
				throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.getTriggers().remove(from.getPattern());
				the_settings.getTriggers().put(to.getPattern(), to);
				//for(TriggerResponder responder : to.getResponders()) {
				//	Log.e("SERVICE","MODIFIED TRIGGER, RESPONDER NOW: "+ responder.getFireType().getString());
				//}
			}
			buildTriggerData();
		}


		public TriggerData getTrigger(String pattern) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				//Log.e("SERVICE","REQUESTED TRIGGER " + pattern);
				//for(TriggerResponder responder : the_settings.getTriggers().get(pattern).getResponders()) {
				//	Log.e("SERVICE","REQUESTED TRIGGER RESPONDER " + responder.getType() + " fires " + responder.getFireType());
				//}
				
				return the_settings.getTriggers().get(pattern);
			}
		}


		public boolean getUseExtractUI() throws RemoteException {
			// TODO Auto-generated method stub
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
								doDispatchNoProcess(outputmsg.getBytes());
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
										the_settings.getAliases().remove(alias);
										the_settings.getAliases().put(alias, argument);
									} else {
										//display error message
										String noarg_message = "\n" + Colorizer.colorRed + " Alias \"" + alias + "\" can not be set to nothing. Acceptable format is \"." + alias + " replacetext\"" + Colorizer.colorWhite +"\n";
										try {
											doDispatchNoProcess(noarg_message.getBytes());
										} catch (RemoteException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
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
										doDispatchNoProcess(error.getBytes());
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
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
							doDispatchNoProcess(outputmsg.getBytes());
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
			joined_alias.append("("+(String)a[0]+")");
			for(int i=1;i<a.length;i++) {
				joined_alias.append("|");
				joined_alias.append("("+(String)a[i]+")");
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
	
	private void DispatchToast(String message) {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).showMessage(message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					doDispatchNoProcess(errormessage.getBytes());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return;
			}
			//if we are here we are good to go.
			final int N = callbacks.beginBroadcast();
			for(int i = 0;i<N;i++) {
				try {
					callbacks.getBroadcastItem(i).executeColorDebug(iarg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
				doDispatchNoProcess(success.getBytes());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//notify listeners that data can be read
			}
			callbacks.finishBroadcast();
			
		}
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
	

	
	public void dispatchFinish(String rawData) {
		
		//String htmlText = colorer.htmlColorize(data);
		//Log.e("SERV","MADE SOME HTML:"+htmlText);
		//if(firstDispatch)
		
		
		final int N = callbacks.beginBroadcast();
		int final_count = N;
	
		for(int i = 0;i<N;i++) {
			//callbacks.getBroadcastItem(i).dataIncoming(data);
			//callbacks.getBroadcastItem(i).processedDataIncoming(the_buffer);
			//callbacks.getBroadcastItem(i).htmlDataIncoming(htmlText);
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
		
		//Pattern triger_regex = Pattern.compile(trigger_string.toString());
		//Matcher trigger_matcher = trigger_regex.matcher("");
		//Log.e("SERVICE","ATTEMPTING TO MATCH" + trigger_matcher.pattern().toString() + " on " + regexp_test.toString());
		
		if(has_triggers) {
			
			trigger_matcher.reset(regexp_test);
		
			while(trigger_matcher.find()) {
				//so if we found something here, we triggered.
				//Log.e("SERVICE","TRIGGERPARSE FOUND" + trigger_matcher.group(0));
				TriggerData triggered = the_settings.getTriggers().get(trigger_matcher.group(0));
				if(triggered != null) {
					//shouldn't be
					//Log.e("SERVICE","TRIGGERED:" + triggered.getName());
					//iterate through the responders.
					for(TriggerResponder responder : triggered.getResponders()) {
						responder.doResponse(this, display, trigger_count++,hasListener,myhandler);
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
								for(TriggerResponder responder : data.getResponders()) {
									responder.doResponse(this, display, trigger_count++, hasListener, myhandler);
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
		
		/*Iterator<String> items = test_set.listIterator();
		while(items.hasNext()) {
			
			String test_for = (String)items.next();
			
			
			//Log.e("SERVICE","TESTING FOR:" + test_for + "| agaist:" + regexp_test);
			//test_for.replaceAll(".", "\\.");
			//test_for.replaceAll("\"", "\\\""); //yea bitch
			//test_for.replaceAll("[", substitute)
			//String regex = "^.*"+Pattern.quote(test_for)+".*$";
			//String regex = Pattern.quote(test_for);
			Pattern p = Pattern.compile("^.*"+Pattern.quote(test_for)+".*$",Pattern.DOTALL);
			
			//Boolean matched = Pattern.matches(regex, new String(regexp_test.toString().getBytes("ISO-8859-1"),"ISO-8859-1"));
			//Log.e("SLICK","MATCH: "+regexp_test+"\nAGAINST: " + regex + "\nRETURNED:" + matched);
			Matcher results = p.matcher(regexp_test);
			//Matcher results = p.matcher(regexp_test);
			//results.
			if(results.matches()) {
				//NOTIFICATION: 
				//Log.e("SERVICE","SERVICE MATCHED: " + test_for);
				//build a new notification.
				Notification note = new Notification(R.drawable.btn_plus,"BaardTERM-Pattern Matched",System.currentTimeMillis());
				//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
				
				Context context = getApplicationContext();
				CharSequence contentTitle = "BaardTERM";
				//CharSequence contentText = "Hello World!";
				CharSequence contentText = "Triggerd on: " + test_for;
				Intent notificationIntent = new Intent(this, com.happygoatstudios.bt.window.BaardTERMWindow.class);
				notificationIntent.putExtra("DISPLAY", display);
				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				
				note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				
				note.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
				note.defaults = Notification.DEFAULT_SOUND;
				note.ledARGB = 0xFFFF00FF;
				note.ledOnMS = 300;
				note.ledOffMS = 300;
				
				long[] vp = new long[4];
				vp[0] = 0;
				vp[1] = 200;
				vp[2] = 50;
				vp[3] = 200;
				
				note.vibrate = vp;
				
				mNM.notify(trigger_count++,note);
				
			}
		}*/
		
		regexp_test.setLength(0);
		//the_buffer.append(sstr);
		/*Bundle datal = CountNewLine(new String(the_buffer.toString().getBytes("ISO-8859-1"),"ISO-8859-1"),100);
		if(datal.getInt("TOTAL") > 100) {
			//Log.e("SERV","TIME TO PRUNE ");
			Object[] unusedspans = the_buffer.getSpans(0, datal.getInt("PRUNELOC"), Object.class);
			for(int i=0;i<unusedspans.length;i++) {
				the_buffer.removeSpan(unusedspans[i]);
			}
			Object[] array = the_buffer.getSpans(0,the_buffer.length(),Object.class);
			int size = array.length;
			//Log.e("SERV","FOUND: " + size + " spans.\n");
			the_buffer.replace(0, datal.getInt("PRUNELOC"), "");
			
		}*/
		
		
		
		
		

		
		//callbacks.finishBroadcast();
		
	}
	
	public void doDispatchNoProcess(byte[] data) throws RemoteException{
		
		String rawData = null;
		try {
			rawData = new String(data,"ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			
			e1.printStackTrace();
		}
		
		final int N = callbacks.beginBroadcast();
		int final_count = N;
		

		
		for(int i = 0;i<N;i++) {
			//callbacks.getBroadcastItem(i).dataIncoming(data);
			//callbacks.getBroadcastItem(i).processedDataIncoming(the_buffer);
			//callbacks.getBroadcastItem(i).htmlDataIncoming(htmlText);
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
	
	boolean debug = false;
	
	public void doStartup() throws UnknownHostException, IOException {
		if(host == BAD_HOST || port == BAD_PORT) {
			return; //dont' start 
		}
		
		if(debug) {
			return;
		}
		
		//gotta do this before connecting apparently.
		synchronized(the_settings) {
			if(the_settings.isKeepWifiActive()) {
				EnableWifiKeepAlive();
			}
		}
		
		
		InetAddress addr = null;
		try {
			//InetAddress[] x = InetAddress.getAllByName(host);
			addr = InetAddress.getByName(host);
			//addr = x[0];
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		the_addr = addr;
		if(the_addr == null) {
			//Log.e("SERV","NULL ADDRESS LOOKED UP");
		}
		

		
		
		the_socket = new Socket(addr.getHostAddress(),port);
		
		
		the_socket.setSendBufferSize(1024);
		int size = the_socket.getSendBufferSize();
		boolean shut = the_socket.isOutputShutdown();
		if(shut) {
			//Log.e("SERV","SOCKET OUTPUT IS SHUT DOWN");
		} else {
			//Log.e("SERV","SOCKET OUTPUT ACTIVE, BUFFER SIZE: " + size);
		}
		
		try {
			output_writer = new BufferedOutputStream(the_socket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//at this point we should have a valid socket
		//start up the pump
		//try {
		//	outputter = new OutputWriter(the_socket.getOutputStream());
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		//outputter.start();
		try {
			pump = new DataPumper(the_socket.getInputStream(),null,myhandler);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pump.start();
		
		synchronized(this) {
			try {
				this.wait(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //give the pump some time sto start up
		}
		
		pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_INITXFER);
		
		//show notification
		showNotification();
		
		the_processor = new Processor(myhandler,mBinder);
		the_buffer = new StringBuffer();
		
		//if we are here we should be connected.

		
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
		if(pump != null) {
			pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_END);
			pump = null;
		}
		
		if(output_writer != null) {
			try {
				output_writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		if(the_socket != null) {
			try {
				the_socket.close();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
