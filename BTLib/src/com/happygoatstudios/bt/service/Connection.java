package com.happygoatstudios.bt.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keplerproject.luajava.LuaException;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.service.function.BellCommand;
import com.happygoatstudios.bt.service.function.ClearButtonCommand;
import com.happygoatstudios.bt.service.function.DirtyExitCommand;
import com.happygoatstudios.bt.service.function.DisconnectCommand;
import com.happygoatstudios.bt.service.function.FullScreenCommand;
import com.happygoatstudios.bt.service.function.FunctionCallbackCommand;
import com.happygoatstudios.bt.service.function.KeyboardCommand;
import com.happygoatstudios.bt.service.function.LoadButtonsCommand;
import com.happygoatstudios.bt.service.function.ReconnectCommand;
import com.happygoatstudios.bt.service.function.SpecialCommand;
import com.happygoatstudios.bt.service.function.ColorDebugCommand;
import com.happygoatstudios.bt.service.function.SpeedwalkCommand;
import com.happygoatstudios.bt.service.function.SwitchWindowCommand;

import com.happygoatstudios.bt.service.plugin.ConnectionSettingsPlugin;
import com.happygoatstudios.bt.service.plugin.Plugin;
import com.happygoatstudios.bt.service.plugin.settings.ConnectionSetttingsParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings.PLUGIN_LOCATION;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.HyperSAXParser;
import com.happygoatstudios.bt.settings.HyperSettings;
import com.happygoatstudios.bt.speedwalk.DirectionData;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.window.TextTree;
import com.happygoatstudios.bt.window.TextTree.Line;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

public class Connection {
	//base "connection class"
	public final static int MESSAGE_STARTUP = 1;
	public final static int MESSAGE_STARTCOMPRESS = 2;
	public final static int MESSAGE_PROCESSORWARNING = 3;
	public final static int MESSAGE_SENDOPTIONDATA = 4;
	public final static int MESSAGE_BELLINC = 5;
	public final static int MESSAGE_DODIALOG = 6;
	public final static int MESSAGE_PROCESS = 7;
	public final static int MESSAGE_DISCONNECTED = 8;
	public static final int MESSAGE_MCCPFATALERROR = 9;
	public final static int MESSAGE_SENDDATA = 9;
	public static final int MESSAGE_LINETOWINDOW = 10;
	public static final int MESSAGE_LUANOTE = 11;
	public static final int MESSAGE_DRAWINDOW = 12;
	public static final int MESSAGE_NEWWINDOW = 13;
	public static final int MESSAGE_MODMAINWINDOW = 14;
	public static final int MESSAGE_WINDOWBUFFER = 15;
	public static final int MESSAGE_ADDFUNCTIONCALLBACK = 16;
	public static final int MESSAGE_WINDOWXCALLS = 17;
	public Handler handler = null;
	ArrayList<Plugin> plugins = null;
	DataPumper pump = null;
	Processor processor = null;
	//TextTree buffer = null;
	TextTree working = null;
	
	String display;
	String host;
	int port;
	
	public StellarService service = null;
	boolean isConnected = false;
	public ConnectionSettingsPlugin the_settings = null;
	
	Character cr = new Character((char)13);
	Character lf = new Character((char)10);
	String crlf = cr.toString() + lf.toString();
	Pattern newline = Pattern.compile("\n");
	Pattern semicolon = Pattern.compile(";");
	Pattern commandPattern = Pattern.compile("^.(\\w+)\\s*(.*)$");
	Matcher commandMatcher = commandPattern.matcher("");
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	StringBuffer joined_alias = new StringBuffer();
	Pattern alias_replace = Pattern.compile(joined_alias.toString());
	Matcher alias_replacer = alias_replace.matcher("");
	Matcher alias_recursive = alias_replace.matcher("");
	Pattern whiteSpace = Pattern.compile("\\s");
	
