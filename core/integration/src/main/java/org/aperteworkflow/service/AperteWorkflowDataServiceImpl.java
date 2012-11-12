package org.aperteworkflow.service;


import org.aperteworkflow.util.ContextUtil;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.*;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;

/**
 * @author tlipski@bluesoft.net.pl
 */
@WebService
public class AperteWorkflowDataServiceImpl implements AperteWorkflowDataService {

	@Override
    @WebMethod
    public long saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        return withContext(new ReturningProcessToolContextCallback<Long>() {
            @Override
            public Long processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
            }
        });
    }

	@Override
    @WebMethod
    public ProcessInstance getProcessInstance(@WebParam(name="id")final long id) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstance(id));
            }
        });
    }

	@Override
    @WebMethod
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
    public ProcessInstance getProcessInstanceByInternalId(@WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId));
            }
        });
    }

	@Override
    @WebMethod
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
    public List<ProcessInstance> findProcessInstancesByKeyword(@WebParam(name="key")final String key, @WebParam(name="processType")final String processType) {
        return withContext(new ReturningProcessToolContextCallback<List<ProcessInstance>>() {
            @Override
            public List<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().findProcessInstancesByKeyword(key, processType));
            }
        });
    }

//	@Override
//	@WebMethod
//	public Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(@WebParam(name="internalIds")final Collection<String> internalIds) {
//		return null;  // TODO
//	}

	@Override
	@WebMethod
    public void deleteProcessInstance(@WebParam(name="instance")final ProcessInstance instance) {
        withContext(new ReturningProcessToolContextCallback() {
            @Override
            public Object processWithContext(ProcessToolContext ctx) {
                ctx.getProcessInstanceDAO().deleteProcessInstance(instance);
                return null;
            }
        });
    }

	@Override
    @WebMethod
    public Collection<ProcessInstanceLog> getUserHistory(@WebParam(name="user")final UserData user,
                                                         @WebParam(name="startDate")final Date startDate,
                                                         @WebParam(name="endDate")final Date endDate) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstanceLog>>() {
            @Override
            public Collection<ProcessInstanceLog> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getUserHistory(user, startDate, endDate));
            }
        });
    }

	@Override
    @WebMethod
    public UserData findOrCreateUser(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().findOrCreateUser(user));
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
                                                       @WebParam(name="queues")final String... queues) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
            @Override
            public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().searchProcesses(filter, offset, limit, onlyRunning, userRoles, assignee, queues));
            }
        });
    }

	@Override
    @WebMethod
    public Collection<ProcessInstance> getUserProcessesAfterDate(@WebParam(name="user")final UserData user,
                                                                 @WebParam(name="minDate")final Calendar minDate) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessInstance>>() {
            @Override
            public Collection<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getUserProcessesAfterDate(user, minDate));
            }
        });
    }

	@Override
    @WebMethod
    public ResultsPageWrapper<ProcessInstance> getRecentProcesses(@WebParam(name="user")final UserData user,
                                                                  @WebParam(name="minDate")final Calendar minDate,
                                                                  @WebParam(name="offset")final Integer offset,
                                                                  @WebParam(name="limit")final Integer limit) {
        return withContext(new ReturningProcessToolContextCallback<ResultsPageWrapper<ProcessInstance>>() {
            @Override
            public ResultsPageWrapper<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getRecentProcesses(user, minDate, offset, limit));
            }
        });
    }

	@Override
    @WebMethod
    public ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(@WebParam(name="internalIds")final Collection<String> internalIds,
                                                                                           @WebParam(name="filter")final ProcessInstanceFilter filter,
                                                                                           @WebParam(name="offset")final Integer offset,
                                                                                           @WebParam(name="limit")final Integer limit) {
        return withContext(new ReturningProcessToolContextCallback<ResultsPageWrapper<ProcessInstance>>() {
            @Override
            public ResultsPageWrapper<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMapWithFilter(internalIds, filter, offset, limit));
            }
        });
    }

	@Override
    @WebMethod
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
    @WebMethod
    public ProcessDefinitionConfig getActiveConfigurationByKey(@WebParam(name="key")final String key) {
        return withContext(new ReturningProcessToolContextCallback<ProcessDefinitionConfig>() {
            @Override
            public ProcessDefinitionConfig processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(key));
            }
        });
    }

	@Override
    @WebMethod
    public Collection<ProcessQueueConfig> getQueueConfigs() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueueConfig>>() {
            @Override
            public Collection<ProcessQueueConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getQueueConfigs());
            }
        });
    }

	@Override
    @WebMethod
    public ProcessStateConfiguration getProcessStateConfiguration(@WebParam(name="task")final BpmTask task) {
        return withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task));
            }
        });
    }

	@Override
    @WebMethod
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
    @WebMethod
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
    @WebMethod
    public Collection<ProcessDefinitionConfig> getConfigurationVersions(@WebParam(name="cfg")final ProcessDefinitionConfig cfg) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getConfigurationVersions(cfg));
            }
        });
    }

	@Override
    @WebMethod
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
    @WebMethod
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
    @WebMethod
    public List<String> getAvailableLogins(@WebParam(name="filter")final String filter) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return getSession(ctx)
                        .getAvailableLogins(filter);
            }
        });
    }

	@Override
    @WebMethod
    public byte[] getProcessLatestDefinition(@WebParam(name="bpmDefinitionKey")final String bpmDefinitionKey,
                                             @WebParam(name="processName")final String processName) {
        return ContextUtil.withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return getSession(ctx)
                        .getProcessLatestDefinition(bpmDefinitionKey, processName);
            }
        });
    }

	@Override
    @WebMethod
    public byte[] getProcessDefinition(@WebParam(name="pi")final ProcessInstance pi) {
        return withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return getSession(ctx)
                        .getProcessDefinition(pi);
            }
        });
    }

	@Override
    @WebMethod
    public byte[] getProcessMapImage(@WebParam(name="pi")final ProcessInstance pi) {
        return withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return getSession(ctx)
                        .getProcessMapImage(pi);
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
