package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
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
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.happygoatstudios.bt.R;
//import com.happygoatstudios.bt.service.BaardTERMService;
import com.happygoatstudios.bt.service.*;

public class BaardTERMWindow extends Activity {
	
	String host;
	int port;
	
	Handler myhandler = null;
	boolean servicestarted = false;
	
	IBaardTERMService service = null;
	
	Object ctrl_tag = new Object();
	Processor the_processor = null;
	
	final static public int MESSAGE_PROCESS = 102;
	protected static final int MESSAGE_PROCESSED = 104;
	protected static final int MESSAGE_SENDDATAOUT = 105;
	
	GestureDetector gestureDetector = null;
	OnTouchListener gestureListener = null;
	
	ScrollView screen1 = null;
	//ScrollView screen2 = null;
	SlickView screen2 = null;
	ScrollView screen3 = null;
	EditText input_box = null;
	CommandKeeper history = null;
	
	ImageButton up_button = null;
	ImageButton down_button = null;
	ImageButton enter_button = null;
	ImageButton test_button = null;
	
	ImageButton up_button_c = null;
	ImageButton down_button_c = null;
	ImageButton enter_button_c  = null;
	boolean input_controls_expanded = false;
	
	boolean isBound = false;
	
	SpannableStringBuilder the_buffer = null;
	
	Boolean settingsLoaded = false; //synchronize or try to mitigate failures of writing button data, or failures to read data
	Boolean serviceConnected = false;
	Boolean isResumed = false;
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			service = IBaardTERMService.Stub.asInterface(arg1); //turn the binder into something useful
			
