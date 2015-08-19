package pl.net.bluesoft.rnd.processtool.ui.basewidgets.view;

import pl.net.bluesoft.rnd.processtool.plugins.GuiRegistry;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.web.domain.IContentProvider;
import pl.net.bluesoft.rnd.processtool.web.view.*;

import java.util.Map;

/**
 * Created by mpawlak@bluesoft.net.pl on 2014-09-02.
 */
@TaskListView(
        queueId = GuiRegistry.STANDARD_PROCESS_QUEUE_ID,
        queueDisplayedName = "controlling.queue.name",
        queueDisplayedDescription = "controlling.queue.desc",
        fileName = "bpm-queue-view.html",
        mainFactory = BpmTaskBeanFactory.class,
        priority = 3,
        queueType = AbstractTaskListView.QueueTypes.CUSTOM_QUEUE,
        processFactories = {})
public class BpmQueueView extends AbstractTaskListView
{

    public BpmQueueView(IContentProvider contentProvider, ITasksListViewBeanFactory providedMainFactory) {
        super(contentProvider, providedMainFactory);
    }

    @Override
    public ProcessInstanceFilter getProcessInstanceFilter(Map<String, Object> parameters)
    {
        String queueId = (String)parameters.get(AbstractTaskListView.PARAMETER_QUEUE_ID);

        ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter();
        processInstanceFilter.setFilterOwnerLogin(queueId);
        processInstanceFilter.addQueue(queueId);
        processInstanceFilter.setViewName(GuiRegistry.STANDARD_PROCESS_QUEUE_ID);
        processInstanceFilter.getAdditionalParameters().putAll(parameters);
        processInstanceFilter.setBpmTaskQueryCondition(new BpmTaskQueryCondition());
        return processInstanceFilter;
    }

}
