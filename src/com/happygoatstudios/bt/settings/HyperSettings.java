package com.happygoatstudios.bt.settings;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.happygoatstudios.bt.window.SlickButtonData;

public class HyperSettings {
	
	private int LineSize = 18;
	private int LineSpaceExtra = 2;
	private int MaxLines = 300;
	private String FontName = "monospace";
	private String FontPath = "none";
	
	private String SaveLocation = "none";
	
	private boolean SemiIsNewLine = true;
	
	private HashMap<String,String> Aliases = new HashMap<String,String>();
	private HashMap<String,Vector<SlickButtonData>> ButtonSets = new HashMap<String,Vector<SlickButtonData>>();
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
	
	public static String writeXml(List<HyperSettings> settings) {
		if(settings.size() < 1) {
			return null;
		}
		HyperSettings data = settings.get(0);
		XmlSerializer out = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			out.setOutput(writer);
			out.startDocument("UTF-8", true);
			out.startTag("", "root");
			out.startTag("", BaseParser.SETTINGS_BLOCK);
			out.startTag("", BaseParser.SAVE_LOCATION);
			out.text(data.getSaveLocation());
			out.endTag("", BaseParser.SAVE_LOCATION);
			
			out.startTag("", BaseParser.WINDOW_LINESIZE);
			out.text(new Integer(data.getLineSize()).toString());
			out.endTag("", BaseParser.WINDOW_LINESIZE);
			
			out.startTag("", BaseParser.WINDOW_LINESIZEEXTRA);
			out.text(new Integer(data.getLineSpaceExtra()).toString());
			out.endTag("", BaseParser.WINDOW_LINESIZEEXTRA);
			
			out.startTag("", BaseParser.WINDOW_FONT);
			out.text(data.getFontName());
			out.endTag("", BaseParser.WINDOW_FONT);
			
			out.startTag("", BaseParser.WINDOW_FONTPATH);
			out.text(data.getFontPath());
			out.endTag("", BaseParser.WINDOW_FONTPATH);
			
			out.startTag("", BaseParser.WINDOW_WRAPMODE);
			int putval = 0;
			switch(data.getWrapMode()) {
			case NONE:
				putval = 0;
				break;
			case BREAK:
				putval = 1;
				break;
			case WORD:
				putval = 2;
				break;
			default:
			}
			out.text(new Integer(putval).toString());
			out.endTag("", BaseParser.WINDOW_WRAPMODE);
			
			out.startTag("", BaseParser.WINDOW_MAXLINES);
			out.text(new Integer(data.getMaxLines()).toString());
			out.endTag("", BaseParser.WINDOW_MAXLINES);
			
			out.startTag("", BaseParser.DATA_SEMINEWLINE);
			if(data.isSemiIsNewLine()) {
				out.text("1");
			} else {
				out.text("0");
			}
			out.endTag("", BaseParser.DATA_SEMINEWLINE);
			
			out.endTag("", BaseParser.SETTINGS_BLOCK);
			out.endTag("", "root");
			out.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static String writeXml2(HyperSettings data) {
		XmlSerializer out = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			out.setOutput(writer);
			out.startDocument("UTF-8", true);
			out.startTag("", "root");
			
			out.startTag("", BaseParser.TAG_WINDOW);
			out.attribute("", BaseParser.ATTR_LINESIZE, new Integer(data.getLineSize()).toString());
			out.attribute("", BaseParser.ATTR_SPACEEXTRA, new Integer(data.getLineSpaceExtra()).toString());
			out.attribute("", BaseParser.ATTR_MAXLINES, new Integer(data.getMaxLines()).toString());
			out.attribute("", BaseParser.ATTR_FONTNAME, data.getFontName());
			out.attribute("", BaseParser.ATTR_FONTPATH, data.getFontPath());
			
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
			
			out.startTag("",BaseParser.TAG_DATASEMINEWLINE);
			if(data.isSemiIsNewLine()) {
				out.text("1");
			} else {
				out.text("2");
			}
			
			out.endTag("", BaseParser.TAG_DATASEMINEWLINE);
			
			//output aliases
			out.startTag("", BaseParser.TAG_ALIASES);
			
			//for each alias, dump the data.
			Set<String> keys = data.getAliases().keySet();
			for(String key : keys) {
				out.startTag("", BaseParser.TAG_ALIAS);
				out.attribute("", BaseParser.ATTR_PRE, key);
				out.attribute("", BaseParser.ATTR_POST, data.getAliases().get(key));
				out.endTag("", BaseParser.TAG_ALIAS);
			}
			
			out.endTag("", BaseParser.TAG_ALIASES);
			
			out.startTag("", BaseParser.TAG_BUTTONSETS);
			//buttons
			Set<String> buttonsets = data.getButtonSets().keySet();
			for(String key : buttonsets) {
				out.startTag("", BaseParser.TAG_BUTTONSET);
				out.attribute("", BaseParser.ATTR_SETNAME, key);
				
				Vector<SlickButtonData> the_set = data.getButtonSets().get(key);
				for(SlickButtonData button : the_set) {
					out.startTag("",BaseParser.TAG_BUTTON);
					out.attribute("", BaseParser.ATTR_XPOS, new Integer(button.x).toString());
					out.attribute("", BaseParser.ATTR_YPOS, new Integer(button.y).toString());
					out.attribute("", BaseParser.ATTR_LABEL, button.the_label);
					out.attribute("", BaseParser.ATTR_CMD, button.the_text);
					out.attribute("", BaseParser.ATTR_FLIPCMD, button.flip_command);
					out.attribute("", BaseParser.ATTR_MOVEMETHOD, new Integer(button.MOVE_STATE).toString());
					out.endTag("", BaseParser.TAG_BUTTON);
				}
				
				out.endTag("", BaseParser.TAG_BUTTONSET);
			}
			
			out.startTag("",BaseParser.TAG_SELECTEDSET);
			out.text(data.getLastSelected());
			out.endTag("", BaseParser.TAG_SELECTEDSET);
			
			out.endTag("", BaseParser.TAG_BUTTONSETS);
			out.endTag("", "root");
			
			out.endDocument();
			
			return writer.toString();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//return writer.toString();
	}

	public void setAliases(HashMap<String,String> aliases) {
		Aliases = aliases;
	}

	public HashMap<String,String> getAliases() {
		return Aliases;
	}

	public void setButtonSets(HashMap<String,Vector<SlickButtonData>> buttonSets) {
		ButtonSets = buttonSets;
	}

	public HashMap<String,Vector<SlickButtonData>> getButtonSets() {
		return ButtonSets;
	}

	public void setLastSelected(String lastSelected) {
		this.lastSelected = lastSelected;
	}

	public String getLastSelected() {
		return lastSelected;
	}

}
