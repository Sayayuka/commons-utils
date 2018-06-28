/*
 * $Id$
 *
 * Copyright (C) 2006 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools.util.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * HtmlParser -- A forgiving HTML parser based on the XML Pull Parser
 * model.  To parse a complete HTML document or fragment, after
 * creating an HtmlParser, call <code>next()</code> repeatedly until
 * it returns <code>EOF</code>.
 *
 * @see #next()
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class HtmlParser
{
    // ===============================================================
    // Implementation note: there is definite room for optimization in
    // how the text is gathered.  Better memory utilization may be
    // gained by reusing a single StringBuilder.  But caution is advised, as JVM implementations
    // can differ a lot on whether or not reusing the StringBuilder is
    // an effective strategy.  Another option might be to gether HTML
    // content into a character array.
    //
    // Another potential optimization is getting rid of the need to do
    // a toUpperCase on the lookup in _closeTagRequired.
    // ===============================================================

    /**
     * Return value of next() when the parser has encounted text.  Use
     * getText() to get the actual text.  The text has all
     * escaping/entities removed.  Call HtmlUtils.escapeXXX on the
     * getText return value to get the actual text that would appear
     * in the HTML.  next() may sometimes break up text into multiple
     * returns of TEXt, usually this only happens if the parser
     * encounters and error in the input and recovers.
     */
    public static final int TEXT = 1;

    /**
     * Return value of next() when a start tag was encountered.  Use
     * getName() to get the name of the tag.  Use the getAttribute*()
     * methods to get the attributes associated with the tag.
     */
    public static final int START_TAG = 2;

    /**
     * Return value of next() when an end tag was encountered.  Use
     * getName() to get the name of the tag that was closed.  The
     * parser may return extra END_TAG to close off inproperly closed
     * tags in the HTML input.
     */
    public static final int END_TAG = 3;

    /**
     * Return value of next() when a comment was encountered.
     * getText() will return the content of the comment, including the
     * opening and closing comment delimiters.
     */
    public static final int COMMENT = 4;

    /**
     * Return value of next() when the end of input was encountered.
     */
    public static final int EOF = 5;

    /**
     * Return value of next() when the end of input was encountered.
     */
    public static final int DOCTYPE = 6;


    /** internal state -- normal parsing */
    private static final int STATE_NORMAL = 0;
    /** internal state -- encountered an open tag with a close tag
     * such as "&lt;tag /&gt;".  This could be used to provide better
     * support for XHTML, but that would likely break HTML 4.0
     * behavior */
    private static final int STATE_OPENCLOSE = 1;
    /** internal state -- recovering from inproperly nested tags.
     * Tags are being popped off the stack in order to close off a tag
     * specified in the source.  The _popIndex is set to stack index
     * at which point the state will return to STATE_NORMAL. */
    private static final int STATE_POPPING = 2;

    /**
     * This is a set of all the HTML 4.01 tags that require a closing
     * tag.  The list was generated from the loose DTD
     * (http://www.w3.org/TR/html4/sgml/loosedtd.html).  Some tags
     * were added to the list that the DTD does not require, such as
     * TR and TD, because browsers tend to behave poorly without the
     * their close tags in practice.
     */
    private static final Set<String> _closeTagRequired = new HashSet<String>(
        Arrays.asList(
            ("TT,I,B,U,S,STRIKE,BIG,SMALL,EM,STRONG,DFN,CODE,SAMP,KBD,VAR,"+
             "CITE,ABBR,ACRONYM,SUB,SUP,SPAN,BDO,FONT,ADDRESS,DIV,CENTER,A,"+
             "MAP,OBJECT,APPLET,H1,H2,H3,H4,H5,H6,PRE,Q,BLOCKQUOTE,INS,DEL,"+
             "DL,DT,DD,OL,DIR,MENU,UL,FORM,LABEL,SELECT,OPTGROUP,OPTION,"+
             "TEXTAREA,FIELDSET,LEGEND,BUTTON,TABLE,CAPTION,TR,TH,TD,"+
             "FRAMESET,IFRAME,NOFRAMES,TITLE,STYLE,SCRIPT,NOSCRIPT")
            .split(",")));

    /**
     * This is a map of named HTML entities to their character code.
     * (String keys, Character values)
     */
    private static final Map<String,Character> _nameToEntityMap = new HashMap<String,Character>();
    static {
        // Copied from HTML 4.01 spec (with a few regex replaces...)
        //
        // http://www.w3.org/TR/html4/sgml/entities.html#h-24.2.1
        // http://www.w3.org/TR/html4/sgml/entities.html#h-24.3.1
        // http://www.w3.org/TR/html4/sgml/entities.html#h-24.4.1


        // Note on implementation:

        // It would also have been possible to implement this as a
        // series of calls such as:
        //
        // _nameToEntityMap.put("name", new Character((char)1234));
        //
        // The way below however reduces the class size by:
        //
        // 1. having the compiler concatenate the string at compile time
        // (run javap -c -private to verify), so that:
        //
        // 2. the code is reduced from a long sequences of calls to a small loop.
        //
        // However, it would probably be better still to have this
        // data be part of a resource file included in the class path
        // (a .properties file would be simple enough)

        String entities =
             "\u00a0:nbsp,"+
             "\u00a1:iexcl,"+
             "\u00a2:cent,"+
             "\u00a3:pound,"+
             "\u00a4:curren,"+
             "\u00a5:yen,"+
             "\u00a6:brvbar,"+
             "\u00a7:sect,"+
             "\u00a8:uml,"+
             "\u00a9:copy,"+
             "\u00aa:ordf,"+
             "\u00ab:laquo,"+
             "\u00ac:not,"+
             "\u00ad:shy,"+
             "\u00ae:reg,"+
             "\u00af:macr,"+
             "\u00b0:deg,"+
             "\u00b1:plusmn,"+
             "\u00b2:sup2,"+
             "\u00b3:sup3,"+
             "\u00b4:acute,"+
             "\u00b5:micro,"+
             "\u00b6:para,"+
             "\u00b7:middot,"+
             "\u00b8:cedil,"+
             "\u00b9:sup1,"+
             "\u00ba:ordm,"+
             "\u00bb:raquo,"+
             "\u00bc:frac14,"+
             "\u00bd:frac12,"+
             "\u00be:frac34,"+
             "\u00bf:iquest,"+
             "\u00c0:Agrave,"+
             "\u00c1:Aacute,"+
             "\u00c2:Acirc,"+
             "\u00c3:Atilde,"+
             "\u00c4:Auml,"+
             "\u00c5:Aring,"+
             "\u00c6:AElig,"+
             "\u00c7:Ccedil,"+
             "\u00c8:Egrave,"+
             "\u00c9:Eacute,"+
             "\u00ca:Ecirc,"+
             "\u00cb:Euml,"+
             "\u00cc:Igrave,"+
             "\u00cd:Iacute,"+
             "\u00ce:Icirc,"+
             "\u00cf:Iuml,"+
             "\u00d0:ETH,"+
             "\u00d1:Ntilde,"+
             "\u00d2:Ograve,"+
             "\u00d3:Oacute,"+
             "\u00d4:Ocirc,"+
             "\u00d5:Otilde,"+
             "\u00d6:Ouml,"+
             "\u00d7:times,"+
             "\u00d8:Oslash,"+
             "\u00d9:Ugrave,"+
             "\u00da:Uacute,"+
             "\u00db:Ucirc,"+
             "\u00dc:Uuml,"+
             "\u00dd:Yacute,"+
             "\u00de:THORN,"+
             "\u00df:szlig,"+
             "\u00e0:agrave,"+
             "\u00e1:aacute,"+
             "\u00e2:acirc,"+
             "\u00e3:atilde,"+
             "\u00e4:auml,"+
             "\u00e5:aring,"+
             "\u00e6:aelig,"+
             "\u00e7:ccedil,"+
             "\u00e8:egrave,"+
             "\u00e9:eacute,"+
             "\u00ea:ecirc,"+
             "\u00eb:euml,"+
             "\u00ec:igrave,"+
             "\u00ed:iacute,"+
             "\u00ee:icirc,"+
             "\u00ef:iuml,"+
             "\u00f0:eth,"+
             "\u00f1:ntilde,"+
             "\u00f2:ograve,"+
             "\u00f3:oacute,"+
             "\u00f4:ocirc,"+
             "\u00f5:otilde,"+
             "\u00f6:ouml,"+
             "\u00f7:divide,"+
             "\u00f8:oslash,"+
             "\u00f9:ugrave,"+
             "\u00fa:uacute,"+
             "\u00fb:ucirc,"+
             "\u00fc:uuml,"+
             "\u00fd:yacute,"+
             "\u00fe:thorn,"+
             "\u00ff:yuml,"+
             "\u0192:fnof,"+
             "\u0391:Alpha,"+
             "\u0392:Beta,"+
             "\u0393:Gamma,"+
             "\u0394:Delta,"+
             "\u0395:Epsilon,"+
             "\u0396:Zeta,"+
             "\u0397:Eta,"+
             "\u0398:Theta,"+
             "\u0399:Iota,"+
             "\u039a:Kappa,"+
             "\u039b:Lambda,"+
             "\u039c:Mu,"+
             "\u039d:Nu,"+
             "\u039e:Xi,"+
             "\u039f:Omicron,"+
             "\u03a0:Pi,"+
             "\u03a1:Rho,"+
             "\u03a3:Sigma,"+
             "\u03a4:Tau,"+
             "\u03a5:Upsilon,"+
             "\u03a6:Phi,"+
             "\u03a7:Chi,"+
             "\u03a8:Psi,"+
             "\u03a9:Omega,"+
             "\u03b1:alpha,"+
             "\u03b2:beta,"+
             "\u03b3:gamma,"+
             "\u03b4:delta,"+
             "\u03b5:epsilon,"+
             "\u03b6:zeta,"+
             "\u03b7:eta,"+
             "\u03b8:theta,"+
             "\u03b9:iota,"+
             "\u03ba:kappa,"+
             "\u03bb:lambda,"+
             "\u03bc:mu,"+
             "\u03bd:nu,"+
             "\u03be:xi,"+
             "\u03bf:omicron,"+
             "\u03c0:pi,"+
             "\u03c1:rho,"+
             "\u03c2:sigmaf,"+
             "\u03c3:sigma,"+
             "\u03c4:tau,"+
             "\u03c5:upsilon,"+
             "\u03c6:phi,"+
             "\u03c7:chi,"+
             "\u03c8:psi,"+
             "\u03c9:omega,"+
             "\u03d1:thetasym,"+
             "\u03d2:upsih,"+
             "\u03d6:piv,"+
             "\u2022:bull,"+
             "\u2026:hellip,"+
             "\u2032:prime,"+
             "\u2033:Prime,"+
             "\u203e:oline,"+
             "\u2044:frasl,"+
             "\u2118:weierp,"+
             "\u2111:image,"+
             "\u211c:real,"+
             "\u2122:trade,"+
             "\u2135:alefsym,"+
             "\u2190:larr,"+
             "\u2191:uarr,"+
             "\u2192:rarr,"+
             "\u2193:darr,"+
             "\u2194:harr,"+
             "\u21b5:crarr,"+
             "\u21d0:lArr,"+
             "\u21d1:uArr,"+
             "\u21d2:rArr,"+
             "\u21d3:dArr,"+
             "\u21d4:hArr,"+
             "\u2200:forall,"+
             "\u2202:part,"+
             "\u2203:exist,"+
             "\u2205:empty,"+
             "\u2207:nabla,"+
             "\u2208:isin,"+
             "\u2209:notin,"+
             "\u220b:ni,"+
             "\u220f:prod,"+
             "\u2211:sum,"+
             "\u2212:minus,"+
             "\u2217:lowast,"+
             "\u221a:radic,"+
             "\u221d:prop,"+
             "\u221e:infin,"+
             "\u2220:ang,"+
             "\u2227:and,"+
             "\u2228:or,"+
             "\u2229:cap,"+
             "\u222a:cup,"+
             "\u222b:int,"+
             "\u2234:there4,"+
             "\u223c:sim,"+
             "\u2245:cong,"+
             "\u2248:asymp,"+
             "\u2260:ne,"+
             "\u2261:equiv,"+
             "\u2264:le,"+
             "\u2265:ge,"+
             "\u2282:sub,"+
             "\u2283:sup,"+
             "\u2284:nsub,"+
             "\u2286:sube,"+
             "\u2287:supe,"+
             "\u2295:oplus,"+
             "\u2297:otimes,"+
             "\u22a5:perp,"+
             "\u22c5:sdot,"+
             "\u2308:lceil,"+
             "\u2309:rceil,"+
             "\u230a:lfloor,"+
             "\u230b:rfloor,"+
             "\u2329:lang,"+
             "\u232a:rang,"+
             "\u25ca:loz,"+
             "\u2660:spades,"+
             "\u2663:clubs,"+
             "\u2665:hearts,"+
             "\u2666:diams,"+
             "\":quot,"+
             "\u0026:amp,"+
             "\u003c:lt,"+
             "\u003e:gt,"+
             "\u0152:OElig,"+
             "\u0153:oelig,"+
             "\u0160:Scaron,"+
             "\u0161:scaron,"+
             "\u0178:Yuml,"+
             "\u02c6:circ,"+
             "\u02dc:tilde,"+
             "\u2002:ensp,"+
             "\u2003:emsp,"+
             "\u2009:thinsp,"+
             "\u200c:zwnj,"+
             "\u200d:zwj,"+
             "\u200e:lrm,"+
             "\u200f:rlm,"+
             "\u2013:ndash,"+
             "\u2014:mdash,"+
             "\u2018:lsquo,"+
             "\u2019:rsquo,"+
             "\u201a:sbquo,"+
             "\u201c:ldquo,"+
             "\u201d:rdquo,"+
             "\u201e:bdquo,"+
             "\u2020:dagger,"+
             "\u2021:Dagger,"+
             "\u2030:permil,"+
             "\u2039:lsaquo,"+
             "\u203a:rsaquo,"+
             "\u20ac:euro";

        for (String entity : entities.split(",")) {
            _nameToEntityMap.put(entity.substring(2),entity.charAt(0));
        }
    }

    /**
     * HTML data to parse
     */
    char[] _data;
    /**
     * Next character offset within _data to parse.
     */
    int _offset;
    /**
     * The offset at which to stop parsing.  _data[_limit-1] is the
     * last character that will be parsed.  _data[_limit] is an
     * invalid index.
     */
    int _limit;

    /**
     * One of the STATE_ constants
     */
    int _state;

    /**
     * The text gathered from parsing TEXT and COMMENT blocks.
     */
    StringBuilder _text;

    /**
     * The name of the tag for START_TAG or END_TAG
     */
    String _tagName;

    /**
     * The number of attributes parsed with the start tag
     */
    int _attrCount;

    /**
     * The names of the attributes parsed.
     */
    String[] _attrNames;

    /**
     * The values of the attributes parsed.
     */
    String[] _attrValues;

    /**
     * The index into the _elementStack at which the parser should
     * stop popping of END_TAGs when _state == STATE_POPPING.  Used to
     * properly close off tags.
     */
    int _popIndex;

    /**
     * A stack of opened (START_TAG) elements that require closing
     * tags (END_TAG).  Elements are pushed and popped from the end of
     * the ArrayList to get O(1) performance.
     */
    ArrayList _elementStack;

    /**
     * Creates an HTMLParser to parse and HTML string.  The next call
     * should probably be to "next()".
     *
     * @str the string to parse.
     * @see #next()
     */
    public HtmlParser(String str) {
        _data = str.toCharArray();
        _offset = 0;
        _limit = _data.length;

        _attrCount = 0;
        _attrNames = new String[16];
        _attrValues = new String[16];

        _state = STATE_NORMAL;

        _popIndex = -1;
        _elementStack = new ArrayList(16);
    }

    /**
     * Decodes a named entity into a Unicode character.  For example
     * "nbsp" as input returns "\u00a0" (the Unicode non-breaking
     * space character).  This handles all named entities defined in
     * the HTML 4.01 spec.
     *
     * @param entityName the named entity
     * @return the entity corresponding to the character or null if
     * there is no such entity.
     */
    public static final Character decodeNamedEntity(String entityName) {
        return _nameToEntityMap.get(entityName);
    }

    /**
     * Tests if a character is a whitespace character according to
     * SGML.
     * @param ch the character to test
     * @return true if ch is a space character, false if not.
     */
    public static final boolean isSpace(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
    }

    /**
     * Tests if a character is an alphabetic character.  This is used
     * for parsing tag and attribute names.  SGML and XML allow more
     * characters than just the ASCII A-Z that this method tests for,
     * but HTML does not define any tags or attributes that would use
     * those characters.
     */
    private static final boolean isAlpha(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    /**
     * Checks if the character is alphanumeric.
     * @param ch character to check
     * @return true if alphanumeric
     */
    private static final boolean isAlphaNumeric(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9') ;
    }

    /**
     * Checks if the character is valid part of XMl attribute
     * @param ch character to check
     * @return true if valid
     */
    private static final boolean isAttrNamePart(char ch) {
    	return isAlphaNumeric(ch) || ch == ':' || ch == '-';
    }

    /**
     * Skips 0 or more spaces.  Returns true if at least one space was
     * skipped.  On return _offset == _limit or _data[_offset] is not
     * a space.
     */
    private boolean skipSpaces() {
        if (_offset >= _limit) {
            return false;
        }

        if (!isSpace(_data[_offset])) {
            return false;
        }

        do {
            if (++_offset >= _limit) {
                return true;
            }
        } while (isSpace(_data[_offset]));

        return true;
    }

    /**
     * Returns the text parsed when next() returns TEXT or COMMENT.
     */
    public String getText() {
        return _text.toString();
    }

    /**
     * Returns the name of the tag when next() returns START_TAG or
     * END_TAG
     */
    public String getName() {
        return _tagName;
    }

    /**
     * Returns the number of attributes found on the last tag.
     *
     * @return the number of attributes found on the last tag
     */
    public int getAttributeCount() {
        return _attrCount;
    }

    /**
     * Returns the name of the attribute at index <tt>index</tt>.
     *
     * @param index the index of the value
     * @return name of the attribute at <tt>index</tt>.
     */
    public String getAttributeName(int index) {
        if (index >= _attrCount) {
            throw new IndexOutOfBoundsException(index+" >= "+_attrCount);
        }
        return _attrNames[index];
    }

    /**
     * Returns the name of the attribute at index <tt>index</tt>.
     *
     * @param index the index of the value
     * @return the value of the attribute at <tt>index</tt>.  An
     * attribute value may be <code>null</code> if the HTML did not
     * specify a value.
     */
    public String getAttributeValue(int index) {
        if (index >= _attrCount) {
            throw new IndexOutOfBoundsException(index+" >= "+_attrCount);
        }
        return _attrValues[index];
    }

    /**
     * Returns the next parsed HTML token type.
     *
     * <ul>
     *   <li>TEXT - for text nodes.  Get the text using getText()</li>
     *   <li>START_TAG - for open element tag.  Get the tag name using getName() and get the attributes using getAttribute*() methods</li>
     *   <li>END_TAG - for close element tag.  Get the tag name using getName()
     *   <li>COMMENT - for an HTML comment.  Get the comment text, including delimiters, using getText()</li>
     *   <li>EOF - for the end of the input stream.</li>
     * </ul>
     *
     * @see #TEXT
     * @see #START_TAG
     * @see #END_TAG
     * @see #COMMENT
     * @see #EOF
     *
     * @return one of TEXT, START_TAG, END_TAG, COMMENT, or EOF.
     */
    public int next() {
        char ch;
        int n;

        switch (_state) {
        case STATE_OPENCLOSE:
            _state = STATE_NORMAL;

            if (_closeTagRequired.contains(_tagName.toUpperCase())) {
              // if the close tag is required pop it from the stack
              // (it was pushed by startTag), and return the end tag.

              _elementStack.remove(_elementStack.size()-1);
              return END_TAG;
            }

            // For XHTML support this should always return END_TAG
            // (e.g. close tags are always required).  But note that
            // <img ... /> will be treated as <img ...></img>, which
            // will cause most browsers to barf on some level.

            break;

        case STATE_POPPING:
            popTags();
            return END_TAG;
        }

      for(;;) {
        if (_offset == _limit) {

            n = _elementStack.size();
            while (n > 0 && !_closeTagRequired.contains(((String)_elementStack.get(n-1)).toUpperCase())) {
                _elementStack.remove(--n);
            }

            if (n != 0) {
                // close off opened tags before returning EOF
                _tagName = (String)_elementStack.remove(n-1);
                return END_TAG;
            }

            return EOF;
        }

        if (_data[_offset] == '<') {
            if (++_offset >= _limit) {
                _text = new StringBuilder("<");
                return TEXT;
            }

            ch = _data[_offset];

            switch (ch) {
            case '!':
                return parseComment();
            case '/':
                return parseEndTag();
            case '?':
                stripProcessingInstruction();
                break;
            default:
                if (isAlpha(ch)) {
                    return parseStartTag();
                } else {
                    _text = new StringBuilder("<");
                    parseText();
                    return TEXT;
                }
            }
        } else {
            _text = new StringBuilder();
            parseText();
            return TEXT;

        }
      }
    }

    /**
     * Parses text content.  Assumes that _text has already be
     * initialized before this method is called.
     */
    private void parseText() {
        while (_offset < _limit) {
            char ch = _data[_offset];
            switch (ch) {
            case '&':
                parseAmp(_text);
                break;
            case '<':
                return;
            default:
                _text.append(ch);
                _offset++;
            }
        }
    }

    /**
     * parseAmp is called by parseText to handle entity references.
     * It will return with _data[_offset] positioned after all the
     * characters processed by this method.  E.g. the next character
     * that the caller should process will be in _data[_offset].
     */
    private void parseAmp(StringBuilder buf) {
        assert _offset < _limit;
        assert _data[_offset] == '&';

        int startOffset = _offset;

        if (++_offset >= _limit) {
            buf.append('&');
            return;
        }

        char ch = _data[_offset];

        if (ch == '#') {
            // numeric reference

            if (++_offset >= _limit) {
                buf.append(_data, startOffset, 2); // '&#'
                return;
            }

            ch = _data[_offset];

            if (ch == 'x' || ch == 'X') {
                // hexidecimal

                int value = 0;

                for (;;) {
                    if (++_offset >= _limit) {
                        buf.append(_data, startOffset, _offset - startOffset);
                        return;
                    }

                    ch = _data[_offset];

                    if (ch == ';') {
                        if (_offset == startOffset + 3) {
                            // didn't see any hex letters yet, this is
                            // not a valid escape, just output
                            // directly
                            buf.append(_data, startOffset, _offset - startOffset);
                        } else {
                            buf.append((char)value);
                        }
                        _offset++;
                        return;
                    }

                    value = value * 16;

                    if ('0' <= ch && ch <= '9') {
                        value += ch - '0';
                    } else if ('a' <= ch && ch <= 'f') {
                        value += ch - ('a' - 10);
                    } else if ('A' <= ch && ch <= 'F') {
                        value += ch - ('A' - 10);
                    } else {
                        // bad character.  Let the caller handle it.
                        // (e.g. don't increment _offset)

                        // append what we've encountered up to now as text.
                        buf.append(_data, startOffset, _offset - startOffset);
                        return;
                    }
                }

            } else if ('0' <= ch && ch <= '9') {
                // decimal

                int value = ch - '0';

                for (;;) {
                    if (++_offset >= _limit) {
                        buf.append(_data, startOffset, _offset - startOffset);
                        return;
                    }

                    ch = _data[_offset];

                    if (ch == ';') {
                        _offset++;
                        buf.append((char)value);
                        return;
                    }

                    if ('0' <= ch && ch <= '9') {
                        value = value * 10 + (ch - '0');
                    } else {
                        // bad character.  Let the caller handle it.
                        // (e.g. don't increment _offset)

                        // append what we've encountered up to now as text.
                        buf.append(_data, startOffset, _offset - startOffset);
                        return;
                    }
                }

            }

        } else if (isAlpha(ch)) {
            // character entity reference (by name)

            for (;;) {
                if (++_offset >= _limit) {
                    buf.append(_data, startOffset, _offset - startOffset);
                    return;
                }

                ch = _data[_offset];

                if (ch == ';') {
                    String name = new String(_data, startOffset+1, _offset - startOffset - 1);

                    // System.out.println("Got Entity Name: '"+name+"'");

                    Character entity = (Character)_nameToEntityMap.get(name);
                    if (entity == null) {
                        // unknown entity, output all as text
                        buf.append(_data, startOffset, _offset - startOffset);
                    } else {
                        buf.append(entity.charValue());
                    }
                    ++_offset;
                    return;
                }

                if (!isAlpha(ch) && !('0' <= ch && ch <= '9')) {
                    // bad character.  Let the caller handle it.
                    // (e.g. don't increment _offset)

                    // append what we've encountered up to now as text.
                    buf.append(_data, startOffset, _offset - startOffset);
                    return;
                }
            }

        } else {
            buf.append('&');
        }
    }

    /**
     * Parses a comment.  If the comment parse fails, this method
     * falls back to parsing as text.
     *
     * @return the actual type of content parsed (either COMMENT or
     * TEXT)
     */
    public int parseComment() {
        assert _offset < _limit;
        assert _data[_offset] == '!';
        assert _data[_offset-1] == '<';

        int startOffset = _offset - 1;

        _text = new StringBuilder();

        if (++_offset >= _limit) {
            _text.append("<!");
            return TEXT;
        }

       //for <!DOCTYPE htmlxxxxxxxxx >
       //This is not a comment but Text
       if (_data[_offset] == 'D' || _data[_offset] == 'd') {
            _tagName = "!DOCTYPE";
            _text.append("<!");
            parseText();
            return DOCTYPE;
        }
        if (_data[_offset] != '-') {
            _text.append("<!");
            parseText();
            return TEXT;
        }

        if (++_offset >= _limit) {
            _text.append("<!-");
            parseText();
            return TEXT;
        }

        if (_data[_offset] != '-') {
            _text.append("<!-");
            parseText();
            return TEXT;
        }

        for (;;) {
            if (++_offset >= _limit) {
                _text.append(_data, startOffset, _offset - startOffset);
                return TEXT;
            }

            if (_data[_offset] != '-') {
                continue;
            }

            if (++_offset >= _limit) {
                _text.append(_data, startOffset, _offset - startOffset);
                return TEXT;
            }

            if (_data[_offset] != '-') {
                continue;
            }

            if (++_offset >= _limit) {
                _text.append(_data, startOffset, _offset - startOffset);
                return TEXT;
            }


            if (_data[_offset] == '>') {
                _text.append(_data, startOffset, ++_offset - startOffset);
                return COMMENT;
            } else {
                _text.append(_data, startOffset, _offset - startOffset);
                parseText();
                return TEXT;
            }
        }
    }

    /**
     * Parses a START_TAG.  If any errors are encounted, the parser
     * falls back to parsing as TEXT.
     *
     * @return the actual type of HTML parsed (either START_TAG or
     * TEXT)
     */
    public int parseStartTag() {
        assert _offset < _limit;
        assert _data[_offset - 1] == '<';

        int startOffset = _offset - 1;
        int rollBack = -1;

        char ch = _data[_offset];

        assert isAlpha(ch);

        _attrCount = 0;

        for (;;) {
            if (++_offset >= _limit) {
                _text = new StringBuilder().append(_data, startOffset, _offset - startOffset);
                return TEXT;
            }

            ch = _data[_offset];

            if (!isAlphaNumeric(ch) && ch != ':' && ch != '-') {
                break;
            }
        }

        _tagName = new String(_data, startOffset + 1, _offset - startOffset - 1);

        if (skipSpaces()) {
            boolean skippedSpaces;
            do {
                String attrName = parseAttrName();
                String attrValue;

                if (attrName == null) {
                    break;
                }

                skippedSpaces = skipSpaces();

                if (_offset < _limit && _data[_offset] == '=') {
                    ++_offset;
                    skipSpaces();
                    attrValue = parseAttrValue();
                    skippedSpaces = skipSpaces();
                } else {
                    attrValue = null;
                }

                addAttribute(attrName, attrValue);
            } while (skippedSpaces);
        }

        if (_offset >= _limit) {
            _text = new StringBuilder().append(_data, startOffset, _offset - startOffset);
            return TEXT;
        }

        ch = _data[_offset];

        if (ch == '>') {
            ++_offset;
            startTag();
            return START_TAG;
        }

        if (ch == '/') {
            if (++_offset >= _limit) {
                _text = new StringBuilder().append(_data, startOffset, _offset - startOffset);
                return TEXT;
            }

            if (_data[_offset] == '>') {
                _state = STATE_OPENCLOSE;
                ++_offset;
                startTag();
                return START_TAG;
            }

            _text = new StringBuilder().append(_data, startOffset, _offset - startOffset);
            parseText();
            return TEXT;
        }

        _text = new StringBuilder().append(_data, startOffset, _offset - startOffset);
        parseText();
        return TEXT;
    }

    private void addAttribute(String name, String value) {
        if (_attrCount >= _attrNames.length) {
            int newLen = _attrNames.length * 2;
            _attrNames = grow(_attrNames, newLen);
            _attrValues = grow(_attrValues, newLen);
        }

        _attrNames[_attrCount] = name;
        _attrValues[_attrCount] = value;

        ++_attrCount;
    }

    private static String[] grow(String[] array, int newLen) {
        String[] tmp = new String[newLen];
        System.arraycopy(array, 0, tmp, 0, array.length);
        return tmp;
    }

    /**
     * Called by parseStartTag when a valid start tag was parsed.
     * This method handles updating internal state related to the
     * start of a tag.  _tagName is expected to already be filled in
     * with the started tag.
     */
    private void startTag() {
        // If the DTD specifies that the close tag is optional, don't
        // push the element onto the stack.  This is especially
        // important for tags like <img> where automatic popping of
        // </img> would produce bad HTML.

        // If the tag is not defined in the DTD, then we let it
        // through as though the close tag is optional, yet we still
        // return it as a tag.  This allows MS extensions like <o:p>
        // through.

      //if (_closeTagRequired.contains(_tagName.toUpperCase())) {
            _elementStack.add(_tagName);
      //}
    }

    /**
     * Parses and returns attribute name.  If the next characters do
     * not make up a valid attribute name, this method returns
     * <code>null</code> instead.
     */
    private String parseAttrName() {
        if (_offset >= _limit) {
            return null;
        }

        if (!isAlpha(_data[_offset])) {
            return null;
        }

        int startOffset = _offset;

        do {
            if (++_offset >= _limit) {
                break;
            }
        } while (isAttrNamePart(_data[_offset]));

        return new String(_data, startOffset, _offset - startOffset);
    }

    /**
     * Parses and returns and attribute value.
     */
    private String parseAttrValue() {
        if (_offset >= _limit) {
            return "";
        }

        char quoteCh = _data[_offset];
        StringBuilder value = new StringBuilder();

        if (quoteCh == '\'' || quoteCh == '\"') {
            if (++_offset >= _limit) {
                return "";
            }

            while (_offset < _limit) {
                char ch = _data[_offset];

                if (ch == quoteCh) {
                    ++_offset;
                    return value.toString();
                }

                if (ch == '&') {
                    parseAmp(value);
                } else {
                    value.append(ch);

                    if (++_offset >= _limit) {
                        return "";
                    }
                }
            }
        } else {
            while (!isSpace(quoteCh) && quoteCh != '>') {
                if (quoteCh == '&') {
                    parseAmp(value);
                } else {
                    value.append(quoteCh);

                    if (++_offset >= _limit) {
                        return "";
                    }
                }

                quoteCh = _data[_offset];
            }

        }
        return value.toString();
    }

    /**
     * Parses a closing tag.  If the parse fails because of bad input,
     * this method will parse the content that looked like and end tag
     * as TEXT instead.
     *
     * @return the actual HTML type parsed (either END_TAG or TEXT)
     */
    public int parseEndTag() {
        assert _offset < _limit;
        assert _data[_offset] == '/';
        assert _data[_offset - 1] == '<';

        int startOffset = _offset;

        if (++_offset >= _limit) {
            _text = new StringBuilder("</");
            return TEXT;
        }

        char ch = _data[_offset];

        if (!isAlpha(ch)) {
            _text = new StringBuilder("</");
            parseText();
            return TEXT;
        }

        do {
            if (++_offset >= _limit) {
                _text = new StringBuilder()
                    .append(_data, startOffset-1, _offset - startOffset + 1);
                return TEXT;
            }

            ch = _data[_offset];
        } while (isAlphaNumeric(ch) || ch == ':' || ch=='-');

        if (ch == '>') {
            String endedTag = new String(_data, startOffset+1, _offset - startOffset - 1);
            _offset++;
            _popIndex = lastIndexOfIgnoreCase(_elementStack, endedTag);

            if (_popIndex == -1) {
                // closed off a tag that isn't currently open, don't
                // pop off anything, instead return this close tag as
                // text.  This should avoid any problems of having
                // dynamic content close off its containing static
                // layout.

            	_text = new StringBuilder("");
                return TEXT;
            }

            // preserve the case of the closed tag
            _elementStack.set(_popIndex, endedTag);
            popTags();
            return END_TAG;
        }

        _text = new StringBuilder()
            .append(_data, startOffset-1, _offset - startOffset + 1);

        parseText();
        return TEXT;
    }

    /**
     * Pops off optional tags from the stack until the pop index is
     * reached or a required tag is reached.  If a required tag is
     * reached, it is returned to the caller and the state is set to
     * STATE_POPPING.  Otherwise the popIndex close tag is returned,
     * and the state is returned to normal.
     */
    private void popTags() {
        int top = _elementStack.size() - 1;
        while (top > _popIndex
               && !_closeTagRequired.contains(((String)_elementStack.get(top)).toUpperCase()))
        {
            _elementStack.remove(top--);
        }

        _tagName = (String)_elementStack.remove(top);

        _state = (_popIndex == top)
            ? STATE_NORMAL : STATE_POPPING;
    }

  /**
   * EE-15806
   * removes xml process instructions, first encounteres when cutting and pasting to the rte from msword.
   * this method is triggered when "?" follows "<".  this whole tag is then removed.
   */
  private void stripProcessingInstruction() {
        assert _offset < _limit;
        assert _data[_offset - 1] == '<';
        assert _data[_offset] == '?';

        for (;;) {
          if(++_offset >= _limit) {
            break;
          }

          if (_data[_offset] == '>') {
            ++_offset;
            break;
          }
        }
    }


    /**
     * Quick (re-)implementation of ArrayList.lastIndexOf that does a
     * case-insensitive comparison instead of the standard .equals
     * call.
     */
    private static int lastIndexOfIgnoreCase(ArrayList list, String str) {
        if (str == null) {
            // this branch is added for completeness.
            // Case-insensitivity doesn't apply to null, so just call
            // the original implemention that has direct underlying
            // array access instead (read: "faster").
            return list.lastIndexOf(str);
        }

        for (int i=list.size() ; --i >= 0 ; ) {
            if (str.equalsIgnoreCase((String)list.get(i))) {
                return i;
            }
        }

        return -1;
    }

} // HtmlParser
