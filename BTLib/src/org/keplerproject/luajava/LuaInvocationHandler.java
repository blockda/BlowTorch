/*
 * $Id: LuaInvocationHandler.java,v 1.4 2006/12/22 14:06:40 thiago Exp $
 * Copyright (C) 2003-2007 Kepler Project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.keplerproject.luajava;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.offsetnull.bt.service.Colorizer;

import android.net.Proxy;
import android.util.Log;

/**
 * Class that implements the InvocationHandler interface.
 * This class is used in the LuaJava's proxy system.
 * When a proxy object is accessed, the method invoked is
 * called from Lua
 * @author Rizzato
 * @author Thiago Ponte
 */
public class LuaInvocationHandler implements InvocationHandler
{
	private LuaObject obj;

	
	public LuaInvocationHandler(LuaObject obj)
	{
		this.obj = obj;
	}
	
	/**
	 * Function called when a proxy object function is invoked.
	 */
  public Object invoke(Object proxy, Method method, Object[] args) throws LuaException
  {
    synchronized(obj.L)
    {
	  	String methodName = method.getName();
	  	LuaObject func    = obj.getField(methodName);
	  	
	  	if ( func.isNil() )
	  	{
	  		return null;
	  	}
	  	
	  	try {
		  	Class retType = method.getReturnType();
		  	Object ret;
	
		  	// Checks if returned type is void. if it is returns null.
		  	if ( retType.equals( Void.class ) || retType.equals( void.class ) )
		  	{
		  		func.call( args , 0 );
		  		ret = null;
		  	}
		  	else
		  	{
		  		ret = func.call(args, 1)[0];
		  		if( ret != null && ret instanceof Double )
		  		{
		  		  ret = LuaState.convertLuaNumber((Double) ret, retType);
		  		}
		  	}
		  	
		  	return ret;
	  	} catch(LuaException e) {
	  		e.printStackTrace();
	  		
	  		StringWriter sw = new StringWriter();
	  		PrintWriter pw = new PrintWriter(sw);
	  		e.printStackTrace(pw);
	  		String error = sw.toString(); // stack trace as a string
	  		
	  		error = "\n" + Colorizer.getRedColor() + "Error in lua proxy object:\n" + e.getLocalizedMessage() + Colorizer.getWhiteColor();
	  		obj.L.getGlobal("debug");
	  		obj.L.getField(obj.L.getTop(), "traceback");
	  		obj.L.remove(-2);
			
	  		obj.L.getGlobal("Note");
	  		
			//if(obj.L.getLuaObject(-1).isFunction()) {
				
				//need to start iterating the given map, re-creating the table on the other side.
				//pushTable("",obj);
				obj.L.pushString(error);
				//obj.L.pushNumber(2);
				int ret = obj.L.pcall(1, 1, -3);
				if(ret !=0) {
					//displayLuaError("WindowXCallT Error:" + obj.L.getLuaObject(-1).getString());
					//crazy i don't think this can happen.
					Log.e("DFG", "failure");
				} else {
					//success!
					Log.e("DFG", "success");
					obj.L.pop(2);
				}
				
			//} else {
			//	obj.L.pop(2);
			//	Log.e("DFG", "not a function");
			//}
	  		
	  		
	  		/*StringWriter sw = new StringWriter();
	  		PrintWriter pw = new PrintWriter(sw);
	  		e.printStackTrace(pw);
	  		obj.L.pushString(sw.toString()); // stack trace as a string */
	  		
	  	}
	  	return null;
	  }
  }
}
