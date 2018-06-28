package com.development.commons.tools.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapMaker;

/**
 * Fast creation of object proxy instances
 *
 * @author dhu
 *
 */
public abstract class TurboProxy {
    /** default concurrecy level, the same to ConcurrentHashMap */
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /** cache of proxy class mappings */
    private static Map<Class<?>, ProxyClass> proxyClassCache = new MapMaker().concurrencyLevel(DEFAULT_CONCURRENCY_LEVEL).weakKeys().weakValues().makeMap();

    /** parameter types of a proxy class constructor */
    private static final Class[] constructorParams = { InvocationHandler.class };

    /**
     * Create an instance of object proxy
     *
     * @param obj
     *            the object to be proxied
     * @param ih
     *            the invocation handler for the proxy
     * @return the proxy instance
     */
    public static Object newProxyInstance(final Object obj, final InvocationHandler ih) {
        final Class<?> oc = obj.getClass();
        ProxyClass proxyClass = proxyClassCache.get(oc);

        if (proxyClass == null) {
            final Class<?> genClass = Proxy.getProxyClass(oc.getClassLoader(), getInterfaces(oc));
            proxyClass = new ProxyClass(genClass, ProxyAccessHelper.needsNewInstanceCheck(genClass));
            proxyClassCache.put(oc, proxyClass);
        }

        final Constructor<?> cons = proxyClass.cons;
        if (System.getSecurityManager() != null && proxyClass.needsNewInstanceCheck) {
            // create proxy instance with doPrivilege as the proxy class may
            // implement non-public interfaces that requires a special permission
            return AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    return newInstance(cons, ih);
                }
            });
        } else {
            return newInstance(cons, ih);
        }
    }

    /**
     * Helper method to create instance from constructor
     *
     * @param cons
     *            constructor
     * @param ih
     *            invocation handler
     * @return the object instance
     */
    private static Object newInstance(final Constructor<?> cons, final InvocationHandler ih) {
        try {
            return cons.newInstance(new Object[] { ih });
        } catch (final IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (final InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (final InvocationTargetException e) {
            final Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new InternalError(t.toString());
            }
        }
    }

    /**
     * Core presentation of the proxy class
     */
    private static class ProxyClass {
        private Constructor<?> cons;
        private final boolean needsNewInstanceCheck;

        ProxyClass(final Class<?> clazz, final boolean needsNewInstanceCheck) {
            try {
                cons = clazz.getConstructor(constructorParams);
            } catch (final NoSuchMethodException e) {
                throw new InternalError(e.toString());
            }
            this.needsNewInstanceCheck = needsNewInstanceCheck;
        }
    }

    /**
     * Get all the interface of a class
     *
     * @param c
     *            class
     * @return array of interfaces
     */
    private static Class<?>[] getInterfaces(final Class<?> c) {
        Class<?> clazz = c;
        final List<Class<?>> result = new ArrayList<Class<?>>();
        if (c.isInterface()) {
            result.add(c);
        } else {
            do {
                addInterfaces(clazz, result);
                clazz = clazz.getSuperclass();
            } while (clazz != null);
        }
        for (int i = 0; i < result.size(); ++i) {
            addInterfaces(result.get(i), result);
        }
        return result.toArray(new Class<?>[result.size()]);
    }

    /**
     * Add a classes' interfaces into a list
     *
     * @param c
     *            class
     * @param list
     *            of interfaces
     */
    private static void addInterfaces(final Class<?> c, final List<Class<?>> list) {
        for (final Class<?> intf : c.getInterfaces()) {
            if (!list.contains(intf)) {
                list.add(intf);
            }
        }
    }

    /**
     * Help with Java class permission check
     */
    private static class ProxyAccessHelper {
        // These system properties are defined to provide a short-term
        // workaround if customers need to disable the new security checks.
        static final boolean allowNewInstance;
        static {
            allowNewInstance = getBooleanProperty("sun.reflect.proxy.allowsNewInstance");
        }

        private static boolean getBooleanProperty(final String key) {
            final String s = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(key);
                }
            });
            return Boolean.valueOf(s);
        }

        @SuppressWarnings("restriction")
        private static boolean needsNewInstanceCheck(final Class<?> proxyClass) {
            if (allowNewInstance) {
                return false;
            }

            // Todo
            /*if (sun.reflect.misc.ReflectUtil.isNonPublicProxyClass(proxyClass)) {
                for (final Class<?> intf : proxyClass.getInterfaces()) {
                    if (!Modifier.isPublic(intf.getModifiers())) {
                        return true;
                    }
                }
            }*/
            return false;
        }
    }

}
