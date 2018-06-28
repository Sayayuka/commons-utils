/*
 * $Id$
 *
 * Copyright (C) 2005 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools.util.date;

import java.util.HashMap;

/**
 * HashMapThreadLocal
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public class HashMapThreadLocal extends ThreadLocal {
  private final int _initialSize;

  public HashMapThreadLocal(final int initialSize) {
    _initialSize = initialSize;
  }

  @Override
  protected Object initialValue() {
    return new HashMap(_initialSize);
  }
} // HashMapThreadLocal
