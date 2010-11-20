package com.happygoatstudios.bt.timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponder;

import android.os.Parcel;
import android.os.Parcelable;
//import android.util.Log;

public class TimerData implements Parcelable {
	
	private String name;
	private Integer ordinal;
	private Integer seconds;
	private boolean repeat;
	private boolean playing;
	
	
	//data that is not serialized, but should still be parcelable.
	private long ttf;
	private Long pauseLocation;
	
	private List<TriggerResponder> responders;
	
	public TimerData() {
		name="";
		ordinal=0;
		seconds=30;
		repeat=true;
		playing = false;
		ttf = seconds*1000;
		responders = new ArrayList<TriggerResponder>();
		
	}
	
	public void reset() {
		ttf = seconds*1000;
		pauseLocation = 0l;
	}
	
	public TimerData copy() {
		
		TimerData tmp = new TimerData();
		tmp.name = this.name;
		tmp.ordinal = this.ordinal;
		tmp.seconds = this.seconds;
		tmp.repeat = this.repeat;
		tmp.playing = this.playing;
		tmp.ttf =  this.ttf;
		for(TriggerResponder responder : this.responders) {
			tmp.responders.add(responder.copy());
		}
		
		return tmp;
		
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof TimerData)) return false;
		TimerData test = (TimerData)o;
		if(!test.name.equals(this.name)) return false;
		if(test.ordinal != this.ordinal) return false;
		if(test.seconds != this.seconds) return false;
		if(test.repeat != this.repeat) return false;
		if(test.playing != this.playing) return false;
		//ttf shouldn't be considered for equality. it seems wrong.
		Iterator<TriggerResponder> test_responders = test.responders.iterator();
		Iterator<TriggerResponder> my_responders = this.responders.iterator();
		while(test_responders.hasNext()) {
			TriggerResponder test_responder = test_responders.next();
			TriggerResponder my_responder = my_responders.next();
			if(!test_responder.equals(my_responder)) return false;
		}
		
		return true;
	}
	
	public static final Parcelable.Creator<TimerData> CREATOR = new Parcelable.Creator<TimerData>() {

		public TimerData createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return new TimerData(arg0);
		}

		public TimerData[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new TimerData[arg0];
		}
	};
	
	public TimerData(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		setName(in.readString());
		setOrdinal(in.readInt());
		setSeconds(in.readInt());
		setRepeat( (in.readInt() == 1) ? true : false);
		setPlaying( (in.readInt() == 1) ? true : false);
		setTTF( in.readLong());
		int numresponders = in.readInt();
		responders = new ArrayList<TriggerResponder>();
		//Log.e("PARCLE","IN: name=" + name);
		//Log.e("PARCLE","IN: ordinal=" + ordinal);
		//Log.e("PARCLE","IN: seconds=" + seconds);
		//Log.e("PARCLE","IN: repeat" + repeat);
		//Log.e("PARCLE","IN: playing=" + playing);
		//Log.e("PARCLE","IN: #responders=" + numresponders);
		
		for(int i = 0;i<numresponders;i++) {
			//Log.e("PARCLE","IN: ATTEMPTING TO LOAD RESPONDER");
			int type = in.readInt();
			//Log.e("PARCLE","IN: FOUND RESPONDER TYPE " + type);
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
		
		//Log.e("PARCEL","PARCLE COMPLETE!");
	}
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		//Log.e("PARCLE","OUT: name=" + name);
		//Log.e("PARCLE","OUT: ordinal=" + ordinal);
		//Log.e("PARCLE","OUT: seconds=" + seconds);
		//Log.e("PARCLE","OUT: repeat" + repeat);
		//Log.e("PARCLE","OUT: playing=" + playing);
		
		o.writeString(name);
		o.writeInt(ordinal);
		o.writeInt(seconds);
		o.writeInt((repeat) ? 1 : 0);
		o.writeInt((playing) ? 1 : 0);
		o.writeLong(ttf);
		o.writeInt(responders.size());
		//Log.e("PARCLE","OUT: " + responders.size() + " responders.");
		
		for(TriggerResponder responder : responders) {
			o.writeInt(responder.getType().getIntVal());
			//Log.e("PARCLE","OUT: RESPONDER TYPE " + responder.getType().getIntVal() + " ADDED.");
			o.writeParcelable(responder, 0);
		}
		
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public Integer getOrdinal() {
		return ordinal;
	}

	public void setSeconds(Integer seconds) {
		this.seconds = seconds;
		//ttf = seconds*1000;
	}

	public Integer getSeconds() {
		return seconds;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setResponders(List<TriggerResponder> responders) {
		this.responders = responders;
	}

	public List<TriggerResponder> getResponders() {
		return responders;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setTTF(long ttf) {
		this.ttf = ttf;
	}

	public long getTTF() {
		return ttf;
	}

	public void setPauseLocation(Long pauseLocation) {
		this.pauseLocation = pauseLocation;
	}

	public Long getPauseLocation() {
		return pauseLocation;
	}
	
}
