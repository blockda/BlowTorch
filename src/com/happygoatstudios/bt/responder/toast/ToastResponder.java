package com.happygoatstudios.bt.responder.toast;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.TriggerResponder.RESPONDER_TYPE;

public class ToastResponder extends TriggerResponder implements Parcelable {

	private String message;
	private int delay;
	
	public ToastResponder() {
		super(RESPONDER_TYPE.TOAST);
	}
	
	public ToastResponder(RESPONDER_TYPE pType) {
		super(pType);
		// TODO Auto-generated constructor stub
	}
	
	public ToastResponder copy() {
		ToastResponder tmp = new ToastResponder();
		tmp.delay = this.delay;
		tmp.message = this.message;
		tmp.setFireType(this.getFireType());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof ToastResponder)) return false;
		
		ToastResponder test = (ToastResponder)o;
		
		if(test.delay != this.delay) return false;
		if(test.message != this.message) return false;
		if(test.getFireType() != this.getFireType()) return false;
		
		return true;
		
	}
	
	
	public ToastResponder(Parcel in) {
		super(RESPONDER_TYPE.TOAST);
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		
		setMessage(in.readString());
		setDelay(in.readInt());
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
		
		Log.e("TOAST","PARCEL IN:" + message + " |[]| " + delay );
	}

	@Override
	public void doResponse(Context c, String displayname, int triggernumber,
			boolean windowIsOpen,Handler dispatcher) {
		//Handler ha;
		// TODO Auto-generated method stub
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED) {
				return;
			}
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN) {
				return;
			}
		}
		Toast t = Toast.makeText(c, message, delay);
		t.show();
	}

	public static Parcelable.Creator<ToastResponder> CREATOR = new Parcelable.Creator<ToastResponder>() {

		public ToastResponder createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new ToastResponder(source);
		}

		public ToastResponder[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ToastResponder[size];
		}
		
	};

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		Log.e("TOAST","PARCEL OUT:" + message + " |[]| " + delay);
		out.writeString(message);
		out.writeInt(delay);
		out.writeString(this.getFireType().getString());
	}

	public void setMessage(String message) {
		if(message == null) message = "Default Trigger Message";
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getDelay() {
		return delay;
	}

	
}
