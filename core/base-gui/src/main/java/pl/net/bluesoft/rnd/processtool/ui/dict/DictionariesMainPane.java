package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table.ColumnGenerator;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.TransactionProvider;
import pl.net.bluesoft.rnd.util.vaadin.ui.LocalizedPagedTable;
import pl.net.bluesoft.util.lang.StringUtil;

import java.util.*;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.horizontalLayout;

public class DictionariesMainPane extends VerticalLayout {
    private Application application;
    private I18NSource i18NSource;
    private TransactionProvider transactionProvider;

    private Component currentTableComponent = null;
    private HorizontalLayout headerLayout;
    private Select dictionarySelect;
    private Select processSelect;
    private Button addEntryButton;

    private Window detailsWindow = null;

    private BeanItemContainer<ProcessDefinitionConfig> processContainer;
    private Map<ProcessDBDictionary, BeanItemContainer> dictContainers;

    public DictionariesMainPane(Application application, I18NSource i18NSource, TransactionProvider transactionProvider) {
        this.application = application;
        this.i18NSource = i18NSource;
        this.transactionProvider = transactionProvider;
        setWidth("100%");
        initWidget();
        loadData();
    }

    private void initWidget() {
        removeAllComponents();
        dictContainers = new LinkedHashMap<ProcessDBDictionary, BeanItemContainer>();
        processContainer = new BeanItemContainer<ProcessDefinitionConfig>(ProcessDefinitionConfig.class);

        processSelect = select(i18NSource.getMessage("process.name"), processContainer, "processName");
        processSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                processConfigChanged((ProcessDefinitionConfig) processSelect.getValue());
            }
        });

        Label titleLabel = new Label(i18NSource.getMessage("dict.title"));
        titleLabel.addStyleName("h1 color processtool-title");
        titleLabel.setWidth("100%");

        addEntryButton = addIcon(application);
        addEntryButton.setCaption(i18NSource.getMessage("dict.addentry"));
        addEntryButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addItemDetails((ProcessDBDictionary) dictionarySelect.getValue());
            }
        });

        headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setSpacing(true);
        headerLayout.addComponent(processSelect);
        headerLayout.setComponentAlignment(processSelect, Alignment.MIDDLE_LEFT);

        addComponent(horizontalLayout(titleLabel, getRefreshButton()));
        addComponent(new Label(i18NSource.getMessage("dict.help.short")));
        addComponent(headerLayout);
    }

    private void loadData() {
        final Set<ProcessDefinitionConfig> processConfigs = new TreeSet<ProcessDefinitionConfig>(new Comparator<ProcessDefinitionConfig>() {
            @Override
            public int compare(ProcessDefinitionConfig o1, ProcessDefinitionConfig o2) {
                return o1.getProcessName().compareTo(o2.getProcessName());
            }
        });
        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                ProcessDictionaryProvider pdp = ctx.getProcessDictionaryRegistry().getDictionaryProvider("db");
                List<ProcessDBDictionary> dictionaries = pdp.fetchAllActiveDictionaries();
                for (ProcessDBDictionary dict : dictionaries) {
                    BeanItemContainer<ProcessDBDictionaryItem> container = new BeanItemContainer<ProcessDBDictionaryItem>(ProcessDBDictionaryItem.class);
                    List<ProcessDBDictionaryItem> items = new ArrayList<ProcessDBDictionaryItem>(dict.getItems().values());
                    Collections.sort(items, new Comparator<ProcessDBDictionaryItem>() {
                        @Override
                        public int compare(ProcessDBDictionaryItem o1, ProcessDBDictionaryItem o2) {
                            return o1.getKey().compareTo(o2.getKey());
                        }
                    });
                    container.addAll(items);
                    dictContainers.put(dict, container);
                    processConfigs.add(dict.getProcessDefinition());
                }
            }
        });
        processContainer.addAll(processConfigs);
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

    private void refreshData() {
        processSelect.setValue(null);
        processContainer.removeAllItems();
        dictContainers.clear();
        loadData();
    }

    private void processConfigChanged(ProcessDefinitionConfig value) {
        clearDictionarySelect();
        if (value != null) {
            List<ProcessDBDictionary> dictList = new ArrayList<ProcessDBDictionary>();
            for (ProcessDBDictionary dict : dictContainers.keySet()) {
                if (value.getId() == dict.getProcessDefinition().getId()) {
                    dictList.add(dict);
                }
            }
            if (dictList.isEmpty()) {
                informationNotification(application, i18NSource, i18NSource.getMessage("dict.dictsempty"));
            }
            else {
                Collections.sort(dictList, new Comparator<ProcessDBDictionary>() {
                    @Override
                    public int compare(ProcessDBDictionary o1, ProcessDBDictionary o2) {
                        return o1.getDictionaryName().compareTo(o2.getDictionaryName());
                    }
                });
                dictionarySelect = select(i18NSource.getMessage("dict.dictionaryName"),
                        new BeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class, dictList), "dictionaryName");
                dictionarySelect.addListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        addEntryButton.setVisible(true);
                        dictionarySelected((ProcessDBDictionary) dictionarySelect.getValue());
                    }
                });
                headerLayout.addComponent(dictionarySelect);
                headerLayout.addComponent(addEntryButton);
                headerLayout.setComponentAlignment(dictionarySelect, Alignment.MIDDLE_LEFT);
                headerLayout.setComponentAlignment(addEntryButton, Alignment.MIDDLE_LEFT);
                headerLayout.setExpandRatio(addEntryButton, 1.0F);
                addEntryButton.setVisible(false);
            }
        }
    }

    private void clearDictionarySelect() {
        if (dictionarySelect != null) {
            headerLayout.removeComponent(dictionarySelect);
            headerLayout.removeComponent(addEntryButton);
            dictionarySelect = null;
        }
        showCurrentTable(false);
    }

    private void showCurrentTable(boolean show) {
        if (currentTableComponent != null) {
            if (show) {
                if (getComponentIndex(currentTableComponent) == -1) {
                    addComponent(currentTableComponent);
                    setExpandRatio(currentTableComponent, 1.0F);
                }
            }
            else {
                removeComponent(currentTableComponent);
                currentTableComponent = null;
            }
        }
    }

    private void dictionarySelected(ProcessDBDictionary dict) {
        final BeanItemContainer container = dictContainers.get(dict);
        Map<String, ColumnGenerator> customColumns = new HashMap<String, ColumnGenerator>();
        customColumns.put("delete", createDeleteColumn(container));
        customColumns.put("extensions", createAdditionalValuesColumn(container));

        String[] visibleColumns = new String[] {"key", "value", "description", "extensions", "delete"};
        String[] columnHeaders = new String[] {i18NSource.getMessage("dict.item.key"), i18NSource.getMessage("dict.item.value"),
                i18NSource.getMessage("dict.item.description"), i18NSource.getMessage("dict.item.extensions"),
                i18NSource.getMessage("pagedtable.delete")};

        LocalizedPagedTable table = pagedTable(container, visibleColumns, columnHeaders, customColumns, new ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                showItemDetails(container.getItem(event.getItemId()));
            }
        });
        showCurrentTable(false);
        currentTableComponent = wrapPagedTable(i18NSource, table);
        showCurrentTable(true);
    }

    private void addItemDetails(final ProcessDBDictionary dictionary) {
        if (detailsWindow != null) {
            return;
        }

        final BeanItem<ProcessDBDictionaryItem> item = new BeanItem<ProcessDBDictionaryItem>(new ProcessDBDictionaryItem());

        Button saveButton = smallButton(i18NSource.getMessage("button.save"));

        final Form form = createDictEntryForm(item, new DictionaryItemFormFieldFactory(application, i18NSource,
                new Object[] {"key", "value", "description", "extensions"},
                new Object[] {"key", "value", "description", "extensions"},
                new Object[] {"key", "value"}),
                saveButton);

        wrapWithModalWindow(form);

        saveButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Map<Field, String> messages = new LinkedHashMap<Field, String>();
                for (Object propertyId : form.getItemPropertyIds()) {
                    Field field = form.getField(propertyId);
                    try {
                        field.validate();
                    }
                    catch (InvalidValueException e) {
                        messages.put(field, e.getMessage());
                    }
                }
                if (messages.isEmpty()) {
                    form.commit();
                    ProcessDBDictionaryItem bean = item.getBean();
                    ProcessDictionaryItem lookedUpItem = dictionary.lookup(bean.getKey());
                    if (lookedUpItem != null) {
                        validationNotification(application, i18NSource, i18NSource.getMessage("validate.dictentry.exists"));
                    }
                    else {
                        bean.setDictionary(dictionary);
                        dictionary.addItem(bean);
                        dictContainers.get(dictionary).addBean(bean);
                        saveDictionaryItem(item.getBean());
                        application.getMainWindow().removeWindow(detailsWindow);
                        detailsWindow = null;
                    }
                }
                else {
                    StringBuilder sb = new StringBuilder();
                    for (Field field : messages.keySet()) {
                        sb.append(messages.get(field)).append("<br/>");
                    }
                    validationNotification(application, i18NSource, sb.toString());
                }
            }
        });

        application.getMainWindow().addWindow(detailsWindow);
    }

    private Form createDictEntryForm(BeanItem<ProcessDBDictionaryItem> item, DictionaryItemFormFieldFactory fieldFactory, Button saveButton) {
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
        form.setFormFieldFactory(fieldFactory);
        form.setItemDataSource(item);
        form.setVisibleItemProperties(fieldFactory.getVisiblePropertyIds());
        form.setValidationVisible(false);
        form.setValidationVisibleOnCommit(false);
        form.setImmediate(true);
        form.setWriteThrough(false);
        Button cancelButton = smallButton(i18NSource.getMessage("button.cancel"));
        cancelButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.discard();
                application.getMainWindow().removeWindow(detailsWindow);
                detailsWindow = null;
            }
        });

        form.setFooter(horizontalLayout(Alignment.MIDDLE_CENTER, cancelButton, saveButton));
        return form;
    }

    private void showItemDetails(final BeanItem<ProcessDBDictionaryItem> item) {
        if (detailsWindow != null) {
            return;
        }

        Button saveButton = smallButton(i18NSource.getMessage("button.save"));

        final Form form = createDictEntryForm(item, new DictionaryItemFormFieldFactory(application, i18NSource,
                new Object[] {"key", "value", "description", "extensions"},
                new Object[] {"value", "description", "extensions"},
                new Object[] {"value"}),
                saveButton);

        wrapWithModalWindow(form);

        saveButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Map<Field, String> messages = new LinkedHashMap<Field, String>();
                for (Object propertyId : form.getItemPropertyIds()) {
                    Field field = form.getField(propertyId);
                    try {
                        field.validate();
                    }
                    catch (InvalidValueException e) {
                        messages.put(field, e.getMessage());
                    }
                }
                if (messages.isEmpty()) {
                    form.commit();
                    saveDictionaryItem(item.getBean());
                    application.getMainWindow().removeWindow(detailsWindow);
                    detailsWindow = null;
                }
                else {
                    StringBuilder sb = new StringBuilder();
                    for (Field field : messages.keySet()) {
                        sb.append(messages.get(field)).append("<br/>");
                    }
                    validationNotification(application, i18NSource, sb.toString());
                }
            }
        });

        application.getMainWindow().addWindow(detailsWindow);
    }

    private void wrapWithModalWindow(Form form) {
        Panel panel = new Panel();
        panel.setWidth("550px");
        panel.setScrollable(true);
        panel.addComponent(form);
        detailsWindow = modalWindow(i18NSource.getMessage("dict.item"), panel);
    }

    private void saveDictionaryItem(final ProcessDBDictionaryItem item) {
        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                ctx.getProcessDictionaryDAO().updateDictionary(item.getDictionary());
            }
        });
    }

    private ColumnGenerator createAdditionalValuesColumn(final BeanItemContainer container) {
        return new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                final Label info = new Label("", Label.CONTENT_XHTML);
                info.setWidth(300, UNITS_PIXELS);
                PopupView popup = new PopupView(i18NSource.getMessage("dict.show"), info);
                popup.addListener(new PopupVisibilityListener() {
                    @Override
                    public void popupVisibilityChange(PopupVisibilityEvent event) {
                        if (event.isPopupVisible()) {
                            ProcessDBDictionaryItem item = (ProcessDBDictionaryItem) container.getItem(itemId).getBean();
                            List<String> extensionNames = new ArrayList<String>(item.getExtensionNames());
                            StringBuilder sb = new StringBuilder().append("<b>");
                            if (extensionNames.isEmpty()) {
                                sb.append(i18NSource.getMessage("dict.item.noextensions"));
                            }
                            else {
                                sb.append("<ul>");
                                Collections.sort(extensionNames);
                                for (String extensionName : extensionNames) {
                                    ProcessDBDictionaryItemExtension ext = item.getExtensionByName(extensionName);
                                    sb.append("<li>").append(ext.getName() + (StringUtil.hasText(item.getDescription())
                                            ? "(" + item.getDescription() + ")" : "") + ": " + ext.getValue()).append("</li>");
                                }
                                sb.append("</ul>");
                            }
                            sb.append("</b>");
                            info.setValue(sb.toString());
                        }
                    }
                });
                popup.setHideOnMouseOut(true);
                popup.addStyleName("bubble");
                return popup;
            }
        };
    }

    private ColumnGenerator createDeleteColumn(final BeanItemContainer container) {
        return new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                Button b = smallButton(i18NSource.getMessage("pagedtable.delete"));
                b.addListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        ProcessDBDictionaryItem item = (ProcessDBDictionaryItem) container.getItem(itemId).getBean();
                        final ProcessDBDictionary dictionary = item.getDictionary();
                        dictionary.removeItem(item.getKey());
                        item.setDictionary(null);
                        container.removeItem(itemId);
                        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
                            @Override
                            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                                ctx.getProcessDictionaryDAO().updateDictionary(dictionary);
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
}


