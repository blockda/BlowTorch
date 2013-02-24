package com.offsetnull.bt.service.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.alias.AliasParser;
import com.offsetnull.bt.responder.IteratorModifiedException;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponder.FIRE_WHEN;
import com.offsetnull.bt.responder.ack.AckResponder;
import com.offsetnull.bt.responder.color.ColorAction;
import com.offsetnull.bt.responder.gag.GagAction;
import com.offsetnull.bt.responder.notification.NotificationResponder;
import com.offsetnull.bt.responder.replace.ReplaceResponder;
import com.offsetnull.bt.responder.script.ScriptResponder;
import com.offsetnull.bt.responder.toast.ToastResponder;
import com.offsetnull.bt.script.ScriptData;
import com.offsetnull.bt.service.Colorizer;
import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.service.ConnectionPluginCallback;
import com.offsetnull.bt.service.SettingsChangedListener;
import com.offsetnull.bt.service.StellarService;
import com.offsetnull.bt.service.WindowToken;
import com.offsetnull.bt.service.plugin.function.DrawWindowFunction;
import com.offsetnull.bt.service.plugin.function.NoteFunction;
import com.offsetnull.bt.service.plugin.function.TriggerEnabledFunction;
import com.offsetnull.bt.service.plugin.settings.BaseOption;
import com.offsetnull.bt.service.plugin.settings.Option;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.timer.TimerParser;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.trigger.TriggerParser;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.window.TextTree.Line;

public class Plugin implements SettingsChangedListener {
	//we are a lua plugin.
	//we can give users 
	//Matcher colorStripper = StellarService.colordata.matcher("");
	LuaState L = null;
	private PluginSettings settings = null;
	Handler mHandler = null;
	Handler innerHandler = null;
	//private String mName = null;
	private String fullPath;
	private String shortName;
	ConnectionPluginCallback parent;
	Context mContext;
	StringBuffer joined_alias = new StringBuffer();
	Pattern alias_replace = Pattern.compile(joined_alias.toString());
	Matcher alias_replacer = alias_replace.matcher("");
	Matcher alias_recursive = alias_replace.matcher("");
	String mEncoding = "ISO-8859-1";
	Pattern whiteSpace = Pattern.compile("\\s");
	HashMap<String,CustomTimerTask> timerTasks = new HashMap<String,CustomTimerTask>();
	private boolean enabled = true;
	private String scriptBlock = "\\";
	//private ArrayList<Integer> optionSkipSaveList = new ArrayList<Integer>();
	
	public static final int LUA_TNIL = 0;
	public static final int LUA_TBOOLEAN = 1;
	public static final int LUA_TTABLE = 5;
	public static final int LUA_TNUMBER = 3;
	public static final int LUA_TSTRING = 4;
	
	
	public Plugin(Handler h,ConnectionPluginCallback parent) throws LuaException {
		setSettings(new PluginSettings());
		mHandler = h;
		L = LuaStateFactory.newLuaState();
		L.openLibs();
		//set up the path and cpath.
		String dataDir = null;
		mContext = parent.getContext();
		try {
			ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
			dataDir = ai.dataDir;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(dataDir == null) {
			//this is bad.
			
		} else {
			//set up the path/cpath.
			L.getGlobal("package");
			//L.getField(-1, "path");
			//String str = L.toString(-1);
			L.pushString(dataDir + "/lua/share/5.1/?.lua");
			L.setField(-2, "path");
			//L.pop(1);
			
			L.pushString(dataDir + "/lua/lib/5.1/?.so");
			L.setField(-2, "cpath");
			L.pop(1);
			
		}
		
		
		//this is going to get ugly.
		//L.newTable();
		//L.pushString("package");
		//L.pushValue(-2);
		//L.setTable(LuaState.LUA_GLOBALSINDEX);
		
		//L.newTable();
		//L.pushString("preload");
		//L.pushValue(-2);
		//L.setTable(-4);
		//L.remove(-2);
		
		//L.pushString("lsqlite3");
		//--L.
		
		
		this.parent = parent;
		//initTimers();
		initLua();
		
	}
	
	public Plugin(PluginSettings settings,Handler h,ConnectionPluginCallback parent) throws LuaException {
		this.settings = settings;
		mHandler = h;
		L = LuaStateFactory.newLuaState();
		mContext = parent.getContext();
		this.parent = parent;
		
		//initTimers();
		
		initLua();
	}
	
	HashMap<String,Long> timerStartTimes;
	
	public void initTimers() {
		innerHandler = new Handler() { 
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case 100:
					DoTimerResponders((String)msg.obj);
					break;
				}
			}
		};
		timerStartTimes = new HashMap<String,Long>();
		CONNECTION_TIMER = new Timer("blowtorch_"+this.getName()+"_timer",true);
		
