package com.happygoatstudios.bt.button;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.window.MainWindow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
		dispater = reportto;
		selected_set = selectedset;
		data = datai;
		service = the_service;
	}
	
	private boolean noSets = false;
	
	@SuppressWarnings("unchecked")
	public void buildList() {
		entries.clear();
		ListView lv = (ListView) findViewById(R.id.buttonset_list);
		
		try {
			data = (HashMap<String, Integer>) service.getButtonSetListInfo();
			selected_set = service.getLastSelectedSet();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		for(String key : data.keySet()) {
			entries.add(new ButtonEntry(key,data.get(key)));
		}
		
		if(data.size() == 0) {
			noSets = true;
		}
		
		adapter = new ConnectionAdapter(this.getContext(),R.layout.buttonset_selection_list_row,entries);
		adapter.sort(new EntryCompare());
		
		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);
		
		lv.setSelection(entries.indexOf(new ButtonEntry(selected_set,data.get(selected_set))));
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.buttonset_selection_dialog);
		
		
		
		ListView lv = (ListView) findViewById(R.id.buttonset_list);

		lv.setScrollbarFadingEnabled(false);
		
		buildList();
		//build list.
		/*for(String key : data.keySet()) {
			entries.add(new ButtonEntry(key,data.get(key)));
		}
		
		if(data.size() == 0) {
			noSets = true;
		}
		adapter = new ConnectionAdapter(lv.getContext(),R.layout.buttonset_selection_list_row,entries);
		adapter.sort(new EntryCompare());*/
		//lv.setAdapter(adapter);
		//lv.setTextFilterEnabled(true);
		
		//lv.setSelection(entries.indexOf(new ButtonEntry(selected_set,data.get(selected_set))));
		
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
						reloadbuttonset = dispater.obtainMessage(MainWindow.MESSAGE_CHANGEBUTTONSET,service.getLastSelectedSet());
					} catch (RemoteException e) {
						throw new RuntimeException(e);
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
				Message changebuttonset = dispater.obtainMessage(MainWindow.MESSAGE_CHANGEBUTTONSET,item.name);
				dispater.sendMessage(changebuttonset);
				ButtonSetSelectorDialog.this.dismiss();
				
			}
			
		});
		
		lv.setOnItemLongClickListener(new ButtonSetEditorOpener());
		
	}
	
	public void onStart() {
		super.onStart();
		if(noSets) {
			Toast t = Toast.makeText(ButtonSetSelectorDialog.this.getContext(), "No butt sets loaded. Click below to create new Button Sets.", Toast.LENGTH_LONG);
			t.show();
		}
	}
	
	public void onBackPressed() {
		if(setSettingsHaveChanged) {
			//ListView lv = (ListView)ButtonSetSelectorDialog.this.findViewById(R.id.buttonset_list);
			//ButtonEntry item = adapter.getItem(lv.getSelectedItemPosition());
			Message reloadbuttonset = null;
			try {
				reloadbuttonset = dispater.obtainMessage(MainWindow.MESSAGE_CHANGEBUTTONSET,service.getLastSelectedSet());
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			dispater.sendMessage(reloadbuttonset);
		}
		this.dismiss();
	}
	
	private class ModifySetDefaultsListener implements DialogInterface.OnClickListener {

		Integer picked = null;
		public ModifySetDefaultsListener(int input) {
			picked = input;
		}
		
		public void onClick(DialogInterface dialog, int which) {
			ButtonEntry entry = adapter.getItem(picked);
			ButtonSetEditor editor = new ButtonSetEditor(ButtonSetSelectorDialog.this.getContext(),service,entry.name,editordonelistenr);
			editor.show();
		}
		
	}
	
	private class DeleteSetListener implements DialogInterface.OnClickListener {

		Integer picked = null;
		public DeleteSetListener(int input) {
			picked = input;
		}
		
		public void onClick(DialogInterface dialog, int which) {
			AlertDialog.Builder confirm = new AlertDialog.Builder(ButtonSetSelectorDialog.this.getContext());
			
			//default button set can not be deleted.
			if(entries.get(picked).name.equals("default")) {
				confirm.setTitle("Cannot Delete Default Set");			
				confirm.setMessage("This set can not be removed. It can be cleared.");
				//confirm.setPositiveButton("Yes, Delete.",new ReallyDeleteSetListener(picked));
				confirm.setNeutralButton("Clear Buttons", new ClearSetListener(picked));
				confirm.setNegativeButton("Cancel.", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				AlertDialog dlg = confirm.create();
				dlg.show();
				dialog.dismiss();
				ButtonSetSelectorDialog.this.dismiss();				
			} else {
			
				confirm.setTitle("Really Delete Button Set?");			
				confirm.setMessage("The set can be cleared of buttons if desired.");
				confirm.setPositiveButton("Delete",new ReallyDeleteSetListener(picked));
				confirm.setNeutralButton("Clear", new ClearSetListener(picked));
				confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				AlertDialog dlg = confirm.create();
				dlg.show();
				dialog.dismiss();
				ButtonSetSelectorDialog.this.dismiss();
			}
		}
		
	}
	
	private class ReallyDeleteSetListener implements DialogInterface.OnClickListener {

		Integer picked = null;
		public ReallyDeleteSetListener(int input) {
			picked = input;
		}
		
		public void onClick(DialogInterface dialog, int which) {
			Message delset = dispater.obtainMessage(MainWindow.MESSAGE_DELETEBUTTONSET);
			delset.obj = (entries.get(picked)).name;
			dispater.sendMessage(delset);
		}
		
	}
	
	private class ClearSetListener implements DialogInterface.OnClickListener {

		Integer picked = null;
		public ClearSetListener(int input) {
			picked = input;
		}
		
		public void onClick(DialogInterface dialog, int which) {
			Message delset = dispater.obtainMessage(MainWindow.MESSAGE_CLEARBUTTONSET);
			delset.obj = (entries.get(picked)).name;
			dispater.sendMessage(delset);
		}
		
	}
	
	private class ConnectionAdapter extends ArrayAdapter<ButtonEntry> {

		private List<ButtonEntry> items;
		
		public ConnectionAdapter(Context context, int textViewResourceId,
				List<ButtonEntry> objects) {
			super(context, textViewResourceId, objects);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(ButtonSetSelectorDialog.this.getContext());
			builder.setTitle("Edit/Delete Button Set");
			builder.setMessage("Attempting to modify or delete " + entries.get(arg2).name + " button set.");
			builder.setPositiveButton("Edit", new ModifySetDefaultsListener(arg2));
			
			builder.setNeutralButton("Delete", new DeleteSetListener(arg2));
			
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
			return false;
		}
		
	}
	
	boolean setSettingsHaveChanged = false;
	private Handler editordonelistenr = new Handler() {
		public void handleMessage(Message msg) {
			//handle the thing comin back;
			//if we got this, it means some settings have changed, and we should reload the button set when we are done regardless if it is the one already selected, or cancelled.
			setSettingsHaveChanged = true;
			ButtonSetSelectorDialog.this.buildList();
			//Log.e("EDITOR","REBUILDING LIST");
		}
	};
	
	private class EntryCompare implements Comparator<ButtonEntry> {

		public int compare(ButtonEntry a, ButtonEntry b) {
			return a.name.compareToIgnoreCase(b.name);
		}


		
	}
	
	private class ButtonEntry {
		public String name;
		public Integer entries;
		
		//public ButtonEntry() {
		//	name = "";
		//	entries = 0;
		//}
		
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
