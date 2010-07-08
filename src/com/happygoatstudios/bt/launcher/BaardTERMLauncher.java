package com.happygoatstudios.bt.launcher;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsoluteLayout.LayoutParams;

import com.happygoatstudios.bt.window.SlickButton;
import com.happygoatstudios.bt.window.SlickButtonData;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.settings.*;

public class BaardTERMLauncher extends Activity implements ReadyListener {
	
	public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	
	private ArrayList<MudConnection> connections;
	private BaardTERMLauncher.ConnectionAdapter apdapter;
	
	ListView lv = null;
	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.new_launcher_layout);
		
		connections = new ArrayList<MudConnection>();
		
		//attempt to read the file.
		com.happygoatstudios.bt.settings.HyperSettings settings = new com.happygoatstudios.bt.settings.HyperSettings();
		
		String filename = "test_settings3.xml";
		try {
			FileOutputStream fos = this.openFileOutput(filename,Context.MODE_PRIVATE);
			settings.setMaxLines(5000);
			HashMap<String,String> test_alias = new HashMap<String,String>();
			test_alias.put("FOO", "BAR");
			test_alias.put("SEMI", "semicolon;test");
			test_alias.put("newline", "This\nHas\nNewlines");
			
			HashMap<String,Vector<SlickButtonData>> test_btnholder = new HashMap<String,Vector<SlickButtonData>>();
			HashMap<String,ColorSetSettings> test_settings = new HashMap<String,ColorSetSettings>();
			ColorSetSettings colorsettings = new ColorSetSettings();
			colorsettings.toDefautls();
			Vector<SlickButtonData> test_set = new Vector<SlickButtonData>();
			Vector<SlickButtonData> test_set2 = new Vector<SlickButtonData>();
			SlickButtonData test1 = new SlickButtonData();
			SlickButtonData test2 = new SlickButtonData();
			SlickButtonData test3 = new SlickButtonData();
			SlickButtonData test4 = new SlickButtonData();
			SlickButtonData test5 = new SlickButtonData();
			
			//test1.setDataFromString("40||40||east||EAST||open e||1");
			//test2.setDataFromString("40||125||west||WEST||open w||1");
			//test3.setDataFromString("40||210||north||NORTH||open n||0");
			//test4.setDataFromString("40||295||south||SOUTH||open s||1");
			//test5.setDataFromString("40||380||scan||SCAN||look||2");
			
			test_set.add(test1);
			test_set.add(test2);
			test_set.add(test3);
			
			test_set2.add(test4);
			test_set2.add(test5);
			
			test_btnholder.put("TEST_SET_1", test_set);
			test_btnholder.put("TEST_SET_2", test_set2);
			
			test_settings.put("TEST_SET_1", colorsettings.copy());
			test_settings.put("TEST_SET_2", colorsettings.copy());
			
			
			settings.setSetSettings(test_settings);
			settings.setButtonSets(test_btnholder);
			settings.setAliases(test_alias);
			
			fos.write(com.happygoatstudios.bt.settings.HyperSettings.writeXml2(settings).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		com.happygoatstudios.bt.settings.HyperSAXParser parser = new com.happygoatstudios.bt.settings.HyperSAXParser(filename,this);
		com.happygoatstudios.bt.settings.HyperSettings newSettings = parser.load();
		
		
		
		
		//EasySAXParser parse = new EasySAXParser(filename,this);
		
		//List<com.happygoatstudios.bt.settings.HyperSettings> list = parse.parse();
		
		//com.happygoatstudios.bt.settings.HyperSettings new_settings = list.get(0);
		
		lv = (ListView)findViewById(R.id.connection_list);
		apdapter = new ConnectionAdapter(lv.getContext(),R.layout.connection_row,connections);
		lv.setAdapter(apdapter);
		
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new listItemClicked());
		lv.setOnItemLongClickListener(new listItemLongClicked());
		
		getConnectionsFromDisk();
		
		Button newbutton = (Button)findViewById(R.id.new_connection);
		newbutton.setOnClickListener(new newClickedListener());
		//start the initializeations
		/*setContentView(R.layout.launcher_layout);
		
		
		
		//get the button
		TextView tv = (TextView)findViewById(R.id.welcometext);
		Button b = (Button)findViewById(com.happygoatstudios.bt.R.id.startbutton);
		
		//make an appropriate listener to launch the connection picker dialog.
		b.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ConnectionPickerDialog dialog = new ConnectionPickerDialog(BaardTERMLauncher.this,BaardTERMLauncher.this);
				dialog.show();
			}
		});
		
		tv.setLongClickable(false);*/

		
	}
	
	public void onDestroy() {
		saveConnectionsToDisk();
		super.onDestroy();
	}
	
	private class newClickedListener implements View.OnClickListener {
		public void onClick(View v) {
			//close the dialog for now
			//ConnectionPickerDialog.this.dismiss();
			NewConnectionDialog diag = new NewConnectionDialog(BaardTERMLauncher.this,BaardTERMLauncher.this);
			diag.show();
		}
	}
	
	private class listItemLongClicked implements ListView.OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			Log.e("LAUNCHER","List item long clicked!");
			MudConnection muc = apdapter.getItem(arg2);
			
			
			Message delmsg = connectionModifier.obtainMessage(MSG_DELETECONNECTION);
			delmsg.obj = muc;
			
			Message modmsg = connectionModifier.obtainMessage(MSG_MODIFYCONNECTION);
			modmsg.obj = muc;
			
			AlertDialog.Builder build = new AlertDialog.Builder(BaardTERMLauncher.this)
				.setMessage("Which operation to perform on: " + muc.getDisplayName());
			AlertDialog dialog = build.create();
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Modify", modmsg);
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Delete",delmsg);
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			
			dialog.show();
			return true;
		}
		
	}
	
	private class listItemClicked implements ListView.OnItemClickListener {

		//@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			//ListView lv = (ListView)findViewById(R.id.connectionlist);
			//MudConnection muc = (MudConnection)lv.getSelectedItem();
			//ConnectionAdapter adp = (ConnectionAdapter)arg0.getAdapter();
			//int pos = lv.getSelectedItemPosition();
			Log.e("LAUNCHER","List Item Clicked");
			
			//make the item the first in the list.
			
			
			MudConnection muc = apdapter.getItem(arg2);		
			
			Intent the_intent = new Intent(com.happygoatstudios.bt.window.BaardTERMWindow.class.getName());
	    	
	    	the_intent.putExtra("DISPLAY",muc.getDisplayName());
	    	the_intent.putExtra("HOST", muc.getHostName());
	    	the_intent.putExtra("PORT", muc.getPortString());
	    	
	    	//write out the intent to the service so it can do some lookup work in advance of the connection, such as loading the settings wad
	    	SharedPreferences prefs = BaardTERMLauncher.this.getSharedPreferences("SERVICE_INFO",0);
	    	Editor edit = prefs.edit();
	    	edit.putString("SETTINGS_PATH", muc.getDisplayName() + ".xml");
	    	edit.commit();
	    	
	    	BaardTERMLauncher.this.startActivity(the_intent);
	    	
			//call ready listener
			//saveConnectionsToDisk();
			//reportto.ready(muc.getDisplayName(), muc.getHostName(), muc.getPortString());	
			//ConnectionPickerDialog.this.dismiss();
	    	
	    	
		}
	}
	
	public final int MSG_DELETECONNECTION = 101;
	public final int MSG_MODIFYCONNECTION = 102;
	public Handler connectionModifier = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_DELETECONNECTION:
				MudConnection todelete = (MudConnection)msg.obj;
				apdapter.remove(todelete);
				apdapter.notifyDataSetChanged();
				break;
			case MSG_MODIFYCONNECTION:
				MudConnection tomodify = (MudConnection)msg.obj;
				break;
			default:
				break;
			}
		}
	};

    public void ready(String displayname,String host,String port) {

    	
    	//start window activity.
    	/*Intent the_intent = new Intent(com.happygoatstudios.bt.window.BaardTERMWindow.class.getName());
    	
    	the_intent.putExtra("DISPLAY",displayname);
    	the_intent.putExtra("HOST", host);
    	the_intent.putExtra("PORT", port);
    	
    	this.startActivity(the_intent);*/
    	
    	//dont start, add new
		MudConnection muc = new MudConnection();
		muc.setDisplayName(displayname);
		muc.setHostName(host);
		muc.setPortString(port);
		
		//apdapter.
		
		apdapter.add(muc);
		apdapter.notifyDataSetChanged();
    	
    }
    
	private void getConnectionsFromDisk() {
		SharedPreferences pref = this.getSharedPreferences(PREFS_NAME, 0);
		
		String thestring = pref.getString("STRINGS", "");
		if(thestring == null || thestring == "") { return; }
		
		Pattern connection = Pattern.compile("([^\\|]+)");
		Pattern breakout = Pattern.compile("(.+):(.+):(.+)");
		
		Matcher c_m = connection.matcher(thestring);
		
		while(c_m.find()) {
			String operate = c_m.group(1);
			Matcher o_m = breakout.matcher(operate);
			while(o_m.find()) {
				String displayname = o_m.group(1);
				String hostname = o_m.group(2);
				String portstring = o_m.group(3);
				
				MudConnection muc = new MudConnection();
				muc.setDisplayName(displayname);
				muc.setHostName(hostname);
				muc.setPortString(portstring);
				
				apdapter.add(muc);
			}
		}
		
	}
	
	private void saveConnectionsToDisk() {
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		//build string
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<apdapter.getCount();i++) {
			MudConnection tmp = apdapter.getItem(i);
			buf.append(tmp.getDisplayName() + ":" +tmp.getHostName()+":"+tmp.getPortString());
				buf.append("|");
		}
		
		editor.putString("STRINGS", buf.toString());
		
		editor.commit();
		
	}
    
    
	private class ConnectionAdapter extends ArrayAdapter<MudConnection> {
		private ArrayList<MudConnection> items;
		
		public ConnectionAdapter(Context context, int txtviewresid, ArrayList<MudConnection> objects) {
			super(context, txtviewresid, objects);
			// TODO Auto-generated constructor stub
			this.items = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.connection_row, null);
			}
			
			MudConnection m = items.get(position);
			if(m != null) {
				TextView title = (TextView)v.findViewById(R.id.displayname);
				TextView host = (TextView)v.findViewById(R.id.hoststring);
				//TextView port = (TextView)v.findViewById(R.id.port);
				if(title != null) {
					title.setText(" " + m.getDisplayName());
				}
				if(host != null) {
					host.setText("\t"  + m.getHostName() + ":" + m.getPortString());
				}
				//if(port != null) {
				//	port.setText(" Port: " + m.getPortString());
				//}
			}
			return v;
		}
		
		
	}

}
