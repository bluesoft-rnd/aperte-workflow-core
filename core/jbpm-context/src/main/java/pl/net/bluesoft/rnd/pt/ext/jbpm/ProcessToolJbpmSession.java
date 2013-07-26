package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.ui.view.ViewEvent;
import org.aperteworkflow.util.SimpleXmlTransformer;
import org.drools.event.process.*;
import org.drools.runtime.process.NodeInstance;
import org.jbpm.task.*;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.StartNodeInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolSecurityException;
import pl.net.bluesoft.rnd.processtool.bpm.impl.AbstractProcessToolSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.AbstractBpmTask;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskDerivedBean;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.token.IAccessTokenFactory;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.query.BpmTaskQuery;
import pl.net.bluesoft.util.lang.Mapcar;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.Transformer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.Lang.keyFilter;

/**
 * jBPM session implementation
 */
public class ProcessToolJbpmSession extends AbstractProcessToolSession implements ProcessEventListener, TaskEventListener {
	private static final Integer DEFAULT_OFFSET_VALUE = 0;
	private static final Integer DEFAULT_LIMIT_VALUE = 1000;

	private static final ViewEvent REFRESH_VIEW = new ViewEvent(ViewEvent.Type.ACTION_COMPLETE);

	@AutoInject
	private IAccessTokenFactory accessTokenFactory;

	@AutoInject
	private ITokenService tokenService;

	private JbpmService jbpmService;

	public ProcessToolJbpmSession(String userLogin, Collection<String> roleNames, String substitutingUserLogin) {
		super(userLogin, roleNames, substitutingUserLogin);

        /* Dependency Injection */
		ObjectFactory.inject(this);

		this.jbpmService = JbpmService.getInstance();
	}

	@Override
	public ProcessToolBpmSession createSession(String userLogin) {
		return createSession(userLogin, null);
	}

	@Override
	public ProcessToolBpmSession createSession(String userLogin, Collection<String> roleNames) {
		return new ProcessToolJbpmSession(userLogin, roleNames, this.userLogin);
	}

	@Override
	public StartProcessResult startProcess(String processDefinitionId, String externalKey, String source) {
		ProcessDefinitionConfig config = getContext().getProcessDefinitionDAO().getActiveConfigurationByKey(processDefinitionId);

		if (!config.isEnabled()) {
			throw new IllegalArgumentException("Process definition has been disabled!");
		}

		startProcessParams = new StartProcessParams(config, externalKey, source, userLogin);

		try {
			getJbpmService().startProcess(config.getBpmProcessId(), getInitialParams());
			return new JbpmStartProcessResult(startProcessParams.newProcessInstance, startProcessParams.createdTasksForCurrentUser);
		}
		finally {
			startProcessParams = null;
		}
	}

	private static class JbpmStartProcessResult implements StartProcessResult {
		private final ProcessInstance processInstance;
		private final List<BpmTask> tasks;

		public JbpmStartProcessResult(ProcessInstance processInstance, List<BpmTask> tasks) {
			this.processInstance = processInstance;
			this.tasks = tasks;
		}

		@Override
		public ProcessInstance getProcessInstance() {
			return processInstance;
		}

		@Override
		public List<BpmTask> getTasksAssignedToCreator() {
			return Collections.unmodifiableList(tasks);
		}
	}

	private Map<String, Object> getInitialParams() {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("initiator", userLogin);
		return vars;
	}

	@Override
	public List<BpmTask> performAction(String actionName, String taskId) {
		BpmTask task = getTaskData(taskId);
		ProcessStateAction action = task.getCurrentProcessStateConfiguration().getProcessStateActionByName(actionName);

		return doPerformAction(action, task);
	}

	@Override
	public List<BpmTask> performAction(String actionName, BpmTask task) {
		return performAction(actionName, task, true);
	}

	@Override
	public List<BpmTask> performAction(String actionName, BpmTask task, boolean reloadTask) {
		if (reloadTask) {
			task = getTaskData(toAwfTaskId(task));
		}
		ProcessStateAction action = task.getCurrentProcessStateConfiguration().getProcessStateActionByName(actionName);
		return doPerformAction(action, task);
	}

	@Override
	public List<BpmTask> performAction(ProcessStateAction action, BpmTask task) {
		return doPerformAction(action, getTaskData(toAwfTaskId(task)));
	}

	private List<BpmTask> doPerformAction(ProcessStateAction action, BpmTask task) {
		if (task == null || task.isFinished()) {
			return null;
		}

		ProcessInstance processInstance = task.getProcessInstance();

		processInstance.setSimpleAttribute("ACTION", action.getBpmName());
		setStatus(processInstance, action);
		addLogEntry(action, task);
		save(processInstance);

		/*Task jbpmTask = ((JbpmTask)task).getTask();

		if (jbpmTask.getTaskData().getStatus() != Status.InProgress) {
			getJbpmService().startTask(jbpmTask.getId(), userLogin);
		}

		completeTaskParams = new CompleteTaskParams(task);

		try {
			getJbpmService().completeTask(jbpmTask.getId(), userLogin, null);
			return completeTaskParams.createdTasksForCurrentUser;
		}
		finally {
			completeTaskParams = null;
		}*/
		
		Task jbpmTask = ((JbpmTask)task).getTask();
		completeTaskParams = new CompleteTaskParams(task);
		try {
			getJbpmService().endTask(jbpmTask.getId(), userLogin, null, jbpmTask.getTaskData().getStatus() != Status.InProgress);
			return completeTaskParams.createdTasksForCurrentUser;
		}
		finally {
			completeTaskParams = null;
		}
	}

