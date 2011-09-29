package com.happygoatstudios.bt.responder.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.keplerproject.luajava.LuaState;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.window.TextTree;

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
	public void doResponse(Context c,TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,TextTree.Line line,Matcher matched,Object source, String displayname, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap,LuaState L,String name) {
			
			/*L.pushNil();
			while(L.next(LuaState.LUA_GLOBALSINDEX) != 0) {
				String two = L.typeName(L.type(-2));
				String one = L.typeName(L.type(-1));
				Log.e("LUA","value: " + two + " data: " + one);
			}*/
		
			L.getGlobal(function);
			if(!L.isFunction(L.getTop())) {
				Log.e("LUA",function + " is not a function.");
				return;
			}
			
			//this is a relativly straightforward matter, push the arguments a la mushclient.
			L.pushString(name);
			L.newTable();
			for(int i=0;i<captureMap.size();i++) {
				L.pushString(Integer.toString(i));
				L.pushString(captureMap.get(Integer.toString(i)));
				L.setTable(-3);
			}
			
			//L.call(2, 0);
			if(L.pcall(2,1,0) != 0) {
				Log.e("FOO","Error running("+function+"): " + L.getLuaObject(-1).getString());
			}
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

	@Override
	public void saveResponderToXML(XmlSerializer out)
			throws IllegalArgumentException, IllegalStateException, IOException {
		ScriptResponderParser.saveScriptResponderToXML(out, this);
		
	}
	

}
