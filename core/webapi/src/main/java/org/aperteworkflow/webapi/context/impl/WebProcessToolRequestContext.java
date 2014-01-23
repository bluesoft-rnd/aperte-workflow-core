package org.aperteworkflow.webapi.context.impl;

import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.LinkedList;


public class WebProcessToolRequestContext implements IProcessToolRequestContext
{
	private UserData userData;
	private ProcessToolBpmSession bpmSession;
	private I18NSource messageSource;
    private Collection<String> userQueues;

	@Override
	public UserData getUser() {
		return userData;
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
	
	void setBpmSession(ProcessToolBpmSession bpmSession)
	{
		this.bpmSession = bpmSession;
	}

	@Override
	public boolean isUserAuthorized() {
		return userData != null;
	}

	public I18NSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(I18NSource messageSource) {
		this.messageSource = messageSource;
	}

    public Collection<String> getUserQueues()
    {
        if(userQueues == null)
        {
            userQueues = new LinkedList<String>();
            for(ProcessQueue processQueue:  bpmSession.getUserAvailableQueues())
                userQueues.add(processQueue.getName());

        }
        return userQueues;
    }

}