	private void setStatus(ProcessInstance processInstance, ProcessStateAction action) {
		if (Strings.hasText(action.getAssignProcessStatus())) {
			String processStatus = action.getAssignProcessStatus();
			ProcessStatus ps = processStatus.length() == 1 ? ProcessStatus.fromChar(processStatus.charAt(0)) : ProcessStatus.fromString(processStatus);
			processInstance.setStatus(ps);
		}
		else {
			// FINISHED status will be assigned in process completed event listener
			processInstance.setStatus(ProcessStatus.RUNNING);
		}
	}

	private void addLogEntry(ProcessStateAction action, BpmTask task) {
		ProcessStateConfiguration state = getContext().getProcessDefinitionDAO().getProcessStateConfiguration(task);

		ProcessInstanceLog log = new ProcessInstanceLog();

		log.setLogType(ProcessInstanceLog.LOG_TYPE_PERFORM_ACTION);
		log.setState(state);
		log.setEntryDate(new Date());
		log.setEventI18NKey("process.log.action-performed");
		log.setLogValue(action.getBpmName());
		log.setAdditionalInfo(nvl(action.getLabel(), action.getDescription(), action.getBpmName()));
		log.setUserLogin(userLogin);
		log.setUserSubstituteLogin(substitutingUserLogin);
		log.setExecutionId(task.getExecutionId());
		log.setOwnProcessInstance(task.getProcessInstance());
		task.getRootProcessInstance().addProcessLog(log);
	}

	@Override
	public BpmTask assignTaskFromQueue(String queueName) {
		return assignTaskFromQueue(queueName, null);
	}

	@Override
	public BpmTask assignTaskFromQueue(String queueName, BpmTask bpmTask) {
		List<ProcessQueue> configs = getUserQueuesFromConfig();
		List<String> names = keyFilter("name", configs);
		if (!names.contains(queueName)) {
			throw new ProcessToolSecurityException("queue.no.rights", queueName);
		}

		long taskId = bpmTask != null ? toJbpmTaskId(bpmTask) : 0;

		Task task = jbpmService.getTaskForAssign(queueName, taskId); 

		if (task == null) {
			log.warning("No tasks found in queue: " + queueName);
			return null;
		}

		ProcessInstance pi = getProcessData(toAwfPIId(task));

		if (pi == null) {
			log.warning("Process instance not found for instance id: " + toAwfPIId(task));
			return null;
		}

		getJbpmService().claimTask(task.getId(), userLogin);

		task = getJbpmService().getTask(task.getId());

		if (!userLogin.equals(getAssignee(task))) {
			log.warning("Task: + " + taskId + " not assigned to requesting user: " + userLogin);
			return null;
		}

		bpmTask = getBpmTask(task, pi);

		ProcessInstanceLog log = new ProcessInstanceLog();

		log.setLogType(ProcessInstanceLog.LOG_TYPE_CLAIM_PROCESS);
		log.setState(getContext().getProcessDefinitionDAO().getProcessStateConfiguration(bpmTask));
		log.setEntryDate(new Date());
		log.setEventI18NKey("process.log.process-assigned");
		log.setLogValue(queueName);
		log.setUserLogin(userLogin);
		log.setExecutionId(toAwfPIId(task));
		log.setOwnProcessInstance(pi);

		pi.getRootProcessInstance().addProcessLog(log);

		pi.setStatus(ProcessStatus.RUNNING);

		save(pi);

		return bpmTask;
	}

	@Override
	public void assignTaskToUser(String taskId, String userLogin) {
		getJbpmService().claimTask(toJbpmTaskId(taskId), userLogin);
	}

	@Override
	public BpmTask getTaskData(String taskId) {
		Task task = getJbpmService().getTask(toJbpmTaskId(taskId));
		if (task == null) {
			return null;
		}
		return getBpmTask(task);
	}

	@Override
	public BpmTask getPastEndTask(ProcessInstanceLog log) {
		ProcessInstance pi = log.getOwnProcessInstance();
		String endTaskName = findEndActivityName(pi);
		if (Strings.hasText(endTaskName)) {
			BpmTaskBean t = new BpmTaskBean();
			t.setProcessInstance(pi);
			t.setAssignee(userLogin);
			t.setTaskName(endTaskName);
			t.setFinished(true);
			return t;
		}
		return null;
	}

	private String findEndActivityName(ProcessInstance pi) {//TODO w pierwotnej wersji to zwracalo nazwe wezla END
		BpmTask latestTask = getLatestTask(pi);
		return latestTask != null ? latestTask.getTaskName() : null;
	}

	private BpmTask getLatestTask(ProcessInstance pi) {
		Task task = jbpmService.getLatestTask(toJbpmPIId(pi));
		return getBpmTask(task, null);
	}

	@Override
	public BpmTask getPastOrActualTask(ProcessInstanceLog log) {
		String taskName = null;
		if (log.getState() != null && Strings.hasText(log.getState().getName())) {
			taskName = log.getState().getName();
		}
		Task task = jbpmService.getPastOrActualTask(toJbpmPIId(log.getExecutionId()), log.getUserLogin(), taskName, new Date());
		return getBpmTask(task);
	}

