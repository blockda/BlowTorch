package com.happygoatstudios.bt.window;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Handler;
import android.os.RemoteException;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.happygoatstudios.bt.service.IConnectionBinder;
import com.happygoatstudios.bt.service.WindowToken;

public class LayerManager {

	WindowToken[] mWindows = null;
	Context mContext = null;
	IConnectionBinder mService = null;
	
	RelativeLayout mRootLayout = null;
	
	Handler rootHandler = null;
	
	boolean landscape = false;
	public LayerManager(IConnectionBinder service, Context context, RelativeLayout root,Handler rootHandler) {
		this.rootHandler = rootHandler;
		mContext = context;
		mService = service;
		mRootLayout = root;
		//WindowManager w = null;
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		landscape = (display.getRotation() == Surface.ROTATION_180 || display.getRotation() == Surface.ROTATION_90) ? true : false;
	}
	
	public void initiailize() {
		//ask the service for all the current windows for the connection.
		//List<WindowToken> windows =  null;
		//make windows in the order they are given, attach the callback and the view to the layout root.
		mRootLayout.removeAllViews();
		
		try {
			mWindows = mService.getWindowTokens();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mWindows == null) {
			//Exception e = new Exception("No windows to show.");
			//throw new RuntimeException(e);
			synchronized(this) {
				while(mWindows == null) {
					try {
						this.wait(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					boolean done = false;
					while(!done) {
						try {
							mWindows = mService.getWindowTokens();
							if(mWindows != null) {
								if(mWindows.length > 0) {
									done = true;
								}
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			//initialize windows.
			for(Object x : mWindows) {
				WindowToken w = null;
				if(x instanceof WindowToken) {
					w = (WindowToken)x;
				} else {
					//err.
				}
				initWindow(w);
				
				
			}
			mRootLayout.requestLayout();
		}
	}
	
	private void initWindow(WindowToken w) {
		View v = mRootLayout.findViewWithTag(w.getName());
		if(v == null) {
			if(w.getName().equals("chats")) {
				long sfs = System.currentTimeMillis();
				sfs = sfs + 10;
			}
			Window tmp = new Window(mContext,w.getName(),w.getPluginName(),rootHandler,w.getSettings());
			
			//determine the appropriate layout group to load.
			int screenLayout = mContext.getResources().getConfiguration().screenLayout;
			//boolean landscape = ((screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) == Configuration.SCREENLAYOUT_LONG_NO) ? true : false;
			
			//int longyesno = screenLayout & m
			int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			
			
			//RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			//p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			//p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			tmp.setLayoutParams(w.getLayout(screenSize,landscape));
			tmp.setTag(w.getName());
			tmp.setVisibility(View.GONE);
			tmp.setId(w.getId());
			mRootLayout.addView(tmp);
			
			//RelativeLayout holder = new AnimatedRelativeLayout(mContext,tmp,this);
			//RelativeLayout.LayoutParams holderParams = new RelativeLayout.LayoutParams(w.getX()+w.getWidth(),w.getY()+w.getHeight());
			//holderParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			//holderParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			//holder.setPadding(w.getX(), w.getY(), 0, 0);
			//holder.setId(w.getId());
			//holder.setLayoutParams();
			
			//holder.addView(tmp);
			
			try {
				String body = mService.getScript(w.getPluginName(),w.getScriptName());
				//TODO: this needs to be much harderly error checked.
				tmp.loadScript(body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tmp.setBufferText(w.isBufferText());
			try {
				mService.registerWindowCallback(w.getName(),tmp.getCallback());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(w.getBuffer() != null) {
				tmp.addBytes(w.getBuffer().dumpToBytes(false), true);
			//construct border.
			}
			
			//attempt to construct a good-ly relative layout to hold the window and any children 
			
			tmp.setVisibility(View.VISIBLE);
			
			
		}
	}

	public void cleanup() {
		for(Object x : mWindows) {
			if(x instanceof WindowToken) {
				WindowToken w = (WindowToken)x;
				View tmp = mRootLayout.findViewWithTag(w.getName());
				
				if(tmp instanceof Window) {
					try {
						mService.unregisterWindowCallback(w.getName(), ((Window)tmp).getCallback());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void callScript(String window, String callback) {
		Window lview = (Window)mRootLayout.findViewWithTag(window);
		if(lview != null) {
			lview.callFunction(callback);
		}
	}
	
	public void shutdown(Window window) {
		try {
			mService.unregisterWindowCallback(window.getName(), window.getCallback());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void bringToFront(AnimatedRelativeLayout a) {
		mRootLayout.bringChildToFront(a);
		//mRootLayout.rem
	}
}
