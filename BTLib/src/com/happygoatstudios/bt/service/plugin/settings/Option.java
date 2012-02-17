package com.happygoatstudios.bt.service.plugin.settings;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Option implements Parcelable {
	public enum TYPE {
		BOOLEAN,
		LIST,
		GROUP,
		ENCODING, INTEGER, COLOR, FILE
	}
	
	protected String title;
	protected int id;
	protected String description;
	protected TYPE type;
	protected String key;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	

}
