package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.happygoatstudios.bt.service.plugin.Plugin;
import com.happygoatstudios.bt.window.TextTree;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

public class Connection {
	//base "connection class"
	public final int MESSAGE_STARTUP = 1;
	public final int MESSAGE_STARTCOMPRESS = 2;
	public final int MESSAGE_PROCESSORWARNING = 3;
	public final int MESSAGE_SENDOPTIONDATA = 4;
	public final int MESSAGE_BELLINC = 5;
	public final int MESSAGE_DODIALOG = 6;
	public final int MESSAGE_PROCESS = 7;
	public final int MESSAGE_DISCONNECTED = 8;
	Handler handler = null;
	ArrayList<Plugin> plugins = null;
	DataPumper pump = null;
	Processor processor = null;
	TextTree buffer = null;
	TextTree working = null;
	
	String display;
	String host;
	int port;
	
	StellarService service = null;
	private boolean isConnected = false;
	
	public Connection(String display,String host,int port,StellarService service) {
		
		this.display = display;
		this.host = host;
		this.port = port;
		this.service = service;
		
		plugins = new ArrayList<Plugin>();
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_STARTUP:
					doStartup();
					break;
				case MESSAGE_STARTCOMPRESS:
					pump.handler.sendMessage(pump.handler.obtainMessage(DataPumper.MESSAGE_COMPRESS,msg.obj));
					break;
				case MESSAGE_SENDOPTIONDATA:
					Bundle b = msg.getData();
					byte[] obytes = b.getByteArray("THE_DATA");
					String message = b.getString("DEBUG_MESSAGE");
					if(message != null) {
						sendDataToWindow(message);
					}
					
					//try {
						if(pump != null) {
							pump.sendData(obytes);
							//output_writer.flush();
						}
					//} catch (IOException e2) {
					//	throw new RuntimeException(e2);
					//}
					break;
				case MESSAGE_PROCESSORWARNING:
					sendDataToWindow((String)msg.obj);
					break;
				case MESSAGE_BELLINC:
					//TODO: use settings;
					/*if(the_settings.isVibrateOnBell()) {
						doVibrateBell();
					}
					if(the_settings.isNotifyOnBell()) {
						doNotifyBell();
					}
					if(the_settings.isDisplayOnBell()) {
						doDisplayBell();
					}*/
					break;
				case MESSAGE_DODIALOG:
					DispatchDialog((String)msg.obj);
					break;
				case MESSAGE_PROCESS:
					try {
						dispatch((byte[])msg.obj);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_DISCONNECTED:
					killNetThreads();
					DoDisconnect(null);
					isConnected = false;
					break;
				default:
					break;
				}
			}
		};
		
		//load plugins.
		//TODO: load plugins.
		
		buffer = new TextTree();
		working = new TextTree();
		
		//TODO: set TextTree encoding options.
		
		handler.sendEmptyMessage(MESSAGE_STARTUP);
	}
	
	protected void DoDisconnect(Object object) {
		// TODO Auto-generated method stub
		
	}

	protected void killNetThreads() {
		if(pump != null) {
			pump.handler.sendEmptyMessage(DataPumper.MESSAGE_END);
			pump = null;
		}
	}

	private void dispatch(byte[] data) throws UnsupportedEncodingException {
		byte[] raw = processor.RawProcess(data);
		if(raw == null) return;
		
		working.setBleedColor(buffer.getBleedColor());
		working.addBytesImpl(data);
		for(Plugin p : plugins) {
			p.process(working, service, true, handler, display);
			working.updateMetrics();
		}
		
		byte[] proc = working.dumpToBytes(false);
		buffer.addBytesImplSimple(proc);
		
		
		sendBytesToWindow(proc);
		
		
	}
	
	protected void DispatchDialog(String str) {
		service.DispatchDialog(str);
	}

	protected void sendDataToWindow(String message) {
		try {
			//TODO: use encoding setting.
			service.doDispatchNoProcess(message.getBytes("ISO-8859-1"));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendBytesToWindow(byte[] data) {
		service.sendRawDataToWindow(data);
	}

	private void doStartup() {
		pump = new DataPumper(host,port,handler);
		pump.start();
		
		processor = new Processor(handler,"ISO-8859-1",service.getApplicationContext(),null);
		
		//show notification somehow.
		isConnected = true;
	}
	
}
