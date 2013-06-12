package org.aperteworkflow.webapi.main.processes.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.aperteworkflow.webapi.context.IProcessToolRequestContext;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.BpmTaskBean;
import org.aperteworkflow.webapi.main.processes.DataPagingBean;
import org.aperteworkflow.webapi.main.processes.action.domain.ActionBean;
import org.aperteworkflow.webapi.main.processes.action.domain.PerformActionResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.SaveResultBean;
import org.aperteworkflow.webapi.main.processes.domain.HtmlWidgetData;
import org.aperteworkflow.webapi.main.processes.domain.NewProcessInstanceBean;
import org.aperteworkflow.webapi.main.processes.widget.domain.WidgetBean;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.event.SaveTaskEvent;
import pl.net.bluesoft.rnd.processtool.event.beans.ErrorBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetAttribute;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

/**
 * Aperte process main web controller based on Spring MVC
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
@Controller
public class ProcessesListController extends AbstractProcessToolServletController
{
	private static Logger logger = Logger.getLogger(ProcessesListController.class.getName());
	private static final String SYSTEM_SOURCE = "System";
	
	/**
	 * Request parameters:
	 * - processStateConfigurationId: process state configuration db id
	 * 
	 * Load all widgets configuration to display them
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/processes/loadProcessWidgets.json")
	@ResponseBody
	public Collection<WidgetBean> loadProcessWidgets(final HttpServletRequest request)
	{
		/* Get process state configuration db id */
		final String processStateConfigurationId = request.getParameter("processStateConfigurationId");
		
		if(processStateConfigurationId == null)
			return null;
		
		final List<WidgetBean> processStateWidgets = new ArrayList<WidgetBean>();
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
			return processStateWidgets;
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				
				ProcessStateConfiguration config = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(Long.parseLong(processStateConfigurationId));

				List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(config.getWidgets());
				Collections.sort(widgets, new Comparator<ProcessStateWidget>() {

					@Override
					public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
						// TODO Auto-generated method stub
						return widget1.getPriority().compareTo(widget2.getPriority());
					}
				});
				
				I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());

				for(ProcessStateWidget widget: widgets)
				{
					WidgetBean parentWidget = processWidgetConfiguration(widget, messageSource);
					processStateWidgets.add(parentWidget);
				}
				
			}
		});
		
		return processStateWidgets;
	}
	
	/**
	 * Request parameters:
	 * - processStateConfigurationId: process state configuration db id
	 * 
	 * Load all action configuration to display buttons
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/processes/loadProcessActions.json")
	@ResponseBody
	public Collection<ActionBean> loadProcessActions(final HttpServletRequest request)
	{
		/* Get process state configuration db id */
		final String processStateConfigurationId = request.getParameter("processStateConfigurationId");
		
		if(processStateConfigurationId == null)
			return null;
		
		final List<ActionBean> processStateWidgets = new ArrayList<ActionBean>();
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
			return processStateWidgets;
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				
				ProcessStateConfiguration config = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(Long.parseLong(processStateConfigurationId));

				List<ProcessStateAction> actions = new ArrayList<ProcessStateAction>(config.getActions());
				Collections.sort(actions, new Comparator<ProcessStateAction>() {

					@Override
					public int compare(ProcessStateAction action1, ProcessStateAction action2) {
						// TODO Auto-generated method stub
						return action1.getPriority().compareTo(action2.getPriority());
					}
				});
				
				I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());

				for(ProcessStateAction action: actions)
				{
					ActionBean actionBean = new ActionBean();
					actionBean.setActionName(action.getBpmName());
					actionBean.setCaption(messageSource.getMessage(action.getLabel()));
					actionBean.setTooltip(messageSource.getMessage(action.getDescription()));
					actionBean.setSkipSaving(action.getSkipSaving().toString());
					processStateWidgets.add(actionBean);
				}
				
			}
		});
		
		return processStateWidgets;
	}
	
	
	/**
	 * Request parameters:
	 * - processStateConfigurationId: process state configuration db id
	 * 
	 * Load all action configuration to display buttons
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/processes/performAction.json")
	@ResponseBody
	public PerformActionResultBean performAction(final HttpServletRequest request)
	{
		I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
		
		final PerformActionResultBean resultBean = new PerformActionResultBean();
		
		final String taskId = request.getParameter("taskId");
		final String actionName = request.getParameter("actionName");
		final String skipSaving = request.getParameter("skipSaving");
		final String widgetData = request.getParameter("widgetData");
		
		if(taskId == null || taskId.isEmpty())
		{
			resultBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.performaction.error.notaskid"));
			return resultBean;
		}
		else if(actionName == null || actionName.isEmpty())
		{
			resultBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.performaction.error.actionName"));
			return resultBean;
		}
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
		{
			resultBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.handle.error.nouser"));
			return resultBean;
		}
		
		/* Save task before action performing */
		if(!"true".equals(skipSaving))
		{
			SaveResultBean saveResult = saveTask(taskId);
			if(saveResult.hasErrors())
			{
				resultBean.copyErrors(saveResult);
				return resultBean;
			}
		}
		
		
		BpmTaskBean bpmTaskBean = context.getRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {

			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) 
			{
				try
				{
					BpmTask currentTask = context.getBpmSession().getTaskData(taskId, ctx);
					ProcessStateAction actionToPerform = currentTask.getCurrentProcessStateConfiguration().getProcessStateActionByName(actionName);
					
					BpmTask newTask = context.getBpmSession().performAction(actionToPerform, currentTask, ctx);
					
					/* Process fished, return null task */
					if(newTask.isFinished())
						return null;
					
					I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
					BpmTaskBean processBean = createFrom(newTask, messageSource);
					
					return processBean;
				}
				catch(Throwable ex)
				{
					logger.log(Level.SEVERE, ex.getMessage(), ex);
					resultBean.addError(taskId, ex.getMessage());
					return null;
				}
			}
		});
		
		resultBean.setNextTask(bpmTaskBean);


	
		return resultBean;
	}
	
	/**
	 * Request parameters:
	 * - processStateConfigurationId: process state configuration db id
	 * 
	 * Load all action configuration to display buttons
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/processes/saveAction.json")
	@ResponseBody
	public SaveResultBean saveAction(final HttpServletRequest request)
	{
		
		I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
		
		SaveResultBean resultBean = new SaveResultBean();
		
		final String taskId = request.getParameter("taskId");
		final String widgetDataJson = request.getParameter("widgetData");
		Collection<HtmlWidgetData> widgetData = null;
		
		if(taskId == null || taskId.isEmpty())
		{
			resultBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.performaction.error.notaskid"));
			return resultBean;
		}
		
		try 
		{
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, HtmlWidgetData.class);	  
			widgetData = mapper.readValue(widgetDataJson, type);
		}
		catch (Throwable e) 
		{
			resultBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.handle.error.jsonparseerror"));
			return resultBean;
		} 
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
		{
			resultBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.handle.error.nouser"));
			return resultBean;
		}
		
		resultBean = saveTask(taskId);
		
		return resultBean;
		
		
	}
	
	/** Send event to all vaadin widgets to perform save task. Widgets are 
	 * registered for this event and filtration is done by taskId
	 * 
	 * @param taskId of task to being saved
	 * @return
	 */
	private SaveResultBean saveTask(String taskId)
	{
		SaveTaskEvent saveEvent = new SaveTaskEvent(taskId);
		
		getEventBus().post(saveEvent);
		
		SaveResultBean saveResult = new SaveResultBean();
		
		/* Copy all errors from event */
		for(ErrorBean errorBean: saveEvent.getErrors())
			saveResult.addError(errorBean);
		
		return saveResult;
	}
	
	/**
	 * Request parameters:
	 * - bpmDefinitionId: process definition config bpm id
	 * 
	 * Start new process with given bpm definition id. 
	 * 
	 * @param request
	 * @return new process instance task id and process state configuration id to display its widgets
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/processes/startNewProcess.json")
	@ResponseBody
	public NewProcessInstanceBean startNewProcess(final HttpServletRequest request)
	{
		final String bpmDefinitionId = request.getParameter("bpmDefinitionId");
		
		if(bpmDefinitionId == null)
			return null;
		
		final NewProcessInstanceBean newProcessInstanceBO = new NewProcessInstanceBean();
		
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
			return newProcessInstanceBO;
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessDefinitionConfig cfg = ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(bpmDefinitionId);
				
				
				ProcessInstance instance = context.getBpmSession().createProcessInstance(cfg, null, ctx, null, null, "portlet", null);
				List<BpmTask> tasks = context.getBpmSession().findUserTasks(instance, ctx);
				if (!tasks.isEmpty()) 
				{
					BpmTask task = tasks.get(0);
					
					newProcessInstanceBO.setTaskId(task.getInternalTaskId());
					newProcessInstanceBO.setProcessStateConfigurationId(task.getCurrentProcessStateConfiguration().getId().toString());

				}	
			}
		});
		
		return newProcessInstanceBO;
		
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/processes/loadProcessesList.json")
	@ResponseBody
	public DataPagingBean<BpmTaskBean> loadProcessesList(final HttpServletRequest request)
	{
		String echo = request.getParameter("sEcho");
		final String queueName = request.getParameter("queueName");
		final String queueType = request.getParameter("queueType");
		
		final List<BpmTaskBean> adminAlertBeanList = new ArrayList<BpmTaskBean>();

		
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
			return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 100, echo);
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
 
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
				
				
				ProcessInstanceFilter filter = new ProcessInstanceFilter();
				if("queue".equals(queueType))
				{
					filter.addQueue(queueName);
					filter.addQueueType(QueueType.OWN_IN_QUEUE);
				}
				else if("process".equals(queueType))
				{
			        //processFilter.setName(getMessage("activity.assigned.tasks"));
			        filter.addOwner(context.getUser());
			        filter.setFilterOwner(context.getUser());
			        filter.addQueueType(QueueType.fromQueueId(queueName));
					filter.setName(queueName);
				}
				
				Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, ctx);
				 
				for(BpmTask task: tasks)
				{ 
					BpmTaskBean processBean = createFrom(task, messageSource);
					adminAlertBeanList.add(processBean);				

				}
				
			}
		});
		
		
		DataPagingBean<BpmTaskBean> pagingCollection = new DataPagingBean<BpmTaskBean>(
				adminAlertBeanList, 100, echo);
		
        return pagingCollection;

	}
	
	private BpmTaskBean createFrom(BpmTask task, I18NSource messageSource)
	{
		
		BpmTaskBean processBean = new BpmTaskBean();
		processBean.setProcessName(messageSource.getMessage(task.getProcessDefinition().getDescription()));
		processBean.setName(task.getTaskName());
		processBean.setCode(task.getExecutionId());
		processBean.setCreationDate(task.getCreateDate());
		processBean.setAssignee(task.getAssignee());
		processBean.setCreator(task.getCreator());
		processBean.setTaskId(task.getInternalTaskId());
		processBean.setInternalProcessId(task.getProcessInstance().getInternalId());
		processBean.setProcessStateConfigurationId(task.getCurrentProcessStateConfiguration().getId().toString());
		processBean.setDeadline(task.getDeadlineDate());
		processBean.setTooltip(messageSource.getMessage(task.getProcessDefinition().getComment()));
		
		return processBean;
	}
	
	private WidgetBean processWidgetConfiguration(ProcessStateWidget widgetConfiguration, I18NSource messageSource)
	{
		WidgetBean parentWidget = new WidgetBean();
		parentWidget.setName(widgetConfiguration.getName());
		parentWidget.setClassName(widgetConfiguration.getClassName());
		parentWidget.setId(widgetConfiguration.getId().toString());
		
		/* Set caption from attributes */
		ProcessStateWidgetAttribute attribute = widgetConfiguration.getAttributeByName("caption");
		if(attribute != null)
			parentWidget.setCaption(messageSource.getMessage(attribute.getValue()));
		
		/* Sort widgets by prority */
		List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(widgetConfiguration.getChildren());
		Collections.sort(widgets, new Comparator<ProcessStateWidget>() {

			@Override
			public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
				// TODO Auto-generated method stub
				return widget1.getPriority().compareTo(widget2.getPriority());
			}
		});
		
		for(ProcessStateWidget childWidget: widgets)
		{
			WidgetBean childWidgetBean = processWidgetConfiguration(childWidget, messageSource);
			parentWidget.getChildren().add(childWidgetBean);
		}
		
		return parentWidget;
	}
}
