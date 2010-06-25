package com.happygoatstudios.bt.window;

import java.util.Map;


import com.happygoatstudios.bt.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewAliasDialog extends Dialog {

	NewAliasDialogDoneListener reportto = null;
	
	public NewAliasDialog(Context context,NewAliasDialogDoneListener useme) {
		super(context);
		// TODO Auto-generated constructor stub
		reportto = useme;
		
		setContentView(R.layout.new_alias_dialog);
		
		Button b = (Button)findViewById(R.id.new_alias_done_button);
		
		b.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				EditText pre = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_pre);
				EditText post = (EditText)NewAliasDialog.this.findViewById(R.id.new_alias_post);
				
				if(pre != null && post != null) {
					reportto.newAliasDialogDone(pre.getText().toString(), post.getText().toString());
					NewAliasDialog.this.dismiss();
				}
			}
		});
		//load in the array adapter to hook up the list view
	}

}
