package com.happygoatstudios.bt.settings;


import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.validator.Validator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CheckedEditPreference extends Preference  {

	View keeper = null;
	//@Override
	protected View onCreateView(ViewGroup parent){
		View v = super.onCreateView(parent);
		
		CheckBox cbox = (CheckBox)v.findViewById(R.id.checkbox);
		EditText input = (EditText)v.findViewById(R.id.text);
		TextView label = (TextView)v.findViewById(R.id.extra);
		
		input.setEnabled(false);
		label.setEnabled(false);
		cbox.setChecked(true);
		
		TextView display = (TextView)v.findViewById(android.R.id.summary);
		
		
		
		keeper = v;
		return v;
		
		//LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//return li.inflate(R.layout.tristate_widget, null);
		//return li.inflate(android.R.layout.preference_category, null);
		//android.R.layout.
	}
	
	public void onBindView(View in) {
		super.onBindView(in);
		View v = keeper;
		
		CheckBox cbox = (CheckBox)v.findViewById(R.id.checkbox);
		EditText input = (EditText)v.findViewById(R.id.text);
		TextView label = (TextView)v.findViewById(R.id.extra);
		
		input.setEnabled(false);
		label.setEnabled(false);
		cbox.setChecked(true);
		
		TextView display = (TextView)v.findViewById(android.R.id.summary);
		
		
		input.setOnKeyListener(new TextWatcher(input));
		
		SharedPreferences prefs = getSharedPreferences();
		int val = prefs.getInt(getKey(), 0);
		//Log.e("LKDSFS","LOADED:" + Integer.toString(val));
		int calc = prefs.getInt("CALCULATED_WIDTH", 80);
		if(val > 0) {
			input.setEnabled(true);
			label.setEnabled(true);
			cbox.setChecked(false);
			input.setText(Integer.toString(val));
		} else {
			input.setEnabled(false);
			label.setEnabled(false);
			cbox.setChecked(true);
			input.setText(Integer.toString(calc));
		}
		
		input.setOnKeyListener(new TextWatcher(input));
		
		//cbox.setOnCheckedChangeListener(new StateChanger(input,label,display));
		cbox.setOnCheckedChangeListener(new StateChanger(input,label,display));
	}
	
	public CheckedEditPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public CheckedEditPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public CheckedEditPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private class TextWatcher implements View.OnKeyListener {

		Validator checker = new Validator();
		EditText towatch = null;
		
		public TextWatcher(EditText pIn) {
			towatch = pIn;
			//checker.add(towatch, Validator.VALIDATE_NOT_BLANK,"Characters");
			checker.add(towatch, Validator.VALIDATE_NUMBER, "Characters");
			checker.add(towatch, Validator.VALIDATE_NUMBER_NOT_ZERO, "Characters");
		}
		
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			
			if(event.getAction() == KeyEvent.ACTION_UP) {
				//validate and save.
				String ret = checker.validate();
				if(ret != null) {
					checker.showMessage(CheckedEditPreference.this.getContext(), ret);
					towatch.setText("1");
					towatch.setSelection(1);
				}
				
				if(!(towatch.getText().toString().equals(""))) {
					SharedPreferences.Editor edit = CheckedEditPreference.this.getEditor();
					edit.putInt(CheckedEditPreference.this.getKey(), Integer.parseInt(towatch.getText().toString()));
					edit.commit();
					//Log.e("sfdFD","SAVING:" +towatch.getText().toString() + " with key: " + CheckedEditPreference.this.getKey());
				}
			}
			
			return false;
		}
		
	}
	
	
	
	private class StateChanger implements CompoundButton.OnCheckedChangeListener {

		//private STATE myState = STATE.AUTO;
		
		private EditText toggle1 = null;
		private TextView toggle2 = null;
		
		TextView display = null;
		
		public StateChanger(EditText in1, TextView in2,TextView pIn) {
			//myState = type;
			toggle1 = in1;
			toggle2 = in2;
			
			display = pIn;
		}

		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
			if(isChecked) {
				toggle1.setEnabled(false);
				toggle2.setEnabled(false);
				
				SharedPreferences prefs = CheckedEditPreference.this.getSharedPreferences();
				int calc = prefs.getInt("CALCULATED_WIDTH", 80);
				toggle1.setText(Integer.toString(calc));
				SharedPreferences.Editor edit = getEditor();
				edit.putInt(CheckedEditPreference.this.getKey(), 0);
				edit.commit();
			} else {
				toggle1.setEnabled(true);
				toggle2.setEnabled(true);
				SharedPreferences.Editor edit = getEditor();
				edit.putInt(CheckedEditPreference.this.getKey(),Integer.parseInt(toggle1.getText().toString()));
				edit.commit();
			}
		}
		
		
		
	}
	
	
}
