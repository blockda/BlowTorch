/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.window;


import java.io.File;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.offsetnull.bt.service.IWindowCallback;
import com.offsetnull.bt.service.plugin.settings.BaseOption;
import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.ColorOption;
import com.offsetnull.bt.service.plugin.settings.FileOption;
import com.offsetnull.bt.service.plugin.settings.IntegerOption;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.text.ClipboardManager;
import android.util.AttributeSet;

import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.offsetnull.bt.service.Colorizer;
import com.offsetnull.bt.service.SettingsChangedListener;
import com.offsetnull.bt.service.WindowToken;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.settings.HyperSettings.LINK_MODE;
import com.offsetnull.bt.window.TextTree.Line;
import com.offsetnull.bt.window.TextTree.Selection;
import com.offsetnull.bt.window.TextTree.SelectionCursor;
import com.offsetnull.bt.window.TextTree.Unit;


/** \brief Window
 *
 *  The Window.java class is the programmable mini-window that also houses the ansi drawing routine.
 */

@SuppressWarnings("deprecation")
public class Window extends View implements AnimatedRelativeLayout.OnAnimationEndListener, SettingsChangedListener {
	/** Add text to the main output window. */
	private static final int MESSAGE_ADDTEXT = 0;
	/** Redraw the screen. */
	private static final int MESSAGE_DRAW = 117;
	/** Immediately send the contents of the input bar to the server. */
	private static final int MESSAGE_FLUSHBUFFER = 118;
	/** Shutdown all windows and exit (?). */
	private static final int MESSAGE_SHUTDOWN = 119;
	/** Cross thread bridge message for the WindowXCallS function. */
	private static final int MESSAGE_PROCESSXCALLS = 4;
	/** Clear all the text in the input bar. */
	private static final int MESSAGE_CLEARTEXT = 5;
	/** Indicate that the settings have changed and should be reloaded. */
	private static final int MESSAGE_SETTINGSCHANGED = 6;
	/** Indicates that the system encoding has changed. */
	private static final int MESSAGE_ENCODINGCHANGED = 7;
	/** Sent from the onTouchEvent handler to indicate that selection should begin. */
	private static final int MESSAGE_STARTSELECTION = 8;
	/** Sent from the selection widget, scroll down. */
	private static final int MESSAGE_SCROLLDOWN = 9;
	/** Sent from the selection widget, scroll up. */
	private static final int MESSAGE_SCROLLUP = 10;
	/** Sent from the selection widget, scroll right. */
	private static final int MESSAGE_SCROLLRIGHT = 11;
	/** Sent from the selection widget, scroll left. */
	private static final int MESSAGE_SCROLLLEFT = 12;
	/** Cross thread bridge message for the WindowXCallB lua function. */
	private static final int MESSAGE_XCALLB = 13;
	/** Message used from lua I think to reset the window, and add text to it. */
	private static final int MESSAGE_RESETWITHDATA = 14;
	
	/** The activity that owns this window. */
	private MainWindowCallback mParent = null;
	/** The bitmap that holds the "return to the bottom of the buffer" button graphic. */
	private Bitmap mHomeWidgetDrawable = null;
	/** The bitmap that holds the selection widget cancel button. */
	private Bitmap mTextSelectionCancelBitmap = null;
	/** The bitmap that holds the selection widget copy button. */
	private Bitmap mTextSelectionCopyBitmap = null;
	/** The bitmap that holds the selection widget cursor swap button. */
	private Bitmap mTextSelectionSwapBitmap = null;
	/** Rectangle that represents the hot-zone (clickable region) for the home button. */
	private Rect mHomeWidgetRect = new Rect();
	/** The buffer object that this window uses to store and draw ansi text. */
	private TextTree mBuffer = null;
	/** The buffer that is used to buffer text when BufferText() is set. */
	private TextTree mHoldBuffer = null;
	/** The maximum height for this window. I don't think this is used. */
	private int mMaxHeight;
	/** The preference for fontsize. */
	private int mPrefFontSize = WindowToken.DEFAULT_FONT_SIZE;
	/** The height of this window. */
	private int mHeight = 1;
	/** The width of this window. */
	private int mWidth = 1;
	/** The measured width of one character using the preference font size. */
	private int mOneCharWidth = 1;
	/** The display density of the device's display panel. */
	private float mDensity;
	/** The LuaState associated with this window. */
	private LuaState mL = null;
	/** The string name of the plugin that launched this window. */
	private String mOwner;
	/** Variable to store the calculated number of lines that can be drawn at the current font size. */
	private int mCalculatedLinesInWindow;
	/** The preference value for the extra line space to add to each line. */
	private int mPrefLineExtra = 2;
	/** The preference value for the total linesize, the sum of the font + extra. */
	private int mPrefLineSize = (int) mPrefFontSize + mPrefLineExtra;
	/** The preferred font to use to draw text. */
	private Typeface mPrefFont = Typeface.MONOSPACE;
	/** The calclualated number of rows in the window for the preferred line size. */
	private int mCalculatedRowsInWindow;
	/** Tracker value for weather or not text selection should be available for this window. */
	private boolean mTextSelectionEnabled = true;
	/** The current fling velocity. */
	private double mFlingVelocity;
	/** The number of chars to fit to the width of the window, -1 to disable. */
	private int mFitChars = -1;
	/** Tracker value for weather or not to buffer incoming text (used while text selecting). */
	private boolean mBufferText = false;
	/** Tracker value for weather or not to center justify text being drawn. */
	private boolean mCenterJustify = false;
	/** Tracker value for weather or not the window has a script OnMeasure function implemented. */
	private boolean mHasScriptOnMeasure = false;
	/** Tracker value for what the current color debug mode is. */
	private int mColorDebugMode = 0;
	/** Tracker value for the current link mode. */
	private LINK_MODE mLinkMode = LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND;
	/** Tracker value for the current link decoration color. */
	private int mLinkHighlightColor = HyperSettings.DEFAULT_HYPERLINK_COLOR;
	/** ANSI Drawing routine current color register. */
	private Integer mSelectedColor = Integer.valueOf(37);
	/** ANSI Drawing routine current brightness register. */
	private Integer mSelectedBright = Integer.valueOf(0);
	/** ANSI Drawing routine current background color. */
	private Integer mSelectedBackground = Integer.valueOf(60);
	/** Utility variable that is used by the ANSI drawing routine to properly handle xterm 256 colors. */
	private boolean mXterm256FGStart = false;
	/** Utility variable that is used by the ANSI drawing routine to properly handle xterm 256 colors. */
	private boolean mXterm256BGStart = false;
	/** Utility variable that is used by the ANSI drawing routine to properly handle xterm 256 colors. */
	private boolean mXterm256Color = false;
	/** The handler message queue for this window. */
	private Handler mHandler = null;
	/** The handler message queue for the main window that holds this window. */
	private Handler mMainWindowHandler = null;
	/** Paint object associated with drawing text inside of the magnifier widget. */
	private Paint mTextSelectionIndicatorPaint = new Paint();
	/** Paint object associated with drawing the background highlight color of the magnifier widget. */
	private Paint mTextSelectionIndicatorBackgroundPaint = new Paint();
	/** The paint object associated with drawing the circle around the magnifier widget. */
	private Paint mTextSelectionIndicatorCirclePaint = new Paint();
	
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
	int selectionIndicatorHalfDimension = 60;
	
	Path selectionIndicatorClipPath = new Path();
	
	Rect selectionIndicatorLeftButtonRect = new Rect();
	Rect selectionIndicatorRightButtonRect = new Rect();
	Rect selectionIndicatorUpButtonRect = new Rect();
	Rect selectionIndicatorDownButtonRect = new Rect();
	Rect selectionIndicatorCenterButtonRect = new Rect();
	
	Rect selectionIndicatorRect = new Rect();
	
	private int scrollRepeatRateStep = 1;
	private int scrollRepeatRateInitial = 300;
	private int scrollRepeatRate = scrollRepeatRateInitial;
	private int scrollRepeatRateMin = 60;
	
	public int gravity = Gravity.LEFT;
	
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
	long target = 0;
	boolean homeWidgetFingerDown = false;
	int touchStartY;
	int pointer = -1;
	float fling_accel = 200.0f; //(units per sec);
	
	private ArrayList<LinkBox> linkBoxes = new ArrayList<LinkBox>();
	
	private Paint scroller_paint = new Paint();
	private boolean homeWidgetShowing = false;
	Rect scrollerRect = new Rect();
	Paint featurePaint = new Paint();

	boolean indicated = false;
	
	public Window(String dataDir,Context context,String name,String owner,Handler mainWindowHandler,SettingsGroup settings,MainWindowCallback activity) {
		super(context);
		this.mParent = activity;
		init(dataDir,name,owner,mainWindowHandler,settings);
	}
	
	public Window(Context c) {
		super(c);
	}
	
	public Window(Context c, AttributeSet a) {
		super(c,a);
	}
	
