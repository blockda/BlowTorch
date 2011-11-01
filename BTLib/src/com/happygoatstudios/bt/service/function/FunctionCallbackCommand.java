package com.happygoatstudios.bt.service.function;

import com.happygoatstudios.bt.service.Connection;

public class FunctionCallbackCommand extends SpecialCommand {
	int id = -1;
	String callback = null;
	public FunctionCallbackCommand(int id,String command,String callback) {
		this.id = id;
		this.callback = callback;
		this.commandName = command;
	}
	
	public Object execute(Object o,Connection c) {
		
		String args = (String)o;
		
		c.executeFunctionCallback(id,callback,args);
		
		return null;
	}
}
