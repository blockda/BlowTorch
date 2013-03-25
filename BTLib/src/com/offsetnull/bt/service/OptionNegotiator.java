package com.offsetnull.bt.service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import android.util.Log;

/** Helper class to the Processor. This object keeps track of the negotiation responses. */
public class OptionNegotiator {
	/** The default number of columns for NAWS. */
	private static final int DEFAULT_COLS = 80;
	/** The default number of rows for NAWS. */
	private static final int DEFAULT_ROWS = 21;
	/** Tracker for the configured number of columns for NAWS. */
	private int mColumns = DEFAULT_COLS;
	/** Tracker for the configured number of rows for NAWS. */
	private int mRows = DEFAULT_ROWS;
	/** Tracker for if naws has been negotiatated. */
	private boolean mIsNAWS = false;
	/** The termtype array of strings that will be iterated through for TTYPE negotiation. */
	private String[] mTermTypes = null;
	/** The termtype negotiation attempt number. */
	private int mTermTypeAttempt = 0;
	/** Selected termtype. */
	private String mTermType = null;
	/** Constructor.
	 * 
	 * @param ttype The package level configurable termtype option.
	 */
	public OptionNegotiator(final String ttype) {
		mTermType = ttype;
		mTermTypes = new String[] {mTermType, "ansi", "BlowTorch-256color", "UNKNOWN"};
		
	}
	byte IAC_WILL = (byte)0xFB; //251
	byte IAC_WONT = (byte)0xFC; //252
	byte IAC_DO = (byte)0xFD; //253
	byte IAC_DONT = (byte)0xFE; //254
	final byte COMPRESS2 = (byte)0x56; //86
	final byte GMCP = (byte)201;
	final byte SUPPRESS_GOAHEAD = (byte)0x03;
	public byte[] processCommand(byte first,byte second,byte third) {
	    	
			
			
			//byte SB = (byte)0xFA; //250 - subnegotiation start
			//byte SE = (byte)0xF0; //240 - subnegotiation start
			
			
			//final byte COMPRESS1 = (byte)0x55; //85
			//final byte ATCP_CUSTOM = (byte)0xC8; //200 -- ATCP protocol, http://www.ironrealms.com/rapture/manual/files/FeatATCP-txt.html
			//final byte AARD_CUSTOM = (byte)0x66; //102 -- Aardwolf custom, http://www.aardwolf.com/blog/2008/07/10/telnet-negotiation-control-mud-client-interaction/
			//final byte TERM_TYPE = (byte)0x18; //24
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
	    			break;
	    		case SUPPRESS_GOAHEAD:
	    			response = IAC_DO;
	    			break;
	    		case GMCP:
	    			if(mUseGMCP == true) {
	    				Log.e("GMCP","IAC WILL GMP RECIEVED, RESPONDING DO");
	    				response = IAC_DO;
	    			} else {
	    				response = IAC_DONT;
	    			}
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
	    			mIsNAWS=true;
	    			donenaws = false;
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
	    	
	    	//byte[] additionalcmd = getCommandSubneg(ret[1],ret[2]);
	    	
	    	/*if(additionalcmd != null) {
	    		//append subnegotiation onto stream.
	    		ByteBuffer buf = ByteBuffer.allocate(ret.length + additionalcmd.length);
	    		buf.put(ret,0,ret.length);
	    		buf.put(additionalcmd,0,additionalcmd.length);
	    		byte[] altret = buf.array();
	    		return altret;
	    	}*/
	    	
	    	return ret;
	    	
	    	
	    }

	
	public byte[] getSubnegotiationResponse(byte[] sequence) {
    	//first some asserts
    	if(sequence[0] != (byte)TC.IAC || sequence[1] != (byte)TC.SB || sequence[sequence.length-2] != (byte)TC.IAC || sequence[sequence.length-1] != (byte)TC.SE) {
    		//return null, not a valid suboption negotiation starting sequence.
    		return null;
    	}
    	
    	byte[] responsedata = null;
    	//Integer sw = new Integer((char)0xFF & sequence[2]); //fetch out the option number
    	switch(sequence[2]) {
    	case TC.TERM:
    		//get terminal response.
    		//String termtype = "UNKNOWN";
    		
    		
    		String termtype = mTermTypes[mTermTypeAttempt];
    		//Log.e("PROCESSOR","Sending terminal type: " + termtype);
    		try {
				responsedata = termtype.getBytes("ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
    		ByteBuffer buf = ByteBuffer.allocate(sequence.length + responsedata.length);
    		buf.put(sequence,0,sequence.length-3);
    		buf.put((byte)0x00); //fix the response to IS
    		buf.put(responsedata,0,responsedata.length);
    		buf.put(sequence,sequence.length-2,2);
    		
    		if(mTermTypeAttempt < 3) {
    			mTermTypeAttempt++;
    		} //else return UNKNOWN every time
    		return buf.array();
    		
    		//break;
    	case COMPRESS2:
    		//holy shit we have the compressor subnegotiation sequence start
    		//construct special return value to notify handler to switch to compression
    		//Log.e("PROCESSOR","COMPRESS2 ENCOUNTERED");
    		
    		byte[] compressstart = new byte[1];
    		compressstart[0] = TC.COMPRESS2;
    		return compressstart;
    	case GMCP:
    		return new byte[] { TC.GMCP };
    		//break;
    	default:
    	}
    	
    	
    	
    	
    	return null;
	}
	
    /*public byte[] getCommandSubneg(byte action,byte option) {
    	//get intval of action
    	Integer w = new Integer((int)(0xFF &action));
    	
    	switch(w) {
    	case 251: //WILL
    		Integer x = new Integer((int)(0xFF & option));
    		switch(x) {
    		case 31: //NAWS
    			//send naws subnegotiation sequence;
    			//construct sequence
    			ByteBuffer buf = ByteBuffer.allocate(9);
    			buf.put((byte)0xFF); //IAC
    			buf.put((byte)0xFA); //SB
    			buf.put((byte)0x1F); //NAWS
    			//buf.put((byte)0x00); //IS
    			//extract high byte from column
    			byte highCol = (byte)((0x0000FF00&columns)>>2);
    			byte lowCol = (byte)((0x000000FF&columns));
    			buf.put(highCol); //columns, high byte
    			buf.put(lowCol); //columns, low byte
    			
    			byte highRow = (byte)((0x0000FF00&rows)>>2);
    			byte lowRow = (byte)((0x000000FF&rows));
    			buf.put(highRow); //lines, high byte
    			buf.put(lowRow); //lines, low byte
    			
    			buf.put((byte)0xFF); //IAC
    			buf.put((byte)0xF0); //SE
    			
    			buf.rewind();
    			byte[] suboption = buf.array();
    			isNAWS = true;
    			
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
    }*/
	//private int old_row = 0;
	//private int old_col = 0;
	private boolean donenaws = false;
	private Boolean mUseGMCP = false;
	public void setColumns(int columns) {
		if(columns < 1) { return; }
		if(this.mColumns != columns) {
			donenaws = false;
		}
		this.mColumns = columns;
		
	}

	public int getColumns() {
		return mColumns;
	}

	public void setRows(int rows) {
		if(rows < 1) { return; }
		if(this.mRows != rows) {
			donenaws = false;
		}
		this.mRows = rows;
		
	}

	public int getRows() {
		return mRows;
	}
	
	public byte[] getNawsString() {
		if(!mIsNAWS) { return null;}
		if(donenaws) { return null;}
		//Log.e("OPT","WHO LET THE NAWS OUT");
		ByteBuffer buf = ByteBuffer.allocate(9);
		buf.put((byte)0xFF); //IAC
		buf.put((byte)0xFA); //SB
		buf.put((byte)0x1F); //NAWS
		//buf.put((byte)0x00); //IS
		//extract high byte from column
		byte highCol = (byte)((0x0000FF00&mColumns)>>2);
		byte lowCol = (byte)((0x000000FF&mColumns));
		buf.put(highCol); //columns, high byte
		buf.put(lowCol); //columns, low byte
		
		byte highRow = (byte)((0x0000FF00&mRows)>>2);
		byte lowRow = (byte)((0x000000FF&mRows));
		buf.put(highRow); //lines, high byte
		buf.put(lowRow); //lines, low byte
		
		buf.put((byte)0xFF); //IAC
		buf.put((byte)0xF0); //SE
		
		buf.rewind();
		byte[] suboption = buf.array();
		
		donenaws = true; //only send naws once per valid session.
		//send the data back.
		return suboption;
	}


	public void reset() {
		mTermTypeAttempt = 0;		
	}


	public void setUseGMCP(Boolean useGMCP) {
		mUseGMCP  = useGMCP;
	}
}
