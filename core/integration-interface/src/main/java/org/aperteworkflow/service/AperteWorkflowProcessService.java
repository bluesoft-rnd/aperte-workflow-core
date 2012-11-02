package org.aperteworkflow.service;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.service.fault.AperteWebServiceError;

/**
 * @author tlipski@bluesoft.net.pl
 * @author kkolodziej@bluesoft.net.pl
 * 
 */
public interface AperteWorkflowProcessService {

	/**
	 * Method  creates and registers, "a new process instance" on the basis of available config.
	 * 
	 * @param config process definition config, based on which the instance is to be constructed
	 * @param externalKey  (the field is not required)
	 * @param user The creator
	 * @param description A description of the instance (the field is not required)
	 * @param keyword (the field is not required)
	 * @param source Always filled: "portlet"
	 * @param internalId Always filled: "portlet" internalID - internal key (if null, it will be generated from config and externalkey, when externalkey is null, it will generate a unique number) (the field is not required)
	 * @return processInstance
	 */
	ProcessInstance createProcessInstance(ProcessDefinitionConfig config,
			String externalKey, UserData user, String description,
			String keyword, String source, String internalId);

	
	/**
	 * 
	 * Returns the process instance, on the basis of internalID. that is given by aperte and visible to the user in the system such as: "Complaint.740020"
	 * 
	 * @param internalId  InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	ProcessInstance getProcessData(String internalId)
			throws AperteWebServiceError;

	/**
	 * 
	 * The method determines whether a process is in progress. On the basis of the "running" filde in "pt_process_instance table".
	 * 
	 * @param internalId InternalID from pt_process_instance table
	 * @return
	 * @throws AperteWebServiceError 
	 */
	boolean isProcessRunning(String internalId) throws AperteWebServiceError;

	/**
	 *  Service saves the modified instances, or create new one. But only in the pt_process_instance table!
	 * 
	 * @param processInstance Process Instance to save.
	 */
	void saveProcessInstance(ProcessInstance processInstance);

	/**
	 * 
	 * Assigns a task from the queue to the user if he has permission.
	 * 
	 * This method is not working properly because it uses the method of 
	 * "getUserQueuesFromConfig" that does not return the correct results. 
	 * 
	 * @param q queue 
	 * @param user User Data
	 * @return
	 */
	BpmTask assignTaskFromQueue(ProcessQueue q, UserData user);

	/**
	 * 
	 * Assigns specific task from the queue to the user if he has permission.
	 * 
	 * This method is not working properly because it uses the method of 
	 * "getUserQueuesFromConfig" that does not return the correct results. 
	 * @param q queue 
	 * @param task BpmTask to assign.
	 * @param user User Data
	 * @return
	 */
	BpmTask assignSpecificTaskFromQueue(ProcessQueue q, BpmTask task,
			UserData user);

	/**
	 * 
	 * Returns TaskBpm data based on Process Instance.
	 * 
	 * @param taskExecutionId  The name of the task from table  "jbpm4_task" in eg. "Accept" or "Complaint"
	 * @param taskName "execution_id_" from the table "jbpm4_task", the value is the same as internalId the table "pt_process_instance" eg. "Complaint.730231"
	 * @return
	 * @throws AperteWebServiceError 
	 */
	BpmTask getTaskDataForProcessInstance(String taskExecutionId,
			String taskName) throws AperteWebServiceError;

	/**
	 * 
	 * Returns BpmTask data based on id.
	 * 
	 * @param taskId the id of task.
	 * @return
	 * @throws AperteWebServiceError 
	 */
	BpmTask getTaskData(String taskId) throws AperteWebServiceError;

	/**
	 * 
	 * Returns All listed BpmTask data from ProcessInstance. If exists. And given User has privilages.
	 * 
	 * @param pi  Process Instance
	 * @param user User Data
	 * @param taskNames List of task names
	 * @return
	 */
	List<BpmTask> findProcessTasksByNames(ProcessInstance pi, UserData user,
			Set<String> taskNames);

	/**
	 * Method Return all task of User from minDate.
	 *  
	 * @param minDate The oldest task date,
	 * @param user User Data
	 * @return
	 */
	Integer getRecentTasksCount(Calendar minDate, UserData user);

	/**
	 * 
	 * Return All task for given User
	 *  
	 * @param user User Data
	 * @return
	 */
	Collection<BpmTask> getAllTasks(UserData user);

