package com.happygoatstudios.bt.settings;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.R;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
//import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class HyperSettingsActivity extends PreferenceActivity {
	
	//private IBaardTERMService service = null;
	
	//public HyperSettingsActivity(IBaardTERMService serv) {
	//	service = serv;
	//}
	private Pattern xmlinsensitive = Pattern.compile("^.+\\.[Xx][Mm][Ll]$");
	private Matcher xmlimatcher = xmlinsensitive.matcher("");
	
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		
		this.addPreferencesFromResource(R.xml.preferences);
		
		ListView the_list = (ListView) this.findViewById(android.R.id.list);
		the_list.setScrollbarFadingEnabled(false);
		
		
		ListPreference encoding = (ListPreference)findPreference("ENCODING");
		
		Vector<String> items = new Vector<String>();
		for(Charset set : Charset.availableCharsets().values()) {
			items.add(set.displayName());
		}
		
		String[] array_actual = null;
		if(items.size() > 0) {
			array_actual = new String[items.size()];
			for(int z=0;z<items.size();z++) {
				array_actual[z] = items.get(z);
			}
		}
		
		encoding.setEntries(array_actual);
		encoding.setEntryValues(array_actual);
		
		Preference pimport = findPreference("IMPORT_PATH");
		Preference pexport = findPreference("EXPORT_PATH");
		String exportDir = ConfigurationLoader.getConfigurationValue("exportDirectory", this);
		pimport.setSummary("Import settings from .xml files in /sdcard/"+exportDir+"/");
		pexport.setSummary("Export settings to an .xml file in /sdcard/"+exportDir+"/");
		ListPreference fonts = (ListPreference)findPreference("FONT_NAME");
		//List<String> fontnames = new ArrayList<String>();
		
		HashMap<String,String> fontmap = new HashMap<String,String>();
		
		File temp = new File("/system/fonts/");
		
		FilenameFilter ttf_only = new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".ttf");
			}
			
		};
		
		FilenameFilter xml_only = new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				//return arg1.endsWith(".xml");
				xmlimatcher.reset(arg1);
				if(xmlimatcher.matches()) {
					return true;
				} else {
					return false;
				}
			}
			
		};
		
		File tmp = Environment.getExternalStorageDirectory();
		//String exportDir = ConfigurationLoader.getConfigurationValue("exportDirectory", this);
		File btermdir = new File(tmp,"/"+exportDir+"/");
		
		String sdstate = Environment.getExternalStorageState();
		HashMap<String,String> efonts = new HashMap<String,String>();
		HashMap<String,String> xmlfiles = new HashMap<String,String>();
		if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
			btermdir.mkdirs();
			
			for(File xml : btermdir.listFiles(xml_only)) {
				xmlfiles.put(xml.getName(), xml.getPath());
			}
			
			for(File efont : btermdir.listFiles(ttf_only)) {
				efonts.put(efont.getName(), efont.getPath());
			}
		}
		
		for(File font : temp.listFiles(ttf_only)) {
			//fontnames.add(font.getName());
			fontmap.put(font.getName(),font.getPath());
		}
		
		Set<String> xmlkeys = xmlfiles.keySet();
		List<String> sortedxmlkeys = new ArrayList<String>(xmlkeys);
		Collections.sort(sortedxmlkeys,String.CASE_INSENSITIVE_ORDER);
		
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
		
		
		String[] xmlentries = new String[sortedxmlkeys.size()];
		String[] xmlpaths = new String[sortedxmlkeys.size()];
		i=0;
		for(String file : sortedxmlkeys) {
			xmlentries[i] = file;
			xmlpaths[i] = xmlfiles.get(file);
			i++;
		}
		
		ListPreference xmlfile_list = (ListPreference)findPreference("IMPORT_PATH");
		xmlfile_list.setEntries(xmlentries);
		xmlfile_list.setEntryValues(xmlpaths);
		
		xmlfile_list.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				importexport.sendEmptyMessageDelayed(0, 10);
				return true;
			}
			
		});
		
		EditTextPreference export = (EditTextPreference)findPreference("EXPORT_PATH");
		export.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				
				//EditTextPreference pref = (EditTextPreference)arg0;
				//arg1 = "/BaardTERM/" + pref.getText();
				
				importexport.sendEmptyMessageDelayed(0, 10);
				return true;
			}
			
		});
		
		HyperDialogPreference defaulter = (HyperDialogPreference)findPreference("RESET_DEFAULTS");
		defaulter.setHandler(importexport);
		
		
		//Preference.
	}
	
	public void onStart() {
		super.onStart();
		
		//CheckedEditPreference cep = (CheckedEditPreference)findPreference("TEST_CHECKER");
		//cep.init();
		//TriStatePreference
		//SeekBarPreference preffer = (SeekBarPreference)findPreference("TEST_SEEK");
		//preffer.
		//SeekBar seeker = null;
		//TextView label = null;
	
		//preffer.getContext()
		
		//View tmp = new LinearLayout(this);
		//ViewGroup widget = (ViewGroup) preffer.getContext().getView(tmp, null);//findViewById(preffer.getWidgetLayoutResource());
		//int max = widget.getChildCount();
		//for(int i=0;i<max;i++) {
		//	View v = widget.getChildAt(i);
		//	if(v instanceof SeekBar) {
				//chow down
		//		seeker = (SeekBar)v;
		//	}
		//	if(v instanceof TextView) {
		//		label = (TextView)v;
		//	}
		//}
		//SeekBar seeker = (SeekBar) widget.findViewById(R.id.slider);
		// = (TextView) widget.findViewById(R.id.extra);
		//seeker.setOnSeekBarChangeListener(new SeekerUpdater(label));
		//Log.e("KLSDF","STARTING ONSTART");
	}
	
	public Handler importexport = new Handler() {
		public void handleMessage(Message msg) {
			//we only get one message, so we do the dumpout.
			dumpout();
		}
	};
	
	public void onBackPressed() {
		dumpout();
	}
	
	public void dumpout() {
		Intent retval = new Intent();
		setResult(RESULT_OK,retval);
		finish();
	}
	
	
	
}
