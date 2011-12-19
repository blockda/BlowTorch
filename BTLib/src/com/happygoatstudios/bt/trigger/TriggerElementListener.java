package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;

import android.sax.ElementListener;
import android.sax.StartElementListener;

public class TriggerElementListener implements ElementListener {

	//PluginSettings settings = null;
	PluginParser.NewItemCallback callback = null;
	TriggerData current_trigger = null;
	public TriggerElementListener(PluginParser.NewItemCallback callback,TriggerData current_trigger) {
		//this.settings = settings;
		this.current_trigger = current_trigger;
		this.callback = callback;
	}
	
	public void start(Attributes a) {
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

	public void end() {
		//settings.getTriggers().put(current_trigger.getName(), current_trigger.copy());
		callback.addTrigger(current_trigger.getName(), current_trigger.copy());
	}

}
