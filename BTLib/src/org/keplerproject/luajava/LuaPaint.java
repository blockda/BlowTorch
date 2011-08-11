package org.keplerproject.luajava;

import java.math.BigInteger;

import android.graphics.Paint;
import android.util.Log;

public class LuaPaint extends Paint {
	
	 public LuaPaint() {
		 super();
	 }
	 
	 public void setColor(int color) {
		 Log.e("LUA","ATTEMPTING TO SET LUA PATINT TO: "+Integer.toHexString(color));
		 super.setColor(color);
	 }
	 
	 public void color(double o) {
		 long l = (long)o;
		 //BigInteger foo = new BigInteger(o);
		 
		 Log.e("LUA","ATTEMPTING TO SET LUA PATINT TO: "+Long.toHexString(l) + " int:" + l);
		 
		 super.setColor((int)l);
	 }
	
}
