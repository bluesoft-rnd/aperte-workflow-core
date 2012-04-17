package org.aperteworkflow.service;

import org.aperteworkflow.bpm.graph.GraphElement;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.RegistryHolder;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;

/**
 * @author tlipski@bluesoft.net.pl
 */
@WebService
public class AperteWorkflowProcessServiceImpl {
    @WebMethod
    public ProcessInstance createProcessInstance(@WebParam(name="config")final ProcessDefinitionConfig config,
                                                 @WebParam(name="externalKey")final String externalKey,
                                                 @WebParam(name="user")final UserData user,
                                                 @WebParam(name="description")final String description,
                                                 @WebParam(name="keyword")final String keyword,
                                                 @WebParam(name="source")final String source) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .createProcessInstance(config, externalKey, ctx, description, keyword, source));
            }
        });
    }

    @WebMethod
    public ProcessInstance getProcessData(@WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getProcessData(internalId, ctx));
            }
        });
    }

    @WebMethod
    public boolean isProcessRunning(@WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
            @Override
            public Boolean processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .isProcessRunning(internalId, ctx);
            }
        });
    }

    @WebMethod
    public void saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .saveProcessInstance(processInstance, ctx);
                return null;
            }
        });
    }

    @WebMethod
    public Collection<ProcessQueue> getUserAvailableQueues(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueue>>() {
            @Override
            public Collection<ProcessQueue> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .getUserAvailableQueues(ctx));
            }
        });
    }

    @WebMethod
    public boolean isProcessOwnedByUser(final ProcessInstance processInstance, final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
            @Override
            public Boolean processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .isProcessOwnedByUser(processInstance, ctx);
            }
        });
    }

    @WebMethod
    public BpmTask assignTaskFromQueue(@WebParam(name="q")final ProcessQueue q, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .assignTaskFromQueue(q, ctx));
            }
        });
    }

    @WebMethod
    public BpmTask assignSpecificTaskFromQueue(@WebParam(name="q")final ProcessQueue q,
                                               @WebParam(name="task")final BpmTask task,
                                               @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .assignTaskFromQueue(q, task, ctx));
            }
        });
    }

    @WebMethod
    public void assignTaskToUser(@WebParam(name="taskId")final String taskId, @WebParam(name="user")final UserData user) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .assignTaskToUser(ctx, taskId, user.getLogin());
                return null;
            }
        });
    }

    @WebMethod
    public BpmTask getTaskDataForProcessInstance(@WebParam(name="taskExecutionId")final String taskExecutionId,
                                                 @WebParam(name="taskName")final String taskName) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getTaskData(taskExecutionId, taskName, ctx));
            }
        });
    }

    @WebMethod
    public BpmTask getTaskData(@WebParam(name="taskId")final String taskId) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getTaskData(taskId, ctx));
            }
        });
    }

    @WebMethod
    public List<BpmTask> findUserTasks(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                       @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .findUserTasks(processInstance, ctx));
            }
        });
    }

    @WebMethod
    public List<BpmTask> findUserTasksPaging(@WebParam(name="offset")final Integer offset,
                                             @WebParam(name="limit")final Integer limit,
                                             @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .findUserTasks(offset, limit, ctx));
            }
        });
    }

    @WebMethod
    public List<BpmTask> findProcessTasks(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                          @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .findProcessTasks(processInstance, ctx));
            }
        });
    }

    @WebMethod
    public List<BpmTask> findProcessTasksByNames(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                                 @WebParam(name="user")final UserData user,
                                                 @WebParam(name="taskNames")final Set<String> taskNames) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .findProcessTasks(processInstance, user.getLogin(), taskNames, ctx));
            }
        });
    }

    @WebMethod
    public Integer getRecentTasksCount(@WebParam(name="minDate")final Calendar minDate, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Integer>() {
            @Override
            public Integer processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .getRecentTasksCount(minDate, ctx);
            }
        });
    }

    @WebMethod
    public Collection<BpmTask> getAllTasks(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Collection<BpmTask>>() {
            @Override
            public Collection<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .getAllTasks(ctx));
            }
        });
    }

    @WebMethod
    public BpmTask performAction(@WebParam(name="action")final ProcessStateAction action,
                                 @WebParam(name="bpmTask")final BpmTask bpmTask,
                                 @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .performAction(action, bpmTask, ctx));
            }
        });
    }

    @WebMethod
    public List<String> getOutgoingTransitionNames(@WebParam(name="executionId")final String executionId) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getOutgoingTransitionNames(executionId, ctx);
            }
        });
    }

    @WebMethod
    public UserData getSubstitutingUser(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessToolSessionFactory().createSession(user, new HashSet<String>())
                        .getSubstitutingUser(ctx));
            }
        });
    }

    @WebMethod
    public List<String> getOutgoingTransitionDestinationNames(@WebParam(name="executionId")final String executionId) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getOutgoingTransitionDestinationNames(executionId, ctx);
            }
        });
    }

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

    @WebMethod
    public void adminReassignProcessTask(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                         @WebParam(name="bpmTask")final BpmTask bpmTask,
                                         @WebParam(name="user")final UserData user) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                ctx.getProcessToolSessionFactory().createAutoSession().adminReassignProcessTask(processInstance, bpmTask,
                        user.getLogin());
                return null;
            }
        });
    }
    

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
//    @WebMethod
//    public List<GraphElement> getProcessHistory(@WebParam(name="user")final ProcessInstance processInstan) {
//        return withContext(new ReturningProcessToolContextCallback<List<GraphElement>>() {
//            @Override
//            public List<GraphElement> processWithContext(ProcessToolContext ctx) {
//                return ctx.getProcessToolSessionFactory().createAutoSession().getProcessHistory(pi);
//            }
//        });
//    }

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
}
