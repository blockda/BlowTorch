/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.service.plugin.settings.BaseOption;
import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.ColorOption;
import com.offsetnull.bt.service.plugin.settings.FileOption;
import com.offsetnull.bt.service.plugin.settings.IntegerOption;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.Option;
import com.offsetnull.bt.service.plugin.settings.PluginParser.NewItemCallback;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;

import android.sax.Element;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/** Utility class to set up and export a WindowToken object to and from the xml file. */
public final class WindowTokenParser {
	
	/** Private constructor. */
	private WindowTokenParser() {
		
	}
	
	/** Utility method to attatch the appropriate SAX parsing listener objects to the supplied node.
	 * 
	 * @param root The node to attach listeners too.
	 * @param currentWindow the window object to dump data into.
	 * @param handler The callback to call when a new window is inflated.
	 */
	public static void registerListeners(final Element root, final WindowToken currentWindow, final NewItemCallback handler) {
		root.setElementListener(new WindowTokenElementListener(handler, currentWindow));
		LayoutElementListener l = new LayoutElementListener(currentWindow);
		
		Element groupElement = root.getChild("layoutGroup");
		groupElement.setStartElementListener(new LayoutGroupElementListener(currentWindow, l));
		
		Element layoutElement = groupElement.getChild("layout");
		layoutElement.setStartElementListener(l);
		
		Element options = root.getChild("options");
		Element option = options.getChild("option");
		
		option.setTextElementListener(new WindowOptionElementListener(currentWindow));
		
	}
	
