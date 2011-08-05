package com.happygoatstudios.bt.service;

import java.util.ArrayList;

public class StatusBarWatchList {
	public ArrayList<String> data = null;
	public StatusBarWatchList() {
		data = new ArrayList<String>();
		data.add("char.maxstats.maxhp");
		data.add("char.vitals.hp");
		data.add("char.maxstats.maxmana");
		data.add("char.vitals.mana");
		data.add("char.status.enemy");
		data.add("char.status.enemypct");
	}
}
