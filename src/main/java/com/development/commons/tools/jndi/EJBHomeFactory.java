/* $Id$ */
package com.development.commons.tools.jndi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class EJBHomeFactory {

    /**
     * Property to set the location of the jndiclient configuration file.
     */
    public static final String JNDI_CONFIG_DIR = "jndiclient.config.dir";

    /**
     * Property indicating the default JBoss configuration location as a URL.
     */
    public static final String JBOSS_CONFIG_URL = "jboss.server.config.url";

    private static final String JNP_DISABLE_DISCOVERY_NAME = "jnp.disableDiscovery";
    private static final String JNP_DISABLE_DISCOVERY_VALUE = "true";

    // Contains the EJBHome objects
    private final Map<String, Object> _ejbHomeCache = Collections
            .synchronizedMap(new HashMap<String, Object>());

    // Contains objects like ConnectionFactory / Queue / Topic
    private final Map<String, Object> _objectCache = Collections
            .synchronizedMap(new HashMap<String, Object>());

    private Context _context;
    private String _serverName;

    /**
     * Used server-side, by <code>JNDIService</code>
     *
     * @return EJBHomeFactory The EJBHomeFactory
     * @throws EJBHomeFactoryCreationException Thrown to allow to allow caller to failover and try another
     *                                         config in the case of failure.
     */
    public static EJBHomeFactory createFactory()
            throws EJBHomeFactoryCreationException {
        return new EJBHomeFactory();
    }

    /**
     * Used client-side (diff machine than server) eg by EJB Clients.
     *
     * @param serverName     The server name from the config file
     * @param configFileName The config file, which must be in the classpath. This is usually
     *                       jndiclient.xml.
     * @return EJBHomeFactory The EJBHomeFactory
     * @throws EJBHomeFactoryCreationException Thrown to allow to allow caller to failover and try another
     *                                         config in the case of failure.
     */
    public static EJBHomeFactory createClientFactory(final String serverName,
                                                     final String configFileName) throws EJBHomeFactoryCreationException {
        if (null == configFileName) {
            throw new EJBHomeFactoryCreationException("No config file name passed");
        }

        File configFileDir = null;
        if (null != System.getProperty(JNDI_CONFIG_DIR)) {
            configFileDir = new File(System.getProperty(JNDI_CONFIG_DIR));
        } else if (null != System.getProperty(JBOSS_CONFIG_URL)) {
            configFileDir = new File(URI.create(System.getProperty(JBOSS_CONFIG_URL)));
        } else {
            configFileDir = loadFromTomcatDefault();

            if (null == configFileDir) {
                final String message = "No file location directory found. Use the property "
                        + JNDI_CONFIG_DIR;
                throw new EJBHomeFactoryCreationException(message);
            }
        }
        return new EJBHomeFactory(serverName, configFileDir, configFileName);
    }

    private static File loadFromTomcatDefault() {
        final String catalinaBase = System.getProperty("catalina.base");
        if (null != catalinaBase) {
            final File tomcatSFConfDirectory = new File(catalinaBase + File.separator
                    + "sf_conf");
            if (tomcatSFConfDirectory != null && tomcatSFConfDirectory.isDirectory()) {
                return tomcatSFConfDirectory;
            }
        }

        return null;
    }

    private EJBHomeFactory() throws EJBHomeFactoryCreationException {
        try {
            _context = new InitialContext();
      /*if (_log.isDebugEnabled()) {
        _log.debug("EJBHomeFactory created using default initial context");
      }*/
        } catch (final NamingException exc) {
//      _log.error("Error creating EJBHomeFactory: ", exc);
            throw new EJBHomeFactoryCreationException(exc);
        } catch (final IllegalArgumentException exc) {
//      _log.error("Error creating EJBHomeFactory: ", exc);
            throw new EJBHomeFactoryCreationException(exc);
        }
    }

    private EJBHomeFactory(final String serverName, final File configFileDir,
                           final String configFileName) throws EJBHomeFactoryCreationException {
        _serverName = serverName;

        // load the jndi config file
        Properties properties = new Properties();
        InputStream configFileInputStream = null;
        try {
      /*if (_log.isDebugEnabled()) {
        _log.debug("trying to load server config: " + serverName
            + " from config file: " + configFileName);
      }*/
            final File configFile = new File(configFileDir, configFileName);
      /*if (_log.isDebugEnabled()) {
        _log.debug("Attempting to fetch configuration at "
            + configFile.toString());
      }*/
            if (!configFile.exists()) {
                final IOException exc = new IOException("Failed to load " + configFile);
//        _log.error("Error creating EJBHomeFactory: ", exc);
                throw new EJBHomeFactoryCreationException(exc);
            }
      /*if (_log.isDebugEnabled()) {
        _log.debug(configFileName + " loaded.");
      }*/
            configFileInputStream = new FileInputStream(configFile);
            properties = new XMLConfigurationParser().parse(configFileInputStream);
        } catch (final SAXException exc) {
//      _log.error("Error creating EJBHomeFactory", exc);
            throw new EJBHomeFactoryCreationException(exc);
        } catch (final ParserConfigurationException exc) {
//      _log.error("Error creating EJBHomeFactory", exc);
            throw new EJBHomeFactoryCreationException(exc);
        } catch (final IOException exc) {
//      _log.error("Error creating EJBHomeFactory", exc);
            throw new EJBHomeFactoryCreationException(exc);
        } finally {
            if (configFileInputStream != null) {
                try {
                    configFileInputStream.close();
                } catch (IOException e) {
//          _log.error("Failed to close file input stream", e);
                }
            }
        }

        final String user = getProperty(properties, "sf.jndi.user");
        String password = getProperty(properties, "sf.jndi.password");
        final String factory = getProperty(properties, "sf.jndi.factory");
        final String url = getProperty(properties, "sf.jndi.url");

        if (factory == null) {
            final String message = "JNDI <factory> must be defined in "
                    + configFileName + " for server: " + serverName;
//      _log.error(message);
            throw new EJBHomeFactoryCreationException(message);
        }

        if (url == null) {
            final String message = "JNDI <url> must be defined in " + configFileName
                    + " for server: " + serverName;
//      _log.error(message);
            throw new EJBHomeFactoryCreationException(message);
        }
//    if (_log.isDebugEnabled()) {
//      _log.debug("Connecting to " + url + " using " + factory);
//    }
        final Properties jndiProperties = new Properties();
        jndiProperties.put(JNP_DISABLE_DISCOVERY_NAME, JNP_DISABLE_DISCOVERY_VALUE);
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, factory);
        jndiProperties.put(Context.PROVIDER_URL, url);

        if ((user != null) && (!user.equals(""))) {
            jndiProperties.put(Context.SECURITY_PRINCIPAL, user);
            if (password == null) {
                password = "";
            }
            jndiProperties.put(Context.SECURITY_CREDENTIALS, password);
        }
        try {
            _context = new InitialContext(jndiProperties);// can throw
            // IllegalArgumentException
            // if port number is out of
            // range

        } catch (final NamingException exc) {
//      _log.error("Error creating EJBHomeFactory", exc);
            throw new EJBHomeFactoryCreationException(exc);
        } catch (final IllegalArgumentException exc) {
//      _log.error("Error creating EJBHomeFactory", exc);
            throw new EJBHomeFactoryCreationException(exc);
        }
    /*if (_log.isDebugEnabled()) {
      _log.debug("InitialContext created");
    }*/
    }

    /**
     * Used by server (cache breaks if using hot deploy, so we make it
     * configurable. See JNDIService)
     *
     * @param serviceName a String
     * @param cacheThis   a boolean
     * @return Object The lookup object
     * @throws NamingException Thrown when lookup failed.
     */
    public Object lookup(final String serviceName, final boolean cacheThis)
            throws NamingException {
        return lookup(serviceName, null, cacheThis);
    }

    /**
     * Used by server (cache breaks if using hot deploy, so we make it
     * configurable. See JNDIService)
     *
     * @param serviceName a String
     * @return Object The lookup object
     * @throws NamingException Thrown when lookup failed.
     */
    public Object lookup(final String serviceName) throws NamingException {
        final boolean cacheThis = false;
        return lookup(serviceName, cacheThis);
    }

    /**
     * Used by client (always caches the results of the lookup)
     *
     * @param serviceName  a String
     * @param serviceClass a service class
     * @return Object The lookup object
     * @throws NamingException Thrown when lookup failed.
     */
    public Object lookup(final String serviceName, final Class<?> serviceClass)
            throws NamingException {
        return lookup(serviceName, serviceClass, true);
    }

    /**
     * Used by client (always caches the results of the lookup)
     *
     * @param serviceName  a String
     * @param serviceClass a Class
     * @param cacheThis    a boolean
     * @return Object The lookup object
     * @throws NamingException Thrown when lookup failed.
     */
    public Object lookup(final String serviceName, final Class<?> serviceClass,
                         final boolean cacheThis) throws NamingException {
    /*if (_log.isDebugEnabled()) {
      _log.debug("EJBHomeFactory looking up: " + serviceName);
    }*/

        // have to use Object because this method can return both home and local
        // interfaces
        Object home = _ejbHomeCache.get(serviceName);
        if (home == null) {
            if (serviceClass != null) {
                home = PortableRemoteObject.narrow(_context.lookup(serviceName),
                        serviceClass);
            } else {
                home = _context.lookup(serviceName);
            }
            if (cacheThis) {
                _ejbHomeCache.put(serviceName, home);
            }
      /*if (_log.isDebugEnabled()) {
        _log.debug(" [LOADED]");
      }*/
        } else {
      /*if (_log.isDebugEnabled()) {
        _log.debug(" [FOUND IN CACHE]");
      }*/
        }
        return home;
    }

    /**
     * getProperty.
     *
     * @param properties a Properties
     * @param property   a String
     * @return String The property value
     */
    protected String getProperty(final Properties properties,
                                 final String property) {
        final String key = property.substring(0, property.indexOf(".") + 1)
                + _serverName
                + property.substring(property.indexOf("."), property.length());
        return properties.getProperty(key);
    }

    /**
     * getObject.
     *
     * @param objectName a String
     * @return Object
     * @throws NamingException when lookup failed.
     */
    public Object getObject(final String objectName) throws NamingException {
    /*if (_log.isDebugEnabled()) {
      _log.debug("EJBHomeFactory looking up: " + objectName);
    }*/
        Object object = null;
        if (_objectCache.containsKey(objectName)) {
            object = _objectCache.get(objectName);
      /*if (_log.isDebugEnabled()) {
        _log.debug(" [FOUND IN CACHE]");
      }*/
        } else {
            object = _context.lookup(objectName);
            _objectCache.put(objectName, object);
      /*if (_log.isDebugEnabled()) {
        _log.debug(" [LOADED]");
      }*/
        }
        return object;
    }

    /**
     * printAllDirectoryObjects.
     *
     * @throws NamingException when lookup failed.
     */
    public void printAllDirectoryObjects() throws NamingException {
        final NamingEnumeration<NameClassPair> names = _context.list("");
        while (names.hasMoreElements()) {
//      _log.error(names.next());
        }
    }

    /**
     * Returns the JNDI naming context
     *
     * @return The naming context
     * @deprecated Use JNDIService.lookup("jndiName") instead
     */
    @Deprecated
    public Context getContext() {
        return _context;
    }

    /**
     * setContext.
     *
     * @param context a Context
     */
    public void setContext(final Context context) {
        this._context = context;
    }

}
