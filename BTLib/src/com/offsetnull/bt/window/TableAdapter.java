package com.offsetnull.bt.window;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class TableAdapter extends BaseAdapter implements ListAdapter {
	private LuaState L = null;
	private String table = null;
	private String viewFunction = null;
	
	public TableAdapter(LuaState L,String table,String viewFunction) {
		this.L = L;
		this.table = table;
		this.viewFunction = viewFunction;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		L.getGlobal(table);
		if(L.isTable(L.getTop())) {
			int ret = L.LgetN(L.getTop());
			L.pop(1);
			return ret;
		}
		
		return 0;
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		L.getGlobal("debug");
		L.getField(L.getTop(), "traceback");
		L.remove(-2);
		
		L.getGlobal(viewFunction);
		if(L.isFunction(L.getTop())) {
			L.pushString(Integer.toString(position));
			if(convertView == null) {
				L.pushNil();
			} else {
				L.pushJavaObject(convertView);
			}
			parent = null;
			
			int ret = L.pcall(2, 1, -4);
			if(ret != 0) {
				Log.e("LUAWINDOW","Error in TableView:getView: "+L.getLuaObject(-1).getString());
			} else {
				LuaObject obj = L.getLuaObject(L.getTop());
				if(obj.isJavaObject()) {
					try {
						View tmp = (View)obj.getObject();
						return tmp;
					} catch (LuaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}			
		}
		return null;
	}

	

	

	
	
}
