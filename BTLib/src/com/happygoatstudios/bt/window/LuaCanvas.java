package com.happygoatstudios.bt.window;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;

public class LuaCanvas extends Canvas {

	public LuaCanvas(Bitmap bmp) {
		super(bmp);
	}

	public void clearCanvas() {
		this.drawColor(0,PorterDuff.Mode.CLEAR);
	}
	Paint clearPaint = new Paint();
	Xfermode mode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
	public void clearRect(RectF rect) {
		//this.drawRe
		clearPaint.setXfermode(mode);
		clearPaint.setColor(0);
		this.drawRect(rect, clearPaint);
	}
	
	
	
}
