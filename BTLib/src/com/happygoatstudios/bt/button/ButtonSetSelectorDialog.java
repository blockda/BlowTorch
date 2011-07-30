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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewFlipper;

public class ButtonSetSelectorDialog extends Dialog {

	ArrayList<ButtonEntry> entries = new ArrayList<ButtonEntry>();
	Handler dispater = null;
	String selected_set;
	HashMap<String,Integer> data;
	ConnectionAdapter adapter;
	IStellarService service;
	ListView list = null;
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
			ButtonEntry tmp = new ButtonEntry(key,data.get(key));
			try {
				tmp.locked = service.isButtonSetLocked(key);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			entries.add(tmp);
		}
		
		if(data.size() == 0) {
			noSets = true;
		}
		
		adapter = new ConnectionAdapter(this.getContext(),R.layout.buttonset_selection_list_row,entries);
		adapter.sort(new EntryCompare());
		
		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);
		
		//Button
		//lv.setSelection(entries.indexOf(new ButtonEntry(selected_set,data.get(selected_set))));
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
				NewButtonSetEntryDialog diag = new NewButtonSetEntryDialog(ButtonSetSelectorDialog.this.getContext(),dispater,service);
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
		
		/*lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				
				ButtonEntry item = entries.get(arg2);
				Message changebuttonset = dispater.obtainMessage(MainWindow.MESSAGE_CHANGEBUTTONSET,item.name);
				dispater.sendMessage(changebuttonset);
				ButtonSetSelectorDialog.this.dismiss();
				
			}
			
		});*/
		
		//lv.setOnItemLongClickListener(new ButtonSetEditorOpener());
		lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				arg0.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
				for(int i = 0;i<adapter.getCount();i++) {
					int first = arg0.getFirstVisiblePosition();
					int last = arg0.getLastVisiblePosition();
					int index = i;
					boolean dostuff = false;
					if(first <= index && index <= last) {
						index = index - first;
						dostuff = true;
					} else if (index >= last) {
						//dont care.
					} 
					//if(arg0.getChildAt(i) != null) {
					if(dostuff) {
						arg0.getChildAt(index).findViewById(R.id.toolbar_tab).setFocusable(false);
						//arg0.getChildAt(i).findViewById(R.id.toolbar_tab).s(false);
						if(i==arg2) {
							arg0.getChildAt(index).findViewById(R.id.toolbar_tab).setFocusable(true);
						}
					}
					//}
					
				}
				lastSelectedIndex = arg2;
				arg1.findViewById(R.id.toolbar_tab).requestFocus();
				
				//Log.e("LIST","SELECTED ELEMENT:" + arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
				//Log.e("LIST","NOTHING SELECTED");
				
			}
		});
		lv.setOnFocusChangeListener(new ListFocusFixerListener());
		list = lv;
		
	}
	
	public class ListFocusFixerListener implements View.OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) {
				for(int i=0;i<adapter.getCount();i++) {
					View view = list.getChildAt(i);
					if(view != null)  {
						view.findViewById(R.id.toolbar_tab).setFocusable(false);
					}
				}
				if(lastSelectedIndex < 0) {
					
				} else {
					//Log.e("LIST","SETTING FOCUS ON:" + lastSelectedIndex);
					int index = lastSelectedIndex;
					int first = list.getFirstVisiblePosition();
					int last = list.getLastVisiblePosition();
					if(first <= index && index <= last) {
						index = index - first;
					} else {
						index = list.getFirstVisiblePosition();
					}
					list.setSelection(lastSelectedIndex);
					list.getChildAt(index).findViewById(R.id.toolbar_tab).setFocusable(true);
					list.getChildAt(index).findViewById(R.id.toolbar_tab).requestFocus();
				}
				
			}
			//Log.e("FOCUS","FOCUS CHANGE LISTENER FIRE, focus is " + hasFocus);
		}
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
				v = li.inflate(R.layout.better_list_row,null);
			}
			
			ButtonEntry e = items.get(pos);
			
			if(e != null) {
				
				//set up the view
				RelativeLayout root = (RelativeLayout)v.findViewById(R.id.root);
				root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
				
				v.findViewById(R.id.spacer).setVisibility(View.INVISIBLE);
				
				ImageView iv = (ImageView) v.findViewById(R.id.icon);
				if(e.locked) {
					iv.setImageResource(R.drawable.toolbar_mini_locked);
					iv.setVisibility(View.VISIBLE);
				} else {
					iv.setVisibility(View.INVISIBLE);
				}
				
				ImageView icon = (ImageView) v.findViewById(R.id.icon);
				if(e.locked) {
					icon.setImageResource(R.drawable.toolbar_mini_locked);
					icon.setVisibility(View.VISIBLE);
				} else {
					icon.setVisibility(View.INVISIBLE);
				}
				
				ImageButton load = new ImageButton(ButtonSetSelectorDialog.this.getContext());
				ImageButton lock = new ImageButton(ButtonSetSelectorDialog.this.getContext());
				ImageButton modify = new ImageButton(ButtonSetSelectorDialog.this.getContext());
				ImageButton delete = new ImageButton(ButtonSetSelectorDialog.this.getContext());
				
				LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				params.setMargins(0, 0, 0, 0);
				
				load.setLayoutParams(params);
				lock.setLayoutParams(params);
				modify.setLayoutParams(params);
				delete.setLayoutParams(params);
				
				load.setPadding(0,0,0,0);
				lock.setPadding(0,0,0,0);
				modify.setPadding(0,0,0,0);
				delete.setPadding(0,0,0,0);
				
				load.setImageResource(R.drawable.toolbar_load_button);
				if(e.locked) {
					lock.setImageResource(R.drawable.toolbar_locked_button);
				} else {
					lock.setImageResource(R.drawable.toolbar_unlocked_button);
				}
				modify.setImageResource(R.drawable.toolbar_modify_button);
				delete.setImageResource(R.drawable.toolbar_delete_button);
				
				load.setBackgroundColor(0);
				lock.setBackgroundColor(0);
				modify.setBackgroundColor(0);
				delete.setBackgroundColor(0);
				
				load.setOnKeyListener(theButtonKeyListener);
				lock.setOnKeyListener(theButtonKeyListener);
				modify.setOnKeyListener(theButtonKeyListener);
				delete.setOnKeyListener(theButtonKeyListener);
				
				LinearLayout holder = (LinearLayout)v.findViewById(R.id.button_holder);
				holder.removeAllViews();
				holder.addView(load);
				holder.addView(lock);
				holder.addView(modify);
				holder.addView(delete);
				
				int width = load.getDrawable().getIntrinsicWidth() + lock.getDrawable().getIntrinsicWidth() + modify.getDrawable().getIntrinsicWidth() + delete.getDrawable().getIntrinsicWidth();
				
				load.setOnClickListener(new LoadButtonListener(pos));
				lock.setOnClickListener(new LockButtonListener(pos,icon));
				modify.setOnClickListener(new ModifyButtonListener(pos));
				delete.setOnClickListener(new DeleteButtonListener(pos,(ViewFlipper)v.findViewById(R.id.flipper),width));
				
				v.findViewById(R.id.toolbar_tab).setOnClickListener(new ToolbarTabOpenListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,pos));
				
				v.findViewById(R.id.toolbar_tab_close).setOnClickListener(new ToolbarTabCloseListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,v.findViewById(R.id.toolbar_tab)));
				v.findViewById(R.id.toolbar_tab_close).setOnKeyListener(theButtonKeyListener);
				
				v.findViewById(R.id.toolbar_tab).setOnFocusChangeListener(new View.OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus) {
							v.setFocusable(true);
							v.setFocusableInTouchMode(true);
						} else {
							v.setFocusable(false);
							v.setFocusableInTouchMode(false);
						}
					}
				});
				
				TextView label = (TextView)v.findViewById(R.id.infoTitle);
				TextView extra = (TextView)v.findViewById(R.id.infoExtended);
				
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
	
	public class LoadButtonListener implements View.OnClickListener {

		private int index = -1;
		public LoadButtonListener(int index) {
			this.index = index;
		}
		
		@Override
		public void onClick(View v) {
			ButtonEntry item = entries.get(index);
			Message changebuttonset = dispater.obtainMessage(MainWindow.MESSAGE_CHANGEBUTTONSET,item.name);
			dispater.sendMessage(changebuttonset);
			ButtonSetSelectorDialog.this.dismiss();
		}
		
	}
	
	public class LockButtonListener implements View.OnClickListener {
		private int index = -1;
		private ImageView icon = null;
		public LockButtonListener(int index,ImageView icon) {
			this.index = index;
			this.icon = icon;
		}
		@Override
		public void onClick(View v) {
			ButtonEntry item = entries.get(index);
			//TODO: actually lock the set.
			
			if(item.locked) {
				//unlock
				try {
					service.setButtonSetLocked(false, item.name);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ImageView iv = (ImageView)v;
				iv.setImageResource(R.drawable.toolbar_unlocked_button);
				icon.setVisibility(View.INVISIBLE);
				item.locked = false;
			} else {
				//lock
				try {
					service.setButtonSetLocked(true, item.name);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ImageView iv = (ImageView)v;
				iv.setImageResource(R.drawable.toolbar_locked_button);
				icon.setVisibility(View.VISIBLE);
				icon.setImageResource(R.drawable.toolbar_mini_locked);
				item.locked = true;
			}
		}
		
	}
	
	public class ModifyButtonListener implements View.OnClickListener {
		private int index = -1;
		public ModifyButtonListener(int index) {
			this.index = index;
		}
		
		@Override
		public void onClick(View v) {
			ButtonEntry entry = adapter.getItem(index);
			ButtonSetEditor editor = new ButtonSetEditor(ButtonSetSelectorDialog.this.getContext(),service,entry.name,editordonelistenr);
			editor.show();
		}
		
	}
	
	public class DeleteButtonListener implements View.OnClickListener {

		private int entry = -1;
		ViewFlipper flip = null;
		private int animateDistance = 0;
		public DeleteButtonListener(int element,ViewFlipper flip,int animateDistance) {
			this.entry = element;
			this.flip = flip;
			this.animateDistance = animateDistance;
		}
		
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ButtonSetSelectorDialog.this.getContext());
			builder.setTitle("Delete Button Set");
			builder.setMessage("Confirm Delete?");
			builder.setPositiveButton("Delete", new ReallyDeleteTriggerListener(flip,animateDistance,entry));
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			AlertDialog d = builder.create();
			d.show();
		}
		
	}
	
	public class ReallyDeleteTriggerListener implements DialogInterface.OnClickListener {
		ViewFlipper flip = null;
		int animateDistance = 0;
		int entry = -1;
		public ReallyDeleteTriggerListener(ViewFlipper flip,int animateDistance,int entry) {
			this.flip = flip;
			this.animateDistance = animateDistance;
			this.entry = entry;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			Animation a = new TranslateAnimation(0, animateDistance, 0, 0);
			a.setDuration(800);
			a.setAnimationListener(new DeleteAnimationListener(entry));
			//list.setOnFocusChangeListener(null);
			//list.setFocusable(false);
			flip.setOutAnimation(a);
			flip.showNext();
		}
		
	}
	
	public class DeleteAnimationListener implements Animation.AnimationListener {

		int entry = -1;
		public DeleteAnimationListener(int entry) {
			this.entry = entry;
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			list.setOnFocusChangeListener(null);
			list.setFocusable(false);
			try {
				service.deleteButtonSet(entries.get(entry).name);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			/*Message delset = dispater.obtainMessage(MainWindow.MESSAGE_DELETEBUTTONSET);
			delset.obj = (entries.get(picked)).name;
			dispater.sendMessage(delset);*/
			editordonelistenr.sendMessageDelayed(editordonelistenr.obtainMessage(104), 10);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public ToolBarButtonKeyListener theButtonKeyListener = new ToolBarButtonKeyListener();
	
	public class ToolBarButtonKeyListener implements View.OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				return true;
			}
			return false;
		}
		
	}
	
	public class ToolbarTabOpenListener implements View.OnClickListener {
		View parent = null;
		ViewFlipper targetFlipper = null;
		int toolbarLength = 0;
		private int index;
		
		public ToolbarTabOpenListener(View parent, ViewFlipper targetFlipper, int toolBarWidth,int index) {
			this.parent = parent;
			this.targetFlipper = targetFlipper;
			toolbarLength = toolBarWidth;
			this.index = index;
		}
		
		@Override
		public void onClick(View v) {
			//v.requestFocus();
			lastSelectedIndex = this.index;
			
			//int targetWidth = 100;
			Animation ai = new TranslateAnimation(toolbarLength, 0, 0, 0);
			ai.setDuration(800);
			
			targetFlipper.setInAnimation(ai);
			
			Animation ao = new TranslateAnimation(0, toolbarLength, 0, 0);
			ao.setDuration(800);
			
			targetFlipper.setOutAnimation(ao);
			
			targetFlipper.showNext();
			
			parent.findViewById(R.id.toolbar_tab_close).requestFocus();
		}
		
	}
	
	private int lastSelectedIndex = -1;
	
	public class ToolbarTabCloseListener implements View.OnClickListener {
		View viewToFocus = null;
		View parent = null;
		ViewFlipper targetFlipper = null;
		int toolbarLength = 0;
		public ToolbarTabCloseListener(View parent, ViewFlipper targetFlipper, int toolBarWidth,View viewToFocus) {
			this.parent = parent;
			this.viewToFocus = viewToFocus;
			this.targetFlipper = targetFlipper;
			toolbarLength = toolBarWidth;
		}
		
		@Override
		public void onClick(View v) {
			//int totalWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_holder).getWidth();
			//int tabWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_tab).getWidth();
			
			//int targetWidth = TriggerSelectionDialog.this.findViewById(R.id.button_holder).getWidth();
			
			Animation ao = new TranslateAnimation(0, toolbarLength, 0, 0);
			ao.setDuration(800);
			//a.setFillBefore(true);
			//a.setFillAfter(false);
			targetFlipper.setOutAnimation(ao);
			
			Animation ai = new TranslateAnimation(toolbarLength, 0, 0, 0);
			ai.setDuration(800);
			//a.setFillBefore(true);
			//a.setFillAfter(false);
			targetFlipper.setInAnimation(ai);
			targetFlipper.showNext();
			
			//parent.findViewById(R.id.toolbar_tab).requestFocus();
			viewToFocus.setFocusable(true);
			viewToFocus.requestFocus();
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
			switch(msg.what) {
			case 104:
				finishDelete();
				break;
			case 100:
				//entry no name change;
				//int index = lastSelectedIndex;
				setSettingsHaveChanged = true;
				ButtonSetSelectorDialog.this.buildList();
				break;
			case 101:
				//edited entry;
				
				setSettingsHaveChanged = true;
				ButtonSetSelectorDialog.this.buildList();
				break;
			}
			//handle the thing comin back;
			//if we got this, it means some settings have changed, and we should reload the button set when we are done regardless if it is the one already selected, or cancelled.

			//Log.e("EDITOR","REBUILDING LIST");
		}
	};
	
	protected void finishDelete() {
		buildList();
		list.setFocusable(true);
		list.setOnFocusChangeListener(new ListFocusFixerListener());
	}
	
	private class EntryCompare implements Comparator<ButtonEntry> {

		public int compare(ButtonEntry a, ButtonEntry b) {
			return a.name.compareToIgnoreCase(b.name);
		}


		
	}
	
	private class ButtonEntry {
		public String name;
		public Integer entries;
		boolean locked = false;
		
		//public ButtonEntry() {
		//	name = "";
		//	entries = 0;
		//}
		
		public ButtonEntry(String n,Integer e) {
			name = n;
			entries = e;
			locked = false;
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
			if(this.locked != bt.locked) retval = false;
			return retval;
		}
	}
	

}
