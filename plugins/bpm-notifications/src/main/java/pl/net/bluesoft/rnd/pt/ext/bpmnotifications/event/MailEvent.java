package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event;

import java.io.Serializable;
import java.util.List;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ProcessedNotificationData;

/**
 * Event wysylania wiadomosci email
 * @author marcin
 *
 */
public class MailEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ProcessedNotificationData notificationData;
	
	public ProcessedNotificationData getNotificationData() {
		return notificationData;
	}
	public void setNotificationData(ProcessedNotificationData notificationData) {
		this.notificationData = notificationData;
	}
	
}
