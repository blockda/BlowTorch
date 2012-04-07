package com.offsetnull.bt.responder.notification;

import java.math.BigInteger;

import org.xml.sax.Attributes;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponder.FIRE_WHEN;
import com.offsetnull.bt.service.plugin.settings.BasePluginParser;
import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.timer.TimerData;
import com.offsetnull.bt.trigger.TriggerData;

import android.sax.StartElementListener;

public class NotificationElementListener implements StartElementListener {

	//PluginSettings settings = null;
	TriggerData current_trigger = null;
	TimerData current_timer = null;
	Object selector = null;
	
	public NotificationElementListener(Object selector,TriggerData current_trigger,TimerData current_timer) {
		//this.settings = settings;
		this.selector = selector;
		this.current_timer = current_timer;
		this.current_trigger = current_trigger;
	}
		
		//public void start(Attributes a) {
	
	public void start(Attributes a) {
		NotificationResponder r = new NotificationResponder();
		r.setMessage(a.getValue("",BasePluginParser.ATTR_NOTIFICATIONMESSAGE));
		r.setTitle(a.getValue("",BasePluginParser.ATTR_NOTIFICATIONTITLE));
		String fireType = a.getValue("",BasePluginParser.ATTR_FIRETYPE);
		if(fireType == null) fireType = "";
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
		String spawnnew = a.getValue("",BasePluginParser.ATTR_NEWNOTIFICATION);
		if(spawnnew == null) spawnnew = "";
		if(spawnnew.equals("true")) {
			r.setSpawnNewNotification(true);
		} else {
			r.setSpawnNewNotification(false);
		}
		
		String useongoing = a.getValue("",BasePluginParser.ATTR_USEONGOING);
		if(useongoing == null) useongoing = "";
		if(useongoing.equals("true")) {
			r.setUseOnGoingNotification(true);
		} else {
			r.setUseOnGoingNotification(false);
		}
		
		String usedefaultlight = a.getValue("",BasePluginParser.ATTR_USEDEFAULTLIGHT);
		if(usedefaultlight == null) usedefaultlight = "false";
		if(usedefaultlight.equals("true")) {
			r.setUseDefaultLight(true);
			r.setColorToUse( (a.getValue("",BasePluginParser.ATTR_LIGHTCOLOR) == null) ? 0xFFFF0000 : new BigInteger(a.getValue("",BasePluginParser.ATTR_LIGHTCOLOR),16).intValue());
		} else {
			r.setUseDefaultLight(false);
		}
		
		String usedefaultvibrate = a.getValue("",BasePluginParser.ATTR_USEDEFAULTVIBRATE);
		if(usedefaultvibrate == null) usedefaultvibrate = "false";
		if(usedefaultvibrate.equals("true")) {
			r.setUseDefaultVibrate(true);
			r.setVibrateLength( (a.getValue("",BasePluginParser.ATTR_VIBRATELENGTH) == null) ? 0 : Integer.parseInt(a.getValue("",BasePluginParser.ATTR_VIBRATELENGTH)));
		} else {
			r.setUseDefaultVibrate(false);
		}
		
		String usedefaultsound = a.getValue("",BasePluginParser.ATTR_USEDEFAULTSOUND);
		if(usedefaultsound == null) usedefaultsound = "false";
		if(usedefaultsound.equals("true")) {
			r.setUseDefaultSound(true);
			r.setSoundPath(a.getValue("",BasePluginParser.ATTR_SOUNDPATH));
		} else {
			r.setUseDefaultSound(false);
		}
		
		if(selector instanceof TriggerData) {
			current_trigger.getResponders().add(r);
		}
		
		if(selector instanceof TimerData) {
			current_timer.getResponders().add(r);
		}
	}

}
