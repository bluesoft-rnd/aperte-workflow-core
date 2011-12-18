package pl.net.bluesoft.rnd.poutils.cquery;

import java.util.Comparator;

/**
* User: Zachariel
* Date: 2011-07-29
* Time: 19:15:18
*/
final class ComposedComparator<T> implements Comparator<T> {
    private final Comparator<? super T> comparator1, comparator2;

    public ComposedComparator(Comparator<? super T> comparator1, Comparator<? super T> comparator2) {
        this.comparator1 = comparator1;
        this.comparator2 = comparator2;
    }

    @Override
    public int compare(T x, T y) {
        int cmp = comparator1.compare(x, y);
        if (cmp == 0) {
            return comparator2.compare(x, y);
        }
        return cmp;
    }
}
