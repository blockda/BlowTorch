package com.offsetnull.bt.service;

import org.xml.sax.Attributes;

import com.offsetnull.bt.service.LayoutGroup.LAYOUT_TYPE;

import android.sax.ElementListener;

public class LayoutGroupElementListener implements ElementListener {

	WindowToken current_window;
	LayoutElementListener sub = null;
	
	public LayoutGroupElementListener(WindowToken current_window,LayoutElementListener sub) {
		this.current_window = current_window;
		this.sub = sub;
	}
	
	public void start(Attributes a) {
		LayoutGroup g = new LayoutGroup();
		String target = a.getValue("","target");
		try {
			LayoutGroup.LAYOUT_TYPE tmp = LayoutGroup.LAYOUT_TYPE.valueOf(target.toLowerCase());
			
			g.type = tmp;
			
			current_window.layouts.put(g.type,g);
			sub.setCurrentType(g.type);
		} catch (IllegalArgumentException e) {
			
		}
	}

	public void end() {
		// TODO Auto-generated method stub
		
	}

}
