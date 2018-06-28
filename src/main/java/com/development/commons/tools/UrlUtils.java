/*
 * $Id$
 */
package com.development.commons.tools;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UrlUtils {

    private static final String EXT_JAR = ".jar";

    /** No instances should be created for this Util class. */
    private UrlUtils() {
        super();
    }

    /**
     * This method encrypts a given string and returns the encrypted bytes.
     *
     * @param companyId
     *            - The company id, used as part of the salt.
     * @param userId
     *            The user id, used as part of the salt.
     * @param text
     *            A given text string
     *
     * @throws NoSuchAlgorithmException
     *             No encryption algorithm was found
     *
     * @return digest The encrypted bytes
     */
    public static byte[] crypt(final String companyId, final String userId, final String text) throws NoSuchAlgorithmException {
        byte[] digest = new byte[1];
        final String newStr = companyId + "_" + userId + "_" + text;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            digest = md.digest(StringUtils.toBytes(newStr));
        } catch (final NoSuchAlgorithmException e) {
            throw e;
        }
        return digest;
    }

    /**
     * Appends a query string to a url. This will handle the case where there is existing query parameters or none on a url.
     *
     * @param url
     *            The url as a string.
     * @param name
     *            The name of the parameter to add.
     * @param value
     *            The value of the parameter to add.
     * @return The new url with the appended param.
     */
    public static String appendQueryParam(final String url, final String name, final String value) {
        final StringBuilder sb = new StringBuilder();
        if (url != null) {
            sb.append(url);
        }
        appendQueryParam(sb, name, value);
        return sb.toString();

    }

    /**
     * This method makes return an absolute url from the given url.
     *
     * @param url
     *            url.
     * @return absolute url.
     * @throws URISyntaxException
     *             uri syntax exception.
     */
    public static String absoluteURL(final String url) throws URISyntaxException {
        String absURL = url;
        final URI uri = new URI(url);
        if (!uri.isAbsolute() && !uri.getPath().startsWith("/")) {
            absURL = "/" + url;
        }
        return absURL;
    }

    /**
     * Appends a query string to a url. This will handle the case where there is existing query parameters or none on a url.
     *
     * @param sb
     *            A StringBuilder that is holding the url.
     * @param name
     *            The name of the parameter to add.
     * @param value
     *            The value of the parameter to add.
     */
    public static void appendQueryParam(final StringBuilder sb, final String name, final String value) {

        if (sb != null) {
            if (sb.indexOf("?") > 0) {
                sb.append("&").append(name).append("=").append(value);
            } else {
                sb.append("?").append(name).append("=").append(value);
            }
        }

    }

    /**
     * Get Jar name from url.
     *
     * @param url
     *            url
     * @return String
     */
    public static String getJarName(final URL url) {
        String urlPath = url.getFile();

        try {
            urlPath = URLDecoder.decode(urlPath, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String filePrefix = "file:";
        if (urlPath.startsWith(filePrefix)) {
            urlPath = urlPath.substring(filePrefix.length());
        }

        if (urlPath.indexOf('!') > 0) {
            urlPath = urlPath.substring(0, urlPath.indexOf('!'));
        }

        final File root = new File(urlPath);
        return root.getName();
        // return urlPath.substring(0,urlPath.lastIndexOf(EXT_JAR)) + EXT_JAR;
    }

    /**
     * .
     *
     * @param url
     *            url
     * @return boolean
     */
    public static boolean isInsideJar(final URL url) {
        String urlPath = url.getFile();
        try {
            urlPath = URLDecoder.decode(urlPath, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        final String filePrefix = "file:";
        if (urlPath.startsWith(filePrefix)) {
            // strip off the file: if it exists
            urlPath = urlPath.substring(filePrefix.length());
        }

        if (urlPath.indexOf('!') > 0) {
            urlPath = urlPath.substring(0, urlPath.indexOf('!'));
        }

        final File root = new File(urlPath);
        return root.getName().indexOf(".jar") > -1 || root.getName().indexOf(".zip") > -1;
    }

}
