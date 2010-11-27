package com.happygoatstudios.bt.service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class OutputWriter extends Thread {
	BufferedOutputStream the_stream = null;
	public Handler pump = null;
	
	static enum messages {
		SHUTDOWN,
		PROCESS		
	};
	
	public OutputWriter(OutputStream to_use) {
		the_stream = new BufferedOutputStream(to_use);
		this.setName("OutputWriter");
		
	}
	
	public Handler getHandler() {
		
		return pump;
	}
	
	public void run() {
		Looper.prepare();
		
		pump = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what)  {
				case 102: //pump output
					//get data.
					byte[] data = msg.getData().getByteArray("THEDATA");
					
					//String dbgmsg = new String(data);
					//Log.e("OUTPUT","Attempting to write:" +dbgmsg);
					
					try {
						the_stream.write(data);
						the_stream.flush();
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
					
					break;
				case 101: //shutdown
					try {
						the_stream.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
					this.getLooper().quit();
					break;
				}
			}
		};
		
		Looper.loop();
		
	}

}
