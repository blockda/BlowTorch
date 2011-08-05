package com.happygoatstudios.bt.window;

import com.happygoatstudios.bt.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class FloatingVitalMoveDialog extends Dialog {

	
	enum EDIT_MODE {
		POSITION,
		SIZE
	}

	EDIT_MODE mode = EDIT_MODE.POSITION;
	CustomListener listener = null;
	//SizeListener slisten = null;
	public FloatingVitalMoveDialog(Context context,CustomListener listener) {
		super(context);
		// TODO Auto-generated constructor stub
		this.listener = listener;
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
		this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		this.setContentView(R.layout.blank);
		
		updateEditMode(mode);
		((Button)findViewById(R.id.toggle)).setText("SIZE");
		findViewById(R.id.toggle).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				EDIT_MODE newmode = EDIT_MODE.POSITION;
				switch(mode) {
				case POSITION:
					newmode = EDIT_MODE.SIZE;
					((Button)findViewById(R.id.toggle)).setText("SIZE");
					break;
				case SIZE:
					newmode = EDIT_MODE.POSITION;
					((Button)findViewById(R.id.toggle)).setText("POSITION");
					break;
				}
				//findViewById(R.id.toggle).invalidate();
				updateEditMode(newmode);
				mode = newmode;
			}
		});
		
	}
	
	private void updateEditMode(EDIT_MODE mode) {
		TouchCatcher t = (TouchCatcher) this.findViewById(R.id.touchcatch);
		t.setListener(listener);
		t.setMode(mode);

		
	}
	
	

	
	
	public interface CustomListener {
		public void onMove(int dx,int dy);
		public void onSize(int dx,int dy);
	}
	

		
	
	
}
