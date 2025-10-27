/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.CompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.ImportContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.PackageDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.PackageFragment;

public class AssistCompilationUnit extends CompilationUnit {
	private final Map<IJavaElement, IElementInfo> infoCache;
	private final Map<JavaElement, Binding> bindingCache;
	public AssistCompilationUnit(ICompilationUnit compilationUnit, WorkingCopyOwner owner, Map<JavaElement, Binding> bindingCache, Map<IJavaElement, IElementInfo> infoCache) {
		super((PackageFragment)compilationUnit.getParent(), compilationUnit.getElementName(), owner);
		this.bindingCache = bindingCache;
		this.infoCache = infoCache;
	}

	@Override
	public CompilationUnitElementInfo getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return (CompilationUnitElementInfo) this.infoCache.get(this);
	}

	@Override
	public ImportContainer getImportContainer() {
		return new AssistImportContainer(this, this.infoCache);
	}

	@Override
	public PackageDeclaration getPackageDeclaration(String pkg) {
		return new AssistPackageDeclaration(this, pkg, this.infoCache);
	}

	@Override
	public IType getType(String typeName) {
		return new AssistSourceType(this, typeName, this.bindingCache, this.infoCache);
	}

	@Override
	public boolean hasChildren() throws JavaModelException {
		JavaElementInfo info = (JavaElementInfo)this.infoCache.get(this);
		return info.getChildren().length > 0;
	}
}
