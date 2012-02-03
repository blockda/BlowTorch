package com.happygoatstudios.bt.service;

import org.xml.sax.Attributes;

import android.sax.StartElementListener;
import android.widget.RelativeLayout;

public class LayoutElementListener implements StartElementListener {

	enum RULES {
		leftOf,
		rightOf,
		below,
		above,
		alignParentRight,
		alignParentLeft,
		alignParentTop,
		alignParentBottom,
		marginLeft,
		marginRight,
		marginTop,
		marginBottom
	}
	
	WindowToken current_window = null;
	
	public LayoutElementListener(WindowToken current_window) {
		this.current_window = current_window;
	}

	public void start(Attributes a) {
		RelativeLayout.LayoutParams params = null;
		LayoutGroup g = current_window.layouts.get(current_window.layouts.size());
		if(a.getValue("", "orientation") != null) {
			if(a.getValue("", "orientation").equals("landscape")) {
				params = g.getLandscapeParams();
			} else {
				params = g.getPortraitParams();
			}
		} else {
			params = g.getLandscapeParams();
		}
		
		params.height = (a.getValue("", "height") == null) ? RelativeLayout.LayoutParams.FILL_PARENT : Integer.parseInt(a.getValue("","height"));
		params.width = (a.getValue("", "width") == null) ? RelativeLayout.LayoutParams.FILL_PARENT : Integer.parseInt(a.getValue("","width"));
		
		int numattributes = a.getLength();
		for(int i=0;i<numattributes;i++) {
			String attribute = a.getLocalName(i);
			int value = Integer.parseInt(a.getValue("",attribute));
			RULES rule = RULES.valueOf(attribute);
			
			switch(rule) {
			case leftOf:
				params.addRule(RelativeLayout.LEFT_OF, value);
				break;
			case rightOf:
				params.addRule(RelativeLayout.RIGHT_OF, value);
				break;
			case below:
				params.addRule(RelativeLayout.BELOW, value);
				break;
			case above:
				params.addRule(RelativeLayout.ABOVE, value);
				break;
			case alignParentRight:
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
				break;
			case alignParentLeft:
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
				break;
			case alignParentBottom:
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
				break;
			case alignParentTop:
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
				break;
			case marginRight:
				params.rightMargin = value;
				break;
			case marginLeft:
				params.leftMargin = value;
				break;
			case marginTop:
				params.topMargin = value;
				break;
			case marginBottom:
				params.bottomMargin = value;
				break;
			}
		}
		
		
	}
	
	
	
	
}
