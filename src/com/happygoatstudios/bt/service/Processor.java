package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Spannable;
import android.util.Log;
//import android.util.Log;

import com.happygoatstudios.bt.service.*;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	/*public Spannable DoProcess(byte[] data) throws UnsupportedEncodingException {
		


		
		String tmp = new String(data,"ISO-8859-1");

		
		//Matcher iac_match = iac_cmd_reg.matcher(tmp);
		//instead of doing the 4 pass run, we are going to do the trigger sytle match/lookup
		ma_matcher.reset(tmp);
		while(ma_matcher.find()) {
			//discover what kind of pattern matched.
			String matched = ma_matcher.group(0);
			//only thing to do now is to test each one.
			iac_match.reset(matched);
			boolean done = false;
			if(iac_match.matches()) {
				//dispatch IAC
				String action = iac_match.group(1);
				String option = iac_match.group(2);
				
				
				byte[] snd = new byte[3];
				snd[0] = TC.IAC;
				byte[] atmp = action.getBytes("ISO-8859-1");
				snd[1] = atmp[0];
				atmp = option.getBytes("ISO-8859-1");
				snd[2] = atmp[0];
				

				byte[] resp = opthandler.processCommand(snd[0], snd[1], snd[2]);
				//String response = new String(resp,"ISO-8859-1");
			
					
				String debug_input = TC.getEncodedString(snd);
				String debug_output = TC.getEncodedString(resp);

				Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
				reportto.sendMessage(sb);
				done = true;
			} else {
				sub_match.reset(matched);
				if(sub_match.matches()) {
					//dispatch SUB
					String subneg = sub_match.group(0);
					
					byte[] stmp = subneg.getBytes("ISO-8859-1");
					

					byte[] sub_r = opthandler.getSubnegotiationResponse(stmp);
					String sub_resp = new String(opthandler.getSubnegotiationResponse(stmp));
					
					//special handling for the compression marker.
					byte[] compressresp = new byte[1];
					compressresp[0] = TC.COMPRESS2;
					if(sub_r[0] == compressresp[0]) {	
						reportto.sendEmptyMessage(StellarService.MESSAGE_STARTCOMPRESS);
					} else {
						Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,sub_r);
						reportto.sendMessage(sbm);
					}
				} else {
					goahead_match.reset(matched);
					if(goahead_match.matches()) {
						//replace goahead.
					} else {
						tab_match.reset(matched);
						if(tab_match.matches()) {
							//replace tab
						} else {
							//we have some kind of option i don't know how to deal with it.
						}
					}
				}
			}
			
		}
		
		
		iac_match.reset(tmp);
		while(iac_match.find()) {
			String action = iac_match.group(1);
			String option = iac_match.group(2);
			
			
			byte[] snd = new byte[3];
			snd[0] = TC.IAC;
			byte[] atmp = action.getBytes("ISO-8859-1");
			snd[1] = atmp[0];
			atmp = option.getBytes("ISO-8859-1");
			snd[2] = atmp[0];
			

			byte[] resp = opthandler.processCommand(snd[0], snd[1], snd[2]);
			//String response = new String(resp,"ISO-8859-1");
		
				
			String debug_input = TC.getEncodedString(snd);
			String debug_output = TC.getEncodedString(resp);

				Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);

				//we have woken
				reportto.sendMessage(sb);
		}
		
		//String secondpass = iac_match.replaceAll("");
		
		//Matcher sub_match = subnego_reg.matcher(secondpass);
		sub_match.reset(iac_match.replaceAll(""));
		while(sub_match.find()) {
			String subneg = sub_match.group(0);
			
			byte[] stmp = subneg.getBytes("ISO-8859-1");
			

			byte[] sub_r = opthandler.getSubnegotiationResponse(stmp);
			String sub_resp = new String(opthandler.getSubnegotiationResponse(stmp));
			
			//special handling for the compression marker.
			byte[] compressresp = new byte[1];
			compressresp[0] = TC.COMPRESS2;
			if(sub_r[0] == compressresp[0]) {
				//Log.e("PROC","PROCESSOR ENCOUNTERED COMPRESSION START, STARTING COMPRESSION");
				
				reportto.sendEmptyMessage(StellarService.MESSAGE_STARTCOMPRESS);

			} else {
			
				Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,sub_r);

				reportto.sendMessage(sbm);
				
			
			}
		}
		

		goahead_match.reset(sub_match.replaceAll(""));
		
		tab_match.reset(goahead_match.replaceAll(""));
		
		
		String textdata = tab_match.replaceAll("");
		
		byte[] textbytes = textdata.getBytes("ISO-8859-1");
		
		Spannable retval = colormebad.Colorize(textbytes);
		
		return retval;
		
	}*/
	
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
		
		/*
		

		//Matcher iac_match = iac_cmd_reg.matcher(tmp);
		iac_match.reset(tmp);
		while(iac_match.find()) {
			String action = iac_match.group(1);
			String option = iac_match.group(2);
			
			
			byte[] snd = new byte[3];
			snd[0] = TC.IAC;
			byte[] atmp = action.getBytes("ISO-8859-1");
			snd[1] = atmp[0];
			atmp = option.getBytes("ISO-8859-1");
			snd[2] = atmp[0];
			

			byte[] resp = opthandler.processCommand(snd[0], snd[1], snd[2]);
			//String response = new String(resp,"ISO-8859-1");
		
				
			String debug_input = TC.getEncodedString(snd);
			String debug_output = TC.getEncodedString(resp);

			Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);

			reportto.sendMessage(sb);
			
		}

		sub_match.reset(iac_match.replaceAll(""));
		while(sub_match.find()) {
			String subneg = sub_match.group(0);
			
			byte[] stmp = subneg.getBytes("ISO-8859-1");
			

			byte[] sub_r = opthandler.getSubnegotiationResponse(stmp);
			String sub_resp = new String(opthandler.getSubnegotiationResponse(stmp));
			
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
		
	
		goahead_match.reset(sub_match.replaceAll(""));

		tab_match.reset(goahead_match.replaceAll(""));
		
		String textdata = tab_match.replaceAll("    ");
		

		
		
		byte[] textbytes = textdata.getBytes("ISO-8859-1");
		
		return textdata;*/
	}
	
	public void dispatchIAC(String action,String option) throws UnsupportedEncodingException {
		//String action = iac_match.group(1);
		//String option = iac_match.group(2);
		
		
		byte[] snd = new byte[3];
		snd[0] = TC.IAC;
		byte[] atmp = action.getBytes("ISO-8859-1");
		snd[1] = atmp[0];
		atmp = option.getBytes("ISO-8859-1");
		snd[2] = atmp[0];
		

		byte[] resp = opthandler.processCommand(snd[0], snd[1], snd[2]);
		//String response = new String(resp,"ISO-8859-1");
	
			
		//String debug_input = TC.getEncodedString(snd);
		//String debug_output = TC.getEncodedString(resp);

		Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);

		reportto.sendMessage(sb);
	}
	
	public void dispatchSUB(String negotiation) throws UnsupportedEncodingException {
		//String subneg = sub_match.group(0);
		
		byte[] stmp = negotiation.getBytes("ISO-8859-1");
		

		byte[] sub_r = opthandler.getSubnegotiationResponse(stmp);
		String sub_resp = new String(opthandler.getSubnegotiationResponse(stmp));
		
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
	

}
