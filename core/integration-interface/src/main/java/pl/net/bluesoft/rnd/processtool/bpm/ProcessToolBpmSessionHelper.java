package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.withContext;

/**
 * User: POlszewski
 * Date: 2013-06-26
 * Time: 09:52
 */
public class ProcessToolBpmSessionHelper {
	public static ProcessToolBpmSession createSession(final ProcessToolBpmSession session, ProcessToolContext ctx, final UserData user, final Collection<String> roleNames) {
		return withContext(ctx, new ReturningProcessToolContextCallback<ProcessToolBpmSession>() {
			@Override
			public ProcessToolBpmSession processWithContext(ProcessToolContext ctx) {
				return session.createSession(user, roleNames);
			}
		}); 		
	}

	public static ProcessInstance startProcess(final ProcessToolBpmSession session, ProcessToolContext ctx, final String bpmDefinitionId,
										final String externalKey, final String description, final String keyword, final String source) {
		return withContext(ctx, new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				return session.startProcess(bpmDefinitionId, externalKey, description, keyword, source);
			}
		});
	}

	public static BpmTask performAction(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessStateAction action, final BpmTask bpmTask) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.performAction(action, bpmTask);
			}
		});
	}

	public static BpmTask assignTaskFromQueue(final ProcessToolBpmSession session, ProcessToolContext ctx, final String queueName) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.assignTaskFromQueue(queueName);
			}
		});
	}

	public static BpmTask assignTaskFromQueue(final ProcessToolBpmSession session, ProcessToolContext ctx, final String queueName, final BpmTask task) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.assignTaskFromQueue(queueName, task);
			}
		});
	}

	public static void assignTaskToUser(final ProcessToolBpmSession session, ProcessToolContext ctx, final String taskId, final String userLogin) {
		withContext(ctx, new ProcessToolContextCallback() {
			@Override
			public void withContext(ProcessToolContext ctx) {
				session.assignTaskToUser(taskId, userLogin);
			}
		});
	}

	public static ProcessInstance getProcessData(final ProcessToolBpmSession session, ProcessToolContext ctx, final String internalId) {
		return withContext(ctx, new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				return session.getProcessData(internalId);
			}
		});
	}

	public static ProcessInstance refreshProcessData(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance processInstance) {
		return withContext(ctx, new ReturningProcessToolContextCallback<ProcessInstance>() {
			@Override
			public ProcessInstance processWithContext(ProcessToolContext ctx) {
				return session.refreshProcessData(processInstance);
			}
		});
	}

	public static void saveProcessInstance(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance processInstance) {
		withContext(ctx, new ProcessToolContextCallback() {
			@Override
			public void withContext(ProcessToolContext ctx) {
				session.saveProcessInstance(processInstance);
			}
		});
	}

	public static BpmTask getTaskData(final ProcessToolBpmSession session, ProcessToolContext ctx, final String taskId) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.getTaskData(taskId);
			}
		});
	}

	public static BpmTask getPastOrActualTask(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstanceLog log) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.getPastOrActualTask(log);
			}
		});
	}

	public static BpmTask getPastEndTask(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstanceLog log) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.getPastEndTask(log);
			}
		});
	}

	public static BpmTask refreshTaskData(final ProcessToolBpmSession session, ProcessToolContext ctx, final BpmTask task) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.refreshTaskData(task);
			}
		});
	}

	public static boolean isProcessRunning(final ProcessToolBpmSession session, ProcessToolContext ctx, final String internalId) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Boolean>() {
			@Override
			public Boolean processWithContext(ProcessToolContext ctx) {
				return session.isProcessRunning(internalId);
			}
		});
	}

	public static int getTasksCount(final ProcessToolBpmSession session, ProcessToolContext ctx, final String userLogin, final QueueType... queueTypes) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Integer>() {
			@Override
			public Integer processWithContext(ProcessToolContext ctx) {
				return session.getTasksCount(userLogin, queueTypes);
			}
		});
	}

	public static int getTasksCount(final ProcessToolBpmSession session, ProcessToolContext ctx, final String userLogin, final Collection<QueueType> queueTypes) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Integer>() {
			@Override
			public Integer processWithContext(ProcessToolContext ctx) {
				return session.getTasksCount(userLogin, queueTypes);
			}
		});
	}

	public static int getFilteredTasksCount(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstanceFilter filter) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Integer>() {
			@Override
			public Integer processWithContext(ProcessToolContext ctx) {
				return session.getFilteredTasksCount(filter);
			}
		});
	}

	public static List<BpmTask> getAllTasks(final ProcessToolBpmSession session, ProcessToolContext ctx) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.getAllTasks();
			}
		});
	}

	public static List<BpmTask> findUserTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance processInstance) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findUserTasks(processInstance);
			}
		});
	}
	
	public static List<BpmTask> findUserTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final Integer offset, final Integer limit) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findUserTasks(offset, limit);
			}
		});
	}

	public static List<BpmTask> findProcessTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance pi) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findProcessTasks(pi);
			}
		});
	}
	
	public static List<BpmTask> findProcessTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance pi,
										  final String userLogin) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findProcessTasks(pi, userLogin);
			}
		});
	}
	
	public static List<BpmTask> findProcessTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance pi,
										  final String userLogin, final Set<String> taskNames) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findProcessTasks(pi, userLogin, taskNames);
			}
		});
	}

	public static List<BpmTask> findFilteredTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstanceFilter filter) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findFilteredTasks(filter);
			}
		});
	}

	public static List<BpmTask> findFilteredTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstanceFilter filter,
										   final int resultOffset, final int maxResults) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findFilteredTasks(filter, resultOffset, maxResults);
			}
		});
	}

	public static List<BpmTask> findRecentTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final Date minDate,
										 final Integer offset, final Integer limit) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findRecentTasks(minDate, offset, limit);
			}
		});
	}

	public static int getRecentTasksCount(final ProcessToolBpmSession session, ProcessToolContext ctx, final Date minDate) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Integer>() {
			@Override
			public Integer processWithContext(ProcessToolContext ctx) {
				return session.getRecentTasksCount(minDate);
			}
		});
	}

	public static Collection<ProcessDefinitionConfig> getAvailableConfigurations(final ProcessToolBpmSession session, ProcessToolContext ctx) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Collection<ProcessDefinitionConfig>>() {
			@Override
			public Collection<ProcessDefinitionConfig> processWithContext(ProcessToolContext ctx) {
				return session.getAvailableConfigurations();
			}
		});
	}
	
	public static List<ProcessQueue> getUserAvailableQueues(final ProcessToolBpmSession session, ProcessToolContext ctx) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<ProcessQueue>>() {
			@Override
			public List<ProcessQueue> processWithContext(ProcessToolContext ctx) {
				return session.getUserAvailableQueues();
			}
		});
	}
	
	public static Set<String> getPermissionsForWidget(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessStateWidget widget) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Set<String>>() {
			@Override
			public Set<String> processWithContext(ProcessToolContext ctx) {
				return session.getPermissionsForWidget(widget);
			}
		});
	}
	
	public static Set<String> getPermissionsForAction(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessStateAction action) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Set<String>>() {
			@Override
			public Set<String> processWithContext(ProcessToolContext ctx) {
				return session.getPermissionsForAction(action);
			}
		});
	}
	
	public static boolean hasPermissionsForDefinitionConfig(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessDefinitionConfig config) {
		return withContext(ctx, new ReturningProcessToolContextCallback<Boolean>() {
			@Override
			public Boolean processWithContext(ProcessToolContext ctx) {
				return session.hasPermissionsForDefinitionConfig(config);
			}
		});
	}

