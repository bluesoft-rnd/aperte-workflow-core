package pl.net.bluesoft.rnd.pt.ext.jbpm;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.Lang.keyFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.bpm.graph.StateNode;
import org.aperteworkflow.bpm.graph.TransitionArc;
import org.aperteworkflow.ui.view.ViewEvent;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.IdentityService;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryActivityInstanceQuery;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.history.HistoryProcessInstanceQuery;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.identity.User;
import org.jbpm.api.model.Transition;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.history.model.HistoryActivityInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskImpl;
import org.jbpm.pvm.internal.history.model.HistoryTaskInstanceImpl;
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
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolSecurityException;
import pl.net.bluesoft.rnd.processtool.bpm.impl.AbstractProcessToolSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessStatus;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.token.IAccessTokenFactory;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.pt.ext.jbpm.query.BpmTaskFilterQuery;
import pl.net.bluesoft.util.lang.Lang;
import pl.net.bluesoft.util.lang.Mapcar;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.Transformer;

/**
 * jBPM session implementation
 *
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 */
public class ProcessToolJbpmSession extends AbstractProcessToolSession 
{
    private static final String AUTO_SKIP_TASK_NAME_PREFIX = "AUTO_SKIP";
	private static final String AUTO_SKIP_ACTION_NAME = "AUTO_SKIP";
	private static final String JOIN_NODE_NAME = "join";
	private static final String FORK_NODE_NAME = "fork";
	protected Logger log = Logger.getLogger(ProcessToolJbpmSession.class.getName());

    private static final Integer DEFAULT_OFFSET_VALUE = 0;
	private static final Integer DEFAULT_LIMIT_VALUE = 1000;
	protected Logger loger = Logger.getLogger(ProcessToolJbpmSession.class.getName());


	@AutoInject
	private IAccessTokenFactory accessTokenFactory;
	
	@AutoInject
	private ITokenService tokenService;

