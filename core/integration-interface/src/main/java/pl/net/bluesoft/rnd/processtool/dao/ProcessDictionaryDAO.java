package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;

import java.util.List;

public interface ProcessDictionaryDAO extends HibernateBean<ProcessDBDictionary> {
    ProcessDBDictionary fetchProcessDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchProcessDictionaries(ProcessDefinitionConfig definition);
    List<ProcessDBDictionary> fetchAllProcessDictionaries();

    ProcessDBDictionary fetchGlobalDictionary(String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchAllGlobalDictionaries();

    void createOrUpdateDictionary(ProcessDefinitionConfig definition, ProcessDBDictionary dictionary, boolean overwrite);
    void createOrUpdateDictionaries(ProcessDefinitionConfig definition, List<ProcessDBDictionary> dictionary, boolean overwrite);
    void updateDictionary(ProcessDBDictionary dictionary);
	void copyDictionaries(ProcessDefinitionConfig oldDefinitionConfig,
			ProcessDefinitionConfig newDefinitionConfig);
	
	/** Add or update dictionary item with given key and value, for specified dictionary id */
	void createOrUpdateDictionaryItem(ProcessDBDictionary dictionary,String dictionaryItemKey, String dictionaryItemValue);
}
