package com.happygoatstudios.bt.window;

import java.math.BigInteger;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.util.Log;

public class LuaPaint extends Paint {
	 float density = 1.0f;
	 public LuaPaint(float density) {
		 super();
		 this.density = density;
	 }
	 
	 public LuaPaint() {
		 super();
		 this.density = 1.0f;
	 }
	 
	 public void setColor(String color) {
		 int i = Integer.valueOf(color, 16).intValue();
		 Log.e("LUA","ATTEMPTING TO SET LUA PAINT TO: "+color + " ival="+i);
		 super.setColor(i);
	 }
	 
	 /*public void setColor(int color) {
		 Log.e("LUA","ATTEMPTING TO SET LUA PATINT TO: "+Integer.toHexString(color));
		 super.setColor(color);
	 }*/
	 
	 /*public void color(double o) {
		 long l = (long)o;
		 //BigInteger foo = new BigInteger(o);
		 
		 Log.e("LUA","ATTEMPTING TO SET LUA PATINT TO: "+Long.toHexString(l) + " int:" + l);
		 
		 super.setColor((int)l);
	 }*/
	 
	 public float density() {
		 return density;
	 }
	 
	 public Mode getPorterDuffModeClear() {
		 return PorterDuff.Mode.CLEAR;
	 }
	
}
