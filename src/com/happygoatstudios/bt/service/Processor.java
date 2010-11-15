package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Spannable;
//import android.util.Log;

import com.happygoatstudios.bt.service.*;

public class Processor {
	
	Handler reportto = null;
	Colorizer colormebad = new Colorizer();
	OptionNegotiator opthandler;
	IStellarService.Stub service = null;
	
	public Processor(Handler useme,IStellarService.Stub theserv) {
		//not really much to do here, this will be a static class thing
		reportto = useme;
		service = theserv;
		
		opthandler = new OptionNegotiator(reportto);
	}
	
	Pattern iac_cmd_reg = Pattern.compile("\\xFF([\\xFB-\\xFE])(.{1})");
	Matcher iac_match = iac_cmd_reg.matcher("");
	//Pattern iac_cmd_reg = Pattern.compile("\\xFF.*");
	Pattern subnego_reg = Pattern.compile("\\xFF\\xFA(.{1})(.*)\\xFF\\xF0");
	Matcher sub_match = subnego_reg.matcher("");
	
	Pattern normal_reg = Pattern.compile("[\\x00-\\x7F]*"); //match any 7-bit ascii character, zero or more times
	
	Pattern goahead_reg = Pattern.compile("\\xFF\\xF9");
	Matcher goahead_match = goahead_reg.matcher("");
	
	Pattern tab_reg = Pattern.compile("\\x09");
	Matcher tab_match = tab_reg.matcher("");
	
	public Spannable DoProcess(byte[] data) throws UnsupportedEncodingException {
		


		
		String tmp = new String(data,"ISO-8859-1");

		
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
			//Log.e("SERV","OPT: " + debug_input + "|RES: " + debug_output);
				
			//try {
				
				//service.sendData(resp);
				//instead send message
				Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
				//Bundle b = new Bundle();
				//b.putByteArray("THEDATA", resp);
				//sb.setData(b);
				
				/*synchronized(reportto) {
					while(reportto.hasMessages(BaardTERMService.MESSAGE_SENDDATA)) {
						try {
							reportto.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}*/
				
				//we have woken
				reportto.sendMessage(sb);
			//} catch (RemoteException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
			//send message to reportto now
			//Message msg = reportto.obtainMessage(BaardTERMService.MESSAGE_SENDDATA);
			//Bundle b = new Bundle();
			//b.putByteArray(new Integer(BaardTERMService.MESSAGE_SENDDATA).toString(), resp);
			//msg.setData(b);
			//reportto.sendMessage(msg);
				
				
		
			
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
				
				//service.
				//try {
					//service.beginCompression();
				//} catch (RemoteException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
			} else {
			
				Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,sub_r);
				//Bundle b = new Bundle();
				//b.putByteArray("THEDATA", sub_r);
				//sbm.setData(b);
				
				/*synchronized(reportto) {
					while(reportto.hasMessages(BaardTERMService.MESSAGE_SENDDATA)) {
						try {
							reportto.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}*/
				
				reportto.sendMessage(sbm);
				//Log.e("PROCESS","SLEEPING ON SERVICE.");
				//synchronized(service) {
					
					//service.wait();
				//}
				//Log.e("PROCESS","WOKE UP FROM SENDING DATA FROM SERVICE");
				
			
			}
		}
		
		//sigh. gotta avoid displaying the IAC GOAHEAD characters. So I'm gonna have a third pass, because I'm finding it hard to shoehorn it into the other regular expressions.
		
		
		//Matcher third_pass = goahead_reg.matcher(sub_match.replaceAll(""));
		goahead_match.reset(sub_match.replaceAll(""));
		
		tab_match.reset(goahead_match.replaceAll(""));
		
		//while(third_pass.find()) {
		//	Log.e("SERVICE","FOUND IAC GOAHEAD");
		//}
		
		//String textdata = sub_match.replaceAll("");
		String textdata = tab_match.replaceAll("");
		
		byte[] textbytes = textdata.getBytes("ISO-8859-1");
		
		Spannable retval = colormebad.Colorize(textbytes);
		
		return retval;
		
	}
	
	public String RawProcess(byte[] data) throws UnsupportedEncodingException {
		if(data == null) {
			return "";
		}
		String tmp = new String(data,"ISO-8859-1");

		
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
			//Log.e("SERV","OPT: " + debug_input + "|RES: " + debug_output);
				
			//try {
				
				//service.sendData(resp);
				//instead send message
				Message sb = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,resp);
				//Bundle b = new Bundle();
				//b.putByteArray("THEDATA", resp);
				//sb.setData(b);
				
				/*synchronized(reportto) {
					while(reportto.hasMessages(BaardTERMService.MESSAGE_SENDDATA)) {
						try {
							reportto.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}*/
				
				//we have woken
				reportto.sendMessage(sb);
			//} catch (RemoteException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
			//send message to reportto now
			//Message msg = reportto.obtainMessage(BaardTERMService.MESSAGE_SENDDATA);
			//Bundle b = new Bundle();
			//b.putByteArray(new Integer(BaardTERMService.MESSAGE_SENDDATA).toString(), resp);
			//msg.setData(b);
			//reportto.sendMessage(msg);
				
				
		
			
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
				
				//reportto.sendEmptyMessage(StellarService.MESSAGE_STARTCOMPRESS);
				reportto.sendMessageAtFrontOfQueue(reportto.obtainMessage(StellarService.MESSAGE_STARTCOMPRESS));
				
				
				
				//service.
				//try {
					//service.beginCompression();
				//} catch (RemoteException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
			} else {
			
				Message sbm = reportto.obtainMessage(StellarService.MESSAGE_SENDOPTIONDATA,sub_r);
				//Bundle b = new Bundle();
				//b.putByteArray("THEDATA", sub_r);
				//sbm.setData(b);
				
				/*synchronized(reportto) {
					while(reportto.hasMessages(BaardTERMService.MESSAGE_SENDDATA)) {
						try {
							reportto.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}*/
				
				reportto.sendMessage(sbm);
				//Log.e("PROCESS","SLEEPING ON SERVICE.");
				//synchronized(service) {
					
					//service.wait();
				//}
				//Log.e("PROCESS","WOKE UP FROM SENDING DATA FROM SERVICE");
				
			
			}
		}
		
		//Matcher third_pass = goahead_reg.matcher(sub_match.replaceAll(""));
		goahead_match.reset(sub_match.replaceAll(""));
		//while(third_pass.find()) {
		//	Log.e("SERVICE","FOUND IAC GOAHEAD");
		//}
		
		//String textdata = sub_match.replaceAll("");
		
		//Matcher fourth_pass = tab_reg.matcher(third_pass.replaceAll(""));
		tab_match.reset(goahead_match.replaceAll(""));
		
		String textdata = tab_match.replaceAll("    ");
		
		//if(textdata.contains(new Character((char)0x09).toString())) {
		//	Log.e("SERVICE","FOUND TAB");
		//	textdata.replaceAll("\\x09", "    ");
		//}
		//Integer i = new Integer(0);
		//for(i=0;i<0x1F;i++) {
			
		//	if(textdata.contains(new Character((char) i.byteValue()).toString())) {
		//		Log.e("SERVICE","FOUND SPECIAL CHAR: " + Integer.toHexString(i));
		//	}
		//}
		
		
		byte[] textbytes = textdata.getBytes("ISO-8859-1");
		
		return textdata;
	}
	

}
