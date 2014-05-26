package org.aperteworkflow.service;


import org.aperteworkflow.service.fault.AperteWsIllegalArgumentException;
import org.aperteworkflow.service.fault.AperteWsWrongArgumentException;
import org.aperteworkflow.util.AperteErrorCheckUtil;
import org.aperteworkflow.util.AperteIllegalArgumentCodes;
import org.aperteworkflow.util.AperteWrongArgumentCodes;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Most of WebMethods works, some of them are taged as (exclude=true), because they allow for too much interference in the aperet workflow  data.
 * To make them work again just delete WebMethod annotaion.
 * @author tlipski@bluesoft.net.pl  
 * @author kkolodziej@bluesoft.net.pl
 */
@WebService
public class AperteWorkflowDataServiceImpl implements AperteWorkflowDataService {
	@Override
	@WebMethod(exclude=true)
	public long saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
		return withContext(new ReturningProcessToolContextCallback<Long>() {
			@Override
			public Long processWithContext(ProcessToolContext ctx) {
				return ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public ProcessInstance getProcessInstance(@WebParam(name="id")final long id) {
		return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstance(id));
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public List<ProcessInstance> getProcessInstances(@WebParam(name="ids")final Collection<Long> ids) {
		return withContext(new ReturningProcessToolContextCallback<List<ProcessInstance>>() {
			@Override
			public List<ProcessInstance> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstances(ids));
			}
		});
	}

	@Override
	@WebMethod
	public ProcessInstance getProcessInstanceByInternalId(@WebParam(name="internalId")final String internalId) throws  AperteWsWrongArgumentException {
		ProcessInstance processInstance = withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId));
			}
		});

		if(processInstance==null){
			AperteWrongArgumentCodes.PROCESS.throwAperteWebServiceException();
		}
		return processInstance;

	}

	@Override
	@WebMethod (exclude=true)
	public ProcessInstance getProcessInstanceByExternalId(@WebParam(name="externalId")final String externalId) {
		return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByExternalId(externalId));
			}
		});
	}

	@Override
	@WebMethod
	public HashMap<String, ProcessInstance> getProcessInstanceByInternalIdMap(@WebParam(name="internalIds")final Collection<String> internalIds) {
		return withContext(new ReturningProcessToolContextCallback<HashMap<String, ProcessInstance>>() {
			@Override
			public HashMap<String, ProcessInstance> processWithContext(ProcessToolContext ctx) {
				return (HashMap<String, ProcessInstance>) fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(internalIds));
			}
		});
	}

	@Override
	@WebMethod
	public void deleteProcessInstance(@WebParam(name="internalId") String internalId) throws AperteWsWrongArgumentException {
		final ProcessInstance processInstance = getProcessInstanceByInternalId(internalId);
		withContext(new ReturningProcessToolContextCallback() {
			@Override
			public Object processWithContext(ProcessToolContext ctx) {
				ctx.getProcessInstanceDAO().deleteProcessInstance(processInstance);
				return null;
			}
		});
	}

	@Override
	@WebMethod
	public Collection<ProcessInstanceLog> getUserHistory(@WebParam(name="userLogin") final String userLogin,
														 @WebParam(name="startDate")final Date startDate,
														 @WebParam(name="endDate")final Date endDate) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstanceLog>>() {
			@Override
			public Collection<ProcessInstanceLog> processWithContext(ProcessToolContext ctx)  {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getUserHistory(userLogin, startDate, endDate));
			}
		});
	}


	@Override
	@WebMethod
	public UserDataBean getUserByLogin(@WebParam(name = "userLogin") final String userLogin) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<UserDataBean>() {
			@Override
			public UserDataBean processWithContext(ProcessToolContext ctx) {
				UserData userData = getRegistry().getUserSource().getUserByLogin(userLogin);
				return userData != null ? new UserDataBean(userData) : null;
			}
		});
	}



	@Override
	@WebMethod
	public void setSimpleAttribute(@WebParam(name="key")final String key,
															 @WebParam(name="newValue")final String newValue,
															 @WebParam(name="internalId") final String internalId) throws AperteWsWrongArgumentException {
		withContext(new ReturningProcessToolContextCallback<Void>() {
			@Override
			public Void processWithContext(ProcessToolContext ctx) {
				ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
				ctx.getProcessInstanceSimpleAttributeDAO().setSimpleAttribute(processInstance.getId(), key, newValue);
				return null;
			}
		});
	}

	@Override
	@WebMethod
	public String getSimpleAttributeValue(@WebParam(name="key")final String key,@WebParam(name="internalId") final String internalId) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<String>() {
			@Override
			public String processWithContext(ProcessToolContext ctx) {
				ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
				return ctx.getProcessInstanceSimpleAttributeDAO().getSimpleAttributeValue(processInstance.getId(), key);
			}
		});
	}

	@Override
	@WebMethod
	public HashMap<String, String> getSimpleAttributesList(@WebParam(name="internalId") final String internalId) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<HashMap<String, String>>() {
			@Override
			public HashMap<String, String> processWithContext(ProcessToolContext ctx) {
				ProcessInstance processInstance = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
				return new HashMap<String, String>(fetchHibernateData(ctx.getProcessInstanceSimpleAttributeDAO().getSimpleAttributesList(processInstance.getId())));
			}
		});
	}

	@Override
	@WebMethod
	public Collection<ProcessInstance> searchProcesses(@WebParam(name="filter")final String filter,
													   @WebParam(name="offset")final int offset,
													   @WebParam(name="limit")final int limit,
													   @WebParam(name="onlyRunning")final boolean onlyRunning,
													   @WebParam(name="userRoles")final String[] userRoles,
													   @WebParam(name="assignee")final String assignee,
													   @WebParam(name="queues")final String... queues) throws AperteWsIllegalArgumentException {

		AperteErrorCheckUtil.checkCorrectnessOfArgument(filter, AperteIllegalArgumentCodes.FILTR);

		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
			@Override
			public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().searchProcesses(filter, offset, limit, onlyRunning, userRoles, assignee, queues));
			}
		});
	}



	@Override
	@WebMethod
	public Collection<ProcessInstance> getUserProcessesBetweenDatesByUserLogin(@WebParam(name="userLogin")final String userLogin,
																			   @WebParam(name="minDate")final Date minDate,
																			   @WebParam(name="maxDate")final Date maxDate) throws AperteWsWrongArgumentException {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
			@Override
			public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {

				return fetchHibernateData(ctx.getProcessInstanceDAO().getUserProcessesBetweenDates(userLogin, minDate, maxDate));
			}
		});
	}


	@Override
	@WebMethod (exclude=true)
	public Collection<ProcessInstance> getUserProcessesAfterDate(@WebParam(name="user")final UserDataBean user,
																 @WebParam(name="minDate")final Date minDate) {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
			@Override
			public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getUserProcessesAfterDate(user.getLogin(), minDate));
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public ResultsPageWrapper<ProcessInstance> getRecentProcesses(@WebParam(name="user")final UserDataBean user,
																  @WebParam(name="minDate")final Date minDate,
																  @WebParam(name="offset")final Integer offset,
																  @WebParam(name="limit")final Integer limit) {
		return withContext(new ReturningProcessToolContextCallback<ResultsPageWrapper<ProcessInstance>>() {
			@Override
			public ResultsPageWrapper<ProcessInstance> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessInstanceDAO().getRecentProcesses(user.getLogin(), minDate, offset, limit));
			}
		});
	}

