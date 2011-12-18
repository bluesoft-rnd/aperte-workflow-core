package pl.net.bluesoft.rnd.poutils.cquery;

import java.util.Iterator;
import java.util.List;

public final class Grouping<K, V> extends CQueryCollection<V> {
	private final K key;
	private final List<V> values;
	
	public Grouping(K key, List<V> values) {
		this.key = key;
		this.values = values;
	}
	
	public K getKey() {
		return key;
	}

	@Override
	public Iterator<V> iterator() {
		return values.iterator();
	}
	
	@Override
	public List<V> toList() {
		return values;
	}
}
