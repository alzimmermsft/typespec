/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
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
 * Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 * Broadcom Corporation - build configurations and references
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;

import java.net.URI;

/**
 * A project is a type of resource which groups resources
 * into buildable, reusable units.
 * <p>
 * Features of projects include:
 * </p>
 * <ul>
 * <li>A project collects together a set of files and folders.</li>
 * <li>A project's location controls where the project's resources are
 * stored in the local file system.</li>
 * <li>A project's build spec controls how building is done on the project.</li>
 * <li>A project can carry session and persistent properties.</li>
 * <li>A project can be open or closed; a closed project is
 * passive and has a minimal in-memory footprint.</li>
 * <li>A project can have one or more project build configurations.</li>
 * <li>A project can carry references to other project build configurations.</li>
 * <li>A project can have one or more project natures.</li>
 * </ul>
 * <p>
 * Projects implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProject extends IContainer {
    /**
     * Option constant (value 1) indicating that a snapshot to be
     * loaded or saved contains a resource tree (refresh information).
     *
     * @since 3.6
     */
    int SNAPSHOT_TREE = 1;

    /**
     * Returns the description for this project.
     * The returned value is a copy and cannot be used to modify
     * this project. The returned value is suitable for use in creating,
     * copying and moving other projects.
     *
     * @return the description for this project
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * </ul>
     */
    IProjectDescription getDescription() throws CoreException;

    /**
     * Returns a handle to the file with the given name in this project.
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
     * Returns a handle to the folder with the given name in this project.
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
     * Returns the location in the local file system of the project-specific
     * working data area for use by the bundle/plug-in with the given identifier,
     * or <code>null</code> if the project does not exist.
     * <p>
     * The content, structure, and management of this area is
     * the responsibility of the bundle/plug-in. This area is deleted when the
     * project is deleted.
     * </p><p>
     * This project needs to exist but does not need to be open.
     * </p>
     * 
     * @param id the bundle or plug-in's identifier
     * @return a local file system path
     * @since 3.0
     */
    IPath getWorkingLocation(String id);

    /**
     * Returns whether the project nature specified by the given
     * nature extension id has been added to this project.
     *
     * @param natureId the nature extension identifier
     * @return <code>true</code> if the project has the given nature
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * </ul>
     */
    boolean hasNature(String natureId) throws CoreException;

    /**
     * Returns whether this project is open.
     * <p>
     * A project must be opened before it can be manipulated.
     * A closed project is passive and has a minimal memory
     * footprint; a closed project has no members.
     * </p>
     *
     * @return <code>true</code> if this project is open, <code>false</code> if
     * this project is closed or does not exist
     */
    boolean isOpen();
}
