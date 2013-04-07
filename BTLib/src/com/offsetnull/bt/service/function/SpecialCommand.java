package com.offsetnull.bt.service.function;

import com.offsetnull.bt.service.Colorizer;
import com.offsetnull.bt.service.Connection;

public class SpecialCommand {
	//public class SpecialCommand {
		public String commandName;
		
		//public public SpecialCommand(); //{
			//nothing really to do here
		//}
		public Object execute(Object o,Connection c) {
			//this is to be overridden.
			return null;
		}
		
		public static String getErrorMessage(String arg1,String arg2) {
			
			String errormessage = "\n" + Colorizer.getRedColor() + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
			errormessage += arg1 + "\n";
			errormessage += arg2 + "\n";
			//errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
			errormessage += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.getWhiteColor()+"\n";
			return errormessage;
		}
	//}
}
