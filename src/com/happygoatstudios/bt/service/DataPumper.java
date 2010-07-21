package com.happygoatstudios.bt.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class DataPumper extends Thread {

		private InputStream reader = null;
		private OutputStream writer = null;
		private Handler reportto = null;
		private boolean compressed = false;
		private Handler myhandler = null;
		
		private Inflater decompress = null;
		
		final static public int MESSAGE_RETRIEVE = 100;
		final static public int MESSAGE_SEND = 101;
		final static public int MESSAGE_END = 102;
		final static public int MESSAGE_INITXFER = 103;
		final static public int MESSAGE_ENDXFER = 104;
		final static public int MESSAGE_COMPRESS = 105;
		final static public int MESSAGE_NOCOMPRESS = 106;
	
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
						Message tmp = myhandler.obtainMessage(MESSAGE_RETRIEVE);
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
						myhandler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, 500);
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
		}
		
		public void stopCompression() {
			compressed = false;
		}
		
		private void getData() {
				//MAIN LOOP
				
				//get all the data in the stream.
				int numtoread = 0;
				try {
					numtoread = reader.available();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//Log.e("PUMP","PUMP QUIT UNEXPECTEDLY!");
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(retval != numtoread) {
						//we have a problem, if we get here we overran the buffer.
					}
					
					if(retval == -1) {
						//end of stream has been reached. need to abort the who dealio.
						//Log.e("PUMPER","END OF STREAM REACHED");
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
									Log.e("BTSERVICE","Encountered data format exception and must quit.");
									myhandler.sendEmptyMessage(MESSAGE_END);
									return;
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
								
								/*ByteBuffer output = ByteBuffer.allocate(count); //capture output
								output.put(tmp,0,count);	
								output.rewind();
								data = new byte[count];
								output.get(data,0,count);*/
								//decompress.reset();
							} //end while
							data = decompressed_data;
					} //end compressed if
							/*String testtring = null;
							try {
								testtring= new String(data,"UTF-8");
								Log.e("WHA","DECOMP:"+testtring);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
							//decompress.reset(); //????
						
					//}
					//report to our superior
					if(reportto != null) {
						Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,data); //get a send data message.
						//Bundle bundle = new Bundle();
						//bundle.putByteArray("THEBYTES", data);
						//msg.setData(bundle);
						
						//only send if they are ready
						//synchronized(reportto) {
						//	while(reportto.hasMessages(StellarService.MESSAGE_PROCESS)) {
						//		try {
						//			reportto.wait();
						//		} catch (InterruptedException e) {
						///			// TODO Auto-generated catch block
						//			e.printStackTrace();
						//		}
						//	}
						//}
						//either we woke up or the processor was ready.
						synchronized(reportto) {
							reportto.sendMessage(msg); //report to mom and dad.
						}
						//Log.e("PUMP","SENDING DATA FROM PUMP TO SERVICE");
						//sleep until they wake us
						//waitme(); //pause till processor is done.

						//Log.i("DR","READER SHUTTING DOWN!");
						//waitme(); //wait for recovery from data processing

						//Log.i("DR","READER RESUMING!");
						
					} 

					data = null; //free data to the garbage collector.
				}
				
				/*synchronized(this) {
					try {
						this.wait(50); //throttle the connection to 50 miliseconds == 20 times per second.
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
		}
		
		Pattern newline = Pattern.compile("\\w*");
		
		private void sendData(byte[] data) {
			//byte[] data = msg.getData().getByteArray("THEDATA");
			String debugmsg = null;
			//try {
			debugmsg = new String(data);
			//} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			
			//Matcher crlf = newline.matcher(debugmsg);
			
			//String matches = "(no newline)";
			//if(crlf.matches()) {
			//	matches = "(newline)";
			//}
				
			int size = data.length;
			int sec_byte = (int)data[data.length -2];
			int last_byte = (int)data[data.length -1];
			
			//Log.e("PUMP","PUMP SENDING: "+ debugmsg + " | stlb: " + sec_byte + " lasb: " +last_byte + " size: " + size);
			try {
				writer.write(data);
				writer.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		}
		
}
