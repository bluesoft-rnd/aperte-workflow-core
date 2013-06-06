package org.aperteworkflow.webapi.context.impl;

import org.aperteworkflow.webapi.context.IProcessToolRequestContext;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;


public class WebProcessToolRequestContext implements IProcessToolRequestContext
{
	private UserData userData;
	private ProcessToolRegistry registry;
	private ProcessToolBpmSession bpmSession;

	@Override
	public UserData getUser() {
		return userData;
	}

	@Override
	public ProcessToolRegistry getRegistry() 
	{
		return registry;
	}
	
	@Override
	public ProcessToolBpmSession getBpmSession() 
	{
		return bpmSession;
	}
	
	void setUser(UserData userData)
	{
		this.userData = userData;
	}
	
	void setRegistry(ProcessToolRegistry registry)
	{
		this.registry = registry;
	}
	
	void setBpmSession(ProcessToolBpmSession bpmSession)
	{
		this.bpmSession = bpmSession;
	}

	@Override
	public boolean isUserAuthorized() {
		return userData != null;
	}

}
