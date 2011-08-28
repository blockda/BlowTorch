package com.happygoatstudios.bt.service.function;

import com.happygoatstudios.bt.service.Connection;

public class SwitchWindowCommand extends SpecialCommand {
	public SwitchWindowCommand() {
		this.commandName = "switch";
	}
	
	public Object execute(Object o,Connection c) {
		String connection = (String)o;
		
		c.service.setClutch(connection);
		c.switchTo(connection);
		
		return null;
	}
}
