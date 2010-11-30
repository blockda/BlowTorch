package com.happygoatstudios.bt.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
//import android.util.Log;
//import android.util.Log;
import android.util.Log;

public class DataPumper extends Thread {

		private InputStream reader = null;
		private OutputStream writer = null;
		private Handler reportto = null;
		private boolean compressed = false;
		private Handler myhandler = null;
		private InflaterInputStream decomp_stream = null;
		
		private Inflater decompress = null;
		
		final static public int MESSAGE_RETRIEVE = 100;
		final static public int MESSAGE_SEND = 101;
		final static public int MESSAGE_END = 102;
		final static public int MESSAGE_INITXFER = 103;
		final static public int MESSAGE_ENDXFER = 104;
		final static public int MESSAGE_COMPRESS = 105;
		final static public int MESSAGE_NOCOMPRESS = 106;
		public static final int MESSAGE_THROTTLE = 108;
		public static final int MESSAGE_NOTHROTTLE = 109;
	
		private boolean throttle = false;
		
		public DataPumper(InputStream istream,OutputStream ostream,Handler useme) {
			reader = new BufferedInputStream(istream);
			//writer = new BufferedOutputStream(ostream);
			writer = ostream;
			reportto = useme;
			decompress = new Inflater(false);
			this.setName("DataPumper");
		}
	
		public void run() {
			Looper.prepare();
			
			myhandler = new Handler() {
				public void handleMessage(Message msg) {
					switch(msg.what) {
					case MESSAGE_THROTTLE:
						//Log.e("PUMPER","DATA PUMP THROTTLING");
						throttle = true;
						break;
					case MESSAGE_NOTHROTTLE:
						//Log.e("PUMPER","DATA PUMP RESUMING NORMAL OPERATION");
						this.removeMessages(MESSAGE_RETRIEVE);
						throttle = false;
						this.sendEmptyMessage(MESSAGE_RETRIEVE);
						
						break;
					case MESSAGE_RETRIEVE:
						//Log.e("PUMP","PUMP CHECKING FOR NEW DATA!");
						getData();
						//keep the pump flowing.
						break;
					case MESSAGE_SEND:
						sendData(msg.getData().getByteArray("THEDATA"));
						break;
					case MESSAGE_END:
						//Log.e("PUMP","PUMP QUITTING!");
						this.getLooper().quit();
						break;
					case MESSAGE_INITXFER:
						//Message tmp = myhandler.obtainMessage(MESSAGE_RETRIEVE);
						//myhandler.sendMessageDelayed(tmp, 50);
						break;
					case MESSAGE_ENDXFER:
						myhandler.removeMessages(MESSAGE_RETRIEVE);
						break;
					case MESSAGE_COMPRESS:
						useCompression();
						break;
					case MESSAGE_NOCOMPRESS:
						stopCompression();
						break;
					}
					
					//keep the pump flowing.
					if(!myhandler.hasMessages(MESSAGE_RETRIEVE)) {
						//only send if there are no messages already in queue.
						if(!throttle) {
							myhandler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, 100);
						} else {
							myhandler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, 1500);
						}
					}
				}
			};
			
			Looper.loop();
		}
		
		public Handler getHandler() {
			return myhandler;
		}
		
		public void useCompression() {
			//Log.e("PUMP","COMPRESSION BEGINNING NOW!");
			compressed = true;
			//compress
		}
		
		public void stopCompression() {
			compressed = false;
		}
		
		public static String toHex(byte[] bytes) {
		    BigInteger bi = new BigInteger(1, bytes);
		    return String.format("%0" + (bytes.length << 1) + "X", bi);
		}

		private void getData() {
				//MAIN LOOP
				int numtoread = 0;
				try {
					numtoread = reader.available();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if(numtoread < 1) {
					//no data to read
				} else {
					//data to read, do it
					byte[] data = new byte[numtoread];
					int retval = -1;
					try {
						retval = reader.read(data,0,numtoread);
					
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					//Log.e("PUMP","READ (string): " + new String(data));
					//Log.e("PUMP","READ (hex): " + toHex(data));
					if(retval != numtoread) {
						//we have a problem, if we get here we overran the buffer.
					}
					
					if(retval == -1) {
						//end of stream has been reached. need to abort the who dealio.
					}
					
					if(compressed) {
						//uncompress data first
						//Inflater decompress = new Inflater();
						//decompress.setInput(data,0,data.length);
						//byte[] newdata = 
						//according to http://java.sun.com/j2se/1.4.2/docs/api/java/util/zip/Inflater.html#Inflater%28boolean%29
						//i need to provide a dummy byte to start with because i don't use headers
						byte[] decompressed_data = null;
						
						decompress.setInput(data,0,data.length);
						
						byte[] tmp = new byte[256];
						//while(!decompress.finished()) {
							int count = 0;
							
							while(!decompress.needsInput()) {
								try {
									count = decompress.inflate(tmp,0,tmp.length);
									//int remain = decompress.getRemaining();
								} catch (DataFormatException e) {
									//Log.e("BTSERVICE","Encountered data format exception and must quit.");
									//myhandler.sendEmptyMessage(MESSAGE_END);
									//if(reportto != null) {
										//Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,tmp); //get a send data message.
										//either we woke up or the processor was ready.
										decompress = new Inflater(false);
										//synchronized(reportto) {
											//reportto.sendMessage(msg); //report to mom and dad.
										//}
									//} 
									compressed = false;
									return;
								}
								
								if(decompress.finished()) {
									Log.e("PUMP","COMPRESSION ENDING");
								}
								
								if(decompressed_data == null && count > 0) {
									ByteBuffer dc_start = ByteBuffer.allocate(count);
									dc_start.put(tmp,0,count);
									decompressed_data = dc_start.array();
								} else { //already have data, append tmp to us
									if(count > 0) { //only perform this step if the inflation yielded results.
									ByteBuffer tmpbuf = ByteBuffer.allocate(decompressed_data.length + count);
									tmpbuf.put(decompressed_data,0,decompressed_data.length);
									tmpbuf.put(tmp,0,count);
									tmpbuf.rewind();
									decompressed_data = tmpbuf.array();
									}
								}
							} //end while
							data = decompressed_data;
					} 
					//report to our superior
					if(reportto != null) {
						Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,data); //get a send data message.
						//either we woke up or the processor was ready.
						synchronized(reportto) {
							reportto.sendMessage(msg); //report to mom and dad.
						}
					} 

					data = null; //free data to the garbage collector.
				}
		}
		
		Pattern newline = Pattern.compile("\\w*");
		
		private void sendData(byte[] data) {
			try {
				writer.write(data);
				writer.flush();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			
			
		}
		
}
