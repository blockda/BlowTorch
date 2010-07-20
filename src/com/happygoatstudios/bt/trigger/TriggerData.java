package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;
import java.util.List;

import com.happygoatstudios.bt.responder.NotificationResponder;
import com.happygoatstudios.bt.responder.TriggerResponder;

import android.os.Parcel;
import android.os.Parcelable;

public class TriggerData implements Parcelable {

	private String name;
	private String pattern;
	private boolean interpretAsRegex;
	

	
	private List<TriggerResponder> responders;
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof TriggerData)) return false;
		TriggerData tmp = (TriggerData)o;
		
		if(!this.getName().equals(tmp.getName())) return false;
		if(!this.getPattern().equals(tmp.getPattern())) return false;
		
		return true;
	}
	
	public TriggerData() {
		name = "";
		pattern = "";
		interpretAsRegex = false;
		responders = new ArrayList<TriggerResponder>();
	}
	
	public TriggerData copy() {
		TriggerData tmp = new TriggerData();
		tmp.name = this.name;
		tmp.pattern = this.pattern;
		tmp.interpretAsRegex = this.interpretAsRegex;
		tmp.responders = this.responders;
		
		return tmp;
	}
	
	public static final Parcelable.Creator<TriggerData> CREATOR = new Parcelable.Creator<TriggerData>() {

		public TriggerData createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return new TriggerData(arg0);
		}

		public TriggerData[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new TriggerData[arg0];
		}
	};
	
	public TriggerData(Parcel in) {
		readFromParcel(in);
	}
	
	public void readFromParcel(Parcel in) {
		//TODO: NEED TO ACTUALLY READ DATA.
		setName(in.readString());
		setPattern(in.readString());
		setInterpretAsRegex( (in.readInt() == 1) ? true : false);
		
		int numresponders = in.readInt();
		for(int i = 0;i<numresponders;i++) {
			int type = in.readInt();
			switch(type) {
			case TriggerResponder.RESPONDER_TYPE_NOTIFICATION:
				NotificationResponder resp = in.readParcelable(null);
				responders.add(resp);
				break;
			case TriggerResponder.RESPONDER_TYPE_TOAST:
				break;
			case TriggerResponder.RESPONDER_TYPE_ACK:
				break;
			}
		}
	}
	
	//save these for later.
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(name);
		out.writeString(pattern);
		out.writeInt( interpretAsRegex ? 1 : 0);
		//out.writeP
		out.writeInt(responders.size());
		for(TriggerResponder responder : responders) {
			out.writeInt(responder.getType().getIntVal());
			out.writeParcelable(responder, 0);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}

	public void setInterpretAsRegex(boolean interpretAsRegex) {
		this.interpretAsRegex = interpretAsRegex;
	}

	public boolean isInterpretAsRegex() {
		return interpretAsRegex;
	}

	public void setResponders(List<TriggerResponder> responders) {
		this.responders = responders;
	}

	public List<TriggerResponder> getResponders() {
		return responders;
	}

}
