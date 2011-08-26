package com.happygoatstudios.bt.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.button.SlickButtonData;
import com.happygoatstudios.bt.responder.toast.ToastResponder;
import com.happygoatstudios.bt.service.StellarService.Data;
import com.happygoatstudios.bt.service.StellarService.SpecialCommand;
import com.happygoatstudios.bt.service.plugin.ConnectionSettingsPlugin;
import com.happygoatstudios.bt.service.plugin.Plugin;
import com.happygoatstudios.bt.settings.ColorSetSettings;
import com.happygoatstudios.bt.speedwalk.DirectionData;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.window.TextTree;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class Connection {
	//base "connection class"
	public final int MESSAGE_STARTUP = 1;
	public final static int MESSAGE_STARTCOMPRESS = 2;
	public final static int MESSAGE_PROCESSORWARNING = 3;
	public final static int MESSAGE_SENDOPTIONDATA = 4;
	public final static int MESSAGE_BELLINC = 5;
	public final static int MESSAGE_DODIALOG = 6;
	public final static int MESSAGE_PROCESS = 7;
	public final static int MESSAGE_DISCONNECTED = 8;
	public static final int MESSAGE_MCCPFATALERROR = 9;
	public final int MESSAGE_SENDDATA = 9;
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
	ConnectionSettingsPlugin the_settings = null;
	
	Character cr = new Character((char)13);
	Character lf = new Character((char)10);
	String crlf = cr.toString() + lf.toString();
	Pattern newline = Pattern.compile("\n");
	Pattern semicolon = Pattern.compile(";");
	Pattern commandPattern = Pattern.compile("^.(\\w+)\\s*(.*)$");
	Matcher commandMatcher = commandPattern.matcher("");
	private HashMap<String,SpecialCommand> specialcommands = new HashMap<String,SpecialCommand>();
	StringBuffer joined_alias = new StringBuffer();
	Pattern alias_replace = Pattern.compile(joined_alias.toString());
	Matcher alias_replacer = alias_replace.matcher("");
	Matcher alias_recursive = alias_replace.matcher("");
	Pattern whiteSpace = Pattern.compile("\\s");
	
	public Connection(String display,String host,int port,StellarService service) {
		
		this.display = display;
		this.host = host;
		this.port = port;
		this.service = service;
		
		plugins = new ArrayList<Plugin>();
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_SENDDATA:
					
					byte[] bytes = (byte[]) msg.obj;
					
					Data d = null;
					try {
						d = ProcessOutputData(new String(bytes,the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e2) {
						e2.printStackTrace();
					}
					
					if(d == null) {
						return;
					}
					
					String nosemidata = null;
					try {
						
						if(d.cmdString != null && !d.cmdString.equals("")) {
							nosemidata = d.cmdString;
							byte[] sendtest = nosemidata.getBytes(the_settings.getEncoding());
							ByteBuffer buf = ByteBuffer.allocate(sendtest.length*2); //just in case EVERY byte is the IAC
							//int found = 0;
							int count = 0;
							for(int i=0;i<sendtest.length;i++) {
								if(sendtest[i] == (byte)0xFF) {
									//buf = ByteBuffer.wrap(buf.array());
									buf.put((byte)0xFF);
									buf.put((byte)0xFF);
									count += 2;
								} else {
									buf.put(sendtest[i]);
									count++;
								}
							}
							
							byte[] tosend = new byte[count];
							buf.rewind();
							buf.get(tosend,0,count);
							
							if(pump.isConnected()) {
								//output_writer.write(tosend);
								//output_writer.flush();
								pump.sendData(tosend);
								//pump.handler.sendMessage(datasend);
							} else {
								sendBytesToWindow(new String(Colorizer.colorRed + "\nDisconnected.\n" + Colorizer.colorWhite).getBytes("UTF-8"));
							}
						} else {
							if(d.cmdString.equals("") && d.visString == null) {
								pump.sendData(crlf.getBytes(the_settings.getEncoding()));
								//pump.handler.sendMessage(datasend);
								//output_writer.flush();
								d.visString = "\n";
							}
						}
						//send the transformed data back to the window
						if(d.visString != null && !d.visString.equals("")) {
							if(the_settings.isLocalEcho()) {
								//preserve.
								//buffer_tree.addBytesImplSimple(data)
								sendBytesToWindow(d.visString.getBytes(the_settings.getEncoding()));
							}
						}
					} catch (IOException e) {
						//throw new RuntimeException(e);
						this.sendEmptyMessage(MESSAGE_DISCONNECTED);
					}
					break;
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
		the_settings = new ConnectionSettingsPlugin();
		//private void loadDefaultDirections() {
			HashMap<String,DirectionData> tmp = new HashMap<String,DirectionData>();
			tmp.put("n", new DirectionData("n","n"));
			tmp.put("e", new DirectionData("e","e"));
			tmp.put("s", new DirectionData("s","s"));
			tmp.put("w", new DirectionData("w","w"));
			tmp.put("h", new DirectionData("h","nw"));
			tmp.put("j", new DirectionData("j","ne"));
			tmp.put("k", new DirectionData("k","sw"));
			tmp.put("l", new DirectionData("l","se"));
			the_settings.setDirections(tmp);
		//}
		//load plugins.
		
		//TODO: load plugins.
		
		buffer = new TextTree();
		working = new TextTree();
		
		//TODO: set TextTree encoding options.
		
		//handler.sendEmptyMessage(MESSAGE_STARTUP);
	}
	
	protected void DoDisconnect(Object object) {
		//TODO: if window showing, show disconnection.
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
		//service.DispatchDialog(str);
	}

	protected void sendDataToWindow(String message) {
		/*try {
			//TODO: use encoding setting.
			service.doDispatchNoProcess(message.getBytes("ISO-8859-1"));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
	}
	
	protected void sendBytesToWindow(byte[] data) {
		//service.sendRawDataToWindow(data);
		int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				callbacks.getBroadcastItem(i).rawDataIncoming(data);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		callbacks.finishBroadcast();
		
	}

	private void doStartup() {
		//int tmpPort = 0;
		//String host = "";
		//String display = "";
		loadConnectionData();
		
		pump = new DataPumper(host,port,handler);
		pump.start();
		
		processor = new Processor(handler,"ISO-8859-1",service.getApplicationContext(),null);
		
		//show notification somehow.
		isConnected = true;
	}
	
	private void loadConnectionData() {
		int N = callbacks.beginBroadcast();
		for(int i = 0;i<N;i++) {
			try {
				port = callbacks.getBroadcastItem(i).getPort();
				host = callbacks.getBroadcastItem(i).getHost();
				display = callbacks.getBroadcastItem(i).getDisplay();
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//host = callbacks.getBroadcastItem(i))
		}
		callbacks.finishBroadcast();
	}
	
	StringBuffer dataToServer = new StringBuffer();
	StringBuffer dataToWindow = new StringBuffer();
	private Data ProcessOutputData(String out) throws UnsupportedEncodingException {
		dataToServer.setLength(0);
		dataToWindow.setLength(0);
		//Log.e("BT","PROCESSING: " + out);
		//steps that need to happen.
		if(out.endsWith("\n")) {
			out = out.substring(0, out.length()-2);
		}
		
		if(out.equals("")) {
			Data enter = new Data();
			enter.cmdString = "";
			enter.visString = null;
			return enter;
		}
		//1 - chop up input up on semi's
		String[] commands = null;
		if(the_settings.isSemiIsNewLine()) {
			commands = semicolon.split(out);  
		} else {
			commands = new String[] { out };
		}
		StringBuffer holdover = new StringBuffer();
		//2 - for each unit		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(commands));
		
		
		ListIterator<String> iterator = list.listIterator();
		while(iterator.hasNext()) {
			String cmd = iterator.next();
			
			if(cmd.endsWith("~")) {
				holdover.append(cmd.substring(0,cmd.length()-1) + ";");
			} else {
				if(holdover.length() > 0) {
					cmd = holdover.toString() + cmd;
					holdover.setLength(0);
				}
				//3 - do special command processing.
				Data d = ProcessCommand(cmd);
				//4 - handle command processing output
				
				if(d != null) {
					boolean m = false;
					if(d.cmdString != null && d.visString != null) {
						if(d.cmdString.equals(d.visString)) {
							m = true; //aliases & regular commands will always have the same cmdString and visString
						}
					}
					
					//5 - alias replacement				
					if(d.cmdString != null && !d.cmdString.equals("")) {
						Boolean reprocess = true;
						byte[] tmp = DoAliasReplacement(d.cmdString.getBytes(the_settings.getEncoding()),reprocess);
						String tmpstr = new String(tmp,the_settings.getEncoding());
						//if(tmpstr)
						if(!d.cmdString.equals(tmpstr)) {
							//alias replaced, needs to be processed
							
							String[] alias_cmds = null;
							if(the_settings.isSemiIsNewLine()) {
								alias_cmds = semicolon.split(tmpstr);
							} else {
								alias_cmds = new String[] { tmpstr };
							}
							for(String alias_cmd : alias_cmds) {
								iterator.add(alias_cmd);
							}
							if(reprocess) {
								for(int ax=0;ax<alias_cmds.length;ax++) {
									iterator.previous();
								}
							}
						
						} else {
						
							if(m) {
								String srv = new String(tmp,the_settings.getEncoding()) + crlf;
								//srv = srv.replace(";", crlf);
								dataToServer.append(new String(srv));
								
								dataToWindow.append(new String(tmp,the_settings.getEncoding()) + ";");
							} else {
								String srv = new String(tmp,the_settings.getEncoding()) + crlf;
								//srv = srv.replace(";", crlf);
								dataToServer.append(new String(srv));
							}
						}
					}
						//dataToServer.append(d.cmdString + crlf);
					if(d.visString != null && !d.visString.equals("")) {
						if(!m) {
							dataToWindow.append(d.visString + ";");
						}
					}
				}
			

			}
		}
		//7 - return Data packet with commands to send to server, and data to send to window.
		Data d = new Data();
		d.cmdString = dataToServer.toString();
		d.visString = dataToWindow.toString();
		//if(the_settings.isSemiIsNewLine()) {
			if(d.visString.endsWith(";")) {
				d.visString = d.visString.substring(0,d.visString.length()-1) + "\n";
			}
		//}
		//Log.e("BT","TO SERVER:" + d.cmdString);
		//Log.e("BT","TO WINDOW:" + d.visString);
		
		return d;
	}
	
	public class Data {
		public String cmdString;
		public String visString;
		public Data() {
			cmdString = "";
			visString = "";
		}
	}
	
	public Data ProcessCommand(String cmd) {
		//Log.e("SERVICE","IN CMD: "+cmd);
		Data data = new Data();
		if(cmd.equals(".." + "\n") || cmd.equals("..")) {
			//Log.e("SERVICE","CMD==\"..\"");
			synchronized(the_settings) {
				String outputmsg = "\n" + Colorizer.colorRed + "Dot command processing ";
				if(the_settings.isProcessPeriod()) {
					//the_settings.setProcessPeriod(false);
					overrideProcessPeriods(false);
					outputmsg = outputmsg.concat("disabled.");
				} else {
					//the_settings.setProcessPeriod(true);
					overrideProcessPeriods(true);
					outputmsg = outputmsg.concat("enabled.");
				}
				outputmsg = outputmsg.concat(Colorizer.colorWhite + "\n");
				try {
					sendBytesToWindow(outputmsg.getBytes(the_settings.getEncoding()));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
			
			return null;
		}
		
		
		if(cmd.startsWith(".") && the_settings.isProcessPeriod()) {
			
			if(cmd.startsWith("..")) {
				data.cmdString = cmd.replace("..", ".");
				data.visString = cmd.replace("..", ".");
				return data;
			}
			
			
			commandMatcher.reset(cmd);
			if(commandMatcher.find()) {
				synchronized(the_settings) {
					
					//string should be of the form .aliasname |settarget can have whitespace|

						String alias = commandMatcher.group(1);
						String argument = commandMatcher.group(2);
						
						
						if(the_settings.getSettings().getAliases().containsKey(alias)) {
							//real argument
							if(!argument.equals("")) {
								AliasData mod = the_settings.getSettings().getAliases().remove(alias);
								mod.setPost(argument);
								the_settings.getSettings().getAliases().put(alias, mod);
								data.cmdString = "";
								if(the_settings.isEchoAliasUpdates()) {
									data.visString = "["+alias+"=>"+argument+"]";
								} else {
									data.visString = "";
								}
								return data;
							} else {
								//display error message
								String noarg_message = "\n" + Colorizer.colorRed + " Alias \"" + alias + "\" can not be set to nothing. Acceptable format is \"." + alias + " replacetext\"" + Colorizer.colorWhite +"\n";
								try {
									sendBytesToWindow(noarg_message.getBytes(the_settings.getEncoding()));
								} catch (UnsupportedEncodingException e) {
									throw new RuntimeException(e);
								}
								return null;
							}
						} else if(specialcommands.containsKey(alias)){
							//Log.e("SERVICE","SERVICE FOUND SPECIAL COMMAND: " + alias);
							SpecialCommand command = specialcommands.get(alias);
							data = (Data) command.execute(argument);
							return data;
						} else {
							//format error message.
							
							String error = Colorizer.colorRed + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
							error += "  \""+alias+"\" is not a recognized alias or command.\n";
							error += "   No data has been sent to the server. If you intended\n";
							error += "   this to be done, please type \".."+alias+"\"\n";
							error += "   To toggle command processing, input \"..\" with no arguments\n";
							error += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.colorWhite+"\n";  
							
							try {
								sendBytesToWindow(error.getBytes(the_settings.getEncoding()));
							} catch (UnsupportedEncodingException e) {
								throw new RuntimeException(e);
							}
							return null;
						}
					}
			} else {
				//Log.e("SERVICE",cmd + " not valid.");
			}
			
			return data;
		} else {
			data.cmdString = cmd;
			data.visString = cmd;
			return data;
		}
		
		
		//return null;
	}
	
	private void overrideProcessPeriods(boolean value) {
		synchronized(the_settings) {
			the_settings.setProcessPeriod(value);
			//this can be called from somewhere esle, so to make sure
			//the settings activity doesn't reset a wrong value, we will write
			//the shared preferences key here
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(service);
			//boolean setvalue = prefs.getBoolean("PROCESS_PERIOD", true);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("PROCESS_PERIOD", the_settings.isProcessPeriod());
			editor.commit();
			
			//Log.e("SERVICE","SET PROCESS PERIOD FROM:" + setvalue + " to " + value);
			
		}
	}
	
	private byte[] DoAliasReplacement(byte[] input,Boolean reprocess) {
		if(joined_alias.length() > 0) {

			//Pattern to_replace = Pattern.compile(joined_alias.toString());
			byte[] retval = null;
			//Matcher replacer = null;
			try {
				alias_replacer.reset(new String(input,the_settings.getEncoding()));//replacer = to_replace.matcher(new String(bytes,the_settings.getEncoding()));
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
			StringBuffer replaced = new StringBuffer();
			
			boolean found = false;
			boolean doTail = true;
			while(alias_replacer.find()) {
				found = true;
				
				AliasData replace_with = the_settings.getSettings().getAliases().get(alias_replacer.group(0));
				//do special replace if only ^ is matched.
				if(replace_with.getPre().startsWith("^") && !replace_with.getPre().endsWith("$")) {
					doTail = false;
					//do special replace.
					String[] tParts = null;
					try {
						tParts = whiteSpace.split(new String(input,the_settings.getEncoding()));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					HashMap<String,String> map = new HashMap<String,String>();
					for(int i=0;i<tParts.length;i++) {
						map.put(Integer.toString(i), tParts[i]);
					}
					ToastResponder r = new ToastResponder();
					String finalString = r.translate(replace_with.getPost(), map);
					
					replaced.append(finalString);
					
				} else {
					alias_replacer.appendReplacement(replaced, replace_with.getPost());
				}
			}
			if(doTail) {
				alias_replacer.appendTail(replaced);
			}
			StringBuffer buffertemp = new StringBuffer();
			if(found) { //if we replaced a match, we need to continue the find/match process until none are found.
				boolean recursivefound = false;
				boolean eatTail = false;
				do {
					recursivefound = false;
					
					//Matcher recursivematch = to_replace.matcher(replaced.toString());
					alias_recursive.reset(replaced.toString());
					buffertemp.setLength(0);
					while(alias_recursive.find()) {
						recursivefound = true;
						AliasData replace_with = the_settings.getSettings().getAliases().get(alias_recursive.group(0));
						if(replace_with.getPre().startsWith("^") && ! replace_with.getPre().endsWith("$")) {
							ToastResponder r = new ToastResponder();
							String[] tParts = null;
							
							String tmpInput = replaced.toString();
							int index = tmpInput.indexOf(";");
							String rest = "";
							if(index > -1) {
								rest = tmpInput.substring(index+1,tmpInput.length());
								tmpInput = tmpInput.substring(0,index);
							}
							String sepchar = "";
							if(rest.length()>0) {
								sepchar = ";";
							}
							tParts = whiteSpace.split(tmpInput);
							
							HashMap<String,String> map = new HashMap<String,String>();
							for(int i=0;i<tParts.length;i++) {
								map.put(Integer.toString(i), tParts[i]);
							} 
							eatTail = true;
							alias_recursive.appendReplacement(buffertemp, r.translate(replace_with.getPost(),map) + sepchar +rest);
							reprocess = false;
						} else {
							alias_recursive.appendReplacement(buffertemp, replace_with.getPost());
						}
						
					}
					if(recursivefound) {
						if(!eatTail) {
							alias_recursive.appendTail(buffertemp);
						}
						replaced.setLength(0);
						replaced.append(buffertemp);
						
					}
				} while(recursivefound == true);
			}
			//so replacer should contain the transformed string now.
			//pull the bytes back out.
			try {
				retval = replaced.toString().getBytes(the_settings.getEncoding());
			} catch (UnsupportedEncodingException e1) {
				throw new RuntimeException(e1);
			}
			
			replaced.setLength(0);
			
			return retval;
		} else {
			return input;
		}
	}
	

	private void buildAliases() {
		joined_alias.setLength(0);
		
		//Object[] a = the_settings.getAliases().keySet().toArray();
		Object[] a = the_settings.getSettings().getAliases().values().toArray();
		
		
		String prefix = "\\b";
		String suffix = "\\b";
		//StringBuffer joined_alias = new StringBuffer();
		if(a.length > 0) {
			if(((AliasData)a[0]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
			if(((AliasData)a[0]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
			joined_alias.append("("+prefix+((AliasData)a[0]).getPre()+suffix+")");
			for(int i=1;i<a.length;i++) {
				if(((AliasData)a[i]).getPre().startsWith("^")) { prefix = ""; } else { prefix = "\\b"; }
				if(((AliasData)a[i]).getPre().endsWith("$")) { suffix = ""; } else { suffix = "\\b"; }
				joined_alias.append("|");
				joined_alias.append("("+prefix+((AliasData)a[i]).getPre()+suffix+")");
			}
			
		}
		
		alias_replace = Pattern.compile(joined_alias.toString());
		alias_replacer = alias_replace.matcher("");
		alias_recursive = alias_replace.matcher("");
		//Log.e("SERVICE","BUILDING ALIAS PATTERN: " + joined_alias.toString());
		
		
			
		
	}
	
	RemoteCallbackList<IConnectionBinderCallback> callbacks = new RemoteCallbackList<IConnectionBinderCallback>();
	IConnectionBinder.Stub mBinder = new IConnectionBinder.Stub() {

		public void registerCallback(IConnectionBinderCallback c)
				throws RemoteException {
			// TODO Auto-generated method stub
			if(c != null) {
				callbacks.register(c);
				if(pump == null) {
					Log.e("SERVICE","STARTING UP CONNECTION");
					doStartup();
					
				}
			}
		}

		public void unregisterCallback(IConnectionBinderCallback c)
				throws RemoteException {
			if(c !=  null) {
				callbacks.unregister(c);
			}
		}

		public int getPid() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void initXfer() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void endXfer() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean hasBuffer() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isConnected() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void sendData(byte[] seq) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void saveSettings() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setNotificationText(CharSequence seq)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setConnectionData(String host, int port, String display)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void beginCompression() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void stopCompression() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void requestBuffer() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void saveBuffer(byte[] buffer) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void addAlias(AliasData a) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public List getSystemCommands() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public Map getAliases() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setAliases(Map map) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setFontSize(int size) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getFontSize() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void setFontSpaceExtra(int size) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getFontSpaceExtra() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void setFontName(String name) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public String getFontName() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setFontPath(String path) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setMaxLines(int keepcount) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getMaxLines() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void setSemiOption(boolean bools_are_newline)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void addButton(String targetset, SlickButtonData new_button)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void removeButton(String targetset,
				SlickButtonData button_to_nuke) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public List<SlickButtonData> getButtonSet(String setname)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public List<String> getButtonSetNames() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void modifyButton(String targetset, SlickButtonData orig,
				SlickButtonData mod) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void addNewButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public List<String> getButtonSets() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public int deleteButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int clearButtonSet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public Map getButtonSetListInfo() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getLastSelectedSet() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void LoadSettingsFromPath(String path) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void ExportSettingsToPath(String path) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void resetSettings() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public ColorSetSettings getCurrentColorSetDefaults()
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public ColorSetSettings getColorSetDefaultsForSet(String the_set)
				throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setColorSetDefaultsForSet(String the_set,
				ColorSetSettings input) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setProcessPeriod(boolean value) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public Map getTriggerData() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public Map getDirectionData() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setDirectionData(Map data) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void newTrigger(TriggerData data) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void updateTrigger(TriggerData from, TriggerData to)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void deleteTrigger(String which) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public TriggerData getTrigger(String pattern) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setUseExtractUI(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean getUseExtractUI() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setThrottleBackground(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isThrottleBackground() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isProcessPeriod() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isEchoAliasUpdate() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setEchoAliasUpdate(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isSemiNewline() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setKeepWifiActive(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isKeepWifiActive() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setAttemptSuggestions(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isAttemptSuggestions() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setKeepLast(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isKeepLast() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isBackSpaceBugFix() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setBackSpaceBugFix(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isAutoLaunchEditor() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setAutoLaunchEditor(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isDisableColor() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setDisableColor(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public String HapticFeedbackMode() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setHapticFeedbackMode(String use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public String getAvailableSet() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getHFOnPress() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getHFOnFlip() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setHFOnPress(String use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setHFOnFlip(String use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setDisplayDimensions(int rows, int cols)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void reconnect() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public Map getTimers() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public TimerData getTimer(String ordinal) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void startTimer(String ordinal) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void pauseTimer(String ordinal) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void stopTimer(String ordinal) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void resetTimer(String ordinal) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void updateTimer(TimerData old, TimerData newtimer)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void addTimer(TimerData newtimer) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void removeTimer(TimerData deltimer) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getNextTimerOrdinal() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public Map getTimerProgressWad() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getEncoding() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setEncoding(String input) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public String getConnectedTo() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isKeepScreenOn() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setKeepScreenOn(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isLocalEcho() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setLocalEcho(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isVibrateOnBell() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setVibrateOnBell(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isNotifyOnBell() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setNotifyOnBell(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isDisplayOnBell() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setDisplayOnBell(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isFullScreen() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setFullScreen(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isRoundButtons() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setRoundButtons(boolean use) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getBreakAmount() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getOrientation() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean isWordWrap() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setBreakAmount(int pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setOrientation(int pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setWordWrap(boolean pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isRemoveExtraColor() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isDebugTelnet() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setRemoveExtraColor(boolean pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setDebugTelnet(boolean pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void updateAndRenameSet(String oldSet, String newSet,
				ColorSetSettings settings) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setHyperLinkMode(String pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public String getHyperLinkMode() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setHyperLinkColor(int pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public int getHyperLinkColor() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		public void setHyperLinkEnabled(boolean pIn) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isHyperLinkEnabled() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public void setTriggerEnabled(boolean enabled, String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public void setButtonSetLocked(boolean locked, String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		public boolean isButtonSetLocked(String key) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isButtonSetLockedMoveButtons(String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isButtonSetLockedNewButtons(String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isButtonSetLockedEditButtons(String key)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
}
