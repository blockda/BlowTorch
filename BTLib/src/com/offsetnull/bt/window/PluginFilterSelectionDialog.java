package com.offsetnull.bt.window;

import java.util.List;

import com.offsetnull.bt.service.IConnectionBinder;

import android.content.Context;
import android.os.RemoteException;
import android.widget.LinearLayout;

public class PluginFilterSelectionDialog extends BaseSelectionDialog implements BaseSelectionDialog.OptionItemClickListener {

	protected IConnectionBinder service;
	public final static String MAIN_SETTINGS = "bt_main_settings";
	protected String currentPlugin = MAIN_SETTINGS;
	
	String[] pluginList;
	
	public PluginFilterSelectionDialog(Context context,IConnectionBinder service) {
		super(context);
		this.service = service;
		setOptionItemClickListener(this);
		try {
			List<String> rawList = this.getPluginList();
			if(rawList == null) return;
			pluginList = new String[rawList.size()];
			pluginList = rawList.toArray(pluginList);
			java.util.Arrays.sort(pluginList);
			//java.util.Arrays.sort(plugins);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.clearOptionItems();
		
		this.addOptionItem("Help", true);
		this.addOptionItem("Enable All", true);
		
		this.addPluginFilterOptions();
	}

	protected void addPluginFilterOptions() {
		if(pluginList.length < 1) { return; }
		this.addOptionDivider("Filter by plugin",false);
		this.addOptionItem("Main", false);
		for(int i=0;i<pluginList.length;i++) {
		
			this.addOptionItem(pluginList[i],false);
		}
	}

	@Override
	public void onOptionItemClicked(int row) {
		// TODO Auto-generated method stub
		switch(row) {
		case 0:
			onHelp();
			break;
		case 1:
			onEnableAll();
			break;
		case 2:
			//divier
			break;
		case 3:
			currentPlugin = MAIN_SETTINGS;
			break;
		default:
			currentPlugin = pluginList[row-4];
			break;
		}
	}
	
	public void onHelp() {
		
	}
	
	public void onEnableAll() {
		
	}
	
	
	
	public List<String> getPluginList() throws RemoteException {
		//List<String> foo = (List<String>)service.getPluginsWithTriggers();
		return null;
	}
	
}


