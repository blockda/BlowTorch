package com.happygoatstudios.bt.alias;


import android.os.Parcel;
import android.os.Parcelable;

public class AliasData implements Parcelable {
	
	private String pre;
	private String post;
	private boolean enabled;
	
	public AliasData() {
		pre = "";
		post = "";
		enabled = true;
	}
	
	public AliasData(String pPre, String pPost,boolean enabled) {
		pre = pPre;
		post = pPost;
		this.enabled = enabled;
	}
	
	public AliasData copy() {
		AliasData tmp = new AliasData();
		tmp.pre = this.pre;
		tmp.post = this.post;
		tmp.enabled = this.enabled;
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if( !(o instanceof AliasData)) return false;
		AliasData t = (AliasData)o;
		if(!t.pre.equals(this.pre)) return false;
		if(!t.post.equals(this.post)) return false;
		if(t.enabled != this.enabled) return false;
		return true;
	}
	
	public static final Parcelable.Creator<AliasData> CREATOR = new Parcelable.Creator<AliasData>() {

		public AliasData createFromParcel(Parcel arg0) {
			return new AliasData(arg0);
		}

		public AliasData[] newArray(int arg0) {
			return new AliasData[arg0];
		}
	};
	
	public AliasData(Parcel p) {
		readFromParcel(p);
	}

	private void readFromParcel(Parcel p) {
		this.pre = p.readString();
		this.post = p.readString();
		this.setEnabled((p.readInt() == 0) ? false : true);
	}



	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		o.writeString(this.pre);
		o.writeString(this.post);
		if(this.enabled) {
			o.writeInt(1);
		} else {
			o.writeInt(0);
		}
	}

	public String getPre() {
		return pre;
	}

	public void setPre(String pre) {
		this.pre = pre;
	}

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
	
	
	
}
