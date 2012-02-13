package com.happygoatstudios.bt.timer;

import java.util.HashMap;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponderEditorDoneListener;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.TriggerResponder.RESPONDER_TYPE;
import com.happygoatstudios.bt.responder.ack.AckResponder;
import com.happygoatstudios.bt.responder.ack.AckResponderEditor;
import com.happygoatstudios.bt.responder.notification.NotificationResponder;
import com.happygoatstudios.bt.responder.notification.NotificationResponderEditor;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.responder.toast.ToastResponderEditor;
import com.happygoatstudios.bt.service.IConnectionBinder;
import com.happygoatstudios.bt.validator.Validator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class TimerEditorDialog extends Dialog implements DialogInterface.OnClickListener,TriggerResponderEditorDoneListener {

	private TableRow legend;
	private TableLayout responderTable;
	
	private TimerData the_timer;
	private TimerData orig_timer;
	
	private IConnectionBinder service;
	
	private Handler finish_with;
	
	HashMap<Integer,Integer> checkopens;
	HashMap<Integer,Integer> checkclosed;
	
	private CheckBox repeat;
	private EditText name;
	private EditText seconds;
	
	private boolean isEditor = false;
	
	String plugin = "main";
	
	public TimerEditorDialog(Context c,String plugin,TimerData input,IConnectionBinder pService,Handler reportto) {
		super(c);
		service = pService;
		finish_with = reportto;
		
		if(input == null) {
			the_timer = new TimerData();
		} else {
			the_timer = input.copy();
			orig_timer = input.copy();
			isEditor = true;
		}
		
		this.plugin = plugin;
		
		checkopens = new HashMap<Integer,Integer>();
		checkclosed = new HashMap<Integer,Integer>();
		
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.timer_editor_dialog);
		
		name = (EditText)findViewById(R.id.timer_editor_name);
		seconds = (EditText)findViewById(R.id.timer_editor_seconds);
		
		repeat = (CheckBox)findViewById(R.id.timer_repeat_checkbox);
		

		legend= (TableRow)findViewById(R.id.timer_notification_legend);
		responderTable = (TableLayout)findViewById(R.id.timer_notification_table);
		
		Button newresponder = (Button)findViewById(R.id.timer_new_notification);
		newresponder.setOnClickListener(new NewResponderListener());
		
		
		refreshResponderTable();
		
		//hook up additional buttons.
		Button cancelbutton = (Button)findViewById(R.id.timer_editor_cancel);
		cancelbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				//TODO: Check if destroyed here.
				TimerEditorDialog.this.dismiss();
			}
		});
		
		Button donebutton = (Button)findViewById(R.id.timer_editor_done_button);
		donebutton.setOnClickListener(new TimerEditerDoneListener());
		
		
		if(isEditor) {
			name.setText(orig_timer.getName());
			seconds.setText(orig_timer.getSeconds().toString());
			repeat.setChecked(orig_timer.isRepeat());
			donebutton.setText("Done");
			
		}
	}
	
	private class TimerEditerDoneListener implements View.OnClickListener {

		public void onClick(View v) {
			//here we validate and invoke the timer saving.
			
			
			Validator checker = new Validator();
			checker.add(name, Validator.VALIDATE_NOT_BLANK, "Timer Name");
			checker.add(seconds, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Timer duration");
			
			String result = checker.validate();
			if(result != null) {
				checker.showMessage(TimerEditorDialog.this.getContext(), result);
				return;
			}
			
			String theName = name.getText().toString();
			String theSeconds = seconds.getText().toString();
			boolean theRepeat = repeat.isChecked();
				
			//now we are validated. proceed with save.
			if(isEditor) {
				the_timer.setName(theName);
				the_timer.setSeconds(Integer.parseInt(theSeconds));
				the_timer.setRepeat(theRepeat);
				
				//responders should be handled already.
				try {
					if(plugin.equals("main")) {
						service.updateTimer(orig_timer, the_timer);
					} else {
						service.updatePluginTimer(plugin, orig_timer, the_timer);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				finish_with.sendMessage(finish_with.obtainMessage(99, the_timer.getOrdinal().toString()));
			} else {
				the_timer.setName(theName);
				the_timer.setSeconds(Integer.parseInt(theSeconds));
				the_timer.setRepeat(theRepeat);
				
				try {
					//the_timer.setOrdinal(service.getNextTimerOrdinal());
					if(plugin.equals("main")) {
						service.addTimer(the_timer);
					} else {
						service.addPluginTimer(plugin,the_timer);
					}
					
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				finish_with.sendMessage(finish_with.obtainMessage(100,the_timer.getOrdinal().toString()));
			}
			
			
			TimerEditorDialog.this.dismiss();
		}
		
	};
	
	private void refreshResponderTable() {
		
		legend.setVisibility(View.GONE);
		
		TableRow newbutton = (TableRow)findViewById(R.id.timer_new_responder_row);
		//responderTable.removeView(newbutton);
		responderTable.removeViews(1, responderTable.getChildCount()-1);
		
		//RelativeLayout p = (RelativeLayout)findViewById(R.id.newtriggerlayout);
		LayoutParams params = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		int margin =  (int) (0*this.getContext().getResources().getDisplayMetrics().density);
		params.rightMargin = margin;
		params.leftMargin =  margin;
		params.topMargin =  margin;
		params.bottomMargin = margin;
		
		int checkboxnumber = 123456; //generate semi unique id's each time we generate this table. we need to do this because the check changed listeners are freaking. out.
		
		checkopens.clear();
		checkclosed.clear();
		int count = 0;
		//boolean legendAdded = false;
		for(TriggerResponder responder : the_timer.getResponders()) {
			//if(!legendAdded) {
			//	responderTable.addView(legend);
			//	legendAdded = true;
			//}
			TableRow row = new TableRow(this.getContext());
			
			TextView label = new TextView(this.getContext());
			label.setOnClickListener(new EditResponderListener(the_timer.getResponders().indexOf(responder)));
			if(responder.getType() == RESPONDER_TYPE.NOTIFICATION) {
				label.setText("Notification: " + ((NotificationResponder)responder).getTitle());
			} else if(responder.getType() == RESPONDER_TYPE.TOAST) {
				label.setText("Toast Message: " + ((ToastResponder)responder).getMessage());
			} else if(responder.getType() == RESPONDER_TYPE.ACK){
				label.setText("Ack With: " + ((AckResponder)responder).getAckWith());
			}
			label.setGravity(Gravity.CENTER);
			label.setSingleLine(true);
			
			int labelwidth = 0;
			//Display display = ;
			switch(this.getContext().getResources().getConfiguration().orientation) {
			
			case Configuration.ORIENTATION_PORTRAIT:
				labelwidth = 70;
				break;
			case Configuration.ORIENTATION_LANDSCAPE:
			default:
				labelwidth = 130;
				break;
			}
			
			label.setWidth((int) (labelwidth * this.getContext().getResources().getDisplayMetrics().density));
			LinearLayout l1 = new LinearLayout(this.getContext());
			l1.setGravity(Gravity.CENTER);
			LinearLayout l2 = new LinearLayout(this.getContext());
			l2.setGravity(Gravity.CENTER);
			CheckBox windowOpen = new CheckBox(this.getContext()); windowOpen.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL); windowOpen.setId(checkboxnumber);
			checkopens.put(the_timer.getResponders().indexOf(responder), checkboxnumber);
			checkboxnumber++;
			
			CheckBox windowClose = new CheckBox(this.getContext()); windowClose.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL); windowClose.setId(checkboxnumber);
			checkclosed.put(the_timer.getResponders().indexOf(responder),checkboxnumber);
			checkboxnumber++;

			
			windowOpen.setOnCheckedChangeListener(new WindowOpenCheckChangeListener(the_timer.getResponders().indexOf(responder)));
			windowClose.setOnCheckedChangeListener(new WindowClosedCheckChangeListener(the_timer.getResponders().indexOf(responder)));
			
			Button delete = new Button(this.getContext()); delete.setBackgroundResource(android.R.drawable.ic_delete);
			delete.setOnClickListener(new DeleteResponderListener(the_timer.getResponders().indexOf(responder)));
			
			windowOpen.setGravity(Gravity.CENTER); windowOpen.setText("");
			windowClose.setGravity(Gravity.CENTER); windowClose.setText("");
			delete.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
			
			l1.addView(windowOpen);
			l2.addView(windowClose);
			//windowOpen.setLayoutParams(params); windowClose.setLayoutParams(params); delete.setLayoutParams(params);
			
			row.addView(label); row.addView(l1); row.addView(l2); row.addView(delete);
			responderTable.addView(row);
			
			
			if(responder.getFireType() == FIRE_WHEN.WINDOW_OPEN || responder.getFireType()==FIRE_WHEN.WINDOW_BOTH) {
				windowOpen.setChecked(true);
				//windowOpen.setText("OC");
			} else {
				windowOpen.setChecked(false);
				//windowOpen.setText("OU");
			}
			
			if(responder.getFireType() == FIRE_WHEN.WINDOW_CLOSED || responder.getFireType() == FIRE_WHEN.WINDOW_BOTH) {
				windowClose.setChecked(true);
				//windowClose.setText("CC");
			} else {
				windowClose.setChecked(false);
				//windowClose.setText("CU");
			}
			
			count++;
		}
		if(count > 0) {
			
			legend.setVisibility(View.VISIBLE);
		}
		
		responderTable.addView(newbutton);
	}
	
	private class DeleteResponderListener implements View.OnClickListener,DialogInterface.OnClickListener {

		int position;
		
		public DeleteResponderListener(int i) {
			position = i;
		}
		
		public void onClick(View arg0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(TimerEditorDialog.this.getContext());
			builder.setPositiveButton("Delete", this);
			builder.setNegativeButton("Cancel", this);
			builder.setTitle("Are you sure?");
			AlertDialog deleter = builder.create();
			deleter.show();
		}

		public void onClick(DialogInterface arg0, int arg1) {
			if(arg1 == DialogInterface.BUTTON_POSITIVE) {
				//really delete the button
				the_timer.getResponders().remove(position);
				refreshResponderTable();
			}
		}
		
	};
	
	private class NewResponderListener implements View.OnClickListener {

		public void onClick(View v) {
			//give out a list of options
			CharSequence[] items = {"Notification","Toast Message","Ack With"};
			AlertDialog.Builder builder = new AlertDialog.Builder(TimerEditorDialog.this.getContext());
			builder.setTitle("Type:");
			
			builder.setItems(items, TimerEditorDialog.this);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
	}
	
	public void onClick(DialogInterface arg0, int arg1) {
		arg0.dismiss();
		switch(arg1) {
		case 0: //notificaiton
			NotificationResponderEditor notifyEditor = new NotificationResponderEditor(this.getContext(),null,this);
			notifyEditor.show();
			break;
		case 1: //toast
			ToastResponderEditor tedit = new ToastResponderEditor(TimerEditorDialog.this.getContext(),null,TimerEditorDialog.this);
			tedit.show();
			break; 
		case 2:
			AckResponderEditor aedit = new AckResponderEditor(TimerEditorDialog.this.getContext(),null,TimerEditorDialog.this);
			aedit.show();
			break; //ack
		default:
			break;
		}
		
	}
	
	private class EditResponderListener implements View.OnClickListener {

		int position;
		
		public EditResponderListener(int pos) {
			position = pos;
		}
		
		public void onClick(View v) {
			TriggerResponder responder = the_timer.getResponders().get(position);
			switch(responder.getType()) {
			case NOTIFICATION:
				//show the notification editor
				NotificationResponderEditor redit = new NotificationResponderEditor(TimerEditorDialog.this.getContext(),(NotificationResponder)responder.copy(),TimerEditorDialog.this);
				redit.show();
				break;
			case TOAST:
				ToastResponderEditor tedit = new ToastResponderEditor(TimerEditorDialog.this.getContext(),(ToastResponder)responder.copy(),TimerEditorDialog.this);
				tedit.show();
				break;
			case ACK:
				AckResponderEditor aedit = new AckResponderEditor(TimerEditorDialog.this.getContext(),(AckResponder)responder.copy(),TimerEditorDialog.this);
				aedit.show();
				break;
			default:
				break;
			}
			
		}
		
	}

	public void editTriggerResponder(TriggerResponder edited,
			TriggerResponder original) {
		int pos = the_timer.getResponders().indexOf(original);
		the_timer.getResponders().remove(pos);
		the_timer.getResponders().add(pos,edited);
		refreshResponderTable();
	}

	public void newTriggerResponder(TriggerResponder newresponder) {
		the_timer.getResponders().add(newresponder);
		refreshResponderTable();
		
	}
	
	private class WindowOpenCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

		private final int position;
		
		WindowOpenCheckChangeListener(int i) {
			position = i;
		}
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			if(checked) {
				//check the closed check state.
				the_timer.getResponders().get(position).addFireType(FIRE_WHEN.WINDOW_OPEN);
				///Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " ADDING windowOpen");
			} else {
				the_timer.getResponders().get(position).removeFireType(FIRE_WHEN.WINDOW_OPEN);
				//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " REMOVING windowOpen");
			}
			//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType() + " AT "+ position + " NOW " + the_trigger.getResponders().get(position).getFireType().getString());
			
			
		}
		
	};
	
	private class WindowClosedCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

		private final int position;
		
		WindowClosedCheckChangeListener(int i) {
			position = i;
		}
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			if(checked) {
				//check the closed check state.
				the_timer.getResponders().get(position).addFireType(FIRE_WHEN.WINDOW_CLOSED);
				//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " ADDING windowClosed");
			} else {
				the_timer.getResponders().get(position).removeFireType(FIRE_WHEN.WINDOW_CLOSED);
				//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " REMOVING windowClosed");
				
			}
			//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " NOW " + the_trigger.getResponders().get(position).getFireType().getString());
			
		}
		
	};
	
}
