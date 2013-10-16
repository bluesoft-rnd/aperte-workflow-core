package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;

import java.util.Date;

/**
 * User: POlszewski
 * Date: 2013-07-15
 * Time: 11:23
 */
public abstract class AbstractBpmTask implements BpmTask {
	@Override
	public ProcessInstance getRootProcessInstance() {
		return getProcessInstance().getRootProcessInstance();
	}

	@Override
	public ProcessStateConfiguration getCurrentProcessStateConfiguration() {
		ProcessDefinitionConfig processDefinitionConfig = getProcessDefinition();

		if (processDefinitionConfig == null) {
			return null;
		}
		return processDefinitionConfig.getProcessStateConfigurationByName(getTaskName());
	}

	@Override
	public String getInternalProcessId() {
		return getProcessInstance() != null ? getProcessInstance().getInternalId() : null;
	}

	@Override
	public String getExternalProcessId() {
		return getProcessInstance() != null ? getProcessInstance().getExternalKey() : null;
	}

	@Override
	public Date getDeadlineDate() {
		ProcessDeadline deadline = getProcessInstance().getDeadline(this);
		return deadline != null ? deadline.getDueDate() : null;
	}

	@Override
	public String toString() {
		return "BpmTask{" +
				"assignee='" + getAssignee() + '\'' +
				", groupId='" + getGroupId() + '\'' +
				", taskName='" + getTaskName() + '\'' +
				", internalTaskId='" + getInternalTaskId() + '\'' +
				", executionId='" + getExecutionId() + '\'' +
				", createDate=" + getCreateDate() +
				", finishDate=" + getFinishDate() +
				", isFinished=" + isFinished() +
				'}';
	}
}
