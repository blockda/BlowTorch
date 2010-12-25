package com.happygoatstudios.bt.window;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TextTree {
	
	public static final int MESSAGE_ADDTEXT = 0;
	Pattern colordata = Pattern.compile("\\x1B\\x5B.+m");
	Matcher colormatch = colordata.matcher("");
	
	Pattern newlinelookup = Pattern.compile("\n");
	Pattern tab = Pattern.compile(new String(new byte[]{0x09}));
	
	public Handler addTextHandler = null;
	
	private static Pattern oplookup = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?(([0-9]{1,2});)?([0-9]{1,2})m");
	private static Matcher op_match = oplookup.matcher("");
	
	private int MAX_LINES = 300;
	
	private String encoding = "ISO-8859-1";
	
	private int breakAt = 77;
	private boolean wordWrap = true;
	
	private int brokenLineCount = 0;
	
	private int totalbytes = 0;
	private boolean cullExtraneous = true;
	
	
	public int getBrokenLineCount() {
		return brokenLineCount;
	}

	public void setBrokenLineCount(int brokenLineCount) {
		this.brokenLineCount = brokenLineCount;
	}

	public TextTree() {
		mLines = new LinkedList<Line>();
		LinkedList<Unit> list = new LinkedList<Unit>();
		addTextHandler = new AddTextHandler();
	}
	
	
	public byte[] dumpToBytes() {
		ByteBuffer buf = ByteBuffer.allocate(totalbytes);
		Log.e("TREE","EXPORTING TREE:" + totalbytes + " bytes.");
		int written =0;
		//gotta do this from end to start.
		ListIterator<Line> i = mLines.listIterator(mLines.size());
		while(i.hasPrevious()) {
			Line l = i.previous();
			Iterator<Unit> iu = l.getData().iterator();
			while(iu.hasNext()) {
				Unit u = iu.next();
				if(u instanceof Text) {
					buf.put(((Text)u).bin);
					written += ((Text)u).bin.length;
				}
				if(u instanceof Color) {
					buf.put(((Color)u).bin);
					written += ((Color)u).bin.length;
				}
				if(u instanceof NewLine) {
					buf.put(NEWLINE);
					written += 1;
				}
				if(u instanceof Tab) {
					buf.put(TAB);
					written += 1;
				}
			}
		}
		
		int size = buf.position();
		Log.e("TREE","FINISHED EXPORTING:" + written + " bytes.");
		byte[] ret = new byte[size];
		buf.rewind();
		buf.get(ret,0,size);
		mLines.clear();
		appendLast = false;
		//buf.rewind();
		return ret;
	}
	
	public LinkedList<Line> getLines() {
		return mLines;
	}

	public void setLines(LinkedList<Line> mLines) {
		this.mLines = mLines;
	}
	
	private static LinkedList<Integer> getOperations(String input) {
		op_match.reset(input);
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		if(op_match.matches()) {
			String one = op_match.group(2);
			String two = op_match.group(4);
			String three = op_match.group(5);
		
			if(one != null) {
				try {
					tmp.add(Integer.parseInt(one));
				} catch (NumberFormatException e) {
					//we dont really care.
				}
			}
			if(two != null) {
				try {
					tmp.add(Integer.parseInt(two));
				} catch (NumberFormatException e) {
					//we dont really care.
				}
			}
			if(three != null) {
				try {
					tmp.add(Integer.parseInt(three));
				} catch (NumberFormatException e) {
					//we dont really care.
				}
			}
		}
		
		return tmp;
	}
	
	private static LinkedList<Integer> getOperationsFromBytes(byte[] in) {
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		int working = 0;
		int place = 1;
		for(int i=0;i<in.length;i++) {
			switch(in[i]) {
			case SEMI:
				//reset 
				tmp.addLast(new Integer(working));
				working = 0;
				place = 1;
				break;
			case m:
				tmp.addLast(new Integer(working));
				return tmp;
				//end
			case b0:
			case b1:
			case b2:
			case b3:
			case b4:
			case b5:
			case b6:
			case b7:
			case b8:
			case b9:
				working = working*place;
				place = place*10;
				working += getAsciiNumber(in[i]);
				break;
			case ESC:
			case BRACKET:
				break;
			default:
				break;
			}
		}
		
		return tmp;
	}
	
	private static int getAsciiNumber(byte b) {
		switch(b) {
		case b0:
			return 0;
		case b1:
			return 1;
		case b2:
			return 2;
		case b3:
			return 3;
		case b4:
			return 4;
		case b5:
			return 5;
		case b6:
			return 6;
		case b7:
			return 7;
		case b8:
			return 8;
		case b9:
			return 9;
		default:
			return 0;
			
		}
	}
	
	StringBuffer buf = new StringBuffer();
	StringBuffer linebuf = new StringBuffer();
	Pattern newline = Pattern.compile("\n");
	Matcher newline_ma = newline.matcher("");
	
	private static enum STATE {
		TEXT,
		ANSI,
		COLOR,
		NEWLINE,
		TAB
	}
	
	private final byte TAB = (byte)0x09;
	private final static byte ESC = (byte)0x1B;
	private final static byte BRACKET = (byte)0x5B;
	private final byte NEWLINE = (byte)0x0A;
	private final byte CARRIAGE = (byte)0x0D;
	private final static byte m = (byte)0x6D;
	private final static byte SEMI = (byte)0x3B;
	
	private final static byte b0 = (byte)0x30;
	private final static byte b1 = (byte)0x31;
	private final static byte b2 = (byte)0x32;
	private final static byte b3 = (byte)0x33;
	private final static byte b4 = (byte)0x34;
	private final static byte b5 = (byte)0x35;
	private final static byte b6 = (byte)0x36;
	private final static byte b7 = (byte)0x37;
	private final static byte b8 = (byte)0x38;
	private final static byte b9 = (byte)0x39;
	
	//more ansi escape sequences.
	private final byte A = (byte)0x41;
	private final byte B = (byte)0x42;
	private final byte C = (byte)0x43;
	private final byte D = (byte)0x44;
	private final byte E = (byte)0x45;
	private final byte F = (byte)0x46;
	private final byte G = (byte)0x47;
	private final byte H = (byte)0x48;
	private final byte J = (byte)0x4A;
	private final byte K = (byte)0x4B;
	private final byte S = (byte)0x53;
	private final byte T = (byte)0x54;
	private final byte f = (byte)0x66;
	private final byte s = (byte)0x73;
	private final byte u = (byte)0x75;
	
	//public void addBytes(byte[] data) {
		//synchronized(addTextHandler) {
			//while(addTextHandler.hasMessages(MESSAGE_ADDTEXT)) {
			//	try {
			//		addTextHandler.wait();
			//	} catch (InterruptedException e) {
			//		throw new RuntimeException(e);
			//	}
			//}
		//synchronized(mLines) {
			//addTextHandler.sendMessage(addTextHandler.obtainMessage(MESSAGE_ADDTEXT,data));
		//}
	//}
	static enum RUN {
		WHITESPACE,
		TEXT,
		NEW
	};
	boolean appendLast = false; //for marking when the addtext call has ended with a newline or not.
	private byte[] holdover = null;
	LinkedList<Integer> prev_color = null;
	Color lastColor = null;
	byte[] strag = null;
	public void addBytesImpl(byte[] data) throws UnsupportedEncodingException {
		//this actually shouldn't be too hard to do with just a for loop.
		STATE init = STATE.TEXT;
		
		LinkedList<Line> lines = new LinkedList<Line>();
		Line tmp = null;
		
		if(holdover != null) {
			//Log.e("TREE","HOLDOVER SEQUENCE:" + new String(holdover,"ISO-8859-1"));
			ByteBuffer b = ByteBuffer.allocate(holdover.length + data.length);
			b.put(holdover,0,holdover.length);
			b.put(data,0,data.length);
			b.rewind();
			data = b.array();
			holdover = null;
		}
		
		if(mLines.size() > 0) {
			Line analyze = mLines.get(0);
			
			//boolean appendLast = false;
			//Log.e("TREE","ANALYZING: " + deColorLine(analyze));
			
			for(Unit u : analyze.getData()) {
				if(u instanceof Text) {
					appendLast = true;
				} else if(u instanceof NewLine) {
					appendLast = false;
				}
			}
			//Log.e("TREE","APPEND LAST IS:" + appendLast);
		}
		
		LinkedList<Unit> ldata = null;
		if(appendLast) { //yay appendLast is over. now just look at the last line of the buffer, parse through it and find if the last text in it (not color) was a newline.
			//if(mLines.size() > 0) {
				tmp = mLines.remove(0); //dont worry kids, it'll be appended back.
				ldata = tmp.getData();
				//Log.e("TREE",">>>>>>>>>>>>>>APPENDING TO: " + deColorLine(tmp));
			//}
		} //else {
			//tmp = new Line();
		//}
		
		tmp = new Line();
		
		if(ldata != null) {
			tmp = new Line();
			tmp.setData(ldata);
			//Log.e("TREE","DATA STRIP OUT:" + deColorLine(tmp));
		} else {
			//Log.e("TREE","NOT ATTEMPTING APPENDING");
		}
		//StringBuffer sb = new StringBuffer();
		ByteBuffer sb = ByteBuffer.allocate(data.length);
		int textcount = 0;
		Text text = new Text();
		ByteBuffer cb = ByteBuffer.allocate(data.length);
		int iacount = 0;
		RUN runtype = RUN.NEW;
		
		//boolean endOnNewLine = false;
		for(int i=0;i<data.length;i++) {
			//Log.e("TREE","DATA PROCESSING LOOP: " + deColorLine(tmp));
			switch(data[i]) {
			case ESC:
				//Log.e("TREE","BEGIN ANSI ESCAPE");
				//end current text node.
				
				if(sb.position() > 0) {
					int size = sb.position();
					strag = new byte[size];
					sb.rewind();
					sb.get(strag,0,size);
					sb.rewind();
					switch(runtype) {
					case WHITESPACE:
						tmp.getData().addLast(new WhiteSpace(strag));
						break;
					case TEXT:
						tmp.getData().addLast(new Text(strag));
						break;
					default:
						break;
					}
					runtype = RUN.NEW;
					
					
				}
				//text.data = sb.toString();
				//sb.rewind();
				//tmp.getData().addLast(text);
				//text = new Text();
				
				if( (i+1) >= data.length) {
					holdover = new byte[]{ ESC };
					Log.e("TREE","APPEND DUE TO HOLDOVER EVENT: " + deColorLine(tmp));
					addLine(tmp);
					//tmp = new Line();
					//Log.e("TEXTTREE",getLastTwenty(false));
					
					//Log.e("TREE","HOLDOVER EVENENT, ESC ONLY");
					return;
				}
				//start ansi process sequence.
				if(data[i+1] != BRACKET) {
					//invalid ansi sequence.
				}
				cb.put(data[i]);
				cb.put(data[i+1]);
				
				boolean done = false;
				
				if( (i+2) >= data.length) {
					int tmpsize = cb.position();
					holdover = new byte[tmpsize];
					cb.rewind();
					cb.get(holdover,0,tmpsize);
					Log.e("TREE","APPEND DUE TO HOLDOVER EVENT: " + deColorLine(tmp));
					addLine(tmp);
					//Log.e("TEXTTREE",getLastTwenty(false));
					//Log.e("TREE","HOLDOVER EVENT, ESC AND [");
					return;
				}
				
				for(int j=i+2;j<data.length;j++) {
					//Log.e("TREE","ANSI ESCAPE ANALYSIS: " + new String(new byte[]{data[j]}));					
					
					switch(data[j]) {
					case m:
						//Log.e("TREE","STOPPING COLOR PARSE");
						done = true;
						cb.put(m);
						int cmdsize = cb.position();
						byte[] cmd = new byte[cmdsize];
						cb.rewind();
						cb.get(cmd,0,cmdsize);
						
						Color c = new Color(cmd);
						if(lastColor == null) {
							lastColor = c;
							tmp.getData().addLast(c);
						} else if(lastColor.equals(c)) {
							//if(strag != null) {
								//tmp.getData().removeLast();
								//sb.put(strag);	
							//}
							//dont add because the last color is the same.
							if(this.isCullExtraneous()) {
								//do nothing
							} else {
								tmp.getData().addLast(c);
							}
						} else {
							tmp.getData().addLast(c);
							lastColor = c;
						}
						
						cb.rewind();
						break;
					case A:
					case B:
					case C:
					case D:
					case E:
					case F:
					case G:
					case H:
					case J:
					case K:
					case S:
					case T:
					case f:
					case s:
					case u:
						cb.rewind();
						break;
					default:
						//Log.e("TREE","APPENDING FOR PARSE");
						cb.put(data[j]);
						break;
					}
					if(done) {
						i = j; //advance the cursor.
						break;
					}
				}
				if(cb.position() > 0) {
					int mtmpsz = cb.position();
					holdover = new byte[mtmpsz];
					cb.rewind();
					cb.get(holdover,0,mtmpsz);
					Log.e("TREE","APPEND DUE TO UNTERMINATED ANSI SEQUENCE:"  + deColorLine(tmp));
					addLine(tmp);
					//Log.e("TREE","WARNING: UNTERMINATED ASCII SEQUENCE: " + new String(holdover,encoding));
					return;
				}
				break;
			case TAB:
				//make new tab node.
				tmp.getData().addLast(new Tab());
				break;
			case NEWLINE:
				//Log.e("TREE","START APPEND DUE TO NEWLINE:"  + deColorLine(tmp));
				//Log.e("TREE","NEWLINE ADDING: " +sb.toString());
				if(sb.position() > 0) {
					int nsize = sb.position();
					byte[] txtdata = new byte[nsize];
					sb.rewind();
					sb.get(txtdata,0,nsize);
					//Log.e("TREE","APPEND TO LINE:"  + deColorLine(tmp));
					switch(runtype) {
					case WHITESPACE:
						tmp.getData().addLast(new WhiteSpace(txtdata));
						break;
					case TEXT:
						tmp.getData().addLast(new Text(txtdata));
						break;
					default:
						break;
					}
					runtype = RUN.NEW;
					sb.rewind();
				}
				//append the line as we do.
				NewLine nl = new NewLine();
				tmp.getData().addLast(nl);
				//Log.e("TREE","APPEND DUE TO NEWLINE:"  + deColorLine(tmp));
				addLine(tmp);
				tmp = new Line();
				break;
			case CARRIAGE:
				//dont append
				break;
			default:
				//put it in the buffer.
				if(Character.isWhitespace(data[i])) {
					//start whitespace run
					//Log.e("BYTE","FOUND WHITESPACE");
					switch(runtype) {
					case TEXT:
						int len = sb.position();
						byte[] cap = new byte[len];
						sb.rewind();
						sb.get(cap,0,len);
						tmp.mData.addLast(new Text(cap));
						runtype = RUN.WHITESPACE;
						sb.rewind();
						break;
					case NEW:
						runtype = RUN.WHITESPACE;
						break;
					default:
						break;
					}
				} else {
					switch(runtype) {
					case WHITESPACE:
						int len = sb.position();
						byte[] cap = new byte[len];
						sb.rewind();
						sb.get(cap,0,len);
						//Log.e("BYTE","ADDING WHITESPACE RUN");
						tmp.mData.addLast(new WhiteSpace(cap));
						runtype = RUN.TEXT;
						sb.rewind();
						break;
					case NEW:
						runtype = RUN.TEXT;
						break;
					default:
						break;
					}
				}
				sb.put(data[i]);
				//Log.e("TREE","BUFFER NOW:"+sb.toString()+"|");
				//endOnNewLine = false;
				
				break;
			}
		}
		//Log.e("TREE","BUFFER CONTAINS:" +sb.toString() + "||||");
		
		if(sb.position() > 0) {
			//Line last = new Line();
			//last.getData().addLast(new Text(sb.toString()));
			int fsize = sb.position();
			byte[] tmpb = new byte[fsize];
			sb.rewind();
			sb.get(tmpb,0,fsize);
			tmp.getData().addLast(new Text(tmpb));
			//Log.e("TEXTTREE",getLastTwenty(false));
			//Log.e("TEXTTREE","ADDED TEXT: LAST 20 LINES");
			//Log.e("TREE",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>NOT ENDED BY NEWLINE:" + deColorLine(tmp));
			
			
			sb.rewind();
		}
		
		if(tmp.getData().size() > 0) {
			addLine(tmp);
		}
		
		//if(lines.size() > 0) {
			//if we have more than 0 lines.
		//	for(Line line : lines) {
		//		mLines.addFirst(line); //this will reverse order them. so the first "line" in the list will be the last.
		//	}
		//}
		
		//prune if too many.
		//Log.e("TREE","BUFFER NOW:" + mLines.size() + " lines.");
		
		
		//Log.e("TEXTTREE",getLastTwenty(false));
		//Log.e("TEXTTREE","ADDED TEXT: LAST 20 LINES");
		//synchronized(addTextHandler) {
		//	addTextHandler.notify();
		//}
	}
	
	public void prune() {
		if(mLines.size() > MAX_LINES) {
			while(mLines.size() > MAX_LINES) {
				//Log.e("TREE","TRIMMING BUFFER");
				Line del = mLines.removeLast();
				brokenLineCount -= (1 + del.breaks);
				totalbytes -= del.bytes;
			}
		}
	}
	
	/*public void addText(String text) {
		//actually the first thing that needs to happen is we cut up the array on newlines.
		//String[] inputs = text.split("[\n\r]");
		newline_ma.reset(text);
		
		LinkedList<Line> lines = new LinkedList<Line>();
		
		boolean newlinefound = false;
		while(newline_ma.find()) {
			linebuf.setLength(0);
			newline_ma.appendReplacement(linebuf, "");
			newlinefound = true;
			//first thing is we have to split the string up on the colors. and set up a new line
			if(linebuf.length() > 0) {
				Line tmp = null;
				if(appendLast) {
					//Log.e("TEXTREEDATAINPUT","APPENDING:" + linebuf.toString());
					if(mLines.size() > 0) {
						tmp  = mLines.remove(0);	
					} else {
						tmp = new Line();
					}
					tmp.getData().add(tmp.getData().size(),new Text("|"));
					tmp.getData().addAll(tmp.getData().size(),makeLineFromData(linebuf.toString()));
					lines.add(tmp);
				} else {
					//Log.e("TEXTREEDATAINPUT","INSERTING:" + linebuf.toString());
					Line colorline = new Line();
					colorline.setData(makeLineFromData(linebuf.toString()));
					lines.add(colorline);
				}
			}
		}
		
		if(newlinefound) {
			//finish up
			linebuf.setLength(0);
			newline_ma.appendTail(linebuf);
			if(linebuf.length() > 0) {
				//Log.e("TEXTREEDATAINPUT","FINISHING:" + linebuf.toString());
				Line tmp = new Line();
				tmp.setData(makeLineFromData(linebuf.toString()));
				lines.add(tmp);
				appendLast = true;
			} else {
				appendLast = false;
			}
		} else {
			//this had no newlines. we should start the next add text loop at the end of this line.
			if(text.length() > 0) {
				//Log.e("TEXTREEDATAINPUT","ADDING END TEXT:" + linebuf.toString());
				
				Line lastline = new Line();
				lastline.setData(makeLineFromData(text));
				lines.add(lastline);
				appendLast = true;
			} else {
				appendLast = false;
			}
		}
		
		//now we are here at the end.
		if(lines.size() > 0) {
			//if we have more than 0 lines.
			for(Line line : lines) {
				mLines.addFirst(line); //this will reverse order them. so the first "line" in the list will be the last.
			}
		}
		
		//prune if too many.
		while(mLines.size() > MAX_LINES) {
			mLines.removeLast();
		}
		//TODO: finish the line loop here
		
		//Log.e("TEXTTREE",getLastTwenty(false));
		//Log.e("TEXTTREE","ADDED TEXT: LAST 20 LINES");
		
	}*/
	
	private class AddTextHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_ADDTEXT:
				try {
					addBytesImpl((byte[])msg.obj);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}
	
	private void addLine(Line l) {
		l.updateData();
		brokenLineCount += l.breaks + 1;
		totalbytes += l.bytes;
		//Log.e("TREE","A:" + deColorLine(l));
		mLines.add(0,l);
	}
	
	LinkedList<Line> mLines;
	Line pStart;
	Line pSend;
	
	public class Line {
		protected int totalchars;
		protected int charcount;
		protected int breaks;
		protected int bytes;
		
		public int getBreaks() {
			return breaks;
		}

		public void setBreaks(int breaks) {
			this.breaks = breaks;
		}

		protected LinkedList<Unit> mData;
		
		public Line() {
			mData = new LinkedList<Unit>();
			breaks =0;
		}
		
		public void updateData() {
			this.breaks = 0;
			this.charcount = 0;
			this.bytes = 0;
			stripBreaks();
			
			int charsinline = 0; //tracker for how many characters are in the line
			//int nonWhiteSpaceRun = 0; //tracker for how many characters have accumulated without whitespace
			boolean whiteSpaceFound = false;
			
			ListIterator<Unit> i = mData.listIterator(0);
			while(i.hasNext()) {
				Unit u = i.next();
				
				//check if it is whitespace
				if(u instanceof WhiteSpace) {
					if(wordWrap) {
						whiteSpaceFound = true;
					}
				}
				if(u instanceof Text) {
					//update charsinline
					charsinline += ((Text)u).charcount;
				}
				
				if(charsinline > breakAt) {
					int amount = charsinline - breakAt;
					if(wordWrap) {
						if(whiteSpaceFound) {
							//find the nearest whitespace and break.
							boolean found = false;
							//i.previous(); //advance back because we are on the right hand side of the unit that broke.
							while(!found && i.hasPrevious()) {
								Unit tmp = i.previous();
								if(tmp instanceof WhiteSpace) {
									i.next(); //get on the right side of the unit.
									i.add(new Break());
									this.breaks += 1;
									found = true;
								}
							}
							whiteSpaceFound = false;
							charsinline = 0;
						} else {
							//just break here and continue
							//if(amount > u.charcount) {
							//	Log.e("TREE","INVESTIGATE ME");
							//}
							int pos = u.charcount - (u.charcount-amount);
							pos += 1;
							pos -= 1;
							breakAt(i,u,pos,u.charcount);
							charsinline = 0;
						}
						
					//if the number of non whitespace characters is < breakAt, then we should go back and search for the whitespace
					//else, break in the middle.
					} else {
						//just break in the middle as we are not word wrapping
						charsinline = breakAt(i,u,amount,u.charcount);
					}
				}
				
			}
			
			//if we are here, then we should work backward through the list requesting sizes
			this.bytes = 0;
			while(i.hasPrevious()) {
				Unit tmp = i.previous();
				this.bytes += tmp.reportSize();
			}
			
		}
		
		public void stripBreaks() {
			Iterator<Unit> stripper = mData.iterator();
			while(stripper.hasNext()) {
				Unit tmp = stripper.next();
				if(tmp instanceof Break) {
					stripper.remove();
				}
			}
		}

		private void DebugCursorPosition(ListIterator<Unit> i,String where) {
			String debug = "Cursor Between: "+i.previousIndex()+":"+i.nextIndex();
			Log.e("TREE",where + " " + debug);
		}
		
		private void DebugCursorPosition2(String message) {
			StringBuilder b = new StringBuilder();
			Iterator<Unit> tmp = this.mData.iterator();
			while(tmp.hasNext()) {
				Unit u = tmp.next();
				if(u instanceof Text) {
					b.append(((Text)u).getString());
				}
			}
			Log.e("TREE",message + "[" + b.toString() + "]" );
		}

		private int breakAt(ListIterator<Unit> i, Unit u, int amount, int length) {
			int charsinline;
			boolean removed;
			if(amount == 0) {
				i.add(new Break());
				//advance so we don't process this for the original break checking.
				if(i.hasNext()) {
					i.next(); //advance the cursor so we don't go through and delete existing breaks.
				}
				breaks += 1;
				charsinline = 0;
				removed = true;
			} else {
				//i.remove();
				//debug.append()
				//Log.e("TREE","BREAKING LINE: l: " +length+ " a:" + amount);
				int start = length - amount;
				int end = length - (length-amount);
				
				String first = ((Text)u).data.substring(0, start);
				String second = ((Text)u).data.substring(start,start+end);
				i.set(new Text(first));
				i.add(new Break());
				i.add(new Text(second));
				//length = end;
				breaks += 1;
				
				
				removed = true;
				
				charsinline = end;
			}
			
			if(removed) {
				i.previous(); //queue the next pass to start with the unbroken end
				//Iterator<Unit> ja = this.mData.iterator();
				//StringBuilder b = new StringBuilder();
				//while(ja.hasNext()) {
				//	Unit tz = ja.next();
				//	if(tz instanceof Text) {
				//		b.append(((Text)tz).getString());
				//	}
				//	if(tz instanceof Break) {
				//		b.append("|");
				//	}
				//}
				//Log.e("TREE","BROKE LINE: " + b.toString());
				//break;
			}
			return charsinline;
		}

		public LinkedList<Unit> getData() {
			return mData;
		}
		
		public void setData(LinkedList<Unit> l) {
			mData = l;
			//need to parse this to make sure we report the correct data.
			charcount = 0;
			totalchars = 0;
			breaks = 0;
			for(Unit u : mData) {
				if(u instanceof Text) {
					charcount += u.charcount;
					totalchars += u.charcount;
				}
				if(u instanceof Color) {
					totalchars += u.charcount;
				}
			}
		}
		
		
		
	}
	
	public class Unit {
		protected int charcount;
		protected int bytecount;
		//protected int bytecount;
		
		public Unit() { charcount = 0; bytecount=0; }
		//	charcount = 0;
		//}
		
		//public Unit copy() { return null;}
		public int reportSize() { return 0; } //raw units have no size.
		
	}
	
	public interface UnitMizer {
		
	}
	
	public class Text extends Unit implements UnitMizer {
		protected String data;
		protected byte[] bin;
		public Text() {
			data = "";
			charcount = 0;
			bytecount = 0;
			bin = new byte[0];
		}
		
		public Text(String input) {
			data = input;
			this.charcount = data.length();
			try {
				bin = data.getBytes(encoding);
				this.bytecount = data.getBytes(encoding).length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Text(byte[] in) throws UnsupportedEncodingException {
			bin = in;
			data = new String(in,encoding);
			this.charcount = data.length();
			bytecount = bin.length;
		}

		public String getString() {
			return data;
		}
		
		public byte[] getBytes() {
			return bin;
		}
		
		public int reportSize() {
		
			return bin.length;
			
		}
		
		//public Text copy() {
			
			
		//}
		
		
	}
	
	private class Tab extends Unit implements UnitMizer {
		protected String data;
		
		public Tab() {
			data = new String(new byte[]{0x09});
			this.charcount = 1;
			this.bytecount = 1;
		}
		
		public int reportSize() {
			return 1;
		}
		
	}
	public class NewLine extends Unit implements UnitMizer {
		protected String data;
		
		public NewLine() {
			data = new String("\n");
			this.charcount = 1;
			this.bytecount = 1;
		}
		
		public int reportSize() {
			return 1;
		}
	}
	public class Color extends Unit implements UnitMizer {
		protected byte[] bin;
		protected String data;
		LinkedList<Integer> operations;
		
		public Color() {
			data = "[0m";
			this.charcount = data.length();
			operations.add(new Integer(0));
		}
		
		public Color(String input) {
			data = input;
			this.charcount = data.length();
			computeOperations(input);
			try {
				bytecount = data.getBytes(encoding).length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Color(String input,LinkedList<Integer> ops) {
			data = input;
			this.charcount = data.length();
			operations = ops; //will need to track this for actual memory usage.
			try {
				bytecount = data.getBytes(encoding).length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Color(byte[] input) {
			bin = input;
			bytecount = input.length;
			operations = getOperationsFromBytes(input);
			try {
				data = new String(bin,encoding);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		
		public String getData() {
			return data;
		}
		
		public void computeOperations(String input) {
			//
		}

		public LinkedList<Integer> getOperations() {
			return operations;
		}
		
		public boolean equals(Object o) {
			if(o == this) return true;
			if(!(o instanceof Color)) return false;
			Color c = (Color)o;
			if(c.bin.length != this.bin.length) {
				return false;
			}
			for(int i=0;i<this.bin.length;i++) {
				if(c.bin[i] != this.bin[i]) {
					return false;
				}
			}
			
			return true;
		}
		
		public int reportSize() {
			return bin.length;
		}
	}
	
	public class Break extends Unit implements UnitMizer {
		//int position;
		//this is merly a marker for us to know where breaks occur, the only space these take up is the handle for a variable in memory.
		public int reportSize() {
			return 0;
		}
		
	}
	
	public class WhiteSpace extends Text {
		//whitespace is esentially text.
		public WhiteSpace() {
			super();
		}
		
		public WhiteSpace(String pIn) {
			super(pIn);
		}
		
		public WhiteSpace(byte[] pIn) throws UnsupportedEncodingException {
			super(pIn);
		}
		
		public String getString() {
			return data;
		}
		
		public byte[] getBytes() {
			return bin;
		}
		
		public int reportSize() {
			return super.reportSize();
		}
	}
	
	public String getLastTwenty(boolean showcolor) {
		StringBuffer buf = new StringBuffer();
		Iterator<Line> i = mLines.iterator();
		int j = 0;
		while(j < 20) {
			if(i.hasNext()) {
				buf.insert(0,j + ":" + deColorLine((Line)i.next()));
				//buf.insert(0,"\n");
				//
			}
			j++;
		}
		return buf.toString();
	}
	
	public static String deColorLine(Line line) {
		StringBuffer buf = new StringBuffer();
		for(Unit u : line.getData()) {
			if(u instanceof Text) {
				buf.append(((Text)u).data);
			}
			if(u instanceof Color) {
				//Log.e("TEXTCOLOR","color encountered" + ((Color)u).data);
				//buf.append(((Color)u).data);
				//something special here.
				////buf.append("{|");
				//for(Integer i : ((Color)u).operations) {
				//	buf.append(Integer.toString(i)+"|");
				//}
				//buf.append("}");
			}
			if(u instanceof NewLine || u instanceof Break) {
				buf.append("\n");
			}
			
		}
		
		return buf.toString();
	}

	public void setMaxLines(int maxLines) {
		MAX_LINES = maxLines;
	}

	public void setLineBreakAt(Integer i) {
		breakAt = i;
		updateTree();
	}
	
	private void updateTree() {
		brokenLineCount = 0;
		totalbytes = 0;
		for(Line l : mLines) {
			l.updateData();
			totalbytes += l.bytes;
			brokenLineCount += (1 + l.breaks);
		}
	}

	public void setWordWrap(boolean wordWrap) {
		this.wordWrap = wordWrap;
	}

	public boolean isWordWrap() {
		return wordWrap;
	}

	public void setCullExtraneous(boolean cullExtraneous) {
		this.cullExtraneous = cullExtraneous;
	}

	public boolean isCullExtraneous() {
		return cullExtraneous;
	}
	
}
