package com.offsetnull.bt.alias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.IConnectionBinder;
import com.offsetnull.bt.window.PluginFilterSelectionDialog;
import com.offsetnull.bt.window.BaseSelectionDialog;

public class BetterAliasSelectionDialog extends PluginFilterSelectionDialog implements BaseSelectionDialog.UtilityToolbarListener,AliasEditorDialogDoneListener {

	HashMap<String,AliasData> dataMap;
	String[] sortedKeys;
	
	public BetterAliasSelectionDialog(Context context,
			IConnectionBinder service) {
		super(context, service);
		buildList();
		this.setToolbarListener(this);
		this.setTitle("ALIASES");
	}

	@Override
	public void onButtonPressed(View v, int row, int index) {
		AliasData d = dataMap.get(sortedKeys[row]);
		Log.e("Trigger","trigger item selected for modification: "+d.getPre());

		AliasEditorDialog editor = new AliasEditorDialog(BetterAliasSelectionDialog.this.getContext(),BetterAliasSelectionDialog.this,d.getPre(),d.getPost(),row,d,service,computeNames(d.getPre()),currentPlugin);
		editor.show();
	}

	@Override
	public void onButtonStateChanged(ImageButton v, int row, int index, boolean statea) {
		AliasData d = dataMap.get(sortedKeys[row]);
		boolean state = !d.isEnabled();
		d.setEnabled(state);
		try {
			if(currentPlugin.equals(MAIN_SETTINGS)) {
				service.setAliasEnabled(state, d.getPre());
			} else {
				service.setPluginAliasEnabled(currentPlugin, state, d.getPre());
			}
		} catch (RemoteException e) {
			
		}
		if(state) {
			v.setImageResource(R.drawable.toolbar_toggleon_button);
			this.setItemMiniIcon(row, R.drawable.toolbar_mini_enabled);
		} else {
			v.setImageResource(R.drawable.toolbar_toggleoff_button);
			this.setItemMiniIcon(row, R.drawable.toolbar_mini_disabled);
		}
		Log.e("Alias","alias item selected for enable/disable: "+d.getPre());
	}

	@Override
	public void onItemDeleted(int row) {
		AliasData d = dataMap.get(sortedKeys[row]);
		
		try {
			if(currentPlugin.equals(MAIN_SETTINGS)) {
				service.deleteAlias(d.getPre());
			} else {
				service.deletePluginAlias(currentPlugin, d.getPre());
			}
		} catch (RemoteException e) {
			
		}
		Log.e("Trigger","alias item selected for delete: "+d.getPre());
	}

