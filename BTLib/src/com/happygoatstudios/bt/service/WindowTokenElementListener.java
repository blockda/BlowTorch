package com.happygoatstudios.bt.service;

import org.xml.sax.Attributes;

import android.sax.StartElementListener;

public class WindowTokenElementListener implements StartElementListener {

	WindowToken current_window = null;
	
	public WindowTokenElementListener(WindowToken current_window) {
		this.current_window = current_window;
	}
	
	public void start(Attributes a) {
		current_window.setName(a.getValue("","name"));
		current_window.setBufferText((a.getValue("","bufferText") == null) ? false : (a.getValue("","bufferText").equals("true")) ? true : false);
		current_window.setId(Integer.parseInt(a.getValue("","id")));
		//current_window.
	}

}
