/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/** Data pumper thread implementation. This object manages itself as a thread, as well as a child
 * thread process to write output data to. */
public class DataPumper extends Thread {
	/** Constant indicating the socket polling loop. */
	public static final int MESSAGE_RETRIEVE = 100;
	/** Constant indicating orderly shutdown via user. */
	public static final int MESSAGE_END = 102;
	/** Constant indicating that data transfer should begin. */
	public static final int MESSAGE_INITXFER = 103;
	/** Constant indicating the end of the data transfer. */
	public static final int MESSAGE_ENDXFER = 104;
	/** Contant indicating that compression should start. */
	public static final int MESSAGE_COMPRESS = 105;
	/** Constant indicating that compression should end. */
	public static final int MESSAGE_NOCOMPRESS = 106;
	/** Constant indicating that throttling should begin. */
	public static final int MESSAGE_THROTTLE = 108;
	/** Constant indicating that throttling should end. */
	public static final int MESSAGE_NOTHROTTLE = 109;
	/** Timeout value in millis. */
	private static final int SOCKET_TIMEOUT = 14000;
	/** Socket buffer size. */
	private static final int SOCKET_BUFFER_SIZE = 1024;
	/** No throttling delay. */
	private static final int NO_THROTTLE_DELAY = 100;
	/** Throttling delay. */
	private static final int THROTTLE_DELAY = 1500;
	/** Working buffer size for the decompression routine. */
	private static final int DECOMPRESSION_BUFFER_SIZE = 256;
	/** The handler for this thread. */
	private Handler mHandler = null;
	/** The input stream from the socket. */
	private InputStream mReader = null;
	/** The handler to communicate progress and data with. */
	private Handler mReportTo = null;
	/** The compression state. */
	private boolean mCompressed = false;
	/** Zlib inflater object. */
	private Inflater mDecompressor = null;
	/** Indicates the throttling state. */
	private boolean mThrottle = false;
	/** Holder for the output writer thread. */
	private OutputWriterThread mWriterThread = null;
	/** Holder for the host name for the connection. */
	private String mHost = "";
	/** Holder for the port number for the connection. */
	private int mPort;
	/** Holder for the actual socket used to communicate with. */
	private Socket mSocket = null;
	/** Tracker for MCCP Corruption. */
	private boolean mCorrupted = false;
	/** Tracker for the intention of corrupting the mccp stream. */
	private boolean mDoCorrupt = false;
	/** Tracker for if we are connected or not. */
	private boolean mConnected = false;
	/** Tracker for the intention of closing the socket. */
	private boolean mClosing = false;
	
	/** Generic constructor.
	 * 
	 * @param host Host name for the connection.
	 * @param port Port number to use.
	 * @param useme Handler to report to when interesting things happen.
	 */
	public DataPumper(final String host, final int port, final Handler useme) {
		this.mHost = host;
		this.mPort = port;
		mReportTo = useme;
	}
	
	/** Sends data to the socket asynchronously.
	 * 
	 * @param data the bytes to send to the server.
	 */
	public final void sendData(final byte[] data) {
		Message msg = mWriterThread.mOutputHandler.obtainMessage(OutputWriterThread.MESSAGE_SEND, data);
		mWriterThread.mOutputHandler.sendMessage(msg);
	}
	
	/** Utility class for housing the output writer thread. */
	class OutputWriterThread extends Thread {
		/** Constant indicating orderly shutdown of this thread. */
		protected static final int MESSAGE_END = 101;
		/** Constant indicating that there is data to send. */
		protected static final int MESSAGE_SEND = 102;
		/** Abstraction from the raw stream. */
		private BufferedOutputStream mWriter = null;
		/** Handler for this thread. */
		private Handler mOutputHandler = null;
		
		/** Generic constructor. 
		 * 
		 * @param stream The stream from the socket to use when writing data.
		 */
		public OutputWriterThread(final BufferedOutputStream stream) {
			mWriter = stream;
		}
		@Override
		public void run() {
			Looper.prepare();
			mOutputHandler = new Handler(new WriteHandler());
			Looper.loop();
		}
	}
	
	/** The write queue is contained in this handler callback. */
	private class WriteHandler implements Handler.Callback {

