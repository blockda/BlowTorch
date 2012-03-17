package com.happygoatstudios.bt.service.plugin;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.alias.AliasParser;
import com.happygoatstudios.bt.responder.IteratorModifiedException;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.service.Connection;
import com.happygoatstudios.bt.service.StellarService;
import com.happygoatstudios.bt.service.WindowToken;
import com.happygoatstudios.bt.service.plugin.function.DrawWindowFunction;
import com.happygoatstudios.bt.service.plugin.function.NoteFunction;
import com.happygoatstudios.bt.service.plugin.function.TriggerEnabledFunction;
import com.happygoatstudios.bt.service.plugin.settings.BaseOption;
import com.happygoatstudios.bt.service.plugin.settings.Option;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerParser;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.trigger.TriggerParser;
import com.happygoatstudios.bt.window.TextTree;

public class Plugin {
	//we are a lua plugin.
	//we can give users 
	Matcher colorStripper = StellarService.colordata.matcher("");
	LuaState L = null;
	private PluginSettings settings = null;
	Handler mHandler = null;
	Handler innerHandler = null;
	//private String mName = null;
	private String fullPath;
	private String shortName;
	Connection parent;
	StringBuffer joined_alias = new StringBuffer();
	Pattern alias_replace = Pattern.compile(joined_alias.toString());
	Matcher alias_replacer = alias_replace.matcher("");
	Matcher alias_recursive = alias_replace.matcher("");
	String mEncoding = "ISO-8859-1";
	Pattern whiteSpace = Pattern.compile("\\s");
	HashMap<String,CustomTimerTask> timerTasks = new HashMap<String,CustomTimerTask>();
	
	public Plugin(Handler h,Connection parent) throws LuaException {
		setSettings(new PluginSettings());
		mHandler = h;
		L = LuaStateFactory.newLuaState();
		this.parent = parent;
		initTimers();
		initLua();
		
	}
	
	public Plugin(PluginSettings settings,Handler h,Connection parent) throws LuaException {
		this.settings = settings;
		mHandler = h;
		L = LuaStateFactory.newLuaState();
		this.parent = parent;
		
		initTimers();
		
		initLua();
	}
	
