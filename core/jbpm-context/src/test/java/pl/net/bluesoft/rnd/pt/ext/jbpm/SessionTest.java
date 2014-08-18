package pl.net.bluesoft.rnd.pt.ext.jbpm;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import junit.framework.TestCase;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.bpm.impl.AbstractProcessToolSession;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.processtool.plugins.deployment.ProcessDeployer;
import pl.net.bluesoft.rnd.processtool.token.IAccessTokenFactory;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.processtool.token.TokenWrapper;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;
import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.util.lang.Lang;
import pl.net.bluesoft.util.lang.cquery.func.F;
import pl.net.bluesoft.util.lang.cquery.func.P;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2013-06-18
 * Time: 11:05
 */
public class SessionTest extends TestCase {
   protected static ProcessToolRegistryImpl registry;
	private static boolean isSetUp;
	private ProcessToolContext ctx;

	private void performSubprocessTests() {
		checkTasksInVQs(new Object[][]{
				{ "peter", new VQElem[][]{ { }, { }, { } } },
		});

		ProcessInstance processInstance = startProcess("peter", "subprocess_assignee");

		checkASFlow(processInstance, "peter");

		ProcessInstance processInstance2 = startProcess("peter", "subprocess_queue");

		checkQSFlow(processInstance2, "peter", new VQElem[]{}, onUser("peter", "AS_Task1"));
	}

	private void checkQSFlow(ProcessInstance processInstance, String user, VQElem[] postFinishTasks, VQElem... alreadyCompletedTasks) {
		checkTasksInVQs(new Object[][]{
				{ user, new VQElem[][]{ { }, {onGroup("QUEUE_NO1", "QS_Task1") }, alreadyCompletedTasks } },
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "QS_Task1", null, "QUEUE_NO1" }
		});

		assignFromQueue(processInstance, user, "QS_Task1", "QUEUE_NO1");


