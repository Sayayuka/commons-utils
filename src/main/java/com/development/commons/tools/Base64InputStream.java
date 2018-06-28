/*
 * $Id$
 *
 * Copyright (c) 2004 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base64InputStream -- decodes a Base64 encoded InputStream.
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
// CHECKSTYLE.OFF: MagicNumber - Fixing these does not make the code more readable
public class Base64InputStream extends FilterInputStream {
  /**
   * Inversion of Base64OutputStream's default alphabet.
   */
  static final short[] DEFAULT_INVERSE_ALPHABET = invertAlphabet(Base64OutputStream.DEFAULT_ALPHABET,
      Base64OutputStream.DEFAULT_TERMINAL);

  /**
   * The same as Base64OutputStream.DEFAULT_TERMINAL. ('=')
   */
  static final byte DEFAULT_TERMINAL = Base64OutputStream.DEFAULT_TERMINAL;

  /**
   * Inverts a Base64 alphabet for use by the decoder
   *
   * @param alphabet
   *          - the Base64 alphabet to invert
   * @param terminal
   *          - the terminal character
   * @return the inverted alphabet
   * @throws IllegalArgumentException
   *           - if the alphabet is bad.
   */
  static final short[] invertAlphabet(final byte[] alphabet, final byte terminal) {
    if (alphabet.length < 64) {
      throw new IllegalArgumentException("Bag alphabet, must be at least 64 bytes (was " + alphabet.length + ")");
    }

    final short[] map = new short[256];

    for (int i = 0; i < 256; ++i) {
      map[i] = -1;
    }

    for (int i = 0; i < 64; ++i) {
      final int index = alphabet[i] & 0xff;
      if (map[index] != -1) {
        throw new IllegalArgumentException("Bad alphabet, '" + ((char) index) + "' is present more than once");
      }
      map[index] = (short) i;
    }

    map[terminal & 0xff] = 0;

    return map;
  }

  /**
   * The inverse alphabet used by this instance
   */
  private final short[] _inverseAlphabet;

  /**
   * The terminal character used by this instance
   */
  private final byte _terminal;

  /**
   * Buffered input from underlying InputStream
   */
  private final byte[] _buffer;

  /**
   * Current offset into _buffer;
   */
  private int _offset;

  /**
   * Number of bytes in _buffer;
   */
  private int _length;

  /**
   * The accumulator used to handle partial quad reads. Used in combination with _byteNo.
   */
  private int _accum;

  /**
   * The current "offset" into _accum. Either 0, 1, or 2 -- determines which byte to pull out of _accum.
   */
  private int _byteNo;

  /**
   * Creates a Base64InputStream wrapped around the specified InputStream, using the default base-64 alphabet. The
   * wrapped stream must be encoded in base-64 using the same alphabet or the read operations will fail with an
   * Base64EncodingException.
   *
   * @param in
   *          - an InputStream encoded in base-64
   */
  public Base64InputStream(final InputStream in) {
    super(in);
    _inverseAlphabet = DEFAULT_INVERSE_ALPHABET;
    _terminal = DEFAULT_TERMINAL;
    _buffer = new byte[1024];
  }

  /**
   * Returns an approximation of how many bytes can be read without blocking.
   *
   * @return the number of bytes that may be safely read without blocking.
   * @throws IOException
   *           - thrown by the underyling InputStream
   */
  @Override
  public int available() throws IOException {
    // this calculation isn't actually accurate, the buffer and
    // underlying InputStream may contain newlines which would make
    // the return value larger than appropriate.

    // division by 5 to support the minimum of 4 bytes per line.

    return (_length - _offset + in.available()) / 5 * 3;
  }

  /**
   * Returns false -- mark is not supported.
   *
   * @return false;
   */
  @Override
  public boolean markSupported() {
    return false;
  }

  /**
   * Throws UnsupportedOperationException -- mark is not supported.
   *
   * @param readLimit
   *          readLimit
   * @throws UnsupportedOperationException
   */
  @Override
  public synchronized void mark(final int readLimit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws IOException -- mark/reset is not supported.
   *
   * @throws IOException
   *           -- always
   */
  @Override
  public synchronized void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  /**
   * Fills the buffer such that there are at least 4 bytes available. This method does not give up until an end of
   * stream or an IOException
   *
   * @return true if 4 bytes were read, false if less.
   */
  private boolean fillBuffer() throws IOException {
    if (_offset > 0 && _length != _offset) {
      // move any remaning bytes to the beginning of the buffer
      System.arraycopy(_buffer, _offset, _buffer, 0, _length - _offset);
    }

    _length -= _offset;
    _offset = 0;

    do {
      final int n = in.read(_buffer, _length, _buffer.length - _length);
      if (n == -1) {
        return false;
      }
      _length += n;
    } while (_length < 4);

    return true;
  }

  /**
   * Reads a single byte from the stream. This method is much slower than the byte array version.
   *
   * @return the byte read or -1 at end of stream
   * @throws IOException
   *           - thrown by underyling stream
   * @throws Base64EncodingException
   *           - if the stream was corrupt or invalid
   */
  @Override
  public int read() throws IOException, Base64EncodingException {
    if (_byteNo == 0) {
      int c1;

      do {
        if (_offset + 4 > _length) {
          if (!fillBuffer()) {
            return -1;
          }
        }

        c1 = _buffer[_offset++];
      } while (c1 == '\n' || c1 == '\r');

      final int c2 = _buffer[_offset + 0];
      final int c3 = _buffer[_offset + 1];
      final int c4 = _buffer[_offset + 2];

      _offset += 3;
      _accum = (_inverseAlphabet[c1] << 18) | (_inverseAlphabet[c2] << 12) | (_inverseAlphabet[c3] << 6)
          | (_inverseAlphabet[c4]);

      if (_accum < 0) {
        throw new IOException("stream corrupted, invalid base-64 sequence");
      }
    }

    switch (_byteNo) {
    case 0:
      _byteNo = 1;
      return (_accum >>> 16) & 0xff;
    case 1:
      if (_buffer[_offset - 2] == _terminal) {
        return -1;
      }
      _byteNo = 2;
      return (_accum >>> 8) & 0xff;
    case 2:
    default:
      if (_buffer[_offset - 1] == _terminal) {
        return -1;
      }
      _byteNo = 0;
      return (_accum >>> 0) & 0xff;
    }
  }

  /**
   * Reads into byte array.
   *
   * @param b
   *          - the byte array
   * @param off
   *          - the offset at which to start reading
   * @param len
   *          - the maximum number of bytes to read
   * @throws IOException
   *           - thrown by the underlying InputStream
   * @return number of bytes read
   * @throws Base64EncodingException
   *           - if the stream was corrupt or invalid
   */
  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException, Base64EncodingException {
    int n = 0;
    int c1;

    // empty out any partial-reads.
    while (_byteNo > 0 && n < len) {
      c1 = read();
      if (c1 == -1) {
        return n > 0 ? n : -1;
      }
      b[off + n++] = (byte) c1;
    }

    final short[] inverseAlphabet = _inverseAlphabet;
    final byte[] buffer = _buffer;
    int offset = _offset;

    // start the block-reads.
    while (n + 3 < len) {
      do {
        if (offset + 4 > _length) {
          _offset = offset;
          if (!fillBuffer()) {
            return n > 0 ? n : -1;
          }
          offset = 0;
        }

        c1 = buffer[offset++];
      } while (c1 == '\n' || c1 == '\r');

      final int c2 = buffer[offset + 0];
      final int c3 = buffer[offset + 1];
      final int c4 = buffer[offset + 2];

      offset += 3;
      final int accum = (inverseAlphabet[c1] << 18) | (inverseAlphabet[c2] << 12) | (inverseAlphabet[c3] << 6)
          | (inverseAlphabet[c4]);

      if (accum < 0) {
        throw new IOException("stream corrupted, invalid base-64 sequence");
      }

      b[off + n++] = (byte) (accum >>> 16);

      if (c3 != _terminal) {
        b[off + n++] = (byte) (accum >>> 8);
      } else {
        _accum = accum;
        _offset = offset;
        _byteNo = 1;
        return n;
      }

      if (c4 != _terminal) {
        b[off + n++] = (byte) (accum >>> 0);
      } else {
        _accum = accum;
        _offset = offset;
        _byteNo = 2;
        return n;
      }
    }

    _offset = offset;

    if (n == 0) {
      // really small buffer sent in, fill it the slow way...
      while (n < len) {
        c1 = read();
        if (c1 == -1) {
          return n > 0 ? n : -1;
        }

        b[off + n++] = (byte) c1;
      }
    }

    return n;
  }

  /**
   * Not implemented.
   *
   * @param n
   *          n
   * @return skip
   * @throws IOException
   *           if it happens
   */
  @Override
  public long skip(final long n) throws IOException {
    throw new UnsupportedOperationException(getClass().getName() + ".skip() not implemented due to laziness");
  }

} // Base64InputStream
