package com.offsetnull.bt.window;

import javax.security.auth.PrivateCredentialPermission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.KeyListener;
import android.text.style.SuggestionSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.TextView;

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
		attrs.actionId = EditorInfo.IME_ACTION_SEND;
		attrs.privateImeOptions = this.getPrivateImeOptions();
		attrs.extras = this.getInputExtras(true);
		attrs.actionLabel = "Send";
		
		if(useFullScreen) {
			return super.onCreateInputConnection(attrs);
		} else {
			if(BackSpaceBugFix) {
				//Log.e("WINDOW","USING INPUTCONNECTIONWRAPPER");
				//attrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_FULLSCREEN;
				
				InputConnection tmp = super.onCreateInputConnection(attrs);
				return new InputConnectionWrapper(tmp,true);
			} else {
				//Log.e("WINDOW","USING BASEINPUTCONNECTION");
				//attrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_FULLSCREEN;
				//attrs.inputType = InputType.TYPE_CLASS_TEXT;
				return new EditableInputConnection(this);
			}
		}	
	}
	
	public class EditableInputConnection extends BaseInputConnection {
	    private static final boolean DEBUG = false;
	    private static final String TAG = "EditableInputConnection";

	    private final TextView mTextView;

	    // Keeps track of nested begin/end batch edit to ensure this connection always has a
	    // balanced impact on its associated TextView.
	    // A negative value means that this connection has been finished by the InputMethodManager.
	    private int mBatchEditNesting;

	    public EditableInputConnection(TextView textview) {
	        super(textview, true);
	        mTextView = textview;
	    }

	    @Override
	    public Editable getEditable() {
	        TextView tv = mTextView;
	        if (tv != null) {
	            return tv.getEditableText();
	        }
	        return null;
	    }

	    @Override
	    public boolean beginBatchEdit() {
	        synchronized(this) {
	            if (mBatchEditNesting >= 0) {
	                mTextView.beginBatchEdit();
	                mBatchEditNesting++;
	                return true;
	            }
	        }
	        return false;
	    }

	    @Override
	    public boolean endBatchEdit() {
	        synchronized(this) {
	            if (mBatchEditNesting > 0) {
	                // When the connection is reset by the InputMethodManager and reportFinish
	                // is called, some endBatchEdit calls may still be asynchronously received from the
	                // IME. Do not take these into account, thus ensuring that this IC's final
	                // contribution to mTextView's nested batch edit count is zero.
	                mTextView.endBatchEdit();
	                mBatchEditNesting--;
	                return true;
	            }
	        }
	        return false;
	    }

	    /*//@Override
	    protected void reportFinish() {
	        //super.reportFinish();

	        synchronized(this) {
	            while (mBatchEditNesting > 0) {
	                endBatchEdit();
	            }
	            // Will prevent any further calls to begin or endBatchEdit
	            mBatchEditNesting = -1;
	        }
	    }*/

	    @Override
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

	    @Override
	    public boolean commitCompletion(CompletionInfo text) {
	        if (DEBUG) Log.v(TAG, "commitCompletion " + text);
	        mTextView.beginBatchEdit();
	        mTextView.onCommitCompletion(text);
	        mTextView.endBatchEdit();
	        return true;
	    }

	    /**
	     * Calls the {@link TextView#onCommitCorrection} method of the associated TextView.
	     */
	    @SuppressLint("NewApi")
		@Override
	    public boolean commitCorrection(CorrectionInfo correctionInfo) {
	        if (DEBUG) Log.v(TAG, "commitCorrection" + correctionInfo);
	        mTextView.beginBatchEdit();
	        mTextView.onCommitCorrection(correctionInfo);
	        mTextView.endBatchEdit();
	        return true;
	    }

	    @Override
	    public boolean performEditorAction(int actionCode) {
	        if (DEBUG) Log.v(TAG, "performEditorAction " + actionCode);
	        mTextView.onEditorAction(actionCode);
	        return true;
	    }
	    
	    @Override
	    public boolean performContextMenuAction(int id) {
	        if (DEBUG) Log.v(TAG, "performContextMenuAction " + id);
	        mTextView.beginBatchEdit();
	        mTextView.onTextContextMenuItem(id);
	        mTextView.endBatchEdit();
	        return true;
	    }
	    
	    @Override
	    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
	        if (mTextView != null) {
	            ExtractedText et = new ExtractedText();
	            if (mTextView.extractText(request, et)) {
	                if ((flags&GET_EXTRACTED_TEXT_MONITOR) != 0) {
	                    //mTextView.setExtracting(request);
	                	
	                	//this method is not available to us however if we are using this we don't care about extracted text.
	                }
	                return et;
	            }
	        }
	        return null;
	    }

	    @Override
	    public boolean performPrivateCommand(String action, Bundle data) {
	        mTextView.onPrivateIMECommand(action, data);
	        return true;
	    }

	    @Override
	    public boolean commitText(CharSequence text, int newCursorPosition) {
	        if (mTextView == null) {
	            return super.commitText(text, newCursorPosition);
	        }
	        if (text instanceof Spanned) {
	            //Spanned spanned = ((Spanned) text);
	            //SuggestionSpan[] spans = spanned.getSpans(0, text.length(), SuggestionSpan.class);
	            //InputManager mIMM = (InputManager)mTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	            //mIMM.registerSuggestionSpansForNotification(spans);
	        }

	        //mTextView.resetErrorChangedFlag();
	        boolean success = super.commitText(text, newCursorPosition);
	        //mTextView.hideErrorIfUnchanged();

	        return success;
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
