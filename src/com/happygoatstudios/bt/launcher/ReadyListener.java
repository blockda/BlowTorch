package com.happygoatstudios.bt.launcher;

public interface ReadyListener {
	public void ready(String displayname,String host,String port);
	public void modify(String displayname,String host,String port,MudConnection old);
}
