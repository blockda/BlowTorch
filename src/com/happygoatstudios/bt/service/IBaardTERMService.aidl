package com.happygoatstudios.bt.service;

import com.happygoatstudios.bt.service.IBaardTERMServiceCallback;

interface IBaardTERMService {
	void registerCallback(IBaardTERMServiceCallback c);
	void unregisterCallback(IBaardTERMServiceCallback c);
	int getPid();
	void initXfer();
	void endXfer();
	void sendData(in byte[] seq);
	void setNotificationText(CharSequence seq);
	void setConnectionData(String host,int port,String display);
	void beginCompression();
	void stopCompression();
	void requestBuffer();
	void saveBuffer(String buffer);
	void addAlias(String what, String to);
	Map getAliases();
	void setAliases(in Map map);
}