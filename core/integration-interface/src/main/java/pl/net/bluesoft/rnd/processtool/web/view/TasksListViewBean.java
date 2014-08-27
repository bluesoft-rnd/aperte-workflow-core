package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;

import java.util.Date;

/**
 * Created by Marcin Król on 2014-05-12.
 */
public abstract class TasksListViewBean extends AbstractResultBean {

    private String queueName;
    private Date taskAssigneDate;
    private Boolean userCanClaim = false;
    private String taskId;
    private Date creationDate;
    private String creator;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Boolean getUserCanClaim() {
        return userCanClaim;
    }

    public void setUserCanClaim(Boolean userCanClaim) {
        this.userCanClaim = userCanClaim;
    }

    public Date getTaskAssigneDate() {
        return taskAssigneDate;
    }

    public void setTaskAssigneDate(Date taskAssigneDate) {
        this.taskAssigneDate = taskAssigneDate;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
