package com.happygoatstudios.bt.button.manager;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class ButtonManagerDialog extends Dialog {
	IStellarService service = null;
	String set = "";
	
	
	public ButtonManagerDialog(String set, IStellarService service, Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.set = set;
		this.service = service;
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0x33777777));
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		this.setContentView(R.layout.button_manager);
		
	}
	
	public void onStart() {
		
	}

}
