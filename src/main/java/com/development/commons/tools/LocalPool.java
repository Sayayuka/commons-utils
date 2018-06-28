/*
 * $Id$
 *
 * Copyright (C) 2005 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools;

import java.util.Arrays;

/**
 * LocalPool -- a collection of methods that allocate and return data cached in ThreadLocal objects. As these buffers
 * can be reused by different request (but never simultaneously), it is important that usage of these never expose data
 * from one request to another.
 *
 * <p>
 * As a general rule we don't do any array growing strategies here, since in the presense of thread pooling that could
 * produce very large and unchanging buffers. The buffer sizes are thus fixed and if any request exceeds the fixed size,
 * a new buffer is returned that will be garbage collected.
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class LocalPool {
  /**
   * This is the size of the buffer returned by getCharBuffer. It is a potential tuning parameter. 4096 should be
   * sufficient for its current usage -- escaping HTML. In general the escaping algorithms allocate the maximum possible
   * escaped size based on the length. That means 5 * the character length. Thus the maximum length of a string that
   * will use the thread local buffer is 4096 / 5 = 819. That should be sufficient for most purposes.
   */
  static final int CHARBUFFER_SIZE = 4096;

  private LocalPool() {
    super();
  }

  private static ThreadLocal<char[]> charBuffer = new ThreadLocal<char[]>() {
    @Override
    protected char[] initialValue() {
      return new char[CHARBUFFER_SIZE];
    }
  };

  /**
   * Returns a buffer of at least <code>minSize</code> characters.
   *
   * @param minSize
   *          the minimum character array size
   * @return a char buffer of at least <code>minSize</code> characters
   */
  public static char[] getCharBuffer(final int minSize) {
    if (minSize > CHARBUFFER_SIZE) {
      return new char[minSize];
    }
    return charBuffer.get();
  }

  /**
   * This method can be used to test if the return value of getCharBuffer was from the thread local or not. Note: this
   * method may return false positives if passed buffers not returned by getCharBuffer.
   *
   * @param buf
   *          a character buffer returned by getCharBuffer
   * @return true if buf is a shared thread local buffer, false if it is not and will be garbage collected.
   */
  public static boolean isLocalBuffer(final char[] buf) {
    return buf.length == CHARBUFFER_SIZE;
  }

  /**
   * Zeros out a buffer before releasing it back to the ThreadLocal.
   *
   * @param buf
   *          the buffer to zero
   */
  public static void zeroBuffer(final char[] buf) {
    if (!LocalPool.isLocalBuffer(buf)) {
      // the buffer was too big to use the shared local buffer, so it
      // was allocated using new. there is no need to zero it.
      return;
    }

    Arrays.fill(buf, 0, buf.length, '\0');

  }

} // LocalPool
