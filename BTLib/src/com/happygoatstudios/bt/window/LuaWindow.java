package com.happygoatstudios.bt.window;

import java.util.ArrayList;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.happygoatstudios.bt.service.IWindowCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.DialogInterface.OnClickListener;

public class LuaWindow extends View {
	protected static final int MESSAGE_SHUTDOWN = 2;

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
	Rect mBounds = null;
	Context mContext = null;
	Handler mainHandler = null;
	LayerManager mManager = null;
	boolean constrictWindow = false;
	public LuaWindow(Context context,LayerManager manager,String name,int x,int y,int width,int height,Handler mainWindowHandler) {
		super(context);
		try {
			Class di = Class.forName("android.content.DialogInterface$OnClickListener");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mManager = manager;
		mContext = context;
		this.mainHandler = mainWindowHandler;
		this.L = LuaStateFactory.newLuaState();
		
		if(x == 0 && y ==0 && width==0 && height == 0) {
			constrictWindow = false;
		} else {
			constrictWindow = true;
			mAnchorTop = y;
			mAnchorLeft = x;
			mWidth = width;
			mHeight = height;
		}
		mBounds = new Rect(mAnchorLeft,mAnchorTop,mAnchorLeft+width,mAnchorTop+height);
		
		mName = name;
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_SHUTDOWN:
					LuaWindow.this.shutdown();
					break;
				case MESSAGE_REDRAW:
					LuaWindow.this.invalidate();
					break;
				}
			}
		};
		
		initLua();
		
		
		
		clearme.setColor(0x00000000);
		clearme.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		DrawFunction draw = new DrawFunction(L);
		try {
			draw.register("draw");
		} catch (LuaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		L.pushJavaObject(this);
		L.setGlobal("view");
		
		//surface.drawColor(0x00000000);
		
		//surface.drawRe
		//l = new LuaPaint(this.getContext().getResources().getDisplayMetrics().density);
		
		//L.pushJavaObject(l);
		//L.setGlobal("paint");
		//l.setColor(0xFF00FF00);
		
		
		//l.setTextSize(26.0f);
		//surface.drawText("Lua Window, drawing from java", 30, 30, l);
		
		 
		
	}
	
	
	
	public String getName() {
		return mName;
	}
	
	protected void shutdown() {
		mManager.shutdown(this);
	}

	protected void onSizeChanged(int w,int h,int oldw,int oldh) {
		if(bmp != null) {
			bmp.recycle();
			bmp = null;
			surface = null;
		}
			//bmp = Bitmap.createBitmap(w,h,Config.ARGB_8888);
		//} else {
		if(constrictWindow) {
			bmp = Bitmap.createBitmap(mWidth,mHeight,Config.ARGB_8888);
		} else {
			bmp = Bitmap.createBitmap(w,h,Config.ARGB_8888);
		}
		//}
			
		surface = new LuaCanvas(bmp);
		//Bitmap.cre
		L.pushJavaObject(surface);
		L.setGlobal("canvas");
	}
	
	protected void onMeasure(int widthSpec,int heightSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthSpec),MeasureSpec.getSize(heightSpec));
		if(!constrictWindow) {
			mAnchorTop = 0;
			mAnchorLeft = 0;
			mWidth = MeasureSpec.getSize(widthSpec);
			mHeight = MeasureSpec.getSize(heightSpec);
		}
	}

	private void initLua() {
		L.openLibs();
		
		InvalidateFunction iv = new InvalidateFunction(L);
		DebugFunction df = new DebugFunction(L);
		BoundsFunction bf = new BoundsFunction(L);
		OptionsMenuFunction omf = new OptionsMenuFunction(L);
		TableAdapterFunction taf = new TableAdapterFunction(L);
		try {
			iv.register("invalidate");
			df.register("debugPrint");
			bf.register("getBounds");
			omf.register("addOptionCallback");
			taf.register("getTableAdapter");
		} catch (LuaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public boolean onTouchEvent(MotionEvent e) {
		boolean retval = false;
		
		//e.offsetLocation(-mAnchorLeft, -mAnchorTop);
		
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

	class DrawFunction extends JavaFunction {

		public DrawFunction(LuaState L) {
			super(L);
			
		}

		@Override
		public int execute() throws LuaException {
			//this takes no arguments and recieves none.
			LuaWindow.this.invalidate();
			return 0;
		}
		
	}
	
	/*public void onCreate(Bundle b) {
		//ooh, lua window booding up. 	
		
		//so this doesn't really do anything yet.
		
	}*/
	
	Bitmap bmp = null;
	LuaCanvas surface = null;
	Paint p = new Paint();
	//boolean dirty = false;
	Paint clearme = new Paint();
	
	public void onDraw(Canvas c) {
		
		c.save();
		c.clipRect(mAnchorLeft, mAnchorTop, mAnchorLeft+mWidth, mAnchorTop+mHeight);
		c.translate(mAnchorLeft, mAnchorTop);
		
		c.drawBitmap(bmp, 0, 0, null);
		/*L.getGlobal("debug");
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
			}
		}*/
		
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

		public void shutdown() throws RemoteException {
			mHandler.sendEmptyMessage(MESSAGE_SHUTDOWN);
		}
	};

	public void loadScript(String body) {
		
		int retv = L.LdoString("function tracer()\n"+
				"	local info = debuginfo.getinfo(1,\"Sl\")\n"+
				"	if(not info) then\n"+
				"		return \"No debug information available.\"\n"+
				"	else\n"+
				"       debug(\"%s:%d\",info.short_src,info.currentline)\n"+
				"		return string.format(\"[%s]:%d\",info.short_src,info.currentline)\n"+
				"	end\n"+
				"end\n\n");
		if(retv != 0) {
			Log.e("LUAWINDOW","Foo chan boo. Problem with custom tracer function.\n"+L.getLuaObject(L.getTop()).getString());
		} else {
			Log.e("LUAWINDOW","Custom tracer loaded.");
		}
		
		
		int ret = L.LdoString(body);
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
	
	private class BoundsFunction extends JavaFunction {

		public BoundsFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			this.L.pushJavaObject(mBounds);
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
	


	public void callFunction(String callback) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.isFunction(L.getTop())) {
			int tmp = L.pcall(0, 1, -3);
			if(tmp != 0) {
				Log.e("LUAWINDOW","Error calling script callback: "+L.getLuaObject(-1).getString());
			}
		}
	}
	
	/*private class TraceFunction extends JavaFunction {

		public TraceFunction(LuaState L) {
			super(L);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int execute() throws LuaException {
			// TODO Auto-generated method stub
			return 1;
		}
		
	}*/

	
	
	
}
