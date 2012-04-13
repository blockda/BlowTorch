package com.offsetnull.bt.window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.IConnectionBinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class PluginDialog extends Dialog {

	private ListView list;
	private IConnectionBinder service;
	private List<PluginItem> entries;
	private PluginListAdapter adapter;
	private int lastSelectedIndex = -1;
	
	public PluginDialog(Context context,IConnectionBinder service) {
		super(context);
		this.service = service;
		// TODO Auto-generated constructor stub
		entries = new ArrayList<PluginItem>();
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.editor_selection_dialog);
		
		//initialize the list view
		list = (ListView)findViewById(R.id.list);
		
		list.setScrollbarFadingEnabled(false);
		
		list.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		//list.setOnItemClickListener(new EditTriggerListener());
		//list.setOnItemLongClickListener(new DeleteTriggerListener());
		//list.setFocusable(false);
		//list.setFocusableInTouchMode(false);
		
		//list.setOnI
		//attempt to fish out the trigger list.
		
		/*list.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.e("WINDOW","LISTVIEW HAS FOCUS");
			}
			
		});*/
		//list.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
				
				Log.e("LIST","SELECTED ELEMENT:" + arg2);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
				Log.e("LIST","NOTHING SELECTED");
				
			}
		});
		
		list.setOnFocusChangeListener(new ListFocusFixerListener());
		
		list.setSelector(R.drawable.transparent);
		
		
		list.setEmptyView(findViewById(R.id.empty));
		buildList();
		
		Button load = (Button)findViewById(R.id.add);
		load.setText("Load");
		load.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				PluginSelectorDialog loader = new PluginSelectorDialog(PluginDialog.this.getContext());
				loader.show();
			}
		});
		
		
		
		Button cancelbutton = (Button)findViewById(R.id.done);
		
		cancelbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				PluginDialog.this.dismiss();
			}
			
		});
	}
	
	private void buildList() {
		//list.setOnItemSelectedListener(null);
		if(adapter != null) {
			adapter.clear();
		}
		entries.clear();
		
		HashMap<String, String> plist = null;
		try {
			plist = (HashMap<String, String>) service.getPluginList();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String key : plist.keySet()) {
			String info = plist.get(key);
			
			PluginItem plug = new PluginItem();
			plug.name = key;
			plug.extra = info;
			entries.add(plug);
		}
		//adapter.notifyDataSetInvalidated();
		/*HashMap<String, TriggerData> trigger_list = null;
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
				t.enabled = data.isEnabled();
				
				entries.add(t);
			}
		}*/
		//adapter.clear();
		//adapter = null;
		
		//list.removeAllViews();
		if(adapter == null) {
			
			adapter = new PluginListAdapter(list.getContext(),0,entries);
			list.setAdapter(adapter);
		}
		
		
		//list.setOnFocusChangeListener(new ListFocusFixerListener());
		adapter.sort(new ItemSorter());
		adapter.notifyDataSetInvalidated();
	}
	
	public class PluginItem {
		public boolean enabled;
		String name;
		String extra;
		
		public PluginItem() {
			name = "";
			extra = "";
			enabled = true;
		}
		
		public boolean equals(Object o) {
			if(o == this) return true;
			
			if(!(o instanceof PluginItem)) return false;
			PluginItem tmp = (PluginItem)o;
			if(!this.name.equals(tmp.name)) return false;
			if(!this.extra.equals(tmp.extra)) return false;
			if(this.enabled != tmp.enabled) return false;
			return true;
		}
	}
	
	public class PluginListAdapter extends ArrayAdapter<PluginItem> {

		List<PluginItem> entries;
		
		public PluginListAdapter(Context context,
				int textViewResourceId, List<PluginItem> objects) {
			super(context, textViewResourceId, objects);
			entries = objects;
		}
		
		public View getView(int pos, View convertView,ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.better_list_row,null);
			}
			
			RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
			root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			PluginItem e = entries.get(pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.infoTitle);
				TextView extra = (TextView)v.findViewById(R.id.infoExtended);
				
				label.setText(e.name);
				extra.setText(e.extra);
				
			}
			
			v.findViewById(R.id.spacer).setVisibility(View.INVISIBLE);
			
			
			ImageView iv = (ImageView) v.findViewById(R.id.icon);
			if(e.enabled) {
				iv.setImageResource(R.drawable.toolbar_mini_enabled);
			} else {
				iv.setImageResource(R.drawable.toolbar_mini_disabled);
			}
			//int totalWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_holder).getWidth();
			//int tabWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_tab).getWidth();
			//ViewFlipper flip = (ViewFlipper) v.findViewById(R.id.flipper);
			//flip.showNext();
			
			//v.findViewById(R.id.toolbar_tab).setOnClickListener(new ToolbarTabOpenListener(v,(ViewFlipper)v.findViewById(R.id.flipper)));
			
			//v.findViewById(R.id.toolbar_tab_close).setOnClickListener(new ToolbarTabCloseListener(v,(ViewFlipper)v.findViewById(R.id.flipper)));
			
			//make and populate utility buttons buttons.
			
			ImageButton toggle = new ImageButton(PluginDialog.this.getContext());
			ImageButton modify = new ImageButton(PluginDialog.this.getContext());
			ImageButton delete = new ImageButton(PluginDialog.this.getContext());
			
			//LayoutParams params = LinearLayout.
			//params.height = LayoutParams.WRAP_CONTENT;
			//params.width = LayoutParams.WRAP_CONTENT;
			//params.
			LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			params.setMargins(0, 0, 0, 0);
			//params.
			//params.
			//toggle.setM
			
			toggle.setLayoutParams(params);
			modify.setLayoutParams(params);
			delete.setLayoutParams(params);
			
			toggle.setPadding(0, 0, 0, 0);
			modify.setPadding(0, 0, 0, 0);
			delete.setPadding(0, 0, 0, 0);
			if(e.enabled) {
				toggle.setImageResource(R.drawable.toolbar_toggleon_button);
			} else {
				toggle.setImageResource(R.drawable.toolbar_toggleoff_button);
			}
			modify.setImageResource(R.drawable.toolbar_modify_button);
			delete.setImageResource(R.drawable.toolbar_delete_button);
			
			//toggle.setIm
			
			toggle.setBackgroundColor(0x0000000000);
			modify.setBackgroundColor(0);
			delete.setBackgroundColor(0);
			
			toggle.setOnKeyListener(theButtonKeyListener);
			modify.setOnKeyListener(theButtonKeyListener);
			delete.setOnKeyListener(theButtonKeyListener);
			
			
			//toggle.setOnClickListener(new ToggleButtonListener(pos));
			//modify.setOnClickListener(new ModifyButtonListener(pos));
			//delete.setOnClickListener(new DeleteButtonListener(pos,v.findViewById(R.id.flipper),width));
			//get the holder.
			LinearLayout holder = (LinearLayout) v.findViewById(R.id.button_holder);
			holder.removeAllViews();
			holder.addView(toggle);
			holder.addView(modify);
			holder.addView(delete);
			
			int width = toggle.getDrawable().getIntrinsicWidth() + delete.getDrawable().getIntrinsicWidth() + modify.getDrawable().getIntrinsicWidth();
			
			toggle.setOnClickListener(new ToggleButtonListener(pos,iv,e.name));
			modify.setOnClickListener(new ModifyButtonListener(pos));
			delete.setOnClickListener(new DeleteButtonListener(pos,(ViewFlipper)v.findViewById(R.id.flipper),width));
			
			v.findViewById(R.id.toolbar_tab).setOnClickListener(new ToolbarTabOpenListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,pos));
			
			v.findViewById(R.id.toolbar_tab_close).setOnClickListener(new ToolbarTabCloseListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,v.findViewById(R.id.toolbar_tab)));
			v.findViewById(R.id.toolbar_tab_close).setOnKeyListener(theButtonKeyListener);
			
			v.findViewById(R.id.toolbar_tab).setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
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
			
			return v;
			
		}
		
	}
	
	public class ListFocusFixerListener implements View.OnFocusChangeListener {
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
					Log.e("LIST","SETTING FOCUS ON:" + lastSelectedIndex);
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
			Log.e("FOCUS","FOCUS CHANGE LISTENER FIRE, focus is " + hasFocus);
		}
	}
	
	public class ToggleButtonListener implements View.OnClickListener {

		private int index = -1;
		ImageView icon = null;
		String key = null;
	
		public ToggleButtonListener(int index,ImageView icon,String key) {
			this.index = index;
			this.icon = icon;
			this.key = key;
		}
		
		public void onClick(View v) {
			PluginItem entry = adapter.getItem(index);
			//View top = list.getChildAt(index);
			//ViewFlipper flip = top.findViewById(R.id.flipper);
			ImageButton b = (ImageButton)v;
			if(entry.enabled) {
				b.setImageResource(R.drawable.toolbar_toggleoff_button);
				try {
					service.setTriggerEnabled(false,key);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entry.enabled = false;
				
				icon.setImageResource(R.drawable.toolbar_mini_disabled);
			} else {
				b.setImageResource(R.drawable.toolbar_toggleon_button);
				try {
					service.setTriggerEnabled( true,key);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entry.enabled = true;
				icon.setImageResource(R.drawable.toolbar_mini_enabled);
			}
			
		}
		
	}
	
	public class ModifyButtonListener implements View.OnClickListener {
		private int index = -1;
		public ModifyButtonListener(int entry) {
			this.index = entry;
		}
		public void onClick(View v) {
			PluginItem entry = adapter.getItem(index);
			//launch the trigger editor with this item.
			//try {
				
				//TriggerData data = service.getTrigger(entry.name);

				//TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),data,service,triggerEditorDoneHandler);
				//editor.show();
				
				
			//} catch (RemoteException e) {
			//	e.printStackTrace();
			//}
		}
	}
	
	public class DeleteAnimationListener implements Animation.AnimationListener {

		int entry = -1;
		public DeleteAnimationListener(int entry) {
			this.entry = entry;
		}
		
		public void onAnimationEnd(Animation animation) {
			list.setOnFocusChangeListener(null);
			list.setFocusable(false);
			try {
				service.deleteTrigger(entries.get(entry).extra);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//triggerModifier.sendMessageDelayed(triggerModifier.obtainMessage(104), 10);
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public class DeleteButtonListener implements View.OnClickListener {

		private int entry = -1;
		ViewFlipper flip = null;
		private int animateDistance = 0;
		public DeleteButtonListener(int element,ViewFlipper flip,int animateDistance) {
			entry = element;
			this.flip = flip;
			this.animateDistance = animateDistance;
		}
		
		public void onClick(View v) {
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(PluginDialog.this.getContext());
			builder.setTitle("Delete Plugin");
			builder.setMessage("Confirm Delete?");
			builder.setPositiveButton("Delete", new ReallyDeleteTriggerListener(flip,animateDistance,entry));
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			AlertDialog d = builder.create();
			d.show();
			
			
			
			//arg0.dismiss();
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
	
	public ToolBarButtonKeyListener theButtonKeyListener = new ToolBarButtonKeyListener();
	
	public class ToolBarButtonKeyListener implements View.OnKeyListener {

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
	
	
	public class ItemSorter implements Comparator<PluginItem>{

		public int compare(PluginItem a, PluginItem b) {
			return a.name.compareToIgnoreCase(b.name);
		}
		
	}
}
