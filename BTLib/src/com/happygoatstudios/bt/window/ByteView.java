package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;

import com.happygoatstudios.bt.service.Colorizer;
import com.happygoatstudios.bt.window.TextTree.Line;
import com.happygoatstudios.bt.window.TextTree.Unit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;

import com.happygoatstudios.bt.settings.HyperSettings;
import com.happygoatstudios.bt.settings.HyperSettings.LINK_MODE;

public class ByteView extends SurfaceView implements SurfaceHolder.Callback {

	private DrawRunner _runner = null;
	
	private TextTree the_tree = null;
	
	private static float PREF_FONTSIZE = 18;
	private int WINDOW_HEIGHT = 1;
	private int WINDOW_WIDTH = 1;
	public int CALCULATED_LINESINWINDOW;
	private int PREF_LINESIZE = 18;
	private Typeface PREF_FONT = Typeface.MONOSPACE;
	public int CALCULATED_ROWSINWINDOW;
	private double fling_velocity;
	boolean buttondropstarted=false;
	boolean increadedPriority = false;
	
	//private RelativeLayout parent_layout = null;

	private View new_text_in_buffer_indicator = null;

	private Double scrollback = (double)WINDOW_HEIGHT;

	private int debug_mode = 0;

	//private String encoding = "ISO-8859-1";

	private int PREF_LINEEXTRA = 2;
	
	
	
	private LINK_MODE linkMode = LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND;
	private int linkHighlightColor = HyperSettings.DEFAULT_HYPERLINK_COLOR;
	
	Integer selectedColor = new Integer(37);
	Integer selectedBright = new Integer(0);
	Integer selectedBackground = new Integer(60);
	boolean xterm256FGStart = false;
	boolean xterm256BGStart = false;
	boolean xterm256Color = false;
	private Handler buttonaddhandler = null;
	private Handler realbuttonhandler = null;
	
	protected static final int MSG_UPPRIORITY = 200;
	protected static final int MSG_NORMALPRIORITY = 201;
	final static public int MSG_BUTTONDROPSTART = 100;
	final static public int MSG_CLEAR_NEW_TEXT_INDICATOR = 105;
	final static public int MSG_SET_NEW_TEXT_INDICATOR = 106;
	final static public int MSG_SET_NEW_TEXT_INDICATOR_ANIMATED = 107;
	final static public int MSG_DELETEBUTTON = 1040;
	final static public int MSG_REALLYDELETEBUTTON = 1041;
		
	public static final int MESSAGE_ADDTEXT = 0;
	private boolean disableEditing = false;
	
	Animation indicator_on = new AlphaAnimation(1.0f,0.0f);
	Animation indicator_off = new AlphaAnimation(0.0f,0.0f);
	Animation indicator_on_no_cycle = new AlphaAnimation(1.0f,1.0f);
	
	Handler dataDispatch = null;
	EditText input = null;
	
	Object token = new Object(); //token for synchronization.
	
	public ByteView(Context context) {
		super(context);
		getHolder().addCallback(this);
		init();
	}
	
	public ByteView(Context context,AttributeSet attrib) {
		super(context,attrib);
		getHolder().addCallback(this);
		//_runner = new DrawRunner(getHolder(),this,touchLock);
		//Log.e("VIEW","VIEW STARTING UP!");
		//createhandler();
		//this.setZOrderOnTop(true);
		init();
	} 
	
