package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import bitronix.tm.TransactionManagerServices;
import org.apache.commons.io.IOUtils;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.event.process.*;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalHumanTaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;
import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.rnd.pt.ext.jbpm.JbpmStepAction;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessResourceNames;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.query.TaskQuery;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.query.UserQuery;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.Strings.hasText;

public class JbpmService implements ProcessEventListener, TaskEventListener {

    protected Logger log = Logger.getLogger(JbpmService.class.getName());

    private static final int MAX_PROC_DEF_LENGTH = 1024;
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
    private JbpmRepository repository;
    private KnowledgeBase knowledgeBase;

    private static JbpmService instance;

    ThreadLocal<JbpmContext> sessionThreadLocal = new ThreadLocal<JbpmContext>();

    public static JbpmService getInstance() {
        if (instance == null) {
            synchronized (JbpmService.class) {
                if (instance == null) {
                    instance = new JbpmService();
                }
            }
        }
        return instance;
    }

    public void init() {
        initEntityManager();
    }

    public void destroy() {
        if (sessionThreadLocal.get() != null) {
            sessionThreadLocal.get().getStatefulKnowledgeSession().dispose();
            sessionThreadLocal.get().getService().dispose();
            sessionThreadLocal.remove();
        }
    }

    private void initEntityManager() {
        emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");

        UserGroupCallbackManager.getInstance().setCallback(new AwfUserCallback());
    }

    private Environment initEnvironment() {
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        return env;
    }


