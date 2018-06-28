package com.development.commons.tools;

/*
 * This license applies to the replace and isEmpty methods: Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * This class provides some string manimpulation methods
 *
 * $Id$ Created by IntelliJ IDEA. User: klo Date: Jul 15, 2003 Time: 3:41:26 PM
 *
 * @version $Revision$ $Date$
 */
// CHECKSTYLE.OFF: MagicNumber - Fixing these does not make the code more readable
public class StringUtils {

    private static Set<String> trueBoolValues = new HashSet<String>();
    private static Set<String> falseBoolValues = new HashSet<String>();

    static {
        trueBoolValues.add("true");
        trueBoolValues.add("yes");
        trueBoolValues.add("y");
        trueBoolValues.add("t");
        trueBoolValues.add("on");
        trueBoolValues.add("1");

        falseBoolValues.add("false");
        falseBoolValues.add("no");
        falseBoolValues.add("n");
        falseBoolValues.add("f");
        falseBoolValues.add("off");
        falseBoolValues.add("0");
    }

    private StringUtils() {
        super();
    }

    /**
     * System property to configure max size of to string operation buffer.
     */
    public static String MAX_SYSTEM_TOSTRING_SIZE_PROP = "sf.sfv4.StringUtils.max.buffer";

    /**
     * A constant for an empty String.
     */
    public static final String EMPTY_STRING = "";

    /**
     * default Max string buffer size for toString method set to 256,000 chars which is about 512KB.
     */
    public static final int MAX_TOSTRING_DEFAULT = 256000;

    /**
     * Some character that needs to include when parsing legal document
     */
    public static final int SPACE = ' ';

    /**
     * A quote character.
     */
    public static final int QUOTE = '\'';

    /**
     * A hypen character.
     */
    public static final int HYPHEN = '-';

    /**
     * Converts a string to a boolean value.
     *
     * @param str
     *            The string representation of a boolean
     * @return true if str has a value of true, yes, t, or y (case insensitive match). false if str is null, blank, or any other value.
     */
    public static boolean getBoolean(final String str) {
        return getBoolean(str, false);
    }

    /**
     * Converts a string to a boolean value.
     *
     * @param s
     *            The string representation of a boolean
     * @param defaultBool
     *            - The default boolean value if the string is null, blank, or not one of the true values.
     * @return true if str has a value of true, yes, t, y, or 1 (case insensitive match). false if str has a value of false, no, f, n, or 0 (case insensitive match). defaultBool if
     *         str is null, empty, or any other value.
     */
    public static boolean getBoolean(final String s, final boolean defaultBool) {
        String str = s;
        if (!isBlank(str)) {
            str = str.toLowerCase().trim();
            if (trueBoolValues.contains(str)) {
                return true;
            } else if (falseBoolValues.contains(str)) {
                return false;
            }
        }

        return defaultBool;
    }

