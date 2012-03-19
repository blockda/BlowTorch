package com.happygoatstudios.bt.service.plugin.settings;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.os.Parcel;
import android.os.Parcelable;

public class BooleanOption extends BaseOption implements Parcelable {


	
	public BooleanOption() {
		type = TYPE.BOOLEAN;
	}
	
	public BooleanOption(Parcel p) {
		type = TYPE.BOOLEAN;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		setValue((p.readInt() == 1) ? true : false);
	}

	@Override
	public void setValue(Object o) {
		if(o instanceof Boolean) {
			this.value = (Boolean)o;
		} else if(o instanceof String) {
			String str = (String)o;
			if(str.equals("true")) {
				value = (Boolean)true;
			} else if(str.equals("false")) {
				value = (Boolean)false;
			}
		}
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Object o) {
		if(o instanceof Boolean) {
			defaultValue = (Boolean)o;
		}
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
		if((Boolean)value == true) {
			p.writeInt(1);
		} else {
			p.writeInt(0);
		}
	}

	public static final Parcelable.Creator<BooleanOption> CREATOR = new Parcelable.Creator<BooleanOption>() {

		public BooleanOption createFromParcel(Parcel arg0) {
			return new BooleanOption(arg0);
		}

		public BooleanOption[] newArray(int arg0) {
			return new BooleanOption[arg0];
		}
	};
	
	public void saveToXML(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", "boolean");
		out.attribute("", "key", this.key);
		out.attribute("", "title", this.title);
		out.attribute("", "summary", this.description);
		out.text(((Boolean)this.value).toString());
		out.endTag("", "boolean");
	}
}
