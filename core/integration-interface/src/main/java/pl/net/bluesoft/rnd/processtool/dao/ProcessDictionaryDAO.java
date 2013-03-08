package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.util.ConfigurationResult;

import java.util.List;

public interface ProcessDictionaryDAO extends HibernateBean<ProcessDBDictionary> {
    ProcessDBDictionary fetchProcessDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchProcessDictionaries(ProcessDefinitionConfig definition);
    List<ProcessDBDictionary> fetchAllProcessDictionaries();

    ProcessDBDictionary fetchGlobalDictionary(String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchAllGlobalDictionaries();

    /** Add or update process dictionaries */
    void processProcessDictionaries(List<ProcessDBDictionary> dictionary, ConfigurationResult result, boolean overwrite);
    
    /** Add or update global dictionaries */
    void processGlobalDictionaries(List<ProcessDBDictionary> dictionary, boolean overwrite);
    
    void updateDictionary(ProcessDBDictionary dictionary);
	void copyDictionaries(ProcessDefinitionConfig oldDefinitionConfig,
			ProcessDefinitionConfig newDefinitionConfig);
	
	/** Add or update dictionary item with given key and value, for specified dictionary id */
	void createOrUpdateDictionaryItem(ProcessDBDictionary dictionary,String dictionaryItemKey, String dictionaryItemValue);
	
	ProcessDBDictionaryItem refresh(ProcessDBDictionaryItem item);
}
