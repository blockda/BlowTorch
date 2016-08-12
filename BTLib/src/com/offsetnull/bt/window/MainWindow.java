package com.offsetnull.bt.window;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
//import android.util.Log;
//import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.IConnectionBinder;
import com.offsetnull.bt.service.IConnectionBinderCallback;
import com.offsetnull.bt.alias.AliasData;
import com.offsetnull.bt.alias.AliasSelectionDialog;
import com.offsetnull.bt.alias.BetterAliasSelectionDialog;
import com.offsetnull.bt.button.ButtonEditorDialog;
import com.offsetnull.bt.button.ButtonSetSelectorDialog;
import com.offsetnull.bt.button.SlickButton;
import com.offsetnull.bt.button.SlickButtonData;
import com.offsetnull.bt.service.*;
import com.offsetnull.bt.service.plugin.settings.BaseOption;
import com.offsetnull.bt.service.plugin.settings.OptionsDialog;
import com.offsetnull.bt.service.plugin.settings.SettingsGroup;
import com.offsetnull.bt.settings.ColorSetSettings;
import com.offsetnull.bt.settings.ConfigurationLoader;
import com.offsetnull.bt.settings.HyperSettings;
import com.offsetnull.bt.settings.HyperSettingsActivity;
import com.offsetnull.bt.speedwalk.BetterSpeedWalkConfigurationDialog;
import com.offsetnull.bt.speedwalk.SpeedWalkConfigurationDialog;
import com.offsetnull.bt.timer.BetterTimerSelectionDialog;
import com.offsetnull.bt.timer.TimerSelectionDialog;
import com.offsetnull.bt.trigger.BetterTriggerSelectionDialog;
import com.offsetnull.bt.trigger.TriggerSelectionDialog;
import android.support.v7.app.AppCompatActivity;

@TargetApi(11)
public class MainWindow extends AppCompatActivity implements MainWindowCallback {
	
	public static String TEST_MODE = "blowTorchTestMode";
	public static String NORMAL_MODE = "blowTorchNormalMode";
	
	//public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	//public String PREFS_NAME;
	private int MAIN_WINDOW_ID = -1;
	protected static final int MESSAGE_HTMLINC = 110;
	protected static final int MESSAGE_RAWINC = 111;
	protected static final int MESSAGE_BUFFINC = 112;
	protected static final int MESSAGE_PROCESS = 102;
	protected static final int MESSAGE_PROCESSED = 104;
	public static final int MESSAGE_SENDDATAOUT = 105;
	protected static final int MESSAGE_RESETINPUTWINDOW = 106;
	protected static final int MESSAGE_PROCESSINPUTWINDOW = 107;
	protected static final int MESSAGE_LOADSETTINGS = 200;
	protected static final int MESSAGE_ADDBUTTON = 201;
	public static final int MESSAGE_MODIFYBUTTON = 202;
	public static final int MESSAGE_NEWBUTTONSET = 205;
	public static final int MESSAGE_CHANGEBUTTONSET = 206;
	public static final int MESSAGE_RELOADBUTTONSET = 208;
	protected static final int MESSAGE_BUTTONREQUESTINGSETCHANGE = 207;
	protected static final int MESSAGE_XMLERROR = 397;
	protected static final int MESSAGE_SAVEERROR = 3993;
	protected static final int MESSAGE_PLUGINSAVEERROR = 3994;
	protected static final int MESSAGE_COLORDEBUG = 675;
	protected static final int MESSAGE_DIRTYEXITNOW = 943;
	protected static final int MESSAGE_DOHAPTICFEEDBACK = 856;
	public static final int MESSAGE_DELETEBUTTONSET = 867;
	public static final int MESSAGE_CLEARBUTTONSET = 868;
	protected static final int MESSAGE_SHOWTOAST = 869;
	protected static final int MESSAGE_SHOWDIALOG = 870;
	public static final int MESSAGE_HFPRESS = 871;
	public static final int MESSAGE_HFFLIP = 872;
	public static final int MESSAGE_LOCKUNDONE = 873;
	public static final int MESSAGE_BUTTONFIT = 874;
	protected static final int MESSAGE_BELLTOAST = 876;
	protected static final int MESSAGE_DOSCREENMODE = 877;
	protected static final int MESSAGE_KEYBOARD = 878;
	protected static final int MESSAGE_DODISCONNECT = 879;
	public static final int MESSAGE_SENDBUTTONDATA = 880;
	private static final int MESSAGE_LINEBREAK = 881;
	private static final int MESSAGE_HIDEKEYBOARD =882;
	protected static final int MESSAGE_CLEARINPUTWINDOW = 883;
	//protected static final int MESSAGE_BUTTONRELOAD = 882;
	protected static final int MESSAGE_CLOSEINPUTWINDOW = 884;
	private static final int MESSAGE_RENAWS = 885;
	public final static int MESSAGE_LAUNCHURL = 886;
	protected static final int MESSAGE_CLEARALLBUTTONS = 887;
	protected static final int MESSAGE_MAXVITALS = 100000;
	//protected static final int MESSAGE_VITALS = 1000001;
	//protected static final int MESSAGE_ENEMYHP = 1000002;
	//protected static final int MESSAGE_VITALS2 = 1000003;
	protected static final int MESSAGE_TESTLUA = 100004;
	protected static final int MESSAGE_TRIGGERSTR = 100005;
	protected static final int MESSAGE_SWITCH = 888;
	/** below is deprecated, remove. */
	protected static final int MESSAGE_RELOADBUFFER = 889;
	protected static final int MESSAGE_INITIALIZEWINDOWS = 890;
	public static final int MESSAGE_ADDOPTIONCALLBACK = 891;
	public static final int MESSAGE_PLUGINXCALLS = 892;
	public static final int MESSAGE_WINDOWBUFFERMAXCHANGED = 893;
	protected static final int MESSAGE_MARKWINDOWSDIRTY = 894;
	protected static final int MESSAGE_MARKSETTINGSDIRTY = 895;
	//private TextTree tree = new TextTree();
	protected static final int MESSAGE_SETKEEPLAST = 896;
	public static final int MESSAGE_PUSHMENUSTACK = 897;
	public static final int MESSAGE_POPMENUSTACK = 898;
	public static final int MESSAGE_DISPLAYLUAERROR = 899;
	protected static final int MESSAGE_USESUGGESTIONS = 900;
	protected static final int MESSAGE_USEFULLSCREENEDITOR = 901;
	protected static final int MESSAGE_SETKEEPSCREENON = 902;
	protected static final int MESSAGE_SETORIENTATION = 903;
	protected static final int MESSAGE_USECOMPATIBILITYMODE = 904;
	protected static final int MESSAGE_DORESETSETTINGS = 905;
	protected static final int MESSAGE_EXPORTSETTINGS = 906;
	public static final int MESSAGE_CLOSEOPTIONSDIALOG = 907;
	public static final int MESSAGE_SHOWREGEXWARNING = 908;
	protected boolean settingsDialogRun = false;
	boolean mHideIcons = true;
	
	private BetterEditText mInputBox = null;
	
	private boolean autoLaunch = true;
	private String overrideHF = "auto";
	private String overrideHFFlip = "auto";
	private String overrideHFPress = "auto";
	
	private boolean isFullScreen = false;
	
	private boolean windowShowing = false;
	private RelativeLayout mRootView = null;
	String host;
	int port;
	
	HashMap<String,com.offsetnull.bt.window.Window> windowMap = null;
	
	Handler myhandler = null;
	//boolean servicestarted = false;
	
	IConnectionBinder service = null;
	Processor the_processor = null;
	private int statusBarHeight = 1;
	//GestureDetector gestureDetector = null;
	OnTouchListener gestureListener = null;
	//ByteView screen2 = null;
	CommandKeeper history = null;
	ImageButton test_button = null;
	ImageButton up_button_c = null;
	ImageButton down_button_c = null;
	ImageButton enter_button_c  = null;
	boolean input_controls_expanded = false;
	boolean isBound = false;
	boolean isKeepLast = false; //for keeping last
	boolean historyWidgetKept = false;
	Boolean settingsLoaded = false; //synchronize or try to mitigate failures of writing button data, or failures to read data
	Boolean serviceConnected = false;
	Boolean isResumed = false;
	WindowToken[] mWindows = null;
	//VitalsView vitals = null;
	boolean landscape = false;
	ArrayList<ScriptOptionCallback> scriptCallbacks = new ArrayList<ScriptOptionCallback>();
	private View mFoldoutBar = null;
	private RelativeLayout.LayoutParams mOriginalInputBarLayoutParams = null;
	private RelativeLayout.LayoutParams mOriginalDividerLayoutParams = null;
	
	private class ScriptOptionCallback {
		private String window;
		private String title;
		private String callback;
		private Drawable drawable;
		
		public ScriptOptionCallback() 
		{
			setWindow("");
			setTitle("");
			setCallback("");
			setDrawable(null);
		}
		
		public ScriptOptionCallback(String pWin,String title,String callback,Drawable res) {
			setWindow(pWin);
			setTitle(title);
			setCallback(callback);
			setDrawable(res);
		}
		
		public void setWindow(String window) {
			this.window = window;
		}
		public String getWindow() {
			return window;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title; 
		}
		public void setCallback(String callback) {
			this.callback = callback;
		}
		public String getCallback() {
			return callback;
		}
		public void setDrawable(Drawable drawable) {
			this.drawable = drawable;
		}
		public Drawable getDrawable() {
			return drawable;
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			//Log.e("window","starting onServiceConnected");
			service = IConnectionBinder.Stub.asInterface(arg1); //turn the binder into something useful
			
			//register callback
			try {
				String display = MainWindow.this.getIntent().getStringExtra("DISPLAY");
				String host = MainWindow.this.getIntent().getStringExtra("HOST");
				int port = Integer.parseInt(MainWindow.this.getIntent().getStringExtra("PORT"));
				service.registerCallback(the_callback,host,port,display);
				
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			synchronized(serviceConnected) {
				//Log.e("WINDOW","SERVICE CONNECTED, SENDING NOTIFICATION");
				serviceConnected.notify();
				serviceConnected = true;
			}
			//finishInitializiation();
			//loadSettings();
			//Log.e("window","ending onServiceConnected()");
		}

		public void onServiceDisconnected(ComponentName arg0) {
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				if(service != null) service.unregisterCallback(the_callback);
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			
			service = null;
			
			synchronized(serviceConnected) {
				serviceConnected.notify();
				serviceConnected = false;
			}
		}
		
	};
	private int titleBarHeight;
	
