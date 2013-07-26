package pl.net.bluesoft.rnd.processtool.ui.substitutions;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.DateUtil;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.processtool.model.UserSubstitution.*;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2011-09-09
 * Time: 13:05:13
 */
public class SubstitutionsMainPane extends VerticalLayout implements Refreshable {
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	private Application application;
    private I18NSource i18NSource;

    private Window detailsWindow;

    private BeanItemContainer<UserSubstitution> container = new BeanItemContainer<UserSubstitution>(UserSubstitution.class);
    private BeanContainer<String, UserData> userDataContainer = new BeanContainer<String, UserData>(UserData.class);

    public SubstitutionsMainPane(Application application, I18NSource i18NSource) {
        this.application = application;
        this.i18NSource = i18NSource;
        setWidth("100%");
        initWidget();
        loadData();
    }

    private void initWidget() {
        removeAllComponents();

        Label titleLabel = new Label(getMessage("user.substitutions.title"));
        titleLabel.addStyleName("h1 color processtool-title");
        titleLabel.setWidth("100%");

        Button addEntryButton = addIcon(application);
        addEntryButton.setCaption(getMessage("substitutions.substitution"));
        addEntryButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showItemDetails(null);
            }
        });

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setSpacing(true);

        headerLayout.addComponent(addEntryButton);

        addComponent(horizontalLayout(titleLabel, getRefreshButton()));
        addComponent(headerLayout);

        Map<String, Table.ColumnGenerator> customColumns = new HashMap<String, Table.ColumnGenerator>();
        customColumns.put(_USER_LOGIN, createUserRealNameColumn());
        customColumns.put(_USER_SUBSTITUTE_LOGIN, createUserRealNameColumn());
        customColumns.put(_DATE_FROM, createDateColumn());
        customColumns.put(_DATE_TO, createDateColumn());
        customColumns.put("delete", createDeleteColumn(container));

        String[] visibleColumns = {_USER_LOGIN, _USER_SUBSTITUTE_LOGIN, _DATE_FROM, _DATE_TO, "delete"};
        String[] columnHeaders = { getMessage("substitutions.user"), getMessage("substitutions.user.substitute"),
                getMessage("substitutions.date.from"), getMessage("substitutions.date.to"),
                getMessage("pagedtable.delete") };

        LocalizedPagedTable table = pagedTable(container, visibleColumns, columnHeaders, customColumns, new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                showItemDetails(container.getItem(event.getItemId()));
            }
        });

        Component c = wrapPagedTable(i18NSource, table);
        addComponent(c);
        setExpandRatio(c, 1);        
    }

    private void loadData() {
        container.removeAllItems();
        userDataContainer.removeAllItems();

		container.addAll(getThreadProcessToolContext().getUserSubstitutionDAO().findAll());

		List<UserData> allUsers = getRegistry().getUserSource().getAllUsers();

		for (UserData user : allUsers) {
			userDataContainer.addItem(user.getLogin(), user);
		}
		userDataContainer.sort(new String[]{ UserData._FILTERED_NAME }, new boolean[]{ true });
    }

    @Override
	public void refreshData() {
        loadData();
    }

    private Component getRefreshButton() {
        Button button = refreshIcon(application);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                withErrorHandling(getApplication(), new Runnable() {
                    @Override
					public void run() {
                        refreshData();
                    }
                });
            }
        });
        return button;
    }

    private Form createSubstitutionForm(final BeanItem<UserSubstitution> item, final boolean add) {
        final Form form = new Form() {
            @Override
            protected void attachField(Object propertyId, Field field) {
                if (field.getValue() == null && field.getType() == String.class) {
                    field.setValue("");
                }
                super.attachField(propertyId, field);
            }
        };
        form.setLocale(application.getLocale());
        form.setFormFieldFactory(new FormFieldFactory(){
            @Override
            public Field createField(Item item, Object propertyId, Component component) {
                if (_USER_LOGIN.equals(propertyId)) {
                    Select s = select(getMessage("substitutions.user"), userDataContainer, UserData._FILTERED_NAME);
                    s.setRequired(true);
                    s.setRequiredError("Substituted User required");
                    return s;
                }
                if (_USER_SUBSTITUTE_LOGIN.equals(propertyId)) {
                    Select s = select(getMessage("substitutions.user.substitute"), userDataContainer, UserData._FILTERED_NAME);
                    s.setRequired(true);
                    s.setRequiredError("Substituting User required");
					s.setWidth(250, UNITS_PIXELS);
                    return s;
                }
                if (_DATE_FROM.equals(propertyId)) {
                    DateField df = createDateField(getMessage("substitutions.date.from"));
                    df.setRequired(true);
                    df.setRequiredError(getMessage("substitutions.date.from.required"));
                    return df;
                }
                if (_DATE_TO.equals(propertyId)) {
                    DateField df = createDateField(getMessage("substitutions.date.to"));
                    df.setRequired(true);
                    df.setRequiredError(getMessage("substitutions.date.to.required"));
                    return df;
                }                
                return null;
            }
        });
        form.setItemDataSource(item);
        form.setVisibleItemProperties(new String[]{ _USER_LOGIN, _USER_SUBSTITUTE_LOGIN, _DATE_FROM, _DATE_TO });
        form.setValidationVisible(false);
        form.setValidationVisibleOnCommit(false);
        form.setImmediate(true);
        form.setWriteThrough(false);
        Button cancelButton = smallButton(getMessage("button.cancel"));
        cancelButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                form.discard();
                application.getMainWindow().removeWindow(detailsWindow);
                detailsWindow = null;
            }
        });
        Button saveButton = smallButton(getMessage("button.save"));
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Map<Field, String> messages = new LinkedHashMap<Field, String>();
                for (Object propertyId : form.getItemPropertyIds()) {
                    Field field = form.getField(propertyId);
                    try {
                        field.validate();
                    }
                    catch (Validator.InvalidValueException e) {
                        messages.put(field, e.getMessage());
                    }
                }
                if (messages.isEmpty()) {
                    form.commit();
                    saveSubstitution(item.getBean());
                    if (add) {
                        container.addBean(item.getBean());
                    }
                }
                else {
                    validationNotification(application, i18NSource, from(messages.values()).toString("<br/>"));
                }
                application.getMainWindow().removeWindow(detailsWindow);
                detailsWindow = null;
            }
        });
        form.setFooter(horizontalLayout(Alignment.MIDDLE_CENTER, cancelButton, saveButton));        
        return form;
    }
    
    private void showItemDetails(BeanItem<UserSubstitution> item) {
        if (detailsWindow != null) {
            return;
        }
        boolean isNew = item == null;
        if (item == null) {
            item = new BeanItem<UserSubstitution>(new UserSubstitution());
        }
        wrapWithModalWindow(createSubstitutionForm(item, isNew));
        application.getMainWindow().addWindow(detailsWindow);
    }

    private void wrapWithModalWindow(Form form) {
        Panel panel = new Panel();
        panel.setWidth("550px");
        panel.setScrollable(true);
        panel.addComponent(form);
        detailsWindow = modalWindow(getMessage("substitutions.Substitution"), panel);
    }

    private void saveSubstitution(UserSubstitution item) {
        item.setDateFrom(DateUtil.beginOfDay(item.getDateFrom()));
        item.setDateTo(DateUtil.endOfDay(item.getDateTo()));

		getThreadProcessToolContext().getUserSubstitutionDAO().saveOrUpdate(item);
    }

    private Table.ColumnGenerator createUserRealNameColumn() {
        return new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Property prop = source.getItem(itemId).getItemProperty(columnId);
                return prop.getValue();
            }
        };
    }

    private Table.ColumnGenerator createDateColumn() {
        return new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Property prop = source.getItem(itemId).getItemProperty(columnId);
                Date date = (Date)prop.getValue();
				return date != null ? new SimpleDateFormat(DATE_FORMAT).format(date) : "";
            }
        };
    }

    private Table.ColumnGenerator createDeleteColumn(final BeanItemContainer container) {
        return new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                Button b = smallButton(getMessage("pagedtable.delete"));
                b.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UserSubstitution item = (UserSubstitution)container.getItem(itemId).getBean();

						container.removeItem(itemId);

						getThreadProcessToolContext().getUserSubstitutionDAO().deleteById(item.getId());
                    }
                });
                return b;
            }
        };
    }

    @Override
	public Application getApplication() {
        return application;
    }

    private PopupDateField createDateField(String caption) {
        PopupDateField dateField = new PopupDateField(caption);
        dateField.setDateFormat(DATE_FORMAT);
        dateField.setResolution(PopupDateField.RESOLUTION_DAY);        
        dateField.setImmediate(true);
        return dateField;
    }

    private String getMessage(String key) {
        return i18NSource.getMessage(key);
    }
}
