package com.offsetnull.bt.service;

import android.widget.RelativeLayout;

public class LayoutGroup {
	
	public enum LAYOUT_TYPE {
		small,
		normal,
		large,
		xlarge
	}
	
	LAYOUT_TYPE type;
	private RelativeLayout.LayoutParams portraitParams = null;
	private RelativeLayout.LayoutParams landscapeParams = null;
	
	public LayoutGroup() {
		setPortraitParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
		setLandscapeParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
	}

	public void setPortraitParams(RelativeLayout.LayoutParams portraitParams) {
		this.portraitParams = portraitParams;
	}

	public RelativeLayout.LayoutParams getPortraitParams() {
		return portraitParams;
	}

	public void setLandscapeParams(RelativeLayout.LayoutParams landscapeParams) {
		this.landscapeParams = landscapeParams;
	}

	public RelativeLayout.LayoutParams getLandscapeParams() {
		return landscapeParams;
	}
	
	public void setType(LayoutGroup.LAYOUT_TYPE input) {
		this.type = input;
	}
	
	public LayoutGroup.LAYOUT_TYPE getType() {
		return this.type;
	}
	
	public void setPortraitHeight(int size) {
		this.portraitParams.height = size;
	}
	
	public void setLandscapeHeight(int size) {
		this.landscapeParams.height = size;
	}
}
