package com.happygoatstudios.bt.timer;

import android.os.Parcel;
import android.os.Parcelable;

public class TimerProgress implements Parcelable {
	private float percentage;
	private long timeleft;
	private STATE state;
	public enum STATE { STOPPED,PLAYING,PAUSED; };
	
	public TimerProgress() {
		percentage = 1.0f;
		timeleft = 30000;
		state = STATE.STOPPED;
	}
	
	public TimerProgress copy() {
		TimerProgress tmp = new TimerProgress();
		tmp.percentage = this.percentage;
		tmp.timeleft = this.timeleft;
		tmp.state = this.state;
		return tmp;
	}
	
	public static final Parcelable.Creator<TimerProgress> CREATOR = new Parcelable.Creator<TimerProgress>() {

		public TimerProgress createFromParcel(Parcel arg0) {
			
			return new TimerProgress(arg0);
		}

		public TimerProgress[] newArray(int arg0) {
			
			return new TimerProgress[arg0];
		}
	};
	
	public TimerProgress(Parcel in) {
		readFromParcel(in);
	}
	
	public void readFromParcel(Parcel in) {
		setPercentage(in.readFloat());
		setTimeleft(in.readLong());
		int instate = in.readInt();
		switch(instate) {
		case 0:
			state = STATE.STOPPED;
			break;
		case 1:
			state = STATE.PLAYING;
			break;
		case 2:
			state = STATE.PAUSED;
			break;
		}
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

	public float getPercentage() {
		return percentage;
	}

	public void setTimeleft(long timeleft) {
		this.timeleft = timeleft;
	}

	public long getTimeleft() {
		return timeleft;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		o.writeFloat(percentage);
		o.writeLong(timeleft);
		switch(state) {
		case STOPPED:
			o.writeInt(0);
			break;
		case PLAYING:
			o.writeInt(1);
			break;
		case PAUSED:
			o.writeInt(2);
			break;
		}
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public STATE getState() {
		return state;
	}
}
