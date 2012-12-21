package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_ext_bpm_notify_template")
public class BpmNotificationTemplate extends PersistentEntity {
	private String sender;
    private String templateName;
    private String subjectTemplate;
	@Lob
    @Type(type = "org.hibernate.type.StringClobType")
	private String templateBody;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSubjectTemplate() {
		return subjectTemplate;
	}

	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateBody() {
		return templateBody;
	}

	public void setTemplateBody(String templateBody) {
		this.templateBody = templateBody;
	}
}
