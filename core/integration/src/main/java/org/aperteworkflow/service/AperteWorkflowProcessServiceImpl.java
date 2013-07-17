package org.aperteworkflow.service;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

import java.io.ByteArrayInputStream;
import java.util.*;

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
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueueBean;
import pl.net.bluesoft.rnd.processtool.plugins.deployment.ProcessDeployer;

/**
 * Most of WebMethods works, some of them are taged as (exclude=true), because they allow for too much interference in the aperet workflow  data.
 * To make them work again just delete WebMethod annotaion.
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */
 
@WebService
public class AperteWorkflowProcessServiceImpl implements AperteWorkflowProcessService {
	
	private static final String PROCESS_INSTANCE_SOURCE ="portlet";

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
                return fetchHibernateData(getSession(user)
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
                return fetchHibernateData(getSession(user)
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
                return fetchHibernateData(getSession().getProcessData(internalId));
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
                return getSession().isProcessRunning(processData.getInternalId());
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                getSession().saveProcessInstance(processInstance);
                return null;
            }
        });
    }

	@Override
    @WebMethod (exclude=true)
    public Collection<ProcessQueueBean> getUserAvailableQueues(@WebParam(name="userLogin") String userLogin) throws  AperteWsWrongArgumentException {
		final UserData findUser = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueueBean>>() {
            @Override
            public Collection<ProcessQueueBean> processWithContext(ProcessToolContext ctx) {
            	
                return ProcessQueueBean.asBeans(getSession(findUser).getUserAvailableQueues());
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
    public BpmTaskBean assignTaskFromQueue(@WebParam(name="q")final ProcessQueueBean q, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
            @Override
            public BpmTaskBean processWithContext(ProcessToolContext ctx) {
                return new BpmTaskBean(fetchHibernateData(getSession(user).assignTaskFromQueue(q.getName())));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public BpmTaskBean assignSpecificTaskFromQueue(@WebParam(name="q")final ProcessQueueBean q,
                                               @WebParam(name="task")final BpmTaskBean task,
                                               @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
            @Override
            public BpmTaskBean processWithContext(ProcessToolContext ctx) {
                return new BpmTaskBean(fetchHibernateData(getSession(user).assignTaskFromQueue(q.getName(), task)));
            }
        });
    }

	@Override
    @WebMethod 
    public void assignTaskToUser(@WebParam(name="taskId")final String taskId, @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		final BpmTaskBean taskData = getTaskData(taskId);
		final UserData user = findUser(userLogin);		
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {          	
                getSession(user).assignTaskToUser(taskData.getInternalTaskId(), userLogin);
                return null;
            }
        });
    }

	@Override
	@WebMethod 
    public BpmTaskBean getTaskData(@WebParam(name="internalId")final String internalId,
                                                 @WebParam(name="taskName")final String taskName) throws AperteWsWrongArgumentException, AperteWsIllegalArgumentException {
		
		final ProcessInstance processData = getProcessData(internalId);
		 
		AperteErrorCheckUtil.checkCorrectnessOfArgument(taskName, AperteIllegalArgumentCodes.TASK);
		
		BpmTaskBean BpmTaskBeans = withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
            @Override
            public BpmTaskBean processWithContext(ProcessToolContext ctx) {
                return new BpmTaskBean(fetchHibernateData(getSession().getTaskData(processData.getInternalId())));
            }
        });
		
		if(BpmTaskBeans== null){
			
			AperteWrongArgumentCodes.BPMTASK.throwAperteWebServiceException();
		}
         
         return BpmTaskBeans;
    }
	

	@Override
	@WebMethod (exclude=true)
    public BpmTaskBean getTaskData(@WebParam(name="taskId")final String taskId) throws AperteWsWrongArgumentException, AperteWsIllegalArgumentException {
		
		AperteErrorCheckUtil.checkCorrectnessOfArgument(taskId, AperteIllegalArgumentCodes.TASK_ID);
		
		BpmTaskBean taskData =  withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
            @Override
            public BpmTaskBean processWithContext(ProcessToolContext ctx) {
                return new BpmTaskBean(fetchHibernateData(getSession().getTaskData(taskId)));
            }
        });
         if(taskData==null){
        	 
        	 AperteWrongArgumentCodes.TASK_ID.throwAperteWebServiceException();
         }
		return taskData;
    }

	@Override
	@WebMethod (exclude=true)
    public List<BpmTaskBean> findUserTasks(@WebParam(name="processInstanceInternalId") String internalId,
    		@WebParam(name="userLogin") String userLogin) throws AperteWsWrongArgumentException {
		final ProcessInstance processInstance = getProcessData(internalId);
		final UserData user = findUser(userLogin);
        return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
            @Override
            public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
                return BpmTaskBean.asBeans(fetchHibernateData(getSession(user).findUserTasks(processInstance)));
            }
        }); 
    }

	@Override
	@WebMethod
    public List<BpmTaskBean> findUserTasksPaging(@WebParam(name="offset")final Integer offset,
                                             @WebParam(name="limit")final Integer limit,
                                             @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		final UserData user = findUser(userLogin);
		
        return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
            @Override
            public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
                return BpmTaskBean.asBeans(fetchHibernateData(getSession(user).findUserTasks(offset, limit)));
            }
        });
    }

	@Override
	@WebMethod
    public List<BpmTaskBean> findProcessTasks(@WebParam(name="internalId")final String internalId,
                                          @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		
		final UserData user = findUser(userLogin);
		final ProcessInstance processInstance = getProcessData(internalId);
		
        return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
            @Override
            public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
                return BpmTaskBean.asBeans(fetchHibernateData(getSession(user).findProcessTasks(processInstance)));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public List<BpmTaskBean> findProcessTasksByNames(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                                 @WebParam(name="user")final UserData user,
                                                 @WebParam(name="taskNames")final Set<String> taskNames) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
            @Override
            public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
                return BpmTaskBean.asBeans(fetchHibernateData(getSession(user).findProcessTasks(processInstance, user.getLogin(), taskNames)));
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public Integer getRecentTasksCount(@WebParam(name="minDate")final Date minDate, @WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<Integer>() {
            @Override
            public Integer processWithContext(ProcessToolContext ctx) {
                return getSession(user).getRecentTasksCount(minDate);
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public List<BpmTaskBean> getAllTasks(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
            @Override
            public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
                return BpmTaskBean.asBeans(fetchHibernateData(getSession(user).getAllTasks()));
            }
        });
    }
	
	
	
	
	
	 
	
	@Override 
    @WebMethod 
    public void performAction(@WebParam(name="procesInstanceInternalId")final String internalId,
    								@WebParam(name="actionName")final String actionName,
                                 @WebParam(name="BpmTaskBeanName")final String BpmTaskBeanName,
                                 @WebParam(name="userLogin")final String userLogin) throws  AperteWsWrongArgumentException {
		
		final BpmTaskBean task = findProcessTask(internalId, userLogin, BpmTaskBeanName);
		final UserData user = findUser(userLogin); 	
		final ProcessInstance processData = getProcessData(internalId);
		final ProcessStateAction action= getActionIfExists(processData, actionName);
		
		if(userLogin==null){
			adminCompleteTask(processData, action, task);
		}
		BpmTaskBean[] bpmnTable = {task};
		processData.setActiveTasks(bpmnTable);
		  BpmTaskBean withContextBpmnTask = withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
	            @Override
	            public BpmTaskBean processWithContext(ProcessToolContext ctx) {
	                getSession(user).performAction(action, task);
	                return null;
	            }
	        });
		
		  
            
    }
	
	
	private BpmTaskBean findProcessTask(String internalId,String userLogin,String BpmTaskBeanName ) throws AperteWsWrongArgumentException{
		List<BpmTaskBean> findProcessTasks = findProcessTasks(internalId, userLogin);
		if(findProcessTasks.isEmpty()){
			
			AperteWrongArgumentCodes.NOTASK.throwAperteWebServiceException();
		}
		if(BpmTaskBeanName == null || BpmTaskBeanName.isEmpty()){
			return findProcessTasks.get(0);
		}
		
		for (BpmTaskBean BpmTaskBeanTemp : findProcessTasks) {
			if(BpmTaskBeanTemp.getTaskName().equals(BpmTaskBeanName)){
				
				return BpmTaskBeanTemp;
				
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
        				List<BpmTask> bpmTasks = getSession().findProcessTasks(processData);
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
//			  AperteWrongArgumentCodes.BpmTaskBean.throwAperteWebServiceException();
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
                return fetchHibernateData(getSession(user).getSubstitutingUser());
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
    public void adminCancelProcessInstance(@WebParam(name="processInstance") String internalId) throws AperteWsWrongArgumentException {
		final ProcessInstance processData = getProcessData(internalId);
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
				getRegistry().getProcessToolSessionFactory().createAutoSession().adminCancelProcessInstance(processData);
                return null;
            }
        });
    }

	@Override
	@WebMethod (exclude=true)
    public void adminReassignProcessTask(@WebParam(name="processInstance")final ProcessInstance processInstance,
                                         @WebParam(name="BpmTaskBean")final BpmTaskBean BpmTaskBean,
                                         @WebParam(name="user")final UserData user) {
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
				getRegistry().getProcessToolSessionFactory().createAutoSession()
						.adminReassignProcessTask(processInstance, BpmTaskBean, user.getLogin());
                return null;
            }
        });
    }
    
	@Override
	@WebMethod (exclude=true)
    public void adminCompleteTask(@WebParam(name="procesInstanceInternalId")final ProcessInstance processData,
	@WebParam(name="action")final ProcessStateAction action,
    @WebParam(name="BpmTaskBean")final BpmTaskBean BpmTaskBean) {
	
        withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
				getRegistry().getProcessToolSessionFactory().createAutoSession()
                        .adminCompleteTask(processData, BpmTaskBean, action);
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
                                             @WebParam(name="processMapImageStream")final byte[] processMapImageStream) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) 
            {
            	ProcessDeployer processDeployer = new ProcessDeployer(ctx);
            	processDeployer.deployOrUpdateProcessDefinition(
                        new ByteArrayInputStream(processMapDefinition),
                        cfg, queues,
                        new ByteArrayInputStream(processMapImageStream));
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
		 
		return withContext(new ReturningProcessToolContextCallback<List<ProcessStateAction>>() {
			
			@Override
			public List<ProcessStateAction> processWithContext(ProcessToolContext ctx) {
				String state = null;
				if(state==null || state.isEmpty()){//TODO its for compatibility with 1.X aperte data. In future its should be removed
					if(instance.getStatus().equals(ProcessStatus.NEW)){
						List<BpmTask> bpmTasks = getSession().findProcessTasks(instance);
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

	private ProcessToolBpmSession getSession() {
		return getSession(null);
	}

	private ProcessToolBpmSession getSession(UserData user) {
		return getRegistry().getProcessToolSessionFactory().createSession(user);
	}
	
	
	
}
