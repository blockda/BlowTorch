package com.happygoatstudios.bt.window;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaPaint;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class LuaWindow extends View {
	LuaState L = null;
	DrawFunction draw = null;
	LuaPaint l = null;
	public LuaWindow(Context context,int x,int y,int width,int height) {
		super(context);
		
		this.L = LuaStateFactory.newLuaState();
		initLua();
		draw = new DrawFunction(L);
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
		
	}
	
	private void initLua() {
		L.openLibs();
		
		
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
	
	public void onCreate(Bundle b) {
		//ooh, lua window booding up. 	
		
		//so this doesn't really do anything yet.
		
	}
	
	Bitmap bmp = null;
	LuaCanvas surface = null;
	Paint p = new Paint();
	//boolean dirty = false;
	public void onDraw(Canvas c) {
		if(bmp != null) {
		c.drawBitmap(bmp,0, 0, p);
		} else {
			Log.e("LUAW","CANNOT DRAW A NULL BITMAP");
		}
		//if(dirty) {	
		//	surface.drawColor(0xFF000000);
		//	dirty = false;
		//}
	}
}
