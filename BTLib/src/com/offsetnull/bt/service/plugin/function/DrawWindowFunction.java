package com.offsetnull.bt.service.plugin.function;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import android.os.Handler;

import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.service.plugin.Plugin;

public class DrawWindowFunction extends PluginFunction {

	public DrawWindowFunction(LuaState L, Plugin p, Handler h) {
		super(L, p, h);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int execute() throws LuaException {
		if(this.getParam(2) == null) return 0;
		
		String win = this.getParam(2).getString();
		
		mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_DRAWINDOW,win));
		
		return 0;
	}

}
