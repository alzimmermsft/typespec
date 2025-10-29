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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * Interface of a classpath container.
 * A classpath container provides a way to indirectly reference a set of classpath entries through
 * a classpath entry of kind <code>CPE_CONTAINER</code>. Typically, a classpath container can
 * be used to describe a complex library composed of multiple JARs or projects, considering also
 * that containers can map to different set of entries on each project, in other words, several
 * projects can reference the same generic container path, but have each of them actually bound
 * to a different container object.
 * <p>
 * The set of entries associated with a classpath container may contain any of the following:
 * </p>
 * <ul>
 * <li> library entries (<code>CPE_LIBRARY</code>) </li>
 * <li> project entries (<code>CPE_PROJECT</code>) </li>
 * </ul>
 * In particular, a classpath container can neither reference further classpath containers or classpath variables.
 * <p>
 * A library entry can reference other libraries through the Class-Path section of the JAR's MANIFEST.MF file. If the
 * container wants such referenced entries to be part of the classpath, the container must explicitly add them to the
 * array returned from {@link #getClasspathEntries()}.
 * </p>
 * <p>
 * Classpath container values are persisted locally to the workspace, but are not preserved from a
 * session to another. It is thus highly recommended to register a <code>ClasspathContainerInitializer</code>
 * for each referenced container (through the extension point
 * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ClasspathContainerInitializer").
 * </p>
 * 
 * @see IClasspathEntry
 * @since 2.0
 */

public interface IClasspathContainer {

    /**
     * Kind for a container mapping to an application library
     */
    int K_APPLICATION = 1;

    /**
     * Kind for a container mapping to a system library
     */
    int K_SYSTEM = 2;

    /**
     * Answers a readable description of this container
     *
     * @return String - a string description of the container
     */
    String getDescription();

    /**
     * Answers the kind of this container. Can be either:
     * <ul>
     * <li><code>K_APPLICATION</code> if this container maps to an application library</li>
     * <li><code>K_SYSTEM</code> if this container maps to a system library</li>
     * <li><code>K_DEFAULT_SYSTEM</code> if this container maps to a default system library (library
     * implicitly contributed by the runtime).</li>
     * </ul>
     * Typically, system containers should be placed first on a build path.
     * 
     * @return the kind of this container
     */
    int getKind();

    /**
     * Answers the container path identifying this container.
     * A container path is formed by a first ID segment followed with extra segments, which
     * can be used as additional hints for resolving to this container.
     * <p>
     * The container ID is also used to identify a<code>ClasspathContainerInitializer</code>
     * registered on the extension point
     * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpathContainerInitializer",
     * which can
     * be invoked if needing to resolve the container before it is explicitly set.
     * </p>
     * 
     * @return IPath - the container path that is associated with this container
     */
    IPath getPath();

}
