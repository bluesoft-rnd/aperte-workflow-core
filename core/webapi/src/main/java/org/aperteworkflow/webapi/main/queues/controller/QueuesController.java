package org.aperteworkflow.webapi.main.queues.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider.UsersQueuesDTO;

@Controller(value = "QueuesController")
public class QueuesController extends AbstractProcessToolServletController
{
	private static Logger logger = Logger.getLogger(QueuesController.class.getName());

	
	@RequestMapping(method = RequestMethod.GET, value = "/getUserQueues.json")
	@ResponseBody
	public Collection<UsersQueuesDTO> getUserQueues(final HttpServletRequest request)
	{
		logger.log(Level.INFO,"getUserQueues ...");
		
		long t0 = System.currentTimeMillis();
		
		final IProcessToolRequestContext context = this.initilizeContext(request, getProcessToolRegistry().getProcessToolSessionFactory());
		final Collection<UsersQueuesDTO> userQueues = new ArrayList<UsersQueuesDTO>();
		
		if(!context.isUserAuthorized())
		{
			return userQueues;
		}

		long t1 = System.currentTimeMillis();
		
		UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(context.getUser().getLogin(), context.getMessageSource());
		Collection<UsersQueuesDTO> queues = userQueuesSizeProvider.getUserProcessQueueSize();
		userQueues.addAll(queues);
		
		long t2 = System.currentTimeMillis();
		
		logger.log(Level.INFO, "getUserQueues total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms " 
				);
		
		return userQueues;
	}
}
