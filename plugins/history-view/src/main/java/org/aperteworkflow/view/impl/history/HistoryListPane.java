package org.aperteworkflow.view.impl.history;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import org.aperteworkflow.ui.view.ViewCallback;
import org.aperteworkflow.ui.view.ViewRenderer;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.text.TextValueChangeListener;
import org.aperteworkflow.util.view.AbstractListPane;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.OrderedLayoutFactory;
import org.aperteworkflow.view.impl.history.DateRangeField.DateRange;
import org.aperteworkflow.view.impl.history.DateRangeField.DateRangeChangedEvent;
import org.aperteworkflow.view.impl.history.DateRangeField.DateRangeListener;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable.PageChangeListener;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable.PagedTableChangeEvent;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Formats;
import pl.net.bluesoft.util.lang.Strings;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;


/**
 * @author amichalak@bluesoft.net.pl
 * @author tlipski@bluesoft.net.pl
 */
public class HistoryListPane extends AbstractListPane implements DateRangeListener, ViewRenderer {
    private static final String HISTORY_SUPERUSER_ROLE_NAME = "AWF_HISTORY_SUPERUSER";

    @Override
    public String getViewId() {
        return HistoryListPane.class.getName();
    }

    @Override
    public Component render(Map<String, ?> viewData) {
        return this;
    }

    @Override
    public void setViewCallback(ViewCallback viewCallback) {
        this.viewCallback = viewCallback;
    }

    private static final String HISTORY_SUPERUSER_CONFIG_KEY = "history.superuser.roles";

    private static final String[] allPropertyNames = new String[] {
            "log.entryDate", "def.desc", "pi.externalKey", "pi.internalId", "state.desc", "log.eventName", "log.actionName", "state.name",
            "log.author", "log.substitutedBy"
    };

    private static final String[] processDetailsPropertyNames = new String[] {
            "def.desc", "def.cmnt", "pi.externalKey", "pi.internalId", "pi.status", "pi.createDate", "pi.creator", "pi.currentTasks"
    };

    private static final String[] taskDetailsPropertyNames = new String[] {
            "log.entryDate", "state.desc", "log.eventName", "log.actionName", "log.author", "log.substitutedBy"
    };

    private ViewCallback viewCallback;

    private TextField searchField;
    private String filterExpression = "";

    private ProcessToolBpmSession bpmSession;

    private DirectIndexedContainer logContainer;
    private IndexedContainer processDetailsContainer;
    private IndexedContainer taskDetailsContainer;

    private VerticalLayout detailsView;
    private LocalizedPagedTable table;

    private DateRangeField dateRangeField;
    private HistorySelection historySelection;

    private boolean isHistorySuperuser = false;
    private boolean isBaseUserOnly = true;

    private int taskCount = 0;

    private class DirectIndexedContainer extends IndexedContainer {
        public Item getItemDirectly(Object itemId) {
            return getUnfilteredItem(itemId);
        }

        public Collection<?> getItemIdsDirectly() {
            return Collections.unmodifiableCollection(getAllItemIds());
        }
    }

    public void setUp(Application app) {
        super.setUp(app, I18NSource.ThreadUtil.getThreadI18nSource(), "activity.task.history");
        application = app;
        messageSource = I18NSource.ThreadUtil.getThreadI18nSource();
        init();
    }

    @Override
    public Object getViewData() {
        return getData();
    }

    @Override
    public String getTitle() {
        return I18NSource.ThreadUtil.getLocalizedMessage("activity.task.history");
    }

    @Override
    public void handleDisplayAction() {
        if (application instanceof GenericVaadinPortlet2BpmApplication)
      			((GenericVaadinPortlet2BpmApplication)application).setShowExitWarning(false);
        VaadinUtility.unregisterClosingWarning(application.getMainWindow());
        setData(Collections.singletonMap("historySelection", new HistorySelection()));
    }

    @Override
    public void setBpmSession(ProcessToolBpmSession bpmSession) {
        this.bpmSession = bpmSession;
    }

    public HistorySelection getHistorySelection() {
        return historySelection != null ? historySelection : (historySelection = new HistorySelection());
    }

