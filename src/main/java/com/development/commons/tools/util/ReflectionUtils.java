/*
 * ----------------------------------------------------------------------------- Copyright (c) 2010 Plateau Systems, Ltd.
 *
 * This software and documentation is the confidential and proprietary information of Plateau Systems. Plateau Systems makes no representation or warranties about the suitability
 * of the software, either expressed or implied. It is subject to change without notice.
 *
 * U.S. and international copyright laws protect this material. No part of this material may be reproduced, published, disclosed, or transmitted in any form or by any means, in
 * whole or in part, without the prior written permission of Plateau Systems. -----------------------------------------------------------------------------
 */

package com.development.commons.tools.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// import edu.umd.cs.findbugs.annotations.CheckForNull;
// import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
// import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Utilities for using reflection.
 *
 * @author J.C. Hamlin
 * @since plateaucommon2
 */
// @DefaultAnnotation(NonNull.class)
public final class ReflectionUtils {

    private static class ReflectionUtilsCache<T extends AccessibleObject> {
        private static final long serialVersionUID = 1L;

        public ReflectionUtilsCache() {
            super();
        }
    }

    private ReflectionUtils() {
        super();
    }

    /**
     * Get a field by name.
     */
    public static Field getField(final Class<?> clazz, final String name) throws NoSuchFieldException {
        Field result = null;
        for (final Field field : getFields(clazz)) {
            if (field.getName().equalsIgnoreCase(name)) {
                result = field;
                break;
            }
        }
        if (result == null) {
            throw new NoSuchFieldException(name);
        }
        return result;
    }

    /**
     * Make an {@link AccessibleObject} accessible by calling its {@link AccessibleObject#setAccessible(boolean)} with <code>true</code> using the
     * <code>AccessController.doPrivileged()</code>.
     */
    public static void setAccessible(final AccessibleObject member) {
        try {
            AccessController.doPrivileged(new PrivilegedAccess(member));
        } catch (final PrivilegedActionException e) {
            final Throwable cause = e.getException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Problem setting member accessible: " + member, cause);
        }
    }

    /**
     * Gets the value of a field identified by its field name.
     */
    // @CheckForNull
    public static Object getFieldValue(final Object object, final String name) throws NoSuchFieldException {
        final Field field = getField(object.getClass(), name);
        final Object result = getFieldValue(object, field);
        return result;
    }

    /**
     * A version of {@link Field#get} that allows access to private members.
     */
    // @CheckForNull
    public static Object getFieldValue(final Object object, final Field field) {
        final Object result;
        try {
            if (!field.isAccessible()) {
                setAccessible(field);
            }
            result = field.get(object);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Problem getting value from " + field, e);
        }
        return result;
    }

    /**
     * Set a field identified by name to the specified value.
     */
    public static void setFieldValue(final Object object, final String name, /* @CheckForNull */final Object value) throws NoSuchFieldException {
        final Field field = getField(object.getClass(), name);
        setFieldValue(object, field, value);
    }

    /**
     * Set a field identified by name to the specified value.
     */
    public static void setFieldValue(final Object object, final String name, /* @CheckForNull */final String value) throws NoSuchFieldException {
        final Field field = getField(object.getClass(), name);
        final Class<?> type = field.getType();
        /*
         * not treating Date from String, provide your own Date objects in the setFieldValue(Object, String, Object) variant if (Date.class.isAssignableFrom(type)) {
         * setFieldValue(object, field, Timestamp.parseDate(value)); } else
         */if (type == Boolean.class || type == boolean.class) {
            setFieldValue(object, field, Boolean.valueOf(toBoolean(value)));
        } else {
            setFieldValue(object, field, value);
        }
    }

    /**
     * Convert a string value to a boolean for the more common forms of what is considered true, such as (1, on, yes, true, T, Y) all case insensitively compared. If the String
     * passed in equals "maybe", the output of this method is undetermined. (that's a bad joke)
     *
     * @param s
     *            the string to convert
     *
     * @return true if any of the appropriate positive values are found.
     */
    private static boolean toBoolean(final String s) {
        if (s == null) {
            return false;
        }

        if (s.equalsIgnoreCase("1") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("T") || s.equalsIgnoreCase("Y")) {
            return true;
        }

        return false;
    }

