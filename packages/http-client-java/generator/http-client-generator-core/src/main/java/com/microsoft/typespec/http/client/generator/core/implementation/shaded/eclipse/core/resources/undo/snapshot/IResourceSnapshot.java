/*******************************************************************************
 * Copyright (c) 2007, 2023 IBM Corporation and others.
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
 *     Red Hat Inc - Adapted from classes in org.eclipse.ui.ide.undo and org.eclipse.ui.internal.ide.undo
 ******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.undo.snapshot;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

/**
 * IResourceSnapshot is a lightweight snapshot that describes the common
 * attributes of a resource to be created.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.20
 */
public interface IResourceSnapshot<T extends IResource> {

    /**
     * Create a resource handle that can be used to create a resource from this
     * resource description. This handle can be used to create the actual
     * resource, or to describe the creation to a resource delta factory.
     *
     * @return the resource handle that can be used to create a resource from
     * this description
     */
    T createResourceHandle();

    /**
     * Get the name of this resource.
     *
     * @return the name of the Resource
     */
    String getName();

    /**
     * Return a boolean indicating whether this resource description has enough
     * information to create a resource.
     *
     * @return <code>true</code> if the resource can be created, and
     * <code>false</code> if it does not have enough information
     */
    boolean isValid();

    /**
     * Record the appropriate state of this resource description using
     * any available resource history.
     *
     * @param monitor
     * the progress monitor to be used
     * @throws CoreException in case of error
     */
    void recordStateFromHistory(IProgressMonitor monitor) throws CoreException;

    /**
     * Return a boolean indicating whether this description represents an
     * existent resource.
     *
     * @param checkMembers
     * Use <code>true</code> if members should also exist in order
     * for this description to be considered existent. A value of
     * <code>false</code> indicates that the existence of members
     * does not matter.
     *
     * @return a boolean indicating whether this description represents an
     * existent resource.
     */
    boolean verifyExistence(boolean checkMembers);
}
