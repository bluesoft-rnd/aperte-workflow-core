package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

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
	private String sentFolderName;
	
	public ProcessedNotificationData()
	{
		this.sendAsHtml = true;
	}


	
	public ProcessedNotificationData(NotificationData notificationData) 
	{
		this();
		setTemplateData(notificationData.getTemplateData());
		setRecipient(notificationData.getRecipient());
		setProfileName(notificationData.getProfileName());
		setAttachments(notificationData.getAttachments());
		setSource(notificationData.getSource());
		setTag(notificationData.getTag());
		setDefaultSender(notificationData.getDefaultSender());
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

	public String getSentFolderName() {
		return sentFolderName;
	}

	public ProcessedNotificationData setSentFolderName(String sentFolderName) {
		this.sentFolderName = sentFolderName;
		return this;

	}
}
