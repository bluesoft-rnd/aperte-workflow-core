package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.web.domain.IContentProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.*;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public abstract class AbstractTaskListView implements Comparable<AbstractTaskListView>
{
    public static enum QueueTypes
    {
        PROCESS,
        CUSTOM_QUEUE
    }


    public static final String PARAMETER_USER_LOGIN = "userLogin";
    public static final String PARAMETER_USER = "user";
    public static final String PARAMETER_QUEUE_ID = "queueId";

    private QueueTypes queueType;
    private IContentProvider contentProvider;
    private Integer priority;
    private String queueDisplayedName;
    private String queueDisplayedDesc;
    private String queueId;
    private ITasksListViewBeanFactory mainFactory;
    private Map<String, ITasksListViewBeanFactory> processFactories = new HashMap<String, ITasksListViewBeanFactory>();

    public AbstractTaskListView(IContentProvider contentProvider, ITasksListViewBeanFactory providedMainFactory)
    {
        this.contentProvider = contentProvider;
        this.mainFactory = providedMainFactory;
    }

    public IContentProvider getContentProvider() {
        return contentProvider;
    }

    protected AbstractTaskListView setMainFactory(ITasksListViewBeanFactory factory)
    {
        this.mainFactory = factory;

        return this;
    }

    public AbstractTaskListView setProcessFactory(String processName, ITasksListViewBeanFactory factory)
    {
        processFactories.put(processName, factory);

        return this;
    }

    /** Create taks list view bean for view */
    public TasksListViewBean createFrom(BpmTask task, I18NSource messageSource)
    {
        String processName = task.getProcessInstance().getDefinition().getBpmDefinitionKey();

        ITasksListViewBeanFactory taskListFactory = getProcessFactory(processName);
        /* No specialized factory for process, use main factory */
        if(taskListFactory == null)
            taskListFactory =  getMainFactory();

        return taskListFactory.createFrom(task, messageSource);

    }

    public ITasksListViewBeanFactory getMainFactory()
    {
        return this.mainFactory;
    }

    public ITasksListViewBeanFactory getProcessFactory(String processName)
    {
        return this.processFactories.get(processName);
    }

    public abstract ProcessInstanceFilter getProcessInstanceFilter(Map<String, Object> parameters);

    /** Get role names which user is required to have, to see this view. Default no role is required */
    public Set<String> getRoleNames()
    {
        return new HashSet<String>();
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(AbstractTaskListView o) {
        return this.priority.compareTo(o.getPriority());
    }

    public String getQueueDisplayedName() {
        return queueDisplayedName;
    }

    public void setQueueDisplayedName(String queueDisplayedName) {
        this.queueDisplayedName = queueDisplayedName;
    }

    public String getQueueDisplayedDesc() {
        return queueDisplayedDesc;
    }

    public void setQueueDisplayedDesc(String queueDisplayedDesc) {
        this.queueDisplayedDesc = queueDisplayedDesc;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public QueueTypes getQueueType() {
        return queueType;
    }

    public void setQueueType(QueueTypes queueType) {
        this.queueType = queueType;
    }
}
