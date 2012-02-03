package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.happygoatstudios.bt.service.LayoutGroup.LAYOUT_TYPE;
import com.happygoatstudios.bt.window.TextTree;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class WindowToken implements Parcelable {
	ArrayList<LayoutGroup> layouts;
	
	private String name;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean bufferText = false;
	private int id;
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
		//portraitParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		//landscapeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		layouts = new ArrayList<LayoutGroup>();
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
		buffer = new TextTree();
		//portraitParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		//landscapeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		layouts = new ArrayList<LayoutGroup>();
	}
	
	public WindowToken copy() {
		WindowToken w = null;
		if(this.scriptName == null) {
			w = new WindowToken(this.name,this.x,this.y,this.width,this.height);
		} else {
			w = new WindowToken(this.name,this.x,this.y,this.width,this.height,this.scriptName,this.pluginName);
		}
		/*w.landscapeParams = new RelativeLayout.LayoutParams(this.landscapeParams);
		int[] rules = this.landscapeParams.getRules();
		for(int i=0;i<rules.length;i++) {
			if(rules[i] != 0) {
				w.landscapeParams.addRule(i,rules[i]);
			}
		}
		w.portraitParams = new RelativeLayout.LayoutParams(this.portraitParams);
		rules = this.portraitParams.getRules();
		for(int i=0;i<rules.length;i++) {
			if(rules[i] != 0) {
				w.portraitParams.addRule(i,rules[i]);
			}
		}*/
		return w;
	}
	

	public WindowToken(Parcel p) {
		//owner = p.readString();
		name = p.readString();
		x = p.readInt();
		y = p.readInt();
		height = p.readInt();
		width = p.readInt();
		layouts = new ArrayList<LayoutGroup>();
		//layout paramters.
		int numLayoutGroups = p.readInt();
		for(int i=0;i<numLayoutGroups;i++) {
			LayoutGroup g = new LayoutGroup();
			int layoutType = p.readInt();
			switch(layoutType) {
			case 0:
				g.type = LAYOUT_TYPE.SMALL;
				break;
			case 1:
				g.type = LAYOUT_TYPE.NORMAL;
				break;
			case 2:
				g.type = LAYOUT_TYPE.LARGE;
				break;
			case 3:
				g.type = LAYOUT_TYPE.XLARGE;
				break;
			}
			
			int pWidth = p.readInt();
			int pHeight = p.readInt();
			int pMarginTop = p.readInt();
			int pMarginLeft = p.readInt();
			int pMarginRight = p.readInt();
			int pMarginBottom = p.readInt();
	
			RelativeLayout.LayoutParams portraitParams = g.getPortraitParams();
			portraitParams.height = pHeight;
			portraitParams.width = pWidth;
			portraitParams.setMargins(pMarginLeft, pMarginTop, pMarginRight, pMarginBottom);
			//int numrules = p.readInt();
			boolean done = false;
			while(!done) {
				int rule = p.readInt();
				if(rule > -1) {
					int option = p.readInt();
					portraitParams.addRule(rule, option);
				} else {
					done = true;
				}
			}
			
			
			int lWidth = p.readInt();
			int lHeight = p.readInt();
			int lMarginTop = p.readInt();
			int lMarginLeft = p.readInt();
			int lMarginRight = p.readInt();
			int lMarginBottom = p.readInt();
			RelativeLayout.LayoutParams landscapeParams = g.getLandscapeParams();
			landscapeParams.height = lHeight;
			landscapeParams.width = lWidth;
			landscapeParams.setMargins(lMarginLeft, lMarginTop, lMarginRight, lMarginBottom);
			done = false;
			while(!done) {
				int rule = p.readInt();
				if(rule > -1) {
					int option = p.readInt();
					landscapeParams.addRule(rule, option);
				} else {
					done = true;
				}
			}
		}
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
			byte[] buf = p.createByteArray();
			buffer = new TextTree();
			type = TYPE.SCRIPT;
			
			try {
				buffer.addBytesImpl(buf);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
		
		//write out layout paramters, portrait first.
		for(int j=0;j<layouts.size();j++) {
			LayoutGroup g = layouts.get(j);
			switch(g.type) {
			case SMALL:
				p.writeInt(0);
				break;
			case NORMAL:
				p.writeInt(1);
				break;
			case LARGE:
				p.writeInt(2);
				break;
			case XLARGE:
				p.writeInt(3);
				break;
			}
			
			RelativeLayout.LayoutParams portraitParams = g.getPortraitParams();
			RelativeLayout.LayoutParams landscapeParams = g.getLandscapeParams();
			p.writeInt(portraitParams.width);
			p.writeInt(portraitParams.height);
			p.writeInt(portraitParams.topMargin);
			p.writeInt(portraitParams.leftMargin);
			p.writeInt(portraitParams.rightMargin);
			p.writeInt(portraitParams.bottomMargin);
			
			int[] rules = portraitParams.getRules();
			for(int i=0;i<rules.length;i++) {
				if(rules[i] != 0) {
					p.writeInt(i);
					p.writeInt(rules[i]);
				}
			}
			p.writeInt(-1);
			
			p.writeInt(landscapeParams.width);
			p.writeInt(landscapeParams.height);
			p.writeInt(landscapeParams.topMargin);
			p.writeInt(landscapeParams.leftMargin);
			p.writeInt(landscapeParams.rightMargin);
			p.writeInt(landscapeParams.bottomMargin);
			
			rules = null;
			rules = landscapeParams.getRules();
			for(int i=0;i<rules.length;i++) {
				if(rules[i] != 0) {
					p.writeInt(i);
					p.writeInt(rules[i]);
				}
			}
			p.writeInt(-1);
		}
		if(bufferText) {
			p.writeInt(1);
		} else {
			p.writeInt(0);
		}
		if(scriptName != null) {
			p.writeInt(1);
			p.writeString(scriptName);
			p.writeString(pluginName);
			p.writeByteArray(buffer.dumpToBytes(true));
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

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static final Parcelable.Creator<WindowToken> CREATOR = new Parcelable.Creator<WindowToken>() {

		public WindowToken createFromParcel(Parcel arg0) {
			return new WindowToken(arg0);
		}

		public WindowToken[] newArray(int arg0) {
			return new WindowToken[arg0];
		}
	};
	

	

	
	//public void setBuffer(TextTree buffer) {
	//	this.buffer = buffer;
	//}
	
}
