package org.aperteworkflow.webapi.main.processes.action.domain;

import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;
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
