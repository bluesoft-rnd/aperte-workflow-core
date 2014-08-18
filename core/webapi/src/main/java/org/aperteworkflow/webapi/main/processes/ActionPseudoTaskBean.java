package org.aperteworkflow.webapi.main.processes;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskDerivedBean;
import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2014-05-15
 */
public class ActionPseudoTaskBean {
	private static final String SEPARATOR = "__";

	public static String getActionPseudoStateName(String taskName, String actionName) {
		return taskName + SEPARATOR + actionName;
	}

	public static String getActionPseudotaskId(String taskId, String actionName) {
		return taskId + SEPARATOR + actionName;
	}

	public static boolean isActionPseudotask(String taskId) {
		return taskId.matches("\\d+" + SEPARATOR + ".+");
	}

	public static String extractJbpmTaskId(String taskId) {
		return taskId.substring(0, taskId.indexOf(SEPARATOR));
	}

	private static String extractActionName(String taskId) {
		return taskId.substring(SEPARATOR.length() + taskId.indexOf(SEPARATOR));
	}

	public static BpmTask createBpmTask(BpmTask task, String taskId) {
		String extractActionName = extractActionName(taskId);
		String actionPseudoStateName = getActionPseudoStateName(task.getTaskName(), extractActionName);
		ProcessStateConfiguration actionPseudoState = task.getProcessDefinition().getProcessStateConfigurationByName(actionPseudoStateName);

		BpmTaskDerivedBean result = new BpmTaskDerivedBean(task);
		result.setCurrentProcessStateConfiguration(actionPseudoState);
		result.setInternalTaskId(taskId);
		return result;
	}

	public static TasksListViewBean createTask(BpmTask task, ProcessStateConfiguration actionPseudoState, String actionName, String viewName, I18NSource messageSource) {
        TasksListViewBean processBean= new TasksListViewBeanFactoryWrapper().createFrom(task, messageSource, viewName);

//		result.setName(actionPseudoState.getName());
        processBean.setTaskId(getActionPseudotaskId(task.getInternalTaskId(), actionName));
        processBean.setUserCanClaim(false);
//		result.set
		return processBean;
	}
}
