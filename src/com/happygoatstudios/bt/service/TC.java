package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;

import android.util.Log;

public class TC {
	/*final static byte IAC = (byte)0xFF; //255
	
	
	final static byte WILL = (byte)0xFB; //251
	final static byte WONT = (byte)0xFC; //252
	final static byte DO = (byte)0xFD; //253
	final static byte DONT = (byte)0xFE; //254
	
	final static byte SB = (byte)0xFA; //250 - subnegotiation start
	final static byte SE = (byte)0xF0; //240 - subnegotiation start
	
	final static byte SEND = (byte)0x01;
	final static byte IS = (byte)0x00;
	
	final static byte COMPRESS2 = (byte)0x56; //86
	final static byte COMPRESS1 = (byte)0x55; //85
	final static byte ATCP_CUSTOM = (byte)0xC8; //200 -- ATCP protocol, http://www.ironrealms.com/rapture/manual/files/FeatATCP-txt.html
	final static byte AARD_CUSTOM = (byte)0x66; //102 -- Aardwolf custom, http://www.aardwolf.com/blog/2008/07/10/telnet-negotiation-control-mud-client-interaction/
	final static byte TERM = (byte)0x18; //24
	final static byte NAWS = (byte)0x1F; //31 -- NAWS, negotiate window size*/
	
	final static int IAC = 0xFF; //255
	
	
	final static int WILL = 0xFB; //251
	final static int WONT = 0xFC; //252
	final static int DO = 0xFD; //253
	final static int DONT = 0xFE; //254
	
	final static int SB = 0xFA; //250 - subnegotiation start
	final static int SE = 0xF0; //240 - subnegotiation start
	
	final static int SEND = 0x01;
	final static int IS = 0x00;
	
	final static int COMPRESS2 = 0x56; //86
	final static int COMPRESS1 = 0x55; //85
	final static int ATCP_CUSTOM = 0xC8; //200 -- ATCP protocol, http://www.ironrealms.com/rapture/manual/files/FeatATCP-txt.html
	final static int AARD_CUSTOM = 0x66; //102 -- Aardwolf custom, http://www.aardwolf.com/blog/2008/07/10/telnet-negotiation-control-mud-client-interaction/
	final static int TERM = 0x18; //24
	final static int NAWS = 0x1F; //31 -- NAWS, negotiate window size
	
	final static int TTYPE = 0x18;
	
	public static String getEncodedString(byte[] sequence) {
		
		String retval = "";
		if(sequence.length < 4) {
			for(int i=0;i<sequence.length;i++) {
				retval = retval.concat(getByteName(sequence[i]) + " ");
			}
		} else {
			//for the suboption return string we need to not encode all the data.
			for(int i=0;i<4;i++) {
				retval = retval.concat(getByteName(sequence[i]) + " ");
			}
			for(int i=4;i<sequence.length-2;i++) {
				retval = retval.concat(new Character((char)sequence[i]).toString());
			}
			retval = retval.concat(" ");
			for(int i=sequence.length-2;i<sequence.length;i++) {
				retval = retval.concat(getByteName(sequence[i]) + " ");
			}
		}
		
		return retval;
	}
	
	public static String getByteName(byte in) {
		String output = "";
		//convert to int and make it unsigned
		Integer iin = new Integer((char)0xFF & in);
		//Log.e("CHECKING","INT VALUE" + iin);
		switch(iin.intValue()) {
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
		case 86:
			output = "COMPRESS2";
			break;
		case 85:
			output = "COMPRESS1";
			break;
		case 31:
			output = "NAWS";
			break;
		case TC.TTYPE:
			output = "TTYPE";
		case 1:
			output = "ECHO";
			
		//many more types.
		
		default:
			output = "";
			break;
		}
		return output;
	}
	
	public static String decodeInt(String in,String encoding) {
		StringBuffer retval = new StringBuffer();
		
		byte[] letters = new byte[0];
		
		try {
			letters = in.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0;i<letters.length;i++) {
			int ival = (int)letters[i];
			String lookup = getByteName((byte) ((byte)0x000000FF&ival));
			//if(lookup.equals("")) {
				//append the normal
				//String str = Integer.toHexString(0x000000FF&ival)+"|";
				String str = Integer.toString(0x000000FF&ival)+"|";
				retval.append(str);
			//} else {
				//retval.append(lookup + "|");
			//}
			
		}
		
		if(retval == null) {
			return null;
		} else {
			return retval.toString();
		}
	}

}
