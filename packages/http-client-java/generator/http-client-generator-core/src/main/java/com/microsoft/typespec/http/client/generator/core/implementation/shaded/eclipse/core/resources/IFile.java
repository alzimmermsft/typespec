/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Sergey Prigogin (Google) - [462440] IFile#getContents methods should specify the status codes for its exceptions
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.FileUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdaptable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.QualifiedName;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentTypeManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Files are leaf resources which contain data.
 * The contents of a file resource is stored as a file in the local
 * file system.
 * <p>
 * Files, like folders, may exist in the workspace but
 * not be local; non-local file resources serve as place-holders for
 * files whose content and properties have not yet been fetched from
 * a repository.
 * </p>
 * <p>
 * Files implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFile extends IResource, IEncodedStorage, IAdaptable {
    /**
     * Character encoding constant (value 0) which identifies
     * files that have an unknown character encoding scheme.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_UNKNOWN = 0;
    /**
     * Character encoding constant (value 1) which identifies
     * files that are encoded with the US-ASCII character encoding scheme.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_US_ASCII = 1;
    /**
     * Character encoding constant (value 2) which identifies
     * files that are encoded with the ISO-8859-1 character encoding scheme,
     * also known as ISO-LATIN-1.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_ISO_8859_1 = 2;
    /**
     * Character encoding constant (value 3) which identifies
     * files that are encoded with the UTF-8 character encoding scheme.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_UTF_8 = 3;
    /**
     * Character encoding constant (value 4) which identifies
     * files that are encoded with the UTF-16BE character encoding scheme.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_UTF_16BE = 4;
    /**
     * Character encoding constant (value 5) which identifies
     * files that are encoded with the UTF-16LE character encoding scheme.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_UTF_16LE = 5;
    /**
     * Character encoding constant (value 6) which identifies
     * files that are encoded with the UTF-16 character encoding scheme.
     *
     * @see IFile#getEncoding()
     * @deprecated see getEncoding for details
     */
    @Deprecated
    int ENCODING_UTF_16 = 6;

    /**
     * Creates a new file resource as a member of this handle's parent resource.
     * The file's contents will be located in the file specified by the given
     * URI. The given URI must be either absolute, or a relative URI whose first path
     * segment is the name of a workspace path variable.
     * <p>
     * The <code>ALLOW_MISSING_LOCAL</code> update flag controls how this
     * method deals with cases where the file system file to be linked does
     * not exist, or is relative to a workspace path variable that is not defined.
     * If <code>ALLOW_MISSING_LOCAL</code> is specified, the operation will succeed
     * even if the local file is missing, or the path is relative to an undefined
     * variable. If <code>ALLOW_MISSING_LOCAL</code> is not specified, the operation
     * will fail in the case where the file system file does not exist or the
     * path is relative to an undefined variable.
     * </p>
     * <p>
     * The {@link IResource#REPLACE} update flag controls how this
     * method deals with cases where a resource of the same name as the
     * prospective link already exists. If {@link IResource#REPLACE}
     * is specified, then any existing resource with the same name is removed
     * from the workspace to make way for creation of the link. This does <b>not</b>
     * cause the underlying file system contents of that resource to be deleted.
     * If {@link IResource#REPLACE} is not specified, this method will
     * fail if an existing resource exists of the same name.
     * </p>
     * <p>
     * The {@link IResource#HIDDEN} update flag indicates that this resource
     * should immediately be set as a hidden resource. Specifying this flag
     * is equivalent to atomically calling {@link IResource#setHidden(boolean)}
     * with a value of <code>true</code> immediately after creating the resource.
     * </p>
     * <p>
     * Update flags other than those listed above are ignored.
     * </p>
     * <p>
     * This method synchronizes this resource with the file system at the given
     * location.
     * </p>
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event, including an indication
     * that the file has been added to its parent.
     * </p>
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param location a file system URI where the file should be linked
     * @param updateFlags bit-wise or of update flag constants
     * ({@link IResource#ALLOW_MISSING_LOCAL}, {@link IResource#REPLACE} and {@link IResource#HIDDEN})
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource already exists in the workspace.</li>
     * <li> The workspace contains a resource of a different type
     * at the same path as this resource.</li>
     * <li> The parent of this resource does not exist.</li>
     * <li> The parent of this resource is not an open project</li>
     * <li> The name of this resource is not valid (according to
     * <code>IWorkspace.validateName</code>).</li>
     * <li> The corresponding location in the file system does not exist, or
     * is relative to an undefined variable, and <code>ALLOW_MISSING_LOCAL</code> is
     * not specified.</li>
     * <li> The corresponding location in the file system is occupied
     * by a directory (as opposed to a file).</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * <li>The team provider for the project which contains this folder does not permit
     * linked resources.</li>
     * <li>This folder's project contains a nature which does not permit linked resources.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     * @see IResource#isLinked()
     * @see IResource#ALLOW_MISSING_LOCAL
     * @see IResource#REPLACE
     * @see IResource#HIDDEN
     * @since 3.2
     */
    void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the name of a charset to be used when decoding the contents of this
     * file into characters.
     * <p>
     * This refinement of the corresponding {@link IEncodedStorage} method
     * is a convenience method, fully equivalent to:</p>
     * 
     * <pre>
     * getCharset(true);
     * </pre>
     * 
     * <p>
     * <b>Note 1</b>: this method does not check whether the result is a supported
     * charset name. Callers should be prepared to handle
     * <code>UnsupportedEncodingException</code> where this charset is used.
     * </p>
     * <p>
     * <b>Note 2</b>: this method returns a cached value for the encoding
     * that may be out of date if the file is not synchronized with the local file system
     * and the encoding has since changed in the file system.
     * </p>
     *
     * @return the name of a charset
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource could not be read.</li>
     * <li> This resource is not local.</li>
     * <li> The corresponding location in the local file system
     * is occupied by a directory.</li>
     * </ul>
     * @since 3.0
     */
    @Override
    String getCharset() throws CoreException;

    /**
     * Returns the name of a charset to be used when decoding the contents of this
     * file into characters.
     * <p>
     * If checkImplicit is <code>false</code>, this method will return the
     * charset defined by calling <code>setCharset</code>, provided this file
     * exists, or <code>null</code> otherwise.
     * </p><p>
     * If checkImplicit is <code>true</code>, this method uses the following
     * algorithm to determine the charset to be returned:</p>
     * <ol>
     * <li>the charset defined by calling #setCharset, if any, and this file
     * exists, or</li>
     * <li>the charset automatically discovered based on this file's contents,
     * if one can be determined, or</li>
     * <li>the default encoding for this file's parent (as defined by
     * <code>IContainer#getDefaultCharset</code>).</li>
     * </ol>
     * <p>
     * <b>Note 1</b>: this method does not check whether the result is a supported
     * charset name. Callers should be prepared to handle
     * <code>UnsupportedEncodingException</code> where this charset is used.
     * </p>
     * <p>
     * <b>Note 2</b>: this method returns a cached value for the encoding
     * that may be out of date if the file is not synchronized with the local file system
     * and the encoding has since changed in the file system.
     * </p>
     *
     * @return the name of a charset, or <code>null</code>
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource could not be read.</li>
     * <li> This resource is not local.</li>
     * <li> The corresponding location in the local file system
     * is occupied by a directory.</li>
     * </ul>
     * @see IEncodedStorage#getCharset()
     * @see IContainer#getDefaultCharset()
     * @since 3.0
     */
    String getCharset(boolean checkImplicit) throws CoreException;

    /**
     * Returns an open input stream on the contents of this file.
     * <p>
     * This refinement of the corresponding {@link IStorage} method
     * is a convenience method returning an open input stream. It's equivalent to:
     * </p>
     * 
     * <pre>
     *   getContents(RefreshManager#PREF_LIGHTWEIGHT_AUTO_REFRESH);
     * </pre>
     * 
     * <p>
     * If lightweight auto-refresh is not enabled this method will throw a CoreException
     * when opening out-of-sync resources.
     * </p>
     * The client is responsible for closing the stream when finished.
     *
     * @return an input stream containing the contents of the file
     * @exception CoreException if this method fails. The status code associated with exception
     * reflects the cause of the failure. Reasons include:
     * <ul>
     * <li> {@link IResourceStatus#RESOURCE_NOT_FOUND} - This file does not exist.
     * Please notice that a successful {@link #exists()} check prior to calling
     * {@link #getContents()} does not guarantee the file existence since the file may be
     * deleted outside Eclipse at the very last moment.</li>
     * <li> {@link IResourceStatus#RESOURCE_NOT_LOCAL} - This resource is not local.</li>
     * <li> {@link IResourceStatus#RESOURCE_WRONG_TYPE} - The file-system resource is not
     * a file.</li>
     * <li> {@link IResourceStatus#OUT_OF_SYNC_LOCAL} - The workspace is not in sync with
     * the corresponding location in the local file system (and
     * {@link ResourcesPlugin#PREF_LIGHTWEIGHT_AUTO_REFRESH} is disabled).</li>
     * </ul>
     */
    @Override
    InputStream getContents() throws CoreException;

    /**
     * This refinement of the corresponding <code>IStorage</code> method
     * returns an open input stream on the contents of this file.
     * The client is responsible for closing the stream when finished.
     * If force is <code>true</code> the file is opened and an input
     * stream returned regardless of the sync state of the file. The file
     * is not synchronized with the workspace.
     * If force is <code>false</code> the method fails if not in sync.
     *
     * @param force a flag controlling how to deal with resources that
     * are not in sync with the local file system
     * @return an input stream containing the contents of the file
     * @exception CoreException if this method fails. The status code associated with exception
     * reflects the cause of the failure. Reasons include:
     * <ul>
     * <li> {@link IResourceStatus#RESOURCE_NOT_FOUND} - This file does not exist.
     * Please notice that a successful {@link #exists()} check prior to calling
     * {@link #getContents()} does not guarantee the file existence since the file may be
     * deleted outside Eclipse at the very last moment.</li>
     * <li> {@link IResourceStatus#RESOURCE_NOT_LOCAL} - This resource is not local.</li>
     * <li> {@link IResourceStatus#RESOURCE_WRONG_TYPE} - The file-system resource is not
     * a file.</li>
     * <li> {@link IResourceStatus#OUT_OF_SYNC_LOCAL} - The workspace is not in sync with
     * the corresponding location in the local file system (and
     * {@link ResourcesPlugin#PREF_LIGHTWEIGHT_AUTO_REFRESH} is disabled).</li>
     * </ul>
     */
    InputStream getContents(boolean force) throws CoreException;

    /**
     * Returns the full path of this file.
     * This refinement of the corresponding <code>IStorage</code> and <code>IResource</code>
     * methods links the semantics of resource and storage object paths such that
     * <code>IFile</code>s always have a path and that path is relative to the
     * containing workspace.
     *
     * @see IResource#getFullPath()
     * @see IStorage#getFullPath()
     */
    @Override
    IPath getFullPath();

    /**
     * Returns the name of this file.
     * This refinement of the corresponding <code>IStorage</code> and <code>IResource</code>
     * methods links the semantics of resource and storage object names such that
     * <code>IFile</code>s always have a name and that name equivalent to the
     * last segment of its full path.
     *
     * @see IResource#getName()
     * @see IStorage#getName()
     */
    @Override
    String getName();

    /**
     * Returns whether this file is read-only.
     * This refinement of the corresponding <code>IStorage</code> and <code>IResource</code>
     * methods links the semantics of read-only resources and read-only storage objects.
     *
     * @see IResource#isReadOnly()
     * @see IStorage#isReadOnly()
     */
    @SuppressWarnings("deprecation")
    @Override
    boolean isReadOnly();

    /**
     * Reads the content in a byte array. This method is not intended for reading in
     * large files that do not fit in a byte array. Preferable (faster) equivalent
     * of calling {@code getContents(true).readAllBytes()}.
     *
     * @return content bytes
     * @throws CoreException on error
     * @see #getContents(boolean)
     * @since 3.21
     */
    default byte[] readAllBytes() throws CoreException {
        // Meant to be overridden for local files
        // as Files.readAllBytes is ~ 1.5 times faster
        try (InputStream stream = getContents(true)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new CoreException(Status.error("Error reading " + getFullPath(), e)); //$NON-NLS-1$
        }
    }

    /**
     * Reads the content as char array. Skips the UTF BOM header if any. This method
     * is not intended for reading in large files that do not fit in a char array.
     * Preferable (potentially faster) equivalent of calling
     * {@code readString().toCharArray()}.
     *
     * @return content as char array without UTF BOM header
     * @throws CoreException on error
     * @see #readString()
     * @since 3.21
     */
    default char[] readAllChars() throws CoreException {
        return FileUtil.readAllChars(this);
    }

    /**
     * Returns line separator appropriate for the given file.
     * <p>
     * If checkParent is <code>false</code>, this method will return the line
     * separator by reading the file, or <code>null</code> otherwise.
     * </p>
     * <p>
     * If checkParent is <code>true</code>, this method uses the following algorithm
     * to determine the line separator to be returned:
     * </p>
     * <ol>
     * <li>the line separator currently used in that file (same as output with
     * checkImplicit=false), or</li>
     * <li>delegates to {@link IProject#getDefaultLineSeparator()}</li>
     * </ol>
     *
     * @param checkParent whether to look up in parent containers settings to
     * retrieve a value
     * @return line separator for the current file
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li>This resource could not be read.</li>
     * <li>This resource is not local.</li>
     * <li>The corresponding location in the local file
     * system is occupied by a directory.</li>
     * </ul>
     * @see IProject#getDefaultLineSeparator()
     * @since 3.18
     */
    default String getLineSeparator(boolean checkParent) throws CoreException {
        return getProject().getDefaultLineSeparator();
    }
}
