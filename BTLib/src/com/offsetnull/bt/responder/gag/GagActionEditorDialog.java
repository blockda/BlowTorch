package com.offsetnull.bt.responder.gag;

import com.offsetnull.bt.R;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponderEditorDoneListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

public class GagActionEditorDialog extends Dialog {
	TriggerResponder original;
	private TriggerResponderEditorDoneListener finish_with;
	
	int startVal;
	
	CheckBox output;
	CheckBox log;
	
	TextView retarget;

	public GagActionEditorDialog(Context context,TriggerResponder original,TriggerResponderEditorDoneListener listener) {
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
		
		this.setContentView(R.layout.responder_gag_dialog);	
		
		output = (CheckBox) this.findViewById(R.id.gag_output);
		log = (CheckBox)this.findViewById(R.id.gag_log);
		
		retarget = (TextView)this.findViewById(R.id.retarget_text);
		
		if(original != null) {
			output.setChecked(((GagAction)original).isGagOutput());
			log.setChecked(((GagAction)original).isGagLog());
			retarget.setText(((GagAction)original).getRetarget());
			
		} else {
			output.setChecked(true);
			log.setChecked(true);
		}
		
		findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(original != null) {
					GagAction edited = new GagAction();
					edited.setGagLog(log.isChecked());
					edited.setGagOutput(output.isChecked());
					edited.setRetarget(retarget.getText().toString());
					finish_with.editTriggerResponder(edited, original);
				} else {
					GagAction tmp = new GagAction();
					tmp.setGagLog(log.isChecked());
					tmp.setGagOutput(output.isChecked());
					tmp.setRetarget(retarget.getText().toString());
					finish_with.newTriggerResponder(tmp);
				}
				
				GagActionEditorDialog.this.dismiss();
			}
		});
		
		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GagActionEditorDialog.this.dismiss();
			}
		});
	}

}
