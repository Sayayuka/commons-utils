package com.development.commons.tools.xi.util.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.development.commons.tools.StringUtils;

/**
 * Scans directories or jars for resources of some type and passes each one to a visitor object.
 *
 * @author ddiodati
 *
 */
public class ResourceScanner<R, T extends ResourceVisitor<R>> {

    /**
     * The resource pattern to locate files.
     */
    protected final String resourcePattern;

    /**
     * The visitor object to invoke.
     */
    protected final T visitor;

    /**
     * The starting root url.
     */
    protected final URL location;

    /**
     * The pattern of files to match on.
     */
    protected Pattern filePat;

    /**
     * Logger.
     */
//    private static final Logger log = LogManager.getLogger();

    /**
     * Creates a resource scanner to visitor specific resources.
     *
     * @param rootLocation
     *            The root directory or jar to look into.
     * @param resourcePattern
     *            The pattern of resources to find. This follows an ant directory based pattern. For example:
     *
     *            {@literal com/successfactors/** } {@literal com/successfactors } {@literal com/successfactors/**\/components }
     *
     * @param visitor
     *            The visitor object to visit each resource.
     */
    public ResourceScanner(final URL rootLocation, final String resourcePattern, final T visitor) {
        this.resourcePattern = resourcePattern;
        this.visitor = visitor;
        this.location = rootLocation;
    }

    /**
     * Scans throw urls looking for resources and passing them to the specified visitor object.
     *
     * @throws IOException
     *             If there is a problem reading the resource.
     */
    public void scan() throws IOException {
        if (location == null) {
            throw new IOException("Root location was null");
        }
        findResources(location);
    }

    /**
     * Looks for resources and starts a scan either through directories or jars.
     *
     * @param url
     * @throws IOException
     */
    private void findResources(final URL url) throws IOException {

        String urlPath = url.getFile();
        urlPath = URLDecoder.decode(urlPath, "UTF-8");

        final String filePrefix = "file:";
        if (urlPath.startsWith(filePrefix)) {
            // strip off the file: if it exists
            urlPath = urlPath.substring(filePrefix.length());
        }

        if (urlPath.indexOf('!') > 0) {
            urlPath = urlPath.substring(0, urlPath.indexOf('!'));
        }

        final String[] resourcePatterns = resourcePattern.split("/");
        filePat = Pattern.compile(initFilePattern());

        final File root = new File(urlPath);

        if (root.isDirectory()) {

            if (!urlPath.endsWith("/")) {
                urlPath += "/";
            }

//            log.debug("Scanning at " + urlPath);

            findDirResources(new File(urlPath), resourcePatterns, 0, "");
        } else if (root.getName().indexOf(".jar") > -1 || root.getName().indexOf(".zip") > -1) {
            findJarResources(root, resourcePatterns);
        } else {
            // use the parent directory if this is some resource file
            findDirResources(root.getParentFile(), resourcePatterns, 0, "");
        }

    }

    /**
     * .
     *
     * @return String
     */
    protected String initFilePattern() {

        final String exts[] = visitor.getSupportedFileExts();

        String result = "";
        if (exts == null || exts[0].equals("*")) {
            result = ".+";
        } else {
            for (int i = 0; i < exts.length; i++) {
                String nPatt;
                nPatt = exts[i].replaceAll("\\.", "\\\\.");
                nPatt = nPatt.replaceAll("\\*", ".*");

                result += nPatt;

                if (i + 1 < exts.length) {
                    result += "|";
                }
            }
        }

        return result;

    }

    /**
     * .
     *
     * @param base
     *            base
     * @param patterns
     *            patterns
     * @param index
     *            index
     * @param path
     *            path
     */
    protected void findDirResources(final File base, final String[] patterns, final int index, final String path) {
        final File[] files = base.listFiles();

        for (final File child : files) {
            if (!visitor.isDone()) {
                // if this is a file and the index is pass the list of patterns
                // (means we have matched parent directories) then check it.

                if (child.isFile() && index == patterns.length) {
                    // then check if the file name pattern
                    // matches. If true visit.
                    if (isFileNameMatch(child.getName())) {
                        visitItem(new ResourceObj(child, path + "/" + child.getName()));
                    }
                }
                // if we match the directory name then follow it, and the current pattern matches
                // the directory then search it, incrementing the pattern index and path.
                //
                else if (child.isDirectory() && (patterns[index].equals(child.getName()) || patterns[index].equals("*"))) {
                    findDirResources(child, patterns, index + 1, path + "/" + child.getName());
                }
                // If this is a wild card directory pattern then handle it.
                else if (patterns[index].equals("**")) {
                    // if this is a file and the next pattern is the last file pattern
                    // then include this file if it is a match. This handles the case **/*.java,
                    // where there is no directories yet for **(since it matches 0 or more directories).
                    if (child.isFile() && index + 1 == patterns.length && isFileNameMatch(child.getName())) {
                        // don't append to path since the child may not be added
                        // and it is not a directory
                        visitItem(new ResourceObj(child, path + "/" + child.getName()));
                    }
                    // handle directory case.
                    else if (child.isDirectory()) {

                        // we look ahead to see if there are any other directories after the directory
                        // wild card. If there is one and we match it then follow that directory
                        // incrementing to the next pattern.
                        if (index + 1 < patterns.length && patterns[index + 1].equals(child.getName())) {
                            findDirResources(child, patterns, index + 2, path + "/" + child.getName());
                        }
                        // Otherwise we match any directory since we are in wild card matching mode.
                        // We search this directory recursively but do not change the
                        // the pattern index
                        // but leave us at the wild card pattern
                        else {
                            findDirResources(child, patterns, index, path + "/" + child.getName());
                        }
                    }

                    // else this is a file but we are in the middle of the pattern match so it should
                    // not be visited.
                }
            } else {
                break;
            }
        }

    }

