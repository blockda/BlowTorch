/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import org.xml.sax.Attributes;

import android.sax.TextElementListener;

/** Custom TextElementListener object used by the settings inflating routine to inflate window option settings from the SAX parser. */
public class WindowOptionElementListener implements TextElementListener {

	/** The current working window to put inflated settings into. */
	private WindowToken mCurrentWindow = null;
	/** The current option key found by the SAX parser. */
	private String mCurrentKey = "";
	
	/** Generic constructor.
	 * 
	 * @param w The window token object to put settings into.
	 */
	public WindowOptionElementListener(final WindowToken w) {
		mCurrentWindow = w;
	}
	
	/** Implementation of the TextElementListener.Start routine.
	 * 
	 * @param a The attributes associated with this tag.
	 */
	public final void start(final Attributes a) {
		if (a.getValue("", "key") != null) {
			mCurrentKey = a.getValue("", "key");
		}
	}

	/** Impelmentation of the TextElementListener.end(...) routine.
	 * 
	 * @param body The text between the tag start and tag end.
	 */
	public final void end(final String body) {
		//Log.e("WINDOWPARSE", "PARSING OPTION:" + mCurrentKey + ":" + body);
		mCurrentWindow.getSettings().setOption(mCurrentKey, body);
	}

}
