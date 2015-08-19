package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.HandleEmailsJob;

import javax.persistence.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static pl.net.bluesoft.util.lang.Strings.hasText;

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

	private static final ObjectMapper mapper = new ObjectMapper();

	/** Sender email adress */
	private String sender;
	
	/** Recipient email adress */
    private String recipient;
    
    /** Subject of the notification */
    private String subject;
    
    /** Attachments list, seperated by semi-colon */
	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
    private String attachments;
    
    /** Send message as html? */
    private Boolean sendAsHtml;
    
    /** Profile name for connection configuration */
    private String profileName;
    
    /** Send message after specific hour */
    private int sendAfterHour;
    
    private boolean groupNotifications = false;

	private Date notificationCreated;
    
	/** The body of the notification */
	@Lob
    @Type(type = "org.hibernate.type.StringClobType")
	private String body;

	@Transient
	private String originalBody;

	private String source;
	private String tag;
	@Column(name = "template_name")
	private String templateName;
	@Column(name = "sent_folder_name", length = 100)
	private String sentFolderName;

	public String getSentFolderName() {
		return sentFolderName;
	}

	public void setSentFolderName(String sentFolderName) {
		this.sentFolderName = sentFolderName;
	}

	public BpmNotification(){
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

	public String getOriginalBody() {
		return originalBody != null ? originalBody : body;
	}

	public void setOriginalBody(String originalBody) {
		this.originalBody = originalBody;
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

	public int getSendAfterHour() {
		return sendAfterHour;
	}

	public void setSendAfterHour(int sendAfterHour) {
		this.sendAfterHour = sendAfterHour;
	}

	public void encodeAttachments(List<BpmAttachment> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			setAttachments(null);
			return;
		}

		try {
			String json = mapper.writeValueAsString(attachments);
			setAttachments(json);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<BpmAttachment> decodeAttachments() {
		if (!hasText(attachments)) {
			return Collections.emptyList();
		}
		try {
			Object value = mapper.readValue(attachments, new TypeReference<List<BpmAttachment>>() {});
			return (List<BpmAttachment>)value;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public boolean hasAttachments() {
		return attachments != null && !attachments.isEmpty();
	}
}
