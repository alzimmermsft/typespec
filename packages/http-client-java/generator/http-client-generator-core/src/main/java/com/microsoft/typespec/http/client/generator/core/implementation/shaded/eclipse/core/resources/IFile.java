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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;

import java.io.IOException;
import java.io.InputStream;

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
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFile extends IResource {

    /**
     * Returns an open input stream on the contents of this file.
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
     */
    @Override
    String getName();

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
     * @since 3.21
     */
    default char[] readAllChars() throws CoreException {
        return FileUtil.readAllChars(this);
    }
}
