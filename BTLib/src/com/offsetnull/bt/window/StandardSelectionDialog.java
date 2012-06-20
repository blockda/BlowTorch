package com.offsetnull.bt.window;

import com.offsetnull.bt.service.IConnectionBinder;

import android.content.Context;
import android.view.View;

public class StandardSelectionDialog extends BaseSelectionDialog {
	
	IConnectionBinder service;
	
	public StandardSelectionDialog(Context context,IConnectionBinder service) {
		super(context);
		this.service = service;
	}


}
