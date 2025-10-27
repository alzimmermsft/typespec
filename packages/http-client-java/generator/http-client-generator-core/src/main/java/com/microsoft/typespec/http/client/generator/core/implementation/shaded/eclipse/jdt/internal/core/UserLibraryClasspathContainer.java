/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;

public class UserLibraryClasspathContainer implements IClasspathContainer {

	private final String name;

	public UserLibraryClasspathContainer(String name) {
		this.name = name;
	}

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		UserLibrary library= getUserLibrary();
		if (library != null) {
			return library.getEntries();
		}
		return new IClasspathEntry[0];
	}

	@Override
	public String getDescription() {
		return this.name;
	}

	@Override
	public int getKind() {
		UserLibrary library= getUserLibrary();
		if (library != null && library.isSystemLibrary()) {
			return K_SYSTEM;
		}
		return K_APPLICATION;
	}

	@Override
	public IPath getPath() {
		return new Path(JavaCore.USER_LIBRARY_CONTAINER_ID).append(this.name);
	}

	private UserLibrary getUserLibrary() {
		UserLibrary userLibrary = JavaModelManager.getUserLibraryManager().getUserLibrary(this.name);
		if (userLibrary == null && (JavaModelManager.CP_RESOLVE_VERBOSE || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE)) {
			verbose_no_user_library_found(this.name);
		}
		return userLibrary;
	}

	private void verbose_no_user_library_found(String userLibraryName) {
		JavaModelManager.trace(
			"UserLibrary INIT - FAILED (no user library found)\n" + //$NON-NLS-1$
			"	userLibraryName: " + userLibraryName); //$NON-NLS-1$
	}
}
