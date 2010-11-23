package com.happygoatstudios.bt.timer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
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
//import android.util.Log;
import android.text.TextUtils.TruncateAt;
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
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TimerSelectionDialog extends Dialog {

	private ListView list;
	private IStellarService service;
	private List<TimerItem> entries;
	private TimerListAdapter adapter;
	
	private Handler doneHandler;
	private HashMap<String,PlayPauseListener> playpause_listeners;
	private HashMap<String,ResetListener> reset_listeners;
	
	private Button edit;
	
	private Window the_window;
	
	public int selected = -1;
	
	private TableLayout button_row;
	
	private HashMap<String,wad> views = new HashMap<String,wad>();
	
	public TimerSelectionDialog(Context context,IStellarService the_service) {
		super(context);
		// TODO Auto-generated constructor stub
		service = the_service;
		entries = new ArrayList<TimerItem>();
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		the_window = this.getWindow();
		//the_window.
		
		//selected = -1;
		
		setContentView(R.layout.timer_selection_dialog);
		
		list = (ListView)findViewById(R.id.timer_list);
		list.setScrollbarFadingEnabled(false);
		list.setFocusable(true);
		list.setClickable(true);
		
		button_row = (TableLayout)findViewById(R.id.button_table);
		//button_row.set
		
		
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//arg2 is the selected item.
				//list.setSelection(arg2);
				TimerSelectionDialog.this.selected = arg2;
				//Log.e("CLICKER","SELECTED IS " + selected);
				//i.selected = true;
				//button_row.setVisibility(View.VISIBLE);
				
				adapter.notifyDataSetChanged();
				
			}
			
		});
		
		//list.inv
		
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
					wad.remove((String)msg.obj);
					buildList();
					break;
				case 101: 
					
					ListView tmp = (ListView) TimerSelectionDialog.this.findViewById(R.id.timer_list);
					tmp.invalidate();
					adapter.notifyDataSetInvalidated();
					//updateTimers();
					buildList();
					break;
					//this.sendEmptyMessageDelayed(101, 1000);
				case 100:
					//editor new 
					//button_row.setVisibility(View.VISIBLE);
					selected = Integer.parseInt((String)msg.obj);
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
		
		//doneHandler.sendEmptyMessageDelayed(102,2500);
		
		//build the list
		
		Button newbutton = (Button)findViewById(R.id.timer_new_button);
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//launch the new editor.
				TimerEditorDialog editor = new TimerEditorDialog(TimerSelectionDialog.this.getContext(),null,service,doneHandler);
				editor.show();
			}
		});
		
		Button cancel = (Button)findViewById(R.id.timer_cancel_button);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				doneHandler.removeMessages(101);
				TimerSelectionDialog.this.dismiss();
			}
		});
		
		reset_listeners = new HashMap<String,ResetListener>();
		playpause_listeners = new HashMap<String,PlayPauseListener>();
		
		ImageButton play = (ImageButton)findViewById(R.id.timer_play);
		ImageButton pause = (ImageButton)findViewById(R.id.timer_pause);
		ImageButton reset = (ImageButton)findViewById(R.id.timer_reset);
		edit = (Button)findViewById(R.id.timer_edit);

		play.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(selected == -1) {
					return;
				}
				
				try {
					service.startTimer(Integer.toString(selected));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buildList();
				//updateTimers();
			}
		});
		
		
		pause.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(selected == -1) {
					return;
				}
				
				try {
					service.pauseTimer(Integer.toString(selected));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buildList();
				//updateTimers();
			}
		});
		
		reset.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(selected == -1) {
					return;
				}
				
				try {
					service.resetTimer(Integer.toString(selected));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buildList();
				//updateTimers();
			}
		});
		
		edit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DispatchEdit();
			}
		});
		
		buildList();
		
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//edit.dispatchKeyEvent(new KeyEvent(KeyEvent.KEYCODE_ENTER));
				DispatchEdit();
				return true;
			}
		});
		
		if(adapter.getCount() > 0) {
			selected = 0;
		}
	}
	
	private void DispatchEdit() {
		if(selected == -1) {
			return;
		}
		
		//else, give the option for editing.
		AlertDialog.Builder b = new AlertDialog.Builder(TimerSelectionDialog.this.getContext());
		b.setTitle("Modify Timer");
		b.setMessage(("Do you want to modify, or delete this timer?"));
		b.setPositiveButton("Modify", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				//attempt to launch the editor.
				TimerEditorDialog editor = null;
				try {
					editor = new TimerEditorDialog(TimerSelectionDialog.this.getContext(),service.getTimer(Integer.toString(selected)),service,doneHandler);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				editor.show();
			}
		});
		b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		b.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				//attempt to delete the timer
				try {
					service.removeTimer(service.getTimer(Integer.toString(selected)));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
				buildList();
			}
		});
		
		AlertDialog alert = b.create();
		alert.show();
	}
	
	public void onBackPressed() {
		doneHandler.removeMessages(101);
		this.dismiss();
	}
	
	private boolean noTimers = false;
	
	private void buildList() {
		if(adapter != null) {
			adapter.clear();
		}
		
		
		
		HashMap<String,TimerData> timer_list = null;
		try{
			timer_list = (HashMap<String,TimerData>)service.getTimers();
		}catch (RemoteException e) {
			e.printStackTrace();
		}
		playpause_listeners.clear();
		reset_listeners.clear();
		boolean anyplaying = false;
		for(TimerData timer : timer_list.values()) {
			TimerItem i = new TimerItem();
			i.name = timer.getName();
			i.ordinal = timer.getOrdinal();
			i.timeLeft = timer.getTTF()/1000;
			i.seconds = timer.getSeconds();
			i.playing = timer.isPlaying();
			i.selected = false;
			//set up a listener
			//playpause_listeners.put(timer.getOrdinal().toString(), new PlayPauseListener(timer.getOrdinal().intValue(),timer.isPlaying()));
			//reset_listeners.put(timer.getOrdinal().toString(), new ResetListener(timer.getOrdinal().intValue()));
			if(i.playing) {
				anyplaying = true;
				Log.e("SELECTOR","ORDINAL " + i.ordinal + " IS PLAYING");
				
			}
			//Log.e("SELECTOR","LOADED BUTTON:" + i.timeLeft + " : " + timer.getSeconds());
			entries.add(i);
		}
		
		if(timer_list.size() == 0) {
			noTimers = true;
			//doneHandler.removeMessages(100);
		}
		
		if(anyplaying) {
			if(doneHandler.hasMessages(101)) {
				
			} else {
				doneHandler.sendEmptyMessageDelayed(101,1000);
				Log.e("SELECTOR","STARTING A NEW DRAW SEQUENCE!");
				
			}
		} else {
			Log.e("SELECTOR","STOPPING DRAWING");
			doneHandler.removeMessages(101);
		}
		
		adapter = new TimerListAdapter(list.getContext(),R.layout.timer_selection_list_row,entries);
		list.setAdapter(adapter);
		adapter.sort(new TimerSorter());
		
		if(selected != 1) {
			list.setSelection(selected);
		}
	}
	HashMap<String,TimerProgress> wad = new HashMap<String,TimerProgress>();
	private void updateTimers() {
		//Log.e("SELECTOR","UPDATING TIMER VIEWS");
		//reset status indicators.
		//for(String ord : wad.keySet()) {
			//views.get(ord).progress.setProgress(100);
			//views.get(ord).
		//}
		
		try {
			 wad.clear();
			 wad = (HashMap<String, TimerProgress>) service.getTimerProgressWad();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wad == null) {
			//Log.e("SELECTOR","GOT NO VIEWS");
			return;
		}
		
		for(String ord : wad.keySet()) {
			wad the_wad = views.get(ord);
			//Log.e("SELECTOR","WORKING ON ORDINAL " + ord);
			if(the_wad != null) {
				//Log.e("SELECTOR","UPDATING WAD TO: " + wad.get(ord).getTimeleft()/1000 + " | " + wad.get(ord).getPercentage()*100 + " THE_WAD.DISPLAY" + the_wad.display.toString());
				the_wad.display.setText(Long.toString(wad.get(ord).getTimeleft()/1000));
				//the_wad.display.setText("WIZ");
				//Log.e("SELECTOR","UPDATED WAD TO: " + the_wad.display.getText().toString());
				
				the_wad.progress.setProgress(wad.get(ord).getPercentage()*100);
				the_wad.progress.setRange(100);
				//the_wad.display.invalidate();
				//the_wad.progress.invalidate();
				//list.invalidate();
				//TimerSelectionDialog.this.
				//adapter.notifyDataSetInvalidated();
				//adapter.
				
			}
			
		}
		
		if(wad.size() > 0) {
			if(doneHandler.hasMessages(101)) {
				
			} else {
				doneHandler.sendEmptyMessageDelayed(101,1000);
				//Log.e("SELECTOR","STARTING A NEW DRAW SEQUENCE!");
				
			}
		} else {
			//Log.e("SELECTOR","STOPPING DRAWING");
			doneHandler.removeMessages(101);
		}
	}
	
	public class TimerListAdapter extends ArrayAdapter<TimerItem> {
		List<TimerItem> entries;
		
		public TimerListAdapter(Context context,
				int textViewResourceId, List<TimerItem> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			entries = objects;
		}
		
		public View getView(int pos, View convertView,ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.timer_selection_list_row,null);
			}
			
			TimerItem e = entries.get(pos);
			
			if(e != null) {
				//pull out the timer data from the service.
				/*TimerData timer = null;
				try {
					timer = service.getTimer(Integer.toString(e.ordinal));
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(timer == null) {
					//still null, uh oh. means that the timer ordinal does not exist.
				}*/
				
				
				
				
				TextView label = (TextView)v.findViewById(R.id.timer_title);
				TextView extra = (TextView)v.findViewById(R.id.timer_display);
				TextView ordinal = (TextView)v.findViewById(R.id.timer_ordinal);
				ProgressMeter p = (ProgressMeter)v.findViewById(R.id.timer_progress);
				TextView status = (TextView)v.findViewById(R.id.timer_status);
				TextView total = (TextView)v.findViewById(R.id.timer_total);
				
				int text_color = 0xFF888888;
				int non_selected = 0xFF333333;
				int selected = 0xFF888888;
				if(e.ordinal == TimerSelectionDialog.this.selected) {
					//Log.e("SELECTOR","SELCTED " + e.ordinal + " SELECTOR IS " + TimerSelectionDialog.this.selected);
					label.setTextColor(0xFF888888);
					extra.setTextColor(0xFF888888);
					label.setBackgroundColor(0xFF555555);
					extra.setBackgroundColor(0xFF555555);
					
					
					ordinal.setTextColor(0xFF888888);
					ordinal.setBackgroundColor(0xFF333333);
					
					total.setTextColor(0xFF888888);
					total.setBackgroundColor(0xFF555555);
					status.setTextColor(0xFF888888);
					status.setBackgroundColor(0xFF555555);
					
					label.setEllipsize(TruncateAt.END);
					//v.setBackgroundColor(0xFF333333);
				} else {
					//Log.e("SELECTOR","NOT SELECTED " + e.ordinal + " SELECTOR IS " + TimerSelectionDialog.this.selected);
					label.setTextColor(0xFF888888);
					extra.setTextColor(0xFF888888);
					label.setBackgroundColor(0xFF333333);
					extra.setBackgroundColor(0xFF333333);
					ordinal.setTextColor(0xFF888888);
					ordinal.setBackgroundColor(0xFF111111);
					//v.setBackgroundColor(0xFF111111);
					
					total.setTextColor(0xFF888888);
					total.setBackgroundColor(0xFF333333);
					status.setTextColor(0xFF888888);
					status.setBackgroundColor(0xFF333333);
					label.setEllipsize(TruncateAt.END);
				}
				
				//if(!e.playing) {
				//	p.setProgress(100);
				//	p.setRange(100);
				//}
				
				/*if(views.containsKey(Integer.toString(e.ordinal))) {
					views.remove(Integer.toString(e.ordinal));
				}
				views.put(Integer.toString(e.ordinal), new wad(extra,p));*/
				label.setText(e.name); label.setClickable(false); label.setFocusable(false);
				
				float progress = ((float)(e.timeLeft)/((float)e.seconds))*100;
				//Log.e("SELECTOR","PROGRESS " + e.timeLeft + " total " + e.seconds + " calc " + progress);
				
				/*if(wad.containsKey(Integer.toString(e.ordinal))) {
					extra.setText(Long.toString(wad.get(Integer.toString(e.ordinal)).getTimeleft()/1000));
					p.setProgress(wad.get(Integer.toString(e.ordinal)).getPercentage()*100);
					p.setRange(100);
				} else {
					//
					extra.setText(Long.toString(e.timeLeft));
					p.setProgress(progress);
					p.setRange(100); p.setClickable(false); p.setFocusable(false);
				}*/
				//extra.setText(Long.toString(e.timeLeft)); extra.setClickable(false); label.setFocusable(false);
				ordinal.setText(Integer.toString(e.ordinal));
				extra.setText(Long.toString(e.timeLeft) + "s left.");
				p.setProgress(progress);
				p.setRange(100);
				
				total.setText(e.seconds + "sec.");
				
				
				if(e.playing) {
					status.setText("(playing)");
				} else {
					if(e.timeLeft == e.seconds) {
						//Log.e("SELECTOR","STATUS STOPPED " + e.timeLeft);
						status.setText("(stopped)");
					} else {
						//Log.e("SELECTOR","STATUS PAUSED " + e.timeLeft);
						status.setText("(paused)");
					}
				}
				//if(wad != null) {
				//	if(wad.containsKey(Integer.toString(e.ordinal))) {
				//		extra.setText(Long.toString(wad.get(Integer.toString(e.ordinal)).getTimeleft()));
				//		p.setProgress(wad.get(Integer.toString(e.ordinal)).getPercentage()*100);
				//		
				//	}
				//}
				//set the progress meter, need to take the ttl and divide it by the seconds/1000.
				
				//Log.e("SELECTOR","CONTRUCTING ROW FOR TIMER" + extra.toString());
				
				
				
				TextView tordinal = (TextView)v.findViewById(R.id.timer_ordinal);
				tordinal.setOnClickListener( new View.OnClickListener() {
					
					public void onClick(View v) {
						v.invalidate();
					}
				});
				
				//v.setFocusable(false);
				//v.setClickable(false);
				
				
				
				//ImageButton playpause = (ImageButton)v.findViewById(R.id.timer_playpause);
				//ImageButton reset = (ImageButton)v.findViewById(R.id.timer_reset);
				
				if(e.playing) {
					//turn the play button into a pause button.
					//playpause.setOnClickListener(playpause_listeners.get( Integer.toString(e.ordinal)));
					//playpause.setImageResource(android.R.drawable.ic_media_pause);
					//playpause_listeners.get( Integer.toString(e.ordinal)).setPlaying(true);
				} else {
					//playpause.setOnClickListener(playpause_listeners.get( Integer.toString(e.ordinal)));
					//playpause.setImageResource(android.R.drawable.ic_media_play);
					//playpause_listeners.get( Integer.toString(e.ordinal)).setPlaying(false);
				}
				//reset.setOnClickListener(reset_listeners.get(Integer.toString(e.ordinal)));
				
				
				
				
				
			}
			
			return v;
			
		}
	}
	
	private class wad {
		TextView display;
		ProgressMeter progress;
		public wad(TextView pDisplay,ProgressMeter pMeter) {
			display = pDisplay;
			//display.setText("WAD");
			progress = pMeter;
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
		long timeLeft;
		boolean playing;
		int seconds;
		boolean selected;
	}
	
	private class PlayPauseListener implements View.OnClickListener {

		private int ordinal;
		private boolean playing;
		public PlayPauseListener(int pOrdinal,boolean pPlaying) {
			ordinal = pOrdinal;
			playing = pPlaying;
		}
		
		public void onClick(View v) {
			//Log.e("PLAY","PLAYPAUSE FOR ORDINAL: " + ordinal);
			//doneHandler.sendEmptyMessageDelayed(100, 100);
			ImageView iv = (ImageView)v;
			try {
				if(playing) {
					//pause
					service.pauseTimer(Integer.toString(ordinal));
					//turn into a pause button
					//Log.e("CLICKER","CHANGING TO PLAY");
					
					iv.setImageResource(android.R.drawable.ic_media_play);
					playing = false;
				} else {
					service.startTimer(Integer.toString(ordinal));
					iv.setImageResource(android.R.drawable.ic_media_pause);
					playing = true;
					//Log.e("CLICKER","CHANGING TO PAUSE");
				}
				//buildList();
				//v.invalidate();
				//adapter.notifyDataSetInvalidated();
				//updateTimers();
				buildList();
			} catch (RemoteException e) {
				
				e.printStackTrace();
			}
		}

		public boolean isPlaying() {
			return playing;
		}

		public void setPlaying(boolean playing) {
			//Log.e("SOMETHING","IS RESETTING " + ordinal + " PLAY STATUS TO " + playing);
			this.playing = playing;
		}
		
	}
	
	private class ResetListener implements View.OnClickListener {

		private int ordinal;
		public ResetListener(int pOrdinal) {
			ordinal = pOrdinal;
			
		}
		
		public void onClick(View v) {
			//Log.e("PAUSE","PAUSE FOR ORDINAL: " + ordinal);
			//doneHandler.sendEmptyMessageDelayed(100, 0);
			try {
				service.resetTimer(Integer.toString(ordinal));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buildList();
			//updateTimers();
		}
		
	}
}
