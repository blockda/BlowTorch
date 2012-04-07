package com.offsetnull.bt.timer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.IConnectionBinder;
import com.offsetnull.bt.window.AnimatedRelativeLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
//import android.util.Log;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class TimerSelectionDialog extends Dialog {

	private ListView list;
	private IConnectionBinder service;
	private List<TimerItem> entries;
	private TimerListAdapter adapter;
	
	private Handler doneHandler;

	public int lastSelectedIndex = -1;

	private RelativeLayout targetHolder = null;
	private int targetIndex = -1;
	String currentPlugin = "main";
	ListView mOptionsList;
	
	LinearLayout theToolbar = null;
	int toolbarLength = 0;
	ImageButton playPauseButton = null;
	
	LayoutAnimationController animateInController = null;
	TranslateAnimation animateOut = null;
	TranslateAnimation animateOutNoTransition = null;
	
	public TimerSelectionDialog(Context context,IConnectionBinder the_service) {
		super(context);
		service = the_service;
		entries = new ArrayList<TimerItem>();
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.editor_selection_dialog);
		
		list = (ListView)findViewById(R.id.list);
		list.setScrollbarFadingEnabled(false);
		list.setFocusable(true);
		list.setClickable(true);
		list.setEmptyView(findViewById(R.id.empty));
		
		list.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				arg1.performClick();
			}
			
		});
		
		list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(arg2 != lastSelectedIndex) {
					//arg0.is
					if(arg0.getFirstVisiblePosition() <= lastSelectedIndex && arg0.getLastVisiblePosition() >= lastSelectedIndex) {
						if(theToolbar.getParent() != null) {
							theToolbar.startAnimation(animateOutNoTransition);
						}
					} else {
						if(theToolbar.getParent() != null) {
							((RelativeLayout)theToolbar.getParent()).removeAllViews();
						}
					}
				}
				lastSelectedIndex = arg2;
				//check to see if the 
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		list.setItemsCanFocus(true);
		adapter = new TimerListAdapter(list.getContext(),0,entries);
		list.setAdapter(adapter);
		View newbutton = findViewById(R.id.add);
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimerEditorDialog newdialog = new TimerEditorDialog(TimerSelectionDialog.this.getContext(),currentPlugin,null,service,doneHandler);
				newdialog.show();
			}
		});
		
		View donebutton = findViewById(R.id.done);
		
		donebutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimerSelectionDialog.this.dismiss();
			}
		});
		
		//set click listeners.
		doneHandler = new Handler() {
			public void handleMessage(Message msg) {
				//do some stuff;
				
				//if(msg.what == 100) {
				
					//if(this.hasMessages(100)) {
						
					//} else {
					//	this.sendEmptyMessageDelayed(100, 330);
					//}
				
				//} else {
				switch(msg.what) {
				case 99:
					//special editor non new exit, means we need to clear the progress wad of the ordinal.
					//wad.remove((String)msg.obj);
					buildList();
					break;
				case 101: 
					
					//ListView tmp = (ListView) TimerSelectionDialog.this.findViewById(R.id.list);
					//tmp.invalidate();
					//adapter.notifyDataSetInvalidated();
					//updateTimers();
					buildList();
					break;
					//this.sendEmptyMessageDelayed(101, 1000);
				case 100:
					//editor new 
					//button_row.setVisibility(View.VISIBLE);
					lastSelectedIndex = Integer.parseInt((String)msg.obj);
					buildList();
					break;
				case 102:
					//button_row.setVisibility(View.INVISIBLE);
					break;
				default:
					break;
				}
				//}
			}
		};
		
		TextView title = (TextView)findViewById(R.id.titlebar);
		title.setText("TIMERS");
		
		makeToolbar();
		
		buildList();
		
	}
	
	//private boolean noTimers = false;
	
	@SuppressWarnings("unchecked")
	private void buildList() {
		
		entries.clear();
		
		HashMap<String,TimerData> timer_list = null;
		try{
			if(currentPlugin.equals("main")) {
				timer_list = (HashMap<String,TimerData>)service.getTimers();
			} else {
				timer_list = (HashMap<String,TimerData>)service.getPluginTimers(currentPlugin);
			}
		}catch (RemoteException e) {
			e.printStackTrace();
		}
		//playpause_listeners.clear();
		//reset_listeners.clear();
		boolean anyplaying = false;
		for(TimerData timer : timer_list.values()) {
			TimerItem i = new TimerItem();
			i.name = timer.getName();
			i.ordinal = timer.getOrdinal();
			i.timeLeft = timer.getRemainingTime();
			i.seconds = timer.getSeconds();
			i.playing = timer.isPlaying();
			i.selected = false;
			//set up a listener
			//playpause_listeners.put(timer.getOrdinal().toString(), new PlayPauseListener(timer.getOrdinal().intValue(),timer.isPlaying()));
			//reset_listeners.put(timer.getOrdinal().toString(), new ResetListener(timer.getOrdinal().intValue()));
			if(i.playing) {
				anyplaying = true;
				//Log.e("SELECTOR","ORDINAL " + i.ordinal + " IS PLAYING");
				
			}
			//Log.e("SELECTOR","LOADED BUTTON:" + i.timeLeft + " : " + timer.getSeconds());
			entries.add(i);
		}
		
		
		
		/*if(anyplaying) {
			if(doneHandler.hasMessages(101)) {
				
			} else {
				doneHandler.sendEmptyMessageDelayed(101,1000);
				//Log.e("SELECTOR","STARTING A NEW DRAW SEQUENCE!");
				
			}
		} else {
			//Log.e("SELECTOR","STOPPING DRAWING");
			doneHandler.removeMessages(101);
		}*/
		
		//adapter = new TimerListAdapter(list.getContext(),0,entries);
		//list.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();
		adapter.sort(new TimerSorter());
		list.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		//if(lastSelectedIndex != 1) {
		//	list.setSelection(lastSelectedIndex);
		//}
	}
	//HashMap<String,TimerProgress> wad = new HashMap<String,TimerProgress>();
	
	public class TimerListAdapter extends ArrayAdapter<TimerItem> {
		List<TimerItem> entries;
		
		public TimerListAdapter(Context context,
				int textViewResourceId, List<TimerItem> objects) {
			super(context, textViewResourceId, objects);
			entries = objects;
		}
		
		public View getView(int pos, View convertView,ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.editor_selection_list_row,null);
				//RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
				//root.setOnClickListener(mLineClicker);
			}
			
			v.setOnClickListener(mLineClicker);
			
			TimerItem e = entries.get(pos);
			
			RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
			
			holder.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			
			holder.setLayoutAnimation(animateInController);
			if(holder.getChildCount() > 0) {
				holder.removeAllViews();
				lastSelectedIndex = -1;
			}
			
			v.setId(157*pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.infoTitle);
				TextView extra = (TextView)v.findViewById(R.id.infoExtended);
				//TextView ordinal = (TextView)v.findViewById(R.id.timer_ordinal);
				//ProgressMeter p = (ProgressMeter)v.findViewById(R.id.timer_progress);
				//TextView status = (TextView)v.findViewById(R.id.timer_status);
				//TextView total = (TextView)v.findViewById(R.id.timer_total);
				label.setText(e.name);
				extra.setText(Integer.toString(e.seconds) + " seconds.");
				
				//int text_color = 0xFF888888;
				//int non_selected = 0xFF333333;
				//int selected = 0xFF888888;
				ImageView icon = (ImageView) v.findViewById(R.id.icon);
				
				if(e.playing) {
					icon.setImageResource(R.drawable.toolbar_mini_play);
				} else {
					if(e.timeLeft == e.seconds) {
						icon.setImageResource(R.drawable.toolbar_mini_stop);
					} else {
						icon.setImageResource(R.drawable.toolbar_mini_pause);
					}
				}
				
				
				
			}
			
			return v;
			
		}
	}
	
	public class TimerSorter implements Comparator<TimerItem> {

		public int compare(TimerItem a,TimerItem b) {
			if(a.ordinal > b.ordinal) {
				return 1;
			} else if(a.ordinal < b.ordinal){
				return -1;
			} else {
				return 0;
			}
		}
		
	}
	
	public class TimerItem {
		String name;
		int ordinal;
		int timeLeft;
		boolean playing;
		int seconds;
		boolean selected;
	}
	
	public class CustomAnimationEndListener implements AnimatedRelativeLayout.OnAnimationEndListener {

		@Override
		public void onCustomAnimationEnd() {
			
			RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
			if(rl == null) {
				return;
			}
			rl.removeAllViews();

			if(targetHolder != null) {
				//set the image view.
				TimerItem data = adapter.getItem(targetIndex);
				if(data.playing) {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_pause_button);
				} else {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_play_button);
				}
				targetHolder.setLayoutAnimation(animateInController);
				
				targetHolder.addView(theToolbar);
			}
			lastSelectedIndex = targetIndex;
		}
		
	}
	
	public CustomAnimationEndListener mCustomAnimationListener = new CustomAnimationEndListener();
	
	private class LineClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			int pos = v.getId() / 157;
			//Log.e("CLICK","this is the clicker, clicked view:"+ pos);
			
			if(lastSelectedIndex < 0) {
				
				lastSelectedIndex = pos;
				RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.toolbarholder);
				rl.setLayoutAnimation(animateInController);
				TimerItem data = adapter.getItem(lastSelectedIndex);
				if(data.playing) {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_pause_button);
				} else {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_play_button);
				}
				rl.addView(theToolbar);
			} else if(lastSelectedIndex != pos) {
				//Log.e("SLDF","AM I EVEN HERE");
				AnimatedRelativeLayout holder = (AnimatedRelativeLayout)theToolbar.getParent();
				if(holder != null) {
					if(list.getFirstVisiblePosition() <= lastSelectedIndex && list.getLastVisiblePosition() >= lastSelectedIndex) {
					
						holder.setAnimationListener(mCustomAnimationListener);
						holder.startAnimation(animateOut);
						targetIndex = pos;
						targetHolder = (RelativeLayout) v.findViewById(R.id.toolbarholder);
						
					} else {
						holder.removeAllViews();
						RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.toolbarholder);
						rl.setLayoutAnimation(animateInController);
						TimerItem data = adapter.getItem(pos);
						if(data.playing) {
							((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_pause_button);
						} else {
							((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_play_button);
						}
						rl.addView(theToolbar);
					}
				}
				//theToolbar.startAnimation(animateOut);
			} else {
				
				//lastSelectedIndex = -1;
				if(theToolbar.getParent() == null) {
					lastSelectedIndex = pos;
					RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
					holder.setLayoutAnimation(animateInController);
					TimerItem data = adapter.getItem(pos);
					if(data.playing) {
						((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_pause_button);
					} else {
						((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_play_button);
					}
					holder.addView(theToolbar);
				} else {
					targetIndex = pos;
					theToolbar.startAnimation(animateOutNoTransition);
					
				}
			}
		}
		
	}
	
	private LineClickedListener mLineClicker = new LineClickedListener();
	
	private void makeToolbar() {
		LayoutInflater li = (LayoutInflater)TimerSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		theToolbar = (LinearLayout) li.inflate(R.layout.editor_selection_list_row_toolbar, null);
		RelativeLayout.LayoutParams toolbarParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		theToolbar.setLayoutParams(toolbarParams);
		
		playPauseButton = new ImageButton(TimerSelectionDialog.this.getContext());
		ImageButton stop = new ImageButton(TimerSelectionDialog.this.getContext());
		ImageButton modify = new ImageButton(TimerSelectionDialog.this.getContext());
		ImageButton delete = new ImageButton(TimerSelectionDialog.this.getContext());
		
		LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		params.setMargins(0, 0, 0, 0);
		
		
		playPauseButton.setLayoutParams(params);
		stop.setLayoutParams(params);
		modify.setLayoutParams(params);
		delete.setLayoutParams(params);
		
		playPauseButton.setPadding(0, 0, 0, 0);
		stop.setPadding(0, 0, 0, 0);
		modify.setPadding(0, 0, 0, 0);
		delete.setPadding(0, 0, 0, 0);
		//AliasEntry a = entries.get(pos);
		//if(a.enabled) {
		stop.setImageResource(R.drawable.toolbar_stop_button);
		playPauseButton.setImageResource(R.drawable.toolbar_play_button);
		
		//} else {
		//	toggle.setImageResource(R.drawable.toolbar_toggleoff_button);
		//}
		modify.setImageResource(R.drawable.toolbar_modify_button);
		delete.setImageResource(R.drawable.toolbar_delete_button);
		
		playPauseButton.setBackgroundColor(0);
		stop.setBackgroundColor(0x0000000000);
		modify.setBackgroundColor(0);
		delete.setBackgroundColor(0);
		
		playPauseButton.setOnKeyListener(theButtonKeyListener);
		stop.setOnKeyListener(theButtonKeyListener);
		modify.setOnKeyListener(theButtonKeyListener);
		delete.setOnKeyListener(theButtonKeyListener);
		
		playPauseButton.setOnClickListener(new PlayPauseButtonListener());
		stop.setOnClickListener(new StopButtonListener());
		modify.setOnClickListener(new ModifyButtonListener());
		delete.setOnClickListener(new DeleteButtonListener());
		
		theToolbar.addView(playPauseButton);
		theToolbar.addView(stop);
		theToolbar.addView(modify);
		theToolbar.addView(delete);
		
		
		ImageButton close = (ImageButton)theToolbar.findViewById(R.id.toolbar_tab_close);
		close.setOnKeyListener(theButtonKeyListener);
		
		toolbarLength = close.getDrawable().getIntrinsicWidth() + (modify.getDrawable().getIntrinsicWidth() * 4); 
		
		TranslateAnimation animation2 = new TranslateAnimation(toolbarLength,0,0,0);
		animation2.setDuration(300);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(animation2);
		animateInController = new LayoutAnimationController(set);
		
		animateOut = new TranslateAnimation(0,toolbarLength,0,0);
		animateOut.setDuration(300);

		animateOutNoTransition = new TranslateAnimation(0,toolbarLength,0,0);
		animateOutNoTransition.setDuration(300);
		animateOutNoTransition.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
				rl.removeAllViews();
				lastSelectedIndex = targetIndex;
				lastSelectedIndex = -1;
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
		});
	}
	
	public ToolBarButtonKeyListener theButtonKeyListener = new ToolBarButtonKeyListener();
	
	public class ToolBarButtonKeyListener implements View.OnKeyListener {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				int first = 0;
				//int last = list.getLastVisiblePosition();
				if(lastSelectedIndex - 1 >= first) {
					/*list.setSelection(lastSelectedIndex -1);
					RelativeLayout row = (RelativeLayout) list.getChildAt(lastSelectedIndex -1);
					row.performClick();*/
					list.setSelection(lastSelectedIndex - 1);
					return true;
				} else {
					return false;
				}
				//break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				int last = list.getCount() -1;
				if(lastSelectedIndex + 1 <= last) {
					/*list.setSelection(lastSelectedIndex +1);
					int childCount = list.getChildCount();
					//list.getAdapter().get
					RelativeLayout row = (RelativeLayout) list.getChildAt(lastSelectedIndex +1);
					row.performClick();*/
					list.setSelection(lastSelectedIndex + 1);
					return true;
				} else {
					return false;
				}
				//break;
			}

			return false;
		}
		
	}
	
	public class ModifyButtonListener implements View.OnClickListener {
		//private int index = -1;
		public ModifyButtonListener() {
			//this.index = entry;
		}
		public void onClick(View v) {
			//int index = v.getId() / 159;
			int index = lastSelectedIndex;
			TimerItem entry = adapter.getItem(index);
			//launch the trigger editor with this item.
			try {
				TimerData data = null;
				if(currentPlugin.equals("main")) {
					data = service.getTimer(entry.name);
				} else {
					data = service.getPluginTimer(currentPlugin,entry.name);
				}
				TimerEditorDialog editor = new TimerEditorDialog(TimerSelectionDialog.this.getContext(),currentPlugin,data,service,doneHandler);
				editor.show();
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class DeleteButtonListener implements View.OnClickListener {


		public DeleteButtonListener() {

		}
		
		public void onClick(View v) {

			AlertDialog.Builder builder = new AlertDialog.Builder(TimerSelectionDialog.this.getContext());
			builder.setTitle("Delete Timer");
			builder.setMessage("Confirm Delete?");
			builder.setPositiveButton("Delete", new ReallyDeleteAliasListener());
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			AlertDialog d = builder.create();
			d.show();
			
		}
		
	}
	
	public class StopButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			TimerItem entry = adapter.getItem(lastSelectedIndex);
			if(currentPlugin.equals("main")) {
				try {
					service.stopTimer(entry.name);
					entry.playing = false;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					service.stopPluginTimer(currentPlugin, entry.name);
					entry.playing = false;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			playPauseButton.setImageResource(R.drawable.toolbar_play_button);
			RelativeLayout row = (RelativeLayout)playPauseButton.getParent().getParent().getParent();
			((ImageView)row.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_stop);
		}
		
	}
	
	public class PlayPauseButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			TimerItem entry = adapter.getItem(lastSelectedIndex);

			if(entry.playing) {
				entry.playing = false;
				if(currentPlugin.equals("main")) {
					try {
						service.pauseTimer(entry.name);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						service.pausePluginTimer(currentPlugin, entry.name);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				((ImageButton)v).setImageResource(R.drawable.toolbar_play_button);
				RelativeLayout row = (RelativeLayout)v.getParent().getParent().getParent();
				((ImageView)row.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_pause);
			} else {
				entry.playing = true;
				if(currentPlugin.equals("main")) {
					try {
						service.startTimer(entry.name);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						service.startPluginTimer(currentPlugin, entry.name);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				((ImageButton)v).setImageResource(R.drawable.toolbar_pause_button);
				RelativeLayout row = (RelativeLayout)v.getParent().getParent().getParent();
				((ImageView)row.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_play);
			}
		}
		
	}
	
	public class ReallyDeleteAliasListener implements DialogInterface.OnClickListener {

		public ReallyDeleteAliasListener() {

		}
		public void onClick(DialogInterface dialog, int which) {

			dialog.dismiss();
			Animation a = new TranslateAnimation(0, toolbarLength, 0, 0);
			a.setDuration(300);
			a.setAnimationListener(new DeleteAnimationListener());
			
			theToolbar.startAnimation(a);
			
		}
		
	}
	
	public class DeleteAnimationListener implements Animation.AnimationListener {

		//int entry = -1;
		public DeleteAnimationListener() {
			//this.entry = entry;
		}
		
		public void onAnimationEnd(Animation animation) {
			//list.setOnFocusChangeListener(null);
			//list.setFocusable(false);
			try {
				if(currentPlugin.equals("main")) {
					service.deleteTimer(entries.get(lastSelectedIndex).name);
				} else {
					service.deletePluginTimer(currentPlugin,entries.get(lastSelectedIndex).name);
				}
				
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//triggerModifier.sendMessageDelayed(triggerModifier.obtainMessage(104), 10);
			adapter.remove(adapter.getItem(lastSelectedIndex));
			adapter.notifyDataSetInvalidated();
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
