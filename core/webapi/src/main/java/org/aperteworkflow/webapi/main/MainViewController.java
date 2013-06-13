package org.aperteworkflow.webapi.main;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.portlet.PortletContext;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider.UsersQueuesDTO;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.cquery.func.F;



@Controller(value = "MainViewController")  
@RequestMapping("VIEW")  
public class MainViewController 
{
	private static final String PROCESS_START_LIST = "processStartList";
	private static final String QUEUES_PARAMETER_NAME = "queues";
	private static final String USER_PARAMETER_NAME = "aperteUser";
	
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
	@RenderMapping ()
	 public ModelAndView handleRenderRequest(RenderRequest request,RenderResponse response,Model model)
	{  
		ModelAndView modelView = new ModelAndView();
		modelView.setViewName("main");
		
		processRequest(modelView, request);
		
	    return modelView;  
	 }  
	
	private void processRequest(final ModelAndView modelView, final RenderRequest request)
	{
		
		IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
		final UserData user = authorizationService.getUserByRequest(request);
		
		/* No user to process, abort */
		if(user == null)
			return;
		

		modelView.addObject(USER_PARAMETER_NAME, user);
		
		processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)request.getAttribute(ProcessToolBpmSession.class.getName());
				if(bpmSession == null)
				{
					bpmSession = ctx.getProcessToolSessionFactory().createSession(user, user.getRoleNames());
					request.setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
				}
				
				I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
				
				addUserQueues(modelView, user, ctx, messageSource);
				addProcessStartList(modelView, ctx, bpmSession);
				
			}
		});
	}
	
	/** Add user queeus to model */
	private void addUserQueues(ModelAndView modelView, UserData user, ProcessToolContext ctx, I18NSource messageSource)
	{
		UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(ctx.getRegistry(), user.getLogin(), messageSource);
		Collection<UsersQueuesDTO> queues = userQueuesSizeProvider.getUserProcessQueueSize();
		
		modelView.addObject(QUEUES_PARAMETER_NAME, queues);
	}
	
	/** Add process start definition */
	private void addProcessStartList(ModelAndView modelView,ProcessToolContext ctx, ProcessToolBpmSession bpmSession)
	{
		List<ProcessDefinitionConfig> orderedByProcessDescr = from(bpmSession.getAvailableConfigurations(ctx))
				.orderBy(new F<ProcessDefinitionConfig, String>() {
					@Override
					public String invoke(ProcessDefinitionConfig pdc) {
						return pdc.getDescription();
					}
				})
				.toList();
		
		modelView.addObject(PROCESS_START_LIST, orderedByProcessDescr);
	}

}
