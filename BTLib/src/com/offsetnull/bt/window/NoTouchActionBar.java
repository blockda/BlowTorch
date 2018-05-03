package com.offsetnull.bt.window;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

//import android.widget.Toolbar;

public class NoTouchActionBar extends Toolbar {

    public NoTouchActionBar(Context c) {
        super(c);
    }

    public NoTouchActionBar(Context c, AttributeSet s) {
        super(c,s);
    }

    public NoTouchActionBar(Context c, AttributeSet s, int d) {
        super(c,s,d);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
