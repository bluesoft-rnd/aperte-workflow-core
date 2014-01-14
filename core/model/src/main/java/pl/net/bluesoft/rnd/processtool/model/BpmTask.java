package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.Collection;
import java.util.Date;

public interface BpmTask {
    boolean isFinished();
    Date getFinishDate();
    Date getCreateDate();
    String getExecutionId();	

    ProcessInstance getProcessInstance();
	ProcessInstance getRootProcessInstance();
	String getInternalProcessId();
	String getExternalProcessId();
    String getInternalTaskId();

    String getTaskName();
	String getCreator();
    String getAssignee();
    Collection<String> getPotentialOwners();
    Collection<String> getQueues();
	String getGroupId();

	ProcessDefinitionConfig getProcessDefinition();

	/** Method returns current state configuration */
    ProcessStateConfiguration getCurrentProcessStateConfiguration();

	/** Get deadline */
	Date getDeadlineDate();

	String getStepInfo();
}
