package com.happygoatstudios.bt.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;



public class DataPumper extends Thread {

		private InputStream reader = null;
		//private OutputStream writer = null;
		private Handler reportto = null;
		private boolean compressed = false;
		public Handler handler = null;
		
		
		//private InflaterInputStream decomp_stream = null;
		
		private Inflater decompress = null;
		//private ZStream decomp  = null;
		
		final static public int MESSAGE_RETRIEVE = 100;
		//final static public int MESSAGE_SEND = 101;
		final static public int MESSAGE_END = 102;
		final static public int MESSAGE_INITXFER = 103;
		final static public int MESSAGE_ENDXFER = 104;
		final static public int MESSAGE_COMPRESS = 105;
		final static public int MESSAGE_NOCOMPRESS = 106;
		public static final int MESSAGE_THROTTLE = 108;
		public static final int MESSAGE_NOTHROTTLE = 109;
		//protected static final int MESSAGE_WRITETOSERVER = 110;
	
		private boolean throttle = false;
		
		public void sendData(byte[] data) {
			Message msg = writerThread.outhandler.obtainMessage(OutputWriterThread.MESSAGE_SEND,data);
			writerThread.outhandler.sendMessage(msg);
		}
		
		class OutputWriterThread extends Thread {
			protected static final int MESSAGE_END = 101;
			protected static final int MESSAGE_SEND = 102;
			BufferedOutputStream writer = null;
			public Handler outhandler = null;
			public OutputWriterThread(BufferedOutputStream stream) {
				writer = stream;
			}
			public void run() {
				Looper.prepare();
				outhandler = new Handler() {
					public void handleMessage(Message msg) {
						switch(msg.what) {
						case MESSAGE_SEND:
							//this will always be the same thing.
							byte[] data = (byte[])msg.obj;
							
							try {
								writer.write(data);
								writer.flush();
							} catch (IOException e1) {
								DispatchDialog(e1.getMessage());
								connected = false;
							}
						break;
						case MESSAGE_END:
							this.getLooper().quit();
						}
					}
				};
				Looper.loop();
			}
		}
		private OutputWriterThread writerThread = null;
		private String host = "";
		private int port = 23;
		
		public DataPumper(String host,int port,Handler useme) {
			this.host = host;
			this.port = port;
			
			reportto = useme;
		}
		
