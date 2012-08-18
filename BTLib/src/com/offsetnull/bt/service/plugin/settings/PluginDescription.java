package com.offsetnull.bt.service.plugin.settings;

public class PluginDescription {
	private String name;
	private String author;
	private String description;
	private int triggers;
	private int aliases;
	private int timers;
	private int scripts;
	private int windows;
	private boolean error;
	private String errorMessage;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getTriggers() {
		return triggers;
	}
	public void setTriggers(int triggers) {
		this.triggers = triggers;
	}
	public int getAliases() {
		return aliases;
	}
	public void setAliases(int aliases) {
		this.aliases = aliases;
	}
	public int getTimers() {
		return timers;
	}
	public void setTimers(int timers) {
		this.timers = timers;
	}
	public int getScripts() {
		return scripts;
	}
	public void setScripts(int scripts) {
		this.scripts = scripts;
	}
	public int getWindows() {
		return windows;
	}
	public void setWindows(int windows) {
		this.windows = windows;
	}
	public void setError(boolean b) {
		this.error = true;
	}
	public void setErrorMessage(String localizedMessage) {
		this.errorMessage = localizedMessage;
	}
	public boolean isError() {
		// TODO Auto-generated method stub
		return error;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
}
