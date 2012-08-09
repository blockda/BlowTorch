package com.offsetnull.bt.trigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.ack.AckResponder;
import com.offsetnull.bt.responder.color.ColorAction;
import com.offsetnull.bt.responder.gag.GagAction;
import com.offsetnull.bt.responder.notification.NotificationResponder;
import com.offsetnull.bt.responder.replace.ReplaceResponder;
import com.offsetnull.bt.responder.script.ScriptResponder;
import com.offsetnull.bt.responder.toast.ToastResponder;

import android.os.Parcel;
import android.os.Parcelable;

public class TriggerData implements Parcelable {

	private String name;
	private String pattern;
	private boolean interpretAsRegex;
	private boolean fireOnce;
	
	private boolean fired = false;
	
	private boolean hidden = false;
	
	private boolean enabled = true;
	
	private boolean save = true;
	private int sequence = DEFAULT_SEQUENCE;
	private boolean keepEvaluating = DEFAULT_KEEPEVAL;
	private String group = DEFAULT_GROUP;
	private List<TriggerResponder> responders;
	
	private Pattern p = null;
	private Matcher m = null;
	//private Matcher m = null;
	
	public TriggerData() {
		name = "";
		pattern = "";
		interpretAsRegex = false;
		responders = new ArrayList<TriggerResponder>();
		fireOnce = false;
		hidden = false;
		enabled = true;
		sequence = DEFAULT_SEQUENCE;
		group = DEFAULT_GROUP;
		keepEvaluating = DEFAULT_KEEPEVAL;
		buildData();
	}
	
	public TriggerData copy() {
		TriggerData tmp = new TriggerData();
		tmp.name = this.name;
		tmp.pattern = this.pattern;
		tmp.interpretAsRegex = this.interpretAsRegex;
		tmp.fireOnce = this.fireOnce;
		tmp.hidden = this.hidden;
		tmp.enabled = this.enabled;
		tmp.sequence = this.sequence;
		tmp.group = this.group;
		tmp.keepEvaluating = this.keepEvaluating;
		for(TriggerResponder responder : this.responders) {
			tmp.responders.add(responder.copy());
		}
		tmp.buildData();
		return tmp;
	}
	
	private void buildData() {
		//if(p == null || p.equals("")) return;
		if(this.interpretAsRegex) {
			this.p = Pattern.compile(pattern);
		} else {
			this.p = Pattern.compile("\\Q"+pattern+"\\E");
		}
		this.m = p.matcher("");
	}
	
