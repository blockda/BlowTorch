package com.happygoatstudios.bt.window;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class BetterEditText extends EditText {

	public BetterEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public BetterEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public BetterEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void onAnimationEnd() {
		Log.e("BETTEREDIT","BETTER EDIT TEXT ANIMATION COMPLETE");
	}
}
