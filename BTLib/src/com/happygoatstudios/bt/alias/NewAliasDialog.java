package com.happygoatstudios.bt.alias;

import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.validator.Validator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
//import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class NewAliasDialog extends Dialog {

	NewAliasDialogDoneListener reportto = null;
	AliasData original_alias = null;
	int old_pos = 0;
	IStellarService service;
	List<String> cant_name;
	
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme,IStellarService pService,List<String> invalid_names) {
		super(context);
		reportto = useme;
		service = pService;
		cant_name = invalid_names;
	}
	
	public void onCreate(Bundle instanceData) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		if(isEditor) {
			createeditor();
		} else {
		
			setContentView(R.layout.new_alias_dialog);
			
			Button b = (Button)findViewById(R.id.new_alias_done_button);
			
			b.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					EditText pre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
					EditText post = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post);
					
					Validator checker = new Validator();
					
					checker.add(pre, Validator.VALIDATE_NOT_BLANK, "Replace field");
					checker.add(post, Validator.VALIDATE_NOT_BLANK, "With field");
					
					String result = checker.validate();
					if(result != null) {
						checker.showMessage(NewAliasDialog.this.getContext(), result);
						return;
					} 
					
					if(!validatePhaseTwo(pre.getText().toString(),post.getText().toString(),checker)) {
						return;
					}
					
					if(pre != null && post != null) {
						reportto.newAliasDialogDone(pre.getText().toString(), post.getText().toString());
						NewAliasDialog.this.dismiss();
					}
				}
			});
			
			Button cancel = (Button)findViewById(R.id.new_alias_cancel);
			cancel.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					NewAliasDialog.this.dismiss();
				}
			});
		
		}
		//load in the array adapter to hook up the list view
	}
	
	private String mPre;
	private String mPost;
	
	private void createeditor() {
		setContentView(R.layout.new_alias_dialog);
		
		TextView titlebar = (TextView)findViewById(R.id.titlebar);
		titlebar.setText("MODIFY ALIAS");
		
		Button b = (Button)findViewById(R.id.new_alias_done_button);
		b.setText("Done");
		EditText tpre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
		EditText tpost = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post);
		
		tpre.setText(mPre);
		tpost.setText(mPost);
		
		//tpre.setEnabled(false);
		
		Button cancel = (Button)findViewById(R.id.new_alias_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				NewAliasDialog.this.dismiss();
			}
		});
		
		b.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				EditText pre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
				EditText post = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post);
				
				Validator checker = new Validator();
				
				checker.add(pre, Validator.VALIDATE_NOT_BLANK, "Replace field");
				checker.add(post, Validator.VALIDATE_NOT_BLANK, "With field");
				
				String result = checker.validate();
				if(result != null) {
					checker.showMessage(NewAliasDialog.this.getContext(), result);
					return;
				} 
				
				//STAGE TWO VALIDATION!
				//must not be: 1) special command.
				//			   2) existing alias.
				if(!validatePhaseTwo(pre.getText().toString(),post.getText().toString(),checker)) {
					return;
				}
				
				
				if(pre != null && post != null) {
					reportto.editAliasDialogDone(pre.getText().toString(), post.getText().toString(),old_pos,original_alias);
					NewAliasDialog.this.dismiss();
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private boolean validatePhaseTwo(String pre, String post, Validator checker) {
		String invalid_name = "";
		boolean is_invalid = false;
		//Log.e("FLIIP","CHECK EXISTING:");
		for(String name : cant_name) {
			//Log.e("FLIIP","EXISTING ALIAS: " + name);
			if(pre.equals(name)) {
				is_invalid = true;
				invalid_name = name;
			}
		}
		if(is_invalid) {
			String already = "Alias: \""+invalid_name+"\" exists already.";
			checker.showMessage(NewAliasDialog.this.getContext(), already);
			return false;
		}
		
		try {
			//Log.e("FLIIP","TRYING SYSTEM COMMANDS");
			for(String name : (List<String>)service.getSystemCommands()) {
				//Log.e("FLIIP","SYSTEM COMMAND: " + name);
				if(pre.equals(name)) {
					is_invalid = true;
					invalid_name = name;
				}
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		if(is_invalid) {
			String system = "\""+invalid_name+"\" is reserved for a system command.";
			checker.showMessage(NewAliasDialog.this.getContext(), system);
			return false;
		}
		
		return true;
	}
	
	boolean isEditor = false;
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme,String pre,String post,int position,AliasData old_alias,IStellarService pService,List<String> invalid_names) {
		super(context);
		isEditor=true;
		reportto = useme;
		original_alias = old_alias;
		old_pos = position;
		mPre = pre;
		mPost = post;
		service = pService;
		cant_name = invalid_names;
	}

		//load in the array adapter to hook up the list view
	

}
