package com.happygoatstudios.bt.window;

import com.happygoatstudios.bt.window.FloatingVitalMoveDialog.CustomListener;
import com.happygoatstudios.bt.window.FloatingVitalMoveDialog.EDIT_MODE;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class TouchCatcher extends LinearLayout {

	CustomListener listener = null;
	
	EDIT_MODE mode = null;
	
	public TouchCatcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public TouchCatcher(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setListener(CustomListener listener) {
		this.listener = listener;
	}
	
	public void setMode(EDIT_MODE mode) {
		this.mode = mode;
	}
	
	int x = 0;
	int y = 0;
	public boolean onTouchEvent(MotionEvent e) {
		
		
		int tmpX = (int) e.getX(0);
		int tmpY = (int) e.getY(0);
		
		switch(e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			x = tmpX;
			y = tmpY;
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			int dX = tmpX - x;
			int dY = tmpY - y;
			Log.e("LOG","LISTENER CLASS" + listener.getClass().toString());
			switch(mode) {
			case POSITION:
				listener.onMove(dX,dY);
				break;
			case SIZE:
				listener.onSize(dX,dY);
				break;
			}
			
			
			x = tmpX;
			y = tmpY;
			break;
		}
		
		return true;
		
		
		
		
		//return true;
	}

}
