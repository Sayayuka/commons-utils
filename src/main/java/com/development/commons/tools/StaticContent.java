/*
 * $Id$
 *
 * Copyright (C) 2006 Jeffrey Ichnowski
 * All Rights Reserved
 */
package com.development.commons.tools;

/**
 * StaticContent -- this is a set of utility methods related to distributing static content among one or more servers.
 *
 * <p>
 * The static content servers can be configured at startup using the system property "sf.sfv4.static-content-servers".
 * For example: "-Dsf.sfv4.static-content-servers=http://static.server.com". To have more than one static content
 * server, separate the server addresses by commas. See {@link #serverFor} for more information about the nature of the
 * static content URLs.
 *
 * <p>
 * A possible real-world configuration might be:
 * "-Dsf.sfv4.static-content-servers=http://img1.successsfactors.com,http://img2.successfactors.com"
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class StaticContent {

  private StaticContent() {
    super();
  }

  /**
   * Cached value from parsed system property.
   */
  private static final String[] STATIC_CONTENT_URLS;

  // this block of code parses the system property and populates the
  // static cached. This means that once this class has been loaded,
  // changes to the system property will be ignored.
  static {
    final String staticUrlProp = System.getProperty("sf.sfv4.static-content-servers");
    if (staticUrlProp == null) {
      STATIC_CONTENT_URLS = null;
    } else {
      final String[] staticUrls = staticUrlProp.split(",");

      if (staticUrls.length == 0) {
        STATIC_CONTENT_URLS = null;
      } else {
        STATIC_CONTENT_URLS = staticUrls;

        for (int i = 0; i < staticUrls.length; ++i) {
          String url = staticUrls[i].trim();

          if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
          }

          staticUrls[i] = url;
        }
      }
    }
  }

  /**
   * Returns the static content server for a specific relative URL. The server returned will either be an empty string,
   * or an absolute URL that should be prepended to the specified relative URL.
   *
   * <p>
   * The specified URL must begin with "/" (e.g. "/img/icon.gif") and the returned server will never end with "/" (e.g.
   * "https://static.server.com" or ""). As such a standard string concatenation is always sufficient to generate the
   * final absolute URL.
   *
   * @param url
   *          a relative url beginning with "/"
   * @return a string to be prepended to the relative url.
   */
  public static final String serverFor(final String url) {

    // IMPORTANT: do not copy/paste this logic anywhere!!! Always call
    // this method and let it handle the lookup. This implementation
    // may change, and having different servers be returned for the
    // same static content could adversely affect end-user
    // performance.

    // Note: for now, we just depend on url.hashCode() being
    // unchanging for a specific url, and providing a resonably random
    // distribution across the specified servers.

    final int MAGIC_NUMBER = 0x7fffffff;
    return (STATIC_CONTENT_URLS == null || url == null || url.length() == 0 || url.charAt(0) != '/') ? ""
        : STATIC_CONTENT_URLS[(url.hashCode() & MAGIC_NUMBER) % STATIC_CONTENT_URLS.length];
  }

  /**
   * Generates the full absolute content-server URL for a given relative url. This method just does the following:
   *
   * <pre>
   * contentServerFor(url) + url
   * </pre>
   *
   * This method is provided for convenience. Usually it is more efficient to directly output the return value of
   * contentServerFor followed by the URL, when using a StringBuffer or a Writer. In some cases it is easier to
   * fall-back on this method.
   *
   * @param url
   *          a relative url
   * @return the full content-server url
   */
  public static final String urlFor(final String url) {
    final String server = serverFor(url);

    if (server.length() == 0) {
      return url;
    }
    return server + url;
  }
} // StaticContent
