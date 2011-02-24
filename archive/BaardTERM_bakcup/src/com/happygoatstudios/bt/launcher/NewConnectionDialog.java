package com.happygoatstudios.bt.launcher;

import android.app.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.happygoatstudios.bt.R;


public class NewConnectionDialog extends Dialog {

	ReadyListener reportto = null;
	
	public NewConnectionDialog(Context context,ReadyListener useme) {
		super(context);
		// TODO Auto-generated constructor stub
		reportto = useme;
	}
	
	@Override
	public void onCreate(Bundle settings) {
		super.onCreate(settings);
		
		setContentView(R.layout.newconnectiondialog);
		
		this.setTitle("Connection Properties:");
		
		Button ok = (Button)findViewById(R.id.acceptbutton);
		Button cancel = (Button)findViewById(R.id.cancelbutton);
		
		ok.setOnClickListener(new OKListener());
		cancel.setOnClickListener(new CANCELListener());
	}
	
	private class OKListener implements View.OnClickListener {
		public void onClick(View v) {
			
			EditText disp = (EditText)findViewById(R.id.dispinput);
			EditText host = (EditText)findViewById(R.id.hostinput);
			EditText port = (EditText)findViewById(R.id.portinput);
			//String dispstr = disp.getText().toStri;
			
			reportto.ready(disp.getText().toString(), host.getText().toString(), port.getText().toString());
			NewConnectionDialog.this.dismiss();
		}
	}
	
	private class CANCELListener implements View.OnClickListener {
		public void onClick(View v) {
			NewConnectionDialog.this.dismiss();
		}
	}

}
