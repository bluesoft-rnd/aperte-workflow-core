package pl.net.bluesoft.rnd.processtool.model.processdata;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "pt_ext_pi_deadline_attr")
public class ProcessDeadline extends ProcessInstanceAttribute {
    private String taskName;
    private Date dueDate;
    private String profileName;
    private String templateName;
    private String notifyUsersWithLogin;
    private String notifyUsersWithRole;
    private boolean skipAssignee;
    private boolean alreadyNotified;

	public boolean isSkipAssignee() {
		return skipAssignee;
	}

	public void setSkipAssignee(boolean skipAssignee) {
		this.skipAssignee = skipAssignee;
	}

	public boolean isAlreadyNotified() {
		return alreadyNotified;
	}

	public void setAlreadyNotified(boolean alreadyNotified) {
		this.alreadyNotified = alreadyNotified;
	}

	public String getNotifyUsersWithLogin() {
        return notifyUsersWithLogin;
    }

    public void setNotifyUsersWithLogin(String notifyUsersWithLogin) {
        this.notifyUsersWithLogin = notifyUsersWithLogin;
    }

    public String getNotifyUsersWithRole() {
        return notifyUsersWithRole;
    }

    public void setNotifyUsersWithRole(String notifyUsersWithRole) {
        this.notifyUsersWithRole = notifyUsersWithRole;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
}
