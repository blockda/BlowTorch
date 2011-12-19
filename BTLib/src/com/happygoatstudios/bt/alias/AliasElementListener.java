package com.happygoatstudios.bt.alias;

import org.xml.sax.Attributes;

import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;

import android.sax.StartElementListener;

public class AliasElementListener implements StartElementListener {

	PluginParser.NewItemCallback callback = null;
	AliasData current_alias = null;
	public AliasElementListener(PluginParser.NewItemCallback callback,AliasData current_alias) {
		this.callback = callback;
		this.current_alias = current_alias;
	}
	
	public void start(Attributes a) {
		current_alias.setPre(a.getValue("",BasePluginParser.ATTR_PRE));
		current_alias.setPost(a.getValue("",BasePluginParser.ATTR_POST));
		String alias_key = current_alias.getPre();
		if(current_alias.getPre().startsWith("^")) alias_key = alias_key.substring(1, alias_key.length());
		if(current_alias.getPre().endsWith("$")) alias_key = alias_key.substring(0, alias_key.length()-1);
		callback.addAlias(alias_key, current_alias.copy());
		//settings.getAliases().put(alias_key, current_alias.copy());
	
	}
	
}