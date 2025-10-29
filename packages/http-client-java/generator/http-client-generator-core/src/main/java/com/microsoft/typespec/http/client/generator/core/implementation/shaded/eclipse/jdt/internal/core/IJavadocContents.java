/*******************************************************************************
 * Copyright (c) 2024 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IField;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;

/**
 * Container for javadoc content on given elements
 */
public interface IJavadocContents {

    /**
     * Returns the part of the javadoc that describes the type
     */
    String getTypeDoc() throws JavaModelException;

    /**
     * Returns the part of the javadoc that describes the package
     */
    String getPackageDoc() throws JavaModelException;

    /**
     * Returns the part of the javadoc that describes the module
     */
    String getModuleDoc() throws JavaModelException;

    /**
     * Returns the part of the javadoc that describes a field of the type
     */
    String getFieldDoc(IField child) throws JavaModelException;

    /**
     * Returns the part of the javadoc that describe a method of the type
     */
    String getMethodDoc(IMethod child) throws JavaModelException;

}
