package com.development.commons.tools.xi.ui.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.development.commons.tools.xi.util.IteratorToEnum;

/**
 * Wraps the HttpServletRequest object to allow you to modify request parameters and pathinfo.
 *
 * @author ddiodati
 *
 */
public class MutableRequestWrapper extends HttpServletRequestWrapper {

  /**
   * request paramter map.
   */
  private final Map<String, String[]> params = new HashMap<String, String[]>();

  /**
   * The path info value.
   */
  private String pathInfo;

  /**
   * servlet path.
   */
  private String servletPath;

  /**
   * Constructor.
   *
   * @param mainRequest
   *          The httpservletrequest to wrap.
   */
  public MutableRequestWrapper(final HttpServletRequest mainRequest) {
    super(mainRequest);
    @SuppressWarnings("unchecked")
    final Map<String, String[]> parameterMap = super.getParameterMap();
    params.putAll(parameterMap);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPathInfo() {
    if (pathInfo != null) {
      return pathInfo;
    }
    return super.getPathInfo();
  }

  /**
   * Sets the path info.
   *
   * @param path
   *          The path.
   */
  public void setPathInfo(final String path) {
    pathInfo = path;
  }

  /**
   * Returns the servlet path set on this wrapper.
   *
   * @return The string path if it is set.
   */
  @Override
  public String getServletPath() {
    if (servletPath != null) {
      return servletPath;
    }
    return super.getServletPath();
  }

  /**
   * Sets the servlet path.
   *
   * @param servletPath
   *          The servlet path.
   */
  public void setServletPath(final String servletPath) {
    this.servletPath = servletPath;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String[]> getParameterMap() {
    return params;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getParameter(final String name) {

    final String result[] = params.get(name);
    if (result != null && result.length > 0) {
      return result[0];
    }
    return null;
  }

  /**
   * Removes a parameter.
   *
   * @param name
   *          Param name.
   */
  public void removeParameter(final String name) {
    final String result[] = params.get(name);
    if (result != null && result.length > 0) {
      params.remove(name);
    }
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          Param name.
   * @param value
   *          Param value.
   */
  public void setParameter(final String name, final String value) {
    String[] items = params.get(name);
    if (items != null && items.length > 0) {
      final String[] newItems = new String[items.length + 1];
      System.arraycopy(items, 0, newItems, 0, items.length);
      items = newItems;
    } else {
      items = new String[1];
    }
    items[items.length - 1] = value;
    params.put(name, items);
  }

  /**
   * Sets a parameter with one or more values.
   *
   * @param name
   *          The parameter name.
   * @param values
   *          The values for the parameter.
   */
  public void setParameter(final String name, final String... values) {
    String[] items = params.get(name);
    if (items != null && items.length > 0) {
      final String[] newItems = new String[items.length + values.length];
      System.arraycopy(items, 0, newItems, 0, newItems.length);
      System.arraycopy(values, 0, newItems, items.length, values.length);
      items = newItems;
    } else {
      items = values;
    }
    params.put(name, items);
  }

  /**
   * Returns enumeration of parameters. This is overrides the httpservletrequest for servlet version2.4, which is used
   * in jboss4.0.5, but we are currently compiling against 2.3 in the build script.
   *
   * @return Enumeration of request parameter names.
   */
  public Enumeration<String> getParameternames() {
    final IteratorToEnum result = new IteratorToEnum(params.keySet().iterator());
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getParameterValues(final String name) {
    return params.get(name);

  }

}