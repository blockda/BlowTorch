package com.happygoatstudios.bt.button;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.button.ButtonEditorDialog.COLOR_FIELDS;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.validator.Validator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
//import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

public class ButtonSetEditor extends Dialog implements ColorPickerDialog.OnColorChangedListener {

	Button normalColor = null;
	Button focusColor = null;
	Button flipColor = null;
	Button labelColor = null;
	Button flipLabelColor = null;
	
	EditText labelSize;
	EditText buttonWidth;
	EditText buttonHeight;
	
	ColorSetSettings newsettings;
	ColorSetSettings oldsettings;
	
	Handler notifychanged = null;
	
	IStellarService service;
	String set;
	public ButtonSetEditor(Context context,IStellarService the_service,String selected_set,Handler use_this_handler) {
		super(context);
		// TODO Auto-generated constructor stub
		service = the_service;
		set = selected_set;
		notifychanged = use_this_handler;
	}
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.buttonset_settings_editor_dialog);
		
		ScrollView sv = (ScrollView)findViewById(R.id.btn_set_editor_scroll);
		sv.setScrollbarFadingEnabled(false);
		//attempt to fetch the settings.
		//ColorSetSettings the_settings =  null;
		try {
			newsettings = service.getColorSetDefaultsForSet(set);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		oldsettings = newsettings.copy();
		
		normalColor = (Button)findViewById(R.id.btnset_defaultcolor);
		focusColor =(Button)findViewById(R.id.btnset_focuscolor);
		flipColor = (Button)findViewById(R.id.btnset_flippedcolor);
		labelColor = (Button)findViewById(R.id.btnset_labelcolor);
		flipLabelColor = (Button)findViewById(R.id.btnset_fliplabelcolor);
		//normalColor = (Button)findViewById(R.id.btn_defaultcolor);
		normalColor.setBackgroundColor(oldsettings.getPrimaryColor());
		focusColor.setBackgroundColor(oldsettings.getSelectedColor());
		labelColor.setBackgroundColor(oldsettings.getLabelColor());
		flipColor.setBackgroundColor(oldsettings.getFlipColor());
		flipLabelColor.setBackgroundColor(oldsettings.getFlipLabelColor());
		
		normalColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getPrimaryColor(),COLOR_FIELDS.COLOR_MAIN);
				diag.show();
			}
		});
		
		focusColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getSelectedColor(),COLOR_FIELDS.COLOR_SELECTED);
				diag.show();
			}
		});
		
		labelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getLabelColor(),COLOR_FIELDS.COLOR_LABEL);
				diag.show();
			}
		});
		
		flipColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getFlipColor(),COLOR_FIELDS.COLOR_FLIPPED);
				diag.show();
			}
		});
		
		flipLabelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getFlipLabelColor(),COLOR_FIELDS.COLOR_FLIPLABEL);
				diag.show();
			}
		});
		
		
		labelSize = (EditText)findViewById(R.id.btnset_editor_lblsize_et);
		buttonHeight = (EditText)findViewById(R.id.btnset_editor_height_et);
		buttonWidth = (EditText)findViewById(R.id.btnset_editor_width_et);
		
		labelSize.setText(new Integer(oldsettings.getLabelSize()).toString());
		buttonHeight.setText(new Integer(oldsettings.getButtonHeight()).toString());
		buttonWidth.setText(new Integer(oldsettings.getButtonWidth()).toString());
		
		Button done = (Button)findViewById(R.id.btnset_done_btn);
		Button cancel = (Button)findViewById(R.id.btnset_cancel_btn);
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ButtonSetEditor.this.dismiss();
			}
		});
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//ColorSetSettings newset = new ColorSetSettings();
				//newset.setPrimaryColor(normalColor.getB)
				Validator checker = new Validator();
				checker.add(buttonHeight, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Button Height");
				checker.add(buttonWidth, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Button Width");
				checker.add(labelSize, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Label Size");
				
				String result = checker.validate();
				if(result != null) {
					checker.showMessage(ButtonSetEditor.this.getContext(), result);
					return;
				}
				
				newsettings.setButtonHeight(Integer.parseInt(buttonHeight.getText().toString()));
				newsettings.setButtonWidth(Integer.parseInt(buttonWidth.getText().toString()));
				newsettings.setLabelSize(Integer.parseInt(labelSize.getText().toString()));
				
				if(newsettings.equals(oldsettings)) {
					//no changes made, do nothing
					//Log.e("SETEDITOR","NOT MODIFYING SET DEFAULTS FOR:" +set);
				} else {
					//changes made, notify the service that the change has been made, and notify the settings dialog that when it exits it needs to reload whatever button set changed.
					try {
						service.setColorSetDefaultsForSet(set, newsettings);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//Log.e("SETEDITOR","NOT MODIFYING SET DEFAULTS FOR:" +set);
					notifychanged.sendEmptyMessage(100);
					
					
				}
				ButtonSetEditor.this.dismiss();
			}
		});
		
	}

	public void colorChanged(int color, COLOR_FIELDS which) {
		// TODO Auto-generated method stub
		
		switch(which) {
		case COLOR_MAIN:
			newsettings.setPrimaryColor(color);
			normalColor.setBackgroundColor(color);
			break;
		case COLOR_SELECTED:
			newsettings.setSelectedColor(color);
			focusColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPPED:
			flipColor.setBackgroundColor(color);
			newsettings.setFlipColor(color);
			break;
		case COLOR_LABEL:
			newsettings.setLabelColor(color);
			labelColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPLABEL:
			newsettings.setFlipLabelColor(color);
			flipLabelColor.setBackgroundColor(color);
			break;
		default:
			break;
		}
	}

}
