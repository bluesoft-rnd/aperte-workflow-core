package pl.net.bluesoft.rnd.poutils.cquery;

import java.util.*;

public final class CustomEqualityMap<K, V> implements Map<K, V> {	
	private final Map<EqualityWrapper<K>, V> map = new HashMap<EqualityWrapper<K>, V>();
	private final EqualityComparer<? super K> comparer;
	private final EqualityWrapper<K> wrapper;

	public CustomEqualityMap(EqualityComparer<? super K> comparer) {
		this.comparer = comparer;
		this.wrapper = new EqualityWrapper<K>(comparer);
	}
	
	@Override
	public void clear() {
		map.clear();		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object o) {
		wrapper.setT((K)o);
		return map.containsKey(wrapper);
	}

	@Override
	public boolean containsValue(Object o) {
		return map.containsValue(o);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K,V>>() {
			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new Iterator<java.util.Map.Entry<K, V>>(){
					Iterator<java.util.Map.Entry<EqualityWrapper<K>, V>> iterator = map.entrySet().iterator();
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public java.util.Map.Entry<K, V> next() {
						final java.util.Map.Entry<EqualityWrapper<K>, V> e = iterator.next();
						return new java.util.Map.Entry<K, V>() {
							@Override
							public K getKey() {
								return e.getKey().getT();
							}

							@Override
							public V getValue() {
								return e.getValue();
							}

							@Override
							public V setValue(V v) {
								return e.setValue(v);
							}							
						};
					}

					@Override
					public void remove() {
						iterator.remove();
					}					
				};
			}

			@Override
			public int size() {
				return map.size();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object o) {
		wrapper.setT((K)o);
		return map.get(wrapper);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				return new Iterator<K>() {
					Iterator<EqualityWrapper<K>> iterator = map.keySet().iterator();
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public K next() {
						EqualityWrapper<K> w = iterator.next();
						if (w != null) {
							return w.getT();
						}
						return null;
					}

					@Override
					public void remove() {
						iterator.remove();
					}					
				};
			}

			@Override
			public int size() {
				return map.size();
			}
		};
	}

	@Override
	public V put(K key, V value) {
		return map.put(new EqualityWrapper<K>(key, comparer), value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> c) {
		for (Map.Entry<? extends K, ? extends V> e : c.entrySet()) {
			put(e.getKey(), e.getValue());
		}		
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object o) {
		wrapper.setT((K)o);
		return map.remove(wrapper);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}
}
