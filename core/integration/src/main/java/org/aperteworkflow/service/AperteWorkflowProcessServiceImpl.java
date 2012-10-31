package org.aperteworkflow.service;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessStatus;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
 
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.service.fault.AperteWebServiceError;
import org.aperteworkflow.util.AperteErrorCodes;

import java.io.ByteArrayInputStream;
import java.util.*; 

import static org.aperteworkflow.util.ContextUtil.withContext;  
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData; 

/**
 * Most of WebMethods works, some of them are taged as (exclude=true), because they allow for too much interference in the aperet workflow  data.
 * To make them work again just delete WebMethod annotaion.
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */
 
@WebService
public class AperteWorkflowProcessServiceImpl implements AperteWorkflowProcessService {
	
	private final static String PROCESS_INSTANCE_SOURCE ="portlet";

	//@WebMethod (exclude=true)
	
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
	/**
	 * Simpler version of createProcessInstance.
	 * @throws AperteWebServiceError  
	 * @throws AperteWebServiceError 
	 */
	@Override 
    @WebMethod
    public ProcessInstance startProcessInstance(@WebParam(name="bpmnkey")final String bpmnkey,                                                
                                                 @WebParam(name="userLogin")final String userLogin ) throws  AperteWebServiceError
                                                 {		
		final UserData user = findUser(userLogin);
		
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
            	ProcessDefinitionConfig activeConfigurationByKey = ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(bpmnkey);            	
                return fetchHibernateData(getSession(ctx, user)
                        .createProcessInstance(activeConfigurationByKey, null, ctx, null, null, PROCESS_INSTANCE_SOURCE, null));
            }
        });
    }
	
	
	private UserData findUser(final String userLogin) throws  AperteWebServiceError {

		UserData userData = withContext(new ReturningProcessToolContextCallback<UserData>() {
			@Override
			public UserData processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getUserDataDAO().loadUserByLogin(
						userLogin));
			}
		});
		if (userLogin!=null && userData == null ) {
			throw new AperteWebServiceError(AperteErrorCodes.USER.getErrorCode(), AperteErrorCodes.USER.getMessage());

		}
		return userData;
	} 

	@Override
    @WebMethod (exclude=true)
    public ProcessInstance getProcessData(@WebParam(name="internalId")final String internalId) throws AperteWebServiceError {
        ProcessInstance processInstance = withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getProcessData(internalId, ctx));
            }
        });
        if(processInstance==null){
        	
        	throw new AperteWebServiceError(AperteErrorCodes.PROCESS.getErrorCode(),AperteErrorCodes.PROCESS.getMessage());
        }
        
        return processInstance;
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
	@WebMethod (exclude=true)
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
    @WebMethod (exclude=true)
    public Collection<ProcessQueue> getUserAvailableQueues(@WebParam(name="userLogin")final String userLogin) throws  AperteWebServiceError {
		final UserData findUser = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueue>>() {
            @Override
            public Collection<ProcessQueue> processWithContext(ProcessToolContext ctx) {
            	
                return fetchHibernateData(getSession(ctx, findUser).getUserAvailableQueues(ctx));
            }
        });
    }
	
	

	@Override
    @WebMethod
    public boolean isProcessOwnedByUser(@WebParam(name="internalId")final String internalId,@WebParam(name="userLogin") final String userLogin) throws AperteWebServiceError {
		final ProcessInstance processData = getProcessData(internalId);
    	final UserData findUser = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
            @Override
            public Boolean processWithContext(ProcessToolContext ctx) {
            	
                return getSession(ctx, findUser).isProcessOwnedByUser(processData, ctx);
            }
        });
    }
	

    

	@Override
	@WebMethod (exclude=true)
    public BpmTask assignTaskFromQueue(@WebParam(name="q")final ProcessQueue q, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).assignTaskFromQueue(q, ctx));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
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
    public void assignTaskToUser(@WebParam(name="taskId")final String taskId, @WebParam(name="userLogin")final String userLogin) throws AperteWebServiceError {
		final UserData user = findUser(userLogin);
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
            	
                getSession(ctx, user).assignTaskToUser(ctx, taskId, userLogin);
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
	@WebMethod (exclude=true)
    public BpmTask getTaskData(@WebParam(name="taskId")final String taskId) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getTaskData(taskId, ctx));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public List<BpmTask> findUserTasks(@WebParam(name="processInstanceInternalId")final String internalId,
    		@WebParam(name="userLogin")final String userLogin) throws AperteWebServiceError {
		final ProcessInstance processInstance = getProcessData(internalId);
		final UserData user = findUser(userLogin);
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
                                             @WebParam(name="userLogin")final String userLogin) throws AperteWebServiceError {
		final UserData user = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findUserTasks(offset, limit, ctx));
            }
        });
    }

	@Override
	@WebMethod
    public List<BpmTask> findProcessTasks(@WebParam(name="internalId")final String internalId,
                                          @WebParam(name="userLogin")final String userLogin) throws AperteWebServiceError {
		
		final UserData user = findUser(userLogin);
		final ProcessInstance processInstance = getProcessData(internalId);
		
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findProcessTasks(processInstance, ctx));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
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
	@WebMethod (exclude=true)
    public Integer getRecentTasksCount(@WebParam(name="minDate")final Calendar minDate, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Integer>() {
            @Override
            public Integer processWithContext(ProcessToolContext ctx) {
                return getSession(ctx, user).getRecentTasksCount(minDate, ctx);
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
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
    public void performAction(@WebParam(name="procesInstanceInternalId")final String internalId,
    								@WebParam(name="actionName")final String actionName,
                                 @WebParam(name="bpmTaskName")final String bpmTaskName,
                                 @WebParam(name="userLogin")final String userLogin) throws  AperteWebServiceError {
		
		final BpmTask bpmTask = findProcessTask(internalId, userLogin, bpmTaskName);
		final UserData user = findUser(userLogin); 
		final ProcessInstance processData = getProcessData(internalId);
		BpmTask[] bpmnTable = {bpmTask}; 
		processData.setActiveTasks(bpmnTable);
		  BpmTask withContextBpmnTask = withContext(new ReturningProcessToolContextCallback<BpmTask>() {
	            @Override
	            public BpmTask processWithContext(ProcessToolContext ctx) {
	            	ProcessStateAction action= getActionIfExists(processData, actionName, ctx);	            	
	                getSession(ctx, user).performAction(action, bpmTask, ctx);
	                return null;
	            }
	        });
		  if(withContextBpmnTask==null){  
			  throw new AperteWebServiceError(AperteErrorCodes.ACTION.getErrorCode(),AperteErrorCodes.ACTION.getMessage());
		  }
		  
            
    }
	
	
	private BpmTask findProcessTask(String internalId,String userLogin,String bpmTaskName ) throws AperteWebServiceError{
		List<BpmTask> findProcessTasks = findProcessTasks(internalId, userLogin);
		if(findProcessTasks.isEmpty()){
			
			throw new AperteWebServiceError(AperteErrorCodes.NOTASK.getErrorCode(),AperteErrorCodes.NOTASK.getMessage());
		}
		if(bpmTaskName == null || bpmTaskName.isEmpty()){
			return findProcessTasks.get(0);
		}
		
		for (BpmTask bpmTaskTemp : findProcessTasks) {
			if(bpmTaskTemp.getTaskName().equals(bpmTaskName)){
				
				return bpmTaskTemp;
				
			}
			throw new AperteWebServiceError(AperteErrorCodes.BPMTASK.getErrorCode(),AperteErrorCodes.BPMTASK.getMessage());
		}
		throw new AperteWebServiceError(AperteErrorCodes.PROCESS.getErrorCode(),AperteErrorCodes.PROCESS.getMessage());
		
	}
	
	private ProcessStateAction  getActionIfExists(ProcessInstance processData, String actionName, ProcessToolContext ctx) {
		 final ProcessDefinitionConfig definition = processData.getDefinition();
		String state = processData.getState();
		if(state==null || state.isEmpty()){//TODO its for compatibility with 1.X aperte data. In future its should be removed
			if(processData.getStatus().equals(ProcessStatus.NEW)){
				List<BpmTask> bpmTasks = getSession(ctx).findProcessTasks(processData, ctx);
				state=bpmTasks.get(0).getTaskName();
			}
		}
		
		List<ProcessStateAction> actionsBasedOnStatus = ctx.getProcessStateActionDAO().getActionsBasedOnStateAndDefinitionId(state, definition.getId());
		
		if(actionName== null || actionName.isEmpty()){
			return actionsBasedOnStatus.get(0);
		}
		for (ProcessStateAction processStateAction : actionsBasedOnStatus) {

		 if (!processStateAction.getBpmName().equals(actionName)){
				return processStateAction;
			}	
		}
		return null;
	
	}

	@Override
    @WebMethod
    public List<String> getOutgoingTransitionNames(@WebParam(name="executionId")final String executionId) throws AperteWebServiceError {
		final ProcessInstance processData = getProcessData(executionId);
		return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return getSession(ctx).getOutgoingTransitionNames(processData.getInternalId(), ctx);
            }
        });
    }
	

	 
	@Override
	@WebMethod (exclude=true)
    public UserData getSubstitutingUser(@WebParam(name="user")final String userLogin) throws AperteWebServiceError {
		final UserData user = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).getSubstitutingUser(ctx));
            }
        });
    }

	@Override
    @WebMethod
    public List<String> getOutgoingTransitionDestinationNames(@WebParam(name="executionId")final String executionId) throws AperteWebServiceError {
		
		final ProcessInstance processData = getProcessData(executionId);
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return getSession(ctx).getOutgoingTransitionDestinationNames(processData.getInternalId(), ctx);
            }
        });
    }

	@Override
    @WebMethod
    public void adminCancelProcessInstance(@WebParam(name="processInstance")final String internalId) throws AperteWebServiceError {
		final ProcessInstance processData = getProcessData(internalId);
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
            	
                ctx.getProcessToolSessionFactory().createAutoSession().adminCancelProcessInstance(processData);
                return null;
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
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
    public void adminCompleteTask(@WebParam(name="procesInstanceInternalId")final String internalId,
	@WebParam(name="actionName")final String actionName,
    @WebParam(name="bpmTaskName")final String bpmTaskName) throws  AperteWebServiceError {

final BpmTask bpmTask = findProcessTask(internalId, null, bpmTaskName);
final ProcessInstance processData = getProcessData(internalId);
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
            	ProcessStateAction action= getActionIfExists(processData, actionName, ctx);	
                ctx.getProcessToolSessionFactory().createAutoSession()
                        .adminCompleteTask(processData, bpmTask, action);
                return null;
            }
        });
    }

	//TODO GraphElement has to be an interface
//	@Override
//    @WebMethod
//    public List<GraphElement> getProcessHistory(@WebParam(name="internalId")final String internalId) throws AperteWebServiceError {
//		final ProcessInstance processInstance = getProcessData(internalId);
//		 
//        return withContext(new ReturningProcessToolContextCallback<List<GraphElement>>() {
//            @Override
//            public List<GraphElement> processWithContext(ProcessToolContext ctx) {
//                return ctx.getProcessToolSessionFactory().createAutoSession().getProcessHistory(processInstance);
//            }
//        });
//    }

	@Override
    @WebMethod (exclude=true)
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
	@WebMethod (exclude=true)
    public void deployProcessDefinition(@WebParam(name="cfgXmlFile")final byte[] cfgXmlFile,
                                        @WebParam(name="queueXmlFile")final byte[] queueXmlFile,
                                        @WebParam(name="processMapDefinition")final byte[] processMapDefinition,
                                        @WebParam(name="processMapImageStream")final byte[] processMapImageStream,
                                        @WebParam(name="logo")final byte[] logo) {
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