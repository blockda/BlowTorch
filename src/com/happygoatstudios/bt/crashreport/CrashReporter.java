package com.happygoatstudios.bt.crashreport;

import android.app.Activity;

public class CrashReporter implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler defaultHandler;
	private Activity app = null;
	
	public CrashReporter(Activity appInstance) {
		app = appInstance;
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		StackTraceElement[] trace = e.getStackTrace();
		String report = e.toString()+"\n\n";
		
		
	}

}