	public Connection(String display,String host,int port,StellarService service) {
		
		ColorDebugCommand colordebug = new ColorDebugCommand();
		DirtyExitCommand dirtyexit = new DirtyExitCommand();
		//TimerCommand timercmd = new TimerCommand();
		BellCommand bellcmd = new BellCommand();
		FullScreenCommand fscmd = new FullScreenCommand();
		KeyboardCommand kbcmd = new KeyboardCommand();
		DisconnectCommand dccmd = new DisconnectCommand();
		ReconnectCommand rccmd = new ReconnectCommand();
		SpeedwalkCommand swcmd = new SpeedwalkCommand();
		LoadButtonsCommand lbcmd = new LoadButtonsCommand();
		ClearButtonCommand cbcmd = new ClearButtonCommand();
		//DumpGMCPCommand dmpcmd = new DumpGMCPCommand();
		//LuaCommand luacmd = new LuaCommand();
		//Lua2Command lua2cmd = new Lua2Command();
		specialcommands.put(colordebug.commandName, colordebug);
		specialcommands.put(dirtyexit.commandName, dirtyexit);
		//specialcommands.put(timercmd.commandName, timercmd);
		specialcommands.put(bellcmd.commandName, bellcmd);
		specialcommands.put(fscmd.commandName, fscmd);
		specialcommands.put(kbcmd.commandName, kbcmd);
		specialcommands.put("kb", kbcmd);
		specialcommands.put(dccmd.commandName, dccmd);
		specialcommands.put(rccmd.commandName, rccmd);
		specialcommands.put(swcmd.commandName, swcmd);
		specialcommands.put(lbcmd.commandName, lbcmd);
		specialcommands.put(cbcmd.commandName, cbcmd);
		//specialcommands.put(dmpcmd.commandName,dmpcmd);
		//specialcommands.put(luacmd.commandName, luacmd);
		
		//specialcommands.put(lua2cmd.commandName,lua2cmd);
		SwitchWindowCommand swdcmd = new SwitchWindowCommand();
		specialcommands.put(swdcmd.commandName, swdcmd);
		
		this.display = display;
		this.host = host;
		this.port = port;
		this.service = service;
		
		plugins = new ArrayList<Plugin>();
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_WINDOWXCALLS:
					//Bundle b = msg.getData();
					Object o = msg.obj;
					String token = msg.getData().getString("TOKEN");
					String function = msg.getData().getString("FUNCTION");
					try {
						Connection.this.windowXCallS(token,function,o);
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					break;
				case MESSAGE_ADDFUNCTIONCALLBACK:
					Bundle data = msg.getData();
					String id = data.getString("ID");
					String command = data.getString("COMMAND");
					String callback = data.getString("CALLBACK");
					int pid = -1;
					//Plugin pTarget = null;
					for(int i=0;i<plugins.size();i++) {//p : plugins) {
						Plugin p = plugins.get(i);
						if(p.getName().equals(id)) {
							pid = i;
						}
					}
					if(pid != -1) {
						FunctionCallbackCommand fcc = new FunctionCallbackCommand(pid,command,callback);
						specialcommands.put(fcc.commandName, fcc);
					} else {
						//error.
					}
					break;
				case MESSAGE_WINDOWBUFFER:
					boolean set = (msg.arg1 == 0) ? false : true;
					//Debug.waitForDebugger();
					
					String name = (String)msg.obj;
					Log.e("PLUGIN","TRING ACTUALLY MODDING WINDOW("+name+") Buffer:"+set);
					
					for(WindowToken tok : mWindows) {
						if(tok.getName().equals(name)) {
							Log.e("PLUGIN","ACTUALLY MODDING WINDOW("+name+") Buffer:"+set);
							tok.setBufferText(set);
						}
					}
					break;
				case MESSAGE_MODMAINWINDOW:
					WindowToken main = mWindows.get(0);
					Bundle c = msg.getData();
					main.setX(c.getInt("X"));
					main.setY(c.getInt("Y"));
					main.setWidth(c.getInt("WIDTH"));
					main.setHeight(c.getInt("HEIGHT"));
					break;
				case MESSAGE_NEWWINDOW:
					WindowToken tok = (WindowToken)msg.obj;
					mWindows.add(tok);
					break;
				case MESSAGE_DRAWINDOW:
					Connection.this.redrawWindow((String)msg.obj);
					break;
				case MESSAGE_LUANOTE:
					String str = (String)msg.obj;
					try {
						dispatchNoProcess(str.getBytes(the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case MESSAGE_LINETOWINDOW:
					TextTree.Line line = (TextTree.Line)msg.obj;
					String target = msg.getData().getString("TARGET");
					Connection.this.lineToWindow(target,line);
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
							
							if(pump.isConnected()) {
								//output_writer.write(tosend);
								//output_writer.flush();
								pump.sendData(tosend);
								//pump.handler.sendMessage(datasend);
							} else {
								sendBytesToWindow(new String(Colorizer.colorRed + "\nDisconnected.\n" + Colorizer.colorWhite).getBytes("UTF-8"));
							}
						} else {
							if(d.cmdString.equals("") && d.visString == null) {
								pump.sendData(crlf.getBytes(the_settings.getEncoding()));
								//pump.handler.sendMessage(datasend);
								//output_writer.flush();
								d.visString = "\n";
							}
						}
						//send the transformed data back to the window
						if(d.visString != null && !d.visString.equals("")) {
							if(the_settings.isLocalEcho()) {
								//preserve.
								//buffer_tree.addBytesImplSimple(data)
								sendBytesToWindow(d.visString.getBytes(the_settings.getEncoding()));
							}
						}
					} catch (IOException e) {
						//throw new RuntimeException(e);
						this.sendEmptyMessage(MESSAGE_DISCONNECTED);
					}
					break;
				case MESSAGE_STARTUP:
					doStartup();
					break;
				case MESSAGE_STARTCOMPRESS:
					pump.handler.sendMessage(pump.handler.obtainMessage(DataPumper.MESSAGE_COMPRESS,msg.obj));
					break;
				case MESSAGE_SENDOPTIONDATA:
					Bundle b = msg.getData();
					byte[] obytes = b.getByteArray("THE_DATA");
					String message = b.getString("DEBUG_MESSAGE");
					if(message != null) {
						sendDataToWindow(message);
					}
					
					//try {
						if(pump != null) {
							pump.sendData(obytes);
							//output_writer.flush();
						}
					//} catch (IOException e2) {
					//	throw new RuntimeException(e2);
					//}
					break;
				case MESSAGE_PROCESSORWARNING:
					sendDataToWindow((String)msg.obj);
					break;
				case MESSAGE_BELLINC:
					//TODO: use settings;
					/*if(the_settings.isVibrateOnBell()) {
						doVibrateBell();
					}
					if(the_settings.isNotifyOnBell()) {
						doNotifyBell();
					}
					if(the_settings.isDisplayOnBell()) {
						doDisplayBell();
					}*/
					break;
				case MESSAGE_DODIALOG:
					DispatchDialog((String)msg.obj);
					break;
				case MESSAGE_PROCESS:
					try {
						dispatch((byte[])msg.obj);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_DISCONNECTED:
					killNetThreads();
					DoDisconnect(null);
					isConnected = false;
					break;
				default:
					break;
				}
			}
		};

		//}
		//load plugins.
		
		//TODO: load plugins.
		
		//buffer = new TextTree();
		working = new TextTree();
		
		//TODO: set TextTree encoding options.
		
		//handler.sendEmptyMessage(MESSAGE_STARTUP);
		//TODO: initializie main window.
		mWindows = new ArrayList<WindowToken>();
		
		//WindowToken token = new WindowToken(MAIN_WINDOW,0,177,880,500);
		WindowToken token = new WindowToken(MAIN_WINDOW,0,0,0,0);
		mWindows.add(token);
		
		/*WindowToken add = new WindowToken("chats",0,0,1280,177);
		try {
			add.getBuffer().addBytesImpl("Omfg\nWe\nHAVE\nMINIWINDOW CHATTING OMFG OMGONGONGONGONGNGNG GN YEA YEAYEA\nAttempting to get lots of text for scrolling. This is a sentence\nOMG MOAR TEXT\nNEW NEW NEW\nMORE MORE MORE\nNOW NOW NOW".getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		mWindows.add(add);
		
		WindowToken mapwin = new WindowToken("map_window",880,177,400,400);
		mapwin.setBufferText(true);
		mWindows.add(mapwin);
		
		WindowToken luawin = new WindowToken("lua_window",880,577,400,100,"windowscript","plugin");
		
		mWindows.add(luawin);
		
		WindowToken bwin = new WindowToken("button_window",0,0,0,0,"buttonwindow","plugin");
		mWindows.add(bwin);*/
		loadPlugins();
		
		
		
	}
	
