package pl.net.bluesoft.rnd.processtool.ui.activity;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TaskTableItem;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderBase;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderParams;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.ui.Component;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class MyProcessesListPane extends ProcessListPane {
    public MyProcessesListPane(ActivityMainPane activityMainPane, String title) {
        super(activityMainPane, title);
    }

    @Override
    protected ProcessInstanceFilter getDefaultFilter() {
        ProcessInstanceFilter processFilter = new ProcessInstanceFilter();
        processFilter.setName(getMessage("activity.assigned.tasks"));
		processFilter.setFilterOwner(getBpmSession().getUser());
//		processFilter.addOwner(processFilter.getFilterOwner());
        processFilter.addQueueType(QueueType.ASSIGNED_TO_CURRENT_USER);
        return processFilter;
    }

    @Override
    protected Component getTaskItem(TaskTableItem tti) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessInstance pi = tti.getTask().getProcessInstance();
        TaskItemProviderBase tip = getTaskItemProvider(ctx, pi);
        TaskItemProviderParams params = getTaskItemProviderParams(ctx, tti);
        return tip.getTaskPane(params);
    }

    @Override
    protected void onClick(final TaskItemProviderParams params) {
        withErrorHandling(getApplication(), new Runnable() {
            @Override
			public void run() 
            {
				getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
					@Override
					public void withContext(ProcessToolContext ctx) {
						I18NSource.ThreadUtil.setThreadI18nSource(messageSource);
						displayProcessData(params.getTask());
					}
				});
            }
        });
    }

	protected void displayProcessData(BpmTask task) {
        activityMainPane.displayProcessData(getBpmSession().getTaskData(task.getInternalTaskId()));
    }

	@Override
    protected boolean getDataPaneUsesSpacing() {
        return false;
    }
}
