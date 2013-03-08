package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import pl.net.bluesoft.util.lang.cquery.func.F;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Controller to perform dictionary operations 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DictionaryModelView 
{
	private TransactionProvider transactionProvider;
	private GenericVaadinPortlet2BpmApplication application;
	
    private Collection<ProcessDBDictionary> dictionaries;
    private EnteryBeanItemContainer<ProcessDBDictionary> beanItemContainerDictionaries;
    private EnteryBeanItemContainer<ProcessDBDictionaryItem> beanItemContainerDictionaryItems;
    private EnteryBeanItemContainer<ProcessDBDictionaryItemValue> beanItemContainerDictionaryItemsValues;
    
    private ProcessDBDictionary selectedDictionary;
    private ProcessDBDictionaryItem selectedDictionaryItem;
    private String selectedDictionaryItemKey;
    

	public DictionaryModelView(TransactionProvider transactionProvider, GenericVaadinPortlet2BpmApplication application) 
	{
		this.application = application;
		this.transactionProvider = transactionProvider;
		
		init();
	}
	
	protected void init()
	{
		dictionaries = new ArrayList<ProcessDBDictionary>();
		beanItemContainerDictionaries = new EnteryBeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class); 
		beanItemContainerDictionaryItems = new EnteryBeanItemContainer<ProcessDBDictionaryItem>(ProcessDBDictionaryItem.class);
		beanItemContainerDictionaryItemsValues = new EnteryBeanItemContainer<ProcessDBDictionaryItemValue>(ProcessDBDictionaryItemValue.class);
	}
	
	public void reloadData()
	{
		getTransactionProvider().withTransaction(new ProcessToolGuiCallback() {
            @Override
            public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) 
            {
            	loadData(ctx);
            }
        });
	}
	
	protected void loadData(ProcessToolContext ctx)
	{
    	dictionaries.clear();
    	
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
		
		selectedDictionaryItem = getTransactionProvider().withTransaction(new ReturningProcessToolContextCallback<ProcessDBDictionaryItem>() {
			

			@Override
			public ProcessDBDictionaryItem processWithContext(ProcessToolContext ctx) 
			{
				return 	ctx.getProcessDictionaryDAO().refresh(selectedDictionaryItem);
			}
		});
		
		/* Restore all properties */
		BeanItem<ProcessDBDictionaryItem> refreshedItem = new BeanItem<ProcessDBDictionaryItem>(selectedDictionaryItem);
		
		for(Object propertyId: modifiedItem.getItemPropertyIds())
		{
			Property propertyToRefresh = modifiedItem.getItemProperty(propertyId);
			if(propertyToRefresh.isReadOnly())
				continue;
			
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
    	if(getSelectedDictionary() != null)
    	{
    		ProcessDBDictionary refresheDictionary = getTransactionProvider().withTransaction(new ReturningProcessToolContextCallback<ProcessDBDictionary>() {
				

				@Override
				public ProcessDBDictionary processWithContext(ProcessToolContext ctx) 
				{
					return 	ctx.getProcessDictionaryDAO().refresh(getSelectedDictionary());
				}
			});

    		setSelectedDictionary(refresheDictionary);	
    	}
    }
	
	public void refreshDictionaryItems()
	{
		beanItemContainerDictionaryItems.removeAllItems();
		beanItemContainerDictionaryItems.addAll(getSelectedDictionary().getItems().values());
		
		beanItemContainerDictionaryItems.sort(new Object[]{"key"}, new boolean[]{true});
		
	}
	
	public void addDictionary(ProcessDBDictionary dict) 
	{
		dictionaries.add(dict);
		beanItemContainerDictionaries.addBean(dict);
	}
	
	public void addDictionaryItemValue(ProcessDBDictionaryItemValue newValue) 
	{
		if(getSelectedDictionaryItem() == null)
			throw new RuntimeException("No dictionary item was selected");
		
		getSelectedDictionaryItem().addValue(newValue);
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
			final ProcessDBDictionary dictionary = itemToRemove.getDictionary();
			dictionary.removeItem(itemToRemove.getKey());
        
			itemToRemove.setDictionary(null);
			
            getTransactionProvider().withTransaction(new ProcessToolGuiCallback() {
                @Override
                public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
                    ctx.getProcessDictionaryDAO().updateDictionary(dictionary);
                }
            });
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
		if(selectedDictionary != null)
			beanItemContainerDictionaryItems.addAll(selectedDictionary.getItems().values());
	}

	protected TransactionProvider getTransactionProvider() {
		return transactionProvider;
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
				return x.getDictionaryName();
			}
		}).toList();
	}


}
