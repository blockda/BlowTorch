package com.happygoatstudios.bt.window;

import android.content.Context;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class AnimatedRelativeLayout extends RelativeLayout{

	Window w = null;
	
	public AnimatedRelativeLayout(Context context,Window w) {
		super(context);
	
		this.w = w;
	}
	
	@Override
	public void onAnimationStart() {
		super.onAnimationStart();
		
		if(!toggle) {
			Log.e("ANIM","CUSTOM HANDLERS START");
			//w.updateDimensions(w.getWidth(),w.getMHeight()+100);
		}
	}
	
	@Override
	public void onAnimationEnd() {
		super.onAnimationEnd();
		
		if(toggle) {
			Log.e("ANIM","CUSTOM HANDLERS END");
			w.updateDimensions(w.getWidth(),w.getMHeight()-100);
		}
	}
	
	@Override
	public void startAnimation(Animation a) {
		super.startAnimation(a);
	}
	
	public void startAnimationX(Animation a,boolean b) {
		super.startAnimation(a);
		toggle = b;
	}
	
	boolean toggle = false;


	 /*@Override
     protected void onAnimationEnd() {
             super.onAnimationEnd();
             
     }

     @Override
     protected void onAnimationStart() {
            
     }*/
	
	

}
