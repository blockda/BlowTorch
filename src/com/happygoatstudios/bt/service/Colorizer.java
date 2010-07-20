package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class Colorizer {
	
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	Pattern nocrlf = Pattern.compile("\\x0D");
	
	public static Character escape = new Character((char) 0x1B);
	public static String colorRed = escape+"[1;31m";
	public static String colorWhite = escape+"[0;37m";
	Pattern newline = Pattern.compile("\\x0D");
	Pattern carriage = Pattern.compile("\\x0A");
	Pattern subnego_reg = Pattern.compile("\\xFF\\xFA(.{1})(.*)\\xFF\\xF0");
	Pattern iac_cmd_reg = Pattern.compile("\\xFF([\\xFB-\\xFE])(.{1})");
	Pattern space = Pattern.compile("\\x20");
	Pattern dash = Pattern.compile("\\x2D");
	Pattern tab = Pattern.compile("\\x09");
	
	public Colorizer() {
		//not much to do here

	}
	
	//
	
	int sel_col = 0xFFFFFF;
	
	String htmlColorize(byte[] data) {
		String str = null;
		
		
		StringBuffer buffer = new StringBuffer();
		
		//strip out control info
		Matcher command = null;
		try {
			command = iac_cmd_reg.matcher(new String(data,"ISO-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Matcher sub_cmd = subnego_reg.matcher(command.replaceAll(""));
		
		str = sub_cmd.replaceAll("");
		
		Matcher newlinematcher = newline.matcher(str);
		
		Matcher carriagerock = carriage.matcher(newlinematcher.replaceAll("<br/>"));
		
		Matcher spacerock = space.matcher(carriagerock.replaceAll(""));
		
		Matcher tabrock = tab.matcher(spacerock.replaceAll("&nbsp;"));
		
		Matcher dashrock = dash.matcher(tabrock.replaceAll("&#09"));
		
		//Matcher colormatcher = colordata.matcher(spacerock.replaceAll("&nbsp;"));
		//Matcher colormatcher = colordata.matcher(carriagerock.replaceAll(""));
		Matcher colormatcher = colordata.matcher(dashrock.replaceAll("&#45;"));
		boolean first = true;
		
		
		
		while(colormatcher.find()) {
			
			String bright = colormatcher.group(2);
			String value = colormatcher.group(3);
			
			Integer color = new Integer(sel_col);
			sel_col = getColorValue(new Integer(bright),new Integer(value));
			
			if(first) {
				colormatcher.appendReplacement(buffer, "<font style=\"color:#"+Integer.toHexString(sel_col)+";\">");
				first = false;
			} else {
				colormatcher.appendReplacement(buffer, "</font><font style=\"color:#"+Integer.toHexString(sel_col)+";\">");
			}
			
			//sel_col = getColorValue(new Integer(bright),new Integer(value));
		}
		
		//buffer.append;
		
		colormatcher.appendTail(buffer);
		
		return buffer.toString();
		
	}
	
	SpannableStringBuilder sstr = null;
	
	public Spannable Colorize(byte[] data) {
		sstr = new SpannableStringBuilder();
		//build a spannable string from this data
		
		//build string
		/*String printable = "";
		for(int i = 0; i < update.length;i++) {
			printable = printable.concat(new Character((char)update[i]).toString());
		}*/
		String printable = null;
		try {
			printable = new String(data,"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//colorize input.
		//Pattern endofline = Pattern.compile("([\\x00-\\x7F]*)"); //match line
		Pattern endofline = null;




		
		Matcher stripcr = nocrlf.matcher(printable);
		
		String tocolor = stripcr.replaceAll(""); //strip all carriage returns

		//Matcher stripcolor = colordata.matcher(stripcr.replaceAll("\n"));
		//Matcher stripcolor = colordata.matcher(printable);
		
		//SpannableString tocolorize = new SpannableString(printable);
		//SpannableString stripped = new SpannableString(stripcolor.replaceAll(""));
		
		Matcher stripcolor = colordata.matcher(tocolor);
		//SpannableString colorized = new SpannableString(stripcolor.replaceAll("")); //strip out all the color data
		
		int m_start = 0;
		int m_end = 0;
		int running = 0;
		Object selected_color = new ForegroundColorSpan(0xFFFFFFFF);
		int count = 0;
		
		boolean ended = false;
		boolean found = false;
		
		String selected_bright = "0";
		String selected_val = "37";
		
		stripcolor.reset(tocolor);
		//stripcolor.reset();
		while(stripcolor.find()) {
			found = true;
			
			int start = stripcolor.start();
			int end = stripcolor.end();
			String matched = stripcolor.group();
			String whole = stripcolor.group(1);
			String bright = stripcolor.group(2);
			String value = stripcolor.group(3);
			
			m_end = stripcolor.start();		
			if(m_end < 0 || m_end < m_start) {
				//we recieved a color code at the start of the string, or back to back, so there will be no string to process
				m_start = stripcolor.end();
				selected_bright = bright;
				selected_val = value;
			} else {
				//we reiceved a color code in the middle of a string, so there will be a string to process.
				
				//get substring
				String substr = tocolor.substring(m_start,m_end);
				
				if(bright == null) {
					bright = "0";
				}
				
				//TODO: remove this block when finished with new colorizing method
				/*Message tmp = reportto.obtainMessage(110);
				Bundle dat = new Bundle();
				dat.putString("STRING", substr);
				dat.putString("BRIGHT", selected_bright);
				dat.putString("VALUE", selected_val);
				
				tmp.setData(dat);
				
				synchronized(reportto) {
					while(reportto.hasMessages(110) || reportto.hasMessages(111)) {
						try {
							reportto.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				reportto.sendMessage(tmp);*/
				
				//TODO:new colorizing method
				//get our own color value.
				AppendString(substr,selected_bright,selected_val);
				//TODO: finish new colorizing method
				
				
				m_start = stripcolor.end();
				if(m_start >= tocolor.length()) {
					//mark that we have ended
					ended = true;
				}
				
				//set selected color
				selected_bright = bright;
				selected_val = value;
			}
		}
				

		//perform cleanup if the data did not end in a color selection:
		boolean sendcleanupmsg = false;
		String restoftxt = null;
		
		if(!ended && found) {
			//finish off the string.
			//colorized.setSpan(new ForegroundColorSpan(0xFF00FFFF), m_start, colorized.length()-1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			//colorized.setSpan(selected_color, m_start, colorized.length()-1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			restoftxt = tocolor.substring(m_start,tocolor.length());
			sendcleanupmsg = true;
			
		}
		
		if(!ended && !found) {
			//no color data found, send with default
			//colorized.setSpan(new ForegroundColorSpan(0xFFFF00FF), 0, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			restoftxt = tocolor;
			sendcleanupmsg = true;
		}
		
		if(sendcleanupmsg) {
			/*Message tmp = reportto.obtainMessage(110);
			Bundle dat = new Bundle();
			dat.putString("STRING", restoftxt);
			dat.putString("BRIGHT", selected_bright);
			dat.putString("VALUE", selected_val);
			
			tmp.setData(dat);
			
			synchronized(reportto) {
				while(reportto.hasMessages(110)) {
					try {
						reportto.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			reportto.sendMessage(tmp);
			*/
			AppendString(restoftxt,selected_bright,selected_val);
		}
		
		
		/*synchronized(reportto) {
			while(reportto.hasMessages(110) || reportto.hasMessages(111)) {
				try {
					reportto.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		reportto.sendEmptyMessage(111);
		
		synchronized(myhandler) {
			myhandler.notify();
		}*/
		return sstr;
		
		//debug_box.append(tocolor);
		//reportto.append(colorized);
		//TextView tv = (TextView)viewtouse.findViewById(R.id.tv);
		
	

		
		//return null;
	}
	
	private void AppendString(String substr,String bright,String value) {
		Object color = getColorCode(new Integer(bright),new Integer(value));
		
		//append string
		sstr.append(substr);
		int endmark = sstr.length();
		int startmark = endmark - substr.length();
		
		sstr.setSpan(color,startmark,endmark,SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
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
				colorval = colorval | 0xFF0000BB;
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
		BACKGROUND,
		FOREGROUND
	}
	
	public static COLOR_TYPE getColorType(CharSequence value) {
		
		Integer c = colormap.get(value.toString());
		
		if(c == null) {
			c = 40;
		}
		
		return getColorType(c);
	}

	public static COLOR_TYPE getColorType(Integer value) {
		
		if(value < 40) {
			return COLOR_TYPE.FOREGROUND;
		} else {
			return COLOR_TYPE.BACKGROUND;
		}
	}
		
	
	public static int getColorValue(Integer bright,Integer value) {
		int colorval = 0x000000;
		
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
				colorval = colorval | 0x0000BB;
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
		/*if(tenspot == 3) {
			return new ForegroundColorSpan(colorval);
		} else if (tenspot == 4) {
			return new BackgroundColorSpan(colorval);
		} else {
			return null;
		}*/
	}


}
