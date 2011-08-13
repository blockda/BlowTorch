package com.happygoatstudios.bt.responder.script;

import java.util.HashMap;

import org.keplerproject.luajava.LuaState;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;

public class ScriptResponder extends TriggerResponder {

	String function = ""; //thats it so far.
	
	public ScriptResponder(RESPONDER_TYPE pType) {
		super(pType);
		// TODO Auto-generated constructor stub
		function = "";
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
	}
	
	public String getFunction() {
		return function;
	}
	
	public void setFunction(String function) {
		this.function = function;
	}

	public ScriptResponder(Parcel source) {
		super(RESPONDER_TYPE.SCRIPT);
		function = source.readString();
		String fireType = source.readString();
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


	public ScriptResponder() {
		super(RESPONDER_TYPE.SCRIPT);
		function = "";
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
	}


	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(function);
		dest.writeString(this.getFireType().getString());
	}
	
	public static Parcelable.Creator<ScriptResponder> CREATOR = new Parcelable.Creator<ScriptResponder>() {

		public ScriptResponder createFromParcel(Parcel source) {
			return new ScriptResponder(source);
		}

		public ScriptResponder[] newArray(int size) {
			return new ScriptResponder[size];
		}
		
	};

	@Override
	public void doResponse(Context c, String displayname, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap,LuaState L,String name) {
		
			L.getGlobal(function);
			//this is a relativly straightforward matter, push the arguments a la mushclient.
			L.pushString(name);
			L.newTable();
			for(int i=0;i<captureMap.size();i++) {
				L.pushString(Integer.toString(i));
				L.pushString(captureMap.get(Integer.toString(i)));
				L.setTable(-3);
			}
			
			L.call(2, 0);
			//return 2;
			
	}

	@Override
	public ScriptResponder copy() {
		ScriptResponder tmp = new ScriptResponder();
		tmp.function = this.function;
		tmp.setFireType(this.getFireType());
		
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof ScriptResponder)) return false;
		ScriptResponder tmp = (ScriptResponder)o;
		if(!this.function.equals(tmp.function)) return false;
		if(this.getFireType() != tmp.getFireType()) return false;
		return true;
	}
	

}
