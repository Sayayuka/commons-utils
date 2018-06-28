/*
 * $Id$
 *
 * Copyright (C) 2010 SuccessFactors, Inc. All Rights Reserved
 */
package com.development.commons.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * ConnectionWrapper for JDK 1.6 and JDK 1.7.
 *
 * @author Jeffrey Ichnowski
 * @version $Revision$
 */
public abstract class ConnectionWrapper implements Connection {

  /**
   * A proxy to wrap returned statements with.
   */
  public interface StatementProxyFactory {
    /**
     * Return a wrapped instance of the statement.
     *
     * @param statement
     *          statement
     * @return Statement
     */
    Statement newInstance(Statement statement);

    /**
     * Return a wrapped instance of the statement.
     *
     * @param statement
     *          statement
     * @param string
     *          string
     * @return Statement
     */
    Statement newInstance(Statement statement, String string);
  }

  /**
   * A default no-op factory.
   */
  public static class NoopStatementProxyFactory implements StatementProxyFactory {
    @Override
    public Statement newInstance(final Statement statement) {
      return statement;
    }

    @Override
    public Statement newInstance(final Statement statement, final String string) {
      return statement;
    }
  }

  /**
   * A proxy to wrap returned statements with.
   */
  static StatementProxyFactory statementProxyFactory = new NoopStatementProxyFactory();

  /**
   * Set the proxy to wrap returned statements with.
   *
   * @param factory
   *          the factory
   */
  public static void setStatementProxyFactory(final StatementProxyFactory factory) {
    statementProxyFactory = factory;
  }

  /**
   * The wrapped connection.
   */
  protected Connection connection;

  /**
   * Creates a ConnectionWrapper around the passed in argument.
   *
   * @param wrapped
   *          the connection to wrap.
   */
  protected ConnectionWrapper(final Connection wrapped) {
    this.connection = wrapped;
  }

