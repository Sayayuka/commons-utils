/* $Id$ */
package com.development.commons.tools.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

public class JNDIService {

    private static EJBHomeFactory __homeFactory;

    static {
        try {
            __homeFactory = EJBHomeFactory.createFactory();
        } catch (final EJBHomeFactoryCreationException exc) {
        }
    }

    /**
     * Retrieves the named object.
     *
     * @param jndiName
     *            The name of the object to look up
     * @return The object bound to the given name, or <code>null</code> if not found. A NamingException will be logged if the object is not bound.
     * @throws NamingException
     *             Thrown when lookup failed.
     */
    public static Object lookup(final String jndiName) throws NamingException {
        Object object = null;
        final boolean cacheMe = true;// should come from config file. need to test whether caching will break hot-deploy -pk
        object = __homeFactory.lookup(jndiName, cacheMe);
        return object;
    }

    /**
     * Returns the JNDI naming context
     *
     * @return the jndi naming context
     * @deprecated Use JNDIService.lookup("jndiName") directly instead of via the context
     */
    @Deprecated
    public static Context getContext() {
        return __homeFactory.getContext();
    }

    /**
     * setContext.
     *
     * @param context
     *            a Context
     */
    public static void setContext(final Context context) {
        __homeFactory.setContext(context);
    }

    private JNDIService() {
        super();
    }

}
