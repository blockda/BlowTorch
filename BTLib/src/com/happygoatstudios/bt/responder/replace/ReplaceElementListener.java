package com.happygoatstudios.bt.responder.replace;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.TextElementListener;

public class ReplaceElementListener implements TextElementListener{

	//PluginSettings settings = null;
	TriggerData current_trigger = null;
	private ReplaceResponder r = new ReplaceResponder();
	public ReplaceElementListener(TriggerData current_trigger) {
		//this.settings = settings;
		this.current_trigger = current_trigger;
	}
	
	public void start(Attributes a) {
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

	public void end(String body) {
		// TODO Auto-generated method stub
		r.setWith(body);
		current_trigger.getResponders().add(r.copy());
	}

}
