package com.happygoatstudios.bt.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.service.Connection;
import com.happygoatstudios.bt.service.WindowToken;
import com.happygoatstudios.bt.service.WindowTokenParser;
import com.happygoatstudios.bt.service.plugin.Plugin;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings.PLUGIN_LOCATION;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerParser;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.trigger.TriggerParser;
import com.happygoatstudios.bt.alias.AliasParser;

import android.content.Context;
import android.os.Handler;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.sax.TextElementListener;
import android.util.Log;
import android.util.Xml;

public class PluginParser extends BasePluginParser {

	//chainsaw for more of a "plug-in group" parser.
	
	//LuaState L = null;
	//Plugin p = null;
	
	ArrayList<Plugin> plugins = null;
	PluginSettings tmp = null;
	Handler serviceHandler = null;
	Connection parent = null;
	
	enum TYPE {
		EXTERNAL,
		INTERNAL
	};
	
	protected TYPE type;
	protected String shortName;
	
	public PluginParser(String location,String name, Context context,ArrayList<Plugin> plugins,Handler serviceHandler,Connection parent) {
		super(location, context);
		// TODO Auto-generated constructor stub
		//L = p.getLuaState();
		//this.p = p;
		this.parent = parent;
		shortName = name;
		this.serviceHandler = serviceHandler;
		this.plugins = plugins;
		type = TYPE.EXTERNAL;
	}
	
	final TimerData current_timer = new TimerData();
	final TriggerData current_trigger = new TriggerData();
	final AliasData current_alias = new AliasData();
	final String current_script_body = new String();
	final WindowToken current_window = new WindowToken();
	String current_script_name = new String();
	
	public ArrayList<Plugin> load() throws FileNotFoundException, IOException, SAXException {
		RootElement root = new RootElement("root");
		tmp = new PluginSettings();
		attatchListeners(root);
		
		
		Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		
		//tmp.setPath(path);
		//p.setSettings(tmp);
		//do alternate parsing for plugin data.
		RootElement root2 = new RootElement("root");
		Element data = root2.getChild(PluginParser.TAG_PLUGINS).getChild(PluginParser.TAG_PLUGIN);
		
		//upon encountering.
		//ok, so here is now where bootstrapping happens.
		//TODO: change this to something like "bootstrap" or ""
		for(Plugin p : plugins) {
			//set up the stuff.
			switch(type) {
			case INTERNAL:
				p.setFullPath(null);
				p.setShortName(null);
				break;
			case EXTERNAL:
				p.setFullPath(path);
				p.setShortName(shortName);
				break;
			}
			if(p.getSettings().getWindows().size() > 0) {
				for(WindowToken t : p.getSettings().getWindows().values()) {
					t.setPluginName(p.getName());
				}
			}
			
			if(p.getSettings().getScripts().containsKey("bootstrap")) {
				//run this script.
				LuaState pL = p.getLuaState();
				pL.getGlobal("debug");
				pL.getField(-1, "traceback");
				pL.remove(-2);
				
				String datas = p.getSettings().getScripts().get("bootstrap");
				pL.LloadString(datas);
				
				int ret = p.getLuaState().pcall(0, 1, -2);
				if(ret != 0) {
					Log.e("PLUGIN","Error in Bootstrap:"+pL.getLuaObject(-1).getString());
				} else {
					//bootstrap success.
					//i think i can use the existing traceback, but the pcall has left a nil on the stack
					//L.pop(1);
					pL.getGlobal("debug");
					pL.getField(-1, "traceback");
					pL.remove(-2);
					
					pL.getGlobal("OnPrepareXML");
					pL.pushJavaObject(data);
					int r2 = pL.pcall(1, 1, -3);
					if(r2 != 0) {
						Log.e("PLUGIN","Error in OnPrepareXML"+pL.getLuaObject(-1).getString());
					} else {
						
					}
				}
			}
		}
		
		Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root2.getContentHandler());	
		
