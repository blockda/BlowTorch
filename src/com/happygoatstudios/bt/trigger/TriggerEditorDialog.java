package com.happygoatstudios.bt.trigger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.happygoatstudios.bt.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
//import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SimpleAdapter.ViewBinder;


import com.happygoatstudios.bt.responder.*;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.TriggerResponder.RESPONDER_TYPE;
import com.happygoatstudios.bt.responder.ack.*;
import com.happygoatstudios.bt.responder.notification.*;
import com.happygoatstudios.bt.responder.toast.*;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.validator.Validator;

public class TriggerEditorDialog extends Dialog implements DialogInterface.OnClickListener,TriggerResponderEditorDoneListener{

	private TableRow legend;
	private TableLayout responderTable;
	
	private TriggerData the_trigger;
	private TriggerData original_trigger;
	private boolean isEditor = false;
	
	private IStellarService service;
	
	private Handler finish_with;
	
	private CheckBox literal;
	private CheckBox once;
	
	HashMap<Integer,Integer> checkopens;
	HashMap<Integer,Integer> checkclosed;
	
	public TriggerEditorDialog(Context context,TriggerData input,IStellarService pService,Handler finisher) {
		super(context);
		
		service = pService;
		finish_with = finisher;
		if(input == null) {
			the_trigger = new TriggerData();
			
		} else {
			the_trigger = input.copy();
			original_trigger = input.copy();
			isEditor=true;
		}
		
		//Log.e("TEDITOR","CONSTRUCTED, GOT A FUCKING STUPID OBJECT THAT HAS RESPONDERS.");
		//for(TriggerResponder responder : the_trigger.getResponders()) {
		///	Log.e("TEDITOR","responder " + responder.getType() + " fires " + responder.getFireType());
		//}
		
		//initialized hashmaps.
		checkopens = new HashMap<Integer,Integer>();
		checkclosed = new HashMap<Integer,Integer>();
		
	}

	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(com.happygoatstudios.bt.R.drawable.dialog_window_crawler1);
		setContentView(com.happygoatstudios.bt.R.layout.trigger_editor_dialog);
		
		ScrollView sv = (ScrollView)findViewById(R.id.trigger_editor_scroll_container);
		sv.setScrollbarFadingEnabled(false);
		
		legend= (TableRow)findViewById(R.id.trigger_notification_legend);
		responderTable = (TableLayout)findViewById(R.id.trigger_notification_table);
		refreshResponderTable();
		
		Button newresponder = (Button)findViewById(R.id.trigger_new_notification);
		newresponder.setOnClickListener(new NewResponderListener());
		
		Button donelistener = (Button)findViewById(R.id.trigger_editor_done_button);
		donelistener.setOnClickListener(new TriggerEditorDoneListener());
		
