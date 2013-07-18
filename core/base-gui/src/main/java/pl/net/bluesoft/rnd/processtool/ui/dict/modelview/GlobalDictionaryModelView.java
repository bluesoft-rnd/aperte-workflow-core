package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;


/**
 * Global dictionaries ModelView
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public final class GlobalDictionaryModelView extends DictionaryModelView
{
	private Collection<String> languageCodes;
	private BeanItemContainer<String> beanItemContainerLanguageCodes;
	
	private String selectedLocale;
	
	private DictionaryFilter dictionaryFilter;
	
	public GlobalDictionaryModelView(TransactionProvider transactionProvider, GenericVaadinPortlet2BpmApplication application) {
		super(transactionProvider, application);
		init();
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

		List<ProcessDBDictionary> allDictionaries = ctx.getProcessDictionaryRegistry().getDictionaryProvider("db").fetchAllDictionaries();
        
        for (ProcessDBDictionary dict : orderByDictionaryName(allDictionaries))
        {
            if (hasPermissionsForDictionary(dict)) 
            {
            	addDictionary(dict);
            }
        }
	}

	public BeanItemContainer<String> getBeanItemContainerLanguageCodes() {
		return beanItemContainerLanguageCodes;
	}

	public void setBeanItemContainerLanguageCodes(
			BeanItemContainer<String> beanItemContainerLanguageCodes) {
		this.beanItemContainerLanguageCodes = beanItemContainerLanguageCodes;
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
			return itemId != null;
		}

		@Override
		public boolean appliesToProperty(Object propertyId) 
		{
			return true;
		}
	}
}
