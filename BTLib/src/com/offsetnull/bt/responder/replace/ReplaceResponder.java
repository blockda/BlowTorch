package com.offsetnull.bt.responder.replace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.keplerproject.luajava.LuaState;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.offsetnull.bt.responder.IteratorModifiedException;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponder.FIRE_WHEN;
import com.offsetnull.bt.responder.ack.AckResponder;
import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.window.TextTree.Text;
import com.offsetnull.bt.window.TextTree.Unit;

public class ReplaceResponder extends TriggerResponder implements Parcelable {

	private String with;
	private String retarget = null;
	//private String windowTarget;
	
	public ReplaceResponder(RESPONDER_TYPE pType) {
		super(pType);
		setWith(null);
		setRetarget(null);
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
		//setWindowTarget(null);
	}

	public ReplaceResponder() {
		super(RESPONDER_TYPE.REPLACE);
		setWith(null);
		setRetarget(null);
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
		//setWindowTarget(null);
	}
	
	public ReplaceResponder(Parcel source) {
		// TODO Auto-generated constructor stub
		super(RESPONDER_TYPE.REPLACE);
		readFromParcel(source);
	}

	private void readFromParcel(Parcel in) {
		this.with = in.readString();
		String fireType = in.readString();
		int ret = in.readInt();
		if(ret == 0) {
			//windowTarget = null;
			retarget = null;
		} else {
			retarget = in.readString();
			//retarget = true;
		}
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
		tmp.retarget = this.retarget;
		tmp.setFireType(this.getFireType());
		tmp.setRetarget(this.getRetarget());
		//tmp.setWindowTarget(this.getWindowTarget());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof ReplaceResponder)) return false;
		ReplaceResponder b = (ReplaceResponder)o;
		ReplaceResponder a = this;
		if(!a.getWith().equals(b.getWith())) return false;
		if(a.getFireType() != b.getFireType()) return false;
		if(a.getRetarget() != b.getRetarget()) return false;
		//if(!a.getWindowTarget().equals(b.getWindowTarget())) return false;
		return true;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel p, int arg1) {
		p.writeString(with);
		p.writeString(this.getFireType().getString());
		if(retarget != null) {
			p.writeInt(1);
			p.writeString(retarget);
		} else {
			p.writeInt(0);
		}
		
	}

	@Override
	public boolean doResponse(Context c,TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,TextTree.Line line,int pstart,int pend,String matched,Object source, String displayname,String host,int port, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LuaState L, String name,String encoding) throws IteratorModifiedException {
			if(line == null) {
				return false;
			}
			
			if(windowIsOpen) {
				if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) return false;
			} else {
				if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) return false;
			}
			
			int end = pend  + 1 + tree.getModCount();
			
			int start = pstart + tree.getModCount();
			
			
			//so here we go, meat of the replacer code.
			//int start = matched.start();
			//int end = matched.end()-1;
			
			ListIterator<TextTree.Unit> it = line.getIterator();
			//reset iterator to begginig of line.
			while(it.hasPrevious()) {
				it.previous();
			}
			
			String replaced = this.translate(this.getWith(), captureMap);
			int delta = (replaced.length()-1) - (end - start);
			if(delta < 0) {
				//delta = delta -1;
			}
			//if(delta < 0)
			tree.setModCount(tree.getModCount()+delta);
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
			
			if(retarget != null) {
				int previndex = iterator.previousIndex();
				int bcount = tree.getBrokenLineCount();
				try {
					tree.getLines().remove(lineNumber);
					
				} catch (Exception e){
					e.printStackTrace();
				}
				//tree.getLines().re
				tree.updateMetrics();
				
				
				int b_acount = tree.getBrokenLineCount();
				//Log.e("REPLAC")
				Log.e("REPLACE","RETARGETING TO: " + retarget + " original: " + bcount + " after: "+b_acount);
				Message msg = dispatcher.obtainMessage(Connection.MESSAGE_LINETOWINDOW,line);
				Bundle b = msg.getData();
				b.putString("TARGET", retarget);
				msg.setData(b);
				dispatcher.sendMessage(msg);
				
				if(iterator.hasPrevious()) {
					iterator = tree.getLines().listIterator(previndex+1);
					IteratorModifiedException e = new IteratorModifiedException(iterator);
					throw e;
				}
			}
			
			return false;
			
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

	public void setRetarget(String retarget) {
		this.retarget = retarget;
	}

	public String getRetarget() {
		return retarget;
	}

}
