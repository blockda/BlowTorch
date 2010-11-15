package com.happygoatstudios.bt.launcher;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.validator.Validator;


public class NewConnectionDialog extends Dialog {

	ReadyListener reportto = null;
	
	String m_display;
	String m_host;
	int m_port;
	
	MudConnection m_prev;
	
	boolean isEditor = false;
	
	public NewConnectionDialog(Context context,ReadyListener useme) {
		super(context);
		// TODO Auto-generated constructor stub
		reportto = useme;
		isEditor = false;
	}
	
	public NewConnectionDialog(Context context,ReadyListener useme,String display,String host,int port,MudConnection old) {
		super(context);
		
		reportto = useme;
		m_display = display;
		m_host = host;
		m_port = port;
		
		
		isEditor = true;
		m_prev = old;
	}
	
	@Override
	public void onCreate(Bundle settings) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		super.onCreate(settings);
		
		setContentView(R.layout.newconnectiondialog);
		
		this.setTitle("Connection Properties:");
		
		Button ok = (Button)findViewById(R.id.acceptbutton);
		Button cancel = (Button)findViewById(R.id.cancelbutton);
		
		ok.setOnClickListener(new OKListener());
		cancel.setOnClickListener(new CANCELListener());
		
		if(isEditor) {
			EditText disp_input = (EditText)findViewById(R.id.dispinput);
			EditText host_input = (EditText)findViewById(R.id.hostinput);
			EditText port_input = (EditText)findViewById(R.id.portinput);
			
			disp_input.setText(m_display);
			host_input.setText(m_host);
			port_input.setText(Integer.toString(m_port));
		}
	}
	
	private class OKListener implements View.OnClickListener {
		public void onClick(View v) {
			
			EditText disp = (EditText)findViewById(R.id.dispinput);
			EditText host = (EditText)findViewById(R.id.hostinput);
			EditText port = (EditText)findViewById(R.id.portinput);
			
			
			Validator checker = new Validator();
			checker.add(disp, Validator.VALIDATE_NOT_BLANK, "Display Name");
			checker.add(host, Validator.VALIDATE_NOT_BLANK, "Host name");
			checker.add(host, Validator.VALIDATE_HOSTNAME, "Host name");
			checker.add(port, Validator.VALIDATE_NOT_BLANK, "Port number");
			checker.add(port, Validator.VALIDATE_NUMBER, "Port number");
			checker.add(port, Validator.VALIDATE_PORT_NUMBER, "Port number");
			
			String result = checker.validate();
			if(result != null) {
				checker.showMessage(NewConnectionDialog.this.getContext(), result);
				
				return;
			}
			//String dispstr = disp.getText().toStri;
			
			if(isEditor) {
				reportto.modify(disp.getText().toString(), host.getText().toString(), port.getText().toString(),m_prev);
			} else {
				reportto.ready(disp.getText().toString(), host.getText().toString(), port.getText().toString());
			}
			
			NewConnectionDialog.this.dismiss();
		}
	}
	
	private class CANCELListener implements View.OnClickListener {
		public void onClick(View v) {
			NewConnectionDialog.this.dismiss();
		}
	}

}
