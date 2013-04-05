/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import org.xml.sax.Attributes;

import android.sax.StartElementListener;
import android.widget.RelativeLayout;

/** The StartElementListener responsible for keeping track of the serialized layout settings in the xml file. */
public class LayoutElementListener implements StartElementListener {

	/** The current layout type being parsed. */
	private LayoutGroup.LAYOUT_TYPE mCurrentType = LayoutGroup.LAYOUT_TYPE.normal;
	
	/** The name of the current working window. */
	private WindowToken mCurrentWindow = null;
	
	/** The rules that can be assigned to this layout. */
	enum RULES {
		/** @see LayoutParams.LEFT_OF */
		leftOf,
		/** @see LayoutParmas.RIGHT_OF */
		rightOf,
		/** @see LayoutParams.BELOW */
		below,
		/** @see LayoutParams.ABOVE */
		above,
		/** @see LayoutParams.ALIGN_PARENT_RIGHT */
		alignParentRight,
		/** @see LayoutParams.ALIGN_PARENT_LEFT */
		alignParentLeft,
		/** @see LayoutParams.ALIGN_PARENT_TOP */
		alignParentTop,
		/** @see LayoutParams.ALIGN_PARENT_BOTTOM */
		alignParentBottom,
		/** left margin value. */
		marginLeft,
		/** right margin value. */
		marginRight,
		/** top margin value. */
		marginTop,
		/** bottom margin value. */
		marginBottom
	}
	
	/** The types of "other" rules that can be applied to layout width/height. */
	enum DIMENSION_SPEC {
		/** LayoutParams.WRAP_CONTENT. */
		wrap_content,
		/** LayoutParams.FILL_PARENT. */
		fill_parent,
		/** LayoutParams.MATCH_PARENT. */
		match_parent
	}
	
	/** Constructor for this class.
	 * 
	 * @param currentWindow The current working window object that is being inflated.
	 */
	public LayoutElementListener(final WindowToken currentWindow) {
		this.mCurrentWindow = currentWindow;
	}
	
	/** Setter for the mCurrentType field.
	 * 
	 * @param type The type to use.
	 */
	public final void setCurrentType(final LayoutGroup.LAYOUT_TYPE type) {
		mCurrentType = type;
	}

	/** The implementation for the StartElementListener method.
	 * 
	 * @param a the associated attributes.
	 * @see StartElementListener
	 */
	@SuppressWarnings("deprecation")
	public final void start(final Attributes a) {
		RelativeLayout.LayoutParams params = null;
		LayoutGroup g = mCurrentWindow.layouts.get(mCurrentType);
		if (a.getValue("", "orientation") != null) {
			if (a.getValue("", "orientation").equals("landscape")) {
				params = g.getLandscapeParams();
			} else {
				params = g.getPortraitParams();
			}
		} else {
			params = g.getLandscapeParams();
		}
		
		try {
			DIMENSION_SPEC height = (a.getValue("", "height") == null) ? DIMENSION_SPEC.valueOf("match_parent") : DIMENSION_SPEC.valueOf(a.getValue("", "height"));
			switch(height) {
			case fill_parent:
			case match_parent:
				params.height = RelativeLayout.LayoutParams.FILL_PARENT;
				break;
			case wrap_content:
				params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
				break;
			default:
				break;
			}
		} catch (IllegalArgumentException e) {
			params.height = Integer.parseInt(a.getValue("", "height"));
		}
		
		try {
			DIMENSION_SPEC width = (a.getValue("", "width") == null) ? DIMENSION_SPEC.valueOf("match_parent") : DIMENSION_SPEC.valueOf(a.getValue("", "width"));
			switch(width) {
			case fill_parent:
			case match_parent:
				params.width = RelativeLayout.LayoutParams.FILL_PARENT;
				break;
			case wrap_content:
				params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
				break;
			default:
				break;
			}
		} catch (IllegalArgumentException e) {
			params.width = Integer.parseInt(a.getValue("", "width"));
		}
		
		int numattributes = a.getLength();
		for (int i = 0; i < numattributes; i++) {
			String attribute = a.getLocalName(i);
			int value = 0;
			try {
				value = Integer.parseInt(a.getValue("", attribute));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			try {
				RULES rule = RULES.valueOf(attribute);
				
				switch (rule) {
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
				default:
					break;
				}
			} catch (IllegalArgumentException e) {
				//orientation, height and width will be run through this. they are not RULES, so they will be ignored here.
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	
	
	
}
