package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;
import java.util.List;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;

import android.app.Application;
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
		setResponders(new ArrayList<TriggerResponder>());
		setInterpretAsRegex( (in.readInt() == 1) ? true : false);
		
		int numresponders = in.readInt();
		for(int i = 0;i<numresponders;i++) {
			int type = in.readInt();
			switch(type) {
			case TriggerResponder.RESPONDER_TYPE_NOTIFICATION:
				NotificationResponder resp = in.readParcelable(com.happygoatstudios.bt.responder.notification.NotificationResponder.class.getClassLoader());
				/*NotificationResponder resp = new NotificationResponder();
				resp.setMessage(in.readString());
				resp.setTitle(in.readString());
				String fireType = in.readString();
				if(TriggerResponder.FIRE_WHEN.WINDOW_BOTH.equals(fireType)) {
					resp.setFireType(FIRE_WHEN.WINDOW_OPEN);
				} else if (TriggerResponder.FIRE_WHEN.WINDOW_CLOSED.equals(fireType)) {
					resp.setFireType(FIRE_WHEN.WINDOW_CLOSED);
				} else if(TriggerResponder.FIRE_WHEN.WINDOW_BOTH.equals(fireType)) {
					resp.setFireType(FIRE_WHEN.WINDOW_BOTH);
				} else {
					resp.setFireType(FIRE_WHEN.WINDOW_BOTH);
				}*/
				
				responders.add(resp);
				break;
			case TriggerResponder.RESPONDER_TYPE_TOAST:
				ToastResponder toasty = in.readParcelable(com.happygoatstudios.bt.responder.toast.ToastResponder.class.getClassLoader());
				/*ToastResponder toast = new ToastResponder();
				toast.setMessage(in.readString());
				toast.setDelay(in.readInt());
				
				fireType = in.readString();
				if(TriggerResponder.FIRE_WHEN.WINDOW_BOTH.equals(fireType)) {
					toast.setFireType(FIRE_WHEN.WINDOW_OPEN);
				} else if (TriggerResponder.FIRE_WHEN.WINDOW_CLOSED.equals(fireType)) {
					toast.setFireType(FIRE_WHEN.WINDOW_CLOSED);
				} else if(TriggerResponder.FIRE_WHEN.WINDOW_BOTH.equals(fireType)) {
					toast.setFireType(FIRE_WHEN.WINDOW_BOTH);
				} else {
					toast.setFireType(FIRE_WHEN.WINDOW_BOTH);
				}*/
				responders.add(toasty);
				break;
			case TriggerResponder.RESPONDER_TYPE_ACK:
				AckResponder ack = in.readParcelable(com.happygoatstudios.bt.responder.ack.AckResponder.class.getClassLoader());
				/*AckResponder ack = new AckResponder();
				ack.setAckWith(in.readString());
				fireType = in.readString();
				if(TriggerResponder.FIRE_WHEN.WINDOW_BOTH.equals(fireType)) {
					ack.setFireType(FIRE_WHEN.WINDOW_OPEN);
				} else if (TriggerResponder.FIRE_WHEN.WINDOW_CLOSED.equals(fireType)) {
					ack.setFireType(FIRE_WHEN.WINDOW_CLOSED);
				} else if(TriggerResponder.FIRE_WHEN.WINDOW_BOTH.equals(fireType)) {
					ack.setFireType(FIRE_WHEN.WINDOW_BOTH);
				} else {
					ack.setFireType(FIRE_WHEN.WINDOW_BOTH);
				}*/
				
				responders.add(ack);
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
			/*switch(responder.getType()) {
			case NOTIFICATION:
				NotificationResponder notify = (NotificationResponder)responder;
				out.writeString(notify.getMessage());
				out.writeString(notify.getTitle());
				out.writeString(notify.getFireType().getString());
				break;
			case TOAST:
				ToastResponder toasty = (ToastResponder)responder;
				out.writeString(toasty.getMessage());
				out.writeInt(toasty.getDelay());
				out.writeString(toasty.getFireType().getString());
				break;
			case ACK:
				AckResponder ack = (AckResponder)responder;
				out.writeString(ack.getAckWith());
				out.writeString(ack.getFireType().getString());
				break;
			}*/
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
