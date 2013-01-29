package com.offsetnull.bt.service.plugin.settings;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.os.Parcel;
import android.os.Parcelable;

public class CallbackOption extends BaseOption implements Parcelable {

	//protected ArrayList<String> items;
	
	public CallbackOption() {
		type = TYPE.CALLBACK;
		//items = new ArrayList<String>();
		this.value = new String();
	}
	
	public CallbackOption(Parcel p) {
		type = TYPE.CALLBACK;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		setValue(p.readString());
		//int size = p.readInt();
		
	}
	
	public CallbackOption copy() {
		CallbackOption tmp = new CallbackOption();
		tmp.key = this.key;
		tmp.value = this.value;
		tmp.title = this.title;
		tmp.description = this.description;
		
		return tmp;
	}
	
	public void reset() {
		this.key = "";
		this.value = new String();
		this.description = "";
		this.title = "";
		//this.items.clear();
	}
	
	@Override
	public void setValue(Object o) {
		if(o instanceof String) {
			value = (String)o;
		} else if(o instanceof String) {
			value = o.toString();
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
		p.writeString(title);
		p.writeString(description);
		p.writeString(key);
		p.writeString((String)value);
		
	}

	public static final Parcelable.Creator<CallbackOption> CREATOR = new Parcelable.Creator<CallbackOption>() {

		public CallbackOption createFromParcel(Parcel arg0) {
			return new CallbackOption(arg0);
		}

		public CallbackOption[] newArray(int arg0) {
			return new CallbackOption[arg0];
		}
	};
	
	public void saveToXML(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", "callback");
		out.attribute("", "key", this.key);
		out.attribute("", "title", this.title);
		out.attribute("", "summary", this.description);
		//out.attribute("","value",Integer.toString((Integer)this.value));
		//out.startTag("", "value");
		out.text((String)this.value);
		//out.endTag("", "value");
		//have to save the list items.
		
		
		out.endTag("", "callback");
	}
}


