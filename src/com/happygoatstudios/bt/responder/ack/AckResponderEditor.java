package com.happygoatstudios.bt.responder.ack;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponderEditorDoneListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class AckResponderEditor extends Dialog {

	private AckResponder the_responder;
	private AckResponder original;
	
	private TriggerResponderEditorDoneListener finish_with;
	
	private boolean isEditor = false;
	
	public AckResponderEditor(Context context,AckResponder input,TriggerResponderEditorDoneListener listener) {
		super(context);
		// TODO Auto-generated constructor stub
		finish_with = listener;
		if(input == null) {
			the_responder = new AckResponder();
		} else {
			the_responder = input.copy();
			original = input.copy();
			isEditor = true;
		}
		
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		setContentView(R.layout.responder_ack_dialog);
		
		EditText ackwith = (EditText)findViewById(R.id.responder_ack_ackwith);
		ackwith.setText(the_responder.getAckWith());
		
		Button done = (Button)findViewById(R.id.responder_ack_done_button);
		done.setOnClickListener(new DoneListener());
		
		Button cancel = (Button)findViewById(R.id.responder_ack_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AckResponderEditor.this.dismiss();
			}
		});
		
	}
	
	private class DoneListener implements View.OnClickListener {

		public void onClick(View arg0) {
			EditText ackwith = (EditText)findViewById(R.id.responder_ack_ackwith);
			//ackwith.setText(the_responder.getAckWith());
			the_responder.setAckWith(ackwith.getText().toString());
			if(isEditor) {
				finish_with.editTriggerResponder(the_responder, original);
			} else {
				finish_with.newTriggerResponder(the_responder);
			}
			AckResponderEditor.this.dismiss();
		}
		
	};

}
