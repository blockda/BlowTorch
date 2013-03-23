package com.offsetnull.bt.service;

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

import com.offsetnull.bt.responder.IteratorModifiedException;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.gag.GagAction;
import com.offsetnull.bt.responder.script.ScriptResponder;
import com.offsetnull.bt.responder.toast.ToastResponder;
import com.offsetnull.bt.script.ScriptData;
import com.offsetnull.bt.service.IWindowCallback;
import com.offsetnull.bt.service.function.BellCommand;
import com.offsetnull.bt.service.function.ClearButtonCommand;
import com.offsetnull.bt.service.function.ColorDebugCommand;
import com.offsetnull.bt.service.function.DirtyExitCommand;
import com.offsetnull.bt.service.function.DisconnectCommand;
import com.offsetnull.bt.service.function.FullScreenCommand;
import com.offsetnull.bt.service.function.FunctionCallbackCommand;
import com.offsetnull.bt.service.function.KeyboardCommand;
import com.offsetnull.bt.service.function.LoadButtonsCommand;
import com.offsetnull.bt.service.function.ReconnectCommand;
import com.offsetnull.bt.service.function.SpecialCommand;
import com.offsetnull.bt.service.function.SpeedwalkCommand;
import com.offsetnull.bt.service.function.SwitchWindowCommand;
import com.offsetnull.bt.service.plugin.ConnectionSettingsPlugin;
import com.offsetnull.bt.service.plugin.Plugin;
import com.offsetnull.bt.service.plugin.settings.BaseOption;
import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.ColorOption;
import com.offsetnull.bt.service.plugin.settings.ConnectionSetttingsParser;
import com.offsetnull.bt.service.plugin.settings.EncodingOption;
import com.offsetnull.bt.service.plugin.settings.FileOption;
import com.offsetnull.bt.service.plugin.settings.IntegerOption;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.StringOption;
import com.offsetnull.bt.service.plugin.settings.Option;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.service.plugin.settings.VersionProbeParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings.PLUGIN_LOCATION;
import com.offsetnull.bt.settings.ColorSetSettings;
import com.offsetnull.bt.settings.ConfigurationLoader;
import com.offsetnull.bt.settings.HyperSAXParser;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.speedwalk.DirectionData;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.window.TextTree.Line;
import com.offsetnull.bt.window.Window;

