package com.happygoatstudios.bt.legacy;

import com.happygoatstudios.bt.window.MainWindow;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

class BgestureListener extends SimpleOnGestureListener {
	
	final int SWIPE_MAX_OFF_PATH = 100;
	final int SWIPE_MIN_DISTANCE = 40;
	final int SWIPE_THRESHOLD_VELOCITY = 10;
	
	MainWindow inform = null;
	
	public BgestureListener(MainWindow reportto) {
		super();
		inform = reportto;
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        //Toast.makeText(SelectFilterActivity.this, "Left Swipe", Toast.LENGTH_SHORT`enter code here`).show();
				//inform.flingLeft();
			}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        //Toast.makeText(SelectFilterActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
				///inform.flingRight();
			}
		} catch (Exception e) {
    // nothing
		}
	return false;
	}
}
