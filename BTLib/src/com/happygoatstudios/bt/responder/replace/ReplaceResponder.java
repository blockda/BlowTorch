package com.happygoatstudios.bt.responder.replace;

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
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.window.TextTree;
import com.happygoatstudios.bt.window.TextTree.Text;
import com.happygoatstudios.bt.window.TextTree.Unit;

public class ReplaceResponder extends TriggerResponder implements Parcelable {

	private String with;
	
	public ReplaceResponder(RESPONDER_TYPE pType) {
		super(pType);
		setWith(null);
	}

	public ReplaceResponder() {
		super(RESPONDER_TYPE.REPLACE);
		setWith(null);
	}
	
	public ReplaceResponder(Parcel source) {
		// TODO Auto-generated constructor stub
		super(RESPONDER_TYPE.REPLACE);
		readFromParcel(source);
	}

	private void readFromParcel(Parcel in) {
		this.with = in.readString();
		String fireType = in.readString();
		if(fireType.equals(FIRE_WINDOW_OPEN)) {
			setFireType(FIRE_WHEN.WINDOW_OPEN);
		} else if (fireType.equals(FIRE_WINDOW_CLOSED)) {
			setFireType(FIRE_WHEN.WINDOW_CLOSED);
		} else if (fireType.equals(FIRE_ALWAYS)) {
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		} else if (fireType.equals(FIRE_NEVER)) {
			setFireType(FIRE_WHEN.WINDOW_NEVER);
		} else {
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		}
	}

	@Override
	public ReplaceResponder copy() {
		ReplaceResponder tmp = new ReplaceResponder();
		tmp.with = this.with;
		tmp.setFireType(this.getFireType());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof ReplaceResponder)) return false;
		ReplaceResponder b = (ReplaceResponder)o;
		ReplaceResponder a = this;
		if(!a.getWith().equals(b.getWith())) return false;
		if(a.getFireType() != b.getFireType()) return false;
	
		return true;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel p, int arg1) {
		p.writeString(with);
		p.writeString(this.getFireType().getString());
	}

	@Override
	public void doResponse(Context c,TextTree tree,TextTree.Line line,Matcher matched,Object source, String displayname, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LuaState L, String name) {
			if(line == null || matched == null) {
				return;
			}
			
			//so here we go, meat of the replacer code.
			int start = matched.start();
			int end = matched.end()-1;
			
			ListIterator<TextTree.Unit> it = line.getIterator();
			//reset iterator to begginig of line.
			while(it.hasPrevious()) {
				it.previous();
			}
			String replaced = this.translate(this.getWith(), captureMap);
			
			//TextTree.Line newLine = new TextTree.Line();
			LinkedList<TextTree.Unit> newLine = new LinkedList<TextTree.Unit>();
			
			int working = 0;
			
			int splitAt = 0;
			TextTree.Unit u = null;
			boolean preEmptiveChop = false;
			int preEmptiveChopAt = 0;
			while(it.hasNext()) {
				u = it.next();
				boolean done = false;
				if(u instanceof TextTree.Text) {
					TextTree.Text t = (TextTree.Text)u;
					int startofunit = working;
					int endofunit = startofunit + t.getString().length()-1;
					
					working += t.getString().length();
					
					if(endofunit >= start) {
						//splitAt = working - start;
						splitAt = start - startofunit;
						//break;
						done = true;
						//int endofunit = start+(t.getString().length()-1);
						if(endofunit >= end) {
							preEmptiveChop = true;
							preEmptiveChopAt = endofunit-end;
						}
						
					} else {
						newLine.add(u);
					}
					
					
				} else {
					newLine.add(u);
				}
				if(done) {
					break;
				}
			}
			
			//so if we are here, it means we have found the beginning of the matched trigger pattern.
			if(splitAt > 0) {
				
				Unit text = line.newText(((Text)u).getString().substring(0,splitAt));
				newLine.add(text);
			}
			
			Unit text = line.newText(replaced);
			newLine.add(text);
			
			if(preEmptiveChop) {
				//means that the matched group landed entirely within a textual unit.
				int length = ((Text)u).getString().length();
				String str = ((Text)u).getString().substring(length-preEmptiveChopAt,length);
				newLine.add(line.newText(str));
			} else {
			
				//finish up, still working with original sequence.
				int chopAt = 0;
				Unit tmp = null;
				while(it.hasNext()) {
					tmp = it.next();
					boolean done = false;
					if(tmp instanceof TextTree.Text) {
						Text t = (Text)tmp;
						int startofunit = working;
						int endofunit = working+t.getString().length()-1;
						working += ((Text)tmp).getString().length();
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
					int length = ((Text)tmp).getString().length();
					Unit chop = line.newText(((Text)tmp).getString().substring(length-chopAt,length));
					newLine.add(chop);
				}
			}
			if(it.hasNext()) {
				while(it.hasNext()) {
					newLine.add(it.next());
				}
			}
			
			line.setData(newLine);
			line.updateData();
	}

	@Override
	public void saveResponderToXML(XmlSerializer out)
			throws IllegalArgumentException, IllegalStateException, IOException {
		ReplaceParser.saveReplaceResponderToXML(out,this);
	}
	
	public static Parcelable.Creator<ReplaceResponder> CREATOR = new Parcelable.Creator<ReplaceResponder>() {

		public ReplaceResponder createFromParcel(Parcel source) {
			return new ReplaceResponder(source);
		}

		public ReplaceResponder[] newArray(int size) {
			return new ReplaceResponder[size];
		}
		
	};

	public String getWith() {
		return with;
	}

	public void setWith(String with) {
		this.with = with;
	}

}
