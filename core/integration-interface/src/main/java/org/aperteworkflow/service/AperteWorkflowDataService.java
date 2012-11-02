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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.aperteworkflow.service.fault.AperteWebServiceError;

/**
 * @author tlipski@bluesoft.net.pl
 */
/**
 * @author ”kkolodziej@bluesoft.net.pl”
 *
 */
public interface AperteWorkflowDataService {

	/**
	 * 
	 *  Service saves the modified instances, or create new one. But only in the pt_process_instance table!
	 *  
	 * @param Process Instance to save.
	 * @return
	 */
	long saveProcessInstance(ProcessInstance processInstance);

	/**
	 * 
	 * Return the process Instance based on ID.
	 * @param id
	 * @return
	 */
	ProcessInstance getProcessInstance(long id);

	/**
	 * 
	 * Returns multiple instances based on list of ID.
	 * @param ids list of process instance ids
	 * @return
	 */
	List<ProcessInstance> getProcessInstances(Collection<Long> ids);

	/**
	 * 
	 * Returns the process instance, on the basis of internalID. that is given by aperte and visible to the user in the system such as: "Complaint.740020"
	 * 
	 * @param internalId  InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	ProcessInstance getProcessInstanceByInternalId(String internalId)
			throws AperteWebServiceError;

	/**
	 * 
	 * Returns the process instance, on the basis of externalId.
	 * 
	 * 
	 * @param externalId externalId from pt_process_instance table
	 * @return
	 */
	ProcessInstance getProcessInstanceByExternalId(String externalId);

	/**
	 * 
	 * Returns the process instance, on the basis of externalId.
	 * 
	 * 
	 * @param key key from pt_process_instance table
	 * @param processType do nothing, filing this attribute, has no consequence.
	 * @return
	 */
	List<ProcessInstance> findProcessInstancesByKeyword(String key,
			String processType);

	/**
	 * Process deletion.
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @throws AperteWebServiceError 
	 */
	void deleteProcessInstance(String internalId) throws AperteWebServiceError;

	/**
	 * 
	 * Returns the full history of the process for the user, the information is retrieved from the pt_process_instance_log. 
	 * "dateFrom" and "dateTo" are not requierd.  The fields only adds upper  and lower restrictions. By default set is right-open.
	 * 
	 * @param userLogin user login 
	 * @param startDate date from, i.e. YYYY-MM-DD (the field is not required)
	 * @param endDate date to, i.e. YYYY-MM-DD (the field is not required)
	 * @return
	 * @throws AperteWebServiceError
	 */
	Collection<ProcessInstanceLog> getUserHistory(String userLogin,
			Date startDate, Date endDate) throws AperteWebServiceError;

	/**
	 * The method creates user, or  returns existing one if logins  match each other.
	 * 
	 * @param ud User Informations*
	 * @return
	 */
	UserData findOrCreateUser(UserData ud);

	/**
	 * 
	 * Method finds the process instance using Lucena. 
	 * 
	 * @param filter The keyword which is used by search engine
	 * @param offset The offset in the results list. (the field is not required)
	 * @param limit The number of results to be displayed
	 * @param onlyRunning (the field is not required)
	 * @param userRoles roles  (the field is not required)
	 * @param assignee the person assigned (the field is not required)
	 * @param queues queues (the field is not required)
	 * @return
	 * @throws AperteWebServiceError 
	 */
	Collection<ProcessInstance> searchProcesses(String filter, int offset,
			int limit, boolean onlyRunning, String[] userRoles,
			String assignee, String... queues) throws AperteWebServiceError;

	/**
	 * 
	 * Returns list of Process Instance which assigned user is userData, and entry date is no older than minDate.
	 * @param userData User data for which process instance will be returned.
	 * @param minDate The oldest allowed date.
	 * @return
	 */
	Collection<ProcessInstance> getUserProcessesAfterDate(UserData userData,
			Calendar minDate);

	/**
	 * 
	 * Returns process witch assigned user is userData, , and entry date is no older than minDate. Results are limited with limit, and offset.
	 * @param userData User data for which process instance will be returned.
	 * @param minDate The oldest allowed date.
	 * @param offset Number of first element.
	 * @param limit Maximum number of results.
	 * @return
	 */
	ResultsPageWrapper<ProcessInstance> getRecentProcesses(UserData userData,
			Calendar minDate, Integer offset, Integer limit);

	/**
	 * Service returns a list of  all, process definitions. From the table: "pt_process_definition_config".
	 * 
	 * @return
	 */
	Collection<ProcessDefinitionConfig> getAllConfigurations();

	/**
	 * Service returns a list of  active, process definitions. 
	 *  Active in this case means, that  fields "enabled" has value: "true" or "null" , and the "latest" is set to true.  In the table: "pt_process_definition_config".
	 * 
	 * @return
	 */
	Collection<ProcessDefinitionConfig> getActiveConfigurations();

	/**
	 * Return all active configuration (look getActiveConfigurations method), based on bpmDefinitionKey eg. Reservation.
	 * 
	 * @param key bpmDefinitionKey from the table: "pt_process_definition_config".
	 * @return
	 * @throws AperteWebServiceError 
	 */
	ProcessDefinitionConfig getActiveConfigurationByKey(String key) throws AperteWebServiceError;

	/**
	 * Returns list of ProcessQueueConfig, its based on table pt_process_queue_config
	 * 
	 * @return
	 */
	Collection<ProcessQueueConfig> getQueueConfigs();

	/**
	 * 
	 * Returns process stace configuration, its based on pt_process_state_config.
	 * @param task BpmTask only "taskName", and "processInstance/definition/id" is mandatory
	 * @return
	 */
	ProcessStateConfiguration getProcessStateConfiguration(BpmTask task);

