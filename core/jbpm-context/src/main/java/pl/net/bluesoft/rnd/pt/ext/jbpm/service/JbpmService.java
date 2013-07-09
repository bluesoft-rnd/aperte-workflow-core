package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import bitronix.tm.TransactionManagerServices;
import org.apache.commons.io.IOUtils;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.*;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;
import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessResourceNames;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.query.TaskQuery;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.query.UserQuery;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.Strings.hasText;

public class JbpmService implements ProcessEventListener, TaskEventListener {
	private static final IProcessToolSettings KSESSION_ID = new IProcessToolSettings() {
		@Override
		public String toString() {
			return "ksession.id";
		}
	};
	private static final IProcessToolSettings JBPM_REPOSITORY_DIR = new IProcessToolSettings() {
		@Override
		public String toString() {
			return "jbpm.repository.dir";
		}
	};

	private EntityManagerFactory emf;
	private Environment env;
	private org.jbpm.task.TaskService client;
	private StatefulKnowledgeSession ksession;

	private JbpmRepository repository;

	private static JbpmService instance;

	public static synchronized JbpmService getInstance() {
		if (instance == null) {
			instance = new JbpmService();
		}
		return instance;
	}

	public void init() {
		initEntityManager();
		initEnvironment();
		initClient();
	}

	public void destroy() {
		if (ksession != null) {
			ksession.dispose();
		}
	}

	private void initEntityManager() {
		emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
	}

	private void initEnvironment() {
		env = EnvironmentFactory.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
	}

	private void initClient() {
		org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
		UserGroupCallbackManager.getInstance().setCallback(new AwfUserCallback());

		LocalTaskService localTaskService = new LocalTaskService(taskService);
		localTaskService.setEnvironment(env);
		localTaskService.addEventListener(this);
		client = localTaskService;
	}

	public String addProcessDefinition(InputStream definitionStream) {
		byte[] bytes;

		try {
			bytes = IOUtils.toByteArray(definitionStream);
			definitionStream = new ByteArrayInputStream(bytes);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// add to jbpm repository

		String result = null;

		if (getRepository() != null) {
			result = getRepository().addResource(ProcessResourceNames.DEFINITION, definitionStream);
		}

		// update session

		if (ksession != null) {
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			kbuilder.add(ResourceFactory.newByteArrayResource(bytes), ResourceType.BPMN2);
			ksession.getKnowledgeBase().addKnowledgePackages(kbuilder.getKnowledgePackages());
		}

		return result;
	}

	private KnowledgeBase getKnowledgeBase() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		if (getRepository() != null) {
			for (byte[] resource : getRepository().getAllResources("bpmn")) {
				kbuilder.add(ResourceFactory.newByteArrayResource(resource), ResourceType.BPMN2);
			}
		}

		return kbuilder.newKnowledgeBase();
	}

	private void loadSession(int sessionId) {
		KnowledgeBase kbase = getKnowledgeBase();

		if (sessionId == -1) {
			ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
		}
		else {
			ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
			ksession.signalEvent("Trigger", null); // may be necessary for pushing processes after server restart
		}

		LocalHTWorkItemHandler handler = new LocalHTWorkItemHandler(client, ksession, OnErrorAction.LOG);
		handler.connect();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		new JPAWorkingMemoryDbLogger(ksession);

		ksession.addEventListener(this);
	}

	public synchronized StatefulKnowledgeSession getSession() {
		if (ksession == null) {
			String ksessionIdStr = getThreadProcessToolContext().getSetting(KSESSION_ID);
			int ksessionId = hasText(ksessionIdStr) ? Integer.parseInt(ksessionIdStr) : -1;

			loadSession(ksessionId);

			if (ksessionId <= 0) {
				getThreadProcessToolContext().setSetting(KSESSION_ID, String.valueOf(ksession.getId()));
			}
		}
		return ksession;
	}

	public org.jbpm.task.TaskService getTaskService() {
		getSession(); // ensure session is created before task service
		return client;
	}

	public TaskQuery<Task> createTaskQuery() {
		return new TaskQuery<Task>(client);
	}

	public UserQuery<User> createUserQuery() {
		return new UserQuery<User>(client);
	}

	public Query createNativeQuery(String sql) {
		return emf.createEntityManager().createNativeQuery(sql);
	}

	@Override
	public void beforeProcessStarted(ProcessStartedEvent event) {
		getProcessEventListener().beforeProcessStarted(event);
	}

	@Override
	public void afterProcessStarted(ProcessStartedEvent event) {
		getProcessEventListener().afterProcessStarted(event);
	}

