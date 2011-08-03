package com.happygoatstudios.bt.window;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Bar {

	private Paint mPaint = null;
	private int min = 0;
	private int max = 100;
	private int value = 50;
	private Rect rect = null;
	private float density = 1.0f;
	private Rect draw = null;
	private Paint border = null;
	private Paint background = null;
	//private Point origin = null;
	

	private void init(Context c) {
		//Log.e("BAR","BAR CREATED");
		mPaint = new Paint();
		mPaint.setColor(0xFFFF0000);
		border = new Paint();
		border.setColor(0xFF888888);
		//Point origin = new Point();
		background = new Paint();
		background.setColor(0xFF1A1A1A);
		//border.setColor(0x00000000);
		border.setStyle(Paint.Style.STROKE);
		density = c.getResources().getDisplayMetrics().density;
		//mPaint.setStrokeWidth(2*density);
		border.setStrokeWidth(2*density);
		rect = new Rect();
		rect.top = 0;
		rect.left = 0;
		rect.bottom = (int) (6 * density);
		rect.right = (int) (250 * density);
	}

	public Bar(Context context) {
				//super(context);
				
				init(context);
	}
	
	public void draw(Canvas c) {
		if(draw == null) {
			
			draw = new Rect();
		}
		/*Rect r = new Rect();
		r.bottom = (int) (6 * this.getContext().getResources().getDisplayMetrics().density);
		r.top = 0;
		r.right = this.getWidth();
		r.left = 0;*/
		
		draw.top = this.rect.top;
		draw.bottom = this.rect.bottom;
		draw.left = this.rect.left;//, measureSpec)
		
		int width = this.rect.right - this.rect.left;
		int height = this.rect.bottom - this.rect.top;
		int value = this.value;
		if(value < 0) {
			value = 0;
		}
		if(width > height) {
			float percent = (float)value / (float)max;
			int newWidth = (int) (width * percent);
			draw.right = draw.left + newWidth;
		} else {
			
		}
		
		c.drawRect(this.rect, background);
		
		c.drawRect(draw, mPaint);
		
		c.drawRect(this.rect, border);
		
		for(int i=1;i<4;i++) {
			if(width > height)
			{
				float quarter = width * 0.25f;
				Point p1 = new Point();
				Point p2 = new Point();
				p1.x = (int) (quarter*i);
				p1.y = this.rect.top;
				
				p2.x = (int)(quarter*i);
				p2.y = this.rect.bottom;
				c.drawLine(p1.x, p1.y, p2.x, p2.y, border);
			} else {
				
			}
		} 
		
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
	
	public int getHeight() {
		return (int) (Math.abs(this.rect.bottom - this.rect.top));
	}
	
	public int getWidth() {
		return (int) (Math.abs(this.rect.left - this.rect.right));
	}

	public int getRight() {
		// TODO Auto-generated method stub
		return this.rect.right;
	}
	
	public void setRight(int right) {
		this.rect.right = right;
	}
		
	
	
}
