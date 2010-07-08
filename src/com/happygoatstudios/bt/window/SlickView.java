package com.happygoatstudios.bt.window;

import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.R;
//import com.happygoatstudios.bt.launcher.SlickButton;
import com.happygoatstudios.bt.service.Colorizer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public class SlickView extends SurfaceView implements SurfaceHolder.Callback {

	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	Pattern lastcolordatainline = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m.*\n");
	
	private DrawRunner _runner;
	
	private int WINDOW_WIDTH = 0;
	private int WINDOW_HEIGHT = 0;
	
	
	RelativeLayout parent_layout = null; //for adding buttons.
	EditText input = null; //for supporting buttons.
	Handler dataDispatch = null;
	
	SlickButtonUtilities sbu = new SlickButtonUtilities();
	
	TextView new_text_in_buffer_indicator = null;
	
	private Pattern newline = Pattern.compile("\n");
	private Pattern carriage = Pattern.compile("\\x0D");
	
	private Float scrollback = new Float(0);
	
	private Boolean touchLock = new Boolean(false);
	
	private Boolean drawn = false;
	private Boolean isdrawn = true;
	
	private Typeface PREF_FONT = Typeface.MONOSPACE;
	private int PREF_MAX_LINES = 300;
	public int PREF_FONTSIZE = 18;
	public int PREF_LINESIZE = 20;
	public String PREF_TYPEFACE = "monospace";
	public int CALCULATED_LINESINWINDOW = 20;
	public int CALCULATED_ROWSINWINDOW = 77;
	
	boolean buttondropstarted = false;
	
	public Vector<SlickButton> buttons = new Vector<SlickButton>();
	
	int bx = 0;
	int by = 0;
	
	final static public int MSG_BUTTONDROPSTART = 100;
	final static public int MSG_DELETEBUTTON = 666;
	final static public int MSG_CREATEBUTTON = 102;

	protected static final int MSG_REALLYDELETEBUTTON = 667;

	protected static final int MSG_CREATEBUTTONWITHDATA = 103;
	final static public int MSG_CLEAR_NEW_TEXT_INDICATOR = 105;
	protected static final int MSG_UPPRIORITY = 200;
	protected static final int MSG_NORMALPRIORITY = 201;
	public Handler buttonaddhandler = null;
	public Handler realbuttonhandler = null;
	
	public SlickView(Context context) {
		super(context);
		getHolder().addCallback(this);
		_runner = new DrawRunner(getHolder(),this,touchLock);
		createhandler();
		//this.setZOrderOnTop(true);
	}
	
	public SlickView(Context context,AttributeSet attrib) {
		super(context,attrib);
		getHolder().addCallback(this);
		//_runner = new DrawRunner(getHolder(),this,touchLock);
		//Log.e("VIEW","VIEW STARTING UP!");
		createhandler();
		//this.setZOrderOnTop(true);
	} 
	
	private void createhandler() {
		buttonaddhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_UPPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
					break;
				case MSG_NORMALPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					break;
				/*case MSG_CREATEBUTTON:
					int ix = msg.getData().getInt("X");
					int iy = msg.getData().getInt("Y");
					String text = msg.getData().getString("THETEXT");
					String label = msg.getData().getString("THELABEL");
					
					//Log.e("SLICK","ATTEMPTING TO ADD BUTTON AT XY:" + ix + "|" + iy + "|" + text);
					
					SlickButton bi = new SlickButton(parent_layout.getContext(),ix,iy);
					bi.setText(text);
					bi.setLabel(label);
					
					RelativeLayout.LayoutParams lpi = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
					
					//SlickButton b = new SlickButton(parent_layout.getContext(),bx,by);

					bi.setClickable(true);
					bi.setFocusable(true);
					
					//bi.setText(input.getText().toString());
					bi.setDispatcher(dataDispatch);
					bi.setDeleter(this);
					
					buttons.add(bi);
					int posi = buttons.lastIndexOf(bi);
					
					sbu.addButtonToSelectedSet(bi);
					
					parent_layout.addView(buttons.get(posi),lpi);
					
					break;*/
				case MSG_BUTTONDROPSTART:
					Message addbtn = realbuttonhandler.obtainMessage(BaardTERMWindow.MESSAGE_ADDBUTTON, bx, by);
					realbuttonhandler.sendMessage(addbtn);
					//wait for touchlock
					/*synchronized(touchLock) {
						while(touchLock.booleanValue()) {
							try {
								touchLock.wait();
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
						}
						touchLock = true;
						Log.e("SLICK","TOUCHLOCK TRUE FOR NEW BUTTON CREATION")
					}*/
					/*if(parent_layout == null) {
						return;
					}

					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
				
					SlickButton b = new SlickButton(parent_layout.getContext(),bx,by);

					b.setClickable(true);
					b.setFocusable(true);
					
					b.setText(input.getText().toString());
					b.setDispatcher(dataDispatch);
					b.setDeleter(this);
					
					buttons.add(b);
					int pos = buttons.lastIndexOf(b);
					
					parent_layout.addView(buttons.get(pos),lp);
					parent_layout.invalidate();
					bx = 0;
					by = 0;*/
					buttondropstarted = false;
					

					break;
				/*case MSG_DELETEBUTTON:

					SlickButton the_b = (SlickButton)msg.obj;
					
					ButtonEditorDialog d = new ButtonEditorDialog(parent_layout.getContext(),the_b,this);
					
					d.show();
					
					break;
				case MSG_REALLYDELETEBUTTON:
					RelativeLayout lb = parent_layout;
					SlickButton del_b = (SlickButton)msg.obj;
					lb.removeView(del_b);
					buttons.remove(del_b);
					break;*/
				/*case MSG_CREATEBUTTONWITHDATA:

					
					SlickButton bip = new SlickButton(parent_layout.getContext(),0,0);
					SlickButtonData the_data = new SlickButtonData();
					the_data.setDataFromString((String)msg.obj);
					bip.setData(the_data);
					
					RelativeLayout.LayoutParams lpip = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
					
					bip.setClickable(true);
					bip.setFocusable(true);
					
					//bi.setText(input.getText().toString());
					bip.setDispatcher(dataDispatch);
					bip.setDeleter(this);
					
					buttons.add(bip);
					int posip = buttons.lastIndexOf(bip);
					
					parent_layout.addView(buttons.get(posip),lpip);
					break;*/
				case MSG_CLEAR_NEW_TEXT_INDICATOR:
					//Animation a = new AlphaAnimation(0.0f,0.0f);
					//a.setDuration(0);
					//a.setFillAfter(true);
					//a.setFillBefore(true);
					new_text_in_buffer_indicator.startAnimation(indicator_off);
					
					break;
				}
			}
		};
		
		Interpolator i = new CycleInterpolator(1);
		indicator_on.setDuration(1000);
		indicator_on.setInterpolator(i);
		indicator_on.setFillAfter(true);
		indicator_on.setFillBefore(true);
		
		indicator_off.setDuration(0);
		indicator_off.setFillAfter(true);
		indicator_off.setFillBefore(true);
	}
	
	public void setButtonHandler(Handler useme) {
		realbuttonhandler = useme;
	}
	
	public void setNewTextIndicator(TextView e) {
		new_text_in_buffer_indicator = e;
	}
	
	public void setParentLayout(RelativeLayout l) {
		parent_layout = l;
	}
	
	public void setInputType(EditText t) {
		input = t;
	}
	
	public void setDispatcher(Handler h) {
		dataDispatch = h;
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
		
		WINDOW_HEIGHT = height;
		WINDOW_WIDTH = width;
		
		calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
		
		//calculate the number of rows and columns in this window.
		/*CALCULATED_LINESINWINDOW = (height / PREF_LINESIZE);
		
		Paint p = new Paint();
		p.setTypeface(Typeface.MONOSPACE);
		p.setTextSize(PREF_FONTSIZE);
		int one_char_is_this_wide = (int)Math.ceil(p.measureText("a")); //measure a single character
		CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		*/
		}
	
	public void calculateCharacterFeatures(int width,int height) {
		
		if(height == 0 && width == 0) {
			return;
		}
		CALCULATED_LINESINWINDOW = (height / PREF_LINESIZE);
		Paint p = new Paint();
		p.setTypeface(PREF_FONT);
		p.setTextSize(PREF_FONTSIZE);
		int one_char_is_this_wide = (int)Math.ceil(p.measureText("a")); //measure a single character
		CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		
		if(CALCULATED_ROWSINWINDOW > 0) {
			Log.e("SLICK","surfaceChanged called, calculated" + CALCULATED_LINESINWINDOW + " lines and " + CALCULATED_ROWSINWINDOW + " rows.");
		}
	}
	


	public void surfaceCreated(SurfaceHolder arg0) {
		_runner = new DrawRunner(getHolder(),this,touchLock);
		_runner.setRunning(true);
		_runner.start();
	}
	
	public String getBuffer() {
		StringBuilder build = new StringBuilder();
		for(String line : dlines) {
			build.append(line + "\n");
		}
		return build.toString();
		//return the_original_buffer.toString();
	}
	
	public void setBuffer(String input) {

		//synchronized(isdrawn) {
		//	while(isdrawn == false) {
		//		try {
		//			isdrawn.wait();
		//		} catch (InterruptedException e) {
		//			//Log.e("SLICK","setBuffer interrupted waiting for screen to be done drawing");
		//			e.printStackTrace();
		//		}
		//	}
		//}
		
		synchronized(dlines) {
			dlines.addAll(Arrays.asList(newline.split(input)));
		}
		
		//the_original_buffer.setLength(0);
		//the_buffer.setLength(0);
		//the_original_buffer = new StringBuffer(input);
		//the_buffer = new StringBuffer(betterBreakLines(the_original_buffer,77));
		
		//synchronized(drawn) {
		//	drawn.notify();
		//	drawn = false;
		//}
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		
	}
	
	public void jumpToZero() {
		//call this to scroll back to 0.
		//synchronized(drawn) {
		//	while(!drawn) {
		//		try {
		//			drawn.wait();
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//	}
		//}
		
		/*--------- set scrollBack to 0--------*/
		synchronized(scrollback) {
			scrollback = (float)0.0;
			fling_velocity = 0.0f;
		}
		/*synchronized(drawn) {
			if(drawn) {
				drawn.notify();
				drawn = false;
			}
		}*/
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
 
	}
	
	public void startDrawing() {
		if(_runner != null) {
			_runner.setRunning(true);
			_runner.start();
			//wasRunning = true;
		}
		
		//synchronized(drawn) {
		//	drawn.notify();
		//}
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		
	}
	
	public void stopDrawing() {
		//Log.e("SLICK","Attempted to kill/stop thread at stopDrawing()");
		boolean retry = true;
		_runner.setRunning(false);
		while(retry) {
			try{
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}

	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		

		while(retry) {
			try{
				_runner.setRunning(false);
				//synchronized(drawn) {
				//	drawn.notify();
				//	drawn = false;
				//	_runner.setRunning(false);
				//}
				//_runner.setRunning(false);
				
				
				//synchronized(touchLock) {

				//	if(touchLock) {
				//		touchLock.notify();
				//		touchLock = false;
				//	}
				//}
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_SHUTDOWN);
				
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}
		
	}
	
	
	public void onAnimationStart() {

		super.onAnimationStart();
	}
	
	public void onAnimationEnd() {
		super.onAnimationEnd();
	}
	
	static final int MSG_UTIL_ADDTEXT = 400;
	static final int MSG_UTIL_PROCTOUCH = 401;
	
	
	//StringBuffer the_buffer = new StringBuffer();
	//StringBuffer the_original_buffer = new StringBuffer();
	
	Boolean utilinterlock = false;
	
	
	Animation indicator_on = new AlphaAnimation(1.0f,0.0f);
	Animation indicator_off = new AlphaAnimation(0.0f,0.0f);
	
		
	
	public void addText(String input,boolean jumptoend) {
		
		Matcher carriagerock = carriage.matcher(input);
		
		//Matcher toLines = newline.matcher();
		StringBuffer carriage_free = new StringBuffer(carriagerock.replaceAll(""));
		//PREVIOUS CHARACTER WIDTH = 77
		StringBuffer broken_lines = betterBreakLines(carriage_free,CALCULATED_ROWSINWINDOW);
		//int numlines = countLines(broken_lines.toString());
		
		int size_before = 0;
		int size_after = 0;
		
		synchronized(dlines) {
			//int PREF_MAX_LINES = 200;
				//dlines.removeAllElements();
				//dlines.addAll(Arrays.asList(newline.split(the_buffer)));
				//dlines.
				size_before = dlines.size();
				dlines.addAll(Arrays.asList(newline.split(broken_lines)));
				size_after = dlines.size();
				if(dlines.size() > PREF_MAX_LINES) {
					dlines.removeRange(0,dlines.size() - PREF_MAX_LINES);
				}
				
			}
		
		if(scrollback > 0) {
			synchronized(scrollback) {
			scrollback = scrollback + (size_after-size_before)*PREF_LINESIZE;
			}
			//EditText filler2 = (EditText) parent_layout.findViewById(R.id.filler2);
			//Animation a = new AlphaAnimation(1.0f,0.0f);
			//Interpolator i = new CycleInterpolator(1);
			//a.setDuration(1000);
			//a.setInterpolator(i);
			//a.setFillAfter(true);
			//a.setFillBefore(true);
			new_text_in_buffer_indicator.startAnimation(indicator_on);
			
			
		} else {
			
			
			//Animation a = new AlphaAnimation(0.0f,0.0f);
			//a.setDuration(0);
			//a.setFillAfter(true);
			//a.setFillBefore(true);
			new_text_in_buffer_indicator.startAnimation(indicator_off);
			
		}
		
		//the_buffer.append(broken_lines);
		
		//the_original_buffer.append(carriage_free);
		
		//int chars_per_row = CALCULATED_ROWSINWINDOW;
		//int rows = 25;
		//if(the_buffer.length() > (10*chars_per_row*rows)) {
		//	the_buffer.replace(0, the_buffer.length() - ((10*chars_per_row*rows)), "");
		//}
		//if(the_original_buffer.length() > (10*chars_per_row*rows)) {
		//	the_original_buffer.replace(0, the_original_buffer.length() - ((10*chars_per_row*rows)), "");
		//}
		
		//linetrap = newline.split(the_buffer);

		
		//synchronized(drawn) {
		//	drawn.notify();
		//	drawn = false;
		//}
		
		if(_runner != null) {
				//if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
			//s//ynchronized(_runner) {
			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW))
					_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
				//}
			//}
		}
		
	}
	
	public void setFont(Typeface font) {
		synchronized(dlines) {
			PREF_FONT = font;
			//calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
		}
	}
	
	public void setFontSize(int size) {
		synchronized(dlines) {
			PREF_FONTSIZE = size;
		}
	}
	
	public void setLineSpace(int space) {
		synchronized(dlines) {
			PREF_LINESIZE = PREF_FONTSIZE + space;
		}
	}
	
	public void setMaxLines(int amount) {
		synchronized(dlines) {
			PREF_MAX_LINES = amount;
		}
	}
	
	public void setCharacterSizes(int size,int space) {
		synchronized(dlines) {
			PREF_FONTSIZE = size;
			PREF_LINESIZE = size+space;
			
			calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
		}
	}
	
	StringBuffer sel_color = new StringBuffer(new Integer(0xFFBBBBBB).toString());
	StringBuffer sel_bright = new StringBuffer(new Integer(0).toString()); 
	StringBuffer csegment = new StringBuffer();
	Canvas drawn_buffer = null;
	long prev_draw_time = 0;
	boolean finger_down_to_up = false;
	Matcher toLines = newline.matcher("");
	StringBuffer drawline = new StringBuffer();
	
	String[] linetrap = new String[0];
	BufferVector<String> dlines = new BufferVector<String>();
	@Override
	public void onDraw(Canvas canvas) {
		
		//dlines.addAll(Arrays.asList(linetrap));
		//dlines.get = "foo";
		
		int scrollbacklines = (int)Math.floor(scrollback / (float)PREF_LINESIZE);
		
		synchronized(scrollback) {
		
		if(prev_draw_time == 0) { //never drawn before
			if(finger_down) {
				scrollback = (float)Math.floor(scrollback + diff_amount);
				if(scrollback < 0) {
					scrollback = 0.0f;
				}
				diff_amount = 0;
				
				scrollbacklines = (int)Math.floor((scrollback) / (float)PREF_LINESIZE);

			} else {
				if(finger_down_to_up) {
					prev_draw_time = System.currentTimeMillis(); 
					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_UPPRIORITY);
					finger_down_to_up=false;
				}
			}
		} else {
			
			if(finger_down == true) {

			} else {
				
				long nowdrawtime = System.currentTimeMillis(); 
				
				float duration_since_last_frame = ((float)(nowdrawtime-prev_draw_time)) / 1000.0f; //convert to seconds
				prev_draw_time = System.currentTimeMillis();
				//compute change in velocity: v = vo + at;
				if(fling_velocity < 0) {
					fling_velocity = fling_velocity + fling_accel*duration_since_last_frame;
					scrollback = scrollback + fling_velocity*duration_since_last_frame;
					
				} else if (fling_velocity > 0) {
					
					fling_velocity = fling_velocity - fling_accel*duration_since_last_frame;
					scrollback = scrollback + fling_velocity*duration_since_last_frame;
					
				}
				
				if(Math.abs(new Double(fling_velocity)) < 15) {
					fling_velocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_NORMALPRIORITY);
				}
				
				/*if(scrollback.intValue() / PREF_LINESIZE < 1) {
					prev_draw_time = 0;
					fling_velocity = 0;
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_CLEAR_NEW_TEXT_INDICATOR);
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				}*/
				
					
				if(scrollback <= 0) {
					scrollback = 0.0f;
					fling_velocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					buttonaddhandler.sendEmptyMessage(SlickView.MSG_NORMALPRIORITY);

					buttonaddhandler.sendEmptyMessage(SlickView.MSG_CLEAR_NEW_TEXT_INDICATOR);
				}
			
			
				scrollbacklines = (int)Math.floor(scrollback / (float)PREF_LINESIZE);
			}

			
		}
		}
		
		synchronized(dlines) {
		int remainder = (int) (canvas.getHeight() - (CALCULATED_LINESINWINDOW*PREF_LINESIZE)) - 6 + (int)Math.floor(scrollback % PREF_LINESIZE);
		if(remainder < 0) {
			//Log.e("SLICK","WE HAVE A PROBLEM WITH WINDOW SIZE");
		}
		
		//first clear the background
		canvas.drawColor(0xFF0A0A0A); //fill with black
		//canvas.drawColor(0xFF333333); //fill with grey
        Paint opts = new Paint();
        opts.setAlpha(0);
        opts.setAntiAlias(true);
        //opts.setDither(true);
        opts.setARGB(255, 255, 255, 255);
        opts.setTextSize(PREF_FONTSIZE);
        
        //opts.setStyle(Style.)
        opts.setTypeface(PREF_FONT);

        //Matcher toLines = newline.matcher(the_buffer.toString());
        //toLines.reset(the_buffer); 
        //StringBuffer line = new StringBuffer();
        //drawline.setLength(0);
        
        //linetrap = null;
        //linetrap = newline.split(the_buffer);
        //linetrap.clear();
        //linetrap.removeAllElements();
        //linetrap.
        //Log.e("SLICK","Buffer contains: " + lines.length + " lines.");
        //if(linetrap.length < 1) {
       // 	return;
        //}
        
       
        
        if(dlines.size() < 1) { return; }
        
        
        int currentline = 1;
        
        //count lines
       // int numlines = countLines();
        
        //
        
        int numlines = dlines.size();
        int maxlines = CALCULATED_LINESINWINDOW; 
        
        
        
        int startDrawingAtLine = 1;
        if(numlines > maxlines) {
        	startDrawingAtLine = numlines - maxlines - scrollbacklines + 1;
        } else {
        	startDrawingAtLine = 1;
        }
        
       
        boolean endonnewline = false;
        
        int startpos = startDrawingAtLine -2;
        if(startpos < 0) {
        	startpos = 0;
        }
        
        int endpos = startDrawingAtLine + maxlines;
        if(endpos > dlines.size()) {
        	endpos = dlines.size();
        }
        
        for(int i=startpos;i<endpos;i++) {
        	//Log.e("SLICK","Drawing line:" + i + ":" + lines[i]);
        	int screenpos = i - startDrawingAtLine + 2;
    		int y_position =  ((screenpos*PREF_LINESIZE)+remainder);
    		Matcher colormatch = colordata.matcher(dlines.get(i));
    		//Log.e("SLICK","Drawing line:" + i + ":" +":"+y_position +":" + lines[i]);
    		
    		float x_position = 0;
    		
    		boolean colorfound = false;
    		
    		while(colormatch.find()) {
    			colorfound = true;
    			colormatch.appendReplacement(csegment, "");
    			
    			//get color data
    			//int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
    			int color = Colorizer.getColorValue(sel_bright, sel_color);
    			if(color == 0) {
    				//Log.e("SLICK","COLORLOOKUP RETURNED 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
    			}
    			opts.setColor(0xFF000000 | color);
    			
    			//canvas.drawText(csegment.toString(), x_position, y_position, opts);
    			canvas.drawText(csegment, 0, csegment.length(), x_position, y_position, opts);
    			//x_position = x_position + opts.measureText(csegment.toString());
    			x_position = x_position + opts.measureText(csegment,0,csegment.length());
    			//opts.mea
    			csegment.setLength(0);
    			
    			sel_bright.setLength(0);
    			sel_color.setLength(0);
    			sel_bright.append((colormatch.group(2) == null) ? "0" : colormatch.group(2));
    			sel_color.append(colormatch.group(3));
    			if(sel_color.toString().equalsIgnoreCase("0")) {
    				//Log.e("SLICK","COLOLPARSE GOT 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
    				sel_color.setLength(0);
    				sel_color.append("37");
    			}
    		}
    		if(colorfound) {
    			//int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
    			int color = Colorizer.getColorValue(sel_bright, sel_color);
    			opts.setColor(0xFF000000 | color);
    			colormatch.appendTail(csegment);
    			//canvas.drawText(csegment.toString(), x_position, y_position, opts);
    			canvas.drawText(csegment, 0, csegment.length(), x_position, y_position, opts);
    			csegment.setLength(0);
    		}
    		
    		if(!colorfound) {
    			canvas.drawText(dlines.get(i), 0, y_position , opts);
    		}
        }
        }
        
        /*while(toLines.find()) {
        	toLines.appendReplacement(drawline, "");
        	
        	//Matcher lastcolor = lastcolordatainline.matcher(drawline.toString() + "\n");
        	
        	//if(lastcolor.find()) {
        		
        	//}
        	
        	if(currentline >= startDrawingAtLine-2 && currentline <= (startDrawingAtLine + maxlines)) {
        		
        		int screenpos = currentline - startDrawingAtLine + 1;
        		int y_position =  ((screenpos*PREF_LINESIZE)+remainder);
        		Matcher colormatch = colordata.matcher(drawline);
        		
        		
        		float x_position = 0;
        		
        		boolean colorfound = false;
        		
        		while(colormatch.find()) {
        			colorfound = true;
        			colormatch.appendReplacement(csegment, "");
        			
        			//get color data
        			//int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
        			int color = Colorizer.getColorValue(sel_bright, sel_color);
        			if(color == 0) {
        				//Log.e("SLICK","COLORLOOKUP RETURNED 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
        			}
        			opts.setColor(0xFF000000 | color);
        			
        			canvas.drawText(csegment.toString(), x_position, y_position, opts);
        			x_position = x_position + opts.measureText(csegment.toString());
        			csegment.setLength(0);
        			
        			sel_bright.setLength(0);
        			sel_color.setLength(0);
        			sel_bright.append((colormatch.group(2) == null) ? "0" : colormatch.group(2));
        			sel_color.append(colormatch.group(3));
        			if(sel_color.toString().equalsIgnoreCase("0")) {
        				//Log.e("SLICK","COLOLPARSE GOT 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
        				sel_color.setLength(0);
        				sel_color.append("37");
        			}
        		}
        		if(colorfound) {
        			int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
        			opts.setColor(0xFF000000 | color);
        			colormatch.appendTail(csegment);
        			canvas.drawText(csegment.toString(), x_position, y_position, opts);
        			csegment.setLength(0);
        		}
        		
        		if(!colorfound) {
        			canvas.drawText(drawline.toString(), 0, y_position , opts);
        		}
        		
        		
        		
         
        	}
        	
        	//line = new StringBuffer();
        	drawline.setLength(0);
        	currentline++;
        	if(toLines.end() == the_buffer.length()) {
        		endonnewline = true;
        	}
        	
        }
        if(!endonnewline) {
        	
        	toLines.appendTail(drawline);
        	int screenpos = currentline - startDrawingAtLine + 1;
    		int y_position =  ((screenpos*PREF_LINESIZE)+remainder);
    		opts.setColor(0xFF00CCCC);
    		Matcher colormatch = colordata.matcher(drawline);
    		
    		
    		float x_position = 0;
    		
    		boolean colorfound = false;
    		
    		while(colormatch.find()) {
    			colorfound = true;
    			colormatch.appendReplacement(csegment, "");
    			
    			//get color data
    			int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
    			opts.setColor(0xFF000000 | color);
    			
    			canvas.drawText(csegment.toString(), x_position, y_position, opts);
    			x_position = x_position + opts.measureText(csegment.toString());
    			csegment.setLength(0);
    			
    			sel_bright.setLength(0);
    			sel_color.setLength(0);
    			sel_bright.append((colormatch.group(2) == null) ? "0" : colormatch.group(2));
    			sel_color.append(colormatch.group(3));
    		}
    		if(colorfound) {
    			int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
    			opts.setColor(0xFF000000 | color);
    			colormatch.appendTail(csegment);
    			canvas.drawText(csegment.toString(), x_position, y_position, opts);
    			csegment.setLength(0);
    		}
    		
    		if(!colorfound) {
    			canvas.drawText(drawline.toString(), 0, y_position , opts);
    		}
    		drawline.setLength(0);
        	currentline++;		
        	
        }*/
        
        //release the lock
        //drawn_buffer = canvas;
	}
	
	boolean finger_down = false;
	int diff_amount = 0;
	public Boolean is_in_touch = false;
	
	public boolean onTouchEvent(MotionEvent t) {
		
		synchronized(scrollback) {
		if(t.getAction() == MotionEvent.ACTION_DOWN) {
			buttonaddhandler.sendEmptyMessageDelayed(MSG_BUTTONDROPSTART, 2500);
			start_x = new Float(t.getX(t.getPointerId(0)));
			start_y = new Float(t.getY(t.getPointerId(0)));
			pre_event = MotionEvent.obtainNoHistory(t);
			fling_velocity = 0.0f;
			finger_down = true;
			finger_down_to_up = false;
			prev_draw_time = 0;
		}
		
		if(!increadedPriority) {
			_runner.dcbPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
			//_runner.
			increadedPriority = true;
		}
		
		if(t.getAction() == MotionEvent.ACTION_MOVE) {
			

			//compute distance;
			Float now_x = new Float(t.getX(t.getPointerId(0)));
			Float now_y = new Float(t.getY(t.getPointerId(0)));
			double distance = Math.sqrt(Math.pow(now_x-start_x, 2) + Math.pow(now_y-start_y,2));
			if(distance > 30) {
				buttonaddhandler.removeMessages(MSG_BUTTONDROPSTART);
			}
			

			float thentime = pre_event.getEventTime();
			float nowtime = t.getEventTime();
			
			float time = (nowtime - thentime) / 1000.0f; //convert to seconds
			
			//float prev_y = t.getHistoricalY(t.getPointerId(0),history-1);
			float prev_y = pre_event.getY(t.getPointerId(0));
			float dist = now_y - prev_y;
			diff_amount = (int)dist;
			
			float velocity = dist / time;
			float MAX_VELOCITY = 700;
			if(Math.abs(velocity) > MAX_VELOCITY) {
				if(velocity > 0) {
					velocity = MAX_VELOCITY;
				} else {
					velocity = MAX_VELOCITY * -1;
				}
			}
			fling_velocity = velocity;
			//Log.e("SLICK","MOTIONEVENT HISTORICAL Y: "+new Float(dist).toString() + " TIME: " + new Float(time).toString() + " VEL: " + new Float(velocity));
			
			
			if(Math.abs(diff_amount) > 5) {
				
				pre_event = MotionEvent.obtainNoHistory(t);
			}
			

		}
		
		int pointers = t.getPointerCount();
		//int the_last_pointer = t.getPointerId(0);
		float diff = 0;
		for(int i=0;i<pointers;i++) {
			
			Float y_val = new Float(t.getY(t.getPointerId(i)));
			Float x_val = new Float(t.getX(t.getPointerId(i)));
			bx = x_val.intValue();
			by = y_val.intValue();
			if(pre_event == null) {
				diff = 0;
			} else {
				diff = y_val - prev_y;	
			}
			
			//Log.e("SLICK","SLICK TOUCH AT PY:" + prev_y + " Y:" + y_val + " P:" + i + " DIF:" + diff);
			prev_y = y_val;
		}
		
		
		if(t.getAction() == (MotionEvent.ACTION_UP)) {
			
			pre_event = null;
			prev_y = new Float(0);
			buttonaddhandler.removeMessages(SlickView.MSG_BUTTONDROPSTART);
	        
	        //reset the priority
	        increadedPriority = false;
	        _runner.dcbPriority(Process.THREAD_PRIORITY_DEFAULT);
	        

	        pre_event = null;
	        finger_down=false;
	        finger_down_to_up = true;
	        
		}
		
        /*synchronized(drawn) {
        	if(drawn.booleanValue()) {
        		drawn.notify();
        		drawn = false;
        	} 
		}*/
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		}
		return true; //consumes
		
	}


	public int measuredX = 0;
	public int measuredY = 0;
	
	public void onMeasure(int wSpec,int hSpec) {
		int provided_x = 0;
		int provided_y = 0;
		switch(MeasureSpec.getMode(wSpec)) {
			case MeasureSpec.AT_MOST:
				
				provided_x = MeasureSpec.getSize(wSpec);
				//Log.e("SLICK","MEASURING WIDTH: AT MOST" + provided_x);
				break;
			case MeasureSpec.EXACTLY:
				
				provided_x = MeasureSpec.getSize(wSpec);
				//Log.e("SLICK","MEASURING WIDTH: EXACTLY" + provided_x);
				break;
			case MeasureSpec.UNSPECIFIED:
				
				provided_x = MeasureSpec.getSize(wSpec);
				//Log.e("SLICK","MEASURING WIDTH: UNSPECIFIED" + provided_x);
				break;
			default:
				break;
		}
		
		switch(MeasureSpec.getMode(hSpec)) {
		case MeasureSpec.AT_MOST:
			
			provided_y = MeasureSpec.getSize(hSpec);
			//Log.e("SLICK","MEASURING HEIGHT: AT MOST" + provided_y);
			break;
		case MeasureSpec.EXACTLY:
			
			provided_y = MeasureSpec.getSize(hSpec);
			//Log.e("SLICK","MEASURING HEIGHT: EXACTLY" + provided_y);
			break;
		case MeasureSpec.UNSPECIFIED:
			
			provided_y = MeasureSpec.getSize(hSpec);
			//Log.e("SLICK","MEASURING HEIGHT: UNSPECIFIED" + provided_y);
			break;
		default:
			break;
	}
		
		this.setMeasuredDimension(MeasureSpec.getSize(wSpec), MeasureSpec.getSize(hSpec));
	}
	
	
	
	MotionEvent pre_event = null;
	Float prev_y = new Float(0.1);
	boolean increadedPriority = false;
	float start_x = 0;
	float start_y = 0;
	float fling_velocity;
	float fling_accel = 200.0f; //(units per sec);
	float extra_accel = 0.0f;
	
	StringBuffer bholder = new StringBuffer();
	StringBuffer bline = new StringBuffer();
	StringBuffer betterBreakLines(StringBuffer input,int char_limit) {
		bholder.setLength(0);
		bline.setLength(0);
		
		Matcher l = newline.matcher(input);
		boolean found = false;
		while(l.find()) {
			l.appendReplacement(bline,"");
			found = true;
			bholder.append(splitLine(bline,char_limit) + "\n");
			bline.setLength(0);
		}
		if(found) {
			//handle tail (prompt)
			bline.setLength(0);
			l.appendTail(bline);
			bholder.append(splitLine(bline,char_limit));
		} else {
			//handle input that has no line breaks.
			bholder.append(splitLine(input,char_limit));
		}
		
		return bholder;
	}
	
	StringBuffer holder = new StringBuffer();
	StringBuffer segment = new StringBuffer();
	StringBuffer line = new StringBuffer();
	StringBuffer enddata = new StringBuffer();
	
	StringBuffer splitLine(StringBuffer input,int char_limit) {
		holder.setLength(0);
		segment.setLength(0);
		line.setLength(0);
		enddata.setLength(0);
		
		Matcher colormatch = colordata.matcher(input);
		
		boolean found = false;
		int normalchars = 0;
		while(colormatch.find()) {
			found = true;
			colormatch.appendReplacement(segment, colormatch.group());
			normalchars += segment.length() - colormatch.group().length();
			if(normalchars > char_limit) {
				int chars_already_drawn = normalchars - (segment.length() - colormatch.group().length());
				while(normalchars > char_limit) {
					//find out how many characters we are over.
					int leftover = normalchars-char_limit;
					int chopto = (segment.length() - colormatch.group().length()) - leftover;
					if(leftover > char_limit) {
						leftover = char_limit;
						chopto = char_limit - chars_already_drawn;
					}
					
					//int chopto = (segment.length() - colormatch.group().length()) - leftover;
					holder.append(segment.subSequence(0, chopto) + "\n");
					segment.replace(0, chopto,"");
					normalchars = segment.length() - colormatch.group().length();
					chars_already_drawn = 0;
				}
				if(segment.length() > 0) {
					holder.append(segment);
					normalchars = segment.length() - colormatch.group().length();
					if(normalchars < 0) {
						normalchars = 0;
					}
				}
			} else {
				//append the segment and zero it out
				holder.append(segment);
			}
			segment.setLength(0);
		}
		if(found) {
			//append tail stuff
			//breaking in the process
			line.setLength(0);
			colormatch.appendTail(line);

			if(line.length() > char_limit - normalchars) {
				while(line.length() > char_limit - normalchars) {
					holder.append(line.subSequence(0, char_limit - normalchars) + "\n");
					line.replace(0, char_limit-normalchars, "");
					normalchars = 0;
				}
				if(line.length() > 0) {
					holder.append(line);
				}
			} else {
				holder.append(line);
			}
			
			
			//holder.append(splitLine(line,77));
			//line.setLength(0);
			//colormatch.appendTail(holder);
		} else {
			//no colors found, perform normal break;
			line.append(input);
			if(line.length() > char_limit) {
				while(line.length() > char_limit) {
					holder.append(line.subSequence(0, char_limit) + "\n");
					line.replace(0, char_limit, "");
				}
				if(line.length() > 0) {
					holder.append(line);
				}
			} else {
				holder.append(line);
			}
		}
		
		
		
		return holder;
		
	}
	
	
	/*public StringBuffer prompt_string = new StringBuffer();
	public StringBuffer garbage_string = new StringBuffer();
	public int countLines() {
		Matcher toLines = newline.matcher(the_buffer.toString());
		
		boolean endonnewline = false;
		int found = 0;
		while(toLines.find()) {
			found++;
			toLines.appendReplacement(garbage_string, "");
			garbage_string.setLength(0);
			if(toLines.end() == the_buffer.length()) {
				//ended on a new line
				endonnewline = true;
				//toLines.appendReplacement()
			}
		}
		if(!endonnewline) {
			//make sure to include the last, un-newline-terminated string, which should be the prompt
			found++;
			String prev_prompt = prompt_string.toString();
			prompt_string.setLength(0); //clear the prompt
			toLines.appendTail(prompt_string);
			if(!prev_prompt.equals(prompt_string.toString())) {
				//Log.e("SLICK","NEW PROMT:" + prompt_string.toString());
			}
			
		}
		
		return found;
	}
	
	public int countLines(String input) {
		Matcher toLines = newline.matcher(input);
		
		int found = 0;
		boolean endonnewline = false;
		while(toLines.find()) {
			toLines.appendReplacement(garbage_string, "");
			found++;
			if(toLines.end() == input.length()) {
				endonnewline = true;
				Log.e("SLICK","Line count ended on a new line");
			}
		}
		if(!endonnewline) {
			found++;
			Log.e("SLICK","Line count not ended on a new line");
		}
		
		garbage_string.setLength(0);
		
		return found;
	}*/
	
	public class DrawRunner extends Thread {
		private SurfaceHolder _surfaceHolder;
		private SlickView _sv;
		private boolean running = false;
		private boolean paused = false;
		private Boolean lock = null;
		
		public static final int MSG_DRAW = 100;
		public static final int MSG_SHUTDOWN = 101;
		
		private Handler threadHandler = null;
		
		public DrawRunner(SurfaceHolder parent,SlickView view,Boolean drawlock) {
			_surfaceHolder = parent;
			_sv = view;
			lock = drawlock;
		}
		
		
		
		public void dcbPriority(int val) {
			Process.setThreadPriority(val);
		}
		
		public void setRunning(boolean val) {
			//Log.e("SLICK","THREAD ATTEMPTED TO SET THE RUNNING VALUE");
			running = val;
		}
		
		public void setPause() {
			paused = true;
		}
		
		public void setResume() {
			paused = false;
		}
		
		@Override
		public void run() {
			Looper.prepare();
			
			threadHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch(msg.what) {
					case MSG_DRAW:
						Canvas c = null;
						try{
							c = _surfaceHolder.lockCanvas(null);
							synchronized(_surfaceHolder) {
								_sv.onDraw(c);
								_surfaceHolder.notify();
							}
						} finally { 
							if(c != null) {
								_surfaceHolder.unlockCanvasAndPost(c);
							}
						}
						if(Math.abs(fling_velocity) > 0.0f) {
							this.sendEmptyMessageDelayed(MSG_DRAW, 3); //throttle myself, just a little bit.
						}
						break;
					case MSG_SHUTDOWN:
						this.getLooper().quit();
						break;
					default:
						break;
					}
				}
			};
			threadHandler.sendEmptyMessage(MSG_DRAW); //just to get us started.
			//threadHandler.getLooper();
			Looper.loop();
			
			//Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
			/*Canvas c;
			//Log.e("SLICK","VIEW THREAD RUNNING");
			while(running) {
				//if(!paused) {
					c = null;
					
					synchronized(drawn) {
						while(drawn && fling_velocity == 0.0f) {
							try {
								//Log.e("SLICK","ALREADY DRAWN VIEW, WAITING FOR EVENT TO RESUME!");
								drawn.wait();
								//Log.e("SLICK","WOKE FROM METHOD CALLING drawn.notify()");
							} catch (InterruptedException e) {
								Log.e("SLICK","Got interrupted while waiting for draw event.");
								e.printStackTrace();
								
							}
						}
						drawn = true;
						synchronized(isdrawn) {
							isdrawn = false;
						}
					}
						try{
							c = _surfaceHolder.lockCanvas(null);
							synchronized(_surfaceHolder) {
								_sv.onDraw(c);
								_surfaceHolder.notify();
							}
						} finally { 
							if(c != null) {
								_surfaceHolder.unlockCanvasAndPost(c);
							}
						}
					
					//synchronized(drawn) {
						//drawn.notify();
						//drawn = true;
					//}
					synchronized(isdrawn) {
						isdrawn.notify();
						isdrawn = true;
					}
			}*/
		}
		
	}

}
