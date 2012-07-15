package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import org.aperteworkflow.util.view.AbstractListPane;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TaskTableItem;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderBase;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProviderParams;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.AligningHorizontalLayout;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class ProcessListPane extends AbstractListPane {
    protected ActivityMainPane activityMainPane;
    protected VerticalLayout dataPane = new VerticalLayout();
    protected int limit = 10;
    protected int offset = 0;
    private List<TaskTableItem> processInstances = Collections.synchronizedList(new LinkedList<TaskTableItem>());
    private TasksFilterBox filterBox;
    private ProcessInstanceFilter filter;
    private static boolean defaultTaskItemRegistered = false;

    public ProcessListPane(ActivityMainPane activityMainPane, String title, ProcessInstanceFilter filter) {
        super(activityMainPane.getApplication(), activityMainPane.getI18NSource(), title);
        this.activityMainPane = activityMainPane;
        this.filter = filter;
    }

    public ProcessListPane(ActivityMainPane activityMainPane, String title) {
        this(activityMainPane, title, null);
    }

    public void setFilter(ProcessInstanceFilter filter) {
        this.filter = filter;
    }

    @Override
    public ProcessListPane init() {
        super.init();

        VerticalLayout marginPanel = new VerticalLayout();
        marginPanel.addComponent(new Label(getMessage("activity.tasks.help.short"), Label.CONTENT_XHTML));
        marginPanel.setMargin(true);
        marginPanel.setWidth("100%");
        addComponent(marginPanel);

        filterBox = new TasksFilterBox(messageSource, getBpmSession(), application, this, processInstances);
        filterBox.setFilter(filter == null ? getDefaultFilter() : filter);
        filterBox.setLimit(limit);

        marginPanel.addComponent(filterBox);
        addComponent(dataPane);
        setExpandRatio(dataPane, 1.0f);
        refreshData();
        reloadView();

        filterBox.addListener(new TasksFilterBox.ItemSetChangeListener() {
            @Override
            public void itemSetChange() {
                reloadView();
            }
        });
        return this;
    }

    protected ProcessToolBpmSession getBpmSession() {
        return activityMainPane.getBpmSession();
    }

    protected abstract ProcessInstanceFilter getDefaultFilter();

    public void reloadView() {
        dataPane.setSpacing(getDataPaneUsesSpacing());
        dataPane.setMargin(true);
        dataPane.setWidth("100%");
        dataPane.removeAllComponents();

        if (offset > getTotalResults() - 1) {
            offset = getTotalResults() - getTotalResults() % limit;
        }
        if (offset < 0) {
            offset = 0;
        }

        Component topNavigation = getNavigation();

        dataPane.addComponent(topNavigation);
        dataPane.setComponentAlignment(topNavigation, Alignment.TOP_RIGHT);

        synchronized (processInstances) {
            sortTaskItems(processInstances);
            for (TaskTableItem tti : processInstances) {
                dataPane.addComponent(getTaskItem(tti));
            }
        }

        Component bottomNavigation = getNavigation();

        dataPane.addComponent(bottomNavigation);
        dataPane.setComponentAlignment(bottomNavigation, Alignment.TOP_RIGHT);
    }

    protected void sortTaskItems(List<TaskTableItem> taskItems) {
    }

    public void refreshData() {
        filterBox.refreshData(getBpmTasks());
    }

    protected boolean getDataPaneUsesSpacing() {
        return true;
    }

    protected ResultsPageWrapper<BpmTask> getBpmTasks() {
        return filterBox.getBpmTasks(offset);
    }

    protected abstract Component getTaskItem(TaskTableItem tti);

    private Component getNavigation() {
        AligningHorizontalLayout ahl = new AligningHorizontalLayout(Alignment.MIDDLE_RIGHT, true);
        ahl.setWidth("100%");

        Button prevButton = VaadinUtility.link(getMessage("activity.tasks.previous"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset -= limit;
                if (offset < 0) {
                    offset = 0;
                }
                refreshData();
            }
        });
        prevButton.setEnabled(offset > 0);

        int first = getTotalResults() > 0 ? offset + 1 : 0;
        int last = Math.min(offset + limit, getTotalResults());
        
        HorizontalLayout resultsLayout = new HorizontalLayout();
        resultsLayout.setMargin(false);
        resultsLayout.setWidth("70px");

        Label resultsLabel = new Label(String.format(getMessage("activity.tasks.of.line"), first, last, getTotalResults()));
        resultsLayout.addComponent(resultsLabel);
        resultsLayout.setComponentAlignment(resultsLabel, Alignment.MIDDLE_CENTER);

        Button nextButton = VaadinUtility.link(getMessage("activity.tasks.next"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset += limit;
                if (offset > getTotalResults() - 1) {
                    offset = getTotalResults() - 1;
                }
                refreshData();
            }
        });
        nextButton.setEnabled(offset + limit < getTotalResults());

        ahl.setMargin(false, true, false, true);
        ahl.addComponents(new Component[] {new Label() {{
            setWidth("100%");
        }}, prevButton, resultsLayout, nextButton});
        
        return ahl;
    }

    public void setNewSearch() {
        offset = 0;
        titleLabel.setValue(getMessage("activity.search.results"));
    }

    public ActivityMainPane getActivityMainPane() {
        return activityMainPane;
    }

    public int getTotalResults() {
        return filterBox.getTotalResults();
    }

	protected static String getLogin(UserData userData) {
		return userData != null ? userData.getLogin() : null;
	}

//    private static void registerDefaultTaskItem(ProcessToolContext ctx) {
//        if (!defaultTaskItemRegistered) {
//            ctx.getRegistry().registerTaskItemProvider(TaskItemProviderBase.class);
//            defaultTaskItemRegistered = true;
//        }
//    }

    protected TaskItemProviderBase getTaskItemProvider(final ProcessToolContext ctx, ProcessInstance pi) {
//        registerDefaultTaskItem(ctx);
        String itemClass = pi.getDefinition().getTaskItemClass();
        if (hasText(itemClass) && !itemClass.equals("null")) {
            try {
                return new TaskItemProviderBase(ctx.getRegistry().makeTaskItemProvider(itemClass.trim()));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new TaskItemProviderBase(null);
    }

    private TaskItemProviderParams createTaskItemProviderParams() {
        return new TaskItemProviderParams() {
            @Override
            public Resource getImage(String image) {
                return ProcessListPane.this.getImage(image);
            }

            @Override
            public Resource getResource(String path) {
                return ProcessListPane.this.getResource(path);
            }

            @Override
            public Resource getStreamResource(String path, final byte[] bytes) {
                Resource res = new StreamResource(new StreamResource.StreamSource() {
                    @Override
                    public InputStream getStream() {
                        return new ByteArrayInputStream(bytes);
                    }
                }, path, application);
                cacheResource(path, res);
                return res;
            }

            @Override
            public void onClick() {
                ProcessListPane.this.onClick(this);
            }
        };
    }

    protected TaskItemProviderParams getTaskItemProviderParams(ProcessToolContext ctx, TaskTableItem tti) {
        TaskItemProviderParams params = createTaskItemProviderParams();
        params.setCtx(ctx);
        params.setBpmSession(getBpmSession());
        params.setI18NSource(messageSource);
        params.setProcessInstance(tti.getTask().getProcessInstance());
        params.setTask(tti.getTask());
        params.setProcessStateConfiguration(tti.getStateConfiguration());
        params.setState(tti.getState());
        params.setQueue(null);
        return params;
    }

    protected abstract void onClick(TaskItemProviderParams params);
}
