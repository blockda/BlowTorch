package com.happygoatstudios.bt.launcher;

public interface ReadyListener {
	//public void ready(String displayname,String host,String port);
	//public void modify(String displayname,String host,String port,MudConnection old);
	public void ready(MudConnection newData);
	public void modify(MudConnection old, MudConnection newData);
}
