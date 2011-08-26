package com.happygoatstudios.bt.service.function;

import com.happygoatstudios.bt.service.Connection;

public class LoadButtonsCommand extends SpecialCommand {
public LoadButtonsCommand() {
	this.commandName = "loadset";
}

public Object execute(Object o,Connection c) {
	String str = (String)o;
	/*if(c.the_settings.getButtonSets().containsKey(str)) {
		//load that set.
		int N = callbacks.beginBroadcast();
		for(int i=0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).reloadButtons(str);
			} catch (RemoteException e) {
			}
		}
		callbacks.finishBroadcast();
	} else {
		//invalid key
		DispatchToast("Button Set: \"" + str + "\" does not exist.",false);
	}*/
	return null;
}
}
