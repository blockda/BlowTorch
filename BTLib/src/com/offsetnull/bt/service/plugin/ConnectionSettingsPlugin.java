package com.offsetnull.bt.service.plugin;

import java.util.ArrayList;
import java.util.HashMap;

import org.keplerproject.luajava.LuaException;
import org.xmlpull.v1.XmlSerializer;


import android.os.Handler;

import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.EncodingOption;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.speedwalk.DirectionData;
import com.offsetnull.bt.trigger.TriggerData;

public class ConnectionSettingsPlugin extends Plugin {
	public ConnectionSettingsPlugin(Handler h,Connection parent) throws LuaException {
		super(h,parent);
		init();
	}
	
	public ConnectionSettingsPlugin(PluginSettings settings,Handler h,Connection parent) throws LuaException {
		super(settings,h,parent);
		init();
	}
	
	private void init() {
		SettingsGroup sg = new SettingsGroup();
		sg.setTitle("Program Settings");
		sg.setListener(parent);

		EncodingOption enc = new EncodingOption();
		enc.setTitle("System Encoding");
		enc.setDescription("Specifies the encoding used to process incoming text.");
		enc.setKey("encoding");
		sg.addOption(enc);
		
		ListOption orientation = new ListOption();
		orientation.setTitle("Orientation");
		orientation.setDescription("Sets the layout mode for the application. Automatic will switch the layout when the device rotates.");
		orientation.setKey("orientation");
		orientation.setValue(new Integer(0));
		orientation.addItem("Automatic");
		orientation.addItem("Landscape");
		orientation.addItem("Portrait");
		sg.addOption(orientation);
		
		BooleanOption screen_on = new BooleanOption();
		screen_on.setTitle("Keep Screen On?");
		screen_on.setDescription("Keep the screen on while the window is active.");
		screen_on.setKey("screen_on");
		screen_on.setValue(true);
		sg.addOption(screen_on);
		
		BooleanOption fullscreen = new BooleanOption();
		fullscreen.setTitle("Use Fullscreen Window?");
		fullscreen.setDescription("Hides the notification bar. This can be toggled by typing .togglefullscreen");
		fullscreen.setKey("fullscreen");
		fullscreen.setValue(true);
		sg.addOption(fullscreen);

		//SettingsGroup window = token.getSettings();
		
		
		SettingsGroup input = new SettingsGroup();
		input.setTitle("Input");
		input.setDescription("Options that deal with the input box and editors.");
		
		BooleanOption fullscreen_editor = new BooleanOption();
		fullscreen_editor.setTitle("Fullscreen Editor?");
		fullscreen_editor.setDescription("Show the full screen editor when the input bar is clicked.");
		fullscreen_editor.setKey("fullscreen_editor");
		fullscreen_editor.setValue(false);
		input.addOption(fullscreen_editor);
		
		BooleanOption use_suggestions = new BooleanOption();
		use_suggestions.setTitle("Use Suggestions?");
		use_suggestions.setDescription("Attempt suggestions if the full screen editor is not used.");
		use_suggestions.setKey("use_suggestions");
		use_suggestions.setValue(false);
		input.addOption(use_suggestions);
		
		BooleanOption keep_last = new BooleanOption();
		keep_last.setTitle("Keep Last Entered?");
		keep_last.setDescription("Keeps the last text entered in the window and highights after sending.");
		keep_last.setKey("keep_last");
		keep_last.setValue(false);
		input.addOption(keep_last);
		
		BooleanOption compatilibility_mode = new BooleanOption();
		compatilibility_mode.setTitle("Enable Compatibility Mode?");
		compatilibility_mode.setDescription("Enable this if you have problems with bascpace not workin in the non-full screen editor.");
		compatilibility_mode.setKey("compatibility_mode");
		compatilibility_mode.setValue(false);
		input.addOption(compatilibility_mode);
		
		sg.addOption(input);

		
		
		SettingsGroup servOptions = new SettingsGroup();
		servOptions.setTitle("Service");
		servOptions.setDescription("Options for the background service and data processing.");
		
		BooleanOption local_echo = new BooleanOption();
		local_echo.setTitle("Local Echo?");
		local_echo.setDescription("Will the service echo data sent to the server?");
		local_echo.setKey("local_echo");
		local_echo.setValue(true);
		servOptions.addOption(local_echo);
		
		BooleanOption process_system_commands = new BooleanOption();
		process_system_commands.setTitle("Process System Commands?");
		process_system_commands.setDescription("Perform system functions for input beginning with the specified system command marker.");
		process_system_commands.setKey("process_system_commands");
		process_system_commands.setValue(true);
		servOptions.addOption(process_system_commands);
		
		BooleanOption echo_alias_updates = new BooleanOption();
		echo_alias_updates.setTitle("Echo Alias Updates?");
		echo_alias_updates.setDescription("Local echo system command updates to aliases.");
		echo_alias_updates.setKey("echo_alias_updates");
		echo_alias_updates.setValue(true);
		servOptions.addOption(echo_alias_updates);
		
		BooleanOption process_semi = new BooleanOption();
		process_semi.setTitle("Process Semicolons?");
		process_semi.setDescription("Semicolons will be replaces with a newline character.");
		process_semi.setKey("process_semicolon");
		process_semi.setValue(true);
		servOptions.addOption(process_semi);
		
		BooleanOption keep_wifi_alive = new BooleanOption();
		keep_wifi_alive.setTitle("Keep Wifi Alive?");
		keep_wifi_alive.setDescription("Attempt to keep WiFi radio active while connected.");
		keep_wifi_alive.setKey("keep_wifi_alive");
		keep_wifi_alive.setValue(true);
		servOptions.addOption(keep_wifi_alive);
		
		BooleanOption cull_extraneous = new BooleanOption();
		cull_extraneous.setTitle("Cull Extraneous Colors?");
		cull_extraneous.setDescription("Removes extraneous color codes.");
		cull_extraneous.setKey("cull_extraneous_color");
		cull_extraneous.setValue(true);
		servOptions.addOption(cull_extraneous);
		
		BooleanOption debug_telnet = new BooleanOption();
		debug_telnet.setTitle("Debug Telnet?");
		debug_telnet.setDescription("Shows data involving telnet option transactions in the window.");
		debug_telnet.setKey("debug_telnet");
		debug_telnet.setValue(false);
		servOptions.addOption(debug_telnet);
		
		sg.addOption(servOptions);
		
		SettingsGroup bellOptions = new SettingsGroup();
		bellOptions.setTitle("Bell");
		bellOptions.setDescription("Options for what happens when the bell character is recieved.");
		
		BooleanOption bell_vibrate = new BooleanOption();
		bell_vibrate.setTitle("Vibrate?");
		bell_vibrate.setDescription("Plays a short vibrate pattern when the bell is recieved.");
		bell_vibrate.setKey("bell_vibrate");
		bell_vibrate.setValue(true);
		bellOptions.addOption(bell_vibrate);
		
		BooleanOption bell_notification = new BooleanOption();
		bell_notification.setTitle("Generate Notification?");
		bell_notification.setDescription("Spawns a new notification when bell is recieved.");
		bell_notification.setKey("bell_notification");
		bell_notification.setValue(false);
		bellOptions.addOption(bell_notification);
		
		BooleanOption bell_display = new BooleanOption();
		bell_display.setTitle("Display Bell?");
		bell_display.setDescription("Displays a small alert on the screen when the bell character is recieved.");
		bell_display.setKey("bell_display");
		bell_display.setValue(false);
		bellOptions.addOption(bell_display);
		
		sg.addOption(bellOptions);
		
		this.getSettings().setOptions(sg);
	}

