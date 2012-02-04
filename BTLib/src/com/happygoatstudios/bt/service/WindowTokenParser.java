package com.happygoatstudios.bt.service;

import com.happygoatstudios.bt.service.plugin.settings.PluginParser.NewItemCallback;

import android.sax.Element;

public class WindowTokenParser {
	
	public static void registerListeners(Element root,WindowToken current_window,NewItemCallback handler) {
		//Element window = root.getChild("window");
		root.setElementListener(new WindowTokenElementListener(handler,current_window));
		LayoutElementListener l = new LayoutElementListener(current_window);
		
		Element groupElement = root.getChild("layoutGroup");
		groupElement.setStartElementListener(new LayoutGroupElementListener(current_window,l));
		
		Element layoutElement = groupElement.getChild("layout");
		layoutElement.setStartElementListener(l);
		//RuleElementListener r = new RuleElementListener(current_window);
		
	}
	
	
}
