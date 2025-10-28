/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.matching;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.builder.ClasspathLocation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.ResourceCompilationUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ClasspathSourceDirectory extends ClasspathLocation implements IModulePathEntry {

	final IContainer sourceFolder;
	final Map<String, Map<String, IResource>> directoryCache = new ConcurrentHashMap<>();
    final char[][] fullExclusionPatternChars;
	final char[][] fulInclusionPatternChars;

ClasspathSourceDirectory(IContainer sourceFolder, char[][] fullExclusionPatternChars, char[][] fulInclusionPatternChars) {
	this.sourceFolder = sourceFolder;
	this.fullExclusionPatternChars = fullExclusionPatternChars;
	this.fulInclusionPatternChars = fulInclusionPatternChars;
}

@Override
public void cleanup() {
	this.directoryCache.clear();
}

@Override
public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathSourceDirectory)) return false;

	return this.sourceFolder.equals(((ClasspathSourceDirectory) o).sourceFolder);
}

@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
	return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName);
}
@Override
public NameEnvironmentAnswer findClass(String sourceFileWithoutExtension, String qualifiedPackageName, String moduleName, String qualifiedSourceFileWithoutExtension) {
	Map<String, IResource> dirTable = directoryTable(qualifiedPackageName);
	if (dirTable != null && !dirTable.isEmpty()) {
		IFile file = (IFile) dirTable.get(sourceFileWithoutExtension);
		if (file != null) {
			return new NameEnvironmentAnswer(new ResourceCompilationUnit(file,
					this.module == null ? null : this.module.name()), null /* no access restriction */);
		}
	}
	return null;
}

@Override
public IPath getProjectRelativePath() {
	return this.sourceFolder.getProjectRelativePath();
}

@Override
public int hashCode() {
	return this.sourceFolder == null ? super.hashCode() : this.sourceFolder.hashCode();
}

@Override
public boolean isPackage(String qualifiedPackageName, String moduleName) {
	if (moduleName != null) {
		if (this.module == null || !moduleName.equals(String.valueOf(this.module.name())))
			return false;
	}
	return directoryTable(qualifiedPackageName) != null;
}
@Override
public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
	Map<String, IResource> dirTable = directoryTable(qualifiedPackageName);
	if (dirTable != null && !dirTable.isEmpty())
		return true;
	return false;
}

@Override
public void reset() {
	this.directoryCache.clear();
}

@Override
public String toString() {
	return "Source classpath directory " + this.sourceFolder.getFullPath().toString(); //$NON-NLS-1$
}

}
