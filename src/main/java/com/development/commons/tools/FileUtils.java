/*
 * $Id$
 */
package com.development.commons.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.FileChannel;

//import org.mozilla.universalchardet.UniversalDetector;

/**
 * This class provides some File manimpulation methods
 *
 */
public class FileUtils {

    /**
     * SYSTEM
     */
    public static final int SYSTEM = 1;

    /**
     * FILE
     */
    public static final int FILE = 2;

    /**
     * Length of a UTF-8 byte order marker (BOM)
     */
    public static final int UTF_8_BOM_LENGTH = 3;

    /**
     * The first byte of a UTF-8 BOM.
     */
    public static final int UTF_8_BOM_BYTE_0 = 0x000000EF;

    /**
     * The second byte of a UTF-8 BOM.
     */
    public static final int UTF_8_BOM_BYTE_1 = 0x000000BB;

    /**
     * The third byte of a UTF-8 BOM.
     */
    public static final int UTF_8_BOM_BYTE_2 = 0x000000BF;

    /**
     * Length of a UTF-8 byte order marker (BOM)
     */
    public static final int UTF_16_BOM_LENGTH = 2;

    /**
     * The first byte of a UTF-16 BOM (in big-endian encoding).
     */
    public static final int UTF_16_BOM_BYTE_0 = 0x000000FE;

    /**
     * The second byte of a UTF-16 BOM (in big-endian encoding).
     */
    public static final int UTF_16_BOM_BYTE_1 = 0x000000FE;

    private FileUtils() {
        super();
    }

    /**
     * Takes a file name and reads it into an InputStream for reading.
     *
     * @param fileName
     *            File name
     * @param location
     *            Location of the file: SYSTEM or FILE
     *
     * @return InputStream object
     *
     * @throws IOException
     *             if it happens
     */
    public static InputStream toInputStream(final String fileName, final int location) throws IOException {
        InputStream is = null;

        try {
            if (SYSTEM == location) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            } else {
                is = new FileInputStream(fileName);
            }
        } catch (final FileNotFoundException e) {
//            logger.warn("Can't find file <" + fileName + "> in the system using location " + location);
            throw e;
        }

        // the getResourceAsStream returns null if it can't find the resource
        if (is == null) {
//            logger.warn("Can't find file <" + fileName + "> in the system using location " + location);
            throw new FileNotFoundException("Cound not find file " + fileName);
        }

