package com.happygoatstudios.bt.service;

interface IWindowCallback {
	boolean isWindowShowing();
	void rawDataIncoming(inout byte[] raw);
	void redraw();
	String getName();
	void shutdown();
}