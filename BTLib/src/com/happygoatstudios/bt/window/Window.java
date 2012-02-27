package com.happygoatstudios.bt.window;


import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.happygoatstudios.bt.service.Colorizer;
import com.happygoatstudios.bt.service.IWindowCallback;
import com.happygoatstudios.bt.service.SettingsChangedListener;
import com.happygoatstudios.bt.service.plugin.settings.BaseOption;
import com.happygoatstudios.bt.service.plugin.settings.SettingsGroup;
//import com.happygoatstudios.bt.window.LuaWindow.BoundsFunction;
//import com.happygoatstudios.bt.window.LuaWindow.DebugFunction;
//import com.happygoatstudios.bt.window.LuaWindow.DrawFunction;
//import com.happygoatstudios.bt.window.LuaWindow.InvalidateFunction;
//import com.happygoatstudios.bt.window.LuaWindow.OptionsMenuFunction;
//import com.happygoatstudios.bt.window.LuaWindow.PluginXCallSFunction;
//import com.happygoatstudios.bt.window.LuaWindow.TableAdapterFunction;
import com.happygoatstudios.bt.window.TextTree.Line;
import com.happygoatstudios.bt.window.TextTree.Selection;
import com.happygoatstudios.bt.window.TextTree.SelectionCursor;
import com.happygoatstudios.bt.window.TextTree.Unit;

import android.R;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.happygoatstudios.bt.settings.HyperSettings;
import com.happygoatstudios.bt.settings.HyperSettings.LINK_MODE;

public class Window extends View implements AnimatedRelativeLayout.OnAnimationEndListener,SettingsChangedListener {

	//private DrawRunner _runner = null;
	private Bitmap homeWidgetDrawable = null;
	private Bitmap textSelectionCancelBitmap = null;
	private Bitmap textSelectionCopyBitmap = null;
	private Bitmap textSelectionSwapBitmap = null;
	private Rect homeWidgetRect = new Rect();
	private TextTree the_tree = null;
	private TextTree buffer = null;
	private int mMaxHeight;
	private int mMaxWidth;
	private int PREF_FONTSIZE = 18;
	private int mHeight = 1;
	private int mWidth = 1;
	int one_char_is_this_wide = 1;
	LuaState L = null;
	String mOwner;
	Paint clearme = new Paint();
	public int CALCULATED_LINESINWINDOW;
	private int PREF_LINEEXTRA = 2;
	private int PREF_LINESIZE = (int)PREF_FONTSIZE + PREF_LINEEXTRA;
	private Typeface PREF_FONT = Typeface.MONOSPACE;
	public int CALCULATED_ROWSINWINDOW;
	private boolean textSelectionEnabled = true;
	private double fling_velocity;
	//boolean buttondropstarted=false;
	boolean increadedPriority = false;
	//boolean lockButtonMoves = false;
	//boolean lockButtonEdits = false;
	//private RelativeLayout parent_layout = null;
	private boolean bufferText = false;
	private View new_text_in_buffer_indicator = null;

	

	private int debug_mode = 0;

	//private String encoding = "ISO-8859-1";
	boolean constrictWindow = false;
	//int constrictedHeight = 300;
	//int constrictedWidth = 600;
	int mAnchorLeft = 0;
	int mAnchorTop = 0;
	
	boolean edgeTop = true;
	boolean edgeLeft = true;
	boolean edgeRight = true;
	boolean edgeBottom = true;
	
	private Paint borderPaint = new Paint();
	
	private LINK_MODE linkMode = LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND;
	private int linkHighlightColor = HyperSettings.DEFAULT_HYPERLINK_COLOR;
	
	Integer selectedColor = new Integer(37);
	Integer selectedBright = new Integer(0);
	Integer selectedBackground = new Integer(60);
	boolean xterm256FGStart = false;
	boolean xterm256BGStart = false;
	boolean xterm256Color = false;
	private Handler mHandler = null;
	//private Handler realbuttonhandler = null;
	Handler mainHandler = null;
	//Rect mBounds = null;
	protected static final int MSG_UPPRIORITY = 200;
	protected static final int MSG_NORMALPRIORITY = 201;
	private Paint textSelectionIndicatorPaint = new Paint();
	private Paint textSelectionIndicatorBackgroundPaint = new Paint();
	private Paint textSelectionIndicatorCirclePaint = new Paint();
	
	//final static public int MSG_BUTTONDROPSTART = 100;
	//final static public int MSG_CLEAR_NEW_TEXT_INDICATOR = 105;
	//final static public int MSG_SET_NEW_TEXT_INDICATOR = 106;
	//final static public int MSG_SET_NEW_TEXT_INDICATOR_ANIMATED = 107;
	//final static public int MSG_DELETEBUTTON = 1040;
	//final static public int MSG_REALLYDELETEBUTTON = 1041;
		
	public static final int MESSAGE_ADDTEXT = 0;

	private static final int MESSAGE_DRAW = 117;

	protected static final int MESSAGE_FLUSHBUFFER = 118;
	protected static final int MESSAGE_SHUTDOWN = 119;
	public static final int MESSAGE_PROCESSXCALLS = 4;
	//private boolean disableEditing = false;
	protected static final int MESSAGE_CLEARTEXT = 5;
	protected static final int MESSAGE_SETTINGSCHANGED = 6;
	protected static final int MESSAGE_ENCODINGCHANGED = 7;
	protected static final int MESSAGE_STARTSELECTION = 8;
	protected static final int MESSAGE_SCROLLDOWN=9;
	protected static final int MESSAGE_SCROLLUP=10;
	protected static final int MESSAGE_SCROLLRIGHT = 11;
	protected static final int MESSAGE_SCROLLLEFT = 12;
	
	//Animation indicator_on = new AlphaAnimation(1.0f,0.0f);
	//Animation indicator_off = new AlphaAnimation(0.0f,0.0f);
	//Animation indicator_on_no_cycle = new AlphaAnimation(1.0f,1.0f);
	
	Handler dataDispatch = null;
	EditText input = null;
	
	Object token = new Object(); //token for synchronization.
	private SettingsGroup settings = null;
	//private int myWidth = -1;
	//LayerManager mManager = null;
	Context mContext = null;
	
	Bitmap mSelectionIndicatorBitmap = null;
	Canvas mSelectionIndicatorCanvas = null;
	
	int SELECTIONINDICATOR_FONTSIZE = 30;
	Paint selectionIndicatorPaint = new Paint();
	int one_selection_char_is_this_wide = 1;
	int selectionIndicatorHalfDimension = 90;
	
	Path selectionIndicatorClipPath = new Path();
	
	Rect selectionIndicatorLeftButtonRect = new Rect();
	Rect selectionIndicatorRightButtonRect = new Rect();
	Rect selectionIndicatorUpButtonRect = new Rect();
	Rect selectionIndicatorDownButtonRect = new Rect();
	Rect selectionIndicatorCenterButtonRect = new Rect();
	
	Rect selectionIndicatorRect = new Rect();
	/*public Window(Context context,LayerManager manager) {
		super(context);
		//getHolder().addCallback(this);
		init();
		mManager = manager;
		mContext = context;
	}
	
	public Window(Context context,AttributeSet attrib) {
		super(context,attrib);
		//getHolder().addCallback(this);
		//_runner = new DrawRunner(getHolder(),this,touchLock);
		//Log.e("VIEW","VIEW STARTING UP!");
		//createhandler();
		//this.setZOrderOnTop(true);
		init();
	} */
	
	public Window(Context context,String name,String owner,Handler mainWindowHandler,SettingsGroup settings) {
		super(context);
		init(name,owner,mainWindowHandler,settings);
	}
	
	
	
	/*public void onCreate(Bundle b) {
		onSizeChanged(this.getWidth(),this.getHeight(),0,0);
		viewCreate();
	}*/
	
	protected void onAttachedToWindow() {
		Log.e("WINDOW","Attatched to window.");
		viewCreate();
	}
	
	protected void onDetachedFromWindow() {
		Log.e("WINDOW","Detached from window.");
		viewDestroy();
	}
//	protected void onMeasure(int widthSpec,int heightSpec) {
//		mHeight = MeasureSpec.getSize(heightSpec);
//		mWidth = MeasureSpec.getSize(widthSpec);
//		
//		
//		setMeasuredDimension(mWidth,mHeight);
//		
//		//if(sizeChanged) {
//		calculateCharacterFeatures(mWidth,mHeight);
//			//sizeChanged = false;
//		//}	
//		//doDelayedDraw(0);
//	}
	
