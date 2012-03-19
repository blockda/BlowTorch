package com.happygoatstudios.bt.service.plugin.settings;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

public abstract class BaseOption extends Option {

	protected Object value;
	protected Object defaultValue;
	
	abstract public void setValue(Object o);
	abstract public Object getValue();
	abstract public Object getDefaultValue();
	abstract public void setDefaultValue(Object o);
	
	abstract public void saveToXML(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException;
}
