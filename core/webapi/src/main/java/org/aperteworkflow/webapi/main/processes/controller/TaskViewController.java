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
import org.springframework.beans.factory.annotation.Autowired;
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
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

@Controller
public class TaskViewController extends AbstractProcessToolServletController
{
	private static Logger logger = Logger.getLogger(TaskViewController.class.getName());
	
    @Autowired
    private ProcessToolRegistry registry;
	
	@RequestMapping(method = RequestMethod.POST, value = "/task/claimTaskFromQueue")
	@ResponseBody
	public BpmTaskBean claimTaskFromQueue(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		logger.info("claimTaskFromQueue ...");
		long t0 = System.currentTimeMillis();
		
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
		final IProcessToolRequestContext context = this.initilizeContext(request,registry.getProcessToolSessionFactory());
		
		if(!context.isUserAuthorized())
		{
			response.getWriter().print(messageSource.getMessage("request.handle.error.nouser"));
			return null;
		}
		
		long t1 = System.currentTimeMillis();

		BpmTaskBean taskBean = registry.withProcessToolContext(new ReturningProcessToolContextCallback<BpmTaskBean>() 
		{

			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
				BpmTaskBean taskBean = null;
				BpmTask task = context.getBpmSession().getTaskData(taskId);
				BpmTask newTask = context.getBpmSession().assignTaskFromQueue(queueName, task);
				
				if (newTask!=null) {
					taskBean = BpmTaskBean.createFrom(newTask, messageSource);
				} else {
					try {
						response.getWriter().print(messageSource.getMessage("request.performaction.error.notask"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
				return taskBean;
			}
		});
		
		long t2 = System.currentTimeMillis();

		logger.log(Level.INFO, "claimTaskFromQueue total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms " 
				);
		
		return taskBean;
		
	}
    
	@RequestMapping(method = RequestMethod.POST, value = "/task/loadTask")
	@ResponseBody
	public void loadTask(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		logger.info("loadTask ...");
		long t0 = System.currentTimeMillis();
		
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
		
		long t1 = System.currentTimeMillis();
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request,registry.getProcessToolSessionFactory());
		
		if(!context.isUserAuthorized())
		{
			response.getWriter().print(messageSource.getMessage("request.handle.error.nouser"));
			return;
		}

		long t2 = System.currentTimeMillis();
		
		registry.withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				long t0 = System.currentTimeMillis();
				
				BpmTask task = context.getBpmSession().getTaskData(taskId);
				
				if(task == null)
					task = context.getBpmSession().getHistoryTask(taskId);

				long t1 = System.currentTimeMillis();
				
				ProcessStateConfiguration config = ctx.getProcessDefinitionDAO().getCachedProcessStateConfiguration(Long.parseLong(processStateConfigurationId));

				long t2 = System.currentTimeMillis();
				
               String processVersion = String.valueOf(config.getDefinition().getBpmDefinitionVersion());
                String processDescription  = messageSource.getMessage(config.getDefinition().getDescription());
				/* Load view widgets */
				List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(config.getWidgets());
				Collections.sort(widgets, new Comparator<ProcessStateWidget>() {

					@Override
					public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
						return widget1.getPriority().compareTo(widget2.getPriority());
					}
				});

				long t3 = System.currentTimeMillis();
				
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
				
				long t4 = System.currentTimeMillis();
				
				TaskViewBuilder taskViewBuilder = new TaskViewBuilder()
					.setWidgets(widgets)
					.setActions(actions)
                    .setDescription(processDescription)
                    .setVersion(processVersion)
					.setI18Source(messageSource)
					.setUser(context.getUser())
                    .setCtx(ctx)
					.setTask(task);
				
				long t5 = System.currentTimeMillis();

				try
				{
					taskViewBuilder.processView(response.getWriter());
				}
				catch(IOException ex)
				{
					logger.log(Level.SEVERE, "Problem during task view generation. TaskId="+taskId, ex);
				}
				
				long t6 = System.currentTimeMillis();

				logger.log(Level.INFO, "loadTask.withContext total: " + (t6-t0) + "ms, " +
						"[1]: " + (t1-t0) + "ms, " +
						"[2]: " + (t2-t1) + "ms, " +
						"[3]: " + (t3-t2) + "ms, " +
						"[4]: " + (t4-t3) + "ms, " +
						"[5]: " + (t5-t4) + "ms, " +
						"[6]: " + (t6-t5) + "ms, "
						);
				
			}
		});

		
		long t3 = System.currentTimeMillis();
		
		logger.log(Level.INFO, "loadTask total: " + (t3-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms, " +
				"[3]: " + (t3-t2) + "ms, "
				);

	}

	private static boolean isNull(String value) {
		return value == null || value.isEmpty() || "null".equals(value);
	}
}
