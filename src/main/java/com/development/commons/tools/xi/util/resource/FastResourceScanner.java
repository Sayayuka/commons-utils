package com.development.commons.tools.xi.util.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.development.commons.tools.StringUtils;

public class FastResourceScanner<R, T extends ResourceVisitor<R>> extends ResourceScanner<R, T> {

    private static final String THREAD_COUNT_PROPERTY_NAME = "sf-fast-resource-scanner-thread-count";

    private static final String DEFAULT_PROCESSING_CAPACITY = "200";

    private static final String PROCESSING_CAPACITY_PROPERTY = "sf-fast-resource-scanner-processing-capacity";

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
    public FastResourceScanner(final URL rootLocation, final String resourcePattern, final T visitor) {
        super(rootLocation, resourcePattern, visitor);
    }

    @Override
    protected void findJarResources(final File base, final String[] paths) throws IOException {
        final ZipFile zip = new ZipFile(base);
        final Enumeration<? extends ZipEntry> allEntries = zip.entries();
        final List<ZipEntry> entries = new ArrayList<ZipEntry>();
        while (allEntries.hasMoreElements()) {
            entries.add(allEntries.nextElement());
        }

        String patterns = StringUtils.join(paths, "/");

        patterns = patterns.replaceAll("\\*\\*", "#");
        patterns = patterns.replaceAll("/#", "/?#");
        patterns = patterns.replace("*", ".+");
        patterns = patterns.replace("#", ".*");

        if (!patterns.endsWith("/.*")) {
            patterns += "/.*";
        }
        final JarScanTask rootTask = new JarScanTask(entries, 0, entries.size(), zip, patterns);
        executeScanTask(rootTask);
    }

