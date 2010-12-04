package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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
		//not really much to do here, this will be a static class thing
		reportto = useme;
		service = theserv;
		
		opthandler = new OptionNegotiator(reportto);
		
		setEncoding(pEncoding);
		try {
			iac_cmd_reg = Pattern.compile(new String("\\xFF([\\xFB-\\xFE])(.{1})".getBytes(encoding),encoding));
			subnego_reg = Pattern.compile(new String("\\xFF\\xFA(.{1})(.*)\\xFF\\xF0".getBytes(encoding),encoding));
			goahead_reg = Pattern.compile(new String("\\xFF\\xF9".getBytes(encoding),encoding));
			tab_reg  = Pattern.compile(new String("\\x09".getBytes(encoding),encoding));
			bell_reg = Pattern.compile(new String("\\x07".getBytes(encoding),encoding));
			iacasdata_reg = Pattern.compile(new String("ÿ".getBytes(encoding),encoding));
			
			iac_match = iac_cmd_reg.matcher("");
			sub_match = subnego_reg.matcher("");
			goahead_match = goahead_reg.matcher("");
			tab_match = tab_reg.matcher("");
			bell_match = bell_reg.matcher("");
			iacasdata_match = iacasdata_reg.matcher("");
			
			massive_match = Pattern.compile("("+new String(iac_cmd_reg.pattern().getBytes(encoding),encoding)+")|" + "("+new String(subnego_reg.pattern().getBytes(encoding),encoding)+")|" + "("+new String(goahead_reg.pattern().getBytes(encoding),encoding)+")|" + "("+new String(tab_reg.pattern().getBytes(encoding),encoding)+")|(" +new String(iacasdata_reg.pattern().getBytes(encoding),encoding)+")|(" + new String(bell_reg.pattern().getBytes(encoding),encoding) + ")");
			ma_matcher = massive_match.matcher("");
		} catch (PatternSyntaxException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	Pattern iac_cmd_reg;
	Matcher iac_match;
	//Pattern iac_cmd_reg = Pattern.compile("\\xFF.*");
	Pattern subnego_reg;
	Matcher sub_match;
	
	Pattern normal_reg = Pattern.compile("[\\x00-\\x7F]*"); //match any 7-bit ascii character, zero or more times
	
	Pattern goahead_reg;
	Matcher goahead_match;
	
	Pattern tab_reg;
	
	
	Matcher tab_match;
	
	Pattern bell_reg;
	Matcher bell_match;
	
	Pattern iacasdata_reg;
	Matcher iacasdata_match;
	
	Pattern massive_match;
	Matcher ma_matcher;
	
	StringBuffer holder = new StringBuffer("");
	
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
	//ByteBuffer buff = ByteBuffer.allocate(255);
	public String RawProcess(byte[] data) throws UnsupportedEncodingException {
		if(data == null) {
			return "";
		}
		
		boolean capturesub = false;
		boolean doiac = false;
		boolean dobell = false;
		boolean dotab = false;
		boolean doiacasdata = false;
		boolean skipgoahead = false;
		
		ByteBuffer buff = ByteBuffer.allocate(data.length);
		ByteBuffer opbuf = ByteBuffer.allocate(10);
		//buff.rewind();
		//buff.reset();
		for(int i=0;i<data.length;i++) {
			opbuf = ByteBuffer.allocate(255);
			switch(data[i]) {
			case IAC:
				//if the next byte is
				if((data[i+1] >= WILL && data[i+1] <= DONT) || data[i+1] == SB) {
					doiac = true;
					Log.e("SERVICE","DO IAC");
					//if iac
					if(data[i+1] == SB) {
						//subnegotiation
						opbuf.put(IAC);
						opbuf.put(data[i+1]);
						opbuf.put(data[i+2]);
						//now we have an optional number of bytes till the IAC SE
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
						dispatchSub()
						i = i + 2 + j + 1; // (original pos, plus the 2 mandatory bytes, plus the optional data length, plus the 2 bytes at the end (one is included in the loop).
						
					} else {
						opbuf.put(IAC);
						opbuf.put(data[i+1]);
						opbuf.put(data[i+2]);
						byte[] tmp1 = { data[i+1] };
						byte[] tmp2 = { data[i+2] };
						//dispatch
						dispatchIAC(new String(tmp1,"ISO-8859-1"),new String(tmp2,"ISO-8859-1"));
						i = i + 2;
					}
				} else if(data[i+1] == GOAHEAD) {
				
					skipgoahead = true;
					Log.e("SERVICE","DO GOAHEAD");
				} else if(data[i+1] == IAC) {
					Log.e("SERVICE","FOUND DOUBLE R");
					buff.put(data[i]);
					i++;
				}
				break;
			case BELL:
				//dispatch bell
				dobell = true;
				break;
			case TAB:
				dotab = true;
				break;
			default:
				buff.put(data[i]);	
				break;
			}
			/*if(doiac) {
				//so if we are here it means we got an iac byte.
				if(data[i+1] == SB) {
					//do sb sequence
					opbuf.put(IAC);
					opbuf.put(data[i+1]);
					opbuf.put(data[i+2]);
					//now we have an optional number of bytes till the IAC SE
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
					i = i + 2 + j + 1; // (original pos, plus the 2 mandatory bytes, plus the optional data length, plus the 2 bytes at the end (one is included in the loop).
					//dispatch the sub
				} else {
					//not an sb sequence do the normal.
					opbuf.put(IAC);
					opbuf.put(data[i+1]);
					opbuf.put(data[i+2]);
					//dispatch
					
					i = i + 2;
				}
			} else if(doiacasdata) {
				buff.put(data[i]);
				i++;
			} else if(dobell) {
				//just dont append
			} else if(dotab) {
				buff.limit(buff.limit()+3);
				buff.put((byte) 0x32);
				buff.put((byte) 0x32);
				buff.put((byte) 0x32);
				buff.put((byte) 0x32);
			} else {
				buff.put(data[i]);
			}*/
		}
		buff.rewind();
		return new String(buff.array(),encoding);
	}
		/*String tmp = new String(data,encoding);
		
		Log.e("PROCESSOR","TRYING: " + TC.decodeInt(tmp.substring(tmp.length()-20, tmp.length()),encoding));
		
		//StringBuffer holder = new StringBuffer("");
		holder.setLength(0);
		
		ma_matcher.reset(tmp);
		byte iac = (byte)0xFF;
		byte[] iaca = { iac };
		Pattern tmppa = Pattern.compile(new String(iaca) + new String(iaca));
		Matcher tmpma = tmppa.matcher(new String(data));
		if(tmpma.find()) {
			Log.e("SERVICE","FOUND IAC AS DATA");
		}
		
		//Log.e("PROCESSOR","DEC:" + TC.decodeInt(tmp.substring(0,tmp.length()),encoding) + " encoded in " + encoding);
		
		boolean hasmatched = false;
		while(ma_matcher.find()) {
			hasmatched = true;
			String matched = ma_matcher.group(0);
			Log.e("PROCESSOR","MATCHED: " + matched);
			boolean tabreplace = false;
			boolean bellreplace= false;
			boolean iacasdatareplace = false;
			iacasdata_match.reset(matched);
			if(iacasdata_match.matches()) {
				Log.e("PROCESSOR","IAC AS DATA");
				iacasdatareplace = true;
			} else {
				iac_match.reset(matched);
				if(iac_match.matches()) {
					//dispatch IAC
					//String action = iac_match.group(1);
					//String option = iac_match.group(2);
					dispatchIAC(iac_match.group(1),iac_match.group(2));
					//Log.e("PROCESSOR","GOT IAC " + TC.decodeInt(iac_match.group(1),encoding) + " " + TC.decodeInt(iac_match.group(2),encoding));
				} else {
					sub_match.reset(matched);
					if(sub_match.matches()) {
						//dispatch sub
						//String subneg = sub_match.group(0);
						//Log.e("PROCESSOR","GOT SUB " + TC.decodeInt(sub_match.group(0),encoding));
						//Log.e("PROCESSOR","IN:" +tmp );
						boolean skip = dispatchSUB(sub_match.group(0),data);
						if(skip) return holder.toString(); //subnegotiation had compress, we shoudl return what we have, but not the rest because it is compressed.
						
					} else {
						goahead_match.reset(matched);
						if(goahead_match.matches()) {
							//replace goahead.
						} else {
							tab_match.reset(matched);
							if(tab_match.matches()) {
								//replace tab
								tabreplace = true;
							} else {
								bell_match.reset(matched);
								if(bell_match.matches()) {
									//replace bell
									bellreplace = true;
								} else {
									//unknown.
									//Log.e("PROCESSOR","UNKNOWN:" + matched);
								}
							}
						}
					}
				}
			}
			if(tabreplace) {
				ma_matcher.appendReplacement(holder, "    ");
			} else if(bellreplace) {
				ma_matcher.appendReplacement(holder, "");
				//and do bell.
				reportto.sendEmptyMessage(StellarService.MESSAGE_BELLINC);
			} else if(iacasdatareplace){
				ma_matcher.appendReplacement(holder, matched.substring(0,1));
			} else {
				ma_matcher.appendReplacement(holder, ""); //remove it from the stream
			}
			
			
		}
		if(hasmatched) {
			ma_matcher.appendTail(holder);
		} else {
			holder.append(tmp);
		}
		
		//now we are finished, send text off for dispatching.
		return holder.toString();
		
		
	}*/
	
	public void dispatchIAC(String action,String option) throws UnsupportedEncodingException {
		byte[] snd = new byte[3];
		snd[0] = (byte)TC.IAC;
		byte[] atmp = action.getBytes("ISO-8859-1");
		snd[1] = atmp[0];
		atmp = option.getBytes("ISO-8859-1");
		snd[2] = atmp[0];
		
		Log.e("PROCESSOR","GOT COMMAND:" + "IAC|" + TC.decodeInt(action,encoding) + "|"+ TC.decodeInt(option, encoding));
		byte[] resp = opthandler.processCommand(snd[0], snd[1], snd[2]);
		Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
		Log.e("PROCESSOR","SENDING RESPONSE:" + TC.decodeInt(new String(resp,encoding), encoding));
		
		reportto.sendMessage(sb);
	}
	
	public boolean dispatchSUB(String negotiation,byte[] in) throws UnsupportedEncodingException {
		byte[] stmp = negotiation.getBytes("ISO-8859-1");
		Log.e("PROCESSOR","GOT SUBNEGOTIATION:" + TC.decodeInt(negotiation, encoding));

		byte[] sub_r = opthandler.getSubnegotiationResponse(stmp);
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
			StringBuffer b = new StringBuffer();
			sub_match.reset(new String(in,"ISO-8859-1"));
			boolean found = false;
			while(sub_match.find()) {
				found = true;
				sub_match.appendReplacement(b, "");
			}
			byte[] rest = null;
			if(found) {
				b.setLength(0);
				sub_match.appendTail(b);
				//b now contains the rest of the data.
				if(b.length() > 0) {
					rest = b.toString().getBytes("ISO-8859-1");
				}
			}
			
			reportto.sendMessageAtFrontOfQueue(reportto.obtainMessage(StellarService.MESSAGE_STARTCOMPRESS,rest));
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
