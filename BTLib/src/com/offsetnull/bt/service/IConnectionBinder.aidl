/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import com.offsetnull.bt.service.IConnectionBinderCallback;
import com.offsetnull.bt.service.ILauncherCallback;
import com.offsetnull.bt.service.IWindowCallback;
import com.offsetnull.bt.button.SlickButtonData;
import com.offsetnull.bt.settings.ColorSetSettings;
import com.offsetnull.bt.trigger.TriggerData;
import com.offsetnull.bt.responder.notification.NotificationResponder;
import com.offsetnull.bt.responder.ack.AckResponder;
import com.offsetnull.bt.responder.toast.ToastResponder;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.timer.TimerProgress;
import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.EncodingOption;
import com.offsetnull.bt.service.plugin.settings.IntegerOption;
import com.offsetnull.bt.service.plugin.settings.ColorOption;
import com.offsetnull.bt.service.plugin.settings.FileOption;
import com.offsetnull.bt.service.WindowToken;


interface IConnectionBinder {
	List getConnections();
	void switchTo(String display);
	void registerCallback(IConnectionBinderCallback c,String host,int port,String display);
	void unregisterCallback(IConnectionBinderCallback c);
	void registerLauncherCallback(ILauncherCallback launcher);
	void unregisterLauncherCallback(ILauncherCallback launcher);
	void startNewConnection(String host,int port,String display);
	void initXfer();
	void endXfer();
	boolean isConnected();
	boolean isConnectedTo(String display);
	void sendData(in byte[] seq);
	void saveSettings();
	void setConnectionData(String host,int port,String display);
	List getSystemCommands();
	AliasData getAlias(String key);
	AliasData getPluginAlias(String plugin,String key);
	Map getAliases();
	Map getPluginAliases(String currentPlugin);
	void setAliases(in Map map);
	void setPluginAliases(String plugin,in Map map);
	void deleteAlias(String key);
	void deletePluginAlias(String plugin,String key);
	void setAliasEnabled(boolean enabled,String key);
	void setPluginAliasEnabled(String plugin,boolean enabled,String key);
	void loadSettingsFromPath(String path);
	void exportSettingsToPath(String path);
	void resetSettings();
	Map getTriggerData();
	Map getDirectionData();
	Map getPluginTriggerData(String id);
	void setDirectionData(in Map data);
	void newTrigger(in TriggerData data);
	void newPluginTrigger(String selectedPlugin,in TriggerData data);
	void updateTrigger(in TriggerData from,in TriggerData to);
	void updatePluginTrigger(String selectedPlugin,in TriggerData from,in TriggerData to);
	void deleteTrigger(String which);
	void deletePluginTrigger(String selectedPlugin,String which);
	TriggerData getTrigger(String pattern);
	TriggerData getPluginTrigger(String selectedPlugin,String pattern);

	boolean isKeepLast();
	
	
	void setDisplayDimensions(int rows,int cols);
	void reconnect(String str);
	Map getTimers();
	Map getPluginTimers(String plugin);
	TimerData getTimer(String ordinal);
	TimerData getPluginTimer(String plugin,String name);
	void deleteTimer(String name);
	void deletePluginTimer(String plugin,String name);
	void startTimer(String ordinal);
	void pauseTimer(String ordinal);
	void stopTimer(String ordinal);
	void startPluginTimer(String plugin,String ordinal);
	void pausePluginTimer(String plugin,String ordinal);
	void stopPluginTimer(String plugin,String ordinal);
	void updateTimer(in TimerData old,in TimerData newtimer);
	void updatePluginTimer(String plugin,in TimerData old,in TimerData newtimer);
	void addPluginTimer(String plugin,in TimerData newtimer);
	void addTimer(in TimerData newtimer);
	void removeTimer(in TimerData deltimer);
	int getNextTimerOrdinal();
	Map getTimerProgressWad();
	String getEncoding();
	String getConnectedTo();
	
	boolean isFullScreen();
	
	void setTriggerEnabled(boolean enabled,String key);
	void setPluginTriggerEnabled(String selectedPlugin,boolean enabled,String key);
	void setButtonSetLocked(boolean locked,String key);
	boolean isButtonSetLocked(String key);
	boolean isButtonSetLockedMoveButtons(String key);
	boolean isButtonSetLockedNewButtons(String key);
	boolean isButtonSetLockedEditButtons(String key);
	WindowToken[] getWindowTokens();
	void registerWindowCallback(String displayName,String name,IWindowCallback callback);
	void unregisterWindowCallback(String name,IWindowCallback callback);
	String getScript(String plugin,String name);
	void reloadSettings();
	void pluginXcallS(String plugin,String function,String str);
	Map getPluginList();
	List getPluginsWithTriggers();
	SettingsGroup getSettings();
	SettingsGroup getPluginSettings(String plugin);
	void updateBooleanSetting(String key,boolean value);
	void updatePluginBooleanSetting(String plugin,String key,boolean value);
	void updateIntegerSetting(String key,int value);
	void updatePluginIntegerSetting(String plugin,String key,int value);
	void updateFloatSetting(String key,float value);
	void updatePluginFloatSetting(String plugin,String key,float value);
	void updateStringSetting(String key,String value);
	void updatePluginStringSetting(String plugin,String key,String value);
	void updateWindowBufferMaxValue(String plugin,String window,int amount);
	void closeConnection(String display);
	void windowShowing(boolean show);
	void dispatchLuaError(String message);
	void addLink(String path);
	void deletePlugin(String plugin);
	void setPluginEnabled(String plugin,boolean enabled);
	List getPluginsWithAliases();
	List getPluginsWithTimers();
	boolean isLinkLoaded(String link);
	String getPluginPath(String plugin);
	void dispatchLuaText(String str);
	void callPluginFunction(String plugin,String function);
	boolean isPluginInstalled(String desired);
	void setShowRegexWarning(boolean state);
}