package com.offsetnull.bt.service.plugin.function;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import android.os.Handler;

import com.offsetnull.bt.service.plugin.settings.PluginSettings;
import com.offsetnull.bt.service.plugin.Plugin;

public class TriggerEnabledFunction extends PluginFunction {

	
	public TriggerEnabledFunction(LuaState L,Plugin p,Handler h) {
		super(L,p,h);
		//this.L = L;
	}
	
	@Override
	public int execute() throws LuaException {
		String trigger = this.getParam(2).getString();
		
		if(mPlugin.getSettings().getTriggers().containsKey(trigger)) {
			//execute function
			if(this.getParam(3) == null) {
				L.pushBoolean(mPlugin.getSettings().getTriggers().get(trigger).isEnabled());
				return 1;
			}
			Boolean state = this.getParam(3).getBoolean();
			mPlugin.getSettings().getTriggers().get(trigger).setEnabled(state);
			
			mPlugin.markTriggersDirty();
			//this.mPlugin.buildTriggerSystem();
			L.pushBoolean(true);
			return 1;
			
		} else {
			//return error
			L.pushString("Function: TriggerEnabled(string,boolean) Error: \""+trigger+"\" does not exist");
			return 1;
		}
		
		//return 0;
	}

	
	
}
