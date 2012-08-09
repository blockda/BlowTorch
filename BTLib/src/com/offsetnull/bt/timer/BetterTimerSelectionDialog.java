package com.offsetnull.bt.timer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.IConnectionBinder;
import com.offsetnull.bt.window.PluginFilterSelectionDialog;
import com.offsetnull.bt.window.BaseSelectionDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView;

public class BetterTimerSelectionDialog extends PluginFilterSelectionDialog implements BaseSelectionDialog.UtilityToolbarListener {

	HashMap<String,TimerData> dataMap;
	String[] sortedKeys;
	
	public BetterTimerSelectionDialog(Context context,
			IConnectionBinder service) {
		super(context, service);
		buildList();
		this.setToolbarListener(this);
		
		this.clearToolbarButtons();
		this.addToolbarButton(R.drawable.toolbar_play_button,0);
		this.addToolbarButton(R.drawable.toolbar_stop_button,1);
		this.addToolbarButton(R.drawable.toolbar_modify_button,2);
		this.addToolbarDeleteButton(R.drawable.toolbar_delete_button,3);
		
		this.setTitle("TIMERS");
	}

	@Override
	public void onButtonPressed(View v, int row, int index) {
		TimerData d = dataMap.get(sortedKeys[row]);
		
		String action = "";
		int icon = 0;
		switch(index) {
		case 0:
			if(d.isPlaying()) {
				icon = R.drawable.toolbar_mini_pause;
				ImageButton b = (ImageButton)v;
				b.setImageResource(R.drawable.toolbar_pause_button);
				try {
					if(currentPlugin.equals(PluginFilterSelectionDialog.MAIN_SETTINGS)) {
						service.pauseTimer(d.getName());
					} else {
						service.pausePluginTimer(d.getName(),currentPlugin);
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				icon = R.drawable.toolbar_mini_play;
				ImageButton b = (ImageButton)v;
				b.setImageResource(R.drawable.toolbar_play_button);
				
				try {
					if(currentPlugin.equals(PluginFilterSelectionDialog.MAIN_SETTINGS)) {
						service.startTimer(d.getName());
					} else {
						service.startPluginTimer(d.getName(),currentPlugin);
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			d.setPlaying(!d.isPlaying());
			action = "play/pause";
			break;
		case 1:
			action = "stop";
			icon = R.drawable.toolbar_mini_stop;
			try {
				if(currentPlugin.equals(PluginFilterSelectionDialog.MAIN_SETTINGS)) {
					service.stopTimer(d.getName());
				} else {
					service.stopPluginTimer(d.getName(),currentPlugin);
				}
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 2:
			action = "mod";
			TimerEditorDialog editor = new TimerEditorDialog(BetterTimerSelectionDialog.this.getContext(),currentPlugin,d,service,triggerEditorDoneHandler);
			editor.show();
			break;
		}
		Log.e("Trigger","timer item selected for "+action+": "+d.getName());
		
		//Log.e("Trigger","trigger item selected for modification: "+d.getName());
		RelativeLayout trow = (RelativeLayout)v.getParent().getParent().getParent();
		((ImageView)trow.findViewById(R.id.icon)).setImageResource(icon);
		
	}

	@Override
	public void onButtonStateChanged(ImageButton v, int row, int index, boolean statea) {
		TimerData d = dataMap.get(sortedKeys[row]);
		//boolean state = !d.isEnabled();
		//d.setEnabled(state);
		//try {
		//	if(currentPlugin.equals(MAIN_SETTINGS)) {
		//		service.setTriggerEnabled(state, d.getName());
		//	} else {
		//		service.setPluginTriggerEnabled(currentPlugin, state, d.getName());
		//	}
		//} catch (RemoteException e) {
		//	
		//}
		/*
		if(state) {
			v.setImageResource(R.drawable.toolbar_toggleon_button);
			this.setItemMiniIcon(row, R.drawable.toolbar_mini_enabled);
		} else {
			v.setImageResource(R.drawable.toolbar_toggleoff_button);
			this.setItemMiniIcon(row, R.drawable.toolbar_mini_disabled);
		}*/
		
	}

	@Override
	public void onItemDeleted(int row) {
		TimerData d = dataMap.get(sortedKeys[row]);
		
		try {
			if(currentPlugin.equals(MAIN_SETTINGS)) {
				service.deleteTrigger(d.getName());
			} else {
				service.deletePluginTrigger(currentPlugin, d.getName());
			}
		} catch (RemoteException e) {
			
		}
		Log.e("Trigger","trigger item selected for delete: "+d.getName());
	}

	@Override
	public void onNewPressed(View v) {
		TimerEditorDialog editor = new TimerEditorDialog(BetterTimerSelectionDialog.this.getContext(),currentPlugin,null,service,triggerEditorDoneHandler);
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
			if(currentPlugin.equals(MAIN_SETTINGS)) {
				dataMap = (HashMap<String, TimerData>) service.getTimers();
			} else {
				dataMap = (HashMap<String, TimerData>) service.getPluginTimers(currentPlugin);
			}
		} catch (RemoteException e) {
			
		}
		
		sortedKeys = new String[dataMap.size()];
		sortedKeys = dataMap.keySet().toArray(sortedKeys);
		Arrays.sort(sortedKeys);
		clearListItems();
		String tag = "";
		for(int i=0;i<sortedKeys.length;i++) {
			TimerData data = dataMap.get(sortedKeys[i]);
			int resource = 0;
			if(data.isPlaying()) {
				resource = R.drawable.toolbar_mini_play;
				tag = " Running.";
			} else {
				if(data.getRemainingTime() != data.getSeconds()) {
					resource = R.drawable.toolbar_mini_pause;
					tag = " Paused, " + data.getRemainingTime() +" seconds remaining.";
				} else {
					resource = R.drawable.toolbar_mini_stop;
					tag = " Stopped.";
				}
			}
			this.addListItem(data.getName(), data.getSeconds() + " Seconds. " + tag,resource, true);
		}
		
		invalidateList();
		
	}
	
	@Override 
	public List<String> getPluginList() throws RemoteException {
		List<String> foo = (List<String>)service.getPluginsWithTimers();
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
		TimerData data = dataMap.get(sortedKeys[row]);
		/*if(data.isEnabled()) {
			b.setImageResource(R.drawable.toolbar_toggleon_button);
		} else {
			b.setImageResource(R.drawable.toolbar_toggleoff_button);
		}*/
	}
	
	@Override
	public void willHideToolbar(LinearLayout toolbar,int row) {
		
	}
	
	private final Handler triggerEditorDoneHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 100:
				TimerData d = (TimerData)msg.obj;
				BetterTimerSelectionDialog.this.buildList();
				BetterTimerSelectionDialog.this.scrollToSelection(d.getName());
				break;
			}
			
		}
	};
	
	

}

