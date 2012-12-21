package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.Date;

/**
 * User: POlszewski
 * Date: 2012-10-13
 * Time: 15:29
 */
public class NotificationHistoryEntry {
	public static final String _BPM_NOTIFICATION_ID = "bpmNotificationId";
	public static final String _SENDER = "sender";
	public static final String _RECIPIENT = "recipient";
	public static final String _SUBJECT = "subject";
	public static final String _BODY = "body";
	public static final String _ENQUEUE_DATE = "enqueueDate";
	public static final String _SEND_DATE = "sendDate";
	public static final String _SENDING_EXCEPTION = "sendingException";

	private long bpmNotificationId;
	private String sender;
	private String recipient;
	private String subject;
	private String body;
	private boolean asHtml;
	private Date enqueueDate;
	private Date sendDate;
	private Exception sendingException;

	public long getBpmNotificationId() {
		return bpmNotificationId;
	}

	public void setBpmNotificationId(long bpmNotificationId) {
		this.bpmNotificationId = bpmNotificationId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isAsHtml() {
		return asHtml;
	}

	public void setAsHtml(boolean asHtml) {
		this.asHtml = asHtml;
	}

	public Date getEnqueueDate() {
		return enqueueDate;
	}

	public void setEnqueueDate(Date enqueueDate) {
		this.enqueueDate = enqueueDate;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public Exception getSendingException() {
		return sendingException;
	}

	public void setSendingException(Exception e) {
		this.sendingException = e;
	}
}