	@Override
	public void onNewPressed(View v) {
		AliasEditorDialog editor = new AliasEditorDialog(BetterAliasSelectionDialog.this.getContext(),BetterAliasSelectionDialog.this,service,computeNames(""),currentPlugin);
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
	public void onHelp() {
		Log.e("Error","Help pressed.");
	}
	
	@Override
	public void onEnableAll() {
		Log.e("Error","Enable All pressed.");
	}
	
	private void buildList() {
		//HashMap<String,TriggerData> list = null;
		//pull the list down, clear out the items list, populate it, and call the superclass to reload the table.
		try {
			if (currentPlugin.equals(MAIN_SETTINGS)) {
				dataMap = (HashMap<String, AliasData>) service.getAliases();
			} else {
				dataMap = (HashMap<String, AliasData>) service.getPluginAliases(currentPlugin);
			}
		} catch (RemoteException e) {
			
		}
		
		sortedKeys = new String[dataMap.size()];
		
		sortedKeys = dataMap.keySet().toArray(sortedKeys);
		//for(int i = 0;i<sortedKeys.length;i++) {
		//	String key = sortedKeys[i];
			//if(key.startsWith("^")) { key = key.substring(1,key.length()); }
			//if(key.endsWith("$")) { key = key.substring(0, key.length()-1); }
		//	sortedKeys[i] = key;
		//}
		Arrays.sort(sortedKeys,String.CASE_INSENSITIVE_ORDER);
		clearListItems();
		for(int i=0;i<sortedKeys.length;i++) {
			AliasData data = dataMap.get(sortedKeys[i]);
			int resource = 0;
			if(data.isEnabled()) {
				resource = R.drawable.toolbar_mini_enabled;
			} else {
				resource = R.drawable.toolbar_mini_disabled;
			}
			//String name = data.getPre();
			//if(name.startsWith("^")) { name = name.substring(1,name.length()); }
			//if(name.endsWith("$")) { name = name.substring(0, name.length()-1); }
			
			this.addListItem(sortedKeys[i], data.getPost(),resource, data.isEnabled());
		}
		
		invalidateList();
		
	}
	
	@Override 
	public List<String> getPluginList() throws RemoteException {
		List<String> foo = (List<String>)service.getPluginsWithAliases();
		return foo;
	}
	
	@Override
	public void onOptionItemClicked(int row) {
		super.onOptionItemClicked(row);
		this.hideOptionsMenu();
		if(row < 3) return;
		buildList();	
		
	}

	@Override
	public void willShowToolbar(LinearLayout toolbar, int row) {
		//this will be called before the toolbar is shown, it will give the implementer the option to set up on/off lock/unlock etc.
		ImageButton b = (ImageButton)toolbar.getChildAt(1);
		toolbar.getChildAt(1);
		AliasData data = dataMap.get(sortedKeys[row]);
		if(data.isEnabled()) {
			b.setImageResource(R.drawable.toolbar_toggleon_button);
		} else {
			b.setImageResource(R.drawable.toolbar_toggleoff_button);
		}
	}
	
	private final Handler aliasEditorDoneHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 100:
				AliasData d = (AliasData)msg.obj;
				BetterAliasSelectionDialog.this.buildList();
				BetterAliasSelectionDialog.this.scrollToSelection(d.getPre());
				break;
			}
			
		}
	};
	private ArrayList<String> names = new ArrayList<String>();
	
	private List<String> computeNames(String name) {
		
		names.clear(); 
		
		if(name.startsWith("^")) name = name.substring(1,name.length());
		if(name.endsWith("$")) name = name.substring(0,name.length()-1);
		
		if(dataMap != null) {
			for(String key : dataMap.keySet()) {
				if(!key.equals(name)) {
					names.add(key);
				}
			}
		}
		
		
		/*for(int i=0;i<apdapter.getCount();i++) {
			if(!apdapter.getItem(i).pre.equals(name)) {
				//Log.e("FLOOP","COMPUTED " + apdapter.getItem(i).pre + " AS INVALID");
				names.add(apdapter.getItem(i).pre);
			}
		}*/
		
		return names;
	}
	
	public void newAliasDialogDone(String pre, String post,boolean enabled) {

		
		/****
		 * 
		 * CHECK FOR CIRCULUAR REFERENCES
		 * 
		 */

		/*lastSelectedIndex = -1;
		if(theToolbar.getParent() != null) {
			((RelativeLayout)theToolbar.getParent()).removeView(theToolbar);
		}
		AliasEntry tmp = new AliasEntry(pre,post,enabled);
		apdapter.add(tmp);
		apdapter.notifyDataSetChanged();
		apdapter.sort(new AliasComparator());*/
		
		try {
			/*HashMap<String,AliasData> existingAliases = null;
			if(currentPlugin.equals("main")) {
				existingAliases =(HashMap<String, AliasData>) service.getAliases();
			} else {
				existingAliases =(HashMap<String, AliasData>) service.getPluginAliases(currentPlugin);
			}*/
			
			AliasData newAlias = new AliasData();
			newAlias.setPost(post);
			newAlias.setPre(pre);
			newAlias.setEnabled(enabled);
			String newKey = newAlias.getPre();
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			
			dataMap.put(newKey, newAlias);
			if(currentPlugin.equals(MAIN_SETTINGS)) {
				service.setAliases(dataMap);
			} else {
				service.setPluginAliases(currentPlugin,dataMap);
			}
			
			aliasEditorDoneHandler.sendMessageDelayed(aliasEditorDoneHandler.obtainMessage(100,newAlias),10);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	public void editAliasDialogDone(String pre,String post,boolean enabled,int pos,AliasData orig) {
		/*lastSelectedIndex = -1;
		if(theToolbar.getParent() != null) {
			((RelativeLayout)theToolbar.getParent()).removeView(theToolbar);
		}
		
		apdapter.remove(apdapter.getItem(pos));
		AliasEntry tmp = new AliasEntry(pre,post,enabled);
		apdapter.insert(tmp,pos);
		apdapter.notifyDataSetChanged();
		apdapter.sort(new AliasComparator());
		pos = apdapter.getPosition(tmp);*/
		
		
		
		//remove from the list and add the new one.
		try {
			
			String oldKey = orig.getPre();
			if(oldKey.startsWith("^")) oldKey = oldKey.substring(1,oldKey.length());
			if(oldKey.endsWith("$")) oldKey = oldKey.substring(0,oldKey.length()-1);
			dataMap.remove(oldKey);
			
			String newKey = pre;
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			AliasData newAlias = new AliasData();
			newAlias.setPre(pre);
			newAlias.setPost(post);
			newAlias.setEnabled(enabled);
			dataMap.put(newKey, newAlias);
			if(currentPlugin.equals(MAIN_SETTINGS)) {
				service.setAliases(dataMap);
			} else {
				service.setPluginAliases(currentPlugin,dataMap);
			}
			
			aliasEditorDoneHandler.sendMessageDelayed(aliasEditorDoneHandler.obtainMessage(100,newAlias),10);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void willHideToolbar(LinearLayout v, int row) {
		// TODO Auto-generated method stub
		
	}

}
