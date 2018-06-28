package com.development.commons.tools.xi.util.resource;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AnnotationScanner extends ResourceScanner {

    /**
     * .
     *
     * @param rootLocation
     *            rootLocation
     * @param resourcePattern
     *            resourcePattern
     * @param visitor
     *            visitor
     */
    public AnnotationScanner(final URL rootLocation, final String resourcePattern, final ResourceVisitor visitor) {
        super(rootLocation, resourcePattern, visitor);
    }

    @Override
    public void scan() {
        String nPat = resourcePattern;

        nPat = nPat.replaceAll("\\*\\*", "#");
        // ** can be zero or more so need to get rid of the /
        nPat = nPat.replaceAll("/#", "/?#");
        nPat = nPat.replace("*", ".+");
        nPat = nPat.replace("#", ".*");

        if (!nPat.endsWith("/.*")) {
            nPat += "/.*";
        }

        String path = location.getPath();
        final String filePrefix = "file:";
        if (path.startsWith(filePrefix)) {
            path = path.substring(filePrefix.length());
        }
        final AnnotationIndex annotationIndex = AnnotationIndex.ALL_FILE_INDEX.get(path);

        final Set<Class<Object>> classesWithAnnotations = new HashSet<Class<Object>>();
        if (annotationIndex != null) {
            for (final Class annotationClass : ((ClassWithAnnotation) visitor).targetAnnotationClasses) {
                final Set<Class<Object>> classesWithAnnotation = annotationIndex.findClassesWithAnnotation(annotationClass, ((ClassVisitor) visitor).classLoader);
                classesWithAnnotations.addAll(classesWithAnnotation);
            }
        }

        for (final Class clz : classesWithAnnotations) {
            if (clz.getName().replace('.', '/').matches(nPat)) {
                ((ClassWithAnnotation) visitor).visit(clz);
            }
        }

    }

}
