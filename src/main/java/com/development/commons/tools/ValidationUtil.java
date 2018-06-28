package com.development.commons.tools;

import java.io.UnsupportedEncodingException;

/**
 * This class is used to validate length of field submitted from http request.
 *
 * @author cyu
 */
public class ValidationUtil {
  /**
   * private constructor
   */
  private ValidationUtil() {
    super();
  }

  /**
   * To validate the input field submitted from request. if the encodingType is null , we will use the
   * StringUtils.DEFAULT_ENCODING;
   *
   * @param source
   *          sourcefield
   * @param length
   *          expected_source_length
   * @param encodingType
   *          validate_encoding_type
   * @return whether the length of source is valid
   */
  public static boolean validateLengthOfField(final String source, final int length, final String encodingType) {
    String tmpEncodingType = encodingType;
    // if source is null this is a valid source text.
    if (StringUtils.isBlank(source)) {
      return true;
    }
    // if passed in length is <= 0 then the valid should always return
    // false.
    if (length <= 0) {
      return false;
    }
    // if encodingType is empty then use default encoding.
    if (StringUtils.isBlank(encodingType)) {
      tmpEncodingType = StringUtils.DEFAULT_ENCODING;
    }
    try {
      return source.getBytes(tmpEncodingType).length <= length;
    } catch (final UnsupportedEncodingException e) {
      return false;
    }
  }

  /**
   * To validate the input field submitted from request using string default encoding type.
   *
   * @param source
   *          sourcefield
   * @param length
   *          expected_source_length
   * @return whether the length of source is valid
   */
  public static boolean validateLengthOfField(final String source, final int length) {
    return validateLengthOfField(source, length, StringUtils.DEFAULT_ENCODING);
  }

}
