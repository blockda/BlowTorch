/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import android.widget.RelativeLayout;

/** Represents a group of layouts for small, normal, large and xlarge screen sizes. */
public class LayoutGroup {
	
	/** The types of layouts that can be part of this group. */
	public enum LAYOUT_TYPE {
		/** Small screens. */
		small,
		/** Normal screens. */
		normal,
		/** Large screens. */
		large,
		/** Extra large screens. */
		xlarge
	}
	
	/** The type of this layout group. */
	private LAYOUT_TYPE mType;
	/** The portrait layout params for this layout group. */
	private RelativeLayout.LayoutParams mPortraitParams = null;
	/** The landscape layout params for this layout group. */
	private RelativeLayout.LayoutParams mLandscapeParams = null;
	
	/** Generic constructor. */
	@SuppressWarnings("deprecation")
	public LayoutGroup() {
		setPortraitParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		setLandscapeParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
	}

	/** mPortraitParams setter.
	 * 
	 * @param portraitParams new layout parameters.
	 */
	public final void setPortraitParams(final RelativeLayout.LayoutParams portraitParams) {
		this.mPortraitParams = portraitParams;
	}

	/** mPortraitParams getter.
	 * 
	 * @return the layout parameter object.
	 */
	public final RelativeLayout.LayoutParams getPortraitParams() {
		return mPortraitParams;
	}

	/** mLandscapeParams setter.
	 * 
	 * @param landscapeParams the new layout params.
	 */
	public final void setLandscapeParams(final RelativeLayout.LayoutParams landscapeParams) {
		this.mLandscapeParams = landscapeParams;
	}

	/** Getter for mLandscapeParams. 
	 * 
	 * @return the layout parameter object.
	 */
	public final RelativeLayout.LayoutParams getLandscapeParams() {
		return mLandscapeParams;
	}
	
	/** Setter for mType.
	 * 
	 * @param input The new layout group type to use.
	 */
	public final void setType(final LayoutGroup.LAYOUT_TYPE input) {
		this.mType = input;
	}
	
	/** Getter for mType.
	 * 
	 * @return The layout group type for this object.
	 */
	public final LayoutGroup.LAYOUT_TYPE getType() {
		return this.mType;
	}
	
	/** Utility method to quickly set the portrait height of this group.
	 * 
	 * @param size The new size in pixels (or LayoutParamter rule) to use.
	 */
	public final void setPortraitHeight(final int size) {
		this.mPortraitParams.height = size;
	}
	
	/** Utility method to quickly set the landscape height of this group.
	 * 
	 * @param size The new size in pixels (or LayoutParamter rule) to use.
	 */
	public final void setLandscapeHeight(final int size) {
		this.mLandscapeParams.height = size;
	}
}