	/**
	 * Creates a new process or alter the configuration of the old one. 
	 * Decision of modify ore not is made in the method: "pl.net.bluesoft.rnd.processtool.dao.impl.ProcessDefinitionDAOImpl.compareDefinitions"
	 * 
	 * @param cfg Process Definition Configuration to create or modify. 
	 */
	void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg);

	/**
	 * 
	 * Sets the value of the "enable" in pt_process_definition_config for the configuration process.
	 * 
	 * @param cfg ProcessDefinitionConfig only id is mandatory
	 * @param enabled Value of the "enable" in pt_process_definition_config table.
	 */
	void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled);

	/**
	 * Method returns all Process Definition Configuration based on bpmDefinitionKey in pt_process_definition_config
	 * 
	 * @param cfg ProcessDefinitionConfig only bpmDefinitionKey is taken for consideration.
	 * @return
	 */
	Collection<ProcessDefinitionConfig> getConfigurationVersions(
			ProcessDefinitionConfig cfg);

	/**
	 * Method creates or updates  ProcessQueueConfig 
	 * 
	 * FIXME 
	 * Method don't work! For more information look in implementation.! 
	 * 
	 * @param cfgs List of queue configurations to modify, only "name" field is taken under consideration. 
	 */
	void updateOrCreateQueueConfigs(Collection<ProcessQueueConfig> cfgs);

	/**
	 * Delete queue configuration from Data base.
	 * 
	 * @param cfgs List of queue configurations to modify, only "name" field is taken under consideration. 
	 */
	void removeQueueConfigs(Collection<ProcessQueueConfig> cfgs);

	/**
	 * The service scans the tables Jbpm "jbpm4_id_user" the id field, which is actually the login as follows: "select user from jbpm4_id_user where id like filter"
	 * The "Activity" is a search on the mail in the same manner
	 * 
	 * @param filter user login or email which data base will be searched.
	 * @return
	 */
	List<String> getAvailableLogins(final String filter);

	/**
	 * 
	 * Method searches user, based one userLogin.
	 * 
	 * @param userLogin user login
	 * @return
	 * @throws AperteWebServiceError
	 */
	UserData findUser(String userLogin) throws AperteWebServiceError;

	/**
	 * 
	 * Search process for the user instance in which he took part in a given period of time, "minDate" and "maxDate" are not required.  
	 * The fields only adds upper  and lower restrictions. By default set is right-open.
	 * 
	 * @param userLogin user login
	 * @param minDate date ,i.e. YYYY-MM-DD (the field is not required)
	 * @param maxDate date ,i.e. YYYY-MM-DD (the field is not required)
	 * @return
	 * @throws AperteWebServiceError
	 */
	Collection<ProcessInstance> getUserProcessesBetweenDatesByUserLogin(
			String userLogin, Calendar minDate, Calendar maxDate)
			throws AperteWebServiceError;

	/**
	 * 
	 * Method returns ". Jpdl.xml" in the form of a byte for the "ProcessInstance", so reviewing historical entries is possible.
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	byte[] getProcessDefinition(String internalId) throws AperteWebServiceError;

	/**
	 * 
	 * The method returns a representation of the process (*. Png) in the form of a byte.
	 * 
	 * @param internalId
	 * @return
	 * @throws AperteWebServiceError
	 */
	byte[] getProcessMapImage(String internalId) throws AperteWebServiceError;

	/**
	 * 
	 * Service sets the new value of the attribute in the process.
	 * @param key  attribute name
	 * @param newValue
	 * @param internalId  InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	ProcessInstanceSimpleAttribute setSimpleAttribute(String key,
			String newValue, String internalId) throws AperteWebServiceError;

	/**
	 * 
	 * Method returns the attribute value of the process.
	 * 
	 * @param key attribute name
	 * @param internalId  InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	String getSimpleAttributeValue(String key, String internalId)
			throws AperteWebServiceError;

	/**
	 * Returns action by name from given process Instance.
	 * 
	 * @param internalId Internal id of process instance.
	 * @param actionName name of Action to be returned. 
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<ProcessStateAction> getActionsListByNameFromInstance(
			String internalId, String actionName) throws AperteWebServiceError;

	/**
	 * 
	 * This method returns a list of actions that can be performed in the current  process state.
	 * @param internalId InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<ProcessStateAction> getAvalivableActionForProcess(String internalId)
			throws AperteWebServiceError;

	/**
	 * 
	 * This method returns all the possible attributes for the process.
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<ProcessInstanceSimpleAttribute> getSimpleAttributesList(
			String internalId) throws AperteWebServiceError;

	/**
	 * The system returns a list of process Instances based on internalId list.
	 * 
	 * @param internalIds list of internalIDs - InternalID from pt_process_instance table
	 * @return
	 */
	HashMap<String, ProcessInstance> getProcessInstanceByInternalIdMap(
			Collection<String> internalIds);

	/**
	 * Method returns to the most recent definition of the process: "processdefinition.jpdl.xml" in the form of a byte.
	 * The attributes correspond to the fields in the table pt_process_definition_config
	 * 
	 * @param bpmDefinitionKey definition name, eg Reservation
	 * @param processName Process name eg. Reservation
	 * @return
	 * @throws AperteWebServiceError 
	 */
	byte[] getProcessLatestDefinition(String bpmDefinitionKey,
			String processName) throws AperteWebServiceError;

	/**
	 * 
	 * This method returns a list of all possible actions in the process. Based on definition.
	 * 
	 * @param definitionName  definition Name  eg "Complaint"
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<ProcessStateAction> getAllActionsListFromDefinition(String definitionName)
			throws AperteWebServiceError;

}