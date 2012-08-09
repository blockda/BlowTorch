package com.offsetnull.bt.responder.color;

import com.offsetnull.bt.R;
import com.offsetnull.bt.responder.TriggerResponder;
import com.offsetnull.bt.responder.TriggerResponderEditorDoneListener;
import com.offsetnull.bt.service.Colorizer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.view.View;

public class ColorActionEditor extends Dialog {

	RadioGroup swatch;
	RadioButton foregroundRadio;
	Button red_up;
	Button red_down;
	Button green_up;
	Button green_down;
	Button blue_up;
	Button blue_down;
	Button grey_up;
	Button grey_down;
	Button sys_up;
	Button sys_down;
	CheckBox bright;
	
	TextView red_val;
	TextView green_val;
	TextView blue_val;
	TextView sys_val;
	TextView grey_val;
	
	int greyInt = 7;
	int redInt = 2;
	int greenInt = 2;
	int blueInt = 2;
	int sysInt = 7;
	
	int finalColor;
	int finalBackgroundColor;
	
	int idRedUp;
	
	TabHost host;
	
	TriggerResponder original;
	private TriggerResponderEditorDoneListener finish_with;
	
	int startVal;
	
	private enum EDIT {
		FOREGROUND,
		BACKGROUND
	}
	
	private EDIT editMode = EDIT.FOREGROUND;
	
