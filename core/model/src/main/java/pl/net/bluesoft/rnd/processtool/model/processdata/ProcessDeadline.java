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
    private String templateName;
    private String notifyUsersWithLogin;
    private String notifyUsersWithRole;
    private Boolean skipAssignee;
    private Boolean alreadyNotified = Boolean.FALSE;

    public boolean isAlreadyProcessed() {
        return getAlreadyNotified() != null ? getAlreadyNotified().booleanValue() : false;
    }

    public Boolean getAlreadyNotified() {
        return alreadyNotified;
    }

    public void setAlreadyNotified(Boolean alreadyNotified) {
        this.alreadyNotified = alreadyNotified;
    }

    public Boolean getSkipAssignee() {
        return skipAssignee;
    }

    public void setSkipAssignee(Boolean skipAssignee) {
        this.skipAssignee = skipAssignee;
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
}