	private void init(String name,String owner,Handler mainWindowHandler,SettingsGroup settings) {
		selectionIndicatorClipPath.addCircle(selectionIndicatorHalfDimension,selectionIndicatorHalfDimension,selectionIndicatorHalfDimension-10,Path.Direction.CCW);
		homeWidgetDrawable = BitmapFactory.decodeResource(this.getContext().getResources(),com.happygoatstudios.bt.R.drawable.homewidget);
		textSelectionCancelBitmap = BitmapFactory.decodeResource(this.getResources(), com.happygoatstudios.bt.R.drawable.cancel_tiny);
		textSelectionCopyBitmap = BitmapFactory.decodeResource(this.getResources(), com.happygoatstudios.bt.R.drawable.copy_tiny);
		textSelectionSwapBitmap = BitmapFactory.decodeResource(this.getResources(), com.happygoatstudios.bt.R.drawable.swap);
		
		textSelectionIndicatorPaint.setStyle(Paint.Style.STROKE);
		textSelectionIndicatorPaint.setStrokeWidth(2);
		textSelectionIndicatorPaint.setColor(0xFFFF0000);
		textSelectionIndicatorPaint.setAntiAlias(true);
		
		textSelectionIndicatorBackgroundPaint.setStyle(Paint.Style.FILL);
		textSelectionIndicatorBackgroundPaint.setColor(0x770000FF);
		
		textSelectionIndicatorCirclePaint.setStyle(Paint.Style.STROKE);
		textSelectionIndicatorCirclePaint.setStrokeWidth(2);
		textSelectionIndicatorCirclePaint.setColor(0xFFFFFFFF);
		DashPathEffect dpe = new DashPathEffect(new float[]{3,3}, 0);
		
		textSelectionIndicatorCirclePaint.setPathEffect(dpe);
		textSelectionIndicatorCirclePaint.setAntiAlias(true);
		//homeWidgetDrawable.setBounds(homeWidgetRect);
		this.settings = settings;
		this.settings.setListener(this);
		borderPaint.setStrokeWidth(5);
		borderPaint.setColor(0xFF444488);
		new_text_in_buffer_indicator = new View(this.getContext());
		the_tree = new TextTree();
		if(name.equals("mainDisplay")) {
			the_tree.debugLineAdd = true;
		}
		buffer = new TextTree();
		//this.setOnTouchListener(textSelectionTouchHandler);
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_SCROLLLEFT:
					Log.e("window","handler scrollleft");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollLeft(true);
					break;
				case MESSAGE_SCROLLRIGHT:
					Log.e("window","handler scrollright");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollRight(true);
					break;
				case MESSAGE_SCROLLDOWN:
					Log.e("window","handler scrolldown");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollDown(true);
					break;
				case MESSAGE_SCROLLUP:
					Log.e("window","handler scrollup");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollUp(true);
					break;
				case MESSAGE_STARTSELECTION:
					Window.this.startSelection(msg.arg1,msg.arg2);
					break;
				case MESSAGE_ENCODINGCHANGED:
					Window.this.updateEncoding((String)msg.obj);
					break;
				case MESSAGE_SETTINGSCHANGED:
					Window.this.doUpdateSetting(msg.getData().getString("KEY"),msg.getData().getString("VALUE"));
					//settings.setOption(msg.getData().getString("KEY"),msg.getData().getString("VALUE"));
					break;
				case MESSAGE_CLEARTEXT:
					Log.e("clear","clearing buffer");
					the_tree.empty();
					buffer.empty();
					//buffer.
					break;
				case MESSAGE_SHUTDOWN:
					Window.this.shutdown();
					break;
				case MESSAGE_FLUSHBUFFER:
					Window.this.flushBuffer();
					break;
				case MESSAGE_DRAW:
					Window.this.invalidate();
					break;
				case MESSAGE_ADDTEXT:
					//String str = new String((byte[])msg.obj);
					//Log.e("window","adding text:\n"+str);
					Window.this.addBytes((byte[])msg.obj, false);
					break;
				case MSG_UPPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
					break;
				case MSG_NORMALPRIORITY:
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					break;
				case MESSAGE_PROCESSXCALLS:
					Window.this.xcallS(msg.getData().getString("FUNCTION"),(String)msg.obj);
					
					break;
				}
			}
		};
		
		
		//lua startup.
		mOwner = owner;
		//mManager = manager;
		//mContext = context;
		
		this.mainHandler = mainWindowHandler;
		
		
		/*if(x == 0 && y ==0 && width==0 && height == 0) {
			constrictWindow = false;
		} else {
			constrictWindow = true;
			mAnchorTop = y;
			mAnchorLeft = x;
			mWidth = width;
			mHeight = height;
			//this.he
			
		}*/
		clearme.setColor(0x00000000);
		clearme.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		//mBounds = new Rect(mAnchorLeft,mAnchorTop,mAnchorLeft+width,mAnchorTop+height);
		
		mName = name;
		//initLua();
		
		
		
		mAnchorTop = 0;
		mAnchorLeft = 0;
		//calculateCharacterFeatures(width,height);
		
		mSelectionIndicatorBitmap = Bitmap.createBitmap(2*selectionIndicatorHalfDimension, 2*selectionIndicatorHalfDimension, Bitmap.Config.ARGB_8888);
		mSelectionIndicatorCanvas = new Canvas(mSelectionIndicatorBitmap);
		
		int full = selectionIndicatorHalfDimension * 2;
		int third = full / 3;
		
		selectionIndicatorLeftButtonRect.set(third, 0, 2*third, 40);
		selectionIndicatorUpButtonRect.set(0, third, 40, 2*third);
		selectionIndicatorRightButtonRect.set(full-40, third, full, 2*third);
		selectionIndicatorDownButtonRect.set(third, 2*third, 2*third, full);
		selectionIndicatorCenterButtonRect.set(third, third, 2*third, 2*third);
		
		selectionIndicatorRect.set(0,0,full,full);
		
	}
	
	private void startSelection(int line,int column) {
		
		theSelection = the_tree.getSelectionForPoint(line,column);
		if(theSelection == null) {
			firstPress = true;
		} else {
			this.setOnTouchListener(textSelectionTouchHandler);
			selectedSelector = theSelection.end;
			moveWidgetToSelector(selectedSelector);
			
			//start the window buffering so it does not interfere with our biz-nas.
			this.setBufferText(true);
			this.invalidate();
		}
		
	}



	/*protected void shutdown() {
		mManager.shutdown(this);
	}*/

	protected void updateEncoding(String value) {
		the_tree.setEncoding(value);
	}



	protected void doUpdateSetting(String key, String value) {
		settings.setOption(key, value);
	}



	public void setTWidth(int height) {
		mWidth=height;
		//the_tree.se
		calculateCharacterFeatures(mWidth,mHeight);
	}
	
	public int getTWidth() {
		return mWidth;
	}
	
	private boolean featuresChanged = true;
	public void calculateCharacterFeatures(int width,int height) {
		
		//Log.e("WINDOW","WINDOW:" + mName + " character features for w/h:" + width+ " : "+height);
		if(height == 0 && width == 0) {
			return;
		}
		//int newlines =  (int) (height / PREF_LINESIZE);
		//if(newlines != CALCULATED_LINESINWINDOW) {
			CALCULATED_LINESINWINDOW = (int) (height / PREF_LINESIZE);
		//	featuresChanged = true;
		//}
		
		featurePaint.setTypeface(PREF_FONT);
		featurePaint.setTextSize(PREF_FONTSIZE);
		one_char_is_this_wide = (int)Math.ceil(featurePaint.measureText("a")); //measure a single character
		//int newrows = (width / one_char_is_this_wide);
		//if(newrows != CALCULATED_ROWSINWINDOW) {
			CALCULATED_ROWSINWINDOW = (width / one_char_is_this_wide);
		//	featuresChanged = true;
		//}
		
		//if(!featuresChanged) {
		//	return;
		//}
		//featuresChanged = false;
		//Log.e("WINDOW","WINDOW("+mName+"):" + CALCULATED_LINESINWINDOW + " drawable lines. RE: " + (CALCULATED_LINESINWINDOW*PREF_LINESIZE) + " target:" + height);
		//leftOver = height - CALCULATED_LINESINWINDOW*PREF_LINESIZE;
		

		
		
		selectionIndicatorPaint.setTextSize(SELECTIONINDICATOR_FONTSIZE);
		selectionIndicatorPaint.setTypeface(PREF_FONT);
		selectionIndicatorPaint.setAntiAlias(true);
		one_selection_char_is_this_wide = (int) Math.ceil(selectionIndicatorPaint.measureText("a"));
		selectionIndicatorVectorX = one_char_is_this_wide + selectionIndicatorHalfDimension;
		if(automaticBreaks) {
			this.setLineBreaks(0);
		}
		
		if(the_tree.getBrokenLineCount() == 0) {
			jumpToZero();
		}
		
	}

	Paint featurePaint = new Paint();
	
	public void viewCreate() {
		
		windowShowing = true;
		
		
	}

	private String mName = null;
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public void viewDestroy() {
		boolean retry = true;
		windowShowing = false;
		
		//Log.e("BYTEVIEW","surfaceViewDestroyed");
//		while(retry) {
//			try{
//				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_SHUTDOWN);
//				//Log.e("WINDOW","SHUTTING DOWN DRAW THREAD");
//				_runner.join();
//				//Log.e("WINDOW","SUCCESSFULY SHUT DOWN DRAW THREAD");
//				retry = false;
//			} catch (InterruptedException e) { }
//		}
		
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
	//boolean fuckyou = false;
	long target = 0;
	boolean homeWidgetFingerDown = false;
	int touchStartY;
	@Override
	public boolean onTouchEvent(MotionEvent t) {
		//if(fuckyou) {
		//switch(t.getActionMasked()) {
		
		//}
		//super.onTouchEvent(t);	
		
			//return true;
		//}
		//long now = System.currentTimeMillis();
		//if(now < target) {
			//normal
		//	return true;
		//}
		//target = now + 1000;
		//Log.e("WINDOW",mName + "onTouchEvent");
		boolean retval = false;
		boolean noFunction = false;
		//L.getGlobal("debug");
		//L.getField(L.getTop(), "traceback");
		//L.remove(-2);
		
		/*L.getGlobal("OnTouchEvent");
		if(!L.isFunction(L.getTop())) {
			//return false;
			noFunction = true;
		}
		if(!noFunction) {
			L.pushJavaObject(t);
			
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				Log.e("LUAWINDOW","Error in onTouchEvent:"+L.getLuaObject(-1).getString());
			} else {
				retval = L.getLuaObject(-1).getBoolean();
				//Log.e("LUAWINDOW","TouchEvent called");
			}
			if(retval) return true;
		}*/
		
		
		if(the_tree.getBrokenLineCount() != 0) {
			Rect rect = new Rect();
			if(!finger_down) {
				if(constrictWindow) {
					rect.top = mAnchorTop;
					rect.left = mAnchorLeft;
					rect.right = mAnchorLeft + mWidth;
					rect.bottom = mAnchorTop + mHeight;
				} else {
					rect.top = 0;
					rect.left = 0;
					rect.right = mWidth;
					rect.bottom = mHeight;
				}
				
				Point point = new Point();
				point.x = (int) t.getX();
				point.y = (int) t.getY();
				if(!rect.contains((int)t.getX(),(int)t.getY())) {
					return false;
				}
			}
			
			
			synchronized(token) {
			if(t.getAction() == MotionEvent.ACTION_DOWN) {
				
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
					}
				}
				
				//calculate row/col
				float x = t.getX();
				float y = t.getY();
				
				//convert y to be at the bottom of the screen.
				
				y = (float) ((float)this.getHeight() - y + (scrollback-SCROLL_MIN));
				
				float xform_to_line = y / (float)PREF_LINESIZE;
				int line = (int)Math.floor(xform_to_line);
				
				float xform_to_column = x / (float)one_char_is_this_wide;
				int column = (int)Math.floor(xform_to_column);
				if(textSelectionEnabled) {
					Log.e("sfdsf","starting text selection");
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_STARTSELECTION, line, column), 1500);
				} else {
					Log.e("sfdsf","not starting text selection");
				}
				
				if(homeWidgetShowing) {
					if(homeWidgetRect.contains((int)x,(int)t.getY())) {
						homeWidgetFingerDown = true;
					}
				}
			}
			
			if(!increadedPriority) {
				increadedPriority = true;
			}
			
			if(t.getAction() == MotionEvent.ACTION_MOVE) {
				
	
				//Float now_x = new Float(t.getX(t.getPointerId(0)));
				Float now_y = new Float(t.getY(t.getPointerId(0)));
				
				
	
				float thentime = pre_event.getEventTime();
				float nowtime = t.getEventTime();
				
				float time = (nowtime - thentime) / 1000.0f; //convert to seconds
				
				float prev_y = pre_event.getY(t.getPointerId(0));
				float dist = now_y - prev_y;
				diff_amount = (int)dist;
				
				if(Math.abs(now_y - start_y) > PREF_LINESIZE*1.5) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
				}
				
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
				
				if(Math.abs(now_y - start_y) > PREF_LINESIZE*1.5) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
					//homeWidgetFingerDown = false;
					
				}
				
				if(Math.abs(diff_amount) > 5) {
					
					pre_event = MotionEvent.obtainNoHistory(t);
				}
				
	
			}
			
			int pointers = t.getPointerCount();
			for(int i=0;i<pointers;i++) {
				
				Float y_val = new Float(t.getY(t.getPointerId(i)));
				Float x_val = new Float(t.getX(t.getPointerId(i)));
				bx = x_val.intValue();
				by = y_val.intValue();
				
				prev_y = y_val;
			}
			
			
			if(t.getAction() == (MotionEvent.ACTION_UP)) {
				
				pre_event = null;
				prev_y = new Float(0);
				//mHandler.removeMessages(Window.MSG_BUTTONDROPSTART);
		        
		        //reset the priority
		        increadedPriority = false;
		        //_runner.dcbPriority(Process.THREAD_PRIORITY_DEFAULT);
		        
	
		        pre_event = null;
		        finger_down=false;
		        finger_down_to_up = true;
		         
				if(touchInLink > -1) {
					dataDispatch.sendMessage(dataDispatch.obtainMessage(MainWindow.MESSAGE_LAUNCHURL, linkBoxes.get(touchInLink).getData()));
			        touchInLink = -1;
				}
				
				//if(Math.abs(now_y - start_y) > PREF_LINESIZE*1.5) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
				//}
					
				if(homeWidgetShowing && homeWidgetFingerDown) {
					if(homeWidgetRect.contains((int)t.getX(),(int)t.getY())) {
						scrollback = SCROLL_MIN;
						homeWidgetFingerDown = false;
						this.invalidate();
					}
				}
		        
			}
			
			}
			this.invalidate();
			return true; //consumes
		}
		
		return false;
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
						mHandler.sendEmptyMessage(Window.MSG_UPPRIORITY);
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
						mHandler.sendEmptyMessage(Window.MSG_NORMALPRIORITY);
					}
						
					if(scrollback <= SCROLL_MIN) {
						scrollback = SCROLL_MIN;
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						mHandler.sendEmptyMessage(Window.MSG_NORMALPRIORITY);

						//mHandler.sendEmptyMessage(Window.MSG_CLEAR_NEW_TEXT_INDICATOR);
					}
					
					if(scrollback >= ((the_tree.getBrokenLineCount() * PREF_LINESIZE))) {
						//Log.e("WINDOW","UPPER CAP OF THE BUFFER REACHED!");
						scrollback = (double)((the_tree.getBrokenLineCount() * PREF_LINESIZE));
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						mHandler.sendEmptyMessage(Window.MSG_NORMALPRIORITY);
						
					}
				}

				
			}
	}
	
	Paint p = new Paint();

	
	Paint b = new Paint();
	
	Paint linkColor = null;
	private Double SCROLL_MIN = 24d;
	private Double scrollback = SCROLL_MIN;
	ListIterator<TextTree.Line> screenIt = null;// = the_tree.getLines().iterator();
	Iterator<Unit> unitIterator = null;
	private int mLinkBoxHeightMinimum = 20;
	
	boolean hasDrawRoutine = true;
	//private boolean drawn = false;
	public void onDraw(Canvas c) {
		/*if(drawn && !drawOnDemand) {
			Log.e("Window","Not drawing ("+mName+")-drawn and not draw on demand");
			return;//dont draw
			
		}*/
		//drawn = true;
		
		if(selectedSelector != null) {
			//int full = selectionIndicatorHalfDimension * 2;
			//int third = (selectionIndicatorHalfDimension * 2) / 3;
			int color = scroller_paint.getColor();
			int newcolor = 0xFF000000 | color;
			scroller_paint.setColor(newcolor);
			mSelectionIndicatorCanvas.drawRect(selectionIndicatorLeftButtonRect, scroller_paint);
			mSelectionIndicatorCanvas.drawRect(selectionIndicatorUpButtonRect, scroller_paint);
			mSelectionIndicatorCanvas.drawRect(selectionIndicatorRightButtonRect, scroller_paint);
			mSelectionIndicatorCanvas.drawRect(selectionIndicatorDownButtonRect, scroller_paint);
			//mSelectionIn
			scroller_paint.setColor(color);
			
			mSelectionIndicatorCanvas.save();
			mSelectionIndicatorCanvas.clipPath(selectionIndicatorClipPath);
			mSelectionIndicatorCanvas.drawColor(0xFF444444);
			
		}
		int startline2=0,startcol=0,endline=0,endcol=0;
		if(theSelection  != null) {

			if(theSelection.start.line == theSelection.end.line) {
				startline2 = theSelection.start.line;
				endline = theSelection.start.line;
				if(theSelection.end.column < theSelection.start.column) {
					startcol = theSelection.end.column;
					endcol = theSelection.start.column;
				} else{
					startcol = theSelection.start.column;
					endcol = theSelection.end.column;
				}
			} else if(theSelection.end.line >  theSelection.start.line) {
				startline2 = theSelection.end.line;
				startcol = theSelection.end.column;
				endline = theSelection.start.line;
				endcol = theSelection.start.column;
			} else {
				startline2 = theSelection.start.line;
				startcol = theSelection.start.column;
				endline = theSelection.end.line;
				endcol = theSelection.end.column;
			}
		}
		
		if(mName != null && mName.equals("map_window")) {
			long xtmp = 10;
			xtmp += System.currentTimeMillis();
		}
		
		if(the_tree.getBrokenLineCount() != 0) {
			if(linkColor == null) {
				
				linkColor = new Paint();
				linkColor.setAntiAlias(true);
				linkColor.setColor(linkHighlightColor);
			}
			
			linkColor.setColor(linkHighlightColor);
			//try {	
			//Log.e("ondraw","Calculating scrollback before:" + scrollback);
			calculateScrollBack();
			//Log.e("ondraw","Calculating scrollback after:" + scrollback);
			c.save();
			Rect clip = new Rect();
			if(constrictWindow) {
			
				clip.top = mAnchorTop;
				clip.left = mAnchorLeft;
				clip.right = clip.left + mWidth;
				clip.bottom = clip.top + mHeight;
				
			} else {
				clip.top = 0;
				clip.left = 0;
				clip.right = mWidth;
				clip.bottom = mHeight;
			}
			c.clipRect(clip);
			if(constrictWindow) {
				c.translate(mAnchorLeft, mAnchorTop);
			}
			//now 0,0 is the lower left hand corner of the screen, and X and Y both increase positivly.
			Paint b = new Paint();
			b.setColor(0xFF0A0A0A);
			c.drawColor(0xFF0A0A0A); //fill with black
			//c.drawColor(0xFF0A0A0A);
			c.drawRect(0,0,clip.right-clip.left,clip.top-clip.bottom,b);
			p.setTypeface(PREF_FONT);
			p.setAntiAlias(true);
			p.setTextSize(PREF_FONTSIZE);
			p.setColor(0xFFFFFFFF);
			
			//float char_width = p.measureText("T");
			
			float x = 0;
			float y = 0;
			if(PREF_LINESIZE * CALCULATED_LINESINWINDOW < this.getHeight()) {
				
				y = ((PREF_LINESIZE * CALCULATED_LINESINWINDOW) - this.getHeight()) - PREF_LINESIZE;
				Log.e("STARTY","STARTY IS:"+y);
			}
			
			
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
			//synchronized(synch) {
			
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
				this.invalidate();
				return;
			}
			screenIt = bundle.getI();
			y = bundle.getOffset();
			//Log.e("STARTY","STARTY:"+y);
			int extraLines = bundle.getExtraLines();
			if(screenIt == null) { return;}
			
			int startline = bundle.getStartLine();
			int workingline = startline;
			int workingcol = 0;
			
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
				
				int linemode = 0;
				if(startline2 == endline && startline2 == workingline) {
					linemode = 1;
				} else if(startline2 == workingline) {
					linemode = 2;
				} else if(startline2 > workingline && endline < workingline) {
					
					linemode = 3;
				} else if(endline == workingline) {
					Log.e("window","doing linemode 4 for line:"+workingline + " ypos:"+y);
					linemode = 4;
					
				}
				
				while(unitIterator.hasNext()) {
					Unit u = unitIterator.next();
					//p.setColor(color)
					boolean useBackground = false;
					if(b.getColor() != 0xFF0A0A0A && b.getColor() != 0xFF000000) {
						useBackground = true;
					}
					

					//if(u instanceof TextTree.Text && !(u instanceof TextTree.WhiteSpace)) {
					if(u instanceof TextTree.Text) {
						boolean doIndicator = false;
						int indicatorlineoffset = 0;
						boolean backgroundSelection = false;
						if(selectedSelector != null && selectedSelector.line == workingline) {
							doIndicator = true;
						} else if(selectedSelector != null && Math.abs(selectedSelector.line -workingline) < 3) {
							doIndicator = true;
							indicatorlineoffset = selectedSelector.line - workingline;
						}
						
						if(theSelection  != null) {
							
							//Log.e("Window","doing selection run: start={"+startline2+","+startcol+"} end={"+endline+","+endcol);
							switch(linemode) {
							case 1:
								int finishCol = workingcol + ((TextTree.Text)u).bytecount;
								if(finishCol > startcol && finishCol-1 <= endcol){
									if((finishCol - startcol) < ((TextTree.Text)u).bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * one_char_is_this_wide;
										int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, textSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, textSelectionIndicatorBackgroundPaint);
									}
								} else if(finishCol > endcol) {
									if((finishCol - endcol) < ((TextTree.Text)u).bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * one_char_is_this_wide;
										//int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
										c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, textSelectionIndicatorBackgroundPaint);
									} else {
										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
									}
								} else {
									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
								}
								break;
							case 2:
								finishCol = workingcol + ((TextTree.Text)u).bytecount;
								if(finishCol > startcol) {
									if((finishCol - startcol) < ((TextTree.Text)u).bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * one_char_is_this_wide;
										int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, textSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, textSelectionIndicatorBackgroundPaint);
									}
								} else {
									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
								}
//								} else if(finishCol > endcol) {
//									if((finishCol - endcol) < ((TextTree.Text)u).bytecount) {
//										int overshoot = endcol - workingcol + 1;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//										//int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
//										c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
//									} else {
//										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//									}
//								} else {
//									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//								}
								break;
							case 3:
								
								c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, textSelectionIndicatorBackgroundPaint);
								break;
							case 4:
								finishCol = workingcol + ((TextTree.Text)u).bytecount;
//								if(finishCol > startcol && finishCol-1 <= endcol){
//									if((finishCol - startcol) < ((TextTree.Text)u).bytecount) {
//										int overshoot = startcol - workingcol;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//									int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
//										c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, scroller_paint);
//									} else {
//										c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//									}
								Log.e("ACEFJSAf","x:"+x+" y:"+y +" text:"+((TextTree.Text)u).getString()+"|" + " finishCol="+finishCol+" workingCol="+workingcol + " endcol="+endcol);
								if(finishCol >= endcol) {
									if((finishCol - endcol) < ((TextTree.Text)u).bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * one_char_is_this_wide;
										//int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
										c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
									} else {
										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
									}
								} else {
									c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, textSelectionIndicatorBackgroundPaint);
								}
								break;
							default:
								break;
							}
						}
							