	public static enum LINK_MODE {
		BACKGROUND ( "background"),
		HIGHLIGHT ("highlight"),
		HIGHLIGHT_COLOR ("highlight_color"),
		HIGHLIGHT_COLOR_ONLY_BLAND ( "highlight_color_bland_only"),
		NONE ( "none");
		
		private final String mode;  
		LINK_MODE(String str) {
			mode = str;
		}
		
		public String getValue() {
			return mode;
		}
	}
	
public final static int DEFAULT_HYPERLINK_COLOR = 0xFF3333AA;
	
	private int LineSize = 18;
	private int LineSpaceExtra = 2;
	private int MaxLines = 300;
	private String FontName = "monospace";
	private String FontPath = "none";
	private boolean AutoLaunchButtonEdtior = true;
	private boolean DisableColor = false;
	//private boolean OverrideHapticFeedback = false;
	private String hapticFeedbackMode = "auto";
	private String hapticFeedbackOnPress = "auto";
	private String hapticFeedbackOnFlip = "none";
	private boolean roundButtons = true;
	
	private boolean keepScreenOn = true;
	private boolean vibrateOnBell = true;
	private boolean notifyOnBell = false;
	private boolean displayOnBell = false;
	private boolean localEcho = true;
	private boolean fullScreen = true;
	private boolean echoAliasUpdates = true;
	