    private LocalTaskService initClient(Environment env) {

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf, SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);
        localTaskService.setEnvironment(env);
        localTaskService.addEventListener(this);
        return localTaskService;
    }

    public String addProcessDefinition(InputStream definitionStream)
    {
        synchronized (getInstance()) {
            String result = null;
            byte[] bytes;

            try {
                bytes = IOUtils.toByteArray(definitionStream);
                definitionStream = new ByteArrayInputStream(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (isValidResource(bytes)) {
                // add to jbpm repository
                if (getRepository() != null) {
                    result = getRepository().addResource(ProcessResourceNames.DEFINITION, definitionStream);
                }

                JbpmContext jbpmContext = getJbpmContext();

                // update session
                if (jbpmContext != null) {
                    KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
                    kbuilder.add(ResourceFactory.newByteArrayResource(bytes), ResourceType.BPMN2);
                    Collection<KnowledgePackage> packages = kbuilder.getKnowledgePackages();
                    KnowledgeBase knowledgeBaseSession = jbpmContext.getStatefulKnowledgeSession().getKnowledgeBase();
                    knowledgeBaseSession.addKnowledgePackages(packages);
                    if (knowledgeBase != null) knowledgeBase.addKnowledgePackages(packages);
                }
            }

            return result;
        }
    }

    private KnowledgeBase getKnowledgeBase() {
        if (knowledgeBase==null) {
            synchronized(getInstance()) {
                if (knowledgeBase==null) {
                    KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

                    try {
                        Thread.currentThread().getContextClassLoader().loadClass(JbpmStepAction.class.getName());
                    } catch (ClassNotFoundException e) {
                        log.warning("JbpmStepAction.class was not found");
                    }

                    if (getRepository() != null) {
                        for (byte[] resource : getValidResources()) {
                            kbuilder.add(ResourceFactory.newByteArrayResource(resource), ResourceType.BPMN2);
                        }
                    }

                    knowledgeBase = kbuilder.newKnowledgeBase();
                }
            }
        }
        return knowledgeBase;
    }

    private List<byte[]> getValidResources() {
        List<byte[]> validResources = new ArrayList<byte[]>();
        for (byte[] resource : getRepository().getAllResources("bpmn")) {
            if (isValidResource(resource)) validResources.add(resource);
        }
        return validResources;
    }

    private boolean isValidResource(byte[] resource) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(resource), ResourceType.BPMN2);
        boolean isOK = true;
        try {
            kbuilder.newKnowledgeBase();
        } catch (Exception e) {
            isOK = false;
            log.info("The following process definition contains errors and was not loaded:\n" + new String(resource).substring(0, Math.min(MAX_PROC_DEF_LENGTH, resource.length-1)) + "...");
        }
        return isOK;
    }

    private JbpmContext getJbpmContext()
    {
        JbpmContext jbpmContext = sessionThreadLocal.get();
        if(jbpmContext == null)
        {
            jbpmContext = newSession();
            sessionThreadLocal.set(jbpmContext);
        }

        return jbpmContext;
    }

    private JbpmContext newSession() {
        KnowledgeBase kbase = getKnowledgeBase();

        Environment env = initEnvironment();
        LocalTaskService client = initClient(env);

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);

        LocalHTWorkItemHandler handler = new LocalHTWorkItemHandler(client, ksession, OnErrorAction.LOG);
        handler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        new JPAWorkingMemoryDbLogger(ksession);
        ksession.addEventListener(this);

        JbpmContext jbpmContext = new JbpmContext();
        jbpmContext.setEnvironment(env);
        jbpmContext.setService(client);
        jbpmContext.setStatefulKnowledgeSession(ksession);

        return jbpmContext;
    }

    // process operations

    public Task getTask(long taskId)
    {
        JbpmContext jbpmContext = getJbpmContext();

        log.finest("JBPMService getTask: " + taskId);
        return jbpmContext.getService().getTask(taskId);
    }

    public void claimTask(long taskId, String userLogin) {
        log.finest("JBPMService claimTask: " + taskId + ", userLogin: " + userLogin);
        JbpmContext jbpmContext = getJbpmContext();
        jbpmContext.getService().claim(taskId, userLogin);
    }

    public void forwardTask(long taskId, String userLogin, String targetUserLogin) {
        log.finest("JBPMService forwardTask: " + taskId + ", userLogin: " + userLogin);
        JbpmContext jbpmContext = getJbpmContext();
        jbpmContext.getService().forward(taskId, userLogin, targetUserLogin);
    }

    public void endTask(long taskId, String userLogin, ContentData outputData, boolean startNeeded) {
        log.finest("JBPMService endTask: " + taskId + ", userLogin: " + userLogin);
        JbpmContext jbpmContext = getJbpmContext();
        if (startNeeded) {
            jbpmContext.getService().start(taskId, userLogin);
        }

        jbpmContext.getService().complete(taskId, userLogin, outputData);
    }

    public ProcessInstance getProcessInstance(long processId) {
        log.finest("JBPMService getProcessInstance: " + processId);
        JbpmContext jbpmContext = getJbpmContext();
        return jbpmContext.getStatefulKnowledgeSession().getProcessInstance(processId);
    }

    public void startProcess(String processId, Map<String,Object> parameters) {
        log.finest("Aquiare lock... " + Thread.currentThread().getId());
        log.info("JBPMService startProcess: " + processId);
        JbpmContext jbpmContext = getJbpmContext();
        jbpmContext.getStatefulKnowledgeSession().startProcess(processId, parameters);
    }

    public void abortProcessInstance(long processId) {
        log.finest("JBPMService abortProcessInstance: " + processId);
        JbpmContext jbpmContext = getJbpmContext();
        jbpmContext.getStatefulKnowledgeSession().abortProcessInstance(processId);
    }

    // queries

    public void refreshDataForNativeQuery() {
        // this call forces JBPM to flush awaiting task data
        JbpmContext jbpmContext = getJbpmContext();
        jbpmContext.getService().query("SELECT task.id FROM Task task ORDER BY task.id DESC", 1, 0);
    }

    public List<NodeInstanceLog> getProcessLog(long processId) {
        String hql = "SELECT nil FROM org.jbpm.process.audit.NodeInstanceLog nil WHERE nil.processInstanceId = " +
                processId + " ORDER BY nil.date";
        JbpmContext jbpmContext = getJbpmContext();
        return (List)jbpmContext.getService().query(hql, 10000, 0);
    }

    private TaskQuery<Task> createTaskQuery() {
        JbpmContext jbpmContext = getJbpmContext();
        return new TaskQuery<Task>(jbpmContext.getService());
    }

    private UserQuery<User> createUserQuery() {
        JbpmContext jbpmContext = getJbpmContext();
        return new UserQuery<User>(jbpmContext.getService());
    }

    public Task getTaskForAssign(String queueName, long taskId) {
        return createTaskQuery()
                .groupId(queueName)
                .taskId(taskId)
                .assigneeIsNull()
                .first();
    }

    public Task getLatestTask(long processId) {
        return createTaskQuery()
                .processInstanceId(processId)
                .completed()
                .orderByCompleteDateDesc()
                .first();
    }

    public Task getMostRecentProcessHistoryTask(long processId, String userLogin, Date completedAfter) {
        return createTaskQuery()
                .assignee(userLogin)
                .processInstanceId(processId)
                .completedAfter(completedAfter)
                .orderByCompleteDateDesc()
                .first();
    }

    public Task getPastOrActualTask(long processId, String userLogin, String taskName, Date completedAfter) {
        return createTaskQuery()
                .assignee(userLogin)
                .processInstanceId(processId)
                .completedAfter(completedAfter)
                .activityName(taskName)
                .orderByCompleteDate()
                .first();
    }

    public Task getPastOrActualTask(long processId, String taskName) {
        return createTaskQuery()
                .processInstanceId(processId)
                .activityName(taskName)
                .orderByCompleteDateDesc()
                .first();
    }

    public List<Task> getTasks(long processId, String userLogin, Collection<String> taskNames) {
        return createTaskQuery()
                .processInstanceId(processId)
                .active()
                .assignee(userLogin)
                .activityNames(taskNames)
                .list();
    }

    public List<Task> getTasks(long processId, String userLogin) {
        return createTaskQuery()
                .processInstanceId(processId)
                .assignee(userLogin)
                .active()
                .list();
    }

    public List<Task> getTasks(String userLogin, Integer offset, Integer limit) {
        return createTaskQuery()
                .assignee(userLogin)
                .active()
                .page(offset, limit)
                .list();
    }

    public List<Task> getTasks() {
        return createTaskQuery().orderByTaskIdDesc().list();
    }

    public List<Object[]> getTaskCounts(List<String> groupNames) {
        return (List<Object[]>)(List)createTaskQuery()
                .selectGroupId()
                .selectCount()
                .assigneeIsNull()
                .groupIds(groupNames)
                .groupByGroupId()
                .list();
    }

    public List<String> getAvailableUserLogins(String filter, Integer offset, Integer limit) {
        return createUserQuery()
                .selectId()
                .whereIdLike(filter != null ? '%' + filter + '%' : null)
                .page(offset, limit)
                .list();
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

    public  JbpmRepository getRepository() {
        synchronized (getInstance()) {
            if (repository == null) {
                String jbpmRepositoryDir = getThreadProcessToolContext().getSetting(JBPM_REPOSITORY_DIR);
                repository = new DefaultJbpmRepository(hasText(jbpmRepositoryDir) ? jbpmRepositoryDir : null);
            }
            return repository;
        }
    }

    private class JbpmContext
    {
        private StatefulKnowledgeSession statefulKnowledgeSession;
        private LocalTaskService service;
        private Environment environment;

        public StatefulKnowledgeSession getStatefulKnowledgeSession() {
            return statefulKnowledgeSession;
        }

        public void setStatefulKnowledgeSession(StatefulKnowledgeSession statefulKnowledgeSession) {
            this.statefulKnowledgeSession = statefulKnowledgeSession;
        }

        public LocalTaskService getService() {
            return service;
        }

        public void setService(LocalTaskService service) {
            this.service = service;
        }

        public Environment getEnvironment() {
            return environment;
        }

        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }
    }


}
