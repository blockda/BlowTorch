package com.happygoatstudios.bt.service;




import java.io.BufferedOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.Toast;

import com.happygoatstudios.bt.service.IBaardTERMServiceCallback;
import com.happygoatstudios.bt.service.IBaardTERMService;


public class BaardTERMService extends Service {

	RemoteCallbackList<IBaardTERMServiceCallback> callbacks = new RemoteCallbackList<IBaardTERMServiceCallback>();
	
	NotificationManager mNM;
	
	OutputStream output_writer = null;
	
	OutputWriter outputter = null;
	
	Processor the_processor = null;
	
	Object sendlock = new Object();
	
	protected int bindCount = 0;
	
	InetAddress the_addr = null;
	String host;
	int port;
	final int BAD_PORT = 999999;
	final String BAD_HOST = "NOTSETYET";
	Socket the_socket = null;
	
	DataPumper pump = null;
	
	Handler myhandler = null;
	
	public int trigger_count = 5555;
	
	final static public int MESSAGE_PROCESS = 102;
	final static public int MESSAGE_INIT = 100;
	final static public int MESSAGE_END = 101;
	final static public int MESSAGE_SETDATA = 103;
	final static public int MESSAGE_STARTCOMPRESS = 104;
	final static public int MESSAGE_ENDCOMPRESS = 105;
	final static public int MESSAGE_SENDDATA = 106;
	final static public int MESSAGE_REQUESTBUFFER = 107;
	final static public int MESSAGE_CHECKIFALIVE = 109;

	protected static final int MESSAGE_SAVEBUFFER = 108;
	
	public boolean sending = false;
	
