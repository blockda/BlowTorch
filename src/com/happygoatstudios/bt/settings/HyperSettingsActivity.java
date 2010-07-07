package com.happygoatstudios.bt.settings;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IBaardTERMService;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class HyperSettingsActivity extends PreferenceActivity {
	
	//private IBaardTERMService service = null;
	
	//public HyperSettingsActivity(IBaardTERMService serv) {
	//	service = serv;
	//}
	
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		
		this.addPreferencesFromResource(R.xml.preferences);
		
		ListPreference fonts = (ListPreference)findPreference("FONT_NAME");
		//List<String> fontnames = new ArrayList<String>();
		
		HashMap<String,String> fontmap = new HashMap<String,String>();
		
		File temp = new File("/system/fonts/");
		
		FilenameFilter ttf_only = new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".ttf");
			}
			
		};
		
		File tmp = Environment.getExternalStorageDirectory();
		String sdstate = Environment.getExternalStorageState();
		HashMap<String,String> efonts = new HashMap<String,String>();
		if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
			for(File efont : tmp.listFiles(ttf_only)) {
				efonts.put(efont.getName(), efont.getPath());
			}
		}
		
		for(File font : temp.listFiles(ttf_only)) {
			//fontnames.add(font.getName());
			fontmap.put(font.getName(),font.getPath());
		}
		
		Set<String> keys = fontmap.keySet();
		
		List<String> sortedkeys = new ArrayList<String>(keys);
		
		Collections.sort(sortedkeys,String.CASE_INSENSITIVE_ORDER);
		
		Set<String> ekeys = efonts.keySet();
		List<String> sortedEkeys = new ArrayList<String>(ekeys);
		Collections.sort(sortedEkeys,String.CASE_INSENSITIVE_ORDER);
		
		//Collections.sort(fontnames,String.CASE_INSENSITIVE_ORDER);
		
		//dumb step. why oh why can't i convert the list to an array
		String[] names = new String[sortedkeys.size() + sortedEkeys.size() + 3];
		names[0] = "monospace";
		names[1] = "sans serif";
		names[2] = "default";
		
		String[] paths = new String[sortedkeys.size() + sortedEkeys.size() + 3];
		paths[0] = "monospace";
		paths[1] = "sans serif";
		paths[2] = "default";
		int i = 3;
		
		for(String str : sortedEkeys) {
			names[i] = str;
			paths[i] = efonts.get(str);
			i++;
		}
		
		for(String str : sortedkeys) {
			names[i] = str;
			paths[i] = fontmap.get(str);
			i++;
		}
		
		fonts.setEntries(names);
		fonts.setEntryValues(paths);
		
		
	}
	
	public void onBackPressed() {
		Intent retval = new Intent();
		setResult(RESULT_OK,retval);
		finish();
	}
}
