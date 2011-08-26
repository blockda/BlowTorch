package com.happygoatstudios.bt.service.function;

import com.happygoatstudios.bt.service.Connection;

public class BellCommand extends SpecialCommand {
	public BellCommand() {
		this.commandName = "dobell";
	}
	public Object execute(Object o,Connection c) {
		
		c.handler.sendEmptyMessage(Connection.MESSAGE_BELLINC);
		
		return null;
		
	}
}