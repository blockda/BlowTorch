package com.happygoatstudios.bt.responder.notification;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.happygoatstudios.bt.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class NotificationResponderEditor extends Dialog {
	
	CheckBox lights;
	CheckBox vibrate;
	CheckBox sound;
	CheckBox spawnnew;
	CheckBox useongoing;
	
	TextView lights_extra;
	TextView vibrate_extra;
	TextView sound_extra;

	public NotificationResponderEditor(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		setContentView(R.layout.responder_notification_dialog);
		
		lights = (CheckBox)findViewById(R.id.responder_notification_lights_check);
		vibrate = (CheckBox)findViewById(R.id.responder_notification_vibrate_check);
		sound = (CheckBox)findViewById(R.id.responder_notification_sound_check);
		spawnnew = (CheckBox)findViewById(R.id.responder_notification_spawnnew_check);
		useongoing = (CheckBox)findViewById(R.id.responder_notification_useongoing_check);
		
		lights_extra = (TextView)findViewById(R.id.responder_notification_lights_extra);
		vibrate_extra = (TextView)findViewById(R.id.responder_notification_vibrate_extra);
		sound_extra = (TextView)findViewById(R.id.responder_notification_sound_extra);
		
		lights.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.LIGHTS));
		vibrate.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.VIBRATE));
		sound.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.SOUND));
		spawnnew.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.SPAWNNEW));
		useongoing.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.USEONGOING));
	}
	
	public static enum CHECK_TYPE {
		LIGHTS,
		VIBRATE,
		SOUND,
		SPAWNNEW,
		USEONGOING;
	}
	
	protected ArrayList<String> sound_files = new ArrayList<String>();
	protected HashMap<String,String> paths = new HashMap<String,String>();
	
	
	
	private class CheckChangedListener implements CompoundButton.OnCheckedChangeListener {
		
		private CHECK_TYPE type;
		
		public CheckChangedListener(CHECK_TYPE iType) {
			type = iType;
		}
		
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			switch(type) {
			case LIGHTS:
				if(arg1) {
					AlertDialog.Builder light_builder = new AlertDialog.Builder(NotificationResponderEditor.this.getContext());
					CharSequence[] light_types = {"Blue","Green","Red","Magenta","Cyan","White"};
					light_builder.setTitle("Select Color");
					light_builder.setItems(light_types, new LightListReturnListener());
					AlertDialog light_picker = light_builder.create();
					light_picker.show();
				} else {
					//just reset
				}
				break;
			case VIBRATE:
				if(arg1) {
					AlertDialog.Builder vibrate_builder = new AlertDialog.Builder(NotificationResponderEditor.this.getContext());
					CharSequence[] vibrate_types = {"Default","Very Short","Short","Long","Suuuper Long"};
					vibrate_builder.setTitle("Select Sequence:");
					vibrate_builder.setItems(vibrate_types, new VibrateListReturnListener());
				} else {
					//turn option off.
				}
				break;
			case SOUND:
				String state = Environment.getExternalStorageState();
				if(state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
					File sdcardroot = Environment.getExternalStorageDirectory();
					
					String[] system_paths = {"/system/media/audio/ringtones/",
											 "/system/media/audio/alarms/",
											 "/system/media/audio/notificiation/"};
					paths.clear();
					
					for(String path : system_paths) {
						//Log.e("RESPONDER","TESTING PATH:"+path);
						File tmp = new File(path);
						//Log.e("RESPONDER","FILE IS DIR: " + tmp.isDirectory() + ", " + tmp.getPath() + "@" + tmp.getName());
						if(tmp != null && tmp.isDirectory()) {
							//Log.e("RESPONDER","PATH EXISTS @" + tmp.getPath() + "|" + tmp.getName());
							for(File file : tmp.listFiles()) {
								//Log.e("RESPONDER","FOUND FILE:" + file.getName() + "@" + file.getPath());
								paths.put(file.getName(), file.getPath());
							}
						}
					}
					
					FilenameFilter mp3_only = new FilenameFilter() {

						public boolean accept(File arg0, String arg1) {
							return arg1.endsWith(".mp3");
						}
						
					};
					
					File btermdir = new File(sdcardroot,"/BaardTERM/");
					for(File path : btermdir.listFiles(mp3_only)) {
						paths.put(path.getName(), path.getPath());
					}
					
					sound_files.clear();
					for(String name : paths.keySet()) {
						sound_files.add(name);
					}
					
					AlertDialog.Builder sound_list = new AlertDialog.Builder(NotificationResponderEditor.this.getContext());
					sound_list.setTitle("Pick Sound:");
					String[] items = new String[sound_files.size()];
					int i = 0;
					for(String file : sound_files) {
						items[i++] = file;
					}
					sound_list.setItems(items, new SoundListReturnListener());
					AlertDialog sound_picker = sound_list.create();
					sound_picker.show();
					
				} else {
					//can't really do antying because the sdcard isn't mounted.
					//set to "dont use"
				}
				break;
			case SPAWNNEW:
				if(arg1) {
					//set the value
				} else {
					//set the value
				}
				break;
			case USEONGOING:
				if(arg1) {
					//set the value
				} else {
					//set the value
				}
				break;
			default:
				break;
			}
			
		}
		
		private class LightListReturnListener implements DialogInterface.OnClickListener {

			public void onClick(DialogInterface arg0, int arg1) {
				
			}
			
		}
		
		private class VibrateListReturnListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		}
		
		private class SoundListReturnListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface arg0, int arg1) {
				String name = sound_files.get(arg1);
				//String path = 
			}
		}
		
		
	}

}
