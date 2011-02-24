package com.happygoatstudios.bt.window;

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
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

public class SlickView extends SurfaceView implements SurfaceHolder.Callback {

	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	
	private DrawRunner _runner;
	
	RelativeLayout parent_layout = null; //for adding buttons.
	EditText input = null; //for supporting buttons.
	Handler dataDispatch = null;
	
	private Pattern newline = Pattern.compile("\n");
	private Pattern carriage = Pattern.compile("\\x0D");
	
	private Float scrollback = new Float(0);
	
	private Boolean touchLock = new Boolean(false);
	
	private Boolean wasRunning = false;
	
	private Boolean drawn = false;
	private Boolean isdrawn = true;
	
	boolean buttondropstarted = false;
	
	public Vector<SlickButton> buttons = new Vector<SlickButton>();
	
	int bx = 0;
	int by = 0;
	
	final static public int MSG_BUTTONDROPSTART = 100;
	final static public int MSG_DELETEBUTTON = 666;
	final static public int MSG_CREATEBUTTON = 102;

	protected static final int MSG_REALLYDELETEBUTTON = 667;

	protected static final int MSG_CREATEBUTTONWITHDATA = 103;
	public Handler buttonaddhandler = null;
	
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
		Log.e("VIEW","VIEW STARTING UP!");
		createhandler();
		//this.setZOrderOnTop(true);
	} 
	
	private void createhandler() {
		buttonaddhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_CREATEBUTTON:
					int ix = msg.getData().getInt("X");
					int iy = msg.getData().getInt("Y");
					String text = msg.getData().getString("THETEXT");
					String label = msg.getData().getString("THELABEL");
					
					Log.e("SLICK","ATTEMPTING TO ADD BUTTON AT XY:" + ix + "|" + iy + "|" + text);
					
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
					
					parent_layout.addView(buttons.get(posi),lpi);
					
					break;
				case MSG_BUTTONDROPSTART:
					
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
					if(parent_layout == null) {
						return;
					}
					Log.e("LAUNCHER","BUTTON HANDLER THING CALLED!");
					//RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);

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
					by = 0;
					buttondropstarted = false;
					
					/*synchronized(touchLock) {
						touchLock.notify();
						touchLock = true;
					}*/
					//buttondropstarted = false;
					break;
				case MSG_DELETEBUTTON:
					Log.e("SLICK","ATTEMPTING TO REMOVE BUTTON");
					
					//TODO: change this to be a dialog and set settings.
					//build alerter dialog

					RelativeLayout l = parent_layout;
					SlickButton the_b = (SlickButton)msg.obj;
					
					ButtonEditorDialog d = new ButtonEditorDialog(parent_layout.getContext(),the_b,this);
					
					d.show();
					
					
					//d shoudl contain return values after we are done
					//if(d.EXIT_STATE == d.EXIT_DELETE) {
					//	l.removeView(del_b);
					//	buttons.remove(del_b);
					//}
					break;
				case MSG_REALLYDELETEBUTTON:
					RelativeLayout lb = parent_layout;
					SlickButton del_b = (SlickButton)msg.obj;
					lb.removeView(del_b);
					buttons.remove(del_b);
					break;
				case MSG_CREATEBUTTONWITHDATA:
					//int ix = msg.getData().getInt("X");
					//int iy = msg.getData().getInt("Y");
					//String text = msg.getData().getString("THETEXT");
					//String label = msg.getData().getString("THELABEL");
					
					Log.e("SLICK","CREATING BUTTON WITH DATA");
					
					//SlickButtonData dayta = (SlickButtonData)msg.obj;
					//int ixp = dayta.x;
					//int iyp = dayta.y;
					//String textp = dayta.the_text;
					//String labelp = dayta.the_label;
					
					SlickButton bip = new SlickButton(parent_layout.getContext(),0,0);
					//bip.setText(textp);
					//bip.setLabel(labelp);
					SlickButtonData the_data = new SlickButtonData();
					the_data.setDataFromString((String)msg.obj);
					bip.setData(the_data);
					
					RelativeLayout.LayoutParams lpip = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
					
					//SlickButton b = new SlickButton(parent_layout.getContext(),bx,by);

					bip.setClickable(true);
					bip.setFocusable(true);
					
					//bi.setText(input.getText().toString());
					bip.setDispatcher(dataDispatch);
					bip.setDeleter(this);
					
					buttons.add(bip);
					int posip = buttons.lastIndexOf(bip);
					
					parent_layout.addView(buttons.get(posip),lpip);
					break;
				}
			}
		};
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

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
		Log.e("SLICK","surfaceChanged called");
	}
	


	public void surfaceCreated(SurfaceHolder arg0) {
		Log.e("VIEW","SURFACE CREATED!");
		//if(!wasRunning) {
			_runner = new DrawRunner(getHolder(),this,touchLock);
			_runner.setRunning(true);
			_runner.start();
			wasRunning = true;
		//}
			


	}
	
	public String getBuffer() {
		
		return the_original_buffer.toString();
	}
	
	public void setBuffer(String input) {

		synchronized(isdrawn) {
			while(isdrawn == false) {
				try {
					isdrawn.wait();
				} catch (InterruptedException e) {
					Log.e("SLICK","setBuffer interrupted waiting for screen to be done drawing");
					e.printStackTrace();
				}
			}
		}
		
		the_original_buffer.setLength(0);
		the_buffer.setLength(0);
		the_original_buffer = new StringBuffer(input);
		the_buffer = new StringBuffer(betterBreakLines(the_original_buffer,77));
		
		synchronized(drawn) {
			//Log.e("SLICK","WAKING UP DRAW THREAD");
			drawn.notify();
			drawn = false;
		}
		
	}
	
	public void jumpToZero() {
		//call this to scroll back to 0.
		synchronized(touchLock) {
			while(touchLock.booleanValue()) {
				try {
					touchLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			touchLock = true;
		}
		
		/*--------- set scrollBack to 0--------*/
		scrollback = (float)0.0;
		
		synchronized(touchLock) {
			touchLock.notify();
			touchLock = false;
		}
	}
	
	public void startDrawing() {
		if(_runner != null) {
			_runner.setRunning(true);
			_runner.start();
			//wasRunning = true;
		}
		
		synchronized(drawn) {
			drawn.notify();
		}
		
	}
	
	public void stopDrawing() {
		Log.e("SLICK","Attempted to kill/stop thread at stopDrawing()");
		boolean retry = true;
		_runner.setRunning(false);
		while(retry) {
			try{
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}
		wasRunning = false;
		

		
		//_runner = null;
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.e("SLICK","SURFACE DESTROYED!");
		boolean retry = true;
		

		while(retry) {
			try{
				_runner.setRunning(false);
				synchronized(drawn) {
					drawn.notify();
					drawn = false;
					_runner.setRunning(false);
				}
				_runner.setRunning(false);
				
				
				synchronized(touchLock) {
					//while(touchLock) {
						//touchLock.wait();
					//}
					if(touchLock) {
						touchLock.notify();
						touchLock = false;
					}
				}
				Log.e("SLICK","Waiting for runner to stop!");
				_runner.join();
				retry = false;
			} catch (InterruptedException e) { }
		}
		wasRunning = false;
		Log.e("SLICK","SURFACE_DESTROYED = TRUE!");
	}
	
	
	public void onAnimationStart() {
		Log.e("SLICK","ANIMATION START!");
		Animation anim = this.getAnimation();
		
		//this.startAnimation(anim);
		super.onAnimationStart();
	}
	
	public void onAnimationEnd() {
		Log.e("SLICK","ANIMATION END!");
		super.onAnimationEnd();
	}
	
	
	StringBuffer the_buffer = new StringBuffer();
	StringBuffer the_original_buffer = new StringBuffer();
	
	public void addText(String input) {
		
		
		synchronized(isdrawn) {
			while(isdrawn == false) {
				try {
					isdrawn.wait();
				} catch (InterruptedException e) {
					Log.e("SLICK","addText interrupted waiting for screen to be done drawing");
					e.printStackTrace();
				}
			}
		}
		
		Matcher carriagerock = carriage.matcher(input);
		
		//Matcher toLines = newline.matcher();
		StringBuffer carriage_free = new StringBuffer(carriagerock.replaceAll(""));
		the_buffer.append(betterBreakLines(carriage_free,77));
		the_original_buffer.append(carriage_free);
		
		//drawn = false;
		//Log.e("SLICK","SETTING DIRTY BUFFER FOR DRAWING!");
		
		//nuke buffer contents.
		int chars_per_row = 77;
		int rows = 25;
		if(the_buffer.length() > (10*chars_per_row*rows)) {
			the_buffer.replace(0, the_buffer.length() - ((10*chars_per_row*rows)), "");
		}
		if(the_original_buffer.length() > (10*chars_per_row*rows)) {
			the_original_buffer.replace(0, the_original_buffer.length() - ((10*chars_per_row*rows)), "");
		}
        
		synchronized(drawn) {
			//Log.e("SLICK","WAKING UP DRAW THREAD");
			drawn.notify();
			drawn = false;
		}
		
	}
	
	
	StringBuffer sel_color = new StringBuffer(new Integer(0xFFBBBBBB).toString());
	StringBuffer sel_bright = new StringBuffer(new Integer(0).toString()); 
	StringBuffer csegment = new StringBuffer();
	Canvas drawn_buffer = null;
	@Override
	public void onDraw(Canvas canvas) {
		//IM DRAWING
		
		int TEXTSIZE = 18;
		int linesize = 20;
		//calculate screen offset and number of lines.
		int height = canvas.getHeight();
		
		
		int numberoflinesinwindow = (int)( height / linesize );
		int scrollbacklines = scrollback.intValue() / linesize;
		
		int remainder = (int) (height - (numberoflinesinwindow*linesize)) - 4 + ((int) scrollback.intValue() % linesize);
		
		if(remainder < 0) {
			Log.e("SLICK","WE HAVE A PROBLEM WITH WINDOW SIZE");
		}
		
		//first clear the background
		canvas.drawColor(0xFF0A0A0A); //fill with black
		//canvas.drawColor(0xFF333333); //fill with grey
        Paint opts = new Paint();
        opts.setAlpha(0);
        opts.setAntiAlias(true);
        //opts.setDither(true);
        opts.setARGB(255, 255, 255, 255);
        opts.setTextSize(TEXTSIZE);
        
        //opts.setStyle(Style.)
        opts.setTypeface(Typeface.MONOSPACE);
        /*canvas.drawText("####------     PAINT    ------##?###?#?#?#?##",0,0,opts);
        canvas.drawText("-----        -------      ||||| |||| Xxxxxxxx",0,13,opts);
        canvas.drawText("||                                         ||",0,26,opts);*/
        Matcher toLines = newline.matcher(the_buffer.toString());
        
        StringBuffer line = new StringBuffer();
        
        
        
        
        int currentline = 1;
        
        //count lines
        int numlines = countLines();
        int maxlines = numberoflinesinwindow; 
        
        
        
        int startDrawingAtLine = 1;
        if(numlines > maxlines) {
        	startDrawingAtLine = numlines - maxlines - scrollbacklines + 1;
        } else {
        	startDrawingAtLine = 1;
        }
        
       
        boolean endonnewline = false;
        while(toLines.find()) {
        	toLines.appendReplacement(line, "");
        	
        	
        	if(currentline >= startDrawingAtLine-1 && currentline <= (startDrawingAtLine + maxlines)) {
        		
        		int screenpos = currentline - startDrawingAtLine + 1;
        		int y_position =  ((screenpos*linesize)+remainder);
        		Matcher colormatch = colordata.matcher(line.toString());
        		
        		
        		float x_position = 0;
        		
        		boolean colorfound = false;
        		
        		while(colormatch.find()) {
        			colorfound = true;
        			//String bright = colormatch.group(2);
        			//String value = colormatch.group(3);
        			//if(bright == null) {
        			//	bright = "0";
        			//}
        			
        			
        			
        			
        			colormatch.appendReplacement(csegment, "");
        			
        			//get color data
        			int color = Colorizer.getColorValue(new Integer(sel_bright.toString()), new Integer(sel_color.toString()));
        			if(color == 0) {
        				Log.e("SLICK","COLORLOOKUP RETURNED 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
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
        				Log.e("SLICK","COLOLPARSE GOT 0 for:" + colormatch.group() + " bright:" + sel_bright + " val: " + sel_color);
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
        			canvas.drawText(line.toString(), 0, y_position , opts);
        		}
        		
        		
        		
         
        	}
        	
        	//line = new StringBuffer();
        	line.setLength(0);
        	currentline++;
        	if(toLines.end() == the_buffer.length()) {
        		endonnewline = true;
        	}
        	
        }
        if(!endonnewline) {
        	
        	toLines.appendTail(line);
        	//canvas.drawText(line.toString(), 0, (currentline*linesize)+remainder+new Float(scrollback).intValue(), opts);
        	//Log.e("SLICK","APPENDED LINE TAIL" + line.toString());
        	
        	int screenpos = currentline - startDrawingAtLine + 1;
    		int y_position =  ((screenpos*linesize)+remainder);
    		//int alt_y_pos = ((screenpos*linesize));
    		opts.setColor(0xFF00CCCC);
    		//canvas.drawLine(0, y_position, canvas.getWidth(), y_position, opts);
    		//opts.setColor(0xFF00FFFF);
    		//canvas.drawLine(0, alt_y_pos, canvas.getWidth(), alt_y_pos, opts);
        	//int tmpy = ((currentline)*linesize)+remainder;
    		//opts.setColor(0xFF00CCCC);
    		/*--------------------------------------*/
    		
    		//int screenpos = currentline - startDrawingAtLine;
    		//int y_position =  ((screenpos*linesize)+remainder);
    		Matcher colormatch = colordata.matcher(line.toString());
    		
    		
    		float x_position = 0;
    		
    		boolean colorfound = false;
    		
    		while(colormatch.find()) {
    			colorfound = true;
    			//String bright = colormatch.group(2);
    			//String value = colormatch.group(3);
    			//if(bright == null) {
    			//	bright = "0";
    			//}
    			
    			
    			
    			
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
    			canvas.drawText(line.toString(), 0, y_position , opts);
    		}
    		/*--------------------------------------*/
        	//canvas.drawText(line.toString(), 0, y_position, opts);
        	//Log.e("VIEW","PROMPT:" + y_position + ":" + line.toString());
        	line.setLength(0);
        	currentline++;		
        	
        }
        
        //release the lock
        drawn_buffer = canvas;
        
        /*synchronized(touchLock) {
        	touchLock.notify();
        	touchLock = false;
        	//touchLock.notify();
        }*/
		//}
	}
	
	public int measuredX = 0;
	public int measuredY = 0;
	
	public void onMeasure(int wSpec,int hSpec) {
		int provided_x = 0;
		int provided_y = 0;
		switch(MeasureSpec.getMode(wSpec)) {
			case MeasureSpec.AT_MOST:
				
				provided_x = MeasureSpec.getSize(wSpec);
				Log.e("SLICK","MEASURING WIDTH: AT MOST" + provided_x);
				break;
			case MeasureSpec.EXACTLY:
				
				provided_x = MeasureSpec.getSize(wSpec);
				Log.e("SLICK","MEASURING WIDTH: EXACTLY" + provided_x);
				break;
			case MeasureSpec.UNSPECIFIED:
				
				provided_x = MeasureSpec.getSize(wSpec);
				Log.e("SLICK","MEASURING WIDTH: UNSPECIFIED" + provided_x);
				break;
			default:
				break;
		}
		
		switch(MeasureSpec.getMode(hSpec)) {
		case MeasureSpec.AT_MOST:
			
			provided_y = MeasureSpec.getSize(hSpec);
			Log.e("SLICK","MEASURING HEIGHT: AT MOST" + provided_y);
			break;
		case MeasureSpec.EXACTLY:
			
			provided_y = MeasureSpec.getSize(hSpec);
			Log.e("SLICK","MEASURING HEIGHT: EXACTLY" + provided_y);
			break;
		case MeasureSpec.UNSPECIFIED:
			
			provided_y = MeasureSpec.getSize(hSpec);
			Log.e("SLICK","MEASURING HEIGHT: UNSPECIFIED" + provided_y);
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
	public boolean onTouchEvent(MotionEvent t) {
		
		
		
		//touchLock = true;
		//_runner.setPause();
		if(t.getAction() == MotionEvent.ACTION_DOWN) {
			buttonaddhandler.sendEmptyMessageDelayed(MSG_BUTTONDROPSTART, 2500);
			start_x = new Float(t.getX(t.getPointerId(0)));
			start_y = new Float(t.getY(t.getPointerId(0)));
		}
		
		if(!increadedPriority) {
			Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
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
		
		synchronized(isdrawn) {
			while(isdrawn == false) {
				try {
					isdrawn.wait();
				} catch (InterruptedException e) {
					Log.e("SLICK","addText interrupted waiting for screen to be done drawing");
					e.printStackTrace();
				}
			}
		}
		
		scrollback = scrollback + diff;
		
			
		
		if(scrollback < 0) {
			scrollback = (float)0.0;
		}
		
		
		
		if(t.getAction() == (MotionEvent.ACTION_UP)) {
			Log.e("SLICK","Got up action: " + scrollback);
			pre_event = null;
			prev_y = new Float(0);
			buttonaddhandler.removeMessages(SlickView.MSG_BUTTONDROPSTART);
	        
	        //reset the priority
	        increadedPriority = false;
	        Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
	        
	        synchronized(drawn) {
				drawn.notify();
				drawn = false;
			}
	        
			return true;
		}
		
		pre_event = MotionEvent.obtain(t);
		//_runner.setResume();
		synchronized(drawn) {
			drawn.notify();
			drawn = false;
		}
       
		
		return true; //consumes
		
		//return false; //does not consume
	}
	
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
			//StringBuffer splitline = ;
			//Log.e("SLICK","ORGINIAL:" + line);
			//Log.e("SLICK","MOD:" + splitline);
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
	
	
	public StringBuffer prompt_string = new StringBuffer();
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
				Log.e("SLICK","NEW PROMT:" + prompt_string.toString());
			}
			
		}
		
		return found;
	}
	
	public class DrawRunner extends Thread {
		private SurfaceHolder _surfaceHolder;
		private SlickView _sv;
		private boolean running = false;
		private boolean paused = false;
		private Boolean lock = null;
		
		public DrawRunner(SurfaceHolder parent,SlickView view,Boolean drawlock) {
			_surfaceHolder = parent;
			_sv = view;
			lock = drawlock;
		}
		
		public void setRunning(boolean val) {
			Log.e("SLICK","THREAD ATTEMPTED TO SET THE RUNNING VALUE");
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
			
			//Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
			Canvas c;
			Log.e("SLICK","VIEW THREAD RUNNING");
			while(running) {
				//if(!paused) {
					c = null;
					
					synchronized(drawn) {
						while(drawn) {
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
					}
					
					synchronized(isdrawn) {
						isdrawn.notify();
						isdrawn = true;
					}
			}
		}
		
	}

}
