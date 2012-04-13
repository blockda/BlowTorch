package com.offsetnull.bt.service.plugin.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class QuickPluginParser extends BasePluginParser {

	public QuickPluginParser(String location, Context context) {
		super(location, context);
		// TODO Auto-generated constructor stub
	}
	
	private PluginDescription[] info = null;
	private Stack<PluginDescription> tmp = null;
	
	String current_plugin;
	String current_author;
	String current_description;
	int trigger_count;
	int alias_count;
	int timer_count;
	int script_count;
	int window_count;
	
	public PluginDescription[] load() throws IOException, SAXException {
		tmp = new Stack<PluginDescription>();
		RootElement root = new RootElement("blowtorch");
		Element plugins = root.getChild("plugins");
		Element plugin = plugins.getChild("plugin");
		Element triggers = plugins.getChild("triggers");
		Element trigger = triggers.getChild("trigger");
		Element aliases = plugins.getChild("aliases");
		Element alias = aliases.getChild("alias");
		Element timers = plugins.getChild("timers");
		Element timer = timers.getChild("timer");
		Element script = plugin.getChild("script");
		Element windows = plugin.getChild("windows");
		Element window = windows.getChild("window");
		
		plugin.setElementListener(new ElementListener() {

			@Override
			public void start(Attributes a) {
				trigger_count = 0;
				alias_count = 0;
				timer_count = 0;
				script_count = 0;
				window_count = 0;
				if(a.getValue("","name") == null) {
					current_plugin = "Name not set";
				} else {
					current_plugin = a.getValue("","name");
				}
				
				if(a.getValue("","author") == null) {
					current_author = "Unknown";
				} else {
					current_plugin = a.getValue("","author");
				}
				
				if(a.getValue("","description") == null) {
					current_description = "No description";
				} else {
					current_description = a.getValue("","description");
				}
				
			}

			@Override
			public void end() {
				//make a new plugindescription and put it on the stack.
				PluginDescription desc = new PluginDescription();
				desc.setName(current_plugin);
				desc.setAuthor(current_author);
				desc.setName(current_description);
				desc.setTriggers(trigger_count);
				desc.setAliases(alias_count);
				desc.setTimers(timer_count);
				desc.setScripts(script_count);
				desc.setWindows(window_count);
				
				tmp.push(desc);
			}
			
		});
		
		trigger.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				trigger_count += 1;
			}
			
		});
		
		alias.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				alias_count += 1;
			}
			
		});
		
		timer.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				timer_count += 1;
			}
			
		});
		
		script.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				script_count += 1;
			}
			
		});
		
		window.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				window_count += 1;
			}
			
		});
		
		InputStream in = this.getInputStream();
		
		Xml.parse(in, Xml.Encoding.UTF_8,root.getContentHandler());
		
		//now that we are here.
		if(tmp.size() == 0) {
			return null;
		} else {
			info = new PluginDescription[tmp.size()];
			int index = tmp.size() -1;
			while(tmp.size() > 0) {
				info[index] = tmp.pop();
				index -= 1;
			}
		}
		
		return info;
	}

}
