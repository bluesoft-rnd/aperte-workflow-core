package pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem;

import com.vaadin.ui.Component;

/**
 * User: POlszewski
 * Date: 2011-12-14
 * Time: 09:45:21
 */
public interface TaskItemProvider {
    Component getTaskPane(TaskItemProviderParams params);
    Component getQueuePane(TaskItemProviderParams params);

    //new
    Component createQueuePaneProcessInfo(TaskItemProviderParams params);
    Component createTaskPaneProcessId(TaskItemProviderParams params);
    Component getTaskItemProcessInfo(TaskItemProviderParams params);

	String getAssigneeName(TaskItemProviderParams params);
}