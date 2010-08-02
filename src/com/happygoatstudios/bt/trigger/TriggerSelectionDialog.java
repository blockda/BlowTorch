package com.happygoatstudios.bt.trigger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.IStellarService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
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
import android.widget.AdapterView.OnItemLongClickListener;

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
		list.setOnItemClickListener(new EditTriggerListener());
		list.setOnItemLongClickListener(new DeleteTriggerListener());
		//list.setOnI
		//attempt to fish out the trigger list.		
		buildList();
		
		Button newbutton = (Button)findViewById(R.id.trigger_new_button);
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
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
	
	private class EditTriggerListener implements AdapterView.OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			TriggerItem entry = adapter.getItem(arg2);
			//launch the trigger editor with this item.
			try {
				//TriggerData data = (TriggerData) (service.getTriggerData()).get(entry.extra);
				TriggerData data = service.getTrigger(entry.extra);
				//Log.e("TSELECTOR","ABOUT TO HAND OFF A TRIGGER TO THE EDITOR, THIS ONE HAS RESPONDERS");
				//for(TriggerResponder responder: data.getResponders() ) {
				//	Log.e("TSELECTOR","has responder " + responder.getType() + " fires " + responder.getFireType());
				//}
				//launch the editor
				TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),data,service,triggerEditorDoneHandler);
				editor.show();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	};
	
	private class DeleteTriggerListener implements OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			//TriggerItem item = entries.get(arg2);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(TriggerSelectionDialog.this.getContext());
			builder.setTitle("Delete Trigger?");
			DeleteTriggerFinishListener delete_me = new DeleteTriggerFinishListener(arg2);
			builder.setPositiveButton("Delete.",delete_me);
			builder.setNegativeButton("Cancel.", delete_me);
			builder.setNeutralButton("Modify", delete_me);
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
			case DialogInterface.BUTTON_POSITIVE:
				//attempt delete
				try {
					service.deleteTrigger(entries.get(position).extra);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buildList();
				arg0.dismiss();
				
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				arg0.dismiss();
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				TriggerItem entry = adapter.getItem(position);
				//launch the trigger editor with this item.
				try {
					TriggerData data = service.getTrigger(entry.extra);

					TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),data,service,triggerEditorDoneHandler);
					editor.show();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
	}
	
	
	private void buildList() {
		if(adapter != null) {
			adapter.clear();
		}
		HashMap<String, TriggerData> trigger_list = null;
		try {
			trigger_list = (HashMap<String, TriggerData>) service.getTriggerData();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	
	public class ItemSorter implements Comparator<TriggerItem>{

		public int compare(TriggerItem a, TriggerItem b) {
			// TODO Auto-generated method stub
			return a.name.compareTo(b.name);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case MESSAGE_MOD_TRIGGER:
				TriggerData from = msg.getData().getParcelable("old");
				TriggerData to = msg.getData().getParcelable("new");
				
				try {
					service.updateTrigger(from, to);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case MESSAGE_DELETE_TRIGGER:
				String which = (String)msg.obj;
				try {
					service.deleteTrigger(which);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	};
	
	
	
}

