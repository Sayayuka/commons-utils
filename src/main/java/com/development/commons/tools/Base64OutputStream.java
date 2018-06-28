/*
 * $Id$
 *
 * Copyright (c) 2004 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>
 * Base64OutputStream - a filtered OutputStream that encodes its output in Base64. Base64 encodes binary data in ASCII
 * by converting 3 bytes into 4 ASCII characters. Each ASCII character represents 6-bits of data. (2^6 = 64, hence the
 * name Base64)
 * </p>
 *
 * <p>
 * The default alphabet, stream-terminator, and line-size conform to the Base64 standard.
 * </p>
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
// CHECKSTYLE.OFF: MagicNumber - Fixing these does not make the code more readable
public class Base64OutputStream extends FilterOutputStream {
  /**
   * The default Base64 alphabet. The byte at index <i>i</i> corresponds to the ASCII character to output for that code.
   */
  static final byte[] DEFAULT_ALPHABET = StringUtils
      .toBytes("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");

  /**
   * The default terminating character used to finish partial Base64 quads.
   */
  static final byte DEFAULT_TERMINAL = '=';

  /**
   * The default number of characters per line.
   */
  static final int DEFAULT_LINESIZE = 76;

  /**
   * The active alphabet. Change to use an alternate alphabet in the Base64 encoding.
   */
  private final byte[] _alphabet;

  /**
   * The terminal character to use.
   */
  private final byte _terminal;

  /**
   * The line size
   */
  private final int _lineSize;

  /**
   * The buffer used to accumulate Base64 quads before writing them to the underlying OutputStream.
   */
  private final byte[] _buffer;

  /**
   * The accumulator used for storing incomplete Base64 quads. Used in conjunction with _byteNo.
   */
  private int _accum;

  /**
   * The current offset in _buffer
   */
  private int _bufferOffset;

  /**
   * The number of bytes currently stored in _accum. This number will always be either 0, 1, or 2.
   */
  private int _byteNo;

  /**
   * The current column number. Used for line termination.
   */
  private int _colNo;

  /**
   * Creates a new Base64OutputStream that will write through to the specified OutputStream. The default Base64
   * alphabet, terminator, and line-size will be used.
   *
   * @param out
   *          - the OutputStream to which the Base64 encoded data is sent.
   */
  public Base64OutputStream(final OutputStream out) {
    super(out);
    _alphabet = DEFAULT_ALPHABET;
    _terminal = DEFAULT_TERMINAL;
    _lineSize = DEFAULT_LINESIZE;
    _colNo = 0;
    _buffer = new byte[1024];
  }

  /**
   * Buffers and writes a single byte.
   *
   * @param b
   *          - the byte to write
   * @throws IOException
   *           - thrown by the underlying OutputStream on a buffer flush.
   */
  @Override
  public void write(final int b) throws IOException {
    switch (_byteNo) {
    case 0:
      _accum = (b & 0xff) << 16;
      _byteNo = 1;
      break;
    case 1:
      _accum |= (b & 0xff) << 8;
      _byteNo = 2;
      break;
    case 2:
      _accum |= (b & 0xff);
      _byteNo = 0;

      _buffer[_bufferOffset + 0] = _alphabet[(_accum >>> 18) & 0x3f];
      _buffer[_bufferOffset + 1] = _alphabet[(_accum >>> 12) & 0x3f];
      _buffer[_bufferOffset + 2] = _alphabet[(_accum >>> 6) & 0x3f];
      _buffer[_bufferOffset + 3] = _alphabet[(_accum >>> 0) & 0x3f];

      _bufferOffset += 4;
      _colNo += 4;

      if (_colNo >= _lineSize) {
        _buffer[_bufferOffset++] = '\n';
        _colNo = 0;
      }

      if (_bufferOffset + 5 > _buffer.length) {
        out.write(_buffer, 0, _bufferOffset);
        _bufferOffset = 0;
      }

      break;
    }
  }

  /**
   * Writes an array of bytes to the underlying OutputStream.
   *
   * @param b
   *          - the array of bytes to write
   * @param off
   *          - offset into b of the first character to write
   * @param len
   *          - the number of bytes to write
   * @throws IOException
   *           - thrown by the underlying OutputStream on a buffer flush.
   */
  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    int o = off;
    int l = len;
    l += o;

    // normalize so we're doing 3 bytes at a time
    switch (_byteNo) {
    case 1:
      if (o < l) {
        write(b[o++] & 0xff);
      }
      //$FALL-THROUGH$
    case 2:
      if (o < l) {
        write(b[o++] & 0xff);
      }
    }

    int bufferOffset = _bufferOffset;
    int colNo = _colNo;
    final int lineSize = _lineSize;
    final byte[] buffer = _buffer;
    final byte[] alphabet = _alphabet;

    for (o += 2; o < l; o += 3) {
      final int accum = ((b[o - 2] & 0xff) << 16) | ((b[o - 1] & 0xff) << 8) | ((b[o - 0] & 0xff) << 0);

      buffer[bufferOffset + 0] = alphabet[(accum >>> 18) & 0x3f];
      buffer[bufferOffset + 1] = alphabet[(accum >>> 12) & 0x3f];
      buffer[bufferOffset + 2] = alphabet[(accum >>> 6) & 0x3f];
      buffer[bufferOffset + 3] = alphabet[(accum >>> 0) & 0x3f];

      bufferOffset += 4;
      colNo += 4;

      if (colNo >= lineSize) {
        buffer[bufferOffset++] = '\n';
        colNo = 0;
      }

      if (bufferOffset + 5 > buffer.length) {
        out.write(buffer, 0, bufferOffset);
        bufferOffset = 0;
      }
    }

    _bufferOffset = bufferOffset;
    _colNo = colNo;

    for (o -= 2; o < l; ++o) {
      write(b[o] & 0xff);
    }
  }

  /**
   * Terminates the Base64 stream. This method is automatically called by out.close(), but is provided here in case the
   * user does not wish to close the underlying OutputStream at the same time.
   *
   * @throws IOException
   *           - thrown by the underlying OutputStream
   */
  public void terminate() throws IOException {
    switch (_byteNo) {
    case 1:
      _buffer[_bufferOffset + 0] = _alphabet[(_accum >>> 18) & 0x3f];
      _buffer[_bufferOffset + 1] = _alphabet[(_accum >>> 12) & 0x3f];
      _buffer[_bufferOffset + 2] = _terminal;
      _buffer[_bufferOffset + 3] = _terminal;
      break;
    case 2:
      _buffer[_bufferOffset + 0] = _alphabet[(_accum >>> 18) & 0x3f];
      _buffer[_bufferOffset + 1] = _alphabet[(_accum >>> 12) & 0x3f];
      _buffer[_bufferOffset + 2] = _alphabet[(_accum >>> 6) & 0x3f];
      _buffer[_bufferOffset + 3] = _terminal;
      break;
    default:
      return;
    }

    _byteNo = 0;
    _bufferOffset += 4;
    _colNo += 4;

    if (_colNo >= _lineSize) {
      _buffer[_bufferOffset++] = '\n';
      _colNo = 0;
    }

    if (_bufferOffset + 5 > _buffer.length) {
      out.write(_buffer, 0, _bufferOffset);
      _bufferOffset = 0;
    }
  }

  /**
   * Flushes the currently stored up buffer. The terminating bytes are not written, use "terminate()" for that.
   *
   * @throws IOException
   *           - thrown by underlying OutputStream
   */
  @Override
  public void flush() throws IOException {
    if (_bufferOffset > 0) {
      out.write(_buffer, 0, _bufferOffset);
      _bufferOffset = 0;
    }

    out.flush();
  }

  /**
   * Terminates the Base64 stream by writing out the contents of the accumulator, flushes the buffer, then closes the
   * underlying OutputStream.
   *
   * @throws IOException
   *           - thrown by underlying OutputStream
   */
  @Override
  public void close() throws IOException {
    terminate();
    flush();
    out.close();
  }

} // Base64OutputStream
