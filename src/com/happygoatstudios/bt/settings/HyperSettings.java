package com.happygoatstudios.bt.settings;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.xmlpull.v1.XmlSerializer;

//import android.util.Log;
import android.util.Xml;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

public class HyperSettings {
	
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
	
	
	private boolean UseExtractUI = false;
	private boolean AttemptSuggestions = false;
	
	private String encoding = "ISO-8859-1";
	
	private String SaveLocation = "none";
	
	private boolean SemiIsNewLine = true;
	private boolean ProcessPeriod = true;
	private boolean ThrottleBackground = false;
	private boolean KeepWifiActive = true;
	private boolean KeepLast = false;
	private boolean backspaceBugFix = false;
	
	
	
	

	
	private HashMap<String,AliasData> Aliases = new HashMap<String,AliasData>();
	private HashMap<String,Vector<SlickButtonData>> ButtonSets = new HashMap<String,Vector<SlickButtonData>>();
	private HashMap<String,ColorSetSettings> SetSettings = new HashMap<String,ColorSetSettings>();
	private HashMap<String,TriggerData> Triggers = new HashMap<String,TriggerData>();
	private HashMap<String,TimerData> Timers = new HashMap<String,TimerData>();
	
	private String lastSelected = "default";
	enum WRAP_MODE {
		NONE,
		BREAK,
		WORD
	}
	
	private WRAP_MODE WrapMode = WRAP_MODE.BREAK;

	public void setLineSize(int lineSize) {
		LineSize = lineSize;
	}

	public int getLineSize() {
		return LineSize;
	}

	public void setLineSpaceExtra(int lineSpaceExtra) {
		LineSpaceExtra = lineSpaceExtra;
	}

	public int getLineSpaceExtra() {
		return LineSpaceExtra;
	}

	public void setMaxLines(int maxLines) {
		MaxLines = maxLines;
	}

	public int getMaxLines() {
		return MaxLines;
	}

	public void setFontName(String fontName) {
		FontName = fontName;
	}

	public String getFontName() {
		return FontName;
	}

	public void setFontPath(String fontPath) {
		FontPath = fontPath;
	}

	public String getFontPath() {
		return FontPath;
	}

	public void setSaveLocation(String saveLocation) {
		SaveLocation = saveLocation;
	}

	public String getSaveLocation() {
		return SaveLocation;
	}

	public void setSemiIsNewLine(boolean semiIsNewLine) {
		SemiIsNewLine = semiIsNewLine;
	}

	public boolean isSemiIsNewLine() {
		return SemiIsNewLine;
	}

	public void setWrapMode(WRAP_MODE wrapMode) {
		WrapMode = wrapMode;
	}

	public WRAP_MODE getWrapMode() {
		return WrapMode;
	}
	

