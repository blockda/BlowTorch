package com.offsetnull.bt.service;

import org.xml.sax.Attributes;

import com.offsetnull.bt.service.plugin.settings.PluginParser.NewItemCallback;

import android.sax.ElementListener;
import android.sax.StartElementListener;

public class WindowTokenElementListener implements ElementListener {

	WindowToken current_window = null;
	NewItemCallback callback = null;
	
	public WindowTokenElementListener(NewItemCallback callback,WindowToken current_window) {
		this.current_window = current_window;
		this.callback = callback;
	}
	
	public void start(Attributes a) {
		current_window.setName((a.getValue("","name") == null) ? "mainDisplay" : a.getValue("","name"));
		current_window.setBufferText((a.getValue("","bufferText") == null) ? false : (a.getValue("","bufferText").equals("true")) ? true : false);
		current_window.setId((a.getValue("","id") == null) ? 0 : Integer.parseInt(a.getValue("","id")));
		current_window.setScriptName((a.getValue("","script") == null) ? "" : a.getValue("","script"));
		//current_window.
	}

	public void end() {
		callback.addWindow(current_window.getName(), current_window.copy());
		current_window.resetToDefaults();
	}

}
