/*
 * $Id$
 */
package com.development.commons.tools.util;

/**
 * Utility Class for converting byte array to hex (2 compliment)
 *
 * @author adunn Success Factors
 */
public class HexUtils {

    private HexUtils() {
    }

    /**
     * Convenience method to convert a byte array to a hex string.
     *
     * @param data
     *            the byte[] to convert
     * @return String the converted byte[]
     */
    public static String bytesToHex(final byte[] data) {
        final StringBuffer buf = new StringBuffer();
        for (final byte element : data) {
            buf.append(byteToHex(element).toUpperCase());
        }
        return (buf.toString());
    }

    /**
     * method to convert a byte to a hex string.
     *
     * @param data
     *            the byte to convert
     * @return String the converted byte
     */
    public static String byteToHex(final byte data) {
        final StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Convenience method to convert an int to a hex char.
     *
     * @param i
     *            the int to convert
     * @return char the converted char
     */
    public static char toHexChar(final int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    /**
     * Parse a hex string and convert to byte array
     *
     * @param hexStr
     *            - hex string to be parsed
     * @return byte array
     * @throws NumberFormatException
     *             if the string cannot be parsed into bytes
     */
    public static byte[] parseSeq(final String hexStr) throws NumberFormatException {
        if (hexStr == null || hexStr.equals("")) {
            return null;
        }
        final int len = hexStr.length();

        if (len % 2 != 0) {
            throw new NumberFormatException("Illegal length of string in hexadecimal notation.");
        }
        final int numOfOctets = len / 2;
        final byte[] seq = new byte[numOfOctets];

        for (int i = 0; i < numOfOctets; i++) {
            final String hex = hexStr.substring(i * 2, i * 2 + 2);

            seq[i] = parseByte(hex);
        }
        return seq;
    }

    /**
     * Parse a binary hex and convert to a byte
     *
     * @param hex
     *            - binary hex
     * @return byte representation of the hex
     * @throws NumberFormatException
     *             if the string cannot be parsed into a byte
     */
    public static byte parseByte(final String hex) throws NumberFormatException {
        if (hex == null) {
            throw new IllegalArgumentException("Null string in hexadecimal notation.");
        }
        if (hex.equals("")) {
            return 0;
        }
        final Integer num = Integer.decode("0x" + hex);
        final int n = num.intValue();

        if (n > 255 || n < 0) {
            throw new NumberFormatException("Out of range for byte.");
        }
        return num.byteValue();
    }

}
