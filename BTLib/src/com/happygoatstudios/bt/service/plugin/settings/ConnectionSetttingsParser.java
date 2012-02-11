package com.happygoatstudios.bt.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.keplerproject.luajava.LuaException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.alias.AliasParser;
import com.happygoatstudios.bt.service.Connection;
import com.happygoatstudios.bt.service.WindowToken;
import com.happygoatstudios.bt.service.plugin.ConnectionSettingsPlugin;
import com.happygoatstudios.bt.service.plugin.Plugin;
import com.happygoatstudios.bt.settings.HyperSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerParser;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.trigger.TriggerParser;

import android.content.Context;
import android.os.Handler;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.sax.TextElementListener;
import android.util.Log;

public class ConnectionSetttingsParser extends PluginParser {

	ConnectionSettingsPlugin settings = null;
	public ConnectionSetttingsParser(String location, Context context,
			ArrayList<Plugin> plugins, Handler serviceHandler,Connection parent) {
		super(location, null,context, plugins, serviceHandler,parent);
		// TODO Auto-generated constructor stub
		type = TYPE.INTERNAL;
	}

	//overrided for awesome sake.
	protected void attatchListeners(RootElement root) {
		Element window = root.getChild("window");
		window.setEndElementListener(new EndElementListener() {

			public void end() {
				Log.e("XMLPARSE","successfuly encountered a window element.");
			}
			
		});
		
		//Element aliases = root.getChild(BasePluginParser.TAG_ALIASES);
		//Element triggers = root.getChild(BasePluginParser.TAG_TRIGGERS);
		//Element timers = root.getChild(BasePluginParser.TAG_TIMERS);
		Element plugins = root.getChild("plugins");
		
		Element link = plugins.getChild("link");
		///Element timer = timers.getChild("timer");
		//Element trigger = triggers.getChild("trigger");
		//Element alias = aliases.getChild("alias");
		Element script = root.getChild("script");
		
		//do our attatch listener dance.
		TriggerParser.registerListeners(root, GLOBAL_HANDLER, new TriggerData(), current_trigger, current_timer);
		AliasParser.registerListeners(root, GLOBAL_HANDLER, current_alias);
		TimerParser.registerListeners(root, GLOBAL_HANDLER, new TimerData(), current_trigger, current_timer);
		
		script.setTextElementListener(new TextElementListener() {

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
				settings.getSettings().getScripts().put(current_script_name, body);
			}
			
		});
		
		plugins.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				Log.e("PARSE","PLUGIN ELEMENT ENCOUNTERED");
			}
			
		});
		
		link.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				
				if(a.getValue("", "file") != null) {
					Log.e("XML","LINK NODE ENCOUNTERED");
					String link = a.getValue("","file");
					settings.getLinks().add(link);
				}
				//s
			}
			
		});
		
		super.attatchListeners(root);
		
		
	}
	
	private class GlobalHandler implements NewItemCallback {

		public void addAlias(String key, AliasData a) {
			// TODO Auto-generated method stub
			settings.getSettings().getAliases().put(key, a.copy());
		}

		public void addTrigger(String key, TriggerData t) {
			settings.getSettings().getTriggers().put(key, t.copy());
			
		}

		public void addTimer(String key, TimerData t) {
			settings.getSettings().getTimers().put(key, t.copy());
		}

		public void addScript(String name, String body) {
			// TODO Auto-generated method stub
			
		}
		
		public void addWindow(String name, WindowToken w) {
			
		}
		
	}
	
	private GlobalHandler GLOBAL_HANDLER = new GlobalHandler();
	
	public ArrayList<Plugin> load(Connection parent) {
		
		ArrayList<Plugin> result = new ArrayList<Plugin>();
		try {
			settings = new ConnectionSettingsPlugin(serviceHandler,parent);
		} catch (LuaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			result = super.load();
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
		
		result.add(0, settings);
		//plugins.addAll(result);
		
		return result;
	}

}
