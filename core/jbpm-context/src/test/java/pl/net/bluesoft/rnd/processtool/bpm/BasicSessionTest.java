package pl.net.bluesoft.rnd.processtool.bpm;

import org.hibernate.Query;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.query.AbstractQuery;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextFactoryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextImpl;

import java.io.InputStream;
import java.util.*;

import static pl.net.bluesoft.util.lang.MapUtil.collectionToMap;

/**
 * @author tlipski@bluesoft.net.pl
 */

public class BasicSessionTest {


	ProcessToolContextFactory processToolContextFactory = new ProcessToolContextFactoryImpl(new ProcessToolRegistryImpl());

	@Before
	public void setup() {
	}

	@After
	public void finish() {
	}

	@Test
	public void testInit() {
		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ctx.getProcessToolSessionFactory().createSession(getTestUser(), new LinkedList());
			}
		});

	}

	@Test
	public void testLoadProcess() {

		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				InputStream res = getClass().getResourceAsStream("/testprc.jpdl.xml");
				Assert.assertNotNull(res);
				String s = ((ProcessToolContextImpl)ctx).getProcessEngine().getRepositoryService().createDeployment()
						.addResourceFromInputStream("/testprc.jpdl.xml", res).deploy();
				Assert.assertNotNull(s);
			}
		});

	}

	@Test
	public void testLoadProcess2() {
		loadProcess("/testprc2.jpdl.xml");
	}

	@Test
	public void testLoadProcess3() {
		loadProcess("/widgetsprc.jpdl.xml");
	}

	private void loadProcess(final String path) {
		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				InputStream res = getClass().getResourceAsStream(path);
				Assert.assertNotNull(res);
				String s = ((ProcessToolContextImpl)ctx).getProcessEngine().getRepositoryService().createDeployment()
						.addResourceFromInputStream(path, res).deploy();
				Assert.assertNotNull(s);
			}
		});
	}

	@Test
	public void testStartProcess() {
		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessToolBpmSession toolBpmSession = ctx.getProcessToolSessionFactory().createSession(getTestUser(), new LinkedList());
				ProcessInstance pi = new ProcessInstance();
				pi.setDefinitionName("testprc");
				pi.setExternalKey("test/1" + System.currentTimeMillis());
				ProcessDefinitionConfig processDefinitionConfig = ctx.getProcessDefinitionDAO().getActiveConfigurations().iterator().next();
				ProcessInstance s = toolBpmSession.createProcessInstance(processDefinitionConfig,
				                                                         "test/1" + System.currentTimeMillis(),
				                                                         ctx,null,null, "test");
				System.out.println("ss: " + s.getInternalId() + ", " + s.getId());
			}
		});


	}

	@Test
	public void testGetUserTasks() {
		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessToolBpmSession toolBpmSession = ctx.getProcessToolSessionFactory().createSession(getTestUser(), new LinkedList());
				Collection<ProcessInstance> userTasks = toolBpmSession.getUserProcesses(0, 100, ctx);
				for (ProcessInstance pi : userTasks) {
					System.out.println("pi:" + pi.getInternalId());
				}
				Assert.assertFalse(userTasks.isEmpty());
			}
		});
	}

	@Test
	public void testDAO() {
		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessInstanceDAO processInstanceDao = ctx.getProcessInstanceDAO();
				ProcessInstance instance = processInstanceDao.getProcessInstance(-1);
				Assert.assertNull(instance);

				ProcessInstance pi = new ProcessInstance();
				pi.setDefinitionName("test");
				pi.setInternalId("666");
				long id = processInstanceDao.saveProcessInstance(pi);
				System.out.println("id: " + id);
				instance = processInstanceDao.getProcessInstance(id);
				Assert.assertNotNull(instance);

				processInstanceDao.deleteProcessInstance(instance);
			}});

	}

	@Test
	public void testAvailableDefinitions() {
		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessToolBpmSession toolBpmSession = ctx.getProcessToolSessionFactory().createSession(getTestUser(), new LinkedList());
				Assert.assertFalse(toolBpmSession.getAvailableConfigurations(ctx).isEmpty());
			}
		});

	}

	@Test
	public void testGroupTasks() {

		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessToolBpmSession toolBpmSession = ctx.getProcessToolSessionFactory().createSession(getTestUser(), Arrays.asList("Power User"));
				Collection<ProcessQueue> queues = toolBpmSession.getUserAvailableQueues(ctx);
				Map<String, ProcessQueue> qMap = collectionToMap(queues, "name");
				ProcessQueue q = qMap.get("sales");
				long salesCnt = q != null ? q.getProcessCount() : 0;

				Assert.assertFalse(queues.isEmpty());

				ProcessInstance pi = new ProcessInstance();
				pi.setDefinitionName("testprc2");
				Collection<ProcessDefinitionConfig> cfgs = ctx.getProcessDefinitionDAO().getActiveConfigurations();
				ProcessDefinitionConfig processDefinitionConfig = null;
				for (ProcessDefinitionConfig c : cfgs) {
					if (c.getProcessName().equals("testprc2")) {
						processDefinitionConfig = c;
					}
				}
				Assert.assertNotNull(processDefinitionConfig);
				ProcessInstance s = toolBpmSession.createProcessInstance(processDefinitionConfig,
				                                                         "2223" + System.currentTimeMillis(), ctx, null, null, "test");

				Assert.assertFalse(toolBpmSession.getUserAvailableQueues(ctx).isEmpty());

//				org.jbpm.api.ProcessInstance processInstance = ctx.getProcessEngine().getExecutionService().findProcessInstanceById(s.getInternalId());
				((ProcessToolContextImpl)ctx).getProcessEngine().getExecutionService().signalExecutionById(s.getInternalId());
				Command<List<Task>> cmd = getFindTaskCommand();
				List<Task> taskList = ((ProcessToolContextImpl)ctx).getProcessEngine().execute(cmd);
				Assert.assertFalse(taskList.isEmpty());
				String taskId = null;
				for (Task t : taskList) {
					String id = t.getExecutionId();
					System.out.println("id: " + id + " vs " + s.getInternalId());
					if (id.equals(s.getInternalId())) {
						taskId = t.getId();
						break;
					}
				}

				Assert.assertNotNull(taskId);


				queues = toolBpmSession.getUserAvailableQueues(ctx);
				qMap = collectionToMap(queues, "name");
				Assert.assertEquals(salesCnt + 1, qMap.get("sales").getProcessCount());

				((ProcessToolContextImpl)ctx).getProcessEngine().getTaskService().assignTask(taskId, "tlipski");
				Task task = ((ProcessToolContextImpl)ctx).getProcessEngine().getTaskService().getTask(taskId);
				Assert.assertNotNull(task);
				Assert.assertNotNull(task.getAssignee());
				Assert.assertEquals("tlipski", task.getAssignee());

				taskList = ((ProcessToolContextImpl)ctx).getProcessEngine().execute(cmd);
				taskId = null;
				for (Task t : taskList) {
					String id = t.getExecutionId();
					System.out.println("id: " + id + " vs " + s.getInternalId());
					if (id.equals(s.getInternalId())) {
						taskId = t.getId();
						break;
					}
				}
				Assert.assertNull(taskId);

				queues = toolBpmSession.getUserAvailableQueues(ctx);
				qMap = collectionToMap(queues, "name");
				Assert.assertEquals(salesCnt, qMap.get("sales").getProcessCount());

//				return null;
			}
		});

	}

	@Test
	public void testAssignTask() {

		processToolContextFactory.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessToolBpmSession toolBpmSession = ctx.getProcessToolSessionFactory().createSession(getTestUser(), Arrays.asList("Power User"));
				Collection<ProcessQueue> queues = toolBpmSession.getUserAvailableQueues(ctx);
				Map<String, ProcessQueue> qMap = collectionToMap(queues, "name");
				ProcessQueue q = qMap.get("sales");
				long salesCnt = q != null ? q.getProcessCount() : 0;

				Assert.assertFalse(queues.isEmpty());

				ProcessInstance pi = new ProcessInstance();
				pi.setDefinitionName("testprc2");
				Collection<ProcessDefinitionConfig> cfgs = ctx.getProcessDefinitionDAO().getActiveConfigurations();
				ProcessDefinitionConfig processDefinitionConfig = null;
				for (ProcessDefinitionConfig c : cfgs) {
					if (c.getProcessName().equals("testprc2")) {
						processDefinitionConfig = c;
					}
				}
				Assert.assertNotNull(processDefinitionConfig);
				ProcessInstance s = toolBpmSession.createProcessInstance(processDefinitionConfig,
				                                                         "2223" + System.currentTimeMillis(),
				                                                         ctx, null, null, "test");

				((ProcessToolContextImpl)ctx).getProcessEngine().getExecutionService().signalExecutionById(s.getInternalId());

				queues = toolBpmSession.getUserAvailableQueues(ctx);
				qMap = collectionToMap(queues, "name");
				Assert.assertEquals(salesCnt + 1, qMap.get("sales").getProcessCount());

				toolBpmSession.assignTaskFromQueue(qMap.get("sales"), ctx);
				queues = toolBpmSession.getUserAvailableQueues(ctx);
				qMap = collectionToMap(queues, "name");
				Assert.assertEquals(salesCnt, qMap.get("sales").getProcessCount());

//				return/**/ null;
			}
		});

	}


	private Command<List<Task>> getFindTaskCommand() {
		Command<List<Task>> cmd = new Command<List<Task>>() {

			@Override
			public List<Task> execute(Environment environment) throws Exception {
				return (List<Task>) new AbstractQuery() {

					@Override
					protected void applyParameters(Query query) {
						query.setString("groupId", "sales");
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select task ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");

						// participations
						hql.append(", ");
						hql.append(ParticipationImpl.class.getName());
						hql.append(" as participant ");

						hql.append("where participant.task=task ");
						hql.append("and participant.type = 'candidate' ");
						hql.append("and participant.groupId = :groupId ");
						hql.append("and task.assignee is null ");

						return hql.toString();

					}
				}.execute(environment);


			}
		};
		return cmd;
	}

	private UserData getTestUser() {
		UserData u = new UserData();
		u.setLogin("tlipski");
		u.setRealName("Tomek Lipski");
		u.setEmail("tlipski@bluesoft.net.pl");
		return u;
	}


}
