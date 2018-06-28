package com.development.commons.tools.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapts an Iterator of Strings into an older Enumeration the fly.
 *
 * This is needed for some older apis.
 *
 * @author ddiodati
 *
 */
public class IteratorToEnum implements Enumeration<String> {
    /**
     * The iterator that is being wrapped.
     */
    private Iterator<String> iter;

    /**
     * Constructor.
     * @param iter The iterator to adapt.
     */
    public IteratorToEnum(Iterator<String> iter)
    {
        this.iter = iter;
    }

    /**
     * Checks if the iterator has more String elements.
     * @return true if there are more elements otherwise false.
     */
    public boolean hasMoreElements()
    {
        return iter.hasNext();
    }

    /**
     * Returns the next string token.
     * @return Next string element.
     */
    public String nextElement()
    {
        return iter.next();
    }

}
