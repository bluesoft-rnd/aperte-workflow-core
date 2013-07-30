package org.aperteworkflow.service;

import org.aperteworkflow.service.fault.AperteWsIllegalArgumentException;
import org.aperteworkflow.service.fault.AperteWsWrongArgumentException;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueueBean;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**<pre>
 * @author tlipski@bluesoft.net.pl
 * @author kkolodziej@bluesoft.net.pl
 * 
 *</pre>*/
public interface AperteWorkflowProcessService {

	/**<pre>
	 * Method  creates and registers, "a new process instance" on the basis of available config.
	 * 
	 * @param config process definition config, based on which the instance is to be constructed
	 * @param externalKey  (the field is not required)
	 * @param user The creator
	 * @param description A description of the instance (the field is not required)
	 * @param keyword (the field is not required)
	 * @param source Always filled: "portlet"
	 * @param internalId Always filled: "portlet" internalID - internal key (if null, it will be generated from config and externalkey, when externalkey is null, it will generate a unique number) (the field is not required)
	 * @return Newly created process instance.
	 *</pre>*/
	ProcessInstance createProcessInstance(String bpmDefinitionKey, String externalKey, String source, String userLogin);

	/**<pre>
	 *
	 * Service creates and registers, "a new process instance" on the basis of available config, the name can be downloaded by using the method: "getActiveConfigurations ()".
	 *
	 * @param bpmnkey Name of the process configuration,  eg"Reservation".
	 * @param userLogin Login of the user who is to become the creator.
	 * @return New process instance.
	 * @throws AperteWsWrongArgumentException If "userLogin" is wrong and  User does not exists (including param null or empty values).
	 * @throws AperteWsIllegalArgumentException If "bpmnkey" is null or empty.
	 *</pre>*/
	ProcessInstance startProcessInstance(String bpmDefinitionKey, String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Assigns a task from the queue to the user if he has permission.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * This method is not working properly because it uses the method of 
	 * "getUserQueuesFromConfig" that does not return the correct results. 
	 * 
	 * @param q Queue 
	 * @param user User Data
	 * @return BpmTaskBean with new assigned user
	 *</pre>*/
	BpmTaskBean assignTaskFromQueue(String queueName, String userLogin);

	/**<pre>
	 * 
	 * Assigns specific task from the queue to the user if he has permission.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * This method is not working properly because it uses the method of 
	 * "getUserQueuesFromConfig" that does not return the correct results. 
	 * 
	 * @param q queue 
	 * @param task BpmTaskBean to assign.
	 * @param user User Data
	 * @return Bpm Task with new assigned User.
	 *</pre>*/
	BpmTaskBean assignSpecificTaskFromQueue(String queueName, String taskId, String userLogin);

	/**<pre>
	 *
	 * The method of "pushing" the process further, the fields:  "actionName" and "BpmTaskBeanName"
	 * are not required they can be null or empty. In this case,
	 * if there is more than one action ore task, its taken randomly one of resulted list.
	 * Action is a transition, so when XOR appears there are 2 possible actions.
	 *
	 * If userLogin is null adminCompleteTask(ProcessInstance processData,
	 * ProcessStateAction action, BpmTaskBean BpmTaskBean) is called.
	 *
	 * @param internalId InternalID from t_process_instance table
	 * @param actionName the name of the action to execute (field is not required)
	 * @param BpmTaskBeanName Taska name. (field is not required)
	 * @param userLogin user login (field is not required)
	 * @throws AperteWsWrongArgumentException If userLogin,internalId is wrong and  User or process instance does not exists (including param null or empty values).
	 *</pre>*/
	void performAction(String actionName, String taskId, String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Returns BpmTaskBean data based on id.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param taskId the id of task.
	 * @return Bpm Task based on Id
	 * @throws AperteWsWrongArgumentException If taskId is wrong and  BpmTaskBean, does not exists.
	 * @throws AperteWsIllegalArgumentException If taskId is null or empty.
	 *</pre>*/
	BpmTaskBean getTaskData(String taskId) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Returns All listed BpmTaskBean data from ProcessInstance if exists, and given User has privileges.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param pi  Process Instance
	 * @param user User Data
	 * @param taskNames List of task names
	 * @return List of Bpm Tasks
	 *</pre>*/
	List<BpmTaskBean> findProcessTasksByNames(String internalId, String userLogin, Set<String> taskNames);

	/**<pre>
	 * Method Return all task of User from minDate.
	 *  
	 * <b>Warning! Method is exclude from WSDL!</b>
	 *  
	 * @param minDate The oldest task date,
	 * @param user User Data
	 * @return Number do recent Tasks
	 *</pre>*/
	int getRecentTasksCount(Date minDate, String userLogin);

	/**<pre>
	 * 
	 * Return All task for given User
	 * 
	 *  <b>Warning! Method is exclude from WSDL!</b>
	 *  
	 * @param user User Data
	 * @return List of BpmTaskBean
	 *</pre>*/
	List<BpmTaskBean> getAllTasks(String userLogin);

	/**<pre>
	 * 
	 * Returns list of Available queues.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param userLogin
	 * @return List od Queues
	 * @throws AperteWsWrongArgumentException  If userLogin is wrong and  User, does not exists (including param null or empty values).
	 *</pre>*/
	Collection<ProcessQueueBean> getUserAvailableQueues(String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Returns the BPMN tasks for the process, if the user is given, it returns only TASKI assigned that user, otherwise it returns all TASKI for this instance.
	 * 
	 * @param internalId InternalID from t_process_instance table
	 * @param userLogin user login
	 * @return List of BpmTaskBean
	 * @throws AperteWsWrongArgumentException If userLogin,internalId is wrong and  User or process instance does not exists (including param null or empty values).
	 *</pre>*/
	List<BpmTaskBean> findProcessTasks(String internalId, String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Returns a list of BPMN tasks for the user, the list may be limited: a max number and the first element position.
	 * 
	 * @param offset The offset in the results list. Default 0(field is not required)
	 * @param limit The number of results to be displayed Default 1000 (field is not required)
	 * @param userLogin user login
	 * @return List of Bpm Tasks
	 * @throws AperteWsWrongArgumentException If userLogin is wrong and  User does not exists (including param null or empty values).
	 *</pre>*/
	List<BpmTaskBean> findUserTasksPaging(Integer offset, Integer limit, String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * Returns Bpm User Task based on user login, id and internalId of process Instance, that interests us.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @param internalId InternalID from t_process_instance table.
	 * @param userLogin User login.
	 * @return List of Bpm Tasks
	 * @throws AperteWsWrongArgumentException
	 *</pre>*/
	List<BpmTaskBean> findUserTasks(String internalId, String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 *
	 * Assigns/Changes user to the task.
	 *
	 * @param taskId The id of the task from the table "jbpm4_task" or its equivalent for Activity
	 * @param userLogin User login
	 * @throws AperteWsWrongArgumentException If userLogin,internalId is wrong and  User or process instance does not exists (including param null or empty values).
	 *</pre>*/
	void assignTaskToUser(String taskId, String userLogin) throws AperteWsWrongArgumentException;

	/**<pre>
	 *
	 * The method is used to cancel a process instance, by setting in the table "pt_process_instance" value "false" in the "running" and the value "null" in the column "status".
	 * In addition,  the history is updated.
	 *
	 * @param internalId InternalID from t_process_instance table
	 * @throws AperteWsWrongArgumentException If internalId is wrong and process instance does not exists (including param null or empty values).
	 *</pre>*/
	void adminCancelProcessInstance(String internalId) throws AperteWsWrongArgumentException;

	/**<pre>
	 * 
	 * 
	 * Same as perform action but its not testing if User has permissions. So its possible to complete every task.
	 * 
	 * <b>Warning! Method is exclude from WSDL!</b>
	 * 
	 * @see #performAction(String, String, String, String)
	 * @param processData Process Instance to work on.
	 * @param action One of many possible actions to perform.
	 * @param BpmTaskBean BpmTaskBean to be "pushed".
	 *</pre>*/
	void adminCompleteTask(String taskId, String actionName);

	/**<pre>
	 *
	 * Service changes task assigned user to any other.
	 *
	 * <b>Warning! Method is exclude from WSDL!</b>
	 *
	 * @param pi Process Instance
	 * @param BpmTaskBean name of Task
	 * @param user new user to be assigned
	 *</pre>*/
	void adminReassignProcessTask(String taskId, String userLogin);

	/**<pre>
	 *
	 * Method deploys process.
	 *
	 * <b>Warning! Method is exclude from WSDL!</b>
	 *
	 *
	 * Method don't work! For more information look in implementation.
	 *
	 * @param cfgXmlFile Process definition Config to deploy as xml.
	 * @param queueXmlFile Queues definitions as Xml
	 * @param processMapDefinition
	 * @param processMapImageStream
	 * @param logo
	 *</pre>*/
	void deployProcessDefinition(byte[] cfgXmlFile, byte[] queueXmlFile,
								 byte[] processMapDefinition, byte[] processMapImageStream,
								 byte[] logo);
}