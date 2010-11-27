package com.happygoatstudios.bt.window;

import java.util.LinkedList;

public class CommandKeeper {
	int max;
	LinkedList<String> commands;
	
	int index = 0;
	int selected = 0;
	
	static enum STATE {
		FORWARD,
		PREV,
		NONE
	}
	
	STATE direction = STATE.NONE;
	
	public CommandKeeper(int maxCommands) {
		max = maxCommands;
		commands = new LinkedList<String>();
	}
	
	public void addCommand(String cmd) {
		
		if(commands.size() > 0) {
			String test = commands.getFirst();
			/*if(test == cmd) {
				return;
			}*/
			if(test.equals(cmd)) {
				selected = 0;
				direction = STATE.NONE;
				return;
			}
		}
		
		if(cmd.equals("")) {
			return;
		}
		
		if(commands.size() > max) {
			commands.removeLast();
		}
		

		
		commands.addFirst(cmd);
		selected = 0;
		direction = STATE.NONE;
		
	}
	
	public String getNext() {
		
		if (commands.size() == 0) {
			return "";
		}
		
		switch(direction) {
		case NONE:
			//at the 0'th command, have not gone forward or back. and set the state.
			selected = 0;
			direction = STATE.FORWARD;
			break;
		case FORWARD:
			//increase one unit
			selected = selected + 1;
			if(selected > commands.size() -1) {
				selected = 0;
			}
			break;
		case PREV:
			//increase by two units and set the state to forward.
			selected = selected + 2;
			direction = STATE.FORWARD;
			if(selected > commands.size() -1) {
				selected = 0;
			}
			break;
		}
		
		String get_current = commands.get(selected);
		return get_current;
		
	}
	
	public String getPrev() {
		
		if(commands.size() ==0) {
			return "";
		}
		
		switch(direction) {
		case FORWARD:
			selected = selected - 1;
			if(selected < 0) {
				selected = 0;
				return "";
			}
			direction = STATE.PREV;
			break;
		case PREV:
			selected = selected - 1;
			if(selected < 0) {
				//selected = commands.size() -1;
				selected = 0;
				direction = STATE.NONE;
				return "";
			}
			break;
		case NONE:
			selected = selected - 1;
			if(selected < 0) {
				selected = 0;
				return "";
			}
			direction = STATE.PREV;
			break;
		}
		
		String select = commands.get(selected);
		
		return select;
		

	}
	
	
}
