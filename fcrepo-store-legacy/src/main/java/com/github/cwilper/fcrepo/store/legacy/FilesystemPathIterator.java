package com.github.cwilper.fcrepo.store.legacy;

import com.google.common.collect.AbstractIterator;

import java.io.File;

/**
 * Lazily iterates paths of all files beneath a given directory. Paths
 * returned by the iterator are relative to the given base directory.
 */
class FilesystemPathIterator extends AbstractIterator<String> {
    private final File baseDir;

    private DirectoryNode currentDir;

    /**
     * Creates an instance.
     *
     * @param baseDir the directory whose content should be iterated.
     */
    FilesystemPathIterator(File baseDir) {
        this.baseDir = baseDir;
        currentDir = new DirectoryNode(null, "");
    }

    @Override
    protected String computeNext() {
        while (currentDir != null) {
            DirectoryNode child = currentDir.nextChild();
            if (child == null) {
                currentDir = currentDir.parent;
            } else if (child.isDirectory()) {
                currentDir = child;
            } else {
                return child.path;
            }
        }
        return endOfData();
    }

    private class DirectoryNode {
        final DirectoryNode parent;
        final String path;

        private String[] childPaths;
        private int childNum;

        DirectoryNode(DirectoryNode parent, // null if root
                String path) { // "" if root, "name/" if subdir, "name" if file
            this.parent = parent;
            this.path = path;
            if (isDirectory()) {
                setChildPaths();
            }
        }

        private void setChildPaths() {
            File dir;
            if (path.length() == 0) {
                dir = baseDir;
            } else {
                dir = new File(baseDir, path);
            }
            File[] childFiles = dir.listFiles();
            childPaths = new String[childFiles.length];
            for (int i = 0; i < childFiles.length; i++) {
                StringBuilder childPath = new StringBuilder(path);
                childPath.append(childFiles[i].getName());
                if (childFiles[i].isDirectory()) {
                    childPath.append("/");
                }
                childPaths[i] = childPath.toString();
            }
        }

        boolean isDirectory() {
            return path.length() == 0 || path.endsWith("/");
        }

        DirectoryNode nextChild() {
            if (isDirectory()) {
                if (childNum == childPaths.length) {
                    return null; // no more children
                } else {
                    return new DirectoryNode(this, childPaths[childNum++]);
                }
            } else {
                return null; // not a directory
            }
        }
    }
}
