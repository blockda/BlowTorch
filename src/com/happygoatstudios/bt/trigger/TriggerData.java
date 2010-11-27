package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import android.os.Parcel;
import android.os.Parcelable;

public class TriggerData implements Parcelable {

	private String name;
	private String pattern;
	private boolean interpretAsRegex;
	private boolean fireOnce;
	
	private boolean fired = false;
	
	private boolean hidden = false;
	
	private List<TriggerResponder> responders;
	
	
	public TriggerData() {
		name = "";
		pattern = "";
		interpretAsRegex = false;
		responders = new ArrayList<TriggerResponder>();
		fireOnce = false;
		hidden = false;
	}
	
	public TriggerData copy() {
		TriggerData tmp = new TriggerData();
		tmp.name = this.name;
		tmp.pattern = this.pattern;
		tmp.interpretAsRegex = this.interpretAsRegex;
		tmp.fireOnce = this.fireOnce;
		tmp.hidden = this.hidden;
		for(TriggerResponder responder : this.responders) {
			tmp.responders.add(responder.copy());
		}
		
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof TriggerData)) return false;
		TriggerData test = (TriggerData)o;
		if(!test.name.equals(this.name)) return false;
		if(!test.pattern.equals(this.pattern)) return false;
		if(test.interpretAsRegex != this.interpretAsRegex) return false;
		if(test.fireOnce != this.fireOnce) return false;
		if(test.hidden != this.hidden) return false;
		Iterator<TriggerResponder> test_responders = test.responders.iterator();
		Iterator<TriggerResponder> my_responders = this.responders.iterator();
		while(test_responders.hasNext()) {
			TriggerResponder test_responder = test_responders.next();
			TriggerResponder my_responder = my_responders.next();
			
			if(!test_responder.equals(my_responder)) return false;
		}
		
		return true;
	}
	
	public static final Parcelable.Creator<TriggerData> CREATOR = new Parcelable.Creator<TriggerData>() {

		public TriggerData createFromParcel(Parcel arg0) {
			return new TriggerData(arg0);
		}

		public TriggerData[] newArray(int arg0) {
			return new TriggerData[arg0];
		}
	};
	
	public TriggerData(Parcel in) {
		readFromParcel(in);
	}
	
	public void readFromParcel(Parcel in) {
		setName(in.readString());
		setPattern(in.readString());
		setResponders(new ArrayList<TriggerResponder>());
		setInterpretAsRegex( (in.readInt() == 1) ? true : false);
		setFireOnce ((in.readInt() == 1) ? true : false);
		setHidden( (in.readInt() == 1) ? true : false);
		int numresponders = in.readInt();
		for(int i = 0;i<numresponders;i++) {
			int type = in.readInt();
			switch(type) {
			case TriggerResponder.RESPONDER_TYPE_NOTIFICATION:
				NotificationResponder resp = in.readParcelable(com.happygoatstudios.bt.responder.notification.NotificationResponder.class.getClassLoader());
				
				responders.add(resp);
				break;
			case TriggerResponder.RESPONDER_TYPE_TOAST:
				ToastResponder toasty = in.readParcelable(com.happygoatstudios.bt.responder.toast.ToastResponder.class.getClassLoader());

				responders.add(toasty);
				break;
			case TriggerResponder.RESPONDER_TYPE_ACK:
				AckResponder ack = in.readParcelable(com.happygoatstudios.bt.responder.ack.AckResponder.class.getClassLoader());
				
				responders.add(ack);
				break;
			}
		}
	}
	
	//save these for later.
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(name);
		out.writeString(pattern);
		out.writeInt( interpretAsRegex ? 1 : 0);
		out.writeInt(fireOnce ? 1 : 0);
		out.writeInt(hidden ? 1 : 0);
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

	public void setFireOnce(boolean fireOnce) {
		this.fireOnce = fireOnce;
	}

	public boolean isFireOnce() {
		return fireOnce;
	}

	public void setFired(boolean fired) {
		this.fired = fired;
	}

	public boolean isFired() {
		return fired;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isHidden() {
		return hidden;
	}

}