	@Override
	public BpmTask refreshTaskData(BpmTask task) {
		Task refreshedTask = getJbpmService().getTask(toJbpmTaskId(task));

		if (refreshedTask == null || refreshedTask.getTaskData().getStatus() == Status.Suspended ||
				!userLogin.equals(getAssignee(refreshedTask))) {
			BpmTaskDerivedBean bpmTask = new BpmTaskDerivedBean(task);
			bpmTask.setFinished(true);
			log.warning("Task " + task.getExecutionId() + " not found");
			return bpmTask;
		}
		else {
			ProcessInstance processInstance = getProcessData(task.getProcessInstance().getInternalId());
			return getBpmTask(refreshedTask, processInstance);
		}
	}

	@Override
	public boolean isProcessRunning(String internalId) {
		if (internalId == null) {
			return false;
		}
		org.drools.runtime.process.ProcessInstance pi = getJbpmService().getProcessInstance(toJbpmPIId(internalId));
		return pi != null && pi.getState() != org.drools.runtime.process.ProcessInstance.STATE_COMPLETED;
	}

	@Override
	public int getTasksCount(String userLogin, QueueType... queueTypes) {
		return getTasksCount(userLogin, Arrays.asList(queueTypes));
	}

	@Override
	public int getTasksCount(String userLogin, Collection<QueueType> queueTypes) {
		return new BpmTaskQuery().user(userLogin).virtualQueues(queueTypes).count();
	}

	@Override
	public int getFilteredTasksCount(ProcessInstanceFilter filter) {
		return getFilterQuery(filter).count();
	}

	@Override
	public BpmTask getHistoryTask(String taskId) {
		return getBpmTask(getJbpmService().getTask(toJbpmTaskId(taskId)));
	}

	@Override
	public List<BpmTask> getAllTasks() {
		List<Task> tasks = jbpmService.getTasks();
		return getBpmTasks(tasks);
	}

	@Override
	public List<BpmTask> findUserTasks(ProcessInstance processInstance) {
		List<Task> tasks = jbpmService.getTasks(toJbpmPIId(processInstance), userLogin); 
		return getBpmTasks(tasks, processInstance);
	}

	@Override
	public List<BpmTask> findUserTasks(Integer offset, Integer limit) {
		List<Task> tasks = jbpmService.getTasks(userLogin, nvl(offset, DEFAULT_OFFSET_VALUE), nvl(limit, DEFAULT_LIMIT_VALUE));
		return getBpmTasks(tasks);
	}

	private List<BpmTask> getBpmTasks(List<Task> tasks) {
		Map<String, List<Task>> tasksByInternalId = groupByInternalId(tasks);
		Map<String, ProcessInstance> instances = getContext().getProcessInstanceDAO().getProcessInstanceByInternalIdMap(tasksByInternalId.keySet());
		List<BpmTask> result = new ArrayList<BpmTask>();

		for (Map.Entry<String, List<Task>> entry : tasksByInternalId.entrySet()) {
			String internalId = entry.getKey();
			List<Task> processTasks = entry.getValue();
			ProcessInstance pi = instances.get(internalId);

			if (pi == null) {
				log.warning("process " + internalId + " not found");
			}
			else {
				result.addAll(getBpmTasks(processTasks, pi));
			}
		}
		Collections.sort(result, BY_CREATE_DATE_DESC);
		return result;
	}

	private Map<String, List<Task>> groupByInternalId(List<Task> tasks) {
		return pl.net.bluesoft.util.lang.Collections.group(tasks, new Transformer<Task, String>() {
			@Override
			public String transform(Task task) {
				return toAwfPIId(task);
			}
		});
	}

	private List<BpmTask> getBpmTasks(List<Task> tasks, final ProcessInstance pi) {
		return new Mapcar<Task, BpmTask>(tasks) {
			@Override
			public BpmTask lambda(Task task) {
				return getBpmTask(task, pi);
			}
		}.go();
	}

	private static BpmTask getBpmTask(Task task, ProcessInstance pi) {
		return task != null ? new JbpmTask(pi, task) : null; // pi may be null since it can be lazy loaded
	}

	private static BpmTask getBpmTask(Task task) {
		return getBpmTask(task, null);
	}

	private static final Comparator<BpmTask> BY_CREATE_DATE_DESC = new Comparator<BpmTask>() {
		@Override
		public int compare(BpmTask o1, BpmTask o2) {
			return o2.getCreateDate().compareTo(o1.getCreateDate());
		}
	};

