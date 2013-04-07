package com.offsetnull.bt.window;


import java.io.File;
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

import com.offsetnull.bt.service.IWindowCallback;
import com.offsetnull.bt.service.plugin.settings.BaseOption;
import com.offsetnull.bt.service.plugin.settings.BooleanOption;
import com.offsetnull.bt.service.plugin.settings.ColorOption;
import com.offsetnull.bt.service.plugin.settings.FileOption;
import com.offsetnull.bt.service.plugin.settings.IntegerOption;
import com.offsetnull.bt.service.plugin.settings.ListOption;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;


import android.R;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
//import android.util.Log;
//import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
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

import com.offsetnull.bt.service.Colorizer;
import com.offsetnull.bt.service.SettingsChangedListener;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.settings.HyperSettings.LINK_MODE;
import com.offsetnull.bt.window.TextTree.Line;
import com.offsetnull.bt.window.TextTree.Selection;
import com.offsetnull.bt.window.TextTree.SelectionCursor;
import com.offsetnull.bt.window.TextTree.Unit;


/*! \brief Window
 *
 *  The Window.java class is the programmable mini-window that also houses the ansi drawing routine.
 */

public class Window extends View implements AnimatedRelativeLayout.OnAnimationEndListener,SettingsChangedListener {

	/*! \brief test
	 * 
	 *  Seriously how does this work.
	 */
	private MainWindowCallback parent = null; /*!< for calling back into the Activity.*/
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
	int one_char_is_this_wide = 1; /*!< gah! how does this work*/
	private float density;
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
	
	boolean increadedPriority = false;
	boolean hasText = true;
	private int fitChars = -1;
	private boolean bufferText = false;
	private View new_text_in_buffer_indicator = null;
	
	private boolean centerJustify = false;
	private boolean hasScriptOnMeasure = false;
	

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
	protected static final int MESSAGE_XCALLB = 13;
	//public static final int MESSAGE_SENDDATA = 0;
	protected static final int MESSAGE_RESETWITHDATA = 14;
	
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
	int selectionIndicatorHalfDimension = 60;
	
	Path selectionIndicatorClipPath = new Path();
	
	Rect selectionIndicatorLeftButtonRect = new Rect();
	Rect selectionIndicatorRightButtonRect = new Rect();
	Rect selectionIndicatorUpButtonRect = new Rect();
	Rect selectionIndicatorDownButtonRect = new Rect();
	Rect selectionIndicatorCenterButtonRect = new Rect();
	
	Rect selectionIndicatorRect = new Rect();
	
	public void displayLuaError(String message) {
		mainHandler.sendMessage(mainHandler.obtainMessage(MainWindow.MESSAGE_DISPLAYLUAERROR,"\n" + Colorizer.getRedColor() + message + Colorizer.getWhiteColor() + "\n"));
	}
	
	public Window(String dataDir,Context context,String name,String owner,Handler mainWindowHandler,SettingsGroup settings,MainWindowCallback activity) {
		super(context);
		this.parent = activity;
		init(dataDir,name,owner,mainWindowHandler,settings);
	}
	
	
	
	public void onCreate(Bundle b) {
		fitFontSize(-1);
		//onSizeChanged(this.getWidth(),this.getHeight(),0,0);
		//viewCreate();
		
		//updateHandler = new ThreadUpdater(the_tree,mHandler,updateSynch);
		//updateHandler.start();
	}
	
	protected void onAttachedToWindow() {
		//Log.e("WINDOW","Attatched to window.");
		viewCreate();
	}
	
	protected void onDetachedFromWindow() {
		//Log.e("WINDOW","Detached from window.");
		viewDestroy();
	}
	
	protected void onMeasure(int widthSpec,int heightSpec) {
		int height = MeasureSpec.getSize(heightSpec);
		int width = MeasureSpec.getSize(widthSpec);
		
		if(hasScriptOnMeasure && L != null) {
			//Log.e("MEASURE","USING THE SCRIPT ON MEASURE");
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			
			L.getGlobal("OnMeasure");
			if(L.isFunction(-1)) {
				L.pushNumber(widthSpec);
				L.pushNumber(heightSpec);
				int ret = L.pcall(2,2,-4);
				if(ret !=0) {
					displayLuaError("Error in OnMeasure:" + L.getLuaObject(-1).getString());
					setMeasuredDimension(1,1);
					
					L.pop(1);
					return;
				} else {
					//get the return values.
					int ret_height = (int) L.getLuaObject(-1).getNumber();
					int ret_width = (int) L.getLuaObject(-2).getNumber();
					L.pop(2);
					//Log.e("MEASURE","CUSTOM MEASURE CODE RETURNED:"+ret_width+"|"+ret_height);
					setMeasuredDimension(ret_width,ret_height);
					//if(width != mWidth) {
					//	doFitFontSize(width);
					//}
					return;
				}
			} else {
				L.pop(2);
			}
		}
		//if(height == mHeight && width == mWidth) { setMeasuredDimension(width,height); return; }
		
		int hspec = MeasureSpec.getMode(heightSpec);
		//int wspec = MeasureSpec.getMode(widthSpec);
		//mWidth = width;
		//mHeight = height;
		if(width != mWidth) {
			doFitFontSize(width);
		}
		//String str = "";
		switch(hspec) {
		case MeasureSpec.AT_MOST:
			//str = "AT_MOST";
			break;
		case MeasureSpec.EXACTLY:
			//str = "EXACTLY";
			break;
		case MeasureSpec.UNSPECIFIED:
			//str = "UNSPECIFIED";
			height = (the_tree.getBrokenLineCount()*PREF_LINESIZE) + PREF_LINEEXTRA;
			break;
		}
		
		//Log.e("MEASURE","MEASURING WINDOW ("+this.getName()+"): height spec:"+str + " using val: " + height + " tree has: " + the_tree.getBrokenLineCount());
		
		setMeasuredDimension(width,height);
		
		//if(sizeChanged) {
		//calculateCharacterFeatures(mWidth,mHeight);
			//sizeChanged = false;
		//}	
		//doDelayedDraw(0);
	}
	
