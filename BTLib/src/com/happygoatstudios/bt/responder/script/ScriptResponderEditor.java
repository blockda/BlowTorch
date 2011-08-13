package com.happygoatstudios.bt.responder.script;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponderEditorDoneListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class ScriptResponderEditor extends Dialog {

	private ScriptResponder the_responder;
	private ScriptResponder original;
	
	private TriggerResponderEditorDoneListener finish_with;
	
	private boolean isEditor = false;
	
	public ScriptResponderEditor(Context context,ScriptResponder input,TriggerResponderEditorDoneListener listener) {
		super(context);
		finish_with = listener;
		if(input == null) {
			the_responder = new ScriptResponder();
		} else {
			the_responder = input.copy();
			original = input.copy();
			isEditor = true;
		}
		
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.responder_script_dialog);
		
		EditText function = (EditText)findViewById(R.id.function);
		function.setText(the_responder.getFunction());
		
		Button done = (Button)findViewById(R.id.done);
		done.setOnClickListener(new DoneListener());
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ScriptResponderEditor.this.dismiss();
			}
		});
		
	}
	
	private class DoneListener implements View.OnClickListener {

		public void onClick(View arg0) {
			EditText function = (EditText)findViewById(R.id.function);
			//ackwith.setText(the_responder.getAckWith());
			the_responder.setFunction(function.getText().toString());
			if(isEditor) {
				finish_with.editTriggerResponder(the_responder, original);
			} else {
				finish_with.newTriggerResponder(the_responder);
			}
			ScriptResponderEditor.this.dismiss();
		}
		
	};

}