	public Matcher getMatcher() {
		return m;
	}

	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof TriggerData)) return false;
		TriggerData test = (TriggerData)o;
		if(!test.name.equals(this.name)) return false;
		if(!test.pattern.equals(this.pattern)) return false;
		if(test.interpretAsRegex != this.interpretAsRegex) return false;
		if(test.fireOnce != this.fireOnce) return false;
		if(test.hidden != this.hidden) return false;
		if(test.enabled != this.enabled) return false;
		if(test.sequence != this.sequence) return false;
		if(test.group != this.group) return false;
		if(test.keepEvaluating != this.keepEvaluating) return false;
		Iterator<TriggerResponder> test_responders = test.responders.iterator();
		Iterator<TriggerResponder> my_responders = this.responders.iterator();
		while(test_responders.hasNext()) {
			TriggerResponder test_responder = test_responders.next();
			TriggerResponder my_responder = my_responders.next();
			
			if(!test_responder.equals(my_responder)) return false;
		}
		
		return true;
	}
	
	public static final Parcelable.Creator<TriggerData> CREATOR = new Parcelable.Creator<TriggerData>() {

		public TriggerData createFromParcel(Parcel arg0) {
			return new TriggerData(arg0);
		}

		public TriggerData[] newArray(int arg0) {
			return new TriggerData[arg0];
		}
	};
	public static final int DEFAULT_SEQUENCE = 10;
	public static final String DEFAULT_GROUP = "";
	public static final boolean DEFAULT_KEEPEVAL = true;
	public TriggerData(Parcel in) {
		readFromParcel(in);
		buildData();
	}
	
	public void readFromParcel(Parcel in) {
		setName(in.readString());
		setPattern(in.readString());
		setResponders(new ArrayList<TriggerResponder>());
		setInterpretAsRegex( (in.readInt() == 1) ? true : false);
		setFireOnce ((in.readInt() == 1) ? true : false);
		setHidden( (in.readInt() == 1) ? true : false);
		setEnabled( (in.readInt() == 1) ? true : false);
		setSequence((in.readInt()));
		setGroup(in.readString());
		setKeepEvaluating((in.readInt() == 1) ? true : false);
		int numresponders = in.readInt();
		for(int i = 0;i<numresponders;i++) {
			int type = in.readInt();
			switch(type) {
			case TriggerResponder.RESPONDER_TYPE_NOTIFICATION:
				NotificationResponder resp = in.readParcelable(com.offsetnull.bt.responder.notification.NotificationResponder.class.getClassLoader());
				
				responders.add(resp);
				break;
			case TriggerResponder.RESPONDER_TYPE_TOAST:
				ToastResponder toasty = in.readParcelable(com.offsetnull.bt.responder.toast.ToastResponder.class.getClassLoader());

				responders.add(toasty);
				break;
			case TriggerResponder.RESPONDER_TYPE_ACK:
				AckResponder ack = in.readParcelable(com.offsetnull.bt.responder.ack.AckResponder.class.getClassLoader());
				
				responders.add(ack);
				break;
			case TriggerResponder.RESPONDER_TYPE_SCRIPT:
				ScriptResponder scr = in.readParcelable(com.offsetnull.bt.responder.script.ScriptResponder.class.getClassLoader());
				
				responders.add(scr);
				break;
			case TriggerResponder.RESPONDER_TYPE_GAG:
				GagAction gag = in.readParcelable(com.offsetnull.bt.responder.gag.GagAction.class.getClassLoader());
				responders.add(gag);
				break;
			case TriggerResponder.RESPONDER_TYPE_REPLACE:
				ReplaceResponder rep = in.readParcelable(com.offsetnull.bt.responder.replace.ReplaceResponder.class.getClassLoader());
				responders.add(rep);
				break;
			case TriggerResponder.RESPONDER_TYPE_COLOR:
				ColorAction color = in.readParcelable(com.offsetnull.bt.responder.color.ColorAction.class.getClassLoader());
				responders.add(color);
				break;
			}
		}
	}
	
	//save these for later.
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(name);
		out.writeString(pattern);
		out.writeInt( interpretAsRegex ? 1 : 0);
		out.writeInt(fireOnce ? 1 : 0);
		out.writeInt(hidden ? 1 : 0);
		out.writeInt(enabled ? 1 : 0);
		out.writeInt(sequence);
		out.writeString(group);
		out.writeInt(keepEvaluating ? 1 : 0);
		out.writeInt(responders.size());
		for(TriggerResponder responder : responders) {
			//if(responder instanceof GagAction) {
				
			//} else {
				out.writeInt(responder.getType().getIntVal());
				out.writeParcelable(responder, 0);
			//}
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		buildData();
	}

	public String getPattern() {
		return pattern;
	}

	public void setInterpretAsRegex(boolean interpretAsRegex) {
		this.interpretAsRegex = interpretAsRegex;
		//buildData();
	}

	public boolean isInterpretAsRegex() {
		return interpretAsRegex;
	}

	public void setResponders(List<TriggerResponder> responders) {
		this.responders = responders;
	}

	public List<TriggerResponder> getResponders() {
		return responders;
	}

	public void setFireOnce(boolean fireOnce) {
		this.fireOnce = fireOnce;
	}

	public boolean isFireOnce() {
		return fireOnce;
	}

	public void setFired(boolean fired) {
		this.fired = fired;
	}

	public boolean isFired() {
		return fired;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public Pattern getCompiledPattern() {
		return p;
	}
	//public boolean matches(CharSequence text) {
		//m.reset(text);

	public void setSave(boolean save) {
		this.save = save;
	}

	public boolean isSave() {
		return save;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public boolean isKeepEvaluating() {
		return keepEvaluating;
	}

	public void setKeepEvaluating(boolean keepEvaluating) {
		this.keepEvaluating = keepEvaluating;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
		
	//}

}
