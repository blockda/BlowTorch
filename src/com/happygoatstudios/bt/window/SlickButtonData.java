package com.happygoatstudios.bt.window;

import org.xml.sax.Attributes;

import android.os.Parcel;
import android.os.Parcelable;
import android.sax.EndTextElementListener;
import android.sax.StartElementListener;
import android.util.Log;

public class SlickButtonData implements Parcelable {
	
	private int x;
	private int y;
	private int width;
	private int height;
	private String text;
	private String label;
	private String flipCommand;
	private String targetSet;
	
	//private String foo;
	
	final static public int MOVE_FREE = 0;
	final static public int MOVE_NUDGE = 1;
	final static public int MOVE_FREEZE = 2;
	
	public int MOVE_STATE = MOVE_FREE;
	
	public SlickButtonData() {
		x = 0;
		y = 0;
		text = "";
		label = "";
		flipCommand = "";
		targetSet = "";
		height=80;
		width=80;
	}
	
	public SlickButtonData(int ix, int iy, String itext, String ilbl) {
		x = ix;
		y = iy;
		text = itext;
		label = ilbl;
		flipCommand = "";
		targetSet = "";
		height=80;
		width=80;
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
		if(this.height != test.height) retval = false;
		if(this.width != test.width) retval = false;
		if(!this.label.equals(test.label)) retval = false;
		if(!this.text.equals(test.text)) retval = false;
		if(!this.flipCommand.equals(test.flipCommand)) retval = false;
		if(this.MOVE_STATE != test.MOVE_STATE) retval = false;
		//if(this.id != test.id) retval = false;
		if(!this.targetSet.equals(test.targetSet)) retval = false;
		//Log.e("SLICKBUTTONDATA","Comparing " + this.toString() + " against " + test.toString() + " returning " + retval);
		
		return retval;
	}
	
	public String toString() {
		/*if(the_text == null) {
			the_text = "";
		}
		if(the_label == null) {
			the_label = "";
		}
		if(flip_command == null) {
			flip_command = "";
		}*/
		return x+"||"+y+"||"+ (text.equals("") ? "[NONE]" : text) +"||"+(label.equals("") ? "[NONE]" : label)+"||"+(flipCommand.equals("") ? "[NONE]" : flipCommand)+"||"+MOVE_STATE+"||"+targetSet+"||"+width+"||"+height;
	}
	
	/*public void setDataFromString(String input) {
		//String[] elements = input.split("\\|\\|");
		
		//if(elements.length != 6) {
			//Log.e("SBD","String not properly formatted");
		}
		
		x = new Integer(elements[0]).intValue();
		y = new Integer(elements[1]).intValue();
		the_text = elements[2].equals("[NONE]") ? "" : elements[2];
		the_label = elements[3].equals("[NONE]") ? "" : elements[3];
		flip_command = elements[4].equals("[NONE]") ? "" : elements[4];
		MOVE_STATE = new Integer(elements[5]).intValue();
		
	}*/
	
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
		d.writeString(text);
		d.writeString(label);
		d.writeString(flipCommand);
		d.writeInt(MOVE_STATE);
		d.writeString(targetSet);
		d.writeInt(height);
		d.writeInt(width);
	}
	
	public void readFromParcel(Parcel in) {
		x = in.readInt();
		y = in.readInt();
		text = in.readString();
		label = in.readString();
		flipCommand = in.readString();
		MOVE_STATE = in.readInt();
		targetSet = in.readString();
		height = in.readInt();
		width = in.readInt();
	}

	
	public SlickButtonData copy() {
		SlickButtonData tmp = new SlickButtonData();
		tmp.x = this.x;
		tmp.y = this.y;
		tmp.flipCommand = this.flipCommand;
		tmp.label = this.label;
		tmp.text = this.text;
		tmp.MOVE_STATE = this.MOVE_STATE;
		tmp.targetSet = this.targetSet;
		tmp.height = this.height;
		tmp.width = this.width;
		return tmp;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setText(String text) {
		if(text == null) text = "";
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setLabel(String label) {
		if(flipCommand == null) flipCommand = "";
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public void setFlipCommand(String flipCommand) {
		if(flipCommand == null) flipCommand = "";
		this.flipCommand = flipCommand;
	}

	public String getFlipCommand() {
		return flipCommand;
	}
	
	public void setTargetSet(String targetSet) {
		if(targetSet == null) targetSet = "";
		this.targetSet = targetSet;
	}

	public String getTargetSet() {
		return targetSet;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

}
