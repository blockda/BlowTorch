/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.offsetnull.bt.settings.ConfigurationLoader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/** Class implementation for the telnet state machine. */
public class Processor {
	/** Skippable bytes in the state machine for case 1. */
	private static final int SKIP_BYTES = 3;
	/** Telnet SUB payload byte count. */
	private static final int PAYLOAD_BYTES = 5;
	/** Handler object to dispatch results to. */
	private Handler mReportTo = null;	
	/** Negotiation sublayer object. */
	private OptionNegotiator mOptionHandler;
	/** Selected encoding to use. */
	private String mEncoding = null;
	/** Application context. */
	private Context mContext = null;
	/** Weather or not to display telnet debugging messages. */
	private boolean mDebugTelnet = false;
	/** Holdover sequence buffer. Used when a telnet negotation spans a transmission boundary. */
	private byte[] mHoldover = null;
	/** GMCP Data holder object. */
	private GMCPData mGMCP = null;
	/** List of GMCP Triggers. */
	private HashMap<String, ArrayList<GMCPWatcher>> mGMCPTriggers = new HashMap<String, ArrayList<GMCPWatcher>>();
	/** GMCP Hello string. */
	private String mGMCPHello = "core.hello {\"client\": \"BlowTorch\",\"version\": \"1.4\"}";
	/** Tracker for weather or not the use GMCP. */
	private Boolean mUseGMCP = false;
	/** GMCP Supports string. */
	private String mGMCPSupports = "core.supports.set [\"char 1\"]";
	/** Constructor.
	 * 
	 * @param useme reporting handler target.
	 * @param pEncoding selected encoding to use.
	 * @param pContext application content.
	 */
	public Processor(final Handler useme, final String pEncoding, final Context pContext) {
		mReportTo = useme;

		mContext = pContext;
		String ttype = ConfigurationLoader.getConfigurationValue("terminalTypeString", mContext);
		mOptionHandler = new OptionNegotiator(ttype);
		mGMCP = new GMCPData();
		setEncoding(pEncoding);
	}

	/** Getter for mDebugTelnet.
	 * 
	 * @return mDebugTelnet
	 */
	public final boolean isDebugTelnet() {
		return mDebugTelnet;
	}

	/** Setter for mDebugTelnet.
	 * 
	 * @param debugTelnet value for mDebugTelnet
	 */
	public final void setDebugTelnet(final boolean debugTelnet) {
		mDebugTelnet = debugTelnet;
	}
	
