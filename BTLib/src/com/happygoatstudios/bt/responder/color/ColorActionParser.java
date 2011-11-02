package com.happygoatstudios.bt.responder.color;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.TextElementListener;

public final class ColorActionParser {
	public static void registerListeners(Element root,PluginSettings settings,TriggerData current_trigger) {
		Element color = root.getChild(BasePluginParser.TAG_COLORACTION);
		color.setTextElementListener(new ColorElementListener(settings,current_trigger));
	}
	
	public static void saveColorActionToXML(XmlSerializer out,ColorAction r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_COLORACTION);
		out.text(Integer.toString(r.getColor()));
		out.endTag("", BasePluginParser.TAG_COLORACTION);
	}
}
