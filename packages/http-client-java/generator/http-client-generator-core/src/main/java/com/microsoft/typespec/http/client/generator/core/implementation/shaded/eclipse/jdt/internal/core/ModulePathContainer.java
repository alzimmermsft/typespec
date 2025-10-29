/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;

public class ModulePathContainer implements IClasspathContainer {

    private final IJavaProject project;

    public ModulePathContainer(IJavaProject project) {
        this.project = project;
    }

    @Override
    public String getDescription() {
        //
        return "Module path"; //$NON-NLS-1$
    }

    @Override
    public int getKind() {
        //
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        //
        return new Path(JavaCore.MODULE_PATH_CONTAINER_ID);
    }

}
