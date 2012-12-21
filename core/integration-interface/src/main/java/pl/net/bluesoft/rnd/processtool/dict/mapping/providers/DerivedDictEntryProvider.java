package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.processtool.dict.mapping.DictEntryFilter;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.CustomDictDescription;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.util.lang.Classes.getProperty;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 23:26:25
 */
public class DerivedDictEntryProvider implements DictEntryProvider {
	private final CustomDictDescription dictDesc;

	private Map<String, Object> entries;

	public DerivedDictEntryProvider(CustomDictDescription dictDesc) {
		this.dictDesc = dictDesc;
	}

	@Override
	public Map getEntries() {
		return getEntries(null);
	}

	@Override
	public Map<String, ?> getEntries(DictEntryFilter entryFilter) {
		if (entryFilter != null) {
			Map<String, Object> result = new HashMap();
			for (Map.Entry<String, ?> e : entries.entrySet()) {
				if (entryFilter.filter(e.getValue())) {
					result.put(e.getKey(), e.getValue());
				}
			}
			return result;
		}
		return entries;
	}

	@Override
	public void prepareEntries(DictEntryProviderParams params) {
		if (entries != null) {
			return;
		}

		entries = new HashMap<String, Object>();

		for (Object entry : params.getDictMapper().getEntries(dictDesc.getBaseDictName()).values()) {
			Object key = getProperty(entry, dictDesc.getKeyProperty());
			Object value = dictDesc.getValueProperty() != null ? getProperty(entry, dictDesc.getValueProperty()) : key;
			entries.put(key != null ? String.valueOf(key) : null, value);
		}
	}

	@Override
	public Map<String, ?> getKeyValueMap() {
		return getKeyValueMap(null);
	}

	@Override
	public Map<String, ?> getKeyValueMap(DictEntryFilter entryFilter) {
		return getEntries(entryFilter);
	}

	@Override
	public Object getValue(String key) {
		return getKeyValueMap().get(key);
	}

	@Override
	public Object getEntryForDate(String key, Date date) {
		throw new UnsupportedOperationException();
	}
}
