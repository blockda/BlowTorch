package com.happygoatstudios.bt.speedwalk;


import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.validator.Validator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class SpeedWalkDirectionEditorDialog extends Dialog {

	boolean isEditor = false;
	DirectionEditorDoneListener doneListener = null;
	DirectionData oldData = null;
	
	EditText direction = null;
	EditText command = null;
	
	public SpeedWalkDirectionEditorDialog(Context context,DirectionEditorDoneListener doneListener) {
		super(context);
		// TODO Auto-generated constructor stub
		this.doneListener = doneListener;
	}
	
	public SpeedWalkDirectionEditorDialog(Context context,DirectionEditorDoneListener doneListener,DirectionData old) {
		super(context);
		// TODO Auto-generated constructor stub
		this.doneListener = doneListener;
		oldData = old;
		isEditor = true;
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.speedwalk_new_direction_dialog);
		
		direction = ((EditText)findViewById(R.id.sw_dir));
		command = ((EditText)findViewById(R.id.sw_cmd));
		
		
		if(isEditor) {
			((TextView)findViewById(R.id.titlebar)).setText("EDIT DIRECTION");
			((TextView)findViewById(R.id.sw_dir)).setText(oldData.getDirection());
			((TextView)findViewById(R.id.sw_cmd)).setText(oldData.getCommand());
			((ImageButton)findViewById(R.id.new_sw_done_button)).setImageResource(R.drawable.edit_button);
			findViewById(R.id.new_sw_done_button).setOnClickListener(new View.OnClickListener() {
				
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean passed = validateEntries();
					if(!passed) return;
					DirectionData tmp = new DirectionData(direction.getText().toString(),command.getText().toString());
					
					doneListener.editDirection(oldData, tmp);
					SpeedWalkDirectionEditorDialog.this.dismiss();
				}
			});
		} else {
			((TextView)findViewById(R.id.titlebar)).setText("NEW DIRECTION");
			findViewById(R.id.new_sw_done_button).setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean passed = validateEntries();
					if(!passed) return;
					DirectionData tmp = new DirectionData(direction.getText().toString(),command.getText().toString());
					
					doneListener.newDirection(tmp);
					SpeedWalkDirectionEditorDialog.this.dismiss();
				}
			});
		}
		
		/*findViewById(R.id.new_sw_cancel).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SpeedWalkDirectionEditorDialog.this.dismiss();
			}
		});*/
		
	}
	
	private boolean validateEntries() {
		Validator checker = new Validator();
		checker.add(command, Validator.VALIDATE_NOT_BLANK, "Command");
		checker.add(direction, Validator.VALIDATE_NOT_BLANK, "Direction");
		
		String result = checker.validate();
		if(result != null) {
			checker.showMessage(SpeedWalkDirectionEditorDialog.this.getContext(), result);
			return false;
		}
		
		if(direction.getText().toString().length() > 1) {
			checker.showMessage(SpeedWalkDirectionEditorDialog.this.getContext(), "Direction field can not be more than 1 character.");
			return false;
		}
		
		try {
			int val = Integer.parseInt(direction.getText().toString());
			checker.showMessage(SpeedWalkDirectionEditorDialog.this.getContext(), "Direction field can not be a number. " + val + " is invalid.");
			return false;
		} catch(NumberFormatException e) {
			
		}
		
		return true;
	}

}