//	public static String getUserLogin(ProcessToolBpmSession session, ProcessToolContext ctx, ) {}
	public static UserData getUser(final ProcessToolBpmSession session, ProcessToolContext ctx) {
		return withContext(ctx, new ReturningProcessToolContextCallback<UserData>() {
			@Override
			public UserData processWithContext(ProcessToolContext ctx) {
				return session.getUser();
			}
		});
	}

	public static UserData getSubstitutingUser(final ProcessToolBpmSession session, ProcessToolContext ctx) {
		return withContext(ctx, new ReturningProcessToolContextCallback<UserData>() {
			@Override
			public UserData processWithContext(ProcessToolContext ctx) {
				return session.getSubstitutingUser();
			}
		});
	}
//	public static UserData loadOrCreateUser(ProcessToolBpmSession session, ProcessToolContext ctx, UserData userData) {}
//	public static Collection<String> getRoleNames(ProcessToolBpmSession session, ProcessToolContext ctx, ) {}

//	public static EventBusManager getEventBusManager(ProcessToolBpmSession session, ProcessToolContext ctx, ) {}
//
//	public static void adminCancelProcessInstance(ProcessToolBpmSession session, ProcessToolContext ctx, ProcessInstance pi) {}
//	public static void adminReassignProcessTask(ProcessToolBpmSession session, ProcessToolContext ctx, ProcessInstance pi, BpmTask bpmTask, String userLogin) {}
//	public static void adminCompleteTask(ProcessToolBpmSession session, ProcessToolContext ctx, ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action) {}
//
//	public static List<String> getAvailableLogins(ProcessToolBpmSession session, ProcessToolContext ctx, String filter) {}
//
//	public static List<GraphElement> getProcessHistory(ProcessToolBpmSession session, ProcessToolContext ctx, ProcessInstance pi) {}

//	byte[] getProcessLatestDefinition(ProcessToolBpmSession session, ProcessToolContext ctx, String bpmDefinitionKey, String processName) {}
//	byte[] getProcessDefinition(ProcessToolBpmSession session, ProcessToolContext ctx, ProcessInstance pi) {}
//	byte[] getProcessMapImage(ProcessToolBpmSession session, ProcessToolContext ctx, ProcessInstance pi) {}
//
//	String deployProcessDefinition(ProcessToolBpmSession session, ProcessToolContext ctx, String processName, String bpmDefinitionKey, InputStream definitionStream, InputStream processMapImageStream) {}
//	

	private ProcessToolBpmSessionHelper() {}
}
