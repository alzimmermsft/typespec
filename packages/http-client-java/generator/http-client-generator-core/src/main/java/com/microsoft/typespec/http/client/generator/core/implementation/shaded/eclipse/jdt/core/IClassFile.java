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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a single <code>.class</code> file, holding the binary form
 * of either a type or a module:
 * <ul>
 * <li>A class file of type {@link IOrdinaryClassFile} has a single child of type <code>IType</code>,</li>
 * <li>a class file of type {@link IModularClassFile} has a single child of type <code>IModuleDescription</code>.</li>
 * </ul>
 * Class file elements need to be opened before they can be navigated.
 * If a class file cannot be parsed, its structure remains unknown. Use
 * <code>IJavaElement.isStructureKnown</code> to determine whether this is the
 * case.
 * <p>
 * Note: <code>IClassFile</code> extends <code>ISourceReference</code>.
 * Source can be obtained for a class file if and only if source has been attached to this
 * class file. The source associated with a class file is the source code of
 * the compilation unit it was (nominally) generated from.
 * </p>
 *
 * @see IPackageFragmentRoot#attachSource(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath,
 * com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath, IProgressMonitor)
 * @noimplement This interface is not intended to be implemented by clients.
 */

public interface IClassFile extends ITypeRoot {

    /**
     * Returns the bytes contained in this class file.
     *
     * @return the bytes contained in this class file
     *
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @since 3.3
     */
    byte[] getBytes() throws JavaModelException;

    /**
     * Returns the type contained in this class file.
     * This is a handle-only method. The type may or may not exist.
     *
     * @return the type contained in this class file
     * @throws UnsupportedOperationException when invoked on an instance representing a modular class file.
     * @deprecated should only be used as {@link IOrdinaryClassFile#getType()}.
     */
    @Deprecated
    IType getType();

    /**
     * Returns whether this type represents a class. This is not guaranteed to be
     * instantaneous, as it may require parsing the underlying file.
     *
     * @return <code>true</code> if the class file represents a class.
     *
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    boolean isClass() throws JavaModelException;

    /**
     * Returns whether this type represents an interface. This is not guaranteed to
     * be instantaneous, as it may require parsing the underlying file.
     *
     * @return <code>true</code> if the class file represents an interface.
     *
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    boolean isInterface() throws JavaModelException;
}
