package com.happygoatstudios.bt.service;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.service.LayoutGroup.LAYOUT_TYPE;

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
		if(target.equals("small")) {
			g.type = LAYOUT_TYPE.SMALL;
		} else if(target.equals("normal")) {
			g.type = LAYOUT_TYPE.NORMAL;
		} else if(target.equals("large")) {
			g.type = LAYOUT_TYPE.LARGE;
		} else if(target.equals("xlarge")) {
			g.type = LAYOUT_TYPE.XLARGE;
		}
		
		current_window.layouts.put(g.type,g);
		sub.setCurrentType(g.type);
	}

	public void end() {
		// TODO Auto-generated method stub
		
	}

}
