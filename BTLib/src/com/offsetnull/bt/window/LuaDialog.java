package com.offsetnull.bt.window;

import com.offsetnull.bt.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;

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
		} else {
			this.getWindow().setBackgroundDrawableResource(com.offsetnull.bt.R.drawable.dialog_window_crawler1);
		}
		
		//Window w = this.getWindow();
		
		//WindowManager.LayoutParams wparams = w.getAttributes();
		//params
		//wparams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		//wparams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		
		
		//w.setAttributes(wparams);
		mView = v;	
		//ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		//mView.setLayoutParams(params);
		if(mView.getLayoutParams() != null) {
			this.setContentView(mView,mView.getLayoutParams());
		} else {
			this.setContentView(mView);
		}
	}
	
	
	
	
}
