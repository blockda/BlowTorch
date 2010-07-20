package com.happygoatstudios.bt.trigger;

import com.happygoatstudios.bt.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.happygoatstudios.bt.responder.*;
import com.happygoatstudios.bt.responder.ack.*;
import com.happygoatstudios.bt.responder.notification.*;
import com.happygoatstudios.bt.responder.toast.*;

public class TriggerEditorDialog extends Dialog implements DialogInterface.OnClickListener {

	private TableRow legend;
	
	public TriggerEditorDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(com.happygoatstudios.bt.R.drawable.dialog_window_crawler1);
		setContentView(com.happygoatstudios.bt.R.layout.trigger_editor_dialog);
		
		
		legend= (TableRow)findViewById(R.id.trigger_notification_legend);
		legend.setVisibility(View.GONE);
		
		Button newresponder = (Button)findViewById(R.id.trigger_new_notification);
		newresponder.setOnClickListener(new NewResponderListener());
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
			NotificationResponderEditor notifyEditor = new NotificationResponderEditor(this.getContext());
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
}
