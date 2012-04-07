package com.offsetnull.bt.settings;

import android.content.Context;

public class ConfigurationLoader {
	
	public static String getConfigurationValue(String key, Context context) {
		int id = context.getResources().getIdentifier(key, "string", context.getPackageName());
		return context.getResources().getString(id);
	}
	
	public static int getAboutDialogResource(Context context) {
		int id = context.getResources().getIdentifier("about_dialog", "layout", context.getPackageName());
		return id;
	}
	
    public static boolean isTestMode(Context context) {
    	if(getConfigurationValue("testMode",context).equals("true")) {
    		return true;
    	} else {
    		return false;
    	}
    }
}
