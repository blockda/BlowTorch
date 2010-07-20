package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TriggerSelectionDialog extends Dialog {

	private ListView list;
	private IStellarService service;
	private List<TriggerItem> entries;
	private TriggerListAdapter adapter;
	
	public TriggerSelectionDialog(Context context,IStellarService the_service) {
		super(context);
		// TODO Auto-generated constructor stub
		service = the_service;
		entries = new ArrayList<TriggerItem>();
	}
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.trigger_selection_dialog);
		
		//initialize the list view
		list = (ListView)findViewById(R.id.trigger_list);
		
		
		//attempt to fish out the trigger list.
		HashMap<String, TriggerData> trigger_list = null;
		try {
			trigger_list = (HashMap<String, TriggerData>) service.getTriggerData();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(TriggerData data : trigger_list.values()) {
			TriggerItem t = new TriggerItem();
			t.name = data.getName();
			t.extra = data.getPattern();
			entries.add(t);
		}
		
		adapter = new TriggerListAdapter(list.getContext(),R.layout.trigger_selection_list_row,entries);
		list.setAdapter(adapter);
		
		
		
	}

	public class TriggerListAdapter extends ArrayAdapter<TriggerItem> {

		List<TriggerItem> entries;
		
		public TriggerListAdapter(Context context,
				int textViewResourceId, List<TriggerItem> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			entries = objects;
		}
		
		public View getView(int pos, View convertView,ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.trigger_selection_list_row,null);
			}
			
			TriggerItem e = entries.get(pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.trigger_title);
				TextView extra = (TextView)v.findViewById(R.id.trigger_extra);
				
				label.setText(e.name);
				extra.setText(e.extra);
				
			}
			
			return v;
			
		}
		
	}
	
	public class TriggerItem {
		String name;
		String extra;
	}
	
	
	
}
