package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.util.lang.Lang;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-07-10
 */
public abstract class CollectionComparer<T> {
	public boolean compare(Collection<? extends T> items1, Collection<? extends T> items2) {
		if (items1 == null) {
			items1 = Collections.emptyList();
		}
		if (items2 == null) {
			items2 = Collections.emptyList();
		}
		if (Lang.equals(items1, items2)) {
			return true;
		}
		if (items1.size() != items2.size()) {
			return false;
		}

		Map<String, T> oldMap = new HashMap<String,T>();
		Map<String, T> newMap = new HashMap<String, T>();

		for (T item : items1) {
			oldMap.put(getKey(item), item);
		}

		for (T item : items2) {
			String key = getKey(item);

			if (!oldMap.containsKey(key)){
				return false;
			}
			newMap.put(key, item);
		}
		for (Map.Entry<String, T> entry : oldMap.entrySet()) {
			String key = entry.getKey();
			if (!newMap.containsKey(key)) {
				return false;
			}
			if (!compareItems(entry.getValue(), newMap.get(key))) {
				return false;
			}
		}
		return true;
	}

	protected abstract String getKey(T item);
	protected abstract boolean compareItems(T item1, T item2);
}