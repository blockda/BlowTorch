package com.offsetnull.bt.service.function;

import com.offsetnull.bt.service.Connection;

public class BellCommand extends SpecialCommand {
	public BellCommand() {
		this.commandName = "dobell";
	}
	public Object execute(Object o,Connection c) {
		
		c.handler.sendEmptyMessage(Connection.MESSAGE_BELLINC);
		
		return null;
		
	}
}