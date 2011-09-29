package com.happygoatstudios.bt.window;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.happygoatstudios.bt.service.IWindowCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LuaWindow extends View {
	protected final int MESSAGE_REDRAW = 1;
	
	LuaState L = null;
	//DrawFunction draw = null;
	//LuaPaint l = null;
	int mAnchorTop;
	int mAnchorLeft;
	int mWidth;
	int mHeight;
	String mName;
	Handler mHandler;
	
	public LuaWindow(Context context,String name,int x,int y,int width,int height) {
		super(context);
		
		this.L = LuaStateFactory.newLuaState();
		
		mAnchorTop = y;
		mAnchorLeft = x;
		mWidth = width;
		mHeight = height;
		mName = name;
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_REDRAW:
					LuaWindow.this.invalidate();
					break;
				}
			}
		};
		
		initLua();
		
		
		
		clearme.setColor(0x00000000);
		clearme.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		/*draw = new DrawFunction(L);
		try {
			draw.register("draw");
		} catch (LuaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bmp = Bitmap.createBitmap(width,height,Config.ARGB_8888);
		surface = new LuaCanvas(bmp);
		//Bitmap.cre
		L.pushJavaObject(surface);
		L.setGlobal("canvas");
		
		L.pushJavaObject(this);
		L.setGlobal("view");
		
		surface.drawColor(0x00000000);
		
		//surface.drawRe
		l = new LuaPaint(this.getContext().getResources().getDisplayMetrics().density);
		
		L.pushJavaObject(l);
		L.setGlobal("paint");
		l.setColor(0xFF00FF00);
		
		
		l.setTextSize(26.0f);
		//surface.drawText("Lua Window, drawing from java", 30, 30, l);
		*/
		 
		
	}
	
	private void initLua() {
		L.openLibs();
		
		InvalidateFunction iv = new InvalidateFunction(L);
		DebugFunction df = new DebugFunction(L);
		try {
			iv.register("invalidate");
			df.register("debugPrint");
		} catch (LuaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean onTouchEvent(MotionEvent e) {
		boolean retval = false;
		
		e.offsetLocation(-mAnchorLeft, -mAnchorTop);
		
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal("OnTouchEvent");
		if(!L.isFunction(L.getTop())) {
			return false;
		}
		
		L.pushJavaObject(e);
		
		int ret = L.pcall(1, 1, -3);
		if(ret != 0) {
			Log.e("LUAWINDOW","Error in onTouchEvent:"+L.getLuaObject(-1).getString());
		} else {
			retval = L.getLuaObject(-1).getBoolean();
			//Log.e("LUAWINDOW","TouchEvent called");
		}
		
		return retval;
		
	}

	/*class DrawFunction extends JavaFunction {

		public DrawFunction(LuaState L) {
			super(L);
			
		}

		@Override
		public int execute() throws LuaException {
			//this takes no arguments and recieves none.
			LuaWindow.this.invalidate();
			return 0;
		}
		
	}*/
	
	/*public void onCreate(Bundle b) {
		//ooh, lua window booding up. 	
		
		//so this doesn't really do anything yet.
		
	}*/
	
	/*Bitmap bmp = null;
	LuaCanvas surface = null;
	Paint p = new Paint();
	//boolean dirty = false;*/
	Paint clearme = new Paint();
	
	public void onDraw(Canvas c) {
		
		c.save();
		c.clipRect(mAnchorLeft, mAnchorTop, mAnchorLeft+mWidth, mAnchorTop+mHeight);
		c.translate(mAnchorLeft, mAnchorTop);
		
		c.drawRect(0,0,mWidth,mHeight, clearme);
		//L.getG
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		
		L.getGlobal("OnDraw");
		if(L.isFunction(L.getTop())) {
			L.pushJavaObject(c);
			//L.pushString("canvas");
			
			
			int ret = L.pcall(1, 1, -3);
			if(ret != 0) {
				Log.e("LUAWINDOW","Error calling OnDraw: " + L.getLuaObject(-1).toString());
			} else {
				Log.e("LUAWINDOW","OnDraw success!");
			}
		}
		
		c.restore();
		/*if(bmp != null) {
		c.drawBitmap(bmp,0, 0, p);
		} else {
			Log.e("LUAW","CANNOT DRAW A NULL BITMAP");
		}*/
		//if(dirty) {	
		//	surface.drawColor(0xFF000000);
		//	dirty = false;
		//}
	}
	
	public IWindowCallback.Stub getCallback() {
		return mCallback;
	}
	
	IWindowCallback.Stub mCallback = new IWindowCallback.Stub() {
		
		public void redraw() throws RemoteException {
			mHandler.sendEmptyMessage(MESSAGE_REDRAW);
		}
		
		public void rawDataIncoming(byte[] raw) throws RemoteException {
			//lua window, at this time will just eat all text that is thrown at it.
		}
		
		public boolean isWindowShowing() throws RemoteException {
			
			return true;
		}
		
		public String getName() throws RemoteException {
			return mName;
		}
	};

	public void loadScript(String body) {
		int ret = L.LdoString(body);
		if(ret != 0) {
			Log.e("LUAWINDOW","Error Loading Script: "+L.getLuaObject(L.getTop()).getString());
		}
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal("OnCreate");
		if(L.isFunction(L.getTop())) {
			int tmp = L.pcall(0, 1, -3);
			if(tmp != 0) {
				Log.e("LUAWINDOW","Calling onDraw: "+L.getLuaObject(-1).getString());
			} else {
				Log.e("LUAWINDOW","OnCreate Success!");
			}
		}
	}
	
	private class InvalidateFunction extends JavaFunction {

		public InvalidateFunction(LuaState L) {
			super(L);
		}

		@Override
		public int execute() throws LuaException {
			LuaWindow.this.invalidate();
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
}
