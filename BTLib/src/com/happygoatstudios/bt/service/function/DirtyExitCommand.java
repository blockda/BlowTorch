package com.happygoatstudios.bt.service.function;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Connection;

public class DirtyExitCommand extends SpecialCommand {
	public DirtyExitCommand() {
		this.commandName = "closewindow";
	}
	public Object execute(Object o,Connection c) {
		
		final int N = c.callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				c.callbacks.getBroadcastItem(i).invokeDirtyExit();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		c.callbacks.finishBroadcast();
		return null;
	}
}