	private String gmcpTriggerChar = "%";
	private boolean wordWrap = true;
	private int breakAmount = 0; //0 is automatic
	private int orientation = 0; //0 is automatic
	
	private boolean UseExtractUI = false;
	private boolean AttemptSuggestions = false;
	
	private String encoding = "ISO-8859-1";
	
	private boolean SemiIsNewLine = true;
	private boolean ProcessPeriod = true;
	private boolean ThrottleBackground = false;
	private boolean KeepWifiActive = true;
	private boolean KeepLast = false;
	private boolean backspaceBugFix = true;
	
	
	private boolean debugTelnet = false;
	private boolean removeExtraColor = true;
	
	private LINK_MODE hyperLinkMode = LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND;
	private int hyperLinkColor = DEFAULT_HYPERLINK_COLOR;
	private boolean hyperLinkEnabled = true;
	
	private HashMap<String,DirectionData> Directions = new HashMap<String,DirectionData>();
	private ArrayList<String> links = new ArrayList<String>();
	
	
	private String lastSelected = "default";
	enum WRAP_MODE {
		NONE,
		BREAK,
		WORD
	}
	
	private WRAP_MODE WrapMode = WRAP_MODE.BREAK;

	public int getLineSize() {
		return LineSize;
	}

	public void setLineSize(int lineSize) {
		LineSize = lineSize;
	}

	public int getLineSpaceExtra() {
		return LineSpaceExtra;
	}

	public void setLineSpaceExtra(int lineSpaceExtra) {
		LineSpaceExtra = lineSpaceExtra;
	}

	public int getMaxLines() {
		return MaxLines;
	}

	public void setMaxLines(int maxLines) {
		MaxLines = maxLines;
	}

	public String getFontName() {
		return FontName;
	}

	public void setFontName(String fontName) {
		FontName = fontName;
	}

	public String getFontPath() {
		return FontPath;
	}

	public void setFontPath(String fontPath) {
		FontPath = fontPath;
	}

	public boolean isAutoLaunchButtonEdtior() {
		return AutoLaunchButtonEdtior;
	}

	public void setAutoLaunchButtonEdtior(boolean autoLaunchButtonEdtior) {
		AutoLaunchButtonEdtior = autoLaunchButtonEdtior;
	}

	public boolean isDisableColor() {
		return DisableColor;
	}

	public void setDisableColor(boolean disableColor) {
		DisableColor = disableColor;
	}

	public String getHapticFeedbackMode() {
		return hapticFeedbackMode;
	}

