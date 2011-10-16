package pl.net.bluesoft.rnd.processtool.ui.substitutions;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.liferay.LiferayBridge;
import pl.net.bluesoft.rnd.util.vaadin.TransactionProvider;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.util.vaadin.ui.LocalizedPagedTable;
import pl.net.bluesoft.util.lang.MapUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static pl.net.bluesoft.rnd.poutils.DateUtil.truncHours;
import static pl.net.bluesoft.rnd.poutils.cquery.CQuery.from;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.*;

/**
 * User: POlszewski
 * Date: 2011-09-09
 * Time: 13:05:13
 */
public class SubstitutionsMainPane extends VerticalLayout implements VaadinUtility.HasRefreshButton {
    private Application application;
    private I18NSource i18NSource;
    private TransactionProvider transactionProvider;

    private Window detailsWindow = null;

    private BeanItemContainer<UserSubstitution> container = new BeanItemContainer<UserSubstitution>(UserSubstitution.class);
    private BeanItemContainer<UserData> userDataContainer = new BeanItemContainer<UserData>(UserData.class);
    private Map<String, UserData> usersByLogin;

    public SubstitutionsMainPane(Application application, I18NSource i18NSource, TransactionProvider transactionProvider) {
        this.application = application;
        this.i18NSource = i18NSource;
        this.transactionProvider = transactionProvider;
        setWidth("100%");
        initWidget();
        loadData();
    }

    private void initWidget() {
        removeAllComponents();

        Label titleLabel = new Label(i18NSource.getMessage("user.substitutions.title"));
        titleLabel.addStyleName("h1 color processtool-title");
        titleLabel.setWidth("100%");

        Button addEntryButton = addIcon(application);
        addEntryButton.setCaption(i18NSource.getMessage("substitutions.substitution"));
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
        customColumns.put("user", createUserRealNameColumn());
        customColumns.put("userSubstitute", createUserRealNameColumn());
        customColumns.put("dateFrom", createDateColumn());
        customColumns.put("dateTo", createDateColumn());
        customColumns.put("delete", createDeleteColumn(container));

        String[] visibleColumns = new String[] {"user", "userSubstitute", "dateFrom", "dateTo", "delete"};
        String[] columnHeaders = new String[] {i18NSource.getMessage("substitutions.user"), i18NSource.getMessage("substitutions.user.substitute"),
                i18NSource.getMessage("substitutions.date.from"), i18NSource.getMessage("substitutions.date.to"),
                i18NSource.getMessage("pagedtable.delete")};

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
        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                container.addAll(ctx.getUserSubstitutionDAO().findAll());
                usersByLogin = MapUtil.collectionToMap(LiferayBridge.getAllUsers(session.getUser(ctx)), "login");
                userDataContainer.addAll(usersByLogin.values());
            }
        });
    }

    public void refreshData() {
        loadData();
    }

    private Component getRefreshButton() {
        Button button = refreshIcon(application);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                withErrorHandling(getApplication(), new Runnable() {
                    public void run() {
                        refreshData();
                    }
                });
            }
        });
        return button;
    }

    private Form createSubstitutionForm(final BeanItem<UserSubstitution> item, final boolean add) {
        UserSubstitution subst = item.getBean();
        if (subst.getUser() != null) {
            subst.setUser(usersByLogin.get(subst.getUser().getLogin()));
        }
        if (subst.getUserSubstitute() != null) {
            subst.setUserSubstitute(usersByLogin.get(subst.getUserSubstitute().getLogin()));
        }
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
                if ("user".equals(propertyId)) {
                    Select s = select("Substituted User", userDataContainer, "realName");
                    s.setRequired(true);
                    s.setRequiredError("Substituted User required");
                    return s;
                }
                if ("userSubstitute".equals(propertyId)) {
                    Select s = select("Substituting User", userDataContainer, "realName");
                    s.setRequired(true);
                    s.setRequiredError("Substituting User required");
                    return s;
                }
                if ("dateFrom".equals(propertyId)) {
                    return createDateField("Date From");
                }
                if ("dateTo".equals(propertyId)) {
                    return createDateField("Date To");
                }                
                return null;
            }
        });
        form.setItemDataSource(item);
        form.setVisibleItemProperties(new String[]{ "user", "userSubstitute", "dateFrom", "dateTo" });
        form.setValidationVisible(false);
        form.setValidationVisibleOnCommit(false);
        form.setImmediate(true);
        form.setWriteThrough(false);
        Button cancelButton = smallButton(i18NSource.getMessage("button.cancel"));
        cancelButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                form.discard();
                application.getMainWindow().removeWindow(detailsWindow);
                detailsWindow = null;
            }
        });
        Button saveButton = smallButton(i18NSource.getMessage("button.save"));
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
        detailsWindow = modalWindow(i18NSource.getMessage("dict.item"), panel);
    }

    private void saveSubstitution(final UserSubstitution item) {
        item.setDateFrom(truncHours(item.getDateFrom()));
        item.setDateTo(truncHours(item.getDateTo()));
        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                item.setUser(ctx.getUserDataDAO().loadOrCreateUserByLogin(item.getUser()));
                item.setUserSubstitute(ctx.getUserDataDAO().loadOrCreateUserByLogin(item.getUserSubstitute()));
                ctx.getUserSubstitutionDAO().saveOrUpdate(item);
            }
        });
    }

    private Table.ColumnGenerator createUserRealNameColumn() {
        return new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                Property prop = source.getItem(itemId).getItemProperty(columnId);
                if (prop.getType().equals(UserData.class)) {
                    Label l = new Label();        
                    l.setPropertyDataSource(new PropertyFormatter(prop) {
                        @Override
                        public String format(Object o) {
                            return ((UserData)o).getRealName();
                        }

                        @Override
                        public Object parse(String s) throws Exception {
                            throw new UnsupportedOperationException();
                        }
                    });
                    return l;
                }
                return null;
            }
        };
    }

    private Table.ColumnGenerator createDateColumn() {
        return new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                Property prop = source.getItem(itemId).getItemProperty(columnId);
                if (prop.getType().equals(Date.class)) {
                    DateField df = createDateField(null);
                    df.setReadOnly(true);
                    df.setPropertyDataSource(prop);
                    return df;
                }
                return null;                
            }
        };
    }

    private Table.ColumnGenerator createDeleteColumn(final BeanItemContainer container) {
        return new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                Button b = smallButton(i18NSource.getMessage("pagedtable.delete"));
                b.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        final UserSubstitution item = (UserSubstitution)container.getItem(itemId).getBean();
                        container.removeItem(itemId);
                        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
                            @Override
                            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                                ctx.getUserSubstitutionDAO().delete(item);
                            }
                        });
                    }
                });
                return b;
            }
        };
    }

    public Application getApplication() {
        return application;
    }

    private PopupDateField createDateField(String caption) {
        PopupDateField dateField = new PopupDateField(caption);
        dateField.setDateFormat("yyyy-MM-dd");
        dateField.setResolution(PopupDateField.RESOLUTION_DAY);
        dateField.setRequired(true);
        dateField.setRequiredError(caption + " required");
        dateField.setImmediate(true);
        return dateField;
    }
}
