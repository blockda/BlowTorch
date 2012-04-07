package com.offsetnull.bt.trigger;

import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.ack.AckResponderParser;
import com.offsetnull.bt.responder.color.ColorActionParser;
import com.offsetnull.bt.responder.gag.GagActionParser;
import com.offsetnull.bt.responder.notification.NotificationResponderParser;
import com.offsetnull.bt.responder.replace.ReplaceParser;
import com.offsetnull.bt.responder.script.ScriptResponderParser;
import com.offsetnull.bt.responder.toast.ToastResponderParser;
import com.offsetnull.bt.service.plugin.settings.BasePluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.timer.TimerData;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.StartElementListener;

public final class TriggerParser {
	public static void registerListeners(Element root,PluginParser.NewItemCallback callback,Object obj,TriggerData current_trigger,TimerData current_timer) {
		//Element triggers = root.getChild("triggers");
		Element trigger = root.getChild(BasePluginParser.TAG_TRIGGER);
		TriggerElementListener listener = new TriggerElementListener(callback,current_trigger);
		
		trigger.setElementListener(listener);
		//trigger.sete
		
		AckResponderParser.registerListeners(trigger, obj, current_timer, current_trigger);
		ToastResponderParser.registerListeners(trigger, obj, current_trigger, current_timer);
		NotificationResponderParser.registerListeners(trigger, obj, current_trigger, current_timer);
		ScriptResponderParser.registerListeners(trigger, obj, current_trigger, current_timer);
		ReplaceParser.registerListeners(trigger, current_trigger);
		ColorActionParser.registerListeners(trigger, current_trigger);
		GagActionParser.registerListeners(trigger, current_trigger);
		
	}
	
	public static void saveTriggerToXML(XmlSerializer out,TriggerData trigger) throws IllegalArgumentException, IllegalStateException, IOException {
		if(trigger.isSave()) {
			out.startTag("", BasePluginParser.TAG_TRIGGER);
			out.attribute("", BasePluginParser.ATTR_TRIGGERTITLE, trigger.getName());
			out.attribute("", BasePluginParser.ATTR_TRIGGERPATTERN, trigger.getPattern());
			out.attribute("", BasePluginParser.ATTR_TRIGGERLITERAL, trigger.isInterpretAsRegex() ? "true" : "false");
			out.attribute("", BasePluginParser.ATTR_TRIGGERONCE, trigger.isFireOnce() ? "true" : "false");
			if(trigger.isHidden())  out.attribute("", BasePluginParser.ATTR_TRIGGERHIDDEN, "true");
			out.attribute("", BasePluginParser.ATTR_TRIGGERENEABLED, trigger.isEnabled() ? "true" : "false");
			out.attribute("", BasePluginParser.ATTR_SEQUENCE, Integer.toString(trigger.getSequence()));
			if(!trigger.getGroup().equals(TriggerData.DEFAULT_GROUP)) out.attribute("", BasePluginParser.ATTR_GROUP, trigger.getGroup());
			out.attribute("", BasePluginParser.ATTR_KEEPEVALUATING, trigger.isKeepEvaluating() ? "true" : "false");
			
			for(TriggerResponder r : trigger.getResponders()){
				r.saveResponderToXML(out);
			}
			//OutputResponders(out,trigger.getResponders());
			out.endTag("", BasePluginParser.TAG_TRIGGER);
		}
	}
	
	
}
