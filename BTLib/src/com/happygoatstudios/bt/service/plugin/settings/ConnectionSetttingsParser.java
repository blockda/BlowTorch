package com.happygoatstudios.bt.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.keplerproject.luajava.LuaException;
import org.xml.sax.SAXException;

import com.happygoatstudios.bt.service.plugin.ConnectionSettingsPlugin;
import com.happygoatstudios.bt.service.plugin.Plugin;

import android.content.Context;
import android.os.Handler;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.util.Log;

public class ConnectionSetttingsParser extends PluginParser {

	ConnectionSettingsPlugin tmp = null;
	public ConnectionSetttingsParser(String location, Context context,
			ArrayList<Plugin> plugins, Handler serviceHandler) {
		super(location, context, plugins, serviceHandler);
		// TODO Auto-generated constructor stub
	}

	//overrided for awesome sake.
	protected void attatchListeners(RootElement root) {
		Element window = root.getChild("window");
		window.setEndElementListener(new EndElementListener() {

			public void end() {
				Log.e("XMLPARSE","successfuly encountered a window element.");
			}
			
		});
		
		super.attatchListeners(root);
	}
	
	public ArrayList<Plugin> load() {
		ArrayList<Plugin> result = new ArrayList<Plugin>();
		try {
			tmp = new ConnectionSettingsPlugin(serviceHandler);
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
		
		result.add(0, tmp);
		
		return result;
	}

}
