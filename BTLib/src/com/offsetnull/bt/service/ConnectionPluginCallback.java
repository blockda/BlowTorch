/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import android.content.Context;

/** Callback interface for plugins to interact with their parent Connection. */
public interface ConnectionPluginCallback {
	/** Sets the plugin system dirty bit. */
	void setTriggersDirty();
	/** Finds a window by name.
	 * 
	 * @param name Desired window name.
	 * @return The WindowToken associated with <b>name</b>. null if window does not exist.
	 */
	WindowToken getWindowByName(String name);
	/** Interrogates the foreground window as to its visibility state.
	 * 
	 * @return The foreground window visibility state.
	 */
	boolean isWindowShowing();
	/** Attaches a window settings changed listener to the target window. 
	 * 
	 * @param w WindowToken to attach a listener to.
	 */
	void attatchWindowSettingsChangedListener(WindowToken w);
	/** Gets the status bar height.
	 * 
	 * @return The status bar height.
	 */
	int getStatusBarHeight();
	/** Gets the title bar height. 
	 * 
	 * @return The title bar height.
	 */
	int getTitleBarHeight();
	/** Causes an immediate rebuild of the trigger system. */
	void buildTriggerSystem();
	/** Gets the display name for the Connection.
	 * 
	 * @return The display name.
	 */
	String getDisplayName();
	/** Gets the host name for the Connection.
	 * 
	 * @return The host name.
	 */
	String getHostName();
	/** Gets the port number for the Connection. 
	 * 
	 * @return The port number.
	 */
	int getPort();
	/** Gets the application context. 
	 * 
	 * @return The application context.
	 */
	Context getContext();
	/** Gets the default SettingsChangedListener used by the parent Connection.
	 * 
	 * @return The SettingsChangedListener.
	 */
	SettingsChangedListener getSettingsListener();
	/** Calls a function in a target plugin. 
	 * 
	 * @param plugin The target plugin.
	 * @param function The target function.
	 * @param data Arugment to supply to <b>target</b>
	 */
	void callPlugin(String plugin, String function, String data);
	/** Tests weather a plugin supports a given function.
	 * 
	 * @param plugin The target plugin.
	 * @param function The target function name to test.
	 * @return True if <b>plugin</b> supports <b>function</b>. False if not or <b>plugin</b> does not exist.
	 */
	boolean pluginSupports(String plugin, String function);
}
