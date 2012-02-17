package com.happygoatstudios.bt.service;

import com.happygoatstudios.bt.service.IConnectionBinderCallback;
import com.happygoatstudios.bt.service.IWindowCallback;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.timer.TimerProgress;
import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.service.plugin.settings.ListOption;
import com.happygoatstudios.bt.service.plugin.settings.SettingsGroup;
import com.happygoatstudios.bt.service.plugin.settings.BooleanOption;
import com.happygoatstudios.bt.service.plugin.settings.EncodingOption;
import com.happygoatstudios.bt.service.plugin.settings.IntegerOption;
import com.happygoatstudios.bt.service.plugin.settings.ColorOption;
import com.happygoatstudios.bt.service.plugin.settings.FileOption;


interface IConnectionBinder {
	List getConnections();
	void switchTo(String display);
	void registerCallback(IConnectionBinderCallback c,String host,int port,String display);
	void unregisterCallback(IConnectionBinderCallback c);
	void startNewConnection(String host,int port,String display);
	int getPid();
	void initXfer();
	void endXfer();
	boolean hasBuffer();
	boolean isConnected();
	boolean isConnectedTo(String display);
	void sendData(in byte[] seq);
	void saveSettings();
	void setNotificationText(CharSequence seq);
	void setConnectionData(String host,int port,String display);
	void beginCompression();
	void stopCompression();
	void requestBuffer();
	void saveBuffer(inout byte[] buffer);
	void addAlias(in AliasData a);
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
	void setFontSize(int size);
	//void setFontSize(String size);
	int getFontSize();
	//String getFontSize();
	void setFontSpaceExtra(int size);
	int getFontSpaceExtra();
	void setFontName(String name);
	String getFontName();
	void setFontPath(String path);
	void setMaxLines(int keepcount);
	int getMaxLines();
	void setSemiOption(boolean bools_are_newline);
	void addButton(String targetset, in SlickButtonData new_button);
	void removeButton(String targetset,in SlickButtonData button_to_nuke);
	List<SlickButtonData> getButtonSet(String setname);
	List<String> getButtonSetNames();
	void modifyButton(String targetset,in SlickButtonData orig, in SlickButtonData mod);
	void addNewButtonSet(String name);
	List<String> getButtonSets();
	int deleteButtonSet(String name);
	int clearButtonSet(String name);
	Map getButtonSetListInfo();
	String getLastSelectedSet();
	void LoadSettingsFromPath(String path);
	void ExportSettingsToPath(String path);
	void resetSettings();
	ColorSetSettings getCurrentColorSetDefaults();
	ColorSetSettings getColorSetDefaultsForSet(String the_set);
	void setColorSetDefaultsForSet(String the_set,in ColorSetSettings input);
	void setProcessPeriod(boolean value);
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
	void setUseExtractUI(boolean use);
	boolean getUseExtractUI();
	void setThrottleBackground(boolean use);
	boolean isThrottleBackground();
	boolean isProcessPeriod();
	boolean isEchoAliasUpdate();
	void setEchoAliasUpdate(boolean use);
	boolean isSemiNewline();
	void setKeepWifiActive(boolean use);
	boolean isKeepWifiActive();
	void setAttemptSuggestions(boolean use);
	boolean isAttemptSuggestions();
	void setKeepLast(boolean use);
	boolean isKeepLast();
	boolean isBackSpaceBugFix();
	void setBackSpaceBugFix(boolean use);
	boolean isAutoLaunchEditor();
	void setAutoLaunchEditor(boolean use);
	boolean isDisableColor();
	void setDisableColor(boolean use);
	String HapticFeedbackMode();
	void setHapticFeedbackMode(String use);
	String getAvailableSet();
	String getHFOnPress();
	String getHFOnFlip();
	void setHFOnPress(String use);
	void setHFOnFlip(String use);
	void setDisplayDimensions(int rows,int cols);
	void reconnect();
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
	void setEncoding(String input);
	String getConnectedTo();
	boolean isKeepScreenOn();
	void setKeepScreenOn(boolean use);
	boolean isLocalEcho();
	void setLocalEcho(boolean use);
	boolean isVibrateOnBell();
	void setVibrateOnBell(boolean use);
	boolean isNotifyOnBell();
	void setNotifyOnBell(boolean use);
	boolean isDisplayOnBell();
	void setDisplayOnBell(boolean use);	
	boolean isFullScreen();
	void setFullScreen(boolean use);	
	boolean isRoundButtons();
	void setRoundButtons(boolean use);
	int getBreakAmount();
	int getOrientation();
	boolean isWordWrap();
	void setBreakAmount(int pIn);
	void setOrientation(int pIn);
	void setWordWrap(boolean pIn);
	boolean isRemoveExtraColor();
	boolean isDebugTelnet();
	void setRemoveExtraColor(boolean pIn);
	void setDebugTelnet(boolean pIn);
	void updateAndRenameSet(String oldSet, String newSet,in ColorSetSettings settings);
	void setHyperLinkMode(String pIn);
	String getHyperLinkMode();
	void setHyperLinkColor(int pIn);
	int getHyperLinkColor();
	void setHyperLinkEnabled(boolean pIn);
	boolean isHyperLinkEnabled();
	void setTriggerEnabled(boolean enabled,String key);
	void setPluginTriggerEnabled(String selectedPlugin,boolean enabled,String key);
	void setButtonSetLocked(boolean locked,String key);
	boolean isButtonSetLocked(String key);
	boolean isButtonSetLockedMoveButtons(String key);
	boolean isButtonSetLockedNewButtons(String key);
	boolean isButtonSetLockedEditButtons(String key);
	List getWindowTokens();
	void registerWindowCallback(String name,IWindowCallback callback);
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
}