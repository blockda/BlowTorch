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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.IteratorModifiedException;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.gag.GagAction;
import com.happygoatstudios.bt.responder.script.ScriptResponder;
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
import com.happygoatstudios.bt.service.plugin.settings.BaseOption;
import com.happygoatstudios.bt.service.plugin.settings.BooleanOption;
import com.happygoatstudios.bt.service.plugin.settings.ColorOption;
import com.happygoatstudios.bt.service.plugin.settings.ConnectionSetttingsParser;
import com.happygoatstudios.bt.service.plugin.settings.EncodingOption;
import com.happygoatstudios.bt.service.plugin.settings.FileOption;
import com.happygoatstudios.bt.service.plugin.settings.IntegerOption;
import com.happygoatstudios.bt.service.plugin.settings.ListOption;
import com.happygoatstudios.bt.service.plugin.settings.PluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings.PLUGIN_LOCATION;
import com.happygoatstudios.bt.service.plugin.settings.SettingsGroup;
import com.happygoatstudios.bt.service.plugin.settings.VersionProbeParser;
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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class Connection implements SettingsChangedListener {
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
	public final static int MESSAGE_SENDDATA_BYTES = 9;
	public static final int MESSAGE_LINETOWINDOW = 10;
	public static final int MESSAGE_LUANOTE = 11;
	public static final int MESSAGE_DRAWINDOW = 12;
	public static final int MESSAGE_NEWWINDOW = 13;
	//public static final int MESSAGE_MODMAINWINDOW = 14;
	public static final int MESSAGE_WINDOWBUFFER = 15;
	public static final int MESSAGE_ADDFUNCTIONCALLBACK = 16;
	public static final int MESSAGE_WINDOWXCALLS = 17;
	public static final int MESSAGE_INVALIDATEWINDOWTEXT = 18;
	public static final int MESSAGE_GMCPTRIGGERED = 19;
	public static final int MESSAGE_SENDDATA_STRING = 20;
	public static final int MESSAGE_SAVESETTINGS = 21;
	public static final int MESSAGE_EXPORTFILE = 22;
	public static final int MESSAGE_IMPORTFILE = 23;
	public static final int MESSAGE_SENDGMCPDATA = 24;
	public Handler handler = null;
	ArrayList<Plugin> plugins = null;
	private HashMap<String,String> captureMap = new HashMap<String,String>();
	DataPumper pump = null;
	Processor processor = null;
	//TextTree buffer = null;
	TextTree working = null;
	TextTree finished = null;
	boolean loaded = false;
	String display;
	String host;
	int port;
	HashMap<String,IWindowCallback> windowCallbackMap = new HashMap<String,IWindowCallback>();
	//String encoding = "ISO-8859-1";
	
	public StellarService service = null;
	boolean isConnected = false;
	public ConnectionSettingsPlugin the_settings = null;
	KeyboardCommand keyboardCommand;
	Character cr = new Character((char)13);
	Character lf = new Character((char)10);
	String crlf = cr.toString() + lf.toString();
	Pattern newline = Pattern.compile("\n");
	Pattern semicolon = Pattern.compile(";");
	Pattern commandPattern = Pattern.compile("^.(\\w+)\\s*(.*)$");
	Matcher commandMatcher = commandPattern.matcher("");
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	//StringBuffer joined_alias = new StringBuffer();
	//Pattern alias_replace = Pattern.compile(joined_alias.toString());
	//Matcher alias_replacer = alias_replace.matcher("");
	//Matcher alias_recursive = alias_replace.matcher("");
	//Pattern whiteSpace = Pattern.compile("\\s");
	
	public Object callbackSync = new Object();
	
	public Connection(String display,String host,int port,StellarService service) {
		
		ColorDebugCommand colordebug = new ColorDebugCommand();
		DirtyExitCommand dirtyexit = new DirtyExitCommand();
		//TimerCommand timercmd = new TimerCommand();
		BellCommand bellcmd = new BellCommand();
		FullScreenCommand fscmd = new FullScreenCommand();
		keyboardCommand = new KeyboardCommand();
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
		specialcommands.put(keyboardCommand.commandName, keyboardCommand);
		specialcommands.put("kb", keyboardCommand);
		specialcommands.put(dccmd.commandName, dccmd);
		specialcommands.put(rccmd.commandName, rccmd);
		specialcommands.put(swcmd.commandName, swcmd);
		specialcommands.put(lbcmd.commandName, lbcmd);
		specialcommands.put(cbcmd.commandName, cbcmd);
		ExportFunction expcmd = new ExportFunction();
		specialcommands.put(expcmd.commandName, expcmd);
		ImportFunction impcmd = new ImportFunction();
		specialcommands.put(impcmd.commandName, impcmd);
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
				case MESSAGE_EXPORTFILE:
					exportSettings((String)msg.obj);
					break;
				case MESSAGE_IMPORTFILE:
					Connection.this.service.markWindowsDirty();
					importSettings((String)msg.obj);
					break;
				case MESSAGE_SAVESETTINGS:
					String changedplugin = (String)msg.obj;
					Connection.this.saveDirtyPlugin(changedplugin);
					break;
				case MESSAGE_GMCPTRIGGERED:
					String plugin = msg.getData().getString("TARGET");
					String gcallback = msg.getData().getString("CALLBACK");
					HashMap<String,Object> gdata = (HashMap<String,Object>)msg.obj;
					Plugin gp = pluginMap.get(plugin);
					gp.handleGMCPCallback(gcallback,gdata);
					break;
				case MESSAGE_INVALIDATEWINDOWTEXT:
					String wname = (String)msg.obj;
					try {
						doInvalidateWindowText(wname);
					} catch (RemoteException e4) {
						// TODO Auto-generated catch block
						e4.printStackTrace();
					}
					break;
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
				case MESSAGE_SENDDATA_STRING:
					try {
						byte[] bytes = ((String)msg.obj).getBytes(the_settings.getEncoding());
						sendToServer(bytes);
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case MESSAGE_SENDDATA_BYTES:
					sendToServer((byte[])msg.obj);
					break;
				case MESSAGE_SENDGMCPDATA:
					Log.e("COnn","CONNECTION SENDING GMCP:"+((String)msg.obj));
					//construct the gmcp packet.
					char IAC = 0xFF;
					char SB = 0xFA;
					char SE = 0xF0;
					char DO = 0xFD;
					char GMCP = (char) TC.GMCP;
					String pkt = IAC + SB+GMCP + ((String)msg.obj) + IAC + SE;
					if(pump != null && pump.isConnected()) {
						try {
							pump.sendData(pkt.getBytes("ISO-8859-1"));
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						this.sendMessageDelayed(this.obtainMessage(MESSAGE_SENDGMCPDATA,msg.obj), 500);
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
		working.setLinkify(false);
		working.setLineBreakAt(10000000);
		
		finished = new TextTree();
		finished.setLinkify(false);
		finished.setLineBreakAt(10000000);
		//TODO: set TextTree encoding options.
		
		//handler.sendEmptyMessage(MESSAGE_STARTUP);
		//TODO: initializie main window.
		mWindows = new ArrayList<WindowToken>();
		
		//WindowToken token = new WindowToken(MAIN_WINDOW,0,177,880,500);
		//if(the_settings.)

		
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
		loadInternalSettings();
		
		loaded = true;
		
		//fish out the window.

	}
	
	protected void saveDirtyPlugin(String changedplugin) {
		if(changedplugin.equals("")) {
			saveMainSettings();
		} else {
			Plugin p = pluginMap.get(changedplugin);
			if(p != null) {
				if(p.getStorageType().equals("INTERNAL")) {
					saveMainSettings();
				}
			}
		}
	}

	protected void doInvalidateWindowText(String name) throws RemoteException {
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		IWindowCallback callback = windowCallbackMap.get(name);
		//for(int i=0;i<N;i++) {
		//	IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
			//String tname = c.getName();
			//if(c.getName().equals(name)) {
				//WindowToken tok = 
				//c.invalidateWindowText();
				//c.rawDataIncoming(raw)
			//	callback = c;
			//}
		//}
		
		WindowToken w = null;
		for(int i=0;i<mWindows.size();i++) {
			WindowToken tmp = mWindows.get(i);
			if(tmp.getName().equals(name)) {
				w = tmp;
			}
		}
		
		callback.clearText();
		callback.rawDataIncoming(w.getBuffer().dumpToBytes(true));
		
		//mWindowCallbacks.finishBroadcast();
		//}
	}

	protected void windowXCallS(String token, String function, Object o) throws RemoteException {
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i=0;i<N;i++) {
			IWindowCallback c = windowCallbackMap.get(token);
		//	String name = c.getName();
		//	if(c.getName().equals(token)) {
				c.xcallS(function,(String)o);
		//		i=N;
		//	}
		//}
		
		//mWindowCallbacks.finishBroadcast();
		//}
	}

	public void reloadSettings() {
		//unhook all windows.
		//while(mWindowCallbacks.)
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		
		for(IWindowCallback c : windowCallbackMap.values()) {
			//IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
			
			try {
				///if(!c.getName().equals(MAIN_WINDOW)) {
					c.shutdown();
				//}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//mWindowCallbacks.finishBroadcast();
		
		//}
		//loadSettings();
		service.markWindowsDirty();
		loadInternalSettings();
		
	}
	
	private void shutdownPlugins() {
		for(Plugin p : plugins) {
			p.shutdown();
			p = null;
		}
		plugins.clear();
	}
	
	private void loadPlugins(ArrayList<Plugin> tmpPlugs) {
		
		HashMap<String,TextTree> bufferSaves = new HashMap<String,TextTree>();
		
		if(mWindows.size() > 0) {
			//must clear out old windows.
			while(mWindows.size() > 0) {
				WindowToken t = mWindows.remove(mWindows.size()-1);
				bufferSaves.put(t.getName(), t.getBuffer());
			}
		} //else {
		/*if(mWindows.size() > 0) {
			WindowToken token = mWindows.get(0);
			
			token.layouts.clear();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			LayoutGroup g = new LayoutGroup();
			g.type = LayoutGroup.LAYOUT_TYPE.normal;
			g.setLandscapeParams(params);
			g.setPortraitParams(params);
		}*/
		//}

		//handle root settings
		//try {
		if(the_settings!=null) {
			the_settings.shutdown();
			
		}
			the_settings = null;
			//the_settings = new ConnectionSettingsPlugin(handler);
		//} catch (LuaException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
			
//		ArrayList<Plugin> tmpPlugs = new ArrayList<Plugin>();
//		Pattern invalidchars = Pattern.compile("\\W");
//		Matcher replacebadchars = invalidchars.matcher(this.display);
//		String prefsname = replacebadchars.replaceAll("");
//		prefsname = prefsname.replaceAll("/", "");
//		//String settingslocation = 
//		//loadXmlSettings(prefsname +".xml");
//		String rootPath = prefsname + ".xml";
//		String convertPath = prefsname + ".v1.xml";
//		String newPath = prefsname + ".v2.xml";
//		String internal = service.getApplicationContext().getApplicationInfo().dataDir + "/files/";
//		File oldp = new File(internal+rootPath);
//		HyperSettings oldSettings = null;
//		if(oldp.exists()) {
//			HyperSAXParser old_parser = new HyperSAXParser(rootPath,service.getApplicationContext());
//			try {
//				oldSettings = old_parser.load();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//			
//			oldp.renameTo(new File(internal+convertPath));
//		}
//		
//		File newSettings = new File(internal+newPath);
//		if(!newSettings.exists()) { //if they have niether and old version or a new one.
//			
//			try {
//				newSettings.createNewFile();
//				int resid = Connection.getResId("default_settings", service.getApplicationContext(), com.happygoatstudios.bt.R.raw.class);
//				InputStream defaultSettings = service.getResources().openRawResource(resid);
//				
//				OutputStream newSettingsFile = new FileOutputStream(newSettings);
//			
//				int read = 0;
//				byte[] bytes = new byte[1024];
//			 
//				while ((read = defaultSettings.read(bytes)) != -1) {
//					newSettingsFile.write(bytes, 0, read);
//				}
//				
//				defaultSettings.close();
//				newSettingsFile.flush();
//				newSettingsFile.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} 
//		
//		
//		ConnectionSetttingsParser csp = new ConnectionSetttingsParser(newPath,service.getApplicationContext(),tmpPlugs,handler,this);
//		
//		tmpPlugs = csp.load(this);

//		
//		if(oldSettings != null) {
//			the_settings.importV1Settings(oldSettings);
//			
//			WindowToken main = mWindows.get(0);
//
//			oldSettings.isDisableColor();
//			oldSettings.isHyperLinkEnabled();
//			oldSettings.isWordWrap();
//			oldSettings.getFontName();
//			oldSettings.getBreakAmount();
//			oldSettings.getFontPath();
//			oldSettings.getMaxLines();
//			oldSettings.getWrapMode();
//			oldSettings.getLineSpaceExtra();
//			oldSettings.getLineSize();
//			
//			
//		}
			
		the_settings = (ConnectionSettingsPlugin) tmpPlugs.get(0);
		the_settings.sortTriggers();
		mWindows.add(0,the_settings.getSettings().getWindows().get(MAIN_WINDOW));

		tmpPlugs.remove(0);
		
		plugins.addAll(tmpPlugs);
		
		
		for(Plugin p : plugins) {
			pluginMap.put(p.getName(), p);
			p.sortTriggers();
			if(p.getSettings().getWindows().size() > 0) {
				mWindows.addAll(p.getSettings().getWindows().values());
			}
		}
		
		for(WindowToken w : mWindows) {
			if(bufferSaves.get(w.getName()) != null) {
				w.setBuffer(bufferSaves.get(w.getName()));
			}
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
				ArrayList<Plugin> tmplist = new ArrayList<Plugin>();
				PluginParser parse = new PluginParser(filename,link,service.getApplicationContext(),tmplist,handler,this);
				
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
						
						if(p.getSettings().getWindows().size() > 0) {
							mWindows.addAll(p.getSettings().getWindows().values());
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
		
		//so now that we have all the plugins, we need to build up the processor's gmcpTriggerTables.
		//loop through all the plugins, looking for literal triggers starting
		//with the gmcpTriggerChar.
		
		
		//plugins.
		//tmpPlug.initScripts(mWindows);
		//tmpPlug.initScripts();
		
		//plugins.add(tmpPlug);
		buildSettingsPage();
		
		//build and sort the new trigger array.
		//the_settings.buildTriggerSystem();
		//for(Plugin p : plugins) {
		//	p.buildTriggerSystem();
		//}
		buildTriggerSystem();
		service.reloadWindows();
		
	}
	

	
	public void buildTriggerSystem() {
		//start with the global settings.
		long start = System.currentTimeMillis();
		sortedTriggerMap = new HashMap<Integer,TriggerData>();
		triggerPluginMap = new HashMap<Integer,Plugin>();
		int currentgroup = 1;
		triggerBuilder.setLength(0);
		boolean addseparator = false;
		ArrayList<TriggerData> tmp = the_settings.getSortedTriggers();
		if(tmp == null) {
			the_settings.sortTriggers();
			tmp = the_settings.getSortedTriggers();
			//if(tmp == null || tmp.size() == 0) return;
		}
		if(tmp != null && tmp.size() > 0) {
			for(int i=0;i<tmp.size();i++) {
				TriggerData t = tmp.get(i);
				if(!t.isInterpretAsRegex() && t.getPattern().startsWith("%")) {
					
				} else {
					if(t.isEnabled()) {
						if(i == 0) {
							triggerBuilder.append("(");
							triggerBuilder.append(t.getPattern());
							triggerBuilder.append(")");
							addseparator = true;
						} else {
							triggerBuilder.append("|(");
							triggerBuilder.append(t.getPattern());
							triggerBuilder.append(")");
						}
						sortedTriggerMap.put(currentgroup, t);
						triggerPluginMap.put(currentgroup, the_settings);
						currentgroup += t.getMatcher().groupCount()+1;
					}
				}
			}
		}
		
		for(Plugin p : plugins) {
			//boolean addseparator = false;
			tmp = p.getSortedTriggers();
			if(tmp == null) {
				p.sortTriggers();
				tmp = p.getSortedTriggers();
				//if(tmp == null || tmp.size() == 0) return;
			}
			if(tmp != null && tmp.size() > 0) {
				for(int i=0;i<tmp.size();i++) {
					TriggerData t = tmp.get(i);
					if(!t.isInterpretAsRegex() && t.getPattern().startsWith("%")) {
						
					} else {
						if(t.isEnabled()) {
							if(i == 0 && addseparator == false) {
								triggerBuilder.append("(");
								triggerBuilder.append(t.getPattern());
								triggerBuilder.append(")");
								addseparator = true;
							} else {
								triggerBuilder.append("|(");
								triggerBuilder.append(t.getPattern());
								triggerBuilder.append(")");
							}
							sortedTriggerMap.put(currentgroup, t);
							triggerPluginMap.put(currentgroup, p);
							currentgroup += t.getMatcher().groupCount()+1;
						}
					}
				}
			}
		
		}
		
		massiveTriggerString = triggerBuilder.toString();
		
		massivePattern = Pattern.compile(massiveTriggerString,Pattern.MULTILINE);
		//massiveTriggerString = massiveTriggerString.replace("|", "\n");
		//Log.e("MASSIVE",massiveTriggerString);
		massiveMatcher = massivePattern.matcher("");
		
		long delta = System.currentTimeMillis() - start;
		Log.e("TRIGGERS","TIMEPROFILE trigger system took " + delta + " millis to build.");
	}
	
	private HashMap<String,ArrayList<String>> linkMap = new HashMap<String,ArrayList<String>>();
	private HashMap<String,Plugin> pluginMap = new HashMap<String,Plugin>(0);
	
	protected void redrawWindow(String win) {
		//Log.e("WINDOW","SERVICE ATTEMPTING TO REDRAW WINDOW:" + win);
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i=0;i<N;i++) {
			IWindowCallback w = windowCallbackMap.get(win);
			try {
				//if(w.getName().equals(win)) {
					w.redraw();
				//}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
		//mWindowCallbacks.finishBroadcast();
		//}
	}

	
	protected void lineToWindow(String target, Line line) {
		//synchronized(callbackSync) {
		
		for(WindowToken w : mWindows) {
			if(w.getName().equals(target)) {
				TextTree tmp = new TextTree();
				tmp.setEncoding(the_settings.getEncoding());
				tmp.appendLine(line);
				tmp.updateMetrics();
				byte[] lol = tmp.dumpToBytes(false);
				
				try {
					w.getBuffer().addBytesImpl(lol);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//int N = mWindowCallbacks.beginBroadcast();
				//for(int i = 0;i<N;i++) {
					IWindowCallback c = windowCallbackMap.get(target);
					try {
						//if(target.equals(c.getName())) {
							c.rawDataIncoming(lol);
						//}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//}
				//mWindowCallbacks.finishBroadcast();
			}
		}
		//}
	}

	private int mCallbackCount = 0;
	boolean callbacksStarted = false;
	public void registerWindowCallback(String name,IWindowCallback callback) {
		if(callbacksStarted) {
			mWindowCallbacks.finishBroadcast();
		}
		
		mWindowCallbacks.register(callback);
		
		int N = mWindowCallbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				windowCallbackMap.put(w.getName(), w);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		callbacksStarted = true;
		//so now the callback list is open, and we have the map of all the callbacks associated by name.
		
		//int i = mWindowCallbacks.beginBroadcast();
		//mWindowCallbacks.finishBroadcast();
		//mWindowCallbackMap.put(name, i);
		//mCallbackCount++;
	}
	
	public void unregisterWindowCallback(String name, IWindowCallback callback) {
		
		if(callbacksStarted) {
			mWindowCallbacks.finishBroadcast();
		}
		mWindowCallbacks.unregister(callback);
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i = 0;i<N;i++) {
		//	IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
		//	String tmp = c.getName();
		int N = mWindowCallbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				windowCallbackMap.put(w.getName(), w);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
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

	Pattern colordata = Pattern.compile("\\x1B\\x5B.+?m");
	Matcher colorStripper = colordata.matcher("");
	
	Pattern linedata = Pattern.compile("^.*$",Pattern.MULTILINE);
	Matcher lineMatcher = linedata.matcher("");
	
	private class Range {
		private int start;
		private int end;
		private int line;
		public Range(int start,int end,int line) { this.start = start; this.end = end; this.line = line;}
		public int getLine() { return line;}
		public int getStart() { return start;}
		public int getEnd() { return end; }
		
	}
	
	private class RangeComparator implements Comparator<Range> {

		@Override
		public int compare(Range a, Range b) {
			// TODO Auto-generated method stub
			if(b.start > a.end && b.end > a.end) {
				return -1;
			}

			if(b.start < a.start && b.end < a.end) {
				return 1;
			}

			return 0;
		}

	}
	
	TreeSet<Range> lineMap = new TreeSet<Range>(new RangeComparator());
	StringBuilder triggerBuilder = new StringBuilder();
	String massiveTriggerString = null;
	Pattern massivePattern = null;
	Matcher massiveMatcher = null;
	HashMap<Integer,TriggerData> sortedTriggerMap = null;
	HashMap<Integer,Plugin> triggerPluginMap = null;
	HashMap<Integer,Integer> lineStartMap = new HashMap<Integer,Integer>();
	private void dispatch(byte[] data) throws UnsupportedEncodingException {
		long start = System.currentTimeMillis();
		
		
		byte[] raw = processor.RawProcess(data);
		if(raw == null) return;
		
		//long deltaraw = System.currentTimeMillis() - start;
		//.e("PARSING","TIMEPROFILE trigger raw processing took " + deltaraw);
		
		//long treestart = System.currentTimeMillis();
		TextTree buffer = null;
		for(WindowToken w : mWindows) {
			if(w.getName().equals(outputWindow)) {
				buffer = w.getBuffer();
			}
		}
		//long deltatree = System.currentTimeMillis() - treestart;
		//Log.e("PARSING","TIMEPROFILE finding the right window took " + deltatree);
		
		
//		for(int i=finished.getLines().size()-1;i>=0;i--) {
//			Line tmpl = finished.getLines().get(i);
//			Log.e("FIN","FINISHED:" + TextTree.deColorLine(tmpl));
//			
//		}
//		
//		for(int i=working.getLines().size()-1;i>=0;i--) {
//			Line tmpl = working.getLines().get(i);
//			Log.e("FIN","WORKING:" + TextTree.deColorLine(tmpl));
//			
//		}
		//long bleedstart = System.currentTimeMillis();
		TextTree.Color tmpcolor = buffer.getBleedColor();
		working.setBleedColor(tmpcolor);
		finished.setBleedColor(tmpcolor);
		//long deltableed = System.currentTimeMillis() - bleedstart;
		//Log.e("PARSING","TIMEPROFILE finding the bleed color took " + deltableed);
		//long addstart = System.currentTimeMillis();
		working.addBytesImpl(raw);
		//long deltadelta = System.currentTimeMillis() - addstart;
		//Log.e("PARSING","TIMEPROFILE adding to the working buffer took " + deltadelta);
		//long deltatmp2 = System.currentTimeMillis() - start;
		//Log.e("PARSING","TIMEPROFILE triggers did " + deltatmp2 + " of necessary prep.");
		
		//long firstpart = System.currentTimeMillis();
		
		//strip the color out.
		colorStripper.reset(new String(raw,the_settings.getEncoding()));
		String stripped = colorStripper.replaceAll("");
		
		//Log.e("LOG","TRIGGER TESTING:\n"+stripped);
		//build up the newline map.
		

		
		ListIterator<TextTree.Line> it = working.getLines().listIterator(working.getLines().size());
		lineMap.clear();
		lineMatcher.reset(stripped);
		boolean found = false;
		int lineNumber = working.getLines().size()-1;
		while(lineMatcher.find()) {
			found = true;
			
			//Log.e("LINE MAP","MAPPING POSITIONS: {" + lineMatcher.start() + ":" + lineMatcher.end() + "} to line:" + lineNumber + " + data:" + lineMatcher.group());
			lineMap.add(new Range(lineMatcher.start(),lineMatcher.end(),lineNumber));
			lineNumber = lineNumber -1;
		}
		boolean keepEvaluating = true;
		lineNumber = working.getLines().size()-1;
		Line l = null;
		if(it.hasPrevious()) {
			l = it.previous();
		} else {
			return;
		}
		
		//long deltatmp = System.currentTimeMillis() - firstpart;
		//Log.e("DELTA","TIMEPROFILE trigger prep work took " + deltatmp + " millis.");
		if(found) {
			boolean done = false;
			while(!done) {
				done = true;
				boolean rebuildTriggers = false;
				int linedelta = 0;
				//attempt the trigger matching.
				massiveMatcher.reset(stripped);
				while(keepEvaluating && massiveMatcher.find()) {
					int s = massiveMatcher.start();
					int e = massiveMatcher.end()-1;
					String matched = massiveMatcher.group();
					Range r = new Range(s,e,0);
					SortedSet<Range> tmp = lineMap.tailSet(r);
	
					int tmpline = tmp.first().getLine();
					int tmpstart = s - tmp.first().getStart();
					int tmpend = (e-1) - tmp.first().getStart();
	

					
					int index = -1;
					for(int i=1;i<=massiveMatcher.groupCount();i++) {
						if(massiveMatcher.group(i) != null) {
							index = i;
							i=massiveMatcher.groupCount();
						}
					}
					
					if(index > 0) {
						

						//we have found a trigger.
						//advance the line number to
						int linesize = working.getLines().size();
						
						TriggerData t = sortedTriggerMap.get(index);
						Plugin p = triggerPluginMap.get(index);
//						if(t.getName().equals("map_capture_end") || t.getName().equals("map_capture")) {
//							Log.e("Parse","Debug Me");
//						}
						//Log.e("TRIGGER","trigger matched:" + t.getName() + " :"+massiveMatcher.group());
						//String matched3 = massiveMatcher.group();
						if(lineNumber >= tmpline) {
							int amount = (lineNumber - tmpline);
							for(int i=0;i<amount;i++) {
								l = it.previous();
							}
							if(it.hasNext()) {
								it.next();
								lineNumber = tmpline;
							} else {
							//	Log.e("TRIGGER","DEBUG ME");
								lineNumber = working.getLines().size()-1;
								
							}
							
						}
						//Log.e("MATCHED","MATCHED TRIGGER:" + matched3 + " : " + t.getName());
						if(t != null && t.isEnabled()) {
							captureMap.clear();
							for(int i=index;i<=(t.getMatcher().groupCount()+index);i++) {
								
								captureMap.put(Integer.toString(i-index), massiveMatcher.group(i));
							}
							for(TriggerResponder responder : t.getResponders()) {
								try {
									boolean ret = responder.doResponse(service.getApplicationContext(), working, lineNumber, it, l, tmpstart,tmpend,matched, t, display, StellarService.getNotificationId(), true, handler, captureMap, p.getLuaState(), t.getName(), the_settings.getEncoding());
									if(ret == true) {
										keepEvaluating = false;
										rebuildTriggers = true;
										//this signals to rebuild the trigger system.
										
									}
									//TriggerResponder r = new GagAction();

								} catch(IteratorModifiedException e1) {
									it = e1.getIterator();
									int now = working.getLines().size();
									lineNumber = it.nextIndex();
									linedelta = now - linesize;
									//int linesize = 
									//if(responder instanceof GagAction) {
										//gagged, working tree now contains.
//									for(int i=working.getLines().size()-1;i>=0;i--) {
//										
//										String lff = TextTree.deColorLine(working.getLines().get(i)).toString();
//										Log.e("GAG","GAG("+i+"):"+lff);
//										//lf = lf-1;
//									}
									
									//}
								}
								if(working.getLines().size() == 0) {
									keepEvaluating = false;
								}
							}
						}
					}
					if(rebuildTriggers) {
						break;
					}
				}
				if(rebuildTriggers) {
					done = false;
					keepEvaluating = true;
					//get the triggering sequence start and end.
					int s = massiveMatcher.start();
					int e = massiveMatcher.end();
					
//					if(e > stripped.length()-1) {
//						Log.e("debug","dededededebug.");
//						Log.e("debug","massive:" + massiveMatcher.group());
//						Log.e("debug","stripped:" + stripped.length() + " matchstart:" + s + "matchend:" + e + "\n" + stripped);
//					}
					//make the stripped text be a substring of what is currently matched.
					if(e == stripped.length()) {
					} else {
						stripped = stripped.substring(e+1,stripped.length());
					}
					
					if(lineNumber <= working.getLines().size()-1) {
						while(working.getLines().size()-1 >= lineNumber) {
//							Log.e("GAG","-------------------------------------------");
//							//while(sf.hasPrevious()) {
//							for(int i=working.getLines().size()-1;i>=0;i--) {
//								
//								String lff = TextTree.deColorLine(working.getLines().get(i)).toString();
//								Log.e("GAG","PRUNEPRE("+i+"):"+lff);
//								//lf = lf-1;
//							}
							Line tmp = working.getLines().get(working.getLines().size()-1);
							working.getLines().remove(working.getLines().size()-1);
							String tmpstr = TextTree.deColorLine(tmp).toString();
							//Log.e("PARSE","removed from working tree:" + tmpstr);
							finished.appendLine(tmp);
							
							//ListIterator<TextTree.Line> sf = buffer.getLines().listIterator(buffer.getLines().size());
							//int lf = working.getLines().size()-1;
//							Log.e("GAG","-------------------------------------------");
//							//while(sf.hasPrevious()) {
//							for(int i=finished.getLines().size()-1;i>=0;i--) {
//								
//								String lff = TextTree.deColorLine(finished.getLines().get(i)).toString();
//								Log.e("GAG","PRUNEPOST("+i+"):"+lff);
//								//lf = lf-1;
//							}
						}
						
					}
					
//					for(int i=working.getLines().size()-1;i>=0;i--) {
//					
//					String lff = TextTree.deColorLine(working.getLines().get(i)).toString();
//					Log.e("GAG","PRUNEPRE("+i+"):"+lff);
//					//lf = lf-1;
//					}
					
					buildTriggerSystem();
					
					lineMap.clear();
					lineMatcher.reset(stripped);
					found = false;
//					lineNumber = working.getLines().size()-1;
//					ListIterator<TextTree.Line> tmpit = working.getLines().listIterator(lineNumber+1);
//					while(tmpit.hasPrevious()) {
//						String str = TextTree.deColorLine(tmpit.previous()).toString();
//						Log.e("WORKING","WORKING TREE("+lineNumber+"):"+str);
//						lineNumber = lineNumber -1;
//					}
					lineNumber = working.getLines().size()-1;
					while(lineMatcher.find()) {
						found = true;
						
						//Log.e("LINE MAP","MAPPING POSITIONS: {" + lineMatcher.start() + ":" + lineMatcher.end() + "} to line:" + lineNumber + " + data:" + lineMatcher.group());
						lineMap.add(new Range(lineMatcher.start(),lineMatcher.end(),lineNumber));
						lineNumber = lineNumber -1;
					}
					
					lineNumber = working.getLines().size()-1;
					if(lineNumber == -1) {
						keepEvaluating = false;
						done = true;
					} else {
						it = working.getLines().listIterator(lineNumber+1);
						l = it.previous();
					}
					
				}
				
				
			}
		}
		
		
//OLD OLD OLD OLD OLD
//		the_settings.process(working, service, true, handler, display);
//		if(working.getLines().size() == 0) {
//			return;
//		}
//		working.updateMetrics();
//		
//		for(Plugin p : plugins) {
//			p.process(working, service, true, handler, display);
//			if(working.getLines().size() == 0) {
//				return;
//			}
//			working.updateMetrics();
//		}

//		ListIterator<TextTree.Line> it = working.getLines().listIterator(working.getLines().size());
//		boolean keepEvaluating = true;
//		int lineNum = working.getLines().size()-1;
//		while(it.hasPrevious()) {
//			Line l = it.previous();
//			String stripped = TextTree.deColorLine(l).toString();
//			try {
//				the_settings.process2(l,stripped,lineNum, working, service, true, handler, display);
//			} catch (IteratorModifiedException e) {
//				it = e.getIterator();
//			}
//			if(working.getLines().size() == 0) {
//				return;
//			}
//			working.updateMetrics();
//			
//			for(Plugin p : plugins) {
//				try {
//					p.process2(l,stripped,lineNum,working, service, true, handler, display);
//				} catch (IteratorModifiedException e) {
//					it = e.getIterator();
//				}
//				if(working.getLines().size() == 0) {
//					return;
//				}
//				working.updateMetrics();
//			}
//			lineNum = lineNum -1;
//		}

		
//NEW BUT BAD NEW BUT BAD
//		ListIterator<TextTree.Line> it = working.getLines().listIterator(working.getLines().size());
//		boolean keepEvaluating = true;
//		int lineNum = working.getLines().size();
//		while(it.hasPrevious() && keepEvaluating) {
//			
//			TextTree.Line l = it.previous();
//			
//			lineNum = lineNum -1;
//			
//			String str = TextTree.deColorLine(l).toString();
//			
//			massiveMatcher.reset(str);
//			
//			while(massiveMatcher.find() && keepEvaluating) {
//				//determine which group fired.
//				//boolean done = false;
//				int index = -1;
//				for(int i=1;i<=massiveMatcher.groupCount();i++) {
//					if(massiveMatcher.group(i) != null) {
//						index = i;
//						i=massiveMatcher.groupCount();
//					}
//				}
//				
//				if(index > 0) {
//					//we have found a trigger.
//					TriggerData t = sortedTriggerMap.get(index);
//					Plugin p = triggerPluginMap.get(index);
//					
//					if(t.isEnabled()) {
//						captureMap.clear();
//						for(int i=index;i<(t.getMatcher().groupCount()+index);i++) {
//							captureMap.put(Integer.toString(i-index), massiveMatcher.group(i));
//						}
//						for(TriggerResponder responder : t.getResponders()) {
//							try {
//								responder.doResponse(service.getApplicationContext(), working, lineNum, it, l, t.getMatcher(), t, display, StellarService.getNotificationId(), true, handler, captureMap, p.getLuaState(), t.getName(), the_settings.getEncoding());
//								
//							} catch(IteratorModifiedException e) {
//								it = e.getIterator();
//							}
//							if(working.getLines().size() == 0) {
//								keepEvaluating = false;
//							}
//						}
//					}
//				}
//			}
//			
//		}
		ListIterator<TextTree.Line> finisher = working.getLines().listIterator(working.getLines().size());
		while(finisher.hasPrevious()) {
			finished.appendLine(finisher.previous());
		}
		
		working.empty();
		finished.updateMetrics();
		
		//ListIterator<TextTree.Line> finalt = finished.getLines().listIterator(finished.getLines().size());
		//while(finalt.hasPrevious()) {
		//	buffer.appendLine(finalt.previous());
		//}
		
		byte[] proc = finished.dumpToBytes(false);
		
		
//		for(int i=finished.getLines().size()-1;i>=0;i--) {
//			Line tmpl = finished.getLines().get(i);
//			Log.e("FIN","FINISHED:" + TextTree.deColorLine(tmpl));
//			
//		}
//		
//		for(int i=working.getLines().size()-1;i>=0;i--) {
//			Line tmpl = working.getLines().get(i);
//			Log.e("FIN","WORKING:" + TextTree.deColorLine(tmpl));
//			
//		}
		
		//synchronized(buffer) {
		buffer.addBytesImplSimple(proc);
		//}
		
		
		long now = System.currentTimeMillis();
		Log.e("Connection","TIMEPROFILE trigger parsing took:" + Long.toString(now - start) + " millis.");
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
		
		long start = System.currentTimeMillis();
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		
		try {
			//Integer i = mWindowCallbackMap.get(outputWindow);
			//if(i != null) {
			//for(int i=0;i<N;i++) {
				IWindowCallback c = windowCallbackMap.get(outputWindow);
		//		if(c.getName().equals(outputWindow)) {
					c.rawDataIncoming(data);
		//		}
		//	}
			//}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		
		//mWindowCallbacks.finishBroadcast();
		//}
		
		long now = System.currentTimeMillis();
		Log.e("Connection","TIMEPROFILE sendBytesToWindow:" + Long.toString(now -start) + " millis.");
		
	}

	private void doStartup() {
		if(pump != null) return; //already started up.
		//int tmpPort = 0;
		//String host = "";
		//String display = "";
		//loadConnectionData();
		
		pump = new DataPumper(host,port,handler);
		pump.start();
		
		/*if(processor != null) {
			processor.reset();
			processor = null;
		}*/
		processor = new Processor(handler,the_settings.getEncoding(),service.getApplicationContext());
		
		loadGMCPTriggers();
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
	
	private void loadGMCPTriggers() {
		String gmcpChar = the_settings.getGMCPTriggerChar();
		for(int i=0;i<plugins.size();i++) {
			Plugin p = plugins.get(i);
			HashMap<String,TriggerData> triggers = p.getSettings().getTriggers();
			for(TriggerData t : triggers.values()) {
				if(!t.isInterpretAsRegex()) { //this actually means literal
					if(t.getPattern().startsWith(gmcpChar)) {
						//add it to the watch list, if it has a script responder
						for(TriggerResponder r : t.getResponders()) {
							if(r instanceof ScriptResponder) {
								ScriptResponder s = (ScriptResponder)r;
								String callback = s.getFunction();
								String module = t.getPattern().substring(1,t.getPattern().length());
								String name = p.getName();
								processor.addWatcher(module, name, callback);
							}
						}
					}
				}
			}
		}
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
						boolean didReplace = false;
						byte[] tmp = null;
						for(int i=0;i<plugins.size()+1;i++) {
							Plugin p = null;
							if(i ==0) {
								p = the_settings;
							} else {
								p = plugins.get(i-1);
							}
							if(p.getSettings().getAliases().size() > 0) {
								Boolean reprocess = true;
								tmp = p.doAliasReplacement(d.cmdString.getBytes(the_settings.getEncoding()),reprocess);
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
									didReplace = true;
									i=plugins.size();
								}
							}
						}
							
						if(didReplace) {

						} else {
							if(tmp != null) {
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
							} else {
								dataToServer.append(d.cmdString + crlf);
								dataToWindow.append(d.cmdString);
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
			if(!d.visString.endsWith(crlf)) {
				d.visString = d.visString + crlf;
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
	
	public WindowToken[] getWindows() {
		if(loaded) {
			WindowToken[] tmp = new WindowToken[mWindows.size()];
			tmp = mWindows.toArray(tmp);
			return tmp;
		} else {
			return null;
		}
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
		
		//if we are here, then it means the main window callback has attempted to load a script.
		//if(the_settings.getSettings().getName().equals(plugin)) {
			if(the_settings.getSettings().getScripts().containsKey(name)) {
				return the_settings.getSettings().getScripts().get(name);
			} else {
				return "";
			}
			
		//}
		//return "";
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

	public WindowToken getWindowByName(String desired) {
		for(int i=0;i<mWindows.size();i++) {
			WindowToken t = mWindows.get(i);
			if(t.getName().equals(desired)) {
				return t;
			}
		}
		return null;
	}

	public HashMap<String, TriggerData> getTriggers() {
		// TODO Auto-generated method stub
		return the_settings.getSettings().getTriggers();
	}

	public Map getPluginTriggers(String id) {
		// TODO Auto-generated method stub
		Plugin p = pluginMap.get(id);
		if(p != null) {
			return p.getSettings().getTriggers();
		} else {
			return null;
		}
	}

	public void addTrigger(TriggerData data) {
		the_settings.addTrigger(data);
	}

	public void updateTrigger(TriggerData from, TriggerData to) {
		the_settings.updateTrigger(from,to);
	}

	public void updatePluginTrigger(String selectedPlugin, TriggerData from,
			TriggerData to) {
		Plugin p = pluginMap.get(selectedPlugin);
		if(p != null) {
			p.updateTrigger(from,to);
		}
	}

	public void newPluginTrigger(String selectedPlugin, TriggerData data) {
		Plugin p = pluginMap.get(selectedPlugin);
		if(p != null) {
			p.addTrigger(data);
		}
	}

	public TriggerData getPluginTrigger(String selectedPlugin, String pattern) {
		Plugin p = pluginMap.get(selectedPlugin);
		if(p != null) {
			return p.getSettings().getTriggers().get(pattern);
		} else {
			return null;
		}
	}

	public TriggerData getTrigger(String pattern) {
		// TODO Auto-generated method stub
		return the_settings.getSettings().getTriggers().get(pattern);
	}

	public void setPluginTriggerEnabled(String selectedPlugin, boolean enabled,
			String key) {
		Plugin p = pluginMap.get(selectedPlugin);
		if(p != null) {
			TriggerData data = p.getSettings().getTriggers().get(key);
			if(data != null) {
				data.setEnabled(enabled);
			}
		}
	}
	
	public void setTriggerEnabled(boolean enabled,String key) {
		TriggerData data = the_settings.getSettings().getTriggers().get(key);
		if(data != null) {
			data.setEnabled(true);
		}
	}

	public void deletePluginTrigger(String selectedPlugin, String which) {
		Plugin p = pluginMap.get(selectedPlugin);
		if(p != null) {
			p.getSettings().getTriggers().remove(which);
			p.sortTriggers();
		}
	}

	public void deleteTrigger(String which) {
		the_settings.getSettings().getTriggers().remove(which);
		the_settings.sortTriggers();
	}

	public void setAliases(Map map) {
		the_settings.getSettings().setAliases((HashMap<String,AliasData>)map);
		the_settings.buildAliases();
	}
	
	public void setPluginAliases(String plugin,Map map) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.getSettings().setAliases((HashMap<String,AliasData>)map);
			p.buildAliases();
		}
	}
	
	public AliasData getPluginAlias(String plugin,String key) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			return p.getSettings().getAliases().get(key);
		}
		return null;
	}
	
	public AliasData getAlias(String key) {
		return the_settings.getSettings().getAliases().get(key);
	}
	
	public void deleteAlias(String key) {
		the_settings.getSettings().getAliases().remove(key);
	}
	
	public void deletePluginAlias(String plugin,String key) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.getSettings().getAliases().remove(key);
		}
	}
	
	public Map getAliases() {
		return the_settings.getSettings().getAliases();
	}
	
	public Map getPluginAliases(String plugin) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			return p.getSettings().getAliases();
		} else {
			return null;
		}
	}
	
	public List getSystemCommands() {
		List<String> list = new ArrayList<String>();
		Set<String> keys = specialcommands.keySet();
		for(String key : keys) {
			list.add(key);
		}
		return list;
	}

	public void setPluginAliasEnabled(String plugin, boolean enabled, String key) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			AliasData data = p.getSettings().getAliases().get(key);
			if(data != null) {
				data.setEnabled(enabled);
				p.buildAliases();
			}
		}
	}

	public void setAliasEnabled(boolean enabled, String key) {
		AliasData data = the_settings.getSettings().getAliases().get(key);
		if(data != null) {
			data.setEnabled(enabled);
			the_settings.buildAliases();
		}
	}

	public byte[] doKeyboardAliasReplace(byte[] bytes, Boolean reprocess) {
		int count = plugins.size();
		byte res[] = null;
		for(int i = 0;i<count;i++) {
			Plugin p = plugins.get(i);
			byte tmp[] = p.doAliasReplacement(bytes, reprocess);
			if(tmp.length != bytes.length) {
				return tmp;
			} else {
				boolean same = true;
				for(int j=0;j<tmp.length;j++) {
					if(tmp[j] != bytes[j]) {
						same = false;
						j=tmp.length;
					}
				}
				if(!same) {
					return tmp;
				}
			}
		}
		
		return bytes;
	}

	public void doReconnect() {
		if(pump != null) {
			pump.handler.sendEmptyMessage(DataPumper.MESSAGE_END);
			pump = null;
		}
		
		doStartup();
	}

	public void deletePluginTimer(String plugin, String name) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.getSettings().getTimers().remove(name);
		}
	}

	public TimerData getTimer(String name) {
		// TODO Auto-generated method stub
		return the_settings.getSettings().getTimers().get(name);
	}

	public void deleteTimer(String name) {
		the_settings.getSettings().getTimers().remove(name);
	}

	public TimerData getPluginTimer(String plugin, String name) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			
			return p.getSettings().getTimers().get(name);
		} else {
			return null;
		}
	}

	public void addPluginTimer(String plugin, TimerData newtimer) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			newtimer.setRemainingTime(newtimer.getSeconds());
			p.getSettings().getTimers().put(newtimer.getName(), newtimer);
		}
	}

	public void updatePluginTimer(String plugin, TimerData old,
		TimerData newtimer) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.getSettings().getTimers().remove(old.getName());				
			p.getSettings().getTimers().put(newtimer.getName(), newtimer);
		}
		
	}

	public void updateTimer(TimerData old, TimerData newtimer) {
		the_settings.getSettings().getTimers().remove(old.getName());
		the_settings.getSettings().getTimers().put(newtimer.getName(),newtimer);
	}

	public Map getTimers() {
		// TODO Auto-generated method stub
		the_settings.updateTimerProgress();
		return the_settings.getSettings().getTimers();
	}

	public Map getPluginTimers(String plugin) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.updateTimerProgress();
			return p.getSettings().getTimers();
		} else {
			return null;
		}
	}

	public void addTimer(TimerData newtimer) {
		newtimer.setRemainingTime(newtimer.getSeconds());
		the_settings.getSettings().getTimers().put(newtimer.getName(), newtimer);
	}

	public boolean isWindowShowing() {
		// TODO Auto-generated method stub
		return service.isWindowConnected();
	}
	
	public String getDisplayName() {
		return display;
	}
	
	public Context getContext() {
		return service.getApplicationContext();
	}
	
	public void playTimer(String key) {
		the_settings.startTimer(key);
	}
	
	public void playPluginTimer(String plugin,String timer) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.startTimer(timer);
		}
	}
	
	public void pauseTimer(String key) {
		the_settings.pauseTimer(key);
	}
	
	public void pausePluginTimer(String plugin,String timer) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.pauseTimer(timer);
		}
	}
	
	public void stopTimer(String key) {
		the_settings.stopTimer(key);
	}
	
	public void stopPluginTimer(String plugin,String key) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.stopTimer(key);
		}
	}

	public SettingsGroup getSettings() {
		// TODO Auto-generated method stub
		return the_settings.getSettings().getOptions();
	}
	
	public SettingsGroup getPluginSettings(String plugin) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			return p.getSettings().getOptions();
		} else {
			return null;
		}
	}

	public void updateBooleanSetting(String key, boolean value) {
		the_settings.updateBooleanSetting(key,value);
	}
	
	public void updatePluginBooleanSetting(String plugin,String key,boolean value) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.updateBooleanSetting(key,value);
		}
	}
	
	public void updateStringSetting(String key,String value) {
		the_settings.updateStringSetting(key,value);
	}
	
	public void updatePluginStringSetting(String plugin,String key,String value) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.updateStringSetting(key,value);
		}
		
	}
	
	public void updateIntegerSetting(String key,int value) {
		the_settings.updateIntegerSetting(key,value);
	}
	
	public void updatePluginIntegerSetting(String plugin,String key,int value) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.updateIntegerSetting(key,value);
		}
	}
	
	public void updateFloatSetting(String key,float value) {
		the_settings.updateFloatSetting(key,value);
	}
	
	public void updatePluginFloatSetting(String plugin,String key,float value) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.updateFloatSetting(key,value);
		}
	}
	
	private class WindowSettingsChangedListener implements SettingsChangedListener {

		private String window;
		
		public WindowSettingsChangedListener(String window) {
			this.window = window;
		}
		
		@Override
		public void updateSetting(String key, String value) {
			Connection.this.handleWindowSettingsChanged(window,key,value);
		}
		
	}
	
	public void handleWindowSettingsChanged(String window,String key,String value) {
		synchronized(callbackSync) {
		int N = mWindowCallbacks.beginBroadcast();
		
		for(int i=0;i<N;i++) {
			IWindowCallback callback = mWindowCallbacks.getBroadcastItem(i);
			try{
				if(callback.getName().equals(window)) {
					callback.updateSetting(key,value);
				}
			} catch (RemoteException e) {
				
			}
		}
		
		mWindowCallbacks.finishBroadcast();
		}
		
	}

	@Override
	public void updateSetting(String key, String value) {
		
		BaseOption o = (BaseOption)the_settings.getSettings().getOptions().findOptionByKey(key);
		try {
			KEYS tmp = KEYS.valueOf(key);
			switch(tmp) {
			case debug_telnet:
				processor.setDebugTelnet((Boolean)o.getValue());
				break;
			case encoding:
				this.doUpdateEncoding((String)o.getValue());
				break;
			case orientation:
				break;
			case screen_on:
				break;
			case fullscreen:
				break;
			case fullscreen_editor:
				break;
			case use_suggestions:
				break;
			case keep_last:
				break;
			case compatibility_mode:
				break;
			case local_echo:
				break;
			case process_system_commands:
				break;
			case echo_alias_updates:
				break;
			case keep_wifi_alive:
				break;
			case cull_extraneous_color:
				break;
			case debug_telent:
				break;
			case bell_vibrate:
				break;
			case bell_notification:
				break;
			case bell_display:
				break;
			}
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	private void doUpdateEncoding(String value) {
		processor.setEncoding(value);
		//this.encoding = value;
		the_settings.setEncoding(value);
		this.working.setEncoding(value);
		
		for(int i=0;i<mWindows.size();i++) {
			WindowToken w = mWindows.get(i);
			w.getBuffer().setEncoding(value);
		}
		
		synchronized(callbackSync) {
			int N = mWindowCallbacks.beginBroadcast();
			
			for(int i=0;i<N;i++) {
				IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
				try {
					w.setEncoding(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			mWindowCallbacks.finishBroadcast();
		}
		
		for(int i=0;i<plugins.size();i++) {
			Plugin p = plugins.get(i);
			p.setEncoding(value);
		}
		
		//handle the keyboard command callback.
		keyboardCommand.setEncoding(value); 
		
		//may want to go through and activate the settings changed handler for plugins.
		//the chat window would want to re-construct it's buffers. But for proper operation
		//it may not be out of the question to make encoding change requrie a restart.
		//everything that doesn't use TextTree's directly to make multi-buffers, will work fine.		
	}

	private enum KEYS {
		debug_telnet,
		encoding, 
		orientation, 
		screen_on, 
		fullscreen, 
		fullscreen_editor,
		use_suggestions,
		keep_last, compatibility_mode,
		local_echo,
		process_system_commands,
		echo_alias_updates,
		keep_wifi_alive,
		cull_extraneous_color,
		debug_telent,
		bell_vibrate,
		bell_notification,
		bell_display
	}
	
	private void sendToServer(byte[] bytes) {
		//byte[] bytes = (byte[]) msg.obj;
		
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
			handler.sendEmptyMessage(MESSAGE_DISCONNECTED);
		}
	}

	public void updateWindowBufferMaxValue(String plugin, String window,
			int amount) {
		for(WindowToken w : mWindows) {
			if(w.getName().equals(window)) {
				//WindowToken w = mWindows.get(0);
				w.setBufferSize(amount);
			}
		} 
	}
	
	public void saveMainSettings() {
		//save the connection settings.
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
		String oldpath = internal + rootPath;
		exportSettings(oldpath);
	}
	
	public void exportSettings(String filename) {
		try {
			File file = new File(filename);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(ConnectionSetttingsParser.outputXML(the_settings,plugins).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class ExportFunction extends SpecialCommand {
		public ExportFunction() {
			this.commandName = "export";
		}
		
		public Object execute(Object o,Connection c) {
			
			String path = "/mnt/sdcard/BlowTorch/" + ((String)o);
			
			handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_EXPORTFILE,path));
			//exportSettings(path);
			return null;
		}
	}
	
	private class ImportFunction extends SpecialCommand {
		public ImportFunction() {
			this.commandName = "import";
		}
		
		public Object execute(Object o,Connection c) {
			
			String path = "/mnt/sdcard/BlowTorch/" + ((String)o);
			
			handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_IMPORTFILE, path));
			//importSettings(path);
			
			return null;
		}
	}
	
	private void importSettings(String path) {
		shutdownPlugins();
		
		VersionProbeParser vpp = new VersionProbeParser(path,service.getApplicationContext());
		
		try {
			boolean isLegacy = vpp.isLegacy();
			if(isLegacy) {
				HyperSAXParser p = new HyperSAXParser(path,service.getApplicationContext());
				HyperSettings s = p.load();
				
				//load up the default settings and then merge the old settings into the new settings.
				ArrayList<Plugin> tmpplugs = new ArrayList<Plugin>();
				ConnectionSetttingsParser newsettings = new ConnectionSetttingsParser(null,service.getApplicationContext(),tmpplugs,handler,this);
				tmpplugs = newsettings.load(this);
				//the_settings = (ConnectionSettingsPlugin) tmpplugs.get(0);
				
				ConnectionSettingsPlugin global = (ConnectionSettingsPlugin)tmpplugs.get(0);
				
				global.importV1Settings(s);
				
				Plugin buttonwindow = tmpplugs.get(1);
				
				//slag out the old settings and RAM them into the new ones.
				LuaState L = buttonwindow.getLuaState();
				
				L.newTable();
				for(String key : s.getButtonSets().keySet()) {
					String name = key;
					ColorSetSettings defaults = s.getSetSettings().get(key);
					//Vector<SlickButtonData> data = s.getButtonSets().get(key);
					L.newTable();
					
					if(defaults.getPrimaryColor() != SlickButtonData.DEFAULT_COLOR) {
						L.pushString("primaryColor");
						L.pushNumber(defaults.getPrimaryColor());
						L.setTable(-3);
					}
					
					if(defaults.getSelectedColor() != SlickButtonData.DEFAULT_SELECTED_COLOR) {
						L.pushString("selectedColor");
						L.pushNumber(defaults.getSelectedColor());
						L.setTable(-3);
					}
					
					if(defaults.getFlipColor() != SlickButtonData.DEFAULT_FLIP_COLOR) {
						L.pushString("flipColor");
						L.pushNumber(defaults.getFlipColor());
						L.setTable(-3);
					}
					
					if(defaults.getLabelColor() != SlickButtonData.DEFAULT_LABEL_COLOR) {
						L.pushString("labelColor");
						L.pushNumber(defaults.getLabelColor());
						L.setTable(-3);
					}
					
					if(defaults.getFlipLabelColor() != SlickButtonData.DEFAULT_FLIPLABEL_COLOR) {
						L.pushString("flipLabelColor");
						L.pushNumber(defaults.getFlipLabelColor());
						L.setTable(-3);
					}
					
					if(defaults.getButtonWidth() != SlickButtonData.DEFAULT_BUTTON_WDITH) {
						L.pushString("width");
						L.pushNumber(defaults.getButtonWidth());
						L.setTable(-3);
					}
					
					if(defaults.getButtonHeight() != SlickButtonData.DEFAULT_BUTTON_HEIGHT) {
						L.pushString("height");
						L.pushNumber(defaults.getButtonHeight());
						L.setTable(-3);
					}
					
					if(defaults.getLabelSize() != SlickButtonData.DEFAULT_LABEL_SIZE) {
						L.pushString("labelSize");
						L.pushNumber(defaults.getLabelSize());
						L.setTable(-3);
					}
					
					L.setField(-2, key);
				}
				
				L.setGlobal("buttonset_defaults");
				
				L.newTable();
				
				for(String name : s.getButtonSets().keySet()) {
					//String name = key;
					ColorSetSettings defaults = s.getSetSettings().get(name);
					Vector<SlickButtonData> data = s.getButtonSets().get(name);
					L.newTable();
					int counter = 1;
					for(SlickButtonData button : data) {
						L.newTable();
						if(defaults.getPrimaryColor() != button.getPrimaryColor()) {
							L.pushString("primaryColor");
							L.pushNumber(button.getPrimaryColor());
							L.setTable(-3);
						}
						
						if(defaults.getSelectedColor() != button.getSelectedColor()) {
							L.pushString("selectedColor");
							L.pushNumber(button.getSelectedColor());
							L.setTable(-3);
						}
						
						if(defaults.getFlipColor() != button.getFlipColor()) {
							L.pushString("flipColor");
							L.pushNumber(button.getFlipColor());
							L.setTable(-3);
						}
						
						if(defaults.getLabelColor() != button.getLabelColor()) {
							L.pushString("labelColor");
							L.pushNumber(button.getLabelColor());
							L.setTable(-3);
						}
						
						if(defaults.getFlipLabelColor() != button.getFlipLabelColor()) {
							L.pushString("flipLabelColor");
							L.pushNumber(button.getFlipLabelColor());
							L.setTable(-3);
						}
						
						if(defaults.getButtonWidth() != button.getWidth()) {
							L.pushString("width");
							L.pushNumber(button.getWidth());
							L.setTable(-3);
						}
						
						if(defaults.getButtonHeight() != button.getHeight()) {
							L.pushString("height");
							L.pushNumber(button.getHeight());
							L.setTable(-3);
						}
						
						if(defaults.getLabelSize() != button.getLabelSize()) {
							L.pushString("labelSize");
							L.pushNumber(button.getLabelSize());
							L.setTable(-3);
						}
						
						L.pushString("x");
						L.pushNumber(button.getX());
						L.setTable(-3);
						
						L.pushString("y");
						L.pushNumber(button.getY());
						L.setTable(-3);
						
						L.pushString("label");
						L.pushString(button.getLabel());
						L.setTable(-3);
						
						L.pushString("command");
						L.pushString(button.getText());
						L.setTable(-3);
						
						L.pushString("flipLabel");
						L.pushString(button.getFlipLabel());
						L.setTable(-3);
						
						L.pushString("flipCommand");
						L.pushString(button.getFlipCommand());
						L.setTable(-3);
						
						
						
						L.rawSetI(-2, counter);
						counter++;
					}
					
					L.setField(-2, name);
				}
				
				L.setGlobal("buttonsets");
				
				L.pushString(s.getLastSelected());
				L.setGlobal("current_set");
				
				L.getGlobal("legacyButtonsImported");
				if(L.getLuaObject(-1).isFunction()) {
					L.call(0, 0);
				}
				
				
				//s.getSetSettings();
				
				loadPlugins(tmpplugs);
			
			} else {
				int version = vpp.getVersionNumber();
				if(version == 2) {
					ArrayList<Plugin> tmpplugs = new ArrayList<Plugin>();
					ConnectionSetttingsParser csp = new ConnectionSetttingsParser(path,service.getApplicationContext(),tmpplugs,handler,this);
					tmpplugs = csp.load(this);
					loadPlugins(tmpplugs);
				}
				
			}
			
			
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
	
	private void loadInternalSettings() {
		Pattern invalidchars = Pattern.compile("\\W");
		Matcher replacebadchars = invalidchars.matcher(this.display);
		String prefsname = replacebadchars.replaceAll("");
		prefsname = prefsname.replaceAll("/", "");
		//String settingslocation = 
		//loadXmlSettings(prefsname +".xml");
		String rootPath = prefsname + ".xml";
		//String convertPath = prefsname + ".v1.xml";
		//String newPath = prefsname + ".v2.xml";
		String internal = service.getApplicationContext().getApplicationInfo().dataDir + "/files/";
		File oldp = new File(internal+rootPath);
		//HyperSettings oldSettings = null;
		
		if(!oldp.exists()) {
			//oldp.renameTo(new File(internal+convertPath));
			importSettings(null);
		} else {
			importSettings(rootPath);
		}

		
	}
	
	
	private void buildSettingsPage() {
		if(the_settings.getSettings().getWindows().size() < 1) {
			WindowToken token = new WindowToken(MAIN_WINDOW,null,null);
			//token.layouts.clear();
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			LayoutGroup g = new LayoutGroup();
			g.type = LayoutGroup.LAYOUT_TYPE.normal;
			g.setLandscapeParams(p);
			g.setPortraitParams(p);
			mWindows.add(0,token);
		} else {
			//mWindows.add
			mWindows.add(0,the_settings.getSettings().getWindows().get(MAIN_WINDOW));
		}
		
		the_settings.doBackgroundStartup();
		for(Plugin pl : plugins) {
			pl.doBackgroundStartup();
		}
		
		for(Plugin pl : plugins) {
			pl.buildAliases();
		}
		
		SettingsGroup sg = new SettingsGroup();
		sg.setTitle("Program Settings");
		sg.setListener(this);

		EncodingOption enc = new EncodingOption();
		enc.setTitle("System Encoding");
		enc.setDescription("Specifies the encoding used to process incoming text.");
		enc.setKey("encoding");
		sg.addOption(enc);
		
		ListOption orientation = new ListOption();
		orientation.setTitle("Orientation");
		orientation.setDescription("Sets the layout mode for the application. Automatic will switch the layout when the device rotates.");
		orientation.setKey("orientation");
		orientation.setValue(new Integer(0));
		orientation.addItem("Automatic");
		orientation.addItem("Landscape");
		orientation.addItem("Portrait");
		sg.addOption(orientation);
		
		BooleanOption screen_on = new BooleanOption();
		screen_on.setTitle("Keep Screen On?");
		screen_on.setDescription("Keep the screen on while the window is active.");
		screen_on.setKey("screen_on");
		screen_on.setValue(true);
		sg.addOption(screen_on);
		
		BooleanOption fullscreen = new BooleanOption();
		fullscreen.setTitle("Use Fullscreen Window?");
		fullscreen.setDescription("Hides the notification bar. This can be toggled by typing .togglefullscreen");
		fullscreen.setKey("fullscreen");
		fullscreen.setValue(true);
		sg.addOption(fullscreen);

		//SettingsGroup window = token.getSettings();
		mWindows.get(0).getSettings().setListener(new WindowSettingsChangedListener(mWindows.get(0).getName()));
		sg.addOption(mWindows.get(0).getSettings());
		
		SettingsGroup input = new SettingsGroup();
		input.setTitle("Input");
		input.setDescription("Options that deal with the input box and editors.");
		
		BooleanOption fullscreen_editor = new BooleanOption();
		fullscreen_editor.setTitle("Fullscreen Editor?");
		fullscreen_editor.setDescription("Show the full screen editor when the input bar is clicked.");
		fullscreen_editor.setKey("fullscreen_editor");
		fullscreen_editor.setValue(false);
		input.addOption(fullscreen_editor);
		
		BooleanOption use_suggestions = new BooleanOption();
		use_suggestions.setTitle("Use Suggestions?");
		use_suggestions.setDescription("Attempt suggestions if the full screen editor is not used.");
		use_suggestions.setKey("use_suggestions");
		use_suggestions.setValue(false);
		input.addOption(use_suggestions);
		
		BooleanOption keep_last = new BooleanOption();
		keep_last.setTitle("Keep Last Entered?");
		keep_last.setDescription("Keeps the last text entered in the window and highights after sending.");
		keep_last.setKey("keep_last");
		keep_last.setValue(false);
		input.addOption(keep_last);
		
		BooleanOption compatilibility_mode = new BooleanOption();
		compatilibility_mode.setTitle("Enable Compatibility Mode?");
		compatilibility_mode.setDescription("Enable this if you have problems with bascpace not workin in the non-full screen editor.");
		compatilibility_mode.setKey("compatibility_mode");
		compatilibility_mode.setValue(false);
		input.addOption(compatilibility_mode);
		
		sg.addOption(input);

		
		
		SettingsGroup servOptions = new SettingsGroup();
		servOptions.setTitle("Service");
		servOptions.setDescription("Options for the background service and data processing.");
		
		BooleanOption local_echo = new BooleanOption();
		local_echo.setTitle("Local Echo?");
		local_echo.setDescription("Will the service echo data sent to the server?");
		local_echo.setKey("local_echo");
		local_echo.setValue(true);
		servOptions.addOption(local_echo);
		
		BooleanOption process_system_commands = new BooleanOption();
		process_system_commands.setTitle("Process System Commands?");
		process_system_commands.setDescription("Perform system functions for input beginning with the specified system command marker.");
		process_system_commands.setKey("process_system_commands");
		process_system_commands.setValue(true);
		servOptions.addOption(process_system_commands);
		
		BooleanOption echo_alias_updates = new BooleanOption();
		echo_alias_updates.setTitle("Echo Alias Updates?");
		echo_alias_updates.setDescription("Local echo system command updates to aliases.");
		echo_alias_updates.setKey("echo_alias_updates");
		echo_alias_updates.setValue(true);
		servOptions.addOption(echo_alias_updates);
		
		BooleanOption keep_wifi_alive = new BooleanOption();
		keep_wifi_alive.setTitle("Keep Wifi Alive?");
		keep_wifi_alive.setDescription("Attempt to keep WiFi radio active while connected.");
		keep_wifi_alive.setKey("keep_wifi_alive");
		keep_wifi_alive.setValue(true);
		servOptions.addOption(keep_wifi_alive);
		
		BooleanOption cull_extraneous = new BooleanOption();
		cull_extraneous.setTitle("Cull Extraneous Colors?");
		cull_extraneous.setDescription("Removes extraneous color codes.");
		cull_extraneous.setKey("cull_extraneous_color");
		cull_extraneous.setValue(true);
		servOptions.addOption(cull_extraneous);
		
		BooleanOption debug_telnet = new BooleanOption();
		debug_telnet.setTitle("Debug Telnet?");
		debug_telnet.setDescription("Shows data involving telnet option transactions in the window.");
		debug_telnet.setKey("debug_telnet");
		debug_telnet.setValue(false);
		servOptions.addOption(debug_telnet);
		
		sg.addOption(servOptions);
		
		SettingsGroup bellOptions = new SettingsGroup();
		bellOptions.setTitle("Bell");
		bellOptions.setDescription("Options for what happens when the bell character is recieved.");
		
		BooleanOption bell_vibrate = new BooleanOption();
		bell_vibrate.setTitle("Vibrate?");
		bell_vibrate.setDescription("Plays a short vibrate pattern when the bell is recieved.");
		bell_vibrate.setKey("bell_vibrate");
		bell_vibrate.setValue(true);
		bellOptions.addOption(bell_vibrate);
		
		BooleanOption bell_notification = new BooleanOption();
		bell_notification.setTitle("Generate Notification?");
		bell_notification.setDescription("Spawns a new notification when bell is recieved.");
		bell_notification.setKey("bell_notification");
		bell_notification.setValue(false);
		bellOptions.addOption(bell_notification);
		
		BooleanOption bell_display = new BooleanOption();
		bell_display.setTitle("Display Bell?");
		bell_display.setDescription("Displays a small alert on the screen when the bell character is recieved.");
		bell_display.setKey("bell_display");
		bell_display.setValue(false);
		bellOptions.addOption(bell_display);
		
		sg.addOption(bellOptions);		
		
		the_settings.getSettings().setOptions(sg);
	}
}
