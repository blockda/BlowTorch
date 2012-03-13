package com.happygoatstudios.bt.responder.ack;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.keplerproject.luajava.LuaState;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.Connection;
import com.happygoatstudios.bt.service.StellarService;
import com.happygoatstudios.bt.window.TextTree;

public class AckResponder extends TriggerResponder implements Parcelable {

	private String ackWith;
	
	public AckResponder() {
		super(RESPONDER_TYPE.ACK);
		ackWith = "";
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
	}
	
	public AckResponder(RESPONDER_TYPE pType) {
		super(pType);
	}
	
	public AckResponder copy() {
		AckResponder tmp = new AckResponder();
		tmp.ackWith = this.ackWith;
		tmp.setFireType(this.getFireType());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof AckResponder)) return false;
		
		AckResponder test = (AckResponder)o;
		
		if(!test.getAckWith().equals(this.getAckWith())) return false;
		if(test.getFireType() != this.getFireType()) return false;
		return true;
	}
	
	Character cr = new Character((char)13);
	Character lf = new Character((char)10);
	String crlf = cr.toString() + lf.toString();

	@Override
	public boolean doResponse(Context c,TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,TextTree.Line line,int start,int end,String matched,Object source, String displayname, int triggernumber,
			boolean windowIsOpen,Handler dispatcher,HashMap<String,String> captureMap,LuaState L,String name,String encoding) {
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) return false;
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) return false;
		}
		
		Message msg = null;
		//Log.e("ACKRESPONDER","RESPONDING WITH: " + this.getAckWith());
		String xformed = AckResponder.this.translate(this.getAckWith(), captureMap);
		//msg = dispatcher.obtainMessage(StellarService.MESSAGE_SENDDATA,(this.getAckWith() + crlf).getBytes("ISO-8859-1"));
		//TODO: make ack responder actually ack

			msg = dispatcher.obtainMessage(Connection.MESSAGE_SENDDATA_STRING,(xformed + crlf));

		
		dispatcher.sendMessage(msg);
		return false;
	}
	
	public AckResponder(Parcel in) {
		super(RESPONDER_TYPE.ACK);
		readFromParcel(in);
	}
	
	public static Parcelable.Creator<AckResponder> CREATOR = new Parcelable.Creator<AckResponder>() {

		public AckResponder createFromParcel(Parcel source) {
			return new AckResponder(source);
		}

		public AckResponder[] newArray(int size) {
			return new AckResponder[size];
		}
		
	};

	public int describeContents() {
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		setAckWith(in.readString());
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

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(ackWith);
		out.writeString(this.getFireType().getString());
	}

	public void setAckWith(String ackWith) {
		if(ackWith == null) ackWith = "";
		this.ackWith = ackWith;
	}

	public String getAckWith() {
		return ackWith;
	}

	@Override
	public void saveResponderToXML(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
		AckResponderParser.saveResponderToXML(out, this);
	}

	/*@Override
	public void doResponse(Context c, String displayname, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LAUNCH_MODE mode) {
		// TODO Auto-generated method stub
		
	}*/

}