	/** The main processing routine.
	 * 
	 * @param data The data to process.
	 * @return The processed data minus telnet data.
	 */
	public final byte[] rawProcess(final byte[] data) {
		if (data == null) {
			return null;
		}
		
		if (data.length == 1) {
			if (data[0] == TC.IAC) {
				return null; //nothing to do here.
			}
		}

		ByteBuffer buff = null;
		if (mHoldover == null) { 
			buff = ByteBuffer.allocate(data.length); 
		} else { 
			buff = ByteBuffer.allocate(data.length + mHoldover.length); 
			buff.put(mHoldover); 
			mHoldover = null; 
		}
		ByteBuffer opbuf = ByteBuffer.allocate(data.length * 2);

		int count = 0; // count of the number of bytes in the buffer;
		for (int i = 0; i < data.length; i++) {
			switch (data[i]) {
			case TC.IAC:
				// if the next byte is
				if (i > data.length - 1) {
					mHoldover = new byte[] {TC.IAC};
					return null;
				}
				if ((data[i + 1] >= TC.WILL && data[i + 1] <= TC.DONT)
						|| data[i + 1] == TC.SB) {
					//Log.e("SERVICE", "DO IAC");
					// switch(data[i+1])
					if (data[i + 1] == TC.SB) {
						// subnegotiation
						// now we have an optional number of bytes between the
						// indicated subnegotiation and the IAC SE end of
						// sequence.
						boolean done = false;
						int j = i + SKIP_BYTES;
						while (!done) {
							if (data[j] == TC.IAC) {
								if (data[j + 1] == TC.SE) {
									done = true;
								}
							} else {
								//opbuf.put(data[j]);
								j++;
							}
						}
						// so if we are here, than j - (i+3) is the number of
						// optional bytes.
						opbuf = ByteBuffer.allocate(j - (i + SKIP_BYTES) + PAYLOAD_BYTES);
						opbuf.put(TC.IAC);
						opbuf.put(data[i + 1]);
						opbuf.put(data[i + 2]);
						if (j - (i + SKIP_BYTES) > 0) {
							for (int q = i + SKIP_BYTES; q < j; q++) {
								opbuf.put(data[q]);
							}
						}
						opbuf.put(TC.IAC);
						opbuf.put(TC.SE);

						opbuf.rewind();
						boolean compress = dispatchSUB(opbuf.array());
						if (compress) {
							ByteBuffer b = ByteBuffer.allocate(data.length - PAYLOAD_BYTES - i);
							for (int z = i + PAYLOAD_BYTES; z < data.length; z++) {
								b.put(data[z]);
							}

							b.rewind();
							mReportTo.sendMessageAtFrontOfQueue(mReportTo
									.obtainMessage(
											Connection.MESSAGE_STARTCOMPRESS,
											b.array()));
							if (mDebugTelnet) {
								String message = "\n" + Colorizer.getTeloptStartColor() + "IN:[IAC SB COMPRESS2 IAC SE] -BEGIN COMPRESSION-" + Colorizer.getResetColor() + "\n";
								mReportTo.sendMessageDelayed(mReportTo.obtainMessage(Connection.MESSAGE_PROCESSORWARNING, message), 1);
							}
							byte[] trunc = new byte[count];
							buff.rewind();
							buff.get(trunc, 0, count);
							return trunc;

						} else {
							i = i + 2 + (j - (i + SKIP_BYTES)) + 2; // (original pos,
															// plus the 2
															// mandatory bytes,
															// plus the optional
															// data length, plus
															// the 2 bytes at
															// the end (one is
															// included in the
															// loop).
															// Thus the extra 2 + 2, and not the PAYLOAD_BYTES constant.
						}
					} else {
						dispatchIAC(data[i + 1], data[i + 2]);
						i = i + 2;
					}
				} else {

					switch (data[i + 1]) {
					case TC.IAC:
						buff.put(data[i]); // and one IAC and consume the extra.
						count++;
						break;
					case TC.GOAHEAD:
					case TC.IP:
						// TODO: REAL IP HANDLING HERE, I THINK THIS INVOLVES
						// SETTING THE CURSOR BACK TO A PLACE OR SOMETHING
					case TC.BREAK:
					case TC.AO:
						// i think this one is more for us to send to the
						// server.
					case TC.EC:
						// TODO: REAL ERASE CHARACTER
					case TC.EL:
						// TODO: REAL ERASE LINE
					case TC.AYT:
						i++; // consume the byte.
						break;
					default:
						// everything else keep
						break;
					}
				}
				break;
			case TC.BELL:
				mReportTo.sendEmptyMessage(Connection.MESSAGE_BELLINC);
				break;
			case TC.CARRIAGE:
				//strip carriage returns
				break;
			default:
				buff.put(data[i]);
				count++;
				break;
			}

		}
		
		buff.rewind();
		byte[] tmp = new byte[count];
		buff.get(tmp, 0, count);
		return tmp;
		
	}

