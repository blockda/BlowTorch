package com.happygoatstudios.bt.button;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.window.SlickView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

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
	
	EditText labelSize;
	EditText xPos;
	EditText yPos;
	EditText eWidth;
	EditText eHeight;
	
	//SlickButtonData orig_data = null;
	
	public ButtonEditorDialog(Context context,SlickButton useme,Handler callback) {
		super(context);
		
		//mod_cmd = cmd;
		//mod_lbl = lbl;
		
		the_button = useme;
		deleter = callback;
		//orig_data = useme.getData().copy();
	}
	
	public ButtonEditorDialog(Context context,int themeid,SlickButton useme,Handler callback) {
		super(context,themeid);
		
		//mod_cmd = cmd;
		//mod_lbl = lbl;
		
		the_button = useme;
		deleter = callback;
		//orig_data = useme.getData().copy();
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//this.setTitle("Modify Button Properties...");
		setContentView(R.layout.button_properties_dialog_tabbed);
		
		TabHost thost = (TabHost)findViewById(R.id.btn_editor_tabhost);
		
		thost.setup();
		
		TabSpec tab1 = (TabSpec) thost.newTabSpec("tab_one_btn_tab");
		TextView lbl1 = new TextView(this.getContext());
		lbl1.setText("Click");
		lbl1.setGravity(Gravity.CENTER);
		lbl1.setBackgroundResource(R.drawable.tab_background);
		
		//lbl1.setHeight(20);
		tab1.setIndicator(lbl1);
		tab1.setContent(R.id.btn_editor_tab1);
		thost.addTab(tab1);
		
		TabSpec tab2 = (TabSpec) thost.newTabSpec("tab_two_btn_tab");
		TextView lbl2 = new TextView(this.getContext());
		lbl2.setText("Flip");
		lbl2.setGravity(Gravity.CENTER);
		lbl2.setBackgroundResource(R.drawable.tab_background);
		tab2.setIndicator(lbl2);
		tab2.setContent(R.id.btn_editor_tab2);
		thost.addTab(tab2);
		
		TabSpec tab3 = (TabSpec) thost.newTabSpec("tab_three_btn_tab");
		TextView lbl3 = new TextView(this.getContext());
		lbl3.setText("Advanced");
		lbl3.setGravity(Gravity.CENTER);
		lbl3.setBackgroundResource(R.drawable.tab_background);
		tab3.setIndicator(lbl3);
		tab3.setContent(R.id.btn_editor_tab3);
		thost.addTab(tab3);
		
		thost.setCurrentTab(1);
		
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
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
		
		labelSize = (EditText)findViewById(R.id.btn_editor_lblsize_et);
		xPos = (EditText)findViewById(R.id.btn_editor_xcoord_et);
		yPos = (EditText)findViewById(R.id.btn_editor_ycoord_et);
		eWidth = (EditText)findViewById(R.id.btn_editor_width_et);
		eHeight = (EditText)findViewById(R.id.btn_editor_height_et);
		
		labelSize.setText(new Integer(this.the_button.getData().getLabelSize()).toString());
		xPos.setText(new Integer(the_button.getData().getX()).toString());
		yPos.setText(new Integer(the_button.getData().getY()).toString());
		eWidth.setText(new Integer(the_button.getData().getWidth()).toString());
		eHeight.setText(new Integer(the_button.getData().getHeight()).toString());
		
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
				the_button.getData().setLabelSize(new Integer(labelSize.getText().toString()));
				the_button.getData().setX(new Integer(xPos.getText().toString()));
				the_button.getData().setY(new Integer(yPos.getText().toString()));
				the_button.getData().setWidth(new Integer(eWidth.getText().toString()));
				the_button.getData().setHeight(new Integer(eHeight.getText().toString()));
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
	
	public void onBackPressed() {
		//the_button.iHaveChanged(the_button.orig_data);
		the_button.moving = false;
		the_button.button_down = false;
		the_button.doing_flip = false;
		the_button.hasfocus = false;
		the_button.dialog_launched = false;
		//the_button.
		the_button.invalidate();
		this.dismiss();
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
