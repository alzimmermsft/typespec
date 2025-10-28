/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     IBM Corporation - specified that a source archive or a source folder can be attached to a binary
 *                               package fragment root.
 *     IBM Corporation - added root manipulation APIs: copy, delete, move
 *     IBM Corporation - added DESTINATION_PROJECT_CLASSPATH
 *     IBM Corporation - added OTHER_REFERRING_PROJECTS_CLASSPATH
 *     IBM Corporation - added NO_RESOURCE_MODIFICATION
 *     IBM Corporation - added REPLACE
 *     IBM Corporation - added ORIGINATING_PROJECT_CLASSPATH
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * A package fragment root contains a set of package fragments.
 * It corresponds to an underlying resource which is either a folder,
 * JAR, or zip. In the case of a folder, all descendant folders represent
 * package fragments. For a given child folder representing a package fragment,
 * the corresponding package name is composed of the folder names between the folder
 * for this root and the child folder representing the package, separated by '.'.
 * In the case of a JAR or zip, the contents of the archive dictates
 * the set of package fragments in an analogous manner.
 * Package fragment roots need to be opened before they can be navigated or manipulated.
 * The children are of type <code>IPackageFragment</code>, and are in no particular order.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPackageFragmentRoot extends IParent, IJavaElement, IOpenable {
    /**
     * Kind constant for a source path root. Indicates this root
     * only contains source files.
     */
    int K_SOURCE = 1;
    /**
     * Kind constant for a binary path root. Indicates this
     * root only contains binary files.
     */
    int K_BINARY = 2;
    /**
     * Empty root path
     */
    String DEFAULT_PACKAGEROOT_PATH = ""; //$NON-NLS-1$
    /**
     * Update model flag constant (bit mask value 1) indicating that the operation
     * is to not copy/move/delete the package fragment root resource.
     * 
     * @since 2.1
     */
    int NO_RESOURCE_MODIFICATION = 1;
    /**
     * Update model flag constant (bit mask value 2) indicating that the operation
     * is to update the classpath of the originating project.
     * 
     * @since 2.1
     */
    int ORIGINATING_PROJECT_CLASSPATH = 2;
    /**
     * Update model flag constant (bit mask value 4) indicating that the operation
     * is to update the classpath of all referring projects except the originating project.
     * 
     * @since 2.1
     */
    int OTHER_REFERRING_PROJECTS_CLASSPATH = 4;
    /**
     * Update model flag constant (bit mask value 16) indicating that the operation
     * is to replace the resource and the destination project's classpath entry.
     * 
     * @since 2.1
     */
    int REPLACE = 16;

    /**
     * Returns this package fragment root's kind encoded as an integer.
     * A package fragment root can contain source files (i.e. files with one
     * of the {@link JavaCore#getJavaLikeExtensions() Java-like extensions},
     * or <code>.class</code> files, but not both.
     * If the underlying folder or archive contains other kinds of files, they are ignored.
     * In particular, <code>.class</code> files are ignored under a source package fragment root,
     * and source files are ignored under a binary package fragment root.
     *
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource.
     * @return this package fragment root's kind encoded as an integer
     * @see IPackageFragmentRoot#K_SOURCE
     * @see IPackageFragmentRoot#K_BINARY
     */
    int getKind() throws JavaModelException;

    /**
     * Returns the package fragment with the given package name.
     * An empty string indicates the default package.
     * This is a handle-only operation. The package fragment
     * may or may not exist.
     *
     * @param packageName the given package name
     * @return the package fragment with the given package name
     */
    IPackageFragment getPackageFragment(String packageName);

    /**
     * Returns the first raw classpath entry that corresponds to this package
     * fragment root.
     * A raw classpath entry corresponds to a package fragment root if once resolved
     * this entry's path is equal to the root's path.
     *
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource.
     * @return the first raw classpath entry that corresponds to this package fragment root
     * @since 2.0
     */
    IClasspathEntry getRawClasspathEntry() throws JavaModelException;

    /**
     * Returns the first resolved classpath entry that corresponds to this package fragment root.
     * A resolved classpath entry is said to correspond to a root if the path of the resolved
     * entry is equal to the root's path.
     *
     * @return the first resolved classpath entry that corresponds to this package fragment root
     * @throws JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource.
     * @since 3.6
     */
    IClasspathEntry getResolvedClasspathEntry() throws JavaModelException;

    /**
     * Returns the absolute path to the source archive attached to
     * this package fragment root's binary archive.
     *
     * @return the absolute path to the corresponding source archive,
     * or <code>null</code> if this package fragment root's binary archive
     * has no corresponding source archive, or if this package fragment root
     * is not a binary archive
     * @exception JavaModelException if this operation fails
     */
    IPath getSourceAttachmentPath() throws JavaModelException;

    /**
     * Returns the path within this package fragment root's source archive.
     * An empty path indicates that packages are located at the root of the
     * source archive.
     *
     * @return the path within the corresponding source archive,
     * or <code>null</code> if this package fragment root's binary archive
     * has no corresponding source archive, or if this package fragment root
     * is not a binary archive
     * @exception JavaModelException if this operation fails
     */
    IPath getSourceAttachmentRootPath() throws JavaModelException;

    /**
     * Returns whether this package fragment root's underlying
     * resource is a binary archive (a JAR or zip file).
     * <p>
     * This is a handle-only method.
     * </p>
     *
     * @return true if this package fragment root's underlying resource is a binary archive, false otherwise
     */
    boolean isArchive();

    /**
     * Returns whether this package fragment root is external
     * to the workbench (that is, a local file), and has no
     * underlying resource.
     * <p>
     * This is a handle-only method.
     * </p>
     *
     * @return true if this package fragment root is external
     * to the workbench (that is, a local file), and has no
     * underlying resource, false otherwise
     */
    boolean isExternal();

    /**
     * Returns the <code>IModuleDescription</code> that this package fragment root contains.
     * Returns <code>null</code> if the root doesn't contain any named module or if the project compiler compliance is
     * 1.8 or lower.
     * If present the module descriptor is found as a child of the package fragment representing the default package.
     *
     * Note that only one of the source package fragment roots in a Java Project can legally
     * contain a module descriptor.
     *
     * @return the <code>IModuleDescription</code> this root contains.
     * @since 3.14
     */
    IModuleDescription getModuleDescription();
}
