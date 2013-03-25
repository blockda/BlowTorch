package com.offsetnull.bt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.keplerproject.luajava.LuaState;

import com.offsetnull.bt.window.StatusGroupData;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GMCPData {

	HashMap<String,Object> data = null;
	Handler reporter = null;
	//LuaState L = null;
	public GMCPData(Handler reporter) {
		data = new HashMap<String,Object>();
		//watchList = new HashMap<String,Boolean>();
		/*watchList.put("char.vitals",falseVal);
		watchList.put("char.maxstats",falseVal);
		watchList.put("char.status",falseVal);*/
		//for(String str : list.data) {
		//	watchList.put(str, falseVal);
		//}
		this.reporter = reporter;
		
	}
	
	Boolean falseVal = new Boolean(false);
	Boolean trueVal = new Boolean(true);
	
	//private HashMap<String,Boolean> watchList = null;
	
	public void absorb(String module,JSONObject o) {
		//for(String key : watchList.keySet()) {
		//	watchList.put(key,falseVal);
		//}
		putData(module,o,data,"");
		
		
	
//		StatusGroupData data = new StatusGroupData();
//		
//		boolean enemyZero = false;
//		for(int i =0;i<list.data.size();i++) {
//			//Log.e("GMCPDUMP",list.data.get(i) + ": " + this.get(list.data.get(i)));
//			//data.addInt((this.get(list.data.get(i)));
//			if(list.data.get(i).equals("char.status.enemy")) {
//				//if(this.get(list.data.get(i)).equals("")) {
//				if(this.get(list.data.get(i)) != null && this.get(list.data.get(i)).equals("")) {
//					enemyZero = true;
//				}
//			}
//		}
//		
//		int hp,mp,maxhp,maxmana,enemy = 0;
//		//Integer ihp,imp,imaxhp,imaxmana = 0;
//		hp = (this.get("char.vitals.hp") == null) ? 100 : (Integer)this.get("char.vitals.hp");
//		mp = (this.get("char.vitals.mana") == null) ? 100 : (Integer)this.get("char.vitals.mana");
//		maxhp = (this.get("char.maxstats.maxhp") == null) ? 100 : (Integer)this.get("char.maxstats.maxhp");
//		maxmana = (this.get("char.maxstats.maxmana") == null) ? 100 : (Integer)this.get("char.maxstats.maxmana");
//		enemy = (this.get("char.status.enemypct") == null) ? 0 : (Integer)this.get("char.status.enemypct");
//		
	};
	
	public void putData(String module,JSONObject object,HashMap<String,Object> node,String previous) {
		String key = "";
		String rest = "";
		String dotChar = "";
		if(!previous.equals("")) {
			dotChar = ".";
		}
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
				map.clear();
				
				insertData(object, map,previous+dotChar+key);
				//if(watchList.containsKey(previous+dotChar+key)) {
				//	watchList.put(previous+dotChar+key, true);
				//}
				//for(String key : object.keys())
			} else {
				//still more modules.
				putData(rest,object,(HashMap<String,Object>)node.get(key),previous+dotChar+key);
			}
		} else {
			if(rest.equals("")) {
				HashMap<String,Object> newnode = new HashMap<String,Object>();
				node.put(key, newnode);
				//putData(rest,object,newnode);
				
				insertData(object, newnode,previous+dotChar+key);
				//if(watchList.containsKey(previous+dotChar+key)) {
				//	watchList.put(previous+dotChar+key, true);
				//}
			} else {
				HashMap<String,Object> newnode = new HashMap<String,Object>();
				node.put(key, newnode);
				putData(rest,object,newnode,previous+dotChar+key);
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
			boolean skip = false;
			//if(object.)
			try {
				
				JSONObject sub = object.getJSONObject(tmp);
				//if we are here it means we have a sub array.
				//Log.e("GMCP","RE-RECURSING FOR GMCP KEY: " + tmp);
				if(node.containsKey(tmp)) {
					node.remove(tmp);
				}
				HashMap<String,Object> newnode = new HashMap<String,Object>();
				node.put(tmp, newnode);
				insertData(sub,newnode,completePath+"."+tmp);
				//return;
				skip = true;
			} catch(JSONException e) {
				//not a sub array
			}
			if(!skip) {
				try {
					intVal = object.getInt(tmp);
					anInt = true;
				} catch (JSONException e) {
					try {
						strVal = object.getString(tmp);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				
				if(anInt) {
					node.put(tmp, new Integer(intVal));
					//if(watchList.containsKey(completePath + "." + tmp)) {
					//	watchList.put(completePath + "." + tmp, trueVal);
					//}
				} else {
					node.put(tmp, new String(strVal));
					//if(watchList.containsKey(completePath + "." + tmp)) {
					//	watchList.put(completePath + "." + tmp, trueVal);
					//}
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
				Log.e("GMCP",cur + "." + key + ": " + map.get(key).toString());
			}
		}
	}

	public HashMap<String, Object> getTable(String path) {
		// TODO Auto-generated method stub
		String[] parts = path.split("\\.");
		//Log.e("LUA","GETTING GMCP TABLE FOR: " + path + " broken piece has " + parts.length + " parts.");
		String working_path = parts[0];
		
		return findNextTable(working_path,parts,0,data);
		
		// null;
	}
	
	private HashMap<String,Object> findNextTable(String key,String[] parts,int index,HashMap<String,Object> node) {
		
		if(node.containsKey(key)) {
			if(index == parts.length-1) {
				//this is the table we asked for
				return (HashMap<String,Object>)node.get(key);
			} else {
				index = index+1;
				return findNextTable(parts[index],parts,index,(HashMap<String,Object>)node.get(key));
			}
		} else {
			return null;
		}
		//return null;
	}
	
	/*private void dumpNodeToLua(String key,HashMap<String,Object> node) {
		if(!key.equals("")) {
			this.L.pushString(key);
		}
		this.L.newTable();
		
		for(String tmp : node.keySet()) {
			
			Object o = node.get(tmp);
			if(o instanceof HashMap) {
				//we recurse
				//Log.e("GMCPDUMP","DUMPING SUB TABLE");
				dumpNodeToLua(tmp,(HashMap<String,Object>)o);
			} else {
				this.L.pushString(tmp);
				if(o instanceof String) {
					this.L.pushString((String)o);
				}
				if(o instanceof Integer) {	
					//TODO: apparantly there is no _pushInteger implementation. wtfxors.
					this.L.pushString(((Integer)o).toString());
				}
				this.L.setTable(-3);
			}
		}
		if(!key.equals("")) {
			this.L.setTable(-3);
		}
		//this.L.setTable(-3);
		
		
	}*/
	
}
