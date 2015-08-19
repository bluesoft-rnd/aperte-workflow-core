package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.ProcessDiagram;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2014-10-03
 */
public class TimeTracingBpmSession implements ProcessToolBpmSession {
	private static final Logger LOGGER = Logger.getLogger(TimeTracingBpmSession.class.getName());

	private final ProcessToolBpmSession session;

	public TimeTracingBpmSession(ProcessToolBpmSession session) {
		this.session = session;
	}

	@Override
	public ProcessToolBpmSession createSession(String userLogin) {
		return session.createSession(userLogin);
	}

	@Override
	public ProcessToolBpmSession createSession(String userLogin, Collection<String> roleNames) {
		return session.createSession(userLogin, roleNames);
	}

	@Override
	public StartProcessResult startProcess(String bpmDefinitionId, String externalKey, String source) {
		long start = System.currentTimeMillis();
		try {
			return session.startProcess(bpmDefinitionId, externalKey, source);
		}
		catch(Throwable ex)
		{
			LOGGER.log(Level.SEVERE, "Error with process start: "+bpmDefinitionId);
			throw new RuntimeException(ex);
		}
		finally {
			LOGGER.info("[startProcess] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public StartProcessResult startProcess(String bpmDefinitionId, String externalKey, String source, Map<String, Object> simpleAttributes) {
		long start = System.currentTimeMillis();
		try {
			return session.startProcess(bpmDefinitionId, externalKey, source, simpleAttributes);
		}
		finally {
			LOGGER.info("[startProcess] t = " + (System.currentTimeMillis() - start));
		}
	}

    @Override
    public StartProcessResult startProcess(String bpmDefinitionId, String externalKey, String source, Map<String, Object> simpleAttributes, Map<String, String> largeAttributes, Map<String, Object> complexAttributes) {
        long start = System.currentTimeMillis();
        try {
            return session.startProcess(bpmDefinitionId, externalKey, source, simpleAttributes, largeAttributes, complexAttributes);
        }
        finally {
            LOGGER.info("[startProcess] t = " + (System.currentTimeMillis() - start));
        }
    }

	@Override
	public List<BpmTask> performAction(String actionName, String taskId) {
		long start = System.currentTimeMillis();
		try {
			return session.performAction(actionName, taskId);
		}
		finally {
			LOGGER.info("[performAction] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public List<BpmTask> performAction(String actionName, BpmTask bpmTask) {
		long start = System.currentTimeMillis();
		try {
			return session.performAction(actionName, bpmTask);
		}
		finally {
			LOGGER.info("[performAction] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public List<BpmTask> performAction(String actionName, BpmTask bpmTask, boolean reloadTask) {
		long start = System.currentTimeMillis();
		try {
			return session.performAction(actionName, bpmTask, reloadTask);
		}
		finally {
			LOGGER.info("[performAction] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public List<BpmTask> performAction(ProcessStateAction action, BpmTask bpmTask) {
		long start = System.currentTimeMillis();
		try {
			return session.performAction(action, bpmTask);
		}
		finally {
			LOGGER.info("[performAction] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public BpmTask assignTaskFromQueue(String queueName) {
		long start = System.currentTimeMillis();
		try {
			return session.assignTaskFromQueue(queueName);
		}
		finally {
			LOGGER.info("[assignTaskFromQueue] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public BpmTask assignTaskFromQueue(String queueName, String taskId) {
		long start = System.currentTimeMillis();
		try {
			return session.assignTaskFromQueue(queueName, taskId);
		}
		finally {
			LOGGER.info("[assignTaskFromQueue] t = " + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public BpmTask assignTaskToUser(String taskId, String userLogin) {
		return session.assignTaskToUser(taskId, userLogin);
	}

	@Override
	public BpmTask getTaskData(String taskId) {
		return session.getTaskData(taskId);
	}

	@Override
	public BpmTask getPastOrActualTask(ProcessInstanceLog log) {
		return session.getPastOrActualTask(log);
	}

	@Override
	public BpmTask getPastEndTask(ProcessInstanceLog log) {
		return session.getPastEndTask(log);
	}

	@Override
	public BpmTask refreshTaskData(BpmTask task) {
		return session.refreshTaskData(task);
	}

	@Override
	public int getTasksCount(ProcessInstanceFilter queueFilter) {
		return session.getTasksCount(queueFilter);
	}

	@Override
	public int getFilteredTasksCount(ProcessInstanceFilter filter) {
		return session.getFilteredTasksCount(filter);
	}

	@Override
	public BpmTask getHistoryTask(String taskId) {
		return session.getHistoryTask(taskId);
	}

	@Override
	public BpmTask getLastHistoryTaskByName(Long internalProcessId, String stepName) {
		return session.getLastHistoryTaskByName(internalProcessId, stepName);
	}

	@Override
	public List<BpmTask> getAllTasks() {
		return session.getAllTasks();
	}

	@Override
	public List<BpmTask> findUserTasks(ProcessInstance processInstance) {
		return session.findUserTasks(processInstance);
	}

	@Override
	public List<BpmTask> findUserTasks(Integer offset, Integer limit) {
		return session.findUserTasks(offset, limit);
	}

	@Override
	public List<BpmTask> findProcessTasks(ProcessInstance pi) {
		return session.findProcessTasks(pi);
	}

	@Override
	public List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin) {
		return session.findProcessTasks(pi, userLogin);
	}

	@Override
	public List<BpmTask> findProcessTasks(ProcessInstance pi, String userLogin, Set<String> taskNames) {
		return session.findProcessTasks(pi, userLogin, taskNames);
	}

	@Override
	public List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter) {
		return session.findFilteredTasks(filter);
	}

	@Override
	public List<BpmTask> findFilteredTasks(ProcessInstanceFilter filter, int resultOffset, int maxResults) {
		return session.findFilteredTasks(filter, resultOffset, maxResults);
	}

	@Override
	public List<BpmTask> findRecentTasks(Date minDate, Integer offset, Integer limit) {
		return session.findRecentTasks(minDate, offset, limit);
	}

	@Override
	public List<BpmTaskNotification> getNotifications(Date date, Locale locale) {
		return session.getNotifications(date, locale);
	}

	@Override
	public int getRecentTasksCount(Date minDate) {
		return session.getRecentTasksCount(minDate);
	}

	@Override
	public List<ProcessDefinitionConfig> getAvailableConfigurations() {
		return session.getAvailableConfigurations();
	}

	@Override
	public List<ProcessQueue> getUserAvailableQueues() {
		return session.getUserAvailableQueues();
	}

	@Override
	public Set<String> getPermissionsForWidget(ProcessStateWidget widget) {
		return session.getPermissionsForWidget(widget);
	}

	@Override
	public Set<String> getPermissionsForAction(ProcessStateAction action) {
		return session.getPermissionsForAction(action);
	}

	@Override
	public boolean hasPermissionsForDefinitionConfig(ProcessDefinitionConfig config) {
		return session.hasPermissionsForDefinitionConfig(config);
	}

	@Override
	public String getUserLogin() {
		return session.getUserLogin();
	}

	@Override
	public String getSubstitutingUserLogin() {
		return session.getSubstitutingUserLogin();
	}

	@Override
	public Collection<String> getRoleNames() {
		return session.getRoleNames();
	}

	@Override
	public void adminCancelProcessInstance(String internalId) {
		session.adminCancelProcessInstance(internalId);
	}

	@Override
	public void adminForwardProcessTask(String taskId, String userLogin, String targetUserLogin) {
		session.adminForwardProcessTask(taskId, userLogin, targetUserLogin);
	}

	@Override
	public void adminReassignProcessTask(String taskId, String userLogin) {
		session.adminReassignProcessTask(taskId, userLogin);
	}

	@Override
	public void adminCompleteTask(String taskId, String actionName) {
		session.adminCompleteTask(taskId, actionName);
	}

	@Override
	public List<String> getAvailableLogins(String filter) {
		return session.getAvailableLogins(filter);
	}

	@Override
	public byte[] getProcessLatestDefinition(String bpmDefinitionKey) {
		return session.getProcessLatestDefinition(bpmDefinitionKey);
	}

	@Override
	public byte[] getProcessDefinition(ProcessInstance pi) {
		return session.getProcessDefinition(pi);
	}

	@Override
	public byte[] getProcessMapImage(ProcessInstance pi) {
		return session.getProcessMapImage(pi);
	}

	@Override
	public boolean differsFromTheLatest(String bpmDefinitionKey, byte[] newDefinition) {
		return session.differsFromTheLatest(bpmDefinitionKey, newDefinition);
	}

	@Override
	public String deployProcessDefinition(String processId, InputStream definitionStream, InputStream processMapImageStream) {
		return session.deployProcessDefinition(processId, definitionStream, processMapImageStream);
	}

	@Override
	public ProcessDiagram getProcessDiagram(BpmTask task, I18NSource i18NSource) {
		long start = System.currentTimeMillis();
		try {
			return session.getProcessDiagram(task, i18NSource);
		}
		finally {
			LOGGER.info("[getProcessDiagram] t = " + (System.currentTimeMillis() - start));
		}
	}
}
