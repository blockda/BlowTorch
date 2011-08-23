package com.happygoatstudios.bt.responder.color;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.keplerproject.luajava.LuaState;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.window.TextTree;
import com.happygoatstudios.bt.window.TextTree.Line;
import com.happygoatstudios.bt.window.TextTree.Text;
import com.happygoatstudios.bt.window.TextTree.Unit;

public class ColorAction extends TriggerResponder implements Parcelable {

	private int color = DEFAULT_COLOR; //xterm 256 color? otherwise this should be an int.
	public static int DEFAULT_COLOR = 256;
	public ColorAction(RESPONDER_TYPE pType) {
		super(pType);
		// TODO Auto-generated constructor stub
		color = DEFAULT_COLOR;
	}
	
	public ColorAction() {
		super(RESPONDER_TYPE.COLOR);
		color = DEFAULT_COLOR;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		// TODO Auto-generated method stub
		o.writeInt(color);
	}

	@Override
	public void doResponse(Context c, Line line, Matcher matched,
			Object source, String displayname, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LuaState L, String name) {
		//well. this is sort of duplication of effort from the replacer action. but whatever.
		int start = matched.start();
		int end = matched.end();
		
		Unit u = null;
		ListIterator<Unit> it = line.getIterator();
		
		LinkedList<Unit> newLine = new LinkedList<Unit>();
		
		int working = 0;
		
		int splitAt = 0;
		boolean preEmptiveChop = false;
		int preEmptiveChopAt = 0;
		while(it.hasNext()) {
			u = it.next();
			boolean done = false;
			if(u instanceof TextTree.Text) {
				Text t = (Text)u;
				working += t.getString().length();
				if(working >= start) {
					splitAt = working - start;
					
					done = true;
					//int endofunit = 
				}
			}
			
			if(done) {
				break;
			}
		}
		
		
		
	}

	@Override
	public TriggerResponder copy() {
		ColorAction tmp = new ColorAction(RESPONDER_TYPE.COLOR);
		tmp.color = this.color;
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof ColorAction)) return false;
		ColorAction b= (ColorAction)o;
		ColorAction a = this;
		if(a.color != b.color) return false;
		
		return true;
	}
	
	public ColorAction(Parcel in) {
		super(RESPONDER_TYPE.COLOR);
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		this.color = in.readInt();
	}
	
	public static Parcelable.Creator<ColorAction> CREATOR = new Parcelable.Creator<ColorAction>() {

		public ColorAction createFromParcel(Parcel source) {
			return new ColorAction(source);
		}

		public ColorAction[] newArray(int size) {
			return new ColorAction[size];
		}
		
	};

	@Override
	public void saveResponderToXML(XmlSerializer out)
			throws IllegalArgumentException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		ColorActionParser.saveColorActionToXML(out,this);
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}
	
}
