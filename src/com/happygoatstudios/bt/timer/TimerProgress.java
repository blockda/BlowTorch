package com.happygoatstudios.bt.timer;

import android.os.Parcel;
import android.os.Parcelable;

public class TimerProgress implements Parcelable {
	private float percentage;
	private long timeleft;
	
	public TimerProgress() {
		percentage = 1.0f;
		timeleft = 30000;
	}
	
	public TimerProgress copy() {
		TimerProgress tmp = new TimerProgress();
		tmp.percentage = this.percentage;
		tmp.timeleft = this.timeleft;
		
		return tmp;
	}
	
	public static final Parcelable.Creator<TimerProgress> CREATOR = new Parcelable.Creator<TimerProgress>() {

		public TimerProgress createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return new TimerProgress(arg0);
		}

		public TimerProgress[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new TimerProgress[arg0];
		}
	};
	
	public TimerProgress(Parcel in) {
		readFromParcel(in);
	}
	
	public void readFromParcel(Parcel in) {
		setPercentage(in.readFloat());
		setTimeleft(in.readLong());
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
	}
}
