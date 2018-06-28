package com.development.commons.tools.xi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for helping with annotations.
 *
 * @author ddiodati
 */
public class AnnotationUtils {

    /**
     * Utility class, no instances allowed.
     */
    private AnnotationUtils() {
        super();
    }

    /**
     * Cache of methods by annotation for a class.
     */
    private static Map<Class<?>, Map<Class<? extends Annotation>, List<Method>>> methodCache = //
            new HashMap<Class<?>, Map<Class<? extends Annotation>, List<Method>>>();

    /**
     * Returns all public methods on a class(including super classes) with the give Annotation. Caches the methods for the
     * class instance so that future calls return immediately.
     *
     * @param srcClass        The class to scan.
     * @param annotationClass The Annotation class that will be on each method. This annotation must have a @Target annotation of
     *                        ElementType.METHOD.
     * @return A list of methods that have the Annotation present.
     */
    public static synchronized List<Method> getMethods(final Class<?> srcClass,
                                                       final Class<? extends Annotation> annotationClass) {
        Map<Class<? extends Annotation>, List<Method>> cachedResult = methodCache.get(srcClass);
        List<Method> result = cachedResult == null ? null : cachedResult.get(annotationClass);
        if (result == null) {
            result = new ArrayList<Method>();
            final Method[] methods = srcClass.getMethods();
            for (final Method m : methods) {
                if (m.isAnnotationPresent(annotationClass)) {
                    result.add(m);
                }
            }
            if (cachedResult == null) {
                cachedResult = new HashMap<Class<? extends Annotation>, List<Method>>();
                cachedResult.put(annotationClass, result);
                methodCache.put(srcClass, cachedResult);
            } else {
                cachedResult.put(annotationClass, result);
            }
        }
        return result;
    }

    /**
     * Cache of fields by annotation for a class.
     */
    private static Map<Class<?>, Map<Class<? extends Annotation>, List<Field>>> fieldCache = //
            new HashMap<Class<?>, Map<Class<? extends Annotation>, List<Field>>>();

    /**
     * Returns all public, private, protected, package protected fields on a class(including super classes) with the give
     * Annotation.
     * <p>
     * Caches the fields for the class instance so that future calls return immediately.
     *
     * @param srcClass        The class to scan.
     * @param annotationClass The Annotation class that will be on each field. This annotation must have a @Target annotation of
     *                        ElementType.METHOD.
     * @return A list of fields that have the Annotation present.
     */
    public static synchronized List<Field> getAllFields(final Class<?> srcClass,
                                                        final Class<? extends Annotation> annotationClass) {

        Map<Class<? extends Annotation>, List<Field>> cachedResult = fieldCache.get(srcClass);
        List<Field> result = cachedResult == null ? null : cachedResult.get(annotationClass);
        if (result == null) {
            result = new ArrayList<Field>();
            final List<Field> fields = ClassUtils.getAllFields(srcClass);
            for (final Field f : fields) {
                if (f.isAnnotationPresent(annotationClass)) {
                    result.add(f);
                }
            }
            if (cachedResult == null) {
                cachedResult = new HashMap<Class<? extends Annotation>, List<Field>>();
                cachedResult.put(annotationClass, result);
                fieldCache.put(srcClass, cachedResult);
            } else {
                cachedResult.put(annotationClass, result);
            }
        }
        return result;
    }

}
