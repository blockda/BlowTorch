package com.happygoatstudios.bt.service.plugin.settings;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ListOption extends BaseOption implements Parcelable {

	ArrayList<String> items;
	
	public ListOption() {
		type = TYPE.LIST;
		items = new ArrayList<String>();
		this.value = new Integer(0);
	}
	
	public ListOption(Parcel p) {
		type = TYPE.LIST;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		setValue(p.readInt());
		int size = p.readInt();
		items = new ArrayList<String>(size);
		for(int i=0;i<size;i++) {
			items.add(p.readString());
		}
	}

	public void addItem(String item) {
		items.add(item);
	}
	
	public ArrayList<String> getItems() {
		return items;
	}
	
	@Override
	public void setValue(Object o) {
		this.value = (Integer)o;
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return (Object)value;
	}

	@Override
	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultValue(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(title);
		p.writeString(description);
		p.writeString(key);
		p.writeInt((Integer)value);
		p.writeInt(items.size());
		for(int i =0;i<items.size();i++) {
			p.writeString(items.get(i));
		}
	}

	public static final Parcelable.Creator<ListOption> CREATOR = new Parcelable.Creator<ListOption>() {

		public ListOption createFromParcel(Parcel arg0) {
			return new ListOption(arg0);
		}

		public ListOption[] newArray(int arg0) {
			return new ListOption[arg0];
		}
	};
}
