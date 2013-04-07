package com.offsetnull.bt.responder.script;

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

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.ack.AckResponder;
import com.offsetnull.bt.service.Colorizer;
import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.window.TextTree;

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
	public boolean doResponse(Context c,TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,TextTree.Line line,int start,int end,String matched,Object source, String displayname,String host,int port, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap,LuaState L,String name,String encoding) {
			
			/*L.pushNil();
			while(L.next(LuaState.LUA_GLOBALSINDEX) != 0) {
				String two = L.typeName(L.type(-2));
				String one = L.typeName(L.type(-1));
				Log.e("LUA","value: " + two + " data: " + one);
			}*/
		
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) return false;
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) return false;
		}
		
			L.getGlobal("debug");
			L.getField(L.getTop(), "traceback");
			L.remove(-2);
			
		
			L.getGlobal(function);
			/*if(function.equals("processChat")) {
				int foo = 100;
				foo = foo + function.length();
			}*/
			if(!L.isFunction(L.getTop())) {
				Log.e("LUA",function + " is not a function.");
				L.pop(2);
				return false;
			}
			
			//this is a relativly straightforward matter, push the arguments a la mushclient.
			L.pushString(name);
			L.pushJavaObject(line);
			L.newTable();
			for(int i=0;i<captureMap.size();i++) {
				L.pushString(Integer.toString(i));
				L.pushString(captureMap.get(Integer.toString(i)));
				L.setTable(-3);
			}
			
			//L.call(2, 0);
			if(L.pcall(3,1,-5) != 0) {
				String str = "Error in trigger callback("+function+"): " + L.getLuaObject(-1).getString();
				dispatcher.sendMessage(dispatcher.obtainMessage(Connection.MESSAGE_PLUGINLUAERROR,"\n" + Colorizer.getRedColor() + str + Colorizer.getWhiteColor() + "\n"));
				//dispatcher.sendMessage(dispatcher.obtainMessage(Connection.MESSAGE_TRIGGER_LUA_ERROR, str));
			} else {
				if(L.getLuaObject(-1).getBoolean() == true) {
					return true;
				}
			}
			
			L.pop(2);
			//return 2;
			
			return false;
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
