package com.development.commons.tools.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An abstract base class for all Comparator implementations that wish to do null safe compares. This treats nulls as smaller than any value (so they would sort first). Extend this
 * class and then implement compareNonNull() and it will only get invoked if both values are not null.
 *
 * @author J.C. Hamlin
 *
 * @param <T>
 *            the type of object this compares
 */
public abstract class AbstractNullSafeComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    public static enum NullBehavior {
        /**
         * Nulls sort first, so null < any T. This is the defualt behavior.
         */
        NULLS_SORT_FIRST,
        /**
         * Nulls sort last, so null > any T.
         */
        NULLS_SORT_LAST,
        /**
         * Nulls will consistently throw a {@link NullPointerException}.
         */
        NULLS_THROW_NULLPOINTEREXCEPTION;
    };

    private NullBehavior nullBehavior;

    /**
     * Constrcut a new instance and treat nulls as {@link NullBehavior#NULLS_SORT_FIRST}.
     */
    public AbstractNullSafeComparator() {
        this(NullBehavior.NULLS_SORT_FIRST);
    }

    /**
     * Constrcut a new instance and treat nulls as specified by the nullBehavior argument to the constructor.
     *
     * @param nullBehavior
     *            the behavior to exibit when handling nulls
     */
    public AbstractNullSafeComparator(final NullBehavior nullBehavior) {
        super();
        this.nullBehavior = nullBehavior;
    }

    public final int compare(final T o1, final T o2) {
        final int result;
        if (o1 == null) {
            if (o2 == null) {
                switch (nullBehavior) {
                case NULLS_THROW_NULLPOINTEREXCEPTION:
                    throw new NullPointerException("Both arguments to compare() were null");
                default:
                    result = 0;
                    break;
                }
            } else {
                switch (nullBehavior) {
                case NULLS_THROW_NULLPOINTEREXCEPTION:
                    throw new NullPointerException("Attempt to compare null to " + o2);
                case NULLS_SORT_FIRST:
                    result = -1;
                    break;
                default:
                    result = 1;
                    break;
                }
            }
        } else {
            if (o2 == null) {
                switch (nullBehavior) {
                case NULLS_THROW_NULLPOINTEREXCEPTION:
                    throw new NullPointerException("Attempt to compare " + o1 + " to null");
                case NULLS_SORT_FIRST:
                    result = 1;
                    break;
                default:
                    result = -1;
                    break;
                }
            } else {
                result = compareNonNull(o1, o2);
            }
        }
        return result;
    }

    /**
     * Compares its two non-null arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the
     * second.
     *
     * @param o1
     *            the first object to compare
     * @param o2
     *            the second object to compare
     *
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    protected abstract int compareNonNull(final T o1, final T o2);

}
