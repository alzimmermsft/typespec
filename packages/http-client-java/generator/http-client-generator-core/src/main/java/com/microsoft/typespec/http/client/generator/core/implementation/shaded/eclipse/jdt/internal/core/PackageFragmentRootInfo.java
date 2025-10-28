/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJarEntryResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

/**
 * The element info for <code>PackageFragmentRoot</code>s.
 */
class PackageFragmentRootInfo extends OpenableElementInfo {

	/**
	 * The SourceMapper for this JAR (or <code>null</code> if
	 * this JAR does not have source attached).
	 */
	protected SourceMapper sourceMapper = null;

	/**
	 * The kind of the root associated with this info.
	 * Valid kinds are: <ul>
	 * <li><code>IPackageFragmentRoot.K_SOURCE</code>
	 * <li><code>IPackageFragmentRoot.K_BINARY</code></ul>
	 */
	protected int rootKind= IPackageFragmentRoot.K_SOURCE;

	private boolean ignoreOptionalProblems;
	private boolean initialized;

/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentRootInfo() {
	this.nonJavaResources = null;
	this.initialized = false;
}
/**
 * Starting at this folder, create non-java resources for this package fragment root
 * and add them to the non-java resources collection.
 *
 * @exception JavaModelException  The resource associated with this package fragment does not exist
 */
static Object[] computeFolderNonJavaResources(IPackageFragmentRoot root, IContainer folder, char[][] inclusionPatterns, char[][] exclusionPatterns) throws JavaModelException {
	IResource[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	try {
		IResource[] members = folder.members();
		int length = members.length;
		if (length > 0) {
			// if package fragment root refers to folder in another IProject, then
			// folder.getProject() is different than root.getJavaProject().getProject()
			// use the other java project's options to verify the name
			IJavaProject otherJavaProject = JavaCore.create(folder.getProject());
			String sourceLevel = "1.8";
			String complianceLevel = "1.8";
			JavaProject javaProject = (JavaProject) root.getJavaProject();
			IClasspathEntry[] classpath = javaProject.getResolvedClasspath();
            for (IResource member : members) {
                switch (member.getType()) {
                    case IResource.FILE:
                        String fileName = member.getName();

                        // ignore .java files that are not excluded
                        if (Util.isValidCompilationUnitName(fileName, sourceLevel, complianceLevel) && !Util.isExcluded(
                            member, inclusionPatterns, exclusionPatterns))
                            continue;
                        // ignore .class files
                        if (Util.isValidClassFileName(fileName, sourceLevel, complianceLevel))
                            continue;
                        // ignore .zip or .jar file on classpath
                        if (isClasspathEntry(member.getFullPath(), classpath))
                            continue;
                        break;

                    case IResource.FOLDER:
                        // ignore valid packages or excluded folders that correspond to a nested pkg fragment root
                        if (Util.isValidFolderNameForPackage(member.getName(), sourceLevel, complianceLevel) && (
                            !Util.isExcluded(member, inclusionPatterns, exclusionPatterns) || isClasspathEntry(
                                member.getFullPath(), classpath)))
                            continue;
                        break;
                }
                if (nonJavaResources.length == nonJavaResourcesCounter) {
                    // resize
                    System.arraycopy(nonJavaResources, 0,
                        (nonJavaResources = new IResource[nonJavaResourcesCounter * 2]), 0, nonJavaResourcesCounter);
                }
                nonJavaResources[nonJavaResourcesCounter++] = member;
            }
		}
		if (ExternalFoldersManager.isInternalPathForExternalFolder(folder.getFullPath())) {
			IJarEntryResource[] jarEntryResources = new IJarEntryResource[nonJavaResourcesCounter];
			for (int i = 0; i < nonJavaResourcesCounter; i++) {
				jarEntryResources[i] = new NonJavaResource(root, nonJavaResources[i]);
			}
			return jarEntryResources;
		} else if (nonJavaResources.length != nonJavaResourcesCounter) {
			System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter]), 0, nonJavaResourcesCounter);
		}
		return nonJavaResources;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}

    /**
 * Returns the kind of this root.
 */
public int getRootKind() {
	return this.rootKind;
}
/**
 * Retuns the SourceMapper for this root, or <code>null</code>
 * if this root does not have attached source.
 */
protected SourceMapper getSourceMapper() {
	return this.sourceMapper;
}
boolean ignoreOptionalProblems(PackageFragmentRoot packageFragmentRoot) throws JavaModelException {
	if (this.initialized == false) {
		this.ignoreOptionalProblems = ((ClasspathEntry) packageFragmentRoot.getRawClasspathEntry()).ignoreOptionalProblems();
		this.initialized = true;
	}
	return this.ignoreOptionalProblems;
}
private static boolean isClasspathEntry(IPath path, IClasspathEntry[] resolvedClasspath) {
	for (IClasspathEntry entry : resolvedClasspath) {
		if (entry.getPath().equals(path)) {
			return true;
		}
	}
	return false;
}

    /**
 * Sets the SourceMapper for this root.
 */
protected void setSourceMapper(SourceMapper mapper) {
	this.sourceMapper= mapper;
}
}
