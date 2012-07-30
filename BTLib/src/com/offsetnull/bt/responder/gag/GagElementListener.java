package com.offsetnull.bt.responder.gag;

import org.xml.sax.Attributes;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.service.plugin.settings.BasePluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.trigger.TriggerData;

import android.sax.StartElementListener;

public class GagElementListener implements StartElementListener {

	//PluginSettings settings = null;
	TriggerData current_trigger = null;
	
	public GagElementListener(TriggerData current_trigger) {
		//this.settings = settings;
		this.current_trigger = current_trigger;
	}
	
	public void start(Attributes a) {
		GagAction tmp = new GagAction();
		
		String fireType = a.getValue("",BasePluginParser.ATTR_FIRETYPE);
		if(fireType==null) fireType="";
		
		if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
			tmp.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
		} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
			tmp.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
		} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
			tmp.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
		} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
			tmp.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_NEVER);
		} else {
			tmp.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
		}
		
		tmp.setRetarget(a.getValue("",BasePluginParser.ATTR_RETARGET));
		tmp.setGagLog((a.getValue("",BasePluginParser.ATTR_GAGLOG) == null) ? GagAction.DEFAULT_GAGLOG : (a.getValue("",BasePluginParser.ATTR_GAGLOG).equals("false")) ? false : true);
		tmp.setGagOutput((a.getValue("",BasePluginParser.ATTR_GAGOUTPUT) == null) ? GagAction.DEFAULT_GAGOUTPUT : (a.getValue("",BasePluginParser.ATTR_GAGOUTPUT).equals("false")) ? false : true);
		current_trigger.getResponders().add(tmp.copy());
	}

}
