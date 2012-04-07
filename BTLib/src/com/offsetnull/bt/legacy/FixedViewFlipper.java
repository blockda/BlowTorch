package com.offsetnull.bt.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class FixedViewFlipper extends ViewFlipper {
	public FixedViewFlipper(Context context) {
		super(context);
	}
	
	public FixedViewFlipper(Context context,AttributeSet attrib) {
		super(context,attrib);
	}
	
	@Override
	protected void onDetachedFromWindow()  {
		try{
			super.onDetachedFromWindow();
		} catch (IllegalArgumentException e) {
			//do something because the regular viewflipper cant.
			stopFlipping(); //http://code.google.com/p/android/issues/detail?id=6191
		}
		
	}
}
