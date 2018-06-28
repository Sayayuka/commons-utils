/* $Id$ */
package com.development.commons.tools.jndi;

public class EJBHomeFactoryCreationException extends Exception {

    private static final long serialVersionUID = -6483643818025710088L;

    /**
     * Creates a new <code>EJBHomeFactoryCreationException</code> with null as its detail message.
     */
    public EJBHomeFactoryCreationException() {
        super();
    }

    /**
     * Creates a new <code>EJBHomeFactoryCreationException</code> with the specified detail message.
     *
     * @param message the detail message
     */
    public EJBHomeFactoryCreationException(final String message) {
        super(message);
    }

    /**
     * Creates a new <code>EJBHomeFactoryCreationException</code> with the specified detail message and cause.
     *
     * @param message the detail mesage
     * @param cause   the cause
     */
    public EJBHomeFactoryCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new <code>EJBHomeFactoryCreationException</code> with the specified cause.
     *
     * @param cause the cause
     */
    public EJBHomeFactoryCreationException(final Throwable cause) {
        super(cause);
    }

}
