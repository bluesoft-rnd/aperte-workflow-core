package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.ui.view.ViewEvent;
import org.aperteworkflow.util.SimpleXmlTransformer;
import org.drools.event.process.*;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.NodeInstance;
import org.jbpm.task.*;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.StartNodeInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
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
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
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
	private StatefulKnowledgeSession ksession;

	public ProcessToolJbpmSession(UserData user, Collection<String> roleNames) {
		super(user, roleNames);

        /* Dependency Injection */
		ObjectFactory.inject(this);

		this.jbpmService = JbpmService.getInstance();
	}

	@Override
	public ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames) {
		ProcessToolJbpmSession session = new ProcessToolJbpmSession(user, roleNames);
		session.substitutingUser = this.user;
		session.substitutingUserEventBusManager = this.eventBusManager;
		return session;
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionId, String externalKey, String description, String keyword, String source) {
		ProcessDefinitionConfig config = getContext().getProcessDefinitionDAO().getActiveConfigurationByKey(processDefinitionId);

		if (!config.isEnabled()) {
			throw new IllegalArgumentException("Process definition has been disabled!");
		}

		startProcessParams = new StartProcessParams(config, externalKey, description, keyword, source, loadOrCreateUser(user));

		try {
			getKSession().startProcess(config.getBpmProcessId(), getInitialParams());
			return startProcessParams.newProcessInstance;
		}
		finally {
			startProcessParams = null;
		}
	}

	private Map<String, Object> getInitialParams() {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("initiator", user.getLogin());
		return vars;
	}

	@Override
	public BpmTask performAction(ProcessStateAction action, BpmTask task) {
		task = getTaskData(toAwfTaskId(task));

		if (task == null || task.isFinished()) {
			return task;
		}

		ProcessInstance processInstance = task.getProcessInstance();

		processInstance.setSimpleAttribute("ACTION", action.getBpmName());
		setStatus(processInstance, action);
		addLogEntry(action, task);
		save(processInstance);

		Task jbpmTask = ((JbpmTask)task).getTask();

		if (jbpmTask.getTaskData().getStatus() != Status.InProgress) {
			getTaskService().start(jbpmTask.getId(), user.getLogin());
		}

		completeTaskParams = new CompleteTaskParams(task);

		try {
			getTaskService().complete(jbpmTask.getId(), user.getLogin(), null);

			if (!completeTaskParams.createdTasksForCurrentUser.isEmpty()) {
				return completeTaskParams.createdTasksForCurrentUser.get(0);
			}
		}
		finally {
			completeTaskParams = null;
		}

        /* Task assigned to queue */

		BpmTaskDerivedBean completedTask = new BpmTaskDerivedBean(task);

		completedTask.setFinished(true);
		completedTask.setProcessInstance(processInstance);

		return completedTask;
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
		log.setUser(findOrCreateUser(user));
		log.setUserSubstitute(getSubstitutingUser());
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

		Task task = jbpmService.createTaskQuery()
				.groupId(queueName)
				.taskId(taskId)
				.assigneeIsNull()
				.first();

		if (task == null) {
			log.warning("No tasks found in queue: " + queueName);
			return null;
		}

		ProcessInstance pi = getProcessData(toAwfPIId(task));

		if (pi == null) {
			log.warning("Process instance not found for instance id: " + toAwfPIId(task));
			return null;
		}

		getTaskService().claim(task.getId(), user.getLogin());

		task = getTaskService().getTask(task.getId());

		if (!user.getLogin().equals(getAssignee(task))) {
			log.warning("Task: + " + taskId + " not assigned to requesting user: " + user.getLogin());
			return null;
		}

		bpmTask = getBpmTask(task, pi);

		ProcessInstanceLog log = new ProcessInstanceLog();

		log.setLogType(ProcessInstanceLog.LOG_TYPE_CLAIM_PROCESS);
		log.setState(getContext().getProcessDefinitionDAO().getProcessStateConfiguration(bpmTask));
		log.setEntryDate(new Date());
		log.setEventI18NKey("process.log.process-assigned");
		log.setLogValue(queueName);
		log.setUser(findOrCreateUser(user));
		log.setExecutionId(toAwfPIId(task));
		log.setOwnProcessInstance(pi);

		pi.getRootProcessInstance().addProcessLog(log);

		pi.setStatus(ProcessStatus.RUNNING);

		save(pi);

		return bpmTask;
	}

	@Override
	public void assignTaskToUser(String taskId, String userLogin) {
		getTaskService().claim(toJbpmTaskId(taskId), userLogin);
	}

	@Override
	public BpmTask getTaskData(String taskId) {
		Task task = getTaskService().getTask(toJbpmTaskId(taskId));
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
			t.setAssignee(user.getLogin());
			t.setOwner(user);
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
		Task task = jbpmService.createTaskQuery()
				.processInstanceId(toJbpmPIId(pi))
				.completed()
				.orderByCompleteDateDesc()
				.first();
		return getBpmTask(task, null);
	}

	@Override
	public BpmTask getPastOrActualTask(ProcessInstanceLog log) {
		String taskName = null;
		if (log.getState() != null && Strings.hasText(log.getState().getName())) {
			taskName = log.getState().getName();
		}

		Task task = jbpmService.createTaskQuery()
				.assignee(log.getUser().getLogin())
				.processInstanceId(toJbpmPIId(log.getExecutionId()))
				.completedAfter(new Date())
				.activityName(taskName)
				.orderByCompleteDate()
				.first();

		return getBpmTask(task);
	}

	@Override
	public BpmTask refreshTaskData(BpmTask task) {
		Task refreshedTask = getTaskService().getTask(toJbpmTaskId(task));

		if (refreshedTask == null || refreshedTask.getTaskData().getStatus() == Status.Suspended ||
				!user.getLogin().equals(getAssignee(refreshedTask))) {
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
		org.drools.runtime.process.ProcessInstance pi = getKSession().getProcessInstance(toJbpmPIId(internalId));
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
		return getBpmTask(getTaskService().getTask(toJbpmTaskId(taskId)));
	}

	@Override
	public List<BpmTask> getAllTasks() {
		List<Task> tasks = jbpmService.createTaskQuery().orderByTaskIdDesc().list();

		return getBpmTasks(tasks);
	}

	@Override
	public List<BpmTask> findUserTasks(ProcessInstance processInstance) {
		List<Task> tasks = jbpmService.createTaskQuery()
				.processInstanceId(toJbpmPIId(processInstance))
				.assignee(user.getLogin())
				.active()
				.list();

		return getBpmTasks(tasks, processInstance);
	}

	@Override
	public List<BpmTask> findUserTasks(Integer offset, Integer limit) {
		limit = nvl(limit, DEFAULT_LIMIT_VALUE);
		offset = nvl(offset, DEFAULT_OFFSET_VALUE);

		List<Task> tasks = jbpmService.createTaskQuery()
				.assignee(user.getLogin())
				.active()
				.page(offset, limit)
				.list();

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

	private BpmTask getBpmTask(Task task, ProcessInstance pi) {
		return task != null ? new JbpmTask(pi, task) : null; // pi may be null since it can be lazy loaded
	}

	private BpmTask getBpmTask(Task task) {
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
		List<Task> tasks = jbpmService.createTaskQuery()
				.processInstanceId(toJbpmPIId(pi))
				.active()
				.assignee(userLogin)
				.activityNames(taskNames)
				.list();
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
		if (filter.getFilterOwner() != null) {
			taskFilterQuery.user(filter.getFilterOwner().getLogin());
		}

		if (!filter.getQueueTypes().isEmpty()) {
			taskFilterQuery.virtualQueues(filter.getQueueTypes());
		}

//		/* Prepare data for owner lists and not owner list */
//		Collection<String> ownerNames = new HashSet<String>();
//		for (UserData userData : filter.getOwners()) {
//			ownerNames.add(userData.getLogin());
//		}

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

//   		/* Add condition for owner */
//		if (!ownerNames.isEmpty()) {
//			taskFilterQuery.owners(ownerNames);
//		}
		return taskFilterQuery;
	}

	@Override
	public List<BpmTask> findRecentTasks(Date minDate, Integer offset, Integer limit) {
		List<BpmTask> recentTasks = new ArrayList<BpmTask>();
		UserData user = getUser();
		ResultsPageWrapper<ProcessInstance> recentInstances = getContext().getProcessInstanceDAO().getRecentProcesses(user, minDate, offset, limit);
		Collection<ProcessInstance> instances = recentInstances.getResults();
		for (ProcessInstance pi : instances) {
			List<BpmTask> tasks = findProcessTasks(pi, user.getLogin());
			if (tasks.isEmpty()) {
				BpmTask task = getMostRecentProcessHistoryTask(pi, user, minDate);
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

	private BpmTask getMostRecentProcessHistoryTask(ProcessInstance pi, UserData user, Date minDate) {
		Task task = jbpmService.createTaskQuery()
				.assignee(user.getLogin())
				.processInstanceId(toJbpmPIId(pi))
				.completedAfter(minDate)
				.orderByCompleteDateDesc()
				.first();
		if (task == null) {
			return null;
		}
		return getBpmTask(task);
	}

	@Override
	public int getRecentTasksCount(Date minDate) {
		int count = 0;
		UserData user = getUser();
		Collection<ProcessInstance> instances = getContext().getProcessInstanceDAO().getUserProcessesAfterDate(user, minDate);
		for (ProcessInstance pi : instances) {
			List<BpmTask> tasks = findProcessTasks(pi, user.getLogin());
			if (tasks.isEmpty() && getMostRecentProcessHistoryTask(pi, user, minDate) != null) {
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
		log.severe("User: " + user.getLogin() + " attempting to cancel process: " + pi.getInternalId());
		pi = getProcessData(pi.getInternalId());
		getKSession().abortProcessInstance(toJbpmPIId(pi));
		fillProcessAssignmentData(pi);
		save(pi);
		log.severe("User: " + user.getLogin() + " has cancelled process: " + pi.getInternalId());
	}

	@Override
	public void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin) {
		log.severe("User: " + user.getLogin() + " attempting to reassign task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to user: " + userLogin);

		pi = getProcessData(pi.getInternalId());
		Task task = getTaskService().getTask(toJbpmTaskId(bpmTask));
		if (nvl(userLogin, "").equals(nvl(getAssignee(task), ""))) {
			log.severe("User: " + user.getLogin() + " has not reassigned task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " as the user is the same: " + userLogin);
			return;
		}
		//this call should also take care of swimlanes
		getTaskService().claim(toJbpmTaskId(bpmTask), userLogin);
		fillProcessAssignmentData(pi);
		log.info("Process.running:" + pi.isProcessRunning());
		save(pi);
		log.severe("User: " + user.getLogin() + " has reassigned task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to user: " + userLogin);
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
		log.severe("User: " + user.getLogin() + " attempting to complete task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to outcome: " + action);
		performAction(action, bpmTask);
		log.severe("User: " + user.getLogin() + " has completed task " + toJbpmTaskId(bpmTask) + " for process: " + pi.getInternalId() + " to outcome: " + action);
	}

	@Override
	public List<String> getAvailableLogins(String filter) {
		List<String> userIds = jbpmService.createUserQuery()
				.selectId()
				.whereIdLike(filter != null ? '%' + filter + '%' : null)
				.page(0, 20)
				.list();

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
		ksession = null;
		return deploymentId;
	}

	private StatefulKnowledgeSession getKSession() {
		if (ksession == null) {
			ksession = jbpmService.getSession();
		}
		JbpmService.setProcessEventListener(this);
		JbpmService.setTaskEventListener(this);
		return ksession;
	}

	private TaskService getTaskService() {
		JbpmService.setProcessEventListener(this);
		JbpmService.setTaskEventListener(this);
		return jbpmService.getTaskService();
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
		BpmTask task = getBpmTask(getTaskService().getTask(event.getTaskId()));

		if (completeTaskParams != null && user.getLogin().equals(event.getUserId())) {
			completeTaskParams.createdTasksForCurrentUser.add(task);
		}

		assignTokens(task);
		refreshDataForNativeQuery();

		broadcastEvent(new BpmEvent(BpmEvent.Type.ASSIGN_TASK, task, user));
		broadcastEvent(REFRESH_VIEW);
	}

	private void refreshDataForNativeQuery() {
		// this call forces JBPM to flush awaiting task data
		jbpmService.getTaskService().query("SELECT task.id FROM Task task ORDER BY task.id DESC", 1, 0);
	}

	private void assignTokens(BpmTask userTask) {
		ProcessStateConfiguration stateConfiguration = getContext().getProcessDefinitionDAO()
				.getProcessStateConfiguration(userTask);

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

		broadcastEvent(new BpmEvent(BpmEvent.Type.TASK_FINISHED, completeTaskParams.task, user));
		broadcastEvent(new BpmEvent(BpmEvent.Type.SIGNAL_PROCESS, completeTaskParams.task, nvl(substitutingUser, user)));
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

	private static class StartProcessParams {
		public final ProcessDefinitionConfig config;
		public final String externalKey;
		public final String description;
		public final String keyword;
		public final String source;
		public final UserData creator;
		public ProcessInstance newProcessInstance;

		public StartProcessParams(ProcessDefinitionConfig config, String externalKey, String description,
								  String keyword, String source, UserData creator) {
			this.config = config;
			this.externalKey = externalKey;
			this.description = description;
			this.keyword = keyword;
			this.source = source;
			this.creator = creator;
		}

		public ProcessInstance createFromParams(org.drools.runtime.process.ProcessInstance jbpmProcessInstance, ProcessInstance parentProcessInstance) {
			ProcessInstance newProcessInstance = new ProcessInstance();

			newProcessInstance.setDefinition(config);
			newProcessInstance.setDefinitionName(config.getBpmDefinitionKey());
			newProcessInstance.setCreator(creator);
			newProcessInstance.setCreateDate(new Date());
			newProcessInstance.setExternalKey(externalKey);
			newProcessInstance.setInternalId(toAwfPIId(jbpmProcessInstance));
			newProcessInstance.setDescription(description);
			newProcessInstance.setKeyword(keyword);
			newProcessInstance.setStatus(ProcessStatus.NEW);
			newProcessInstance.addOwner(creator.getLogin());

			newProcessInstance.addAttribute(new ProcessInstanceSimpleAttribute("creator", creator.getLogin()));
			newProcessInstance.addAttribute(new ProcessInstanceSimpleAttribute("creatorName", creator.getRealName()));
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
			log.setUser(creator);
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

	private static class CompleteTaskParams {
		public final BpmTask task;
		public final List<BpmTask> createdTasksForCurrentUser = new ArrayList<BpmTask>();

		private CompleteTaskParams(BpmTask task) {
			this.task = task;
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

				StartProcessParams params = new StartProcessParams(
						config, null, null, null, "parent_process", parentProcessInstance.getCreator()
				);
				newProcessInstance = params.createFromParams(jbpmProcessInstance, parentProcessInstance);

				save(newProcessInstance);

				/* Inform about parent process halt */
				broadcastEvent(new BpmEvent(BpmEvent.Type.PROCESS_HALTED, parentProcessInstance, parentProcessInstance.getCreator()));
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

		broadcastEvent(new BpmEvent(BpmEvent.Type.NEW_PROCESS, newProcessInstance, newProcessInstance.getCreator()));
	}

	private void handleSubprocess(SubProcessNode node, org.drools.runtime.process.ProcessInstance processInstance) {
		subprocessParams = new SubprocessParams(node.getProcessId(), processInstance);
	}

	private void handleProcessCompleted(org.drools.runtime.process.ProcessInstance jbpmProcessInstance) {
		ProcessInstance processInstance = getByInternalId(toAwfPIId(jbpmProcessInstance));

		processInstance.setStatus(ProcessStatus.FINISHED);
		save(processInstance);

		broadcastEvent(new BpmEvent(BpmEvent.Type.END_PROCESS, processInstance, user));
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
		public UserData getOwner() {
			UserData userData = getContext().getUserDataDAO().loadUserByLogin(getAssignee());
			if (userData == null) {
				userData = new UserData();
				userData.setLogin(getAssignee());
			}
			return userData;
		}

		@Override
		public String getTaskName() {
			List<I18NText> names = task.getNames();
			return names.isEmpty() ? null : names.get(0).getText();
		}

		@Override
		public String getCreator() {
			return task.getTaskData().getCreatedBy().getId();
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
			Long definitionId = (Long)getContext().getHibernateSession().getIdentifier(getProcessInstance().getDefinition());
			return getContext().getProcessDefinitionDAO().getCachedDefinitionById(definitionId);
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

	private static long toJbpmTaskId(String taskId) {
		return Long.parseLong(taskId);
	}

	private static long toJbpmTaskId(BpmTask t) {
		return toJbpmTaskId(t.getInternalTaskId());
	}

	private static String toAwfTaskId(long taskId) {
		return String.valueOf(taskId);
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

				List<Object[]> rows = (List<Object[]>)(List)JbpmService.getInstance().createTaskQuery()
						.selectGroupId()
						.selectCount()
						.assigneeIsNull()
						.groupIds(names)
						.groupByGroupId()
						.list();

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
