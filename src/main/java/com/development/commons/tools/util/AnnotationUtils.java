package com.development.commons.tools.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.development.commons.tools.xi.util.ClassUtils;

/**
 * Utility class for helping with annotations.
 *
 * @author ddiodati
 *
 */
public class AnnotationUtils {

    /** Utility class, no instances allowed. */
    private AnnotationUtils() {
    }

    /**
     * static map to cache fields or methods(with a specific annotation)
     *  for a given class.
     */
    private static Map<Class,Map<Class<? extends Annotation>,List<Object>>> cache =
        new HashMap<Class,Map<Class<? extends Annotation>,List<Object>>>();

    /**
     * Returns all public methods on a class(including super classes)
     * with the give Annotation.
     * Caches the methods for the class instance so that future calls
     * return immediately.
     *
     * @param srcClass The class to scan.
     * @param annotationClass The Annotation class that will be on each method.
     * This annotation must have a @Target annotation of ElementType.METHOD.
     *
     * @return A list of methods that have the Annotation present.
     */
    public static synchronized List<Method> getMethods(Class srcClass,
            Class<? extends Annotation> annotationClass) {
        List<Method> results = getCachedObjects(srcClass, annotationClass);

        if ( results == null) {
            results = new ArrayList<Method>();

            Method[] methods = srcClass.getMethods();
            for(Method m : methods) {
                if ( m.isAnnotationPresent(annotationClass) ) {
                    results.add(m);
                }
            }

            putCachedObjects(srcClass, annotationClass, results);
        }

        return results;
    }




    /**
     * Returns all public, private, protected, package protected fields on a
     * class(including super classes) with the give Annotation.
     *
     * Caches the fields for the class instance so that future calls
     * return immediately.
     *
     * @param srcClass The class to scan.
     * @param annotationClass The Annotation class that will be on each field.
     * This annotation must have a @Target annotation of ElementType.METHOD.
     * @return A list of fields that have the Annotation present.
     */
    public static synchronized List<Field> getAllFields(Class srcClass,
            Class<? extends Annotation> annotationClass) {

        List<Field> results = getCachedObjects(srcClass, annotationClass);

        if ( results == null) {
            results = new ArrayList<Field>();


            List<Field> fields = ClassUtils.getAllFields(srcClass);
            for(Field f : fields) {
                if ( f.isAnnotationPresent(annotationClass) ) {
                    results.add(f);
                }
            }
            putCachedObjects(srcClass, annotationClass, results);
        }

        return results;
    }

    private static List getCachedObjects(Class srcClass, Class<? extends Annotation>annoClass)
    {

       Map<Class<? extends Annotation>,List<Object>> annotations = cache.get(srcClass);

       List<Object> reflectObjs = null;

       if(annotations != null) {
          reflectObjs =  annotations.get(annoClass);
       }

       return reflectObjs;

    }


    private static void putCachedObjects(Class srcClass, Class<? extends Annotation>annoClass, List objs)
    {

       Map<Class<? extends Annotation>,List<Object>> annotations = cache.get(srcClass);

       if(annotations == null) {
          annotations = new HashMap<Class<? extends Annotation>,List<Object>>();
          cache.put(srcClass, annotations);
       }

       annotations.put(annoClass, objs);

    }

}