	StringBuffer the_buffer = new StringBuffer();
	
	
	public void onCreate() {
		//called when we are created from a startService or bindService call with the IBaardTERMService interface intent.
		Log.e("SERV","BAARDTERMSERVICE STARTING!");
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		host = BAD_HOST;
		port = BAD_PORT;
		
		//Looper.prepare();
		
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_INIT:
					try {
						doStartup();
					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				case MESSAGE_END:
					pump.stop();
					doShutdown();
					break;
				case MESSAGE_PROCESS:
					byte[] data = msg.getData().getByteArray("THEBYTES");
					//need to throttle this somehow to avoid sending messages too fast.
					try {
						dispatch(data);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_SETDATA:
					host = msg.getData().getString("HOST");
					port = msg.getData().getInt("PORT");
					break;
				case MESSAGE_STARTCOMPRESS:
					pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_COMPRESS);
					break;
				case MESSAGE_ENDCOMPRESS:
					break;
				case MESSAGE_SENDDATA:
					//Message dmsg = outputter.getHandler().obtainMessage(102);
					//Bundle bun = new Bundle();
					//bun.putByteArray("THEDATA", msg.getData().getByteArray("THEDATA"));
					//dmsg.setData(bun);
					//outputter.getHandler().sendMessage(dmsg);
					
					byte[] bytes = msg.getData().getByteArray("THEDATA");
					
					
					
					try {
						String dbgmsg = new String(bytes,"ISO-8859-1");
						
						
						Log.e("SERV","SENDING STRING " + dbgmsg + "|size: " + bytes.length);
						
						output_writer.write(bytes);
						//if(dbgmsg.contains("noc")) {
							//Log.e("SERV","CLOSEING STREAM");
							//output_writer.flush();
							//output_writer.flush();
							//output_writer.close();
						//}

						output_writer.flush();
						
						//Log.e("SERV","calling notify.");

						//output_writer.
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					//}
					
					}
					break;
				case MESSAGE_REQUESTBUFFER:
					//dispatch();
					try {
						sendBuffer();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MESSAGE_SAVEBUFFER:
					the_buffer = new StringBuffer(msg.getData().getString("BUFFER") + the_buffer);
					break;
				case MESSAGE_CHECKIFALIVE:
					if(the_socket != null) {
						//the_socket.
						/*if(!the_socket.isConnected() || the_socket.isClosed()) {
						//do shutdown, except now, attempt to launch a dialog.
							Toast.makeText(BaardTERMService.this, "CONNECTION DEAD", 3000);
						}*/
					}
					this.sendEmptyMessageDelayed(BaardTERMService.MESSAGE_CHECKIFALIVE, 3000);
					break;
				default:
					break;	
				}
				
			}
		};
		showNotification();
		//Log.e("SERV","REACHED THE END OF THE STARTUP METHOD");
		//Looper.loop();
		myhandler.sendEmptyMessageDelayed(BaardTERMService.MESSAGE_CHECKIFALIVE, 3000);
		//REGISTER TRIGGER PATTERS.
		//test_set.add("Your eyes glaze over.");
		test_set.add("QUEST: You may now quest again.");
		test_set.add("i love kurt");
		//test_set.add("TICK");
		//test_set.add("--> TICK <--");
	}
	
	public void onDestroy() {
		Log.e("SERV","ON DESTROY CALLED!");
		doShutdown();
	}
	
	Object binderCookie = new Object();
	Boolean hasListener = false;
	private final IBaardTERMService.Stub mBinder = new IBaardTERMService.Stub() {
		public void registerCallback(IBaardTERMServiceCallback m) {
			if(m != null && !hasListener) {
				if(callbacks.register(m,binderCookie)) {
					bindCount++;
					Log.e("SERV","Registering callback, " + bindCount + " now.");
					hasListener = true;
				} else {
					Log.e("SERV","Callback not registerd because it is already in the list, " + bindCount + " now.");
				}
			} else {
				callbacks.kill();
				callbacks = new RemoteCallbackList<IBaardTERMServiceCallback>();
				if(m!= null) {
					callbacks.register(m);
					hasListener = true;
				}
				
			}
		}
		
		
		public void unregisterCallback(IBaardTERMServiceCallback m)
		throws RemoteException {
			if(m != null) {
				if(callbacks.unregister(m)) {
					bindCount--;
					Log.e("SERV","Unregistering callback, " + bindCount + " left.");
					hasListener = false;
				}
			}
	
		}

		public int getPid() throws RemoteException {
			return Process.myPid();
		}
		
		
		public void endXfer() {
			
		}
		
		public void setNotificationText(CharSequence seq) throws RemoteException {
			//do some stuff
			
		}

		public void initXfer() throws RemoteException {
			//
			myhandler.sendEmptyMessage(BaardTERMService.MESSAGE_INIT);
		}

		public void sendData(byte[] seq) throws RemoteException {
			//String str = (String)seq;
			
			//byte[] bytes = str.getBytes();
			
			//TODO: instead of sending the message from the binder object, send myself a message, then dispatch to pump
			/*Message msg = pump.getHandler().obtainMessage(DataPumper.MESSAGE_SEND);
			
			Bundle b = new Bundle();
			
			b.putByteArray("THEDATA", seq);
			
			msg.setData(b);
			
			pump.getHandler().sendMessage(msg);*/
			
			//ENTER GIANT SYNCHRONIZATION STEP
			if(myhandler.hasMessages(BaardTERMService.MESSAGE_SENDDATA)) {
			
				//Log.e("SERV","GOING TO SLEEP");
			
				synchronized(sendlock) {
				
					while(myhandler.hasMessages(BaardTERMService.MESSAGE_SENDDATA)) {
						
						try {
							sendlock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				//Log.e("SERV","WAKING BACK UP AFTER NOTIFY CALLED");
			
			}
			
			Message msg = myhandler.obtainMessage(BaardTERMService.MESSAGE_SENDDATA);
			
			Bundle b = new Bundle();
			
			b.putByteArray("THEDATA",seq);
			
			msg.setData(b);
			
			myhandler.sendMessage(msg);
		
			
		}
		
		public void setConnectionData(String ihost,int iport) {
			//host = ihost;
			//port = iport;
			Message msg = myhandler.obtainMessage(BaardTERMService.MESSAGE_SETDATA);
			Bundle b = new Bundle();
			b.putString("HOST",ihost);
			b.putInt("PORT",iport);
			
			msg.setData(b);
			
			myhandler.sendMessage(msg);
			
		}
		
		public void beginCompression() {
			myhandler.sendEmptyMessage(BaardTERMService.MESSAGE_STARTCOMPRESS);
			
		}
		
		public void stopCompression() {
			
		}

		public void requestBuffer() throws RemoteException {
			myhandler.sendEmptyMessage(BaardTERMService.MESSAGE_REQUESTBUFFER);
			
		}

		public void saveBuffer(String buffer) throws RemoteException {
			Message msg = myhandler.obtainMessage(BaardTERMService.MESSAGE_SAVEBUFFER);
			Bundle b = msg.getData();
			b.putString("BUFFER",buffer);
			msg.setData(b);
			myhandler.sendMessage(msg);
			
		}


	};
	
	Pattern newline = Pattern.compile("\n");
	

	public Bundle CountNewLine(String ISOLATINstring,int maxlines) {
		
		Matcher match = newline.matcher(ISOLATINstring);
		
		int prunelocation = 0;
		int numberfound = 0;
		while(match.find()) {
			numberfound++;
		}
		
		if(numberfound > maxlines) {
			int numtoprune = numberfound - maxlines;
			match.reset();
			for(int i = 0;i < numtoprune;i++) {
				if(match.find()) { //shouldalways be true
					prunelocation = match.start();
				}
			}
			//by the time we are here, the prunelocation is known
		}
		
		Bundle dat = new Bundle();
		dat.putInt("TOTAL", numberfound);
		dat.putInt("PRUNELOC", prunelocation);
		
		return dat;
	}
	
	public void sendBuffer() throws RemoteException {
		final int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			//callbacks.getBroadcastItem(i).dataIncoming(data);
			//callbacks.getBroadcastItem(i).processedDataIncoming(the_buffer);
			callbacks.getBroadcastItem(i).rawBufferIncoming(the_buffer.toString());
		}
		
		callbacks.finishBroadcast();
		
		//Log.e("SERV","BUFFERED DATA REQUESTED. Delivered and cleared.");
		
		if(the_buffer != null) {
			the_buffer.setLength(0);
			//the_buffer.clearSpans();
		}
	}
	
	Colorizer colorer = new Colorizer();
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	StringBuffer regexp_test = new StringBuffer();
	Vector<String> test_set = new Vector<String>();
	
	public void dispatch(byte[] data) throws RemoteException, UnsupportedEncodingException {
		
		String rawData = the_processor.RawProcess(data);

		
		//Spannable processed = the_processor.DoProcess(data);
		
		
		//String htmlText = colorer.htmlColorize(data);
		//Log.e("SERV","MADE SOME HTML:"+htmlText);
		
		the_buffer.append(rawData);
		
		//IDLE:  "Your eyes glaze over."
		//REQU:  "QUEST: You may now quest again."
		Matcher stripcolor = colordata.matcher(rawData);
		regexp_test.append(stripcolor.replaceAll(""));
		
		//
		//test the de-colorized data against registered patterns.
		Iterator<String> items = test_set.listIterator();
		while(items.hasNext()) {
			
			String test_for = (String)items.next();
			
			
			//Log.e("SERVICE","TESTING FOR:" + test_for + "| agaist:" + regexp_test);
			//test_for.replaceAll(".", "\\.");
			//test_for.replaceAll("\"", "\\\""); //yea bitch
			//test_for.replaceAll("[", substitute)
			//String regex = "^.*"+Pattern.quote(test_for)+".*$";
			//String regex = Pattern.quote(test_for);
			Pattern p = Pattern.compile("^.*"+Pattern.quote(test_for)+".*$",Pattern.DOTALL);
			
			//Boolean matched = Pattern.matches(regex, new String(regexp_test.toString().getBytes("ISO-8859-1"),"ISO-8859-1"));
			//Log.e("SLICK","MATCH: "+regexp_test+"\nAGAINST: " + regex + "\nRETURNED:" + matched);
			Matcher results = p.matcher(regexp_test);
			//Matcher results = p.matcher(regexp_test);
			//results.
			if(results.matches()) {
				//NOTIFICATION: 
				Log.e("SERVICE","SERVICE MATCHED: " + test_for);
				//build a new notification.
				Notification note = new Notification(R.drawable.btn_plus,"BaardTERM-Pattern Matched",System.currentTimeMillis());
				//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
				
				Context context = getApplicationContext();
				CharSequence contentTitle = "BaardTERM";
				//CharSequence contentText = "Hello World!";
				CharSequence contentText = "Triggerd on: " + test_for;
				Intent notificationIntent = new Intent(this, com.happygoatstudios.bt.window.BaardTERMWindow.class);

				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

				
				note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				
				note.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
				note.defaults = Notification.DEFAULT_SOUND;
				note.ledARGB = 0xFFFF00FF;
				note.ledOnMS = 300;
				note.ledOffMS = 300;
				
				long[] vp = new long[4];
				vp[0] = 0;
				vp[1] = 200;
				vp[2] = 50;
				vp[3] = 200;
				
				note.vibrate = vp;
				
				mNM.notify(trigger_count++,note);
				
			}
		}
		
		regexp_test.setLength(0);
		//the_buffer.append(sstr);
		/*Bundle datal = CountNewLine(new String(the_buffer.toString().getBytes("ISO-8859-1"),"ISO-8859-1"),100);
		if(datal.getInt("TOTAL") > 100) {
			//Log.e("SERV","TIME TO PRUNE ");
			Object[] unusedspans = the_buffer.getSpans(0, datal.getInt("PRUNELOC"), Object.class);
			for(int i=0;i<unusedspans.length;i++) {
				the_buffer.removeSpan(unusedspans[i]);
			}
			Object[] array = the_buffer.getSpans(0,the_buffer.length(),Object.class);
			int size = array.length;
			//Log.e("SERV","FOUND: " + size + " spans.\n");
			the_buffer.replace(0, datal.getInt("PRUNELOC"), "");
			
		}*/
		
		
		
		
		
		final int N = callbacks.beginBroadcast();
		int final_count = N;
		

		
		for(int i = 0;i<N;i++) {
			//callbacks.getBroadcastItem(i).dataIncoming(data);
			//callbacks.getBroadcastItem(i).processedDataIncoming(the_buffer);
			//callbacks.getBroadcastItem(i).htmlDataIncoming(htmlText);
			try {
			callbacks.getBroadcastItem(i).rawDataIncoming(rawData);
			} catch (RemoteException e) {
				//just need to catch it, don't need to care, the list maintains itself apparently.
				final_count = final_count - 1;
			}
		}
		
		callbacks.finishBroadcast();
		
		//if(callbacks.)
		if(final_count == 0) {
			//someone is listening so don't save the buffer
			//Log.e("SERV","No listeners, buffering data.");
		} else {
			
			the_buffer.setLength(0);
			//the_buffer.clearSpans();
			//Log.e("SERV","Clearing the buffer because I have " + bindCount + " listeners.");
		}
		
		//callbacks.finishBroadcast();
		
	}
	
	public void doStartup() throws SocketException {
		if(host == BAD_HOST || port == BAD_PORT) {
			return; //dont' start 
		}
		
		InetAddress addr = null;
		try {
			//InetAddress[] x = InetAddress.getAllByName(host);
			addr = InetAddress.getByName(host);
			//addr = x[0];
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		the_addr = addr;
		if(the_addr == null) {
			//Log.e("SERV","NULL ADDRESS LOOKED UP");
		}
		

		
		try {
			the_socket = new Socket(addr.getHostAddress(),port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		the_socket.setSendBufferSize(1024);
		int size = the_socket.getSendBufferSize();
		boolean shut = the_socket.isOutputShutdown();
		if(shut) {
			//Log.e("SERV","SOCKET OUTPUT IS SHUT DOWN");
		} else {
			//Log.e("SERV","SOCKET OUTPUT ACTIVE, BUFFER SIZE: " + size);
		}
		
		try {
			output_writer = new BufferedOutputStream(the_socket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//at this point we should have a valid socket
		//start up the pump
		//try {
		//	outputter = new OutputWriter(the_socket.getOutputStream());
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		//outputter.start();
		try {
			pump = new DataPumper(the_socket.getInputStream(),null,myhandler);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pump.start();
		
		synchronized(this) {
			try {
				this.wait(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //give the pump some time sto start up
		}
		
		pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_INITXFER);
		
		//show notification
		showNotification();
		
		the_processor = new Processor(myhandler,mBinder);
		the_buffer = new StringBuffer();
	}
	
	private void showNotification() {
		
		
		
		Notification note = new Notification(R.drawable.btn_plus,"BaardTERM",System.currentTimeMillis());
		//note.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "BaardTERM";
		//CharSequence contentText = "Hello World!";
		CharSequence contentText = "Connected: "+ host +":"+ port + ")";
		Intent notificationIntent = new Intent(this, com.happygoatstudios.bt.window.BaardTERMWindow.class);

		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		
		note.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		note.flags = Notification.FLAG_ONGOING_EVENT;
		
		mNM.notify(5545,note);
		
	}
	
	public void doShutdown() {
		//pump.stop();
		if(pump != null) {
			pump.getHandler().sendEmptyMessage(DataPumper.MESSAGE_END);
			pump = null;
		}
		
		if(output_writer != null) {
			try {
				output_writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		if(the_socket != null) {
			try {
				the_socket.close();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			the_socket = null;
		}
		//kill the notification.
		mNM.cancel(5545);
		
		
	}
	
	public void doProcess(byte[] data) {
		//broadcast this data.
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

}
