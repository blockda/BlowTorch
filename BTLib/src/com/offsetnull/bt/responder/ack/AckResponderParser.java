package com.offsetnull.bt.responder.ack;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponder.FIRE_WHEN;
import com.offsetnull.bt.service.plugin.settings.BasePluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.StartElementListener;

public final class AckResponderParser {
	public static void registerListeners(Element root,Object obj,TimerData current_timer,TriggerData current_trigger) {
		Element ack = root.getChild(BasePluginParser.TAG_ACKRESPONDER);
		ack.setStartElementListener(new AckElementListener(new TriggerData(),current_trigger,current_timer));
	}
	
	public static void saveResponderToXML(XmlSerializer out,AckResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_ACKRESPONDER);
		out.attribute("", BasePluginParser.ATTR_ACKWITH, r.getAckWith());
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		out.endTag("",BasePluginParser.TAG_ACKRESPONDER);
	}
}
