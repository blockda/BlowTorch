package com.happygoatstudios.bt.settings;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.happygoatstudios.bt.button.SlickButtonData;

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
	private HashMap<String,ColorSetSettings> SetSettings = new HashMap<String,ColorSetSettings>();
	
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
				ColorSetSettings setdefaults = data.getSetSettings().get(key);
				//Log.e("OUTPUT","ATTEMPTING TO WRITE SOME COLOR INFO FOR:" + key);
				for(String color : data.getSetSettings().keySet()) {
					//Log.e("OUTPUT","SET SETTINGS MAP CONTAINS ENTRY: " + color);
				}
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
			out.endTag("", "root");
			
			out.endDocument();
			
			return writer.toString();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//return writer.toString();
	}
	

	//getters and setters under nya.

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

}
