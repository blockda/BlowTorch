package com.offsetnull.bt.launcher;

import java.io.StringWriter;
import java.util.HashMap;

import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.settings.BaseParser;

import android.util.Xml;

public class LauncherSettings {

	private String currentVersion = "1.0.4";
	
	private HashMap<String,MudConnection> list = new HashMap<String,MudConnection>();
	
	//serialize the list.
	public static String writeXml(LauncherSettings data) {
		XmlSerializer out = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try {
			out.setOutput(writer);
			out.startDocument("UTF-8", true);
			out.startTag("", "root");
			
			
			out.startTag("", BaseParser.TAG_LAUNCHER);
			out.attribute("", BaseParser.ATTR_VERSION, data.getCurrentVersion());
			
			for(MudConnection item : data.getList().values()) {
				out.startTag("", BaseParser.TAG_ITEM);
				out.attribute("", BaseParser.ATTR_NAME, item.getDisplayName());
				out.attribute("", BaseParser.ATTR_HOST, item.getHostName());
				out.attribute("", BaseParser.ATTR_PORT, item.getPortString());
				out.attribute("", BaseParser.ATTR_DATEPLAYED, item.getLastPlayed());
				out.endTag("", BaseParser.TAG_ITEM);
			}
			
			out.endTag("", BaseParser.TAG_LAUNCHER);
			
			out.endTag("", "root");
			
			out.endDocument();
			
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setList(HashMap<String,MudConnection> list) {
		this.list = list;
	}

	public HashMap<String,MudConnection> getList() {
		return list;
	}
	
}
