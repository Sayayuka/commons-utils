package com.development.commons.tools.xi.util.resource;

import java.net.URL;

import com.development.commons.tools.UrlUtils;

public class ResourceScannerFactory {

    private static final String DISABLE_FAST_RESOURCE_SCANNER_PROPERTY = "sf-disable-fast-resource-scanner";

    private ResourceScannerFactory() {

    }

    /**
     * .
     *
     * @param <R>
     *            R
     * @param <T>
     *            T
     * @param rootLocation
     *            rootLocation
     * @param resourcePattern
     *            resourcePattern
     * @param visitor
     *            visitor
     * @return ResourceVisitor
     */
    public static <R, T extends ResourceVisitor<R>> ResourceScanner createScanner(final URL rootLocation, final String resourcePattern, final T visitor) {
        if (visitor instanceof ClassWithAnnotation && UrlUtils.isInsideJar(rootLocation)) {
            final AnnotationIndex annotationIndex = AnnotationIndex.ALL_FILE_INDEX.get(UrlUtils.getJarName(rootLocation));
            if (annotationIndex != null) {
                return new AnnotationScanner(rootLocation, resourcePattern, visitor);
            }
        }
        final boolean disableFastResourceScanner = Boolean.parseBoolean(System.getProperty(DISABLE_FAST_RESOURCE_SCANNER_PROPERTY, "false"));
        ResourceScanner resourceScanner = new FastResourceScanner(rootLocation, resourcePattern, visitor);
        if (disableFastResourceScanner) {
            resourceScanner = new ResourceScanner(rootLocation, resourcePattern, visitor);
        }
        return resourceScanner;
    }

}
