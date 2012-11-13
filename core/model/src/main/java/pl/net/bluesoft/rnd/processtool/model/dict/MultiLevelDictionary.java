package pl.net.bluesoft.rnd.processtool.model.dict;

import org.apache.commons.collections.CollectionUtils;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import java.util.*;

/**
 * User: POlszewski
 * Date: 2012-09-12
 * Time: 22:31
 */
public class MultiLevelDictionary implements ProcessDictionary<String, String> {
	private ProcessDictionary<String, String>[] dictionaries;
	private Collection<String> allItemKeys;
	private Collection<ProcessDictionaryItem<String, String>> allItems;

	public MultiLevelDictionary(ProcessDictionary... dictionaries) {
		this.dictionaries = Lang2.noCopy(dictionaries);
	}

	public MultiLevelDictionary(List<ProcessDictionary> dictionaries) {
		this(Lang2.toObjectArray(dictionaries, ProcessDictionary.class));
	}

	@Override
	public String getDictionaryId() {
		return dictionaries[0].getDictionaryId();
	}

	@Override
	public String getDictionaryName() {
		return dictionaries[0].getDictionaryName();
	}

	@Override
	public String getLanguageCode() {
		return dictionaries[0].getLanguageCode();
	}

	@Override
	public Boolean isDefaultDictionary() {
		return false;
	}

	@Override
	public ProcessDefinitionConfig getProcessDefinition() {
		return dictionaries[0].getProcessDefinition();
	}

	@Override
	public ProcessDictionaryItem<String, String> lookup(String key) {
		for (ProcessDictionary<String, String> dictionary : dictionaries) {
			if (dictionary.containsKey(key)) {
				return dictionary.lookup(key);
			}
		}
		return null;
	}

	@Override
	public Collection<String> itemKeys() {
		if (allItemKeys == null) {
			allItemKeys = new ArrayList<String>();

			for (ProcessDictionary<String, String> dictionary : dictionaries) {
				allItemKeys.addAll(dictionary.itemKeys());
			}
		}
		return allItemKeys;
	}

	@Override
	public Collection<ProcessDictionaryItem<String, String>> items() {
		if (allItems == null) {
			allItems = new ArrayList<ProcessDictionaryItem<String, String>>();
			Set<String> addedKeys = new HashSet<String>();

			for (ProcessDictionary<String, String> dictionary : dictionaries) {
				for (String key : dictionary.itemKeys()) {
					if (!addedKeys.contains(key)) {
						allItems.add(dictionary.lookup(key));
						addedKeys.add(key);
					}
				}
			}
		}
		return allItems;
	}

	@Override
	public boolean containsKey(String key) {
		return itemKeys().contains(key);
	}
}