	public ColorActionEditor(Context context,TriggerResponder original,TriggerResponderEditorDoneListener listener) {
		super(context);
		
		this.original = original;
		finish_with = listener;
		
		//if(original != null) {
		//	startVal
		//}
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		this.setContentView(R.layout.responder_color_dialog);
		
		host = (TabHost)this.findViewById(android.R.id.tabhost);
		
		host.setup();
		
		TabSpec xterm = host.newTabSpec("256");
		TabSpec bw = host.newTabSpec("BW");
		TabSpec sys = host.newTabSpec("Sys");
		
		xterm.setIndicator("256");
		bw.setIndicator("BW");
		sys.setIndicator("Sys");
		xterm.setContent(R.id.tab1);
		bw.setContent(R.id.tab2);
		sys.setContent(R.id.tab3);
		
		host.addTab(xterm);
		host.addTab(bw);
		host.addTab(sys);
		
		host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				if(tabId.equals("256")) {
					updateRedValue();
				} else if (tabId.equals("BW")) {
					updateGreyValue();
				} else if (tabId.equals("Sys")) {
					updateSysValue();
				}
						
			}
		});
		
		
		
		
		swatch = (RadioGroup)this.findViewById(R.id.swatch);
		foregroundRadio = (RadioButton)this.findViewById(R.id.foreground);
		
		swatch.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.foreground) {
					foregroundRadio.setText("Foreground");
					editMode = EDIT.FOREGROUND;
					updateColorTab();
				} else if(checkedId == R.id.background) {
					foregroundRadio.setText("Background");
					editMode = EDIT.BACKGROUND;
					updateColorTab();
				}
			}
		});
		
		//swatch.set
		//swatch.setText("");
		red_up = (Button)this.findViewById(R.id.red_up);
		red_down = (Button)this.findViewById(R.id.red_down);
		green_up = (Button)this.findViewById(R.id.green_up);
		green_down = (Button)this.findViewById(R.id.green_down);
		blue_up = (Button)this.findViewById(R.id.blue_up);
		blue_down = (Button)this.findViewById(R.id.blue_down);
		grey_up = (Button)this.findViewById(R.id.grey_up);
		grey_down = (Button)this.findViewById(R.id.grey_down);
		sys_up = (Button)this.findViewById(R.id.sys_up);
		sys_down = (Button)this.findViewById(R.id.sys_down);
		bright = (CheckBox)this.findViewById(R.id.bright);
		bright.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				updateSysValue();
			}
			
		});
		
		red_val = (TextView)this.findViewById(R.id.red_val);
		green_val = (TextView)this.findViewById(R.id.green_val);
		blue_val = (TextView)this.findViewById(R.id.blue_val);
		sys_val = (TextView)this.findViewById(R.id.sys_val);
		grey_val = (TextView)this.findViewById(R.id.grey_value);
		
		red_val.setText(Integer.toString(redInt));
		green_val.setText(Integer.toString(greenInt));
		blue_val.setText(Integer.toString(blueInt));
		grey_val.setText(Integer.toString(greyInt));
		sys_val.setText(Integer.toString(sysInt));
		
		red_up.setTag(new Integer(0));
		green_up.setTag(new Integer(1));
		blue_up.setTag(new Integer(2));
		grey_up.setTag(new Integer(3));
		sys_up.setTag(new Integer(4));
		
		red_down.setTag(new Integer(0));
		green_down.setTag(new Integer(1));
		blue_down.setTag(new Integer(2));
		grey_down.setTag(new Integer(3));
		sys_down.setTag(new Integer(4));
		
		red_up.setOnClickListener(mUpListener);
		green_up.setOnClickListener(mUpListener);
		blue_up.setOnClickListener(mUpListener);
		grey_up.setOnClickListener(mUpListener);
		sys_up.setOnClickListener(mUpListener);
		
		red_down.setOnClickListener(mDownListener);
		green_down.setOnClickListener(mDownListener);
		blue_down.setOnClickListener(mDownListener);
		grey_down.setOnClickListener(mDownListener);
		sys_down.setOnClickListener(mDownListener);
		
		idRedUp = R.id.red_up;
		
		findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doExit();
			}
		});
		
		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ColorActionEditor.this.dismiss();
			}
		});
		
		if(original != null) {
			finalColor = ((ColorAction)original).getColor();
			finalBackgroundColor = ((ColorAction)original).getBackgroundColor();
			updateColorTab();
		} else {
			host.setCurrentTab(0);
			updateRedValue();
			updateGreenValue();
			updateBlueValue();
		}
		
	}
	
	private void updateColorTab() {
		if(editMode == EDIT.FOREGROUND) {
			startVal = finalColor;
		} else {
			startVal = finalBackgroundColor;
		}
		
		if(startVal >0 && startVal <= 15) {
			//system color
			if(startVal > 7) {
				startVal = startVal - 8;
				bright.setChecked(true);
			}
			sysInt = startVal;
			host.setCurrentTab(2);
			updateSysValue();
		} else if (startVal >= 16 && startVal <= 231) {
			//256 color
			int val = startVal -16;
			redInt = val / 36;
			greenInt = (val % 36) / 6;
			blueInt = (val % 36) % 6;
			
			host.setCurrentTab(0);
			updateRedValue();
			updateBlueValue();
			updateGreenValue();
		} else if (startVal >= 232 && startVal <= 255) {
			greyInt = startVal - 232;
			host.setCurrentTab(1);
			updateGreyValue();
		}
	}
	
	protected void doExit() {
		if(original != null) {
			ColorAction action = new ColorAction();
			action.setColor(finalColor);
			action.setBackgroundColor(finalBackgroundColor);
			finish_with.editTriggerResponder(action, original);
		} else {
			ColorAction action = new ColorAction();
			action.setColor(finalColor);
			action.setBackgroundColor(finalBackgroundColor);
			finish_with.newTriggerResponder(action);
		}
		this.dismiss();
	}

	private View.OnClickListener mUpListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch((Integer)v.getTag()) {
			case 0:
				redInt++;
				if(redInt > 5) redInt = 5;
				updateRedValue();
				break;
			case 1:
				greenInt++;
				if(greenInt > 5) greenInt = 5;
				updateGreenValue();
				break;
			case 2:
				blueInt++;
				if(blueInt > 5) blueInt = 5;
				updateBlueValue();
				break;
			case 3:
				greyInt++;
				if(greyInt > 23) greyInt = 23;
				updateGreyValue();
				break;
			case 4:
				sysInt++;
				if(sysInt > 7) sysInt = 7;
				updateSysValue();
				break;
			}
		}

		
		
	};
	
	private View.OnClickListener mDownListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch((Integer)v.getTag()) {
			case 0:
				redInt--;
				if(redInt < 0) redInt = 0;
				updateRedValue();
				break;
			case 1:
				greenInt--;
				if(greenInt < 0) greenInt = 0;
				updateGreenValue();
				break;
			case 2:
				blueInt--;
				if(blueInt < 0) blueInt = 0;
				updateBlueValue();
				break;
			case 3:
				greyInt--;
				if(greyInt < 0) greyInt = 0;
				updateGreyValue();
				break;
			case 4:
				sysInt--;
				if(sysInt < 0) sysInt = 0;
				updateSysValue();
				break;
			}
		}
		
	};
	
	private void updateRGB() {
		int val = 16 + (redInt * 36) + (greenInt * 6) + blueInt;
		switch(editMode) {
			case FOREGROUND:
				finalColor = val;
				break;
			case BACKGROUND:
				finalBackgroundColor = val;
				break;
		}
		
		swatch.setBackgroundColor(Colorizer.get256ColorValue(finalBackgroundColor));
		foregroundRadio.setTextColor(Colorizer.get256ColorValue(finalColor));
	}
	
	private void updateRedValue() {
		red_val.setText(Integer.toString(redInt));
		
		updateRGB();
	}
	
	private void updateGreenValue() {
		green_val.setText(Integer.toString(greenInt));
		updateRGB();
	}
	
	private void updateBlueValue() {
		blue_val.setText(Integer.toString(blueInt));
		updateRGB();
	}
	
	private void updateGreyValue() {
		grey_val.setText(Integer.toString(greyInt));
		int val = 232 + greyInt;
		
		switch(editMode) {
		case FOREGROUND:
			finalColor = val;
			break;
		case BACKGROUND:
			finalBackgroundColor = val;
			break;
		}
	
		swatch.setBackgroundColor(Colorizer.get256ColorValue(finalBackgroundColor));
		foregroundRadio.setTextColor(Colorizer.get256ColorValue(finalColor));
	}
	
	private void updateSysValue() {
		sys_val.setText(Integer.toString(sysInt));
		
		int val = sysInt;
		if(bright.isChecked()) {
			val = val + 8;
		}
		
		switch(editMode) {
		case FOREGROUND:
			finalColor = val;
			break;
		case BACKGROUND:
			finalBackgroundColor = val;
			break;
		}
	
		swatch.setBackgroundColor(Colorizer.get256ColorValue(finalBackgroundColor));
		foregroundRadio.setTextColor(Colorizer.get256ColorValue(finalColor));
	}

}
