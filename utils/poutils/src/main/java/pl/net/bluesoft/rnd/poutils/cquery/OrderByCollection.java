package pl.net.bluesoft.rnd.poutils.cquery;

import pl.net.bluesoft.rnd.poutils.cquery.func.F;

import java.util.*;

/**
* User: Zachariel
* Date: 2011-07-29
* Time: 19:11:49
*/
public final class OrderByCollection<T> extends CQueryCollection<T> {
    private final CQueryCollection<T> collection;
    private final Comparator<? super T> comparator;

    OrderByCollection(CQueryCollection<T> collection, final Comparator<? super T> comparator) {
        this.collection = collection;
        this.comparator = comparator;
    }

    public <R extends Comparable<R>> OrderByCollection<T> thenBy(F<? super T, R> selector) {
        return new OrderByCollection<T>(this, new ComposedComparator<T>(this.comparator, new AscendingComparator<T, R>(selector)));
    }

    public <R extends Comparable<R>> OrderByCollection<T> thenByDescending(F<? super T, R> selector) {
        return new OrderByCollection<T>(this, new ComposedComparator<T>(this.comparator, new DescendingComparator<T, R>(selector)));
    }

    public <R> OrderByCollection<T> thenBy(F<? super T, R> selector, Comparator<? super R> comparator) {
        return new OrderByCollection<T>(this, new ComposedComparator<T>(this.comparator, new CustomAscentingComparator<T, R>(selector, comparator)));
    }

    public <R> OrderByCollection<T> thenByDescending(F<? super T, R> selector, Comparator<? super R> comparator) {
        return new OrderByCollection<T>(this, new ComposedComparator<T>(this.comparator, new CustomDescendingComparator<T, R>(selector, comparator)));
    }   

    @Override
    public List<T> toList() {
        List<T> sortedTable = new ArrayList<T>();
        for (T t : collection) {
            sortedTable.add(t);
        }
        Collections.sort(sortedTable, comparator);
        return sortedTable;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> iterator = collection.iterator();
            private ArrayList<T> sortedTable;
            private int i;

            @Override
            public boolean hasNext() {
                if (sortedTable != null) {
                    return i < sortedTable.size();
                }
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (sortedTable == null) {
                    sortedTable = new ArrayList<T>();
                    while (iterator.hasNext()) {
                        sortedTable.add(iterator.next());
                    }
                    Collections.sort(sortedTable, comparator);
                }
                return sortedTable.get(i++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
