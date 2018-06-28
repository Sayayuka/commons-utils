package com.development.commons.tools.comparator;

/**
 * <p>
 * Compare two {@link Comparable} objects, either or both which can be null (i.e. it is null-safe). It is also thread-safe. This can be used in a type-safe way with any type or
 * interface implements Comparable, such as String, Integer, Number, etc.
 * </p>
 *
 * <p>
 * For example, to use this to compare String objects, you would write this:
 * </p>
 * <code>
 * Comparator&lt;String&gt; stringComparator = new ComparableObjectComparator&lt;String&gt;();
 * </code>
 * <p>
 * Or for Integer objects, you would write this:
 * </p>
 * <code>
 * Comparator&lt;Integer&gt; integerComparator = new ComparableObjectComparator&lt;Integer&gt;();
 * </code>
 *
 * <p>
 * </p>
 *
 * @author J.C. Hamlin
 *
 * @param <T>
 *            the type of object compared by the comparator
 */
public class ComparableObjectComparator<T extends Comparable<T>> extends AbstractNullSafeComparator<T> {

    private static final long serialVersionUID = 1L;

    @Override
    protected int compareNonNull(final T t1, final T t2) {
        return t1.compareTo(t2);
    }

}
