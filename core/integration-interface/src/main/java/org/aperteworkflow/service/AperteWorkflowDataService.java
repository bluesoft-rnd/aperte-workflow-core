package org.aperteworkflow.service;

import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import org.aperteworkflow.service.fault.AperteWsIllegalArgumentException;
import org.aperteworkflow.service.fault.AperteWsWrongArgumentException;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;

/**
 * @author tlipski@bluesoft.net.pl
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public interface AperteWorkflowDataService {

	/**<pre>
	 * Service saves the modified instances, or create new one. But only in the pt_process_instance table!
	 *  
	 * <b>Warning! Method is exclude from WSDL!</b>
	 *  
	 * @param processInstance Process Instance to save.
	 * @return Created process Id.
	 *</pre>
	 */
	long saveProcessInstance(ProcessInstance processInstance);

	/**<pre>
	 * Return the process Instance based on ID.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param id Id of process to download. 
	 * @return New Process Instance.
	 *</pre>*/
	ProcessInstance getProcessInstance(long id);

	/**<pre>
	 * Returns multiple instances based on list of ID.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param ids List of process instance ids.
	 * @return List of Process Instance based on Id.
	 *</pre>*/
	List<ProcessInstance> getProcessInstances(Collection<Long> ids);

	/**<pre>
	 * Returns the process instance, on the basis of internalID. that is given by Aperte and visible to the user in the system such as: "Complaint.740020"
	 * 
	 * @param internalId  InternalID from pt_process_instance table
	 * @throws AperteWsWrongArgumentException If process instance does not exists (including param null or empty values).  
	 * @return  Process instance from pt_process_instance.
	 *</pre>*/
	ProcessInstance getProcessInstanceByInternalId(String internalId)
			throws AperteWsWrongArgumentException;

	/**<pre>
	 * Returns the process instance, on the basis of externalId.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param externalId externalId from pt_process_instance table
	 * @return New Process Instance
	 *</pre>*/
	ProcessInstance getProcessInstanceByExternalId(String externalId);

	/**<pre>
	 * Returns the process instance, on the basis of externalId.
	 * 
	 *  <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param key key from pt_process_instance table
	 * @param processType do nothing, filing this attribute, has no consequence.
	 * @return List od Process instances.
	 *</pre>*/
	List<ProcessInstance> findProcessInstancesByKeyword(String key,
			String processType);

	/**<pre>
	 * Process deletion.
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @throws AperteWsWrongArgumentException If process instance does not exists (including param null or empty values).
	 *</pre>*/
	void deleteProcessInstance(String internalId) throws AperteWsWrongArgumentException;

	/**<pre>
	 * Returns the full history of the process for the user, the information is retrieved from the pt_process_instance_log. 
	 * "dateFrom" and "dateTo" are not requierd.  The fields only adds upper  and lower restrictions. By default set is right-open.
	 * 
	 * @param userLogin User login. 
	 * @param startDate Date from, i.e. YYYY-MM-DD (the field is not required)
	 * @param endDate Date to, i.e. YYYY-MM-DD (the field is not required)
	 * @throws AperteWsWrongArgumentException If user does not exists (including param null or empty values).  
	 * @return List of Process instance.
	 *</pre>*/
	Collection<ProcessInstanceLog> getUserHistory(String userLogin,
			Date startDate, Date endDate) throws AperteWsWrongArgumentException;

	/**<pre>
	 * The method creates user, or  returns existing one if logins  match each other.
	 * 
	 * @param ud User Informations*
	 * @return Created user.
	 * @return User in form of Userdata object.
	 *</pre>*/
	UserData findOrCreateUser(UserData ud);

	/**<pre>
	 * Method finds the process instance using Lucena. 
	 * 
	 * @param filter The keyword which is used by search engine
	 * @param offset The offset in the results list. (the field is not required)
	 * @param limit The number of results to be displayed
	 * @param onlyRunning (the field is not required)
	 * @param userRoles roles  (the field is not required)
	 * @param assignee Person assigned (the field is not required)
	 * @param queues Queues (the field is not required)
	 * @return List of process Instance wich mache to filter.
	 * @throws AperteWsIllegalArgumentException If filter is null or empty. 
	 *</pre>*/
	Collection<ProcessInstance> searchProcesses(String filter, int offset,
			int limit, boolean onlyRunning, String[] userRoles,
			String assignee, String... queues) throws AperteWsIllegalArgumentException;

	/**<pre>
	 * Returns list of Process Instance which assigned user is userData, and entry date is no older than minDate.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 *  
	 * @param userData User data for which process instance will be returned.
	 * @param minDate The oldest allowed date.
	 * @return List of processInstances,
	 *</pre>*/
	Collection<ProcessInstance> getUserProcessesAfterDate(UserData userData, Date minDate);

	/**<pre>
	 * Returns process witch, assigned user is userData, and entry date is no older than minDate. Results are limited with "limit", and "offset".
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param userData User data for which process instance will be returned.
	 * @param minDate The oldest allowed date.
	 * @param offset Number of first element.
	 * @param limit Maximum number of results.
	 * @return List of processInstances.
	 *</pre>*/
	ResultsPageWrapper<ProcessInstance> getRecentProcesses(UserData userData,
														   Date minDate, Integer offset, Integer limit);

	/**<pre>
	 * Service returns a list of  all, process definitions. From the table: "pt_process_definition_config".
	 * 
	 *  <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @return List od all Process Definition Configurations.
	 * 
	 *</pre>*/
	Collection<ProcessDefinitionConfig> getAllConfigurations();

	/**<pre>
	 * Service returns a list of  active, process definitions. 
	 *  Active in this case means, that  fields "enabled" has value: "true" or "null" , and the "latest" is set to true.  In the table: "pt_process_definition_config".
	 *  @return List of all active configurations.
	 *</pre>*/
	Collection<ProcessDefinitionConfig> getActiveConfigurations();

	/**<pre>
	 * Return  active configuration (look getActiveConfigurations method), based on bpmDefinitionKey eg. Reservation.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param key "bpmDefinitionKey" from the table: "pt_process_definition_config".
	 * @throws AperteWsWrongArgumentException  If Process Definition Config does not exists (including param null or empty values).  
	 * @return Return single active configuration.
	 *</pre>*/
	ProcessDefinitionConfig getActiveConfigurationByKey(String key) throws AperteWsWrongArgumentException;

	/**<pre>
	 * Returns list of ProcessQueueConfig, its based on table pt_process_queue_config
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @return Returns list of all ProcessQueueConfig
	 *</pre>*/
	Collection<ProcessQueueConfig> getQueueConfigs();

	/**<pre>
	 * Returns process state configuration, its based on pt_process_state_config.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param task BpmTask only "taskName", and "processInstance/definition/id" is mandatory
	 * @return process State configuration.
	 *</pre>*/
	ProcessStateConfiguration getProcessStateConfiguration(BpmTaskBean task);

	/**<pre>
	 * Creates a new process or alter the configuration of the old one. 
	 * Decision of modify ore not is made in the method: "pl.net.bluesoft.rnd.processtool.dao.impl.ProcessDefinitionDAOImpl.compareDefinitions"
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param cfg Process Definition Configuration to create or modify. 
	 *</pre>*/
	void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg);

	/**<pre>
	 * Sets the value of the "enable" in pt_process_definition_config for the configuration process.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param cfg ProcessDefinitionConfig only id is mandatory
	 * @param enabled Value of the "enable" in pt_process_definition_config table.
	 *</pre>*/
	void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled);

	/**<pre>
	 * Method returns all Process Definition Configuration based on bpmDefinitionKey in pt_process_definition_config
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param cfg ProcessDefinitionConfig only bpmDefinitionKey is taken for consideration.
	 * @return Version of configuration.
	 *</pre>*/
	Collection<ProcessDefinitionConfig> getConfigurationVersions(
			ProcessDefinitionConfig cfg);

	/**<pre>
	 * Method creates or updates  ProcessQueueConfig 
	 * 
	 *  <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * 
	 * Method don't work! For more information look in implementation.! 
	 * 
	 * @param cfgs List of queue configurations to modify, only "name" field is taken under consideration. 
	 * 
	 * 
	 *</pre>*/
	void updateOrCreateQueueConfigs(Collection<ProcessQueueConfig> cfgs);

	/**<pre>
	 * Delete queue configuration from Data base.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param cfgs List of queue configurations to modify, only "name" field is taken under consideration. 
	 *</pre>*/
	void removeQueueConfigs(Collection<ProcessQueueConfig> cfgs);

	/**<pre>
	 * The service scans the table Jbpm "jbpm4_id_user" the id field, which is actually the login. Sql is: "select user from jbpm4_id_user where id like filter"
	 * The "Activity" is a searched, on the "mail" field in the same manner
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param filter user login or email which data base will be searched.
	 * @return List of all available Logins.
	 *</pre>*/
	List<String> getAvailableLogins(final String filter);

	/**<pre>
	 * 
	 * Method searches user, based one userLogin.
	 * 
	 * @param userLogin user login
	 * @return User in form of UserData.
	 * @throws AperteWsWrongArgumentException If user does not exists (including param null or empty values). 
	 *</pre>*/
	UserData findUser(String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Search process for the user instance in which he took part in a given period of time, "minDate" and "maxDate" are not required.  
	 * The fields only adds upper  and lower restrictions. By default set is right-open.
	 * 
	 * @param userLogin user login
	 * @param minDate date ,i.e. YYYY-MM-DD (the field is not required)
	 * @param maxDate date ,i.e. YYYY-MM-DD (the field is not required)
	 * @return List of Process Instances
	 * @throws AperteWsWrongArgumentException If user login is wrong and user does not exists (including param null or empty values). 
	 *</pre>*/
	Collection<ProcessInstance> getUserProcessesBetweenDatesByUserLogin(
			String userLogin, Date minDate, Date maxDate)
			throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Method returns ". Jpdl.xml" in the form of a byte for the "ProcessInstance", so reviewing historical entries is possible.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @return Process definition as JPDL in byte form. 
	 * @throws AperteWsWrongArgumentException If Process instance from which definition is taken, does not exists (including param null or empty values). 
	 *</pre>*/
	byte[] getProcessDefinition(String internalId) throws AperteWsWrongArgumentException;

	/**<pre>
	 * The method returns a representation of the process (*. Png) in the form of a byte.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @return Process map as PNG in byte form. 
	 * @throws AperteWsWrongArgumentException If Process instance from which definition is taken, does not exists (including param null or empty values).
	 *</pre>*/
	byte[] getProcessMapImage(String internalId) throws AperteWsWrongArgumentException;

	/**<pre>
	 * Service sets the new value of the attribute in the process.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param key Attribute name
	 * @param newValue New value of attribute.
	 * @param internalId  InternalID from pt_process_instance table
	 * @return Value of simple attribute.
	 * @throws AperteWsWrongArgumentException If internalId is wrong and process Instance, does not exists (including param null or empty values).
	 *</pre>*/
	ProcessInstanceSimpleAttribute setSimpleAttribute(String key,
			String newValue, String internalId) throws AperteWsWrongArgumentException;

	/**<pre> 
	 * Method returns the attribute value of the process.
	 * 
	 * @param key attribute name
	 * @param internalId  InternalID from pt_process_instance table
	 * @return Value of simple attribute
	 * @throws AperteWsWrongArgumentException If internalId is wrong and process Instance, does not exists (including param null or empty values).
	 *</pre>*/
	String getSimpleAttributeValue(String key, String internalId)
			throws AperteWsWrongArgumentException;


	/**<pre>
	 * This method returns all the possible attributes for the process.
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @return List of all simple attributes in process instance
	 * @throws AperteWsWrongArgumentException If process Instance, does not exists (including param null or empty values).
	 *</pre>*/
	List<ProcessInstanceSimpleAttribute> getSimpleAttributesList(
			String internalId) throws AperteWsWrongArgumentException;

	/**<pre>
	 * The system returns a list of process Instances based on internalId list.
	 *
	 * @param internalIds list of internalIDs - InternalID from pt_process_instance table
	 * @return HashMap of IntarnalId and ProcessInstance
	 *</pre>*/
	HashMap<String, ProcessInstance> getProcessInstanceByInternalIdMap(
			Collection<String> internalIds);

	/**<pre>
	 * Method returns to the most recent definition of the process: "processdefinition.jpdl.xml" in the form of a byte.
	 * The attributes correspond to the fields in the table pt_process_definition_config
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param bpmDefinitionKey definition name, eg Reservation
	 * @param processName Process name eg. Reservation
	 * @return Returns Jpdl definition in form of Byte
	 * @throws AperteWsIllegalArgumentException If bpmDefinitionKey or processName is null or empty.
	 *</pre>*/
	
	byte[] getProcessLatestDefinition(String bpmDefinitionKey,
			String processName) throws AperteWsIllegalArgumentException;

	/**<pre>
	 * This method returns a list of all possible actions in the process. Based on definition.
	 * 
	 * @param definitionName  definition Name  eg "Complaint"
	 * @return List of all action in the Definition.
	 * @throws AperteWsWrongArgumentException If definitionName is wrong and Definition, does not exists (including param null or empty values).
	 *</pre>*/
	List<ProcessStateAction> getAllActionsListFromDefinition(String definitionName)
			throws AperteWsWrongArgumentException;

}

