package com.development.commons.tools.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.ConstructorUtils;

public class ObjectCreator {

    private ObjectCreator() {};

    /**
     * Method to create a object dynamically.
     * @param name The full class name.
     * @param args Constructor arguments.
     * @return Object instance.
     * @throws InvocationTargetException If we can not create the object.
     */
    public static final Object createObject(String name, Object... args) throws InvocationTargetException {
        Object result = null;

        try {
            Class clazz = Class.forName(name,true, Thread.currentThread().getContextClassLoader());
            if(clazz != null ) {
                result = ConstructorUtils.invokeConstructor(clazz, args);
            }

        } catch (ClassNotFoundException e) {
            throw new InvocationTargetException(e);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
           throw new InvocationTargetException(e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        } catch (InstantiationException e) {
            throw new InvocationTargetException(e);
        }

        return result;
    }

    /**
     * Method to create a object dynamically.
     * @param name The full class name.
     * @return Object instance.
     * @throws InvocationTargetException If we can not create the object.
     */
    public static final Object createObject(String name) throws InvocationTargetException {
        Object result = null;

        try {
            Class clazz = Class.forName(name,true, Thread.currentThread().getContextClassLoader());
            if(clazz != null ) {
                result = clazz.newInstance();
            }

        } catch (ClassNotFoundException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
           throw new InvocationTargetException(e);
        } catch (InstantiationException e) {
            throw new InvocationTargetException(e);
        }

        return result;
    }

}