	public static String writeXml2(HyperSettings data) {
		XmlSerializer out = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			out.setOutput(writer);
			//out.startDocument("UTF-8", true);
			out.startDocument("UTF-8", true);
			out.startTag("", "root");
			
			out.startTag("", BaseParser.TAG_WINDOW);
			out.attribute("", BaseParser.ATTR_LINESIZE, new Integer(data.getLineSize()).toString());
			out.attribute("", BaseParser.ATTR_SPACEEXTRA, new Integer(data.getLineSpaceExtra()).toString());
			out.attribute("", BaseParser.ATTR_MAXLINES, new Integer(data.getMaxLines()).toString());
			out.attribute("", BaseParser.ATTR_FONTNAME, data.getFontName());
			out.attribute("", BaseParser.ATTR_FONTPATH, data.getFontPath());
			out.attribute("", BaseParser.ATTR_USEEXTRACTUI, (data.isUseExtractUI()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_SUGGESTIONS, (data.isAttemptSuggestions()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_KEEPLAST, (data.isKeepLast()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_BACKSPACEFIX, (data.isBackspaceBugFix()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_AUTOLAUNCHEDITOR, (data.isAutoLaunchButtonEdtior()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_DISABLECOLOR, (data.isDisableColor()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_OVERRIDEHF, (data.getHapticFeedbackMode().equals("")) ? "auto" : data.getHapticFeedbackMode());
			out.attribute("", BaseParser.ATTR_HFONPRESS, (data.getHapticFeedbackOnPress().equals("")) ? "auto" : data.getHapticFeedbackOnPress());
			out.attribute("", BaseParser.ATTR_HFONFLIP, (data.getHapticFeedbackOnFlip().equals("")) ? "auto" : data.getHapticFeedbackOnFlip());
			out.attribute("", BaseParser.ATTR_ENCODING, data.getEncoding());
			out.attribute("", BaseParser.ATTR_KEEPSCREENON, (data.isKeepScreenOn()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_FULLSCREEN, (data.isFullScreen()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_ROUNDBUTTONS, (data.isRoundButtons()) ? "true" : "false");
			
			switch(data.getWrapMode()) {
			case NONE:
				out.attribute("", BaseParser.ATTR_WRAPMODE, "0");
				break;
			case BREAK:
				out.attribute("", BaseParser.ATTR_WRAPMODE, "1");
				break;
			case WORD:
				out.attribute("", BaseParser.ATTR_WRAPMODE, "2");
				break;
			default:
			}
			
			out.endTag("", BaseParser.TAG_WINDOW);
			
			/****************************************
			out.startTag("",BaseParser.TAG_DATASEMINEWLINE);
			if(data.isSemiIsNewLine()) {
				out.text("1");
			} else {
				out.text("2");
			}
			
			out.endTag("", BaseParser.TAG_DATASEMINEWLINE);
			
			out.startTag("", BaseParser.TAG_PROCESSPERIOD);
			if(data.isProcessPeriod()) {
				out.text("true");
			} else {
				out.text("false");
			}
			out.endTag("",BaseParser.TAG_PROCESSPERIOD);
			********************************************/
			
			out.startTag("",BaseParser.TAG_SERVICE);
			
			out.attribute("", BaseParser.ATTR_SEMINEWLINE, (data.isSemiIsNewLine()) ? "true" : "false");
			//out.attribute("", BaseParser.ATTR_USEEXTRACTUI, (data.isUseExtractUI()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_THROTTLEBACKGROUND, (data.isThrottleBackground()) ? "true" : "false");
			out.attribute("" , BaseParser.ATTR_PROCESSPERIOD, data.isProcessPeriod() ? "true" : "false");
			out.attribute("", BaseParser.ATTR_WIFIKEEPALIVE, data.isKeepWifiActive() ? "true" : "false");
			out.attribute("", BaseParser.ATTR_LOCALECHO, (data.isLocalEcho()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_BELLVIBRATE, (data.isVibrateOnBell()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_BELLNOTIFY, (data.isNotifyOnBell()) ? "true" : "false");
			out.attribute("", BaseParser.ATTR_BELLDISPLAY, (data.isDisplayOnBell()) ? "true" : "false");
			
			out.endTag("",BaseParser.TAG_SERVICE);
			
			//output aliases
			out.startTag("", BaseParser.TAG_ALIASES);
			
			//for each alias, dump the data.
			Set<String> keys = data.getAliases().keySet();
			for(String key : keys) {
				AliasData tmpalias = data.getAliases().get(key);
				out.startTag("", BaseParser.TAG_ALIAS);
				out.attribute("", BaseParser.ATTR_PRE, tmpalias.getPre());
				out.attribute("", BaseParser.ATTR_POST, tmpalias.getPost());
				out.endTag("", BaseParser.TAG_ALIAS);
			}
			
			out.endTag("", BaseParser.TAG_ALIASES);
			
			out.startTag("", BaseParser.TAG_BUTTONSETS);
			//buttons
			Set<String> buttonsets = data.getButtonSets().keySet();
			for(String key : buttonsets) {
				out.startTag("", BaseParser.TAG_BUTTONSET);
				ColorSetSettings setdefaults = data.getSetSettings().get(key);
				out.attribute("", BaseParser.ATTR_SETNAME, key);
				if(setdefaults.getPrimaryColor() != SlickButtonData.DEFAULT_COLOR) out.attribute("", BaseParser.ATTR_PRIMARYCOLOR, Integer.toHexString(setdefaults.getPrimaryColor()));
				if(setdefaults.getSelectedColor() != SlickButtonData.DEFAULT_SELECTED_COLOR) out.attribute("", BaseParser.ATTR_SELECTEDCOLOR, Integer.toHexString(setdefaults.getSelectedColor()));
				if(setdefaults.getFlipColor() != SlickButtonData.DEFAULT_FLIP_COLOR) out.attribute("", BaseParser.ATTR_FLIPCOLOR, Integer.toHexString(setdefaults.getFlipColor()));
				if(setdefaults.getLabelColor() != SlickButtonData.DEFAULT_LABEL_COLOR) out.attribute("", BaseParser.ATTR_LABELCOLOR, Integer.toHexString(setdefaults.getLabelColor()));
				if(setdefaults.getButtonHeight() != SlickButtonData.DEFAULT_BUTTON_HEIGHT) out.attribute("", BaseParser.ATTR_BUTTONHEIGHT, new Integer(setdefaults.getButtonHeight()).toString());
				if(setdefaults.getButtonWidth() != SlickButtonData.DEFAULT_BUTTON_WDITH) out.attribute("", BaseParser.ATTR_BUTTONWIDTH, new Integer(setdefaults.getButtonWidth()).toString());
				if(setdefaults.getLabelSize() != SlickButtonData.DEFAULT_LABEL_SIZE) out.attribute("", BaseParser.ATTR_LABELSIZE, new Integer(setdefaults.getLabelSize()).toString());
				if(setdefaults.getFlipLabelColor() != SlickButtonData.DEFAULT_FLIPLABEL_COLOR) out.attribute("", BaseParser.ATTR_FLIPLABELCOLOR, Integer.toHexString(setdefaults.getFlipLabelColor()));
				
				Vector<SlickButtonData> the_set = data.getButtonSets().get(key);
				
				for(SlickButtonData button : the_set) {
					out.startTag("",BaseParser.TAG_BUTTON);
					out.attribute("", BaseParser.ATTR_XPOS, new Integer(button.getX()).toString());
					out.attribute("", BaseParser.ATTR_YPOS, new Integer(button.getY()).toString());
					if(!button.getLabel().equals(""))  out.attribute("", BaseParser.ATTR_LABEL, button.getLabel());
					if(!button.getText().equals("")) out.attribute("", BaseParser.ATTR_CMD, button.getText());
					if(!button.getFlipCommand().equals("")) out.attribute("", BaseParser.ATTR_FLIPCMD, button.getFlipCommand());
					out.attribute("", BaseParser.ATTR_MOVEMETHOD, new Integer(button.MOVE_STATE).toString());
					if(!button.getTargetSet().equals("")) out.attribute("", BaseParser.ATTR_TARGETSET, button.getTargetSet());
					if(button.getWidth() != setdefaults.getButtonWidth()) out.attribute("", BaseParser.ATTR_WIDTH, new Integer(button.getWidth()).toString());
					if(button.getHeight() != setdefaults.getButtonHeight()) out.attribute("", BaseParser.ATTR_HEIGHT, new Integer(button.getHeight()).toString()); 
					if(button.getPrimaryColor() != setdefaults.getPrimaryColor())  out.attribute("", BaseParser.ATTR_PRIMARYCOLOR, Integer.toHexString(button.getPrimaryColor()));
					if(button.getSelectedColor() != setdefaults.getSelectedColor())  out.attribute("", BaseParser.ATTR_SELECTEDCOLOR, Integer.toHexString(button.getSelectedColor()));
					if(button.getFlipColor() != setdefaults.getFlipColor())  out.attribute("", BaseParser.ATTR_FLIPCOLOR, Integer.toHexString(button.getFlipColor()).toString());
					if(button.getLabelColor() != setdefaults.getLabelColor())  out.attribute("", BaseParser.ATTR_LABELCOLOR, Integer.toHexString(button.getLabelColor()));
					if(button.getLabelSize() != setdefaults.getLabelSize())  out.attribute("", BaseParser.ATTR_LABELSIZE,  new Integer(button.getLabelSize()).toString());
					if(button.getFlipLabelColor() != setdefaults.getFlipLabelColor()) out.attribute("", BaseParser.ATTR_FLIPLABELCOLOR, Integer.toHexString(button.getFlipLabelColor()));
					if(!button.getFlipLabel().equals("")) out.attribute("", BaseParser.ATTR_FLIPLABEL, button.getFlipLabel());
					out.endTag("", BaseParser.TAG_BUTTON);
				}
				
				out.endTag("", BaseParser.TAG_BUTTONSET);
			}
			
			out.startTag("",BaseParser.TAG_SELECTEDSET);
			out.text(data.getLastSelected());
			out.endTag("", BaseParser.TAG_SELECTEDSET);
			
			out.endTag("", BaseParser.TAG_BUTTONSETS);
			
			//write trigger stuffs.
			out.startTag("",BaseParser.TAG_TRIGGERS);
			
			for(TriggerData trigger : data.getTriggers().values()) {
				out.startTag("", BaseParser.TAG_TRIGGER);
				out.attribute("", BaseParser.ATTR_TRIGGERTITLE, trigger.getName());
				out.attribute("", BaseParser.ATTR_TRIGGERPATTERN, trigger.getPattern());
				out.attribute("", BaseParser.ATTR_TRIGGERLITERAL, trigger.isInterpretAsRegex() ? "true" : "false");
				out.attribute("", BaseParser.ATTR_TRIGGERONCE, trigger.isFireOnce() ? "true" : "false");
				if(trigger.isHidden())  out.attribute("", BaseParser.ATTR_TRIGGERHIDDEN, "true");
				
				OutputResponders(out,trigger.getResponders());
				/*for(TriggerResponder responder : trigger.getResponders()) {
					switch(responder.getType()) {
					case NOTIFICATION:
						NotificationResponder notify = (NotificationResponder)responder;
						out.startTag("", BaseParser.TAG_NOTIFICATIONRESPONDER);
						out.attribute("", BaseParser.ATTR_NOTIFICATIONTITLE,notify.getTitle());
						out.attribute("", BaseParser.ATTR_NOTIFICATIONMESSAGE, notify.getMessage());
						out.attribute("", BaseParser.ATTR_FIRETYPE, notify.getFireType().getString() );
						if(notify.isUseDefaultSound()) {
							out.attribute("", BaseParser.ATTR_USEDEFAULTSOUND, "true");
							out.attribute("", BaseParser.ATTR_SOUNDPATH, notify.getSoundPath());
						} else {
							out.attribute("", BaseParser.ATTR_USEDEFAULTSOUND, "false");
						}
						
						if(notify.isUseDefaultLight()) {
							out.attribute("", BaseParser.ATTR_USEDEFAULTLIGHT, "true");
							out.attribute("", BaseParser.ATTR_LIGHTCOLOR, Integer.toHexString(notify.getColorToUse()));
						} else {
							out.attribute("", BaseParser.ATTR_USEDEFAULTLIGHT, "false");
						}
						
						if(notify.isUseDefaultVibrate()) {
							out.attribute("", BaseParser.ATTR_USEDEFAULTVIBRATE, "true");
							out.attribute("", BaseParser.ATTR_VIBRATELENGTH, Integer.toString(notify.getVibrateLength()));
						} else {
							out.attribute("", BaseParser.ATTR_USEDEFAULTVIBRATE, "false");
						}
						
						out.attribute("", BaseParser.ATTR_NEWNOTIFICATION, (notify.isSpawnNewNotification()) ? "true" : "false");
						out.attribute("", BaseParser.ATTR_USEONGOING, (notify.isUseOnGoingNotification()) ? "true" : "false");
						out.endTag("", BaseParser.TAG_NOTIFICATIONRESPONDER);
						break;
					case TOAST:
						ToastResponder toasty = (ToastResponder)responder;
						out.startTag("", BaseParser.TAG_TOASTRESPONDER);
						out.attribute("", BaseParser.ATTR_TOASTMESSAGE, toasty.getMessage());
						out.attribute("", BaseParser.ATTR_TOASTDELAY, new Integer(toasty.getDelay()).toString());
						out.attribute("", BaseParser.ATTR_FIRETYPE, toasty.getFireType().getString());
						out.endTag("", BaseParser.TAG_TOASTRESPONDER);
						break;
					case ACK:
						AckResponder ack = (AckResponder)responder;
						out.startTag("", BaseParser.TAG_ACKRESPONDER);
						out.attribute("", BaseParser.ATTR_ACKWITH, ack.getAckWith());
						out.attribute("", BaseParser.ATTR_FIRETYPE, ack.getFireType().getString());
						out.endTag("", BaseParser.TAG_ACKRESPONDER);
						break;
					default:
						break;
					}
					
				}*/
				
				
				out.endTag("", BaseParser.TAG_TRIGGER);
			}
			
			out.endTag("",BaseParser.TAG_TRIGGERS);
			
			//start timer output.
			out.startTag("", BaseParser.TAG_TIMERS);
			
			for(TimerData timer : data.getTimers().values()) {
				out.startTag("", BaseParser.TAG_TIMER);
				out.attribute("", BaseParser.ATTR_TIMERNAME, timer.getName());
				out.attribute("", BaseParser.ATTR_ORDINAL, timer.getOrdinal().toString());
				out.attribute("", BaseParser.ATTR_SECONDS, timer.getSeconds().toString());
				out.attribute("", BaseParser.ATTR_REPEAT, (timer.isRepeat()) ? "true" : "false");
				out.attribute("", BaseParser.ATTR_PLAYING, (timer.isPlaying()) ? "true" : "false");
				OutputResponders(out,timer.getResponders());
				out.endTag("", BaseParser.TAG_TIMER);
			}
			
			out.endTag("", BaseParser.TAG_TIMERS);
			
			out.endTag("", "root");
			
			out.endDocument();
			
			return writer.toString();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//return writer.toString();
	}
	
	private static void OutputResponders(XmlSerializer out, List<TriggerResponder> responders) throws IllegalArgumentException, IllegalStateException, IOException {
		for(TriggerResponder responder : responders) {
			switch(responder.getType()) {
			case NOTIFICATION:
				NotificationResponder notify = (NotificationResponder)responder;
				out.startTag("", BaseParser.TAG_NOTIFICATIONRESPONDER);
				out.attribute("", BaseParser.ATTR_NOTIFICATIONTITLE,notify.getTitle());
				out.attribute("", BaseParser.ATTR_NOTIFICATIONMESSAGE, notify.getMessage());
				out.attribute("", BaseParser.ATTR_FIRETYPE, notify.getFireType().getString() );
				if(notify.isUseDefaultSound()) {
					out.attribute("", BaseParser.ATTR_USEDEFAULTSOUND, "true");
					out.attribute("", BaseParser.ATTR_SOUNDPATH, notify.getSoundPath());
				} else {
					out.attribute("", BaseParser.ATTR_USEDEFAULTSOUND, "false");
				}
				
				if(notify.isUseDefaultLight()) {
					out.attribute("", BaseParser.ATTR_USEDEFAULTLIGHT, "true");
					out.attribute("", BaseParser.ATTR_LIGHTCOLOR, Integer.toHexString(notify.getColorToUse()));
				} else {
					out.attribute("", BaseParser.ATTR_USEDEFAULTLIGHT, "false");
				}
				
				if(notify.isUseDefaultVibrate()) {
					out.attribute("", BaseParser.ATTR_USEDEFAULTVIBRATE, "true");
					out.attribute("", BaseParser.ATTR_VIBRATELENGTH, Integer.toString(notify.getVibrateLength()));
				} else {
					out.attribute("", BaseParser.ATTR_USEDEFAULTVIBRATE, "false");
				}
				
				out.attribute("", BaseParser.ATTR_NEWNOTIFICATION, (notify.isSpawnNewNotification()) ? "true" : "false");
				out.attribute("", BaseParser.ATTR_USEONGOING, (notify.isUseOnGoingNotification()) ? "true" : "false");
				out.endTag("", BaseParser.TAG_NOTIFICATIONRESPONDER);
				break;
			case TOAST:
				ToastResponder toasty = (ToastResponder)responder;
				out.startTag("", BaseParser.TAG_TOASTRESPONDER);
				out.attribute("", BaseParser.ATTR_TOASTMESSAGE, toasty.getMessage());
				out.attribute("", BaseParser.ATTR_TOASTDELAY, new Integer(toasty.getDelay()).toString());
				out.attribute("", BaseParser.ATTR_FIRETYPE, toasty.getFireType().getString());
				out.endTag("", BaseParser.TAG_TOASTRESPONDER);
				break;
			case ACK:
				AckResponder ack = (AckResponder)responder;
				out.startTag("", BaseParser.TAG_ACKRESPONDER);
				out.attribute("", BaseParser.ATTR_ACKWITH, ack.getAckWith());
				out.attribute("", BaseParser.ATTR_FIRETYPE, ack.getFireType().getString());
				out.endTag("", BaseParser.TAG_ACKRESPONDER);
				break;
			default:
				break;
			}
		}
	}
	

	//getters and setters under nya.

	public void setAliases(HashMap<String,AliasData> aliases) {
		Aliases = aliases;
	}

	public HashMap<String,AliasData> getAliases() {
		return Aliases;
	}

	public void setButtonSets(HashMap<String,Vector<SlickButtonData>> buttonSets) {
		ButtonSets = buttonSets;
	}

	public HashMap<String,Vector<SlickButtonData>> getButtonSets() {
		return ButtonSets;
	}
	
	public void setSetSettings(HashMap<String,ColorSetSettings> setSettings) {
		SetSettings = setSettings;
	}
	
	public HashMap<String,ColorSetSettings> getSetSettings() {
		return SetSettings;
	}

	public void setLastSelected(String lastSelected) {
		//Log.e("SETTINGS","LAST SELECTED SET CHANGED TO:" + lastSelected);
		this.lastSelected = lastSelected;
	}

	public String getLastSelected() {
		//Log.e("SETTINGS","RETURNING SELECTED SET:" + lastSelected);
		return lastSelected;
	}

	public void setProcessPeriod(boolean processPeriod) {
		ProcessPeriod = processPeriod;
	}

	public boolean isProcessPeriod() {
		return ProcessPeriod;
	}

	public void setTriggers(HashMap<String,TriggerData> triggers) {
		Triggers = triggers;
	}

	public HashMap<String,TriggerData> getTriggers() {
		return Triggers;
	}

	public void setUseExtractUI(boolean useExtractUI) {
		UseExtractUI = useExtractUI;
	}

	public boolean isUseExtractUI() {
		return UseExtractUI;
	}

	public void setThrottleBackground(boolean throttleBackground) {
		ThrottleBackground = throttleBackground;
	}

	public boolean isThrottleBackground() {
		return ThrottleBackground;
	}
	
	public void setKeepWifiActive(boolean keepAlive) {
		KeepWifiActive = keepAlive;
	}
	
	public boolean isKeepWifiActive() {
		return KeepWifiActive;
	}

	public void setAttemptSuggestions(boolean attemptSuggestions) {
		AttemptSuggestions = attemptSuggestions;
	}

	public boolean isAttemptSuggestions() {
		return AttemptSuggestions;
	}

	public void setKeepLast(boolean keepLast) {
		KeepLast = keepLast;
	}

	public boolean isKeepLast() {
		return KeepLast;
	}

	public void setBackspaceBugFix(boolean backspaceBugFix) {
		this.backspaceBugFix = backspaceBugFix;
	}

	public boolean isBackspaceBugFix() {
		return backspaceBugFix;
	}

	public void setAutoLaunchButtonEdtior(boolean autoLaunchButtonEdtior) {
		AutoLaunchButtonEdtior = autoLaunchButtonEdtior;
	}

	public boolean isAutoLaunchButtonEdtior() {
		return AutoLaunchButtonEdtior;
	}

	public void setDisableColor(boolean disableColor) {
		DisableColor = disableColor;
	}

	public boolean isDisableColor() {
		return DisableColor;
	}

	/*public void setOverrideHapticFeedback(boolean overrideHapticFeedback) {
		OverrideHapticFeedback = overrideHapticFeedback;
	}

	public boolean isOverrideHapticFeedback() {
		return OverrideHapticFeedback;
	}*/

	public void setHapticFeedbackMode(String hapticFeedbackMode) {
		this.hapticFeedbackMode = hapticFeedbackMode;
	}

	public String getHapticFeedbackMode() {
		return hapticFeedbackMode;
	}

	public void setHapticFeedbackOnPress(String hapticFeedbackOnPress) {
		this.hapticFeedbackOnPress = hapticFeedbackOnPress;
	}

	public String getHapticFeedbackOnPress() {
		return hapticFeedbackOnPress;
	}

	public void setHapticFeedbackOnFlip(String hapticFeedbackOnFlip) {
		this.hapticFeedbackOnFlip = hapticFeedbackOnFlip;
	}

	public String getHapticFeedbackOnFlip() {
		return hapticFeedbackOnFlip;
	}

	public void setTimers(HashMap<String,TimerData> timers) {
		Timers = timers;
	}

	public HashMap<String,TimerData> getTimers() {
		return Timers;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setKeepScreenOn(boolean keepScreenOn) {
		this.keepScreenOn = keepScreenOn;
	}

	public boolean isKeepScreenOn() {
		return keepScreenOn;
	}

	public void setVibrateOnBell(boolean vibrateOnBell) {
		this.vibrateOnBell = vibrateOnBell;
	}

	public boolean isVibrateOnBell() {
		return vibrateOnBell;
	}

	public void setNotifyOnBell(boolean notifyOnBell) {
		this.notifyOnBell = notifyOnBell;
	}

	public boolean isNotifyOnBell() {
		return notifyOnBell;
	}

	public void setDisplayOnBell(boolean displayOnBell) {
		this.displayOnBell = displayOnBell;
	}

	public boolean isDisplayOnBell() {
		return displayOnBell;
	}

	public void setLocalEcho(boolean localEcho) {
		this.localEcho = localEcho;
	}

	public boolean isLocalEcho() {
		return localEcho;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setRoundButtons(boolean roundButtons) {
		this.roundButtons = roundButtons;
	}

	public boolean isRoundButtons() {
		return roundButtons;
	}

}
