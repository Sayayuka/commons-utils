package com.development.commons.tools.xi.util.resource;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.xml.DomDriver;

@XStreamAlias("index")
public class AnnotationIndex {
    private static final String GAS_INDEX_FILE = "gas.xml";
    private static final String EXT_JAR = ".jar";
    private static final String FILE_PREFIX = "file:";

    @XStreamImplicit
    private Map<String, Set<String>> index;

    private static XStream x;
    /**
     * .
     */
    public static final AnnotationIndex ALL = new AnnotationIndex();
    /**
     * .
     */
    public static final Map<String, AnnotationIndex> ALL_FILE_INDEX = new HashMap<String, AnnotationIndex>();

    static {
        try {
            Class.forName("org.xmlpull.v1.XmlPullParserException");
            Class.forName("org.xmlpull.v1.XmlPullParserFactory");
            Class.forName("org.xmlpull.mxp1.MXParser");
//            logger.info("GAS uses XPP which is a fast XML parser");
            x = new XStream();
        } catch (final ClassNotFoundException e) {
//            logger.warn("GAS has to use DomDriver since XPP is not in class path");
            x = new XStream(new DomDriver());
        }

        x.processAnnotations(new Class[] { AnnotationIndex.class });
        buildIndexes();
    }

    /**
     * .
     */
    public AnnotationIndex() {
        index = new HashMap<String, Set<String>>();
    }

    /**
     * .
     *
     * @return Map
     */
    public Map<String, Set<String>> getIndex() {
        return index;
    }

    /**
     * .
     *
     * @param index
     *            index
     */
    public void setIndex(final Map<String, Set<String>> index) {
        this.index = index;
    }

    /**
     * .
     *
     * @param annotationIndex
     *            annotationIndex
     */
    public void merge(final AnnotationIndex annotationIndex) {
        if (annotationIndex == null || annotationIndex.index == null) {
            return;
        }
        for (final String annotationName : annotationIndex.index.keySet()) {
            Set<String> classNames = index.get(annotationName);
            if (classNames == null) {
                classNames = new HashSet<String>();
            }
            classNames.addAll(annotationIndex.index.get(annotationName));
            index.put(annotationName, classNames);
        }
    }

    @Override
    public String toString() {
        /*
         * Sort the index for output. Using a separate adapter class allows for
         * backward compatibility with existing (released) annotation index
         * files.
         */
        return new SortedAnnotationIndex(this).toString();
    }

    /**
     * .
     *
     * @param annotationName
     *            annotationName
     * @param className
     *            className
     */
    public void putNewSeekedClass(final String annotationName, final String className) {
        Set<String> classNames = index.get(annotationName);
        if (classNames == null) {
            classNames = new HashSet<String>();

        }
        if (!classNames.contains(className)) {
            classNames.add(className);
            index.put(annotationName, classNames);
        }
    }

    /**
     * Retrieve the set of classes containing ANY of the specified annotations
     *
     * @param classLoader
     *            used to load the classes
     * @param annotations
     *            to identify returned classes
     * @return set of <code>Class</code> containing any specified annotation
     * @since b1508
     */
    public Set<Class<Object>> findClassesWithAnnotations(final ClassLoader classLoader, final Class... annotations) {
        final Set<Class<Object>> classes = new HashSet<Class<Object>>();

        for (final Class annotation : annotations) {
            classes.addAll(findClassesWithAnnotation(annotation, classLoader));
        }

        return classes;
    }

    /**
     * .
     *
     * @param annotationClass
     *            annotationClass
     * @param classLoader
     *            classLoader
     * @return Set
     */
    public Set<Class<Object>> findClassesWithAnnotation(final Class annotationClass, final ClassLoader classLoader) {
        final Set<Class<Object>> classes = new HashSet<Class<Object>>();
        if (index != null) {
            final Set<String> classNames = index.get(annotationClass.getName());
            if (classNames != null) {
                for (final String className : classNames) {
                    try {
                        final Class clz = classLoader.loadClass(className);
                        classes.add(clz);
                    } catch (final Throwable e) {
//                        logger.error("fail to load class:[" + className + "] with error: [" + e.getMessage() + "]");
                    }
                }
            }
        }
        return classes;
    }

    private static AnnotationIndex fromXML(final URL url) {
        try {
            return (AnnotationIndex) x.fromXML(url);
        } catch (final Exception e) {
//            logger.fatal("=================================== got error while parsing: " + url.toString());
//            logger.catching(e);
            return null;
        }
    }

    private static synchronized void buildIndexes() {
        try {
            final long b = System.currentTimeMillis();
            final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(GAS_INDEX_FILE);
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
//                logger.info("build gas index from " + url);
                buildIndex(url);
            }
//            logger.info("*********************  GAS initialize takes " + (System.currentTimeMillis() - b) + " ms ***************************");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * .
     *
     * @param url
     *            url
     */
    public static synchronized void buildIndex(final URL url) {
        final AnnotationIndex annotationIndex = fromXML(url);
        ALL.merge(annotationIndex);
        String path = null;
        if (!isJar(url)) {
//            logger.info(" not a jar: " + url.getPath());
            path = getDirectoryName(url);
            // continue;
        } else {
            path = getJarName(url);
        }
//        logger.info("load gas from " + path);
        ALL_FILE_INDEX.put(path, annotationIndex);
    }

    /**
     * .
     *
     * @param url
     *            url
     * @return String
     */
    public static String getJarName(final URL url) {
        String urlPath = url.getPath();
        if (urlPath.startsWith(FILE_PREFIX)) {
            // strip off the file: if it exists
            urlPath = urlPath.substring(FILE_PREFIX.length());
        }
        return urlPath.substring(0, urlPath.lastIndexOf(EXT_JAR)) + EXT_JAR;
    }

    private static String getDirectoryName(final URL url) {
        String urlPath = url.getPath();
        if (urlPath.startsWith(FILE_PREFIX)) {
            // strip off the file: if it exists
            urlPath = urlPath.substring(FILE_PREFIX.length());
        }
        return urlPath;
    }

    /**
     * .
     *
     * @param url
     *            url
     * @return boolean
     */
    public static boolean isJar(final URL url) {
        return url.getPath().indexOf(EXT_JAR) > 0;
    }

}
