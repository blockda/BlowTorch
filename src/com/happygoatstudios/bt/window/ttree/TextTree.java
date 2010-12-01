package com.happygoatstudios.bt.window.ttree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class TextTree {
	
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?(([0-9]{1,2});)?([0-9]{1,2})m");
	Matcher colormatch = colordata.matcher("");
	
	private static Pattern oplookup = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?(([0-9]{1,2});)?([0-9]{1,2})m");
	private static Matcher op_match = oplookup.matcher("");
	
	private int MAX_LINES = 20;
	
	public TextTree() {
		mLines = new LinkedList<Line>();
		LinkedList<Unit> list = new LinkedList<Unit>();
		
	}
	
	private LinkedList<Unit> ColorizeText(String input) {
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
	boolean appendLast = false; //for marking when the addtext call has ended with a newline or not.
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
					tmp.getData().addAll(tmp.getData().size(),ColorizeText(linebuf.toString()));
					lines.add(tmp);
				} else {
					//Log.e("TEXTREEDATAINPUT","INSERTING:" + linebuf.toString());
					Line colorline = new Line();
					colorline.setData(ColorizeText(linebuf.toString()));
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
				tmp.setData(ColorizeText(linebuf.toString()));
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
				lastline.setData(ColorizeText(text));
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
		
		Log.e("TEXTTREE",getLastTwenty(false));
		Log.e("TEXTTREE","ADDED TEXT: LAST 20 LINES");
		
	}
	
	LinkedList<Line> mLines;
	Line pStart;
	Line pSend;
	
	private class Line {
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
	
	private class Unit {
		protected int charcount;
		//protected int bytecount;
		
		public Unit() {
			charcount = 0;
		}
	}
	
	private class Text extends Unit {
		protected String data;
		
		public Text() {
			data = "";
		}
		
		public Text(String input) {
			data = input;
			this.charcount = data.length();
		}
		
		
	}
	
	private class Color extends Unit {
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
				buf.insert(0,"\n");
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
				buf.append(((Color)u).data);
			}
		}
		
		return buf.toString();
	}
	
}