	//private LayerManager mLayers = null;
	public void onCreate(Bundle icicle) {
		//Log.e("Window","start onCreate");
		//Debug.startMethodTracing("window");
		super.onCreate(icicle);
		windowMap = new HashMap<String,com.offsetnull.bt.window.Window>(0);

		
		//this.requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		//this
		if(ConfigurationLoader.isTestMode(this)) {
			//Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this.getApplicationContext()));
		}
		
		SharedPreferences sprefs = this.getSharedPreferences("STATUS_BAR_HEIGHT", 0);
		statusBarHeight = sprefs.getInt("STATUS_BAR_HEIGHT", (int)(25 * this.getResources().getDisplayMetrics().density));
		titleBarHeight = sprefs.getInt("TITLE_BAR_HEIGHT", 0);
		setContentView(R.layout.window_layout);
		
		history = new CommandKeeper(10);

		android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

        //screen2 = (ByteView)findViewById(R.id.slickview);
        //RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);
        //screen2.setParentLayout(l);
        //View fill2 = (View)findViewById(R.id.filler2);
       // fill2.setFocusable(false);
        //fill2.setClickable(false);
        //screen2.setNewTextIndicator(fill2);
        
        //Animation alphaout = new AlphaAnimation(1.0f,0.0f);
        //alphaout.setDuration(100);
       // alphaout.setFillBefore(true);
        //alphaout.setFillAfter(true);
        //fill2.startAnimation(alphaout);
        
        //screen2.setZOrderOnTop(false);
        //screen2.setOnTouchListener(gestureListener);
        
        //vitals = (VitalsView) this.findViewById(R.id.vitals);
        
        //TODO: init lua
        
		
        //health = (Bar)vitals.findViewById(R.id.health);
        //mana = (Bar)vitals.findViewById(R.id.mana);
        //enemy = (Bar)vitals.findViewById(R.id.enemy);
        //health.setColor(0xFF00FF00);
        //mana.setColor(0xFF0000FF);
        /*SharedPreferences vc = this.getSharedPreferences("VITALS_CONF", Context.MODE_PRIVATE);
        boolean run = vc.getBoolean("HASRUN", false);
        if(!run) {
        	SharedPreferences.Editor ed = vc.edit();
        	ed.putBoolean("HASRUN", true);
        	vitals.autoPosition();
        	vitals.savePosition(ed);
        	ed.commit();
        	
        } else {
        	int left = vc.getInt("LEFT", 0);
        	int right = vc.getInt("RIGHT", 150);;
        	int top = vc.getInt("TOP", 0);
        	int bottom = vc.getInt("BOTTOM", 0);
        	vitals.setRect(left,right,top,bottom);
        }*/
        //enemy.setValue(10);
        //mana.setValue(90);
        //health.setValue(10);
		
		View divider = findViewById(R.id.divider);
		RelativeLayout.LayoutParams dividerparams = (android.widget.RelativeLayout.LayoutParams) divider.getLayoutParams();
		dividerparams.addRule(RelativeLayout.ABOVE,10);
		divider.setId(40);
		
        View v = findViewById(R.id.textinput);
        //v.setTag("inputbar");
        //EditText input_box = (EditText)v;
        mInputBox = (BetterEditText) v;
        mInputBox.setId(30);
        
        View inputBar = findViewById(R.id.inputbar);
    	mOriginalInputBarLayoutParams = new RelativeLayout.LayoutParams(inputBar.getLayoutParams());
    	mOriginalDividerLayoutParams =  new RelativeLayout.LayoutParams(divider.getLayoutParams());;
        inputBar.setBackgroundColor(0xFF0A0A0A);
        inputBar.setId(10);
       // mInputBox.setSelectAllOnFocus(true);
        mInputBox.setFocusable(true);
        //mInputBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
		//	@Override
		//	public void onFocusChange(View v, boolean hasFocus) {
		//		Log.e("Selection","Setting selection for focus.");
		//		if(hasFocus) {
					
		//			((EditText)v).selectAll();
		//		}
		//	}
		//});
        
//        mInputBox.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//				myhandler.sendEmptyMessageDelayed(MESSAGE_RESETINPUTWINDOW, 3000);
//			}
//		});
//        
        mInputBox.setOnKeyListener(new TextView.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				//EditText input_box = (EditText)findViewById(R.id.textinput);
				if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					
					String cmd = history.getNext();
					//try {
						if(isKeepLast) {
							if(historyWidgetKept) {
								String tmp = history.getNext();
								mInputBox.setText(tmp);
								mInputBox.setSelection(tmp.length());
								historyWidgetKept=false;
							} else {
								mInputBox.setText(cmd);
								//input_box.setText(cmd);
								mInputBox.setSelection(cmd.length());
							}
						} else {
							mInputBox.setText(cmd);
							mInputBox.setSelection(cmd.length());
						}
					//} catch (RemoteException e) {
					//	throw new RuntimeException(e);
					//}
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getPrev();
					mInputBox.setText(cmd);
					mInputBox.setSelection(cmd.length());
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_UP) {
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
					//screen2.jumpToZero();
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					return true;
				}
				
				return false;
			}
   
        });
        
        mInputBox.setDrawingCacheEnabled(true);
        mInputBox.setVisibility(View.VISIBLE);
        mInputBox.setEnabled(true);
        
        mInputBox.setOnBackPressedListener(new BetterEditText.BackPressedListener() {
			
			@Override
			public void onBackPressed() {
				Log.e("log","intercepting back press");
				
				mInputBox.setOnTouchListener(mEditBoxTouchListener);
			}
		});
        //TextView filler = (TextView)findViewById(R.id.filler);
        //filler.setFocusable(false);
        //filler.setClickable(false);
        
        mInputBox.setOnTouchListener(mEditBoxTouchListener);
        
        
        mInputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        

        
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
				//EditText input_box = (EditText)findViewById(R.id.textinput);
				
				if(actionId == EditorInfo.IME_ACTION_SEND) {
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
					return true;
				} 
				if(event == null) return true;
				if((((event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) && event.getAction() == KeyEvent.ACTION_UP))) {
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getNext();
					mInputBox.setText(cmd);
					mInputBox.setSelection(cmd.length());
					if(actionId == EditorInfo.IME_ACTION_DONE) {

						//	return false;
							return true;
							
					} else { return true; }
				} else {
					return true;
				}
				//return false;
			}
		});
        
		
		//assign my handler
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				//EditText input_box = (EditText)findViewById(R.id.textinput);
				switch(msg.what) {
				case MESSAGE_SHOWREGEXWARNING:
					mShowRegexWarning = (msg.arg1 == 1) ? true : false;
					break;
				case MESSAGE_CLOSEOPTIONSDIALOG:
					closeOptionsDialog();
					break;
				case MESSAGE_EXPORTSETTINGS:
					MainWindow.this.doExportSettings((String)msg.obj);
					break;
				case MESSAGE_DORESETSETTINGS:
					MainWindow.this.doResetSettings();
					break;
				case MESSAGE_USECOMPATIBILITYMODE:
					MainWindow.this.setUseCompatibilityMode((msg.arg1 == 1) ? true : false);
					break;
				case MESSAGE_USESUGGESTIONS:
					MainWindow.this.setUseSuggestions( (msg.arg1 == 1) ? true : false);
					break;
				case MESSAGE_USEFULLSCREENEDITOR:
					MainWindow.this.setUseFullscreenEditor((msg.arg1 == 1) ? true : false);
					break;
				case MESSAGE_SETKEEPSCREENON:
					MainWindow.this.setKeepScreenOn((msg.arg1 == 1) ? true : false);
					break;
				case MESSAGE_SETORIENTATION:
					MainWindow.this.setOrientation(msg.arg1);
					break;
				case MESSAGE_DISPLAYLUAERROR:
					MainWindow.this.dispatchLuaError((String)msg.obj);
					break;
				case MESSAGE_POPMENUSTACK:
					MainWindow.this.popMenuStack();
					break;
				case MESSAGE_PUSHMENUSTACK:
					MainWindow.this.pushMenuStack((String)msg.obj,msg.getData().getString("CALLBACK"));
					break;
				case MESSAGE_SETKEEPLAST:
					MainWindow.this.setKeepLast((msg.arg1 == 1) ? true : false);
					break;
				case MESSAGE_MARKSETTINGSDIRTY:
					MainWindow.this.markSettingsDirty();
					break;
				case MESSAGE_MARKWINDOWSDIRTY:
					MainWindow.this.markWindowsDirty();
					break;
				case MESSAGE_WINDOWBUFFERMAXCHANGED:
					String pluginl = msg.getData().getString("PLUGIN");
					String window = msg.getData().getString("WINDOW");
					int amount = msg.arg1;
					try {
						service.updateWindowBufferMaxValue(pluginl,window,amount);
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					break;
				case MESSAGE_PLUGINXCALLS:
					//Map map = (Map)msg.obj;
					String plugin = msg.getData().getString("PLUGIN");
					String function = msg.getData().getString("FUNCTION");
					try {
						service.pluginXcallS(plugin,function,(String)msg.obj);
					} catch (RemoteException e9) {
						// TODO Auto-generated catch block
						e9.printStackTrace();
					}
					break;
				case MESSAGE_ADDOPTIONCALLBACK:
					Bundle datab = msg.getData();
//					String pWin,String title,String callback,Drawable res
					ScriptOptionCallback cb = null;
					if(msg.obj instanceof Drawable) {
						cb = new ScriptOptionCallback(datab.getString("window"),
								datab.getString("title"),
								datab.getString("funcName"),
								(Drawable)msg.obj);
					} else {
						cb = new ScriptOptionCallback(datab.getString("window"),
								datab.getString("title"),
								datab.getString("funcName"),
								null);
					}
					scriptCallbacks.add(0, cb);
					//if(supportsActionBar()) {
						MainWindow.this.invalidateOptionsMenu();
					//}
					break;
				case MESSAGE_INITIALIZEWINDOWS:
					//Log.e("WINDOW","INITIALIZE WINDOWS CALLED");
					//windowsInitialized = false;
					scriptCallbacks.clear();
					//if(supportsActionBar()) {
						MainWindow.this.invalidateOptionsMenu();
					//}
					
					loadSettings();
					MainWindow.this.initiailizeWindows();
					
					try {
						service.initXfer();
					} catch (RemoteException e5) {
						// TODO Auto-generated catch block
						e5.printStackTrace();
					}
					break;
				case MESSAGE_SWITCH:
					//mConnection.
					MainWindow.this.unbindService(mConnection);
					
					//MainWindow.this.bin
					String serviceBindAction = ConfigurationLoader.getConfigurationValue("serviceBindAction", MainWindow.this);
					SharedPreferences.Editor edit = MainWindow.this.getSharedPreferences("CONNECT_TO", Context.MODE_PRIVATE).edit();
					edit.putString("CONNECT_TO", MainWindow.this.getIntent().getStringExtra("DISPLAY"));
					edit.commit();
					MainWindow.this.bindService(new Intent(serviceBindAction, null, MainWindow.this.getApplicationContext(), StellarService.class),mConnection, 0);
					//MainWindow.this.bindService(n, conn, flags)
					
					break;
				case MESSAGE_TRIGGERSTR:
					
					break;
				case MESSAGE_TESTLUA:
					//LuaState exist = LuaStateFactory.getExistingState(msg.arg1);
					//exist.LdoString("Note(\"Fooooooo\")");
					break;
				case MESSAGE_LAUNCHURL:
					Pattern urlPattern = Pattern.compile(TextTree.urlFinderString);
					Matcher urlMatcher = urlPattern.matcher((String)msg.obj);
					if(urlMatcher.find()) {
						String url = "";
						if(urlMatcher.group(1) == null || urlMatcher.group(1).equals("")) {
							if(urlMatcher.group(2) == null || !urlMatcher.group(2).equals("")) {
								url = "http://"+urlMatcher.group(2);
							}
						} else {
							url = urlMatcher.group(1);
						}
						if(!url.equals("")) {
							Intent web_help = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
							startActivity(web_help);
						}
					}
					break;
				case MESSAGE_RENAWS:
					//try {
						//TODO: NAWS WORK
						//service.setDisplayDimensions(screen2.CALCULATED_LINESINWINDOW, screen2.CALCULATED_ROWSINWINDOW);
					//} catch (RemoteException e5) {
						
						//e5.printStackTrace();
					//}
					break;
				case MESSAGE_CLEARINPUTWINDOW:
					ClearKeyboard();
					break;
				case MESSAGE_CLOSEINPUTWINDOW:
				case MESSAGE_HIDEKEYBOARD:
					HideKeyboard();
					break;
				case MESSAGE_LINEBREAK:
					//screen2.setLineBreaks((Integer)msg.obj);
					break;
				case MESSAGE_SENDBUTTONDATA:
					
					try {
						service.sendData(((String)msg.obj).getBytes(service.getEncoding()));
						
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					}
					//screen2.jumpToZero();
					break;
				case MESSAGE_DODISCONNECT:
					//Log.e("WINDOW","SHOW MESSAGE");
					DoDisconnectMessage((String)msg.obj);
					break;
				case MESSAGE_KEYBOARD:
					boolean add = (msg.arg2 > 0) ? true : false;
					boolean popup = (msg.arg1 > 0) ? true : false;
					String text = (String)msg.obj;
					
					if(!add) {
						//reset text
						mInputBox.setText(text);
						mInputBox.setSelection(mInputBox.getText().toString().length());
					} else {
						//append text
						mInputBox.setText(mInputBox.getText().toString() + text);
						mInputBox.setSelection(mInputBox.getText().toString().length());
					}
					
					if(popup) {
						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						mgr.showSoftInput(mInputBox, InputMethodManager.SHOW_FORCED);
						mInputBox.setOnTouchListener(null);
					}
				
					break;
				case MESSAGE_DOSCREENMODE:
					boolean fullscreen = false;
					if(msg.arg1 == 1) {
						fullscreen = true;
					}
					boolean needschange = false;
					if(fullscreen && !isFullScreen) {
						//switch to fullscreen.
						
							//service.setFullScreen(true);
						isFullScreen = true;
					    MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					    MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
					    needschange = true;
						
					}
					
					if(!fullscreen && isFullScreen) {
						//switch to non full screen.
						
						//service.setFullScreen(false);
						isFullScreen = false;
						MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
						MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					
						//MainWindow.this.findViewById(R.id.window_container).requestLayout();
						needschange = true;
						
					}
					
					if(needschange) {
						
						//screen2.doDelayedDraw(100);
						
					}
					
					//try {
					//	this.sendMessage(this.obtainMessage(MESSAGE_CHANGEBUTTONSET,service.getLastSelectedSet()));
					//} catch (RemoteException e5) {
					//	throw new RuntimeException(e5);
					//}
					
					
					break;
				case MESSAGE_BELLTOAST:
					Toast belltoast = Toast.makeText(MainWindow.this, "No actual message.", Toast.LENGTH_LONG);
					//t.setView(view);
					
					
					LayoutInflater li = (LayoutInflater) MainWindow.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = li.inflate(R.layout.bell_toast, null);
					//TextView tv = (TextView) v.findViewById(R.id.message);
					//tv.setText(message);
					
					belltoast.setView(v);
					float density = MainWindow.this.getResources().getDisplayMetrics().density;
					belltoast.setGravity(Gravity.TOP|Gravity.RIGHT, (int)(40*density), (int)(30*density));
					belltoast.setDuration(Toast.LENGTH_SHORT);
					belltoast.show();
					break;
				case MESSAGE_LOCKUNDONE:
					//MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					//screen2.forceDraw();
					//screen2.invalidate();
					//Log.e("WINDOW","ATTEMPTING TO FORCE REDRAW THE SCREEN");
					break;
				case MESSAGE_HFPRESS:
					DoHapticFeedbackPress();
					break;
				case MESSAGE_HFFLIP:
					DoHapticFeedbackFlip();
					break;
				case MESSAGE_SHOWDIALOG:
					AlertDialog.Builder dbuilder = new AlertDialog.Builder(MainWindow.this);
					dbuilder.setTitle("ERROR");
					dbuilder.setMessage((String)msg.obj);
					dbuilder.setCancelable(true);
					//dbuilder.set
					dbuilder.setPositiveButton("Close Window", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							cleanExit();
							MainWindow.this.finish();
							
						}
					});
					
					AlertDialog dlg = dbuilder.create();
					dlg.show();
					
					break;
				case MESSAGE_SHOWTOAST:
					Toast t = null;
					if(msg.arg1 == 1) {
						t = Toast.makeText(MainWindow.this, (String)msg.obj, Toast.LENGTH_LONG);
					} else {
						t = Toast.makeText(MainWindow.this, (String)msg.obj, Toast.LENGTH_SHORT);		
					}
					t.show();
					break;

				case MESSAGE_DOHAPTICFEEDBACK:
					DoHapticFeedback();
					break;
				case MESSAGE_DIRTYEXITNOW:
					//the service via an entered command ".closewindow" or something, to bypass the window asking if you want to close
					dirtyExit();
					MainWindow.this.finish();
					break;
				case MESSAGE_COLORDEBUG:
					//execute color debug.
					//screen2.setColorDebugMode(msg.arg1);
					//TODO: COLOR DEBUG MODE
					break;
				case MESSAGE_XMLERROR:
					//got an xml error, need to display it.
					String xmlerror = (String)msg.obj;
					AlertDialog.Builder builder = new AlertDialog.Builder(MainWindow.this);
					builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					
					builder.setMessage("XML Error: " + xmlerror + "\nSettings have not been loaded.");
					builder.setTitle("Problem with XML File.");
					
					
					//tvtmp.setText("TESTING");
					//builder.setView(tvtmp);
					
					
					AlertDialog error = builder.create();
					error.show();
					TextView tvtmp = (TextView)error.findViewById(android.R.id.message);
					tvtmp.setTypeface(Typeface.MONOSPACE);
					
					break;
				case MESSAGE_SAVEERROR:
					String saveerror = (String)msg.obj;
					AlertDialog.Builder sbuilder = new AlertDialog.Builder(MainWindow.this);
					sbuilder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					
					sbuilder.setMessage(saveerror + "\nSettings have not been saved.");
					sbuilder.setTitle("Error Saving Settings");
					
					
					//tvtmp.setText("TESTING");
					//builder.setView(tvtmp);
					
					
					AlertDialog serror = sbuilder.create();
					serror.show();
					TextView stvtmp = (TextView)serror.findViewById(android.R.id.message);
					stvtmp.setTypeface(Typeface.MONOSPACE);
					break;
				case MESSAGE_PLUGINSAVEERROR:
					String pserror = (String)msg.obj;
					
					AlertDialog.Builder psbuilder = new AlertDialog.Builder(MainWindow.this);
					psbuilder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					
					psbuilder.setMessage(pserror + "\nPlugin has not been saved.");
					psbuilder.setTitle("Error Saving Plugin");
					
					
					//tvtmp.setText("TESTING");
					//builder.setView(tvtmp);
					
					
					AlertDialog pserrord = psbuilder.create();
					pserrord.show();
					TextView pstvtmp = (TextView)pserrord.findViewById(android.R.id.message);
					pstvtmp.setTypeface(Typeface.MONOSPACE);
					break;
				case MESSAGE_LOADSETTINGS:
					//the service is connected at this point, so the service is alive and settings are loaded
					//Log.e("WINDOW","CALLBACK INDICATED RELOADING OF SETTINGS");
					loadSettings();
					break;
				case MESSAGE_PROCESSINPUTWINDOW:
					
					//input_box.debug(5);
					
					String pdata = mInputBox.getText().toString();
					history.addCommand(pdata);
					Character cr = new Character((char)13);
					Character lf = new Character((char)10);
					String crlf = cr.toString() + lf.toString();
					pdata = pdata.concat(crlf);
					//ByteBuffer buf = ByteBuffer.allocate(pdata.length());
					ByteBuffer buf = null;
					try {
						String enc = service.getEncoding();
						if(enc == null) {
							Log.e("uh oh","null pointer incoming");
						}
						
						buf = ByteBuffer.allocate(pdata.getBytes(service.getEncoding()).length);
					} catch (UnsupportedEncodingException e2) {
						throw new RuntimeException(e2);
					} catch (RemoteException e2) {
						throw new RuntimeException(e2);
					}
					
					
					try {
						buf.put(pdata.getBytes(service.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					} catch (RemoteException e) {
						
						e.printStackTrace();
					}
				
					buf.rewind();
				
					byte[] buffbytes = buf.array();

					try {
						service.sendData(buffbytes);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_RESETINPUTWINDOW);
					break;
				case MESSAGE_RESETINPUTWINDOW:
					//Log.e("WINDOW","Attempting to reset input bar.");
					
					//try {
						if(isKeepLast) {
							mInputBox.setSelection(0, mInputBox.getText().length());
							mInputBox.selectAll();
							historyWidgetKept = true;
						} else {
							mInputBox.clearComposingText();
							mInputBox.setText("");
						}
						
						com.offsetnull.bt.window.Window w = (com.offsetnull.bt.window.Window) MainWindow.this.findViewById(MAIN_WINDOW_ID);
						if(w != null) {
							w.jumpToStart();
						}
						//} catch (RemoteException e1) {
					//	throw new RuntimeException(e1);
					//}
					break;
				case MESSAGE_RAWINC:
					
					//screen2.addBytes((byte[])msg.obj, false);
					
					break;
				case MESSAGE_BUFFINC:
					
					//screen2.addBytes((byte[])msg.obj,true);
					break;
				case MESSAGE_SENDDATAOUT:
					try {
						service.sendData((byte[])msg.obj);
						
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					//screen2.jumpToZero();
					
					
					break;
				default:
					break;
				}
			}

			
		};
		
		//EditText input_box = (EditText)findViewById(R.id.textinput);
		//BetterEditText bet = (BetterEditText)input_box;
		//bet.setListener(mInputBarAnimationListener);
		
		test_button = (ImageButton)findViewById(R.id.foldout);
				
		test_button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//change my layout parameters and add a new button.
				//RelativeLayout rl = (RelativeLayout)findViewById(R.id.input_bar);
				//LinearLayout l = (LinearLayout)findViewById(R.id.ctrl_target);
				//if(l == null) {
				///	return;
				//}
				
				if(mFoldoutBar == null) {
					LayoutInflater lf = LayoutInflater.from(MainWindow.this);
					mFoldoutBar = (LinearLayout)lf.inflate(R.layout.input_controls, null);
					
				
					
					ImageButton dc = (ImageButton)mFoldoutBar.findViewById(R.id.down_btn_c);
					ImageButton uc = (ImageButton)mFoldoutBar.findViewById(R.id.up_btn_c);
					ImageButton ec = (ImageButton)mFoldoutBar.findViewById(R.id.enter_btn_c);
					
					//target.removeView(dc);
					//target.removeView(uc);
					//target.removeView(ec);
					
					//target.removeAllViews();
					
					//downButton = dc;
					//upButton = uc;
					//enterButton = ec;
					
					uc.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View arg0) {
							//EditText input_box = (EditText)findViewById(R.id.textinput);
							String cmd = history.getNext();
							//try {
								if(isKeepLast) {
									if(historyWidgetKept) {
										String tmp = history.getNext();
										mInputBox.setText(tmp);
										mInputBox.setSelection(tmp.length());
										historyWidgetKept = false;
									} else {
										mInputBox.setText(cmd);
										mInputBox.setSelection(cmd.length());
									}
								} else {
									mInputBox.setText(cmd);
									mInputBox.setSelection(cmd.length());
								}
							//} catch (RemoteException e) {
							//	throw new RuntimeException(e);
							//}
						}
					});
					
					
					dc.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View arg0) {
							//EditText input_box = (EditText)findViewById(R.id.textinput);
							String cmd = history.getPrev();
							mInputBox.setText(cmd);
						}
					});
					
					ec.setOnClickListener(new View.OnClickListener() {
	
						public void onClick(View arg0) {
							myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
							//screen2.jumpToZero();
						}
						
					});
					
					//RelativeLayout rl = (RelativeLayout) MainWindow.this.findViewById(R.id.window_container);
					//rl.addView(enterButton);
					//rl.addView(upButton);
					//rl.addView(downButton);
				}
				//pre multi screen support animation value 178px
				int foldoutbuttonwidth = 0;
				ImageButton dc = (ImageButton)mFoldoutBar.findViewById(R.id.down_btn_c);
				
				foldoutbuttonwidth = dc.getDrawable().getIntrinsicWidth();
				//foldoutbuttonwidth = (int) (40*MainWindow.this.getResources().getDisplayMetrics().density);
				
				float amount = foldoutbuttonwidth*3;

				if(input_controls_expanded) {
					//TranslateAnimation outanim = new TranslateAnimation(0,-1*amount,0,0);
					//outanim.setDuration(320);
					//TranslateAnimation outanimB = new TranslateAnimation(0,-1*amount,0,0);
					//outanim.setDuration(320);
					//TranslateAnimation outanimC = new TranslateAnimation(0,-1*amount,0,0);
					//outanim.setDuration(320);
					
					//TranslateAnimation nullanim = new TranslateAnimation(0,0,0,0);
					//nullanim.setDuration(320);
					Animation a = new TranslateAnimation(0,-1*amount,0,0);

					a.setDuration(320);

					AnimationSet set = new AnimationSet(true);
					set.addAnimation(a);

					LayoutAnimationController lac = new LayoutAnimationController(set,0.0f);
					
					LinearLayout inputbar = (LinearLayout) MainWindow.this.findViewById(10);
					inputbar.setLayoutAnimation(lac);
					
					
					//inputbar.addView(mFoldoutBar, 0);
					//input_controls_expanded = true;
					
					input_controls_expanded = false;
					//inputbar.removeView(mFoldoutBar);
					//lac.start();
					inputbar.startLayoutAnimation();
					//((LinearLayout)mFoldoutBar.getParent()).removeView(mFoldoutBar);
					mInputBox.setListener(mInputBarAnimationListener);
					//((View)mFoldoutBar.getParent()).startAnimation(outanim);
					//mInputBox.startAnimation(nullanim);

				} else {
					Animation a = new TranslateAnimation(-1*amount,0,0,0);

					a.setDuration(320);

					AnimationSet set = new AnimationSet(true);
					set.addAnimation(a);

					LayoutAnimationController lac = new LayoutAnimationController(set,0.0f);
					
					LinearLayout inputbar = (LinearLayout) MainWindow.this.findViewById(10);
					inputbar.setLayoutAnimation(lac);
					
					
					inputbar.addView(mFoldoutBar, 0);
					input_controls_expanded = true;
					
				}

			}
		});
		
		//screen2.setDispatcher(myhandler);
		//screen2.setButtonHandler(myhandler);
		//screen2.setInputType(input_box);
		//input_box.bringToFront();
		//icicile is out, prefs are in
		
		synchronized(settingsLoaded) {
		//Log.e("WINDOW","CHECKING SETTINGS FROM: " + PREFS_NAME);
		//SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		//servicestarted = prefs.getBoolean("CONNECTED",false);
		//finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		
		//int count = prefs.getInt("BUTTONCOUNT", 0);
		//for(int i = 0;i<count;i++) {
		//	//get button string
		//	String data = prefs.getString("BUTTON"+i, "");
//
		//	Message msg = screen2.buttonaddhandler.obtainMessage(103, data);
		//	screen2.buttonaddhandler.sendMessage(msg);
			
			
		//}
		
		//settingsLoaded.notify();
		//settingsLoaded = true;
		} 
		//if(icicle != null) {
		//	CharSequence seq = icicle.getCharSequence("BUFFER");
		//	if(seq != null) {
		//		screen2.setBuffer((new StringBuffer(seq).toString()));
		//	} else {
		//	}
		//} else {
		//}
		
		if(!isServiceRunning()) {
			//start the service
			//if("com.happygoatstudios.bt.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			//	mode = LAUNCH_MODE.FREE;
			//} else if("com.happygoatstudios.bt.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			//	mode = LAUNCH_MODE.TEST;
			//}
			
			/*if(mode == LAUNCH_MODE.FREE) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
			} else if(mode == LAUNCH_MODE.TEST) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
			}*/
			
			String serviceBindAction = ConfigurationLoader.getConfigurationValue("serviceBindAction", this);
			Intent startAction = new Intent(serviceBindAction);
			//startAction.putExtra(name, value)
			//Bundle b = startAction.getExtras();
			startAction.putExtra("DISPLAY", "aardwolf");
			startAction.putExtra("PORT", 4010);
			startAction.putExtra("HOST", "aardmud.org");
			
			this.startService(new Intent(serviceBindAction));
			
			//this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()));
			//servicestarted = true;
		}
		
		//register screenlock thingie.
		//IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		//filter.addAction(Intent.ACTION_SCREEN_OFF);
		///filter.addAction(Intent.ACTION_USER_PRESENT);
		//BroadcastReceiver mReceiver = new ScreenState(myhandler);
		//registerReceiver(mReceiver, filter);
		
		
		//give it some time to launch
		synchronized(this) {
			try {
				this.wait(5);
			} catch (InterruptedException e) {
			}
		}
		
		
		mInputBarAnimationListener = new BetterEditText.AnimationEndListener() {
			
			public void onAnimationEnd() {
				//input_bar.
				
				if(input_controls_expanded) {

				} else {
					//Log.e("Ou","IN THE CUSTOM ANIMATION LISTENER CONTROLLRE");
					LinearLayout p = (LinearLayout) mInputBox.getParent();
					p.removeViewAt(0);
				}
				//
			}
		};
		
		mInputBox.setListener(mInputBarAnimationListener);
		mRootView = (RelativeLayout)this.findViewById(R.id.window_container);

		this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0x00FFFFFF));

		this.getSupportActionBar().setDisplayOptions(0, android.support.v7.app.ActionBar.DISPLAY_SHOW_HOME);
		this.getSupportActionBar().setDisplayOptions(0, android.support.v7.app.ActionBar.DISPLAY_SHOW_TITLE);



		Button b = new Button(this);
		b.setBackgroundColor(0x00000000);
		//b.setBackgroundColor(0x33FF0000);
		android.support.v7.app.ActionBar.LayoutParams tmp2 = new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT,android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		b.setLayoutParams(tmp);

		b.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Log.e("lsfd","sldjfs");
				//mRootView.dispatchTouchEvent(event);

				return mRootView.dispatchTouchEvent(event);
				//return false;
			}
		});
		//b.setEnabled(false);
		this.getSupportActionBar().setCustomView(b,tmp2);
		this.getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
		this.getSupportActionBar().setDisplayShowCustomEnabled(true);
		//this.getSupportActionBar().setContent
		//android.support.v7.widget.Toolbar parent =(android.support.v7.widget.Toolbar) customView.getParent();
		//parent.setContentInsetsAbsolute(0,0);

		//Log.e("Window","End on create");
	}
	
	View.OnTouchListener mEditBoxTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()) {
			case MotionEvent.ACTION_UP:
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		        imm.showSoftInput(mInputBox, InputMethodManager.SHOW_FORCED);
		        mInputBox.setOnTouchListener(null);
				break;
			}
			return true;
		}
	};
	
	protected void doExportSettings(String path) {
		try {
			service.exportSettingsToPath(path);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void doResetSettings() {
		try {
			service.resetSettings();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void setUseCompatibilityMode(boolean value) {
		
		mInputBox.setBackSpaceBugFix(value);
		setupEditor(fullscreenEditor, value);
		InputMethodManager imm = (InputMethodManager) mInputBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.restartInput(mInputBox);
	}

	protected void setUseFullscreenEditor(boolean value) {
		fullscreenEditor = value;
		setupEditor(fullscreenEditor,useSuggestions);
		InputMethodManager imm = (InputMethodManager) mInputBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.restartInput(mInputBox);
	}

	protected void setUseSuggestions(boolean value) {
		useSuggestions = value;
		setupEditor(fullscreenEditor,useSuggestions);
		InputMethodManager imm = (InputMethodManager) mInputBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.restartInput(mInputBox);
	}

	protected void setKeepScreenOn(boolean value) {
		mInputBox.setKeepScreenOn(value);
	}

	protected void setOrientation(int arg1) {
		doSetOrientiation(arg1);
	}

	protected void dispatchLuaError(String obj) {
		try {
			service.dispatchLuaError(obj);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dispatchLuaText(String obj) {
		try {
			service.dispatchLuaText(obj);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void popMenuStack() {
		menuStack.pop();
		//if(supportsActionBar()) {
			this.invalidateOptionsMenu();
		//}
	}

	Stack<MenuStackItem> menuStack = new Stack<MenuStackItem>();
	protected void pushMenuStack(String obj,String callback) {
		MenuStackItem tmp = new MenuStackItem(obj,callback);
		menuStack.push(tmp);
		//if(supportsActionBar()) {
			this.invalidateOptionsMenu();
		//}
	}
	
	private class MenuStackItem {
		String window;
		String callback;
		public MenuStackItem(String window,String callback) {
			this.window = window;
			this.callback = callback;
		}
	}


	protected void setKeepLast(boolean b) {
		this.isKeepLast = b;
	}

	protected void markSettingsDirty() {
		// TODO Auto-generated method stub
		
	}

	protected void markWindowsDirty() {
		this.windowsInitialized = false;
	}

	ImageButton downButton = null;
	ImageButton upButton = null;
	ImageButton enterButton = null;
	RelativeLayout.LayoutParams enterOutParams = null;
	RelativeLayout.LayoutParams enterInParams = null;
	RelativeLayout.LayoutParams upOutParams = null;
	RelativeLayout.LayoutParams upInParams = null;
	RelativeLayout.LayoutParams downOutParams = null;
	RelativeLayout.LayoutParams downInParams = null;
	RelativeLayout.LayoutParams toggleOutParams = null;
	RelativeLayout.LayoutParams toggleInParams = null;
	
	
	protected void initVitals() {
		//RelativeLayout layout = (RelativeLayout) MainWindow.this.findViewById(R.id.vitals);
		
		//layout.addView(vitals);
		//layout.invalidate();
		
	}
	
	

	/*boolean showsettingsoptions = false;
	boolean settingsmenuclosed  = true;
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		menu.clear();
		if(!showsettingsoptions) {
			menu.add(0,99,0,"Aliases");
			menu.add(0,100,0,"Triggers");
			menu.add(0,101,0,"Options");
			menu.add(0,102,0,"Button Sets");
		} else {
			menu.add(0,103,0,"Edit Settings");
			menu.add(0,104,0,"Import Settings");
			menu.add(0,105,0,"Export Settings");
		}
		
		return true;
	}*/
	
	private void DoDisconnectMessage(final String str) {
		AlertDialog.Builder err = new AlertDialog.Builder(this);
		err.setTitle("Disconnected");
		err.setMessage("Connection to "+str+ " has closed. Reconnect?");
		err.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				try {
					service.reconnect(str);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		err.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				try {
					//if(service.getConnections().size() > 1) {
						service.closeConnection(str);
						//switch to the next one. service will do this for us.
						
					//} else {
					
						cleanExit();
						dialog.dismiss();
						MainWindow.this.finish();
					//}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		AlertDialog d = err.create();
		d.show();
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		onCreateOptionsMenu(menu);
		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.window_container);
		
		if(menuStack.size() > 0) {
			com.offsetnull.bt.window.Window tmp = (com.offsetnull.bt.window.Window)rl.findViewWithTag(menuStack.peek().window);
			tmp.populateMenu(menu);
			return true;
		}
		
		if(mWindows != null) {
			for(WindowToken w : mWindows) {
				com.offsetnull.bt.window.Window tmp = (com.offsetnull.bt.window.Window)rl.findViewWithTag(w.getName());
				tmp.populateMenu(menu);

			}
		}
		
		/*if(supportsActionBar()) {
			if(mHideIcons) {
				for(int i=0;i<menu.size();i++) {
					MenuItem m = menu.getItem(i);
					m.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				}
			}
		}*/
		
		
		//MenuItem tmp = null;

			/*for(int i=1000;i<scriptCallbacks.size()+1000;i++) {
				MenuItem hurdur = menu.add(0,i,0,scriptCallbacks.get(i-1000).getTitle());
				if(scriptCallbacks.get(i-1000).getDrawable() != null) {
					hurdur.setIcon(scriptCallbacks.get(i-1000).getDrawable());
					hurdur.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				} else {
					hurdur.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				}
			}*/
			
//			Button b = new Button(this);
//			b.setText("YEA YAAAA");
//			LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//			b.setLayoutParams(tmp);
//			
//			int count = this.getActionBar().getTabCount();
//			for(int i = 0;i<count;i++) {
//				Log.e("menu tab","tab tab:"+this.getActionBar().getTabAt(i).getText());
//			}
			boolean hide = true;
			
			menu.add(0,100,100,"Aliases").setIcon(R.drawable.ic_menu_alias).setShowAsAction((hide==true) ? MenuItem.SHOW_AS_ACTION_NEVER : MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0,200,200,"Triggers").setIcon(R.drawable.ic_menu_triggers).setShowAsAction((hide==true) ? MenuItem.SHOW_AS_ACTION_NEVER : MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0,300,300,"Timers").setIcon(R.drawable.ic_menu_timers).setShowAsAction((hide==true) ? MenuItem.SHOW_AS_ACTION_NEVER : MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0,400,400,"Options").setIcon(R.drawable.ic_menu_options).setShowAsAction((hide==true) ? MenuItem.SHOW_AS_ACTION_NEVER : MenuItem.SHOW_AS_ACTION_ALWAYS);
			//menu.add(0,102,0,"Button Sets").setIcon(R.drawable.ic_menu_button_sets).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		//SubMenu sm = menu.addSubMenu(0, 900, 0, "More");
		menu.add(0, 500, 500 ,"Speedwalk Directions");
		menu.add(0, 600, 600, "Plugins");
		menu.add(0, 700, 700, "Reconnect");
		menu.add(0, 800, 800, "Disconnect");
		menu.add(0, 900, 900, "Quit");
		menu.add(0, 1000, 1000, "Help/About");
		menu.add(0, 1100,1100,"Reload Settings");
		menu.add(0, 1200,1200,"Reset Settings");
		menu.add(0, 1300,1300,"Export Settings");
		menu.add(0, 1400,1400,"Import Settings");
		
		
		
		return true;
		
	}
	
	//RotatableDialog d = null;
	OptionsDialog optdialog = null;
	
	private void closeOptionsDialog() {
		if(optdialog != null) {
			optdialog.dismiss();
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean onOptionsItemSelected(MenuItem item) {
//		if(item.getItemId() >= 1000) {
//			//script callback
//			ScriptOptionCallback callback = scriptCallbacks.get(item.getItemId()-1000);
//			callWindowScript(callback.getWindow(),callback.getCallback());
//			return true;
//		}
		
		switch(item.getItemId()) {
		case 1200:
			//reset
			doResetDialog();
			break;
		case 1300:
			doExportDialog();
			//export
			break;
		case 1400:
			doImportDialog();
			break;
		case 600:
			BetterPluginSelectionDialog pd = new BetterPluginSelectionDialog(this,service);
			pd.show();
			//PluginDialog pd = new PluginDialog(this,service);
			//pd.show();
			break;
		case 1100:
			try {
				service.reloadSettings();
			} catch (RemoteException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;
		case 1000: //Help/About
			AboutDialog abtdialog = new AboutDialog(this);
			abtdialog.show();
			break;
		case 500: //speedwalk config
			BetterSpeedWalkConfigurationDialog swDialog = new BetterSpeedWalkConfigurationDialog(this,service);
			swDialog.show();
			break;
		case 900:
			this.cleanExit();
			this.finish();
			break;
		case 800:
			//myhandler.sendEmptyMessage(MESSAGE_DODISCONNECT);
			//service.
			try {
				service.endXfer();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 700:
			try {
				service.reconnect(service.getConnectedTo());
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			break;
		case 300:
			BetterTimerSelectionDialog sel = new BetterTimerSelectionDialog(this,service);
			sel.show();
			//TimerSelectionDialog tsel = null;
			//tsel = new TimerSelectionDialog(MainWindow.this,service);
			//tsel.show();
			
			break;
		case 100:
			BetterAliasSelectionDialog d = null;
			//try {
			d = new BetterAliasSelectionDialog(this,service);
			//} catch (RemoteException e) {
			//	throw new RuntimeException(e);
			//}
			d.setTitle("Edit Aliases:");
			d.show();
			break;
//		case 102:
//			//show the button set selector dialog
//			ButtonSetSelectorDialog buttoneditor = null;
//			try{
//				buttoneditor = new ButtonSetSelectorDialog(this,myhandler,(HashMap<String,Integer>)service.getButtonSetListInfo(),service.getLastSelectedSet(),service);
//				buttoneditor.setTitle("Select Button Set");
//				buttoneditor.show();
//			} catch(RemoteException e) {
//				e.printStackTrace();
//			}
//			break;
//		case 400:
//			
//			MainWindow.this.myhandler.postDelayed(new Runnable() { public void run() { openOptionsMenu();}}, 1);
//			
//			break;
		case 400:
			//enter new routine.
			/*SettingsGroup sg = null;
			try {
				sg = service.getSettings();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//give up the list to the dialog.
			int size = sg.getOptions().size();*/
			optdialog = new OptionsDialog(this,service,"main");
			optdialog.show();
			//OptionsDialogFragment odf = new OptionsDialogFragment(service,"main",getFragmentManager());
			//odf.show(getFragmentManager(), "dialog");
			
			break;
			
			
			//OLD SETTINGS METHOD.
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainWindow.this);
//			SharedPreferences.Editor edit = prefs.edit();
//			
//			try {
//				edit.putBoolean("THROTTLE_BACKGROUND",service.isThrottleBackground());
//				edit.putBoolean("USE_EXTRACTUI", service.getUseExtractUI());
//				edit.putBoolean("PROCESS_PERIOD", service.isProcessPeriod());
//				edit.putBoolean("PROCESS_SEMI", service.isSemiNewline());
//				edit.putBoolean("WIFI_KEEPALIVE", service.isKeepWifiActive());
//				edit.putBoolean("USE_SUGGESTIONS", service.isAttemptSuggestions());
//				edit.putBoolean("BACKSPACE_BUGFIX", service.isBackSpaceBugFix());
//				edit.putBoolean("AUTOLAUNCH_EDITOR", service.isAutoLaunchEditor());
//				edit.putBoolean("DISABLE_COLOR",service.isDisableColor());
//				edit.putString("OVERRIDE_HAPTICFEEDBACK", service.HapticFeedbackMode());
//				edit.putString("HAPTIC_PRESS", service.getHFOnPress());
//				edit.putString("HAPTIC_FLIP", service.getHFOnFlip());
//				edit.putString("ENCODING", service.getEncoding());
//				edit.putInt("BREAK_AMOUNT", service.getBreakAmount());
//				edit.putInt("ORIENTATION", service.getOrientation());
//				edit.putBoolean("WORD_WRAP",service.isWordWrap());
//				edit.putBoolean("REMOVE_EXTRA_COLOR", service.isRemoveExtraColor());
//				edit.putBoolean("DEBUG_TELNET", service.isDebugTelnet());
//				edit.putBoolean("KEEPLAST", service.isKeepLast());
//				edit.putString("FONT_SIZE", Integer.toString((service.getFontSize())));
//				edit.putString("FONT_SIZE_EXTRA", Integer.toString(service.getFontSpaceExtra()));
//				edit.putString("MAX_LINES", Integer.toString(service.getMaxLines()));
//				edit.putString("FONT_NAME", service.getFontName());
//				edit.putBoolean("KEEP_SCREEN_ON",service.isKeepScreenOn());
//				edit.putBoolean("LOCAL_ECHO", service.isLocalEcho());
//				edit.putBoolean("BELL_VIBRATE", service.isVibrateOnBell());
//				edit.putBoolean("BELL_NOTIFY", service.isNotifyOnBell());
//				edit.putBoolean("BELL_DISPLAY", service.isDisplayOnBell());
//				edit.putBoolean("WINDOW_FULLSCREEN",service.isFullScreen());
//				edit.putBoolean("ROUND_BUTTONS",service.isRoundButtons());
//				edit.putBoolean("ECHO_ALIAS_UPDATE", service.isEchoAliasUpdate());
//				edit.putInt("HYPERLINK_COLOR", service.getHyperLinkColor());
//				edit.putString("HYPERLINK_MODE", service.getHyperLinkMode());
//				edit.putBoolean("HYPERLINK_ENABLED", service.isHyperLinkEnabled());
//			} catch (RemoteException e) {
//				throw new RuntimeException(e);
//			}
//			
//			edit.commit();
//			
//			Intent settingintent = new Intent(this,HyperSettingsActivity.class);
//			this.startActivityForResult(settingintent, 0);
//
//			//break;
		case 200:
			//launch the sweet trigger dialog.
			//TriggerSelectionDialog trigger_selector = new TriggerSelectionDialog(this,service);
			//trigger_selector.show();
			BetterTriggerSelectionDialog btsd = new BetterTriggerSelectionDialog(this,service,mShowRegexWarning);
			btsd.show();
			break;
		default:
			break;
		}
		return true;
	}
	
	private Pattern xmlinsensitive = Pattern.compile("^.+\\.[Xx][Mm][Ll]$");
	private Matcher xmlimatcher = xmlinsensitive.matcher("");
	
	private void doImportDialog() {
		String exportDir = ConfigurationLoader.getConfigurationValue("exportDirectory", this);
		
		File tmp = Environment.getExternalStorageDirectory();
		//String exportDir = ConfigurationLoader.getConfigurationValue("exportDirectory", this);
		File btermdir = new File(tmp,"/"+exportDir+"/");
		
		FilenameFilter xml_only = new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				//return arg1.endsWith(".xml");
				xmlimatcher.reset(arg1);
				if(xmlimatcher.matches()) {
					return true;
				} else {
					return false;
				}
			}
			
		};
		
		String sdstate = Environment.getExternalStorageState();
		//HashMap<String,String> efonts = new HashMap<String,String>();
		HashMap<String,String> xmlfiles = new HashMap<String,String>();
		if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
			btermdir.mkdirs();
			
			for(File xml : btermdir.listFiles(xml_only)) {
				xmlfiles.put(xml.getName(), xml.getPath());
			}

		}
		
		Set<String> xmlkeys = xmlfiles.keySet();
		List<String> sortedxmlkeys = new ArrayList<String>(xmlkeys);
		Collections.sort(sortedxmlkeys,String.CASE_INSENSITIVE_ORDER);
		
		String[] xmlentries = new String[sortedxmlkeys.size()];
		String[] xmlpaths = new String[sortedxmlkeys.size()];
		int i=0;
		for(String file : sortedxmlkeys) {
			xmlentries[i] = file;
			xmlpaths[i] = xmlfiles.get(file);
			i++;
		}
		
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		
		b.setTitle("Import Settings");
		b.setItems(xmlentries, new ImportDialogListener(xmlpaths));
		
		AlertDialog d = b.create();
		
		d.show();
		
	}
	
	private class ImportDialogListener implements DialogInterface.OnClickListener {
		String[] items = null;
		
		public ImportDialogListener(String[] items) {
			this.items = items;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			try {
				service.loadSettingsFromPath(items[which]);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	EditText filenameExport = null;
	private void doExportDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Export Settings");
		filenameExport = new EditText(this);
		filenameExport.setHint("Enter file name");
		
		b.setView(filenameExport);
		
		b.setPositiveButton("Export", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String path = filenameExport.getText().toString();
				
				MainWindow.this.myhandler.sendMessage(MainWindow.this.myhandler.obtainMessage(MainWindow.MESSAGE_EXPORTSETTINGS, path));
				dialog.dismiss();
			}
		});
		
		b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog d = b.create();
		d.show();
	}

	private void doResetDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Reset Settings?");
		b.setMessage("Are you sure you want to reset settings?");
		b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainWindow.this.myhandler.sendEmptyMessage(MainWindow.this.MESSAGE_DORESETSETTINGS);
				dialog.dismiss();
			}
		});
		
		b.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog d = b.create();
		d.show();
	}

	boolean actionBarTested = false;
	boolean supportsActionBar = false;
	private boolean supportsActionBar() {
		if(actionBarTested == true) {
			return supportsActionBar;
		}
		actionBarTested = true;
		//try {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		//	this.getClass().getMethod("getActionBar", null);
			supportsActionBar = true;
			return true;
		}
		//} catch(NoSuchMethodException e) {
			supportsActionBar = false;
			return false;
		//}
		//if(this.getClass().getM)
		//return false;
	}
	
//	private boolean supportsRotation() {
//		try {
//			android.view.Display.class.getMethod("getRotation", null);
//			return true;
//		} catch (NoSuchMethodException e) {
//			return false;
//		}
//		//return false;
//	}


	Handler extporthandler = new Handler() {
		public void handleMessage(Message msg) {
			//so we are kludging out the new button set dialog to just be a "string enterer" dialog.
			//should be a full path /sdcard/something.xml
			String filename = (String)msg.obj;
			try {
				//Log.e("WINDOW","TRYING TO GET SERVICE TO WRITE A FILE FOR ME!");
				service.exportSettingsToPath(filename);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	};
	
	public void onBackPressed() {
		//Log.e("WINDOW","BACK PRESSED TRAPPED");
		
		if(menuStack.size() > 0) {
			MenuStackItem tmp = menuStack.peek();
			RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.window_container);
			
			com.offsetnull.bt.window.Window w = (com.offsetnull.bt.window.Window)rl.findViewWithTag(tmp.window);
			w.callFunction(tmp.callback,null);
				
			
			return;
		}
		
		//show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(MainWindow.this);
		builder.setMessage("Keep connection running in background?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                MainWindow.this.dirtyExit();
		                MainWindow.this.finish();
		           }
		       });
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //dialog.cancel();
		        	   MainWindow.this.cleanExit();
		        	   MainWindow.this.finish();
		           }
		       });
		//AlertDialog alert = builder.create();
		builder.create();
		builder.show();
		//alert.show();
		
		//super.onBackPressed();
	}
	
	int OREINTATION = Configuration.ORIENTATION_LANDSCAPE;
	
	boolean keyboardShowing = false;
	
	public void onConfigurationChanged(Configuration newconfig) {
		//Log.e("WINDOW","CONFIGURATION CHANGING");
		super.onConfigurationChanged(newconfig);
		
		if(service == null) {
			super.onConfigurationChanged(newconfig);
			return;
		}
		
		
		if(newconfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
			if(keyboardShowing == true) {
				keyboardShowing = false;
				return;
			}
		}
		
		if(newconfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
			if(keyboardShowing == false) {
				keyboardShowing = true;
				return;
			}
		}
		//Log.e("WINDOW","CONFIGURATION CHANGED");
		//RelativeLayout container = (RelativeLayout)this.findViewById(R.id.window_container);
		//RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)container.getLayoutParams();
		switch(newconfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			
		//	container.requestLayout();
			//DoButtonPortraitMode(true);
			//OREINTATION = Configuration.ORIENTATION_PORTRAIT;
			myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 10);
			myhandler.sendEmptyMessageDelayed(MESSAGE_RENAWS, 80);
			
			if(orientation == 1) { //if we are selected as landscape
				newconfig.orientation = Configuration.ORIENTATION_LANDSCAPE;
				//HideKeyboard();
				//myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 1000);
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				
			}
			
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
		//	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//	container.requestLayout();
			//DoButtonPortraitMode(false);
			//OREINTATION = Configuration.ORIENTATION_LANDSCAPE;
			myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 10);
			myhandler.sendEmptyMessageDelayed(MESSAGE_RENAWS, 80);
			
			if(orientation == 2) { //if we are selected as landscape
				newconfig.orientation = Configuration.ORIENTATION_PORTRAIT;
				//HideKeyboard();
				//myhandler.sendEmptyMessageDelayed(MESSAGE_HIDEKEYBOARD, 1000);
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			
			break;
		}
		
		
		
	}
	
	private void ClearKeyboard() {
		//EditText input_box = (EditText)findViewById(R.id.textinput);
		mInputBox.setText("");
	}
	
	private void HideKeyboard() {
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		//EditText input_box = (EditText)findViewById(R.id.textinput);
		imm.hideSoftInputFromWindow(mInputBox.getWindowToken(), 0);
		//Log.e("WINDOW","ATTEMPTING TO HIDE THE KEYBOARD");
		mInputBox.setOnTouchListener(mEditBoxTouchListener);
	}
	
	private void DoHapticFeedback() {
		if(overrideHF.equals("none")) {
			return;
		}
		
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHF.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		
		//BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		mInputBox.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private void DoHapticFeedbackPress() {
		if(overrideHFPress.equals("none")) {
			return;
		}
		
		//Log.e("WINDOW","D")
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHFPress.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		//Log.e("WINDOW","DISPATCHING HAPTIC FEEDBACK FOR PRESS!");
		//BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		mInputBox.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private void DoHapticFeedbackFlip() {
		if(overrideHFFlip.equals("none")) {
			return;
		}
		
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHFFlip.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		
		//BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		mInputBox.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private boolean isServiceRunning() {
	
		ActivityManager activityManager = (ActivityManager)MainWindow.this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
		boolean found = false;
		String serviceProcessName = "com.happygoatstudios.bt" + ConfigurationLoader.getConfigurationValue("serviceProcessName", this);
		for(RunningServiceInfo service : services) {
			if(com.offsetnull.bt.service.StellarService.class.getName().equals(service.service.getClassName())) {
				if(service.process.equals(serviceProcessName)) found = true;
			}
		}
		return found;
	}
	
	private boolean isServiceConnected() {
		try {
			if(service.isConnected()) {
				return true;
			} else {
				return false;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void cleanExit() {
		//we want to kill the service when we go.
		cleanupWindows();
		//shut down the service
		
		try {
			String connected = service.getConnectedTo();
			if(connected != null) {
				service.closeConnection(connected);
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(isBound) {
			try {
				if(service != null) {
					service.unregisterCallback(the_callback);
				}
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			
			unbindService(mConnection);
			
			
			
			isBound = false;
			//Log.e("WINDOW","Unbound connection at cleanExit");
		}
		

		//String serviceBindAction = ConfigurationLoader.getConfigurationValue("serviceBindAction", this);
		//this.stopService(new Intent(serviceBindAction));
		/*if(mode == LAUNCH_MODE.FREE) {
			this.stopService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
		} else if(mode == LAUNCH_MODE.TEST) {
			this.stopService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
		}*/
		
	}
	
	public void dirtyExit() {
		//we dont want to kill the service
		cleanupWindows();
		if(isBound) {
			
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				service.unregisterCallback(the_callback);
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			
			unbindService(mConnection);
			//Log.e("WINDOW","Unbound connection at cleanExit");
			isBound = false;
		}
		//saveSettings();
	}
	
	public void onSaveInstanceState(Bundle data) {
		super.onSaveInstanceState(data);
	}
	
	public void onRestoreInstanceState(Bundle data) {
		super.onRestoreInstanceState(data);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == RESULT_OK) {
			//Log.e("WINDOW","onActivityResult()");
			settingsDialogRun = true;
		}
	}
	
	//LuaWindow lwin = null;

	public void onStart() {
		//Log.e("Window","starting onStart");
		super.onStart();
		/*if("com.happygoatstudios.bt.window.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
			mode = LAUNCH_MODE.FREE;
		} else if("com.happygoatstudios.bt.window.MainWindow.TEST_MODE".equals(this.getIntent().getAction())) {
			mode = LAUNCH_MODE.TEST;
		}*/
		//if(supportsActionBar()) {
			//int height = this.getActionBar().getHeight();
			//Log.e("ACFLSAFD","ACTION BAR HEIGHT(fg) IS :" + height);



		//}
		
		
		if(!isServiceRunning()) {
			String serviceBindAction = ConfigurationLoader.getConfigurationValue("serviceBindAction", this);
			this.startService(new Intent(serviceBindAction));
			//start the service
			/*if(mode == LAUNCH_MODE.FREE) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
			} else if(mode == LAUNCH_MODE.TEST) {
				this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
			}*/
			//servicestarted = true;
		}
		//Log.e("window","ending onStart");
		
	}
	public void onDestroy() {
		
		if(isBound) {
			
			try {
				//Log.e("WINDOW","SAVING BUFFER IN SERVICE");
				
				if(service != null) {
					//service.unregisterCallback(the_callback);
					
					service.unregisterCallback(the_callback);
					service.unregisterCallback(the_callback);
					
					unbindService(mConnection);
					
					//saveSettings();
				} else {
					//uh oh, pausing with a null service, this should not happen
					
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				
			}
			isBound = false;
			
		} else {
			//calling pause without being bound, should not happen
			
		}

		isResumed = false;
		super.onDestroy();
		
		//this.finish();
	
	}
	
	public void onStop() {
		//Log.e("WINDOW","onStop()");
		super.onStop();
	}
	
	public void onPause() {
		//Log.e("WINDOW","onDestroy()");
		//windowShowing = false;
		if(service == null) { super.onPause(); return; };
		try {
			service.windowShowing(false);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//screen2.pauseDrawing();
		//screen2.clearAllText();
		isResumed = false;
		super.onPause();
	}
	public void onResume() {
		super.onResume();
		//Log.e("window","start onResume()");
		//windowShowing = true;
		
		if(!isBound) {
			/*if("com.happygoatstudios.bt.window.MainWindow.NORMAL_MODE".equals(this.getIntent().getAction())) {
				mode = LAUNCH_MODE.FREE;
			} else if("com.happygoatstudios.bt.window.MainWindow.TEST_MODE".equals(this.getIntent().getAction())) {
				mode = LAUNCH_MODE.TEST;
			}
			if(mode == LAUNCH_MODE.FREE) {
				//this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_NORMAL"));
				bindService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()+".MODE_NORMAL"), mConnection, 0);
			} else if(mode == LAUNCH_MODE.TEST) {
				//this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName() + ".MODE_TEST"));
				this.bindService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()+".MODE_TEST"), mConnection, 0);
			}*/
			SharedPreferences.Editor edit = MainWindow.this.getSharedPreferences("CONNECT_TO", Context.MODE_PRIVATE).edit();
			edit.putString("CONNECT_TO", MainWindow.this.getIntent().getStringExtra("DISPLAY"));
			edit.commit();
			String serviceBindAction = ConfigurationLoader.getConfigurationValue("serviceBindAction", this);
			this.bindService(new Intent(serviceBindAction,null,this,StellarService.class),mConnection, 0);
			
			isBound = true;
			isResumed = true;

		} else {
			//request buffer.
			try {
				if(service != null) {
					service.windowShowing(true);
				}
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Intent i = this.getIntent();
			//Log.e("LOG","RESUMING WINDOW WITH INTENT: display="+i.getStringExtra("DISPLAY")+" host="+i.getStringExtra("HOST")+" port="+i.getStringExtra("PORT"));
			String display = i.getStringExtra("DISPLAY");
			
			try {
				if(service != null) {
				if(!service.getConnectedTo().equals(display)) {
					Log.e("LOG","ATTEMPTING TO SWITCH TO: " + display);
					//this.cleanupWindows();
					service.switchTo(display);
				}
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//try {
				//loadSettings();
				//if(service.hasBuffer()) {
				//	setHyperLinkSettings();
				//	service.requestBuffer();
				//} else {
				//	
				//}
			//} catch (RemoteException e2) {
				
			//	e2.printStackTrace();
			//}
			//myhandler.sendEmptyMessage(MESSAGE_LOADSETTINGS);
		}
		
		//screen2.resumeDrawing();
		//screen2.doDelayedDraw(0);
		isResumed = true;
		super.onResume();
		//Log.e("window","end onResume");
	}
	
	public void onDestroy(Bundle saveInstance) {
		//Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	
	/*private void initLayers() {
		RelativeLayout holder = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
		initializeWindows();
		
		
	}*/
	
	private String mBorderTag = "BorderLayer";
	private void setHyperLinkSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//boolean enabled = prefs.getBoolean("HYPERLINKS_ENABLED", true);
		//int color = prefs.getInt("HYPERLINK_COLOR", HyperSettings.DEFAULT_HYPERLINK_COLOR);
		String hyperLinkMode = prefs.getString("HYPERLINK_MODE", "highlight_color_bland_only");
		int hyperLinkColor = prefs.getInt("HYPERLINK_COLOR", HyperSettings.DEFAULT_HYPERLINK_COLOR);
		//boolean fitmessage = prefs.getBoolean("FIT_MESSAGE", true);
		boolean hyperLinkEnabled = prefs.getBoolean("HYPERLINK_ENABLED", true);
		
	}
	
	private int orientation;
	private Boolean mShowRegexWarning;
	private void loadSettings() {
		//TODO: NEW LOAD SETTINGS PLACE
		//if(!isResumed || !screen2.loaded()) {
		if(!isResumed) {
			myhandler.sendEmptyMessageDelayed(MESSAGE_LOADSETTINGS, 50);
			return;
		}
		//attemppt to load button sets.
		@SuppressWarnings("unused")
		boolean fontSizeChanged = false;
		//boolean fullscreen_now = false;		
		
		try {
			//calculate80CharFontSize();
			//ByteView.LINK_MODE hyperLinkMode = ByteView.LINK_MODE.HIGHLIGHT_COLOR_ONLY_BLAND;
			
			
			//screen2.setLinkColor(service.getHyperLinkColor());
			
			//screen2.setLinksEnabled(service.isHyperLinkEnabled());
			//if(!service.isConnected()) { return; }
			SettingsGroup group = service.getSettings();
			
			if(group == null) return; //haven't fully loaded yet.
			if(group.getOptions().size() == 0) return;
			boolean fullscreen = (Boolean)((BaseOption)group.findOptionByKey("fullscreen")).getValue();
			if(fullscreen) {
			    MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			    MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			} else {
				MainWindow.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				MainWindow.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			
			mShowRegexWarning = (Boolean)((BaseOption)(group.findOptionByKey("show_regex_warning"))).getValue();
			
			//

			MainWindow.this.findViewById(R.id.window_container).requestLayout();
			isFullScreen = fullscreen;
			//BetterEditText input_box = (BetterEditText)findViewById(R.id.textinput);
			mInputBox.setBackSpaceBugFix(true);
			
			boolean keep_screen_on = (Boolean)((BaseOption)group.findOptionByKey("screen_on")).getValue();
			
			mInputBox.setKeepScreenOn(keep_screen_on);
		
			
			//screen2.setEncoding(service.getEncoding());
			
			//screen2.setCullExtraneous(service.isRemoveExtraColor());
			
			//int or = MainWindow.this.getRequestedOrientation();
			orientation = (Integer)((BaseOption)group.findOptionByKey("orientation")).getValue();
			doSetOrientiation(orientation);
			
			
			//screen2.setFontSize(service.getFontSize());
			//screen2.setLineSpace(service.getFontSpaceExtra());
			//screen2.setCharacterSizes(service.getFontSize(), service.getFontSpaceExtra());
			//screen2.setMaxLines(service.getMaxLines());
			
			//get the font name 
			//String tmpname = service.getFontName();
			//Typeface font = loadFontFromName(tmpname);
			
			//screen2.setFont(loadFontFromName(tmpname));
			//TODO: NAWS-ACTION
			//service.setDisplayDimensions(screen2.CALCULATED_LINESINWINDOW, screen2.CALCULATED_ROWSINWINDOW);
			
			//if(fontSizeChanged) {
			//	screen2.reBreakBuffer();
			//}
			
			boolean useExtractUI = (Boolean)((BaseOption)group.findOptionByKey("fullscreen_editor")).getValue();
			boolean sugtmp = (Boolean)((BaseOption)group.findOptionByKey("use_suggestions")).getValue();
			setupEditor(useExtractUI,sugtmp);
			fullscreenEditor = useExtractUI;
			useSuggestions = sugtmp;
			
			
			isKeepLast = (Boolean)((BaseOption)group.findOptionByKey("keep_last")).getValue();
			
			//orientation = (Integer)((BaseOption)group.findOptionByKey("orientation")).getValue();
			
			//if(service.isKeepLast()) {
			//	isKeepLast = true;
			//} else {
			//	isKeepLast = false;
			//}
			
			//handle auto launch
			///autoLaunch = service.isAutoLaunchEditor();
			//handle disable color
			//if(service.isDisableColor()) {
				//set the slick view debug mode to 3.
				//screen2.setColorDebugMode(3);
			//} else {
				//screen2.setColorDebugMode(0);
			//}
			///handle overridehf.
			//overrideHF = service.HapticFeedbackMode();
			
			//overrideHFPress = service.getHFOnPress();
			//overrideHFFlip = service.getHFOnFlip();
			
			boolean compatibility = (Boolean)((BaseOption)group.findOptionByKey("compatibility_mode")).getValue();
			
			if(compatibility) {
				//Log.e("WINDOW","APPLYING BACK SPACE BUG FIX");
				//BetterEditText tmp_bar = (BetterEditText)input_box;
				mInputBox.setBackSpaceBugFix(true);
			} else {
				//BetterEditText tmp_bar = (BetterEditText)input_box;
				mInputBox.setBackSpaceBugFix(false);
				//Log.e("WINDOW","NOT APPLYING BACK SPACE BUG FIX");
			}
			
			InputMethodManager imm = (InputMethodManager) mInputBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.restartInput(mInputBox);
			//imm.
			//im
			//get the rest of the window options that are necessary to function
			
		} catch (RemoteException e1) {
			throw new RuntimeException(e1);
		}
		
		//initiailizeWindows();
		//int i = R.id.textinput;
	}

	public void doSetOrientiation(int orientation) {
		switch(orientation) {
		case 0:
			MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			break;
		case 1:
			MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case 2:
			MainWindow.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		default:
			break;
		}
	}

	private boolean fullscreenEditor = false;
	private boolean useSuggestions = false;
	public void setupEditor(boolean useExtractUI,boolean useSuggestions) {
		mInputBox.setHorizontallyScrolling(false);
		mInputBox.setMaxLines(19);
	
		if(useExtractUI) {
			
			
			int current = mInputBox.getImeOptions();
			int wanted = current & (0xFFFFFFFF^EditorInfo.IME_FLAG_NO_EXTRACT_UI);
			wanted = wanted | EditorInfo.IME_ACTION_SEND;
			
			//Log.e("WINDOW","ATTEMPTING TO SET FULL SCREEN IME| WAS: "+ Integer.toHexString(current) +" WANT: " + Integer.toHexString(wanted));
			mInputBox.setImeOptions(wanted);
			mInputBox.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
			//BetterEditText better = (BetterEditText)input_box;
			mInputBox.setUseFullScreen(true);
			//mInputBox.setBackSpaceBugFix(backSpaceBugFix)
		} else {
			int current = mInputBox.getImeOptions();
			int wanted = current | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEND;
			//Log.e("WINDOW","ATTEMPTING TO SET NO EXTRACT IME| WAS: "+ Integer.toHexString(current) +" WANT: " + Integer.toHexString(wanted));
			mInputBox.setImeOptions(wanted);
			if(useSuggestions) {
				mInputBox.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
			} else {
				mInputBox.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
				//mInputBox.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
					
			}
			//BetterEditText better = (BetterEditText)input_box;
			mInputBox.setUseFullScreen(false);
			//Log.e("WINDOW","SETTINGS NOW "+Integer.toHexString(input_box.getImeOptions()));
		}
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
	
	private BetterEditText.AnimationEndListener mInputBarAnimationListener = null;


	private IConnectionBinderCallback.Stub the_callback = new IConnectionBinderCallback.Stub() {

		public void dataIncoming(byte[] seq) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_PROCESS);
			Bundle b = new Bundle();
			b.putByteArray("SEQ", seq);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}
		
		public boolean isWindowShowing() {
			return windowShowing;
		}

		public void processedDataIncoming(CharSequence seq) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_PROCESSED); 
			Bundle b = new Bundle();
			b.putCharSequence("SEQ", seq);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}

		public void htmlDataIncoming(String html) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_HTMLINC);
			Bundle b = new Bundle();
			b.putString("HTML", html);
			msg.setData(b);
			myhandler.sendMessage(msg);
			
		}

		public void rawDataIncoming(byte[] raw) throws RemoteException {
			
			Message msg = myhandler.obtainMessage(MESSAGE_RAWINC,raw);
			//Log.e("WINDOW","RECIEVING RAW");
			myhandler.sendMessage(msg);
			
		}
		
		public void rawBufferIncoming(byte[] rawbuf) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_BUFFINC,rawbuf);
			myhandler.sendMessage(msg);
			//Log.e("WINDOW","RECEIVING BUFFER: " + rawbuf.length());
		}

		public void loadSettings() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_LOADSETTINGS);
		}

		public void displayXMLError(String error) throws RemoteException {
			Message xmlerror = myhandler.obtainMessage(MESSAGE_XMLERROR);
			xmlerror.obj = error;
			myhandler.sendMessage(xmlerror);
			
		}

		@Override
		public void displaySaveError(String error) throws RemoteException {
			Message saveerror = myhandler.obtainMessage(MESSAGE_SAVEERROR);
			saveerror.obj = error;
			myhandler.sendMessage(saveerror);
		}
		
		@Override
		public void displayPluginSaveError(String plugin, String error) throws RemoteException {
			Message saveerror = myhandler.obtainMessage(MESSAGE_SAVEERROR);
			saveerror.obj = error;
			saveerror.getData().putString("PLUGIN", plugin);
			myhandler.sendMessage(saveerror);
		}

		public void executeColorDebug(int arg) throws RemoteException {
			Message colordebug = myhandler.obtainMessage(MESSAGE_COLORDEBUG);
			colordebug.arg1 = arg;
			myhandler.sendMessage(colordebug);
		}

		public void invokeDirtyExit() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_DIRTYEXITNOW);
			
		}

		public void showMessage(String message,boolean longtime) throws RemoteException {
			Message showmessage = myhandler.obtainMessage(MESSAGE_SHOWTOAST);
			showmessage.obj = message;
			if(longtime) {
				showmessage.arg1 = 1;
			} else {
				showmessage.arg1 = 0;
			}
			myhandler.sendMessage(showmessage);
			
		}

		public void showDialog(String message) throws RemoteException {
			Message showdlg = myhandler.obtainMessage(MESSAGE_SHOWDIALOG);
			showdlg.obj = message;
			myhandler.sendMessage(showdlg);
		}

		public void doVisualBell() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_BELLTOAST);
		}

		public void setScreenMode(boolean fullscreen) throws RemoteException {
			Message doScreenMode = myhandler.obtainMessage(MESSAGE_DOSCREENMODE);
			if(fullscreen) {
				doScreenMode.arg1 = 1;
			} else {
				doScreenMode.arg1 = 0;
			}
			
			myhandler.sendMessage(doScreenMode);
		}

		public void showKeyBoard(String txt,boolean popup,boolean add,boolean flush,boolean clear,boolean close) throws RemoteException {
			if(flush) {
				myhandler.sendEmptyMessage(MESSAGE_PROCESSINPUTWINDOW);
				return;
			}
			
			if(clear) {
				myhandler.sendEmptyMessage(MESSAGE_CLEARINPUTWINDOW);
				return;
			}
			
			if(close) {
				myhandler.sendEmptyMessage(MESSAGE_CLOSEINPUTWINDOW);
				return;
			}
			int p = (popup) ? 1 : 0;
			int a = (add) ? 1 : 0;
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_KEYBOARD,p,a,txt));
		}

		public void doDisconnectNotice(String display) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_DODISCONNECT, display));
			
		}

		public void doLineBreak(int i) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_LINEBREAK,new Integer(i)));
		}

		public void reloadButtons(String setName) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_CHANGEBUTTONSET,setName));
		}
		
		public void clearAllButtons() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_CLEARALLBUTTONS);
		}
		
		public void updateMaxVitals(int hp, int mana, int moves) {
			Message msg = myhandler.obtainMessage(MESSAGE_MAXVITALS);
			Bundle b = msg.getData();
			b.putInt("maxhp", hp);
			b.putInt("maxmp", mana);
			b.putInt("maxmoves", moves);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}
		public void updateVitals(int hp, int mana, int moves) {
			/*Message msg = myhandler.obtainMessage(MESSAGE_VITALS);
			Bundle b = msg.getData();
			b.putInt("hp", hp);
			b.putInt("mp", mana);
			b.putInt("moves", moves);
			msg.setData(b);
			myhandler.sendMessage(msg);*/
		}

		public void updateEnemy(int hp) throws RemoteException {
			//myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_ENEMYHP,hp,0));
		}

		public void updateVitals2(int hp, int mp, int maxhp, int maxmana,
				int enemy) throws RemoteException {
			/*Message m = myhandler.obtainMessage(MESSAGE_VITALS2);
			//if(this.get(list.data.MESSget(i))
			Bundle b = m.getData();
			b.putInt("HP", hp);
			b.putInt("MP", mp);
			b.putInt("MAXHP", maxhp);
			b.putInt("MAXMANA", maxmana);
			b.putInt("ENEMY",enemy);
			
			m.setData(b);
			myhandler.sendMessage(m);*/
		}
		
		public void luaOmg(int stateIndex) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TESTLUA,stateIndex,0));
		}

		public void updateTriggerDebugString(String str) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_TRIGGERSTR,str));
		}

		public int getPort() throws RemoteException {
			Intent i= MainWindow.this.getIntent();
			
			return (new Integer(i.getStringExtra("HOST")).intValue());
		}

		public String getHost() throws RemoteException {
			// TODO Auto-generated method stub
			Intent i= MainWindow.this.getIntent();
			
			return i.getStringExtra("HOST");
			
		}

		public String getDisplay() throws RemoteException {
			// TODO Auto-generated method stub
			Intent i= MainWindow.this.getIntent();
			
			return i.getStringExtra("DISPLAY");
		}

		public void switchTo(String connection) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_SWITCH,connection));
		}

		public void reloadBuffer() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_RELOADBUFFER);
		}

		public void loadWindowSettings() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_INITIALIZEWINDOWS);
		}
		
		public void markWindowsDirty() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_MARKWINDOWSDIRTY);
		}

		@Override
		public void markSettingsDirty() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_MARKSETTINGSDIRTY);
		}

		@Override
		public void setKeepLast(boolean keep) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_SETKEEPLAST, (keep==true) ? 1 : 0, 0));
		}

		@Override
		public void setOrientation(int orientation) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_SETORIENTATION,orientation,0));
		}

		@Override
		public void setKeepScreenOn(boolean value) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_SETKEEPSCREENON, (value == true) ? 1 : 0,0));
		}

		@Override
		public void setUseFullscreenEditor(boolean value)
				throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_USEFULLSCREENEDITOR,(value == true) ? 1 :0,0));
		}

		@Override
		public void setUseSuggestions(boolean value) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_USESUGGESTIONS,(value==true) ? 1 : 0,0));
		}

		@Override
		public void setCompatibilityMode(boolean value) throws RemoteException {
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_USECOMPATIBILITYMODE,(value==true) ? 1 : 0,0));
		}

		@Override
		public void setRegexWarning(boolean value) throws RemoteException {
			// TODO Auto-generated method stub
			myhandler.sendMessage(myhandler.obtainMessage(MESSAGE_SHOWREGEXWARNING,(value==true) ? 1 : 0,0));
		}
	};
	
	boolean windowsInitialized = false;
	//boolean landscape = false
	public void initiailizeWindows() {
		//ask the service for all the current windows for the connection.
		//List<WindowToken> windows =  null;
		//make windows in the order they are given, attach the callback and the view to the layout root.
		//mRootLayout.removeAllViews();
		
		//cleanupWindows();
		if(windowsInitialized == true) {
			//Log.e("WINDOW","ALREADY LOADED WINDOWS");
			return;
		}
		
		if(mWindows != null) {
			//Log.e("LUAWINDOW","cleaning up windows.");
		}
		cleanupWindows();
		
		Display display = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//if(supportsRotation()) {
		//	landscape = (display.getRotation() == Surface.ROTATION_180 || display.getRotation() == Surface.ROTATION_90) ? true : false;
		//} else {
			
		//}
		landscape = isLandscape();
		windowsInitialized = true;
		String displayname = "";
		
		try {
			mWindows = service.getWindowTokens();
			//displayname = service.getConnectedTo();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mWindows == null || mWindows.length == 0) {
			//Exception e = new Exception("No windows to show.");
			//throw new RuntimeException(e);
			synchronized(this) {
				while(mWindows == null || mWindows.length == 0) {
					try {
						this.wait(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					boolean done = false;
					//while(!done) {
						try {
							mWindows = service.getWindowTokens();
							if(mWindows != null) {
								if(mWindows.length > 0) {
									done = true;
								}
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					//}
				}
			}
		} 
			ApplicationInfo ai = null;
			try {
				ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String dataDir = ai.dataDir;
		
			//initialize windows.
			for(Object x : mWindows) {
				WindowToken w = null;
				if(x instanceof WindowToken) {
					w = (WindowToken)x;
				} else {
					//err.
				}
				if(MAIN_WINDOW_ID == -1) {
					MAIN_WINDOW_ID = w.getId();
				}
				initWindow(w,dataDir);
				
				
			}
			RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.window_container);
			
			for(Object x : mWindows) {
				WindowToken w = null;
				if(x instanceof WindowToken) {
					w = (WindowToken)x;
				}
				com.offsetnull.bt.window.Window v = (com.offsetnull.bt.window.Window)rl.findViewWithTag(w.getName());
				if(v != null) {
					v.runScriptOnCreate();
				} else {
					Log.e("WARNING","Could not load window: "+w.getName());
				}
			}
			//mRootLayout.requestLayout();
		//}
			
		//if(supportsActionBar()) {
			this.invalidateOptionsMenu();
		//}

		//bring the toolbar to the front
		android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
		myToolbar.bringToFront();
		//Debug.stopMethodTracing();
	}
	
	private void initWindow(WindowToken w,String dataDir) {
		RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.window_container);
		View v = rl.findViewWithTag(w.getName());
		if(v == null) {
			long start = System.currentTimeMillis();
			//if(w.getName().equals("chats")) {
			//	long sfs = System.currentTimeMillis();
			//	sfs = sfs + 10;
			//}
			Log.e("WINDOW","INITIALIZING WINDOW: " + w.getName() + " id:" + w.getId());
			com.offsetnull.bt.window.Window tmp = new com.offsetnull.bt.window.Window(dataDir,this,w.getName(),w.getPluginName(),myhandler,w.getSettings(),this);
			
			//determine the appropriate layout group to load.
			int screenLayout = this.getResources().getConfiguration().screenLayout;
			//boolean landscape = ((screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) == Configuration.SCREENLAYOUT_LONG_NO) ? true : false;
			
			//int longyesno = screenLayout & m
			int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			
			
			//RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			//p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			//p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) w.getLayout(screenSize, landscape);
			if(params == null) {
				params = (android.widget.RelativeLayout.LayoutParams) w.getLayout(screenSize, !landscape);
			}

			tmp.setLayoutParams(params);
			tmp.setTag(w.getName());
			tmp.setVisibility(View.GONE);
			tmp.setId(w.getId());
			rl.addView(tmp);
			
			windowMap.put(w.getName(), tmp);
			
			//RelativeLayout holder = new AnimatedRelativeLayout(mContext,tmp,this);
			//RelativeLayout.LayoutParams holderParams = new RelativeLayout.LayoutParams(w.getX()+w.getWidth(),w.getY()+w.getHeight());
			//holderParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			//holderParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			//holder.setPadding(w.getX(), w.getY(), 0, 0);
			//holder.setId(w.getId());
			//holder.setLayoutParams();
			
			//holder.addView(tmp);
			
			try {
				String body = service.getScript(w.getPluginName(),w.getScriptName());
				//TODO: this needs to be much harderly error checked.
				tmp.loadScript(body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tmp.setBufferText(w.isBufferText());
			try {
				service.registerWindowCallback(w.getDisplayHost(),w.getName(),tmp.getCallback());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(w.getBuffer() != null) {
				//tmp.addBytes(w.getBuffer().dumpToBytes(false), true);
				tmp.setBuffer(w.getBuffer());
			//construct border.
			}
			
			//attempt to construct a good-ly relative layout to hold the window and any children 
			
			tmp.setVisibility(View.VISIBLE);
			
			long dur = System.currentTimeMillis() - start;
			Log.e("WINDOW","Init Window ("+w.getName()+"): took:" + dur + " millis.");
		}
	}
	
	
	public void cleanupWindows() {
		RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.window_container);
		if(mWindows == null) return;
		for(Object x : mWindows) {
			if(x instanceof WindowToken) {
				WindowToken w = (WindowToken)x;
				View tmp = rl.findViewWithTag(w.getName());
				
				if(tmp instanceof com.offsetnull.bt.window.Window) {
					try {
						service.unregisterWindowCallback(w.getDisplayHost(), ((com.offsetnull.bt.window.Window)tmp).getCallback());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Log.e("WINDOW","SHUTTING DOWN WINDOW " + w.getName());
					((com.offsetnull.bt.window.Window)tmp).shutdown();
					
					
				}
			}
		}
		
		for(Object x : mWindows) {
			if(x instanceof WindowToken) {
				WindowToken w = (WindowToken)x;
				View tmp = rl.findViewWithTag(w.getName());
				if(tmp instanceof com.offsetnull.bt.window.Window) {
					((com.offsetnull.bt.window.Window)tmp).closeLua();
					windowMap.remove(w.getName());
					rl.removeView(tmp);
					tmp = null;
					Log.e("WINDOW","SHUT DOWN WINDOW" + w.getName() + "SUCCESS");
				}
			}
		}
		
		int counter = 0;
		/*while(rl.getChildCount() > 2) {
			View v = rl.getChildAt(rl.getChildCount()-1);
			if(v.getId() != 10) {
				rl.removeView(v);
			} else {
				rl.removeViewAt(rl.getChildCount()-2);
			}
		}*/
		View inputbar = rl.findViewById(10);
		View divider = rl.findViewById(40);
		rl.removeAllViews();
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		inputbar.setLayoutParams(p);
		RelativeLayout.LayoutParams pl = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,(int) (3*this.getResources().getDisplayMetrics().density));
		pl.addRule(RelativeLayout.ABOVE,10);
		//p.addRule(RelativeLayout.BELOW,6666);
		divider.setLayoutParams(pl);
		rl.addView(inputbar);
		rl.addView(divider);
	}
	
	public void callWindowScript(String window, String callback) {
		RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.window_container);
		
		com.offsetnull.bt.window.Window lview = (com.offsetnull.bt.window.Window)rl.findViewWithTag(window);
		if(lview != null) {
			lview.callFunction(callback,null);
		}
	}
	
	public void shutdownWindow(com.offsetnull.bt.window.Window window) {
		try {
			service.unregisterWindowCallback(window.getName(), window.getCallback());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isLandscape() {
	    Display getOrient = getWindowManager().getDefaultDisplay();
	    int orientation = Configuration.ORIENTATION_UNDEFINED;
	    if(getOrient.getWidth()==getOrient.getHeight()){
	        orientation = Configuration.ORIENTATION_SQUARE;
	    } else{ 
	        if(getOrient.getWidth() < getOrient.getHeight()){
	            orientation = Configuration.ORIENTATION_PORTRAIT;
	        }else { 
	             orientation = Configuration.ORIENTATION_LANDSCAPE;
	        }
	    }
	    if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	return true;
	    } else {
	    	return false;
	    }
	    
	}
	
	public double getStatusBarHeight() {
		return statusBarHeight;

	}
	
	public boolean isStatusBarHidden() {
		return isFullScreen;
	}
	
	public double getTitleBarHeight() {
		return titleBarHeight;
	}
	
	@Override
	public void onNewIntent(Intent i) {
		//this is if the activity is currently open, and a new intent has been posted.
		Log.e("new intent","new intent : " + i.getStringExtra("DISPLAY"));
		
		this.setIntent(i);
		/*try {
			service.windowShowing(true);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Intent i = this.getIntent();
		//Log.e("LOG","RESUMING WINDOW WITH INTENT: display="+i.getStringExtra("DISPLAY")+" host="+i.getStringExtra("HOST")+" port="+i.getStringExtra("PORT"));
		String display = i.getStringExtra("DISPLAY");
		
		try {
			if(!service.getConnectedTo().equals(display)) {
				//Log.e("LOG","ATTEMPTING TO SWITCH TO: " + display);
				service.switchTo(display);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		//try {
			//loadSettings();
	}

	public String getPathForPlugin(String mOwner) {
		try {
			String path = service.getPluginPath(mOwner);
			return path;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Activity getActivity() {
		// TODO Auto-generated method stub
		return (Activity)this;
	}

	@Override
	public boolean isPluginInstalled(String desired) throws RemoteException {
		boolean ret = service.isPluginInstalled(desired);
		return ret;
	}

	@Override
	public boolean checkWindowSupports(String desired, String function) {
		com.offsetnull.bt.window.Window window = windowMap.get(desired);
		if(window != null) {
			return window.checkSupports(function);
		}
		return false;
	}

	@Override
	public void windowCall(String desired, String function, String data) {
		com.offsetnull.bt.window.Window window = windowMap.get(desired);
		if(window != null) {
			window.callFunction(function,data);
		}
	}
	
	@Override
	public void windowBroadcast(String function, String data) {
		for(com.offsetnull.bt.window.Window window : windowMap.values()) {
			if(window.checkSupports(function)) {
				window.callFunction(function, data);
			}
		}
	}

	@Override
	public String getPluginOption(String plugin, String value) throws RemoteException {
		String ret = service.getPluginOption(plugin,value);
		return ret;
	}
}
