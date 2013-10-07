/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keplerproject.luajava.LuaState;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.responder.IteratorModifiedException;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.gag.GagAction;
import com.offsetnull.bt.responder.script.ScriptResponder;
import com.offsetnull.bt.script.ScriptData;
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
import com.offsetnull.bt.service.plugin.settings.ConnectionSetttingsParser;
import com.offsetnull.bt.service.plugin.settings.Option;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.service.plugin.settings.VersionProbeParser;
import com.offsetnull.bt.settings.ColorSetSettings;
import com.offsetnull.bt.settings.ConfigurationLoader;
import com.offsetnull.bt.settings.HyperSAXParser;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.speedwalk.DirectionData;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.window.TextTree.Line;
import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.button.SlickButtonData;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;

import android.util.Log;
import android.util.SparseArray;
//import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.Toast;

/** Connection class implementation. */
public class Connection implements SettingsChangedListener, ConnectionPluginCallback {
	
	/** Initiates the connection with the server. */
	public static final int MESSAGE_STARTUP = 1;
	
	/** Bridge between Processor and DataPumper to initiate MCCP compression. */
	public static final int MESSAGE_STARTCOMPRESS = 2;
	
	/** Sent by various objects to throw text to the window bypassing the trigger parse routine. */
	public static final int MESSAGE_PROCESSORWARNING = 3;
	
	/** Sent from the Processor to send data to the DataPumper's output thread. */
	public static final int MESSAGE_SENDOPTIONDATA = 4;
	
	/** Sent from the Processor indicating the bell character has been recieved. */
	public static final int MESSAGE_BELLINC = 5;
	
	/** Not sure where this is sent from, I think the Xml parser or the datapumper. 
	 ** Used to put up an alert dialog with some text to the foreground window if connected. */
	public static final int MESSAGE_DODIALOG = 6;
	
	/** Sent from Processor, contains the non-telnet related data in the incoming transmission. */
	public static final int MESSAGE_PROCESS = 7;
	
	/** Sent from the DataPumper when the connection has been lost. */
	public static final int MESSAGE_DISCONNECTED = 8;
	
	/** Sent from the DataPumper indicating a fatal mccp error. */
	public static final int MESSAGE_MCCPFATALERROR = 9;
	
	/** Sent from the foreground window with data to be sent to the server. */
	public static final int MESSAGE_SENDDATA_BYTES = 9;
	
	/** Sent from Plugin.LineToWindowFunction contains a 
	 ** TextTree.Line object to send to a specific window. */
	public static final int MESSAGE_LINETOWINDOW = 10;
	
	/** Sent from Plugin.NoteFunction with text to send to the output window. */
	public static final int MESSAGE_LUANOTE = 11;
	
	/** Sent from Plugin.DrawWindowFunction, I think this is no longer used and deprecated. */
	public static final int MESSAGE_DRAWINDOW = 12;
	
	/** Sent from Plugin.NewWindowFucntion used to create a new 
	 * miniwindow with then given configuration. */
	public static final int MESSAGE_NEWWINDOW = 13;
	
	/** Sent from Plugin.WindowBufferFunction and sets the buffering 
	 * option of a window. I think this is deprecated. */
	public static final int MESSAGE_WINDOWBUFFER = 15;
	
	/** Sent from Plugin.RegisterSpecialCommandFunction and registers a .command callback. */
	public static final int MESSAGE_ADDFUNCTIONCALLBACK = 16;
	
	/** Sent from Plugin.WindowXCallSFunction, calls an anonymous global function
	 *  in the target window with data. */
	public static final int MESSAGE_WINDOWXCALLS = 17;
	
	/** Sent from plugin functions, used to redraw a target window. */
	public static final int MESSAGE_INVALIDATEWINDOWTEXT = 18;
	
	/** Sent from Processor indicating that gmcp has triggered. */
	public static final int MESSAGE_GMCPTRIGGERED = 19;
	
	/** Sent from various sources, containing a string to be sent to 
	 * the server in the selected encoding. */
	public static final int MESSAGE_SENDDATA_STRING = 20;
	
	/** Sent from various sources, initates the settings loader routine. */
	public static final int MESSAGE_SAVESETTINGS = 21;
	
	/** Sent from either the foreground window or the save settings routine, 
	 * exports settings to a given path. */
	public static final int MESSAGE_EXPORTFILE = 22;
	
	/** Sent from either the foreground window or the settings loader routing, 
	 * imports settings from a given location into this active connection. */
	public static final int MESSAGE_IMPORTFILE = 23;
	
	/** Sent from Plugin.SendGMCPDataFunction, sends the given data to the server using gmcp. */
	public static final int MESSAGE_SENDGMCPDATA = 24;
	
	/** Sent from Plugin.WindowXCallB, same as WindowXCallS only great care is taken
	 *  to preserve the "bytes". */
	public static final int MESSAGE_WINDOWXCALLB = 25;
	
	/** Sent from Plugin, indicating an error condition from a script entry point. */
	public static final int MESSAGE_PLUGINLUAERROR = 26;

	/** Sent from DataPumper indicating that the tcp connection to the server has started. */
	public static final int MESSAGE_CONNECTED = 30;
	
	/** Sent from AckWithResponder when a trigger executes script code that results in error. */
	public static final int MESSAGE_TRIGGER_LUA_ERROR = 32;
	
	/** Sent from the foreground window, initiates the settings reloading process. */
	public static final int MESSAGE_RELOADSETTINGS = 33;
	
	/** Sent from various sources, indicates that triggers need to be rebuilt, 
	 * I believe this is deprecated. */
	public static final int MESSAGE_SETTRIGGERSDIRTY = 34;
	
	/** Sent from the DataPumper indicating the orderly shutdown of the tcp connection. */
	public static final int MESSAGE_TERMINATED_BY_PEER = 41;
	
	/** Sent from the foreground window indicating that the DataPumper
	 *  should re-establish the tcp connection to the server. */
	private static final int MESSAGE_RECONNECT = 31;
	
	/** Sent from the foreground window, initates a settings reset. */
	private static final int MESSAGE_DORESETSETTINGS = 27;
	
	/** Sent from the foreground window, adds an external plugin at the given path. */
	private static final int MESSAGE_ADDLINK = 28;
	
	/** Sent from the foreground window, deletes and removes a plugin. */
	private static final int MESSAGE_DELETEPLUGIN = 29;

	/** Sent from Plugin.CallPlugin calls an anonymous global function in the target
	 *  plugin with arguments. */
	private static final int MESSAGE_CALLPLUGIN = 35;
	
	/** Sent from the timer command. */
	private static final int MESSAGE_TIMERINFO = 36;
	
	/** Sent from the timer command. */
	private static final int MESSAGE_TIMERSTART = 37;
	
	/** Sent from the timer command. */
	private static final int MESSAGE_TIMERPAUSE = 38;
	
	/** Sent from the timer command. */
	private static final int MESSAGE_TIMERRESET = 39;
	
	/** Sent from the timer command. */
	private static final int MESSAGE_TIMERSTOP = 40;
	
	/** GMCP payload minimum size. */
	private static final int GMCP_PAYLOAD_SIZE = 5;
	
	/** The value of 4. */
	private static final int FOUR = 4;
	
	/** 500 ms timeout, generic timeout or other. */
	private static final int FIVE_HUNDRED_MILLIS = 500;
	
	/** 1000 mm. */
	private static final double ONE_THOUSAND_MILLIS = 1000.0;
	
	/** Toast message offset from the top of the screen. */
	private static final double TOAST_MESSAGE_TOP_OFFSET = 50.0;
	/** Very large value. */
	private static final int TEN_MILLION = 10000000;
	
	/** Medium large value. */
	private static final int TEN_THOUSAND = 10000;
	
	/** 3 seconds. */
	private static final int THREE_THOUSAND_MILLIS = 3000;
	
	/** 20 seconds. */
	private static final int TWENTY_THOUSAND_MILLIS = 20000;
	
	/** Minimum starting font size for the fit routine. */
	private static final float MIN_FONT_SIZE = 8.0f;
	
	/** Target char width for fit routine. */
	private static final float TARGET_FIT_WIDTH = 80.0f;
	
	/** Status bar height holder. */
	private static final int STATUS_BAR_DEFAULT_SIZE = 25;
	
	/** Value of -3. */
	private static final int NEGATIVE_THREE = -3;
	
	/** Value of -2. */
	private static final int NEGATIVE_TWO = -2;
	
	/** ANSI Color code pattern. */
	private static final Pattern COLOR_PATTERN = Pattern.compile("\\x1B\\x5B.+?m");
	
	/** ANSI Color code matcher. */
	private static final Matcher COLOR_MATCHER = COLOR_PATTERN.matcher("");
	
	/** Generic "match a line" pattern. */
	private static final Pattern LINE_PATTERN = Pattern.compile("^.*$", Pattern.MULTILINE);
	
	/** Line matching matcher. */
	private static final Matcher LINE_MATCHER = LINE_PATTERN.matcher("");
	


	
	/** The configurable character denoting that the input to follow should be executed as a script. */
	private static String mScriptBlock = "/";
	
	/** String name of the default output window. */
	private static final String MAIN_WINDOW = "mainDisplay";
	
	/** Constant indicating that one or more triggers is invalid and the trigger system should be rebuilt
	 * on the next pass of dispatch().
	 */
	private static boolean triggersDirty = false;
	/** This variable is used in conjunction with mWindowCallbackMap to track IWindowCallback aidl connections
	 * window names.
	 */
	private boolean mCallbacksStarted = false;
	/** String builder used by the alias parsing routine. */
	private final StringBuffer mDataToServer = new StringBuffer();
	/** String builder used by the alias parsing routine. */
	private final StringBuffer mDataToWindow = new StringBuffer();
	/** Semicolon matching pattern. */
	private final Pattern mSemicolon = Pattern.compile(";");
	/** Semicolon matcher. */
	private final Matcher mSemiMatcher = mSemicolon.matcher("");
	/** String builder used by the alias parsing routine. */
	private final StringBuffer mCommandBuilder = new StringBuffer();
	/** Used by the trigger processor to map line start/end to line number. */
	private final TreeSet<Range> mLineMap = new TreeSet<Range>(new RangeComparator());
	/** Used by the trigger building routine to build the new string with great speed. */
	private final StringBuilder mTriggerBuilder = new StringBuilder();

	/** A utiltiy object to keep track of the order of triggers. */
	private final SparseArray<TriggerData> mSortedTriggerMap = new SparseArray<TriggerData>(0);
	/** A utiltity object to keep track of the sorted order of plugins. */
	private final SparseArray<Plugin> mTriggerPluginMap = new SparseArray<Plugin>(0);
	/** Remote window callback map. Reduces overhead for needing to communicate with windows. */
	private final RemoteCallbackList<IWindowCallback> mWindowCallbacks = new RemoteCallbackList<IWindowCallback>();
	/** The list of window tokens in loaded order. */
	private ArrayList<WindowToken> mWindows;
	/** The auto reconnect limit helper varialbe. */
	private Integer mAutoReconnectLimit;
	/** The current auto reconnect attempt. */
	private Integer mAutoReconnectAttempt = 0;
	/** Weather or not we should auto reconnect on connection failure. */
	private Boolean mAutoReconnect;
	/** The amalgamated trigger string. Very long in most cases. */
	private String mMassiveTriggerString = null;
	/** The amalgamated trigger string pattern object. */
	private Pattern mMassivePattern = null;
	/** The amalgamated trigger string matcher object. */
	private Matcher mMassiveMatcher = null;
	/** The main looper handler for this "foreground" thread, although I'm not sure
	 *  if service processes get "foreground threads". */
	private Handler mHandler = null;
	/** Global handler for the speedwalk command, useful for changing the settings. */
	private SpeedwalkCommand mSpeedwalkCommand = null;
	
	/** Pattern for matching .xml extensions not case sensitive. */
	private final Pattern mXMLExtensionPattern = Pattern.compile("^.+\\.[xX][mM][lL]$");
	/** Matcher for matching .xml extensions not case sensitive. */
	private final Matcher mXMLExtensionMatcher = mXMLExtensionPattern.matcher("");
	
	/** Main tracker for plugins, generic ordered list of plugins in the order they were loaded. */
	private ArrayList<Plugin> mPlugins = null;
	
	/** Global map for handling the capture transformation for triggers and aliases. */
	private HashMap<String, String> mCaptureMap = new HashMap<String, String>();
	
	/** The DataPumper instance for this connection. */
	private DataPumper mPump = null;
	
	/** The Processor instance for this connection. */
	private Processor mProcessor = null;
	//TextTree buffer = null;
	
	/** TextTree instance used for trigger parsing input text. */
	private TextTree mWorking = null;
	
	/** TextTree instance used for trigger parsing input text. */
	private TextTree mFinished = null;
	
	/** Mapping of link paths to plugin names. */
	private HashMap<String, ArrayList<String>> mLinkMap = new HashMap<String, ArrayList<String>>();
	
	/** Mapping of plugin names to plugin objects. */
	private HashMap<String, Plugin> mPluginMap = new HashMap<String, Plugin>(0);
	
	/** Not really sure what this is. */
	private boolean mLoaded = false;
	
	/** Launcher display name for this Connection. */
	private String mDisplay;
	
	/** Host name for this connection. */
	private String mHost;
	
	/** Port indication for this connection. */
	private int mPort;
	
	/** Synchronization target to manage window loading/unloading. */
	private Object mWindowSynch = new Object();
	
	/** Mapping of window names to IWindowCallback aidl bridge connections. */
	private HashMap<String, IWindowCallback> mWindowCallbackMap = 
			new HashMap<String, IWindowCallback>();

	
	/** Enum used for the Timer command action ordinals. */
	private enum TIMER_ACTION {
		/** Play action.*/
		PLAY,
		/** Pause action. */
		PAUSE,
		/** Reset action.*/
		RESET,
		/** Info action.*/
		INFO,
		/** Stop action. */
		STOP,
		/** No action. */
		NONE
	}
	
	/** Instance of our parent service. This is bad. */
	private StellarService mService = null;
	
	/** A simple holder for if we are connected or not. */
	private boolean mIsConnected = false;
	
	/** The main settings wad/plugin. */
	private ConnectionSettingsPlugin mSettings = null;
	
	/** The keyboard command instance, not sure why this is here. */
	private KeyboardCommand mKeyboardCommand;
	
	/** Value of CRLF. */
	private String mCRLF = "\r\n";

	/** The pattern for the .command. */
	private Pattern mCommandPattern = Pattern.compile("^.(\\w+)\\s*(.*)$");
	
	/** The matcher for the .command. */
	private Matcher mCommandMatcher = mCommandPattern.matcher("");
	
	/** The map of special commands. */
	private HashMap<String, SpecialCommand> mSpecialCommands = new HashMap<String, SpecialCommand>();

	/** Constant for the status bar height, useful for plugins, hard to get. */
	private int mStatusBarHeight;
	
	/** Constant for the title bar height, useful for plugins, hard to get. */
	private int mTitleBarHeight;
	
	/** Public constructor for the Connection.
	* @param display The display name.
	* @param host The host name.
	* @param port The port number.
	* @param service Parent that initated this connection.
	*/
	public Connection(final String display, final String host, final int port, final StellarService service) {
		
		ColorDebugCommand colordebug = new ColorDebugCommand();
		DirtyExitCommand dirtyexit = new DirtyExitCommand();
		TimerCommand timercmd = new TimerCommand();
		BellCommand bellcmd = new BellCommand();
		FullScreenCommand fscmd = new FullScreenCommand();
		mKeyboardCommand = new KeyboardCommand();
		DisconnectCommand dccmd = new DisconnectCommand();
		ReconnectCommand rccmd = new ReconnectCommand();
		mSpeedwalkCommand = new SpeedwalkCommand(null, new Data());
		LoadButtonsCommand lbcmd = new LoadButtonsCommand();
		ClearButtonCommand cbcmd = new ClearButtonCommand();
		mSpecialCommands.put(colordebug.commandName, colordebug);
		mSpecialCommands.put(dirtyexit.commandName, dirtyexit);
		mSpecialCommands.put(timercmd.commandName, timercmd);
		mSpecialCommands.put(bellcmd.commandName, bellcmd);
		mSpecialCommands.put(fscmd.commandName, fscmd);
		mSpecialCommands.put(mKeyboardCommand.commandName, mKeyboardCommand);
		mSpecialCommands.put("kb", mKeyboardCommand);
		mSpecialCommands.put(dccmd.commandName, dccmd);
		mSpecialCommands.put(rccmd.commandName, rccmd);
		mSpecialCommands.put(mSpeedwalkCommand.commandName, mSpeedwalkCommand);
		mSpecialCommands.put(lbcmd.commandName, lbcmd);
		mSpecialCommands.put(cbcmd.commandName, cbcmd);
		SwitchWindowCommand swdcmd = new SwitchWindowCommand();
		mSpecialCommands.put(swdcmd.commandName, swdcmd);
		
		this.mDisplay = display;
		this.mHost = host;
		this.mPort = port;
		this.mService = service;
		
		mPlugins = new ArrayList<Plugin>();
		mHandler = new Handler(new ConnectionHandler());

		mWorking = new TextTree();
		mWorking.setLinkify(false);
		mWorking.setLineBreakAt(TEN_MILLION);
		mWorking.setMaxLines(TEN_THOUSAND);
		
		mFinished = new TextTree();
		mFinished.setLinkify(false);
		mFinished.setLineBreakAt(TEN_MILLION);
		mFinished.setMaxLines(TEN_THOUSAND);

		mWindows = new ArrayList<WindowToken>();
		
		SharedPreferences sprefs = this.getContext().getSharedPreferences("STATUS_BAR_HEIGHT", 0);
		mStatusBarHeight = sprefs.getInt("STATUS_BAR_HEIGHT", (int) (STATUS_BAR_DEFAULT_SIZE * this.getContext().getResources().getDisplayMetrics().density));
		mTitleBarHeight = sprefs.getInt("TITLE_BAR_HEIGHT", 0);

		mLoaded = true;
		
		//fish out the window.

	}
	
