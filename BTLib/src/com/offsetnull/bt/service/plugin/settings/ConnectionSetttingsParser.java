package com.offsetnull.bt.service.plugin.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;

import org.keplerproject.luajava.LuaException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.alias.AliasParser;
import com.offsetnull.bt.script.ScriptData;
import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.service.WindowToken;
import com.offsetnull.bt.service.WindowTokenParser;
import com.offsetnull.bt.service.function.SpecialCommand;
import com.offsetnull.bt.service.plugin.ConnectionSettingsPlugin;
import com.offsetnull.bt.service.plugin.Plugin;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.speedwalk.DirectionData;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.timer.TimerParser;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.trigger.TriggerParser;

import android.content.Context;
import android.os.Handler;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.sax.TextElementListener;
import android.util.Log;
import android.util.Xml;

public class ConnectionSetttingsParser extends PluginParser {

	public enum OPTION_KEY {
		encoding,
		orientation,
		screen_on,
		fullscreen,
		fullscreen_editor,
		use_suggestions,
		keep_last,
		compatibility_mode,
		local_echo,
		process_system_commands,
		process_semicolon,
		echo_alias_updates,
		keep_wifi_alive,
		auto_reconnect,
		auto_reconnect_limit,
		cull_extraneous_color,
		debug_telnet,
		bell_vibrate,
		bell_notification,
		bell_display, use_gmcp, gmcp_supports
	}
	
	ConnectionSettingsPlugin settings = null;
	public ConnectionSetttingsParser(String location, Context context,
			ArrayList<Plugin> plugins, Handler serviceHandler,Connection parent) {
		super(location, null,context, plugins, serviceHandler,parent);
		// TODO Auto-generated constructor stub
		type = TYPE.INTERNAL;
	}

