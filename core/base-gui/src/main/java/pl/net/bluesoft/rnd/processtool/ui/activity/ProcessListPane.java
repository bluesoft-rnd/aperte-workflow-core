package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.event.FieldEvents;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;
import org.aperteworkflow.util.vaadin.VaadinUtility.HasRefreshButton;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.util.i18n.I18NSource.ThreadUtil.getLocalizedMessage;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;

/**
 * Created by IntelliJ IDEA.
 * User: tomek
 * Date: 4/29/11
 * Time: 8:43 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProcessListPane extends VerticalLayout implements HasRefreshButton {
    protected Logger logger = Logger.getLogger(ProcessListPane.class.getName());

    protected ActivityMainPane activityMainPane;
    protected VerticalLayout dataPane = new VerticalLayout();
    int limit = 10;
    int offset = 0;
    protected TextField searchField = new TextField();
    protected String filterExpression;
    private List<TasksMainPane.TaskTableItem> processInstances = new LinkedList();
    protected pl.net.bluesoft.util.eventbus.EventListener<BpmEvent> bpmEventSubScriber;

    public ProcessListPane(ActivityMainPane activityMainPane, String title) {
        this(activityMainPane, title, true);
    }

    protected ProcessListPane(ActivityMainPane activityMainPane, String title, boolean callInit) {
        this.activityMainPane = activityMainPane;

        if (callInit) {
            init(title);
        }
    }

    protected void init(String title) {
        setWidth("100%");
        setMargin(true);
        setSpacing(true);

        Label l = new Label(getMessage(title));
        l.addStyleName("h1 color processtool-title");

        addComponent(horizontalLayout(l, refreshIcon(activityMainPane.getApplication(), this)));
        Label helpLbl = new Label(getMessage("activity.tasks.help.short"));

        VerticalLayout marginPanel = new VerticalLayout();
        marginPanel.addComponent(helpLbl);
        marginPanel.setMargin(true);
        marginPanel.setWidth("100%");
        addComponent(marginPanel);

        searchField.setInputPrompt(getMessage("search.prompt"));
        searchField.setWidth("100%");
        searchField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
        searchField.setTextChangeTimeout(500);

        marginPanel.addComponent(searchField);
        addComponent(dataPane);
//        refreshData();

        if (bpmEventSubScriber == null) {
            getBpmSession().getEventBusManager().subscribe(BpmEvent.class, bpmEventSubScriber = new pl.net.bluesoft.util.eventbus.EventListener<BpmEvent>() {
                @Override
                public void onEvent(BpmEvent e) {
                    refreshData();
                }
            });
        }
        searchField.addListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                filterExpression = event.getText();
                offset = 0;
                refreshData();
            }
        });
    }

    protected ProcessToolBpmSession getBpmSession() {
        return activityMainPane.getBpmSession();
    }

    public void refreshData() {
//        searchField.focus();
        dataPane.setSpacing(getDataPaneUsesSpacing());
        dataPane.setMargin(true);
        dataPane.setWidth("100%");
        dataPane.removeAllComponents();

        try {
            List<ProcessInstance> userProcesses = getProcessInstances(filterExpression, offset, limit + 1);
            Collections.sort(userProcesses, new Comparator<ProcessInstance>() {
                @Override
                public int compare(ProcessInstance o1, ProcessInstance o2) {
                    return -1 * o1.getId().compareTo(o2.getId());
                }
            });
            List<TasksMainPane.TaskTableItem> filteredProcesses = new ArrayList<TasksMainPane.TaskTableItem>();
            for (ProcessInstance pi : userProcesses) {
                TasksMainPane.TaskTableItem tti = new TasksMainPane.TaskTableItem(pi.getDefinition().getDescription(),
                        pi.getInternalId(), pi.getState(), pi, null);
                if (tti.getState() != null) {
                    for (ProcessStateConfiguration st : pi.getDefinition().getStates()) {
                        if (tti.getState().equals(st.getName())) {
                            tti.setState(st.getDescription());
                            tti.setStateConfiguration(st);
                            break;
                        }
                    }
                }
                filteredProcesses.add(tti);
            }
            processInstances = filteredProcesses;
            if (offset < 0) offset = 0;

            if (!processInstances.isEmpty()) {
                Component topNavigation = getNavigation();
                dataPane.addComponent(topNavigation);
                dataPane.setComponentAlignment(topNavigation, Alignment.TOP_RIGHT);

                for (int i = 0; i < Math.min(limit, processInstances.size()); i++) {
                    TasksMainPane.TaskTableItem tti = processInstances.get(i);
                    dataPane.addComponent(getTaskItem(tti));
                }

                Component bottomNavigation = getNavigation();
                dataPane.addComponent(bottomNavigation);
                dataPane.setComponentAlignment(bottomNavigation, Alignment.TOP_RIGHT);
            } else {
                dataPane.addComponent(new Label(getLocalizedMessage("activity.no-results")));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            dataPane.addComponent(new Label(getLocalizedMessage("processinstances.console.failed") + " " + e.getClass().getName()
                    + ": " + e.getMessage()));
        }

    }

    protected boolean getDataPaneUsesSpacing() {
        return true;
    }

    protected abstract List<ProcessInstance> getProcessInstances(String filterExpression, int offset, int limit);

    protected abstract Component getTaskItem(TasksMainPane.TaskTableItem tti);

    private Component getNavigation() {
        HorizontalLayout hl = new HorizontalLayout();

        hl.setSpacing(true);
        Button prevButton = new Button(getMessage("activity.tasks.previous"));
        prevButton.setStyleName(BaseTheme.BUTTON_LINK);
        prevButton.setEnabled(offset > 0);
        prevButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset -= limit;
                if (offset < 0) offset = 0;
                refreshData();
            }
        });
        hl.addComponent(prevButton);

        final int size = processInstances.size();
        hl.addComponent(new Label((offset + 1) + "-" + Math.min(offset + limit, offset + size)));

        Button nextButton = new Button(getMessage("activity.tasks.next"));
        nextButton.setStyleName(BaseTheme.BUTTON_LINK);
        nextButton.setEnabled(limit < size);
        nextButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset += limit;
//                if (offset > size - 1) offset = size - 1;
                refreshData();
            }
        });
        hl.addComponent(nextButton);

        return hl;
    }


    protected String getMessage(String title) {
        return activityMainPane.getI18NSource().getMessage(title);
    }

    private final Map<String, Resource> resourceCache = new HashMap<String, Resource>();

    protected void cacheResource(String path, Resource resource) {
        resourceCache.put(path, resource);
    }

    protected Resource getResource(String path) {
        return resourceCache.get(path);
    }

    protected Resource getImage(String path) {
        if (!resourceCache.containsKey(path)) {
            resourceCache.put(path, new ClassResource(getClass(), path, activityMainPane.getApplication()));
        }
        return resourceCache.get(path);
    }

	protected String buildTaskItemHeader(final ProcessInstance pi) {
		return getMessage(pi.getDefinition().getDescription()) + " " +
	            nvl(pi.getExternalKey(), pi.getInternalId()) + " " +
	            new SimpleDateFormat("yyyy-MM-dd").format(pi.getCreateDate());
	}
}
