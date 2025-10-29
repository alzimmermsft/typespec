/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClasspathChange {

    JavaProject project;
    IClasspathEntry[] oldRawClasspath;
    IPath oldOutputLocation;
    IClasspathEntry[] oldResolvedClasspath;

    public ClasspathChange(JavaProject project, IClasspathEntry[] oldRawClasspath, IPath oldOutputLocation,
        IClasspathEntry[] oldResolvedClasspath) {
        this.project = project;
        this.oldRawClasspath = oldRawClasspath;
        this.oldOutputLocation = oldOutputLocation;
        this.oldResolvedClasspath = oldResolvedClasspath;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClasspathChange))
            return false;
        return this.project.equals(((ClasspathChange) obj).project);
    }

    @Override
    public int hashCode() {
        return this.project.hashCode();
    }

    @Override
    public String toString() {
        return "ClasspathChange: " + this.project.getElementName(); //$NON-NLS-1$
    }

}