    /**
     * Converts a string to a long value.
     *
     * @param str
     *            The string representation of a long
     * @param defaultValue
     *            - The default long value if the string is null, blank, or not valid long strings.
     * @return the converted long value or the default value.
     */
    public static long getLong(final String str, final long defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Converts a string to a double value.
     *
     * @param str
     *            The string representation of a double
     * @param defaultValue
     *            - The default double value if the string is null, blank, or not valid double strings.
     * @return the converted double value or the default value.
     */
    public static double getDouble(final String str, final double defaultValue) {
        return NumberUtils.toDouble(str, defaultValue);
    }

    /**
     * Converts a string to a integer value.
     *
     * @param str
     *            The string representation of a integer
     * @param defaultValue
     *            - The default long value if the string is null, blank, or not valid integer strings.
     * @return the converted integer value or the default value.
     */
    public static int getInteger(final String str, final int defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Create a string array from a string separated by delim
     *
     * @deprecated The String.split function should be used instead.
     * @param line
     *            the line to split
     * @param delim
     *            the delimter to split by
     * @return a string array of the split fields
     */
    @Deprecated
    public static String[] split(final String line, final String delim) {
        final List<String> list = new ArrayList<String>();
        final StringTokenizer t = new StringTokenizer(line, delim);
        while (t.hasMoreTokens()) {
            list.add(t.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Joins an array of strings together.
     *
     * @param strs
     *            The array of separate strings.
     * @param separator
     *            The string to be placed between each string.
     * @return A new joined string.
     */
    public static String join(final String[] strs, final String separator) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]);
            if (i + 1 < strs.length) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * Indent with two space
     *
     * @param sb
     *            sb
     * @param level
     *            level
     */
    public static void indent(final StringBuffer sb, final int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }

    /**
     * Not null
     *
     * @param str
     *            the string to test
     * @return str if str != null, or "" if str == null.
     */
    public static String notNull(final String str) {
        return str == null ? "" : str;
    }

    /**
     * Returns a string representation of the object if it is not null, or "" if object is null.
     *
     * @param obj
     *            the object to test
     * @return a string representation of the object if it is not null, or "" if object is null.
     */
    public static String notNull(final Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * The value of.
     *
     * @param o
     *            the object to test
     * @return o.toString if o != null, or "" if o == null.
     */
    public static String valueOf(final Object o) {
        return o == null ? null : o.toString();
    }

    /**
     * The length.
     *
     * @param str
     *            the string
     * @return the length
     */
    public static int length(final String str) {
        return notNull(str).length();
    }

    /**
     * <p>
     * Replaces all occurrences of a String within another String.
     * </p>
     *
     * <p>
     * A <code>null</code> reference passed to this method is a no-op.
     * </p>
     *
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @see #replace(String text, String repl, String with, int max)
     * @param text
     *            text to search and replace in, may be null
     * @param repl
     *            the String to search for, may be null
     * @param with
     *            the String to replace with, may be null
     * @return the text with any replacements processed, <code>null</code> if null String input
     */
    public static String replace(final String text, final String repl, final String with) {
        return replace(text, repl, with, -1);
    }

    /**
     * <p>
     * Replaces a String with another String inside a larger String, for the first <code>max</code> values of the search String.
     * </p>
     *
     * <p>
     * A <code>null</code> reference passed to this method is a no-op.
     * </p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text
     *            text to search and replace in, may be null
     * @param repl
     *            the String to search for, may be null
     * @param with
     *            the String to replace with, may be null
     * @param max
     *            maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed, <code>null</code> if null String input
     */
    public static String replace(final String text, final String repl, final String with, final int max) {
        int m = max;
        if (text == null || isEmpty(repl) || with == null || m == 0) {
            return text;
        }

        // Implementation is based on the Learning Management System's StringUtils#replaceString(String, String, String)
        // method (see "http://pe-svn.plateau.internal/dev/tms/b1311/trunk/common/java/plateaucommon2/plateaucommon-core/"
        // + "src/main/java/com/plateau/common/core/api/tools/StringUtil.java")

        final int searchStrLength = repl.length();

        StringBuilder outBuffer = null;

        int lastIdx = 0;
        int nextIdx;

        while ((nextIdx = text.indexOf(repl, lastIdx)) != -1) {
            if (outBuffer == null) {
                outBuffer = new StringBuilder(text.length() + with.length());
            }
            outBuffer.append(text.substring(lastIdx, nextIdx));
            outBuffer.append(with);
            lastIdx = nextIdx + searchStrLength;
            if (--m == 0) {
                break;
            }
        }

        if (outBuffer == null) {
            return text;
        }

        outBuffer.append(text.substring(lastIdx));

        return outBuffer.toString();
    }

    // Replace, character based
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Replaces all occurrences of a character in a String with another. This is a null-safe version of {@link String#replace(char, char)}.
     * </p>
     *
     * <p>
     * A <code>null</code> string input returns <code>null</code>. An empty ("") string input returns an empty string.
     * </p>
     *
     * <pre>
     * StringUtils.replaceChars(null, *, *)        = null
     * StringUtils.replaceChars("", *, *)          = ""
     * StringUtils.replaceChars("abcba", 'b', 'y') = "aycya"
     * StringUtils.replaceChars("abcba", 'z', 'y') = "abcba"
     * </pre>
     *
     * @param str
     *            String to replace characters in, may be null
     * @param searchChar
     *            the character to search for, may be null
     * @param replaceChar
     *            the character to replace, may be null
     * @return modified String, <code>null</code> if null string input
     * @since 2.0
     */
    public static String replaceChars(final String str, final char searchChar, final char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    /**
     * To hex string.
     *
     * @param str
     *            str
     * @return hex
     */
    public static String toHexString(final String str) {
        if (str == null) {
            return str;
        }

        final StringBuilder sb = new StringBuilder();
        final int len = str.length();

        for (int i = 0; i < len; i++) {
            final char c = str.charAt(i);
            sb.append(Character.forDigit((c >> 12) & 0xf, 16));
            sb.append(Character.forDigit((c >> 8) & 0xf, 16));
            sb.append(Character.forDigit((c >> 4) & 0xf, 16));
            sb.append(Character.forDigit((c >> 0) & 0xf, 16));
        }
        return sb.toString();
    }

    /**
     * <p>
     * Replaces multiple characters in a String in one go. This method can also be used to delete characters.
     * </p>
     *
     * <p>
     * For example:<br />
     * <code>replaceChars(&quot;hello&quot;, &quot;ho&quot;, &quot;jy&quot;) = jelly</code>.
     * </p>
     *
     * <p>
     * A <code>null</code> string input returns <code>null</code>. An empty ("") string input returns an empty string. A null or empty set of search characters returns the input
     * string.
     * </p>
     *
     * <p>
     * The length of the search characters should normally equal the length of the replace characters. If the search characters is longer, then the extra search characters are
     * deleted. If the search characters is shorter, then the extra replace characters are ignored.
     * </p>
     *
     * <pre>
     * StringUtils.replaceChars(null, *, *)           = null
     * StringUtils.replaceChars("", *, *)             = ""
     * StringUtils.replaceChars("abc", null, *)       = "abc"
     * StringUtils.replaceChars("abc", "", *)         = "abc"
     * StringUtils.replaceChars("abc", "b", null)     = "ac"
     * StringUtils.replaceChars("abc", "b", "")       = "ac"
     * StringUtils.replaceChars("abcba", "bc", "yz")  = "ayzya"
     * StringUtils.replaceChars("abcba", "bc", "y")   = "ayya"
     * StringUtils.replaceChars("abcba", "bc", "yzx") = "ayzya"
     * </pre>
     *
     * @param str
     *            String to replace characters in, may be null
     * @param searchChars
     *            a set of characters to search for, may be null
     * @param replaceChars
     *            a set of characters to replace, may be null
     * @return modified String, <code>null</code> if null string input
     * @since 2.0
     */
    public static String replaceChars(final String str, final String searchChars, final String replaceChars) {
        String replChars = replaceChars;
        if (isEmpty(str) || isEmpty(searchChars)) {
            return str;
        }
        if (replChars == null) {
            replChars = "";
        }
        boolean modified = false;
        final StringBuilder buf = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            final int index = searchChars.indexOf(ch);
            if (index >= 0) {
                modified = true;
                if (index < replChars.length()) {
                    buf.append(replChars.charAt(index));
                }
            } else {
                buf.append(ch);
            }
        }
        if (modified) {
            return buf.toString();
        }
        return str;
    }

    /**
     * Convenience method. The InputStream and OutputStream are treated as encoded in the DEFAULT_ENCODING.
     *
     * @param in
     *            in
     * @param out
     *            out
     * @param strings
     *            strings
     * @param replacements
     *            replacements
     * @throws IOException
     *             if it happens
     */
    public static void replaceStrings(final InputStream in, final OutputStream out, final String[] strings, final String[] replacements) throws IOException {
        final OutputStreamWriter osr = new OutputStreamWriter(out, DEFAULT_ENCODING);
        replaceStrings(new InputStreamReader(in, DEFAULT_ENCODING), osr, strings, replacements);
        osr.flush();
    }

    /**
     * Convenience method for the replaceStrings methods that takes java.tools.regex.Pattern arguments.
     *
     * @param in
     *            in
     * @param out
     *            out
     * @param strings
     *            strings
     * @param replacements
     *            replacements
     * @throws IOException
     *             if it happens
     */
    public static void replaceStrings(final Reader in, final Writer out, final String[] strings, final String[] replacements) throws IOException {
        int maxLength = 0;
        final Pattern[] patterns = new Pattern[strings.length];

        for (int i = 0; i < strings.length; ++i) {
            if (strings[i].length() > maxLength) {
                maxLength = strings[i].length();
            }

            patterns[i] = Pattern.compile(strings[i]);
        }

        replaceStrings(in, out, patterns, replacements, maxLength);
    }

    /**
     * Copies the data from a Reader to a Writer, replacing character sequences along the way.
     *
     * @param in
     *            where to read from
     * @param out
     *            where to write to
     * @param patterns
     *            the patterns to replace
     * @param replacements
     *            the replacement strings
     * @param maxLength
     *            the maximum length of a string that can match the pattern.
     * @throws IOException
     *             from the Reader or Writer
     */
    public static void replaceStrings(final Reader in, final Writer out, final Pattern[] patterns, final String[] replacements, final int maxLength) throws IOException {
        final int BUFSIZE = 1024;

        assert patterns.length > 0;
        assert patterns.length == replacements.length;

        final int nReplacements = patterns.length;
        final CharBuffer buf = CharBuffer.allocate(BUFSIZE);
        final Matcher[] matchers = new Matcher[nReplacements];
        final int matchIndex[] = new int[nReplacements];

        for (int i = 0; i < nReplacements; ++i) {
            matchers[i] = patterns[i].matcher(buf);
        }

        assert maxLength < BUFSIZE;

        final char[] array = buf.array();
        int n;
        while ((n = in.read(array, buf.position(), buf.remaining())) != -1) {

            // advance the buffer by the number of characters read
            buf.position(buf.position() + n);

            // flip the buffer to start reading from it
            buf.flip();

            int nextIndex = Integer.MAX_VALUE;
            int replaceNo = -1;

            // initialize the matchIndex array for the new input
            for (int i = 0; i < nReplacements; ++i) {
                if (!matchers[i].find(0)) {
                    matchIndex[i] = -1;
                } else {
                    matchIndex[i] = matchers[i].start();
                    if (matchers[i].start() < nextIndex) {
                        nextIndex = matchIndex[i];
                        replaceNo = i;
                    }
                }
            }

            while (replaceNo != -1) {
                final int positionShift = nextIndex + matchers[replaceNo].end() - matchers[replaceNo].start();

                // write everything up to the match
                out.write(array, buf.position(), nextIndex);

                // write the replacement for the match
                out.write(replacements[replaceNo]);

                // advance the buffer past the matching input
                // aka buf.position += matchStart + matchLength
                buf.position(buf.position() + positionShift);

                // look for the next match
                nextIndex = Integer.MAX_VALUE;
                replaceNo = -1;
                for (int i = 0; i < nReplacements; ++i) {

                    // this matcher never found one in the first place, skip it.
                    if (matchIndex[i] < 0) {
                        continue;
                    }

                    // advance the match index to account for the shift in
                    // buffer position.
                    matchIndex[i] -= positionShift;

                    // we've advanced past this match, find the next matching
                    // index (if there is one)
                    if (matchIndex[i] < 0) {
                        if (!matchers[i].find(0)) {
                            matchIndex[i] = -1;
                            continue; // nope.
                        }
                        matchIndex[i] = matchers[i].start();
                    }

                    if (matchIndex[i] < nextIndex) {
                        nextIndex = matchIndex[i];
                        replaceNo = i;
                    }
                }
            }

            // output as much of the remaining buffer as we can. keep
            // around at least maxLength characters because the start of the
            // next match may be in there.

            final int bytesRemaining = buf.remaining() - maxLength;
            if (bytesRemaining > 0) {
                out.write(array, buf.position(), bytesRemaining);
                buf.position(buf.position() + bytesRemaining);
            }

            // compact the buffer for the next read
            buf.compact();

        } // while (!EOF)

        // write out any remaining characters
        buf.flip();
        out.write(array, buf.position(), buf.remaining());
    }

    /**
     * Highlight's the matched pattern in the target String.
     *
     * @param patternStr
     *            patternStr
     * @param targetStr
     *            targetStr
     * @return formated String
     */
    public static String highlightMatch(final String patternStr, final String targetStr) {
        if (isBlank(patternStr) || isBlank(targetStr)) {
            return targetStr;
        }

        final Pattern pattern = Pattern.compile("(" + patternStr + ")", Pattern.CASE_INSENSITIVE);
        // Get the Matcher for the target string.
        final Matcher matcher = pattern.matcher(targetStr);
        final String replaceWith = "<span class=\"search_hilight\">$0</span>";
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replaceWith);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Strip
     *
     * @param s
     *            s
     * @return strip
     */
    public static String strip(final String s) {
        final String goodChars = " -abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return strip(s, goodChars);
    }

    /**
     * Strip
     *
     * @param s
     *            s
     * @param goodChars
     *            goodChars
     * @return strip
     */
    public static String strip(final String s, final String goodChars) {
        final StringBuilder result = new StringBuilder("");
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (goodChars.indexOf(c) >= 0) {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Normalize the string
     *
     * @param s
     *            the string
     * @return the normalized string
     */
    public static String normalize(final String s) {
        String result = s;
        if (result == null) {
            return "";
        }
        result = result.toLowerCase();
        result = strip(result);
        result = result.trim();
        if (result == null) {
            return "";
        }
        return result;
    }

    /**
     * Normalize the unicode word.
     *
     * @param str
     *            the unicode word
     * @param locale
     *            the locale
     * @return the normalized unicode word
     */
    public static String normalizeUnicodeWord(final String str, final Locale locale) {
        if (str == null || isBlank(str)) {
            return "";
        }

        final String s = str.toLowerCase(locale).trim();
        final StringBuilder result = new StringBuilder("");
        int pos = 0;
        final int len = s.length();
        boolean wsfound = true;
        while (pos < len) {
            final int cpoint = s.codePointAt(pos);

            if (Character.isWhitespace(cpoint)) {
                if (wsfound) {
                    pos++;
                    if (Character.isSupplementaryCodePoint(cpoint)) {
                        pos++;
                    }
                    continue;
                }
                result.appendCodePoint(SPACE);
                wsfound = true;
            } else if (Character.isLetterOrDigit(cpoint)) {
                result.appendCodePoint(cpoint);
                wsfound = false;
            } else if (isRecognizableCharacter(cpoint, locale)) {
                result.appendCodePoint(cpoint);
                wsfound = false;
            } else {
                // Non letter or digit
                if (wsfound) {
                    pos++;
                    if (Character.isSupplementaryCodePoint(cpoint)) {
                        pos++;
                    }
                    continue;
                }
                result.appendCodePoint(SPACE);
                wsfound = true;
            }

            pos++;
            if (Character.isSupplementaryCodePoint(cpoint)) {
                pos++;
            }
        }

        return result.toString();
    }

    /**
     * Determine if this is a hyphen or quote.
     *
     * @param codePoint
     *            the codePoint
     * @param locale
     *            the locale
     * @return if this is a hypen or quote
     */
    public static boolean isRecognizableCharacter(final int codePoint, final Locale locale) {
        // This should later depends on which locale
        // For now include hypen and quote for all
        final boolean result = (codePoint == HYPHEN || codePoint == QUOTE);
        return result;
    }

    /**
     * <p>
     * Checks if a String is all hex digits.
     * </p>
     *
     * @param str
     *            a non-null String to check.
     * @return <code>true</code> if the String is composed of hex digits.
     */
    public static boolean isHex(final String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.digit(str.charAt(i), 16) == -1) {
                return false;
            }
        }
        return true;
    }

    // Empty checks
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That functionality is available in isBlank().
     * </p>
     *
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(final String str) {
        return (str == null || str.length() == 0);
    }

    /**
     * Is blank
     *
     * @param str
     *            str
     * @return blank
     */
    public static boolean isBlank(final String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Determines if a character is a space or white space.
     *
     * @param character
     *            The character being evaluated.
     *
     * @return True if the character is a space or white space and false if not.
     */
    public static boolean isSpace(final char character) {

        if (character == ' ' || character == '\t' || character == '\n' || character == '\r' || character == '\f') {
            return true;
        }

        return false;
    }

    /**
     * Basically converts an object into a string, suppressing the ClassCastException. Unlike the basic conversion utility, this method does three things <li>
     * converts the object to String</li> <li>trims white spaces</li> <li>if the remaining string is length 0, then it returns null</li>
     *
     * @param obj
     *            the object to convert to string
     * @return a non-empty string, or null
     */
    public static String trim(final Object obj) {

        if (obj == null) {
            return null;
        }

        if (!(obj instanceof String)) {
            return null;
        }

        String str = (String) obj;
        str = str.trim();
        if (str.length() == 0) {
            return null;
        }

        return str;
    }

    // Trys to obtain the max buffer string size in the
    // following order.
    // 1. maxCustomLength if greater than 0.
    // 2. if not null, value from MAX_SYSTEM_TOSTRING_SIZE_PROP.
    // 3. MAX_TO_STRING_DEFAULT size.
    //
    private static int getMaxStringSize(final int maxCustomLength) {
        int maxSize = MAX_TOSTRING_DEFAULT;
        if (maxCustomLength > 0) {
            return maxCustomLength;
        }

        final String maxSizeStr = System.getProperty(MAX_SYSTEM_TOSTRING_SIZE_PROP);
        if (!StringUtils.isEmpty(maxSizeStr)) {
            maxSize = Integer.parseInt(maxSizeStr);
        }

        return maxSize;
    }

    /**
     * Reads the content of a reader into a string.
     *
     * @param in
     *            contains the return value
     * @param maxLength
     *            the maximum length allowed (to prevent OutOfMemoryException for unexpectedly large streams). If this is zero, then one of the following values are used: 1. If not
     *            null, value from system property MAX_SYSTEM_TOSTRING_SIZE_PROP 2. MAX_TO_STRING_DEFAULT.
     * @param crop
     *            crop the string at maxLength if true, throw an IOException if false.
     * @return the contents of <code>in</code> read into a string
     * @throws IOException
     *             if it happens
     */
    public static String toString(final Reader in, final int maxLength, final boolean crop) throws IOException {
        final int max = getMaxStringSize(maxLength);

        // same number of copies as a StringBuffer implementation, but
        // less overhead.
        char buf[] = new char[Math.min(1024, max)];
        int offset = 0;
        int n;
        while ((n = in.read(buf, offset, buf.length - offset)) != -1) {
            offset += n;

            if (offset == buf.length) {
                // out of space in the buffer

                if (offset == max) {

                    if (crop) {
                        return new String(buf);
                    }
                    // edge case, stream contains exactly the maximum, which is
                    // indicated by in.read() returning -1 for EOF.
                    if (in.read() == -1) {
                        return new String(buf);
                    }

                    // XXX/JGI: there could be a better exception for this
                    throw new IOException("out of space");
                }

                // double the buffer size, up to the max
                final char grow[] = new char[Math.min(max, offset * 2)];
                System.arraycopy(buf, 0, grow, 0, offset);
                buf = grow;
            }
        }
        return new String(buf, 0, offset);
    }

    /**
     * Converts a string to a byte array using our default encoding. Use this method instead of String.getBytes() as it uses the JVM's default encoding which may change if not
     * configured correctly.
     *
     * @param str
     *            the string to encode
     * @return the encoded string
     */
    public static byte[] toBytes(final String str) {
        return toBytes(str, null);
    }

    /**
     * Converts a string to a byte array using a selected encoding.
     *
     * @param str
     *            The string to encode.
     * @param encoding
     *            The encoding selected
     * @return bytes
     */
    public static byte[] toBytes(final String str, final String encoding) {
        try {
            if (encoding != null) {
                return str.getBytes(encoding);
            }
            return str.getBytes(DEFAULT_ENCODING);
        } catch (final UnsupportedEncodingException e) {
//            LogManager.getLogger().error("UnsupportedEncodingException encoding=" + encoding, e);
            throw (InternalError) new InternalError("UnsupportedEncodingException encoding=" + encoding).initCause(e);
        }
    }

    /**
     * PLEASE PLEASE PLEASE DON'T USE THIS METHOD IF YOU DON'T HAVE TO!!!
     *
     * @param out
     *            the output stream
     * @param t
     *            the throwable
     */
    public static void dumpStackTrace(final OutputStream out, final Throwable t) {
        try {
            final PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, PLATFORM_ENCODING));
            t.printStackTrace(pw);
            pw.flush();
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpectedly found that the PLATFORM_ENCODING of " + PLATFORM_ENCODING + " was not supported", e);
        }
    }

    /**
     * Replacement for <code>new String(byte[])</code> that converts bytes to a String using our default encoding.
     *
     * @param bytes
     *            an encoded String
     * @return <code>bytes</code> converted to a string
     */
    public static String fromBytes(final byte[] bytes) {
        try {
            return new String(bytes, DEFAULT_ENCODING);
        } catch (final UnsupportedEncodingException e) {
//            LogManager.getLogger().error("UnsupportedEncodingException encoding=" + DEFAULT_ENCODING, e);
            throw (InternalError) new InternalError("UnsupportedEncodingException encoding=" + DEFAULT_ENCODING).initCause(e);
        }
    }

    /**
     * A UTF-8 aware truncation routine. This method test-encodes the string parameter and returns a substring that is at most <code>maxLength</code> bytes when encoded in UTF-8.
     *
     * @param str
     *            string to test encode
     * @param maxLength
     *            the maximum number of bytes of encoded <code>str</code> before it should be truncated.
     * @return <code>str</code> truncated. If <code>str</code> can be encoded in less than <code>maxLength</code> bytes, the return value will be <code>str</code> itself.
     */
    public static String truncateToEncodedLength(final String str, final int maxLength) {
        /*
         * * xtao: This is an optimization, since the size of UTF8 encoded bytes never grows to >3x of* the original Unicode string when we convert Unicode string to UTF8 bytes.*
         * so we know we won't need to truncate when its length is <= 1/3 maximum
         */
        if (str.length() * 3 <= maxLength) {
            return str;
        }

        final CharsetEncoder encoder = DEFAULT_CHARSET.newEncoder();
        final ByteBuffer out = ByteBuffer.allocate(maxLength);
        final CharBuffer in = CharBuffer.wrap(str);

        final CoderResult result = encoder.encode(in, out, true);

        if (result.isUnderflow()) {
            return str;
        }

        return str.substring(0, in.position());
    }

    /**
     * Read full.
     *
     * @param in
     *            in
     * @return fully
     * @throws IOException
     *             if it happens
     */
    public static byte[] readFully(final InputStream in) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    /**
     * The default encoding used in the VM. This should be the encoding used in XML files. e.g.
     *
     * <pre>
     * "&lt?xml version=\"1.0\" encoding=\"+StringUtils.DEFAULT_ENCODING\"?&gt;
     * </pre>
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * A java.nio.charset.Charset for the DEFAULT_ENCODING.
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    /**
     * This is the platform's default encoding. Equivalent to "file.encoding". This encoding should be used when wrapping System.out in a OutputStreamWriter, or anything
     * equivalent.
     */
    public static final String PLATFORM_ENCODING = System.getProperty("file.encoding");

    /**
     * Unfortunately the only encoding Microsoft seems to support for CSV files is "ISO-8859-1". That's what this constant is defined to be.
     */
    public static final String CSV_ENCODING = "ISO-8859-1";

    /**
     * This is for reading in a script file.
     */
    public static final String SCRIPT_ENCODING = "ISO-8859-1";

    // Please do not delete the following commented out block until the
    // "UTF-8" default is stable. -JI 2005.01.27
    //
    // we no longer accept encoding configuration from the command line
    //
    // // static initializer to set DEFAULT_ENCODING to the "file.encoding"
    // // system property.
    // static {
    // String encoding = System.getProperty("file.encoding");

    // if (encoding == null || encoding.endsWith("8859-1") || encoding.endsWith("8859_1")) {
    // encoding = "ISO-8859-1";
    // } else if ("UTF8".equals(encoding) || "UTF-8".equals(encoding)) {
    // encoding = "UTF-8";
    // } else if ("US-ASCII".equals(encoding) || "ASCII".equals(encoding)) {
    // encoding = "US-ASCII";
    // } else if ("Cp1252".equalsIgnoreCase(encoding) || "windows-1252".equalsIgnoreCase(encoding)) {
    // encoding = "windows-1252"; // XML recognized name
    // } else {
    // // bad encoding. don't let the server run...
    // throw new InternalError("FATAL ERROR: Unrecognized default encoding: "+encoding);
    // }

    // DEFAULT_ENCODING = encoding;
    // DEFAULT_CHARSET = Charset.forName(encoding);

    // // add a work-around for existing blobs encoded with ISO8859-1
    // org.apache.xerces.tools.EncodingMap.putIANA2JavaMapping("ISO8859-1", "ISO-8859-1");
    // }

    /**
     * check if the string is a unsigned numeric or not
     *
     * @param str
     *            a string that might be numeric
     * @return <code>boolean</code> true if str is a number or false if not
     */
    public static boolean isNumeric(final String str) {
        if (str == null) {
            return false;
        }
        final int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is double?
     *
     * @param value
     *            value
     * @return double
     */
    public static boolean isDouble(final String value) {

        if (isBlank(value)) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get list from string.
     *
     * @param s
     *            s
     * @param delim
     *            delim
     * @return list
     */
    public static List<String> getListFromString(final String s, final String delim) {
        List<String> result = null;
        if (s == null) {
            return null;
        }
        result = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(s, delim);
        while (st.hasMoreElements()) {
            result.add(((String) st.nextElement()).trim());
        }
        return result;
    }

    /**
     * Is valid email format?
     *
     * @param emailStr
     *            emailStr
     * @return valid
     */
    public static boolean isValidEmailFormat(final String emailStr) {
        // Set the email pattern string
        final Pattern p = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[a-z]+");
        // Match the given string with the pattern
        final Matcher m = p.matcher(emailStr);

        // check whether match is found
        return m.matches();
    }

    /**
     * Tokenize a string.
     *
     * @param input
     *            the string
     * @param parseFor
     *            the token
     * @return the tokenized string
     */
    public static List<String> tokenize(final String input, final String parseFor) {
        final List<String> strList = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(input, parseFor);
        while (st.hasMoreTokens()) {
            strList.add(st.nextToken());
        }
        return strList;
    }

    /**
     * Compares two nullable strings
     *
     * @param str1
     *            the first string
     * @param str2
     *            the second string
     * @return true if two strings are equal or both are null
     */
    public static boolean equals(final String str1, final String str2) {
        if (str1 != null) {
            return str1.equals(str2);
        }
        return str2 == null;
    }

    /**
     * Return whether all characters in the string are from the basic latin character set. (See ISO-8859-1)
     *
     * @param str
     *            The given string.
     * @return True if all characters in the string are from the basic latin character set.
     */
    public static boolean containsNonBasicLatinChars(final String str) {
        boolean result = false;
        if (str != null) {
            final int sz = str.length();
            final int FF = 0xFF;
            for (int i = 0; i < sz; i++) {
                if (str.charAt(i) > FF) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Coverts the given String into ASCII code.
     *
     * @param stream
     *            can contain both ascii and native characters (non ascii)
     * @return the stream in which native characters are replaced through ASCII code
     */
    public static String native2Ascii(final String stream) {
        if (stream == null) {
            return null;
        }
        // Max (stream.length() * 10) characters to accomodate the multiples of \\\\uxxxx
        // Assumed all the characters are non ascii
        final StringBuilder buffer = new StringBuilder(stream.length() * 10);
        final int tilde = 0x7E;
        final int stringLength = 4;
        for (int i = 0; i < stream.length(); i++) {
            final char ch = stream.charAt(i);
            if (ch <= tilde) { // ~ tilde as the last ascii character
                buffer.append(ch);
            } else { // non ascii characters
                buffer.append("\\\\u");
                final String hex = Integer.toHexString(ch);
                for (int j = hex.length(); j < stringLength; j++) {
                    buffer.append('0');
                }
                buffer.append(hex);
            }
        }
        return buffer.toString();
    }

    /**
     * SEC-2013 Fix HTTP Response Splitting issue Truncate everything include and after char \n
     *
     * @param url
     *            the URL
     *
     * @return String url with everything after(include) \n removed
     */
    public static String removeCRLFFromString(final String url) {
        String redirectUrl = url;
        final int index = redirectUrl.indexOf("\n");
        if (index > -1) {
            redirectUrl = redirectUrl.substring(0, index);
        }
        return redirectUrl;
    }

    /**
     * translate unicode to string
     *
     * @param unicode
     *            String
     * @return String
     */
    public static String unicode2String(final String unicode) {
        final StringBuffer string = new StringBuffer();
        final String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            final int data = Integer.parseInt(hex[i], 16);
            string.append((char) data);
        }
        return string.toString();
    }
}
