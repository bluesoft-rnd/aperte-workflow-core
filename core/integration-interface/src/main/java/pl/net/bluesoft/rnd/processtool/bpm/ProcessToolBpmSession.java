package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.ProcessDiagram;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.InputStream;
import java.util.*;

/**
 * The process tool api, providing basic operations.
 * <p/>
 * Any BPM engine supported by process tool should be
 * provided by implementing this api.
 *
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public interface ProcessToolBpmSession {
	ProcessToolBpmSession createSession(String userLogin);
    ProcessToolBpmSession createSession(String userLogin, Collection<String> roleNames);

	StartProcessResult startProcess(String bpmDefinitionId, String externalKey, String source);
	StartProcessResult startProcess(String bpmDefinitionId, String externalKey, String source, Map<String, Object> simpleAttributes);
	List<BpmTask> performAction(String actionName, String taskId);
	List<BpmTask> performAction(String actionName, BpmTask bpmTask);
	List<BpmTask> performAction(String actionName, BpmTask bpmTask, boolean reloadTask);
	List<BpmTask> performAction(ProcessStateAction action, BpmTask bpmTask);
	BpmTask assignTaskFromQueue(String queueName);
	BpmTask assignTaskFromQueue(String queueName, String taskId);
    BpmTask assignTaskToUser(String taskId, String userLogin);

	BpmTask getTaskData(String taskId);
	BpmTask getPastOrActualTask(ProcessInstanceLog log);
	BpmTask getPastEndTask(ProcessInstanceLog log);
	BpmTask refreshTaskData(BpmTask task);

	/** Method returns queue size for given queue type and user login. Methods is significally faster
	 * than {@link getFilteredTasksCount} but does not provide filtering support.
	 */
	int getTasksCount(ProcessInstanceFilter queueFilter);


	/** Method returns queue size for conditions provided by given filter. Methods is slower then
	 * than {@link getTasksCount} but has full filtering options. It does not load entities to
	 * memory
	 */
	int getFilteredTasksCount(ProcessInstanceFilter filter);

	BpmTask getHistoryTask(String taskId);
    BpmTask getLastHistoryTaskByName(Long internalProcessId, String stepName);

	List<BpmTask> getAllTasks();

	List<BpmTask> findUserTasks(ProcessInstance processInstance);
	List<BpmTask> findUserTasks(Integer offset, Integer limit);

	List<BpmTask> findProcessTasks(ProcessInstance pi);
	List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin);
	List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin, Set<String> taskNames);

	/** Find tasks from user process queue with given queue type and login in filter instance */
	List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter);

	/** Find tasks from user process queue with given queue type and login in filter instance with given max results limit */
	List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, int resultOffset, int maxResults);

	List<BpmTask> findRecentTasks(Date minDate, Integer offset, Integer limit);

	List<BpmTaskNotification> getNotifications(Date date, Locale locale);

	int getRecentTasksCount(Date minDate);

	List<ProcessDefinitionConfig> getAvailableConfigurations();
    List<ProcessQueue> getUserAvailableQueues();
    Set<String> getPermissionsForWidget(ProcessStateWidget widget);
    Set<String> getPermissionsForAction(ProcessStateAction action);
    boolean hasPermissionsForDefinitionConfig(ProcessDefinitionConfig config);

    String getUserLogin();
	String getSubstitutingUserLogin();
	Collection<String> getRoleNames();

    void adminCancelProcessInstance(String internalId);
    void adminForwardProcessTask(String taskId, String userLogin, String targetUserLogin);
    void adminReassignProcessTask(String taskId, String userLogin);
    void adminCompleteTask(String taskId, String actionName);

    List<String> getAvailableLogins(String filter);

    byte[] getProcessLatestDefinition(String bpmDefinitionKey);
    byte[] getProcessDefinition(ProcessInstance pi);
    byte[] getProcessMapImage(ProcessInstance pi);

	boolean differsFromTheLatest(String bpmDefinitionKey, byte[] newDefinition);

    String deployProcessDefinition(String processId, InputStream definitionStream, InputStream processMapImageStream);

	ProcessDiagram getProcessDiagram(BpmTask task, I18NSource i18NSource);
}
