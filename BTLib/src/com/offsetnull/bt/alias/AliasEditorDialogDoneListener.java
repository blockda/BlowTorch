package com.offsetnull.bt.alias;

public interface AliasEditorDialogDoneListener {
	public void newAliasDialogDone(String pre,String post,boolean enabled);
	public void editAliasDialogDone(String pre,String post,boolean enabled,int pos,AliasData orig);
}
