/*******************************************************************************
 * Copyright (c) 2004, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Hannes Wellmann - Bug 576643: Clean up and unify Bundle resource classes
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.SecureAction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.url.BundleResourceHandler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.util.Enumeration;

/**
 * The BundleFile API is used by Adaptors to read resources out of an installed
 * Bundle in the Framework.
 * <p>
 * Clients wishing to modify or extend the functionality of this class at
 * runtime should extend the associated {@link BundleFileWrapper decorator}
 * instead.
 * </p>
 */
abstract public class BundleFile {
    static final SecureAction secureAction = AccessController.doPrivileged(SecureAction.createSecureAction());
    /**
     * The File object for this BundleFile.
     */
    protected File basefile;
    private int mruIndex = -1;

    /**
     * BundleFile constructor
     * 
     * @param basefile The File object where this BundleFile is persistently stored.
     */
    protected BundleFile(File basefile) {
        this.basefile = basefile;
    }

    /**
     * Returns a File for the bundle entry specified by the path. If required the
     * content of the bundle entry is extracted into a file on the file system.
     * 
     * @param path The path to the entry to locate a File for.
     * @param nativeCode true if the path is native code.
     * @return A File object to access the contents of the bundle entry.
     */
    abstract public File getFile(String path, boolean nativeCode);

    /**
     * Locates a file name in this bundle and returns a BundleEntry object
     *
     * @param path path of the entry to locate in the bundle
     * @return BundleEntry object or null if the file name does not exist in the
     * bundle
     */
    abstract public BundleEntry getEntry(String path);

    /**
     * Performs the same function as calling {@link #getEntryPaths(String, boolean)}
     * with <code>recurse</code> equal to <code>false</code>.
     * 
     * @param path path of the entry to locate in the bundle
     * @return an Enumeration of Strings that indicate the paths found or null if
     * the path does not exist.
     */
    public Enumeration<String> getEntryPaths(String path) {
        return getEntryPaths(path, false);
    }

    /**
     * Allows to access the entries of the bundle. Since the bundle content is
     * usually a jar, this allows to access the jar contents.
     *
     * GetEntryPaths allows to enumerate the content of "path". If path is a
     * directory, it is equivalent to listing the directory contents. The returned
     * names are either files or directories themselves. If a returned name is a
     * directory, it finishes with a slash. If a returned name is a file, it does
     * not finish with a slash.
     * 
     * @param path path of the entry to locate in the bundle
     * @param recurse - If <code>true</code>, provide entries for the files and
     * directories within the directory denoted by <code>path</code>
     * plus all sub-directories and files; otherwise, provide only
     * the entries within the immediate directory.
     * @return an Enumeration of Strings that indicate the paths found or null if
     * the path does not exist.
     */
    abstract public Enumeration<String> getEntryPaths(String path, boolean recurse);

    /**
     * Closes the BundleFile.
     * 
     * @throws IOException if any error occurs.
     */
    abstract public void close() throws IOException;

    /**
     * Opens the BundleFiles.
     * 
     * @throws IOException if any error occurs.
     */
    abstract public void open() throws IOException;

    /**
     * Determines if any BundleEntries exist in the given directory path.
     * 
     * @param dir The directory path to check existence of.
     * @return true if the BundleFile contains entries under the given directory
     * path; false otherwise.
     */
    abstract public boolean containsDir(String dir);

    /**
     * Returns the base file for this BundleFile
     * 
     * @return the base file for this BundleFile
     */
    public File getBaseFile() {
        return basefile;
    }

    void setMruIndex(int index) {
        mruIndex = index;
    }

    int getMruIndex() {
        return mruIndex;
    }

    @Override
    public String toString() {
        return String.valueOf(basefile);
    }

    public static URL createURL(String protocol, long bundleId, ModuleContainer container, BundleEntry entry, int index,
        String path, URLStreamHandler handler) {
        path = fixTrailingSlash(path, entry);
        try {
            String host = BundleResourceHandler.createURLHostForBundleID(container, bundleId);
            return secureAction.getURL(protocol, host, index, path, handler);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String fixTrailingSlash(String path, BundleEntry entry) {
        if (path.isEmpty())
            return "/"; //$NON-NLS-1$
        if (path.charAt(0) != '/')
            path = '/' + path;
        String name = entry.getName();
        if (name.isEmpty())
            return path;
        boolean pathSlash = path.charAt(path.length() - 1) == '/';
        boolean entrySlash = !name.isEmpty() && name.charAt(name.length() - 1) == '/';
        if (entrySlash != pathSlash) {
            if (entrySlash)
                path = path + '/';
            else
                path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
