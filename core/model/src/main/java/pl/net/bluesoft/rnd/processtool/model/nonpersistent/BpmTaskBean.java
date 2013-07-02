package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-06-19
 * Time: 14:29
 */
public class BpmTaskBean implements BpmTask, Serializable {
	private static final long serialVersionUID = -6922749510138539783L;

	private String assignee;
	private String groupId;
	private UserData owner;
	private String taskName;
	private String internalTaskId;
	private String executionId;
	private Date createDate;
	private Date finishDate;
	private ProcessInstance processInstance;
	private boolean isFinished;

	public BpmTaskBean() {
	}

	public BpmTaskBean(BpmTask task) {
		this.assignee = task.getAssignee();
		this.groupId = task.getGroupId();
		this.owner = task.getOwner();
		this.taskName = task.getTaskName();
		this.internalTaskId = task.getInternalTaskId();
		this.executionId = task.getExecutionId();
		this.createDate = task.getCreateDate();
		this.finishDate = task.getFinishDate();
		this.processInstance = task.getProcessInstance();
		this.isFinished = task.isFinished();
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	@Override
	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	@Override
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	@Override
	public ProcessInstance getRootProcessInstance() {
		return processInstance.getRootProcessInstance();
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	@Override
	public String getInternalTaskId() {
		return internalTaskId;
	}

	public void setInternalTaskId(String internalTaskId) {
		this.internalTaskId = internalTaskId;
	}

	@Override
	public UserData getOwner() {
		return owner;
	}

	public void setOwner(UserData owner) {
		this.owner = owner;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public String getCreator() {
		return processInstance != null ? processInstance.getCreator().getLogin() : null;
	}

	@Override
	public String getAssignee() {
		return assignee;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	@Override
	public ProcessDefinitionConfig getProcessDefinition() {
		return processInstance != null ? processInstance.getDefinition() : null;
	}

	@Override
	public ProcessStateConfiguration getCurrentProcessStateConfiguration() {
    	/* Find current state by action name */
		ProcessDefinitionConfig processDefinitionConfig = getProcessDefinition();

		if(processDefinitionConfig == null)
			return null;

		String stateName = getTaskName();

		return processDefinitionConfig.getProcessStateConfigurationByName(stateName);
	}

	@Override
	public String getInternalProcessId() {
		return processInstance != null ? processInstance.getInternalId() : null;
	}

	@Override
	public String getExternalProcessId() {
		return processInstance != null ? processInstance.getExternalKey() : null;
	}

	@Override
	public String toString() {
		return "BpmTaskBean{" +
				"assignee='" + assignee + '\'' +
				", groupId='" + groupId + '\'' +
				", owner=" + owner +
				", taskName='" + taskName + '\'' +
				", internalTaskId='" + internalTaskId + '\'' +
				", executionId='" + executionId + '\'' +
				", createDate=" + createDate +
				", finishDate=" + finishDate +
				", isFinished=" + isFinished +
				'}';
	}

	public static List<BpmTaskBean> asBeans(List<? extends BpmTask> list) {
		List<BpmTaskBean> result = new ArrayList<BpmTaskBean>();

		for (BpmTask task : list) {
			result.add(new BpmTaskBean(task));
		}
		return result;
	}
}
