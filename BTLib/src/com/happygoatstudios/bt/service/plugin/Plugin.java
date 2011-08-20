package com.happygoatstudios.bt.service.plugin;

import java.util.HashMap;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

public class Plugin {
	//we are a lua plugin.
	//we can give users 
	private PluginSettings settings = null;
	
	public Plugin() {
		settings = new PluginSettings();
	}
	
	
}
