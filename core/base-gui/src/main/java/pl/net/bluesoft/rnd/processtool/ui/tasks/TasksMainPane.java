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
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.newprocess.NewProcessPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.processtool.ui.process.WindowProcessDataDisplayContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.io.Serializable;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.Strings.hasText;

public class TasksMainPane extends VerticalLayout implements Refreshable {
    private I18NSource i18NSource;
    private TextField searchField = new TextField();

    private ProcessToolBpmSession session;
    private EventListener<BpmEvent> eventListener;
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

    private Table getUserTasksTable() {
        final Table table = new Table();
        table.setWidth("100%");
        table.setHeight("200px");
        table.setImmediate(true); // react at once when something is selected
        table.setSelectable(true);

        bic = new BeanItemContainer<TaskTableItem>(TaskTableItem.class);
        fillTaskList(bic);
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
        table.setVisibleColumns(new Object[] {"definitionName", "internalId", "externalId", "state"});

        for (Object o : table.getVisibleColumns()) {
            table.setColumnHeader(o, getMessage("tasks." + o));
        }

        table.addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(final ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    withErrorHandling(getApplication(), new Runnable() {
                        public void run() {
                            BeanItem<TaskTableItem> beanItem = bic.getItem(event.getItemId());
                            TaskTableItem tti = beanItem.getBean();
                            Window w = new Window(tti.getInternalId());
                            w.setContent(new ProcessDataPane(getApplication(), session, i18NSource, tti.getTask(), new WindowProcessDataDisplayContext(w)));
                            w.center();
                            getWindow().addWindow(w);
                            w.focus();
                        }
                    });
                }
            }
        });

        if (eventListener == null) {
            session.getEventBusManager().subscribe(BpmEvent.class, eventListener = new MyEventListener());
        }

        return table;
    }

    public void refreshData() {
        bic.removeAllItems();
        fillTaskList(bic);
    }

    private void fillTaskList(BeanItemContainer<TaskTableItem> bic) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        List<BpmTask> tasks = session.findUserTasks(0, 1000, ctx);
        for (BpmTask task : tasks) {
            TaskTableItem tti = new TaskTableItem(task);
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

	private class MyEventListener implements EventListener<BpmEvent>, Serializable {
		@Override
		public void onEvent(BpmEvent e) {
			if (TasksMainPane.this.isVisible() && TasksMainPane.this.getApplication() != null) {
				refreshData();
			}
		}
	}
}
