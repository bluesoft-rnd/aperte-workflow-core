package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.HandleEmailsJob;

/**
 * The entity which represents notification to be send by scheduler
 * {@link HandleEmailsJob}
 * 
 * @author Maciej Pawlak
 *
 */
@Entity
@Table(name="pt_ext_bpm_notification")
public class BpmNotification extends PersistentEntity 
{
	private static final long serialVersionUID = -1358169256410750304L;

	/** Sender email adress */
	private String sender;
	
	/** Recipient email adress */
    private String recipient;
    
    /** Subject of the notification */
    private String subject;
    
    /** Attachments list, seperated by semi-colon */
    private String attachments;
    
    /** Send message as html? */
    private Boolean sendAsHtml;
    
    /** Profile name for connection configuration */
    private String profileName;
    
    /** Send message after specific hour */
    private Date sendAfterHour;
    
    private boolean groupNotifications;

	private Date notificationCreated;
    
	/** The body of the notification */
	@Lob
    @Type(type = "org.hibernate.type.StringClobType")
	private String body;
	
	public BpmNotification(){
		/*Calendar cal = Calendar.getInstance();
		cal.setTime( new Date());
		int time = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
		*/
        notificationCreated = new Date();
	}
	
	public Date getNotificationCreated() {
		return notificationCreated;
	}

	public void setNotificationCreated(Date notificationCreated) {
		this.notificationCreated = notificationCreated;
	}

	public boolean isGroupNotifications() {
		return groupNotifications;
	}

	public void setGroupNotifications(boolean groupNotifications) {
		this.groupNotifications = groupNotifications;
	}
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
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

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public Boolean getSendAsHtml() {
		return sendAsHtml;
	}

	public void setSendAsHtml(Boolean sendAsHtml) {
		this.sendAsHtml = sendAsHtml;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public Date getSendAfterHour() {
		return sendAfterHour;
	}

	public void setSendAfterHour(Date sendAfterHour) {
		this.sendAfterHour = sendAfterHour;
	}
}
