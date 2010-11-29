package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.os.Handler;
import android.os.Message;
//import android.util.Log;
//import android.util.Log;

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
			
			iac_match = iac_cmd_reg.matcher("");
			sub_match = subnego_reg.matcher("");
			goahead_match = goahead_reg.matcher("");
			tab_match = tab_reg.matcher("");
			bell_match = bell_reg.matcher("");
			
			massive_match = Pattern.compile("("+iac_cmd_reg.pattern()+")|" + "("+subnego_reg.pattern()+")|" + "("+goahead_reg.pattern()+")|" + "("+tab_reg.pattern()+")|(" + bell_reg.pattern() + ")");
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
	
	Pattern massive_match;
	Matcher ma_matcher;
	
	StringBuffer holder = new StringBuffer("");
	public String RawProcess(byte[] data) throws UnsupportedEncodingException {
		if(data == null) {
			return "";
		}
		String tmp = new String(data,encoding);
		
		//StringBuffer holder = new StringBuffer("");
		holder.setLength(0);
		
		ma_matcher.reset(tmp);
		
		//Log.e("PROCESSOR","DEC:" + TC.decodeInt(tmp.substring(0,tmp.length()),encoding) + " encoded in " + encoding);
		
		boolean hasmatched = false;
		while(ma_matcher.find()) {
			hasmatched = true;
			String matched = ma_matcher.group(0);
			boolean tabreplace = false;
			boolean bellreplace= false;
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
					dispatchSUB(sub_match.group(0));
					//Log.e("PROCESSOR","GOT SUB " + TC.decodeInt(sub_match.group(0),encoding));
					
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
		
		
	}
	
	public void dispatchIAC(String action,String option) throws UnsupportedEncodingException {
		byte[] snd = new byte[3];
		snd[0] = (byte)TC.IAC;
		byte[] atmp = action.getBytes("ISO-8859-1");
		snd[1] = atmp[0];
		atmp = option.getBytes("ISO-8859-1");
		snd[2] = atmp[0];
		
		//Log.e("PROCESSOR","GOT COMMAND:" + "IAC|" + TC.decodeInt(action,encoding) + "|"+ TC.decodeInt(option, encoding));
		byte[] resp = opthandler.processCommand(snd[0], snd[1], snd[2]);
		Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
		//Log.e("PROCESSOR","SENDING RESPONSE:" + TC.decodeInt(new String(resp,encoding), encoding));
		
		reportto.sendMessage(sb);
	}
	
	public void dispatchSUB(String negotiation) throws UnsupportedEncodingException {
		byte[] stmp = negotiation.getBytes("ISO-8859-1");
		//Log.e("PROCESSOR","GOT SUBNEGOTIATION:" + TC.decodeInt(negotiation, encoding));

		byte[] sub_r = opthandler.getSubnegotiationResponse(stmp);
		//String sub_resp = new String(opthandler.getSubnegotiationResponse(stmp));
		
		if(sub_r == null) {
			//Log.e("PROCESSOR","SUBNEGOTIATION RESPONSE NULL");
			return;
		} else {
			//Log.e("PROCESSOR","RESPONSE:" + TC.decodeInt(new String(sub_r,encoding), encoding));
		}

		//special handling for the compression marker.
		byte[] compressresp = new byte[1];
		compressresp[0] = TC.COMPRESS2;
		if(sub_r[0] == compressresp[0]) {
			reportto.sendMessageAtFrontOfQueue(reportto.obtainMessage(StellarService.MESSAGE_STARTCOMPRESS));

		} else {
			Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,sub_r);
			reportto.sendMessage(sbm);

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
		Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,opthandler.getNawsString());
		reportto.sendMessage(sbm);
		return;
	}
	

}
