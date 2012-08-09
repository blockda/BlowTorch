package com.offsetnull.bt.service;




import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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

//import org.keplerproject.luajava.LuaState;
//import org.keplerproject.luajava.LuaStateFactory;
import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;
import org.xml.sax.SAXException;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
//import android.util.Log;

//import com.happygoatstudios.bt.service.IStellarServiceCallback;
//import com.happygoatstudios.bt.service.IStellarService;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponder.RESPONDER_TYPE;
import com.offsetnull.bt.responder.replace.ReplaceResponder;
import com.offsetnull.bt.responder.script.ScriptResponder;
import com.offsetnull.bt.responder.toast.ToastResponder;
import com.offsetnull.bt.service.IConnectionBinder;
import com.offsetnull.bt.service.IConnectionBinderCallback;
import com.offsetnull.bt.service.IWindowCallback;
import com.offsetnull.bt.service.function.SpecialCommand;
import com.offsetnull.bt.service.plugin.ConnectionSettingsPlugin;
import com.offsetnull.bt.service.plugin.Plugin;
import com.offsetnull.bt.speedwalk.DirectionData;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.timer.TimerExtraTask;
import com.offsetnull.bt.timer.TimerProgress;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.service.plugin.settings.EncodingOption;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.settings.ColorSetSettings;
import com.offsetnull.bt.settings.ConfigurationLoader;
import com.offsetnull.bt.settings.HyperSAXParser;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.button.SlickButtonData;

import dalvik.system.PathClassLoader;


public class StellarService extends Service {

	protected static final int MESSAGE_STARTUP = 0;
	protected static final int MESSAGE_NEWCONENCTION = 1;
	protected static final int MESSAGE_SWITCH = 2;
	protected static final int MESSAGE_RELOADSETTINGS = 3;
	protected static final int MESSAGE_STOPANR = 4;

	private boolean windowShowing = true;
	//public static final String ALIAS_PREFS = "ALIAS_SETTINGS";
	//TreeMap<String, String> aliases = new TreeMap<String, String>();
	//RemoteCallbackList<IStellarServiceCallback_BAK> callbacks = new RemoteCallbackList<IStellarServiceCallback_BAK>();
	//HyperSettings the_settings = new HyperSettings();
	NotificationManager mNM;
	//OutputStream output_writer = null;
	//Processor the_processor = null;
	//Object sendlock = new Object();
	//protected int bindCount = 0;
	//InetAddress the_addr = null;
	//String host;
	//int port;
	//String display;
	//final int BAD_PORT = 999999;
	//final String BAD_HOST = "NOTSETYET";
	//Socket the_socket = null;
	//DataPumper pump = null;
	//Handler myhandler = null;
	//public int trigger_count = 5555;
	/*final static public int MESSAGE_PROCESS = 102;
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
	public static final int MESSAGE_VITALS = 6001;
	public static final int MESSAGE_MAXVITALS = 6002;
	public static final int MESSAGE_ENEMYHP = 60003;
	public static final int MESSAGE_FOO = 600004;
	public static final int MESSAGE_UPDATEROOMINFO = 6000343;
	public static final int MESSAGE_DODIALOG = 512;*/
	//public boolean sending = false;
	//String settingslocation = "test_settings2.xml";
	//com.happygoatstudios.bt.window.TextTree buffer_tree = new com.happygoatstudios.bt.window.TextTree();

	//Timer the_timer = new Timer("BLOWTORCH_TIMER",true);
	//HashMap<String,TimerExtraTask> timerTasks = new HashMap<String,TimerExtraTask>();
	
	static {
		System.loadLibrary("sqlite3");
		System.loadLibrary("lua");
		//System.loadLibrary("lsqlite3");
		//System.loadLibrary("luabins");
	}
	
	public void onLowMemory() {
		//Log.e("SERVICE","The service has been requested to shore up memory usage, potentially going to be killed.");
	}
	
	@Override
	public int onStartCommand(Intent intent,int flags,int startId) {
		if(intent == null) {
			//Log.e("SERVICE","onStartCommand passed null intent");
			return Service.START_STICKY;
		}
		//Log.e("SERVICE","onStartCommand");
		if(ConfigurationLoader.isTestMode(this.getApplicationContext())) {
			//Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this.getApplicationContext()));
		}
		
		//lol, here we go.
		
		//L.
		
		return Service.START_STICKY;
	}
	
