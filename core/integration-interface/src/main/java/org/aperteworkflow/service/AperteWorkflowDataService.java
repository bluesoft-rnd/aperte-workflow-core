package org.aperteworkflow.service;

import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface AperteWorkflowDataService {

    long saveProcessInstance(ProcessInstance processInstance);
    ProcessInstance getProcessInstance(long id);
    List<ProcessInstance> getProcessInstances(Collection<Long> ids);
    ProcessInstance getProcessInstanceByInternalId(String internalId);
    ProcessInstance getProcessInstanceByExternalId(String externalId);
    List<ProcessInstance> findProcessInstancesByKeyword(String key, String processType);
//    Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(Collection<String> internalId);
    void deleteProcessInstance(ProcessInstance instance);
    Collection<ProcessInstanceLog> getUserHistory(UserData user, Date startDate, Date endDate);
    UserData findOrCreateUser(UserData ud);
    Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit, boolean onlyRunning,
                                                String[] userRoles, String assignee, String... queues);
    Collection<ProcessInstance> getUserProcessesAfterDate(UserData userData, Calendar minDate);
    ResultsPageWrapper<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate, Integer offset, Integer limit);
    ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(Collection<String> internalIds, ProcessInstanceFilter filter,
                                                                                    Integer offset, Integer limit);
    Collection<ProcessDefinitionConfig> getAllConfigurations();
    Collection<ProcessDefinitionConfig> getActiveConfigurations();
    ProcessDefinitionConfig getActiveConfigurationByKey(String key);
    Collection<ProcessQueueConfig> getQueueConfigs();
    ProcessStateConfiguration getProcessStateConfiguration(BpmTask task);
    void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg);
    void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled);
    Collection<ProcessDefinitionConfig> getConfigurationVersions(ProcessDefinitionConfig cfg);
    void updateOrCreateQueueConfigs(Collection<ProcessQueueConfig> cfgs);
    void removeQueueConfigs(Collection<ProcessQueueConfig> cfgs);

    List<String> getAvailableLogins(final String filter);

    byte[] getProcessLatestDefinition(String bpmDefinitionKey, String processName);
    byte[] getProcessDefinition(ProcessInstance pi);
    byte[] getProcessMapImage(ProcessInstance pi);

}
