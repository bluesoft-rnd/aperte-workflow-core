package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.*;

/**
 * User: POlszewski
 * Date: 2013-07-15
 * Time: 15:13
 */
public class BpmTaskDerivedBean implements BpmTask {
	private final BpmTask task;
	private boolean isFinishedPresent;
	private boolean isFinished;
	private boolean finishDatePresent;
	private Date finishDate;
	private boolean createDeatePresent;
	private Date createDate;
	private boolean executionIdPresent;
	private String executionId;
	private boolean processInstancePresent;
	private ProcessInstance processInstance;
	private boolean rootProcessInstancePresent;
	private ProcessInstance rootProcessInstance;
	private boolean internalProcessIdPresent;
	private String internalProcessId;
	private boolean externalProcessIdPresent;
	private String externalProcessId;
	private boolean internalTaskIdPresent;
	private String internalTaskId;
	private boolean taskNamePresent;
	private String taskName;
	private boolean creatorPresent;
	private String creator;
	private boolean assigneePresent;
	private String assignee;
	private boolean groupIdPresent;
	private String groupId;
	private boolean processDefinitionPresent;
	private ProcessDefinitionConfig processDefinition;
	private boolean currentProcessConfigurationPresent;
	private ProcessStateConfiguration currentProcessConfiguration;
	private boolean deadlineDatePresent;
	private Date deadlineDate;
	private boolean stepInfoPresent;
	private String stepInfo;
    private Collection<String> potentialOwners = new HashSet<String>();
    private boolean potentialOwnersPresent;
    private Collection<String> queues = new HashSet<String>();
    private boolean queuesPresent;

	public BpmTaskDerivedBean(BpmTask task) {
		this.task = task;
	}

	@Override
	public boolean isFinished() {
		if (!isFinishedPresent) {
			setFinished(task.isFinished());
		}
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
		isFinishedPresent = true;
	}

	@Override
	public Date getFinishDate() {
		if (!finishDatePresent) {
			setFinishDate(task.getFinishDate());
		}
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
		this.finishDatePresent = true;
	}

	@Override
	public Date getCreateDate() {
		if (!createDeatePresent) {
			setCreateDate(task.getCreateDate());
		}
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
		this.createDeatePresent = true;
	}

	@Override
	public String getExecutionId() {
		if (!executionIdPresent) {
			setExecutionId(task.getExecutionId());
		}
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
		this.executionIdPresent = true;
	}

	@Override
	public ProcessInstance getProcessInstance() {
		if (!processInstancePresent) {
			setProcessInstance(task.getProcessInstance());
		}
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
		this.processInstancePresent = true;
	}

	@Override
	public ProcessInstance getRootProcessInstance() {
		if (!rootProcessInstancePresent) {
			setRootProcessInstance(task.getRootProcessInstance());
		}
		return rootProcessInstance;
	}

	public void setRootProcessInstance(ProcessInstance rootProcessInstance) {
		this.rootProcessInstance = rootProcessInstance;
		this.rootProcessInstancePresent = true;
	}

	@Override
	public String getInternalProcessId() {
		if (!internalProcessIdPresent) {
			setInternalProcessId(task.getInternalProcessId());
		}
		return internalProcessId;
	}

	public void setInternalProcessId(String internalProcessId) {
		this.internalProcessId = internalProcessId;
		this.internalProcessIdPresent = true;
	}

	@Override
	public String getExternalProcessId() {
		if (!externalProcessIdPresent) {
			setExternalProcessId(task.getExternalProcessId());
		}
		return externalProcessId;
	}

	public void setExternalProcessId(String externalProcessId) {
		this.externalProcessId = externalProcessId;
		this.externalProcessIdPresent = true;
	}

	@Override
	public String getInternalTaskId() {
		if (!internalTaskIdPresent) {
			setInternalTaskId(task.getInternalTaskId());
		}
		return internalTaskId;
	}

	public void setInternalTaskId(String internalTaskId) {
		this.internalTaskId = internalTaskId;
		this.internalTaskIdPresent = true;
	}

	@Override
	public String getTaskName() {
		if (!taskNamePresent) {
			setTaskName(task.getTaskName());
		}
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
		this.taskNamePresent = true;
	}

	@Override
	public String getCreator() {
		if (!creatorPresent) {
			setCreator(task.getCreator());
		}
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
		this.creatorPresent = true;
	}

	@Override
	public String getAssignee() {
		if (!assigneePresent) {
			setAssignee(task.getAssignee());
		}
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
		this.assigneePresent = true;
	}

	@Override
	public String getGroupId() {
		if (!groupIdPresent) {
			setGroupId(task.getGroupId());
		}
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
		this.groupIdPresent = true;
	}

	@Override
	public ProcessDefinitionConfig getProcessDefinition() {
		if (!processDefinitionPresent) {
			setProcessDefinition(task.getProcessDefinition());
		}
		return processDefinition;
	}

	public void setProcessDefinition(ProcessDefinitionConfig processDefinition) {
		this.processDefinition = processDefinition;
		this.processDefinitionPresent = true;
	}

	@Override
	public ProcessStateConfiguration getCurrentProcessStateConfiguration() {
		if (!currentProcessConfigurationPresent) {
			setCurrentProcessConfiguration(task.getCurrentProcessStateConfiguration());
		}
		return currentProcessConfiguration;
	}

	public void setCurrentProcessConfiguration(ProcessStateConfiguration currentProcessConfiguration) {
		this.currentProcessConfiguration = currentProcessConfiguration;
		this.currentProcessConfigurationPresent = true;
	}

	@Override
	public Date getDeadlineDate() {
		if (!deadlineDatePresent) {
			setDeadlineDate(task.getDeadlineDate());
		}
		return deadlineDate;
	}

	public void setDeadlineDate(Date deadlineDate) {
		this.deadlineDate = deadlineDate;
		this.deadlineDatePresent = true;
	}

	@Override
	public String getStepInfo() {
		if (!stepInfoPresent) {
			setStepInfo(task.getStepInfo());
		}
		return stepInfo;
	}

	public void setStepInfo(String stepInfo) {
		this.stepInfo = stepInfo;
		this.stepInfoPresent = true;
	}


    public Collection<String> getPotentialOwners()
    {
        if (!potentialOwnersPresent) {
            setPotentialOwners(task.getPotentialOwners());
        }
        return potentialOwners;
    }


    public void setPotentialOwners(Collection<String> potentialOwners) {
        this.potentialOwners = potentialOwners;
        this.potentialOwnersPresent = true;
    }

    public Collection<String> getQueues()
    {
        if (!queuesPresent) {
            setQueues(task.getQueues());
        }
        return queues;
    }


    public void setQueues(Collection<String> queues) {
        this.queues = queues;
        this.queuesPresent = true;
    }

    public static List<BpmTaskDerivedBean> asBeans(List<? extends BpmTask> list) {
		List<BpmTaskDerivedBean> result = new ArrayList<BpmTaskDerivedBean>();

		for (BpmTask task : list) {
			result.add(new BpmTaskDerivedBean(task));
		}
		return result;
	}
}
