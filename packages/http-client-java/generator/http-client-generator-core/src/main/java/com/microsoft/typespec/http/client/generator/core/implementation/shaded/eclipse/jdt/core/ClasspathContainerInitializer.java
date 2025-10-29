/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     IBM Corporation - added support for requesting updates of a particular
 *                       container for generic container operations.
 * 						 - canUpdateClasspathContainer(IPath, IJavaProject)
 * 						 - requestClasspathContainerUpdate(IPath, IJavaProject, IClasspathContainer)
 *     IBM Corporation - allow initializers to provide a readable description
 *                       of a container reference, ahead of actual resolution.
 * 						 - getDescription(IPath, IJavaProject)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * Abstract base implementation of all classpath container initializer.
 * Classpath variable containers are used in conjunction with the
 * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpathContainerInitializer"
 * extension point.
 * <p>
 * Clients should subclass this class to implement a specific classpath
 * container initializer. The subclass must have a public 0-argument
 * constructor and a concrete implementation of {@link #initialize(IPath, IJavaProject)}.
 * <p>
 * Multiple classpath containers can be registered, each of them declares
 * the container ID they can handle, so as to narrow the set of containers they
 * can resolve, in other words, a container initializer is guaranteed to only be
 * activated to resolve containers which match the ID they registered onto.
 * <p>
 * In case multiple container initializers collide on the same container ID, the first
 * registered one will be invoked.
 *
 * @see IClasspathEntry
 * @see IClasspathContainer
 * @since 2.0
 */
public abstract class ClasspathContainerInitializer {

    /**
     * Status code indicating that an attribute is not modifiable.
     *
     *
     * @since 3.3
     */
    public static final int ATTRIBUTE_READ_ONLY = 2;

    /**
     * Creates a new classpath container initializer.
     */
    public ClasspathContainerInitializer() {
        // a classpath container initializer must have a public 0-argument constructor
    }

    /**
     * Binds a classpath container to a <code>IClasspathContainer</code> for a given project,
     * or silently fails if unable to do so.
     * <p>
     * A container is identified by a container path, which must be formed of two segments.
     * The first segment is used as a unique identifier (which this initializer did register onto), and
     * the second segment can be used as an additional hint when performing the resolution.
     * <p>
     * The initializer is invoked if a container path needs to be resolved for a given project, and no
     * value for it was recorded so far. The implementation of the initializer would typically set the
     * corresponding container using <code>JavaCore#setClasspathContainer</code>.
     * <p>
     * A container initialization can be indirectly performed while attempting to resolve a project
     * classpath using <code>IJavaProject#getResolvedClasspath(</code>; or directly when using
     * <code>JavaCore#getClasspathContainer</code>. During the initialization process, any attempt
     * to further obtain the same container will simply return <code>null</code> so as to avoid an
     * infinite regression of initializations.
     * <p>
     * A container initialization may also occur indirectly when setting a project classpath, as the operation
     * needs to resolve the classpath for validation purpose. While the operation is in progress, a referenced
     * container initializer may be invoked. If the initializer further tries to access the referring project classpath,
     * it will not see the new assigned classpath until the operation has completed. Note that once the Java
     * change notification occurs (at the end of the operation), the model has been updated, and the project
     * classpath can be queried normally.
     * <p>
     * This method is called by the Java model to give the party that defined
     * this particular kind of classpath container the chance to install
     * classpath container objects that will be used to convert classpath
     * container entries into simpler classpath entries. The method is typically
     * called exactly once for a given Java project and classpath container
     * entry. This method must not be called by other clients.
     * <p>
     * There are a wide variety of conditions under which this method may be
     * invoked. To ensure that the implementation does not interfere with
     * correct functioning of the Java model, the implementation should use
     * only the following Java model APIs:
     * <ul>
     * <li>{@link JavaCore#create(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot)}</li>
     * <li>{@link JavaCore#create(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject)}</li>
     * <li>{@link IJavaModel#getJavaProjects()}</li>
     * <li>Java element operations marked as "handle-only"</li>
     * </ul>
     * The effects of using other Java model APIs are unspecified.
     *
     * @param containerPath a two-segment path (ID/hint) identifying the container that needs
     * to be resolved
     * @param project the Java project in which context the container is to be resolved.
     * This allows generic containers to be bound with project specific values.
     * @throws CoreException if an exception occurs during the initialization
     */
    public abstract void initialize(IPath containerPath, IJavaProject project) throws CoreException;

    /**
     * Returns a readable description for a container path. A readable description for a container path can be
     * used for improving the display of references to container, without actually needing to resolve them.
     * A good implementation should answer a description consistent with the description of the associated
     * target container (see {@link IClasspathContainer#getDescription()}).
     *
     * @param containerPath the path of the container which requires a readable description
     * @param project the project from which the container is referenced
     * @return a string description of the container
     * @since 2.1
     */
    public String getDescription(IPath containerPath, IJavaProject project) {

        // By default, a container path is the only available description
        return containerPath.makeRelative().toString();
    }

}
