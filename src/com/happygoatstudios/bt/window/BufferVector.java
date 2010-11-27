package com.happygoatstudios.bt.window;

import java.util.Vector;

@SuppressWarnings("serial")
public class BufferVector<T> extends Vector<T> {
	public void removeRange(int start,int end) {
		super.removeRange(start, end);
	}
}
