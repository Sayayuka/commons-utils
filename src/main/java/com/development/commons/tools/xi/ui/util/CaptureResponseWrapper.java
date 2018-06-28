package com.development.commons.tools.xi.ui.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.development.commons.tools.StringUtils;

/**
 * $Id$
 *
 * Wraps a HttpServletResponse to capture all data being written to a stream.
 *
 * @author ddiodati
 *
 */
public class CaptureResponseWrapper extends HttpServletResponseWrapper {

  /**
   * Buffer size.
   */
  private int bufferSize = 0;

  /**
   * Encode url pattern. Used for url substitution and modification. The client need to set the encodePattern, if
   * applicable. This works in conjuction with the attribute 'replacement'.
   */
  private Pattern encodePattern;

  /**
   * Replacement encoding url string.
   */
  private String replacement;

  /**
   * Writer.
   */
  private final StringWriter strWriter = new StringWriter();

  /**
   * Constructor.
   *
   * @param mainResponse
   *          The response to wrap.
   */
  public CaptureResponseWrapper(final HttpServletResponse mainResponse) {
    super(mainResponse);

    bufferSize = mainResponse.getBufferSize();

  }

  /**
   * Sets a encoding pattern to replace the url for the encodeURL method.
   *
   * @param p
   *          Regex pattern to look for.
   * @param replacementStr
   *          The string replacement.
   */
  public void setEncodePattern(final Pattern p, final String replacementStr) {
    encodePattern = p;
    this.replacement = replacementStr;
  }

  /**
   * {@inheritDoc} Disables redirects.
   */
  @Override
  public void sendRedirect(final String arg0) throws IOException {
    // do nothing
  }

  /**
   * {@inheritDoc}
   *
   * Applies the encoding pattern set by the {@link #setEncodePattern(Pattern, String)} method.
   */
  @Override
  public String encodeURL(final String url) {
    String result;
    Matcher m = null;

    if (encodePattern != null) {
      m = encodePattern.matcher(url);
    }

    if (encodePattern != null && !StringUtils.isEmpty(replacement) && m != null && m.matches()) {
      result = m.replaceFirst(replacement);
    } else if (!StringUtils.isEmpty(url)) {
      result = super.encodeURL(url);
    } else {
      result = null;
    }

    return result;
  }

  /**
   * {@inheritDoc} Does nothing.
   */
  @Override
  public void flushBuffer() throws IOException {
    // do nothing
  }

  /**
   * {@inheritDoc} Does nothing.
   */
  @Override
  public void reset() {
    // do nothing
  }

  /**
   * {@inheritDoc} Does nothing.
   */
  @Override
  public void resetBuffer() {
    // do nothing
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void setBufferSize(final int arg0) {
    bufferSize = arg0;
  }

  /**
   * {@inheritDoc} Does nothing.
   */
  @Override
  public void setContentLength(final int arg0) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public int getBufferSize() {
    // TODO Auto-generated method stub
    return bufferSize;
  }

  /**
   * {@inheritDoc} Does nothing.
   */
  @Override
  public void setContentType(final String arg0) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(strWriter);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return new ResOutputStream(strWriter);
  }

  /**
   * Returns the string buffer that has buffered the stream.
   *
   * @return StringBuffer with the data.
   */
  public StringBuffer getStringBuffer() {

    return strWriter.getBuffer();
  }

  /**
   * {@inheritDoc} Always returns false since this will not commit the request.
   */
  @Override
  public boolean isCommitted() {
    return false;
  }

  /**
   * Inner outputstream to redirect writes to the internal buffer.
   *
   * @author ddiodati
   *
   */
  private static final class ResOutputStream extends ServletOutputStream {

    /**
     * Writer.
     */
    private final StringWriter strWriter;

    public ResOutputStream(final StringWriter strWriter) {
      this.strWriter = strWriter;
    }

    @Override
    public void write(final int b) {
      strWriter.write(b);
    }

    public boolean isReady() {
      return false;
    }

    public void setWriteListener(WriteListener writeListener) {

    }
  }

}
