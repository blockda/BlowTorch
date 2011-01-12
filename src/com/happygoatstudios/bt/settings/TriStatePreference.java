package com.happygoatstudios.bt.settings;


import com.happygoatstudios.bt.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TriStatePreference extends Preference  {

	View keeper = null;
	//@Override
	protected View onCreateView(ViewGroup parent){
		View v = super.onCreateView(parent);
		
		Button box1 = (Button)v.findViewById(R.id.auto);
		Button box2 = (Button)v.findViewById(R.id.landscape);
		Button box3 = (Button)v.findViewById(R.id.portriat);
		
		TextView t = (TextView) v.findViewById(android.R.id.summary);
		//t.setText("FOOMFOD");
		
		
		box1.setOnClickListener(new StateChanger(STATE.AUTO,box2,box3,t));
		box2.setOnClickListener(new StateChanger(STATE.LANDSCAPE,box1,box3,t));
		box3.setOnClickListener(new StateChanger(STATE.PORTRAIT,box1,box2,t));
		//myHandler.sendEmptyMessageDelayed(0,10);
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
		
		Button box1 = (Button)v.findViewById(R.id.auto);
		Button box2 = (Button)v.findViewById(R.id.landscape);
		Button box3 = (Button)v.findViewById(R.id.portriat);
		
		TextView t = (TextView) v.findViewById(android.R.id.summary);
		//t.setText("FOOMFOD");
		
		SharedPreferences pref = getSharedPreferences();
		int mode = pref.getInt(getKey(), STATE.AUTO.intVal());
		//STATE readState = STATE.AUTO;
		switch(mode) {
		case 0:
			box1.setEnabled(false);
			t.setText("Using automatic");
			break;
		case 1:
			box2.setEnabled(false);
			t.setText("Using landscape");
			break;
		case 2:
			box3.setEnabled(false);
			t.setText("Using portrait");
			break;
		default:
			break;
		}
	}
	
	public TriStatePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public TriStatePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public TriStatePreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	
	public enum STATE {
		AUTO (0),
		LANDSCAPE (1),
		PORTRAIT (2);
		
		private final int intVal;
		STATE(int i) {
			intVal = i;
		}
		
		public int intVal() {
			return intVal;
		}
	}
	
	private class StateChanger implements View.OnClickListener {

		private STATE myState = STATE.AUTO;
		
		private Button toggle1 = null;
		private Button toggle2 = null;
		
		TextView display = null;
		
		public StateChanger(STATE type,Button in1, Button in2,TextView pIn) {
			myState = type;
			toggle1 = in1;
			toggle2 = in2;
			
			display = pIn;
		}
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			SharedPreferences.Editor editor = getEditor();
			
			switch(myState) {
			case AUTO:
				display.setText("Using automatic");
				editor.putInt(getKey(), STATE.AUTO.intVal());
				break;
			case LANDSCAPE:
				display.setText("Using landscape");
				editor.putInt(getKey(), STATE.LANDSCAPE.intVal());
				break;
			case PORTRAIT:
				display.setText("Using portrait");
				editor.putInt(getKey(), STATE.PORTRAIT.intVal());
				break;
			default:
				break;
			}
			
			editor.commit();
			
			toggle1.setEnabled(true);
			toggle2.setEnabled(true);
			v.setEnabled(false);
		}
		
	}
	
	
}
