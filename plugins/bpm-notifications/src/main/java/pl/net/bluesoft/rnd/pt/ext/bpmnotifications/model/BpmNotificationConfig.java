package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_ext_bpm_notify_config")
public class BpmNotificationConfig extends PersistentEntity {
    private String profileName;
	private String processTypeRegex;
    private String stateRegex;
    private String lastActionRegex;
	private boolean notifyTaskAssignee;
    @Column(name = "skipNotification")
	private boolean skipNotificationWhenTriggeredByAssignee;
	private boolean active;
	private String notifyEmailAddresses;
	private String notifyUserAttributes;
	private String templateName;
	private boolean sendHtml;
    private String locale;
    private boolean notifyOnProcessStart;
	private boolean notifyOnProcessEnd;
    private boolean onEnteringStep;
	private String templateArgumentProvider;

	public boolean isNotifyOnProcessStart() {
        return notifyOnProcessStart;
    }

    public void setNotifyOnProcessStart(boolean notifyOnProcessStart) {
        this.notifyOnProcessStart = notifyOnProcessStart;
    }

	public boolean isNotifyOnProcessEnd() {
		return notifyOnProcessEnd;
	}

	public void setNotifyOnProcessEnd(boolean notifyOnProcessEnd) {
		this.notifyOnProcessEnd = notifyOnProcessEnd;
	}

	public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

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

	public String getNotifyUserAttributes() {
		return notifyUserAttributes;
	}

	public void setNotifyUserAttributes(String notifyUserAttributes) {
		this.notifyUserAttributes = notifyUserAttributes;
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

	public String getTemplateArgumentProvider() {
		return templateArgumentProvider;
	}

	public void setTemplateArgumentProvider(String templateArgumentProvider) {
		this.templateArgumentProvider = templateArgumentProvider;
	}

	public String getLastActionRegex() {
		return lastActionRegex;
	}

	public void setLastActionRegex(String lastTransitionRegex) {
		this.lastActionRegex = lastTransitionRegex;
	}

	public boolean isOnEnteringStep() {
		return onEnteringStep;
	}

	public void setOnEnteringStep(boolean onEnteringStep) {
		this.onEnteringStep = onEnteringStep;
	}
}
