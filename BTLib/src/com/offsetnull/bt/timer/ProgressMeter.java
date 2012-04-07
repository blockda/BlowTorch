package com.offsetnull.bt.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class ProgressMeter extends View {

	private float progress;
	private float range;
	
	public ProgressMeter(Context context) {
		super(context);
		
		init();
	}
	public ProgressMeter(Context context,AttributeSet set) {
		super(context,set);
		init();
	}
	
	private void init() {
		progress = 25;
		range = 100;
	}
	
	//private int indicatorWidth = 10;
	
	public void onDraw(Canvas c) {
		//Log.e("PROGRESS","DRAWING THE PROGRESS BAR");
		
		//float center_x = (this.getRight() - this.getLeft())/2;
		//float center_y = (this.getBottom() - this.getTop())/2;
		int indicator_pos = (int) (this.getWidth()*(progress/range));
		//c.translate(center_x, center_y);
		//this.getP
		Paint p = new Paint();
		int yellow = 0xFFEDBF24;
		//int orange = 0xFFED6124;
		int[] colors = { 0xFFFF0000, yellow, 0xFF00FF00 };
		float[] pos = { 0f , 0.3f , 1f };
		Shader s = new LinearGradient(0,0,this.getRight(),0,colors,pos,Shader.TileMode.REPEAT);
		
		p.setStrokeWidth(19*getResources().getDisplayMetrics().density);
		
		p.setShader(s);
		//p.
		Rect r = new Rect();
		r.top = this.getTop();
		r.bottom = this.getBottom();
		r.left = this.getLeft();
		r.right = this.getRight();
		
		Paint alt = new Paint();
		alt.setColor(0xFF030303);
		c.drawRect(r, alt);
		c.drawLine(0, 0, indicator_pos, 0, p);
		
		
	}
	public void setProgress(float progress) {
		this.progress = progress;
	}
	public float getProgress() {
		return progress;
	}
	public void setRange(float range) {
		this.range = range;
	}
	public float getRange() {
		return range;
	}
	
	

}
