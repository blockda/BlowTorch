package com.happygoatstudios.bt.window;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Bar extends View {

	private Paint mPaint = null;
	private int min = 0;
	private int max = 100;
	private int value = 50;
	
	
	public Bar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}

	public Bar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}

	public Bar(Context context) {
				super(context);
				
				init();
	}
	
	public void onDraw(Canvas c) {
		Rect r = new Rect();
		r.bottom = (int) (6 * this.getContext().getResources().getDisplayMetrics().density);
		r.top = 0;
		r.right = c.getWidth();
		r.left = 0;
		
		int width = r.right - r.left;
		int height = r.bottom - r.top;
		if(width > height) {
			float percent = (float)value / (float)max;
			int newWidth = (int) (width * percent);
			r.right = r.left + newWidth;
		} else {
			
		}
		
		
		c.drawRect(r, mPaint);
		
	}
	
	private void init() {
		//Log.e("BAR","BAR CREATED");
		mPaint = new Paint();
		mPaint.setColor(0xFFFF0000);
	}
	
	public void setColor(int color) {
		mPaint.setColor(color);
	}

	public void setValue(int value) {
		//Log.e("BAR","SETTING VALUE:" + value);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setMax(int max) {
		//Log.e("BAR","SETTING MAX:" + max);
		this.max = max;
	}

	public int getMax() {
		return max;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMin() {
		return min;
	}
	
	
		
	
	
}
