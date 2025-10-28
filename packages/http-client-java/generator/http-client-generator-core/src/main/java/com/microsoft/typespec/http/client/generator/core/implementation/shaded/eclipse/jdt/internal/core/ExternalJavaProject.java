/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;

public class ExternalJavaProject extends JavaProject {

	/*
	 * Note this name can be surfaced in the UI (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=128258)
	 */
	public static final String EXTERNAL_PROJECT_NAME = " "; //$NON-NLS-1$

	public ExternalJavaProject(IClasspathEntry[] rawClasspath) {
		super(ResourcesPlugin.getWorkspace().getRoot().getProject(EXTERNAL_PROJECT_NAME), JavaModelManager.getJavaModelManager().getJavaModel());
		try {
			getPerProjectInfo().setRawClasspath(rawClasspath, defaultOutputLocation(), JavaModelStatus.VERIFIED_OK/*no .classpath format problem*/);
		} catch (JavaModelException e) {
			// getPerProjectInfo() never throws JavaModelException for an ExternalJavaProject
		}
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public boolean exists() {
		// external project never exists
		return false;
	}

    @Override
	protected IStatus validateExistence(IResource underlyingResource) {
		// allow opening of external project
		return JavaModelStatus.VERIFIED_OK;
	}
}
