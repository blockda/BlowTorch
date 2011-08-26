package com.happygoatstudios.bt.service.function;

import java.io.UnsupportedEncodingException;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Colorizer;
import com.happygoatstudios.bt.service.Connection;

public class DisconnectCommand extends SpecialCommand {
	
	public DisconnectCommand() {
		this.commandName = "disconnect";
	}
	public Object execute(Object o,Connection c) {
		
		
		//myhandler.sendEmptyMessage(MESSAGE_DODISCONNECT);
		String msg = "\n" + Colorizer.colorRed + "Disconnected." + Colorizer.colorWhite + "\n";
		c.sendDataToWindow(msg);
		return null;
	}
}
