package com.happygoatstudios.bt.window;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class BetterEditText extends EditText {

	public BetterEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public BetterEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public BetterEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void onAnimationEnd() {
		//Log.e("BETTEREDIT","BETTER EDIT TEXT ANIMATION COMPLETE");
	}
	
	public InputConnection onCreateInputConnection(EditorInfo attrs) {
		//Log.e("BETTEREDIT","RETURNING NEW INPUT CONNECTION!");
		SlickConnection slick = new SlickConnection(this,false);
		//attrs.
		//slick.
		attrs.imeOptions=EditorInfo.IME_ACTION_SEND|EditorInfo.IME_FLAG_NO_EXTRACT_UI;
		attrs.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE;
		
		return new SlickConnection(this,false);
		//return super.onCreateInputConnection(attrs);
		
	}
	
	public boolean onCheckIsTextEditor() {
		//Log.e("BETTEREDIT","CHECKING IF TEXT EDITOR: super returns: " + super.onCheckIsTextEditor());
		return true;
	}
}