			//register callback
			try {
				service.registerCallback(the_callback);
				
			} catch (RemoteException e) {
				//do nothing here, as there isn't much we can do
			}
			synchronized(serviceConnected) {
				Log.e("WINDOW","SERVICE CONNECTED, SENDING NOTIFICATION");
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
	
	//public void onResized
	/*public void onSizeChanged(int w, int h,int oldw,int oldh) {
		Log.e("WINDOW","WINDOW RESIZED!!!!");
	}
	
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.e("WINDOW","RE-LAYOUT!!!!!");
	}*/
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.e("WINDOW","onCreate()");
		//Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
		
		
		
		setContentView(R.layout.window_layout);
		
		
		//get the main layout and see what we can do
		RelativeLayout lay = (RelativeLayout)findViewById(R.id.window_container);
		
		
		
		TextView tv = (TextView)findViewById(R.id.tv);
		tv.setPaintFlags(Paint.DITHER_FLAG|Paint.LINEAR_TEXT_FLAG); //no flags, see Paint.<FLAG> to turn stuff on.
		
		//set up the flinger
        gestureDetector = new GestureDetector(new BgestureListener(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        
        WebView wview = (WebView)findViewById(R.id.webview);
        //wview.getSettings().setTextSize(TextSize.SMALLEST);
        wview.getSettings().setStandardFontFamily("Serrif");
        
        wview.loadData("<html><body style=\"background-color:black;\"><span style=\"background-color:black;color:red;font-size=\"12px\";generic-family:monospace;\"><b>LSDJFLSDKJFHELLO FOLKS</b><br>A<br>b<br>d<br>c<br><br><br>f<br><br>A<br>b<br>d<br>c<br><br><br>f<br></span></body></html>", "text/html", "utf-8");
        
        
        
        history = new CommandKeeper(10);
        
        screen1 = (ScrollView)findViewById(R.id.mainscroller);
        //screen2 = (ScrollView)findViewById(R.id.secscroller);
        screen2 = (SlickView)findViewById(R.id.slickview);
        RelativeLayout l = (RelativeLayout)findViewById(R.id.slickholder);
        screen2.setParentLayout(l);
        EditText fill2 = (EditText)findViewById(R.id.filler2);
        //View fill2 = findViewById(R.id.filler2);
        fill2.setFocusable(false);
        fill2.setClickable(false);
        screen2.setNewTextIndicator(fill2);
        
        Animation alphaout = new AlphaAnimation(1.0f,0.0f);
        alphaout.setDuration(100);
        alphaout.setFillBefore(true);
        alphaout.setFillAfter(true);
        fill2.startAnimation(alphaout);
        
        screen2.setZOrderOnTop(false);
        //screen3 = (ScrollView)findViewById(R.id.thrirdscroller);
        
        FixedViewFlipper flipper = (FixedViewFlipper)findViewById(R.id.flippdipper);
       
        flipper.showNext();
        
        screen1.setOnTouchListener(gestureListener);
        screen2.setOnTouchListener(gestureListener);
        //screen2.setZOrderOnTop(false);
       
       // screen3.setOnTouchListener(gestureListener);
        
		
		screen1.setVerticalFadingEdgeEnabled(false);
		//screen2.setVerticalFadingEdgeEnabled(false);
		//screen3.setVerticalFadingEdgeEnabled(false);
		
        input_box = (EditText)findViewById(R.id.textinput);
        
        input_box.setOnKeyListener(new TextView.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
					
					String cmd = history.getNext();
					input_box.setText(cmd);
					return true;
				} else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
					String cmd = history.getPrev();
					input_box.setText(cmd);
					return true;
				}
				return false;
			}
   
        });
        
        
        input_box.setVisibility(View.VISIBLE);
        input_box.setEnabled(true);
        EditText filler = (EditText)findViewById(R.id.filler);
        filler.setFocusable(false);
        filler.setClickable(false);
        
        
        input_box.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        //input_box.setOnKeyListener(new EditText.OnKeyListener() {

        
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		//public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				//post message to conneciton thread
				//only do something if enter was pressed
				//InputConnection ic =null;
				//ic.
			
				//LOG.e("WINDOW","EDITOR ACTION" + actionId);
			
				if(event == null)  {
					//return false;
					//LOG.e("WINDOW","EDITOR EVENT HAD NULL EVENT");
				}
				
				if(actionId == EditorInfo.IME_ACTION_SEND) {

				//	return false;
					event = new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER);
					
				}
				
				if((event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)) {
					//Message tmp = ConnectionHandler.obtainMessage(105);
					String data = input_box.getText().toString();
					history.addCommand(data);
					Character cr = new Character((char)13);
					Character lf = new Character((char)10);
					String crlf = cr.toString() + lf.toString();
					String nosemidata = data.replace(";", crlf);
					nosemidata = nosemidata.concat(crlf);
					ByteBuffer buf = ByteBuffer.allocate(nosemidata.length());
		
					
					try {
						buf.put(nosemidata.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					}
				
					buf.rewind();
				
					byte[] buffbytes = buf.array();
					//Bundle bundle = new Bundle();
					//bundle.putByteArray("DATA", buffbytes);
					//tmp.setData(bundle);
					//ConnectionHandler.sendMessage(tmp);
					try {
						service.sendData(buffbytes);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					//send the box to the window, with newline because we are cool.
					//but delete that pesky carriage return, yuck
					data.replace(cr.toString(), "\n");
					data = data.concat("\n");
					screen2.addText(data,false);
					screen2.jumpToZero();
				
					input_box.setText("");
					if(actionId == EditorInfo.IME_ACTION_DONE) {

						//	return false;
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
				switch(msg.what) {
				case MESSAGE_PROCESS:
					try {
						Spannable data = the_processor.DoProcess(msg.getData().getByteArray("SEQ"));
						
						TextView tv = (TextView)findViewById(R.id.tv);
						
						tv.append(data);
						//set window text to data.
						
					} catch (UnsupportedEncodingException e) {
						
						//e.printStackTrace();
					}
					break;
				case MESSAGE_PROCESSED:
					TextView tv = (TextView)findViewById(R.id.tv);
					
					CharSequence seq = msg.getData().getCharSequence("SEQ");
					if(seq != null) {
						the_buffer.append(seq);
					}
					//prune
					//the_buffer.append(sstr);
					Bundle datal = null;
					try {
						datal = CountNewLine(new String(the_buffer.toString().getBytes("ISO-8859-1"),"ISO-8859-1"),30);
					} catch (UnsupportedEncodingException e) {
						//nothing to do, that's messed that they don't support isolatin
					}
					if(datal.getInt("TOTAL") > 30) {
						//LOG.e("WINDOW","TIME TO PRUNE AT: " + datal.getInt("PRUNELOC") + " FOUND " + datal.getInt("TOTAL") + " lines.");
						Object[] unusedspans = the_buffer.getSpans(0, datal.getInt("PRUNELOC"), Object.class);
						for(int i=0;i<unusedspans.length;i++) {
							the_buffer.removeSpan(unusedspans[i]);
						}
						Object[] array = the_buffer.getSpans(0,the_buffer.length(),Object.class);
						int size = array.length;
						//LOG.e("WINDOW","FOUND: " + size + " spans.\n");
						the_buffer.replace(0, datal.getInt("PRUNELOC"), "");
						
					}
					
					tv.setText(the_buffer);
					
					
					
					//TODO: don't scroll unless the scroll position is at the bottom.
					screen1.post(new Runnable() {
						public void run() {
							ScrollView tmp = (ScrollView)findViewById(R.id.mainscroller);

							
							tmp.fullScroll(ScrollView.FOCUS_DOWN);
							
							EditText tmpedit = (EditText)findViewById(R.id.textinput);
							tmpedit.requestFocus();
						}
					});
					break;
				case MESSAGE_HTMLINC:
					WebView web = (WebView)findViewById(R.id.webview);
					//web.getSettings().setStandardFontFamily("courier new");
					//web.getSettings().setFixedFontFamily("monospace");
					web.getSettings().setJavaScriptEnabled(true);
					//web.getSettings().set
					String html_header = "<html><head>" +
							"<style type=\"text/css\">" +
							".mybod{background-color:#555555;width:520px;" +
							"font-family:\"courier\";font-size:0.8em;" +
							"padding:0;margin:0;}</style>" +
							"</head><body class=mybod><div class=mybod>";
					String html_footer = "</div></body></html>";
					
					html_buffer = html_buffer.concat(msg.getData().getString("HTML"));
					
					//TextView debug_screen = (TextView)findViewById(R.id.tv2);
					//debug_screen.setText(html_buffer);
					
					//web.destroy();
					//web.load
					web.loadDataWithBaseURL("fake:////url.is.fake",html_header + html_buffer + html_footer, "text/html", "us-ascii","fake:////url.is.fake");
					//LOG.e("WINDOW","HTML DRAW ATTEMPTED WITH:" + html_buffer);
					break;
				case MESSAGE_RAWINC:
					//raw data incoming
					screen2.addText(msg.getData().getString("RAW"),false);
					break;
				case MESSAGE_BUFFINC:
					screen2.addText(msg.getData().getString("RAW"), true);
					break;
				case MESSAGE_SENDDATAOUT:
					byte[] data = msg.getData().getByteArray("DATA");
					try {
						String nosemidata = null;
						try {
							nosemidata = new String(data,"ISO-8859-1");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Character cr = new Character((char)13);
						Character lf = new Character((char)10);
						String crlf = cr.toString() + lf.toString();
						nosemidata = nosemidata.replace(";", crlf);
						try {
							service.sendData(nosemidata.getBytes("ISO-8859-1"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					screen2.addText(new String(msg.getData().getByteArray("DATA")),false);
					screen2.jumpToZero();
					break;
				default:
					break;
				}
			}
		};
		
		/*up_button = (ImageButton)findViewById(R.id.up_btn);
		down_button = (ImageButton)findViewById(R.id.down_btn);
		enter_button = (ImageButton)findViewById(R.id.enter_btn);
		
		up_button.setImageResource(android.R.drawable.arrow_up_float);
		down_button.setImageResource(android.R.drawable.arrow_down_float);
		enter_button.setImageResource(android.R.drawable.presence_online);
		
		up_button_c = (ImageButton)findViewById(R.id.up_btn_c);
		down_button_c = (ImageButton)findViewById(R.id.down_btn_c);
		enter_button_c = (ImageButton)findViewById(R.id.enter_btn_c);
		
		up_button.setImageResource(android.R.drawable.arrow_up_float);
		down_button.setImageResource(android.R.drawable.arrow_down_float);
		enter_button.setImageResource(android.R.drawable.presence_online); */
		
		test_button = (ImageButton)findViewById(R.id.test_btn);
		//test_button.setImageResource(android.R.drawable.btn_star_big_on);
		
		LinearLayout ctrl_target = (LinearLayout)findViewById(R.id.ctrl_target);
		
		AnimationSet set = new AnimationSet(true);
		
		Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		//Animation a = new AlphaAnimation(0f,1.0f);
		//LayoutAnimationController a = AnimationUtils.loadLayoutAnimation(this, R.anim.slide_left_in);
		a.setDuration(1000);
		
		set.addAnimation(a);
		
		LayoutAnimationController controller = new LayoutAnimationController(a,0.0f);
		
		//Animation b = AnimationUtils.loadLayoutAnimation(this, R.anim.slide_left_in);
		
		//ctrl_target.setLayoutAnimation(controller);
		
		//RelativeLayout rl = (RelativeLayout)findViewById(R.id.input_bar);
		//rl.setLayoutAnimation(controller);
		
		
		
		test_button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//change my layout parameters and add a new button.
				
				RelativeLayout rl = (RelativeLayout)findViewById(R.id.input_bar);
				LinearLayout l = (LinearLayout)findViewById(R.id.ctrl_target);
				if(l == null) {
					return;
				}
				RelativeLayout.LayoutParams orig_param = (RelativeLayout.LayoutParams)test_button.getLayoutParams();
				
				RelativeLayout.LayoutParams mod_param = (RelativeLayout.LayoutParams)test_button.getLayoutParams();
				
				//ImageButton tmp = new ImageButton(BaardTERMWindow.this);
				
				//tmp.setImageResource(android.R.drawable.btn_star_big_off);
				
				//tmp.setLayoutParams(orig_param);
				//tmp.setId(l.getId());
				//tmp.setTag(ctrl_tag);
				//LinearLayout l = (LinearLayout)findViewById(R.id.ctrl_target);
				
				//RelativeLayout rl = (RelativeLayout)BaardTERMWindow.this.findViewById(R.id.window_container);
				
				//RelativeLayout.LayoutParams new_param = new RelativeLayout.LayoutParams(80,LayoutParams.WRAP_CONTENT);
				
				//new_param.addRule(RelativeLayout.LEFT_OF,tmp.getId());
				
				//new_param.addRule(RelativeLayout.RIGHT_OF,input_box.getId());
				
				//Log.e("WINDOW","mod params:" + mod_param.debug(""));
				//rl.addView(tmp);
				//rl.removeView(l);
				
				LayoutInflater lf = LayoutInflater.from(l.getContext());
				RelativeLayout target = (RelativeLayout)lf.inflate(R.layout.input_controls, null);
				
				ImageButton dc = (ImageButton)target.findViewById(R.id.down_btn_c);
				ImageButton uc = (ImageButton)target.findViewById(R.id.up_btn_c);
				ImageButton ec = (ImageButton)target.findViewById(R.id.enter_btn_c);
				
				uc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						String cmd = history.getNext();
						input_box.setText(cmd);
						//Animation a = AnimationUtils.loadAnimation(BaardTERMWindow.this, R.anim.slide_left_out);
						//AnimationSet set = new AnimationSet(true);
						//Animation a = new AlphaAnimation((float)1.0,(float)0.0);
						//Animation a = new TranslateAnimation(0,-300,0,0);
						//a.setDuration(2000);
						//Log.e("WINDOW","Attempting to start animation");
						
						//up_button.setAnimation(a);
						//up_button.startAnimation(a);
						//test some shizzle
						//RelativeLayout l = (RelativeLayout)BaardTERMWindow.this.findViewById(R.id.window_container);
						//l.setLayoutAnimation
					}
				});
				
				
				dc.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View arg0) {
						String cmd = history.getPrev();
						input_box.setText(cmd);
					}
				});
				
				ec.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {
						//send a click to the edit box
						//input_box.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER));
						//LOG.e("WINDOW","trying to stimulate an enter keypress on the input_box");
						//gettext from the window and send a button message style message
						String data = input_box.getText().toString();
						history.addCommand(data);
						Character cr = new Character((char)13);
						Character lf = new Character((char)10);
						String crlf = cr.toString() + lf.toString();
						String nosemidata = data.replace(";", crlf);
						nosemidata = nosemidata.concat(crlf);
						ByteBuffer buf = ByteBuffer.allocate(nosemidata.length());
			
						
						try {
							buf.put(nosemidata.getBytes("UTF-8"));
						} catch (UnsupportedEncodingException e) {
							
							e.printStackTrace();
						}
					
						buf.rewind();
					
						byte[] buffbytes = buf.array();
						//Bundle bundle = new Bundle();
						//bundle.putByteArray("DATA", buffbytes);
						//tmp.setData(bundle);
						//ConnectionHandler.sendMessage(tmp);
						try {
							service.sendData(buffbytes);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
						//send the box to the window, with newline because we are cool.
						//but delete that pesky carriage return, yuck
						data.replace(cr.toString(), "\n");
						data = data.concat("\n");
						screen2.addText(data,false);
						screen2.jumpToZero();
					
						input_box.setText("");
					}
					
				});
				
				//dc.setImageResource(R.drawable.down2u);
				//uc.setImageResource(R.drawable.up2u);
				//ec.setImageResource(R.drawable.send2u);
				
				//Animation a = new AlphaAnimation(0.0f,1.0f);
				//Animation a = new TranslateAnimation(-1*target.getWidth(),0,0,0);
				Animation a = new TranslateAnimation(-178,0,0,0);
				//a.setDuration(100);
				//a.setFillBefore(true);
				//a.setFillAfter(true);
				//test_button.startAnimation(a);
				//input_box.startAnimation(a);
				//Animation a = AnimationUtils.loadAnimation(BaardTERMWindow.this, R.anim.slide_right_in);
				//Animation a = new AlphaAnimation(0f,1.0f);
				//LayoutAnimationController a = AnimationUtils.loadLayoutAnimation(this, R.anim.slide_left_in);
				//Animation a = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,80,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
				
				a.setDuration(120);
				AnimationSet set = new AnimationSet(true);
				set.addAnimation(a);
				
				LayoutAnimationController lac = new LayoutAnimationController(set,0.01f);
				
				rl.setLayoutAnimation(lac);
				
				//l.startAnimation(a);
				//l.addView(tmp);
				

				
				
				//FixedViewFlipper control_flip = (FixedViewFlipper)BaardTERMWindow.this.findViewById(R.id.control_flipper);
				
				//RelativeLayout rla = (RelativeLayout)control_flip.findViewById(R.id.input_ctrl_container_c);
				
				/*ImageButton dc = (ImageButton)target.findViewById(R.id.down_btn_c);
				ImageButton uc = (ImageButton)target.findViewById(R.id.up_btn_c);
				ImageButton ec = (ImageButton)target.findViewById(R.id.enter_btn_c);
				
				dc.setImageResource(android.R.drawable.arrow_down_float);
				uc.setImageResource(android.R.drawable.arrow_up_float);
				ec.setImageResource(android.R.drawable.presence_online);
				
				FixedViewFlipper test_flip = (FixedViewFlipper)BaardTERMWindow.this.findViewById(R.id.test_flipper);
				test_flip.showNext();
				*/
				
				//control_flip.showNext();
				//LOG.e("WINDOW","FLIPPER");
				
				if(input_controls_expanded) {
					//switch the image resource
					
					
					Animation outanim = new TranslateAnimation(178,0,0,0);
					outanim.setDuration(120);
					LayoutAnimationController lac2 = new LayoutAnimationController(outanim,0.0f);
					rl.setLayoutAnimation(lac2);
					l.removeAllViews();
					input_controls_expanded = false;
					test_button.setImageResource(R.drawable.sliderwidgetout);
				} else {
					//Animation b = new TranslateAnimation(0,80,0,0);
					//b.setDuration(300);
					//b.setFillAfter(true);
					//test_button.startAnimation(b);
					//input_box.startAnimation(b);
					//Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
					//Animation a = new AlphaAnimation(0f,1.0f);
					//LayoutAnimationController a = AnimationUtils.loadLayoutAnimation(this, R.anim.slide_left_in);
					//a.setDuration(1000);
					l.addView(target);
					input_controls_expanded = true;
					test_button.setImageResource(R.drawable.sliderwidgetin);
				}
				//ImageButton alt = (ImageButton)rl.findViewWithTag(ctrl_tag);
				//new_param.addRule(RelativeLayout.LEFT_OF,alt.getId());
				
				//test_button.setLayoutParams(new_param);
				
				//rl.removeView(l);
				
				
				//Log.e("WINDOW","mod params:" + mod_param.debug(""));
				//mod_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
				//mod_param.addRule(RelativeLayout.LEFT_OF,tmp.getId());
				
				//Log.e("WINDOW","mod params:" + mod_param.debug(""));
				
				//test_button.setLayoutParams(mod_param);
				

				
				//l.invalidate();
				
				
			}
		});
		
		

		
		/*View.OnTouchListener bgchangelistener = new View.OnTouchListener() {
			
			public boolean onTouch(View arg0, MotionEvent arg1) {
				//Log.e("WINDOW","SLICK GOT HERE");
				if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
					Log.e("WINDOW","SLICK GOT HERE");
					ImageButton btn = (ImageButton)arg0;
					btn.setBackgroundColor(0x555555);
					btn.invalidate();
				}
				
				if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
					ImageButton btn = (ImageButton)arg0;
					btn.setBackgroundColor(0x0A0A0A);
					btn.invalidate();
					
				}
				
				return false;
			}
		};
		
		up_button.setOnTouchListener(bgchangelistener);
		down_button.setOnTouchListener(bgchangelistener);
		enter_button.setOnTouchListener(bgchangelistener); */
		
		/*up_button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				String cmd = history.getNext();
				input_box.setText(cmd);
				//Animation a = AnimationUtils.loadAnimation(BaardTERMWindow.this, R.anim.slide_left_out);
				AnimationSet set = new AnimationSet(true);
				//Animation a = new AlphaAnimation((float)1.0,(float)0.0);
				Animation a = new TranslateAnimation(0,-300,0,0);
				a.setDuration(2000);
				Log.e("WINDOW","Attempting to start animation");
				
				//up_button.setAnimation(a);
				up_button.startAnimation(a);
				//test some shizzle
				//RelativeLayout l = (RelativeLayout)BaardTERMWindow.this.findViewById(R.id.window_container);
				//l.setLayoutAnimation
			}
		});
		
		down_button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				String cmd = history.getPrev();
				input_box.setText(cmd);
			}
		});
		
		enter_button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				//send a click to the edit box
				//input_box.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER));
				Log.e("WINDOW","trying to stimulate an enter keypress on the input_box");
				//gettext from the window and send a button message style message
				String data = input_box.getText().toString();
				history.addCommand(data);
				Character cr = new Character((char)13);
				Character lf = new Character((char)10);
				String crlf = cr.toString() + lf.toString();
				String nosemidata = data.replace(";", crlf);
				nosemidata = nosemidata.concat(crlf);
				ByteBuffer buf = ByteBuffer.allocate(nosemidata.length());
	
				
				try {
					buf.put(nosemidata.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
				}
			
				buf.rewind();
			
				byte[] buffbytes = buf.array();
				//Bundle bundle = new Bundle();
				//bundle.putByteArray("DATA", buffbytes);
				//tmp.setData(bundle);
				//ConnectionHandler.sendMessage(tmp);
				try {
					service.sendData(buffbytes);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				//send the box to the window, with newline because we are cool.
				//but delete that pesky carriage return, yuck
				data.replace(cr.toString(), "\n");
				data = data.concat("\n");
				screen2.addText(data);
				screen2.jumpToZero();
			
				input_box.setText("");
			}
			
		}); */
		
		//start the service if it isn't already started, a call to Context.startService shouldn't start a new service if it is already launched.
		//the icicle should contain a boolean to see if we have already started the service
		/*if(icicle != null) {
			Log.e("WINDOW","STARTED WITH SAVED INSTANCE STATE");
			servicestarted = icicle.getBoolean("CONNECTED");
			finishStart = icicle.getBoolean("FINISHSTART");
			if(servicestarted) {
				Log.e("WINDOW","SERVICE ALREADY STARTED!");
			}
		}*/
		screen2.setDispatcher(myhandler);
		screen2.setInputType(input_box);
		input_box.bringToFront();
		//icicile is out, prefs are in
		
		synchronized(settingsLoaded) {
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME,0);
		
		servicestarted = prefs.getBoolean("CONNECTED",false);
		finishStart = prefs.getBoolean("FINISHSTART", true);
		
		/*String buttons = prefs.getString("BUTTONS", null);
		Log.e("WINDOW","WORKING ON STRING" +buttons);
		if(buttons != null && buttons != "") {
			String[] b_line = buttons.split("&&");
			for(int i=0;i<b_line.length;i++) {
				Log.e("WINDOW","WORKING ON BUTTON" + b_line[i]);
				String[] elements = (b_line[i]).split("\\|\\|");
				for(int z = 0;z<elements.length;z++) {
					Log.e("WINDOW","ELEMENT:" + elements[z]);
				}
				//Log.e("WINDOW","SPLIT INTO X:"+elements[0] + " Y: "+elements[1] + " STR:" + elements[2]);
				int x = new Integer(elements[0]).intValue();
				int y = new Integer(elements[1]).intValue();
				String str = null;
				String lbl = null;
				if(elements.length > 2) {
					str = elements[2];
					if(elements.length > 3) {
						lbl = elements[3];
					} else {
						lbl = "";
					}
				} else {
					str = "";
				}
				
				Message msg = screen2.buttonaddhandler.obtainMessage(SlickView.MSG_CREATEBUTTON);
				Bundle b = msg.getData();
				b.putInt("X", x);
				b.putInt("Y", y);
				b.putString("THETEXT",str);
				b.putString("THELABEL",lbl);
				msg.setData(b);
				screen2.buttonaddhandler.sendMessage(msg);
				
			}
		}*/
		
		int count = prefs.getInt("BUTTONCOUNT", 0);
		for(int i = 0;i<count;i++) {
			//get button string
			String data = prefs.getString("BUTTON"+i, "");
			//Parcel p = Parcel.obtain();
			//byte[] bytedata = null;
			//try {
			//	bytedata = data.getBytes("ISO-8859-1");
			//} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			//p.unmarshall(bytedata, 0, bytedata.length);
			//Log.e("WINDOW","UNMARSHALLING:" + data);
			//Bundle tmp = p.readBundle();
			
			//SlickButtonData d = tmp.getParcelable("DATA");
			
			//LOG.e("WINDOW","SBD CONTAINS:" + data);
			Message msg = screen2.buttonaddhandler.obtainMessage(103, data);
			screen2.buttonaddhandler.sendMessage(msg);
			
			
		}
		
		settingsLoaded.notify();
		settingsLoaded = true;
		} /*END SYNCHRONIZATION!*/
		//get the saved buffer from the prefs
		if(icicle != null) {
			CharSequence seq = icicle.getCharSequence("BUFFER");
			if(seq != null) {
				//the_buffer = new SpannableStringBuilder();
				screen2.setBuffer((new StringBuffer(seq).toString()));
			} else {
				//the_buffer = new SpannableStringBuilder();
				
			}
		} else {
			//the_buffer = new StringBuffer();
		}
		
		if(!servicestarted) {
			//start the service
			this.startService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()));
			servicestarted = true;
		}
		
		
		//give it some time to launch
		synchronized(this) {
			try {
				this.wait(5);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
		
		//if the service was already started, just bind to the service.
		//bindService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()), mConnection, 0); //do not auto create
		//isBound = true;
		//Log.e("WINDOW","Bound service in onCreate()");
		//if(passed) {
		//	Log.e("STUFF","KALU-KALE! BINDSERVICE RETURNED TRUE");
		//}
		//after successful binding, we need to set the connection data provided to us by the launcher.
		
		
	
	}
	
	/*public void onConfigurationChanged(Configuration newconfig) {
		//
		super.onConfigurationChanged(newconfig);
		Log.e("WINDOW","INTERCEPTING CONFIGURATION CHANGE!");
	}*/
	
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0,100,0,"Triggers");
		menu.add(0,101,0,"Options");
		menu.add(0,102,0,"Slick Buttons");
		
		return true;
		
	}
	
	public void onBackPressed() {
		//Log.e("WINDOW","BACK PRESSED TRAPPED");
		
		//show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(BaardTERMWindow.this);
		builder.setMessage("Keep service running in background?");
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                BaardTERMWindow.this.dirtyExit();
		                BaardTERMWindow.this.finish();
		           }
		       });
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //dialog.cancel();
		        	   BaardTERMWindow.this.cleanExit();
		        	   BaardTERMWindow.this.finish();
		           }
		       });
		//AlertDialog alert = builder.create();
		builder.create();
		builder.show();
		//alert.show();
		
		//super.onBackPressed();
	}
	
	public void cleanExit() {
		//we want to kill the service when we go.
		
		//shut down the service
		if(isBound) {
			try {
				//Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
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
		
		stopService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()));
		
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
		//Log.e("WINDOW","App being paused, onSaveInstanceState");
		//data.putBoolean("CONNECTED", servicestarted);
		//data.putBoolean("FINISHSTART", finishStart);
		SlickView sv = (SlickView)findViewById(R.id.slickview);
		data.putCharSequence("BUFFER", sv.getBuffer());
		//Log.e("WINDOW","SAVING STATE WITH BUFFER: " + sv.getBuffer());
		
		
	}
	
	public void onRestoreInstanceState(Bundle data) {
		//Log.e("WINDOW","App being restored, onRestoreInstanceState");
		//servicestarted = data.getBoolean("CONNECTED");
		//finishStart = data.getBoolean("FINISHSTART");
		//the_buffer = new SpannableStringBuilder(data.getCharSequence("BUFFER"));
		SlickView sv = (SlickView)findViewById(R.id.slickview);
		sv.setBuffer((new StringBuffer(data.getCharSequence("BUFFER")).toString()));
		//Log.e("WINDOW","RESTORE STATE:"+ (new StringBuffer(data.getCharSequence("BUFFER")).toString()));

	}
	
	
	public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	protected static final int MESSAGE_HTMLINC = 110;
	protected static final int MESSAGE_RAWINC = 111;
	protected static final int MESSAGE_BUFFINC = 112;
	
	public void saveSettings() {
		//shared preferences
		synchronized(settingsLoaded) {
			while(settingsLoaded == false) {
				try {
					settingsLoaded.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
			String str = new String(b.getData().x +"||" +b.getData().y + "||"+b.getData().the_text+"||"+b.getData().the_label);
			serialbuttons.append(str + "&&");
			
			
			bstrings.add(str);
		}
		
		editor.putInt("BUTTONCOUNT", buttoncount);
		if(buttoncount == 0) {
			Log.e("WINDOW","SAVED BUTTONCOUNT OF ZERO!!!");
		}
		
		//editor.putString("BUTTONS", serialbuttons.toString());
		
		
		//so now we need to save the string 
		
		//String[] strings = (String[])bstrings.toArray();
		//for(int i = 0;i<strings.length;i++) {
			
		//}
		
		//actually put it in the bundle
		//editor.put
		
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
		//Log.e("WINDOW",str);
		
	}
	
	public void onStart() {
		Log.e("WINDOW","onStart()");
		super.onStart();
	}
	

	
	public void onPause() {
		Log.e("WINDOW","onPause()");
		
		if(isBound) {
			
			try {
				Log.e("WINDOW","Attempting to unregister the callback due to unbinding");
				if(service != null) {
					service.unregisterCallback(the_callback);
					Log.e("WINDOW","unregisterCallback called");
					unbindService(mConnection);
					Log.e("WINDOW","unBindService Called");
					saveSettings();
				} else {
					Log.e("WINDOW","Not unbinding because service is null.");
					
				}
			} catch (RemoteException e) {
				//e.printStackTrace();
				
			}
			
			
			//Log.e("WINDOW","Unbound connection at onPause");
			isBound = false;
			
		} else {
			Log.e("WINDOW","CALLING PAUSE WITH isBound = false");
			//mConnection.
			//unbindService(mConnection);
			
		}
		
		//Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		
		
		
		//SlickView sv = (SlickView)findViewById(R.id.slickview);
		//sv.stopDrawing();
		isResumed = false;
		super.onPause();
		
		//this.finish();
	}
	
	public void onStop() {
		Log.e("WINDOW","onStop()");
		super.onStop();
	}
	
	public void onDestroy() {
		Log.e("WINDOW","onDestroy()");
		super.onDestroy();
	}
	
	public void onResume() {
		
		

		Log.e("WINDOW","onResume()");
		//Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
		
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
			bindService(new Intent(com.happygoatstudios.bt.service.IBaardTERMService.class.getName()), mConnection, 0); //do not auto create
			Log.e("WINDOW","Bound connection at onResume");
			isBound = true;
			
			
			isResumed = true;
			synchronized(serviceConnected) {
				//while(serviceConnected == false) {
				//	Log.e("WINDOW","GOING TO SLEEP WAITING FOR NOTIFICATION FROM SERVICECONNECTION");
				//	try {
				//		serviceConnected.wait();
				//	} catch (InterruptedException e) {
				//		// TODO Auto- generated catch block
				//		e.printStackTrace();
				//	}
				
				//}
			}
		}
		

		
		//synchronized(service) {
		//	while(service == null) {
		//		try {
		//			service.wait();
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//	}
		//}
		
		//SlickView sv = (SlickView)findViewById(R.id.slickview);
		//sv.startDrawing();
		
		/*if(service != null) {
		try {
			service.requestBuffer();
		} catch (RemoteException e) {
			//e.printStackTrace();
		}
		}*/
		super.onResume();
	}
	
	public void onDestroy(Bundle saveInstance) {
		Log.e("WINDOW","onDestroy()");
		//unbindService(mConnection);
		super.onDestroy();
	}
	
	
	
	
	
	private void finishInitializiation() {
		Intent myintent = this.getIntent();
		
		//the_processor = new Processor(myhandler,service);
		
		if(!finishStart) {
			//service is already started, skip init by returning
			//Log.e("WINDOW","RETURNING BECAUSE THE SERVICE IS ALREADY STARTED!");
			try {
				service.requestBuffer();
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
			return;
		} else {
			//Log.e("WINDOW","FINISHING INITIALIZATION BECAUSE THE SERVICE JUST BOOTED UP!");
		}
		
		finishStart = false;
		
		//get extra data from the intent;
		//String display = myintent.getStringExtra("DISPLAY");
		String host = myintent.getStringExtra("HOST");
		String port = myintent.getStringExtra("PORT");
		
		//for now we are going to override:
		host = "aardmud.org";
		port = "4010";
		
		while(service == null) {
			host = "aardmud.org"; //waste time till onServiceConnected is called.
		}
		
		
		try {
			service.setConnectionData(host, new Integer(port).intValue());
		} catch (NumberFormatException e1) {
			//e1.printStackTrace();
		} catch (RemoteException e1) {
			//e1.printStackTrace();
		}
		
		
		
		
		
		
		//connect up the service and start pumping data
		try {
			service.initXfer();
		} catch (RemoteException e) {
			//e.printStackTrace();
		}
	}
	
	public void flingLeft() {
	    	//LOG.e("SCRO","SCROLL LEFT");
	    	FixedViewFlipper tmp = (FixedViewFlipper)findViewById(R.id.flippdipper);
	    	tmp.setInAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_left_in));
	    	tmp.setOutAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_left_out));
	    	tmp.showPrevious();
	    	
	    }
	    
	public void flingRight() {
	    	//LOG.e("SCRO","SCROLL RIGHT");
	    	FixedViewFlipper tmp = (FixedViewFlipper)findViewById(R.id.flippdipper);
	    	tmp.setOutAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_right_out));
	    	tmp.setInAnimation(AnimationUtils.loadAnimation(tmp.getContext(), R.anim.slide_right_in));
	    	tmp.showNext();
	    }
	
	
	private IBaardTERMServiceCallback.Stub the_callback = new IBaardTERMServiceCallback.Stub() {

		public void dataIncoming(byte[] seq) throws RemoteException {
			//create a new message and send it to my handler
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
			Message msg = myhandler.obtainMessage(MESSAGE_RAWINC);
			
			Bundle b = new Bundle();
			
			b.putString("RAW",raw);
			
			msg.setData(b);
			
			myhandler.sendMessage(msg);
			
		}
		
		public void rawBufferIncoming(String rawbuf) throws RemoteException {
			Message msg = myhandler.obtainMessage(MESSAGE_BUFFINC);
			Bundle b = msg.getData();
			b.putString("RAW", rawbuf);
			msg.setData(b);
			myhandler.sendMessage(msg);
		}
		
		
		
	
		
	};

}