    public ProcessToolJbpmSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx) {
        super(user, roleNames, ctx.getRegistry());
        
        /* Dependency Injection */
        ObjectFactory.inject(this);
        
        
        if (user != null) {
            IdentityService is = getProcessEngine(ctx).getIdentityService();
            User jbpmUser = is.findUserById(user.getLogin());
            if (jbpmUser == null) {
                is.createUser(user.getLogin(), user.getRealName(), user.getEmail());
            }
            getProcessEngine(ctx).setAuthenticatedUserId(user.getLogin());

        }
    }

    @Override
   	public Collection<BpmTask> getAllTasks(ProcessToolContext ctx) {
   		Command<List<Task>> cmd = new Command<List<Task>>() {
   			@Override
   			public List<Task> execute(Environment environment) throws Exception {
   				AbstractQuery q = new AbstractQuery() {
   					@Override
   					protected void applyPage(Query query) {
   					}

   					@Override
   					protected void applyParameters(Query query) {
   					}

   					@Override
   					public String hql() {
   						StringBuilder hql = new StringBuilder();
   						hql.append("select task ");
   						hql.append("from ");
   						hql.append(TaskImpl.class.getName());
   						hql.append(" as task  ");
   						hql.append("order by task.id DESC");
   						return hql.toString();
   					}
   				};
   				return (List<Task>) q.execute(environment);
   			}
   		};
   		List<Task> tasks = getProcessEngine(ctx).execute(cmd);
   		return findProcessInstancesForTasks(tasks, ctx);
   	}
    
   	public Collection<BpmTask> getProcessTaskInQueues(ProcessToolContext ctx, final ProcessInstance processInstance) 
   	{
   		Command<List<Task>> cmd = new Command<List<Task>>() {
   			@Override
   			public List<Task> execute(Environment environment) throws Exception {
   				AbstractQuery q = new AbstractQuery() {
   					@Override
   					protected void applyPage(Query query) 
   					{

   					}
   					
   					@Override
   					protected void applyParameters(Query query) 
   					{
   						query.setString("executionId", processInstance.getInternalId());
   					}

   					@Override
   					public String hql() {
   						StringBuilder hql = new StringBuilder();
   						hql.append("select task ")
   						.append("from ")
   						.append(TaskImpl.class.getName()).append(" as task, ")
   						.append(ParticipationImpl.class.getName()).append(" as participation ")
   						.append(" where participation.task = task ")
   						.append(" and task.executionId = :executionId ");
   						return hql.toString();
   					}
   				};
   				return (List<Task>) q.execute(environment);
   			}
   		};
   		List<Task> tasks = getProcessEngine(ctx).execute(cmd);
   		return findProcessInstancesForTasks(tasks, ctx);
   	}

       @Override
       public BpmTask getPastEndTask(ProcessInstanceLog log, ProcessToolContext ctx) {
           final ProcessInstance pi = log.getOwnProcessInstance();
           String endTaskName = findEndActivityName(pi.getInternalId(), ctx);
           if (Strings.hasText(endTaskName)) {
               MutableBpmTask t = new MutableBpmTask();
               t.setProcessInstance(pi);
               t.setAssignee(user.getLogin());
               t.setOwner(user);
               t.setTaskName(endTaskName);
               t.setFinished(true);
               return t;
           }
           return null;
       }

       @Override
       public BpmTask getPastOrActualTask(final ProcessInstanceLog log, ProcessToolContext ctx) {
           final UserData user = log.getUser();
           final ProcessInstance pi = log.getOwnProcessInstance();
           final Calendar minDate = log.getEntryDate();
           final Set<String> taskNames = new HashSet<String>();
           if (log.getState() != null && Strings.hasText(log.getState().getName())) {
               taskNames.add(log.getState().getName());
           }

           Command<List<HistoryActivityInstance>> cmd = new Command<List<HistoryActivityInstance>>() {
               @Override
               public List<HistoryActivityInstance> execute(Environment environment) throws Exception {
                   AbstractQuery q = new AbstractQuery() {
                       @Override
                       protected void applyPage(Query query) {
                           query.setFirstResult(0);
                           query.setMaxResults(1);
                       }

                       @Override
                       protected void applyParameters(Query query) {
                           query.setString("userIds", user.getLogin());
                           query.setString("internalIds", log.getExecutionId());
                           query.setDate("minDate", minDate.getTime());
                           if (!taskNames.isEmpty()) {
                               query.setParameterList("taskName", taskNames);
                           }
                       }

                       @Override
                       public String hql() {
                           StringBuilder hql = new StringBuilder()
                                   .append("select act ")
                                   .append("from ")
                                   .append(HistoryTaskInstanceImpl.class.getName()).append(" as act, ")
                                   .append(HistoryProcessInstanceImpl.class.getName()).append(" as proc, ")
                                   .append(HistoryTaskImpl.class.getName()).append(" as task  ")
                                   .append(" where act.historyProcessInstance = proc ")
                                   .append(" and act.historyTask = task ")
                                   .append(" and act.endTime is not null ");
                           if (!taskNames.isEmpty()) {
                               hql.append(" and act.activityName in (:taskName) ");
                           }
                           hql.append(" and task.assignee = :userIds ")
                                   .append(" and task.endTime > :minDate ")
                                   .append(" and act.executionId = :internalIds ")
                                   .append(" order by act.endTime asc ");
                           return hql.toString();
                       }
                   };
                   return (List<HistoryActivityInstance>) q.execute(environment);
               }
           };

           List<HistoryActivityInstance> pastTasks = getProcessEngine(ctx).execute(cmd);
           if (!pastTasks.isEmpty()) {
               return collectHistoryActivity(pastTasks.get(0), pi, user, ctx);
           }
           else {
               List<BpmTask> actualTasks = findProcessTasks(pi, user.getLogin(), taskNames, ctx);
               if (!actualTasks.isEmpty()) {
                   return actualTasks.get(0);
               }
           }
           return null;
       }

       @Override
   	public List<BpmTask> findRecentTasks(Calendar minDate, Integer offset, Integer limit, ProcessToolContext ctx) {
   		List<BpmTask> recentTasks = new ArrayList<BpmTask>();
   		UserData user = getUser(ctx);
   		ResultsPageWrapper<ProcessInstance> recentInstances = ctx.getProcessInstanceDAO().getRecentProcesses(user, minDate, offset, limit);
   		Collection<ProcessInstance> instances = recentInstances.getResults();
   		for (ProcessInstance pi : instances) {
   			List<BpmTask> tasks = findProcessTasks(pi, user.getLogin(), ctx);
   			if (tasks.isEmpty()) {
   				BpmTask task = getMostRecentProcessHistoryTask(pi, user, minDate, ctx);
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

   	private BpmTask getMostRecentProcessHistoryTask(final ProcessInstance pi, final UserData user, final Calendar minDate, ProcessToolContext ctx) {
   		Command<List<HistoryActivityInstance>> cmd = new Command<List<HistoryActivityInstance>>() {
   			@Override
   			public List<HistoryActivityInstance> execute(Environment environment) throws Exception {
   				AbstractQuery q = new AbstractQuery() {
   					@Override
   					protected void applyPage(Query query) {
   						query.setFirstResult(0);
   						query.setMaxResults(1);
   					}
   					@Override
   					protected void applyParameters(Query query) {
   						query.setString("userIds", user.getLogin());
   						query.setString("internalIds", pi.getInternalId());
   						query.setDate("minDate", minDate.getTime());
   					}
   					@Override
   					public String hql() {
   						StringBuilder hql = new StringBuilder()
   						.append("select act ")
   						.append("from ")
   						.append(HistoryTaskInstanceImpl.class.getName()).append(" as act, ")
   						.append(HistoryProcessInstanceImpl.class.getName()).append(" as proc, ")
   						.append(HistoryTaskImpl.class.getName()).append(" as task  ")
   						.append(" where act.historyProcessInstance = proc ")
   						.append(" and act.historyTask = task ")
   						.append(" and act.endTime is not null ")
   						.append(" and task.assignee = :userIds ")
   						.append(" and task.endTime > :minDate ")
   						.append(" and proc.processInstanceId = :internalIds ")
   						.append(" order by act.endTime desc ");
   						return hql.toString();
   					}
   				};
   				return (List<HistoryActivityInstance>) q.execute(environment);
   			}
   		};

   		List<HistoryActivityInstance> tasks = getProcessEngine(ctx).execute(cmd);
           if (tasks.isEmpty()) {
               return null;
           }
           MutableBpmTask task = collectHistoryActivity(tasks.get(0), pi, user, ctx);
           String endTaskName = findEndActivityName(pi.getInternalId(), ctx);
           if (Strings.hasText(endTaskName)) {
               task.setTaskName(endTaskName);
           }
           return task;
   	}

   	@Override
   	public Integer getRecentTasksCount(Calendar minDate, ProcessToolContext ctx) {
   		int count = 0;
   		UserData user = getUser(ctx);
   		Collection<ProcessInstance> instances = ctx.getProcessInstanceDAO().getUserProcessesAfterDate(user, minDate);
   		for (ProcessInstance pi : instances) {
   			List<BpmTask> tasks = findProcessTasks(pi, user.getLogin(), ctx);
   			if (tasks.isEmpty() && getMostRecentProcessHistoryTask(pi, user, minDate, ctx) != null) {
   				count += 1;
   			}
   			else {
   				count += tasks.size();
   			}
   		}
   		return count;
   	}
   	
	@Override
	public int getTasksCount(ProcessToolContext ctx, String userLogin, QueueType... queueTypes) 
	{
		return ctx.getUserProcessQueueDAO().getQueueLength(userLogin, queueTypes);
	}

	@Override
	public int getTasksCount(ProcessToolContext ctx, String userLogin, Collection<QueueType> queueTypes) 
	{
		return ctx.getUserProcessQueueDAO().getQueueLength(userLogin, queueTypes);
	}
	
	@Override
	public int getFilteredTasksCount(ProcessInstanceFilter filter, ProcessToolContext ctx) 
	{
   		/* Initialize query */
   		BpmTaskFilterQuery taskFilterQuery = new BpmTaskFilterQuery(ctx);
   		
   		/* Queues filter do not have owner */
   		if(filter.getFilterOwner() != null)
   			taskFilterQuery.addUserLoginCondition(filter.getFilterOwner().getLogin());
   		
   		if(!filter.getQueueTypes().isEmpty())
   			taskFilterQuery.addQueueTypeCondition(filter.getQueueTypes());
   		
   		/* Add external conditions for process instance filter */
   		addExternalConditions(taskFilterQuery, filter);
   		
   		/* Get results count. No entities are loaded to memory using this */
   		return taskFilterQuery.getBpmTaskCount();
	}
   	
   	
	public List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, ProcessToolContext ctx)
	{
		return findFilteredTasks(filter, ctx, 0, 0);
	}
   	
	public List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, ProcessToolContext ctx, int offset, int maxResults)
   	{
   		/* Initialize query */
   		BpmTaskFilterQuery taskFilterQuery = new BpmTaskFilterQuery(ctx);
   		
   		/* Queues filter do not have owner */
   		if(filter.getFilterOwner() != null)
   			taskFilterQuery.addUserLoginCondition(filter.getFilterOwner().getLogin());
   		
   		if(!filter.getQueueTypes().isEmpty())
   			taskFilterQuery.addQueueTypeCondition(filter.getQueueTypes());
   		
   		/* Set limit for max results count */
   		taskFilterQuery.setMaxResultsLimit(maxResults);
   		taskFilterQuery.setResultsOffset(offset);

        if(filter.getExpression() != null && filter.getExpression().length() > 1)
            taskFilterQuery.setExpression(filter.getExpression());

        /* Set sort order */
        taskFilterQuery.setSortOrderCondition(filter.getSortOrderCondition());
        taskFilterQuery.setSortOrder(filter.getSortOrder());

   		/* Add external conditions for process instance filter */
   		addExternalConditions(taskFilterQuery, filter);
   		
		/* BpmTasks */
		List<BpmTask> result = taskFilterQuery.getBpmTasks();
   		
   		return result;
   	}
   	
   	/** Add additional conditions to query from process instance filter */
   	private void addExternalConditions(BpmTaskFilterQuery taskFilterQuery,ProcessInstanceFilter filter)
   	{
   		/* Prepare data for owner lists and not owner list */
   		Collection<String> ownerNames = new HashSet<String>();
   		for(UserData userData: filter.getOwners())
   			ownerNames.add(userData.getLogin());
   		
   		/* Add condition for task names if any exists */
   		if(!filter.getTaskNames().isEmpty())
   			taskFilterQuery.addTaskNamesCondtition(filter.getTaskNames());
   		
   		/* Add conidtion for created before date */
   		if(filter.getCreatedBefore() != null)
   			taskFilterQuery.addCreatedBeforeCondition(filter.getCreatedBefore());
   		
   		/* Add conidtion for created after date */
   		if(filter.getCreatedAfter() != null)
   			taskFilterQuery.addCreatedAfterCondition(filter.getCreatedAfter());
   		
   		if(!filter.getQueues().isEmpty())
   			taskFilterQuery.addQueuesCondition(filter.getQueues());
   		
   		/* Add condition for owner */
   		if(!ownerNames.isEmpty())
   			taskFilterQuery.addOwnerLoginsCondtition(ownerNames);
   	}
   	

   	protected ProcessEngine getProcessEngine(ProcessToolContext ctx) {
   		if (ctx instanceof ProcessToolContextImpl) {
   			ProcessEngine engine = ((ProcessToolContextImpl) ctx).getProcessEngine();
   			if (user != null && user.getLogin() != null) {
   				engine.setAuthenticatedUserId(user.getLogin());
   			}
   			return engine;
   		}
   		else {
   			throw new IllegalArgumentException(ctx + " not an instance of " + ProcessToolContextImpl.class.getName());
   		}
   	}

   	@Override
   	public Collection<ProcessQueue> getUserAvailableQueues(ProcessToolContext ctx) {  
   		Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);  
   		final List<String> names = keyFilter("name", configs);
   		if (names.isEmpty()) {
   			return new ArrayList<ProcessQueue>();  
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
   						return new StringBuilder()
   						.append("select new map(participant.groupId as groupId, count(task) as taskCount) ")
   						.append("from ")
   						.append(TaskImpl.class.getName())
   						.append(" as task ")
   						.append(", ")
   						.append(ParticipationImpl.class.getName())
   						.append(" as participant ")
   						.append("where participant.task=task ")
   						.append("and participant.type = 'candidate' ")
   						.append("and participant.groupId IN (:groupIds) ")
   						.append("and task.assignee is null ")
   						.append("group by participant.groupId")
   						.toString();
   					}
   				}.execute(environment);
   			}
   		};

   		List<Map> maps = getProcessEngine(ctx).execute(cmd);
   		Map<String, Long> counts = new HashMap<String, Long>();
   		for (Map m : maps) {
   			counts.put((String) m.get("groupId"), (Long) m.get("taskCount"));
   		}

   		for (ProcessQueue q : configs) {
   			q.setProcessCount(nvl(counts.get(q.getName()), (long) 0));
   		}
   		return configs;
   	}
   	
	@SuppressWarnings("unchecked")
	@Override
	public List<BpmTask> getQueueTasks(ProcessToolContext ctx, String queueName)
	{
		SQLQuery query = ctx.getHibernateSession().createSQLQuery(
				"select task.*, process.*, part.* from jbpm4_task task, pt_process_instance process, jbpm4_participation part " +
				"where process.internalid = task.execution_id_ and part.task_ = task.dbid_ " +
				"and part.groupid_ = '"+queueName+"'");
		
		query.addEntity("task", TaskImpl.class);
		query.addEntity("process", ProcessInstance.class);
		query.addEntity("part", ParticipationImpl.class);
		
		/* Get query results */
		List<Object[]> queueResults = query.list();
		
		List<BpmTask> result = new ArrayList<BpmTask>();
		
		BpmTaskFactory taskFactory = new BpmTaskFactory(ctx);
		
		/* Every row is one queue element with jbpm task as first column and process instance as second */
   		for(Object[] resultRow: queueResults)
   		{
   			
   			TaskImpl taskInstance = (TaskImpl)resultRow[0];
   			ProcessInstance processInstance = (ProcessInstance)resultRow[1];
   			
   			/* Map process and jbpm task to system's bpm task */
   			BpmTask task = taskFactory.create(taskInstance, processInstance);
   			
   			result.add(task);
   		}
   		
   		return result;
	}
	

   	@Override
   	public BpmTask assignTaskFromQueue(String queueName, ProcessToolContext ctx) {
   		return assignTaskFromQueue(queueName, null, ctx);
   	}

    @Override
    public BpmTask assignTaskFromQueue(final String queueName, BpmTask bpmTask, ProcessToolContext ctx) {
        Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
        final List<String> names = keyFilter("name", configs);
        if (!names.contains(queueName)) {
            throw new ProcessToolSecurityException("queue.no.rights", queueName);
        }

        ProcessEngine processEngine = getProcessEngine(ctx);

        final String taskId = bpmTask != null ? bpmTask.getInternalTaskId() : null;
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
                        query.setParameterList("groupIds", java.util.Collections.singleton(queueName));
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
                        if (taskId != null) {
                            hql.append("and task.id = :taskId");
                        }
                        return hql.toString();
                    }
                };
                return (List<Task>) q.execute(environment);
            }
        };
        List<Task> taskList = processEngine.execute(cmd);
        if (taskList.isEmpty()) {
            loger.warning("No tasks found in queue: " + queueName);
            return null;
        }
        Task task = taskList.get(0);
        Execution exec = processEngine.getExecutionService().findExecutionById(task.getExecutionId());
        String internalId = exec.getProcessInstance().getId();
        ProcessInstance pi = getProcessData(internalId, ctx);
        if (pi == null) {
            loger.warning("Process instance not found for instance id: " + internalId);
            return null;
        }

        Calendar snapshotDate = Calendar.getInstance();

        processEngine.getTaskService().assignTask(task.getId(), user.getLogin());
        task = processEngine.getTaskService().getTask(task.getId());
        if (!user.getLogin().equals(task.getAssignee())) {
            loger.warning("Task: + " + bpmTask.getExecutionId() + " not assigned to requesting user: " + user.getLogin());
            return null;
        }

        ProcessInstanceLog log = new ProcessInstanceLog();
        log.setLogType(ProcessInstanceLog.LOG_TYPE_CLAIM_PROCESS);

        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        bpmTask = collectTask(task, pi, ctx);
        log.setState(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(bpmTask));
        log.setEntryDate(snapshotDate);
        log.setEventI18NKey("process.log.process-assigned");
        log.setLogValue(queueName);
        log.setUser(findOrCreateUser(user, ctx));
        //log.setAdditionalInfo(pq.getDescription());
        log.setExecutionId(task.getExecutionId());
        log.setOwnProcessInstance(pi);
        pi.getRootProcessInstance().addProcessLog(log);

        if (!ProcessStatus.RUNNING.equals(pi.getStatus())) {
            pi.setStatus(ProcessStatus.RUNNING);
        }
        
		/* Inform queue manager about task assigne */
		ctx.getUserProcessQueueManager().onTaskAssigne(bpmTask);
		
        broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.ASSIGN_TASK, bpmTask, user));
        broadcastEvent(ctx, new ViewEvent(ViewEvent.Type.ACTION_COMPLETE));

        return bpmTask;
    }

   	private List<BpmTask> collectTasks(List<Task> tasks, final ProcessInstance pi, final ProcessToolContext ctx) {
   		
   		
   		return new Mapcar<Task, BpmTask>(tasks) {
   			@Override
   			public BpmTask lambda(Task x) {
   				return collectTask(x, pi, ctx);   
   			}
   		}.go();
   	}

   	private MutableBpmTask collectHistoryActivity(HistoryActivityInstance task, ProcessInstance pi, UserData user, ProcessToolContext ctx) {
   		MutableBpmTask t = new MutableBpmTask();
   		t.setProcessInstance(pi);
   		t.setAssignee(user.getLogin());
   		t.setOwner(user);
   		t.setTaskName(task.getActivityName());
   		t.setInternalTaskId(null);
   		t.setExecutionId(task.getExecutionId());
   		t.setCreateDate(task.getStartTime());
   		t.setFinishDate(task.getEndTime());
   		t.setFinished(task.getEndTime() != null);
   		return t;
   	}
   	
   	
   	

   	/** Get the last step name of the process */
    private String findEndActivityName(String internalId, ProcessToolContext ctx) 
    {
    	/* Get all history activities for given process and order it by the end date */
    	List<HistoryActivityInstance> activities = getProcessEngine(ctx).getHistoryService().createHistoryActivityInstanceQuery()
    			.processInstanceId(internalId)
    			.orderDesc(HistoryActivityInstanceQuery.PROPERTY_ENDTIME)
    			.list();
    	
    	/* Looking for specific activity, becouse if there is a for-each statment in process, there 
    	 * might be a problem with correct end task name
    	 */
        if (activities != null && !activities.isEmpty()) 
        {
        	for(HistoryActivityInstance activity: activities)
        	{
        		if(activity instanceof HistoryActivityInstanceImpl)
        		{
        			HistoryActivityInstanceImpl activityImpl = (HistoryActivityInstanceImpl)activity;
        			/* There are trasictions and xors, we are interested in only with tasks */
        			if(activityImpl.getType().equals("task"))
        				return activityImpl.getActivityName();
        		}
        	}
            
        }
        return null;
    }

    private MutableBpmTask collectTask(Task task, ProcessInstance pi, ProcessToolContext ctx) {
   		MutableBpmTask t = new MutableBpmTask();
   		t.setProcessInstance(pi);
   		t.setAssignee(task.getAssignee());
   		UserData ud = ctx.getUserDataDAO().loadUserByLogin(task.getAssignee());
   		if (ud == null) {
   			ud = new UserData();
   			ud.setLogin(task.getAssignee());
   		}
   		t.setOwner(ud);
   		t.setTaskName(task.getActivityName());
   		t.setInternalTaskId(task.getId());
   		t.setExecutionId(task.getExecutionId());
   		t.setCreateDate(task.getCreateTime());
   		t.setFinished(false);
   		return t;
   	}
    
    private MutableBpmTask collectTask(HistoryTask task, ProcessInstance pi, ProcessToolContext ctx) {
   		MutableBpmTask t = new MutableBpmTask();
   		t.setProcessInstance(pi);
   		t.setAssignee(task.getAssignee());
   		UserData ud = ctx.getUserDataDAO().loadUserByLogin(task.getAssignee());
   		if (ud == null) {
   			ud = new UserData();
   			ud.setLogin(task.getAssignee());
   		}
   		t.setOwner(ud);
   		t.setTaskName(task.getOutcome());
   		t.setInternalTaskId(task.getId());
   		t.setExecutionId(task.getExecutionId());
   		t.setCreateDate(task.getCreateTime());
   		t.setFinished(true);
   		return t;
   	}
    
    public BpmTask getHistoryTask(final String taskId, ProcessToolContext ctx)
    {
   		HistoryTask task = getProcessEngine(ctx).getHistoryService().
   				createHistoryTaskQuery().
   				taskId(taskId).
   				uniqueResult();
   		
   		if (task == null) {
   			return null;
   		}
   		return findProcessInstanceForTask(task, ctx);
    }

   	@Override
   	public BpmTask getTaskData(String taskId, ProcessToolContext ctx) 
   	{
   		Task task = getProcessEngine(ctx).getTaskService().getTask(taskId);
   		if (task == null) {
   			return null;
   		}
   		
   		return findProcessInstanceForTask(task, ctx);
   	}

   	@Override
   	public BpmTask getTaskData(String taskExecutionId, String taskName, ProcessToolContext ctx) {
   		List<Task> tasks = getProcessEngine(ctx).getTaskService().createTaskQuery()
   				.notSuspended()
   				.activityName(taskName)
   				.executionId(taskExecutionId)
   				.assignee(user.getLogin())
   				.page(0, 1)
   				.list();
   		if (tasks.isEmpty()) {
   			log.warning("Task " + taskExecutionId + " not found");
   			return null;
   		}
   		List<BpmTask> bpmTasks = findProcessInstancesForTasks(tasks, ctx);
   		return bpmTasks.isEmpty() ? null : bpmTasks.get(0);
   	}

   	@Override
   	public BpmTask refreshTaskData(BpmTask task, ProcessToolContext ctx) {
   		MutableBpmTask bpmTask = task instanceof MutableBpmTask ? (MutableBpmTask) task : new MutableBpmTask(task);
   		bpmTask.setProcessInstance(getProcessData(task.getProcessInstance().getInternalId(), ctx));
   		List<Task> tasks = getProcessEngine(ctx).getTaskService().createTaskQuery()
   				.notSuspended()
   				.activityName(task.getTaskName())
   				.executionId(task.getExecutionId())
   				.assignee(user.getLogin())
   				.page(0, 1)
   				.list();
   		if (tasks.isEmpty()) {
   			loger.warning("Task " + task.getExecutionId() + " not found");
   			bpmTask.setFinished(true);
   		}
   		return bpmTask;
   	}

       @Override
       public List<BpmTask> findProcessTasks(ProcessInstance pi,
                                             final String userLogin,
                                             final Set<String> taskNames,
                                             ProcessToolContext ctx) {
           final Map<String, Execution> executions = getActiveExecutions(pi.getInternalId(), ctx);
           if (executions.isEmpty()) {
               return new ArrayList<BpmTask>();
           }
           Command<List<Task>> cmd = new Command<List<Task>>() {
               @Override
               public List<Task> execute(Environment environment) throws Exception {
                   AbstractQuery q = new AbstractQuery() {
                       @Override
                       protected void applyPage(Query query) {
                           query.setFirstResult(0);
                       }

                       @Override
                       protected void applyParameters(Query query) {
                           query.setParameterList("executionId", executions.keySet());
                           if (userLogin != null) {
                               query.setParameterList("userLogin", java.util.Collections.singleton(userLogin));
                           }
                           if (taskNames != null && !taskNames.isEmpty()) {
                               query.setParameterList("taskNames", taskNames);
                           }
                       }

                       @Override
                       public String hql() {
                           StringBuilder hql = new StringBuilder();
                           hql.append("select task ");
                           hql.append("from ");
                           hql.append(TaskImpl.class.getName());
                           hql.append(" as task ");
                           hql.append("where executionId in (:executionId) ");
                           hql.append("and task.assignee ");
                           hql.append(userLogin != null ? " in (:userLogin) " : " is not null ");
                           if (taskNames != null && !taskNames.isEmpty()) {
                               hql.append("and task.name in (:taskNames) ");
                           }
                           return hql.toString();
                       }
                   };
                   return (List<Task>) q.execute(environment);
               }
           };
           List<Task> tasks = getProcessEngine(ctx).execute(cmd);
           return collectTasks(tasks, pi, ctx);
       }

       @Override
       public List<BpmTask> findProcessTasks(ProcessInstance pi, final String userLogin, ProcessToolContext ctx) {
           return findProcessTasks(pi, userLogin, null, ctx);
       }
 
       @Override
   	public List<BpmTask> findProcessTasks(ProcessInstance pi, ProcessToolContext ctx) {
   		return findProcessTasks(pi, null, ctx);
   	}

   	@Override
   	public boolean isProcessOwnedByUser(final ProcessInstance processInstance, ProcessToolContext ctx) {
   		final Map<String, Execution> executions = getActiveExecutions(processInstance.getInternalId(), ctx);
   		if (executions.isEmpty()) {
   			return false;
   		}
   		Command<List<Task>> cmd = new Command<List<Task>>() {
   			@Override
   			public List<Task> execute(Environment environment) throws Exception {
   				AbstractQuery q = new AbstractQuery() {
   					@Override
   					protected void applyPage(Query query) {
   						query.setFirstResult(0);
   						query.setMaxResults(1);
   					}

   					@Override
   					protected void applyParameters(Query query) {
   						query.setParameterList("executionId", executions.keySet());
   						query.setParameterList("assignee", java.util.Collections.singleton(user.getLogin()));
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
   		List<Task> tasks = getProcessEngine(ctx).execute(cmd);
   		return !tasks.isEmpty();
   	}

   	@Override
   	public List<BpmTask> findUserTasks(Integer offset, Integer limit, final ProcessToolContext ctx) {
   		
   	 long start = System.currentTimeMillis();
   		if(limit== null){
   			
   			limit=DEFAULT_LIMIT_VALUE;
   		}
   		if(offset== null){
   			offset=DEFAULT_OFFSET_VALUE;
   			
   		}
   		List<Task> tasks = getProcessEngine(ctx).getTaskService().createTaskQuery()
   				.notSuspended()
   				.assignee(user.getLogin())
   				.page(offset, limit)
   				.list();
   		 List<BpmTask> processInstancesForTasks = findProcessInstancesForTasks(tasks, ctx);
   		 
   		 long duration = System.currentTimeMillis() - start;
			log.severe("findUserTasks: " +  duration);
   		 
   		 return processInstancesForTasks;
   	}
   	
   	private BpmTask findProcessInstanceForTask(HistoryTask task, final ProcessToolContext ctx)
   	{
   		ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(task.getExecutionId());
   		
   		return collectTask(task, processInstance, ctx);
   	}
   	
   	private BpmTask findProcessInstanceForTask(Task task, final ProcessToolContext ctx)
   	{
   		ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(task.getExecutionId());
   		
   		return collectTask(task, processInstance, ctx);
   	}

   	private List<BpmTask> findProcessInstancesForTasks(List<Task> tasks, final ProcessToolContext ctx) {
   		Map<String, List<Task>> tasksByProcessId = pl.net.bluesoft.util.lang.Collections.group(tasks, new Transformer<Task, String>() {
               @Override
               public String transform(Task task) {
                   Execution exec = getProcessEngine(ctx).getExecutionService().findExecutionById(task.getExecutionId());
                   return exec.getProcessInstance().getId();
               }
           });
   		final Map<String, ProcessInstance> instances = ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(tasksByProcessId.keySet());
   		final List<BpmTask> result = new ArrayList<BpmTask>();
		for (Map.Entry<String, List<Task>> entry : tasksByProcessId.entrySet()) {
			final String processId = entry.getKey();
			List<Task> processTasks = entry.getValue();

   			result.addAll(new Mapcar<Task, BpmTask>(processTasks) {
   				@Override
   				public BpmTask lambda(Task task) {
   					ProcessInstance pi = instances.get(processId);
   					if (pi == null) {
   						loger.warning("process " + processId + " not found");
   						return null;
   					}
   					return collectTask(task, pi, ctx);
   				}
   			}.go());
   		}
   		java.util.Collections.sort(result, new Comparator<BpmTask>() {
               @Override
               public int compare(BpmTask o1, BpmTask o2) {
                   return o2.getCreateDate().compareTo(o1.getCreateDate());
               }
           });
   		return result;
   	}

   	@Override
   	public List<BpmTask> findUserTasks(final ProcessInstance processInstance, ProcessToolContext ctx) {
   		final Map<String, Execution> executions = getActiveExecutions(processInstance.getInternalId(), ctx);
   		if (executions.isEmpty()) {
   			return new ArrayList<BpmTask>();
   		}
   		Command<List<Task>> cmd = new Command<List<Task>>() {
   			@Override
   			public List<Task> execute(Environment environment) throws Exception {
   				AbstractQuery q = new AbstractQuery() {
   					@Override
   					protected void applyPage(Query query) {
   						query.setFirstResult(0);
   					}

   					@Override
   					protected void applyParameters(Query query) {
   						query.setParameterList("executionId", executions.keySet());
   						query.setParameterList("assignee", java.util.Collections.singleton(user.getLogin()));
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
   		List<Task> tasks = getProcessEngine(ctx).execute(cmd);
   		return collectTasks(tasks, processInstance, ctx);
   	}

  
   	
   	
	@Override
    public BpmTask performAction(ProcessStateAction action, BpmTask task, ProcessToolContext ctx) 
   	{
           BpmTask bpmTask = getTaskData(task.getInternalTaskId(), ctx);

           
           if (bpmTask == null || bpmTask.isFinished()) 

               return bpmTask;
           
           ProcessInstance processInstance = bpmTask.getProcessInstance();
           


        		   
           
           
           ProcessInstanceLog log = addActionLogEntry(action, task, ctx);
           Map<String, Object> vars = new HashMap<String, Object>();
           vars.put("ACTION", action.getBpmName());
           processInstance.setSimpleAttribute("ACTION", action.getBpmName());
           List<String> outgoingTransitionNames = getOutgoingTransitionNames(task.getInternalTaskId(), ctx);

           ProcessEngine processEngine = getProcessEngine(ctx);

           Task bpmTaskInstance = processEngine.getTaskService().getTask(task.getInternalTaskId());
           String executionId = bpmTaskInstance.getExecutionId();


           Set<String> taskIdsBeforeCompletion = new HashSet<String>();
           pl.net.bluesoft.util.lang.Collections.collect(findProcessTasks(processInstance, ctx), new Transformer<BpmTask, String>() {
               @Override
               public String transform(BpmTask obj) {
                   return obj.getInternalTaskId();  
               }
           }, taskIdsBeforeCompletion);
           
           
           /** Add simple attributes to process variable */
           for(ProcessInstanceAttribute attr: processInstance.getProcessAttributes())
        	   if(attr instanceof ProcessInstanceSimpleAttribute)
        	   {
        		   ProcessInstanceSimpleAttribute simple = (ProcessInstanceSimpleAttribute)attr;
        		   vars.put(simple.getKey(), simple.getValue());
        	   }

           
           
          if (outgoingTransitionNames.size() == 1)
              processEngine.getTaskService().completeTask(task.getInternalTaskId(), outgoingTransitionNames.get(0), vars); //BPMN2.0 style, decision is taken on the XOR gateway
          else
              processEngine.getTaskService().completeTask(task.getInternalTaskId(), action.getBpmName(), vars);
          
          /* Remove all task tokens, if any exists */
          
			/* Delete all tokens for this taskId */
          tokenService.deleteTokensByTaskId(Long.parseLong(task.getInternalTaskId()));
          
          
          broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.TASK_FINISHED, task, user));
          
		   /* Inform queue manager about task finish and process state change */
		   ctx.getUserProcessQueueManager().onTaskFinished(task);

          String processState = getProcessState(processInstance, ctx);
          
          /* Check if new subProcess is created */
          boolean startsSubprocess = updateSubprocess(processInstance, executionId, ctx);

          fillProcessAssignmentData(processEngine, processInstance, ctx);
          processInstance.setState(processState);
          if (startsSubprocess == false && processState == null && processInstance.getRunning() && !isProcessRunning(processInstance.getInternalId(), ctx)) {
              processInstance.setRunning(false);
          }
          
          
          
           if (log.getUserSubstitute() == null)
               broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.SIGNAL_PROCESS, bpmTask, user));
           else
               broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.SIGNAL_PROCESS, bpmTask, log.getUserSubstitute()));

           if (Strings.hasText(action.getAssignProcessStatus())) {
               String processStatus = action.getAssignProcessStatus();
               ProcessStatus ps = processStatus.length() == 1 ? ProcessStatus.fromChar(processStatus.charAt(0)) : ProcessStatus.fromString(processStatus);
               processInstance.setStatus(ps);
           } 
           else 
           {
        	   boolean isProcessRunning = processInstance.isProcessRunning();
        	   //boolean isProcessRunning = isProcessRunning(pi.getInternalId(), ctx);
        	   
        	   /* Process is not running and no new suprocesses are created, so process should
        	    * be finished by now
        	    */
        	   if(!isProcessRunning && !startsSubprocess)
        	   {
        		   broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.END_PROCESS, bpmTask, user));
        		   
        		   /* Inform queue manager about process ending. Only main process is stored */
        		   if(!processInstance.isSubprocess())
        			   ctx.getUserProcessQueueManager().onProcessFinished(processInstance, bpmTask);
        		   
        		   processInstance.setStatus(ProcessStatus.FINISHED);
        	   }
        	   
        	   /* Process is running or is halted, but new subprocess are created */
        	   else if(!isProcessRunning && startsSubprocess)  
        	   {
        		   broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.PROCESS_HALTED, bpmTask, user));
        		   
        		   /* Inform queue manager about process halt */
        		   ctx.getUserProcessQueueManager().onProcessHalted(processInstance, bpmTask);
        		   
        		   processInstance.setStatus(ProcessStatus.RUNNING);
        	   }
        	   else 
        	   {
        		   processInstance.setStatus(ProcessStatus.RUNNING);
        	   }
           }
           
           ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);

           BpmTask userTask = null;

           BpmTask autoSkipTask = null;
           
           /* Is process finished */
           boolean isProcessFinished = processInstance.getStatus().equals(ProcessStatus.FINISHED);
           boolean isSubProcess = processInstance.getParent() != null;

           List<BpmTask> tasksAfterCompletion = null;
           if(startsSubprocess && processInstance.getChildren() != null) {
	           for(ProcessInstance child : processInstance.getChildren()) {
	        	   tasksAfterCompletion = findProcessTasks(child, ctx);
	        	   if(tasksAfterCompletion != null && tasksAfterCompletion.size() > 0)
	        		   break;
	           }
           }
           if(tasksAfterCompletion == null || tasksAfterCompletion.size() == 0) {
               tasksAfterCompletion = findProcessTasks(processInstance, ctx);
           }
           if(processInstance.getParent() != null && (tasksAfterCompletion == null || tasksAfterCompletion.size() == 0)) {
               tasksAfterCompletion = findProcessTasks(processInstance.getParent(), ctx);
           }
           if(tasksAfterCompletion != null) {
	           for (BpmTask createdTask : tasksAfterCompletion) {
	               if (!taskIdsBeforeCompletion.contains(createdTask.getInternalTaskId())) 
	               {
	                   broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.ASSIGN_TASK, createdTask, user));
	                   
	        		   /* Inform queue manager about task assigne */
	        		   ctx.getUserProcessQueueManager().onTaskAssigne(createdTask);
	        		           		   
	        		   assignTokens(ctx, createdTask);
	               }
	               if (Lang.equals(user.getLogin(), createdTask.getOwner().getLogin())) {
	                   userTask = createdTask;
	               }
	               if(createdTask.getTaskName().toLowerCase().startsWith(AUTO_SKIP_TASK_NAME_PREFIX.toLowerCase())) {
	            	   autoSkipTask = createdTask;
	               }
	           }
           }
           
           if(autoSkipTask != null) {
        	   ProcessStateAction skipAction = new ProcessStateAction();
        	   skipAction.setBpmName(AUTO_SKIP_ACTION_NAME);
        	   return performAction(skipAction, autoSkipTask, ctx);
           }
           
           /* Task assigned to queue */
           if (userTask == null) 
           {
        	   /* Process is finished, ask about parent process queues */
        	   if(isProcessFinished && isSubProcess)
        		   processInstance = processInstance.getParent();
        	   
        	   /* Get task assigned to queues */
        	   Collection<BpmTask> queueTasks = getProcessTaskInQueues(ctx, processInstance);
        	   
        	   for(BpmTask queueTask: queueTasks)
        	   {
        		   MutableBpmTask mutableTask = new MutableBpmTask(queueTask);
        		   mutableTask.setAssignee(task.getAssignee());
        		   mutableTask.setProcessInstance(processInstance);
        		   
        		   /* Inform queue manager about task assigne */
        		   ctx.getUserProcessQueueManager().onQueueAssigne(mutableTask);
        	   }
        	   
               MutableBpmTask mutableTask = new MutableBpmTask(task);
               mutableTask.setFinished(true);
               mutableTask.setProcessInstance(processInstance);
               userTask = mutableTask;
           }

           broadcastEvent(ctx, new ViewEvent(ViewEvent.Type.ACTION_COMPLETE));
           
           return userTask;
           
           
       }
   	

	private void assignTokens(ProcessToolContext ctx, BpmTask userTask)
	{
		/* Do not assigne tokens to autoskip steps */
		if(userTask.isAutoSkip())
			return;
		
		ProcessStateConfiguration stateConfiguration = ctx.getProcessDefinitionDAO()
		.getProcessStateConfiguration(userTask);
		   
		Boolean isAccessibleByToken = stateConfiguration.getEnableExternalAccess(); 
		   
		/* Step is accessible by token, generate one */
		if(isAccessibleByToken != null && isAccessibleByToken)
			generateAccessToken(ctx, userTask);
	}
	
	
	private void generateAccessToken(ProcessToolContext ctx, BpmTask userTask)
	{
		log.log(Level.INFO, "Generate token [Thread: ]"+Thread.currentThread().getId()+"]");
		try
		{
			/* Generate new token */
			ProcessStateConfiguration currentState = userTask.getCurrentProcessStateConfiguration();
			
			/* No current state, abort */
			if(currentState == null)
				return;
			
			/* For each action, create token */
			for(ProcessStateAction action: currentState.getActions())
			{
				/* Action is not accessible by external token. Skip it */
				if(action.getHideForExternalAccess())
					continue;
				
				AccessToken token = accessTokenFactory.create(userTask, action.getBpmName());
				log.log(Level.INFO, "Token generated: "+token.getToken());
				
				/* Save token */
				ctx.getHibernateSession().saveOrUpdate(token);
			}
			ctx.getHibernateSession().flush();
		}
		catch(Exception ex)
		{
			log.log(Level.SEVERE, "Problem during token generation", ex);
		}
		
	}

    @Override
   	public boolean isProcessRunning(String internalId, ProcessToolContext ctx) {
   		if (internalId == null) {
   			return false;
   		}
   		ExecutionService service = getProcessEngine(ctx).getExecutionService();
   		org.jbpm.api.ProcessInstance processInstance = service.findProcessInstanceById(internalId);
   		return processInstance != null && !processInstance.isEnded();
   	}

    @Override
   	protected ProcessInstance startProcessInstance(ProcessDefinitionConfig config, String externalKey, ProcessToolContext ctx, ProcessInstance pi) {
   		final ExecutionService execService = getProcessEngine(ctx).getExecutionService();
   		Map vars = new HashMap();
   		vars.put("processInstanceId", String.valueOf(pi.getId()));
   		vars.put("initiator", user.getLogin());

   		org.jbpm.api.ProcessInstance instance = execService.startProcessInstanceByKey(config.getBpmDefinitionKey(), vars, externalKey);
   		pi.setInternalId(instance.getId());

   		
   		return pi;
   	}

   	@Override
   	public ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx) {
   		ProcessToolJbpmSession session = new ProcessToolJbpmSession(user, roleNames, ctx);
   		session.substitutingUser = this.user;
   		session.substitutingUserEventBusManager = this.eventBusManager;
   		return session;
   	}

   	@Override
   	public void assignTaskToUser(ProcessToolContext ctx, String taskId, String userLogin) {
   		ProcessEngine processEngine = getProcessEngine(ctx);
   		processEngine.getTaskService().assignTask(taskId, userLogin);
   		BpmTask taskData = getTaskData(taskId, ctx); 
   		UserData newUser = new UserData();  
   		newUser.setLogin(userLogin); 
   		UserData loadOrCreateUser = loadOrCreateUser(ctx,newUser);
   		broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.ASSIGN_TASK, taskData, loadOrCreateUser));
   		broadcastEvent(ctx, new BpmEvent(BpmEvent.Type.ASSIGN_TASK, taskData, user));
		ctx.getUserProcessQueueManager().onTaskAssigne(taskData);

   	}

   	protected Map<String, Execution> getActiveExecutions(String internalId, ProcessToolContext ctx) {
   		Execution exec = getProcessEngine(ctx).getExecutionService().findExecutionById(internalId);
   		if (exec == null) {
   			loger.warning("Unable to find execution by process internal id: " + internalId);
   			return new HashMap<String, Execution>();
   		}
   		Collection<Execution> executions = getActiveExecutions(exec, new Predicate<Execution>() {
               @Override
               public boolean apply(Execution exec) {
                   return Execution.STATE_ACTIVE_ROOT.equals(exec.getState()) || Execution.STATE_ACTIVE_CONCURRENT.equals(exec.getState());
               }
           });
   		return pl.net.bluesoft.util.lang.Collections.transform(executions, new Transformer<Execution, String>() {
               @Override
               public String transform(Execution obj) {
                   return obj.getId();
               }
           });
   	}

   	private Collection<Execution> getActiveExecutions(Execution exec, Predicate<Execution> predicate) {
   		Collection<Execution> result = new ArrayList<Execution>();
   		if (predicate.apply(exec)) {
   			result.add(exec);
   		}
   		else {
   			Collection<? extends Execution> executions = exec.getExecutions();
   			if (executions != null && !executions.isEmpty()) {
   				for (Execution e : executions) {
   					result.addAll(getActiveExecutions(e, predicate));
   				}
   			}
   		}
   		return result;
   	}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<String> getOutgoingTransitionNames(String internalId, ProcessToolContext ctx) {
        ProcessEngine engine = getProcessEngine(ctx);
        return new ArrayList<String>(engine.getTaskService().getOutcomes(internalId));
    }

    public boolean updateSubprocess(final ProcessInstance parentPi, String executionId, ProcessToolContext ctx) {
        ProcessEngine engine = getProcessEngine(ctx);
        ExecutionService executionService = engine.getExecutionService();
		Execution jbpmPi = executionService.findExecutionById(executionId);
        if(jbpmPi != null){
	        Execution subprocess = jbpmPi.getSubProcessInstance();
	        if(subprocess != null){
	        	ctx.getHibernateSession().refresh(subprocess);
	        	
	        	if (ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(subprocess.getId()) == null) 
	        	{
					String processDefinitionId = subprocess.getProcessDefinitionId().replaceFirst("-\\d+$", "");
					ProcessDefinitionConfig config = ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(
							processDefinitionId);

					/* Create new instance of parent process' subprocess */
					ProcessInstance subProcessInstance = createSubprocessInstance(config, ctx, parentPi, "parent_process", subprocess.getId());
						
					long subPiId = ctx.getProcessInstanceDAO().saveProcessInstance(subProcessInstance);
					
					executionService.createVariable(subprocess.getId(), "processInstanceId", String.valueOf(subPiId),
							false);
					
					return true;
				}
	        }
        }
        return false;
    }



	public List<String> getOutgoingTransitionDestinationNames(String internalId, ProcessToolContext ctx) {
        ProcessEngine engine = getProcessEngine(ctx);
        org.jbpm.api.ProcessInstance pi = engine.getExecutionService().findProcessInstanceById(internalId);
        final ExecutionImpl execution = (ExecutionImpl) pi.getProcessInstance();
        final List<String> transitionNames = new ArrayList<String>();
        engine.execute(new Command() {
            public Object execute(Environment env) {
                for (Transition transition : execution.getActivity().getOutgoingTransitions()) {
                    transitionNames.add(transition.getDestination().getName());
                }
                return null;
            }
        });

        return transitionNames;
    }

    private void fillProcessAssignmentData(final ProcessEngine processEngine, final ProcessInstance pi, ProcessToolContext ctx) {
        Set<String> assignees = new HashSet<String>();
        Set<String> queues = new HashSet<String>();
        TaskService taskService = processEngine.getTaskService();
        List<BpmTask> processTasks = findProcessTasks(pi, null, null, ctx);
        for (BpmTask t : processTasks) {
            if (t.getAssignee() != null) {
                assignees.add(t.getAssignee());
            } else { //some optimization could be possible
                for (Participation participation : taskService.getTaskParticipations(t.getInternalTaskId())) {
                    if ("candidate".equals(participation.getType())) {
                        queues.add(participation.getGroupId());
                    }
                }
            }
        }
        pi.setActiveTasks(processTasks.toArray(new BpmTask[processTasks.size()]));
        pi.setAssignees(assignees.toArray(new String[assignees.size()]));
        pi.setTaskQueues(queues.toArray(new String[queues.size()]));
    }

    private ProcessInstanceLog addActionLogEntry(ProcessStateAction action, BpmTask task, ProcessToolContext ctx) {
        ProcessStateConfiguration state = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task);

        ProcessInstanceLog log = new ProcessInstanceLog();
        if(AUTO_SKIP_ACTION_NAME.toLowerCase().equals(action.getBpmName().toLowerCase()))
        	return log;
        
        log.setLogType(ProcessInstanceLog.LOG_TYPE_PERFORM_ACTION);
        log.setState(state);
        log.setEntryDate(Calendar.getInstance());
        log.setEventI18NKey("process.log.action-performed");
        log.setLogValue(action.getBpmName());
        log.setAdditionalInfo(nvl(action.getLabel(), action.getDescription(), action.getBpmName()));
        log.setUser(findOrCreateUser(user, ctx));
        log.setUserSubstitute(getSubstitutingUser(ctx));
        log.setExecutionId(task.getExecutionId());
        log.setOwnProcessInstance(task.getProcessInstance());
        task.getProcessInstance().getRootProcessInstance().addProcessLog(log);
        return log;
    }
    @Override
    public void adminCancelProcessInstance(ProcessInstance pi) {
        loger.severe("User: " + user.getLogin() + " attempting to cancel process: " + pi.getInternalId());
        long start = System.currentTimeMillis();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        pi = getProcessData(pi.getInternalId(), ctx);
        ProcessEngine processEngine = getProcessEngine(ctx);
        processEngine.getExecutionService().endProcessInstance(pi.getInternalId(), "admin-cancelled");
        fillProcessAssignmentData(processEngine, pi, ctx);
        pi.setRunning(false);
        pi.setState(null);
        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        long duration = System.currentTimeMillis() - start;
		log.severe("adminCancelProcessInstance: " +  duration);
        loger.severe("User: " + user.getLogin() + " has cancelled process: " + pi.getInternalId());

    }

        @Override
        public void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin) {
            loger.severe("User: " + user.getLogin() + " attempting to reassign task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to user: " + userLogin);

            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            pi = getProcessData(pi.getInternalId(), ctx);
            ProcessEngine processEngine = getProcessEngine(ctx);
            TaskService ts = processEngine.getTaskService();
            Task task = ts.getTask(bpmTask.getInternalTaskId());
            if (nvl(userLogin,"").equals(nvl(task.getAssignee(),""))) {
                loger.severe("User: " + user.getLogin() + " has not reassigned task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " as the user is the same: " + userLogin);
                return;
            }
            //this call should also take care of swimlanes
            ts.assignTask(bpmTask.getInternalTaskId(), userLogin);
            fillProcessAssignmentData(processEngine, pi, ctx);
            loger.info("Process.running:" + pi.getRunning());
            ctx.getProcessInstanceDAO().saveProcessInstance(pi);
            loger.severe("User: " + user.getLogin() + " has reassigned task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to user: " + userLogin);

        }

        @Override
        public void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action) {
            loger.severe("User: " + user.getLogin() + " attempting to complete task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to outcome: " + action);
            performAction(action, bpmTask, ProcessToolContext.Util.getThreadProcessToolContext());
            loger.severe("User: " + user.getLogin() + " has completed task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to outcome: " + action);

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
            List<User> users = getProcessEngine(ProcessToolContext.Util.getThreadProcessToolContext()).execute(cmd);
            List<String> res = new ArrayList<String>();
            for (User u : users) {
                res.add(u.getId());
            }
            java.util.Collections.sort(res);
            return res;

        }  

        @Override
        public List<GraphElement> getProcessHistory(final ProcessInstance pi) {
            ProcessEngine processEngine = getProcessEngine(ProcessToolContext.Util.getThreadProcessToolContext());
            HistoryService service = processEngine.getHistoryService();
             
            HistoryActivityInstanceQuery createHistoryActivityInstanceQuery = service.createHistoryActivityInstanceQuery();
            HistoryActivityInstanceQuery activityInstanceQuery = createHistoryActivityInstanceQuery.processInstanceId(pi.getInternalId());
            List<HistoryActivityInstance> list = activityInstanceQuery.list();
            
            
            
            
   
            Map<String,GraphElement> processGraphElements = parseProcessDefinition(pi);

            
            
            List<GraphElement> transitionaArcsList = new ArrayList<GraphElement>();
            Collection<GraphElement> pge = processGraphElements.values() ;
            
            
            for (GraphElement ge : pge) {	
            	  if( ge instanceof TransitionArc){
            		  TransitionArc ta = (TransitionArc)ge;
            			  transitionaArcsList.add(ta);
              		
          		}
            }
            
    		
            
            ArrayList<GraphElement> res = new ArrayList<GraphElement>();
            for (HistoryActivityInstance hpi : list) {
                loger.fine("Handling: " + hpi.getActivityName());
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
                    
                   
                    
                    ArrayList<StateNode> arrayList = new ArrayList<StateNode>();
                    Collection<GraphElement> values = processGraphElements.values();
                    for (GraphElement graphElement : values) {
						if(graphElement instanceof StateNode){
							arrayList.add((StateNode) graphElement);
						}
                    	
                    	
					}
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
                    loger.severe("Unsupported entry: " + hpi);
                }
            }
            
           addJoinAndForkElements(res,processGraphElements);
            
            
            HistoryProcessInstanceQuery historyProcessInstanceQuery = processEngine.getHistoryService()
                    .createHistoryProcessInstanceQuery().processInstanceId(pi.getInternalId());
            HistoryProcessInstance historyProcessInstance = historyProcessInstanceQuery.uniqueResult();
            if (historyProcessInstance != null && historyProcessInstance.getEndActivityName() != null) {
                StateNode sn = (StateNode) processGraphElements.get(historyProcessInstance.getEndActivityName());
                if (sn != null) {
                    StateNode e = sn.cloneNode();
                    e.setUnfinished(true);
                    res.add(e);
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
                String[] nodeTypes = new String[]{"start", "end", "java", "task", "decision","join","fork"};
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
                            sn.setNodeType(nodeType);
                            String name = node.getAttribute("name"); 
                            sn.setLabel(name);
                            res.put(name, sn);
                            if ("start".equals(nodeType)) {
                                res.put("__AWF__start_node", sn);
                            }
                            loger.fine("Found node" + name + ": " + x + "," + y + "," + w + "," + h);
                        } catch (Exception e) {
                            loger.log(Level.SEVERE, e.getMessage(), e);
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
                                loger.severe("Start node " + startNodeName +
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
                                    loger.severe("End node " + to + " has not been localized for transition " + name +
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

                                    double a;//remember about vertical line
                                    double b;

                                    endX = arc.getPath().get(1).getX();
                                    endY = arc.getPath().get(1).getY();
                                    if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
                                        if (endY > startNode.getY()+startNode.getHeight()) { //below
                                            startY = startNode.getY()+startNode.getHeight();
                                        } else {
                                            startY = startNode.getY();
                                        }
                                    } else {
                                        a = ((double)(startY-endY))/((double)(startX - endX));
                                        b = (double)startY - (double)startX*a;
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
                                    }
                                    arc.getPath().get(0).setX(startX);
                                    arc.getPath().get(0).setY(startY);

                                    endX = arc.getPath().get(arc.getPath().size()-1).getX();
                                    endY = arc.getPath().get(arc.getPath().size()-1).getY();
                                    startX = arc.getPath().get(arc.getPath().size()-2).getX();
                                    startY = arc.getPath().get(arc.getPath().size()-2).getY();
                                    if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
                                       if (startY > endNode.getY()+endNode.getHeight()) { //below
                                           endY = endNode.getY()+endNode.getHeight();
                                       } else {
                                           endY = endNode.getY();
                                       }
                                    } else {
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
                                    }
                                    arc.getPath().get(arc.getPath().size()-1).setX(endX);
                                    arc.getPath().get(arc.getPath().size()-1).setY(endY);
                                    arc.setDestination(to);
                                    arc.setSource(startNodeName);

                                    res.put(startNodeName + "_" + name,arc);
                                    if ("start".equals(nodeType)) {
                                        res.put("__AWF__start_transition_to_" + to, arc);
                                    }
                                    if (transitions.getLength() == 1) {
                                        res.put("__AWF__default_transition_" + startNodeName, arc);
                                    }
                                } else {
                                    loger.severe("No 'g' attribute for transition "+ name +
                                                   " of node " + startNodeName + ", skipping transition drawing.");
                                }
                            }

                        } catch (Exception e) {
                            loger.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return res;
        }
        
        
        
    	private void addJoinAndForkElements(ArrayList<GraphElement> historyGraph,
    			Map<String, GraphElement> originalGraphMap) {

    		Map<String, StateNode> joinList = new HashMap<String, StateNode>();
    		Map<String, StateNode> forkList = new HashMap<String, StateNode>();

    		Map<GraphElement, List<TransitionArc>> joinWithTransitions = new HashMap<GraphElement, List<TransitionArc>>();
    		Map<GraphElement, List<TransitionArc>> forkWithTransitions = new HashMap<GraphElement, List<TransitionArc>>();

    		List<TransitionArc> transitionArcList = new ArrayList<TransitionArc>();

    		Collection<GraphElement> originalGraph = originalGraphMap.values();

    		for (GraphElement originalGraphElement : originalGraph) {

    			fillListWithAllPosibleTransitionArc(originalGraphElement,
    					transitionArcList);
    			fillListsWithAllPosibleJoinAndForkNodes(originalGraphElement,
    					joinList, forkList);

    		}

    		if (joinList.size() > 0 || forkList.size() > 0) {

    			for (TransitionArc transitionArc : transitionArcList) {
    				relateNodesWithTransitionArcs(forkList, transitionArc,
    						forkWithTransitions);
    				relateNodesWithTransitionArcs(joinList, transitionArc,
    						joinWithTransitions);
    			}

    			Set<GraphElement> forks = forkWithTransitions.keySet();
    			ArrayList<String> tasksNamesFromHistoryAsArray = getTasksNamesFromHistoryAsArray(historyGraph);

    			for (GraphElement fork : forks) {
    				List<TransitionArc> forkTransitions = forkWithTransitions
    						.get(fork);
    				if (isForkOutgoingsExistsInHistoryGraph(forkTransitions, fork,
    						tasksNamesFromHistoryAsArray)) {
    					historyGraph.addAll(forkTransitions);
    					historyGraph.add(fork);
    				}

    			}

    			Set<GraphElement> joins = joinWithTransitions.keySet();
    			for (GraphElement join : joins) {
    				List<TransitionArc> list = joinWithTransitions.get(join);
    				if (isJoinOutgoingWasMissingInHistoryGraph(list,
    						tasksNamesFromHistoryAsArray, historyGraph)) {
    					historyGraph.add(join);
    				}

    			}
    		}

    	}

    	private void fillListWithAllPosibleTransitionArc(
    			GraphElement originalGraphElement,
    			List<TransitionArc> transitionArcList) {

    		if (originalGraphElement instanceof TransitionArc) {
    			TransitionArc transitionArc = (TransitionArc) originalGraphElement;
    			transitionArcList.add(transitionArc);
    		}

    	}

    	private void fillListsWithAllPosibleJoinAndForkNodes(
    			GraphElement originalGraphElement, Map<String, StateNode> joinList,
    			Map<String, StateNode> forkList) {

    		if (originalGraphElement instanceof StateNode) {

    			updateNodeList(joinList, JOIN_NODE_NAME, originalGraphElement);

    			updateNodeList(forkList, FORK_NODE_NAME, originalGraphElement);

    		}

    	}

    	private void updateNodeList(Map<String, StateNode> nodeList,
    			String nodeType, GraphElement originalGraphElement) {

    		StateNode stateNode = (StateNode) originalGraphElement;

    		if (stateNode.getNodeType() != null
    				&& (stateNode.getNodeType().equals(nodeType))) {

    			nodeList.put(stateNode.getLabel(), stateNode);
    		}

    	}

    	private void relateNodesWithTransitionArcs(
    			Map<String, StateNode> joinForkList, TransitionArc transitionArc,
    			Map<GraphElement, List<TransitionArc>> joinOrForkWithTransitions) {
    		if (joinForkList.containsKey(transitionArc.getSource())) {
    			StateNode stateNode = joinForkList.get(transitionArc.getSource());
    			List<TransitionArc> listOfTransition = null;

    			if (joinOrForkWithTransitions.containsKey(stateNode)) {

    				listOfTransition = joinOrForkWithTransitions.get(stateNode);
    			} else {
    				listOfTransition = new ArrayList<TransitionArc>();
    			}

    			listOfTransition.add(transitionArc);
    			joinOrForkWithTransitions.put(stateNode, listOfTransition);

    		}

    	}

    	private ArrayList<String> getTasksNamesFromHistoryAsArray(
    			ArrayList<GraphElement> historyGraph) {

    		ArrayList<String> newHistoryElement = new ArrayList<String>();
    		for (GraphElement historyElement : historyGraph) {
    			if (historyElement instanceof StateNode) {
    				StateNode historyNode = (StateNode) historyElement;
    				String histloryNodeLabel = historyNode.getLabel();
    				String[] split = histloryNodeLabel.split(":");

    				String taskNameFromHistory = split[0];
    				newHistoryElement.add(taskNameFromHistory);
    			}

    		}

    		return newHistoryElement;
    	}

    	private boolean isForkOutgoingsExistsInHistoryGraph(
    			List<TransitionArc> list, GraphElement fork,
    			ArrayList<String> tasksNamesFromHistoryAsArray) {

    		int apperenceCounter = list.size();
    		for (TransitionArc transitionArc : list) {
    			if (tasksNamesFromHistoryAsArray.contains(transitionArc
    					.getDestination())) {
    				apperenceCounter--;
    			}

    		}
    		if (apperenceCounter == 0) {
    			return true;

    		}

    		return false;

    	}

    	private boolean isJoinOutgoingWasMissingInHistoryGraph(
    			List<TransitionArc> list,
    			ArrayList<String> tasksNamesFromHistoryAsArray,
    			ArrayList<GraphElement> historyGraph) {
    		boolean addedTransition = false;

    		for (TransitionArc transitionArc : list) {
    			if (tasksNamesFromHistoryAsArray.contains(transitionArc
    					.getDestination())) {
    				historyGraph.add(transitionArc);
    				addedTransition = true;
    			}
    		}

    		if (addedTransition == true) {
    			return true;

    		}

    		return false;

    	}
        

        @Override
        public byte[] getProcessLatestDefinition(String definitionKey, String processName) {
            String resourceName = processName + ".jpdl.xml";
            
            long start = System.currentTimeMillis();
             byte[] fetchLatestProcessResource = fetchLatestProcessResource(definitionKey, resourceName);
             
             long duration = System.currentTimeMillis() - start;
 			log.severe("getProcessLatestDefinition: " +  duration);
             return fetchLatestProcessResource;
        }

        @Override
        public byte[] getProcessMapImage(ProcessInstance pi) {
        	
        	 long start = System.currentTimeMillis();
            String resourceName = pi.getDefinition().getProcessName() + ".png";
             byte[] processResource = fetchProcessResource(pi, resourceName);
             
             long duration = System.currentTimeMillis() - start;
 			log.severe("getProcessMapImage: " +  duration);
             
             return processResource;
        }

        @Override
        public byte[] getProcessDefinition(ProcessInstance pi) {
        	
        	 long start = System.currentTimeMillis();
        	 
            String resourceName = pi.getDefinition().getProcessName() + ".jpdl.xml";
             byte[] processResource = fetchProcessResource(pi, resourceName);
             
             long duration = System.currentTimeMillis() - start;
 			log.severe("getProcessDefinition: " +  duration);
             
             return processResource;
        }

        private byte[] fetchLatestProcessResource(String definitionKey, String resourceName) {
            RepositoryService service = getProcessEngine(ProcessToolContext.Util.getThreadProcessToolContext())
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
            ProcessEngine processEngine = getProcessEngine(ProcessToolContext.Util.getThreadProcessToolContext());
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
            RepositoryService service = getProcessEngine(ProcessToolContext.Util.getThreadProcessToolContext()).getRepositoryService();;
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
                } finally {
                    if (oldStream != null) {
                        oldStream.close();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        public String deployProcessDefinition(String processName, InputStream definitionStream, InputStream processMapImageStream) {
            RepositoryService service = getProcessEngine(ProcessToolContext.Util.getThreadProcessToolContext())
                    .getRepositoryService();
            NewDeployment deployment = service.createDeployment();
            deployment.addResourceFromInputStream(processName + ".jpdl.xml", definitionStream);
            if (processMapImageStream != null)
                deployment.addResourceFromInputStream(processName + ".png", processMapImageStream);
            return deployment.deploy();
        }

    public String getProcessState(ProcessInstance pi, ProcessToolContext ctx) {
        List<BpmTask> tasks = findProcessTasks(pi, ctx);
        for (BpmTask task : tasks) {
            return task.getTaskName();
        }
        return null;
    }
}
