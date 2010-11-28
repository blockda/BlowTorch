package com.happygoatstudios.bt.service;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
//import android.util.Log;

public class Colorizer {
	
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	Pattern nocrlf = Pattern.compile("\\x0D");
	
	public static Character escape = new Character((char) 0x1B);
	public static String colorRed = escape+"[1;31m";
	public static String colorWhite = escape+"[0;37m";
	public static String colorGreen = escape+"[1;34m";
	public static String colorCyanBright = escape+"[1;36m";
	public static String colorYeollowBright = escape+"[1;33m";
	public static String debugString = escape+"[39;49m" + escape + "[0;10m" + "this is the debug string" + colorGreen + "greentext" + escape + "[39;49m" + escape + "[0;10mbacktonormal\n";
	Pattern newline = Pattern.compile("\\x0D");
	Pattern carriage = Pattern.compile("\\x0A");
	Pattern subnego_reg = Pattern.compile("\\xFF\\xFA(.{1})(.*)\\xFF\\xF0");
	Pattern iac_cmd_reg = Pattern.compile("\\xFF([\\xFB-\\xFE])(.{1})");
	Pattern space = Pattern.compile("\\x20");
	Pattern dash = Pattern.compile("\\x2D");
	Pattern tab = Pattern.compile("\\x09");
	
	public Colorizer() {
		//i'm more of a static class.
	}
	
	
	public Object getColorCode(Integer bright,Integer value) {
		int colorval = 0xFF000000;
		
		int onespot = 0;
		int tenspot = 0;
		if(value >= 30 && value < 40) {
			onespot = value - 30;
			tenspot = 3;
		} else if(value >= 40 && value < 50) {
			onespot = value - 40;
			tenspot = 4;
		}
		
		if(bright == null || bright == 0) {
			//normal operation, not bright color
			switch(onespot) {
			case 0:
				//black, nothing
				break;
			case 1:
				//red
				colorval = colorval | 0xFFBB0000;
				break;
			case 2:
				//green
				colorval = colorval | 0xFF00BB00;
				break;
			case 3: 
				//yellow
				colorval = colorval | 0xFFBBBB00;
				break;
			case 4:
				//blue
				//colorval = colorval | 0xFF0000BB; //BB is too dark, turning it up, this hsould be an option.
				colorval = colorval | 0xFF0000EE;
				break;
			case 5:
				//magenta
				colorval = colorval | 0xFFBB00BB;
				break;
			case 6:
				//cyan
				colorval = colorval | 0xFF00BBBB;
				break;
			case 7:
				//white
				colorval = colorval | 0xFFBBBBBB;
				break;
			}
			
		} else if(bright == 1) {
			//bright color operation
			switch(onespot) {
			case 0:
				//black, 
				colorval = colorval | 0xFF555555;
				break;
			case 1:
				//red
				colorval = colorval | 0xFFFF5555;
				break;
			case 2:
				//green
				colorval = colorval | 0xFF55FF55;
				break;
			case 3: 
				//yellow
				colorval = colorval | 0xFFFFFF55;
				break;
			case 4:
				//blue
				colorval = colorval | 0xFF5555FF;
				break;
			case 5:
				//magenta
				colorval = colorval | 0xFFFF55FF;
				break;
			case 6:
				//cyan
				colorval = colorval | 0xFF55FFFF;
				break;
			case 7:
				//white
				colorval = colorval | 0xFFFFFFFF;
				break;
			}
		}
		
		if(tenspot == 3) {
			return new ForegroundColorSpan(colorval);
		} else if (tenspot == 4) {
			return new BackgroundColorSpan(colorval);
		} else {
			return null;
		}
	}
	
	
	public static HashMap<CharSequence,Integer> colormap = new HashMap<CharSequence, Integer>();
	static
	{
		colormap.put("0", 0);
		colormap.put("1", 1);
		colormap.put("2",2);
		colormap.put("30", 30);
		colormap.put("31", 31);
		colormap.put("32", 32);
		colormap.put("33", 33);
		colormap.put("34", 34);
		colormap.put("35", 35);
		colormap.put("36", 36);
		colormap.put("37", 37);
		colormap.put("40", 40);
		colormap.put("41", 41);
		colormap.put("42", 42);
		colormap.put("43", 43);
		colormap.put("44", 44);
		colormap.put("45", 45);
		colormap.put("46", 46);
		colormap.put("47", 47);
		//defaults.
		colormap.put("39", 39);
		colormap.put("49", 49);
		//colormap.put("10", 10);
	}
	public static int getColorValue(CharSequence bright, CharSequence value) {
		
		Integer b = colormap.get(bright.toString());
		Integer c = colormap.get(value.toString());
		
		//Integer b = BigInteger(bright);
		//Integer b = Integer.parseInt(bright);
		
		if(b == null) {
			b = 0;
		}
		
		if(c == null) {
			c = 31;
		}
		
		return getColorValue(b,c);
	}
	
