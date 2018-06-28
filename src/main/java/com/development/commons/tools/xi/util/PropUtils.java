package com.development.commons.tools.xi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.development.commons.tools.FileUtils;
import com.development.commons.tools.StringUtils;

/**
 * Utility class for Property objects.
 *
 * @author ddiodati
 *
 */
public class PropUtils {

    /**
     * No instances.
     */
    private PropUtils() {
        super();
    }

    /**
     * Hash map to store all system properties
     */
    private static Map<String, String> _properties = null;

    static {
        _properties = new HashMap<String, String>();

        // Now load in any of the System properties.
        // We do this after loading the config file so that we can override
        // items via the command line
        final Properties props = System.getProperties();
        for (final Object element : props.keySet()) {
            final String key = (String) element;
            _properties.put(key, props.getProperty(key));
        }
    }

    /**
     * Returns an Object value for the given property.
     *
     * @param key
     *            property name
     * @return the boolean value
     */
    public static Object get(final String key) {
        return _properties.get(key);
    }

    /**
     * Returns an object for the given property. If no property is configured, defaultValue will be returned.
     *
     * @param key
     *            property name
     * @param defaultValue
     *            default value
     * @return the object
     */
    public static Object get(final String key, final Object defaultValue) {
        final Object value = get(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns a boolean value for the given property. If no property is configured, defaultValue will be returned.
     *
     * @param key
     *            property name
     * @param defaultValue
     *            default value
     * @return the boolean value
     */
    public static boolean getBoolean(final String key, final boolean defaultValue) {
        final String value = (String) get(key);
        if (value == null) {
            return defaultValue;
        }

        if ("off".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            return false;
        } else if ("on".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
            return true;
        } else {
//            _log.error("Invalid setting for " + key + ". Must be one of: off/on/0/1/false/true/no/yes.");
            return defaultValue;
        }
    }

    /**
     * Returns a string value for the given property.
     *
     * @param key
     *            property name
     * @return the string value
     */
    public static String getString(final String key) {
        return (String) get(key);
    }

    /**
     * Returns a string value for the given property. If no property is configured, defaultValue will be returned.
     *
     * @param key
     *            property name
     * @param defaultValue
     *            default value
     * @return the string value
     */
    public static final String getString(final String key, final String defaultValue) {
        return (String) get(key, defaultValue);
    }

    /**
     * Returns a list of values for the given property.
     *
     * @param key
     *            property name
     * @return the list
     */
    public static final List<String> getList(final String key) {
        final List<String> list = new ArrayList<String>();
        final String str = getString(key);
        if (!StringUtils.isBlank(str)) {
            final StringTokenizer st = new StringTokenizer(str, ",");
            while (st.hasMoreTokens()) {
                list.add(st.nextToken().trim());
            }
        }
        return list;
    }

    /**
     * Returns the configured int value for the given property. If no property is configured, defaultValue will be returned.
     *
     * @param key
     *            property name
     * @param defaultValue
     *            default value
     * @return the integer value
     */
    public static final int getInt(final String key, final int defaultValue) {
        final String temp = getString(key);
        if (!StringUtils.isEmpty(temp)) {
            return Integer.parseInt(temp);
        }
        return defaultValue;
    }

    /**
     * Tries to obtain a required property. Allows for multiple properties to be obtained so that that missing required properties can be gathered up and checked all at once.
     *
     * This is useful for saving the time of someone configuring properties since they do not have to fix one property and then get the next error, over and over. By using this all
     * missing required properties can be indicated so that it can be fixed at once.
     *
     * If a property is not found(null) then name of the property is appended to the errors buffer.
     *
     * @param props
     *            The properties to search from.
     * @param key
     *            The key of the required property.
     * @param errors
     *            Comma separated list of missing required properties.
     * @return The value of the required property or <code>null</code> if not found.
     */
    public static final String getReqProperty(final Properties props, final String key, final StringBuilder errors) {
        final String val = props.getProperty(key);
        if (val == null) {
            if (errors.length() > 0) {
                errors.append(", ");
            }
            errors.append(key);
        }
        return val;

    }

    /**
     * Loads a property map from a url.
     *
     * @param url
     *            The url to load the data stream from.
     * @param useCaches
     *            Indicates if the url caches should be enabled or not.
     * @return A Properties object with all the properties.
     * @throws IOException
     *             If the URL is not usable.
     */
    public static final Properties loadProps(final URL url, final boolean useCaches) throws IOException {
        final URLConnection conn = url.openConnection();
        conn.setUseCaches(useCaches);
        final Properties props = new Properties();
        props.load(conn.getInputStream());

        return props;
    }

    /**
     * Load properties from the JBoss server configuration directory. The directory targeted is under <code>/<i>jboss</i>/server/<i>servername</i>/<b>conf</b></code>
     *
     * @param fileName
     *            Configuration file name.
     * @return properties loaded from file, or null if the file wasn't found or not read.
     */
    public static Properties loadPropertiesFromConfigDir(final String fileName) {
        InputStream istream = null;
        Properties props = null;
        try {
            final String property = System.getProperty("jboss.server.config.url");
            if (property != null) {
                final URI uri = URI.create(property);
                final File configurationDir = new File(uri);
                final File propertyFile = new File(configurationDir, fileName);
                /*if (_log.isDebugEnabled()) {
                    _log.debug("Attempting to fetch configuration at " + propertyFile.toString());
                }*/
                istream = new FileInputStream(propertyFile);
                props = new Properties();
                props.load(istream);
            }
        } catch (final NullPointerException e) {
//            _log.error("Unable to access properties in " + fileName);
//            _log.info("Unable to access properties in " + fileName, e);
        } catch (final IllegalArgumentException e) {
//            _log.error("Unable to access properties in " + fileName);
//            _log.info("Unable to access properties in " + fileName, e);
        } catch (final IOException e) {
//            _log.error("Unable to read properties in " + fileName);
//            _log.info("Unable to read properties in " + fileName, e);
        } finally {
            FileUtils.close(istream);
        }
        return props;
    }

}
