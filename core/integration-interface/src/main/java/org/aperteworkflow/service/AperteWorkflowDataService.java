package org.aperteworkflow.service;

import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.aperteworkflow.service.fault.AperteWebServiceError;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface AperteWorkflowDataService {

    long saveProcessInstance(ProcessInstance processInstance);
    ProcessInstance getProcessInstance(long id);
    List<ProcessInstance> getProcessInstances(Collection<Long> ids);
    ProcessInstance getProcessInstanceByInternalId(String internalId) throws AperteWebServiceError;
    ProcessInstance getProcessInstanceByExternalId(String externalId);
    List<ProcessInstance> findProcessInstancesByKeyword(String key, String processType);
//    Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(Collection<String> internalId);
    //void deleteProcessInstance(ProcessInstance instance);
    void deleteProcessInstance(String internalId);
   // Collection<ProcessInstanceLog> getUserHistory(UserData user, Date startDate, Date endDate);
    Collection<ProcessInstanceLog> getUserHistory(String userLogin, Date startDate, Date endDate) throws AperteWebServiceError;
    UserData findOrCreateUser(UserData ud);
    Collection<ProcessInstance> searchProcesses(String filter, int offset, int limit, boolean onlyRunning,
                                                String[] userRoles, String assignee, String... queues);
    Collection<ProcessInstance> getUserProcessesAfterDate(UserData userData, Calendar minDate);
    ResultsPageWrapper<ProcessInstance> getRecentProcesses(UserData userData, Calendar minDate, Integer offset, Integer limit);
//    ResultsPageWrapper<ProcessInstance> getProcessInstanceByInternalIdMapWithFilter(Collection<String> internalIds, ProcessInstanceFilter filter,
//                                                                                    Integer offset, Integer limit);
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

  //  byte[] getProcessDefinition(ProcessInstance pi);
  //  byte[] getProcessMapImage(ProcessInstance pi);
	UserData findUser(String userLogin) throws  AperteWebServiceError;
	Collection<ProcessInstance> getUserProcessesBetweenDatesByUserLogin(
			String userLogin, Calendar minDate, Calendar maxDate) throws  AperteWebServiceError;
	byte[] getProcessDefinition(String internalId) throws  AperteWebServiceError;
	byte[] getProcessMapImage(String internalId) throws  AperteWebServiceError;
	ProcessInstanceSimpleAttribute setSimpleAttribute(String key,
			 String newValue, String internalId) throws  AperteWebServiceError;
	String getSimpleAttributeValue(String key, String internalId) throws  AperteWebServiceError;
	List<ProcessStateAction> getActionsListByNameFromInstance(
			String internalId, String actionName) throws  AperteWebServiceError;
	List<ProcessStateAction> getAvalivableActionForProcess(String internalId) throws  AperteWebServiceError;
	List<ProcessInstanceSimpleAttribute> getSimpleAttributesList(String internalId) throws  AperteWebServiceError;
	Map<String, ProcessInstance> getProcessInstanceByInternalIdMap(
			Collection<String> internalIds);
	byte[] getProcessLatestDefinition(String bpmDefinitionKey,
			String processName);
	List<ProcessStateAction> getAllActionsListFromDefinition(String internalId)
			throws  AperteWebServiceError;


}