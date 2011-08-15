package com.happygoatstudios.bt.window;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;

public class LuaCanvas extends Canvas {

	public LuaCanvas(Bitmap bmp) {
		super(bmp);
	}

	public void clearCanvas() {
		this.drawColor(0,PorterDuff.Mode.CLEAR);
	}
	
}
