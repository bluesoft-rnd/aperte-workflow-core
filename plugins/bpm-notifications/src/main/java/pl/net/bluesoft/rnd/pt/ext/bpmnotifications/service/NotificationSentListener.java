package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;

import javax.mail.Message;

/**
 * User: POlszewski
 * Date: 2014-05-26
 */
public interface NotificationSentListener {
	void notificationSent(BpmNotification notification, Message message);
}
