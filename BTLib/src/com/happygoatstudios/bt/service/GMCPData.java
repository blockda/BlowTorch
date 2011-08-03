package com.happygoatstudios.bt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

public class GMCPData {

	HashMap<String,Object> data = null;
	Handler reporter = null;
	StatusBarWatchList list = null;
	public GMCPData(Handler reporter) {
		data = new HashMap<String,Object>();
		watchList = new HashMap<String,Boolean>();
		list = new StatusBarWatchList();
		/*watchList.put("char.vitals",falseVal);
		watchList.put("char.maxstats",falseVal);
		watchList.put("char.status",falseVal);*/
		for(String str : list.data) {
			watchList.put(str, falseVal);
		}
		this.reporter = reporter;
	}
	
	Boolean falseVal = new Boolean(false);
	Boolean trueVal = new Boolean(true);
	
	private HashMap<String,Boolean> watchList = null;
	
	public void absorb(String module,JSONObject o) {
		for(String key : watchList.keySet()) {
			watchList.put(key,falseVal);
		}
		putData(module,o,data);
		
		boolean didTrigger = false;
		for(String key : watchList.keySet()) {
			if(watchList.get(key) == true) {
				//we have a hit.
				didTrigger = true;
			}
		}
		if(didTrigger) {
			Log.e("GMCP","WATCH LIST TRIGGERED");
			for(int i =0;i<list.data.size();i++) {
				Log.e("GMCPDUMP",list.data.get(i) + ": " + this.get(list.data.get(i)));
			}
		}
	};
	
	public void putData(String module,JSONObject object,HashMap<String,Object> node) {
		String key = "";
		String rest = "";
		int index = module.indexOf(".");
		
		if(index > 0) {
			key = module.substring(0, index);
			rest = module.substring(index+1,module.length());
		} else {
			//means that it wasn't found or it lead off with, go with key
			key = module;
		}
		
		if(node.containsKey(key)) {
			//grab it and insert the rest of the data, if there is no rest, dump the stuff into the current map.
			if(rest.equals("")) {
				Object o = node.get(key);
				if(o == null) { Log.e("GMCP","WARNING! KEY: " + key + " is null!"); } //holy god. never should this happen.
				if(!(o instanceof HashMap<?,?>)) { Log.e("GMCP","WARNING! KEY: " + key + " is not a hashmap!"); }
				HashMap<String,Object> map = (HashMap<String,Object>)o;
				//Iterator<String> keys = object.keys();
				insertData(object, map,module);
				//for(String key : object.keys())
			} else {
				//still more modules.
				putData(rest,object,(HashMap<String,Object>)node.get(key));
			}
		} else {
			if(rest.equals("")) {
				HashMap<String,Object> newnode = new HashMap<String,Object>();
				node.put(key, newnode);
				//putData(rest,object,newnode);
				insertData(object, newnode,module);
			} else {
				HashMap<String,Object> newnode = new HashMap<String,Object>();
				node.put(key, newnode);
				putData(rest,object,newnode);
			}
			
			
		}
		
		
//		Log.e("GMCP","MODULE" + parts[0] + " rest: " + parts[1]);
//		//discover if map exists
//		if(node.containsKey(parts[0])) {
//			putData(parts[1])
//		}
	}
	
	public Object get(String path) {
		return getData(path,data);
	}
	
	private Object getData(String path,HashMap<String,Object> node) {
		int index = path.indexOf(".");
		String key = "";
		String rest = "";
		if(index > 0) {
			key = path.substring(0, index);
			rest = path.substring(index+1,path.length());
			
		} else {
			key = path;
		}
		
		if(node.containsKey(key)) {
			if(rest.equals("")) {
				//HashMap<String,Object> tmp = (HashMap<String,Object>)node.get(key);
				return node.get(key);
			} else {
				HashMap<String,Object> tmp = (HashMap<String,Object>)node.get(key);
				return getData(rest,tmp);
			}
		} else {
			return null; //failed so hard.
		}
		
		
	}

	private void insertData(JSONObject object, HashMap<String, Object> node,String completePath) {
		Iterator<String> keys = object.keys();
		while(keys.hasNext()) {
			String tmp = keys.next();
			int intVal = 0;
			String strVal = "";
			boolean anInt = false;
			try {
				intVal = object.getInt(tmp);
				anInt = true;
			} catch (JSONException e) {
				
			}
			
			if(node.containsKey(tmp)) {
				//boolean trigger = false;
				Object obj = node.get(tmp);
				if(obj != null && obj instanceof Integer) {
					if(!anInt) { Log.e("GMCP","WARNING: REPLACING KEY " + tmp + " stored value is an int, incoming data: " + strVal + " is not."); }
					Integer value = (Integer)obj;
					if(value.intValue() != intVal) {
						node.put(tmp, intVal);
						watchList.put(completePath + "." + tmp, trueVal);
						//trigger = true;
					}
					
				} else {
					String str = (String)node.get(tmp);
					if(str != null && !str.equals(strVal)) {
						node.put(tmp, new String(strVal));
						watchList.put(completePath + "." + tmp, trueVal);
						//trigger = true;
					} else {
						if(anInt) {
							node.put(tmp, new Integer(intVal));
						} else {
							node.put(tmp, new String(strVal));
						}
					}
				}
				/*if(trigger) {
					if(watchList.containsKey(completePath + "." + key)) {
						watchList.put(completePath + "." + key, trueVal);
					}
				}*/
			} else {
				if(anInt) {
					node.put(tmp, new Integer(intVal));
				} else {
					node.put(tmp, strVal);
				}
			}
		}
	}
	
	public void dumpCache() {
		int tabs = 0;
		//ArrayList<String> keys = new ArrayList<String>();
		//Arrays.sort
		String currentPath = "";
		dumpNode(currentPath,data);
	}
	
	public void dumpNode(String path,HashMap<String,Object> map) {
		String cur = new String(path);
		//cur += path;
		
		for(String key : map.keySet()) {
			Object o = map.get(key);
			if(o instanceof HashMap<?,?>) {
				HashMap<String,Object> tmp = (HashMap<String, Object>)o;
				//dump hash
				if(path.equals("")) {
					dumpNode(key,tmp);
				} else {
					dumpNode(cur + "." + key,tmp);
				}
				
			} else {
				//dump value
				Log.e("GMCP",cur + "." + key + ": " + map.get(key));
			}
		}
	}
	
}