	@Override
	public List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin, Set<String> taskNames) {
		List<Task> tasks = jbpmService.getTasks(toJbpmPIId(pi), userLogin, taskNames);
		return getBpmTasks(tasks, pi);
	}

	@Override
	public List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin) {
		return findProcessTasks(pi, userLogin, null);
	}

	@Override
	public List<BpmTask> findProcessTasks(ProcessInstance pi) {
		return findProcessTasks(pi, null);
	}

	@Override
	public List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter) {
		return findFilteredTasks(filter, 0, -1);
	}

	@Override
	public List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, int offset, int maxResults) {
		return getFilterQuery(filter).page(offset, maxResults).list();
	}

	private BpmTaskQuery getFilterQuery(ProcessInstanceFilter filter) {
		BpmTaskQuery taskFilterQuery = new BpmTaskQuery();

   		/* Queues filter do not have owner */
		if (filter.getFilterOwnerLogin() != null) {
			taskFilterQuery.user(filter.getFilterOwnerLogin());
		}

		if (!filter.getQueueTypes().isEmpty()) {
			taskFilterQuery.virtualQueues(filter.getQueueTypes());
		}

   		/* Add condition for task names if any exists */
		if (!filter.getTaskNames().isEmpty()) {
			taskFilterQuery.taskNames(filter.getTaskNames());
		}

   		/* Add conidtion for created before date */
		if (filter.getCreatedBefore() != null) {
			taskFilterQuery.createdBefore(filter.getCreatedBefore());
		}

   		/* Add conidtion for created after date */
		if (filter.getCreatedAfter() != null) {
			taskFilterQuery.createdAfter(filter.getCreatedAfter());
		}

		if (!filter.getQueues().isEmpty()) {
			taskFilterQuery.queues(filter.getQueues());
		}

		taskFilterQuery.orderBy(filter.getSortOrderCondition(), filter.getSortOrder());

		return taskFilterQuery;
	}

	@Override
	public List<BpmTask> findRecentTasks(Date minDate, Integer offset, Integer limit) {
		List<BpmTask> recentTasks = new ArrayList<BpmTask>();
		ResultsPageWrapper<ProcessInstance> recentInstances = getContext().getProcessInstanceDAO().getRecentProcesses(userLogin, minDate, offset, limit);
		Collection<ProcessInstance> instances = recentInstances.getResults();
		for (ProcessInstance pi : instances) {
			List<BpmTask> tasks = findProcessTasks(pi, userLogin);
			if (tasks.isEmpty()) {
				BpmTask task = getMostRecentProcessHistoryTask(pi, userLogin, minDate);
				if (task != null) {
					recentTasks.add(task);
				}
			}
			else {
				recentTasks.addAll(tasks);
			}
		}
		return recentTasks;
	}

	private BpmTask getMostRecentProcessHistoryTask(ProcessInstance pi, String userLogin, Date minDate) {
		Task task = jbpmService.getMostRecentProcessHistoryTask(toJbpmPIId(pi), userLogin, minDate);
		return getBpmTask(task);
	}

	@Override
	public int getRecentTasksCount(Date minDate) {
		int count = 0;
		Collection<ProcessInstance> instances = getContext().getProcessInstanceDAO().getUserProcessesAfterDate(userLogin, minDate);
		for (ProcessInstance pi : instances) {
			List<BpmTask> tasks = findProcessTasks(pi, userLogin);
			if (tasks.isEmpty() && getMostRecentProcessHistoryTask(pi, userLogin, minDate) != null) {
				count += 1;
			}
			else {
				count += tasks.size();
			}
		}
		return count;
	}

	@Override
	public List<ProcessQueue> getUserAvailableQueues() {
		List<ProcessQueue> configs = getUserQueuesFromConfig();

		if (configs.isEmpty()) {
			return Collections.emptyList();
		}

		ProcessQueueSizeEvaluator sizeEvaluator = new ProcessQueueSizeEvaluator(configs);

		List<ProcessQueue> result = new ArrayList<ProcessQueue>();

		for (ProcessQueue q : configs) {
			result.add(new LazyProcessQueue(q, sizeEvaluator));
		}
		return result;
	}

	/**
	 * Get the last step name of the process
	 */
	private String getAssignee(Task task) {
		User actualOwner = task.getTaskData().getActualOwner();
		return actualOwner != null ? actualOwner.getId() : null;
	}

	@Override
	public void adminCancelProcessInstance(ProcessInstance pi) {
		log.severe("User: " + userLogin + " attempting to cancel process: " + pi.getInternalId());
		pi = getProcessData(pi.getInternalId());
		getJbpmService().abortProcessInstance(toJbpmPIId(pi));
		fillProcessAssignmentData(pi);
		save(pi);
		log.severe("User: " + userLogin + " has cancelled process: " + pi.getInternalId());
	}

	@Override
	public void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin) {
		log.severe("User: " + userLogin + " attempting to reassign task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to user: " + userLogin);

		pi = getProcessData(pi.getInternalId());
		Task task = getJbpmService().getTask(toJbpmTaskId(bpmTask));
		if (nvl(userLogin, "").equals(nvl(getAssignee(task), ""))) {
			log.severe("User: " + userLogin + " has not reassigned task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " as the user is the same: " + userLogin);
			return;
		}
		//this call should also take care of swimlanes
		getJbpmService().claimTask(toJbpmTaskId(bpmTask), userLogin);
		fillProcessAssignmentData(pi);
		log.info("Process.running:" + pi.isProcessRunning());
		save(pi);
		log.severe("User: " + userLogin + " has reassigned task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to user: " + userLogin);
	}

	private void fillProcessAssignmentData(ProcessInstance pi) {
		Set<String> assignees = new HashSet<String>();
		Set<String> queues = new HashSet<String>();
		List<BpmTask> processTasks = findProcessTasks(pi);

		for (BpmTask t : processTasks) {
			if (t.getAssignee() != null) {
				assignees.add(t.getAssignee());
			}
			else {
				queues.add(t.getGroupId());
			}
		}
		pi.setActiveTasks(processTasks.toArray(new BpmTask[processTasks.size()]));
		pi.setAssignees(assignees.toArray(new String[assignees.size()]));
		pi.setTaskQueues(queues.toArray(new String[queues.size()]));
	}

	@Override
	public void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action) {
		log.severe("User: " + userLogin + " attempting to complete task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to outcome: " + action);
		performAction(action, bpmTask);
		log.severe("User: " + userLogin + " has completed task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to outcome: " + action);
	}

	@Override
	public List<String> getAvailableLogins(String filter) {
		List<String> userIds = jbpmService.getAvailableUserLogins(filter,0,20);
		Collections.sort(userIds);
		return userIds;
	}

	@Override
	public List<GraphElement> getProcessHistory(ProcessInstance pi) {
		return null;//jbpm.getProcessHistoryProvider().getProcessHistory(pi);
	}

	@Override
	public byte[] getProcessLatestDefinition(String definitionKey) {
		ProcessDefinitionConfig config = getContext().getProcessDefinitionDAO().getActiveConfigurationByKey(definitionKey);
		if (config == null) {
			return null;
		}
		return jbpmService.getRepository().getResource(config.getDeploymentId(), ProcessResourceNames.DEFINITION);
	}

	@Override
	public byte[] getProcessMapImage(ProcessInstance pi) {
		return fetchProcessResource(pi, ProcessResourceNames.MAP_IMAGE);
	}

	@Override
	public byte[] getProcessDefinition(ProcessInstance pi) {
		return fetchProcessResource(pi, ProcessResourceNames.DEFINITION);
	}

	private byte[] fetchProcessResource(ProcessInstance pi, String resourceName) {
		String deploymentId = pi.getDefinition().getDeploymentId();
		return jbpmService.getRepository().getResource(deploymentId, resourceName);
	}

	private InputStream prepareForDeployment(final String processId, InputStream definitionStream) {
		SimpleXmlTransformer xmlTransformer = new SimpleXmlTransformer(definitionStream);

		xmlTransformer.transformAttributes(PROCESS_ID_XPATH, new SimpleXmlTransformer.AttributeTransformer() {
			@Override
			public String transform(String processIdWithoutVersion) {
				return processId;
			}
		});

		xmlTransformer.transformAttributes(SUBPROCESS_ID_XPATH, SUBSTITUTE_SUBPROCESS_IDS);

		return new ByteArrayInputStream(xmlTransformer.toString().getBytes());
	}

	private static final String PROCESS_ID_XPATH = "/definitions/process/@id";
	private static final String SUBPROCESS_ID_XPATH = "//callActivity/@calledElement";

	private static final SimpleXmlTransformer.AttributeTransformer SUBSTITUTE_SUBPROCESS_IDS = new SimpleXmlTransformer.AttributeTransformer() {
		@Override
		public String transform(String subprocessId) {
			if (ProcessDefinitionConfig.hasVersion(subprocessId)) {
				checkForExistingProcess(subprocessId);
				return subprocessId;
			}
			else {
				return getLatestProcessId(subprocessId);
			}
		}
	};

	private static void checkForExistingProcess(String processId) {
		ProcessDefinitionConfig config = getContext().getProcessDefinitionDAO()
				.getConfigurationByProcessId(processId);

		if (config == null) {
			throw new RuntimeException("No process defined with processId " + processId);
		}
	}

	private static String getLatestProcessId(String bpmDefinitionKey) {
		ProcessDefinitionConfig config = getContext().getProcessDefinitionDAO()
				.getActiveConfigurationByKey(bpmDefinitionKey);

		// it is necessary that subprocess bundles are deployed before processes bundles

		if (config == null) {
			throw new RuntimeException("No process defined with key " + bpmDefinitionKey);
		}

		return config.getBpmProcessId();
	}

	@Override
	public boolean differsFromTheLatest(String bpmDefinitionKey, byte[] newDefinition) {
		byte[] latestDefinition = getProcessLatestDefinition(bpmDefinitionKey);

		if (latestDefinition == null) {
			return true;
		}

		String latestDefinitionStr = prepareForComparison(latestDefinition, false);
		String newDefinitionStr = prepareForComparison(newDefinition, true);

		return !latestDefinitionStr.equals(newDefinitionStr);
	}

	private String prepareForComparison(byte[] definition, boolean substituteNewestSubprocessIds) {
		SimpleXmlTransformer xmlTransformer = new SimpleXmlTransformer(new ByteArrayInputStream(definition));

		xmlTransformer.transformAttributes(PROCESS_ID_XPATH, new SimpleXmlTransformer.AttributeTransformer() {
			@Override
			public String transform(String processIdWithoutVersion) {
				return "";
			}
		});

		if (substituteNewestSubprocessIds) {
			xmlTransformer.transformAttributes(SUBPROCESS_ID_XPATH, SUBSTITUTE_SUBPROCESS_IDS);
		}
		return xmlTransformer.toString();
	}

	@Override
	public String deployProcessDefinition(String processId, InputStream definitionStream, InputStream processMapImageStream) {
		String deploymentId = jbpmService.addProcessDefinition(prepareForDeployment(processId, definitionStream));

		if (processMapImageStream != null) {
			jbpmService.getRepository().addResource(deploymentId, ProcessResourceNames.MAP_IMAGE, processMapImageStream);
		}
		return deploymentId;
	}

	private JbpmService getJbpmService() {
		JbpmService.setProcessEventListener(this);
		JbpmService.setTaskEventListener(this);
		return jbpmService;
	}

	@Override
	public void beforeProcessStarted(ProcessStartedEvent event) {
	}

	@Override
	public void afterProcessStarted(ProcessStartedEvent event) {
	}

	@Override
	public void beforeProcessCompleted(ProcessCompletedEvent event) {
	}

	@Override
	public void afterProcessCompleted(ProcessCompletedEvent event) {
		handleProcessCompleted(event.getProcessInstance());
	}

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		if (event.getNodeInstance() instanceof StartNodeInstance) {
			handleProcessStarted(event.getProcessInstance());
		}
		else if (event.getNodeInstance() instanceof SubProcessNodeInstance) {
			SubProcessNode node = (SubProcessNode)event.getNodeInstance().getNode();
			handleSubprocess(node, event.getProcessInstance());
		}
		else if (event.getNodeInstance() instanceof WorkItemNodeInstance) {
			copyAttributesFromJbpm(event.getProcessInstance(), event.getNodeInstance());
		}
		//TODO tu sie da past end task zaimpl
	}

	@Override
	public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
	}

	@Override
	public void beforeNodeLeft(ProcessNodeLeftEvent event) {
		if (event.getNodeInstance() instanceof WorkItemNodeInstance) {
			copyAttributesIntoJbpm(event.getProcessInstance(), event.getNodeInstance());
		}
	}

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
	}

	@Override
	public void beforeVariableChanged(ProcessVariableChangedEvent event) {
	}

	@Override
	public void afterVariableChanged(ProcessVariableChangedEvent event) {
	}

	// Handles tasks put into a queue during creation

	@Override
	public void taskCreated(TaskUserEvent event) {
		refreshDataForNativeQuery();

		broadcastEvent(REFRESH_VIEW);
	}

	// Handles tasks assigned during creation or picked from a queue

	@Override
	public void taskClaimed(TaskUserEvent event) {
		BpmTask task = getBpmTask(getJbpmService().getTask(event.getTaskId()));

		if (completeTaskParams != null) {
			completeTaskParams.addCreatedTask(task, userLogin.equals(event.getUserId()));
		}
		else if (startProcessParams != null) {
			startProcessParams.addCreatedTask(task, userLogin.equals(event.getUserId()));
		}

		assignTokens(task);
		refreshDataForNativeQuery();

		broadcastEvent(new BpmEvent(BpmEvent.Type.ASSIGN_TASK, task, userLogin));
		broadcastEvent(REFRESH_VIEW);
	}

	private void refreshDataForNativeQuery() {
		getJbpmService().refreshDataForNativeQuery();
	}

	private void assignTokens(BpmTask userTask) {
		ProcessStateConfiguration stateConfiguration = getContext().getProcessDefinitionDAO()
				.getProcessStateConfiguration(userTask);  //TODO optymalizacja

		Boolean isAccessibleByToken = stateConfiguration.getEnableExternalAccess();

		/* Step is accessible by token, generate one */
		if (isAccessibleByToken != null && isAccessibleByToken) {
			generateAccessToken(userTask);
		}
	}

	private void generateAccessToken(BpmTask userTask) {
		log.log(Level.INFO, "Generate token [Thread: ]" + Thread.currentThread().getId() + ']');
		try {
			/* Generate new token */
			ProcessStateConfiguration currentState = userTask.getCurrentProcessStateConfiguration();

			/* No current state, abort */
			if (currentState == null) {
				return;
			}

			/* For each action, create token */
			for (ProcessStateAction action : currentState.getActions()) {
				/* Action is not accessible by external token. Skip it */
				if (action.getHideForExternalAccess()) {
					continue;
				}

				AccessToken token = accessTokenFactory.create(userTask, action.getBpmName());
				log.log(Level.INFO, "Token generated: " + token.getToken());

				/* Save token */
				getContext().getHibernateSession().saveOrUpdate(token);
			}
			getContext().getHibernateSession().flush();
		}
		catch (Exception ex) {
			log.log(Level.SEVERE, "Problem during token generation", ex);
		}
	}

	@Override
	public void taskStarted(TaskUserEvent event) {
	}

	@Override
	public void taskStopped(TaskUserEvent event) {
	}

	@Override
	public void taskReleased(TaskUserEvent event) {
	}

	@Override
	public void taskCompleted(TaskUserEvent event) {
		/* Remove all task tokens, if any exists */
		/* Delete all tokens for this taskId */
		tokenService.deleteTokensByTaskId(Long.parseLong(toAwfTaskId(completeTaskParams.task)));

		fillProcessAssignmentData(completeTaskParams.task.getProcessInstance());

		refreshDataForNativeQuery();

		broadcastEvent(new BpmEvent(BpmEvent.Type.TASK_FINISHED, completeTaskParams.task, userLogin));
		broadcastEvent(new BpmEvent(BpmEvent.Type.SIGNAL_PROCESS, completeTaskParams.task, nvl(substitutingUserLogin, userLogin)));
		broadcastEvent(REFRESH_VIEW);
	}

	@Override
	public void taskFailed(TaskUserEvent event) {
	}

	@Override
	public void taskSkipped(TaskUserEvent event) {
	}

	@Override
	public void taskForwarded(TaskUserEvent event) {
	}

	private static class StartProcessParams extends TaskCapturer {
		public final ProcessDefinitionConfig config;
		public final String externalKey;
		public final String source;
		public final String creator;
		public ProcessInstance newProcessInstance;

		public StartProcessParams(ProcessDefinitionConfig config, String externalKey, String source, String creator) {
			this.config = config;
			this.externalKey = externalKey;
			this.source = source;
			this.creator = creator;
		}

		public ProcessInstance createFromParams(org.drools.runtime.process.ProcessInstance jbpmProcessInstance, ProcessInstance parentProcessInstance) {
			ProcessInstance newProcessInstance = new ProcessInstance();

			newProcessInstance.setDefinition(config);
			newProcessInstance.setDefinitionName(config.getBpmDefinitionKey());
			newProcessInstance.setCreatorLogin(creator);
			newProcessInstance.setCreateDate(new Date());
			newProcessInstance.setExternalKey(externalKey);
			newProcessInstance.setInternalId(toAwfPIId(jbpmProcessInstance));
			newProcessInstance.setStatus(ProcessStatus.NEW);
			newProcessInstance.addOwner(creator);

			newProcessInstance.addAttribute(new ProcessInstanceSimpleAttribute("creator", creator));
			newProcessInstance.addAttribute(new ProcessInstanceSimpleAttribute("creatorName", getRegistry().getUserSource().getUserByLogin(creator).getRealName()));
			newProcessInstance.addAttribute(new ProcessInstanceSimpleAttribute("source", source));

			if (parentProcessInstance != null) {
				newProcessInstance.setParent(parentProcessInstance);
				parentProcessInstance.getChildren().add(newProcessInstance);
				newProcessInstance.addOwners(parentProcessInstance.getOwners());
			}

			ProcessInstanceLog log = new ProcessInstanceLog();

			log.setState(null);
			log.setEntryDate(new Date());
			log.setEventI18NKey("process.log.process-started");
			log.setUserLogin(creator);
			log.setLogType(ProcessInstanceLog.LOG_TYPE_START_PROCESS);
			log.setOwnProcessInstance(newProcessInstance);

			newProcessInstance.getRootProcessInstance().addProcessLog(log);

			return newProcessInstance;
		}
	}

	private static class SubprocessParams {
		public final String processId;
		public final org.drools.runtime.process.ProcessInstance parentProcessInstance;

		public SubprocessParams(String processId, org.drools.runtime.process.ProcessInstance parentProcessInstance) {
			this.processId = processId;
			this.parentProcessInstance = parentProcessInstance;
		}
	}

	private static class CompleteTaskParams extends TaskCapturer {
		public final BpmTask task;

		private CompleteTaskParams(BpmTask task) {
			this.task = task;
		}
	}

	private static class TaskCapturer {
		public final List<BpmTask> createdTasksForCurrentUser = new ArrayList<BpmTask>();

		public void addCreatedTask(BpmTask task, boolean assignedToCurrentUser) {
			if (assignedToCurrentUser) {
				createdTasksForCurrentUser.add(task);
			}
		}
	}

	private StartProcessParams startProcessParams;
	private SubprocessParams subprocessParams;
	private CompleteTaskParams completeTaskParams;

	private void handleProcessStarted(org.drools.runtime.process.ProcessInstance jbpmProcessInstance) {
		ProcessInstance newProcessInstance;

		if (subprocessParams != null) {
			try {
				ProcessInstance parentProcessInstance = getByInternalId(toAwfPIId(subprocessParams.parentProcessInstance));
				ProcessDefinitionConfig config = getContext().getProcessDefinitionDAO()
						.getConfigurationByProcessId(subprocessParams.processId);

				String creator = parentProcessInstance.getCreatorLogin();

				StartProcessParams params = new StartProcessParams(
						config, null, "parent_process", creator
				);
				newProcessInstance = params.createFromParams(jbpmProcessInstance, parentProcessInstance);

				save(newProcessInstance);

				/* Inform about parent process halt */
				broadcastEvent(new BpmEvent(BpmEvent.Type.PROCESS_HALTED, parentProcessInstance, creator));
			}
			finally {
				subprocessParams = null;
			}
		}
		else {
			newProcessInstance = startProcessParams.createFromParams(jbpmProcessInstance, null);

			save(newProcessInstance);

			startProcessParams.newProcessInstance = newProcessInstance;
		}

		broadcastEvent(new BpmEvent(BpmEvent.Type.NEW_PROCESS, newProcessInstance, newProcessInstance.getCreatorLogin()));
	}

	private void handleSubprocess(SubProcessNode node, org.drools.runtime.process.ProcessInstance processInstance) {
		subprocessParams = new SubprocessParams(node.getProcessId(), processInstance);
	}

	private void handleProcessCompleted(org.drools.runtime.process.ProcessInstance jbpmProcessInstance) {
		ProcessInstance processInstance = getByInternalId(toAwfPIId(jbpmProcessInstance));

		processInstance.setStatus(ProcessStatus.FINISHED);
		save(processInstance);

		broadcastEvent(new BpmEvent(BpmEvent.Type.END_PROCESS, processInstance, userLogin));
	}

	private void copyAttributesFromJbpm(org.drools.runtime.process.ProcessInstance jbpmProcessInstance, NodeInstance nodeInstance) {
		ProcessInstance processInstance = getByInternalId(toAwfPIId(jbpmProcessInstance));
		Map<String, Object> variables = ((WorkflowProcessInstanceImpl)nodeInstance.getProcessInstance()).getVariables();

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			processInstance.setSimpleAttribute(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
		}
		save(processInstance);
	}

	private void copyAttributesIntoJbpm(org.drools.runtime.process.ProcessInstance jbpmProcessInstance, NodeInstance nodeInstance) {
		ProcessInstance processInstance = getByInternalId(toAwfPIId(jbpmProcessInstance));

		for (Map.Entry<String, String> entry : processInstance.getSimpleAttributeValues().entrySet()) {
			nodeInstance.getProcessInstance().setVariable(entry.getKey(), entry.getValue());
		}
	}

	private static class JbpmTask extends AbstractBpmTask implements Serializable {
		private ProcessInstance processInstance;
		private final Task task;

		private JbpmTask(ProcessInstance processInstance, Task task) {
			this.processInstance = processInstance;
			this.task = task;
		}

		@Override
		public boolean isFinished() {
			return task.getTaskData().getStatus() == Status.Completed;
		}

		@Override
		public Date getFinishDate() {
			return task.getTaskData().getCompletedOn();
		}

		@Override
		public Date getCreateDate() {
			return task.getTaskData().getCreatedOn();
		}

		@Override
		public String getExecutionId() {
			return toAwfPIId(task);
		}

		@Override
		public ProcessInstance getProcessInstance() {
			if (processInstance == null) {
				processInstance = getByInternalId(toAwfPIId(task));
			}
			return processInstance;
		}

		@Override
		public String getInternalTaskId() {
			return toAwfTaskId(task.getId());
		}

		@Override
		public String getTaskName() {
			List<I18NText> names = task.getNames();
			return names.isEmpty() ? null : names.get(0).getText();
		}

		@Override
		public String getCreator() {
			User createdBy = task.getTaskData().getCreatedBy();
			return createdBy != null ? createdBy.getId() : null;
		}

		@Override
		public String getAssignee() {
			User owner = task.getTaskData().getActualOwner();
			return owner != null ? owner.getId() : null;
		}

		@Override
		public String getGroupId() {
			if (getAssignee() != null) {
				return null;
			}

			List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();

			for (OrganizationalEntity potentialOwner : potentialOwners) {
				if (potentialOwner instanceof Group) {
					return potentialOwner.getId();
				}
			}
			return null;
		}

		public Task getTask() {
			return task;
		}

		@Override
		public ProcessDefinitionConfig getProcessDefinition() {
			return getContext().getProcessDefinitionDAO().getCachedDefinitionById(getProcessInstance());
		}
	}

	private static class LazyProcessQueue implements ProcessQueue, Serializable {
		private final ProcessQueue queue;
		private final ProcessQueueSizeEvaluator sizeEvaluator;

		public LazyProcessQueue(ProcessQueue queue, ProcessQueueSizeEvaluator sizeEvaluator) {
			this.queue = queue;
			this.sizeEvaluator = sizeEvaluator;
		}

		@Override
		public String getName() {
			return queue.getName();
		}

		@Override
		public boolean isBrowsable() {
			return queue.isBrowsable();
		}

		@Override
		public int getProcessCount() {
			return sizeEvaluator.getSize(queue.getName());
		}

		@Override
		public String getDescription() {
			return queue.getDescription();
		}

		@Override
		public boolean getUserAdded() {
			return queue.getUserAdded();
		}
	}

	private static long toJbpmPIId(ProcessInstance processInstance) {
		return toJbpmPIId(processInstance.getInternalId());
	}

	private static long toJbpmPIId(String internalId) {
		return Long.parseLong(internalId);
	}

	private static String toAwfPIId(org.drools.runtime.process.ProcessInstance processInstance) {
		return toAwfPIId(processInstance.getId());
	}

	private static String toAwfPIId(Task task) {
		return toAwfPIId(task.getTaskData().getProcessInstanceId());
	}

	private static String toAwfPIId(long processInstanceId) {
		return String.valueOf(processInstanceId);
	}

	private static Long toJbpmTaskId(String taskId) {
		return taskId != null ? Long.parseLong(taskId) : null;
	}

	private static Long toJbpmTaskId(BpmTask t) {
		return toJbpmTaskId(t.getInternalTaskId());
	}

	private static String toAwfTaskId(Long taskId) {
		return taskId != null ? String.valueOf(taskId) : null;
	}

	private static String toAwfTaskId(BpmTask task) {
		return task.getInternalTaskId();
	}

	private static ProcessInstance getByInternalId(String internalId) {
		return getContext().getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
	}

	private static void save(ProcessInstance processInstance) {
		getContext().getProcessInstanceDAO().saveProcessInstance(processInstance);
	}

	private static class ProcessQueueSizeEvaluator implements Serializable {
		private List<ProcessQueue> configs;
		private Map<String, Integer> counts;

		public ProcessQueueSizeEvaluator(List<ProcessQueue> configs) {
			this.configs = configs;
		}

		public int getSize(String queueName) {
			Integer result = getCounts().get(queueName);
			return result != null ? result : 0;
		}

		private Map<String, Integer> getCounts() {
			if (counts == null) {
				List<String> names = keyFilter("name", configs);
				List<Object[]> rows = JbpmService.getInstance().getTaskCounts(names);
				counts = new HashMap<String, Integer>();
				for (Object[] t : rows) {
					counts.put((String)t[0], ((Number)t[1]).intValue());
				}
				configs = null;
			}
			return counts;
		}
	}
}
