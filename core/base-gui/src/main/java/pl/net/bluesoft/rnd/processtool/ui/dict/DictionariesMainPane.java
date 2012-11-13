package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;

import java.text.DateFormat;
import java.util.*;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;

public class DictionariesMainPane extends VerticalLayout implements ProcessToolBpmConstants, Refreshable {
    private GenericVaadinPortlet2BpmApplication application;
    private I18NSource i18NSource;
    private TransactionProvider transactionProvider;

    private TabSheet tabSheet;

    private Select globalDictionarySelect;
    private Select processDefinitionSelect;

    private Window detailsWindow = null;

    private HorizontalLayout processHeaderLayout;
    private VerticalLayout processTableLayout;

    private BeanItemContainer<ProcessDefinitionConfig> processContainer;
    private BeanItemContainer<ProcessDBDictionary> globalDictionaryContainer;
    private Map<ProcessDBDictionary, BeanItemContainer<ProcessDBDictionaryItem>> dictItemContainers;
    private Map<ProcessDefinitionConfig, Map<String, Set<ProcessDBDictionary>>> processDictionariesMap;
    private Map<String, Set<ProcessDBDictionary>> globalDictionariesMap;

    private static final String EMPTY_VALID_DATE = "...";

    public DictionariesMainPane(GenericVaadinPortlet2BpmApplication application, I18NSource i18NSource, TransactionProvider transactionProvider) {
        this.application = application;
        this.i18NSource = i18NSource;
        this.transactionProvider = transactionProvider;
        setWidth("100%");
        initWidget();
        loadData();
    }

