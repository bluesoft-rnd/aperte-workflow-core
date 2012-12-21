package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event;

import java.io.Serializable;
import java.util.List;

/**
 * Event wysylania wiadomosci email
 * @author marcin
 *
 */
public class MailEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String mailSessionProfileName;
	private String sender;
	private String recipient;
	private String subject;
	private String body;
	private List<String> attachments;
	
	public String getMailSessionProfileName() {
		return mailSessionProfileName;
	}
	public void setMailSessionProfileName(String mailSessionProfileName) {
		this.mailSessionProfileName = mailSessionProfileName;
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
	public List<String> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
	
}
