package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_ext_bpm_notify_template")
public class BpmNotificationTemplate extends PersistentEntity {
	public static final String _SENDER = "sender";
	public static final String _TEMPLATE_NAME = "templateName";
	public static final String _SUBJECT_TEMPLATE = "subjectTemplate";
	public static final String _TEMPLATE_BODY = "templateBody";
	public static final String _FOOTER_TEMPLATE = "footerTemplate";

	private String sender;
    private String templateName;
    private String subjectTemplate;
	@Lob
    @Type(type = "org.hibernate.type.StringClobType")
	private String templateBody;
	private String footerTemplate;
	@Column(name = "sent_folder_name", length = 100, nullable = true)
	private String sentFolderName;

	public String getSentFolderName() {
		return sentFolderName;
	}

	public void setSentFolderName(String sentFolderName) {
		this.sentFolderName = sentFolderName;
	}

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

	public String getFooterTemplate() {
		return footerTemplate;
	}

	public void setFooterTemplate(String footerTemplate) {
		this.footerTemplate = footerTemplate;
	}
}
