package com.happygoatstudios.bt.service;


oneway interface IBaardTERMServiceCallback {
	void dataIncoming(inout byte[] seq);
	void processedDataIncoming(CharSequence seq);
	void htmlDataIncoming(String html);
	void rawDataIncoming(String raw);
	void rawBufferIncoming(String rawbuf);
	void loadSettings();
}