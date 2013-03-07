package pl.net.bluesoft.rnd.processtool.ui.dict;

import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.validationNotification;

import java.util.HashSet;

import org.aperteworkflow.util.dict.ui.DictionaryItemTableBuilder;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.GlobalDictionaryModelView;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.ProcessDictionaryModelView;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.AddNewDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.CancelEditionOfDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.CopyDictionaryItemValueActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.DeleteDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.DeleteDictionaryItemValueActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.SaveDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.SaveNewDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.ShowDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.validator.DictionaryItemValidator;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequestListener;
import pl.net.bluesoft.rnd.processtool.ui.request.exception.UnknownActionRequestException;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.text.DateFormat;
import java.util.*;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

public class DictionariesMainPane extends VerticalLayout implements ProcessToolBpmConstants, Refreshable, DictionaryItemTableBuilder.DictionaryItemModificationHandler<DBDictionaryItemWrapper>, IActionRequestListener, IEntryValidator<ProcessDBDictionaryItem> {
    private GenericVaadinPortlet2BpmApplication application;
    private I18NSource i18NSource;
    public I18NSource getI18NSource() {
		return i18NSource;
	}

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

    private void initWidget() 
    {
        removeAllComponents();

        Label titleLabel = new Label(getMessage("dict.title"));
        titleLabel.addStyleName("h1 color processtool-title");
        titleLabel.setWidth("100%");

        processTab = new ProcessDictionaryTab(this, new ProcessDictionaryModelView(transactionProvider, application));
        globalTab = new GlobalDictionaryTab(this, new GlobalDictionaryModelView(transactionProvider, application));

        tabSheet = new TabSheet();
        tabSheet.setWidth("100%");
        tabSheet.addTab(processTab, getMessage("dict.title.process"), VaadinUtility.imageResource(application, "dict.png"));
        tabSheet.addTab(globalTab, getMessage("dict.title.global"), VaadinUtility.imageResource(application, "globe.png"));   

        addComponent(horizontalLayout(titleLabel, VaadinUtility.refreshIcon(application, this)));
        addComponent(new Label(getMessage("dict.help.short")));
        addComponent(tabSheet);
    }

    private void loadData() 
    {
    	processTab.getModelView().reloadData();
    	globalTab.getModelView().reloadData();
    }


    public void refreshData() 
    {
    	processTab.getModelView().refreshData();
    	globalTab.getModelView().refreshData();
    }

    public String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    public String getMessage(String key, Object ... parameters) {
        return i18NSource.getMessage(key, key, parameters);
    }

