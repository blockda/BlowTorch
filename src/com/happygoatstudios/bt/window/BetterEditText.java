package com.happygoatstudios.bt.window;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
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
		// TODO Auto-generated constructor stub
		//super.m
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
		//SlickConnection slick = new SlickConnection(this,false);
		//attrs.
		//slick.
		//attrs.imeOptions=EditorInfo.IME_ACTION_SEND|EditorInfo.IME_FLAG_NO_EXTRACT_UI;
		//attrs.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE;
		attrs.imeOptions = this.getImeOptions();
		attrs.inputType = this.getInputType();
		attrs.fieldId = this.getId();
		//attrs.label = "Command:";
		//attrs.actionLabel = "Fuuck:";
		//attrs.fieldName = "input_box";
		
		//super.onCreateInputConnection(attrs);
		
		//boolean usefullscreen = ((this.getImeOptions()&EditorInfo.IME_FLAG_NO_EXTRACT_UI) == EditorInfo.IME_FLAG_NO_EXTRACT_UI) ? false : true;
		
		//return new SlickConnection(this,usefullscreen);
		if(useFullScreen) {
			return super.onCreateInputConnection(attrs);
		} else {
			if(BackSpaceBugFix) {
				//Log.e("WINDOW","USING INPUTCONNECTIONWRAPPER");
				attrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
				InputConnection tmp = super.onCreateInputConnection(attrs);
				return new InputConnectionWrapper(tmp,true);
			} else {
				//Log.e("WINDOW","USING BASEINPUTCONNECTION");
				return new BaseInputConnection(this,false);	
			}
		}
		//SlickConnection conn = new SlickConnection(this,usefullscreen);
		//InputConnectionWrapper wrapper = new InputConnectionWrapper(conn, false) {
		//wrapper.setTarget(conn);
		//};
		//InputConnectionWrapper wrapper = new InputConnectionWrapper(super.onCreateInputConnection(attrs), false);
		
		//InputConnection conny = super.onCreateInputConnection(attrs);
		
		//return wrapper;
		
		
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
	}

	public Boolean getBackSpaceBugFix() {
		return BackSpaceBugFix;
	}
	
	//protected boolean getDefaultEditable() {
	//	return true;
	//}
	
	//public Editable getText() {
	//	return (Editable)super.getText();
	//}
}
