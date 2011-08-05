package com.happygoatstudios.bt.window;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusGroupData implements Parcelable {

	ArrayList<Integer> data = null;
	
	public StatusGroupData() {
		data = new ArrayList<Integer>();
	}
	
	public void addInt(int value) {
		data.add(new Integer(value));
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<StatusGroupData> CREATOR = new Parcelable.Creator<StatusGroupData>() {

		public StatusGroupData createFromParcel(Parcel arg0) {
			return new StatusGroupData(arg0);
		}

		public StatusGroupData[] newArray(int arg0) {
			return new StatusGroupData[arg0];
		}
	};
	
	public StatusGroupData(Parcel p) {
		readFromParcel(p);
	}

	private void readFromParcel(Parcel p) {
		//this.pre = p.readString();
		//this.post = p.readString();
		int val = p.readInt();
		for(int i =0;i<val;i++) {
			data.add(p.readInt());
		}
	}

	public void writeToParcel(Parcel p, int arg1) {
		p.writeInt(data.size());
		for(int i=0;i<data.size();i++) {
			p.writeInt(data.get(i));
		}
	}

}
