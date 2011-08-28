package com.happygoatstudios.bt.service.function;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Connection;

public class FullScreenCommand extends SpecialCommand {
	public FullScreenCommand() {
		this.commandName = "togglefullscreen";
	}
	
	public Object execute(Object o,Connection c) {
		
		
		c.service.doExecuteFullscreen();
		return null;
	}
}

