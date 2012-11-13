package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.processtool.dict.mapping.DictEntryFilter;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.DictDescription;

import java.util.Date;
import java.util.Map;

import static pl.net.bluesoft.util.lang.Classes.copyProperties;

/**
 * User: POlszewski
 * Date: 2012-06-15
 * Time: 15:58
 */
public class LazyLoadDictEntryProvider implements DictEntryProvider {
	private final DictDescription dictDescription;
	private DictEntryProvider dictEntryProvider;
	private DictEntryProviderParams params;

	public LazyLoadDictEntryProvider(DictDescription dictDescription) {
		this.dictDescription = dictDescription;
	}

	@Override
	public Map getEntries() {
		return getDictEntryProvider().getEntries();
	}

	@Override
	public Map getEntries(DictEntryFilter entryFilter) {
		return getDictEntryProvider().getEntries(entryFilter);
	}

	@Override
	public Map<String, ?> getKeyValueMap() {
		return getDictEntryProvider().getKeyValueMap();
	}

	@Override
	public Map<String, ?> getKeyValueMap(DictEntryFilter entryFilter) {
		return getDictEntryProvider().getKeyValueMap(entryFilter);
	}

	@Override
	public Object getValue(String key) {
		return getDictEntryProvider().getValue(key);
	}

	@Override
	public Object getEntryForDate(String key, Date date) {
		return getDictEntryProvider().getEntryForDate(key, date);
	}

	@Override
	public void prepareEntries(DictEntryProviderParams params) {
		this.params = new DictEntryProviderParams();
		copyProperties(this.params, params);
	}

	private DictEntryProvider getDictEntryProvider() {
		if (dictEntryProvider == null) {
			dictEntryProvider = dictDescription.createDictEntryProvider();
			dictEntryProvider.prepareEntries(params);
		}
		return dictEntryProvider;
	}
}
