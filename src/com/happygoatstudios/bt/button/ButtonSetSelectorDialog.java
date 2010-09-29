package com.happygoatstudios.bt.button;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.window.BaardTERMWindow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Contacts.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ButtonSetSelectorDialog extends Dialog {

	ArrayList<ButtonEntry> entries = new ArrayList<ButtonEntry>();
	Handler dispater = null;
	String selected_set;
	HashMap<String,Integer> data;
	ConnectionAdapter adapter;
	IStellarService service;
	public ButtonSetSelectorDialog(Context context,Handler reportto,HashMap<String,Integer> datai,String selectedset,IStellarService the_service) {
		super(context);
		// TODO Auto-generated constructor stub
		dispater = reportto;
		selected_set = selectedset;
		data = datai;
		service = the_service;
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		setContentView(R.layout.buttonset_selection_dialog);
		
		ListView lv = (ListView) findViewById(R.id.buttonset_list);

		//build list.
		for(String key : data.keySet()) {
			entries.add(new ButtonEntry(key,data.get(key)));
		}
		adapter = new ConnectionAdapter(lv.getContext(),R.layout.buttonset_selection_list_row,entries);
		adapter.sort(new EntryCompare());
		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);
		
		lv.setSelection(entries.indexOf(new ButtonEntry(selected_set,data.get(selected_set))));
		
		Button newbutton = (Button)findViewById(R.id.new_buttonset_button);
		Button cancel = (Button)findViewById(R.id.cancel_buttonset_button);
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				NewButtonSetEntryDialog diag = new NewButtonSetEntryDialog(ButtonSetSelectorDialog.this.getContext(),dispater);
				diag.setTitle("New Button Set:");
				diag.show();
				ButtonSetSelectorDialog.this.dismiss();
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(setSettingsHaveChanged) {
					//ListView lv = (ListView)ButtonSetSelectorDialog.this.findViewById(R.id.buttonset_list);
					//ButtonEntry item = adapter.getItem(lv.getSelectedItemPosition());
					Message reloadbuttonset = null;
					try {
						reloadbuttonset = dispater.obtainMessage(BaardTERMWindow.MESSAGE_CHANGEBUTTONSET,service.getLastSelectedSet());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					dispater.sendMessage(reloadbuttonset);
				}
				ButtonSetSelectorDialog.this.dismiss();
			}
		});
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ButtonEntry item = entries.get(arg2);
				Message changebuttonset = dispater.obtainMessage(BaardTERMWindow.MESSAGE_CHANGEBUTTONSET,item.name);
				dispater.sendMessage(changebuttonset);
				ButtonSetSelectorDialog.this.dismiss();
			}
			
		});
		
		lv.setOnItemLongClickListener(new ButtonSetEditorOpener());
		
	}
	
	public void onBackPressed() {
		if(setSettingsHaveChanged) {
			//ListView lv = (ListView)ButtonSetSelectorDialog.this.findViewById(R.id.buttonset_list);
			//ButtonEntry item = adapter.getItem(lv.getSelectedItemPosition());
			Message reloadbuttonset = null;
			try {
				reloadbuttonset = dispater.obtainMessage(BaardTERMWindow.MESSAGE_CHANGEBUTTONSET,service.getLastSelectedSet());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dispater.sendMessage(reloadbuttonset);
		}
		this.dismiss();
	}
	
	private class ConnectionAdapter extends ArrayAdapter<ButtonEntry> {

		private List<ButtonEntry> items;
		
		public ConnectionAdapter(Context context, int textViewResourceId,
				List<ButtonEntry> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			this.items = objects;
		}
		
		public View getView(int pos, View convertView,ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.buttonset_selection_list_row,null);
			}
			
			ButtonEntry e = items.get(pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.buttonset_title);
				TextView extra = (TextView)v.findViewById(R.id.buttonset_extra);
				
				label.setText(e.name);
				extra.setText("Contains " + e.entries + " buttons.");
				
				if(e.name.equals(selected_set)) {
					label.setBackgroundColor(0xAA888888);
					extra.setBackgroundColor(0xAA888888);
				} else {
					label.setBackgroundColor(0xAA333333);
					extra.setBackgroundColor(0xAA333333);
				}
			}
			
			return v;
			
		}
		
	}
	
	private class ButtonSetEditorOpener implements ListView.OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			ButtonEntry entry = adapter.getItem(arg2);
			
			ButtonSetEditor editor = new ButtonSetEditor(ButtonSetSelectorDialog.this.getContext(),service,entry.name,editordonelistenr);
			editor.show();
			
			return false;
		}
		
	}
	
	boolean setSettingsHaveChanged = false;
	private Handler editordonelistenr = new Handler() {
		public void handleMessage(Message msg) {
			//handle the thing comin back;
			//if we got this, it means some settings have changed, and we should reload the button set when we are done regardless if it is the one already selected, or cancelled.
			setSettingsHaveChanged = true;
		}
	};
	
	private class EntryCompare implements Comparator<ButtonEntry> {

		public int compare(ButtonEntry a, ButtonEntry b) {
			// TODO Auto-generated method stub
			return a.name.compareToIgnoreCase(b.name);
		}


		
	}
	
	private class ButtonEntry {
		public String name;
		public Integer entries;
		
		public ButtonEntry() {
			name = "";
			entries = 0;
		}
		
		public ButtonEntry(String n,Integer e) {
			name = n;
			entries = e;
		}
		
		public boolean equals(Object test) {
			if(this == test) {
				return true;
			}
			
			if(!(test instanceof ButtonEntry)) {
				return false;
			}
			ButtonEntry bt = (ButtonEntry)test;
			
			boolean retval = true;
			if(!(this.name.equals(bt.name))) retval = false;
			if(this.entries.intValue() != bt.entries.intValue()) retval = false;
			return retval;
		}
	}
	

}
