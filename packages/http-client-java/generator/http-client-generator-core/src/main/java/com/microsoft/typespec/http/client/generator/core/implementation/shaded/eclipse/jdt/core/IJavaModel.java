/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

/**
 * Represent the root Java element corresponding to the workspace.
 * Since there is only one such root element, it is commonly referred to as
 * <em>the</em> Java model element.
 * The Java model element needs to be opened before it can be navigated or manipulated.
 * The Java model element has no parent (it is the root of the Java element
 * hierarchy). Its children are <code>IJavaProject</code>s.
 * <p>
 * This interface provides methods for performing copy, move, rename, and
 * delete operations on multiple Java elements.
 * </p>
 * <p>
 * An instance of one of these handles can be created via
 * <code>JavaCore.create(workspace.getRoot())</code>.
 * </p>
 *
 * @see JavaCore#create(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot)
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IJavaModel extends IJavaElement, IOpenable, IParent {

    /**
     * Returns the Java project with the given name. The given name must be a valid
     * path segment as defined by {@link IPath#isValidSegment(String)}.
     * This is a handle-only method.
     * The project may or may not exist.
     *
     * @param name the name of the Java project
     * @return the Java project with the given name
     */
    IJavaProject getJavaProject(String name);

    /**
     * Returns the Java projects in this Java model, or an empty array if there
     * are none.
     *
     * @return the Java projects in this Java model, or an empty array if there
     * are none
     * @exception JavaModelException if this request fails.
     */
    IJavaProject[] getJavaProjects() throws JavaModelException;

}
