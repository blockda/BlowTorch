/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;

import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.ColorOption;
import com.offsetnull.bt.service.plugin.settings.FileOption;
import com.offsetnull.bt.service.plugin.settings.IntegerOption;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.service.LayoutGroup.LAYOUT_TYPE;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.window.TextTree;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

/** The serializable data that constitutes a foreground miniwindow. */
public class WindowToken implements Parcelable {
	/** Default hyperlink color. */
	public static final int DEFAULT_HYPERLINK_COLOR = 0xFF0000FF;
	/** Default hyperlink mode. */
	public static final int DEFAULT_HYPERLINK_MODE = 3;
	/** Default colorizing mode. */
	public static final int DEFAULT_COLOR_MODE = 0;
	/** Default font size. */
	public static final int DEFAULT_FONT_SIZE = 18;
	/** Default line spacing extra size. */
	public static final int DEFAULT_LINE_EXTRA = 3;
	/** Default buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 300;
	/** Default font path. */
	public static final String DEFAULT_FONT_PATH = "monospace";
	/** Required field for the parcelable interface. */
	public static final Parcelable.Creator<WindowToken> CREATOR = new Parcelable.Creator<WindowToken>() {

		public WindowToken createFromParcel(final Parcel arg0) {
			return new WindowToken(arg0);
		}

		public WindowToken[] newArray(final int arg0) {
			return new WindowToken[arg0];
		}
	};
	/** Enumeration containing the possible settings keys for a window. */
	public enum OPTION_KEY {
		/** Hyperlinking on or off. */
		hyperlinks_enabled,
		/** Hyperlink style (colorize, no colorize, only colorize if bland). */
		hyperlink_mode,
		/** Color to make hyperlinks. */
		hyperlink_color,
		/** Word wrapping on or off. */
		word_wrap,
		/** Color mode (see color debug option). */
		color_option,
		/** Font size option. */
		font_size,
		/** Line extra option. */
		line_extra,
		/** Buffer size. */
		buffer_size,
		/** Path to the font to use. */
		font_path
	}
	/** Hyperlink decoration off. */
	private static final int HYPERLINK_OFF = 0;
	/** Hyperlink highlight. */
	private static final int HYPERLINK_HIGHLIGHT = 1;
	/** Hyperlink colorize. */
	private static final int HYPERLINK_HIGHLIGHT_COLOR = 2;
	/** Hyperlink colorize if bland. */
	private static final int HYPERLINK_HIGHLIGHT_IF_BLAND = 3;
	/** Hyperlink background colorize. */
	private static final int HYPERLINK_BACKGROUND = 4;
	/** Small layout int for parcel indicator. */
	private static final int LAYOUT_SMALL = 0;
	/** Normal layout int for parcel indicator. */
	private static final int LAYOUT_NORMAL = 1;
	/** Large layout int for parcel indicator. */
	private static final int LAYOUT_LARGE = 2;
	/** XLarge layout int for parcel indicator. */
	private static final int LAYOUT_XLARGE = 3;
	
	/** The layout map for this window, maps layout type to the layoutgroup object. */
	private HashMap<LayoutGroup.LAYOUT_TYPE, LayoutGroup> mLayouts;
	/** The name of this window. */
	private String mName;
	/** Weather or not to buffer incoming text. */
	private boolean mBufferText = false;
	/** The id of this window. */
	private int mId;
	/** The text buffer for this window. */
	private TextTree mBuffer;
	/** The connection display name that owns this window. */
	private String mDisplayHost;
	/** The settings for this window. */
	private SettingsGroup mSettings = null;
	/** The name of the script to execute when this window is initialized. */
	private String mScriptName;
	/** The name of the plugin that owns this window. */
	private String mPluginName;
	
	/** Generic constructor. */
	public WindowToken() {
		mName = "";
		mDisplayHost = "";
		mLayouts = new HashMap<LayoutGroup.LAYOUT_TYPE, LayoutGroup>(0);
		initSettings();
	}
	
