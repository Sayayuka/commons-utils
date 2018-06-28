/*
 * $Id$
 *
 * Copyright (C) 2005 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools.util.html;

import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSSStyleParser
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class CSSStyleParser {

    static final boolean debug = false;

    CharBuffer _buffer;

    int _tokenKind;
    int _tokenColor;
    float _tokenNumber;
    String _tokenString;
    String _tokenUnits;

    public CSSStyleParser(CharSequence style) {
        char[] charArray = style.toString().toCharArray(); // Workaround for AYT-3651
        _buffer = CharBuffer.wrap(charArray);
    }

    //==================================================================
    // Callbacks
    //==================================================================

    protected void declaration(String name) throws ParseException {
    }

    /**
     * Called by default from the typed (number, percent, length,
     * string, ident, and color) callbacks with a CSS formated string
     * value.  This allows implementations that don't care about typing
     * to just override one callback.  The string will be formatted in
     * CSS notation suitable for directly copying into a style
     * attribute -- including all necessary escaping.
     */
    protected void value(String str) throws ParseException {
    }

    protected void number(float number) throws ParseException {
        value(Float.toString(number));
    }

    protected void percent(float percent) throws ParseException {
        value((int) (percent + 0.5f) + "%");
    }

    protected void length(float length, String units) throws ParseException {
        value(length + units);
    }

    protected void string(String string) throws ParseException {
        value("'" + CSSUtils.escapeString(string) + "'");
    }

    protected void ident(String ident) throws ParseException {
        value(ident);
    }

    protected void color(int rgb) throws ParseException {
        String str = Integer.toHexString(rgb | 0x1000000);
        value("#" + str.substring(str.length() - 6));
    }

    protected void url(String url) throws ParseException {
        value("url('" + CSSUtils.escapeString(url) + "')");
    }

    protected void comma() throws ParseException {
    }

    protected void important() throws ParseException {
    }

    //==================================================================
    // Parsing Routines
    //==================================================================

    private static final int
            EOF = -1,
            IDENT = 0,
            COLON = 1,
            MINUS = 2,
            PLUS = 3,
            COLOR = 4,
            URL = 5,
            STRING = 6,
            NUMBER = 7,
            SLASH = 8,
            COMMA = 9,
            SEMICOLON = 10,
            IMPORTANT = 11;

    public void parse() throws ParseException {
        _tokenKind = nextToken();

        if (_tokenKind == EOF) {
            return;
        }

        parseDeclarations();
    }

    public int position() {
        return _buffer.position();
    }

    void parseDeclarations() throws ParseException {
        // declarations
        //   : declaration [ ';' declaration ]*

        if (_tokenKind == IDENT) {
            parseDeclaration();
        }

        for (; ; ) {
            if (_tokenKind == SEMICOLON) {
                _tokenKind = nextToken();
            }

            if (_tokenKind == EOF) {
                return;
            }

            parseDeclaration();
        }
    }

    void parseDeclaration() throws ParseException {
        // declaration
        //   : IDENT ':' expr prio?
        //   | /* empty */
        //   ;

        if (_tokenKind != IDENT) {
            throw new ParseException(
                    "expected style name", _buffer.position());
        }

//    if (debug) logger.info("declaration: '"+_tokenString+"'");

        declaration(_tokenString);

        if (nextToken() != COLON) {
            throw new ParseException(
                    "expected ':' after style name", _buffer.position());
        }

        _tokenKind = nextToken();
        parseExpr();

        // the prio? part
        if (_tokenKind == IMPORTANT) {
            important();
            _tokenKind = nextToken();
        }
    }

    void parseExpr() throws ParseException {
        // expr
        //   : term [ operator term ]*
        //   ;
        // operator
        //   : '/' | ',' | /* empty */
        //   ;

        parseTerm();

        for (; ; ) {
            switch (_tokenKind) {
                case EOF:
                case SEMICOLON:
                case IMPORTANT:
                    return;
                case COMMA:
                    comma();
                    // (fall through to next "case" block...)
                case SLASH:
                    _tokenKind = nextToken();
                    break;
            }

            parseTerm();
        }
    }

    void parseTerm() throws ParseException {
        // term
        //   : unary_operator?
        //     [ NUMBER | STRING | PERCENTAGE | LENGTH | EMS | EXS
        //     | IDENT | hexcolor | URL | RGB ]
        //   ;

        if (_tokenKind == PLUS || _tokenKind == MINUS) {
            _tokenKind = nextToken();
        }

        switch (_tokenKind) {
            case NUMBER:
//                if (debug) logger.info("  number: '" + _tokenNumber + "' units='" + _tokenUnits + "'");

                if (_tokenUnits == null) {
                    number(_tokenNumber);
                } else if ("%".equals(_tokenUnits)) {
                    percent(_tokenNumber);
                } else {
                    length(_tokenNumber, _tokenUnits);
                }
                break;

            case STRING:
//      if (debug) logger.info("  string: '"+_tokenString+"'");

                string(_tokenString);
                break;

            case IDENT:
//      if (debug) logger.info("  ident: '"+_tokenString+"'");

                ident(_tokenString);
                break;

            case COLOR:
//      if (debug) logger.info("  color: '"+Integer.toHexString(_tokenColor)+"'");

                color(_tokenColor);
                break;

            case URL:
//      if (debug) logger.info("  url: ...");
                url(_tokenString);
                break;

            default:
                throw new ParseException(
                        "expected style value", _buffer.position());
        }

        _tokenKind = nextToken();
    }

    //==================================================================
    // Tokenizer Routines
    //==================================================================

    private static Pattern PATTERN_IDENT = Pattern.compile(
            "[a-zA-Z\u00a1-\u00ff][-a-zA-Z0-9\u00a1-\u00ff]*");
    private static final Pattern PATTERN_HEXCOLOR = Pattern.compile(
            "#([0-9a-fA-F]+)");
    private static final Pattern PATTERN_RGB = Pattern.compile(
            "rgb\\(" +
                    "\\s*([0-9]+(?:\\.[0-9]+)?|\\.[0-9]+)(%?)\\s*," +
                    "\\s*([0-9]+(?:\\.[0-9]+)?|\\.[0-9]+)(%?)\\s*," +
                    "\\s*([0-9]+(?:\\.[0-9]+)?|\\.[0-9]+)(%?)\\s*\\)",
            Pattern.CASE_INSENSITIVE);

    private static final String REGEX_STRING =
            "\"(?:[\\t -~\200-\377&&[^\"\\\\]]|(?:\\\\[\\r\\n\\t -~\200-\377]))*\"|" +
                    "\'(?:[\\t -~\200-\377&&[^\'\\\\]]|(?:\\\\[\\r\\n\\t -~\200-\377]))*\'";

    //according to http://stackoverflow.com/questions/15082010/stackoverflowerror-when-matching-large-input-using-regex
    //adding + at the end to resolve StackOverflowError problem to make quantifier possessive
    private static final String REGEX_URL =
            "(?:[!-~&&[^\'\"\\(\\)\\\\]]|\\\\(?:[ -~\200-\377&&[^0-9a-fA-F]]|[0-9a-fA-F]{1,4}[ \\t\\r\\n]?))*+";

    private static final Pattern PATTERN_STRING = Pattern.compile(
            REGEX_STRING);

    private static final Pattern PATTERN_URL = Pattern.compile(
            "[uU][rR][lL]\\(\\s*((?:" + REGEX_STRING + ")|(?:" + REGEX_URL + "))\\s*\\)");

    private static final Pattern PATTERN_NUMBER = Pattern.compile(
            "([+-]?\\d+(?:\\.\\d+)?|\\.\\d+)(%|pt|mm|cm|pc|in|px|em|ex)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_IMPORTANT = Pattern.compile(
            "!\\s*important");

    static boolean isSpace(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
    }

    private void advance(Matcher m) {
        _buffer.position(_buffer.position() + m.end());
    }

    boolean consumeRGB() {
        Matcher m = PATTERN_RGB.matcher(_buffer);
        if (!m.lookingAt()) {
            return false;
        }

        float r = Float.parseFloat(m.group(1));
        if ("%".equals(m.group(2))) r *= 2.55f;
        if (r >= 256) r = 255;

        float g = Float.parseFloat(m.group(3));
        if ("%".equals(m.group(4))) g *= 2.55f;
        if (g >= 256) g = 255;

        float b = Float.parseFloat(m.group(5));
        if ("%".equals(m.group(6))) b *= 2.55f;
        if (b >= 256) b = 255;

        _tokenColor = (((int) r) << 16) | (((int) g) << 8) | ((int) b);
        advance(m);
        return true;
    }

    boolean consumeURL() {
        try {

            Matcher m = PATTERN_URL.matcher(_buffer);
            if (!m.lookingAt()) {
                return false;
            }

            String str = m.group(1);

            if (str.length() > 0 && (str.charAt(0) == '\'' || str.charAt(0) == '\"')) {
                _tokenString = unescape(str, 1, str.length() - 1);
            } else {
                _tokenString = unescape(str, 0, str.length());
            }

            advance(m);
            return true;

        } catch (StackOverflowError error) {
//      logger.error("UI-6008 CSSStyleParser stackoverflow error. String to parser is:'" + _buffer + "'", error);
            return false;
        }
    }

    boolean consumeHexColor() {
        Matcher m = PATTERN_HEXCOLOR.matcher(_buffer);
        if (!m.lookingAt()) {
            return false;
        }

        int len = m.end(1);
        int rgb = Integer.parseInt(m.group(1), 16);

        if (len == 7) {
            _tokenColor = rgb;
            advance(m);
            return true;
        }

        if (len == 4) {
            int r = (rgb >> 8) & 0xf;
            int g = (rgb >> 4) & 0xf;
            int b = rgb & 0xf;
            _tokenColor = r * 0x110000 + g * 0x1100 + b * 0x11;
            advance(m);
            return true;
        }

        return false;
    }

    static int fromHex(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        } else if ('a' <= ch && ch <= 'f') {
            return ch - ('a' - 10);
        } else if ('A' <= ch && ch <= 'F') {
            return ch - ('A' - 10);
        } else {
            return -1;
        }
    }

    private String unescape(String str, int startIndex, int endIndex) {
        StringBuffer buf = new StringBuffer(endIndex - startIndex);

        for (int i = startIndex; i < endIndex; ++i) {
            char ch = str.charAt(i);
            if (ch != '\\') {
                buf.append(ch);
            } else {
                if (i + 1 < endIndex) {
                    ch = str.charAt(++i);
                    int value = 0;

                    if ((value = fromHex(ch)) == -1) {
                        buf.append(ch);
                    } else {
                        int lim = Math.min(i + 4, endIndex);
                        int hex;

                        while (i + 1 < lim && (hex = fromHex(str.charAt(i + 1))) != -1) {
                            value = value * 16 + hex;
                            ++i;
                        }

                        if (i + 1 < endIndex && str.charAt(i + 1) == ' ') {
                            ++i;
                        }

                        buf.append((char) value);
                    }
                }
            }
        }

        return buf.toString();
    }

    boolean consumeString() {
        Matcher m = PATTERN_STRING.matcher(_buffer);
        if (!m.lookingAt()) {
            return false;
        }

        String str = m.group();
        _tokenString = unescape(str, 1, str.length() - 1);
        advance(m);
        return true;
    }

    boolean consumeIdent() {
        Matcher m = PATTERN_IDENT.matcher(_buffer);
        if (!m.lookingAt()) {
            return false;
        }

        _tokenString = m.group();
        advance(m);
        return true;
    }

    boolean consumeNumber() {
        Matcher m = PATTERN_NUMBER.matcher(_buffer);
        if (!m.lookingAt()) {
            return false;
        }

        _tokenNumber = Float.parseFloat(m.group(1));
        _tokenUnits = m.group(2);
        advance(m);
        return true;
    }

    boolean consumeImportant() {
        Matcher m = PATTERN_IMPORTANT.matcher(_buffer);
        if (!m.lookingAt()) {
            return false;
        }

        advance(m);
        return true;
    }

    int nextToken() throws ParseException {
        for (; ; ) {
            if (_buffer.remaining() == 0) {
                return EOF;
            }
            if (!isSpace(_buffer.charAt(0))) {
                break;
            }
            _buffer.get();
        }

        Matcher m;
        int tokenStart = _buffer.position();
        switch (_buffer.charAt(0)) {
            case ':':
                _buffer.get();
                return COLON;
            case '-':
                if (consumeNumber()) {
                    return NUMBER;
                }
                _buffer.get();
                return MINUS;
            case '+':
                _buffer.get();
                return PLUS;
            case '/':
                _buffer.get();
                return SLASH;
            case ',':
                _buffer.get();
                return COMMA;
            case ';':
                _buffer.get();
                return SEMICOLON;
            case '!':
                if (consumeImportant()) {
                    return IMPORTANT;
                }
                throw new ParseException(
                        "unexpected character sequence", _buffer.position());
            case '#':
                if (consumeHexColor()) {
                    return COLOR;
                }
                throw new ParseException(
                        "invalid #RGB color", _buffer.position());

            case 'r':
            case 'R':
                if (consumeRGB()) {
                    return COLOR;
                }
                break;
            case 'u':
            case 'U':
                if (consumeURL()) {
                    return URL;
                }
                break;
            case '\"':
            case '\'':
                if (consumeString()) {
                    return STRING;
                }
                throw new ParseException(
                        "invalid quoted string", _buffer.position());
        }

        if (consumeIdent()) {
            return IDENT;
        }

        if (consumeNumber()) {
            return NUMBER;
        }

        throw new ParseException(
                "unrecognized character sequence", _buffer.position());
    }

} // CSSStyleParser
