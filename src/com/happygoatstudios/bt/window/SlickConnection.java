package com.happygoatstudios.bt.window;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public class SlickConnection extends BaseInputConnection {

	public SlickConnection(View targetView, boolean fullEditor) {
		super(targetView, fullEditor);
		//Log.e("CONNECTION","CONSTRUCTING NEW SLICK CONNECTION FOR KEYBOARD AWSOMENESS!");
	}




}
