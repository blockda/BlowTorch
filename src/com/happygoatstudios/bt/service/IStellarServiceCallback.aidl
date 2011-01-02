package com.happygoatstudios.bt.service;


oneway interface IStellarServiceCallback {
	void dataIncoming(inout byte[] seq);
	void processedDataIncoming(CharSequence seq);
	void htmlDataIncoming(String html);
	void rawDataIncoming(inout byte[] raw);
	void rawBufferIncoming(inout byte[] incoming);
	void loadSettings();
	void displayXMLError(String error);
	void executeColorDebug(int arg);
	void invokeDirtyExit();
	void showMessage(String message,boolean longtime);
	void showDialog(String message);
	void doVisualBell();
	void setScreenMode(boolean fullscreen);
	void showKeyBoard(String txt,boolean popup,boolean add,boolean flush);
	void doDisconnectNotice();
	void doLineBreak(int i);
	void reloadButtons(String setName);
}