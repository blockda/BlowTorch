package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
//import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.happygoatstudios.bt.R;
//import com.happygoatstudios.bt.service.BaardTERMService;
import com.happygoatstudios.bt.alias.AliasDialogDoneListener;
import com.happygoatstudios.bt.alias.AliasEditorDialog;
import com.happygoatstudios.bt.button.ButtonEditorDialog;
import com.happygoatstudios.bt.button.ButtonSetSelectorDialog;
import com.happygoatstudios.bt.button.SlickButton;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.legacy.FixedViewFlipper;
import com.happygoatstudios.bt.service.*;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.settings.HyperSettingsActivity;
import com.happygoatstudios.bt.trigger.TriggerSelectionDialog;

public class MainWindow extends Activity implements AliasDialogDoneListener {
	
	
	
	//public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	public String PREFS_NAME;
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
	protected static final int MESSAGE_COLORDEBUG = 675;
	protected static final int MESSAGE_DIRTYEXITNOW = 943;
	protected static final int MESSAGE_DOHAPTICFEEDBACK = 856;
	public static final int MESSAGE_DELETEBUTTONSET = 867;
	public static final int MESSAGE_CLEARBUTTONSET = 868;
	protected static final int MESSAGE_SHOWTOAST = 869;
	protected static final int MESSAGE_SHOWDIALOG = 870;
	public static final int MESSAGE_HFPRESS = 871;
	public static final int MESSAGE_HFFLIP = 872;
	
	
	protected boolean settingsDialogRun = false;
	
	private boolean autoLaunch = true;
	private boolean disableColor = false;
	private String overrideHF = "auto";
	private String overrideHFFlip = "auto";
	private String overrideHFPress = "auto";
	
	String host;
	int port;
	
	Handler myhandler = null;
	boolean servicestarted = false;
	
	IStellarService service = null;
	//Object ctrl_tag = new Object();
	Processor the_processor = null;
	

	
	GestureDetector gestureDetector = null;
	OnTouchListener gestureListener = null;
	
	
	//ScrollView screen2 = null;
	SlickView screen2 = null;
	
	//EditText input_box = null;
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
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			service = IStellarService.Stub.asInterface(arg1); //turn the binder into something useful
			
			//register callback
			try {
				service.registerCallback(the_callback);
				
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			synchronized(serviceConnected) {
				//Log.e("WINDOW","SERVICE CONNECTED, SENDING NOTIFICATION");
				serviceConnected.notify();
				serviceConnected = true;
			}
			finishInitializiation();
			
		}

		public void onServiceDisconnected(ComponentName arg0) {
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				service.unregisterCallback(the_callback);
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
	
	Pattern newline = Pattern.compile("\n");
	//Pattern newline = Pattern.compile("[\\x0D][\\x0A]");

	public Bundle CountNewLine(String ISOLATINstring,int maxlines) {
		
		Matcher match = newline.matcher(ISOLATINstring);
		
		int prunelocation = 0;
		int numberfound = 0;
		//boolean found = false;
		while(match.find()) {
			numberfound++;	
		}
		
		
		
		if(numberfound > maxlines) {
			int numtoprune = numberfound - maxlines;
			match.reset();
			for(int i = 0;i < numtoprune;i++) {
				if(match.find()) { //shouldalways be true
					prunelocation = match.start();
				}
			}
			//by the time we are here, the prunelocation is known
		}
		//LOG.e("WINDOW","FOUND: " + numberfound + " with prune location at: " + prunelocation);
		
		Bundle dat = new Bundle();
		dat.putInt("TOTAL", numberfound);
		dat.putInt("PRUNELOC", prunelocation);
		
		return dat;
	}
	
	public boolean finishStart = true;
	
	String html_buffer = new String();
	Vector<SlickButton> current_button_views = new Vector<SlickButton>();
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//Log.e("WINDOW","onCreate()");
		
		//set up the crash reporter
		//Thread.setDefaultUncaughtExceptionHandler(new com.happygoatstudios.bt.crashreport.CrashReporter(this));
		
		setContentView(R.layout.window_layout);
		
		String display_name = this.getIntent().getStringExtra("DISPLAY");
		Pattern invalidchars = Pattern.compile("\\W"); 
		Matcher replacebadchars = invalidchars.matcher(display_name);
		String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		prefsname = prefsname.replaceAll("/", "");
        //Log.e("WINDOW","LAUNCHING WITH CONFIGURATION FILE NAME:" + tmpstr);
        PREFS_NAME = prefsname; //kill off all white space in the display name, use it as the preference file
        history = new CommandKeeper(10);
        
        screen2 = (SlickView)findViewById(R.id.slickview);
        RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);
        screen2.setParentLayout(l);
        TextView fill2 = (TextView)findViewById(R.id.filler2);
        fill2.setFocusable(false);
        fill2.setClickable(false);
        screen2.setNewTextIndicator(fill2);
        
        Animation alphaout = new AlphaAnimation(1.0f,0.0f);
        alphaout.setDuration(100);
        alphaout.setFillBefore(true);
        alphaout.setFillAfter(true);
        fill2.startAnimation(alphaout);
        
        //RelativeLayout input_bar = (RelativeLayout)findViewById(R.id.input_bar);
        //ViewParent parent = input_bar.getParent();
        //parent.bringChildToFront(input_bar);
        //input_bar.set
        
        screen2.setZOrderOnTop(false);
        screen2.setOnTouchListener(gestureListener);
		
        EditText input_box = (EditText)findViewById(R.id.textinput);
        
