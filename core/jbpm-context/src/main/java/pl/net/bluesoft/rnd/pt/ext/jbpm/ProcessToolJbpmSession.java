package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.Query;
import org.jbpm.api.*;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.identity.User;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.Transition;
import org.jbpm.pvm.internal.query.AbstractQuery;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
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

import java.util.*;
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

	protected Logger log = Logger.getLogger(ProcessToolJbpmSession.class.getName());

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
		return new Mapcar<Task, BpmTask>(findProcessTasks(pi, ctx)) {
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
		ProcessStateConfiguration state = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(processInstance);
		processInstance = getProcessData(processInstance.getInternalId(), ctx);

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
		ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);

        ProcessEngine processEngine = getProcessEngine(ctx);
        processEngine.getTaskService().completeTask(task.getId(), action.getBpmName());
		String s = getProcessState(processInstance, ctx);
        fillProcessAssignmentData(processEngine, processInstance, ctx);
		if (s != null) {
			processInstance.setState(s);
			ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
			eventBusManager.publish(new BpmEvent(BpmEvent.Type.SIGNAL_PROCESS,
			                                     processInstance,
			                                     user));
			ctx.getEventBusManager().publish(new BpmEvent(BpmEvent.Type.SIGNAL_PROCESS,
			                                              processInstance,
			                                              user));
		} else {
            if (processInstance.getRunning() && !isProcessRunning(processInstance.getInternalId(), ctx)) {
                processInstance.setRunning(false);
                ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
                eventBusManager.publish(new BpmEvent(BpmEvent.Type.END_PROCESS,
                			                                     processInstance,
                			                                     user));
                ctx.getEventBusManager().publish(new BpmEvent(BpmEvent.Type.END_PROCESS,
                                                              processInstance,
                                                              user));

            }

        }
		return processInstance;
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
}
