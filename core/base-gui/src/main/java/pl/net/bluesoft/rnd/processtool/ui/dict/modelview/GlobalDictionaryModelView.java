package pl.net.bluesoft.rnd.processtool.ui.dict.modelview;

import java.util.Collection;
import java.util.List;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;

import com.vaadin.data.util.BeanItemContainer;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.util.i18n.I18NSource.ThreadUtil.getThreadI18nSource;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;


/**
 * Global dictionaries ModelView
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public final class GlobalDictionaryModelView extends DictionaryModelView
{
	private BeanItemContainer<String> beanItemContainerLanguageCodes;
	
	private String selectedLocale;
	
	public GlobalDictionaryModelView(GenericVaadinPortlet2BpmApplication application) {
		super(application);
		init();
	}

	@Override
	protected void init() 
	{
		super.init();
		
    	beanItemContainerLanguageCodes = new BeanItemContainer<String>(String.class);
	}
	
	@Override
	protected void loadData()
	{
		super.loadData();
		
    	beanItemContainerLanguageCodes.removeAllItems();

		List<ProcessDBDictionary> allDictionaries = getThreadProcessToolContext().getProcessDictionaryRegistry()
				.getDictionaryProvider("db").fetchAllDictionaries();
        
        for (ProcessDBDictionary dict : orderByDictionaryName(allDictionaries))
        {
            if (hasPermissionsForDictionary(dict)) 
            {
            	addDictionary(dict);

				for (String languageCode : getAvailableLanguages(dict)) {
					addLanguageCode(languageCode);
				}
            }
        }
	}

	private Collection<String> getAvailableLanguages(ProcessDBDictionary dict) {
		return from(dict.getUsedLanguageCodes()).concat(getThreadI18nSource().getLocale().toString()).ordered();
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
		if(getSelectedDictionary() != null)
		{
			setSelectedDictionary(getSelectedDictionary());
		}
	}
}
