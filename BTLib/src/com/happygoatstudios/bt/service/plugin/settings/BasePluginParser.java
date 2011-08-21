package com.happygoatstudios.bt.service.plugin.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;

public class BasePluginParser {
	public static final String TAG_PLUGIN = "plugin";
	public static final String TAG_PLUGINS = "plugins";
	
	
	public static final String ATTR_NAME = "name";
	public static final String ATTR_AUTHOR = "author";
	public static final String ATTR_ID = "id";
	
	public static final String TAG_ALIASES = "aliases";
	public static final String TAG_ALIAS = "alias";
	public static final String ATTR_PRE = "pre";
	public static final String ATTR_POST = "post";
	
	public static final String TAG_TRIGGERS = "triggers";
	public static final String TAG_TRIGGER = "trigger";
	public static final String ATTR_TRIGGERTITLE = "title";
	public static final String ATTR_TRIGGERPATTERN = "pattern";
	public static final String ATTR_TRIGGERLITERAL = "interpretLiteral";
	public static final String ATTR_TRIGGERONCE = "fireOnce";
	public static final String ATTR_TRIGGERHIDDEN = "hidden";
	public static final String ATTR_TRIGGERENEABLED = "enabled";
	
	
	public static final String TAG_TIMERS = "timers";
	public static final String TAG_TIMER = "timer";
	public static final String ATTR_TIMERNAME = "name";
	public static final String ATTR_ORDINAL = "ordinal";
	public static final String ATTR_SECONDS = "seconds";
	public static final String ATTR_REPEAT = "repeat";
	public static final String ATTR_PLAYING = "playing";
	
	public static final String ATTR_FIRETYPE = "fireWhen";
	
	public static final String TAG_NOTIFICATIONRESPONDER = "notification";
	public static final String ATTR_NOTIFICATIONTITLE = "title";
	public static final String ATTR_NOTIFICATIONMESSAGE = "message";
	public static final String ATTR_NEWNOTIFICATION = "spawnNew";
	public static final String ATTR_USEONGOING = "useOngoing";
	public static final String ATTR_USEDEFAULTSOUND = "useSound";
	public static final String ATTR_USEDEFAULTLIGHT = "useLights";
	public static final String ATTR_USEDEFAULTVIBRATE = "useVibrate";
	public static final String ATTR_SOUNDPATH = "soundPath";
	public static final String ATTR_LIGHTCOLOR = "lightColor";
	public static final String ATTR_VIBRATELENGTH = "vibrateType";
	
	public static final String TAG_TOASTRESPONDER = "toast";
	public static final String ATTR_TOASTDELAY = "delay";
	public static final String ATTR_TOASTMESSAGE = "message";
	
	public static final String TAG_ACKRESPONDER = "ack";
	public static final String ATTR_ACKWITH = "with";
	
	public static final String TAG_SCRIPTRESPONDER = "script";
	public static final String ATTR_FUNCTION = "function";
	
	//public static final String TAG_PLUGIN = "plugin";
	///static final String 
	
	final String path;
	Context mContext;
	boolean defaultSettings = false;
	
	protected BasePluginParser(String location,Context context) {
		mContext = context;
		path = location;
	}
	
	protected InputStream getInputStream() throws FileNotFoundException {
		FileInputStream input = null;
		
		if(path == null) {
			return mContext.getResources().openRawResource(mContext.getResources().getIdentifier("default_settings", "raw", mContext.getPackageName()));	
		}
		
		if(path.contains(Environment.getExternalStorageDirectory().getPath())) {
			String state = Environment.getExternalStorageState();
			if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				input = new FileInputStream(path);
			}
		} else {
			input = mContext.openFileInput(path);
		}
		
		return input;
		
	}
	
}
