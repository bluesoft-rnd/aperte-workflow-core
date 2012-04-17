package org.aperteworkflow.service;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.aperteworkflow.util.ContextUtil;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.plugins.RegistryHolder;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

import static org.aperteworkflow.util.ContextUtil.withContext;
import static org.aperteworkflow.util.HibernateBeanUtil.fetchHibernateData;

/**
 * @author tlipski@bluesoft.net.pl
 */
@WebService
public class AperteWorkflowDataServiceImpl {


    @WebMethod
    public long saveProcessInstance(@WebParam(name="processInstance")final ProcessInstance processInstance) {
        return withContext(new ReturningProcessToolContextCallback<Long>() {
            @Override
            public Long processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
            }
        });
    }

    @WebMethod
    public ProcessInstance getProcessInstance(@WebParam(name="id")final long id) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstance(id));
            }
        });
    }

    @WebMethod
    public List<ProcessInstance> getProcessInstances(@WebParam(name="ids")final Collection<Long> ids) {
        return withContext(new ReturningProcessToolContextCallback<List<ProcessInstance>>() {
            @Override
            public List<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstances(ids));
            }
        });
    }

    @WebMethod
    public ProcessInstance getProcessInstanceByInternalId(@WebParam(name="internalId")final String internalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId));
            }
        });
    }

    @WebMethod
    public ProcessInstance getProcessInstanceByExternalId(@WebParam(name="externalId")final String externalId) {
        return withContext(new ReturningProcessToolContextCallback<ProcessInstance>() {
            @Override
            public ProcessInstance processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().getProcessInstanceByExternalId(externalId));
            }
        });
    }

    @WebMethod
    public List<ProcessInstance> findProcessInstancesByKeyword(@WebParam(name="key")final String key, @WebParam(name="processType")final String processType) {
        return withContext(new ReturningProcessToolContextCallback<List<ProcessInstance>>() {
            @Override
            public List<ProcessInstance> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().findProcessInstancesByKeyword(key, processType));
            }
        });
    }

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

    @WebMethod
    public UserData findOrCreateUser(@WebParam(name="user")final UserData user) {
        return withContext(new ReturningProcessToolContextCallback<UserData>() {
            @Override
            public UserData processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessInstanceDAO().findOrCreateUser(user));
            }
        });
    }

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

    @WebMethod
    public Collection<ProcessDefinitionConfig> getAllConfigurations() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getAllConfigurations());
            }
        });
    }


    @WebMethod
    public Collection<ProcessDefinitionConfig> getActiveConfigurations() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurations());
            }
        });
    }

    @WebMethod
    public ProcessDefinitionConfig getActiveConfigurationByKey(@WebParam(name="key")final String key) {
        return withContext(new ReturningProcessToolContextCallback<ProcessDefinitionConfig>() {
            @Override
            public ProcessDefinitionConfig processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(key));
            }
        });
    }

    @WebMethod
    public Collection<ProcessQueueConfig> getQueueConfigs() {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessQueueConfig>>() {
            @Override
            public Collection<ProcessQueueConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getQueueConfigs());
            }
        });
    }

    @WebMethod
    public ProcessStateConfiguration getProcessStateConfiguration(@WebParam(name="task")final BpmTask task) {
        return withContext(new ReturningProcessToolContextCallback<ProcessStateConfiguration>() {
            @Override
            public ProcessStateConfiguration processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task));
            }
        });
    }

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

    @WebMethod
    public Collection<ProcessDefinitionConfig> getConfigurationVersions(@WebParam(name="cfg")final ProcessDefinitionConfig cfg) {
        return withContext(new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
            @Override
            public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
                return fetchHibernateData(ctx.getProcessDefinitionDAO().getConfigurationVersions(cfg));
            }
        });
    }

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

    @WebMethod
    public List<String> getAvailableLogins(@WebParam(name="filter")final String filter) {
        return withContext(new ReturningProcessToolContextCallback<List<String>>() {
            @Override
            public List<String> processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getAvailableLogins(filter);
            }
        });
    }

    @WebMethod
    public byte[] getProcessLatestDefinition(@WebParam(name="bpmDefinitionKey")final String bpmDefinitionKey,
                                             @WebParam(name="processName")final String processName) {
        return ContextUtil.withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getProcessLatestDefinition(bpmDefinitionKey, processName);
            }
        });
    }

    @WebMethod
    public byte[] getProcessDefinition(@WebParam(name="pi")final ProcessInstance pi) {
        return withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getProcessDefinition(pi);
            }
        });
    }

    @WebMethod
    public byte[] getProcessMapImage(@WebParam(name="pi")final ProcessInstance pi) {
        return withContext(new ReturningProcessToolContextCallback<byte[]>() {
            @Override
            public byte[] processWithContext(ProcessToolContext ctx) {
                return ctx.getProcessToolSessionFactory().createSession(null, new HashSet<String>())
                        .getProcessMapImage(pi);
            }
        });
    }


}
