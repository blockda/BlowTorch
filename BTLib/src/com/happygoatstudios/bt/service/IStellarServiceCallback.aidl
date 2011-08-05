package com.happygoatstudios.bt.service;


interface IStellarServiceCallback {
	boolean isWindowShowing();
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
	void showKeyBoard(String txt,boolean popup,boolean add,boolean flush,boolean clear,boolean close);
	void doDisconnectNotice();
	void doLineBreak(int i);
	void reloadButtons(String setName);
	void clearAllButtons();
	void updateMaxVitals(int hp, int mana, int moves);
	void updateVitals(int hp,int mana,int moves);
	void updateEnemy(int hp);
	void updateVitals2(int hp,int mp,int maxhp, int maxmana,int enemy);
}
