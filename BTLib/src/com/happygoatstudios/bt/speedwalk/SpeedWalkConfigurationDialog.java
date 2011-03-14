package com.happygoatstudios.bt.speedwalk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;

//import android.R;
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

public class SpeedWalkConfigurationDialog extends Dialog implements DirectionEditorDoneListener {

	IStellarService service;
	DirectionAdapter adapter = null;
	
	public SpeedWalkConfigurationDialog(Context context,IStellarService service) {
		super(context);
		this.service = service;
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		this.setContentView(R.layout.speedwalk_dialog);
		
		//build list
		try {
			buildList();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//set up adapter
		ListView lv = (ListView)findViewById(R.id.sw_list);
		lv.setEmptyView(findViewById(R.id.sw_empty));
		
		adapter = new DirectionAdapter(this.getContext(),R.layout.speedwalk_row,theList);
		lv.setAdapter(adapter);
		
		adapter.sort(new SWComparator());
		
		findViewById(R.id.sw_close).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				SpeedWalkConfigurationDialog.this.dismiss();
			}
		});
		
		findViewById(R.id.new_dir_button).setOnClickListener(new NewClickListener());
		
	}
	
	ArrayList<SWEntry> theList = new ArrayList<SWEntry>();
	
	private void buildList() throws RemoteException {
		HashMap<String,DirectionData> map = (HashMap<String, DirectionData>) service.getDirectionData();
		
		theList.clear();
		
		for(DirectionData tmp : map.values()) {
			SWEntry newEntry = new SWEntry();
			newEntry.cmd = tmp.getCommand();
			newEntry.dir = tmp.getDirection();
			theList.add(newEntry);
		}
		
		//notify data set invalidated.
		if(adapter != null) {
			adapter.notifyDataSetChanged();
			adapter.notifyDataSetInvalidated();
			adapter.sort(new SWComparator());
		}
	}
	
	private void saveList() throws RemoteException {
		HashMap<String,DirectionData> tmp = new HashMap<String,DirectionData>();
		
		for(SWEntry entry : theList) {
			DirectionData d = new DirectionData();
			d.setCommand(entry.cmd);
			d.setDirection(entry.dir);
			tmp.put(d.getDirection(), d);
		}
		
		service.setDirectionData(tmp);
		
	}
	
	private class EditClickListener implements View.OnClickListener {
		
		int pos = -1;
		
		public EditClickListener(int pos) {
			this.pos = pos;
		}
		
		public void onClick(View v) {
			SWEntry e = theList.get(pos);
			DirectionData d = new DirectionData(e.dir,e.cmd);
			SpeedWalkDirectionEditorDialog editor = new SpeedWalkDirectionEditorDialog(SpeedWalkConfigurationDialog.this.getContext(),SpeedWalkConfigurationDialog.this,d,service);
			editor.show();
		}
		
	}
	
	private class NewClickListener implements View.OnClickListener {

		public void onClick(View v) {
			SpeedWalkDirectionEditorDialog editor = new SpeedWalkDirectionEditorDialog(SpeedWalkConfigurationDialog.this.getContext(),SpeedWalkConfigurationDialog.this,service);
			editor.show();
		}
		
	}
	
	private class DeleteClickListener implements View.OnClickListener {

		int pos = -1;
		
		public DeleteClickListener(int pos) {
			this.pos = pos;
		}
		
		public void onClick(View v) {
			SpeedWalkConfigurationDialog.this.deleteEntry(pos);
		}
		
	}
	
	private class SWEntry {
		public String dir;
		public String cmd;
	}
	
	private class SWComparator implements Comparator<SWEntry> {

		public int compare(SWEntry a, SWEntry b) {
			// TODO Auto-generated method stub
			return a.dir.compareToIgnoreCase(b.dir);
		}
		
	}
	
	private class DirectionAdapter extends ArrayAdapter<SWEntry> {

		ArrayList<SWEntry> items;
		
		public DirectionAdapter(Context context, int textViewResourceId,
				ArrayList<SWEntry> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			items = objects;
		}
		
		public View getView(int position, View v, ViewGroup parent) {
			
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.speedwalk_row, null);
			}
			
			SWEntry tmp = items.get(position);
			
			if(tmp != null) {
				((TextView)v.findViewById(R.id.dir_str)).setText(tmp.dir + " => " + tmp.cmd);
				v.findViewById(R.id.dir_edit).setOnClickListener(new EditClickListener(position));
				v.findViewById(R.id.dir_del).setOnClickListener(new DeleteClickListener(position));
				
			}
			
			return v;
			
		}
		
	}

	public void editDirection(DirectionData old, DirectionData mod) {
		SWEntry oldEntry = new SWEntry();
		oldEntry.cmd = old.getCommand();
		oldEntry.dir = old.getDirection();
		
		SWEntry newEntry = new SWEntry();
		newEntry.cmd = mod.getCommand();
		newEntry.dir = mod.getDirection();
		
		for(int i=0;i<theList.size();i++) {
			SWEntry tmp = theList.get(i);
			if(tmp.cmd.equals(oldEntry.cmd) && tmp.dir.equals(oldEntry.dir)) {
				theList.remove(i);
				theList.add(i, newEntry);
				i=i+theList.size();
			}
		}
		
		try {
			saveList();
			buildList();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void deleteEntry(int pos) {
		theList.remove(pos);
		
		try {
			saveList();
			buildList();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void newDirection(DirectionData d) {
		SWEntry n = new SWEntry();
		n.cmd = d.getCommand();
		n.dir = d.getDirection();
		theList.add(n);
		
		try {
			saveList();
			buildList();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
