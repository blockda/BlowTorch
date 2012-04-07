package com.offsetnull.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class TC {
	final static byte IAC = (byte) 0xFF; //255
	
	final static byte MXP = (byte)0x5B;
	final static byte WILL = (byte) 0xFB; //251
	final static byte WONT = (byte) 0xFC; //252
	final static byte DO = (byte) 0xFD; //253
	final static byte DONT = (byte) 0xFE; //254
	
	final static byte SB = (byte) 0xFA; //250 - subnegotiation start
	final static byte SE = (byte) 0xF0; //240 - subnegotiation start
	
	final static byte SEND = 0x01;
	final static byte IS = 0x00;
	
	final static byte COMPRESS2 = 0x56; //86
	final static byte COMPRESS1 = 0x55; //85
	final static byte ATCP_CUSTOM = (byte) 0xC8; //200 -- ATCP protocol, http://www.ironrealms.com/rapture/manual/files/FeatATCP-txt.html
	final static byte AARD_CUSTOM = 0x66; //102 -- Aardwolf custom, http://www.aardwolf.com/blog/2008/07/10/telnet-negotiation-control-mud-client-interaction/
	final static byte TERM = 0x18; //24
	final static byte NAWS = 0x1F; //31 -- NAWS, negotiate window size
	final static byte ECHO = 0x01;
	final static byte TTYPE = 0x18;
	final static byte GMCP = (byte)201;
	
	public static String getByteName(byte in) {
		String output = "";
		switch(in) {
		case TC.IAC:
			output = "IAC";
			break;
		case TC.DONT:
			output = "DONT";
			break;
		case TC.DO:
			output = "DO";
			break;
		case TC.WONT:
			output = "WONT";
			break;
		case TC.WILL:
			output = "WILL";
			break;
		case TC.SB:
			output = "SB";
			break;
		case TC.SE:
			output = "SE";
			break;
		//case 0:
			//output = "XMITBIN";
		case TC.COMPRESS2:
			output = "COMPRESS2";
			break;
		case TC.COMPRESS1:
			output = "COMPRESS1";
			break;
		case TC.NAWS:
			output = "NAWS";
			break;
		case TC.TTYPE:
			output = "TERM";
			break;
		case TC.MXP:
			output="MXP";
			break;
		case TC.ECHO:
			output = "ECHO";
			break;
			
		//many more types.
		
		default:
			output = Integer.toString(0x000000FF&in);
			break;
		}
		return output;
	}
	
	public static String decodeSUB(byte[] in) {
		String ret = "";
		
		int datalen = in.length-4; //SB ACTION DATA IAC SB
		
		if(datalen > 0) {
			ret += "IAC SB ";
			ret += getByteName(in[2]) + " ";
			switch(in[2]) {
			case TERM:
				ByteBuffer tmp = ByteBuffer.wrap(in);
				if(in.length > 6) {
					byte[] type = new byte[in.length - 6];
					tmp.rewind();
					tmp.position(4);
					tmp.get(type,0,in.length-6);
					ret += "IS " + new String(type) + " IAC SE";
					return ret;
				} else {
					ret += "SEND IAC SE";
					return ret;
				}
			case GMCP:
				ByteBuffer t = ByteBuffer.wrap(in);
				if(in.length > 5) { //has to be if valid gmcp message IAC SB GMCP <lotsofdata> IAC SE
					byte[] data = new byte[in.length -5];
					t.rewind();
					t.position(3);
					t.get(data,0,in.length-5);
					try {
						ret += new String(data,"UTF-8");
						ret += " IAC SE";
						return ret;
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case NAWS:
				ret +=  Integer.toString(0x0000FF&in[3]) + " " + 
						    Integer.toString(0x000000FF&in[4]) + " " +
						   Integer.toString(0x000000FF&in[5]) + " " +
						  Integer.toString(0x000000FF&in[6]) + " IAC SE";
				return ret;
							
			default:
				for(int j=0;j<datalen;j++) {
					byte[] b = { in[2+j] };
					ret += new String(b);
				}
				break;
			}
			ret += " IAC SE";
			
		} else {
			//standard response
			for(byte b: in) {
				ret += getByteName(b) + " ";
			}
			ret = ret.substring(0,ret.length());
		}
		
		return ret;
	}
	
	public static String decodeIAC(byte[] in) {
		String ret = "";
		for(int i=0;i<in.length;i++) {
			if(in[i] == IAC && in[i+1] == SB) {
				ByteBuffer b = ByteBuffer.wrap(in);
				byte[] len = new byte[in.length-i];
				b.position(i);
				b.get(len, 0, in.length-i);
				ret += decodeSUB(len);
				return ret;
			} else {
				ret += getByteName(in[i]) +" ";
			}
		}
		
		return ret.substring(0, ret.length()-1);
	}

}