    public void setHistorySelection(HistorySelection historySelection) {
        this.historySelection = historySelection;
    }

    @Override
    public HistoryListPane init() {
        super.init();
        processDetailsContainer = createProcessDetailsContainer();
        taskDetailsContainer = createTaskDetailsContainer();

        setupHistorySuperuserFlag();

        dateRangeField = new DateRangeField(messageSource);
        dateRangeField.setOptionGroupLayoutFactory(new OrderedLayoutFactory<VerticalLayout>() {
            @Override
            public VerticalLayout create() {
                return new VerticalLayout();
            }
        });
        dateRangeField.setDateStepperBaseValue(0);
        dateRangeField.setDateStepperMaxValue(31);

        VerticalLayout marginPanel = new VerticalLayout();
        marginPanel.addComponent(new Label(getMessagePrefixed("help.short"), Label.CONTENT_XHTML));
        marginPanel.setMargin(true);
        marginPanel.setWidth("100%");
        marginPanel.addComponent(createSearchBox());

        if (isHistorySuperuser) {
            final CheckBox cb = VaadinUtility.checkBox(null);
            cb.setWidth("12px");
            cb.setValue(isBaseUserOnly = getHistorySelection().isOnlyBaseUser());
            cb.addListener(new ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    isBaseUserOnly = cb.booleanValue();
                    filterResults();
                }
            });
            Label l = new Label(getMessagePrefixed("base.user.only"), Label.CONTENT_XHTML);
            HorizontalLayout hl = VaadinUtility.horizontalLayout(Alignment.MIDDLE_LEFT, cb, l);
            hl.setMargin(false, true, false, false);
            hl.addListener(new LayoutClickListener() {
                @Override
                public void layoutClick(LayoutClickEvent event) {
                    cb.setValue(!cb.booleanValue());
                }
            });
            marginPanel.addComponent(hl);
        }

        table = VaadinUtility.pagedTable(logContainer = createLogContainer(), allPropertyNames, createColumnHeaders(allPropertyNames), null, new ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                showDetailedView((ProcessInstanceLog) event.getItemId());
            }
        });
        table.setPageLength(getHistorySelection().getPageLength());
        table.addListener(new PageChangeListener() {
            @Override
            public void pageChanged(PagedTableChangeEvent event) {
                getHistorySelection().setPageLength(table.getPageLength());
            }
        });
        table.setMultiSelect(false);
        table.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                showDetailedView((ProcessInstanceLog) event.getProperty().getValue());
            }
        });
        table.setSortAscending(false);

        VerticalLayout tableLayout = VaadinUtility.verticalLayout(dateRangeField, VaadinUtility.hr(), createTableControls(), table, createTableControls());
        tableLayout.setMargin(true, false, false, false);

        marginPanel.addComponent(tableLayout);

        addComponent(marginPanel);

        String historyFilterExpression = getHistorySelection().getFilterExpression();
        if (getHistorySelection().getDateRange() != null) {
            dateRangeField.setValue(getHistorySelection().getDateRange());
        }
        refreshData();

        ProcessInstanceLog selectedItem = getHistorySelection().getSelectedItemId();
        if (selectedItem != null) {
            ProcessInstanceLog log = null;
            for (Object itemId : logContainer.getItemIdsDirectly()) {
                log = (ProcessInstanceLog) itemId;
                if (log.getId().equals(selectedItem.getId())) {
                    break;
                }
            }
            getHistorySelection().setSelectedItemId(log);
            table.setValue(log);
        }

        searchField.setValue(Strings.hasText(historyFilterExpression) ? historyFilterExpression : "");
        filterResults();

        dateRangeField.addListener(this);

        return this;
    }

    private void setupHistorySuperuserFlag() {
        isHistorySuperuser = false;
        if (bpmSession != null) {
            UserData user = (UserData) application.getUser();
            if (user.containsRole(HISTORY_SUPERUSER_ROLE_NAME)) {
                isHistorySuperuser = true;
            }
            else {
                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                String config = ctx.getSetting(HISTORY_SUPERUSER_CONFIG_KEY);
                if (Strings.hasText(config)) {
                    for (String roleName : config.split(",")) {
                        if (user.containsRole(roleName)) {
                            isHistorySuperuser = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private HorizontalLayout createTableControls() {
        return table.createControls(messageSource);
    }

    private DirectIndexedContainer createLogContainer() {
        DirectIndexedContainer container = new DirectIndexedContainer();
        for (String propertyName : allPropertyNames) {
            container.addContainerProperty(propertyName, String.class, "");
        }
        return container;
    }

    private IndexedContainer createProcessDetailsContainer() {
        return createSimpleContainer("title", processDetailsPropertyNames);
    }

    private IndexedContainer createTaskDetailsContainer() {
        return createSimpleContainer("title", taskDetailsPropertyNames);
    }

    private IndexedContainer createSimpleContainer(String propertyId, String[] propertyNames) {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(propertyId, String.class, "");
        for (String propertyName : propertyNames) {
            Item item = container.addItem(propertyName);
            item.getItemProperty(propertyId).setValue(getMessagePrefixed(propertyName));
        }
        return container;
    }

    private String[] createColumnHeaders(String[] propertyNames) {
        String[] headers = new String[propertyNames.length];
        for (int i = 0; i < propertyNames.length; ++i) {
            headers[i] = getMessagePrefixed(propertyNames[i]);
        }
        return headers;
    }

    private HorizontalLayout createSearchBox() {
        final TextField field = new TextField();
        field.addStyleName("search");
        field.setInputPrompt(getMessage("search.prompt"));
        field.setWidth("100%");
        field.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
        field.setTextChangeTimeout(2000);

        TextValueChangeListener listener = new TextValueChangeListener() {
            @Override
            public void handleTextChange(String changedText) {
                filterExpression = Strings.hasText(changedText) ? changedText : "";
                filterResults();
            }
        };
        field.addListener((ValueChangeListener) listener);
        field.addListener((TextChangeListener) listener);
        searchField = field;

        Button searchButton = VaadinUtility.button(getMessage("search.advanced.search"), null, "default small", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                String changedText = (String) field.getValue();
                filterExpression = Strings.hasText(changedText) ? changedText : "";
                filterResults();
            }
        });
        searchButton.setClickShortcut(KeyCode.ENTER);

        return VaadinUtility.horizontalLayout(field, searchButton);
    }

    private void filterResults() {
        logContainer.removeAllContainerFilters();
        if (Strings.hasText(filterExpression)) {
            List<Filter> filters = new ArrayList<Filter>() {{
                for (Object propertyId : logContainer.getContainerPropertyIds()) {
                    add(new SimpleStringFilter(propertyId, filterExpression, true, false));
                }
            }};
            logContainer.addContainerFilter(new Or(filters.toArray(new Filter[filters.size()])));
        }
        if (isHistorySuperuser && isBaseUserOnly) {
            UserData user = (UserData) application.getUser();
            logContainer.addContainerFilter(new Or(
                    new SimpleStringFilter("log.author", user.getRealName(), false, true),
                    new SimpleStringFilter("log.substitutedBy", user.getRealName(), false, true)));
        }
        getHistorySelection().setOnlyBaseUser(isBaseUserOnly);
        getHistorySelection().setFilterExpression(filterExpression);
        updatePageControls();
    }

    @Override
    public void onEvent(DateRangeChangedEvent dateRangeChangedEvent) {
        getHistorySelection().setDateRange((DateRange) dateRangeField.getValue());
        refreshData();
    }

    @Override
    public void refreshData() {
        logContainer.removeAllItems();
        searchField.setValue("");
        table.setValue(null);
        if (bpmSession != null) {
            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            UserData user = isHistorySuperuser ? null : bpmSession.getUser(ctx);
            Collection<ProcessInstanceLog> logs = ctx.getProcessInstanceDAO().getUserHistory(user,
                    dateRangeField.getStartDate(), dateRangeField.getEndDate());
            DateFormat dateFormat = VaadinUtility.fullDateFormat();
            for (ProcessInstanceLog log : logs) {
                Item item = logContainer.addItem(log);
                item.getItemProperty("log.entryDate").setValue(dateFormat.format(log.getEntryDate().getTime()));
                item.getItemProperty("log.eventName").setValue(getMessage(log.getEventI18NKey()));
                item.getItemProperty("log.actionName").setValue(getMessage(log.getAdditionalInfo()));

                ProcessInstance pi = log.getProcessInstance();
                if (pi != null) {
                    item.getItemProperty("def.desc").setValue(getMessage(pi.getDefinition().getDescription()));
                    item.getItemProperty("pi.externalKey").setValue(getExternalKey(pi));
                    item.getItemProperty("pi.internalId").setValue(pi.getInternalId());
                }

                ProcessStateConfiguration state = log.getState();
                if (state != null) {
                    item.getItemProperty("state.desc").setValue(getMessage(state.getDescription()));
                    item.getItemProperty("state.name").setValue(getMessage(state.getName()));
                }

                String authorName = log.getUser().getRealName();
                item.getItemProperty("log.author").setValue(authorName);

                UserData userSubstitute = log.getUserSubstitute();
                String userSubstituteName = userSubstitute != null ? userSubstitute.getRealName() : "";
                item.getItemProperty("log.substitutedBy").setValue(userSubstituteName);
            }
            logContainer.sort(new Object[] {"log.entryDate"}, new boolean[] {false});
            table.sort();
        }
        updatePageControls();
    }

    private void updatePageControls() {
        table.setCurrentPage(0);
        table.firePagedChangedEvent();
    }

    private void showDetailedView(final ProcessInstanceLog log) {
        hideDetailedView();
        getHistorySelection().setSelectedItemId(log);
        if (log == null) {
            return;
        }

        detailsView = new VerticalLayout();
        detailsView.setWidth("100%");
        detailsView.setMargin(false, true, false, true);
        detailsView.setSpacing(true);
        detailsView.addComponent(VaadinUtility.hr());
        addComponent(detailsView);

        Table taskTable = attachTitledTable(detailsView, "log.details", taskDetailsContainer, new Object[] {"title", "component"},
                new HashMap<String, ColumnGenerator>() {{
                    put("component", new ColumnGenerator() {
                        @Override
                        public Object generateCell(Table source, Object itemId, Object columnId) {
                            Item item = logContainer.getItemDirectly(log);
                            Property property = item.getItemProperty(itemId);
                            if (property != null && property.getValue() != null) {
                                String value = (String) property.getValue();
                                if (Strings.hasText(value) && ("log.substitutedBy".equals(itemId) || "log.author".equals(itemId))) {
                                    return VaadinUtility.labelWithIcon(getImage("/img/user_standard.png"), value, null, getMessagePrefixed(itemId.toString()));
                                }
                                return VaadinUtility.boldLabel(value);
                            }
                            return null;
                        }
                    });
                }});
        // being stupid is its own reward
        taskTable.setHeight(22 * (taskDetailsPropertyNames.length - 2) + 50, Sizeable.UNITS_PIXELS);

        Collection<Button> taskButtons = createTaskButtons(log);
        HorizontalLayout buttons = VaadinUtility.horizontalLayout(Alignment.MIDDLE_LEFT, taskButtons.toArray(new Component[taskButtons.size()]));
        detailsView.addComponent(buttons);

        Table processTable = attachTitledTable(detailsView, "pi.details", processDetailsContainer, new Object[] {"title", "component"},
                new HashMap<String, ColumnGenerator>() {{
                    put("component", new ColumnGenerator() {
                        private DateFormat dateFormat = VaadinUtility.simpleDateFormat();

                        @Override
                        public Object generateCell(Table source, Object itemId, Object columnId) {
                            ProcessInstance pi = log.getProcessInstance();
                            ProcessDefinitionConfig def = pi.getDefinition();
                            if ("def.desc".equals(itemId)) {
                                return VaadinUtility.boldLabel(getMessage(Formats.nvl(def.getDescription())));
                            }
                            else if ("def.cmnt".equals(itemId)) {
                                return VaadinUtility.boldLabel(getMessage(Formats.nvl(def.getComment())));
                            }
                            else if ("pi.externalKey".equals(itemId)) {
                                return VaadinUtility.boldLabel(getMessage(Formats.nvl(getExternalKey(pi))));
                            }
                            else if ("pi.internalId".equals(itemId)) {
                                return VaadinUtility.boldLabel(getMessage(pi.getInternalId()));
                            }
                            else if ("pi.status".equals(itemId)) {
                                return VaadinUtility.boldLabel(getProcessStatus(pi));
                            }
                            else if ("pi.createDate".equals(itemId)) {
                                return VaadinUtility.boldLabel(dateFormat.format(pi.getCreateDate()));
                            }
                            else if ("pi.creator".equals(itemId)) {
                                return VaadinUtility.boldLabel(pi.getCreator() != null ? pi.getCreator().getRealName() : "");
                            }
                            else if ("pi.currentTasks".equals(itemId)) {
                                return getProcessCurrentTasksView(pi);
                            }
                            return null;
                        }
                    });
                }});

        // i say no to drugs, but they don’t listen
        processTable.setHeight(22 * (processDetailsPropertyNames.length - 1) + Math.max(taskCount, 1) * 35, Sizeable.UNITS_PIXELS);
        detailsView.setExpandRatio(processTable, 1.0f);
    }

    private Collection<Button> createTaskButtons(final ProcessInstanceLog log) {
        Item item = logContainer.getItemDirectly(log);
        Property property = item.getItemProperty("state.desc");
        String stateDescription = (String) property.getValue();
        boolean hasState = Strings.hasText(stateDescription);
        boolean isFinished = ProcessStatus.FINISHED.equals(log.getProcessInstance().getStatus())
                || ProcessStatus.CANCELLED.equals(log.getProcessInstance().getStatus());

        final Button showStateButton = createOpenInstanceButton(log, getMessagePrefixed("open.current.step"),
                getMessagePrefixed("current.not.found", stateDescription), false, hasState, getMessagePrefixed("no.state"));

        final Button finalStateButton = createOpenInstanceButton(log, getMessagePrefixed("open.final.step"),
                getMessagePrefixed("final.not.found"), true, isFinished, getMessagePrefixed("not.finished"));

        return new ArrayList<Button>() {{
            add(showStateButton);
            add(finalStateButton);
        }};
    }

    private Button createOpenInstanceButton(final ProcessInstanceLog log, String caption, final String notFoundMessage,
                                            final boolean finalTask, boolean enabled, String notEnabledDescription) {
        Button b = VaadinUtility.button(caption, null, "default", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                openHistoryInstance(log, notFoundMessage, finalTask);
            }
        });
        b.setWidth(b.getWidth() + 20, Sizeable.UNITS_PIXELS);
        b.setIcon(VaadinUtility.imageResource(application, finalTask ? "green-flag.png" : "task.png"));
        b.setEnabled(enabled);
        b.setDescription(enabled ? caption : notEnabledDescription);
        return b;
    }

    private void openHistoryInstance(ProcessInstanceLog log, String notFoundMessage, boolean finalTask) {
        getHistorySelection().update(table.getPageLength(), filterExpression, isBaseUserOnly, (DateRange) dateRangeField.getValue(), log);
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessInstance ownPi = null;
        if(log.getOwnProcessInstance() != null)
        	ownPi = ctx.getProcessInstanceDAO().getProcessInstance(log.getOwnProcessInstance().getId());
        else 
        	ownPi = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(log.getExecutionId());
        log.setOwnProcessInstance(ownPi);
        BpmTask task = finalTask ? bpmSession.getPastEndTask(log, ctx) : bpmSession.getPastOrActualTask(log, ctx);
        if (task != null) {
            viewCallback.displayProcessData(task, true);
        }
        else {
            VaadinUtility.validationNotification(application, messageSource, notFoundMessage);
        }
    }

    private Table attachTitledTable(VerticalLayout layout, String title, Container dataSource,
                                    Object[] visiblePropertyIds, Map<String, ColumnGenerator> customColumns) {
        Label titleLabel = new Label(getMessagePrefixed(title), Label.CONTENT_XHTML) {{
            addStyleName("h1 color processtool-title");
        }};

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.addComponent(titleLabel);
        hl.setExpandRatio(titleLabel, 1.0f);
        layout.addComponent(hl);

        Table table = VaadinUtility.simpleTable(dataSource, visiblePropertyIds, customColumns);

        layout.addComponent(table);

        return table;
    }

    private String getProcessStatus(ProcessInstance pi) {
        ProcessStatus status = pi.getStatus() != null ? pi.getStatus() : ProcessStatus.UNKNOWN;
        return getMessage("process.instance.status." + status.name().toLowerCase());
    }

    private Component getProcessCurrentTasksView(ProcessInstance pi) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        List<BpmTask> tasks = bpmSession.findProcessTasks(pi, ctx);
        taskCount = tasks.size();
        if (tasks.isEmpty()) {
            return VaadinUtility.boldLabel(getMessagePrefixed("pi.currentTasks.empty"));
        }
        VerticalLayout vl = new VerticalLayout();
        vl.setWidth("100%");
        vl.setSpacing(false);
        vl.setMargin(false);
        for (BpmTask task : tasks) {
            ProcessStateConfiguration state = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task);
            vl.addComponent(createTaskComponent(task, state));
        }
        return vl;
    }

    private Component createTaskComponent(BpmTask task, ProcessStateConfiguration state) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setMargin(false);

        hl.addComponent(VaadinUtility.boldLabel(getMessage(state.getDescription())));

        String assignedName = "<b>" + (task.getOwner() != null ? task.getOwner().getRealName() : getMessage("activity.assigned.empty")) + "</b>";
        Component assignedComponent = VaadinUtility.labelWithIcon(getImage("/img/user_assigned.png"), assignedName, null, getMessage("activity.assigned"));
        hl.addComponent(assignedComponent);

        Iterator<Component> it = hl.getComponentIterator();
        while (it.hasNext()) {
            Component next = it.next();
            hl.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
        }

        return hl;
    }

    private void hideDetailedView() {
        if (detailsView != null) {
            detailsView.removeAllComponents();
            removeComponent(detailsView);
        }
        detailsView = null;
    }

    public String getMessagePrefixed(String key) {
        return messageSource.getMessage("activity.task.history." + key);
    }

    public String getMessagePrefixed(String key, String... args) {
        return MessageFormat.format(messageSource.getMessage("activity.task.history." + key), args);
    }

    public static class HistorySelection {
        private String filterExpression;
        private int pageLength;
        private DateRange dateRange;
        private ProcessInstanceLog selectedItemId;
        private boolean onlyBaseUser;

        public HistorySelection() {
            this.pageLength = 25;
            this.filterExpression = "";
            this.onlyBaseUser = true;
        }

        public DateRange getDateRange() {
            return dateRange;
        }

        public void setDateRange(DateRange dateRange) {
            this.dateRange = dateRange;
        }

        public ProcessInstanceLog getSelectedItemId() {
            return selectedItemId;
        }

        public void setSelectedItemId(ProcessInstanceLog selectedItemId) {
            this.selectedItemId = selectedItemId;
        }

        public String getFilterExpression() {
            return filterExpression;
        }

        public void setFilterExpression(String filterExpression) {
            this.filterExpression = filterExpression;
        }

        public int getPageLength() {
            return pageLength;
        }

        public void setPageLength(int pageLength) {
            this.pageLength = pageLength;
        }

        public boolean isOnlyBaseUser() {
            return onlyBaseUser;
        }

        public void setOnlyBaseUser(boolean onlyBaseUser) {
            this.onlyBaseUser = onlyBaseUser;
        }

        public void update(int pageLength, String filterExpression, boolean onlyBaseUser, DateRange dateRange, ProcessInstanceLog selectedItemId) {
            setDateRange(dateRange);
            setFilterExpression(filterExpression);
            setOnlyBaseUser(onlyBaseUser);
            setPageLength(pageLength);
            setSelectedItemId(selectedItemId);
        }
    }

	private static String getExternalKey(ProcessInstance pi) {
		do {
			if (pi.getExternalKey() != null) {
				return pi.getExternalKey();
			}
			pi = pi.getParent();
		} while (pi != null);
		return null;
	}
}
