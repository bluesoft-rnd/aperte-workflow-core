package org.aperteworkflow.service;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.ByteArrayInputStream;
import java.util.*;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;

/**
 * @author tlipski@bluesoft.net.pl
 */
@WebService
public class AperteWorkflowProcessServiceImpl implements AperteWorkflowProcessService {

	@Override
    @WebMethod
    public ProcessInstance createProcessInstance(@WebParam(name="config")final ProcessDefinitionConfig config,
                                                 @WebParam(name="externalKey")final String externalKey,
                                                 @WebParam(name="user")final UserData user,
                                                 @WebParam(name="description")final String description,
                                                 @WebParam(name="keyword")final String keyword,
                                                 @WebParam(name="source")final String source,
                                                 @WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user)
                        .createProcessInstance(config, externalKey, ctx, description, keyword, source, internalId));
            }
        });
    }

	@Override
    @WebMethod
    public ProcessInstance getProcessData(@WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getProcessData(internalId, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public boolean isProcessRunning(@WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
            @Override
            public Boolean processWithContext(ProcessToolContext ctx) {
                return getSession(ctx).isProcessRunning(internalId, ctx);
            }
        });
    }

	@Override
    @WebMethod
    public void saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                getSession(ctx).saveProcessInstance(processInstance, ctx);
                return null;
            }
        });
    }

	@Override
    @WebMethod
    public Collection<ProcessQueue> getUserAvailableQueues(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueue>>() {
            @Override
            public Collection<ProcessQueue> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).getUserAvailableQueues(ctx));
            }
        });
    }

	@Override
    @WebMethod
    public boolean isProcessOwnedByUser(final ProcessInstance processInstance, final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
            @Override
            public Boolean processWithContext(ProcessToolContext ctx) {
                return getSession(ctx, user).isProcessOwnedByUser(processInstance, ctx);
            }
        });
    }

	@Override
    @WebMethod
    public BpmTask assignTaskFromQueue(@WebParam(name="q")final ProcessQueue q, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).assignTaskFromQueue(q, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public BpmTask assignSpecificTaskFromQueue(@WebParam(name="q")final ProcessQueue q,
                                               @WebParam(name="task")final BpmTask task,
                                               @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).assignTaskFromQueue(q, task, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public void assignTaskToUser(@WebParam(name="taskId")final String taskId, @WebParam(name="user")final UserData user) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                getSession(ctx, user).assignTaskToUser(ctx, taskId, user.getLogin());
                return null;
            }
        });
    }

	@Override
    @WebMethod
    public BpmTask getTaskDataForProcessInstance(@WebParam(name="taskExecutionId")final String taskExecutionId,
                                                 @WebParam(name="taskName")final String taskName) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getTaskData(taskExecutionId, taskName, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public BpmTask getTaskData(@WebParam(name="taskId")final String taskId) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getTaskData(taskId, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<BpmTask> findUserTasks(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                       @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findUserTasks(processInstance, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<BpmTask> findUserTasksPaging(@WebParam(name="offset")final Integer offset,
                                             @WebParam(name="limit")final Integer limit,
                                             @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findUserTasks(offset, limit, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<BpmTask> findProcessTasks(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                          @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findProcessTasks(processInstance, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<BpmTask> findProcessTasksByNames(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                                 @WebParam(name="user")final UserData user,
                                                 @WebParam(name="taskNames")final Set<String> taskNames) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findProcessTasks(processInstance, user.getLogin(), taskNames, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public Integer getRecentTasksCount(@WebParam(name="minDate")final Calendar minDate, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Integer>() {
            @Override
            public Integer processWithContext(ProcessToolContext ctx) {
                return getSession(ctx, user).getRecentTasksCount(minDate, ctx);
            }
        });
    }

	@Override
    @WebMethod
    public Collection<BpmTask> getAllTasks(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Collection<BpmTask>>() {
            @Override
            public Collection<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).getAllTasks(ctx));
            }
        });
    }

	@Override
    @WebMethod
    public BpmTask performAction(@WebParam(name="action")final ProcessStateAction action,
                                 @WebParam(name="bpmTask")final BpmTask bpmTask,
                                 @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).performAction(action, bpmTask, ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<String> getOutgoingTransitionNames(@WebParam(name="executionId")final String executionId) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return getSession(ctx).getOutgoingTransitionNames(executionId, ctx);
            }
        });
    }

	@Override
    @WebMethod
    public UserData getSubstitutingUser(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).getSubstitutingUser(ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<String> getOutgoingTransitionDestinationNames(@WebParam(name="executionId")final String executionId) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return getSession(ctx).getOutgoingTransitionDestinationNames(executionId, ctx);
            }
        });
    }

	@Override
    @WebMethod
    public void adminCancelProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                ctx.getProcessToolSessionFactory().createAutoSession().adminCancelProcessInstance(processInstance);
                return null;
            }
        });
    }

	@Override
    @WebMethod
    public void adminReassignProcessTask(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                         @WebParam(name="bpmTask")final BpmTask bpmTask,
                                         @WebParam(name="user")final UserData user) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                ctx.getProcessToolSessionFactory().createAutoSession()
						.adminReassignProcessTask(processInstance, bpmTask, user.getLogin());
                return null;
            }
        });
    }
    
	@Override
    @WebMethod
    public void adminCompleteTask(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                  @WebParam(name="bpmTask")final BpmTask bpmTask,
                                  @WebParam(name="action")final ProcessStateAction action) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                ctx.getProcessToolSessionFactory().createAutoSession()
                        .adminCompleteTask(processInstance, bpmTask, action);
                return null;
            }
        });
    }

	//TODO GraphElement has to be an interface
