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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
	
	private RelativeLayout parent_layout = null;

	private TextView new_text_in_buffer_indicator = null;

	private int scrollback = 20*PREF_LINESIZE;

	private int debug_mode = 1;

	private String encoding = "ISO-8859-1";

	private int PREF_LINEEXTRA = 2;
	
	int selectedColor = 37;
	int selectedBright = 0;
	int selectedBackground = 60;
	
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
	
	Paint p = new Paint();

	
	Paint b = new Paint();
	public void onDraw(Canvas c) {
		//Matrix m = c.getMatrix();
		
		//m.setTranslate(0, WINDOW_HEIGHT-5);
		//m.setScale(1, -1);
		
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
		int y_position = CALCULATED_LINESINWINDOW*PREF_LINESIZE;
		int line_number = y_position/PREF_LINESIZE;
		
		//TODO: STEP 2
		//get the iterator of the list at the given position.
		//i = the_tree.getLines().listIterator(line_number);
		//use our super cool iterator function.
		Float offset = 0f;
		IteratorBundle bundle = getListIteratorForPosition(y_position,PREF_LINESIZE,offset);
		i = bundle.getI();
		y = bundle.getOffset()-(3*this.getContext().getResources().getDisplayMetrics().density);
		if(i == null) {Log.e("BYTE","CAN'T ITERATE, NO ITERATOR!"); return;}
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
					y = y + PREF_LINESIZE;
					x = 0;
					drawnlines++;
					if(drawnlines > CALCULATED_LINESINWINDOW) {
						stop = true;
					}
				}
			}
			
			
		}
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
								synchronized(the_tree) {
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

	public void setNewTextIndicator(TextView fill2) {
		new_text_in_buffer_indicator = fill2;
	}

	public void jumpToZero() {
		//scrollback = 0;
	}

	public void doDelayedDraw(int i) {
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
		synchronized(the_tree) {
			try {
				the_tree.addBytes(obj.getBytes("ISO-8859-1"));
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
	
	private IteratorBundle getListIteratorForPosition(float pY,float pLineSize,Float pOffset) {
		
		float max = the_tree.getBrokenLineCount() * pLineSize;
		float percent = pY / max;
		float fline = pY / pLineSize;
		
		double target = Math.floor(fline);
		double offsetPercent = fline - Math.floor(fline);
		
		int current = 0;
		boolean done = false;
		int position = 0;
		for(Line l : the_tree.getLines()) {
			if(l.getBreaks() == 0) {
				//none broken line
				if(current > target) {
					pOffset = (float) (pLineSize*offsetPercent);
					done = true;
				} else {
					current++;
				}
			} else {
				for(int i=0;i < l.getBreaks() ; i++) {
					if((current - i) > target) {
						Log.e("BYTE","CALCULATING OFFSET");
						done = true;
						pOffset = (float) (pLineSize*(i-(l.getBreaks()+1)) + pLineSize*offsetPercent);
						Log.e("BYTE","CALCULATED OFFSET:" + pOffset + " LINEPOS:" + position);
					
					} else {
						current++;
					}
				}
			}
			if(done) {
				if(position < CALCULATED_LINESINWINDOW) {
					pOffset = pLineSize * (CALCULATED_LINESINWINDOW-position);
				}
				IteratorBundle b = new IteratorBundle(the_tree.getLines().listIterator(position),pOffset);
				return b;
			}
			position++;
		}
		if(position-1 < CALCULATED_LINESINWINDOW) {
			pOffset = pLineSize * (CALCULATED_LINESINWINDOW-position);
		}
		IteratorBundle b = new IteratorBundle(the_tree.getLines().listIterator(the_tree.getLines().size()),pOffset);
		return b;
		//return the_tree.getLines().listIterator(the_tree.getLines().size());
	}
}