        input_box.setOnKeyListener(new TextView.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				EditText input_box = (EditText)findViewById(R.id.textinput);
				if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					
					String cmd = history.getNext();
					try {
						if(service.isKeepLast()) {
							if(historyWidgetKept) {
								input_box.setText(history.getNext());
								historyWidgetKept=false;
							} else {
								input_box.setText(cmd);
							}
						} else {
							input_box.setText(cmd);
							//Log.e("WINDOW","UNFREAKY");
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getPrev();
					input_box.setText(cmd);
					return true;
				}
				//Log.e("WINDOW","Key event happened, invalidating view.");
				//input_box.invalidate();
				return false;
			}
   
        });
        
        input_box.setDrawingCacheEnabled(true);
        //input_box.addTextChangedListener()
        
        
        input_box.setVisibility(View.VISIBLE);
        input_box.setEnabled(true);
        TextView filler = (TextView)findViewById(R.id.filler);
        filler.setFocusable(false);
        filler.setClickable(false);
        
        
        input_box.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        

        
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
				EditText input_box = (EditText)findViewById(R.id.textinput);
				
			
				if(event == null)  {
					
				}
				
				if(actionId == EditorInfo.IME_ACTION_SEND) {
					event = new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER);
				}
				
				if((event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)) {
					myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
					screen2.jumpToZero();

					if(actionId == EditorInfo.IME_ACTION_DONE) {
							return true;
					} else { return true; }
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getNext();
					input_box.setText(cmd);
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
				EditText input_box = (EditText)findViewById(R.id.textinput);
				switch(msg.what) {
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
					dbuilder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
						
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
					Toast t = Toast.makeText(MainWindow.this, (String)msg.obj, Toast.LENGTH_SHORT);
					t.show();
					break;
				case MESSAGE_DELETEBUTTONSET:
					try {
						int count = service.deleteButtonSet((String)msg.obj);
						String message = "Deleted " + (String)msg.obj + " button set ";
						if(count > 0) {
							message +=  "with " + count + " buttons.";
						} else {
							message += ".";
						}
						
						Message reloadset = this.obtainMessage(MESSAGE_CHANGEBUTTONSET);
						reloadset.obj = service.getAvailableSet();
						reloadset.arg1 = 10;
						
						if(service.getLastSelectedSet().equals((String)msg.obj)) {
							message += "\nLoaded default button set.";
						}
						Toast cleared = Toast.makeText(MainWindow.this,message, Toast.LENGTH_SHORT);
						cleared.show();
						
						this.sendMessage(reloadset);
						

						
					} catch (RemoteException e4) {
						// TODO Auto-generated catch block
						e4.printStackTrace();
					}
					break;
				case MESSAGE_CLEARBUTTONSET:
					try {
						int count = service.clearButtonSet((String)msg.obj);
						Toast cleared = Toast.makeText(MainWindow.this,"Cleared " + count+" buttons from " + (String)msg.obj + " button set.", Toast.LENGTH_SHORT);
						cleared.show();
						if(service.getLastSelectedSet().equals((String)msg.obj)) {
							Message reloadset = this.obtainMessage(MESSAGE_CHANGEBUTTONSET);
							reloadset.obj = msg.obj;
							reloadset.arg1 = 10;
							this.sendMessage(reloadset);
						}
						
					} catch (RemoteException e4) {
						// TODO Auto-generated catch block
						e4.printStackTrace();
					}
					break;
				case MESSAGE_DOHAPTICFEEDBACK:
					/*int flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
					if(overrideHF) {
						flags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
					}
					input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, flags);*/
					DoHapticFeedback();
					break;
				case MESSAGE_DIRTYEXITNOW:
					//the service via an entered command ".closewindow" or something, to bypass the window asking if you want to close
					dirtyExit();
					MainWindow.this.finish();
					break;
				case MESSAGE_COLORDEBUG:
					//execute color debug.
					screen2.setColorDebugMode(msg.arg1);
					break;
				case MESSAGE_XMLERROR:
					//got an xml error, need to display it.
					String xmlerror = (String)msg.obj;
					
					//display it
					AlertDialog.Builder builder = new AlertDialog.Builder(MainWindow.this);
					builder.setPositiveButton("Acknowledge.", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					
					builder.setMessage("XML Error: " + xmlerror + "\nSettings have not been loaded.");
					builder.setTitle("Problem with XML File.");
					
					AlertDialog error = builder.create();
					error.show();
					break;
				case MESSAGE_RELOADBUTTONSET:
					
					break;
				case MESSAGE_NEWBUTTONSET:
					try {
						service.addNewButtonSet((String)msg.obj);
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					RelativeLayout clearb = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
					//for(SlickButton b : current_button_views) {
					//	clearb.removeView(b);
					//	clearb.removeV
					//}
					int pos = clearb.indexOfChild(screen2);
					int count = clearb.getChildCount();
					if(pos == 0) {
						clearb.removeViews(1, count-1);
					} else {
						clearb.removeViews(0,pos);
						clearb.removeViews(pos+1,count - pos);
					}
					//current_button_views.clear();
					//make the fake button.
					makeFakeButton();
					showNoButtonMessage(true);
					
					break;
				case MESSAGE_CHANGEBUTTONSET:
					RelativeLayout modb = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
					//get the new list
					try {
						
						List<SlickButtonData> newset = service.getButtonSet((String)msg.obj);
						
						if(newset != null) {
							
							int posm = modb.indexOfChild(screen2);
							int countm = modb.getChildCount();
							if(posm == 0) {
								modb.removeViews(1, countm-1);
							} else {
								modb.removeViews(0,posm);
								modb.removeViews(posm+1,countm - posm);
							}
							
							if(newset.size() > 0) {
								for(SlickButtonData tmp : newset) {
									SlickButton new_button = new SlickButton(modb.getContext(),0,0);
									new_button.setData(tmp);
									new_button.setDispatcher(this);
									new_button.setDeleter(this);
									modb.addView(new_button);
								}
							} else {
								makeFakeButton();
								if(msg.arg1 != 10) showNoButtonMessage(false);
							}
						}
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					break;
				case MESSAGE_MODIFYBUTTON:
					SlickButtonData orig = msg.getData().getParcelable("ORIG_DATA");
					SlickButtonData mod = msg.getData().getParcelable("MOD_DATA");
					
					try {
						if(orig != null && mod != null) {
							//Log.e("WINDOW","MODIFY BUTTON " +orig.toString() + " TO " + mod.toString() + " attempting service call now");
							service.modifyButton(service.getLastSelectedSet(),orig,mod);
						} else {
							//Log.e("WINDOW","ATTEMPTED TO MODIFY BUTTON, BUT GOT NULL DATA");
						}
					} catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					//we modified the button, now load the set again to make the changes appear on the screen.
					Message reloadset = myhandler.obtainMessage(MESSAGE_CHANGEBUTTONSET);
					try {
						reloadset.obj = service.getLastSelectedSet();
					} catch (RemoteException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					myhandler.sendMessage(reloadset);
					break;
				case MESSAGE_LOADSETTINGS:
					//the service is connected at this point, so the service is alive and settings are loaded
					//TODO: HERE!
					//attemppt to load button sets.
					if(settingsDialogRun) {
						//so, if we a are here, then the dialog screen has been run.
						//we need to read in the values and supply them to the service
						settingsDialogRun = false;
						//Log.e("WINDOW","SETTINGS DIALOG HAS BEEN RUN! LOAD CHANGES!");
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainWindow.this);
						Integer font_size = new Integer(prefs.getString("FONT_SIZE", "18"));
						Integer line_space = new Integer(prefs.getString("FONT_SIZE_EXTRA", "2"));
						Integer max_lines = new Integer(prefs.getString("MAX_LINES", "300"));
						String font_name = prefs.getString("FONT_NAME", "monospace");
						boolean process_periods = prefs.getBoolean("PROCESS_PERIOD", true);
						boolean use_semi = prefs.getBoolean("PROCESS_SEMI", true);
						boolean use_extractui = prefs.getBoolean("USE_EXTRACTUI", false);
						boolean throttle_background = prefs.getBoolean("THROTTLE_BACKGROUND", false);
						boolean wifi_keepalive = prefs.getBoolean("WIFI_KEEPALIVE", true);
						boolean use_suggestions = prefs.getBoolean("USE_SUGGESTIONS", true);
						boolean keeplast = prefs.getBoolean("KEEPLAST", false);
						boolean bsbugfix = prefs.getBoolean("BACKSPACE_BUGFIX", false);
						boolean autolaunch = prefs.getBoolean("AUTOLAUNCH_EDITOR",true);
						boolean disablecolor = prefs.getBoolean("DISABLE_COLOR", false);
						String overrideHF = prefs.getString("OVERRIDE_HAPTICFEEDBACK","auto");
						String overrideHFPress = prefs.getString("HAPTIC_PRESS", "auto");
						String overrideHFFlip = prefs.getString("HAPTIC_FLIP", "auto");
						
						//Log.e("WINDOW","LOADED KEEPLAST AS " + keeplast);
						
						try {
							service.setFontSize(font_size);
							service.setFontSpaceExtra(line_space);
							service.setMaxLines(max_lines);
							service.setFontName(font_name);
							service.setProcessPeriod(process_periods);
							service.setUseExtractUI(use_extractui);
							service.setThrottleBackground(throttle_background);
							service.setSemiOption(use_semi);
							service.setKeepWifiActive(wifi_keepalive);
							service.setAttemptSuggestions(use_suggestions);
							service.setKeepLast(keeplast);
							service.setBackSpaceBugFix(bsbugfix);
							service.setAutoLaunchEditor(autolaunch);
							service.setDisableColor(disablecolor);
							service.setHapticFeedbackMode(overrideHF);
							service.setHFOnPress(overrideHFPress);
							service.setHFOnFlip(overrideHFFlip);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						String importPath = prefs.getString("IMPORT_PATH","");
						String exportPath = prefs.getString("EXPORT_PATH", "");
						String defaultSettings = prefs.getString("SETTINGS_TO_DEFAULT", "");
						

						
						SharedPreferences.Editor editor = prefs.edit();
						
						if(defaultSettings.equals("doit")) {
							editor.putString("SETTINGS_TO_DEFAULT", "");
							try {
								service.resetSettings();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						if(!importPath.equals("")) {
							//import needed
							//Log.e("WINDOW","WINDOW SENDING IMPORT REQUEST FOR " + importPath);
							try {
								service.LoadSettingsFromPath(importPath);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							editor.putString("IMPORT_PATH", "");
						}
						
						if(!exportPath.equals("")) {
							//export needed
							String fullPath = "/BlowTorch/" + exportPath;
							//Log.e("WINDOW","WINDOW SENDING EXPORT REQUEST TO PATH: " + fullPath);
							try {
								service.ExportSettingsToPath(fullPath);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							editor.putString("EXPORT_PATH","");
						}
						
						editor.commit();
						
					}
					
					
					try {
						//current_button_views.clear();
						List<SlickButtonData> buttons =  service.getButtonSet(service.getLastSelectedSet());
						
						RelativeLayout button_layout = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
						
						int posl = button_layout.indexOfChild(screen2);
						int countl = button_layout.getChildCount();
						if(posl == 0) {
							button_layout.removeViews(1, countl-1);
						} else {
							button_layout.removeViews(0,posl);
							button_layout.removeViews(posl+1,countl - posl);
						}
						//current_button_views.clear();
						
						if(buttons != null) {
							if(buttons.size() > 0) {
								for(SlickButtonData button : buttons) {
									SlickButton tmp = new SlickButton(MainWindow.this,0,0);
									tmp.setData(button);
									tmp.setDispatcher(this);
									tmp.setDeleter(this);
									button_layout.addView(tmp);
									//current_button_views.add(tmp);
								}
							} else {
								makeFakeButton();
								//show that the loaded set has no buttons.
								showNoButtonMessage(false);
							}
						}
						
						//screen2.setFontSize(service.getFontSize());
						//screen2.setLineSpace(service.getFontSpaceExtra());
						screen2.setCharacterSizes(service.getFontSize(), service.getFontSpaceExtra());
						screen2.setMaxLines(service.getMaxLines());
						
						//get the font name 
						String tmpname = service.getFontName();
						Typeface font = Typeface.MONOSPACE;
						//Log.e("WINDOW","FONT SELECTION IS:" + tmpname);
						if(tmpname.contains("/")) {
							//string is a path
							if(tmpname.contains(Environment.getExternalStorageDirectory().getPath())) {
								//Log.e("WINDOW","Loading font from SDCARD!");
								boolean available = false;
								String sdstate = Environment.getExternalStorageState();
								if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
									font = Typeface.createFromFile(tmpname);
								} else {
									font = Typeface.MONOSPACE;
								}
								//path is an sdcard path
							} else {
								//path is a system path
								//Log.e("WINDOW","Loading font from path!");
								font = Typeface.createFromFile(tmpname);
							}
							
						} else {
							if(tmpname.equals("monospace")) {
								font = Typeface.MONOSPACE;
							} else if(tmpname.equals("sans serif")) {
								font = Typeface.SANS_SERIF;
							} else if (tmpname.equals("default")) {
								font = Typeface.DEFAULT;
							}
						}
						
						screen2.setFont(font);
						
						
						if(service.getUseExtractUI()) {
							
							
							int current = input_box.getImeOptions();
							int wanted = current & (0xFFFFFFFF^EditorInfo.IME_FLAG_NO_EXTRACT_UI);
							
							//Log.e("WINDOW","ATTEMPTING TO SET FULL SCREEN IME| WAS: "+ Integer.toHexString(current) +" WANT: " + Integer.toHexString(wanted));
							input_box.setImeOptions(wanted);
							input_box.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
							BetterEditText better = (BetterEditText)input_box;
							better.setUseFullScreen(true);
						} else {
							int current = input_box.getImeOptions();
							int wanted = current | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
							//Log.e("WINDOW","ATTEMPTING TO SET NO EXTRACT IME| WAS: "+ Integer.toHexString(current) +" WANT: " + Integer.toHexString(wanted));
							input_box.setImeOptions(wanted);
							if(service.isAttemptSuggestions()) {
								input_box.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
							} else {
								input_box.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
									
							}
							BetterEditText better = (BetterEditText)input_box;
							better.setUseFullScreen(false);
							//Log.e("WINDOW","SETTINGS NOW "+Integer.toHexString(input_box.getImeOptions()));
						}
						
						
						if(service.isKeepLast()) {
							isKeepLast = true;
						} else {
							isKeepLast = false;
						}
						
						//handle auto launch
						autoLaunch = service.isAutoLaunchEditor();
						//handle disable color
						if(service.isDisableColor()) {
							//set the slick view debug mode to 3.
							screen2.setColorDebugMode(3);
						} else {
							screen2.setColorDebugMode(0);
						}
						//handle overridehf.
						overrideHF = service.HapticFeedbackMode();
						
						overrideHFPress = service.getHFOnPress();
						overrideHFFlip = service.getHFOnFlip();
						
						if(service.isBackSpaceBugFix()) {
							//Log.e("WINDOW","APPLYING BACK SPACE BUG FIX");
							BetterEditText tmp_bar = (BetterEditText)input_box;
							tmp_bar.setBackSpaceBugFix(true);
						} else {
							BetterEditText tmp_bar = (BetterEditText)input_box;
							tmp_bar.setBackSpaceBugFix(false);
							//Log.e("WINDOW","NOT APPLYING BACK SPACE BUG FIX");
						}
						
						InputMethodManager imm = (InputMethodManager) input_box.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.restartInput(input_box);
						//imm.
						//im
						//get the rest of the window options that are necessary to function
						
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case SlickView.MSG_DELETEBUTTON:
					ButtonEditorDialog d = new ButtonEditorDialog(MainWindow.this,R.style.SuperSweetDialog,(SlickButton)msg.obj,this);
					d.show();
					break;
				case SlickView.MSG_REALLYDELETEBUTTON:
					try {
						service.removeButton(service.getLastSelectedSet(), ((SlickButton)msg.obj).orig_data);
					} catch (RemoteException e1) {
						throw new RuntimeException(e1);
					}
					RelativeLayout layout = (RelativeLayout) MainWindow.this.findViewById(R.id.slickholder);
					layout.removeView((SlickButton)msg.obj);
					//current_button_views.remove((SlickView)msg.obj);
					//remove from the service.
					
					break;
				case MESSAGE_ADDBUTTON:
					SlickButtonData tmp = new SlickButtonData();
					tmp.setX(msg.arg1);
					tmp.setY(msg.arg2);
					tmp.setText(input_box.getText().toString());
					tmp.setLabel("LABEL");
					
					ColorSetSettings colorset = null;
					try {
						colorset = service.getCurrentColorSetDefaults();
					} catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					tmp.setLabelColor(colorset.getLabelColor());
					tmp.setPrimaryColor(colorset.getPrimaryColor());
					tmp.setFlipColor(colorset.getFlipColor());
					tmp.setSelectedColor(colorset.getSelectedColor());
					tmp.setLabelSize(colorset.getLabelSize());
					
					tmp.setWidth(colorset.getButtonWidth());
					tmp.setHeight(colorset.getButtonHeight());
					
					SlickButton new_button = new SlickButton(MainWindow.this,0,0);
					new_button.setData(tmp);
					new_button.setDeleter(this);
					new_button.setDispatcher(this);
					
					try {
						service.addButton(service.getLastSelectedSet(), tmp);
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					RelativeLayout hold = (RelativeLayout)MainWindow.this.findViewById(R.id.slickholder);
					hold.addView(new_button);
					
					/*int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
					if(overrideHF) {
						aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
					}
					new_button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
					*/
					DoHapticFeedback();
					
					//this may look real funky, but MSG_DELETEBUTTON really means to launch the editor.
					if(autoLaunch) {
						new_button.prepareToLaunchEditor();
						
						Message launcheditor = this.obtainMessage(SlickView.MSG_DELETEBUTTON);
						launcheditor.obj = new_button;
						this.sendMessage(launcheditor);
					}
					//current_button_views.add(new_button);
					
					break;
				case MESSAGE_PROCESSINPUTWINDOW:
					
					//input_box.debug(5);
					
					String pdata = input_box.getText().toString();
					history.addCommand(pdata);
					Character cr = new Character((char)13);
					Character lf = new Character((char)10);
					String crlf = cr.toString() + lf.toString();
					pdata = pdata.concat(crlf);
					ByteBuffer buf = ByteBuffer.allocate(pdata.length());
		
					
					try {
						buf.put(pdata.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						
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
					
					try {
						if(service.isKeepLast()) {
							//Log.e("WINDOW","ATTEMPTING TO SET LAST TO SELECTED");
							input_box.setSelection(0, input_box.getText().length());
							historyWidgetKept = true;
						} else {
							//Log.e("WINDOW","NOT KEEPING LAST");
							input_box.clearComposingText();
							input_box.setText("");
						}
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					//InputMethodManager imm = (InputMethodManager) input_box.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					//imm.restartInput(input_box);
					break;
				case MESSAGE_RAWINC:
					screen2.addText((String)msg.obj,false);
					break;
				case MESSAGE_BUFFINC:
					screen2.addText((String)msg.obj,false);
					break;
				case MESSAGE_SENDDATAOUT:
					try {
						service.sendData((byte[])msg.obj);
						
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					screen2.jumpToZero();
					
					
					break;
				default:
					break;
				}
			}
		};
		
		test_button = (ImageButton)findViewById(R.id.test_btn);
				
		test_button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//change my layout parameters and add a new button.
				
				
				RelativeLayout rl = (RelativeLayout)findViewById(R.id.input_bar);
				LinearLayout l = (LinearLayout)findViewById(R.id.ctrl_target);
				if(l == null) {
					return;
				}

				
				LayoutInflater lf = LayoutInflater.from(l.getContext());
				RelativeLayout target = (RelativeLayout)lf.inflate(R.layout.input_controls, null);
				
				ImageButton dc = (ImageButton)target.findViewById(R.id.down_btn_c);
				ImageButton uc = (ImageButton)target.findViewById(R.id.up_btn_c);
				ImageButton ec = (ImageButton)target.findViewById(R.id.enter_btn_c);
				
				uc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						EditText input_box = (EditText)findViewById(R.id.textinput);
						String cmd = history.getNext();
						try {
							if(service.isKeepLast()) {
								if(historyWidgetKept) {
									input_box.setText(history.getNext());
									historyWidgetKept = false;
								} else {
									input_box.setText(cmd);
								}
							} else {
								input_box.setText(cmd);
								//Log.e("WINDOW","UNFREAKY");
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
				
				dc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						EditText input_box = (EditText)findViewById(R.id.textinput);
						String cmd = history.getPrev();
						input_box.setText(cmd);
					}
				});
				
				ec.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {
						myhandler.sendEmptyMessage(MainWindow.MESSAGE_PROCESSINPUTWINDOW);
						screen2.jumpToZero();
					}
					
				});
				
				//pre multi screen support animation value 178px
				float amount = 180*MainWindow.this.getResources().getDisplayMetrics().density;
				Animation a = new TranslateAnimation(-1*amount,0,0,0);
				a.setDuration(120);
				AnimationSet set = new AnimationSet(true);
				set.addAnimation(a);
				
				LayoutAnimationController lac = new LayoutAnimationController(set,0.01f);
				
				rl.setLayoutAnimation(lac);
				
				if(input_controls_expanded) {
					//switch the image resource
					Animation outanim = new TranslateAnimation(amount,0,0,0);
					outanim.setDuration(120);
					LayoutAnimationController lac2 = new LayoutAnimationController(outanim,0.0f);
					rl.setLayoutAnimation(lac2);
					l.removeAllViews();
					input_controls_expanded = false;
					test_button.setImageResource(R.drawable.sliderwidgetout);
				} else {
					l.addView(target);
					input_controls_expanded = true;
					test_button.setImageResource(R.drawable.sliderwidgetin);
				}

			}
		});
		
		screen2.setDispatcher(myhandler);
		screen2.setButtonHandler(myhandler);
		screen2.setInputType(input_box);
		input_box.bringToFront();
		//icicile is out, prefs are in
		
		synchronized(settingsLoaded) {
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		servicestarted = prefs.getBoolean("CONNECTED",false);
		finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		
		int count = prefs.getInt("BUTTONCOUNT", 0);
		for(int i = 0;i<count;i++) {
			//get button string
			String data = prefs.getString("BUTTON"+i, "");

			Message msg = screen2.buttonaddhandler.obtainMessage(103, data);
			screen2.buttonaddhandler.sendMessage(msg);
			
			
		}
		
		settingsLoaded.notify();
		settingsLoaded = true;
		} 
		if(icicle != null) {
			CharSequence seq = icicle.getCharSequence("BUFFER");
			if(seq != null) {
				screen2.setBuffer((new StringBuffer(seq).toString()));
			} else {
			}
		} else {
		}
		
		if(!servicestarted) {
			//start the service
			this.startService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()));
			servicestarted = true;
		}
		
		
		//give it some time to launch
		synchronized(this) {
			try {
				this.wait(5);
			} catch (InterruptedException e) {
			}
		}
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
	
	private void makeFakeButton() {
		
		//the uglyest of ugly hacks.
		RelativeLayout modb = (RelativeLayout)findViewById(R.id.slickholder);
		//Log.e("WINDOW","ADDING FAKE BUTTON!");
		SlickButton fakey = new SlickButton(modb.getContext(),0,0);
		SlickButtonData fakedatay = new SlickButtonData();
		fakedatay.setPrimaryColor(0x00FFFFFF);
		fakedatay.setLabelColor(0x00FFFFFF);
		fakedatay.setFlipColor(0x00FFFFFF);
		fakedatay.setFlipLabelColor(0x00FFFFFF);
		fakedatay.setSelectedColor(0x00FFFFFF);
		fakedatay.setText("");
		fakedatay.setFlipCommand("");
		fakedatay.setLabel("");
		fakedatay.setFlipLabel("");
		fakedatay.setWidth(2);
		fakedatay.setHeight(2);
		fakedatay.setX(-10);
		fakedatay.setY(-10);
		
		fakey.setData(fakedatay);
		
		modb.addView(fakey);
		

		
	}
	
	private void showNoButtonMessage(boolean newset) {
		
		//if we are here then we loaded a blank button set, show the button set info message
		try {
			String message = "";
			if(newset) {
				message = "Button set " + service.getLastSelectedSet() + " created!";
			} else {
				message = "No buttons loaded for " + service.getLastSelectedSet() + " set.";
			}
			message += "\nNew buttons can be made by long pressing the window.";
			message += "\nButtons may be moved after press + hold.";
			message += "\nButtons may be edited after press + hold + hold.";
			Toast t = Toast.makeText(MainWindow.this, message, Toast.LENGTH_LONG);
			t.show();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0,99,0,"Aliases");
		menu.add(0,100,0,"Triggers");
		menu.add(0,103,0,"Options");
		menu.add(0,102,0,"Button Sets");
		
		return true;
		
	}
	
	public void onOptionMenuClose(Menu menu) {
		//if(showsettingsoptions) {
		//	showsettingsoptions = false;
		//}
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 99:
			AliasEditorDialog d = null;
			try {
				d = new AliasEditorDialog(this,service.getAliases(),this);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			d.setTitle("Edit Aliases:");
			d.show();
			break;
		case 102:
			//show the button set selector dialog
			ButtonSetSelectorDialog buttoneditor = null;
			try{
				buttoneditor = new ButtonSetSelectorDialog(this,myhandler,(HashMap<String,Integer>)service.getButtonSetListInfo(),service.getLastSelectedSet(),service);
				buttoneditor.setTitle("Select Button Set");
				buttoneditor.show();
			} catch(RemoteException e) {
				e.printStackTrace();
			}
			break;
		case 101:
			//SettingEditorDialog sedit = new SettingEditorDialog(this,service);
			//sedit.setTitle("Modify Settings...");
			//sedit.show();
			//showsettingsoptions = true;
			//settingsmenuclosed = false;
			MainWindow.this.myhandler.postDelayed(new Runnable() { public void run() { openOptionsMenu();}}, 1);
			//Intent settingintent = new Intent(this,HyperSettingsActivity.class);
			//this.startActivityForResult(settingintent, 0);
			break;
		case 103:
			//fix up the shared preferences so that it will actually work.
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainWindow.this);
			
			SharedPreferences.Editor edit = prefs.edit();
			
			try {
				//Log.e("WINDOW","FUCKING FUCK FUCK SERVICE IS MODIFYING THE FUCKING FILE");
				edit.putBoolean("THROTTLE_BACKGROUND",service.isThrottleBackground());
				edit.putBoolean("USE_EXTRACTUI", service.getUseExtractUI());
				edit.putBoolean("PROCESS_PERIOD", service.isProcessPeriod());
				edit.putBoolean("PROCESS_SEMI", service.isSemiNewline());
				edit.putBoolean("WIFI_KEEPALIVE", service.isKeepWifiActive());
				edit.putBoolean("USE_SUGGESTIONS", service.isAttemptSuggestions());
				edit.putBoolean("BACKSPACE_BUGFIX", service.isBackSpaceBugFix());
				edit.putBoolean("AUTOLAUNCH_EDITOR", service.isAutoLaunchEditor());
				edit.putBoolean("DISABLE_COLOR",service.isDisableColor());
				edit.putString("OVERRIDE_HAPTICFEEDBACK", service.HapticFeedbackMode());
				edit.putString("HAPTIC_PRESS", service.getHFOnPress());
				edit.putString("HAPTIC_FLIP", service.getHFOnFlip());
				
				
				edit.putBoolean("KEEPLAST", service.isKeepLast());
				edit.putString("FONT_SIZE", Integer.toString(service.getFontSize()));
				edit.putString("FONT_SIZE_EXTRA", Integer.toString(service.getFontSpaceExtra()));
				edit.putString("MAX_LINES", Integer.toString(service.getMaxLines()));
				edit.putString("FONT_NAME", service.getFontName());
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			edit.commit();
			
			Intent settingintent = new Intent(this,HyperSettingsActivity.class);
			this.startActivityForResult(settingintent, 0);
			//settingsmenuclosed = true;
			//showsettingsoptions = false;
			break;
		case 100:
			//launch the sweet trigger dialog.
			TriggerSelectionDialog trigger_selector = new TriggerSelectionDialog(this,service);
			trigger_selector.show();
			break;
		default:
			break;
		}
		return true;
	}
	
	
	Handler extporthandler = new Handler() {
		public void handleMessage(Message msg) {
			//so we are kludging out the new button set dialog to just be a "string enterer" dialog.
			//should be a full path /sdcard/something.xml
			String filename = (String)msg.obj;
			try {
				//Log.e("WINDOW","TRYING TO GET SERVICE TO WRITE A FILE FOR ME!");
				service.ExportSettingsToPath(filename);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	public void onBackPressed() {
		//Log.e("WINDOW","BACK PRESSED TRAPPED");
		
		//show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(MainWindow.this);
		builder.setMessage("Keep service running in background?");
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
	
	private void DoHapticFeedback() {
		if(overrideHF.equals("none")) {
			return;
		}
		
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHF.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		
		BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
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
		BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	private void DoHapticFeedbackFlip() {
		if(overrideHFFlip.equals("none")) {
			return;
		}
		
		int aflags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
		if(overrideHFFlip.equals("always")) {
			aflags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
		}
		
		BetterEditText input_box = (BetterEditText) this.findViewById(R.id.textinput);
		input_box.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, aflags);
	}
	
	public void cleanExit() {
		//we want to kill the service when we go.
		
		//shut down the service
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
		
		
		finishStart = true;
		servicestarted = false;
		
		saveSettings();
		
		stopService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()));
		
	}
	
	public void dirtyExit() {
		//we dont want to kill the service
		if(isBound) {
			
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				SlickView sv = (SlickView)findViewById(R.id.slickview);
				service.saveBuffer(sv.getBuffer());
				service.unregisterCallback(the_callback);
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			
			unbindService(mConnection);
			//Log.e("WINDOW","Unbound connection at cleanExit");
			isBound = false;
		}
		
		//save settings
		finishStart = false;
		servicestarted = true;
		//this.onPause();
		saveSettings();
	}
	
	public void onSaveInstanceState(Bundle data) {
		SlickView sv = (SlickView)findViewById(R.id.slickview);
		data.putCharSequence("BUFFER", sv.getBuffer());
	}
	
	public void onRestoreInstanceState(Bundle data) {

		SlickView sv = (SlickView)findViewById(R.id.slickview);
		sv.setBuffer((new StringBuffer(data.getCharSequence("BUFFER")).toString()));


	}
	
	public void saveSettings() {
		//shared preferences
		synchronized(settingsLoaded) {
			while(settingsLoaded == false) {
				try {
					settingsLoaded.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putBoolean("CONNECTED", servicestarted);
		editor.putBoolean("FINISHSTART", finishStart);
		
		//build the slickviews string arrays for buttons.
		SlickView sv = (SlickView)findViewById(R.id.slickview);
		Vector<SlickButton> btns = sv.buttons;
		Iterator<SlickButton> itrator = btns.listIterator();
		
		Vector<String> bstrings = new Vector<String>();
		StringBuffer serialbuttons = new StringBuffer();
		int buttoncount = 0;
		while(itrator.hasNext()) {
			SlickButton b = itrator.next();
			Parcel p = Parcel.obtain();
			Bundle tmp = new Bundle();
			tmp.putParcelable("DATA", b.getData());
			p.writeBundle(tmp);
			//p.unmarshall(arg0, arg1, arg2)

			editor.putString("BUTTON" + buttoncount,b.getData().toString());
			//LOG.e("WINDOW","SERIALIZING: " + b.toString());
			buttoncount++;
			String str = new String(b.getData().getX() +"||" +b.getData().getY() + "||"+b.getData().getText()+"||"+b.getData().getLabel());
			serialbuttons.append(str + "&&");
			
			
			bstrings.add(str);
		}
		
		editor.putInt("BUTTONCOUNT", buttoncount);
		if(buttoncount == 0) {
			//Log.e("WINDOW","SAVED BUTTONCOUNT OF ZERO!!!");
		}
		
		editor.commit();
		
		String str = "Save settings: ";
		if(servicestarted) {
			str = str + " servicestatred=true";
		} else {
			str = str + " servicestatred=false";
		}
		
		if(finishStart) {
			str = str + " finishStart=true";
		} else {
			str = str + " finishStart=false";
		}
		}
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == RESULT_OK) {
			//Log.e("WINDOW","onActivityResult()");
			settingsDialogRun = true;
		}
	}
	
	
	public void onStart() {
		//Log.e("WINDOW","onStart()");
		super.onStart();
	}
	

	
	public void onPause() {
		//Log.e("WINDOW","onPause()");
		
		if(isBound) {
			
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				if(service != null) {
					service.unregisterCallback(the_callback);
					
					unbindService(mConnection);
					
					saveSettings();
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
		super.onPause();
	
	}
	
	public void onStop() {
		//Log.e("WINDOW","onStop()");
		super.onStop();
	}
	
	public void onDestroy() {
		//Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	public void onResume() {
		
		

		//Log.e("WINDOW","onResume()");
		
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		servicestarted = prefs.getBoolean("CONNECTED", false);
		finishStart = prefs.getBoolean("FINISHSTART", true);
		
		
		String str = "Load settings (onResume): ";
		if(servicestarted) {
			str = str + " servicestatred=true";
		} else {
			str = str + " servicestatred=false";
		}
		
		if(finishStart) {
			str = str + " finishStart=true";
		} else {
			str = str + " finishStart=false";
		}
		
		//Log.e("WINDOW",str);
		
		
		if(!isBound) {
			bindService(new Intent(com.happygoatstudios.bt.service.IStellarService.class.getName()), mConnection, 0); //do not auto create
			//Log.e("WINDOW","Bound connection at onResume");
			isBound = true;
			
			
			isResumed = true;

		}
	
		super.onResume();
	}
	
	public void onDestroy(Bundle saveInstance) {
		//Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	private void finishInitializiation() {
		Intent myintent = this.getIntent();
		
		if(!finishStart) {
			
			try {
				service.requestBuffer();
			} catch (RemoteException e) {
				
			}
			return;
		} else {
			
		}
		
		finishStart = false;
		
		String host = myintent.getStringExtra("HOST");
		String port = myintent.getStringExtra("PORT");
		
		//for now we are going to override:
		//host = "aardmud.org";
		//port = "4010";
		
		
		while(service == null) {
			host = "aardmud.org"; //waste time till onServiceConnected is called.
		}
		
		
		try {
			service.setConnectionData(host, new Integer(port).intValue(),myintent.getStringExtra("DISPLAY"));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			
		}
		
		
		//connect up the service and start pumping data
		try {
			service.initXfer();
		} catch (RemoteException e) {
			//e.printStackTrace();
		}
	}
	
	/*public void flingLeft() {
    	FixedViewFlipper tmp = (FixedViewFlipper)findViewById(R.id.flippdipper);
    	tmp.setInAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_left_in));
    	tmp.setOutAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_left_out));
    	tmp.showPrevious();
	}
	    
	public void flingRight() {
    	FixedViewFlipper tmp = (FixedViewFlipper)findViewById(R.id.flippdipper);
    	tmp.setOutAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_right_out));
    	tmp.setInAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_right_in));
    	tmp.showNext();
	}*/
	
	
	private IStellarServiceCallback.Stub the_callback = new IStellarServiceCallback.Stub() {

		public void dataIncoming(byte[] seq) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_PROCESS);
			Bundle b = new Bundle();
			b.putByteArray("SEQ", seq);
			msg.setData(b);
			myhandler.sendMessage(msg);
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

		public void rawDataIncoming(String raw) throws RemoteException {
			
			Message msg = myhandler.obtainMessage(MESSAGE_RAWINC,raw);
			myhandler.sendMessage(msg);
			
		}
		
		public void rawBufferIncoming(String rawbuf) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_BUFFINC,rawbuf);
			myhandler.sendMessage(msg);
		}

		public void loadSettings() throws RemoteException {
			// TODO Auto-generated method stub
			myhandler.sendEmptyMessage(MESSAGE_LOADSETTINGS);
		}

		public void displayXMLError(String error) throws RemoteException {
			// TODO Auto-generated method stub
			Message xmlerror = myhandler.obtainMessage(MESSAGE_XMLERROR);
			xmlerror.obj = error;
			myhandler.sendMessage(xmlerror);
			
		}

		public void executeColorDebug(int arg) throws RemoteException {
			Message colordebug = myhandler.obtainMessage(MESSAGE_COLORDEBUG);
			colordebug.arg1 = arg;
			myhandler.sendMessage(colordebug);
		}

		public void invokeDirtyExit() throws RemoteException {
			myhandler.sendEmptyMessage(MESSAGE_DIRTYEXITNOW);
			
		}

		public void showMessage(String message) throws RemoteException {
			Message showmessage = myhandler.obtainMessage(MESSAGE_SHOWTOAST);
			showmessage.obj = message;
			myhandler.sendMessage(showmessage);
			
		}

		public void showDialog(String message) throws RemoteException {
			Message showdlg = myhandler.obtainMessage(MESSAGE_SHOWDIALOG);
			showdlg.obj = message;
			myhandler.sendMessage(showdlg);
		}
	};
	
	public void aliasDialogDone(ArrayList<String> items) {
		HashMap<String,String> map = new HashMap<String,String>();
		
		if(items.size() > 0) {
			for(int i=0;i<items.size();i++) {
				String[] parts = items.get(i).split("\\Q[||]\\E");
				map.put(parts[0], parts[1]);
			}
			try {
				service.setAliases(map);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			try {
				service.setAliases(map);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

		
	

}
