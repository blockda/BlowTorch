package com.happygoatstudios.bt.responder.gag;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.StartElementListener;

public class GagElementListener implements StartElementListener {

	PluginSettings settings = null;
	TriggerData current_trigger = null;
	
	public GagElementListener(PluginSettings settings,TriggerData current_trigger) {
		this.settings = settings;
		this.current_trigger = current_trigger;
	}
	
	public void start(Attributes a) {
		GagAction tmp = new GagAction();
		tmp.setRetarget(a.getValue("",BasePluginParser.ATTR_RETARGET));
		tmp.setGagLog((a.getValue("",BasePluginParser.ATTR_GAGLOG) == null) ? GagAction.DEFAULT_GAGLOG : (a.getValue("",BasePluginParser.ATTR_GAGLOG).equals("false")) ? false : true);
		tmp.setGagOutput((a.getValue("",BasePluginParser.ATTR_GAGOUTPUT) == null) ? GagAction.DEFAULT_GAGOUTPUT : (a.getValue("",BasePluginParser.ATTR_GAGOUTPUT).equals("false")) ? false : true);
		current_trigger.getResponders().add(tmp.copy());
	}

}
