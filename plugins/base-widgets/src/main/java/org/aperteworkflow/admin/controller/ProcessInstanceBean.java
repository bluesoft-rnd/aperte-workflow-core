package org.aperteworkflow.admin.controller;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcessInstanceBean extends AbstractResultBean {

    private Long definitionId;
    private String definitionDescription;
    private String bpmDefinitionKey;
    private String creatorLogin;
    private Date creationDate;
    private String status;
    private String assignedTo;
    private String externalKey;
    private String processInternalId;
    private List<Action> availableActions = new ArrayList<Action>();
    private String taskName;
    private String taskInternalId;

    public static List<ProcessInstanceBean> createBeans(ProcessInstance instance, I18NSource messageSource, ProcessToolBpmSession bpmSession) {
        ProcessDefinitionConfig definition = instance.getDefinition();
        List<BpmTask> taskList = new ArrayList<BpmTask>(bpmSession.findProcessTasks(instance));
        List<ProcessInstanceLog> processLogs = new ArrayList<ProcessInstanceLog>(instance.getProcessLogs());
        return createBeansForAllTasks(messageSource, instance, definition, taskList, processLogs);
    }

    private static List<ProcessInstanceBean> createBeansForAllTasks(I18NSource messageSource, ProcessInstance processInstance, ProcessDefinitionConfig definition, List<BpmTask> taskList, List<ProcessInstanceLog> processLogs) {
        List<ProcessInstanceBean> processInstanceBeans = new ArrayList<ProcessInstanceBean>();
        ProcessInstanceBean bean;
        for (final BpmTask task : taskList) {
            ProcessStateConfiguration processState = task.getCurrentProcessStateConfiguration();
            bean = new ProcessInstanceBean();
            bean.definitionId = definition.getId();
            bean.definitionDescription = definition.getDescription();
            bean.bpmDefinitionKey = definition.getBpmDefinitionKey();
            bean.assignedTo = task.getAssignee();
            bean.taskInternalId = task.getInternalTaskId();
            bean.taskName = messageSource.getMessage(task.getTaskName());
            bean.creatorLogin = processInstance.getCreatorLogin();
            bean.creationDate = processInstance.getCreateDate();
            bean.externalKey = processInstance.getExternalKey();
            bean.processInternalId = processInstance.getInternalId();

            for (ProcessInstanceLog pl : processLogs) {
                bean.status = pl.getState() != null ? pl.getState().getDescription() + pl.getState().getName() : "none";
            }
            if (processState != null && !processState.getActions().isEmpty()) {
                for (ProcessStateAction action : processState.getActions()) {
                    bean.availableActions.add(new Action(action.getBpmName(), messageSource.getMessage(action.getLabel())));
                }
            }
            processInstanceBeans.add(bean);
        }
        return processInstanceBeans;
    }

    public String getTaskInternalId() {
        return taskInternalId;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public String getDefinitionDescription() {
        return definitionDescription;
    }

    public String getBpmDefinitionKey() {
        return bpmDefinitionKey;
    }

    public String getCreatorLogin() {
        return creatorLogin;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getProcessInternalId() {
        return processInternalId;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public List<Action> getAvailableActions() {
        return availableActions;
    }

    public String getTaskName() {
        return taskName;
    }

}
