package com.development.commons.tools.xi.util.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource object that provides access to the resource. Provides a generic interface to getting the input stream, path,
 * and name
 *
 * @author ddiodati
 * @see ResourceScanner
 */
public interface Resource {

  /**
   * Returns an input stream.
   *
   * NOTE: User is responsible for closing the stream in a finally block.
   *
   * @return The input stream to read the resource from.
   * @throws IOException
   *           If it fails to read the resource.
   */
  InputStream getInputStream() throws IOException;

  /**
   * Returns the path to the resource relative to the ResourceScanner rootUrl.
   *
   * @see ResourceScanner
   * @return The string path.
   */
  String getPath();

  /**
   * Obtains the name of resource.
   *
   * @return The name of the resource.
   */
  String getName();

  /**
   * Returns the last modified date of the resource.
   *
   * @return The time in milliseconds.
   */
  long getLastModifiedDate();

}