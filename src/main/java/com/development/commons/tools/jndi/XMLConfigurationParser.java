/* $Id$ */
package com.development.commons.tools.jndi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses an xml config file and creates a properties representation.
 */
public class XMLConfigurationParser extends DefaultHandler {

    private final Properties _properties = new Properties();
    private final Stack<String> _stack = new Stack<String>();

    /**
     * Parse the xml config from input Stream.
     *
     * @param inputStream the input stream
     * @return The xml parsed in Properties
     * @throws SAXException                 when SAXException is thrown by the SAX parser.
     * @throws IOException                  when IO error occurs.
     * @throws ParserConfigurationException when parser configuration error occurs.
     */
    public Properties parse(final InputStream inputStream) //
            throws SAXException, IOException, ParserConfigurationException {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser xmlReader = factory.newSAXParser();
        xmlReader.parse(new InputSource(inputStream), this);
        return _properties;
    }

    /**
     * Receive notification of character data inside an element.
     *
     * @param chars  The characters
     * @param start  The start position in the character array
     * @param length The number of characters to use from the character array
     */
    @Override
    public void characters(final char[] chars, final int start, final int length) {
        String value = new String(chars, start, length);
        value = value.trim();
        if (value.length() != 0) {
            _properties.put(getCurrentXMLPath(), value);
        }
    }

    /**
     * Receive notification of the start of an element.
     *
     * @param uri       a String
     * @param localName a String
     * @param qName     a String
     * @param attrs     an Attributes
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) {
        push(qName);
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri       a String
     * @param localName a String
     * @param qName     a String
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        pop();
    }

    private void push(final String key) {
        _stack.push(key);
    }

    private void pop() {
        _stack.pop();
    }

    private String getCurrentXMLPath() {
        final StringBuffer sb = new StringBuffer();
        for (final Iterator<String> it = _stack.iterator(); it.hasNext(); ) {
            sb.append(it.next() + ".");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
