package pl.net.bluesoft.rnd.processtool.ui.tasks;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Table.ColumnGenerator;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.ui.newprocess.NewProcessPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.VaadinUtility.HasRefreshButton;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TasksMainPane extends VerticalLayout implements HasRefreshButton {

    private I18NSource i18NSource;
    private TextField searchField = new TextField();

    private ProcessToolBpmSession session;
    private EventListener<BpmEvent> bpmEventSubScriber;
    private BeanItemContainer<TaskTableItem> bic;
    private Application application;
    private String filterExpression;

    public TasksMainPane(I18NSource i18NSource, ProcessToolBpmSession session, Application application) {
        this.i18NSource = i18NSource;
        this.session = session;
        this.application = application;
        initUI();
    }

    public TasksMainPane(ProcessToolBpmSession session) {
        this.session = session;
        initUI();
    }

    private void initUI() {
        addComponent(new Label(getMessage("tasks.help.short")));
        searchField.setWidth("100%");
        searchField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
        searchField.setTextChangeTimeout(500);
        searchField.setInputPrompt(i18NSource.getMessage("search.prompt"));
        searchField.addListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                filterExpression = event.getText();
                refreshData();
            }
        });
        Component rb = refreshIcon(application, this);
        HorizontalLayout hl = horizontalLayout(searchField, rb);
        hl.setMargin(new MarginInfo(false, false, true, false));
        addComponent(hl);

        Table table = getUserTasksTable();
        addComponent(table);
        addComponent(new NewProcessPane(session, i18NSource, null));

    }

    public static class TaskTableItem {
        private String definitionName, internalId, state;
        private ProcessInstance processInstance;
        private ProcessStateConfiguration stateConfiguration;

        public TaskTableItem(String definitionName, String internalId, String state, ProcessInstance processInstance,
                             ProcessStateConfiguration stateConfiguration) {
            this.definitionName = definitionName;
            this.internalId = internalId;
            this.state = state;
            this.processInstance = processInstance;
            this.stateConfiguration = stateConfiguration;
        }

        public TaskTableItem() {
        }

        public String getDefinitionName() {
            return definitionName;
        }

        public void setDefinitionName(String definitionName) {
            this.definitionName = definitionName;
        }

        public String getInternalId() {
            return internalId;
        }

        public void setInternalId(String internalId) {
            this.internalId = internalId;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public ProcessInstance getProcessInstance() {
            return processInstance;
        }

        public void setProcessInstance(ProcessInstance processInstance) {
            this.processInstance = processInstance;
        }

        public ProcessStateConfiguration getStateConfiguration() {
            return stateConfiguration;
        }

        public void setStateConfiguration(ProcessStateConfiguration stateConfiguration) {
            this.stateConfiguration = stateConfiguration;
        }

        public boolean matchSearchCriteria(String expression) {
            String[] fields = new String[] {
                    state,
                    internalId,
                    definitionName,
                    processInstance.getDescription(),
                    processInstance.getKeyword(),
                    stateConfiguration != null ? stateConfiguration.getCommentary() : null
            };
            for (String f : fields) {
                if (f != null && f.toUpperCase().contains(expression.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }
    }

    private Table getUserTasksTable() {
        final Table table = new Table();
        table.setWidth("100%");
        table.setHeight("200px");

        table.setImmediate(true); // react at once when something is selected
        table.setSelectable(true);

        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        bic = new BeanItemContainer<TaskTableItem>(TaskTableItem.class);
        fillTaskList(ctx, bic);
        table.setContainerDataSource(bic);
        table.addGeneratedColumn("definitionName", new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, Object itemId, Object columnId) {
                TaskTableItem tti = bic.getItem(itemId).getBean();
                String definitionName = getMessage(tti.getDefinitionName());
                return new Label(definitionName, Label.CONTENT_XHTML);
            }
        });
        table.addGeneratedColumn("state", new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, Object itemId, Object columnId) {
                TaskTableItem tti = bic.getItem(itemId).getBean();
                String state = getMessage(tti.getState());
                return new Label(state, Label.CONTENT_XHTML);
            }
        });
        table.setVisibleColumns(new Object[] {"definitionName", "internalId", "state"});

        for (Object o : table.getVisibleColumns()) {
            table.setColumnHeader(o, getMessage("tasks." + o));
        }

        table.addListener(
                new ItemClickEvent.ItemClickListener() {
                    public void itemClick(final ItemClickEvent event) {
                        if (event.isDoubleClick()) {
                            withErrorHandling(getApplication(), new Runnable() {
                                public void run() {
                                    BeanItem<TaskTableItem> instanceBeanItem = bic.getItem(event.getItemId());
                                    Window w = new Window(instanceBeanItem.getBean().internalId);
                                    w.setContent(new ProcessDataPane(getApplication(), session, i18NSource,
                                            instanceBeanItem.getBean().processInstance,
                                            new ProcessDataPane.WindowDisplayProcessContextImpl(w)));
                                    w.center();
                                    getWindow().addWindow(w);
                                    w.focus();
                                }
                            });
                        }
                    }
                });
        if (bpmEventSubScriber == null) {
            session.getEventBusManager().subscribe(BpmEvent.class, bpmEventSubScriber = new EventListener<BpmEvent>() {
                @Override
                public void onEvent(BpmEvent e) {
                    refreshData();
                }
            });
        }

        return table;
    }

    public void refreshData() {
        bic.removeAllItems();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        fillTaskList(ctx, bic);
    }

    private void fillTaskList(ProcessToolContext ctx, BeanItemContainer<TaskTableItem> bic) {
        List<ProcessInstance> userProcesses = new ArrayList(session.getUserProcesses(0, 1000, ctx));
        Collections.sort(userProcesses, new Comparator<ProcessInstance>() {
            @Override
            public int compare(ProcessInstance o1, ProcessInstance o2) {
                return -1 * new Long(o1.getId()).compareTo(o2.getId());
            }
        });
        for (ProcessInstance pi : userProcesses) {
            TaskTableItem tti = new TaskTableItem(pi.getDefinition().getDescription(), pi.getInternalId(), pi.getState(), pi, null);
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
                bic.addBean(tti);
            }
        }
    }

    private String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    public I18NSource getI18NSource() {
        return i18NSource;
    }

    public void setI18NSource(I18NSource i18NSource) {
        this.i18NSource = i18NSource;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public void setSearchField(TextField searchField) {
        this.searchField = searchField;
    }
}
