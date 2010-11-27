package com.happygoatstudios.bt.window;

import android.content.Context;
import android.util.AttributeSet;
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
				attrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
				InputConnection tmp = super.onCreateInputConnection(attrs);
				return new InputConnectionWrapper(tmp,true);
			} else {
				//Log.e("WINDOW","USING BASEINPUTCONNECTION");
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
