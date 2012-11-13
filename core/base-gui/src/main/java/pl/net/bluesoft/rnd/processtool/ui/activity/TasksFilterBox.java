package pl.net.bluesoft.rnd.processtool.ui.activity;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.FilterChangedEvent;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TaskTableItem;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class TasksFilterBox extends VerticalLayout {
	private I18NSource i18NSource;
	private ProcessToolBpmSession session;
	private Application application;
	private TextField searchField;
	private String filterExpression;
	private Form advancedForm;
	private Button advancedTrigger;
	private ProcessInstanceFilter filter;
	private List<ItemSetChangeListener> listeners = new LinkedList<ItemSetChangeListener>();
	private ProcessListPane parent;
	private int limit;
	private TextField filterNameField;
	private Button filterNameSave;

	public TasksFilterBox(I18NSource i18NSource, ProcessToolBpmSession session, Application application, ProcessListPane parent) 
	{
		this.i18NSource = i18NSource;
		this.session = session;
		this.application = application;
		this.parent = parent;
		initUI();
	}

	private void initUI() {
		searchField = new TextField();
		searchField.setInputPrompt(getMessage("search.prompt"));
		searchField.setWidth("100%");
		searchField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
		searchField.setTextChangeTimeout(500);
		searchField.addListener(new FieldEvents.TextChangeListener() {
			@Override
			public void textChange(FieldEvents.TextChangeEvent event) {
				filterExpression = event.getText();
				parent.setNewSearch();
				refreshData();
			}
		});
		addComponent(searchField);

		advancedTrigger = new Button(getMessage("search.advanced.show"));
		advancedTrigger.setStyleName(BaseTheme.BUTTON_LINK);
		advancedTrigger.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				createAdvancedForm();
				filterNameField.setVisible(!advancedForm.isVisible());
				filterNameSave.setVisible(!advancedForm.isVisible());
				advancedForm.setVisible(!advancedForm.isVisible());
				advancedTrigger.setCaption(getMessage(advancedForm.isVisible() ? "search.advanced.hide" : "search.advanced.show"));
			}
		});
		addComponent(advancedTrigger);
	}

	private void createAdvancedForm() {
		if (advancedForm != null) {
			return;
		}

		GridLayout gl = new GridLayout();
		gl.setSpacing(true);
		gl.setColumns(4);

		advancedForm = new Form(gl);
		advancedForm.setFormFieldFactory(new TasksFilterFieldFactory(this));
		advancedForm.setVisible(false);
		advancedForm.getFooter().addComponent(new Button(getMessage("search.advanced.search"), new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				parent.setNewSearch();
				refreshData();
			}
		}));

		loadFormData();
		addComponent(advancedForm);

		filterNameField = new TextField(getMessage("search.advanced.filter.name"));
		filterNameField.setVisible(false);
		filterNameSave = new Button(getMessage("search.advanced.filter.save_as_new"), new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (StringUtils.isEmpty((String)filterNameField.getValue())) {
					VaadinUtility.validationNotification(application, i18NSource,
							getMessage("search.advanced.filter.name.required"));
				}
				else {
					saveFilter();
				}
			}
		});
		filterNameSave.setVisible(false);

		addComponent(filterNameField);
		addComponent(filterNameSave);
	}

	private void saveFilter() 
	{
		if(filter == null)
			return;
		
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		filter.setGenericQuery(filterExpression);
		filter.setName((String) filterNameField.getValue());
		filter.setFilterOwner(session.getUser(ctx));
		filter.setId(null);
		ctx.getProcessInstanceFilterDAO().saveProcessInstanceFilter(filter);
		parent.getActivityMainPane().getBpmSession().getEventBusManager().publish(new FilterChangedEvent());
	}

	public void refreshData() 
	{
		fireItemSetChanged();
	}

	public void addListener(ItemSetChangeListener listener) {
		listeners.add(listener);
	}

	private void fireItemSetChanged() {
		for (ItemSetChangeListener listener : listeners) {
			listener.itemSetChange();
		}
	}

	public ProcessInstanceFilter getFilter() {
		return filter;
	}

	public void setFilter(ProcessInstanceFilter filter) {
		this.filter = filter;
		loadFormData();
	}

	private void loadFormData() {
		if (advancedForm != null) {
			advancedForm.setItemDataSource(new BeanItem<ProcessInstanceFilter>(filter));
			advancedForm.setVisibleItemProperties(Arrays.asList("createdBefore", "createdAfter", "notUpdatedAfter", "updatedAfter", "creators", "owners", "queues"));
		}
	}

	public interface ItemSetChangeListener {
		void itemSetChange();
	}

	public String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	public ProcessToolBpmSession getSession() {
		return session;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
