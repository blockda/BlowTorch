package com.offsetnull.bt.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
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
				Log.e("XMLPARSE","FOUND LEGACY ROOT NODE IN VERSION PROBE LEGACY TEST");
				VersionProbeParser.this.isLegacy = true;
			}
			
		});
		
		try {
			Log.e("XMLPARSE","ATTEMPTING VERSION PROBE LEGACY TEST");
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch(Exception e) {
			//catch any bad xml eccetera
			Log.e("XMLPARSE","MISSING LEGACY ROOT NODE IN VERSION PROBE LEGACY TEST");
			isLegacy = false;
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
						Log.e("XMLPARSE","FOUND APPROPRIATE BLOWTORCH ROOT NODE IN V2 SETTINGS FILE - FOUND VERSION "+VersionProbeParser.this.version);
					} catch(NumberFormatException e) {
						Log.e("XMLPARSE","DID NOT FIND APPROPRIATE BLOWTORCH ROOT NOTE VERSION NUMBER IN V2 SETTINGS FILE");
						
					}
				}
			}
			
		});
		Log.e("XMLPARSE","ATTEMPTING VERSION PROBE PARSE OF V2 SETTINGS FORMAT");
		Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		
		return version;
	}
	
}