	/*private class LogFunction extends JavaFunction {
		Handler the_handler = null;
		public LogFunction(Handler h,LuaState L) {
			super(L);
			the_handler = h;
			// TODO Auto-generated constructor stub
		}
		@Override
		public int execute() throws LuaException {
			
			the_handler.sendMessage(the_handler.obtainMessage(101010101,this.getParam(2).getString()));
			
			return 0;
		}
		
	}
	
	private class FieldFunction extends JavaFunction {

		public FieldFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			//LuaObject o = this.getParam(2);
			//o.
			//Log.e("LUA","FIELD FUNCTION CALLED WITH " + n + " paramters");
			L.pushNil();
			
			//boolean clean = false;
			ContentValues cv = new ContentValues();
			while(L.next(2) != 0) {
				//clean = true;
				String id = L.toString(-2);
				String name = L.toString(-1);
				//Log.e("LUA",id + " <==> " +name);
				
				cv.put(id, name);
				L.pop(1);
			}
			L.pop(1);
			L.pushJavaObject(cv);
			
			return 1;
			
		}
		
	}
	
	private class RowFunction extends JavaFunction {
		Handler the_handler = null;
		public RowFunction(Handler h,LuaState L) {
			super(L);
			the_handler = h;
			// TODO Auto-generated constructor stub
		}
		@Override
		public int execute() throws LuaException {
			
			
			int n = L.getTop();
			String[] str = new String[n-1];
			for(int i=2;i<=n;i++) {
				str[i-2]=L.toString(i);
			}
			
			
			//L.pop(n-1);
			
			L.pushJavaObject(str);
			
			return 1;
			//ArrayList<String>
			//L.pushNil();
			
			//while(L.next(2) != 0) {
			//	String id = L.toString(-2);
			//	String name = L.toString(-1);
				
			//}
			//String param1 = this.getParam(2).getString();
			//String param2 = this.getParam(3).getString();
			
			//L.pushObjectValue(new String[] {param1,param2});
			//return 1;
		}
		
	}
	
	private class GMCPFunction extends JavaFunction {
		Processor proc = null;
		public GMCPFunction(Processor proc,LuaState L) {
			super(L);
			this.proc = proc;
		}

		@Override
		public int execute() throws LuaException {
			//L.pushObjectValue(proc.)
			//L.pushObjectValue(proc.getGMCPValue(this.getParam(2).getString()));
			//ok, so we need to check and see if what we want is a table.
			String args = this.getParam(2).getString();
			
			//String parts[] = args.split(".");
			
			
			
			HashMap<String,Object> tmp = proc.getGMCPTable(args);
			if(tmp == null) {
				//somehow return
			} else {
				//begin iterative lua dump.
				dumpNode(tmp,"");
			}
			//should be one table on top of the stack.
			return 1;
			//return 0;
		}
		
		private void dumpNode(HashMap<String,Object> node,String key) {
			if(!key.equals("")) {
				this.L.pushString(key);
			}
			this.L.newTable();
			
			for(String tmp : node.keySet()) {
				
				
				Object o = node.get(tmp);
				if(o instanceof HashMap) {
					//we recurse
					//Log.e("GMCPDUMP","DUMPING SUB TABLE");
					dumpNode((HashMap<String,Object>)o,tmp);
				} else {
					this.L.pushString(tmp);
					if(o instanceof String) {
						this.L.pushString((String)o);
					}
					if(o instanceof Integer) {	
						//TODO: apparantly there is no _pushInteger implementation. wtfxors.
						this.L.pushString(((Integer)o).toString());
					}
					this.L.setTable(-3);
				}
			}
			if(!key.equals("")) {
				this.L.setTable(-3);
			}
			//this.L.setTable(-3);
			
			
		}
		
	}*/
	
	SQLiteDatabase database = null;
	SQLiteHelper helper = null;
	private class TriggerFunction extends JavaFunction {
		HyperSettings settings = null;
		public TriggerFunction(HyperSettings the_settings, LuaState L) {
			super(L);
			settings = the_settings;
		}

		@Override
		public int execute() throws LuaException {
			//attempt to access trigger data.
			
			//synchronized(the_settings) {
				String key = this.getParam(2).getString();
				//Log.e("LUA","ATTEMPTING TO RETURN TRIGGER: " + key);
				TriggerData dat = settings.getTriggers().get(key);
				if(dat == null) {
					L.pushNil();
				} else {
					L.pushObjectValue(dat);
				}
			//}
			
			return 1;
		}
		
	}
	
	private class ServerSendFunction extends JavaFunction {
		//HyperSettings settings = null;
		public ServerSendFunction(LuaState L) {
			super(L);
			//settings = the_settings;
		}

		@Override
		public int execute() throws LuaException {
			//attempt to access trigger data.
			
			//synchronized(the_settings) {
				String key = this.getParam(2).getString();
				
				//try {
					//StellarService.this.myhandler.sendMessage(StellarService.this.myhandler.obtainMessage(MESSAGE_SENDDATA, key.getBytes(the_settings.getEncoding())));
				//} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
				//Log.e("LUA","ATTEMPTING TO RETURN TRIGGER: " + key);
				/*TriggerData dat = settings.getTriggers().get(key);
				if(dat == null) {
					L.pushNil();
				} else {
					L.pushObjectValue(dat);
				}*/
			//}
			
			return 1;
		}
		
	}
	
	private class NewTriggerFunction extends JavaFunction {

		public NewTriggerFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			//expected arguments.
			//label
			//pattern
			//literal
			//fireonce
			//responders
			
