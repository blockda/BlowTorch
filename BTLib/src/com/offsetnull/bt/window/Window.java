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
import android.util.Log;

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
	/** Scroll repeat rate inital value. */
	private static final int SCROLL_REPEAT_RATE = 300;
	/** Lua relative stack location -2. */
	private static final int TOP_MINUS_TWO = -2;
	/** Lua relative stack location -2. */
	private static final int TOP_MINUS_THREE = -3;
	/** Lua relative stack location -2. */
	private static final int TOP_MINUS_FOUR = -4;
	/** Lua relative stack location -2. */
	private static final int TOP_MINUS_FIVE = -5;
	/** Maximum fling velocity. */
	private static final float MAX_VELOCITY = 700; 
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
	/** Synchronization target for touch handling and text adding (i think). */
	private Object mToken = new Object(); //token for synchronization.
	/** The user configurable settings for this window. */
	private SettingsGroup mSettings = null;
	/** Application context. */
	//private Context mContext = null;
	/** Bitmap that holds the selection indicator widget. */
	private Bitmap mSelectionIndicatorBitmap = null;
	/** Canvas that allows drawing to the selection indicator bitmap. */
	private Canvas mSelectionIndicatorCanvas = null;
	/** The font size for the selection widget. */
	private int mSelectionIndicatorFontSize = 30;
	/** Another patint object associatied with drawing the selection indicator. */
	private Paint mSelectionIndicatorPaint = new Paint();
	/** The measure of one character in the selection widget. */
	private int mSelectionCharacterWidth = 1;
	/** The measure of half of the selection widget. */
	private int mSelectionIndicatorHalfDimension = 60;
	/** Clip object to cut away the outside of the circle by masking. */
	private Path mSelectionIndicatorClipPath = new Path();
	/** Left button hot zone for the selection widget. */
	private Rect mSelectionIndicatorLeftButtonRect = new Rect();
	/** right button hot zone for the selection widget. */
	private Rect mSelectionIndicatorRightButtonRect = new Rect();
	/** top button hot zone for the selection widget. */
	private Rect mSelectionIndicatorUpButtonRect = new Rect();
	/** bottom button hot zone for the selection widget. */
	private Rect mSelectionIndicatorDownButtonRect = new Rect();
	/** center button hot zone for the selection widget. */
	private Rect mSelectionIndicatorCenterButtonRect = new Rect();
	/** hot zone for the selection widget. */
	private Rect mSelectionIndicatorRect = new Rect();
	/** Scroll repeat acceleration. */
	private int mScrollRepeatRateStep = 1;
	/** Scroll repeat rate variable. */
	private int mScrollRepeatRate = SCROLL_REPEAT_RATE;
	/** The minimum scroll repeat rate. */
	private int mScrollRepeatRateMin = 60;
	/** Indicates that a finger is currently down on the touchpad. */
	boolean mFingerDown = false;
	/** The difference between the last touch event and this event. */
	int diff_amount = 0;
	/** The x value of the start of the touch event. */
	Float start_x = null;
	/** The y value of the start of the touch event. */
	Float mStartY = null;
	/** The previous touch event. */
	MotionEvent mTouchPreEvent  = null;
	/** Indicates that the finger has left the touchpad. */
	boolean finger_down_to_up = false;
	/** The system time in millis that the last frame was drawn at. */
	long mLastFrameTime = 0;
	/** If a touch event happened inside of a link hitbox it will be noted here. */
	public int mTouchInLink = -1;
	/** Indicates weather a touch event happened inside of the jump to home button. */
	boolean homeWidgetFingerDown = false;
	/** The first finger pointer id. */
	int pointer = -1;
	/** Fling acceleration for scrolling. */
	float fling_accel = 200.0f; //(units per sec);
	/** List of link boxes found on the current drawing routine. */
	private ArrayList<LinkBox> linkBoxes = new ArrayList<LinkBox>(); /** TODO: make this into a preallocated list of a fixed size so you don't reallocate during the draw phase. */
	/** Paint object for the scroll bar. */
	private Paint mScrollerPaint = new Paint();
	/** Indicates if the home widget is being drawn. */
	private boolean homeWidgetShowing = false;
	/** View bounds for the the scroller rectangle. */
	Rect scrollerRect = new Rect();
	/** This is kind of a promiscuous variable, color is set on the fly. */
	Paint featurePaint = new Paint();
	/** The internal memory location of the internal lua default libraries for BlowTorch. */
	private String mDataDir = null;
	/** Foreground text paint object, short name for code readability. */
	Paint p = new Paint();
	/** Backgrond highlighting paint object, short name for code readabliity. */
	Paint b = new Paint();
	/** The configured hyperlink background or highlight color. */
	Paint linkColor = null;
	/** The minimum amount of scroll, i think this gets set before use. */
	private Double SCROLL_MIN = 24d;
	/** The current scrollback amount. */
	private Double mScrollback = SCROLL_MIN;
	/** Uhg. The screen iterator. I'm not really sure this whole thing is worth it. There has to be a better way. */
	ListIterator<TextTree.Line> screenIt = null;
	/** Helper object for the draw routine, iterates a line object unit to unit. */
	ListIterator<Unit> unitIterator = null;
	/** The minimum hitbox size for a link. */
	private int mLinkBoxHeightMinimum = 20;
	/** Inidcates the presence of a global OnDraw(Canvas) function in the backing script. */
	boolean mHasDrawRoutine = true;
	/** The name of this window. */
	private String mName = null;
	/** The clipping rectangle for the draw routine. */
	private Rect mClipRect = new Rect();
	/** The current link click that is being clicked. */
	private StringBuffer mCurrentLink = new StringBuffer();
	
	/** Constructor to be used, the others aren't really suppposed to be there. 
	 * 
	 * @param pDataDir The path to the internal lua libraries.
	 * @param context The application context.
	 * @param name The name of the window. 
	 * @param owner The plugin owner of the window.
	 * @param mainWindowHandler The callback handler to the MainWindow Activity.
	 * @param settings The serializable settings for the window.
	 * @param activity The MainWindowCallback to use to report changes from the scripts
	 */
	public Window(final String pDataDir,
			final Context context,
			final String name,
			final String owner,
			final Handler mainWindowHandler,
			final SettingsGroup settings,
			final MainWindowCallback activity) {
		super(context);
		this.mParent = activity;
		init(pDataDir, name, owner, mainWindowHandler, settings);
	}
	
	/** Generic window constructor, here to pass the lint test. Will not work. 
	 * 
	 * @param c Application Context
	 */
	public Window(final Context c) {
		super(c);
	}
	
	/** XML Inflation constructor, here to pass the lint test. Will not work. 
	 * 
	 * @param c Application context
	 * @param a I'm not really sure but you can't use it from lua.
	 */
	public Window(final Context c, final AttributeSet a) {
		super(c, a);
	}
	
	/** Kicks off a lua error message to the main output window. This has to be sent round trip through the Service in order to be picked up by the internal buffer.
	 * 
	 * @param message The lua error to display.
	 */
	public final void displayLuaError(final String message) {
		mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_DISPLAYLUAERROR, "\n" + Colorizer.getRedColor() + message + Colorizer.getWhiteColor() + "\n"));
	}
	
	@Override
	protected final void onAttachedToWindow() {
		windowShowing = true;
	}
	
	@Override
	protected final void onDetachedFromWindow() {
		windowShowing = false;
	}
	


	/** Initialization routine. It all starts here.
	 * 
	 * @param dataDir The path to the internal lua libraries.
	 * @param name The name of the window. 
	 * @param owner The plugin owner of the window.
	 * @param mainWindowHandler The callback handler to the MainWindow Activity.
	 * @param settings The serializable settings for the window.
	 */
	private void init(final String dataDir, final String name, final String owner, final Handler mainWindowHandler, final SettingsGroup settings) {
		this.mDataDir = dataDir;
		this.mDensity = this.getContext().getResources().getDisplayMetrics().density;
		if ((Window.this.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			mSelectionIndicatorHalfDimension = (int) (90 * mDensity);
		} else {
			mSelectionIndicatorHalfDimension = (int) (60 * mDensity);
		}
		
		mSelectionIndicatorClipPath.addCircle(mSelectionIndicatorHalfDimension, mSelectionIndicatorHalfDimension, mSelectionIndicatorHalfDimension - 10, Path.Direction.CCW);
		mHomeWidgetDrawable = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.homewidget);
		mTextSelectionCancelBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.cancel_tiny);
		mTextSelectionCopyBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.copy_tiny);
		mTextSelectionSwapBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.offsetnull.bt.R.drawable.swap);
		
		mTextSelectionIndicatorPaint.setStyle(Paint.Style.STROKE);
		mTextSelectionIndicatorPaint.setStrokeWidth(1 * mDensity);
		mTextSelectionIndicatorPaint.setColor(0xFFFF0000);
		mTextSelectionIndicatorPaint.setAntiAlias(true);
		
		mTextSelectionIndicatorBackgroundPaint.setStyle(Paint.Style.FILL);
		mTextSelectionIndicatorBackgroundPaint.setColor(0x770000FF);
		
		mTextSelectionIndicatorCirclePaint.setStyle(Paint.Style.STROKE);
		mTextSelectionIndicatorCirclePaint.setStrokeWidth(2);
		mTextSelectionIndicatorCirclePaint.setColor(0xFFFFFFFF);
		DashPathEffect dpe = new DashPathEffect(new float[]{3, 3}, 0);
		
		mTextSelectionIndicatorCirclePaint.setPathEffect(dpe);
		mTextSelectionIndicatorCirclePaint.setAntiAlias(true);
		this.mSettings = settings;
		this.mSettings.setListener(this);
		mBuffer = new TextTree();
		if (name.equals("mainDisplay")) {
			mBuffer.debugLineAdd = true;
		}
		mHoldBuffer = new TextTree();
		mHandler = new Handler() {
			public void handleMessage(final Message msg) {
				switch(msg.what) {
				case MESSAGE_RESETWITHDATA:
					Window.this.resetAndAddText((byte[]) msg.obj);
					break;
				case MESSAGE_SCROLLLEFT:
					mScrollRepeatRate -= (mScrollRepeatRateStep++) * 5; if (mScrollRepeatRate < mScrollRepeatRateMin) { mScrollRepeatRate = mScrollRepeatRateMin; }
					Window.this.doScrollLeft(true);
					break;
				case MESSAGE_SCROLLRIGHT:
					mScrollRepeatRate -= (mScrollRepeatRateStep++) *5 ; if (mScrollRepeatRate < mScrollRepeatRateMin) { mScrollRepeatRate = mScrollRepeatRateMin; }
					Window.this.doScrollRight(true);
					break;
				case MESSAGE_SCROLLDOWN:
					mScrollRepeatRate -= (mScrollRepeatRateStep++) * 5; if (mScrollRepeatRate < mScrollRepeatRateMin) { mScrollRepeatRate = mScrollRepeatRateMin; }
					Window.this.doScrollDown(true);
					break;
				case MESSAGE_SCROLLUP:
					mScrollRepeatRate -= (mScrollRepeatRateStep++) * 5; if (mScrollRepeatRate < mScrollRepeatRateMin) { mScrollRepeatRate = mScrollRepeatRateMin; }
					Window.this.doScrollUp(true);
					break;
				case MESSAGE_STARTSELECTION:
					Window.this.startSelection(msg.arg1, msg.arg2);
					break;
				case MESSAGE_ENCODINGCHANGED:
					Window.this.updateEncoding((String) msg.obj);
					break;
				case MESSAGE_SETTINGSCHANGED:
					Window.this.doUpdateSetting(msg.getData().getString("KEY"), msg.getData().getString("VALUE"));
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
					Window.this.addBytes((byte[]) msg.obj, false);
					break;
				case MESSAGE_PROCESSXCALLS:
					Window.this.xcallS(msg.getData().getString("FUNCTION"), (String) msg.obj);
					
					break;
				case MESSAGE_XCALLB:
					//try {
					try {
						Window.this.xcallB(msg.getData().getString("FUNCTION"), (byte[]) msg.obj);
					} catch (LuaException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}
		};
		
		
		//lua startup.
		mOwner = owner;
		
		this.mMainWindowHandler = mainWindowHandler;
		
		mName = name;
		
		mSelectionIndicatorBitmap = Bitmap.createBitmap(2 * mSelectionIndicatorHalfDimension, 2 * mSelectionIndicatorHalfDimension, Bitmap.Config.ARGB_8888);
		mSelectionIndicatorCanvas = new Canvas(mSelectionIndicatorBitmap);
		
		int full = mSelectionIndicatorHalfDimension * 2;
		int third = full / 3;
		
		mSelectionIndicatorLeftButtonRect.set(third, 0, 2 * third, 40);
		mSelectionIndicatorUpButtonRect.set(0, third, 40, 2 * third);
		mSelectionIndicatorRightButtonRect.set(full - 40, third, full, 2 * third);
		mSelectionIndicatorDownButtonRect.set(third, 2 * third, 2 * third, full);
		mSelectionIndicatorCenterButtonRect.set(third, third, 2 * third, 2 * third);
		
		mSelectionIndicatorRect.set(0, 0, full, full);
		
		//start extracting and setting settings.
		IntegerOption fontsize = (IntegerOption) settings.findOptionByKey("font_size");
		IntegerOption lineextra = (IntegerOption) settings.findOptionByKey("line_extra");
		IntegerOption buffersize = (IntegerOption) settings.findOptionByKey("buffer_size");
		FileOption fontpath = (FileOption) settings.findOptionByKey("font_path");
		ListOption colorOption = (ListOption) settings.findOptionByKey("color_option");
		ColorOption hyperlinkcolor = (ColorOption) settings.findOptionByKey("hyperlink_color");
		
		BooleanOption wordwrap = (BooleanOption) settings.findOptionByKey("word_wrap");
		BooleanOption hlenabled = (BooleanOption) settings.findOptionByKey("hyperlinks_enabled");
		
		ListOption hlmode = (ListOption) settings.findOptionByKey("hyperlink_mode");
		
		mPrefFont = loadFontFromName((String) fontpath.getValue());
		p.setTypeface(mPrefFont);
		
		mBuffer.setMaxLines((Integer) buffersize.getValue());
		
		mPrefLineExtra = (Integer) lineextra.getValue();
		mPrefFontSize = (Integer) fontsize.getValue();
		setCharacterSizes(mPrefFontSize, mPrefLineExtra);
		
		switch((Integer) colorOption.getValue()) {
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
		default:
			break;
		}
		
		this.setWordWrap((Boolean) wordwrap.getValue());
		this.setLinkColor((Integer) hyperlinkcolor.getValue());
		this.setLinkMode((Integer) hlmode.getValue());
		this.setLinksEnabled((Boolean) hlenabled.getValue());
	}
	
	/** Resets the buffer with the given argument. 
	 * 
	 * @param obj The text to add in bytes.
	 */
	protected final void resetAndAddText(final byte[] obj) {
		mBuffer.empty();
		mHoldBuffer.empty();
		addBytes(obj, true);
	}

	/** The end of the WindowXCallB Lua function.
	 * 
	 * @param string The name of the global callback function to call.
	 * @param bytes The bytes to provide to it (gets converted to a lua string without going through java.
	 * @throws LuaException Thown when there is a problem with lua.
	 */
	protected final void xcallB(final String string, final byte[] bytes) throws LuaException {
		if (mL == null) { return; }
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(TOP_MINUS_TWO);
		
		mL.getGlobal(string);
		if (mL.getLuaObject(-1).isFunction()) {
			mL.pushObjectValue(bytes);
			int ret = mL.pcall(1, 1, TOP_MINUS_THREE);
			if (ret != 0) {
				displayLuaError("WindowXCallB calling: " + string + " error:" + mL.getLuaObject(-1).getString());
			} else {
				mL.pop(2);
			}
		} else {
			mL.pop(2);
		}
	}

	/** Starts the selection mode, sets up structures and flags that cause the widget to be drawn in onDraw(...).
	 * 
	 * @param line Starting line.
	 * @param column Starting column.
	 */
	private void startSelection(final int line, final int column) {
		
		theSelection = mBuffer.getSelectionForPoint(line, column);
		if (theSelection == null) {
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
	
	/** Called from the MainWindow when the system encoding changes. 
	 * 
	 * @param value The new encoding value.
	 */
	protected final void updateEncoding(final String value) {
		mBuffer.setEncoding(value);
	}

	/** Implementation of the settings handler routine to handle when settings change. 
	 * 
	 * @param key The key of the setting that changed.
	 * @param value The new value of the setting.
	 */
	protected final void doUpdateSetting(final String key, final String value) {
		mSettings.setOption(key, value);
	}
	
	/** Updates relevant draw routine structures and temporary values. Measures one character and handles the fit routine. 
	 * 
	 * @param width Window width.
	 * @param height Window height.
	 */
	public final void calculateCharacterFeatures(final int width, final int height) {
		
		if (height == 0 && width == 0) {
			return;
		}
		mCalculatedLinesInWindow = (int) (height / mPrefLineSize);
		
		featurePaint.setTypeface(mPrefFont);
		featurePaint.setTextSize(mPrefFontSize);
		mOneCharWidth = (int) Math.ceil(featurePaint.measureText("a")); //measure a single character
		mCalculatedRowsInWindow = width / mOneCharWidth;
		
		mSelectionIndicatorPaint.setTextSize(mSelectionIndicatorFontSize);
		mSelectionIndicatorPaint.setTypeface(mPrefFont);
		mSelectionIndicatorPaint.setAntiAlias(true);
		mSelectionCharacterWidth = (int) Math.ceil(mSelectionIndicatorPaint.measureText("a"));
		selectionIndicatorVectorX = mOneCharWidth + mSelectionIndicatorHalfDimension;
		if (automaticBreaks) {
			this.setLineBreaks(0);
		}
		
		if (mBuffer.getBrokenLineCount() == 0) {
			jumpToZero();
		}
		
	}
	

	/** Setter for mName.
	 * 
	 * @param name New name value.
	 */
	public final void setName(final String name) {
		mName = name;
	}
	
	/** Getter for mName. 
	 * 
	 * @return The name of this window.
	 */
	public final String getName() {
		return mName;
	}
	

	@Override
	public final boolean onTouchEvent(final MotionEvent t) {
		int pointerIndex = (t.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerId = t.getPointerId(pointerIndex);
		
		if (pointer > 0 && pointerId != pointer) {
			//but invalidate this anyway
			this.invalidate();
			return false;
		}
			//normal
		if (!scrollingEnabled) {
			return false;
		}
		int index = t.findPointerIndex(pointerId);
		start_x = Float.valueOf(t.getX(index));
		start_x = start_x + 1;
		
		if (mBuffer.getBrokenLineCount() != 0) {
			Rect rect = new Rect();
			if (!mFingerDown) {
				
				rect.top = 0;
				rect.left = 0;
				rect.right = mWidth;
				rect.bottom = mHeight;
				
				
				Point point = new Point();
				point.x = (int) t.getX();
				point.y = (int) t.getY();
				if (!rect.contains((int) t.getX(), (int) t.getY())) {
					return false;
				}
			}
			
			synchronized (mToken) {
			if (t.getAction() == MotionEvent.ACTION_DOWN) {
				pointer = pointerId;
				start_x = Float.valueOf(t.getX(index));
				mStartY = Float.valueOf(t.getY(index));
				mTouchPreEvent = MotionEvent.obtainNoHistory(t);
				//calculate row/col
				float x = t.getX(index);
				float y = t.getY(index);
				//t.recycle();
				mFlingVelocity = 0.0f;
				mFingerDown = true;
				finger_down_to_up = false;
				mLastFrameTime = 0;
				
				for (int tmpCount = 0; tmpCount < linkBoxes.size(); tmpCount++) {
					if (linkBoxes.get(tmpCount).getBox().contains(start_x.intValue(), mStartY.intValue())) {
						mTouchInLink = tmpCount;
					}
				}
				
				//convert y to be at the bottom of the screen.
				
				y = (float) ((float) this.getHeight() - y + (mScrollback - SCROLL_MIN));
				
				int line = (int) Math.floor(y / (float) mPrefLineSize);
				
				int column = (int) Math.floor(x / (float) mOneCharWidth);
				if (mTextSelectionEnabled) {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_STARTSELECTION, line, column), 1500);
				}
				
				if (homeWidgetShowing) {
					if (mHomeWidgetRect.contains((int) x, (int) t.getY())) {
						homeWidgetFingerDown = true;
					}
				}
				
			}
			
			if (t.getAction() == MotionEvent.ACTION_MOVE) {
				
	
				Float nowY = Float.valueOf(t.getY(index));
				
				
	
				float thentime = mTouchPreEvent.getEventTime();
				float nowtime = t.getEventTime();
				
				float time = (nowtime - thentime) / 1000.0f; //convert to seconds
				
				float prevY = mTouchPreEvent.getY(index);
				float dist = nowY - prevY;
				diff_amount = (int) dist;
				
				if (Math.abs(nowY - mStartY) > mPrefLineSize * 1.5) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
				}
				
				float velocity = dist / time;
				
				if (Math.abs(velocity) > MAX_VELOCITY) {
					if (velocity > 0) {
						velocity = MAX_VELOCITY;
					} else {
						velocity = MAX_VELOCITY * -1;
					}
				}
				mFlingVelocity = velocity;
				
				if (Math.abs(nowY - mStartY) > mPrefLineSize * 1.5 * mDensity) {
					mHandler.removeMessages(MESSAGE_STARTSELECTION);
				}
				
				if (Math.abs(diff_amount) > 5 * mDensity) {
					
					mTouchPreEvent = MotionEvent.obtainNoHistory(t);
					//t.recycle();
				}
				
			}						
			
			if (t.getAction() == (MotionEvent.ACTION_UP)) {
				
				mTouchPreEvent = null;
				//prev_y = new Float(0);
		        
		        //reset the priority
		        pointer = -1;
	
		        mTouchPreEvent = null;
		        mFingerDown = false;
		        finger_down_to_up = true;
		         
				if (mTouchInLink > -1) {
					mMainWindowHandler.sendMessage(mMainWindowHandler.obtainMessage(MainWindow.MESSAGE_LAUNCHURL, linkBoxes.get(mTouchInLink).getData()));
			        mTouchInLink = -1;
				}
				
				
				mHandler.removeMessages(MESSAGE_STARTSELECTION);
					
				if (homeWidgetShowing && homeWidgetFingerDown) {
					if (mHomeWidgetRect.contains((int) t.getX(index), (int) t.getY(index))) {
						mScrollback = SCROLL_MIN;
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
	
	/** Called from onDraw, calculates a new scrollback value for this frame. */
	private void calculateScrollBack() {
		
		if (mLastFrameTime == 0) { //never drawn before
			if (mBuffer.getBrokenLineCount() <= mCalculatedLinesInWindow) { mScrollback = SCROLL_MIN; return;}
			if (mFingerDown) {
				mScrollback = (double) Math.floor(mScrollback + diff_amount);
				if (mScrollback < SCROLL_MIN) {
					mScrollback = SCROLL_MIN;
				} else {
					if (mScrollback >= ((mBuffer.getBrokenLineCount() * mPrefLineSize))) {
						mScrollback = (double) ((mBuffer.getBrokenLineCount() * mPrefLineSize));
					}
				}
				diff_amount = 0;
			} else {
				if (finger_down_to_up) {
					mLastFrameTime = System.currentTimeMillis(); 
					finger_down_to_up = false;
				}
			}
		} else {
			
			if (!mFingerDown) {				
				long nowdrawtime = System.currentTimeMillis(); 
				
				float durationSinceLastFrame = ((float) (nowdrawtime - mLastFrameTime)) / 1000.0f; //convert to seconds
				mLastFrameTime = System.currentTimeMillis();
				if (mFlingVelocity < 0) {
					mFlingVelocity = mFlingVelocity + fling_accel * durationSinceLastFrame;
					mScrollback =  mScrollback + mFlingVelocity * durationSinceLastFrame;
				} else if (mFlingVelocity > 0) {
					mFlingVelocity = mFlingVelocity - fling_accel * durationSinceLastFrame;
					mScrollback =  mScrollback + mFlingVelocity * durationSinceLastFrame;
				}
				
				if (Math.abs(Double.valueOf(mFlingVelocity)) < 15) {
					mFlingVelocity = 0;
					mLastFrameTime = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				}
					
				if (mScrollback <= SCROLL_MIN) {
					mScrollback = SCROLL_MIN;
					mFlingVelocity = 0;
					mLastFrameTime = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				}
				
				if (mScrollback >= ((mBuffer.getBrokenLineCount() * mPrefLineSize))) {
					mScrollback = (double) ((mBuffer.getBrokenLineCount() * mPrefLineSize));
					mFlingVelocity = 0;
					mLastFrameTime = 0;
					Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
					
				}
			}

			
		}
			
	}
	
	public void runScriptOnCreate() {
		if(mL == null) return;
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
/*! \page entry_points
 * \section window Window Lua State Entry Points
 * \subsection OnCreate OnCreate
 * Called during window creation. After the main script has been loaded and the actual backing android View is created and shown.
 * 
 * \param none
 * 
 * \note General initialization of code can be done when the script is loaded. But certain graphical subsystems will be unavailable until this callback is called.
 */
		
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
	

	@Override
	public final void onDraw(final Canvas c) {
		if (selectedSelector != null) {
			int color = mScrollerPaint.getColor();
			int newcolor = 0xFF000000 | color;
			mScrollerPaint.setColor(newcolor);
			mSelectionIndicatorCanvas.drawRect(mSelectionIndicatorLeftButtonRect, mScrollerPaint);
			mSelectionIndicatorCanvas.drawRect(mSelectionIndicatorUpButtonRect, mScrollerPaint);
			mSelectionIndicatorCanvas.drawRect(mSelectionIndicatorRightButtonRect, mScrollerPaint);
			mSelectionIndicatorCanvas.drawRect(mSelectionIndicatorDownButtonRect, mScrollerPaint);
			mScrollerPaint.setColor(color);
			
			mSelectionIndicatorCanvas.save();
			mSelectionIndicatorCanvas.clipPath(mSelectionIndicatorClipPath);
			mSelectionIndicatorCanvas.drawColor(0xFF444444);
			
		}
		int startline2 = 0, startcol = 0, endline = 0, endcol = 0;
		if (theSelection  != null) {

			if (theSelection.start.line == theSelection.end.line) {
				startline2 = theSelection.start.line;
				endline = theSelection.start.line;
				if (theSelection.end.column < theSelection.start.column) {
					startcol = theSelection.end.column;
					endcol = theSelection.start.column;
				} else{
					startcol = theSelection.start.column;
					endcol = theSelection.end.column;
				}
			} else if (theSelection.end.line >  theSelection.start.line) {
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
		
		
		if (mBuffer.getBrokenLineCount() != 0) {
			if (linkColor == null) {
				
				linkColor = new Paint();
				linkColor.setAntiAlias(true);
				linkColor.setColor(mLinkHighlightColor);
			}
			
			linkColor.setColor(mLinkHighlightColor);
			calculateScrollBack();
			c.save();
			
			
			mClipRect.top = 0;
			mClipRect.left = 0;
			mClipRect.right = mWidth;
			mClipRect.bottom = mHeight;
			
			c.clipRect(mClipRect);
			
			b.setColor(0xFF0A0A0A);
			c.drawColor(0xFF0A0A0A); //fill with black
			c.drawRect(0, 0, mClipRect.right - mClipRect.left, mClipRect.top - mClipRect.bottom, b);
			p.setTypeface(mPrefFont);
			p.setAntiAlias(true);
			p.setTextSize(mPrefFontSize);
			p.setColor(0xFFFFFFFF);
			
			float x = 0;
			float y = 0;
			if (mPrefLineSize * mCalculatedLinesInWindow < this.getHeight()) {
				
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
			
			while (!gotIt && tries <= maxTries) {
				try {
					tries = tries + 1;
					bundle = getScreenIterator(mScrollback, mPrefLineSize);
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
			if (!gotIt) {
				this.invalidate();
				return;
			}
			screenIt = bundle.getI();
			y = bundle.getOffset();

			int extraLines = bundle.getExtraLines();
			if (screenIt == null) { return;}
			
			int startline = bundle.getStartLine();
			int workingline = startline;
			int workingcol = 0;
			
			//TODO: STEP 3
			//find bleed.
			boolean bleeding = false;
			int back = 0;
			while (screenIt.hasNext() && !bleeding) {
				
				Line l = screenIt.next();
				back++;

				for (Unit u : l.getData()) {
					if (u instanceof TextTree.Color) {
						mXterm256Color = false;
						mXterm256FGStart = false;
						mXterm256BGStart = false;
						for (int i = 0; i < ((TextTree.Color) u).getOperations().size(); i++) {
							updateColorRegisters(((TextTree.Color) u).getOperations().get(i));
							Colorizer.COLOR_TYPE type = Colorizer.getColorType(((TextTree.Color) u).getOperations().get(i));
							if (type != Colorizer.COLOR_TYPE.NOT_A_COLOR && type != Colorizer.COLOR_TYPE.BACKGROUND && type != Colorizer.COLOR_TYPE.BRIGHT_CODE) {
								bleeding = true;
							}
						}
						if (mXterm256FGStart) {
							p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor, mXterm256Color));
						} else {
							p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor, false));
							
						}
						
						b.setColor(0xFF000000);//no not bleed background colors
	
					}
				}
			}
			
			if (!bleeding) {
				p.setColor(0xFF000000 | Colorizer.getColorValue(0, 37, false));
			}
			//TODO: STEP 4
			//advance the iterator back the number of units it took to find a bleed.
			//second real expensive move. In the case of a no color text buffer, it would walk from scroll to end and back every time. USE COLOR 
			while (back > 0) {
				screenIt.previous();
				back--;
			}
			
			if (screenIt.hasNext()) {
				screenIt.next(); // the bleed/back stuff seems to be messing with my calculation
			} 
			//TODO: STEP 5
			//draw the text, from top to bottom.	
			
			int drawnlines = 0;
			
			boolean doingLink = false;
			
			mCurrentLink.setLength(0);
			linkBoxes.clear();
			
			while (!stop && screenIt.hasPrevious()) {
				Line l = screenIt.previous();
				
				
				if (mCenterJustify) {
					//center justify.

					int amount = mOneCharWidth * l.charcount;
					x = (float) ((mWidth / 2.0) - (amount / 2.0));
				}
				unitIterator = l.getIterator();
				
				int linemode = 0;
				if (startline2 == endline && startline2 == workingline) {
					linemode = 1;
				} else if (startline2 == workingline) {
					linemode = 2;
				} else if (startline2 > workingline && endline < workingline) {
					
					linemode = 3;
				} else if (endline == workingline) {
					linemode = 4;
					
				}
				
				boolean finishedWithNewLine = false;
				
				while (unitIterator.hasNext()) {
					Unit u = unitIterator.next();
					boolean useBackground = false;
					if (b.getColor() != 0xFF0A0A0A && b.getColor() != 0xFF000000) {
						useBackground = true;
					}
					
					switch(u.type) {
					case WHITESPACE:
					case TEXT:
						TextTree.Text text = (TextTree.Text) u;
						boolean doIndicator = false;
						int indicatorlineoffset = 0;
						if (selectedSelector != null && selectedSelector.line == workingline) {
							doIndicator = true;
						} else if (selectedSelector != null && Math.abs(selectedSelector.line - workingline) < 3) {
							doIndicator = true;
							indicatorlineoffset = selectedSelector.line - workingline;
						}
						
						if (theSelection  != null) {
							switch(linemode) {
							case 1:
								int finishCol = workingcol + text.bytecount;
								if (finishCol > startcol && finishCol - 1 <= endcol){
									if ((finishCol - startcol) < text.bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * mOneCharWidth;
										int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize() + (3 * mDensity), x + stringWidth, y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + p.measureText(text.getString()), y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
									}
								} else if (finishCol > endcol) {
									if ((finishCol - endcol) < text.bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * mOneCharWidth;
										c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + overshootPixels, y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
									} 
								} 
								break;
							case 2:
								finishCol = workingcol + text.bytecount;
								if (finishCol > startcol) {
									if ((finishCol - startcol) < text.bytecount) {
										int overshoot = startcol - workingcol;
										int overshootPixels = overshoot * mOneCharWidth;
										int stringWidth = (int) p.measureText(text.getString());
										c.drawRect(x + overshootPixels, y - p.getTextSize() + (2 * mDensity), x + stringWidth, y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
									} else {
										c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + p.measureText(text.getString()), y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
									}
								} 
								break;
							case 3:
								
								c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + p.measureText(text.getString()), y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
								break;
							case 4:
								finishCol = workingcol + text.bytecount;
								if (finishCol >= endcol) {
									if ((finishCol - endcol) < text.bytecount) {
										int overshoot = endcol - workingcol + 1;
										int overshootPixels = overshoot * mOneCharWidth;
										c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + overshootPixels, y + (4 * mDensity), mScrollerPaint);
									}
								} else {
									c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + p.measureText(text.getString()), y + (4 * mDensity), mTextSelectionIndicatorBackgroundPaint);
								}
								break;
							default:
								break;
							}
						}
						
						if (useBackground) {
							c.drawRect(x, y - p.getTextSize() + (2 * mDensity), x + p.measureText(text.getString()), y + (4 * mDensity), b);
						}
						
						if (text.isLink() || doingLink) {
							if (u instanceof TextTree.WhiteSpace) {
								//DO LINK BOX.
								for (int z = 0; z < linkBoxes.size(); z++) {
									if (linkBoxes.get(z).getData() == null) {
										linkBoxes.get(z).setData(mCurrentLink.toString());
									}
								}
								mCurrentLink.setLength(0);
								doingLink = false;
							} else {
								doingLink = true;
								mCurrentLink.append(text.getString());
								
								
								Rect r = new Rect();
								r.left = (int) x;
								r.top = (int) (y - p.getTextSize());
								r.right = (int) (x + p.measureText(text.getString()));
								r.bottom = (int) (y + 5);
								if (mLinkMode == LINK_MODE.BACKGROUND) {
									linkColor.setColor(mLinkHighlightColor);
									c.drawRect(r.left, r.top, r.right, r.bottom, linkColor);
								}
								
								int linkBoxHeightDips = (int) ((r.bottom - r.top) / this.getResources().getDisplayMetrics().density);
								if (linkBoxHeightDips < mLinkBoxHeightMinimum) {
									int additionalAmount = (mLinkBoxHeightMinimum - linkBoxHeightDips) / 2;
									if (additionalAmount > 0) {
										r.top -= additionalAmount * this.getResources().getDisplayMetrics().density;
										r.bottom += additionalAmount * this.getResources().getDisplayMetrics().density;
									}
								}
								
								LinkBox linkbox = new LinkBox(null, r);
								linkBoxes.add(linkbox);
								
							}
						}
						if (doingLink) {
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
								if (mSelectedColor == 37) {
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
							
							if (doIndicator) {
								int unitEndCol = workingcol + (text.bytecount - 1);
								if (unitEndCol > selectedSelector.column - 10 && workingcol < selectedSelector.column + 10) {
									float size = p.getTextSize();
									p.setTextSize(30);
									int overshoot = workingcol - selectedSelector.column;
									int ix = 0, iy = mSelectionIndicatorFontSize;
									ix = (int) (mSelectionIndicatorHalfDimension + (overshoot * mSelectionCharacterWidth) - 0.5 * mSelectionCharacterWidth);
									iy = (int) (mSelectionIndicatorHalfDimension + (0.5 * mSelectionIndicatorFontSize)) + (indicatorlineoffset * mSelectionIndicatorFontSize);
									mSelectionIndicatorCanvas.drawText(text.getString(), ix, iy, p);
									p.setTextSize(size);
								}
								
							}
							c.drawText(text.getString(), x, y, linkColor);
							x += p.measureText(text.getString());
							
						} else {
							
							if (doIndicator) {
								int unitEndCol = workingcol + (text.bytecount - 1);
								if (unitEndCol > selectedSelector.column - 10 && workingcol < selectedSelector.column + 10) {
									float size = p.getTextSize();
									p.setTextSize(30);
									int overshoot = workingcol - selectedSelector.column;
									int ix = 0 , iy = mSelectionIndicatorFontSize;
									ix = (int) (mSelectionIndicatorHalfDimension + (overshoot * mSelectionCharacterWidth) - 0.5 * mSelectionCharacterWidth);
									iy = (int) (mSelectionIndicatorHalfDimension + (0.5 * mSelectionIndicatorFontSize)) + (indicatorlineoffset * mSelectionIndicatorFontSize);
									mSelectionIndicatorCanvas.drawText(text.getString(), ix, iy, p);
									p.setTextSize(size);
								}
								
							}
							workingcol += text.bytecount;
							c.drawText(text.getString(), x, y, p);
							x += p.measureText(text.getString());
						}
						
						break;
					case COLOR:
						mXterm256Color = false;
						mXterm256FGStart = false;
						mXterm256BGStart = false;
						for (int i = 0; i < ((TextTree.Color) u).getOperations().size(); i++) {
							updateColorRegisters(((TextTree.Color) u).getOperations().get(i));
						}
						
						if (mColorDebugMode == 2 || mColorDebugMode == 3) {
							p.setColor(0xFF000000 | Colorizer.getColorValue(0, 37,false));
							b.setColor(0xFF000000 | Colorizer.getColorValue(0, 40,false));
						} else {
							if (mXterm256FGStart) {
								if (mSelectedColor == 33) {
									mSelectedColor = 33;
								}
								p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor, mXterm256Color));
							} else {
								if (!mXterm256BGStart) {
									p.setColor(0xFF000000 | Colorizer.getColorValue(mSelectedBright, mSelectedColor, false));
								}
							}
							
							if (mXterm256BGStart) {
								b.setColor(0xFF000000 | Colorizer.getColorValue(0, mSelectedBackground,mXterm256Color));
							} else {
								b.setColor(0xFF000000 | Colorizer.getColorValue(0, mSelectedBackground,false));
								
							}
						}
						if (mColorDebugMode == 1 || mColorDebugMode == 2) {
							String str = "";
							try {
								str = new String(((TextTree.Color) u).bin,"ISO-8859-1");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							c.drawText(str, x, y, p);
							x += p.measureText(str);
						}
						break;
					case NEWLINE:
					case BREAK:
						if (u instanceof TextTree.NewLine) {
							if (doingLink) {
								for (int z = 0; z < linkBoxes.size(); z++) {
									if (linkBoxes.get(z).getData() == null) {
										linkBoxes.get(z).setData(mCurrentLink.toString());
									}
								}
								mCurrentLink.setLength(0);
								doingLink = false;
								//REGISTER LINK BOX
							}
						} else if (u instanceof TextTree.Break) {
							workingline = workingline -1;
							if (startline2 == endline && startline2 == workingline) {
								linemode = 1;
							} else if (startline2 == workingline) {
								linemode = 2;
							} else if (startline2 > workingline && endline < workingline) {
								
								linemode = 3;
							} else if (endline == workingline) {
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
						if (drawnlines > mCalculatedLinesInWindow + extraLines) {
							stop = true;
						}
						break;
					default:
						break;
					}
				}
				if (!finishedWithNewLine) {
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
			if (Math.abs(mFlingVelocity) > mPrefLineSize) {
				if (!mHandler.hasMessages(MESSAGE_DRAW)) {
					this.mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW, 3);
				}
			} else {
				mFlingVelocity = 0;
			}
		
		}
		
		//phew, do the lua stuff, and lets be done with this.
		c.save();
		if (mHasDrawRoutine) {
			if (mL != null) {
				
/*! \page entry_points
 * \subsection OnDraw OnDraw
 * This function is called whenever the window is dirty and needs to redraw custom content.
 * 
 * \param canvas
 * 
 * \note It is difficult to know exactly what needs to be freed for garbage collection, how to do it, and weather or not it worked. 
 * A good example is the button window, it has many custom resources and I had run into memory issues with it when closing/opening the window a few times. 
 * It may never happen, it may happen after 100 open/close cycles, or 5, but the general trend of running the foreground process out of memory is an immediate termination of the window. 
 * So if you are in a case where you are coming back into the appliation after a phone call or web browser and it immediatly exits, this may be the culprit.
 */
				
				mL.getGlobal("debug");
				mL.getField(mL.getTop(), "traceback");
				mL.remove(TOP_MINUS_TWO);
				
				
				mL.getGlobal("OnDraw");
				if (mL.isFunction(mL.getTop())) {
					mL.pushJavaObject(c);
					int ret = mL.pcall(1, 1, TOP_MINUS_THREE);
					if (ret != 0) {
						displayLuaError("Error calling OnDraw: " + mL.getLuaObject(-1).toString());
					} else {
						mL.pop(2);
					}
				} else {
					mHasDrawRoutine = false;
					mL.pop(2);
				}
			}
		}
		
		c.restore();
	}
	
	/** Utility class to keep track of a drawn link's hitbox and link info. */
	private class LinkBox {
		/** The link data (url). */
		private String mData;
		/** The hitbox in view coordinates. */
		private Rect mBox;
		/** Public constructor.
		 * 
		 * @param link Link data.
		 * @param rect Hitbox. 
		 */
		public LinkBox(final String link, final Rect rect) {
			//this.mData = link;
			this.mBox = rect;
		}
		/** Setter for data. 
		 * 
		 * @param data The data.
		 */
		public void setData(final String data) {
			this.mData = data;
		}
		/** Getter for data.
		 * 
		 * @return The data.
		 */
		public String getData() {
			return mData;
		}
		/** Getter for the hitbox. 
		 * 
		 * @return The hitbox.
		 */
		public Rect getBox() {
			return mBox;
		}
	}
	
	/** Draws the scroller rectangle (and I think the selection box. 
	 * 
	 * @param c The canvas to draw on.
	 */
	public final void showScroller(final Canvas c) {
		mScrollerPaint.setColor(0xFFFF0000);
		
		if (mBuffer.getBrokenLineCount() < 1) {
			return; //no scroller to show.
		}
		
		if (mScrollback > SCROLL_MIN + 3 * mDensity && mBuffer.getBrokenLineCount() > mCalculatedLinesInWindow) {
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
		if (windowPercent > 1) {
			//then we have but 1 page to show
			return;
		} else {
			scrollerSize = windowPercent * workingHeight;
			posPercent = (mScrollback - (workingHeight / 2)) / (mBuffer.getBrokenLineCount() * mPrefLineSize);
			scrollerPos = workingHeight * posPercent;
			scrollerPos = workingHeight - scrollerPos;
		}
		
		int blueValue = (int) (-1*255*posPercent + 255);
		int redValue = (int) (255 * posPercent);
		int alphaValue = (int) ((255 - 70) * posPercent + 70);
		int finalColor = android.graphics.Color.argb(alphaValue, redValue, 100, blueValue);
		mScrollerPaint.setColor(finalColor);
		float density = this.getResources().getDisplayMetrics().density;
		scrollerRect.set((int) workingWidth - (int) (2 * density), (int) (scrollerPos - scrollerSize / 2), (int) workingWidth, (int) (scrollerPos + scrollerSize / 2));
		
		c.drawRect(scrollerRect, mScrollerPaint);
		
		if (theSelection != null) {
			//compute rects for the guys.
			//compute the current line in pixels from the bottom of the screen.
			int currentLine = theSelection.start.line * mPrefLineSize;
			currentLine = (int) (currentLine - (mScrollback - SCROLL_MIN));
			
			
			int startBottom = (int) (this.getHeight() - currentLine);
			int startTop = startBottom - mPrefLineSize;
			int startLeft = theSelection.start.column * mOneCharWidth;
			int startRight = startLeft + mOneCharWidth;
			
			currentLine = theSelection.end.line * mPrefLineSize;
			currentLine = (int) (currentLine - (mScrollback - SCROLL_MIN));
			
			int endBottom = (int) (this.getHeight() - currentLine);
			int endTop = endBottom - mPrefLineSize;
			int endLeft = theSelection.end.column * mOneCharWidth;
			int endRight = endLeft + mOneCharWidth;
			
			//int scroll_from_bottom = (int) (scrollback-SCROLL_MIN);
			
			c.drawRect(startLeft, startTop - 2, startRight, startBottom - 2, mTextSelectionIndicatorPaint);
			c.drawRect(endLeft, endTop - 2, endRight, endBottom - 2, mTextSelectionIndicatorPaint);
			
			int x = 0, y = 0;
			if (selectedSelector == theSelection.end) {
				x = endLeft + (endRight - endLeft) / 2;
				y = endTop + (endBottom - endTop) / 2;
			} else {
				x = startLeft + (startRight - startLeft) / 2;
				y = startTop + (startBottom - startTop) / 2;
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
			
			mSelectionIndicatorCanvas.drawPath(mSelectionIndicatorClipPath, edgePaint);
			
			//draw the cancel, start, end and copy buttons.
			Paint cancelPaint = new Paint();
			cancelPaint.setStyle(Paint.Style.FILL);
			cancelPaint.setAntiAlias(true);
			cancelPaint.setColor(0xFFFF0000);
			int third = (mSelectionIndicatorHalfDimension * 2 ) / 3;
			mSelectionIndicatorCanvas.drawBitmap(mTextSelectionCopyBitmap, 0, 0, null);
			mSelectionIndicatorCanvas.drawBitmap(mTextSelectionCancelBitmap, 0, 2 * third, null);
			mSelectionIndicatorCanvas.drawBitmap(mTextSelectionSwapBitmap, 2 * third, 0, null);
			
			
			float left = (float) (mSelectionIndicatorHalfDimension - (0.5 * mSelectionCharacterWidth));
			float top = (float) (mSelectionIndicatorHalfDimension - (0.5 * mSelectionIndicatorFontSize));
			float right = (float) (mSelectionIndicatorHalfDimension + (0.5 * mSelectionCharacterWidth));
			float bottom = (float) (mSelectionIndicatorHalfDimension + (0.5 * mSelectionIndicatorFontSize));
			
			c.drawBitmap(mSelectionIndicatorBitmap, mWidgetX - mSelectionIndicatorHalfDimension, mWidgetY - mSelectionIndicatorHalfDimension, null);
			c.drawRect(left + (mWidgetX - mSelectionIndicatorHalfDimension), 
					top + (mWidgetY - mSelectionIndicatorHalfDimension), 
					right + (mWidgetX - mSelectionIndicatorHalfDimension),
					bottom + (mWidgetY - mSelectionIndicatorHalfDimension), 
					mScrollerPaint);		
		}
		
	}

	/** Clears all text from the buffer. */
	public final void clearText() {
		mBuffer.dumpToBytes(false);
		mBuffer.prune();
	}
	
	/** If the window was in buffering mode, this function will dump the buffered text into the real buffer. */
	public final void flushBuffer() {
		try {
			mBuffer.addBytesImpl(mHoldBuffer.dumpToBytes(false));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		mBuffer.prune();
		drawingIterator = null;
		this.invalidate();
	}

	/** If the window has been scrolled back, this function will return it to home. */
	public final void jumpToZero() {
		synchronized (mToken) {
			SCROLL_MIN = mHeight - (double) (5 * Window.this.getResources().getDisplayMetrics().density);
			mScrollback = SCROLL_MIN;
			mFlingVelocity = 0;
		}
	}

	/** This is kind of a hack function, schedule a redraw for i milliseconds.
	 * 
	 * @param i The number of milliseconds to wait before drawing.
	 */
	public final void doDelayedDraw(final int i) {
		if (!mHandler.hasMessages(MESSAGE_DRAW)) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_DRAW, i);
		}
	}

	/** Sets the color debug mode for the window. */
	public void setColorDebugMode(int i) {
		mColorDebugMode = i;
		doDelayedDraw(1);
	}

	/** Sets the active encoding for the window. */
	public void setEncoding(String pEncoding) {	
		mBuffer.setEncoding(pEncoding);
		mHoldBuffer.setEncoding(pEncoding);
		
	}

	public void setCharacterSizes(int fontSize, int fontSpaceExtra) {
		mPrefFontSize = fontSize;
		mPrefLineExtra = fontSpaceExtra;
		mPrefLineSize = (int) (mPrefFontSize + mPrefLineExtra);
		calculateCharacterFeatures(mWidth,mHeight);
	}

	public void setMaxLines(int maxLines) {
		mBuffer.setMaxLines(maxLines);
		mHoldBuffer.setMaxLines(maxLines);
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
				mScrollback = SCROLL_MIN;
				//mHandler.sendEmptyMessage(MSG_CLEAR_NEW_TEXT_INDICATOR);
			} else {
				if(mBuffer.getBrokenLineCount() <= mCalculatedLinesInWindow) {
					mScrollback = (double)mHeight;
				} else {
					if(mScrollback > SCROLL_MIN + mPrefLineSize ) {
						//scrollback = oldposition * (the_tree.getBrokenLineCount()*PREF_LINESIZE);
						double new_max = mBuffer.getBrokenLineCount()*mPrefLineSize;
						int lines = (int) ((new_max - old_max)/mPrefLineSize);
						
						mScrollback += linesadded*mPrefLineSize;
						//Log.e("BYTE",mName+"REPORT: old_max="+old_max+" new_max="+new_max+" delta="+(new_max-old_max)+" scrollback="+scrollback + " lines="+lines + " oldbroken="+oldbrokencount+ "newbroken="+the_tree.getBrokenLineCount());
						
					} else {
						mScrollback = SCROLL_MIN;
					}
				
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
		String launchPath = mParent.getPathForPlugin(mOwner);
		if(mDataDir == null) {
			//this is bad.
		} else {
			
			//set up the path/cpath.
			//TODO: add the plugin load path.
			String packagePath = mDataDir + "/lua/share/5.1/?.lua";
			if(launchPath != null && !launchPath.equals("")) {
				File file = new File(launchPath);
				String dir = file.getParent();
				//file.getPar
				//L.pushString(dir);
				packagePath += ";" + dir + "/?.lua";
			}
			mL.getGlobal("package");
			mL.pushString(packagePath);
			mL.setField(-2, "path");
			
			mL.pushString(mDataDir + "/lua/lib/5.1/?.so");
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
	

	
	/*! \page page1
\section window Window Functions
\subsection AddOptionCallback AddOptionCallback
Add a top level menu item that will call a global function when pressed.

\par Full Signature
\luacode
AddOptionCallback(functionName,menuText,iconDrawable)
\endluacode
\param functionName \b string value of the function name that will be called when the menu item is pressed.
\param menuText \b string value that will appear on the menu item.
\param iconDrawable \b android.graphics.drawable.Drawable the drawable resource that will be used for the icon.
\returns nothing
\par Example with no icon
\luacode
AddOptionCallback("functionName","Click Me!",nil)
\endluacode
\par Example with icon
\luacode
drawable = luajava.newInstance("android.drawable.BitmapDrawable",context:getResources(),"/path/to/image.png")
function menuClicked()
	Note("Menu Item Clicked!")
end

AddOptionCallback("menuClicked","Click Me!",drawable)
\endluacode

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
\subsection sec4 CancelCallback
Cancel a scheduled call made with ScheduleCallback.
\note This will cancel all pending callbacks with the given identifier.

\par Full Signature
\luacode
CancelCallback(id)
\endluacode
\param id \b number the callback id to cancel
\returns nothing
\par Example 
\luacode
CancelCallback(100)
\endluacode
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
\subsection sec16 CloseOptionsDialog
Closes the Options dialog if it is currently open.

\par Full Signature
\luacode
CloseOptionsDialog()
\endluacode
\param none
\returns nothing
\par Example 
\luacode
CloseOptionsDialog()
\endluacode
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
\subsection sec0 GetActivity
Get a handle to the current Activity that is hosting the foreground window process.

\par Full Signature
\luacode
GetActivity()
\endluacode
\param none
\returns \b android.app.Activity the current Activity that is hosting the foreground processes.
\par Example 
\luacode
activity = GetActivity()
\endluacode
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
\subsection sec14 IsStatusBarHidden
Gets the state of the status bar.

\par Full Signature
\luacode
IsStatusBarHidden()
\endluacode
\param none
\returns \b bool true if the status bar is hidden (full screen), false if the status bar is being shown (non full screen)
\par Example 
\luacode
if(IsStatusBarHidden()) then
 Note("status bar hidden")
else
 Note("status bar not hidden")
end
\endluacode
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
\subsection PluginXCallS PluginXCallS
Calls a function in the parent plugin's Lua state. Provides one way signaling across the AIDL bridge to the plugin host running in the background.

\par Full Signature
\luacode
PluginXCallS(functionName,data)
\endluacode
\param functionName \b string the global function in the plugin's host Lua state.
\param data \b string the data to pass as a argument to the given function
\returns nothing
\par Example 
\luacode
PluginXCallS("saveData","300")
\endluacode
\note Tables can be serialized to a string and reconstituted in the plugin using loadstring(...) but the performance may suffer if the tables are large. See PluginXCallB for a slightly faster method of communication that doesn't involve the heavy Java string manipulation.
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
\subsection sec12 PopMenuStack
Removes the current menu item and returns the menu stack to its previous state.

\par Full Signature
\luacode
PopMenuStack()
\endluacode
\param none
\returns nothing
\par Example 
\luacode
PopMenuStack()
\endluacode
\see PushMenuStack
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
\subsection sec11 PushMenuStack
Starts the creating of a new menu object, providing a global function name to call that will handle weather or not the back button is pressed while the menu stack is active.
After this function is called, the [PopulateMenu](entry_points.html#PopulateMenu) entry point will be called when the operating system has created the new menu to show.


\par Full Signature
\luacode
PushMenuStack(callbackName)
\endluacode
\param \b string the name of a global function to call if the back button is pressed to cancel the menu.
\returns nothing
\par Example 
\luacode
function PopulateMenu(menu)
 menu:addItem(0,1,1,foo)
end
PushMenuStack("menuBackPressed")

function menuBackPressed()
	PopMenuStack()
end
\endluacode
\see this relies largely on the Android Menu and MenuItem classes, please refer to the documentation and other menu related sample code.
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
\subsection sec3 ScheduleCallback
Add a top level menu item that will call a global function when pressed.

\par Full Signature
\luacode
ScheduleCallback(id,callbackName,delayMillis)
\endluacode
\param id \b number unique identifier associated with this event, will be passed to the callback.
\param callbackName \b string name of the global function to call after the desired elapsed time.
\param delayMillis \b dumber how long in milliseconds to delay the execution of the callback
\returns nothing
\par Example
\luacode
function delayCallback(id)
 Note(string.format("event %d fired.",id))
end

ScheduleCallback(100,"delayCallback",3000)
ScheduleCallback(104,"delayCallback",5000)
\endluacode
\tableofcontents
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
	
  /*! \page page1
\subsection PluginInstalled PluginInstalled
Checks whether a plugin is installed.

\par Full Signature
\luacode
PluginInstalled(name)
\endluacode
\param name \b the plugin name to test.
\returns \b boolean whether or not the plugin is installed.
\par Example 
\luacode
if(PluginInstalled("button_window")) then
	WindowCall("button_window","clearButtons")
end
\endluacode
	*/
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

  /*! \page page1
\subsection WindowBroadcast WindowBroadcast
Calls a named global function in every window (if the window has defined it).

\par Full Signature
\luacode
WindowBroadcast(function,arg)
\endluacode
\param function \b the function to call.
\param arg \b string a string or number to provide to the function as an argument.
\returns nothing
\par Example 
\luacode
WindowBroadcast("adjustZOrder","now")
\endluacode
	*/
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
  /*! \page page1
\subsection WindowCall WindowCall
Calls a named global function on the target window.

\par Full Signature
\luacode
WindowCall(name,function,arg)
\endluacode
\param name \b string the name of the window to target.
\param function \b the function to call.
\param arg \b string a string or number to provide to the function as an argument.
\returns nothing
\par Example 
\luacode
WindowCall("button_window","loadButtonSet","default")
\endluacode
\see WindowSupports to test whether or not it has a global function of a desired name.
	*/
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
	  /*! \page page1
\subsection WindowSupports WindowSupports
Tests whether a named global function exists in the target window.

\par Full Signature
\luacode
WindowSupports(name,function)
\endluacode
\param name \b string the name of the window to target.
\param function \b the function to test.
\returns \b boolean true if the window has a global function named \b function, false if not.
\par Example 
\luacode
if(WindowSupports("button_window","clearButtons")) then
	WindowCall("button_window","clearButtons")
end
\endluacode
\tableofcontents
		*/
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
		BaseOption o = (BaseOption) mSettings.findOptionByKey(key);
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
			
			y += (mScrollback-SCROLL_MIN);
			
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
						int modx = (int) x - (mWidgetX - mSelectionIndicatorHalfDimension);
						int mody = (int) event.getY() - (mWidgetY - mSelectionIndicatorHalfDimension);
						if(mSelectionIndicatorRect.contains(modx,mody)) {
							
							//int newx = (int) (x - selectionIndicatorRect.left);
							//int newy = (int) (mody - selectionIndicatorRect.top);
							
							int full = mSelectionIndicatorHalfDimension * 2;
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
									int remainder = ((int)(mScrollback-SCROLL_MIN) % mPrefLineSize)-mPrefLineSize;
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
					if(Math.abs(widgetCenterMovedX) > mSelectionCharacterWidth) {
						int sign = (int)Math.signum(widgetCenterMovedX);
						if(sign > 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
							doScrollRight(false);
						} else if(sign < 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
							doScrollLeft(false);
						}
//						selectedSelector.column += 1 * Math.signum(widgetCenterMovedX);
//						selectorCenterX += one_char_is_this_wide * Math.signum(widgetCenterMovedX);
//						calculateWidgetPosition(selectorCenterX,selectorCenterY);
						widgetCenterMovedX = 0;
						v.invalidate();
					}
					if(Math.abs(widgetCenterMovedY) > mSelectionIndicatorFontSize) {
						int sign = (int)Math.signum(widgetCenterMovedY);
						if(sign > 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
							doScrollUp(false);
						} else if(sign < 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
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
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
							doScrollRight(false);
						} else if(sign < 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
							doScrollLeft(false);
						}
						widgetCenterMovedX = 0;
						v.invalidate();
					} 
					
					if(Math.abs(widgetCenterMovedY) > mSelectionIndicatorFontSize) {
						int sign = (int) Math.signum(widgetCenterMovedY);
						if(sign > 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
							doScrollUp(false);
						} else if(sign < 0) {
							mScrollRepeatRate = SCROLL_REPEAT_RATE;
							mScrollRepeatRateStep = 1;
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
					mScrollRepeatRate = SCROLL_REPEAT_RATE;
					mScrollRepeatRateStep = 1;
					
					int mod2x = (int) x - (mWidgetX - mSelectionIndicatorHalfDimension);
					int mod2y = (int) event.getY() - (mWidgetY - mSelectionIndicatorHalfDimension);
					if(mSelectionIndicatorRect.contains(mod2x,mod2y)) {
						
						//int newx = (int) (x - (widgetX - selectionIndic);
						//int newy = (int) (event.getY() - selectionIndicatorRect.top);
						
						int full = mSelectionIndicatorHalfDimension * 2;
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
	
	private int mWidgetX = 0;
	private int mWidgetY = 0;
	
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
		
		if((newWidgetX + (mSelectionIndicatorHalfDimension)) > this.getWidth()) {
			selectionIndicatorVectorX -= mOneCharWidth;
			if(selectionIndicatorVectorX < (mOneCharWidth + mSelectionIndicatorHalfDimension)) {
				selectionIndicatorVectorX = -(selectionIndicatorVectorX+mOneCharWidth);
			}
			newWidgetX = (int) (startX + selectionIndicatorVectorX);
			
		} else if((newWidgetX - mSelectionIndicatorHalfDimension) < 0) {
			//flip the vector
			selectionIndicatorVectorX += mOneCharWidth;
			if(selectionIndicatorVectorX > -(mOneCharWidth+mSelectionIndicatorHalfDimension)) {
				selectionIndicatorVectorX = -(selectionIndicatorVectorX-mOneCharWidth);
			}
			newWidgetX = (int) (startX + selectionIndicatorVectorX);
		}
		
		if((newWidgetY + (mSelectionIndicatorHalfDimension)) > this.getHeight()) {
			selectionIndicatorVectorY -= mPrefLineSize;
			//only if we run into 
			newWidgetY = (int) (startY + selectionIndicatorVectorY);
			if(newWidgetY + mSelectionIndicatorHalfDimension > this.getHeight()) {
				selectionIndicatorVectorY = -mSelectionIndicatorHalfDimension;
				newWidgetY = (int) (startY + selectionIndicatorVectorY);
			}
			
		} else if ((newWidgetY - mSelectionIndicatorHalfDimension) < 0) {
			selectionIndicatorVectorY += mPrefLineSize;
			
			newWidgetY = (int) (startY + selectionIndicatorVectorY);
			if(newWidgetY - mSelectionIndicatorHalfDimension < 0) {
				selectionIndicatorVectorY = +mSelectionIndicatorHalfDimension;
				newWidgetY = (int) (startY + selectionIndicatorVectorY);
			}
		}
		
		mWidgetX = newWidgetX;
		mWidgetY = newWidgetY;
		
	}
	
	private void moveWidgetToSelector(TextTree.SelectionCursor cursor) {
		
		int part1 = (int) (selectedSelector.line * mPrefLineSize + (0.5*mSelectionIndicatorFontSize));
		//int part2 = (int) (scrollback);
		int part2 = (int) (selectedSelector.line * mPrefLineSize - (0.5*mSelectionIndicatorFontSize));
		
		
		if(part1 > mScrollback) {
			//calculate how much scroll to go to get to be true.
			//int tmp = (int) (scrollback-SCROLL_MIN);
			//int howmuch = part2 - part1;
			mScrollback = (double) part1;
		} else if(part2 < (mScrollback-SCROLL_MIN)) {
			mScrollback -= ((mScrollback-SCROLL_MIN) - part2);
		}
		
		int endx = (int) ((selectedSelector.column * mOneCharWidth) + (0.5*mOneCharWidth));
		int endy = (int) ((this.getHeight() - ((selectedSelector.line * mPrefLineSize) + (0.5*mSelectionIndicatorFontSize) - (mScrollback-SCROLL_MIN))));
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
 * When the foreground process is being terimnated normally; used for memory management (freeing custom bitmaps, data, stuff that needs to be garbage collected).
 * 
 * \param none
 * 
 * \note It is difficult to know exactly what needs to be freed for garbage collection, how to do it, and wheather or not it worked. See the button window script for an example demonstrating use. The button window has many custom resources and I had run into memory issues with it when closing/opening the window a few times. It may never happen, it may happen after 100 open/close cycles, or 5, but the general trend of running the foreground process out of memory is an immediate termination of the window. So if you are in a case where you are coming back into the BlowTorch after a phone call or web browser session and it immediatly exits, this may be the culprit.
 */
		
	public void shutdown() {
		//Log.e("LUAWINDOW","SHUTTING DOWN: "+mName);
		if(mL == null) return;
		//call into lua to notify shutdown imminent.
		
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
		
		Log.e("WINDOW","   Calling OnDestroy for state: " + mL.getStateId());
		
		mL.getGlobal("OnDestroy");
		Log.e("WINDOW"," getting global");
		if(mL.getLuaObject(mL.getTop()).isFunction()) {
			Log.e("WINDOW"," is function");
			int ret = mL.pcall(0, 1, -2);
			if(ret != 0) {
				Log.e("WINDOW","LUA ERROR:" + mL.getLuaObject(-1).getString());
				displayLuaError("Error in OnDestroy: "+mL.getLuaObject(-1).getString());
			} else {
				Log.e("window","poping lua stack");
				mL.pop(2);
			}
		} else {
			//no method.
			mL.pop(2);
		}
		
		//callbackHandler.removeCallbacksAndMessages(token)
		
		//mL = null;
		
	}
	
	public void closeLua() {
		if(mL != null) {
			mL.close();
			mL = null;
		}
	}
	
	@Override
	protected final void onMeasure(final int widthSpec, final int heightSpec) {
		int height = MeasureSpec.getSize(heightSpec);
		int width = MeasureSpec.getSize(widthSpec);
		
/*! \page entry_points
 * \subsection OnMeasure OnMeasure
 * Whenever the layout hierarchy initiates re-measuring (window hierarchy changed) this function is called, many times. There is much to know about this function. More documentation will come, but the information passed in the variables is called a measure spec. It contains the target dimension and the measurement mode. More information can be found here. <insert link>
 * 
 * \param widthspec
 * \param heightspec
 * 
 * \return width and height, see note
\par Example
\luacode
function OnMeasure(wspec,hspec)	
	if(wspec == measurespec_width and hspec == measurespec_height) then return measured_width,measured_height end
	--Note(string.format("measurespecs: %d, %d\n",wspec,hspec))
	measurespec_width = wspec
	measurespec_height = hspec
	measured_width = MeasureSpec:getSize(wspec)
	--local wmode = MeasureSpec:getMode(wspec)
	
	measured_height = MeasureSpec:getSize(hspec)
	--local hmode = MeasureSpec:getMode(hspec)
	
	function test()
		local orientation = view:getParent():getOrientation()
	end
	local ret,err = pcall(test,debug.traceback)
	if(not ret) then
		--there was a problem, but do we care
		--Note("stat widget is relative, width:"..measured_width.." height:"..measured_height.."\n")
		return measured_width,measured_height
	end
	
	
	local orientation = view:getParent():getOrientation()
	if(orientation == LinearLayout.VERTICAL) then
		view:fitFontSize(36)
		view:doFitFontSize(measured_width)
		measured_height = view:getLineSize()*view:getBuffer():getBrokenLineCount()
		
		--Note("stat widget is vertical, width:"..measured_width.." height:"..measured_height.."\n")
		return measured_width,measured_height
	else
		view:setCharacterSizes((measured_height-6)/3,2)
		measured_width = 37*view:measure("a")
		view:fitFontSize(-1)
		measured_height = view:getLineSize()*view:getBuffer():getBrokenLineCount()
		--Note("stat widget is horizontal, width:"..measured_width.." height:"..measured_height.."\n")
		return measured_width,measured_height
	end
	--end
	
	

end
\endluacode
 * 
 * \note This function expects a measured width and height value returned, e.g. return width,height is expected. If it is not supplied the window will not appear. 
 */
		if (mHasScriptOnMeasure && mL != null) {
			mL.getGlobal("debug");
			mL.getField(-1, "traceback");
			mL.remove(TOP_MINUS_TWO);
			
			mL.getGlobal("OnMeasure");
			if (mL.isFunction(-1)) {
				mL.pushNumber(widthSpec);
				mL.pushNumber(heightSpec);
				int ret = mL.pcall(2, 2, TOP_MINUS_FOUR);
				if (ret != 0) {
					displayLuaError("Error in OnMeasure:" + mL.getLuaObject(-1).getString());
					setMeasuredDimension(1, 1);
					mL.pop(1);
					return;
				} else {
					int retHeight = (int) mL.getLuaObject(-1).getNumber();
					int retWidth = (int) mL.getLuaObject(TOP_MINUS_TWO).getNumber();
					mL.pop(2);
					setMeasuredDimension(retWidth, retHeight);
					return;
				}
			} else {
				mL.pop(2);
			}
		}
		int hspec = MeasureSpec.getMode(heightSpec);
		if (width != mWidth) {
			doFitFontSize(width);
		}
		switch(hspec) {
		case MeasureSpec.AT_MOST:
			break;
		case MeasureSpec.EXACTLY:
			break;
		case MeasureSpec.UNSPECIFIED:
			height = (mBuffer.getBrokenLineCount() * mPrefLineSize) + mPrefLineExtra;
			break;
		default:
			break;
		}
		
		setMeasuredDimension(width, height);
		
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
		if(mScrollback == SCROLL_MIN) {
			SCROLL_MIN = mHeight-(double)(5*Window.this.getResources().getDisplayMetrics().density);
			mScrollback = SCROLL_MIN;
		} else {
			//we have to calculate the new scrollback position.
			double oldmin = SCROLL_MIN;
			SCROLL_MIN = mHeight-(double)(5*Window.this.getResources().getDisplayMetrics().density);
			mScrollback -= oldmin - SCROLL_MIN;
		}
		
		//if(the_tree.getBrokenLineCount() <= CALCULATED_LINESINWINDOW) {
		//	scrollback = 0.0;
		//}

		
		mHomeWidgetRect.set(mWidth-mHomeWidgetDrawable.getWidth(),mHeight-mHomeWidgetDrawable.getHeight(),mWidth,mHeight);
		
		Float foo = new Float(0);
		//foo.
		
/*! \page entry_points
 * \subsection OnSizeChanged OnSizeChanged
 * If the window's size changes this function is called.
 * 
 * \param new width
 * \param new height
 * \param old width
 * \param old height
 * 
 * \return none
\par Example
\luacode
function OnSizeChanged(w,h,oldw,oldh)
	Note("Window starting OnSizeChanged()")
	if(w == 0 and h == 0) then
		draw = false
	end
end
\endluacode
 * 
 */
		
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
	
	private void doScrollDown(boolean repeat) {
		//Log.e("FOO","do scroll down");
		selectedSelector.line -= 1;
		if(selectedSelector.line < 0) {
			selectedSelector.line = 0;
			repeat = false;
		} else {
			int remainder = ((int)(mScrollback-SCROLL_MIN) % mPrefLineSize) + mPrefLineSize;
			selectorCenterY += mPrefLineSize;
			if(selectorCenterY > this.getHeight() - remainder) {
				selectorCenterY -= mPrefLineSize;
				mScrollback -= mPrefLineSize;
			}
			calculateWidgetPosition(selectorCenterX,selectorCenterY);
		}
	
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLDOWN,mScrollRepeatRate);
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
			int remainder = ((int)(mScrollback-SCROLL_MIN) % mPrefLineSize)-mPrefLineSize;
			selectorCenterY -= mPrefLineSize;
			if(selectorCenterY - (mPrefLineSize) < remainder) {
				selectorCenterY = selectorCenterY + mPrefLineSize;
				mScrollback += mPrefLineSize;
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
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLUP,mScrollRepeatRate);
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
			mWidgetY -= mOneCharWidth;
			calculateWidgetPosition(selectorCenterX,selectorCenterY);
		}
		this.invalidate();
		if(repeat) {
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLLEFT,mScrollRepeatRate);
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
			mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLLRIGHT, mScrollRepeatRate);
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
		mScrollback = SCROLL_MIN;
		mFlingVelocity=0;
		this.invalidate();
	}



	
	public void populateMenu(Menu menu) {
		if(mL == null) return;
		mL.getGlobal("debug");
		mL.getField(-1, "traceback");
		mL.remove(-2);
/*! \page entry_points
 * \subsection PopulateMenu PopulateMenu
 * Called during the activity creation process. [I think] Before OnCreate is called, but after the plugin windows have been loaded and the script bodies run.
 * 
 * \param menu android.menu.Menu that is the menu for the foreground window activity.
\par Example
\luacode
menucallback = {}

function menucallback.onMenuItemClick(item)
	Note("menu item clicked")
	
	--this function must return true if it consumes the click event.
	return true
end
menucallback_cb = luajava.createProxy("android.view.MenuItem$OnMenuItemClickListener",menucallback)

 
function PopulateMenu(menu)
 	--see android Menu documentation, menu:add() returns an android.menu.MenuItem
 	--that can be manipulated to have a drawable (more sample code coming soon)
 	--and can be configured for Android 4.0 (ICS)+ features.
 	item = menu:add(0,401,401,"Ex Button Sets")
	item:setOnMenuItemClickListener(buttonsetMenuClicked_cb)
end
 
\endluacode
 * \note Need some example code. It is necessary to create and attach menu items to the top level menu. This is how the button window script attaches its menu item into the top level list.
 *  \tableofcontents
 */
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