		private Socket the_socket = null;
		public void init()  {
			//Debug.waitForDebugger();
			this.setName("DataPumper");
			//TODO: MAKE CONNECTION STARTUP CODE HERE.
			InetAddress addr = null;
			
			
			sendWarning(new String(Colorizer.colorCyanBright+"Attempting connection to: "+ Colorizer.colorYeollowBright + host + ":"+port+"\n"+Colorizer.colorCyanBright+"Timeout set to 14 seconds."+Colorizer.colorWhite+"\n"));
			
			try {
				addr = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				DispatchDialog("Unknown Host: " + e.getMessage());
				return;
				//e.printStackTrace();
			}
			
			String ip = addr.getHostAddress();
			
			if(ip.equals(host)) {
				//it was an ip, don't display it.
			} else {
				//handler.handle
				sendWarning(Colorizer.colorCyanBright+"Looked up: "+Colorizer.colorYeollowBright + ip +Colorizer.colorCyanBright+ " for "+Colorizer.colorYeollowBright+host+Colorizer.colorWhite+"\n");
			}
			
			the_socket = new Socket();
			//SocketAddress adr = new InetSocketAddress(addr,port);
			
			try {
				
				the_socket = new Socket();
				SocketAddress adr = new InetSocketAddress(addr,port);
				the_socket.connect(adr,14000);
				sendWarning(Colorizer.colorCyanBright+"Connected to: "+Colorizer.colorYeollowBright+host+Colorizer.colorCyanBright+"!"+Colorizer.colorWhite+"\n");
				
				the_socket.setSendBufferSize(1024);
				writerThread = new OutputWriterThread(new BufferedOutputStream(the_socket.getOutputStream()));
				writerThread.start();
				
				connected = true;
				reader = new BufferedInputStream(the_socket.getInputStream());
				decompress = new Inflater(false);
			} catch (SocketException e) {
				DispatchDialog("Socket Exception: " + e.getMessage());
				//Log.e("SERVICE","NET FAILURE:" + e.getMessage());
			} catch (SocketTimeoutException e) {
				DispatchDialog("Operation timed out.");
			} catch (ProtocolException e) {
				DispatchDialog("Protocol Exception: " + e.getMessage());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		private void DispatchDialog(String str) {
			reportto.sendMessage(reportto.obtainMessage(Connection.MESSAGE_DODIALOG,str));
		}
		/*public DataPumper(InputStream istream,OutputStream ostream,Handler useme) {
			reader = new BufferedInputStream(istream);
			//writer = new BufferedOutputStream(ostream);
			//writer = ostream;
			reportto = useme;
			decompress = new Inflater(false);
			//decomp = new ZStream();
			writer=ostream;
			this.setName("DataPumper");
		}*/
		
		public void sendWarning(String str) {
			reportto.sendMessage(reportto.obtainMessage(Connection.MESSAGE_PROCESSORWARNING,(str)));
			
		}
	
		public void run() {
			Looper.prepare();
			init();
			handler = new Handler() {
				public void handleMessage(Message msg) {
					//boolean doRestart = true;
					switch(msg.what) {
					
					/*case MESSAGE_WRITETOSERVER:
						//Log.e("DAN0","DEPERATLY TRYING TO WRITE TO THE SERVER");
						byte[] data = (byte[])msg.obj;
						
						try {
							writer.write(data);
							writer.flush();
						} catch (IOException e1) {
							DispatchDialog(e1.getMessage());
							connected = false;
						}
						//doRestart = false;
						break;*/
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
					//case MESSAGE_SEND:
						//sendData(msg.getData().getByteArray("THEDATA"));
						//break;
					case MESSAGE_END:
						//Log.e("PUMP","PUMP QUITTING!");
						
						try {
							writerThread.outhandler.sendEmptyMessage(OutputWriterThread.MESSAGE_END);
							//writer.close();
							reader.close();
							the_socket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						this.getLooper().quit();
						break;
					case MESSAGE_INITXFER:
						//Message tmp = myhandler.obtainMessage(MESSAGE_RETRIEVE);
						//myhandler.sendMessageDelayed(tmp, 50);
						break;
					case MESSAGE_ENDXFER:
						handler.removeMessages(MESSAGE_RETRIEVE);
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
					
					//if(!restart)
					//keep the pump flowing.
					if(!handler.hasMessages(MESSAGE_RETRIEVE)) {
						//only send if there are no messages already in queue.
						if(!throttle) {
							handler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, 100);
						} else {
							handler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, 1500);
						}
					}
				}
			};
			handler.sendEmptyMessage(MESSAGE_RETRIEVE);
			Looper.loop();
		}
		
		/*public Handler getHandler() {
			return myhandler;
		}*/
		
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
				Message msg = reportto.obtainMessage(Connection.MESSAGE_PROCESS,data); //get a send data message.
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
					reportto.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
					connected = false;
					return;
					
				}
				if(numtoread < 1) {
					//no data to read
					//Log.e("PUMP","NO DATA TO READ");
					reader.mark(1);
					try {
						if(reader.read() == -1) {
							//Log.e("PUMP","END OF STREAM");
							reportto.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
							connected = false;
						} else {
							reader.reset();
						}
					} catch (IOException e) { 
						e.printStackTrace();
						reportto.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
						connected = false;
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
						reportto.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
						connected = false;
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
						Message msg = reportto.obtainMessage(Connection.MESSAGE_PROCESS,data); //get a send data message.
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
		
		//Pattern newline = Pattern.compile("\\w*");
		
		/*private void sendData(byte[] data) {
			try {
				writer.write(data);
				writer.flush();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			
			
		}*/
		
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
					reportto.sendEmptyMessage(Connection.MESSAGE_MCCPFATALERROR);
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
						Message msg = reportto.obtainMessage(Connection.MESSAGE_PROCESS,b.array()); //get a send data message.
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
	private boolean connected = false;;
	public void corruptMe() {
		doCorrupt = true;
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return connected ;
	}
		
}
