package com.happygoatstudios.bt.timer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
//import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class TimerSelectionDialog extends Dialog {

	private ListView list;
	private IStellarService service;
	private List<TimerItem> entries;
	private TimerListAdapter adapter;
	
	private Handler doneHandler;
	
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
		
		setContentView(R.layout.timer_selection_dialog);
		
		list = (ListView)findViewById(R.id.timer_list);
		list.setScrollbarFadingEnabled(false);
		
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
				if(msg.what == 101) {
					updateTimers();
					
					//this.sendEmptyMessageDelayed(101, 1000);
				} else {
					buildList();
				}
				//}
			}
		};
		
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
		
		buildList();
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
		
		boolean anyplaying = false;
		for(TimerData timer : timer_list.values()) {
			TimerItem i = new TimerItem();
			i.name = timer.getName();
			i.ordinal = timer.getOrdinal();
			i.timeLeft = timer.getTTF()/1000;
			i.seconds = timer.getSeconds();
			i.playing = timer.isPlaying();
			if(i.playing) {
				anyplaying = true;
			}
			//Log.e("SELECTOR","LOADED BUTTON:" + i.timeLeft + " : " + timer.getSeconds());
			entries.add(i);
		}
		
		if(timer_list.size() == 0) {
			noTimers = true;
			//doneHandler.removeMessages(100);
		}
		
		if(anyplaying) {
			//doneHandler.sendEmptyMessageDelayed(100,950); //start the draw cycle.
			//updateTimers();
		} else {
			//doneHandler.removeMessages(100);
		}
		
		adapter = new TimerListAdapter(list.getContext(),R.layout.timer_selection_list_row,entries);
		list.setAdapter(adapter);
		adapter.sort(new TimerSorter());
	}
	
	private void updateTimers() {
		Log.e("SELECTOR","UPDATING TIMER VIEWS");
		HashMap<String,TimerProgress> wad = null;
		try {
			 wad = (HashMap<String, TimerProgress>) service.getTimerProgressWad();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wad == null) {
			Log.e("SELECTOR","GOT NO VIEWS");
			return;
		}
		
		for(String ord : wad.keySet()) {
			wad the_wad = views.get(ord);
			Log.e("SELECTOR","WORKING ON ORDINAL " + ord);
			if(the_wad != null) {
				Log.e("SELECTOR","UPDATING WAD TO: " + wad.get(ord).getTimeleft()/1000 + " | " + wad.get(ord).getPercentage()*100 + " THE_WAD.DISPLAY" + the_wad.display.toString());
				//the_wad.display.setText(Long.toString(wad.get(ord).getTimeleft()/1000));
				the_wad.display.setText("WIZ");
				Log.e("SELECTOR","UPDATED WAD TO: " + the_wad.display.getText().toString());
				
				the_wad.progress.setProgress(wad.get(ord).getPercentage()*100);
				//the_wad.display.invalidate();
				//the_wad.progress.invalidate();
				list.invalidate();
				
			}
			
		}
		
		if(wad.size() > 0) {
			Log.e("SELECTOR","REDRAWING SCREEN");
			doneHandler.sendEmptyMessageDelayed(101,1000);
		} else {
			Log.e("SELECTOR","STOPPING DRAWING");
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
				//TimerData timer = null;
				//try {
				//	timer = service.getTimer(Integer.toString(e.ordinal));
				//} catch (RemoteException e1) {
				//	// TODO Auto-generated catch block
				//	e1.printStackTrace();
				//}
				//if(timer == null) {
					//still null, uh oh. means that the timer ordinal does not exist.
				//}
				
				

				
				TextView label = (TextView)v.findViewById(R.id.timer_title);
				TextView extra = (TextView)v.findViewById(R.id.timer_display);
				TextView ordinal = (TextView)v.findViewById(R.id.timer_ordinal);
				ProgressMeter p = (ProgressMeter)v.findViewById(R.id.timer_progress);
				
				if(views.containsKey(Integer.toString(e.ordinal))) {
					views.remove(Integer.toString(e.ordinal));
				}
				views.put(Integer.toString(e.ordinal), new wad(extra,p));
				label.setText(e.name);
				//extra.setText(Long.toString(e.timeLeft));
				ordinal.setText(Integer.toString(e.ordinal));
				
				
				
				//set the progress meter, need to take the ttl and divide it by the seconds/1000.
				float progress = ((float)(e.timeLeft*1000)/((float)e.seconds*1000))*100;
				Log.e("SELECTOR","CONTRUCTING ROW FOR TIMER" + extra.toString());
				p.setProgress(progress);
				p.setRange(100);
				
				ImageButton playpause = (ImageButton)v.findViewById(R.id.timer_playpause);
				ImageButton reset = (ImageButton)v.findViewById(R.id.timer_reset);
				
				if(e.playing) {
					//turn the play button into a pause button.
					playpause.setOnClickListener(new PlayPauseListener(e.ordinal,true));
					playpause.setImageResource(android.R.drawable.ic_media_pause);
				} else {
					playpause.setOnClickListener(new PlayPauseListener(e.ordinal,false));
					playpause.setImageResource(android.R.drawable.ic_media_play);
				}
				reset.setOnClickListener(new ResetListener(e.ordinal));
				
				
				
				
				
			}
			
			return v;
			
		}
	}
	
	private class wad {
		TextView display;
		ProgressMeter progress;
		public wad(TextView pDisplay,ProgressMeter pMeter) {
			display = pDisplay;
			display.setText("WAD");
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
			try {
				if(playing) {
					//pause
					service.pauseTimer(Integer.toString(ordinal));
				} else {
					service.startTimer(Integer.toString(ordinal));
				}
				//buildList();
				updateTimers();
			} catch (RemoteException e) {
				
				e.printStackTrace();
			}
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
			//buildList();
			updateTimers();
		}
		
	}
}