		for(TimerData timer : settings.getTimers().values()) {
			timer.setRemainingTime(timer.getSeconds());
			if(timer.isPlaying()) {
				
				//l//ong startTime = SystemClock.elapsedRealtime();
				//CustomTimerTask task = new CustomTimerTask(timer.getName());
				//if(timer.isRepeat()) {
				//	timer.setStartTime(startTime);
				//	CONNECTION_TIMER.schedule(task, timer.getSeconds()*1000, timer.getSeconds()*1000);
				//} else {
				//	timer.setStartTime(startTime);
				//	CONNECTION_TIMER.schedule(task, timer.getSeconds()*1000);
				//}
				timer.setPlaying(false);
				startTimer(timer.getName());
			}
		}
	}

	private void initLua() throws LuaException {
		//need to set up global functions, it all goes here.
		
		
		TriggerEnabledFunction tef = new TriggerEnabledFunction(L,this,mHandler);
		tef.register("EnableTrigger");
		L.pushJavaObject(settings.getTriggers());
		L.setGlobal("triggers");
		
		L.pushJavaObject(mContext);
		L.setGlobal("context");
		
		NoteFunction nf = new NoteFunction(L,this,mHandler);
		nf.register("Note");
		
		DrawWindowFunction dwf = new DrawWindowFunction(L,this,mHandler);
		dwf.register("DrawWindow");
		
		WindowFunction wf = new WindowFunction(L);
		ExecuteScriptFunction esf = new ExecuteScriptFunction(L);
		GetWindowFunction mwf = new GetWindowFunction(L);
		WindowBufferFunction wbf = new WindowBufferFunction(L);
		RegisterFunctionCallback rfc = new RegisterFunctionCallback(L);
		DebugFunction df = new DebugFunction(L);
		WindowXCallSFunction wxctf = new WindowXCallSFunction(L);
		AppendLineToWindowFunction altwf = new AppendLineToWindowFunction(L);
		InvalidateWindowTextFunction iwtf = new InvalidateWindowTextFunction(L);
		GMCPSendFunction gsf = new GMCPSendFunction(L);
		UserPresentFunction upf = new UserPresentFunction(L);
		WindowXCallBFunction wxcbf = new WindowXCallBFunction(L);
		GetExternalStorageDirectoryFunction gesdf = new GetExternalStorageDirectoryFunction(L);
		GetDisplayDensityFunction gdsdf = new GetDisplayDensityFunction(L);
		AppendWindowSettingsFunction awsf = new AppendWindowSettingsFunction(L);
		GetStatusBarHeight gsbshf = new GetStatusBarHeight(L);
		//StatusBarHiddenMethod sghm = new StatusBarHiddenMethod(L);
		GetActionBarHeightFunction gabhf = new GetActionBarHeightFunction(L);
		GetPluginInstallDirectoryFunction gpidf = new GetPluginInstallDirectoryFunction(L);
		SendToServerFunction stsf = new SendToServerFunction(L);
		GetPluginIdFunction gpuidf = new GetPluginIdFunction(L);
		GetPluginSettingsFunction gpsf = new GetPluginSettingsFunction(L);
		ReloadSettingsFunction rlsf = new ReloadSettingsFunction(L);
		SaveSettingsFunction ssfun = new SaveSettingsFunction(L);
		NewTriggerFunction ntf = new NewTriggerFunction(L);
		DeleteTriggerFunction dtf = new DeleteTriggerFunction(L);
		CallPluginFunction cpf = new CallPluginFunction(L);
		PluginSupportsFunction psf = new PluginSupportsFunction(L);
		EnableTriggerGroupFunction etgf = new EnableTriggerGroupFunction(L);
		//common functions
		
		gabhf.register("GetActionBarHeight");
		gdsdf.register("GetDisplayDensity");
		gesdf.register("GetExternalStorageDirectory");
		gpuidf.register("GetPluginID");
		gpidf.register("GetPluginInstallDirectory");
		gsbshf.register("GetStatusBarHeight");
		stsf.register("SendToServer");
		//server functions
		altwf.register("AppendLineToWindow");
		awsf.register("AppendWindowSettings");
		esf.register("ExecuteScript");
		gpsf.register("GetPluginSettings");
		mwf.register("GetWindowTokenByName");
		iwtf.register("InvalidateWindowText");
		wf.register("NewWindow");
		rlsf.register("ReloadSettings");
		rfc.register("RegisterSpecialCommand");
		ssfun.register("SaveSettings");
		gsf.register("Send_GMCP_Packet");
		upf.register("UserPresent");
		wbf.register("WindowBuffer");
		wxctf.register("WindowXCallS");
		wxcbf.register("WindowXCallB");
		ntf.register("NewTrigger");
		dtf.register("DeleteTrigger");
		cpf.register("CallPlugin");
		psf.register("PluginSupports");
		etgf.register("EnableTriggerGroup");
		
		/*L.getGlobal("Note");
		L.pushString("this is a test");
		int ret = L.pcall(1, 0, 0);
		if(ret != 0) {
			Log.e("LUA","TRIED TO CALL NOTE BUT FAILED: "+L.getLuaObject(L.getTop()).getString());
		}*/
		
		/*L.pushNil();
		while(L.next(LuaState.LUA_GLOBALSINDEX) != 0) {
			String two = L.typeName(L.type(-2));
			String one = L.typeName(L.type(-1));
			Log.e("LUA","value: " + two + " data: " + one);
		}*/
	}
	
	//public void 

	public void setSettings(PluginSettings settings) {
		this.settings = settings;
		
	}

	public PluginSettings getSettings() {
		return settings;
	}
	
	private final HashMap<String,String> captureMap = new HashMap<String,String>();
	/*public void process(TextTree input,StellarService service,boolean windowOpen,Handler pump,String display) {
		//if(this.settings.getName().equals("map_miniwindow")) {
			//inspection
		//<String,TriggerData> triggers = this.settings.getTriggers();
		//	Collection<TriggerData> c = triggers.values();
		//	c.contains("foo");
		//}
		String host = "";
		int port = 0;
		if(this.settings.getTriggers().size() == 0) return;
		if(sortedTriggers == null) {
			sortTriggers();
		}
		
		List<TriggerData> triggers = sortedTriggers;//new ArrayList<TriggerData>(this.settings.getTriggers().values());
//		Collections.sort(triggers,new Comparator() {
//		
//			
//			
//
//			public int compare(Object arg0, Object arg1) {
//				// TODO Auto-generated method stub
//				TriggerData a = (TriggerData)arg0;
//				TriggerData b = (TriggerData)arg1;
//				//if(a.getSequence() == 5 || b.getSequence() == 5) {
//					//Log.e("COMP","STOP HERE");
//				//}
//				if(a.getSequence() > b.getSequence()) return 1;
//				if(a.getSequence() < b.getSequence()) return -1;
//				
//				return 0;
//			}
//			
//		});
//		//sick ass shit in the hiznizouous
		ListIterator<TextTree.Line> it = input.getLines().listIterator(input.getLines().size());
		boolean keepEvaluating = true;
		int lineNum = input.getLines().size();
		while(it.hasPrevious() && keepEvaluating) {
			boolean done = false;
			boolean modified = false;
			//while(!done) {
				
				//try {
					TextTree.Line l = it.previous();
				//} catch(ConcurrentModificationException e) {
				//	modified = true;
				//	it = input.getLines().listIterator(input.getLines().size() - 1)
				//}
			//}
			//if(!modified) {
				lineNum = lineNum - 1;
			//}
			//StringBuffer tmp = TextTree.deColorLine(l);
			//test this line against each trigger.
			String str = TextTree.deColorLine(l).toString();
			for(TriggerData t : triggers) {
				if(!t.isInterpretAsRegex() && t.getPattern().startsWith("%")) {
					
				} else {
					if(t.isEnabled()) {
						
						t.getMatcher().reset(str);
						while(t.getMatcher().find() && keepEvaluating) {
							if(t.isFireOnce() && t.isFired()) {
								//do nothiong
							} else {
								if(t.isFireOnce()) {
									t.setFired(true);
								}
								
								captureMap.clear();
								for(int i=0;i<=t.getMatcher().groupCount();i++) {
									captureMap.put(Integer.toString(i), t.getMatcher().group(i));
								}
								for(TriggerResponder responder : t.getResponders()) {
									try {
										responder.doResponse(service.getApplicationContext(),input,lineNum,it,l,0,0,"",t, display,host,port, StellarService.getNotificationId(), windowOpen, pump,captureMap,L,t.getName(),mEncoding);
									} catch(IteratorModifiedException e) {
										it = e.getIterator();
									}
									if(input.getLines().size() == 0) {
										return;
									}
								}
								if(!t.isKeepEvaluating()) {
									keepEvaluating = false;
									break;
								}
								
								
							}
						}
					}
				}
			}
		}
		//return null;
	}
	
	public boolean process2(TextTree.Line l,String stripped,int lineNum,TextTree input,StellarService service,boolean windowOpen,Handler pump,String display) throws IteratorModifiedException {
		boolean modified = false;
		if(getSettings().getTriggers().size() == 0) return false;
		if(sortedTriggers == null) {
			sortTriggers();
			buildTriggerSystem();
			if(sortedTriggers == null) {
				return false;
			}
		}
		String host = "";
		int port = 0;
		//for(TriggerData t: sortedTriggers) {
		//	if(!t.isInterpretAsRegex() && t.getPattern().startsWith("%")) {
				
		//	} else {
				//if(t.isEnabled()) {
					massiveMatcher.reset(stripped);
					while(massiveMatcher.find()) {
						int index = -1;
						for(int i=1;i<=massiveMatcher.groupCount();i++) {
							if(massiveMatcher.group(i) != null) {
								index = i;
								i = massiveMatcher.groupCount()+1;
							}
						}
						
						if(index > 0) {
							TriggerData t = sortedTriggerMap.get(index);
							if(t.isFireOnce() && t.isFired()) {
								
							} else {
								if(t.isFireOnce()) {
									t.setFired(true);
								}
							}
							
							int start = massiveMatcher.start();
							int end = massiveMatcher.end();
							String matched = massiveMatcher.group(0);
							
							captureMap.clear();
							for(int i=index;i<=(t.getMatcher().groupCount()+index);i++) {
								captureMap.put(Integer.toString(i), massiveMatcher.group(i));
							}
							
							for(TriggerResponder responder : t.getResponders()) {
	
									responder.doResponse(service.getApplicationContext(),input,lineNum,null,l,start,end,matched,t, display,host,port, StellarService.getNotificationId(), windowOpen, pump,captureMap,L,t.getName(),mEncoding);
								
								if(input.getLines().size() == 0) {
									return true;
								}
							}
							if(!t.isKeepEvaluating()) {
								//keepEvaluating = false;
								break;
							}
						}
					}
				//}
			//}
		//}
			
		return modified;
	}*/
	
	public void initScripts(ArrayList<WindowToken> windows) {
		//for(Script)
		
		
		/*for(String script : settings.getScripts().keySet()) {
			//Log.e("LUA","ATTEMPTING TO LOAD:" + script + "\n" + settings.getScripts().get(script));
			if(script.equals("global")) {
				int ret =L.LdoString(settings.getScripts().get(script));
				if(ret != 0) {
					Log.e("LUA","PROBLEM LOADING SCRIPT:" + L.getLuaObject(-1).getString());
				}
			}
		}*/
	}
	
	
	private class EnableTriggerGroupFunction extends JavaFunction {
		public EnableTriggerGroupFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			String group = this.getParam(2).getString();
			boolean state = this.getParam(3).getBoolean();
			for(TriggerData t : Plugin.this.settings.getTriggers().values()) {
				if(t.getGroup().equals(group)) {
					t.setEnabled(state);
				}
			}
			parent.setTriggersDirty();
			return 0;
		}
	}
	
  /*! \page page1
   * \section common Common Functions
	* \subsection GetActionBarHeight GetActionBarHeight
	* Executes a script that has been loaded during the plugin parsing phase.
	* 
	* \par Full Signature
	* \code
	* GetActionBarHeight()
	* \endcode
	* \param none
	* \returns \b number the height of the ActionBar, 0 if running on Android 2.3 or lower.
	* \par Example 
	* \code
	* barHeight = GetActionBarHeight()
	* \endcode
	*/
	private class GetActionBarHeightFunction extends JavaFunction {
	
		public GetActionBarHeightFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			L.pushString(Integer.toString(((int)Plugin.this.parent.getTitleBarHeight())));
			return 1;
		}
		
	}

  /*! \page page1
	* \subsection GetDisplayDensity GetDisplayDensity
	* Executes a script that has been loaded during the plugin parsing phase.
	* 
	* \par Full Signature
	* \code
	* GetActionBarHeight()
	* \endcode
	* \param none
	* \returns \b number the height of the ActionBar, 0 if running on Android 2.3 or lower.
	* \par Example 
	* \code
	* barHeight = GetActionBarHeight()
	* \endcode
	*/
	private class GetDisplayDensityFunction extends JavaFunction {
	
		public GetDisplayDensityFunction(LuaState L) {
			super(L);
			
		}
	
		@Override
		public int execute() throws LuaException {
			float density = mContext.getResources().getDisplayMetrics().density;
			//if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			//	density = density * 1.5f;
			//}
			//Log.e("WINODW","PUSHING DENSITY:"+Float.toString(density));
			L.pushNumber(density);
			return 1;
		}
		
	}

  /*! \page page1
	* \subsection GetExternalStorageDirectory GetExternalStorageDirectory
	* Get the current external storage volume directory. Checks if the volume exists but \b not if it is write protected.
	* 
	* \par Full Signature
	* \code
	* GetExternalStorageDirectory()
	* \endcode
	* \param none
	* \returns \c string the absolute path to the root of the external storage directory.
	* \returns \c nil if there is no current external storage volume available.
	* \par Example 
	* \code
	* path = GetExternalStorageDirectory()
	* \endcode
	* \note Equivelent lua code
	* \code
	* Environment = luajava.bindClass("android.os.Environment")
	* local path = nil
	* if(Environment:getExternalStorageState() == Environment.MEDIA_MOUNTED) then
	*  path = Environment:getExternalStorageDirectory():getAbsolutePath()
	* else
	*  if(Environment:getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY) then
	*   path = Environment:getExternalStorageDirectory():getAbsolutePath()
	*   Note("alert: external storage is read only!")
	*  end
	* end
	* \endcode
	*/
	private class GetExternalStorageDirectoryFunction extends JavaFunction {
	
		public GetExternalStorageDirectoryFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			//Log.e("PLUGIN","Get External storage state:"+Environment)
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				L.pushString(Environment.getExternalStorageDirectory().getAbsolutePath());
			} else {
				L.pushString("/mnt/sdcard/");
			}
			return 1;
		}
		
	}

  /*! \page page1
	* \subsection GetPluginID GetPluginID
	* Gets the plugin id associated with this plugin.
	* 
	* \par Full Signature
	* \code
	* GetPluginID()
	* \endcode
	* \param none
	* \returns \c string the id that has been assigned to this plugin.
	* \par Example 
	* \code
	* id = GetPluginID()
	* \endcode
	*/
	private class GetPluginIdFunction extends JavaFunction {
		public GetPluginIdFunction(LuaState L) {
			super(L);
		}
		
		@Override
		public int execute() throws LuaException {
			//if(this.getParam(2).isNil()) { return 0; }
			this.L.pushNumber(Plugin.this.getSettings().getId());
			
			//Log.e("LUAWINDOW","script is sending:"+this.getParam(2).getString()+" to server.");
			//parent.handler.sendMessage(parent.handler.obtainMessage(Connection.MESSAGE_SENDDATA_STRING,this.getParam(2).getString()));
			return 1;
		}
		
	}

  /*! \page page1
	* \subsection GetPluginInstallDirectory GetPluginInstallDirectory
	* Get the absolute path to the path that the plugin was loaded from.
	* 
	* \par Full Signature
	* \code
	* GetPluginInstallDirectory()
	* \endcode
	* \param none
	* \returns \c string the absolute path to where the plugin was loaded
	* \par Example 
	* \code
	* path = GetPluginInstallDirectory()
	* \endcode
	* \note this function should always return the path regardless if the path no longer exists to the external volume being unmounted etc.
	*/
	private class GetPluginInstallDirectoryFunction extends JavaFunction {
	
		public GetPluginInstallDirectoryFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			//Log.e("PLUGIN","Get External storage state:"+Environment)
			/*if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				L.pushString(Environment.getExternalStorageDirectory().getAbsolutePath());
			} else {
				L.pushNil();
			}*/
			String path = Plugin.this.getFullPath();
			File file = new File(path);
			String dir = file.getParent();
			//file.getPar
			L.pushString(dir);
			return 1;
		}
		
	}

  /*! \page page1
	* \subsection GetStatusBarHeight GetStatusBarHeight
	* Gets the current height of the status bar.
	* 
	* \par Full Signature
	* \code
	* GetStatusBarHeight()
	* \endcode
	* \param none
	* \returns \c number the size of the status bar, will always be constant regardless of the full screen state.
	* \par Example
	* \code
	* height = GetStatusBarHeight()
	* \endcode
	*/
	private class GetStatusBarHeight extends JavaFunction {
	
		public GetStatusBarHeight(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			L.pushString(Integer.toString((int)Plugin.this.parent.getStatusBarHeight()));
			return 1;
		}
		
	}
	
	
	private class NewTriggerFunction extends JavaFunction {
		
		public NewTriggerFunction(LuaState L) {
			super(L);
		}
		
		@Override
		public int execute() throws LuaException {
			//biiig function.
			TriggerData t = new TriggerData();
			String name = this.getParam(2).getString();
			String pattern = this.getParam(3).getString();
			
			if(Plugin.this.settings.getTriggers().containsKey(name)) {
				this.L.pushString("0");
				return 1;
			}
			
			t.setName(name);
			t.setPattern(pattern);
			
			Log.e("LUA","NEW TRIGGER: " + name + " PATTERN: " + pattern);
			
			//now comes the hard part.
			LuaObject options = this.getParam(4);
			if(options.isTable()) {
				this.L.pushNil();
				Log.e("LUA","DUMPING TRIGGER OPTION TABLE");
				while(this.L.next(4) != 0) {
					String key = this.L.getLuaObject(-2).getString();
					LuaObject obj = this.L.getLuaObject(-1);
					int type = obj.type();
					String value = null;
					//int bssfd = LuaState.LUA_TBOOLEAN;
					switch(type) {
					case LUA_TBOOLEAN:
						value = Boolean.toString(obj.getBoolean()); 
						break;
					}
					
					if(key.equals("enabled")) {
						t.setEnabled(obj.getBoolean());
					} else if(key.equals("once")) {
						t.setFireOnce(obj.getBoolean());
					} else if(key.equals("regex")) {
						t.setInterpretAsRegex(obj.getBoolean());
					} else if(key.equals("group")) {
						t.setGroup(obj.getString());
					}
					Log.e("LUA","KEY: " + key + " VALUE: " + value + " TYPE: "+type);
					this.L.pop(1);
				}
				Log.e("LUA","\n\n");
			} else if(options.isNil()) {
				//assume default options
			} else {
				//error?
			}
			
			//start reading out the possible limitless responders.
			ArrayList<HashMap<String,Object>> responders = new ArrayList<HashMap<String,Object>>();
			
			int top = this.L.getTop();
			for(int i=5;i<=top;i++) {
				LuaObject tmp = this.getParam(i);
				if(tmp.isTable()) {
					HashMap<String,Object> data = new HashMap<String,Object>();
					//pump and dump the table
					this.L.pushNil();
					Log.e("LUA","DUMPING RESPONDER TABLE ARGUMENT: "+i);
					while(this.L.next(i) != 0) {
						String key = this.L.getLuaObject(-2).getString();
						LuaObject obj = this.L.getLuaObject(-1);
						int type = this.L.type(-1);
						Object value = null;
						switch(type) {
						case LUA_TNIL:
							break;
						case LUA_TNUMBER:
							value = new Double(obj.getNumber());
							break;
						case LUA_TBOOLEAN:
							value = new Boolean(obj.getBoolean());
							break;
						case LUA_TSTRING:
							value = obj.getString();
							break;
						}
						data.put(key, value);
						Log.e("LUA","KEY: " + key + " VALUE: " + value + " TYPE: "+type);
						this.L.pop(1);
					}
					
					if(data.size() > 0) {
						responders.add(data);
					}
					Log.e("LUA","\n\n");
				}
			}
			
			//ok now that we are done we can actually make the new trigger.
			for(int i=0;i<responders.size();i++) {
				TriggerResponder r = null;
				HashMap<String,Object> data = responders.get(i);
				String type = (String)data.get("type");
				boolean valid = true;
				if(type.equals("notification")) {
					NotificationResponder tmp = new NotificationResponder();
					Object title = data.get("title");
					Object message = data.get("message");
					Object soundpath = data.get("soundPath");
					Object vibrate = data.get("vibrate");
					Object light = data.get("light");
					Object spawnNew = data.get("spawnNew");
					
					if(title == null) {
						title = new String("Custom title!");
					}
					
					if(message == null) {
						message = new String("Custom message.");
					}
					
					if(soundpath == null) {
						soundpath = new Boolean(false);
					}
					
					if(vibrate == null) {
						vibrate = new Boolean(false);
					}
					
					if(light == null) {
						light = new Boolean(false);
					}
					
					if(spawnNew == null) {
						spawnNew = new Boolean(false);
					}
					
					tmp.setTitle((String)title);
					tmp.setMessage((String)message);
					if(soundpath instanceof String) {
						tmp.setSoundPath((String)soundpath);
						tmp.setUseDefaultSound(true);
					} else if(soundpath instanceof Boolean) {
						boolean b = (Boolean)soundpath;
						if(b) {
							tmp.setSoundPath("");
							tmp.setUseDefaultSound(true);
						} else {
							tmp.setSoundPath("");
							tmp.setUseDefaultSound(false);
						}
					}
					
					if(vibrate instanceof Boolean) {
						boolean b = (Boolean)vibrate;
						if(b) {
							tmp.setUseDefaultVibrate(true);
							tmp.setVibrateLength(0);
						} else {
							tmp.setUseDefaultVibrate(false);
							tmp.setVibrateLength(0);
						}
					} else if(vibrate instanceof Double) {
						tmp.setUseDefaultLight(true);
						int v = ((Double)vibrate).intValue();
						tmp.setVibrateLength((int)v);
					}
					
					if(light instanceof Boolean) {
						boolean b= (Boolean)light;
						if(b) {
							tmp.setUseDefaultLight(true);
							tmp.setColorToUse(0);
						} else {
							tmp.setUseDefaultLight(false);
							tmp.setColorToUse(0);
						}
					} else if(light instanceof Double) {
						tmp.setUseDefaultLight(true);
						int l = ((Double)light).intValue();
						tmp.setColorToUse(l);
					}
					
					if(spawnNew instanceof Boolean) {
						boolean b = (Boolean)spawnNew;
						tmp.setSpawnNewNotification(b);
					}
					
					r = tmp;
				} else if(type.equals("send")) {
					AckResponder tmp = new AckResponder();
					Object text = data.get("text");
					if(text == null) {
						text = "";
					} else if(text instanceof Double) {
						text = Double.toString((Double)text);
					}
					tmp.setAckWith((String)text);
					
					r = tmp;
				} else if(type.equals("toast")) {
					ToastResponder tmp = new ToastResponder();
					Object message = data.get("message");
					Object duration = data.get("duration");
					if(message == null) {
						message = "";
					}
					
					if(duration == null || !(duration instanceof Double)) {
						duration = Double.valueOf(0);
					}
					
					tmp.setMessage((String)message);
					tmp.setDelay(((Double)duration).intValue());
					
					r = tmp;
				} else if(type.equals("gag")) {
					GagAction tmp = new GagAction();
					Object output = data.get("output");
					Object log = data.get("log");
					Object retarget = data.get("retarget");
					if(output == null || !(output instanceof Boolean)) {
						output = true;
					}
					
					if(log == null || !(log instanceof Boolean)) {
						log = true;
					}
					
					if(retarget == null && !(retarget instanceof String)) {
						retarget = "";
					}
					
					tmp.setGagOutput((Boolean)output);
					tmp.setGagLog((Boolean)log);
					tmp.setRetarget((String)retarget);
					
					r = tmp;
				} else if(type.equals("replace")) {
					ReplaceResponder tmp = new ReplaceResponder();
					Object text = data.get("text");
					if(text == null || !(text instanceof String)) {
						text = "";
					}
					tmp.setWith((String)text);
					tmp.setRetarget(null);
					r = tmp;
				} else if(type.equals("color")) {
					ColorAction tmp = new ColorAction();
					Object foreground = data.get("foreground");
					Object background = data.get("background");
					if(foreground == null || !(foreground instanceof Double)) {
						foreground = new Double(256);
					}
					
					if(background == null || !(background instanceof Double)) {
						background = new Double(232);
					}
					tmp.setColor(((Double)foreground).intValue());
					tmp.setBackgroundColor(((Double)background).intValue());
					r = tmp;
				} else if(type.equals("script")) {
					ScriptResponder tmp = new ScriptResponder();
					Object function = data.get("function");
					if(function == null || !(function instanceof String)) {
						function = "";
					}
					tmp.setFunction((String)function);
					r = tmp;
				} else {
					//invalid.
					valid = false;
				}
				
				if(valid) {
				//handle fire type.
					String fire = (String)data.get("fire");
					
					if(fire == null) {
						r.setFireType(FIRE_WHEN.WINDOW_BOTH);
					} else {
						if(fire.equals("always")) {
							r.setFireType(FIRE_WHEN.WINDOW_BOTH);
						} else if(fire.equals("never")) {
							r.setFireType(FIRE_WHEN.WINDOW_NEVER);
						} else if(fire.equals("windowOpen")) {
							r.setFireType(FIRE_WHEN.WINDOW_OPEN);
						} else if(fire.equals("windowClosed")) {
							r.setFireType(FIRE_WHEN.WINDOW_CLOSED);
						}
					}
				}
				t.getResponders().add(r);
			}
			
			Plugin.this.addTrigger(t);
			return 0;
		}
		
	}

   /*! \page page1
	 * 
	 * \subsection sec1 Note
	 * This is the basic linkage between Lua and the Console. The function will echo the parameter string to the main window.
	 * \par Full Signature
	 * \code
	 * Note(text)
	 * \endcode
	 * \param text The text to echo back
	 * \returns nothing
	 * \par Example
	 * \code
	 * Note("Example text!")
	 * \endcode
	 * 
	*/
	
  /*! \page page1
	* \subsection SendToServer SendToServer
	* Send the given string to the server.
	* 
	* \par Full Signature
	* \code
	* SendToServer(str)
	* \endcode
	* \param str \c string the data to send to the server
	* \returns nothing
	* \par Example 
	* \code
	* SendToServer("run north;open door")
	* \endcode
	* \note This is the same as sending data from the keyboard. The data is processed for special commands and aliases.
	*/
	private class SendToServerFunction extends JavaFunction {
	
		public SendToServerFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			if(this.getParam(2).isNil()) { return 0; }
			//Log.e("LUAWINDOW","script is sending:"+this.getParam(2).getString()+" to server.");
			Plugin.this.mHandler.sendMessage(Plugin.this.mHandler.obtainMessage(Connection.MESSAGE_SENDDATA_STRING,this.getParam(2).getString()));
			return 0;
		}
		
	}

	/*! \page page1
	   * \section service Service Functions
		* \subsection AppendLineToWindow AppendLineToWindow
		* Sends a packet of gmcp data to the server
		* 
		* \par Full Signature
		* \code
		* AppendLineToWindow(line,windowName)
		* \endcode
		* \param line \c com.offsetnull.bt.window.TextTree$Line the line to append, this usually comes from a trigger callback
		* \returns none
		* \par Example 
		* \code
		* function calledFromTrigger(line,number,map)
		*  AppendLineToWindow(line,GetPluginID().."_chat_window")
		* end
		* \endcode
		*/
		private class AppendLineToWindowFunction extends JavaFunction {
	
			public AppendLineToWindowFunction(LuaState L) {
				super(L);
			}
	
			@Override
			public int execute() throws LuaException {
				//the only arguments should be a TextTree.Line copied from the one passed to it from a trigger script action
				//and the window id to send it to.
				LuaObject o = this.getParam(3);
				
				TextTree.Line line = null;
				String windowId = (this.getParam(2).getString());
				
				if(o.isJavaObject()) {
					//test if it is a real line.
					Object tmp = o.getObject();
					if(tmp instanceof TextTree.Line) {
						line = (TextTree.Line)tmp;
						
						//now abuse the lineToWindow handler from the replace action to ferry this bad boy across
						Message m = mHandler.obtainMessage(Connection.MESSAGE_LINETOWINDOW,line);
						m.getData().putString("TARGET", windowId);
						mHandler.sendMessage(m);
					} else {
						//error is java object but not a TextTree.Line
					}
				} else if(o.isString()) {
					//construct a new line and append it.
					Message m = mHandler.obtainMessage(Connection.MESSAGE_LINETOWINDOW,o.getString());
					m.getData().putString("TARGET", windowId);
					mHandler.sendMessage(m);
				} else {
					//error bad argument
				}
				
				//TextTree.Line line = (TextTree.Line)(this.getParam(3)).getObject();
				
				
				
				return 0;
			}
			
		}

	/*! \page page1
	* \subsection AppendWindowSettings AppendWindowSettings
	* Attatches a settings group from a window to the plugin settings block. This is to allow a plugin writer to include window settings in the main options dialog block at thier discretion.
	* 
	* \par Full Signature
	* \code
	* AppendWindowSettings(name)
	* \endcode
	* \param name \c string the line to append, this usually comes from a trigger callback
	* \returns none
	* \par Example 
	* \code
	* function OnBackgroundStartup()
	* 	AppendWindowSettings(GetPluginID().."_chat_window")
	* end
	* \endcode
	*/
		
	private class AppendWindowSettingsFunction extends JavaFunction {
	
		public AppendWindowSettingsFunction(LuaState L) {
			super(L);
			
		}
	
		@Override
		public int execute() throws LuaException {
			//float density = parent.getContext().getResources().getDisplayMetrics().density;
			//if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			//	density = density * 1.5f;
			//}
			//Log.e("WINODW","PUSHING DENSITY:"+Float.toString(density));
			//L.pushNumber(density);
			String name = this.getParam(2).getString();
			WindowToken w = Plugin.this.getSettings().getWindows().get(name);
			if(w != null) {
				//mWindows.get(0).getSettings().setListener(new WindowSettingsChangedListener(mWindows.get(0).getName()));
				parent.attatchWindowSettingsChangedListener(w);
				w.getSettings().setSkipForPluginSave(true);
				Plugin.this.getSettings().getOptions().addOption(w.getSettings());
				//optionSkipSaveList.add(Plugin.this.getSettings().getOptions().getOptions().size()-1);
			}
			return 0;
		}
		
	}
	
	private class CallPluginFunction extends JavaFunction {
		public CallPluginFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			
			String plugin = this.getParam(2).getString();
			String function = this.getParam(3).getString();
			String data = this.getParam(4).getString();
			
			parent.callPlugin(plugin,function,data);
			
			return 0;
		}
		
		
	}
	
	private class DeleteTriggerFunction extends JavaFunction {
		public DeleteTriggerFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			
			String name = this.getParam(2).getString();
			if(name != null) {
				if(Plugin.this.settings.getTriggers().containsKey(name)) {
					Plugin.this.settings.getTriggers().remove(name);
				}
			}
			parent.setTriggersDirty();
			return 0;
		}
	}

	/*! \page page1
		* \subsection ExecuteScript ExecuteScript
		* Executes a script that has been loaded during the plugin parsing phase.
		* 
		* \par Full Signature
		* \code
		* ExecuteScript(name)
		* \endcode
		* \param name \c name of the script to run, this is configured in the plugin settings.
		* \returns none
		* \par Example 
		* \code
		* ExecuteScript("parseInventory")
		* \endcode
		*/	
	  private class ExecuteScriptFunction extends JavaFunction {
	
			public ExecuteScriptFunction(LuaState L) {
				super(L);
				// TODO Auto-generated constructor stub
			}
	
			@Override
			public int execute() throws LuaException {
				// TODO Auto-generated method stub
				String pName = this.getParam(2).getString();
				ScriptData d = Plugin.this.getSettings().getScripts().get(pName);
				String body = d.getData();
				if(body != null) {
					L.LdoString(body);
				} else {
					//error
					//L.pushString(bytes)
					//L.error();
				}
				return 0;
			}
			
		}
	/*! \page page1
	* \subsection GetWindowTokenByName GetWindowTokenByName
	* Gets the raw /c com.offsetnull.bt.service.WindowToken object that is being held by the background service. This is to allow direct manipulation of the buffer.
	* 
	* \par Full Signature
	* \code
	* GetWindowTokenByName(name)
	* \endcode
	* \param name \c string the id of the window to get.
	* \returns none
	* \par Example 
	* \code
	* window = GetWindowTokenByName(GetPluginID().."_chat_window")
	* \endcode
	*/	
	private class GetWindowFunction extends JavaFunction {

		public GetWindowFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			/*int x = (int) this.getParam(2).getNumber();
			int y = (int) this.getParam(3).getNumber();
			int width = (int) this.getParam(4).getNumber();
			int height = (int) this.getParam(5).getNumber();
			
			Message msg = mHandler.obtainMessage(Connection.MESSAGE_MODMAINWINDOW);
			Bundle b = msg.getData();
			b.putInt("X", x);
			b.putInt("Y", y);
			b.putInt("WIDTH", width);
			b.putInt("HEIGHT", height);
			msg.setData(b);
			mHandler.sendMessage(msg);*/
			String desired = this.getParam(2).getString();
			WindowToken t = parent.getWindowByName(desired);
			if(t == null) {
				//check our local window that haven't been loaded into the main window group.
				for(WindowToken tmp : settings.getWindows().values()) {
					if(tmp.getName().equals(desired)) {
						t = tmp;
					}					
				}
			}
			L.pushJavaObject(t);
			
			return 1;
		}
		
	}
	
	/*! \page page1
	* 
	* \subsection InvalidateWindowText InvalidateWindowText
	* Invalidates a foreground windows text and forces it to redraw.
	* 
	* \par Full Signature
	* \code
	* InvalidateWindowText(name)
	* \endcode
	* \param name \c string the name of the winodw to redraw
	* \returns none
	* \par Example 
	* \code
	* InvalidateWindowText(GetPluginID().."_chat_window")
	* \endcode
	*/
	private class InvalidateWindowTextFunction extends JavaFunction {
	
		public InvalidateWindowTextFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			String name = this.getParam(2).getString();
			mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_INVALIDATEWINDOWTEXT,name));
			return 0;
		}
		
	}

	/*! \page page1
	* 
	* \subsection GetPluginSettings GetPluginSettings
	* Gets the raw \c com.offsetnull.bt.service.settings.SettingsGroup settings, this is to allow direct manipluation.
	* 
	* \par Full Signature
	* \code
	* InvalidateWindowText(name)
	* \endcode
	* \param none
	* \returns a \c com.offsetnull.bt.service.settings.SettingsGroup object that can be directly manipulated.
	* \par Example 
	* \code
	* settings = GetPluginSettings()
	* \endcode
	*/
	private class GetPluginSettingsFunction extends JavaFunction {
		public GetPluginSettingsFunction(LuaState L) {
			super(L);
		}
		
		@Override
		public int execute() throws LuaException {
			//if(this.getParam(2).isNil()) { return 0; }
			this.L.pushJavaObject(Plugin.this.getSettings().getOptions());
			
			//Log.e("LUAWINDOW","script is sending:"+this.getParam(2).getString()+" to server.");
			//parent.handler.sendMessage(parent.handler.obtainMessage(Connection.MESSAGE_SENDDATA_STRING,this.getParam(2).getString()));
			return 1;
		}
		
	}

	/*! \page page1
	* \subsection NewWindow NewWindow
	* Makes a new window with the given paramters.
	* 
	* \par Full Signature
	* \code
	* NewWindow(name,x,y,width,height,script)
	* \endcode
	* \param name \c string name or id of the window
	* \param x \c number the x coordinate of the window
	* \param y \c number the y coordinate of the window
	* \param width \c number the width of the window
	* \param height \c number the height of the window
	* \param script \c string the named script to load into the window's Lua state.
	* \returns none
	* \par Example 
	* \code
	* NewWindow("chat_window",0,0,400,400,"chat_script")
	* \endcode
	* \note positioning the window and having it interact with other windows is a much more complicated problem that deserves its own page.
	*/
	private class WindowFunction extends JavaFunction {
	
		public WindowFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			String name = null;
			int x = 0;
			int y = 0;
			int width = 0;
			int height = 0;
			String scriptName = null;
			
			LuaObject pName = this.getParam(2);
			LuaObject pX = this.getParam(3);
			LuaObject pY = this.getParam(4);
			LuaObject pWidth = this.getParam(5);
			LuaObject pHeight = this.getParam(6);
			LuaObject pScriptName = this.getParam(7);
			
			if(pName.isString()) {
				name = pName.getString();
			} else {
				//error
			}
			
			if(pX.isNil() || pY.isNil() || pWidth.isNil() || pHeight.isNil()) {
				//errror
			}
			
			if(!pX.isNumber() || !pY.isNumber() || !pWidth.isNumber() || !pHeight.isNumber()) {
				//error
			}
			
			x = (int) pX.getNumber();
			y = (int) pY.getNumber();
			width = (int) pWidth.getNumber();
			height = (int) pHeight.getNumber();
			
			if(!pScriptName.isNil() && pScriptName.isString()) {
				scriptName = pScriptName.getString();
			}
			
			WindowToken tok = null;
			if(pScriptName.isNil()) {
				tok = new WindowToken(name,null,null,parent.getDisplayName());
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_NEWWINDOW, tok));
			} else {
				tok = new WindowToken(name,scriptName,Plugin.this.getSettings().getName(),parent.getDisplayName());
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_NEWWINDOW, tok));
			}
			
			L.pushJavaObject(tok);
			
			return 1;
		}
		
	}
	
	private class PluginSupportsFunction extends JavaFunction {
		public PluginSupportsFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			String plugin = this.getParam(2).getString();
			String function = this.getParam(3).getString();
			
			boolean ret = parent.pluginSupports(plugin,function);
			L.pushBoolean(ret);
			return 1;
		}
	}

	/*! \page page1
	* \subsection ReloadSettings ReloadSettings
	* Causes the BlowTorch core to dump the current settings and reload them from the source files.
	* 
	* \par Full Signature
	* \code
	* ReloadSettings()
	* \endcode
	* \param none
	* \returns none
	* \par Example 
	* \code
	* ReloadSettings()
	* \endcode
	* \note positioning the window and having it interact with other windows is a much more complicated problem that deserves its own page.
	*/
	private class ReloadSettingsFunction extends JavaFunction {
		public ReloadSettingsFunction(LuaState L) {
			super(L);
		}
		
		@Override
		public int execute() throws LuaException {
			//if(this.getParam(2).isNil()) { return 0; }
			//this.L.pushNumber(Plugin.this.getSettings().getId());
			
			//Log.e("LUAWINDOW","script is sending:"+this.getParam(2).getString()+" to server.");
			Plugin.this.mHandler.sendMessage(Plugin.this.mHandler.obtainMessage(Connection.MESSAGE_RELOADSETTINGS));
			return 0;
		}
		
	}

	/*! \page page1
	* \subsection RegisterSpecialCommand RegisterSpecialCommand
	* Adds and entry into the "special command" processor, or .nameyouwant and it will call the specified global function
	* 
	* \par Full Signature
	* \code
	* RegisterSpecialCommand(sortName,callbackName)
	* \endcode
	* \param shortName \c string short name that will be searched for
	* \param callbackName \c string the name of a global function to call when this is called.
	* \returns none
	* \par Example 
	* \code
	* function goHome(args)
	* 	SendToServer("enter portal")
	* end
	* RegisterSpecialCommand("home","goHome")
	* \endcode
	* \note the callback that is called when the command is processed will give the arguments as a single string to the callback function.
	*/
	private class RegisterFunctionCallback extends JavaFunction {
	
		public RegisterFunctionCallback(LuaState L) {
			super(L);
			
		}
	
		@Override
		public int execute() throws LuaException {
			LuaObject name = this.getParam(2);
			LuaObject function = this.getParam(3);
			
			if(name == null) {
				return 0;
			}
			
			if(function == null) {
				return 0;
			}
			
			if(!name.isString()) {
				return 0;
			}
			
			if(!function.isString()) {
				return 0;
			}
			//function.tos
			String funcstring = function.getString();
			Log.e("PLUGIN","SENDING FUNCTION:" + name + "("+funcstring+"): for inclusion into the global .command processor.");
			
			Message msg = mHandler.obtainMessage(Connection.MESSAGE_ADDFUNCTIONCALLBACK);
			Bundle b = msg.getData();
			b.putString("ID", settings.getName());
			b.putString("COMMAND", name.getString());
			b.putString("CALLBACK", funcstring);
			msg.setData(b);
			mHandler.sendMessage(msg);
			return 0;
		}
		
		
		
	}
	/*! \page page1
	* \subsection SaveSettings SaveSettings
	* Initiates the saving of the whole settings wad for the currently open connection.
	*  
	* \par Full Signature
	* \code
	* SaveSettings()
	* \endcode
	* \param none
	* \returns none
	* \par Example 
	* \code
	* SaveSettings()
	* \endcode
	* \note the callback that is called when the command is processed will give the arguments as a single string to the callback function.
	*/
	private class SaveSettingsFunction extends JavaFunction {
		
		public SaveSettingsFunction(LuaState L) {
			super(L);
		}
	
		@Override
		public int execute() throws LuaException {
			mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_SAVESETTINGS,Plugin.this.getName()));
			return 0;
		}
	}

	/*! \page page1
	* \subsection GMCPSend Send_GMCP_Packet
	* Sends a packet of gmcp data to the server
	* 
	* \par Full Signature
	* \code
	* Send_GMCP_Packet(str)
	* \endcode
	* \param \c string the data to send, please see the GMCP Documentation
	* \returns note
	* \par Example 
	* \code
	* GMCPSend("core.hello foo")
	* \endcode
	*/
	private class GMCPSendFunction extends JavaFunction {
		public GMCPSendFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			String str = this.getParam(2).getString();
			Log.e("LUA","GMCP SEND:" + str);
			mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_SENDGMCPDATA,str));
			return 0;
		}
		
		
	}
	/*! \page page1
	* \subsection UserPresent UserPresent
	* Call to get a boolean indicating if the screen is on / user present.
	* 
	* \par Full Signature
	* \code
	* UserPresent()
	* \endcode
	* \param none
	* \returns boolean true if the user is present, false if not
	* \par Example 
	* \code
	* present = UserPresent()
	* \endcode
	*/
	private class UserPresentFunction extends JavaFunction {
	
		public UserPresentFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			L.pushBoolean(parent.isWindowShowing());
			return 1;
		}
		
	}

	/*! \page page1
	* \subsection WindowBuffer WindowBuffer
	* Instructs a named window to either start or stop buffering incoming text
	* 
	* \par Full Signature
	* \code
	* WindowBuffer(name,state)
	* \endcode
	* \param name \c string the name of the window to affect
	* \param state \c boolean the state of the buffering desired
	* \returns nothing
	* \par Example 
	* \code
	* WindowBuffer(GetPluginID().."_chat_window",false)
	* \endcode
	*/
	private class WindowBufferFunction extends JavaFunction {

		public WindowBufferFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String win = this.getParam(2).getString();
			boolean set = this.getParam(3).getBoolean();
			Log.e("PLUGIN","MODDING WINDOW("+win+") Buffer:"+set);
			mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_WINDOWBUFFER, set ? 1 : 0, 0, win));
			
			return 0;
		}
		
	}
	
	/*! \page page1
	* \subsection WindowXCallB WindowXCallB
	* Sends a message to a foreground window that it should run a specified callback with the desired argument data.
	* 
	* \par Full Signature
	* \code
	* WindowXCallB(name,data)
	* \endcode
	* \param name \c string the global callback in the window to call
	* \param data \c string the data to send
	* \returns nothing
	* \par Example 
	* \code
	* WindowXCallB(GetPluginID().."_chat_window",42)
	* \note Symantically this is the same as WindowXCallS only great care has been taken to ensure that the data that is ferried across the aidl bridge and delivered to the foreground window's lua state as an array of \b bytes without having any intervention by the DalvikVM host converting it through a java string to avoid corruption. This is largley to support large data tables being serialized with libmarshal or any other binary serialization format.
	* \endcode
	*/
	private class WindowXCallBFunction extends JavaFunction {
	
		public WindowXCallBFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		public int execute() throws LuaException {
			String token = this.getParam(2).getString();
			String function = this.getParam(3).getString();
			byte[] foo = null;
			//try {
				foo = this.getParam(4).getBytes();
			//} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
			//L.L
			Message msg = mHandler.obtainMessage(Connection.MESSAGE_WINDOWXCALLB,foo);
			//String str = "";
			//try {
			//	str = parent.windowXCallS(token, function, foo.getString());
			//} catch (RemoteException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			msg.getData().putString("TOKEN",token);
			msg.getData().putString("FUNCTION", function);
			//L.pushString(str);
			mHandler.sendMessage(msg);
			return 0;
		}
		
	}

	/*! \page page1
	* \subsection WindowXCallS WindowXCallS
	* Sends a message to a foreground window that it should run a specified callback with the desired argument data.
	* 
	* \par Full Signature
	* \code
	* WindowXCallS(name,data)
	* \endcode
	* \param name \c string the global callback in the window to call
	* \param data \c string the data to send
	* \returns nothing
	* \par Example 
	* \code
	* WindowXCallS(GetPluginID().."_chat_window",42)
	* \note Symantically this is the same as WindowXCallB, only the data is cross converted to a DalviVM string through the aidl bridge, this can cause some problems with binary data. If you need very large serialized tables, or any kind of binary data, see WindowXCallB
	* \endcode
	*/
	private class WindowXCallSFunction extends JavaFunction {
		//HashMap<String,String> 
		public WindowXCallSFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String token = this.getParam(2).getString();
			String function = this.getParam(3).getString();
			LuaObject foo = this.getParam(4);
			
			
			//--if(foo.isTable()) {
			//-	Log.e("DEBUG","ARGUMENT IS TABLE");
			//}
			//HashMap<String,Object> dump = dumpTable("t",4);
			//
			/*L.pushNil();
			while(L.next(2) != 0) {
				
				String id = L.toString(-2);
				LuaObject l = L.getLuaObject(-1);
				if(l.isTable()) {
					//need to dump more tables
				} else {
					
				}
			}*/
			//mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_X, obj))
			Message msg = null;
			
			if(foo.isNil()) {
				msg = mHandler.obtainMessage(Connection.MESSAGE_WINDOWXCALLS);
			} else {
				msg = mHandler.obtainMessage(Connection.MESSAGE_WINDOWXCALLS,foo.getString());
			}
			
			
			//String str = "";
			//try {
			//	str = parent.windowXCallS(token, function, foo.getString());
			//} catch (RemoteException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			msg.getData().putString("TOKEN",token);
			msg.getData().putString("FUNCTION", function);
			//L.pushString(str);
			mHandler.sendMessage(msg);
			// TODO Auto-generated method stub
			return 1;
		}
	}
	
	private class DebugFunction extends JavaFunction {

		public DebugFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String foo = this.getParam(2).getString();
			Log.e("LUAWINDOW","DEBUG:"+foo);
			return 0;
		}
		
	}
	
	public void shutdown() {
		// TODO Auto-generated method stub
		//L.close();
		L = null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return settings.getName();
	}
	
	public void displayLuaError(String message) {
		mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_PLUGINLUAERROR,"\n" + Colorizer.colorRed + message + Colorizer.colorWhite + "\n"));
	}

	public void execute(String callback,String args) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.getLuaObject(-1).isFunction()) {
			L.pushString(args);
			
			int ret = L.pcall(1, 1, -3);
			
			if(ret != 0) {
				displayLuaError("Error calling function callback:"+settings.getName()+"("+callback+"):"+L.getLuaObject(-1).getString());
			} else {
				//Log.e("PLUGIN","Successfuly called plugin function:"+settings.getName()+"("+callback+")");
				L.pop(2);
			}
		
		} else {
			L.pop(2);
		}
		
		checkStack("ExecuteCallback");
		
	}

	public void xcallS(String function, String str) {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal(function);
		if(L.getLuaObject(-1).isFunction()) {
			//pushTable("",map);
			L.pushString(str);
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("PluginXCallS Error:" + L.getLuaObject(-1).getString());
			} else {
				//success
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
		
		checkStack("PluginXCallS");
	}
	
	private void pushTable(String key,Map<String,Object> map) {
		if(!key.equals("")) {
			L.pushString(key);
		}
		
		L.newTable();
		
		for(String tmp : map.keySet()) {
			Object o = map.get(tmp);
			if(o instanceof Map) {
				pushTable(tmp,(Map)o);
			} else {
				if(o instanceof String) {
					L.pushString(tmp);
					L.pushString((String)o);
					L.setTable(-3);
				} else if(o instanceof Integer) {
					L.pushString(tmp);
					L.pushString(Integer.toString((Integer)o));
					L.setTable(-3);
				}
			}
		}
		if(!key.equals("")) {
			L.setTable(-3);
		}
	}

	public LuaState getLuaState() {
		// TODO Auto-generated method stub
		return L;
	}
	
	/*public void outputXMLInternal(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		//see if lua has a SaveXML method.
		
		L.getGlobal("saveXML");
		if(L.getLuaObject(-1).isFunction()) {
			//call it and allow plugin to dump settings to the main wad.
			//need to dump plugin constants.
			//then call lua.
			out.startTag("", "plugin");
			dumpPluginCommonData(out);
			dumpLuaData(out);
			out.endTag("", "plugin");
		}
	}
	
	public void outputXMLExternal(StellarService service,XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", "plugin");
		dumpPluginCommonData(out);
		dumpLuaData(out);
		out.endTag("", "plugin");
	}*/
	
	/*private void dumpLuaData(XmlSerializer out) {
		//now call the saveXML function in lua.
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		L.getGlobal("saveXML");
		if(L.getLuaObject(-1).isFunction()) {
			//out.startT
			
			L.pushJavaObject(out);
			
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("Plugin SaveXML Error:" + L.getLuaObject(-1).getString());
			} else {
				//success
			}
			
		} else {
			L.pop(2);
		}
		
		checkStack("SaveXML");
	}*/
	
/*	public void outputXMLExternal(StellarService service) {
		L.getGlobal("saveXML");
		if(L.getLuaObject(-1).isFunction()) {
			//set up file for writing, calling saveXML.
			try {
				FileOutputStream fos = service.openFileOutput(settings.getPath(), Context.MODE_PRIVATE);
				
				XmlSerializer out = Xml.newSerializer();
				
				StringWriter writer = new StringWriter();
				
				out.setOutput(writer);
				
				out.startDocument("UTF-8", true);
				
				out.startTag("", "plugin");

				dumpPluginCommonData(out);
				
				out.endTag("", "plugin");
				out.endDocument();
				
				fos.write(writer.toString().getBytes());
				
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
			
			
		} else {
			//no saveXML detected, no saving needed, as the next parse will just pick up the same info.
	
		}
	}*/
	
	private void dumpPluginCommonData(XmlSerializer out) {
		try{
			out.attribute("", "author", settings.getAuthor());
			out.attribute("", "name", settings.getName());
			out.attribute("", "id", Integer.toString(settings.getId()));
			
		
			//out.
			//dump common/normal plugin data.
			for(AliasData alias : settings.getAliases().values()) {
				AliasParser.saveAliasToXML(out, alias);
			}
			
			for(TriggerData trigger : settings.getTriggers().values()) {
				TriggerParser.saveTriggerToXML(out, trigger);
			}
			
			for(TimerData timer : settings.getTimers().values()) {
				TimerParser.saveTimerToXML(out,timer);
			}
			
			for(String script : settings.getScripts().keySet()) {
				
				ScriptData d = settings.getScripts().get(script);
				out.startTag("", "script");
				out.attribute("", "name", script);
				if(d.isExecute()) {
					out.attribute("", "execute", "true");
				}
				
				//out.text(settings.getScripts().get(script));
				out.cdsect(d.getData());
				
				//out.cdsect(text)
				out.endTag("", "script");
			}
			
			/*L.pop(1);
			L.getGlobal("debug");
			L.getField(-1,"traceback");
			L.remove(-2);
			//L.pushJavaObject(out);
			L.getGlobal("saveXML");
			L.pushJavaObject(out);
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				Log.e("PLUGIN","SaveXML Error:" + L.getLuaObject(-1).getString());
				return;
			}*/
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}
	
	public int getTriggerCount() {
		return settings.getTriggers().size();
	}
	
	public int getAliasCount() {
		return settings.getAliases().size();
	}
	
	public int getTimerCount() {
		return settings.getTimers().size();
	}
	
	public int getScriptCount() {
		return settings.getScripts().size();
	}
	
	public String getStorageType() {
		switch(settings.getLocationType()) {
		case INTERNAL:
			return "INTERNAL";
			//break;
		case EXTERNAL:
			return "EXTERNAL";
			//break;
		default:
			return "OOPS";
				//break;
		}
	}

	public void handleGMCPCallback(String callback, HashMap<String, Object> data) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.getLuaObject(L.getTop()).isFunction()) {
			pushTable("",data);
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("Error calling gmcp callback:" + callback + " error:\n"+L.getLuaObject(-1).getString());
			} else {
				//success.
				L.pop(2);
			}
		} else {
			//callback not defined.
			L.pop(2);
		}
		
		//checkStack("GMCP Callback");
	}
	
	public void addTrigger(TriggerData data) {
		this.getSettings().getTriggers().put(data.getName(), data);
		this.sortTriggers();
		parent.buildTriggerSystem();
		settings.setDirty(true);
	}

	public void updateTrigger(TriggerData from, TriggerData to) {
		TriggerData tmp = this.getSettings().getTriggers().remove(from.getName());
		this.getSettings().getTriggers().put(to.getName(),to);
		tmp = null;
		this.sortTriggers();
		parent.buildTriggerSystem();
		settings.setDirty(true);
	}
	
	HashMap<Integer,AliasData> aliasMap = null;
	public void buildAliases() {
		joined_alias.setLength(0);
		
		aliasMap = new HashMap<Integer,AliasData>();
		//Object[] a = the_settings.getAliases().keySet().toArray();
		Object[] a = getSettings().getAliases().values().toArray();
		int currentGroup = 1;
		
		String prefix = "\\b";
		String suffix = "\\b";
		//StringBuffer joined_alias = new StringBuffer();
		
		if(a.length > 0) {
			int j=0;
			for(int i=0;i<a.length;i++) {
				if(((AliasData)a[i]).isEnabled()) {
					if(((AliasData)a[i]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
					if(((AliasData)a[i]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
					String tmp = "("+prefix+((AliasData)a[i]).getPre()+suffix+")";
					joined_alias.append(tmp);
					Matcher m = Pattern.compile(tmp).matcher("");
					aliasMap.put(currentGroup, (AliasData)a[i]);
					currentGroup += m.groupCount();
					j=i+1;
					i=a.length;
					
				}
			}
			for(int i=j;i<a.length;i++) {
				if(((AliasData)a[i]).isEnabled()) {
					if(((AliasData)a[i]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
					if(((AliasData)a[i]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
					String tmp = "("+prefix+((AliasData)a[i]).getPre()+suffix+")";
					//joined_alias.append(tmp);
					Matcher m = Pattern.compile(tmp).matcher("");
					aliasMap.put(currentGroup, (AliasData)a[i]);
					currentGroup += m.groupCount();
					
					joined_alias.append("|");
					joined_alias.append(tmp);
					//joined_alias.append("("+prefix+((AliasData)a[i]).getPre()+suffix+")");
				}
			}
			
		}
		
		alias_replace = Pattern.compile(joined_alias.toString());
		alias_replacer = alias_replace.matcher("");
		alias_recursive = alias_replace.matcher("");
		//Log.e("SERVICE","BUILDING ALIAS PATTERN: " + joined_alias.toString());
	}
	
	public byte[] doAliasReplacement(byte[] input,Boolean reprocess) {
		if(joined_alias.length() > 0) {

			//Pattern to_replace = Pattern.compile(joined_alias.toString());
			byte[] retval = null;
			//Matcher replacer = null;
			try {
				alias_replacer.reset(new String(input,mEncoding));//replacer = to_replace.matcher(new String(bytes,the_settings.getEncoding()));
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
			StringBuffer replaced = new StringBuffer();
			
			boolean found = false;
			boolean doTail = true;
			while(alias_replacer.find()) {
				found = true;
				
				
				int index = -1;
				for(int i=1;i<=alias_replacer.groupCount();i++) {
					if(alias_replacer.group(i) != null) {
						index = i;
						i=alias_replacer.groupCount();
					}
				}
				//String str = alias_replacer.group(0);
				AliasData replace_with = aliasMap.get(index);
				//AliasData replace_with = getSettings().getAliases().get(alias_replacer.group(0));
				//do special replace if only ^ is matched.
				//do lua execute if ^ and $ is matched
				
				
				boolean startAnchor = replace_with.getPre().startsWith("^");
				boolean endAnchor = replace_with.getPre().endsWith("$");
				
				if(startAnchor && !endAnchor) {
					doTail = false;
					//do special replace.
					String[] tParts = null;
					try {
						tParts = whiteSpace.split(new String(input,mEncoding));
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
					
				} else if(startAnchor && endAnchor) {
					//ok, we have to run the matcher and generate the capture group

					
					Pattern aliasHarvest = Pattern.compile(replace_with.getPre());
					Matcher aliasHarvestMatcher = aliasHarvest.matcher("");
					captureMap.clear();
					for(int i=index+1;i<=(aliasHarvestMatcher.groupCount()+index);i++) {
						
						captureMap.put(Integer.toString(i-index), alias_replacer.group(i));
					}
					
					ToastResponder t = new ToastResponder();
					String finalString = t.translate(replace_with.getPost(), captureMap);
					
					if(finalString.startsWith(scriptBlock)) {
						//if it didn't compile then we send as normal, if it did it gets eaten, but in this case eaten means "return the same caller string
						//so that the visual string sent back to the
						this.runLuaString(finalString.substring(scriptBlock.length(),finalString.length()));
					} else {
						//alias_replacer.appendReplacement(replaced, new String(input));
						alias_replacer.appendReplacement(replaced, replace_with.getPost());
					}
					doTail = false;
					
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
						int idx = -1;
						for(int i=1;i<=alias_recursive.groupCount();i++) {
							if(alias_recursive.group(i) != null) {
								idx = i;
								i=alias_recursive.groupCount();
							}
						}
						//String str = alias_replacer.group(0);
						AliasData replace_with = aliasMap.get(idx);
						//AliasData replace_with = getSettings().getAliases().get(alias_recursive.group(0));
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
				retval = replaced.toString().getBytes(mEncoding);
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
			replaced.setLength(0);
			
			return retval;
		} else {
			return input;
		}
	}

	private Timer CONNECTION_TIMER;
	
	private class CustomTimerTask extends java.util.TimerTask {

		private String name;
		private long startTime;
		public CustomTimerTask(String name) {
			this.name = name;
			startTime = SystemClock.elapsedRealtime();
		}
		
		@Override
		public void run() {
			innerHandler.sendMessage(innerHandler.obtainMessage(100,name));
			TimerData d = getSettings().getTimers().get(name);
			if(d != null) {
				if(!d.isRepeat()) {
					timerTasks.remove(d.getName());
				} else {
					CustomTimerTask t = timerTasks.get(name);
					t.setStartTime(SystemClock.elapsedRealtime());
				}
			}
			
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}
		
	}
	
	private void DoTimerResponders(String ordinal) {
		//synchronized(the_settings) {
			
			//just a precaution, 
			if(innerHandler == null) {
				return; //responders need the handler, and will choke on null.
			}
			
			if(!getSettings().getTimers().containsKey(ordinal)) {
				return; // no ordinal
			}
			
			TimerData data = getSettings().getTimers().get(ordinal);
			if(data == null) {
				return; //this shoudn't happen. means there is a null entry in the map.
			}
			
			//hasListener = isWindowShowing();
			for(TriggerResponder responder : data.getResponders()) {
				try {
					responder.doResponse(mContext,null,0,null,null,0,0,"",(Object)getSettings().getTimers().get(ordinal), parent.getDisplayName(),parent.getHostName(),parent.getPort(), StellarService.getNotificationId(), parent.isWindowShowing(), mHandler,captureMap,L,Plugin.this.getSettings().getTimers().get(ordinal).getName(),mEncoding);
				} catch (IteratorModifiedException e) {
					// won't ever get here because gag/replace actions can't be applied to timers.
				}
				//service.
				//responder.doResponse(parent.getContext(),null,null,null,null, parent.getDisplayName(), StellarService.getNotificationId(), parent.isWindowShowing(), mHandler, null,L,data.getName());
			}
			
			if(data.isRepeat()) {
				stopTimer(ordinal);
				startTimer(ordinal);
			} else {
				stopTimer(ordinal);
			}
		//}
	}
	
	//public void initTimers() {
		
	//}
	
	public void startTimer(String key) {
		TimerData d = getSettings().getTimers().get(key);
		if(d == null) {
			return;
		}
		if(timerTasks.containsKey(d.getName())) {
			//already playing.
			return;
		}
		
		if(d.isPlaying()) {
			//already playing
		} else {
			if(d.getRemainingTime() != d.getSeconds()) {
				CustomTimerTask task = new CustomTimerTask(d.getName());
				long startTime = SystemClock.elapsedRealtime() - ((d.getSeconds() - d.getRemainingTime())*1000);
				if(d.isRepeat()) {
					d.setStartTime(startTime);
					CONNECTION_TIMER.schedule(task, d.getRemainingTime()*1000, d.getSeconds()*1000);
				} else {
					d.setStartTime(startTime);
					CONNECTION_TIMER.schedule(task, d.getRemainingTime()*1000);
				}
				
				timerTasks.put(d.getName(), task);
			} else {
				CustomTimerTask task = new CustomTimerTask(d.getName());
				long startTime = SystemClock.elapsedRealtime();
				if(d.isRepeat()) {
					d.setStartTime(startTime);
					CONNECTION_TIMER.schedule(task, d.getSeconds()*1000, d.getSeconds()*1000);
				} else {
					d.setStartTime(startTime);
					CONNECTION_TIMER.schedule(task, d.getSeconds()*1000);
				}
				timerTasks.put(d.getName(), task);
			}
			
			d.setPlaying(true);
		}
		
	}
	
	public void stopTimer(String key) {
		CustomTimerTask task = timerTasks.get(key);
		if(task != null) {
			task.cancel();
			timerTasks.remove(key);
		}
		TimerData d = getSettings().getTimers().get(key);
		if(d != null) {
			d.setRemainingTime(d.getSeconds());
			d.setPlaying(false);
		}
	}
	
	public void pauseTimer(String key) {
		CustomTimerTask task = timerTasks.get(key);
		
		if(task != null) {
			task.cancel();
			timerTasks.remove(key);
		} else {
			return;
		}
		
		long taskStartTime = task.getStartTime();
		
		TimerData d = getSettings().getTimers().get(key);
		if(d != null) {
			//calculate the remaining seconds.
			long now = SystemClock.elapsedRealtime();
			int elapsed = d.getSeconds() - (int) Math.floor((now - taskStartTime)/1000);
			d.setRemainingTime(elapsed);
			d.setPlaying(false);
		}
		
	}
	
	public void resetTimer(String obj) {
		CustomTimerTask task = timerTasks.get(obj);
		boolean running = false;
		if(task != null) {
			//was running, need to restart
			//task.cancel();
			//timerTasks.remove(obj);
			running = true;
		}
		
		if(!running) {
			stopTimer(obj);
		} else {
			stopTimer(obj);
			startTimer(obj);
		}
	}
	
	public void updateTimerProgress() {
		for(String key : timerTasks.keySet()) {
			CustomTimerTask t = timerTasks.get(key);
			long taskStart = t.getStartTime();
			long now = SystemClock.elapsedRealtime();
			int elapsed = (int) Math.floor((now - taskStart)/1000);
			TimerData d = getSettings().getTimers().get(key);
			if(d != null) {
				d.setRemainingTime(d.getSeconds() - elapsed);
			}
		}
	}
	
	public void updateBooleanSetting(String key,boolean value) {
		settings.getOptions().updateBoolean(key,value);
		settings.setDirty(true);
	}
	
	public void updateIntegerSetting(String key,int value) {
		settings.getOptions().updateInteger(key,value);
		settings.setDirty(true);
	}
	
	public void updateFloatSetting(String key,float value) {
		settings.getOptions().updateFloat(key,value);
		settings.setDirty(true);
	}
	
	public void updateStringSetting(String key,String value) {
		settings.getOptions().updateString(key,value);
		settings.setDirty(true);
	}
	
	public void setEncoding(String encoding) {
		this.mEncoding = encoding;
	}

	
/*! \page entry_points Lua State Entry Points
 * \section service Background Service Entry Points
 * \subsection OnBackgroundStartup OnBackgroundStartup
 * Called when all plugins have been parsed and loaded, but before the connection to the server is initiated.
 * 
 * \param none
 */
	public void doBackgroundStartup() {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("OnBackgroundStartup");
		if(L.getLuaObject(-1).isFunction()) {
			int ret = L.pcall(0, 1, -2);
			if(ret != 0) {
				displayLuaError("Error in OnBackgroundStartup:"+L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
		
		checkStack("OnBackgroundStartup");
	}

/*! \page entry_points
 * \subsection OnXmlExport OnXmlExport
 * When the BlowTorch core has initatied a settings serialization (saves the settings) this will be called to notify the plugin that it needs to serialize any data that it needs to, and provides an android.xml.XMLSerializer that is set up to be either to the main settings wad or the external plugin's descriptor file.
 * 
 * \param out \c android.xml.XmlSerialzer represent the output serialzer object.
 * 
 * \note Please check the documentation of the android java class or the examples for saving data for details of what the body of this function should look like. 
 */

	public void scriptXmlExport(XmlSerializer out) {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("OnXmlExport");
		if(L.getLuaObject(-1).isFunction()) {
			L.pushJavaObject(out);
			int retval = L.pcall(1, 1, -3);
			if(retval != 0) {
				displayLuaError("Plugin: "+this.getName()+" OnXmlExport() Error:" + L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
		checkStack("OnXmlExport");
		
	}
	
	private ArrayList<TriggerData> sortedTriggers = null;
	public void sortTriggers() {
		if(this.settings.getTriggers().size() == 0) return;
		sortedTriggers = new ArrayList<TriggerData>(this.settings.getTriggers().values());
		Collections.sort(sortedTriggers,new Comparator() {

			
			

			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				TriggerData a = (TriggerData)arg0;
				TriggerData b = (TriggerData)arg1;
				//if(a.getSequence() == 5 || b.getSequence() == 5) {
					//Log.e("COMP","STOP HERE");
				//}
				if(a.getSequence() > b.getSequence()) return 1;
				if(a.getSequence() < b.getSequence()) return -1;
				
				return 0;
			}
			
		});
	}
	
	public ArrayList<TriggerData> getSortedTriggers() {
		if(sortedTriggers == null) { sortTriggers(); }
		return sortedTriggers;
	}
	
	public void buildTriggerSystem() {
		//start with the global settings.
		long start = System.currentTimeMillis();
		sortedTriggerMap = new HashMap<Integer,TriggerData>();
		//triggerPluginMap = new HashMap<Integer,Plugin>();
		int working = 1;
		triggerBuilder.setLength(0);
		boolean addseparator = false;
		ArrayList<TriggerData> tmp = sortedTriggers;
		if(tmp == null) {
			sortTriggers();
			tmp = sortedTriggers;
			if(tmp == null || tmp.size() == 0) return;
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
						sortedTriggerMap.put(working, t);
						//triggerPluginMap.put(working, the_settings);
						working += t.getMatcher().groupCount()+1;
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
		//Log.e("TRIGGERS","TIMEPROFILE "+getSettings().getName()+" trigger system took " + delta + " millis to build.");
	}
	
	StringBuilder triggerBuilder = new StringBuilder();
	String massiveTriggerString = null;
	Pattern massivePattern = null;
	Matcher massiveMatcher = null;
	HashMap<Integer,TriggerData> sortedTriggerMap = null;
	//HashMap<Integer,Plugin> triggerPluginMap = null;

/*! \page entry_points
 * \subsection OnOptionsChanged OnOptionsChanged
 * This function is called whenever a plugin defied option has changed through the user activating the options menu UI.
 * 
 * \param key \c string the key value of the option that changed
 * \param value \c string the new value of the option
 * 
 * \note There are a few deomnstrations on how to use this function in the button window and chat window plugins.
 */
	
	@Override
	public void updateSetting(String key, String value) {
		if(L != null) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			
			L.getGlobal("OnOptionChanged");
			if(L.getLuaObject(-1).isFunction()) {
				L.pushString(key);
				L.pushString(value);
				int ret = L.pcall(2, 1, -4);
				if(ret != 0) {
					displayLuaError("Error in OnOptionChanged:"+L.getLuaObject(-1).getString());
					
				} else {
					L.pop(2);
				}
			} else {
				L.pop(2);
			}
			
			checkStack("OnOptionChanged");
		}
	}

	public void pushOptionsToLua() {
		dumpOption(this.getSettings().getOptions());
	}
	
	private void dumpOption(SettingsGroup group) {
		ArrayList<Option> options = group.getOptions();
		if(!this.getSettings().getName().equals("button_window")) {
			long foo = System.currentTimeMillis();
		}
		for(Option o : options) {
			if(o instanceof SettingsGroup) {
				dumpOption((SettingsGroup)o);
			} else {
				BaseOption tmp = (BaseOption)o;
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				
				L.getGlobal("OnOptionChanged");
				if(L.getLuaObject(-1).isFunction()) {
					L.pushString(tmp.getKey());
					L.pushString(tmp.getValue().toString());
					int ret = L.pcall(2, 1, -4);
					if(ret != 0) {
						displayLuaError("Error in OnOptionChanged:"+L.getLuaObject(-1).getString());
					} else {
						L.pop(2);
					}
				} else {
					L.pop(2);
				}
				
				checkStack("OnOptionChanged");
			}
		}
	}

	public void setEnabled(boolean enabled) {
		enabled = true;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void markTriggersDirty() {
		parent.setTriggersDirty();
		
	}
	
	private boolean debug = true;
	private void checkStack(String method) {
		int top = L.getTop();
		//Log.e("PLUGIN","checking stack after "+method+" size: "+Integer.toString(top));
	}

	public void callFunction(String function) {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal(function);
		if(L.getLuaObject(-1).isFunction()) {
			//L.pushJavaObject(out);
			int retval = L.pcall(0, 1, -2);
			if(retval != 0) {
				displayLuaError("Plugin: "+this.getName()+" Script callback("+function+") Error:" + L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			displayLuaError("No function named: "+function+" in plugin: "+this.getName());
			L.pop(2);
		}
	}

	public void callFunction(String function, String data) {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal(function);
		if(L.getLuaObject(-1).isFunction()) {
			//L.pushJavaObject(out);
			L.pushString(data);
			int retval = L.pcall(1, 1, -2);
			if(retval != 0) {
				displayLuaError("Plugin: "+this.getName()+" Script callback("+function+") Error:" + L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			displayLuaError("No function named: "+function+" in plugin: "+this.getName());
			L.pop(2);
		}
	}

	public boolean checkPluginSupports(String function) {
		if(L != null) {
			L.getGlobal(function);
			if(L.isFunction(-1)) {
				L.pop(1);
				return true;
			} else {
				L.pop(1);
				return false;
			}
		}
		
		return false;
	}
	
	public boolean runLuaString(String str) {
		//boolean ret = false;
		if(L != null) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			
			int ret = L.LloadString(str);
			if(ret != 0) {
				//invalid lua, no dice for you
				displayLuaError(L.getLuaObject(-1).getString());
				return false;
			}
			
			ret = L.pcall(0, 1, -2);
			if(ret != 0) {
				displayLuaError(L.getLuaObject(-1).getString());
				return true;
			} else {
				return true;
			}
			
		}
		return false;
	}

	public String getScriptBlock() {
		return scriptBlock;
	}

	public void setScriptBlock(String scriptBlock) {
		this.scriptBlock = scriptBlock;
	}


	
	
	
}
