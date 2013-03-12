package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.data;

import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * Notification DTO
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class NotificationData 
{
	private UserData recipient;
	private String profileName;
	private TemplateData templateData;
	
	public TemplateData getTemplateData() {
		return templateData;
	}

	public NotificationData setTemplateData(TemplateData templateData) {
		this.templateData = templateData;
		
		return this;
	}

	public UserData getRecipient() {
		return recipient;
	}
	public NotificationData setRecipient(UserData recipient) {
		this.recipient = recipient;
		
		return this;
	}
	
	public String getProfileName() {
		return profileName;
	}

	public NotificationData setProfileName(String profileName) {
		this.profileName = profileName;
		
		return this;
	}
}
