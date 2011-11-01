package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;

import com.happygoatstudios.bt.window.TextTree;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class WindowToken implements Parcelable {
	private String name;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean bufferText = false;
	private TextTree buffer;
	//private String owner;
	
	public enum TYPE {
		NORMAL,
		SCRIPT
	}
	
	private TYPE type = TYPE.NORMAL;
	
	private String scriptName;
	private String pluginName;
	
	public WindowToken(String name,int x,int y,int width,int height) {
		type = TYPE.NORMAL;
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.scriptName = null;
		this.pluginName = null;
		buffer = new TextTree();
		//this.owner = owner;
	}
	
	public WindowToken(String name,int x,int y,int width,int height,String scriptName,String pluginName) {
		type = TYPE.SCRIPT;
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.scriptName = scriptName;
		this.pluginName = pluginName;
		//this.owner = owner;
	}
	

	public WindowToken(Parcel p) {
		//owner = p.readString();
		name = p.readString();
		x = p.readInt();
		y = p.readInt();
		height = p.readInt();
		width = p.readInt();
		setBufferText((p.readInt()==0) ? false : true);
		int script = p.readInt();
		if(script != 1) {
			scriptName = null;
			pluginName = null;
			type = TYPE.NORMAL;
			byte[] buf = p.createByteArray();
			buffer = new TextTree();
			try {
				buffer.addBytesImpl(buf);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.e("PARCEL","WINDOWTOKEN RECONSTITUTING: " + buffer.getBrokenLineCount() + " lines.");
			
		} else {
			scriptName = p.readString();
			pluginName = p.readString();
			type = TYPE.SCRIPT;
			
		}
	}

	/*public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public String getScriptPath() {
		return scriptPath;
	}*/

	public void setType(TYPE type) {
		this.type = type;
	}

	public TYPE getType() {
		return type;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel p, int arg1) {
		//p.writeString(owner);
		p.writeString(name);
		p.writeInt(x);
		p.writeInt(y);
		p.writeInt(height);
		p.writeInt(width);
		if(bufferText) {
			p.writeInt(1);
		} else {
			p.writeInt(0);
		}
		if(scriptName != null) {
			p.writeInt(1);
			p.writeString(scriptName);
			p.writeString(pluginName);
		} else {
			p.writeInt(0);
			Log.e("PARCEL","WINDOWTOKEN DUMPING: " + buffer.getBrokenLineCount() + " lines.");
			p.writeByteArray(buffer.dumpToBytes(true));
		}
		
	}
	public void setBuffer(TextTree buffer) {
		this.buffer = buffer;
	}

	public TextTree getBuffer() {
		return buffer;
	}
	
	public void setBufferText(boolean bufferText) {
		this.bufferText = bufferText;
	}

	public boolean isBufferText() {
		return bufferText;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getScriptName() {
		return scriptName;
	}
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}
	
	public static final Parcelable.Creator<WindowToken> CREATOR = new Parcelable.Creator<WindowToken>() {

		public WindowToken createFromParcel(Parcel arg0) {
			return new WindowToken(arg0);
		}

		public WindowToken[] newArray(int arg0) {
			return new WindowToken[arg0];
		}
	};
	
}
