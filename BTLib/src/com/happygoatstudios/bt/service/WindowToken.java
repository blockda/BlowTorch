package com.happygoatstudios.bt.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.happygoatstudios.bt.service.LayoutGroup.LAYOUT_TYPE;
import com.happygoatstudios.bt.window.TextTree;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class WindowToken implements Parcelable {
	HashMap<LayoutGroup.LAYOUT_TYPE,LayoutGroup> layouts;
	
	private String name;
	//private int x;
	//private int y;
	//private int width;
	//private int height;
	private boolean bufferText = false;
	private int id;
	private TextTree buffer;
	//private String owner;
	
	/*public enum TYPE {
		NORMAL,
		SCRIPT
	}*/
	
	//private TYPE type = TYPE.NORMAL;
	
	private String scriptName;
	private String pluginName;
	
	public WindowToken() {
		name = "";

		//type = TYPE.NORMAL;
		layouts = new HashMap<LayoutGroup.LAYOUT_TYPE,LayoutGroup>(4);
	}
	
	/*public WindowToken(String name,int x,int y,int width,int height) {
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
	}*/
	
	public WindowToken(String name,String scriptName,String pluginName) {
		//type = TYPE.SCRIPT;
		this.name = name;

		this.scriptName = scriptName;
		this.pluginName = pluginName;
		//this.owner = owner;
		buffer = new TextTree();
		//portraitParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		//landscapeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		layouts = new HashMap<LayoutGroup.LAYOUT_TYPE,LayoutGroup>(4);
	}
	
	public WindowToken copy() {
		WindowToken w = null;
		//if(this.scriptName == null) {
		//	w = new WindowToken(this.name,this.x,this.y,this.width,this.height);
		//} else {
		w = new WindowToken(this.name,this.scriptName,this.pluginName);
		//}
		w.id = this.id;
		
		int numgroups = this.layouts.size();
		for(LayoutGroup g : this.layouts.values()) {
			//LayoutGroup g = layouts.get(i);
			LayoutGroup newg = new LayoutGroup();
			newg.type = g.type;
			RelativeLayout.LayoutParams pparams = g.getPortraitParams();
			RelativeLayout.LayoutParams newportraitparams = new RelativeLayout.LayoutParams(pparams.width,pparams.height);
			newportraitparams.setMargins(pparams.leftMargin, pparams.topMargin, pparams.rightMargin, pparams.bottomMargin);
			int[] rules = pparams.getRules();
			for(int j=0;j<rules.length;j++) {
				if(rules[j] != 0) {
					newportraitparams.addRule(j,rules[j]);
				}
			}
			
			RelativeLayout.LayoutParams lparams = g.getLandscapeParams();
			RelativeLayout.LayoutParams newlandscapeparams = new RelativeLayout.LayoutParams(lparams.width,lparams.height);
			newportraitparams.setMargins(lparams.leftMargin, lparams.topMargin, lparams.rightMargin, lparams.bottomMargin);
			rules = null;
			rules = lparams.getRules();
			for(int j=0;j<rules.length;j++) {
				if(rules[j] != 0) {
					newlandscapeparams.addRule(j,rules[j]);
				}
			}
			
			newg.setLandscapeParams(newlandscapeparams);
			newg.setPortraitParams(newportraitparams);
			
			w.layouts.put(newg.type,newg);
			//pparams = g.getPortraitParams();
			//newg.setPortraitParams(g.clone());
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
		if(name.equals("chats")) {
			long ssfd = System.currentTimeMillis();
			ssfd = ssfd +10;
		}
		id = p.readInt();
		Log.e("TOKEN","PARCEL: READING WINDOW WITH ID:" + id);
		layouts = new HashMap<LayoutGroup.LAYOUT_TYPE,LayoutGroup>();
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
			
			layouts.put(g.type, g);
		}
		setBufferText((p.readInt()==0) ? false : true);
		int script = p.readInt();
		if(script != 1) {
			scriptName = null;
			pluginName = null;
		} else {
			scriptName = p.readString();
			pluginName = p.readString();
		}
		byte[] buf = p.createByteArray();
		buffer = new TextTree();
		//type = TYPE.SCRIPT;
		
		try {
			buffer.addBytesImpl(buf);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public String getScriptPath() {
		return scriptPath;
	}*/


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
		Log.e("TOKEN","PARCEL: WRITING WINDOW WITH ID:" + id);
		p.writeInt(id);
		Set<LayoutGroup.LAYOUT_TYPE> keySet = layouts.keySet();
		p.writeInt(keySet.size());
		for(LayoutGroup g : layouts.values()) {
			//LayoutGroup.LAYOUT_TYPE = keySet.
			//LayoutGroup g = layouts.get(j);
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
	
	public HashMap<LayoutGroup.LAYOUT_TYPE,LayoutGroup> getLayouts() {
		return layouts;
	}
	
	//public void setType(LayoutGrou)

	public static final Parcelable.Creator<WindowToken> CREATOR = new Parcelable.Creator<WindowToken>() {

		public WindowToken createFromParcel(Parcel arg0) {
			return new WindowToken(arg0);
		}

		public WindowToken[] newArray(int arg0) {
			return new WindowToken[arg0];
		}
	};

	public void resetToDefaults() {
		name = "";
		id = 0;
		bufferText = false;
		layouts.clear();
	}

	public LayoutParams getLayout(int size, boolean landscape) {
		
		switch(size) {
		case Configuration.SCREENLAYOUT_SIZE_SMALL:
			//tmp.setLayoutParams(w.getLayout(Configuration.SCREENLAYOUT_SIZE_SMALL,landscape));
			if(layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
			//break;
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			if(layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
			//break;
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			if(layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
			
		case Configuration.SCREENLAYOUT_SIZE_XLARGE:
			if(layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.XLARGE).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.LARGE).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.NORMAL).getPortraitParams();
				}
			} else if(layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL) != null) {
				if(landscape) {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getLandscapeParams();
				} else {
					return layouts.get(LayoutGroup.LAYOUT_TYPE.SMALL).getPortraitParams();
				}
			} else {
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				return p;
			}
		}
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		return p;
		//return null;
	}
	

	

	
	//public void setBuffer(TextTree buffer) {
	//	this.buffer = buffer;
	//}
	
}