	/** The connection handler message queue. Coordinates multithreaded efforts from the DataPumper and foreground window via the Service. */
	private class ConnectionHandler implements Handler.Callback {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(final Message msg) {
			switch(msg.what) {
			case MESSAGE_TERMINATED_BY_PEER:
				killNetThreads(true);
				doDisconnect(true);
				mIsConnected = false;
				break;
			case MESSAGE_TIMERSTOP:
				doTimerAction((String) msg.obj, msg.arg2, TIMER_ACTION.STOP);
				break;
			case MESSAGE_TIMERSTART:
				doTimerAction((String) msg.obj, msg.arg2, TIMER_ACTION.PLAY);
				break;
			case MESSAGE_TIMERRESET:
				doTimerAction((String) msg.obj, msg.arg2, TIMER_ACTION.RESET);
				break;
			case MESSAGE_TIMERINFO:
				doTimerAction((String) msg.obj, msg.arg2, TIMER_ACTION.INFO);
				break;
			case MESSAGE_TIMERPAUSE:
				doTimerAction((String) msg.obj, msg.arg2, TIMER_ACTION.PAUSE);
				break;
			case MESSAGE_CALLPLUGIN:
				String ptmp = msg.getData().getString("PLUGIN");
				String ftmp = msg.getData().getString("FUNCTION");
				String dtmp = msg.getData().getString("DATA");
				doCallPlugin(ptmp, ftmp, dtmp);
				break;
			case MESSAGE_SETTRIGGERSDIRTY:
				setTriggersDirty();
				break;
			case MESSAGE_RELOADSETTINGS:
				reloadSettings();
				break;
			case MESSAGE_TRIGGER_LUA_ERROR:
				dispatchLuaError((String) msg.obj);
				break;
			case MESSAGE_RECONNECT:
				doReconnect();
				break;
			case MESSAGE_CONNECTED:
				mAutoReconnectAttempt = 0;
				break;
			case MESSAGE_DELETEPLUGIN:
				doDeletePlugin((String) msg.obj);
				break;
			case MESSAGE_ADDLINK:
				doAddLink((String) msg.obj);
				break;
			case MESSAGE_DORESETSETTINGS:
				doResetSettings();
				break;
			case MESSAGE_PLUGINLUAERROR:
				dispatchLuaError((String) msg.obj);
				break;
			case MESSAGE_EXPORTFILE:
				exportSettings((String) msg.obj);
				break;
			case MESSAGE_IMPORTFILE:
				Connection.this.mService.markWindowsDirty();
				importSettings((String) msg.obj, true, false);
				break;
			case MESSAGE_SAVESETTINGS:
				String changedplugin = (String) msg.obj;
				Connection.this.saveDirtyPlugin(changedplugin);
				break;
			case MESSAGE_GMCPTRIGGERED:
				String plugin = msg.getData().getString("TARGET");
				String gcallback = msg.getData().getString("CALLBACK");
				HashMap<String, Object> gdata = (HashMap<String, Object>) msg.obj;
				Plugin gp = mPluginMap.get(plugin);
				gp.handleGMCPCallback(gcallback, gdata);
				break;
			case MESSAGE_INVALIDATEWINDOWTEXT:
				String wname = (String) msg.obj;
				try {
					doInvalidateWindowText(wname);
				} catch (RemoteException e4) {
					e4.printStackTrace();
				}
				break;
			case MESSAGE_WINDOWXCALLS:
				Object o = msg.obj;
				if (o == null) {
					o = "";
				}
				String token = msg.getData().getString("TOKEN");
				String function = msg.getData().getString("FUNCTION");
				try {
					Connection.this.windowXCallS(token, function, o);
				} catch (RemoteException e3) {
					e3.printStackTrace();
				}
				break;
			case MESSAGE_WINDOWXCALLB:
				byte[] bytesa = (byte[]) msg.obj;
				String tokens = msg.getData().getString("TOKEN");
				String functions = msg.getData().getString("FUNCTION");
				try {
					Connection.this.windowXCallB(tokens, functions, bytesa);
				} catch (RemoteException e3) {
					e3.printStackTrace();
				}
				break;
			case MESSAGE_ADDFUNCTIONCALLBACK:
				Bundle data = msg.getData();
				String id = data.getString("ID");
				String command = data.getString("COMMAND");
				String callback = data.getString("CALLBACK");
				int pid = -1;
				for (int i = 0; i < mPlugins.size(); i++) {
					Plugin p = mPlugins.get(i);
					if (p.getName().equals(id)) {
						pid = i;
					}
				}
				if (pid != -1) {
					FunctionCallbackCommand fcc = new FunctionCallbackCommand(pid, command, callback);
					mSpecialCommands.put(fcc.commandName, fcc);
				}
				break;
			case MESSAGE_WINDOWBUFFER:
				boolean set = (msg.arg1 == 0) ? false : true;
				
				String name = (String) msg.obj;
				
				for (WindowToken tok : mWindows) {
					if (tok.getName().equals(name)) {
						tok.setBufferText(set);
					}
				}
				break;
			case MESSAGE_NEWWINDOW:
				WindowToken tok = (WindowToken) msg.obj;
				mWindows.add(tok);
				break;
			case MESSAGE_DRAWINDOW:
				Connection.this.redrawWindow((String) msg.obj);
				break;
			case MESSAGE_LUANOTE:
				String str = (String) msg.obj;
				try {
					dispatchNoProcess(str.getBytes(mSettings.getEncoding()));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				break;
			case MESSAGE_LINETOWINDOW:
				Object line = msg.obj;
				String target = msg.getData().getString("TARGET");
				try {
					Connection.this.lineToWindow(target, line);
				} catch (RemoteException e3) {
					e3.printStackTrace();
				}
				break;
			case MESSAGE_SENDDATA_STRING:
				try {
					byte[] bytes = ((String) msg.obj).getBytes(mSettings.getEncoding());
					sendToServer(bytes);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				break;
			case MESSAGE_SENDDATA_BYTES:
				sendToServer((byte[]) msg.obj);
				break;
			case MESSAGE_SENDGMCPDATA:
				byte bIAC = TC.IAC;
				byte bSB = TC.SB;
				byte bSE = TC.SE;
				byte bGMCP = TC.GMCP;
				int size = ((String) msg.obj).length() + GMCP_PAYLOAD_SIZE;
				ByteBuffer fub = ByteBuffer.allocate(size);
				fub.put(bIAC).put(bSB).put(bGMCP);
				try {
					fub.put(((String) msg.obj).getBytes("ISO-8859-1"));
				} catch (UnsupportedEncodingException e2) {
					e2.printStackTrace();
				}
				fub.put(bIAC).put(bSE);
				byte[] fubtmp = new byte[size];
				fub.rewind();
				fub.get(fubtmp);
				if (mPump != null && mPump.isConnected()) {
					mPump.sendData(fubtmp);
				} else {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_SENDGMCPDATA, msg.obj), FIVE_HUNDRED_MILLIS);
				}
				break;
			case MESSAGE_STARTUP:
				doStartup();
				break;
			case MESSAGE_STARTCOMPRESS:
				mPump.getHandler().sendMessage(mPump.getHandler().obtainMessage(DataPumper.MESSAGE_COMPRESS, msg.obj));
				break;
			case MESSAGE_SENDOPTIONDATA:
				Bundle b = msg.getData();
				byte[] obytes = b.getByteArray("THE_DATA");
				String message = b.getString("DEBUG_MESSAGE");
				if (message != null) {
					sendDataToWindow(message);
				}

				if (mPump != null) {
					mPump.sendData(obytes);
				}
				break;
			case MESSAGE_PROCESSORWARNING:
				sendDataToWindow((String) msg.obj);
				break;
			case MESSAGE_BELLINC:
				if (mSettings.isVibrateOnBell()) {
					Connection.this.mService.doVibrateBell();
				}
				if (mSettings.isNotifyOnBell()) {
					Connection.this.mService.doNotifyBell(Connection.this.mDisplay, Connection.this.mHost, Connection.this.mPort);
				}
				if (mSettings.isDisplayOnBell()) {
					Connection.this.mService.doDisplayBell();
				}
				break;
			case MESSAGE_DODIALOG:
				dispatchDialog((String) msg.obj);
				break;
			case MESSAGE_PROCESS:
				try {
					dispatch((byte[]) msg.obj);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;
			case MESSAGE_DISCONNECTED:
				killNetThreads(true);
				doDisconnect(false);
				mIsConnected = false;
				break;
			default:
				break;
			}
			return true;
		}
		
	}

