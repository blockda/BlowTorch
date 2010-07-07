package com.happygoatstudios.bt.window;

import org.xml.sax.Attributes;

import android.os.Parcel;
import android.os.Parcelable;
import android.sax.EndTextElementListener;
import android.sax.StartElementListener;
import android.util.Log;

public class SlickButtonData implements Parcelable {
	
	public int x;
	public int y;
	public String the_text;
	public String the_label;
	public String flip_command;
	
	final static public int MOVE_FREE = 0;
	final static public int MOVE_NUDGE = 1;
	final static public int MOVE_FREEZE = 2;
	
	public int MOVE_STATE = MOVE_FREE;
	
	public SlickButtonData() {
		int x = 0;
		int y = 0;
		the_text = "";
		the_label = "";
		flip_command = "";
	}
	
	public SlickButtonData(int ix, int iy, String itext, String ilbl) {
		x = ix;
		y = iy;
		the_text = itext;
		the_label = ilbl;
		flip_command = "";
	}
	
	public boolean equals(Object aTest) {
		//check for self equality
		if(this == aTest) {
			return true;
		}
		
		//return false if this is not a slickbuttondata holder
		if( !(aTest instanceof SlickButtonData)) return false;
		
		SlickButtonData test = (SlickButtonData)aTest;
		
		boolean retval = true;
		if(this.x != test.x) retval = false;
		if(this.y != test.y) retval = false;
		if(!this.the_label.equals(test.the_label)) retval = false;
		if(!this.the_text.equals(test.the_text)) retval = false;
		if(!this.flip_command.equals(test.flip_command)) retval = false;
		if(this.MOVE_STATE != test.MOVE_STATE) retval = false;
		
		return retval;
	}
	
	public String toString() {
		if(the_text == null) {
			the_text = "";
		}
		if(the_label == null) {
			the_label = "";
		}
		if(flip_command == null) {
			flip_command = "";
		}
		return x+"||"+y+"||"+ (the_text.equals("") ? "[NONE]" : the_text) +"||"+(the_label.equals("") ? "[NONE]" : the_label)+"||"+(flip_command.equals("") ? "[NONE]" : flip_command)+"||"+MOVE_STATE;
	}
	
	public void setDataFromString(String input) {
		String[] elements = input.split("\\|\\|");
		
		if(elements.length != 6) {
			//Log.e("SBD","String not properly formatted");
		}
		
		x = new Integer(elements[0]).intValue();
		y = new Integer(elements[1]).intValue();
		the_text = elements[2].equals("[NONE]") ? "" : elements[2];
		the_label = elements[3].equals("[NONE]") ? "" : elements[3];
		flip_command = elements[4].equals("[NONE]") ? "" : elements[4];
		MOVE_STATE = new Integer(elements[5]).intValue();
		
	}
	
	public SlickButtonData(Parcel in) {
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<SlickButtonData> CREATOR = new Parcelable.Creator<SlickButtonData>() {

		public SlickButtonData createFromParcel(Parcel arg0) {
			return new SlickButtonData(arg0);
		}

		public SlickButtonData[] newArray(int arg0) {
			return new SlickButtonData[arg0];
		}
	};

	public int describeContents() {
		// nothing special about this, as far as i know.
		return 0;
	}

	public void writeToParcel(Parcel d, int arg1) {
		d.writeInt(x);
		d.writeInt(y);
		d.writeString(the_text);
		d.writeString(the_label);
		d.writeString(flip_command);
		d.writeInt(MOVE_STATE);
	}
	
	public void readFromParcel(Parcel in) {
		x = in.readInt();
		y = in.readInt();
		the_text = in.readString();
		the_label = in.readString();
		flip_command = in.readString();
		MOVE_STATE = in.readInt();
	}

	
	public SlickButtonData copy() {
		SlickButtonData tmp = new SlickButtonData();
		tmp.x = this.x;
		tmp.y = this.y;
		tmp.flip_command = this.flip_command;
		tmp.the_label = this.the_label;
		tmp.the_text = this.the_text;
		tmp.MOVE_STATE = this.MOVE_STATE;
		return tmp;
	}

}
