/*
 * $Id$
 *
 * Copyright (c) 2004 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools;

import java.io.IOException;

/**
 * Base64EncodingException
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class Base64EncodingException extends IOException {

  private static final long serialVersionUID = -1249218909540203704L;

  /**
   * Constructor.
   *
   * @param msg
   *          msg
   */
  public Base64EncodingException(final String msg) {
    super(msg);
  }
} // Base64EncodingException
