/*
 * $Id$
 */
package com.development.commons.tools.xi.util;

/**
 * Basic visitor interface to visit objects of type T.
 *
 * @author ddiodati
 *
 */
public interface SimpleVisitor<T> {

  /**
   * Checks if we are done visiting.
   *
   * @return true if we should stop.
   */
  boolean isDone();

  /**
   * Visit a select item.
   *
   * @param item
   *          The current select Item.
   */
  void visit(T item);

}
