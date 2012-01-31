package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.bpm.graph.StateNode;
import org.aperteworkflow.bpm.graph.TransitionArc;
import org.hibernate.Query;
import org.jbpm.api.*;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.api.identity.User;
import org.jbpm.api.model.Transition;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;
import org.jbpm.pvm.internal.identity.impl.UserImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.query.AbstractQuery;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTask;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolSecurityException;
import pl.net.bluesoft.rnd.processtool.bpm.impl.AbstractProcessToolSession;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.lang.Mapcar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.Lang.keyFilter;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * jBPM session implementation
 *
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolJbpmSession extends AbstractProcessToolSession {

    public static final Logger LOGGER = Logger.getLogger(ProcessToolJbpmSession.class.getName());
    protected Logger log = LOGGER;

	public ProcessToolJbpmSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx) {
		super(user, roleNames);
		IdentityService is = getProcessEngine(ctx).getIdentityService();
		List<User> list = is.findUsers();
		User jbpmUser = null;
		for (User u : list) {
			if (u.getId().equals(user.getLogin())) {
				jbpmUser = u;
			}
		}
		if (jbpmUser == null) {
			is.createUser(user.getLogin(), user.getRealName(), user.getEmail());
		}
		getProcessEngine(ctx).setAuthenticatedUserId(user.getLogin());
	}

	public String getProcessState(ProcessInstance pi, ProcessToolContext ctx) {
		Task newTask = findProcessTask(pi, ctx);
		return newTask != null ? newTask.getName() : null;
	}

	@Override
	public void saveProcessInstance(ProcessInstance processInstance, ProcessToolContext ctx) {
		ExecutionService es = getProcessEngine(ctx).getExecutionService();
		for (ProcessInstanceAttribute pia : processInstance.getProcessAttributes()) {
			if (pia instanceof BpmVariable) {
				BpmVariable bpmVar = (BpmVariable) pia;
				if (hasText(bpmVar.getBpmVariableName())) {
					es.setVariable(processInstance.getInternalId(),
					               bpmVar.getBpmVariableName(),
					               bpmVar.getBpmVariableValue());
				}
			}
		}

		super.saveProcessInstance(processInstance, ctx);
	}

	@Override
	public Collection<ProcessInstance> getQueueContents(final ProcessQueue pq, final int offset, final int limit, ProcessToolContext ctx) {
		Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
		final List<String> names = keyFilter("name", configs);
		if (!names.contains(pq.getName())) throw new ProcessToolSecurityException("queue.no.rights", pq.getName());

		Command<List<Task>> cmd = new Command<List<Task>>() {
			@Override
			public List<Task> execute(Environment environment) throws Exception {
				AbstractQuery q = new AbstractQuery() {
					@Override
					protected void applyPage(Query query) {
						query.setFirstResult(offset);
						query.setFetchSize(limit);
					}

					@Override
					protected void applyParameters(Query query) {
						query.setParameterList("groupIds", Arrays.asList(pq.getName()));
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select task ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");
						hql.append(", ");
						hql.append(ParticipationImpl.class.getName());
						hql.append(" as participant ");
						hql.append("where participant.task=task ");
						hql.append("and participant.type = 'candidate' ");
						hql.append("and participant.groupId IN (:groupIds) ");
						hql.append("and task.assignee is null ");
						hql.append("order by task.id DESC");
						return hql.toString();

					}
				};
				return (List<Task>) q.execute(environment);
			}
		};
		ProcessEngine processEngine = getProcessEngine(ctx);
		List<Task> taskList = processEngine.execute(cmd);
		List<String> ids = keyFilter("executionId", taskList);
		final Map<String, ProcessInstance> instances = ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(ids);
		return new Mapcar<Task, ProcessInstance>(taskList) {

			@Override
			public ProcessInstance lambda(Task task) {
				ProcessInstance pi = instances.get(task.getExecutionId());
				if (pi == null) {
					log.warning("process " + task.getExecutionId() + " not found");
					return null;
				}
				pi.setState(task.getActivityName());
				pi.setTaskId(task.getId());
				return pi;
			}
		}.go();


	}

	@Override
	public ProcessInstance assignTaskFromQueue(ProcessQueue q, ProcessToolContext processToolContextFromThread) {
		return assignTaskFromQueue(q, null, processToolContextFromThread);
	}

    public List<String> getOutgoingTransitionNames(String internalId, ProcessToolContext ctx) {
        ProcessEngine engine = getProcessEngine(ctx);
        org.jbpm.api.ProcessInstance pi = engine.getExecutionService().findProcessInstanceById(internalId);
        ExecutionImpl execution = (ExecutionImpl) pi.getProcessInstance();
        List<String> transitionNames = new ArrayList<String>();
        for (Transition transition : execution.getActivity().getOutgoingTransitions()) {
            transitionNames.add(transition.getDestination().getName());
        }
        return transitionNames;
    }

	protected ProcessEngine getProcessEngine(ProcessToolContext ctx) {
		if (ctx instanceof ProcessToolContextImpl) {
			ProcessEngine engine = ((ProcessToolContextImpl) ctx).getProcessEngine();
			if (user != null && user.getLogin() != null) engine.setAuthenticatedUserId(user.getLogin());
			return engine;
		} else {
			throw new IllegalArgumentException(ctx + " not an instance of " + ProcessToolContextImpl.class.getName());
		}
	}

	public Collection<ProcessInstance> getUserProcesses(int offset, int limit, ProcessToolContext ctx) {
		List<Task> taskList = getProcessEngine(ctx).getTaskService().createTaskQuery()
				.notSuspended()
				.assignee(user.getLogin())
                .orderDesc("executionId")
				.page(offset, limit).list();

		List<String> ids = keyFilter("executionId", taskList);
		final Map<String, ProcessInstance> instances = ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(ids);
		return new Mapcar<Task, ProcessInstance>(taskList) {

			@Override
			public ProcessInstance lambda(Task task) {
				ProcessInstance pi = instances.get(task.getExecutionId());
				if (pi == null) {
					log.warning("process " + task.getExecutionId() + " not found");
					return null;
				}
				pi.setState(task.getActivityName());
				pi.setTaskId(task.getId());
				return pi;
			}
		}.go();
	}

	@Override
	public Collection<ProcessQueue> getUserAvailableQueues(ProcessToolContext ctx) {
		Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
		final List<String> names = keyFilter("name", configs);

		if (names.isEmpty()) {
			return new ArrayList();
		}

		Command<List<Map>> cmd = new Command<List<Map>>() {
			@Override
			public List<Map> execute(Environment environment) throws Exception {
				return (List<Map>) new AbstractQuery() {

					@Override
					protected void applyParameters(Query query) {
						query.setParameterList("groupIds", names);
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select new map(participant.groupId as groupId, count(task) as taskCount) ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");
						hql.append(", ");
						hql.append(ParticipationImpl.class.getName());
						hql.append(" as participant ");
						hql.append("where participant.task=task ");
						hql.append("and participant.type = 'candidate' ");
						hql.append("and participant.groupId IN (:groupIds) ");
						hql.append("and task.assignee is null ");
						hql.append("group by participant.groupId");

						return hql.toString();

					}
				}.execute(environment);
			}
		};

		List<Map> maps = getProcessEngine(ctx).execute(cmd);
		Map<String, Long> counts = new HashMap();
		for (Map m : maps) {
			counts.put((String) m.get("groupId"), (Long) m.get("taskCount"));
		}

		for (ProcessQueue q : configs) {
			q.setProcessCount(nvl(counts.get(q.getName()), (long) 0));
		}
		return configs;
	}

	@Override
	public ProcessInstance assignTaskFromQueue(final ProcessQueue pq, ProcessInstance pi, ProcessToolContext ctx) {

		Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
		final List<String> names = keyFilter("name", configs);
		final String taskId = pi != null ? pi.getTaskId() : null;
		if (!names.contains(pq.getName())) throw new ProcessToolSecurityException("queue.no.rights", pq.getName());

		Command<List<Task>> cmd = new Command<List<Task>>() {
			@Override
			public List<Task> execute(Environment environment) throws Exception {
				AbstractQuery q = new AbstractQuery() {
					@Override
					protected void applyPage(Query query) {
						query.setFirstResult(0);
						query.setFetchSize(1);
					}

					@Override
					protected void applyParameters(Query query) {
						query.setParameterList("groupIds", Arrays.asList(pq.getName()));
						if (taskId != null) {
							query.setParameter("taskId", new Long(taskId));
						}
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select task ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");
						hql.append(", ");
						hql.append(ParticipationImpl.class.getName());
						hql.append(" as participant ");
						hql.append("where participant.task=task ");
						hql.append("and participant.type = 'candidate' ");
						hql.append("and participant.groupId IN (:groupIds) ");
						hql.append("and task.assignee is null ");
						if (taskId != null)
							hql.append("and task.id = :taskId");
						return hql.toString();

					}
				};
				return (List<Task>) q.execute(environment);
			}
		};
		ProcessEngine processEngine = getProcessEngine(ctx);
		List<Task> taskList = processEngine.execute(cmd);
		if (taskList.isEmpty()) {
			return null;
		}
		Task task = taskList.get(0);
		ProcessInstance pi2 = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(task.getExecutionId());
		if (pi2 == null) {
			if (pi == null)
				return assignTaskFromQueue(pq, ctx);
			else
				return null;
		}
		processEngine.getTaskService().assignTask(task.getId(), user.getLogin());

		if (!user.getLogin().equals(processEngine.getTaskService().getTask(task.getId()).getAssignee())) {
			if (pi == null)
				return assignTaskFromQueue(pq, ctx);
			else
				return null;
		}
		pi2.setTaskId(task.getId());
		pi2.setState(task.getActivityName());

		ProcessInstanceLog log = new ProcessInstanceLog();
		log.setLogType(ProcessInstanceLog.LOG_TYPE_CLAIM_PROCESS);
		log.setState(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(pi));
		log.setEntryDate(Calendar.getInstance());
		log.setEventI18NKey("process.log.process-assigned");
		log.setLogValue(pq.getName());
		log.setUser(ctx.getProcessInstanceDAO().findOrCreateUser(user));
		log.setAdditionalInfo(pq.getDescription());
		pi2.addProcessLog(log);

        fillProcessAssignmentData(processEngine, pi2, ctx);
		ctx.getProcessInstanceDAO().saveProcessInstance(pi2);

		eventBusManager.publish(new BpmEvent(BpmEvent.Type.ASSIGN_PROCESS,
		                                     pi2, user));

		ctx.getEventBusManager().publish(new BpmEvent(BpmEvent.Type.ASSIGN_PROCESS,
		                                              pi2,
		                                              user));

		return pi2;
	}

    private void fillProcessAssignmentData(final ProcessEngine processEngine, final ProcessInstance pi, ProcessToolContext ctx) {
        Set<String> assignees = new HashSet<String>();
        Set<String> queues = new HashSet<String>();       
        TaskService taskService = processEngine.getTaskService();
        for (Task t : findProcessTasks(pi, ctx, false)) {
            if (t.getAssignee() != null) {
                assignees.add(t.getAssignee());
            } else { //some optimization could be possible
                for (Participation participation : taskService.getTaskParticipations(t.getId())) {
                    if ("candidate".equals(participation.getType())) {
                        queues.add(participation.getGroupId());
                    }
                }
            }
        }
        pi.setAssignees(assignees.toArray(new String[assignees.size()]));
        pi.setTaskQueues(queues.toArray(new String[queues.size()]));
    }

	@Override()
	public Collection<BpmTask> getTaskList(ProcessInstance pi, final ProcessToolContext ctx) {
        return getTaskList(pi, ctx, true);
    }

	@Override()
	public Collection<BpmTask> getTaskList(ProcessInstance pi, final ProcessToolContext ctx, final boolean mustHaveAssignee) {
		return new Mapcar<Task, BpmTask>(findProcessTasks(pi, ctx, mustHaveAssignee)) {
			@Override
			public BpmTask lambda(Task x) {
				BpmTask t = new BpmTask();
				t.setOwner(ctx.getUserDataDAO().loadUserByLogin(x.getAssignee()));
				t.setTaskName(x.getActivityName());
				t.setInternalTaskId(x.getId());
				return t;
			}
		}.go();
	}

	@Override
	public boolean isProcessOwnedByUser(final ProcessInstance processInstance, ProcessToolContext ctx) {
		List<Task> taskList = findUserTask(processInstance, ctx);
		return !taskList.isEmpty();

	}

	private List<Task> findUserTask(final ProcessInstance processInstance, ProcessToolContext ctx) {
		Command<List<Task>> cmd = new Command<List<Task>>() {
			@Override
			public List<Task> execute(Environment environment) throws Exception {
				AbstractQuery q = new AbstractQuery() {
					@Override
					protected void applyPage(Query query) {
						query.setFirstResult(0);
						query.setFetchSize(1);
					}

					@Override
					protected void applyParameters(Query query) {
						query.setParameterList("executionId", Arrays.asList(processInstance.getInternalId()));
						query.setParameterList("assignee", Arrays.asList(user.getLogin()));
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select task ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");
						hql.append("where executionId in (:executionId) ");
						hql.append("and task.assignee in (:assignee) ");

						return hql.toString();

					}
				};
				return (List<Task>) q.execute(environment);
			}
		};
		return getProcessEngine(ctx).execute(cmd);
	}

    private List<Task> findProcessTasks(final ProcessInstance processInstance, ProcessToolContext ctx) {
        return findProcessTasks(processInstance, ctx, true);
    }
	private List<Task> findProcessTasks(final ProcessInstance processInstance, ProcessToolContext ctx,
                                        final boolean mustHaveAssignee) {
		Command<List<Task>> cmd = new Command<List<Task>>() {
			@Override
			public List<Task> execute(Environment environment) throws Exception {
				AbstractQuery q = new AbstractQuery() {
					@Override
					protected void applyPage(Query query) {
						query.setFirstResult(0);
						query.setFetchSize(1);
					}

					@Override
					protected void applyParameters(Query query) {
						query.setParameterList("executionId", Arrays.asList(processInstance.getInternalId()));
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select task ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");
						hql.append("where executionId in (:executionId) ");
						if (mustHaveAssignee) hql.append("and task.assignee is not null ");

						return hql.toString();

					}
				};
				return (List<Task>) q.execute(environment);
			}
		};
		return getProcessEngine(ctx).execute(cmd);
	}

	private Task findProcessTask(final ProcessInstance processInstance, ProcessToolContext ctx) {
		Command<List<Task>> cmd = new Command<List<Task>>() {
			@Override
			public List<Task> execute(Environment environment) throws Exception {
				AbstractQuery q = new AbstractQuery() {
					@Override
					protected void applyPage(Query query) {
						query.setFirstResult(0);
						query.setFetchSize(1);
					}

					@Override
					protected void applyParameters(Query query) {
						query.setParameterList("executionId", Arrays.asList(processInstance.getInternalId()));
					}

					@Override
					public String hql() {
						StringBuilder hql = new StringBuilder();
						hql.append("select task ");
						hql.append("from ");
						hql.append(TaskImpl.class.getName());
						hql.append(" as task ");
						hql.append("where executionId in (:executionId) ");

						return hql.toString();

					}
				};
				return (List<Task>) q.execute(environment);
			}
		};
		List<Task> res = getProcessEngine(ctx).execute(cmd);
		if (res.isEmpty()) return null;
		return res.get(0);
	}


	public ProcessInstance performAction(ProcessStateAction action,
	                                     ProcessInstance processInstance,
	                                     ProcessToolContext ctx) {
		List<Task> tasks = findUserTask(processInstance, ctx);

		if (tasks.isEmpty()) {
			throw new IllegalArgumentException("process.not.owner");
		}

		Task task = tasks.get(0);
		return performAction(action, processInstance, ctx, task);
	}
	public ProcessInstance performAction(ProcessStateAction action,
	                                     ProcessInstance processInstance,
	                                     ProcessToolContext ctx,
	                                     BpmTask bpmTask) {
		return performAction(action, processInstance, ctx, getProcessEngine(ctx).getTaskService().getTask(bpmTask.getInternalTaskId()));
	}


	private ProcessInstance performAction(ProcessStateAction action, ProcessInstance processInstance, ProcessToolContext ctx, Task task) {
        processInstance = getProcessData(processInstance.getInternalId(), ctx);

        addActionLogEntry(action, processInstance, ctx);
		ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
        ProcessEngine processEngine = getProcessEngine(ctx);

        processEngine.getTaskService().completeTask(task.getId(), action.getBpmName());
        
		String s = getProcessState(processInstance, ctx);
        fillProcessAssignmentData(processEngine, processInstance, ctx);
        processInstance.setState(s);
        if (s == null && processInstance.getRunning() && !isProcessRunning(processInstance.getInternalId(), ctx)) {
            processInstance.setRunning(false);
        }
        ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
        publishEvents(processInstance, processInstance.getRunning() ? BpmEvent.Type.SIGNAL_PROCESS : BpmEvent.Type.END_PROCESS);                
        
		return processInstance;
	}

    private void publishEvents(ProcessInstance processInstance, BpmEvent.Type signalProcess) {
        eventBusManager.publish(new BpmEvent(signalProcess,
                                             processInstance,
                                             user));
        ProcessToolContext.Util.getProcessToolContextFromThread().getEventBusManager().publish(new BpmEvent(signalProcess,
                                                      processInstance,
                                                      user));
    }

    private void addActionLogEntry(ProcessStateAction action, ProcessInstance processInstance, ProcessToolContext ctx) {
        ProcessStateConfiguration state = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(processInstance);

        ProcessInstanceLog log = new ProcessInstanceLog();
        log.setLogType(ProcessInstanceLog.LOG_TYPE_PERFORM_ACTION);
        log.setState(state);
        log.setEntryDate(Calendar.getInstance());
        log.setEventI18NKey("process.log.action-performed");
        log.setLogValue(action.getBpmName());
        log.setAdditionalInfo(nvl(action.getLabel(), action.getDescription(), action.getBpmName()));
        log.setUser(ctx.getProcessInstanceDAO().findOrCreateUser(user));
        log.setUserSubstitute(substitutingUser != null ? ctx.getProcessInstanceDAO().findOrCreateUser(substitutingUser) : null);
        processInstance.addProcessLog(log);
    }

    @Override
	public boolean isProcessRunning(String internalId, ProcessToolContext ctx) {

		if (internalId == null) return false;

		ExecutionService service = getProcessEngine(ctx).getExecutionService();
		org.jbpm.api.ProcessInstance processInstance = service.findProcessInstanceById(internalId);
		return processInstance != null && !processInstance.isEnded();

	}

	protected ProcessInstance startProcessInstance(ProcessDefinitionConfig config,
	                                               String externalKey,
	                                               ProcessToolContext ctx,
	                                               ProcessInstance pi) {
        ProcessEngine processEngine = getProcessEngine(ctx);
        final ExecutionService execService = processEngine.getExecutionService();
		Map vars = new HashMap();
		vars.put("processInstanceId", String.valueOf(pi.getId()));
		vars.put("initiator", user.getLogin());
        for (ProcessInstanceAttribute pia : pi.getProcessAttributes()) {
			if (pia instanceof BpmVariable) {
				BpmVariable bpmVar = (BpmVariable) pia;
				if (hasText(bpmVar.getBpmVariableName())) {
					vars.put(bpmVar.getBpmVariableName(), bpmVar.getBpmVariableValue());
				}
			}
		}

		org.jbpm.api.ProcessInstance instance = execService.startProcessInstanceByKey(config.getBpmDefinitionKey(),
		                                                                              vars,
		                                                                              externalKey);
		pi.setInternalId(instance.getId());
        fillProcessAssignmentData(processEngine, pi, ctx);
		return pi;
	}

    @Override
    public ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx) {
        ProcessToolJbpmSession session = new ProcessToolJbpmSession(user, roleNames, ctx);
        session.substitutingUser = this.user;
        return session;
    }

    @Override
    public void adminCancelProcessInstance(ProcessInstance pi) {
        log.severe("User: " + user.getLogin() + " attempting to cancel process: " + pi.getInternalId());
        ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
        pi = getProcessData(pi.getInternalId(), ctx);
        ProcessEngine processEngine = getProcessEngine(ctx);
        processEngine.getExecutionService().endProcessInstance(pi.getInternalId(), "admin-cancelled");
        fillProcessAssignmentData(processEngine, pi, ctx);
        pi.setRunning(false);
        pi.setState(null);
        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        log.severe("User: " + user.getLogin() + " has cancelled process: " + pi.getInternalId());

    }

    @Override
    public void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin) {
        log.severe("User: " + user.getLogin() + " attempting to reassign task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to user: " + userLogin);

        ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
        pi = getProcessData(pi.getInternalId(), ctx);
        ProcessEngine processEngine = getProcessEngine(ctx);
        TaskService ts = processEngine.getTaskService();
        Task task = ts.getTask(bpmTask.getInternalTaskId());
        if (nvl(userLogin,"").equals(nvl(task.getAssignee(),""))) {
            log.severe("User: " + user.getLogin() + " has not reassigned task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " as the user is the same: " + userLogin);            
            return;
        }
        //this call should also take care of swimlanes
        ts.assignTask(bpmTask.getInternalTaskId(), userLogin);
        fillProcessAssignmentData(processEngine, pi, ctx);
        log.info("Process.running:" + pi.getRunning());
        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        log.severe("User: " + user.getLogin() + " has reassigned task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to user: " + userLogin);

    }

    @Override
    public void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action) {
        log.severe("User: " + user.getLogin() + " attempting to complete task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to outcome: " + action);
        performAction(action, pi, ProcessToolContext.Util.getProcessToolContextFromThread(), bpmTask);
        log.severe("User: " + user.getLogin() + " has completed task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to outcome: " + action);

    }
    
    public List<String> getAvailableLogins(final String filter) {
        Command<List<User>> cmd = new Command<List<User>>() {
            @Override
            public List<User> execute(Environment environment) throws Exception {
                AbstractQuery q = new AbstractQuery() {
                    @Override
                    protected void applyPage(Query query) {
                        query.setFirstResult(0);
                        query.setFetchSize(20);
                    }

                    @Override
                    protected void applyParameters(Query query) {
                        query.setParameter("filter", "%" + filter + "%");
                    }

                    @Override
                    public String hql() {
                        StringBuilder hql = new StringBuilder();
                        hql.append("select user ");
                        hql.append("from ");
                        hql.append(UserImpl.class.getName());
                        hql.append(" as user ");
                        hql.append("where id like :filter ");

                        return hql.toString();

                    }
                };
                return (List<User>) q.execute(environment);
            }
        };
        List<User> users = getProcessEngine(ProcessToolContext.Util.getProcessToolContextFromThread()).execute(cmd);
        List<String> res = new ArrayList<String>();
        for (User u : users) {
            res.add(u.getId());
        }
        Collections.sort(res);
        return res;

    }

    @Override
    public List<GraphElement> getProcessHistory(final ProcessInstance pi) {
        ProcessEngine processEngine = getProcessEngine(ProcessToolContext.Util.getProcessToolContextFromThread());
        HistoryService service = processEngine.getHistoryService();
        HistoryActivityInstanceQuery activityInstanceQuery = service.createHistoryActivityInstanceQuery().executionId(pi.getInternalId());
        List<HistoryActivityInstance> list = activityInstanceQuery.list();

        Map<String,GraphElement> processGraphElements = parseProcessDefinition(pi);

        ArrayList<GraphElement> res = new ArrayList<GraphElement>();
        for (HistoryActivityInstance hpi : list) {
            System.out.println(hpi.getActivityName());
            if (hpi instanceof HistoryActivityInstanceImpl) {
                HistoryActivityInstanceImpl activity = (HistoryActivityInstanceImpl) hpi;
                String activityName = activity.getActivityName();
                if (res.isEmpty()) { //initialize start node and its transition
                    GraphElement startNode = processGraphElements.get("__AWF__start_node");
                    if (startNode != null) {
                        res.add(startNode);
                    }
                    GraphElement firstTransition = processGraphElements.get("__AWF__start_transition_to_" + activityName);
                    if (firstTransition != null) {
                        res.add(firstTransition);
                    }
                }
                StateNode sn = (StateNode) processGraphElements.get(activityName);
                if (sn == null) continue;
                sn = sn.cloneNode();
                sn.setUnfinished(activity.getEndTime() == null);
                sn.setLabel(activityName + ": " + hpi.getDuration() + "ms");
                res.add(sn);
                //look for transition
                TransitionArc ta = (TransitionArc) processGraphElements.get(activityName + "_" + activity.getTransitionName());
                if (ta == null) { //look for default!
                    ta = (TransitionArc) processGraphElements.get("__AWF__default_transition_" + activityName);
                }
                if (ta == null) {
                    continue;
                }
                res.add(ta.cloneNode());
            } else {
                LOGGER.severe("Unsupported entry: " + hpi);
            }
        }
        HistoryProcessInstanceQuery historyProcessInstanceQuery = processEngine.getHistoryService()
                .createHistoryProcessInstanceQuery().processInstanceId(pi.getInternalId());
        HistoryProcessInstance historyProcessInstance = historyProcessInstanceQuery.uniqueResult();
        if (historyProcessInstance != null && historyProcessInstance.getEndActivityName() != null) {
            StateNode sn = (StateNode) processGraphElements.get(historyProcessInstance.getEndActivityName());
            if (sn != null) {
                res.add(sn);
            }
        }
        return res;
    }

    private HashMap<String, GraphElement> parseProcessDefinition(ProcessInstance pi) {
        HashMap<String, GraphElement> res = new HashMap<String, GraphElement>();
        byte[] processDefinition = getProcessDefinition(pi);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(new ByteArrayInputStream(processDefinition));
            Element documentElement = dom.getDocumentElement();
            String[] nodeTypes = new String[]{"start", "end", "java", "task", "decision"};
            for (String nodeType : nodeTypes) {
                NodeList nodes = documentElement.getElementsByTagName(nodeType);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    try {
                        StateNode sn = new StateNode();
                        String gval = node.getAttribute("g");
                        String[] vals = gval.split(",", 4);
                        int x = Integer.parseInt(vals[0]);
                        int y = Integer.parseInt(vals[1]);
                        int w = Integer.parseInt(vals[2]);
                        int h = Integer.parseInt(vals[3]);
                        sn.setX(x);
                        sn.setY(y);
                        sn.setWidth(w);
                        sn.setHeight(h);
                        String name = node.getAttribute("name");
                        sn.setLabel(name);
                        res.put(name, sn);
                        if ("start".equals(nodeType)) {
                            res.put("__AWF__start_node", sn);
                        }
                        LOGGER.fine("Found node" + name + ": " + x + "," + y + "," + w + "," + h);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
                //once again - for transitions
            for (String nodeType : nodeTypes) {
                NodeList nodes = documentElement.getElementsByTagName(nodeType);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    try {
                        String startNodeName = node.getAttribute("name");
                        StateNode startNode = (StateNode) res.get(startNodeName);
                        if (startNode == null) {
                            LOGGER.severe("Start node " + startNodeName +
                                    " has not been localized, skipping transition drawing too.");
                            continue;
                        }
                        NodeList transitions = node.getElementsByTagName("transition");
                        for (int j=0; j < transitions.getLength(); j++) {
                            Element transitionEl = (Element) transitions.item(j);
                            String name = transitionEl.getAttribute("name");
                            String to = transitionEl.getAttribute("to") ;
                            StateNode endNode = (StateNode) res.get(to);
                            if (endNode == null) {
                                LOGGER.severe("End node " + to + " has not been localized for transition " + name +
                                        " of node " + startNodeName + ", skipping transition drawing.");
                                continue;                                
                            }
                            String g = transitionEl.getAttribute("g");
                            if (g != null) {
                                String[] dockersAndDistances = g.split(":");
                                String[] dockers = new String[0];
                                if (dockersAndDistances.length == 2) {
                                    dockers = dockersAndDistances[0].split(";");//what the other numbers mean - I have no idea...
                                }
                                //calculate line start node which is a center of the start node
                                int startX = startNode.getX() + startNode.getWidth()/2;
                                int startY = startNode.getY() + startNode.getHeight()/2;
                                //and the same for end node
                                int endX   = endNode.getX() + endNode.getWidth()/2;
                                int endY   = endNode.getY() + endNode.getHeight()/2;

                                TransitionArc arc = new TransitionArc();
                                arc.setName(name);
                                arc.addPoint(startX, startY);
                                for (String docker : dockers) {
                                    String[] split = docker.split(",",2);
                                    arc.addPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                                }
                                arc.addPoint(endX, endY);

                                endX = arc.getPath().get(1).getX();
                                endY = arc.getPath().get(1).getY();
                                double a = ((double)(startY-endY))/((double)(startX - endX));//remember about vertical line
                                //startY = startX*a+b
                                double b = (double)startY - (double)startX*a;
                                for (int x = startX; x <= endX; x++) {
                                    int y = (int) Math.round(a*x+b);
                                    boolean inside = false;
                                    if (x >= startNode.getX() && x <= startNode.getX() + startNode.getWidth()) {
                                        if (y >= startNode.getY() && y <= startNode.getY() + startNode.getHeight()) {
                                            inside = true;
                                        }
                                    }
                                    if (!inside) {
                                        startX = x;
                                        startY = y;
                                        break;
                                    }
                                }
                                for (int x = startX; x > endX; x--) {
                                    int y = (int) Math.round(a*x+b);
                                    boolean inside = false;
                                    if (x >= startNode.getX() && x <= startNode.getX() + startNode.getWidth()) {
                                        if (y >= startNode.getY() && y <= startNode.getY() + startNode.getHeight()) {
                                            inside = true;
                                        }
                                    }
                                    if (!inside) {
                                        startX = x;
                                        startY = y;
                                        break;
                                    }
                                }
                                arc.getPath().get(0).setX(startX);
                                arc.getPath().get(0).setY(startY);

                                endX = arc.getPath().get(arc.getPath().size()-1).getX();
                                endY = arc.getPath().get(arc.getPath().size()-1).getY();
                                startX = arc.getPath().get(arc.getPath().size()-2).getX();
                                startY = arc.getPath().get(arc.getPath().size()-2).getY();
                                if (arc.getPath().size() > 2) {
                                    System.out.println(arc);
                                }
                                a = ((double)(startY-endY))/((double)(startX - endX));//remember about vertical line
                                //startY = startX*a+b
                                b = (double)startY - (double)startX*a;
                                for (int x = endX; x <= startX; x++) {
                                    int y = (int) Math.round(a*x+b);
                                    boolean inside = false;
                                    if (x >= endNode.getX() && x <= endNode.getX() + endNode.getWidth()) {
                                        if (y >= endNode.getY() && y <= endNode.getY() + endNode.getHeight()) {
                                            inside = true;
                                        }
                                    }
                                    if (!inside) {
                                        endX = x;
                                        endY = y;
                                        break;
                                    }
                                }
                                for (int x = endX; x > startX; x--) {
                                    int y = (int) Math.round(a*x+b);
                                    boolean inside = false;
                                    if (x >= endNode.getX() && x <= endNode.getX() + endNode.getWidth()) {
                                        if (y >= endNode.getY() && y <= endNode.getY() + endNode.getHeight()) {
                                            inside = true;
                                        }
                                    }
                                    if (!inside) {
                                        endX = x;
                                        endY = y;
                                        break;
                                    }
                                }
                                arc.getPath().get(arc.getPath().size()-1).setX(endX);
                                arc.getPath().get(arc.getPath().size()-1).setY(endY);

                                res.put(startNodeName + "_" + name,arc);
                                if ("start".equals(nodeType)) {
                                    res.put("__AWF__start_transition_to_" + to, arc);
                                }
                                if (transitions.getLength() == 1) {
                                    res.put("__AWF__default_transition_" + startNodeName, arc);
                                }
                            } else {
                                LOGGER.severe("No 'g' attribute for transition "+ name +
                                               " of node " + startNodeName + ", skipping transition drawing.");
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public byte[] getProcessLatestDefinition(String definitionKey, String processName) {
        String resourceName = processName + ".jpdl.xml";
        return fetchLatestProcessResource(definitionKey, resourceName);
    }

    @Override
    public byte[] getProcessMapImage(ProcessInstance pi) {
        String resourceName = pi.getDefinition().getProcessName() + ".png";
        return fetchProcessResource(pi, resourceName);
    }

    @Override
    public byte[] getProcessDefinition(ProcessInstance pi) {
        String resourceName = pi.getDefinition().getProcessName() + ".jpdl.xml";
        return fetchProcessResource(pi, resourceName);
    }

    private byte[] fetchLatestProcessResource(String definitionKey, String resourceName) {
        RepositoryService service = getProcessEngine(ProcessToolContext.Util.getProcessToolContextFromThread())
                .getRepositoryService();
        List<ProcessDefinition> latestList = service.createProcessDefinitionQuery()
                .processDefinitionKey(definitionKey).orderDesc("deployment.dbid").page(0, 1).list();
        if (!latestList.isEmpty()) {
            String oldDeploymentId = latestList.get(0).getDeploymentId();
            return getDeploymentResource(resourceName, oldDeploymentId);
        }
        return null;
    }
    private byte[] fetchProcessResource(ProcessInstance pi, String resourceName) {
        ProcessEngine processEngine = getProcessEngine(ProcessToolContext.Util.getProcessToolContextFromThread());
        RepositoryService service = processEngine.getRepositoryService();

        ExecutionService executionService = processEngine.getExecutionService();
        org.jbpm.api.ProcessInstance processInstanceById = executionService.findProcessInstanceById(pi.getInternalId());
        String processDefinitionId;
        if (processInstanceById == null) { //look in history service
            HistoryProcessInstanceQuery historyProcessInstanceQuery = processEngine.getHistoryService()
                    .createHistoryProcessInstanceQuery().processInstanceId(pi.getInternalId());
            HistoryProcessInstance historyProcessInstance = historyProcessInstanceQuery.uniqueResult();
            processDefinitionId = historyProcessInstance.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstanceById.getProcessDefinitionId();
        }
        List<ProcessDefinition> latestList = service.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).orderDesc("deployment.dbid").page(0, 1).list();
        if (!latestList.isEmpty()) {
            String oldDeploymentId = latestList.get(0).getDeploymentId();
            return getDeploymentResource(resourceName, oldDeploymentId);
        }
        return null;
    }

    private byte[] getDeploymentResource(String resourceName, String oldDeploymentId) {
        RepositoryService service = getProcessEngine(ProcessToolContext.Util.getProcessToolContextFromThread()).getRepositoryService();;
        try {
            InputStream oldStream = service.getResourceAsStream(oldDeploymentId, resourceName);
            try {
                if (oldStream != null) {
                    ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                    int c;
                    while ((c = oldStream.read()) >= 0) {
                        bos2.write(c);
                    }
                    return bos2.toByteArray();
                }
            }
            finally {
                oldStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String deployProcessDefinition(String processName, InputStream definitionStream, InputStream processMapImageStream) {
        RepositoryService service = getProcessEngine(ProcessToolContext.Util.getProcessToolContextFromThread())
                .getRepositoryService();
        NewDeployment deployment = service.createDeployment();
        deployment.addResourceFromInputStream(processName + ".jpdl.xml", definitionStream);
        if (processMapImageStream != null)
            deployment.addResourceFromInputStream(processName + ".png", processMapImageStream);
        return deployment.deploy();
    }

}
