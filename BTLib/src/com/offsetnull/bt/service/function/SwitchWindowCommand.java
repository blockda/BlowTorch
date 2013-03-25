package com.offsetnull.bt.service.function;

import com.offsetnull.bt.service.Connection;

public class SwitchWindowCommand extends SpecialCommand {
	public SwitchWindowCommand() {
		this.commandName = "switch";
	}
	
	public Object execute(Object o,Connection c) {
		String connection = (String)o;
		
		c.getService().setClutch(connection);
		c.switchTo(connection);
		
		return null;
	}
}
