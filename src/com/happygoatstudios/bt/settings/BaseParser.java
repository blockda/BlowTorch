package com.happygoatstudios.bt.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
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
	
	static final String TAG_WINDOW = "window";
	static final String ATTR_LINESIZE = "lineSize";
	static final String ATTR_SPACEEXTRA = "spaceExtra";
	static final String ATTR_MAXLINES = "maxLines";
	static final String ATTR_WRAPMODE = "wrapMode";
	static final String ATTR_FONTNAME = "fontName";
	static final String ATTR_FONTPATH = "fontPath";
	static final String TAG_DATASEMINEWLINE = "seminewline";
	
	static final String TAG_ALIASES = "aliases";
	static final String TAG_ALIAS = "alias";
	static final String ATTR_PRE = "pre";
	static final String ATTR_POST = "post";
	
	static final String TAG_BUTTONSETS = "buttonsets";
	static final String TAG_BUTTONSET = "buttonset";
	static final String ATTR_SETNAME = "setName";
	static final String TAG_BUTTON = "button";
	static final String ATTR_XPOS = "xPos";
	static final String ATTR_YPOS = "yPos";
	static final String ATTR_LABEL = "label";
	static final String ATTR_CMD = "command";
	static final String ATTR_FLIPCMD = "flipCommand";
	static final String ATTR_MOVEMETHOD = "moveMethod";
	
	final String path;
	Context window;
	
	BaseParser(String location,Context context) {
			
			window = context;
			this.path = location;
		
		
	}
	
	protected InputStream getInputStream() {
		try {
			FileInputStream input = window.openFileInput(path);
			
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
