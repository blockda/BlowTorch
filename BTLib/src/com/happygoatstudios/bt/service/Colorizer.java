package com.happygoatstudios.bt.service;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;


public class Colorizer {
	
	Pattern colordata = Pattern.compile("\\x1B\\x5B(([0-9]{1,2});)?([0-9]{1,2})m");
	Pattern nocrlf = Pattern.compile("\\x0D");
	
	public static Character escape = new Character((char) 0x1B);
	public static String colorRed = escape+"[1;31m";
	public static String colorWhite = escape+"[0;37m";
	public static String colorGreen = escape+"[1;34m";
	public static String colorCyanBright = escape+"[1;36m";
	public static String colorYeollowBright = escape+"[1;33m";
	public static String telOptColorBegin = escape + "[1;43;30m";
	public static String telOptColorEnd = escape + "[0m";
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
	
	
	
	public static int getColorValue(CharSequence bright, CharSequence value,boolean is256Color) {
		
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
		
		return getColorValue(b,c,is256Color);
	}
	
	public enum COLOR_TYPE {
		ZERO_CODE,
		BRIGHT_CODE,
		DEFAULT_FOREGROUND,
		DEFAULT_BACKGROUND,
		DIM_CODE,
		BACKGROUND,
		FOREGROUND,
		NOT_A_COLOR,
		XTERM_256_FG_START,
		XTERM_256_BG_START,
		XTERM_256_FIVE,
		XTERM_256_COLOR
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
		
		if(value == 38) return COLOR_TYPE.XTERM_256_FG_START;
		
		if(value == 48) return COLOR_TYPE.XTERM_256_BG_START;
		
		if(value == 5) return COLOR_TYPE.XTERM_256_FIVE;
		
		COLOR_TYPE retval = COLOR_TYPE.NOT_A_COLOR;
		if(value < 40 && value >=30) {
			retval = COLOR_TYPE.FOREGROUND;
		} else if(value >=40 && value < 50) {
			retval = COLOR_TYPE.BACKGROUND;
		}
		
		//Log.e("Colorizer","Returning " + retval + " for " + value);
		
		return retval;
	}
	
	
	
