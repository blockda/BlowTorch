package com.happygoatstudios.bt.window.ttree;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class TextTree {
	
	Pattern colordata = Pattern.compile("\\x1B\\x5B.+m");
	Matcher colormatch = colordata.matcher("");
	
	Pattern newlinelookup = Pattern.compile("\n");
	Pattern tab = Pattern.compile(new String(new byte[]{0x09}));
	
	private static Pattern oplookup = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?(([0-9]{1,2});)?([0-9]{1,2})m");
	private static Matcher op_match = oplookup.matcher("");
	
	private int MAX_LINES = 20;
	
	private String encoding = "ISO-8859-1";
	
	public TextTree() {
		mLines = new LinkedList<Line>();
		LinkedList<Unit> list = new LinkedList<Unit>();
		
	}
	
	public LinkedList<Line> getLines() {
		return mLines;
	}

	public void setLines(LinkedList<Line> mLines) {
		this.mLines = mLines;
	}

	private LinkedList<Unit> makeLineFromData(String input) {
		LinkedList<Unit> tmp = new LinkedList<Unit>();
		colormatch.reset(input);
		buf.setLength(0);
		boolean colorfound = false;
		while(colormatch.find()) {
			colorfound = true;
			buf.setLength(0);
			colormatch.appendReplacement(buf, "");
		
			if(buf.length() > 0) {
				//if there is text, make and attach the text node first
				tmp.addLast(new Text(buf.toString()));
			} 
			//then handle the color.
			Color c = new Color(colormatch.group(0),getOperations(colormatch.group(0)));
			tmp.addLast(c);
		}
		if(colorfound) {
			//finish up
			buf.setLength(0);
			colormatch.appendTail(buf);
			if(buf.length() > 0) {
				tmp.addLast(new Text(buf.toString()));
			} 
		} else {
			//there is no color info in this line
			tmp.addLast(new Text(input));
		}
		return tmp;
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
	private final byte ESC = (byte)0x1B;
	private final byte BRACKET = (byte)0x5B;
	private final byte NEWLINE = (byte)0x0A;
	private final byte CARRIAGE = (byte)0x0D;
	private final byte m = (byte)0x6D;
	
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
	
	boolean appendLast = false; //for marking when the addtext call has ended with a newline or not.
	private byte[] holdover = null;
	LinkedList<Integer> prev_color = null;
	public void addBytes(byte[] data) throws UnsupportedEncodingException {
		//this actually shouldn't be too hard to do with just a for loop.
		STATE init = STATE.TEXT;
		
		LinkedList<Line> lines = new LinkedList<Line>();
		Line tmp = null;
		
		if(holdover != null) {
			Log.e("TREE","HOLDOVER SEQUENCE:" + new String(holdover,"ISO-8859-1"));
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
		
		if(appendLast) { //yay appendLast is over. now just look at the last line of the buffer, parse through it and find if the last text in it (not color) was a newline.
			//if(mLines.size() > 0) {
				tmp = mLines.remove(0); //dont worry kids, it'll be appended back.
				//Log.e("TREE",">>>>>>>>>>>>>>APPENDING TO: " + deColorLine(tmp));
			//}
		} else {
			tmp = new Line();
		}
		
		//StringBuffer sb = new StringBuffer();
		ByteBuffer sb = ByteBuffer.allocate(data.length);
		int textcount = 0;
		Text text = new Text();
		ByteBuffer cb = ByteBuffer.allocate(data.length);
		int iacount = 0;
		//boolean endOnNewLine = false;
		for(int i=0;i<data.length;i++) {
			switch(data[i]) {
			case ESC:
				//Log.e("TREE","BEGIN ANSI ESCAPE");
				//end current text node.
				if(sb.position() > 0) {
					int size = sb.position();
					byte[] strag = new byte[size];
					sb.rewind();
					sb.get(strag,0,size);
					sb.rewind();
					tmp.getData().addLast(new Text(strag));
					
					
				}
				//text.data = sb.toString();
				//sb.rewind();
				//tmp.getData().addLast(text);
				//text = new Text();
				
				if( (i+1) >= data.length) {
					holdover = new byte[]{ ESC };
					mLines.add(0,tmp);
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
					mLines.add(0,tmp);
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
						Color c = new Color(cb.toString(),getOperations(new String(cmd,encoding)));
						tmp.getData().addLast(c);
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
					mLines.add(0,tmp);
					Log.e("TREE","WARNING: UNTERMINATED ASCII SEQUENCE: " + new String(holdover,encoding));
					return;
				}
				break;
			case TAB:
				//make new tab node.
				tmp.getData().addLast(new Tab());
				break;
			case NEWLINE:
				//Log.e("TREE","NEWLINE ADDING: " +sb.toString());
				if(sb.position() > 0) {
					int nsize = sb.position();
					byte[] txtdata = new byte[nsize];
					sb.rewind();
					sb.get(txtdata,0,nsize);
					tmp.getData().addLast(new Text(txtdata));
					sb.rewind();
				}
				//append the line as we do.
				NewLine nl = new NewLine();
				tmp.getData().addLast(nl);
				mLines.add(0,tmp);
				tmp = new Line();
				break;
			case CARRIAGE:
				//dont append
				break;
			default:
				//put it in the buffer.
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
			Log.e("TREE",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>NOT ENDED BY NEWLINE:" +((Text)tmp.getData().getLast()).data + " |||||");
			
			mLines.add(0,tmp);
			sb.rewind();
		}
		
		//if(lines.size() > 0) {
			//if we have more than 0 lines.
		//	for(Line line : lines) {
		//		mLines.addFirst(line); //this will reverse order them. so the first "line" in the list will be the last.
		//	}
		//}
		
		//prune if too many.
		while(mLines.size() > MAX_LINES) {
			mLines.removeLast();
		}
		
		Log.e("TEXTTREE",getLastTwenty(false));
		Log.e("TEXTTREE","ADDED TEXT: LAST 20 LINES");
	}
	
	public void addText(String text) {
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
		
	}
	
	LinkedList<Line> mLines;
	Line pStart;
	Line pSend;
	
	public class Line {
		protected int totalchars;
		protected int charcount;
		protected int breaks;
		
		protected LinkedList<Unit> mData;
		
		public Line() {
			mData = new LinkedList<Unit>();
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
		//protected int bytecount;
		
		public Unit() {
			charcount = 0;
		}
	}
	
	public class Text extends Unit {
		protected String data;
		protected byte[] bin;
		public Text() {
			data = "";
		}
		
		public Text(String input) {
			data = input;
			this.charcount = data.length();
		}
		
		public Text(byte[] in) throws UnsupportedEncodingException {
			bin = in;
			data = new String(in,encoding);
		}

		public String getString() {
			return data;
		}
		
		
	}
	
	private class Tab extends Unit {
		protected String data;
		
		public Tab() {
			data = new String(new byte[]{0x09});
			this.charcount = 1;
		}
		
	}
	public class NewLine extends Unit {
		protected String data;
		
		public NewLine() {
			data = new String("\n");
			this.charcount = 1;
		}
		
	}
	public class Color extends Unit {
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
		}
		
		public Color(String input,LinkedList<Integer> ops) {
			data = input;
			this.charcount = data.length();
			operations = ops; //will need to track this for actual memory usage.
		}
		
		public void computeOperations(String input) {
			//
		}

		public LinkedList<Integer> getOperations() {
			return operations;
		}
	}
	
	private class Break extends Unit {
		int position;
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
	
	private String deColorLine(Line line) {
		StringBuffer buf = new StringBuffer();
		for(Unit u : line.getData()) {
			if(u instanceof Text) {
				buf.append(((Text)u).data);
			}
			if(u instanceof Color) {
				//Log.e("TEXTCOLOR","color encountered" + ((Color)u).data);
				//buf.append(((Color)u).data);
				//something special here.
				buf.append("{|");
				for(Integer i : ((Color)u).operations) {
					buf.append(Integer.toString(i)+"|");
				}
				buf.append("}");
			}
			if(u instanceof NewLine) {
				buf.append("\n");
			}
		}
		
		return buf.toString();
	}

	public void setMaxLines(int maxLines) {
		MAX_LINES = maxLines;
	}
	
}