		checkTasksInVQs(new Object[][]{
				{ user, new VQElem[][]{ { onUser(user, "QS_Task1") }, {onUser(user, "QS_Task1") }, alreadyCompletedTasks } },
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "QS_Task1", user, null }
		});

		performAction(processInstance, user, "QS_Task1", "end");


		checkTasksInVQs(new Object[][]{
				{ user, new VQElem[][]{ postFinishTasks, postFinishTasks, join(alreadyCompletedTasks, onUser(user, "QS_Task1"))  } },
		});
		checkTasksByProcess(processInstance, new String[][]{});

		checkFinished(processInstance);
	}

	private void checkASFlow(ProcessInstance processInstance, String user, VQElem... alreadyCompletedTasks) {
		checkTasksInVQs(new Object[][]{
				{ user, new VQElem[][]{ { onUser(user, "AS_Task1") }, {onUser(user, "AS_Task1") }, alreadyCompletedTasks } },
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "AS_Task1", user, null }
		});

		performAction(processInstance, user, "AS_Task1", "end");

        checkTasksInVQs(new Object[][]{
                { user, new VQElem[][]{ { }, { }, join(alreadyCompletedTasks, onUser(user, "AS_Task1")) } },
        });
        checkTasksByProcess(processInstance, new String[][]{});
        checkFinished(processInstance);
	}

	private VQElem[] join(VQElem[] elems, VQElem elem) {
		ArrayList<VQElem> list = new ArrayList<VQElem>(Arrays.asList(elems));
		list.add(elem);
		return list.toArray(new VQElem[list.size()]);
	}

	private void performSubprocessUsageTest() {
		checkTasksInVQs(new Object[][]{
				{ "jack", new VQElem[][]{ { }, { }, { } } },
		});

		ProcessInstance parentProcessInstance = startProcess("jack", "subprocess_usage");

		checkNotFinished(parentProcessInstance);

		ProcessInstance subProcessInstance = checkIsInSubprocess(parentProcessInstance, "subprocess_queue");
		checkQSFlow(subProcessInstance, "jack", new VQElem[]{ onUser("jack", "AS_Task1") });

		checkNotFinished(parentProcessInstance);

		ProcessInstance subProcessInstance2 = checkIsInSubprocess(parentProcessInstance, "subprocess_assignee");
		checkASFlow(subProcessInstance2, "jack", onUser("jack", "QS_Task1"));

		checkFinished(parentProcessInstance);
	}

	private ProcessInstance checkIsInSubprocess(final ProcessInstance processInstance, final String bpmDefinitionKey) {
        final ProcessInstance[] subprocess = new ProcessInstance[1];
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                ProcessInstance processInstanceLocal =  ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstance.getInternalId());

		assertNotNull(processInstanceLocal.getChildren());

		 subprocess[0] = from(processInstanceLocal.getChildren()).first(new P<ProcessInstance>() {
			@Override
			public boolean invoke(ProcessInstance x) {
				return x.isProcessRunning() && x.getDefinition().getBpmDefinitionKey().equals(bpmDefinitionKey);
			}
		});
            }
        });

		return subprocess[0];
	}

	//TODO weryfikacja czy wszystkie oczekiwane zdarzenia zostaly wywolane
	private void performTest1() {
//		Date processStartTime = DateUtil.addSeconds(new Date(), -1);

		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{ { }, { }, { } } },
				{ "mary", new VQElem[][]{ { }, { }, { } } }
		});

		ProcessInstance processInstance = startProcess("john", "Complaint");

		checkAnyTaskExists();
		checkUserHasAccessToQueues("john", "QUEUE_NO1");

		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{ { onUser("john", "Complain") }, { onUser("john", "Complain")}, { } } },
				{ "mary", new VQElem[][]{ { },  { }, { } } }
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "Complain", "john", null }
		});
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 1 }, { "mary", 0 }
		});

		performAction(processInstance, "john", "Complain", "fork");

		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{
						{ },
						{ onUser("mary", "Recomendation_1"),onGroup("QUEUE_NO1", "Recomendation_2") },
						{ onUser("john", "Complain") }
				} },
				{ "mary", new VQElem[][]{
						{onUser("mary", "Recomendation_1") },
						{ },
						{ }
				} }
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "Recomendation_1", "mary", null },
				{ "Recomendation_2", null, "QUEUE_NO1" }
		});
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 0 }, { "mary", 1 }
		});


		performAction(processInstance, "mary", "Recomendation_1", "join");


		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{
						{ },
						{ onGroup("QUEUE_NO1", "Recomendation_2") },
						{ onUser("john", "Complain") }
				} },
				{ "mary", new VQElem[][]{
						{ },
						{ },
						{ }
				} }
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "Recomendation_2", null, "QUEUE_NO1" }
		});
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 0 }, { "mary", 0 }
		});

		assignFromQueue(processInstance, "john", "Recomendation_2", "QUEUE_NO1");


		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{
						{ onUser("john", "Recomendation_2") },
						{ onUser("john", "Recomendation_2")},
						{ onUser("john", "Complain") }
				} },
				{ "mary", new VQElem[][]{
						{ },
						{ },
						{ }
				} }
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "Recomendation_2", "john", null }
		});
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 1 }, { "mary", 0 }
		});

		performAction(processInstance, "john", "Recomendation_2", "join");


		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{
						{ onUser("john", "Make_Decision") },
						{ onUser("john", "Make_Decision")},
						{ onUser("john", "Complain"), onUser("john", "Recomendation_2") }
				} },
				{ "mary", new VQElem[][]{
						{ },
						{ },
						{ }
				} }
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "Make_Decision", "john", null }
		});
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 1 }, { "mary", 0 }
		});

		performAction(processInstance, "john", "Make_Decision", "Accept2");


		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{
						{ },
						{ onUser("mary", "Accept") },
						{ onUser("john", "Complain"), onUser("john", "Recomendation_2"), onUser("john", "Make_Decision") }
				} },
				{ "mary", new VQElem[][]{
						{ onUser("mary", "Accept")},
						{ },
						{ }
				} }
		});
		checkTasksByProcess(processInstance, new String[][]{
				{ "Accept", "mary", null }
		});
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 0 }, { "mary", 1 }
		});

		performAction(processInstance, "mary", "Accept", "end");


		checkTasksInVQs(new Object[][]{
				{ "john", new VQElem[][]{
						{ },
						{ },
						{ onUser("john", "Complain"), onUser("john", "Recomendation_2"), onUser("john", "Make_Decision") }
				} },
				{ "mary", new VQElem[][]{
						{ },
						{ },
						{ }
				} }
		});
		checkTasksByProcess(processInstance, new String[][]{ });
		checkTasksByProcessByUser(processInstance, new Object[][]{
				{ "john", 0 }, { "mary", 0 }
		});

		checkFinished(processInstance);
	}

	private void checkFinished(final ProcessInstance processInstance) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                ProcessInstance processInstanceLocal =  ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstance.getInternalId());
                assertFalse(processInstanceLocal.isProcessRunning());
            }
        });

	}

	private void checkNotFinished(final ProcessInstance processInstance) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                ProcessInstance processInstanceLocal =  ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstance.getInternalId());
                assertTrue(processInstanceLocal.isProcessRunning());
            }
        });
	}



	private VQElem onUser(String assignee, String taskName) {
		return new VQElem(taskName, assignee, null);
	}

	private VQElem onGroup(String group, String taskName) {
		return new VQElem(taskName, null, group);
	}

	private void checkTasksInVQs(final Object[][] rows) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {


		for (Object[] row : rows) {
			String user = (String)row[0];
			VQElem[][] elems = (VQElem[][])row[1];

			if (elems.length != 3) {
				throw new RuntimeException("Expected 3 elems instead of " + elems.length);
			}

			ProcessToolBpmSession session = createSession(user);

//			assertEquals(elems[0].length, session.getTasksCount(user, QueueType.MY_TASKS));
//			assertEquals(elems[1].length, session.getTasksCount(user, QueueType.OWN_IN_PROGRESS));
//			assertEquals(elems[2].length, session.getTasksCount(user, QueueType.OWN_FINISHED));

			assertEquals(elems[0].length, session.getFilteredTasksCount(createFilter(user, QueueType.MY_TASKS)));
			assertEquals(elems[1].length, session.getFilteredTasksCount(createFilter(user, QueueType.OWN_IN_PROGRESS)));
			assertEquals(elems[2].length, session.getFilteredTasksCount(createFilter(user, QueueType.OWN_FINISHED)));

			checkVQ(elems[0], session.findFilteredTasks(createFilter(user, QueueType.MY_TASKS)));
			checkVQ(elems[1], session.findFilteredTasks(createFilter(user, QueueType.OWN_IN_PROGRESS)));
			checkVQ(elems[2], session.findFilteredTasks(createFilter(user, QueueType.OWN_FINISHED)));
		}
            }
        });
	}

	private void checkVQ(VQElem[] elem, List<BpmTask> filteredTasks) {
		assertEquals(elem.length, filteredTasks.size());

		for (final VQElem vqElem : elem) {
			assertTrue(from(filteredTasks).any(new P<BpmTask>() {
				@Override
				public boolean invoke(BpmTask bpmTask) {
					return Lang.equals(vqElem.taskName, bpmTask.getTaskName()) &&
							Lang.equals(vqElem.assignee, bpmTask.getAssignee()) &&
							Lang.equals(vqElem.group, bpmTask.getGroupId());
				}
			}));
		}
	}

	private ProcessInstanceFilter createFilter(String userLogin, QueueType... queueTypes) {
		ProcessInstanceFilter filter = new ProcessInstanceFilter();
		filter.setFilterOwnerLogin(userLogin);
		filter.setQueueTypes(new HashSet<QueueType>(Arrays.asList(queueTypes)));
		return filter;
	}

	private void checkTasksByProcessByUser(final ProcessInstance processInstance, final Object[][] rows) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
		for (Object[] row : rows) {
			String user = (String)row[0];
			int count = (Integer)row[1];

			List<BpmTask> list = createSession(user).findUserTasks(processInstance);

			assertEquals(count, list.size());

			List<BpmTask> list2 = createSession(user).findUserTasks(0, Integer.MAX_VALUE);

			List<String> ids1 = from(list).select(new F<BpmTask, String>() {
				@Override
				public String invoke(BpmTask x) {
					return x.getInternalTaskId();
				}
			}).toList();

			List<String> ids2 = from(list2).select(new F<BpmTask, String>() {
				@Override
				public String invoke(BpmTask x) {
					return x.getInternalTaskId();
				}
			}).toList();

			assertTrue(from(ids2).containsAll((Iterable<String>)ids1));
		}
            }
        });
	}

	private void checkUserHasAccessToQueues(final String user, final String... queueNames) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                List<ProcessQueue> queues = createSession(user).getUserAvailableQueues();

                for (String queueName : queueNames) {
                    ProcessQueue queue = byQName(queues).get(queueName);
                    assertNotNull(queue);
                }
            }
        });



	}

	private void checkAnyTaskExists() {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
		List<BpmTask> allTasks = createSession("admin").getAllTasks();

		assertTrue(!allTasks.isEmpty());
            }
        });
	}

	private void assignFromQueue(final ProcessInstance processInstance, final String user, final String state, final String queueName) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {

                ProcessToolBpmSession session = createSession(user);

                List<BpmTask> tasks = session.findProcessTasks(processInstance, null, Collections.singleton(state));

                assertEquals(1, tasks.size());

                BpmTask task = tasks.get(0);

                List<ProcessQueue> queues = session.getUserAvailableQueues();

                ProcessQueue queue = byQName(queues).get(queueName);

                assertNotNull(queue);
                assertTrue(queue.getProcessCount() > 0);

                task = session.assignTaskFromQueue(queue.getName(), task.getInternalTaskId());

                assertNotNull(task);
                assertEquals(user, task.getAssignee());
                assertNull(task.getGroupId());
            }
        });
	}

	private void performAction( final ProcessInstance processInstance, final String user, final String state, final String actionName) {

        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                SessionTest.this.ctx = ctx;

				ProcessToolBpmSession session = createSession(user);

				ProcessInstance processInstanceLocal = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstance.getInternalId());

				List<BpmTask> list = session.findProcessTasks(processInstanceLocal, user, Collections.singleton(state));

				BpmTask task = byName(list).get(state);

				assertNotNull(task);

				ProcessStateAction action = getAction(processInstanceLocal, state, actionName);

				assertNotNull(action);

				print("--------> " + task);

				List<BpmTask> tasks = session.performAction(action, task);

				print("--------> AFTER " + task);

				assertTrue(tasks != null);
				assertTrue(user.equals(task.getAssignee()) || task.isFinished());
            }
        });
	}

	private Map<String, BpmTask> byName(List<BpmTask> list) {
		Map<String, BpmTask> result = new HashMap<String, BpmTask>();

		for (BpmTask bpmTask : list) {
			result.put(bpmTask.getTaskName(), bpmTask);
		}
		return result;
	}

	private Map<String, ProcessQueue> byQName(List<ProcessQueue> list) {
		Map<String, ProcessQueue> result = new HashMap<String, ProcessQueue>();

		for (ProcessQueue queue : list) {
			result.put(queue.getName(), queue);
		}
		return result;
	}


	private ProcessStateAction getAction(ProcessInstance processInstance, String stateName, String actionName) {
		ProcessStateConfiguration state = processInstance.getDefinition().getProcessStateConfigurationByName(stateName);

		assertNotNull(state);

		ProcessStateAction action = state.getProcessStateActionByName(actionName);

		assertNotNull(action);

		return action;
	}

	private void checkTasksByProcess(final ProcessInstance processInstance, final String[][] args) {

        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
		ProcessToolBpmSession session = createSession("admin");
        ProcessInstance processInstanceLocal = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstance.getInternalId());

		List<BpmTask> list = session.findProcessTasks(processInstanceLocal);

		assertEquals(args.length, list.size());

		List<BpmTask> allTasks = session.getAllTasks();

		Map<String, BpmTask> byId = from(allTasks).toMap(new F<BpmTask, String>() {
			@Override
			public String invoke(BpmTask x) {
				return x.getInternalTaskId();
			}
		});

		for (String[] arg : args) {
			String name = arg[0];
			String assignee = arg[1];
			String groupId = arg[2];
			System.out.println(byName(list).get(name).getAssignee());
			if (assignee != null) {
				assertTrue("Assignee in available logins: " + assignee, session.getAvailableLogins(null).contains(assignee));
			}

			BpmTask task = byName(list).get(name);

			assertNotNull(task);

			assertEquals(assignee, task.getAssignee());
			assertEquals(groupId, task.getGroupId());
			assertEquals(name, task.getTaskName());
			assertEquals(processInstanceLocal.getInternalId(), String.valueOf(task.getExecutionId()));
			assertNotNull(task.getCurrentProcessStateConfiguration());
			assertFalse(task.isFinished());

			List<BpmTask> tasks = session.findProcessTasks(processInstanceLocal, assignee, Collections.singleton(name));

			assertEquals(1, tasks.size());
			assertEquals(task.getInternalTaskId(), tasks.get(0).getInternalTaskId());

			BpmTask task2 = session.getTaskData(task.getInternalTaskId());

			checkAreTheSame(task, task2);

			if (task.getAssignee() != null) {
				BpmTask task3 = createSession(task.getAssignee()).refreshTaskData(task);

				checkAreTheSame(task, task3);
			}

			assertTrue(byId.containsKey(task.getInternalTaskId()));
		}
            }
        });
	}

	private void checkAreTheSame(BpmTask task, BpmTask task2) {
		assertEquals(task.getInternalTaskId(), task2.getInternalTaskId());
		assertEquals(task.getProcessInstance().getId(), task2.getProcessInstance().getId());
		assertEquals(task.getAssignee(), task2.getAssignee());
		assertEquals(task.getGroupId(), task2.getGroupId());
		assertEquals(task.getTaskName(), task2.getTaskName());
		assertEquals(task.getProcessInstance().getInternalId(), task2.getProcessInstance().getInternalId());
		assertEquals(task.getCurrentProcessStateConfiguration(), task2.getCurrentProcessStateConfiguration());
		assertEquals(task.isFinished(), task2.isFinished());
	}

	private ProcessInstance startProcess(final String user, final String bpmDefinitionKey) {
        final ProcessInstance[] processInstance = new ProcessInstance[1];
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                SessionTest.this.ctx = ctx;
                ProcessToolBpmSession session = createSession(user);

                String externalKey = "NR" + System.currentTimeMillis();

                StartProcessResult result = session.startProcess(bpmDefinitionKey, externalKey, "test");
                processInstance[0] = result.getProcessInstance();

                assertNotNull(processInstance[0]);
                assertNotNull(processInstance[0].getDefinition());
                assertEquals(bpmDefinitionKey, processInstance[0].getDefinitionName());
                assertNotNull(processInstance[0].getInternalId());
                assertEquals(externalKey, processInstance[0].getExternalKey());
                assertNotNull(processInstance[0].getCreateDate());
                assertNotNull(processInstance[0].getCreatorLogin());
                assertFalse(processInstance[0].isSubprocess());
                assertNull(processInstance[0].getParent());

                assertTrue(processInstance[0].isProcessRunning());
            }
        });
		return processInstance[0];
	}

	private void deploy(ProcessToolContext ctx, String basePath) {
		try {
			ProcessDeployer processDeployer = new ProcessDeployer(ctx);

			String path = basePath + "/processtool-config.xml";
			ProcessDefinitionConfig newConfig = processDeployer.unmarshallProcessDefinition(getStream(path));

			ProcessDefinitionDAO dao = ctx.getProcessDefinitionDAO();
			dao.getActiveConfigurationByKey(newConfig.getBpmDefinitionKey());

			processDeployer.deployOrUpdateProcessDefinition(
					getStream(basePath + "/processdefinition." + registry.getProcessToolSessionFactory().getBpmDefinitionLanguage()),
					getStream(basePath + "/processtool-config.xml"),
					getStream(basePath + "/queues-config.xml"),
					getStream(basePath + "/processdefinition.png"),
					getStream(basePath + "/processdefinition-logo.png"));

			newConfig = dao.getActiveConfigurationByKey(newConfig.getBpmDefinitionKey());

			assertNotNull(newConfig);

			Collection<ProcessDefinitionConfig> configs = ctx.getProcessDefinitionDAO().getActiveConfigurations();

			final ProcessDefinitionConfig finalNewConfig = newConfig;
			assertTrue(from(configs).any(new P<ProcessDefinitionConfig>() {
				@Override
				public boolean invoke(ProcessDefinitionConfig config) {
					return config.getId().equals(finalNewConfig.getId());
				}
			}));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void disableAlreadyExistingDefinitions() {
		Collection<ProcessDefinitionConfig> activeConfigurations = ctx.getProcessDefinitionDAO().getActiveConfigurations();
		for (ProcessDefinitionConfig config : activeConfigurations) {
			config.setLatest(false);
		}
		ctx.getProcessDefinitionDAO().saveOrUpdate(activeConfigurations);
	}

	private InputStream getStream(String path) {
		return getClass().getResourceAsStream(path);
	}

	private void print(Object obj) {
		System.out.println(obj);
	}

	private ProcessToolBpmSession createSession(String user) {
        ProcessToolBpmSession session = getRegistry().getProcessToolSessionFactory().createSession(user, Arrays.asList("ADMIN", user + "_ROLE"));

         if(session instanceof  AbstractProcessToolSession){
          session = injectRegistryToSession((AbstractProcessToolSession) session);
         }
        return session;
    }

    private ProcessToolBpmSession injectRegistryToSession(AbstractProcessToolSession session){
            try {
                Field field = AbstractProcessToolSession.class.getDeclaredField("registry");
                field.setAccessible(true);
                field.set(session,getRegistry());

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        return session;
    }

	@Override
	protected void setUp() throws Exception
	{
		if (isSetUp) {
			return;
		}
		isSetUp = true;

		System.setProperty("use.bitronix", "true");

		ClassDependencyManager.getInstance().injectImplementation(ITokenService.class, TokenServiceMock.class);
		ClassDependencyManager.getInstance().injectImplementation(IAccessTokenFactory.class, AccessTokenFactoryMock.class);

        // Create initial context
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,  "org.apache.naming");
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

        System.setProperty("org.aperteworkflow.datasource", "aperte-workflow-ds");

        new InitialContext().lookup("aperte-workflow-ds");

    	registry = new ProcessToolRegistryImpl();
        UserSourceMock userSourceMock = new UserSourceMock();
        registry.setUserSource(userSourceMock);
        ProcessToolContextFactory contextFactory = new ProcessToolContextFactoryImpl();
		ProcessToolRegistry.Util.setAwfClassLoader(Thread.currentThread().getContextClassLoader());

		super.setUp();
	}

	public static class TokenServiceMock implements ITokenService {
		@Override
		public AccessToken getTokenByTokenId(String tokenId) {
			return null;
		}

		@Override
		public TokenWrapper getTokenWrapperByTokenId(String tokenId) {
			return null;
		}

		@Override
		public TokenWrapper wrapAccessToken(AccessToken token) {
			return null;
		}

		@Override
		public void deleteTokensByTaskId(long taskId) {

		}

		@Override
		public Collection<AccessToken> getAccessTokensByTaskId(long taskId) {
			return null;
		}

		@Override
		public String getPerfromActionServletAddressForToken(String token, ProcessToolBpmConstants.TextModes mode) {
			return null;
		}

		@Override
		public String getFastLinkServletAddressForToken(String token) {
			return null;
		}
	}

	public static class AccessTokenFactoryMock implements IAccessTokenFactory {
		@Override
		public AccessToken create(BpmTask userTask, String actionName) {
			return null;
		}
	}


    public void testSubprocess() {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                SessionTest.this.ctx = ctx;


                wipeDb();
                disableAlreadyExistingDefinitions();
                deploy(ctx, "/processes/subprocess_assignee");
                deploy(ctx, "/processes/subprocess_queue");
                deploy(ctx, "/processes/subprocess_usage");
            }
        });
                performSubprocessTests();
                performSubprocessUsageTest();
    }


    public void testComplaint() {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                SessionTest.this.ctx = ctx;


                wipeDb();
                disableAlreadyExistingDefinitions();
                deploy(ctx, "/processes/complaint");
            }
        });
        performTest1();


    }

	private void wipeDb() {
		exec("delete from pt_process_instance_s_attr;");
		exec("delete from pt_process_instance_attr;");
		exec("delete from pt_process_instance_log;");
		exec("delete from pt_process_instance_owners;");
		exec("delete from pt_process_instance;");

		exec("delete from I18NText;");
		exec("delete from PeopleAssignments_BAs;");
		exec("delete from PeopleAssignments_PotOwners;");
		exec("delete from task;");
	}

	private void exec(String sql) {
		ctx.getHibernateSession().createSQLQuery(sql).executeUpdate();
	}

	private static class VQElem {
		public final String taskName;
		public final String assignee;
		public final String group;

		public VQElem(String taskName, String assignee, String group) {
			this.taskName = taskName;
			this.assignee = assignee;
			this.group = group;
		}
	}

    private class UserSourceMock implements IUserSource{
        private Map<String,UserData> userList = new HashMap<String,UserData>();
        public UserSourceMock(){
            userList.put("admin",createUser("admin", "admin_ROLE"));
            userList.put("john",createUser("john", "john_ROLE"));
            userList.put("mary",createUser("mary", "mary_ROLE"));
            userList.put("peter",createUser("peter", "peter_ROLE"));
            userList.put("jack",createUser("jack", "jack_ROLE"));
        }
        private UserData createUser(String login, String... roles) {
            UserDataBean userData = new UserDataBean(login, login, login+"@wp.pl");
            if (userData.getRoles().isEmpty()) {
                for (String role : roles) {
                    userData.addRole(role);
                }
            }
            return  userData;
        }

        @Override
        public UserData getUserByLogin(String login) throws UserSourceException {
            return userList.get(login);
        }

        @Override
        public UserData getUserByLogin(String login, Long companyId) throws UserSourceException {
            return null;
        }

        @Override
        public UserData getUserByEmail(String email) {
            return null;
        }

        @Override
        public List<UserData> getAllUsers() {
            return null;
        }

        @Override
        public List<UserData> findUsers(String query) {
            return null;
        }
    }

}
