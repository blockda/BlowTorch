/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import org.xml.sax.Attributes;

import com.offsetnull.bt.service.plugin.settings.PluginParser.NewItemCallback;

import android.sax.ElementListener;

/** Utility class that is used to inflate window token data from the xml settings through the SAX parser. */
public class WindowTokenElementListener implements ElementListener {

	/** The current window to dump inflated settings to. */
	private WindowToken mCurrentWindow = null;
	/** The callback to call when a new window is completly inflated. */
	private NewItemCallback mCallback = null;
	
	/** Constructor.
	 * 
	 * @param callback The callback to use when a new window is finished inflating.
	 * @param currentWindow The window object to dump inflated settings and data too.
	 */
	public WindowTokenElementListener(final NewItemCallback callback, final WindowToken currentWindow) {
		this.mCurrentWindow = currentWindow;
		this.mCallback = callback;
	}
	
	/** Implementation of ElementListener.start(...).
	 * 
	 * @param a The attributes associated with the parsed tag.
	 */
	public final void start(final Attributes a) {
		mCurrentWindow.setName((a.getValue("", "name") == null) ? "mainDisplay" : a.getValue("", "name"));
		mCurrentWindow.setBufferText((a.getValue("", "bufferText") == null) ? false : (a.getValue("", "bufferText").equals("true")) ? true : false);
		mCurrentWindow.setId((a.getValue("", "id") == null) ? 0 : Integer.parseInt(a.getValue("", "id")));
		mCurrentWindow.setScriptName((a.getValue("", "script") == null) ? "" : a.getValue("", "script"));
		//current_window.
	}

	/** Implementation of ElementListener.end(...). */
	public final void end() {
		mCallback.addWindow(mCurrentWindow.getName(), mCurrentWindow.copy());
		mCurrentWindow.resetToDefaults();
	}

}
