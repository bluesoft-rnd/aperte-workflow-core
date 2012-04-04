package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.io.Serializable;
import java.util.Date;

public interface BpmTask extends Serializable {
    boolean isFinished();

    Date getCreateDate();

    Date getFinishDate();

    String getExecutionId();

    ProcessInstance getProcessInstance();

    ProcessDefinitionConfig getProcessDefinition();

    String getInternalTaskId();

    String getInternalProcessId();

    String getExternalProcessId();

    UserData getOwner();

    String getTaskName();

    String getCreator();

    String getAssignee();
}
