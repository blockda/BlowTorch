package com.happygoatstudios.bt.window;

import com.happygoatstudios.bt.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewButtonSetEntryDialog extends Dialog {

	Handler dispatcher = null;
	
	public NewButtonSetEntryDialog(Context context,Handler reportto) {
		super(context);
		// TODO Auto-generated constructor stub
		dispatcher = reportto;
		
		setContentView(R.layout.new_buttonset_entry);
		
		Button done = (Button) findViewById(R.id.newbuttonset_done);
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				EditText ed = (EditText)findViewById(R.id.newbuttonset_entry);
				
				//send a message to add and start working on the new button set
				Message newset = dispatcher.obtainMessage(BaardTERMWindow.MESSAGE_NEWBUTTONSET,ed.getText().toString());
				dispatcher.sendMessage(newset);
				
				NewButtonSetEntryDialog.this.dismiss();
			}
		});
		
	}
}
