package com.happygoatstudios.bt.timer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TimerSelectionDialog extends Dialog {

	private ListView list;
	private IStellarService service;
	private List<TimerItem> entries;
	private TimerListAdapter adapter;
	
	private Handler doneHandler;
	
	public TimerSelectionDialog(Context context,IStellarService the_service) {
		super(context);
		// TODO Auto-generated constructor stub
		service = the_service;
		entries = new ArrayList<TimerItem>();
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.timer_selection_dialog);
		
		list = (ListView)findViewById(R.id.timer_list);
		list.setScrollbarFadingEnabled(false);
		
		//set click listeners.
		doneHandler = new Handler() {
			public void handleMessage(Message msg) {
				//do some stuff;
				buildList();
			}
		};
		
		//build the list
		
		Button newbutton = (Button)findViewById(R.id.timer_new_button);
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//launch the new editor.
				TimerEditorDialog editor = new TimerEditorDialog(TimerSelectionDialog.this.getContext(),null,service,doneHandler);
				editor.show();
			}
		});
		
		Button cancel = (Button)findViewById(R.id.timer_cancel_button);
		
		buildList();
	}
	
	private boolean noTimers = false;
	
	private void buildList() {
		if(adapter != null) {
			adapter.clear();
		}
		
		HashMap<String,TimerData> timer_list = null;
		try{
			timer_list = (HashMap<String,TimerData>)service.getTimers();
		}catch (RemoteException e) {
			e.printStackTrace();
		}
		
		for(TimerData timer : timer_list.values()) {
			TimerItem i = new TimerItem();
			i.name = timer.getName();
			entries.add(i);
		}
		
		if(timer_list.size() == 0) {
			noTimers = true;
		}
		
		adapter = new TimerListAdapter(list.getContext(),R.layout.trigger_selection_list_row,entries);
		list.setAdapter(adapter);
		adapter.sort(new TimerSorter());
	}
	
	public class TimerListAdapter extends ArrayAdapter<TimerItem> {
		List<TimerItem> entries;
		
		public TimerListAdapter(Context context,
				int textViewResourceId, List<TimerItem> objects) {
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
			
			TimerItem e = entries.get(pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.trigger_title);
				TextView extra = (TextView)v.findViewById(R.id.trigger_extra);
				
				label.setText(e.name);
				//extra.setText(e.extra);
				
			}
			
			return v;
			
		}
	}
	
	public class TimerSorter implements Comparator<TimerItem> {

		public int compare(TimerItem a,TimerItem b) {
			if(a.ordinal > b.ordinal) {
				return 1;
			} else if(a.ordinal < b.ordinal){
				return -1;
			} else {
				return 0;
			}
		}
		
	}
	
	public class TimerItem {
		String name;
		int ordinal;
		int timeLeft;
	}
}