//	@Override
//    @WebMethod
//    public List<GraphElement> getProcessHistory(@WebParam(name="user")final ProcessInstance processInstan) {
//        return withContext(new ReturningProcessToolContextCallback<List<GraphElement>>() {
//            @Override
//            public List<GraphElement> processWithContext(ProcessToolContext ctx) {
//                return ctx.getProcessToolSessionFactory().createAutoSession().getProcessHistory(pi);
//            }
//        });
//    }

	@Override
    @WebMethod
    public void deployProcessDefinitionBytes(@WebParam(name="cfg")final ProcessDefinitionConfig cfg,
                                             @WebParam(name="queues")final ProcessQueueConfig[] queues,
                                             @WebParam(name="processMapDefinition")final byte[] processMapDefinition,
                                             @WebParam(name="processMapImageStream")final byte[] processMapImageStream,
                                             @WebParam(name="logo")final byte[] logo) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) {
                ctx.getRegistry().deployOrUpdateProcessDefinition(
                        new ByteArrayInputStream(processMapDefinition),
                        cfg, queues,
                        new ByteArrayInputStream(processMapImageStream),
                        new ByteArrayInputStream(logo));
                return null;
            }
        });
    }

	@Override
    @WebMethod
    public void deployProcessDefinition(@WebParam(name="cfgXmlFile")final byte[] cfgXmlFile,
                                        @WebParam(name="user")final byte[] queueXmlFile,
                                        @WebParam(name="user")final byte[] processMapDefinition,
                                        @WebParam(name="user")final byte[] processMapImageStream,
                                        @WebParam(name="user")final byte[] logo) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) {
                ctx.getRegistry().deployOrUpdateProcessDefinition(
                        new ByteArrayInputStream(processMapDefinition),
                        new ByteArrayInputStream(cfgXmlFile),
                        new ByteArrayInputStream(queueXmlFile),
                        new ByteArrayInputStream(processMapImageStream),
                        new ByteArrayInputStream(logo));
                return null;
            }
        });
    }

	private ProcessToolBpmSession getSession(ProcessToolContext ctx) {
		return getSession(ctx, null);
	}

	private ProcessToolBpmSession getSession(ProcessToolContext ctx, UserData user) {
		return ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>());
	}
}
