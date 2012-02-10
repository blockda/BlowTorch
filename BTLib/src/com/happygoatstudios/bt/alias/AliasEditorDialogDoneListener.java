package com.happygoatstudios.bt.alias;

public interface AliasEditorDialogDoneListener {
	public void newAliasDialogDone(String pre,String post);
	public void editAliasDialogDone(String pre,String post,int pos,AliasData orig);
}
