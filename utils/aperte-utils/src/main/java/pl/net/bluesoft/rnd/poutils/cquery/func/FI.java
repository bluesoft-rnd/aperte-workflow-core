package pl.net.bluesoft.rnd.poutils.cquery.func;

public interface FI<T, R> {
	R invoke(T x, int index);
}