	public void displayLuaError(String message) {
		mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_DISPLAYLUAERROR,"\n" + Colorizer.getRedColor() + message + Colorizer.getWhiteColor() + "\n"));
	}
	
	public void onCreate(Bundle b) {
		fitFontSize(-1);
	}
	
	protected void onAttachedToWindow() {
		viewCreate();
	}
	
	protected void onDetachedFromWindow() {
		viewDestroy();
	}
	
	protected void onMeasure(int widthSpec,int heightSpec) {
		int height = MeasureSpec.getSize(heightSpec);
		int width = MeasureSpec.getSize(widthSpec);
		
		if(mHasScriptOnMeasure && mL != null) {
			mL.getGlobal("debug");
			mL.getField(-1, "traceback");
			mL.remove(-2);
			
			mL.getGlobal("OnMeasure");
			if(mL.isFunction(-1)) {
				mL.pushNumber(widthSpec);
				mL.pushNumber(heightSpec);
				int ret = mL.pcall(2,2,-4);
				if(ret !=0) {
					displayLuaError("Error in OnMeasure:" + mL.getLuaObject(-1).getString());
					setMeasuredDimension(1,1);
					
					mL.pop(1);
					return;
				} else {
					//get the return values.
					int ret_height = (int) mL.getLuaObject(-1).getNumber();
					int ret_width = (int) mL.getLuaObject(-2).getNumber();
					mL.pop(2);
					setMeasuredDimension(ret_width,ret_height);
					return;
				}
			} else {
				mL.pop(2);
			}
		}
		int hspec = MeasureSpec.getMode(heightSpec);
		if(width != mWidth) {
			doFitFontSize(width);
		}
		switch(hspec) {
		case MeasureSpec.AT_MOST:
			break;
		case MeasureSpec.EXACTLY:
			break;
		case MeasureSpec.UNSPECIFIED:
			height = (mBuffer.getBrokenLineCount()*mPrefLineSize) + mPrefLineExtra;
			break;
		}
		
		setMeasuredDimension(width,height);
		
	}
	
	private String dataDir = null;
	
	private void init(String dataDir,String name,String owner,Handler mainWindowHandler,SettingsGroup settings) {
		this.dataDir = dataDir;
		this.mDensity = this.getContext().getResources().getDisplayMetrics().density;
		if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			selectionIndicatorHalfDimension = (int) (90*mDensity);
		} else {
			selectionIndicatorHalfDimension = (int) (60*mDensity);
		}
		
		selectionIndicatorClipPath.addCircle(selectionIndicatorHalfDimension,selectionIndicatorHalfDimension,selectionIndicatorHalfDimension-10,Path.Direction.CCW);
		mHomeWidgetDrawable = BitmapFactory.decodeResource(this.getContext().getResources(),com.offsetnull.bt.R.drawable.homewidget);
		mTextSelectionCancelBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.cancel_tiny);
		mTextSelectionCopyBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.copy_tiny);
		mTextSelectionSwapBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.swap);
		
		mTextSelectionIndicatorPaint.setStyle(Paint.Style.STROKE);
		mTextSelectionIndicatorPaint.setStrokeWidth(1*mDensity);
		mTextSelectionIndicatorPaint.setColor(0xFFFF0000);
		mTextSelectionIndicatorPaint.setAntiAlias(true);
		
		mTextSelectionIndicatorBackgroundPaint.setStyle(Paint.Style.FILL);
		mTextSelectionIndicatorBackgroundPaint.setColor(0x770000FF);
		
		mTextSelectionIndicatorCirclePaint.setStyle(Paint.Style.STROKE);
		mTextSelectionIndicatorCirclePaint.setStrokeWidth(2);
		mTextSelectionIndicatorCirclePaint.setColor(0xFFFFFFFF);
		DashPathEffect dpe = new DashPathEffect(new float[]{3,3}, 0);
		
		mTextSelectionIndicatorCirclePaint.setPathEffect(dpe);
		mTextSelectionIndicatorCirclePaint.setAntiAlias(true);
		this.settings = settings;
		this.settings.setListener(this);
		mBuffer = new TextTree();
		if(name.equals("mainDisplay")) {
			mBuffer.debugLineAdd = true;
		}
		mHoldBuffer = new TextTree();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_RESETWITHDATA:
					Window.this.resetAndAddText((byte[])msg.obj);
					break;
				case MESSAGE_SCROLLLEFT:
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollLeft(true);
					break;
				case MESSAGE_SCROLLRIGHT:
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollRight(true);
					break;
				case MESSAGE_SCROLLDOWN:
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollDown(true);
					break;
				case MESSAGE_SCROLLUP:
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
					break;
				case MESSAGE_CLEARTEXT:
					mBuffer.empty();
					mHoldBuffer.empty();
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
					Window.this.addBytes((byte[])msg.obj, false);
					break;
				case MESSAGE_PROCESSXCALLS:
					Window.this.xcallS(msg.getData().getString("FUNCTION"),(String)msg.obj);
					
					break;
				case MESSAGE_XCALLB:
					//try {
					try {
						Window.this.xcallB(msg.getData().getString("FUNCTION"),(byte[])msg.obj);
					} catch (LuaException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		};
		
		
		//lua startup.
		mOwner = owner;
		
		this.mMainWindowHandler = mainWindowHandler;
		
		mName = name;
		
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
		
		//start extracting and setting settings.
		IntegerOption fontsize = (IntegerOption) settings.findOptionByKey("font_size");
		IntegerOption lineextra = (IntegerOption) settings.findOptionByKey("line_extra");
		IntegerOption buffersize = (IntegerOption) settings.findOptionByKey("buffer_size");
		FileOption fontpath = (FileOption)settings.findOptionByKey("font_path");
		ListOption colorOption = (ListOption) settings.findOptionByKey("color_option");
		ColorOption hyperlinkcolor = (ColorOption) settings.findOptionByKey("hyperlink_color");
		
		BooleanOption wordwrap = (BooleanOption)settings.findOptionByKey("word_wrap");
		BooleanOption hlenabled = (BooleanOption)settings.findOptionByKey("hyperlinks_enabled");
		
		ListOption hlmode = (ListOption)settings.findOptionByKey("hyperlink_mode");
		
		mPrefFont = loadFontFromName((String)fontpath.getValue());
		p.setTypeface(mPrefFont);
		
		mBuffer.setMaxLines((Integer)buffersize.getValue());
		
		mPrefLineExtra = (Integer)lineextra.getValue();
		mPrefFontSize = (Integer)fontsize.getValue();
		setCharacterSizes(mPrefFontSize,mPrefLineExtra);
		
		switch((Integer)colorOption.getValue()) {
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
		
		this.setWordWrap((Boolean)wordwrap.getValue());
		this.setLinkColor((Integer)hyperlinkcolor.getValue());
		this.setLinkMode((Integer)hlmode.getValue());
		this.setLinksEnabled((Boolean)hlenabled.getValue());
	}
	
	protected void resetAndAddText(byte[] obj) {
		mBuffer.empty();
		mHoldBuffer.empty();
		addBytes(obj,true);
	}

	protected void xcallB(String string,byte[] bytes) throws LuaException {
		if(mL == null) { return;}
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
		mL.getGlobal(string);
		if(mL.getLuaObject(-1).isFunction()) {
			mL.pushObjectValue(bytes);
			int ret = mL.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("WindowXCallB calling: " + string + " error:"+mL.getLuaObject(-1).getString());
			} else {
				mL.pop(2);
			}
		} else {
			mL.pop(2);
		}
	}

	private void startSelection(int line,int column) {
		
		theSelection = mBuffer.getSelectionForPoint(line,column);
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
	
	protected void updateEncoding(String value) {
		mBuffer.setEncoding(value);
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
		
		if(height == 0 && width == 0) {
			return;
		}
		mCalculatedLinesInWindow = (int) (height / mPrefLineSize);
		
		featurePaint.setTypeface(mPrefFont);
		featurePaint.setTextSize(mPrefFontSize);
		mOneCharWidth = (int)Math.ceil(featurePaint.measureText("a")); //measure a single character
		mCalculatedRowsInWindow = (width / mOneCharWidth);
		
		selectionIndicatorPaint.setTextSize(SELECTIONINDICATOR_FONTSIZE);
		selectionIndicatorPaint.setTypeface(mPrefFont);
		selectionIndicatorPaint.setAntiAlias(true);
		one_selection_char_is_this_wide = (int) Math.ceil(selectionIndicatorPaint.measureText("a"));
		selectionIndicatorVectorX = mOneCharWidth + selectionIndicatorHalfDimension;
		if(automaticBreaks) {
			this.setLineBreaks(0);
		}
		
		if(mBuffer.getBrokenLineCount() == 0) {
			jumpToZero();
		}
		
	}


	
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
		windowShowing = false;
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent t) {
		int action = t.getAction() & MotionEvent.ACTION_MASK;     
		int pointerIndex = (t.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerId = t.getPointerId(pointerIndex);
		
		if(pointer > 0 && pointerId != pointer) {
			//but invalidate this anyway
			this.invalidate();
			return false;
		}
			//normal
		if(!scrollingEnabled) {
			return false;
		}
		boolean retval = false;
		boolean noFunction = false;
		int index = t.findPointerIndex(pointerId);
		start_x = new Float(t.getX(index));
		start_x = start_x+1;
		
		if(mBuffer.getBrokenLineCount() != 0) {
			Rect rect = new Rect();
			if(!finger_down) {
				
				rect.top = 0;
				rect.left = 0;
				rect.right = mWidth;
				rect.bottom = mHeight;
				
				
				Point point = new Point();
				point.x = (int) t.getX();
				point.y = (int) t.getY();
				if(!rect.contains((int)t.getX(),(int)t.getY())) {
					return false;
				}
			}
			
			
			synchronized(token) {
			if(t.getAction() == MotionEvent.ACTION_DOWN) {
				pointer = pointerId;
				start_x = new Float(t.getX(index));
				start_y = new Float(t.getY(index));
				pre_event = MotionEvent.obtainNoHistory(t);
				mFlingVelocity = 0.0f;
				finger_down = true;
				finger_down_to_up = false;
				prev_draw_time = 0;
				
				for(int tmpCount=0;tmpCount<linkBoxes.size();tmpCount++) {
					if(linkBoxes.get(tmpCount).getBox().contains((int)(float)start_x,(int)(float)start_y)) {
						touchInLink = tmpCount;
					}
				}
				
				//calculate row/col
				float x = t.getX(index);
				float y = t.getY(index);
				
				//convert y to be at the bottom of the screen.
				
				y = (float) ((float)this.getHeight() - y + (scrollback-SCROLL_MIN));
				
				float xform_to_line = y / (float)mPrefLineSize;
				int line = (int)Math.floor(xform_to_line);
				
				float xform_to_column = x / (float)mOneCharWidth;
				int column = (int)Math.floor(xform_to_column);
				if(mTextSelectionEnabled) {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_STARTSELECTION, line, column), 1500);
				}
				
				if(homeWidgetShowing) {
					if(mHomeWidgetRect.contains((int)x,(int)t.getY())) {
						homeWidgetFingerDown = true;
					}
				}
				
			}
			
			if(t.getAction() == MotionEvent.ACTION_MOVE) {
				
	
				Float now_y = new Float(t.getY(index));
				
				
	
				float thentime = pre_event.getEventTime();
				float nowtime = t.getEventTime();
				
				float time = (nowtime - thentime) / 1000.0f; //convert to seconds
				
				float prev_y = pre_event.getY(index);
				float dist = now_y - prev_y;
				diff_amount = (int)dist;
				
				if(Math.abs(now_y - start_y) > mPrefLineSize*1.5) {
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
				mFlingVelocity = velocity;
				
				if(Math.abs(now_y - start_y) > mPrefLineSize*1.5*mDensity) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
				}
				
				if(Math.abs(diff_amount) > 5*mDensity) {
					
					pre_event = MotionEvent.obtainNoHistory(t);
				}
				
			}			
			int pointers = t.getPointerCount();
			for(int i=0;i<pointers;i++) {
				
				Float y_val = new Float(t.getY(index));
				Float x_val = new Float(t.getX(index));
				bx = x_val.intValue();
				by = y_val.intValue();
				
				prev_y = y_val;
			}
			
			
			if(t.getAction() == (MotionEvent.ACTION_UP)) {
				
				pre_event = null;
				prev_y = new Float(0);
		        
		        //reset the priority
		        pointer = -1;
	
		        pre_event = null;
		        finger_down=false;
		        finger_down_to_up = true;
		         
				if(touchInLink > -1) {
					mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_LAUNCHURL, linkBoxes.get(touchInLink).getData()));
			        touchInLink = -1;
				}
				
				
				mHandler.removeMessages(MESSAGE_STARTSELECTION);
					
				if(homeWidgetShowing && homeWidgetFingerDown) {
					if(mHomeWidgetRect.contains((int)t.getX(index),(int)t.getY(index))) {
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
	
	private void calculateScrollBack() {
		
		if(prev_draw_time == 0) { //never drawn before
			if(mBuffer.getBrokenLineCount() <= mCalculatedLinesInWindow) { scrollback = SCROLL_MIN; return;}
			if(finger_down) {
				scrollback = (double)Math.floor(scrollback + diff_amount);
				if(scrollback < SCROLL_MIN) {
					scrollback = SCROLL_MIN;
					//Log.e("FLUF","1: "+scrollback);
				} else {
					if(scrollback >= ((mBuffer.getBrokenLineCount() * mPrefLineSize))) {
						
						scrollback = (double)((mBuffer.getBrokenLineCount() * mPrefLineSize));
						//Log.e("FLUF","2: "+scrollback);
					}
				}
				diff_amount = 0;
			} else {
				if(finger_down_to_up) {
					prev_draw_time = System.currentTimeMillis(); 
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
				if(mFlingVelocity < 0) {
					mFlingVelocity = mFlingVelocity + fling_accel*duration_since_last_frame;
					scrollback =  (scrollback + mFlingVelocity*duration_since_last_frame);
					//Log.e("FLUF","3: "+scrollback);
				} else if (mFlingVelocity > 0) {
					
					mFlingVelocity = mFlingVelocity - fling_accel*duration_since_last_frame;
					scrollback =  (scrollback + mFlingVelocity*duration_since_last_frame);
					//Log.e("FLUF","4: "+scrollback);
				}
				
				if(Math.abs(new Double(mFlingVelocity)) < 15) {
					mFlingVelocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				}
					
				if(scrollback <= SCROLL_MIN) {
					scrollback = SCROLL_MIN;
					//Log.e("FLUF","5: "+scrollback);
					mFlingVelocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

					//mHandler.sendEmptyMessage(Window.MSG_CLEAR_NEW_TEXT_INDICATOR);
				}
				
				if(scrollback >= ((mBuffer.getBrokenLineCount() * mPrefLineSize))) {
					//Log.e("WINDOW","UPPER CAP OF THE BUFFER REACHED!");
					scrollback = (double)((mBuffer.getBrokenLineCount() * mPrefLineSize));
					//Log.e("FLUF","6: "+scrollback);
					mFlingVelocity = 0;
					prev_draw_time = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					
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
	ListIterator<Unit> unitIterator = null;
	private int mLinkBoxHeightMinimum = 20;
	
	boolean hasDrawRoutine = true;
	//private boolean drawn = false;
	public void onDraw(Canvas c) {
		/*if(drawn && !drawOnDemand) {
			Log.e("Window","Not drawing ("+mName+")-drawn and not draw on demand");
			return;//dont draw
			
		}*/
		//drawn = true;
		
		//synchronized(updateSynch) {
		
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
		
		
		if(mBuffer.getBrokenLineCount() != 0) {
			if(linkColor == null) {
				
				linkColor = new Paint();
				linkColor.setAntiAlias(true);
				linkColor.setColor(mLinkHighlightColor);
			}
			
			linkColor.setColor(mLinkHighlightColor);
			calculateScrollBack();
			c.save();
			Rect clip = new Rect();
			
			clip.top = 0;
			clip.left = 0;
			clip.right = mWidth;
			clip.bottom = mHeight;
			
			c.clipRect(clip);
			
			//now 0,0 is the lower left hand corner of the screen, and X and Y both increase positivly.
			Paint b = new Paint();
			b.setColor(0xFF0A0A0A);
			c.drawColor(0xFF0A0A0A); //fill with black
			c.drawRect(0,0,clip.right-clip.left,clip.top-clip.bottom,b);
			p.setTypeface(mPrefFont);
			p.setAntiAlias(true);
			p.setTextSize(mPrefFontSize);
			p.setColor(0xFFFFFFFF);
			
			float x = 0;
			float y = 0;
			if(mPrefLineSize * mCalculatedLinesInWindow < this.getHeight()) {
				
				y = ((mPrefLineSize * mCalculatedLinesInWindow) - this.getHeight()) - mPrefLineSize;
				//Log.e("STARTY","STARTY IS:"+y);
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
					bundle = getScreenIterator(scrollback,mPrefLineSize);
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
						mXterm256Color = false;
						mXterm256FGStart = false;
						mXterm256BGStart = false;
						for(int i=0;i<((TextTree.Color) u).getOperations().size();i++) {
						//for(Integer o : ((TextTree.Color) u).getOperations()) {
							
							updateColorRegisters(((TextTree.Color) u).getOperations().get(i));
							Colorizer.COLOR_TYPE type = Colorizer.getColorType(((TextTree.Color) u).getOperations().get(i));
							if(type != Colorizer.COLOR_TYPE.NOT_A_COLOR && type != Colorizer.COLOR_TYPE.BACKGROUND && type != Colorizer.COLOR_TYPE.BRIGHT_CODE) {
								bleeding = true;
							}
							
						}
						//bleeding = ((TextTree.Color)u).updateColorRegisters(selectedBright, selectedColor, selectedBackground);
						if(mXterm256FGStart) {
							p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor, mXterm256Color));
						} else {//b.setColor(0xFF000000 | Colorizer.getColorValue(0, selectedBackground));
							p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor, false));
							
						}
						
						b.setColor(0xFF000000);//no not bleed background colors
	
					}
				}
			}
			
			if(!bleeding) {
				//Log.e("WINDOW","WINDOW " + this.getName() + " is not bleeding");
				p.setColor(0xFF000000 | Colorizer.getColorValue(0,37,false));
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
			
			int postmp = screenIt.previousIndex();
			while(!stop && screenIt.hasPrevious()) {
				int loc = screenIt.previousIndex();
				Line l = screenIt.previous();
				
				
				if(mCenterJustify) {
					//center justify.

					int amount = mOneCharWidth*l.charcount;
					x = (float) ((mWidth/2.0)-(amount/2.0));
				}
				unitIterator = l.getIterator();
				
				int linemode = 0;
				if(startline2 == endline && startline2 == workingline) {
					linemode = 1;
				} else if(startline2 == workingline) {
					linemode = 2;
				} else if(startline2 > workingline && endline < workingline) {
					
					linemode = 3;
				} else if(endline == workingline) {
					linemode = 4;
					
				}
				
				boolean finishedWithNewLine = false;
				
				while(unitIterator.hasNext()) {
					Unit u = unitIterator.next();
					//p.setColor(color)
					boolean useBackground = false;
					if(b.getColor() != 0xFF0A0A0A && b.getColor() != 0xFF000000) {
						useBackground = true;
					}
					
					switch(u.type) {
					case WHITESPACE:
					case TEXT:
						TextTree.Text text = ((TextTree.Text)u);
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
							
							switch(linemode) {
							case 1:
								int finishCol = workingcol + text.bytecount;
								if(finishCol > startcol && finishCol-1 <= endcol){
									if((finishCol - startcol) < text.bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * mOneCharWidth;
										int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize()+(3*mDensity), x + stringWidth, y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + p.measureText(text.getString()), y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
									}
								} else if(finishCol > endcol) {
									if((finishCol - endcol) < text.bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * mOneCharWidth;
										c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + overshootPixels, y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
									} 
								} 
								break;
							case 2:
								finishCol = workingcol + text.bytecount;
								if(finishCol > startcol) {
									if((finishCol - startcol) < text.bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * mOneCharWidth;
										int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize()+(2*mDensity), x + stringWidth, y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + p.measureText(text.getString()), y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
									}
								} 
								break;
							case 3:
								
								c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + p.measureText(text.getString()), y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
								break;
							case 4:
								finishCol = workingcol + text.bytecount;
								if(finishCol >= endcol) {
									if((finishCol - endcol) < text.bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * mOneCharWidth;
										c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + overshootPixels, y+(4*mDensity), scroller_paint);
									}
								} else {
									c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + p.measureText(text.getString()), y+(4*mDensity), mTextSelectionIndicatorBackgroundPaint);
								}
								break;
							default:
								break;
							}
						}
						
						if(useBackground) {
							c.drawRect(x, y - p.getTextSize()+(2*mDensity), x + p.measureText(text.getString()), y+(4*mDensity), b);
						}
						
						if(text.isLink() || doingLink) {
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
								currentLink.append(text.getString());
								
								
								Rect r = new Rect();
								r.left = (int) x;
								r.top = (int) (y - p.getTextSize());
								r.right = (int) (x + p.measureText(text.getString()));
								r.bottom = (int) (y+5);
								if(mLinkMode == LINK_MODE.BACKGROUND) {
									linkColor.setColor(mLinkHighlightColor);
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
							switch(mLinkMode) {
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
								linkColor.setColor(mLinkHighlightColor);
								linkColor.setUnderlineText(true);
								break;
							case HIGHLIGHT_COLOR_ONLY_BLAND:
								
								linkColor.setTextSize(p.getTextSize());
								linkColor.setTypeface(p.getTypeface());
								if(mSelectedColor == 37) {
									linkColor.setColor(mLinkHighlightColor);
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
								int counterpart = 0xFF000000 | (mLinkHighlightColor ^ 0xFFFFFFFF);
								linkColor.setColor(counterpart);
								break;
							default:
								linkColor.setTextSize(p.getTextSize());
								linkColor.setTypeface(p.getTypeface());
								linkColor.setUnderlineText(false);
								linkColor.setColor(mLinkHighlightColor);
							}
							
							if(doIndicator) {
								int unitEndCol = workingcol + (text.bytecount-1);
								if(unitEndCol > selectedSelector.column - 10 && workingcol < selectedSelector.column +10) {
									float size = p.getTextSize();
									p.setTextSize(30);
									int overshoot = (workingcol - selectedSelector.column);
									int ix = 0,iy=SELECTIONINDICATOR_FONTSIZE;
									//if(overshoot > 0) {
										ix = (int) (selectionIndicatorHalfDimension + (overshoot*one_selection_char_is_this_wide) - 0.5*one_selection_char_is_this_wide);
										iy = (int) (selectionIndicatorHalfDimension+(0.5*SELECTIONINDICATOR_FONTSIZE)) + (indicatorlineoffset*SELECTIONINDICATOR_FONTSIZE);
									
									
									
									mSelectionIndicatorCanvas.drawText(text.getString(), ix, iy, p);
									
									p.setTextSize(size);
								}
								
							}
							c.drawText(text.getString(),x,y,linkColor);
							x += p.measureText(text.getString());
							
						} else {
							
							if(doIndicator) {
								int unitEndCol = workingcol + (text.bytecount-1);
								if(unitEndCol > selectedSelector.column - 10 && workingcol < selectedSelector.column +10) {
									float size = p.getTextSize();
									p.setTextSize(30);
									int overshoot = (workingcol - selectedSelector.column);
									int ix = 0,iy=SELECTIONINDICATOR_FONTSIZE;
									//if(overshoot > 0) {
										ix = (int) (selectionIndicatorHalfDimension + (overshoot*one_selection_char_is_this_wide) - 0.5*one_selection_char_is_this_wide);
										iy = (int) (selectionIndicatorHalfDimension+(0.5*SELECTIONINDICATOR_FONTSIZE)) + (indicatorlineoffset*SELECTIONINDICATOR_FONTSIZE);
									
									
									
									mSelectionIndicatorCanvas.drawText(text.getString(), ix, iy, p);
									
									p.setTextSize(size);
								}
								
							}
							workingcol += text.bytecount;
							c.drawText(text.getString(),x,y,p);
							x += p.measureText(text.getString());
						}
						
						break;
					case COLOR:
						mXterm256Color = false;
						mXterm256FGStart = false;
						mXterm256BGStart = false;
						for(int i=0;i<((TextTree.Color) u).getOperations().size();i++) {
							updateColorRegisters(((TextTree.Color) u).getOperations().get(i));
						}
						
						if(mColorDebugMode == 2 || mColorDebugMode == 3) {
							p.setColor(0xFF000000 | Colorizer.getColorValue(0, 37,false));
							b.setColor(0xFF000000 | Colorizer.getColorValue(0, 40,false));
						} else {
							if(mXterm256FGStart) {
								if(mSelectedColor == 33) {
									mSelectedColor = 33;
								}
								p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor,mXterm256Color));
							} else {
								if(!mXterm256BGStart) {
									p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor,false));
								}
							}
							
							if(mXterm256BGStart) {
								b.setColor(0xFF000000 | Colorizer.getColorValue(0, mSelectedBackground,mXterm256Color));
							} else {
								b.setColor(0xFF000000 | Colorizer.getColorValue(0, mSelectedBackground,false));
								
							}
						}
						if(mColorDebugMode == 1 || mColorDebugMode == 2) {
							String str = "";
							try {
								str = new String(((TextTree.Color)u).bin,"ISO-8859-1");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							c.drawText(str,x,y,p);
							x += p.measureText(str);
						}
						break;
					case NEWLINE:
					case BREAK:
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
								linemode = 4;
								
							} else {
								linemode = -1;
							}
						}
						
						finishedWithNewLine = true;
						
						//TODO: make sure that where this is moved to works
						y = y + mPrefLineSize;
						
						
						x = 0;
						drawnlines++;
						workingcol = 0;
						if(drawnlines > mCalculatedLinesInWindow + extraLines) {
							stop = true;
						}
						break;
					default:
						break;
					}
				}
				if(!finishedWithNewLine) {
					y = y + mPrefLineSize;
					x = 0;
					drawnlines++;
					workingcol = 0;
				}
				workingline = workingline - 1;
				workingcol = 0;
				l.resetIterator();
			}
			showScroller(c);
			c.restore();
			if(Math.abs(mFlingVelocity) > mPrefLineSize) {
				if(!mHandler.hasMessages(MESSAGE_DRAW)) {
					this.mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW,3);
				}
			} else {
				mFlingVelocity = 0;
			}
		
		}
		
		//phew, do the lua stuff, and lets be done with this.
		c.save();
		if(hasDrawRoutine){
			if(mL != null) {
				
/*! \page entry_points
 * \section window Window Lua State Entry Points
 * \subsection OnDraw OnDraw
 * This function is called whenever the window is dirty and needs redrawing of custom content.
 * 
 * \param canvas
 * 
 * \note It is difficult to know exactly what needs to be freed for garbage collection, how to do it, and weather or not it worked. A good example is the button window, it has many custom resources and I had run into memory issues with it when closing/opening the window a few times. It may never happen, it may happen after 100 open/close cycles, or 5, but the general trend of running the foreground process out of memory is an immediate termination of the window. So if you are in a case where you are coming back into the appliation after a phone call or web browser and it immediatly exits, this may be the culprit.
 */
				
				mL.getGlobal("debug");
				mL.getField(mL.getTop(), "traceback");
				mL.remove(-2);
				
				
				mL.getGlobal("OnDraw");
				if(mL.isFunction(mL.getTop())) {
					mL.pushJavaObject(c);
					
					
					
					int ret = mL.pcall(1, 1, -3);
					if(ret != 0) {
						displayLuaError("Error calling OnDraw: " + mL.getLuaObject(-1).toString());
					} else {
						//Log.e("LUAWINDOW","OnDraw success!");
						//hasDrawRoutine = false;
						mL.pop(2);
					}
				} else {
					hasDrawRoutine = false;
					mL.pop(2);
				}
			}
		}
		
		c.restore();
	}
	
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
	

	public void showScroller(Canvas c) {
		scroller_paint.setColor(0xFFFF0000);
		
		if(mBuffer.getBrokenLineCount() < 1) {
			return; //no scroller to show.
		}
		
		if(scrollback > SCROLL_MIN +3*mDensity && mBuffer.getBrokenLineCount() > mCalculatedLinesInWindow) {
			homeWidgetShowing = true;
			c.drawBitmap(mHomeWidgetDrawable, mHomeWidgetRect.left, mHomeWidgetRect.top, null);
		} else {
			homeWidgetShowing = false;
		}
		
		double scrollerSize = 0.0f;
		double scrollerPos = 0.0f;
		double posPercent = 0.0f;
		
		float workingHeight = mHeight;
		float workingWidth = mWidth;
		
		Float windowPercent = workingHeight / (mBuffer.getBrokenLineCount()*mPrefLineSize);
		if(windowPercent > 1) {
			//then we have but 1 page to show
			return;
		} else {
			scrollerSize = windowPercent*workingHeight;
			posPercent = (scrollback - (workingHeight/2))/(mBuffer.getBrokenLineCount()*mPrefLineSize);
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
			int currentLine = theSelection.start.line * mPrefLineSize;
			currentLine = (int) (currentLine - (scrollback - SCROLL_MIN));
			
			
			int startBottom = (int) (this.getHeight() - currentLine);
			int startTop = startBottom - mPrefLineSize;
			int startLeft = theSelection.start.column * mOneCharWidth;
			int startRight = startLeft + mOneCharWidth;
			
			currentLine = theSelection.end.line * mPrefLineSize;
			currentLine = (int) (currentLine - (scrollback - SCROLL_MIN));
			
			int endBottom = (int) (this.getHeight() - currentLine);
			int endTop = endBottom - mPrefLineSize;
			int endLeft = theSelection.end.column * mOneCharWidth;
			int endRight = endLeft + mOneCharWidth;
			
			//int scroll_from_bottom = (int) (scrollback-SCROLL_MIN);
			
			c.drawRect(startLeft, startTop-2, startRight, startBottom-2, mTextSelectionIndicatorPaint);
			c.drawRect(endLeft, endTop-2, endRight, endBottom-2, mTextSelectionIndicatorPaint);
			
			int x=0,y=0;
			if(selectedSelector == theSelection.end) {
				x = endLeft + (endRight - endLeft)/2;
				y = endTop + (endBottom - endTop)/2;
			} else {
				x = startLeft + (startRight - startLeft)/2;
				y = startTop + (startBottom - startTop)/2;
			}
			
			if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
				c.drawCircle(x, y-2, 50*density, mTextSelectionIndicatorCirclePaint);
			} else {
				c.drawCircle(x, y-2, 33*density, mTextSelectionIndicatorCirclePaint);
			}
			
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
			if(mTextSelectionCopyBitmap.isRecycled()) {
				//Log.e("sf","bitmap is recycled");
			}
			mSelectionIndicatorCanvas.drawBitmap(mTextSelectionCopyBitmap, 0,0, null);
			mSelectionIndicatorCanvas.drawBitmap(mTextSelectionCancelBitmap, 0,2*third, null);
			mSelectionIndicatorCanvas.drawBitmap(mTextSelectionSwapBitmap, 2*third,0, null);
			
			
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

	public void clearText() {
		mBuffer.dumpToBytes(false);
		mBuffer.prune();
	}
	
	//Object synch = new Object();
	public void flushBuffer() {
			try {
				
					mBuffer.addBytesImpl(mHoldBuffer.dumpToBytes(false));
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			mBuffer.prune();
		drawingIterator = null;
		this.invalidate();
	}
	
	
	public void setButtonHandler(Handler useme) {
		//realbuttonhandler = useme;
	}
	//public void setDispatcher(Handler h) {
	//	dataDispatch = h;
	//}
	//public void setInputType(EditText t) {
		// t;
	//}

	public void jumpToZero() {

		synchronized(token) {
			SCROLL_MIN = mHeight-(double)(5*Window.this.getResources().getDisplayMetrics().density);
			scrollback = SCROLL_MIN;
			mFlingVelocity=0;
		}
				
	}

	public void doDelayedDraw(int i) {
		if(!mHandler.hasMessages(MESSAGE_DRAW)) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW, i);
		}
	}

	public void setColorDebugMode(int i) {
		mColorDebugMode = i;
		doDelayedDraw(1);
	}

	public void setEncoding(String pEncoding) {
			mBuffer.setEncoding(pEncoding);
	}

	public void setCharacterSizes(int fontSize, int fontSpaceExtra) {
		mPrefFontSize = fontSize;
		mPrefLineExtra = fontSpaceExtra;
		mPrefLineSize = (int) (mPrefFontSize + mPrefLineExtra);
		calculateCharacterFeatures(mWidth,mHeight);
	}

	public void setMaxLines(int maxLines) {
		mBuffer.setMaxLines(maxLines);
	}

	public void setFont(Typeface font) {
		mPrefFont = font;
	}
	
	public void setBold(boolean bold) {
		if(bold) {
			mPrefFont = Typeface.create(mPrefFont, Typeface.BOLD);
			p.setTypeface(mPrefFont);
		} else {
			mPrefFont = Typeface.create(mPrefFont, Typeface.NORMAL);
			p.setTypeface(mPrefFont);
		}
	}
	
	public Typeface getFont() {
		return mPrefFont;
	}
	
	
	boolean automaticBreaks = true;
	public void setLineBreaks(Integer i) {
		
			if(i == 0) {
				if(mCalculatedRowsInWindow != 0) {
					mBuffer.setLineBreakAt(mCalculatedRowsInWindow);
				} else {
					mBuffer.setLineBreakAt(80);
				}
				automaticBreaks = true;
			} else {
				mBuffer.setLineBreakAt(i);
				automaticBreaks = false;
			}
		
		
			
		this.invalidate();
	}
	
	public void setWordWrap(boolean pIn ) {
		
			mBuffer.setWordWrap(pIn);
		
			jumpToZero();
		
			this.invalidate();
	}
	
	public void setLinkMode(LINK_MODE mode) {
		this.mLinkMode = mode;
	}
	
	public void setLinkColor(int linkColor) {
		this.mLinkHighlightColor = linkColor;
	}
	
	public void clearAllText() {
			mBuffer.empty();
	}
	
	public void addBytes(byte[] obj,boolean jumpToEnd) {
			addBytesImpl(obj,jumpToEnd);
	}
	
	public void addText(String str,boolean jumpToEnd) {
		try {
			addBytesImpl(str.getBytes(mBuffer.getEncoding()),jumpToEnd);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	
	private void addBytesImpl(byte[] obj,boolean jumpToEnd) {
		if(obj.length == 0) return;
		
			if(mBufferText) {
				//synchronized(synch) {
					mHoldBuffer.addBytesImplSimple(obj);
				//}
				return;
			}
			
			int oldbrokencount = mBuffer.getBrokenLineCount();
			double old_max = mBuffer.getBrokenLineCount() * mPrefLineSize;
			//synchronized(synch) {
			int linesadded = 0;
			try {
				linesadded = mBuffer.addBytesImpl(obj);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			int tmpcount = mBuffer.getBrokenLineCount();
			drawingIterator = null;
			
			if(jumpToEnd) {
				scrollback = SCROLL_MIN;
				//mHandler.sendEmptyMessage(MSG_CLEAR_NEW_TEXT_INDICATOR);
			} else {
				if(mBuffer.getBrokenLineCount() <= mCalculatedLinesInWindow) {
					scrollback = (double)mHeight;
				} else {
					if(scrollback > SCROLL_MIN + mPrefLineSize ) {
						//scrollback = oldposition * (the_tree.getBrokenLineCount()*PREF_LINESIZE);
						double new_max = mBuffer.getBrokenLineCount()*mPrefLineSize;
						int lines = (int) ((new_max - old_max)/mPrefLineSize);
						
						scrollback += linesadded*mPrefLineSize;
						//Log.e("BYTE",mName+"REPORT: old_max="+old_max+" new_max="+new_max+" delta="+(new_max-old_max)+" scrollback="+scrollback + " lines="+lines + " oldbroken="+oldbrokencount+ "newbroken="+the_tree.getBrokenLineCount());
						
					} else {
						scrollback = SCROLL_MIN;
					}
				
				}
				if(scrollback > mHeight) {
					if(!indicated) {
						
						indicated = true;
					}
				} else {
					indicated = false;
				}
			}
			mBuffer.prune();
			tmpcount = mBuffer.getBrokenLineCount();
		this.invalidate();
	}
	
	
	
	private Colorizer.COLOR_TYPE updateColorRegisters(Integer i) {
		if(i == null) return Colorizer.COLOR_TYPE.NOT_A_COLOR;
		
		if(mXterm256Color) {
			if(mXterm256FGStart) {
				mSelectedColor = i;
				//xterm256FGStart = false;
				//xterm256Color = false;
			}
			
			if(mXterm256BGStart) {
				mSelectedBackground = i;
				//xterm256BGStart = false;
				//xterm256Color = false;
			}
			
			return null;
		}
		
		Colorizer.COLOR_TYPE type = Colorizer.getColorType(i);
		switch(type) {
		case FOREGROUND:
			mSelectedColor = i;
			mXterm256FGStart = false;
			mXterm256BGStart = false;
			mXterm256Color = false;
			//opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBright, selectedColor));
			//notFound = false;
			break;
		case BACKGROUND:
			//Log.e("SLICK","BACKGROUND COLOR ENCOUNTERED: " + i);
			mSelectedBackground = i;
			mXterm256FGStart = false;
			mXterm256BGStart = false;
			mXterm256Color = false;
			//bg_opts.setColor(0xFF000000 | Colorizer.getColorValue(selectedBackgroundBright, selectedBackgroundColor));
			break;
		case ZERO_CODE:
			//Log.e("WINDOW","ZERO CODE ENCOUNTERED");
			mSelectedBright = 0;
			mSelectedColor = 37;
			mSelectedBackground = 40;
			mXterm256FGStart = false;
			mXterm256BGStart = false;
			mXterm256Color = false;
			break;
		case BRIGHT_CODE:
			mSelectedBright = 1;
			mXterm256FGStart = false;
			mXterm256BGStart = false;
			mXterm256Color = false;
			break;
		case XTERM_256_FG_START:
			mXterm256FGStart = true;
			break;
		case XTERM_256_BG_START:
			mXterm256BGStart = true;
			break;
		case XTERM_256_FIVE:
			if(mXterm256BGStart || mXterm256FGStart) {
				mXterm256Color = true;
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
			mBuffer.setCullExtraneous(pIn);
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
		double pY = pIn;
		double max = mBuffer.getBrokenLineCount() * pLineSize;
		if(pY >= max) {
			pY = max;
		}
		
		int startline = 0;
		int current = 0;
		if(drawingIterator == null) {
			drawingIterator = mBuffer.getLines().listIterator();
		} else {
			while(drawingIterator.hasPrevious()) {
				drawingIterator.previous(); //reset to beginning
			}
		}
		
		if(mBuffer.getBrokenLineCount() <= mCalculatedLinesInWindow) {
			int offset = 0;
			if(mPrefLineSize * mCalculatedLinesInWindow < this.getHeight()) {
				
				offset = ((mPrefLineSize) * mCalculatedLinesInWindow) - this.getHeight();
			}
			int under = mCalculatedLinesInWindow-(mBuffer.getBrokenLineCount()-1);
			while(drawingIterator.hasNext()) {drawingIterator.next(); startline += 1;}
			float tmpy = (under*pLineSize-(offset+(mPrefLineSize/3)));
			
			return new IteratorBundle(drawingIterator,tmpy,0,startline);
		}
		int lines = 1;
		
		while(drawingIterator.hasNext()) {
			//position = drawingIterator.nextIndex();
			Line l = drawingIterator.next();
			working_h += pLineSize * (1 + l.getBreaks());
			current += 1 + l.getBreaks();
			lines = lines + 1;
			
			if(working_h >= pY) {
				int y = 0;
				if(mPrefLineSize * mCalculatedLinesInWindow < this.getHeight()) {
					
					y = ((mPrefLineSize) * mCalculatedLinesInWindow) - this.getHeight();
				}
				double delta = working_h - pY;
				double offset = delta - pLineSize;
				int extra = (int) Math.ceil(delta/pLineSize);
				if(drawingIterator.hasPrevious()) drawingIterator.previous();
				if(l.breaks > 0) {
					startline += l.breaks;
				}
				return new IteratorBundle(drawingIterator,-1*offset,extra,startline);
			} 
			startline += 1 + l.getBreaks();
		}
		
		return new IteratorBundle(drawingIterator,pLineSize,0,startline);
		
	}

	public void setLinksEnabled(boolean hyperLinkEnabled) {
		mBuffer.setLinkify(hyperLinkEnabled);
	}

	public boolean windowShowing = false;
	public boolean loaded() {
		
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

		@Override
		public void xcallB(String function, byte[] raw) throws RemoteException {
			Message m = mHandler.obtainMessage(MESSAGE_XCALLB,raw);
			m.getData().putString("FUNCTION", function);
			mHandler.sendMessage(m);
		}

		@Override
		public void resetWithRawDataIncoming(byte[] raw) throws RemoteException {
			mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RESETWITHDATA,raw));
			
		}
		
	};
	
	public IWindowCallback.Stub getCallback() {
		return mCallback;
	}

	public void setBufferText(boolean bufferText) {
		this.mBufferText = bufferText;
	}

	public boolean isBufferText() {
		return mBufferText;
	}

	protected void onSizeChanged(int w,int h,int oldw,int oldh) {
		boolean dofit = false;
		if(mWidth != w) {
			dofit = true;
		}
		mWidth = w;
		mHeight = h;
		if(dofit) {
			doFitFontSize(mWidth);
		}
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
		
		//if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) {
		//	scrollback = 0.0;
		//}

		
		mHomeWidgetRect.set(mWidth-mHomeWidgetDrawable.getWidth(),mHeight-mHomeWidgetDrawable.getHeight(),mWidth,mHeight);
		
		Float foo = new Float(0);
		//foo.
		
		if(mL == null || !hasOnSizeChanged) return;
		mL.getGlobal("debug");
		mL.getField(mL.getTop(), "traceback");
		mL.remove(-2);
		mL.getGlobal("OnSizeChanged");
		if(mL.getLuaObject(mL.getTop()).isFunction()) {
			mL.pushString(Integer.toString(w));
			mL.pushString(Integer.toString(h));
			mL.pushString(Integer.toString(oldw));
			mL.pushString(Integer.toString(oldh));
			int ret = mL.pcall(4, 1, -6);
			if(ret != 0) {
				displayLuaError("Window("+mName+") OnSizeChangedError: " + mL.getLuaObject(-1).getString());
			} else {
				mL.pop(2);
			}
		} else {
			//Log.e("LUAWINDOW","Window("+mName+"): No OnSizeChanged Function Defined.");
			hasOnSizeChanged = false;
			mL.pop(2);
		}
		this.invalidate();
	}
	boolean hasOnSizeChanged = true;
	
	protected void xcallS(String string, String str) {
		if(mL == null) return;
		mL.getGlobal("debug");
		mL.getField(mL.getTop(), "traceback");
		mL.remove(-2);
		
		mL.getGlobal(string);
		if(mL.getLuaObject(-1).isFunction()) {
			
			//need to start iterating the given map, re-creating the table on the other side.
			//pushTable("",obj);
			mL.pushString(str);
			
			int ret = mL.pcall(1, 1, -3);
			if(ret !=0) {
				displayLuaError("WindowXCallT Error:" + mL.getLuaObject(-1).getString());
			} else {
				//success!
				mL.pop(2);
			}
			
		} else {
			mL.pop(2);
		}
	}
	

	private void initLua() {
		mL.openLibs();
		
		if(dataDir == null) {
			//this is bad.
		} else {
			
			//set up the path/cpath.
			//TODO: add the plugin load path.
			mL.getGlobal("package");
			mL.pushString(dataDir + "/lua/share/5.1/?.lua");
			mL.setField(-2, "path");
			
			mL.pushString(dataDir + "/lua/lib/5.1/?.so");
			mL.setField(-2, "cpath");
			mL.pop(1);
			
		}
		
		
		NoteFunction df = new NoteFunction(mL);
		OptionsMenuFunction omf = new OptionsMenuFunction(mL);
		PluginXCallSFunction pxcf = new PluginXCallSFunction(mL);
		SheduleCallbackFunction scf = new SheduleCallbackFunction(mL);
		CancelSheduleCallbackFunction cscf = new CancelSheduleCallbackFunction(mL);
		GetDisplayDensityFunction gddf = new GetDisplayDensityFunction(mL); 
		SendToServerFunction stsf = new SendToServerFunction(mL);
		GetExternalStorageDirectoryFunction gesdf = new GetExternalStorageDirectoryFunction(mL);
		PushMenuStackFunction pmsf = new PushMenuStackFunction(mL);
		PopMenuStackFunction popmsf = new PopMenuStackFunction(mL);
		GetStatusBarHeight gsbshf = new GetStatusBarHeight(mL);
		StatusBarHiddenMethod sghm = new StatusBarHiddenMethod(mL);
		GetActionBarHeightFunction gabhf = new GetActionBarHeightFunction(mL);
		GetPluginInstallDirectoryFunction gpisdf = new GetPluginInstallDirectoryFunction(mL);
        CloseOptionsDialogFunction codf = new CloseOptionsDialogFunction(mL);
        GetActivityFunction gaf = new GetActivityFunction(mL);
        PluginInstalledFunction pif = new PluginInstalledFunction(mL);
        WindowSupportsFunction wsf = new WindowSupportsFunction(mL);
        WindowCallFunction wcf = new WindowCallFunction(mL);
        WindowBroadcastFunction wbcf = new WindowBroadcastFunction(mL);
		try {
			
			gsbshf.register("GetStatusBarHeight");
			//iv.register("Invalidate");
			df.register("Note");
			//bf.register("GetBounds");
			omf.register("AddOptionCallback");
			pxcf.register("PluginXCallS");
			scf.register("ScheduleCallback");
			cscf.register("CancelCallback");
			gddf.register("GetDisplayDensity");
			stsf.register("SendToServer");
			gesdf.register("GetExternalStorageDirectory");
			pmsf.register("PushMenuStack");
			popmsf.register("PopMenuStack");
			sghm.register("IsStatusBarHidden");
			gabhf.register("GetActionBarHeight");
			gpisdf.register("GetPluginInstallDirectory");
			codf.register("CloseOptionsDialog");
			gaf.register("GetActivity");
			pif.register("PluginInstalled");
			wcf.register("WindowCall");
			wsf.register("WindowSupports");
			wbcf.register("WindowBroadcast");
		} catch (LuaException e) {
			e.printStackTrace();
		}
		
	}
	
	
	boolean noScript = true;
	public void loadScript(String body) {
		
		if(body == null || body.equals("")) {
			noScript = true;
			if(mL != null) {
				mL.close();
				mL = null;
			}
			return;
		} else {
			noScript = false;
		}
		if(mL != null) {
			mL.close();
			mL = null;
		}
		this.mL = LuaStateFactory.newLuaState();
		initLua();
		mL.pushJavaObject(this);
		mL.setGlobal("view");
		
		
		
		mL.getGlobal("debug");
		mL.getField(mL.getTop(), "traceback");
		mL.remove(-2);
		mL.LloadString(body);
		int ret = mL.pcall(0, 1, -2);
		if(ret != 0) {
			displayLuaError("Error Loading Script: "+mL.getLuaObject(mL.getTop()).getString());
		} else {
			mL.pop(2);
		}

	}
	
	public void runScriptOnCreate() {
		if(mL == null) return;
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
		
		mL.getGlobal("OnCreate");
		if(mL.getLuaObject(-1).isFunction()) {
			int tmp = mL.pcall(0, 1, -2);
			if(tmp != 0) {
				displayLuaError("Calling OnCreate: "+mL.getLuaObject(-1).getString());
			} else {
				//Log.e("LUAWINDOW","OnCreate Success for window ("+this.getName()+")!");
				mL.pop(2);
			}
		} else {
			mL.pop(2);
		}
		
		mL.getGlobal("OnMeasure");
		if(mL.isFunction(-1)) {
			mHasScriptOnMeasure = true;
		} else {
			mHasScriptOnMeasure = false;
		}
		mL.pop(1);
	}
	
	/*! \page page1
	* \section window Window Functions
	* \subsection sec2 AddOptionCallback
	* Add a top level menu item that will call a global function when pressed.
	* 
	* \par Full Signature
	* \code
	* AddOptionCallback(functionName,menuText,iconDrawable)
	* \endcode
	* \param functionName \c string value of the function name that will be called when the menu item is pressed.
	* \param menuText \c string value that will appear on the menu item.
	* \param iconDrawable \c android.graphics.drawable.Drawable the drawable resource that will be used for the icon.
	* \returns nothing
	* \par Example with no icon
	* \code
	* AddOptionCallback("functionName","Click Me!",nil)
	* \endcode
	* \par Example with icon
	* \code
	* drawable = luajava.newInstance("android.drawable.BitmapDrawable",context:getResources(),"/path/to/image.png")
	* function menuClicked()
	* 	Note("Menu Item Clicked!")
	* end
	* 
	* AddOptionCallback("menuClicked","Click Me!",drawable)
	* \endcode
	* 
	*/
	private class OptionsMenuFunction extends JavaFunction {

		public OptionsMenuFunction(LuaState L) {
			super(L);
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
			Message msg = mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_ADDOPTIONCALLBACK);
			if(o != null) msg.obj = o;
			Bundle b = msg.getData();
			b.putString("funcName", funcName);
			b.putString("title", title);
			b.putString("window", mName);
			msg.setData(b);
			mMainWindowHandler.sendMessage(msg);
			return 0;
		}
		
	}
	
  /*! \page page1
	* \subsection sec4 CancelCallback
	* Cancel a scheduled call made with ScheduleCallback.
	* \note This will cancel all pending callbacks with the given identifier.
	* 
	* \par Full Signature
	* \code
	* CancelCallback(id)
	* \endcode
	* \param id \c number the callback id to cancel
	* \returns nothing
	* \par Example 
	* \code
	* CancelCallback(100)
	* \endcode
	*/
	private class CancelSheduleCallbackFunction extends JavaFunction {

		public CancelSheduleCallbackFunction(LuaState L) {
			super(L);

		}

		@Override
		public int execute() throws LuaException {
			int id = Integer.parseInt(this.getParam(2).getString());
			//String callback = this.getParam(3).getString();
			//callScheduleCallback(id,callback);
			callbackHandler.removeMessages(id);
			return 0;
		}
		
	}
	
  /*! \page page1
	* \subsection sec16 CloseOptionsDialog
	* Closes the Options dialog if it is currently open.
	* 
	* \par Full Signature
	* \code
	* CloseOptionsDialog()
	* \endcode
	* \param none
	* \returns nothing
	* \par Example 
	* \code
	* CloseOptionsDialog()
	* \endcode
	*/
	private class CloseOptionsDialogFunction extends JavaFunction {
		public CloseOptionsDialogFunction(LuaState L) {
			super(L);
		}
		
		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_CLOSEOPTIONSDIALOG));
			return 0;
		}
	}
	
	private class GetActionBarHeightFunction extends JavaFunction {

		public GetActionBarHeightFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			L.pushString(Integer.toString(((int)Window.this.mParent.getTitleBarHeight())));
			return 1;
		}
		
	}
	
 /*! \page page1
	* \subsection sec0 GetActivity
	* Get a handle to the current Activity that is hosting the foreground window process.
	* 
	* \par Full Signature
	* \code
	* GetActivity()
	* \endcode
	* \param none
	* \returns \c android.app.Activity the current Activity that is hosting the foreground processes.
	* \par Example 
	* \code
	* activity = GetActivity()
	* \endcode
	*/
	private class GetActivityFunction extends JavaFunction {

		public GetActivityFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			//Log.e("PLUGIN","Get External storage state:"+Environment)
			L.pushJavaObject((Activity)mParent.getActivity());
			return 1;
		}
		
	}
	
	private class GetDisplayDensityFunction extends JavaFunction {

		public GetDisplayDensityFunction(LuaState L) {
			super(L);
			
		}

		@Override
		public int execute() throws LuaException {
			float density = Window.this.getContext().getResources().getDisplayMetrics().density;
			//if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			//	density = density * 1.5f;
			//}
			//Log.e("WINODW","PUSHING DENSITY:"+Float.toString(density));
			L.pushNumber(density);
			return 1;
		}
		
	}
	

		private class GetExternalStorageDirectoryFunction extends JavaFunction {

			public GetExternalStorageDirectoryFunction(LuaState L) {
				super(L);
				// TODO Auto-generated constructor stub
			}

			@Override
			public int execute() throws LuaException {
				//Log.e("PLUGIN","Get External storage state:"+Environment)
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					L.pushString(Environment.getExternalStorageDirectory().getAbsolutePath());
				} else {
					L.pushNil();
				}
				return 1;
			}
			
		}
		
	 
		

	private class GetPluginInstallDirectoryFunction extends JavaFunction {

		public GetPluginInstallDirectoryFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			//Log.e("PLUGIN","Get External storage state:"+Environment)
			/*if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				L.pushString(Environment.getExternalStorageDirectory().getAbsolutePath());
			} else {
				L.pushNil();
			}*/
			String path = mParent.getPathForPlugin(mOwner);
			//Log.e("LUA","FETCHED PATH ("+path+") for plugin, "+mOwner);
			File file = new File(path);
			String dir = file.getParent();
			//file.getPar
			L.pushString(dir);
			return 1;
		}
		
	}
	

	private class GetStatusBarHeight extends JavaFunction {

		public GetStatusBarHeight(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			L.pushString(Integer.toString((int)Window.this.mParent.getStatusBarHeight()));
			return 1;
		}
		
	}
	
	/*! \page page1
	* \subsection sec14 IsStatusBarHidden
	* Gets the state of the status bar.
	* 
	* \par Full Signature
	* \code
	* IsStatusBarHidden()
	* \endcode
	* \param none
	* \returns \c bool true if the status bar is hidden (full screen), false if the status bar is being shown (non full screen)
	* \par Example 
	* \code
	* if(IsStatusBarHidden()) then
	*  Note("status bar hidden")
	* else
	*  Note("status bar not hidden")
	* end
	* \endcode
	*/
	private class StatusBarHiddenMethod extends JavaFunction {

		public StatusBarHiddenMethod(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			L.pushBoolean(Window.this.mParent.isStatusBarHidden());
			return 1;
		}
		
	}
	
	

	protected class NoteFunction extends JavaFunction {

		public NoteFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			String foo = this.getParam(2).getString();
			//Log.e("LUAWINDOW","DEBUG:"+foo);
			Window.this.mParent.dispatchLuaText(foo);
			return 0;
		}
		
	}
	
  /*! \page page1
	* \subsection PluginXCallS PluginXCallS
	* Calls a function in the parent plugin's lua state. Provides one way signaling across the aidl bridge to the plugin host running in the background.
	* 
	* \par Full Signature
	* \code
	* PluginXCallS(functionName,data)
	* \endcode
	* \param functionName \c string the global function in the plugin's host lua state.
	* \param data \c string the data to pass as a argument to the given function
	* \returns nothing
	* \par Example 
	* \code
	* PluginXCallS("saveData","300")
	* \endcode
	* \note tables can be serialized to a string and reconstituted in the plugin using loadstring(...) but the performance may suffer if the tables are large. See PluginXCallB for a slightly faster method of communication that doesn't involve the heavy java string manipulation.
	*/
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
			Message msg = mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_PLUGINXCALLS,foo.getString());
			
			msg.getData().putString("PLUGIN",mOwner);
			msg.getData().putString("FUNCTION", function);
			
			mMainWindowHandler.sendMessage(msg);
			// TODO Auto-generated method stub
			return 0;
		}		
		
	}
	

	
  /*! \page page1
	* \subsection sec12 PopMenuStack
	* Removes the current menu item and returns the menu stack to its previous state.
	* 
	* \par Full Signature
	* \code
	* PopMenuStack()
	* \endcode
	* \param none
	* \returns nothing
	* \par Example 
	* \code
	* PopMenuStack()
	* \endcode
	* \see PushMenuStack
	*/
	private class PopMenuStackFunction extends JavaFunction {

		public PopMenuStackFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_POPMENUSTACK));
			return 0;
		}
		
	}
		
 /*! \page page1
	* \subsection sec11 PushMenuStack
	* Starts a new menu object, providing a global function name to call that will populate the menu
	* 
	* \par Full Signature
	* \code
	* PushMenuStack(callbackName)
	* \endcode
	* \param \c string the name of a global function to call in order to populate the new menu item.
	* \returns nothing
	* \par Example 
	* \code
	* function addMenu(menu)
	*  menu:addItem(0,0,0,foo)
	* end
	* PushMenuStack("addMenu")
	* \endcode
	* \see this relies largely on the android Menu and MenuItem classes, please refer to the documentation and other menu related sample code.
	*/
	private class PushMenuStackFunction extends JavaFunction {

		public PushMenuStackFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			//LuaObject func = this.getParam(2);
			//L.isFunction(-1);
			/*if(!L.isFunction(-1)) {
				this.L.pushString("Argument must be a function call back to be called on back button press.");
				this.L.error();
				return 0;
			}*/
			//this.L.LcheckString(2);
			
			String function = this.getParam(2).getString();
			
			//Log.e("PUSHMENUSTACK","FUNCTION NAME:"+function);
			
			Message m = mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_PUSHMENUSTACK,Window.this.mName);
			m.getData().putString("CALLBACK", function);
			
			mMainWindowHandler.sendMessage(m);
			return 0;
		}
		
	}
	

	private class SendToServerFunction extends JavaFunction {

		public SendToServerFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			if(this.getParam(2).isNil()) { return 0; }
			//Log.e("LUAWINDOW","script is sending:"+this.getParam(2).getString()+" to server.");
			mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_SENDBUTTONDATA,this.getParam(2).getString()));
			return 0;
		}
		
	}
		
  /*! \page page1
	* \subsection sec3 ScheduleCallback
	* Add a top level menu item that will call a global function when pressed.
	* 
	* \par Full Signature
	* \code
	* ScheduleCallback(id,callbackName,delayMillis)
	* \endcode
	* \param id \c number unique identifier associated with this event, will be passed to the callback.
	* \param callbackName \c string name of the global function to call after the desired elapsed time.
	* \param delayMillis \c dumber how long in milliseconds to delay the execution of the callback
	* \returns nothing
	* \par Example
	* \code
	* function delayCallback(id)
	*  Note(string.format("event %d fired.",id))
	* end
	* 
	* ScheduleCallback(100,"delayCallback",3000)
	* ScheduleCallback(104,"delayCallback",5000)
	* \endcode
	* \tableofcontents
	*/
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
	

	
	private Handler callbackHandler = new Handler() {
		public void handleMessage(Message msg) {
			//
			//just call the string.
			Window.this.callScheduleCallback(msg.arg1,(String)msg.obj);
			
			
		}
	};
	
	
	
	private void callScheduleCallback(int id,String callback) {
		if(mL == null) return;
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
		mL.getGlobal(callback);
		if(mL.getLuaObject(-1).isFunction()) {
			//prepare to call.
			mL.pushString(Integer.toString(id));
			int ret = mL.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("Scheduled callback("+callback+") error:"+mL.getLuaObject(-1).toString());
			} else {
				mL.pop(2);
			}
		} else {
			//error no function.
			mL.pop(2);
		}
	}
	
	public void callFunction(String callback, String data) {
		mL.getGlobal("debug");
		mL.getField(mL.getTop(), "traceback");
		mL.remove(-2);
		
		mL.getGlobal(callback);
		if(mL.isFunction(mL.getTop())) {
			if(data != null) {
				mL.pushString(data);
			} else {
				mL.pushNil();
			}
			int tmp = mL.pcall(1, 1, -3);
			if(tmp != 0) {
				displayLuaError("Error calling window script function "+callback+": "+mL.getLuaObject(-1).getString());
			} else {
				mL.pop(1);
			}
		} else {
			mL.pop(1);
		}
	}
	
	private class PluginInstalledFunction extends JavaFunction {

		public PluginInstalledFunction(LuaState L) {
			super(L);
			
		}

		@Override
		public int execute() throws LuaException, RemoteException {
			String desired = this.getParam(2).getString();
			boolean result = mParent.isPluginInstalled(desired);
			//parent.isPluginInstalled();
			L.pushBoolean(result);
			return 1;
		}
		
	}
	
	private class WindowSupportsFunction extends JavaFunction {

		public WindowSupportsFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException, RemoteException {
			String desired = this.getParam(2).getString();
			String function = this.getParam(2).getString();
			boolean ret = mParent.checkWindowSupports(desired,function);
			L.pushBoolean(ret);
			return 1;
		}
		
	}
	
	private class WindowBroadcastFunction extends JavaFunction {

		public WindowBroadcastFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException, RemoteException {
			String function = this.getParam(2).getString();
			String data = this.getParam(3).getString();
			mParent.windowBroadcast(function, data);
			
			return 0;
		}
		
	}
	
	private class WindowCallFunction extends JavaFunction {

		public WindowCallFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException, RemoteException {
			String window = this.getParam(2).getString();
			String function = this.getParam(3).getString();
			String data = this.getParam(4).getString();
			
			mParent.windowCall(window,function,data);
			return 0;
		}
		
	}
	
	/*public void callFunction(String callback,Object o) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		
		if(L.isFunction(L.getTop())) {
			L.pushJavaObject(o);
			int tmp = L.pcall(1, 1, -3);
			if(tmp != 0) {
				displayLuaError("Error calling script callback: "+L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
	}*/



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
		callFunction("onParentAnimationEnd",null);
	}
	
	@Override
	public void onAnimationEnd() {
		//call into lua to notify that the parent animation has completed.
		callFunction("onAnimationEnd",null);
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
				setCharacterSizes((Integer)o.getValue(),mPrefLineExtra);
				break;
			case line_extra:
				setCharacterSizes(mPrefFontSize,(Integer)o.getValue());
				break;
			case buffer_size:
				mBuffer.setMaxLines((Integer)o.getValue());
				Message msg = mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_WINDOWBUFFERMAXCHANGED);
				msg.arg1 = (Integer)o.getValue();
				msg.getData().putString("PLUGIN", this.mOwner);
				msg.getData().putString("WINDOW", mName);
				mMainWindowHandler.sendMessage(msg);
				break;
			case font_path:
				mPrefFont = loadFontFromName((String)o.getValue());
				p.setTypeface(mPrefFont);
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
			
			float xform_to_line = y / (float)mPrefLineSize;
			int line = (int)Math.floor(xform_to_line);
			
			float xform_to_column = x / (float)mOneCharWidth;
			int column = (int)Math.floor(xform_to_column);
			
			switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//if(firstPress) {

					
				//} else {
					if(Math.abs(theSelection.start.line - line) < 2 && Math.abs(theSelection.start.column - column) < 2) {
						selectedSelector = theSelection.start;
						selectionFingerDown = true;
						moveWidgetToSelector(selectedSelector);
						//Log.e("window","moving start selector");
						v.invalidate();
					} else if(Math.abs(theSelection.end.line - line) < 2 && Math.abs(theSelection.end.column - column) < 2) {
						selectedSelector = theSelection.end;
						moveWidgetToSelector(selectedSelector);
						selectionFingerDown = true;
						//Log.e("window","moving end selector");
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
									int remainder = ((int)(scrollback-SCROLL_MIN) % mPrefLineSize)-mPrefLineSize;
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
					//Log.e("window","moving selector-> line:"+line+" col:"+column);
					
					widgetCenterMovedX += (x - widgetCenterMoveXLast);
					widgetCenterMovedY -= (event.getY() - widgetCenterMoveYLast);
					widgetCenterMoveXLast = (int) x;
					widgetCenterMoveYLast = (int) event.getY();
					if(Math.abs(widgetCenterMovedX) > mOneCharWidth) {
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
						widgetCenterMovedX = 0;
						v.invalidate();
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

						widgetCenterMovedY = 0;
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
								doScrollUp(false);

								break;
							case DOWN:
								doScrollDown(false);
								break;
							case NEXT:
								String copy = mBuffer.getTextSection(theSelection);
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
							case LEFT:
								doScrollLeft(false);
								break;
							case RIGHT:
								doScrollRight(false);
								break;
							case CENTER:
								break;
							case COPY:
								//actually switch.
								if(selectedSelector == theSelection.end) {
									selectedSelector = theSelection.start;
								} else {
									selectedSelector = theSelection.end;
								}
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
			selectionIndicatorVectorX -= mOneCharWidth;
			if(selectionIndicatorVectorX < (mOneCharWidth + selectionIndicatorHalfDimension)) {
				selectionIndicatorVectorX = -(selectionIndicatorVectorX+mOneCharWidth);
			}
			newWidgetX = (int) (startX + selectionIndicatorVectorX);
			
		} else if((newWidgetX - selectionIndicatorHalfDimension) < 0) {
			//flip the vector
			selectionIndicatorVectorX += mOneCharWidth;
			if(selectionIndicatorVectorX > -(mOneCharWidth+selectionIndicatorHalfDimension)) {
				selectionIndicatorVectorX = -(selectionIndicatorVectorX-mOneCharWidth);
			}
			newWidgetX = (int) (startX + selectionIndicatorVectorX);
		}
		
		if((newWidgetY + (selectionIndicatorHalfDimension)) > this.getHeight()) {
			selectionIndicatorVectorY -= mPrefLineSize;
			//only if we run into 
			newWidgetY = (int) (startY + selectionIndicatorVectorY);
			if(newWidgetY + selectionIndicatorHalfDimension > this.getHeight()) {
				selectionIndicatorVectorY = -selectionIndicatorHalfDimension;
				newWidgetY = (int) (startY + selectionIndicatorVectorY);
			}
			
		} else if ((newWidgetY - selectionIndicatorHalfDimension) < 0) {
			selectionIndicatorVectorY += mPrefLineSize;
			
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
		
		int part1 = (int) (selectedSelector.line * mPrefLineSize + (0.5*SELECTIONINDICATOR_FONTSIZE));
		//int part2 = (int) (scrollback);
		int part2 = (int) (selectedSelector.line * mPrefLineSize - (0.5*SELECTIONINDICATOR_FONTSIZE));
		
		
		if(part1 > scrollback) {
			//calculate how much scroll to go to get to be true.
			//int tmp = (int) (scrollback-SCROLL_MIN);
			//int howmuch = part2 - part1;
			scrollback = (double) part1;
		} else if(part2 < (scrollback-SCROLL_MIN)) {
			scrollback -= ((scrollback-SCROLL_MIN) - part2);
		}
		
		int endx = (int) ((selectedSelector.column * mOneCharWidth) + (0.5*mOneCharWidth));
		int endy = (int) ((this.getHeight() - ((selectedSelector.line * mPrefLineSize) + (0.5*SELECTIONINDICATOR_FONTSIZE) - (scrollback-SCROLL_MIN))));
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
	private boolean scrollingEnabled = true;

/*! \page entry_points
 * \subsection OnDestroy OnDestroy
 * When the foreground process is being terimnated normally, this function will be called and it is appropriate to put memory management stuff here (freeing custom bitmaps, data, stuff that needs to be garbage collected).
 * 
 * \param none
 * 
 * \note It is difficult to know exactly what needs to be freed for garbage collection, how to do it, and weather or not it worked. A good example is the button window, it has many custom resources and I had run into memory issues with it when closing/opening the window a few times. It may never happen, it may happen after 100 open/close cycles, or 5, but the general trend of running the foreground process out of memory is an immediate termination of the window. So if you are in a case where you are coming back into the appliation after a phone call or web browser and it immediatly exits, this may be the culprit.
 */
		
	public void shutdown() {
		//Log.e("LUAWINDOW","SHUTTING DOWN: "+mName);
		if(mL == null) return;
		//call into lua to notify shutdown imminent.
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
		mL.getGlobal("OnDestroy");
		if(mL.getLuaObject(mL.getTop()).isFunction()) {
			int ret = mL.pcall(0, 1, -2);
			if(ret != 0) {
				displayLuaError("Error in OnDestroy: "+mL.getLuaObject(-1).getString());
			} else {
				mL.pop(2);
			}
		} else {
			//no method.
			mL.pop(2);
		}
		
		//callbackHandler.removeCallbacksAndMessages(token)
		
		mL.close();
		mL = null;
		
	}
	
	private void doScrollDown(boolean repeat) {
		//Log.e("FOO","do scroll down");
		selectedSelector.line -= 1;
		if(selectedSelector.line < 0) {
			selectedSelector.line = 0;
			repeat = false;
		} else {
			int remainder = ((int)(scrollback-SCROLL_MIN) % mPrefLineSize) + mPrefLineSize;
			selectorCenterY += mPrefLineSize;
			if(selectorCenterY > this.getHeight() - remainder) {
				selectorCenterY -= mPrefLineSize;
				scrollback -= mPrefLineSize;
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
		//Log.e("FOO","do scroll up");
		selectedSelector.line += 1;
		if(selectedSelector.line == mBuffer.getBrokenLineCount()) {
			selectedSelector.line -= 1;
			repeat = false;
		} else {
			int remainder = ((int)(scrollback-SCROLL_MIN) % mPrefLineSize)-mPrefLineSize;
			selectorCenterY -= mPrefLineSize;
			if(selectorCenterY - (mPrefLineSize) < remainder) {
				selectorCenterY = selectorCenterY + mPrefLineSize;
				scrollback += mPrefLineSize;
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
			selectorCenterX -= mOneCharWidth;
			widgetY -= mOneCharWidth;
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
		selectorCenterX += mOneCharWidth;
		calculateWidgetPosition(selectorCenterX,selectorCenterY);
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLRIGHT, scrollRepeatRate);
		}else {
			mHandler.removeMessages(MESSAGE_SCROLLRIGHT);
		}
	}
	
	public boolean isTextSelectionEnabled() {
		return mTextSelectionEnabled;
	}



	public void setTextSelectionEnabled(boolean textSelectionEnabled) {
		this.mTextSelectionEnabled = textSelectionEnabled;
		//Log.e("sfdsf","setting text selection enabled="+textSelectionEnabled);
	}
	
	public void setScrollingEnabled(boolean scrollingEnabled) {
		this.scrollingEnabled  = scrollingEnabled;
	}
	
	public boolean isScrollingEnabled() {
		return this.scrollingEnabled;
	}
	
	public void jumpToStart() {
		scrollback = SCROLL_MIN;
		mFlingVelocity=0;
		this.invalidate();
	}



	
	public void populateMenu(Menu menu) {
		if(mL == null) return;
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
		mL.getGlobal("PopulateMenu");
		if(mL.getLuaObject(-1).isFunction()) {
			mL.pushJavaObject(menu);
			int ret = mL.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("Error in PopulateMenu:"+mL.getLuaObject(-1).getString());
			} else {
				mL.pop(2);
			}
		} else {
			mL.pop(2);
		}
	}

	public void setBuffer(TextTree buffer) {
		// TODO Auto-generated method stub
		this.mBuffer = buffer;
		
	}

	public boolean checkSupports(String function) {
		if(mL != null) {
			mL.getGlobal(function);
			
			boolean ret = mL.isFunction(-1);
			mL.pop(1);
			return ret;
		}
		return false;
	}

	public boolean isCenterJustify() {
		return mCenterJustify;
	}

	public void setCenterJustify(boolean centerJustify) {
		this.mCenterJustify = centerJustify;
	}
	
	public int getLineSize() {
		return mPrefLineSize;
	}
	
	public void fitFontSize(int chars) {
		//Log.e("LUA","SETTING FITCHARS:"+chars);
		mFitChars = chars;
	}
		
	public void doFitFontSize(int width) {
		if(mFitChars < 0) return;
		//Log.e("LUA","DOING THE FIT ROUTINE: "+mWidth+" chars:"+fitChars + " for window: "+this.getName());
		int windowWidth = width;
		//int windowWidth = service.getResources().getDisplayMetrics().widthPixels;
		//if(service.getResources().getDisplayMetrics().heightPixels > windowWidth) {
		//	windowWidth = service.getResources().getDisplayMetrics().heightPixels;
		//}
		float fontSize = 8.0f;
		float delta = 1.0f;
		Paint p = new Paint();
		p.setTextSize(8.0f);
		//p.setTypeface(Typeface.createFromFile(service.getFontName()));
		p.setTypeface(Typeface.MONOSPACE);
		boolean done = false;
		
		float charWidth = p.measureText("A");
		float charsPerLine = windowWidth / charWidth;
		
		if(charsPerLine < mFitChars) {
			//for QVGA screens, this test will always fail on the first step.
			done = true;
		} else {
			fontSize += delta;
			p.setTextSize(fontSize);
		}
		
		while(!done) {
			charWidth = p.measureText("A");
			charsPerLine = windowWidth / charWidth;
			if(charsPerLine < mFitChars) {
				done = true;
				fontSize -= delta; //return to the previous font size that produced > 80 characters.
			} else {
				fontSize += delta;
				p.setTextSize(fontSize);
			}
		}
		
		mPrefFontSize = (int) fontSize;
		mPrefLineSize = mPrefFontSize + mPrefLineExtra;
		calculateCharacterFeatures(mWidth,mHeight);
		//return (int)fontSize;
	}

	public TextTree getBuffer() {
		return mBuffer;
	}
	
	public double measure(String str) {
		return featurePaint.measureText(str);
	}
}

