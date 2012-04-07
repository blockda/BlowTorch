package com.offsetnull.bt.launcher;

public class MudConnection {

	private String displayname;
	private String hostname;
	private String port;
	private String lastPlayed = "never";
	private boolean connected = false;
	public MudConnection copy() {
		MudConnection tmp = new MudConnection();
		
		tmp.displayname = this.displayname;
		tmp.hostname = this.hostname;
		tmp.port = this.port;
		tmp.lastPlayed = this.lastPlayed;
		
		
		return tmp;
	}
	
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
		if(!test.getLastPlayed().equals(this.getLastPlayed())) return false;
		return true;
	}

	public void setLastPlayed(String lastPlayed) {
		this.lastPlayed = lastPlayed;
	}

	public String getLastPlayed() {
		return lastPlayed;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isConnected() {
		return connected;
	}
}