	/** Telnet negotiation sequence.
	 * 
	 * @param action The action byte (WILL, WONT, DO, DONT)
	 * @param option The numeric indicator of the telnet negotiation type (TTYPE, GMCP, ECHO ...)
	 */
	public final void dispatchIAC(final byte action, final byte option) {
		
		byte[] resp = mOptionHandler.processCommand(TC.IAC, action, option);
		Message sb = mReportTo.obtainMessage(Connection.MESSAGE_SENDOPTIONDATA, resp);
		if (resp.length > 2) {
			if (resp[2] == TC.NAWS) {
				//naws has started.
				disaptchNawsString();
			}
			
		}
		Bundle b = sb.getData();
		b.putByteArray("THE_DATA", resp);
		String message = null;
		if (mDebugTelnet) {
			message = Colorizer.getTeloptStartColor() + "IN:[" + TC.decodeIAC(new byte[]{TC.IAC, action, option}) + "]" + " ";
			message += Colorizer.getTeloptStartColor() + "OUT:[" + TC.decodeIAC(resp) + "]" + Colorizer.getResetColor() + "\n";
		}
		b.putString("DEBUG_MESSAGE", message);
		sb.setData(b);
		mReportTo.sendMessage(sb);
		
		if (action == TC.WILL && option == TC.GMCP) {
			//so we are responding accordingly, but we want to "initialize" the gmcp
			if (mUseGMCP) {
				initGMCP();
			}
		}
		
	}
	
