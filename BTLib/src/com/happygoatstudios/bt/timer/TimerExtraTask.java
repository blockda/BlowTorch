package com.happygoatstudios.bt.timer;

import java.util.TimerTask;

import com.happygoatstudios.bt.service.StellarService;

import android.os.Handler;
import android.os.Message;

public class TimerExtraTask extends TimerTask {

	private long starttime;
	private int ordinal;
	private Handler dispatcher;
	
	public TimerExtraTask(int timer,long init,Handler useme) {
		starttime = init;
		dispatcher = useme;
		ordinal = timer;
	}
	
	@Override
	public void run() {
		starttime = System.currentTimeMillis();
		//we need to send the dispatch message and reset the start time if this is a repeater.
		//Message timerproc = dispatcher.obtainMessage(StellarService.MESSAGE_TIMERFIRED);
		//timerproc.arg1 = ordinal;
		//dispatcher.sendMessage(timerproc);
		//TODO: make timer actually fire.
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public long getStarttime() {
		return starttime;
	}

}
