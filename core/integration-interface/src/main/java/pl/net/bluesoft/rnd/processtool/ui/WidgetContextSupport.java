package pl.net.bluesoft.rnd.processtool.ui;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public interface WidgetContextSupport {
    Map<ProcessToolDataWidget, Collection<String>> getWidgetsErrors(BpmTask bpmTask, boolean skipRequired);
    Set<ProcessToolDataWidget> getWidgets();
    void displayValidationErrors(Map<ProcessToolDataWidget, Collection<String>> widgetsErrors);

    boolean validateWidgetsAndSaveData(BpmTask task);
    void saveTaskData(BpmTask task, ProcessToolActionButton... actions);

    ProcessToolContext getCurrentContext();
    BpmTask refreshTask(ProcessToolBpmSession bpmSession, BpmTask bpmTask);
    void updateTask(BpmTask task);

    void saveTaskWithoutData(BpmTask task, ProcessToolActionButton... actions);
}