	//overrided for awesome sake.
	protected void attatchListeners(RootElement root) {
		Element windows = root.getChild("windows");
		Element window = windows.getChild("window");
		/*window.setEndElementListener(new EndElementListener() {

			public void end() {
				Log.e("XMLPARSE","successfuly encountered a window element.");
			}
			
		});*/
		
		
		//Element aliases = root.getChild(BasePluginParser.TAG_ALIASES);
		//Element triggers = root.getChild(BasePluginParser.TAG_TRIGGERS);
		//Element timers = root.getChild(BasePluginParser.TAG_TIMERS);
		Element plugins = root.getChild("plugins");
		
		Element link = plugins.getChild("link");
		///Element timer = timers.getChild("timer");
		//Element trigger = triggers.getChild("trigger");
		//Element alias = aliases.getChild("alias");
		Element script = root.getChild("script");
		Element triggers = root.getChild("triggers");
		Element directions = root.getChild("directions");
		Element dirEntries = directions.getChild("entry");
		
		//do our attatch listener dance.
		TriggerParser.registerListeners(triggers, GLOBAL_HANDLER, new TriggerData(), current_trigger, current_timer);
		AliasParser.registerListeners(root, GLOBAL_HANDLER, current_alias);
		TimerParser.registerListeners(root, GLOBAL_HANDLER, new TimerData(), current_trigger, current_timer);
		WindowTokenParser.registerListeners(window, current_window, GLOBAL_HANDLER);
		script.setTextElementListener(new TextElementListener() {

			public void start(Attributes a) {
				current_script_name = a.getValue("",BasePluginParser.ATTR_NAME);
				if(a.getValue("","execute") != null) {
					if(a.getValue("","execute").equals("true")) {
						current_script_execute = true;
					} else {
						current_script_execute = false;
					}
				} else {
					current_script_execute = false;
				}
			}

			public void end(String body) {
				//Log.e("SCRIPT","SCRIPT BODY:\n"+body);
				
				if(current_script_name == null) {
					Random r = new Random();
					r.setSeed(System.currentTimeMillis());
					int rand = r.nextInt();
					
					current_script_name = Integer.toHexString(rand).toUpperCase();
					
				}
				//current_script_body = body;
				ScriptData d = new ScriptData();
				d.setName(current_script_name);
				d.setData(body);
				d.setExecute(current_script_execute);
				settings.getSettings().getScripts().put(current_script_name, d);
			}
			
		});
		
		dirEntries.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes a) {
				DirectionData d = new DirectionData();
				d.setDirection(a.getValue("","dir"));
				d.setCommand(a.getValue("","cmd"));
				
				settings.getDirections().put(d.getDirection(), d);
			}
			
		});
		
		/*plugins.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				Log.e("PARSE","PLUGIN ELEMENT ENCOUNTERED");
			}
			
		});*/
		
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
		
		Element options = root.getChild("options");
		Element option = options.getChild("option");
		option.setTextElementListener(new TextElementListener() {
			String current_key = null;
			@Override
			public void start(Attributes a) {
				current_key = a.getValue("","key");
			}

			@Override
			public void end(String body) {
				if(current_key != null) {
					settings.getSettings().getOptions().setOption(current_key, body);
				}
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

		public void addScript(String name, String body, boolean b) {
			// TODO Auto-generated method stub
			
		}
		
		public void addWindow(String name, WindowToken w) {
			settings.getSettings().getWindows().put(w.getName(), w);
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
	
	public static String outputXML(ConnectionSettingsPlugin p,ArrayList<Plugin> plugins) throws IllegalArgumentException, IllegalStateException, IOException {
		XmlSerializer out = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		//out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		//out.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
		//out.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
		out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		out.setOutput(writer);
		out.startDocument("UTF-8", true);
		out.startTag("", "blowtorch");
		out.attribute("", "xmlversion", "2");
		out.startTag("", "windows");
	
		
		
		for(WindowToken w : p.getSettings().getWindows().values()) {
			WindowTokenParser.saveToXml(out,w);
		}
		
		out.endTag("", "windows");
		
		outputOptions(out,p);
		
		out.startTag("", "triggers");
		for(TriggerData t : p.getSettings().getTriggers().values()) {
			TriggerParser.saveTriggerToXML(out, t);
		}
		out.endTag("", "triggers");
		
		out.startTag("", "aliases");
		for(AliasData a : p.getSettings().getAliases().values()) {
			AliasParser.saveAliasToXML(out, a);
		}
		out.endTag("", "aliases");
		
		out.startTag("","timers");
		for(TimerData t : p.getSettings().getTimers().values()) {
			TimerParser.saveTimerToXML(out, t);
		}
		out.endTag("", "timers");
		
		out.startTag("", "directions");
		for(DirectionData d : p.getDirections().values()) {
			out.startTag("","entry");
			out.attribute("", "dir", d.getDirection());
			out.attribute("", "cmd", d.getCommand());
			out.endTag("","entry");
		}
		out.endTag("", "directions");
		
		for(String key : p.getSettings().getScripts().keySet()) {
			ScriptData d = p.getSettings().getScripts().get(key);
			
			out.startTag("", "script");
			out.attribute("", "name", key);
			if(d.isExecute()) {
				out.attribute("", "execute", "true");
			}
			out.cdsect(d.getData());
			out.endTag("", "script");
		}
		
		out.startTag("","plugins");
		for(String link : p.getLinks()) {
			out.startTag("", "link");
			out.attribute("", "file", link);
			out.endTag("", "link");
		}
		
		//output "internal" plugins.
		for(Plugin plugin : plugins) {
			if(plugin.getStorageType().equals("INTERNAL")) {
				PluginParser.saveToXml(out,plugin);
			}// else {
				
			//}
		}
		
		
		out.endTag("", "plugins");
		out.endTag("", "blowtorch");
		out.endDocument();
		
		//go back through the link list and check if the settings are dirty, if so then save the link.
		/*for(String link : p.getLinks()) {
			for(Plugin plugin : plugins) {
				if(plugin.getStorageType().equals("EXTERNAL") && plugin.getSettings().isDirty() &&) {
					String filename = plugin.getFullPath();
					File file = new File(filename);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(Plugin.outputXML(plugin.getSettings()).getBytes());
					fos.close();
				}
			}
		}*/
		
		return writer.toString();
	}
	
	private static void outputOptions(XmlSerializer out,ConnectionSettingsPlugin p) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", "options");
		SettingsGroup g = p.getSettings().getOptions();
		dumpOptions(out,g);
		out.endTag("", "options");
	}
	
	private static void dumpOptions(XmlSerializer out,SettingsGroup o) throws IllegalArgumentException, IllegalStateException, IOException {
		for(Option tmp : o.getOptions()) {
			if(tmp instanceof SettingsGroup) {
				dumpOptions(out,(SettingsGroup)tmp);
			} else {
				BaseOption opt = (BaseOption)tmp;
				try {
					boolean dooutput = false;
					OPTION_KEY key = OPTION_KEY.valueOf(opt.getKey());
					switch(key) {
					case process_semicolon:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case encoding:
						if(!((String)opt.getValue()).equals("ISO-8859-1")) {
							dooutput = true;
						}
						break;
					case orientation:
						if((Integer)opt.getValue() != 0) {
							dooutput = true;
						}
						break;
					case screen_on:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case fullscreen:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case fullscreen_editor:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case use_suggestions:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case keep_last:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case compatibility_mode:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case local_echo:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case process_system_commands:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case echo_alias_updates:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case keep_wifi_alive:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case cull_extraneous_color:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case debug_telnet:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case use_gmcp:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case gmcp_supports:
						if(!((String)opt.getValue()).equals("\"char 1\"]")) {
							dooutput = true;
						}
						break;
					case bell_vibrate:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case bell_notification:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
						break;
					case bell_display:
						if((Boolean)opt.getValue() != false) {
							dooutput = true;
						}
					case auto_reconnect:
						if((Boolean)opt.getValue() != true) {
							dooutput = true;
						}
						break;
					case auto_reconnect_limit:
						if((Integer)opt.getValue() != 5) {
							dooutput = true;
						}
						break;
					}
					if(dooutput) {
						out.startTag("", "option");
						out.attribute("", "key", opt.getKey());
						out.text(opt.getValue().toString());
						out.endTag("", "option");
					}
				} catch (IllegalArgumentException e){
					
				}
			}
		}

	}

}
