package com.happygoatstudios.bt.service;

import android.sax.Element;

public class WindowTokenParser {
	
	public static void registerListeners(Element root,WindowToken current_window) {
		Element window = root.getChild("window");
		window.setStartElementListener(new WindowTokenElementListener(current_window));
		LayoutElementListener l = new LayoutElementListener(current_window);
		
		Element groupElement = window.getChild("layoutGroup");
		groupElement.setStartElementListener(new LayoutGroupElementListener(current_window));
		
		Element layoutElement = groupElement.getChild("layout");
		layoutElement.setStartElementListener(l);
		//RuleElementListener r = new RuleElementListener(current_window);
		
	}
	
	
}