	public void setHapticFeedbackMode(String hapticFeedbackMode) {
		this.hapticFeedbackMode = hapticFeedbackMode;
	}

	public String getHapticFeedbackOnPress() {
		return hapticFeedbackOnPress;
	}

	public void setHapticFeedbackOnPress(String hapticFeedbackOnPress) {
		this.hapticFeedbackOnPress = hapticFeedbackOnPress;
	}

	public String getHapticFeedbackOnFlip() {
		return hapticFeedbackOnFlip;
	}

	public void setHapticFeedbackOnFlip(String hapticFeedbackOnFlip) {
		this.hapticFeedbackOnFlip = hapticFeedbackOnFlip;
	}

	public boolean isRoundButtons() {
		return roundButtons;
	}

	public void setRoundButtons(boolean roundButtons) {
		this.roundButtons = roundButtons;
	}

	public boolean isKeepScreenOn() {
		return keepScreenOn;
	}

	public void setKeepScreenOn(boolean keepScreenOn) {
		this.keepScreenOn = keepScreenOn;
	}

	public boolean isVibrateOnBell() {
		return vibrateOnBell;
	}

	public void setVibrateOnBell(boolean vibrateOnBell) {
		this.vibrateOnBell = vibrateOnBell;
	}

	public boolean isNotifyOnBell() {
		return notifyOnBell;
	}

	public void setNotifyOnBell(boolean notifyOnBell) {
		this.notifyOnBell = notifyOnBell;
	}

	public boolean isDisplayOnBell() {
		return displayOnBell;
	}

	public void setDisplayOnBell(boolean displayOnBell) {
		this.displayOnBell = displayOnBell;
	}

	public boolean isLocalEcho() {
		return localEcho;
	}

