package com.happygoatstudios.bt.service.plugin;

import java.util.HashMap;


import com.happygoatstudios.bt.speedwalk.DirectionData;

public class ConnectionSettingsPlugin extends Plugin {
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
	private boolean backspaceBugFix = false;
	
	
	private boolean debugTelnet = false;
	private boolean removeExtraColor = true;
	
	private LINK_MODE hyperLinkMode = LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND;
	private int hyperLinkColor = DEFAULT_HYPERLINK_COLOR;
	private boolean hyperLinkEnabled = true;
	
	private HashMap<String,DirectionData> Directions = new HashMap<String,DirectionData>();
	
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

	
}
