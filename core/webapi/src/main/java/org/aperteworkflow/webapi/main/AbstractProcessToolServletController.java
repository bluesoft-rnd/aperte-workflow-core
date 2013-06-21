package org.aperteworkflow.webapi.main;

import javax.servlet.http.HttpServletRequest;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import org.aperteworkflow.webapi.context.impl.WebProcessToolContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

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

    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private ProcessToolRegistry processToolRegistry;
    
    public AbstractProcessToolServletController()
    {
    	SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
	/** Initilize context */
	protected IProcessToolRequestContext initilizeContext(HttpServletRequest request)
	{
		IProcessToolRequestContext context = WebProcessToolContextFactory.create(request);
		
		return context;
	}
	
	protected EventBus getEventBus()
	{
		return this.eventBus;
	}
	
	protected ProcessToolRegistry getProcessToolRegistry()
	{
		return this.processToolRegistry;
	}

}
