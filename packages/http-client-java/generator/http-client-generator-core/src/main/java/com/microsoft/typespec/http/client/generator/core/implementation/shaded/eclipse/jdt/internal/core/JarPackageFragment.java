/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;

import java.util.ArrayList;

/**
 * A package fragment that represents a package fragment found in a JAR.
 *
 * @see org.eclipse.jdt.core.IPackageFragment
 */
class JarPackageFragment extends PackageFragment {
    /**
     * Constructs a package fragment that is contained within a jar or a zip.
     */
    protected JarPackageFragment(PackageFragmentRoot root, String[] names) {
        super(root, names);
    }

    /**
     * Returns true if this fragment contains at least one java resource.
     * Returns false otherwise.
     */
    @Override
    public boolean containsJavaResources() throws JavaModelException {
        return ((JarPackageFragmentInfo) getElementInfo()).containsJavaResources();
    }

    /**
     * @see JavaElement
     */
    @Override
    protected JarPackageFragmentInfo createElementInfo() {
        return new JarPackageFragmentInfo();
    }

    /**
     * @see org.eclipse.jdt.core.IPackageFragment
     */
    @Override
    public IClassFile[] getAllClassFiles() throws JavaModelException {
        ArrayList<?> list = getChildrenOfType(CLASS_FILE);
        return list.toArray(IClassFile[]::new);
    }

    /**
     * A jar package fragment never contains compilation units.
     * 
     * @see org.eclipse.jdt.core.IPackageFragment
     */
    @Override
    public ICompilationUnit[] getCompilationUnits() {
        return NO_COMPILATION_UNITS;
    }

    /**
     * A package fragment in a jar has no corresponding resource.
     *
     * @see IJavaElement
     */
    @Override
    public IResource getCorrespondingResource() {
        return null;
    }

    @Override
    protected boolean internalIsValidPackageName() {
        return true;
    }

    /**
     * Jars and jar entries are all read only
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }

}
