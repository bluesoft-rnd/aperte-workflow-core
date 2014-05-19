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
    private String assignedTo = "";
    private String externalKey;
    private String internalId;
    private List<String> availableActions = new ArrayList<String>();
    private String taskName = "";
    private String taskInternalId;
    private List<String> taskPotentialOwners;

    public static List<ProcessInstanceBean> createBeans(ProcessInstance instance, I18NSource messageSource, ProcessToolBpmSession bpmSession) {

        ProcessDefinitionConfig definition = instance.getDefinition();

        List<BpmTask> taskList = new ArrayList<BpmTask>(bpmSession.findProcessTasks(instance));
        List<ProcessInstanceLog> processLogs = new ArrayList<ProcessInstanceLog>(instance.getProcessLogs());

        List<ProcessInstanceBean> processInstanceBeans = createBeansForAllTasks(instance, definition, taskList, processLogs);
        return processInstanceBeans;
    }

    private static List<ProcessInstanceBean> createBeansForAllTasks(ProcessInstance processInstance, ProcessDefinitionConfig definition, List<BpmTask> taskList, List<ProcessInstanceLog> processLogs) {
        List<ProcessInstanceBean> processInstanceBeans = new ArrayList<ProcessInstanceBean>();
        ProcessInstanceBean bean;
        for (final BpmTask task : taskList) {
            bean = new ProcessInstanceBean();

            ProcessStateConfiguration processState = task.getCurrentProcessStateConfiguration();

            bean.definitionId = definition.getId();
            bean.definitionDescription = definition.getDescription();
            bean.bpmDefinitionKey = definition.getBpmDefinitionKey();
            bean.assignedTo = task.getAssignee();
            bean.taskInternalId = task.getInternalTaskId();
            bean.taskName = task.getTaskName();
            bean.creatorLogin = processInstance.getCreatorLogin();
            bean.creationDate = processInstance.getCreateDate();
            bean.externalKey = processInstance.getExternalKey();
            bean.internalId = processInstance.getInternalId();
            bean.taskPotentialOwners = (List<String>) task.getPotentialOwners();

            for (ProcessInstanceLog pl : processLogs) {
                bean.status = pl.getState() != null ? pl.getState().getDescription() + pl.getState().getName() : "none";
            }
            if (processState != null && !processState.getActions().isEmpty()) {
                for (ProcessStateAction action : processState.getActions()) {
                    bean.availableActions.add(action.getBpmName());
                }
            }
            processInstanceBeans.add(bean);
        }
        return processInstanceBeans;
    }

    public List<String> getTaskPotentialOwners() {
        return taskPotentialOwners;
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

    public String getInternalId() {
        return internalId;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public String getTaskName() {
        return taskName;
    }

}
