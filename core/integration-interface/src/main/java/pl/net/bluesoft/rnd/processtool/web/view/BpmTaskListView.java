package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.filters.factory.ProcessInstanceFilterFactory;
import pl.net.bluesoft.rnd.processtool.web.domain.IContentProvider;

import java.util.Map;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
@TaskListView(
        queueId = "myTasks",
        queueDisplayedName = "aperte.task.view.mytasks.name",
        queueDisplayedDescription = "aperte.task.view.mytasks.desc",
        fileName = "my-task-bpm-task.html",
        mainFactory = BpmTaskBeanFactory.class,
        queueType = AbstractTaskListView.QueueTypes.PROCESS,
        priority = 1,
        processFactories = {}
)
public class BpmTaskListView extends AbstractTaskListView
{

    public BpmTaskListView(IContentProvider contentProvider, ITasksListViewBeanFactory providedMainFactory) {
        super(contentProvider,providedMainFactory);
    }

    @Override
    public ProcessInstanceFilter getProcessInstanceFilter(Map<String, Object> parameters)
    {
        ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();

        ProcessInstanceFilter filter = filterFactory.createAllTasksFilter((String)parameters.get(PARAMETER_USER_LOGIN));
        filter.getAdditionalParameters().putAll(parameters);
        filter.setBpmTaskQueryCondition(new BpmTaskQueryCondition());

        return filter;
    }

}
