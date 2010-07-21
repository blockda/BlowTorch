package com.happygoatstudios.bt.responder.notification;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponderEditorDoneListener;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.notification.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
	
	EditText title;
	EditText message;
	
	
	
	NotificationResponder the_responder;
	NotificationResponder original;
	TriggerResponderEditorDoneListener finish_with;
	boolean isEditor = false;
	
	public NotificationResponderEditor(Context context,NotificationResponder input,TriggerResponderEditorDoneListener listener) {
		super(context);
		// TODO Auto-generated constructor stub
		finish_with = listener;
		if(input == null) {
			the_responder = new NotificationResponder();
			the_responder.setFireType(FIRE_WHEN.WINDOW_BOTH);
			//new_data = input;
		} else {
			the_responder = input;
			original = input.copy();
			isEditor = true;
		}
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		setContentView(R.layout.responder_notification_dialog);
		
		title = (EditText)findViewById(R.id.responder_notification_title);
		message = (EditText)findViewById(R.id.responder_notification_extra);
		
		lights = (CheckBox)findViewById(R.id.responder_notification_lights_check);
		vibrate = (CheckBox)findViewById(R.id.responder_notification_vibrate_check);
		sound = (CheckBox)findViewById(R.id.responder_notification_sound_check);
		spawnnew = (CheckBox)findViewById(R.id.responder_notification_spawnnew_check);
		useongoing = (CheckBox)findViewById(R.id.responder_notification_useongoing_check);
		
		lights_extra = (TextView)findViewById(R.id.responder_notification_lights_extra);
		sound_extra = (TextView)findViewById(R.id.responder_notification_sound_extra);
		vibrate_extra = (TextView)findViewById(R.id.responder_notification_vibrate_extra);
	
		sound_extra.setMaxWidth((int) (50*this.getContext().getResources().getDisplayMetrics().density));
		sound_extra.setSingleLine(true);
		//if(isEditor) {
			//we have so much work to do.
			title.setText(the_responder.getTitle());
			message.setText(the_responder.getMessage());
			
			if(the_responder.isUseDefaultSound()) {
				sound.setChecked(true);
				if(the_responder.getSoundPath().equals("")) {
					
					sound_extra.setText("Currently using default sound.");
					
				} else {
					sound_extra.setText(the_responder.getSoundPath());
					//should be "", if useDefaultSounds is true;
				}
				//the_responder.setSoundPath("");
			} else {
				sound.setChecked(false);
				if(the_responder.getSoundPath().equals("")) {
					sound_extra.setText("Currently disabled.");
				} else {
					sound_extra.setText(the_responder.getSoundPath() + "[ERROR]");
				}
				//the_responder.setSoundPath("");
			}
			
			if(the_responder.isUseDefaultLight()) {
				lights.setChecked(true);
				if(the_responder.getColorToUse() != 0) {
					lights_extra.setText("Currently Using: " + lookupRawColor(the_responder.getColorToUse()));
				} else {
					lights_extra.setText("Currently Using: default");
				}
			} else {
				lights.setChecked(false);
				lights_extra.setText("Currently disabled.");
			}
			
			if(the_responder.isUseDefaultVibrate())  {
				
				vibrate.setChecked(true);
				if(the_responder.getVibrateLength() != 0) {
					//vibrate_extra.setText("Currently using: " + the_responder.getVibrateLength());
					vibrate_extra.setText("Currently using: " + lookupVibrateLength(the_responder.getVibrateLength()));
				} else {
					vibrate_extra.setText("Currently using: default");
				}
			} else {
				vibrate.setChecked(false);
				vibrate.setText("Currently disabled.");
			}
			
			if(the_responder.isUseOnGoingNotification()) {
				useongoing.setChecked(true);
			} else {
				useongoing.setChecked(false);
			}
			
			if(the_responder.isSpawnNewNotification()) {
				spawnnew.setChecked(true);
			} else {
				spawnnew.setChecked(false);
			}
			
		//}
		
		lights_extra = (TextView)findViewById(R.id.responder_notification_lights_extra);
		vibrate_extra = (TextView)findViewById(R.id.responder_notification_vibrate_extra);
		sound_extra = (TextView)findViewById(R.id.responder_notification_sound_extra);
		
		lights.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.LIGHTS));
		vibrate.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.VIBRATE));
		sound.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.SOUND));
		spawnnew.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.SPAWNNEW));
		useongoing.setOnCheckedChangeListener(new CheckChangedListener(CHECK_TYPE.USEONGOING));
		
		Button done = (Button)findViewById(R.id.responder_notification_done_button);
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				doFinish();
			}
		});
		
		Button cancel = (Button)findViewById(R.id.responder_notification_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				NotificationResponderEditor.this.dismiss();
			}
		});
		
		
	}
	
	private void doFinish() {
		EditText title = (EditText)findViewById(R.id.responder_notification_title);
		EditText extra = (EditText)findViewById(R.id.responder_notification_extra);
		
		the_responder.setTitle(title.getText().toString());
		the_responder.setMessage(extra.getText().toString());
		
		if(isEditor) {		
			finish_with.editTriggerResponder(the_responder, original);
		} else {
			finish_with.newTriggerResponder(the_responder);
		}
		this.dismiss();
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
	
	private String lookupColor(int input) {
		switch(input) {
		case 0:
			return "default";
		case 1:
			return "Blue";
		case 2:
			return "Green";
		case 3:
			return "Red";
		case 4:
			return "Magenta";
		case 5:
			return "Cyan";
		case 6:
			return "White";
		default:
			return "default";
		}
	}
	
	private String lookupRawColor(int rawColor) {
		switch(rawColor) {
		case 0x00000000:
			return "default";
		case 0xFF0000FF:
			return "Blue";
		case 0xFF00FF00:
			return "Green";
		case 0xFFFF0000:
			return "Red";
		case 0xFFFF00FF:
			return "Magenta";
		case 0xFF00FFFF:
			return "Cyan";
		case 0xFFFFFFFF:
			return "White";
		default:
			return "default";
		}
	}
	
	private String lookupVibrateLength(int i) {
		switch(i) {
		case 0: return "default";
		case 1: return "Very Short";
		case 2: return "Short";
		case 3: return "Long";
		case 4: return "Suuper Long";
		default: return "default";
		}
	}
	
	private static final String DISABLED_MSG = "Currently disabled.";
	private static final String DEFAULT_MSG = "Currently using: default";
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
					CharSequence[] light_types = {"Default","Blue","Green","Red","Magenta","Cyan","White"};
					light_builder.setTitle("Select Color");
					light_builder.setItems(light_types, new LightListReturnListener());
					AlertDialog light_picker = light_builder.create();
					light_picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
						
						public void onCancel(DialogInterface dialog) {
							lights.setChecked(false);
							the_responder.setUseDefaultLight(false);
							the_responder.setColorToUse(0);
							lights_extra.setText(DISABLED_MSG);
						}
						
					});
					light_picker.show();
				} else {
					//just reset
					the_responder.setUseDefaultLight(false);
					the_responder.setColorToUse(0);
					lights_extra.setText(DISABLED_MSG);
				}
				break;
			case VIBRATE:
				if(arg1) {
					AlertDialog.Builder vibrate_builder = new AlertDialog.Builder(NotificationResponderEditor.this.getContext());
					CharSequence[] vibrate_types = {"Default","Very Short","Short","Long","Suuuper Long"};
					vibrate_builder.setTitle("Select Sequence:");
					vibrate_builder.setItems(vibrate_types, new VibrateListReturnListener());
					AlertDialog vibrate_dialog = vibrate_builder.create();
					vibrate_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						
						public void onCancel(DialogInterface dialog) {
							vibrate.setChecked(false);
							the_responder.setUseDefaultVibrate(false);
							the_responder.setVibrateLength(0);
							vibrate_extra.setText(DISABLED_MSG);
						}
					});
					vibrate_dialog.show();
				} else {
					//turn option off.
					the_responder.setUseDefaultVibrate(false);
					the_responder.setVibrateLength(0);
					vibrate_extra.setText(DISABLED_MSG);
				}
				break;
			case SOUND:
				if(arg1) {
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
					sound_picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
						
						public void onCancel(DialogInterface dialog) {
							//if we got here then we need to uncheck the box.
							sound.setChecked(false);
							the_responder.setUseDefaultSound(false);
							the_responder.setSoundPath("");
							sound_extra.setText(DISABLED_MSG);
						}
					});
					//
					sound_picker.show();
					
				} else {
					//can't really do antying because the sdcard isn't mounted.
					//set to "dont use"
					
				}
				} else {
					the_responder.setUseDefaultSound(false);
					the_responder.setSoundPath("");
					sound_extra.setText(DISABLED_MSG);
				}
				break;
			case SPAWNNEW:
				if(arg1) {
					//set the value
					the_responder.setSpawnNewNotification(true);
				} else {
					//set the value
					the_responder.setSpawnNewNotification(false);
				}
				break;
			case USEONGOING:
				if(arg1) {
					//set the value
					the_responder.setUseOnGoingNotification(true);
				} else {
					//set the value
					the_responder.setUseOnGoingNotification(false);
				}
				break;
			default:
				break;
			}
			
		}
		

		
		private class LightListReturnListener implements DialogInterface.OnClickListener {

			public void onClick(DialogInterface arg0, int arg1) {
				CharSequence[] light_types = {"Default","Blue","Green","Red","Magenta","Cyan","White"};
				switch(arg1) {
				case 0:
					//default;
					the_responder.setUseDefaultLight(true);
					break;
				case 1:
					//blue
					the_responder.setUseDefaultLight(true);
					the_responder.setColorToUse(0xFF0000FF);
					break;
				case 2:
					//green
					the_responder.setUseDefaultLight(true);
					the_responder.setColorToUse(0xFF00FF00);
					break;
				case 3:
					//red
					the_responder.setUseDefaultLight(true);
					the_responder.setColorToUse(0xFFFF0000);
					break;
				case 4:
					//magenta
					the_responder.setUseDefaultLight(true);
					the_responder.setColorToUse(0xFFFF00FF);
					break;
				case 5:
					//cyan
					the_responder.setUseDefaultLight(true);
					the_responder.setColorToUse(0xFF00FFFF);
					break;
				case 6:
					//white
					the_responder.setUseDefaultLight(true);
					the_responder.setColorToUse(0xFFFFFFFF);
					break;
				default:
					break;
				}
				if(arg1 != 0) {	
					lights_extra.setText("Currently using: " + lookupColor(arg1));
				} else {
					lights_extra.setText(DEFAULT_MSG);
				}
				
			}
			
		}
		

		
		private class VibrateListReturnListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface arg0, int arg1) {
				CharSequence[] vibrate_types = {"Default","Very Short","Short","Long","Suuuper Long"};
				//CharSequence type = vibrate_types[arg1];
				switch(arg1) {
				case 0:
					//default
					the_responder.setUseDefaultVibrate(true);
					the_responder.setVibrateLength(0);
					break;
				case 1:
					//very short
					the_responder.setUseDefaultVibrate(true);
					the_responder.setVibrateLength(1);
					break;
				case 2:
					//short
					the_responder.setUseDefaultVibrate(true);
					the_responder.setVibrateLength(2);
					break;
				case 3:
					//Long
					the_responder.setUseDefaultVibrate(true);
					the_responder.setVibrateLength(3);
					break;
				case 4:
					//Suuper Long
					the_responder.setUseDefaultVibrate(true);
					the_responder.setVibrateLength(4);
					break;
				default:
					break;
				}
				if(arg1 != 0){
					vibrate_extra.setText("Currently using: " + lookupVibrateLength(arg1));
				} else {
					vibrate_extra.setText(DEFAULT_MSG);
				}
			}
		}
		
		private class SoundListReturnListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface arg0, int arg1) {
				String name = sound_files.get(arg1);
				//String path = 
				String path = paths.get(name);
				the_responder.setUseDefaultSound(true);
				the_responder.setSoundPath(path);
				if(path.equals("")) {
					sound_extra.setText(DEFAULT_MSG);
				} else {
					sound_extra.setText("Currently using: " + path);
				}
			}
		}
		
		
	}

}
