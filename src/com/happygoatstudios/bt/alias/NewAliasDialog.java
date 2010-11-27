package com.happygoatstudios.bt.alias;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.validator.Validator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class NewAliasDialog extends Dialog {

	NewAliasDialogDoneListener reportto = null;
	String original_alias = null;
	int old_pos = 0;
	
	
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme) {
		super(context);
		reportto = useme;
		
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
		
		tpre.setEnabled(false);
		
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
				
				
				if(pre != null && post != null) {
					reportto.editAliasDialogDone(pre.getText().toString(), post.getText().toString(),old_pos,original_alias);
					NewAliasDialog.this.dismiss();
				}
			}
		});
	}
	
	boolean isEditor = false;
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme,String pre,String post,int position,String old_alias) {
		super(context);
		isEditor=true;
		reportto = useme;
		original_alias = old_alias;
		old_pos = position;
		mPre = pre;
		mPost = post;
	}

		//load in the array adapter to hook up the list view
	

}