		@Override
		public boolean handleMessage(final Message msg) {
			switch (msg.what) {
			case OutputWriterThread.MESSAGE_SEND:
				//this will always be the same thing.
				byte[] data = (byte[]) msg.obj;
				
				try {
					mWriterThread.mWriter.write(data);
					mWriterThread.mWriter.flush();
				} catch (IOException e1) {
					dispatchDialog(e1.getMessage());
					mConnected = false;
				}
				break;
			case OutputWriterThread.MESSAGE_END:
				Log.e("TEST", "OUTPUT WRITER THREAD SHUTTING DOWN");
				
				mWriterThread.mOutputHandler.getLooper().quit();
				break;
			default:
				break;
			}
			return true;
		}
		
	}
	
	/** Startup and initialization routine. */
	public final void init()  {
		this.setName("DataPumper");
		InetAddress addr = null;
		mClosing = false;
		
		sendWarning(new String(Colorizer.getBrightCyanColor() + "Attempting connection to: " + Colorizer.getBrightYellowColor() + mHost + ":" + mPort + "\n"
		+ Colorizer.getBrightCyanColor() + "Timeout set to 14 seconds." + Colorizer.getWhiteColor() + "\n"));
		
		try {
			addr = InetAddress.getByName(mHost);
		} catch (UnknownHostException e) {
			dispatchDialog("Unknown Host: " + e.getMessage());
			return;
		}
		
		String ip = addr.getHostAddress();
		
		if (!ip.equals(mHost)) {
			sendWarning(Colorizer.getBrightCyanColor() + "Looked up: " + Colorizer.getBrightYellowColor() + ip + Colorizer.getBrightCyanColor() + " for "
			+ Colorizer.getBrightYellowColor() + mHost + Colorizer.getWhiteColor() + "\n");
		}
		
		mSocket = new Socket();
		try {
			
			mSocket = new Socket();
			SocketAddress adr = new InetSocketAddress(addr, mPort);
			mSocket.setKeepAlive(true);
			mSocket.setSoTimeout(0);
			mSocket.connect(adr, SOCKET_TIMEOUT);
			sendWarning(Colorizer.getBrightCyanColor() + "Connected to: " + Colorizer.getBrightYellowColor() + mHost + Colorizer.getBrightCyanColor() + "!" + Colorizer.getWhiteColor() + "\n");
			
			mSocket.setSendBufferSize(SOCKET_BUFFER_SIZE);
			mWriterThread = new OutputWriterThread(new BufferedOutputStream(mSocket.getOutputStream()));
			mWriterThread.start();
			
			mConnected = true;
			mReader = new BufferedInputStream(mSocket.getInputStream());
			mDecompressor = new Inflater(false);
			
			mReportTo.sendEmptyMessage(Connection.MESSAGE_CONNECTED);
		} catch (SocketException e) {
			dispatchDialog("Socket Exception: " + e.getMessage());
		} catch (SocketTimeoutException e) {
			dispatchDialog("Operation timed out.");
		} catch (ProtocolException e) {
			dispatchDialog("Protocol Exception: " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Quick little helper method to send off the error dialog.
	 * 
	 * @param str The message to put in the dialog.
	 */
	private void dispatchDialog(final String str) {
		mReportTo.sendMessage(mReportTo.obtainMessage(Connection.MESSAGE_DODIALOG, str));
	}
	
	/** Quick little helper method to send off a processor warning.
	 * 
	 * @param str The processor warning to send.
	 */
	public final void sendWarning(final String str) {
		mReportTo.sendMessage(mReportTo.obtainMessage(Connection.MESSAGE_PROCESSORWARNING, str));
		
	}
	
	@Override
	public final void run() {
		Looper.prepare();
		init();
		mHandler = new Handler(new ReadHandler());
		if (mReader != null) {
			mHandler.sendEmptyMessage(MESSAGE_RETRIEVE);
		}
		Looper.loop();
	}
	
	/** The reader thread is managed by this handler callback. */
	private class ReadHandler implements Handler.Callback {
		@Override
		public boolean handleMessage(final Message msg) {
			//boolean doRestart = true;
			switch (msg.what) {
			case MESSAGE_THROTTLE:
				mThrottle = true;
				break;
			case MESSAGE_NOTHROTTLE:
				mHandler.removeMessages(MESSAGE_RETRIEVE);
				mThrottle = false;
				mHandler.sendEmptyMessage(MESSAGE_RETRIEVE);
				
				break;
			case MESSAGE_RETRIEVE:
				try {
					getData();
				}  catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			case MESSAGE_END:
				mHandler.removeMessages(MESSAGE_RETRIEVE);
				Log.e("TEST", "DATA PUMPER STARTING END SEQUENCE");
				try {
					if (mWriterThread != null) {
						mWriterThread.mOutputHandler.sendEmptyMessage(OutputWriterThread.MESSAGE_END);
						try {
							Log.e("TEST", "KILLING WRITER THREAD");
							mWriterThread.join();
							Log.e("TEST", "WRITER THREAD DEAD");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (mReader != null) {
						mReader.close();
					}
					if (mSocket != null) {
						mSocket.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Log.e("TEST", "Net reader thread stopping self.");
			
				mHandler.getLooper().quit();
				break;
			case MESSAGE_INITXFER:
				break;
			case MESSAGE_ENDXFER:
				mHandler.removeMessages(MESSAGE_RETRIEVE);
				break;
			case MESSAGE_COMPRESS:
				try {
					useCompression((byte[]) msg.obj);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				break;
			case MESSAGE_NOCOMPRESS:
				stopCompression();
				break;
			default:
				break;
			}
			if (!mHandler.hasMessages(MESSAGE_RETRIEVE) && mConnected) {
				//only send if there are no messages already in queue.
				if (!mThrottle) {
					mHandler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, NO_THROTTLE_DELAY);
				} else {
					mHandler.sendEmptyMessageDelayed(MESSAGE_RETRIEVE, THROTTLE_DELAY);
				}
			}
			return true;
		}
	}
		
	/** Utility method to interrpt a blocked socket. */
	public final void interruptSocket() {
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Called when compression should be initated.
	 * 
	 * @param input The data to start the compression with. This is whatever followed IAC SB MCCP2 IAC SE
	 * @throws UnsupportedEncodingException Thrown when a String<==>byte[] conversion is given a bad encoding option.
	 */
	private void useCompression(final byte[] input) throws UnsupportedEncodingException {
		//Log.e("PUMP","COMPRESSION BEGINNING NOW!");
		mCompressed = true;
		mCorrupted = false;
		if (input == null) {
			return;
		}
		if (input.length > 0) {
			byte[] data = doDecompress(input);
			if (data == null) { return; }
			Message msg = mReportTo.obtainMessage(Connection.MESSAGE_PROCESS, data); //get a send data message.
			synchronized (mReportTo) {
				mReportTo.sendMessage(msg); //report to mom and dad.
			}
		}
	}
	
	/** Utility method to set the compression use flag. */
	private void stopCompression() {
		mCompressed = false;
	}
		
	/** Utility method to convert byte[] to a hex string.
	 * 
	 * @param bytes The byte array to convert.
	 * @return The hex string representation.
	 */
	public static String toHex(final byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	/** The main data fetching routine. This takes care of reading data, managing disconnection and decompression.
	 * 
	 * @throws IOException Thrown when there is a problem with the socket.
	 */
	private void getData() throws IOException {
		int numtoread = 0;
		try {
			numtoread = mReader.available();
		} catch (IOException e) {
			if (!mClosing) {
				mReportTo.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
			}
			mConnected = false;
			return;
			
		}
		if (numtoread < 1) {
			mReader.mark(1);
			try {
				if (mReader.read() == -1) {
					if (!mClosing) {
						sendWarning("\n" + Colorizer.getRedColor() + "Connection terminated by peer." + Colorizer.getWhiteColor() + "\n");
						mReportTo.sendEmptyMessage(Connection.MESSAGE_TERMINATED_BY_PEER);
					}
					mConnected = false;
				} else {
					mReader.reset();
				}
			} catch (IOException e) { 
				e.printStackTrace();
				if (!mClosing) {
					mReportTo.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
				}
				mConnected = false;
				return;
			}
			
		} else {
			byte[] data = new byte[numtoread];
			try {
				mReader.read(data, 0, numtoread);
			
			} catch (IOException e) {
				if (!mClosing) {
					mReportTo.sendEmptyMessage(Connection.MESSAGE_DISCONNECTED);
				}
				mConnected = false;
			}
			
			if (mCompressed) {
				data = doDecompress(data);
				if (data == null) { return; }
			} 
			
			if (mReportTo != null) {
				Message msg = mReportTo.obtainMessage(Connection.MESSAGE_PROCESS, data); //get a send data message.
				synchronized (mReportTo) {
					mReportTo.sendMessage(msg); //report to mom and dad.
				}
			} 

			data = null; //free data to the garbage collector.
		}
	}
		
	/** The workhorse decompression rotine.
	 * 
	 * @param data Bytes in.
	 * @return Bytes out.
	 * @throws UnsupportedEncodingException Thown when there is a a problem with string encoding.
	 */
	private byte[] doDecompress(final byte[] data) throws UnsupportedEncodingException {
		int count = 0;
		
		byte[] decompressedData = null;
		
		if (mDoCorrupt) {
			mDoCorrupt = false;
			if (data.length > 1) {
				mDecompressor.setInput(data, 1, data.length - 1);
			}
		} else {
			mDecompressor.setInput(data, 0, data.length);
		}
		
		byte[] tmp = new byte[DECOMPRESSION_BUFFER_SIZE];
		
		while (!mDecompressor.needsInput()) {
			try {
				count = mDecompressor.inflate(tmp, 0, tmp.length);
			} catch (DataFormatException e) {
				if (mReportTo != null) {
					mDecompressor = new Inflater(false);
				}
				mReportTo.sendEmptyMessage(Connection.MESSAGE_MCCPFATALERROR);
				mCompressed = false;
				mCorrupted = true;
				return null;
			}
			if (mDecompressor.finished()) {
				int pos = data.length - mDecompressor.getRemaining();
				int length = mDecompressor.getRemaining();
				ByteBuffer b = ByteBuffer.allocate(length);
				b.put(data, pos, length);
				b.rewind();
				if (mReportTo != null) {
					Message msg = mReportTo.obtainMessage(Connection.MESSAGE_PROCESS, b.array()); //get a send data message.
					synchronized (mReportTo) {
						mReportTo.sendMessage(msg); //report to mom and dad.
					}
				} 
				mCompressed = false;
				mDecompressor = new Inflater(false);
				mCorrupted = false;
				return null;
			}
			if (decompressedData == null && count > 0) {
				ByteBuffer dcStart = ByteBuffer.allocate(count);
				dcStart.put(tmp, 0, count);
				decompressedData = dcStart.array();
			} else { //already have data, append tmp to us
				if (count > 0) { //only perform this step if the inflation yielded results.
				ByteBuffer tmpbuf = ByteBuffer.allocate(decompressedData.length + count);
				tmpbuf.put(decompressedData, 0, decompressedData.length);
				tmpbuf.put(tmp, 0, count);
				tmpbuf.rewind();
				decompressedData = tmpbuf.array();
				}
			}
		} //end while
		if (mCorrupted) {
			return null;
		} else {
			return decompressedData;
		}
	}

	/** Utility function to corrup the MCCP stream. */
	public final void corruptMe() {
		mDoCorrupt = true;
	}

	/** Utility method to get if the socket is connected. 
	 * 
	 * @return Weather or not the socket is connected.
	 */
	public final boolean isConnected() {
		return mConnected;
	}

	/** Utility method to close the socket, including the reader and writer threads. */
	public final void closeSocket() {
		try {
			mClosing = true;
			mConnected = false;
			if (mSocket != null) {
				mSocket.shutdownInput();
				mSocket.shutdownOutput();
				mSocket.close();
			}
			if (mReader != null) {
				mReader.close();
			}
			mSocket = null;
			this.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/** Getter for mHandler.
	 * 
	 * @return The mHandler handler for the reader thread.
	 */
	public final Handler getHandler() {
		return mHandler;
	}
		
}