	/** Export the supplied window token to the supplied XmlSerializer.
	 * 
	 * @param out The XmlSerializer to export the window to.
	 * @param w The window to export.
	 */
	@SuppressWarnings("deprecation")
	public static void saveToXml(final XmlSerializer out, final WindowToken w) {
		try {
		out.startTag("", "window");
		out.attribute("", "name", w.getName());
		if (w.getId() != 0) {
			out.attribute("", "id", Integer.toString(w.getId()));
		}
		if (w.getScriptName() != null && !w.getScriptName().equals("")) {
			out.attribute("", "script", w.getScriptName());
		}
		
		
		for (LayoutGroup g : w.getLayouts().values()) {
			out.startTag("", "layoutGroup");
			out.attribute("", "target", g.getType().toString());
			
			if (g.getLandscapeParams() != null) {
				out.startTag("", "layout");
				out.attribute("", "orientation", "landscape");
				LayoutParams params = g.getLandscapeParams();
				int[] rules = params.getRules();
				for (int i = 0; i < rules.length; i++) {
					if (rules[i] != 0) {
						switch(i) {
						case RelativeLayout.ABOVE:
							out.attribute("", "above", Integer.toString(rules[i]));
							break;
						case RelativeLayout.BELOW:
							out.attribute("", "below", Integer.toString(rules[i]));
							break;
						case RelativeLayout.LEFT_OF:
							out.attribute("", "leftOf", Integer.toString(rules[i]));
							break;
						case RelativeLayout.RIGHT_OF:
							out.attribute("", "rightOf", Integer.toString(rules[i]));
							break;
						case RelativeLayout.ALIGN_PARENT_RIGHT:
							out.attribute("", "alignParentRight", "true");
							break;
						case RelativeLayout.ALIGN_PARENT_LEFT:
							out.attribute("", "alignParentLeft", "true");
							break;
						case RelativeLayout.ALIGN_PARENT_TOP:
							out.attribute("", "alignParentTop", "true");
							break;
						case RelativeLayout.ALIGN_PARENT_BOTTOM:
							out.attribute("", "alignParentBottom", "true");
							break;
						default:
							break;
						}
					}
				}
				if (params.topMargin != 0) {
					out.attribute("", "marginTop", Integer.toString(params.topMargin));
				}
				if (params.bottomMargin != 0) {
					out.attribute("", "marginBottom", Integer.toString(params.bottomMargin));
				}
				if (params.leftMargin != 0) {
					out.attribute("", "marginLeft", Integer.toString(params.leftMargin));
				}
				if (params.rightMargin != 0) {
					out.attribute("", "marginRight", Integer.toString(params.rightMargin));
				}
				
				if (params.width == RelativeLayout.LayoutParams.FILL_PARENT) {
					out.attribute("", "width", "fill_parent");
				} else if (params.width == RelativeLayout.LayoutParams.WRAP_CONTENT) {
					out.attribute("", "width", "wrap_content");
				} else {
					out.attribute("", "width", Integer.toString(params.width));
				}
				
				if (params.height == RelativeLayout.LayoutParams.FILL_PARENT) {
					out.attribute("", "height", "fill_parent");
				} else if (params.height == RelativeLayout.LayoutParams.WRAP_CONTENT) {
					out.attribute("", "height", "wrap_content");
				} else {
					out.attribute("", "height", Integer.toString(params.height));
				}
				out.endTag("", "layout");
			}
			
			if (g.getPortraitParams() != null) {
				out.startTag("", "layout");
				out.attribute("", "orientation", "portrait");
				LayoutParams params = g.getPortraitParams();
				int[] rules = params.getRules();
				for (int i = 0; i < rules.length; i++) {
					if (rules[i] != 0) {
						switch(i) {
						case RelativeLayout.ABOVE:
							out.attribute("", "above", Integer.toString(rules[i]));
							break;
						case RelativeLayout.BELOW:
							out.attribute("", "below", Integer.toString(rules[i]));
							break;
						case RelativeLayout.LEFT_OF:
							out.attribute("", "leftOf", Integer.toString(rules[i]));
							break;
						case RelativeLayout.RIGHT_OF:
							out.attribute("", "rightOf", Integer.toString(rules[i]));
							break;
						case RelativeLayout.ALIGN_PARENT_RIGHT:
							out.attribute("", "alignParentRight", "true");
							break;
						case RelativeLayout.ALIGN_PARENT_LEFT:
							out.attribute("", "alignParentLeft", "true");
							break;
						case RelativeLayout.ALIGN_PARENT_TOP:
							out.attribute("", "alignParentTop", "true");
							break;
						case RelativeLayout.ALIGN_PARENT_BOTTOM:
							out.attribute("", "alignParentBottom", "true");
							break;
						default:
							break;
						}
					}
				}
				if (params.topMargin != 0) {
					out.attribute("", "marginTop", Integer.toString(params.topMargin));
				}
				if (params.bottomMargin != 0) {
					out.attribute("", "marginBottom", Integer.toString(params.bottomMargin));
				}
				if (params.leftMargin != 0) {
					out.attribute("", "marginLeft", Integer.toString(params.leftMargin));
				}
				if (params.rightMargin != 0) {
					out.attribute("", "marginRight", Integer.toString(params.rightMargin));
				}
				
				if (params.width == RelativeLayout.LayoutParams.FILL_PARENT) {
					out.attribute("", "width", "fill_parent");
				} else if (params.width == RelativeLayout.LayoutParams.WRAP_CONTENT) {
					out.attribute("", "width", "wrap_content");
				} else {
					out.attribute("", "width", Integer.toString(params.width));
				}
				
				if (params.height == RelativeLayout.LayoutParams.FILL_PARENT) {
					out.attribute("", "height", "fill_parent");
				} else if (params.height == RelativeLayout.LayoutParams.WRAP_CONTENT) {
					out.attribute("", "height", "wrap_content");
				} else {
					out.attribute("", "height", Integer.toString(params.height));
				}
				out.endTag("", "layout");
			}
			
			out.endTag("", "layoutGroup");
		}
		
		//output the option tags.
		out.startTag("", "options");

		//do this peicewise in order to control defaults.
		dumpOptions(out, w.getSettings());
		
		
		out.endTag("", "options");
		out.endTag("", "window");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
	}
	
	/** Recursive option dumping routine.
	 * 
	 * @param out The XmlSerializer to dump options to.
	 * @param group A settings group object.
	 */
	private static void dumpOptions(final XmlSerializer out, final SettingsGroup group) {
		try {
		for (Option tmp : group.getOptions()) {
			if (tmp instanceof SettingsGroup) {
				dumpOptions(out, (SettingsGroup) tmp);
			} else {
				BaseOption o = (BaseOption) tmp;
				try {
					WindowToken.OPTION_KEY key = WindowToken.OPTION_KEY.valueOf(o.getKey());
					switch(key) {
					case word_wrap:
					case hyperlinks_enabled:
						if (!(Boolean) ((BooleanOption) o).getValue()) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((Boolean) ((BooleanOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					case hyperlink_color:
						if (((Integer) ((ColorOption) o).getValue()) != WindowToken.DEFAULT_HYPERLINK_COLOR) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text("#" + Integer.toHexString((Integer) ((ColorOption) o).getValue()));
							out.endTag("", "option");
						}
						break;
					case hyperlink_mode:
						if (((Integer) ((ListOption) o).getValue()) != WindowToken.DEFAULT_HYPERLINK_MODE) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((Integer) ((ListOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					case color_option:
						if (((Integer) ((ListOption) o).getValue()) != WindowToken.DEFAULT_COLOR_MODE) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((Integer) ((ListOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					case font_size:
						Log.e("SAVING WINDOW", "FONT SIZE:" + (Integer) ((IntegerOption) o).getValue());
						if (((Integer) ((IntegerOption) o).getValue()) != WindowToken.DEFAULT_FONT_SIZE) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((Integer) ((IntegerOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					case line_extra:
						if (((Integer) ((IntegerOption) o).getValue()) != WindowToken.DEFAULT_LINE_EXTRA) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((Integer) ((IntegerOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					case buffer_size:
						if (((Integer) ((IntegerOption) o).getValue()) != WindowToken.DEFAULT_BUFFER_SIZE) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((Integer) ((IntegerOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					case font_path:
						if (!((String) ((FileOption) o).getValue()).equals(WindowToken.DEFAULT_FONT_PATH)) {
							out.startTag("", "option");
							out.attribute("", "key", key.toString());
							out.text(((String) ((FileOption) o).getValue()).toString());
							out.endTag("", "option");
						}
						break;
					default:
						break;
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
