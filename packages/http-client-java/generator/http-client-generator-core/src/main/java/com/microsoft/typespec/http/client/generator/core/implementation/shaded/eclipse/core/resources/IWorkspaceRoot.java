/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;

/**
 * A root resource represents the top of the resource hierarchy in a workspace.
 * There is exactly one root in a workspace. The root resource has the following
 * behavior:
 * <ul>
 * <li>It cannot be moved or copied </li>
 * <li>It always exists.</li>
 * <li>Deleting the root deletes all of the children under the root but leaves the root itself</li>
 * <li>It is always local.</li>
 * <li>It is never a phantom.</li>
 * </ul>
 * <p>
 * Workspace roots implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkspaceRoot extends IContainer, IAdaptable {

    /**
     * Returns a handle to the project resource with the given name
     * which is a child of this root. The given name must be a valid
     * path segment as defined by {@link IPath#isValidSegment(String)}.
     * <p>
     * Note: This method deals exclusively with resource handles,
     * independent of whether the resources exist in the workspace.
     * With the exception of validating that the name is a valid path segment,
     * validation checking of the project name is not done
     * when the project handle is constructed; rather, it is done
     * automatically as the project is created.
     * </p>
     *
     * @param name the name of the project
     * @return a project resource handle
     * @see #getProjects()
     */
    IProject getProject(String name);

    /**
     * Returns the collection of projects which exist under this root.
     * The projects can be open or closed.
     * <p>
     * This is a convenience method, fully equivalent to <code>getProjects(IResource.NONE)</code>.
     * Hidden projects are <b>not</b> included.
     * </p>
     * 
     * @return an array of projects
     * @see #getProject(String)
     * @see IResource#isHidden()
     */
    IProject[] getProjects();

    /**
     * Returns the collection of projects which exist under this root.
     * The projects can be open or closed.
     * <p>
     * If the {@link #INCLUDE_HIDDEN} flag is specified in the member flags, hidden
     * projects will be included along with the others. If the {@link #INCLUDE_HIDDEN} flag
     * is not specified (recommended), the result will omit any hidden projects.
     * </p>
     *
     * @param memberFlags bit-wise or of member flag constants indicating which
     * projects are of interest (only {@link #INCLUDE_HIDDEN} is currently applicable)
     * @return an array of projects
     * @see #getProject(String)
     * @see IResource#isHidden()
     * @since 3.4
     */
    IProject[] getProjects(int memberFlags);
}