	/** Quick frontend for dispatchNoProcess(...) for sending a lua error message.
	 * 
	 * @param message The message to show.
	 */
	protected final void dispatchLuaError(final String message) {
		try {
			dispatchNoProcess(message.getBytes(mSettings.getEncoding()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/** Saves dirty plugins or the main settings wad.
	 * 
	 * @param changedplugin The name of the plugin, "" will save the main settings.
	 * @note This doesn't work as far as I know.
	 */
	protected final void saveDirtyPlugin(final String changedplugin) {
		if (changedplugin.equals("")) {
			saveMainSettings();
		} else {
			Plugin p = mPluginMap.get(changedplugin);
			if (p != null) {
				if (p.getStorageType().equals("INTERNAL")) {
					saveMainSettings();
				} else {
					if(p.getSettings().isDirty()) {
						saveMainSettings(); //ugly, need to be able to save plugins individually.
					}
				}
			}
		}
	}

	/** Work horse method for plugins to invalidate a target window's text.
	 * 
	 * @param name Name of the window that should invalidate it's text.
	 * @throws RemoteException Thrown when there is a problem with the aidl bridge.
	 */
	protected final void doInvalidateWindowText(final String name) throws RemoteException {

		IWindowCallback callback = mWindowCallbackMap.get(name);
	
		if (callback == null) {
			return;
		}
		
		WindowToken w = null;
		for (int i = 0; i < mWindows.size(); i++) {
			WindowToken tmp = mWindows.get(i);
			if (tmp.getName().equals(name)) {
				w = tmp;
			}
		}
		
		TextTree buffer = w.getBuffer();

		callback.resetWithRawDataIncoming(buffer.dumpToBytes(true));
	}

	/** Work horse method for WindowXCallS Lua function.
	 * 
	 * @param name Name of the target window.
	 * @param function Name of the anonymous global function to call
	 * @param o String argument to provide to @param function
	 * @throws RemoteException Thrown when there is a problem with the aidl bridge.
	 */
	public final void windowXCallS(final String name, final String function, final Object o) throws RemoteException {

		IWindowCallback c = mWindowCallbackMap.get(name);

		if (c != null) {
			c.xcallS(function, (String) o);
		}

	}

	/** Work horse method for WindowXCallB Lua function.
	 * 
	 * @param name Name of the target window.
	 * @param functions Name of the anonymous global function to call.
	 * @param bytes Bytes to provide as an argument to @param function
	 * @throws RemoteException Thrown when there is a problem with the aidl bridge.
	 */
	protected final void windowXCallB(final String name, final String functions, final byte[] bytes) throws RemoteException {
		IWindowCallback c = mWindowCallbackMap.get(name);
		if (c != null) {
			c.xcallB(functions, bytes);
		}
	}
	
	/** Work horse method for the CallPlugin Lua function.
	 * 
	 * @param plugin Name of the plugin to call.
	 * @param function Name of the anonymous global function to call.
	 * @param data String argument to provide to @param function.
	 */
	private void doCallPlugin(final String plugin, final String function, final String data) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.callFunction(function, data);
		} else {
			this.dispatchLuaText("\n" + Colorizer.getRedColor() + "No plugin named: " + plugin + Colorizer.getRedColor() + "\n");
		}
	}

	/** Calling this method will reload the connection settings and all plugins. */
	public final void reloadSettings() {

		for (IWindowCallback c : mWindowCallbackMap.values()) {
			try {
				c.shutdown();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		mWindowCallbackMap.clear();
		mService.markWindowsDirty();
		loadInternalSettings();
		
	}
	
	/** Shuts down all running plugins and clears associated structures.*/
	private void shutdownPlugins() {
		for (Plugin p : mPlugins) {
			p.shutdown();
			p = null;
		}
		mPlugins.clear();
	}
	
	/** Loads plugins and sets up internal data structures.
	 * 
	 * @param tmpPlugs The array of already loaded plugins.
	 * @param summary A holder string fo what happened during the internal loading process.
	 */
	private void loadPlugins(final ArrayList<Plugin> tmpPlugs, final String summary) {
		
		HashMap<String, TextTree> bufferSaves = new HashMap<String, TextTree>();
		
		TextTree buffer = null;
		if (mWindows.size() > 0) {
			buffer = mWindows.get(0).getBuffer();
			while (mWindows.size() > 0) {
				WindowToken t = mWindows.remove(mWindows.size() - 1);
				bufferSaves.put(t.getName(), t.getBuffer());
			}
		} 
		if (mSettings != null) {
			mSettings.shutdown();
			
		}
		mSettings = null;
			
		mSettings = (ConnectionSettingsPlugin) tmpPlugs.get(0);
		mSettings.sortTriggers();
		mSettings.initTimers();
		for (WindowToken tmpw : mSettings.getSettings().getWindows().values()) {
			tmpw.setDisplayHost(mDisplay);
		}
		
		mWindows.add(0, mSettings.getSettings().getWindows().get(MAIN_WINDOW));
		if (buffer == null) {
			buffer = mWindows.get(0).getBuffer();
		} else {
			buffer.addString("\n\n");
		}
		
		buffer.addString(summary);
		tmpPlugs.remove(0);
		
		mPluginMap.clear();
		mLinkMap.clear();
		
		mPlugins.addAll(tmpPlugs);
		
		
		for (Plugin p : mPlugins) {
			for (WindowToken tmpw : p.getSettings().getWindows().values()) {
				tmpw.setDisplayHost(mDisplay);
			}
			p.initTimers();
			mPluginMap.put(p.getName(), p);
			p.sortTriggers();
			if (p.getSettings().getWindows().size() > 0) {
				mWindows.addAll(p.getSettings().getWindows().values());
			}
			
			p.pushOptionsToLua();
		}
		
		if (mSettings.getDirections().size() == 0) {
			HashMap<String, DirectionData> tmp = new HashMap<String, DirectionData>();
			tmp.put("n", new DirectionData("n", "n"));
			tmp.put("e", new DirectionData("e", "e"));
			tmp.put("s", new DirectionData("s", "s"));
			tmp.put("w", new DirectionData("w", "w"));
			tmp.put("h", new DirectionData("h", "nw"));
			tmp.put("j", new DirectionData("j", "ne"));
			tmp.put("k", new DirectionData("k", "sw"));
			tmp.put("l", new DirectionData("l", "se"));
			mSettings.setDirections(tmp);
			mSpeedwalkCommand.setDirections(tmp);
		} else {
			mSpeedwalkCommand.setDirections(mSettings.getDirections());
		}
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			for (String link : mSettings.getLinks()) {
				buffer.addString(Colorizer.getWhiteColor() + "Loading plugin file: " + link);
				String filename = Environment.getExternalStorageDirectory() + "/BlowTorch/" + link;
				ArrayList<Plugin> tmplist = new ArrayList<Plugin>();
				PluginParser parse = new PluginParser(filename, link, mService.getApplicationContext(), tmplist, mHandler, this);
				
				try {
					ArrayList<Plugin> group = parse.load();
					for (Plugin p : group) {
						mPluginMap.put(p.getName(), p);
						if (mLinkMap.get(link) == null) {
							ArrayList<String> vals = new ArrayList<String>();
							vals.add(p.getName());
							mLinkMap.put(link, vals);
						} else {
							ArrayList<String> vals = mLinkMap.get(link);
							vals.add(p.getName());
						}
						
						for (WindowToken tmpw : p.getSettings().getWindows().values()) {
							tmpw.setDisplayHost(mDisplay);
						}
						
						if (p.getSettings().getWindows().size() > 0) {
							mWindows.addAll(p.getSettings().getWindows().values());
						}
						
						p.pushOptionsToLua();
					}
					
					mPlugins.addAll(group);
					
					buffer.addString(Colorizer.getWhiteColor() + ", success." + Colorizer.getWhiteColor() + "\n");
				} catch (FileNotFoundException e) {
					buffer.addString(Colorizer.getRedColor() + " file not found." + Colorizer.getWhiteColor() + "\n");
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					buffer.addString(Colorizer.getRedColor() + " XML Parse error.\n" + e.getLocalizedMessage() + Colorizer.getWhiteColor() + "\n");
				}
			}
		}
	
		
		//so now that we have all the plugins, we need to build up the processor's gmcpTriggerTables.
		//loop through all the plugins, looking for literal triggers starting
		//with the gmcpTriggerChar.
		
		if (bufferSaves != null) {
			for (WindowToken w : mWindows) {
				if (w != null) {
					
					if (bufferSaves.get(w.getName()) != null) {
						w.setBuffer(bufferSaves.get(w.getName()));
					}
				}
			}
		}
		
		buildSettingsPage();
		mService.reloadWindows();
		
	}
	
	/** Work horse function to rebuild the trigger system.
	 * 
	 * I think this is called from a number of placed, but it should really be called from dispatch()
	 * when triggers are dirty.
	 */
	public final void buildTriggerSystem() {
		if (mSettings == null) { 
			return; 
		}
		mSortedTriggerMap.clear();
		mTriggerPluginMap.clear();
		int currentgroup = 1;
		mTriggerBuilder.setLength(0);
		boolean addseparator = false;
		ArrayList<TriggerData> tmp = mSettings.getSortedTriggers();
		if (tmp == null) {
			mSettings.sortTriggers();
			tmp = mSettings.getSortedTriggers();
		}
		if (tmp != null && tmp.size() > 0) {
			for (int i = 0; i < tmp.size(); i++) {
				TriggerData t = tmp.get(i);
				if (!(!t.isInterpretAsRegex() && t.getPattern().startsWith("%"))) {
					if (t.isEnabled()) {
						if (!addseparator) {
							mTriggerBuilder.append("(");
							if (!t.isInterpretAsRegex()) {
								mTriggerBuilder.append("\\Q");
							}
							mTriggerBuilder.append(t.getPattern());
							if (!t.isInterpretAsRegex()) {
								mTriggerBuilder.append("\\E");
							}
							mTriggerBuilder.append(")");
							addseparator = true;
						} else {
							mTriggerBuilder.append("|(");
							if (!t.isInterpretAsRegex()) {
								mTriggerBuilder.append("\\Q");
							}
							mTriggerBuilder.append(t.getPattern());
							if (!t.isInterpretAsRegex()) {
								mTriggerBuilder.append("\\E");
							}
							mTriggerBuilder.append(")");
						}
						mSortedTriggerMap.put(currentgroup, t);
						mTriggerPluginMap.put(currentgroup, mSettings);
						currentgroup += t.getMatcher().groupCount() + 1;
					}
				}
			}
		}
		
		for (Plugin p : mPlugins) {
			tmp = p.getSortedTriggers();
			if (tmp == null) {
				p.sortTriggers();
				tmp = p.getSortedTriggers();
			}
			if (tmp != null && tmp.size() > 0) {
				for (int i = 0; i < tmp.size(); i++) {
					TriggerData t = tmp.get(i);
					if (!(!t.isInterpretAsRegex() && t.getPattern().startsWith("%"))) {
						if (t.isEnabled()) {
							if (i == 0 && !addseparator) {
								mTriggerBuilder.append("(");
								if (!t.isInterpretAsRegex()) {
									mTriggerBuilder.append("\\Q");
								}
								mTriggerBuilder.append(t.getPattern());
								if (!t.isInterpretAsRegex()) {
									mTriggerBuilder.append("\\E");
								}
								mTriggerBuilder.append(")");
								addseparator = true;
							} else {
								mTriggerBuilder.append("|(");
								if (!t.isInterpretAsRegex()) {
									mTriggerBuilder.append("\\Q");
								}
								mTriggerBuilder.append(t.getPattern());
								if (!t.isInterpretAsRegex()) {
									mTriggerBuilder.append("\\E");
								}
								mTriggerBuilder.append(")");
							}
							mSortedTriggerMap.put(currentgroup, t);
							mTriggerPluginMap.put(currentgroup, p);
							currentgroup += t.getMatcher().groupCount() + 1;
						}
					}
				}
			}
		
		}
		mMassiveTriggerString = mTriggerBuilder.toString();
		mMassivePattern = Pattern.compile(mMassiveTriggerString, Pattern.MULTILINE);
		mMassiveMatcher = mMassivePattern.matcher("");
		triggersDirty = false;
	}
	
	/** end of the line of the DrawWindow function. I don't think this is used.
	 * 
	 * @param win Name of the window to redraw.
	 */
	protected final void redrawWindow(final String win) {

			IWindowCallback w = mWindowCallbackMap.get(win);
			if (w == null) {
				return;
			}
			try {
					w.redraw();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

	}

	/** Actual working method for the LineToWindow Lua function.
	 * 
	 * @param target Name of the window to recieve the line.
	 * @param line The TextTree.Line to send to @param target
	 * @throws RemoteException Thrown when there is a problem with the aidl bridge.
	 */
	protected final void lineToWindow(final String target, final Object line) throws RemoteException {
		
		for (WindowToken w : mWindows) {
			if (w.getName().equals(target)) {
				TextTree tmp = new TextTree();
				tmp.setEncoding(mSettings.getEncoding());
				if (line instanceof TextTree.Line) {
					tmp.appendLine((TextTree.Line) line);
				} else if (line instanceof String) {
					try {
						tmp.addBytesImpl(((String) line).getBytes(mSettings.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				tmp.updateMetrics();
				byte[] lol = tmp.dumpToBytes(false);
				
				try {
					w.getBuffer().addBytesImpl(lol);
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
				}

					IWindowCallback c = mWindowCallbackMap.get(target);
					if (c != null) {
						c.rawDataIncoming(lol);		
					}
			}
		}
	}
	
	/** Called from the aidl bridge housing in StellarService when the foreground window has started a new
	 * window and needs to let the Connection know that a new window is open for it.
	 * 
	 * @param name The name of the new window.
	 * @param callback The IWindowCallback aidl conenction object associated with the window.
	 */
	public final void registerWindowCallback(final String name, final IWindowCallback callback) {
		synchronized (mWindowSynch) {
		Log.e("LOG","REGISTERING WINDOW "+name + " mCallbacksStarte="+mCallbacksStarted);
		if (mCallbacksStarted) {
			mWindowCallbacks.finishBroadcast();
		}
		Log.e("LOG","REGISTERING " + name);
		mWindowCallbacks.register(callback);
		
		int n = mWindowCallbacks.beginBroadcast();
		for (int i = 0; i < n; i++) {
			IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				mWindowCallbackMap.put(w.getName(), w);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mCallbacksStarted = true;
		}
	}
	
	/** Called from the aidl bridge housing in StellarService when the foreground window has stopped and destroyed a
	 * window and needs to let the Connection know that the IWindowCallback is invalid.
	 * 
	 * @param callback The IWindowCallback aidl connection object of the destroyed window.
	 */
	public final void unregisterWindowCallback(final IWindowCallback callback) {
		synchronized (mWindowSynch) {
		Log.e("LOG","UNREGISTERING WINDOW "+" mCallbacksStarted="+mCallbacksStarted);
		if (mCallbacksStarted) {
			mWindowCallbacks.finishBroadcast();
			//mCallbacksStarted = false;
		}
		try {
			Log.e("LOG","UNREGISTERING " + callback.getName());
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mWindowCallbacks.unregister(callback);

		mWindowCallbackMap.clear();
		int n = mWindowCallbacks.beginBroadcast();
		for (int i = 0; i < n; i++) {
			IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				mWindowCallbackMap.put(w.getName(), w);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		mCallbacksStarted = true;
		}
	}
	
	/** Called from the DataPumper when the net threads have been shut down.
	 * 
	 * @param override Indicates weather the auto reconnect should be overridden.
	 */
	protected final void doDisconnect(final boolean override) {
		if (mHandler == null) {
			return;
		}
		if (mAutoReconnect && !override) {
			if (mAutoReconnectAttempt < mAutoReconnectLimit) {
				mAutoReconnectAttempt++;
				String message = "\n" + Colorizer.getRedColor() + "Network connection disconnected.\n"
								 + "Attmempting reconnect in 3 seconds. " + (mAutoReconnectLimit - mAutoReconnectAttempt) + " tries remaining." + Colorizer.getWhiteColor() + "\n";
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_PROCESSORWARNING, message));
				mHandler.sendEmptyMessageDelayed(MESSAGE_RECONNECT, THREE_THOUSAND_MILLIS);
				return;
			}
		}
		
		
		mService.doDisconnect(this);
	}
	
	/** Called from various sources to kill the DataPumper and all of its threads.
	 * 
	 * @param noreconnect true if there should be no reconnect attempt made.
	 */
	protected final void killNetThreads(final boolean noreconnect) {
		
		if (mPump == null) {
			return;
		}
		if (mPump != null) {
			if (mPump.getHandler() != null) {
				mPump.closeSocket();
				//mPump.getHandler().removeMessages(DataPumper.MESSAGE_RETRIEVE);
				mPump.getHandler().removeCallbacksAndMessages(null);
				mPump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_END);
			
			
				try {
					mPump.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		mProcessor = null;
		
		if (noreconnect) {
			if (mHandler != null) {
				mHandler.removeMessages(MESSAGE_RECONNECT);
			}
		}
		
		mPump = null;
	}
	
	/** Sends a byte array to the default output window. Does not invoke trigger processing.
	 * 
	 * @param data The data to send.
	 */
	private void dispatchNoProcess(final byte[] data) {
		mWindows.get(0).getBuffer().addBytesImplSimple(data);
		sendBytesToWindow(data);
	}
	
	/** Utility class used for trigger processing. Maps a start and end value to a line number.	 */
	private class Range {
		/** Start of the line. */
		private int mStart;
		/** End of the line. */
		private int mEnd;
		/** Line number. */
		private int mLine;
		/** Generic assignment constructor.
		 * 
		 * @param start Start of line.
		 * @param end End of line.
		 * @param line Line number.
		 */
		public Range(final int start, final int end, final int line) { 
			this.mStart = start; 
			this.mEnd = end; 
			this.mLine = line;
		}
		/** Line number getter.
		 * 
		 * @return The line number.
		 */
		public int getLine() { return mLine; }
		/** Range start getter.
		 * 
		 * @return index position of the start of the line.
		 */
		public int getStart() { return mStart; }
		/** Range end getter.
		 * 
		 * @return index position of the end of the line.
		 */
		public int getEnd() { return mEnd; }
		
	}
	
	/** Range class comparator.	 */
	private class RangeComparator implements Comparator<Range> {

		@Override
		public int compare(final Range a, final Range b) {
			if (b.mStart > a.mEnd && b.mEnd > a.mEnd) {
				return -1;
			}

			if (b.mStart < a.mStart && b.mEnd < a.mEnd) {
				return 1;
			}

			return 0;
		}

	}
	

	
	/** Setter for triggersDirty. */
	public final void setTriggersDirty() {
		triggersDirty = true;
	}

	/** THE INCOMING DATA DISPATCH ROUTINE! Unicorns and puppies and all kinds of good things live here.
	 * 
	 * @param data The data to process.
	 * @throws UnsupportedEncodingException Thrown when a string<==>byte[] conversion has a bad encoding provided.
	 */
	private void dispatch(final byte[] data) throws UnsupportedEncodingException {

		byte[] raw = mProcessor.rawProcess(data);
		if (raw == null) { 
			return; 
		}
		
		TextTree buffer = null;
		for (WindowToken w : mWindows) {
			if (w.getName().equals(MAIN_WINDOW)) {
				buffer = w.getBuffer();
			}
		}

		TextTree.Color tmpcolor = buffer.getBleedColor();
		mWorking.setBleedColor(tmpcolor);
		mFinished.setBleedColor(tmpcolor);

		mWorking.addBytesImpl(raw);
	
		mWorking.setModCount(0);
		
		//strip the color out.
		COLOR_MATCHER.reset(new String(raw, mSettings.getEncoding()));
		String stripped = COLOR_MATCHER.replaceAll("");
		
		if (triggersDirty) {
			buildTriggerSystem();
		}

		ListIterator<TextTree.Line> it = mWorking.getLines().listIterator(mWorking.getLines().size());
		mLineMap.clear();
		LINE_MATCHER.reset(stripped);
		boolean found = false;
		int lineNumber = mWorking.getLines().size() - 1;
		while (LINE_MATCHER.find()) {
			found = true;
			mLineMap.add(new Range(LINE_MATCHER.start(), LINE_MATCHER.end(), lineNumber));
			lineNumber = lineNumber - 1;
		}
		boolean keepEvaluating = true;
		lineNumber = mWorking.getLines().size() - 1;
		Line l = null;
		if (it.hasPrevious()) {
			l = it.previous();
		} else {
			return;
		}
		if (found) {
			boolean done = false;
			while (!done) {
				done = true;
				boolean rebuildTriggers = false;
				boolean replaceGagged = false;
				int gagloc = -1;
				mMassiveMatcher.reset(stripped);
				while (keepEvaluating && mMassiveMatcher.find()) {
					int s = mMassiveMatcher.start();
					int e = mMassiveMatcher.end() - 1;
					String matched = mMassiveMatcher.group();
					Range r = new Range(s, e, 0);
					SortedSet<Range> tmp = mLineMap.tailSet(r);
	
					int tmpline = tmp.first().getLine();
					int tmpstart = s - tmp.first().getStart();
					int tmpend = (e - 1) - tmp.first().getStart();
					gagloc = tmp.first().getEnd();
					
					int index = -1;
					for (int i = 1; i <= mMassiveMatcher.groupCount(); i++) {
						if (mMassiveMatcher.group(i) != null) {
							index = i;
							i = mMassiveMatcher.groupCount();
						}
					}
					
					if (index > 0) {
						//we have found a trigger. advance the line number to
						
						TriggerData t = mSortedTriggerMap.get(index);
						Plugin p = mTriggerPluginMap.get(index);

						boolean gagged = false;
						if (lineNumber > tmpline) {
							int amount = lineNumber - tmpline;
							
							for (int i = 0; i < amount; i++) {
								if (it.hasPrevious()) {
								l = it.previous();
								}
							}
							mWorking.setModCount(0);
							lineNumber = tmpline;
							if (it.hasNext()) {
								lineNumber = tmpline;	
							}
						} else if (tmpline > lineNumber) {
							gagged = true;
						}
						if (t != null && t.isEnabled() && !gagged) {
							mCaptureMap.clear();
							for (int i = index; i <= (t.getMatcher().groupCount() + index); i++) {
								
								mCaptureMap.put(Integer.toString(i - index), mMassiveMatcher.group(i));
							}
							for (TriggerResponder responder : t.getResponders()) {
								if (responder instanceof GagAction) {
									replaceGagged = true;
								}
								try {
									responder.doResponse(mService.getApplicationContext(), 
																	   mWorking, 
																	   lineNumber, 
																	   it, 
																	   l, 
																	   tmpstart,
																	   tmpend,
																	   matched, 
																	   t, 
																	   mDisplay,
																	   mHost,
																	   mPort, 
																	   StellarService.getNotificationId(), 
																	   mService.isWindowConnected(), 
																	   mHandler, 
																	   mCaptureMap, 
																	   p.getLuaState(), 
																	   t.getName(), 
																	   mSettings.getEncoding());
									
									if (triggersDirty) {
										keepEvaluating = false;
										rebuildTriggers = true;
									}
								} catch (IteratorModifiedException e1) {
									it = e1.getIterator();
									mWorking.setModCount(0);
									lineNumber = it.previousIndex();
									if (it.hasPrevious()) {
										l = it.previous();
									} else {
										keepEvaluating = false;
									}
									
								}
								if (mWorking.getLines().size() == 0) {
									keepEvaluating = false;
								}
							}
						}
					}
					if (rebuildTriggers) {
						break;
					}
				}
				if (rebuildTriggers) {
					mWorking.setModCount(0);
					done = false;
					keepEvaluating = true;
					int e = mMassiveMatcher.end();

					if (e != stripped.length()) {
						if (replaceGagged) {
							stripped = stripped.substring(gagloc + 1, stripped.length());
						} else {
							stripped = stripped.substring(e + 1, stripped.length());
						}	
					}
					
					if (lineNumber <= mWorking.getLines().size() - 1) {
						while (mWorking.getLines().size() - 1 > lineNumber) {

							Line tmp = mWorking.getLines().get(mWorking.getLines().size() - 1);
							mWorking.getLines().remove(mWorking.getLines().size() - 1);
							mFinished.appendLine(tmp);
						}
						
					}
					
					buildTriggerSystem();
					
					mLineMap.clear();
					LINE_MATCHER.reset(stripped);
					found = false;

					lineNumber = mWorking.getLines().size() - 1;
					while (LINE_MATCHER.find()) {
						found = true;
						mLineMap.add(new Range(LINE_MATCHER.start(), LINE_MATCHER.end(), lineNumber));
						lineNumber = lineNumber - 1;
					}
					
					lineNumber = mWorking.getLines().size() - 1;
					if (lineNumber == -1) {
						keepEvaluating = false;
						done = true;
					} else {
						it = mWorking.getLines().listIterator(lineNumber + 1);
						l = it.previous();
					}
					
				}
				
				
			}
		}

		ListIterator<TextTree.Line> finisher = mWorking.getLines().listIterator(mWorking.getLines().size());
		while (finisher.hasPrevious()) {
			mFinished.appendLine(finisher.previous());
		}
		
		mWorking.empty();
		mFinished.updateMetrics();
		
		byte[] proc = mFinished.dumpToBytes(false);
		
		buffer.addBytesImplSimple(proc);
		sendBytesToWindow(proc);
		
	}
	
	/** Called from a few places I think. Triggers the network disconnected dialog in the foreground window.
	 * Unless the auto reconnect is set.
	 * 
	 * @param str The message fro the dialog.
	 */
	protected final void dispatchDialog(final String str) {
		if (mHandler == null || str == null) { return; }
		if (mAutoReconnect) {
			if (mAutoReconnectAttempt < mAutoReconnectLimit) {
				mAutoReconnectAttempt++;
				killNetThreads(true);
				String message = "\n" + Colorizer.getRedColor() + "Network Error: " + str + "\n" + "Attmempting reconnect in 20 seconds. " + (mAutoReconnectLimit - mAutoReconnectAttempt) 
						+ " tries remaining." + Colorizer.getWhiteColor() + "\n";
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_PROCESSORWARNING, message));
				mHandler.sendEmptyMessageDelayed(MESSAGE_RECONNECT, TWENTY_THOUSAND_MILLIS);
				return;
			}
		}
		mService.dispatchDialog(str);
	}

	/** Sends a string to the main output window.
	 * 
	 * @param message The string to send.
	 */
	public final void sendDataToWindow(final String message) {
		
		try {
			sendBytesToWindow(message.getBytes(mSettings.getEncoding()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/** Sends bytes to the main output window.
	 * 
	 * @param data The bytes to send.
	 */
	public final void sendBytesToWindow(final byte[] data) {

		try {

			IWindowCallback c = mWindowCallbackMap.get(MAIN_WINDOW);
			if (c != null) {
				c.rawDataIncoming(data);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/** Meat of the startup sequence. Starts the net threads after the settings have been loaded. */
	private void doStartup() {
	
		killNetThreads(true);
		
		mPump = new DataPumper(mHost, mPort, mHandler);
		
		mProcessor = new Processor(mHandler, mSettings.getEncoding(), mService.getApplicationContext());

		initSettings();
		mPump.start();
		loadGMCPTriggers();
		mIsConnected = true;
		
		mService.showConnectionNotification(mDisplay, mHost, mPort);
	}
	
	/** The gmcp trigger loading routine. This is pretty self explanatory, but it seeks out
	 * non-regex triggers that start witht he gmcp trigger char (default %) and tracks them 
	 * accordingly.
	 */
	private void loadGMCPTriggers() {
		String gmcpChar = mSettings.getGMCPTriggerChar();
		for (int i = 0; i < mPlugins.size(); i++) {
			Plugin p = mPlugins.get(i);
			HashMap<String, TriggerData> triggers = p.getSettings().getTriggers();
			for (TriggerData t : triggers.values()) {
				if (!t.isInterpretAsRegex()) { //this actually means literal
					if (t.getPattern().startsWith(gmcpChar)) {
						//add it to the watch list, if it has a script responder
						for (TriggerResponder r : t.getResponders()) {
							if (r instanceof ScriptResponder) {
								ScriptResponder s = (ScriptResponder) r;
								String callback = s.getFunction();
								String module = t.getPattern().substring(1, t.getPattern().length());
								String name = p.getName();
								mProcessor.addWatcher(module, name, callback);
							}
						}
					}
				}
			}
		}
	}


	
	/** Alias parsing and special command handling routine.
	 * 
	 * @param data The data on its way to the server in need of processing.
	 * @return A Data object containing the string for the server and the string to the window.
	 * @throws UnsupportedEncodingException Problem with the String<==>byte[] conversion indicating a bad encoding option.
	 */
	private Data processOutputData(final String data) throws UnsupportedEncodingException {
		mDataToServer.setLength(0);
		mDataToWindow.setLength(0);
		String out = data;
		if (out.endsWith("\n")) {
			out = out.substring(0, out.length() - 2);
		}
		
		if (out.equals("")) {
			Data enter = new Data();
			enter.mCmdString = "";
			enter.mVisString = null;
			return enter;
		}
		
		if (out.equals(";;")) {
			Data enter = new Data();
			enter.mCmdString = ";" + mCRLF;
			enter.mVisString = ";";
			return enter;
		}
		List<String> list = null;
		
		if (mSettings.isSemiIsNewLine()) {
			//commands = semicolon.split(out);
			list = splitSemicolonSafe(out);
			
		} else {
			list = new ArrayList<String>();
			list.add(out);
		}
		StringBuffer holdover = new StringBuffer();
		
		ListIterator<String> iterator = list.listIterator();
		while (iterator.hasNext()) {
			String cmd = iterator.next();
			
			if (cmd.endsWith("~")) {
				holdover.append(cmd.substring(0, cmd.length() - 1) + ";");
			} else {
				if (holdover.length() > 0) {
					cmd = holdover.toString() + cmd;
					holdover.setLength(0);
				}
				//2.5 run command through the global lua state
				Data d = null;
				
				if (cmd.startsWith(mScriptBlock)) {
					mSettings.runLuaString(cmd.substring(mScriptBlock.length(), cmd.length()));
				} else {
					d = processCommand(cmd);
				}
				//3 - do special command processing.
				
				//4 - handle command processing output
				
				if (d != null) {
					boolean m = false;
					if (d.mCmdString != null && d.mVisString != null) {
						if (d.mCmdString.equals(d.mVisString)) {
							m = true; //aliases & regular commands will always have the same cmdString and visString
						}
					}
					
					//5 - alias replacement				
					if (d.mCmdString != null && !d.mCmdString.equals("")) {
						boolean didReplace = false;
						byte[] tmp = null;
						for (int i = 0; i < mPlugins.size() + 1; i++) {
							Plugin p = null;
							if (i == 0) {
								p = mSettings;
							} else {
								p = mPlugins.get(i - 1);
							}
							if (p.getSettings().getAliases().size() > 0) {
								Boolean reprocess = true;
								tmp = p.doAliasReplacement(d.mCmdString.getBytes(mSettings.getEncoding()), reprocess);
								String tmpstr = new String(tmp, mSettings.getEncoding());
								if (!d.mCmdString.equals(tmpstr)) {
									//alias replaced, needs to be processed
									
									List<String> aliasCommands = null;
									if (mSettings.isSemiIsNewLine()) {
										aliasCommands = splitSemicolonSafe(tmpstr);
									} else {
										aliasCommands = new ArrayList<String>(1);  
										aliasCommands.add(tmpstr);
									}
									for (String acmd : aliasCommands) {
										iterator.add(acmd);
									}
									if (reprocess) {
										for (int ax = 0; ax < aliasCommands.size(); ax++) {
											iterator.previous();
										}
									}
									didReplace = true;
									i = mPlugins.size();
								}
							}
						}
							
						if (!didReplace) {
							if (tmp != null) {
								if (m) {
									String srv = new String(tmp, mSettings.getEncoding()) + mCRLF;
									mDataToServer.append(new String(srv));
									mDataToWindow.append(new String(tmp, mSettings.getEncoding()) + ";");
								} else {
									String srv = new String(tmp, mSettings.getEncoding()) + mCRLF;
									mDataToServer.append(new String(srv));
								}
							} else {
								mDataToServer.append(d.mCmdString + mCRLF);
								mDataToWindow.append(d.mCmdString);
							}
						}
							
					}
					
						//dataToServer.append(d.cmdString + crlf);
					if (d.mVisString != null && !d.mVisString.equals("")) {
						if (!m) {
							mDataToWindow.append(d.mVisString + ";");
						}
					}
				}
			

			}
		}
		//7 - return Data packet with commands to send to server, and data to send to window.
		Data d = new Data();
		d.mCmdString = mDataToServer.toString();
		d.mVisString = mDataToWindow.toString();
		
		if (d.mVisString.endsWith(";")) {
			d.mVisString = d.mVisString.substring(0, d.mVisString.length() - 1);
		}
		if (!d.mVisString.endsWith(mCRLF)) {
			d.mVisString = d.mVisString + mCRLF;
		}
		return d;
	}
	
	/** Semicolon splitting routine that looks for ;; smartly.
	 * 
	 * @param string The string to process.
	 * @return The resulting list of strings.
	 */
	private List<String> splitSemicolonSafe(final String string) {
		List<String> list = new ArrayList<String>();
		mSemiMatcher.reset(string);
		boolean matched = false;
		boolean append = false;
		boolean firstSemi = true;
		//int lastLength = -1;
		while (mSemiMatcher.find()) {
			matched = true;
			mCommandBuilder.setLength(0);
			
			mSemiMatcher.appendReplacement(mCommandBuilder, "");
			if (mCommandBuilder.length() == 0) {
				append = true;
				if (list.size() == 0) {
					if (!firstSemi) {
						list.add(";");
					} else {
						firstSemi = false; //don't add the first one, but add subsequent ones.
					}
				} else {
					list.add(list.remove(list.size() - 1) + ";");
				}
			} else {
				if (append) {
					if (list.size() == 0) {
						list.add(";");
					} else {
						list.add(list.remove(list.size() - 1) + mCommandBuilder.toString());
					}
					append = false;
				} else {
					list.add(mCommandBuilder.toString());
				}
				
			}
		} 
		
		if (!matched) {
			list.add(string);
		} else {
			mCommandBuilder.setLength(0);
			mSemiMatcher.appendTail(mCommandBuilder);
			if (append) {
				if(list.size() != 0) {
					list.add(list.remove(list.size() - 1) + mCommandBuilder.toString());
				}
			} else {
				list.add(mCommandBuilder.toString());
			}
		}
		
		mCommandBuilder.setLength(0);
		return list;
	}
	
	/** Utility class for alias replacement and special command parsing routine. */
	public class Data {
		/** The string to send to the server. */
		private String mCmdString;
		/** The string to echo back to the input window. */
		private String mVisString;
		/** Generic constructor. */
		public Data() {
			mCmdString = "";
			mVisString = "";
		}
		/** Cmd string getter. 
		 * 
		 * @return The string.
		 */
		public final String getCmdString() {
			return mCmdString;
		}
		/** Vis string getter.
		 * 
		 * @return The string.
		 */
		public final String getVisString() {
			return mVisString;
		}
		/** Vis string setter. 
		 * 
		 * @param vis Desired string.
		 */
		public final void setVisString(final String vis) {
			this.mVisString = vis;
		}
		/** Cmd string setter. 
		 * 
		 * @param cmd Desired string.
		 */
		public final void setCmdString(final String cmd) {
			this.mCmdString = cmd;
		}
	}
	
	/** Data generator for outside package use of the Data class.
	 * 
	 * @return A new data
	 */
	/*public static Data makeData() {
		return new Data();
	}*/
	
	/** Generic command processor. This looks for "." commands.
	 * 
	 * @param cmd The input string to parse.
	 * @return The Data object containing the string to return to the server and the string to return to the window.
	 */
	public final Data processCommand(final String cmd) {
		Data data = new Data();
		if (cmd.equals(".." + "\n") || cmd.equals("..")) {
			synchronized (mSettings) {
				String outputmsg = "\n" + Colorizer.getRedColor() + "Dot command processing ";
				if (mSettings.isProcessPeriod()) {
					//the_settings.setProcessPeriod(false);
					overrideProcessPeriods(false);
					outputmsg = outputmsg.concat("disabled.");
				} else {
					//the_settings.setProcessPeriod(true);
					overrideProcessPeriods(true);
					outputmsg = outputmsg.concat("enabled.");
				}
				outputmsg = outputmsg.concat(Colorizer.getWhiteColor() + "\n");
				try {
					sendBytesToWindow(outputmsg.getBytes(mSettings.getEncoding()));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
			
			return null;
		}
		
		
		if (cmd.startsWith(".") && mSettings.isProcessPeriod()) {
			
			if (cmd.startsWith("..")) {
				data.mCmdString = cmd.replace("..", ".");
				data.mVisString = cmd.replace("..", ".");
				return data;
			}
			
			
			mCommandMatcher.reset(cmd);
			if (mCommandMatcher.find()) {
				synchronized (mSettings) {
					
					//string should be of the form .aliasname |settarget can have whitespace|

						String alias = mCommandMatcher.group(1);
						String argument = mCommandMatcher.group(2);
						
						
						if (mSettings.getSettings().getAliases().containsKey(alias)) {
							//real argument
							if (!argument.equals("")) {
								AliasData mod = mSettings.getSettings().getAliases().remove(alias);
								mod.setPost(argument);
								mSettings.getSettings().getAliases().put(alias, mod);
								data.mCmdString = "";
								if (mSettings.isEchoAliasUpdates()) {
									data.mVisString = "[" + alias + "=>" + argument + "]";
								} else {
									data.mVisString = "";
								}
								return data;
							} else {
								//display error message
								String noargMessage = "\n" + Colorizer.getRedColor() + " Alias \"" + alias + "\" can not be set to nothing. Acceptable format is \"."
													+ alias + " replacetext\"" + Colorizer.getWhiteColor() + "\n";
								try {
									sendBytesToWindow(noargMessage.getBytes(mSettings.getEncoding()));
								} catch (UnsupportedEncodingException e) {
									throw new RuntimeException(e);
								}
								return null;
							}
						} else if (mSpecialCommands.containsKey(alias)) {
							//Log.e("SERVICE","SERVICE FOUND SPECIAL COMMAND: " + alias);
							SpecialCommand command = mSpecialCommands.get(alias);
							data = (Data) command.execute(argument, this);
							return data;
						} else {
							//format error message.
							
							String error = Colorizer.getRedColor() + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
							error += "  \"" + alias + "\" is not a recognized alias or command.\n";
							error += "   No data has been sent to the server. If you intended\n";
							error += "   this to be done, please type \".." + alias + "\"\n";
							error += "   To toggle command processing, input \"..\" with no arguments\n";
							error += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]" + Colorizer.getWhiteColor() + "\n";  
							
							try {
								sendBytesToWindow(error.getBytes(mSettings.getEncoding()));
							} catch (UnsupportedEncodingException e) {
								throw new RuntimeException(e);
							}
							return null;
						}
					}
			}
			return data;
		} else {
			data.mCmdString = cmd;
			data.mVisString = cmd;
			return data;
		}
		
	}
	
	/** Overrides the process special commands setting and sets a new value.
	 * 
	 * @param value The new value for the process periods command.
	 */
	private void overrideProcessPeriods(final boolean value) {
		synchronized (mSettings) { //not sure why this is here.
			mSettings.setProcessPeriod(value);
		}
	}
	
	/** Switches to another open connection.
	 * 
	 * @param connection Name of the connection to switch to.
	 */
	public final void switchTo(final String connection) {
		mService.switchTo(connection);
	}
	
	/** Gets the current window token list in loaded order as an array.
	 * 
	 * @return The array of window tokens in loaded order.
	 */
	public final WindowToken[] getWindows() {
		if (mLoaded) {
			WindowToken[] tmp = new WindowToken[mWindows.size()];
			tmp = mWindows.toArray(tmp);
			return tmp;
		} else {
			return null;
		}
	}

	/** Called from the foreground window. This method fetches a named script body from a plugin.
	 * 
	 * @param plugin The plugin to look in.
	 * @param name The name of the script to fetch.
	 * @return The script body.
	 */
	public final String getScript(final String plugin, final String name) {
		for (Plugin p : mPlugins) {
			if (p.getSettings().getName().equals(plugin)) {
				if (p.getSettings().getScripts().containsKey(name)) {
					ScriptData d = p.getSettings().getScripts().get(name);
					return d.getData();
				} else {
					return "";
				}
			}
		}
		
		if (mSettings.getSettings().getScripts().containsKey(name)) {
			ScriptData d = mSettings.getSettings().getScripts().get(name);
			return d.getData();
		} else {
			return "";
		}
	}

	/** Calls an anonymous global function in the target plugin with arguments.
	 * 
	 * @param id The ID of the plugin to target.
	 * @param callback The name of the desired function to execute.
	 * @param args The data to supply to @param callback.
	 */
	public final void executeFunctionCallback(final int id, final String callback, final String args) {
		Plugin p = mPlugins.get(id);
		p.execute(callback, args);
	}

	/** The reciever of the foreground window PluginXCallS Lua function.
	 * 
	 * @param plugin The name of the plugin to look in.
	 * @param function The name of the anonymous global function to call.
	 * @param str The argument to pass to <b>function</b>.
	 */
	public final void pluginXcallS(final String plugin, final String function, final String str) {
		for (Plugin p : mPlugins) {
			if (p.getName().equals(plugin)) {
				p.xcallS(function, str);
			}
		}
	}

	/** Helper method for reverse mapping R.java constants from name to id.
	 * 
	 * @param variableName The desired field name e.g. "alias_dialog".
	 * @param context THe current application context to use.
	 * @param c The class to search, this is usually R.layout or R.drawable.
	 * @return The integer id of <b>variableName</b> or -1 if the class does not have a field named <b>variableName</b>.
	 */
	public static int getResId(final String variableName, final Context context, final Class<?> c) {

	    try {
	        Field idField = c.getDeclaredField(variableName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } 
	}

	/** Helper function to get a window by name.
	 * 
	 * @param desired The name of the window to look up.
	 * @return The WindowToken for the corresponding window name.
	 */
	public final WindowToken getWindowByName(final String desired) {
		for (int i = 0; i < mWindows.size(); i++) {
			WindowToken t = mWindows.get(i);
			if (t.getName().equals(desired)) {
				return t;
			}
		}
		return null;
	}

	/** Helper function to get the triggers for the main conenction settings.
	 * 
	 * @return the triggers for the main connection settings.
	 */
	public final HashMap<String, TriggerData> getTriggers() {
		return mSettings.getSettings().getTriggers();
	}

	/** Helper function to get the triggers for a given plugin.
	 * 
	 * @param name The name of the plugin to interrogate.
	 * @return The triggers of the given plugin, null if <b>name</b> does not correspond to a loaded plugin.
	 */
	public final HashMap<String, TriggerData> getPluginTriggers(final String name) {
		Plugin p = mPluginMap.get(name);
		if (p != null) {
			return p.getSettings().getTriggers();
		} else {
			return null;
		}
	}

	/** Adds a trigger into the main settings plugin.
	 * 
	 * @param data The trigger to add.
	 */
	public final void addTrigger(final TriggerData data) {
		mSettings.addTrigger(data);
	}

	/** Updates a trigger in the main settings plugin.
	 * 
	 * @param from Old trigger.
	 * @param to New trigger.
	 */
	public final void updateTrigger(final TriggerData from, final TriggerData to) {
		mSettings.updateTrigger(from, to);
	}

	/** Updates a trigger in the target plugin.
	 * 
	 * @param selectedPlugin Name of the plugin to work in.
	 * @param from Old plugin.
	 * @param to New plugin.
	 */
	public final void updatePluginTrigger(final String selectedPlugin, final TriggerData from,
			final TriggerData to) {
		Plugin p = mPluginMap.get(selectedPlugin);
		if (p != null) {
			p.updateTrigger(from, to);
		}
	}

	/** Adds a new trigger in the target plugin.
	 * 
	 * @param selectedPlugin Target plugin for the new trigger.
	 * @param data The new trigger.
	 */
	public final void newPluginTrigger(final String selectedPlugin, final TriggerData data) {
		Plugin p = mPluginMap.get(selectedPlugin);
		if (p != null) {
			p.addTrigger(data);
		}
	}

	/** Gets a trigger in the target plugin.
	 * 
	 * @param selectedPlugin Name of the plugin to look in.
	 * @param pattern Name of the desired trigger.
	 * @return The trigger, <b>null</b> if it does not exist.
	 */
	public final TriggerData getPluginTrigger(final String selectedPlugin, final String pattern) {
		Plugin p = mPluginMap.get(selectedPlugin);
		if (p != null) {
			return p.getSettings().getTriggers().get(pattern);
		} else {
			return null;
		}
	}

	/** Gets a trigger from the main settings plugin.
	 * 
	 * @param pattern Name of the trigger to get.
	 * @return The trigger, <b>null</b> if it does not exist.
	 */
	public final TriggerData getTrigger(final String pattern) {
		return mSettings.getSettings().getTriggers().get(pattern);
	}

	/** Sets the enabled state of a trigger in the target plugin.
	 * 
	 * @param selectedPlugin Name of the target plugin to affect.
	 * @param enabled Desired state of the trigger.
	 * @param key The name of the trigger to affect.
	 */
	public final void setPluginTriggerEnabled(final String selectedPlugin, final boolean enabled,
			final String key) {
		Plugin p = mPluginMap.get(selectedPlugin);
		if (p != null) {
			TriggerData data = p.getSettings().getTriggers().get(key);
			if (data != null) {
				data.setEnabled(enabled);
				p.getSettings().setDirty(true);
				buildTriggerSystem();
			}
		}
	}
	
	/** Sets the enabled state of a trigger in the main settings plugin.
	 * 
	 * @param enabled Desired state of the target trigger.
	 * @param key Name of the trigger to affect.
	 */
	public final void setTriggerEnabled(final boolean enabled, final String key) {
		TriggerData data = mSettings.getSettings().getTriggers().get(key);
		if (data != null) {
			data.setEnabled(enabled);
			buildTriggerSystem();
		}
	}

	/** Removes a trigger from the target plugin.
	 * 
	 * @param selectedPlugin Name of the plugin to search in.
	 * @param which Name of the trigger to remove.
	 */
	public final void deletePluginTrigger(final String selectedPlugin, final String which) {
		Plugin p = mPluginMap.get(selectedPlugin);
		if (p != null) {
			p.getSettings().getTriggers().remove(which);
			p.getSettings().setDirty(true);
			p.sortTriggers();
		}
		buildTriggerSystem();
	}

	/** Removes a trigger from the main settings plugin.
	 * 
	 * @param which Name of the trigger to remove.
	 */
	public final void deleteTrigger(final String which) {
		mSettings.getSettings().getTriggers().remove(which);
		mSettings.sortTriggers();
		buildTriggerSystem();
	}

	/** Sets the aliases for the main settings plugin. This comes from the foreground window in one glob.
	 * 
	 * @param map The new alias map (HashMap<String, AliasData>).
	 */
	public final void setAliases(final HashMap<String, AliasData> map) {
		mSettings.getSettings().setAliases(map);
		mSettings.buildAliases();
	}
	
	/** Sets the aliases for a given plugin. This comes from the foreground window in one glob.
	 * 
	 * @param plugin Name of the target plugin to affect.
	 * @param map The new alias map (HashMap<String, AliasData>)
	 */
	public final void setPluginAliases(final String plugin, final HashMap<String, AliasData> map) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.getSettings().setAliases(map);
			p.getSettings().setDirty(true);
			p.buildAliases();
		}
	}
	
	/** Gets an alias for a target plugin.
	 * 
	 * @param plugin Name of the plugin to search.
	 * @param key The pre part of the alias.
	 * @return The AliasData associated with <b>key</b>.
	 */
	public final AliasData getPluginAlias(final String plugin, final String key) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			return p.getSettings().getAliases().get(key);
		}
		return null;
	}
	
	/** Gets an alias from the main settings plugin.
	 * 
	 * @param key The pre part of the alias.
	 * @return The AliasData associated with <b>key</b>
	 */
	public final AliasData getAlias(final String key) {
		return mSettings.getSettings().getAliases().get(key);
	}
	
	/** Removes an alias form the main settings plugin.
	 * 
	 * @param key The pre part of the alias to delete.
	 */
	public final void deleteAlias(final String key) {
		mSettings.getSettings().getAliases().remove(key);
	}
	
	/** Removes an alias from the target plugin.
	 * 
	 * @param plugin The name of the plugin to affect.
	 * @param key The pre part of the alias to remove.
	 */
	public final void deletePluginAlias(final String plugin, final String key) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.getSettings().getAliases().remove(key);
		}
	}
	
	/** Gets the alias map for the main settings plugin.
	 * 
	 * @return The alais map for the main settings plugin.
	 */
	public final HashMap<String, AliasData> getAliases() {
		return mSettings.getSettings().getAliases();
	}
	
	/** Gets the alias map for a target plugin.
	 * 
	 * @param plugin The desired plugin to interrogate.
	 * @return The alias map for <b>plugin</b>.
	 */
	public final HashMap<String, AliasData> getPluginAliases(final String plugin) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			return p.getSettings().getAliases();
		} else {
			return null;
		}
	}
	
	/** Gets the list of all the installed system commands.
	 * 
	 * @return The system command list.
	 */
	public final ArrayList<String> getSystemCommands() {
		ArrayList<String> list = new ArrayList<String>();
		Set<String> keys = mSpecialCommands.keySet();
		for (String key : keys) {
			list.add(key);
		}
		return list;
	}

	/** Sets the enabled state of an alias in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param enabled Desired state of the alias.
	 * @param key The pre part of the alias to affect.
	 */
	public final void setPluginAliasEnabled(final String plugin, final boolean enabled, final String key) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			AliasData data = p.getSettings().getAliases().get(key);
			if (data != null) {
				data.setEnabled(enabled);
				p.getSettings().setDirty(true);
				p.buildAliases();
			}
		}
	}

	/** Sets the enabled state of an alias in the main settings plugin.
	 * 
	 * @param enabled Desired state of the alias.
	 * @param key The pre part of the alias to affect.
	 */
	public final void setAliasEnabled(final boolean enabled, final String key) {
		AliasData data = mSettings.getSettings().getAliases().get(key);
		if (data != null) {
			data.setEnabled(enabled);
			mSettings.buildAliases();
		}
	}

	/** Helper function for the keyboard command. Does an alias replacment in a special kind of way.
	 * 
	 * @param bytes Bytes to process.
	 * @param reprocess Weather to do recursive alias replacement.
	 * @return The processed command bytes.
	 */
	public final byte[] doKeyboardAliasReplace(final byte[] bytes, final Boolean reprocess) {
		int count = mPlugins.size();
		for (int i = 0; i < count; i++) {
			Plugin p = mPlugins.get(i);
			byte[] tmp = p.doAliasReplacement(bytes, reprocess);
			if (tmp.length != bytes.length) {
				return tmp;
			} else {
				boolean same = true;
				for (int j = 0; j < tmp.length; j++) {
					if (tmp[j] != bytes[j]) {
						same = false;
						j = tmp.length;
					}
				}
				if (!same) {
					return tmp;
				}
			}
		}
		
		return bytes;
	}

	/** Helper method that kicks off the reconnection sequence. */
	public final void startReconnect() {
		mHandler.sendEmptyMessage(MESSAGE_RECONNECT);
	}
	
	/** Helper method to initiate a reconnect right now. */
	public final void doReconnect() {
		if (mPump != null) {
			if (mPump.getHandler() != null) {
				mPump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_END);
			}
			mPump = null;
		}
		
		doStartup();
	}

	/** Removes a timer from the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param name Name of the timer to remove.
	 */
	public final void deletePluginTimer(final String plugin, final String name) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.getSettings().getTimers().remove(name);
		}
	}

	/** Gets a timer from the main settings plugin.
	 * 
	 * @param name Name of the timer to get.
	 * @return The timer associated with <b>name</b>.
	 */
	public final TimerData getTimer(final String name) {
		return mSettings.getSettings().getTimers().get(name);
	}

	/** Removes a timer from the main settings plugin.
	 * 
	 * @param name Name of the trigger to remove.
	 */
	public final void deleteTimer(final String name) {
		mSettings.getSettings().getTimers().remove(name);
	}

	/** Gets a timer from the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param name Name of the trigger to get.
	 * @return The trigger associated with <b>name</b>.
	 */
	public final TimerData getPluginTimer(final String plugin, final String name) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			return p.getSettings().getTimers().get(name);
		} else {
			return null;
		}
	}

	/** Adds a timer to the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param newtimer New timer data.
	 */
	public final void addPluginTimer(final String plugin, final TimerData newtimer) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			newtimer.setRemainingTime(newtimer.getSeconds());
			p.getSettings().getTimers().put(newtimer.getName(), newtimer);
			p.getSettings().setDirty(true);
		}
	}

	/** Updates a timer in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param old Old timer data.
	 * @param newtimer New timer data.
	 */
	public final void updatePluginTimer(final String plugin, final TimerData old,
		final TimerData newtimer) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.getSettings().getTimers().remove(old.getName());				
			p.getSettings().getTimers().put(newtimer.getName(), newtimer);
			p.getSettings().setDirty(true);
		}
		
	}