	private void initTimers() {
		innerHandler = new Handler() { 
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case 100:
					DoTimerResponders((String)msg.obj);
					break;
				}
			}
		};
		
		CONNECTION_TIMER = new Timer("blowtorch_"+this.getName()+"_timer",true);
		
		for(TimerData timer : settings.getTimers().values()) {
			if(timer.isPlaying()) {
				CustomTimerTask task = new CustomTimerTask(timer.getName());
				if(timer.isRepeat()) {
					CONNECTION_TIMER.schedule(task, timer.getSeconds()*1000, timer.getSeconds()*1000);
				} else {
					CONNECTION_TIMER.schedule(task, timer.getSeconds()*1000);
				}
				
			}
		}
	}

	private void initLua() throws LuaException {
		//need to set up global functions, it all goes here.
		L.openLibs();
		
		TriggerEnabledFunction tef = new TriggerEnabledFunction(L,this,mHandler);
		tef.register("TriggerEnabled");
		L.pushJavaObject(settings.getTriggers());
		L.setGlobal("triggers");
		
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
		wf.register("NewWindow");
		mwf.register("GetWindowTokenByName");
		esf.register("ExecuteScript");
		wbf.register("WindowBuffer");
		rfc.register("RegisterSpecialCommand");
		df.register("debugPrint");
		wxctf.register("WindowXCallS");
		altwf.register("appendLineToWindow");
		iwtf.register("invalidateWindowText");
		gsf.register("Send_GMCP_Packet");
		SaveSettingsFunction ssfun = new SaveSettingsFunction(L);
		ssfun.register("saveSettings");
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
	public void process(TextTree input,StellarService service,boolean windowOpen,Handler pump,String display) {
		//if(this.settings.getName().equals("map_miniwindow")) {
			//inspection
		//<String,TriggerData> triggers = this.settings.getTriggers();
		//	Collection<TriggerData> c = triggers.values();
		//	c.contains("foo");
		//}
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
										responder.doResponse(service.getApplicationContext(),input,lineNum,it,l,0,0,"",t, display, StellarService.getNotificationId(), windowOpen, pump,captureMap,L,t.getName(),mEncoding);
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
	
									responder.doResponse(service.getApplicationContext(),input,lineNum,null,l,start,end,matched,t, display, StellarService.getNotificationId(), windowOpen, pump,captureMap,L,t.getName(),mEncoding);
								
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
	}
	
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
				tok = new WindowToken(name,null,null);
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_NEWWINDOW, tok));
			} else {
				tok = new WindowToken(name,scriptName,Plugin.this.getSettings().getName());
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_NEWWINDOW, tok));
			}
			
			L.pushJavaObject(tok);
			
			return 1;
		}
		
	}
	
	private class ExecuteScriptFunction extends JavaFunction {

		public ExecuteScriptFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			String pName = this.getParam(2).getString();
			String body = Plugin.this.getSettings().getScripts().get(pName);
			if(body != null) {
				L.LdoString(Plugin.this.getSettings().getScripts().get(pName));
			} else {
				//error
				//L.pushString(bytes)
				//L.error();
			}
			return 0;
		}
		
	}
	
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
			Message msg = mHandler.obtainMessage(Connection.MESSAGE_WINDOWXCALLS,foo.getString());
			
			msg.getData().putString("TOKEN",token);
			msg.getData().putString("FUNCTION", function);
			
			mHandler.sendMessage(msg);
			// TODO Auto-generated method stub
			return 0;
		}
		
		public HashMap<String,Object> dumpTable(String tablePath,int idx) {
			
			HashMap<String,Object> tmp = new HashMap<String,Object>();
			int counter = 1;
			L.pushNil();
			while(L.next(idx) != 0) {
				//String id = L.toString(-2);
				String id = null;
				if(L.isNumber(-2)) {
					id = Integer.toString(counter);
					counter++;
				} else if(L.isString(-2)) {
					id = L.toString(-2);
				}
				LuaObject l = L.getLuaObject(-1);
				if(l.isTable()) {
					//need to dump more tables
					tmp.put(id, dumpTable(tablePath+"."+id,L.getTop()));
					//Log.e("PLUGIN","TABLE RECURSIVE DUMP:"+L.getTop()+":"+(L.getLuaObject(L.getTop()).toString()));
				
				} else {
					//Log.e("PLUGIN","WXCALLT:"+tablePath+"|"+id+"<==>"+l.getString());
					tmp.put(id, l.getString());
				}
				
				L.pop(1);
			}
			
			//L.pop(1);
			return tmp;
		}
		
		
	}
	
	private class AppendLineToWindowFunction extends JavaFunction {

		public AppendLineToWindowFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			//the only arguments should be a TextTree.Line copied from the one passed to it from a trigger script action
			//and the window id to send it to.
			TextTree.Line line = (TextTree.Line)(this.getParam(3)).getObject();
			String windowId = (this.getParam(2).getString());
			
			//now abuse the lineToWindow handler from the replace action to ferry this bad boy across
			Message m = mHandler.obtainMessage(Connection.MESSAGE_LINETOWINDOW,line);
			m.getData().putString("TARGET", windowId);
			mHandler.sendMessage(m);
			return 0;
		}
		
	}
	
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

	public void shutdown() {
		// TODO Auto-generated method stub
		//L.close();
		L = null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return settings.getName();
	}

	public void execute(String callback,String args) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		L.pushString(args);
		
		int ret = L.pcall(1, 1, -3);
		
		if(ret != 0) {
			Log.e("PLUGIN","Error calling function callback:"+settings.getName()+"("+callback+"):"+L.getLuaObject(-1).getString());
		} else {
			Log.e("PLUGIN","Successfuly called plugin function:"+settings.getName()+"("+callback+")");
		}
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
				Log.e("PLUGIN","PluginXCallS Error:" + L.getLuaObject(-1).getString());
			} else {
				//success
			}
		} else {
			//error
		}
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
	
	public void outputXMLInternal(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
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
	}
	
	private void dumpLuaData(XmlSerializer out) {
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
				Log.e("PLUGIN","Plugin SaveXML Error:" + L.getLuaObject(-1).getString());
			} else {
				//success
			}
			
		} else {
			L.pop(2);
		}
	}
	
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
				out.startTag("", "script");
				out.attribute("", "name", script);
				//out.text(settings.getScripts().get(script));
				out.cdsect(settings.getScripts().get(script));
				
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
				Log.e("PLUGIN","Error calling gmcp callback:" + callback + " error:\n"+L.getLuaObject(-1).getString());
			} else {
				//success.
			}
		} else {
			//callback not defined.
		}
	}
	
	public void addTrigger(TriggerData data) {
		this.getSettings().getTriggers().put(data.getName(), data);
		parent.buildTriggerSystem();
	}

	public void updateTrigger(TriggerData from, TriggerData to) {
		TriggerData tmp = this.getSettings().getTriggers().remove(from.getName());
		this.getSettings().getTriggers().put(to.getName(),to);
		tmp = null;
		this.sortTriggers();
		parent.buildTriggerSystem();
	}
	
	public void buildAliases() {
		joined_alias.setLength(0);
		
		//Object[] a = the_settings.getAliases().keySet().toArray();
		Object[] a = getSettings().getAliases().values().toArray();
		
		
		String prefix = "\\b";
		String suffix = "\\b";
		//StringBuffer joined_alias = new StringBuffer();
		if(a.length > 0) {
			int j=0;
			for(int i=0;i<a.length;i++) {
				if(((AliasData)a[i]).isEnabled()) {
					if(((AliasData)a[i]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
					if(((AliasData)a[i]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
					joined_alias.append("("+prefix+((AliasData)a[i]).getPre()+suffix+")");
					j=i;
					i=a.length;
					
				}
			}
			for(int i=j;i<a.length;i++) {
				if(((AliasData)a[i]).isEnabled()) {
					if(((AliasData)a[i]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
					if(((AliasData)a[i]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
					joined_alias.append("|");
					joined_alias.append("("+prefix+((AliasData)a[i]).getPre()+suffix+")");
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
				
				AliasData replace_with = getSettings().getAliases().get(alias_replacer.group(0));
				//do special replace if only ^ is matched.
				if(replace_with.getPre().startsWith("^") && !replace_with.getPre().endsWith("$")) {
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
						AliasData replace_with = getSettings().getAliases().get(alias_recursive.group(0));
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
					responder.doResponse(parent.getContext(),null,0,null,null,0,0,"",(Object)getSettings().getTimers().get(ordinal), parent.getDisplayName(), StellarService.getNotificationId(), parent.isWindowShowing(), mHandler,captureMap,L,Plugin.this.getSettings().getTimers().get(ordinal).getName(),mEncoding);
				} catch (IteratorModifiedException e) {
					// won't ever get here because gag/replace actions can't be applied to timers.
				}
				//service.
				//responder.doResponse(parent.getContext(),null,null,null,null, parent.getDisplayName(), StellarService.getNotificationId(), parent.isWindowShowing(), mHandler, null,L,data.getName());
			}
		//}
	}
	
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
				if(d.isRepeat()) {
					CONNECTION_TIMER.schedule(task, d.getRemainingTime()*1000, d.getSeconds()*1000);
				} else {
					CONNECTION_TIMER.schedule(task, d.getRemainingTime()*1000);
				}
				
				timerTasks.put(d.getName(), task);
			} else {
				CustomTimerTask task = new CustomTimerTask(d.getName());
				if(d.isRepeat()) {
					CONNECTION_TIMER.schedule(task, d.getSeconds()*1000, d.getSeconds()*1000);
				} else {
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
		long taskStartTime = task.getStartTime();
		if(task != null) {
			task.cancel();
			timerTasks.remove(key);
		}
		
		TimerData d = getSettings().getTimers().get(key);
		if(d != null) {
			//calculate the remaining seconds.
			long now = SystemClock.elapsedRealtime();
			int elapsed = d.getSeconds() - (int) Math.floor((now - taskStartTime)/1000);
			d.setRemainingTime(elapsed);
			d.setPlaying(false);
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
	}
	
	public void updateIntegerSetting(String key,int value) {
		settings.getOptions().updateInteger(key,value);
		//o.setValue(value);
	}
	
	public void updateFloatSetting(String key,float value) {
		settings.getOptions().updateFloat(key,value);
		//o.setValue(value);
	}
	
	public void updateStringSetting(String key,String value) {
		settings.getOptions().updateString(key,value);
		//o.setValue(value);
	}
	
	public void setEncoding(String encoding) {
		this.mEncoding = encoding;
	}

	public void doBackgroundStartup() {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("OnBackgroundStartup");
		if(L.getLuaObject(-1).isFunction()) {
			int ret = L.pcall(0, 1, -2);
			if(ret != 0) {
				Log.e("LUA","Error in OnBackgroundStartup:"+L.getLuaObject(-1).getString());
			}
		}
	}

	public void scriptXmlExport(XmlSerializer out) {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("OnXmlExport");
		if(L.getLuaObject(-1).isFunction()) {
			L.pushJavaObject(out);
			int retval = L.pcall(1, 1, -3);
			if(retval != 0) {
				Log.e("LUA","Plugin: "+this.getName()+" OnXmlExport() Error:" + L.getLuaObject(-1).getString());
			}
		}
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
}