    /**
     * A version of {@link Field#set} that allows access to private members. Avoids {@link IllegalAccessException}.
     */
    public static void setFieldValue(final Object object, final Field field, /* @CheckForNull */final Object value) {
        try {
            if (!field.isAccessible()) {
                setAccessible(field);
            }
            field.set(object, value);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Problem setting value to field " + field + " value: " + value, e);
        }
    }

    /**
     * Gets a pseudo field identified by name to the specified value. A pseudo field is defined as a field that has a corresponding public getter method.
     *
     * @param object
     *            the instance whose pseudo field value is to be gotten
     * @param name
     *            the name of the pseudo field
     * @throws NoSuchFieldException
     *             if the pseudo field does not exist
     * @throws InvocationTargetException
     *             if an underlying exception occurs while getting the pseudo field value via its getter method
     */
    // @CheckForNull
    public static Object getPseudoFieldValue(final Object object, final String name) throws NoSuchFieldException, InvocationTargetException {
        final Method getter = getGetterMethod(object.getClass(), name);
        if (getter == null) {
            throw new NoSuchFieldException(name);
        }
        return getPseudoFieldValue(object, getter);
    }

    /**
     * Gets a pseudo field value via the specified getter method.
     *
     * @param object
     *            the instance whose pseudo field value is to be gotten
     * @param getter
     *            the getter method of the pseudo field
     * @throws InvocationTargetException
     *             if an underlying exception occurs while getting the pseudo field value via its getter method
     */
    // @CheckForNull
    public static Object getPseudoFieldValue(final Object object, final Method getter) throws InvocationTargetException {
        return invokeMethod(object, getter);
    }

    /**
     * Sets a pseudo field identified by name to the specified value. A pseudo field is defined as a field that has a corresponding public setter method.
     *
     * @param object
     *            the instance whose pseudo field value is to be set
     * @param name
     *            the name of the pseudo field
     * @param value
     *            the object representation of the field value
     * @throws NoSuchFieldException
     *             if the pseudo field does not exist
     * @throws InvocationTargetException
     *             if an underlying exception occurs while setting the pseudo field via its setter method
     */
    public static void setPseudoFieldValue(final Object object, final String name, final Object value) throws NoSuchFieldException, InvocationTargetException {
        final Method setter = getSetterMethod(object.getClass(), name);
        if (setter == null) {
            throw new NoSuchFieldException(name);
        }
        setPseudoFieldValue(object, setter, value);
    }

    /**
     * Sets a pseudo field identified by setter method to the specified value.
     *
     * @param object
     *            the instance whose pseudo field value is to be set
     * @param setter
     *            the setter method of the pseudo field
     * @param value
     *            the object representation of the field value
     * @throws InvocationTargetException
     *             if an underlying exception occurs while setting the pseudo field via its setter method
     */
    public static void setPseudoFieldValue(final Object object, final Method setter, /* @CheckForNull */final Object value) throws InvocationTargetException {
        invokeMethod(object, setter, value);
    }

