package pl.net.bluesoft.rnd.poutils.cquery;

import java.util.*;

public final class CustomEqualitySet<T> extends AbstractSet<T> {
	private Set<EqualityWrapper<T>> set = new HashSet<EqualityWrapper<T>>();
	private final EqualityComparer<? super T> comparer;
	private final EqualityWrapper<T> wrapper;
	
	public CustomEqualitySet(EqualityComparer<? super T> comparer) {
		this.comparer = comparer;
		this.wrapper = new EqualityWrapper<T>(comparer);
	}

	@Override
	public boolean add(T e) {
		return set.add(new EqualityWrapper<T>(e, comparer));		
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = false; 
		for (T e : c) {
			result = add(e) || result;
		}
		return result;
	}

	@Override
	public void clear() {
		set.clear();		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		wrapper.setT((T)o);
		return set.contains(wrapper);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e : c) {
			if (!contains(e)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<EqualityWrapper<T>> iterator = set.iterator();
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.next().getT();
			}

			@Override
			public void remove() {
				iterator.remove();				
			}			
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		wrapper.setT((T)o);
		return set.remove(wrapper);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false; 
		for (Object e : c) {
			if (remove(e)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Set<EqualityWrapper<T>> newSet = new HashSet<EqualityWrapper<T>>();
		boolean changed = false;
		for (EqualityWrapper<T> w : set) {
			if (c.contains(w.getT())) {
				newSet.add(w);
			}
			else {
				changed = true;
			}
		}
		this.set = newSet;
		return changed;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		Object[] a = new Object[set.size()];
		int i = 0;
		for (EqualityWrapper<T> w : set) {
			a[i++] = w.getT();
		}
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R[] toArray(R[] a) {		
		if (a.length < set.size()) {
			a = Arrays.copyOf(a, set.size());
		}
		int i = 0;
		for (EqualityWrapper<T> w : set) {
			a[i++] = (R)w.getT();
		}
		for (; i < a.length; ++i) {
			a[i] = null;
		}
		return a;
	}
}
