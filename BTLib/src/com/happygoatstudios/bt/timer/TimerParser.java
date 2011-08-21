package com.happygoatstudios.bt.timer;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponderParser;
import com.happygoatstudios.bt.responder.notification.NotificationResponderParser;
import com.happygoatstudios.bt.responder.script.ScriptResponderParser;
import com.happygoatstudios.bt.responder.toast.ToastResponderParser;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.StartElementListener;

public final class TimerParser {
	public static void registerListeners(Element root,final PluginSettings settings,final Object obj,final TriggerData current_trigger,final TimerData current_timer) {
		Element timer = root.getChild(BasePluginParser.TAG_TIMER);
		timer.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				current_timer.setName((a.getValue("",BasePluginParser.ATTR_TIMERNAME)==null) ? "" : a.getValue("",BasePluginParser.ATTR_TIMERNAME));
				current_timer.setOrdinal((a.getValue("",BasePluginParser.ATTR_ORDINAL)==null) ? 0 : Integer.parseInt(a.getValue("",BasePluginParser.ATTR_ORDINAL)));
				//if(a.getValue("",ATTR_SECONDS) == null) {
					//Log.e("PARSER","SECONDS ATTRIBUTE NOT FOUND, DEFAULTING");
				//} else {
					//Log.e("PARSER","SECONDS ATTRIBUTE CONTAINS " + a.getValue("",ATTR_SECONDS));
				//}
				current_timer.setSeconds((a.getValue("",BasePluginParser.ATTR_SECONDS) == null) ? 30 : Integer.parseInt(a.getValue("",BasePluginParser.ATTR_SECONDS)));
				//Log.e("PARSER","SECONDS IN CONTAINER IS NOW " + current_timer.getSeconds().toString());
				current_timer.setRepeat((a.getValue("",BasePluginParser.ATTR_REPEAT) == null) ? false : a.getValue("",BasePluginParser.ATTR_REPEAT).equals("true") ? true : false);
				current_timer.setPlaying((a.getValue("",BasePluginParser.ATTR_PLAYING) == null) ? false : a.getValue("",BasePluginParser.ATTR_PLAYING).equals("true") ? true : false);
				current_timer.setResponders(new ArrayList<TriggerResponder>());
			
				
			}
			
		});
		
		timer.setEndElementListener(new EndElementListener() {

			public void end() {
				settings.getTimers().put(current_timer.getName(), current_timer.copy());
			}
			
		});
		
		AckResponderParser.registerListeners(timer, settings, obj, current_timer, current_trigger);
		ToastResponderParser.registerListeners(timer, settings, obj, current_trigger, current_timer);
		NotificationResponderParser.registerListeners(timer, settings, obj, current_trigger, current_timer);
		ScriptResponderParser.registerListeners(timer, settings, obj, current_trigger, current_timer);
	
	}
}
