package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.util.Date;

public class MutableBpmTask implements BpmTask {
    private String assignee;
    private UserData owner;
    private String taskName;
    private String internalTaskId;
    private String executionId;
    private Date createDate;
    private Date finishDate;
    private ProcessInstance processInstance;
    private boolean isFinished;

    public MutableBpmTask() {
    }

    public MutableBpmTask(BpmTask task) {
        this.assignee = task.getAssignee();
        this.owner = task.getOwner();
        this.taskName = task.getTaskName();
        this.internalTaskId = task.getInternalTaskId();
        this.executionId = task.getExecutionId();
        this.createDate = task.getCreateDate();
        this.finishDate = task.getFinishDate();
        this.processInstance = task.getProcessInstance();
        this.isFinished = task.isFinished();
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public String getInternalTaskId() {
        return internalTaskId;
    }

    public void setInternalTaskId(String internalTaskId) {
        this.internalTaskId = internalTaskId;
    }

    public UserData getOwner() {
        return owner;
    }

    public void setOwner(UserData owner) {
        this.owner = owner;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCreator() {
        return processInstance != null ? processInstance.getCreator().getLogin() : null;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    @Override
    public ProcessDefinitionConfig getProcessDefinition() {
        return processInstance != null ? processInstance.getDefinition() : null;
    }

    @Override
    public String getInternalProcessId() {
        return processInstance != null ? processInstance.getInternalId() : null;
    }

    @Override
    public String getExternalProcessId() {
        return processInstance != null ? processInstance.getExternalKey() : null;
    }
}
