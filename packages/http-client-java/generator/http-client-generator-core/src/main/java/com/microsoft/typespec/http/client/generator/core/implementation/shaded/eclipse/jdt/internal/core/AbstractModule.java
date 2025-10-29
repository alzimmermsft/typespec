/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ITypeRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule;

public interface AbstractModule extends IModuleDescription {

    /**
     * Handle for an automatic module.
     *
     * <p>Note, that by definition this is mostly a fake, only {@link #getElementName()} provides a useful value.</p>
     */
    class AutoModule extends NamedMember implements AbstractModule {

        private final boolean nameFromManifest;

        public AutoModule(JavaElement parent, String name, boolean nameFromManifest) {
            super(parent, name);
            this.nameFromManifest = nameFromManifest;
        }

        @Override
        public IJavaElement[] getChildren() throws JavaModelException {
            return JavaElement.NO_ELEMENTS; // may later answer computed details
        }

        @Override
        public int getFlags() throws JavaModelException {
            return 0;
        }

        public boolean isAutoNameFromManifest() {
            return this.nameFromManifest;
        }

        @Override
        public char getHandleMementoDelimiter() {
            return JavaElement.JEM_MODULE;
        }

        @Override
        public ITypeRoot getTypeRoot() {
            return null; // has no real CompilationUnit nor ClassFile
        }

    }

    // "forward declaration" for a method from JavaElement:
    Object getElementInfo() throws JavaModelException;

    default IModule getModuleInfo() throws JavaModelException {
        return (IModule) getElementInfo();
    }

    @Override
    default int getElementType() {
        return JAVA_MODULE;
    }
}
