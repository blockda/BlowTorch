package com.happygoatstudios.bt.launcher;

import java.util.HashMap;

import org.xml.sax.Attributes;

import android.content.Context;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import com.happygoatstudios.bt.settings.BaseParser;

public class LauncherSAXParser extends BaseParser {
	
	final MudConnection current_item = new MudConnection();
	
	public LauncherSAXParser(String location, Context context) {
		super(location, context);
		
	}
	
	public LauncherSettings load() {
		
		final LauncherSettings tmp = new LauncherSettings();
		
		RootElement root = new RootElement("root");
		Element launcher = root.getChild(BaseParser.TAG_LAUNCHER);
		Element item = launcher.getChild(BaseParser.TAG_ITEM);
		
		final HashMap<String,MudConnection> items_read = new HashMap<String,MudConnection>();
		
		launcher.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				tmp.setCurrentVersion( (a.getValue("",BaseParser.ATTR_VERSION) == null) ? "v1.0.4" : a.getValue("",BaseParser.ATTR_VERSION));
			}
		});
		
		item.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				current_item.setDisplayName( (a.getValue("",BaseParser.ATTR_NAME) == null) ? "Mud" : a.getValue("",BaseParser.ATTR_NAME));
				current_item.setHostName((a.getValue("",BaseParser.ATTR_HOST) == null) ? "host not set" : a.getValue("",BaseParser.ATTR_HOST));
				current_item.setPortString((a.getValue("",BaseParser.ATTR_PORT) == null) ? "4002" : a.getValue("",BaseParser.ATTR_PORT));
				current_item.setLastPlayed((a.getValue("",BaseParser.ATTR_DATEPLAYED) == null) ? "11-25-2010 11:53am" : a.getValue("",BaseParser.ATTR_DATEPLAYED));
				items_read.put(current_item.getDisplayName(), current_item.copy());
			}
			
		});
		
		launcher.setEndElementListener(new EndElementListener() {

			public void end() {
				tmp.setList(items_read);
			}
			
		});
		
		try {
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return tmp;
	}

}
