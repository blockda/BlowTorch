package com.offsetnull.bt.service.function;

import java.io.UnsupportedEncodingException;

import android.os.RemoteException;

import com.offsetnull.bt.service.Colorizer;
import com.offsetnull.bt.service.Connection;

public class ReconnectCommand extends SpecialCommand {
	public ReconnectCommand() {
		this.commandName = "reconnect";
	}
	public Object execute(Object o,Connection c) {
		
		
		//myhandler.sendEmptyMessage(MESSAGE_RECONNECT);
		String msg = "\n" + Colorizer.getRedColor() + "Reconnecting . . ." + Colorizer.getWhiteColor() + "\n";
		c.sendDataToWindow(msg);
		return null;
	}
}
