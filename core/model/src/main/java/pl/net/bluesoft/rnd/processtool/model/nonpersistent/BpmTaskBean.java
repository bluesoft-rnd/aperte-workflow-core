package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.io.Serializable;
import java.util.*;

/**
 * User: POlszewski
 * Date: 2013-06-19
 * Time: 14:29
 */
public class BpmTaskBean extends AbstractBpmTask implements Serializable {
	private static final long serialVersionUID = -6922749510138539783L;

	private String assignee;
	private String groupId;
	private String taskName;
	private String internalTaskId;
	private String executionId;
	private Date createDate;
	private Date finishDate;
	private ProcessInstance processInstance;
	private ProcessDefinitionConfig processDefinition;
	private boolean finished;
	private Date deadlineDate;
	private String stepInfo;
    private Collection<String> potentialOwners = new HashSet<String>();
    private Collection<String> queues = new HashSet<String>();

	public BpmTaskBean() {
	}

	public BpmTaskBean(BpmTask task) {
		this.assignee = task.getAssignee();
		this.groupId = task.getGroupId();
		this.taskName = task.getTaskName();
		this.internalTaskId = task.getInternalTaskId();
		this.executionId = task.getExecutionId();
		this.createDate = task.getCreateDate();
		this.finishDate = task.getFinishDate();
		this.processInstance = task.getProcessInstance();
		this.finished = task.isFinished();
		this.processDefinition = task.getProcessDefinition();
		this.stepInfo = task.getStepInfo();
        this.potentialOwners.addAll(task.getPotentialOwners());
        this.queues.addAll(task.getQueues());
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
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

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	@Override
	public ProcessDefinitionConfig getProcessDefinition() {
		return processDefinition;
	}

	public void setProcessDefinition(ProcessDefinitionConfig processDefinition) {
		this.processDefinition = processDefinition;
	}

	@Override
	public String getInternalTaskId() {
		return internalTaskId;
	}

	public void setInternalTaskId(String internalTaskId) {
		this.internalTaskId = internalTaskId;
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
		return processInstance != null ? processInstance.getCreatorLogin() : null;
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
	public Date getDeadlineDate() {
		return deadlineDate;
	}

	public void setDeadlineDate(Date deadlineDate) {
		this.deadlineDate = deadlineDate;
	}

	@Override
	public String getStepInfo() {
		return stepInfo;
	}

	public void setStepInfo(String stepInfo) {
		this.stepInfo = stepInfo;
	}

    public Collection<String> getPotentialOwners() {
        return potentialOwners;
    }

    public Collection<String> getQueues() {
        return queues;
    }

    public void setQueues(Collection<String> queues) {
        this.queues = queues;
    }

    public void setPotentialOwners(Collection<String> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public static List<BpmTaskBean> asBeans(List<? extends BpmTask> list) {
		List<BpmTaskBean> result = new ArrayList<BpmTaskBean>();

		for (BpmTask task : list) {
			result.add(new BpmTaskBean(task));
		}
		return result;
	}

	public static List<String> getTaskIds(Collection<? extends BpmTask> tasks) {
		List<String> result = new ArrayList<String>();

		for (BpmTask task : tasks) {
			result.add(task.getInternalTaskId());
		}
		return result;
	}
}
