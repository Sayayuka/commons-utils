package com.development.commons.tools.xi.util.resource;

/**
 * Base implementation of a ClassVisitor that will scan for all class resources.
 *
 * @see ResourceVisitor
 * @author ddiodati
 *
 */
public abstract class ClassVisitor implements ResourceVisitor<Class<?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void visit(Class<?> clazz);

    /**
     * .
     */
    protected ClassLoader classLoader;

    /**
     * Supported exts for the this visitor.
     */
    private static final String[] EXTS = new String[] { "*.class" };

    /**
     * .
     */
    public ClassVisitor() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * .
     *
     * @param classLoader
     *            classLoader
     */
    public ClassVisitor(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSupportedFileExts() {
        return EXTS;
    }

    /**
     * {@inheritDoc}
     *
     * This converts the Resource object to a specific Class object.
     *
     */
    @Override
    public Class<?> convertObject(final Resource resource) {
        // build name of class
        String path = resource.getPath();

        // strip off the beginning /
        // and upto the .class
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        path = path.substring(0, path.indexOf(".class"));

        path = path.replace('/', '.');

        Class<?> clazz = null;

        try {
            clazz = classLoader.loadClass(path);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return clazz;
    }
}