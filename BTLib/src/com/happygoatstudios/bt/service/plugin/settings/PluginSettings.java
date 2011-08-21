package com.happygoatstudios.bt.service.plugin.settings;

import java.util.HashMap;


import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

public class PluginSettings {
	private String name;
	private int id;
	private String author;
	
	private HashMap<String,AliasData> aliases = null;
	private HashMap<String,TriggerData> triggers = null;
	private HashMap<String,TimerData> timers = null;
	
	//LuaState L = null;
	public PluginSettings() {
		
		name = "";
		id = -1;
		setAuthor("");
		
		setAliases(new HashMap<String,AliasData>());
		setTriggers(new HashMap<String,TriggerData>());
		setTimers(new HashMap<String,TimerData>());
		//L = LuaStateFactory.newLuaState();
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


	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getId() {
		return id;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public String getAuthor() {
		return author;
	}
}
