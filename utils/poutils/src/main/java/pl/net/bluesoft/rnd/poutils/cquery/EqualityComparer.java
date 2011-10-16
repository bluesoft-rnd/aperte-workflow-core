package pl.net.bluesoft.rnd.poutils.cquery;

public interface EqualityComparer<T> {
	boolean equals(T t1, T t2);
	int hashCode(T t);
}
