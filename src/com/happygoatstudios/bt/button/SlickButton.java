package com.happygoatstudios.bt.button;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.happygoatstudios.bt.window.MainWindow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
//import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class SlickButton extends View {

	//int x;
	//int y;
	boolean hasfocus = false;
	private Listener the_listener = new Listener();
	private Handler myhandler = null;
	//private String the_text = null;
	//private String the_label = null;
	private SlickButtonData data = new SlickButtonData();
	private Handler dispatcher = null;
	private Handler deleter = null;
	
	//dip change
	//was 80
	private int size = 48;
	
	boolean moving = false;
	
	final static public int MSG_BEGINMOVE = 100;
	final static public int MSG_DELETE = 101;
	
	private float density = 1;
	
	boolean dialog_launched = false;
	
	//the units of width, height and labelheight are now dips. react accordingly
	/*
	 * Plan. 
	 * Constructors,
	 * where the rectangle peices are
	 * drawing routine.
	 */
	
	public SlickButton(Context context,int px,int py) {
		super(context);
		//x = px;
		//y = py;
		
		density = context.getResources().getDisplayMetrics().density;
		
		
		data.setX(px);
		data.setY(py);
		this.setClickable(true);
		this.setFocusable(true);
		this.setOnClickListener(the_listener);
		data.setLabel("NULL!");
		updateRect();
		
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_BEGINMOVE:
					moving = true;
					SlickButton.this.invalidate();
					
					//SlickButton.this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
					dispatcher.sendEmptyMessage(856); //the haptic feeback message
					//SlickButton.this.invalidate(SlickButton.this.rect); //only invaldate my rect.
					break;
				case MSG_DELETE:
					//SlickButton.this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
					dispatcher.sendEmptyMessage(856); //the haptic feeback message
					
					dialog_launched = true;
					button_down = false;
					moving = false;
					nudged = false;
					hasfocus = false;
					Message deleme = deleter.obtainMessage(666, SlickButton.this);
					deleter.sendMessage(deleme);
					//dispatcher.
					break;
				}
			}
		};
		
		
		//Log.e("SB","SLICKBUTTON CONSTRUCTOR PASSED");
		
	}
	
	public void setData(SlickButtonData in) {
		data = in;
		//Rect rect = new Rect();
		//x and y are now dips.
		//pull out density factor and multiply.
		
		rect.set(data.getX()-(int)((data.getWidth()*density)/2),data.getY()-(int)((data.getHeight()*density)/2),data.getX()+(int)((data.getWidth()*density)/2),data.getY()+(int)((data.getHeight()*density)/2));
		
	}
	
	public SlickButtonData getData() {
		return data;
	}
	

	public void setDeleter(Handler d) {
		deleter = d;
	}
	
	public void setDispatcher(Handler d) {
		dispatcher = d;
	}
	
	public void doDispatchFlip() {
		//only dispatch flip if it contains a command
		if(data.getFlipCommand() == null || data.getFlipCommand().equals("")) {
			
		} else {
			dispatchText(data.getFlipCommand());
		}
	}
	
	public void doDispatch() {
		//Message tmp = ConnectionHandler.obtainMessage(105);
		dispatchText(data.getText());
		
	}
	
	private void doButtonSetChange() {
		Message swaptoset = dispatcher.obtainMessage(MainWindow.MESSAGE_CHANGEBUTTONSET, data.getTargetSet());
		dispatcher.sendMessage(swaptoset);
	}
	
	public void dispatchText(String istr) {
		//Log.e("SB","DISPATCHING " + istr);
		String tmp = istr;
		//history.addCommand(data);
		Character cr = new Character((char)13);
		Character lf = new Character((char)10);
		String crlf = cr.toString() + lf.toString();
		tmp = tmp.concat(crlf);
		ByteBuffer buf = ByteBuffer.allocate(tmp.length());
	
		try {
			buf.put(tmp.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
	
		buf.rewind();
	
		byte[] buffbytes = buf.array();
		
		Message msg = dispatcher.obtainMessage(MainWindow.MESSAGE_SENDDATAOUT,buffbytes);
		dispatcher.sendMessage(msg);
	}
	
	public void setText(String t) {
		data.setText(t);
	}
	
	public String getText() {
		return data.getText();
	}
	
	public String getLabel() {
		return data.getLabel();
	}
	
	public void setFlipCommand(String t) {
		data.setFlipCommand(t);
	}
	
	public void setMoveMethod(int i) {
		//Log.e("SB","MOVE STATE CHANGED TO:"+i);
		data.MOVE_STATE = i;
	}
	
	public int getMoveMethod() {
		return data.MOVE_STATE;
	}
	
	public void setLabel(String t) {
		if(t == null) {
			t = "NULL!";
		}
		data.setLabel(t);
	}
	
	private int start_x = 0;
	private int start_y = 0;
	
	private int save_x = 0;
	private int save_y = 0;
	
	boolean nudged = false;
	boolean button_down = false;
	
	public SlickButtonData orig_data = null;	
	
	//down only once per cycle lock
	boolean preserve_lock = false;
	boolean doing_flip = false;
	
	Rect rect = new Rect();
	
	public void updateRect() {
		rect.set(data.getX()-(int)((data.getWidth()/2)*density),data.getY()-(int)((data.getHeight()*density)/2),data.getX()+(int)((data.getWidth()*density)/2),data.getY()+(int)((data.getHeight()*density)/2));
	}
	
	private enum DISPLAY_STATE {
		NONE,
		NORMAL,
		SELECTED,
		FLIPPED,
		MOVING
	}
	
	private DISPLAY_STATE state = DISPLAY_STATE.NONE;
	
	public boolean onTouchEvent(MotionEvent e) {
		
		//if(!nudged) {
		//	save_x = data.x;
		//	save_y = data.y;
		//}
		DISPLAY_STATE newstate = state;
		
		if(dialog_launched) {
			//Log.e("SLICKBUTTON","BAILING BECAUSE DIALOG LAUNCHED!");
			return true; //bail if anything happens
		}
		
		int pointer = e.getPointerId(0);
		int touchx = (int) e.getX(pointer);
		int touchy = (int) e.getY(pointer);
		

		
		//Rect rect = new Rect();
		//rect.set(data.getX()-(data.getWidth()/2),data.getY()-(data.getHeight()/2),data.getX()+(data.getWidth()/2),data.getY()+(data.getHeight()/2));
		
		if(rect.contains(touchx,touchy)) {
			//continue
			if(doing_flip) {
				doing_flip = false;
				//this.invalidate();
				newstate = DISPLAY_STATE.SELECTED;
				//this.invalidate(rect); //only invaldate my rect.
				
			}
			
		} else {
			if(!moving && !button_down) {
				return false;
			}
			
			if(!button_down) {
				return false;
			} else {
				//Log.e("SB","LETTING CLICK PASS THROUGH BECAUSE BUTTON IS DOWN");
				if(!moving && button_down) {
					if(!doing_flip) {
						
						doing_flip = true;
						//this.invalidate();
						newstate = DISPLAY_STATE.FLIPPED;
						//this.invalidate(rect); //only invaldate my rect.
						
					}
				} else {
					doing_flip = false;
				}
			}
		}
		
		//Log.e("SB","SB GOT TOUCH EVENT");
		if(e.getAction() == MotionEvent.ACTION_DOWN) {
			if(!preserve_lock) {
				//Log.e("SLICKBUTTON","SETTING ORIGINAL DATA TO IDENTIFY MYSELF WHEN THE DIALOG GOES.");
				orig_data = this.getData().copy();
			}
			hasfocus = true;
			start_x = touchx;
			start_y = touchy;
			//schedule message for moving
			myhandler.sendEmptyMessageDelayed(MSG_BEGINMOVE, 1000);
			myhandler.sendEmptyMessageDelayed(MSG_DELETE, 2000);
			save_x = data.getX();
			save_y = data.getY();
			button_down=true;
			//this.invalidate();
			newstate = DISPLAY_STATE.SELECTED;
			this.bringToFront();
			//dispatcher.sendEmptyMessage(MainWindow.MESSAGE_HFPRESS);
			//this.invalidate(rect);
		}
		if(e.getAction() == MotionEvent.ACTION_MOVE) {
			if(moving) {
				if(data.MOVE_STATE == data.MOVE_FREE) {
					data.setX(touchx);
					data.setY(touchy);
					updateRect();
					//this.invalidate();
					newstate = DISPLAY_STATE.MOVING;
				} else if (data.MOVE_STATE == data.MOVE_NUDGE) {
					//compute nudge
					int tmpx = touchx - start_x;
					int tmpy = touchy - start_y;
					data.setX(save_x + tmpx / 10);
					data.setY(save_y + tmpy / 10);
					updateRect();
					//double dist = 
					nudged = true;
					newstate = DISPLAY_STATE.MOVING;
					//this.invalidate();
				}
			}
			int diff_x = touchx - start_x;
			int diff_y = touchy - start_y;
			double abs_length = Math.sqrt(Math.pow(diff_x,2) + Math.pow(diff_y, 2));
			if(abs_length > 12.0*density) {
				//Log.e("SLICK","Length: " + (new Double(abs_length)));
				myhandler.removeMessages(MSG_DELETE);
				myhandler.removeMessages(MSG_BEGINMOVE);
			}
			
		}
		
		if(e.getAction() == MotionEvent.ACTION_UP) {
			doing_flip = false;
			hasfocus = false;
			myhandler.removeMessages(MSG_BEGINMOVE);
			myhandler.removeMessages(MSG_DELETE);
			if(!moving) {
				if(!rect.contains(touchx,touchy)) {
					//up action happend outside of recticle, do dispatch flip
					//Log.e("BUTTON",this.data.toString() + "CLICKED!");
					if(data.getTargetSet().equals("")) {
						doDispatchFlip(); //execute flip command
					} 
				} else {
					if(data.getTargetSet().equals("")) {
						doDispatch();
					} else {
						doButtonSetChange();
					}
				}
			}
			
			preserve_lock = false;
			
			button_down = false;
			
			moving = false;
			nudged = false;
			
			if(!orig_data.equals(this.data)) {
				iHaveChanged(orig_data);
			}
			
			//this.invalidate();
			newstate = DISPLAY_STATE.NORMAL;
		}
		
		if(newstate != state || newstate == DISPLAY_STATE.MOVING) {
			//Log.e("BUTTON","DRAWING BUTTON BECAUSE STATE CHANGED");
			this.invalidate();
			state = newstate;
			
			if(state == DISPLAY_STATE.FLIPPED) {
				dispatcher.sendEmptyMessage(MainWindow.MESSAGE_HFFLIP);
			}
			if(state == DISPLAY_STATE.SELECTED) {
				dispatcher.sendEmptyMessage(MainWindow.MESSAGE_HFPRESS);
			}
		} else {
			//Log.e("BUTTON","NOT DRAWING BUTTON BECAUSE STATE DIDN'T CHANGE");
		}
		return true;
	}
	


	/*protected void onFocusChanged(boolean gainFocus,int direction,Rect prev_rect) {
		//Log.e("SB","FOCUS CHANGED");
		if(gainFocus == true) {
			hasfocus = true;
		} else {
			hasfocus = false;
		}
		this.invalidate(rect);
	}*/
	Paint p = new Paint();
	Paint opts = new Paint();
	public void onDraw(Canvas c) {
		//c.
		//Log.e("BUTTON","DRAWING BUTTON!");
		//Rect rect = new Rect();
		
		//rect.set(data.getX()-(data.getWidth()/2),data.getY()-(data.getHeight()/2),data.getX()+(data.getWidth()/2),data.getY()+(data.getHeight()/2));
		//RectF f_rect = new RectF(rect);
		//c.drawColor(0xFF0FF000);
		//c.
		
		
		if(hasfocus) {
			if(doing_flip) {
				p.setColor(data.getFlipColor());
			} else {
				p.setColor(data.getSelectedColor());
			}
		} else {
			p.setColor(data.getPrimaryColor());
		}
		//c.drawRoundRect(f_rect, 8,8, p);
		c.drawRect(rect, p);
		
		//get text size.
		
		opts.setTypeface(Typeface.DEFAULT_BOLD);
		opts.setTextSize(data.getLabelSize()*density);
		//opts.setF
		
		opts.setFlags(Paint.ANTI_ALIAS_FLAG);
		
		float tsize = 0;
		if(doing_flip) {
			opts.setColor(data.getFlipLabelColor());
			tsize = opts.measureText( (data.getFlipLabel().equals("")) ? data.getLabel() : data.getFlipLabel());
			c.drawText((data.getFlipLabel().equals("")) ? data.getLabel() : data.getFlipLabel(), data.getX()-tsize/2, data.getY()+(int)((data.getLabelSize()*density)/2), opts);
		} else {
			opts.setColor(data.getLabelColor());
			tsize = opts.measureText(data.getLabel());
			c.drawText(data.getLabel(), data.getX()-tsize/2, data.getY()+(int)((data.getLabelSize()*density)/2), opts);
		}
		//float tsize = opts.measureText(data.getLabel());
		//c.drawText(data.getLabel(), data.getX()-tsize/2, data.getY()+12, opts);
		
		if(moving) {
			Rect m_rect = new Rect();
			m_rect.set(data.getX()-(int)((data.getWidth()*density)/2)+5,data.getY()-(int)((data.getHeight()*density)/2)+5,data.getX()+(int)((data.getWidth()*density)/2)-5,data.getY()+(int)((data.getHeight()*density)/2)-5);
			Paint rpaint = new Paint();
			rpaint.setColor(0xAAFF0000);
			c.drawRect(m_rect, rpaint);
			//RectF frect = new RectF(m_rect);
			//c.drawRoundRect(frect, 8, 8, rpaint);
		}
		
		return;
	}
	
	public void iHaveChanged(SlickButtonData orig_data) {
		Message modify = deleter.obtainMessage(MainWindow.MESSAGE_MODIFYBUTTON);
		Bundle b = modify.getData();
		b.putParcelable("ORIG_DATA", orig_data);
		b.putParcelable("MOD_DATA", this.data);
		modify.setData(b);
		deleter.sendMessage(modify);
	}
	
	public void setOnClickListener(OnClickListener l) {
		the_listener.setListener(l);
	}
	
	
	protected void onMeasure(int measurespec) {
		this.setMeasuredDimension(measureWidth(measurespec), measureHeight(measurespec));
	}
	
	private int measureWidth(int measureSpec) {
		int pref = 50;
		return getMesaurement(measureSpec,pref);
	}
	
	private int measureHeight(int measureSpec) {
		int pref = 50;
		return getMesaurement(measureSpec,pref);
	}
	
	private int getMesaurement(int measureSpec,int pref) {
		int specSize = MeasureSpec.getSize(measureSpec);
		int measurement = 0;
		
		switch(MeasureSpec.getMode(measureSpec)) {
		case MeasureSpec.EXACTLY:
			measurement = specSize;
			break;
		case MeasureSpec.AT_MOST:
			measurement = Math.min(specSize, pref);
			break;
		default:
			measurement = pref;
			break;
		}
		
		return measurement;
	}
	
	private class Listener implements View.OnClickListener 
	{
		private OnClickListener listener = null;

		public void onClick(View v) {
			//Log.e("SB","SENDING CLICK PRESS");
			if(listener != null) {
				listener.onClick(SlickButton.this);
			}
		}
		
		public void setListener(OnClickListener l) {
			listener = l;
		}
		
		
	}

	public void prepareToLaunchEditor() {
		orig_data = this.getData().copy();
		dialog_launched = true;
		button_down = false;
		moving = false;
		nudged = false;
		hasfocus = false;
	}
	


}
