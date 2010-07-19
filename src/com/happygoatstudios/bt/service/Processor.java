package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Spannable;
import android.util.Log;

import com.happygoatstudios.bt.service.*;

public class Processor {
	
	Handler reportto = null;
	Colorizer colormebad = new Colorizer();
	OptionNegotiator opthandler = new OptionNegotiator();
	IStellarService.Stub service = null;
	
	public Processor(Handler useme,IStellarService.Stub theserv) {
		//not really much to do here, this will be a static class thing
		reportto = useme;
		service = theserv;
	}
	
	Pattern iac_cmd_reg = Pattern.compile("\\xFF([\\xFB-\\xFE])(.{1})");
	//Pattern iac_cmd_reg = Pattern.compile("\\xFF.*");
	Pattern subnego_reg = Pattern.compile("\\xFF\\xFA(.{1})(.*)\\xFF\\xF0");
	
	Pattern normal_reg = Pattern.compile("[\\x00-\\x7F]*"); //match any 7-bit ascii character, zero or more times
	
	public Spannable DoProcess(byte[] data) throws UnsupportedEncodingException {
		


		
		String tmp = new String(data,"ISO-8859-1");

		
		Matcher iac_match = iac_cmd_reg.matcher(tmp);
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
		
		String secondpass = iac_match.replaceAll("");
		
		Matcher sub_match = subnego_reg.matcher(secondpass);
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
		
		
		String textdata = sub_match.replaceAll("");
		
		byte[] textbytes = textdata.getBytes("ISO-8859-1");
		
		Spannable retval = colormebad.Colorize(textbytes);
		
		return retval;
		
	}
	
	public String RawProcess(byte[] data) throws UnsupportedEncodingException {
		if(data == null) {
			return "";
		}
		String tmp = new String(data,"ISO-8859-1");

		
		Matcher iac_match = iac_cmd_reg.matcher(tmp);
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
		
		String secondpass = iac_match.replaceAll("");
		
		Matcher sub_match = subnego_reg.matcher(secondpass);
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
		
		
		String textdata = sub_match.replaceAll("");
		
		byte[] textbytes = textdata.getBytes("ISO-8859-1");
		
		return textdata;
	}
	

}
