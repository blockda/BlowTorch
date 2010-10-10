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
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof MudConnection)) return false;
		
		MudConnection test = (MudConnection)o;
		
		if(!test.getDisplayName().equals(this.getDisplayName())) return false;
		if(!test.getHostName().equals(this.getHostName())) return false;
		if(!test.getPortString().equals(this.getPortString())) return false;
		return true;
	}
}
