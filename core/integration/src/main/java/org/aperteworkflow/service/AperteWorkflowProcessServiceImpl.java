package org.aperteworkflow.service;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.aperteworkflow.service.fault.AperteWsIllegalArgumentException;
import org.aperteworkflow.service.fault.AperteWsWrongArgumentException;
import org.aperteworkflow.util.AperteErrorCheckUtil;
import org.aperteworkflow.util.AperteIllegalArgumentCodes;
import org.aperteworkflow.util.AperteWrongArgumentCodes;

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
import pl.net.bluesoft.rnd.processtool.plugins.deployment.ProcessDeployer;

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
                        .startProcess(config.getBpmDefinitionKey(), externalKey, description, keyword, source));
            }
        });
    }
	/**
	 * Simpler version of createProcessInstance.    
	 * @throws AperteWsWrongArgumentException  
	 * @throws AperteWsWrongArgumentException 
	 */
	@Override 
    @WebMethod
    public ProcessInstance startProcessInstance(@WebParam(name="bpmnkey")final String bpmnkey,                                                
                                                 @WebParam(name="userLogin")final String userLogin ) throws  AperteWsWrongArgumentException, AperteWsIllegalArgumentException
                                                 {	
		
		AperteErrorCheckUtil.checkCorrectnessOfArgument(bpmnkey, AperteIllegalArgumentCodes.DEFINITION);
		
		final UserData user = findUser(userLogin);
		
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {            	
                return fetchHibernateData(getSession(ctx, user)
                        .startProcess(bpmnkey, null, null, null, PROCESS_INSTANCE_SOURCE));
            }
        });
    }
	
	
	private UserData findUser(final String userLogin) throws  AperteWsWrongArgumentException {

		UserData userData = withContext(new ReturningProcessToolContextCallback<UserData>() {
			@Override
			public UserData processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getUserDataDAO().loadUserByLogin(
						userLogin));
			}
		});
		if (userLogin!=null && userData == null ) {
			AperteWrongArgumentCodes.USER.throwAperteWebServiceException();

		}
		return userData;
	} 

	@Override
    @WebMethod (exclude=true)
    public ProcessInstance getProcessData(@WebParam(name="internalId")final String internalId) throws AperteWsWrongArgumentException {
        ProcessInstance processInstance = withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getProcessData(internalId));
            }
        });
        if(processInstance==null){
        	
        	AperteWrongArgumentCodes.PROCESS.throwAperteWebServiceException();
        }
        
        return processInstance;
    }

	@Override
    @WebMethod
    public boolean isProcessRunning(@WebParam(name="internalId")final String internalId) throws AperteWsWrongArgumentException {
		
		final ProcessInstance processData = getProcessData(internalId);
        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
            @Override
            public Boolean processWithContext(ProcessToolContext ctx) {
                return getSession(ctx).isProcessRunning(processData.getInternalId());
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                getSession(ctx).saveProcessInstance(processInstance);
                return null;
            }
        });
    }

	@Override
    @WebMethod (exclude=true)
    public Collection<ProcessQueue> getUserAvailableQueues(@WebParam(name="userLogin")final String userLogin) throws  AperteWsWrongArgumentException {
		final UserData findUser = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueue>>() {
            @Override
            public Collection<ProcessQueue> processWithContext(ProcessToolContext ctx) {
            	
                return fetchHibernateData(getSession(ctx, findUser).getUserAvailableQueues());
            }
        });
    }
	
	

	@Override
    @WebMethod
    public boolean isProcessOwnedByUser(@WebParam(name="internalId")final String internalId,@WebParam(name="userLogin") final String userLogin) throws AperteWsWrongArgumentException {
		throw new RuntimeException("TODO: is it really necessary?");

//		final ProcessInstance processData = getProcessData(internalId);
//    	final UserData findUser = findUser(userLogin);
//        return withContext(new ReturningProcessToolContextCallback<Boolean>() {
//            @Override
//            public Boolean processWithContext(ProcessToolContext ctx) {
//
//                return getSession(ctx, findUser).isProcessOwnedByUser(processData);
//            }
//        });
    }
	

    

	@Override
	@WebMethod (exclude=true)
    public BpmTask assignTaskFromQueue(@WebParam(name="q")final ProcessQueue q, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).assignTaskFromQueue(q));
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
                return fetchHibernateData(getSession(ctx, user).assignTaskFromQueue(q, task));
            }
        });
    }

	@Override
    @WebMethod 
    public void assignTaskToUser(@WebParam(name="taskId")final String taskId, @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		final BpmTask taskData = getTaskData(taskId);
		final UserData user = findUser(userLogin);		
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {          	
                getSession(ctx, user).assignTaskToUser(taskData.getInternalTaskId(), userLogin);
                return null;
            }
        });
    }

	@Override
	@WebMethod 
    public BpmTask getTaskData(@WebParam(name="internalId")final String internalId,
                                                 @WebParam(name="taskName")final String taskName) throws AperteWsWrongArgumentException, AperteWsIllegalArgumentException {
		
		final ProcessInstance processData = getProcessData(internalId);
		 
		AperteErrorCheckUtil.checkCorrectnessOfArgument(taskName, AperteIllegalArgumentCodes.TASK);
		
		BpmTask bpmTasks = withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getTaskData(processData.getInternalId()));
            }
        });
		
		if(bpmTasks== null){
			
			AperteWrongArgumentCodes.BPMTASK.throwAperteWebServiceException();
		}
         
         return bpmTasks;
    }
	

	@Override
	@WebMethod (exclude=true)
    public BpmTask getTaskData(@WebParam(name="taskId")final String taskId) throws AperteWsWrongArgumentException, AperteWsIllegalArgumentException {
		
		AperteErrorCheckUtil.checkCorrectnessOfArgument(taskId, AperteIllegalArgumentCodes.TASK_ID);
		
		BpmTask taskData =  withContext(new ReturningProcessToolContextCallback<BpmTask>() {
            @Override
            public BpmTask processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx).getTaskData(taskId));
            }
        });
         if(taskData==null){
        	 
        	 AperteWrongArgumentCodes.TASK_ID.throwAperteWebServiceException();
         }
		return taskData;
    }

	@Override
	@WebMethod (exclude=true)
    public List<BpmTask> findUserTasks(@WebParam(name="processInstanceInternalId")final String internalId,
    		@WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		final ProcessInstance processInstance = getProcessData(internalId);
		final UserData user = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findUserTasks(processInstance));
            }
        }); 
    }

	@Override
	@WebMethod
    public List<BpmTask> findUserTasksPaging(@WebParam(name="offset")final Integer offset,
                                             @WebParam(name="limit")final Integer limit,
                                             @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		final UserData user = findUser(userLogin);
		
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findUserTasks(offset, limit));
            }
        });
    }

	@Override
	@WebMethod
    public List<BpmTask> findProcessTasks(@WebParam(name="internalId")final String internalId,
                                          @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		
		final UserData user = findUser(userLogin);
		final ProcessInstance processInstance = getProcessData(internalId);
		
        return withContext(new ReturningProcessToolContextCallback<List<BpmTask>>() {
            @Override
            public List<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).findProcessTasks(processInstance));
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
                return fetchHibernateData(getSession(ctx, user).findProcessTasks(processInstance, user.getLogin(), taskNames));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public Integer getRecentTasksCount(@WebParam(name="minDate")final Calendar minDate, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Integer>() {
            @Override
            public Integer processWithContext(ProcessToolContext ctx) {
                return getSession(ctx, user).getRecentTasksCount(minDate);
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public Collection<BpmTask> getAllTasks(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Collection<BpmTask>>() {
            @Override
            public Collection<BpmTask> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).getAllTasks());
            }
        });
    }
	
	
	
	
	
	 
	
	@Override 
    @WebMethod 
    public void performAction(@WebParam(name="procesInstanceInternalId")final String internalId,
    								@WebParam(name="actionName")final String actionName,
                                 @WebParam(name="bpmTaskName")final String bpmTaskName,
                                 @WebParam(name="userLogin")final String userLogin) throws  AperteWsWrongArgumentException {
		
		final BpmTask bpmTask = findProcessTask(internalId, userLogin, bpmTaskName);
		final UserData user = findUser(userLogin); 	
		final ProcessInstance processData = getProcessData(internalId);
		final ProcessStateAction action= getActionIfExists(processData, actionName);
		
		if(userLogin==null){
			adminCompleteTask(processData, action, bpmTask);
		}
		BpmTask[] bpmnTable = {bpmTask}; 
		processData.setActiveTasks(bpmnTable);
		  BpmTask withContextBpmnTask = withContext(new ReturningProcessToolContextCallback<BpmTask>() {
	            @Override
	            public BpmTask processWithContext(ProcessToolContext ctx) {
	                getSession(ctx, user).performAction(action, bpmTask);
	                return null;
	            }
	        });
		
		  
            
    }
	
	
	private BpmTask findProcessTask(String internalId,String userLogin,String bpmTaskName ) throws AperteWsWrongArgumentException{
		List<BpmTask> findProcessTasks = findProcessTasks(internalId, userLogin);
		if(findProcessTasks.isEmpty()){
			
			AperteWrongArgumentCodes.NOTASK.throwAperteWebServiceException();
		}
		if(bpmTaskName == null || bpmTaskName.isEmpty()){
			return findProcessTasks.get(0);
		}
		
		for (BpmTask bpmTaskTemp : findProcessTasks) {
			if(bpmTaskTemp.getTaskName().equals(bpmTaskName)){
				
				return bpmTaskTemp;
				
			}
			AperteWrongArgumentCodes.BPMTASK.throwAperteWebServiceException();
		}
		AperteWrongArgumentCodes.PROCESS.throwAperteWebServiceException();
		return null;
		
	}
	

    private ProcessStateAction getActionIfExists(final ProcessInstance processData, String actionName) throws AperteWsWrongArgumentException {
    	final ProcessDefinitionConfig definition = processData.getDefinition();
    	List<ProcessStateAction> actionsBasedOnStatus = withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
            @Override
            public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
            	final ProcessDefinitionConfig definition = processData.getDefinition();
        		String state = null;//TODO processData.getState();
        		if(state==null || state.isEmpty()){//TODO its for compatibility with 1.X aperte data. In future its should be removed
        			if(processData.getStatus().equals(ProcessStatus.NEW)){
        				List<BpmTask> bpmTasks = getSession(ctx).findProcessTasks(processData);
        				state=bpmTasks.get(0).getTaskName();
        			}
        		}
        		
        		List<ProcessStateAction> actionsBasedOnStatus = ctx.getProcessStateActionDAO().getActionsBasedOnStateAndDefinitionId(state, definition.getId());
				return actionsBasedOnStatus;
        		
        			
            }
        });
		
		
		if(actionsBasedOnStatus == null || actionsBasedOnStatus.isEmpty()){
			AperteWrongArgumentCodes.ACTION.throwAperteWebServiceException();
			
		}

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
    public List<String> getOutgoingTransitionNamesByTaskId(@WebParam(name="taskId")final String taskId) throws AperteWsWrongArgumentException {
		throw new RuntimeException("TODO: is it really necessary?");
//		AperteErrorCheckUtil.checkCorrectnessOfArgument(taskId, AperteIllegalArgumentCodes.TASK);
//
//		 List<String> outgoingTransmisionsList = withContext(new ReturningProcessToolContextCallback<List<String>>() {
//            @Override
//            public List<String> processWithContext(ProcessToolContext ctx) {
//                return getSession(ctx).getOutgoingTransitionNames(taskId);
//            }
//        });
//
//		 if( outgoingTransmisionsList.isEmpty() ){
//			  AperteWrongArgumentCodes.BPMTASK.throwAperteWebServiceException();
//		 }
//
//		 return outgoingTransmisionsList;
		 
    }
	 
	@Override
	@WebMethod (exclude=true)
    public UserData getSubstitutingUser(@WebParam(name="user")final String userLogin) throws AperteWsWrongArgumentException {
		final UserData user = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(getSession(ctx, user).getSubstitutingUser());
            }
        });
    }

	@Override
    @WebMethod
    public List<String> getOutgoingTransitionDestinationNames(@WebParam(name="executionId")final String executionId) throws AperteWsWrongArgumentException {
		throw new RuntimeException("TODO: is it really necessary?");
//		final ProcessInstance processData = getProcessData(executionId);
//        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
//            @Override
//            public List<String> processWithContext(ProcessToolContext ctx) {
//                return getSession(ctx).getOutgoingTransitionDestinationNames(processData.getInternalId());
//            }
//        });
    }

	@Override
    @WebMethod
    public void adminCancelProcessInstance(@WebParam(name="processInstance")final String internalId) throws AperteWsWrongArgumentException {
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
	@WebMethod (exclude=true)
    public void adminCompleteTask(@WebParam(name="procesInstanceInternalId")final ProcessInstance processData,
	@WebParam(name="action")final ProcessStateAction action,
    @WebParam(name="bpmTask")final BpmTask bpmTask) {
	
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
            	
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

	
	
	/*
	 * FIXME
	 * Service does not work,It fails when it try to update queues (When queues are empty, everything works), 
	 * the problem is known from queuing mechanism update. Exception occurs: 
	 * "[Hibernate] NonUniqueObjectException: a different object with the same 
	 * identifier value was already associated with the session" probably swap in the classroom: 
	 * "ProcessDefinitionDaoImpl" in the method: "updateOrCreateQueueConfigs" "save" to "merge" or "saveOrUpdate" solve the problem.
	 * 
	 * 
	 */
	@Override
    @WebMethod (exclude=true)
    public void deployProcessDefinitionBytes(@WebParam(name="cfg")final ProcessDefinitionConfig cfg,
                                             @WebParam(name="queues")final ProcessQueueConfig[] queues,
                                             @WebParam(name="processMapDefinition")final byte[] processMapDefinition,
                                             @WebParam(name="processMapImageStream")final byte[] processMapImageStream,
                                             @WebParam(name="logo")final byte[] logo) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) 
            {
            	ProcessDeployer processDeployer = new ProcessDeployer(ctx);
            	processDeployer.deployOrUpdateProcessDefinition(
                        new ByteArrayInputStream(processMapDefinition),
                        cfg, queues,
                        new ByteArrayInputStream(processMapImageStream),
                        new ByteArrayInputStream(logo));
                return null;
            }
        });
    }

	/*
	 * FIXME
	 * Service does not work,It fails when it try to update queues (When queues are empty, everything works), 
	 * the problem is known from queuing mechanism update. Exception occurs: 
	 * "[Hibernate] NonUniqueObjectException: a different object with the same 
	 * identifier value was already associated with the session" probably swap in the classroom: 
	 * "ProcessDefinitionDaoImpl" in the method: "updateOrCreateQueueConfigs" "save" to "merge" or "saveOrUpdate" solve the problem.
	 * 
	 * 
	 */
	
	@Override 
	@WebMethod (exclude=true)
    public void deployProcessDefinition(@WebParam(name="cfgXmlFile")final byte[] cfgXmlFile,
                                        @WebParam(name="queueXmlFile")final byte[] queueXmlFile,
                                        @WebParam(name="processMapDefinition")final byte[] processMapDefinition,
                                        @WebParam(name="processMapImageStream")final byte[] processMapImageStream,
                                        @WebParam(name="logo")final byte[] logo) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) 
            {
            	ProcessDeployer processDeployer = new ProcessDeployer(ctx);
            	
            	processDeployer.deployOrUpdateProcessDefinition(
                        new ByteArrayInputStream(processMapDefinition),
                        new ByteArrayInputStream(cfgXmlFile),
                        new ByteArrayInputStream(queueXmlFile),
                        new ByteArrayInputStream(processMapImageStream),
                        new ByteArrayInputStream(logo));
                return null;
            }
        });
    }
	
	@Override
    @WebMethod
	public List<ProcessStateAction> getAvalivableActionForProcess( 
			@WebParam(name = "internalId") final String internalId) throws AperteWsWrongArgumentException {

		 final ProcessInstance instance = getProcessData(internalId);
		 final ProcessDefinitionConfig definition = instance.getDefinition();
		List<BpmTask> findProcessTasks = findProcessTasks(internalId, null);
		 
		return withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
			
			@Override
			public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
				String state = null;
				if(state==null || state.isEmpty()){//TODO its for compatibility with 1.X aperte data. In future its should be removed
					if(instance.getStatus().equals(ProcessStatus.NEW)){
						List<BpmTask> bpmTasks = getSession(ctx).findProcessTasks(instance);
						state=bpmTasks.get(0).getTaskName();
					}
					
				 } 
				return fetchHibernateData(ctx.getProcessStateActionDAO().getActionsBasedOnStateAndDefinitionId(state, definition.getId())); 
			}
		});
 
	}
	
	@Override
    @WebMethod  (exclude=true)
	public List<ProcessStateAction> getActionsListByNameFromInstance( 
			@WebParam(name = "internalId") final String internalId,@WebParam(name = "actionName") final String actionName) throws AperteWsWrongArgumentException {

		ProcessInstance instanceByInternalId = getProcessData(internalId);
		final ProcessDefinitionConfig definition = instanceByInternalId.getDefinition();
		return withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
			
			@Override
			public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
				
				return fetchHibernateData(ctx.getProcessStateActionDAO().getActionByNameFromDefinition(definition, actionName));
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
