package com.offsetnull.bt.service.plugin.settings;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.os.Parcel;
import android.os.Parcelable;

public class FileOption extends ListOption implements Parcelable {

	ArrayList<String> paths;
	ArrayList<String> extensions;
	//ArrayList<String> items;
	
	public FileOption() {
		//super();
		this.type = TYPE.FILE;
		paths = new ArrayList<String>(0);
		extensions = new ArrayList<String>(0);
		items = new ArrayList<String>(0);
		setValue("");
	}
	
	public FileOption(Parcel p) {
		type = TYPE.FILE;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		setValue(p.readString());
		
		int itemcount = p.readInt();
		items = new ArrayList<String>(itemcount);
		for(int i =0;i<itemcount;i++) {
			items.add(p.readString());
		}
		
		int pathcount = p.readInt();
		paths = new ArrayList<String>(pathcount);
		for(int i=0;i<pathcount;i++) {
			paths.add(p.readString());
		}
		
		int extcount = p.readInt();
		extensions = new ArrayList<String>(extcount);
		for(int i=0;i<extcount;i++) {
			extensions.add(p.readString());
		}
		
	}
	
	public void addPath(String path) {
		paths.add(path);
	}
	
	public void addExtension(String extension) {
		extensions.add(extension);
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
		p.writeInt(items.size());
		for(int i=0;i<items.size();i++) {
			p.writeString(items.get(i));
		}
		
		p.writeInt(paths.size());
		for(int i=0;i<paths.size();i++)  {
			p.writeString(paths.get(i));
		}
		
		p.writeInt(extensions.size());
		for(int i=0;i<extensions.size();i++) {
			p.writeString(extensions.get(i));
		}
	}

	@Override
	public void setValue(Object o) {
		if(o instanceof String) {
			value = (String)o;
		} else {
			//dunno.
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

	public static final Parcelable.Creator<FileOption> CREATOR = new Parcelable.Creator<FileOption>() {

		public FileOption createFromParcel(Parcel arg0) {
			return new FileOption(arg0);
		}

		public FileOption[] newArray(int arg0) {
			return new FileOption[arg0];
		}
	};
	
	@Override
	public void saveToXML(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		//this is a different xml serializer routine than the list option.
		out.startTag("", "file");
		out.attribute("", "key", this.key);
		out.attribute("", "title", this.title);
		out.attribute("", "summary", this.description);
		//out.attribute("", "value", (String)this.value);
		out.startTag("", "value");
		out.text((String)this.value);
		out.endTag("", "value");
		for(String path : paths) {
			out.startTag("", "path");
			out.text(path);
			out.endTag("", "path");
		}
		
		for(String ext : extensions) {
			out.startTag("", "extension");
			out.text(ext);
			out.endTag("", "extension");
		}
		out.endTag("", "file");
	}
	
	@Override
	public FileOption copy() {
		FileOption tmp = new FileOption();
		tmp.title = this.title;
		tmp.description = this.description;
		tmp.key = this.key;
		tmp.value = this.value;
		
		tmp.paths = new ArrayList<String>();
		for(String path : this.paths) {
			tmp.paths.add(path);
		}
		
		tmp.extensions = new ArrayList<String>();
		for(String extension : this.extensions) {
			tmp.extensions.add(extension);
		}
		
		return tmp;
	}
	
	@Override
	public void reset() {
		this.title = "";
		this.description = "";
		this.value = new Object();
		this.key = "";
		this.extensions.clear();
		this.paths.clear();
	}
}
