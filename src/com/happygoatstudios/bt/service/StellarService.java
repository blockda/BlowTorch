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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Bundle;
import android.provider.Contacts.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.Toast;

import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.service.IStellarServiceCallback;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.HyperSAXParser;
import com.happygoatstudios.bt.settings.HyperSettings;


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
	
	public boolean sending = false;
	
	StringBuffer the_buffer = new StringBuffer();
	String settingslocation = "test_settings2.xml";
	
	public void onCreate() {
		//called when we are created from a startService or bindService call with the IBaardTERMService interface intent.
		//Log.e("SERV","BAARDTERMSERVICE STARTING!");
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		host = BAD_HOST;
		port = BAD_PORT;
		
		//Looper.prepare();
		SharedPreferences prefs = this.getSharedPreferences("SERVICE_INFO", 0);
		settingslocation = prefs.getString("SETTINGS_PATH", "");
		if(settingslocation.equals("")) {
			//Log.e("SERVICE","LAUNCHER FAILED TO PROVIDE SETTINGS PATH");
			return;
		}
		
		loadXmlSettings(settingslocation);
		
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_INIT:
					try {
						doStartup();
					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case MESSAGE_END:
					pump.stop();
					doShutdown();
					break;
				case MESSAGE_PROCESS:
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
				case MESSAGE_SETDATA:
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
					pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_COMPRESS);
					break;
				case MESSAGE_ENDCOMPRESS:
					break;
				case MESSAGE_SENDOPTIONDATA:
					//byte[] obytes = msg.getData().getByteArray("THEDATA");
					byte[] obytes = (byte[])msg.obj;
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
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					
					break;
				case MESSAGE_SENDDATA:
					
					//byte[] bytes = msg.getData().getByteArray("THEDATA");
					byte[] bytes = (byte[]) msg.obj;
					
					//dispatch this for command processing
					String retval = ProcessCommands(new String(bytes));
					if(retval == null) {
						//command was intercepted. do nothing for now and return
						return;
					} else {
						//not a command data.
					}
					//do search and replace with aliases.
					
					
					//convert all alias keys to the big match string.
					//Object[] a = aliases.keySet().toArray();
					

					//search for commands.
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN.
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN.
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN.
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN.
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN.
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
					//TODO:MAKE IT HAPPEN
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
					String nosemidata = tostripsemi.replace(";", crlf);
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
					//dispatch();
					try {
						sendBuffer();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_SAVEBUFFER:
					the_buffer = new StringBuffer(msg.getData().getString("BUFFER") + the_buffer);
					break;
				case MESSAGE_CHECKIFALIVE:
					if(the_socket != null) {
						//the_socket.
						/*if(!the_socket.isConnected() || the_socket.isClosed()) {
						//do shutdown, except now, attempt to launch a dialog.
							Toast.makeText(BaardTERMService.this, "CONNECTION DEAD", 3000);
						}*/
					}
					this.sendEmptyMessageDelayed(StellarService.MESSAGE_CHECKIFALIVE, 3000);
					break;
				default:
					break;	
				}
				
			}
		};
		
		//Log.e("SERV","REACHED THE END OF THE STARTUP METHOD");
		//Looper.loop();
		myhandler.sendEmptyMessageDelayed(StellarService.MESSAGE_CHECKIFALIVE, 3000);
		//REGISTER TRIGGER PATTERS.
		//test_set.add("Your eyes glaze over.");
		test_set.add("QUEST: You may now quest again.");
		test_set.add("i love kurt");
		aliases.put("REPLACE", "gulp");
		aliases.put("TESTREP", "enter");
		
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
		} catch (FileNotFoundException e) {
			//if the file does not exist, then we need to load the default settings
			the_settings.getButtonSets().put("default", new Vector<SlickButtonData>());
			ColorSetSettings def_colorset = new ColorSetSettings();
			def_colorset.toDefautls();
			def_colorset.setPrimaryColor(0xFFFFFFFF);
			the_settings.getSetSettings().put("default", def_colorset);
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
				the_settings.setLastSelected(setname);
				Vector<SlickButtonData> tmp = the_settings.getButtonSets().get(setname);
				if(tmp == null) {
					//Log.e("SERVICE","WINDOW REQUESTED BUTTONSET: " + setname + " but got null");
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


		public void clearButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				the_settings.getButtonSets().get(name).removeAllElements();
			}
			
		}


		public void deleteButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized(the_settings) {
				if(name.equals("default")) {
					//cannot delete default button set, only clear it
					the_settings.getButtonSets().get(name).removeAllElements();
				} else {
					the_settings.getButtonSets().remove(name);
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
					File file = new File(root,path);
					file.createNewFile();
					//Log.e("SERVICE","ATTEMPTING TO WRITE TO FILE: " + file.getPath());
					FileWriter writer = new FileWriter(file);
					BufferedWriter tmp = new BufferedWriter(writer);
					tmp.write(HyperSettings.writeXml2(the_settings));
					tmp.close();
				} else {
					//Log.e("SERVICE","COULD NOT WRITE SETTINGS FILE!");
				}
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		}
		
		public void LoadSettingsFromPath(String path) throws RemoteException {
			synchronized(the_settings) {
				HyperSAXParser loader = new HyperSAXParser(path,StellarService.this);
				the_settings = loader.load();
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
		

	};
	
	Pattern newline = Pattern.compile("\n");
	

	Pattern commandPattern = Pattern.compile("^.(\\w+)\\s+(.+)$");
	Matcher commandMatcher = commandPattern.matcher("");
	
	public String ProcessCommands(String input) {
		String output = null;
		if(input.startsWith(".")) {
			//we have a player
			if(input.startsWith("..")) {
				output = input.replace("..", ".");
				//allow it to go through
			} else {
				//attempt to look up alias name;
				synchronized(the_settings) {
					//string should be of the form .aliasname |settarget can have whitespace|
					commandMatcher.reset(input);
					
					String alias = the_settings.getAliases().get(commandMatcher.group(1));
					String argument = commandMatcher.group(2);
					
					if(alias != null) {
						//real argument
						the_settings.getAliases().remove(alias);
						the_settings.getAliases().put(alias, argument);
					}
				}
			}
			
			
		}
		
		return null;
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
	
	Colorizer colorer = new Colorizer();
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	StringBuffer regexp_test = new StringBuffer();
	Vector<String> test_set = new Vector<String>();
	
	public void dispatch(byte[] data) throws RemoteException, UnsupportedEncodingException {
		
		String rawData = the_processor.RawProcess(data);
		//changing this to send data to the window, then process the triggers.
		
		//Spannable processed = the_processor.DoProcess(data);
		
		
		//String htmlText = colorer.htmlColorize(data);
		//Log.e("SERV","MADE SOME HTML:"+htmlText);
		
		
		
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
		Matcher stripcolor = colordata.matcher(rawData);
		regexp_test.append(stripcolor.replaceAll(""));
		
		//
		//test the de-colorized data against registered patterns.
		Iterator<String> items = test_set.listIterator();
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
		}
		
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
	
	public void doStartup() throws SocketException {
		if(host == BAD_HOST || port == BAD_PORT) {
			return; //dont' start 
		}
		
		if(debug) {
			return;
		}
		
		
		//return; //TODO: WE WILL NEVER START UP;
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
		

		
		try {
			the_socket = new Socket(addr.getHostAddress(),port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	}
	
	private void showNotification() {
		
		
		
		Notification note = new Notification(R.drawable.btn_plus,"BaardTERM",System.currentTimeMillis());
		//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "BaardTERM";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = "Connected: "+ host +":"+ port + ")";
		Intent notificationIntent = new Intent(this, com.happygoatstudios.bt.window.BaardTERMWindow.class);
		notificationIntent.putExtra("DISPLAY", display);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		note.flags = Notification.FLAG_ONGOING_EVENT;
		
		mNM.notify(5545,note);
		
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
		
		
	}
	
	public void doProcess(byte[] data) {
		//broadcast this data.
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

}
