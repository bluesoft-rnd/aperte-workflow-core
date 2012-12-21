package pl.net.bluesoft.rnd.processtool.ui.activity;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TaskTableItem;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderBase;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderParams;

import com.vaadin.ui.Component;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class MyProcessesListPane extends ProcessListPane {

    public MyProcessesListPane(ActivityMainPane activityMainPane, String title) {
        super(activityMainPane, title);
    }

    public MyProcessesListPane(ActivityMainPane activityMainPane, ProcessInstanceFilter filter) {
        super(activityMainPane, filter.getName(), filter);
    }

    @Override
    protected ProcessInstanceFilter getDefaultFilter() {
        ProcessInstanceFilter processFilter = new ProcessInstanceFilter();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        processFilter.setName(getMessage("activity.assigned.tasks"));
        processFilter.addOwner(getBpmSession().getUser(ctx));
        processFilter.setFilterOwner(getBpmSession().getUser(ctx));
        processFilter.addQueueType(QueueType.ASSIGNED_TO_CURRENT_USER);
        return processFilter;
    }

    @Override
    protected Component getTaskItem(final TaskTableItem tti) {
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
			public void run() {
                displayProcessData(params.getTask());
            }
        });
    }

    @Override
    protected void sortTaskItems(List<TaskTableItem> taskItems) {
        final Date now = new Date();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        List<TaskTableItem> outdatedItems = new ArrayList<TaskTableItem>();
        List<TaskTableItem> recentItems = new ArrayList<TaskTableItem>();
        List<TaskTableItem> finishedItems = new ArrayList<TaskTableItem>();
        for (TaskTableItem tti : taskItems) {
            BpmTask task = tti.getTask();
            if (getBpmSession().isProcessRunning(task.getProcessInstance().getInternalId(), ctx)) {
                if (isOutdated(now, getDeadlineDate(task))) {
                    outdatedItems.add(tti);
                }
                else {
                    recentItems.add(tti);
                }
            }
            else {
                finishedItems.add(tti);
            }
        }
        taskItems.clear();
        for (List<TaskTableItem> list : new List[] {outdatedItems, recentItems, finishedItems}) {
            Collections.sort(list, new Comparator<TaskTableItem>() {
                @Override
                public int compare(TaskTableItem o1, TaskTableItem o2) {
                    return o2.getTask().getCreateDate().compareTo(o1.getTask().getCreateDate());
                }
            });
            taskItems.addAll(list);
        }
    }

    public static boolean isOutdated(Date baseDate, Date checkedDate) {
        return checkedDate != null && checkedDate.before(baseDate);
    }

    protected void displayProcessData(BpmTask task) {
        activityMainPane.displayProcessData(task);
    }

    /** Metoda wylicza date wygasniecia procesu. W przypadku podprocesow, siega
     * do atrubutow procesu głównego
     */
    public static Date getDeadlineDate(BpmTask task) 
    {
        ProcessInstance process = task.getProcessInstance();
        
        Set<ProcessDeadline> deadlines = process.findAttributesByClass(ProcessDeadline.class);
        for (ProcessDeadline pd : deadlines) {
            if (pd.getTaskName().equalsIgnoreCase(task.getTaskName())) {
                return pd.getDueDate();
            }
        }
        return null;
    }

    @Override
    protected boolean getDataPaneUsesSpacing() {
        return false;
    }

	public void setType(QueueType type) 
	{
		// TODO Auto-generated method stub
		
	}
}
