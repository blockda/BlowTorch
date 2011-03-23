package com.happygoatstudios.bt.alias;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		initMatches();
		//load in the array adapter to hook up the list view
	}
	
	private String mPre;
	private String mPost;
	
	private void initMatches() {
		Button carrot = (Button)findViewById(R.id.carrot);
		Button dollar = (Button)findViewById(R.id.dollar);
		carrot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText pre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
				if(!pre.getText().toString().equals("")) {
					if(!pre.getText().toString().startsWith("^")) {
						pre.setText("^" + pre.getText().toString());
					} else {
						pre.setText(pre.getText().toString().substring(1, pre.getText().toString().length()));
					}
					pre.setSelection(pre.getText().toString().length());
				}
			}
		});
		
		dollar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText pre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
				if(!pre.getText().toString().equals("")) {
					if(!pre.getText().toString().endsWith("$")) {
						pre.setText(pre.getText().toString() + "$");
					} else {
						pre.setText(pre.getText().toString().substring(0, pre.getText().toString().length()-1));
					}
					pre.setSelection(pre.getText().toString().length());
				}
			}
		});
	}
	
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
		if(pre.startsWith("^")) pre = pre.substring(1,pre.length());
		if(pre.endsWith("$")) pre = pre.substring(0,pre.length()-1);
		
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
		
		try {
			if(!validateList()) {
				checker.showMessage(NewAliasDialog.this.getContext(), "Circular reference detected.");
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	Vector<String> offenders = new Vector<String>();
		//load in the array adapter to hook up the list view
	public boolean validateList() throws RemoteException {
		Boolean retval = true;
		
		//int count = apdapter.getCount();
		HashMap<String,AliasData> existingAliases = (HashMap<String, AliasData>) service.getAliases();
		int count = existingAliases.size();
		
		String testVal = "";
		if(isEditor) {
			String key = original_alias.getPre();
			if(key.startsWith("^")) key = key.substring(1,key.length());
			if(key.endsWith("$")) key = key.substring(0,key.length()-1);
			
			
			existingAliases.remove(key);
			
			AliasData tmp = new AliasData();
			tmp.setPost(((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post)).getText().toString());
			tmp.setPre(((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre)).getText().toString());
			
			String newKey = tmp.getPre();
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			testVal = newKey;
			existingAliases.put(newKey, tmp);
		} else {
			AliasData tmp = new AliasData();
			tmp.setPost(((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post)).getText().toString());
			tmp.setPre(((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre)).getText().toString());
			
			String newKey = tmp.getPre();
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			testVal = newKey;
			existingAliases.put(newKey, tmp);
		}
		
		//build "alias table"
		String regExp = "";
		//String suffix = "\\b";
		//String prefix = "\\b";
		for(String pre : existingAliases.keySet()) {
			//if()
			String suffix = "\\b";
			String prefix = "\\b";
			AliasData d = existingAliases.get(pre);
			//if(!d.equals(original_alias)) {
				if(d.getPre().startsWith("^")) prefix = "";
				if(d.getPre().endsWith("$")) suffix = "";
				regExp += "("+prefix+d.getPre()+suffix+")|";
			//}
		}
		
		regExp = regExp.substring(0,regExp.length()-1);
		
		Pattern bigmatch = Pattern.compile(regExp);
		Matcher reMatch = bigmatch.matcher(testVal);
		
		StringBuffer replaceHolder = new StringBuffer();
		
		int tries = 0;
		int maxTries = existingAliases.size() + 5; //size + wiggle room.
		boolean done = false;
		while(tries <= maxTries && !done) {
			boolean matched = false;
			while(reMatch.find()) {
				matched = true;
				AliasData d = existingAliases.get(reMatch.group(0));
				
				reMatch.appendReplacement(replaceHolder, d.getPost());
			}
			if(matched) {
				reMatch.appendTail(replaceHolder);
				
				reMatch.reset(replaceHolder.toString());
				replaceHolder.setLength(0);
				tries++;
			} else {
				done = true;
			}
		}
		
		if(tries >= maxTries) {
			return false;
		}
		
		return true;
	}

}
