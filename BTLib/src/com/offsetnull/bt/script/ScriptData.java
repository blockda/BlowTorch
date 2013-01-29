package com.offsetnull.bt.script;

public class ScriptData {
	private String name;
	private boolean execute;
	private String data;
	
	public ScriptData() {
		name = "";
		execute = false;
		data = "";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isExecute() {
		return execute;
	}
	public void setExecute(boolean execute) {
		this.execute = execute;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
