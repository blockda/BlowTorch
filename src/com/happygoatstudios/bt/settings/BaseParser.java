package com.happygoatstudios.bt.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.Message;

public class BaseParser {

	static final String SETTINGS_BLOCK = "settings";
	static final String SAVE_LOCATION = "saveloc";
	static final String WINDOW_SETTINGS = "window";
	static final String WINDOW_LINESIZE = "linesize";
	static final String WINDOW_LINESIZEEXTRA = "linespaceextra";
	static final String WINDOW_WRAPMODE = "wrapmode";
	static final String WINDOW_FONT = "font";
	static final String WINDOW_FONTPATH = "fontpath";
	static final String DATA_SEMINEWLINE = "seminewline";
	static final String WINDOW_MAXLINES = "maxlines";
	static final String ATTR_AUTOLAUNCHEDITOR = "launchEditor";
	static final String ATTR_DISABLECOLOR = "disableColor";
	static final String ATTR_OVERRIDEHF = "hapticFeedBack";
	
	static final String TAG_WINDOW = "window";
	static final String ATTR_LINESIZE = "lineSize";
	static final String ATTR_SPACEEXTRA = "spaceExtra";
	static final String ATTR_MAXLINES = "maxLines";
	static final String ATTR_WRAPMODE = "wrapMode";
	static final String ATTR_FONTNAME = "fontName";
	static final String ATTR_FONTPATH = "fontPath";
	static final String TAG_DATASEMINEWLINE = "seminewline";
	static final String ATTR_USEEXTRACTUI = "useExtractUI";
	static final String ATTR_SUGGESTIONS = "useSuggest";
	static final String ATTR_KEEPLAST = "keepLast";
	static final String ATTR_BACKSPACEFIX = "backspaceBugFix";
	
	static final String TAG_SERVICE = "service";
	static final String ATTR_SEMINEWLINE = "processSemi";
	static final String ATTR_THROTTLEBACKGROUND = "throttle";
	static final String ATTR_PROCESSPERIOD = "processPeriod";
	static final String ATTR_WIFIKEEPALIVE = "keepWifiActive";
	static final String TAG_ALIASES = "aliases";
	static final String TAG_ALIAS = "alias";
	static final String ATTR_PRE = "pre";
	static final String ATTR_POST = "post";
	
	static final String TAG_BUTTONSETS = "buttonsets";
	static final String TAG_BUTTONSET = "buttonset";
	static final String TAG_SELECTEDSET = "selectedset";
	static final String ATTR_BUTTONWIDTH = "buttonWidth";
	static final String ATTR_BUTTONHEIGHT = "buttonHeight";
	static final String ATTR_SETNAME = "setName";
	static final String TAG_BUTTON = "button";
	static final String ATTR_XPOS = "xPos";
	static final String ATTR_YPOS = "yPos";
	static final String ATTR_HEIGHT = "height";
	static final String ATTR_WIDTH = "width";
	static final String ATTR_LABEL = "label";
	static final String ATTR_CMD = "command";
	static final String ATTR_TARGETSET = "targetSet";
	static final String ATTR_FLIPCMD = "flipCommand";
	static final String ATTR_MOVEMETHOD = "moveMethod";
	static final String ATTR_PRIMARYCOLOR = "color";
	static final String ATTR_SELECTEDCOLOR = "focusColor";
	static final String ATTR_FLIPCOLOR = "flipColor";
	static final String ATTR_LABELCOLOR = "labelColor";
	static final String ATTR_LABELSIZE = "labelSize";
	static final String ATTR_FLIPLABELCOLOR = "flipLabelColor";
	static final String ATTR_FLIPLABEL = "flipLabel";
	
	static final String TAG_TRIGGERS = "triggers";
	static final String TAG_TRIGGER = "trigger";
	static final String ATTR_TRIGGERTITLE = "title";
	static final String ATTR_TRIGGERPATTERN = "pattern";
	static final String ATTR_TRIGGERLITERAL = "interpretLiteral";
	static final String ATTR_TRIGGERONCE = "fireOnce";
	static final String ATTR_TRIGGERHIDDEN = "hidden";
	
	static final String ATTR_FIRETYPE = "fireWhen";
	
	static final String TAG_NOTIFICATIONRESPONDER = "notification";
	static final String ATTR_NOTIFICATIONTITLE = "title";
	static final String ATTR_NOTIFICATIONMESSAGE = "message";
	static final String ATTR_NEWNOTIFICATION = "spawnNew";
	static final String ATTR_USEONGOING = "useOngoing";
	static final String ATTR_USEDEFAULTSOUND = "useSound";
	static final String ATTR_USEDEFAULTLIGHT = "useLights";
	static final String ATTR_USEDEFAULTVIBRATE = "useVibrate";
	static final String ATTR_SOUNDPATH = "soundPath";
	static final String ATTR_LIGHTCOLOR = "lightColor";
	static final String ATTR_VIBRATELENGTH = "vibrateType";
	
	static final String TAG_TOASTRESPONDER = "toast";
	static final String ATTR_TOASTDELAY = "delay";
	static final String ATTR_TOASTMESSAGE = "message";
	
	static final String TAG_ACKRESPONDER = "ack";
	static final String ATTR_ACKWITH = "with";
	

	
	final String path;
	Context window;
	
	BaseParser(String location,Context context) {
			
			window = context;
			this.path = location;
		
		
	}
	
	protected InputStream getInputStream() {
		try {
			FileInputStream input = null;
			if(path.contains(Environment.getExternalStorageDirectory().getPath())) {
				String state = Environment.getExternalStorageState();
				if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					input = new FileInputStream(path);
				}
			} else {
				input = window.openFileInput(path);
			}
			return input;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	//appease the beast
	public List<HyperSettings> parse() {
		return null;
	}

}
