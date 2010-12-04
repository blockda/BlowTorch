package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.os.Handler;
import android.os.Message;
//import android.util.Log;
//import android.util.Log;
//import android.util.Log;
//import android.util.Log;
import android.util.Log;

public class Processor {
	
	Handler reportto = null;
	Colorizer colormebad = new Colorizer();
	OptionNegotiator opthandler;
	IStellarService.Stub service = null;
	
	private String encoding = null;
	
	public Processor(Handler useme,IStellarService.Stub theserv,String pEncoding) {
		reportto = useme;
		service = theserv;
		
		opthandler = new OptionNegotiator(reportto);
		
		setEncoding(pEncoding);
	}
	
	private final byte IAC = (byte) 0xFF;
	private final byte SB = (byte) 0xFA;
	private final byte SE = (byte)0xF0;
	
	private final byte WILL = (byte) 0xFB;
	private final byte WONT = (byte) 0xFC;
	private final byte DO = (byte) 0xFD;
	private final byte DONT = (byte) 0xFE;
	
	private final byte GOAHEAD = (byte)0xF9;
	
	private final byte TAB = (byte)0x09;
	private final byte BELL = (byte)0x07;
	//private byte
	//subnegotioation: \\xFF\\xFA(.{1})(.*)\\xFF\\xF0

	public String RawProcess(byte[] data) throws UnsupportedEncodingException {
		if(data == null) {
			return "";
		}
		
		ByteBuffer buff = ByteBuffer.allocate(data.length);
		ByteBuffer opbuf = ByteBuffer.allocate(30);

		int count = 0; //count of the number of bytes in the buffer;
		for(int i=0;i<data.length;i++) {
			switch(data[i]) {
			case IAC:
				//if the next byte is
				if((data[i+1] >= WILL && data[i+1] <= DONT) || data[i+1] == SB) {
					Log.e("SERVICE","DO IAC");
					if(data[i+1] == SB) {
						//subnegotiation
						//now we have an optional number of bytes between the indicated subnegotiation and the IAC SE end of sequence.
						boolean done = false;
						int j = i+3;
						while(!done) {
							if(data[j] == IAC) {
								if(data[j+1] == SE) {
									done = true;
								}
							} else {
								opbuf.put(data[j]);
								j++;
							}
						}
						//so if we are here, than j - (i+3)  is the number of optional bytes.
						opbuf = ByteBuffer.allocate(j-(i+3) + 5);
						opbuf.put(IAC);
						opbuf.put(data[i+1]);
						opbuf.put(data[i+2]);
						if(j-(i+3) > 0) {
							for(int q=i+3;q<j;q++) {
								opbuf.put(data[q]);
							}
						}
						opbuf.put(IAC);
						opbuf.put(SE);
						
						
						opbuf.rewind();
						boolean compress = dispatchSUB(opbuf.array());
						if(compress) {
							ByteBuffer b = ByteBuffer.allocate(data.length - 5 - i);
							//if(in[0] == IAC && in[1] == SB && in[2] == compressresp[0] && in[3] == IAC && in[4] == SE) {
								Log.e("PROCESSOR","ENCOUNTERED START OF COMPRESSION EVENT");
								//get rest
								
							for(int z=i+5;z<data.length;z++) {
								b.put(data[z]);
							}
								
							
							b.rewind();
							reportto.sendMessageAtFrontOfQueue(reportto.obtainMessage(StellarService.MESSAGE_STARTCOMPRESS,b.array()));
							byte[] trunc = new byte[count];
							buff.rewind();
							buff.get(trunc,0,count);
							return new String(trunc,encoding);
						
						} else {
							i = i + 2 + (j-(i+3)) + 2; // (original pos, plus the 2 mandatory bytes, plus the optional data length, plus the 2 bytes at the end (one is included in the loop).
						}
					} else {
						dispatchIAC(data[i+1],data[i+2]);
						i = i + 2;
					}
				} else if(data[i+1] == GOAHEAD) {
				
					i++;
					Log.e("SERVICE","DO GOAHEAD");
				} else if(data[i+1] == IAC) {
					Log.e("SERVICE","FOUND DOUBLE R");
					buff.put(data[i]);
					count++;
					i++;
				}
				break;
			case BELL:
				//dispatch bell
				reportto.sendEmptyMessage(StellarService.MESSAGE_BELLINC);
				break;
			//UNTIL FURTHER NOTICE, TAB HANDLING WILL BE THE WINDOWS RESPONSIBILITY
			default:
				buff.put(data[i]);	
				count++;
				break;
			}
			
		}
		//buff.rewind();
		//count should reflect an accurate amount of bytes written to the buffer.
		buff.rewind();
		byte[] tmp = new byte[count];
		buff.get(tmp,0,count);
		return new String(tmp,encoding);
	}
	
	public void dispatchIAC(byte action,byte option) throws UnsupportedEncodingException {
		//byte[] snd = new byte[3];
		//snd[0] = (byte)TC.IAC;
		//byte[] atmp = action.getBytes("ISO-8859-1");
		//snd[1] = atmp[0];
		//atmp = option.getBytes("ISO-8859-1");
		//snd[2] = atmp[0];
		
		Log.e("PROCESSOR","GOT COMMAND:" + "IAC|" + TC.decodeInt(new String(new byte[]{action},encoding),encoding) + "|"+ TC.decodeInt(new String(new byte[]{option},encoding), encoding));
		byte[] resp = opthandler.processCommand(IAC, action, option);
		Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
		Log.e("PROCESSOR","SENDING RESPONSE:" + TC.decodeInt(new String(resp,encoding), encoding));
		
		reportto.sendMessage(sb);
	}
	
	public boolean dispatchSUB(byte[] negotiation) throws UnsupportedEncodingException {
		//byte[] stmp = negotiation.getBytes("ISO-8859-1");
		Log.e("PROCESSOR","GOT SUBNEGOTIATION:" + TC.decodeInt(new String(negotiation,encoding), encoding));

		byte[] sub_r = opthandler.getSubnegotiationResponse(negotiation);
		//String sub_resp = new String(opthandler.getSubnegotiationResponse(stmp));
		
		if(sub_r == null) {
			//Log.e("PROCESSOR","SUBNEGOTIATION RESPONSE NULL");
			return false;
		} else {
			
			Log.e("PROCESSOR","RESPONSE:" + TC.decodeInt(new String(sub_r,encoding), encoding));
		}

		//special handling for the compression marker.
		byte[] compressresp = new byte[1];
		compressresp[0] = TC.COMPRESS2;
		
		if(sub_r[0] == compressresp[0]) {
			return true;
		} else {
			Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,sub_r);
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
	
	public void setDisplayDimensions(int rows,int cols) {
		opthandler.setColumns(cols);
		opthandler.setRows(rows);
	}
	
	public void disaptchNawsString() {
		if(opthandler.getNawsString() == null) return;
		Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,opthandler.getNawsString());
		reportto.sendMessage(sbm);
		return;
	}
	

}
