package com.development.commons.utility.comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CompositeComparator<T> implements Comparator<T> {

    private static final Comparator<?>[] NO_COMPARATORS = new Comparator[0];
    private final Comparator<T>[] delegates;

    public CompositeComparator(Comparator... delegates) {
        if (delegates == null) {
            this.delegates = (Comparator[]) NO_COMPARATORS;
        } else {
            this.delegates = (Comparator[]) (new Comparator[delegates.length]);
            System.arraycopy(delegates, 0, this.delegates, 0, delegates.length);
        }
    }

    public CompositeComparator(Iterable<Comparator<T>> delegates) {
        if (delegates == null) {
            this.delegates = (Comparator[]) NO_COMPARATORS;
        } else {
            List<Comparator<T>> list = new ArrayList();
            Iterator i$ = delegates.iterator();

            while (i$.hasNext()) {
                Comparator<T> comparator = (Comparator) i$.next();
                list.add(comparator);
            }
            this.delegates = (Comparator[]) (list.toArray(new Comparator[list.size()]));
        }
    }

    public int compare(T obj1, T obj2) {
        int result = 0;
        Comparator[] arr$ = this.delegates;
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Comparator<T> delegate = arr$[i$];
            result = delegate.compare(obj1, obj2);
            if (result != 0) {
                break;
            }
        }
        return result;
    }

}

