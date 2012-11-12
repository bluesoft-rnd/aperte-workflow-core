package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_state_action")
public class ProcessStateAction extends PersistentEntity {
    final static public String PRIMARY_ACTION = "primary";
    final static public String SECONDARY_ACTION = "secondary";

//    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "state_id")
    private ProcessStateConfiguration config;

    private String bpmName;
    private String label;
    private String description;
    private String buttonName = "Default";
    private String actionType = PRIMARY_ACTION;
    private String url;
    private String title;
    private String question;
    private String notification;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "action_id")
    private Set<ProcessStateActionPermission> permissions = new HashSet();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "action_id")
    private Set<ProcessStateActionAttribute> attributes = new HashSet();

    private String assignProcessStatus;

    private Boolean markProcessImportant = false;

    private Boolean skipSaving = false;

    private Boolean autohide = false;

    private Integer priority;

    public String getAssignProcessStatus() {
        return assignProcessStatus;
    }

    public void setAssignProcessStatus(String assignProcessStatus) {
        this.assignProcessStatus = assignProcessStatus;
    }

    public String getButtonName() {
        return nvl(buttonName, "Default");
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }

    @XmlTransient
    public ProcessStateConfiguration getConfig() {
        return config;
    }

//    @XmlTransient
    public void setConfig(ProcessStateConfiguration config) {
        this.config = config;
    }

    public String getBpmName() {
        return bpmName;
    }

    public void setBpmName(String bpmName) {
        this.bpmName = bpmName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ProcessStateActionPermission> getPermissions() {
        if (permissions == null) permissions = new HashSet<ProcessStateActionPermission>();
        return permissions;
    }

    public void setPermissions(Set<ProcessStateActionPermission> permissions) {
        this.permissions = permissions;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getSkipSaving() {
        return nvl(skipSaving, false);
    }

    public void setSkipSaving(Boolean skipSaving) {
        this.skipSaving = skipSaving;
    }

    public Boolean getAutohide() {
        return nvl(autohide, true);
    }

    public void setAutohide(Boolean autohide) {
        this.autohide = autohide;
    }

    public Set<ProcessStateActionAttribute> getAttributes() {
        if (attributes == null) attributes = new HashSet<ProcessStateActionAttribute>();
        return attributes;
    }

    public void setAttributes(Set<ProcessStateActionAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Boolean getMarkProcessImportant() {
        return markProcessImportant;
    }

    public void setMarkProcessImportant(Boolean markProcessImportant) {
        this.markProcessImportant = markProcessImportant;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}
}