//							if(startline2 == endline && startline2 == workingline) {
//								int finishCol = workingcol + ((TextTree.Text)u).bytecount;
//								if(finishCol > startcol && finishCol-1 <= endcol){
//									if((finishCol - startcol) < ((TextTree.Text)u).bytecount) {
//										int overshoot = startcol - workingcol;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//										int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
//										c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, scroller_paint);
//									} else {
//										c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//									}
//								} else if(finishCol > endcol) {
//									if((finishCol - endcol) < ((TextTree.Text)u).bytecount) {
//										int overshoot = endcol - workingcol + 1;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//										//int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
//										c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
//									} else {
//										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//									}
//								} else {
//									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//								}
//							} else if(startline2 == workingline) {
//								int finishCol = workingcol + ((TextTree.Text)u).bytecount;
//								if((finishCol - startcol) < ((TextTree.Text)u).bytecount) {
//									int overshoot = startcol - workingcol + 1;
//									int overshootPixels = overshoot * one_char_is_this_wide;
//									//int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
//									c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
//								} else {
//									c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//								}
//							} else if(startline2 > workingline && endline < workingline) {
//								c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//								Log.e("wondow","drawing in between line:" +workingline+ " ypos:"+y);
//							} else if(endline == workingline) {
//								int finishCol = workingcol + ((TextTree.Text)u).bytecount;
//								if((finishCol - endcol) < ((TextTree.Text)u).bytecount) {
//									int overshoot = endcol - workingcol;
//									int overshootPixels = overshoot * one_char_is_this_wide;
//									int stringWidth = (int) p.measureText(((TextTree.Text)u).getString());
//									c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, scroller_paint);
//								} else {
//									c.drawRect(x, y - p.getTextSize(), x + p.measureText(((TextTree.Text)u).getString()), y+5, scroller_paint);
//								}
//							}
//						}
						
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
							
							if(doIndicator) {
								int unitEndCol = workingcol + (((TextTree.Text)u).bytecount-1);
								if(unitEndCol > selectedSelector.column - 10 && workingcol < selectedSelector.column +10) {
									float size = p.getTextSize();
									p.setTextSize(30);
									int overshoot = (workingcol - selectedSelector.column);
									int ix = 0,iy=SELECTIONINDICATOR_FONTSIZE;
									//if(overshoot > 0) {
										ix = (int) (selectionIndicatorHalfDimension + (overshoot*one_selection_char_is_this_wide) - 0.5*one_selection_char_is_this_wide);
										iy = (int) (selectionIndicatorHalfDimension+(0.5*SELECTIONINDICATOR_FONTSIZE)) + (indicatorlineoffset*SELECTIONINDICATOR_FONTSIZE);
									
									
									
									mSelectionIndicatorCanvas.drawText(((TextTree.Text)u).getString(), ix, iy, p);
									
									p.setTextSize(size);
								}
								
							}
							c.drawText(((TextTree.Text)u).getString(),x,y,linkColor);
							x += p.measureText(((TextTree.Text)u).getString());
							
						} else {
							//p.setUnderlineText(false);
							if(useBackground) {
								//Log.e("WINDOW","DRAWING BACKGROUND TEXT: B:" + Integer.toHexString(b.getColor()) + " P:" + Integer.toHexString(p.getColor()));
							}
							boolean backGroundSelection = false;
							
							if(doIndicator) {
								int unitEndCol = workingcol + (((TextTree.Text)u).bytecount-1);
								if(unitEndCol > selectedSelector.column - 10 && workingcol < selectedSelector.column +10) {
									float size = p.getTextSize();
									p.setTextSize(30);
									int overshoot = (workingcol - selectedSelector.column);
									int ix = 0,iy=SELECTIONINDICATOR_FONTSIZE;
									//if(overshoot > 0) {
										ix = (int) (selectionIndicatorHalfDimension + (overshoot*one_selection_char_is_this_wide) - 0.5*one_selection_char_is_this_wide);
										iy = (int) (selectionIndicatorHalfDimension+(0.5*SELECTIONINDICATOR_FONTSIZE)) + (indicatorlineoffset*SELECTIONINDICATOR_FONTSIZE);
									
									
									
									mSelectionIndicatorCanvas.drawText(((TextTree.Text)u).getString(), ix, iy, p);
									
									p.setTextSize(size);
								}
								
							}
							workingcol += ((TextTree.Text)u).bytecount;
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
						} else if(u instanceof TextTree.Break) {
							workingline = workingline -1;
							if(startline2 == endline && startline2 == workingline) {
								linemode = 1;
							} else if(startline2 == workingline) {
								linemode = 2;
							} else if(startline2 > workingline && endline < workingline) {
								
								linemode = 3;
							} else if(endline == workingline) {
								Log.e("window","doing linemode 4 for line:"+workingline + " ypos:"+y);
								linemode = 4;
								
							} else {
								linemode = -1;
							}
						}
						
						y = y + PREF_LINESIZE;
						x = 0;
						//workingline = workingline - 1;
						drawnlines++;
						workingcol = 0;
						if(drawnlines > CALCULATED_LINESINWINDOW + extraLines) {
							stop = true;
						}
					}
				}
				workingline = workingline - 1;
				workingcol = 0;
				l.resetIterator();
			}
			
			//}
			showScroller(c);
			c.restore();
			if(Math.abs(fling_velocity) > PREF_LINESIZE) {
				//this.sendEmptyMessageDelayed(MSG_DRAW, 3); //throttle myself, just a little bit.
				//this.invalidate();
				//Log.e("SFS","fling redrawing");
				//fling_velocity = 0;
				this.mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW,3);
			} else {
				fling_velocity = 0;
			}
		
		}
		
		//phew, do the lua stuff, and lets be done with this.
		c.save();
		if(constrictWindow) {
		c.clipRect(mAnchorLeft, mAnchorTop, mAnchorLeft+mWidth, mAnchorTop+mHeight);
		c.translate(mAnchorLeft, mAnchorTop);
		}
		//c.drawBitmap(bmp, 0, 0, null);
		if(hasDrawRoutine){
			if(L != null) {
				L.getGlobal("debug");
				L.getField(L.getTop(), "traceback");
				L.remove(-2);
				
				
				L.getGlobal("OnDraw");
				if(L.isFunction(L.getTop())) {
					L.pushJavaObject(c);
					
					
					
					int ret = L.pcall(1, 1, -3);
					if(ret != 0) {
						Log.e("LUAWINDOW","Error calling OnDraw: " + L.getLuaObject(-1).toString());
					} else {
						//Log.e("LUAWINDOW","OnDraw success!");
						//hasDrawRoutine = false;
					}
				} else {
					hasDrawRoutine = false;
				}
			}
		} else {
			//Log.e("DRAW","Skipping draw routine cuz there is none.");
		}
		
		
		//omg, after *all* of that we still have to draw the borders.
		//ArrayList<Border> borders = mLayerManager.borders;
		
