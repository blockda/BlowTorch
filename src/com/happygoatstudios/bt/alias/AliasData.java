package com.happygoatstudios.bt.alias;


import android.os.Parcel;
import android.os.Parcelable;

public class AliasData implements Parcelable {
	
	private String pre;
	private String post;
	
	public AliasData() {
		pre = "";
		post = "";
	}
	
	public AliasData(String pPre, String pPost) {
		pre = pPre;
		post = pPost;
	}
	
	public AliasData copy() {
		AliasData tmp = new AliasData();
		tmp.pre = this.pre;
		tmp.post = this.post;
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if( !(o instanceof AliasData)) return false;
		AliasData t = (AliasData)o;
		if(!t.pre.equals(this.pre)) return false;
		if(!t.post.equals(this.post)) return false;
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
	}



	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel o, int flags) {
		o.writeString(this.pre);
		o.writeString(this.post);
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
	
	
	
	
	
}
