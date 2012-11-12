package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.util;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;

import java.util.*;

/**
 * User: POlszewski
 * Date: 2012-10-13
 * Time: 15:25
 */
public class NotificationHistory {
	private final LinkedList<NotificationHistoryEntry> entries = new LinkedList<NotificationHistoryEntry>();
	private final Map<Long, NotificationHistoryEntry> entriesByNotificationId = new HashMap<Long, NotificationHistoryEntry>();
	private final int maxEntries;

	public NotificationHistory(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	public synchronized void notificationEnqueued(BpmNotification notification) {
		getEntry(notification).setEnqueueDate(new Date());
	}

	public synchronized void notificationSent(BpmNotification notification) {
		getEntry(notification).setSendDate(new Date());
	}

	public synchronized void errorWhileSendingNotification(BpmNotification notification, Exception e) {
		NotificationHistoryEntry entry = getEntry(notification);
		entry.setSendDate(new Date());
		entry.setSendingException(e);
	}

	public synchronized List<NotificationHistoryEntry> getRecentEntries() {
		return new ArrayList<NotificationHistoryEntry>(entries);
	}

	private NotificationHistoryEntry getEntry(BpmNotification notification) {
		NotificationHistoryEntry entry = entriesByNotificationId.get(notification.getId());
		if (entry != null) {
			return entry;
		}
		entry = new NotificationHistoryEntry();
		entry.setBpmNotificationId(notification.getId());
		entry.setSender(notification.getSender());
		entry.setRecipient(notification.getRecipient());
		entry.setSubject(notification.getSubject());
		entry.setBody(notification.getBody());
		entry.setAsHtml(notification.getSendAsHtml());
		entries.add(entry);
		entriesByNotificationId.put(notification.getId(), entry);
		if (maxEntries > 0 && entries.size() > maxEntries) {
			NotificationHistoryEntry removedEntry = entries.removeFirst();
			entriesByNotificationId.remove(removedEntry.getBpmNotificationId());
		}
		return entry;
	}
}
