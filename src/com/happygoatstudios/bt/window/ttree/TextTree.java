package com.happygoatstudios.bt.window.ttree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TextTree {
	
	public TextTree() {
		mLines = new ArrayList<Line>();
		LinkedList<Unit> list = new LinkedList<Unit>();
		
	}
	
	List<Line> mLines;
	Line pStart;
	Line pSend;
	
	private class Line {
		protected int totalbytes;
		protected int charcount;
		protected int breaks;
		
		List<Unit> mData;
	}
	
	private class Unit {
		protected int charcount;
		protected int bytecount;
		
		protected Unit right;
		protected Unit left;
	}
	
	private class Text extends Unit {
		protected String data;
	}
	
	private class Color extends Unit {
		protected String data;
		int color;
	}
	
	private class Break extends Unit {
		int position;
	}
	
}
