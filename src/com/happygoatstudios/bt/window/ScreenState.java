package com.happygoatstudios.bt.window;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
//import android.util.Log;

public class ScreenState extends BroadcastReceiver {

	public enum STATE {
		WAS_ON,
		WAS_OFF
	}
	
	private STATE status = STATE.WAS_ON;
	
	private Handler dispatcher;
	
	public ScreenState(Handler useme) {
		status = STATE.WAS_ON;
		dispatcher = useme;
	}
	
	@Override
	public void onReceive(Context arg0, Intent i) {
		// TODO Auto-generated method stub
		if(i.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			//Log.e("STATEWATCH","WINDOW STATE IS ON!!!!!!!");
		}
		
		if(i.getAction().equals(Intent.ACTION_USER_PRESENT)) {
			//Log.e("STATEWATCH","USER IS PRESENT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			//dispatcher.sendEmptyMessageDelayed(MainWindow.MESSAGE_LOCKUNDONE,20);
			//dispatcher.sendEmptyMessageDelayed(MainWindow.MESSAGE_LOCKUNDONE,100);
			//dispatcher.sendEmptyMessageDelayed(MainWindow.MESSAGE_LOCKUNDONE,200);
		}
	}

}
