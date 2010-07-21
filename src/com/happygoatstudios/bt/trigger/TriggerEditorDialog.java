package com.happygoatstudios.bt.trigger;

import com.happygoatstudios.bt.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

import com.happygoatstudios.bt.responder.*;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.TriggerResponder.RESPONDER_TYPE;
import com.happygoatstudios.bt.responder.ack.*;
import com.happygoatstudios.bt.responder.notification.*;
import com.happygoatstudios.bt.responder.toast.*;
import com.happygoatstudios.bt.service.IStellarService;

public class TriggerEditorDialog extends Dialog implements DialogInterface.OnClickListener,NotificationResponderDoneListener{

	private TableRow legend;
	private TableLayout responderTable;
	
	private TriggerData the_trigger;
	private TriggerData original_trigger;
	private boolean isEditor = false;
	
	private IStellarService service;
	
	private Handler finish_with;
	
	public TriggerEditorDialog(Context context,TriggerData input,IStellarService pService,Handler finisher) {
		super(context);
		
		service = pService;
		finish_with = finisher;
		if(input == null) {
			the_trigger = new TriggerData();
			
		} else {
			the_trigger = input;
			original_trigger = input.copy();
			isEditor=true;
		}
	}

	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(com.happygoatstudios.bt.R.drawable.dialog_window_crawler1);
		setContentView(com.happygoatstudios.bt.R.layout.trigger_editor_dialog);
		
		legend= (TableRow)findViewById(R.id.trigger_notification_legend);
		responderTable = (TableLayout)findViewById(R.id.trigger_notification_table);
		refreshResponderTable();
		
		Button newresponder = (Button)findViewById(R.id.trigger_new_notification);
		newresponder.setOnClickListener(new NewResponderListener());
		
		Button donelistener = (Button)findViewById(R.id.trigger_editor_done_button);
		donelistener.setOnClickListener(new TriggerEditorDoneListener());
		
		if(isEditor) {
			EditText title = (EditText)findViewById(R.id.trigger_editor_name);
			EditText pattern = (EditText)findViewById(R.id.trigger_editor_pattern);
			
			CheckBox literal = (CheckBox)findViewById(R.id.trigger_literal_checkbox);
			
			title.setText(the_trigger.getName());
			pattern.setText(the_trigger.getPattern());
			
			literal.setChecked(!the_trigger.isInterpretAsRegex());
		}
	}
	
	private class TriggerEditorDoneListener implements View.OnClickListener {

		public void onClick(View v) {
			//return the trigger whatever the modification state.
			

			
			//responders should already be set up.
			EditText title = (EditText)findViewById(R.id.trigger_editor_name);
			EditText pattern = (EditText)findViewById(R.id.trigger_editor_pattern);
			
			CheckBox literal = (CheckBox)findViewById(R.id.trigger_literal_checkbox);
			
			if(isEditor) {
				//do editor type action
				the_trigger.setName(title.getText().toString());
				the_trigger.setPattern(pattern.getText().toString());
				the_trigger.setInterpretAsRegex(!literal.isChecked());
				try {
					service.updateTrigger(original_trigger,the_trigger);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {	
				the_trigger.setName(title.getText().toString());
				the_trigger.setPattern(pattern.getText().toString());
				the_trigger.setInterpretAsRegex(!literal.isChecked());
				try {
					service.newTrigger(the_trigger);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			finish_with.sendEmptyMessage(100);
			TriggerEditorDialog.this.dismiss();
		}
		
	}
	
	private void refreshResponderTable() {

		legend.setVisibility(View.GONE);
		
		TableRow newbutton = (TableRow)findViewById(R.id.trigger_new_responder_row);
		//responderTable.removeView(newbutton);
		responderTable.removeViews(1, responderTable.getChildCount()-1);
		
		int count = 0;
		boolean legendAdded = false;
		for(TriggerResponder responder : the_trigger.getResponders()) {
			//if(!legendAdded) {
			//	responderTable.addView(legend);
			//	legendAdded = true;
			//}
			TableRow row = new TableRow(this.getContext());
			
			TextView label = new TextView(this.getContext());
			label.setOnClickListener(new EditResponderListener(the_trigger.getResponders().indexOf(responder)));
			label.setText("Notification");
			label.setGravity(Gravity.CENTER);
			CheckBox windowOpen = new CheckBox(this.getContext()); windowOpen.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
			CheckBox windowClose = new CheckBox(this.getContext()); windowClose.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
			row.addView(label); row.addView(windowOpen); row.addView(windowClose);
			responderTable.addView(row);
			count++;
		}
		if(count > 0) {
			
			legend.setVisibility(View.VISIBLE);
		}
		
		responderTable.addView(newbutton);
	}
	
	private class EditResponderListener implements View.OnClickListener {

		int position;
		
		public EditResponderListener(int pos) {
			position = pos;
		}
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			TriggerResponder responder = the_trigger.getResponders().get(position);
			switch(responder.getType()) {
			case NOTIFICATION:
				//show the notification editor
				NotificationResponderEditor redit = new NotificationResponderEditor(TriggerEditorDialog.this.getContext(),(NotificationResponder)responder.copy(),TriggerEditorDialog.this);
				redit.show();
				break;
			case TOAST:
				break;
			case ACK:
				break;
			default:
				break;
			}
			
		}
		
	}
	
	private class NewResponderListener implements View.OnClickListener {

		public void onClick(View v) {
			//give out a list of options
			CharSequence[] items = {"Notification","Toast Message","Ack With"};
			AlertDialog.Builder builder = new AlertDialog.Builder(TriggerEditorDialog.this.getContext());
			builder.setTitle("Type:");
			
			builder.setItems(items, TriggerEditorDialog.this);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
	}

	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.dismiss();
		//Log.e("TEDITOR","DISMISSED WITH BUTTON:" + arg1);
		switch(arg1) {
		case 0: //notificaiton
			NotificationResponderEditor notifyEditor = new NotificationResponderEditor(this.getContext(),null,this);
			notifyEditor.show();
			break;
		case 1:
			break; //toast
		case 2:
			break; //ack
		default:
			break;
		}
		
	}
	
	public void editNotificationResponder(NotificationResponder edited,NotificationResponder original) {
		Log.e("TEDITOR","ATTEMPTING TO MODIFY TRIGGER");
		int pos = the_trigger.getResponders().indexOf(original);
		Log.e("TEDITOR","ORIGINAL RESPONDER LIVES AT:" + pos);
		the_trigger.getResponders().remove(pos);
		the_trigger.getResponders().add(pos,edited);
		refreshResponderTable();
	}

	public void newNotificationResponder(NotificationResponder newresponder) {
		//so the new responder is in.
		the_trigger.getResponders().add(newresponder);
		refreshResponderTable();
	}


	

}
