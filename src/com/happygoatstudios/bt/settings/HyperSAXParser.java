package com.happygoatstudios.bt.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.window.SlickButtonData;

import android.content.Context;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class HyperSAXParser extends BaseParser {

	public HyperSAXParser(String location, Context context) {
		super(location, context);
		// TODO Auto-generated constructor stub
	}
	
	public HyperSettings load() {
		final HyperSettings tmp = new HyperSettings();
		RootElement root = new RootElement("root");
		Element window = root.getChild(TAG_WINDOW);
		Element data = root.getChild(TAG_DATASEMINEWLINE);
		Element aliases = root.getChild(TAG_ALIASES);
		Element alias = aliases.getChild(TAG_ALIAS);
		Element buttonsets = root.getChild(TAG_BUTTONSETS);
		Element buttonset = buttonsets.getChild(TAG_BUTTONSET);
		Element button = buttonset.getChild(TAG_BUTTON);
		
		final HashMap<String,String> aliases_read = new HashMap<String,String>();
		final HashMap<String,Vector<SlickButtonData>> buttons = new HashMap<String,Vector<SlickButtonData>>();
		final Vector<SlickButtonData> current_button_set = new Vector<SlickButtonData>();
		final StringBuffer button_set_name = new StringBuffer("default");
		
		window.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				//read in attribute values.
				tmp.setLineSize(new Integer(attributes.getValue("",ATTR_LINESIZE)).intValue());
				tmp.setLineSpaceExtra(new Integer(attributes.getValue("",ATTR_SPACEEXTRA)).intValue());
				tmp.setMaxLines(new Integer(attributes.getValue("",ATTR_MAXLINES)).intValue());
				tmp.setFontName(attributes.getValue("",ATTR_FONTNAME));
				tmp.setFontPath(attributes.getValue("",ATTR_FONTPATH));
				
				int wmode = new Integer(attributes.getValue("",ATTR_WRAPMODE));
				switch(wmode) {
				case 0:
					tmp.setWrapMode(HyperSettings.WRAP_MODE.NONE);
					break;
				case 1:
					tmp.setWrapMode(HyperSettings.WRAP_MODE.BREAK);
					break;
				case 2:
					tmp.setWrapMode(HyperSettings.WRAP_MODE.WORD);
					break;
				default:
				}
			}
			
		});
		
		data.setEndTextElementListener(new EndTextElementListener() {

			public void end(String body) {
				int newline = new Integer(body).intValue();
				if(newline == 0) {
					tmp.setSemiIsNewLine(false);
				} else {
					tmp.setSemiIsNewLine(true);
				}
			}
			
		});
		
		alias.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				aliases_read.put(attributes.getValue("",ATTR_PRE), attributes.getValue("",ATTR_POST));
			}
			
		});
		
		aliases.setEndElementListener(new EndElementListener() {

			public void end() {
				tmp.setAliases((HashMap<String,String>)aliases_read.clone());
			}
			
		});
		
		buttonset.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				button_set_name.setLength(0);
				button_set_name.append(attributes.getValue("",ATTR_SETNAME));
			}
			
		});
		
		buttonset.setEndElementListener(new EndElementListener() {

			@SuppressWarnings("unchecked")
			public void end() {
				//add the current set
				buttons.put(button_set_name.toString(), (Vector<SlickButtonData>)current_button_set.clone());
				
				//reset for next found
				button_set_name.setLength(0);
				button_set_name.append("default");
				current_button_set.removeAllElements();
				current_button_set.clear();
			}
			
		});
		
		button.setStartElementListener(new StartElementListener() {

			public void start(Attributes attributes) {
				SlickButtonData tmp  = new SlickButtonData();
				tmp.x = new Integer(attributes.getValue("",ATTR_XPOS));
				tmp.y = new Integer(attributes.getValue("",ATTR_YPOS));
				tmp.the_text = attributes.getValue("",ATTR_CMD);
				tmp.flip_command = attributes.getValue("", ATTR_FLIPCMD);
				tmp.the_label = attributes.getValue("",ATTR_LABEL);
				tmp.MOVE_STATE = new Integer(attributes.getValue("",ATTR_MOVEMETHOD));
				
				//add the new button to the current list.
				current_button_set.add(tmp);
			}
			
		});
		
		buttonsets.setEndElementListener(new EndElementListener() {

			public void end() {
				tmp.setButtonSets(buttons);
			}
			
		});
		
		
		
		
		try {
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return tmp; 
	}

}
