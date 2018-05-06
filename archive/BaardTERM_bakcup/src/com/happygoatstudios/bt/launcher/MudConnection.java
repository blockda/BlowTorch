package com.happygoatstudios.bt.launcher;

public class MudConnection {

	private String displayname;
	private String hostname;
	private String port;
	
	public String getDisplayName() {
		return displayname;
	}
	
	public String getHostName() {
		return hostname;
	}
	
	public String getPortString() {
		return port;
	}
	
	public void setDisplayName(String in) {
		displayname = in;
	}
	
	public void setHostName(String in) {
		hostname = in;
	}
	
	public void setPortString(String in) {
		port = in;
	}
}
