package com.development.commons.tools.comparator;

/**
 * This Comparator compares two arrays of Comparable objects using a {@link ComparableObjectComparator}. It is null-safe and thread-safe.
 *
 * @author J.C. Hamlin
 *
 * @param <T>
 *            the type of objects the array contains that implement Comparable
 */
public class ComparableArrayComparator<T extends Comparable<T>> extends ArrayComparator<T> {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new ComparableArrayComparator using the default {@link ComparableObjectComparator} to compare the objects in the array.
     */
    public ComparableArrayComparator() {
        super(new ComparableObjectComparator<T>());
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
