package com.happygoatstudios.bt.service;

import java.util.ArrayList;

public class StatusBarWatchList {
	public ArrayList<String> data = null;
	public StatusBarWatchList() {
		data = new ArrayList<String>();
		data.add("char.vitals");
		data.add("char.maxstats");
		data.add("char.status");
		data.add("room.info");
	}
}
