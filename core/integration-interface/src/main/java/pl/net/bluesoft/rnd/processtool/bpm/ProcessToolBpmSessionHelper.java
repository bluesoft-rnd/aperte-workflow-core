package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;

import java.util.List;
import java.util.Set;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.withContext;

/**
 * User: POlszewski
 * Date: 2013-06-26
 * Time: 09:52
 */
public class ProcessToolBpmSessionHelper {
	public static StartProcessResult startProcess(final ProcessToolBpmSession session, ProcessToolContext ctx, final String bpmDefinitionId,
										final String externalKey, final String source) {
		return withContext(ctx, new ReturningProcessToolContextCallback<StartProcessResult>() {
			@Override
			public StartProcessResult processWithContext(ProcessToolContext ctx) {
				return session.startProcess(bpmDefinitionId, externalKey, source);
			}
		});
	}

	public static List<BpmTask> performAction(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessStateAction action, final BpmTask bpmTask) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.performAction(action, bpmTask);
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


	public static BpmTask getTaskData(final ProcessToolBpmSession session, ProcessToolContext ctx, final String taskId) {
		return withContext(ctx, new ReturningProcessToolContextCallback<BpmTask>() {
			@Override
			public BpmTask processWithContext(ProcessToolContext ctx) {
				return session.getTaskData(taskId);
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

	public static List<BpmTask> findProcessTasks(final ProcessToolBpmSession session, ProcessToolContext ctx, final ProcessInstance pi) {
		return withContext(ctx, new ReturningProcessToolContextCallback<List<BpmTask>>() {
			@Override
			public List<BpmTask> processWithContext(ProcessToolContext ctx) {
				return session.findProcessTasks(pi);
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

	private ProcessToolBpmSessionHelper() {}
}
