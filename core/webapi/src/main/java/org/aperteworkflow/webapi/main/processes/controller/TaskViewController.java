package org.aperteworkflow.webapi.main.processes.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.BpmTaskBean;
import org.aperteworkflow.webapi.main.ui.TaskViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

@Controller
public class TaskViewController extends AbstractProcessToolServletController
{
	private static Logger logger = Logger.getLogger(TaskViewController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST, value = "/task/claimTaskFromQueue")
	@ResponseBody
	public BpmTaskBean claimTaskFromQueue(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		
		final I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
		
		final String queueName = request.getParameter("queueName");
		final String taskId = request.getParameter("taskId");
		
		if(isNull(taskId))
		{
			response.getWriter().print(messageSource.getMessage("request.performaction.error.notaskid"));
			return null;
		}
		else if(isNull(queueName))
		{
			response.getWriter().print(messageSource.getMessage("request.performaction.error.noqueuename"));
			return null;
		}
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
		{
			response.getWriter().print(messageSource.getMessage("request.handle.error.nouser"));
			return null;
		}
		
		BpmTaskBean taskBean = context.getRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<BpmTaskBean>() 
		{

			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
				BpmTask task = context.getBpmSession().getTaskData(taskId);

				BpmTask newTask = context.getBpmSession().assignTaskFromQueue(queueName, task);
				
				BpmTaskBean taskBean = BpmTaskBean.createFrom(newTask, messageSource);
				
				return taskBean;
			}
		});
		
		return taskBean;
		
	}
    
	@RequestMapping(method = RequestMethod.POST, value = "/task/loadTask")
	@ResponseBody
	public void loadTask(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		final I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
		
		/* Get process state configuration db id */
		final String processStateConfigurationId = request.getParameter("processStateConfigurationId");
		final String taskId = request.getParameter("taskId");
		
		if(isNull(taskId))
		{
			response.getWriter().print(messageSource.getMessage("request.performaction.error.notaskid"));
			return;
		}
		else if(isNull(processStateConfigurationId))
		{
			response.getWriter().print(messageSource.getMessage("request.performaction.error.nocofnigurationid"));
			return;
		}
		
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
		{
			response.getWriter().print(messageSource.getMessage("request.handle.error.nouser"));
			return;
		}
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				BpmTask task = context.getBpmSession().getTaskData(taskId);
				
				if(task == null)
					task = context.getBpmSession().getHistoryTask(taskId);

				ProcessStateConfiguration config = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(Long.parseLong(processStateConfigurationId));

				/* Load view widgets */
				List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(config.getWidgets());
				Collections.sort(widgets, new Comparator<ProcessStateWidget>() {

					@Override
					public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
						// TODO Auto-generated method stub
						return widget1.getPriority().compareTo(widget2.getPriority());
					}
				});
				
				/* Load view actions */
				List<ProcessStateAction> actions = new ArrayList<ProcessStateAction>(config.getActions());
				Collections.sort(actions, new Comparator<ProcessStateAction>() {

					@Override
					public int compare(ProcessStateAction action1, ProcessStateAction action2)
                    {
                        if(action1.getPriority() == null)
                            return -1;
                        if(action2.getPriority() == null)
                            return 1;
						return action1.getPriority().compareTo(action2.getPriority());
					}
				});
				
				TaskViewBuilder taskViewBuilder = new TaskViewBuilder()
					.setWidgets(widgets)
					.setActions(actions)
					.setI18Source(messageSource)
					.setUser(context.getUser())
                    .setCtx(ctx)
					.setTask(task);

				try
				{
					taskViewBuilder.processView(response.getWriter());
				}
				catch(IOException ex)
				{
					logger.log(Level.SEVERE, "Problem during task view generation. TaskId="+taskId, ex);
				}

			}
		});
	}

	private static boolean isNull(String value) {
		return value == null || value.isEmpty() || "null".equals(value);
	}
}
