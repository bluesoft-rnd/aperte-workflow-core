package org.aperteworkflow.webapi.main;

import javax.servlet.http.HttpServletRequest;

import org.aperteworkflow.webapi.context.impl.IWebProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import org.aperteworkflow.webapi.context.impl.WebProcessToolContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import com.google.common.eventbus.EventBus;

/**
 * Base controller based on Process Tool Context 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class AbstractProcessToolServletController 
{
    public static final String SYSTEM_SOURCE = "System";

    // You have to leave required as false for Liferay 6.2
    @Autowired(required = false)
    private EventBus eventBus;

    @Autowired(required = false)
    private ProcessToolRegistry processToolRegistry;

    @Autowired(required = false)
    private IAuthorizationService authorizationService;

    @Autowired(required = false)
    private IWebProcessToolContextFactory webProcessToolContextFactory;
    
	/** Initilize context */
	protected IProcessToolRequestContext initilizeContext(HttpServletRequest request, ProcessToolSessionFactory sessionFactory)
    {

		IProcessToolRequestContext context = getWebProcessToolContextFactory().create(request);
		return context;
	}
	
	protected EventBus getEventBus()
    {
        if(eventBus == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		return this.eventBus;
	}
	
	protected ProcessToolRegistry getProcessToolRegistry()
    {
        if(processToolRegistry == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return this.processToolRegistry;
	}

    public IAuthorizationService getAuthorizationService()
    {
        if(authorizationService == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return authorizationService;
    }

    public IWebProcessToolContextFactory getWebProcessToolContextFactory()
    {
        if(webProcessToolContextFactory == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return webProcessToolContextFactory;
    }
}
