package com.happygoatstudios.bt.window;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;

public class LuaDialog extends Dialog {

	private View mView = null;
	
	public LuaDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LuaDialog(Context context,View v,boolean title,Drawable border) {
		super(context);
		if(!title) {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		if(border != null) {
			this.getWindow().setBackgroundDrawable(border);
		}
		mView = v;		
		this.setContentView(mView);
		
	}
	
	
	
	
}
