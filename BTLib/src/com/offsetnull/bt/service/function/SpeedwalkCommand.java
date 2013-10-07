package com.offsetnull.bt.service.function;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;

import android.os.RemoteException;

import com.offsetnull.bt.service.Connection;
import com.offsetnull.bt.speedwalk.DirectionData;

public class SpeedwalkCommand extends SpecialCommand {
	
	private HashMap<String,DirectionData> mDirections = null;
	private com.offsetnull.bt.service.Connection.Data mData = null;
	
	public SpeedwalkCommand(HashMap<String,DirectionData> directions,com.offsetnull.bt.service.Connection.Data data) {
		this.commandName = "run";
		mDirections = directions;
		mData = data;
	}
	
	public void setDirections(HashMap<String,DirectionData> directions) {
		mDirections = directions;
	}
	public Object execute(Object o,Connection c) {
		String str = (String)o;
		
		Character cr = new Character((char)13);
		Character lf = new Character((char)10);
		String crlf = cr.toString() + lf.toString();
		//str will be of the form, 3d2enewsnu3d32wijkl
		//direction ordinals are now configurable.
		
		if(str.equals("") || str.equals(" ")) {
			c.sendDataToWindow(getErrorMessage("Speedwalk (run) special command usage:",".run directions\n" +
					"Direction ordinal to command mappings are editable, press MENU->More->Speedwalk Configuration for more info. The default mapping is as follows:\n" +
					" n: north\n e: east\n s: south\n w: west\n u: up\n d: down\n h: northwest\n j: northeast\n k: southwest\n l: southeast\n"+
					"directions may be prefeced with an integer value to run that many times.\n" +
					"Commands may be inserted into the direction stream with commas,\n" +
					"directions may be resumed by entering another comma followed by directions.\n" +
					"Example:\n" +
					"\".run 3desw2n\", will send d;d;d;e;s;w;n;n to the server.\n" +
					"\".run jlk3n3j\", will send se;nw;sw;n;n;n;se;se;se to the server.\n"+
					"\".run 3ds,open door,3w\" will send d;d;d;s;open door;w;w;w to the server.\n"));
			return null;
			
		}

		StringBuffer buf = new StringBuffer();
		boolean commanding = false;
		LinkedList<Integer> runtable = new LinkedList<Integer>();
		for(int i=0;i<str.length();i++) {
			char theChar = str.charAt(i);
			String bit = String.valueOf(theChar);
			if(commanding) {
				if(bit.equals(",")) {
					commanding = false;
					buf.append(crlf);
				} else {
					buf.append(bit);
				}
			} else {
				
			
				try {
					int num = Integer.parseInt(bit);
					runtable.add(num);
					//place += 1;
					//runlength = (runlength *10) + runlength * num;
				} catch (NumberFormatException e) {
					//got exception, this is a direction or an invalid character.
					boolean valid = false;
					String respString = "";
					
					//make "theChar" a string
					String testVal = Character.toString(theChar);
					if(testVal.equals(",")) {
						commanding = true;
						buf.append(crlf);
					} else {
						//check if the testVal has a mapping in the table
						if(mDirections.containsKey(testVal)) {
							valid = true;
							respString = mDirections.get(testVal).getCommand();
						}
					}
					
					
					/*switch(theChar) {
					case 'n':
						respString = "n";
						valid = true;
						break;
					case 'e':
						respString = "e";
						valid = true;
						break;
					case 's':
						respString = "s";
						valid = true;
						break;
					case 'w':
						respString = "w";
						valid = true;
						break;
					case 'u':
						respString = "u";
						valid = true;
						break;
					case 'd':
						respString = "d";
						valid = true;
						break;
					case 'h':
						respString = "ne";
						valid = true;
						break;
					case 'j':
						respString = "se";
						valid = true;
						break;
					case 'k':
						respString = "sw";
						valid = true;
						break;
					case 'l':
						respString = "nw";
						valid = true;
						break;
					case ',':
						commanding = true;
						buf.append(crlf);
						break;
					default:
						
					
					}*/
					
					if(valid) {
						//compute the run length.
						int run = 1;
						int tmpPlace = runtable.size()-1;
						if(runtable.size() > 0) {
							run = 0;
							for(Integer tmp : runtable) {
								run += Math.pow(10,tmpPlace) * tmp;
								tmpPlace--;
							}
						}
						
						for(int j=0;j<run;j++) {
							//if(j == run-1) {
							//	buf.append(respString);
							//} else {
								buf.append(respString+crlf);
							//}
						}
						
						runtable.clear();
						
					} else if(!valid && !commanding) {
						//bail with error,
						int errlength = i + 5;
						StringBuffer tmpb = new StringBuffer();
						for(int a=0;a<errlength;a++) {
							tmpb.append("-");
						}
						tmpb.append("^");
						c.sendDataToWindow((getErrorMessage("Invalid direction in command:","."+commandName + " " +str+"\n" +
								tmpb.toString() + "\n" + 
								"At location " + errlength + ", " + bit)));
						return null;
					}
				}
			}
		}
		
		//mData = new com.offsetnull.bt.service.Connection.Data();
		String cmd = buf.toString();
		
		mData.setCmdString(cmd.substring(0, cmd.length()-2)); //strip trailing crlf
		mData.setVisString(".run " + str);
		
		return mData;
	}
	
	public class Data {
		public String cmdString;
		public String visString;
		public Data() {
			cmdString = "";
			visString = "";
		}
	}
}
