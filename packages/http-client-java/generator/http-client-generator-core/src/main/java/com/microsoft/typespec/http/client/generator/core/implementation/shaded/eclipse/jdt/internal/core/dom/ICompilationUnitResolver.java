/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom;

import java.util.List;
import java.util.Map;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ASTRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.CompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.FileASTRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;

/**
 * This interface is used to resolve a jdt dom tree from source files.
 * It is contributed to via the compilationUnitResolver extension point.
 * This interface is currently internal only, and is not considered API.
 * This interface may be modified, changed, or removed at any time.
 *
* <p>
* <strong>EXPERIMENTAL</strong>. This class or interface has been added as
* part of a work in progress. There is no guarantee that this API will
* work or that it will remain the same. Please do not use this API without
* consulting with the Red Hat team.
* </p>
*
* See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2641 for discussion on possible
* changes to this interface
*/
public interface ICompilationUnitResolver {
	/**
	 * Parse the ASTs for the given source units using the following options.
	 *
	 * @param compilationUnits the compilation units to create ASTs for
	 * @param requestor the AST requestor that collects abstract syntax trees and bindings
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 */
	void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor);

	/**
	 * Parse the given source paths with the following options.
	 *
	 * @param sourceFilePaths the compilation units to create ASTs for
	 * @param encodings the given encoding for the source units
	 * @param requestor the AST requester that collects abstract syntax trees and bindings
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 */
	void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor);

	/**
	 * Convert the given source unit into a CompilationUnit using the following options.
	 *
	 * @param sourceUnit A source unit
	 * @param initialNeedsToResolveBinding Initial guess as to whether we need to resolve bindings
	 * @param project The project providing the context of the conversion
	 * @param classpaths A list of classpaths to use during this operation
	 * @param focalPosition a position to focus on, or -1 if N/A
	 * @param apiLevel Level of AST API desired.
	 * @param compilerOptions Compiler options. Defaults to JavaCore.getOptions().
	 * @param parsedUnitWorkingCopyOwner The working copy owner of the unit
	 * @param typeRootWorkingCopyOwner The working copy owner of the type
	 * @param flags Flags to to be used during this operation
	 * @param monitor A progress monitor
	 * @return A CompilationUnit
	 */
	CompilationUnit toCompilationUnit(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, final boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths, int focalPosition,
			int apiLevel, Map<String, String> compilerOptions, WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags, IProgressMonitor monitor);
}
