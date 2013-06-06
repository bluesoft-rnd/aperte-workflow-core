package org.aperteworkflow.webapi.main.processes.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider.UsersQueuesDTO;


@Controller
public class QueuesController 
{
	private static Logger logger = Logger.getLogger(QueuesController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST, value = "/queues/loadForUser.json")
	@ResponseBody
	public Collection<UsersQueuesDTO> alertsLoadJson(HttpServletRequest request)
	{	
		final Collection<UsersQueuesDTO> queues = new ArrayList<UsersQueuesDTO>();
		
		ProcessToolRegistry reg = ProcessToolRegistry.ThreadUtil.getThreadRegistry();
		if(reg == null)
		{
			ServletContext context = request.getSession().getServletContext();	
			reg = (ProcessToolRegistry)context.getAttribute(ProcessToolRegistry.class.getName());
		}
		
		IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
		final UserData user = authorizationService.getUserByRequest(request);
		
		if(user == null)
			return queues;
		
		reg.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(ctx.getRegistry(), user.getLogin());
				queues.addAll(userQueuesSizeProvider.getUserProcessQueueSize());
				
			}
		});
		
        return queues;

	}
	

}
