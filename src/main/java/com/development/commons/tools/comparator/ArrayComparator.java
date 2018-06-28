package com.development.commons.tools.comparator;

import java.util.Comparator;

/**
 * This Comparator compares two arrays of Objects using the specified Comparator. It is null-safe (i.e. either array can be null), but if you want this to be
 * completely null-safe, the comparator used to compare the elements should also be null-safe. And if the provided element Comparator is thread-safe, then this
 * class is thread-safe too.
 *
 * @author J.C. Hamlin
 *
 * @param <T>
 *            the type of objects the array contains that implement Comparable
 */
public class ArrayComparator<T> extends AbstractNullSafeComparator<T[]> {

    private static final long serialVersionUID = 1L;

    final Comparator<T> elementComparator;

    /**
     * Construct a new ArrayComparator using the specified Comparator to compare the objects in the array.
     *
     * @param elementComparator
     *            the Comparator used to compare the objects in the array
     */
    public ArrayComparator(final Comparator<T> elementComparator) {
        super();
        this.elementComparator = elementComparator;
    }

    @Override
    protected int compareNonNull(final T[] o1, final T[] o2) {
        int pos = 0;
        // skip equal elements
        while (pos < o1.length && pos < o2.length && o1[pos].equals(o2[pos])) {
            pos++;
        }

        // end of o1 reached, o2 longer
        if (o1.length == pos && o2.length > pos) {
            return -1;
        }

        // end of o2 reached, o1 longer
        if (o2.length == pos && o1.length > pos) {
            return 1;
        }

        // end of both sets reached: equal
        if (o1.length == pos && o2.length == pos) {
            return 0;
        }

        // first position found, where sets differ
        return elementComparator.compare(o1[pos], o2[pos]);
    }

}
