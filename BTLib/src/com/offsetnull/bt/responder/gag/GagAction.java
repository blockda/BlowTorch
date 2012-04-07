package com.offsetnull.bt.responder.gag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.window.TextTree;
import com.offsetnull.bt.window.TextTree.Line;

public class GagAction extends TriggerResponder implements Parcelable {
	public static boolean DEFAULT_GAGLOG = true;
	public static boolean DEFAULT_GAGOUTPUT = true;
	
	private boolean gagLog = DEFAULT_GAGLOG;
	private boolean gagOutput = DEFAULT_GAGOUTPUT;
	
	private String retarget = null;
	public GagAction(RESPONDER_TYPE pType) {
		super(pType);
		// TODO Auto-generated constructor stub
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		// TODO Auto-generated method stub
		o.writeInt(gagLog ? 1:0);
		o.writeInt(gagOutput ? 1:0);
	}

	@Override
	public boolean doResponse(Context c, TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,Line line, int start,int end,String matched,
			Object source, String displayname,String host,int port, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LuaState L, String name,String encoding) throws IteratorModifiedException {
			//iterator.pr
			int prevloc = -1;
			ListIterator<TextTree.Line> lineit = tree.getLines().listIterator(lineNumber);
			if(lineit.hasPrevious()) {
				//Log.e("GAG","PREVIOUS INDEX:" + iterator.previousIndex());
				prevloc = lineit.previousIndex();
			}
			//if(tree.getLines().size() == 0) {
			//	return false;
			//}
			//if(tree.getLines().size()-1 <= lineNumber) {
				tree.getLines().remove(lineNumber);
			//} else {
			//	return false; //not really sure why that is happening.
			//}
			
			
			if(retarget != null) {
				Message msg = dispatcher.obtainMessage(Connection.MESSAGE_LINETOWINDOW,line);
				Bundle b = msg.getData();
				//Log.e("GAG","SENDING DATA TO ("+retarget+"): " + TextTree.deColorLine(line));
				b.putString("TARGET", retarget);
				msg.setData(b);
				dispatcher.sendMessage(msg);
			} else {
				//Log.e("GAG","NOT RETARGETING TO: " + retarget);
			}
			
			if(lineit.hasPrevious()) {
				iterator = tree.getLines().listIterator(prevloc+1);
				IteratorModifiedException e = new IteratorModifiedException(iterator);
				throw e;
			}
			
			return false;
			
	}

	@Override
	public TriggerResponder copy() {
		GagAction tmp = new GagAction(RESPONDER_TYPE.GAG);
		tmp.setGagLog(this.isGagLog());
		tmp.setGagOutput(this.gagOutput);
		tmp.setRetarget(this.getRetarget());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof GagAction)) return false;
		GagAction b = (GagAction)o;
		GagAction a = this;
		if(a.gagLog != b.gagLog) return false;
		if(a.gagOutput != b.gagOutput) return false;
		return true;
	}
	
	public GagAction(Parcel in) {
		super(RESPONDER_TYPE.GAG);
		readFromParcel(in);
	}

	public GagAction() {
		// TODO Auto-generated constructor stub
		super(RESPONDER_TYPE.GAG);
	}

	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		this.setGagLog((in.readInt() == 1) ? true : false );
		this.setGagOutput((in.readInt() == 1) ? true : false );
	}

	@Override
	public void saveResponderToXML(XmlSerializer out)
			throws IllegalArgumentException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		GagActionParser.saveGagActionToXML(out,this);
	}

	public void setGagLog(boolean gagLog) {
		this.gagLog = gagLog;
	}

	public boolean isGagLog() {
		return gagLog;
	}

	public void setGagOutput(boolean gagOutput) {
		this.gagOutput = gagOutput;
	}

	public boolean isGagOutput() {
		return gagOutput;
	}

	public void setRetarget(String retarget) {
		this.retarget = retarget;
	}

	public String getRetarget() {
		return retarget;
	}
	
	public static Parcelable.Creator<GagAction> CREATOR = new Parcelable.Creator<GagAction>() {

		public GagAction createFromParcel(Parcel source) {
			return new GagAction(source);
		}

		public GagAction[] newArray(int size) {
			return new GagAction[size];
		}
		
	};

}
