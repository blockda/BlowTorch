package com.happygoatstudios.bt.service;

import org.xml.sax.Attributes;

import android.sax.StartElementListener;
import android.widget.RelativeLayout;

public class LayoutElementListener implements StartElementListener {

	LayoutGroup.LAYOUT_TYPE currentType = LayoutGroup.LAYOUT_TYPE.normal;
	
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
	
	enum DIMENSION_SPEC {
		wrap_content,
		fill_parent,
		match_parent
	}
	
	WindowToken current_window = null;
	
	public LayoutElementListener(WindowToken current_window) {
		this.current_window = current_window;
	}
	
	public void setCurrentType(LayoutGroup.LAYOUT_TYPE type) {
		currentType = type;
	}

	public void start(Attributes a) {
		RelativeLayout.LayoutParams params = null;
		LayoutGroup g = current_window.layouts.get(currentType);
		if(a.getValue("", "orientation") != null) {
			if(a.getValue("", "orientation").equals("landscape")) {
				params = g.getLandscapeParams();
			} else {
				params = g.getPortraitParams();
			}
		} else {
			params = g.getLandscapeParams();
		}
		
		try {
			DIMENSION_SPEC height = (a.getValue("", "height") == null) ? DIMENSION_SPEC.valueOf("match_parent") : DIMENSION_SPEC.valueOf(a.getValue("","height"));
			switch(height) {
			case fill_parent:
			case match_parent:
				params.height = RelativeLayout.LayoutParams.FILL_PARENT;
				break;
			case wrap_content:
				params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
				break;
			}
		} catch (IllegalArgumentException e) {
			params.height = Integer.parseInt(a.getValue("","height"));
		}
		
		try {
			DIMENSION_SPEC width = (a.getValue("", "width") == null) ? DIMENSION_SPEC.valueOf("match_parent") : DIMENSION_SPEC.valueOf(a.getValue("","width"));
			switch(width) {
			case fill_parent:
			case match_parent:
				params.width = RelativeLayout.LayoutParams.FILL_PARENT;
				break;
			case wrap_content:
				params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
				break;
			}
		} catch (IllegalArgumentException e) {
			params.width = Integer.parseInt(a.getValue("","width"));
		}
		
		int numattributes = a.getLength();
		for(int i=0;i<numattributes;i++) {
			String attribute = a.getLocalName(i);
			int value = 0;
			String strValue = "";
			boolean boolValue = false;
			try {
				//int value = 
				value = Integer.parseInt(a.getValue("",attribute));
			} catch(NumberFormatException e) {
				strValue = a.getValue("",attribute);
			}
			try {
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
			} catch(IllegalArgumentException e) {
				//orientation, height and width will be run through this. they are not RULES, so they will be ignored here.
			}
			
		}
		
		
	}
	
	
	
	
}
