/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;

/**
 * The element info for <code>PackageFragmentRoot</code>s.
 */
class PackageFragmentRootInfo extends OpenableElementInfo {

    /**
     * The SourceMapper for this JAR (or <code>null</code> if
     * this JAR does not have source attached).
     */
    protected SourceMapper sourceMapper = null;

    /**
     * The kind of the root associated with this info.
     * Valid kinds are: <ul>
     * <li><code>IPackageFragmentRoot.K_SOURCE</code>
     * <li><code>IPackageFragmentRoot.K_BINARY</code></ul>
     */
    protected int rootKind = IPackageFragmentRoot.K_SOURCE;

    private boolean ignoreOptionalProblems;
    private boolean initialized;

    /**
     * Create and initialize a new instance of the receiver
     */
    public PackageFragmentRootInfo() {
        this.nonJavaResources = null;
        this.initialized = false;
    }

    /**
     * Returns the kind of this root.
     */
    public int getRootKind() {
        return this.rootKind;
    }

    /**
     * Retuns the SourceMapper for this root, or <code>null</code>
     * if this root does not have attached source.
     */
    protected SourceMapper getSourceMapper() {
        return this.sourceMapper;
    }

    boolean ignoreOptionalProblems(PackageFragmentRoot packageFragmentRoot) throws JavaModelException {
        if (this.initialized == false) {
            this.ignoreOptionalProblems
                = ((ClasspathEntry) packageFragmentRoot.getRawClasspathEntry()).ignoreOptionalProblems();
            this.initialized = true;
        }
        return this.ignoreOptionalProblems;
    }

    /**
     * Sets the SourceMapper for this root.
     */
    protected void setSourceMapper(SourceMapper mapper) {
        this.sourceMapper = mapper;
    }
}
