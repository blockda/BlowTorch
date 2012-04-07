package com.offsetnull.bt.service.function;

import android.os.RemoteException;

import com.offsetnull.bt.service.Connection;

public class DirtyExitCommand extends SpecialCommand {
	public DirtyExitCommand() {
		this.commandName = "closewindow";
	}
	public Object execute(Object o,Connection c) {
		
		c.service.doDirtyExit();
		return null;
	}
}
