package com.offsetnull.bt.speedwalk;

import android.os.Parcel;
import android.os.Parcelable;

public class DirectionData implements Parcelable {
	private String direction = "";
	private String command = "";
	private String reverse = "";
	
	public DirectionData() {
		
	}
	
	public DirectionData(String direction,String command) {
		this.direction = direction;
		this.command = command;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof DirectionData)) return false;
		DirectionData tmp = (DirectionData)o;
		if(!tmp.direction.equals(this.direction)) return false;
		if(!tmp.command.equals(this.command)) return false;
		if(!tmp.reverse.equals(this.reverse)) return false;
		
		return true;
	}
	
	public DirectionData copy() {
		DirectionData tmp = new DirectionData();
		tmp.direction = this.direction;
		tmp.command = this.command;
		tmp.reverse = this.reverse;
		return tmp;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getDirection() {
		return direction;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setReverse(String reverse) {
		this.reverse = reverse;
	}

	public String getReverse() {
		return reverse;
	}
	
	public static final Parcelable.Creator<DirectionData> CREATOR = new Parcelable.Creator<DirectionData>() {

		
		public DirectionData createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new DirectionData(source);
		}

		
		public DirectionData[] newArray(int size) {
			// TODO Auto-generated method stub
			return new DirectionData[size];
		}
	
	
	};
	
	public DirectionData(Parcel p) {
		readFromParcel(p);
	}
	
	private void readFromParcel(Parcel p) {
		this.direction = p.readString();
		this.command = p.readString();
		this.reverse = p.readString();
	}

	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void writeToParcel(Parcel p, int arg1) {
		p.writeString(this.direction);
		p.writeString(this.command);
		p.writeString(this.reverse);
	}
}