    /**
     * Get a method by name.
     */
    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parametertypes) throws NoSuchMethodException {
        Method result = null;
        for (final Method method : getMethods(clazz)) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parametertypes)) {
                result = method;
                break;
            }
        }
        if (result == null) {
            throw new NoSuchMethodException(name);
        }

        return result;
    }

    /**
     * Get a static method by name.
     */
    public static Method getStaticMethod(final Class<?> clazz, final String name, final Class<?>... parametertypes) throws NoSuchMethodException {
        Method result = null;
        for (final Method method : getStaticMethods(clazz)) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parametertypes)) {
                result = method;
                break;
            }
        }
        if (result == null) {
            throw new NoSuchMethodException(name);
        }

        return result;
    }

    /**
     * Returns an unmodifiable list of the specified items. This will copy the collection into a new list and return an unmodifiable view of that list.
     */
    // @SafeVarargs
    // @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification =
    // "FB bug related to #3506402: @CheckForNull on varargs parameter not taking effect")
    private static <T> List<T> unmodifiableList(/* @CheckForNull */final T... items) {
        final List<T> result;
        if (items == null) {
            result = Collections.unmodifiableList(new ArrayList<T>());
        } else {
            result = Collections.unmodifiableList(new ArrayList<T>(Arrays.asList(items)));
        }
        return result;
    }

    /**
     * Returns an unmodifiable list of the specified items. This will copy the collection into a new list and return an unmodifiable view of that list.
     */
    private static <T> List<T> unmodifiableList(/* @CheckForNull */final Collection<T> items) {
        final List<T> result;
        if (items == null) {
            result = Collections.unmodifiableList(new ArrayList<T>());
        } else {
            result = Collections.unmodifiableList(new ArrayList<T>(items));
        }
        return result;
    }

    /**
     * A version of {@link Method#invoke} that allows access to private members. Avoids {@link IllegalAccessException}.
     */
    // @CheckForNull
    public static Object invokeMethod(final Object object, final String methodName) throws InvocationTargetException, NoSuchMethodException {
        final Method method = getMethod(object.getClass(), methodName);
        final Object result;
        try {
            setAccessible(method);
            result = method.invoke(object);
            // } catch (final InvocationTargetException e) {
            // throw e;
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("Problem invoking method " + methodName, e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Problem invoking method " + methodName, e);
        }
        return result;
    }

    /**
     * A version of {@link Method#invoke} that allows access to private members. Avoids {@link IllegalAccessException}.
     */
    // @CheckForNull
    public static Object invokeStaticMethod(final Object object, final String methodName) throws InvocationTargetException, NoSuchMethodException {
        final Method method = getStaticMethod(object.getClass(), methodName);
        final Object result;
        try {
            setAccessible(method);
            result = method.invoke(object);
            // } catch (final InvocationTargetException e) {
            // throw e;
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("Problem invoking method " + methodName, e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Problem invoking method " + methodName, e);
        }
        return result;
    }

    /**
     * A version of {@link Method#invoke} that allows access to private members. Avoids {@link IllegalAccessException}.
     */
    // @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification =
    // "FB bug related to #3506402: @CheckForNull on varargs parameter not taking effect")
    // @CheckForNull
    public static Object invokeMethod(final Object object, final Method method, /* @CheckForNull */final Object... args) throws InvocationTargetException {
        final Object result;
        try {
            setAccessible(method);
            result = method.invoke(object, args);
            // } catch (final InvocationTargetException e) {
            // throw e;
        } catch (final IllegalArgumentException e) {
            final List<Object> list = unmodifiableList(args);
            throw new RuntimeException("Problem invoking method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
        } catch (final IllegalAccessException e) {
            final List<Object> list = unmodifiableList(args);
            throw new RuntimeException("Problem invoking method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
        }
        return result;
    }

    /**
     * A version of {@link Method#invoke} that allows access to private members. Avoids {@link IllegalAccessException}.
     */
    // @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification =
    // "FB bug related to #3506402: @CheckForNull on varargs parameter not taking effect")
    // @CheckForNull
    public static Object invokeStaticMethod(final Method method, /* @CheckForNull */final Object... args) throws InvocationTargetException {
        final Object result;
        try {
            setAccessible(method);
            result = method.invoke(null, args);
            // } catch (final InvocationTargetException e) {
            // throw e;
        } catch (final IllegalArgumentException e) {
            final List<Object> list = unmodifiableList(args);
            throw new RuntimeException("Problem invoking static method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
        } catch (final IllegalAccessException e) {
            final List<Object> list = unmodifiableList(args);
            throw new RuntimeException("Problem invoking static method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
        }
        return result;
    }

    /**
     * Get a constructor by args.
     */
    public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... parametertypes) throws NoSuchMethodException {
        final Constructor<T> result = clazz.getDeclaredConstructor(parametertypes);
        return result;
    }

    /**
     * A version of {@link Constructor#newInstance} that allows access to private constructors. Avoids {@link IllegalAccessException}.
     */
    public static <T> T newInstance(final Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException {
        final Constructor<T> constructor = getConstructor(clazz);
        final T result = newInstance(constructor);
        return result;
    }

    /**
     * A version of {@link Constructor#newInstance} that allows access to private constructors. Avoids {@link IllegalAccessException}.
     */
    // @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification =
    // "FB bug related to #3506402: @CheckForNull on varargs parameter not taking effect")
    public static <T> T newInstance(final Constructor<T> constructor, /* @CheckForNull */final Object... args) throws InvocationTargetException, InstantiationException {
        T result;
        try {
            setAccessible(constructor);
            result = constructor.newInstance(args);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Problem invoking constructor " + constructor + " args: (" + (args == null ? "" : Arrays.asList(args)) + ")", e);
        }
        return result;
    }

    /**
     * List of all the fields of the class, and all of its superclasses. NOTE: This returns the most common kind of fields which by default excludes {@link Modifier#STATIC} fields.
     */
    public static List<Field> getFields(final Class<?> clazz) {
        return getFields(clazz, Modifier.STATIC);
    }

    /**
     * List of all the methods of the class, and all of its superclasses. NOTE: This returns the most common kind of methods which by default excludes {@link Modifier#STATIC}
     * methods.
     */
    public static List<Method> getMethods(final Class<?> clazz) {
        return getMethods(clazz, Modifier.STATIC);
    }

    /**
     * List of all the fields of the class and all of its superclasses.
     */
    public static List<Field> getFields(final Class<?> clazz, final int excludedModifiers) {
        final List<Field> result = new ArrayList<Field>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            final List<Field> fields = getDeclaredFields(c, excludedModifiers);
            result.addAll(0, fields);
        }
        return result;
    }

    /**
     * List of all the fields of the class (not not its superclasses).
     */
    public static List<Field> getDeclaredFields(final Class<?> clazz, final int excludedModifiers) {
        final List<Field> result = new ArrayList<Field>();
        for (final Field f : clazz.getDeclaredFields()) {
            final int fieldModifiers = f.getModifiers();
            if ((fieldModifiers & excludedModifiers) == 0) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * List of all the methods of the class and all of its superclasses.
     */
    public static List<Method> getMethods(final Class<?> clazz, final int excludedModifiers) {
        final List<Method> result = new ArrayList<Method>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            final List<Method> methods = getDeclaredMethods(c, 0, excludedModifiers);
            result.addAll(0, methods);
        }
        return result;
    }

    /**
     * List of all the methods of the class and all of its superclasses.
     */
    public static List<Method> getStaticMethods(final Class<?> clazz) {
        final List<Method> result = new ArrayList<Method>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            final List<Method> methods = getDeclaredMethods(c, Modifier.STATIC, 0);
            result.addAll(0, methods);
        }
        return result;
    }

    /**
     * List of all the methods of the class (not its superclasses).
     */
    private static List<Method> getDeclaredMethods(final Class<?> clazz, final int includedModifiers, final int excludedModifiers) {
        final List<Method> result = new ArrayList<Method>();
        for (final Method m : clazz.getDeclaredMethods()) {
            final int methodModifiers = m.getModifiers();
            if (includedModifiers != 0 && ((methodModifiers & includedModifiers) == 0)) {
                continue;
            }
            if ((methodModifiers & excludedModifiers) == 0) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Returns the first public getter for the specified property, or <code>null</code> for none
     */
    // @CheckForNull
    public static Method getGetterMethod(final Class<?> clazz, final String propertyName) {
        Method result = null;
        final int excludedModifiers = Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC;
        final String name1 = "get" + propertyName;
        final String name2 = "is" + propertyName;
        MAIN_LOOP: for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (final Method m : c.getDeclaredMethods()) {
                final int methodModifiers = m.getModifiers();
                if ((methodModifiers & excludedModifiers) != 0) {
                    continue;
                }
                final String methodName = m.getName();
                if ((!name1.equalsIgnoreCase(methodName)) && (!name2.equalsIgnoreCase(methodName))) {
                    continue;
                }
                if (isGetterMethod(m)) {
                    result = m;
                    break MAIN_LOOP;
                }
            }
        }
        return result;
    }

    /**
     * Returns the first public setter for the specified property, or <code>null</code> for none
     */
    // @CheckForNull
    public static Method getSetterMethod(final Class<?> clazz, final String propertyName) {
        Method result = null;
        final int excludedModifiers = Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC;
        final String name = "set" + propertyName;
        MAIN_LOOP: for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (final Method m : c.getDeclaredMethods()) {
                final int methodModifiers = m.getModifiers();
                if ((methodModifiers & excludedModifiers) != 0) {
                    continue;
                }
                final String methodName = m.getName();
                if (!name.equalsIgnoreCase(methodName)) {
                    continue;
                }
                if (m.getParameterTypes().length != 1) {
                    continue;
                }
                result = m;
                break MAIN_LOOP;
            }
        }
        return result;
    }

    /**
     * Returns the first public setter for the specified property and parameter type, or <code>null</code> for none
     */
    // @CheckForNull
    public static Method getSetterMethod(final Class<?> clazz, final String propertyName, final Class<?> type) {
        Method result = null;
        final int excludedModifiers = Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC;
        final String name = "set" + propertyName;
        MAIN_LOOP: for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (final Method m : c.getDeclaredMethods()) {
                final int methodModifiers = m.getModifiers();
                if ((methodModifiers & excludedModifiers) != 0) {
                    continue;
                }
                final String methodName = m.getName();
                if (!name.equalsIgnoreCase(methodName)) {
                    continue;
                }
                if (m.getParameterTypes().length != 1) {
                    continue;
                }
                if (!m.getParameterTypes()[0].isAssignableFrom(type)) {
                    continue;
                }
                result = m;
                break MAIN_LOOP;
            }
        }
        return result;
    }

    /**
     * Generate a map of "getter" methods keyed by the property name the getters wrap. Note that if there is both a "get" and an "is" for the same boolean property, the last method
     * declared in the class wins. "is" prefixed getters are ignored for non-boolean return types.
     *
     * @param c
     *            class from which to generate getter map
     * @return map of getter Methods keyed by property name
     */
    public static Map<String, Method> getPropertyGetterMap(final Class<?> c) {
        // returning a map: key == property name, value == getter method
        final Map<String, Method> result = new LinkedHashMap<String, Method>();

        // getters are only valid if public, non-static (and concrete)
        final List<Method> methods = ReflectionUtils.getMethods(c, Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC);
        for (final Method m : methods) {
            // is this a getter? (starts with is/get, has no args, returns a value)
            final Class<?> returnType = m.getReturnType();
            if (returnType != null && m.getParameterTypes().length == 0) {
                final String methodName = m.getName();
                String propertyName;
                if (methodName.startsWith("is") && (returnType.equals(Boolean.class) || returnType.equals(boolean.class))) {
                    propertyName = methodName.substring(2);
                } else if (methodName.startsWith("get")) {
                    propertyName = methodName.substring(3);
                } else {
                    // ignore this method - not a recognized getter
                    continue;
                }
                propertyName = toLowerCase(propertyName.substring(0, 1)) + propertyName.substring(1);
                result.put(propertyName, m);
            }
        }
        return result;
    }

    /**
     * Null-safe conversion of a string to lower case (using default locale).
     *
     * @param s
     *            string to lower-case
     * @return lower-cased string or null if original string was null
     */
    private static String toLowerCase(final String s) {
        final String result;
        if (s != null && s.length() > 0) {
            result = s.toLowerCase(Locale.getDefault());
        } else {
            result = s;
        }
        return result;
    }

    /**
     * Get the first annotation of the declared type from this object.
     */
    // @CheckForNull
    public static <A extends Annotation> A getAnnotation(final Object object, final Class<A> annotationClass) {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null in getAnnotation()");
        }
        if (annotationClass == null) {
            throw new IllegalArgumentException("annotationClass cannot be null in getAnnotation()");
        }
        final Class<? extends Object> clazz = object.getClass();
        A result = null;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            result = c.getAnnotation(annotationClass);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Get all the fields of this object and its superclasses that are annotated with this annotation.
     */
    public static List<Field> getFieldsWithAnnotation(final Class<?> clazz, final Class<?>... annotationClasss) {
        final List<Field> result = new ArrayList<Field>();
        final List<Field> fields = getFields(clazz);
        for (final Field f : fields) {
            if (isAnnotationPresent(f.getAnnotations(), annotationClasss)) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Get all the fields of this object and its superclasses that are not annotated with this annotation.
     */
    public static List<Field> getFieldsWithoutAnnotation(final Class<?> clazz, final Class<?>... annotationClasss) {
        final List<Field> result = new ArrayList<Field>();
        final List<Field> fields = getFields(clazz);
        for (final Field f : fields) {
            if (!isAnnotationPresent(f.getAnnotations(), annotationClasss)) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Get all the methods of this object and its superclasses that are annotated with this annotation.
     */
    public static List<Method> getMethodsWithAnnotation(final Class<?> clazz, final Class<?>... annotationClasses) {
        final List<Method> result = new ArrayList<Method>();
        final List<Method> methods = getMethods(clazz);
        for (final Method m : methods) {
            if (isAnnotationPresent(m.getAnnotations(), annotationClasses)) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Get all the methods of this object and its superclasses that are not annotated with this annotation.
     */
    public static List<Method> getMethodsWithoutAnnotation(final Class<?> clazz, final Class<?>... annotationClasses) {
        final List<Method> result = new ArrayList<Method>();
        final List<Method> methods = getMethods(clazz);
        for (final Method m : methods) {
            if (!isAnnotationPresent(m.getAnnotations(), annotationClasses)) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Determine if any of the annotations requested are present in the specified array of annotations.
     */
    public static boolean isAnnotationPresent(final Annotation[] annotations, final Class<?>... annotationClasses) {
        boolean result = false;
        for (final Annotation a : annotations) {
            for (final Class<?> c : annotationClasses) {
                if (c.isAssignableFrom(a.getClass())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Is this a getter method?
     */
    public static boolean isGetterMethod(final Method m) {
        final boolean result;
        final String methodName = m.getName();
        final Class<?> returnType = m.getReturnType();
        final Class<?>[] parameterTypes = m.getParameterTypes();
        if (methodName.startsWith("is") && (returnType.equals(Boolean.class) || returnType.equals(boolean.class)) && parameterTypes.length == 0) {
            result = true;
        } else if (methodName.startsWith("get") && !Void.TYPE.equals(returnType) && parameterTypes.length == 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Is this a setter method?
     */
    public static boolean isSetterMethod(final Method m) {
        final boolean result;
        final String methodName = m.getName();
        final Class<?> returnType = m.getReturnType();
        final Class<?>[] parameterTypes = m.getParameterTypes();
        if (methodName.startsWith("set") && Void.TYPE.equals(returnType) && parameterTypes.length == 1) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Find all the interfaces that the given class implements including all of its superclasses.
     *
     * @param clazz
     *            class to inspect
     * @return array of interfaces the class implements, or zero-length array if none
     */
    // @NonNull
    public static Class<?>[] getAllInterfaces(/* @NonNull */final Class<?> clazz) {
        final Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        Class<?> c = clazz;
        while (c != null) {
            final Class<?>[] cInterfaces = c.getInterfaces();
            interfaces.addAll(Arrays.asList(cInterfaces));
            c = c.getSuperclass();
        }
        final Class<?>[] result = interfaces.toArray(new Class<?>[interfaces.size()]);
        return result;
    }

    /**
     * A {@link java.security.PrivilegedAction} that sets the {@link AccessibleObject#setAccessible(boolean)} flag that will allow access protected and private members of a
     * {@link Class}.
     */
    private static final class PrivilegedAccess implements PrivilegedExceptionAction<Object> {
        private final AccessibleObject member;

        private PrivilegedAccess(final AccessibleObject member) {
            this.member = member;
        }

        @Override
        public Object run() {
            member.setAccessible(true);
            return null;
        }
    }

    /**
     * Determines if the method is a member of the class.
     */
    public static boolean isAMethodOf(final Class<?> clazz, final Method method) {
        boolean result;
        try {
            clazz.getMethod(method.getName(), method.getParameterTypes());
            result = true;
        } catch (final NoSuchMethodException e) {
            result = false;
        }
        return result;
    }

    /**
     * Create a new proxy instance around a target for a given InvocationHandler, automatically proxying the PUBLIC interfaces for the target, and throwing on any additional given
     * interfaces.
     *
     * @param target
     *            object to proxy
     * @param h
     *            handler for the proxy
     * @param extraInterfaces
     *            any additional interfaces the proxy should implement
     * @return proxied object
     * @throws IllegalArgumentException
     *             if the proxy cannot be created
     */
    public static Object newProxyInstance(final Object target, final InvocationHandler h, final Class<?>... extraInterfaces) throws IllegalArgumentException {
        final Object result = newProxyInstance(target, h, null, extraInterfaces);
        return result;
    }

    /**
     * Create a new proxy instance around a target for a given InvocationHandler, automatically proxying the PUBLIC interfaces for the target, and throwing on any additional given
     * interfaces.
     *
     * @param target
     *            object to proxy
     * @param h
     *            handler for the proxy
     * @param knownNonPublicInterfaces
     *            collection of interfaces (fully-qualified names) known to be non-public and thus ignorable
     * @param extraInterfaces
     *            any additional interfaces the proxy should implement
     * @return proxied object
     * @throws IllegalArgumentException
     *             if the proxy cannot be created
     */
    public static Object newProxyInstance(final Object target, final InvocationHandler h, /* @CheckForNull */final Collection<String> knownNonPublicInterfaces,
            final Class<?>... extraInterfaces) throws IllegalArgumentException {
        final Class<?> targetClass = target.getClass();
        try {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            final Class<?>[] interfaces = ReflectionUtils.getAllInterfaces(targetClass);
            final Set<Class<?>> interfaceSet = new LinkedHashSet<Class<?>>();
            addOnlyPublicInterfaces(interfaceSet, interfaces);

            // add additional interfaces
            for (final Class<?> c : extraInterfaces) {
                interfaceSet.add(c);
            }

            final Class<?>[] interfaceArray = interfaceSet.toArray(new Class<?>[interfaceSet.size()]);
            try {
                final Object result = Proxy.newProxyInstance(loader, interfaceArray, h);
                return result;
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Could not proxy " + targetClass + " with interfaces " + interfaceSet + " for classLoader " + loader, e);
            }
        } catch (final IllegalAccessError e) {
            throw new IllegalArgumentException("Could not proxy " + targetClass, e);
        }
    }

    /**
     * .
     *
     * @param source
     *            source
     * @param knownNonPublicInterfaces
     *            knownNonPublicInterfaces
     * @return Collection
     */
    protected static Collection<Class<?>> removeKnownNonPublicInterfaces(final Collection<Class<?>> source, final Collection<String> knownNonPublicInterfaces) {
        final Collection<Class<?>> result;
        if (knownNonPublicInterfaces.isEmpty()) {
            result = source;
        } else {
            result = new LinkedHashSet<Class<?>>();
            for (final Class<?> c : source) {
                final String className = c.getCanonicalName();
                if (!knownNonPublicInterfaces.contains(className)) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    private static void addOnlyPublicInterfaces(final Set<Class<?>> interfaceSet, final Class<?>[] interfaces) {
        for (final Class<?> i : interfaces) {
            final int modifiers = i.getModifiers();
            if ((modifiers & Modifier.PUBLIC) == 0) {
                final Class<?>[] superInterfaces = i.getInterfaces();
                addOnlyPublicInterfaces(interfaceSet, superInterfaces);
            } else {
                interfaceSet.add(i);
            }
        }
    }

}
