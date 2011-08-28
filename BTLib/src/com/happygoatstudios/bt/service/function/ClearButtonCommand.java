package com.happygoatstudios.bt.service.function;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Connection;

public class ClearButtonCommand extends SpecialCommand {
	public ClearButtonCommand() {
		this.commandName = "clearbuttons";
	}
	
	public Object execute(Object o,Connection c) {
		c.service.doClearAllButtons();
		return null;
	}
	
}
