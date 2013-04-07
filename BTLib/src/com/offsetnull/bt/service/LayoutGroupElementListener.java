/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.util.Locale;

import org.xml.sax.Attributes;

import android.sax.ElementListener;

/** The utiltity class used by the SAX parser to inflate a layout group element. */
public class LayoutGroupElementListener implements ElementListener {

	/** The current working window object, this is actually a single window object that gets re-used for every inflated window. */
	private WindowToken mCurrentwindow;
	/** The listener that is being used to listen to for new layouts. */
	private LayoutElementListener mSub = null;
	
	/** Generic constructor.
	 * 
	 * @param currentWindow The working window object to dump inflated settings to.
	 * @param sub The current layout element listener to coordinate with.
	 */
	public LayoutGroupElementListener(final WindowToken currentWindow, final LayoutElementListener sub) {
		this.mCurrentwindow = currentWindow;
		this.mSub = sub;
	}
	
	/** Implementation of the ElementListener.start(...) method.
	 * 
	 * @param a The attributes that are associated with the current tag.
	 */
	public final void start(final Attributes a) {
		LayoutGroup g = new LayoutGroup();
		String target = a.getValue("", "target");
		try {
			LayoutGroup.LAYOUT_TYPE tmp = LayoutGroup.LAYOUT_TYPE.valueOf(target.toLowerCase(Locale.US));
			
			g.setType(tmp);
			
			mCurrentwindow.getLayouts().put(g.getType(), g);
			mSub.setCurrentType(g.getType());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void end() {
		
	}

}
