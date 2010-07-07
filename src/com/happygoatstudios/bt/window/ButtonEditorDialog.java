package com.happygoatstudios.bt.window;

import com.happygoatstudios.bt.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

public class ButtonEditorDialog extends Dialog {
	
	final int EXIT_CANCEL = 0;
	final int EXIT_DONE = 1;
	final int EXIT_DELETE = 2;
	
	public String mod_cmd = null;
	public String mod_lbl = null;
	public int EXIT_STATE = EXIT_CANCEL;
	
	Handler deleter = null;
	SlickButton the_button = null;
	
	CheckBox move_free = null;
	CheckBox move_nudge = null;
	CheckBox move_freeze = null;
	
	//SlickButtonData orig_data = null;
	
	public ButtonEditorDialog(Context context,SlickButton useme,Handler callback) {
		super(context);
		
		//mod_cmd = cmd;
		//mod_lbl = lbl;
		
		the_button = useme;
		deleter = callback;
		//orig_data = useme.getData().copy();
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);

		this.setTitle("Modify Button Properties...");
		setContentView(R.layout.button_properties_dialog);
		
		EditText label = (EditText)findViewById(R.id.button_text_et);
		label.setText(the_button.getData().getLabel());
		
		EditText command = (EditText)findViewById(R.id.button_command_et);
		command.setText(the_button.getData().getText());
		
		EditText flip = (EditText)findViewById(R.id.button_flip_et);
		flip.setText(the_button.getData().getFlipCommand());
		
		move_free = (CheckBox)findViewById(R.id.move_free);
		move_nudge = (CheckBox)findViewById(R.id.move_nudge);
		move_freeze = (CheckBox)findViewById(R.id.move_freeze);
		//set up radio button handling.
		//set initial checked value
		Log.e("BTNEDITOR","INITIALIZING DIALOG WITH:" + the_button.getMoveMethod());
		switch(the_button.getMoveMethod()) {
		case SlickButtonData.MOVE_FREE:
			move_free.setChecked(true);
			move_nudge.setChecked(false);
			move_freeze.setChecked(false);
			break;
		case SlickButtonData.MOVE_NUDGE:
			move_free.setChecked(false);
			move_nudge.setChecked(true);
			move_freeze.setChecked(false);
			break;
		case SlickButtonData.MOVE_FREEZE:
			move_free.setChecked(false);
			move_nudge.setChecked(false);
			move_freeze.setChecked(true);
			break;
		}
		
		
		move_free.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				//if(!move_free.isChecked()) {
					move_free.setChecked(true);
					move_nudge.setChecked(false);
					move_freeze.setChecked(false);
				//}
			}
		});
		
		move_nudge.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				//if(!move_nudge.isChecked()) {
					move_free.setChecked(false);
					move_nudge.setChecked(true);
					move_freeze.setChecked(false);
				//}
			}
		});
		
		move_freeze.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				//if(!move_freeze.isChecked()) {
					move_free.setChecked(false);
					move_nudge.setChecked(false);
					move_freeze.setChecked(true);
				//}
			}
		});
		
		Button delbutton = (Button)findViewById(R.id.button_delete_btn);
		
		delbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				EXIT_STATE = EXIT_DELETE;
				Message msg = deleter.obtainMessage(SlickView.MSG_REALLYDELETEBUTTON, the_button);
				deleter.sendMessage(msg);
				ButtonEditorDialog.this.dismiss();
			}
		});
		
		Button donebutton = (Button)findViewById(R.id.button_done_btn);
		donebutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				EditText label = (EditText)findViewById(R.id.button_text_et);
				
				
				EditText command = (EditText)findViewById(R.id.button_command_et);
				EditText flip = (EditText)findViewById(R.id.button_flip_et);
				
				the_button.setLabel(label.getText().toString());
				the_button.setText(command.getText().toString());
				the_button.setFlipCommand(flip.getText().toString());
				
				CheckBox tfree = (CheckBox)findViewById(R.id.move_free);
				CheckBox tnudge = (CheckBox)findViewById(R.id.move_nudge);
				CheckBox tfreeze = (CheckBox)findViewById(R.id.move_freeze);
				
				if(tfree.isChecked()) {
					Log.e("BTNEDITOR","SAVING WITH MOVE_FREE");
					the_button.setMoveMethod(SlickButtonData.MOVE_FREE);
				}
				
				if(tnudge.isChecked()) {
					Log.e("BTNEDITOR","SAVING WITH MOVE_NUDGE");
					the_button.setMoveMethod(SlickButtonData.MOVE_NUDGE);
				}
				
				if(tfreeze.isChecked()) {
					the_button.setMoveMethod(SlickButtonData.MOVE_FREEZE);
				}
				the_button.dialog_launched = false;
				the_button.iHaveChanged(the_button.orig_data);
				the_button.invalidate();
				EXIT_STATE = EXIT_DONE;
				ButtonEditorDialog.this.dismiss();
			}
		});
	}

}
