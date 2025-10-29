/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 * Represents an entire Java type root (either an <code>ICompilationUnit</code>
 * or an <code>IClassFile</code>).
 *
 * <p>See {@link ICompilationUnit} Note that methods {@link #findPrimaryType()} and {@link #getElementAt(int)}
 * were already implemented in this interface respectively since version 3.0 and version 1.0.
 * <p>See {@link IClassFile} Note that method {@link #getWorkingCopy(WorkingCopyOwner, IProgressMonitor)}
 * was already implemented in this interface since version 3.0.
 * 
 * @since 3.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITypeRoot extends IJavaElement, IParent, IOpenable, ISourceReference {

    /**
     * Finds the primary type of this Java type root (that is, the type with the same name as the
     * compilation unit, or the type of a class file), or <code>null</code> if no such a type exists.
     *
     * @return the found primary type of this Java type root, or <code>null</code> if no such a type exists
     */
    IType findPrimaryType();

    /**
     * Returns the module description contained in this type root or null if there is no module
     * in this type root.
     * <p>Only subtype {@link IModularClassFile} promises to return non-null.</p>
     *
     * @since 3.14
     * @return the module description contained in the type root or null.
     */
    default IModuleDescription getModule() throws JavaModelException {
        return null;
    }

    /**
     * Returns the smallest element within this Java type root that
     * includes the given source position (that is, a method, field, etc.), or
     * <code>null</code> if there is no element other than the Java type root
     * itself at the given position, or if the given position is not
     * within the source range of the source of this Java type root.
     *
     * @param position a source position inside the Java type root
     * @return the innermost Java element enclosing a given source position or <code>null</code>
     * if none (excluding the Java type root).
     * @throws JavaModelException if the Java type root does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IJavaElement getElementAt(int position) throws JavaModelException;

}
