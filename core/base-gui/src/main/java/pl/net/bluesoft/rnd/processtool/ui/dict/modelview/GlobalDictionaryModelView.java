package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.Collection;
import java.util.HashSet;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;


/**
 * Global dictionaries ModelView
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class GlobalDictionaryModelView extends DictionaryModelView
{
	private Collection<String> languageCodes;
	private BeanItemContainer<String> beanItemContainerLanguageCodes;
	
	private String selectedLocale;
	
	private DictionaryFilter dictionaryFilter;
	
	public GlobalDictionaryModelView(TransactionProvider transactionProvider, GenericVaadinPortlet2BpmApplication application) {
		super(transactionProvider, application);
	}

	@Override
	protected void init() 
	{
		super.init();
		
        languageCodes = new HashSet<String>();
    	
    	beanItemContainerLanguageCodes = new BeanItemContainer<String>(String.class);
    	
    	dictionaryFilter = new DictionaryFilter();
    	getBeanItemContainerDictionaries().addContainerFilter(dictionaryFilter);
	}
	
	@Override
	protected void loadData(ProcessToolContext ctx) 
	{
		super.loadData(ctx);
		
		languageCodes.clear();
		

    	beanItemContainerLanguageCodes.removeAllItems();
 
        
        GlobalDictionaryProvider gdp = ctx.getProcessDictionaryRegistry().getGlobalDictionaryProvider("db");
        Collection<ProcessDBDictionary> allDictionaries = gdp.fetchAllGlobalDictionaries();
        
        for (ProcessDBDictionary dict : allDictionaries) 
        {
            if (hasPermissionsForDictionary(dict)) 
            {
            	addDictionary(dict);
            }
        }
	}
	
    @Override
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
    		refreshDictionaryItems();
    		
    	}
    	
    	super.refreshData();
    }

	public BeanItemContainer<String> getBeanItemContainerLanguageCodes() {
		return beanItemContainerLanguageCodes;
	}

	public void setBeanItemContainerLanguageCodes(
			BeanItemContainer<String> beanItemContainerLanguageCodes) {
		this.beanItemContainerLanguageCodes = beanItemContainerLanguageCodes;
	}

	@Override
	public void addDictionary(ProcessDBDictionary dict) 
	{
		super.addDictionary(dict);
		addLanguageCode(dict.getLanguageCode());
		
	}
	
	public void addLanguageCode(String languageCode)
	{
		languageCodes.add(languageCode);
		beanItemContainerLanguageCodes.addBean(languageCode);
	}

	public String getSelectedLocale() {
		return selectedLocale;
	}

	public void setSelectedLocale(String selectedLocale) 
	{
		this.selectedLocale = selectedLocale;
		
		getBeanItemContainerDictionaries().removeContainerFilter(dictionaryFilter);
		getBeanItemContainerDictionaries().addContainerFilter(dictionaryFilter);
		
		/* There was selected Dictionary, try to find its coresponding dictionary 
		 * in new locale
		 */
		if(getSelectedDictionary() != null)
		{
			ProcessDBDictionary dictionaryInNewLocale = null;
			String dictionaryId = getSelectedDictionary().getDictionaryId();
			for(ProcessDBDictionary dictionary: getBeanItemContainerDictionaries().getItemIds())
			{
				if(dictionary.getDictionaryId().equals(dictionaryId))
				{
					dictionaryInNewLocale = dictionary;
					break;
				}
			}
			
			setSelectedDictionary(dictionaryInNewLocale);
		}
	}

	/** Dictionaries filter by language and process definition */
	private class DictionaryFilter implements Filter
	{
		@Override
		public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException 
		{	
			ProcessDBDictionary dictionary = (ProcessDBDictionary)itemId;
			if(dictionary == null)
				return false;
			
			if(!dictionary.getLanguageCode().equals(selectedLocale))
				return false;
			
			return true;
		}

		@Override
		public boolean appliesToProperty(Object propertyId) 
		{
			return true;
		}
		
	}


	
}
