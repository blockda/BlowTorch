package com.happygoatstudios.bt.responder.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponder;
import com.happygoatstudios.bt.responder.TriggerResponder.FIRE_WHEN;
import com.happygoatstudios.bt.responder.TriggerResponder.RESPONDER_TYPE;

public class NotificationResponder extends TriggerResponder implements Parcelable {

	private String message;
	private String title;
	
	private boolean useDefaultSound;
	private String soundPath;
	private boolean useDefaultLight;
	private int colorToUse;
	private boolean useDefaultVibrate;
	private int vibrateLength;
	
	private Integer myTriggerId = null;
	
	private boolean spawnNewNotification;
	private boolean useOnGoingNotification;
	
	public NotificationResponder() {
		super(RESPONDER_TYPE.NOTIFICATION);
		message = "";
		title = "";
		useDefaultSound = true;
		soundPath ="";
		useDefaultLight = true;
		colorToUse = 0xFFFFFFFF;
		useDefaultVibrate = true;
		vibrateLength = 0x03;
	}
	
	public static final Parcelable.Creator<NotificationResponder> CREATOR = new Parcelable.Creator<NotificationResponder>() {

		public NotificationResponder createFromParcel(Parcel arg0) {
			return new NotificationResponder(arg0);
		}

		public NotificationResponder[] newArray(int arg0) {
			return new NotificationResponder[arg0];
		}
		
	};
	
	public NotificationResponder(Parcel p) {
		
		super(RESPONDER_TYPE.NOTIFICATION);
		readFromParcel(p);
	}
	
	public void readFromParcel(Parcel in) {
		setMessage(in.readString());
		setTitle(in.readString());
		String fireType = in.readString();
		if(fireType.equals(FIRE_WINDOW_OPEN)) {
			setFireType(FIRE_WHEN.WINDOW_OPEN);
		} else if (fireType.equals(FIRE_WINDOW_CLOSED)) {
			setFireType(FIRE_WHEN.WINDOW_CLOSED);
		} else if (fireType.equals(FIRE_ALWAYS)) {
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		} else if (fireType.equals(FIRE_NEVER)) {
			setFireType(FIRE_WHEN.WINDOW_NEVER);
		} else {
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		}
		setUseDefaultSound( (in.readInt() < 1) ? false : true);
		setSoundPath(in.readString());
		setUseDefaultLight((in.readInt() < 1) ? false : true);
		setColorToUse(in.readInt());
		setUseDefaultVibrate( (in.readInt() < 1) ? false : true);
		setVibrateLength(in.readInt());
		Log.e("RESPONDER","BEING PARCEL CREATED:" + getMessage() + " || " + getTitle());
	}

	public NotificationResponder(RESPONDER_TYPE pType) {
		super(pType);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(message);
		out.writeString(title);
		out.writeString(this.getFireType().getString());
		out.writeInt( (useDefaultSound) ? 1 : 0);
		out.writeString(soundPath);
		out.writeInt((useDefaultLight) ? 1 : 0);
		out.writeInt(colorToUse);
		out.writeInt((useDefaultVibrate) ? 1 : 0);
		out.writeInt(vibrateLength);
		Log.e("RESPONDER","BEING PARCELE OUTPUTED");
		
	}

	public void setMessage(String message) {
		if(message == null) message = "";
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setTitle(String title) {
		if(title == null) title = "";
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public void doResponse(Context c,String displayname,int triggernumber,boolean windowIsOpen,Handler dispatcher) {
		//we are going to do the window response now.
		
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED) {
				return;
			}
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN) {
				return;
			}
		}
		
		if(myTriggerId == null) {
			//has not been set yet.
			myTriggerId = triggernumber;
		} else {
			if(this.isSpawnNewNotification()) {
				myTriggerId = triggernumber;
			}
		}
		

		NotificationManager NM = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification note = new Notification(android.R.drawable.btn_plus,"Trigger Fired!",System.currentTimeMillis());
		Intent notificationIntent  = new Intent(c,com.happygoatstudios.bt.window.BaardTERMWindow.class);
		notificationIntent.putExtra("DISPLAY", displayname);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		note.setLatestEventInfo(c, title, message, contentIntent);
		note.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		note.defaults = Notification.DEFAULT_SOUND;
		note.ledARGB = 0xFFFF00FF;
		note.ledOnMS = 300;
		note.ledOffMS = 300;
		
		long[] vp = new long[4];
		vp[0] = 0;
		vp[1] = 200;
		vp[2] = 50;
		vp[3] = 200;
		
		note.vibrate = vp;
		
		NM.notify(myTriggerId,note);
	}

	public void setSpawnNewNotification(boolean spawnNewNotification) {
		this.spawnNewNotification = spawnNewNotification;
	}

	public boolean isSpawnNewNotification() {
		return spawnNewNotification;
	}

	public void setUseOnGoingNotification(boolean useOnGoingNotification) {
		this.useOnGoingNotification = useOnGoingNotification;
	}

	public boolean isUseOnGoingNotification() {
		return useOnGoingNotification;
	}

	public void setUseDefaultSound(boolean useDefaultSound) {
		this.useDefaultSound = useDefaultSound;
	}

	public boolean isUseDefaultSound() {
		return useDefaultSound;
	}

	public void setSoundPath(String soundPath) {
		this.soundPath = soundPath;
	}

	public String getSoundPath() {
		return soundPath;
	}

	public void setUseDefaultLight(boolean useDefaultLight) {
		this.useDefaultLight = useDefaultLight;
	}

	public boolean isUseDefaultLight() {
		return useDefaultLight;
	}

	public void setColorToUse(int colorToUse) {
		this.colorToUse = colorToUse;
	}

	public int getColorToUse() {
		return colorToUse;
	}

	public void setUseDefaultVibrate(boolean useDefaultVibrate) {
		this.useDefaultVibrate = useDefaultVibrate;
	}

	public boolean isUseDefaultVibrate() {
		return useDefaultVibrate;
	}

	public void setVibrateLength(int vibrateLength) {
		this.vibrateLength = vibrateLength;
	}

	public int getVibrateLength() {
		return vibrateLength;
	}

}
