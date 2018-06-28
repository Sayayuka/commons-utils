/*
 * $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * (Experimental header!)
 */
package com.development.commons.tools.util.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.development.commons.tools.comparator.AbstractNullSafeComparator;
import com.development.commons.tools.LocalPool;

/**
 * Various XML related utilities (mostly for escaping text).
 *
 * <p>
 * Note about escaping methods: For EE-3454, all non-valid characters are
 * removed from escaped content. The valid XML characters (see www.w3.org for
 * XML spec) are:
 * </p>
 *
 * <pre>
 * Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
 * </pre>
 *
 * <p>
 * The UTF-16 range [#D800-#DFFF] are surrogate pair characters that can be
 * combined to create the Unicode range [#x10000-#x10FFFF]. (Wikipedia has
 * (had?) a good explanation http://en.wikipedia.org/wiki/UTF-16/UCS-2). There
 * is much improved support for surrogate pairs in JDK1.5. For here though, we
 * need to do is validate that the surrogate pairs are correct.
 * </p>
 *
 * @version $Revision$
 * @author Jeffrey Ichnowski
 */
public final class XMLUtils implements XmlTemplateConstants {
//  private static final Logger logger = LogManager.getLogger();

  public static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  /**
   * CDATA section start character sequence ("&lt;![CDATA[")
   */
  public static String CDATA_START = "<![CDATA[";
  /**
   * CDATA section end character sequence ("]]&lt;")
   */
  public static String CDATA_END = "]]>";

  /**
   * @see <a
   *      href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#isoformats">
   *      The standard XML date format as recommended by XMLSchemas and ISO8601.
   *      </a>
   */
  public static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd";

  /**
   * The stardard XML date/time format. The timezone on the DateFormat object
   * must be set to UTC for this to work to spec. The alternative is to use
   * would be to replace the ending 'Z' with a "+/-h:mm" indicating the
   * timezone, but SimpleDateFormat does not have support for that.
   */
  public static final String ISO8601_DATETIME_UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * The standard XML time format. There are no format/parse methods for this
   * yet because I didn't think anyone would need them in the near future. This
   * is here mostly for reference. Times are written in UTC, see note on
   * ISO8601_DATETIME_UTC_FORMAT for details.
   */
  public static final String ISO8601_TIME_UTC_FORMAT = "HH:mm:ss'Z'";

  /**
   * It is possible to create XML documents whose processing could result in the
   * use of all system resources. This property enables Xerces to detect such
   * documents, and abort their processing.
   *
   * @see <a
   *      href="http://xerces.apache.org/xerces2-j/properties.html">Properties</a>
   */
  public static final String XML_SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";

  /**
   * The UTC TimeZone used by getDateTimeFormat()
   */
  private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

