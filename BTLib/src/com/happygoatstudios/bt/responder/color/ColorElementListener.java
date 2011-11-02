package com.happygoatstudios.bt.responder.color;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.StartElementListener;
import android.sax.TextElementListener;

public class ColorElementListener implements TextElementListener{

	PluginSettings settings = null;
	TriggerData current_trigger = null;
	//TimerData current_timer = null;
	//Object selector = null;
	
	public ColorElementListener(PluginSettings settings,TriggerData current_trigger) {
		this.settings = settings;
		//this.selector = selector;
		//this.current_timer = current_timer;
		this.current_trigger = current_trigger;
	}
	
	public void start(Attributes a) {
	
	}

	public void end(String body) {
		ColorAction tmp = new ColorAction();
		if(body != null && body.length() > 0) {
			tmp.setColor(Integer.parseInt(body));
		}
		current_trigger.getResponders().add(tmp.copy());
	
	}

}