		Button cancel = (Button)findViewById(R.id.new_trigger_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				boolean changed = hasTriggerChanged();
				if(changed) {
					//Log.e("TEDITR","DATA CHANGED");
					AlertDialog.Builder builder = new AlertDialog.Builder(TriggerEditorDialog.this.getContext());
					builder.setTitle("Destroy Changes?");
					builder.setMessage("You have changed the data of this trigger, are you sure you want to dismiss?");
					DialogInterface.OnClickListener listner = new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							switch(arg1) {
							case DialogInterface.BUTTON_POSITIVE: //quit
								arg0.dismiss();
								TriggerEditorDialog.this.dismiss();
								break;
							case DialogInterface.BUTTON_NEGATIVE: //cancel
								arg0.dismiss();
								break;
							case DialogInterface.BUTTON_NEUTRAL: //save and quit
								arg0.dismiss();
								Button done = (Button)TriggerEditorDialog.this.findViewById(R.id.trigger_editor_done_button);
								done.performClick();
								break;
								
							}
						}
					};
					builder.setPositiveButton("Destroy changes", listner);
					builder.setNegativeButton("Cancel.", listner);
					builder.setNeutralButton("Save and quit.", listner);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					//Log.e("TEDITR","DATA NOT CHANGED");
					TriggerEditorDialog.this.dismiss();
				}
				
			}
		});
		
		literal = (CheckBox)findViewById(R.id.trigger_literal_checkbox);
		once = (CheckBox)findViewById(R.id.trigger_once_checkbox);
		
		//if(isEditor) {
		EditText title = (EditText)findViewById(R.id.trigger_editor_name);
		EditText pattern = (EditText)findViewById(R.id.trigger_editor_pattern);
		
		CheckBox literal = (CheckBox)findViewById(R.id.trigger_literal_checkbox);
		
		title.setText(the_trigger.getName());
		pattern.setText(the_trigger.getPattern());
		
		literal.setChecked(!the_trigger.isInterpretAsRegex());
		once.setChecked(the_trigger.isFireOnce());
		
		if(isEditor) {
			Button editdone = (Button)findViewById(R.id.trigger_editor_done_button);
			editdone.setText("Modify this trigger.");
		}	
		//}
		
		literal.setOnCheckedChangeListener(new LiteralCheckChangedListener());
		once.setOnCheckedChangeListener(new FireOnceCheckChangedListener());
	}
	
	
	private boolean hasTriggerChanged() {
		if(original_trigger == null) {
			return false;
		}
		TriggerData test = original_trigger.copy();
		
		EditText title = (EditText)findViewById(R.id.trigger_editor_name);
		EditText pattern = (EditText)findViewById(R.id.trigger_editor_pattern);
		
		CheckBox literal = (CheckBox)findViewById(R.id.trigger_literal_checkbox);
		CheckBox fireOnce = (CheckBox)findViewById(R.id.trigger_once_checkbox);
		boolean retval = false;
		if(!(title.getText().toString().equals(test.getName()))) retval = true;
		if(!(pattern.getText().toString().equals(test.getPattern()))) retval = true;
		if(test.isInterpretAsRegex() != !literal.isChecked()) retval = true;
		if(test.isFireOnce() != fireOnce.isChecked()) retval = true; 
		
		boolean checkresponder = false;
		if(test.getResponders().size() == the_trigger.getResponders().size()) { checkresponder = true; } else { retval = true; }
		
		if(checkresponder) {
			Iterator<TriggerResponder> test_responders = original_trigger.getResponders().iterator();
			Iterator<TriggerResponder> current_responders = the_trigger.getResponders().iterator();
			
			while(test_responders.hasNext()) {
				TriggerResponder torig = test_responders.next();
				TriggerResponder tcurr = current_responders.next();
				if(!torig.equals(tcurr)) {
					retval = true;
				}
			}
			
		}
		return retval;
	}
	
	private class TriggerEditorDoneListener implements View.OnClickListener {

		public void onClick(View v) {
			//return the trigger whatever the modification state.
			

		
			
			//responders should already be set up.
			EditText title = (EditText)findViewById(R.id.trigger_editor_name);
			EditText pattern = (EditText)findViewById(R.id.trigger_editor_pattern);
			
			Validator checker = new Validator();
			checker.add(title, Validator.VALIDATE_NOT_BLANK, "Trigger name");
			checker.add(pattern,Validator.VALIDATE_NOT_BLANK,"Pattern");
			
			String result = checker.validate();
			if(result != null) {
				checker.showMessage(TriggerEditorDialog.this.getContext(), result);
				return;
			}
			
			CheckBox literal = (CheckBox)findViewById(R.id.trigger_literal_checkbox);
			
			if(pattern.getText().toString().equals("")) {
				//the pattern can not be blank.
				AlertDialog.Builder builder = new AlertDialog.Builder(TriggerEditorDialog.this.getContext());
				builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
				});
				
				builder.setMessage("Pattern can not be blank.");
				builder.setTitle("Pattern error.");
				AlertDialog error = builder.create();
				error.show();
				
				return;
			} else {
				//check to make sure it is a valid pattern
				if(the_trigger.isInterpretAsRegex()) {
					try {
						Pattern p = Pattern.compile(pattern.getText().toString());
					} catch (PatternSyntaxException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(TriggerEditorDialog.this.getContext());
						builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						
						builder.setMessage("Pattern Error: " + e.getMessage());
						builder.setTitle("Problem with pattern syntax.");
						
						AlertDialog error = builder.create();
						error.show();
						
						return;
					}
				}
			}
			
			if(isEditor) {
				//do editor type action
				the_trigger.setName(title.getText().toString());
				the_trigger.setPattern(pattern.getText().toString());
				the_trigger.setInterpretAsRegex(!literal.isChecked());
				
				//i don't care anymore about the checkchanged listeners. it was a neat idea, but here goes.
				
				/*TableLayout table = (TableLayout)findViewById(R.id.trigger_notification_table);
				//Log.e("TEDITOR","SENDING TRIGGER TO SERVICE:");
				for(TriggerResponder responder : the_trigger.getResponders()) {
					//Log.e("TEDITOR","RESPONDER TYPE " + responder.getType() + " RESPONDS " + responder.getFireType());
					int position = the_trigger.getResponders().indexOf(responder);
					CheckBox open = (CheckBox)table.findViewById(checkopens.get(position));
					CheckBox closed = (CheckBox)table.findViewById(checkclosed.get(position));
					if(open.isChecked() && closed.isChecked()) {
						//Log.e("TEDITOR","EDITOR ATTEMPING TO SET RESPONDER TO BOTH");
						the_trigger.getResponders().get(position).setFireType(FIRE_WHEN.WINDOW_BOTH);
					} else if(open.isChecked()) {
						//Log.e("TEDITOR","EDITOR ATTEMPING TO SET RESPONDER TO OPEN");
						the_trigger.getResponders().get(position).setFireType(FIRE_WHEN.WINDOW_OPEN);
					} else if(closed.isChecked()) {
						the_trigger.getResponders().get(position).setFireType(FIRE_WHEN.WINDOW_CLOSED);
						//Log.e("TEDITOR","EDITOR ATTEMPING TO SET RESPONDER TO CLOSED");
					} else {
						//Log.e("TEDITOR","EDITOR ATTEMPING TO SET RESPONDER TO NONE");
						the_trigger.getResponders().get(position).setFireType(FIRE_WHEN.WINDOW_NEVER);
					}
					//Log.e("TEDITOR","RESPONDER TYPE " + responder.getType() + " RESPONDS " + responder.getFireType() + " AFTER MY LITTLE \"PUSH\"");
				}*/
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
		
		RelativeLayout p = (RelativeLayout)findViewById(R.id.newtriggerlayout);
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
		boolean legendAdded = false;
		for(TriggerResponder responder : the_trigger.getResponders()) {
			//if(!legendAdded) {
			//	responderTable.addView(legend);
			//	legendAdded = true;
			//}
			TableRow row = new TableRow(this.getContext());
			
			TextView label = new TextView(this.getContext());
			label.setOnClickListener(new EditResponderListener(the_trigger.getResponders().indexOf(responder)));
			if(responder.getType() == RESPONDER_TYPE.NOTIFICATION) {
				label.setText("Notification: " + ((NotificationResponder)responder).getTitle());
			} else if(responder.getType() == RESPONDER_TYPE.TOAST) {
				label.setText("Toast Message: " + ((ToastResponder)responder).getMessage());
			} else if(responder.getType() == RESPONDER_TYPE.ACK){
				label.setText("Ack With: " + ((AckResponder)responder).getAckWith());
			}
			label.setGravity(Gravity.CENTER);
			label.setSingleLine(true);
			label.setWidth((int) (130 * this.getContext().getResources().getDisplayMetrics().density));
			LinearLayout l1 = new LinearLayout(this.getContext());
			l1.setGravity(Gravity.CENTER);
			LinearLayout l2 = new LinearLayout(this.getContext());
			l2.setGravity(Gravity.CENTER);
			CheckBox windowOpen = new CheckBox(this.getContext()); windowOpen.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL); windowOpen.setId(checkboxnumber);
			checkopens.put(the_trigger.getResponders().indexOf(responder), checkboxnumber);
			checkboxnumber++;
			
			CheckBox windowClose = new CheckBox(this.getContext()); windowClose.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL); windowClose.setId(checkboxnumber);
			checkclosed.put(the_trigger.getResponders().indexOf(responder),checkboxnumber);
			checkboxnumber++;

			
			windowOpen.setOnCheckedChangeListener(new WindowOpenCheckChangeListener(the_trigger.getResponders().indexOf(responder)));
			windowClose.setOnCheckedChangeListener(new WindowClosedCheckChangeListener(the_trigger.getResponders().indexOf(responder)));
			
			Button delete = new Button(this.getContext()); delete.setBackgroundResource(android.R.drawable.ic_delete);
			delete.setOnClickListener(new DeleteResponderListener(the_trigger.getResponders().indexOf(responder)));
			
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
				ToastResponderEditor tedit = new ToastResponderEditor(TriggerEditorDialog.this.getContext(),(ToastResponder)responder.copy(),TriggerEditorDialog.this);
				tedit.show();
				break;
			case ACK:
				AckResponderEditor aedit = new AckResponderEditor(TriggerEditorDialog.this.getContext(),(AckResponder)responder.copy(),TriggerEditorDialog.this);
				aedit.show();
				break;
			default:
				break;
			}
			
		}
		
	}
	
	private class WindowOpenCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

		private final int position;
		
		WindowOpenCheckChangeListener(int i) {
			position = i;
		}
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			if(checked) {
				//check the closed check state.
				the_trigger.getResponders().get(position).addFireType(FIRE_WHEN.WINDOW_OPEN);
				///Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " ADDING windowOpen");
			} else {
				the_trigger.getResponders().get(position).removeFireType(FIRE_WHEN.WINDOW_OPEN);
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
				the_trigger.getResponders().get(position).addFireType(FIRE_WHEN.WINDOW_CLOSED);
				//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " ADDING windowClosed");
			} else {
				the_trigger.getResponders().get(position).removeFireType(FIRE_WHEN.WINDOW_CLOSED);
				//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " REMOVING windowClosed");
				
			}
			//Log.e("TEDITOR","TRIGGER TYPE " + the_trigger.getResponders().get(position).getType().getIntVal() + " AT "+ position + " NOW " + the_trigger.getResponders().get(position).getFireType().getString());
			
		}
		
	};
	
	private class DeleteResponderListener implements View.OnClickListener,DialogInterface.OnClickListener {

		int position;
		
		public DeleteResponderListener(int i) {
			position = i;
		}
		
		public void onClick(View arg0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(TriggerEditorDialog.this.getContext());
			builder.setPositiveButton("Delete", this);
			builder.setNegativeButton("Cancel", this);
			builder.setTitle("Are you sure?");
			AlertDialog deleter = builder.create();
			deleter.show();
		}

		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			//Log.e("TEDITOR","DELETE ALERT RETURNED " + arg1);
			if(arg1 == DialogInterface.BUTTON_POSITIVE) {
				//really delete the button
				the_trigger.getResponders().remove(position);
				refreshResponderTable();
			}
		}
		
	};
	
	
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
	
	private class LiteralCheckChangedListener implements CompoundButton.OnCheckedChangeListener {

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if(arg1) {
				the_trigger.setInterpretAsRegex(false); //NO NOT INTERPRET AS REGEX
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(TriggerEditorDialog.this.getContext());
				builder.setTitle("Don't kill your phone.");
				builder.setMessage("You have turned on regular expression parsing for this trigger. Poorly formed expressions can cause the following: break other triggers, drain your battery, dump thousands of bytes to the server, etc. Please read the guide to the Java Pattern Class if you need more information. Have a nice day.");
				builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
					
				});
				
				AlertDialog dialog = builder.create();
				dialog.show();
				the_trigger.setInterpretAsRegex(true); //DO INTERPRET AS REGEX.
			}
		}
		
	}
	
	private class FireOnceCheckChangedListener implements CompoundButton.OnCheckedChangeListener {

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if(arg1) {
				the_trigger.setFireOnce(true); 
			} else {
				the_trigger.setFireOnce(false); 
			}
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
		case 1: //toast
			ToastResponderEditor tedit = new ToastResponderEditor(TriggerEditorDialog.this.getContext(),null,TriggerEditorDialog.this);
			tedit.show();
			break; 
		case 2:
			AckResponderEditor aedit = new AckResponderEditor(TriggerEditorDialog.this.getContext(),null,TriggerEditorDialog.this);
			aedit.show();
			break; //ack
		default:
			break;
		}
		
	}
	

	public void editTriggerResponder(TriggerResponder edited,TriggerResponder original) {
		
		//Log.e("TEDITOR","ATTEMPTING TO MODIFY TRIGGER");
		int pos = the_trigger.getResponders().indexOf(original);
		//Log.e("TEDITOR","ORIGINAL RESPONDER LIVES AT:" + pos);
		the_trigger.getResponders().remove(pos);
		the_trigger.getResponders().add(pos,edited);
		refreshResponderTable();
		
		//Log.e("TEDITOR","ATTEMPTING TO MODIFY RESPONDERS");
		//for(TriggerResponder responder : the_trigger.getResponders()) {
			//Log.e("TEDITOR","RESPONDER TYPE " + responder.getType() + " RESPONDS " + responder.getFireType());
		//}
	}

	public void newTriggerResponder(TriggerResponder newresponder) {
		//so the new responder is in.
		the_trigger.getResponders().add(newresponder);
		refreshResponderTable();
	}

	




	

}
