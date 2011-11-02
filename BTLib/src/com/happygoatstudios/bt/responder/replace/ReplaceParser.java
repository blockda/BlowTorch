package com.happygoatstudios.bt.responder.replace;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.TextElementListener;
import android.util.Log;

public final class ReplaceParser {
	public static void registerListeners(Element root,PluginSettings settings,TriggerData current_trigger) {
		Element r = root.getChild(BasePluginParser.TAG_REPLACERESPONDER);
		r.setTextElementListener(new ReplaceElementListener(settings,current_trigger));
	}

	public static void saveReplaceResponderToXML(XmlSerializer out,
			ReplaceResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_REPLACERESPONDER);
		
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		if(r.getRetarget() != null) {
			out.attribute("", BasePluginParser.ATTR_RETARGET, r.getRetarget());
			//out.attribute("", BasePluginParser.ATTR_DESTINATION, r.getWindowTarget());
		}
		out.text(r.getWith());
		
		out.endTag("", BasePluginParser.TAG_REPLACERESPONDER);
		
	}
}
