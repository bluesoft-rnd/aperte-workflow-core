package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_ext_bpm_notify_config")
public class BpmNotificationConfig extends PersistentEntity {
	private String processTypeRegex, stateRegex;
	private boolean notifyTaskAssignee;
    @Column(name = "skipNotification")
	private boolean skipNotificationWhenTriggeredByAssignee;
	private boolean active;
	private String notifyEmailAddresses;

	private String templateName;
	private boolean sendHtml;

	public String getProcessTypeRegex() {
		return processTypeRegex;
	}

	public void setProcessTypeRegex(String processTypeRegex) {
		this.processTypeRegex = processTypeRegex;
	}

	public String getStateRegex() {
		return stateRegex;
	}

	public void setStateRegex(String stateRegex) {
		this.stateRegex = stateRegex;
	}

	public boolean isNotifyTaskAssignee() {
		return notifyTaskAssignee;
	}

	public void setNotifyTaskAssignee(boolean notifyTaskAssignee) {
		this.notifyTaskAssignee = notifyTaskAssignee;
	}

	public String getNotifyEmailAddresses() {
		return notifyEmailAddresses;
	}

	public void setNotifyEmailAddresses(String notifyEmailAddresses) {
		this.notifyEmailAddresses = notifyEmailAddresses;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public boolean isSendHtml() {
		return sendHtml;
	}

	public void setSendHtml(boolean sendHtml) {
		this.sendHtml = sendHtml;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSkipNotificationWhenTriggeredByAssignee() {
		return skipNotificationWhenTriggeredByAssignee;
	}

	public void setSkipNotificationWhenTriggeredByAssignee(boolean skipNotificationWhenTriggeredByAssignee) {
		this.skipNotificationWhenTriggeredByAssignee = skipNotificationWhenTriggeredByAssignee;
	}
}
