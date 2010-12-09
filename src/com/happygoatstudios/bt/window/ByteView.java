package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.ListIterator;

import com.happygoatstudios.bt.service.Colorizer;
import com.happygoatstudios.bt.window.SlickView.DrawRunner;
import com.happygoatstudios.bt.window.ttree.TextTree;
import com.happygoatstudios.bt.window.ttree.TextTree.Line;
import com.happygoatstudios.bt.window.ttree.TextTree.Unit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ByteView extends SurfaceView implements SurfaceHolder.Callback {

	private DrawRunner _runner = null;
	
	private TextTree the_tree = null;
	
	private static final float PREF_FONTSIZE = 18;
	private int WINDOW_HEIGHT = 1;
	private int WINDOW_WIDTH = 1;
	public int CALCULATED_LINESINWINDOW;
	private int PREF_LINESIZE = 18;
	private Typeface PREF_FONT = Typeface.MONOSPACE;
	public int CALCULATED_ROWSINWINDOW;
	private double fling_velocity;
	boolean buttondropstarted=false;
	boolean increadedPriority = false;
	
	private RelativeLayout parent_layout = null;

	private TextView new_text_in_buffer_indicator = null;

	private Float scrollback = new Float(20*PREF_LINESIZE);

	private int debug_mode = 1;

	private String encoding = "ISO-8859-1";

	private int PREF_LINEEXTRA = 2;
	
	int selectedColor = 37;
	int selectedBright = 0;
	int selectedBackground = 60;
	private Handler buttonaddhandler = null;
	private Handler realbuttonhandler = null;
	
	protected static final int MSG_UPPRIORITY = 200;
	protected static final int MSG_NORMALPRIORITY = 201;
	final static public int MSG_BUTTONDROPSTART = 100;
	final static public int MSG_CLEAR_NEW_TEXT_INDICATOR = 105;

	public static final int MESSAGE_ADDTEXT = 0;
	
	Animation indicator_on = new AlphaAnimation(1.0f,0.0f);
	Animation indicator_off = new AlphaAnimation(0.0f,0.0f);
	
	Handler dataDispatch = null;
	EditText input = null;
	
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
	}
	private Handler addTextHandler = new AddTextHandler();
	private class AddTextHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_ADDTEXT:
				addBytesImpl((byte[])msg.obj);
				break;
			default:
				break;
			}
		}

		
	}

	private void addBytesImpl(byte[] obj) {
		the_tree.addBytes(obj);
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		WINDOW_HEIGHT = height;
		WINDOW_WIDTH = width;		
		calculateCharacterFeatures(width,height);
	}
	
	public void calculateCharacterFeatures(int width,int height) {
		
		if(height == 0 && width == 0) {
			return;
		}
		CALCULATED_LINESINWINDOW = (int) (height / PREF_LINESIZE);
		Paint p = new Paint();
		p.setTypeface(PREF_FONT);
		p.setTextSize(PREF_FONTSIZE);
		int one_char_is_this_wide = (int)Math.ceil(p.measureText("a")); //measure a single character
		CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		
		//if(CALCULATED_ROWSINWINDOW > 0) {
			//Log.e("SLICK","surfaceChanged called, calculated" + CALCULATED_LINESINWINDOW + " lines and " + CALCULATED_ROWSINWINDOW + " rows.");
		//}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		_runner = new DrawRunner(getHolder(),this);
		_runner.setRunning(true);
		_runner.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		

		while(retry) {
			try{
				_runner.setRunning(false);
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_SHUTDOWN);
				
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}
	}
	
	boolean finger_down = false;
	int diff_amount = 0;
	public Boolean is_in_touch = false;
	Float start_x = null;
	Float start_y = null;
	MotionEvent pre_event  = null;
	boolean finger_down_to_up = false;
	int prev_draw_time = 0;
	Float prev_y = 0f;
	int bx = 0;
	int by = 0;
	public boolean onTouchEvent(MotionEvent t) {
		Log.e("BYTE","TOUCH EVENT");
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
			buttonaddhandler.removeMessages(SlickView.MSG_BUTTONDROPSTART);
	        
	        //reset the priority
	        increadedPriority = false;
	        _runner.dcbPriority(Process.THREAD_PRIORITY_DEFAULT);
	        

	        pre_event = null;
	        finger_down=false;
	        finger_down_to_up = true;
	        
		}
		
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
		}
		}
		return true; //consumes
		
	}
	
	
	
	Paint p = new Paint();

	
	Paint b = new Paint();
	Paint breakcolor = new Paint();
	public void onDraw(Canvas c) {
		//Matrix m = c.getMatrix();
		
		//m.setTranslate(0, WINDOW_HEIGHT-5);
		//m.setScale(1, -1);
		synchronized(the_tree) {
		//c.setMatrix(m);
		//now 0,0 is the lower left hand corner of the screen, and X and Y both increase positivly.
		c.drawColor(0xFF0A0A0A); //fill with black
		p.setTypeface(Typeface.MONOSPACE);
		p.setAntiAlias(true);
		p.setTextSize(PREF_LINESIZE);
		p.setColor(0xFFFFFFFF);
		
		float char_width = p.measureText("T");
		
		float x = 0;
		float y = 0;
		
		ListIterator<TextTree.Line> i = null;// = the_tree.getLines().iterator();
		//Iterator<TextTree.Unit> u = null;
		boolean stop = false;
		
		//TODO: STEP 0
		//calculate the y position of the first line.
		//float max_y = PREF_LINESIZE*the_tree.getLines().size();
		
		
		//instead of being able to draw from the buttom up like i would have liked.
		//we are going to do the first in hopefully few, really expensive operations.
		
		//TODO: STEP 1
		//noting the current scrollback & window size, calculate the position of the first line of text that we need to draw.
		float y_position = WINDOW_HEIGHT;
		float line_number = y_position/PREF_LINESIZE;
		
		//TODO: STEP 2
		//new step 2, get an iterator to the start of the scrollback
		
		//get the iterator of the list at the given position.
		//i = the_tree.getLines().listIterator(line_number);
		//use our super cool iterator function.
		Float offset = 0f;
		IteratorBundle bundle = getScreenIterator(y_position,PREF_LINESIZE);
		i = bundle.getI();
		y = bundle.getOffset();
		if(i == null) {Log.e("BYTE","CAN'T ITERATE, NO ITERATOR!"); return;}
		
		Paint z = new Paint();
		z.setColor(0xFF0000FF);
		c.drawLine(0, y, WINDOW_WIDTH, y, z);
		z.setColor(0xFFFF0000);
		//TODO: STEP 3
		//find bleed.
		boolean bleeding = false;
		int back = 0;
		while(i.hasNext() && !bleeding) {
			
			Line l = i.next();
			back++;
			
			for(Unit u : l.getData()) {
				if(u instanceof TextTree.Color) {
					bleeding = true;
					for(Integer o : ((TextTree.Color) u).getOperations()) {
						updateColorRegisters(o);
					}
					p.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
					//b.setColor(0xFF000000 | Colorizer.getColorValue(0, selectedBackground));
					b.setColor(0xFF000000);//no not bleed background colors
					
				}
			}
		}
		
		//TODO: STEP 4
		//advance the iterator back the number of units it took to find a bleed.
		//second real expensive move. In the case of a no color text buffer, it would walk from scroll to end and back every time. USE COLOR 
		while(back > 0) {
			i.previous();
			back--;
		}
		
		//TODO: STEP 5
		//draw the text, from top to bottom.	
		int drawnlines = 0;
		while(!stop && i.hasPrevious()) {
			Line l = i.previous();
			
			c.drawText(Integer.toString(drawnlines)+":", x, y, p);
			x += p.measureText(Integer.toString(drawnlines)+":");
			for(Unit u : l.getData()) {
				//p.setColor(color)
				boolean useBackground = false;
				if(b.getColor() != 0xFF000000) {
					useBackground = true;
				}
				
				if(u instanceof TextTree.Text) {
					if(useBackground) {
						c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, b);
					}
					c.drawText(((TextTree.Text)u).getString(),x,y,p);
					x += p.measureText(((TextTree.Text)u).getString());
				}
				if(u instanceof TextTree.Color) {
					for(Integer o : ((TextTree.Color)u).getOperations()) {
						updateColorRegisters(o);
					}
					if(debug_mode == 2 || debug_mode == 3) {
						p.setColor(0xFF000000 | Colorizer.getColorValue(0, 37));
						b.setColor(0xFF000000 | Colorizer.getColorValue(0, 40));
					} else {
						p.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
						b.setColor(0xFF000000 | Colorizer.getColorValue(0, selectedBackground));
					}
					if(debug_mode == 1 || debug_mode == 2) {
						c.drawText(((TextTree.Color)u).getData(),x,y,p);
						x += p.measureText(((TextTree.Color)u).getData());
					}
				}
				if(u instanceof TextTree.NewLine || u instanceof TextTree.Break) {
					if(u instanceof TextTree.NewLine) {
						breakcolor.setColor(0xFFFF0000);
					}
					if(u instanceof TextTree.Break) {
						breakcolor.setColor(0xFF0000FF);
					}
					//draw break.
					c.drawRect(x,y-p.getTextSize(),x+char_width,y+5,breakcolor);
					
					y = y + PREF_LINESIZE;
					x = 0;
					drawnlines++;
					
					if(u instanceof TextTree.Break) {
						c.drawText(Integer.toString(drawnlines)+":", x, y, p);
						x += p.measureText(Integer.toString(drawnlines)+":");
					}
					if(drawnlines > CALCULATED_LINESINWINDOW) {
						stop = true;
						
						
					}
				}
			}
			
			
		}
		c.drawLine(0, y, WINDOW_WIDTH, y, z);
		}//end synchronized block
	}
	
	public class DrawRunner extends Thread {
		private SurfaceHolder _surfaceHolder;
		private ByteView _sv;
		//private boolean running = false;
		//private boolean paused = false;
		//private Boolean lock = null;
		
		public static final int MSG_DRAW = 100;
		public static final int MSG_SHUTDOWN = 101;
		
		private Handler threadHandler = null;
		
		public DrawRunner(SurfaceHolder parent,ByteView view) {
			_surfaceHolder = parent;
			_sv = view;
			//lock = drawlock;
		}
		
		
		
		public void dcbPriority(int val) {
			Process.setThreadPriority(val);
		}
		
		public void setRunning(boolean val) {
			//Log.e("SLICK","THREAD ATTEMPTED TO SET THE RUNNING VALUE");
			//running = val;
		}
		
		public void setPause() {
			//paused = true;
		}
		
		public void setResume() {
			//paused = false;
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
								//synchronized(the_tree) {
									_sv.onDraw(c);
								//}
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
			
		}
		
	}

	public void setParentLayout(RelativeLayout l) {
		parent_layout = l;
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
	public void setNewTextIndicator(TextView fill2) {
		new_text_in_buffer_indicator = fill2;
	}

	public void jumpToZero() {
		//scrollback = 0;
	}

	public void doDelayedDraw(int i) {
		if(_runner.threadHandler == null) return;
		if(!_runner.threadHandler.hasMessages(SlickView.DrawRunner.MSG_DRAW)) {
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
		encoding = pEncoding;
	}

	public void setCharacterSizes(int fontSize, int fontSpaceExtra) {
		PREF_LINESIZE = fontSize;
		PREF_LINEEXTRA = fontSpaceExtra;
	}

	public void setMaxLines(int maxLines) {
		the_tree.setMaxLines(maxLines);
	}

	public void setFont(Typeface font) {
		PREF_FONT = font;
	}

	public void addText(String obj, boolean b) {
		if(obj.equals("")) return;
		synchronized(the_tree) {
			//Log.e("BYTE",">>>>>>>ADDING TEXT:" + obj);
			try {
				the_tree.addBytesImpl(obj.getBytes("ISO-8859-1"));
			//try {
			//	addTextHandler.sendMessage(addTextHandler.obtainMessage(MESSAGE_ADDTEXT,obj.getBytes("ISO-8859-1")));
			//} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(_runner != null) {
			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);

			}
		}
	}
	
	private Colorizer.COLOR_TYPE updateColorRegisters(Integer i) {
		if(i == null) return Colorizer.COLOR_TYPE.NOT_A_COLOR;
		
		Colorizer.COLOR_TYPE type = Colorizer.getColorType(i);
		switch(type) {
		case FOREGROUND:
			selectedColor = i;
			//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
			//notFound = false;
			break;
		case BACKGROUND:
			//Log.e("SLICK","BACKGROUND COLOR ENCOUNTERED: " + i);
			selectedBackground = i;
			//bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
			break;
		case ZERO_CODE:
			//Log.e("WINDOW","ZERO CODE ENCOUNTERED");
			selectedBright = 0;
			selectedColor = 37;
			selectedBackground = 40;
			break;
		case BRIGHT_CODE:
			selectedBright = 1;
			break;
		default:
			return Colorizer.COLOR_TYPE.NOT_A_COLOR;
		}
		
		return type;
		//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
	
	}
	
	private class IteratorBundle {
		private ListIterator<TextTree.Line> i;
		private Float offset;
		public IteratorBundle(ListIterator<TextTree.Line> pI,Float pOffset) {
			setI(pI);
			setOffset(pOffset);
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
		
	}
	
	private IteratorBundle getScreenIterator(float pIn,float pLineSize) {
		
		float working_h = 0;
		int position = 0;
		
		float pY = pIn;
		
		if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) {
			//calculate how few.
			int under = CALCULATED_LINESINWINDOW-(the_tree.getBrokenLineCount());
			return new IteratorBundle(the_tree.getLines().listIterator(the_tree.getLines().size()),under*pLineSize);
		}
		
		double target = Math.floor(pY/pLineSize);
		int current = 0;
		
		Iterator<Line> i = the_tree.getLines().iterator();
		while(i.hasNext()) {
			Line l = i.next();
			working_h += pLineSize * (1 + l.getBreaks());
			current += 1 + l.getBreaks();
			if(working_h > pY) {
				float delta = working_h - pY;
				float offset = delta - pLineSize;
				
				//assert that overrun did happen.
				float showing = pY - (working_h - pLineSize*(1+l.getBreaks()));
				
				//fetch the actual next line.
				l = i.next();
				position++;
				
				showing -= pLineSize * (l.getBreaks());
				
				//String oformat = new PrintFormat("%6s").;
				//String deltaformat;
				//String hformat;
				//String yformat;
				String report = "REPORT: O="+-1*offset+" delta="+delta+" working_h="+working_h+" target="+pY+" position="+position+" breaks="+l.getBreaks()+" target="+target+" current="+current+" showing="+showing;
				Log.e("BYTE",report);
				//this is technically a new position.
				//position = position +1;
				return new IteratorBundle(the_tree.getLines().listIterator(position),showing);
			} else {
				//next line
				position++;
			}
		}
		
		return null;
		
	}
	
	private IteratorBundle getListIteratorForPosition(float pY,float pLineSize,Float pOffset) {
		
		float max = (the_tree.getBrokenLineCount()+the_tree.getLines().size()) * pLineSize;
		float percent = pY / max;
		float fline = pY / pLineSize;
		
		double target = Math.floor(fline);
		
		
		double offsetPercent = fline - Math.floor(fline);
		
		float working_y = 0;
		
		float target_y = (float) ((target)*pLineSize);
		
		int current = 0;
		boolean done = false;
		int position = 0;
		
	
		//Log.e("BYTE","BEGINNING SEARCH FOR THE WINDOW LINES");
		for(Line l : the_tree.getLines()) {
			//Log.e("BYTE","WORKING ON:" + TextTree.deColorLine(l));
			//Log.e("BYTE","HAS 1 Line and " + l.getBreaks() + " breaks.");
			current += 1;
			working_y = (current-1)*pLineSize;
			
			if(working_y > target_y) {
				return new IteratorBundle(the_tree.getLines().listIterator(position),target_y-working_y);
			}
			
			if(l.getBreaks() > 0) {
				for(int i=1;i<=l.getBreaks();i++) {
					current += 1;
					working_y = (current-1)*pLineSize;
					
					
					if(working_y > target_y) {
						//fill out the number, this will most likely be in the middle of a broken line
						float offset = (target_y-working_y)-(i-l.getBreaks())*pLineSize;
						float initial = target_y-working_y;
						float rest = (l.getBreaks()-i)*pLineSize;
						offset = -rest;
						Log.e("TREE","INIT: " + initial + " REST: " + rest+ "CALCULATED OFFSET:" + offset + " for line: " + position + " that is " + current + " broken lines away in the buffer with a window size of " + CALCULATED_LINESINWINDOW +" lines.\n"+
								"LINE HAS " + l.getBreaks() + " breaks and broke on break " +i+ ".");
						return new IteratorBundle(the_tree.getLines().listIterator(position),offset);
						
					}
				}
			}
			
			/*working_y = (current-1)*pLineSize;
			//Log.e("BYTE","CURRENT NOW:"+ current);
			if(working_y >= target_y) {
				//int overby = current - CALCULATED_LINESINWINDOW;
				float offset = target_y - working_y;
				//offset ;
				//int total = (current +1 + l.getBreaks()) - CALCULATED_LINESINWINDOW;
				//int offset = (1 + l.getBreaks()) - total;
				Log.e("TREE","CALCULATED OFFSET:" + offset + " for line: " + position + " that is " + current + " broken lines away in the buffer with a window size of " + CALCULATED_LINESINWINDOW +" lines.");
				return new IteratorBundle(the_tree.getLines().listIterator(position),offset);
			} */
			position++;
			//else {
				//current += 1 + l.getBreaks();
				//Log.e("BYTE","CURRENT NOW:"+ current);
			//}
			/*if(l.getBreaks() == 0) {
				//none broken line
				if(current >= target) {
					pOffset = (float) (pLineSize*offsetPercent);
					done = true;
				} else {
					current++;
				}
			} else {
				for(int i=0;i <= l.getBreaks() ; i++) {
					if((current + i) >= target) {
						//Log.e("BYTE","CALCULATING OFFSET");
						done = true;
						pOffset = (float) (pLineSize*(i-(l.getBreaks()+1)) + pLineSize*offsetPercent);
						Log.e("BYTE","CALCULATED OFFSET ON BROKEN LINE:" + pOffset + " LINEPOS:" + position);
						break;
					} else {
						current++;
					}
				}
			}
			if(done) {
				if(the_tree.getLines().size() <= CALCULATED_LINESINWINDOW) {
					pOffset = pLineSize * (CALCULATED_LINESINWINDOW-position);
				}
				Log.e("BYTE","CALCULATED OFFSET:" + pOffset + " LINEPOS:" + position);
				
				IteratorBundle b = new IteratorBundle(the_tree.getLines().listIterator(position),0f);
				return b;
			}
			position++;*/
		}
		if(the_tree.getLines().size() <= CALCULATED_LINESINWINDOW) {
			pOffset = pLineSize * (CALCULATED_LINESINWINDOW-position);
		}
		//Log.e("BYTE","NOT ENOUGH LINES:" + pOffset + " LINEPOS:" + position);
		
		IteratorBundle b = new IteratorBundle(the_tree.getLines().listIterator(the_tree.getLines().size()),0f);
		return b;
		//return the_tree.getLines().listIterator(the_tree.getLines().size());
	}
}
