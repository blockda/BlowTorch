package com.offsetnull.bt.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.offsetnull.bt.service.IConnectionBinder;

public class BetterPluginSelectionDialog extends StandardSelectionDialog implements BaseSelectionDialog.UtilityToolbarListener,BaseSelectionDialog.OptionItemClickListener, PluginSelectorDialog.OnPluginLoadListener {

	ArrayList<String> items = new ArrayList<String>();
	
	public BetterPluginSelectionDialog(Context context,
			IConnectionBinder service) {
		super(context, service);
		
		this.setToolbarListener(this);
		//on creation, get the list of stuff and prepare the dialog.
		HashMap<String,String> plist = null;
		try {
			plist = (HashMap<String,String>)service.getPluginList();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mListItems.clear();
		//plist.key
		List<String> sortedSet = new ArrayList<String>(plist.keySet());
		Collections.sort(sortedSet,String.CASE_INSENSITIVE_ORDER);
		for(String key : sortedSet) {
			String info = plist.get(key);
			items.add(key);
			this.addListItem(key, info, 0, true);
		}
		
		this.setNewButtonLabel("Load");
		
		this.setTitle("PLUGINS");
	}
	
	
	@Override
	public void onButtonPressed(View v, int row, int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onButtonStateChanged(ImageButton v, int row, int index, boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemDeleted(int row) {
		String plugin = items.remove(row);
		
		try {
			service.deletePlugin(plugin);
			service.saveSettings();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onNewPressed(View v) {
		PluginSelectorDialog loader = new PluginSelectorDialog(v.getContext(),service,this);
		loader.show();
	}

	@Override
	public void onDonePressed(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOptionItemClicked(int row) {
		Log.e("Foo","Option Item " + row + " clicked.");
		this.hideOptionsMenu();
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
	public void onPluginLoad() {
		this.dismiss();
	}

}
