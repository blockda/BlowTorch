package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.keplerproject.luajava.LuaState;

import com.happygoatstudios.bt.settings.ConfigurationLoader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class Processor {

	Handler reportto = null;
	Colorizer colormebad = new Colorizer();
	OptionNegotiator opthandler;

	private String encoding = null;
	private Context mContext = null;
	public Processor(Handler useme,
			String pEncoding,Context pContext,LuaState L) {
		reportto = useme;

		mContext = pContext;
		String ttype = ConfigurationLoader.getConfigurationValue("terminalTypeString", mContext);
		opthandler = new OptionNegotiator(ttype);
		gmcp = new GMCPData(useme,L);
		setEncoding(pEncoding);
	}

	private boolean debugTelnet = true;

	public boolean isDebugTelnet() {
		return debugTelnet;
	}

	public void setDebugTelnet(boolean debugTelnet) {
		this.debugTelnet = debugTelnet;
		//Log.e("PROC","SETTING DEBUG TELNET TO " + debugTelnet);
	}

	private final byte IAC = (byte) 0xFF;
	private final byte SB = (byte) 0xFA;
	private final byte SE = (byte) 0xF0;
	private final byte WILL = (byte) 0xFB;
	private final byte DONT = (byte) 0xFE;
	private final byte BREAK = (byte) 243; // NVT character BRK.
	// private final byte Interrupt Process 244 //The function IP.
	private final byte AO = (byte) 245; // The function AO.
	private final byte AYT = (byte) 246; // The function AYT.
	private final byte EC = (byte) 247; // The function EC.
	private final byte EL = (byte) 248; // The function EL.
	private final byte CARRIAGE = (byte)0x0D;
	private final byte GOAHEAD = (byte) 0xF9;
	private final byte IP = (byte) 0xF4;
	// private final byte TAB = (byte)0x09;
	private final byte BELL = (byte) 0x07;
	byte[] holdover = null;

	public byte[] RawProcess(byte[] data) {
		if (data == null) {
			return null;
		}
		
		if(data.length == 1) {
			if(data[0] == IAC) {
				return null; //nothing to do here.
			}
		}

		ByteBuffer buff = null;
		if(holdover == null) {buff = ByteBuffer.allocate(data.length); }
		else { buff = ByteBuffer.allocate(data.length + holdover.length); buff.put(holdover); holdover = null; }
		ByteBuffer opbuf = ByteBuffer.allocate(data.length*2);

		int count = 0; // count of the number of bytes in the buffer;
		for (int i = 0; i < data.length; i++) {
			switch (data[i]) {
			case IAC:
				// if the next byte is
				if(i > data.length-1) {
					holdover = new byte[] { (byte)0xFF };
					return null;
				}
				if ((data[i + 1] >= WILL && data[i + 1] <= DONT)
						|| data[i + 1] == SB) {
					//Log.e("SERVICE", "DO IAC");
					// switch(data[i+1])
					if (data[i + 1] == SB) {
						// subnegotiation
						// now we have an optional number of bytes between the
						// indicated subnegotiation and the IAC SE end of
						// sequence.
						boolean done = false;
						int j = i + 3;
						while (!done) {
							if (data[j] == IAC) {
								if (data[j + 1] == SE) {
									done = true;
								}
							} else {
								//opbuf.put(data[j]);
								j++;
							}
						}
						// so if we are here, than j - (i+3) is the number of
						// optional bytes.
						opbuf = ByteBuffer.allocate(j - (i + 3) + 5);
						opbuf.put(IAC);
						opbuf.put(data[i + 1]);
						opbuf.put(data[i + 2]);
						if (j - (i + 3) > 0) {
							for (int q = i + 3; q < j; q++) {
								opbuf.put(data[q]);
							}
						}
						opbuf.put(IAC);
						opbuf.put(SE);

						opbuf.rewind();
						boolean compress = dispatchSUB(opbuf.array());
						if (compress) {
							ByteBuffer b = ByteBuffer.allocate(data.length - 5
									- i);
							// if(in[0] == IAC && in[1] == SB && in[2] ==
							// compressresp[0] && in[3] == IAC && in[4] == SE) {
							//Log.e("PROCESSOR",
									//"ENCOUNTERED START OF COMPRESSION EVENT");
							// get rest

							for (int z = i + 5; z < data.length; z++) {
								b.put(data[z]);
							}

							b.rewind();
							reportto.sendMessageAtFrontOfQueue(reportto
									.obtainMessage(
											StellarService.MESSAGE_STARTCOMPRESS,
											b.array()));
							if(debugTelnet) {
								String message = "\n"+Colorizer.telOptColorBegin + "IN:[IAC SB COMPRESS2 IAC SE] -BEGIN COMPRESSION-" + Colorizer.telOptColorEnd+"\n";
								reportto.sendMessageDelayed(reportto.obtainMessage(StellarService.MESSAGE_PROCESSORWARNING,message), 1);
							}
							byte[] trunc = new byte[count];
							buff.rewind();
							buff.get(trunc, 0, count);
							return trunc;
							//try {
							//	return new String(trunc, encoding);
							//} catch (UnsupportedEncodingException e) {
							//	throw new RuntimeException(e);
							//}

						} else {
							i = i + 2 + (j - (i + 3)) + 2; // (original pos,
															// plus the 2
															// mandatory bytes,
															// plus the optional
															// data length, plus
															// the 2 bytes at
															// the end (one is
															// included in the
															// loop).
						}
					} else {
						dispatchIAC(data[i + 1], data[i + 2]);
						i = i + 2;
					}
				} else {// if(data[i+1] == GOAHEAD) {

					switch (data[i + 1]) {
					case IAC:
						buff.put(data[i]); // and one IAC and consume the extra.
						count++;
						break;
					case GOAHEAD:
					case IP:
						// TODO: REAL IP HANDLING HERE, I THINK THIS INVOLVES
						// SETTING THE CURSOR BACK TO A PLACE OR SOMETHING
					case BREAK:
					case AO:
						// i think this one is more for us to send to the
						// server.
					case EC:
						// TODO: REAL ERASE CHARACTER
					case EL:
						// TODO: REAL ERASE LINE
					case AYT:
						i++; // consume the byte.
						break;
					default:
						// everything else keep
						break;
					}
				}
				break;
			case BELL:
				reportto.sendEmptyMessage(StellarService.MESSAGE_BELLINC);
				break;
			case CARRIAGE:
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

	public void dispatchIAC(byte action,byte option) {
		
		//Log.e("PROCESSOR","GOT COMMAND:" + "IAC|" + TC.decodeInt(new String(new byte[]{action},encoding),encoding) + "|"+ TC.decodeInt(new String(new byte[]{option},encoding), encoding));
		byte[] resp = opthandler.processCommand(IAC, action, option);
		Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
		if(resp.length > 2) {
			if(resp[2] == TC.NAWS) {
				//naws has started.
				disaptchNawsString();
			}
			
		}
		
		//Log.e("PROCESSOR","SENDING RESPONSE:" + TC.decodeInt(new String(resp,encoding), encoding));
		//message format: IN:[WILL ECHO] OUT:[DONT ECHO] //background.
		Bundle b = sb.getData();
		b.putByteArray("THE_DATA", resp);
		String message = null;
		if(debugTelnet) {
			message = Colorizer.telOptColorBegin + "IN:[" +TC.decodeIAC(new byte[]{IAC,action,option}) + "]" + " ";
			message += Colorizer.telOptColorBegin + "OUT:[" + TC.decodeIAC(resp) + "]"+ Colorizer.telOptColorEnd + "\n";
			//reportto.sendMessageDelayed(reportto.obtainMessage(StellarService.MESSAGE_PROCESSORWARNING, message),5);
		}
		b.putString("DEBUG_MESSAGE", message);
		sb.setData(b);
		reportto.sendMessage(sb);
		
		if(action == TC.WILL && option == TC.GMCP) {
			//so we are responding accordingly, but we want to "initialize" the gmcp
			initGMCP();
		}
		
	}
	

	public boolean dispatchSUB(byte[] negotiation) {
		// byte[] stmp = negotiation.getBytes("ISO-8859-1");
		// Log.e("PROCESSOR","GOT SUBNEGOTIATION:" + TC.decodeInt(new
		// String(negotiation,encoding), encoding));

		byte[] sub_r = opthandler.getSubnegotiationResponse(negotiation);
		// String sub_resp = new
		// String(opthandler.getSubnegotiationResponse(stmp));

		if (sub_r == null) {
			// Log.e("PROCESSOR","SUBNEGOTIATION RESPONSE NULL");
			return false;
		} else {

		}

		
		
		// special handling for the compression marker.
		byte[] compressresp = new byte[1];
		compressresp[0] = TC.COMPRESS2;

		if (sub_r[0] == compressresp[0]) {
			return true;
		} else if(sub_r[0] == TC.GMCP) {
			//TODO: GMCP SUBNEGOTIATION RESPONSE CAUGHT HERE!!!!!!!!
			//String message = "\n"+Colorizer.telOptColorBegin + "IN:["+TC.decodeSUB(negotiation)+"]" + Colorizer.telOptColorEnd+"\n";
			//Log.e("GMCP","RECIEVED GMCP: " + message);
			if(debugTelnet) {
				String message = "\n"+Colorizer.telOptColorBegin + "IN:["+TC.decodeSUB(negotiation)+"]" + Colorizer.telOptColorEnd+"\n";
				reportto.sendMessageDelayed(reportto.obtainMessage(StellarService.MESSAGE_PROCESSORWARNING,message), 1);
				
				
				
			}
			byte[] foo = new byte[negotiation.length-5];
			ByteBuffer wrap = ByteBuffer.wrap(negotiation);
			wrap.rewind();
			wrap.position(3);
			wrap.get(foo, 0, negotiation.length-5);
			try {
				String whole = new String(foo,"UTF-8");
				int split = whole.indexOf(" ");
				String module = whole.substring(0, split);
				String data = whole.substring(split+1, whole.length());
				try {
					JSONObject jo = new JSONObject(data);
					gmcp.absorb(module, jo);
				} catch (JSONException e) {
					Log.e("GMCP","GMCP PARSING FOR: " + data);
					Log.e("GMCP","REASON: " + e.getMessage());
					e.printStackTrace();
					//Log.e("GMCP","STACK: " + e.);
				}
				
				//Log.e("GMCP","MODULE NAME: " + module);
				//String output = "";
				//Iterator<String> it = jo.keys();
				//while(it.hasNext()) {
				//	String tmp = it.next();
				//	String dat = jo.getString(tmp);
				//	output += " ["+tmp+"=>"+dat+"] ";
				//}
				//Log.e("GMCP","DATA: " + output);
				//TODO: THIS IS WHERE THE ACTUAL WORK IS DONE TO SEND MUD DATA.
				/*if(module.equals("char.vitals")) {
					int hp = jo.getInt("hp");
					int mp = jo.getInt("mana");
					reportto.sendMessage(reportto.obtainMessage(StellarService.MESSAGE_VITALS, hp, mp));
				} else if(module.equals("char.maxstats")) {
					int hp = jo.getInt("maxhp");
					int mp = jo.getInt("maxmana");
					reportto.sendMessage(reportto.obtainMessage(StellarService.MESSAGE_MAXVITALS, hp, mp));
				} else if(module.equals("char.status")) {
					int value = -1;
					if(!jo.getString("enemy").equals("")) {
						//Log.e("LOG","SENDING ENEMY STATE");
						value = jo.getInt("enemypct");
						
					}
					reportto.sendMessage(reportto.obtainMessage(StellarService.MESSAGE_ENEMYHP, value, 0));
				}*/
				//jo.
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return false;
		} else {
			String message = null;
			if(debugTelnet) {
				message = Colorizer.telOptColorBegin + "IN:[" + TC.decodeSUB(negotiation) + "]" + " ";
				message += Colorizer.telOptColorBegin + "OUT:[" +TC.decodeSUB(sub_r) + "]" + Colorizer.telOptColorEnd + "\n";
				//reportto.sendMessage(reportto.obtainMessage(StellarService.MESSAGE_PROCESSORWARNING, message));
			}
			Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA);
			Bundle b = sbm.getData();
			b.putByteArray("THE_DATA",sub_r);
			b.putString("DEBUG_MESSAGE", message);
			sbm.setData(b);
			reportto.sendMessage(sbm);
			return false;
		}
		
		
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setDisplayDimensions(int rows, int cols) {
		opthandler.setColumns(cols);
		opthandler.setRows(rows);
	}

	public void disaptchNawsString() {
		byte[] nawsout = opthandler.getNawsString();
		if(nawsout == null) {
			//Log.e("PROCESSOR","NAWS NOT CURRENTLY NEGOTIABLE");
			return;
		}
		//Log.e("PROCESSOR","DISPATCHING NAWS");
		Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA);
		Bundle b = sbm.getData();
		b.putByteArray("THE_DATA", nawsout);
		
		String message = null;
		if(debugTelnet) {
			message = Colorizer.telOptColorBegin + "OUT:[" + TC.decodeSUB(nawsout) + "]" + Colorizer.telOptColorEnd + "\n";
		}
		b.putString("DEBUG_MESSAGE", message);
		sbm.setData(b);
		reportto.sendMessageDelayed(sbm,2);
		return;
	}

	public void reset() {
		opthandler.reset();
	}
	
	public Object getGMCPValue(String str) {
		return gmcp.get(str);
	}
	
	public HashMap<String,Object> getGMCPTable(String path) {
		
		//String parts[] = path.split(".");
		
		return gmcp.getTable(path);
	}
	
	public void initGMCP() {
		String hello = "core.hello {\"client\": \"BlowTorch\"," +
			"\"version\": \"1.4\"}";
		String support = "core.supports.set [\"core 1\",\"char 1\"]";
		try {
			byte[] hellob = getGMCPResponse(hello);
			byte[] supportb = getGMCPResponse(support);
			
			String out_hello = Colorizer.telOptColorBegin + "OUT:[" +TC.decodeSUB(hellob) + "]" + Colorizer.telOptColorEnd + "\n";
			String out_support = Colorizer.telOptColorBegin + "OUT:[" +TC.decodeSUB(supportb) + "]" + Colorizer.telOptColorEnd + "\n";
			
			Message hm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA);
			Bundle bh = hm.getData();
			bh.putByteArray("THE_DATA",hellob);
			bh.putString("DEBUG_MESSAGE", out_hello);
			hm.setData(bh);
			reportto.sendMessage(hm);
			
			Message sm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA);
			Bundle bs = sm.getData();
			bs.putByteArray("THE_DATA",supportb);
			bs.putString("DEBUG_MESSAGE", out_support);
			sm.setData(bs);
			reportto.sendMessage(sm);
		
		} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
}
	}
	
	public byte[] getGMCPResponse(String str) throws UnsupportedEncodingException {
		//check for IAC in the string.
		int iaccount = 0;
		byte[] tmp = str.getBytes("ISO-8859-1");
		for(int i=0;i<tmp.length;i++) {
			if(tmp[i] == TC.IAC) {
				iaccount++;
			}
		}
		
		
		byte[] resp = new byte[str.getBytes("ISO-8859-1").length + 5 + iaccount];
		resp[0] = TC.IAC;
		resp[1] = TC.SB;
		resp[2] = TC.GMCP;
		resp[resp.length-1] = TC.SE;
		resp[resp.length-2] = TC.IAC;
		int j=3;
		for(int i=0;i<(tmp.length);i++) {
			resp[j] = tmp[i];
			if(tmp[i] == TC.IAC) {
				resp[j+1] = TC.IAC;
				j++;
			}
			j++;
		}
		
		
		return resp;
	}

	public void dumpGMCP() {
		// TODO Auto-generated method stub
		gmcp.dumpCache();
	}

	GMCPData gmcp = null;

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
