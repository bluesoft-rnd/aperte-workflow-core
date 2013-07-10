package pl.net.bluesoft.rnd.processtool.model;

import java.util.Date;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;

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
	UserData getOwner();
	String getCreator();
    String getAssignee();
	String getGroupId();

	ProcessDefinitionConfig getProcessDefinition();

	/** Method returns current state configuration */
    ProcessStateConfiguration getCurrentProcessStateConfiguration();

	/** Get deadline */
	public Date getDeadlineDate()
	{
		for(ProcessInstanceAttribute attribute: processInstance.getProcessAttributes())
			if(attribute instanceof ProcessDeadline)
				return ((ProcessDeadline)attribute).getDueDate();

		return null;
	}
}
