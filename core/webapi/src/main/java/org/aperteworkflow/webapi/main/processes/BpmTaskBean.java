package org.aperteworkflow.webapi.main.processes;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.Serializable;
import java.util.Date;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Process Instance Bean
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class BpmTaskBean extends AbstractResultBean
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
	private String stepInfo;

	public static BpmTaskBean createFrom(BpmTask task, I18NSource messageSource)
	{
        BpmTaskBean processBean = new BpmTaskBean();
		processBean.processName = messageSource.getMessage(task.getProcessDefinition().getDescription());
		processBean.name = task.getTaskName();
		processBean.code = nvl(task.getExternalProcessId(), task.getInternalProcessId());
		processBean.creationDate = task.getCreateDate();
		processBean.assignee = task.getAssignee();
		processBean.creator = task.getCreator();
		processBean.taskId = task.getInternalTaskId();
		processBean.internalProcessId = task.getInternalProcessId();
		processBean.processStateConfigurationId = task.getCurrentProcessStateConfiguration().getId().toString();
		processBean.deadline = task.getDeadlineDate();
		processBean.tooltip = messageSource.getMessage(task.getProcessDefinition().getComment());
		processBean.step = messageSource.getMessage(task.getCurrentProcessStateConfiguration().getDescription());
		processBean.stepInfo = task.getStepInfo();
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

	public String getStepInfo() {
		return stepInfo;
	}

	public void setStepInfo(String stepInfo) {
		this.stepInfo = stepInfo;
	}
}
