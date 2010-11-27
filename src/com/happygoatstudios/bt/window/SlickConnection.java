package com.happygoatstudios.bt.window;

import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
//import android.util.Log;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.TextView;

public class SlickConnection extends BaseInputConnection {

	  ///private static final boolean DEBUG = false;
	    //private static final String TAG = "EditableInputConnection";

	    private final TextView mTextView;

	    public SlickConnection(TextView textview,boolean usefull) {
	        super(textview, usefull);
	        mTextView = textview;
	    }

	    public Editable getEditable() {
	        TextView tv = mTextView;
	        if (tv != null) {
	            return tv.getEditableText();
	        }
	        return null;
	    }
	    
	    public boolean beginBatchEdit() {
	        mTextView.beginBatchEdit();
	        return true;
	    }
	    
	    public boolean endBatchEdit() {
	        mTextView.endBatchEdit();
	        return true;
	    }
	    
	    public boolean clearMetaKeyStates(int states) {
	        final Editable content = getEditable();
	        if (content == null) return false;
	        KeyListener kl = mTextView.getKeyListener();
	        if (kl != null) {
	            try {
	                kl.clearMetaKeyState(mTextView, content, states);
	            } catch (AbstractMethodError e) {
	                // This is an old listener that doesn't implement the
	                // new method.
	            }
	        }
	        return true;
	    }
	    
	    public boolean commitCompletion(CompletionInfo text) {
	        //if (DEBUG) Log.v(TAG, "commitCompletion " + text);
	        mTextView.beginBatchEdit();
	        mTextView.onCommitCompletion(text);
	        mTextView.endBatchEdit();
	        return true;
	    }

	    public boolean performEditorAction(int actionCode) {
	        //if (DEBUG) Log.v(TAG, "performEditorAction " + actionCode);
	        mTextView.onEditorAction(actionCode);
	        return true;
	    }
	    
	    public boolean performContextMenuAction(int id) {
	        //if (DEBUG) Log.v(TAG, "performContextMenuAction " + id);
	        mTextView.beginBatchEdit();
	        mTextView.onTextContextMenuItem(id);
	        mTextView.endBatchEdit();
	        return true;
	    }
	    
	    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
	        if (mTextView != null) {
	            ExtractedText et = new ExtractedText();
	            
	            if (mTextView.extractText(request, et)) {
	            	//Log.e("CONNECTOIN","Extracted: " + et.text);
	                if ((flags&GET_EXTRACTED_TEXT_MONITOR) != 0) {
	                    //mTextView.setExtractedText(et);
	                	//mTextView.setExtracting(request);
	                	//mTextView.extr
	                	//ExtractEditText e;
	                	//this.
	                }
	                return et;
	            }
	        }
	        return null;
	    }
	    
	    public boolean performPrivateCommand(String action, Bundle data) {
	        mTextView.onPrivateIMECommand(action, data);
	        return true;
	    }

	    @Override
	    public boolean commitText(CharSequence text, int newCursorPosition) {
	        if (mTextView == null) {
	            return super.commitText(text, newCursorPosition);
	        }

	        CharSequence errorBefore = mTextView.getError();
	        boolean success = super.commitText(text, newCursorPosition);
	        CharSequence errorAfter = mTextView.getError();

	        if (errorAfter != null && errorBefore == errorAfter) {
	            mTextView.setError(null, null);
	        }

	        return success;
	    }


}
