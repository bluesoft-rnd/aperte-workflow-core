package pl.net.bluesoft.rnd.processtool.bpm;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aperteworkflow.bpm.graph.GraphElement;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.eventbus.EventBusManager;

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

//
public interface ProcessToolBpmSession extends ProcessToolBpmConstants {
    ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx);

    ProcessInstance createProcessInstance(ProcessDefinitionConfig config,
                                          String externalKey,
                                          ProcessToolContext ctx,
                                          String description,
                                          String keyword,
                                          String source, String internalId);

    ProcessInstance getProcessData(String internalId, ProcessToolContext ctx);

    ProcessInstance refreshProcessData(ProcessInstance processInstance, ProcessToolContext ctx);

    boolean isProcessRunning(String internalId, ProcessToolContext ctx);

    void saveProcessInstance(ProcessInstance processInstance, ProcessToolContext ctx);

    Collection<ProcessDefinitionConfig> getAvailableConfigurations(ProcessToolContext ctx);

    Collection<ProcessQueue> getUserAvailableQueues(ProcessToolContext ctx);

    Set<String> getPermissionsForWidget(ProcessStateWidget widget, ProcessToolContext ctx);

    Set<String> getPermissionsForAction(ProcessStateAction action, ProcessToolContext ctx);

    boolean hasPermissionsForDefinitionConfig(ProcessDefinitionConfig config);

    boolean isProcessOwnedByUser(ProcessInstance processInstance, ProcessToolContext ctx);

    BpmTask assignTaskFromQueue(ProcessQueue q, ProcessToolContext ctx);

    BpmTask assignTaskFromQueue(ProcessQueue q, BpmTask task, ProcessToolContext ctx);

    void assignTaskToUser(ProcessToolContext ctx, String taskId, String userLogin);

    BpmTask getTaskData(String taskExecutionId, String taskName, ProcessToolContext ctx);

    BpmTask getTaskData(String taskId, ProcessToolContext ctx);

    BpmTask getPastOrActualTask(ProcessInstanceLog log, ProcessToolContext ctx);

    BpmTask getPastEndTask(ProcessInstanceLog log, ProcessToolContext ctx);

    BpmTask refreshTaskData(BpmTask task, ProcessToolContext ctx);
    
    /** Method returns queue size for given queue type and user login. Methods is significally faster
     * than {@link getFilteredTasksCount} but does not provide filtering support. 
     */
    int getTasksCount(ProcessToolContext ctx, String userLogin, QueueType ... queueTypes);
    
    int getTasksCount(ProcessToolContext ctx, String userLogin, Collection<QueueType> queueTypes);
    
    /** Method returns queue size for conditions provided by given filter. Methods is slower then
     * than {@link getTasksCount} but has full filtering options. It does not load entities to
     * memory
     */
	int getFilteredTasksCount(ProcessInstanceFilter filter, ProcessToolContext ctx);

    List<BpmTask> findUserTasks(ProcessInstance processInstance, ProcessToolContext ctx);

    List<BpmTask> findUserTasks(Integer offset, Integer limit, ProcessToolContext ctx);

    List<BpmTask> findProcessTasks(ProcessInstance pi, ProcessToolContext ctx);

    List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin, ProcessToolContext ctx);

    List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin, Set<String> taskNames, ProcessToolContext ctx);
    
    /** Find tasks from user process queue with given queue type and login in filter instance */
    List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, ProcessToolContext ctx);
    
    /** Find tasks from user process queue with given queue type and login in filter instance with given max results limit */
    List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, ProcessToolContext ctx, int resultOffset, int maxResults);

    List<BpmTask> findRecentTasks(Calendar minDate, Integer offset, Integer limit, ProcessToolContext ctx);

    Integer getRecentTasksCount(Calendar minDate, ProcessToolContext ctx);

    Collection<BpmTask> getAllTasks(ProcessToolContext ctx);

    BpmTask performAction(ProcessStateAction action, BpmTask bpmTask, ProcessToolContext ctx);

    List<String> getOutgoingTransitionNames(String executionId, ProcessToolContext ctx);

    String getUserLogin();

    UserData getUser(ProcessToolContext ctx);

    UserData loadOrCreateUser(ProcessToolContext ctx, UserData userData);

    UserData getSubstitutingUser(ProcessToolContext ctx);

    EventBusManager getEventBusManager();


//    Collection<BpmTask> getTaskList(ProcessInstance pi, final ProcessToolContext ctx, final boolean mustHaveAssignee);

    List<String> getOutgoingTransitionDestinationNames(String executionId, ProcessToolContext ctx);

    Collection<String> getRoleNames();

    void adminCancelProcessInstance(ProcessInstance pi);

    void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin);

    void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action);

    List<String> getAvailableLogins(final String filter);

    List<GraphElement> getProcessHistory(ProcessInstance pi);

    byte[] getProcessLatestDefinition(String bpmDefinitionKey, String processName);

    byte[] getProcessDefinition(ProcessInstance pi);

    byte[] getProcessMapImage(ProcessInstance pi);

    String deployProcessDefinition(String processName, InputStream definitionStream, InputStream processMapImageStream);

	Collection<BpmTask> getProcessTaskInQueues(ProcessToolContext ctx, final ProcessInstance processInstance);

	/** Get all tasks in queue with given queue name 
	 * @param ctx */
	List<BpmTask> getQueueTasks(ProcessToolContext ctx, String queueName);
}
