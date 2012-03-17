package com.happygoatstudios.bt.service.plugin.settings;

import java.util.ArrayList;
import java.util.HashMap;

import com.happygoatstudios.bt.service.SettingsChangedListener;

import android.os.Parcel;
import android.os.Parcelable;

public class SettingsGroup extends Option implements Parcelable {
	
	private static final int OPTION_BOOLEAN = 1;
	private static final int OPTION_OPTIONS = 2;
	private static final int OPTION_LIST = 3;
	private static final int OPTION_ENCODING = 4;
	private static final int OPTION_INTEGER = 5;
	private static final int OPTION_COLOR = 6;
	private static final int OPTION_FILE = 7;
	
	private HashMap<String,Option> optionsMap = new HashMap<String,Option>();
	private HashMap<String,SettingsChangedListener> listenerMap = new HashMap<String,SettingsChangedListener>();
	
	private ArrayList<Option> options;
	
	private SettingsChangedListener listener;
	
	public SettingsGroup() {
		options = new ArrayList<Option>();
		type = TYPE.GROUP;
	}
	
	public ArrayList<Option> getOptions() {
		return options;
	}
	
	public SettingsGroup(Parcel p) {
		type = TYPE.GROUP;
		setTitle(p.readString());
		setDescription(p.readString());
		setKey(p.readString());
		int size = p.readInt();
		options = new ArrayList<Option>(size);
		for(int i=0;i<size;i++) {
			int type = p.readInt();
			Option o = null;
			switch(type) {
			case OPTION_BOOLEAN:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.BooleanOption.class.getClassLoader());
				break;
			case OPTION_LIST:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.ListOption.class.getClassLoader());
				
				break;
			case OPTION_OPTIONS:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.SettingsGroup.class.getClassLoader());
				
				break;
			case OPTION_ENCODING:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.EncodingOption.class.getClassLoader());
				break;
			case OPTION_INTEGER:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.IntegerOption.class.getClassLoader());
				break;
			case OPTION_COLOR:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.ColorOption.class.getClassLoader());
				break;
			case OPTION_FILE:
				o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.FileOption.class.getClassLoader());
				break;
			}

			//Option o = p.readParcelable(com.happygoatstudios.bt.service.plugin.settings.Option.class.getClassLoader());
			addOption(o);
		}
	}

	public void addOption(Option option) {
		updateOptionsMap(option,listener);
		options.add(option);
	}
	
	private void updateOptionsMap(Option option,SettingsChangedListener listener) {
		switch(option.type) {
		case GROUP:
			SettingsGroup sg = (SettingsGroup)option;
			if(sg.getListener() == null) { sg.setListener(listener); }
			for(int i=0;i<sg.getOptions().size();i++) {
				updateOptionsMap(sg.getOptions().get(i),sg.getListener());
			}
			break;
		default:
			optionsMap.put(option.getKey(),option);
			listenerMap.put(option.getKey(), listener);
			break;
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeString(getTitle());
		p.writeString(getDescription());
		p.writeString(getKey());
		p.writeInt(options.size());
		for(int i=0;i<options.size();i++) {
			Option o = options.get(i);
			switch(o.type) {
			case BOOLEAN:
				p.writeInt(OPTION_BOOLEAN);
				break;
			case GROUP:
				p.writeInt(OPTION_OPTIONS);
				break;
			case LIST:
				p.writeInt(OPTION_LIST);
				break;
			case ENCODING:
				p.writeInt(OPTION_ENCODING);
				break;
			case INTEGER:
				p.writeInt(OPTION_INTEGER);
				break;
			case COLOR:
				p.writeInt(OPTION_COLOR);
				break;
			case FILE:
				p.writeInt(OPTION_FILE);
				break;
			}
			p.writeParcelable(o, flags);
		}
	}
	
	public static final Parcelable.Creator<SettingsGroup> CREATOR = new Parcelable.Creator<SettingsGroup>() {

		public SettingsGroup createFromParcel(Parcel arg0) {
			return new SettingsGroup(arg0);
		}

		public SettingsGroup[] newArray(int arg0) {
			return new SettingsGroup[arg0];
		}
	};
	
	public Option findOptionByKey(String key) {
		return optionsMap.get(key);
	}
	
	public void updateBoolean(String key,boolean value) {
		BaseOption o = (BaseOption) optionsMap.get(key);
		if(o != null) {
			o.setValue(value);
			SettingsChangedListener tmp = listenerMap.get(key);
			if(tmp != null) {
				tmp.updateSetting(key, Boolean.toString(value));
			}
		} else {
			//type mismatch, don't do anything.
		}
	}
	
	public void updateInteger(String key,int value) {
		BaseOption o = (BaseOption) optionsMap.get(key);
		if(o != null) {
			o.setValue(value);
			SettingsChangedListener tmp = listenerMap.get(key);
			if(tmp != null) {
				tmp.updateSetting(key, Integer.toString(value));
			}
		} else {
			//type mismatch, don't do anything.
		}
		

	}
	
	public void updateFloat(String key,float value) {
		BaseOption o = (BaseOption) optionsMap.get(key);
		if(o != null) {
			o.setValue(value);
			SettingsChangedListener tmp = listenerMap.get(key);
			if(tmp != null) {
				tmp.updateSetting(key, Float.toString(value));
			}
		}
		//if(o instanceof FloatOption) {
		//	((IntegerOption) o).setValue(value);
		//} else {
			//type mismatch, don't do anything.
		//}

	}
	
	public void updateString(String key,String value) {
		BaseOption o = (BaseOption) optionsMap.get(key);
		if(o != null) {
			o.setValue(value);
			SettingsChangedListener tmp = listenerMap.get(key);
			if(tmp != null) {
				tmp.updateSetting(key, value);
			}
		}

	}
	
	//since all options sould take a string as a value, this is a "force update on setting with string" functoin
	//that the xml parser will use.
	public void setOption(String key,String value) {
		BaseOption o = (BaseOption) optionsMap.get(key);
		if(o != null) {
			o.setValue(value);
			SettingsChangedListener tmp = listenerMap.get(key);
			if(tmp != null) {
				tmp.updateSetting(key, value);
			}
		}
		

		
	}

	public SettingsChangedListener getListener() {
		return listener;
	}

	public void setListener(SettingsChangedListener listener) {
		this.listener = listener;
		
		recursiveListenerUpdate(this);
	}
	
	private void recursiveListenerUpdate(SettingsGroup group) {
		listenerMap.clear();
		int size = group.getOptions().size();
		for(int i = 0;i<size;i++) {
			Option tmp = group.getOptions().get(i);
			if(tmp.type == TYPE.GROUP) {
				SettingsGroup sg = (SettingsGroup)tmp;
				sg.setListener(listener);
				recursiveListenerUpdate(sg);
			} else {
				listenerMap.put(tmp.getKey(), listener);
			}
		}
	}

	public void addOptionAt(Option option, int i) {
		updateOptionsMap(option,listener);
		options.add(i,option);
	}
	
	/*public interface BlandNewOption {
		
	}*/
	
}
