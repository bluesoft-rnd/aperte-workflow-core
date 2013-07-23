package pl.net.bluesoft.rnd.processtool.ui.dict;

import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.validationNotification;
import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

import java.util.HashSet;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.ui.dict.modelview.GlobalDictionaryModelView;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.AddNewDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.CancelEditionOfDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.CopyDictionaryItemValueActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.DeleteDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.DeleteDictionaryItemValueActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.SaveDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.SaveNewDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.request.EditDictionaryItemActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.dict.validator.DictionaryItemValidator;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequest;
import pl.net.bluesoft.rnd.processtool.ui.request.IActionRequestListener;
import pl.net.bluesoft.rnd.processtool.ui.request.exception.UnknownActionRequestException;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class DictionariesMainPane extends VerticalLayout implements Refreshable, IActionRequestListener, IEntryValidator<ProcessDBDictionaryItem>
{
    private GenericVaadinPortlet2BpmApplication application;
    private I18NSource i18NSource;

    private TabSheet tabSheet;

    private Window detailsWindow;

    private GlobalDictionaryTab globalTab;

    public DictionariesMainPane(GenericVaadinPortlet2BpmApplication application, I18NSource i18NSource) {
        this.application = application;
        this.i18NSource = i18NSource;
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

        globalTab = new GlobalDictionaryTab(this, new GlobalDictionaryModelView(application));

        tabSheet = new TabSheet();
        tabSheet.setWidth("100%");
        tabSheet.addTab(globalTab, getMessage("dict.title.global"), VaadinUtility.imageResource(application, "globe.png"));   

        addComponent(horizontalLayout(titleLabel, VaadinUtility.refreshIcon(application, this)));
        addComponent(new Label(getMessage("dict.help.short")));
        addComponent(tabSheet);
    }

    private void loadData() 
    {
    	globalTab.getModelView().reloadData();
    }

    @Override
	public void refreshData()
    {
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

		getThreadProcessToolContext().getProcessDictionaryDAO().updateDictionary(item.getDictionary());

        application.getMainWindow().removeWindow(detailsWindow);
        detailsWindow = null;

		globalTab.commitChanges();
        refreshData();
    }


	@Override
	public void handleActionRequest(IActionRequest actionRequest) 
	{
		if(actionRequest instanceof DeleteDictionaryItemActionRequest)
		{
			DeleteDictionaryItemActionRequest deleteRequest = (DeleteDictionaryItemActionRequest)actionRequest;

			globalTab.removeItem(deleteRequest.getItemToDelete());
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
			globalTab.editItem(addNewRequest.getItemToShow());
		}
		else if(actionRequest instanceof EditDictionaryItemActionRequest)
		{
			EditDictionaryItemActionRequest showRequest = (EditDictionaryItemActionRequest)actionRequest;
			
			/* Show edition window for item to edit */
			globalTab.editItem(showRequest.getItemToShow());
		}
		else if(actionRequest instanceof CancelEditionOfDictionaryItemActionRequest)
		{
			CancelEditionOfDictionaryItemActionRequest cancelRequest = (CancelEditionOfDictionaryItemActionRequest)actionRequest;
			
			/* New item creantion cancellation */
			if(cancelRequest.getItemToRollback().getId() == null)
			{
				globalTab.dicardChanges();
				globalTab.removeItem(cancelRequest.getItemToRollback());
			}
			/* Discard current change and refresh dictionary and view */
			else
			{
				globalTab.dicardChanges();
			}
		}
		else if(actionRequest instanceof CopyDictionaryItemValueActionRequest)
		{
			CopyDictionaryItemValueActionRequest request = (CopyDictionaryItemValueActionRequest)actionRequest;
			
			/* Make shallow copy of the item's value */
			ProcessDBDictionaryItemValue value =  request.getItemValueToCopy().getWrappedObject();
			ProcessDBDictionaryItemValue shallowCopy = value.shallowCopy();
			
			/* Add copy to the items' values */
			value.getItem().addValue(shallowCopy);
			
			/* Inform modelView about change */
			globalTab.getModelView().addDictionaryItemValue(shallowCopy);
		}
		else if(actionRequest instanceof DeleteDictionaryItemValueActionRequest)
		{
			DeleteDictionaryItemValueActionRequest showRequest = (DeleteDictionaryItemValueActionRequest)actionRequest;
			ProcessDBDictionaryItemValue value =  showRequest.getItemValueToDelete().getWrappedObject();
			
			/* Remove value from the item's collection */
			value.getItem().removeValue(value);
			
			/* Inform modelView about change */
			globalTab.getModelView().removeItemValue(showRequest.getItemValueToDelete());
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
		return application;
	}

    /**
     * Validate given dictionary item
     * 
     * @param item item to validate
     * @return false if item is invalid
     */
    @Override
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

	public I18NSource getI18NSource() {
		return i18NSource;
	}
}


