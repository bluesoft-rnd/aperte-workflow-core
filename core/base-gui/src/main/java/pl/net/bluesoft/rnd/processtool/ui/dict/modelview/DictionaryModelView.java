package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;

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
    private BeanItemContainer<ProcessDBDictionary> beanItemContainerDictionaries;
    private BeanItemContainer<ProcessDBDictionaryItem> beanItemContainerDictionaryItems;
    private BeanItemContainer<ProcessDBDictionaryItemValue> beanItemContainerDictionaryItemsValues;
    
    private ProcessDBDictionary selectedDictionary;
    private ProcessDBDictionaryItem selectedDictionaryItem;
    

	public DictionaryModelView(TransactionProvider transactionProvider, GenericVaadinPortlet2BpmApplication application) 
	{
		this.application = application;
		this.transactionProvider = transactionProvider;
		
		init();
	}
	
	protected void init()
	{
		dictionaries = new ArrayList<ProcessDBDictionary>();
		beanItemContainerDictionaries = new BeanItemContainer<ProcessDBDictionary>(ProcessDBDictionary.class); 
		beanItemContainerDictionaryItems = new BeanItemContainer<ProcessDBDictionaryItem>(ProcessDBDictionaryItem.class);
		beanItemContainerDictionaryItemsValues = new BeanItemContainer<ProcessDBDictionaryItemValue>(ProcessDBDictionaryItemValue.class);
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
	
	public void refreshData() 
	{
		// TODO Auto-generated method stub
		
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
    	
    	beanItemContainerDictionaryItemsValues.removeAllItems();
    	beanItemContainerDictionaryItemsValues.addAll(item.getValues());
	}
	
	public ProcessDBDictionaryItem getSelectedDictionaryItem() {
		return selectedDictionaryItem;
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