//	@Override
//	@WebMethod
//    public ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(@WebParam(name="internalIds")final Collection<String> internalIds,
//                                                                                           @WebParam(name="filter")final ProcessInstanceFilter filter,
//                                                                                           @WebParam(name="offset")final Integer offset,
//                                                                                           @WebParam(name="limit")final Integer limit) {
//        return withContext(new ReturningProcessToolContextCallback<ResultsPageWrapper<ProcessInstance>>() {
//            @Override
//            public ResultsPageWrapper<ProcessInstance> processWithContext(ProcessToolContext ctx) {
//                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMapWithFilter(internalIds, filter, offset, limit));
//            }
//        });
//    }

	@Override
	@WebMethod (exclude=true)
	public Collection<ProcessDefinitionConfig> getAllConfigurations() {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
			@Override
			public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessDefinitionDAO().getAllConfigurations());
			}
		});
	}

	@Override
	@WebMethod
	public Collection<ProcessDefinitionConfig> getActiveConfigurations() {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
			@Override
			public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurations());
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public ProcessDefinitionConfig getActiveConfigurationByKey(@WebParam(name="key")final String key) throws AperteWsWrongArgumentException {
		ProcessDefinitionConfig processDefinitionConfig = withContext(new ReturningProcessToolContextCallback<ProcessDefinitionConfig>() {
			@Override
			public ProcessDefinitionConfig processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(key));
			}
		});

		if(processDefinitionConfig == null){
			AperteWrongArgumentCodes.DEFINITION.throwAperteWebServiceException();
		}

		return processDefinitionConfig;
	}

	@Override
	@WebMethod (exclude=true)
	public Collection<ProcessQueueConfig> getQueueConfigs() {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueueConfig>>() {
			@Override
			public Collection<ProcessQueueConfig> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessDefinitionDAO().getQueueConfigs());
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public ProcessStateConfiguration getProcessStateConfiguration(@WebParam(name="task")final BpmTaskBean task) {
		return withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
			@Override
			public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(task.getCurrentProcessStateConfiguration());
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public void updateOrCreateProcessDefinitionConfig(@WebParam(name="cfg")final ProcessDefinitionConfig cfg) {
		withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
			@Override
			public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
				ctx.getProcessDefinitionDAO().updateOrCreateProcessDefinitionConfig(cfg);
				return null;
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public void setConfigurationEnabled(@WebParam(name="cfg")final ProcessDefinitionConfig cfg, @WebParam(name="enabled")final boolean enabled) {
		withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
			@Override
			public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
				ctx.getProcessDefinitionDAO().setConfigurationEnabled(cfg, enabled);
				return null;
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public Collection<ProcessDefinitionConfig> getConfigurationVersions(@WebParam(name="cfg")final ProcessDefinitionConfig cfg) {
		return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
			@Override
			public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
				return fetchHibernateData(ctx.getProcessDefinitionDAO().getConfigurationVersions(cfg));
			}
		});
	}


	/*
	 * FIXME
	 * Service does not work when you try to update, there is an exception:
	 * "[Hibernate] NonUniqueObjectException: a different object with the same identifier value was already associated with the session"
	 * probably swap in the classroom:
	 * "ProcessDefinitionDaoImpl" in the method: "updateOrCreateQueueConfigs" "save" the "merge "or" saveOrUpdate "solve the problem.
	 */
	@Override
	@WebMethod (exclude=true)
	public void updateOrCreateQueueConfigs(@WebParam(name="cfgs")final Collection<ProcessQueueConfig> cfgs) {
		withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
			@Override
			public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
				ctx.getProcessDefinitionDAO().updateOrCreateQueueConfigs(cfgs);
				return null;
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public void removeQueueConfigs(@WebParam(name="cfgs")final Collection<ProcessQueueConfig> cfgs) {
		withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
			@Override
			public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
				ctx.getProcessDefinitionDAO().removeQueueConfigs(cfgs);
				return null;
			}
		});
	}

	@Override
	@WebMethod (exclude=true)
	public List<String> getAvailableLogins(@WebParam(name="filter")final String filter) {
		return withContext(new ReturningProcessToolContextCallback<List<String>>() {
			@Override
			public List<String> processWithContext(ProcessToolContext ctx) {
				return getSession().getAvailableLogins(filter);
			}
		});
	}

	@Override
	@WebMethod(exclude=true)
	public byte[] getProcessLatestDefinition(@WebParam(name="bpmDefinitionKey")final String bpmDefinitionKey,
											 @WebParam(name="processName") String processName) throws AperteWsIllegalArgumentException {

		AperteErrorCheckUtil.checkCorrectnessOfArgument(bpmDefinitionKey, AperteIllegalArgumentCodes.DEFINITION);
		AperteErrorCheckUtil.checkCorrectnessOfArgument(processName, AperteIllegalArgumentCodes.PROCESS);

		return withContext(new ReturningProcessToolContextCallback<byte[]>() {
			@Override
			public byte[] processWithContext(ProcessToolContext ctx) {
				return getSession().getProcessLatestDefinition(bpmDefinitionKey);
			}
		});
	}



	@Override
	@WebMethod(exclude=true)
	public byte[] getProcessDefinition(@WebParam(name="internalId") String internalId) throws  AperteWsWrongArgumentException {
		final ProcessInstance processInstanceByInternalId = getProcessInstanceByInternalId(internalId);
		return withContext(new ReturningProcessToolContextCallback<byte[]>() {
			@Override
			public byte[] processWithContext(ProcessToolContext ctx) {
				return getSession().getProcessDefinition(processInstanceByInternalId);
			}
		});
	}

	@Override
	@WebMethod(exclude=true)
	public byte[] getProcessMapImage(@WebParam(name="internalId") String internalId) throws  AperteWsWrongArgumentException {
		final ProcessInstance processInstanceByInternalId = getProcessInstanceByInternalId(internalId);
		return withContext(new ReturningProcessToolContextCallback<byte[]>() {
			@Override
			public byte[] processWithContext(ProcessToolContext ctx) {
				return getSession().getProcessMapImage(processInstanceByInternalId);
			}
		});
	}

	private ProcessToolBpmSession getSession() {
		return getSession(null);
	}

	private ProcessToolBpmSession getSession(UserDataBean user) {
		return getRegistry().getProcessToolSessionFactory().createSession(user.getLogin());
	}
}