	private String dataDir = null;
	
	private void init(String dataDir,String name,String owner,Handler mainWindowHandler,SettingsGroup settings) {
		this.dataDir = dataDir;
		this.density = this.getContext().getResources().getDisplayMetrics().density;
		if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			selectionIndicatorHalfDimension = (int) (90*density);
		} else {
			selectionIndicatorHalfDimension = (int) (60*density);
		}
		
		selectionIndicatorClipPath.addCircle(selectionIndicatorHalfDimension,selectionIndicatorHalfDimension,selectionIndicatorHalfDimension-10,Path.Direction.CCW);
		homeWidgetDrawable = BitmapFactory.decodeResource(this.getContext().getResources(),com.offsetnull.bt.R.drawable.homewidget);
		textSelectionCancelBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.cancel_tiny);
		textSelectionCopyBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.copy_tiny);
		textSelectionSwapBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.swap);
		
		textSelectionIndicatorPaint.setStyle(Paint.Style.STROKE);
		textSelectionIndicatorPaint.setStrokeWidth(1*density);
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
				case MESSAGE_RESETWITHDATA:
					Window.this.resetAndAddText((byte[])msg.obj);
					break;
				case MESSAGE_SCROLLLEFT:
					//Log.e("window","handler scrollleft");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollLeft(true);
					break;
				case MESSAGE_SCROLLRIGHT:
					//Log.e("window","handler scrollright");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollRight(true);
					break;
				case MESSAGE_SCROLLDOWN:
					//Log.e("window","handler scrolldown");
					scrollRepeatRate -= (scrollRepeatRateStep++)*5; if(scrollRepeatRate < scrollRepeatRateMin) { scrollRepeatRate = scrollRepeatRateMin; }
					Window.this.doScrollDown(true);
					break;
				case MESSAGE_SCROLLUP:
					//Log.e("window","handler scrollup");
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
					//Log.e("clear","clearing buffer");
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
				case MESSAGE_XCALLB:
					//try {
					try {
						Window.this.xcallB(msg.getData().getString("FUNCTION"),(byte[])msg.obj);
					} catch (LuaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					//}
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
		
		PREF_FONT = loadFontFromName((String)fontpath.getValue());
		p.setTypeface(PREF_FONT);
		
		the_tree.setMaxLines((Integer)buffersize.getValue());
		
		PREF_LINEEXTRA = (Integer)lineextra.getValue();
		PREF_FONTSIZE = (Integer)fontsize.getValue();
		setCharacterSizes(PREF_FONTSIZE,PREF_LINEEXTRA);
		
		//Log.e("WINDOW","WINDOW COLOR VALUE("+this.getName()+"): " + (Integer)colorOption.getValue());
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
		the_tree.empty();
		buffer.empty();
		addBytes(obj,true);
	}

	protected void xcallB(String string,byte[] bytes) throws LuaException {
		//Log.e("WINDOW","xcallB on "+mName+" calling " + string + " id: "+this.toString());
		if(L == null) { return;}
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal(string);
		if(L.getLuaObject(-1).isFunction()) {
			L.pushObjectValue(bytes);
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("WindowXCallB calling: " + string + " error:"+L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
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
	int pointer = -1;
	@Override
	public boolean onTouchEvent(MotionEvent t) {
		//if(fuckyou) {
		//switch(t.getActionMasked()) {
		
		//}
		//super.onTouchEvent(t);	
		
			//return true;
		//}
		//long now = System.currentTimeMillis();
		int action = t.getAction() & MotionEvent.ACTION_MASK;     
		int pointerIndex = (t.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerId = t.getPointerId(pointerIndex);
		//if(now < target) {
		
		if(pointer > 0 && pointerId != pointer) {
			//but invalidate this anyway
			this.invalidate();
			return false;
		}
			//normal
		if(!scrollingEnabled) {
			return false;
		}
		//	return true;
		//}
		//target = now + 1000;
		//Log.e("WINDOW",mName + "onTouchEvent");
		boolean retval = false;
		boolean noFunction = false;
		//L.getGlobal("debug");
		//L.getField(L.getTop(), "traceback");
		//L.remove(-2);
		int index = t.findPointerIndex(pointerId);
		start_x = new Float(t.getX(index));
		start_x = start_x+1;
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
				pointer = pointerId;
				start_x = new Float(t.getX(index));
				start_y = new Float(t.getY(index));
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
				float x = t.getX(index);
				float y = t.getY(index);
				
				//convert y to be at the bottom of the screen.
				
				y = (float) ((float)this.getHeight() - y + (scrollback-SCROLL_MIN));
				
				float xform_to_line = y / (float)PREF_LINESIZE;
				int line = (int)Math.floor(xform_to_line);
				
				float xform_to_column = x / (float)one_char_is_this_wide;
				int column = (int)Math.floor(xform_to_column);
				if(textSelectionEnabled) {
					//Log.e("sfdsf","starting text selection");
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_STARTSELECTION, line, column), 1500);
				} else {
					//Log.e("sfdsf","not starting text selection");
				}
				
				if(homeWidgetShowing) {
					if(homeWidgetRect.contains((int)x,(int)t.getY())) {
						homeWidgetFingerDown = true;
					}
				}
				
				//Log.e("FLUF","IN ON DOWN: "+scrollback);
			}
			
			if(!increadedPriority) {
				increadedPriority = true;
			}
			
			if(t.getAction() == MotionEvent.ACTION_MOVE) {
				
	
				//Float now_x = new Float(t.getX(t.getPointerId(0)));
				Float now_y = new Float(t.getY(index));
				
				
	
				float thentime = pre_event.getEventTime();
				float nowtime = t.getEventTime();
				
				float time = (nowtime - thentime) / 1000.0f; //convert to seconds
				
				float prev_y = pre_event.getY(index);
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
				
				if(Math.abs(now_y - start_y) > PREF_LINESIZE*1.5*density) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
					//homeWidgetFingerDown = false;
					
				}
				
				if(Math.abs(diff_amount) > 5*density) {
					
					pre_event = MotionEvent.obtainNoHistory(t);
				}
				
				//Log.e("FLUF","IN ON MOVE: "+scrollback);
	
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
				//mHandler.removeMessages(Window.MSG_BUTTONDROPSTART);
		        
		        //reset the priority
		        increadedPriority = false;
		        //_runner.dcbPriority(Process.THREAD_PRIORITY_DEFAULT);
		        pointer = -1;
	
		        pre_event = null;
		        finger_down=false;
		        finger_down_to_up = true;
		         
				if(touchInLink > -1) {
					mainHandler.sendMessage(mainHandler.obtainMessage(MainWindow.MESSAGE_LAUNCHURL, linkBoxes.get(touchInLink).getData()));
			        touchInLink = -1;
				}
				
				//Log.e("FLUF","IN ON UP: "+scrollback);
				
				//if(Math.abs(now_y - start_y) > PREF_LINESIZE*1.5) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
				//}
					
				if(homeWidgetShowing && homeWidgetFingerDown) {
					if(homeWidgetRect.contains((int)t.getX(index),(int)t.getY(index))) {
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
				if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) { scrollback = SCROLL_MIN; return;}
				if(finger_down) {
					scrollback = (double)Math.floor(scrollback + diff_amount);
					if(scrollback < SCROLL_MIN) {
						scrollback = SCROLL_MIN;
						//Log.e("FLUF","1: "+scrollback);
					} else {
						if(scrollback >= ((the_tree.getBrokenLineCount() * PREF_LINESIZE))) {
							
							scrollback = (double)((the_tree.getBrokenLineCount() * PREF_LINESIZE));
							//Log.e("FLUF","2: "+scrollback);
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
						//Log.e("FLUF","3: "+scrollback);
					} else if (fling_velocity > 0) {
						
						fling_velocity = fling_velocity - fling_accel*duration_since_last_frame;
						scrollback =  (scrollback + fling_velocity*duration_since_last_frame);
						//Log.e("FLUF","4: "+scrollback);
					}
					
					if(Math.abs(new Double(fling_velocity)) < 15) {
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						mHandler.sendEmptyMessage(Window.MSG_NORMALPRIORITY);
					}
						
					if(scrollback <= SCROLL_MIN) {
						scrollback = SCROLL_MIN;
						//Log.e("FLUF","5: "+scrollback);
						fling_velocity = 0;
						prev_draw_time = 0;
						Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
						mHandler.sendEmptyMessage(Window.MSG_NORMALPRIORITY);

						//mHandler.sendEmptyMessage(Window.MSG_CLEAR_NEW_TEXT_INDICATOR);
					}
					
					if(scrollback >= ((the_tree.getBrokenLineCount() * PREF_LINESIZE))) {
						//Log.e("WINDOW","UPPER CAP OF THE BUFFER REACHED!");
						scrollback = (double)((the_tree.getBrokenLineCount() * PREF_LINESIZE));
						//Log.e("FLUF","6: "+scrollback);
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
		
		//if(mName != null && mName.equals("map_window")) {
		//	long xtmp = 10;
		//	xtmp += System.currentTimeMillis();
		//}
		
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
//			if(mName.equals("mainDisplay")) {
//				Log.e("WINDOW","starting drawing iterator at:"+postmp);
//			}
			//try {
			while(!stop && screenIt.hasPrevious()) {
				//int index = screenIt.previousIndex();
				//boolean started = false;
				int loc = screenIt.previousIndex();
				Line l = screenIt.previous();
				//String tmpstr = TextTree.deColorLine(l).toString();
				/*if(mName.equals("map_window")) {
					Log.e("map","map window line: "+tmpstr+"|"+l.viswidth+" calc:"+CALCULATED_ROWSINWINDOW);
				}*/
				
//				if(mName.equals("mainDisplay")) {
//					String str = TextTree.deColorLine(l).toString();
//					Log.e("WINDOW",drawnlines+":"+loc+":"+y+":"+str);
//				}
				
				if(centerJustify) {
					//center justify.

					int amount = one_char_is_this_wide*l.charcount;
					x = (float) ((mWidth/2.0)-(amount/2.0));
				}
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
					//Log.e("window","doing linemode 4 for line:"+workingline + " ypos:"+y);
					linemode = 4;
					
				}
				
//				if(mName.equals("mainDisplay")) {
//					if(!unitIterator.hasNext()) {
//						Log.e("WINDOW","HEY, THIS LINE HAS NO UNITS");
//					} else {
//						Log.e("WINDOW","Line has:"+l.getData().size() + " units.");
//						while(unitIterator.hasNext()) {
//							Unit u = unitIterator.next();
//							Log.e("WINDOW","UNIT TYPE:"+u.getClass().toString());
//						}
//						
//						while(unitIterator.hasPrevious()) {
//							unitIterator.previous();
//						}
//						
//					}
//				}
				boolean finishedWithNewLine = false;
				
				while(unitIterator.hasNext()) {
					Unit u = unitIterator.next();
					//p.setColor(color)
					boolean useBackground = false;
					if(b.getColor() != 0xFF0A0A0A && b.getColor() != 0xFF000000) {
						useBackground = true;
					}
					
					switch(u.type) {
					//if(u instanceof TextTree.Text && !(u instanceof TextTree.WhiteSpace)) {
					//if(u instanceof TextTree.Text) {
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
							
							//Log.e("Window","doing selection run: start={"+startline2+","+startcol+"} end={"+endline+","+endcol);
							switch(linemode) {
							case 1:
								int finishCol = workingcol + text.bytecount;
								if(finishCol > startcol && finishCol-1 <= endcol){
									if((finishCol - startcol) < text.bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * one_char_is_this_wide;
										int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize()+(3*density), x + stringWidth, y+(4*density), textSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize()+(2*density), x + p.measureText(text.getString()), y+(4*density), textSelectionIndicatorBackgroundPaint);
									}
								} else if(finishCol > endcol) {
									if((finishCol - endcol) < text.bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * one_char_is_this_wide;
										//int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x, y - p.getTextSize()+(2*density), x + overshootPixels, y+(4*density), textSelectionIndicatorBackgroundPaint);
									} else {
										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
									}
								} else {
									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
								}
								break;
							case 2:
								finishCol = workingcol + text.bytecount;
								if(finishCol > startcol) {
									if((finishCol - startcol) < text.bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * one_char_is_this_wide;
										int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize()+(2*density), x + stringWidth, y+(4*density), textSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize()+(2*density), x + p.measureText(text.getString()), y+(4*density), textSelectionIndicatorBackgroundPaint);
									}
								} else {
									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
								}
//								} else if(finishCol > endcol) {
//									if((finishCol - endcol) < text.bytecount) {
//										int overshoot = endcol - workingcol + 1;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//										//int stringWidth = (int) p.measureText(text.getString());
//										c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
//									} else {
//										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//									}
//								} else {
//									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//								}
								break;
							case 3:
								
								c.drawRect(x, y - p.getTextSize()+(2*density), x + p.measureText(text.getString()), y+(4*density), textSelectionIndicatorBackgroundPaint);
								break;
							case 4:
								finishCol = workingcol + text.bytecount;
//								if(finishCol > startcol && finishCol-1 <= endcol){
//									if((finishCol - startcol) < text.bytecount) {
//										int overshoot = startcol - workingcol;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//									int stringWidth = (int) p.measureText(text.getString());
//										c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, scroller_paint);
//									} else {
//										c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//									}
								//Log.e("ACEFJSAf","x:"+x+" y:"+y +" text:"+text.getString()+"|" + " finishCol="+finishCol+" workingCol="+workingcol + " endcol="+endcol);
								if(finishCol >= endcol) {
									if((finishCol - endcol) < text.bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * one_char_is_this_wide;
										//int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x, y - p.getTextSize()+(2*density), x + overshootPixels, y+(4*density), scroller_paint);
									} else {
										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
									}
								} else {
									c.drawRect(x, y - p.getTextSize()+(2*density), x + p.measureText(text.getString()), y+(4*density), textSelectionIndicatorBackgroundPaint);
								}
								break;
							default:
								break;
							}
						}
							
//							if(startline2 == endline && startline2 == workingline) {
//								int finishCol = workingcol + text.bytecount;
//								if(finishCol > startcol && finishCol-1 <= endcol){
//									if((finishCol - startcol) < text.bytecount) {
//										int overshoot = startcol - workingcol;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//										int stringWidth = (int) p.measureText(text.getString());
//										c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, scroller_paint);
//									} else {
//										c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//									}
//								} else if(finishCol > endcol) {
//									if((finishCol - endcol) < text.bytecount) {
//										int overshoot = endcol - workingcol + 1;
//										int overshootPixels = overshoot * one_char_is_this_wide;
//										//int stringWidth = (int) p.measureText(text.getString());
//										c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
//									} else {
//										//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//									}
//								} else {
//									//c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//								}
//							} else if(startline2 == workingline) {
//								int finishCol = workingcol + text.bytecount;
//								if((finishCol - startcol) < text.bytecount) {
//									int overshoot = startcol - workingcol + 1;
//									int overshootPixels = overshoot * one_char_is_this_wide;
//									//int stringWidth = (int) p.measureText(text.getString());
//									c.drawRect(x, y - p.getTextSize(), x + overshootPixels, y+5, scroller_paint);
//								} else {
//									c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//								}
//							} else if(startline2 > workingline && endline < workingline) {
//								c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//								Log.e("wondow","drawing in between line:" +workingline+ " ypos:"+y);
//							} else if(endline == workingline) {
//								int finishCol = workingcol + text.bytecount;
//								if((finishCol - endcol) < text.bytecount) {
//									int overshoot = endcol - workingcol;
//									int overshootPixels = overshoot * one_char_is_this_wide;
//									int stringWidth = (int) p.measureText(text.getString());
//									c.drawRect(x + overshootPixels, y - p.getTextSize(), x + stringWidth, y+5, scroller_paint);
//								} else {
//									c.drawRect(x, y - p.getTextSize(), x + p.measureText(text.getString()), y+5, scroller_paint);
//								}
//							}
//						}
						
						if(useBackground) {
							//Log.e("WINDOW","DRAWING BACKGROUND HIGHLIGHT: B:" + Integer.toHexString(b.getColor()) + " P:" + Integer.toHexString(p.getColor()));
							c.drawRect(x, y - p.getTextSize()+(2*density), x + p.measureText(text.getString()), y+(4*density), b);
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
							//p.setUnderlineText(false);
							if(useBackground) {
								//Log.e("WINDOW","DRAWING BACKGROUND TEXT: B:" + Integer.toHexString(b.getColor()) + " P:" + Integer.toHexString(p.getColor()));
							}
							boolean backGroundSelection = false;
							
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
					//if(u instanceof TextTree.Color) {
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
						break;
					case NEWLINE:
						//x = 0;
						//break;
					case BREAK:
					//if(u instanceof TextTree.NewLine || u instanceof TextTree.Break) {
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
								//Log.e("window","doing linemode 4 for line:"+workingline + " ypos:"+y);
								linemode = 4;
								
							} else {
								linemode = -1;
							}
						}
						
						finishedWithNewLine = true;
						
						//TODO: make sure that where this is moved to works
						y = y + PREF_LINESIZE;
						
						
						//if(mName.equals("mainDisplay")) {
							//Log.e("WINDOW","Calculating starting position, y="+y+ " offset="+offset+" extra="+extra+" delta="+delta);
							//Log.e("WINDOW","Calculating starting position, y="+y);
						//}
						//Log.e("SCREENIT","GET SCREEN ITERATOR, EXTRA:"+extra);
						x = 0;
						//workingline = workingline - 1;
						drawnlines++;
						workingcol = 0;
						if(drawnlines > CALCULATED_LINESINWINDOW + extraLines) {
							//Log.e("WINDOW","STOPPING DRAWING BECAUSE WE DREW: " +drawnlines+" lines");
							stop = true;
						}
						break;
					default:
						//Log.e("WINDOW","DEFAULT CASE FOR DRAW LOOP, FERRET OUT THAT BUG");
						break;
					}
				}
				if(!finishedWithNewLine) {
					y = y + PREF_LINESIZE;
					x = 0;
					drawnlines++;
					workingcol = 0;
				}
				//y = y + PREF_LINESIZE;
				workingline = workingline - 1;
				workingcol = 0;
				l.resetIterator();
			}
//			if(!drawingIterator.hasPrevious()) {
//				Log.e("WINDOW","STOPPED DRAWING BECAUSE WE WERE OUT OF LINES AFTER:"+drawnlines);
//				
//			}
			//}
			showScroller(c);
			c.restore();
			if(Math.abs(fling_velocity) > PREF_LINESIZE) {
				//this.sendEmptyMessageDelayed(MSG_DRAW, 3); //throttle myself, just a little bit.
				//this.invalidate();
				//Log.e("SFS","fling redrawing");
				//fling_velocity = 0;
				if(!mHandler.hasMessages(MESSAGE_DRAW)) {
					this.mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW,3);
				}
			} else {
				fling_velocity = 0;
			}
		
		} else {
			if(!hasDrawRoutine) {
			//Log.e("BUFFERTEST","WINDOW ASKED TO DRAW BUT DID NOT BECAUSE NO DATA");
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
				
/*! \page entry_points
 * \section window Window Lua State Entry Points
 * \subsection OnDraw OnDraw
 * This function is called whenever the window is dirty and needs redrawing of custom content.
 * 
 * \param canvas
 * 
 * \note It is difficult to know exactly what needs to be freed for garbage collection, how to do it, and weather or not it worked. A good example is the button window, it has many custom resources and I had run into memory issues with it when closing/opening the window a few times. It may never happen, it may happen after 100 open/close cycles, or 5, but the general trend of running the foreground process out of memory is an immediate termination of the window. So if you are in a case where you are coming back into the appliation after a phone call or web browser and it immediatly exits, this may be the culprit.
 */
				
				L.getGlobal("debug");
				L.getField(L.getTop(), "traceback");
				L.remove(-2);
				
				
				L.getGlobal("OnDraw");
				if(L.isFunction(L.getTop())) {
					L.pushJavaObject(c);
					
					
					
					int ret = L.pcall(1, 1, -3);
					if(ret != 0) {
						displayLuaError("Error calling OnDraw: " + L.getLuaObject(-1).toString());
					} else {
						//Log.e("LUAWINDOW","OnDraw success!");
						//hasDrawRoutine = false;
						L.pop(2);
					}
				} else {
					hasDrawRoutine = false;
					L.pop(2);
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
		/*if(edgeLeft) {
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
		}*/
		
		c.restore();
		
		//}
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
		
		if(scrollback > SCROLL_MIN +3*density && the_tree.getBrokenLineCount() > CALCULATED_LINESINWINDOW) {
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
			
			if((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
				c.drawCircle(x, y-2, 50*density, textSelectionIndicatorCirclePaint);
			} else {
				c.drawCircle(x, y-2, 33*density, textSelectionIndicatorCirclePaint);
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
			if(textSelectionCopyBitmap.isRecycled()) {
				//Log.e("sf","bitmap is recycled");
			}
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

	public void clearText() {
		the_tree.dumpToBytes(false);
		the_tree.prune();
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
	
	public void setBold(boolean bold) {
		if(bold) {
			PREF_FONT = Typeface.create(PREF_FONT, Typeface.BOLD);
			p.setTypeface(PREF_FONT);
		} else {
			PREF_FONT = Typeface.create(PREF_FONT, Typeface.NORMAL);
			p.setTypeface(PREF_FONT);
		}
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
		//if(updateHandler == null) {
			addBytesImpl(obj,jumpToEnd);
		//} else {
		//	updateHandler.handler.sendMessage(updateHandler.handler.obtainMessage(ThreadUpdater.MESSAGE_ADDTEXT,(jumpToEnd == true) ? 1 : 0, 0, obj));
		//}
		//		synchronized(token) {
//			addBytesImpl(obj,jumpToEnd);
//		}
	}
	
	public void addText(String str,boolean jumpToEnd) {
		//Log.e("LUA","ADDING STRING TO TREE ("+this.getName()+":"+this.mHeight+"): "+str);
		try {
			addBytesImpl(str.getBytes(the_tree.getEncoding()),jumpToEnd);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void addBytesImpl(byte[] obj,boolean jumpToEnd) {
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
			int linesadded = 0;
			try {
				linesadded = the_tree.addBytesImpl(obj);
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
						
						scrollback += linesadded*PREF_LINESIZE;
						//Log.e("BYTE",mName+"REPORT: old_max="+old_max+" new_max="+new_max+" delta="+(new_max-old_max)+" scrollback="+scrollback + " lines="+lines + " oldbroken="+oldbrokencount+ "newbroken="+the_tree.getBrokenLineCount());
						
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
			int offset = 0;
			if(PREF_LINESIZE * CALCULATED_LINESINWINDOW < this.getHeight()) {
				
				offset = ((PREF_LINESIZE) * CALCULATED_LINESINWINDOW) - this.getHeight();
				//Log.e("STARTY","STARTY IS:"+y);
			}
			//int offset =
			int under = CALCULATED_LINESINWINDOW-(the_tree.getBrokenLineCount()-1);
			while(drawingIterator.hasNext()) {drawingIterator.next(); startline += 1;}
			//return new IteratorBundle(the_tree.getLines().listIterator(the_tree.getLines().size()),under*pLineSize,0);
			float tmpy = (under*pLineSize-(offset+(PREF_LINESIZE/3)));
			
			//if(mName.equals("chats")) Log.e("MATH",tmpy+"="+under+"*"+pLineSize+"-("+offset+"("+PREF_LINESIZE+"/3)))");
			return new IteratorBundle(drawingIterator,tmpy,0,startline);
		}
		int lines = 1;
		//double target = Math.floor(pY/pLineSize);
		
		while(drawingIterator.hasNext()) {
			//position = drawingIterator.nextIndex();
			Line l = drawingIterator.next();
			working_h += pLineSize * (1 + l.getBreaks());
			current += 1 + l.getBreaks();
			lines = lines + 1;
			
			if(working_h >= pY) {
				int y = 0;
				if(PREF_LINESIZE * CALCULATED_LINESINWINDOW < this.getHeight()) {
					
					y = ((PREF_LINESIZE) * CALCULATED_LINESINWINDOW) - this.getHeight();
					//Log.e("STARTY","STARTY IS:"+y);
				}
				//if(mName.equals("mainDisplay")) {
				//	Log.e("WINDOW","Calculating starting position, y="+y);
				//}
				double delta = working_h - pY;
				double offset = delta - pLineSize;
				int extra = (int) Math.ceil(delta/pLineSize);
//				if(mName.equals("mainDisplay")) {
//					//Log.e("WINDOW","Calculating starting position, y="+y+ " offset="+offset+" extra="+extra+" delta="+delta);
//					Log.e("WINDOW","Calculating starting position, measured:"+lines);
//				}
				
				//if(mName.equals("chats")) Log.e("MATH","extra="+extra+" offset="+offset+" delta="+delta);
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

		
		homeWidgetRect.set(mWidth-homeWidgetDrawable.getWidth(),mHeight-homeWidgetDrawable.getHeight(),mWidth,mHeight);
		
		Float foo = new Float(0);
		//foo.
		
		if(L == null || !hasOnSizeChanged) return;
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
				displayLuaError("Window("+mName+") OnSizeChangedError: " + L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			//Log.e("LUAWINDOW","Window("+mName+"): No OnSizeChanged Function Defined.");
			hasOnSizeChanged = false;
			L.pop(2);
		}
		this.invalidate();
	}
	boolean hasOnSizeChanged = true;
	
	
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
				displayLuaError("WindowXCallT Error:" + L.getLuaObject(-1).getString());
			} else {
				//success!
				L.pop(2);
			}
			
		} else {
			L.pop(2);
		}
	}
	

	private void initLua() {
		L.openLibs();
		
		//String dataDir = null;
		//try {
		//	ApplicationInfo ai = this..getPackageManager().getApplicationInfo(parent.service.getPackageName(), PackageManager.GET_META_DATA);
		//	dataDir = ai.dataDir;
		//} catch (NameNotFoundException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		if(dataDir == null) {
			//this is bad.
		} else {
			
			//set up the path/cpath.
			L.getGlobal("package");
			L.pushString(dataDir + "/lua/share/5.1/?.lua");
			L.setField(-2, "path");
			
			L.pushString(dataDir + "/lua/lib/5.1/?.so");
			L.setField(-2, "cpath");
			L.pop(1);
			
		}
		
		
		//InvalidateFunction iv = new InvalidateFunction(L);
		NoteFunction df = new NoteFunction(L);
		//BoundsFunction bf = new BoundsFunction(L);
		OptionsMenuFunction omf = new OptionsMenuFunction(L);
		PluginXCallSFunction pxcf = new PluginXCallSFunction(L);
		SheduleCallbackFunction scf = new SheduleCallbackFunction(L);
		CancelSheduleCallbackFunction cscf = new CancelSheduleCallbackFunction(L);
		GetDisplayDensityFunction gddf = new GetDisplayDensityFunction(L); 
		SendToServerFunction stsf = new SendToServerFunction(L);
		GetExternalStorageDirectoryFunction gesdf = new GetExternalStorageDirectoryFunction(L);
		PushMenuStackFunction pmsf = new PushMenuStackFunction(L);
		PopMenuStackFunction popmsf = new PopMenuStackFunction(L);
		GetStatusBarHeight gsbshf = new GetStatusBarHeight(L);
		StatusBarHiddenMethod sghm = new StatusBarHiddenMethod(L);
		GetActionBarHeightFunction gabhf = new GetActionBarHeightFunction(L);
		GetPluginInstallDirectoryFunction gpisdf = new GetPluginInstallDirectoryFunction(L);
        CloseOptionsDialogFunction codf = new CloseOptionsDialogFunction(L);
        GetActivityFunction gaf = new GetActivityFunction(L);
        PluginInstalledFunction pif = new PluginInstalledFunction(L);
        WindowSupportsFunction wsf = new WindowSupportsFunction(L);
        WindowCallFunction wcf = new WindowCallFunction(L);
        WindowBroadcastFunction wbcf = new WindowBroadcastFunction(L);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Log.e("WINDOW","LOADING LUA FOR "+mName);
	}
	
	
	boolean noScript = true;
	public void loadScript(String body) {
		
		if(body == null || body.equals("")) {
			//Log.e("Window","NO SCRIPT SPECIFIED, SHUTTING DOWN LUA:"+mName);
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
		//DrawFunction draw = new DrawFunction(L);
		//try {
		//	draw.register("draw");
		//} catch (LuaException e) {
		//	e.printStackTrace();
		//}
		
		//L.pushJavaObject(parent);
		
		
		
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		L.LloadString(body);
		int ret = L.pcall(0, 1, -2);
		if(ret != 0) {
			displayLuaError("Error Loading Script: "+L.getLuaObject(L.getTop()).getString());
		} else {
			//Log.e("LUAWINDOW","Loaded script body for: " + mName + " id:"+this.toString());
			L.pop(2);
		}

	}
	
	public void runScriptOnCreate() {
		if(L == null) return;
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		
		L.getGlobal("OnCreate");
		if(L.getLuaObject(-1).isFunction()) {
			int tmp = L.pcall(0, 1, -2);
			if(tmp != 0) {
				displayLuaError("Calling OnCreate: "+L.getLuaObject(-1).getString());
			} else {
				//Log.e("LUAWINDOW","OnCreate Success for window ("+this.getName()+")!");
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
		
		L.getGlobal("OnMeasure");
		if(L.isFunction(-1)) {
			hasScriptOnMeasure = true;
		} else {
			hasScriptOnMeasure = false;
		}
		L.pop(1);
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
			mainHandler.sendMessage(mainHandler.obtainMessage(MainWindow.MESSAGE_CLOSEOPTIONSDIALOG));
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
			L.pushString(Integer.toString(((int)Window.this.parent.getTitleBarHeight())));
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
			L.pushJavaObject((Activity)parent.getActivity());
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
			String path = parent.getPathForPlugin(mOwner);
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
			L.pushString(Integer.toString((int)Window.this.parent.getStatusBarHeight()));
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
			L.pushBoolean(Window.this.parent.isStatusBarHidden());
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
			Window.this.parent.dispatchLuaText(foo);
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
			mainHandler.sendMessage(mainHandler.obtainMessage(MainWindow.MESSAGE_POPMENUSTACK));
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
			
			Message m = mainHandler.obtainMessage(MainWindow.MESSAGE_PUSHMENUSTACK,Window.this.mName);
			m.getData().putString("CALLBACK", function);
			
			mainHandler.sendMessage(m);
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
			mainHandler.sendMessage(mainHandler.obtainMessage(MainWindow.MESSAGE_SENDBUTTONDATA,this.getParam(2).getString()));
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
		if(L == null) return;
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.getLuaObject(-1).isFunction()) {
			//prepare to call.
			L.pushString(Integer.toString(id));
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("Scheduled callback("+callback+") error:"+L.getLuaObject(-1).toString());
			} else {
				L.pop(2);
			}
		} else {
			//error no function.
			L.pop(2);
		}
	}
	
	public void callFunction(String callback, String data) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.isFunction(L.getTop())) {
			if(data != null) {
				L.pushString(data);
			} else {
				L.pushNil();
			}
			int tmp = L.pcall(1, 1, -3);
			if(tmp != 0) {
				displayLuaError("Error calling window script function "+callback+": "+L.getLuaObject(-1).getString());
			} else {
				L.pop(1);
			}
		} else {
			L.pop(1);
		}
	}
	
	private class PluginInstalledFunction extends JavaFunction {

		public PluginInstalledFunction(LuaState L) {
			super(L);
			
		}

		@Override
		public int execute() throws LuaException, RemoteException {
			String desired = this.getParam(2).getString();
			boolean result = parent.isPluginInstalled(desired);
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
			boolean ret = parent.checkWindowSupports(desired,function);
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
			parent.windowBroadcast(function, data);
			
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
			
			parent.windowCall(window,function,data);
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
				setCharacterSizes((Integer)o.getValue(),PREF_LINEEXTRA);
				break;
			case line_extra:
				setCharacterSizes(PREF_FONTSIZE,(Integer)o.getValue());
				break;
			case buffer_size:
				the_tree.setMaxLines((Integer)o.getValue());
				Message msg = mainHandler.obtainMessage(MainWindow.MESSAGE_WINDOWBUFFERMAXCHANGED);
				msg.arg1 = (Integer)o.getValue();
				msg.getData().putString("PLUGIN", this.mOwner);
				msg.getData().putString("WINDOW", mName);
				mainHandler.sendMessage(msg);
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
					//Log.e("window","moving selector-> line:"+line+" col:"+column);
					
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
								//Log.e("widget","widget up pressed");
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
								//Log.e("widget","widget down pressed");
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
								//Log.e("widget","widget next pressed");
								//copy the bitches and resume normal operation.
								String copy = the_tree.getTextSection(theSelection);
								//Log.e("copied text","text copied:\n"+copy);
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
								//Log.e("widget","widget left pressed");
								doScrollLeft(false);
								break;
							case RIGHT:
								//.e("widget","widget right pressed");
								doScrollRight(false);
								break;
							case CENTER:
								//Log.e("widget","widget center pressed");
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
		if(L == null) return;
		//call into lua to notify shutdown imminent.
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("OnDestroy");
		if(L.getLuaObject(L.getTop()).isFunction()) {
			int ret = L.pcall(0, 1, -2);
			if(ret != 0) {
				displayLuaError("Error in OnDestroy: "+L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			//no method.
			L.pop(2);
		}
		
		//callbackHandler.removeCallbacksAndMessages(token)
		
		L.close();
		L = null;
		
	}
	
	private void doScrollDown(boolean repeat) {
		//Log.e("FOO","do scroll down");
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
		//Log.e("FOO","do scroll up");
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
		fling_velocity=0;
		this.invalidate();
	}

	private int scrollRepeatRateStep = 1;
	private int scrollRepeatRateInitial = 300;
	private int scrollRepeatRate = scrollRepeatRateInitial;
	private int scrollRepeatRateMin = 60;
	
	public int gravity = Gravity.LEFT;

	
	public void populateMenu(Menu menu) {
		if(L == null) return;
		L.getGlobal("debug");
		L.getField(-1, "traceback");
		L.remove(-2);
		
		L.getGlobal("PopulateMenu");
		if(L.getLuaObject(-1).isFunction()) {
			L.pushJavaObject(menu);
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				displayLuaError("Error in PopulateMenu:"+L.getLuaObject(-1).getString());
			} else {
				L.pop(2);
			}
		} else {
			L.pop(2);
		}
	}

	public void setBuffer(TextTree buffer) {
		// TODO Auto-generated method stub
		this.the_tree = buffer;
		
	}

	public boolean checkSupports(String function) {
		if(L != null) {
			L.getGlobal(function);
			
			boolean ret = L.isFunction(-1);
			L.pop(1);
			return ret;
		}
		return false;
	}

	public boolean isCenterJustify() {
		return centerJustify;
	}

	public void setCenterJustify(boolean centerJustify) {
		this.centerJustify = centerJustify;
	}
	
	public int getLineSize() {
		return PREF_LINESIZE;
	}
	
	public void fitFontSize(int chars) {
		//Log.e("LUA","SETTING FITCHARS:"+chars);
		fitChars = chars;
	}
		
	public void doFitFontSize(int width) {
		if(fitChars < 0) return;
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
		
		if(charsPerLine < fitChars) {
			//for QVGA screens, this test will always fail on the first step.
			done = true;
		} else {
			fontSize += delta;
			p.setTextSize(fontSize);
		}
		
		while(!done) {
			charWidth = p.measureText("A");
			charsPerLine = windowWidth / charWidth;
			if(charsPerLine < fitChars) {
				done = true;
				fontSize -= delta; //return to the previous font size that produced > 80 characters.
			} else {
				fontSize += delta;
				p.setTextSize(fontSize);
			}
		}
		
		PREF_FONTSIZE = (int) fontSize;
		PREF_LINESIZE = PREF_FONTSIZE + PREF_LINEEXTRA;
		calculateCharacterFeatures(mWidth,mHeight);
		//return (int)fontSize;
	}
	
	public void setHasText(boolean has) {
		boolean hasText = has;
	}
//	private class ThreadUpdater extends Thread {
//		public final static int MESSAGE_ADDTEXT = 1;
//		public final static int MESSAGE_QUIT = 2;
//		TextTree buffer = null;
//		Object synch = null;
//		Handler mainHandler = null;
//		public Handler handler = null;
//		public ThreadUpdater(TextTree buffer,Handler h,Object synch) {
//			this.buffer = buffer;
//			this.mainHandler = h;
//			this.synch = synch;
//		}
//		
//		@Override
//		public void run() {
//			Looper.prepare();
//			
//			this.handler = new Handler() {
//				public void handleMessage(Message msg) {
//					switch(msg.what) {
//					case MESSAGE_ADDTEXT:
//						synchronized(synch) {
//							//try {
//								boolean jumptoend = false;
//								if(msg.arg1 == 1) jumptoend = true;
//								Window.this.addBytesImpl((byte[])msg.obj,jumptoend);
//								//buffer.addBytesImpl((byte[])msg.obj);
//							//} catch (UnsupportedEncodingException e) {
//								// TODO Auto-generated catch block
//							//	e.printStackTrace();
//							//}
//							synch.notify();
//						}
//						break;
//					case MESSAGE_QUIT:
//						this.getLooper().quit();
//						break;
//					}
//				}
//			};
//			
//			Looper.loop();
//		}
		
		
	//}
	
	//private ThreadUpdater updateHandler = null;
	//private Object updateSynch = new Object();
	public TextTree getBuffer() {
		return the_tree;
	}
	
	public double measure(String str) {
		return featurePaint.measureText(str);
	}
}

