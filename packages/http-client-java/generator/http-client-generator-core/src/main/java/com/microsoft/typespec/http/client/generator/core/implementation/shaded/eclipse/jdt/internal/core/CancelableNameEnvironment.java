/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.codeassist.ISearchRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.AbortCompilation;


public class CancelableNameEnvironment extends SearchableEnvironment implements INameEnvironmentWithProgress {
	private IProgressMonitor monitor;

	public CancelableNameEnvironment(JavaProject project, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		this(project, owner, monitor, false, JavaProject.NO_RELEASE);
	}
	public CancelableNameEnvironment(JavaProject project, WorkingCopyOwner owner, IProgressMonitor monitor, boolean excludeTestCode, int release) throws JavaModelException {
		super(project, owner, excludeTestCode, release);
		setMonitor(monitor);
	}

	private void checkCanceled() {
		if (this.monitor != null && this.monitor.isCanceled()) {
			if (NameLookup.VERBOSE)
				System.out.println(Thread.currentThread() + " CANCELLING LOOKUP "); //$NON-NLS-1$
			throw new AbortCompilation(true/*silent*/, new OperationCanceledException());
		}
	}

	@Override
	public void findPackages(char[] prefix, ISearchRequestor requestor) {
		checkCanceled();
		super.findPackages(prefix, requestor);
	}

	@Override
	public NameEnvironmentAnswer findType(char[] name, char[][] packageName) {
		checkCanceled();
		return super.findType(name, packageName);
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		checkCanceled();
		return super.findType(compoundTypeName);
	}
	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, boolean searchWithSecondaryTypes, char[] moduleName) {
		return findType(typeName, packageName, moduleName);
	}

	@Override
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
}
