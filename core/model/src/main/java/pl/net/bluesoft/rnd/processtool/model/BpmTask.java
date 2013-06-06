package pl.net.bluesoft.rnd.processtool.model;

import java.io.Serializable;
import java.util.Date;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

public class BpmTask implements Serializable 
{
	private static final long serialVersionUID = -6922749510138539783L;

	public static final String AUTO_SKIP_TASK_NAME_PREFIX = "AUTO_SKIP";
	
    protected String assignee;
    protected UserData owner;
    protected String taskName;
    protected String internalTaskId;
    protected String executionId;
    protected Date createDate;
    protected Date finishDate;
    protected ProcessInstance processInstance;
    protected boolean isFinished;

    public BpmTask() {
    }

    public BpmTask(BpmTask task) {
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

    public ProcessDefinitionConfig getProcessDefinition() {
        return processInstance != null ? processInstance.getDefinition() : null;
    }
    
    /** Method returns current state configuration */
    public ProcessStateConfiguration getCurrentProcessStateConfiguration()
    {
    	/* Find current state by action name */
    	ProcessDefinitionConfig processDefinitionConfig = getProcessDefinition();
    	
    	if(processDefinitionConfig == null)
    		return null;
    	
    	String stateName = getTaskName();
    	
    	return processDefinitionConfig.getProcessStateConfigurationByName(stateName);
    }

    public String getInternalProcessId() {
        return processInstance != null ? processInstance.getInternalId() : null;
    }

    public String getExternalProcessId() {
        return processInstance != null ? processInstance.getExternalKey() : null;
    }

	@Override
	public String toString() {
		return "BpmTask [taskName=" + taskName + "]";
	}

	/** Check if the task is set to auto skip */
	public boolean isAutoSkip() 
	{
		
		return getTaskName().contains(AUTO_SKIP_TASK_NAME_PREFIX); 
	}

}
