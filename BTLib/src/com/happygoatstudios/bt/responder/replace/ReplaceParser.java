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
	public static void registerListeners(Element root,final PluginSettings settings,final Object obj,final TriggerData current_trigger) {
		Element r = root.getChild(BasePluginParser.TAG_REPLACERESPONDER);
		r.setTextElementListener(new TextElementListener() {
			private ReplaceResponder r = new ReplaceResponder();
			public void start(Attributes a) {
				//if( != null && a.getValue("",BasePluginParser.ATTR_DESTINATION) != null) {
					r.setRetarget(a.getValue("",BasePluginParser.ATTR_RETARGET));

				
				String fireType = a.getValue("",BasePluginParser.ATTR_FIRETYPE);
				if(fireType == null) fireType = "";
				//Log.e("PARSER","ACK TAG READ, FIRETYPE IS:" + fireType);
				if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
					r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
				} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
					r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
				} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
					r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
					r.setFireType(FIRE_WHEN.WINDOW_NEVER);
				} else {
					r.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				}
			}

			public void end(String arg0) {
				//ReplaceResponder r = new ReplaceResponder();
				r.setWith(arg0);
				current_trigger.getResponders().add(r.copy());
			}
			
		});
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