	public enum COLOR_TYPE {
		ZERO_CODE,
		BRIGHT_CODE,
		DEFAULT_FOREGROUND,
		DEFAULT_BACKGROUND,
		DIM_CODE,
		BACKGROUND,
		FOREGROUND,
		NOT_A_COLOR
	}
	
	public static COLOR_TYPE getColorType(CharSequence value) {
		
		Integer c = colormap.get(value.toString());
		
		if(c == null) {
			return COLOR_TYPE.NOT_A_COLOR;
		}
		
		return getColorType(c);
	}

	public static COLOR_TYPE getColorType(Integer value) {
		if(value == 0) {
			return COLOR_TYPE.ZERO_CODE;
		}
		
		if(value == 1) {
			return COLOR_TYPE.BRIGHT_CODE;
		}
		
		if(value == 2) {
			return COLOR_TYPE.DIM_CODE;
		}
		
		if(value == 39) {
			return COLOR_TYPE.DEFAULT_FOREGROUND;
		}
		
		if(value == 49) {
			return COLOR_TYPE.DEFAULT_BACKGROUND;
		}
		
		COLOR_TYPE retval = COLOR_TYPE.NOT_A_COLOR;
		if(value < 40 && value >=30) {
			retval = COLOR_TYPE.FOREGROUND;
		} else if(value >=40 && value < 50) {
			retval = COLOR_TYPE.BACKGROUND;
		}
		
		//Log.e("Colorizer","Returning " + retval + " for " + value);
		
		return retval;
	}
		
	
	public static int getColorValue(Integer bright,Integer value) {
		int colorval = 0x000000;
		
		int onespot = 0;
		//int tenspot = 0;
		
		if(value == 39) {
			return 0xBBBBBB;
		}
		if(value == 49) {
			return 0x000000;
		}
		
		if(value >= 30 && value < 40) {
			onespot = value - 30;
			//tenspot = 3;
		} else if(value >= 40 && value < 50) {
			onespot = value - 40;
			//tenspot = 4;
		}
		
		if(bright == null || bright == 0) {
			//normal operation, not bright color
			switch(onespot) {
			case 0:
				//black, nothing
				colorval = colorval & 0x000000;
				break;
			case 1:
				//red
				colorval = colorval | 0xBB0000;
				break;
			case 2:
				//green
				colorval = colorval | 0x00BB00;
				break;
			case 3: 
				//yellow
				colorval = colorval | 0xBBBB00;
				break;
			case 4:
				//blue
				//colorval = colorval | 0x0000BB; //0x0000BB is a bit too dark on my screen, so i'm turning it up a bit. this should really be an option.
				colorval = colorval | 0x0000EE;
				break;
			case 5:
				//magenta
				colorval = colorval | 0xBB00BB;
				break;
			case 6:
				//cyan
				colorval = colorval | 0x00BBBB;
				break;
			case 7:
				//white
				colorval = colorval | 0xBBBBBB;
				break;
			}
			
		} else if(bright == 1) {
			//bright color operation
			switch(onespot) {
			case 0:
				//black, 
				colorval = colorval | 0x555555;
				break;
			case 1:
				//red
				colorval = colorval | 0xFF5555;
				break;
			case 2:
				//green
				colorval = colorval | 0x55FF55;
				break;
			case 3: 
				//yellow
				colorval = colorval | 0xFFFF55;
				break;
			case 4:
				//blue
				colorval = colorval | 0x5555FF;
				break;
			case 5:
				//magenta
				colorval = colorval | 0xFF55FF;
				break;
			case 6:
				//cyan
				colorval = colorval | 0x55FFFF;
				break;
			case 7:
				//white
				colorval = colorval | 0xFFFFFF;
				break;
			}
		}
		
		return colorval;
		
	}


}
