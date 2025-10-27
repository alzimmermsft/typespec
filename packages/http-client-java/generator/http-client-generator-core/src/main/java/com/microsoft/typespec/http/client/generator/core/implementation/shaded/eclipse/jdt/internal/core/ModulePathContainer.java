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

import java.util.ArrayList;
import java.util.List;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathAttribute;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModulePathEntry;

public class ModulePathContainer implements IClasspathContainer{

	private final IJavaProject project;

	public ModulePathContainer(IJavaProject project) {
		this.project = project;
	}
	@Override
	public IClasspathEntry[] getClasspathEntries() {
		//
		List<IClasspathEntry> entries = new ArrayList<>();
		ModuleSourcePathManager manager = JavaModelManager.getModulePathManager();
		try {
			AbstractModule module = (AbstractModule) ((JavaProject)this.project).getModuleDescription();
			if (module == null)
				return new IClasspathEntry[0];
			for (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule.IModuleReference ref : module.getRequiredModules()) {
				IModulePathEntry entry = manager.getModuleRoot(new String(ref.name()));
				JavaProject refRoot = null;
				if (entry instanceof ProjectEntry) {
					refRoot = ((ProjectEntry) entry).project;
				}
				if (refRoot == null)
					continue;
				IPath path = refRoot.getPath();
				IClasspathAttribute moduleAttribute = new ClasspathAttribute(IClasspathAttribute.MODULE, "true"); //$NON-NLS-1$
				entries.add(JavaCore.newProjectEntry(path, ClasspathEntry.NO_ACCESS_RULES,
						false,
						new IClasspathAttribute[] {moduleAttribute}, ref.isTransitive()));
			}
		} catch (JavaModelException e) {
			// ignore
		}
		return entries.toArray(new IClasspathEntry[entries.size()]);
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
