package pl.net.bluesoft.rnd.processtool.ui.dict;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.aperteworkflow.util.dict.ui.DictionaryItemForm;
import org.aperteworkflow.util.dict.ui.DictionaryItemTableBuilder;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.*;
import pl.net.bluesoft.rnd.processtool.ui.dict.wrappers.DBDictionaryItemValueWrapper;
import pl.net.bluesoft.rnd.processtool.ui.dict.wrappers.DBDictionaryItemWrapper;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.*;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

public class DictionariesMainPane extends VerticalLayout implements ProcessToolBpmConstants, Refreshable, DictionaryItemTableBuilder.DictionaryItemModificationHandler<DBDictionaryItemWrapper> {
    private GenericVaadinPortlet2BpmApplication application;
    private I18NSource i18NSource;
    private TransactionProvider transactionProvider;

    private TabSheet tabSheet;

    private Select globalDictionarySelect;
    private Select processDefinitionSelect;

    private HorizontalLayout processHeaderLayout;
    private VerticalLayout processTableLayout;

    private BeanItemContainer<ProcessDefinitionConfig> processContainer;
    private BeanItemContainer<ProcessDBDictionary> globalDictionaryContainer;
    private Map<ProcessDBDictionary, BeanItemContainer<DBDictionaryItemWrapper>> dictItemContainers;
    private Map<ProcessDefinitionConfig, Map<String, Set<ProcessDBDictionary>>> processDictionariesMap;
    private Map<String, Set<ProcessDBDictionary>> globalDictionariesMap;
	private DictionaryItemTableBuilder<ProcessDBDictionaryItem, DBDictionaryItemValueWrapper, DBDictionaryItemWrapper> builder;

	public DictionariesMainPane(final GenericVaadinPortlet2BpmApplication application, final I18NSource i18NSource, TransactionProvider transactionProvider) {
        this.application = application;
        this.i18NSource = i18NSource;
        this.transactionProvider = transactionProvider;
		this.builder = new DictionaryItemTableBuilder<ProcessDBDictionaryItem, DBDictionaryItemValueWrapper, DBDictionaryItemWrapper>(this) {
			@Override
			protected DictionaryItemForm createDictionaryItemForm(Application application, I18NSource source, BeanItem<DBDictionaryItemWrapper> item) {
				return new DBDictionaryItemForm(application, source, item);
			}

			@Override
			protected Application getApplication() {
				return application;
			}

			@Override
			protected I18NSource getI18NSource() {
				return i18NSource;
			}
		};
        setWidth("100%");
        initWidget();
        loadData();
    }

    private void initWidget() {
        removeAllComponents();

        dictItemContainers = new LinkedHashMap<ProcessDBDictionary, BeanItemContainer<DBDictionaryItemWrapper>>();
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
        BeanItemContainer<DBDictionaryItemWrapper> container = new BeanItemContainer<DBDictionaryItemWrapper>(DBDictionaryItemWrapper.class);
        List<ProcessDBDictionaryItem> items = new ArrayList<ProcessDBDictionaryItem>(dict.getItems().values());
        container.addAll(from(items).select(new F<ProcessDBDictionaryItem, DBDictionaryItemWrapper>() {
			@Override
			public DBDictionaryItemWrapper invoke(ProcessDBDictionaryItem x) {
				return new DBDictionaryItemWrapper(x);
			}
		}));
        container.sort(new Object[] {"key"}, new boolean[] {false});
        dictItemContainers.put(dict, container);
    }

    private BeanItemContainer<DBDictionaryItemWrapper> getDictionaryItems(ProcessDBDictionary dict) {
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
                showItemDetails(new BeanItem<DBDictionaryItemWrapper>(new DBDictionaryItemWrapper()), new DictionaryItemTableBuilder.SaveCallback<DBDictionaryItemWrapper>() {
                    @Override
                    public void onSave(BeanItem<DBDictionaryItemWrapper> item) {
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

    private Component localeSelected(ProcessDBDictionary dict) {
        BeanItemContainer<DBDictionaryItemWrapper> container = getDictionaryItems(dict);
        return builder.createTable(container);
    }

    public String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    public String getMessage(String key, String defaultValue) {
        return i18NSource.getMessage(key, defaultValue);
    }

    private void prepareAndSaveNewItem(BeanItem<DBDictionaryItemWrapper> item, ProcessDBDictionary dictionary) {
		DBDictionaryItemWrapper bean = item.getBean();
        ProcessDictionaryItem lookedUpItem = dictionary.lookup(bean.getKey());
        if (lookedUpItem != null) {
            validationNotification(application, i18NSource, getMessage("validate.dictentry.exists"));
        }
        else {
            bean.getWrappedObject().setDictionary(dictionary);
            dictionary.addItem(bean.getWrappedObject());
            dictItemContainers.get(dictionary).addBean(bean);
            handleItemSave(bean);
			builder.closeDetailsWindow();
        }
    }

    private void showItemDetails(BeanItem<DBDictionaryItemWrapper> item, DictionaryItemTableBuilder.SaveCallback<DBDictionaryItemWrapper> callback) {
        builder.showItemDetails(item, callback);
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

	@Override
	public void handleItemSave(DBDictionaryItemWrapper wrapper) {
		final ProcessDBDictionaryItem item = wrapper.getWrappedObject();
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
	}

	@Override
	public void handleItemDelete(DBDictionaryItemWrapper wrapper) {
		ProcessDBDictionaryItem item = wrapper.getWrappedObject();
		final ProcessDBDictionary dictionary = item.getDictionary();
		dictionary.removeItem(item.getKey());
		item.setDictionary(null);
		transactionProvider.withTransaction(new ProcessToolGuiCallback() {
			@Override
			public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
				ctx.getProcessDictionaryDAO().updateDictionary(dictionary);
			}
		});
	}
}


