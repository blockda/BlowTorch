package com.happygoatstudios.bt.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
//import android.util.Log;
//import android.util.Log;


public class DataPumper extends Thread {

		private InputStream reader = null;
		private OutputStream writer = null;
		private Handler reportto = null;
		private boolean compressed = false;
		private Handler myhandler = null;
		//private InflaterInputStream decomp_stream = null;
		
		private Inflater decompress = null;
		//private ZStream decomp  = null;
		
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
			//decomp = new ZStream();
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
						try {
							getData();
						}  catch (IOException e) {
							throw new RuntimeException(e);
						}
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
						try {
							useCompression((byte[])msg.obj);
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
						break;
					case MESSAGE_NOCOMPRESS:
						stopCompression();
						//Log.e("BTPUMP","COMPRESSION TURNED OFF DUE TO:\n" + "BEING TOLD TO STOP BY THE SERVICE (processor encountered the IAC SB");
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
		
		private void useCompression(byte[] input) throws UnsupportedEncodingException {
			//Log.e("PUMP","COMPRESSION BEGINNING NOW!");
			compressed = true;
			corrupted = false;
			if(input == null) return;
			if(input.length > 0) {
				//try {
				//	Log.e("PUMP","STARTING COMPRESSION WITH:" +new String(input,"ISO-8859-1"));
				//} catch (UnsupportedEncodingException e) {
				//	throw new RuntimeException(e);
				//}
				byte[] data = DoDecompress(input);
				if(data == null) return;
				Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,data); //get a send data message.
				//either we woke up or the processor was ready.
				synchronized(reportto) {
					reportto.sendMessage(msg); //report to mom and dad.
				}
				//try {
				//	Log.e("PROCESSOR","STARTING COMPRESSION, INITIALLY DECOMPRESSED:" + new String(data,"ISO-8859-1"));
				//} catch (UnsupportedEncodingException e) {
				//	
				//	e.printStackTrace();
				//}
			}
		}
		
		private void stopCompression() {
			compressed = false;
		}
		
		public static String toHex(byte[] bytes) {
		    BigInteger bi = new BigInteger(1, bytes);
		    return String.format("%0" + (bytes.length << 1) + "X", bi);
		}

		private void getData() throws IOException {
				//MAIN LOOP
				int numtoread = 0;
				try {
					numtoread = reader.available();
				} catch (IOException e) {
					//throw new RuntimeException(e);
					reportto.sendEmptyMessage(StellarService.MESSAGE_DISCONNECTED);
					return;
				}
				if(numtoread < 1) {
					//no data to read
					//Log.e("PUMP","NO DATA TO READ");
					reader.mark(1);
					try {
						if(reader.read() == -1) {
							//Log.e("PUMP","END OF STREAM");
							reportto.sendEmptyMessage(StellarService.MESSAGE_DISCONNECTED);
						} else {
							reader.reset();
						}
					} catch (IOException e) { 
						reportto.sendEmptyMessage(StellarService.MESSAGE_DISCONNECTED);
						return;
					}
					
				} else {
					//data to read, do it
					//try {
					byte[] data = new byte[numtoread];
					int retval = -2;
					try {
						retval = reader.read(data,0,numtoread);
					
					} catch (IOException e) {
						//throw new RuntimeException(e);
						reportto.sendEmptyMessage(StellarService.MESSAGE_DISCONNECTED);
					}
					//Log.e("PUMP","READ (string): " + new String(data));
					//Log.e("PUMP","READ (hex): " + toHex(data));
					if(retval != numtoread) {
						//we have a problem, if we get here we overran the buffer.
					}
					
					if(retval == -1) {
						//end of stream has been reached. need to abort the who dealio.
						//Log.e("PUMP","END OF INPUT FROM SERVER");
					}
					
					if(compressed) {

						data = DoDecompress(data);
						if(data == null) return;
					} 
					

					if(reportto != null) {
						Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,data); //get a send data message.
						//either we woke up or the processor was ready.
						synchronized(reportto) {
							reportto.sendMessage(msg); //report to mom and dad.
						}
					} 

					data = null; //free data to the garbage collector.
					//} catch (SocketException e) {
						
					//}
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
		
		private boolean corrupted = false;
		private byte[] DoDecompress(byte[] data) throws UnsupportedEncodingException {
			int count = 0;
			
			byte[] decompressed_data = null;
			
			if(doCorrupt) {
				doCorrupt = false;
				if(data.length > 1) {
					decompress.setInput(data,1,data.length-1);
				}
			} else {
				decompress.setInput(data,0,data.length);
			}
			
			
			
			byte[] tmp = new byte[256];
			
			while(!decompress.needsInput()) {
				try {
					count = decompress.inflate(tmp,0,tmp.length);
					//int remain = decompress.getRemaining();
				} catch (DataFormatException e) {
					//Log.e("BTSERVICE","Encountered data format exception and must quit.");
					//myhandler.sendEmptyMessage(MESSAGE_END);
					if(reportto != null) {
						//Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,tmp); //get a send data message.
						//either we woke up or the processor was ready.
						decompress = new Inflater(false);
						//synchronized(reportto) {
							//reportto.sendMessage(msg); //report to mom and dad.
						}
					//} 
					//Log.e("BTPUMP","ATTEMPTING MCCP RENEGOTIATION DUE TO:\n" + e.getMessage());
					//compressed = false;
					reportto.sendEmptyMessage(StellarService.MESSAGE_MCCPFATALERROR);
					//return;
					compressed = false;
					corrupted = true;
					return null;
				}
				if(decompress.finished()) {
					//Log.e("PUMP","ENDING COMPRESSION:" + count + " byes uncompressed." + decompress.getRemaining() + " remaining bytes. As string: " + new String(data,"ISO-8859-1"));
					//decompress.
					//get any remaining datas.
					int pos = data.length - decompress.getRemaining();
					int length = decompress.getRemaining();
					ByteBuffer b = ByteBuffer.allocate(length);
					b.put(data, pos, length);
					b.rewind();
					//String remains = new String(b.array(),"ISO-8859-1");
					
					if(reportto != null) {
						Message msg = reportto.obtainMessage(StellarService.MESSAGE_PROCESS,b.array()); //get a send data message.
						//either we woke up or the processor was ready.
						synchronized(reportto) {
							reportto.sendMessage(msg); //report to mom and dad.
						}
					} 
					//Log.e("BTPUMP","COMPRESSION TURNED OFF DUE TO:\nCompression End Event Processed.");
					compressed = false;
					decompress = new Inflater(false);
					corrupted = false;
					return null;
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
			if(corrupted) {
				return null;
			} else {
				return decompressed_data;
			}
		}

	private boolean doCorrupt = false;
	public void corruptMe() {
		doCorrupt = true;
	}
		
}
