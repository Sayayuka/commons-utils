/*
 * $Id$
 *
 * Copyright (C) 2010 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools;

/**
 * BASE64Encoder -- minimal drop in replacement for sun.misc.BASE64Encoder. It is meant to allow us to get off the
 * proprietary sun version since the JDK6 compiler complains about it. The signature of this class allows us to update
 * only the import statement for BASE64Encoder to use this version.
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
// CHECKSTYLE.OFF: MagicNumber - Fixing these does not make the code more readable
public class BASE64Encoder {
  private static final int CHARS_PER_ATOM = 4;
  private static final int BYTES_PER_ATOM = 3;
  private static final int BYTES_PER_LINE = 57;

  private static final char[] ALPHABET = ("ABCDEFGHIJKLMNOP" //
      + "QRSTUVWXYZabcdef" //
      + "ghijklmnopqrstuv" //
      + "wxyz0123456789+/" //
  ).toCharArray();

  /**
   * Encodes a byte array to a base-64 string.
   *
   * @param input
   *          the array to encode
   * @return the base-64 encoded form of input
   */
  public String encode(final byte[] input) {
    final int length = input.length;

    // pre-allocate exactly enough space in the output buffer
    final StringBuilder out = new StringBuilder((BYTES_PER_ATOM - 1 + length) / BYTES_PER_ATOM * CHARS_PER_ATOM);

    int offset = 0;

    while (offset + BYTES_PER_LINE < length) {
      for (final int max = offset + BYTES_PER_LINE; offset < max; offset += BYTES_PER_ATOM) {
        encodeAtom(out, input, offset);
      }
      out.append('\n');
    }

    for (; offset + BYTES_PER_ATOM <= length; offset += BYTES_PER_ATOM) {
      encodeAtom(out, input, offset);
    }

    if (offset < length) {
      encodeAtom(out, input, offset, length - offset);
    }

    return out.toString();
  }

  private void encodeAtom(final StringBuilder out, final byte[] data, final int offset) {
    final int v = (((data[offset] & 0xff) << 16) | ((data[offset + 1] & 0xff) << 8) | ((data[offset + 2] & 0xff)));

    out.append(ALPHABET[(v >>> 18) & 0x3f]).append(ALPHABET[(v >>> 12) & 0x3f]).append(ALPHABET[(v >>> 6) & 0x3f])
        .append(ALPHABET[v & 0x3f]);
  }

  private void encodeAtom(final StringBuilder out, final byte[] data, final int offset, final int len) {
    byte a, b;

    switch (len) {
    case 0:
      break;

    case 1:
      a = data[offset];
      out.append(ALPHABET[(a >>> 2) & 0x3f]).append(ALPHABET[(a << 4) & 0x30]).append('=').append('=');
      break;

    case 2:
      a = data[offset];
      b = data[offset + 1];
      out.append(ALPHABET[(a >>> 2) & 0x3f]).append(ALPHABET[((a << 4) & 0x30) + ((b >>> 4) & 0xf)])
          .append(ALPHABET[((b << 2) & 0x3c)]).append('=');
      break;

    case BYTES_PER_ATOM:
      encodeAtom(out, data, offset);
      break;

    default:
      throw new AssertionError("Invalid length: " + len);
    }
  }

} // BASE64Encoder
