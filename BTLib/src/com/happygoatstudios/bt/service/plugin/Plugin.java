package com.happygoatstudios.bt.service.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import android.os.Handler;
import android.util.Log;

import com.happygoatstudios.bt.alias.AliasData;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.service.StellarService;
import com.happygoatstudios.bt.service.plugin.settings.PluginSettings;
import com.happygoatstudios.bt.timer.TimerData;
import com.happygoatstudios.bt.trigger.TriggerData;
import com.happygoatstudios.bt.window.TextTree;

public class Plugin {
	//we are a lua plugin.
	//we can give users 
	Matcher colorStripper = StellarService.colordata.matcher("");
	LuaState L = null;
	private PluginSettings settings = null;
	
	public Plugin() {
		setSettings(new PluginSettings());
		L = LuaStateFactory.newLuaState();
	}
	
	public Plugin(PluginSettings settings) {
		this.settings = settings;
		L = LuaStateFactory.newLuaState();
	}

	public void setSettings(PluginSettings settings) {
		this.settings = settings;
	}

	public PluginSettings getSettings() {
		return settings;
	}
	private final HashMap<String,String> captureMap = new HashMap<String,String>();
	public void process(TextTree input,StellarService service,boolean windowOpen,Handler pump,String display) {
		List<TriggerData> triggers = new ArrayList<TriggerData>(this.settings.getTriggers().values());
		Collections.sort(triggers,new Comparator() {

			
			/*public int compare(TriggerData a, TriggerData b) {
				// TODO Auto-generated method stub
				if(a.getSequence() < b.getSequence()) {
					return -1;
				} else if(a.getSequence() == b.getSequence()) {
					return 0;
				} else if(a.getSequence() > b.getSequence()){
					return 1;
				}
				
				return 0;
				
			}*/

			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				TriggerData a = (TriggerData)arg0;
				TriggerData b = (TriggerData)arg1;
				//if(a.getSequence() == 5 || b.getSequence() == 5) {
					//Log.e("COMP","STOP HERE");
				//}
				if(a.getSequence() > b.getSequence()) return 1;
				if(a.getSequence() < b.getSequence()) return -1;
				
				return 0;
			}
			
		});
		//sick ass shit in the hiznizouous
		ListIterator<TextTree.Line> it = input.getLines().listIterator(input.getLines().size());
		boolean keepEvaluating = true;
		while(it.hasPrevious() && keepEvaluating) {
			TextTree.Line l = it.previous();
			//StringBuffer tmp = TextTree.deColorLine(l);
			//test this line against each trigger.
			for(TriggerData t : triggers) {
				if(t.isEnabled()) {
					String str = TextTree.deColorLine(l).toString();
					t.getMatcher().reset(str);
					while(t.getMatcher().find() && keepEvaluating) {
						if(t.isFireOnce() && t.isFired()) {
							//do nothiong
						} else {
							if(t.isFireOnce()) {
								t.setFired(true);
							}
							
							captureMap.clear();
							for(int i=0;i<=t.getMatcher().groupCount();i++) {
								captureMap.put(Integer.toString(i), t.getMatcher().group(i));
							}
							for(TriggerResponder responder : t.getResponders()) {
								responder.doResponse(service.getApplicationContext(),input,l,t.getMatcher(),t, display, StellarService.getNotificationId(), windowOpen, pump,captureMap,L,t.getName());
							}
							if(!t.isKeepEvaluating()) {
								keepEvaluating = false;
								break;
							}
							
							
						}
					}
				}
			}
		}
		//return null;
	}
	
	
}
