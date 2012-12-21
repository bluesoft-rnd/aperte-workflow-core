package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.processtool.dict.mapping.DictEntryFilter;

import java.util.Date;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 16:38:30
 */
public interface DictEntryProvider<EntryType> {
	Map<String, EntryType> getEntries();
	Map<String, EntryType> getEntries(DictEntryFilter entryFilter);
	Map<String, ?> getKeyValueMap();
	Map<String, ?> getKeyValueMap(DictEntryFilter entryFilter);
	Object getValue(String key);
	EntryType getEntryForDate(String key, Date date);

	void prepareEntries(DictEntryProviderParams params);
}
