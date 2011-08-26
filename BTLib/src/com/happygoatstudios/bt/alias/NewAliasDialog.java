package com.happygoatstudios.bt.alias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IConnectionBinder;
import com.happygoatstudios.bt.validator.Validator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


public class NewAliasDialog extends Dialog {

	NewAliasDialogDoneListener reportto = null;
	AliasData original_alias = null;
	int old_pos = 0;
	IConnectionBinder service;
	List<String> cant_name;
	
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme,IConnectionBinder pService,List<String> invalid_names) {
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
						CheckBox carrot = (CheckBox)NewAliasDialog.this.findViewById(R.id.carrot);
						CheckBox dollar = (CheckBox)NewAliasDialog.this.findViewById(R.id.dollar);
						String prefix = "^";
						String suffix= "$";
						if(!carrot.isChecked()) {
							prefix = "";
						}
						
						if(!dollar.isChecked()) {
							suffix = "";
						}
						reportto.newAliasDialogDone(prefix + pre.getText().toString() + suffix, post.getText().toString());
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
		CheckBox carrot = (CheckBox)findViewById(R.id.carrot);
		CheckBox dollar = (CheckBox)findViewById(R.id.dollar);
		
		TextView pre = (TextView)findViewById(R.id.new_alias_pre);
		
		pre.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				//Log.e("EDITOR","EDITOR ACTION: " + actionId);
				//if(event != null) Log.e("EDITOR","EVENT IS NOT NULL:" + event.describeContents());
				
				String str = v.getText().toString();
				if(str.startsWith("^")) {
					str = str.substring(1,str.length());
					((CheckBox)findViewById(R.id.carrot)).setChecked(true);
				}
				
				if(str.endsWith("$")) {
					str = str.substring(0,str.length()-1);
					((CheckBox)findViewById(R.id.dollar)).setChecked(true);
				}
				v.setText(str);
				return false;
			}
			
		});
		
		pre.setOnFocusChangeListener(new OnFocusChangeListener() {

			
			public void onFocusChange(View v, boolean focused) {
				if(!focused) {
					EditText e = (EditText)v;
					
					String str = e.getText().toString();
					if(str.startsWith("^")) {
						str = str.substring(1,str.length());
						((CheckBox)findViewById(R.id.carrot)).setChecked(true);
					}
					
					if(str.endsWith("$")) {
						str = str.substring(0,str.length()-1);
						((CheckBox)findViewById(R.id.dollar)).setChecked(true);
					}
					e.setText(str);
				}
			}
			
		});
		/*carrot.setOnClickListener(new View.OnClickListener() {
			
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
		});*/
	}
	
	private void createeditor() {
		setContentView(R.layout.new_alias_dialog);
		
		TextView titlebar = (TextView)findViewById(R.id.titlebar);
		titlebar.setText("MODIFY ALIAS");
		
		Button b = (Button)findViewById(R.id.new_alias_done_button);
		b.setText("Done");
		EditText tpre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
		EditText tpost = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post);
		
		
		tpost.setText(mPost);
		
		CheckBox carrot = (CheckBox)NewAliasDialog.this.findViewById(R.id.carrot);
		CheckBox dollar = (CheckBox)NewAliasDialog.this.findViewById(R.id.dollar);
		
		String preText = mPre;
		if(preText.startsWith("^")) {
			preText = preText.substring(1,preText.length());
			carrot.setChecked(true);
		} else {
			carrot.setChecked(false);
		}
		if(preText.endsWith("$")) {
			preText = preText.substring(0,preText.length()-1);
			dollar.setChecked(true);
		} else {
			dollar.setChecked(false);
		}
		
		
		tpre.setText(preText);
		
		
		
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
					CheckBox carrot = (CheckBox)NewAliasDialog.this.findViewById(R.id.carrot);
					CheckBox dollar = (CheckBox)NewAliasDialog.this.findViewById(R.id.dollar);
					String prefix = "^";
					String suffix= "$";
					if(!carrot.isChecked()) {
						prefix = "";
					}
					
					if(!dollar.isChecked()) {
						suffix = "";
					}
					
					reportto.editAliasDialogDone(prefix + pre.getText().toString() + suffix, post.getText().toString(),old_pos,original_alias);
					NewAliasDialog.this.dismiss();
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private boolean validatePhaseTwo(String pre, String post, Validator checker) {
		if(pre.startsWith("^")) pre = pre.substring(1,pre.length());
		if(pre.endsWith("$")) pre = pre.substring(0,pre.length()-1);
		
		String original_pre = "";
		if(original_alias != null) {
			original_pre = original_alias.getPre();
		} else {
			original_pre = "";
		}
		if(original_pre.startsWith("^")) original_pre = original_pre.substring(1,original_pre.length());
		if(original_pre.endsWith("$")) original_pre = original_pre.substring(0,original_pre.length()-1);
		String invalid_name = "";
		boolean is_invalid = false;
		//Log.e("FLIIP","CHECK EXISTING:");
		for(String name : cant_name) {
			//Log.e("FLIIP","EXISTING ALIAS: " + name);
			if(pre.equals(name) && !name.equals(original_pre)) {
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
			Object[] offenders = validateList();
			if(offenders != null && offenders.length > 0) {
				String offendersStr = "";
				for(int i=0;i<offenders.length;i++) {
					offendersStr += (String)offenders[i] + ", ";
				}
				offendersStr = offendersStr.substring(0,offendersStr.length()-2);
				checker.showMessage(NewAliasDialog.this.getContext(), "Circular reference with aliases: "+offendersStr);
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	boolean isEditor = false;
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme,String pre,String post,int position,AliasData old_alias,IConnectionBinder pService,List<String> invalid_names) {
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
	public Object[] validateList() throws RemoteException {
		Boolean retval = true;
		ArrayList<String> offenders = new ArrayList<String>();
		//int count = apdapter.getCount();
		HashMap<String,AliasData> existingAliases = (HashMap<String, AliasData>) service.getAliases();
		int count = existingAliases.size();
		
		CheckBox carrot = (CheckBox)NewAliasDialog.this.findViewById(R.id.carrot);
		CheckBox dollar = (CheckBox)NewAliasDialog.this.findViewById(R.id.dollar);
		String pre_prefix = "";
		String pre_suffix = "";
		
		if(carrot.isChecked()) {
			pre_prefix = "^";
		}
		
		if(dollar.isChecked()) {
			pre_suffix = "$";
		}
		
		String testVal = "";
		if(isEditor) {
			String key = original_alias.getPre();
			if(key.startsWith("^")) key = key.substring(1,key.length());
			if(key.endsWith("$")) key = key.substring(0,key.length()-1);
			
			
			existingAliases.remove(key);
			
			AliasData tmp = new AliasData();
			tmp.setPost(((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post)).getText().toString());
			tmp.setPre(pre_prefix + ((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre)).getText().toString() + pre_suffix);
			
			String newKey = tmp.getPre();
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			testVal = newKey;
			existingAliases.put(newKey, tmp);
		} else {
			AliasData tmp = new AliasData();
			tmp.setPost(((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post)).getText().toString());
			tmp.setPre(pre_prefix + ((EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre)).getText().toString() + pre_suffix);
			
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
				if(tries > 0) { if(!offenders.contains(reMatch.group(0))) { offenders.add(reMatch.group(0)); } }
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
			return offenders.toArray();
		}
		
		return null;
	}

}
