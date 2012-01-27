package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * The process tool interface, providing basic operations.
 *
 * Any BPM engine supported by process tool should be
 * provided by implementing this interface.
 *
 * @author tlipski@bluesoft.net.pl
 */

public interface ProcessToolBpmSession {

	String getProcessState(ProcessInstance pi, ProcessToolContext ctx);
	Collection<ProcessInstance> getUserProcesses(int offset, int limit, ProcessToolContext ctx);
	ProcessInstance createProcessInstance(ProcessDefinitionConfig config, String externalKey, ProcessToolContext ctx,
	                                      String description, String keyword, String source);
	Collection<ProcessDefinitionConfig> getAvailableConfigurations(ProcessToolContext ctx);
	Collection<ProcessQueue> getUserAvailableQueues(ProcessToolContext ctx);

	ProcessInstance assignTaskFromQueue(ProcessQueue q, ProcessToolContext processToolContextFromThread);
	ProcessStateConfiguration getProcessStateConfiguration(ProcessInstance pi, ProcessToolContext ctx);
	Set<String> getPermissionsForWidget(ProcessStateWidget widget, ProcessToolContext ctx);
	Set<String> getPermissionsForAction(ProcessStateAction action, ProcessToolContext ctx);
	EventBusManager getEventBusManager();
	Collection<BpmTask> getTaskList(ProcessInstance pi, ProcessToolContext ctx);
	boolean isProcessOwnedByUser(ProcessInstance processInstance, ProcessToolContext ctx);
	ProcessInstance performAction(ProcessStateAction action, ProcessInstance processInstance, ProcessToolContext ctx);
	ProcessInstance performAction(ProcessStateAction action,
	                              ProcessInstance processInstance,
	                              ProcessToolContext ctx,
	                              BpmTask task);


	String getUserLogin();
	UserData getUser(ProcessToolContext ctx);
    UserData getSubstitutingUser(ProcessToolContext ctx);

	ProcessInstance getProcessData(String internalId, ProcessToolContext ctx);

	boolean isProcessRunning(String internalId, ProcessToolContext ctx);
	void saveProcessInstance(ProcessInstance processInstance, ProcessToolContext ctx);

    Collection<ProcessInstance> getQueueContents(ProcessQueue q, int offset, int limit, ProcessToolContext ctx);

    ProcessInstance assignTaskFromQueue(ProcessQueue q, ProcessInstance processInstance,
                                        ProcessToolContext processToolContextFromThread);

    List<String> getOutgoingTransitionNames(String internalId, ProcessToolContext ctx);
        
    ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx);

    Collection<String> getRoleNames();

    void adminCancelProcessInstance(ProcessInstance pi);

    void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin);

    void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action);

}
