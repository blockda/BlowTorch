package com.offsetnull.bt.window;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

public class BetterEditText extends EditText {

	private Boolean useFullScreen = false;
	private Boolean BackSpaceBugFix = false;
	
	public BetterEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BetterEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public BetterEditText(Context context) {
		super(context);
	}
	
	public InputConnection onCreateInputConnection(EditorInfo attrs) {
		attrs.imeOptions = this.getImeOptions();
		attrs.inputType = this.getInputType();
		attrs.fieldId = this.getId();
		if(useFullScreen) {
			return super.onCreateInputConnection(attrs);
		} else {
			if(BackSpaceBugFix) {
				//Log.e("WINDOW","USING INPUTCONNECTIONWRAPPER");
				attrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN;
				InputConnection tmp = super.onCreateInputConnection(attrs);
				return new InputConnectionWrapper(tmp,true);
			} else {
				//Log.e("WINDOW","USING BASEINPUTCONNECTION");
				attrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN;
				attrs.inputType |= EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
				return new BaseInputConnection(this,false);	
			}
		}	
	}
	
	public boolean onCheckIsTextEditor() {
		//Log.e("BETTEREDIT","CHECKING IF TEXT EDITOR: super returns: " + super.onCheckIsTextEditor());
		return true;
	}
	
	public void setExtractedText(ExtractedText text) {
		//Log.e("BETTEREDIT","SETTING EXTRACTED TEXT");
		super.setExtractedText(text);
	}

	public void setUseFullScreen(Boolean useFullScreen) {
		this.useFullScreen = useFullScreen;
	}

	public Boolean getUseFullScreen() {
		return useFullScreen;
	}

	public void setBackSpaceBugFix(Boolean backSpaceBugFix) {
		BackSpaceBugFix = backSpaceBugFix;
		//BackSpaceBugFix = true;
	}

	public Boolean getBackSpaceBugFix() {
		return BackSpaceBugFix;
	}
	
	@Override
	protected void onAnimationEnd() {
		Log.e("BET","IN THE ANIMATION END LISTENER");
		super.onAnimationEnd();
		if(listener != null) {
			listener.onAnimationEnd();
		}
	}
	
	
	public void setListener(AnimationEndListener listener) {
		this.listener = listener;
	}

	public AnimationEndListener getListener() {
		return listener;
	}

	private AnimationEndListener listener = null;
	
	public interface AnimationEndListener {
		public void onAnimationEnd();
	}
	
	
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            if(mListener != null) {
            	mListener.onBackPressed();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
	
	BackPressedListener mListener;
	
	public void setOnBackPressedListener(BackPressedListener l) {
		mListener = l;
	}
	
	public interface BackPressedListener {
		public void onBackPressed();
	}
	//protected boolean getDefaultEditable() {
	//	return true;
	//}
	
	//public Editable getText() {
	//	return (Editable)super.getText();
	//}
}