        return is;
    }

    /**
     * Takes a file name and reads it into an InputStream for reading.
     *
     * @param fileName
     *            File name
     *
     * @return InputStream object
     *
     * @throws IOException
     *             if it happens
     */
    public static InputStream toInputStream(final String fileName) throws IOException {
        InputStream is = null;

        try {
            is = toInputStream(fileName, FILE);
        } catch (final FileNotFoundException e) {
//            logger.warn("Can't find file <" + fileName + "> with location " + FILE + ".  Try " + SYSTEM + "...");
        }

        if (null == is) {
            is = toInputStream(fileName, SYSTEM);
        }

        return is;
    }

    /**
     * Takes a file name and reads it into a byte[].
     *
     * @param fileName
     *            File name
     *
     * @return a byte[] object
     *
     * @throws IOException
     *             if it happens
     */
    public static byte[] toBytes(final String fileName) throws IOException {
        InputStream is = null;
        try {

            is = toInputStream(fileName);
            return StringUtils.readFully(is);

        } catch (final IOException e) {
//            logger.warn("Can't find file <" + fileName + "> in the system.");
            throw e;
        } finally {
            if (is != null) {
                close(is);
            }
        }
    }

    /**
     * Takes a file name and reads it into a byte[].
     *
     * @param fileName
     *            File name
     * @param location
     *            Location of the file: SYSTEM or FILE
     *
     * @return a byte[] object
     *
     * @throws IOException
     *             if it happens
     */
    public static byte[] toBytes(final String fileName, final int location) throws IOException {
        InputStream is = null;
        try {

            is = toInputStream(fileName, location);
            return StringUtils.readFully(is);

        } catch (final IOException e) {
//            logger.warn("Can't find file <" + fileName + "> in the system.");
            throw e;
        } finally {
            if (is != null) {
                close(is);
            }
        }
    }

    /**
     * This method closes an InputStream object.
     *
     * @param is
     *            InputStream object to close
     */
    public static void close(final InputStream is) {
        if (null == is) {
            return;
        }

        try {
            is.close();
        } catch (final IOException e) {
            throw new RuntimeException("InputStream close() failed: " + is, e);
        }
    }

    /**
     * This method closes an InputStream object.
     *
     * @param reader
     *            object to close
     */
    public static void close(final Reader reader) {
        if (null == reader) {
            return;
        }

        try {
            reader.close();
        } catch (final IOException e) {
            throw new RuntimeException("Reader close() failed: " + reader, e);
        }
    }

    /**
     * This method closes an OutputStream object.
     *
     * @param os
     *            InputStream object to close
     */
    public static void close(final OutputStream os) {
        if (null == os) {
            return;
        }

        try {
            os.close();
        } catch (final IOException e) {
            throw new RuntimeException("OutputStream close() failed: " + os, e);
        }
    }

    /**
     * This method checks if "data" is XML.
     *
     * @param data
     *            byte[] to be examined
     *
     * @return true if the data is XML; false otherwise
     */
    public static boolean isXML(final byte data[]) {
        final int index = findFirstByte(data);
        final int indexOfQuestion = 1;
        final int indexOfx = 2;
        final int indexOfm = 3;
        final int indexOfl = 4;
        if (index >= 0 && data.length > (index + indexOfl)) {
            return (data[index] == '<' && data[index + indexOfQuestion] == '?' && data[index + indexOfx] == 'x' && data[index + indexOfm] == 'm' && data[index + indexOfl] == 'l');
        }
        return false;
    }

    /**
     * This method checks if the given "data" has any Byte-Order Mark.
     *
     * @param data
     *            byte[] to be examined
     *
     * @return true if the data contains Byte-Order Mark; false otherwise
     */
    public static boolean hasByteOrderMark(final byte[] data) {
        final boolean result = getEncodingFromByteOrderMark(data) != null;
        return result;
    }

    /**
     * This method tries to find the encoding from the XML encoding declaration. It returns null if it can't find the encoding.
     *
     * @param data
     *            byte[] to be examined
     *
     * @return the encoding detected; null if none found
     */
    public static String getEncodingFromXML(final byte[] data) {
        String encoding = null;
        if (!isXML(data)) {
            return encoding;
        }
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final InputStreamReader isr = new InputStreamReader(bais, StringUtils.DEFAULT_ENCODING);
            final BufferedReader reader = new BufferedReader(isr);
            final String line = reader.readLine();
            if (line != null) {
                final String ENCODING = "encoding=";
                final int index = line.indexOf(ENCODING);
                if (index > 0) {
                    // Found the encoding.
                    final int closingQuote = line.indexOf("\"", index + ENCODING.length() + 1);
                    if (closingQuote > index) {
                        encoding = line.substring(index + ENCODING.length() + 1, closingQuote);
                    }
                }
            }
        } catch (final UnsupportedEncodingException e) {
//            logger.error("UnsupportedEncodingException caught.", e);
        } catch (final IOException e) {
//            logger.error("IOException caught.", e);
        }

        return encoding;
    }

    /**
     * This method tries to find the encoding from the Byte-Order Mark. It returns null if it can't find the encoding or there is no Byte-Order Mark.
     *
     * @param data
     *            byte[] to be examined
     *
     * @return the encoding detected using the Byte-Order Mark; null if none found
     * @deprecated use getEncodingFromFile instead (note. This method has problem detecting UTF-16BE).
     */
    @Deprecated
    public static String getEncodingFromByteOrderMark(final byte[] data) {
        if (data != null && data.length > UTF_8_BOM_LENGTH) {
            if ((data[0] & UTF_8_BOM_BYTE_0) == UTF_8_BOM_BYTE_0 && (data[1] & UTF_8_BOM_BYTE_1) == UTF_8_BOM_BYTE_1 && (data[2] & UTF_8_BOM_BYTE_2) == UTF_8_BOM_BYTE_2) {
                return "UTF-8";
            }
            if ((data[0] & UTF_16_BOM_BYTE_0) == UTF_16_BOM_BYTE_0 && (data[1] & UTF_16_BOM_BYTE_1) == UTF_16_BOM_BYTE_1 && data[2] != 0) {
                return "UTF-16"; // big-endian or UTF-16-BE
            }
            if ((data[0] & UTF_16_BOM_BYTE_1) == UTF_16_BOM_BYTE_1 && (data[1] & UTF_16_BOM_BYTE_0) == UTF_16_BOM_BYTE_0 && data[2] != 0) {
                return "UTF-16"; // little-endian or UTF-16-LE
            }
        }

        // We don't support UCS-4 encoding so we ignore it.

        return null; // No recognizable Byte-Order Mark. DO NOT ATTEMPT TO RETURN A DEFAUTL ENCODING!!!
    }

    private static final int DETECTOR_TEST_LIMIT = 3; // limit the buffer test to only 3 rounds ( or up to 3 x 4096 bytes
                                                      // )
    private static final int DETECTOR_BUF_LEN = 4096;

    /**
     * Derived this code from juniversalchardet.jar library. Using juniversalchardet method to decide the encoding of a given file
     *
     * @param data
     *            data
     * @return encoding
     */
    /*public static String getEncodingFromFile(final byte[] data) {
        final UniversalDetector detector = new UniversalDetector(null);

        int len = data.length;
        int offset = 0;
        int bufsize = DETECTOR_BUF_LEN;
        int test = 0;
        while (len > 0 && test < DETECTOR_TEST_LIMIT) {
            if (len < DETECTOR_BUF_LEN) {
                bufsize = len;
            }

            // The detector engine works
            detector.handleData(data, offset, bufsize);

            if (detector.isDone()) {
                break;
            }
            len -= bufsize;
            offset += DETECTOR_BUF_LEN;
            test++;
        }

        detector.dataEnd();

        final String encoding = detector.getDetectedCharset();
        detector.reset();

        return encoding;
    }*/

    /**
     * This method skips all Byte-Order Marks and returns the starting index to the first data byte in the given byte[].
     *
     * @param data
     *            byte[] to be examined
     *
     * @return index to the first valid data byte; -1 if no valid data byte found
     */
    public static int findFirstByte(final byte[] data) {
        if (data != null && data.length > UTF_8_BOM_LENGTH) {
            if ((data[0] & UTF_8_BOM_BYTE_0) == UTF_8_BOM_BYTE_0 && (data[1] & UTF_8_BOM_BYTE_1) == UTF_8_BOM_BYTE_1 && (data[2] & UTF_8_BOM_BYTE_2) == UTF_8_BOM_BYTE_2) {
                return UTF_8_BOM_LENGTH;
            } else if ((data[0] & UTF_16_BOM_BYTE_0) == UTF_16_BOM_BYTE_0 && (data[1] & UTF_16_BOM_BYTE_1) == UTF_16_BOM_BYTE_1) {
                return UTF_16_BOM_LENGTH;
            } else if ((data[0] & UTF_16_BOM_BYTE_1) == UTF_16_BOM_BYTE_1 && (data[1] & UTF_16_BOM_BYTE_0) == UTF_16_BOM_BYTE_0) {
                return UTF_16_BOM_LENGTH;
            }
        }
        return 0;
    }

    /**
     * This method removes any Byte-Order Mark in the given "data".
     *
     * @param data
     *            byte[] to be examined
     *
     * @return data without any Byte-Order Mark
     */
    public static byte[] removeByteOrderMark(final byte[] data) {
        if (data == null || data.length <= 0) {
            return data;
        }

        final int index = findFirstByte(data);

        final byte[] newData = new byte[data.length - index];
        // TODO: Need a more efficient way of copying the array.
        System.arraycopy(data, index, newData, 0, data.length - index);

        return newData;
    }

    /**
     * Copies all bytes from an InputStream to an OutputStream
     *
     * @param src
     *            src
     * @param dst
     *            dst
     * @throws IOException
     *             if it happens
     */
    public static void copy(final InputStream src, final OutputStream dst) throws IOException {
        final int BUF_SIZE = 16384;
        final byte[] buf = new byte[BUF_SIZE];
        int n;
        while ((n = src.read(buf)) != -1) {
            dst.write(buf, 0, n);
        }
    }

    /**
     * Copies all bytes from an InputStream to a File. The file will be created if it doesn't exist and will be overwritten if it does.
     *
     * @param src
     *            src
     * @param dst
     *            dst
     * @throws IOException
     *             if it happens
     */
    public static void copy(final InputStream src, final File dst) throws IOException {
        final FileOutputStream out = new FileOutputStream(dst);
        try {
            copy(src, out);
        } finally {
            out.close();
        }
    }

    /**
     * Copies all bytes from a URL to a File.
     *
     * @param src
     *            src
     * @param dst
     *            dst
     * @throws IOException
     *             if it happens
     */
    public static void copy(final URL src, final File dst) throws IOException {
        final InputStream in = src.openStream();
        try {
            copy(in, dst);
        } finally {
            in.close();
        }
    }

    /**
     * File to file copy using the very efficient nio APIs.
     *
     * @param src
     *            src
     * @param dst
     *            dst
     * @throws IOException
     *             if it happens
     */
    public static void copy(final File src, final File dst) throws IOException {
        final FileInputStream in = new FileInputStream(src);
        try {
            final FileOutputStream out = new FileOutputStream(dst);
            try {
                final FileChannel ic = in.getChannel();
                final FileChannel oc = out.getChannel();
                final long length = src.length();
                long offset = 0;
                while (offset < length) {
                    final long n = ic.transferTo(offset, length - offset, oc);
                    offset += n;
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * replace all characters windows doesn't accept to '_'
     *
     * @param fileName
     *            fileName
     * @return normalized file name
     */
    public static String normalizeFileName(final String fileName) {
        final String temp = fileName.replaceAll("[\\/:,;*?^%\"{}<>|\\s]", "_");

        return temp;
    }

}
