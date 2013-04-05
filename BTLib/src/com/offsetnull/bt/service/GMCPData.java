/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

/** The implementation of the GMCP Table. It keeps track of the data using
 *  hashmaps to store tables at string paths.
 */
public class GMCPData {

	/** The actual gmcp table. */
	private HashMap<String, Object> mData = null;

	/** Constructor. */
	public GMCPData() {
		mData = new HashMap<String, Object>();
	}
	
	/** Absorbs a JSONObject and all associated data into the module path.
	 * 
	 * @param module The module to start adding data to, e.g. char.vitals.
	 * @param o The JSONObject to add.
	 */
	public final void absorb(final String module, final JSONObject o) {

		putData(module, o, mData, "");
	
	};
	
	/** The recursive method of adding a JSONObject to the gmcp table.
	 * 
	 * @param module The target module path.
	 * @param object The object to add.
	 * @param node The current working node of the gmcp table that is to be added to.
	 * @param previous The previous path that was processed.
	 */
	@SuppressWarnings("unchecked")
	public final void putData(final String module, final JSONObject object, final HashMap<String, Object> node, final String previous) {
		String key = "";
		String rest = "";
		String dotChar = "";
		if (!previous.equals("")) {
			dotChar = ".";
		}
		int index = module.indexOf(".");
		
		if (index > 0) {
			key = module.substring(0, index);
			rest = module.substring(index + 1, module.length());
		} else {
			//means that it wasn't found or it lead off with, go with key
			key = module;
		}
		
		if (node.containsKey(key)) {
			//grab it and insert the rest of the data, if there is no rest, dump the stuff into the current map.
			if (rest.equals("")) {
				Object o = node.get(key);
				if (o == null) { Log.e("GMCP", "WARNING! KEY: " + key + " is null!"); } //holy god. never should this happen.
				if (!(o instanceof HashMap<?, ?>)) { Log.e("GMCP", "WARNING! KEY: " + key + " is not a hashmap!"); }
				HashMap<String, Object> map = (HashMap<String, Object>) o;
				map.clear();
				insertData(object, map, previous + dotChar + key);
			} else {
				//still more modules.
				putData(rest, object, (HashMap<String, Object>) node.get(key), previous + dotChar + key);
			}
		} else {
			if (rest.equals("")) {
				HashMap<String, Object> newnode = new HashMap<String, Object>();
				node.put(key, newnode);
				insertData(object, newnode, previous + dotChar + key);
			} else {
				HashMap<String, Object> newnode = new HashMap<String, Object>();
				node.put(key, newnode);
				putData(rest, object, newnode, previous + dotChar + key);
			}
			
			
		}
		
	}
	
	/** Gets the gmcp data for a given path.
	 * 
	 * @param path The path to fetch, e.g. char.vitals.hp.
	 * @return The gmcp data for the node.
	 */
	public final Object get(final String path) {
		return getData(path, mData);
	}
	
	/** The actual implementation of the getData(...) function.
	 * 
	 * @param path The path to fetch, e.g. char.vitals.hp.
	 * @param node The gmcp data table to fetch, the recursive function starts with the top level table.
	 * @return The gmcp data for the node.
	 */
	private Object getData(final String path, final HashMap<String, Object> node) {
		int index = path.indexOf(".");
		
		String key = "";
		String rest = "";
		if (index > 0) {
			key = path.substring(0, index);
			rest = path.substring(index + 1, path.length());
			
		} else {
			key = path;
		}
		
		if (node.containsKey(key)) {
			if (rest.equals("")) {
				return node.get(key);
			} else {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> tmp = (HashMap<String, Object>) node.get(key);
				return getData(rest, tmp);
			}
		} else {
			return null; //failed so hard.
		}
		
		
	}

	/** Inserts data into the gmcp table at a specific location.
	 * 
	 * @param object The JSONObject to insert.
	 * @param node The current working node of the gmcp table.
	 * @param completePath The complete path of the target location.
	 */
	private void insertData(final JSONObject object, final HashMap<String, Object> node, final String completePath) {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = object.keys();
		while (keys.hasNext()) {
			String tmp = keys.next();
			int intVal = 0;
			String strVal = "";
			boolean anInt = false;
			boolean skip = false;
			try {
				
				JSONObject sub = object.getJSONObject(tmp);
				if (node.containsKey(tmp)) {
					node.remove(tmp);
				}
				HashMap<String, Object> newnode = new HashMap<String, Object>();
				node.put(tmp, newnode);
				insertData(sub, newnode, completePath + "." + tmp);
				//return;
				skip = true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (!skip) {
				try {
					intVal = object.getInt(tmp);
					anInt = true;
				} catch (JSONException e) {
					try {
						strVal = object.getString(tmp);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
				
				
				if (anInt) {
					node.put(tmp, Integer.valueOf(intVal));
				} else {
					node.put(tmp, new String(strVal));
				}
			}
			
		}
	}
	
	/** Debug function, dumps the whole gmcp table to the system log. */
	public final void dumpCache() {
		String currentPath = "";
		dumpNode(currentPath, mData);
	}
	
	/** Debug function, the recursive part of the gmcp table dumping routine. 
	 * 
	 * @param path The current path.
	 * @param map The current node of the gmcp table.
	 */
	public final void dumpNode(final String path, final HashMap<String, Object> map) {
		String cur = new String(path);

		for (String key : map.keySet()) {
			Object o = map.get(key);
			if (o instanceof HashMap<?, ?>) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> tmp = (HashMap<String, Object>) o;
				//dump hash
				if (path.equals("")) {
					dumpNode(key, tmp);
				} else {
					dumpNode(cur + "." + key, tmp);
				}
				
			} else {
				//dump value
				Log.e("GMCP", cur + "." + key + ": " + map.get(key).toString());
			}
		}
	}

	/** Gets the map part of the gmcp table.
	 * 
	 * @param path The target path to fetch, e.g. char.vitals.hp.
	 * @return The map of the given node, null of path doesn't exist.
	 */
	public final HashMap<String, Object> getTable(final String path) {
		String[] parts = path.split("\\.");
		String workingPath = parts[0];
		return findNextTable(workingPath, parts, 0, mData);
	}
	
	/** Recursive part of the gmcp table finding routine.
	 * 
	 * @param key The current working path.
	 * @param parts The exploded path in array form.
	 * @param pIndex The current level of the routine.
	 * @param node The current working node of the gmcp table.
	 * @return The gmcp table at the target path.
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> findNextTable(final String key, final String[] parts, final int pIndex, final HashMap<String, Object> node) {
		int index = pIndex;
		if (node.containsKey(key)) {
			if (index == parts.length - 1) {
				//this is the table we asked for
				return (HashMap<String, Object>) node.get(key);
			} else {
				index = index + 1;
				return findNextTable(parts[index], parts, index, (HashMap<String, Object>) node.get(key));
			}
		} else {
			return null;
		}
		//return null;
	}
	
}
