package pl.net.bluesoft.rnd.poutils.cquery;

final class EqualityWrapper<T> {
	private T t;
	private final EqualityComparer<? super T> comparer;
	
	EqualityWrapper(EqualityComparer<? super T> comparer) {
		this.comparer = comparer;
	}
	
	public EqualityWrapper(T t, EqualityComparer<? super T> comparer) {
		this.t = t;
		this.comparer = comparer;
	}
		
	public T getT() {
		return t;
	}

	void setT(T t) {
		this.t = t;
	}

	public EqualityComparer<? super T> getComparer() {
		return comparer;
	}

	@Override
	public int hashCode() {
		return comparer.hashCode(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		return obj instanceof EqualityWrapper && comparer.equals(t, ((EqualityWrapper<T>)obj).t);
	}

	@Override
	public String toString() {		
		return t.toString();
	}
}
