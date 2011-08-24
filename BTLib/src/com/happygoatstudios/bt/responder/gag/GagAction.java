package com.happygoatstudios.bt.responder.gag;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.keplerproject.luajava.LuaState;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.window.TextTree;
import com.happygoatstudios.bt.window.TextTree.Line;

public class GagAction extends TriggerResponder implements Parcelable {
	public static boolean DEFAULT_GAGLOG = true;
	public static boolean DEFAULT_GAGOUTPUT = true;
	
	private boolean gagLog = DEFAULT_GAGLOG;
	private boolean gagOutput = DEFAULT_GAGOUTPUT;
	

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
	public void doResponse(Context c, TextTree tree,Line line, Matcher matched,
			Object source, String displayname, int triggernumber,
			boolean windowIsOpen, Handler dispatcher,
			HashMap<String, String> captureMap, LuaState L, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TriggerResponder copy() {
		GagAction tmp = new GagAction(RESPONDER_TYPE.GAG);
		tmp.setGagLog(this.isGagLog());
		tmp.setGagOutput(this.gagOutput);
		
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

}
