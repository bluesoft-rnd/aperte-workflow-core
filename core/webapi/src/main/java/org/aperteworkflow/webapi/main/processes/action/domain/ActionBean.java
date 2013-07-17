package org.aperteworkflow.webapi.main.processes.action.domain;

import java.io.Serializable;

/**
 * Bean for process state action
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ActionBean implements Serializable 
{
	private String actionName;
	private String caption;
	private String tooltip;
	private String skipSaving;

	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getTooltip() {
		return tooltip;
	}
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	public String getSkipSaving() {
		return skipSaving;
	}
	public void setSkipSaving(String skipSaving) {
		this.skipSaving = skipSaving;
	}
	
	

}
