package com.happygoatstudios.bt.window;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.happygoatstudios.bt.service.IConnectionBinder;
import com.happygoatstudios.bt.service.WindowToken;

public class LayerManager {

	List<WindowToken> mWindows = null;
	Context mContext = null;
	IConnectionBinder mService = null;
	
	RelativeLayout mRootLayout = null;
	
	Handler rootHandler = null;
	
	public class Border {
		Point p1;
		Point p2;
		public Border() {
			p1 = new Point();
			p2 = new Point();
		}
	}
	
	public ArrayList<Border> borders = new ArrayList<Border>();
	
	public LayerManager(IConnectionBinder service, Context context, RelativeLayout root,Handler rootHandler) {
		this.rootHandler = rootHandler;
		mContext = context;
		mService = service;
		mRootLayout = root;
		
		/*Border b = new Border();
		b.p1.x = 0;
		b.p2.x = 100;
		b.p1.y = 0;
		b.p2.y = 100;
		borders.add(b);*/
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
								if(mWindows.size() > 0) {
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
		}
	}
	
	private void initWindow(WindowToken w) {
		View v = mRootLayout.findViewWithTag(w.getName());
		if(v == null) {
			
			Window tmp = new Window(mContext,this,w.getName(),w.getPluginName(),w.getX(),w.getY(),w.getWidth(),w.getHeight(),rootHandler);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			tmp.setLayoutParams(p);
			tmp.setTag(w.getName());
			
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
				tmp.addBytesImpl(w.getBuffer().dumpToBytes(false), true);
			//construct border.
			}
			
			//attempt to construct a good-ly relative layout to hold the window and any children 
			RelativeLayout holder = new AnimatedRelativeLayout(mContext,tmp);
			RelativeLayout.LayoutParams holderParams = new RelativeLayout.LayoutParams(w.getX()+w.getWidth(),w.getY()+w.getHeight());
			//RelativeLayout.LayoutParams holderParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			holderParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			holderParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			holder.setPadding(w.getX(), w.getY(), 0, 0);
			holder.setLayoutParams(holderParams);
			
			Button tmpb = new Button(mContext);
			
			RelativeLayout.LayoutParams btmpP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			btmpP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			btmpP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			
			tmpb.setLayoutParams(btmpP);
			
			tmpb.setOnClickListener(new ButtonClicker(tmp));
			//tmpb.add
			
			holder.addView(tmp);
			holder.addView(tmpb);
			Border top = new Border();
			Border bottom = new Border();
			Border left = new Border();
			Border right = new Border();
			
			top.p1.x = tmp.mBounds.left;
			top.p1.y = tmp.mBounds.top;
			top.p2.x = tmp.mBounds.right;
			top.p2.y = tmp.mBounds.top;
			
			bottom.p1.x = tmp.mBounds.left;
			bottom.p1.y = tmp.mBounds.bottom;
			bottom.p2.x = tmp.mBounds.right;
			bottom.p2.y = tmp.mBounds.bottom;
			
			left.p1.x = tmp.mBounds.left;
			left.p1.y = tmp.mBounds.top;
			left.p2.x = tmp.mBounds.left;
			left.p2.y = tmp.mBounds.bottom;
			
			right.p1.x = tmp.mBounds.right;
			right.p1.y = tmp.mBounds.top;
			right.p2.x = tmp.mBounds.right;
			right.p2.y = tmp.mBounds.bottom;
			
			borders.add(top);
			borders.add(bottom);
			borders.add(left);
			borders.add(right);
			
			mRootLayout.addView(holder);
		}
	}
	
	private class ButtonClicker implements View.OnClickListener {
		
		Window w = null;
		WindowExpander expand = null;
		WindowShrinker shrink = null;
		TranslateAnimation tExpand = new TranslateAnimation(0,0,-100,0);
		TranslateAnimation tShrink = new TranslateAnimation(0,0,0,-100);
		boolean toggle = true;
		public ButtonClicker(Window w) {
			this.w = w;
			//expand = new WindowExpander(w);
			shrink = new WindowShrinker(w,w.getParentView());
		}
		
		public void onClick(View v) {
			AnimatedRelativeLayout view = (AnimatedRelativeLayout) w.getParentView();
			mRootLayout.bringChildToFront(view);
			if(toggle) {
				w.updateDimensions(w.getWidth(),w.getMHeight()+100);
				tExpand.setDuration(450);
				tExpand.setFillAfter(false);
				tExpand.setFillBefore(true);
				//tExpand.setAnimationListener(expand);
				view.startAnimationX(tExpand,false);
				toggle = false;
			} else {
				//TranslateAnimation t = new TranslateAnimation(0,0,0,-100);
				
				//w.updateDimensions(w.getWidth(), w.getMHeight()-100);
				//w.updateDimensions(w.getWidth(),w.getMHeight()-100);
				tShrink.setDuration(450);
				tShrink.setFillAfter(false);
				tShrink.setFillBefore(true);
				//tShrink.setAnimationListener(shrink);
				view.startAnimationX(tShrink,true);
				toggle = true;
				
			}
		}
		
	}
	
	private class WindowExpander implements AnimationListener {

		Window w = null;
		View v = null;
		
		public WindowExpander(Window w,View v) {
			this.w = w;
			this.v = v;
		}

		public void onAnimationEnd(Animation a) {
			//w.updateAnchor(0, 0);
		}

		public void onAnimationRepeat(Animation a) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation a) {
			//w.updateDimensions(w.getWidth(), w.getMHeight()+100);
			//w.updateAnchor(0, -100);
			w.updateDimensions(w.getWidth(),w.getMHeight()+100);
			//w.updateAnchor(0,-100);
			//if(a instanceof TranslateAnimation) {
				//TranslateAnimation t = (TranslateAnimation)a;
				//t.
			//}
		}
		
		
	}
	
	private class WindowShrinker implements AnimationListener {

		Window w = null;
		View v;
		public WindowShrinker(Window w,View v) {
			this.w = w;
			this.v = v;
		}

		public void onAnimationEnd(Animation a) {
			v.clearAnimation();
			w.updateDimensions(w.getWidth(),w.getMHeight()-100);
			//w.updateAnchor(0,0);
		}

		public void onAnimationRepeat(Animation a) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation a) {
			
		}
		
	}
	private void initLuaWindow(WindowToken w) {
		View v = mRootLayout.findViewWithTag(w.getName());
		if(v == null) {
			
			LuaWindow tmp = new LuaWindow(mContext,this,w.getName(),w.getPluginName(),w.getX(),w.getY(),w.getWidth(),w.getHeight(),rootHandler);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			tmp.setLayoutParams(p);
			tmp.setTag(w.getName());
			
			try {
				String body = mService.getScript(w.getPluginName(),w.getScriptName());
				//TODO: this needs to be much harderly error checked.
				tmp.loadScript(body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				mService.registerWindowCallback(w.getName(),tmp.getCallback());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//construct border.
			Border top = new Border();
			Border bottom = new Border();
			Border left = new Border();
			Border right = new Border();
			
			top.p1.x = tmp.mBounds.left;
			top.p1.y = tmp.mBounds.top;
			top.p2.x = tmp.mBounds.right;
			top.p2.y = tmp.mBounds.top;
			
			bottom.p1.x = tmp.mBounds.left;
			bottom.p1.y = tmp.mBounds.bottom;
			bottom.p2.x = tmp.mBounds.right;
			bottom.p2.y = tmp.mBounds.bottom;
			
			left.p1.x = tmp.mBounds.left;
			left.p1.y = tmp.mBounds.top;
			left.p2.x = tmp.mBounds.left;
			left.p2.y = tmp.mBounds.bottom;
			
			right.p1.x = tmp.mBounds.right;
			right.p1.y = tmp.mBounds.top;
			right.p2.x = tmp.mBounds.right;
			right.p2.y = tmp.mBounds.bottom;
			
			borders.add(top);
			borders.add(bottom);
			borders.add(left);
			borders.add(right);
			
			mRootLayout.addView(tmp);
		}
	}

	
	private void initByteView(WindowToken w) {
		View v = mRootLayout.findViewWithTag(w.getName());
		if(v == null) {
			//need to initialize.
			ByteView tmp = new ByteView(mContext,this);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			tmp.setTag(w.getName());
			tmp.setLayoutParams(p);
			
			
			tmp.setDisplayDimensions(w.getX(),w.getY(),w.getWidth(),w.getHeight());
			//register callback.
			tmp.setBufferText(w.isBufferText());
			try {
				mService.registerWindowCallback(w.getName(),tmp.getCallback());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tmp.addBytesImpl(w.getBuffer().dumpToBytes(false), true);
			tmp.setTag(w.getName());
			tmp.setName(w.getName());
			
			if(tmp.constrictWindow) {
				Border top = new Border();
				Border bottom = new Border();
				Border left = new Border();
				Border right = new Border();
				
				top.p1.x = tmp.anchorLeft;
				top.p1.y = tmp.anchorTop;
				top.p2.x = tmp.anchorLeft + tmp.constrictedWidth;
				top.p2.y = tmp.anchorTop;
				
				bottom.p1.x = tmp.anchorLeft;
				bottom.p1.y = tmp.anchorTop + tmp.constrictedHeight;
				bottom.p2.x = tmp.anchorLeft + tmp.constrictedWidth;
				bottom.p2.y = tmp.anchorTop + tmp.constrictedHeight;
				
				left.p1.x = tmp.anchorLeft;
				left.p1.y = tmp.anchorTop;
				left.p2.x = tmp.anchorLeft;
				left.p2.y = tmp.anchorTop + tmp.constrictedHeight;
				
				right.p1.x = tmp.anchorLeft + tmp.constrictedWidth;
				right.p1.y = tmp.anchorTop;
				right.p2.x = tmp.anchorLeft + tmp.constrictedWidth;
				right.p2.y = tmp.anchorTop + tmp.constrictedHeight;
				
				borders.add(top);
				borders.add(bottom);
				borders.add(left);
				borders.add(right);
			}
			mRootLayout.addView(tmp);
		} else {
			//already exists.
		}
	}

	public void cleanup() {
		for(Object x : mWindows) {
			if(x instanceof WindowToken) {
				WindowToken w = (WindowToken)x;
				View tmp = mRootLayout.findViewWithTag(w.getName());
				if(tmp instanceof ByteView) {
					try {
						mService.unregisterWindowCallback(w.getName(),((ByteView)tmp).getCallback());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if(tmp instanceof LuaWindow) {
					try {
						mService.unregisterWindowCallback(w.getName(), ((LuaWindow)tmp).getCallback());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				
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
		LuaWindow lview = (LuaWindow)mRootLayout.findViewWithTag(window);
		if(lview != null) {
			lview.callFunction(callback);
		}
	}

	public void shutdown(ByteView byteView) {
		try {
			mService.unregisterWindowCallback(byteView.getName(), byteView.getCallback());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void shutdown(LuaWindow luaWindow) {
		try {
			mService.unregisterWindowCallback(luaWindow.getName(), luaWindow.getCallback());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
