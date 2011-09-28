package com.happygoatstudios.bt.service.plugin.function;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import android.os.Handler;

import com.happygoatstudios.bt.service.plugin.Plugin;

public abstract class PluginFunction extends JavaFunction {
	
	Plugin mPlugin = null;
	Handler mHandler = null;
	
	public PluginFunction(LuaState L,Plugin p,Handler h) {
		super(L);
		this.mPlugin = p;
		mHandler = h;
	}
	

}