	/** A more functional constructor.
	 * 
	 * @param name The name of this window.
	 * @param scriptName The script body to execute (can be null).
	 * @param pluginName The plugin name that owns this window.
	 * @param displayHost The display name of the connection that owns this window.
	 */
	public WindowToken(final String name, final String scriptName, final String pluginName, final String displayHost) {
		//type = TYPE.SCRIPT;
		this.mName = name;
		this.mDisplayHost = displayHost;

		this.mScriptName = scriptName;
		this.mPluginName = pluginName;
		mBuffer = new TextTree();
		mLayouts = new HashMap<LayoutGroup.LAYOUT_TYPE, LayoutGroup>(0);
		
		initSettings();
	}
	
	/** Parcellable constructor. Constructs from a parcel.
	 * 
	 * @param p The incoming parcel object.
	 */
	public WindowToken(final Parcel p) {
		//owner = p.readString();
		mName = p.readString();
		//if(name.equals("chats")) {
		//	long ssfd = System.currentTimeMillis();
		//	ssfd = ssfd +10;
		//}
		mDisplayHost = p.readString();
		mId = p.readInt();
		//Log.e("TOKEN","PARCEL: READING WINDOW WITH ID:" + id);
		mLayouts = new HashMap<LayoutGroup.LAYOUT_TYPE, LayoutGroup>();
		//layout paramters.
		int numLayoutGroups = p.readInt();
		for (int i = 0; i < numLayoutGroups; i++) {
			LayoutGroup g = new LayoutGroup();
			int layoutType = p.readInt();
			switch(layoutType) {
			case LAYOUT_SMALL:
				g.setType(LAYOUT_TYPE.small);
				break;
			case LAYOUT_NORMAL:
				g.setType(LAYOUT_TYPE.normal);
				break;
			case LAYOUT_LARGE:
				g.setType(LAYOUT_TYPE.large);
				break;
			case LAYOUT_XLARGE:
				g.setType(LAYOUT_TYPE.xlarge);
				break;
			default:
				break;
			}
			
			int pWidth = p.readInt();
			int pHeight = p.readInt();
			int pMarginTop = p.readInt();
			int pMarginLeft = p.readInt();
			int pMarginRight = p.readInt();
			int pMarginBottom = p.readInt();
	
			RelativeLayout.LayoutParams portraitParams = g.getPortraitParams();
			portraitParams.height = pHeight;
			portraitParams.width = pWidth;
			portraitParams.setMargins(pMarginLeft, pMarginTop, pMarginRight, pMarginBottom);
			//int numrules = p.readInt();
			boolean done = false;
			while (!done) {
				int rule = p.readInt();
				if (rule > -1) {
					int option = p.readInt();
					portraitParams.addRule(rule, option);
				} else {
					done = true;
				}
			}
			
			
			int lWidth = p.readInt();
			int lHeight = p.readInt();
			int lMarginTop = p.readInt();
			int lMarginLeft = p.readInt();
			int lMarginRight = p.readInt();
			int lMarginBottom = p.readInt();
			RelativeLayout.LayoutParams landscapeParams = g.getLandscapeParams();
			landscapeParams.height = lHeight;
			landscapeParams.width = lWidth;
			landscapeParams.setMargins(lMarginLeft, lMarginTop, lMarginRight, lMarginBottom);
			done = false;
			while (!done) {
				int rule = p.readInt();
				if (rule > -1) {
					int option = p.readInt();
					landscapeParams.addRule(rule, option);
				} else {
					done = true;
				}
			}
			
			mLayouts.put(g.getType(), g);
		}
		setBufferText((p.readInt() == 0) ? false : true);
		int script = p.readInt();
		if (script != 1) {
			mScriptName = null;
			mPluginName = null;
		} else {
			mScriptName = p.readString();
			mPluginName = p.readString();
		}
		byte[] buf = p.createByteArray();
		mBuffer = new TextTree();
		//type = TYPE.SCRIPT;
		
		try {
			mBuffer.addBytesImpl(buf);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		mSettings = p.readParcelable(com.offsetnull.bt.service.plugin.settings.SettingsGroup.class.getClassLoader());
		
	}
	
	/** The real initialization routine. */
	private void initSettings() {
		SettingsGroup window = new SettingsGroup();
		window.setTitle("Window");
		window.setDescription("Options involved with the display of text or interaction with the window.");
		window.setKey("window_group");
		
		SettingsGroup hyperlinks = new SettingsGroup();
		hyperlinks.setTitle("Hyperlink Settings");
		hyperlinks.setDescription("Options for highlighting web page URLs.");
		hyperlinks.setKey("hyperlinks_options");
		
		BooleanOption hyperlinksEnabled = new BooleanOption();
		hyperlinksEnabled.setTitle("Enable Hyperlinks?");
		hyperlinksEnabled.setDescription("Make text that starts with http:// or www. a clickable link.");
		hyperlinksEnabled.setKey("hyperlinks_enabled");
		hyperlinksEnabled.setValue(true);
		hyperlinks.addOption(hyperlinksEnabled);
		
		ListOption hyperlinkMode = new ListOption();
		hyperlinkMode.setTitle("Hyperlink Mode");
		hyperlinkMode.setDescription("How hyperlinks are presented.");
		hyperlinkMode.setKey("hyperlink_mode");
		hyperlinkMode.addItem("None");
		hyperlinkMode.addItem("Underline");
		hyperlinkMode.addItem("Underline with specified Color");
		hyperlinkMode.addItem("Underline and Colorize, only if no ANSI color is specified");
		hyperlinkMode.addItem("Background highlight with specified color");
		hyperlinkMode.setValue(Integer.valueOf(DEFAULT_HYPERLINK_MODE));
		hyperlinks.addOption(hyperlinkMode);
		
		ColorOption hyperlinkColor = new ColorOption();
		hyperlinkColor.setTitle("Hyperlink Color");
		hyperlinkColor.setDescription("The color the hyperlink will be colorized with.");
		hyperlinkColor.setKey("hyperlink_color");
		hyperlinkColor.setValue(Integer.valueOf(DEFAULT_HYPERLINK_COLOR));
		hyperlinks.addOption(hyperlinkColor);
		
		window.addOption(hyperlinks);
		
		BooleanOption wordWrap = new BooleanOption();
		wordWrap.setTitle("Word Wrap?");
		wordWrap.setDescription("Broken text will be wrapped at the nearest whitespace.");
		wordWrap.setKey("word_wrap");
		wordWrap.setValue(true);
		window.addOption(wordWrap);
		
		ListOption colorOption = new ListOption();
		colorOption.setTitle("ANSI Color");
		colorOption.setDescription("Options for handling or disabling ANSI Color");
		colorOption.setKey("color_option");
		colorOption.setValue(0);
		colorOption.addItem("Enabled");
		colorOption.addItem("Disabled");
		colorOption.addItem("Show and colorize codes");
		colorOption.addItem("Show codes, do not colorize");
		window.addOption(colorOption);
		
		IntegerOption fontSize = new IntegerOption();
		fontSize.setTitle("Font Size");
		fontSize.setDescription("The height of a drawn character.");
		fontSize.setKey("font_size");
		fontSize.setValue(DEFAULT_FONT_SIZE);
		window.addOption(fontSize);
		
		IntegerOption lineExtra = new IntegerOption();
		lineExtra.setTitle("Line Spacing");
		lineExtra.setDescription("The extra space in between lines (in pixels)");
		lineExtra.setKey("line_extra");
		lineExtra.setValue(2);
		window.addOption(lineExtra);
		
		IntegerOption bufferSize = new IntegerOption();
		bufferSize.setTitle("Text Buffer Size");
		bufferSize.setDescription("The number of lines kept by the window for scrollback.");
		bufferSize.setKey("buffer_size");
		bufferSize.setValue(DEFAULT_BUFFER_SIZE);
		window.addOption(bufferSize);
		
		FileOption fontPath = new FileOption();
		fontPath.setTitle("Font");
		fontPath.setDescription("The font used by the window to render text.");
		fontPath.setKey("font_path");
		fontPath.setValue("monospace");
		fontPath.addItem("monospace");
		fontPath.addItem("sans serrif");
		fontPath.addItem("default");
		fontPath.addPath("/system/fonts/");
		fontPath.addPath("BlowTorch/");
		fontPath.addExtension(".ttf");
		window.addOption(fontPath);
		
		setSettings(window);
	}
	
	/** Shallow copy function.
	 * 
	 * @return A new WindowToken with the exact settings as this one.
	 */
	public final WindowToken copy() {
		WindowToken w = null;
		//if(this.scriptName == null) {
		//	w = new WindowToken(this.name,this.x,this.y,this.width,this.height);
		//} else {
		w = new WindowToken(this.mName, this.mScriptName, this.mPluginName, this.mDisplayHost);
		//}
		w.setSettings(this.getSettings());
		this.setSettings(null);
		this.initSettings();
		w.mId = this.mId;
		
		for (LayoutGroup g : this.mLayouts.values()) {
			//LayoutGroup g = layouts.get(i);
			LayoutGroup newg = new LayoutGroup();
			newg.setType(g.getType());
			RelativeLayout.LayoutParams pparams = g.getPortraitParams();
			RelativeLayout.LayoutParams newportraitparams = new RelativeLayout.LayoutParams(pparams.width, pparams.height);
			newportraitparams.setMargins(pparams.leftMargin, pparams.topMargin, pparams.rightMargin, pparams.bottomMargin);
			int[] rules = pparams.getRules();
			for (int j = 0; j < rules.length; j++) {
				if (rules[j] != 0) {
					newportraitparams.addRule(j, rules[j]);
				}
			}
			
			RelativeLayout.LayoutParams lparams = g.getLandscapeParams();
			RelativeLayout.LayoutParams newlandscapeparams = new RelativeLayout.LayoutParams(lparams.width, lparams.height);
			newportraitparams.setMargins(lparams.leftMargin, lparams.topMargin, lparams.rightMargin, lparams.bottomMargin);
			rules = null;
			rules = lparams.getRules();
			for (int j = 0; j < rules.length; j++) {
				if (rules[j] != 0) {
					newlandscapeparams.addRule(j, rules[j]);
				}
			}
			
			newg.setLandscapeParams(newlandscapeparams);
			newg.setPortraitParams(newportraitparams);
			
			w.mLayouts.put(newg.getType(), newg);
		}

		return w;
	}
	
	/** Setter for mName.
	 * 
	 * @param name The name to use.
	 */
	public final void setName(final String name) {
		this.mName = name;
	}

	/** Getter for mName.
	 * 
	 * @return mName
	 */
	public final String getName() {
		return mName;
	}
	
	/** Getter for mDisplayHost.
	 * 
	 * @return mDisplayHost
	 */
	public final String getDisplayHost() {
		return mDisplayHost;
	}
	
	/** Setter for mDisplayHost.
	 * 
	 * @param str The display host name to use.
	 */
	public final void setDisplayHost(final String str) {
		mDisplayHost = str;
	}

	/** Required method for the Parcellable interface. I think this needs to return a unique random int.
	 * 
	 * @return The integer description for the contents of this parcel.
	 */
	public final int describeContents() {
		return 0;
	}

	/** Implementation of the Parcelable writeToParcel method.
	 * 
	 * @param p The parcel to write to.
	 * @param arg1 The flags for the operation.
	 */
	public final void writeToParcel(final Parcel p, final int arg1) {
		p.writeString(mName);
		p.writeString(mDisplayHost);
		p.writeInt(mId);
		Set<LayoutGroup.LAYOUT_TYPE> keySet = mLayouts.keySet();
		p.writeInt(keySet.size());
		for (LayoutGroup g : mLayouts.values()) {
			switch(g.getType()) {
			case small:
				p.writeInt(LAYOUT_SMALL);
				break;
			case normal:
				p.writeInt(LAYOUT_NORMAL);
				break;
			case large:
				p.writeInt(LAYOUT_LARGE);
				break;
			case xlarge:
				p.writeInt(LAYOUT_XLARGE);
				break;
			default:
				break;
			}
			
			RelativeLayout.LayoutParams portraitParams = g.getPortraitParams();
			RelativeLayout.LayoutParams landscapeParams = g.getLandscapeParams();
			p.writeInt(portraitParams.width);
			p.writeInt(portraitParams.height);
			p.writeInt(portraitParams.topMargin);
			p.writeInt(portraitParams.leftMargin);
			p.writeInt(portraitParams.rightMargin);
			p.writeInt(portraitParams.bottomMargin);
			
			int[] rules = portraitParams.getRules();
			for (int i = 0; i < rules.length; i++) {
				if (rules[i] != 0) {
					p.writeInt(i);
					p.writeInt(rules[i]);
				}
			}
			p.writeInt(-1);
			
			p.writeInt(landscapeParams.width);
			p.writeInt(landscapeParams.height);
			p.writeInt(landscapeParams.topMargin);
			p.writeInt(landscapeParams.leftMargin);
			p.writeInt(landscapeParams.rightMargin);
			p.writeInt(landscapeParams.bottomMargin);
			
			rules = null;
			rules = landscapeParams.getRules();
			for (int i = 0; i < rules.length; i++) {
				if (rules[i] != 0) {
					p.writeInt(i);
					p.writeInt(rules[i]);
				}
			}
			p.writeInt(-1);
		}
		if (mBufferText) {
			p.writeInt(1);
		} else {
			p.writeInt(0);
		}
		if (mScriptName != null) {
			p.writeInt(1);
			p.writeString(mScriptName);
			p.writeString(mPluginName);
			p.writeByteArray(mBuffer.dumpToBytes(true));
		} else {
			p.writeInt(0);
			//Log.e("PARCEL","WINDOWTOKEN("+name+") DUMPING: " + buffer.getLines().size() + " lines.");
			p.writeByteArray(mBuffer.dumpToBytes(true));
		}
		
		p.writeParcelable(mSettings, arg1);
	}
	/** Setter for mBuffer.
	 * 
	 * @param buffer The new buffer to use.
	 */
	public final void setBuffer(final TextTree buffer) {
		this.mBuffer = buffer;
	}

	/** Getter for mBuffer.
	 * 
	 * @return mBuffer
	 */
	public final TextTree getBuffer() {
		return mBuffer;
	}
	
	/** Setter for mBufferText.
	 * 
	 * @param bufferText The new bufferText value.
	 */
	public final void setBufferText(final boolean bufferText) {
		this.mBufferText = bufferText;
	}

	/** Getter for mBufferText.
	 * 
	 * @return mBufferText
	 */
	public final boolean isBufferText() {
		return mBufferText;
	}
	
	/** Setter for mScriptName.
	 * 
	 * @param scriptName The new script name to execute on window initialization.
	 */
	public final void setScriptName(final String scriptName) {
		this.mScriptName = scriptName;
	}

	/** Getter for mScriptName.
	 * 
	 * @return mScriptName
	 */
	public final String getScriptName() {
		return mScriptName;
	}
	/** Setter for mPluginName.
	 * 
	 * @param pluginName The new plugin owner name to use.
	 */
	public final void setPluginName(final String pluginName) {
		this.mPluginName = pluginName;
	}

	/** Getter for mPluginName.
	 * 
	 * @return mPluginName
	 */
	public final String getPluginName() {
		return mPluginName;
	}

	/** Setter for mId.
	 * 
	 * @param id The new id value to use.
	 */
	public final void setId(final int id) {
		this.mId = id;
	}

	/** Getter for mId.
	 * 
	 * @return mId
	 */
	public final int getId() {
		return mId;
	}
	
	/** Getter for mLayouts.
	 * 
	 * @return mLayouts
	 */
	public final HashMap<LayoutGroup.LAYOUT_TYPE, LayoutGroup> getLayouts() {
		return mLayouts;
	}
	
	/** Utility method to reset this window token to the default values. */
	public final void resetToDefaults() {
		mName = "";
		mId = 0;
		mBufferText = false;
		mLayouts.clear();
	}

	/** Layout getter routine.
	 * 
	 * @param size The size value for the display.
	 * @param landscape True for landscape, false for portrait.
	 * @return The layout paramter object to be used for the current layout.
	 */
	public final LayoutParams getLayout(final int size, final boolean landscape) {
		
		switch(size) {
		case Configuration.SCREENLAYOUT_SIZE_SMALL:
			//tmp.setLayoutParams(w.getLayout(Configuration.SCREENLAYOUT_SIZE_SMALL,landscape));
			if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.small) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.small).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.small).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.large) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
			//break;
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.large) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.small) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.small).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.small).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
			//break;
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.large) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.small) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.small).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.small).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
			
		case Configuration.SCREENLAYOUT_SIZE_XLARGE:
			if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.xlarge).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.large) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.large).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getPortraitParams();
				}
			} else if (mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal) != null) {
				if (landscape) {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getLandscapeParams();
				} else {
					return mLayouts.get(LayoutGroup.LAYOUT_TYPE.normal).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
		default:
			break;
		}
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		return p;
		//return null;
	}

	/** Getter for mSettings.
	 * 
	 * @return mSettings
	 */
	public final SettingsGroup getSettings() {
		return mSettings;
	}

	/** Setter for mSettings.
	 * 
	 * @param settings The new settings group to use for this window's settings.
	 */
	public final void setSettings(final SettingsGroup settings) {
		this.mSettings = settings;
	}

	/** Utility method to set a new buffer size, prunes the tree after setting the value.
	 * 
	 * @param amount The new buffer size to use.
	 */
	public final void setBufferSize(final int amount) {
		this.mBuffer.setMaxLines(amount);
		this.mBuffer.prune();
	}

	/** Utility method to absorb v1 window settings into this window.
	 * 
	 * @param s The old deprecated HyperSettings object to sniff for settings.
	 */
	public final void importV1Settings(final HyperSettings s) {
		this.getSettings().setOption("hyperlinks_enabled", Boolean.toString(s.isHyperLinkEnabled()));
		//HyperSettings.LINK_MODE.
		switch(s.getHyperLinkMode()) {
		case NONE:
			this.getSettings().setOption("hyperlink_mode", Integer.toString(HYPERLINK_OFF));
			break;
		case HIGHLIGHT:
			this.getSettings().setOption("hyperlink_mode", Integer.toString(HYPERLINK_HIGHLIGHT));
			break;
		case HIGHLIGHT_COLOR:
			this.getSettings().setOption("hyperlink_mode", Integer.toString(HYPERLINK_HIGHLIGHT_COLOR));
			break;
		case HIGHLIGHT_COLOR_ONLY_BLAND:
			this.getSettings().setOption("hyperlink_mode", Integer.toString(HYPERLINK_HIGHLIGHT_IF_BLAND));
			break;
		case BACKGROUND:
			this.getSettings().setOption("hyperlink_mode", Integer.toString(HYPERLINK_BACKGROUND));
			break;
		default:
			break;
		}
		
		this.getSettings().setOption("hyperlink_color", Integer.toString(s.getHyperLinkColor()));
		this.getSettings().setOption("word_wrap", Boolean.toString(s.isWordWrap()));
		this.getSettings().setOption("color_option", Integer.toString(s.isDisableColor() ? 1 : 0));
		
		this.getSettings().setOption("font_size", Integer.toString(s.getLineSize()));
		this.getSettings().setOption("line_extra", Integer.toString(s.getLineSpaceExtra()));
		this.getSettings().setOption("buffer_size", Integer.toString(s.getMaxLines()));
		if (s.getFontName().equals("")) {
			this.getSettings().setOption("font_path", s.getFontPath());
		} else {
			this.getSettings().setOption("font_path", s.getFontName());
		}
	}
	

	//public void addSettingsToParent() {
		
	//}

	
	//public void setBuffer(TextTree buffer) {
	//	this.buffer = buffer;
	//}
	
}
