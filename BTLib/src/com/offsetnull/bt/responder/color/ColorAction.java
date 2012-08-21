package com.offsetnull.bt.responder.color;

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
import android.util.Log;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.window.TextTree.Color;
import com.offsetnull.bt.window.TextTree.Line;
import com.offsetnull.bt.window.TextTree.Text;
import com.offsetnull.bt.window.TextTree.Unit;

public class ColorAction extends TriggerResponder implements Parcelable {

	private int color = DEFAULT_COLOR; //xterm 256 color? otherwise this should be an int.
	private int backgroundColor = DEFAULT_BACKGROUND_COLOR;
	public static int DEFAULT_COLOR = 256;
	public static int DEFAULT_BACKGROUND_COLOR = 232;
	
	public ColorAction(RESPONDER_TYPE pType) {
		super(pType);
		// TODO Auto-generated constructor stub
		color = DEFAULT_COLOR;
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
	}
	
	public ColorAction() {
		super(RESPONDER_TYPE.COLOR);
		//color = DEFAULT_COLOR;
		color = DEFAULT_COLOR;
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		// TODO Auto-generated method stub
		o.writeInt(color);
		o.writeInt(backgroundColor);
		o.writeString(this.getFireType().getString());
	}

	@Override
	public boolean doResponse(Context c, TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,Line line, int pstart, int pend,String matched,
			Object source, String displayname,String host,int port, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LuaState L, String name,String encoding) {
		//well. this is sort of duplication of effort from the replacer action. but whatever.
		//int start = matched.start();
		//int end = matched.end()-1;
		int end = pend + 1 + tree.getModCount();
		int start = pstart + tree.getModCount();
		Unit u = null;
		line.resetIterator();
		ListIterator<Unit> it = line.getIterator();
		
		LinkedList<Unit> newLine = new LinkedList<Unit>();
		
		int working = 0;
		
		Color bleed = tree.getBleedColor();
		
		int splitAt = 0;
		boolean preEmptiveChop = false;
		int preEmptiveChopAt = 0;
		while(it.hasNext()) {
			u = it.next();
			boolean done = false;
			if(u instanceof TextTree.Text) {
				Text t = (Text)u;
				int startofunit = working;
				int endofunit = working + t.getString().length()-1;
				
				working += t.getString().length();
				
				if(endofunit >= start) {
					//pre-emptive replace. replaced text is entirely contained in the text unit
					splitAt = start - startofunit;
					
					done = true;
					if(endofunit >= end) {
						preEmptiveChop = true;
						preEmptiveChopAt = endofunit-end;
					}
				} else {
					newLine.add(u);
				}
			} else {
				if(u instanceof TextTree.Color) {
					bleed = (Color)u;
				}
				newLine.add(u);
			}
			
			if(done) {
				break;
			}
		}
		
		if(splitAt > 0) {
			Text pre = line.newText(((Text)u).getString().substring(0,splitAt));
			newLine.add(pre);
		}
		
		//here is where we would insert replaced text if this were a replacer.
		//instead, this is where we insert a new color unit denoting which color we would like.
		newLine.add(line.newColor(color));
		if(backgroundColor != 0 && backgroundColor != 16 && backgroundColor != 231) {
			newLine.add(line.newBackgroundColor(backgroundColor));
		}
		newLine.add(line.newText(matched));
		if(preEmptiveChop) {
			int length = ((Text)u).getString().length();
			Text post = line.newText(((Text)u).getString().substring(length-preEmptiveChopAt,length));
			//insert bleed color to complete the "text color change"
			newLine.add(bleed);
			newLine.add(post);
		} else {
			//normal "find and chop" procedure.
			boolean done = false;
			int chopAt = 0;
			Unit chop = null;
			while(it.hasNext()) {
				chop = it.next();
				if(chop instanceof Text) {
					Text t = (Text)chop;
					int startofunit = working;
					int endofunit = startofunit + t.getString().length()-1;
					working += t.getString().length();
					if(end <= endofunit) {
						chopAt = endofunit - end;
						done = true;
					}
				}
				if(done) {
					break;
				}
			}
			
			if(chopAt > 0) {
				int length = ((Text)chop).getString().length();
				Text post = line.newText(((Text)chop).getString().substring(length-chopAt,length));
				//insert bleed color
				newLine.add(bleed);
				
				newLine.add(post);
			} else {
			
				newLine.add(bleed);
			}
			//newLine.add(post);
		}
		
		//finish out units if there are any.
		while(it.hasNext()) {
			newLine.add(it.next());
		}
		
		//here is where we would do tree pruning/data updating.
		
		//set line's data
		line.setData(newLine);
		line.resetIterator();
		it = line.getIterator();
		/*while(it.hasNext()) {
			Unit a = it.next();
			if(a instanceof Color) {
				Log.e("COLORIZE","OMG WE REPLACED COLOR");
			}
		}*/
		
		//return
		return false;
	}

	@Override
	public TriggerResponder copy() {
		ColorAction tmp = new ColorAction(RESPONDER_TYPE.COLOR);
		tmp.color = this.color;
		tmp.backgroundColor = this.backgroundColor;
		tmp.setFireType(this.getFireType());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof ColorAction)) return false;
		ColorAction b= (ColorAction)o;
		ColorAction a = this;
		if(a.color != b.color) return false;
		if(a.backgroundColor != b.backgroundColor) return false;
		if(a.getFireType() != b.getFireType()) return false;
		
		return true;
	}
	
	public ColorAction(Parcel in) {
		super(RESPONDER_TYPE.COLOR);
		
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		this.color = in.readInt();
		this.backgroundColor = in.readInt();
		
		String fireType = in.readString();
		//Log.e("ACKRESPONDER","READING FROM PARCEL, FIRE TYPE:" + fireType);
		if(fireType.equals(FIRE_WINDOW_OPEN)) {
			//Log.e("ACKRESPONDER","attempting to set open");
			setFireType(FIRE_WHEN.WINDOW_OPEN);
		} else if (fireType.equals(FIRE_WINDOW_CLOSED)) {
			//Log.e("ACKRESPONDER","attempting to set closed");
			setFireType(FIRE_WHEN.WINDOW_CLOSED);
		} else if (fireType.equals(FIRE_ALWAYS)) {
			//Log.e("ACKRESPONDER","attempting to set both");
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		} else if (fireType.equals(FIRE_NEVER)) {
			//Log.e("ACKRESPONDER","attempting to set never");
			setFireType(FIRE_WHEN.WINDOW_NEVER);
		} else {
			//Log.e("ACKRESPONDER","defaulting to both");
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		}
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
	
	public void setBackgroundColor(int color) {
		this.backgroundColor = color;
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
}
