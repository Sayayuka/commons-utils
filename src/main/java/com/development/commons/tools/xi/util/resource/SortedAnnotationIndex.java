/* -----------------------------------------------------------------------------
 * Copyright (c) 2015 SuccessFactors, an SAP Company.
 * -----------------------------------------------------------------------------
 */

package com.development.commons.tools.xi.util.resource;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * An adapter for the annotation index so that output can be in sorted order. This allows the annotation index generation to be idempotent (unchanging without local cause).
 *
 * @since b1508
 */

@XStreamAlias("index")
class SortedAnnotationIndex {

    @XStreamImplicit
    private final Map<String, Set<String>> index;

    /** XStream for XML processing of index */
    private static XStream x;

    static {
        x = new XStream(new DomDriver());
        x.processAnnotations(new Class[] { SortedAnnotationIndex.class });
        x.alias("set", SortedSet.class);
    }

    /**
     * Adapter constructor.
     *
     * @param annotationIndex
     *            to sort
     */
    SortedAnnotationIndex(final AnnotationIndex annotationIndex) {
        final Map<String, Set<String>> unsortedIndex = annotationIndex.getIndex();
        index = new TreeMap<String, Set<String>>();

        for (final String annotation : unsortedIndex.keySet()) {
            final Set<String> sortedClasses = new TreeSet<String>(unsortedIndex.get(annotation));
            index.put(annotation, sortedClasses);
        }
    }

    @Override
    public String toString() {
        return x.toXML(this);
    }

}
