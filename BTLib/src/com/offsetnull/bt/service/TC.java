package com.offsetnull.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/** Utility class that maps a bunch of telnet related constants. */
public final class TC {
	/** IAC (0xFF - 255) Interpret as command. */
	public static final byte IAC = (byte) 0xFF;
	
	/** MXP 0x5B Test byte for MXP.*/
	public static final byte MXP = (byte) 0x5B;
	
	/** Telnet WILL Constant. */
	public static final byte WILL = (byte) 0xFB;
	
	/** Telnet WONT Constant. */
	public static final byte WONT = (byte) 0xFC;
	
	/** Telnet DO Constant. */
	public static final byte DO = (byte) 0xFD;
	
	/** Telnet DONT Constant. */
	public static final byte DONT = (byte) 0xFE;
	
	/** Telnet SUB start constant. */
	public static final byte SB = (byte) 0xFA; //250 - subnegotiation start
	
	/** Telnet SUB end constant. */
	public static final byte SE = (byte) 0xF0; //240 - subnegotiation start
	
	/** Telnet SEND constant. */
	public static final byte SEND = 0x01;
	
	/** Telnet IS constant. */
	public static final byte IS = 0x00;
	
	/** MCCP2 constant.*/
	public static final byte COMPRESS2 = 0x56; //86
	
	/** MCCP1 constant. */
	public static final byte COMPRESS1 = 0x55; //85
	
	/** test constant for aard specific ATCP. */
	public static final byte ATCP_CUSTOM = (byte) 0xC8; //200 -- ATCP protocol, http://www.ironrealms.com/rapture/manual/files/FeatATCP-txt.html
	
	/** test constant for aard custom protocol. */
	public static final byte AARD_CUSTOM = 0x66; //102 -- Aardwolf custom, http://www.aardwolf.com/blog/2008/07/10/telnet-negotiation-control-mud-client-interaction/
	
	/** Telnet TERM constant. */
	public static final byte TERM = 0x18; //24
	
	/** Telnet NAWS constant. */
	public static final byte NAWS = 0x1F; //31 -- NAWS, negotiate window size
	
	/** Telnet ECHO constant. */
	public static final byte ECHO = 0x01;
	
	/** Telnet TTYPE constant. */
	public static final byte TTYPE = 0x18;
	
	/** Telnet GMCP constant. */
	public static final byte GMCP = (byte) 201;
	
	/** LSB mask for a 32 bit int. */
	public static final int LSB_MASK = 0x000000FF;
	
	/** NVT character BRK. */
	public static final byte BREAK = (byte) 243;
	/** The A0 function. */
	public static final byte AO = (byte) 245; 
	/** The AYT function. */
	public static final byte AYT = (byte) 246;
	/** The EC function. */
	public static final byte EC = (byte) 247; 
	/** The EL function. */
	public static final byte EL = (byte) 248; 
	/** The CARRIAGE RETURN byte. */
	public static final byte CARRIAGE = (byte) 0x0D;
	/** The GOAHEAD byte. */
	public static final byte GOAHEAD = (byte) 0xF9;
	/** The IP Byte. */
	public static final byte IP = (byte) 0xF4;
	/** The BELL byte. */
	public static final byte BELL = (byte) 0x07;
	/** The Suppress goahead byte. */
	public static final byte SUPPRESS_GOAHEAD = (byte) 0x03;
	
	/** Private constructor. */
	private TC() {
		
	}
	
	/** Gets a string value for a given byte.
	 * 
	 * @param in byte value of the telnet constant you want a string for.
	 * @return the string description of the telnet constant (IAC, DONT, etc).
	 */
	public static String getByteName(final byte in) {
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
			output = "MXP";
			break;
		case TC.ECHO:
			output = "ECHO";
			break;
			
		//many more types.
		
		default:
			output = Integer.toString(LSB_MASK & in);
			break;
		}
		return output;
	}
	
	/** Decodes a subnegotation.
	 * 
	 * @param in byte array of the complete subnegotiation.
	 * @return the string representation of the subnegotiation (IAC SB TTYPE ... etc)
	 */
	public static String decodeSUB(final byte[] in) {
		String ret = "";
		
		int datalen = in.length - 4; //SB ACTION DATA IAC SB
		
		if (datalen > 0) {
			ret += "IAC SB ";
			ret += getByteName(in[2]) + " ";
			switch(in[2]) {
			case TERM:
				ByteBuffer tmp = ByteBuffer.wrap(in);
				if (in.length > 6) {
					byte[] type = new byte[in.length - 6];
					tmp.rewind();
					tmp.position(4);
					tmp.get(type, 0, in.length - 6);
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
					t.get(data,0,in.length - 5);
					try {
						ret += new String(data, "UTF-8");
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
				for (int j = 0; j < datalen; j++) {
					byte[] b = {in[2 + j] };
					ret += new String(b);
				}
				break;
			}
			ret += " IAC SE";
			
		} else {
			//standard response
			for (byte b: in) {
				ret += getByteName(b) + " ";
			}
			ret = ret.substring(0, ret.length());
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
