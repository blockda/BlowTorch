package com.happygoatstudios.bt.service;

import com.happygoatstudios.bt.window.SlickButtonData;
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
	void setFontSize(int size);
	void setFontSpaceExtra(int size);
	void setFontName(String name);
	void setFontPath(String path);
	void setSemiOption(boolean bools_are_newline);
	void addButton(String targetset, in SlickButtonData new_button);
	void removeButton(String targetset,in SlickButtonData button_to_nuke);
	List<SlickButtonData> getButtonSet(String setname);
	List<String> getButtonSetNames();
	void modifyButton(String targetset,in SlickButtonData orig, in SlickButtonData mod);
	void addNewButtonSet(String name);
	List<String> getButtonSets();
	void deleteButtonSet(String name);
	void clearButtonSet(String name);
	Map getButtonSetListInfo();
	String getLastSelectedSet();
}