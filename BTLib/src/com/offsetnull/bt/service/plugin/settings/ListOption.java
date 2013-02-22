package com.offsetnull.bt.service.plugin.settings;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ListOption extends BaseOption implements Parcelable {

	protected ArrayList<String> items;
	
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
	
	public ListOption copy() {
		ListOption tmp = new ListOption();
		tmp.key = this.key;
		tmp.value = this.value;
		tmp.title = this.title;
		tmp.description = this.description;
		
		tmp.items = new ArrayList<String>();
		for(String item : this.items) {
			tmp.items.add(item);
		}
		
		return tmp;
	}
	
	public void reset() {
		this.key = "";
		this.value = new Object();
		this.description = "";
		this.title = "";
		this.items.clear();
	}

	public void addItem(String item) {
		items.add(item);
	}
	
	public ArrayList<String> getItems() {
		return items;
	}
	
	@Override
	public void setValue(Object o) {
		if(o instanceof Integer) {
			value = (Integer)o;
		} else if(o instanceof String) {
			try {
				int num = Integer.parseInt((String)o);
				value = (Integer)num;
			} catch(NumberFormatException e) {
				
			}
		}
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
		Log.e("FKDSL","PARCELLING LIST OPTION: "+key);
		
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
	
	public void saveToXML(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", "list");
		out.attribute("", "key", this.key);
		out.attribute("", "title", this.title);
		out.attribute("", "summary", this.description);
		//out.attribute("","value",Integer.toString((Integer)this.value));
		out.startTag("", "value");
		out.text(Integer.toString((Integer)this.value));
		out.endTag("", "value");
		//have to save the list items.
		for(String item : items) {
			out.startTag("", "item");
			out.text(item);
			out.endTag("", "item");
		}
		
		out.endTag("", "list");
	}
}
