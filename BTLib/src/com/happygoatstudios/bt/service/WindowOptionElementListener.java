package com.happygoatstudios.bt.service;

import org.xml.sax.Attributes;

import android.sax.ElementListener;
import android.sax.TextElementListener;

public class WindowOptionElementListener implements TextElementListener {

	WindowToken current_window = null;
	public WindowOptionElementListener(WindowToken w) {
		current_window = w;
	}
	
	String current_key = "";
	
	@Override
	public void start(Attributes a) {
		if(a.getValue("","key") != null) {
			current_key = a.getValue("","key");
		}
	}

	@Override
	public void end(String body) {
		current_window.getSettings().setOption(current_key, body);
	}

}
