package com.happygoatstudios.bt.window;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class AnimatedRelativeLayout extends RelativeLayout{

	public interface OnAnimationEndListener {
		public void onCustomAnimationEnd();
	}
	
	public AnimatedRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AnimatedRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	OnAnimationEndListener w = null;
	//LayerManager parent = null;
	public AnimatedRelativeLayout(Context context,OnAnimationEndListener w) {
		super(context);
		//this.parent = m;
		this.w = w;
	}
	
	public AnimatedRelativeLayout(Context context) {
		super(context);
		this.w = null;
		//this.parent = null;
	}
	
	public void setAnimationListener(OnAnimationEndListener w) {
		this.w = w;
	}
	
	@Override
	public void onAnimationStart() {
		super.onAnimationStart();
		
		//if(!toggle) {
		//	Log.e("ANIM","CUSTOM HANDLERS START");
			//w.updateDimensions(w.getWidth(),w.getMHeight()+100);
		//}
	}
	
	@Override
	public void onAnimationEnd() {
		super.onAnimationEnd();
		
		//if(toggle) {
			Log.e("ANIM","CUSTOM HANDLERS END");
			//w.updateDimensions(w.getWidth(),w.getMHeight()-100);
			if(w != null) {
				w.onCustomAnimationEnd();
			}
		//}
	}
	
	
	@Override
	public void startAnimation(Animation a) {
		super.startAnimation(a);
	}
	
	public void bringToFront() {
		//parent.bringToFront(this);
	}
	
	//public void startAnimationX(Animation a,boolean b) {
		//super.startAnimation(a);
		//toggle = b;
	//}
	
	//boolean toggle = false;


	 /*@Override
     protected void onAnimationEnd() {
             super.onAnimationEnd();
             
     }

     @Override
     protected void onAnimationStart() {
            
     }*/
	
	public void setDimensions(int width, int height) {
		//mWidth = width;
		//mHeight = height;
		//calculateCharacterFeatures(mWidth,mHeight);
		//View v = ((View)this.getParent());
		RelativeLayout.LayoutParams p = (LayoutParams) this.getLayoutParams();
		p.height = height;
		p.width = width;
		
		//Log.e("WINDOW","WINDOW HEIGHT NOW:" + mHeight);
		//v.setLayoutParams(p);
		//v.requestLayout();
		//this.requestLayout();
	}
	
	public void setWidth(int width) {
		RelativeLayout.LayoutParams p = (LayoutParams) this.getLayoutParams();
		//p.height = height;
		p.width = width;
	}
	
	public void setHeight(int height) {
		RelativeLayout.LayoutParams p = (LayoutParams) this.getLayoutParams();
		p.height = height;
		//p.width = width;
	}
	
	/*@Override
	protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
		Log.e("RELATIVELAYOUT","LAYOUT CHANGING AT HOLDER RELATIVELAYOUT:" + changed);
		if(changed) {
			for(int i=0;i<this.getChildCount();i++) {
				View child = this.getChildAt(i);
				child.layout(left, top, right, bottom);
			}
		} else {
			for(int i=0;i<this.getChildCount();i++) {
				View child = this.getChildAt(i);
				if(child.isLayoutRequested()) {
					child.layout(left, top, right, bottom);
				}
			}
		}
	}*/
	

}