	/**
	 * Service returns the name of the current output of a task in the process is dependent of the proces state.
	 * 
	 * @param executionId InternalID from t_process_instance table
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<String> getOutgoingTransitionNames(String executionId)
			throws AperteWebServiceError;

	/**
	 * 
	 * Returns Destination names of outgoing transitions, based on process Internal id.
	 * 
	 * @param executionId
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<String> getOutgoingTransitionDestinationNames(String executionId)
			throws AperteWebServiceError;

	/**
	 * 
	 * Service changes task assigned user to any other.
	 * 
	 * @param pi Process Instance
	 * @param bpmTask name of Task
	 * @param user new user to be assigned 
	 */
	void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask,
			UserData user);

	/**
	 * 
	 * Method deploys process.
	 * 
	 * TODO
	 * Method don't work! For more information look in implementation.
	 * 
	 * @param cfg Process definition Config to deploy
	 * @param queues Queues definitions 
	 * @param processMapDefinition 
	 * @param processMapImageStream
	 * @param logo
	 */
	void deployProcessDefinitionBytes(ProcessDefinitionConfig cfg,
			ProcessQueueConfig[] queues, byte[] processMapDefinition,
			byte[] processMapImageStream, byte[] logo);

	/**
	 * 
	 * Method deploys process.
	 * 
	 * TODO
	 * Method don't work! For more information look in implementation.
	 * 
	 * 
	 * @param cfgXmlFile Process definition Config to deploy as xml.
	 * @param queueXmlFile Queues definitions as Xml
	 * @param processMapDefinition
	 * @param processMapImageStream
	 * @param logo
	 */
	void deployProcessDefinition(byte[] cfgXmlFile, byte[] queueXmlFile,
			byte[] processMapDefinition, byte[] processMapImageStream,
			byte[] logo);

	/**
	 * @param userLogin
	 * @return
	 * @throws AperteWebServiceError
	 */
	Collection<ProcessQueue> getUserAvailableQueues(String userLogin)
			throws AperteWebServiceError;

	/**
	 * 
	 * Returns information whether the user is currently assigned to the task.
	 * 
	 * @param internalId InternalID from t_process_instance table
	 * @param userLogin User login
	 * @return
	 * @throws AperteWebServiceError
	 */
	boolean isProcessOwnedByUser(String internalId, String userLogin)
			throws AperteWebServiceError;

	/**
	 * 
	 * Assigns/Changes user to the task.
	 * 
	 * @param taskId The id of the task from the table "jbpm4_task" or its equivalent for Activity
	 * @param userLogin User login
	 * @throws AperteWebServiceError
	 */
	void assignTaskToUser(String taskId, String userLogin)
			throws AperteWebServiceError;

	/**
	 * 
	 * The method is used to cancel a process instance, by setting in the table "pt_process_instance" value "false" in the "running" and the value "null" in the column "status". 
	 * In addition,  the history is updated.
	 * 
	 * @param internalId InternalID from t_process_instance table
	 * @throws AperteWebServiceError
	 */
	void adminCancelProcessInstance(String internalId)
			throws AperteWebServiceError;

	/**
	 * 
	 * Service creates and registers, "a new process instance" on the basis of available config, the name can be downloaded by using the method: "getActiveConfigurations ()".
	 * 
	 * @param bpmnkey Name of the process configuration,  eg"Reservation".
	 * @param userLogin Login of the user who is to become the creator.
	 * @return
	 * @throws AperteWebServiceError
	 */
	ProcessInstance startProcessInstance(String bpmnkey, String userLogin)
			throws AperteWebServiceError;

	/**
	 * 
	 * Returns the BPMN tasks for the process, if the user is given, it returns only TASKI assigned that user, otherwise it returns all TASKI for this instance.
	 * 
	 * @param internalId InternalID from t_process_instance table
	 * @param userLogin user login
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<BpmTask> findProcessTasks(String internalId, String userLogin)
			throws AperteWebServiceError;

	/**
	 * @param userLogin
	 * @return
	 * @throws AperteWebServiceError
	 */
	UserData getSubstitutingUser(String userLogin) throws AperteWebServiceError;

	/**
	 * 
	 * The method of "pushing" the process further, the fields:  "actionName" and "bpmTaskName" 
	 * are not required they can be null or empty. In this case, 
	 * if there is more than one action ore task, its taken randomly one of resulted list. 
	 * Action is a transition, so when XOR appears there are 2 possible actions.
	 * 
	 * If userLogin is null adminCompleteTask(ProcessInstance processData,
	 * ProcessStateAction action, BpmTask bpmTask) is called. 
	 * 
	 * @param internalId InternalID from t_process_instance table
	 * @param actionName the name of the action to execute (field is not required)
	 * @param bpmTaskName Taska name. (field is not required)
	 * @param userLogin user login (field is not required)
	 * @throws AperteWebServiceError
	 */
	void performAction(String internalId, String actionName,
			String bpmTaskName, String userLogin) throws AperteWebServiceError;

	/**
	 * 
	 * Returns a list of BPMN tasks for the user, the list may be limited: a max number and the first element position.
	 * 
	 * @param offset The offset in the results list. Default 0(field is not required)
	 * @param limit The number of results to be displayed Default 1000 (field is not required)
	 * @param userLogin user login
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<BpmTask> findUserTasksPaging(Integer offset, Integer limit,
			String userLogin) throws AperteWebServiceError;

	/**
	 * 
	 * Returns jbpm UserTask based on user login id and internal processinstance that interests us.
	 * 
	 * 
	 * @param internalId InternalID from t_process_instance table
	 * @param userLogin User login
	 * @return
	 * @throws AperteWebServiceError
	 */
	List<BpmTask> findUserTasks(String internalId, String userLogin)
			throws AperteWebServiceError;

	/**
	 * Look: performAction performAction(String internalId, String actionName,
	 * String bpmTaskName, String userLogin).
	 * 
	 * Same but its not testing if User has permissions. So its possible to complete every task.
	 * 
	 * @param processData
	 * @param action
	 * @param bpmTask
	 * @throws AperteWebServiceError
	 */
	void adminCompleteTask(ProcessInstance processData,
			ProcessStateAction action, BpmTask bpmTask)
			throws AperteWebServiceError;
}