	/** Telnet subnegotiation handler. 
	 * 
	 * @param negotiation the subnegotiation sequence.
	 * @return I think the return value here means start compression. But that would be bad.
	 */
	public final boolean dispatchSUB(final byte[] negotiation) {
		byte[] sub = mOptionHandler.getSubnegotiationResponse(negotiation);

		if (sub == null) {
			return false;
		} 
		
		// special handling for the compression marker.
		byte[] compressresp = new byte[1];
		compressresp[0] = TC.COMPRESS2;

		if (sub[0] == compressresp[0]) {
			return true;
		} else if (sub[0] == TC.GMCP) {
			if (mDebugTelnet) {
				String message = "\n" + Colorizer.getTeloptStartColor() + "IN:[" + TC.decodeSUB(negotiation) + "]" + Colorizer.getResetColor() + "\n";
				mReportTo.sendMessageDelayed(mReportTo.obtainMessage(Connection.MESSAGE_PROCESSORWARNING, message), 1);
			}
			byte[] foo = new byte[negotiation.length - PAYLOAD_BYTES];
			ByteBuffer wrap = ByteBuffer.wrap(negotiation);
			wrap.rewind();
			wrap.position(SKIP_BYTES);
			wrap.get(foo, 0, negotiation.length - PAYLOAD_BYTES);
			try {
				String whole = new String(foo, "UTF-8");
				int split = whole.indexOf(" ");
				String module = whole.substring(0, split);
				String data = whole.substring(split + 1, whole.length());
				try {
					JSONObject jo = new JSONObject(data);
					mGMCP.absorb(module, jo);
				} catch (JSONException e) {
					Log.e("GMCP", "GMCP PARSING FOR: " + data);
					Log.e("GMCP", "REASON: " + e.getMessage());
					e.printStackTrace();
				}
				
				//TODO: THIS IS WHERE THE ACTUAL WORK IS DONE TO SEND MUD DATA.
				ArrayList<GMCPWatcher> list = mGMCPTriggers.get(module);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						GMCPWatcher tmp = list.get(i);
						HashMap<String, Object> tmpdata = mGMCP.getTable(module);
						Message gmsg = mReportTo.obtainMessage(Connection.MESSAGE_GMCPTRIGGERED, tmpdata);
						gmsg.getData().putString("TARGET", tmp.mPlugin);
						gmsg.getData().putString("CALLBACK", tmp.mCallback);
						mReportTo.sendMessage(gmsg);
					}
				}				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			
			return false;
		} else {
			String message = null;
			if (mDebugTelnet) {
				message = Colorizer.getTeloptStartColor() + "IN:[" + TC.decodeSUB(negotiation) + "]" + " ";
				message += Colorizer.getTeloptStartColor() + "OUT:[" + TC.decodeSUB(sub) + "]" + Colorizer.getResetColor() + "\n";
			}
			Message sbm = mReportTo.obtainMessage(Connection.MESSAGE_SENDOPTIONDATA);
			Bundle b = sbm.getData();
			b.putByteArray("THE_DATA", sub);
			b.putString("DEBUG_MESSAGE", message);
			sbm.setData(b);
			mReportTo.sendMessage(sbm);
			return false;
		}
		
		
	}

	/** Setter for mEncoding.
	 * 
	 * @param encoding Selected encoding.
	 */
	public final void setEncoding(final String encoding) {
		this.mEncoding = encoding;
	}

	/** Getter for mEncoding.
	 * 
	 * @return The currently selected encoding.
	 */
	public final String getEncoding() {
		return mEncoding;
	}

	/** Helper method for NAWS.
	 * 
	 * @param rows Rows in display.
	 * @param cols Columns in display.
	 */
	public final void setDisplayDimensions(final int rows, final int cols) {
		mOptionHandler.setColumns(cols);
		mOptionHandler.setRows(rows);
	}

	/** Helper method for naws. This may happen because the foreground window changed shape. */
	public final void disaptchNawsString() {
		byte[] nawsout = mOptionHandler.getNawsString();
		if (nawsout == null) {
			return;
		}
		Message sbm = mReportTo.obtainMessage(Connection.MESSAGE_SENDOPTIONDATA);
		Bundle b = sbm.getData();
		b.putByteArray("THE_DATA", nawsout);
		
		String message = null;
		if (mDebugTelnet) {
			message = Colorizer.getTeloptStartColor() + "OUT:[" + TC.decodeSUB(nawsout) + "]" + Colorizer.getResetColor() + "\n";
		}
		b.putString("DEBUG_MESSAGE", message);
		sbm.setData(b);
		mReportTo.sendMessageDelayed(sbm, 2);
		return;
	}

	/** Reset method, this is called when the settings have been foreably reloaded. */
	public final void reset() {
		mOptionHandler.reset();
	}
	
	/** Helper method to get a GMCP module quickly.
	 * 
	 * @param str The module to get?
	 * @return The table of data?
	 */
	public final Object getGMCPValue(final String str) {
		return mGMCP.get(str);
	}
	
	/** Helper method to get a GMCP table for a given path.
	 * 
	 * @param path The module path, e.g. char.vitals.hp
	 * @return The mapping of objects representing the gmcp table at the desired path.
	 */
	public final HashMap<String, Object> getGMCPTable(final String path) {
		return mGMCP.getTable(path);
	}
	
	/** Utility method to initialize GMCP. */
	public final void initGMCP() {
		
		try {
			byte[] hellob = getGMCPResponse(mGMCPHello);
			byte[] supportb = getGMCPResponse(mGMCPSupports);
			
			String hello = Colorizer.getTeloptStartColor() + "OUT:[" + TC.decodeSUB(hellob) + "]" + Colorizer.getResetColor() + "\n";
			String supports = Colorizer.getTeloptStartColor() + "OUT:[" + TC.decodeSUB(supportb) + "]" + Colorizer.getResetColor() + "\n";
			
			Message hm = mReportTo.obtainMessage(Connection.MESSAGE_SENDOPTIONDATA);
			Bundle bh = hm.getData();
			bh.putByteArray("THE_DATA", hellob);
			if (mDebugTelnet) {
				bh.putString("DEBUG_MESSAGE", hello);
			}
			hm.setData(bh);
			mReportTo.sendMessage(hm);
			
			Message sm = mReportTo.obtainMessage(Connection.MESSAGE_SENDOPTIONDATA);
			Bundle bs = sm.getData();
			bs.putByteArray("THE_DATA", supportb);
			if (mDebugTelnet) {
				bs.putString("DEBUG_MESSAGE", supports);
			}
			sm.setData(bs);
			mReportTo.sendMessage(sm);
		
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/** Helper method to respond to the GMCP negotiation sequence.
	 * 
	 * @param str The subnegotiation string.
	 * @return The response.
	 * @throws UnsupportedEncodingException Thrown if the selected encoding isn't supported.
	 */
	public final byte[] getGMCPResponse(final String str) throws UnsupportedEncodingException {
		//check for IAC in the string.
		int iaccount = 0;
		byte[] tmp = str.getBytes("ISO-8859-1");
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] == TC.IAC) {
				iaccount++;
			}
		}
		
		
		byte[] resp = new byte[str.getBytes("ISO-8859-1").length + PAYLOAD_BYTES + iaccount];
		resp[0] = TC.IAC;
		resp[1] = TC.SB;
		resp[2] = TC.GMCP;
		resp[resp.length - 1] = TC.SE;
		resp[resp.length - 2] = TC.IAC;
		int j = SKIP_BYTES;
		for (int i = 0; i < tmp.length; i++) {
			resp[j] = tmp[i];
			if (tmp[i] == TC.IAC) {
				resp[j + 1] = TC.IAC;
				j++;
			}
			j++;
		}
		
		
		return resp;
	}

	/** Utility method to dump the current gmcp data to the log. */
	public final void dumpGMCP() {
		mGMCP.dumpCache();
	}

	/** Utility class representing a plugin wanting to execute a callback when a gmcp module changes. */
	public class GMCPWatcher {
		/** The plugin name. */
		private String mPlugin;
		/** The callback to execute. */
		private String mCallback;
		/** Constructor. 
		 * 
		 * @param plugin The plugin name.
		 * @param callback The callback name.
		 */
		public GMCPWatcher(final String plugin, final String callback) {
			this.mPlugin = plugin;
			this.mCallback = callback;
		}
		
		/** Getter for mPlugin. 
		 * 
		 * @return the value of mPlugin
		 */
		public final String getPlugin() {
			return mPlugin;
		}
		
		/** Getter for mCallback. 
		 * 
		 * @return value of mCallback
		 */
		public final String getCallback() {
			return mCallback;
		}
	}
	
	/** Adds a new gmcp watcher for a given module path.
	 * 
	 * @param module Module path, e.g. char.vitals.hp.
	 * @param plugin The target plugin that is watching.
	 * @param callback The callback function to execute when module has changed.
	 */
	public final void addWatcher(final String module, final String plugin, final String callback) {
		GMCPWatcher tmp = new GMCPWatcher(plugin, callback);
		
		ArrayList<GMCPWatcher> list = mGMCPTriggers.get(module);
		if (list == null) {
			ArrayList<GMCPWatcher> foo = new ArrayList<GMCPWatcher>();
			foo.add(tmp);
			mGMCPTriggers.put(module, foo);
		} else {
			list.add(tmp);
		}
		
	}

	/** Setter method for mUseGMCP. 
	 * 
	 * @param value the new value for mUseGMCP.
	 */
	public final void setUseGMCP(final Boolean value) {
		mUseGMCP = value;
		mOptionHandler.setUseGMCP(mUseGMCP);
	}

	/** Setter method for mGMCPSupports.
	 * 
	 * @param value The new value for mGMCPSupports.
	 */
	public final void setGMCPSupports(final String value) {
		mGMCPSupports = "core.supports.set [" + value + "]";
	}
}

/*
 * Straight from rfc 854 NAME CODE MEANING
 * 
 * SE 240 End of subnegotiation parameters. NOP 241 No operation. Data Mark 242
 * The data stream portion of a Synch. This should always be accompanied by a
 * TCP Urgent notification. Break 243 NVT character BRK. Interrupt Process 244
 * The function IP. Abort output 245 The function AO. Are You There 246 The
 * function AYT. Erase character 247 The function EC. Erase Line 248 The
 * function EL. Go ahead 249 The GA signal. SB 250 Indicates that what follows
 * is subnegotiation of the indicated option. WILL (option code) 251 Indicates
 * the desire to begin performing, or confirmation that you are now performing,
 * the indicated option. WON'T (option code) 252 Indicates the refusal to
 * perform, or continue performing, the indicated option. DO (option code) 253
 * Indicates the request that the other party perform, or confirmation that you
 * are expecting the other party to perform, the indicated option. DON'T (option
 * code) 254 Indicates the demand that the other party stop performing, or
 * confirmation that you are no longer expecting the other party to perform, the
 * indicated option. IAC 255 Data Byte 255.
 */