		return plugins;
	}
	
	protected void attatchListeners(RootElement root) {
		Element pgroup = root.getChild(BasePluginParser.TAG_PLUGINS);
		
		Element plugin = pgroup.getChild(BasePluginParser.TAG_PLUGIN);
		//Element aliases = plugin.getChild(BasePluginParser.TAG_ALIASES);
		Element triggers = plugin.getChild(BasePluginParser.TAG_TRIGGERS);
		Element timers = plugin.getChild(BasePluginParser.TAG_TIMERS);
		Element scripts = plugin.getChild(BasePluginParser.TAG_SCRIPT);
		Element windows = plugin.getChild("windows");
		Element window = windows.getChild("window");
		//Element alias = aliases.getChild(BasePluginParser.TAG_ALIAS);
		AliasParser.registerListeners(plugin, newItemHandler, current_alias);
		
		//Element trigger = triggers.getChild(BasePluginParser.TAG_TRIGGER);
		TriggerParser.registerListeners(triggers, newItemHandler, new TriggerData(),current_trigger,current_timer);
		
		//Element timer = timers.getChild(BasePluginParser.TAG_TIMER);
		TimerParser.registerListeners(timers, newItemHandler, new TimerData(), current_trigger, current_timer);
		
		WindowTokenParser.registerListeners(window, current_window, newItemHandler);
		
		scripts.setTextElementListener(new TextElementListener() {

			public void start(Attributes a) {
				current_script_name = a.getValue("",BasePluginParser.ATTR_NAME);
			}

			public void end(String body) {
				Log.e("SCRIPT","SCRIPT BODY:\n"+body);
				if(current_script_name == null) {
					Random r = new Random();
					r.setSeed(System.currentTimeMillis());
					int rand = r.nextInt();
					
					current_script_name = Integer.toHexString(rand).toUpperCase();
					
				}
				//current_script_body = body;
				tmp.getScripts().put(current_script_name, body);
			}
			
		});
		
		plugin.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				
				tmp.setName(a.getValue("",BasePluginParser.ATTR_NAME));
				tmp.setAuthor(a.getValue("",BasePluginParser.ATTR_AUTHOR));
				tmp.setId(Integer.parseInt(a.getValue("",BasePluginParser.ATTR_ID)));
				/*if(a.getValue("","location") == null) {
					tmp.setLocationType(PLUGIN_LOCATION.INTERNAL);
				} else {
					if(a.getValue("","location").equals("external")) {
						tmp.setLocationType(PLUGIN_LOCATION.EXTERNAL);
					} else {
						tmp.setLocationType(PLUGIN_LOCATION.INTERNAL);
					}
				}*/
				switch(type) {
				case INTERNAL:
					tmp.setLocationType(PLUGIN_LOCATION.INTERNAL);
					break;
				case EXTERNAL:
					tmp.setLocationType(PLUGIN_LOCATION.EXTERNAL);
					break;
				}
			}
			
		});
		
		plugin.setEndElementListener(new EndElementListener() {

			public void end() {
				//construct the new plugin.
				Plugin p;
				try {
					p = new Plugin(serviceHandler,parent);
					tmp.setPath(path);
					p.setSettings(tmp);
					
					plugins.add(p);
				} catch (LuaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tmp = new PluginSettings();
				
			}
			
		});
		
		
	}
	
	public interface NewItemCallback {
		public void addAlias(String key,AliasData a);
		public void addTrigger(String key,TriggerData t);
		public void addTimer(String key,TimerData t);
		public void addScript(String name,String body);
		public void addWindow(String name,WindowToken w);
	}
	
	protected class NewItemHandler implements NewItemCallback {

		public void addTrigger(String key, TriggerData t) {
			PluginParser.this.tmp.getTriggers().put(key, t);
		}

		public void addTimer(String key, TimerData t) {
			PluginParser.this.tmp.getTimers().put(key, t);
		}

		public void addScript(String name, String body) {
			PluginParser.this.tmp.getScripts().put(name, body);
		}

		public void addAlias(String key, AliasData a) {
			PluginParser.this.tmp.getAliases().put(key, a);
		}
		
		public void addWindow(String key, WindowToken w) {
			PluginParser.this.tmp.getWindows().put(key,w);
		}
		
	}
	
	private NewItemHandler newItemHandler = new NewItemHandler();
	
}
