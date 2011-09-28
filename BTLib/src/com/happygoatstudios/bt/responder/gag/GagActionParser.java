package com.happygoatstudios.bt.responder.gag;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.trigger.TriggerData;

import android.sax.Element;
import android.sax.StartElementListener;

public final class GagActionParser {
	public static void registerListeners(Element root,final PluginSettings settings,final TriggerData current_trigger) {
		Element gag = root.getChild(BasePluginParser.TAG_GAGACTION);
		gag.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				GagAction tmp = new GagAction();
				tmp.setRetarget(a.getValue("",BasePluginParser.ATTR_RETARGET));
				tmp.setGagLog((a.getValue("",BasePluginParser.ATTR_GAGLOG) == null) ? GagAction.DEFAULT_GAGLOG : (a.getValue("",BasePluginParser.ATTR_GAGLOG).equals("false")) ? false : true);
				tmp.setGagOutput((a.getValue("",BasePluginParser.ATTR_GAGOUTPUT) == null) ? GagAction.DEFAULT_GAGOUTPUT : (a.getValue("",BasePluginParser.ATTR_GAGOUTPUT).equals("false")) ? false : true);
				current_trigger.getResponders().add(tmp.copy());
			}
			
		});
	}
	
	public static void saveGagActionToXML(XmlSerializer out,GagAction r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_GAGACTION);
		if(r.getRetarget() != null) {
			out.attribute("", BasePluginParser.ATTR_RETARGET, r.getRetarget());
		}
		
		if(r.isGagLog()) {
			
		} else {
			out.attribute("", BasePluginParser.ATTR_GAGLOG, "false");
		}
		
		if(r.isGagOutput()) {
			
		} else {
			out.attribute("", BasePluginParser.ATTR_GAGOUTPUT, "false");
		}
		if(r.getFireType() != FIRE_WHEN.WINDOW_BOTH) {
			out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		}
		out.endTag("", BasePluginParser.TAG_GAGACTION);
	}
}
