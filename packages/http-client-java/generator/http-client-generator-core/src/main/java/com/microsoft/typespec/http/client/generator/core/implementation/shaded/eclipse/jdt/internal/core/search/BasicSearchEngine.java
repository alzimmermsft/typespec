/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for bug 215139 and bug 295894
 *     Microsoft Corporation - Contribution for bug 575562 - improve completion search performance
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.IJavaSearchScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchEngine;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaProject;

import java.util.HashSet;
import java.util.Set;

import static com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager.trace;

/**
 * Search basic engine. Public search engine (see {@link SearchEngine}
 * for detailed comment), now uses basic engine functionalities.
 * Note that search basic engine does not implement deprecated functionalities...
 */
public class BasicSearchEngine {
    /**
	 * For tracing purpose.
	 */
	public static boolean VERBOSE = false;

	/*
	 * Creates a new search basic engine.
	 */
	public BasicSearchEngine() {
		// will use working copies of PRIMARY owner
	}

	/**
	 * @see SearchEngine#SearchEngine(ICompilationUnit[]) for detailed comment.
	 */
	public BasicSearchEngine(ICompilationUnit[] workingCopies) {
    }

    /**
	 * @see SearchEngine#SearchEngine(WorkingCopyOwner) for detailed comment.
	 */
	public BasicSearchEngine(WorkingCopyOwner workingCopyOwner) {
    }

    /**
	 * @see SearchEngine#createJavaSearchScope(IJavaElement[]) for detailed comment.
	 */
	public static IJavaSearchScope createJavaSearchScope(IJavaElement[] elements) {
		return createJavaSearchScope(false, elements, true);
	}

    /**
	 * @see SearchEngine#createJavaSearchScope(boolean, IJavaElement[], boolean) for detailed comment.
	 */
	public static IJavaSearchScope createJavaSearchScope(boolean excludeTestCode, IJavaElement[] elements, boolean includeReferencedProjects) {
		int includeMask = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SYSTEM_LIBRARIES;
		if (includeReferencedProjects) {
			includeMask |= IJavaSearchScope.REFERENCED_PROJECTS;
		}
		return createJavaSearchScope(excludeTestCode, elements, includeMask);
	}

    /**
	 * @see SearchEngine#createJavaSearchScope(boolean, IJavaElement[], int) for detailed comment.
	 */
	public static IJavaSearchScope createJavaSearchScope(boolean excludeTestCode, IJavaElement[] elements, int includeMask) {
		Set<JavaProject> projectsToBeAdded = new HashSet<>(2);
		for (IJavaElement element : elements) {
			if (element instanceof JavaProject p) {
				projectsToBeAdded.add(p);
			}
		}
		JavaSearchScope scope = new JavaSearchScope(excludeTestCode);
		for (IJavaElement element : elements) {
			if (element != null) {
				try {
					if (projectsToBeAdded.contains(element)) {
						scope.add((JavaProject)element, includeMask, projectsToBeAdded);
					} else {
						scope.add(element);
					}
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}
		return scope;
	}

    /**
	 * @see SearchEngine#createWorkspaceScope() for detailed comment.
	 */
	public static IJavaSearchScope createWorkspaceScope() {
		return JavaModelManager.getJavaModelManager().getWorkspaceScope();
	}

    /**
	 * Returns a new default Java search participant.
	 *
	 * @return a new default Java search participant
	 * @since 3.0
	 */
	public static SearchParticipant getDefaultSearchParticipant() {
		return new JavaSearchParticipant();
	}

}
