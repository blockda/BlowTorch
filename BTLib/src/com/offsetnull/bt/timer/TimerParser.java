package com.offsetnull.bt.timer;

import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.ack.AckResponderParser;
import com.offsetnull.bt.responder.notification.NotificationResponderParser;
import com.offsetnull.bt.responder.script.ScriptResponderParser;
import com.offsetnull.bt.responder.toast.ToastResponderParser;
import com.offsetnull.bt.service.plugin.settings.BasePluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.StartElementListener;

public final class TimerParser {
	public static void registerListeners(Element root,PluginParser.NewItemCallback callback,Object obj,TriggerData current_trigger,TimerData current_timer) {
		Element timer = root.getChild(BasePluginParser.TAG_TIMER);
		//Element timer = timers.getChild(BasePluginParser.TAG_TIMER);
		timer.setElementListener(new TimerElementListener(callback,current_timer));
		
		AckResponderParser.registerListeners(timer, obj, current_timer, current_trigger);
		ToastResponderParser.registerListeners(timer, obj, current_trigger, current_timer);
		NotificationResponderParser.registerListeners(timer, obj, current_trigger, current_timer);
		ScriptResponderParser.registerListeners(timer, obj, current_trigger, current_timer);
		
	}

	public static void saveTimerToXML(XmlSerializer out, TimerData timer) throws IllegalArgumentException, IllegalStateException, IOException {
		//not implemented yet. simple serialization routine.
		out.startTag("", BasePluginParser.TAG_TIMER);
		//out.startTag("", BasePluginParser.TAG_TIMER);
		out.attribute("", BasePluginParser.ATTR_TIMERNAME, timer.getName());
		//out.attribute("", BasePluginParser.ATTR_ORDINAL, timer.getOrdinal().toString());
		out.attribute("", BasePluginParser.ATTR_SECONDS, timer.getSeconds().toString());
		out.attribute("", BasePluginParser.ATTR_REPEAT, (timer.isRepeat()) ? "true" : "false");
		out.attribute("", BasePluginParser.ATTR_PLAYING, (timer.isPlaying()) ? "true" : "false");
		for(TriggerResponder r : timer.getResponders()){
			r.saveResponderToXML(out);
		}
		out.endTag("", BasePluginParser.TAG_TIMER);
	}
}
