package com.offsetnull.bt.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class VersionProbeParser extends BasePluginParser {

	public VersionProbeParser(String location, Context context) {
		super(location, context);
		// TODO Auto-generated constructor stub
	}

	int version = -1;
	boolean isLegacy = false;
	public boolean isLegacy() throws FileNotFoundException, IOException, SAXException {
		RootElement root = new RootElement("root");
		
		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				VersionProbeParser.this.isLegacy = true;
			}
			
		});
		
		try {
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch(Exception e) {
			//catch any bad xml eccetera
		}
		return isLegacy;
	}
	
	public int getVersionNumber() throws FileNotFoundException, IOException, SAXException {
		RootElement root = new RootElement("blowtorch");
		
		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes a) {
				if(a.getValue("","xmlversion") != null) {
					try {
						VersionProbeParser.this.version = Integer.parseInt(a.getValue("","xmlversion"));
					} catch(NumberFormatException e) {
						
					}
				}
			}
			
		});
		
		Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		
		return version;
	}
	
}
