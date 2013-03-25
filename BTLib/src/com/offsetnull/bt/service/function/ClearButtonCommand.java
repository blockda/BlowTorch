package com.offsetnull.bt.service.function;

import android.os.RemoteException;

import com.offsetnull.bt.service.Connection;

public class ClearButtonCommand extends SpecialCommand {
	public ClearButtonCommand() {
		this.commandName = "clearbuttons";
	}
	
	public Object execute(Object o,Connection c) {
		c.getService().doClearAllButtons();
		return null;
	}
	
}
