package com.happygoatstudios.bt.responder.ack;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.TriggerResponder.RESPONDER_TYPE;
import com.happygoatstudios.bt.service.StellarService;

public class AckResponder extends TriggerResponder implements Parcelable {

	private String ackWith;
	
	public AckResponder() {
		super(RESPONDER_TYPE.ACK);
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
	public void doResponse(Context c, String displayname, int triggernumber,
			boolean windowIsOpen,Handler dispatcher) {
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED) return;
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN) return;
		}
		
		Message msg = null;
		try {
			//Log.e("ACKRESPONDER","RESPONDING WITH: " + this.getAckWith());
			msg = dispatcher.obtainMessage(StellarService.MESSAGE_SENDDATA,(this.getAckWith() + crlf).getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dispatcher.sendMessage(msg);
		
	}
	
	public AckResponder(Parcel in) {
		super(RESPONDER_TYPE.ACK);
		readFromParcel(in);
	}
	
	public static Parcelable.Creator<AckResponder> CREATOR = new Parcelable.Creator<AckResponder>() {

		public AckResponder createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new AckResponder(source);
		}

		public AckResponder[] newArray(int size) {
			// TODO Auto-generated method stub
			return new AckResponder[size];
		}
		
	};

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		setAckWith(in.readString());
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

	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
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

}
