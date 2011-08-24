package com.happygoatstudios.bt.trigger;

import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponderParser;
import com.happygoatstudios.bt.responder.color.ColorActionParser;
import com.happygoatstudios.bt.responder.gag.GagActionParser;
import com.happygoatstudios.bt.responder.notification.NotificationResponderParser;
import com.happygoatstudios.bt.responder.replace.ReplaceParser;
import com.happygoatstudios.bt.responder.script.ScriptResponderParser;
import com.happygoatstudios.bt.responder.toast.ToastResponderParser;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.StartElementListener;

public final class TriggerParser {
	public static void registerListeners(Element root,final PluginSettings settings,final Object obj,final TriggerData current_trigger,final TimerData current_timer) {
		Element trigger = root.getChild(BasePluginParser.TAG_TRIGGER);
		trigger.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				// TODO Auto-generated method stub
				current_trigger.setName(a.getValue("",BasePluginParser.ATTR_TRIGGERTITLE));
				current_trigger.setPattern(a.getValue("",BasePluginParser.ATTR_TRIGGERPATTERN));
				current_trigger.setInterpretAsRegex( a.getValue("",BasePluginParser.ATTR_TRIGGERLITERAL).equals("true") ? true : false);
				current_trigger.setFireOnce(a.getValue("",BasePluginParser.ATTR_TRIGGERONCE).equals("true") ? true : false);
				current_trigger.setHidden( (a.getValue("",BasePluginParser.ATTR_TRIGGERHIDDEN) == null) ? false : (a.getValue("",BasePluginParser.ATTR_TRIGGERHIDDEN)).equals("true") ? true : false);
				current_trigger.setEnabled( (a.getValue("",BasePluginParser.ATTR_TRIGGERENEABLED) == null) ? true : (a.getValue("",BasePluginParser.ATTR_TRIGGERENEABLED)).equals("true") ? true : false);
				current_trigger.setSequence( (a.getValue("",BasePluginParser.ATTR_SEQUENCE) == null) ? 10 : Integer.parseInt(a.getValue("",BasePluginParser.ATTR_SEQUENCE)));
				current_trigger.setGroup( (a.getValue("",BasePluginParser.ATTR_GROUP) == null) ? "" : a.getValue("",BasePluginParser.ATTR_GROUP));
				current_trigger.setKeepEvaluating((a.getValue("",BasePluginParser.ATTR_KEEPEVALUATING) == null) ? true : ("true".equals(a.getValue("",BasePluginParser.ATTR_KEEPEVALUATING))) ? true : false);
				current_trigger.setResponders(new ArrayList<TriggerResponder>());
				
			}
			
		});
		
		trigger.setEndElementListener(new EndElementListener() {

			public void end() {
				settings.getTriggers().put(current_trigger.getName(), current_trigger.copy());
			}
		
		});
		
		AckResponderParser.registerListeners(trigger, settings, obj, current_timer, current_trigger);
		ToastResponderParser.registerListeners(trigger, settings, obj, current_trigger, current_timer);
		NotificationResponderParser.registerListeners(trigger, settings, obj, current_trigger, current_timer);
		ScriptResponderParser.registerListeners(trigger, settings, obj, current_trigger, current_timer);
		ReplaceParser.registerListeners(trigger, settings, new TriggerData(), current_trigger);
		ColorActionParser.registerListeners(trigger, settings, current_trigger);
		GagActionParser.registerListeners(trigger, settings, current_trigger);
		
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
