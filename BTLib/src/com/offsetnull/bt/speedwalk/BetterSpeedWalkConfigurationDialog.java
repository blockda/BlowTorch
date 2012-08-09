package com.offsetnull.bt.speedwalk;

import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.IConnectionBinder;
import com.offsetnull.bt.window.StandardSelectionDialog;
import com.offsetnull.bt.window.BaseSelectionDialog;

//public class BetterPluginSelectionDialog extends StandardSelectionDialog implements BaseSelectionDialog.UtilityToolbarListener,BaseSelectionDialog.OptionItemClickListener {

public class BetterSpeedWalkConfigurationDialog extends StandardSelectionDialog implements BaseSelectionDialog.UtilityToolbarListener,DirectionEditorDoneListener {

	
	HashMap<String,DirectionData> dataMap;
	String[] sortedKeys;
	
	public BetterSpeedWalkConfigurationDialog(Context context,
			IConnectionBinder service) {
		super(context, service);
		// TODO Auto-generated constructor stub
		
		
		buildList();
		this.setToolbarListener(this);
		
		this.clearToolbarButtons();

		this.addToolbarButton(R.drawable.toolbar_modify_button,0);
		this.addToolbarDeleteButton(R.drawable.toolbar_delete_button,1);
		
		this.setTitle("DIRECTIONS");
	}
	
	private void buildList() {
		
		try {
			dataMap = (HashMap<String, DirectionData>) service.getDirectionData();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sortedKeys = new String[dataMap.size()];
		sortedKeys = dataMap.keySet().toArray(sortedKeys);
		Arrays.sort(sortedKeys);
		clearListItems();
		String tag = "";
		for(int i=0;i<sortedKeys.length;i++) {
			DirectionData data = dataMap.get(sortedKeys[i]);
			int resource = 0;
			
			this.addListItem(data.getDirection(), "Command: " + data.getCommand(),resource, true);
		}
		
		invalidateList();
	}
	
	private void saveList() {
		
		try {
			service.setDirectionData(dataMap);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onButtonPressed(View v, int row, int index) {
		String entry = sortedKeys[row];
		DirectionData d = dataMap.get(entry);
		SpeedWalkDirectionEditorDialog editor = new SpeedWalkDirectionEditorDialog(BetterSpeedWalkConfigurationDialog.this.getContext(),BetterSpeedWalkConfigurationDialog.this,d,service);
		editor.show();
	}

	@Override
	public void onButtonStateChanged(ImageButton v, int row, int index,
			boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemDeleted(int row) {
		String key = sortedKeys[row];
		dataMap.remove(key);
		
		saveList();
		buildList();
	}

	@Override
	public void onNewPressed(View v) {
		SpeedWalkDirectionEditorDialog editor = new SpeedWalkDirectionEditorDialog(BetterSpeedWalkConfigurationDialog.this.getContext(),BetterSpeedWalkConfigurationDialog.this,service);
		editor.show();
	}

	@Override
	public void onDonePressed(View v) {
		try {
			service.saveSettings();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void willShowToolbar(LinearLayout v, int row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void willHideToolbar(LinearLayout v, int row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newDirection(DirectionData d) {
		dataMap.put(d.getDirection(), d);
		//buildList();
		saveList();
		buildList();
	}

	@Override
	public void editDirection(DirectionData old, DirectionData mod) {
		dataMap.remove(old.getDirection());
		dataMap.put(mod.getDirection(),mod);
		saveList();
		buildList();
	}

}