	private void init() {
		the_tree = new TextTree();
		buttonaddhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_UPPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
					break;
				case MSG_NORMALPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					break;
				case MSG_BUTTONDROPSTART:
					Message addbtn = realbuttonhandler.obtainMessage(MainWindow.MESSAGE_ADDBUTTON, bx, by);
					realbuttonhandler.sendMessage(addbtn);
					buttondropstarted = false;
					break;
				case MSG_CLEAR_NEW_TEXT_INDICATOR:
					new_text_in_buffer_indicator.startAnimation(indicator_off);
					indicated = false;
					break;
				case MSG_SET_NEW_TEXT_INDICATOR:
					new_text_in_buffer_indicator.startAnimation(indicator_on_no_cycle);
					indicated = true;
					break;
				case MSG_SET_NEW_TEXT_INDICATOR_ANIMATED:
					new_text_in_buffer_indicator.startAnimation(indicator_on);
					indicated = true;
					break;
				}
			}
		};
		
		Interpolator i = new CycleInterpolator(1);
		indicator_on.setDuration(1000);
		indicator_on.setInterpolator(i);
		indicator_on.setDuration(1000);
		indicator_on.setFillAfter(true);
		indicator_on.setFillBefore(true);
		
		indicator_off.setDuration(1000);
		indicator_off.setFillAfter(true);
		indicator_off.setFillBefore(true);
		
		indicator_on_no_cycle.setDuration(1);
		indicator_on_no_cycle.setFillAfter(true);
		indicator_on_no_cycle.setFillBefore(true);
		
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		WINDOW_HEIGHT = height;
		WINDOW_WIDTH = width;		
		calculateCharacterFeatures(width,height);
		//Log.e("BYTE","SURFACE CHANGED");
		doDelayedDraw(0);
	}
	
	//private int leftOver = 0;
	
	public void calculateCharacterFeatures(int width,int height) {
		
		if(height == 0 && width == 0) {
			return;
		}
		CALCULATED_LINESINWINDOW = (int) (height / PREF_LINESIZE);
		
		//leftOver = height - CALCULATED_LINESINWINDOW*PREF_LINESIZE;
		Paint p = new Paint();
		p.setTypeface(PREF_FONT);
		p.setTextSize(PREF_FONTSIZE);
		int one_char_is_this_wide = (int)Math.ceil(p.measureText("a")); //measure a single character
		CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		
		if(automaticBreaks) {
			this.setLineBreaks(0);
		}
		
		jumpToZero();
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		_runner = new DrawRunner(getHolder(),this,this.token);
		_runner.start();
		while(_runner.threadHandler == null) {
			synchronized(this) {
				try {
					this.wait(50); //make sure the thread handler starts up before we do anything else.
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//Log.e("SERVICE","ON SURFACE CREATED: started draw thread.");
		windowShowing = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		windowShowing = false;
		//Log.e("BYTEVIEW","surfaceViewDestroyed");
		while(retry) {
			try{
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_SHUTDOWN);
				//Log.e("WINDOW","SHUTTING DOWN DRAW THREAD");
				_runner.join();
				//Log.e("WINDOW","SUCCESSFULY SHUT DOWN DRAW THREAD");
				retry = false;
			} catch (InterruptedException e) { }
		}
		
		//the_tree.empty();
	}
	
	boolean finger_down = false;
	int diff_amount = 0;
	public Boolean is_in_touch = false;
	Float start_x = null;
	Float start_y = null;
	MotionEvent pre_event  = null;
	boolean finger_down_to_up = false;
	long prev_draw_time = 0;
	Float prev_y = 0f;
	int bx = 0;
	int by = 0;
	public int touchInLink = -1;
	public boolean onTouchEvent(MotionEvent t) {
		synchronized(token) {
		if(t.getAction() == MotionEvent.ACTION_DOWN) {
			if(!disableEditing) {
				buttonaddhandler.sendEmptyMessageDelayed(MSG_BUTTONDROPSTART, 2500);
			}
			start_x = new Float(t.getX(t.getPointerId(0)));
			start_y = new Float(t.getY(t.getPointerId(0)));
			pre_event = MotionEvent.obtainNoHistory(t);
			fling_velocity = 0.0f;
			finger_down = true;
			finger_down_to_up = false;
			prev_draw_time = 0;
			
			for(int tmpCount=0;tmpCount<linkBoxes.size();tmpCount++) {
				if(linkBoxes.get(tmpCount).getBox().contains((int)(float)start_x,(int)(float)start_y)) {
					touchInLink = tmpCount;
					//Rect b = linkBoxes.get(tmpCount).getBox();
					//Log.e("WINDOW","CLICKED LINK: " + Integer.toString((int) ((b.bottom - b.top) / this.getResources().getDisplayMetrics().density)) + " dips tall");
					buttonaddhandler.removeMessages(MSG_BUTTONDROPSTART);
				}
			}
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
				touchInLink = -1;
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
		//float diff = 0;
		for(int i=0;i<pointers;i++) {
			
			Float y_val = new Float(t.getY(t.getPointerId(i)));
			Float x_val = new Float(t.getX(t.getPointerId(i)));
			bx = x_val.intValue();
			by = y_val.intValue();
			//if(pre_event == null) {
				//diff = 0;
			//} else {
				//diff = y_val - prev_y;	
			//}
			prev_y = y_val;
		}
		
		
		if(t.getAction() == (MotionEvent.ACTION_UP)) {
			
			pre_event = null;
			prev_y = new Float(0);
			buttonaddhandler.removeMessages(ByteView.MSG_BUTTONDROPSTART);
	        
	        //reset the priority
	        increadedPriority = false;
	        _runner.dcbPriority(Process.THREAD_PRIORITY_DEFAULT);
	        

	        pre_event = null;
	        finger_down=false;
	        finger_down_to_up = true;
	         
			if(touchInLink > -1) {
				dataDispatch.sendMessage(dataDispatch.obtainMessage(MainWindow.MESSAGE_LAUNCHURL, linkBoxes.get(touchInLink).getData()));
		        touchInLink = -1;
			}
	        
		}
		
		if(!_runner.threadHandler.hasMessages(ByteView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		}
		return true; //consumes
		
	}
	

	float fling_accel = 200.0f; //(units per sec);
	
	private void calculateScrollBack() {
		//synchronized(the_tree) {
			
			if(prev_draw_time == 0) { //never drawn before
				if(finger_down) {
					scrollback = (double)Math.floor(scrollback + diff_amount);
					if(scrollback < SCROLL_MIN) {
						scrollback = SCROLL_MIN;
					} else {
						if(scrollback >= ((the_tree.getBrokenLineCount() * PREF_LINESIZE))) {
							
							scrollback = (double)((the_tree.getBrokenLineCount() * PREF_LINESIZE));
							
						}
					}
					diff_amount = 0;
				} else {
					if(finger_down_to_up) {
						prev_draw_time = System.currentTimeMillis(); 
						Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
						buttonaddhandler.sendEmptyMessage(ByteView.MSG_UPPRIORITY);
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
						scrollback =  (scrollback + fling_velocity*duration_since_last_frame);
						
					} else if (fling_velocity > 0) {
						
						fling_velocity = fling_velocity - fling_accel*duration_since_last_frame;
						scrollback =  (scrollback + fling_velocity*duration_since_last_frame);
						
					}
					
					if(Math.abs(new Double(fling_velocity)) < 15) {
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						buttonaddhandler.sendEmptyMessage(ByteView.MSG_NORMALPRIORITY);
					}
						
					if(scrollback <= SCROLL_MIN) {
						scrollback = SCROLL_MIN;
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						buttonaddhandler.sendEmptyMessage(ByteView.MSG_NORMALPRIORITY);

						buttonaddhandler.sendEmptyMessage(ByteView.MSG_CLEAR_NEW_TEXT_INDICATOR);
					}
					
					if(scrollback >= ((the_tree.getBrokenLineCount() * PREF_LINESIZE))) {
						//Log.e("WINDOW","UPPER CAP OF THE BUFFER REACHED!");
						scrollback = (double)((the_tree.getBrokenLineCount() * PREF_LINESIZE));
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						buttonaddhandler.sendEmptyMessage(ByteView.MSG_NORMALPRIORITY);
						
					}
				}

				
			}
	}
	
	Paint p = new Paint();

	
	Paint b = new Paint();
	
	Paint linkColor = null;
	private Double SCROLL_MIN = 24d;
	
	ListIterator<TextTree.Line> screenIt = null;// = the_tree.getLines().iterator();
	Iterator<Unit> unitIterator = null;
	private int mLinkBoxHeightMinimum = 20;
	
	public void onDraw(Canvas c) {
		if(linkColor == null) {
			
			linkColor = new Paint();
			linkColor.setAntiAlias(true);
			linkColor.setColor(linkHighlightColor);
		}
		
		linkColor.setColor(linkHighlightColor);
		//try {	
		calculateScrollBack();
		//now 0,0 is the lower left hand corner of the screen, and X and Y both increase positivly.
		c.drawColor(0xFF0A0A0A); //fill with black
		p.setTypeface(PREF_FONT);
		p.setAntiAlias(true);
		p.setTextSize(PREF_FONTSIZE);
		p.setColor(0xFFFFFFFF);
		
		//float char_width = p.measureText("T");
		
		float x = 0;
		float y = 0;
		
		
		//Iterator<TextTree.Unit> u = null;
		boolean stop = false;
		
		//TODO: STEP 0
		//calculate the y position of the first line.
		//float max_y = PREF_LINESIZE*the_tree.getLines().size();
		
		
		//instead of being able to draw from the buttom up like i would have liked.
		//we are going to do the first in hopefully few, really expensive operations.
		
		//TODO: STEP 1
		//noting the current scrollback & window size, calculate the position of the first line of text that we need to draw.
		//float y_position = WINDOW_HEIGHT+PREF_LINESIZE;
		//float line_number = y_position/PREF_LINESIZE;
		
		//TODO: STEP 2
		//new step 2, get an iterator to the start of the scrollback
		
		//get the iterator of the list at the given position.
		//i = the_tree.getLines().listIterator(line_number);
		//use our super cool iterator function.
		//Float offset = 0f;
		IteratorBundle bundle = null;
		boolean gotIt = false;
		int maxTries = 20;
		int tries = 0;
		while(!gotIt && tries <= maxTries) {
			try {
				tries = tries + 1;
				bundle = getScreenIterator(scrollback,PREF_LINESIZE);
				gotIt = true;
				
			} catch (ConcurrentModificationException e) {
				//loop again to get it, continue till you get one.
				synchronized(this) {
					try {
						//Log.e("DRAWRUNNER","CAUGHT CONCURRENT MODIFICATION, tried:" + tries);
						this.wait(5);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		if(!gotIt) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
			return;
		}
		screenIt = bundle.getI();
		y = bundle.getOffset();
		int extraLines = bundle.getExtraLines();
		if(screenIt == null) { return;}
		
		//TODO: STEP 3
		//find bleed.
		boolean bleeding = false;
		int back = 0;
		while(screenIt.hasNext() && !bleeding) {
			
			Line l = screenIt.next();
			back++;
			
			for(Unit u : l.getData()) {
				if(u instanceof TextTree.Color) {
					xterm256Color = false;
					xterm256FGStart = false;
					xterm256BGStart = false;
					for(int i=0;i<((TextTree.Color) u).getOperations().size();i++) {
					//for(Integer o : ((TextTree.Color) u).getOperations()) {
						
						updateColorRegisters(((TextTree.Color) u).getOperations().get(i));
						Colorizer.COLOR_TYPE type = Colorizer.getColorType(((TextTree.Color) u).getOperations().get(i));
						if(type != Colorizer.COLOR_TYPE.NOT_A_COLOR && type != Colorizer.COLOR_TYPE.BACKGROUND && type != Colorizer.COLOR_TYPE.BRIGHT_CODE) {
							bleeding = true;
						}
						
					}
					//bleeding = ((TextTree.Color)u).updateColorRegisters(selectedBright, selectedColor, selectedBackground);
					if(xterm256FGStart) {
						p.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor, xterm256Color));
					} else {//b.setColor(0xFF000000 | Colorizer.getColorValue(0, selectedBackground));
						p.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor, false));
						
					}
					
					b.setColor(0xFF000000);//no not bleed background colors

				}
			}
		}
		
		//TODO: STEP 4
		//advance the iterator back the number of units it took to find a bleed.
		//second real expensive move. In the case of a no color text buffer, it would walk from scroll to end and back every time. USE COLOR 
		while(back > 0) {
			screenIt.previous();
			back--;
		}
		if(screenIt.hasNext()) {
		screenIt.next(); // the bleed/back stuff seems to be messing with my calculation
		//of what the next line is.
		}
		//TODO: STEP 5
		//draw the text, from top to bottom.	
		
		int drawnlines = 0;
		
		boolean doingLink = false;
		StringBuffer currentLink = new StringBuffer();
		linkBoxes.clear();
		//try {
		while(!stop && screenIt.hasPrevious()) {
			//int index = screenIt.previousIndex();
			Line l = screenIt.previous();
		
			//c.drawText(Integer.toString(index)+":"+Integer.toString(drawnlines)+":", x, y, p);
			//x += p.measureText(Integer.toString(index)+":"+Integer.toString(drawnlines)+":");
			unitIterator = l.getIterator();
			while(unitIterator.hasNext()) {
				Unit u = unitIterator.next();
				//p.setColor(color)
				boolean useBackground = false;
				if(b.getColor() != 0xFF000000) {
					useBackground = true;
				}
				
				//if(u instanceof TextTree.Text && !(u instanceof TextTree.WhiteSpace)) {
				if(u instanceof TextTree.Text) {
					if(useBackground) {
						//Log.e("WINDOW","DRAWING BACKGROUND HIGHLIGHT: B:" + Integer.toHexString(b.getColor()) + " P:" + Integer.toHexString(p.getColor()));
						c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, b);
					}
					
					if(((TextTree.Text)u).isLink() || doingLink) {
						if(u instanceof TextTree.WhiteSpace) {
							//DO LINK BOX.
							for(int z=0;z<linkBoxes.size();z++) {
								if(linkBoxes.get(z).getData() == null) {
									linkBoxes.get(z).setData(currentLink.toString());
								}
							}
							currentLink.setLength(0);
							doingLink = false;
						} else {
							doingLink = true;
							currentLink.append(((TextTree.Text)u).getString());
							
							
							Rect r = new Rect();
							r.left = (int) x;
							r.top = (int) (y - p.getTextSize());
							r.right = (int) (x + p.measureText(((TextTree.Text)u).getString()));
							r.bottom = (int) (y+5);
							if(linkMode == LINK_MODE.BACKGROUND) {
								linkColor.setColor(linkHighlightColor);
								c.drawRect(r.left, r.top, r.right, r.bottom, linkColor);
							}
							
							int linkBoxHeightDips = (int) ((r.bottom - r.top) / this.getResources().getDisplayMetrics().density);
							if( linkBoxHeightDips < mLinkBoxHeightMinimum) {
								int additionalAmount = (mLinkBoxHeightMinimum - linkBoxHeightDips)/2;
								if(additionalAmount > 0) {
									r.top -= additionalAmount * this.getResources().getDisplayMetrics().density;
									r.bottom += additionalAmount * this.getResources().getDisplayMetrics().density;
								}
							}
							
							LinkBox linkbox = new LinkBox(null,r);
							linkBoxes.add(linkbox);
							
						}
					}
					if(doingLink) {
						switch(linkMode) {
						case NONE:
							linkColor.setTextSize(p.getTextSize());
							linkColor.setTypeface(p.getTypeface());
							linkColor.setUnderlineText(false);
							linkColor.setColor(p.getColor());
							break;
						case HIGHLIGHT:
							linkColor.setTextSize(p.getTextSize());
							linkColor.setTypeface(p.getTypeface());
							linkColor.setColor(p.getColor());
							linkColor.setUnderlineText(true);
							break;
						case HIGHLIGHT_COLOR:
							linkColor.setTextSize(p.getTextSize());
							linkColor.setTypeface(p.getTypeface());
							linkColor.setColor(linkHighlightColor);
							linkColor.setUnderlineText(true);
							break;
						case HIGHLIGHT_COLOR_ONLY_BLAND:
							
							linkColor.setTextSize(p.getTextSize());
							linkColor.setTypeface(p.getTypeface());
							if(selectedColor == 37) {
								linkColor.setColor(linkHighlightColor);
							} else {
								linkColor.setColor(p.getColor());
							}
							linkColor.setUnderlineText(true);
							break;
						case BACKGROUND:
							linkColor.setTextSize(p.getTextSize());
							linkColor.setTypeface(p.getTypeface());
							linkColor.setUnderlineText(false);
							//calculate the "reverse-most-constrasty-color"
							int counterpart = 0xFF000000 | (linkHighlightColor ^ 0xFFFFFFFF);
							linkColor.setColor(counterpart);
							break;
						default:
							linkColor.setTextSize(p.getTextSize());
							linkColor.setTypeface(p.getTypeface());
							linkColor.setUnderlineText(false);
							linkColor.setColor(linkHighlightColor);
						}
						c.drawText(((TextTree.Text)u).getString(),x,y,linkColor);
						x += p.measureText(((TextTree.Text)u).getString());
						
					} else {
						//p.setUnderlineText(false);
						if(useBackground) {
							//Log.e("WINDOW","DRAWING BACKGROUND TEXT: B:" + Integer.toHexString(b.getColor()) + " P:" + Integer.toHexString(p.getColor()));
						}
						c.drawText(((TextTree.Text)u).getString(),x,y,p);
						x += p.measureText(((TextTree.Text)u).getString());
					}
					
				}
				if(u instanceof TextTree.Color) {
					xterm256Color = false;
					xterm256FGStart = false;
					xterm256BGStart = false;
					for(int i=0;i<((TextTree.Color) u).getOperations().size();i++) {
						updateColorRegisters(((TextTree.Color) u).getOperations().get(i));
					}
					
					if(debug_mode == 2 || debug_mode == 3) {
						p.setColor(0xFF000000 | Colorizer.getColorValue(0, 37,false));
						b.setColor(0xFF000000 | Colorizer.getColorValue(0, 40,false));
					} else {
						if(xterm256FGStart) {
							if(selectedColor == 33) {
								selectedColor = 33;
							}
							p.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor,xterm256Color));
						} else {
							if(!xterm256BGStart) {
								p.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor,false));
							}
						}
						
						if(xterm256BGStart) {
							b.setColor(0xFF000000 | Colorizer.getColorValue(0, selectedBackground,xterm256Color));
						} else {
							b.setColor(0xFF000000 | Colorizer.getColorValue(0, selectedBackground,false));
							
						}
					}
					if(debug_mode == 1 || debug_mode == 2) {
						String str = "";
						try {
							str = new String(((TextTree.Color)u).bin,"ISO-8859-1");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						c.drawText(str,x,y,p);
						x += p.measureText(str);
					}
				}
				if(u instanceof TextTree.NewLine || u instanceof TextTree.Break) {
					if(u instanceof TextTree.NewLine) {
						if(doingLink) {
							for(int z=0;z<linkBoxes.size();z++) {
								if(linkBoxes.get(z).getData() == null) {
									linkBoxes.get(z).setData(currentLink.toString());
								}
							}
							currentLink.setLength(0);
							doingLink = false;
							//REGISTER LINK BOX
						}
					}
					
					y = y + PREF_LINESIZE;
					x = 0;
					drawnlines++;
					if(drawnlines > CALCULATED_LINESINWINDOW + extraLines) {
						stop = true;
					}
				}
			}
			
			l.resetIterator();
		}
		showScroller(c);
		
	}
	
	private ArrayList<LinkBox> linkBoxes = new ArrayList<LinkBox>();
	
	private class LinkBox {
		private String data;
		private Rect box;
		public LinkBox(String link,Rect rect) {
			this.data = link;
			this.box = rect;
		}
		public void setData(String data) {
			this.data = data;
		}
		public String getData() {
			return data;
		}
		
		public Rect getBox() {
			return box;
		}
	}
	
	private Paint scroller_paint = new Paint();
	Rect scrollerRect = new Rect();
	public void showScroller(Canvas c) {
		scroller_paint.setColor(0xFFFF0000);
		
		if(the_tree.getBrokenLineCount() < 1) {
			return; //no scroller to show.
		}
		
		double scrollerSize = 0.0f;
		double scrollerPos = 0.0f;
		double posPercent = 0.0f;
		Float windowPercent = (float)WINDOW_HEIGHT / (the_tree.getBrokenLineCount()*PREF_LINESIZE);
		if(windowPercent > 1) {
			//then we have but 1 page to show
			return;
		} else {
			scrollerSize = windowPercent*WINDOW_HEIGHT;
			posPercent = (scrollback - (WINDOW_HEIGHT/2))/(the_tree.getBrokenLineCount()*PREF_LINESIZE);
			scrollerPos = WINDOW_HEIGHT*posPercent;
			scrollerPos = WINDOW_HEIGHT-scrollerPos;
		}
		
		int blue_value = (int) (-1*255*posPercent + 255);
		int red_value = (int) (255*posPercent);
		int alpha_value = (int) ((255-70)*posPercent+70);
		int final_color = android.graphics.Color.argb(alpha_value, red_value, 100, blue_value);
		scroller_paint.setColor( final_color);
		float density = this.getResources().getDisplayMetrics().density;
		scrollerRect.set(WINDOW_WIDTH-(int)(2*density),(int)(scrollerPos - scrollerSize/2),WINDOW_WIDTH,(int)(scrollerPos + scrollerSize/2));
		
		c.drawRect(scrollerRect, scroller_paint);
		
	}
	
	public class DrawRunner extends Thread {
		private SurfaceHolder _surfaceHolder;
		private ByteView _sv;
		private Object token = null;
		
		public static final int MSG_DRAW = 100;
		public static final int MSG_SHUTDOWN = 101;
		public static final int MESSAGE_EMPTY_TREE = 102;
		
		private Handler threadHandler = null;
		
		public DrawRunner(SurfaceHolder parent,ByteView view,Object synchroObj) {
			_surfaceHolder = parent;
			_sv = view;
			token = synchroObj;
			//lock = drawlock;
		}
		
		
		
		public void dcbPriority(int val) {
			Process.setThreadPriority(val);
		}
		
		
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
								//synchronized(the_tree) {
								synchronized(DrawRunner.this.token) {
									_sv.onDraw(c);
								}
								_surfaceHolder.notify();
								//Log.e("DRAW","DRAWING THE SCREEEEEEN!!!!");
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
						this.removeMessages(MSG_DRAW);
						this.getLooper().quit();
						break;
					case MESSAGE_EMPTY_TREE:
						the_tree.empty();
					default:
						break;
					}
				}
			};
			threadHandler.sendEmptyMessage(MSG_DRAW); //just to get us started.
			Looper.loop();
		}
		
	}

	
	public void setButtonHandler(Handler useme) {
		realbuttonhandler = useme;
	}
	public void setDispatcher(Handler h) {
		dataDispatch = h;
	}
	public void setInputType(EditText t) {
		input = t;
	}
	public void setNewTextIndicator(View fill2) {
		new_text_in_buffer_indicator = fill2;
	}

	public void jumpToZero() {

		synchronized(token) {
			SCROLL_MIN = WINDOW_HEIGHT-(double)(5*ByteView.this.getResources().getDisplayMetrics().density);
			scrollback = SCROLL_MIN;
			fling_velocity=0;
		}
				
	}

	public void doDelayedDraw(int i) {
		if(_runner == null || _runner.threadHandler == null || !_runner.isAlive()) return;
		if(!_runner.threadHandler.hasMessages(ByteView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessageDelayed(DrawRunner.MSG_DRAW,i);
		} else {
			//Log.e("SLICK","VIEW ALREADY HAS DRAW MESSAGES");
		}
	}

	public void setColorDebugMode(int i) {
		debug_mode = i;
		doDelayedDraw(1);
	}

	public void setEncoding(String pEncoding) {
		//encoding = pEncoding;
		synchronized(token) {
			the_tree.setEncoding(pEncoding);
		}
	}

	public void setCharacterSizes(int fontSize, int fontSpaceExtra) {
		PREF_FONTSIZE = fontSize;
		PREF_LINEEXTRA = fontSpaceExtra;
		PREF_LINESIZE = (int) (PREF_FONTSIZE + PREF_LINEEXTRA);
		calculateCharacterFeatures(WINDOW_WIDTH,WINDOW_HEIGHT);
	}

	public void setMaxLines(int maxLines) {
		the_tree.setMaxLines(maxLines);
	}

	public void setFont(Typeface font) {
		PREF_FONT = font;
	}
	
	public Typeface getFont() {
		return PREF_FONT;
	}
	
	
	boolean automaticBreaks = true;
	public void setLineBreaks(Integer i) {
		
		synchronized(token) {
			if(i == 0) {
				if(CALCULATED_ROWSINWINDOW != 0) {
					the_tree.setLineBreakAt(CALCULATED_ROWSINWINDOW);
					//Log.e("BYTE","SET LINE BREAKS TO: " + CALCULATED_ROWSINWINDOW);
				} else {
					the_tree.setLineBreakAt(80);
					//Log.e("BYTE","SET LINE BREAKS TO DEFAULT BECAUSE CALCULATED WAS 0");
				}
				automaticBreaks = true;
			} else {
				the_tree.setLineBreakAt(i);
				automaticBreaks = false;
				//Log.e("BYTE","SET LINE BREAKS TO: " + i);
			}
		
		
			jumpToZero();
		}
		
		
		if(_runner != null && _runner.threadHandler != null) {
			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);

			}
		}
	}
	
	public void setWordWrap(boolean pIn ) {
		
		synchronized(token) {
			the_tree.setWordWrap(pIn);
		//}
		
			jumpToZero();
		
			if(_runner != null && _runner.threadHandler != null) {
				if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
					_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
	
				}
			}
		}
	}
	
	public void setLinkMode(LINK_MODE mode) {
		this.linkMode = mode;
	}
	
	public void setLinkColor(int linkColor) {
		this.linkHighlightColor = linkColor;
	}
	
	public void clearAllText() {
		_runner.threadHandler.sendEmptyMessage(DrawRunner.MESSAGE_EMPTY_TREE);
	}
	
	public void addBytes(byte[] obj,boolean jumpToEnd) {
		
		synchronized(token) {
			addBytesImpl(obj,jumpToEnd);
		}
	}
	public void addBytesImpl(byte[] obj,boolean jumpToEnd) {
		if(obj.length == 0) return;
		
			double old_max = the_tree.getBrokenLineCount() * PREF_LINESIZE;
			try {
				the_tree.addBytesImpl(obj);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			drawingIterator = null;
			
			if(jumpToEnd) {
				scrollback = SCROLL_MIN;
				buttonaddhandler.sendEmptyMessage(MSG_CLEAR_NEW_TEXT_INDICATOR);
			} else {
				if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) {
					scrollback = (double)WINDOW_HEIGHT;
				} else {
					if(scrollback > SCROLL_MIN + PREF_LINESIZE ) {
					//scrollback = oldposition * (the_tree.getBrokenLineCount()*PREF_LINESIZE);
						double new_max = the_tree.getBrokenLineCount()*PREF_LINESIZE;
						scrollback += new_max - old_max;
						//Log.e("BYTE","REPORT: old_max="+old_max+" new_max="+new_max+" delta="+(new_max-old_max)+" scrollback="+scrollback);
						
					} else {
						scrollback = SCROLL_MIN;
					}
				
				}
				if(scrollback > WINDOW_HEIGHT) {
					if(!indicated) {
						if(fling_velocity > 0) {
							//play with no animation
							//new_text_in_buffer_indicator.startAnimation(indicator_on_no_cycle);
							buttonaddhandler.sendEmptyMessage(MSG_SET_NEW_TEXT_INDICATOR);
						} else {
							//new_text_in_buffer_indicator.startAnimation(indicator_on);
							buttonaddhandler.sendEmptyMessage(MSG_SET_NEW_TEXT_INDICATOR_ANIMATED);
							//indicated = true;
						}
						//Log.e("BYTE","REPORTED");
						indicated = true;
					}
				} else {
					//new_text_in_buffer_indicator.startAnimation(indicator_off);
					buttonaddhandler.sendEmptyMessage(ByteView.MSG_CLEAR_NEW_TEXT_INDICATOR);
					indicated = false;
					//indicated = false;
				}
			}
			the_tree.prune();
			
		//}
		
		if(_runner != null && _runner.isAlive()) {
			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);

			}
		}
	}
	
	boolean indicated = false;
	
	private Colorizer.COLOR_TYPE updateColorRegisters(Integer i) {
		if(i == null) return Colorizer.COLOR_TYPE.NOT_A_COLOR;
		
		if(xterm256Color) {
			if(xterm256FGStart) {
				selectedColor = i;
				//xterm256FGStart = false;
				//xterm256Color = false;
			}
			
			if(xterm256BGStart) {
				selectedBackground = i;
				//xterm256BGStart = false;
				//xterm256Color = false;
			}
			
			return null;
		}
		
		Colorizer.COLOR_TYPE type = Colorizer.getColorType(i);
		switch(type) {
		case FOREGROUND:
			selectedColor = i;
			xterm256FGStart = false;
			xterm256BGStart = false;
			xterm256Color = false;
			//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
			//notFound = false;
			break;
		case BACKGROUND:
			//Log.e("SLICK","BACKGROUND COLOR ENCOUNTERED: " + i);
			selectedBackground = i;
			xterm256FGStart = false;
			xterm256BGStart = false;
			xterm256Color = false;
			//bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
			break;
		case ZERO_CODE:
			//Log.e("WINDOW","ZERO CODE ENCOUNTERED");
			selectedBright = 0;
			selectedColor = 37;
			selectedBackground = 40;
			xterm256FGStart = false;
			xterm256BGStart = false;
			xterm256Color = false;
			break;
		case BRIGHT_CODE:
			selectedBright = 1;
			xterm256FGStart = false;
			xterm256BGStart = false;
			xterm256Color = false;
			break;
		case XTERM_256_FG_START:
			xterm256FGStart = true;
			break;
		case XTERM_256_BG_START:
			xterm256BGStart = true;
			break;
		case XTERM_256_FIVE:
			if(xterm256BGStart || xterm256FGStart) {
				xterm256Color = true;
			} else {
				//this would be a "blink" command, but blink sucks, so do nothing.
			}
			break;
		default:
			return Colorizer.COLOR_TYPE.NOT_A_COLOR;
		}
		
		return type;
		//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	
	}
	
	public void setCullExtraneous(boolean pIn) {
		
		synchronized(token) {
			the_tree.setCullExtraneous(pIn);
		}
			
	}
	
	private class IteratorBundle {
		private ListIterator<TextTree.Line> i;
		private Float offset;
		private int extraLines;
		public IteratorBundle(ListIterator<TextTree.Line> pI,double pOffset,int lines) {
			setI(pI);
			setOffset((float)pOffset);
			setExtraLines(lines);
		}
		public void setOffset(Float offset) {
			this.offset = offset;
		}
		public Float getOffset() {
			return offset;
		}
		public void setI(ListIterator<TextTree.Line> i) {
			this.i = i;
		}
		public ListIterator<TextTree.Line> getI() {
			return i;
		}
		public void setExtraLines(int extraLines) {
			this.extraLines = extraLines;
		}
		public int getExtraLines() {
			return extraLines;
		}
		
	}
	ListIterator<Line> drawingIterator = null;
	private IteratorBundle getScreenIterator(double pIn,float pLineSize) {
		float working_h = 0;
		//int position = 0;
		
		//Log.e("BYTE","TREE HAS:" + the_tree.getBrokenLineCount() + " total lines.");
		double pY = pIn;
		double max = the_tree.getBrokenLineCount() * pLineSize;
		if(pY >= max) {
			pY = max;
		}
		//Log.e("BYTE","SCROLLBACK IS:" +pIn);
		int current = 0;
		if(drawingIterator == null) {
			drawingIterator = the_tree.getLines().listIterator();
		} else {
			while(drawingIterator.hasPrevious()) {
				drawingIterator.previous(); //reset to beginning
			}
		}
		
		if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) {
			//calculate how few.
			int under = CALCULATED_LINESINWINDOW-(the_tree.getBrokenLineCount()-1);
			while(drawingIterator.hasNext()) drawingIterator.next();
			//return new IteratorBundle(the_tree.getLines().listIterator(the_tree.getLines().size()),under*pLineSize,0);
			return new IteratorBundle(drawingIterator,under*pLineSize,0);
		}
		
		//double target = Math.floor(pY/pLineSize);
		
		while(drawingIterator.hasNext()) {
			//position = drawingIterator.nextIndex();
			Line l = drawingIterator.next();
			working_h += pLineSize * (1 + l.getBreaks());
			current += 1 + l.getBreaks();
			if(working_h >= pY) {
				double delta = working_h - pY;
				double offset = delta - pLineSize;
				int extra = (int) Math.ceil(delta/pLineSize);
				if(drawingIterator.hasPrevious()) drawingIterator.previous();
				return new IteratorBundle(drawingIterator,-1*offset,extra);
			} else {
				//next line
				//position++;
			}
		}
		
		return new IteratorBundle(drawingIterator,pLineSize,0);
		
	}

	public void setLinksEnabled(boolean hyperLinkEnabled) {
		the_tree.setLinkify(hyperLinkEnabled);
	}

	public boolean windowShowing = false;
	public boolean loaded() {
		
		if(_runner == null || _runner.threadHandler == null || !_runner.isAlive()) {
			if(_runner != null && !_runner.isAlive()) {
				//Log.e("WINDOW","HOLY GOD THE THREAD IS DEAD");
			}
			return false;
		}
		
		return windowShowing;
	}

	public void setDisableEditing(boolean disableEditing) {
		this.disableEditing = disableEditing;
	}

	public boolean isDisableEditing() {
		return disableEditing;
	}
	
	
}
