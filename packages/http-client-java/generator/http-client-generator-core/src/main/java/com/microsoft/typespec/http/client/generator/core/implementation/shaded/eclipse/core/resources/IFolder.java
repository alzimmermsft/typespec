/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group Support
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;

/**
 * Folders may be leaf or non-leaf resources and may contain files and/or other folders.
 * A folder resource is stored as a directory in the local file system.
 * <p>
 * Folders, like other resource types, may exist in the workspace but
 * not be local; non-local folder resources serve as place-holders for
 * folders whose properties have not yet been fetched from a repository.
 * </p>
 * <p>
 * Folders implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFolder extends IContainer {

    /**
     * Deletes this resource from the workspace.
     * <p>
     * This is a convenience method, fully equivalent to:
     * </p>
     * 
     * <pre>
     * delete((keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
     * </pre>
     * 
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event, including an indication
     * that this folder has been removed from its parent.
     * </p>
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param force a flag controlling whether resources that are not
     * in sync with the local file system will be tolerated
     * @param keepHistory a flag controlling whether files under this folder
     * should be stored in the workspace's local history
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource could not be deleted for some reason.</li>
     * <li> This resource is out of sync with the local file system
     * and <code>force</code> is <code>false</code>.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     *
     */
    void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns a handle to the file with the given name in this folder.
     * <p>
     * This is a resource handle operation; neither the resource nor
     * the result need exist in the workspace.
     * The validation check on the resource name/path is not done
     * when the resource handle is constructed; rather, it is done
     * automatically as the resource is created.
     * </p>
     *
     * @param name the string name of the member file
     * @return the (handle of the) member file
     * @see #getFolder(String)
     */
    IFile getFile(String name);

    /**
     * Returns a handle to the folder with the given name in this folder.
     * <p>
     * This is a resource handle operation; neither the container
     * nor the result need exist in the workspace.
     * The validation check on the resource name/path is not done
     * when the resource handle is constructed; rather, it is done
     * automatically as the resource is created.
     * </p>
     *
     * @param name the string name of the member folder
     * @return the (handle of the) member folder
     * @see #getFile(String)
     */
    IFolder getFolder(String name);

    /**
     * Moves this resource so that it is located at the given path.
     * <p>
     * This is a convenience method, fully equivalent to:
     * </p>
     * 
     * <pre>
     * move(destination, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
     * </pre>
     * 
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event, including an indication
     * that this folder has been removed from its parent and a new folder
     * has been added to the parent of the destination.
     * </p>
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param destination the destination path
     * @param force a flag controlling whether resources that are not
     * in sync with the local file system will be tolerated
     * @param keepHistory a flag controlling whether files under this folder
     * should be stored in the workspace's local history
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this resource could not be moved. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource or one of its descendents is not local.</li>
     * <li> The resource corresponding to the parent destination path does not exist.</li>
     * <li> The resource corresponding to the parent destination path is a closed
     * project.</li>
     * <li> A resource at destination path does exist.</li>
     * <li> A resource of a different type exists at the destination path.</li>
     * <li> This resource or one of its descendents is out of sync with the local file system
     * and <code>force</code> is <code>false</code>.</li>
     * <li> The workspace and the local file system are out of sync
     * at the destination resource or one of its descendents.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     */
    void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;
}
