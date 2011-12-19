package com.happygoatstudios.bt.timer;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;

import android.sax.ElementListener;

public class TimerElementListener implements ElementListener {

	PluginParser.NewItemCallback callback = null;
	TimerData current_timer = null;
	
	public TimerElementListener(PluginParser.NewItemCallback callback,TimerData current_timer) {
		this.callback = callback;
		this.current_timer = current_timer;
	}
	
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

	public void end() {
		//settings.getTimers().put(current_timer.getName(), current_timer.copy());
		callback.addTimer(current_timer.getName(), current_timer.copy());
	}

}
