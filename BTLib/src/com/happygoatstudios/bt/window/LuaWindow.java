package com.happygoatstudios.bt.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

	//protected static final int MESSAGE_PROCESSXCALLT = 3;

	public static final int MESSAGE_PROCESSXCALLS = 4;

	protected final int MESSAGE_REDRAW = 1;
	
	LuaState L = null;
	//DrawFunction draw = null;
	//LuaPaint l = null;
	int mAnchorTop;
	int mAnchorLeft;
	int mWidth;
	int mHeight;
	String mName;
	String mOwner;
	Handler mHandler;
	Rect mBounds = null;
	Context mContext = null;
	Handler mainHandler = null;
	LayerManager mManager = null;
	boolean constrictWindow = false;
	public LuaWindow(Context context,LayerManager manager,String name,String owner,int x,int y,int width,int height,Handler mainWindowHandler) {
		super(context);
		try {
			Class di = Class.forName("android.content.DialogInterface$OnClickListener");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mOwner = owner;
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
				case MESSAGE_PROCESSXCALLS:
					LuaWindow.this.xcallS(msg.getData().getString("FUNCTION"),(String)msg.obj);
					break;
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
	
	
	
	protected void xcallS(String string, String str) {
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
				Log.e("LUAWINDOW","WindowXCallT Error:" + L.getLuaObject(-1).getString());
			} else {
				//success!
			}
			
		} else {
			
		}
	}
	
	private void pushTable(String key,Map<String,Object> map) {
		if(!key.equals("")) {
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
		}
	}



	public String getName() {
		return mName;
	}
	
	protected void shutdown() {
		mManager.shutdown(this);
	}

	protected void onSizeChanged(int w,int h,int oldw,int oldh) {
		/*if(bmp != null) {
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
		L.setGlobal("canvas");*/
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
				Log.e("LUAWINDOW","Window("+mName+"): " + L.getLuaObject(-1).getString());
			}
		} else {
			Log.e("LUAWINDOW","Window("+mName+"): No OnSizeChanged Function Defined.");
		}
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
		PluginXCallSFunction pxcf = new PluginXCallSFunction(L);
		
		try {
			iv.register("invalidate");
			df.register("debugPrint");
			bf.register("getBounds");
			omf.register("addOptionCallback");
			taf.register("getTableAdapter");
			pxcf.register("PluginXCallS");
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
	
	//Bitmap bmp = null;
	//LuaCanvas surface = null;
	Paint p = new Paint();
	//boolean dirty = false;
	Paint clearme = new Paint();
	
	public void onDraw(Canvas c) {
		
		c.save();
		if(constrictWindow) {
		c.clipRect(mAnchorLeft, mAnchorTop, mAnchorLeft+mWidth, mAnchorTop+mHeight);
		c.translate(mAnchorLeft, mAnchorTop);
		}
		//c.drawBitmap(bmp, 0, 0, null);
		L.getGlobal("debug");
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

		public void shutdown() throws RemoteException {
			mHandler.sendEmptyMessage(MESSAGE_SHUTDOWN);
		}

		public void xcallS(String function, String str) throws RemoteException {
			Message msg = mHandler.obtainMessage(MESSAGE_PROCESSXCALLS,str);
			msg.getData().putString("FUNCTION", function);
			mHandler.sendMessage(msg);
		}
	};

	public void loadScript(String body) {
		
		
		
		
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
	


	public void callFunction(String callback) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(callback);
		if(L.isFunction(L.getTop())) {
			int tmp = L.pcall(0, 1, -2);
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
