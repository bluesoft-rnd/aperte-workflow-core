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
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.HasRefreshButton;

import java.util.*;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.horizontalLayout;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * Created by IntelliJ IDEA.
 * User: tomek
 * Date: 4/29/11
 * Time: 8:43 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProcessListPane extends VerticalLayout implements HasRefreshButton {
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
                refreshData();
            }
        });
    }

    protected ProcessToolBpmSession getBpmSession() {
        return activityMainPane.getBpmSession();
    }

    public void refreshData() {
        dataPane.setSpacing(getDataPaneUsesSpacing());
        dataPane.setMargin(true);
        dataPane.setWidth("100%");
        dataPane.removeAllComponents();

        List<ProcessInstance> userProcesses = getProcessInstances();
        Collections.sort(userProcesses, new Comparator<ProcessInstance>() {
            @Override
            public int compare(ProcessInstance o1, ProcessInstance o2) {
                return -1 * new Long(o1.getId()).compareTo(o2.getId());
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

            if (!hasText(filterExpression) || tti.matchSearchCriteria(filterExpression)) {
                filteredProcesses.add(tti);
            }
        }
        processInstances = filteredProcesses;
        if (offset > processInstances.size()) {
            offset = processInstances.size() - processInstances.size() % limit;
        }
        if (offset < 0) offset = 0;

        Component topNavigation = getNavigation();
        dataPane.addComponent(topNavigation);
        dataPane.setComponentAlignment(topNavigation, Alignment.TOP_RIGHT);

        for (int i = offset; i < Math.min(offset + limit, processInstances.size()); i++) {
            TasksMainPane.TaskTableItem tti = processInstances.get(i);
            dataPane.addComponent(getTaskItem(tti));
        }

        Component bottomNavigation = getNavigation();
        dataPane.addComponent(bottomNavigation);
        dataPane.setComponentAlignment(bottomNavigation, Alignment.TOP_RIGHT);

    }

    protected boolean getDataPaneUsesSpacing() {
        return true;
    }

    protected abstract List<ProcessInstance> getProcessInstances();

    protected abstract Component getTaskItem(TasksMainPane.TaskTableItem tti);

//    private Component getNavigation() {
//        HorizontalLayout hl = new HorizontalLayout();
//        hl.setWidth("100%");
//        hl.setSpacing(true);
//
//        Button firstButton = createPagingButton("/img/left.png", GoToPage.FIRST);
//        hl.addComponent(firstButton);
//        hl.setComponentAlignment(firstButton, Alignment.MIDDLE_LEFT);
//
//        HorizontalLayout hl2 = new HorizontalLayout();
//        hl2.setSpacing(true);
//
//        Button prevButton = createPagingButton("/img/left-large.png", GoToPage.PREVIOUS);
//        hl2.addComponent(prevButton);
//        hl2.setComponentAlignment(prevButton, Alignment.MIDDLE_LEFT);
//
//        Label rangeLabel = new Label((offset + 1) + "-" + Math.min(offset + limit, processInstances.size()) + " z " + processInstances.size());
//        rangeLabel.styled("tti-range-label");
//        hl2.addComponent(rangeLabel);
//        hl2.setComponentAlignment(rangeLabel, Alignment.MIDDLE_LEFT);
//
//        Button nextButton = createPagingButton("/img/right-large.png", GoToPage.NEXT);
//        hl2.addComponent(nextButton);
//        hl2.setComponentAlignment(nextButton, Alignment.MIDDLE_LEFT);
//
//        Button lastButton = createPagingButton("/img/right.png", GoToPage.LAST);
//        hl2.addComponent(lastButton);
//        hl2.setComponentAlignment(lastButton, Alignment.MIDDLE_LEFT);
//
//        hl.addComponent(hl2);
//        hl.setComponentAlignment(hl2, Alignment.MIDDLE_RIGHT);
//
//        return hl;
//    }
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

        hl.addComponent(new Label((offset + 1) + "-" + Math.min(offset + limit, processInstances.size())
                + " " + getMessage("activity.tasks.of") + " " + processInstances.size()));
        Button nextButton = new Button(getMessage("activity.tasks.next"));
        nextButton.setStyleName(BaseTheme.BUTTON_LINK);
        nextButton.setEnabled(offset + limit < processInstances.size());
        nextButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset += limit;
                if (offset > processInstances.size() - 1) offset = processInstances.size() - 1;
                refreshData();
            }
        });
        hl.addComponent(nextButton);

        return hl;
    }

    private Button createPagingButton(String icon, final GoToPage goTo) {
        Button button = new Button();
        button.setIcon(getImage(icon));
        button.setStyleName(BaseTheme.BUTTON_LINK);
        switch (goTo) {
            case NEXT:
            case LAST:
                button.setEnabled(offset + limit < processInstances.size());
                break;
            case PREVIOUS:
            case FIRST:
                button.setEnabled(offset > 0);
                break;
        }
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                goToPage(goTo);
            }
        });
        return button;
    }

    private enum GoToPage {
        NEXT,
        PREVIOUS,
        FIRST,
        LAST,
    }
    private void goToPage(GoToPage goTo) {
        switch (goTo) {
            case NEXT:
                offset += limit;
                break;
            case PREVIOUS:
                offset -= limit;
                break;
            case FIRST:
                offset = 0;
                break;
            case LAST:
                offset = (processInstances.size()/limit)*limit - 1;
                break;
        }
        if (offset < 0) offset = 0;
        if (offset > processInstances.size() - 1) offset = processInstances.size() - 1;
        refreshData();
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
}
