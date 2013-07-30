package pl.net.bluesoft.rnd.processtool.model.dict;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.util.lang.cquery.CQuery;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.*;

/**
 * User: POlszewski
 * Date: 2012-09-12
 * Time: 22:31
 */
public class MultiLevelDictionary implements ProcessDictionary {
	private ProcessDictionary[] dictionaries;
	private Collection<String> allItemKeys;
	private Collection<ProcessDictionaryItem> allItems;

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
	public String getDefaultName() {
		return dictionaries[0].getDefaultName();
	}

	@Override
	public String getName(String languageCode) {
		return dictionaries[0].getName(languageCode);
	}

	@Override
	public String getName(Locale locale) {
		return getName(locale.toString());
	}

	@Override
	public ProcessDictionaryItem lookup(String key) {
		for (ProcessDictionary dictionary : dictionaries) {
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

			for (ProcessDictionary dictionary : dictionaries) {
				allItemKeys.addAll(dictionary.itemKeys());
			}
		}
		return Collections.unmodifiableCollection(allItemKeys);
	}

	@Override
	public Collection<ProcessDictionaryItem> items() {
		if (allItems == null) {
			allItems = new ArrayList<ProcessDictionaryItem>();
			Set<String> addedKeys = new HashSet<String>();

			for (ProcessDictionary dictionary : dictionaries) {
				for (String key : dictionary.itemKeys()) {
					if (!addedKeys.contains(key)) {
						allItems.add(dictionary.lookup(key));
						addedKeys.add(key);
					}
				}
			}
		}
		return Collections.unmodifiableCollection(allItems);
	}

	@Override
	public boolean containsKey(String key) {
		return itemKeys().contains(key);
	}

	@Override
	public List<ProcessDictionaryItem> sortedItems(final String languageCode) {
		return (List)CQuery.from(items()).orderBy(new F<ProcessDictionaryItem, String>() {
			@Override
			public String invoke(ProcessDictionaryItem item) {
				return item.getValueForCurrentDate().getValue(languageCode);
			}
		}).toList();
	}
}