	/** Updates a timer in the main settings plugin.
	 * 
	 * @param old Old timer data.
	 * @param newtimer New timer data.
	 */
	public final void updateTimer(final TimerData old, final TimerData newtimer) {
		mSettings.getSettings().getTimers().remove(old.getName());
		mSettings.getSettings().getTimers().put(newtimer.getName(), newtimer);
	}

	/** Gets the timer map for the main settings plugin.
	 * 
	 * @return The timer map.
	 */
	public final HashMap<String, TimerData> getTimers() {
		mSettings.updateTimerProgress();
		return mSettings.getSettings().getTimers();
	}

	/** Gets the timer map for a target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @return The tier map.
	 */
	public final HashMap<String, TimerData> getPluginTimers(final String plugin) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.updateTimerProgress();
			return p.getSettings().getTimers();
		} else {
			return null;
		}
	}

	/** Adds a new timer into the main settings plugin.
	 * 
	 * @param newtimer New timer to add.
	 */
	public final void addTimer(final TimerData newtimer) {
		newtimer.setRemainingTime(newtimer.getSeconds());
		mSettings.getSettings().getTimers().put(newtimer.getName(), newtimer);
		mSettings.getSettings().setDirty(true);
	}

	/** Helper method to see if the window is currently being shown.
	 * 
	 * @return visibility state of the foreground window.
	 */
	public final boolean isWindowShowing() {
		return mService.isWindowConnected();
	}
	
	/** Getter method for mDisplay.
	 * 
	 * @return the display name for this connection.
	 */
	public final String getDisplayName() {
		return mDisplay;
	}
	
	/** Helper method for getting the application context out here in the desert of the Service.
	 * 
	 * @return The application context.
	 */
	public final Context getContext() {
		return mService.getApplicationContext();
	}
	
	/** Starts a timer in the main settings plugin with the target name.
	 * 
	 * @param key Name of the timer to start.
	 */
	public final void playTimer(final String key) {
		mSettings.startTimer(key);
	}
	
	/** Starts a timer in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param timer Name of the timer to start.
	 */
	public final void playPluginTimer(final String plugin, final String timer) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.startTimer(timer);
		}
	}
	
	/** Pauses a timer in the main settings plugin.
	 * 
	 * @param key Name of the plugin to pause.
	 */
	public final void pauseTimer(final String key) {
		mSettings.pauseTimer(key);
	}
	
	/** Pauses a timer in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param timer Name of the timer to pause.
	 */
	public final void pausePluginTimer(final String plugin, final String timer) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.pauseTimer(timer);
		}
	}
	
	/** Stops a timer in the main settings plugin.
	 * 
	 * @param key Name of the timer to stop.
	 */
	public final void stopTimer(final String key) {
		mSettings.stopTimer(key);
	}
	
	/** Stops a timer in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param key Name of the timer to stop.
	 */
	public final void stopPluginTimer(final String plugin, final String key) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.stopTimer(key);
		}
	}

	/** Gets the settings object for the main settings plugin.
	 * 
	 * @return The settings for the main settings plugin.
	 */
	public final SettingsGroup getSettings() {
		if (mSettings == null) {
			return new SettingsGroup();
		}
		return mSettings.getSettings().getOptions();
	}
	
	/** Gets the settings object for a target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @return The settings for the target plugin. Returns null if <b>name</b> is not a loaded plugin.
	 */
	public final SettingsGroup getPluginSettings(final String plugin) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			return p.getSettings().getOptions();
		} else {
			return null;
		}
	}

	/** Updates a boolean setting in the main settings plugin.
	 * 
	 * @param key id of the setting to affect.
	 * @param value new value for setting <b>key</b>
	 */
	public final void updateBooleanSetting(final String key, final boolean value) {
		mSettings.updateBooleanSetting(key, value);
	}
	
	/** Updates a boolean setting in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param key key id of the setting to affect.
	 * @param value the value to use.
	 */
	public final void updatePluginBooleanSetting(final String plugin, final String key, final boolean value) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.updateBooleanSetting(key, value);
		}
	}
	
	/** Updates a string setting in the main settings plugin.
	 * 
	 * @param key key id of the setting to affect.
	 * @param value the value to use.
	 */
	public final void updateStringSetting(final String key, final String value) {
		mSettings.updateStringSetting(key, value);
	}
	
	/** Updates a string setting in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param key key id of the setting to affect.
	 * @param value the value to use.
	 */
	public final void updatePluginStringSetting(final String plugin, final String key, final String value) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.updateStringSetting(key, value);
		}
	}
	
	/** Udpates an integer setting in the main settings plugin.
	 * 
	 * @param key key id of the setting to affect.
	 * @param value the value to use.
	 */
	public final void updateIntegerSetting(final String key, final int value) {
		mSettings.updateIntegerSetting(key, value);
	}
	
	/** Updates an integer setting in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param key key id of the setting to affect.
	 * @param value the value to use.
	 */
	public final void updatePluginIntegerSetting(final String plugin, final String key, final int value) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.updateIntegerSetting(key, value);
		}
	}
	
	/** Updates a float setting in the main settings plugin.
	 * 
	 * @param key key id of the setting to update.
	 * @param value the value to use.
	 */
	public final void updateFloatSetting(final String key, final float value) {
		mSettings.updateFloatSetting(key, value);
	}
	
	/** Updates a float setting in the target plugin.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param key key id of the setting to affect.
	 * @param value the value to use.
	 */
	public final void updatePluginFloatSetting(final String plugin, final String key, final float value) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.updateFloatSetting(key, value);
		}
	}
	
	/** Utility class for tracking changes ot the main window settings. */
	private class WindowSettingsChangedListener implements SettingsChangedListener {
		/** Name of the window that this listener is watching. */
		private String mWindow;
		
		/** Constructor.
		 * 
		 * @param window Name of the window to watch.
		 */
		public WindowSettingsChangedListener(final String window) {
			this.mWindow = window;
		}
		
		@Override
		public void updateSetting(final String key, final String value) {
			Connection.this.handleWindowSettingsChanged(mWindow, key, value);
		}
		
	}
	
	/** Work horse of the main window settings change listener.
	 * 
	 * @param window Name of the window that was affected.
	 * @param key Name of the key that changed.
	 * @param value The value that it was changed to.
	 */
	public final void handleWindowSettingsChanged(final String window, final String key, final String value) {

			IWindowCallback callback = mWindowCallbackMap.get(window);
			if (callback == null) {
				return;
			}
			try {
				callback.updateSetting(key, value);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}

	@Override
	public final void updateSetting(final String key, final String value) {
		if (mSettings == null) {
			return; //this is for when the settings are first being loaded.
		}
		BaseOption o = (BaseOption) mSettings.getSettings().getOptions().findOptionByKey(key);
		try {
			KEYS tmp = KEYS.valueOf(key);
			switch (tmp) {
			case process_semicolon:
				mSettings.setSemiIsNewLine((Boolean) o.getValue());
				break;
			case debug_telnet:
				if (mProcessor != null) {
					mProcessor.setDebugTelnet((Boolean) o.getValue());
				}
				break;
			case encoding:
				this.doUpdateEncoding((String) o.getValue());
				break;
			case orientation:
				mService.doExecuteSetOrientation((Integer) o.getValue());
				break;
			case screen_on:
				mService.doExecuteKeepScreenOn((Boolean) o.getValue());
				break;
			case fullscreen:
				mService.doExecuteFullscreen((Boolean) o.getValue());
				break;
			case fullscreen_editor:
				mService.doExecuteFullscreenEditor((Boolean) o.getValue());
				break;
			case use_suggestions:
				mService.doExecuteUseSuggestions((Boolean) o.getValue());
				break;
			case keep_last:
				this.doSetKeepLast((Boolean) o.getValue());
				break;
			case compatibility_mode:
				mService.doExecuteCompatibilityMode((Boolean) o.getValue());
				break;
			case local_echo:
				this.doSetLocalEcho((Boolean) o.getValue());
				break;
			case process_system_commands:
				this.doSetProcessSystemCommands((Boolean) o.getValue());
				break;
			case echo_alias_updates:
				this.doSetAliasUpdates((Boolean) o.getValue());
				break;
			case keep_wifi_alive:
				this.doSetKeepWifiAlive((Boolean) o.getValue());
				break;
			case auto_reconnect:
				this.setAutoReconnect((Boolean) o.getValue());
				break;
			case auto_reconnect_limit:
				this.setAutoReconnectLimit((Integer) o.getValue());
				break;
			case cull_extraneous_color:
				this.doSetCullExtraneousColor((Boolean) o.getValue());
				break;
			case debug_telent:
				this.doSetDebugTelnet((Boolean) o.getValue());
				break;
			case bell_vibrate:
				this.doSetBellVibrate((Boolean) o.getValue());
				break;
			case bell_notification:
				this.doSetBellNotify((Boolean) o.getValue());
				break;
			case bell_display:
				this.doSeBellDisplay((Boolean) o.getValue());
				break;
			case show_regex_warning:
				doSetRegexWarning((Boolean) o.getValue());
				break;
			case use_gmcp:
				this.doSetUseGMCP((Boolean) o.getValue());
				break;
			case gmcp_supports:
				this.doSetGMCPSupports((String) o.getValue());
				break;
			default:
				break;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	/** Implementation of the gmcp supports string setting handler.
	 * 
	 * @param value New value for setting.
	 */
	private void doSetGMCPSupports(final String value) {
		mProcessor.setGMCPSupports(value);
	}

	/** Implementation of the use gmcp settings handler.
	 * 
	 * @param value New value for setting.
	 */
	private void doSetUseGMCP(final Boolean value) {
		mProcessor.setUseGMCP(value);
	}

	/** Implementation of the auto reconnect attempt limit settings handler.
	 * 
	 * @param value New value for setting.
	 */
	private void setAutoReconnectLimit(final Integer value) {
		mAutoReconnectLimit = value;
	}

	/** Impelementation of the use auto reconnect settings handler.
	 * 
	 * @param value New value for setting.
	 */
	private void setAutoReconnect(final Boolean value) {
		mAutoReconnect = value;
	}

	/** Impelemntation of the bell vibrate settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetBellVibrate(final Boolean value) {
		mSettings.setVibrateOnBell(value);		
	}
	
	/** Impelemntation of the bell notify settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetBellNotify(final Boolean value) {
		mSettings.setNotifyOnBell(value);
	}
	
	/** Impelemntation of the bell toast settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSeBellDisplay(final Boolean value) {
		mSettings.setDisplayOnBell(value);
	}

	/** Impelemntation of the set debug telnet settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetDebugTelnet(final Boolean value) {
		mSettings.setDebugTelnet(value);
		if (mProcessor != null) {
			mProcessor.setDebugTelnet(value);
		}
	}

	/** Impelemntation of the cull extraneous colors settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetCullExtraneousColor(final Boolean value) {
		mSettings.setRemoveExtraColor(value);
		mWindows.get(0).getBuffer().setCullExtraneous(value);
	}

	/** Impelemntation of the keep wifi alive settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetKeepWifiAlive(final Boolean value) {
		mSettings.setKeepWifiActive(value);
		if (value) {
			mService.enableWifiKeepAlive();
		} else {
			mService.disableWifiKeepAlive();
		}
	}

	/** Impelemntation of the echo alias update settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetAliasUpdates(final Boolean value) {
		mSettings.setEchoAliasUpdates(value);
	}

	/** Impelemntation of the process system commands settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetProcessSystemCommands(final Boolean value) {
		mSettings.setProcessPeriod(value);
	}

	/** Impelemntation of the local echo settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetLocalEcho(final Boolean value) {
		mSettings.setLocalEcho(value);
	}

	/** Impelemntation of the keep last settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetKeepLast(final Boolean value) {
		mService.dispatchKeepLast(value);
	}
	
	/** Impelemntation of the show regex warning handler.
	 * 
	 * @param value New value to use.
	 */
	private void doSetRegexWarning(final Boolean value) {
		mService.dispatchShowRegexWarning(value);
	}
	

	/** Impelemntation of the system encoding settings handler.
	 * 
	 * @param value New value to use.
	 */
	private void doUpdateEncoding(final String value) {
		if (mProcessor == null) { return; }
		mProcessor.setEncoding(value);
		//this.encoding = value;
		mSettings.setEncoding(value);
		this.mWorking.setEncoding(value);
		this.mFinished.setEncoding(value);
		if (mProcessor != null) {
			this.mProcessor.setEncoding(value);
		}
		for (int i = 0; i < mWindows.size(); i++) {
			WindowToken w = mWindows.get(i);
			w.getBuffer().setEncoding(value);
		}
		
		for (IWindowCallback w : mWindowCallbackMap.values()) {
			//IWindowCallback w = mWindowCallbacks.getBroadcastItem(i);
			try {
				w.setEncoding(value);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < mPlugins.size(); i++) {
			Plugin p = mPlugins.get(i);
			p.setEncoding(value);
		}
		
		//handle the keyboard command callback.
		mKeyboardCommand.setEncoding(value); 
		
		//may want to go through and activate the settings changed handler for plugins.
		//the chat window would want to re-construct it's buffers. But for proper operation
		//it may not be out of the question to make encoding change requrie a restart.
		//everything that doesn't use TextTree's directly to make multi-buffers, will work fine.		
	}

	/** Helper enum to map the main settings plugin's settings keys to strings. */
	private enum KEYS {
		/** Semicolon processing. */
		process_semicolon,
		/** Debug telnet. */
		debug_telnet,
		/** System encoding. */
		encoding, 
		/** Window orientation. */
		orientation, 
		/** Keep screen on. */
		screen_on, 
		/** Hide notification bar. */
		fullscreen, 
		/** Use fullscreen editor. */
		fullscreen_editor,
		/** Make editor use suggestions. */
		use_suggestions,
		/** Keep last entered. */
		keep_last, 
		/** Input compatibility mode. */
		compatibility_mode,
		/** Local echo. */
		local_echo,
		/** Process period commands. */
		process_system_commands,
		/** Echo alias updates. */
		echo_alias_updates,
		/** Keep wifi alive. */
		keep_wifi_alive,
		/** Cull extraneous color codes. */
		cull_extraneous_color,
		/** Debug telnet data. */
		debug_telent,
		/** Bell vibrates. */
		bell_vibrate,
		/** Bell notifies. */
		bell_notification,
		/** Bell toasts. */
		bell_display, 
		/** Auto reconnect. */
		auto_reconnect, 
		/** Auto reconnect limit. */
		auto_reconnect_limit, 
		/** Use GMCP. */
		use_gmcp, 
		/** GMCP Supports string. */
		gmcp_supports, 
		/** Show Regex Warning. */
		show_regex_warning
	}
	
	/** Work horse function of sending data to the server, this initiates all levels of processing.
	 * 
	 * @param bytes Input to process.
	 */
	private void sendToServer(final byte[] bytes) {
		if (bytes == null || mSettings == null) {
			return;
		}
		Data d = null;
		try {
			d = processOutputData(new String(bytes, mSettings.getEncoding()));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		if (d == null) {
			return;
		}
		
		if (d.mCmdString.equals("") && (d.mVisString != null && d.mVisString.replaceAll("\\s", "").equals(""))) {
			return;
		}
		
		String nosemidata = null;
		try {
			
			if (d.mCmdString != null && !d.mCmdString.equals("")) {
				nosemidata = d.mCmdString;
				byte[] sendtest = nosemidata.getBytes(mSettings.getEncoding());
				ByteBuffer buf = ByteBuffer.allocate(sendtest.length * 2); //just in case EVERY byte is the IAC
				int count = 0;
				for (int i = 0; i < sendtest.length; i++) {
					if (sendtest[i] == TC.IAC) {
						buf.put(TC.IAC);
						buf.put(TC.IAC);
						count += 2;
					} else {
						buf.put(sendtest[i]);
						count++;
					}
				}
				
				byte[] tosend = new byte[count];
				buf.rewind();
				buf.get(tosend, 0, count);
				
				if (mPump != null && mPump.isConnected()) {
					mPump.sendData(tosend);
				} else {
					sendBytesToWindow(new String(Colorizer.getRedColor() + "\nDisconnected.\n" + Colorizer.getWhiteColor()).getBytes("UTF-8"));
				}
			} else {
				if (d.mCmdString.equals("") && d.mVisString == null) {
					mPump.sendData(mCRLF.getBytes(mSettings.getEncoding()));
					d.mVisString = "\n";
				}
			}
			if (d.mVisString != null && !d.mVisString.equals("")) {
				if (mSettings.isLocalEcho()) {
					mWindows.get(0).getBuffer().addBytesImplSimple(d.mVisString.getBytes(mSettings.getEncoding()));
					sendBytesToWindow(d.mVisString.getBytes(mSettings.getEncoding()));
				}
			}
		} catch (IOException e) {
			mHandler.sendEmptyMessage(MESSAGE_DISCONNECTED);
		}
	}

	/** Possibly Deprecated. Sets the buffer size for a target window in a target plugin.
	 * 
	 * @param plugin The target plugin.
	 * @param window The target window.
	 * @param amount The new buffer size value.
	 */
	public final void updateWindowBufferMaxValue(final String plugin, final String window, final int amount) {
		for (WindowToken w : mWindows) {
			if (w.getName().equals(window)) {
				//WindowToken w = mWindows.get(0);
				w.setBufferSize(amount);
			}
		} 
	}
	
	/** The main starting point for the save settings routine. This is called for a few different locations. */
	public final void saveMainSettings() {
		Pattern invalidchars = Pattern.compile("\\W");
		Matcher replacebadchars = invalidchars.matcher(this.mDisplay);
		String prefsname = replacebadchars.replaceAll("");
		prefsname = prefsname.replaceAll("/", "");
		String rootPath = prefsname + ".xml";
		
		//rootPath is the v1 settings file name.
		String internal = mService.getApplicationContext().getApplicationInfo().dataDir + "/files/";
		String oldpath = internal + rootPath;
		exportSettings(oldpath);
	}
	
	/** Export settings routine. Called from either the main settings save routine or the export settings dialog.
	 * 
	 * @param path File name to save to. Must be absolute from the OS root directory.
	 */
	public final void exportSettings(final String path) {
		boolean domessage = false;
		boolean addextra = false;
		String filename = path;
		File cachedir = this.getContext().getCacheDir();
		if (!filename.startsWith("/")) {
			//mod
			domessage = true;
			File ext = Environment.getExternalStorageDirectory();
			String dir = ConfigurationLoader.getConfigurationValue("exportDirectory", mService.getApplicationContext());
			
			filename = ext.getAbsolutePath() + "/" + dir + "/" + filename;
			mXMLExtensionMatcher.reset(filename);
			if (!mXMLExtensionMatcher.matches()) {
				filename = filename + ".xml";
				addextra = true;
			}
			
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
				cachedir = this.getContext().getExternalCacheDir();
			} else {
				String packagename = this.getContext().getPackageName();
				cachedir = new File(Environment.getExternalStorageDirectory(),"/Android/data/"+packagename+"/cache/");
			}
		}
		
		//try to output the file.
		boolean passed = true;
		File file = new File(filename);
		//File cachedir = this.getContext().getCacheDir();
		FileOutputStream fos = null;
		File tmpfile = null;
		try {
		tmpfile = File.createTempFile("settings", "xml",cachedir);
		
		fos = new FileOutputStream(tmpfile);
		String foo = ConnectionSetttingsParser.outputXML(mSettings, mPlugins);
		fos.write(foo.getBytes());
		fos.close();
		} catch (Exception e) {
			//dispatch error.
			//do not copy files
			try {
				mService.dispatchSaveError(e.getLocalizedMessage());
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			passed = false;
		} finally {
			if(passed) {
				try {
					fos.close();
				} catch (IOException e) {
					//we are in real trouble here.
				}
				
				//copy the file over to the real path.
				boolean success = tmpfile.renameTo(file);
				if(success) {
					Log.e("BT","file shadow copy success");
				} else {
					Log.e("BT","file shadow copy failed");
				}
			} else {
				if(fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						//real trouble.
					}
				}
			}
		}
		
		
		for (String link : mLinkMap.keySet()) {
			ArrayList<String> plugins  = mLinkMap.get(link);
			boolean doExport = false;
			String fullpath = "";
			for (String plugin : plugins) {
				Plugin p = mPluginMap.get(plugin);
				if (p.getSettings().isDirty()) {
					doExport = true;
					fullpath = p.getFullPath();
				}
			}
			
			if (doExport) {
				XmlSerializer out = Xml.newSerializer();
				StringWriter writer = new StringWriter();
				
				File extfile = null;
				FileOutputStream extfilestream = null;
				passed = true;
				File extcachedir = null;
				if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
					extcachedir = this.getContext().getExternalCacheDir();
				} else {
					String packagename = this.getContext().getPackageName();
					extcachedir = new File(Environment.getExternalStorageDirectory(),"/Android/data/"+packagename+"/cache/");
				}
				//File cachedir = this.getContext().getCacheDir();
				//FileOutputStream fos = null;
				File tmppluginfile = null;
				String currentplugin = "";
				try {
				
				out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				out.setOutput(writer);
				out.startDocument("UTF-8", true);
				out.startTag("", "blowtorch");
				out.attribute("", "xmlversion", "2");
				out.startTag("", "plugins");
				
				for (String plugin :plugins) {
					currentplugin = plugin;
					Plugin p = mPluginMap.get(plugin);
					PluginParser.saveToXml(out, p);
					p.getSettings().setDirty(false);
				}
				
				out.endTag("", "plugins");
				out.endTag("", "blowtorch");
				out.endDocument();
				
				tmppluginfile = File.createTempFile("plugin_settings", "xml",extcachedir);
				
				extfile = new File(fullpath);
				extfilestream = new FileOutputStream(tmppluginfile);
				extfilestream.write(writer.toString().getBytes());
				extfilestream.close();
				} catch(Exception e) {
					try {
						mService.dispatchPluginSaveError(currentplugin,e.getLocalizedMessage());
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					passed = false;
				} finally {
					if(extfilestream != null) {
						try {
							extfilestream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					if(passed) {
						//try and copy the file over.
						long start = System.currentTimeMillis();
						boolean success = tmppluginfile.renameTo(extfile);
						int duraction = (int)(System.currentTimeMillis() - start);
						if(success) {
							Log.e("BT","Plugin shadow copy success, took " + duraction);
						} else {
							Log.e("BT","Plugin shadow copy failure, took " + duraction);
						}
					}
				}
			}
			
		}
		
		
		if (domessage) {
			String message = "Settings Exported to " + filename;
			if (addextra) {
				message = message + "\n.xml extension added.";
			}
			mService.dispatchToast(message, true);
		}
	}
	
	/** Access point for the foreground window to initate a custom export action with the provided path.
	 * 
	 * @param path Path to save settings to, this must be absolute from the root directory (?)
	 */
	public final void startExportSequence(final String path) {
		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_EXPORTFILE, path));
	}
	
	/** The real workhorse method for importing settings.
	 * 
	 * @param path The path of the settings to load.
	 * @param save flag to save the settings after loading.
	 * @param loadmessage verb for loading(true) or importing(false)
	 */
	private void importSettings(final String path, final boolean save, final boolean loadmessage) {
		shutdownPlugins();
		
		String verb = null;
		if (loadmessage) {
			verb = "Loading";
		} else {
			verb = "Importing";
		}
		
		VersionProbeParser vpp = new VersionProbeParser(path, mService.getApplicationContext());

		
		try {
			boolean isLegacy = vpp.isLegacy();
			if (isLegacy) {
				Log.e("XMLPARSE", "LOADING V1 SETTINGS FROM PATH: " + path);
				
				if(!path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
					//internal legacy file being loaded, export the settings to the external blowtorch directory.
					//convert path to external settings directory.
					//get the package path.
					//mService.getApplicationContext().getFilesDir();
					File f = new File(mService.getApplicationContext().getFilesDir(),path);
					String file = f.getName();
					File p = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/recovered/");
					if(!p.exists()) {
						p.mkdirs();
					}
					File newfile = new File(p,file);
					
					if(!newfile.exists()) {
						newfile.createNewFile();
					}
					
				    InputStream in = new FileInputStream(f);
				    OutputStream out = new FileOutputStream(newfile);

				    
				    // Transfer bytes from in to out
				    byte[] buf = new byte[1024];
				    int len;
				    while ((len = in.read(buf)) > 0) {
				        out.write(buf, 0, len);
				    }
				    in.close();
				    out.close();
					
				}
				
				HyperSAXParser p = new HyperSAXParser(path, mService.getApplicationContext());
				HyperSettings s = p.load();
				
				//load up the default settings and then merge the old settings into the new settings.
				ArrayList<Plugin> tmpplugs = new ArrayList<Plugin>();
				ConnectionSetttingsParser newsettings = new ConnectionSetttingsParser(null, mService.getApplicationContext(), tmpplugs, mHandler, this);
				tmpplugs = newsettings.load(this);
				
				Plugin buttonwindow = tmpplugs.get(1);
				ConnectionSettingsPlugin root_settings = (ConnectionSettingsPlugin)tmpplugs.get(0);
				
				if (path != null) { //import old buttons
				
				//slag out the old settings and RAM them into the new ones.
				LuaState pL = buttonwindow.getLuaState();
				
				pL.newTable();
				for (String key : s.getButtonSets().keySet()) {
					ColorSetSettings defaults = s.getSetSettings().get(key);
					//Vector<SlickButtonData> data = s.getButtonSets().get(key);
					pL.newTable();
					
					if (defaults.getPrimaryColor() != SlickButtonData.DEFAULT_COLOR) {
						pL.pushString("primaryColor");
						pL.pushNumber(defaults.getPrimaryColor());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getSelectedColor() != SlickButtonData.DEFAULT_SELECTED_COLOR) {
						pL.pushString("selectedColor");
						pL.pushNumber(defaults.getSelectedColor());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getFlipColor() != SlickButtonData.DEFAULT_FLIP_COLOR) {
						pL.pushString("flipColor");
						pL.pushNumber(defaults.getFlipColor());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getLabelColor() != SlickButtonData.DEFAULT_LABEL_COLOR) {
						pL.pushString("labelColor");
						pL.pushNumber(defaults.getLabelColor());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getFlipLabelColor() != SlickButtonData.DEFAULT_FLIPLABEL_COLOR) {
						pL.pushString("flipLabelColor");
						pL.pushNumber(defaults.getFlipLabelColor());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getButtonWidth() != SlickButtonData.DEFAULT_BUTTON_WDITH) {
						pL.pushString("width");
						pL.pushNumber(defaults.getButtonWidth());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getButtonHeight() != SlickButtonData.DEFAULT_BUTTON_HEIGHT) {
						pL.pushString("height");
						pL.pushNumber(defaults.getButtonHeight());
						pL.setTable(NEGATIVE_THREE);
					}
					
					if (defaults.getLabelSize() != SlickButtonData.DEFAULT_LABEL_SIZE) {
						pL.pushString("labelSize");
						pL.pushNumber(defaults.getLabelSize());
						pL.setTable(NEGATIVE_THREE);
					}
					
					pL.setField(NEGATIVE_TWO, key);
				}
				
				pL.setGlobal("buttonset_defaults");
				
				pL.newTable();
				
				for (String name : s.getButtonSets().keySet()) {
					//String name = key;
					ColorSetSettings defaults = s.getSetSettings().get(name);
					Vector<SlickButtonData> data = s.getButtonSets().get(name);
					pL.newTable();
					int counter = 1;
					for (SlickButtonData button : data) {
						pL.newTable();
						if (defaults.getPrimaryColor() != button.getPrimaryColor()) {
							pL.pushString("primaryColor");
							pL.pushNumber(button.getPrimaryColor());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getSelectedColor() != button.getSelectedColor()) {
							pL.pushString("selectedColor");
							pL.pushNumber(button.getSelectedColor());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getFlipColor() != button.getFlipColor()) {
							pL.pushString("flipColor");
							pL.pushNumber(button.getFlipColor());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getLabelColor() != button.getLabelColor()) {
							pL.pushString("labelColor");
							pL.pushNumber(button.getLabelColor());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getFlipLabelColor() != button.getFlipLabelColor()) {
							pL.pushString("flipLabelColor");
							pL.pushNumber(button.getFlipLabelColor());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getButtonWidth() != button.getWidth()) {
							pL.pushString("width");
							pL.pushNumber(button.getWidth());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getButtonHeight() != button.getHeight()) {
							pL.pushString("height");
							pL.pushNumber(button.getHeight());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (defaults.getLabelSize() != button.getLabelSize()) {
							pL.pushString("labelSize");
							pL.pushNumber(button.getLabelSize());
							pL.setTable(NEGATIVE_THREE);
						}
						
						if (button.getTargetSet() != null && !button.getTargetSet().equals("")) {
							pL.pushString("switchTo");
							pL.pushString(button.getTargetSet());
							pL.setTable(NEGATIVE_THREE);
						}
						
						pL.pushString("x");
						pL.pushNumber(button.getX());
						pL.setTable(NEGATIVE_THREE);
						
						pL.pushString("y");
						pL.pushNumber(button.getY());
						pL.setTable(NEGATIVE_THREE);
						
						pL.pushString("label");
						pL.pushString(button.getLabel());
						pL.setTable(NEGATIVE_THREE);
						
						pL.pushString("command");
						pL.pushString(button.getText());
						pL.setTable(NEGATIVE_THREE);
						
						pL.pushString("flipLabel");
						pL.pushString(button.getFlipLabel());
						pL.setTable(NEGATIVE_THREE);
						
						pL.pushString("flipCommand");
						pL.pushString(button.getFlipCommand());
						pL.setTable(NEGATIVE_THREE);
						
						
						
						pL.rawSetI(NEGATIVE_TWO, counter);
						counter++;
					}
					
					pL.setField(NEGATIVE_TWO, name);
				}
				
				pL.setGlobal("buttonsets");
				
				pL.pushString(s.getLastSelected());
				pL.setGlobal("current_set");
				
				pL.getGlobal("legacyButtonsImported");
				if (pL.getLuaObject(-1).isFunction()) {
					pL.call(0, 0);
				}
				
				} else {
					//default settings are being loaded.
					//run the adjustment for the new buttons
					LuaState pL = buttonwindow.getLuaState();
					pL.getGlobal("debug");
					pL.getField(-1, "traceback");
					pL.getGlobal("alignDefaultButtons");
					if (pL.isFunction(-1)) {
						int ret = pL.pcall(0, 1, NEGATIVE_TWO);
						if (ret != 0) {
							this.dispatchLuaError(pL.getLuaObject(-1).getString());
						}
					} else {
						pL.pop(1);
					}
				}
				//s.getSetSettings();
				
				
				
				
				WindowToken tmp = root_settings.getSettings().getWindows().get("mainDisplay");
				tmp.importV1Settings(s);
				
				//handle button settings.
				SettingsGroup buttonops = buttonwindow.getSettings().getOptions();
				String hfedit = s.getHapticFeedbackMode();
				if (hfedit.equals("auto")) {
					buttonops.setOption("haptic_edit", Integer.toString(0));
				} else if (hfedit.equals("always")) {
					buttonops.setOption("haptic_edit", Integer.toString(1));
				} else if (hfedit.equals("none")) {
					buttonops.setOption("haptic_edit", Integer.toString(2));
				}
				
				String hfpress = s.getHapticFeedbackOnPress();
				if (hfpress.equals("auto")) {
					buttonops.setOption("haptic_press", Integer.toString(0));
				} else if (hfpress.equals("always")) {
					buttonops.setOption("haptic_press", Integer.toString(1));
				} else if (hfpress.equals("none")) {
					buttonops.setOption("haptic_press", Integer.toString(2));
				}
				
				String hfflip = s.getHapticFeedbackOnFlip();
				if (hfflip.equals("auto")) {
					buttonops.setOption("haptic_flip", Integer.toString(0));
				} else if (hfflip.equals("always")) {
					buttonops.setOption("haptic_flip", Integer.toString(1));
				} else if (hfflip.equals("none")) {
					buttonops.setOption("haptic_flip", Integer.toString(2));
				}
				String summary = Colorizer.getWhiteColor() + verb + " legacy settings file.\n";
				loadPlugins(tmpplugs, summary);
				root_settings.importV1Settings(s);
				if (!s.isRoundButtons()) {
					buttonops.setOption("button_roundness", Integer.toString(0));
				} 
			} else {
				int version = vpp.getVersionNumber();
				if (version == 2) {
					Log.e("XMLPARSE", "LOADING V2 SETTINGS FROM PATH: " + path);
					ArrayList<Plugin> tmpplugs = new ArrayList<Plugin>();
					ConnectionSetttingsParser csp = new ConnectionSetttingsParser(path, mService.getApplicationContext(), tmpplugs, mHandler, this);
					tmpplugs = csp.load(this);
					if (path == null) {
						Plugin buttonwindow = tmpplugs.get(1);
						//LuaState L = buttonwindow.getLuaState();
						LuaState pL = buttonwindow.getLuaState();
						pL.getGlobal("debug");
						pL.getField(-1, "traceback");
						pL.getGlobal("alignDefaultButtons");
						if (pL.isFunction(-1)) {
							int ret = pL.pcall(0, 1, NEGATIVE_TWO);
							if (ret != 0) {
								Connection.this.dispatchLuaError("ERROR IN DEFAULT BUTTONS:" + (pL.getLuaObject(-1).getString()));
							}
						} else {
							pL.pop(1);
						}
					}
					String summary = Colorizer.getWhiteColor() + verb + " settings file.\n";
					loadPlugins(tmpplugs, summary);
				} else {
					Log.e("XMLPARSE", "ERROR IN LOADING V2 SETTINGS, DID NOT FIND PROPER XMLVERSION NUMBER");
					try {
						mService.dispatchXMLError("Error " + verb.toLowerCase(Locale.US) + " settings, invalid or missing version attribute.\n");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return;
				}
				
			}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
			try {
				mService.dispatchXMLError(e.getLocalizedMessage());
				return;
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
		
		if (path == null) {
			mSettings.getSettings().getOptions().setOption("font_size", Integer.toString(calculate80CharFontSize()));
		
		}
		
		buildTriggerSystem();
		if (save) {
			this.saveMainSettings();
		}
	}
	
	/** Entry point to load the internal settings. */
	private void loadInternalSettings() {
		Pattern invalidchars = Pattern.compile("\\W");
		Matcher replacebadchars = invalidchars.matcher(this.mDisplay);
		String prefsname = replacebadchars.replaceAll("");
		prefsname = prefsname.replaceAll("/", "");
		String rootPath = prefsname + ".xml";
		String internal = mService.getApplicationContext().getApplicationInfo().dataDir + "/files/";
		File oldp = new File(internal + rootPath);
		if (!oldp.exists()) {
			importSettings(null, false, true);
		} else {
			importSettings(rootPath, false, true);
		}
	}
	
	/** Build settings page routine. This is used by the settings loading routine. 
	 * The reason why the magic number 4 is used is that is the position of the window settings in the
	 * settings list is similar to the position of the menu item in the v1 client.
	 */
	@SuppressWarnings("deprecation")
	private void buildSettingsPage() {
		if (mSettings.getSettings().getWindows().size() < 1) {
			WindowToken token = new WindowToken(MAIN_WINDOW, null, null, mDisplay);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			LayoutGroup g = new LayoutGroup();
			g.setType(LayoutGroup.LAYOUT_TYPE.normal);
			g.setLandscapeParams(p);
			g.setPortraitParams(p);
			mWindows.add(0, token);
		} else {
			mWindows.add(0, mSettings.getSettings().getWindows().get(MAIN_WINDOW));
		}
		
		mSettings.doBackgroundStartup();
		for (Plugin pl : mPlugins) {
			pl.doBackgroundStartup();
		}
		
		mSettings.buildAliases();
		for (Plugin pl : mPlugins) {
			pl.buildAliases();
		}
		
		buildTriggerSystem();
		mWindows.get(0).getSettings().setListener(new WindowSettingsChangedListener(mWindows.get(0).getName()));
		mSettings.getSettings().getOptions().addOptionAt(mWindows.get(0).getSettings(), FOUR);
	}
	
	/** Attatches a WindowSettingsChangedListener to the given WindowToken.
	 * 
	 * @param w The window to attatch a new settings changed listener to.
	 */
	public final void attatchWindowSettingsChangedListener(final WindowToken w) {
		w.getSettings().setListener(new WindowSettingsChangedListener(w.getName()));
	}

	/** Target for the foreground window to check if the keep last setting is set.
	 * 
	 * @return value of the keep list settings.
	 */
	public final boolean isKeepLast() {
		return (Boolean) ((BooleanOption) mSettings.getSettings().getOptions().findOptionByKey("keep_last")).getValue();
	}

	/** Target for the foreground window to check if the full screen settings is set.
	 * 
	 * @return the value of the full screen option.
	 */
	public final boolean isFullScren() {
		return (Boolean) ((BooleanOption) mSettings.getSettings().getOptions().findOptionByKey("fullscreen")).getValue();
	}

	/** Getter for mHost.
	 * 
	 * @return mHost;
	 */
	public final String getHostName() {
		return mHost;
	}
	
	/** Getter for this connection's port value.
	 * 
	 * @return the port number this connection is using.
	 */
	public final int getPort() {
		return mPort;
	}
	
	/** Utility method that generates the font size necessary to fit 80 chars to the window width.
	 * 
	 * @return the font size that will produce nearest to 80 chars as possible.
	 */
	private int calculate80CharFontSize() {
		int windowWidth = mService.getResources().getDisplayMetrics().widthPixels;
		if (mService.getResources().getDisplayMetrics().heightPixels > windowWidth) {
			windowWidth = mService.getResources().getDisplayMetrics().heightPixels;
		}
		float fontSize = MIN_FONT_SIZE;
		float delta = 1.0f;
		Paint p = new Paint();
		p.setTextSize(MIN_FONT_SIZE);
		//p.setTypeface(Typeface.createFromFile(service.getFontName()));
		p.setTypeface(Typeface.MONOSPACE);
		boolean done = false;
		
		float charWidth = p.measureText("A");
		float charsPerLine = windowWidth / charWidth;
		
		if (charsPerLine < TARGET_FIT_WIDTH) {
			//for QVGA screens, this test will always fail on the first step.
			done = true;
		} else {
			fontSize += delta;
			p.setTextSize(fontSize);
		}
		
		while (!done) {
			charWidth = p.measureText("A");
			charsPerLine = windowWidth / charWidth;
			if (charsPerLine < TARGET_FIT_WIDTH) {
				done = true;
				fontSize -= delta; //return to the previous font size that produced > 80 characters.
			} else {
				fontSize += delta;
				p.setTextSize(fontSize);
			}
		}
		return (int) fontSize;
	}
	
	/** Starts the recursive settings initialization routine to set all the settings loaded from the serialized settings file. */
	private void initSettings() {
		initSetting(mSettings.getSettings().getOptions());
	}
	
	/** Recursive settings initializations routine. 
	 * 
	 * @param s the SettingsGroup to dump.
	 */
	private void initSetting(final SettingsGroup s) {
		for (Option o : s.getOptions()) {
			if (o instanceof SettingsGroup) {
				initSetting((SettingsGroup) o);
			} else {
				BaseOption tmp = (BaseOption) o;
				this.updateSetting(o.getKey(), tmp.getValue().toString());
			}
		}
	}

	/** Entry point for the foreground window to reset the settings for this connection. */
	public final void resetSettings() {
		this.mHandler.sendEmptyMessage(MESSAGE_DORESETSETTINGS);
	}
	
	/** Work horse routine that actually resets the settings. */
	public final void doResetSettings() {
		for (IWindowCallback c : mWindowCallbackMap.values()) {
			try {
				c.shutdown();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mService.markWindowsDirty();
		importSettings(null, true, true);
	}

	/** Entry point for the foreground window to import a custom settings file at the given location.
	 * 
	 * @param path Path of the settings to load.
	 */
	public final void startLoadSettingsSequence(final String path) {
		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_IMPORTFILE, path));
	}
	
	/** Work horse for the foreground window to add an external plugin and reload the settings.
	 * 
	 * @param path The location of the external settings file.
	 */
	public final void doAddLink(final String path) {
		mSettings.getLinks().add(path);
		saveMainSettings();
		reloadSettings();
	}
	
	/** Entry point for the foreground window to add an external plugin and reload the settings.
	 * 
	 * @param path The location of the external settings file.
	 */
	public final void addLink(final String path) {
		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADDLINK, path));
	}

	/** Work horse routine for removing a plugin. 
	 * 
	 * @param plugin The name of the plugin to remove.
	 */
	private void doDeletePlugin(final String plugin) {
		Plugin p = mPluginMap.remove(plugin);
		
		String remove = null;
		if (p.getStorageType().equals("EXTERNAL")) {
			for (String path : mSettings.getLinks()) {
				if (p.getFullPath().contains(path)) {
					remove = path;
				}
			}
		}
		if (remove != null) { 
			mSettings.getLinks().remove(remove);
			mLinkMap.remove(remove);
		}
		
		mPlugins.remove(p);
		saveMainSettings();
		reloadSettings();
	}
	
	/** Entry poit routine for removing a plugin.
	 * 
	 * @param plugin The name of the plugin to remove.
	 */
	public final void deletePlugin(final String plugin) {
		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_DELETEPLUGIN, plugin));
	}

	/** Sets a plugin enabled.
	 * 
	 * @param plugin Name of the plugin to affect.
	 * @param enabled Desired state of the plugin.
	 */
	public final void setPluginEnabled(final String plugin, final boolean enabled) {
		Plugin p = mPluginMap.get(plugin);
		p.setEnabled(enabled);
		saveMainSettings();
		reloadSettings();
	}

	/** Gets the direction data from the main settings plugin.
	 * 
	 * @return The direction map.
	 */
	public final HashMap<String, DirectionData> getDirectionData() {
		return mSettings.getDirections();
	}

	/** Sets the directio data for the main settings plugin. This is supplied from the foreground window.
	 * 
	 * @param data Diretion data wad from to use for the main settings wad.
	 */
	public final void setDirectionData(final HashMap<String, DirectionData> data) {
		mSettings.setDirections(data);
		mSpeedwalkCommand.setDirections(data);
	}

	/** Getter for the title bar height. 
	 * 
	 * @return the title bar height.
	 */
	public final int getTitleBarHeight() {
		return mTitleBarHeight;
	}

	/** Getter for the status bar height. 
	 * 
	 * @return the status bar height.
	 */
	public final int getStatusBarHeight() {
		return mStatusBarHeight;
	}

	/** Utility method to test to see if a link has been loaded.
	 * 
	 * @param link The path of the link to test. Relative to the BlowTorch sd card root.
	 * @return The state of the target plugin. true = loaded, false = unloaded.
	 */
	public final boolean isLinkLoaded(final String link) {
		String foo = Environment.getExternalStorageDirectory() + "/BlowTorch/";
		String bar = link.replace(foo, "");
		
		boolean ret = mLinkMap.containsKey(bar);
		return ret;
	}

	/** Kicks off the loadInternalSettings() routine. */
	public final void initWindows() {
		loadInternalSettings();
	}

	/** Immediatly shuts down this connection and all associated data structures. */
	public final void shutdown() {
		this.saveMainSettings();
		this.killNetThreads(true);
		for (Plugin p : mPlugins) {
			p.shutdown();
			p = null;
		}
		mSettings.shutdown();
		mSettings = null;
		mHandler.removeMessages(MESSAGE_RECONNECT);
		mHandler = null;
		mService.removeConnectionNotification(mDisplay);
	}

	/** Gets the path for a plugin.
	 * 
	 * @param plugin Name of the plugin to interrogate.
	 * @return The full path of the plugin.
	 */
	public final String getPluginPath(final String plugin) {
		Plugin p = mPluginMap.get(plugin);
		return p.getFullPath();
	}

	/** Entry point for Plugins to send data to the foreground window without trigger parsing.
	 * 
	 * @param str The string to send.
	 */
	public final void dispatchLuaText(final String str) {
		mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_LUANOTE, str));
	}

	/** Calls an anonymous global function in the target plugin. Does not provide an arugment.
	 * 
	 * @param plugin Name of the target plugin.
	 * @param function Name of the function to call.
	 */
	public final void callPluginFunction(final String plugin, final String function) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			p.callFunction(function);
		} else {
			this.dispatchLuaText("\n" + Colorizer.getRedColor() + "No plugin named: " + plugin + Colorizer.getRedColor() + "\n");
		}
	}

	@Override
	public final SettingsChangedListener getSettingsListener() {
		return (SettingsChangedListener) this;
	}

	@Override
	public final void callPlugin(final String plugin, final String function, final String data) {
		Message m = mHandler.obtainMessage(MESSAGE_CALLPLUGIN);
		m.getData().putString("PLUGIN", plugin);
		m.getData().putString("FUNCTION", function);
		m.getData().putString("DATA", data);
		mHandler.sendMessage(m);
	}

	@Override
	public final boolean pluginSupports(final String plugin, final String function) {
		Plugin p = mPluginMap.get(plugin);
		if (p != null) {
			return p.checkPluginSupports(function);
		}
		return false;
	}

	/** Test to see of a plugin is installed. 
	 * 
	 * @param desired Name of the desired plugin.
	 * @return is it loaded or not.
	 */
	public final boolean isPluginInstalled(final String desired) {
		return mPluginMap.containsKey(desired);
	}
	
	/** Utility class providing the .timer command. */
	private class TimerCommand extends SpecialCommand {
		/** Acceptable timer action strings. */
		private ArrayList<String> mTimerActions = new ArrayList<String>();
		/** Ordinal capture group. */
		private final int mOrdinalGroupIndex = 3;
		/** Silent marker. */
		private final int mSilent = 50;
		/** Generic constructor. */
		public TimerCommand() {
			this.commandName = "timer";
			mTimerActions.add("play");
			mTimerActions.add("pause");
			mTimerActions.add("info");
			mTimerActions.add("reset");
			mTimerActions.add("stop");
		}
		/** Execute method for this command.
		 * 
		 * @param o parameter object.
		 * @param c connection that called this function
		 * @return whatever this function returns.
		 */
		public Object execute(final Object o, final Connection c)  {
			//example argument " info 0"
			//regex = "^\s+(\S+)\s+(\d+)";
			Pattern p = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s*(\\S*)");
			
			Matcher m = p.matcher((String) o);
			
			if (m.matches()) {
				//extract arguments
				String action = m.group(1).toLowerCase(Locale.US);
				String ordinal = m.group(2);
				String silent = "";
				if (m.groupCount() > 2) {
					silent = m.group(mOrdinalGroupIndex);
				}
				if (!mTimerActions.contains(action)) {
					//error with bad action.
					dispatchNoProcess(getErrorMessage("Timer action arguemnt " + action + " is invalid.", "Acceptable arguments are \"play\",\"pause\",\"reset\",\"stop\" and \"info\".").getBytes());
					return null;
				}
				int domsg = mSilent;
				if (!silent.equals("")) {
					domsg = 0;
				}
				
				if (action.equals("info")) {
					mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_TIMERINFO, ordinal));
					return null;
				}
				if (action.equals("reset")) {
					mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_TIMERRESET, 0, domsg, ordinal));
					return null;
				}
				if (action.equals("play")) {
					//play
					mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_TIMERSTART, 0, domsg, ordinal));
					return null;
				}
				if (action.equals("pause")) {
					mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_TIMERPAUSE, 0, domsg, ordinal));
					return null;
				}
				if (action.equals("stop")) {
					mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_TIMERSTOP, 0, domsg, ordinal));
					return null;
				}
			} else {
				dispatchNoProcess(getErrorMessage("Timer command: \".timer " + (String) o + "\" is invalid.", "Timer function format \".timer action index [silent]\"\n"
							+ "Where action is \"play\",\"pause\",\"reset\" or \"info\".\nIndex is the timer index displayed in the timer selection list.").getBytes());
			}
			
			return null;
			
		}
	}
	
	/** Work horse method for the timer command.
	 * 
	 * @param obj The name of the timer.
	 * @param arg2 The silent flag (0 = silent, anything else = not silent).
	 * @param action The action that was harvested from the entry point.
	 */
	private void doTimerAction(final String obj, final int arg2, final TIMER_ACTION action) {
		//check for valid ordinals.
		boolean found = false;
		Plugin host = null;
		if (mSettings.getSettings().getTimers().containsKey(obj)) {
			host = mSettings;
			found = true;
		} else {
			//check plugins
			for (Plugin p : mPlugins) {
				if (p.getSettings().getTimers().containsKey(obj)) {
					host = p;
					found = true;
				}
			}
		}
		boolean silent = false;
		if (arg2 == 0) {
			silent = true;
		}
		
		if (!found) {
			//show error message.
			dispatchNoProcess(SpecialCommand.getErrorMessage("Timer command error", "No timer with name " + obj + " found.").getBytes());
		} else {
			switch (action) {
			case PLAY:
				host.startTimer(obj);
				if (!silent) {
					toast("Timer " + obj + " started.");
				}
				break;
			case PAUSE:
				host.pauseTimer(obj);
				if (!silent) {
					toast("Timer " + obj + " paused.");
				}
				break;
			case RESET:
				host.resetTimer(obj);
				if (!silent) {
					toast("Timer " + obj + " reset.");
				}
				break;
			case STOP:
				host.pauseTimer(obj);
				host.resetTimer(obj);
				if (!silent) {
					toast("Timer " + obj + " stopped.");
				}
				break;
			case INFO:
				TimerData t = host.getSettings().getTimers().get(obj);
				if (t.isPlaying()) {
					long now = SystemClock.elapsedRealtime();
					long dur = now - t.getStartTime();
					int sec = t.getSeconds() - (int) (dur / ONE_THOUSAND_MILLIS);
					toast(obj + ": " + sec + "s");
				} else {
					if (t.getRemainingTime() != t.getSeconds()) {
						int sec = t.getSeconds() - t.getRemainingTime();
						toast("Timer " + obj + " is paused, " + sec + " remain.");
					} else {
						toast("Timer " + obj + " is not running.");
					}
				}
				break;
			case NONE:
				break;
			default:
				break;
			}
		}
	}
	
	/** Utility method for putting up a generic toast message.
	 * 
	 * @param str The string to use for the toast message.
	 */
	private void toast(final String str) {
		Context c = this.getContext();
		Toast t = Toast.makeText(c, str, Toast.LENGTH_SHORT);
		float density = c.getResources().getDisplayMetrics().density;
		t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, (int) (TOAST_MESSAGE_TOP_OFFSET * density));
		t.show();
	}
	
	/** Getter for mHandler.
	 * 
	 * @return The handler associated with this connection.
	 */
	public final Handler getHandler() {
		return mHandler;
	}
	
	/** Getter for the plugin list.
	 * 
	 * @return The plugin list in loaded order.
	 */
	public final ArrayList<Plugin> getPlugins() {
		return mPlugins;
	}
	
	/** Getter for mPump.
	 * 
	 * @return the data pump for this connection.
	 */
	public final DataPumper getPump() {
		return mPump;
	}
	
	/** Getter for mProcessor.
	 * 
	 * @return the processor associated with this connection.
	 */
	public final Processor getProcessor() {
		return mProcessor;
	}
	
	/** Getter for the display name.
	 * 
	 * @return the launcher display name for this connection.
	 */
	public final String getDisplay() {
		return mDisplay;
	}
	
	/** Getter for the host name.
	 * 
	 * @return the host name this connection uses.
	 */
	public final String getHost() {
		return mHost;
	}
	
	/** getter for mIsConnected.
	 * 
	 * @return the connected state of this connection.
	 */
	public final boolean isConnected() {
		return mIsConnected;
	}
	
	/** Getter for mService. This is really ugly and should be fixed immediatly.
	 * 
	 * @return the service that initated this connection.
	 */
	public final StellarService getService() {
		return mService;
	}
}