	protected void windowXCallS(String token, String function, Object o) throws RemoteException {
		int N = mWindowCallbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
			String name = c.getName();
			if(c.getName().equals(token)) {
				c.xcallS(function,(String)o);
				i=N;
			}
		}
		
		mWindowCallbacks.finishBroadcast();
	}

	public void reloadSettings() {
		//unhook all windows.
		//while(mWindowCallbacks.)
		int N = mWindowCallbacks.beginBroadcast();
		
		for(int i=0;i<N;i++) {
			IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
			
			try {
				if(!c.getName().equals(MAIN_WINDOW)) {
					c.shutdown();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		mWindowCallbacks.finishBroadcast();
		
		//loadSettings();
		loadPlugins();
		
	}
	
	private void loadPlugins() {
		
		if(mWindows.size() > 1) {
			//must clear out old windows.
			while(mWindows.size() > 1) {
				mWindows.remove(mWindows.size()-1);
			}
		} //else {
		WindowToken token = mWindows.get(0);
		token.setX(0);
		token.setY(0);
		token.setWidth(0);
		token.setHeight(0);
		//}
		for(Plugin p : plugins) {
			p.shutdown();
			p = null;
		}
		plugins.clear();
		//handle root settings
		//try {
			the_settings = null;
			//the_settings = new ConnectionSettingsPlugin(handler);
		//} catch (LuaException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
			
		ArrayList<Plugin> tmpPlugs = new ArrayList<Plugin>();
		Pattern invalidchars = Pattern.compile("\\W");
		Matcher replacebadchars = invalidchars.matcher(this.display);
		String prefsname = replacebadchars.replaceAll("");
		prefsname = prefsname.replaceAll("/", "");
		//String settingslocation = 
		//loadXmlSettings(prefsname +".xml");
		String rootPath = prefsname + ".xml";
		String convertPath = prefsname + ".v1.xml";
		String newPath = prefsname + ".v2.xml";
		//rootPath is the v1 settings file name.
		String internal = service.getApplicationContext().getApplicationInfo().dataDir + "/files/";
		File oldp = new File(internal+rootPath);
		HyperSettings oldSettings = null;
		if(oldp.exists()) {
			HyperSAXParser old_parser = new HyperSAXParser(rootPath,service.getApplicationContext());
			try {
				oldSettings = old_parser.load();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			oldp.renameTo(new File(internal+convertPath));
		}
		
		//check to see if the new settings file exists.
		File newSettings = new File(internal+newPath);
		if(!newSettings.exists()) { //if they have niether and old version or a new one.
			
			//copy the defaults file to the new location and parse it.
			try {
				newSettings.createNewFile();
				int resid = Connection.getResId("default_settings", service.getApplicationContext(), com.happygoatstudios.bt.R.raw.class);
				InputStream defaultSettings = service.getResources().openRawResource(resid);
				
				OutputStream newSettingsFile = new FileOutputStream(newSettings);
			
				int read = 0;
				byte[] bytes = new byte[1024];
			 
				while ((read = defaultSettings.read(bytes)) != -1) {
					newSettingsFile.write(bytes, 0, read);
				}
				
				defaultSettings.close();
				newSettingsFile.flush();
				newSettingsFile.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		
		ConnectionSetttingsParser csp = new ConnectionSetttingsParser(newPath,service.getApplicationContext(),tmpPlugs,handler);
		
		tmpPlugs = csp.load();
		the_settings = (ConnectionSettingsPlugin) tmpPlugs.get(0);
		tmpPlugs.remove(0);
		
		if(oldSettings != null) {
			//import old settings.
			the_settings.importV1Settings(oldSettings);
			
			//have to rip out seperate settings into the mainwindow token.
			WindowToken main = mWindows.get(0);
			//oldSettings.
			
			//PULL WINDOW RELATED OPTIONS OUT!!
			oldSettings.isDisableColor();
			oldSettings.isHyperLinkEnabled();
			oldSettings.isWordWrap();
			oldSettings.getFontName();
			oldSettings.getBreakAmount();
			oldSettings.getFontPath();
			oldSettings.getMaxLines();
			oldSettings.getWrapMode();
			oldSettings.getLineSpaceExtra();
			oldSettings.getLineSize();
			
			
		}
		
		plugins.addAll(tmpPlugs);
		
		for(Plugin p : plugins) {
			pluginMap.put(p.getName(), p);
		}
		
		//private void loadDefaultDirections() {
		if(the_settings.getDirections().size() == 0) {
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
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			for(String link : the_settings.getLinks()) {
				
				String filename = Environment.getExternalStorageDirectory() + "/BlowTorch/" + link;
				Log.e("XML","Attempting to load plugins from:" + filename);
				PluginParser parse = new PluginParser(filename,link,service.getApplicationContext(),plugins,handler);
				
				try {
					ArrayList<Plugin> group = parse.load();
					for(Plugin p : group) {
						pluginMap.put(p.getName(), p);
						if(linkMap.get(link) == null) {
							ArrayList<String> vals = new ArrayList<String>();
							vals.add(p.getName());
							linkMap.put(link, vals);
						} else {
							ArrayList<String> vals = linkMap.get(link);
							vals.add(p.getName());
						}
					}
					plugins.addAll(group);
					
					//tmpPlug.setSettings(parse.load());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//plugins.
		//tmpPlug.initScripts(mWindows);
		//tmpPlug.initScripts();
		
		//plugins.add(tmpPlug);
	}
	
	private HashMap<String,ArrayList<String>> linkMap = new HashMap<String,ArrayList<String>>();
	private HashMap<String,Plugin> pluginMap = new HashMap<String,Plugin>(0);
	
	protected void redrawWindow(String win) {
		Log.e("WINDOW","SERVICE ATTEMPTING TO REDRAW WINDOW:" + win);
		int N = mWindowCallbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				if(w.getName().equals(win)) {
					w.redraw();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mWindowCallbacks.finishBroadcast();
	}

	
	protected void lineToWindow(String target, Line line) {
		for(WindowToken w : mWindows) {
			if(w.getName().equals(target)) {
				TextTree tmp = new TextTree();
				tmp.appendLine(line);
				tmp.updateMetrics();
				byte[] lol = tmp.dumpToBytes(false);
				
				try {
					w.getBuffer().addBytesImpl(lol);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				int N = mWindowCallbacks.beginBroadcast();
				for(int i = 0;i<N;i++) {
					IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
					try {
						if(target.equals(c.getName())) {
							c.rawDataIncoming(lol);
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mWindowCallbacks.finishBroadcast();
			}
		}
	}

	private int mCallbackCount = 0;
	public void registerWindowCallback(String name,IWindowCallback callback) {
		mWindowCallbacks.register(callback);
		//int i = mWindowCallbacks.beginBroadcast();
		//mWindowCallbacks.finishBroadcast();
		//mWindowCallbackMap.put(name, i);
		//mCallbackCount++;
	}
	
	public void unregisterWindowCallback(String name, IWindowCallback callback) {
		mWindowCallbacks.unregister(callback);
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i = 0;i<N;i++) {
		//	IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
		//	String tmp = c.getName();
			
		//}
	}
	
	private final String MAIN_WINDOW = "mainDisplay";
	private String outputWindow = MAIN_WINDOW;
	
	protected void DoDisconnect(Object object) {
		//TODO: if window showing, show disconnection.
	}

	protected void killNetThreads() {
		if(pump != null) {
			pump.handler.sendEmptyMessage(DataPumper.MESSAGE_END);
			pump = null;
		}
	}
	
	private void dispatchNoProcess(byte[] data) {
		sendBytesToWindow(data);
	}

	private void dispatch(byte[] data) throws UnsupportedEncodingException {
		byte[] raw = processor.RawProcess(data);
		if(raw == null) return;
		
		TextTree buffer = null;
		for(WindowToken w : mWindows) {
			if(w.getName().equals(outputWindow)) {
				buffer = w.getBuffer();
			}
		}
		
		
		working.setBleedColor(buffer.getBleedColor());
		working.addBytesImpl(raw);
		for(Plugin p : plugins) {
			p.process(working, service, true, handler, display);
			if(working.getLines().size() == 0) {
				return;
			}
			working.updateMetrics();
		}
		
		byte[] proc = working.dumpToBytes(false);
		synchronized(buffer) {
			buffer.addBytesImplSimple(proc);
		}
		
		sendBytesToWindow(proc);
		
		
	}
	
	protected void DispatchDialog(String str) {
		//service.DispatchDialog(str);
	}

	public void sendDataToWindow(String message) {
		
		try {
			sendBytesToWindow(message.getBytes(the_settings.getEncoding()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendBytesToWindow(byte[] data) {
		//service.sendRawDataToWindow(data);
		/*if(activated) {
			service.sendRawDataToWindow(data);
		}*/
		int N = mWindowCallbacks.beginBroadcast();
		
		try {
			//Integer i = mWindowCallbackMap.get(outputWindow);
			//if(i != null) {
			for(int i=0;i<N;i++) {
				IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
				if(c.getName().equals(outputWindow)) {
					c.rawDataIncoming(data);
				}
			}
			//}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mWindowCallbacks.finishBroadcast();
		
	}

	private void doStartup() {
		if(pump != null) return; //already started up.
		//int tmpPort = 0;
		//String host = "";
		//String display = "";
		//loadConnectionData();
		
		pump = new DataPumper(host,port,handler);
		pump.start();
		
		processor = new Processor(handler,the_settings.getEncoding(),service.getApplicationContext(),null);
		
		//show notification somehow.
		isConnected = true;
		
	}
	
	/*private void loadConnectionData() {
		int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				port = callbacks.getBroadcastItem(i).getPort();
				host = callbacks.getBroadcastItem(i).getHost();
				display = callbacks.getBroadcastItem(i).getDisplay();
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//host = callbacks.getBroadcastItem(i))
		}
		callbacks.finishBroadcast();
	}*/
	
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
						Boolean reprocess = true;
						byte[] tmp = DoAliasReplacement(d.cmdString.getBytes(the_settings.getEncoding()),reprocess);
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
							if(reprocess) {
								for(int ax=0;ax<alias_cmds.length;ax++) {
									iterator.previous();
								}
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
	
	public class Data {
		public String cmdString;
		public String visString;
		public Data() {
			cmdString = "";
			visString = "";
		}
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
					sendBytesToWindow(outputmsg.getBytes(the_settings.getEncoding()));
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
						
						
						if(the_settings.getSettings().getAliases().containsKey(alias)) {
							//real argument
							if(!argument.equals("")) {
								AliasData mod = the_settings.getSettings().getAliases().remove(alias);
								mod.setPost(argument);
								the_settings.getSettings().getAliases().put(alias, mod);
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
									sendBytesToWindow(noarg_message.getBytes(the_settings.getEncoding()));
								} catch (UnsupportedEncodingException e) {
									throw new RuntimeException(e);
								}
								return null;
							}
						} else if(specialcommands.containsKey(alias)){
							//Log.e("SERVICE","SERVICE FOUND SPECIAL COMMAND: " + alias);
							SpecialCommand command = specialcommands.get(alias);
							data = (Data) command.execute(argument,this);
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
								sendBytesToWindow(error.getBytes(the_settings.getEncoding()));
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(service);
			//boolean setvalue = prefs.getBoolean("PROCESS_PERIOD", true);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("PROCESS_PERIOD", the_settings.isProcessPeriod());
			editor.commit();
			
			//Log.e("SERVICE","SET PROCESS PERIOD FROM:" + setvalue + " to " + value);
			
		}
	}
	
	public byte[] DoAliasReplacement(byte[] input,Boolean reprocess) {
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
				
				AliasData replace_with = the_settings.getSettings().getAliases().get(alias_replacer.group(0));
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
				boolean eatTail = false;
				do {
					recursivefound = false;
					
					//Matcher recursivematch = to_replace.matcher(replaced.toString());
					alias_recursive.reset(replaced.toString());
					buffertemp.setLength(0);
					while(alias_recursive.find()) {
						recursivefound = true;
						AliasData replace_with = the_settings.getSettings().getAliases().get(alias_recursive.group(0));
						if(replace_with.getPre().startsWith("^") && ! replace_with.getPre().endsWith("$")) {
							ToastResponder r = new ToastResponder();
							String[] tParts = null;
							
							String tmpInput = replaced.toString();
							int index = tmpInput.indexOf(";");
							String rest = "";
							if(index > -1) {
								rest = tmpInput.substring(index+1,tmpInput.length());
								tmpInput = tmpInput.substring(0,index);
							}
							String sepchar = "";
							if(rest.length()>0) {
								sepchar = ";";
							}
							tParts = whiteSpace.split(tmpInput);
							
							HashMap<String,String> map = new HashMap<String,String>();
							for(int i=0;i<tParts.length;i++) {
								map.put(Integer.toString(i), tParts[i]);
							} 
							eatTail = true;
							alias_recursive.appendReplacement(buffertemp, r.translate(replace_with.getPost(),map) + sepchar +rest);
							reprocess = false;
						} else {
							alias_recursive.appendReplacement(buffertemp, replace_with.getPost());
						}
						
					}
					if(recursivefound) {
						if(!eatTail) {
							alias_recursive.appendTail(buffertemp);
						}
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
	

	private void buildAliases() {
		joined_alias.setLength(0);
		
		//Object[] a = the_settings.getAliases().keySet().toArray();
		Object[] a = the_settings.getSettings().getAliases().values().toArray();
		
		
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
	
	//public RemoteCallbackList<IConnectionBinderCallback> callbacks = new RemoteCallbackList<IConnectionBinderCallback>();
	private RemoteCallbackList<IWindowCallback> mWindowCallbacks = new RemoteCallbackList<IWindowCallback>();
	//private HashMap<String,Integer> mWindowCallbackMap = new HashMap<String,Integer>();
	
	public void switchTo(String connection) {
		//
		service.switchTo(connection);
//		if(isWindowConnected()) {
//			int N = callbacks.beginBroadcast();
//			for(int i =0;i<N;i++) {
//				try {
//					callbacks.getBroadcastItem(i).switchTo(connection);
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			callbacks.finishBroadcast();
//		}
	}

	private boolean isWindowConnected() {
		return service.isWindowConnected();
	}
//		boolean showing = false;
//		int N = callbacks.beginBroadcast();
//		for(int i =0;i<N;i++) {
//			try {
//				showing = callbacks.getBroadcastItem(i).isWindowShowing();
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(showing) {
//				break;
//			}
//		}
//		callbacks.finishBroadcast();
//		return showing;
//	}

	public void deactivate() {
		// TODO Auto-generated method stub
		activated = false;
	}

	public void activate() {
		// TODO Auto-generated method stub
		activated = true;
	}
	
	boolean activated = false;
	
	ArrayList<WindowToken> mWindows;
	
	public List<WindowToken> getWindows() {
		// TODO Auto-generated method stub
		return mWindows;
	}

	public String getScript(String plugin, String name) {
		for(Plugin p : plugins) {
			if(p.getSettings().getName().equals(plugin)) {
				if(p.getSettings().getScripts().containsKey(name)) {
					return p.getSettings().getScripts().get(name);
				} else {
					return "";
				}
			}
		}
		return "";
	}

	public void executeFunctionCallback(int id, String callback, String args) {
		Plugin p = plugins.get(id);
		p.execute(callback,args);
		//Plugin p = 
	}

	public void pluginXcallS(String plugin, String function, String str) {
		for(Plugin p : plugins) {
			if(p.getName().equals(plugin)) {
				p.xcallS(function,str);
			}
		}
	}
	
	public void saveSettings(String filename)  {
		try {
			FileOutputStream fos = service.openFileOutput(filename,Context.MODE_PRIVATE);
			
			XmlSerializer out = Xml.newSerializer();
			
			StringWriter writer = new StringWriter();
			
			out.setOutput(writer);
			
			out.startDocument("UTF-8", true);
			out.startTag("", "root");
			
			//fill in from plugins.
			the_settings.outputXMLInternal(out);
			
			out.startTag("", "plugins");
			for(Plugin p : plugins) {
				//if(p.getSettings()Connection.)
				if(p.getSettings().getLocationType() == PLUGIN_LOCATION.INTERNAL) {
					p.outputXMLInternal(out);
				//} else {
				//	p.outputXMLExternal(service);
				}
				
			}
			
			out.endTag("", "plugins");
			out.endTag("","root");
			out.endDocument();
			
			fos.write(writer.toString().getBytes());
			
			fos.close();
			
			
			//output links.
			ArrayList<String> links = the_settings.getLinks();
			for(String link : links) {
				//start the dump:
				FileOutputStream linkOS = service.openFileOutput(link, Context.MODE_PRIVATE);
				
				XmlSerializer linkOut = Xml.newSerializer();
				
				StringWriter linkWriter = new StringWriter();
				
				linkOut.setOutput(linkWriter);
				
				linkOut.startDocument("UTF-8", true);
				
				linkOut.startTag("", "root");
				linkOut.startTag("", "plugins");

				//dumpPluginCommonData(out);
				ArrayList<String> vals = linkMap.get(link);
				for(String pKey : vals) {
					Plugin p = pluginMap.get(pKey);
					p.outputXMLExternal(service,linkOut);
				}
				
				linkOut.endTag("", "plugins");
				linkOut.endTag("","root");
				//linkOut.endTag("", "plugin");
				linkOut.endDocument();
				
				linkOS.write(writer.toString().getBytes());
				
				linkOS.close();
				
				
				
				
			}
			
			

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public static int getResId(String variableName, Context context, Class<?> c) {

	    try {
	        Field idField = c.getDeclaredField(variableName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } 
	}
	
	
}
