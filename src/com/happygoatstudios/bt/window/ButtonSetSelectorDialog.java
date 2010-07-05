package com.happygoatstudios.bt.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	
	public ButtonSetSelectorDialog(Context context,Handler reportto,HashMap<String,Integer> data,String selectedset) {
		super(context);
		// TODO Auto-generated constructor stub
		dispater = reportto;
		selected_set = selectedset;
		
		setContentView(R.layout.buttonset_selection_dialog);
		
		ListView lv = (ListView) findViewById(R.id.buttonset_list);

		//build list.
		for(String key : data.keySet()) {
			entries.add(new ButtonEntry(key,data.get(key)));
		}

		lv.setAdapter(new ConnectionAdapter(lv.getContext(),R.layout.buttonset_selection_list_row,entries));
		lv.setTextFilterEnabled(true);
		
		lv.setSelection(entries.indexOf(new ButtonEntry(selectedset,data.get(selectedset))));
		
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
