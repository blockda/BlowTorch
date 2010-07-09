package com.happygoatstudios.bt.button;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.window.SlickView;

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

public class ButtonEditorDialog extends Dialog implements ColorPickerDialog.OnColorChangedListener {
	
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
	
	Button normalColor = null;
	Button focusColor = null;
	Button flipColor = null;
	Button labelColor = null;
	Button flipLabelColor = null;
	
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
		
		EditText fliplabel = (EditText)findViewById(R.id.button_flip_label_et);
		fliplabel.setText(the_button.getData().getFlipLabel());
		
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
		//Button normalColor = null;
		//Button focusColor = null;
		//Button flipColor = null;
		//Button labelColor = null;
		//Button flipLabelColor = null;
		
		
		normalColor = (Button)findViewById(R.id.btn_defaultcolor);
		focusColor =(Button)findViewById(R.id.btn_focuscolor);
		flipColor = (Button)findViewById(R.id.btn_flippedcolor);
		labelColor = (Button)findViewById(R.id.btn_labelcolor);
		flipLabelColor = (Button)findViewById(R.id.btn_fliplabelcolor);
		//normalColor = (Button)findViewById(R.id.btn_defaultcolor);
		normalColor.setBackgroundColor(the_button.getData().getPrimaryColor());
		focusColor.setBackgroundColor(the_button.getData().getSelectedColor());
		labelColor.setBackgroundColor(the_button.getData().getLabelColor());
		flipColor.setBackgroundColor(the_button.getData().getFlipColor());
		flipLabelColor.setBackgroundColor(the_button.getData().getFlipLabelColor());
		
		
		normalColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getPrimaryColor(),COLOR_FIELDS.COLOR_MAIN);
				diag.show();
			}
		});
		
		focusColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getSelectedColor(),COLOR_FIELDS.COLOR_SELECTED);
				diag.show();
			}
		});
		
		labelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getLabelColor(),COLOR_FIELDS.COLOR_LABEL);
				diag.show();
			}
		});
		
		flipColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getFlipColor(),COLOR_FIELDS.COLOR_FLIPPED);
				diag.show();
			}
		});
		
		flipLabelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getFlipLabelColor(),COLOR_FIELDS.COLOR_FLIPLABEL);
				diag.show();
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
				
				//ColorPickerDialog cpd = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,0xFF00FF00,COLOR_FIELDS.COLOR_MAIN);
				//cpd.show();
				
				EditText label = (EditText)findViewById(R.id.button_text_et);
				
				
				EditText command = (EditText)findViewById(R.id.button_command_et);
				EditText flip = (EditText)findViewById(R.id.button_flip_et);
				EditText fliplbl = (EditText)findViewById(R.id.button_flip_label_et);
				the_button.setLabel(label.getText().toString());
				the_button.setText(command.getText().toString());
				the_button.setFlipCommand(flip.getText().toString());
				the_button.getData().setFlipLabel(fliplbl.getText().toString());
				
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
	
	public enum COLOR_FIELDS {
		COLOR_MAIN,
		COLOR_SELECTED,
		COLOR_FLIPPED,
		COLOR_LABEL,
		COLOR_FLIPLABEL
	}

	//Button normalColor = null;
	//Button focusColor = null;
	//Button flipColor = null;
	//Button labelColor = null;
	//Button flipLabelColor = null;
	public void colorChanged(int color,COLOR_FIELDS which) {
		// TODO Auto-generated method stub
		Log.e("BTNEDITOR","GOT NEW COLOR FOR" + which + " returned " + color);
		switch(which) {
		case COLOR_MAIN:
			the_button.getData().setPrimaryColor(color);
			normalColor.setBackgroundColor(color);
			break;
		case COLOR_SELECTED:
			the_button.getData().setSelectedColor(color);
			focusColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPPED:
			flipColor.setBackgroundColor(color);
			the_button.getData().setFlipColor(color);
			break;
		case COLOR_LABEL:
			the_button.getData().setLabelColor(color);
			labelColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPLABEL:
			the_button.getData().setFlipLabelColor(color);
			flipLabelColor.setBackgroundColor(color);
			break;
		default:
			break;
		}
		
	}

}
