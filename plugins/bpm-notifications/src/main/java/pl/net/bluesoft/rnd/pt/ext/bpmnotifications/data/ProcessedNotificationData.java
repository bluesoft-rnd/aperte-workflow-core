package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Notification data with template processed data, ready to be send
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ProcessedNotificationData extends NotificationData 
{
	private String body;
	private String subject;
	private String sender;
	private boolean sendAsHtml;
	private Collection<String> attachments;
	
	public ProcessedNotificationData()
	{
		this.sendAsHtml = true;
		this.attachments = new ArrayList<String>();
	}
	
	public ProcessedNotificationData(NotificationData notificationData) 
	{
		this();
		
		setTemplateData(notificationData.getTemplateData());
		setRecipient(notificationData.getRecipient());
		setProfileName(notificationData.getProfileName());
	}

	public String getBody() {
		return body;
	}
	public ProcessedNotificationData setBody(String body) {
		this.body = body;
		
		return this;
	}
	public String getSubject() {
		return subject;
	}
	public ProcessedNotificationData setSubject(String subject) {
		this.subject = subject;
		
		return this;
	}
	public String getSender() 
	{
		return sender;
	}
	
	public ProcessedNotificationData setSender(String sender) 
	{
		this.sender = sender;
		
		return this;
	}
	
	public boolean hasSender()
	{
		return this.sender != null && !this.sender.isEmpty();
	}
	
	public boolean isSendAsHtml() {
		return sendAsHtml;
	}
	public ProcessedNotificationData setSendAsHtml(boolean sendAsHtml) {
		this.sendAsHtml = sendAsHtml;
		
		return this;
	}

	public Collection<String> getAttachments() {
		return attachments;
	}

	public ProcessedNotificationData addAttachment(String attachment) {
		this.attachments.add(attachment);
		
		return this;
	}

	
}
