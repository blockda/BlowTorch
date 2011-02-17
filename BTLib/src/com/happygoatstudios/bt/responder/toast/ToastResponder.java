package com.happygoatstudios.bt.responder.toast;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.widget.Toast;

import com.happygoatstudios.bt.launcher.Launcher.LAUNCH_MODE;
import com.happygoatstudios.bt.responder.TriggerResponder;

public class ToastResponder extends TriggerResponder implements Parcelable {

	private String message;
	private int delay;
	
	public ToastResponder() {
		super(RESPONDER_TYPE.TOAST);
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
		message = "";
		delay = 2;
	}
	
	public ToastResponder(RESPONDER_TYPE pType) {
		super(pType);
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
		
		//Log.e("TOAST","PARCEL IN:" + message + " |[]| " + delay );
	}

	@Override
	public void doResponse(Context c, String displayname, int triggernumber,
			boolean windowIsOpen,Handler dispatcher,HashMap<String,String> captureMap,LAUNCH_MODE mode) {
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) {
				return;
			}
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN  || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) {
				return;
			}
		}
		
		String translated = ToastResponder.this.translate(message, captureMap);
		Toast t = Toast.makeText(c, translated, delay);
		float density = c.getResources().getDisplayMetrics().density;
		t.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int) (50*density));
		t.show();
	}

	public static Parcelable.Creator<ToastResponder> CREATOR = new Parcelable.Creator<ToastResponder>() {

		public ToastResponder createFromParcel(Parcel source) {
			return new ToastResponder(source);
		}

		public ToastResponder[] newArray(int size) {
			return new ToastResponder[size];
		}
		
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
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
