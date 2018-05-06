package com.happygoatstudios.bt.launcher;

import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

import com.happygoatstudios.bt.launcher.MudConnection;
import com.happygoatstudios.bt.R;

public class ConnectionPickerDialog extends Dialog implements ReadyListener {
	ListView lv = null;
	public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	
	private ArrayList<MudConnection> connections;
	private ConnectionAdapter apdapter;
	
	ReadyListener reportto =  null;

	public ConnectionPickerDialog(Context context,ReadyListener useme) {
		super(context);
		reportto=useme;
		//launch the dialog.		
	}
	
	public void ready(String displayname,String host,String port) {
		MudConnection muc = new MudConnection();
		muc.setDisplayName(displayname);
		muc.setHostName(host);
		muc.setPortString(port);
		
		apdapter.add(muc);
		apdapter.notifyDataSetChanged();
	}
	
	private void getConnectionsFromDisk() {
		SharedPreferences pref = this.getContext().getSharedPreferences(PREFS_NAME, 0);
		
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
		SharedPreferences prefs = this.getContext().getSharedPreferences(PREFS_NAME,0);
		
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
	
	private void populateList() {
		//create an entry;
		MudConnection tmp = new MudConnection();
		tmp.setDisplayName("AARDWOLF");
		tmp.setHostName("aardmud.org");
		tmp.setPortString("4000");
		
		//apdapter.add(tmp);
		//apdapter.notifyDataSetChanged();
		connections.add(tmp);
		
		MudConnection tmp2 = new MudConnection();
		tmp2.setDisplayName("AARDWOLF - SHORT LOGIN");
		tmp2.setHostName("aardmud.org");
		tmp2.setPortString("4010");
		
		//apdapter.add(tmp2);
		//apdapter.notifyDataSetChanged();
		connections.add(tmp2);
		
		for(int i=0;i<10;i++) {
			MudConnection mc = new MudConnection();
			mc.setDisplayName("TESTING: " + (new Integer(i).toString()));
			mc.setHostName("TEST");
			mc.setPortString("9999");
			connections.add(mc);
		}
	}
	
	private void notifyList() {
		
        /*if(connections != null && connections.size() > 0){
        	//apdapter.clear();
            apdapter.notifyDataSetChanged();
            int size = connections.size();
            for(int i=0;i<size;i++)
            	apdapter.add(connections.get(i));
        }
		//apdapter.add();
		
		apdapter.notifyDataSetChanged();*/
	}
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.conpickerdialog);
		
		Button newbutton = (Button)findViewById(R.id.newbutton);
		
		newbutton.setOnClickListener(new newClickedListener());
		
		this.setTitle("Please Select A Connection...");
		
		connections = new ArrayList<MudConnection>();
		
		
		lv = (ListView)findViewById(R.id.connectionlist);
		
		//populateList();
		
		apdapter = new ConnectionAdapter(lv.getContext(),R.layout.connection_row,connections);
		
		lv.setAdapter(apdapter);
		//lv.setOnClickListener(new listItemClicked());
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new listItemClicked());
		
		//load list
		getConnectionsFromDisk();
		

		//notifyList();
		
		
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
			MudConnection muc = apdapter.getItem(arg2);
			//call ready listener
			saveConnectionsToDisk();
			reportto.ready(muc.getDisplayName(), muc.getHostName(), muc.getPortString());	
			ConnectionPickerDialog.this.dismiss();
		}
	}
	
	private class newClickedListener implements View.OnClickListener {
		public void onClick(View v) {
			//close the dialog for now
			//ConnectionPickerDialog.this.dismiss();
			NewConnectionDialog diag = new NewConnectionDialog(ConnectionPickerDialog.this.getContext(),ConnectionPickerDialog.this);
			diag.show();
		}
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
				TextView host = (TextView)v.findViewById(R.id.hostname);
				TextView port = (TextView)v.findViewById(R.id.port);
				if(title != null) {
					title.setText(m.getDisplayName());
				}
				if(host != null) {
					host.setText("Host: " + m.getHostName());
				}
				if(port != null) {
					port.setText(" Port: " + m.getPortString());
				}
			}
			return v;
		}
		
		
	}

}
