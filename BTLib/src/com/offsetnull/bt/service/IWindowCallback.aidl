/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

interface IWindowCallback {
	boolean isWindowShowing();
	void rawDataIncoming(in byte[] raw);
	void resetWithRawDataIncoming(in byte[] raw);
	void redraw();
	String getName();
	void shutdown();
	void xcallS(String function,String str);
	void xcallB(String function,in byte[] raw);
	void clearText();
	void updateSetting(String key,String value);
	void setEncoding(String value);
}
