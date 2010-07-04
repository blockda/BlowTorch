package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

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
import android.util.Log;
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
	private int size = 80;
	
	boolean moving = false;
	
	final static public int MSG_BEGINMOVE = 100;
	final static public int MSG_DELETE = 101;
	
	public SlickButton(Context context,int px,int py) {
		super(context);
		//x = px;
		//y = py;
		data.x = px;
		data.y = py;
		this.setClickable(true);
		this.setFocusable(true);
		this.setOnClickListener(the_listener);
		data.the_label = "NULL!";
		
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MSG_BEGINMOVE:
					moving = true;
					SlickButton.this.invalidate();
					break;
				case MSG_DELETE:
					Message deleme = deleter.obtainMessage(666, SlickButton.this);
					deleter.sendMessage(deleme);
					//dispatcher.
					break;
				}
			}
		};
		
		
		Log.e("SB","SLICKBUTTON CONSTRUCTOR PASSED");
		
	}
	
	public void setData(SlickButtonData in) {
		data = in;
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
		if(data.flip_command == null || data.flip_command.equals("")) {
			
		} else {
			dispatchText(data.flip_command);
		}
	}
	
	public void doDispatch() {
		//Message tmp = ConnectionHandler.obtainMessage(105);
		dispatchText(data.the_text);
		
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
		
		Message msg = dispatcher.obtainMessage(BaardTERMWindow.MESSAGE_SENDDATAOUT,buffbytes);
		dispatcher.sendMessage(msg);
	}
	
	public void setText(String t) {
		data.the_text = t;
	}
	
	public String getText() {
		return data.the_text;
	}
	
	public String getLabel() {
		return data.the_label;
	}
	
	public void setFlipCommand(String t) {
		data.flip_command = t;
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
		data.the_label = t;
	}
	
	private int start_x = 0;
	private int start_y = 0;
	
	private int save_x = 0;
	private int save_y = 0;
	
	boolean nudged = false;
	boolean button_down = false;
	
	public boolean onTouchEvent(MotionEvent e) {
		
		//if(!nudged) {
		//	save_x = data.x;
		//	save_y = data.y;
		//}
		int pointer = e.getPointerId(0);
		int touchx = (int) e.getX(pointer);
		int touchy = (int) e.getY(pointer);
		
		Rect rect = new Rect();
		rect.set(data.x-(size/2),data.y-(size/2),data.x+(size/2),data.y+(size/2));
		
		if(rect.contains(touchx,touchy)) {
			//continue
		} else {
			if(!moving && !button_down) {
				return false;
			}
			
			if(!button_down) {
				return false;
			} else {
				//Log.e("SB","LETTING CLICK PASS THROUGH BECAUSE BUTTON IS DOWN");
			}
		}
		
		//Log.e("SB","SB GOT TOUCH EVENT");
		if(e.getAction() == MotionEvent.ACTION_DOWN) {
			hasfocus = true;
			start_x = touchx;
			start_y = touchy;
			//schedule message for moving
			myhandler.sendEmptyMessageDelayed(MSG_BEGINMOVE, 1500);
			myhandler.sendEmptyMessageDelayed(MSG_DELETE, 4000);
			save_x = data.x;
			save_y = data.y;
			button_down=true;
			this.invalidate();
		}
		if(e.getAction() == MotionEvent.ACTION_MOVE) {
			if(moving) {
				if(data.MOVE_STATE == data.MOVE_FREE) {
					data.x = touchx;
					data.y = touchy;
				} else if (data.MOVE_STATE == data.MOVE_NUDGE) {
					//compute nudge
					int tmpx = touchx - start_x;
					int tmpy = touchy - start_y;
					data.x = save_x + tmpx / 10;
					data.y = save_y + tmpy / 10;
					//double dist = 
					nudged = true;
				}
			}
			int diff_x = touchx - start_x;
			int diff_y = touchy - start_y;
			double abs_length = Math.sqrt(Math.pow(diff_x,2) + Math.pow(diff_y, 2));
			if(abs_length > 18.0) {
				//Log.e("SLICK","Length: " + (new Double(abs_length)));
				myhandler.removeMessages(MSG_DELETE);
			}
			this.invalidate();
		}
		
		if(e.getAction() == MotionEvent.ACTION_UP) {
			hasfocus = false;
			myhandler.removeMessages(MSG_BEGINMOVE);
			myhandler.removeMessages(MSG_DELETE);
			if(!moving) {
				if(!rect.contains(touchx,touchy)) {
					//up action happend outside of recticle, do dispatch flip
					doDispatchFlip(); //execute flip command
				} else {
					doDispatch();
				}
			}
			
			button_down = false;
			
			moving = false;
			nudged = false;
			
			this.invalidate();
		}
		return true;
	}
	
	protected void onFocusChanged(boolean gainFocus,int direction,Rect prev_rect) {
		//Log.e("SB","FOCUS CHANGED");
		if(gainFocus == true) {
			hasfocus = true;
		} else {
			hasfocus = false;
		}
		this.invalidate();
	}
	
	public void onDraw(Canvas c) {
		Rect rect = new Rect();
		
		rect.set(data.x-(size/2),data.y-(size/2),data.x+(size/2),data.y+(size/2));
		//RectF f_rect = new RectF(rect);
		//c.drawColor(0xFF0FF000);
		Paint p = new Paint();
		
		if(hasfocus) {
			p.setColor(0x8800FF00);
		} else {
			p.setColor(0x550000FF);
		}
		//c.drawRoundRect(f_rect, 8,8, p);
		c.drawRect(rect, p);
		
		//get text size.
		Paint opts = new Paint();
		opts.setTypeface(Typeface.DEFAULT_BOLD);
		opts.setColor(0x990000FF);
		opts.setTextSize(24);
		float tsize = opts.measureText(data.the_label);
		c.drawText(data.the_label, data.x-tsize/2, data.y+12, opts);
		
		if(moving) {
			Rect m_rect = new Rect();
			m_rect.set(data.x-(size/2)+5,data.y-(size/2)+5,data.x+(size/2)-5,data.y+(size/2)-5);
			Paint rpaint = new Paint();
			rpaint.setColor(0xAAFF0000);
			c.drawRect(m_rect, rpaint);
			//RectF frect = new RectF(m_rect);
			//c.drawRoundRect(frect, 8, 8, rpaint);
		}
		
		return;
	}
	
	public void iHaveChanged(SlickButtonData orig_data) {
		Message modify = deleter.obtainMessage(BaardTERMWindow.MESSAGE_MODIFYBUTTON);
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
	


}