	public void setLocalEcho(boolean localEcho) {
		this.localEcho = localEcho;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public boolean isEchoAliasUpdates() {
		return echoAliasUpdates;
	}

	public void setEchoAliasUpdates(boolean echoAliasUpdates) {
		this.echoAliasUpdates = echoAliasUpdates;
	}

	public boolean isWordWrap() {
		return wordWrap;
	}

	public void setWordWrap(boolean wordWrap) {
		this.wordWrap = wordWrap;
	}

	public int getBreakAmount() {
		return breakAmount;
	}

	public void setBreakAmount(int breakAmount) {
		this.breakAmount = breakAmount;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public boolean isUseExtractUI() {
		return UseExtractUI;
	}

	public void setUseExtractUI(boolean useExtractUI) {
		UseExtractUI = useExtractUI;
	}

	public boolean isAttemptSuggestions() {
		return AttemptSuggestions;
	}

	public void setAttemptSuggestions(boolean attemptSuggestions) {
		AttemptSuggestions = attemptSuggestions;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isSemiIsNewLine() {
		return SemiIsNewLine;
	}

	public void setSemiIsNewLine(boolean semiIsNewLine) {
		SemiIsNewLine = semiIsNewLine;
	}

	public boolean isProcessPeriod() {
		return ProcessPeriod;
	}

	public void setProcessPeriod(boolean processPeriod) {
		ProcessPeriod = processPeriod;
	}

	public boolean isThrottleBackground() {
		return ThrottleBackground;
	}

	public void setThrottleBackground(boolean throttleBackground) {
		ThrottleBackground = throttleBackground;
	}

	public boolean isKeepLast() {
		return KeepLast;
	}

	public void setKeepLast(boolean keepLast) {
		KeepLast = keepLast;
	}

	public boolean isKeepWifiActive() {
		return KeepWifiActive;
	}

	public void setKeepWifiActive(boolean keepWifiActive) {
		KeepWifiActive = keepWifiActive;
	}

	public boolean isBackspaceBugFix() {
		return backspaceBugFix;
	}

	public void setBackspaceBugFix(boolean backspaceBugFix) {
		this.backspaceBugFix = backspaceBugFix;
	}

	public boolean isDebugTelnet() {
		return debugTelnet;
	}

	public void setDebugTelnet(boolean debugTelnet) {
		this.debugTelnet = debugTelnet;
	}

	public boolean isRemoveExtraColor() {
		return removeExtraColor;
	}

	public void setRemoveExtraColor(boolean removeExtraColor) {
		this.removeExtraColor = removeExtraColor;
	}

	public LINK_MODE getHyperLinkMode() {
		return hyperLinkMode;
	}

	public void setHyperLinkMode(LINK_MODE hyperLinkMode) {
		this.hyperLinkMode = hyperLinkMode;
	}

	public int getHyperLinkColor() {
		return hyperLinkColor;
	}

	public void setHyperLinkColor(int hyperLinkColor) {
		this.hyperLinkColor = hyperLinkColor;
	}

	public boolean isHyperLinkEnabled() {
		return hyperLinkEnabled;
	}

	public void setHyperLinkEnabled(boolean hyperLinkEnabled) {
		this.hyperLinkEnabled = hyperLinkEnabled;
	}

	public HashMap<String,DirectionData> getDirections() {
		return Directions;
	}

	public void setDirections(HashMap<String,DirectionData> directions) {
		Directions = directions;
	}

	public String getLastSelected() {
		return lastSelected;
	}

	public void setLastSelected(String lastSelected) {
		this.lastSelected = lastSelected;
	}

	public WRAP_MODE getWrapMode() {
		return WrapMode;
	}

	public void setWrapMode(WRAP_MODE wrapMode) {
		WrapMode = wrapMode;
	}

	public void outputXMLInternal(XmlSerializer out) {
		//this is where we take our normal data and 
	}

	public void importV1Settings(HyperSettings oldSettings) {
		//
		this.getSettings().setAliases(oldSettings.getAliases());
		this.getSettings().setTriggers(oldSettings.getTriggers());
		this.getSettings().setTimers(oldSettings.getTimers());
		
		//somehow handle buttons.
		this.setDirections(oldSettings.getDirections());
		
		//this.setWrapMode(oldSettings.getWrapMode());
		this.setKeepLast(oldSettings.isKeepLast());
		this.setRemoveExtraColor(oldSettings.isRemoveExtraColor());
		this.setDebugTelnet(oldSettings.isDebugTelnet());
		this.setAttemptSuggestions(oldSettings.isAttemptSuggestions());
		this.setEncoding(oldSettings.getEncoding());
		this.setDisplayOnBell(oldSettings.isDisplayOnBell());
		this.setNotifyOnBell(oldSettings.isNotifyOnBell());
		this.setVibrateOnBell(oldSettings.isVibrateOnBell());
		this.setFullScreen(oldSettings.isFullScreen());
		this.setKeepScreenOn(oldSettings.isKeepScreenOn());
		this.setProcessPeriod(oldSettings.isProcessPeriod());
		this.setOrientation(oldSettings.getOrientation());
		this.setEchoAliasUpdates(oldSettings.isEchoAliasUpdates());
		this.setUseExtractUI(oldSettings.isUseExtractUI());
		this.setSemiIsNewLine(oldSettings.isSemiIsNewLine());
		this.setLocalEcho(oldSettings.isLocalEcho());
		
		this.getSettings().getOptions().setOption("keep_last", Boolean.toString(oldSettings.isKeepLast()));
		this.getSettings().getOptions().setOption("cull_extraneous_color", Boolean.toString(oldSettings.isRemoveExtraColor()));
		this.getSettings().getOptions().setOption("debug_telnet", Boolean.toString(oldSettings.isDebugTelnet()));
		this.getSettings().getOptions().setOption("use_suggestions", Boolean.toString(oldSettings.isAttemptSuggestions()));
		this.getSettings().getOptions().setOption("encoding", oldSettings.getEncoding());
		this.getSettings().getOptions().setOption("bell_vibrate", Boolean.toString(oldSettings.isVibrateOnBell()));
		this.getSettings().getOptions().setOption("bell_notification", Boolean.toString(oldSettings.isNotifyOnBell()));
		this.getSettings().getOptions().setOption("bell_display", Boolean.toString(oldSettings.isDisplayOnBell()));
		this.getSettings().getOptions().setOption("fullscreen", Boolean.toString(oldSettings.isFullScreen()));
		this.getSettings().getOptions().setOption("screen_on", Boolean.toString(oldSettings.isKeepScreenOn()));
		this.getSettings().getOptions().setOption("process_system_commands", Boolean.toString(oldSettings.isProcessPeriod()));
		this.getSettings().getOptions().setOption("orientation", Integer.toString(oldSettings.getOrientation()));
		this.getSettings().getOptions().setOption("echo_alias_update", Boolean.toString(oldSettings.isEchoAliasUpdates()));
		this.getSettings().getOptions().setOption("fullscreen_editor", Boolean.toString(oldSettings.isUseExtractUI()));
		this.getSettings().getOptions().setOption("local_echo", Boolean.toString(oldSettings.isLocalEcho()));
		this.getSettings().getOptions().setOption("keep_wifi_alive", Boolean.toString(oldSettings.isKeepWifiActive()));
		this.getSettings().getOptions().setOption("compatibility_mode", Boolean.toString(oldSettings.isBackspaceBugFix()));
		this.getSettings().getOptions().setOption("process_semicolon", Boolean.toString(oldSettings.isSemiIsNewLine()));
		
		//set window token settings.
		this.getSettings().getOptions().setOption("hyperlinks_enabled", Boolean.toString(oldSettings.isHyperLinkEnabled()));
		switch(oldSettings.getHyperLinkMode()) {
		case BACKGROUND:
			this.getSettings().getOptions().setOption("hyperlink_mode", Integer.toString(4));
			break;
		case NONE:
			this.getSettings().getOptions().setOption("hyperlink_mode", Integer.toString(0));
			break;
		case HIGHLIGHT_COLOR_ONLY_BLAND:
			this.getSettings().getOptions().setOption("hyperlink_mode", Integer.toString(3));
			break;
		case HIGHLIGHT_COLOR:
			this.getSettings().getOptions().setOption("hyperlink_mode", Integer.toString(2));
			break;
		case HIGHLIGHT:
			this.getSettings().getOptions().setOption("hyperlink_mode", Integer.toString(1));
			break;
		}
		
		this.getSettings().getOptions().setOption("hyperlink_color", Integer.toString(oldSettings.getHyperLinkColor()));
		this.getSettings().getOptions().setOption("word_wrap", Boolean.toString(oldSettings.isWordWrap()));
		this.getSettings().getOptions().setOption("color_option", Integer.toString((oldSettings.isDisableColor() == true) ? 1 : 0));
		this.getSettings().getOptions().setOption("font_size", Integer.toString(oldSettings.getLineSize()));
		this.getSettings().getOptions().setOption("line_extra", Integer.toString(oldSettings.getLineSpaceExtra()));
		this.getSettings().getOptions().setOption("buffer_size", Integer.toString(oldSettings.getMaxLines()));
		if(oldSettings.getFontName().equals("")) {
			this.getSettings().getOptions().setOption("font_path", oldSettings.getFontPath());
		} else {
			this.getSettings().getOptions().setOption("font_path", oldSettings.getFontName());
		}
		
		
		
		//this.set
		
	}

	public void setLinks(ArrayList<String> links) {
		this.links = links;
	}

	public ArrayList<String> getLinks() {
		return links;
	}

	public void setGMCPTriggerChar(String gmcpTriggerChar) {
		this.gmcpTriggerChar = gmcpTriggerChar;
	}

	public String getGMCPTriggerChar() {
		return gmcpTriggerChar;
	}


}
