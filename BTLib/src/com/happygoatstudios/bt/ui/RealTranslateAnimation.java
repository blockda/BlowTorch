package com.happygoatstudios.bt.ui;

import android.view.animation.TranslateAnimation;

public class RealTranslateAnimation extends TranslateAnimation{

	public RealTranslateAnimation(float fromXDelta, float toXDelta,
			float fromYDelta, float toYDelta) {
		super(fromXDelta, toXDelta, fromYDelta, toYDelta);
		// TODO Auto-generated constructor stub
	}
	
	public boolean willChangeTransformationMatrix() {
		return true;
	}

}
