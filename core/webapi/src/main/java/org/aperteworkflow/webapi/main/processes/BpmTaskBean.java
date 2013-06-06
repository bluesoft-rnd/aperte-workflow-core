package org.aperteworkflow.webapi.main.processes;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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


	
}
