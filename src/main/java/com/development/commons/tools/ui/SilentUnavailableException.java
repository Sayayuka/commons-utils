package com.development.commons.tools.ui;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.UnavailableException;

/**
 * A simplified UnavailableException which can be thrown out to tell the servlet
 * container that a certain servlet is unavailable, and will not litter the
 * server log with exception stack trace.
 */
public class SilentUnavailableException extends UnavailableException {

    /**
     * Constructs a new exception with a descriptive message indicating that the
     * servlet is permanently unavailable.
     *
     * @param msg a <code>String</code> specifying the descriptive message
     */
    public SilentUnavailableException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new exception with a descriptive message indicating that the
     * servlet is temporarily unavailable and giving an estimate of how long it
     * will be unavailable.
     *
     * @param msg     a <code>String</code> specifying the descriptive message, which
     *                can be written to a log file or displayed for the user.
     * @param seconds an integer specifying the number of seconds the servlet expects to
     *                be unavailable; if zero or negative, indicates that the servlet
     *                can't make an estimate
     */
    public SilentUnavailableException(final String msg, final int seconds) {
        super(msg, seconds);
    }

    @Override
    public void printStackTrace(final PrintStream s) {
        synchronized (s) {
            s.println(this);
        }
    }

    @Override
    public void printStackTrace(final PrintWriter s) {
        synchronized (s) {
            s.println(this);
        }
    }

}
