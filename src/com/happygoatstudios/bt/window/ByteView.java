package com.happygoatstudios.bt.window;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ByteView extends SurfaceView implements SurfaceHolder.Callback {

	
	
	public ByteView(Context context) {
		super(context);
		getHolder().addCallback(this);
		init();
	}
	
	private void init() {
		
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	
}