	public static int get256ColorValue(Integer value) {
		int retVal = 0xFFFFFFFF;
		Integer val = colormap256.get(value);
		if(val != null) retVal = val;
		return retVal;
	}
		
	
	public static int getColorValue(Integer bright,Integer value,boolean is256color) {
		int colorval = 0x000000;
		
		if(is256color) return get256ColorValue(value);
		
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
	
	private static HashMap<Integer,Integer> colormap256 = new HashMap<Integer,Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6744132113788617754L;

		{
			this.put(0, 0xFF000000); //BLACK
			this.put(1, 0xFFBB0000); //RED
			this.put(2, 0xFF00BB00); //GREEN
			this.put(3, 0xFFBBBB00); //YELLOW
			this.put(4, 0xFF0000EE); //BLUE
			this.put(5, 0xFFBB00BB); //MAGENTA
			this.put(6, 0xFF00BBBB); //CYAN
			this.put(7, 0xFFBBBBBB); //WHITE
			this.put(8, 0xFF555555); //BRIGHT BLACK (GREY)
			this.put(9, 0xFFFF5555); //BRIGHT RED
			this.put(10, 0xFF55FF55); //BRIGHT GREEN
			this.put(11, 0xFFFFFF55); //BRIGHT YELLOW
			this.put(12, 0xFF5555FF); //BRIGHT BLUE
			this.put(13, 0xFFFF55FF); //BRIGHT MAGENTA
			this.put(14, 0xFF55FFFF); //BRIGHT CYAN
			this.put(15, 0xFFFFFFFF); //BRIGHT WHITE
			
			//start normative xterm256 color
			this.put(16, 0xFF000000);
			this.put(17, 0xFF00005F);
			this.put(18, 0xFF000087);
			this.put(19, 0xFF0000AF);
			this.put(20, 0xFF0000D7);
			this.put(21, 0xFF0000FF);
			
			this.put(22, 0xFF005F00);
			this.put(23, 0xFF005F5F);
			this.put(24, 0xFF005F87);
			this.put(25, 0xFF005FAF);
			this.put(26, 0xFF005FD7);
			this.put(27, 0xFF005FFF);
			
			this.put(28, 0xFF008700);
			this.put(29, 0xFF00875F);
			this.put(30, 0xFF008787);
			this.put(31, 0xFF0087AF);
			this.put(32, 0xFF0087D7);
			this.put(33, 0xFF0087FF);
			
			this.put(34, 0xFF00AF00);
			this.put(35, 0xFF00AF5F);
			this.put(36, 0xFF00AF87);
			this.put(37, 0xFF00AFAF);
			this.put(38, 0xFF00AFD7);
			this.put(39, 0xFF00AFFF);
			
			this.put(40, 0xFF00D700);
			this.put(41, 0xFF00D75F);
			this.put(42, 0xFF00D787);
			this.put(43, 0xFF00D7AF);
			this.put(44, 0xFF00D7D7);
			this.put(45, 0xFF00D7FF);
			
			this.put(46, 0xFF00FF00);
			this.put(47, 0xFF00FF5F);
			this.put(48, 0xFF00FF87);
			this.put(49, 0xFF00FFAF);
			this.put(50, 0xFF00FFD7);
			this.put(51, 0xFF00FFFF);
			
			this.put(52, 0xFF5F0000);
			this.put(53, 0xFF5F005F);
			this.put(54, 0xFF5F0087);
			this.put(55, 0xFF5F00AF);
			this.put(56, 0xFF5F00D7);
			this.put(57, 0xFF5F00FF);
			
			this.put(58, 0xFF5F5F00);
			this.put(59, 0xFF5F5F5F);
			this.put(60, 0xFF5F5F87);
			this.put(61, 0xFF5F5FAF);
			this.put(62, 0xFF5F5FD7);
			this.put(63, 0xFF5F5FFF);
			
			this.put(64, 0xFF5F8700);
			this.put(65, 0xFF5F875F);
			this.put(66, 0xFF5F8787);
			this.put(67, 0xFF5F87AF);
			this.put(68, 0xFF5F87D7);
			this.put(69, 0xFF5F87FF);
			
			this.put(70, 0xFF5FAF00);
			this.put(71, 0xFF5FAF5F);
			this.put(72, 0xFF5FAF87);
			this.put(73, 0xFF5FAFAF);
			this.put(74, 0xFF5FAFD7);
			this.put(75, 0xFF5FAFFF);
			
			this.put(76, 0xFF5FD700);
			this.put(77, 0xFF5FD75F);
			this.put(78, 0xFF5FD787);
			this.put(79, 0xFF5FD7AF);
			this.put(80, 0xFF5FD7D7);
			this.put(81, 0xFF5FD7FF);
			
			this.put(82, 0xFF5FFF00);
			this.put(83, 0xFF5FFF5F);
			this.put(84, 0xFF5FFF87);
			this.put(85, 0xFF5FFFAF);
			this.put(86, 0xFF5FFFD7);
			this.put(87, 0xFF5FFFFF);
			
			this.put(88, 0xFF870000);
			this.put(89, 0xFF87005F);
			this.put(90, 0xFF870087);
			this.put(91, 0xFF8700AF);
			this.put(92, 0xFF8700D7);
			this.put(93, 0xFF8700FF);
			
			this.put(94, 0xFF875F00);
			this.put(95, 0xFF875F5F);
			this.put(96, 0xFF875F87);
			this.put(97, 0xFF875FAF);
			this.put(98, 0xFF875FD7);
			this.put(99, 0xFF875FFF);
			
			this.put(100, 0xFF878700);
			this.put(101, 0xFF87875F);
			this.put(102, 0xFF878787);
			this.put(103, 0xFF8787AF);
			this.put(104, 0xFF8787D7);
			this.put(105, 0xFF8787FF);
			
			this.put(106, 0xFF87AF00);
			this.put(107, 0xFF87AF5F);
			this.put(108, 0xFF87AF87);
			this.put(109, 0xFF87AFAF);
			this.put(110, 0xFF87AFD7);
			this.put(111, 0xFF87AFFF);
			
			this.put(112, 0xFF87D700);
			this.put(113, 0xFF87D75F);
			this.put(114, 0xFF87D787);
			this.put(115, 0xFF87D7AF);
			this.put(116, 0xFF87D7D7);
			this.put(117, 0xFF87D7FF);
			
			this.put(118, 0xFF87FF00);
			this.put(119, 0xFF87FF5F);
			this.put(120, 0xFF87FF87);
			this.put(121, 0xFF87FFAF);
			this.put(122, 0xFF87FFD7);
			this.put(123, 0xFF87FFFF);
			
			this.put(124, 0xFFAF0000);
			this.put(125, 0xFFAF005F);
			this.put(126, 0xFFAF0087);
			this.put(127, 0xFFAF00AF);
			this.put(128, 0xFFAF00D7);
			this.put(129, 0xFFAF00FF);
			
			this.put(130, 0xFFAF5F00);
			this.put(131, 0xFFAF5F5F);
			this.put(132, 0xFFAF5F87);
			this.put(133, 0xFFAF5FAF);
			this.put(134, 0xFFAF5FD7);
			this.put(135, 0xFFAF5FFF);
			
			this.put(136, 0xFFAF8700);
			this.put(137, 0xFFAF875F);
			this.put(138, 0xFFAF8787);
			this.put(139, 0xFFAF87AF);
			this.put(140, 0xFFAF87D7);
			this.put(141, 0xFFAF87FF);
			
			this.put(142, 0xFFAFAF00);
			this.put(143, 0xFFAFAF5F);
			this.put(144, 0xFFAFAF87);
			this.put(145, 0xFFAFAFAF);
			this.put(146, 0xFFAFAFD7);
			this.put(147, 0xFFAFAFFF);
			
			this.put(148, 0xFFAFD700);
			this.put(149, 0xFFAFD75F);
			this.put(150, 0xFFAFD787);
			this.put(151, 0xFFAFD7AF);
			this.put(152, 0xFFAFD7D7);
			this.put(153, 0xFFAFD7FF);
			
			this.put(154, 0xFFAFFF00);
			this.put(155, 0xFFAFFF5F);
			this.put(156, 0xFFAFFF87);
			this.put(157, 0xFFAFFFAF);
			this.put(158, 0xFFAFFFD7);
			this.put(159, 0xFFAFFFFF);
			
			this.put(160, 0xFFD70000);
			this.put(161, 0xFFD7005F);
			this.put(162, 0xFFD70087);
			this.put(163, 0xFFD700AF);
			this.put(164, 0xFFD700D7);
			this.put(165, 0xFFD700FF);
			
			this.put(166, 0xFFD75F00);
			this.put(167, 0xFFD75F5F);
			this.put(168, 0xFFD75F87);
			this.put(169, 0xFFD75FAF);
			this.put(170, 0xFFD75FD7);
			this.put(171, 0xFFD75FFF);
			
			this.put(172, 0xFFD78700);
			this.put(173, 0xFFD7875F);
			this.put(174, 0xFFD78787);
			this.put(175, 0xFFD787AF);
			this.put(176, 0xFFD787D7);
			this.put(177, 0xFFD787FF);
			
			this.put(178, 0xFFD7AF00);
			this.put(179, 0xFFD7AF5F);
			this.put(180, 0xFFD7AF87);
			this.put(181, 0xFFD7AFAF);
			this.put(182, 0xFFD7AFD7);
			this.put(183, 0xFFD7AFFF);
			
			this.put(184, 0xFFD7D700);
			this.put(185, 0xFFD7D75F);
			this.put(186, 0xFFD7D787);
			this.put(187, 0xFFD7D7AF);
			this.put(188, 0xFFD7D7D7);
			this.put(189, 0xFFD7D7FF);
			
			this.put(190, 0xFFD7FF00);
			this.put(191, 0xFFD7FF5F);
			this.put(192, 0xFFD7FF87);
			this.put(193, 0xFFD7FFAF);
			this.put(194, 0xFFD7FFD7);
			this.put(195, 0xFFD7FFFF);
			
			this.put(196, 0xFFFF0000);
			this.put(197, 0xFFFF005F);
			this.put(198, 0xFFFF0087);
			this.put(199, 0xFFFF00AF);
			this.put(200, 0xFFFF00D7);
			this.put(201, 0xFFFF00FF);
			
			this.put(202, 0xFFFF5F00);
			this.put(203, 0xFFFF5F5F);
			this.put(204, 0xFFFF5F87);
			this.put(205, 0xFFFF5FAF);
			this.put(206, 0xFFFF5FD7);
			this.put(207, 0xFFFF5FFF);
			
			this.put(208, 0xFFFF8700);
			this.put(209, 0xFFFF875F);
			this.put(210, 0xFFFF8787);
			this.put(211, 0xFFFF87AF);
			this.put(212, 0xFFFF87D7);
			this.put(213, 0xFFFF87FF);
			
			this.put(214, 0xFFFFAF00);
			this.put(215, 0xFFFFAF5F);
			this.put(216, 0xFFFFAF87);
			this.put(217, 0xFFFFAFAF);
			this.put(218, 0xFFFFAFD7);
			this.put(219, 0xFFFFAFFF);
			
			this.put(220, 0xFFFFD700);
			this.put(221, 0xFFFFD75F);
			this.put(222, 0xFFFFD787);
			this.put(223, 0xFFFFD7AF);
			this.put(224, 0xFFFFD7D7);
			this.put(225, 0xFFFFD7FF);
			
			this.put(226, 0xFFFFFF00);
			this.put(227, 0xFFFFFF5F);
			this.put(228, 0xFFFFFF87);
			this.put(229, 0xFFFFFFAF);
			this.put(230, 0xFFFFFFD7);
			this.put(231, 0xFFFFFFFF);
			
			//blacks/greys
			this.put(232, 0xFF080808);
			this.put(233, 0xFF121212);
			this.put(234, 0xFF1C1C1C);
			this.put(235, 0xFF262626);
			this.put(236, 0xFF303030);
			this.put(237, 0xFF3A3A3A);
			this.put(238, 0xFF444444);
			this.put(239, 0xFF4E4E4E);
			this.put(240, 0xFF585858);
			this.put(241, 0xFF626262);
			this.put(242, 0xFF6C6C6C);
			this.put(243, 0xFF767676);
			this.put(244, 0xFF808080);
			this.put(245, 0xFF8A8A8A);
			this.put(246, 0xFF949494);
			this.put(247, 0xFF9E9E9E);
			this.put(248, 0xFFA8A8A8);
			this.put(249, 0xFFB2B2B2);
			this.put(250, 0xFFBCBCBC);
			this.put(251, 0xFFC6C6C6);
			this.put(252, 0xFFD0D0D0);
			this.put(253, 0xFFDADADA);
			this.put(254, 0xFFE4E4E4);
			this.put(255, 0xFFEEEEEE);
		}
	};
	
