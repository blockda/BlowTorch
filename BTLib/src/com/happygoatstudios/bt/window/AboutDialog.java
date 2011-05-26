package com.happygoatstudios.bt.window;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.settings.ConfigurationLoader;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context);
		
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		this.setContentView(ConfigurationLoader.getAboutDialogResource(this.getContext()));

		try {
			String str = this.getContext().getPackageManager().getPackageInfo(this.getContext().getPackageName(), Context.CONTEXT_INCLUDE_CODE).versionName;
			int abtid = this.getContext().getResources().getIdentifier("blowtorch_about", "id", this.getContext().getPackageName());
			TextView v = (TextView) this.findViewById(abtid);
			v.setText("BlowTorch " + str);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int aardid = this.getContext().getResources().getIdentifier("aardwolf_button", "id", this.getContext().getPackageName());
		if(aardid != 0) {
		this.findViewById(aardid).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent web_help = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.aardmud.org/"));
				AboutDialog.this.getContext().startActivity(web_help);
			}
		});
		}
		
		int btid = this.getContext().getResources().getIdentifier("blowtorch_button", "id", this.getContext().getPackageName()); 
		this.findViewById(btid).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent web_help = new Intent(Intent.ACTION_VIEW,Uri.parse("http://bt.happygoatstudios.com/"));
				AboutDialog.this.getContext().startActivity(web_help);
			}
		});
		
		//setup links
	}

}
