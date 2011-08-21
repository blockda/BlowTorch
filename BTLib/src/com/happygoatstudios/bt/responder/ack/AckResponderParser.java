package com.happygoatstudios.bt.responder.ack;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.StartElementListener;

public final class AckResponderParser {
	public static void registerListeners(Element root,final PluginSettings settings,final Object obj,final TimerData current_timer,final TriggerData current_trigger) {
		Element ack = root.getChild(BasePluginParser.TAG_ACKRESPONDER);
		ack.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				AckResponder r = new AckResponder();
				r.setAckWith(a.getValue("", BasePluginParser.ATTR_ACKWITH));
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
				
				if(obj instanceof TriggerData) {
					current_trigger.getResponders().add(r.copy());
				} else if(obj instanceof TimerData) {
					current_timer.getResponders().add(r.copy());
				}
			}
			
		});
	}
	
	public static void saveResponderToXML(XmlSerializer out,AckResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.ATTR_ACKWITH);
		out.attribute("", BasePluginParser.ATTR_ACKWITH, r.getAckWith());
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		out.endTag("",BasePluginParser.ATTR_ACKWITH);
	}
}
