package com.happygoatstudios.bt.service.function;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Connection;

public class ClearButtonCommand extends SpecialCommand {
	public ClearButtonCommand() {
		this.commandName = "clearbuttons";
	}
	
	public Object execute(Object o,Connection c) {
		int N = c.callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				c.callbacks.getBroadcastItem(i).clearAllButtons();
			} catch (RemoteException e) {
			}
		}
		c.callbacks.finishBroadcast();
		return null;
	}
	
}
