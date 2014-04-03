package pl.net.bluesoft.rnd.pt.ext.jbpm;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import junit.framework.TestCase;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.hibernate.Session;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Maciej
 */
public class MultiSessionTest extends TestCase
{
    private static boolean contextCreated = false;

    @Test
    public void testSingleSessionFail() throws NamingException, SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        SessionBean sessionBean1 = createSessionBean(1);
        SessionBean sessionBean2 = createSessionBean(1);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recipient", "john");

        ProcessInstance processInstance1 = sessionBean1.getStatefulKnowledgeSession().startProcess(
                "com.sample.rewards-basic", params);

        long processInstanceId = processInstance1.getId();


        try {
            ProcessInstance processInstance2 = sessionBean2.getStatefulKnowledgeSession().startProcess(
                    "com.sample.rewards-basic", params);

            System.out.println("Process started ... : processInstanceId = "
                    + processInstanceId);
        }
        catch(Throwable ex)
        {
            return;
        }

        fail("Test should fail");


        List<TaskSummary> list = sessionBean1.getLocalTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK");

        for(TaskSummary task: list) {
            sessionBean1.getLocalTaskService().start(task.getId(), "john");
            sessionBean1.getLocalTaskService().complete(task.getId(), "john", null);
        }

        list = sessionBean1.getLocalTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK");

        for(TaskSummary task: list) {
            sessionBean1.getLocalTaskService().start(task.getId(), "mary");
            sessionBean1.getLocalTaskService().complete(task.getId(), "mary", null);
        }
    }

    @Test
    public void testMultipleSessions() throws NamingException, SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        SessionBean sessionBean1 = createSessionBean(1);
        SessionBean sessionBean2 = createSessionBean(2);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recipient", "john");





        ProcessInstance processInstance1 = sessionBean1.getStatefulKnowledgeSession().startProcess(
                "com.sample.rewards-basic", params);

        long processInstanceId = processInstance1.getId();


        try {
            ProcessInstance processInstance2 = sessionBean2.getStatefulKnowledgeSession().startProcess(
                    "com.sample.rewards-basic", params);

            System.out.println("Process started ... : processInstanceId = "
                    + processInstanceId);
        }
        catch(Throwable ex)
        {
            fail("There shouldn't be any exception here");
        }



        List<TaskSummary> list = sessionBean1.getLocalTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK");

        for(TaskSummary task: list) {
            sessionBean1.getLocalTaskService().start(task.getId(), "john");
            sessionBean1.getLocalTaskService().complete(task.getId(), "john", null);
        }

        list = sessionBean1.getLocalTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK");

        for(TaskSummary task: list) {
            sessionBean1.getLocalTaskService().start(task.getId(), "mary");
            sessionBean1.getLocalTaskService().complete(task.getId(), "mary", null);
        }
    }

    private class SessionBean
    {
        private StatefulKnowledgeSession statefulKnowledgeSession;
        private LocalTaskService localTaskService;

        public StatefulKnowledgeSession getStatefulKnowledgeSession() {
            return statefulKnowledgeSession;
        }

        public void setStatefulKnowledgeSession(StatefulKnowledgeSession statefulKnowledgeSession) {
            this.statefulKnowledgeSession = statefulKnowledgeSession;
        }

        public LocalTaskService getLocalTaskService() {
            return localTaskService;
        }

        public void setLocalTaskService(LocalTaskService localTaskService) {
            this.localTaskService = localTaskService;
        }
    }

    private SessionBean createSessionBean(int number) throws NamingException
    {



        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("processes/rewards/rewards-basic.bpmn20"), ResourceType.BPMN2);

        TaskService taskService = new org.jbpm.task.service.TaskService(emf, SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);
        localTaskService.setEnvironment(env);

        User john = new User();
        john.setId("john");
        User mary = new User();
        mary.setId("mary");
        User administrator = new User();
        administrator.setId("Administrator");

        Map<String, User> stringUserMap = new HashMap<String, User>();
        stringUserMap.put("john", john);
        stringUserMap.put("mary", mary);
        stringUserMap.put("Administrator", administrator);

        taskService.addUsersAndGroups(stringUserMap, new HashMap<String, Group>());


        KnowledgeBase knowledgeBase = kbuilder.newKnowledgeBase();

        StatefulKnowledgeSession ksession1;

        try {
            ksession1 = JPAKnowledgeService.loadStatefulKnowledgeSession(number, knowledgeBase, null, env);
        }
        catch(Throwable ex)
        {
            ksession1 = JPAKnowledgeService.newStatefulKnowledgeSession(knowledgeBase, null, env);
        }

        LocalHTWorkItemHandler handler1 = new LocalHTWorkItemHandler(localTaskService, ksession1, OnErrorAction.LOG);
        handler1.connect();
        ksession1.getWorkItemManager().registerWorkItemHandler("Human Task", handler1);

        SessionBean sessionBean = new SessionBean();
        sessionBean.setLocalTaskService(localTaskService);
        sessionBean.setStatefulKnowledgeSession(ksession1);

        return sessionBean;
    }

    @Override
    protected void setUp() throws Exception
    {
        if(!contextCreated)
        {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,  "org.apache.naming");

            contextCreated = true;

            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");

            ic.createSubcontext("java:comp");
            ic.createSubcontext("java:comp/env");
            ic.createSubcontext("java:comp/env/jdbc");

            PoolingDataSource ds1 = new PoolingDataSource();
            ds1.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
            ds1.setUniqueName("aperte-workflow-ds");
            ds1.setMaxPoolSize(5);
            ds1.setAllowLocalTransactions(true);
            ds1.setApplyTransactionTimeout(false);
            ds1.getDriverProperties().setProperty("driverClassName", "org.postgresql.Driver");
            ds1.getDriverProperties().setProperty("url", "jdbc:postgresql://localhost:5432/jbpm6");
            ds1.getDriverProperties().setProperty("user", "dpd");
            ds1.getDriverProperties().setProperty("password", "dpd");
            ds1.init();
            ic.bind("aperte-workflow-ds", ds1);

            ic.bind("UserTransaction", TransactionManagerServices.getTransactionManager());
            ic.bind("java:comp/UserTransaction", TransactionManagerServices.getTransactionManager());
            ic.bind("java:/comp/UserTransaction", TransactionManagerServices.getTransactionManager());
        }

        System.setProperty("org.aperteworkflow.datasource", "aperte-workflow-ds");

        new InitialContext().lookup("aperte-workflow-ds");
        super.setUp();
    }


}