	public static HashMap<CharSequence,Integer> colormap = new HashMap<CharSequence, Integer>();
	static
	{
		colormap.put("0", 0);
		colormap.put("1", 1);
		colormap.put("2", 2);
		
		//unused
		colormap.put("3", 3);
		colormap.put("4", 4);
		
		//xterm256
		colormap.put("5", 5);
		
		//unused
		colormap.put("6", 6);
		colormap.put("7", 7);
		colormap.put("8", 8);
		colormap.put("9", 9);
		colormap.put("10", 10);
		colormap.put("11", 11);
		colormap.put("12", 12);
		colormap.put("13", 13);
		colormap.put("14", 14);
		colormap.put("15", 15);
		colormap.put("16", 16);
		colormap.put("17", 17);
		colormap.put("18", 18);
		colormap.put("19", 19);
		colormap.put("20", 20);
		colormap.put("21", 21);
		colormap.put("22", 22);
		colormap.put("23", 23);
		colormap.put("24", 24);
		colormap.put("25", 25);
		colormap.put("26", 26);
		colormap.put("27", 27);
		colormap.put("28", 28);
		colormap.put("29", 29);
		
		//standard ansi foreground colors
		colormap.put("30", 30);
		colormap.put("31", 31);
		colormap.put("32", 32);
		colormap.put("33", 33);
		colormap.put("34", 34);
		colormap.put("35", 35);
		colormap.put("36", 36);
		colormap.put("37", 37);
		//defaults.
		colormap.put("38", 38); //sterm 256
		colormap.put("39", 39);
		
		//standard ansi background
		colormap.put("40", 40);
		colormap.put("41", 41);
		colormap.put("42", 42);
		colormap.put("43", 43);
		colormap.put("44", 44);
		colormap.put("45", 45);
		colormap.put("46", 46);
		colormap.put("47", 47);
		
		//defaults
		colormap.put("48", 48); //xterm 256
		colormap.put("49", 49);
		
		//unused, except for xterm 256
		colormap.put("50", 50);
		colormap.put("51", 51);
		colormap.put("52", 52);
		colormap.put("53", 53);
		colormap.put("54", 54);
		colormap.put("55", 55);
		colormap.put("56", 56);
		colormap.put("57", 57);
		colormap.put("58", 58);
		colormap.put("59", 59);
		
		colormap.put("60", 60);
		colormap.put("61", 61);
		colormap.put("62", 62);
		colormap.put("63", 63);
		colormap.put("64", 64);
		colormap.put("65", 65);
		colormap.put("66", 66);
		colormap.put("67", 67);
		colormap.put("68", 68);
		colormap.put("69", 69);

		colormap.put("70", 70);
		colormap.put("71", 71);
		colormap.put("72", 72);
		colormap.put("73", 73);
		colormap.put("74", 74);
		colormap.put("75", 75);
		colormap.put("76", 76);
		colormap.put("77", 77);
		colormap.put("78", 78);
		colormap.put("79", 79);

		colormap.put("80", 80);
		colormap.put("81", 81);
		colormap.put("82", 82);
		colormap.put("83", 83);
		colormap.put("84", 84);
		colormap.put("85", 85);
		colormap.put("86", 86);
		colormap.put("87", 87);
		colormap.put("88", 88);
		colormap.put("89", 89);

		colormap.put("90", 90);
		colormap.put("91", 91);
		colormap.put("92", 92);
		colormap.put("93", 93);
		colormap.put("94", 94);
		colormap.put("95", 95);
		colormap.put("96", 96);
		colormap.put("97", 97);
		colormap.put("98", 98);
		colormap.put("99", 99);
		
		colormap.put("100", 100);
		colormap.put("101", 101);
		colormap.put("102", 102);
		colormap.put("103", 103);
		colormap.put("104", 104);
		colormap.put("105", 105);
		colormap.put("106", 106);
		colormap.put("107", 107);
		colormap.put("108", 108);
		colormap.put("109", 109);

		colormap.put("110", 110);
		colormap.put("111", 111);
		colormap.put("112", 112);
		colormap.put("113", 113);
		colormap.put("114", 114);
		colormap.put("115", 115);
		colormap.put("116", 116);
		colormap.put("117", 117);
		colormap.put("118", 118);
		colormap.put("119", 119);

		colormap.put("120", 120);
		colormap.put("121", 121);
		colormap.put("122", 122);
		colormap.put("123", 123);
		colormap.put("124", 124);
		colormap.put("125", 125);
		colormap.put("126", 126);
		colormap.put("127", 127);
		colormap.put("128", 128);
		colormap.put("129", 129);
		
		colormap.put("130", 130);
		colormap.put("131", 131);
		colormap.put("132", 132);
		colormap.put("133", 133);
		colormap.put("134", 134);
		colormap.put("135", 135);
		colormap.put("136", 136);
		colormap.put("137", 137);
		colormap.put("138", 138);
		colormap.put("139", 139);
		
		colormap.put("140", 140);
		colormap.put("141", 141);
		colormap.put("142", 142);
		colormap.put("143", 143);
		colormap.put("144", 144);
		colormap.put("145", 145);
		colormap.put("146", 146);
		colormap.put("147", 147);
		colormap.put("148", 148);
		colormap.put("149", 149);

		colormap.put("150", 150);
		colormap.put("151", 151);
		colormap.put("152", 152);
		colormap.put("153", 153);
		colormap.put("154", 154);
		colormap.put("155", 155);
		colormap.put("156", 156);
		colormap.put("157", 157);
		colormap.put("158", 158);
		colormap.put("159", 159);

		colormap.put("160", 160);
		colormap.put("161", 161);
		colormap.put("162", 162);
		colormap.put("163", 163);
		colormap.put("164", 164);
		colormap.put("165", 165);
		colormap.put("166", 166);
		colormap.put("167", 167);
		colormap.put("168", 168);
		colormap.put("169", 169);

		colormap.put("170", 170);
		colormap.put("171", 171);
		colormap.put("172", 172);
		colormap.put("173", 173);
		colormap.put("174", 174);
		colormap.put("175", 175);
		colormap.put("176", 176);
		colormap.put("177", 177);
		colormap.put("178", 178);
		colormap.put("179", 179);

		colormap.put("180", 180);
		colormap.put("181", 181);
		colormap.put("182", 182);
		colormap.put("183", 183);
		colormap.put("184", 184);
		colormap.put("185", 185);
		colormap.put("186", 186);
		colormap.put("187", 187);
		colormap.put("188", 188);
		colormap.put("189", 189);

		colormap.put("190", 190);
		colormap.put("191", 191);
		colormap.put("192", 192);
		colormap.put("193", 193);
		colormap.put("194", 194);
		colormap.put("195", 195);
		colormap.put("196", 196);
		colormap.put("197", 197);
		colormap.put("198", 198);
		colormap.put("199", 199);

		colormap.put("200", 200);
		colormap.put("201", 201);
		colormap.put("202", 202);
		colormap.put("203", 203);
		colormap.put("204", 204);
		colormap.put("205", 205);
		colormap.put("206", 206);
		colormap.put("207", 207);
		colormap.put("208", 208);
		colormap.put("209", 209);
		
		colormap.put("210", 210);
		colormap.put("211", 211);
		colormap.put("212", 212);
		colormap.put("213", 213);
		colormap.put("214", 214);
		colormap.put("215", 215);
		colormap.put("216", 216);
		colormap.put("217", 217);
		colormap.put("218", 218);
		colormap.put("219", 219);
		
		colormap.put("220", 220);
		colormap.put("221", 221);
		colormap.put("222", 222);
		colormap.put("223", 223);
		colormap.put("224", 224);
		colormap.put("225", 225);
		colormap.put("226", 226);
		colormap.put("227", 227);
		colormap.put("228", 228);
		colormap.put("229", 229);
		
		colormap.put("230", 230);
		colormap.put("231", 231);
		colormap.put("232", 232);
		colormap.put("233", 233);
		colormap.put("234", 234);
		colormap.put("235", 235);
		colormap.put("236", 236);
		colormap.put("237", 237);
		colormap.put("238", 238);
		colormap.put("239", 239);
		
		colormap.put("240", 240);
		colormap.put("241", 241);
		colormap.put("242", 242);
		colormap.put("243", 243);
		colormap.put("244", 244);
		colormap.put("245", 245);
		colormap.put("246", 246);
		colormap.put("247", 247);
		colormap.put("248", 248);
		colormap.put("249", 249);
		
		colormap.put("250", 250);
		colormap.put("251", 251);
		colormap.put("252", 252);
		colormap.put("253", 253);
		colormap.put("254", 254);
		colormap.put("255", 255);
		
	}


}