			//TODO:CHECK INPUT, RETURN BAD ERROR
			
			String label = this.getParam(2).getString();
			String pattern = this.getParam(3).getString();
			Boolean literal = this.getParam(4).getBoolean();
			String function = this.getParam(5).getString();
			//Boolean fireonce = this.getParam(5).getBoolean();
			//ArrayList<TriggerResponder> responders = (ArrayList<TriggerResponder>)this.getParam(7).getObject();
			
			//StellarService.this.makeTmpScriptTrigger(label,pattern,literal,function);
			return 0;
		}
		
	}
	
	private class DeleteTriggerFunction extends JavaFunction {

		public DeleteTriggerFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String name = this.getParam(2).getString();
			//deleteTrigger(name);
			return 0;
		}

	}
	
	LuaState L = null;
	String theLuaString = null;
	
	Plugin plugin = null;
	Handler handler = null;
	public void onCreate() {
		
		//Debug.waitForDebugger();
		//Debug.startMethodTracing("service");
		connections = new HashMap<String,Connection>();
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		//mNM.cancel(5546);
		//host = BAD_HOST;
		//port = BAD_PORT;

		
		
		SharedPreferences prefs = this.getSharedPreferences("SERVICE_INFO", 0);
		
		int libsver = prefs.getInt("CURRENT_LUA_LIBS_VERSION", 0);
		ComponentName myservice = new ComponentName(this,this.getClass());
		Bundle meta = null;
		try {
			meta = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int packagever = meta.getInt("BLOWTORCH_LUA_LIBS_VERSION");
		if(packagever != libsver) {
			//copy new libs.
			try {
				updateLibs();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//SharedPreferences.Editor editor = prefs.edit();
			//editor.putInt("CURRENT_LUA_LIBS_VERSION", packagever);
			//editor.commit();
 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//settingslocation = prefs.getString("SETTINGS_PATH", "");
		//if(settingslocation.equals("")) {
			//Log.e("SERVICE","LAUNCHER FAILED TO PROVIDE SETTINGS PATH");
		//	return;
		//}
		
		Pattern invalidchars = Pattern.compile("\\W"); 
		//Matcher replacebadchars = invalidchars.matcher(settingslocation);
		//String prefsname = replacebadchars.replaceAll("");
		//prefsname = prefsname.replaceAll("/", "");
		//settingslocation = prefsname + ".xml";
		//loadXmlSettings(prefsname +".xml");
		//Looper.prepare();
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_RELOADSETTINGS:
					connections.get(connectionClutch).reloadSettings();
					reloadWindows();
					break;
				case MESSAGE_STARTUP:
					if(connections.get(connectionClutch).pump == null) {
						connections.get(connectionClutch).handler.sendEmptyMessage(Connection.MESSAGE_STARTUP);
					}
					/*callbacks.beginBroadcast();
					try {
						callbacks.getBroadcastItem(0).loadWindowSettings();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					callbacks.finishBroadcast();*/
					//Debug.stopMethodTracing();
					break;
				case MESSAGE_NEWCONENCTION:
					Bundle b = msg.getData();
					String display = b.getString("DISPLAY");
					String host = b.getString("HOST");
					int port = b.getInt("PORT");
					
					Connection c = connections.get(display);
					if(c != null) {
						//c.host = host;
						//c.port = port;
						//c.display = display;
						for(Connection tmp : connections.values()) {
							tmp.deactivate();
						}
						c.activate();
						//the_settings = c.the_settings;
					} else {
						//make new conneciton.
						connectionClutch = display;
						c = new Connection(display,host,port,StellarService.this);
						connections.put(connectionClutch, c);
						//the_settings = c.the_settings;
						c.initWindows();
						
						for(Connection tmp : connections.values()) {
							//Connection off = connections.ge
							tmp.deactivate();
						}
						c.activate();
						
					}
					break;
				case MESSAGE_SWITCH:
					switchTo((String)msg.obj);
					break;
				//case MESSAGE_STOPANR:
					//this.sendEmptyMessageDelayed(MESSAGE_STOPANR, 2000);
					//break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
		//Looper.loop();
		//buffer_tree.setLineBreakAt(80); //this doesn't really matter
		//buffer_tree.setEncoding(the_settings.getEncoding());
		//buffer_tree.setMaxLines(the_settings.getMaxLines());
		
		//handler.sendEmptyMessageDelayed(MESSAGE_STOPANR, 2000);
		
	}	
	
	
	/*private void initPlugins() {
		//Debug.waitForDebugger();
		PluginParser pparser = new PluginParser("/mnt/sdcard/BlowTorch/plugin.xml",this.getApplicationContext());
		try {
			plugin = new Plugin(pparser.load());
		} catch (FileNotFoundException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		} catch (IOException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		} catch (SAXException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		} catch (LuaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}*/
	
	/*private void pluginTest() {
		//TODO: plugin test

		
		PluginParser pparser = new PluginParser("/mnt/sdcard/BlowTorch/plugin.xml",this.getApplicationContext());
		try {
			plugin = new Plugin(pparser.load());
		} catch (FileNotFoundException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		} catch (IOException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		} catch (SAXException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}
		
		Log.e("PLUG","ALIAS COUNT:" + plugin.getSettings().getAliases().size());
		for(String d : plugin.getSettings().getAliases().keySet()) {
			AliasData a = plugin.getSettings().getAliases().get(d);
			Log.e("PLUG","ALIAS: pre=" + a.getPre() + " post=" + a.getPost());
		}
		Log.e("PLUG","TRIGGER COUNT:" + plugin.getSettings().getTriggers().size());
		for(String d : plugin.getSettings().getTriggers().keySet()) {
			TriggerData t = plugin.getSettings().getTriggers().get(d);
			Log.e("PLUG","TRIGGER: name="+t.getName() + " p="+t.getPattern() + " has " + t.getResponders().size() + " responders.");
			for(TriggerResponder r : t.getResponders()) {
				if(r.getType() == RESPONDER_TYPE.REPLACE) {
					Log.e("PLUG","REPLACE RESPONDER FOUND:" + ((ReplaceResponder)r).getWith() + " fireType="+r.getFireType().getString());
				}
			}
		}
		Log.e("PLUG","TIMER COUNT:" + plugin.getSettings().getTimers().size());
		
		for(String d : plugin.getSettings().getTimers().keySet()) {
			TimerData t = plugin.getSettings().getTimers().get(d);
			Log.e("PLUG","TIMER: name="+t.getName()+" dur="+t.getSeconds()+ " has " + t.getResponders().size() + " responders.");
		}
		//plugin 
		//Debug.waitForDebugger();
		
		
		
		//attempt to query the match against the new plugin.
				TextTree testTree = new TextTree();
				try {
					testTree.addBytesImpl("What be thy name, adventurer?".getBytes("ISO-8859-1"));
					plugin.process(testTree, this, true, myhandler, display);
					testTree.updateMetrics();
					String xformed = new String(testTree.dumpToBytes(true),"ISO-8859-1");
					Log.e("PLUG","INPUT AFTER PLUGIN RUN: \n" + xformed);
				} catch (UnsupportedEncodingException e5) {
					// TODO Auto-generated catch block
					e5.printStackTrace();
				}
	}*/
	
	/*public void makeTmpScriptTrigger(String label,String pattern,Boolean literal,String function) {
		ScriptResponder r = new ScriptResponder();
		r.setFunction(function);
		ArrayList<TriggerResponder> list = new ArrayList<TriggerResponder>();
		list.add(r);
		makeNewTrigger(label,pattern,literal,false,list,true,false);
	}*/
	
	/*public void makeNewTrigger(String label, String pattern, Boolean literal,
			Boolean fireonce, ArrayList<TriggerResponder> responders,Boolean hidden,boolean save) {
		synchronized(the_settings) {
			TriggerData tmp = new TriggerData();
			tmp.setPattern(pattern);
			tmp.setName(label);
			tmp.setEnabled(true);
			tmp.setFireOnce(fireonce);
			tmp.setInterpretAsRegex(!literal);
			tmp.setResponders(responders);
			tmp.setHidden(hidden);
			tmp.setSave(save);
			
			the_settings.getTriggers().put(pattern, tmp);
			
		}
	}
	
	public void deleteTrigger(String label) {
		synchronized(the_settings) {
			the_settings.getTriggers().remove(label);
		}
	}*/

	protected class SQLiteHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "btdb";
		private static final int VERSION = 2;
		
		private static final String ROOM_TABLE = "" +
							"CREATE TABLE rooms (_id integer PRIMARY KEY," +
							"name TEXT NOT NULL,"+
							"zone TEXT NOT NULL,"+
							"terrain TEXT NOT NULL,"+
							"details TEXT,"+
							"cont_id integer NOT NULL,"+
							"x integer NOT NULL," +
							"y integer NOT NULL," +
							"cont_room integer NOT NULL);";
		
		
		private static final String EXIT_TABLE = ""+
						"CREATE TABLE exits(_id integer PRIMARY KEY autoincrement,"+
						"room integer NOT NULL," +
						"command TEXT NOT NULL," + 
						"destination integer NOT NULL);";
		
		public SQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) {
			//arg0.execSQL(ROOM_TABLE);
			//arg0.execSQL(EXIT_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			arg0.execSQL("DROP TABLE IF EXISTS rooms");
			arg0.execSQL("DROP TABLE IF EXISTS exits");
		}
	}
	
	protected void dispatchHPUpdateV2(int hp,int mp,int maxhp,int maxmana, int enemy) {
		/*int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).updateVitals2(hp,mp,maxhp,maxmana,enemy);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}*/
		//callbacks.finishBroadcast();
	}

	protected void setEnemyHealth(int arg1) {
		/*int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).updateEnemy(arg1);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();*/
	}
	
	protected void initLua() {
		//TODO: Lua bootstrap
				
	/*			
				LogFunction logger = new LogFunction(myhandler,L);
				//TriggerFunction trig = new TriggerFunction(the_settings,L);
				RowFunction row = new RowFunction(myhandler,L);
				//GMCPFunction gmcp = new GMCPFunction(the_processor,L);
				FieldFunction field = new FieldFunction(L);
				DeleteTriggerFunction dtrig = new DeleteTriggerFunction(L);
				NewTriggerFunction ntrig = new NewTriggerFunction(L);
				ServerSendFunction send = new ServerSendFunction(L);
				try {
					logger.register("Note");
					//trig.register("trigger");
					row.register("row");
					field.register("fields");
					//gmcp.register("gmcpTable");
					ntrig.register("NewTrigger");
					dtrig.register("DeleteTrigger");
					send.register("send");
				} catch (LuaException e) {
					e.printStackTrace();
				}
				
				//File file = new File(this.getResources().openRawResource(R.));
				try {
					InputStream stream = this.getAssets().open("utils.lua");
					byte buf[] = new byte[stream.available()];
					stream.read(buf);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bos.write(buf);
					stream.close();
					String luaString = bos.toString("ISO-8859-1");
					int result = L.LdoString(luaString);
					if(result != 0) {
							String debug = L.toString(-1);
							Log.e("LUA",(L.toString(-1)));
					}
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				helper = new SQLiteHelper(this.getApplicationContext());
				database = helper.getWritableDatabase();
				//helper.
				L.pushJavaObject(database);
				L.setGlobal("db");
				//database.exe
				//database.que
		
				 */
	}

	protected void doMaxVitals(int arg1, int arg2) {
		/*int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).updateMaxVitals(arg1, arg2, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();*/
	}

	protected void doVitals(int arg1, int arg2) {
		/*int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).updateVitals(arg1, arg2, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();*/
	}

	public void onDestroy() {
		//Log.e("SERV","ON DESTROY CALLED!");
		//saveXmlSettings(settingslocation);
		//TODO: save connection settings.
		//saveAliases();
		doShutdown();
	}
	
	public void DoDisconnect(Connection c) {
		//attempt to display the disconnection dialog.
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				
				callbacks.getBroadcastItem(i).doDisconnectNotice(c.display);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		callbacks.finishBroadcast();
		
		if(N < 1) {
			//no listeneres, just shutdown and put up a new notification.
			ShowDisconnectedNotification(c,c.display,c.host,c.port);
			//doShutdown();
		}
		
	}
	
	

	/*public void saveXmlSettings(String filename) {
		try {
			FileOutputStream fos = this.openFileOutput(filename,Context.MODE_PRIVATE);
			fos.write(HyperSettings.writeXml2(the_settings).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}*/
	
	/*public void loadXmlSettings(String filename) {
		
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
		//buildTriggerData();
		
		//L.pushJavaObject(((the_settings.getAliases())));
		//L.setGlobal("foo");
		
	}*/
	
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
	
	
	
	void doVibrateBell() {
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
	}
	
	private int bellcount = 3344;
	void doNotifyBell(String display,String host,int port) { 
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		Notification note = new Notification(resId,"Alert!",System.currentTimeMillis());
		//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = display + " - Alert!";
		//CharSequence contentText = "Hello World!";
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
		
		//startForeground to avoid being killed off.
		//this.startForeground(5545, note);
		
		mNM.notify(bellcount,note);
	}
	
	void doDisplayBell() {
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
	

	
	/*private void doThrottleBackgroundImpl() {
		if(pump == null) return;
		if(pump.handler == null) return;
		if(the_settings == null) return;
		
		synchronized(the_settings) {
			if(the_settings.isThrottleBackground()) {
				if(!hasListener && pump != null) {
					if(pump.handler != null) 
						pump.handler.sendEmptyMessage(DataPumper.MESSAGE_THROTTLE);
				} else {
					if(pump.handler != null)
						pump.handler.sendEmptyMessage(DataPumper.MESSAGE_NOTHROTTLE);
				}
			}
		}
		
	}*/
	
	Object binderCookie = new Object();
	Boolean hasListener = false;
	protected boolean isConnected = false;
	
	Pattern newline = Pattern.compile("\n");
	Pattern semicolon = Pattern.compile(";");

	//Pattern commandPattern = Pattern.compile("^.(\\w+)\\s+(.+)$");
	Pattern commandPattern = Pattern.compile("^.(\\w+)\\s*(.*)$");
	Matcher commandMatcher = commandPattern.matcher("");
	
	Character cr = new Character((char)13);
	Character lf = new Character((char)10);
	String crlf = cr.toString() + lf.toString();
	
	/*public String ProcessCommands(String input) {
		
		
		
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
	}*/
	
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
	
	/*StringBuffer joined_alias = new StringBuffer();
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
		
		
			
		
	}*/
	
	
	
	public void sendInitOk() throws RemoteException {
		
		/*final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).loadSettings();
		}
		callbacks.finishBroadcast();*/
	}
	
	public void dispatchXMLError(String error) throws RemoteException {
		/*final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			callbacks.getBroadcastItem(i).displayXMLError(error);
		}
		callbacks.finishBroadcast();*/
	}
	
	public void sendBuffer() throws RemoteException {
		
//		byte[] buf = buffer_tree.dumpToBytes(true);
//		
//		final int N = callbacks.beginBroadcast();
//		for(int i = 0;i<N;i++) {
//			callbacks.getBroadcastItem(i).rawBufferIncoming(buf);
//		}
//		
//		callbacks.finishBroadcast();
//		
//		if( N < 1) {
//			myhandler.sendEmptyMessageDelayed(MESSAGE_REQUESTBUFFER,100);
//			try {
//				buffer_tree.addBytesImpl(buf);
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		} 
	}
	
	/*Pattern trigger_regex = Pattern.compile("");
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
					
					if(trigger.isEnabled()) {
						has_triggers = true;
						if(trigger.isInterpretAsRegex()) {
							trigger_string.append("(" + trigger.getPattern() + ")|");
						} else {
							trigger_string.append("(\\Q" + trigger.getPattern() + "\\E)|");
						}
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
	}*/
	
	//private boolean isWifiLocked = false;
	private WifiManager.WifiLock the_wifi_lock = null;
	private WifiManager the_wifi_manager = null;
	
	public void EnableWifiKeepAlive() {
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
	
	public void DisableWifiKeepAlive() {
		//if we have a wifi lock, release it
		if(the_wifi_lock != null) {
			the_wifi_lock.release();
			the_wifi_lock = null;
		}
	}
	
//	private void DispatchButtonLoad(String setName) {
//		final int N = callbacks.beginBroadcast();
//		for(int i = 0;i<N;i++) {
//			try {
//				callbacks.getBroadcastItem(i).reloadButtons(setName);
//			} catch (RemoteException e) {
//				throw new RuntimeException(e);
//			}
//			//notify listeners that data can be read
//		}
//		callbacks.finishBroadcast();
//	}
	
	public void DispatchToast(String message,boolean longtime) {
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
	
	
	public void DispatchDialog(String message) {
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
	/*
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
	*/
	

	
	
	
	
	

	
	ArrayList<String> timer_actions;
	
	/*private class TimerCommand extends SpecialCommand {
		
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
	}*/
	

	

	
	
	

	

	

	

	
//	private class DumpGMCPCommand extends SpecialCommand {
//		public DumpGMCPCommand() {
//			this.commandName = "dumpgmcp";
//		}
//		
//		public Object execute(Object o) {
//			
//			//the_processor.dumpGMCP();
//			return null;
//		}
//	}
	
	private class LuaCommand extends SpecialCommand {
		public LuaCommand() {
			this.commandName = "lua";
		}
		
		public Object execute(Object o) {
			
			String str = (String)o;
			
			int result = L.LdoString(str);
			if(result == 0) {
				//try {
					//StellarService.this.doDispatchNoProcess("\nFuckin Lua. How does it work?!\n".getBytes("ISO-8859-1"));
					//if(theInterpreter.toString(1) != null) {
					//	StellarService.this.doDispatchNoProcess(theInterpreter.toString(1).getBytes("ISO-8859-1"));
					//}
					LuaObject obj = L.getLuaObject("foo");
					//Log.e("LUA","FOO OBJECT:"+obj.getString());
					try {
						StellarService.this.doDispatchNoProcess(str.getBytes("ISO-8859-1"));
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//} catch (RemoteException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
				//success
			} else {
				
				try {
					StellarService.this.doDispatchNoProcess(("\n"+L.toString(-1)+"\n").getBytes("ISO-8859-1"));
				} catch (RemoteException e) {
					
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
				}
			}
			
			
			
			return null;
		}
	}
	
	private class Lua2Command extends SpecialCommand {
		public Lua2Command() {
			this.commandName = "lua2";
		}
		
		public Object execute(Object o) {
			
			
//			int N = callbacks.beginBroadcast();
//			
//			for(int i=0;i<N;i++) {
//				try {
//					callbacks.getBroadcastItem(i).luaOmg(L.getStateId());
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//			callbacks.finishBroadcast();
			
			
			return null;
		}
	}

	
	private boolean isWindowShowing() {
		boolean result = false;
//		final int N = callbacks.beginBroadcast();
//		for(int i=0;i<N;i++) {
//			try {
//				result = callbacks.getBroadcastItem(i).isWindowShowing();
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//		callbacks.finishBroadcast();
		
		return windowShowing;
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
	

	
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	
	
	
	//Colorizer colorer = new Colorizer();
	public static Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	//StringBuffer regexp_test = new StringBuffer();
	Vector<String> test_set = new Vector<String>();
	
	boolean firstDispatch = true;
	
	public void dispatch(byte[] data) throws RemoteException, UnsupportedEncodingException {
		
		//byte[] rawData = the_processor.RawProcess(data);
		//changing this to send data to the window, then process the triggers.
		//if(firstDispatch)
		//if(rawData == null) {
			return;
		//}
		//Spannable processed = the_processor.DoProcess(data);
		//Message dofinal = myhandler.obtainMessage(MESSAGE_DOFINALDISPATCH,rawData);
		//myhandler.sendMessage(dofinal);
	}
	
	private void reloadButtonSet(String setname) {
		/*final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).reloadButtons(setname);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		callbacks.finishBroadcast();*/
	}
	
	private static int notificationCount = 100;
	public static int getNotificationId() {
		notificationCount += 1;
		return new Integer(notificationCount);
	}
	
	
	

	
	public void doDispatchNoProcess(byte[] data) throws RemoteException{
		
		//buffer_tree.addBytesImplSimple(data);
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
		
		
//		final int N = callbacks.beginBroadcast();
//		int final_count = N;
//		
//
//		//Log.e("SERVICE","SENDING TO WINDOW: " + rawData);
//		for(int i = 0;i<N;i++) {
//			try {
//			callbacks.getBroadcastItem(i).rawDataIncoming(stripped);
//			} catch (RemoteException e) {
//				//just need to catch it, don't need to care, the list maintains itself apparently.
//				final_count = final_count - 1;
//			}
//		}
//		callbacks.finishBroadcast();
	}
	
	
	//Pattern alias_replace = Pattern.compile(joined_alias.toString());
	//Matcher alias_replacer = alias_replace.matcher("");
	//Matcher alias_recursive = alias_replace.matcher("");
	
	//Pattern whiteSpace = Pattern.compile("\\s");
	
	/*private byte[] DoAliasReplacement(byte[] input,Boolean reprocess) {
		//if(joined_alias.length() > 0) {

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
				boolean eatTail = false;
				do {
					recursivefound = false;
					
					//Matcher recursivematch = to_replace.matcher(replaced.toString());
					alias_recursive.reset(replaced.toString());
					buffertemp.setLength(0);
					while(alias_recursive.find()) {
						recursivefound = true;
						AliasData replace_with = the_settings.getAliases().get(alias_recursive.group(0));
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
	}*/
	
	/*private void DoTimerResponders(String ordinal) {
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
				responder.doResponse(StellarService.this.getApplicationContext(),null,null,null,null, display, trigger_count++, hasListener, myhandler, null,L,data.getName());
			}
		}
	}
	
	private void DoTimerStart(String timer,Integer loud) {
		//Debug.waitForDebugger();
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
	}*/
	
	boolean debug = false;
	
	public void doStartup() throws UnknownHostException, IOException, RemoteException {
		//if(host == BAD_HOST || port == BAD_PORT) {
		//	return; //dont' start 
		//}
		
		/*if(debug) {
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
			*/
		
			//pump = new DataPumper(host,port,myhandler);
			//pump.start();
			//synchronized(this) {
			//	try {
			//		this.wait(500);
			//		//give the pump some time sto start up
			//	} catch (InterruptedException e) {
			//		throw new RuntimeException(e);
			//	} 
			//}
			//show notification
			//showNotification();
			//Log.e("LUA","STARTING UP");
			//L = LuaStateFactory.newLuaState();
			//L.openLibs();

			//the_processor = new Processor(myhandler,the_settings.getEncoding(),this.getApplicationContext(),L);
			//synchronized(the_settings) {
			//	if(the_settings.isKeepWifiActive()) {
			//		EnableWifiKeepAlive();
			//	}
				
			//	the_processor.setDebugTelnet(the_settings.isDebugTelnet());
			//}
			
			//GMCPFunction gmcp = new GMCPFunction(the_processor,L);
			
			//isConnected = true;
			
		/*} catch (SocketException e) {
			DispatchDialog("Socket Exception: " + e.getMessage());
			//Log.e("SERVICE","NET FAILURE:" + e.getMessage());
		} catch (SocketTimeoutException e) {
			DispatchDialog("Operation timed out.");
		} catch (ProtocolException e) {
			DispatchDialog("Protocol Exception: " + e.getMessage());
		}*/
		
		initLua();

		//pluginTest();
		//initPlugins();
	}
	
	private void ShowDisconnectedNotification(Connection c,String display,String host,int port) {
		
		
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
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
		Pattern invalidchars = Pattern.compile("\\W"); 
		//Matcher replacebadchars = invalidchars.matcher(display);
		//String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		//SharedPreferences sprefs = this.getSharedPreferences(prefsname,0);
		//SharedPreferences.Editor editor = sprefs.edit();
		//editor.putBoolean("CONNECTED", false);
		//editor.putBoolean("FINISHSTART", true);
		//editor.commit();
		//editor.commit();
		this.stopForeground(true);
		mNM.notify(5546,note);
		showdcmessage = true;
		this.stopSelf();
	}
	
	public void showNotification() {
		
		int resId = this.getResources().getIdentifier(ConfigurationLoader.getConfigurationValue("notificationIcon", this.getApplicationContext()), "drawable", this.getPackageName());
		
		
		Notification note = new Notification(resId,"BlowTorch Connected",System.currentTimeMillis());
		Context context = getApplicationContext();
		CharSequence contentTitle = null;
		CharSequence contentText = null;
		if(connections.size() > 1) {
			contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
			contentText = connections.size() + " connections";
		} else {
			Connection c = connections.get(connectionClutch);
			contentTitle = ConfigurationLoader.getConfigurationValue("ongoingNotificationLabel", this.getApplicationContext());
			contentText = "Connected: ("+ c.host +":"+ c.port + ")";
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
		
		Connection c = connections.get(connectionClutch);
		notificationIntent.putExtra("DISPLAY", c.display);
		notificationIntent.putExtra("HOST", c.host);
		notificationIntent.putExtra("PORT", Integer.toString(c.port));
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		note.icon = resId;
		note.flags = Notification.FLAG_ONGOING_EVENT;
		this.startForeground(5545, note);
		
		
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
		}
		
		this.stopForeground(true);
		
		this.stopSelf();
		
		
	}
	
	//public void doProcess(byte[] data) {
		//broadcast this data.
	//}
	
	private HashMap<String,Connection> connections = null;
	String connectionClutch = "";
	
	public void onRebind(Intent i) {
		//Log.e("LOG","REBIND CALLED");
		
	}
	
	public boolean onUnbind(Intent i) {
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
		// TODO Auto-generated method stub
		connectionClutch = connection;
		for(Connection c : connections.values()) {
			c.deactivate();
		}
		Connection tmp = connections.get(connection);
		if(tmp == null) {
			//dispatch error.
		} else {
			//the_settings = tmp.the_settings;
			tmp.activate();
		}
		
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

		public int getPid() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void initXfer() throws RemoteException {
			//handler.sendEmptyMessage(MESSAGE_STARTUP);
			
			handler.sendEmptyMessage(MESSAGE_STARTUP);
		}

		public void endXfer() throws RemoteException {
			//doStartup();
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
			return connections.get(connectionClutch).isConnected;
		}

		public void sendData(byte[] seq) throws RemoteException {
			Handler handler = connections.get(connectionClutch).handler;
			handler.sendMessage(handler.obtainMessage(Connection.MESSAGE_SENDDATA_BYTES, seq));
		}

		public void saveSettings() throws RemoteException {
			connections.get(connectionClutch).saveMainSettings();
		}

		public void setNotificationText(CharSequence seq)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setConnectionData(String host, int port, String display)
				throws RemoteException {
			Message msg = handler.obtainMessage(MESSAGE_NEWCONENCTION);
			Bundle b = msg.getData();
			b.putString("DISPLAY",display);//, value)
			b.putString("HOST",host);
			b.putInt("PORT",port);
			msg.setData(b);
			handler.sendMessage(msg);
			
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

		public void setAliases(Map map) throws RemoteException {
			connections.get(connectionClutch).setAliases(map);
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

		public void setDirectionData(Map data) throws RemoteException {
			connections.get(connectionClutch).setDirectionData(data);
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
			
			c.processor.setDisplayDimensions(rows, cols);
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
			handler.sendMessage(handler.obtainMessage(MESSAGE_SWITCH,display));
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

		public void registerWindowCallback(String name,IWindowCallback callback)
				throws RemoteException {
			//Log.e("SERVICE","ATTEMPTING TO SET WINDOW CALLBACK FOR:" + connectionClutch);
			connections.get(connectionClutch).registerWindowCallback(name, callback);
		}

		public void unregisterWindowCallback(String name,
				IWindowCallback callback) throws RemoteException {
			connections.get(connectionClutch).unregisterWindowCallback(name,callback);
			
		}

		public String getScript(String plugin, String name)
				throws RemoteException {
			return connections.get(connectionClutch).getScript(plugin,name);
			//return null;
		}

		public void reloadSettings() throws RemoteException {
			handler.sendEmptyMessage(MESSAGE_RELOADSETTINGS);
			
		}

		public void pluginXcallS(String plugin, String function, String str)
				throws RemoteException {
			connections.get(connectionClutch).pluginXcallS(plugin,function,str);
		}

		public Map getPluginList() throws RemoteException {
			
			Connection c = connections.get(connectionClutch);
			HashMap<String,String> list = new HashMap<String,String>();
			
			for(Plugin p : c.plugins) {
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
			for(Plugin p : c.plugins) {
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

		public void setPluginAliases(String plugin, Map map)
				throws RemoteException {
			connections.get(connectionClutch).setPluginAliases(plugin,map);
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
			c.killNetThreads();
			connections.remove(display);
			//switch to the next active connection.
			//connectionClutch = connections.
			showNotification();
		}
		
		@Override
		public void windowShowing(boolean show) {
			//Log.e("Log","window showing: " + show);
			windowShowing = show;
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
			for(Plugin p : c.plugins) {
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
			for(Plugin p : c.plugins) {
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
			return windowShowing;
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
