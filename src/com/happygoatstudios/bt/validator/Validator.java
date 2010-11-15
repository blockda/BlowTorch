package com.happygoatstudios.bt.validator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.launcher.NewConnectionDialog;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Validator {
	public static int VALIDATE_NOT_BLANK = 0x01;
	public static int VALIDATE_HOSTNAME = 0x02;
	public static int VALIDATE_PORT_NUMBER = 0x04;
	public static int VALIDATE_NUMBER = 0x08;
	public static int VALIDATE_NUMBER_NOT_ZERO = 0x10;
	
	public Validator() {
		//nothing to construct.
		validation_list = new TreeMap<String,ValidationItem>();
	}
	
	TreeMap<String,ValidationItem> validation_list;
	
	public void add(EditText field,int what,String message) {
		ValidationItem item = new ValidationItem(what,field);
		
		if(validation_list.containsKey(message)) {
			validation_list.get(message).addValidation(what);
		} else {
			validation_list.put(message, item);		
		}
	}
	
	public String validate() {
		//will return error strings and automagically highlight errors.
		//return null if validated.
		String message = "";
		
		boolean validated = true;
		for(String field : validation_list.keySet()) {
			//get the item out.
			ValidationItem tmp = validation_list.get(field);
			if((tmp.What & VALIDATE_NOT_BLANK) == VALIDATE_NOT_BLANK) {
				if(tmp.field.getText().toString().equals("")) {
					//field is blank
					message += field + " can not be blank.\n";
					validated = false;
				}
			}
			
			if((tmp.What & VALIDATE_HOSTNAME) == VALIDATE_HOSTNAME) {
				//check to see if there is a port specified. if there is it is invalid by me.
				//because apparantly EVERYTHING YOU ENTER CAN BE A VALID URL BY THE URL and INETADDRESS CLASS
				//only check if there is text to search
				if(!tmp.field.getText().toString().equals("")) {
					if(tmp.field.getText().toString().contains(":")) {
						validated = false;
						message += tmp.field.getText().toString() + " is not a valid hostname. Should be of the form hostname.com or IP address.\n";
						tmp.field.setSelection(0, tmp.field.getText().toString().length());
						
					}
				}
				
				
			}
			
			if((tmp.What & VALIDATE_PORT_NUMBER) == VALIDATE_PORT_NUMBER) {
				if(!tmp.field.getText().toString().equals("")) {
					try {
						Integer i = Integer.parseInt(tmp.field.getText().toString());
						if(i>=0 && i < 65535) {
							//passed.
							//Log.e("VALIDATOR","PORT NUMBER VALIDATION PASSED WITH " + i);
						} else {
							validated = false;
							message += field + " should be a number between 0 and 65535.\n";
						}
					} catch (NumberFormatException e) {
						//shouldn't be here, they should use VALIDATE_NUMBER ALSO
						//validated = false;
						//message += field + " is not a valid number.";
					}
				}
			}
			
			if((tmp.What & VALIDATE_NUMBER) == VALIDATE_NUMBER) {
				if(!tmp.field.getText().toString().equals("")) {
					try {
						Integer i = Integer.parseInt(tmp.field.getText().toString());
					} catch (NumberFormatException e) {
						validated = false;
						message += field + " does not contain a valid number.\n";
					}
				}
			}
			
			if((tmp.What & VALIDATE_NUMBER_NOT_ZERO) == VALIDATE_NUMBER_NOT_ZERO) {
				if(!tmp.field.getText().toString().equals("")) {
					try { 
						Integer i = Integer.parseInt(tmp.field.getText().toString());
						if(i < 1) {
							validated = false;
							message += field + " must contain a number greater than 0.\n";
						}
					} catch (NumberFormatException e) {
						//they should use VALIDATE_NUMBER ALSO ON THIS.
					}
				}
			}
		}
		
		if(validated) {				
			return null;
		} else {
			return message;
		}
	}
	
	public void showMessage(Context c,String result) {
		
		
		//TextView view = new TextView(c);
		//view.setText(result);
		//view.setTextSize(35);
		//view.setTextColor(0xFF000000);
		//view.setBackgroundColor(0xFFFF3333);
		///LayoutParams.
		String[] parts = result.split("\n");
		int longest = 0;
		for(String peice : parts) {
			if(peice.length() > longest) {
				longest = peice.length();
			}
		}
		
		String message = "INVALID INPUT DETECTED\n";
		for(int i=0;i<longest;i++) {
			message += "-";
		}
		message += "\n";
		
		message += result;
		
		//lop off the end message.
		if(message.endsWith("\n")) {
			message = message.substring(0, message.length()-1);
		//	Pattern nukelastline = Pattern.compile("(.*?)(\n)$",Pattern.DOTALL);
		//	Matcher m = nukelastline.matcher(message);
		//	message = m.replaceAll("");
		}

		//Toast t = new Toast(c);
		
		Toast t = Toast.makeText(c, message, 4000);
		//t.setView(view);
		
		
		LayoutInflater li = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.toast_override, null);
		TextView tv = (TextView) v.findViewById(R.id.message);
		tv.setText(message);
		//TextView v = (TextView)t.getView();
		//v.setTypeface(Typeface.MONOSPACE);
		t.setView(v);
		t.setDuration(Toast.LENGTH_LONG);
		t.show();
		/*AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setTitle("Invalid Input Detected");
		builder.setMessage(result);
		builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();*/
	}
	
	private class ValidationItem {
		public int What;
		public EditText field;
		
		public ValidationItem(int type,EditText pField) {
			What = type;
			field = pField;
		}
		
		public void addValidation(int what) {
			//Log.e("VALIDATOR","ADDING VALIDATION " + what);
			What |= what;
			//Log.e("VALIDATOR","VALIDATION NOW " + What);
			
		}
	}
	
	
	
}
