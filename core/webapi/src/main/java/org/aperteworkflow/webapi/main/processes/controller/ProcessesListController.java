package org.aperteworkflow.webapi.main.processes.controller;

import org.apache.commons.lang3.StringUtils;
import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.ActionPseudoTaskBean;
import org.aperteworkflow.webapi.main.processes.TasksListViewBeanFactoryWrapper;
import org.aperteworkflow.webapi.main.processes.action.domain.PerformActionResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.SaveResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.ValidateResultBean;
import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;
import org.aperteworkflow.webapi.main.processes.domain.KeyValueBean;
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
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory.ExecutionType;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.exceptions.BusinessException;
import pl.net.bluesoft.rnd.processtool.exceptions.ExceptionsUtils;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.plugins.GuiRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.ErrorResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import pl.net.bluesoft.rnd.processtool.web.view.*;
import pl.net.bluesoft.rnd.util.TaskUtil;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aperteworkflow.webapi.main.processes.controller.TaskViewController.getBpmSession;

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
    private static Logger performanceLogger = Logger.getLogger("PRF");

    private static final String TASKS_LIST_VIEW_NAME_PARAM = "taskListViewName";

    private static final String EMPTY_JSON = "[{}]";
    

	/**
	 * Request parameters:
	 * - processStateConfigurationId: process state configuration db id
	 * 
	 * Load all action configuration to display buttons
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/processes/performAction.json", headers = "Content-Type=application/json")
	@ResponseBody
	public PerformActionResultBean performAction(final HttpServletRequest request)
	{
		logger.finest("performAction ...");
		
		final PerformActionResultBean resultBean = new PerformActionResultBean();
        try
        {
			long t0 = System.currentTimeMillis();
			
            /* Initilize request context */
            final IProcessToolRequestContext context = this.initilizeContext(request, getProcessToolRegistry().getProcessToolSessionFactory());

            if(!context.isUserAuthorized())
            {
                resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
                return resultBean;
            }

            final String taskId = request.getParameter("taskId");
            final String actionName = request.getParameter("actionName");
            final String skipSaving = request.getParameter("skipSaving");
            final String commentNeeded = request.getParameter("commentNeeded");
            final String comment = request.getParameter("comment");
            final String changeOwner = request.getParameter("changeOwner");
            final String changeOwnerAttributeKey = request.getParameter("changeOwnerAttributeKey");
            final String changeOwnerAttributeValue = request.getParameter("changeOwnerAttributeValue");
            final String viewName = request.getParameter(TASKS_LIST_VIEW_NAME_PARAM);

            if(isNull(taskId))
            {
                resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.notaskid"));
                return resultBean;
            }
            else if(isNull(actionName))
            {
                resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.actionName"));
                return resultBean;
            }
            else if("true".equals(commentNeeded) && isNull(comment))
            {
                resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.noComment"));
                return resultBean;
            }
            else if("true".equals(changeOwner) && isNull(changeOwnerAttributeKey))
            {
                resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.noChangeOwnerAttrKey"));
                return resultBean;
            }

            /* Save task before action performing */
            if(!"true".equals(skipSaving))
            {
                GenericResultBean saveResult = saveAction(request);
                if(saveResult.hasErrors())
                {
                    resultBean.copyErrors(saveResult);
                    return resultBean;
                }
            }

			long t1 = System.currentTimeMillis();

            TasksListViewBean bpmTaskBean = getProcessToolRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<TasksListViewBean>() {
                @Override
                public TasksListViewBean processWithContext(ProcessToolContext ctx) {
                    try {
                        logger.log(Level.FINEST, "performAction.withContext ... ");

                        long t0 = System.currentTimeMillis();
                        long t1 = System.currentTimeMillis();
                        long t2 = System.currentTimeMillis();

                        BpmTask task = context.getBpmSession().getTaskData(taskId);

                        if("true".equals(changeOwner))
                        {
                             task.getProcessInstance().setSimpleAttribute(changeOwnerAttributeKey, changeOwnerAttributeValue);
                        }

                        if(StringUtils.isNotEmpty(comment))
                            TaskUtil.saveComment(task, context.getUser(), getUserSource(), comment);

                        List<BpmTask> newTasks = getBpmSession(context, task.getAssignee()).performAction(actionName, task, false);

                        long t3 = System.currentTimeMillis();


                        
                        /* Task finished or no tasks created (ie waiting for timer) */
                        if (newTasks == null || newTasks.isEmpty()) {
							String actionPseudoStateName = ActionPseudoTaskBean.getActionPseudoStateName(task.getTaskName(), actionName);
							ProcessStateConfiguration actionPseudoState = task.getProcessDefinition().getProcessStateConfigurationByName(actionPseudoStateName);

							if (actionPseudoState != null) {
								I18NSource messageSource = context.getMessageSource();

								return ActionPseudoTaskBean.createTask(task, actionPseudoState, actionName, viewName, messageSource);
							}
							return null;
                        }

                        I18NSource messageSource = context.getMessageSource();
                        TasksListViewBean processBean= new TasksListViewBeanFactoryWrapper().createFrom(newTasks.get(0), messageSource, viewName);

                        long t4 = System.currentTimeMillis();

                        logger.log(Level.FINEST, "performAction.withContext total: " + (t4 - t0) + "ms, " +
                                "[1]: " + (t1 - t0) + "ms, " +
                                "[2]: " + (t2 - t1) + "ms, " +
                                "[3]: " + (t3 - t2) + "ms, " +
                                "[4]: " + (t4 - t3) + "ms "
                        );

                        return processBean;
                    }
                    catch (Throwable ex) {
                        if(ExceptionsUtils.isExceptionOfClassExistis(ex, BusinessException.class))
                        {
                            BusinessException businessException = ExceptionsUtils.getExceptionByClassFromStack(ex, BusinessException.class);
                            logger.log(Level.WARNING, "Business error", businessException);
                            resultBean.addError(SYSTEM_SOURCE, businessException.getMessage());
                        }
                        else {
                            logger.log(Level.SEVERE, "Error during performing BPM action", ex);
                            resultBean.addError(SYSTEM_SOURCE, ex.getMessage());
                        }

                        return null;
                    }
                }

	            private void saveComment(BpmTask task) {
		            if(comment != null && !comment.isEmpty())
		            {
		                UserData actionPerformer = context.getUser();
		                String taskOwner = task.getAssignee();

		                String authorLogin = actionPerformer.getLogin();
		                String authorFullName = actionPerformer.getRealName();

		                ProcessComment processComment = new ProcessComment();
		                processComment.setCreateTime(new Date());
		                processComment.setProcessState(task.getTaskName());
		                processComment.setBody(comment);
		                processComment.setAuthorLogin(authorLogin);
		                processComment.setAuthorFullName(authorFullName);

		                /* Action performed by task owner*/
		                if(taskOwner.equals(authorLogin))
		                {
		                    processComment.setAuthorLogin(authorLogin);
		                    processComment.setAuthorFullName(authorFullName);
		                }
		                /* Action performed by substituting user */
		                else
		                {
		                    UserData owner = getUserSource().getUserByLogin(taskOwner);
		                    processComment.setAuthorLogin(owner.getLogin());
		                    processComment.setAuthorFullName(owner.getRealName());
		                    processComment.setSubstituteLogin(authorLogin);
		                    processComment.setSubstituteFullName(authorFullName);

		                }

		                ProcessInstance pi = task.getProcessInstance().getRootProcessInstance();

		                pi.addComment(processComment);
		                pi.setSimpleAttribute("commentAdded", "true");
		            }
	            }
            }, ExecutionType.TRANSACTION);
		
		    resultBean.setNextTask(bpmTaskBean);

		    long t2 = System.currentTimeMillis();

            performanceLogger.log(Level.FINEST, "performAction total: " + (t2-t0) + "ms, " +
					"[1]: " + (t1-t0) + "ms, " +
					"[2]: " + (t2-t1) + "ms " 
					);
            
        }
        catch(Throwable e)
        {
            logger.log(Level.WARNING, "Business error", e);
        }

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
	@RequestMapping(method = RequestMethod.POST, value = "/processes/saveAction.json")
	@ResponseBody
	public GenericResultBean saveAction(final HttpServletRequest request)
	{
		logger.finest("saveAction ...");
		long t0 = System.currentTimeMillis();

		final GenericResultBean resultBean = new GenericResultBean();
		
		/* Initilize request context */
		final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());
		
		if(!context.isUserAuthorized()) 
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
			return resultBean;
		}
		
		final String taskId = request.getParameter("taskId");
		final String widgetDataJson = request.getParameter("widgetData");
		final Collection<HtmlWidget> widgetData;
		
		if(isNull(taskId))
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
		
		long t1 = System.currentTimeMillis();

        try
        {
                getProcessToolRegistry().withProcessToolContext(new ProcessToolContextCallback() {

                @Override
                public void withContext(ProcessToolContext ctx)
                {
                    try
                    {
                    long t0 = System.currentTimeMillis();

                    BpmTask task = context.getBpmSession().getTaskData(taskId);

                    long t1 = System.currentTimeMillis();

                    TaskProcessor taskSaveProcessor = new TaskProcessor(task, context.getMessageSource(), widgetData);

                    long t2 = System.currentTimeMillis();

                        /* Validate widgets */
                    ValidateResultBean widgetsValidationResult = taskSaveProcessor.validateWidgets();

                    long t3 = System.currentTimeMillis();

                    if (widgetsValidationResult.hasErrors()) {
                            /* Copy all errors from event */
                        for (ErrorResultBean errorBean : widgetsValidationResult.getErrors())
                            resultBean.addError(errorBean);
                    }
                    /* No validation errors, save widgets */
                    else {
                        SaveResultBean widgetsSaveResult = taskSaveProcessor.saveWidgets();

                        if (widgetsSaveResult.hasErrors()) {
                                /* Copy all errors from event */
                            for (ErrorResultBean errorBean : widgetsSaveResult.getErrors())
                                resultBean.addError(errorBean);
                        } else {
                            // rebuild the task view
                            final String view = TaskViewController.buildTaskView(getProcessToolRegistry(), context, taskId);
                            if (!isNull(view))
                                resultBean.setData(view);
                        }

                    }

                    long t4 = System.currentTimeMillis();

                        performanceLogger.log(Level.FINEST, "saveAction.withContext total: " + (t4 - t0) + "ms, " +
                            "[1]: " + (t1 - t0) + "ms, " +
                            "[2]: " + (t2 - t1) + "ms, " +
                            "[3]: " + (t3 - t2) + "ms, " +
                            "[4]: " + (t4 - t3) + "ms "
                    );
                    }
                    catch(Throwable e)
                    {
                        logger.log(Level.SEVERE, "Problem during data saving", e);
                        resultBean.addError(SYSTEM_SOURCE, e.getLocalizedMessage());
                    }
                }
            }, ExecutionType.TRANSACTION);

        }
        catch(BusinessException e)
        {
            logger.log(Level.WARNING, "Business error", e);
            resultBean.addError(SYSTEM_SOURCE, e.getMessage());
        }
        catch(Throwable e)
        {
            if(ExceptionsUtils.isExceptionOfClassExistis(e, BusinessException.class))
            {
                BusinessException businessException = ExceptionsUtils.getExceptionByClassFromStack(e, BusinessException.class);
                logger.log(Level.WARNING, "Business error", businessException);
                resultBean.addError(SYSTEM_SOURCE, businessException.getMessage());
            }
            else {
                logger.log(Level.SEVERE, "Problem during data saving", e);
                resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage(
                        "request.handle.error.saveerror",
                        e.getLocalizedMessage()));
            }
        }


		
		long t2 = System.currentTimeMillis();

        performanceLogger.log(Level.FINEST, "saveAction total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms " 
				);
		
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
		logger.finest("startNewProcess ...");
		final NewProcessInstanceBean newProcessInstanceBO = new NewProcessInstanceBean();

        try
        {
    		long t0 = System.currentTimeMillis();
    		
            final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());

            if(!context.isUserAuthorized())
            {
                newProcessInstanceBO.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
                return newProcessInstanceBO;
            }

            final Map<String, String> simpleAttributes = new HashMap<String, String> ();

            final String bpmDefinitionId = request.getParameter("bpmDefinitionId");
            final String processSimpleAttributes = request.getParameter("processSimpleAttributes");

            if(bpmDefinitionId == null)
            {
                newProcessInstanceBO.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.performaction.error.notaskid"));
                return newProcessInstanceBO;
            }

            if(processSimpleAttributes != null && !EMPTY_JSON.equals(processSimpleAttributes))
            {
                ObjectMapper mapper = new ObjectMapper();
                JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, KeyValueBean.class);
                List<KeyValueBean> mappedAttributes = mapper.readValue(processSimpleAttributes, type);
                for(KeyValueBean keyValueBean: mappedAttributes)
                    simpleAttributes.put(keyValueBean.getKey(), keyValueBean.getValue());
            }

    		long t1 = System.currentTimeMillis();

            getProcessToolRegistry().withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    long t0 = System.currentTimeMillis();

                    StartProcessResult result = context.getBpmSession().startProcess(bpmDefinitionId, null, "portlet");
                    ProcessInstance instance = result.getProcessInstance();

                    long t1 = System.currentTimeMillis();
                    for (String key : simpleAttributes.keySet()) {
                        if (key.equals(ProcessInstance.EXTERNAL_KEY_PROPERTY))
                            instance.setExternalKey(simpleAttributes.get(key));
                        else
                            instance.setSimpleAttribute(key, simpleAttributes.get(key));
                    }

                    long t2 = System.currentTimeMillis();
                    List<BpmTask> tasks = result.getTasksAssignedToCreator();

                    if (!tasks.isEmpty()) {
                        BpmTask task = tasks.get(0);

                        newProcessInstanceBO.setTaskId(task.getInternalTaskId());
                        newProcessInstanceBO.setProcessStateConfigurationId(task.getCurrentProcessStateConfiguration().getId().toString());
                    }

                    long t3 = System.currentTimeMillis();

                    performanceLogger.log(Level.FINEST, "startNewProcess.withContext total: " + (t3 - t0) + "ms, " +
                            "[1]: " + (t1 - t0) + "ms, " +
                            "[2]: " + (t2 - t1) + "ms, " +
                            "[3]: " + (t3 - t2) + "ms, "
                    );

                }
            }, ExecutionType.TRANSACTION);
            
    		long t2 = System.currentTimeMillis();

            performanceLogger.log(Level.FINEST, "startNewProcess total: " + (t2-t0) + "ms, " +
    				"[1]: " + (t1-t0) + "ms, " +
    				"[2]: " + (t2-t1) + "ms "
    				);
    		
            return newProcessInstanceBO;
        }
        catch (Throwable e)
        {
            logger.log(Level.SEVERE, "Error during process starting", e);

            newProcessInstanceBO.addError(SYSTEM_SOURCE, e.getMessage());
            return newProcessInstanceBO;
        }
	}

    @RequestMapping(method = RequestMethod.POST, value = "/processes/searchTasks.json")
    @ResponseBody
    public DataPagingBean<TasksListViewBean> searchTasks(final HttpServletRequest request)
    {
		logger.finest("searchTasks ...");
		long t0 = System.currentTimeMillis();

    	final JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(request.getParameterMap());

        final List<TasksListViewBean> adminAlertBeanList = new ArrayList<TasksListViewBean>();

        final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());

        if(!context.isUserAuthorized())
            return new DataPagingBean<TasksListViewBean>(adminAlertBeanList, 0, dataTable.getDraw());

        final String sortCol = request.getParameter("iSortCol_0");
        final String sortDir = request.getParameter("sSortDir_0");
        final String searchString = request.getParameter("_AperteWorkflowActivitiesPortlet_WAR_aperteworkflow_sSearch");
        final String searchProcessKey = request.getParameter("processKey");
        String displayStartString = request.getParameter("iDisplayStart");
        String displayLengthString = request.getParameter("iDisplayLength");
        final String viewName = request.getParameter(TASKS_LIST_VIEW_NAME_PARAM);

        final Integer displayStart = Integer.parseInt(displayStartString);
        final Integer displayLength = Integer.parseInt(displayLengthString);

        final DataPagingBean<TasksListViewBean> pagingCollection = new DataPagingBean<TasksListViewBean>(
                adminAlertBeanList, 100, dataTable.getDraw());

		long t1 = System.currentTimeMillis();

        getProcessToolRegistry().withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                long t0 = System.currentTimeMillis();

                I18NSource messageSource = context.getMessageSource();

                ProcessInstanceFilter filter = new ProcessInstanceFilter();

                filter.setUsePrivileges(true);

                if (searchString != null && !searchString.isEmpty()) {
                    filter.setExpression(searchString);
                    filter.setLocale(messageSource.getLocale());
                }

                if (searchProcessKey != null) {
                    filter.setProcessBpmKey(searchProcessKey);
                }

                List<JQueryDataTableColumn> sortingColumns = dataTable.getSortingColumnOrder();

                for(JQueryDataTableColumn sortingColumn: sortingColumns)
                {
                    ProcessInstanceFilterSortingColumn processInstanceFilterSortingColumn = new ProcessInstanceFilterSortingColumn();
                    processInstanceFilterSortingColumn.setColumnName(sortingColumn.getPropertyName());
                    processInstanceFilterSortingColumn.setPriority(sortingColumn.getPriority());
                    processInstanceFilterSortingColumn.setOrder(sortingColumn.getSortedAsc() ? QueueOrder.ASC : QueueOrder.DESC);
                    filter.addSortingColumnOrder(processInstanceFilterSortingColumn);
                }
                
                filter.setFilterOwnerLogin(context.getUser().getLogin());

                filter.setViewName(viewName);


                long t1 = System.currentTimeMillis();

                Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, displayStart, displayLength);

                for (BpmTask task : tasks) {
                    TasksListViewBean taskViewBean = new TasksListViewBeanFactoryWrapper().createFrom(task, messageSource, viewName);

                    adminAlertBeanList.add(taskViewBean);
                }

                long t2 = System.currentTimeMillis();

                int totalRecords = context.getBpmSession().getFilteredTasksCount(filter);
                pagingCollection.setRecordsTotal(totalRecords);
                pagingCollection.setRecordsFiltered(totalRecords);
                pagingCollection.setData(adminAlertBeanList);

                long t3 = System.currentTimeMillis();

                performanceLogger.log(Level.FINEST, "searchTasks.withContext total: " + (t3 - t0) + "ms, " +
                        "[1]: " + (t1 - t0) + "ms, " +
                        "[2]: " + (t2 - t1) + "ms " +
                        "[3]: " + (t3 - t2) + "ms "
                );

            }
        }, ExecutionType.NO_TRANSACTION);

		long t2 = System.currentTimeMillis();

        performanceLogger.log(Level.FINEST, "searchTasks total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms "
				);
		
        return pagingCollection;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/processes/loadQueue")
    @ResponseBody
    public void loadQueue(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
    {
        logger.finest("loadQueue ...");
        long t0 = System.currentTimeMillis();

        /* Get process state configuration db id */
        final String queueId = request.getParameter("queueId");
        final String ownerLogin = request.getParameter("ownerLogin");

        /* Initilize request context */
        final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());

        if(isNull(queueId))
        {
            response.getWriter().print(context.getMessageSource().getMessage("request.performaction.error.noqueueid"));
            return;
        }

        long t1 = System.currentTimeMillis();

        if(!context.isUserAuthorized())
        {
            response.getWriter().print(context.getMessageSource().getMessage("request.handle.error.nouser"));
            return;
        }

        long t2 = System.currentTimeMillis();

        final String output = buildTaskListView(getProcessToolRegistry(), context, queueId, ownerLogin);

        /* Write to output writer here, so there will be no invalid output
        for error in previous code with session
         */
        response.getWriter().print(output);

        long t3 = System.currentTimeMillis();

        performanceLogger.log(Level.FINEST, "loadTask total: " + (t3-t0) + "ms, " +
                        "[1]: " + (t1-t0) + "ms, " +
                        "[2]: " + (t2-t1) + "ms, " +
                        "[3]: " + (t3-t2) + "ms, "
        );
    }

    public static String buildTaskListView(final ProcessToolRegistry processToolRegistry, final IProcessToolRequestContext context, final String queueId, final String ownerLogin) {
        final StringBuilder builder = new StringBuilder();

        processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                long t0 = System.currentTimeMillis();

                // reset string buffer
                builder.setLength(0);

                long t1 = System.currentTimeMillis();

                TaskListBuilder taskListBuilder = new TaskListBuilder()
                        .setQueueId(queueId)
                        .setUser(context.getUser())
                        .setOwnerLogin(ownerLogin)
                        .setI18Source(context.getMessageSource());


                long t5 = System.currentTimeMillis();

                try {
                    builder.append(taskListBuilder.build());

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Problem during queue list view generation. queueId=" + queueId, ex);
                }
                long t6 = System.currentTimeMillis();

                performanceLogger.log(Level.FINEST, "loadTask.withContext total: " + (t6-t0) + "ms, " +
                                "[1]: " + (t1-t0) + "ms, " +
                                "[6]: " + (t6-t5) + "ms, "
                );
            }
        }, ExecutionType.NO_TRANSACTION);
        return builder.toString();
    }
	
	@RequestMapping(method = RequestMethod.POST, value = "/processes/loadProcessesList.json")
	@ResponseBody
	public DataPagingBean<TasksListViewBean> loadProcessesList(final HttpServletRequest request)
	{
		logger.finest("loadProcessesList ...");
		long t0 = System.currentTimeMillis();

        final JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(request.getParameterMap());

		final String queueName = request.getParameter("queueName");
		final String queueType = request.getParameter("queueType");
		final String ownerLogin = request.getParameter("ownerLogin");
        final String viewName = request.getParameter(TASKS_LIST_VIEW_NAME_PARAM);

		final List<TasksListViewBean> adminAlertBeanList = new ArrayList<TasksListViewBean>();

		if(isNull(queueType) || isNull(ownerLogin))
		{
			return new DataPagingBean<TasksListViewBean>(adminAlertBeanList, 0, dataTable.getDraw());
		}

        Locale locale = request.getLocale();

		final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());

		if(!context.isUserAuthorized())
			return new DataPagingBean<TasksListViewBean>(adminAlertBeanList, 0, dataTable.getDraw());

		final String searchString = request.getParameter("search[value]");


		final DataPagingBean<TasksListViewBean> pagingCollection = new DataPagingBean<TasksListViewBean>(
				adminAlertBeanList, 100, dataTable.getDraw());

		long t1 = System.currentTimeMillis();

        AbstractTaskListView listView = getProcessToolRegistry().getGuiRegistry().getTasksListView(viewName);
        if(listView == null) {
            listView = getProcessToolRegistry().getGuiRegistry().getTasksListView(GuiRegistry.STANDARD_PROCESS_QUEUE_ID);
        }

        final AbstractTaskListView finalListView = listView;

        getProcessToolRegistry().withProcessToolContext(new ProcessToolContextCallback() {

            @Override
            public void withContext(ProcessToolContext ctx) {
                long t0 = System.currentTimeMillis();

                I18NSource messageSource = context.getMessageSource();

                UserData owner = getUserSource().getUserByLogin(ownerLogin);

                Map<String, Object> listViewParameters = new HashMap<String, Object>();
                listViewParameters.put(AbstractTaskListView.PARAMETER_USER_LOGIN, ownerLogin);
                listViewParameters.put(AbstractTaskListView.PARAMETER_USER, owner);
                listViewParameters.put(AbstractTaskListView.PARAMETER_QUEUE_ID, viewName);

                ProcessInstanceFilter filter = finalListView.getProcessInstanceFilter(listViewParameters);

                filter.setExpression(searchString);
                filter.setLocale(messageSource.getLocale());

                List<JQueryDataTableColumn> sortingColumns = dataTable.getSortingColumnOrder();

                for(JQueryDataTableColumn sortingColumn: sortingColumns)
                {
                    ProcessInstanceFilterSortingColumn processInstanceFilterSortingColumn = new ProcessInstanceFilterSortingColumn();
                    processInstanceFilterSortingColumn.setColumnName(sortingColumn.getPropertyName());
                    processInstanceFilterSortingColumn.setPriority(sortingColumn.getPriority());
                    processInstanceFilterSortingColumn.setOrder(sortingColumn.getSortedAsc() ? QueueOrder.ASC : QueueOrder.DESC);
                    filter.addSortingColumnOrder(processInstanceFilterSortingColumn);

                }

                long t1 = System.currentTimeMillis();

                Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, dataTable.getPageOffset(), dataTable.getPageLength());

                long t2 = System.currentTimeMillis();

                for (BpmTask task : tasks) {
                    TasksListViewBean taskViewBean = new TasksListViewBeanFactoryWrapper().createFrom(task, messageSource, finalListView.getQueueId());

                    if(hasUserRightsToTask(context, task))
                        taskViewBean.setUserCanClaim(true);

                    adminAlertBeanList.add(taskViewBean);
                }


                long t3 = System.currentTimeMillis();
                int totalRecords = context.getBpmSession().getFilteredTasksCount(filter);
                pagingCollection.setRecordsTotal(totalRecords);
                pagingCollection.setRecordsFiltered(totalRecords);
                pagingCollection.setListData(adminAlertBeanList);

                long t4 = System.currentTimeMillis();

                performanceLogger.log(Level.FINEST, "loadProcessesList.withContext total: " + (t4 - t0) + "ms, " +
                        "[1]: " + (t1 - t0) + "ms, " +
                        "[2]: " + (t2 - t1) + "ms, " +
                        "[" +
                                "3]: " + (t3 - t2) + "ms, " +
                        "[4]: " + (t4 - t3) + "ms "
                );

            }
        }, ExecutionType.NO_TRANSACTION);

		long t2 = System.currentTimeMillis();

        performanceLogger.log(Level.FINEST, "loadProcessesList total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms "
				);

        return pagingCollection;
	}

    private boolean hasUserRightsToTask(IProcessToolRequestContext context, BpmTask task)
    {
        if(task.getPotentialOwners().contains(context.getUser().getLogin()))
            return true;

        for(String queueName:  context.getUserQueues())
            if(task.getQueues().contains(queueName))
                return true;

        return false;
    }

	private static boolean isNull(String value) {
		return value == null || value.isEmpty() || "null".equals(value);
	}


}