    private void executeScanTask(final RecursiveAction rootTask) {
        final String defaultThreadCount = ((Integer) Math.max(1, Runtime.getRuntime().availableProcessors() / 2)).toString();
        final int threadCount = Integer.parseInt(System.getProperty(THREAD_COUNT_PROPERTY_NAME, defaultThreadCount));
        // System.out.println("start fast resource scanner with "+threadCount+" thread(s)");
        final ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        forkJoinPool.invoke(rootTask);
        forkJoinPool.shutdownNow();
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
    @Override
    protected void findDirResources(final File base, final String[] patterns, final int index, final String path) {
        final DirScanTask rootTask = new DirScanTask(base, patterns, index, path);
        executeScanTask(rootTask);
    }

    private class DirScanTask extends RecursiveAction {

        private final File base;
        private final String[] patterns;
        private final int index;
        private final String path;

        private DirScanTask(final File base, final String[] patterns, final int index, final String path) {
            this.base = base;
            this.patterns = patterns;
            this.index = index;
            this.path = path;
            // System.out.println(String.format("fork new task: %s,%s,%d,%s",base.getName(),patterns.toString(),index,path));
        }

        @Override
        protected void compute() {
            final File[] files = base.listFiles();
            final List<DirScanTask> forks = new ArrayList<DirScanTask>();
            for (final File child : files) {
                if (!visitor.isDone()) {
                    // if this is a file and the index is pass the list of
                    // patterns
                    // (means we have matched parent directories) then check it.
                    if (child.isFile() && index == patterns.length) {
                        // then check if the file name pattern
                        // matches. If true visit.
                        if (isFileNameMatch(child.getName())) {
                            visitItem(new ResourceObj(child, path + "/" + child.getName()));
                        }
                    }
                    // if we match the directory name then follow it, and the
                    // current pattern matches
                    // the directory then search it, incrementing the pattern
                    // index
                    // and path.
                    //
                    else if (child.isDirectory() && (patterns[index].equals(child.getName()) || patterns[index].equals("*"))) {
                        // findDirResources(child, patterns, index + 1, path +
                        // "/"
                        // + child.getName());

                        final DirScanTask task = new DirScanTask(child, patterns, index + 1, path + "/" + child.getName());
                        forks.add(task);
                    }
                    // If this is a wild card directory pattern then handle it.
                    else if (patterns[index].equals("**")) {
                        // if this is a file and the next pattern is the last
                        // file
                        // pattern
                        // then include this file if it is a match. This handles
                        // the
                        // case **/*.java,
                        // where there is no directories yet for **(since it
                        // matches
                        // 0 or more directories).
                        if (child.isFile() && index + 1 == patterns.length && isFileNameMatch(child.getName())) {
                            // don't append to path since the child may not be
                            // added
                            // and it is not a directory
                            visitItem(new ResourceObj(child, path + "/" + child.getName()));
                        }
                        // handle directory case.
                        else if (child.isDirectory()) {

                            // we look ahead to see if there are any other
                            // directories after the directory
                            // wild card. If there is one and we match it then
                            // follow that directory
                            // incrementing to the next pattern.
                            if (index + 1 < patterns.length && patterns[index + 1].equals(child.getName())) {
                                // findDirResources(child, patterns, index + 2,
                                // path
                                // + "/" + child.getName());
                                final DirScanTask task = new DirScanTask(child, patterns, index + 2, path + "/" + child.getName());
                                forks.add(task);
                            }
                            // Otherwise we match any directory since we are in
                            // wild
                            // card matching mode.
                            // We search this directory recursively but do not
                            // change the
                            // the pattern index
                            // but leave us at the wild card pattern
                            else {
                                // findDirResources(child, patterns, index, path
                                // + "/"
                                // + child.getName());

                                final DirScanTask task = new DirScanTask(child, patterns, index, path + "/" + child.getName());
                                forks.add(task);
                            }
                        }

                        // else this is a file but we are in the middle of the
                        // pattern match so it should
                        // not be visited.
                    }
                } else {
                    break;
                }
            }
            invokeAll(forks);
        }
    }

    private class JarScanTask extends RecursiveAction {

        private final List<ZipEntry> entries;
        private final ZipFile zip;
        private final String patterns;
        private final int startPointInclusive;
        private final int endPointExclusive;
        private final int processingCapacity = Integer.parseInt(System.getProperty(PROCESSING_CAPACITY_PROPERTY, DEFAULT_PROCESSING_CAPACITY));

        private JarScanTask(final List<ZipEntry> entries, final int startPointInclusive, final int endPointExclusive, final ZipFile zip, final String patterns) {
            this.entries = entries;
            this.zip = zip;
            this.patterns = patterns;
            this.startPointInclusive = startPointInclusive;
            this.endPointExclusive = endPointExclusive;
        }

        @Override
        protected void compute() {

            if ((endPointExclusive - startPointInclusive) < processingCapacity) {
                doScan();
            } else {
                final int middlePoint = startPointInclusive + (endPointExclusive - startPointInclusive) / 2;
                // System.out.println(String.format("break down task [%d , %d]",startPointInclusive,endPointExclusive));
                final JarScanTask breakdownTask1 = new JarScanTask(entries, startPointInclusive, middlePoint, zip, patterns);
                // System.out.println(String.format("task1:[%d,%d]",startPointInclusive,middlePoint));
                final JarScanTask breakdownTask2 = new JarScanTask(entries, middlePoint, endPointExclusive, zip, patterns);
                // System.out.println(String.format("task2:[%d,%d]",middlePoint,endPointExclusive));
                invokeAll(breakdownTask1, breakdownTask2);
            }

        }

        private void doScan() {
            for (int i = startPointInclusive; i < endPointExclusive; i++) {
                final ZipEntry child = entries.get(i);
                if (child.isDirectory()) {
                    continue;
                } else if (child.getName().matches(patterns)) {
                    final ResourceObj resource = new ResourceObj(zip, child, child.getName());
                    if (isFileNameMatch(resource.getName())) {
                        visitItem(resource);
                    }
                }
            }
        }

    }

}
