package com.happygoatstudios.bt.responder.notification;

import java.io.IOException;
import java.math.BigInteger;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.StartElementListener;

public final class NotificationResponderParser {
	public static void registerListeners(Element root,PluginSettings settings,final Object obj,final TriggerData current_trigger,final TimerData current_timer) {
		Element note = root.getChild(BasePluginParser.TAG_NOTIFICATIONRESPONDER);
		note.setStartElementListener(new StartElementListener() {

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
				
				if(obj instanceof TriggerData) {
					current_trigger.getResponders().add(r);
				}
				
				if(obj instanceof TimerData) {
					current_timer.getResponders().add(r);
				}
			}
			
		});
	}
	
	public static void saveNotificationResponderToXML(XmlSerializer out,NotificationResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_NOTIFICATIONRESPONDER);
		out.attribute("", BasePluginParser.ATTR_NOTIFICATIONTITLE,r.getTitle());
		out.attribute("", BasePluginParser.ATTR_NOTIFICATIONMESSAGE, r.getMessage());
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString() );
		if(r.isUseDefaultSound()) {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTSOUND, "true");
			out.attribute("", BasePluginParser.ATTR_SOUNDPATH, r.getSoundPath());
		} else {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTSOUND, "false");
		}
		
		if(r.isUseDefaultLight()) {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTLIGHT, "true");
			out.attribute("", BasePluginParser.ATTR_LIGHTCOLOR, Integer.toHexString(r.getColorToUse()));
		} else {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTLIGHT, "false");
		}
		
		if(r.isUseDefaultVibrate()) {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTVIBRATE, "true");
			out.attribute("", BasePluginParser.ATTR_VIBRATELENGTH, Integer.toString(r.getVibrateLength()));
		} else {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTVIBRATE, "false");
		}
		
		out.attribute("", BasePluginParser.ATTR_NEWNOTIFICATION, (r.isSpawnNewNotification()) ? "true" : "false");
		out.attribute("", BasePluginParser.ATTR_USEONGOING, (r.isUseOnGoingNotification()) ? "true" : "false");
		
		out.endTag("", BasePluginParser.TAG_NOTIFICATIONRESPONDER);
	}
}
