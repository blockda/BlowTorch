package com.happygoatstudios.bt.service.function;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Colorizer;
import com.happygoatstudios.bt.service.Connection;

public class ColorDebugCommand extends SpecialCommand {
	public ColorDebugCommand() {
		commandName = "colordebug";
	}
	public Object execute(Object o,Connection c) {
		//Log.e("WINDOW","EXECUTING COLOR DEBUG COMMAND WITH STRING ARGUMENT: " + (String)o);
		String arg = (String)o;
		Integer iarg = 0;
		boolean failed = false;
		
		try {
			iarg = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			//invalid number
			failed = true;
			//errormessage += "\"colordebug\" special command is unable to use the argument: " + arg + "\n";
			//errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
		}
		if(iarg < 0 || iarg > 3) {
			//invalid number
			failed = true;
		}
		
		if(failed) {
			String errormessage = "\n" + Colorizer.colorRed + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
			if(arg.equals("")) {
				errormessage += "\"colordebug\" special command requires an argument.\n";
			} else {
				errormessage += "\"colordebug\" special command is unable to use the argument: " + arg + "\n";
			}
			errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
			errormessage += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.colorWhite+"\n";
			
			//try {
				c.sendDataToWindow(errormessage);
			//} catch (RemoteException e) {
			//	throw new RuntimeException(e);
			//} catch (UnsupportedEncodingException e) {
			//	throw new RuntimeException(e);
			//}
			
			return null;
		}
		//if we are here we are good to go.
		final int N = c.callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				c.callbacks.getBroadcastItem(i).executeColorDebug(iarg);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//notify listeners that data can be read
		}
		c.callbacks.finishBroadcast();
		
		//so with the color debug mode set, we should probably dispatch a message to them.
		String success = "\n" + Colorizer.colorRed + "Color Debug Mode " + iarg + " activated. ";
		if(iarg == 0) {
			success = "\n" + Colorizer.colorRed + "Normal color processing resumed." ;
		} else if(iarg == 1) {
			success += "(color enabled, color codes shown)";
		} else if(iarg == 2) {
			success += "(color disabled, color codes shown)";
		} else if(iarg == 3) {
			success += "(color disabled, color codes not shown)";
		} else {
			success += "(this argument shouldn't happen, contact developer)";
		}
		
		success += Colorizer.colorWhite +"\n";
		
		//try {
			c.sendDataToWindow(success);
		//} catch (RemoteException e) {
		//	throw new RuntimeException(e);
		//} catch (UnsupportedEncodingException e) {
		//	throw new RuntimeException(e);
		//}
		
		return null;
	}
	
}