import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.button.SlickButtonData;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import android.util.Log;
//import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class Connection implements SettingsChangedListener,ConnectionPluginCallback {
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
	public static final int MESSAGE_WINDOWXCALLB = 25;
	public static final int MESSAGE_PLUGINLUAERROR = 26;
	private static final int MESSAGE_DORESETSETTINGS = 27;
	protected static final int MESSAGE_ADDLINK = 28;
	private static final int MESSAGE_DELETEPLUGIN = 29;
	static final int MESSAGE_CONNECTED = 30;
	private static final int MESSAGE_RECONNECT = 31;
	public static final int MESSAGE_TRIGGER_LUA_ERROR = 32;
	public static final int MESSAGE_RELOADSETTINGS = 33;
	public static final int MESSAGE_SETTRIGGERSDIRTY = 34;
	private static final int MESSAGE_CALLPLUGIN = 35;
	private static final int MESSAGE_TIMERINFO = 36;
	private static final int MESSAGE_TIMERSTART = 37;
	private static final int MESSAGE_TIMERPAUSE = 38;
	private static final int MESSAGE_TIMERRESET = 39;
	private static final int MESSAGE_TIMERSTOP = 40;
	public static final int MESSAGE_TERMINATED_BY_PEER = 41;
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
	
	private enum TIMER_ACTION {
		PLAY,
		PAUSE,
		RESET,
		INFO,
		STOP,
		NONE
	}
	
	//TIMER_ACTION current_timer_action = TIMER_ACTION.NONE;
	
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
	private boolean localEcho = true;
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	//StringBuffer joined_alias = new StringBuffer();
	//Pattern alias_replace = Pattern.compile(joined_alias.toString());
	//Matcher alias_replacer = alias_replace.matcher("");
	//Matcher alias_recursive = alias_replace.matcher("");
	//Pattern whiteSpace = Pattern.compile("\\s");
	
	public Object callbackSync = new Object();
	private int statusBarHeight;
	private int titleBarHeight;
	
	
	public Connection(String display,String host,int port,StellarService service) {
		
		ColorDebugCommand colordebug = new ColorDebugCommand();
		DirtyExitCommand dirtyexit = new DirtyExitCommand();
		TimerCommand timercmd = new TimerCommand();
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
		specialcommands.put(timercmd.commandName, timercmd);
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
				case MESSAGE_TERMINATED_BY_PEER:
					killNetThreads(true);
					DoDisconnect(true);
					isConnected = false;
					break;
				case MESSAGE_TIMERSTOP:
					doTimerAction((String)msg.obj,msg.arg2,TIMER_ACTION.STOP);
					break;
				case MESSAGE_TIMERSTART:
					//current_timer_action = TIMER_ACTION.PLAY;
					doTimerAction((String)msg.obj,msg.arg2,TIMER_ACTION.PLAY);
					break;
				case MESSAGE_TIMERRESET:
					//current_timer_action = TIMER_ACTION.PLAY;
					doTimerAction((String)msg.obj,msg.arg2,TIMER_ACTION.RESET);
					break;
				case MESSAGE_TIMERINFO:
					// = TIMER_ACTION.PLAY;
					doTimerAction((String)msg.obj,msg.arg2,TIMER_ACTION.INFO);
					break;
				case MESSAGE_TIMERPAUSE:
					//doTimerAction((String)msg.obj,msg.arg2);
					doTimerAction((String)msg.obj,msg.arg2,TIMER_ACTION.PAUSE);
					break;
				case MESSAGE_CALLPLUGIN:
					String ptmp = msg.getData().getString("PLUGIN");
					String ftmp = msg.getData().getString("FUNCTION");
					String dtmp = msg.getData().getString("DATA");
					doCallPlugin(ptmp,ftmp,dtmp);
					break;
				case MESSAGE_SETTRIGGERSDIRTY:
					setTriggersDirty();
					break;
				case MESSAGE_RELOADSETTINGS:
					reloadSettings();
					break;
				case MESSAGE_TRIGGER_LUA_ERROR:
					dispatchLuaError((String)msg.obj);
					break;
				case MESSAGE_RECONNECT:
					doReconnect();
					break;
				case MESSAGE_CONNECTED:
					mAutoReconnectAttempt = 0;
					break;
				case MESSAGE_DELETEPLUGIN:
					doDeletePlugin((String)msg.obj);
					break;
				case MESSAGE_ADDLINK:
					doAddLink((String)msg.obj);
					break;
				case MESSAGE_DORESETSETTINGS:
					doResetSettings();
					break;
				case MESSAGE_PLUGINLUAERROR:
					dispatchLuaError((String)msg.obj);
					break;
				case MESSAGE_EXPORTFILE:
					exportSettings((String)msg.obj);
					break;
				case MESSAGE_IMPORTFILE:
					Connection.this.service.markWindowsDirty();
					importSettings((String)msg.obj,true,false);
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
					if(o == null) {
						o = "";
					}
					String token = msg.getData().getString("TOKEN");
					String function = msg.getData().getString("FUNCTION");
					try {
						Connection.this.windowXCallS(token,function,o);
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						//e3.printStackTrace();
					}
					break;
				case MESSAGE_WINDOWXCALLB:
					byte[] bytesa = (byte[]) msg.obj;
					String tokens = msg.getData().getString("TOKEN");
					String functions = msg.getData().getString("FUNCTION");
					try {
						Connection.this.windowXCallB(tokens, functions, bytesa);
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						//e3.printStackTrace();
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
					//Log.e("PLUGIN","TRING ACTUALLY MODDING WINDOW("+name+") Buffer:"+set);
					
					for(WindowToken tok : mWindows) {
						if(tok.getName().equals(name)) {
							//Log.e("PLUGIN","ACTUALLY MODDING WINDOW("+name+") Buffer:"+set);
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
					Object line = msg.obj;
					String target = msg.getData().getString("TARGET");
					try {
						Connection.this.lineToWindow(target,line);
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
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
					
					//construct the gmcp packet.
					byte IAC = (byte) 0xFF;
					byte SB = (byte) 0xFA;
					byte SE = (byte) 0xF0;
					byte DO = (byte) 0xFD;
					byte GMCP = TC.GMCP;
					int size = ((String)msg.obj).length() + 5;
					ByteBuffer fub = ByteBuffer.allocate(size);
					fub.put(IAC).put(SB).put(GMCP);
					try {
						fub.put(((String)msg.obj).getBytes("ISO-8859-1"));
					} catch (UnsupportedEncodingException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					fub.put(IAC).put(SE);
					byte[] fubtmp = new byte[size];
					//String pkt = IAC + SB+GMCP + ((String)msg.obj) + IAC + SE;
					fub.rewind();
					fub.get(fubtmp);
					if(pump != null && pump.isConnected()) {
						//try {
							//Log.e("COnn","CONNECTION SENDING GMCP:"+new String(fubtmp,"ISO-8859-1"));
							pump.sendData(fubtmp);
						//} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
						//	e1.printStackTrace();
						//}
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
					if(the_settings.isVibrateOnBell()) {
						Connection.this.service.doVibrateBell();
					}
					if(the_settings.isNotifyOnBell()) {
						Connection.this.service.doNotifyBell(Connection.this.display,Connection.this.host,Connection.this.port);
					}
					if(the_settings.isDisplayOnBell()) {
						Connection.this.service.doDisplayBell();
					}
					break;
				case MESSAGE_DODIALOG:
					DispatchDialog((String)msg.obj);
					break;
				case MESSAGE_PROCESS:
					try {
						//synchronized(Connection.this) {
							dispatch((byte[])msg.obj);
						//}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_DISCONNECTED:
					killNetThreads(true);
					DoDisconnect(false);
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
		working.setMaxLines(10000);
		
		finished = new TextTree();
		finished.setLinkify(false);
		finished.setLineBreakAt(10000000);
		finished.setMaxLines(10000);
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
		SharedPreferences sprefs = this.getContext().getSharedPreferences("STATUS_BAR_HEIGHT", 0);
		statusBarHeight = sprefs.getInt("STATUS_BAR_HEIGHT", (int)(25 * this.getContext().getResources().getDisplayMetrics().density));
		titleBarHeight = sprefs.getInt("TITLE_BAR_HEIGHT", 0);

		loaded = true;
		
		//fish out the window.

	}
	
	//protected void doDisplayBell() {
	//	// TODO Auto-generated method stub
		
	//}

	//protected void doNotifyBell() {
		/*final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).doVisualBell();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();*/
//		IWindowCallback main = windowCallbackMap.get("mainDisplay");
//		if(main!=null) {
//			main.
//		}
	//}

	protected void dispatchLuaError(String message) {
		try {
			dispatchNoProcess(message.getBytes(the_settings.getEncoding()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		if(callback == null) return;
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
		
		TextTree buffer = w.getBuffer();
		if(buffer.getBrokenLineCount() == 0) {
			//Log.e("BUFFERTEST","INVALIDATE WINDOW TEXT CALLED WITH NO DATA");
		}
		
		//callback.clearText();
		//callback.rawDataIncoming(buffer.dumpToBytes(true));
		callback.resetWithRawDataIncoming(buffer.dumpToBytes(true));
		
		//mWindowCallbacks.finishBroadcast();
		//}
	}

	public void windowXCallS(String token, String function, Object o) throws RemoteException {
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i=0;i<N;i++) {
			IWindowCallback c = windowCallbackMap.get(token);
		//	String name = c.getName();
		//	if(c.getName().equals(token)) {
			if(c != null) {
				c.xcallS(function,(String)o);
			}
			//return ret;
		//		i=N;
		//	}
		//}
		
		//mWindowCallbacks.finishBroadcast();
		//}
	}

	protected void windowXCallB(String tokens, String functions, byte[] bytes) throws RemoteException {
		IWindowCallback c = windowCallbackMap.get(tokens);
		if(c != null) {
			c.xcallB(functions, bytes);
		}
	}
	
	private void doCallPlugin(String plugin, String function,
			String data) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.callFunction(function,data);
		} else {
			this.dispatchLuaText("\n"+Colorizer.colorRed+"No plugin named: "+plugin+Colorizer.colorRed+"\n");
		}
		//Plugin p = pluginMap.get(plugin);
		//p.callFunction(function,data);
	
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
		
		
		windowCallbackMap.clear();
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
	
	private void loadPlugins(ArrayList<Plugin> tmpPlugs,String summary) {
		
		HashMap<String,TextTree> bufferSaves = new HashMap<String,TextTree>();
		
		TextTree buffer = null;
		if(mWindows.size() > 0) {
			buffer = mWindows.get(0).getBuffer();
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
		the_settings.initTimers();
		for(WindowToken tmpw : the_settings.getSettings().getWindows().values()) {
			tmpw.setDisplayHost(display);
		}
		
		mWindows.add(0,the_settings.getSettings().getWindows().get(MAIN_WINDOW));
		if(buffer == null) {
			buffer = mWindows.get(0).getBuffer();
		} else {
			buffer.addString("\n\n");
		}
		
		buffer.addString(summary);
		tmpPlugs.remove(0);
		
		pluginMap.clear();
		linkMap.clear();
		
		plugins.addAll(tmpPlugs);
		
		
		for(Plugin p : plugins) {
			for(WindowToken tmpw : p.getSettings().getWindows().values()) {
				tmpw.setDisplayHost(display);
			}
			p.initTimers();
			pluginMap.put(p.getName(), p);
			p.sortTriggers();
			if(p.getSettings().getWindows().size() > 0) {
				mWindows.addAll(p.getSettings().getWindows().values());
			}
			
			p.pushOptionsToLua();
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
				buffer.addString(Colorizer.colorWhite+"Loading plugin file: "+link);
				String filename = Environment.getExternalStorageDirectory() + "/BlowTorch/" + link;
				//Log.e("XML","Attempting to load plugins from:" + filename);
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
						
						for(WindowToken tmpw : p.getSettings().getWindows().values()) {
							tmpw.setDisplayHost(display);
						}
						
						if(p.getSettings().getWindows().size() > 0) {
							mWindows.addAll(p.getSettings().getWindows().values());
						}
						
						p.pushOptionsToLua();
					}
					
					plugins.addAll(group);
					
					buffer.addString(Colorizer.colorWhite+", success."+Colorizer.colorWhite+"\n");
					
					//tmpPlug.setSettings(parse.load());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					buffer.addString(Colorizer.colorRed+" file not found."+Colorizer.colorWhite+"\n");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					//try {
						//this.dispatchLuaError(message)
						
						//service.dispatchXMLError(e.getLocalizedMessage());
					//} catch (RemoteException e1) {
						// TODO Auto-generated catch block
					//	e1.printStackTrace();
					//}
					buffer.addString(Colorizer.colorRed+ " XML Parse error.\n"+e.getLocalizedMessage()+Colorizer.colorWhite+"\n");
					//e.printStackTrace();
				}
			}
		}
	
		
		//so now that we have all the plugins, we need to build up the processor's gmcpTriggerTables.
		//loop through all the plugins, looking for literal triggers starting
		//with the gmcpTriggerChar.
		
		
		//plugins.
		//tmpPlug.initScripts(mWindows);
		//tmpPlug.initScripts();
		
		if(bufferSaves != null) {
			for(WindowToken w : mWindows) {
				if(w != null) {
					
					if(bufferSaves.get(w.getName()) != null) {
						w.setBuffer(bufferSaves.get(w.getName()));
					}
				}
			}
		}
		
		//plugins.add(tmpPlug);
		buildSettingsPage();
		
		//build and sort the new trigger array.
		//the_settings.buildTriggerSystem();
		//for(Plugin p : plugins) {
		//	p.buildTriggerSystem();
		//}
		//buildTriggerSystem();
		service.reloadWindows();
		
	}
	

	
	public void buildTriggerSystem() {
		//start with the global settings.
		//long start = System.currentTimeMillis();
		if(the_settings == null) return;
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
						if(!addseparator) {
							triggerBuilder.append("(");
							if(!t.isInterpretAsRegex()) {
								triggerBuilder.append("\\Q");
							}
							triggerBuilder.append(t.getPattern());
							if(!t.isInterpretAsRegex()) {
								triggerBuilder.append("\\E");
							}
							triggerBuilder.append(")");
							addseparator = true;
						} else {
							triggerBuilder.append("|(");
							if(!t.isInterpretAsRegex()) {
								triggerBuilder.append("\\Q");
							}
							triggerBuilder.append(t.getPattern());
							if(!t.isInterpretAsRegex()) {
								triggerBuilder.append("\\E");
							}
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
								if(!t.isInterpretAsRegex()) {
									triggerBuilder.append("\\Q");
								}
								triggerBuilder.append(t.getPattern());
								if(!t.isInterpretAsRegex()) {
									triggerBuilder.append("\\E");
								}
								triggerBuilder.append(")");
								addseparator = true;
							} else {
								triggerBuilder.append("|(");
								if(!t.isInterpretAsRegex()) {
									triggerBuilder.append("\\Q");
								}
								triggerBuilder.append(t.getPattern());
								if(!t.isInterpretAsRegex()) {
									triggerBuilder.append("\\E");
								}
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
		String trgstr = triggerBuilder.toString();
		trgstr = trgstr.replace("|", "\n");
		//Log.e("MASSIVE",trgstr);
		massivePattern = Pattern.compile(massiveTriggerString,Pattern.MULTILINE);
		
		massiveMatcher = massivePattern.matcher("");
		
		//long delta = System.currentTimeMillis() - start;
		//Log.e("TRIGGERS","TIMEPROFILE trigger system took " + delta + " millis to build.");
		triggersDirty = false;
	}
	
	private HashMap<String,ArrayList<String>> linkMap = new HashMap<String,ArrayList<String>>();
	private HashMap<String,Plugin> pluginMap = new HashMap<String,Plugin>(0);
	
	protected void redrawWindow(String win) {
		//Log.e("WINDOW","SERVICE ATTEMPTING TO REDRAW WINDOW:" + win);
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i=0;i<N;i++) {
			IWindowCallback w = windowCallbackMap.get(win);
			if(w == null) return;
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

	
	protected void lineToWindow(String target, Object line) throws RemoteException {
		//synchronized(callbackSync) {
		
		for(WindowToken w : mWindows) {
			if(w.getName().equals(target)) {
				TextTree tmp = new TextTree();
				tmp.setEncoding(the_settings.getEncoding());
				if(line instanceof TextTree.Line) {
					tmp.appendLine((TextTree.Line)line);
				} else if (line instanceof String) {
					try {
						tmp.addBytesImpl(((String)line).getBytes(the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
					if(c!=null) {
					
							//if(target.equals(c.getName())) {
								c.rawDataIncoming(lol);
							//}
						
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
	
	public void unregisterWindowCallback(IWindowCallback callback) {
		String name = "";
		try {
			name = callback.getName();
		} catch (RemoteException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//try {
			//Log.e("SERVICE",display + ": attempting to remove " + name + " from the callback map.");
		//} catch (RemoteException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		
		if(callbacksStarted) {
			mWindowCallbacks.finishBroadcast();
			callbacksStarted = false;
		}
		
		if(!mWindowCallbacks.unregister(callback)) {
			//Log.e("SERVICE",display+":attempted to remove "+name+ " from the callback list but failed.");
		}
		//int N = mWindowCallbacks.beginBroadcast();
		//for(int i = 0;i<N;i++) {
		//	IWindowCallback c = mWindowCallbacks.getBroadcastItem(i);
		//	String tmp = c.getName();
		windowCallbackMap.clear();
		int N = mWindowCallbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				//Log.e("SERVICE",display + ":ADDING "+w.getName() + " to the callback map.");
				windowCallbackMap.put(w.getName(), w);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(!callbacksStarted) { callbacksStarted = true; }
		//}
	}
	
	private final String MAIN_WINDOW = "mainDisplay";
	private String outputWindow = MAIN_WINDOW;
	
	protected void DoDisconnect(boolean override) {
		//TODO: if window showing, show disconnection.
		if(handler == null) return;
		if(mAutoReconnect && !override) {
			if(mAutoReconnectAttempt < mAutoReconnectLimit) {
				mAutoReconnectAttempt++;
				String message = "\n" + Colorizer.colorRed + "Network connection disconnected.\n" + "Attmempting reconnect in 3 seconds. " + (mAutoReconnectLimit - mAutoReconnectAttempt) + " tries remaining."+Colorizer.colorWhite+"\n";
				handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_PROCESSORWARNING,(message)));
				handler.sendEmptyMessageDelayed(MESSAGE_RECONNECT, 3000);
				return;
			}
		}
		
		
		service.DoDisconnect(this);
	}
	
	

	protected void killNetThreads(boolean noreconnect) {
		
		if(pump == null) return;
		//Log.e("TEST","Killing net threads");
		if(pump != null) {
			if(pump.handler != null) {
				pump.handler.sendEmptyMessage(DataPumper.MESSAGE_END);
			}
			pump.closeSocket();
			//pump.interrupt();
			//pump = null;
			try {
				//Log.e("TEST","waiting for thread death.");
				pump.join();
				//Log.e("TEST","thread death completed.");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		processor = null;
		
		//synchronized(this) {
		if(noreconnect) {
			if(handler != null) {
				handler.removeMessages(MESSAGE_RECONNECT);
			}
		}
		//}
		
		pump = null;
	}
	
	private void dispatchNoProcess(byte[] data) {
		mWindows.get(0).getBuffer().addBytesImplSimple(data);
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
	
	private boolean triggersDirty = false;
	public void setTriggersDirty() {
		triggersDirty = true;
	}
	
	TreeSet<Range> lineMap = new TreeSet<Range>(new RangeComparator());
	StringBuilder triggerBuilder = new StringBuilder();
	String massiveTriggerString = null;
	Pattern massivePattern = null;
	Matcher massiveMatcher = null;
	HashMap<Integer,TriggerData> sortedTriggerMap = null;
	HashMap<Integer,Plugin> triggerPluginMap = null;
	HashMap<Integer,Integer> lineStartMap = new HashMap<Integer,Integer>();
	private boolean processing = false;
	private byte[] holddata = null;
	private void dispatch(byte[] data) throws UnsupportedEncodingException {
		//long start = System.currentTimeMillis();
		
		//String str = new String(data);
		//Log.e("RAW","RAW DATA:\n"+str);
//		if(processing) {
//			Log.e("DISPATCHTEST","DISPATCH PROCESSING WHILE DATA IS STILL BEING PROCESSED");
//			String newstr = new String(data);
//			String oldstr = new String(holddata);
//			Log.e("DISPATCHTEST","New data:\n"+newstr);
//			Log.e("DISPATCHTEST","Old Data:\n"+oldstr);
//		}
		holddata = data;
		processing = true;
		//String ftmp = new String(data);
		//Log.e("FIN","INCOMING RAW-----\n"+ftmp+"\nEND INCOMING RAW-----");
		
		byte[] raw = processor.RawProcess(data);
		if(raw == null) { processing = false; return; }
		
		//long deltaraw = System.currentTimeMillis() - start;
		//.e("PARSING","TIMEPROFILE trigger raw processing took " + deltaraw);
		//String rawproc = new String(raw);
		//Log.e("RAW","PROCESSED RAW DATA:\n"+rawproc);
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

		//long bleedstart = System.currentTimeMillis();
		TextTree.Color tmpcolor = buffer.getBleedColor();
		working.setBleedColor(tmpcolor);
		finished.setBleedColor(tmpcolor);
		//long deltableed = System.currentTimeMillis() - bleedstart;
		//Log.e("PARSING","TIMEPROFILE finding the bleed color took " + deltableed);
		//long addstart = System.currentTimeMillis();
		working.addBytesImpl(raw);
		
//		Log.e("FIN","---------START XMISSION BLOCK----------");
//		for(int i=working.getLines().size()-1;i>=0;i--) {
//			Line tmpl = working.getLines().get(i);
//			Log.e("FIN","WORKING("+i+"):" + TextTree.deColorLine(tmpl));
//			
//		}
//		Log.e("FIN","---------END XMISSION BLOCK----------");
		//Log.e("FIN","END TRANSMISSION:-------------------------------");
		//long deltadelta = System.currentTimeMillis() - addstart;
		//Log.e("PARSING","TIMEPROFILE adding to the working buffer took " + deltadelta);
		//long deltatmp2 = System.currentTimeMillis() - start;
		//Log.e("PARSING","TIMEPROFILE triggers did " + deltatmp2 + " of necessary prep.");
		
		//long firstpart = System.currentTimeMillis();
		working.setModCount(0);
		//strip the color out.
		colorStripper.reset(new String(raw,the_settings.getEncoding()));
		String stripped = colorStripper.replaceAll("");
		
		//Log.e("LOG","TRIGGER TESTING:\n"+stripped);
		//build up the newline map.
		if(triggersDirty) {
			buildTriggerSystem();
		}

		
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
			processing = false;
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
				
				boolean replace_gagged = false;
				int gagloc = -1;
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
					gagloc = tmp.first().getEnd();
					
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
						//Log.e("TRIGGER","TRIGGER "+t.getName()+" FIRED ON LINE:"+tmpline);
						
//						if(t.getName().equals("map_capture_end") || t.getName().equals("map_capture")) {
//							Log.e("Parse","Debug Me");
//						}
						//Log.e("TRIGGER","trigger matched:" + t.getName() + " :"+massiveMatcher.group());
						//String matched3 = massiveMatcher.group();
						boolean gagged = false;
						if(lineNumber > tmpline) {
							int amount = (lineNumber - tmpline);
							
							for(int i=0;i<amount;i++) {
								if(it.hasPrevious()) {
								l = it.previous();
								}
							}
							working.setModCount(0);
							lineNumber = tmpline;
							//Log.e("TRIGGER","Line Data:" + TextTree.deColorLine(l));
							if(it.hasNext()) {
								//Line tmpl = it.next();
								//it.previous();
								lineNumber = tmpline;
								
							} else {
							//	Log.e("TRIGGER","DEBUG ME");
								//lineNumber = working.getLines().size()-1;
								
							}
							//Log.e("TRIGGER","TRIGGER FOUND AND PROCESSING ON LINE:"+lineNumber);
							
						} else if(tmpline > lineNumber) {
							gagged = true;
						} else {
							//l = it.next();
						}
						//Log.e("MATCHED","MATCHED TRIGGER:" + matched3 + " : " + t.getName());
						if(t != null && t.isEnabled() && gagged == false) {
							captureMap.clear();
							for(int i=index;i<=(t.getMatcher().groupCount()+index);i++) {
								
								captureMap.put(Integer.toString(i-index), massiveMatcher.group(i));
							}
							for(TriggerResponder responder : t.getResponders()) {
								if(responder instanceof GagAction) {
									replace_gagged = true;
									//gagloc = lineend;
								}
								try {
									boolean ret = responder.doResponse(service.getApplicationContext(), working, lineNumber, it, l, tmpstart,tmpend,matched, t, display,host,port, StellarService.getNotificationId(), service.isWindowConnected(), handler, captureMap, p.getLuaState(), t.getName(), the_settings.getEncoding());
									
									if(ret == true) {
										//keepEvaluating = false;
										//rebuildTriggers = true;
										//this signals to rebuild the trigger system.
										
									}
									
									if(triggersDirty) {
										keepEvaluating = false;
										rebuildTriggers = true;
										//Log.e("dirty","triggers marked dirty, rebuilding.");
									}
									//TriggerResponder r = new GagAction();

								} catch(IteratorModifiedException e1) {
									it = e1.getIterator();
									int now = working.getLines().size();
									working.setModCount(0);
									lineNumber = it.previousIndex();
									linedelta = now - linesize;
									if(it.hasPrevious()) {
										l = it.previous();
									} else {
										keepEvaluating = false;
									}
									//it.next();
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
					working.setModCount(0);
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
						if(replace_gagged) {
							stripped = stripped.substring(gagloc+1,stripped.length());
						} else {
							stripped = stripped.substring(e+1,stripped.length());
						}
						
					}
					
					if(lineNumber <= working.getLines().size()-1) {
						//while(working.getLines().size()-1 >= lineNumber) { // DCB 1/22/13 - this was to make the map window work.
						while(working.getLines().size()-1 > lineNumber) {
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
		
		
		//long now = System.currentTimeMillis();
		//Log.e("Connection","TIMEPROFILE trigger parsing took:" + Long.toString(now - start) + " millis.");
		sendBytesToWindow(proc);
		
		processing = false;
	}
	
	protected void DispatchDialog(String str) {
		if(mAutoReconnect) {
			if(mAutoReconnectAttempt < mAutoReconnectLimit) {
				mAutoReconnectAttempt++;
				killNetThreads(true);
				String message = "\n" + Colorizer.colorRed + "Network Error: "+str+"\n" + "Attmempting reconnect in 20 seconds. " + (mAutoReconnectLimit -mAutoReconnectAttempt) + " tries remaining."+Colorizer.colorWhite+"\n";
				handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_PROCESSORWARNING,(message)));
				handler.sendEmptyMessageDelayed(MESSAGE_RECONNECT, 20000);
				return;
			}
		}
		service.DispatchDialog(str);
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
		
		//long start = System.currentTimeMillis();
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
		
		try {
			//Integer i = mWindowCallbackMap.get(outputWindow);
			//if(i != null) {
			//for(int i=0;i<N;i++) {
				IWindowCallback c = windowCallbackMap.get(outputWindow);
		//		if(c.getName().equals(outputWindow)) {
				if(c!=null) {
					c.rawDataIncoming(data);
				} else {
					//mWindows.get(0).getBuffer().addBytesImplSimple(data);
				}
		//		}
		//	}
			//}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		
		//mWindowCallbacks.finishBroadcast();
		//}
		
		//long now = System.currentTimeMillis();
		//Log.e("Connection","TIMEPROFILE sendBytesToWindow:" + Long.toString(now -start) + " millis.");
		
	}
	

	private void doStartup() {
		//if(pump != null) {
		killNetThreads(true);
			
			//pump = new DataPumper(host,port,handler);
			//processor = null;
			//processor = new Processor(handler,the_settings.getEncoding(),service.getApplicationContext());
			//pump.start();
			//return; //already started up.
		//}
		//int tmpPort = 0;
		//String host = "";
		//String display = "";
		//loadConnectionData();
		
		pump = new DataPumper(host,port,handler);
		
		
		/*if(processor != null) {
			processor.reset();
			processor = null;
		}*/
		processor = new Processor(handler,the_settings.getEncoding(),service.getApplicationContext());
		//processor.setDebugTelnet(the_settings.isDebugTelnet());
		//boolean use = (Boolean)((BooleanOption)the_settings.getSettings().getOptions().findOptionByKey("use_gmcp")).getValue();
		//String support = (String)((StringOption)the_settings.getSettings().getOptions().findOptionByKey("gmcp_supports")).getValue();
		//processor.setUseGMCP(use);
		//processor.setGMCPSupports(support);
		
		initSettings();
		pump.start();
		loadGMCPTriggers();
		//show notification somehow.
		isConnected = true;
		
		service.showConnectionNotification(display,host,port);
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
				boolean x_regex = t.isInterpretAsRegex();
				String x_name = t.getName();
				String x_pattern = t.getPattern();
				int j = x_name.length();
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
	Matcher semiMatcher = semicolon.matcher("");
	StringBuffer commandBuilder = new StringBuffer();
	private String scriptBlock = "/";
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
		
		if(out.equals(";;")) {
			Data enter = new Data();
			enter.cmdString = ";"+crlf;
			enter.visString = ";";
			return enter;
		}
		//1 - chop up input up on semi's
		//String[] commands = null;
		
		List<String> list = null;
		
		if(the_settings.isSemiIsNewLine()) {
			//commands = semicolon.split(out);
			list = splitSemicolonSafe(out);
			
		} else {
			list = new ArrayList<String>();
			list.add(out);
		}
		StringBuffer holdover = new StringBuffer();
		//2 - for each unit		
		//ArrayList<String> list = new ArrayList<String>(Arrays.asList(commands));
		
		
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
				//2.5 run command through the global lua state
				Data d = null;
				
				if(cmd.startsWith(this.scriptBlock)) {
					the_settings.runLuaString(cmd.substring(this.scriptBlock.length(), cmd.length()));
				} else {
					d = ProcessCommand(cmd);
				}
				//3 - do special command processing.
				
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
									
									List<String> alias_cmds = null;
									if(the_settings.isSemiIsNewLine()) {
										alias_cmds = splitSemicolonSafe(tmpstr);
									} else {
										alias_cmds = new ArrayList<String>(1);  
										alias_cmds.add(tmpstr);
									}
									for(String alias_cmd : alias_cmds) {
										iterator.add(alias_cmd);
									}
									if(reprocess) {
										for(int ax=0;ax<alias_cmds.size();ax++) {
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
				d.visString = d.visString.substring(0,d.visString.length()-1);
			}
			if(!d.visString.endsWith(crlf)) {
				d.visString = d.visString + crlf;
			}
		//}
		//Log.e("BT","TO SERVER:" + d.cmdString);
		//Log.e("BT","TO WINDOW:" + d.visString);
		
		
		return d;
	}
	
	private List<String> splitSemicolonSafe(String out) {
		List<String> list = new ArrayList<String>();
		semiMatcher.reset(out);
		boolean matched = false;
		boolean append = false;
		//int lastLength = -1;
		while(semiMatcher.find()) {
			matched = true;
			commandBuilder.setLength(0);
			semiMatcher.appendReplacement(commandBuilder,"");
			if(commandBuilder.length() == 0) {
				append = true;
				list.add(list.remove(list.size()-1) + ";");
			} else {
				if(append) {
					list.add(list.remove(list.size()-1) + commandBuilder.toString());
					append = false;
				} else {
					list.add(commandBuilder.toString());
				}
				
			}
		} 
		
		if(!matched) {
			list.add(out);
		} else {
			commandBuilder.setLength(0);
			semiMatcher.appendTail(commandBuilder);
			if(append) {
				list.add(list.remove(list.size()-1) + commandBuilder.toString());
			} else {
				list.add(commandBuilder.toString());
			}
		}
		
		commandBuilder.setLength(0);
		return list;
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
	private Integer mAutoReconnectLimit;
	private Integer mAutoReconnectAttempt = 0;
	private Boolean mAutoReconnect;
	
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
					ScriptData d = p.getSettings().getScripts().get(name);
					return d.getData();
				} else {
					return "";
				}
			}
		}
		
		//if we are here, then it means the main window callback has attempted to load a script.
		//if(the_settings.getSettings().getName().equals(plugin)) {
			if(the_settings.getSettings().getScripts().containsKey(name)) {
				ScriptData d = the_settings.getSettings().getScripts().get(name);
				return d.getData();
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
	
	/*public void saveSettings(String filename)  {
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
		
	}*/

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
				p.getSettings().setDirty(true);
				buildTriggerSystem();
			}
		}
	}
	
	public void setTriggerEnabled(boolean enabled,String key) {
		TriggerData data = the_settings.getSettings().getTriggers().get(key);
		if(data != null) {
			data.setEnabled(enabled);
			buildTriggerSystem();
		}
	}

	public void deletePluginTrigger(String selectedPlugin, String which) {
		Plugin p = pluginMap.get(selectedPlugin);
		if(p != null) {
			p.getSettings().getTriggers().remove(which);
			p.getSettings().setDirty(true);
			p.sortTriggers();
		}
		buildTriggerSystem();
	}

	public void deleteTrigger(String which) {
		the_settings.getSettings().getTriggers().remove(which);
		the_settings.sortTriggers();
		buildTriggerSystem();
	}

	public void setAliases(Map map) {
		the_settings.getSettings().setAliases((HashMap<String,AliasData>)map);
		the_settings.buildAliases();
	}
	
	public void setPluginAliases(String plugin,Map map) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.getSettings().setAliases((HashMap<String,AliasData>)map);
			p.getSettings().setDirty(true);
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
				p.getSettings().setDirty(true);
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

	public void startReconnect() {
		handler.sendEmptyMessage(MESSAGE_RECONNECT);
	}
	
	public void doReconnect() {
		if(pump != null) {
			if(pump.handler != null) {
				pump.handler.sendEmptyMessage(DataPumper.MESSAGE_END);
			}
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
			p.getSettings().setDirty(true);
		}
	}

	public void updatePluginTimer(String plugin, TimerData old,
		TimerData newtimer) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.getSettings().getTimers().remove(old.getName());				
			p.getSettings().getTimers().put(newtimer.getName(), newtimer);
			p.getSettings().setDirty(true);
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
		the_settings.getSettings().setDirty(true);
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
		if(the_settings == null) return new SettingsGroup();
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
		//synchronized(callbackSync) {
		//int N = mWindowCallbacks.beginBroadcast();
			//for(WindowToken w : mWindows) {
			//	if(w.getName().equals(window)) {
			//		w.getSettings().setOption(key, value);
			//	}
			//}
		//for(int i=0;i<N;i++) {
			IWindowCallback callback = windowCallbackMap.get(window);
			if(callback == null) return;
			try{
				//if(callback.getName().equals(window)) {
					callback.updateSetting(key,value);
				//}
			} catch (RemoteException e) {
				
			}
		//}
		
		//mWindowCallbacks.finishBroadcast();
		//}
		
	}

	@Override
	public void updateSetting(String key, String value) {
		if(the_settings == null) return; //this is for when the settings are first being loaded.
		BaseOption o = (BaseOption)the_settings.getSettings().getOptions().findOptionByKey(key);
		try {
			KEYS tmp = KEYS.valueOf(key);
			switch(tmp) {
			case process_semicolon:
				the_settings.setSemiIsNewLine((Boolean)o.getValue());
				break;
			case debug_telnet:
				processor.setDebugTelnet((Boolean)o.getValue());
				break;
			case encoding:
				this.doUpdateEncoding((String)o.getValue());
				break;
			case orientation:
				service.doExecuteSetOrientation((Integer)o.getValue());
				break;
			case screen_on:
				service.doExecuteKeepScreenOn((Boolean)o.getValue());
				break;
			case fullscreen:
				service.doExecuteFullscreen((Boolean)o.getValue());
				break;
			case fullscreen_editor:
				service.doExecuteFullscreenEditor((Boolean)o.getValue());
				break;
			case use_suggestions:
				service.doExecuteUseSuggestions((Boolean)o.getValue());
				break;
			case keep_last:
				this.doSetKeepLast((Boolean)o.getValue());
				break;
			case compatibility_mode:
				service.doExecuteCompatibilityMode((Boolean)o.getValue());
				break;
			case local_echo:
				this.doSetLocalEcho((Boolean)o.getValue());
				break;
			case process_system_commands:
				this.doSetProcessSystemCommands((Boolean)o.getValue());
				break;
			case echo_alias_updates:
				this.doSetAliasUpdates((Boolean)o.getValue());
				break;
			case keep_wifi_alive:
				this.doSetKeepWifiAlive((Boolean)o.getValue());
				break;
			case auto_reconnect:
				this.setAutoReconnect((Boolean)o.getValue());
				break;
			case auto_reconnect_limit:
				this.setAutoReconnectLimit((Integer)o.getValue());
				break;
			case cull_extraneous_color:
				this.doSetCullExtraneousColor((Boolean)o.getValue());
				break;
			case debug_telent:
				this.doSetDebugTelnet((Boolean)o.getValue());
				break;
			case bell_vibrate:
				this.doSetBellVibrate((Boolean)o.getValue());
				break;
			case bell_notification:
				this.doSetBellNotify((Boolean)o.getValue());
				break;
			case bell_display:
				this.doSeBellDisplay((Boolean)o.getValue());
				break;
			case use_gmcp:
				this.doSetUseGMCP((Boolean)o.getValue());
				break;
			case gmcp_supports:
				this.doSetGMCPSupports((String)o.getValue());
				break;
			}
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	private void doSetGMCPSupports(String value) {
		processor.setGMCPSupports(value);
	}

	private void doSetUseGMCP(Boolean value) {
		processor.setUseGMCP(value);
	}

	private void setAutoReconnectLimit(Integer value) {
		mAutoReconnectLimit = value;
	}

	private void setAutoReconnect(Boolean value) {
		mAutoReconnect = value;
	}

	private void doSetBellVibrate(Boolean value) {
		the_settings.setVibrateOnBell(value);		
	}
	
	private void doSetBellNotify(Boolean value) {
		the_settings.setNotifyOnBell(value);
	}
	
	private void doSeBellDisplay(Boolean value) {
		the_settings.setDisplayOnBell(value);
	}

	private void doSetDebugTelnet(Boolean value) {
		the_settings.setDebugTelnet(value);
		if( processor != null) {
			processor.setDebugTelnet(value);
		}
	}

	private void doSetCullExtraneousColor(Boolean value) {
		the_settings.setRemoveExtraColor(value);
		mWindows.get(0).getBuffer().setCullExtraneous(value);
	}

	private void doSetKeepWifiAlive(Boolean value) {
		the_settings.setKeepWifiActive(value);
		if(value) {
			service.EnableWifiKeepAlive();
		} else {
			service.DisableWifiKeepAlive();
		}
	}

	private void doSetAliasUpdates(Boolean value) {
		the_settings.setEchoAliasUpdates(value);
	}

	private void doSetProcessSystemCommands(Boolean value) {
		the_settings.setProcessPeriod(value);
	}

	private void doSetLocalEcho(Boolean value) {
		the_settings.setLocalEcho(value);
	}

	private void doSetKeepLast(Boolean value) {
		service.dispatchKeepLast(value);
	}

	private void doUpdateEncoding(String value) {
		processor.setEncoding(value);
		//this.encoding = value;
		the_settings.setEncoding(value);
		this.working.setEncoding(value);
		this.finished.setEncoding(value);
		if(processor != null) {
			this.processor.setEncoding(value);
		}
		for(int i=0;i<mWindows.size();i++) {
			WindowToken w = mWindows.get(i);
			w.getBuffer().setEncoding(value);
		}
		
		//synchronized(callbackSync) {
			//int N = mWindowCallbacks.beginBroadcast();
			
			for(IWindowCallback w : windowCallbackMap.values()) {
				//IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
				try {
					w.setEncoding(value);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//mWindowCallbacks.finishBroadcast();
		//}
		
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
		process_semicolon,
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
		bell_display, auto_reconnect, auto_reconnect_limit, use_gmcp, gmcp_supports
	}
	
	private void sendToServer(byte[] bytes) {
		//byte[] bytes = (byte[]) msg.obj;
		if(bytes == null || the_settings == null) {
			return;
		}
		Data d = null;
		try {
			d = ProcessOutputData(new String(bytes,the_settings.getEncoding()));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		if(d == null) {
			return;
		}
		
		if(d.cmdString.equals("") && (d.visString != null && d.visString.replaceAll("\\s", "").equals(""))) {
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
				
				if(pump != null && pump.isConnected()) {
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
					mWindows.get(0).getBuffer().addBytesImplSimple(d.visString.getBytes(the_settings.getEncoding()));
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
	
	Pattern xmlext = Pattern.compile("^.+\\.[xX][mM][lL]$");
	Matcher xmlmatch = xmlext.matcher("");
	public void exportSettings(String filename) {
		boolean domessage = false;
		boolean addextra = false;
		if(filename.startsWith("/")) {
			//dont mod
		} else {
			//mod
			domessage = true;
			File ext = Environment.getExternalStorageDirectory();
			String dir = ConfigurationLoader.getConfigurationValue("exportDirectory", service.getApplicationContext());
			
			filename = ext.getAbsolutePath() + "/" + dir + "/"+filename;
			xmlmatch.reset(filename);
			if(!xmlmatch.matches()) {
				filename = filename + ".xml";
				addextra = true;
			}
		}
		try {
			File file = new File(filename);
			FileOutputStream fos = new FileOutputStream(file);
			String foo = ConnectionSetttingsParser.outputXML(the_settings,plugins);
			fos.write(foo.getBytes());
			fos.close();
			
			for(String link : linkMap.keySet()) {
				ArrayList<String> plugins  = linkMap.get(link);
				boolean doExport = false;
				String fullpath = "";
				for(String plugin : plugins) {
					Plugin p = pluginMap.get(plugin);
					if(p.getSettings().isDirty()) {
						doExport = true;
						fullpath = p.getFullPath();
					}
				}
				
				if(doExport) {
					XmlSerializer out = Xml.newSerializer();
					StringWriter writer = new StringWriter();
					
					out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
					out.setOutput(writer);
					out.startDocument("UTF-8", true);
					out.startTag("", "blowtorch");
					out.attribute("", "xmlversion", "2");
					out.startTag("", "plugins");
					for(String plugin :plugins) {
						Plugin p = pluginMap.get(plugin);
						PluginParser.saveToXml(out, p);
						p.getSettings().setDirty(false);
					}
					
					out.endTag("", "plugins");
					out.endTag("", "blowtorch");
					out.endDocument();
					
					File extfile = new File(fullpath);
					FileOutputStream extfilestream = new FileOutputStream(extfile);
					extfilestream.write(writer.toString().getBytes());
					extfilestream.close();
				}
				
			}
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
		
		if(domessage) {
			String message = "Settings Exported to " + filename;
			if(addextra) {
				message = message + "\n.xml extension added.";
			}
			service.DispatchToast(message, true);
		}
	}
	
	public void startExportSequence(String path) {
		handler.sendMessage(handler.obtainMessage(MESSAGE_EXPORTFILE, path));
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
	
	private void importSettings(String path,boolean save,boolean loadmessage) {
		shutdownPlugins();
		
		String verb = null;
		if(loadmessage) {
			verb = "Loading";
		} else {
			verb = "Importing";
		}
		
		VersionProbeParser vpp = new VersionProbeParser(path,service.getApplicationContext());

		
		try {
			boolean isLegacy = vpp.isLegacy();
			if(isLegacy) {
				Log.e("XMLPARSE","LOADING V1 SETTINGS FROM PATH: "+path);
				HyperSAXParser p = new HyperSAXParser(path,service.getApplicationContext());
				HyperSettings s = p.load();
				
				//load up the default settings and then merge the old settings into the new settings.
				ArrayList<Plugin> tmpplugs = new ArrayList<Plugin>();
				ConnectionSetttingsParser newsettings = new ConnectionSetttingsParser(null,service.getApplicationContext(),tmpplugs,handler,this);
				tmpplugs = newsettings.load(this);
				//the_settings = (ConnectionSettingsPlugin) tmpplugs.get(0);
				
				ConnectionSettingsPlugin global = (ConnectionSettingsPlugin)tmpplugs.get(0);
				
				//global.importV1Settings(s);
				
				Plugin buttonwindow = tmpplugs.get(1);
				
				if(path != null) { //import old buttons
				
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
						
						if(button.getTargetSet() != null && !button.getTargetSet().equals("")) {
							L.pushString("switchTo");
							L.pushString(button.getTargetSet());
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
				
				} else {
					//run the adjustment for the new buttons
					LuaState L = buttonwindow.getLuaState();
					L.getGlobal("debug");
					L.getField(-1, "traceback");
					L.getGlobal("alignDefaultButtons");
					if(L.isFunction(-1)) {
						int ret = L.pcall(0, 1, -2);
						if(ret != 0) {
							this.dispatchLuaError(L.getLuaObject(-1).getString());
						}
					} else {
						L.pop(1);
					}
				}
				//s.getSetSettings();
				
				
				
				
				WindowToken tmp = the_settings.getSettings().getWindows().get("mainDisplay");
				tmp.importV1Settings(s);
				
				//handle button settings.
				SettingsGroup buttonops = buttonwindow.getSettings().getOptions();
				String hfedit = s.getHapticFeedbackMode();
				if(hfedit.equals("auto")) {
					buttonops.setOption("haptic_edit", Integer.toString(0));
				} else if(hfedit.equals("always")) {
					buttonops.setOption("haptic_edit", Integer.toString(1));
				} else if(hfedit.equals("none")) {
					buttonops.setOption("haptic_edit", Integer.toString(2));
				}
				
				String hfpress = s.getHapticFeedbackOnPress();
				if(hfpress.equals("auto")) {
					buttonops.setOption("haptic_press", Integer.toString(0));
				} else if(hfpress.equals("always")) {
					buttonops.setOption("haptic_press", Integer.toString(1));
				} else if(hfpress.equals("none")) {
					buttonops.setOption("haptic_press", Integer.toString(2));
				}
				
				String hfflip = s.getHapticFeedbackOnFlip();
				if(hfflip.equals("auto")) {
					buttonops.setOption("haptic_flip", Integer.toString(0));
				} else if(hfflip.equals("always")) {
					buttonops.setOption("haptic_flip", Integer.toString(1));
				} else if(hfflip.equals("none")) {
					buttonops.setOption("haptic_flip", Integer.toString(2));
				}
				String summary = Colorizer.colorWhite + verb + " legacy settings file.\n";
				loadPlugins(tmpplugs,summary);
				the_settings.importV1Settings(s);
				if(!s.isRoundButtons()) {
					buttonops.setOption("button_roundness", Integer.toString(0));
				} 
			} else {
				int version = vpp.getVersionNumber();
				if(version == 2) {
					Log.e("XMLPARSE","LOADING V2 SETTINGS FROM PATH: "+path);
					ArrayList<Plugin> tmpplugs = new ArrayList<Plugin>();
					ConnectionSetttingsParser csp = new ConnectionSetttingsParser(path,service.getApplicationContext(),tmpplugs,handler,this);
					tmpplugs = csp.load(this);
					if(path == null) {
						Plugin buttonwindow = tmpplugs.get(1);
						//LuaState L = buttonwindow.getLuaState();
						LuaState L = buttonwindow.getLuaState();
						L.getGlobal("debug");
						L.getField(-1, "traceback");
						L.getGlobal("alignDefaultButtons");
						if(L.isFunction(-1)) {
							int ret = L.pcall(0, 1, -2);
							if(ret != 0) {
								Connection.this.dispatchLuaError("ERROR IN DEFAULT BUTTONS:"+(L.getLuaObject(-1).getString()));
							}
						} else {
							L.pop(1);
						}
					}
					String summary = Colorizer.colorWhite + verb + " settings file.\n";
					loadPlugins(tmpplugs,summary);
				} else {
					Log.e("XMLPARSE","ERROR IN LOADING V2 SETTINGS, DID NOT FIND PROPER XMLVERSION NUMBER");
					try {
						service.dispatchXMLError("Error "+verb.toLowerCase()+" settings, invalid or missing version attribute.\n");
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
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
			try {
				service.dispatchXMLError(e.getLocalizedMessage());
				return;
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if(path == null) {
			the_settings.getSettings().getOptions().setOption("font_size", Integer.toString(calculate80CharFontSize()));
		
		}
		
		buildTriggerSystem();
		if(save) {
			this.saveMainSettings();
		}
	}
	
	private void loadInternalSettings() {
		Long start = System.currentTimeMillis();
		
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
			importSettings(null,false,true);
		} else {
			importSettings(rootPath,false,true);
		}

		Long end = System.currentTimeMillis();
		int dur = (int) (end - start);
		//Log.e("Connection","Took" + dur + " to load settings.");
	}
	
	
	private void buildSettingsPage() {
		if(the_settings.getSettings().getWindows().size() < 1) {
			WindowToken token = new WindowToken(MAIN_WINDOW,null,null,display);
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
		
		the_settings.buildAliases();
		for(Plugin pl : plugins) {
			pl.buildAliases();
		}
		
		buildTriggerSystem();
		mWindows.get(0).getSettings().setListener(new WindowSettingsChangedListener(mWindows.get(0).getName()));
		the_settings.getSettings().getOptions().addOptionAt(mWindows.get(0).getSettings(),4);
		
		
		//go through all the plugins and add thier options if they have them.
		/*for(Plugin p : plugins) {
			if(p.getSettings().getWindows().size() > 0) {
				for(String token : p.getSettings().getWindows().keySet()) {
					WindowToken w = p.getSettings().getWindows().get(token);
					p.getSettings().getOptions().addOption(w.getSettings());
				}
			}
			
			if(p.getSettings().getOptions().getOptions().size() > 0) {
				the_settings.getSettings().getOptions().addOption(p.getSettings().getOptions());
			}
		}*/
		
		//the_settings.getSettings().setOptions(sg);
	}
	
	public void attatchWindowSettingsChangedListener(WindowToken w) {
		w.getSettings().setListener(new WindowSettingsChangedListener(w.getName()));
	}

	public boolean isKeepLast() {
		return (Boolean)((BooleanOption)the_settings.getSettings().getOptions().findOptionByKey("keep_last")).getValue();
	}

	public boolean isFullScren() {
		return (Boolean)((BooleanOption)the_settings.getSettings().getOptions().findOptionByKey("fullscreen")).getValue();
	}

	public String getHostName() {
		// TODO Auto-generated method stub
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	private int calculate80CharFontSize() {
		int windowWidth = service.getResources().getDisplayMetrics().widthPixels;
		if(service.getResources().getDisplayMetrics().heightPixels > windowWidth) {
			windowWidth = service.getResources().getDisplayMetrics().heightPixels;
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
	
	private void initSettings() {
		initSetting(the_settings.getSettings().getOptions());
	}
	
	private void initSetting(SettingsGroup s) {
		for(Option o : s.getOptions()) {
			if(o instanceof SettingsGroup) {
				initSetting((SettingsGroup)o);
			} else {
				BaseOption tmp = (BaseOption)o;
				this.updateSetting(o.getKey(), tmp.getValue().toString());
			}
		}
	}

	public void resetSettings() {
		this.handler.sendEmptyMessage(MESSAGE_DORESETSETTINGS);
	}
	
	public void doResetSettings() {
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
		importSettings(null,true,true);
	}

	public void startLoadSettingsSequence(String path) {
		handler.sendMessage(handler.obtainMessage(MESSAGE_IMPORTFILE,path));
	}
	
	public void doAddLink(String path) {
		the_settings.getLinks().add(path);
		saveMainSettings();
		reloadSettings();
	}
	
	public void addLink(String path) {
		handler.sendMessage(handler.obtainMessage(MESSAGE_ADDLINK,path));
	}

	private void doDeletePlugin(String plugin) {
		Plugin p = pluginMap.remove(plugin);
		String remove = null;
		if(p.getStorageType().equals("EXTERNAL")) {
			for(String path : the_settings.getLinks()) {
				if(p.getFullPath().contains(path)) {
					//the_settings.getLinks().remove(path);
					//linkMap.remove(path);
					remove = path;
				}
			}
		}
		if(remove == null) { return; } //no plugin of that name path exists
		the_settings.getLinks().remove(remove);
		linkMap.remove(remove);
		plugins.remove(p);
		//the_settings.get
		saveMainSettings();
		reloadSettings();
	}
	public void deletePlugin(String plugin) {
		handler.sendMessage(handler.obtainMessage(MESSAGE_DELETEPLUGIN,plugin));
	}

	public void setPluginEnabled(String plugin, boolean enabled) {
		Plugin p = pluginMap.get(plugin);
		p.setEnabled(enabled);
		saveMainSettings();
		reloadSettings();
	}

	public Map getDirectionData() {
		// TODO Auto-generated method stub
		return the_settings.getDirections();
	}

	public void setDirectionData(Map data) {
		the_settings.setDirections((HashMap<String, DirectionData>) data);
	}

	public int getTitleBarHeight() {
		// TODO Auto-generated method stub
		return titleBarHeight;
	}

	public int getStatusBarHeight() {
		// TODO Auto-generated method stub
		return statusBarHeight;
	}

	public boolean isLinkLoaded(String link) {
		// TODO Auto-generated method stub
		String foo = Environment.getExternalStorageDirectory() + "/BlowTorch/";
		String bar = link.replace(foo, "");
		
		boolean ret = linkMap.containsKey(bar);
		//Plugin p = pluginMap.get()
		return ret;
	}

	public void initWindows() {
		// TODO Auto-generated method stub
		loadInternalSettings();
	}

	public void shutdown() {
		this.saveMainSettings();
		this.killNetThreads(true);
		for(Plugin p : plugins) {
			p.shutdown();
			p = null;
		}
		
		
		
		the_settings.shutdown();
		the_settings = null;
		
		handler.removeMessages(MESSAGE_RECONNECT);
		handler = null;
		
		service.removeConnectionNotification(display);
		
	}

	public String getPluginPath(String plugin) {
		Plugin p = pluginMap.get(plugin);
		return p.getFullPath();
	}

	public void dispatchLuaText(String str) {
		handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_LUANOTE,str));
		//Log.e("lua text","lua text: " + str);
	}

	public void callPluginFunction(String plugin, String function) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			p.callFunction(function);
		} else {
			this.dispatchLuaText("\n"+Colorizer.colorRed+"No plugin named: "+plugin+Colorizer.colorRed+"\n");
		}
	}

	@Override
	public SettingsChangedListener getSettingsListener() {
		// TODO Auto-generated method stub
		return (SettingsChangedListener)this;
	}

	@Override
	public void callPlugin(String plugin, String function, String data) {
		Message m = handler.obtainMessage(MESSAGE_CALLPLUGIN);
		m.getData().putString("PLUGIN",plugin);
		m.getData().putString("FUNCTION", function);
		m.getData().putString("DATA", data);
		handler.sendMessage(m);
	}

	@Override
	public boolean pluginSupports(String plugin,String function) {
		Plugin p = pluginMap.get(plugin);
		if(p != null) {
			return p.checkPluginSupports(function);
		}
		return false;
	}

	public boolean isPluginInstalled(String desired) {
		return pluginMap.containsKey(desired);
	}
	
	private class TimerCommand extends SpecialCommand {
	ArrayList<String> timer_actions = new ArrayList<String>();
	public TimerCommand() {
		this.commandName = "timer";
		timer_actions.add("play");
		timer_actions.add("pause");
		timer_actions.add("info");
		timer_actions.add("reset");
		timer_actions.add("stop");
	}
	public Object execute(Object o,Connection c)  {
		//example argument " info 0"
		//regex = "^\s+(\S+)\s+(\d+)";
		Pattern p = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s*(\\S*)");
		
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
				//try {
					dispatchNoProcess(getErrorMessage("Timer action arguemnt " + action + " is invalid.","Acceptable arguments are \"play\",\"pause\",\"reset\",\"stop\" and \"info\".").getBytes());
				//} catch (RemoteException e) {
				//	throw new RuntimeException(e);
				//}
				return null;
			}
			
			//try {
			//	pOrdinal = Integer.parseInt(ordinal);
			//	pOrdinal = pOrdinal + 1;
			//} catch (NumberFormatException e) {
				//try {
			//		dispatchNoProcess(getErrorMessage("Timer index argument " + ordinal + " is not a number.","Acceptable argument is an integer.").getBytes());
			//		return null;
				//} catch (RemoteException e1) {
				//	throw new RuntimeException(e);
				//}
			//}
			
			//if(the_settings.getSettings().getTimers().containsKey(ordinal)) {
				//valid timer.
				int domsg = 50;
				//Log.e("SERVICE","SILENT IS " + silent);
				if(!silent.equals("")) {
					domsg = 0;
				}
				
				if(action.equals("info")) {
					handler.sendMessage(handler.obtainMessage(MESSAGE_TIMERINFO, ordinal));
					return null;
				}
				if(action.equals("reset")) {
					handler.sendMessage(handler.obtainMessage(MESSAGE_TIMERRESET, 0, domsg, ordinal));
					return null;
				}
				if(action.equals("play")) {
					//play
					handler.sendMessage(handler.obtainMessage(MESSAGE_TIMERSTART,0,domsg,ordinal));
					return null;
				}
				if(action.equals("pause")) {
					handler.sendMessage(handler.obtainMessage(MESSAGE_TIMERPAUSE, 0, domsg, ordinal));
					return null;
				}
				if(action.equals("stop")) {
					handler.sendMessage(handler.obtainMessage(MESSAGE_TIMERSTOP, 0, domsg, ordinal));
					return null;
				}
				
				
			//} else {
				//invalid timer
				//try {
					//dispatchNoProcess(getErrorMessage("Timer at index " + ordinal + " does not exist.","The timer index is the number displayed next to the timer the timer selection screen.").getBytes());
				//} catch (RemoteException e) {
				//	throw new RuntimeException(e);
				//}
			//}
			
			
			
		} else {
			//try {
				dispatchNoProcess(getErrorMessage("Timer command: \".timer " + (String)o + "\" is invalid.","Timer function format \".timer action index [silent]\"\nWhere action is \"play\",\"pause\",\"reset\" or \"info\".\nIndex is the timer index displayed in the timer selection list.").getBytes());
			//} catch (RemoteException e) {
			//	throw new RuntimeException(e);
			//}
		}
		
		return null;
		
	}
	}
	
	private void doTimerAction(String obj, int arg2, TIMER_ACTION action) {
		//check for valid ordinals.
		boolean found = false;
		Plugin host = null;
		if(the_settings.getSettings().getTimers().containsKey(obj)) {
			host = the_settings;
			found = true;
		} else {
			//check plugins
			for(Plugin p : plugins) {
				if(p.getSettings().getTimers().containsKey(obj)) {
					host = p;
					found = true;
				}
			}
		}
		boolean silent = false;
		if(arg2 == 0) {
			silent = true;
		}
		
		if(!found) {
			//show error message.
			dispatchNoProcess(SpecialCommand.getErrorMessage("Timer command error","No timer with name "+obj+" found.").getBytes());
		} else {
			switch(action) {
			case PLAY:
				host.startTimer(obj);
				if(!silent) {
					toast("Timer "+obj+" started.");
				}
				break;
			case PAUSE:
				host.pauseTimer(obj);
				if(!silent) {
					toast("Timer "+obj+" paused.");
				}
				break;
			case RESET:
				host.resetTimer(obj);
				if(!silent) {
					toast("Timer "+obj+" reset.");
				}
				break;
			case STOP:
				host.pauseTimer(obj);
				host.resetTimer(obj);
				if(!silent) {
					toast("Timer "+obj+" stopped.");
				}
				break;
			case INFO:
				TimerData t = host.getSettings().getTimers().get(obj);
				if(t.isPlaying()) {
					long now = SystemClock.elapsedRealtime();
					long dur = now - t.getStartTime();
					int sec = t.getSeconds() - (int) (dur/1000);
					//Log.e("TIMER","Timer "+obj+" is playing, "+sec+" remaining.");
					toast(obj+": "+sec+"s");
				} else {
					if(t.getRemainingTime() != t.getSeconds()) {
						int sec = t.getSeconds() - t.getRemainingTime();
						//Log.e("TIMER","Timer "+obj+" is paused, " + sec + " remain.");
						toast("Timer "+obj+" is paused, " + sec + " remain.");
					} else {
						//Log.e("TIMER","Timer "+obj+" is not playing.");
						toast("Timer "+obj+" is not running.");
					}
				}
				//long time = host.getSettings().getTimers().get(obj).getStartTime();
				
				//goooodie
				break;
			case NONE:
				break;
			}
		}
	}
	
	private void toast(String str) {
		Context c = this.getContext();
		//String translated = ToastResponder.this.translate(message, captureMap);
		Toast t = Toast.makeText(c, str, 0);
		float density = c.getResources().getDisplayMetrics().density;
		t.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int) (50*density));
		t.show();
	}
	
}
