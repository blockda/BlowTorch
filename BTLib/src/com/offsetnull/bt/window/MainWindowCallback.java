package com.offsetnull.bt.window;

import android.app.Activity;
import android.os.RemoteException;

public interface MainWindowCallback {
	double getTitleBarHeight();
	double getStatusBarHeight();
	boolean isStatusBarHidden();
	String getPathForPlugin(String plugin);
	void dispatchLuaText(String text);
	Activity getActivity();
	boolean isPluginInstalled(String desired) throws RemoteException;
	boolean checkWindowSupports(String desired, String function);
	void windowCall(String window, String function, String data);
	void windowBroadcast(String function, String data);
}
