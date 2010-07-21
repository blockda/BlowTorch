package com.happygoatstudios.bt.responder.notification;

public interface NotificationResponderDoneListener {
	
	void newNotificationResponder(NotificationResponder newresponder);
	void editNotificationResponder(NotificationResponder edited,NotificationResponder original);
}
