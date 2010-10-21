package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import android.os.Handler;


public class OptionNegotiator {
	
	private Handler dispatcher;

	public OptionNegotiator(Handler idispatcher) {
		//not really much to initialize, this class just returns a response to an option
		dispatcher = idispatcher;
	}
	
	 public byte[] processCommand(byte first,byte second,byte third) {
	    	
			byte IAC_WILL = (byte)0xFB; //251
			byte IAC_WONT = (byte)0xFC; //252
			byte IAC_DO = (byte)0xFD; //253
			byte IAC_DONT = (byte)0xFE; //254
			
			byte SB = (byte)0xFA; //250 - subnegotiation start
			byte SE = (byte)0xF0; //240 - subnegotiation start
			
			final byte COMPRESS2 = (byte)0x56; //86
			final byte COMPRESS1 = (byte)0x55; //85
			final byte ATCP_CUSTOM = (byte)0xC8; //200 -- ATCP protocol, http://www.ironrealms.com/rapture/manual/files/FeatATCP-txt.html
			final byte AARD_CUSTOM = (byte)0x66; //102 -- Aardwolf custom, http://www.aardwolf.com/blog/2008/07/10/telnet-negotiation-control-mud-client-interaction/
			final byte TERM_TYPE = (byte)0x18; //24
			final byte NAWS_TYPE = (byte)0x1F; //31 -- NAWS, negotiate window size
			
	    	byte[] ret = new byte[3];
	    	
	    	//first byte should always be 255.
	    	if(first != (byte)0xFF) {
	    		return null;
	    	}
	    	
	    	byte response = 0x00;
	    	
	    	if(second == IAC_WILL) {
	    		switch(third) {
	    		case COMPRESS2:
	    			response = IAC_DO;
	    			//TODO: turn compression back on.
	    			dispatcher.sendEmptyMessage(StellarService.MESSAGE_COMPRESSIONREQUESTED);
	    			//response = IAC_DONT;
	    			break;
	   			
	    		default:
	    			response = IAC_DONT;
	    		}
	    	}
	    	
	    	if(second == IAC_DO) {
	    		switch(third) {
	    		case COMPRESS2:
	    			response = IAC_WONT;
	    			break;
	    		case NAWS_TYPE:
	    			response = IAC_WILL;
	    			break; 
	    		case TC.TERM:
	    			response = IAC_WILL;
	    			break;
	    		default:
	    			response = IAC_WONT;
	    		}
	    	}
	    	
	    	if(second == IAC_WONT) {
	    		response = IAC_DONT;
	    	}
	    	
	    	if(second == IAC_DONT) {
	    		response = IAC_WONT;
	    	}
	    		
	    	//construct return value
	    	ret[0] = first;
	    	ret[1] = response;
	    	ret[2] = third;
	    	
	    	byte[] additionalcmd = getCommandSubneg(ret[1],ret[2]);
	    	
	    	if(additionalcmd != null) {
	    		//append subnegotiation onto stream.
	    		ByteBuffer buf = ByteBuffer.allocate(ret.length + additionalcmd.length);
	    		buf.put(ret,0,ret.length);
	    		buf.put(additionalcmd,0,additionalcmd.length);
	    		byte[] altret = buf.array();
	    		return altret;
	    	}
	    	
	    	return ret;
	    	
	    	
	    }

	
	public byte[] getSubnegotiationResponse(byte[] sequence) {
    	//first some asserts
    	if(sequence[0] != TC.IAC || sequence[1] != TC.SB || sequence[sequence.length-2] != TC.IAC || sequence[sequence.length-1] != TC.SE) {
    		//return null, not a valid suboption negotiation starting sequence.
    		return null;
    	}
    	
    	byte[] responsedata = null;
    	//Integer sw = new Integer((char)0xFF & sequence[2]); //fetch out the option number
    	switch(sequence[2]) {
    	case TC.TERM:
    		//get terminal response.
    		//String termtype = "UNKNOWN";
    		String termtype = "BlowTorch";
    		try {
				responsedata = termtype.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		ByteBuffer buf = ByteBuffer.allocate(sequence.length + responsedata.length);
    		buf.put(sequence,0,sequence.length-3);
    		buf.put((byte)0x00); //fix the response to IS
    		buf.put(responsedata,0,responsedata.length);
    		buf.put(sequence,sequence.length-2,2);
    		
    		return buf.array();
    		//break;
    	case TC.COMPRESS2:
    		//holy shit we have the compressor subnegotiation sequence start
    		//construct special return value to notify handler to switch to compression
    		byte[] compressstart = new byte[1];
    		compressstart[0] = TC.COMPRESS2;
    		return compressstart;
    	default:
    	}
    	
    	
    	
    	
    	return null;
	}
	
    public byte[] getCommandSubneg(byte action,byte option) {
    	//get intval of action
    	Integer w = new Integer((int)(0xFF &action));
    	
    	switch(w) {
    	case 251: //WILL
    		Integer x = new Integer((int)(0xFF & option));
    		switch(x) {
    		case 31: //NAWS
    			//send naws subnegotiation sequence;
    			//construct sequence
    			ByteBuffer buf = ByteBuffer.allocate(10);
    			buf.put((byte)0xFF); //IAC
    			buf.put((byte)0xFA); //SB
    			buf.put((byte)0x1F); //NAWS
    			buf.put((byte)0x00); //IS
    			buf.put((byte)0x00); //columns, high byte
    			buf.put((byte)0x80); //columns, low byte
    			buf.put((byte)0x00); //lines, high byte
    			buf.put((byte)0x24); //lines, low byte
    			buf.put((byte)0xFF); //IAC
    			buf.put((byte)0xF0); //SE
    			
    			buf.rewind();
    			byte[] suboption = buf.array();
    			
    			//send the data back.
    			return suboption;
    		default:
    			break;
    		}
    		break;
    	default:
    		break;
    	
    	}
    	return null;
    }
}
