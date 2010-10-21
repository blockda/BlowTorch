package com.happygoatstudios.bt.button;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.window.MainWindow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class NewButtonSetEntryDialog extends Dialog {

	Handler dispatcher = null;
	
	public NewButtonSetEntryDialog(Context context,Handler reportto) {
		super(context);
		// TODO Auto-generated constructor stub
		dispatcher = reportto;
		

		
	}
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.new_buttonset_entry);
		
		Button done = (Button) findViewById(R.id.newbuttonset_done);
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				EditText ed = (EditText)findViewById(R.id.newbuttonset_entry);
				
				//send a message to add and start working on the new button set
				Message newset = dispatcher.obtainMessage(MainWindow.MESSAGE_NEWBUTTONSET,ed.getText().toString());
				dispatcher.sendMessage(newset);
				
				NewButtonSetEntryDialog.this.dismiss();
			}
		});
	}
}
