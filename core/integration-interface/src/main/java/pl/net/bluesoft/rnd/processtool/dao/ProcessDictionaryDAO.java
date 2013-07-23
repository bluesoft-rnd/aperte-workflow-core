package pl.net.bluesoft.rnd.processtool.dao;

import java.util.Collection;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;

public interface ProcessDictionaryDAO extends HibernateBean<ProcessDBDictionary> {
	ProcessDBDictionary fetchDictionary(String dictionaryId);
	List<ProcessDBDictionary> fetchAllDictionaries();

	/** Add or update global dictionaries */
	void processDictionaries(Collection<ProcessDBDictionary> newDictionaries, boolean overwrite);

	void updateDictionary(ProcessDBDictionary dictionary);

	/** Add or update dictionary item with given key and value, for specified dictionary id */
	void createOrUpdateDictionaryItem(ProcessDBDictionary dictionary, String languageCode, String dictionaryItemKey, String dictionaryItemValue);

	ProcessDBDictionaryItem refresh(ProcessDBDictionaryItem item);
}
