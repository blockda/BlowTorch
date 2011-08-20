package com.happygoatstudios.bt.service.plugin.settings;

import java.util.HashMap;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

public class PluginSettings {
	private HashMap<String,AliasData> aliases = null;
	private HashMap<String,TriggerData> triggers = null;
	private HashMap<String,TimerData> timers = null;
	
	LuaState L = null;
	public PluginSettings() {
		setAliases(new HashMap<String,AliasData>());
		setTriggers(new HashMap<String,TriggerData>());
		setTimers(new HashMap<String,TimerData>());
		L = LuaStateFactory.newLuaState();
	}
	
	
	public void setTimers(HashMap<String,TimerData> timers) {
		this.timers = timers;
	}
	public HashMap<String,TimerData> getTimers() {
		return timers;
	}
	public void setTriggers(HashMap<String,TriggerData> triggers) {
		this.triggers = triggers;
	}
	public HashMap<String,TriggerData> getTriggers() {
		return triggers;
	}
	public void setAliases(HashMap<String,AliasData> aliases) {
		this.aliases = aliases;
	}
	public HashMap<String,AliasData> getAliases() {
		return aliases;
	}
}
