package org.aperteworkflow.webapi.main.processes;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Process Instance Bean
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class BpmTaskBean implements Serializable
{
	private static final long serialVersionUID = 8814138252434090661L;
	
	private String name;
	private String processName;
	private String code;
	private String creator;
	private String assignee;
	private Date creationDate;
	private Date deadline;
	private String taskId;
	private String internalProcessId;
	private String processStateConfigurationId;
	private String tooltip;
	private String queueName;
    private String step;
	
	public static BpmTaskBean createFrom(BpmTask task, I18NSource messageSource)
	{
        String processExteralKey = task.getProcessInstance().getExternalKey();

        BpmTaskBean processBean = new BpmTaskBean();
		processBean.setProcessName(messageSource.getMessage(task.getProcessDefinition().getDescription()));
		processBean.setName(task.getTaskName());
		processBean.setCode(processExteralKey == null ? task.getExecutionId() : processExteralKey);
		processBean.setCreationDate(task.getCreateDate());
		processBean.setAssignee(task.getAssignee());
		processBean.setCreator(task.getCreator());
		processBean.setTaskId(task.getInternalTaskId());
		processBean.setInternalProcessId(task.getProcessInstance().getInternalId());
		processBean.setProcessStateConfigurationId(task.getCurrentProcessStateConfiguration().getId().toString());
		processBean.setDeadline(task.getDeadlineDate());
		processBean.setTooltip(messageSource.getMessage(task.getProcessDefinition().getComment()));
        processBean.setStep(messageSource.getMessage(task.getTaskName()));
		return processBean;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getInternalProcessId() {
		return internalProcessId;
	}
	public void setInternalProcessId(String processId) {
		this.internalProcessId = processId;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getProcessStateConfigurationId() {
		return processStateConfigurationId;
	}
	public void setProcessStateConfigurationId(String processWidgetStatedIds) {
		this.processStateConfigurationId = processWidgetStatedIds;
	}
	public String getTooltip() {
		return tooltip;
	}
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}


    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
