package com.happygoatstudios.bt.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerParser;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.trigger.TriggerParser;
import com.happygoatstudios.bt.alias.AliasParser;

import android.content.Context;
import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.sax.TextElementListener;
import android.util.Log;
import android.util.Xml;

public class PluginParser extends BasePluginParser {

	public PluginParser(String location, Context context) {
		super(location, context);
		// TODO Auto-generated constructor stub
	}
	
	final TimerData current_timer = new TimerData();
	final TriggerData current_trigger = new TriggerData();
	final AliasData current_alias = new AliasData();
	final String current_script_body = new String();
	String current_script_name = new String();
	
	public PluginSettings load() throws FileNotFoundException, IOException, SAXException {
		final PluginSettings tmp = new PluginSettings();
		RootElement root = new RootElement("plugins");
		Element plugin = root.getChild(BasePluginParser.TAG_PLUGIN);
		Element aliases = plugin.getChild(BasePluginParser.TAG_ALIASES);
		Element triggers = plugin.getChild(BasePluginParser.TAG_TRIGGERS);
		Element timers = plugin.getChild(BasePluginParser.TAG_TIMERS);
		Element scripts = plugin.getChild(BasePluginParser.TAG_SCRIPT);
		//Element alias = aliases.getChild(BasePluginParser.TAG_ALIAS);
		AliasParser.registerListeners(aliases, tmp, current_alias);
		
		//Element trigger = triggers.getChild(BasePluginParser.TAG_TRIGGER);
		TriggerParser.registerListeners(triggers, tmp, new TriggerData(),current_trigger,current_timer);
		
		//Element timer = timers.getChild(BasePluginParser.TAG_TIMER);
		TimerParser.registerListeners(timers, tmp, new TimerData(), current_trigger, current_timer);
		
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
			}
			
		});
		
		Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		
		return tmp;
	}
	
	
	
	
}