//		for(int i = 0;i<borders.size();i++) {
//			Border b = borders.get(i);
//			c.drawLine(b.p1.x, b.p1.y, b.p2.x, b.p2.y, p);
//		}
		if(edgeLeft) {
			c.drawLine(0, 0, 0, mHeight, borderPaint);
		}
		if(edgeRight) {
			c.drawLine(mWidth, 0, mWidth, mHeight, borderPaint);
		}
		if(edgeTop) {
			c.drawLine(0, 0, mWidth, 0,borderPaint);
		}
		if(edgeBottom) {
			c.drawLine(0, mHeight, mWidth, mHeight, borderPaint);
		}
		
		c.restore();
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
	private boolean homeWidgetShowing = false;
	Rect scrollerRect = new Rect();
	public void showScroller(Canvas c) {
		scroller_paint.setColor(0xFFFF0000);
		
		if(the_tree.getBrokenLineCount() < 1) {
			return; //no scroller to show.
		}
		
		if(scrollback > SCROLL_MIN) {
			homeWidgetShowing = true;
			//c.save();
			//c.translate(homeWidgetRect.left, homeWidgetRect.bottom);
			//homeWidgetDrawable.draw(c);
			//c.restore();
			//homeWidgetDrawable.draw(c);
			c.drawBitmap(homeWidgetDrawable, homeWidgetRect.left, homeWidgetRect.top, null);
		} else {
			homeWidgetShowing = false;
		}
		
		double scrollerSize = 0.0f;
		double scrollerPos = 0.0f;
		double posPercent = 0.0f;
		
		float workingHeight = mHeight;
		float workingWidth = mWidth;
		
		if(constrictWindow) {
			workingHeight = mHeight;
			workingWidth = mWidth;
		}
		
		Float windowPercent = workingHeight / (the_tree.getBrokenLineCount()*PREF_LINESIZE);
		if(windowPercent > 1) {
			//then we have but 1 page to show
			return;
		} else {
			scrollerSize = windowPercent*workingHeight;
			posPercent = (scrollback - (workingHeight/2))/(the_tree.getBrokenLineCount()*PREF_LINESIZE);
			scrollerPos = workingHeight*posPercent;
			scrollerPos = workingHeight-scrollerPos;
		}
		
		int blue_value = (int) (-1*255*posPercent + 255);
		int red_value = (int) (255*posPercent);
		int alpha_value = (int) ((255-70)*posPercent+70);
		int final_color = android.graphics.Color.argb(alpha_value, red_value, 100, blue_value);
		scroller_paint.setColor( final_color);
		float density = this.getResources().getDisplayMetrics().density;
		scrollerRect.set((int)workingWidth-(int)(2*density),(int)(scrollerPos - scrollerSize/2),(int)workingWidth,(int)(scrollerPos + scrollerSize/2));
		
		c.drawRect(scrollerRect, scroller_paint);
		
		
		if(theSelection != null) {
			//compute rects for the guys.
			//compute the current line in pixels from the bottom of the screen.
			int currentLine = theSelection.start.line * PREF_LINESIZE;
			currentLine = (int) (currentLine - (scrollback - SCROLL_MIN));
			
			
			int startBottom = (int) (this.getHeight() - currentLine);
			int startTop = startBottom - PREF_LINESIZE;
			int startLeft = theSelection.start.column * one_char_is_this_wide;
			int startRight = startLeft + one_char_is_this_wide;
			
			currentLine = theSelection.end.line * PREF_LINESIZE;
			currentLine = (int) (currentLine - (scrollback - SCROLL_MIN));
			
			int endBottom = (int) (this.getHeight() - currentLine);
			int endTop = endBottom - PREF_LINESIZE;
			int endLeft = theSelection.end.column * one_char_is_this_wide;
			int endRight = endLeft + one_char_is_this_wide;
			
			//int scroll_from_bottom = (int) (scrollback-SCROLL_MIN);
			
			c.drawRect(startLeft, startTop-2, startRight, startBottom-2, textSelectionIndicatorPaint);
			c.drawRect(endLeft, endTop-2, endRight, endBottom-2, textSelectionIndicatorPaint);
			
			int x=0,y=0;
			if(selectedSelector == theSelection.end) {
				x = endLeft + (endRight - endLeft)/2;
				y = endTop + (endBottom - endTop)/2;
			} else {
				x = startLeft + (startRight - startLeft)/2;
				y = startTop + (startBottom - startTop)/2;
			}
			
			c.drawCircle(x, y-2, 50, textSelectionIndicatorCirclePaint);
		}
		
		if(selectedSelector != null) {
			//this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			//c.save();
			
			
			//c.clipPath(path);
			mSelectionIndicatorCanvas.restore();
			Paint edgePaint = new Paint();
			edgePaint.setStyle(Paint.Style.STROKE);
			edgePaint.setStrokeWidth(6);
			edgePaint.setAntiAlias(true);
			edgePaint.setColor(0xFFAA22AA);
			
			mSelectionIndicatorCanvas.drawPath(selectionIndicatorClipPath, edgePaint);
			
			//draw the cancel, start, end and copy buttons.
			Paint cancelPaint = new Paint();
			cancelPaint.setStyle(Paint.Style.FILL);
			cancelPaint.setAntiAlias(true);
			cancelPaint.setColor(0xFFFF0000);
			int third = (selectionIndicatorHalfDimension*2)/3;
			mSelectionIndicatorCanvas.drawBitmap(textSelectionCopyBitmap, 0,0, null);
			mSelectionIndicatorCanvas.drawBitmap(textSelectionCancelBitmap, 0,2*third, null);
			mSelectionIndicatorCanvas.drawBitmap(textSelectionSwapBitmap, 2*third,0, null);
			
			//mSelectionIndicatorCanvas.drawCircle(third/2, third/2, 15, cancelPaint);
			//mSelectionIndicatorCanvas.drawCircle((2*third)+(third/2), third/2, 15, cancelPaint);
			
			//mSelectionIndicatorCanvas.drawCircle(third/2,(2*third)+(third/2) , 15, cancelPaint);
			//mSelectionIndicatorCanvas.drawCircle((2*third)+(third/2),(2*third)+(third/2) , 15, cancelPaint);
			
			float left = (float) (selectionIndicatorHalfDimension-(0.5*one_selection_char_is_this_wide));
			float top = (float)(selectionIndicatorHalfDimension-(0.5*SELECTIONINDICATOR_FONTSIZE));
			float right = (float)(selectionIndicatorHalfDimension+(0.5*one_selection_char_is_this_wide));
			float bottom = (float)(selectionIndicatorHalfDimension+(0.5*SELECTIONINDICATOR_FONTSIZE));
			
			c.drawBitmap(mSelectionIndicatorBitmap, widgetX-selectionIndicatorHalfDimension, widgetY-selectionIndicatorHalfDimension, null);
			c.drawRect(left+(widgetX-selectionIndicatorHalfDimension),top+(widgetY-selectionIndicatorHalfDimension),right+(widgetX-selectionIndicatorHalfDimension),bottom+(widgetY-selectionIndicatorHalfDimension), scroller_paint);		
			//c.restore();
			//this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
		
	}

	//Object synch = new Object();
	public void flushBuffer() {
		//synchronized(synch) {
			try {
				
					the_tree.addBytesImpl(buffer.dumpToBytes(false));
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			the_tree.prune();
		//}
		drawingIterator = null;
		this.invalidate();
	}
	
	
	public void setButtonHandler(Handler useme) {
		//realbuttonhandler = useme;
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
			SCROLL_MIN = mHeight-(double)(5*Window.this.getResources().getDisplayMetrics().density);
			scrollback = SCROLL_MIN;
			fling_velocity=0;
		}
				
	}

	public void doDelayedDraw(int i) {
		/*if(_runner == null || _runner.threadHandler == null || !_runner.isAlive()) return;
		if(!_runner.threadHandler.hasMessages(ByteView.DrawRunner.MSG_DRAW)) {
			_runner.threadHandler.sendEmptyMessageDelayed(DrawRunner.MSG_DRAW,i);
		} else {
			//Log.e("SLICK","VIEW ALREADY HAS DRAW MESSAGES");
		}*/
		//this.invalidate();
		if(!mHandler.hasMessages(MESSAGE_DRAW)) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW, i);
		}
	}

	public void setColorDebugMode(int i) {
		debug_mode = i;
		doDelayedDraw(1);
	}

	public void setEncoding(String pEncoding) {
		//encoding = pEncoding;
		//synchronized(token) {
			the_tree.setEncoding(pEncoding);
		//}
	}

	public void setCharacterSizes(int fontSize, int fontSpaceExtra) {
		PREF_FONTSIZE = fontSize;
		PREF_LINEEXTRA = fontSpaceExtra;
		PREF_LINESIZE = (int) (PREF_FONTSIZE + PREF_LINEEXTRA);
		calculateCharacterFeatures(mWidth,mHeight);
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
		
		//synchronized(synch) {
			if(i == 0) {
				if(CALCULATED_ROWSINWINDOW != 0) {
					the_tree.setLineBreakAt(CALCULATED_ROWSINWINDOW);
					//Log.e("BYTE","SET LINE BREAKS TO: " + CALCULATED_ROWSINWINDOW);
				} else {
					the_tree.setLineBreakAt(80);
					//Log.e("BYTE","SET LINE BREAKS TO DEFAULT BECAUSE CALCULATED WAS 0");
				}
				//jumpToZero();
				automaticBreaks = true;
			} else {
				the_tree.setLineBreakAt(i);
				automaticBreaks = false;
				//jumpToZero();
				//Log.e("BYTE","SET LINE BREAKS TO: " + i);
			}
		
		
			
		//}
		
		
//		if(_runner != null && _runner.threadHandler != null) {
//			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
//				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
//
//			}
//		}
		this.invalidate();
	}
	
	public void setWordWrap(boolean pIn ) {
		
		//synchronized(synch) {
			the_tree.setWordWrap(pIn);
		//}
		
			jumpToZero();
		
//			if(_runner != null && _runner.threadHandler != null) {
//				if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
//					_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
//	
//				}
//			}
			this.invalidate();
		//}
	}
	
	public void setLinkMode(LINK_MODE mode) {
		this.linkMode = mode;
	}
	
	public void setLinkColor(int linkColor) {
		this.linkHighlightColor = linkColor;
	}
	
	public void clearAllText() {
		//_runner.threadHandler.sendEmptyMessage(DrawRunner.MESSAGE_EMPTY_TREE);
		//synchronized(synch) {
			the_tree.empty();
		//}
	}
	
	public void addBytes(byte[] obj,boolean jumpToEnd) {
		
		synchronized(token) {
			addBytesImpl(obj,jumpToEnd);
		}
	}
	public void addBytesImpl(byte[] obj,boolean jumpToEnd) {
		if(obj.length == 0) return;
		
			if(bufferText) {
				//synchronized(synch) {
					buffer.addBytesImplSimple(obj);
				//}
				return;
			}
		
			int oldbrokencount = the_tree.getBrokenLineCount();
			double old_max = the_tree.getBrokenLineCount() * PREF_LINESIZE;
			//synchronized(synch) {
			try {
				the_tree.addBytesImpl(obj);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			int tmpcount = the_tree.getBrokenLineCount();
			drawingIterator = null;
			
			if(jumpToEnd) {
				scrollback = SCROLL_MIN;
				//mHandler.sendEmptyMessage(MSG_CLEAR_NEW_TEXT_INDICATOR);
			} else {
				if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) {
					scrollback = (double)mHeight;
				} else {
					if(scrollback > SCROLL_MIN + PREF_LINESIZE ) {
						//scrollback = oldposition * (the_tree.getBrokenLineCount()*PREF_LINESIZE);
						double new_max = the_tree.getBrokenLineCount()*PREF_LINESIZE;
						int lines = (int) ((new_max - old_max)/PREF_LINESIZE);
						
						scrollback += new_max - old_max;
						Log.e("BYTE",mName+"REPORT: old_max="+old_max+" new_max="+new_max+" delta="+(new_max-old_max)+" scrollback="+scrollback + " lines="+lines + " oldbroken="+oldbrokencount+ "newbroken="+the_tree.getBrokenLineCount());
						
					} else {
						scrollback = SCROLL_MIN;
					}
				
				}
				if(scrollback > mHeight) {
					if(!indicated) {
						if(fling_velocity > 0) {
							//play with no animation
							//new_text_in_buffer_indicator.startAnimation(indicator_on_no_cycle);
							//mHandler.sendEmptyMessage(MSG_SET_NEW_TEXT_INDICATOR);
						} else {
							//new_text_in_buffer_indicator.startAnimation(indicator_on);
							//mHandler.sendEmptyMessage(MSG_SET_NEW_TEXT_INDICATOR_ANIMATED);
							//indicated = true;
						}
						//Log.e("BYTE","REPORTED");
						indicated = true;
					}
				} else {
					//new_text_in_buffer_indicator.startAnimation(indicator_off);
					//mHandler.sendEmptyMessage(Window.MSG_CLEAR_NEW_TEXT_INDICATOR);
					indicated = false;
					//indicated = false;
				}
			}
			the_tree.prune();
			tmpcount = the_tree.getBrokenLineCount();
		//}
			//}
		
//		if(_runner != null && _runner.isAlive()) {
//			if(!_runner.threadHandler.hasMessages(DrawRunner.MSG_DRAW)) {
//				_runner.threadHandler.sendEmptyMessage(DrawRunner.MSG_DRAW);
//
//			}
//		}
		//if(drawOnDemand) {
		//	this.invalidate();
		//}
		this.invalidate();
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
		
		//synchronized(synch) {
			the_tree.setCullExtraneous(pIn);
		//}
			
	}
	
	private class IteratorBundle {
		private ListIterator<TextTree.Line> i;
		private Float offset;
		private int extraLines;
		private int startLine;
		public IteratorBundle(ListIterator<TextTree.Line> pI,double pOffset,int lines,int startline) {
			setI(pI);
			setOffset((float)pOffset);
			setExtraLines(lines);
			setStartLine(startline);
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
		public int getStartLine() {
			return startLine;
		}
		public void setStartLine(int startLine) {
			this.startLine = startLine;
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
		
		int startline = 0;
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
			while(drawingIterator.hasNext()) {drawingIterator.next(); startline += 1;}
			//return new IteratorBundle(the_tree.getLines().listIterator(the_tree.getLines().size()),under*pLineSize,0);
			return new IteratorBundle(drawingIterator,under*pLineSize,0,startline);
		}
		
		//double target = Math.floor(pY/pLineSize);
		
		while(drawingIterator.hasNext()) {
			//position = drawingIterator.nextIndex();
			Line l = drawingIterator.next();
			working_h += pLineSize * (1 + l.getBreaks());
			current += 1 + l.getBreaks();
			
			if(working_h >= pY) {
				int y = 0;
				if(PREF_LINESIZE * CALCULATED_LINESINWINDOW < this.getHeight()) {
					
					y = ((PREF_LINESIZE) * CALCULATED_LINESINWINDOW) - this.getHeight();
					//Log.e("STARTY","STARTY IS:"+y);
				}
				double delta = working_h - pY;
				double offset = delta - pLineSize;
				int extra = (int) Math.ceil(delta/pLineSize);
				//Log.e("SCREENIT","GET SCREEN ITERATOR, EXTRA:"+extra);
				if(drawingIterator.hasPrevious()) drawingIterator.previous();
				if(l.breaks > 0) {
					startline += l.breaks;
				}
				return new IteratorBundle(drawingIterator,-1*offset,extra,startline);
			} else {
				//next line
				//position++;
			}
			startline += 1 + l.getBreaks();
		}
		
		return new IteratorBundle(drawingIterator,pLineSize,0,startline);
		
	}

	public void setLinksEnabled(boolean hyperLinkEnabled) {
		the_tree.setLinkify(hyperLinkEnabled);
	}

	public boolean windowShowing = false;
	public boolean loaded() {
		
//		if(_runner == null || _runner.threadHandler == null || !_runner.isAlive()) {
//			if(_runner != null && !_runner.isAlive()) {
//				//Log.e("WINDOW","HOLY GOD THE THREAD IS DEAD");
//			}
//			return false;
//		}
		
		return windowShowing;
	}

	private IWindowCallback.Stub mCallback = new IWindowCallback.Stub() {

		public void rawDataIncoming(byte[] raw) throws RemoteException {
			mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADDTEXT, raw));
		}

		public boolean isWindowShowing() throws RemoteException {
			
			return true;
		}

		public String getName() throws RemoteException {
			// TODO Auto-generated method stub
			return Window.this.mName;
		}

		public void redraw() throws RemoteException {
			mHandler.sendEmptyMessage(Window.MESSAGE_FLUSHBUFFER);
		}

		public void shutdown() throws RemoteException {
			mHandler.sendEmptyMessage(MESSAGE_SHUTDOWN);
		}

		public void xcallS(String function, String str) throws RemoteException {
			Message msg = mHandler.obtainMessage(MESSAGE_PROCESSXCALLS,str);
			msg.getData().putString("FUNCTION", function);
			mHandler.sendMessage(msg);
		}

		public void clearText() throws RemoteException {
			mHandler.sendEmptyMessage(MESSAGE_CLEARTEXT);
		}

		@Override
		public void updateSetting(String key, String value)
				throws RemoteException {
			//mHandler.sendMessage(mHandler.ob)
			Message m = mHandler.obtainMessage(MESSAGE_SETTINGSCHANGED);
			m.getData().putString("KEY", key);
			m.getData().putString("VALUE", value);
			mHandler.sendMessage(m);
		}
		
		public void setEncoding(String value) {
			mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ENCODINGCHANGED,value));
		}
		
	};
	
	public IWindowCallback.Stub getCallback() {
		return mCallback;
	}

	/*public void setDisplayDimensions(int x, int y, int width, int height) {
		if(x ==0 && y==0 && height == 0 && width==0) {
			constrictWindow = false;
			return;
		}
		
		constrictWindow = true;
		constrictedHeight = height;
		constrictedWidth = width;
		
		mAnchorLeft = x;
		mAnchorTop = y;
		
		calculateCharacterFeatures(width,height);
		
	}*/

	public void setBufferText(boolean bufferText) {
		this.bufferText = bufferText;
	}

	public boolean isBufferText() {
		return bufferText;
	}

	protected void onSizeChanged(int w,int h,int oldw,int oldh) {
		mWidth = w;
		mHeight = h;
		calculateCharacterFeatures(mWidth,mHeight);
		//int diff = oldh - h;
		//scrollback -= diff;
		if(scrollback == SCROLL_MIN) {
			SCROLL_MIN = mHeight-(double)(5*Window.this.getResources().getDisplayMetrics().density);
			scrollback = SCROLL_MIN;
		} else {
			//we have to calculate the new scrollback position.
			double oldmin = SCROLL_MIN;
			SCROLL_MIN = mHeight-(double)(5*Window.this.getResources().getDisplayMetrics().density);
			scrollback -= oldmin - SCROLL_MIN;
		}
		
		homeWidgetRect.set(mWidth-homeWidgetDrawable.getWidth(),mHeight-homeWidgetDrawable.getHeight(),mWidth,mHeight);
		
		Float foo = new Float(0);
		//foo.
		
		if(L == null) return;
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		L.getGlobal("OnSizeChanged");
		if(L.getLuaObject(L.getTop()).isFunction()) {
			L.pushString(Integer.toString(w));
			L.pushString(Integer.toString(h));
			L.pushString(Integer.toString(oldw));
			L.pushString(Integer.toString(oldh));
			int ret = L.pcall(4, 1, -6);
			if(ret != 0) {
				Log.e("LUAWINDOW","Window("+mName+"): " + L.getLuaObject(-1).getString());
			}
		} else {
			//Log.e("LUAWINDOW","Window("+mName+"): No OnSizeChanged Function Defined.");
		}
	}
	private void pushTable(String key,Map<String,Object> map) {
		/*if(!key.equals("")) {
			L.pushString(key);
		}
		
		L.newTable();
		
		for(String tmp : map.keySet()) {
			Object o = map.get(tmp);
			if(o instanceof Map) {
				pushTable(tmp,(Map)o);
			} else {
				if(o instanceof String) {
					L.pushString(tmp);
					L.pushString((String)o);
					L.setTable(-3);
				}
			}
		}
		if(!key.equals("")) {
			L.setTable(-3);
		}*/
	}
	
	protected void xcallS(String string, String str) {
		if(L == null) return;
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(string);
		if(L.getLuaObject(-1).isFunction()) {
			
			//need to start iterating the given map, re-creating the table on the other side.
			//pushTable("",obj);
			L.pushString(str);
			
			int ret = L.pcall(1, 1, -3);
			if(ret !=0) {
				Log.e("LUAWINDOW","WindowXCallT Error:" + L.getLuaObject(-1).getString());
			} else {
				//success!
			}
			
		} else {
			
		}
	}
	

	private void initLua() {
		L.openLibs();
		
		InvalidateFunction iv = new InvalidateFunction(L);
		DebugFunction df = new DebugFunction(L);
		BoundsFunction bf = new BoundsFunction(L);
		OptionsMenuFunction omf = new OptionsMenuFunction(L);
		TableAdapterFunction taf = new TableAdapterFunction(L);
		PluginXCallSFunction pxcf = new PluginXCallSFunction(L);
		SheduleCallbackFunction scf = new SheduleCallbackFunction(L);
		CancelSheduleCallbackFunction cscf = new CancelSheduleCallbackFunction(L);
		try {
			iv.register("invalidate");
			df.register("debugPrint");
			bf.register("getBounds");
			omf.register("addOptionCallback");
			taf.register("getTableAdapter");
			pxcf.register("PluginXCallS");
			scf.register("scheduleCallback");
			cscf.register("cancelCallback");
		} catch (LuaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	boolean noScript = true;
	public void loadScript(String body) {
		
		if(body == null || body.equals("")) {
			Log.e("Window","NO SCRIPT SPECIFIED, SHUTTING DOWN LUA");
			noScript = true;
			if(L != null) {
				L.close();
				L = null;
			}
			return;
		} else {
			noScript = false;
		}
		if(L != null) {
			L.close();
			L = null;
		}
		this.L = LuaStateFactory.newLuaState();
		initLua();
		L.pushJavaObject(this);
		L.setGlobal("view");
		DrawFunction draw = new DrawFunction(L);
		try {
			draw.register("draw");
		} catch (LuaException e) {
			e.printStackTrace();
		}
		
		
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		L.LloadString(body);
		int ret = L.pcall(0, 1, -2);
		if(ret != 0) {
			Log.e("LUAWINDOW","Error Loading Script: "+L.getLuaObject(L.getTop()).getString());
		} else {
			Log.e("LUAWINDOW","Loaded script body for: " + mName);
		}
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal("OnCreate");
		if(L.isFunction(L.getTop())) {
			int tmp = L.pcall(0, 1, -3);
			if(tmp != 0) {
				Log.e("LUAWINDOW","Calling OnCreate: "+L.getLuaObject(-1).getString());
			} else {
				Log.e("LUAWINDOW","OnCreate Success!");
			}
		}
	}
	
	
	class DrawFunction extends JavaFunction {

		public DrawFunction(LuaState L) {
			super(L);
			
		}

		@Override
		public int execute() throws LuaException {
			//this takes no arguments and recieves none.
			Window.this.invalidate();
			return 0;
		}
		
	}
	
	private class InvalidateFunction extends JavaFunction {

		public InvalidateFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			Window.this.invalidate();
			return 0;
		}
		
	}
	
	private class DebugFunction extends JavaFunction {

		public DebugFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String foo = this.getParam(2).getString();
			Log.e("LUAWINDOW","DEBUG:"+foo);
			return 0;
		}
		
	}
	
	private class BoundsFunction extends JavaFunction {

		public BoundsFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			//this.L.pushJavaObject(mBounds);
			return 1;
		}
		
	}
	
	private class OptionsMenuFunction extends JavaFunction {

		public OptionsMenuFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String funcName = this.getParam(2).getString();
			String title = this.getParam(3).getString();
			
			
			
			
			Object o = null;
			LuaObject tmp = this.getParam(4);
			if(tmp != null && tmp.isJavaObject()) {
				o = tmp.getObject();
			}
			
			//Handler h = 
			Message msg = mainHandler.obtainMessage(MainWindow.MESSAGE_ADDOPTIONCALLBACK);
			if(o != null) msg.obj = o;
			Bundle b = msg.getData();
			b.putString("funcName", funcName);
			b.putString("title", title);
			b.putString("window", mName);
			msg.setData(b);
			mainHandler.sendMessage(msg);
			return 0;
		}
		
	}
	
	private class TableAdapterFunction extends JavaFunction {

		public TableAdapterFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String table = this.getParam(2).getString();
			String viewFunc = this.getParam(3).getString();
			TableAdapter tb = new TableAdapter(this.L,table,viewFunc);
			L.pushJavaObject(tb);
			return 1;
		}
		
	}
	
	private class SheduleCallbackFunction extends JavaFunction {

		public SheduleCallbackFunction(LuaState L) {
			super(L);

		}

		@Override
		public int execute() throws LuaException {
			int id = (int)this.getParam(2).getNumber();
			String callback = this.getParam(3).getString();
			long delay = Long.parseLong(this.getParam(4).getString());
			//callScheduleCallback(id,callback);
			Message msg = callbackHandler.obtainMessage(id,callback);
			callbackHandler.sendMessageDelayed(msg, delay);
			return 0;
		}
		
	}
	
	private class CancelSheduleCallbackFunction extends JavaFunction {

		public CancelSheduleCallbackFunction(LuaState L) {
			super(L);

		}

		@Override
		public int execute() throws LuaException {
			int id = (int)this.getParam(2).getNumber();
			//String callback = this.getParam(3).getString();
			//callScheduleCallback(id,callback);
			callbackHandler.removeMessages(id);
			return 0;
		}
		
	}
	
	private Handler callbackHandler = new Handler() {
		public void handleMessage(Message msg) {
			//
			//just call the string.
			Window.this.callScheduleCallback(msg.arg1,(String)msg.obj);
			
			
		}
	};
	
	private void callScheduleCallback(int id,String callback) {
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.getLuaObject(-1).isFunction()) {
			//prepare to call.
			L.pushString(Integer.toString(id));
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				Log.e("Window","Scheduled callback("+callback+") error:"+L.getLuaObject(-1).toString());
			}
		} else {
			//error no function.
		}
	}
	
	private class PluginXCallSFunction extends JavaFunction {
		//HashMap<String,String> 
		public PluginXCallSFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			//String token = this.getParam(2).getString();
			String function = this.getParam(2).getString();
			LuaObject foo = this.getParam(3);
			
			
			//--if(foo.isTable()) {
			//	Log.e("DEBUG","ARGUMENT IS TABLE");
			//}
			//HashMap<String,Object> dump = dumpTable("t",3);
			//
			/*L.pushNil();
			while(L.next(2) != 0) {
				
				String id = L.toString(-2);
				LuaObject l = L.getLuaObject(-1);
				if(l.isTable()) {
					//need to dump more tables
				} else {
					
				}
			}*/
			//mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_X, obj))
			Message msg = mainHandler.obtainMessage(MainWindow.MESSAGE_PLUGINXCALLS,foo.getString());
			
			msg.getData().putString("PLUGIN",mOwner);
			msg.getData().putString("FUNCTION", function);
			
			mainHandler.sendMessage(msg);
			// TODO Auto-generated method stub
			return 0;
		}
		
		public HashMap<String,Object> dumpTable(String tablePath,int idx) {
			
			HashMap<String,Object> tmp = new HashMap<String,Object>();
			int counter = 1;
			L.pushNil();
			while(L.next(idx) != 0) {
				//String id = L.toString(-2);
				String id = null;
				if(L.isNumber(-2)) {
					id = Integer.toString(counter);
					counter++;
				} else if(L.isString(-2)) {
					id = L.toString(-2);
				}
				LuaObject l = L.getLuaObject(-1);
				if(l.isTable()) {
					//need to dump more tables
					tmp.put(id, dumpTable(tablePath+"."+id,L.getTop()));
					//Log.e("PLUGIN","TABLE RECURSIVE DUMP:"+L.getTop()+":"+(L.getLuaObject(L.getTop()).toString()));
				
				} else {
					//Log.e("PLUGIN","WXCALLT:"+tablePath+"|"+id+"<==>"+l.getString());
					tmp.put(id, l.getString());
				}
				
				L.pop(1);
			}
			
			//L.pop(1);
			return tmp;
		}
		
		
	}
	


	public void callFunction(String callback) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.isFunction(L.getTop())) {
			int tmp = L.pcall(0, 1, -2);
			if(tmp != 0) {
				Log.e("LUAWINDOW","Error calling script callback: "+L.getLuaObject(-1).getString());
			}
		}
	}
	
	public void callFunction(String callback,Object o) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		
		if(L.isFunction(L.getTop())) {
			L.pushJavaObject(o);
			int tmp = L.pcall(1, 1, -3);
			if(tmp != 0) {
				Log.e("LUAWINDOW","Error calling script callback: "+L.getLuaObject(-1).getString());
			}
		}
	}



	public void setDimensions(int width, int height) {
		LayoutParams p = (LayoutParams) this.getLayoutParams();
		p.width = width;
		p.height = height;
		mWidth = width;
		mHeight = height;
		calculateCharacterFeatures(mWidth,mHeight);
		//View v = ((View)this.getParent());
		//RelativeLayout.LayoutParams p = (LayoutParams) v.getLayoutParams();
		//p.height = mHeight;
		//p.width = mWidth;
		
		//Log.e("WINDOW","WINDOW HEIGHT NOW:" + mHeight);
		//v.setLayoutParams(p);
		//v.requestLayout();
		//this.requestLayout();
	}
	
	public void setWidth(int width) {
		LayoutParams p = (LayoutParams) this.getLayoutParams();
		p.width = width;
		//p.height = height;
		this.mWidth = width;
		calculateCharacterFeatures(mWidth,mHeight);
	}
	
	public void setHeight(int height) {
		LayoutParams p = (LayoutParams) this.getLayoutParams();
		//p.width = width;
		p.height = height;
		calculateCharacterFeatures(mWidth,mHeight);
	}
	

	/*public void updateAnchor(int x, int y) {
		View v = ((View)this.getParent());
		//v.setPadding(x, y, 0, 0);
		LayoutParams p = (LayoutParams) v.getLayoutParams();
		p.setMargins(x, y, 0, 0);
		
		//v.requestLayout();
	}*/
	
	public RelativeLayout getParentView() {
		return (RelativeLayout)this.getParent();
	}

	public void onCustomAnimationEnd() {
		//call into lua to notify that the parent animation has completed.
		callFunction("onParentAnimationEnd");
	}
	
	@Override
	public void onAnimationEnd() {
		//call into lua to notify that the parent animation has completed.
		callFunction("onAnimationEnd");
	}
	
	/*public void addView(View v) {
		RelativeLayout tmp = this.getParentView();
		tmp.addView(v);
		//tmp.getLayoutParams().
		//RelativeLayout.LayoutParams p = (LayoutParams) tmp.getLayoutParams();
		//p.setma
	}*/
	
	public int getMaxHeight() {
		return mMaxHeight;
	}
	
	public int getMaxWidth() {
		return mMaxHeight;
	}



	@Override
	public void updateSetting(String key, String value) {
		//convert to enum value, then switch, handle accordingly.
		BaseOption o = (BaseOption) settings.findOptionByKey(key);
		o.setValue(value);
		try {
			KEYS tmp = KEYS.valueOf(key);
			switch(tmp) {
			case hyperlinks_enabled:
				this.setLinksEnabled((Boolean)o.getValue());
				break;
			case hyperlink_mode:
				this.setLinkMode((Integer)o.getValue());
				break;
				
			case hyperlink_color:
				this.setLinkColor((Integer)o.getValue());
				break;
				
			case word_wrap:
				this.setWordWrap((Boolean)o.getValue());
				break;
			
			case color_option:
				switch((Integer)o.getValue()) {
				case 0:
					this.setColorDebugMode(0);
					break;
				case 1:
					this.setColorDebugMode(3);
					break;
				case 2:
					this.setColorDebugMode(1);
					break;
				case 3:
					this.setColorDebugMode(2);
				}
				break;				
			case font_size:
				setCharacterSizes((Integer)o.getValue(),PREF_LINEEXTRA);
				break;
			case line_extra:
				setCharacterSizes(PREF_FONTSIZE,(Integer)o.getValue());
				break;
			case buffer_size:
				the_tree.setMaxLines((Integer)o.getValue());
				break;
			case font_path:
				PREF_FONT = loadFontFromName((String)o.getValue());
				p.setTypeface(PREF_FONT);
				this.invalidate();
				break;
				
			}
		} catch(IllegalArgumentException e) {
		}
	}
	
	private void setLinkMode(Integer value) {
		switch(value) {
		case 0:
			setLinkMode(HyperSettings.LINK_MODE.NONE);
			break;
		case 1:
			setLinkMode(HyperSettings.LINK_MODE.HIGHLIGHT);
			break;
		case 2:
			setLinkMode(HyperSettings.LINK_MODE.HIGHLIGHT_COLOR);
			break;
		case 3:
			setLinkMode(HyperSettings.LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND);
			break;
		case 4:
			setLinkMode(HyperSettings.LINK_MODE.BACKGROUND);
			break;
		}
	}

	private enum KEYS {
		hyperlinks_enabled,
		hyperlink_mode,
		hyperlink_color,
		word_wrap,
		color_option,
		screen_on,
		font_size,
		line_extra,
		buffer_size,
		font_path
	}
	
	/*@Override
	protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
		//Log.e("WINDOW","WINDOW ONLAYOUT CALLED: "+changed);
		if(changed) {
			sizeChanged = true;
		}
	}*/
	
	//boolean sizeChanged = false;
	/*@Override
	public RelativeLayout.LayoutParams getLayoutParams() {
		return this.getLayoutParams();
	}*/

	/*public int getMHeight() {
		// TODO Auto-generated method stub
		return mHeight;
	}*/
	
	/*public void startAnimation(Animation a) {
		View v = ((View)this.getParent());
		v.startAnimation(a);
	}*/
	private Typeface loadFontFromName(String name) {
		Typeface font = Typeface.MONOSPACE;
		//Log.e("WINDOW","FONT SELECTION IS:" + tmpname);
		if(name.contains("/")) {
			//string is a path
			if(name.contains(Environment.getExternalStorageDirectory().getPath())) {
				
				String sdstate = Environment.getExternalStorageState();
				if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
					font = Typeface.createFromFile(name);
				} else {
					font = Typeface.MONOSPACE;
				}
				
			} else {
				//path is a system path
				font = Typeface.createFromFile(name);
			}
			
		} else {
			if(name.equals("monospace")) {
				font = Typeface.MONOSPACE;
			} else if(name.equals("sans serif")) {
				font = Typeface.SANS_SERIF;
			} else if (name.equals("default")) {
				font = Typeface.DEFAULT;
			}
		}
		return font;
	}
	
	private View.OnTouchListener textSelectionTouchHandler = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//calculate out the position
			
			float x = event.getX();
			float y = event.getY();
			
			//convert y to be at the bottom of the screen.
			y = (float)v.getHeight() - y;
			
			y += (scrollback-SCROLL_MIN);
			
			float xform_to_line = y / (float)PREF_LINESIZE;
			int line = (int)Math.floor(xform_to_line);
			
			float xform_to_column = x / (float)one_char_is_this_wide;
			int column = (int)Math.floor(xform_to_column);
			
			switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//if(firstPress) {

					
				//} else {
					if(Math.abs(theSelection.start.line - line) < 2 && Math.abs(theSelection.start.column - column) < 2) {
						selectedSelector = theSelection.start;
						selectionFingerDown = true;
						moveWidgetToSelector(selectedSelector);
						Log.e("window","moving start selector");
						v.invalidate();
					} else if(Math.abs(theSelection.end.line - line) < 2 && Math.abs(theSelection.end.column - column) < 2) {
						selectedSelector = theSelection.end;
						moveWidgetToSelector(selectedSelector);
						selectionFingerDown = true;
						Log.e("window","moving end selector");
						v.invalidate();
					} else {
						int modx = (int) x - (widgetX - selectionIndicatorHalfDimension);
						int mody = (int) event.getY() - (widgetY - selectionIndicatorHalfDimension);
						if(selectionIndicatorRect.contains(modx,mody)) {
							
							//int newx = (int) (x - selectionIndicatorRect.left);
							//int newy = (int) (mody - selectionIndicatorRect.top);
							
							int full = selectionIndicatorHalfDimension * 2;
							int third = full / 3;
							
							int col = modx / third;
							
							int row = mody / third;
							
							switch(row) {
							case 0:
								switch(col) {
								case 0:
									//upper left
									selectionButtonDown = SelectionWidgetButtons.NEXT;
									break;
								case 1:
									//upper middle
									selectionButtonDown = SelectionWidgetButtons.UP;
									int remainder = ((int)(scrollback-SCROLL_MIN) % PREF_LINESIZE)-PREF_LINESIZE;
									//selectorCenterY -= PREF_LINESIZE;
									//if(selectorCenterY - (2*PREF_LINESIZE) < remainder) {
										//selectorCenterY = selectorCenterY + PREF_LINESIZE;
										//scrollback += PREF_LINESIZE;
										mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLUP,700);
									//}
									break;
								case 2:
									//upper right
									selectionButtonDown = SelectionWidgetButtons.COPY;
									break;
								}
								break;
							case 1:
								switch(col) {
								case 0:
									//middle left
									selectionButtonDown = SelectionWidgetButtons.LEFT;
									mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLLEFT,700);
									break;
								case 1:
									//center
									selectionButtonDown = SelectionWidgetButtons.CENTER;
									widgetCenterMovedX = 0;
									widgetCenterMovedY = 0;
									widgetCenterMoveXLast = (int) x;
									widgetCenterMoveYLast = (int) event.getY();
									break;
								case 2:
									selectionButtonDown = SelectionWidgetButtons.RIGHT;
									mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLRIGHT, 700);
									//middle right
									break;
								}
								break;
							case 2:
								switch(col) {
								case 0:
									//bottom left
									selectionButtonDown = SelectionWidgetButtons.EXIT;
									break;
								case 1:
									//bottom middle
									selectionButtonDown = SelectionWidgetButtons.DOWN;
									//int remainder = ((int)(scrollback-SCROLL_MIN) % PREF_LINESIZE) + PREF_LINESIZE;
									//selectorCenterY += PREF_LINESIZE;
									//if(selectorCenterY + PREF_LINESIZE > v.getHeight() - remainder) {
										//send the message to start scrolling.
										mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLDOWN,700);
									//}
									//calculateWidgetPosition(selectorCenterX,selectorCenterY);
									break;
								case 2:
									//bottom right
									break;
								}
								break;
							}
						}
						
						
					//}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(selectionButtonDown != null && selectionButtonDown == SelectionWidgetButtons.CENTER) {
					widgetCenterMovedX += (x - widgetCenterMoveXLast);
					widgetCenterMovedY -= (event.getY() - widgetCenterMoveYLast);
					widgetCenterMoveXLast = (int) x;
					widgetCenterMoveYLast = (int) event.getY();
					if(Math.abs(widgetCenterMovedX) > one_selection_char_is_this_wide) {
						int sign = (int)Math.signum(widgetCenterMovedX);
						if(sign > 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollRight(false);
						} else if(sign < 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollLeft(false);
						}
//						selectedSelector.column += 1 * Math.signum(widgetCenterMovedX);
//						selectorCenterX += one_char_is_this_wide * Math.signum(widgetCenterMovedX);
//						calculateWidgetPosition(selectorCenterX,selectorCenterY);
						widgetCenterMovedX = 0;
						v.invalidate();
					}
					if(Math.abs(widgetCenterMovedY) > SELECTIONINDICATOR_FONTSIZE) {
						int sign = (int)Math.signum(widgetCenterMovedY);
						if(sign > 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollUp(false);
						} else if(sign < 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollDown(false);
						}
//						selectedSelector.line += 1 * Math.signum(widgetCenterMovedY);
//						selectorCenterY += PREF_LINESIZE * -Math.signum(widgetCenterMovedY);
//						calculateWidgetPosition(selectorCenterX,selectorCenterY);
						widgetCenterMovedY = 0;
						v.invalidate();
					}
					return true;
				}
				if(selectedSelector != null && selectionFingerDown == true) {
					selectedSelector.column = column;
					selectedSelector.line = line;
					Log.e("window","moving selector-> line:"+line+" col:"+column);
					
					widgetCenterMovedX += (x - widgetCenterMoveXLast);
					widgetCenterMovedY -= (event.getY() - widgetCenterMoveYLast);
					widgetCenterMoveXLast = (int) x;
					widgetCenterMoveYLast = (int) event.getY();
					if(Math.abs(widgetCenterMovedX) > one_char_is_this_wide) {
						int sign = (int)Math.signum(widgetCenterMovedX);
						if(sign > 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollRight(false);
						} else if(sign < 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollLeft(false);
						}
//						selectedSelector.column += 1 * Math.signum(widgetCenterMovedX);
//						selectorCenterX += one_char_is_this_wide * Math.signum(widgetCenterMovedX);
//						calculateWidgetPosition(selectorCenterX,selectorCenterY);
						widgetCenterMovedX = 0;
						//mHandler.removeMessages(MESSAGE_SCROLLDOWN);
						//mHandler.removeMessages(MESSAGE_SCROLLUP);
						v.invalidate();
					} else {
//						mHandler.removeMessages(MESSAGE_SCROLLDOWN);
//						mHandler.removeMessages(MESSAGE_SCROLLUP);
//						mHandler.removeMessages(MESSAGE_SCROLLLEFT);
//						mHandler.removeMessages(MESSAGE_SCROLLRIGHT);
					}
					
					if(Math.abs(widgetCenterMovedY) > SELECTIONINDICATOR_FONTSIZE) {
						int sign = (int) Math.signum(widgetCenterMovedY);
						if(sign > 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollUp(false);
						} else if(sign < 0) {
							scrollRepeatRate = scrollRepeatRateInitial;
							scrollRepeatRateStep = 1;
							doScrollDown(false);
						}

						/*selectedSelector.line += 1 * Math.signum(widgetCenterMovedY);
						selectorCenterY += PREF_LINESIZE * -Math.signum(widgetCenterMovedY);
						calculateWidgetPosition(selectorCenterX,selectorCenterY);
						*/
						widgetCenterMovedY = 0;
						//mHandler.removeMessages(MESSAGE_SCROLLDOWN);
						//mHandler.removeMessages(MESSAGE_SCROLLUP);
						v.invalidate();
					}

					
					v.invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				selectionFingerDown = false;
				if(theSelection != null) {
					mHandler.removeMessages(MESSAGE_SCROLLDOWN);
					mHandler.removeMessages(MESSAGE_SCROLLUP);
					mHandler.removeMessages(MESSAGE_SCROLLLEFT);
					mHandler.removeMessages(MESSAGE_SCROLLRIGHT);
					scrollRepeatRate = scrollRepeatRateInitial;
					scrollRepeatRateStep = 1;
					/*if(selectedSelector != null) {
						selectedSelector = null;
						v.invalidate();
						return true;
					}*/
//					boolean doselection = false;
//					
//					int startline,startcol,endline,endcol;
//					if(theSelection.end.line > theSelection.start.line) {
//						
//						startline = theSelection.end.line;
//						startcol = theSelection.end.column;
//						endline = theSelection.start.line;
//						endcol = theSelection.start.column;
//					} else {
//						startline = theSelection.start.line;
//						startcol = theSelection.start.column;
//						endline = theSelection.end.line;
//						endcol = theSelection.end.column;
//					}
//					
//					if(startline == endline) {
//						if(startcol < column && endcol > column && startline == line) {
//							doselection = true;
//						}
//					} else {
//						if(startline > line && endline < line) {
//							doselection = true;
//						} else if(startline == line) {
//							if(startcol < column) {
//								doselection = true;
//							}
//						} else if(endline == line) {
//							if(endcol > column) {
//								doselection = true;
//							}
//						}
//					}
//					
//					if(doselection) {
//						String text = the_tree.getTextSection(theSelection);
//						Log.e("window","copied text:\n" + text);
//						v.invalidate();
//					}
					
					int mod2x = (int) x - (widgetX - selectionIndicatorHalfDimension);
					int mod2y = (int) event.getY() - (widgetY - selectionIndicatorHalfDimension);
					if(selectionIndicatorRect.contains(mod2x,mod2y)) {
						
						//int newx = (int) (x - (widgetX - selectionIndic);
						//int newy = (int) (event.getY() - selectionIndicatorRect.top);
						
						int full = selectionIndicatorHalfDimension * 2;
						int third = full / 3;
						
						int col = mod2x / third;
						
						int row = mod2y / third;
						
						SelectionWidgetButtons tmp = null;
						
						switch(row) {
						case 0:
							switch(col) {
							case 0:
								//upper left
								tmp = SelectionWidgetButtons.NEXT;
								break;
							case 1:
								//upper middle
								tmp = SelectionWidgetButtons.UP;
								break;
							case 2:
								//upper right
								tmp = SelectionWidgetButtons.COPY;
								break;
							}
							break;
						case 1:
							switch(col) {
							case 0:
								//middle left
								tmp = SelectionWidgetButtons.LEFT;
								break;
							case 1:
								//center
								tmp = SelectionWidgetButtons.CENTER;
								break;
							case 2:
								tmp = SelectionWidgetButtons.RIGHT;
								//middle right
								break;
							}
							break;
						case 2:
							switch(col) {
							case 0:
								//bottom left
								tmp = SelectionWidgetButtons.EXIT;
								break;
							case 1:
								//bottom middle
								tmp = SelectionWidgetButtons.DOWN;
								break;
							case 2:
								//bottom right
								break;
							}
							break;
						}
						
						if(selectionButtonDown != null && tmp == selectionButtonDown) {
							switch(tmp) {
							case UP:
								Log.e("widget","widget up pressed");
								doScrollUp(false);
//								selectedSelector.line += 1;
//								if(selectedSelector.line == the_tree.getBrokenLineCount()) {
//									selectedSelector.line -= 1;
//									return true;
//								} else {
//									int remainder = ((int)(scrollback-SCROLL_MIN) % PREF_LINESIZE)-PREF_LINESIZE;
//									selectorCenterY -= PREF_LINESIZE;
//									if(selectorCenterY - (PREF_LINESIZE) < remainder) {
//										selectorCenterY = selectorCenterY + PREF_LINESIZE;
//										scrollback += PREF_LINESIZE;
//									}
//									calculateWidgetPosition(selectorCenterX,selectorCenterY);
//								}
								break;
							case DOWN:
								Log.e("widget","widget down pressed");
								doScrollDown(false);
//								selectedSelector.line -= 1;
//								if(selectedSelector.line < 0) {
//									selectedSelector.line = 0;
//									return true;
//								} else {
//									int remainder = ((int)(scrollback-SCROLL_MIN) % PREF_LINESIZE) + PREF_LINESIZE;
//									selectorCenterY += PREF_LINESIZE;
//									if(selectorCenterY > v.getHeight() - remainder) {
//										selectorCenterY -= PREF_LINESIZE;
//										scrollback -= PREF_LINESIZE;
//									}
//									calculateWidgetPosition(selectorCenterX,selectorCenterY);
//								}
								break;
							case NEXT:
								Log.e("widget","widget next pressed");
								//copy the bitches and resume normal operation.
								String copy = the_tree.getTextSection(theSelection);
								Log.e("copied text","text copied:\n"+copy);
								ClipboardManager cpMan = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
								cpMan.setText(copy);
								v.setOnTouchListener(null);
								theSelection.start = null;
								theSelection.end = null;
								theSelection = null;
								selectedSelector = null;
								Window.this.flushBuffer();
								Window.this.setBufferText(false);
								v.invalidate();
								return true;
								//break;
							case LEFT:
								Log.e("widget","widget left pressed");
								doScrollLeft(false);
								break;
							case RIGHT:
								Log.e("widget","widget right pressed");
								doScrollRight(false);
								break;
							case CENTER:
								Log.e("widget","widget center pressed");
								break;
							case COPY:
								//actually switch.
								if(selectedSelector == theSelection.end) {
									selectedSelector = theSelection.start;
								} else {
									selectedSelector = theSelection.end;
								}
								//calculateWidgetPosition(selectedSelector.line,selectedSelector.column);
								moveWidgetToSelector(selectedSelector);
								break;
							case EXIT:
								//get out and don't copy.
								v.setOnTouchListener(null);
								theSelection.start = null;
								theSelection.end = null;
								theSelection = null;
								selectedSelector = null;
								Window.this.flushBuffer();
								Window.this.setBufferText(false);
								v.invalidate();
								return true;
								//break;
							}

							v.invalidate();
						}
					}
					
				}
				selectionButtonDown = null;
				break;
			}
			

			//String word = the_tree.getWordAt(line,column);
			//the_tree.
			//Log.e("texttree","word at line:"+line+" column:"+column+" is: " + word);
			
			
			//break;
			//}
			return true;
		}
	};
	
	private int widgetCenterMovedX = 0;
	private int widgetCenterMovedY = 0;
	private int widgetCenterMoveXLast = 0;
	private int widgetCenterMoveYLast = 0;
	
	private int widgetX = 0;
	private int widgetY = 0;
	
	private SelectionCursor selectedSelector;
	private Selection theSelection;
	boolean firstPress = true;
	
	private enum SelectionWidgetButtons {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		CENTER,
		EXIT,
		COPY,
		NEXT,
	}
	
	private SelectionWidgetButtons selectionButtonDown = null;
	
	private void calculateWidgetPosition(int startX,int startY) {
		//test to see if we can place it here.
		//add the vector to the components of the widget's center
		int newWidgetX = (int) (startX + selectionIndicatorVectorX);
		int newWidgetY = (int) ((int) (startY + selectionIndicatorVectorY));
		
		if((newWidgetX + (selectionIndicatorHalfDimension)) > this.getWidth()) {
			selectionIndicatorVectorX -= one_char_is_this_wide;
			if(selectionIndicatorVectorX < (one_char_is_this_wide + selectionIndicatorHalfDimension)) {
				selectionIndicatorVectorX = -(selectionIndicatorVectorX+one_char_is_this_wide);
			}
			newWidgetX = (int) (startX + selectionIndicatorVectorX);
			
		} else if((newWidgetX - selectionIndicatorHalfDimension) < 0) {
			//flip the vector
			selectionIndicatorVectorX += one_char_is_this_wide;
			if(selectionIndicatorVectorX > -(one_char_is_this_wide+selectionIndicatorHalfDimension)) {
				selectionIndicatorVectorX = -(selectionIndicatorVectorX-one_char_is_this_wide);
			}
			newWidgetX = (int) (startX + selectionIndicatorVectorX);
		}
		
		if((newWidgetY + (selectionIndicatorHalfDimension)) > this.getHeight()) {
			selectionIndicatorVectorY -= PREF_LINESIZE;
			//only if we run into 
			newWidgetY = (int) (startY + selectionIndicatorVectorY);
			if(newWidgetY + selectionIndicatorHalfDimension > this.getHeight()) {
				selectionIndicatorVectorY = -selectionIndicatorHalfDimension;
				newWidgetY = (int) (startY + selectionIndicatorVectorY);
			}
			
		} else if ((newWidgetY - selectionIndicatorHalfDimension) < 0) {
			selectionIndicatorVectorY += PREF_LINESIZE;
			
			newWidgetY = (int) (startY + selectionIndicatorVectorY);
			if(newWidgetY - selectionIndicatorHalfDimension < 0) {
				selectionIndicatorVectorY = +selectionIndicatorHalfDimension;
				newWidgetY = (int) (startY + selectionIndicatorVectorY);
			}
		}
		
		widgetX = newWidgetX;
		widgetY = newWidgetY;
		
	}
	
	private void moveWidgetToSelector(TextTree.SelectionCursor cursor) {
		
		int part1 = (int) (selectedSelector.line * PREF_LINESIZE + (0.5*SELECTIONINDICATOR_FONTSIZE));
		//int part2 = (int) (scrollback);
		int part2 = (int) (selectedSelector.line * PREF_LINESIZE - (0.5*SELECTIONINDICATOR_FONTSIZE));
		
		
		if(part1 > scrollback) {
			//calculate how much scroll to go to get to be true.
			//int tmp = (int) (scrollback-SCROLL_MIN);
			//int howmuch = part2 - part1;
			scrollback = (double) part1;
		} else if(part2 < (scrollback-SCROLL_MIN)) {
			scrollback -= ((scrollback-SCROLL_MIN) - part2);
		}
		
		int endx = (int) ((selectedSelector.column * one_char_is_this_wide) + (0.5*one_char_is_this_wide));
		int endy = (int) ((this.getHeight() - ((selectedSelector.line * PREF_LINESIZE) + (0.5*SELECTIONINDICATOR_FONTSIZE) - (scrollback-SCROLL_MIN))));
		//widgetX = endx;
		//widgetY = endy;
		selectorCenterX = endx;
		selectorCenterY = endy;
		calculateWidgetPosition(selectorCenterX,selectorCenterY);
	}
	
	private int selectorCenterX = 0;
	private int selectorCenterY = 0;
	
	private float selectionIndicatorVectorX = 160;
	private float selectionIndicatorVectorY = 0;
	
	private boolean selectionFingerDown = false;

	public void shutdown() {
		if(L == null) return;
		//call into lua to notify shutdown imminent.
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("OnDestroy");
		if(L.getLuaObject(L.getTop()).isFunction()) {
			int ret = L.pcall(0, 1, -2);
			if(ret != 0) {
				Log.e("LUA","Error in OnDestroy: "+L.getLuaObject(-1).getString());
			}
		} else {
			//no method.
		}
		
		L.close();
		L = null;
		
	}
	
	private void doScrollDown(boolean repeat) {
		Log.e("FOO","do scroll down");
		selectedSelector.line -= 1;
		if(selectedSelector.line < 0) {
			selectedSelector.line = 0;
			repeat = false;
		} else {
			int remainder = ((int)(scrollback-SCROLL_MIN) % PREF_LINESIZE) + PREF_LINESIZE;
			selectorCenterY += PREF_LINESIZE;
			if(selectorCenterY > this.getHeight() - remainder) {
				selectorCenterY -= PREF_LINESIZE;
				scrollback -= PREF_LINESIZE;
			}
			calculateWidgetPosition(selectorCenterX,selectorCenterY);
		}
	
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLDOWN,scrollRepeatRate);
		} else {
			mHandler.removeMessages(MESSAGE_SCROLLDOWN);
		}
	}
	
	private void doScrollUp(boolean repeat) {
		Log.e("FOO","do scroll up");
		selectedSelector.line += 1;
		if(selectedSelector.line == the_tree.getBrokenLineCount()) {
			selectedSelector.line -= 1;
			repeat = false;
		} else {
			int remainder = ((int)(scrollback-SCROLL_MIN) % PREF_LINESIZE)-PREF_LINESIZE;
			selectorCenterY -= PREF_LINESIZE;
			if(selectorCenterY - (PREF_LINESIZE) < remainder) {
				selectorCenterY = selectorCenterY + PREF_LINESIZE;
				scrollback += PREF_LINESIZE;
			}
			calculateWidgetPosition(selectorCenterX,selectorCenterY);
		}
//		selectedSelector.line += 1;
//		if(selectedSelector.line >= the_tree.getBrokenLineCount()) {
//			selectedSelector.line -= 1;
//			repeat = false;
//		} else {
//			scrollback += PREF_LINESIZE;
//		}
//		calculateWidgetPosition(selectorCenterX,selectorCenterY);
		//calculateWidgetPosition()
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLUP,scrollRepeatRate);
		} else {
			mHandler.removeMessages(MESSAGE_SCROLLUP);
		}
	}
	
	private void doScrollLeft(boolean repeat) {
		selectedSelector.column -= 1;
		if(selectedSelector.column < 0) {
			selectedSelector.column = 0;
		} else {
			selectorCenterX -= one_char_is_this_wide;
			widgetY -= one_char_is_this_wide;
			calculateWidgetPosition(selectorCenterX,selectorCenterY);
		}
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLLEFT,scrollRepeatRate);
		}else {
			mHandler.removeMessages(MESSAGE_SCROLLLEFT);
		}
	}
	
	private void doScrollRight(boolean repeat) {
		selectedSelector.column += 1;
		selectorCenterX += one_char_is_this_wide;
		calculateWidgetPosition(selectorCenterX,selectorCenterY);
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLRIGHT, scrollRepeatRate);
		}else {
			mHandler.removeMessages(MESSAGE_SCROLLRIGHT);
		}
	}
	
	public boolean isTextSelectionEnabled() {
		return textSelectionEnabled;
	}



	public void setTextSelectionEnabled(boolean textSelectionEnabled) {
		this.textSelectionEnabled = textSelectionEnabled;
		Log.e("sfdsf","setting text selection enabled="+textSelectionEnabled);
	}

	private int scrollRepeatRateStep = 1;
	private int scrollRepeatRateInitial = 300;
	private int scrollRepeatRate = scrollRepeatRateInitial;
	private int scrollRepeatRateMin = 60;
	
	
}

