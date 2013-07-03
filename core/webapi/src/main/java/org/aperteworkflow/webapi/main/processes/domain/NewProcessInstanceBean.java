package org.aperteworkflow.webapi.main.processes.domain;

import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;

/**
 * Bean representing new process insance 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class NewProcessInstanceBean  extends AbstractResultBean
{
	private String taskId;
	private String processStateConfigurationId;
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getProcessStateConfigurationId() {
		return processStateConfigurationId;
	}
	public void setProcessStateConfigurationId(String processStateConfigurationId) {
		this.processStateConfigurationId = processStateConfigurationId;
	}
	
	

}
