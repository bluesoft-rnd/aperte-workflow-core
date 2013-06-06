package pl.net.bluesoft.rnd.processtool.dao;

import java.util.Collection;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;

public interface ProcessDictionaryDAO extends HibernateBean<ProcessDBDictionary> {
    ProcessDBDictionary fetchProcessDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchProcessDictionaries(ProcessDefinitionConfig definition);
    List<ProcessDBDictionary> fetchAllProcessDictionaries();

    ProcessDBDictionary fetchGlobalDictionary(String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchAllGlobalDictionaries();

    /** Add or update process dictionaries */
    void processProcessDictionaries(Collection<ProcessDBDictionary> newDictionaries, ProcessDefinitionConfig newProcess, ProcessDefinitionConfig oldProcess, boolean overwrite);
    
    /** Add or update global dictionaries */
    void processGlobalDictionaries(Collection<ProcessDBDictionary> newDictionaries, boolean overwrite);
    
    void updateDictionary(ProcessDBDictionary dictionary);
	void copyDictionaries(ProcessDefinitionConfig oldDefinitionConfig,
			ProcessDefinitionConfig newDefinitionConfig);
	
	/** Add or update dictionary item with given key and value, for specified dictionary id */
	void createOrUpdateDictionaryItem(ProcessDBDictionary dictionary,String dictionaryItemKey, String dictionaryItemValue);
	
	ProcessDBDictionaryItem refresh(ProcessDBDictionaryItem item);
}
