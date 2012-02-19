package com.happygoatstudios.bt.service.function;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.RemoteException;

import com.happygoatstudios.bt.service.Connection;

public class KeyboardCommand extends SpecialCommand {
	String encoding;
	public KeyboardCommand() {
		this.commandName = "keyboard";
		//alternate short form, kb.
		//this.encoding = encoding;
		this.encoding = "ISO-8859-1";
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public Object execute(Object o,Connection c) {
		
		//DO ALIAS/VARIABLE TRANSFORMATIONS!!!
		//ACTUALLY, I THINK THE TRANSFORM STEP
		//IS DONE BEFORE SPECIAL COMMAND PARSING.
		
		//command format.
		//.kb message - set keyboard text.
		//.kb add message - append message to current keyboard.
		//.kb popup message - set keyboard text and popup.
		//.kb add popop message - append message and popup.
		//.kb popup add message - same as prev, but with syntax swapped.
		//.kb flush message - send the keyboard.
		//.kb close - closes the keyboard
		//.kb clear - clears any text in the keyboard
		//.kb - print the kb help message.
		boolean failed = false;
		if(o==null) {
			//fail, print kb
			failed = true;
		} else if(((String)o).equals("")) {
			//fail, print kb.
			failed = true;
		}
		
		if(failed) {
			c.sendDataToWindow(getErrorMessage("Keyboard (kb) special command usage:",".kb options message\n" +
					"Options are as follows:\n" +
					"add,popup,flush,close,clear\n"+
					"add and popup are optional flags that will append text or popup the window when supplied.\n" +
					"flush sends the current text in the input window to the server.\n" +
					"close will close the keyboard if it is open.\n"+
					"clear will erase any text that is currently in the input window.\n" +
					"Example:\n" +
					"\".kb popup reply \" will put \"reply \" into the input bar and pop up the keyboard.\n" +
					"\".kb add foo\" will append foo to the current text in the input box and not pop up the keyboard.\n" +
					"\".kb flush\" will transmit the text currently in the box.\n" +
					"The cursor is always moved to the end of the new text."));
			return null;
		}
		
		Pattern p = Pattern.compile("^\\s*(add|popup|flush|close|clear){0,1}\\s*(add\\s+|popup\\s+|flush\\s+){0,1}(.*)$");
		Matcher m = p.matcher((String)o);
		String operation1 = "";
		String operation2 = "";
		String text = "";
		if(m.matches()) {
			//match
			operation1 = m.group(1);
			operation2 = m.group(2);
			text = m.group(3);
		} else {
			//shouldn't ever not match.
		}
		boolean doadd = false;
		boolean dopopup = false;
		boolean doflush = false;
		boolean doclear = false;
		boolean doclose = false;
		
		if(operation1 != null && !operation1.equals("")) {
			operation1 = operation1.replaceAll("\\s", "");
			if(operation1.equalsIgnoreCase("add")) {
				doadd = true;
			}
			if(operation1.equalsIgnoreCase("popup")) {
				dopopup = true;
			}
		}
		if(operation2 != null && !operation2.equals("")) {
			operation2 = operation2.replaceAll("\\s", "");
			if(operation2.equalsIgnoreCase("add")) {
				doadd = true;
			}
			if(operation2.equalsIgnoreCase("popup")) {
				dopopup = true;
			}
		}
		
		if(operation1 != null && !operation1.equals("")) {
			
			if(operation1.equalsIgnoreCase("flush")) {
				doflush = true;
			}
			if(operation1.equalsIgnoreCase("clear")) {
				doclear = true;
			}
			if(operation1.equalsIgnoreCase("close")) {
				doclose = true;
			}
		}
		
		Boolean foo = new Boolean(true);
		try {
			text = new String(c.doKeyboardAliasReplace(text.getBytes(encoding),foo),encoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		c.service.doShowKeyboard(text,dopopup,doadd,doflush,doclear,doclose);
//		return null;
		return null;
	}
}
