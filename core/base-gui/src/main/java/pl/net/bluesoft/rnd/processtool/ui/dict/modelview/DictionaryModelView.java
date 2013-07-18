package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.Collection;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import pl.net.bluesoft.util.lang.cquery.func.F;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Controller to perform dictionary operations 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DictionaryModelView 
{
	private GenericVaadinPortlet2BpmApplication application;

    private EnteryBeanItemContainer<ProcessDBDictionary> beanItemContainerDictionaries;
    private EnteryBeanItemContainer<ProcessDBDictionaryItem> beanItemContainerDictionaryItems;
    private EnteryBeanItemContainer<ProcessDBDictionaryItemValue> beanItemContainerDictionaryItemsValues;
    
    private ProcessDBDictionary selectedDictionary;
    private ProcessDBDictionaryItem selectedDictionaryItem;
    private String selectedDictionaryItemKey;

	public DictionaryModelView(GenericVaadinPortlet2BpmApplication application)
	{
		this.application = application;
	}
	
	protected void init()
	{
		beanItemContainerDictionaries = new EnteryBeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class);
		beanItemContainerDictionaryItems = new EnteryBeanItemContainer<ProcessDBDictionaryItem>(ProcessDBDictionaryItem.class);
		beanItemContainerDictionaryItemsValues = new EnteryBeanItemContainer<ProcessDBDictionaryItemValue>(ProcessDBDictionaryItemValue.class);
	}
	
	public void reloadData()
	{
		loadData();
	}
	
	protected void loadData()
	{
    	beanItemContainerDictionaries.removeAllItems();
	}
	
	public void discardChanges() 
	{
		/* No selected item, nothing to discard */
		if(selectedDictionaryItem == null)
			return;
		
		/* restore key for equals */
		selectedDictionaryItem.setKey(selectedDictionaryItemKey);
		
		BeanItem<ProcessDBDictionaryItem> modifiedItem =  beanItemContainerDictionaryItems.getItem(selectedDictionaryItem);
		
		selectedDictionaryItem = getThreadProcessToolContext().getProcessDictionaryDAO().refresh(selectedDictionaryItem);

		/* Restore all properties */
		BeanItem<ProcessDBDictionaryItem> refreshedItem = new BeanItem<ProcessDBDictionaryItem>(selectedDictionaryItem);
		
		for(Object propertyId: modifiedItem.getItemPropertyIds())
		{
			Property propertyToRefresh = modifiedItem.getItemProperty(propertyId);

			if(propertyToRefresh.isReadOnly()) {
				continue;
			}
			
			Property property = refreshedItem.getItemProperty(propertyId);
			
			propertyToRefresh.setValue(property.getValue());
		}
		
		this.selectedDictionaryItem = null;
		this.selectedDictionaryItemKey = null;
		//beanItemContainerDictionaryItems.refresh();
	}
	
	public void refreshData() 
    {
    	/** If there is selected dictionary to refresh, update it */
    	if(selectedDictionary != null)
    	{
    		ProcessDBDictionary refreshedDictionary = getThreadProcessToolContext().getProcessDictionaryDAO()
					.refresh(selectedDictionary);

    		setSelectedDictionary(refreshedDictionary);
    	}
    }
	
	public void refreshDictionaryItems()
	{
		beanItemContainerDictionaryItems.removeAllItems();
		beanItemContainerDictionaryItems.addAll(selectedDictionary.getItems().values());
		
		beanItemContainerDictionaryItems.sort(new Object[]{"key"}, new boolean[]{true});
	}
	
	public void addDictionary(ProcessDBDictionary dict) 
	{
		beanItemContainerDictionaries.addBean(dict);
	}
	
	public void addDictionaryItemValue(ProcessDBDictionaryItemValue newValue) 
	{
		if(selectedDictionaryItem == null) {
			throw new RuntimeException("No dictionary item was selected");
		}

		selectedDictionaryItem.addValue(newValue);
		beanItemContainerDictionaryItemsValues.addBean(newValue);
	}
	
	public void setSelectedDictionaryItem(ProcessDBDictionaryItem item) 
	{		
    	this.selectedDictionaryItem = item;
    	this.selectedDictionaryItemKey = item.getKey();
    	
    	beanItemContainerDictionaryItemsValues.removeAllItems();
    	beanItemContainerDictionaryItemsValues.addAll(item.getValues());
	}
	
	public ProcessDBDictionaryItem getSelectedDictionaryItem() {
		return selectedDictionaryItem;
	}
	
	public void removeItem(ProcessDBDictionaryItem itemToRemove) 
	{
		boolean isPersistedItem = itemToRemove.getId() == null;
		
		beanItemContainerDictionaryItems.removeItem(itemToRemove);
		
		/* Item has been stored in database, remove it */
		if(isPersistedItem)
		{
			ProcessDBDictionary dictionary = itemToRemove.getDictionary();
			dictionary.removeItem(itemToRemove.getKey());
        
			itemToRemove.setDictionary(null);

			getThreadProcessToolContext().getProcessDictionaryDAO().updateDictionary(dictionary);
		}
	}
	
	public void removeItemValue(ProcessDBDictionaryItemValue value) 
	{
		beanItemContainerDictionaryItemsValues.removeItem(value);
	}

	public BeanItemContainer<ProcessDBDictionary> getBeanItemContainerDictionaries() {
		return beanItemContainerDictionaries;
	}

	public BeanItemContainer<ProcessDBDictionaryItem> getBeanItemContainerDictionaryItems() {
		return beanItemContainerDictionaryItems;
	}

	public BeanItemContainer<ProcessDBDictionaryItemValue> getBeanItemContainerDictionaryItemsValues() {
		return beanItemContainerDictionaryItemsValues;
	}

	public ProcessDBDictionary getSelectedDictionary() {
		return selectedDictionary;
	}

	public void setSelectedDictionary(ProcessDBDictionary selectedDictionary)
	{
		this.selectedDictionary = selectedDictionary;
		
		/* Add all dictionery items */
		beanItemContainerDictionaryItems.removeAllItems();
		if(selectedDictionary != null) {
			beanItemContainerDictionaryItems.addAll(selectedDictionary.getItems().values());
		}
	}

    protected boolean hasPermissionsForDictionary(ProcessDBDictionary config) {
        if (config.getPermissions() == null || config.getPermissions().isEmpty()) {
            return true;
        }

        Collection<ProcessDBDictionaryPermission> edit = Collections.filter(config.getPermissions(), new Predicate<ProcessDBDictionaryPermission>() {
            @Override
            public boolean apply(ProcessDBDictionaryPermission input) {
                return ProcessToolBpmConstants.PRIVILEGE_EDIT.equalsIgnoreCase(input.getPrivilegeName());
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

	public void addDictionaryItem(ProcessDBDictionaryItem item) 
	{
    	getBeanItemContainerDictionaryItems().addBean(item);
	}

	protected List<ProcessDBDictionary> orderByDictionaryName(List<ProcessDBDictionary> dictionaries) {
		return from(dictionaries).orderBy(new F<ProcessDBDictionary, String>() {
			@Override
			public String invoke(ProcessDBDictionary x) {
				return x.getDefaultName();
			}
		}).toList();
	}
}
