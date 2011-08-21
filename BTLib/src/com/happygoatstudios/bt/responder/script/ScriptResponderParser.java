package com.happygoatstudios.bt.responder.script;

import java.io.IOException;

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

public class ScriptResponderParser {
	public static void registerListeners(Element root,PluginSettings settings,final Object obj,final TriggerData current_trigger,final TimerData current_timer) {
		Element script = root.getChild(BasePluginParser.TAG_SCRIPTRESPONDER);
		script.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				ScriptResponder scr = new ScriptResponder();
				scr.setFunction(a.getValue("",BasePluginParser.ATTR_FUNCTION));
				
				String fireType = a.getValue("",BasePluginParser.ATTR_FIRETYPE);
				if(fireType==null) fireType="";
				
				if(fireType.equals(TriggerResponder.FIRE_WINDOW_OPEN)) {
					scr.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_OPEN);
				} else if (fireType.equals(TriggerResponder.FIRE_WINDOW_CLOSED)) {
					scr.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_CLOSED);
				} else if (fireType.equals(TriggerResponder.FIRE_ALWAYS)) {
					scr.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				} else if (fireType.equals(TriggerResponder.FIRE_NEVER)) {
					scr.setFireType(FIRE_WHEN.WINDOW_NEVER);
				} else {
					scr.setFireType(TriggerResponder.FIRE_WHEN.WINDOW_BOTH);
				}
				
				if(obj instanceof TriggerData) {
					current_trigger.getResponders().add(scr);
				}
				
				if(obj instanceof TimerData) {
					current_timer.getResponders().add(scr);
				}
			}
			
		});
	}
	
	public static void saveScriptResponderToXML(XmlSerializer out,ScriptResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_SCRIPTRESPONDER);
		out.attribute("", BasePluginParser.ATTR_FUNCTION, r.getFunction());
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		out.endTag("", BasePluginParser.TAG_SCRIPTRESPONDER);
	}
}
