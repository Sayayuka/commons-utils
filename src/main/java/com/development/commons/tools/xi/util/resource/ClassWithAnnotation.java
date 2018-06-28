package com.development.commons.tools.xi.util.resource;

public abstract class ClassWithAnnotation extends ClassVisitor {

    /**
     * .
     */
    protected Class[] targetAnnotationClasses;

    /**
     * .
     *
     * @param classLoader
     *            classLoader
     * @param targetAnnotationClasses
     *            targetAnnotationClasses
     */
    public ClassWithAnnotation(final ClassLoader classLoader, final Class... targetAnnotationClasses) {
        super(classLoader);
        this.targetAnnotationClasses = targetAnnotationClasses;
    }

    @Override
    public Class<?> convertObject(final Resource resource) {
        final Class clz = super.convertObject(resource);
        if (clz != null) {
            for (final Class annotationClass : targetAnnotationClasses) {
                if (clz.getAnnotation(annotationClass) != null) {
                    return clz;
                }
            }
        }
        return null;
    }

}
