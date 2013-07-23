package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.*;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.model.dict.db.*;
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

	private BeanItemContainer<String> beanItemContainerLanguageCodes;
    private EnteryBeanItemContainer<ProcessDBDictionary> beanItemContainerDictionaries;
    private EnteryBeanItemContainer<ProcessDBDictionaryItem> beanItemContainerDictionaryItems;
    private EnteryBeanItemContainer<ProcessDBDictionaryItemValueWrapper> beanItemContainerDictionaryItemsValues;

	private String selectedLocale;
    private ProcessDBDictionary selectedDictionary;
    private ProcessDBDictionaryItem selectedDictionaryItem;
    private String selectedDictionaryItemKey;

	public DictionaryModelView(GenericVaadinPortlet2BpmApplication application)
	{
		this.application = application;
	}
	
	protected void init()
	{
		beanItemContainerLanguageCodes = new BeanItemContainer<String>(String.class);
		beanItemContainerDictionaries = new EnteryBeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class);
		beanItemContainerDictionaryItems = new EnteryBeanItemContainer<ProcessDBDictionaryItem>(ProcessDBDictionaryItem.class);
		beanItemContainerDictionaryItemsValues = new EnteryBeanItemContainer<ProcessDBDictionaryItemValueWrapper>(ProcessDBDictionaryItemValueWrapper.class);
	}
	
	public void reloadData()
	{
		loadData();
	}
	
	protected void loadData()
	{
		beanItemContainerLanguageCodes.removeAllItems();
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
		beanItemContainerDictionaryItemsValues.addBean(new ProcessDBDictionaryItemValueWrapper(newValue));
	}
	
	public void setSelectedDictionaryItem(ProcessDBDictionaryItem item) 
	{		
    	this.selectedDictionaryItem = item;
    	this.selectedDictionaryItemKey = item.getKey();
    	
    	beanItemContainerDictionaryItemsValues.removeAllItems();
    	beanItemContainerDictionaryItemsValues.addAll(from(item.getValues()).select(new F<ProcessDBDictionaryItemValue, ProcessDBDictionaryItemValueWrapper>() {
			@Override
			public ProcessDBDictionaryItemValueWrapper invoke(ProcessDBDictionaryItemValue x) {
				return new ProcessDBDictionaryItemValueWrapper(x);
			}
		}));
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
	
	public void removeItemValue(ProcessDBDictionaryItemValueWrapper value)
	{
		beanItemContainerDictionaryItemsValues.removeItem(value);
	}

	public BeanItemContainer<ProcessDBDictionary> getBeanItemContainerDictionaries() {
		return beanItemContainerDictionaries;
	}

	public BeanItemContainer<ProcessDBDictionaryItem> getBeanItemContainerDictionaryItems() {
		return beanItemContainerDictionaryItems;
	}

	public BeanItemContainer<ProcessDBDictionaryItemValueWrapper> getBeanItemContainerDictionaryItemsValues() {
		return beanItemContainerDictionaryItemsValues;
	}

	public BeanItemContainer<String> getBeanItemContainerLanguageCodes() {
		return beanItemContainerLanguageCodes;
	}

	public void addLanguageCode(String languageCode)
	{
		beanItemContainerLanguageCodes.addBean(languageCode);
	}

	public String getSelectedLocale() {
		return selectedLocale;
	}

	public void setSelectedLocale(String selectedLocale)
	{
		this.selectedLocale = selectedLocale;

		/* There was selected Dictionary, try to find its coresponding dictionary
		 * in new locale
		 */
		if(selectedDictionary != null)
		{
			setSelectedDictionary(selectedDictionary);
		}
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

	public class ProcessDBDictionaryItemValueWrapper {
		private final ProcessDBDictionaryItemValue wrappedObject;

		private ProcessDBDictionaryItemValueWrapper(ProcessDBDictionaryItemValue wrappedObject) {
			this.wrappedObject = wrappedObject;
		}

		public ProcessDBDictionaryItemValue getWrappedObject() {
			return wrappedObject;
		}

		public String getDefaultValue() {
			return wrappedObject.getDefaultValue();
		}

		public void setDefaultValue(String value) {
			wrappedObject.setDefaultValue(value);
		}

		public String getValue() {
			return wrappedObject.getValue(selectedLocale);
		}

		public void setValue(String value) {
			wrappedObject.setValue(selectedLocale, value);
		}

		public Date getValidFrom() {
			return wrappedObject.getValidFrom();
		}

		public void setValidFrom(Date validFrom) {
			wrappedObject.setValidFrom(validFrom);
		}

		public Date getValidTo() {
			return wrappedObject.getValidTo();
		}

		public void setValidTo(Date validTo) {
			wrappedObject.setValidTo(validTo);
		}

		public Set<ProcessDBDictionaryItemExtension> getExtensions() {
			return wrappedObject.getExtensions();
		}

		public void setExtensions(Set<ProcessDBDictionaryItemExtension> extensions) {
			wrappedObject.setExtensions(extensions);
		}
	}
}
