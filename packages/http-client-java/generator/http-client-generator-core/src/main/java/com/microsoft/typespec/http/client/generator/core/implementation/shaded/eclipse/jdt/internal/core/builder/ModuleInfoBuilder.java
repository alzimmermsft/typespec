/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.builder;

import java.util.Locale;
import java.util.Map;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatusConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.IAttributeNamesConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.CompilationResult;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.Compiler;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ICompilerRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SimpleSetOfCharArray;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.CompilationGroup;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.ModuleUpdater;

/** Implementation of {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore#compileWithAttributes(IModuleDescription, Map)}. */
public class ModuleInfoBuilder {

	static class BytecodeCollector implements ICompilerRequestor {
		byte[] bytes;

		@Override
		public void acceptResult(CompilationResult result) {
			assert this.bytes == null : "duplicate result"; //$NON-NLS-1$
			ClassFile[] classFiles = result.getClassFiles();
			assert classFiles.length == 1;
			this.bytes = classFiles[0].getBytes();
		}
	}

	public byte[] compileWithAttributes(IModuleDescription module, Map<String,String> classFileAttributes) throws JavaModelException {
		IJavaProject javaProject = module.getJavaProject();
		NameEnvironment nameEnvironment = new NameEnvironment(javaProject, CompilationGroup.MAIN);

		addModuleUpdates(module, nameEnvironment.moduleUpdater, classFileAttributes);

		ClasspathMultiDirectory sourceLocation = getSourceLocation(javaProject, nameEnvironment, module);
		IFile file = (IFile) module.getCompilationUnit().getCorrespondingResource();
		ICompilationUnit[] sourceUnits = { new SourceFile(file, sourceLocation) };
		BytecodeCollector collector = new BytecodeCollector();
		Compiler newCompiler = new Compiler(
									nameEnvironment,
									DefaultErrorHandlingPolicies.exitOnFirstError(),
									new CompilerOptions(javaProject.getOptions(true)),
									collector,
									ProblemFactory.getProblemFactory(Locale.getDefault()));
		newCompiler.compile(sourceUnits);
		return collector.bytes;
	}

	private void addModuleUpdates(IModuleDescription module, ModuleUpdater moduleUpdater, Map<String,String> classFileAttributes) {
		String mainClassName = classFileAttributes.remove(String.valueOf(IAttributeNamesConstants.MODULE_MAIN_CLASS));
		if (mainClassName != null) {
			moduleUpdater.addModuleUpdate(module.getElementName(), m -> m.setMainClassName(mainClassName.toCharArray()), UpdateKind.MODULE);
		}
		String modulePackageNames = classFileAttributes.remove(String.valueOf(IAttributeNamesConstants.MODULE_PACKAGES));
		if (modulePackageNames != null) {
			SimpleSetOfCharArray namesSet = new SimpleSetOfCharArray();
			String[] providedNames = modulePackageNames.split(","); //$NON-NLS-1$
			for (String providedName : providedNames) {
				namesSet.add(providedName.trim().toCharArray());
			}
			moduleUpdater.addModuleUpdate(module.getElementName(),  m -> m.setPackageNames(namesSet), UpdateKind.MODULE);
		}
		if (!classFileAttributes.isEmpty()) {
			throw new IllegalArgumentException("Unsupported key(s): "+classFileAttributes.keySet().toString()); //$NON-NLS-1$
		}
	}

	private ClasspathMultiDirectory getSourceLocation(IJavaProject javaProject, NameEnvironment nameEnvironment, IModuleDescription module)
			throws JavaModelException {
		IPackageFragmentRoot root = (IPackageFragmentRoot) module.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		IResource rootResource = root.getCorrespondingResource();
		for (ClasspathMultiDirectory sourceLocation : nameEnvironment.sourceLocations) {
			if (sourceLocation.sourceFolder.equals(rootResource)) {
				return sourceLocation;
			}
		}
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH, module));
	}
}