    private void saveDictionaryItem(final ProcessDBDictionaryItem item) {
        if (item.getValues() == null) {
            item.setValues(new HashSet<ProcessDBDictionaryItemValue>());
        }
        else {
            for (ProcessDBDictionaryItemValue itemValue : item.getValues()) {
                itemValue.setItem(item);
                for (ProcessDBDictionaryItemExtension ext : itemValue.getExtensions()) {
                    ext.setItemValue(itemValue);
                }
            }
        }
        getTransactionProvider().withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                ctx.getProcessDictionaryDAO().updateDictionary(item.getDictionary());
            }
        });
        application.getMainWindow().removeWindow(detailsWindow);
        detailsWindow = null;
        
        getTabByItem(item).commitChanges();
        refreshData();
    }


	@Override
	public void handleActionRequest(IActionRequest actionRequest) 
	{
		if(actionRequest instanceof DeleteDictionaryItemActionRequest)
		{
			DeleteDictionaryItemActionRequest deleteRequest = (DeleteDictionaryItemActionRequest)actionRequest;
			ProcessDBDictionaryItem item = deleteRequest.getItemToDelete();
			BeanItemContainer<ProcessDBDictionaryItem> container = deleteRequest.getContainer();
			
            final ProcessDBDictionary dictionary = item.getDictionary();
            dictionary.removeItem(item.getKey());
            item.setDictionary(null);
            container.removeItem(item);
            getTransactionProvider().withTransaction(new ProcessToolGuiCallback() {
                @Override
                public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                    ctx.getProcessDictionaryDAO().updateDictionary(dictionary);
                }
            });
		}
		else if(actionRequest instanceof SaveNewDictionaryItemActionRequest)
		{
			SaveNewDictionaryItemActionRequest saveNewItemRequest = (SaveNewDictionaryItemActionRequest)actionRequest;
			
			/* Get item to save */
			ProcessDBDictionaryItem item = saveNewItemRequest.getItemToSave();
			
			/* Add new item to the dictionary */
	    	ProcessDBDictionary dictionary = item.getDictionary();
            dictionary.addItem(item);
            
            /* Save new dictionary item */
            saveDictionaryItem(item);
	        
	        refreshData();
		}
		else if(actionRequest instanceof SaveDictionaryItemActionRequest)
		{
			SaveDictionaryItemActionRequest saveRequest = (SaveDictionaryItemActionRequest)actionRequest;
			saveDictionaryItem(saveRequest.getItemToSave());
		}
		else if(actionRequest instanceof AddNewDictionaryItemActionRequest)
		{
			AddNewDictionaryItemActionRequest addNewRequest = (AddNewDictionaryItemActionRequest)actionRequest;
			
			/* Show edition window for new item */
			getTabByItem(addNewRequest.getItemToShow()).editItem(addNewRequest.getItemToShow());
		}
		else if(actionRequest instanceof ShowDictionaryItemActionRequest)
		{
			ShowDictionaryItemActionRequest showRequest = (ShowDictionaryItemActionRequest)actionRequest;
			
			/* Show edition window for item to edit */
			getTabByItem(showRequest.getItemToShow()).editItem(showRequest.getItemToShow());
		}
		else if(actionRequest instanceof CancelEditionOfDictionaryItemActionRequest)
		{
			CancelEditionOfDictionaryItemActionRequest cancelRequest = (CancelEditionOfDictionaryItemActionRequest)actionRequest;
			
			/* Discard current change and refresh dictionary and view */
	        getTabByItem(cancelRequest.getItemToRollback()).dicardChanges();
	        refreshData();
		}
		else if(actionRequest instanceof CopyDictionaryItemValueActionRequest)
		{
			CopyDictionaryItemValueActionRequest request = (CopyDictionaryItemValueActionRequest)actionRequest;
			
			/* Make shallow copy of the item's value */
			ProcessDBDictionaryItemValue value =  request.getItemValueToCopy();
			ProcessDBDictionaryItemValue shallowCopy = value.shallowCopy();
			
			/* Add copy to the items' values */
			value.getItem().getValues().add(shallowCopy);
			
			/* Inform modelView about change */
			getTabByItem(value.getItem()).getModelView().addDictionaryItemValue(shallowCopy);
		}
		else if(actionRequest instanceof DeleteDictionaryItemValueActionRequest)
		{
			DeleteDictionaryItemValueActionRequest showRequest = (DeleteDictionaryItemValueActionRequest)actionRequest;
			ProcessDBDictionaryItemValue value =  showRequest.getItemValueToDelete();
			
			/* Remove value from the item's collection */
			value.getItem().getValues().remove(value);
			
			/* Inform modelView about change */
			getTabByItem(value.getItem()).getModelView().removeItemValue(value);
		}
		else
		{
			throw new UnknownActionRequestException("Unknown action request: "+actionRequest);
		}
		
	}
	
	@Override
	public Application getApplication() {
		return application;
	}
	
	public GenericVaadinPortlet2BpmApplication getVaadinApplication()
	{
		return (GenericVaadinPortlet2BpmApplication)application;
	}

    public TransactionProvider getTransactionProvider() {
		return transactionProvider;
	}
    
    /**
     * Validate given dictionary item
     * 
     * @param item item to validate
     * @return false if item is invalid
     */
    public boolean isEntryValid(ProcessDBDictionaryItem item)
    {
    	DictionaryItemValidator validator = new DictionaryItemValidator(application);
    	
    	try
    	{
    		validator.validate(item);
    		
    		/* Item is OK */
    		return true;
    	}
    	catch(InvalidValueException ex)
    	{
    		validationNotification(application, i18NSource, ex.getMessage());
			
			/* Invalid value */
			return false;
    	}
    }
    
    private DictionaryTab getTabByItem(ProcessDBDictionaryItem item)
    {
    	if(item.getDictionary().isGlobalDictionary())
    		return globalTab;
    	else
    		return processTab;
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


