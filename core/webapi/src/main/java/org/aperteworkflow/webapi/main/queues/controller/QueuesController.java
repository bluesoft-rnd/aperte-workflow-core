package org.aperteworkflow.webapi.main.queues.controller;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider.UsersQueuesDTO;

@Controller
public class QueuesController extends AbstractProcessToolServletController
{
	@RequestMapping(method = RequestMethod.GET, value = "/queues/getUserQueues.json")
	@ResponseBody
	public Collection<UsersQueuesDTO> getUserQueues(final HttpServletRequest request)
	{
		final IProcessToolRequestContext context = this.initilizeContext(request);
		final Collection<UsersQueuesDTO> userQueues = new ArrayList<UsersQueuesDTO>();
		
		if(!context.isUserAuthorized())
		{
			return userQueues;
		}
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(ctx.getRegistry(), context.getUser().getLogin(), context.getMessageSource());
				Collection<UsersQueuesDTO> queues = userQueuesSizeProvider.getUserProcessQueueSize();
				
				userQueues.addAll(queues);

				
			}
		});
		
		return userQueues;
	}
}