    private void initWidget() {
        removeAllComponents();

        dictItemContainers = new LinkedHashMap<ProcessDBDictionary, BeanItemContainer<ProcessDBDictionaryItem>>();
        processContainer = new BeanItemContainer<ProcessDefinitionConfig>(ProcessDefinitionConfig.class);
        globalDictionaryContainer = new BeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class);
        globalDictionariesMap = new TreeMap<String, Set<ProcessDBDictionary>>();
        processDictionariesMap = new TreeMap<ProcessDefinitionConfig, Map<String, Set<ProcessDBDictionary>>>(new Comparator<ProcessDefinitionConfig>() {
            @Override
            public int compare(ProcessDefinitionConfig o1, ProcessDefinitionConfig o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        Label titleLabel = new Label(getMessage("dict.title"));
        titleLabel.addStyleName("h1 color processtool-title");
        titleLabel.setWidth("100%");

        processDefinitionSelect = select(getMessage("process.name"), processContainer, "description");
        processDefinitionSelect.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);
        processDefinitionSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                processConfigSelected((ProcessDefinitionConfig) processDefinitionSelect.getValue());
            }
        });

        HorizontalLayout globalHeaderLayout = fullHorizontalLayout();
        VerticalLayout globalTableLayout = verticalLayout();

        globalDictionarySelect = createDictionaryNameSelect(globalTableLayout, globalHeaderLayout, globalDictionaryContainer, globalDictionariesMap, null);

        VerticalLayout processDictLayout = verticalLayout(processHeaderLayout = fullHorizontalLayout(), processTableLayout = verticalLayout());
        VerticalLayout globalDictLayout = verticalLayout(globalHeaderLayout, globalTableLayout);

        reloadLayoutsWithComponents(processTableLayout, processHeaderLayout, processDefinitionSelect);
        reloadLayoutsWithComponents(globalTableLayout, globalHeaderLayout, globalDictionarySelect);

        tabSheet = new TabSheet();
        tabSheet.setWidth("100%");
        tabSheet.addTab(processDictLayout, getMessage("dict.title.process"), VaadinUtility.imageResource(application, "dict.png"));
        tabSheet.addTab(globalDictLayout, getMessage("dict.title.global"), VaadinUtility.imageResource(application, "globe.png"));

        addComponent(horizontalLayout(titleLabel, VaadinUtility.refreshIcon(application, this)));
        addComponent(new Label(getMessage("dict.help.short")));
        addComponent(tabSheet);
    }

    private void loadData() {
        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                Collection<ProcessDefinitionConfig> configs = ctx.getProcessDefinitionDAO().getActiveConfigurations();
                //LOL - it overwrites descriptions in database with language of the first user who logs in :)
//                for (ProcessDefinitionConfig config : configs) {
//                    config.setDescription(i18NSource.getMessage(config.getDescription()));
//                }
                processContainer.addAll(configs);
                for (ProcessDefinitionConfig config : processContainer.getItemIds()) {
                	processDefinitionSelect.setItemCaption(config, i18NSource.getMessage(config.getDescription()));
              	}
                ProcessDictionaryProvider pdp = ctx.getProcessDictionaryRegistry().getProcessDictionaryProvider("db");
                List<ProcessDBDictionary> dictionaries = pdp.fetchAllActiveProcessDictionaries();
                for (ProcessDBDictionary dict : dictionaries) {
                    if (hasPermissionsForDictionary(dict)) {
                        Map<String, Set<ProcessDBDictionary>> dictionariesMap = processDictionariesMap.get(dict.getProcessDefinition());
                        if (dictionariesMap == null) {
                            dictionariesMap = new TreeMap<String, Set<ProcessDBDictionary>>();
                            processDictionariesMap.put(dict.getProcessDefinition(), dictionariesMap);
                        }
                        groupDictionariesByLocale(dict, dictionariesMap);
                        bindBeanItemContainer(dict);
                    }
                }

                GlobalDictionaryProvider gdp = ctx.getProcessDictionaryRegistry().getGlobalDictionaryProvider("db");
                dictionaries = gdp.fetchAllGlobalDictionaries();
                for (ProcessDBDictionary dict : dictionaries) {
                    if (hasPermissionsForDictionary(dict)) {
                        groupDictionariesByLocale(dict, globalDictionariesMap);
                        bindBeanItemContainer(dict);
                    }
                }
                prepareDistinctDictionaryNameContainer(globalDictionaryContainer, globalDictionariesMap);
            }
        });
    }

    private void groupDictionariesByLocale(ProcessDBDictionary dict, Map<String, Set<ProcessDBDictionary>> dictionariesMap) {
        String dictId = dict.getDictionaryId();
        if (!Strings.hasText(dictId)) {
            throw new IllegalArgumentException("Dictionary id cannot be null");
        }
        Set<ProcessDBDictionary> localeSet = dictionariesMap.get(dictId);
        if (localeSet == null) {
            localeSet = new TreeSet<ProcessDBDictionary>(new Comparator<ProcessDBDictionary>() {
                @Override
                public int compare(ProcessDBDictionary o1, ProcessDBDictionary o2) {
                    return o1.getLanguageCode().compareTo(o2.getLanguageCode());
                }
            });
            dictionariesMap.put(dictId, localeSet);
        }
        localeSet.add(dict);
    }

    private void bindBeanItemContainer(ProcessDBDictionary dict) {
        BeanItemContainer<ProcessDBDictionaryItem> container = new BeanItemContainer<ProcessDBDictionaryItem>(ProcessDBDictionaryItem.class);
        List<ProcessDBDictionaryItem> items = new ArrayList<ProcessDBDictionaryItem>(dict.getItems().values());
        container.addAll(items);
        container.sort(new Object[] {"key"}, new boolean[] {false});
        dictItemContainers.put(dict, container);
    }

    private BeanItemContainer<ProcessDBDictionaryItem> getDictionaryItems(ProcessDBDictionary dict) {
        return dictItemContainers.get(dict);
    }

    public void refreshData() {
        processDefinitionSelect.setValue(null);
        globalDictionarySelect.setValue(null);
        processContainer.removeAllItems();
        globalDictionaryContainer.removeAllItems();
        dictItemContainers.clear();
        processDictionariesMap.clear();
        globalDictionariesMap.clear();
        loadData();
    }

    private Select createDictionaryNameSelect(final AbstractOrderedLayout tableLayout, final AbstractOrderedLayout headerLayout,
                                              BeanItemContainer<ProcessDBDictionary> dictionaryContainer,
                                              final Map<String, Set<ProcessDBDictionary>> dictionariesMap,
                                              final Component headerComponent) {
        final Select dictionaryNameSelect = select(getMessage("dict.dictionaryName"), dictionaryContainer, "dictionaryName");
        dictionaryNameSelect.setWidth("300px");
        dictionaryNameSelect.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                ProcessDBDictionary dict = (ProcessDBDictionary) dictionaryNameSelect.getValue();
                if (dict != null) {
                    Button addButton = addIcon(application);
                    addButton.setCaption(getMessage("dict.addentry"));
                    Component localeSelect = dictionaryNameSelected(addButton, tableLayout, dictionariesMap.get(dict.getDictionaryId()));
                    reloadLayoutsWithComponents(tableLayout, headerLayout, headerComponent, dictionaryNameSelect, localeSelect, addButton);
                }
                else {
                    reloadLayoutsWithComponents(tableLayout, headerLayout, headerComponent, dictionaryNameSelect);
                }

            }
        });
        return dictionaryNameSelect;
    }

    private void showItemTable(AbstractOrderedLayout tableLayout, Component tableComponent) {
        tableLayout.removeAllComponents();
        if (tableComponent != null) {
            tableLayout.addComponent(tableComponent);
            tableLayout.setExpandRatio(tableComponent, 1.0F);
        }
    }

    private void reloadLayoutsWithComponents(AbstractOrderedLayout tableLayout, AbstractOrderedLayout headerLayout, Component... components) {
        tableLayout.removeAllComponents();
        headerLayout.removeAllComponents();
        if (components != null && components.length > 0) {
            for (Component comp : components) {
                if (comp != null) {
                    headerLayout.addComponent(comp);
                    headerLayout.setComponentAlignment(comp, Alignment.MIDDLE_LEFT);
                }
            }
            headerLayout.setExpandRatio(headerLayout.getComponent(headerLayout.getComponentCount() - 1), 1.0F);
        }
    }

    private void processConfigSelected(ProcessDefinitionConfig value) {
        Select processDictionarySelect = null;
        if (value != null) {
            final Map<String, Set<ProcessDBDictionary>> localizedDictionariesMap = processDictionariesMap.get(value);
            if (localizedDictionariesMap == null || localizedDictionariesMap.isEmpty()) {
                informationNotification(application, getMessage("dict.dictsempty"));
            }
            else {
                BeanItemContainer<ProcessDBDictionary> distinctProcessDictionaryContainer =
                        new BeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class);
                prepareDistinctDictionaryNameContainer(distinctProcessDictionaryContainer, localizedDictionariesMap);
                processDictionarySelect = createDictionaryNameSelect(processTableLayout, processHeaderLayout,
                        distinctProcessDictionaryContainer, localizedDictionariesMap, processDefinitionSelect);
            }
        }
        reloadLayoutsWithComponents(processTableLayout, processHeaderLayout, processDefinitionSelect, processDictionarySelect);
    }

    private void prepareDistinctDictionaryNameContainer(BeanItemContainer<ProcessDBDictionary> distinctNameContainer,
            Map<String, Set<ProcessDBDictionary>> localizedDictionariesMap) {
        Locale locale = i18NSource.getLocale();
		for (Set<ProcessDBDictionary> dictSet : localizedDictionariesMap.values()) {
            if (dictSet != null && !dictSet.isEmpty()) {
                boolean addedEntry = false;
                for (ProcessDBDictionary dict : dictSet) {
                    if (locale.toString().equals(dict.getLanguageCode())) {
                        distinctNameContainer.addBean(dict);
                        addedEntry = true;
                        break;
                    }
                }
                if (!addedEntry) {
                    distinctNameContainer.addBean(dictSet.iterator().next());
                }
            }
        }
    }

    private Component dictionaryNameSelected(final Button addButton, final AbstractOrderedLayout tableLayout, Set<ProcessDBDictionary> dictSet) {
        final Select localeSelect = select(getMessage("dict.locale"),
                new BeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class, dictSet), "languageCode");

        addButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                showItemDetails(new BeanItem<ProcessDBDictionaryItem>(new ProcessDBDictionaryItem()), new SaveCallback() {
                    @Override
                    public void onSave(BeanItem<ProcessDBDictionaryItem> item) {
                        prepareAndSaveNewItem(item, (ProcessDBDictionary) localeSelect.getValue());
                    }
                });
            }
        });

        localeSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                addButton.setVisible(true);
                Component processTableComponent = localeSelected((ProcessDBDictionary) localeSelect.getValue());
                showItemTable(tableLayout, processTableComponent);
            }
        });
        addButton.setVisible(false);
        return localeSelect;
    }

    private Component localeSelected(final ProcessDBDictionary dict) {
        final BeanItemContainer container = getDictionaryItems(dict);
        Map<String, ColumnGenerator> customColumns = new HashMap<String, ColumnGenerator>();
        customColumns.put("value", createValueColumn(container));
        customColumns.put("extensions", createAdditionalValuesColumn(container));
        customColumns.put("delete", createDeleteColumn(container));

        String[] visibleColumns = new String[] {"key", "description", "value", "extensions", "delete"};
        String[] columnHeaders = new String[] {getMessage("dict.item.key"), getMessage("dict.item.description"),
                getMessage("dict.item.values"), getMessage("dict.item.extensions"),
                getMessage("pagedtable.delete")};

        LocalizedPagedTable table = pagedTable(container, visibleColumns, columnHeaders, customColumns, new ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                showItemDetails(container.getItem(event.getItemId()), new SaveCallback() {
                    @Override
                    public void onSave(BeanItem<ProcessDBDictionaryItem> item) {
                        saveDictionaryItem(item.getBean());
                    }
                });
            }
        });
        return wrapPagedTable(i18NSource, table);
    }

    public String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    public String getMessage(String key, String defaultValue) {
        return i18NSource.getMessage(key, defaultValue);
    }

    private interface SaveCallback {
        void onSave(BeanItem<ProcessDBDictionaryItem> item);
    }

    private void prepareAndSaveNewItem(BeanItem<ProcessDBDictionaryItem> item, ProcessDBDictionary dictionary) {
        ProcessDBDictionaryItem bean = item.getBean();
        ProcessDictionaryItem lookedUpItem = dictionary.lookup(bean.getKey());
        if (lookedUpItem != null) {
            validationNotification(application, i18NSource, getMessage("validate.dictentry.exists"));
        }
        else {
            bean.setDictionary(dictionary);
            dictionary.addItem(bean);
            dictItemContainers.get(dictionary).addBean(bean);
            saveDictionaryItem(bean);
        }
    }

    private void showItemDetails(final BeanItem<ProcessDBDictionaryItem> item, final SaveCallback callback) {
        if (detailsWindow != null) {
            return;
        }

        final DictionaryItemForm form = new DictionaryItemForm(application, i18NSource, item);
        form.setWidth("100%");

        form.addSaveClickListener(new ClickListener() {
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
                    callback.onSave(item);
                }
                else {
                    StringBuilder sb = new StringBuilder();
					for (String msg : messages.values()) {
                        sb.append(msg).append("<br/>");
                    }
                    validationNotification(application, i18NSource, sb.toString());
                }
            }
        });

        form.addCancelClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.discard();
                application.getMainWindow().removeWindow(detailsWindow);
                detailsWindow = null;
            }
        });

        wrapWithModalWindow(form);
        application.getMainWindow().addWindow(detailsWindow);
    }

    private void wrapWithModalWindow(Form form) {
        Panel panel = new Panel();
        panel.setWidth("800px");
        panel.setScrollable(true);
        panel.addComponent(form);
        detailsWindow = modalWindow(getMessage("dict.item"), panel);
    }

    private void saveDictionaryItem(final ProcessDBDictionaryItem item) {
        if (item.getValues() == null) {
            item.setValues(new HashSet<ProcessDBDictionaryItemValue>());
        }
        else {
            for (ProcessDBDictionaryItemValue itemValue : item.getValues()) {
                itemValue.setItem(item);
                for (ProcessDBDictionaryItemExtension ext : itemValue.getExtensions().values()) {
                    ext.setItemValue(itemValue);
                }
            }
        }
        transactionProvider.withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                ctx.getProcessDictionaryDAO().updateDictionary(item.getDictionary());
            }
        });
        application.getMainWindow().removeWindow(detailsWindow);
        detailsWindow = null;
    }

    private ColumnGenerator createValueColumn(final BeanItemContainer container) {
        return new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                return new DictPopupView(getMessage("dict.showValues"), new DictPopupVisibilityListener(container, itemId) {
                    @Override
                    public String getEmptyDescription() {
                        return getMessage("dict.item.novalues");
                    }
                    @Override
                    public String getItemRepresentation(ProcessDBDictionaryItemValue item) {
                        DateFormat dateFormat = VaadinUtility.simpleDateFormat();
                        StringBuilder sb = new StringBuilder().append("<b>").append(item.getValue()).append("</b>").append(" (").append("<i>");
                        if (item.hasFullDatesRange()) {
                            sb.append(getMessage("dict.full.range"));
                        }
                        else {
                            sb.append(item.getValidStartDate() != null ? dateFormat.format(item.getValidStartDate()) : EMPTY_VALID_DATE)
                                    .append(" - ")
                                    .append(item.getValidEndDate() != null ? dateFormat.format(item.getValidEndDate()) : EMPTY_VALID_DATE);
                        }
                        sb.append("</i>)");
                        return sb.toString();
                    }
                });
            }
        };
    }

    private ColumnGenerator createAdditionalValuesColumn(final BeanItemContainer container) {
        return new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                return new DictPopupView(getMessage("dict.showExtensions"), new DictPopupVisibilityListener(container, itemId) {
                    @Override
                    public String getEmptyDescription() {
                        return getMessage("dict.item.noextensions");
                    }

                    @Override
                    public String getItemRepresentation(ProcessDBDictionaryItemValue item) {
                        StringBuilder sb = new StringBuilder().append("<b>").append(item.getValue()).append("</b>").append("<ul>");
                        List<String> extensionNames = new ArrayList<String>(item.getExtensionNames());
                        if (extensionNames.isEmpty()) {
                            sb.append("<li>").append(getMessage("dict.item.noextensions")).append("</li>");
                        }
                        else {
                            java.util.Collections.sort(extensionNames);
                            for (String extensionName : extensionNames) {
                                ProcessDBDictionaryItemExtension ext = item.getExtensionByName(extensionName);
                                sb.append("<li>")
                                        .append("<b>").append(ext.getName()).append("</b>")
                                        .append(Strings.hasText(ext.getDescription())
                                                ? " (" + ext.getDescription() + ")" : "")
                                        .append(": ")
                                        .append(Strings.hasText(ext.getValue()) ? "<b>" + ext.getValue() + "</b>"
                                                : getMessage("dict.item.extensions.novalue"))
                                        .append("</li>");
                            }
                        }
                        sb.append("</ul>");
                        return sb.toString();
                    }
                });
            }
        };
    }

    private class DictPopupView extends PopupView {
        private Label info;

        public DictPopupView(final String smallTitle, DictPopupVisibilityListener listener) {
            super(smallTitle, null);
            this.info = new Label("", Label.CONTENT_XHTML);
            this.info.setWidth(400, UNITS_PIXELS);
            setContent(new Content() {
                @Override
                public String getMinimizedValueAsHTML() {
                    return smallTitle;
                }

                @Override
                public Component getPopupComponent() {
                    return info;
                }
            });
            listener.setLargeView(info);
            addListener(listener);
            setHideOnMouseOut(true);
            addStyleName("bubble");
        }
    }

    private abstract class DictPopupVisibilityListener implements PopupVisibilityListener {
        private final BeanItemContainer container;
        private final Object itemId;
        private Label largeView;

        private DictPopupVisibilityListener(BeanItemContainer container, Object itemId) {
            this.container = container;
            this.itemId = itemId;
        }

        public void setLargeView(Label largeView) {
            this.largeView = largeView;
        }

        public abstract String getEmptyDescription();

        public abstract String getItemRepresentation(ProcessDBDictionaryItemValue item);

        @Override
        public void popupVisibilityChange(PopupVisibilityEvent event) {
            if (event.isPopupVisible()) {
                ProcessDBDictionaryItem item = (ProcessDBDictionaryItem) container.getItem(itemId).getBean();
                List<ProcessDBDictionaryItemValue> values = new ArrayList<ProcessDBDictionaryItemValue>(item.getValues());
                StringBuilder sb = new StringBuilder();
                if (values.isEmpty()) {
                    sb.append(getEmptyDescription());
                }
                else {
                    sb.append("<ul>");
                    java.util.Collections.sort(values, new Comparator<ProcessDBDictionaryItemValue>() {
                        @Override
                        public int compare(ProcessDBDictionaryItemValue o1, ProcessDBDictionaryItemValue o2) {
                            return o1.getValue().compareTo(o2.getValue());
                        }
                    });
                    for (ProcessDBDictionaryItemValue value : values) {
                        sb.append("<li>").append(getItemRepresentation(value)).append("</li>");
                    }
                    sb.append("</ul>");
                }
                sb.append("</b>");
                largeView.setValue(sb.toString());
            }
        }
    }

    private ColumnGenerator createDeleteColumn(final BeanItemContainer container) {
        return new ColumnGenerator() {
            @Override
            public Component generateCell(Table source, final Object itemId, Object columnId) {
                Button b = smallButton(getMessage("pagedtable.delete"));
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

    private boolean hasPermissionsForDictionary(ProcessDBDictionary config) {
        if (config.getPermissions() == null || config.getPermissions().isEmpty()) {
            return true;
        }

        Collection<ProcessDBDictionaryPermission> edit = Collections.filter(config.getPermissions(), new Predicate<ProcessDBDictionaryPermission>() {
            @Override
            public boolean apply(ProcessDBDictionaryPermission input) {
                return PRIVILEGE_EDIT.equalsIgnoreCase(input.getPrivilegeName());
            }
        });

        ProcessDBDictionaryPermission permission = Collections.firstMatching(edit, new Predicate<ProcessDBDictionaryPermission>() {
            @Override
            public boolean apply(ProcessDBDictionaryPermission input) {
                return application.hasMatchingRole(input.getRoleName());
            }
        });
        return permission != null;
    }
}


