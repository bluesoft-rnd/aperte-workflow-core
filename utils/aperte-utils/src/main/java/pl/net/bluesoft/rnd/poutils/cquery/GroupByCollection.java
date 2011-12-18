package pl.net.bluesoft.rnd.poutils.cquery;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GroupByCollection<K, V> extends CQueryCollection<Grouping<K, V>> {
	private final Map<K, List<V>> groups;
	
	public GroupByCollection(Map<K, List<V>> groups) {
		this.groups = groups;
	}

	@Override
	public Iterator<Grouping<K, V>> iterator() {
		return new Iterator<Grouping<K, V>>() {
			Iterator<Map.Entry<K, List<V>>> iterator = groups.entrySet().iterator();
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Grouping<K, V> next() {
				Map.Entry<K, List<V>> kv = iterator.next();
				return new Grouping<K, V>(kv.getKey(), kv.getValue());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();					
			}
			
		};
	}
	
	public Map<K, List<V>> toMap() {
		return groups;
	}
}