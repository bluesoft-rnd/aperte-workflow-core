package org.aperteworkflow.service;

import org.aperteworkflow.service.fault.AperteWsWrongArgumentException;
import org.aperteworkflow.util.AperteErrorCheckUtil;
import org.aperteworkflow.util.AperteIllegalArgumentCodes;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueueBean;
import pl.net.bluesoft.rnd.processtool.plugins.deployment.ProcessDeployer;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;
import static pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean.asBeans;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Most of WebMethods works, some of them are taged as (exclude=true), because they allow for too much interference in the aperet workflow  data.
 * To make them work again just delete WebMethod annotaion.
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */

@WebService
public class AperteWorkflowProcessServiceImpl implements AperteWorkflowProcessService {
	private static final String PROCESS_INSTANCE_SOURCE ="portlet";

	@Override
	@WebMethod
	public ProcessInstance createProcessInstance(@WebParam(name="bpmDefinitionKey")final String bpmDefinitionKey,
												 @WebParam(name="externalKey")final String externalKey,
												 @WebParam(name="source")final String source,
												 @WebParam(name="userLogin")final String userLogin) {
		return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				StartProcessResult result = getSession(userLogin).startProcess(bpmDefinitionKey, externalKey, source);
				return fetchHibernateData(result.getProcessInstance());
			}
		});
	}

	@Override
	@WebMethod
	public ProcessInstance startProcessInstance(@WebParam(name="bpmDefinitionKey")final String bpmDefinitionKey,
												@WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		AperteErrorCheckUtil.checkCorrectnessOfArgument(bpmDefinitionKey, AperteIllegalArgumentCodes.DEFINITION);

		return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				StartProcessResult result = getSession(userLogin).startProcess(bpmDefinitionKey, null, PROCESS_INSTANCE_SOURCE);
				return fetchHibernateData(result.getProcessInstance());
			}
		});
	}

	@Override
	@WebMethod
	public Collection<ProcessQueueBean> getUserAvailableQueues(@WebParam(name="userLogin") final String userLogin) throws  AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueueBean>>() {
			@Override
			public Collection<ProcessQueueBean> processWithContext(ProcessToolContext ctx) {
				return ProcessQueueBean.asBeans(getSession(userLogin).getUserAvailableQueues());
			}
		});
	}

	@Override
	@WebMethod
	public BpmTaskBean assignTaskFromQueue(@WebParam(name="queueName")final String queueName,
										   @WebParam(name="userLogin")final String userLogin) {
		return withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(new BpmTaskBean(getSession(userLogin).assignTaskFromQueue(queueName)));
			}
		});
	}

	@Override
	@WebMethod
	public BpmTaskBean assignSpecificTaskFromQueue(@WebParam(name="queueName")final String queueName,
												   @WebParam(name="taskId")final String taskId,
												   @WebParam(name="userLogin")final String userLogin) {
		return withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(new BpmTaskBean(getSession(userLogin).assignTaskFromQueue(queueName, taskId)));
			}
		});
	}

	@Override
	@WebMethod
	public void assignTaskToUser(@WebParam(name="taskId")final String taskId, @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				getSession(userLogin).assignTaskToUser(taskId, userLogin);
				return null;
			}
		});
	}

	@Override
	@WebMethod
	public BpmTaskBean getTaskData(@WebParam(name="taskId")final String taskId) throws AperteWsWrongArgumentException {
		AperteErrorCheckUtil.checkCorrectnessOfArgument(taskId, AperteIllegalArgumentCodes.TASK_ID);

		return withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(new BpmTaskBean(getSession().getTaskData(taskId)));
			}
		});
	}

	@Override
	@WebMethod
	public List<BpmTaskBean> findUserTasks(@WebParam(name="internalId") final String internalId,
										   @WebParam(name="userLogin") final String userLogin) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
			@Override
			public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
				ProcessToolBpmSession session = getSession(userLogin);
				ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
				return fetchHibernateData(asBeans(session.findUserTasks(processInstance)));
			}
		});
	}

	@Override
	@WebMethod
	public List<BpmTaskBean> findUserTasksPaging(@WebParam(name="offset")final Integer offset,
												 @WebParam(name="limit")final Integer limit,
												 @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
			@Override
			public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(asBeans(getSession(userLogin).findUserTasks(offset, limit)));
			}
		});
	}

	@Override
	@WebMethod
	public List<BpmTaskBean> findProcessTasks(@WebParam(name="internalId")final String internalId,
											  @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
			@Override
			public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
				ProcessToolBpmSession session = getSession(userLogin);
				ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
				return fetchHibernateData(asBeans(session.findProcessTasks(processInstance)));
			}
		});
	}

	@Override
	@WebMethod
	public List<BpmTaskBean> findProcessTasksByNames(@WebParam(name="internalId")final String internalId,
													 @WebParam(name="userLogin")final String userLogin,
													 @WebParam(name="taskNames")final Set<String> taskNames) {
		return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
			@Override
			public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
				ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
				return fetchHibernateData(asBeans(getSession(userLogin).findProcessTasks(processInstance, userLogin, taskNames)));
			}
		});
	}

	@Override
	@WebMethod
	public int getRecentTasksCount(@WebParam(name="minDate")final Date minDate,
								   @WebParam(name="userLogin")final String userLogin) {
		return withContext(new ReturningProcessToolContextCallback<Integer>() {
			@Override
			public Integer processWithContext(ProcessToolContext ctx) {
				return getSession(userLogin).getRecentTasksCount(minDate);
			}
		});
	}

	@Override
	@WebMethod
	public List<BpmTaskBean> getAllTasks(@WebParam(name="userLogin")final String userLogin) {
		return withContext(new ReturningProcessToolContextCallback<List<BpmTaskBean>>() {
			@Override
			public List<BpmTaskBean> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(asBeans(getSession(userLogin).getAllTasks()));
			}
		});
	}

	@Override
	@WebMethod
	public void performAction(@WebParam(name="actionName")final String actionName,
							  @WebParam(name="taskId")final String taskId,
							  @WebParam(name="userLogin")final String userLogin) throws AperteWsWrongArgumentException {
		withContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
				getSession(userLogin).performAction(actionName, taskId);
				return null;
			}
		});
	}

	@Override
	@WebMethod
	public void adminCancelProcessInstance(@WebParam(name="internalId") final String internalId) throws AperteWsWrongArgumentException {
		withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				getRegistry().getProcessToolSessionFactory().createAutoSession().adminCancelProcessInstance(internalId);
				return null;
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public void adminReassignProcessTask(@WebParam(name="taskId")final String taskId,
										 @WebParam(name="userLogin")final String userLogin) {
		withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {

				getRegistry().getProcessToolSessionFactory().createAutoSession()
						.adminReassignProcessTask(taskId, userLogin);
				return null;
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public void adminCompleteTask(@WebParam(name="taskId") final String taskId,
								  @WebParam(name="actionName") final String actionName) {
		withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				getRegistry().getProcessToolSessionFactory().createAutoSession()
						.adminCompleteTask(taskId, actionName);
				return null;
			}
		});
	}

	//TODO GraphElement has to be an api
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
						processMapImageStream != null ? new ByteArrayInputStream(processMapImageStream) : null,
						logo != null ? new ByteArrayInputStream(logo) : null);
				return null;
			}
		});
	}

	private ProcessToolBpmSession getSession() {
		return getSession(ProcessToolBpmConstants.SYSTEM_USER.getLogin());
	}

	private ProcessToolBpmSession getSession(String userLogin) {
		UserData user = getRegistry().getUserSource().getUserByLogin(userLogin);
		if (user == null) {
			throw new RuntimeException("No user with login: " + userLogin);
		}
		return getRegistry().getProcessToolSessionFactory().createSession(userLogin);
	}
}