  /** {@inheritDoc} */
  @Override
  public void setReadOnly(final boolean flag) throws SQLException {
    connection.setReadOnly(flag);
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws SQLException {
    connection.close();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isReadOnly() throws SQLException {
    return connection.isReadOnly();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isClosed() throws SQLException {
    return connection.isClosed();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isValid(final int n) throws SQLException {
    return connection.isValid(n);
  }

  /** {@inheritDoc} */
  @Override
  public Statement createStatement() throws SQLException {
    return statementProxyFactory.newInstance(connection.createStatement());
  }

  /** {@inheritDoc} */
  @Override
  public Statement createStatement(final int n, final int n1) throws SQLException {
    return statementProxyFactory.newInstance(connection.createStatement(n, n1));
  }

  /** {@inheritDoc} */
  @Override
  public Statement createStatement(final int n, final int n1, final int n2) throws SQLException {
    return statementProxyFactory.newInstance(connection.createStatement(n, n1, n2));
  }

  /** {@inheritDoc} */
  @Override
  public PreparedStatement prepareStatement(final String string, final int n, final int n1) throws SQLException {
    return (PreparedStatement) statementProxyFactory.newInstance(connection.prepareStatement(string, n, n1), string);
  }

  /** {@inheritDoc} */
  @Override
  public PreparedStatement prepareStatement(final String string) throws SQLException {
    return (PreparedStatement) statementProxyFactory.newInstance(connection.prepareStatement(string), string);
  }

  /** {@inheritDoc} */
  @Override
  public PreparedStatement prepareStatement(final String string, final int n, final int n1, final int n2) throws SQLException {
    return (PreparedStatement) statementProxyFactory.newInstance(connection.prepareStatement(string, n, n1, n2), string);
  }

  /** {@inheritDoc} */
  @Override
  public PreparedStatement prepareStatement(final String string, final int n) throws SQLException {
    return (PreparedStatement) statementProxyFactory.newInstance(connection.prepareStatement(string, n), string);
  }

  /** {@inheritDoc} */
  @Override
  public PreparedStatement prepareStatement(final String string, final int[] intArray) throws SQLException {
    return (PreparedStatement) statementProxyFactory.newInstance(connection.prepareStatement(string, intArray), string);
  }

  /** {@inheritDoc} */
  @Override
  public PreparedStatement prepareStatement(final String string, final String[] stringArray) throws SQLException {
    return (PreparedStatement) statementProxyFactory.newInstance(connection.prepareStatement(string, stringArray), string);
  }

  /** {@inheritDoc} */
  @Override
  public CallableStatement prepareCall(final String string) throws SQLException {
    return (CallableStatement) statementProxyFactory.newInstance(connection.prepareCall(string), string);
  }

  /** {@inheritDoc} */
  @Override
  public CallableStatement prepareCall(final String string, final int n, final int n1, final int n2) throws SQLException {
    return (CallableStatement) statementProxyFactory.newInstance(connection.prepareCall(string, n, n1, n2), string);
  }

  /** {@inheritDoc} */
  @Override
  public CallableStatement prepareCall(final String string, final int n, final int n1) throws SQLException {
    return (CallableStatement) statementProxyFactory.newInstance(connection.prepareCall(string, n, n1), string);
  }

  /** {@inheritDoc} */
  @Override
  public String nativeSQL(final String string) throws SQLException {
    return connection.nativeSQL(string);
  }

  /** {@inheritDoc} */
  @Override
  public void setAutoCommit(final boolean flag) throws SQLException {
    connection.setAutoCommit(flag);
  }

  /** {@inheritDoc} */
  @Override
  public boolean getAutoCommit() throws SQLException {
    return connection.getAutoCommit();
  }

  /** {@inheritDoc} */
  @Override
  public void commit() throws SQLException {
    connection.commit();
  }

  /** {@inheritDoc} */
  @Override
  public void rollback(final Savepoint savepoint) throws SQLException {
    connection.rollback(savepoint);
  }

  /** {@inheritDoc} */
  @Override
  public void rollback() throws SQLException {
    connection.rollback();
  }

  /** {@inheritDoc} */
  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return connection.getMetaData();
  }

  /** {@inheritDoc} */
  @Override
  public void setCatalog(final String string) throws SQLException {
    connection.setCatalog(string);
  }

  /** {@inheritDoc} */
  @Override
  public String getCatalog() throws SQLException {
    return connection.getCatalog();
  }

  /** {@inheritDoc} */
  @Override
  public void setTransactionIsolation(final int n) throws SQLException {
    connection.setTransactionIsolation(n);
  }

  /** {@inheritDoc} */
  @Override
  public int getTransactionIsolation() throws SQLException {
    return connection.getTransactionIsolation();
  }

  /** {@inheritDoc} */
  @Override
  public SQLWarning getWarnings() throws SQLException {
    return connection.getWarnings();
  }

  /** {@inheritDoc} */
  @Override
  public void clearWarnings() throws SQLException {
    connection.clearWarnings();
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return connection.getTypeMap();
  }

  /** {@inheritDoc} */
  @Override
  public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
    connection.setTypeMap(map);
  }

  /** {@inheritDoc} */
  @Override
  public void setHoldability(final int n) throws SQLException {
    connection.setHoldability(n);
  }

  /** {@inheritDoc} */
  @Override
  public int getHoldability() throws SQLException {
    return connection.getHoldability();
  }

  /** {@inheritDoc} */
  @Override
  public Savepoint setSavepoint() throws SQLException {
    return connection.setSavepoint();
  }

  /** {@inheritDoc} */
  @Override
  public Savepoint setSavepoint(final String string) throws SQLException {
    return connection.setSavepoint(string);
  }

  /** {@inheritDoc} */
  @Override
  public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
    connection.releaseSavepoint(savepoint);
  }

  /** {@inheritDoc} */
  @Override
  public Clob createClob() throws SQLException {
    return connection.createClob();
  }

  /** {@inheritDoc} */
  @Override
  public Blob createBlob() throws SQLException {
    return connection.createBlob();
  }

  /** {@inheritDoc} */
  @Override
  public NClob createNClob() throws SQLException {
    return connection.createNClob();
  }

  /** {@inheritDoc} */
  @Override
  public SQLXML createSQLXML() throws SQLException {
    return connection.createSQLXML();
  }

  /** {@inheritDoc} */
  @Override
  public void setClientInfo(final String string, final String string1) throws SQLClientInfoException {
    connection.setClientInfo(string, string1);
  }

  /** {@inheritDoc} */
  @Override
  public void setClientInfo(final Properties properties) throws SQLClientInfoException {
    connection.setClientInfo(properties);
  }

  /** {@inheritDoc} */
  @Override
  public String getClientInfo(final String string) throws SQLException {
    return connection.getClientInfo(string);
  }

  /** {@inheritDoc} */
  @Override
  public Properties getClientInfo() throws SQLException {
    return connection.getClientInfo();
  }

  /** {@inheritDoc} */
  @Override
  public Array createArrayOf(final String string, final Object[] objectArray) throws SQLException {
    return connection.createArrayOf(string, objectArray);
  }

  /** {@inheritDoc} */
  @Override
  public Struct createStruct(final String string, final Object[] objectArray) throws SQLException {
    return connection.createStruct(string, objectArray);
  }

  /** {@inheritDoc} */
  @Override
  public <T> T unwrap(final Class<T> clazz) throws SQLException {
    return connection.unwrap(clazz);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isWrapperFor(final Class<?> clazz) throws SQLException {
    return connection.isWrapperFor(clazz);
  }

  // Java 7/JDBC 4.1 interface methods implemented through reflection to allow
  // this to be compiled in both JDK 1.6 and JDK 1.7.
  // When we are supporting only Java 7, we can reimplement these as direct
  // pass-thru methods, and uncomment the @Override on each method.

  /** {@inheritDoc} */
  // @Override
  public void setSchema(final String schema) throws SQLException {
    try {
      final Class<? extends Connection> c = connection.getClass();
      final Method m = c.getMethod("setSchema", String.class);
      m.invoke(connection, schema);
    } catch (final NoSuchMethodException e) {
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    } catch (final SecurityException e) {
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    } catch (final IllegalArgumentException e) {
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    } catch (final IllegalAccessException e) {
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    } catch (final InvocationTargetException e) {
      final Throwable target = e.getTargetException();
      if (target instanceof SQLException) {
        throw (SQLException) target;
      } else if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    }
  }

  /** {@inheritDoc} */
  // @Override
  public String getSchema() throws SQLException {
    final String result;
    try {
      final Class<? extends Connection> c = connection.getClass();
      final Method method = c.getMethod("getSchema");
      result = (String) method.invoke(connection);
    } catch (final NoSuchMethodException e) {
      throw new UnsupportedOperationException("Problem invoking method getSchema()", e);
    } catch (final SecurityException e) {
      throw new UnsupportedOperationException("Problem invoking method getSchema()", e);
    } catch (final IllegalArgumentException e) {
      throw new UnsupportedOperationException("Problem invoking method getSchema()", e);
    } catch (final IllegalAccessException e) {
      throw new UnsupportedOperationException("Problem invoking method getSchema()", e);
    } catch (final InvocationTargetException e) {
      final Throwable target = e.getTargetException();
      if (target instanceof SQLException) {
        throw (SQLException) target;
      } else if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    }
    return result;
  }

  /** {@inheritDoc} */
  // @Override
  public void abort(final Executor executor) throws SQLException {
    try {
      final Class<? extends Connection> c = connection.getClass();
      final Method m = c.getMethod("abort", Executor.class);
      m.invoke(connection, executor);
    } catch (final NoSuchMethodException e) {
      throw new UnsupportedOperationException("Problem invoking method abort(Executor)", e);
    } catch (final SecurityException e) {
      throw new UnsupportedOperationException("Problem invoking method abort(Executor)", e);
    } catch (final IllegalArgumentException e) {
      throw new UnsupportedOperationException("Problem invoking method abort(Executor)", e);
    } catch (final IllegalAccessException e) {
      throw new UnsupportedOperationException("Problem invoking method abort(Executor)", e);
    } catch (final InvocationTargetException e) {
      final Throwable target = e.getTargetException();
      if (target instanceof SQLException) {
        throw (SQLException) target;
      } else if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      throw new UnsupportedOperationException("Problem invoking method setSchema(String)", e);
    }
  }

  /** {@inheritDoc} */
  // @Override
  public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
    try {
      final Class<? extends Connection> c = connection.getClass();
      final Method m = c.getMethod("setNetworkTimeout", Executor.class, int.class);
      m.invoke(connection, executor, Integer.valueOf(milliseconds));
    } catch (final NoSuchMethodException e) {
      throw new UnsupportedOperationException("Problem invoking method setNetworkTimeout(Executor,int)", e);
    } catch (final SecurityException e) {
      throw new UnsupportedOperationException("Problem invoking method setNetworkTimeout(Executor,int)", e);
    } catch (final IllegalArgumentException e) {
      throw new UnsupportedOperationException("Problem invoking method setNetworkTimeout(Executor,int)", e);
    } catch (final IllegalAccessException e) {
      throw new UnsupportedOperationException("Problem invoking method setNetworkTimeout(Executor,int)", e);
    } catch (final InvocationTargetException e) {
      final Throwable target = e.getTargetException();
      if (target instanceof SQLException) {
        throw (SQLException) target;
      } else if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      throw new UnsupportedOperationException("Problem invoking method setNetworkTimeout(Executor,int)", e);
    }
  }

  /** {@inheritDoc} */
  // @Override
  public int getNetworkTimeout() throws SQLException {
    final int result;
    try {
      final Class<? extends Connection> c = connection.getClass();
      final Method m = c.getMethod("getNetworkTimeout");
      result = ((Integer) m.invoke(connection)).intValue();
    } catch (final NoSuchMethodException e) {
      throw new UnsupportedOperationException("Problem invoking method getNetworkTimeout()", e);
    } catch (final SecurityException e) {
      throw new UnsupportedOperationException("Problem invoking method getNetworkTimeout()", e);
    } catch (final IllegalArgumentException e) {
      throw new UnsupportedOperationException("Problem invoking method getNetworkTimeout()", e);
    } catch (final IllegalAccessException e) {
      throw new UnsupportedOperationException("Problem invoking method getNetworkTimeout()", e);
    } catch (final InvocationTargetException e) {
      final Throwable target = e.getTargetException();
      if (target instanceof SQLException) {
        throw (SQLException) target;
      } else if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      throw new UnsupportedOperationException("Problem invoking method getNetworkTimeout()", e);
    }
    return result;
  }

} // ConnectionWrapper
