package com.happygoatstudios.bt.launcher;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsoluteLayout.LayoutParams;
import com.happygoatstudios.bt.window.SlickButton;

import com.happygoatstudios.bt.R;

public class BaardTERMLauncher extends Activity implements ReadyListener {
	
	Timer button_timer = new Timer();
	Handler buttonaddhandler = null;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		//start the initializeations
		setContentView(R.layout.launcher_layout);
		
		
		//get the button
		TextView tv = (TextView)findViewById(R.id.welcometext);
		Button b = (Button)findViewById(com.happygoatstudios.bt.R.id.startbutton);
		
		//make an appropriate listener to launch the connection picker dialog.
		b.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ConnectionPickerDialog dialog = new ConnectionPickerDialog(BaardTERMLauncher.this,BaardTERMLauncher.this);
				dialog.show();
			}
		});
		
		tv.setOnTouchListener(new View.OnTouchListener() {
			long downpresstime;
			long duration;
			@SuppressWarnings("deprecation")
			public boolean onTouch(View v, MotionEvent event) {
				//if(event.getAction() == MotionEvent.ACTION_DOWN) { 
				//	downpresstime = event.getEventTime();
				//}
				if(!buttondropstarted && event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.e("LAUNCHER","MOTIONEVENTS INCOMING!" + event.getDownTime() + "|" + event.getEventTime());
				
				x = (int)event.getX(event.getPointerId(0));
				y = (int)event.getY(event.getPointerId(0));
				
				button_timer.schedule(new AddButtonTask(), 3000);
				buttondropstarted = true;
				
				return true;
				}
				
				if(buttondropstarted && event.getAction() == MotionEvent.ACTION_UP) {
					button_timer.cancel();
					button_timer.purge();
					button_timer = new Timer();
					Log.e("LAUNCHER","CANCELLING BUTTON ADD");
					//button_timer.purge();
					buttondropstarted = false;
					/*RelativeLayout l = (RelativeLayout)findViewById(R.id.welcomelayout);
					l.removeAllViews();
					BaardTERMLauncher.this.setContentView(l);
					l.invalidate(); */
					
					
				}
				return false;
			}
		});
		
		tv.setLongClickable(false);
		
		/*tv.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				//add button at current xy, reset the long click coordinates and state
				Log.e("LAUNCHER","LONG PRESS @ X:" + x + " Y:" + y);
				buttondropstarted = false;
				return true;
			}
		});*/
		
		buttonaddhandler = new Handler() {
			public void handleMessage(Message what) {
				//addbutton
				Log.e("LAUNCHER","BUTTON HANDLER THING CALLED!");
				RelativeLayout l = (RelativeLayout)findViewById(R.id.welcomelayout);

				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			
				SlickButton b = new SlickButton(l.getContext(),x,y);

				b.setClickable(true);
				b.setFocusable(true);
				
				buttons.add(b);
				int pos = buttons.lastIndexOf(b);
				
				l.addView(buttons.get(pos),lp);
				l.invalidate();
				x = 0;
				y = 0;
				buttondropstarted = false;
			}
		};
		
	}
	
	Vector<SlickButton> buttons = new Vector<SlickButton>();
	
	int x = 0;
	int y = 0;
	boolean buttondropstarted = false;
	boolean buttondropdone = true;
	
	//called when ConnectionPickerDialog returns.
    public void ready(String displayname,String host,String port) {
        /*connection = new Thread(this);
        connection.setName("Connection_Handler");
        connection.start();
        
		FixedViewFlipper vf = (FixedViewFlipper)findViewById(R.id.welcomeflipper);
		vf.showNext();
		
		synchronized(this) {
		try {
			this.wait(1000); //give the other thread time to start up.
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		Message tmp = ConnectionHandler.obtainMessage(100);
		ConnectionHandler.sendMessage(tmp);*/
    	
    	//start service
    	
    	//start window activity.
    	Intent the_intent = new Intent(com.happygoatstudios.bt.window.BaardTERMWindow.class.getName());
    	
    	the_intent.putExtra("DISPLAY",displayname);
    	the_intent.putExtra("HOST", host);
    	the_intent.putExtra("PORT", port);
    	
    	this.startActivity(the_intent);
    }
    
    public class AddButtonTask extends TimerTask {
    	@SuppressWarnings("deprecation")
		public void run() {
    		//if it comes to this, add a button to the text view.
    		Log.e("TIMER","TIMER EVENT FIRING, ADDING BUTTON!");
    		
    		//RelativeLayout layout = (RelativeLayout)findViewById(R.id.welcomelayout);
    		//TextView tv = (TextView)findViewById(R.id.welcometext);
    		
    		//LayoutInflater lf = BaardTERMLauncher.this.getLayoutInflater();
    		//Button button = new Button(layout.getContext());
    		//Button button = (Button)lf.inflate(R.id.cancelbutton,layout);
    		//button.setText("I AM A NEW BUTTON");
    		//button.setVisibility(Button.VISIBLE);
    		//button.layout(100,60,150,110);
    		
    		//layout.removeAllViews();
    		//layout.removeViewInLayout(tv);
    		//layout.removeAllViews();
    		//LayoutParams p = new LayoutParams();
    		//LayoutParams lp = new AbsoluteLayout.LayoutParams(button.getWidth(),button.getHeight(),250,250);
    		
    		
    		//RelativeLayout newlayout = new RelativeLayout(layout.getContext());
    		//layout.addView(button);
    		//layout.invalidate();
    		//BaardTERMLauncher.this.setContentView(layout);
    		
    		
    		
    		
    		//x = 0;
    		//y = 0;
    		
    		buttonaddhandler.sendEmptyMessage(0);
    		
    	}
    }

}
