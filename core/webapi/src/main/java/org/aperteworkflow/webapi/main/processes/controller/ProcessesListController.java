package org.aperteworkflow.webapi.main.processes.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.BpmTaskBean;
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
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.ErrorResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.servlet.http.HttpServletRequest;
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

    private static final String PROCESS_NAME_COLUMN = "name";
    private static final String PROCESS_CODE_COLUMN = "code";
    private static final String PROCESS_STEP_COLUMN = "step";
    private static final String PROCESS_BUSINESS_STATUS_COLUMN = "businessStatus";
    private static final String CREATOR_NAME_COLUMN = "creator";
    private static final String ASSIGNEE_NAME_COLUMN = "assignee";
    private static final String CREATED_DATE_COLUMN = "creationDate";

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
	@RequestMapping(method = RequestMethod.GET, value = "/processes/performAction.json")
	@ResponseBody
	public PerformActionResultBean performAction(final HttpServletRequest request)
	{
		logger.info("performAction ...");
		
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
                SaveResultBean saveResult = saveAction(request);
                if(saveResult.hasErrors())
                {
                    resultBean.copyErrors(saveResult);
                    return resultBean;
                }
            }

			long t1 = System.currentTimeMillis();

            BpmTaskBean bpmTaskBean = getProcessToolRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
                @Override
                public BpmTaskBean processWithContext(ProcessToolContext ctx) {
                    try {
                        logger.log(Level.INFO, "performAction.withContext ... ");

                        long t0 = System.currentTimeMillis();
                        long t1 = System.currentTimeMillis();
                        long t2 = System.currentTimeMillis();

                        BpmTask task = context.getBpmSession().getTaskData(taskId);

                        if("true".equals(changeOwner))
                        {
                             task.getProcessInstance().setSimpleAttribute(changeOwnerAttributeKey, changeOwnerAttributeValue);
                        }

                        List<BpmTask> newTasks = getBpmSession(context, task.getAssignee()).performAction(actionName, task, false);

                        long t3 = System.currentTimeMillis();

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
                        
                        /* Task finished or no tasks created (ie waiting for timer) */
                        if (newTasks == null || newTasks.isEmpty()) {
                            return null;
                        }

                        I18NSource messageSource = context.getMessageSource();
                        BpmTaskBean processBean = BpmTaskBean.createFrom(newTasks.get(0), messageSource);

                        long t4 = System.currentTimeMillis();

                        logger.log(Level.INFO, "performAction.withContext total: " + (t4 - t0) + "ms, " +
                                "[1]: " + (t1 - t0) + "ms, " +
                                "[2]: " + (t2 - t1) + "ms, " +
                                "[3]: " + (t3 - t2) + "ms, " +
                                "[4]: " + (t4 - t3) + "ms "
                        );

                        return processBean;
                    } catch (Throwable ex) {
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                        resultBean.addError(taskId, ex.getMessage());
                        return null;
                    }
                }
            }, ExecutionType.TRANSACTION_SYNCH);
		
		    resultBean.setNextTask(bpmTaskBean);

		    long t2 = System.currentTimeMillis();
		    
			logger.log(Level.INFO, "performAction total: " + (t2-t0) + "ms, " +
					"[1]: " + (t1-t0) + "ms, " +
					"[2]: " + (t2-t1) + "ms " 
					);
            
        }
        catch (Throwable e)
        {
            logger.log(Level.SEVERE, "Error during process starting", e);

            resultBean.addError(SYSTEM_SOURCE, e.getMessage());
            return resultBean;
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
	public SaveResultBean saveAction(final HttpServletRequest request)
	{
		logger.info("saveAction ...");
		long t0 = System.currentTimeMillis();

		logger.warning("SAVE!");
		final SaveResultBean resultBean = new SaveResultBean();
		
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
                public void withContext(ProcessToolContext ctx) {
                    long t0 = System.currentTimeMillis();

                    BpmTask task = context.getBpmSession().getTaskData(taskId);

                    long t1 = System.currentTimeMillis();

                    TaskProcessor taskSaveProcessor = new TaskProcessor(task, getEventBus(), context.getMessageSource(), widgetData);

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
                        }

                    }

                    long t4 = System.currentTimeMillis();

                    logger.log(Level.INFO, "saveAction.withContext total: " + (t4 - t0) + "ms, " +
                            "[1]: " + (t1 - t0) + "ms, " +
                            "[2]: " + (t2 - t1) + "ms, " +
                            "[3]: " + (t3 - t2) + "ms, " +
                            "[4]: " + (t4 - t3) + "ms "
                    );
                }
            }, ExecutionType.TRANSACTION_SYNCH);

        }
        catch(Throwable e)
        {
            logger.log(Level.SEVERE, "Problem during data saving", e);
            resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage(
                    "request.handle.error.saveerror",
                    "Problem during data saving: {0}",
                    e.getLocalizedMessage()));
        }
		
		long t2 = System.currentTimeMillis();

		logger.log(Level.INFO, "saveAction total: " + (t2-t0) + "ms, " +
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
		logger.info("startNewProcess ...");
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

                    logger.log(Level.INFO, "startNewProcess.withContext total: " + (t3 - t0) + "ms, " +
                            "[1]: " + (t1 - t0) + "ms, " +
                            "[2]: " + (t2 - t1) + "ms, " +
                            "[3]: " + (t3 - t2) + "ms, "
                    );

                }
            }, ExecutionType.TRANSACTION_SYNCH);
            
    		long t2 = System.currentTimeMillis();

    		logger.log(Level.INFO, "startNewProcess total: " + (t2-t0) + "ms, " +
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
    public DataPagingBean<BpmTaskBean> searchTasks(final HttpServletRequest request)
    {
		logger.info("searchTasks ...");
		long t0 = System.currentTimeMillis();

    	final JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(request.getParameterMap());

        final List<BpmTaskBean> adminAlertBeanList = new ArrayList<BpmTaskBean>();

        final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());

        if(!context.isUserAuthorized())
            return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 0, dataTable.getEcho());

        final String sortCol = request.getParameter("iSortCol_0");
        final String sortDir = request.getParameter("sSortDir_0");
        final String searchString = request.getParameter("_AperteWorkflowActivitiesPortlet_WAR_aperteworkflow_sSearch");
        final String searchProcessKey = request.getParameter("processKey");
        String displayStartString = request.getParameter("iDisplayStart");
        String displayLengthString = request.getParameter("iDisplayLength");

        final Integer displayStart = Integer.parseInt(displayStartString);
        final Integer displayLength = Integer.parseInt(displayLengthString);

        final DataPagingBean<BpmTaskBean> pagingCollection = new DataPagingBean<BpmTaskBean>(
                adminAlertBeanList, 100, dataTable.getEcho());

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

                JQueryDataTableColumn sortingColumn = dataTable.getFirstSortingColumn();
                
                filter.setFilterOwnerLogin(context.getUser().getLogin());
                filter.setSortOrderCondition(mapColumnNameToOrderCondition(sortingColumn.getPropertyName()));
                filter.setSortOrder(sortingColumn.getSortedAsc() ? QueueOrder.ASC : QueueOrder.DESC);

                long t1 = System.currentTimeMillis();

                Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, displayStart, displayLength);

                for (BpmTask task : tasks) {
                    BpmTaskBean processBean = BpmTaskBean.createFrom(task, messageSource);

                    adminAlertBeanList.add(processBean);
                }

                long t2 = System.currentTimeMillis();

                int totalRecords = context.getBpmSession().getFilteredTasksCount(filter);
                pagingCollection.setiTotalRecords(totalRecords);
                pagingCollection.setiTotalDisplayRecords(totalRecords);
                pagingCollection.setAaData(adminAlertBeanList);

                long t3 = System.currentTimeMillis();

                logger.log(Level.INFO, "searchTasks.withContext total: " + (t3 - t0) + "ms, " +
                        "[1]: " + (t1 - t0) + "ms, " +
                        "[2]: " + (t2 - t1) + "ms " +
                        "[3]: " + (t3 - t2) + "ms "
                );

            }
        }, ExecutionType.TRANSACTION_SYNCH);

		long t2 = System.currentTimeMillis();

		logger.log(Level.INFO, "searchTasks total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms " 
				);
		
        return pagingCollection;
    }
	
	@RequestMapping(method = RequestMethod.POST, value = "/processes/loadProcessesList.json")
	@ResponseBody
	public DataPagingBean<BpmTaskBean> loadProcessesList(final HttpServletRequest request)
	{
		logger.info("loadProcessesList ...");
		long t0 = System.currentTimeMillis();
		
        final JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(request.getParameterMap());

		final String queueName = request.getParameter("queueName");
		final String queueType = request.getParameter("queueType");
		final String ownerLogin = request.getParameter("ownerLogin");

		final List<BpmTaskBean> adminAlertBeanList = new ArrayList<BpmTaskBean>();
		
		if(isNull(queueName) || isNull(queueType) || isNull(ownerLogin))
		{
			return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 0, dataTable.getEcho());
		}

        Locale locale = request.getLocale();

		final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());
		
		if(!context.isUserAuthorized())
			return new DataPagingBean<BpmTaskBean>(adminAlertBeanList, 0, dataTable.getEcho());

		final String searchString = request.getParameter("sSearch");

		
		final DataPagingBean<BpmTaskBean> pagingCollection = new DataPagingBean<BpmTaskBean>(
				adminAlertBeanList, 100, dataTable.getEcho());

		long t1 = System.currentTimeMillis();

        getProcessToolRegistry().withProcessToolContext(new ProcessToolContextCallback() {

            @Override
            public void withContext(ProcessToolContext ctx) {
                long t0 = System.currentTimeMillis();

                I18NSource messageSource = context.getMessageSource();

                boolean isQueue = "queue".equals(queueType);

                ProcessInstanceFilter filter = new ProcessInstanceFilter();
                if (isQueue) {
                    filter.addQueue(queueName);
                    filter.setFilterOwnerLogin(queueName);
                } else if ("process".equals(queueType)) {
                    filter.addOwner(ownerLogin);
                    filter.setFilterOwnerLogin(ownerLogin);
                    filter.addQueueType(QueueType.fromQueueId(queueName));
                    filter.setName(queueName);
                }

                filter.setExpression(searchString);
                filter.setLocale(messageSource.getLocale());

                JQueryDataTableColumn sortingColumn = dataTable.getFirstSortingColumn();

                filter.setSortOrderCondition(mapColumnNameToOrderCondition(sortingColumn.getPropertyName()));
                filter.setSortOrder(sortingColumn.getSortedAsc() ? QueueOrder.ASC : QueueOrder.DESC);

                long t1 = System.currentTimeMillis();

                Collection<BpmTask> tasks = context.getBpmSession().findFilteredTasks(filter, dataTable.getPageOffset(), dataTable.getPageLength());

                long t2 = System.currentTimeMillis();

                for (BpmTask task : tasks) {
                    BpmTaskBean taskBean = BpmTaskBean.createFrom(task, messageSource);

                    if (isQueue) {
                        taskBean.setQueueName(queueName);
                    }

                    if(hasUserRightsToTask(context, task))
                        taskBean.setUserCanClaim(true);

                    adminAlertBeanList.add(taskBean);
                }


                long t3 = System.currentTimeMillis();
                int totalRecords = context.getBpmSession().getFilteredTasksCount(filter);
                pagingCollection.setiTotalRecords(totalRecords);
                pagingCollection.setiTotalDisplayRecords(totalRecords);
                pagingCollection.setAaData(adminAlertBeanList);

                long t4 = System.currentTimeMillis();

                logger.log(Level.INFO, "loadProcessesList.withContext total: " + (t4 - t0) + "ms, " +
                        "[1]: " + (t1 - t0) + "ms, " +
                        "[2]: " + (t2 - t1) + "ms, " +
                        "[3]: " + (t3 - t2) + "ms, " +
                        "[4]: " + (t4 - t3) + "ms "
                );

            }
        }, ExecutionType.NO_TRANSACTION);

		long t2 = System.currentTimeMillis();

		logger.log(Level.INFO, "loadProcessesList total: " + (t2-t0) + "ms, " +
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

    private QueueOrderCondition mapColumnNameToOrderCondition(String columnName)
    {
        if(PROCESS_NAME_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_PROCESS_NAME_ORDER;

        else if(PROCESS_STEP_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_PROCESS_STEP_ORDER;

        else if(PROCESS_CODE_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_PROCESS_CODE_ORDER;

        else if(PROCESS_BUSINESS_STATUS_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_PROCESS_BUSINESS_STATUS_ORDER;

        else if(CREATOR_NAME_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_CREATOR_ORDER;

        else if(ASSIGNEE_NAME_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_ASSIGNEE_ORDER;

        else if(CREATED_DATE_COLUMN.equals(columnName))
            return QueueOrderCondition.SORT_BY_DATE_ORDER;

        else
            return null;
    }

	private static boolean isNull(String value) {
		return value == null || value.isEmpty() || "null".equals(value);
	}


}
