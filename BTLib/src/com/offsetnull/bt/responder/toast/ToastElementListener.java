package com.offsetnull.bt.responder.toast;

import org.xml.sax.Attributes;

import android.sax.StartElementListener;

import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;

public class ToastElementListener implements StartElementListener {
	//PluginSettings settings = null;
	TriggerData current_trigger = null;
	TimerData current_timer = null;
	Object selector = null;
	
	public ToastElementListener(Object selector,TriggerData current_trigger,TimerData current_timer) {
		//this.settings = settings;
		this.current_trigger = current_trigger;
		this.selector = selector;
		this.current_timer = current_timer;
	}

	public void start(Attributes attributes) {
		// TODO Auto-generated method stub
		
	}
}