	@Override
	public void beforeProcessCompleted(ProcessCompletedEvent event) {
		getProcessEventListener().beforeProcessCompleted(event);
	}

	@Override
	public void afterProcessCompleted(ProcessCompletedEvent event) {
		getProcessEventListener().afterProcessCompleted(event);
	}

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		getProcessEventListener().beforeNodeTriggered(event);
	}

	@Override
	public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
		getProcessEventListener().afterNodeTriggered(event);
	}

	@Override
	public void beforeNodeLeft(ProcessNodeLeftEvent event) {
		getProcessEventListener().beforeNodeLeft(event);
	}

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
		getProcessEventListener().afterNodeLeft(event);
	}

	@Override
	public void beforeVariableChanged(ProcessVariableChangedEvent event) {
		getProcessEventListener().beforeVariableChanged(event);
	}

	@Override
	public void afterVariableChanged(ProcessVariableChangedEvent event) {
		getProcessEventListener().afterVariableChanged(event);
	}

	@Override
	public void taskCreated(TaskUserEvent event) {
		getTaskEventListener().taskCreated(event);
	}

	@Override
	public void taskClaimed(TaskUserEvent event) {
		getTaskEventListener().taskClaimed(event);
	}

	@Override
	public void taskStarted(TaskUserEvent event) {
		getTaskEventListener().taskStarted(event);
	}

	@Override
	public void taskStopped(TaskUserEvent event) {
		getTaskEventListener().taskStopped(event);
	}

	@Override
	public void taskReleased(TaskUserEvent event) {
		getTaskEventListener().taskReleased(event);
	}

	@Override
	public void taskCompleted(TaskUserEvent event) {
		getTaskEventListener().taskCompleted(event);
	}

	@Override
	public void taskFailed(TaskUserEvent event) {
		getTaskEventListener().taskFailed(event);
	}

	@Override
	public void taskSkipped(TaskUserEvent event) {
		getTaskEventListener().taskSkipped(event);
	}

	@Override
	public void taskForwarded(TaskUserEvent event) {
		getTaskEventListener().taskForwarded(event);
	}

	private static final ProcessEventListener NULL_PROCESS_LISTENER = new ProcessEventListener() {
		@Override
		public void beforeProcessStarted(ProcessStartedEvent processStartedEvent) {
		}

		@Override
		public void afterProcessStarted(ProcessStartedEvent processStartedEvent) {
		}

		@Override
		public void beforeProcessCompleted(ProcessCompletedEvent processCompletedEvent) {
		}

		@Override
		public void afterProcessCompleted(ProcessCompletedEvent processCompletedEvent) {
		}

		@Override
		public void beforeNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {
		}

		@Override
		public void afterNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {
		}

		@Override
		public void beforeNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {
		}

		@Override
		public void afterNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {
		}

		@Override
		public void beforeVariableChanged(ProcessVariableChangedEvent event) {
		}

		@Override
		public void afterVariableChanged(ProcessVariableChangedEvent event) {
		}
	};

	private static final TaskEventListener NULL_TASK_LISTENER = new TaskEventListener() {
		@Override
		public void taskCreated(TaskUserEvent event) {
		}

		@Override
		public void taskClaimed(TaskUserEvent event) {
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
	};

	private static final ThreadLocal<ProcessEventListener> processListenerTL = new ThreadLocal<ProcessEventListener>();
	private static final ThreadLocal<TaskEventListener> taskListenerTL = new ThreadLocal<TaskEventListener>();

	public static void setProcessEventListener(ProcessEventListener eventListener) {
		processListenerTL.set(eventListener);
	}

	public static void setTaskEventListener(TaskEventListener eventListener) {
		taskListenerTL.set(eventListener);
	}

	private static ProcessEventListener getProcessEventListener() {
		ProcessEventListener eventListener = processListenerTL.get();
		return eventListener != null ? eventListener : NULL_PROCESS_LISTENER;
	}

	private static TaskEventListener getTaskEventListener() {
		TaskEventListener eventListener = taskListenerTL.get();
		return eventListener != null ? eventListener : NULL_TASK_LISTENER;
	}

	public synchronized JbpmRepository getRepository() {
		if (repository == null) {
			String jbpmRepositoryDir = getThreadProcessToolContext().getSetting(JBPM_REPOSITORY_DIR);
			repository = new DefaultJbpmRepository(hasText(jbpmRepositoryDir) ? jbpmRepositoryDir : null);
		}
		return repository;
	}
}
