package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class TriggerSelectionDialog extends Dialog {

	private ListView list;
	private IStellarService service;
	private List<TriggerItem> entries;
	private TriggerListAdapter adapter;
	
	public TriggerSelectionDialog(Context context,IStellarService the_service) {
		super(context);
		service = the_service;
		entries = new ArrayList<TriggerItem>();
	}
	
	//private boolean noTriggers = false;
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.trigger_selection_dialog);
		
		//initialize the list view
		list = (ListView)findViewById(R.id.trigger_list);
		
		list.setScrollbarFadingEnabled(false);
		
		list.setOnItemClickListener(new EditTriggerListener());
		list.setOnItemLongClickListener(new DeleteTriggerListener());
		//list.setOnI
		//attempt to fish out the trigger list.	
		
		list.setEmptyView(findViewById(R.id.trigger_empty));
		buildList();
		
		Button newbutton = (Button)findViewById(R.id.trigger_new_button);
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),null,service,triggerEditorDoneHandler);
				editor.show();
			}
		});
		
		
		Button cancelbutton = (Button)findViewById(R.id.trigger_cancel_button);
		
		cancelbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				TriggerSelectionDialog.this.dismiss();
			}
			
		});
		
		
	}
	
	public void onStart() {
		/*if(noTriggers) {
			Toast t = Toast.makeText(TriggerSelectionDialog.this.getContext(), "No triggers loaded. Click below to create new Triggers.", Toast.LENGTH_LONG);
			t.show();
		}*/
	}
	
	private class EditTriggerListener implements AdapterView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			TriggerItem entry = adapter.getItem(arg2);
			//launch the trigger editor with this item.
			try {
				//TriggerData data = (TriggerData) (service.getTriggerData()).get(entry.extra);
				TriggerData data = service.getTrigger(entry.extra);
				TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),data,service,triggerEditorDoneHandler);
				editor.show();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
		
	};
	
	private class DeleteTriggerListener implements OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			AlertDialog.Builder builder = new AlertDialog.Builder(TriggerSelectionDialog.this.getContext());
			builder.setTitle("Edit Trigger?");
			DeleteTriggerFinishListener delete_me = new DeleteTriggerFinishListener(arg2);
			builder.setNeutralButton("Delete",delete_me);
			builder.setNegativeButton("Cancel", delete_me);
			builder.setPositiveButton("Edit", delete_me);
			AlertDialog dialog = builder.create();
			dialog.show();
			
			return true;
		}
		
	}
	
	private class DeleteTriggerFinishListener implements DialogInterface.OnClickListener {

		private int position;
		
		public DeleteTriggerFinishListener(int i) {
			position = i;
		}
		
		public void onClick(DialogInterface arg0, int arg1) {
			switch(arg1) {
			case DialogInterface.BUTTON_NEUTRAL:
				//attempt delete
				try {
					service.deleteTrigger(entries.get(position).extra);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				buildList();
				arg0.dismiss();
				
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				arg0.dismiss();
				break;
			case DialogInterface.BUTTON_POSITIVE:
				TriggerItem entry = adapter.getItem(position);
				//launch the trigger editor with this item.
				try {
					TriggerData data = service.getTrigger(entry.extra);

					TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),data,service,triggerEditorDoneHandler);
					editor.show();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void buildList() {
		if(adapter != null) {
			adapter.clear();
		}
		HashMap<String, TriggerData> trigger_list = null;
		try {
			trigger_list = (HashMap<String, TriggerData>) service.getTriggerData();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		for(TriggerData data : trigger_list.values()) {
			if(!data.isHidden()) {
				TriggerItem t = new TriggerItem();
				t.name = data.getName();
				t.extra = data.getPattern();
				entries.add(t);
			}
		}
		
		adapter = new TriggerListAdapter(list.getContext(),R.layout.trigger_selection_list_row,entries);
		list.setAdapter(adapter);
		adapter.sort(new ItemSorter());
	}
	
	private Handler triggerEditorDoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			//refresh the list because it's done.
			buildList();
		}
	};

	public class TriggerListAdapter extends ArrayAdapter<TriggerItem> {

		List<TriggerItem> entries;
		
		public TriggerListAdapter(Context context,
				int textViewResourceId, List<TriggerItem> objects) {
			super(context, textViewResourceId, objects);
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
	
	
	public class ItemSorter implements Comparator<TriggerItem>{

		public int compare(TriggerItem a, TriggerItem b) {
			return a.name.compareToIgnoreCase(b.name);
		}
		
	}
	
	public class TriggerItem {
		String name;
		String extra;
	}
	
	public static final int MESSAGE_NEW_TRIGGER = 100;
	public static final int MESSAGE_MOD_TRIGGER = 101;
	public static final int MESSAGE_DELETE_TRIGGER = 102;
	
	public Handler triggerModifier = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_NEW_TRIGGER:
				TriggerData tmp = (TriggerData)msg.obj;
				//attempt to modify service
				try {
					service.newTrigger(tmp);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case MESSAGE_MOD_TRIGGER:
				TriggerData from = msg.getData().getParcelable("old");
				TriggerData to = msg.getData().getParcelable("new");
				
				try {
					service.updateTrigger(from, to);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				break;
			case MESSAGE_DELETE_TRIGGER:
				String which = (String)msg.obj;
				try {
					service.deleteTrigger(which);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				break;
			}
		}
	};
	
	
	
}

