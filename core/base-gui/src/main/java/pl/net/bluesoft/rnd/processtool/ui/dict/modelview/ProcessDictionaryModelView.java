package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.TransactionProvider;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolGuiCallback;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;


/**
 * Controller to perform dictionary operations 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ProcessDictionaryModelView extends DictionaryModelView
{
	private Collection<ProcessDefinitionConfig> configs;
	private BeanItemContainer<ProcessDefinitionConfig> beanItemContainerConfigs;
	
	private Collection<String> languageCodes;
	private BeanItemContainer<String> beanItemContainerLanguageCodes;
	
	private ProcessDefinitionConfig selectedProcess;
	private String selectedLocale;
	
	private DictionaryFilter dictionaryFilter;
	
	public ProcessDictionaryModelView(TransactionProvider transactionProvider, GenericVaadinPortlet2BpmApplication application) {
		super(transactionProvider, application);
	}

	@Override
	protected void init() 
	{
		super.init();
		
        languageCodes = new HashSet<String>();
    	
    	beanItemContainerConfigs = new BeanItemContainer<ProcessDefinitionConfig>(ProcessDefinitionConfig.class);
    	beanItemContainerLanguageCodes = new BeanItemContainer<String>(String.class);
    	
    	dictionaryFilter = new DictionaryFilter();
    	getBeanItemContainerDictionaries().addContainerFilter(dictionaryFilter);
	}
	
	@Override
	protected void loadData(ProcessToolContext ctx) 
	{
		super.loadData(ctx);
		
		languageCodes.clear();
		
    	beanItemContainerConfigs.removeAllItems();
    	beanItemContainerLanguageCodes.removeAllItems();
    	
        configs = ctx.getProcessDefinitionDAO().getActiveConfigurations();
        
        beanItemContainerConfigs.addAll(configs);
        
        ProcessDictionaryProvider pdp = ctx.getProcessDictionaryRegistry().getProcessDictionaryProvider("db");
        List<ProcessDBDictionary> allDictnionaries = pdp.fetchAllActiveProcessDictionaries();
        for (ProcessDBDictionary dict : allDictnionaries) 
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
    		getTransactionProvider().withTransaction(new ProcessToolGuiCallback() {
				
				@Override
				public void callback(ProcessToolContext ctx, ProcessToolBpmSession session) {
					ctx.getProcessDictionaryDAO().refresh(getSelectedDictionary());
					
				}
			});

    		refreshDictionaryItems();
    		
    	}
    	super.refreshData();
    }

	public BeanItemContainer<ProcessDefinitionConfig> getBeanItemContainerConfigs() {
		return beanItemContainerConfigs;
	}

	public BeanItemContainer<String> getBeanItemContainerLanguageCodes() {
		return beanItemContainerLanguageCodes;
	}

	public void setBeanItemContainerConfigs(
			BeanItemContainer<ProcessDefinitionConfig> beanItemContainerConfigs) {
		this.beanItemContainerConfigs = beanItemContainerConfigs;
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
	
	public ProcessDefinitionConfig getSelectedProcess() {
		return selectedProcess;
	}

	public void setSelectedProcess(ProcessDefinitionConfig selectedProcess) 
	{
		this.selectedProcess = selectedProcess;
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
			
			if(dictionary.getProcessDefinition() == null || !dictionary.getProcessDefinition().equals(selectedProcess))
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
