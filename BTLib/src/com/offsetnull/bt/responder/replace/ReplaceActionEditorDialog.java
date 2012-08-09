package com.offsetnull.bt.responder.replace;

import com.offsetnull.bt.R;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponderEditorDoneListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class ReplaceActionEditorDialog extends Dialog {

	TriggerResponder original;
	private TriggerResponderEditorDoneListener finish_with;
	
	TextView with;
	
	TextView retarget;

	public ReplaceActionEditorDialog(Context context,TriggerResponder original,TriggerResponderEditorDoneListener listener) {
		super(context);
		
		this.original = original;
		finish_with = listener;
		
		//if(original != null) {
		//	startVal
		//}
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		this.setContentView(R.layout.responder_script_dialog);
		
		((TextView)findViewById(R.id.titlebar)).setText("REPLACE RESPONDER");
		((TextView)findViewById(R.id.action_label)).setText("Replace triggered text with:");
		
		with = (TextView)findViewById(R.id.function);
		
		findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(original != null) {
					ReplaceResponder tmp = new ReplaceResponder();
					tmp.setWith(with.getText().toString());
					tmp.setFireType(original.getFireType());
					finish_with.editTriggerResponder(tmp, original);
				} else {
					ReplaceResponder tmp = new ReplaceResponder();
					tmp.setWith(with.getText().toString());
					finish_with.newTriggerResponder(tmp);
				}
				
				ReplaceActionEditorDialog.this.dismiss();
			}
		});
		
		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ReplaceActionEditorDialog.this.dismiss();
			}
		});
		
		if(original != null) {
			with.setText(((ReplaceResponder)original).getWith());
		}
	}
}
