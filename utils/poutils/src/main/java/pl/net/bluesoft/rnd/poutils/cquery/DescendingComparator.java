package pl.net.bluesoft.rnd.poutils.cquery;

import pl.net.bluesoft.rnd.poutils.cquery.func.F;

import java.util.Comparator;

/**
* User: Zachariel
* Date: 2011-07-29
* Time: 19:14:45
*/
final class DescendingComparator<T, R extends Comparable<R>> implements Comparator<T> {
    private final F<? super T, R> selector;

    public DescendingComparator(F<? super T, R> selector) {
        this.selector = selector;
    }

    @Override
    public int compare(T x, T y) {
        R sx = selector.invoke(x);
        if (sx == null) {
            return +1;
        }
        R sy = selector.invoke(y);
        if (sy == null) {
            return -1;
        }
        return -sx.compareTo(sy);
    }
}
