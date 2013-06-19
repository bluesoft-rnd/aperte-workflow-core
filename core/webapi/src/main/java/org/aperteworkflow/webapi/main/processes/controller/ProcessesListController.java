package org.aperteworkflow.webapi.main.processes.controller;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.aperteworkflow.webapi.context.IProcessToolRequestContext;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.BpmTaskBean;
import org.aperteworkflow.webapi.main.processes.DataPagingBean;
import org.aperteworkflow.webapi.main.processes.action.domain.ErrorResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.PerformActionResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.SaveResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.ValidateResultBean;
import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;
import org.aperteworkflow.webapi.main.processes.domain.NewProcessInstanceBean;
import org.aperteworkflow.webapi.main.processes.processor.TaskProcessor;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
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

    private static final String PROCESS_NAME_COLUMN = "0";
    private static final String PROCESS_CODE_COLUMN = "1";
    private static final String CREATOR_NAME_COLUMN = "2";
    private static final String ASSIGNEE_NAME_COLUMN = "3";
    private static final String CREATED_DATE_COLUMN = "4";
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

		
		final PerformActionResultBean resultBean = new PerformActionResultBean();
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
			return resultBean;
		}
		
		final String taskId = request.getParameter("taskId");
		final String actionName = request.getParameter("actionName");
		final String skipSaving = request.getParameter("skipSaving");
		
		if(taskId == null || taskId.isEmpty())
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.notaskid"));
			return resultBean;
		}
		else if(actionName == null || actionName.isEmpty())
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.actionName"));
			return resultBean;
		}
		
		
		/* Save task before action performing */
		if(!"true".equals(skipSaving))
		{
			SaveResultBean saveResult = saveAction(request);
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
					BpmTaskBean processBean = BpmTaskBean.createFrom(newTask, messageSource);
					
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
		logger.warning("SAVE!");
		final SaveResultBean resultBean = new SaveResultBean();
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized()) 
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
			return resultBean;
		}
		
		final String taskId = request.getParameter("taskId");
		final String widgetDataJson = request.getParameter("widgetData");
		final Collection<HtmlWidget> widgetData;
		
		if(taskId == null || taskId.isEmpty())
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.notaskid"));
			return resultBean;
		}
		
		try 
		{
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, HtmlWidget.class);	  
			widgetData = mapper.readValue(widgetDataJson, type);
		}
		catch (Throwable e) 
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.jsonparseerror"));
			return resultBean;
		} 
		
		
		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() 
		{

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				BpmTask task = context.getBpmSession().getTaskData(taskId, ctx);
				
				TaskProcessor taskSaveProcessor = new TaskProcessor(task, ctx, getEventBus(), context.getMessageSource(), widgetData);
				
				/* Validate widgets */
				ValidateResultBean widgetsValidationResult = taskSaveProcessor.validateWidgets();
				
				if(widgetsValidationResult.hasErrors())
				{
					/* Copy all errors from event */
					for(ErrorResultBean errorBean: widgetsValidationResult.getErrors())
						resultBean.addError(errorBean);
				}
				/* No validation errors, save widgets */
				else
				{
					SaveResultBean widgetsSaveResult = taskSaveProcessor.saveWidgets();
					
					if(widgetsSaveResult.hasErrors())
					{
						/* Copy all errors from event */
						for(ErrorResultBean errorBean: widgetsSaveResult.getErrors())
							resultBean.addError(errorBean);
					}
						
				}

				
			}
		});
		
		return resultBean;
		
		
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

    @RequestMapping(method = RequestMethod.POST, value = "/processes/searchTasks.json")
    @ResponseBody
    public DataPagingBean<BpmTaskBean> searchTasks(final HttpServletRequest request)
    {
        String echo = request.getParameter("sEcho");



        final List<BpmTaskBean> adminAlertBeanList = new ArrayList<BpmTaskBean>();

        final IProcessToolRequestContext context = this.initilizeContext(request);

        if(!context.isUserAuthorized())
            return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 0, echo);

        final String sortCol = request.getParameter("iSortCol_0");
        final String sortDir = request.getParameter("sSortDir_0");
        final String searchString = request.getParameter("sSearch");

        String displayStartString = request.getParameter("iDisplayStart");
        String displayLengthString = request.getParameter("iDisplayLength");

        final Integer displayStart = Integer.parseInt(displayStartString);
        final Integer displayLength = Integer.parseInt(displayLengthString);

        final DataPagingBean<BpmTaskBean> pagingCollection = new DataPagingBean<BpmTaskBean>(
                adminAlertBeanList, 100, echo);

        context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() {

            @Override
            public void withContext(ProcessToolContext ctx)
            {
                I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());

                ProcessInstanceFilter filter = new ProcessInstanceFilter();

                filter.setExpression(searchString);
                filter.setSortOrderCondition(mapColumnNameToOrderCondition(sortCol));
                filter.setSortOrder(mapColumnSortToSortOrder(sortDir));

                Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, ctx, displayStart, displayLength);

                for(BpmTask task: tasks)
                {
                    BpmTaskBean processBean = BpmTaskBean.createFrom(task, messageSource);

                    adminAlertBeanList.add(processBean);

                }

                int totalRecords = context.getBpmSession().getFilteredTasksCount(filter, ctx);
                pagingCollection.setiTotalRecords(totalRecords);
                pagingCollection.setiTotalDisplayRecords(totalRecords);
                pagingCollection.setAaData(adminAlertBeanList);

            }
        });




        return pagingCollection;

    }
	
	@RequestMapping(method = RequestMethod.POST, value = "/processes/loadProcessesList.json")
	@ResponseBody
	public DataPagingBean<BpmTaskBean> loadProcessesList(final HttpServletRequest request)
	{
		String echo = request.getParameter("sEcho");
		final String queueName = request.getParameter("queueName");
		final String queueType = request.getParameter("queueType");
		final String ownerLogin = request.getParameter("ownerLogin");

		
		final List<BpmTaskBean> adminAlertBeanList = new ArrayList<BpmTaskBean>();
		
		if(queueName == null || queueName.isEmpty() || queueType == null || queueType.isEmpty() || ownerLogin == null)
		{
			return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 0, echo);
		}
		

		
		final IProcessToolRequestContext context = this.initilizeContext(request);
		
		if(!context.isUserAuthorized())
			return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 0, echo);
		
		final String sortCol = request.getParameter("iSortCol_0");
		final String sortDir = request.getParameter("sSortDir_0");
		final String searchString = request.getParameter("sSearch");
		
		String displayStartString = request.getParameter("iDisplayStart");
		String displayLengthString = request.getParameter("iDisplayLength");
		
		final Integer displayStart = Integer.parseInt(displayStartString);
		final Integer displayLength = Integer.parseInt(displayLengthString);
		
		final DataPagingBean<BpmTaskBean> pagingCollection = new DataPagingBean<BpmTaskBean>(
				adminAlertBeanList, 100, echo);

		context.getRegistry().withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx)
			{
				I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());

				boolean isQueue = "queue".equals(queueType);

				UserData owner = ctx.getUserDataDAO().loadUserByLogin(ownerLogin);


				ProcessInstanceFilter filter = new ProcessInstanceFilter();
				if(isQueue)
				{
					filter.addQueue(queueName);
					filter.addQueueType(QueueType.OWN_IN_QUEUE);
				}
				else if("process".equals(queueType))
				{
			        //processFilter.setName(getMessage("activity.assigned.tasks"));
			        filter.addOwner(owner);
			        filter.setFilterOwner(owner);
			        filter.addQueueType(QueueType.fromQueueId(queueName));
					filter.setName(queueName);
				}


                filter.setExpression(searchString);
                filter.setSortOrderCondition(mapColumnNameToOrderCondition(sortCol));
                filter.setSortOrder(mapColumnSortToSortOrder(sortDir));

				Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, ctx, displayStart, displayLength);

				for(BpmTask task: tasks)
				{
					BpmTaskBean processBean = BpmTaskBean.createFrom(task, messageSource);

					if(isQueue)
						processBean.setQueueName(queueName);

					adminAlertBeanList.add(processBean);

                }

				int totalRecords = context.getBpmSession().getFilteredTasksCount(filter, ctx);
				pagingCollection.setiTotalRecords(totalRecords);
				pagingCollection.setiTotalDisplayRecords(totalRecords);
				pagingCollection.setAaData(adminAlertBeanList);

			}
		});




        return pagingCollection;

	}

    private QueueOrderCondition mapColumnNameToOrderCondition(String columnName)
    {
        if(PROCESS_NAME_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_PROCESS_NAME_ORDER;

        else if(PROCESS_CODE_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_PROCESS_CODE_ORDER;

        else if(CREATOR_NAME_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_CREATOR_ORDER;

        else if(ASSIGNEE_NAME_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_ASSIGNEE_ORDER;

        else if(CREATED_DATE_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_CREATE_DATE_ORDER;

        else
            return null;
    }

    private QueueOrder mapColumnSortToSortOrder(String sortOrder)
    {
        if("desc".equals(sortOrder))
            return QueueOrder.DESC;
        else
            return QueueOrder.ASC;
    }


	

}
