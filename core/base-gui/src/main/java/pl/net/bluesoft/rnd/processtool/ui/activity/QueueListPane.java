package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.TaskState;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TaskTableItem;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderBase;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderParams;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class QueueListPane extends ProcessListPane {
    private ProcessQueue queue;

    public QueueListPane(ActivityMainPane activityMainPane) {
        super(activityMainPane, null);
    }

    public void setQueue(ProcessQueue queue) {
        this.queue = queue;
        setTitle(queue.getDescription());
    }

    @Override
    protected Component getTaskItem(final TaskTableItem tti) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessInstance pi = tti.getTask().getProcessInstance();
        TaskItemProviderBase tip = getTaskItemProvider(ctx, pi);
        TaskItemProviderParams params = getTaskItemProviderParams(ctx, tti);
        params.setQueue(queue);
        return tip.getQueuePane(params);
    }

    @Override
    protected void onClick(final TaskItemProviderParams params) {
        withErrorHandling(getApplication(), new Runnable() {
            public void run() {
                BpmTask task = getBpmSession().assignTaskFromQueue(queue, params.getTask(),
                        ProcessToolContext.Util.getThreadProcessToolContext());
                if (task != null) {
                    getApplication().getMainWindow().showNotification(getMessage("process-tool.task.assigned"),
                            Window.Notification.TYPE_HUMANIZED_MESSAGE);
                    displayProcessData(task);
                }
            }
        });
    }

    protected void displayProcessData(BpmTask task) {
        activityMainPane.displayProcessData(task);
    }

    @Override
    protected ProcessInstanceFilter getDefaultFilter() {
        ProcessInstanceFilter tfi = new ProcessInstanceFilter();
        if (queue != null) {
            tfi.addQueue(queue.getName());
        }
        tfi.addQueueType(QueueType.OWN_IN_QUEUE);
        return tfi;
    }
}
