package com.development.commons.tools.xi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.development.commons.tools.FileUtils;

/**
 * Utility class for class instances. $Id$
 *
 * @version $Revision$ $Date$
 * @author ddiodati
 *
 */
public class ClassUtils {

    /**
     * map to cache all fields for a given class.
     */
    private static Map<Class<?>, List<Field>> fieldCache = new HashMap<Class<?>, List<Field>>();

    /**
     *
     * Used by getShortClassName method. Return the 3rd matching group which is all the text after the last occurence of a DOT ".".
     */
    private static final int CLASSNAME_GROUP = 3;

    /**
     * No instances.
     */
    private ClassUtils() {
        super();
    }

    /**
     * Obtains all private,protected,package protected, Fields for a class(including its super classes).
     *
     * @param srcClass
     *            The class to check.
     * @return List of {@link Field} objects from the class.
     */
    public static synchronized List<Field> getAllFields(final Class<?> srcClass) {

        List<Field> results = fieldCache.get(srcClass);

        if (results == null) {
            results = new ArrayList<Field>();
            fieldCache.put(srcClass, results);

            Collections.addAll(results, srcClass.getDeclaredFields());

            Class<?> parent = srcClass.getSuperclass();
            while (parent != null && !(parent == Object.class)) {
                Collections.addAll(results, parent.getDeclaredFields());
                parent = parent.getSuperclass();
            }
        }

        return results;

    }

    /**
     * Utility method to strip out just the class name e.g. getShortClassName(com.foo.FooClass) will return FooClass.
     *
     * @param fullyQualifiedClassName
     *            the fully qualified class name
     * @return just the name of the class
     */
    public static String getShortClassName(final String fullyQualifiedClassName) {

        final Matcher classMatcher = Pattern.compile("((.*)\\.)?([^.]+)").matcher(fullyQualifiedClassName);
        if (!classMatcher.matches()) {
            return fullyQualifiedClassName;
        }

        return classMatcher.group(CLASSNAME_GROUP);
    }

    /**
     * Creates an identical copy of an object including all contained objects.
     *
     * NOTE: The object and all its children must implement the interface Serializable, otherwise you will an exception.
     *
     * @param oldObj
     *            The object to be cloned
     * @return a clone (identical copy) of oldObj
     *
     */
    public static Object deepCopy(final Object oldObj) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            // serialize and pass the object
            oos.writeObject(oldObj);
            oos.flush();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            // return the new object
            return ois.readObject();
        } catch (final NotSerializableException e) {
//            logger.error("Either the class, '" + oldObj.getClass().getName() + "', or one of its contained object classes does not implement Serializable", e);
            throw new RuntimeException(e);
        } catch (final IOException e) {
//            logger.error("IOException while trying to clone an instnce of class '" + oldObj.getClass().getName() + "'", e);
            throw new RuntimeException(e);
        } catch (final ClassNotFoundException e) {
//            logger.error("Failed to fine declaration for class '" + oldObj.getClass().getName() + "'", e);
            throw new RuntimeException(e);
        } finally {
            FileUtils.close(oos);
            FileUtils.close(ois);
        }
    }

    /**
     * Indicate whether the specified class is a test class (as oppossed to a deployed application class (e.g., this class is a mock for some API interface).
     *
     * @param type
     *            being checked
     * @return <code>true</code> if the specified type is a test class; <code>false</code>, otherwise
     * @since b1508
     */
    public static boolean isTest(final Class<?> type) {
        return whereFrom(type).contains("/testclasses/");
    }

    /**
     * Determine the location on the classpath where the specified type was found.
     *
     * @param type
     *            to be located
     * @return location of the specified type; "Unknown" if such location cannot be determined
     * @since b1508
     */
    private static String whereFrom(final Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        if (loader == null) {
            // Try the bootstrap classloader - obtained from the ultimate parent of the System Class Loader.
            loader = ClassLoader.getSystemClassLoader();
            while (loader != null && loader.getParent() != null) {
                loader = loader.getParent();
            }
        }
        if (loader != null) {
            final String name = type.getCanonicalName();
            final URL resource = loader.getResource(name.replace(".", "/") + ".class");
            if (resource != null) {
                return resource.toString();
            }
        }
        return "Unknown";
    }

}
