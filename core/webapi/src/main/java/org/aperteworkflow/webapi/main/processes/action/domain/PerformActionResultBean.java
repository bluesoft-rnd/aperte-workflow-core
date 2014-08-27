package org.aperteworkflow.webapi.main.processes.action.domain;

import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;
import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBean;

/**
 * Result of perform action 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class PerformActionResultBean extends AbstractResultBean
{
	private static final long serialVersionUID = 4831982682204632002L;
	
	private TasksListViewBean nextTask;
	
	public TasksListViewBean getNextTask() {
		return nextTask;
	}
	public void setNextTask(TasksListViewBean nextTask) {
		this.nextTask = nextTask;
	}
	
	

}
