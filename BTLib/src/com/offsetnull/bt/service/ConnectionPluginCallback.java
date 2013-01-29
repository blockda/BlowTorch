package com.offsetnull.bt.service;

import android.content.Context;

public interface ConnectionPluginCallback {
	public void setTriggersDirty();
	public WindowToken getWindowByName(String desired);
	boolean isWindowShowing();
	void attatchWindowSettingsChangedListener(WindowToken w);
	int getStatusBarHeight();
	int getTitleBarHeight();
	void buildTriggerSystem();
	String getDisplayName();
	String getHostName();
	int getPort();
	Context getContext();
	SettingsChangedListener getSettingsListener();
	public void callPlugin(String plugin, String function, String data);
	public boolean pluginSupports(String plugin,String function);
}
