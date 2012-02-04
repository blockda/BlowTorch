package com.happygoatstudios.bt.service.plugin.settings;

import java.util.HashMap;


import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.service.WindowToken;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;

public class PluginSettings implements Cloneable {
	private String name;
	private int id;
	private String author;
	
	public enum PLUGIN_LOCATION {
		INTERNAL,
		EXTERNAL
	}
	
	private String path = null;
	
	private PLUGIN_LOCATION locationType = PLUGIN_LOCATION.INTERNAL;
	
	private HashMap<String,AliasData> aliases = null;
	private HashMap<String,TriggerData> triggers = null;
	private HashMap<String,TimerData> timers = null;
	private HashMap<String,String> scripts = null;
	
	private HashMap<String,WindowToken> windows = null;
	
	//LuaState L = null;
	public PluginSettings() {
		
		name = "";
		id = -1;
		setAuthor("");
		
		setAliases(new HashMap<String,AliasData>());
		setTriggers(new HashMap<String,TriggerData>());
		setTimers(new HashMap<String,TimerData>());
		setScripts(new HashMap<String,String>());
		setWindows(new HashMap<String,WindowToken>());
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


	public void setScripts(HashMap<String,String> scripts) {
		this.scripts = scripts;
	}


	public HashMap<String,String> getScripts() {
		return scripts;
	}


	public void setLocationType(PLUGIN_LOCATION locationType) {
		this.locationType = locationType;
	}


	public PLUGIN_LOCATION getLocationType() {
		return locationType;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getPath() {
		return path;
	}


	public void setWindows(HashMap<String,WindowToken> windows) {
		this.windows = windows;
	}


	public HashMap<String,WindowToken> getWindows() {
		return windows;
	}
}