  static DocumentBuilderFactory _safeDocumentBuilderFactory;
  static DocumentBuilderFactory _soapDocumentBuilderFactory;
  static {
    try {
      _safeDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
      _safeDocumentBuilderFactory.setCoalescing(true);
      _safeDocumentBuilderFactory.setValidating(false);
      _safeDocumentBuilderFactory.setIgnoringComments(true);
      _safeDocumentBuilderFactory.setNamespaceAware(true);
      _safeDocumentBuilderFactory.setExpandEntityReferences(false);

      try {
        final String className = _safeDocumentBuilderFactory.getClass()
            .getName().replace('.', '/')
            + ".class";
        ClassLoader classloader = _safeDocumentBuilderFactory.getClass()
            .getClassLoader();
        if (classloader == null) {
          classloader = ClassLoader.getSystemClassLoader();
        }
        final URL resource = classloader.getResource(className);
//        logger.info("=========resourceURL: " + resource);
      } catch (final Exception e) {
        // for now, ignore until we fix the classpath for junit
      }
      try {
        // prevent DTD recursion attacks
        // this is documented on http://xml.apache.org/xerces2-j/properties.html
        // NOTE: This will generate an exception in JUNIT mode since there is no
        // security manager present.
        _safeDocumentBuilderFactory.setAttribute(XML_SECURITY_MANAGER,
            new org.apache.xerces.util.SecurityManager());
      } catch (final Exception e) {
//        logger.info("Exception " + e + " when setting the xml security manager");
      }

      _safeDocumentBuilderFactory.newDocumentBuilder(); // test

    } catch (final Exception e) {
      e.printStackTrace();
    }

    try {
      _soapDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
      _soapDocumentBuilderFactory.setCoalescing(true);
      _soapDocumentBuilderFactory.setValidating(false);
      _soapDocumentBuilderFactory.setIgnoringComments(true);
      _soapDocumentBuilderFactory.setNamespaceAware(true);
      _soapDocumentBuilderFactory.setExpandEntityReferences(false);

      try {
        // prevent DTD recursion attacks
        // this is documented on http://xml.apache.org/xerces2-j/properties.html
        // NOTE: This will generate an exception in JUNIT mode since there is no
        // security manager present.
        _soapDocumentBuilderFactory.setAttribute(XML_SECURITY_MANAGER,
            new org.apache.xerces.util.SecurityManager());
      } catch (final Exception e) {
//        logger.info("Exception " + e + " when setting the xml security manager");
      }

      _soapDocumentBuilderFactory.newDocumentBuilder(); // test

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  static final char MAX_VALID_UTF16 = '\ufffd';

  // These constants are defined in java.lang.Character in 1.5
  static final char MIN_HIGH_SURROGATE = '\uD800';
  static final char MAX_HIGH_SURROGATE = '\uDBFF';
  static final char MIN_LOW_SURROGATE = '\uDC00';
  static final char MAX_LOW_SURROGATE = '\uDFFF';

  // no instances allowed
  private XMLUtils() {
  }

  /**
   * Escapes a string suitable for an XML attribute value. Both quotes and
   * single-quotes are escape, so attributes may safely be enclosed within
   * either delimiter.
   *
   * @param buf
   *          - the buffer into which the string is escaped
   * @param src
   *          - the string to escape
   * @return buf
   */
  public static StringBuffer escapeAttribute(final StringBuffer buf,
      final String src) {
    if (null != src) {
      for (int i = 0, n = src.length(); i < n; ++i) {
        final char c = src.charAt(i);

        // See class Javadoc for notes on what characters get discarded.

        if (c < MIN_HIGH_SURROGATE) {
          // #x9 | #xA | #xD | [#x20-#xD7FF]
          switch (c) {
          case '\t':
          case '\r':
          case '\n':
            buf.append(c);
            break;
          case '"':
            buf.append("&#34;");
            break;
          case '&':
            buf.append("&amp;");
            break;
          case '\'':
            buf.append("&#39;");
            break;

          // '<' is not allowed in attribute values in XML
          case '<':
            buf.append("&lt;");
            break;

          default:
            if ('\u0020' <= c) {
              buf.append(c);
            } else {
              // #x9 #xA and #xD are already included by switch so
              // this discards everything else between #x0 and #x1f
//              logger.warn("Control character has been found and discarded in XMLUtils.escapeAttribute(): "
//                      + src);
            }
          }
        } else if (MAX_LOW_SURROGATE < c && c <= MAX_VALID_UTF16) {
          // [#xE000-#xFFFD]
          buf.append(c);
        } else if (c <= MAX_HIGH_SURROGATE) {
          if (i + 1 < n) {
            final char c2 = src.charAt(i + 1);
            if (MIN_LOW_SURROGATE <= c2 && c2 <= MAX_LOW_SURROGATE) {
              buf.append(c).append(c2);
              ++i;
            } else {
              // else discard c as invalid surrogate char
//              logger
//                  .warn("Surrogate character has been found and discarded in XMLUtils.escapeAttribute(): "
//                      + src);
            }
          }
        }
      }
    }
    return buf;
  }

  /**
   * Shorthand for escaping a single string, equivalent to:
   *
   * <pre>
   * return escapeAttribute(new StringBuffer(), src).toString();
   * </pre>
   *
   * @param src
   *          - the string to escape
   * @return the escaped string
   */
  public static String escapeAttribute(final String src) {
    return escapeAttribute(new StringBuffer(), src).toString();
  }

  /**
   * Escapes a string for XML content. Suitable for any content outside of a
   * tag. Only ampersands, less-than signs, and greater-than signs are escaped.
   *
   * @param buf
   *          - the buffer into which the string is escaped
   * @param src
   *          - the string to escape
   * @param escapeQuotes
   *          - whether quotes should be escaped
   * @return buf
   */
  public static StringBuffer escapeContent(final StringBuffer buf,
      final String src, boolean escapeQuotes) {
    if (null != src) {
      for (int i = 0, n = src.length(); i < n; ++i) {
        final char c = src.charAt(i);

        // See class Javadoc for notes on what characters get discarded.

        if (c < MIN_HIGH_SURROGATE) {
          // #x9 | #xA | #xD | [#x20-#xD7FF]
          switch (c) {
          case '\t':
          case '\r':
          case '\n':
            buf.append(c);
            break;
          case '&':
            buf.append("&amp;");
            break;
          case '<':
            buf.append("&lt;");
            break;
          case '>':
            buf.append("&gt;");
            break;
          case '"':
            buf.append(escapeQuotes ? "&quot;" : '"');
            break;
          default:
            if ('\u0020' <= c) {
              buf.append(c);
            } else {
              // #x9 #xA and #xD are already included by switch so
              // this discards everything else between #x0 and #x1f
//              logger
//                  .warn("Control character has been found and discarded in XMLUtils.escapeContent(): "
//                      + src);
            }
          }
        } else if (MAX_LOW_SURROGATE < c && c <= MAX_VALID_UTF16) {
          // [#xE000-#xFFFD]
          buf.append(c);
        } else if (c <= MAX_HIGH_SURROGATE) {
          if (i + 1 < n) {
            final char c2 = src.charAt(i + 1);
            if (MIN_LOW_SURROGATE <= c2 && c2 <= MAX_LOW_SURROGATE) {
              buf.append(c).append(c2);
              ++i;
            } else {
              // else discard c as invalid surrogate char
//              logger
//                  .warn("Surrogate character has been found and discarded in XMLUtils.escapeContent(): "
//                      + src);
            }
          }
        }
      }
    }
    return buf;
  }

  /**
   * Unescape content to bring it back to its original form. Only ampersands,
   * less-than signs, and greater-than signs are escaped.
   *
   * @param src
   *          escaped attribute.
   * @return the reconstructed original attribute.
   */
  public static String unescapeContent(final String src) {
    String unescapedContent = src.replaceAll("&lt;", "<");
    unescapedContent = unescapedContent.replaceAll("&gt;", ">");
    unescapedContent = unescapedContent.replaceAll("&apos;", "'");
    unescapedContent = unescapedContent.replaceAll("&amp;", "&");

    return unescapedContent;
  }

  /**
   * Shortcut for escaping a single string. Equivalent to:
   *
   * <pre>
   * return escapeContent(new StringBuffer(), src).toString();
   * </pre>
   *
   * @param str
   *          - the string to escape
   * @return the escaped string
   */
  public static String escapeContent(final String str) {
    return escapeContent(new StringBuffer(), str).toString();
  }

  /**
   * Shortcut for escaping a string. Quotes are escaped by default
   *
   * @param buf the buffer into which the string is escaped
   * @param src string to be escaped
   * @return
   */
  public static StringBuffer escapeContent(final StringBuffer buf,
      final String src){
    return escapeContent(buf, src, true);
  }

  /**
   * Escapes content for inclusion within a &gt;![CDATA[ section. This looks for
   * and escapes all occurances of ']]&gt;'. Note: this will be a highly
   * unoptimal escape sequence for strings containing a large amount of "]]&gt;"
   * sequences, since the escape sequence for "]]&gt;" within a CDATA section is
   * "]]&gt;]]&lt;![CDATA[gt;". Of course under normal circumstances, the
   * "]]&gt;" sequence is relatively uncommon.
   *
   * <p>
   * For large strings,
   *
   * <pre>
   * XMLUtils.escapeCDATA(new StringBuffer(XMLUtils.CDATA_START), src).append(
   *     XMLUtils.CDATA_END)
   * </pre>
   *
   * Will generally be faster than
   *
   * <pre>
   * XMLUtils.escapeContent(new StringBuffer(), src)
   * </pre>
   *
   * <p>
   * Note: be careful when appending more than one string to a CDATA section
   * with this method. Since the CDATA_END sequence is 3 characters long, it is
   * possible to have it span multiple strings. E.g.
   *
   * <pre>
   * buf.append(XMLUtils.CDATA_START);
   * XMLUtils.escapeCDATA(buf, &quot;foo]&quot;);
   * XMLUtils.escapeCDATA(buf, &quot;]&gt; we've just terminated a CDATA section&quot;);
   * buf.append(XMLUtils.CDATA_END);
   * </pre>
   *
   * <p>
   * For this reason, it is almost always better to use the toCDATA methods or,
   * the escapeContent methods.
   *
   * <!-- for people reading the javadoc source, the escape sequence for "]]>"
   * is "]]>]]<!CDATA[>" -->
   *
   * @param buf
   *          - where to append the escaped value
   * @param src
   *          - the string to escape
   */
  public static StringBuffer escapeCDATA(final StringBuffer buf,
      final String src) {
    if (null != src) {
      for (int i = 0, n = src.length(); i < n; ++i) {
        final char c = src.charAt(i);

        // See class Javadoc for notes on what characters get discarded.

        if (c < MIN_HIGH_SURROGATE) {

          if (c == ']' && i + 2 < n && src.charAt(i + 1) == ']'
              && src.charAt(i + 2) == '>') {
            // "]]>]]&gt;<![CDATA[" would work too, but '>' can appear
            // in CDATA sections without requiring escaping (saves 3 characters)
            buf.append("]]>]]<![CDATA[>");
            i += 2;
          } else if (c >= '\u0020' || c == '\r' || c == '\n' || c == '\t') {
            buf.append(c);
          } else {
            // discard
//            logger
//                .warn("Control character has been found and discarded in XMLUtils.escapeCDATA(): "
//                    + src);
          }

        } else if (MAX_LOW_SURROGATE < c && c <= MAX_VALID_UTF16) {
          buf.append(c);
        } else if (c <= MAX_HIGH_SURROGATE) {
          if (i + 1 < n) {
            final char c2 = src.charAt(i + 1);
            if (MIN_LOW_SURROGATE <= c2 && c2 <= MAX_LOW_SURROGATE) {
              buf.append(c).append(c2);
              ++i;
            } else {
              // else discard c as invalid surrogate char
//              logger
//                  .warn("Surrogate character has been found and discarded in XMLUtils.escapeCDATA(): "
//                      + src);
            }
          }
        }
      }
    }

    return buf;
  }

  /**
   * Shortcut for escaping a single string. Equivalent to:
   *
   * <pre>
   * return escapeCDATA(new StringBuffer(), src).toString();
   * </pre>
   *
   * @param src
   *          - the string to escape
   * @return the escaped string
   */
  public static String escapeCDATA(final String src) {
    return escapeCDATA(new StringBuffer(), src).toString();
  }

  /**
   * Creates a CDATA section. Equivalent to:
   *
   * <pre>
   * buf.append(CDATA_START);
   * escapeCDATA(buf, src);
   * buf.append(CDATA_END);
   * </pre>
   */
  public static StringBuffer toCDATA(final StringBuffer buf, final String src) {
    return escapeCDATA(buf.append(CDATA_START), src).append(CDATA_END);
  }

  /**
   * Shortcut for escaping a single string. Equivalent to:
   *
   * <pre>
   * return toCDATA(new StringBuffer(), src).toString();
   * </pre>
   *
   * @param src
   *          - the string to escape
   * @return the escaped string
   */
  public static String toCDATA(final String src) {
    return toCDATA(new StringBuffer(), src).toString();
  }

  /**
   * Returns a java.text.DateFormat suitable for formatting and parsing dates to
   * and from XML files.
   *
   * @return XML date format
   */
  public static final DateFormat getDateFormat() {
    final SimpleDateFormat format = new SimpleDateFormat(ISO8601_DATE_FORMAT,
        Locale.US);
    return format;
  }

  /**
   * Returns a java.text.DateFormat suitable for formatting and parsing dates
   * and time to and from XML files. The timezone on the returned date format is
   * set to UTC to comply with the standard (this is the primary advantage of
   * using this method instead of the string constant for the date format
   * directly).
   *
   * Note: if using for timestamps, this has a resolution of 1 second.
   *
   * @return XML date/time format
   */
  public static final DateFormat getDateTimeFormat() {
    final SimpleDateFormat format = new SimpleDateFormat(
        ISO8601_DATETIME_UTC_FORMAT, Locale.US);
    format.setTimeZone(UTC_TIMEZONE);
    return format;
  }

  /**
   * Appends a date to a StringBuffer using the XML ISO8601 standard format.
   *
   * @param buf
   *          - the buffer to which the formatted date is appended
   * @param date
   *          - the date to format and append to buf
   * @return buf
   */
  public static final StringBuffer formatDate(final StringBuffer buf,
      final Date date) {
    return getDateFormat().format(date, buf, new FieldPosition(0));
  }

  /**
   * Formats a date to XML ISO8601 standards
   *
   * @param date
   *          - the date to format
   * @return the formatted date
   */
  public static final String formatDate(final Date date) {
    return getDateFormat().format(date);
  }

  /**
   * Parses a date from XML ISO8601 standards. (The reverse of formatDate)
   *
   * @param text
   *          - the text to parse into a date
   * @return the parsed date
   * @throws ParseException
   *           - if the date could not be parsed (this method never returns
   *           null)
   */
  public static final Date parseDate(final String text) throws ParseException {
    return getDateFormat().parse(text.trim());
  }

  /**
   * Appends a date/time to a StringBuffer using the XML ISO8601 standard
   * format.
   *
   * @param buf
   *          - the buffer to which the formatted date/time is appended
   * @param date
   *          - the date/time to format and append to buf
   * @return buf
   */
  public static final StringBuffer formatDateTime(final StringBuffer buf,
      final Date date) {
    return getDateTimeFormat().format(date, buf, new FieldPosition(0));
  }

  /**
   * Formats a date/time to XML ISO8601 standards
   *
   * @param date
   *          - the date/time to format
   * @return the formatted date/time
   */
  public static final String formatDateTime(final Date date) {
    return getDateTimeFormat().format(date);
  }

  /**
   * Parses a date/time from XML ISO8601 standards. (The reverse of
   * formatDateTime)
   *
   * @param text
   *          - the text to parse into a date
   * @return the parsed date/time
   * @throws ParseException
   *           - if the date/time could not be parsed (this method never returns
   *           null)
   */
  public static final Date parseDateTime(final String text)
      throws ParseException {
    return getDateTimeFormat().parse(text);
  }

  /**
   * Returns a non-namespace aware DocumentBuilder with DTD recursion attack
   * hindered.
   */
  public static DocumentBuilder newSafeDocumentBuilder() {
    try {
      return _safeDocumentBuilderFactory.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      throw (InternalError) new InternalError("ParserConfigurationException")
          .initCause(e);
    }
  }

  /**
   * Returns a DocumentBuilder suitable for use in SOAP. It is namespace aware,
   * and has DTD recursion attack hindered.
   */
  public static DocumentBuilder newSoapDocumentBuilder() {
    try {
      return _soapDocumentBuilderFactory.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      throw (InternalError) new InternalError("ParserConfigurationException")
          .initCause(e);
    }
  }

  /**
   * Prints a sequence of spaced the specified writer. Uses the GC friendly
   * LocalPool.getCharBuffer for a character buffer.
   *
   * @param out
   *          where to write
   * @param indentLevel
   *          the number of spaces to write.
   */
  public static void indent(final PrintWriter out, final int indentLevel) {
    if (indentLevel > 0) {
      final char[] chars = LocalPool.getCharBuffer(indentLevel);
      Arrays.fill(chars, 0, indentLevel, ' ');
      out.write(chars, 0, indentLevel);
      LocalPool.zeroBuffer(chars);
    }
  }

  public static void indent(final StringBuffer buf, final int indentLevel) {
    if (indentLevel > 0) {
      final char[] chars = LocalPool.getCharBuffer(indentLevel);
      Arrays.fill(chars, 0, indentLevel, ' ');
      buf.append(chars, 0, indentLevel);
      LocalPool.zeroBuffer(chars);
    }
  }

  /**
   * Prints a simple XML element to a PrintWriter. The output looks like the
   * following, with a newline appended:
   *
   * <pre>
   *   ... indent ... &lt;name&gt;value&lt;/name&gt;
   * </pre>
   *
   * To distinguish null from empty string, null values are not written, and
   * empty strings are written as an empty element.
   *
   * @param out
   *          where to write
   * @param indentLevel
   *          the number of spaces to preceed the element
   * @param name
   *          the name of the XML element (will not be escaped)
   * @param value
   *          the value of the XML element (will be escaped)
   */
  public static void printSimpleElement(final PrintWriter out,
      final int indentLevel, final String name, final String value) {
    if (value != null) {
      if (indentLevel > 0) {
        indent(out, indentLevel);
      }
      out.print('<');
      out.print(name);

      if (value.length() == 0) {
        out.print('/');
      } else {
        out.print('>');
        out.print(escapeContent(value));
        out.print('<');
        out.print('/');
        out.print(name);
      }
      out.println('>');
    }
  }

  public static void appendSimpleElement(final StringBuffer out,
      final int indentLevel, final String name, final String value) {
    if (value != null) {
      if (indentLevel > 0) {
        indent(out, indentLevel);
      }
      out.append('<');
      out.append(name);

      if (value.length() == 0) {
        out.append('/');
      } else {
        out.append('>');
        escapeContent(out, value);
        out.append('<');
        out.append('/');
        out.append(name);
      }
      out.append('>');
    }
  }

  /**
   * Wraps an XmlPullParserException in a SAXException (used for transitioning
   * SAX based parser method to XmlPullParser)
   */
  public static SAXException newSAXException(final XmlPullParserException e) {
    final SAXException saxe = new SAXException("XmlPullParserException");
    saxe.initCause(e);
    return saxe;
  }

  /**
   * Wrapper for common XmlPullParserFactory. Creates and returns a new
   * XmlPullParser around the specified byte array. The returned parser is
   * non-validating and not namespace aware.
   */
  public static XmlPullParser newPullParser(final byte[] data,
      final int offset, final int length) throws UnsupportedEncodingException,
      XmlPullParserException {
    // This is the "proper" way to create XmlPullParsers:
    // ==================================================
    // XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
    // System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
    // factory.setNamespaceAware(false);
    // XmlPullParser xpp = factory.newPullParser();
    // ==================================================

    // ...but instead we use the constructor directly. This shouldn't
    // be a problem for migration as long as everyone uses the
    // XMLUtils method instead of copying this code.

    final XmlPullParser xpp = new org.xmlpull.mxp1.MXParser();

    // detectEncoding is here because MXParser doesn't handle encoding
    // detection. Though it may be faster to do it this way even if
    // it did support encoding detection.
    final Reader input = detectEncoding(data, offset, length, "UTF-8");

    xpp.setInput(input);
    return xpp;
  }

  /**
   * Wrapper for common XmlPullParserFactory. Creates and returns a new
   * XmlPullParser using the specified Reader. The returned parser is
   * non-validating and not namespace aware.
   */
  public static XmlPullParser newPullParser(final Reader reader)
      throws UnsupportedEncodingException, XmlPullParserException {
    final XmlPullParser xpp = new org.xmlpull.mxp1.MXParser();
    xpp.setInput(reader);
    return xpp;
  }

  public static XmlPullParser newPullParser(final byte[] data)
      throws UnsupportedEncodingException, XmlPullParserException {
    return newPullParser(data, 0, data.length);
  }

  /**
   * Returns an instance of a sax parser.
   *
   * @param validate
   *          indicates if the parser should do validation or not.
   * @return A sax parser.
   *
   * @throws SAXException
   *           if the parser could not be created.
   */
  public static final XMLReader newSAXParser(final boolean validate)
      throws SAXException {

    XMLReader parser;

    try {
      parser = (XMLReader) Class.forName("org.apache.xerces.parsers.SAXParser")
          .newInstance();
    } catch (final IllegalAccessException ei) {
      throw new SAXException("Cannot access the Parser");
    } catch (final InstantiationException ci) {
      throw new SAXException("Cannot instantiate the Parser");
    } catch (final ClassNotFoundException ce) {
      throw new SAXException("Cannot find the Parser");
    }

    parser.setFeature("http://xml.org/sax/features/validation", validate);
    parser.setFeature("http://xml.org/sax/features/namespaces", false);
    parser
        .setFeature("http://apache.org/xml/features/validation/schema", false);

    return parser;
  }

  public static final Reader detectEncoding(final byte[] data,
      final String defaultEncoding) throws UnsupportedEncodingException {
    return detectEncoding(data, 0, data.length, defaultEncoding);
  }

  /**
   * Implementation of "Extensible Markup Language (XML) 1.0 (Third
   * Edition)" Section F.1 "Detection Without External Encoding Information".
   *
   * (TODO: JUNIT!!!)
   *
   * @param data
   *          a byte array containing an XML stream
   * @param offset
   *          the start index in data
   * @param length
   *          the length of valid input
   * @param defaultEncoding
   *          the encoding to use if the encoding type cannot be detected
   * @return a Reader around the data. The reader will not include byte-order
   *         marks or parsed XML header.
   * @throws UnsupportedEncodingException
   *           if the XML is in an unsupported encoding
   */
  public static final Reader detectEncoding(final byte[] data,
      final int offset, final int length, final String defaultEncoding)
      throws UnsupportedEncodingException {
    try {
      final int mark = ((data[0]) << 24) | ((data[1] & 0xff) << 16)
          | ((data[2] & 0xff) << 8) | (data[3] & 0xff);

      // Note: Java 1.4 and 1.5 do not support UCS-4 out of the box. We
      // don't expect to see it either, but for the sake of completeness
      // the UCS-4 cases are included. The expected result is that the
      // caller will get an UnsupportedEncodingException when trying to
      // use the encoding later -- this is the correct behavior, and
      // must be handled properly.

      switch (mark) {
      // With a byte order mark
      case 0x0000feff:
        return encodingReader(data, offset + 4, length - 4, "UCS-4-1234");
      case 0xfffe0000:
        return encodingReader(data, offset + 4, length - 4, "UCS-4-4321");
      case 0x0000fffe:
        return encodingReader(data, offset + 4, length - 4, "UCS-4-2143");
      case 0xfeff0000:
        return encodingReader(data, offset + 4, length - 4, "UCS-4-3412");

        // Detect the common UTF-16 and UTF-8 cases in which the
        // byte-order mark is immediately followed by a '<' character.
        // The uncommon cases are checked in the default case.
      case 0xfeff003c: // 0xfeff####:
        return encodingReader(data, offset + 2, length - 2, "UTF-16BE");
      case 0xfffe3c00: // 0xfffe####:
        return encodingReader(data, offset + 2, length - 2, "UTF-16LE");
      case 0xefbbbf3c: // 0xefbbbf##:
        return encodingReader(data, offset + 3, length - 3, "UTF-8");

        // Without a Byte order mark (expect "<?xml ...", 0x3c = '<', 0x3f =
        // '?', etc...)
      case 0x0000003c:
        return readEncodingHeader(data, offset, length, 3, 4, "UCS-4-1234");
      case 0x3c000000:
        return readEncodingHeader(data, offset, length, 0, 4, "UCS-4-4321");
      case 0x00003c00:
        return readEncodingHeader(data, offset, length, 2, 4, "UCS-4-2143");
      case 0x003c0000:
        return readEncodingHeader(data, offset, length, 1, 4, "UCS-4-3412");

      case 0x003c003f:
        return readEncodingHeader(data, offset, length, 1, 2, "UTF-16BE");
      case 0x3c003f00:
        return readEncodingHeader(data, offset, length, 0, 2, "UTF-16LE");
      case 0x3c3f786d:
        // lower ASCII common, (UTF-8, ISO 646, ASCII, 8859, Shift-JIS, EUC,
        // ...)
        return readEncodingHeader(data, offset, length, 0, 1, defaultEncoding);

      case 0x4c6fa794:
        // to be absolutely correct read the encoding value to determine
        // flavor of EBCDIC, but not really an issue for us...
        return encodingReader(data, offset, length, "EBCDIC");
      default:

        // test for UTF-16 or UTF-8 byte-order mark without an
        // immediately following "<?xml" (The #### comments above)
        switch (mark >>> 16) {
        case 0xfeff:
          return encodingReader(data, offset + 2, length - 2, "UTF-16BE");
        case 0xfffe:
          return encodingReader(data, offset + 2, length - 2, "UTF-16LE");
        case 0xefbb:
          if (data[3] == (byte) 0xbf) {
            return encodingReader(data, offset + 3, length - 3, "UTF-8");
          }
        }

        return encodingReader(data, offset, length, defaultEncoding);
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      // normally one should explicitly check array indexes against
      // the length of the input, but since we expect all input to be
      // valid XML, an ArrayIndexOutOfBoundsException is an
      // exceptional case.
      return encodingReader(data, offset, length, defaultEncoding);
    }
  }

  private static Reader encodingReader(final byte[] data, final int offset,
      final int length, final String encoding)
      throws UnsupportedEncodingException {
    return new InputStreamReader(
        new ByteArrayInputStream(data, offset, length), encoding);
  }

  // ASCII encoded strings used by XML encoding detection
  private static final byte[] __XMLDECL;
  private static final byte[] __VERSION;
  private static final byte[] __ENCODING;
  static {
    try {
      __XMLDECL = "<?xml".getBytes("US-ASCII");
      __VERSION = "version".getBytes("US-ASCII");
      __ENCODING = "encoding".getBytes("US-ASCII");
    } catch (final UnsupportedEncodingException e) {
      throw new InternalError("UnsupportedEncodingException " + e);
    }
  }

/**
   * <p>This method is called by detectEncoding to read the XML header
   * after detecting the number of bytes per character.
   *
   * <p>This will process any encoding that is ASCII based.  The
   * <code>inc</code> determines the number of bytes to skip after
   * reading each character.  For example, when processing a possible
   * UTF-8, ISO-8859-1, or US-ASCII encoding, inc should be 1.  When
   * processing a UTF-16 (LE or BE), inc should be 2.  For UCS-4 based
   * encodings, inc should be 4.
   *
   * <p>In any of the multi-byte cases, data[offset] should be the
   * ASCII component of the multi-byte sequence.  For example if the
   * array contains {0,'<',0,'?',...} (i.e. UTF-16BE), offset should
   * be 1.
   *
   * @throws ArrayIndexOutOfBoundsException if data contains a
   * valid-looking but incomplete XML header.
   */
  private static Reader readEncodingHeader(final byte[] data, final int offset,
      final int length, final int asciiOffset, final int inc,
      final String defaultEncoding) throws UnsupportedEncodingException {
    // [3] S ::= (#x20 | #x9 | #xD | #xA)+
    // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
    // [24] VersionInfo ::= S 'version' Eq ("'" VersionNum "'" | '"' VersionNum
    // '"')
    // [25] Eq ::= S? '=' S?
    // [26] VersionNum ::= '1.0'
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' | "'" EncName "'"
    // )

    final long S = (1L << 0x20) | (1L << 0x9) | (1L << 0xD) | (1L << 0xA);
    int off = offset + asciiOffset;

    do {
      // "<?xml"
      if (!matches(data, off, inc, __XMLDECL)) {
        break;
      }

      // System.out.println("matched <?xml");

      // S
      if (!isSpace(data[off += __XMLDECL.length * inc])) {
        break;
      }
      off = skipSpaces(data, off + inc, inc);

      // System.out.println("skipped spaces");

      // "version"
      if (!matches(data, off, inc, __VERSION)) {
        break;
      }

      // System.out.println("matched version");

      // S?
      off = skipSpaces(data, off + __VERSION.length * inc, inc);

      if (data[off] != (byte) '=') {
        break;
      }

      // S?
      off = skipSpaces(data, off + inc, inc);

      // System.out.println("matched =");

      // Quoted VersionNum
      byte quoteChar = data[off];
      if (quoteChar != '\'' && quoteChar != '\"') {
        break;
      }
      if (data[off += inc] != (byte) '1') {
        break;
      }
      if (data[off += inc] != (byte) '.') {
        break;
      }
      if (data[off += inc] != (byte) '0') {
        break;
      }
      if (data[off += inc] != quoteChar) {
        break;
      }

      // System.out.println("matched '1.0'");

      // S
      if (!isSpace(data[off += inc])) {
        break;
      }
      off = skipSpaces(data, off + inc, inc);

      if (!matches(data, off, inc, __ENCODING)) {
        break;
      }

      // System.out.println("matched encoding");

      // S?
      off = skipSpaces(data, off + __ENCODING.length * inc, inc);

      if (data[off] != (byte) '=') {
        break;
      }

      // S?
      off = skipSpaces(data, off + inc, inc);

      // System.out.println("matched =");

      quoteChar = data[off];
      if (quoteChar != '\'' && quoteChar != '\"') {
        break;
      }

      // read contents of quote
      final StringBuffer buf = new StringBuffer();
      while (data[off += inc] != quoteChar) {
        buf.append((char) data[off]);
      }
      final String encoding = buf.toString();

      // System.out.println("encoding = "+encoding);

      // S?
      off = skipSpaces(data, off, inc);

      if (data[off] == (byte) '?' && data[off + inc] == (byte) '>') {
        // we've already read past the XMLDecl, return a reader just
        // past it so the XML parser doesn't have to process it again.
        off = off + inc * 2 - asciiOffset;
        return encodingReader(data, off, length - (off - offset), encoding);
      } else {
        // there was extraneous information in the XMLDecl, we'll let
        // the XML parser handle it, return the whole stream.
        return encodingReader(data, offset, length, encoding);
      }

    } while (false); // for breaks

    // System.out.println("MISMATCH!");

    return encodingReader(data, offset, length, defaultEncoding);
  }

  private static boolean matches(final byte[] data, int off, final int inc,
      final byte[] test) {
    for (int i = 0, n = test.length; i < n; i += 1, off += inc) {
      if (data[off] != test[i]) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSpace(int b) {
    b &= 0xff;
    return (b <= 0x20)
        && (((1L << 0x20) | (1L << 0x9) | (1L << 0xD) | (1L << 0xA)) & (1L << b)) != 0;
  }

  private static int skipSpaces(final byte[] data, int off, final int inc) {
    while (isSpace(data[off])) {
      off += inc;
    }
    return off;
  }


  static class KeysStringComparator extends AbstractNullSafeComparator<String> {
    private static final long serialVersionUID = 1L;

    @Override
    public int compareNonNull(final String o1, final String o2) {
      if (o1 == o2) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      return o1.compareTo(o2);
    }
  }

}
