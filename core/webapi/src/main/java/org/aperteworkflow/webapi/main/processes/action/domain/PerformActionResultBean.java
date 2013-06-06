package org.aperteworkflow.webapi.main.processes.action.domain;

import org.aperteworkflow.webapi.main.processes.BpmTaskBean;

/**
 * Result of perform action 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class PerformActionResultBean extends AbstractResultBean
{
	private static final long serialVersionUID = 4831982682204632002L;
	
	private BpmTaskBean nextTask;
	
	public BpmTaskBean getNextTask() {
		return nextTask;
	}
	public void setNextTask(BpmTaskBean nextTask) {
		this.nextTask = nextTask;
	}
	
	

}
