package pl.net.bluesoft.rnd.processtool.bpm;

import org.aperteworkflow.bpm.graph.GraphElement;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The process tool interface, providing basic operations.
 * <p/>
 * Any BPM engine supported by process tool should be
 * provided by implementing this interface.
 *
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public interface ProcessToolBpmSession extends ProcessToolBpmConstants {
    ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames);

	ProcessInstance startProcess(String bpmDefinitionId, String externalKey, String description, String keyword,
								 String source);
	BpmTask performAction(ProcessStateAction action, BpmTask bpmTask);
	BpmTask assignTaskFromQueue(String queueName);
	BpmTask assignTaskFromQueue(String queueName, BpmTask task);
	void assignTaskToUser(String taskId, String userLogin);

    ProcessInstance getProcessData(String internalId);
    ProcessInstance refreshProcessData(ProcessInstance processInstance);
	void saveProcessInstance(ProcessInstance processInstance);

	BpmTask getTaskData(String taskId);
	BpmTask getPastOrActualTask(ProcessInstanceLog log);
	BpmTask getPastEndTask(ProcessInstanceLog log);
	BpmTask refreshTaskData(BpmTask task);

	boolean isProcessRunning(String internalId);

	/** Method returns queue size for given queue type and user login. Methods is significally faster
	 * than {@link getFilteredTasksCount} but does not provide filtering support.
	 */
	int getTasksCount(String userLogin, QueueType ... queueTypes);

	int getTasksCount(String userLogin, Collection<QueueType> queueTypes);

	/** Method returns queue size for conditions provided by given filter. Methods is slower then
	 * than {@link getTasksCount} but has full filtering options. It does not load entities to
	 * memory
	 */
	int getFilteredTasksCount(ProcessInstanceFilter filter);

	BpmTask getHistoryTask(String taskId);
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

	int getRecentTasksCount(Date minDate);

	Collection<ProcessDefinitionConfig> getAvailableConfigurations();
    List<ProcessQueue> getUserAvailableQueues();
    Set<String> getPermissionsForWidget(ProcessStateWidget widget);
    Set<String> getPermissionsForAction(ProcessStateAction action);
    boolean hasPermissionsForDefinitionConfig(ProcessDefinitionConfig config);

    String getUserLogin();
    UserData getUser();
	UserData getSubstitutingUser();
    UserData loadOrCreateUser(UserData userData);
	Collection<String> getRoleNames();

    EventBusManager getEventBusManager();

    void adminCancelProcessInstance(ProcessInstance pi);
    void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin);
    void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action);

    List<String> getAvailableLogins(String filter);

    List<GraphElement> getProcessHistory(ProcessInstance pi);

    byte[] getProcessLatestDefinition(String bpmDefinitionKey);
    byte[] getProcessDefinition(ProcessInstance pi);
    byte[] getProcessMapImage(ProcessInstance pi);

	boolean differsFromTheLatest(String bpmDefinitionKey, byte[] newDefinition);

    String deployProcessDefinition(String processId, InputStream definitionStream, InputStream processMapImageStream);
}
