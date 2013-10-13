package pl.net.bluesoft.rnd.processtool.plugins.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

/**
 * Manager which proviedes dictionary value change in database, using jbpm
 * context. 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DictionaryHelpChanger 
{
	private ProcessToolRegistry registry;
	
	public DictionaryHelpChanger(ProcessToolRegistry registry) 
	{
		this.registry = registry;
	}

	public void changeDictionaryHelp(final DictionaryChangeRequest changeRequest)
	{
		registry.withProcessToolContext(new ProcessToolContextCallback()
		{
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessDictionaryDAO dictionaryDao = registry.getDataRegistry().getProcessDictionaryDAO(ctx.getHibernateSession());
				ProcessDBDictionary dictionary = dictionaryDao.fetchDictionary(changeRequest.getDictionaryId());

				if(dictionary == null)
				{
					dictionary = createDictionary(changeRequest.getDictionaryId());
					dictionaryDao.saveOrUpdate(dictionary);
				}
				dictionaryDao.createOrUpdateDictionaryItem(dictionary, changeRequest.getLanguageCode(), changeRequest.getDictionaryItemKey(), changeRequest.getDictionaryItemValue());
			}
		});
	}
	
	private ProcessDBDictionary createDictionary(String dictionaryId)
	{
		ProcessDBDictionary processDictionary = new ProcessDBDictionary();
		processDictionary.setDictionaryId(dictionaryId);
		return processDictionary;
	}
	
	/** DTO for better data flow inside the servlet */
	public static class DictionaryChangeRequest
	{
		String processDeifinitionName;
		String dictionaryId;
		String languageCode;
		String dictionaryItemKey;
		String dictionaryItemValue;
		
		public String getProcessDeifinitionName() {
			return processDeifinitionName;
		}
		public DictionaryChangeRequest setProcessDeifinitionName(String processDeifinitionName) 
		{
			this.processDeifinitionName = processDeifinitionName;
			return this;
		}
		public String getDictionaryId() {
			return dictionaryId;
		}
		public DictionaryChangeRequest setDictionaryId(String dictionaryId) 
		{
			this.dictionaryId = dictionaryId;
			return this;
		}
		public String getLanguageCode() {
			return languageCode;
		}
		public DictionaryChangeRequest setLanguageCode(String languageCode) 
		{
			this.languageCode = languageCode;
			return this;
		}
		public String getDictionaryItemKey() 
		{
			return dictionaryItemKey;
		}
		public DictionaryChangeRequest setDictionaryItemKey(String dictionaryItemKey) 
		{
			this.dictionaryItemKey = dictionaryItemKey;
			return this;
		}
		public String getDictionaryItemValue() 
		{
			return dictionaryItemValue;
		}
		public DictionaryChangeRequest setDictionaryItemValue(String dictionaryItemValue) 
		{
			this.dictionaryItemValue = dictionaryItemValue;
			return this;
		}
	}
}
