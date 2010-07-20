package com.happygoatstudios.bt.trigger;

import com.happygoatstudios.bt.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.happygoatstudios.bt.responder.*;
import com.happygoatstudios.bt.responder.ack.*;
import com.happygoatstudios.bt.responder.notification.*;
import com.happygoatstudios.bt.responder.toast.*;

public class TriggerEditorDialog extends Dialog implements DialogInterface.OnClickListener,NotificationResponderDoneListener{

	private TableRow legend;
	private TableLayout responderTable;
	
	private TriggerData the_trigger;
	private boolean isEditor = false;
	
	public TriggerEditorDialog(Context context,TriggerData input) {
		super(context);
		// TODO Auto-generated constructor stub
		if(input == null) {
			the_trigger = new TriggerData();
		} else {
			the_trigger = input;
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

	public void newNotificationResponder(NotificationResponder newresponder) {
		//so the new responder is in.
		the_trigger.getResponders().add(newresponder);
		refreshResponderTable();
	}
	

}
