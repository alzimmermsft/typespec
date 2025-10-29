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
 *     Broadcom Corporation - build configurations and references
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

import java.net.URI;

/**
 * A project description contains the meta-data required to define
 * a project. In effect, a project description is a project's "content".
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectDescription {
    /**
     * Constant that denotes the name of the project description file (value
     * <code>".project"</code>).
     * The handle of a project's description file is
     * <code>project.getFile(DESCRIPTION_FILE_NAME)</code>.
     * The project description file is located in the root of the project's content area.
     *
     * @since 2.0
     */
    String DESCRIPTION_FILE_NAME = ".project"; //$NON-NLS-1$

    /**
     * Returns whether the project nature specified by the given
     * nature extension id has been added to the described project.
     *
     * @param natureId the nature extension identifier
     * @return <code>true</code> if the described project has the given nature
     */
    boolean hasNature(String natureId);

}
