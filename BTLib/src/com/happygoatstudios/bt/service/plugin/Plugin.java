package com.happygoatstudios.bt.service.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.responder.IteratorModifiedException;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.Connection;
import com.happygoatstudios.bt.service.StellarService;
import com.happygoatstudios.bt.service.WindowToken;
import com.happygoatstudios.bt.service.plugin.function.DrawWindowFunction;
import com.happygoatstudios.bt.service.plugin.function.NoteFunction;
import com.happygoatstudios.bt.service.plugin.function.TriggerEnabledFunction;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.window.TextTree;

public class Plugin {
	//we are a lua plugin.
	//we can give users 
	Matcher colorStripper = StellarService.colordata.matcher("");
	LuaState L = null;
	private PluginSettings settings = null;
	Handler mHandler = null;
	//private String mName = null;
	
	public Plugin(Handler h) throws LuaException {
		setSettings(new PluginSettings());
		mHandler = h;
		L = LuaStateFactory.newLuaState();
		initLua();
	}
	
	public Plugin(PluginSettings settings,Handler h) throws LuaException {
		this.settings = settings;
		mHandler = h;
		L = LuaStateFactory.newLuaState();
		initLua();
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
		MainWindowFunction mwf = new MainWindowFunction(L);
		WindowBufferFunction wbf = new WindowBufferFunction(L);
		wf.register("NewWindow");
		mwf.register("MainWindowSize");
		esf.register("ExecuteScript");
		wbf.register("WindowBuffer");
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

	public void setSettings(PluginSettings settings) {
		this.settings = settings;
		
	}

	public PluginSettings getSettings() {
		return settings;
	}
	private final HashMap<String,String> captureMap = new HashMap<String,String>();
	public void process(TextTree input,StellarService service,boolean windowOpen,Handler pump,String display) {
		List<TriggerData> triggers = new ArrayList<TriggerData>(this.settings.getTriggers().values());
		Collections.sort(triggers,new Comparator() {

			
			/*public int compare(TriggerData a, TriggerData b) {
				// TODO Auto-generated method stub
				if(a.getSequence() < b.getSequence()) {
					return -1;
				} else if(a.getSequence() == b.getSequence()) {
					return 0;
				} else if(a.getSequence() > b.getSequence()){
					return 1;
				}
				
				return 0;
				
			}*/

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
		//sick ass shit in the hiznizouous
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
			for(TriggerData t : triggers) {
				if(t.isEnabled()) {
					String str = TextTree.deColorLine(l).toString();
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
									responder.doResponse(service.getApplicationContext(),input,lineNum,it,l,t.getMatcher(),t, display, StellarService.getNotificationId(), windowOpen, pump,captureMap,L,t.getName());
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
		//return null;
	}
	
	public void initScripts(ArrayList<WindowToken> windows) {
		//for(Script)
		
		
		for(String script : settings.getScripts().keySet()) {
			//Log.e("LUA","ATTEMPTING TO LOAD:" + script + "\n" + settings.getScripts().get(script));
			if(script.equals("global")) {
				int ret =L.LdoString(settings.getScripts().get(script));
				if(ret != 0) {
					Log.e("LUA","PROBLEM LOADING SCRIPT:" + L.getLuaObject(-1).getString());
				}
			}
		}
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
			
			if(pScriptName.isNil()) {
				WindowToken tok = new WindowToken(name,x,y,width,height);
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_NEWWINDOW, tok));
			} else {
				WindowToken tok = new WindowToken(name,x,y,width,height,scriptName,Plugin.this.getSettings().getName());
				mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_NEWWINDOW, tok));
			}
			
			return 0;
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
	
	private class MainWindowFunction extends JavaFunction {

		public MainWindowFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			int x = (int) this.getParam(2).getNumber();
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
			mHandler.sendMessage(msg);
			return 0;
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

	public void shutdown() {
		// TODO Auto-generated method stub
		L.close();
	}
	
	
}