    /**
     * Does a pattern file name match.
     *
     * @param name
     *            The string
     * @return true if the name matches or false otherwise.
     */
    protected boolean isFileNameMatch(final String name) {
        return filePat.matcher(name).matches();
    }

    /**
     * .
     *
     * @param base
     *            base
     * @param paths
     *            paths
     * @throws IOException
     *             when this fails
     */
    protected void findJarResources(final File base, final String[] paths) throws IOException {

        final ZipFile zip = new ZipFile(base);
        try {
            final Enumeration<? extends ZipEntry> entries = zip.entries();

            String nPat = StringUtils.join(paths, "/");

            // replace the ** with a zero or more regular expression match.
            nPat = nPat.replaceAll("\\*\\*", "#");
            // ** can be zero or more so need to get rid of the /
            nPat = nPat.replaceAll("/#", "/?#");
            nPat = nPat.replace("*", ".+");
            nPat = nPat.replace("#", ".*");

            if (!nPat.endsWith("/.*")) {
                nPat += "/.*";
            }

//            log.debug("Scanning jar " + base.getAbsolutePath() + " with pattern " + nPat);

            while (entries.hasMoreElements()) {
                final ZipEntry child = entries.nextElement();

                // System.out.println("Here 2 " + child.getName());
                if (child.isDirectory()) {
                    continue;
                } else if (child.getName().matches(nPat)) {
                    final ResourceObj resource = new ResourceObj(zip, child, child.getName());
                    if (isFileNameMatch(resource.getName())) {
                        visitItem(resource);
                    }
                }
            }
        } finally {
            zip.close();
        }

    }

    /**
     * .
     *
     * @param resource
     *            resource
     */
    protected void visitItem(final ResourceObj resource) {
        // now try to convert the object into the needed type.
        // if it fails to convert then don't don't call visit.
        final R result = visitor.convertObject(resource);

        if (result != null) {
            visitor.visit(result);
        }
        // if a null is returned then it skips calling visit on this resource.

        return;

    }

    /**
     * Internal impl of Resource object.
     *
     * @see Resource
     */
    protected static final class ResourceObj implements Resource {

        /**
         * The main archive if this is a zip/jar file.
         */
        private ZipFile archive;

        /**
         * The current file/directory entry in the zip file that this resource represents.
         */
        private final Object entry;

        /**
         * The path to the resource relative to the root url of the scanner.
         */
        private final String path;

        /**
         * The name of the resource file.
         */
        private String name;

        /**
         * Creates a Resource based on zip entry.
         *
         * @param archive
         *            The main zip archive.
         * @param entry
         *            The current zip entry item.
         * @param path
         *            The path to the file (relative to the root url of the scanner).
         */
        public ResourceObj(final ZipFile archive, final ZipEntry entry, final String path) {
            this.entry = entry;
            this.archive = archive;
            name = entry.getName();
            final int i = name.lastIndexOf("/");
            if (i > -1) {
                name = name.substring(i + 1);
            }

            this.path = path;

        }

        /**
         * Creates a file based resource.
         *
         * @param f
         *            The file object
         * @param relPath
         *            The file path relative to the root url of the scanner.
         */
        public ResourceObj(final File f, final String relPath) {
            entry = f;
            name = f.getName();

            path = relPath;

        }

        /**
         * {@inheritDoc}
         */
        public InputStream getInputStream() throws IOException {
            if (entry instanceof ZipEntry) {
                final ZipEntry e = (ZipEntry) entry;
                return archive.getInputStream(e);
            }
            final File f = (File) entry;
            return new FileInputStream(f);
        }

        /**
         * Returns the last modified date of the resource.
         *
         * @return The time in milliseconds.
         */
        public long getLastModifiedDate() {
            if (entry instanceof ZipEntry) {
                final ZipEntry e = (ZipEntry) entry;
                return e.getTime();
            }
            final File f = (File) entry;
            return f.lastModified();
        }

        /**
         * {@inheritDoc}
         */
        public String getName() {
            return name;
        }

        /**
         * {@inheritDoc}
         */
        public String getPath() {
            return path;
        }
    }

}
