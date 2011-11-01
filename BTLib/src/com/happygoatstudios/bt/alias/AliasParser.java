package com.happygoatstudios.bt.alias;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import android.sax.Element;
import android.sax.StartElementListener;

import com.happygoatstudios.bt.service.plugin.settings.BasePluginParser;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;

public final class AliasParser {
	//PluginSettings settings = null;
	//final AliasData current_alias = new AliasData();
	//public AliasParser(Element root, PluginSettings settings) {
		//this.settings = settings;
		//registerListeners(root);
	//}
	public static void registerListeners(Element root,final PluginSettings settings,final AliasData current_alias) {
		Element alias = root.getChild(BasePluginParser.TAG_ALIAS);
		alias.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				current_alias.setPre(a.getValue("",BasePluginParser.ATTR_PRE));
				current_alias.setPost(a.getValue("",BasePluginParser.ATTR_POST));
				String alias_key = current_alias.getPre();
				if(current_alias.getPre().startsWith("^")) alias_key = alias_key.substring(1, alias_key.length());
				if(current_alias.getPre().endsWith("$")) alias_key = alias_key.substring(0, alias_key.length()-1);
				settings.getAliases().put(alias_key, current_alias.copy());
			}
			
		});
	}
	
	public static void saveAliasToXML(XmlSerializer out,AliasData data) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_ALIAS);
		out.attribute("", BasePluginParser.ATTR_PRE, data.getPre());
		out.attribute("", BasePluginParser.ATTR_POST, data.getPost());
		out.endTag("", BasePluginParser.TAG_ALIAS);
	}
	
	
	
	
	
}
